 package org.phl0w.aurora.itdropper;
 
 import ms.aurora.api.script.ActionScript;
 import ms.aurora.api.script.ScriptManifest;
 import org.phl0w.aurora.itdropper.actions.Drop;
 
 import java.util.logging.Logger;
 
 @ScriptManifest(name = "iTDropper", version = 1.0d, author = "_phl0w")
 public class iTDropper extends ActionScript {
 
     public static final Logger log = Logger.getLogger("iTDropper");
 
     @Override
     public void onStart() {
         log.info("iTDropper by _phl0w started");
        log.info("testing to see if script updated");
         submit(new Drop());
         getEventBus().register(this);
     }
 }
