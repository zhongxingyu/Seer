 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.gatech.statics.modes.fbd.ui;
 
 import com.jmex.bui.BButton;
 import com.jmex.bui.event.ActionEvent;
 import com.jmex.bui.event.ActionListener;
import edu.gatech.statics.application.StaticsApplication;
 import edu.gatech.statics.modes.fbd.FreeBodyDiagram;
 import edu.gatech.statics.modes.fbd.tools.CreateForceTool2D;
 import edu.gatech.statics.modes.fbd.tools.CreateMomentTool2D;
import edu.gatech.statics.objects.manipulators.Tool;
 import edu.gatech.statics.ui.ButtonUtil;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class FBDTools2D extends FBDTools {
 
     private BButton addForce;
     private BButton addMoment;
 
    private Tool currentTool;

     public FBDTools2D(FBDModePanel modePanel) {
         super(modePanel);
         ToolListener listener = new ToolListener();
 
         addForce = new BButton("", listener, "force");
         addMoment = new BButton("", listener, "moment");
 
         ButtonUtil.setImageBackground(addForce, "rsrc/interfaceTextures/add_force");
         ButtonUtil.setImageBackground(addMoment, "rsrc/interfaceTextures/add_moment");
 
         add(addForce);
         add(addMoment);
     }
 
     private class ToolListener implements ActionListener {
 
         public void actionPerformed(ActionEvent event) {
 
             FreeBodyDiagram diagram = (FreeBodyDiagram) getModePanel().getDiagram();
             // we do not load any tools if the diagram is solved.
             if (diagram.isSolved()) {
                 return;
             }

            boolean cancelledPrevious = false;
            if(currentTool != null && currentTool.isActive()) {
                currentTool.cancel();
                cancelledPrevious = true;
            }
             
             if (event.getAction().equals("force")) {
                 CreateForceTool2D createForce = new CreateForceTool2D(diagram);
                currentTool = createForce;
                 createForce.activate();
             } else if (event.getAction().equals("moment")) {
                 CreateMomentTool2D createMoment = new CreateMomentTool2D(diagram);
                currentTool = createMoment;
                 createMoment.activate();
             }

            if(cancelledPrevious) {
                StaticsApplication.getApp().setUIFeedback("To cancel adding a force or moment, press ESC");
            }
         }
     }
 }
