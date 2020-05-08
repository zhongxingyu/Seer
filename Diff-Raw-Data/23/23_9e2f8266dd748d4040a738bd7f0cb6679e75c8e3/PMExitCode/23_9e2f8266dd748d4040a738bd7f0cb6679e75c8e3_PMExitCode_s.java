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
     * Couldn't use a necessary encryption algorithm.
      */
    public static final int ALGORITHM = 1;

    /**
     * Tried to use an invalid cryptographic key.
     * Currently unused.
     */
    public static final int KEY = 2;
 
     /**
      * Private constructor since this only has public static fields and should
      * never be instantiated.
      */
     private PMExitCode() {}
 }
