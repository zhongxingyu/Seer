 package core;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import core.filters.DateLogFilter;
 
 import polly.logging.MyPlugin;
 
 
 import de.skuzzle.polly.sdk.FormatManager;
 import de.skuzzle.polly.sdk.MailManager;
 import de.skuzzle.polly.sdk.MyPolly;
 import de.skuzzle.polly.sdk.UserManager;
 import de.skuzzle.polly.sdk.eventlistener.MessageAdapter;
 import de.skuzzle.polly.sdk.eventlistener.MessageEvent;
 import de.skuzzle.polly.sdk.eventlistener.MessageListener;
 import de.skuzzle.polly.sdk.exceptions.DatabaseException;
 import de.skuzzle.polly.sdk.exceptions.EMailException;
 import de.skuzzle.polly.sdk.model.User;
 import entities.LogEntry;
 
 
 public class ForwardHighlightHandler extends MessageAdapter {
     
     private final static int HIGHLIGHT_DELAY = 30000; // 30s
     
     private final static String SUBJECT = "[POLLY Highlight Forwarder] Highlight in %s";
     
     private final static String MESSAGE = "Hi %s,\n\nDu wurdest im Channel %s " +
     		"von %s gehighlighted. Nachricht:\n%s\n\n " +
     		"Channellog:\n%s\n\n" +
     		"Bye\nPolly";
     
     
     public final static long MAIL_DELAY = 30000; // 30 seconds
     
     private MailManager mailManager;
     private UserManager userManager;
     private Map<String, Long> timestamps;
     private PollyLoggingManager logManager;
     private LogFormatter logFormatter;
     private FormatManager formatManager;
     
     
     
     private class Highlight extends Thread implements MessageListener {
 
         private MessageEvent e;
         private User user;
         
         
         public Highlight(MessageEvent e, User user) {
             super("HL_FOR_" + e.getUser().getNickName());
             this.e = e;
             this.user = user;
             e.getSource().addMessageListener(this);
         }
         
         
 
         @Override
         public void run() {
             
             try {
                 Thread.sleep(HIGHLIGHT_DELAY);
             } catch (InterruptedException e) {
                 return;
             } finally {
                 this.e.getSource().removeMessageListener(this);
             }
             
             try {
                 String mail = this.user.getAttribute("EMAIL");
                 
                 List<LogEntry> prefiltered = logManager.preFilterChannel(
                     e.getChannel());
                 
                 prefiltered = logManager.postFilter(prefiltered, 
                         new DateLogFilter(new Date(this.user.getLastIdleTime())));
                 
                 Collections.reverse(prefiltered);
                 
                 String logs = formatList(prefiltered);
                 String subject = String.format(SUBJECT, this.e.getChannel());
                 String message = String.format(MESSAGE, 
                     this.user.getName(), this.e.getChannel(), this.e.getUser(), 
                     this.e.getMessage(), logs);
             
                 mailManager.sendMail(mail, subject, message);
             } catch (DatabaseException e1) {
                 e1.printStackTrace();
             } catch (EMailException e1) {
                 e1.printStackTrace();
             }
         }
 
         
         
         private void checkCancel(MessageEvent e) {
             // cancel highlight if user reacts within time
             if (e.getUser().getNickName().equals(this.user.getCurrentNickName())) {
                 this.interrupt();
             }
         }
 
 
         @Override
         public void publicMessage(MessageEvent e) {
             this.checkCancel(e);
         }
 
         
         
         @Override
         public void actionMessage(MessageEvent e) {
             this.checkCancel(e);
         }
 
         
 
         @Override
         public void privateMessage(MessageEvent ignore) {}
 
 
 
         @Override
         public void noticeMessage(MessageEvent ignore) {}
     }
     
     
     
     public ForwardHighlightHandler(MyPolly myPolly, PollyLoggingManager logManager) {
         this.mailManager = myPolly.mails();
         this.userManager = myPolly.users();
         this.formatManager = myPolly.formatting();
         this.timestamps = new HashMap<String, Long>();
         this.logManager = logManager;
         this.logFormatter = new DefaultLogFormatter();
     }
     
     
     
     public void publicMessage(MessageEvent e) {
         this.forwardHighlight(e);
     }
 
     
 
     @Override
     public void actionMessage(MessageEvent e) {
         this.forwardHighlight(e);
     }
     
     
     
     private void forwardHighlight(MessageEvent e) {
         Collection<User> allUsers = this.userManager.getRegisteredUsers();
         
         for (User user : allUsers) {
             
             // if user is offline, the nick to check is the username, otherwise the 
             // current nickname
             String nick = user.getCurrentNickName() == null 
                 ? user.getName() 
                 : user.getCurrentNickName();
                 
             // ignore self highlighting
             if (e.getUser().getNickName().equals(nick)) {
                 continue;
            } else if (!e.getSource().isOnChannel(e.getChannel(), nick)) {
                // ignore if user is not on that channel
                continue;
             }
             boolean hl = e.getMessage().toLowerCase().contains(nick.toLowerCase());
             
             if (!hl) {
                 continue;
             }
             
             String mail = user.getAttribute("EMAIL");
             
             // forward if user is idle, wants forward and has a mail address set
             boolean fwd = user.isIdle() &&
                 user.getAttribute(MyPlugin.FORWARD_HIGHLIGHTS).equals("true") &&
                 !mail.equals("none");
 
             if (fwd && this.canSend(mail)) {
                 new Highlight(e, user).start();
                 
             }
         }
     }
     
     
     
     private String formatList(List<LogEntry> logs) {
         StringBuilder b = new StringBuilder();
         for (LogEntry logEntry : logs) {
             b.append(this.logFormatter.formatLog(logEntry, this.formatManager));
             b.append(System.lineSeparator());
         }
         return b.toString();
     }
     
     
     
     private boolean canSend(String recipient) {
         synchronized (this.timestamps) {
             Long ts = this.timestamps.get(recipient);
             if (ts == null) {
                 this.timestamps.put(recipient, System.currentTimeMillis());
                 return true;
             }
             long diff = System.currentTimeMillis() - ts;
             if (diff < MAIL_DELAY) {
                 return false;
             } else {
                 this.timestamps.put(recipient, System.currentTimeMillis());
                 return true;
             }
         }
     }
 }
