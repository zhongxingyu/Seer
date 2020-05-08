 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.gatech.statics.modes.distributed.ui;
 
 import com.jmex.bui.BButton;
 import com.jmex.bui.BContainer;
 import com.jmex.bui.BLabel;
 import com.jmex.bui.BTextField;
 import com.jmex.bui.event.ActionEvent;
 import com.jmex.bui.event.ActionListener;
 import com.jmex.bui.layout.BorderLayout;
 import com.jmex.bui.layout.GroupLayout;
 import edu.gatech.statics.application.StaticsApplication;
 import edu.gatech.statics.exercise.DiagramType;
 import edu.gatech.statics.modes.distributed.DistributedDiagram;
 import edu.gatech.statics.modes.distributed.DistributedMode;
 import edu.gatech.statics.modes.distributed.DistributedState;
 import edu.gatech.statics.ui.applicationbar.ApplicationModePanel;
 import edu.gatech.statics.ui.applicationbar.ApplicationTab;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class DistributedModePanel extends ApplicationModePanel {
 
     private BTextField positionField;
     private BTextField magnitudeField;
     private BButton checkButton;
     //private String positionValue;
     //private String magnitudeValue;
 
     public DistributedModePanel() {
 
         getTitleLabel().setText("Find the Resultant");
 
         BContainer mainContainer = new BContainer(new BorderLayout());
         add(mainContainer, BorderLayout.CENTER);
 
         ActionListener listener = new ActionListener() {
 
             public void actionPerformed(ActionEvent event) {
                 performCheck();
             }
         };
 
         checkButton = new BButton("Check", listener, "check");
         checkButton.setStyleClass("circle_button");
         mainContainer.add(checkButton, BorderLayout.EAST);
 
         BContainer equationContainer = new BContainer(GroupLayout.makeVert(GroupLayout.CENTER));
         mainContainer.add(equationContainer, BorderLayout.CENTER);
 
         BContainer positionContainer = new BContainer(new BorderLayout());
         BContainer magnitudeContainer = new BContainer(new BorderLayout());
 
         equationContainer.add(positionContainer);
         equationContainer.add(magnitudeContainer);
 
         positionContainer.add(new BLabel("Position: "), BorderLayout.WEST);
         magnitudeContainer.add(new BLabel("Magnitude: "), BorderLayout.WEST);
 
         positionField = new BTextField() {
 
             @Override
             protected void lostFocus() {
                 super.lostFocus();
                 DistributedDiagram diagram = (DistributedDiagram) getDiagram();
                 diagram.setPosition(getText());
             }
         };
         positionContainer.add(positionField, BorderLayout.CENTER);
         positionField.setPreferredWidth(200);
         positionField.setStyleClass("textfield_appbar");
 
         magnitudeField = new BTextField() {
 
             @Override
             protected void lostFocus() {
                 super.lostFocus();
                 DistributedDiagram diagram = (DistributedDiagram) getDiagram();
                 diagram.setMagnitude(getText());
             }
         };
         magnitudeContainer.add(magnitudeField, BorderLayout.CENTER);
         magnitudeField.setPreferredWidth(200);
         magnitudeField.setStyleClass("textfield_appbar");
 
 
     }
 
     protected void performCheck() {
         DistributedDiagram diagram = (DistributedDiagram) getDiagram();
         // check to see if the distributed check succeeds
         if (diagram.check(positionField.getText(), magnitudeField.getText())) {
             // distributed check is successful!
             StaticsApplication.getApp().setAdviceKey("distributed_feedback_check_success");
             diagram.setSolved();
         } else {
             // should we give any more detailed feedback?
             StaticsApplication.getApp().setAdviceKey("distributed_feedback_check_fail");
         }
     }
 
     @Override
     public void stateChanged() {
         super.stateChanged();
 
         // lock the input fields if the diagram is locked
         if (getDiagram().isLocked()) {
             magnitudeField.setEnabled(false);
             positionField.setEnabled(false);
         } else {
             magnitudeField.setEnabled(true);
             positionField.setEnabled(true);
         }
     }
 
     @Override
     public void activate() {
         DistributedState state = (DistributedState) getDiagram().getCurrentState();
 
         magnitudeField.setText(state.getMagnitude());
         positionField.setText(state.getPosition());
     }
 
     @Override
     protected ApplicationTab createTab() {
         return new ApplicationTab("Resultant");
     }
 
     @Override
     public DiagramType getDiagramType() {
         return DistributedMode.instance.getDiagramType();
     }
 }
