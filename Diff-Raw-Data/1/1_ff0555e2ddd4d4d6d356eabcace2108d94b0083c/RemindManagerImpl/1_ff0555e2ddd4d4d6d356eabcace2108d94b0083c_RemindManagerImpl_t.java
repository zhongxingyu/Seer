 package core;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Timer;
 
 import org.apache.log4j.Logger;
 
 
 import polly.reminds.MyPlugin;
 
 import de.skuzzle.polly.sdk.AbstractDisposable;
 import de.skuzzle.polly.sdk.FormatManager;
 import de.skuzzle.polly.sdk.IrcManager;
 import de.skuzzle.polly.sdk.MailManager;
 import de.skuzzle.polly.sdk.MyPolly;
 import de.skuzzle.polly.sdk.PersistenceManager;
 import de.skuzzle.polly.sdk.UserManager;
 import de.skuzzle.polly.sdk.WriteAction;
 import de.skuzzle.polly.sdk.eventlistener.IrcUser;
 import de.skuzzle.polly.sdk.exceptions.CommandException;
 import de.skuzzle.polly.sdk.exceptions.DatabaseException;
 import de.skuzzle.polly.sdk.exceptions.DisposingException;
 import de.skuzzle.polly.sdk.exceptions.EMailException;
 import de.skuzzle.polly.sdk.model.User;
 import de.skuzzle.polly.sdk.roles.RoleManager;
 import de.skuzzle.polly.sdk.time.Milliseconds;
 import de.skuzzle.polly.sdk.time.Time;
 import entities.RemindEntity;
 
 
 
 public class RemindManagerImpl extends AbstractDisposable implements RemindManager {
     
     private final static RemindFormatter MAIL_FORMAT = new MailRemindFormatter();
     
     private final static String SUBJECT = "[Reminder] Erinnerung um %s";
     
     private final static long AUTO_SNOOZE_WAIT_TIME = Milliseconds.fromMinutes(5);
     
     public final static RemindFormatter DEFAULT_FORMAT = 
         PatternRemindFormatter.forPattern(MyPlugin.REMIND_FORMAT_VALUE);
     
     // XXX: special case for clum
     private final static RemindFormatter heidiFormat = new HeidiRemindFormatter();
     
     
     private MailManager mails;
     private IrcManager irc;
     private PersistenceManager persistence;
     private UserManager userManager;
     private RoleManager roleManager;
     private Timer remindScheduler;
     private Map<Integer, RemindTask> scheduledReminds;
     private Map<String, RemindFormatter> specialFormats;
     private Map<String, RemindEntity> sleeping;
     private ActionCounter actionCounter;
     private ActionCounter messageCounter;
     private FormatManager formatter;
     private RemindDBWrapper dbWrapper;
     private Logger logger;
     
     
     public RemindManagerImpl(MyPolly myPolly) {
         this.mails = myPolly.mails();
         this.irc = myPolly.irc();
         this.persistence = myPolly.persistence();
         this.userManager = myPolly.users();
         this.formatter = myPolly.formatting();
         this.roleManager = myPolly.roles();
         this.dbWrapper = new RemindDBWrapperImpl(myPolly.persistence());
         this.remindScheduler = new Timer("REMIND_SCHEDULER", true);
         this.scheduledReminds = new HashMap<Integer, RemindManager.RemindTask>();
         this.specialFormats = new HashMap<String, RemindFormatter>();
         this.sleeping = new HashMap<String, RemindEntity>();
         this.actionCounter = new ActionCounter();
         this.messageCounter = new ActionCounter();
         this.logger = Logger.getLogger(myPolly.getLoggerName(this.getClass()));
         
         // XXX: special case for clum:
         this.specialFormats.put("clum", heidiFormat);
     }
     
     
     
     @Override
     public RemindDBWrapper getDatabaseWrapper() {
         return this.dbWrapper;
     }
     
     
     
     @Override
     public synchronized void addRemind(final RemindEntity remind, boolean schedule) 
                 throws DatabaseException {
         logger.info("Adding " + remind + ", schedule: " + schedule);
         this.dbWrapper.addRemind(remind);
         
         if (schedule) {
             this.scheduleRemind(remind);
         }
         
         if (remind.isOnAction()) {
             logger.trace("Storing remind as on return action.");
             this.actionCounter.put(remind.getForUser());
         }
         if (remind.isMessage()) {
             logger.trace("Storing remind as leave message.");
             this.messageCounter.put(remind.getForUser());
         }
     }
     
     
 
     @Override
     public void deleteRemind(int id) throws DatabaseException {
         RemindEntity remind = this.dbWrapper.getRemind(id);
         logger.trace("Deleting remind with id " + id);
         this.deleteRemind(remind);
     }
 
     
     
     @Override
     public synchronized void deleteRemind(RemindEntity remind) throws DatabaseException {
         if (remind != null) {
             this.cancelScheduledRemind(remind.getId());
             this.dbWrapper.deleteRemind(remind);
         } else {
             logger.warn("tried to delete non-existent remind.");
         }
     }
     
     
     
     @Override
     public void deleteRemind(User executor, int id) 
             throws CommandException, DatabaseException {
         logger.debug("User '" + executor + " wants to delete remind with id " + id);
         RemindEntity remind = this.dbWrapper.getRemind(id);
         this.checkRemind(executor, remind, id);
         this.deleteRemind(remind);
     }
     
     
     
     @Override
     public synchronized void deliverRemind(RemindEntity remind, boolean checkIdleStatus) 
             throws DatabaseException, EMailException {
         User forUser = this.getUser(remind.getForUser());
         
         logger.info("Trying to deliver " + remind + " for " + forUser);
         try {
             if (remind.isMail()) {
                 this.deliverNowMail(remind, forUser);
             } else {
                 logger.trace("Remind is to be delivered in IRC. Checking user state");
                 boolean idle = this.isIdle(forUser) && checkIdleStatus;
                 boolean online = this.irc.isOnline(forUser.getCurrentNickName());
                 logger.trace("Idle state: " + idle + ", online state: " + online);
                 
                 
                 if (!online || idle) {
                     this.deliverLater(remind, forUser, idle, online);
                 } else {
                     this.deliverNowIrc(remind, forUser, online);
                 }
             }
         } finally {
             logger.trace("Now deleting " + remind);
             this.deleteRemind(remind);            
         }
     }
     
     
 
     @Override
     public void deliverLater(final RemindEntity remind, User forUser, boolean wasIdle, 
                 boolean online) throws DatabaseException, EMailException {
         logger.trace("Delivering later. Checking if remind schould be delivered as mail");
         boolean asMail = forUser.getAttribute(MyPlugin.LEAVE_AS_MAIL).equals("true");
         boolean doubleDelivery = forUser.getAttribute(
                 MyPlugin.REMIND_DOUBLE_DELIVERY).equals("true");
         logger.trace("As Mail: " + asMail);
         logger.trace("Double-delivery: " + doubleDelivery);
         
         if (asMail && wasIdle) {
             // user was idle and wanted email notification
             this.deliverNowIrc(remind, forUser, online);
             this.deliverNowMail(remind, forUser);
         } else if (asMail) {
             // user was offline and wanted email
             this.deliverNowMail(remind, forUser);
         } else if (wasIdle) {
             // user was online and wanted no email notification: notify now and when he 
             // returns
             this.deliverNowIrc(remind, forUser, online);
             if (doubleDelivery) {
                 RemindEntity onAction = new RemindEntity(remind.getMessage(), 
                     remind.getFromUser(), 
                     remind.getForUser(), 
                     remind.getOnChannel(), 
                     remind.getDueDate(), true);
                 
                 onAction.setIsMessage(true);
                 this.addRemind(onAction, false);
             }
         } else {
             RemindEntity message = new RemindEntity(remind.getMessage(), 
                 remind.getFromUser(), 
                 remind.getForUser(), 
                 remind.getOnChannel(), 
                 remind.getDueDate());
             message.setWasRemind(true);
             message.setIsMessage(true);
             this.addRemind(message, false);
         }
     }
     
     
     
     @Override
     public void deliverNowIrc(RemindEntity remind, User forUser, boolean online) {
         if (!online) {
             return;
         }
         logger.trace("Delivering " + remind + " now in IRC");
         RemindFormatter formatter = this.getFormat(forUser);
         
         String message = formatter.format(remind, this.formatter);
         boolean inChannel = this.irc.isOnChannel(
             remind.getOnChannel(), remind.getForUser());
     
         // If the user is not on the specified channel, the remind is delivered in query
         String channel = inChannel ? remind.getOnChannel() : remind.getForUser();
         // onAction messages are always delivered as qry
         if (remind.isOnAction()) {
             logger.trace("Remind was onAction. Removing it from onActionSet");
             channel = remind.getForUser();
             this.actionCounter.take(remind.getForUser());
         }
 
         // decrease counter of undelivered reminds for that user
         if (remind.isMessage()) {
             this.messageCounter.take(remind.getForUser());
         }
         this.irc.sendMessage(channel, message, this);
 
         
         boolean qry = channel.equals(remind.getForUser());
         if (qry && (!remind.getForUser().equals(remind.getFromUser()))) {
             // send notice to user who left this remind if it was delivered in qry
             this.irc.sendMessage(remind.getFromUser(), "Deine Nachricht an '" + 
                 remind.getForUser() + "' wurde zugestellt");
         }
         this.putToSleep(remind, forUser);
         this.checkTriggerAutoSnooze(forUser);
     }
     
     
     
     private final void checkTriggerAutoSnooze(User forUser) {
         if (!this.userManager.isSignedOn(forUser)) {
             return;
         } else if (forUser.getAttribute(MyPlugin.AUTO_SNOOZE).equals("false")) {
             return;
         }
         String indicator = forUser.getAttribute(MyPlugin.AUTO_SNOOZE_INDICATOR);
         this.irc.sendMessage(forUser.getCurrentNickName(), 
             "Auto Snooze aktiv. Schreibe '" + indicator + 
             "' um deine letzte Erinnerung zu verlngern.", this);
         
         
         new AutoSnoozeRunLater("AUTO_SNOOZE_WAITER", forUser, 
             AUTO_SNOOZE_WAIT_TIME, this.irc, this, this.formatter).start();
     }
     
     
     
     @Override
     public void deliverNowMail(RemindEntity remind, User forUser) 
                 throws DatabaseException, EMailException {
         logger.trace("Delivering " + remind + " now as mail");
         
         String mail = forUser.getAttribute(MyPlugin.EMAIL);
         if (mail.equals(MyPlugin.DEFAULT_EMAIL)) {
             logger.warn("Destination user has no valid email address set");
             RemindEntity r = new RemindEntity(
                 "Deine E-Mail Nachricht an " + remind.getForUser() + 
                 " konnte nicht zugestellt werden, da keine gltie E-mail Adresse " +
                 "hinterlegt ist.", 
                 this.irc.getNickname(), 
                 remind.getFromUser(), 
                 remind.getFromUser(),
                 Time.currentTime());
             // schedule this Remind for now so it will be delivered immediately.
             // if user is not online, it will automatically be delivered later
             // by the policy implemented in #deliverLater
             this.addRemind(r, true);
         } else {
             String subject = String.format(SUBJECT, 
                 this.formatter.formatDate(remind.getDueDate()));
             String message = MAIL_FORMAT.format(remind, this.formatter);
         
             this.mails.sendMail(mail, subject, message);
         }
     }
 
     
     
     @Override
     public void scheduleRemind(RemindEntity remind) {
         this.scheduleRemind(remind, remind.getDueDate());
     }
 
     
     
     @Override
     public void scheduleRemind(RemindEntity remind, Date dueDate) {
         logger.trace("Scheduling remind " + remind + ". Due date: " + dueDate);
         RemindTask task = new RemindTask(this, remind);
         synchronized (this.scheduledReminds) {
             this.scheduledReminds.put(remind.getId(), task);
         }
         this.remindScheduler.schedule(task, dueDate);
     }
 
     
     
     @Override
     public void cancelScheduledRemind(RemindEntity remind) {
         this.cancelScheduledRemind(remind.getId());
     }
 
     
     
     @Override
     public void cancelScheduledRemind(int id) {
         logger.trace("Cancelling scheduled remind with id " + id);
         RemindTask task = null;
         synchronized (this.scheduledReminds) {
             task = this.scheduledReminds.get(id);
             if (task != null) {
                 task.cancel();
                 this.scheduledReminds.remove(id);
             }
         }
     }
 
     
     
     @Override
     public void putToSleep(RemindEntity remind, User forUser) {
         logger.trace("Remembering " + remind + " for snooze");
         synchronized (this.sleeping) {
             this.sleeping.put(remind.getForUser(), remind);
         }
         // get sleep time:
         int sleepTime = Integer.parseInt(forUser.getAttribute(MyPlugin.SNOOZE_TIME));
         logger.trace("Snooze time for " + forUser + ": " + sleepTime);
         if (sleepTime > 0) {
             SleepTask task = new SleepTask(this, remind.getForUser());
             this.remindScheduler.schedule(task, sleepTime);
         }
     }
     
     
     
     @Override
     public RemindEntity cancelSleep(RemindEntity remind) {
         return this.cancelSleep(remind.getForUser());
     }
     
     
     
     @Override
     public RemindEntity cancelSleep(String forUser) {
         logger.trace("Cancelling snooze for user " + forUser);
         synchronized (this.sleeping) {
             return this.sleeping.remove(forUser);
         }
     }
     
     
     
     @Override
     public RemindEntity snooze(User executor, Date dueDate) 
             throws CommandException, DatabaseException {
         
         logger.trace("User " + executor + " requested snooze");
         RemindEntity existing;
         synchronized (this.sleeping) {
             existing = this.sleeping.get(executor.getCurrentNickName());
             this.cancelSleep(executor.getCurrentNickName());
         }
         
         if (existing == null) {
             throw new CommandException("Es existiert kein Remind fr dich das du " +
             		"verlngern kannst");
         }
         
         // if no explicit date is given, schedule new remind as long as the old was 
         // running
         if (dueDate == null) {
             logger.trace("No duedate given. Calculating runtime of remind to snooze.");
             long runtime = existing.getDueDate().getTime() - 
                 existing.getLeaveDate().getTime();
             dueDate = new Date(Time.currentTimeMillis() + runtime);
            logger.trace("Remind runtime is: " + this.formatter.formatTimeSpanMs(runtime));
         }
         
         RemindEntity newRemind = existing.copyForNewDueDate(dueDate);
         this.addRemind(newRemind, true);
         return newRemind;
     }
     
     
     
     @Override
     public RemindEntity snooze(User executor) throws DatabaseException,
             CommandException {
         
         boolean useSnoozeTime = executor.getAttribute(
             MyPlugin.USE_SNOOZE_TIME).equals("true");
         if (useSnoozeTime) {
             int defaultRemindTime = Integer.parseInt(
                 executor.getAttribute(MyPlugin.DEFAULT_REMIND_TIME));
             return this.snooze(executor, 
                 new Date(Time.currentTimeMillis() + defaultRemindTime));
         } else {
             return this.snooze(executor, null);
         }
         
     }
     
     
     
     @Override
     public RemindEntity toggleIsMail(User executor, int id)
             throws DatabaseException, CommandException {
         final RemindEntity remind = this.persistence.atomicRetrieveSingle(
             RemindEntity.class, id);
         
         logger.trace("Toggeling delivery of " + remind);
         this.checkRemind(executor, remind, id);
         this.persistence.atomicWriteOperation(new WriteAction() {
             @Override
             public void performUpdate(PersistenceManager persistence) {
                 remind.setIsMail(!remind.isMail());
             }
         });
         logger.trace("New delivery type: " + (remind.isMail() ? "Mail" : "IRC"));
         return remind;
     }
     
     
     
     @Override
     public User getUser(String nickName) {
         logger.trace("Getting user for name '" + nickName + "'");
         User u = this.userManager.getUser(nickName);
         if (u == null) {
             logger.trace("User is unknown, creating new one");
             u = this.userManager.createUser(nickName, "");
         }
         if (u.getCurrentNickName() == null) {
             u.setCurrentNickName(nickName);
         }
         
         return u;
     }
 
     
     
     @Override
     public RemindFormatter getFormat(User user) {
         RemindFormatter special = this.specialFormats.get(
             user.getCurrentNickName().toLowerCase());
         if (special != null) {
             return special;
         }
         
         String pattern = user.getAttribute(MyPlugin.REMIND_FORMAT_NAME);
         if (pattern == null) {
             return DEFAULT_FORMAT;
         }
         return PatternRemindFormatter.forPattern(pattern, true);
     }
 
 
 
     @Override
     public boolean isIdle(User user) {
         long lastMsg = Long.parseLong(user.getAttribute(MyPlugin.REMIND_IDLE_TIME));
         return Time.currentTimeMillis() - user.getLastMessageTime() > lastMsg;
     }
     
     
     
     @Override
     public boolean isOnActionAvailable(String forUser) {
         return this.actionCounter.available(forUser);
     }
     
     
     
     @Override
     public boolean isStale(String forUser) {
         return this.messageCounter.available(forUser);
     }
     
     
     
     @Override
     public void modifyRemind(User executor, int id, final Date dueDate, final String msg)
             throws CommandException, DatabaseException {
         logger.trace("User " + executor + " requested to modify remind with id " + id);
         final RemindEntity remind = this.dbWrapper.getRemind(id);
     
         this.checkRemind(executor, remind, id);
         this.cancelScheduledRemind(id);
         this.persistence.atomicWriteOperation(new WriteAction() {
             @Override
             public void performUpdate(PersistenceManager persistence) {
                 remind.setDueDate(dueDate);
                 remind.setMessage(msg);
             }
         });
         this.scheduleRemind(remind, dueDate);
     }
     
     
     
     @Override
     public boolean canEdit(User user, RemindEntity remind) {
         return remind.getForUser().equals(user.getCurrentNickName()) ||
             remind.getFromUser().equals(user.getCurrentNickName()) ||
             this.roleManager.hasPermission(user, 
                 MyPlugin.MODIFY_OTHER_REMIND_PERMISSION);
     }
     
     
     
     @Override
     public void checkRemind(User user, RemindEntity remind, int id) 
             throws CommandException {
         logger.trace("Checking for sufficient rights of user " + user + " for " + remind);
         if (remind == null) {
             throw new CommandException("Kein Remind mit der ID " + id);
         } else if (!canEdit(user, remind)) {
             throw new CommandException("Du kannst das Remind mit der ID " + id + 
                 " nicht ndern oder lschen");
         }
     }
     
     
     
     @Override
     public void traceNickChange(IrcUser oldUser, final IrcUser newUser) {
         logger.trace("tracing nickchange " + oldUser + " -> " + newUser);
         User oldForUser = this.getUser(oldUser.getNickName());
         if (oldForUser.getAttribute(MyPlugin.REMIND_TRACK_NICKCHANGE).equals("false")) {
             logger.trace("User doesnt want this nickchange to be tracked");
             return;
         }
         
         final List<RemindEntity> reminds = this.dbWrapper.getRemindsForUser(
             oldUser.getNickName());
         
         User newForUser = this.getUser(newUser.getNickName());
         this.actionCounter.moveUser(oldUser.getNickName(), newUser.getNickName());
         this.messageCounter.moveUser(oldUser.getNickName(), newUser.getNickName());
         
         try {
             // HACK: this resets the sleep time
             RemindEntity sleeping = this.cancelSleep(oldUser.getNickName());
             if (sleeping != null) {
                 this.putToSleep(sleeping, newForUser);
             }
             
             if (reminds.isEmpty()) {
                 return;
             }
             
             this.persistence.atomicWriteOperation(new WriteAction() {
                 
                 @Override
                 public void performUpdate(PersistenceManager persistence) {
                     for (RemindEntity remind : reminds) {
                         remind.setForUser(newUser.getNickName());
                     }
                 }
             });
         } catch (DatabaseException e) {
             
         }
     }
     
     
     
     @Override
     public void rescheduleAll() {
         logger.trace("Scheduling all existing reminds for their duedate");
         List<RemindEntity> allReminds = this.dbWrapper.getAllReminds();
         synchronized (this.scheduledReminds) {
             for (RemindEntity r : allReminds) {
                 logger.trace("Scheduling remind " + r + ". Due date: " + r.getDueDate());
                 RemindTask task = new RemindTask(this, r);
                 this.scheduledReminds.put(r.getId(), task);
                 this.remindScheduler.schedule(task, r.getDueDate());
                 
                 if (r.isMessage()) {
                     this.messageCounter.put(r.getForUser());
                 }
                 if (r.isOnAction()) {
                     this.actionCounter.put(r.getForUser());
                 }
             }
         }
     }
     
     
     
     @Override
     protected void actualDispose() throws DisposingException {
         this.remindScheduler.cancel();
         this.sleeping.clear();
         this.actionCounter.clear();
         this.scheduledReminds.clear();
         this.specialFormats.clear();
     }
 }
