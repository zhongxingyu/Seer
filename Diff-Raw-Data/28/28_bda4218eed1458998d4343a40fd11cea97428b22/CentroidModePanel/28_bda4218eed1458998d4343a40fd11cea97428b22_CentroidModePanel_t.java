 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.gatech.statics.modes.centroid.ui;
 
 import com.jmex.bui.BButton;
 import com.jmex.bui.BContainer;
 import com.jmex.bui.BLabel;
 import com.jmex.bui.BTextField;
 import com.jmex.bui.event.ActionEvent;
 import com.jmex.bui.event.ActionListener;
 import com.jmex.bui.layout.BorderLayout;
 import com.jmex.bui.layout.TableLayout;
 import edu.gatech.statics.application.StaticsApplication;
 import edu.gatech.statics.exercise.DiagramType;
 import edu.gatech.statics.modes.centroid.CentroidBody;
 import edu.gatech.statics.modes.centroid.CentroidDiagram;
 import edu.gatech.statics.modes.centroid.CentroidMode;
 import edu.gatech.statics.modes.centroid.CentroidPartState;
 import edu.gatech.statics.modes.centroid.CentroidState;
 import edu.gatech.statics.modes.centroid.CentroidState.Builder;
 import edu.gatech.statics.modes.centroid.objects.CentroidPart;
 import edu.gatech.statics.modes.centroid.objects.CentroidPartObject;
 import edu.gatech.statics.objects.SimulationObject;
 import edu.gatech.statics.ui.InterfaceRoot;
 import edu.gatech.statics.ui.applicationbar.ApplicationModePanel;
 import edu.gatech.statics.ui.components.NextButton;
 import java.util.List;
 import java.util.Map;
 
 /**
  *
  * @author Jimmy Truesdell
  */
 public class CentroidModePanel extends ApplicationModePanel {
 
     private BContainer mainContainer;
     private BContainer equationContainer;
     private BTextField areaField;
     private BTextField xField;
     private BTextField yField;
     private BLabel areaLabel;
     private BLabel xLabel;
     private BLabel yLabel;
     private BButton checkButton;
     private CentroidPartObject currentlySelected;
 
     public CentroidModePanel() {
         mainContainer = new BContainer(new BorderLayout(5, 0));
         equationContainer = new BContainer(new TableLayout(2, 5, 5));
         add(mainContainer, BorderLayout.CENTER);
         mainContainer.add(equationContainer, BorderLayout.CENTER);
         generateUI();
         //SimulationObject o = (SimulationObject) getDiagram().allObjects().get(0);
     }
 
     protected void performCheck() {
         CentroidDiagram diagram = (CentroidDiagram) getDiagram();
         // check to see if the distributed check succeeds
         if (diagram.check(areaField.getText(), xField.getText(), yField.getText())) {
             // distributed check is successful!
             StaticsApplication.getApp().setStaticsFeedbackKey("centroid_feedback_check_success");
             checkButton.setEnabled(false);
             areaField.setEnabled(false);
             xField.setEnabled(false);
             yField.setEnabled(false);
            //((CentroidState)getDiagram().getCurrentState()).getMyPartState(currentlySelected.getCentroidPart()).getBuilder().setSolved(true);
            CentroidState state = (CentroidState) getDiagram().getCurrentState();
            Builder builder = state.getBuilder();
            Map<CentroidPart, CentroidPartState> partsMap = builder.getMyParts();
            CentroidPartState.Builder newPart = partsMap.get(currentlySelected.getCentroidPart()).getBuilder();

            newPart.setSolved(true);
            partsMap.remove(currentlySelected.getCentroidPart());
            partsMap.put(currentlySelected.getCentroidPart(), newPart.build());
            partsMap.get(currentlySelected.getCentroidPart()).isLocked();
            getDiagram().pushState(builder.build());

            //TODO Do we need to do this?
            //diagram.setSolved();
         } else {
             // should we give any more detailed feedback?
             StaticsApplication.getApp().setStaticsFeedbackKey("centroid_feedback_check_fail");
         }
     }
 
     @Override
     public void stateChanged() {
         super.stateChanged();
 
 //         lock the input fields if the diagram is locked
        Map<CentroidPart, CentroidPartState> partsMap = ((CentroidState)getDiagram().getCurrentState()).getBuilder().getMyParts();
        if (partsMap.containsKey(currentlySelected.getCentroidPart()) && partsMap.get(currentlySelected.getCentroidPart()).isLocked() == true) {
             areaField.setEnabled(false);
             xField.setEnabled(false);
             yField.setEnabled(false);
             checkButton.setEnabled(false);
         } else {
             areaField.setEnabled(true);
             xField.setEnabled(true);
             yField.setEnabled(true);
             checkButton.setEnabled(true);
         }
     }
 
     @Override
     public void activate() {
         if (getDiagram() != null && currentlySelected != null) {
             CentroidState state = (CentroidState) getDiagram().getCurrentState();
             if (state.getBuilder().getMyParts().containsKey(currentlySelected.getCentroidPart())) {//state.getMyPartState(currentlySelected.getCentroidPart()) != null) {
                 areaField.setText(state.getMyPartState(currentlySelected.getCentroidPart()).getArea());
                 xField.setText(state.getMyPartState(currentlySelected.getCentroidPart()).getXPosition());
                 yField.setText(state.getMyPartState(currentlySelected.getCentroidPart()).getYPosition());
             } else {
                 CentroidPartState.Builder partBuilder = new CentroidPartState.Builder();
                 partBuilder.setArea("");
                 partBuilder.setXPosition("");
                 partBuilder.setYPosition("");
                 partBuilder.setSolved(false);
                 partBuilder.setMyPart(currentlySelected.getCentroidPart());
 
                 Builder builder = state.getBuilder();
                 Map<CentroidPart, CentroidPartState> partsMap = builder.getMyParts();
                 partsMap.put(partBuilder.getMyPart(), partBuilder.build());
 
                 getDiagram().pushState(builder.build());
                 //((CentroidState)getDiagram().getCurrentState()).getEquationStates().containsKey(currentlySelected.getCentroidPart());
                 areaField.setText("");
                 xField.setText("");
                 yField.setText("");
             }
             stateChanged();
         }
     }
 
     @Override
     public DiagramType getDiagramType() {
         return CentroidMode.instance.getDiagramType();
     }
 
     public void updateSelection(CentroidPartObject currentlySelected) {
         this.currentlySelected = currentlySelected;
         if (currentlySelected != null) {
             if (!checkButton.isAdded()) {
                 mainContainer.add(checkButton, BorderLayout.EAST);
             }
             if (!areaField.isAdded()) {
                 equationContainer.add(areaLabel, BorderLayout.WEST);
                 equationContainer.add(areaField, BorderLayout.CENTER);
             }
             if (!xField.isAdded()) {
                 equationContainer.add(xLabel, BorderLayout.WEST);
                 equationContainer.add(xField, BorderLayout.CENTER);
             }
             if (!yField.isAdded()) {
                 equationContainer.add(yLabel, BorderLayout.WEST);
                 equationContainer.add(yField, BorderLayout.CENTER);
             }
             areaField.setText("");
             xField.setText("");
             yField.setText("");
 
             activate();
         } else {
             mainContainer.remove(checkButton);
             areaField.setText("");
             xField.setText("");
             yField.setText("");
             equationContainer.removeAll();
         }
         InterfaceRoot.getInstance().getApplicationBar().updateSize();
     }
 
     private void generateUI() {
         ActionListener listener = new ActionListener() {
 
             public void actionPerformed(ActionEvent event) {
                 performCheck();
             }
         };
 
         checkButton = new NextButton("Get Centroid", listener, "check");
 
 
         areaField = new BTextField() {
 
             @Override
             protected void lostFocus() {
                 super.lostFocus();
                 CentroidDiagram diagram = (CentroidDiagram) getDiagram();
                 diagram.setArea(getText());
             }
         };
 
         areaField.setPreferredWidth(200);
         areaField.setStyleClass("textfield_appbar");
 
         areaLabel = new BLabel("Surface Area: ");
 
 
 
         xField = new BTextField() {
 
             @Override
             protected void lostFocus() {
                 super.lostFocus();
                 CentroidDiagram diagram = (CentroidDiagram) getDiagram();
                 diagram.setXPosition(getText());
             }
         };
 
         xField.setPreferredWidth(200);
         xField.setStyleClass("textfield_appbar");
 
         xLabel = new BLabel("X Position: ");
 
 

         yField = new BTextField() {
 
             @Override
             protected void lostFocus() {
                 super.lostFocus();
                 CentroidDiagram diagram = (CentroidDiagram) getDiagram();
                 diagram.setYPosition(getText());
             }
         };
 
         yField.setPreferredWidth(200);
         yField.setStyleClass("textfield_appbar");
 
         yLabel = new BLabel("Y Position: ");
     }
 }
