 package org.freenetproject.plugin.infocalypse_webui.ui.fcp;
 
 import freenet.pluginmanager.FredPluginFCP;
 import freenet.pluginmanager.PluginNotFoundException;
 import freenet.pluginmanager.PluginReplySender;
 import freenet.support.SimpleFieldSet;
 import freenet.support.api.Bucket;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 /**
  * Tracks session state and dispatches messages to specific handlers.
  */
 public class FCPHandler implements FredPluginFCP {
 
     /*
      * TODO: Where to keep state, like which sessions are still considered connected? Hm, that should be any message.
      * Still, where to keep state when it is needed? Per message type doesn't seem to make sense. It depends on the
      * type of state and where it's needed, so perhaps that remains to be seen when actual functionality comes into
      * the fold.
      *
      * Is connectedness only a UI thing? It would make sense if any message causes reconnection,
      * and there isn't yet an apparent reason why a session would carry its own state.
      *
      * So Infocalypse sends Ping, which merits no response, and ClearToSend, which replies with whatever user request
      * from the UI?
      *
      * TODO: Does it make sense to only allow one session at once? Is there a less ugly way to implement a timeout?
      */
 
     /**
     * Milliseonds before an FCP connection to Infocalypse is considered timed out.
      */
     public static final long fcpTimeout = 5000;
 
     private String connectedIdentifier;
     private final Timer timeout;
 
     public FCPHandler() {
         // Timer is a daemon - if shutting down there's no need to continue tracking a timeout.
         timeout = new Timer(true);
     }
 
     @Override
     public void handle(PluginReplySender replysender, SimpleFieldSet params, Bucket data, int accesstype) {
         // TODO: What to do with accesstype?
         synchronized (timeout) {
             if (connectedIdentifier == null) {
                 connectedIdentifier = replysender.getIdentifier();
             } else if (!connectedIdentifier.equals(replysender.getIdentifier())) {
                 // A different identifier is already connected.
                 SimpleFieldSet sfs = new SimpleFieldSet(true);
                 sfs.putOverwrite("Message", "Error");
                 sfs.putOverwrite("Description", "An Infocalypse session is already connected: " + replysender
                         .getIdentifier());
                 try {
                     replysender.send(sfs);
                 } catch (PluginNotFoundException e) {
                     // TODO: Lazy error handling. Look into real logging.
                     System.err.println("Cannot find plugin / connection closed: " + e);
                 }
                 return;
             } else {
                 // This identifier was already connected.
                 assert connectedIdentifier.equals(replysender.getIdentifier());
                 timeout.cancel();
             }
 
             timeout.schedule(new TimerTask() {
                 @Override
                 public void run() {
                     synchronized (timeout) {
                         System.err.println("Session '" + connectedIdentifier +"' timed out.");
                         connectedIdentifier = null;
                     }
                 }
             }, fcpTimeout);
         }
 
         SimpleFieldSet sfs = new SimpleFieldSet(true);
         sfs.putOverwrite("Message", "Pong");
         try {
             replysender.send(sfs);
         } catch (PluginNotFoundException e) {
             // TODO: Copy and paste from above. Wrapper, rearrange control flow, or just put up with it?
             System.err.println("Cannot find plugin / connection closed: " + e);
         }
     }
 
     /**
      * @return true if a session is connected and has not timed out; false otherwise.
      */
     public boolean isConnected() {
         synchronized (timeout) {
             return connectedIdentifier != null;
         }
     }
 }
