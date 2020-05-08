 /*
  * FBDWorld.java
  *
  * Created on June 12, 2007, 2:01 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 package edu.gatech.statics.modes.fbd;
 
 import com.jme.renderer.Renderer;
 import edu.gatech.statics.Mode;
 import edu.gatech.statics.application.StaticsApplication;
 import edu.gatech.statics.exercise.BodySubset;
 import edu.gatech.statics.exercise.Exercise;
 import edu.gatech.statics.exercise.SubDiagram;
 import edu.gatech.statics.math.AnchoredVector;
 import edu.gatech.statics.math.Unit;
 import edu.gatech.statics.modes.equation.EquationMode;
 import edu.gatech.statics.modes.fbd.tools.LabelManipulator;
 import edu.gatech.statics.modes.fbd.tools.LabelSelector;
 import edu.gatech.statics.objects.Body;
 import edu.gatech.statics.objects.Connector;
 import edu.gatech.statics.objects.Force;
 import edu.gatech.statics.objects.Load;
 import edu.gatech.statics.objects.Measurement;
 import edu.gatech.statics.objects.Moment;
 import edu.gatech.statics.objects.Point;
 import edu.gatech.statics.objects.SimulationObject;
 import edu.gatech.statics.util.SelectionFilter;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class FreeBodyDiagram extends SubDiagram<FBDState> {
 
     private FBDInput fbdInput;
     /**
      * This is a list of Loads that are currently maintained by the diagram.
      * These loads are not the state of the diagram, but will be updated to reflect
      * the state. We do not use a Map&lt;AnchoredVector,Load&gt; because that would
      * require there to be distinct AnchoredVectors in the state. This is not necessarily
      * the case, becuase users may add two forces that are equivalent. 
      */
     private List<Load> loadObjects = new ArrayList<Load>();
     /**
      * This list is of the temporary loads present in the diagram. Right now the list
      * does not do anything, but it may be useful for keeping track of what the temporary
      * loads are at any given moment.
      */
     private List<Load> temporaryLoads = new ArrayList<Load>();
 
     /**
      * This method adds a temporary load to the diagram. This load exists and is visible,
      * but does not reflect the actual state of the diagram. Any loads added via this method
      * will be cleared the next time that the state changes. 
      * @param load
      */
     public void addTemporaryLoad(Load load) {
         loadObjects.add(load);
         temporaryLoads.add(load);
         updateDiagram();
         //System.out.println("****** ADDING TEMPORARY LOAD");
     }
 
     /**
      * This method removes the specified temporary load.
      * @param load
      */
     public void removeTemporaryLoad(Load load) {
         loadObjects.remove(load);
         temporaryLoads.remove(load);
         updateDiagram();
         //System.out.println("****** REMOVING TEMPORARY LOAD");
     }
 
     /**
      * The user objects for this diagram are merely the loads that are present.
      * @return
      */
     @Override
     public List<Load> getUserObjects() {
         return loadObjects;
     }
 
     @Override
     public List<Body> allBodies() {
         //return super.allBodies();
         return new ArrayList<Body>(getBodySubset().getBodies());
     }
 
     /**
      * Returns true if this fbd is solved. This method delegates to the current state
      * of the diagram.
      * @return
      */
     public boolean isSolved() {
         return getCurrentState().isSolved();
     }
 
     /**
      * This method can be used for making sure that the loads which are displayed
      * are reflective of the state. Returns null if none is found.
      * This will search to find if a load corresponds to the vector provided. If
      * there are multiple vectors present in the state that are equivalent, this will return the first one.
      * @param vector
      * @return
      */
     protected Load getLoad(AnchoredVector vector) {
         // this uses the brute force technique.
 
         for (Load load : loadObjects) {
             if (load.getAnchoredVector().equals(vector)) {
                 return load;
             }
         }
         return null;
     }
 
     /**
      * This is called whenever the state changes, and causes the loads in this
      * diagram to be synchronized with what is present in the state.
      */
     @Override
    protected void stateChanged() {
         super.stateChanged();
 
         // clear the temporary list.
         // any actual temporary loads will be cleared subsequently.
         temporaryLoads.clear();
 
         // check if there are unnecessary loads now
         List<Load> missingLoads = new ArrayList<Load>();
         for (Load load : loadObjects) {
             AnchoredVector vector = load.getAnchoredVector();
             if (!getCurrentState().getAddedLoads().contains(vector)) {
                 missingLoads.add(load);
             }
         }
 
         // remove them
         for (Load load : missingLoads) {
             loadObjects.remove(load);
         }
 
         // check for newly added loads
         // this check does something special in that it uses an equivalence test,
         // but also needs to make sure that even duplicates get corresponding loads
         // to back them.
         List<AnchoredVector> newVectors = new ArrayList<AnchoredVector>();
         List<Load> accountedFor = new ArrayList<Load>(loadObjects);
         for (AnchoredVector vector : getCurrentState().getAddedLoads()) {
             // try to retrieve from the accounted list.
             Load load = null;
             for (Load candidate : accountedFor) {
                 if (candidate.getAnchoredVector().equals(vector)) {
                     load = candidate;                //if()
                 }
             }
             // if we have not found it 
             if (load == null) {
                 newVectors.add(vector);
             }
         }
 
         for (AnchoredVector vector : newVectors) {
             Load load = createLoad(vector);
             loadObjects.add(load);
         }
 
         updateDiagram();
 
         // check for an invalid state:
         // this may occur when corrupted states are saved via persistence. The
         // cause of this problem should be identified in the future, but for now we
         // should aim on preventing invalid states from being set
         // this checks for the particular case where the diagram is marked as solved, but has no
         // loads present in it whatsoever.
         if (getCurrentState().isLocked() && getCurrentState().getAddedLoads().isEmpty()) {
             // we have a problem.
             // there is nothing at all entered in the state itself, so we commit a new empty state.
             Logger.getLogger("Statics").log(Level.SEVERE, "Encountered push for invalid state in the fbd state");
             pushState(createInitialState());
             clearStateStack();
         }
     }
 
     /**
      * This creates a load from an AnchoredVector for display.
      * The method is intended to construct loads to reflect the state of the diagram.
      * This creates the representation. It should also probably set up any listeners
      * that need to work with the object.
      * @param vector
      * @return
      */
     protected Load createLoad(AnchoredVector vector) {
         Load load;
         if (vector.getUnit() == Unit.force) {
             load = new Force(vector);
         } else if (vector.getUnit() == Unit.moment) {
             load = new Moment(vector);
         } else {
             throw new IllegalArgumentException(
                     "Have some sort of invalid type of load: " + vector +
                     " the unit is a " + vector.getUnit() + ". It should be either a force or moment.");
         }
         load.createDefaultSchematicRepresentation();
         new LabelManipulator(load);
         return load;
     }
 
     /**
      * Override this method to supply diagram checkers with additional functionality.
      * @return
      */
     public FBDChecker getChecker() {
         return new FBDChecker(this);
     }
 
     /**
      * Reset simply instantiates the initial state.
      */
     public void reset() {
         FBDState initialState = createInitialState();
         pushState(initialState);
 
         currentHighlight = null;
         currentSelection = null;
     }
 
     /**
      * This marks the diagram as solved (or unmarks it when passing false),
      * and locks or unlocks the diagram correspondingly.
      * @param solved
      */
     public void setSolved(boolean solved) {
 
         // current state already reflects the change, OK
         if (getCurrentState().isSolved() == solved) {
             return;
         }
 
         // otherwise...
         // push the changed state with the solve
         FBDState.Builder builder = new FBDState.Builder(getCurrentState());
         builder.setSolved(solved);
         pushState(builder.build());
 
         // make sure that the state history is purged
         clearStateStack();
 
         if (solved) {
             for (SimulationObject obj : allObjects()) {
                 if (!(obj instanceof Load)) {
                     continue;
                 }
                 Load loadObject = (Load) obj;
                 if (!loadObject.isSymbol()) {
                     continue;
                 }
                 Exercise.getExercise().getSymbolManager().addSymbol(loadObject.getAnchoredVector());
             }
 
             String advice = java.util.ResourceBundle.getBundle("rsrc/Strings").getString("fbd_feedback_check_success");
             StaticsApplication.getApp().setStaticsFeedback(advice);
             StaticsApplication.getApp().setDefaultUIFeedback(advice);
             StaticsApplication.getApp().resetUIFeedback();
         } else {
         }
     }
 
     /**
      * This handles what happens when the diagram is solved.
      * By default, we load the equation mode. Later on, other exercise types may 
      * choose to pass this step.
      */
     @Override
     public void completed() {
         EquationMode.instance.load(getBodySubset());
     }
 
     /**
      * Get bodies that are adjacent to this free body diagram.
      * @return
      */
     public List<SimulationObject> getAdjacentObjects() {
         List<SimulationObject> adjacentObjects = new ArrayList<SimulationObject>();
         List<SimulationObject> centralObjects = getCentralObjects();
 
         for (Body body : getSchematic().allBodies()) {
             // go through each body in the schematic (not our list!)
             for (SimulationObject obj : body.getAttachedObjects()) {
                 if (obj instanceof Connector) {
                     // through each connector
                     Connector connector = (Connector) obj;
                     if ((getBodySubset().getBodies().contains(connector.getBody1()) ||
                             getBodySubset().getBodies().contains(connector.getBody2())) &&
                             !centralObjects.contains(body) && !adjacentObjects.contains(body)) {
                         // ok, the body is attached to a body in the diagram,
                         // but is not a body in the diagram
 
                         adjacentObjects.add(body);
 
                         // now we want to add adjacent points, but not points that are already in the diagram.
                         for (SimulationObject attached : body.getAttachedObjects()) {
                             if (attached instanceof Point && !centralObjects.contains(attached) && !adjacentObjects.contains(attached)) {
                                 adjacentObjects.add(attached);
                             }
                         }
                     }
                 }
             }
         }
         return adjacentObjects;
     }
 
     /**
      * These are the objects that are central to the FBD, getBaseObjects contains the central objects,
      * as well as the adjacent ones. The idea here is central, in contrast to adjacent.
      * @return
      */
     public List<SimulationObject> getCentralObjects() {
 
         List<SimulationObject> objects = new ArrayList<SimulationObject>();
         for (Body body : getBodySubset().getBodies()) {
             objects.add(body);
             for (SimulationObject obj : body.getAttachedObjects()) {
                 if (!(obj instanceof Load)) {
                     objects.add(obj);
                 }
             }
         }
         return objects;
     }
 
     @Override
     protected List<SimulationObject> getBaseObjects() {
         List<SimulationObject> objects = getCentralObjects();
 
         // adjacent body initial test
         objects.addAll(getAdjacentObjects());
 
         for (Measurement measurement : getSchematic().getMeasurements(getBodySubset())) {
             objects.add(measurement);
         }
         return objects;
     }
 
     /** Creates a new instance of FBDWorld */
     public FreeBodyDiagram(BodySubset bodies) {
         super(bodies);
         fbdInput = new FBDInput(this);
     }
 
     /*@Override
     public void addUserObject(SimulationObject obj) {
     super.addUserObject(obj);
     if (obj instanceof Load) {
     addedLoads.add((Load) obj);
     new LabelManipulator((Load) obj);
     }
     }*/
     @Override
     public FBDInput getInputHandler() {
         return fbdInput;
     }
     private static final SelectionFilter filter = new SelectionFilter() {
 
         public boolean canSelect(SimulationObject obj) {
             return obj instanceof Load;
         }
     };
 
     @Override
     public SelectionFilter getSelectionFilter() {
         return filter;
     }
     private Load currentHighlight;
     private Load currentSelection;
 
     @Override
     public void onHover(SimulationObject obj) {
 
         if (currentHighlight == obj) {
             return;
         }
 
         // we are changing our hover, so clear the current
         if (currentHighlight != null) {
             currentHighlight.setDisplayHighlight(false);
         }
 
         if (obj == null) {
             currentHighlight = null;
             return;
         }
 
         if (obj != currentSelection) {
             currentHighlight = (Load) obj;
             currentHighlight.setDisplayHighlight(true);
         }
     }
 
     @Override
     public void onClick(SimulationObject obj) {
 
         if (obj == null) {
             if (currentSelection == null) {
                 return;
             }
             currentSelection.setDisplaySelected(false);
             currentSelection = null;
             return;
         }
 
         if (currentSelection == obj) {
             currentSelection.setDisplaySelected(false);
             currentSelection = null;
         } else {
             if (currentSelection != null) {
                 currentSelection.setDisplaySelected(false);
             }
             currentSelection = (Load) obj;
             currentSelection.setDisplaySelected(true);
 
             fbdInput.onSelect(currentSelection);
         }
     }
 
     /**
      * This is the method responsible for opening the popup dialog to change the label
      * of a load when its label has been double-clicked.
      * @param load
      */
     public void onLabel(Load load) {
         if (getCurrentState().isLocked()) {
             return;
         }
 
         LabelSelector labelTool = new LabelSelector(this, load, load.getAnchor().getTranslation());
         labelTool.setAdvice("Please give a name or a value for your load");
         //labelTool.setUnits(load.getUnit().getSuffix());
         //labelTool.setHintText("");
         if (load.isSymbol()) {
             labelTool.setHintText(load.getSymbolName());
         } else {
             labelTool.setHintText(load.toStringDecimal());
         }
         labelTool.setIsCreating(false);
         labelTool.popup();
     }
 
     @Override
     public void render(Renderer r) {
         super.render(r);
         //renderIcon();
     }
 
     @Override
     public void activate() {
         super.activate();
 
         for (SimulationObject adjacent : getAdjacentObjects()) {
             adjacent.setDisplayGrayed(true);
         }
 
         StaticsApplication.getApp().setDefaultUIFeedback(
                 java.util.ResourceBundle.getBundle("rsrc/Strings").getString("fbd_feedback_welcome"));
         StaticsApplication.getApp().resetUIFeedback();
 
         if (getCurrentState().isLocked()) {
             setSolved(true);
         }
     }
 
     Load getSelection() {
         return currentSelection;
     }
 
     @Override
     public Mode getMode() {
         return FBDMode.instance;
     }
 
     @Override
     protected FBDState createInitialState() {
         return new FBDState.Builder().build();
     }
 
     @Override
     public String getDescriptionText() {
         return "Add loads: " + getKey();
     }
 }
