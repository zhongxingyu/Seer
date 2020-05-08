 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.gatech.statics.modes.truss.zfm;
 
 import edu.gatech.statics.Mode;
 import edu.gatech.statics.Representation;
 import edu.gatech.statics.application.StaticsApplication;
 import edu.gatech.statics.exercise.Diagram;
 import edu.gatech.statics.modes.select.SelectMode;
 import edu.gatech.statics.modes.truss.zfm.ZFMState.Builder;
 import edu.gatech.statics.objects.SimulationObject;
 import edu.gatech.statics.objects.bodies.TwoForceMember;
 import edu.gatech.statics.objects.representations.MimicRepresentation;
 import edu.gatech.statics.util.SelectionFilter;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class ZFMDiagram extends Diagram<ZFMState> {
 
     // the number of times check has been called: used for giving hints
     // does not need persistence.
     private int checkCount;
 
     public ZFMDiagram() {
         super(null);
     }
 
     @Override
     public String getName() {
         return "Find ZFMs";
     }
 
     @Override
     public void completed() {
         SelectMode.instance.load();
     }
 
     @Override
     public Mode getMode() {
         return ZFMMode.instance;
     }
 
     @Override
     protected List<SimulationObject> getBaseObjects() {
         // here, we add everything that is not a body. Primarily, we want to capture
         // the PotentialZFMs, but ignore the actual bodies.
 
         List<SimulationObject> objects = new ArrayList<SimulationObject>();
         for (SimulationObject obj : getSchematic().allObjects()) {
             if (obj instanceof TwoForceMember) {
                 // pass
             } else {
                 objects.add(obj);
             }
         }
         return objects;
     }
     private static final SelectionFilter filter = new SelectionFilter() {
 
         public boolean canSelect(SimulationObject obj) {
             return obj instanceof PotentialZFM;
         }
     };
 
     @Override
     public void activate() {
         super.activate();
 
         // activate the mimic representations
         for (SimulationObject obj : getBaseObjects()) {
             if (obj instanceof PotentialZFM) {
                 setPotentialZfmActive((PotentialZFM) obj, true);
             }
         }
     }
 
     @Override
     public void deactivate() {
         super.deactivate();
 
         // deactivate the mimic representations
         for (SimulationObject obj : getBaseObjects()) {
             if (obj instanceof PotentialZFM) {
                 setPotentialZfmActive((PotentialZFM) obj, false);
             }
         }
     }
 
     private void setPotentialZfmActive(PotentialZFM potential, boolean active) {
         for (Representation representation : potential.allRepresentations()) {
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
 
     @Override
     public SelectionFilter getSelectionFilter() {
         return filter;
     }
 
     @Override
     protected void stateChanged() {
         super.stateChanged();
 
         // update the display
         for (SimulationObject obj : allObjects()) {
             // mark to display as selected if it is in the list of selected objects.
             boolean isSelected = getCurrentState().getSelectedZFMs().contains(obj);
             obj.setDisplaySelected(isSelected);
         }
 
        if (getCurrentState().isLocked()) {
            completed();
        }
     }
 
     @Override
     protected ZFMState createInitialState() {
         return (new ZFMState.Builder()).build();
     }
 
     /**
      * Creates a SelectAction for use in this diagram.
      * Subclasses of ZFMDiagram that want to do interesting things with
      * user selection should override this and supply their own version of
      * SelectAction.
      * @param obj
      * @return
      */
     protected SelectAction createSelectAction(PotentialZFM obj) {
         return new SelectAction(obj);
     }
 
     @Override
     public void onClick(SimulationObject clicked) {
 
         PotentialZFM potentialClicked = (PotentialZFM) clicked;
 
         // create and perform the select action
         SelectAction action = createSelectAction(potentialClicked);
         performAction(action);
     }
 
     public void setSolved() {
         Builder builder = getCurrentState().getBuilder();
         builder.setLocked(true);
 
         pushState(builder.build());
         clearStateStack();
 
         //completed();
     }
 
     public boolean check() {
 
         checkCount++;
 
         String extraHint = "";
 
         if (checkCount > 10) {
             extraHint = "<br><b>Hint:</b> Try looking at unloaded joints attached to 3 two-force members and identify which of those members can carry loads.";
         }
         if (checkCount > 20) {
             extraHint = "<br><b>Hint:</b> If an unloaded joint has 3 two-force members attached to it, and two of those members have the same angle, then the third cannot carry a load.";
         }
 
         // perform this check using two loops, because students get confused regarding its process.
 
         // first make sure that the student has added all ZFMs
         for (SimulationObject obj : getBaseObjects()) {
             if (obj instanceof PotentialZFM) {
                 PotentialZFM potentialZfm = (PotentialZFM) obj;
                 boolean isZfm = potentialZfm.isZfm();
                 boolean userZfm = getCurrentState().getSelectedZFMs().contains(potentialZfm);
 
                 if (isZfm && !userZfm) {
                     // there is a ZFM that the user missed
                     StaticsApplication.getApp().setStaticsFeedback("You have missed a Zero Force Member" + extraHint);
                     return false;
                 }
             }
         }
 
         // then make sure that the student has not added anything that is NOT a ZMF
         for (SimulationObject obj : getBaseObjects()) {
             if (obj instanceof PotentialZFM) {
                 PotentialZFM potentialZfm = (PotentialZFM) obj;
                 boolean isZfm = potentialZfm.isZfm();
                 boolean userZfm = getCurrentState().getSelectedZFMs().contains(potentialZfm);
 
                 if (userZfm && !isZfm) {
                     // the user added a ZFM that is not really
                     StaticsApplication.getApp().setStaticsFeedback("You have selected a body that is not a Zero Force Member" + extraHint);
                 }
                return false;
             }
         }
 
         return true;
     }
 
     @Override
     public String getDescriptionText() {
         return "Select Zero Force Members";
     }
 }
