 /**
  * 
  */
 package ussr.samples.atron;
 
 
 /**
  * @author ups
  *
  * TODO Write a nice and user-friendly comment here
  * 
  */
 public class ATRONNativeController extends ATRONController {
 
     public ATRONNativeController(String nativeLibraryName) {
         try {
             System.loadLibrary(nativeLibraryName);
         } catch(UnsatisfiedLinkError error) {
             System.err.println("Unable to link native controller "+nativeLibraryName);
             System.err.println(" java.library.path="+System.getProperty("java.library.path"));
             System.err.println(" exception="+error);
             throw new Error("Native controller not found");
         }
     }
     
     @Override
     public native void activate();
 
     void iterationSimulatorHook() {
         do { Thread.yield(); } while(module.getSimulation().isPaused());
     }
 
 }
