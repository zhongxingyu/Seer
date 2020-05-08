 package no.feide.moria;
 
 import java.util.TimerTask;
 import java.util.logging.Logger;    
 
 
 /**
  * Represents a periodic task to manipulate the session store.
  */
 public class SessionStoreTask
 extends TimerTask {
 
     /** Session time out value. */
     private int timeoutSec;
 
     /** SSO Session time out value. */
     private int ssoTimeoutMin;
 
     /** Authenticated session time out value. */
     private int authTimeoutSec;
 
     /** Used for logging. */
     private static Logger log = Logger.getLogger(SessionStoreTask.class.toString());
     
     /** Local pointer to session store. */
     private SessionStore sessionStore;
     
     
     /**
      * Constructor. Sets the local pointer to the session store.
      * @throws SessionException If there's a problem getting the session store
     *                          pointer, or if any of the required
     *                          configuration settings are undefined.
      */
     public SessionStoreTask()
     throws SessionException {
        log.info("SessionStoreTask()");
        
         sessionStore = SessionStore.getInstance();
       
        // Sets some properties, with sanity checks.
        String s = System.getProperty("no.feide.moria.SessionTimeout");
        if (s == null) {
            log.severe("no.feide.moria.SessionTimeout required, but not set");
            throw new SessionException("no.feide.moria.SessionTimeout required, but not set");
        }
        timeoutSec = new Integer(s).intValue(); 
        s = System.getProperty("no.feide.moria.SessionSSOTimeout");
        if (s == null) {
            log.severe("no.feide.moria.SessionSSOTimeout required, but not set");
            throw new SessionException("no.feide.moria.SessionSSOTimeout required, but not set");
        }
        ssoTimeoutMin = new Integer(s).intValue(); 
        s = System.getProperty("no.feide.moria.AuthenticatedSessionTimeout");
        if (s == null) {
            log.severe("no.feide.moria.AuthenticatedSessionTimeout required, but not set");
            throw new SessionException("no.feide.moria.AuthenticatedSessionTimeout required, but not set");
        }
        authTimeoutSec = new Integer(s).intValue();
         
        // Configuration logging.
         log.config("Session time out set to "+timeoutSec+" seconds.");
         log.config("Authenticated session time out set to "+authTimeoutSec+" seconds.");
         log.config("Session SSO time out set to "+ssoTimeoutMin+" minutes.");
     }
      
     
     /**
      * Called periodically by the timer.
      **/
     public void run() {
         log.fine("run()");
         sessionStore.checkTimeout(timeoutSec*1000, authTimeoutSec*1000, ssoTimeoutMin*60*1000);
     }    
     
 }
