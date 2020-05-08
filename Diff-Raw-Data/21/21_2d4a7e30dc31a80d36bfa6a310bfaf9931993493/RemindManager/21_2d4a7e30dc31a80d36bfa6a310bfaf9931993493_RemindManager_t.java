 package core;
 
 
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Timer;
 
 import org.apache.log4j.Logger;
 
 import polly.reminds.MyPlugin;
 
 import de.skuzzle.polly.sdk.Disposable;
 import de.skuzzle.polly.sdk.MyPolly;
 import de.skuzzle.polly.sdk.PersistenceManager;
 import de.skuzzle.polly.sdk.eventlistener.IrcUser;
 import de.skuzzle.polly.sdk.exceptions.DatabaseException;
 import de.skuzzle.polly.sdk.model.User;
 import entities.RemindEntity;
 
 
 
 public class RemindManager implements Disposable {
 
     private Logger logger;
     private PersistenceManager persistence;
     private MyPolly myPolly;
     private Timer remindScheduler;
     private Map<Integer, RemindTask> reminds;
     private Map<String, RemindEntity> sleeps;
     
     private final static RemindFormatter DEFAULT_FORMAT = 
             PatternRemindFormatter.forPattern(MyPlugin.REMIND_FORMAT_VALUE);
     
     
     
     public RemindManager(MyPolly myPolly) {
         this.logger = Logger.getLogger(myPolly.getLoggerName(this.getClass()));
         this.persistence = myPolly.persistence();
         this.myPolly = myPolly;
         this.remindScheduler = new Timer("REMIND_SCHEDULER");
         this.reminds = Collections.synchronizedMap(new HashMap<Integer, RemindTask>());
         this.sleeps = Collections.synchronizedMap(new HashMap<String, RemindEntity>());
     }
 
     
     
    public synchronized void scheduleRemind(RemindEntity remind, Date dueDate) {
         logger.info("Scheduling Remind: " + remind);
         RemindTask task = new RemindTask(remind, this);
         this.remindScheduler.schedule(task, dueDate);
         this.reminds.put(remind.getId(), task);
     }
     
     
     
     public RemindEntity getRemind(int id) {
         try {
             this.persistence.readLock();
             return this.persistence.find(RemindEntity.class, id);
         } finally {
             this.persistence.readUnlock();
         }
     }
     
     
     
    public synchronized void unSchedule(int remindId) {
         RemindTask t = this.reminds.get(remindId);
         if (t != null) {
             logger.debug("Cancelling remind task for remind id " + remindId);
             t.cancel();
             this.reminds.remove(remindId);
         }
     }
     
     
     
    public synchronized void deliverRemind(RemindEntity remind) {
         logger.debug("Executing Remind: " + remind);
         this.persistence.refresh(remind);
         
         if (!this.myPolly.irc().isOnline(remind.getForUser())) {
             logger.debug("User is not online. Remind will be delivered when he returns.");
             try {
                 this.persistence.writeLock();
                 this.persistence.startTransaction();
                 remind.setIsMessage(true);
                 remind.setWasRemind(true);
                 this.persistence.commitTransaction();
             } catch (Exception e) {
                 e.printStackTrace();
             } finally {
                 this.persistence.writeUnlock();
             }
             return;
         }
         
         RemindFormatter formatter = RemindManager.DEFAULT_FORMAT;
         IrcUser tmp = new IrcUser(remind.getForUser(), "", "");
         if (this.myPolly.users().isSignedOn(tmp)) {
             User user = this.myPolly.users().getUser(tmp);
             formatter = this.formatForUser(user);
         }
         
         String message = formatter.formatRemind(remind, this.myPolly.formatting());
     
         myPolly.irc().sendMessage(remind.getOnChannel(), message);
         
         this.putToSleep(remind);
         this.deleteRemind(remind);
     }
     
     
     
     private void putToSleep(RemindEntity remind) {
         String sleepString = MyPlugin.SLEEP_DEFAULT_VALUE;
         
         IrcUser tmp = new IrcUser(remind.getForUser(), "", "");
         if (this.myPolly.users().isSignedOn(tmp)) {
             User user = this.myPolly.users().getUser(tmp);
             
             try {
                 this.persistence.readLock();
                 sleepString = user.getAttribute(MyPlugin.SLEEP_TIME);
             } finally {
                 this.persistence.readUnlock();
             }
         }
         
         int sleepTime = Integer.parseInt(sleepString);
         this.sleeps.put(remind.getForUser(), remind);
         if (sleepTime > 0) {
             logger.trace("Scheduling remind for snooze for " + sleepString + "ms.");
             this.remindScheduler.schedule(new SleepTask(remind.getForUser(), this), 
                 sleepTime);
         } else {
             logger.trace("Remind will be sleepable forever");
         }
     }
     
     
     
     private RemindFormatter formatForUser(User user) {
         String pattern = user.getAttribute(MyPlugin.REMIND_FORMAT_NAME);
         if (pattern == null) {
             return RemindManager.DEFAULT_FORMAT;
         }
         return PatternRemindFormatter.forPattern(pattern, true);
     }
     
     
     
     public RemindEntity getSleep(String nickName) {
         return this.sleeps.get(nickName);
     }
     
     
     
     public void removeSleep(String nickName) {
         this.sleeps.remove(nickName);
     }
     
     
     
     public List<RemindEntity> undeliveredReminds(IrcUser user) {
         try {
             this.persistence.readLock();
             return this.persistence.findList(RemindEntity.class, "UNDELIVERED_FOR_USER", 
                     user.getNickName());
         } finally {
             this.persistence.readUnlock();
         }
     }
     
     
     
     public void addRemind(RemindEntity remind) {
         logger.debug("Adding Remind: " + remind);
         try {
             this.persistence.writeLock();
             this.persistence.startTransaction();
             this.persistence.persist(remind);
             this.persistence.commitTransaction();
         } catch (DatabaseException e) {
             e.printStackTrace();
         } finally {
             this.persistence.writeUnlock();
         }
     }
     
     
     
     public void deleteRemind(RemindEntity remind) {
         this.deleteRemind(remind.getId());
     }
     
     
     
     public void deleteRemind(int id) {
         logger.debug("Removing Remind with id " + id);
         try {
             this.persistence.writeLock();
             this.reminds.remove(id);
             RemindEntity r = this.persistence.find(RemindEntity.class, id);
             
             this.persistence.startTransaction();
             this.persistence.remove(r);
             this.persistence.commitTransaction();
         } catch (DatabaseException e) {
             e.printStackTrace();
         } finally {
             this.persistence.writeUnlock();
         }
     }
     
     
     
     public List<RemindEntity> getAllReminds() {
         try {
             this.persistence.readLock();
             return this.persistence.findList(RemindEntity.class, "ALL_REMINDS");
         } finally {
             this.persistence.readUnlock();
         }
     }
     
     
     
     public List<RemindEntity> getRemindsForUser(String nickName) {
         try {
             this.persistence.readLock();
             return this.persistence.findList(
                     RemindEntity.class, "REMIND_FOR_USER", nickName);
         } finally {
             this.persistence.readUnlock();
         }
     }
     
     
     
    public synchronized void traceNickChange(IrcUser oldUser, IrcUser newUser) {
         List<RemindEntity> reminds = this.getRemindsForUser(oldUser.getNickName());
         if (reminds.isEmpty()) {
             return;
         }
         
         try {
             logger.debug("Tracing Nickchange: " + oldUser + " to " + newUser);
             
             this.persistence.writeLock();
             
             RemindEntity rmd = this.sleeps.get(oldUser.getNickName());
             this.sleeps.put(oldUser.getNickName(), null);
             this.sleeps.put(newUser.getNickName(), rmd);
             
             this.persistence.startTransaction();
             for (RemindEntity remind : reminds) {
                 logger.trace("Setting receiver for remind " + remind.getId() + 
                         " from " + oldUser + " to " + newUser);
                 remind.setForUser(newUser.getNickName());
             }
             this.persistence.commitTransaction();
         } catch (DatabaseException e) {
             e.printStackTrace();
         } finally {
             this.persistence.writeUnlock();
         }
     }
 
 
 
     @Override
     public void dispose() {
         logger.debug("Shutting down remind scheduler.");
         this.remindScheduler.cancel();
     }
 }
