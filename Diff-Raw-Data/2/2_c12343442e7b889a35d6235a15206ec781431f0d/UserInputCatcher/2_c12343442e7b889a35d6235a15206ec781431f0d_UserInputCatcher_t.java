 package info.opencards.oimputils;
 
 import com.sun.star.animations.XAnimationNode;
 import com.sun.star.lang.EventObject;
 import com.sun.star.presentation.XSlideShowListener;
 
 
 /**
  * DOCUMENT ME!
  *
  * @author Holger Brandl
  */
 public class UserInputCatcher implements XSlideShowListener {
 
     public void paused() {
     }
 
 
     public void resumed() {
     }
 
 
     public void slideTransitionStarted() {
         System.err.println("");
     }
 
 
     public void slideTransitionEnded() {
     }
 
 
     public void slideAnimationsEnded() {
         System.err.println("");
 
     }
 
 
    public void slideEnded(boolean b) {
     }
 
 
     public void hyperLinkClicked(String s) {
     }
 
 
     public void beginEvent(XAnimationNode xAnimationNode) {
         System.err.println("");
 
     }
 
 
     public void endEvent(XAnimationNode xAnimationNode) {
         System.err.println("");
 
     }
 
 
     public void repeat(XAnimationNode xAnimationNode, int i) {
     }
 
 
     public void disposing(EventObject eventObject) {
     }
 }
