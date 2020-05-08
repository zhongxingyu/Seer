 package polly.eventhandler;
 
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.log4j.Logger;
 
 import polly.EventThreadFactory;
 import polly.PollyConfiguration;
 import polly.core.IrcManagerImpl;
 import polly.core.UserManagerImpl;
 import de.skuzzle.polly.sdk.AbstractDisposable;
 import de.skuzzle.polly.sdk.eventlistener.NickChangeEvent;
 import de.skuzzle.polly.sdk.eventlistener.NickChangeListener;
 import de.skuzzle.polly.sdk.eventlistener.SpotEvent;
 import de.skuzzle.polly.sdk.eventlistener.UserEvent;
 import de.skuzzle.polly.sdk.eventlistener.UserListener;
 import de.skuzzle.polly.sdk.eventlistener.UserSpottedListener;
 import de.skuzzle.polly.sdk.exceptions.AlreadySignedOnException;
 import de.skuzzle.polly.sdk.exceptions.DisposingException;
 import de.skuzzle.polly.sdk.exceptions.UnknownUserException;
 import de.skuzzle.polly.sdk.model.User;
 
 
 public class AutoLogonLogoffHandler extends AbstractDisposable
         implements UserSpottedListener, NickChangeListener, UserListener {
     
     
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
             
             synchronized (scheduledLogons) {
                 if (scheduledLogons.containsKey(this.forUser)) {
                     try {
                         // removing the entry happens in the event handler
                         // AutoLogonLogoffHandler.this.scheduledLogons.remove(this.forUser);
                         
                         userManager.logonWithoutPassword(this.forUser);
                     } catch (UnknownUserException e) {
                         logger.warn("Error while autologon", e);
                     } catch (AlreadySignedOnException e) {
                         logger.warn("Error while autologon", e);
                     } catch (Exception e) {
                         logger.error("Error while autologon", e);
                     }
                 }
             }
         }
     }
     
 
     private static Logger logger = Logger.getLogger(AutoLogonLogoffHandler.class.getName());
     
  
     private IrcManagerImpl ircManager;
     private UserManagerImpl userManager;
     private ScheduledExecutorService autoLogonExecutor;
     private Map<String, AutoLogonRunnable> scheduledLogons;
     private PollyConfiguration config;
     
     
     public AutoLogonLogoffHandler(IrcManagerImpl ircManager, 
             UserManagerImpl userManager, PollyConfiguration config) {
         this.ircManager = ircManager;
         this.userManager = userManager;
         this.autoLogonExecutor = Executors.newScheduledThreadPool(4, 
                 new EventThreadFactory("LOGON"));
         this.scheduledLogons = new HashMap<String, AutoLogonRunnable>();
         this.config = config;
     }
 
 
 
     @Override
     public void userSpotted(SpotEvent e) {
         if (!this.config.isAutoLogon()) {
             return;
         }
         this.scheduleAutoLogon(e.getUser().getNickName());
     }
 
     
     
     @Override
     public void nickChanged(NickChangeEvent e) {
         if (!this.config.isAutoLogon()) {
             return;
         }
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
             
            User u = this.userManager.getUser(e.getNewUser());
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
                 this.autoLogonExecutor.schedule(runMe, this.config.getAutoLogonTime(), 
                         TimeUnit.MILLISECONDS);
                 logger.debug("Auto logon for " + forUser + " scheduled");
             }
         }
     }
     
     
     
     private boolean userExists(String name) {
         User user = this.userManager.getUser(name);
         return user != null;
     }
     
     
 
     @Override
     public synchronized void userLost(SpotEvent e) {
         if (this.userManager.isSignedOn(e.getUser())) {
             logger.warn("Auto logoff for user: " + e.getUser());
             if (e.getType() != SpotEvent.USER_QUIT) {
                 this.ircManager.sendMessage(e.getUser().getNickName(), 
                     "Du wurdest automatisch ausgeloggt weil du den letzten " +
                    "gemeinsamen Channel verlassen hast.", this);
             }
             this.userManager.logoff(e.getUser(), true);
         }
     }
 
 
 
     @Override
     public void userSignedOn(UserEvent e) {
         if (!this.config.isAutoLogon()) {
             return;
         }
         
         synchronized (this.scheduledLogons) {
             this.scheduledLogons.remove(e.getUser().getName());
         }
     }
 
 
 
     @Override
     public void userSignedOff(UserEvent ignore) {}
 
 
 
     @Override
     protected void actualDispose() throws DisposingException {
         this.autoLogonExecutor.shutdown();
     }
 }
