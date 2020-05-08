 package net.sapium.livestreamprocessor.gui;
 
 import net.sapium.livestreamprocessor.utils.Processor;
 import net.sapium.livestreamprocessor.utils.ProgressChangedListener;
 
 import org.eclipse.swt.widgets.Composite;
 
 /**
  * Tab superclass for organizing the GUI elements
  * 
  * @author Andrew Kallmeyer
  */
 public abstract class TabContent extends Composite {
     
     protected Processor processor;
     
     public TabContent(Composite arg0, int arg1) {
         super(arg0, arg1);
         createContents();
         addListeners();
     }
     
     /**
      * Cancel the running operation
      */
     public void cancel() {
         if(processor != null && processor.isRunning()) {
             processor.cancel();
         }
     }
     
     /**
      * Create GUI elements here
      */
     protected abstract void createContents();
     
     /**
      * Add listeners to the GUI elements here (Called after createContents())
      */
     protected abstract void addListeners();
     
     /**
      * Called when the start button is pressed. Each tab should implement its
      * functionality here.
      * 
      * @param listener ProgressChangedListener to send progress events to
      */
     protected abstract void start(ProgressChangedListener listener);
 }
