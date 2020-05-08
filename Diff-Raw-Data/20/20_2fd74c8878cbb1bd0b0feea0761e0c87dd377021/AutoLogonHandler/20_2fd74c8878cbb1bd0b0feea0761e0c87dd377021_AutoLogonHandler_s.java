 package de.skuzzle.polly.core.eventhandler;
 
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.log4j.Logger;
 
 import de.skuzzle.polly.core.internal.DefaultUserAttributesProvider;
 import de.skuzzle.polly.core.internal.irc.IrcManagerImpl;
 import de.skuzzle.polly.core.internal.users.UserManagerImpl;
 import de.skuzzle.polly.sdk.AbstractDisposable;
 import de.skuzzle.polly.sdk.Types.BooleanType;
 import de.skuzzle.polly.sdk.User;
 import de.skuzzle.polly.sdk.eventlistener.ConnectionEvent;
 import de.skuzzle.polly.sdk.eventlistener.ConnectionListener;
 import de.skuzzle.polly.sdk.eventlistener.MessageAdapter;
 import de.skuzzle.polly.sdk.eventlistener.MessageEvent;
 import de.skuzzle.polly.sdk.eventlistener.NickChangeEvent;
 import de.skuzzle.polly.sdk.eventlistener.NickChangeListener;
 import de.skuzzle.polly.sdk.eventlistener.SpotEvent;
 import de.skuzzle.polly.sdk.eventlistener.UserEvent;
 import de.skuzzle.polly.sdk.eventlistener.UserListener;
 import de.skuzzle.polly.sdk.eventlistener.UserSpottedListener;
 import de.skuzzle.polly.sdk.exceptions.AlreadySignedOnException;
 import de.skuzzle.polly.sdk.exceptions.DisposingException;
 import de.skuzzle.polly.sdk.exceptions.UnknownUserException;
 import de.skuzzle.polly.tools.concurrent.ThreadFactoryBuilder;
 
 
 
 public class AutoLogonHandler extends AbstractDisposable
         implements UserSpottedListener, NickChangeListener, UserListener, 
                    ConnectionListener {
     
     //private final static User NICKSERV = new polly.core.users.User("NickServ", "", 0);
     static {
         //NICKSERV.setCurrentNickName("NickServ");
     }
     
     
     
     private class AutoLogonRunnable implements Runnable {
         
         private String forUser;
         private boolean canceled;
         
         
         public AutoLogonRunnable(String forUser) {
             this.forUser = forUser;
         }
         
         
         public void cancel() {
             this.canceled = true;
         }
         
         
         @Override
         public void run() {
             if (this.canceled) {
                 return;
             }
             
             // ISSUE 0000091: request status for the user from nickserv
             synchronized (scheduledLogons) {
                 if (scheduledLogons.containsKey(this.forUser)) {
                     scheduledLogons.remove(this.forUser);
                     ircManager.sendRawCommand("NICKSERV STATUS " + this.forUser);
                 }
             }
         }
     }
     
     
     
     private MessageAdapter autoSignOnHandler = new MessageAdapter() {
         @Override
         public void noticeMessage(MessageEvent e) {
             if (!e.getUser().getNickName().equalsIgnoreCase("nickserv")) {
                 return;
             }
             String[] parts = e.getMessage().split(" ");
             if (parts.length != 3 || 
                     !parts[0].equalsIgnoreCase("status") || 
                     !parts[2].equals("3")) {
                 return;
             }
             String forUser = parts[1];
             try {
                 userManager.logonWithoutPassword(forUser);
             } catch (AlreadySignedOnException e1) {
                 logger.trace("User logged in while waiting for auto logon");
             } catch (UnknownUserException e1) {
                 logger.error("Error while auto logon. User '" + forUser + 
                     "' unknown", e1);
             }
         }
     };
     
 
     private static Logger logger = Logger.getLogger(AutoLogonHandler.class.getName());
     
  
     private IrcManagerImpl ircManager;
     private UserManagerImpl userManager;
     private ScheduledExecutorService autoLogonExecutor;
     private Map<String, AutoLogonRunnable> scheduledLogons;
     private int autoLoginTime;
     
     
     
     public AutoLogonHandler(IrcManagerImpl ircManager, UserManagerImpl userManager, 
             int autoLoginTime) {
         
         ircManager.addMessageListener(this.autoSignOnHandler);
         this.ircManager = ircManager;
         this.userManager = userManager;
         this.autoLogonExecutor = Executors.newScheduledThreadPool(1, 
                 new ThreadFactoryBuilder("LOGON"));
         this.scheduledLogons = new HashMap<String, AutoLogonRunnable>();
         this.autoLoginTime = autoLoginTime;
     }
 
 
 
     @Override
     public void userSpotted(SpotEvent e) {
         final String forUser = e.getUser().getNickName();
         if (this.userExists(forUser)) {
             // try instant login if user just spotted 
             this.ircManager.sendRawCommand("NICKSERV STATUS " + forUser);
         }
         this.scheduleAutoLogon(e.getUser().getNickName());
     }
 
     
     
     @Override
     public void nickChanged(NickChangeEvent e) {
         /*
          * If there is a auto logon scheduled for the old nickname, it will be 
          * canceled. If there is a registered user with the new nickname that is 
          * currently not logged on, a new auto logon is scheduled for that user. 
          */
         synchronized (this.scheduledLogons) {
             AutoLogonRunnable alr = this.scheduledLogons.get(e.getOldUser().getNickName());
             
             if (alr != null) {
                 alr.cancel();
                 this.scheduledLogons.remove(e.getOldUser().getNickName());
                 logger.debug("Auto logon for " + e.getOldUser() + " canceled");
             }
             
             User u = this.userManager.getUser(e.getNewUser().getNickName());
             if (u != null && !this.userManager.isSignedOn(u)) {
                 this.scheduleAutoLogon(e.getNewUser().getNickName());
             }
         }
     }
 
     
     
     private void scheduleAutoLogon(String forUser) {
         synchronized (this.scheduledLogons) {
             if (this.userExists(forUser)) {
                 AutoLogonRunnable runMe = new AutoLogonRunnable(forUser);
                 this.scheduledLogons.put(forUser, runMe);
                 this.autoLogonExecutor.schedule(runMe, this.autoLoginTime, 
                         TimeUnit.MILLISECONDS);
                 logger.debug("Auto logon for " + forUser + " scheduled");
             }
         }
     }
     
     
     
     private boolean userExists(String name) {
         User user = this.userManager.getUser(name);
         final BooleanType bool = (BooleanType) 
             user.getAttribute(DefaultUserAttributesProvider.AUTO_LOGON);
        return user != null && bool.getValue();
     }
     
     
 
     @Override
     public void userSignedOn(UserEvent e) {
         synchronized (this.scheduledLogons) {
             AutoLogonRunnable alr = this.scheduledLogons.get(e.getUser().getName());
             if (alr != null) {
                 logger.trace("Removing scheduled auto logon for user " + 
                     e.getUser().getName());
                 alr.cancel();
                 this.scheduledLogons.remove(e.getUser().getName());
             }
         }
     }
 
 
 
     @Override
     protected void actualDispose() throws DisposingException {
         this.autoLogonExecutor.shutdown();
         try {
             this.autoLogonExecutor.awaitTermination(1000, TimeUnit.MILLISECONDS);
         } catch (InterruptedException ignore) {}
     }
     
     
     
     @Override
     public void userSignedOff(UserEvent ignore) {}
 
     @Override
     public void userLost(SpotEvent ignore) {}
 
 
 
     @Override
     public void ircConnectionEstablished(ConnectionEvent e) {
     }
 
 
 
     @Override
     public void ircConnectionLost(ConnectionEvent e) {
     }
 }
