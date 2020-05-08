 package edu.wustl.cab2b.client.ui.util;
 
 import java.awt.Component;
 
 import javax.swing.SwingUtilities;
 
 /**
  * This is a variant of the SwingWorker
  * It works in conjunction with the GlassPane class
  * to allow users to execute timeconsuming task on a separate thread
  * The GlassPane addition can prevent users from executing another SwingWorker task
  * while one SwingWorker task is already executing
  *
  */
 public abstract class CustomSwingWorker {
 
     private ThreadVar threadVar;
 
     private GlassPane glassPane;
 
     private Component aComponent;
 
     private Component parentCoponent;
 
     private boolean errorOccured;
 
     /** 
      * Class to maintain reference to current worker thread
      * under separate synchronization control.
      */
     private static class ThreadVar {
 
         private Thread thread;
 
         ThreadVar(Thread t) {
             thread = t;
         }
 
         synchronized Thread get() {
             return thread;
         }
 
         synchronized void clear() {
             thread = null;
         }
     }
 
     public CustomSwingWorker(Component aComponent) {
         this(aComponent, aComponent);
     }
 
     /**
      * Start a thread that will call the <code>construct</code> method
      * and then exit.
      * @param aComponent a reference to the UI component that's directly using
      * SwingWorker
      */
     public CustomSwingWorker(Component aComponent, Component parentCoponent) {
         this.parentCoponent = parentCoponent;
         setAComponent(aComponent);
 
         final Runnable doFinished = new Runnable() {
 
             public void run() {
                 finished();
             }
         };
 
         Runnable doConstruct = new Runnable() {
 
             public void run() {
                 try {
                     construct();
                 } finally {
                     threadVar.clear();
                 }
 
                 // Execute the doFinished runnable on the Swing dispatcher thread
                 SwingUtilities.invokeLater(doFinished);
             }
         };
 
         // Group the new worker thread in the same group as the "spawner" thread
         Thread t = new Thread(Thread.currentThread().getThreadGroup(), doConstruct);
         threadVar = new ThreadVar(t);
     }
 
     /**
      * Start a thread that will call the <code>construct</code> method
      * and then exit.
      * @param aComponent a reference to the UI component that's directly using
      * SwingWorker
      */
     public CustomSwingWorker(Component aComponent, Object object) {
         setAComponent(aComponent);
 
         final Runnable doFinished = new Runnable() {
 
             public void run() {
                 finished();
             }
         };
 
         Runnable doConstruct = new Runnable() {
 
             public void run() {
                 try {
                     construct();
                 } finally {
                     threadVar.clear();
                 }
 
                 // Execute the doFinished runnable on the Swing dispatcher thread
                 SwingUtilities.invokeLater(doFinished);
             }
         };
 
         // Group the new worker thread in the same group as the "spawner" thread
         Thread t = new Thread(Thread.currentThread().getThreadGroup(), doConstruct);
         threadVar = new ThreadVar(t);
     }
 
     /**
      * Activate the capabilities of glasspane
      * 
      */
     private void activateGlassPane() {
         // Mount the glasspane on the component window
         GlassPane aPane = GlassPane.mount(getAComponent(), true);
 
         // keep track of the glasspane as an instance variable
         setGlassPane(aPane);
 
         if (getGlassPane() != null) {
             // Start interception UI interactions
             getGlassPane().setVisible(true);
         }
     }
 
     /** 
      * Enable the glass pane (to disable unwanted UI manipulation), then spawn the non-UI logic on a separate thread.
      */
     private void construct() {
         activateGlassPane();
         try {
             doNonUILogic();
         } catch (Exception e) {
             errorOccured = true;
            CommonUtils.handleException(CommonUtils.getCab2bException(e), this.parentCoponent, true, false, false,
                                         false);
         }
     }
 
     /**
      * Deactivate the glasspane
      * 
      */
     private void deactivateGlassPane() {
         if (getGlassPane() != null) {
             // Stop UI interception
             getGlassPane().setVisible(false);
         }
     }
 
     /**
      * This method will be implemented by the inner class of SwingWorker
      * It should only consist of the logic that's unrelated to UI
      *
      * @throws java.lang.RuntimeException thrown if there are any errors in the non-ui logic
      */
     protected abstract void doNonUILogic() throws Exception;
 
     /**
      * This method will be implemented by the inner class of SwingWorker
      * It should only consist of the logic that's related to UI updating, after
      * the doNonUILogic() method is done. 
      *
      * @throws java.lang.RuntimeException thrown if there are any problems executing the ui update logic
      */
     protected abstract void doUIUpdateLogic() throws Exception;
 
     /**
      * Called on the event dispatching thread (not on the worker thread)
      * after the <code>construct</code> method has returned.
      */
     protected void finished() {
         try {
             deactivateGlassPane();
             if (!errorOccured) {
                 doUIUpdateLogic();
             }
         } catch (Exception e) {
             CommonUtils.handleException(CommonUtils.getCab2bException(e), this.parentCoponent, true, false, false,
                                         false);
         } finally {
             // Allow original component to get the focus
             if (getAComponent() != null) {
                 getAComponent().requestFocus();
             }
         }
     }
 
     /**
      * Getter method
      * 
      * @return java.awt.Component
      */
     protected Component getAComponent() {
         return aComponent;
     }
 
     /**
      * Getter method
      * 
      * @return GlassPane
      */
     protected GlassPane getGlassPane() {
         return glassPane;
     }
 
     /**
      * A new method that interrupts the worker thread.  Call this method
      * to force the worker to stop what it's doing.
      */
     public void interrupt() {
         Thread t = threadVar.get();
         if (t != null) {
             t.interrupt();
         }
         threadVar.clear();
     }
 
     /**
      * Setter method
      * 
      * @param newAComponent java.awt.Component
      */
     protected void setAComponent(Component newAComponent) {
         aComponent = newAComponent;
     }
 
     /**
      * Setter method
      * 
      * @param newGlassPane GlassPane
      */
     protected void setGlassPane(GlassPane newGlassPane) {
         glassPane = newGlassPane;
     }
 
     /**
      * Start the worker thread.
      */
     public void start() {
         Thread t = threadVar.get();
         if (t != null) {
             t.start();
         }
     }
 
     /**
      * @return the hasErrorOccured
      */
     public boolean getHasErrorOccured() {
         return errorOccured;
     }
 
     /**
      * @param hasErrorOccured the hasErrorOccured to set
      */
     public void setHasErrorOccured(boolean hasErrorOccured) {
         this.errorOccured = hasErrorOccured;
     }
 }
