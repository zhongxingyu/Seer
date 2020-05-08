 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.gatech.statics.application;
 
 import com.jme.input.MouseInput;
 import com.jme.input.action.InputActionEvent;
 import com.jme.input.action.MouseInputAction;
 import edu.gatech.statics.ui.InterfaceRoot;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class MouseDragAndZoom extends MouseInputAction {
 
     public MouseDragAndZoom(StaticsApplication app) {
         mouse = app.getMouse();
         mouseWasDown = MouseInput.get().isButtonDown(0);
     }
     private boolean mouseWasDown;
     private boolean enabled = true;
     
     void setEnabled(boolean enabled) {
         this.enabled = enabled;
     }
     private static final float mouseSpeed = .1f;
     private static final float zoomSpeed = .03f;
    
    private int lastX, lastY;
 
     public void performAction(InputActionEvent evt) {
         if (!enabled || 
                 StaticsApplication.getApp().getCurrentTool() != null) {
             return;
         }
         
         // special case for continuing to drag while under the interface.
         if(!mouseWasDown && InterfaceRoot.getInstance().hasMouse()) {
             return;
         }
        
        int currentX = MouseInput.get().getXAbsolute();
        int currentY = MouseInput.get().getYAbsolute();
 
         boolean mouseIsDown = MouseInput.get().isButtonDown(0);
 
         if (mouseWasDown && mouseIsDown) {
            float deltaX = -mouseSpeed * (currentX - lastX);
            float deltaY = -mouseSpeed * (currentY - lastY);
 
             InterfaceRoot.getInstance().getCameraControl().panCamera(deltaX, deltaY);
         }
         float deltaZoom = zoomSpeed * MouseInput.get().getWheelDelta();
         if(deltaZoom != 0)
             InterfaceRoot.getInstance().getCameraControl().zoomCamera(-deltaZoom);
         mouseWasDown = mouseIsDown;
        
        lastX = currentX;
        lastY = currentY;
     }
 }
