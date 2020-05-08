 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.gatech.statics.modes.centroid;
 
 import edu.gatech.statics.Mode;
 import edu.gatech.statics.Representation;
 import edu.gatech.statics.application.StaticsApplication;
 import edu.gatech.statics.exercise.Diagram;
 import edu.gatech.statics.math.expressionparser.Parser;
 import edu.gatech.statics.modes.centroid.objects.CentroidPart;
 import edu.gatech.statics.modes.centroid.objects.CentroidPartObject;
 import edu.gatech.statics.modes.centroid.CentroidState.Builder;
 import edu.gatech.statics.modes.centroid.actions.SetAreaValue;
 import edu.gatech.statics.modes.centroid.actions.SetXPositionValue;
 import edu.gatech.statics.modes.centroid.actions.SetYPositionValue;
 import edu.gatech.statics.modes.centroid.ui.CentroidModePanel;
 import edu.gatech.statics.objects.SimulationObject;
 import edu.gatech.statics.objects.representations.MimicRepresentation;
 import edu.gatech.statics.ui.InterfaceRoot;
 import edu.gatech.statics.util.SelectionFilter;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author Jimmy Truesdell
  */
 public class CentroidDiagram extends Diagram<CentroidState> {
 
     private static final float TOLERANCE = .01f;
 //    private CentroidPartObject cpObj;
     private CentroidBody body;
     private CentroidPartObject currentlySelected;
 
     public CentroidDiagram(CentroidBody body) {
         super(body);
         this.body = body;
 
         // pick out the actual centroid part object from the schematic
 //        for (SimulationObject obj : Exercise.getExercise().getSchematic().allObjects()) {
 //            if (obj instanceof CentroidPartObject) {
 //                CentroidPartObject cpObjTest = (CentroidPartObject) obj;
 //                if (cpObjTest.getCentroidPart().equals(body)) {
 //                    cpObj = cpObjTest;
 //                }
 //            }
 //        }
     }
     private SelectionFilter selector = new SelectionFilter() {
 
         public boolean canSelect(SimulationObject obj) {
             return obj instanceof CentroidPartObject;
         }
     };
 
     @Override
     public SelectionFilter getSelectionFilter() {
         //can select CentroidPartObjects. not the body
         return selector;
     }
 
     @Override
     public void onClick(SimulationObject obj) {
         //somethind like that. also needs to be selectable = true
         //CentroidModePanel modePanel = (CentroidModePanel) InterfaceRoot.getInstance().getModePanel(CentroiMode.instance.getModeName());
         //modePanel.onSelect((CentroidPartObject) obj);
         //System.out.println("Oi!!! Clicked on: "+obj);
 
         // no effect if we click on the same thing twice
         if (currentlySelected == obj) {
             return;
         }
 
         // if we are here, we know that currentlySelected != obj
         // de-highlight the last selection
         if (currentlySelected != null) {
             currentlySelected.setDisplaySelected(false);
             this.activate();
         }
 
         // switch the current selection
         currentlySelected = (CentroidPartObject) obj;
 
         if (currentlySelected != null) {
             currentlySelected.setDisplaySelected(true);
         }
 
         // update the UI
         CentroidModePanel modePanel = (CentroidModePanel) InterfaceRoot.getInstance().getApplicationBar().getModePanel();
         modePanel.updateSelection(currentlySelected);
     }
 
     public boolean check(String areaValue, String xValue, String yValue) {
         BigDecimal userArea = Parser.evaluate(areaValue);
         BigDecimal userXPosition = Parser.evaluate(xValue);
         BigDecimal userYPosition = Parser.evaluate(yValue);
 
         if (userArea == null || userXPosition == null || userYPosition == null) {
             return false;
         }
 
         CentroidPart part = getCentroidPart();
         BigDecimal desiredArea = new BigDecimal(part.getSurfaceArea());
         BigDecimal desiredX = new BigDecimal(part.getxPosition());
         BigDecimal desiredY = new BigDecimal(part.getyPosition());
 
         // debugging messages
         System.out.println("user area: \"" + areaValue + "\" " + userArea);
         System.out.println("user xpos: \"" + xValue + "\" " + userXPosition);
         System.out.println("user ypos: \"" + yValue + "\" " + userYPosition);
         System.out.println("area: " + desiredArea);
         System.out.println("xpos: " + desiredX);
         System.out.println("ypos: " + desiredY);
 
         boolean success = true;
 
         float areaTolerance = TOLERANCE * Math.max(1, Math.abs(desiredArea.floatValue()));
         float xPositionTolerance = TOLERANCE * Math.max(1, Math.abs(desiredX.floatValue()));
         float yPositionTolerance = TOLERANCE * Math.max(1, Math.abs(desiredY.floatValue()));
 
         success &= Math.abs(desiredArea.subtract(userArea).floatValue()) < areaTolerance;
         success &= Math.abs(desiredX.subtract(userXPosition).floatValue()) < xPositionTolerance;
         success &= Math.abs(desiredY.subtract(userYPosition).floatValue()) < yPositionTolerance;
 
         System.out.println("success: " + success);
 
         return success;
     }
 
     public void setArea(String text) {
         SetAreaValue action = new SetAreaValue(text, currentlySelected);
         performAction(action);
     }
 
     public void setXPosition(String text) {
         SetXPositionValue action = new SetXPositionValue(text, currentlySelected);
         performAction(action);
     }
 
     public void setYPosition(String text) {
         SetYPositionValue action = new SetYPositionValue(text, currentlySelected);
         performAction(action);
     }
 
     @Override
     public void activate() {
         super.activate();
        //TODO: ADD THE SPECIFIC CODE THAT RECONSTRUCTS THE DIAGRAM
         updateCentroid();
 
         if (isSolved()) {
             StaticsApplication.getApp().setDefaultUIFeedback("You have solved for the values for the centroid.");
         } else {
             StaticsApplication.getApp().setDefaultUIFeedback("Select a part of the body to determine its centroid.");
         }
         StaticsApplication.getApp().resetUIFeedback();
 
         // activate the mimic representations
         for (SimulationObject obj : getBaseObjects()) {
             if (obj instanceof CentroidPartObject) {
                 setCentroidPartActive((CentroidPartObject) obj, true);
             }
         }
     }
 
     @Override
     public void deactivate() {
         super.deactivate();
         //TODO: ADD THE SPECIFIC CODE THAT DECONSTRUCTS THE DIAGRAM
 
         // deactivate the mimic representations
         for (SimulationObject obj : getBaseObjects()) {
             if (obj instanceof CentroidPartObject) {
                 setCentroidPartActive((CentroidPartObject) obj, false);
             }
         }
     }
 
     public void setSolved() {
         //updateResultant();
         Builder builder = getCurrentState().getBuilder();
         builder.setSolved(true);
         pushState(builder.build());
         clearStateStack();
     }
 
     public boolean isSolved() {
         return getCurrentState().isLocked();
     }
 
     protected CentroidPart getCentroidPart() {
         return currentlySelected.getCentroidPart();//getKey();
     }
 
     @Override
     public String getDescriptionText() {
         return "Determine the centroid for the part: " + getKey();
     }
 
     @Override
     protected CentroidState createInitialState() {
         CentroidState.Builder builder = new CentroidState.Builder();
         return builder.build();
     }
 
     @Override
     public void completed() {
         //nothing apparently
     }
 
     @Override
     public Mode getMode() {
         return CentroidMode.instance;
     }
 
     @Override
     protected List<SimulationObject> getBaseObjects() {
         List<SimulationObject> objects = new ArrayList<SimulationObject>();
         //objects.add(cpObj);
         objects.addAll(body.getParts());
 
 
         //measurements?
         return objects;
     }
 
     public void updateCentroid() {
 //        for (SimulationObject cpo : getBaseObjects()) {
 //            if (cpo instanceof CentroidPartObject) {
 //                //System.out.println("#############"+((CentroidPartObject)cpo).getName());
 //                ((CentroidPartObject)cpo).setSelectable(true);
 //            }
 //        }
         //TODO FILL THIS OUT
         //Needs to update so that the centroid is displayed properly once the user solves,
         //but this might not be needed and we can do it like friction problems for mu
     }
 
     private void setCentroidPartActive(CentroidPartObject part, boolean active) {
         for (Representation representation : part.allRepresentations()) {
             if (representation instanceof MimicRepresentation) {
                 MimicRepresentation mimic = (MimicRepresentation) representation;
                 if (active) {
                     mimic.activate();
                 } else {
                     mimic.deactivate();
                 }
             }
         }
     }
 }
