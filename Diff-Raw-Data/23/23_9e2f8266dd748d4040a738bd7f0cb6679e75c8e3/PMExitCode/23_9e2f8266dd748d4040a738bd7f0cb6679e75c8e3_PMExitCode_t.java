 package cs242.pcurry2.pm;
 
 /**
  * C-Style enum for all the different return codes the program may leave when
  * encountering a fatal error. This may not be necessary later if I
  * add more robustness to the app.
  */
 /* I'm writing it this way instead of as an Enum so I can interface with it
  * like so: System.exit(PMExitCode.ALGORITHM); which java enums annoyingly
  * won't let me do. (I have to make a getValue method to the enum.)
  */
 public class PMExitCode {
     /**
     * There was an unrecovable error within the JCA's encryption library.
     * You should run JavaEncryptionSettingsTest to ensure that the appropriate
     * functionality is supported on this platform.
      */
    public static final int JCA_ALGORITHM_ERROR = 1;
 
     /**
      * Private constructor since this only has public static fields and should
      * never be instantiated.
      */
     private PMExitCode() {}
 }
