 /*
  * EquationWorld.java
  *
  * Created on July 19, 2007, 3:23 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 package edu.gatech.statics.modes.equation;
 
 import edu.gatech.statics.Mode;
 import edu.gatech.statics.modes.equation.ui.EquationBar;
 import edu.gatech.statics.modes.equation.worksheet.Worksheet;
 import com.jme.math.Vector2f;
 import com.jme.math.Vector3f;
 import com.jme.renderer.ColorRGBA;
 import com.jme.renderer.Renderer;
 import edu.gatech.statics.objects.SimulationObject;
 import edu.gatech.statics.application.StaticsApplication;
 import edu.gatech.statics.exercise.BodySubset;
 import edu.gatech.statics.exercise.Exercise;
 import edu.gatech.statics.exercise.SubDiagram;
 import edu.gatech.statics.math.Quantity;
 import edu.gatech.statics.math.Unit;
 import edu.gatech.statics.objects.Body;
 import edu.gatech.statics.objects.Connector;
 import edu.gatech.statics.objects.Point;
 import edu.gatech.statics.math.Vector;
 import edu.gatech.statics.math.Vector3bd;
 import edu.gatech.statics.modes.equation.ui.EquationModePanel;
 import edu.gatech.statics.modes.equation.worksheet.EquationMath;
 import edu.gatech.statics.modes.equation.worksheet.EquationMathMoments;
 import edu.gatech.statics.modes.equation.worksheet.Worksheet2D;
 import edu.gatech.statics.modes.fbd.FreeBodyDiagram;
 import edu.gatech.statics.objects.Load;
 import edu.gatech.statics.objects.Measurement;
 import edu.gatech.statics.objects.Moment;
 import edu.gatech.statics.objects.UnknownPoint;
 import edu.gatech.statics.objects.VectorObject;
 import edu.gatech.statics.objects.representations.ArrowRepresentation;
 import edu.gatech.statics.objects.representations.CurveUtil;
 //import edu.gatech.statics.util.SelectableFilter;
 import edu.gatech.statics.ui.InterfaceRoot;
 import edu.gatech.statics.util.SelectionFilter;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class EquationDiagram extends SubDiagram {
 
     private Worksheet worksheet;
 
     public Worksheet getWorksheet() {
         return worksheet;
     }
     private Point momentPoint;
 
     /**
      * Sets the point about which the moment will be calculated.
      * This also notifies the panel to update its value for the moment point.
      * @param momentPoint
      */
     public void setMomentPoint(Point momentPoint) {
         this.momentPoint = momentPoint;
         EquationModePanel eqPanel = (EquationModePanel) InterfaceRoot.getInstance().getApplicationBar().getModePanel();
         eqPanel.setMomentPoint(momentPoint);
     }
 
     /**
      * Returns the point about which the moment is being calculated.
      * @return
      */
     public Point getMomentPoint() {
         return momentPoint;
     }
 
     public EquationMath getChecker() {
         return new EquationMath(this);
     }
     // IMPORTANT NOTE HERE
     // if the vector is symbolic and has been reversed,
     // then it will no longer be *equal* to the value stored as a key
     // in vectorMap. So, we need to use another approach to pull out the vector.
     // This is why we do not use a hashmap
     public Load getLoad(VectorObject vector) {
         //return vectorMap.get(vector);
         if (vector == null) {
             return null;
         }
         for (SimulationObject obj : allObjects()) {
             if (obj instanceof Load) {
                 Load load = (Load) obj;
                 if (vector.equals(load)) {
                     return load;
                 }
                 if (vector.isSymbol() && load.isSymbol() &&
                         vector.getVector().equalsSymbolic(load.getVector()) &&
                         vector.getSymbolName().equals(load.getSymbolName())) {
                     return load;
                 //vectorMap.put(load.getVector(), load);
                 }
             }
         }
         return null;
     }
 
     public Load getLoad(String symbolName) {
         if (symbolName == null) {
             return null;
         }
         for (SimulationObject obj : allObjects()) {
             if (obj instanceof Load) {
                 Load load = (Load) obj;
                 if (load.getSymbolName() != null && load.getSymbolName().equals(symbolName)) {
                     return load;
                 }
             }
         }
         return null;
     }
 
     @Override
     protected List<SimulationObject> getBaseObjects() {
         FreeBodyDiagram fbd = StaticsApplication.getApp().getExercise().getFreeBodyDiagram(getBodySubset());
         return fbd.allObjects();
     }
 
     /** Creates a new instance of EquationWorld */
     public EquationDiagram(BodySubset bodies) {
         super(bodies);
 
         // FIXME: This diagram automatically loads a 2D worksheet
         worksheet = new Worksheet2D(this);
     }
 
     /**
      * This actually updates the vectors and joints with the result of the solution.
      * The update applies to objects within the EquationDiagram, but these are references
      * to the original copies defined in the schematic, which are updated correspondingly.
      * @param values
      */
     public void performSolve(Map<Quantity, Float> values) {
 
         // FIRST BATCH
         // this first batch of updates updates basic values, vectors and measurements
         // the second batch will depend on these values.
 
         // go through the vectors, and make sure everything is in order:
         // give the vectors the new solved values
         // also go through measurements
         for (SimulationObject obj : allObjects()) {
             if (obj instanceof Load) {
                 Load vObj = (Load) obj;
                 Vector v = vObj.getVector();
                 if (v.isSymbol() && !v.isKnown()) {
                     // v is a symbolic force, but is not yet solved.
                     float value = values.get(v.getQuantity());
                     //v.setValue(v.getValueNormalized().mult( value ));
                     vObj.setDiagramValue(new BigDecimal(value));
                     vObj.setKnown(true);
 
                     // attempt to make sure the vector value is updated in the symbol manager
                     Load symbolManagerLoad = Exercise.getExercise().getSymbolManager().getLoad(vObj);
 
                     /*if (symbolManagerLoad == null) {
                     symbolManagerLoad = Exercise.getExercise().getSymbolManager().getLoad2FM(vObj.getAnchor().getMember(), vObj);
                     }*/
 
                     if (symbolManagerLoad != vObj) {
                         symbolManagerLoad.setDiagramValue(new BigDecimal(value));
                         symbolManagerLoad.setKnown(true);
                     }
                 }
             }
 
             if (obj instanceof Measurement) {
                 Measurement measure = (Measurement) obj;
                 // ignore if the measurement is already known
                 if (measure.isKnown()) {
                     continue;
                 }
                 // this is kind of a crummy test, but we'll try it anyway
                 for (Map.Entry<Quantity, Float> entry : values.entrySet()) {
                     if (measure.getSymbolName().equals(entry.getKey().getSymbolName())) {
                         // okay, our measure actually is actually solved for in the solution
                         measure.updateQuantityValue(new BigDecimal(entry.getValue()));
                         measure.setKnown(true);
                     }
                 }
             }
         }
 
         // SECOND BATCH
         // this depends on the first batch to be updated before it will work
 
         // go through the joints, and mark the joints as solved,
         // assigning to them the solved values as having the updated vectors.
         // also go through unknown points...
         for (SimulationObject obj : allObjects()) {
             if (obj instanceof Connector) {
                 Connector connector = (Connector) obj;
                 if (connector.isSolved()) {
                     continue;
                 }
 
                 Point point = connector.getAnchor();
                 List<Vector> reactions = new ArrayList<Vector>();
                 /*for (Quantity q : values.keySet()) {
                 Load load = getLoad(q.getSymbolName());
                 if (load != null && load.getAnchor() == point) {
                 reactions.add(load.getVector());
                 }
                 }*/
 
                 // it's possible that the reactions are not meant for this particular connector
                 // go through all of our solved quantities and see if we can get some matches.
                 for (Vector reaction : connector.getReactions()) {
                     for (Quantity q : values.keySet()) {
                         Load load = getLoad(q.getSymbolName());
                         if (load != null && load.getAnchor() == point) {
                             // now test if the direction is okay
                             if (load.getVectorValue().equals(reaction.getVectorValue()) ||
                                     load.getVectorValue().equals(reaction.getVectorValue().negate())) {
                                 reactions.add(load.getVector());
                             }
                         }
                     }
                 }
 
                 if (reactions.isEmpty()) {
                     continue;
                 }
 
                 // hopefully this should be accurate...
                 Body solveBody = null;
 
                 for (Body body : allBodies()) {
                    if (body.getAttachedObjects().contains(point)) {
                         solveBody = body;
                     }
                 }
 
                 // we want to say that we have solved this joint from the perspective
                 // of the current body in question. 
                 connector.solveReaction(solveBody, reactions);
             }
 
             if (obj instanceof UnknownPoint) {
                 UnknownPoint point = (UnknownPoint) obj;
 
                 // we do not test whether point is known, because by virtue of the measurement being solved just above
                 // this would cause the point to always be known
 
                 // this is kind of a crummy test, but we'll try it anyway
                 for (Map.Entry<Quantity, Float> entry : values.entrySet()) {
                     if (point.getSymbol().equals(entry.getKey().getSymbolName())) {
                         // okay, our measure actually is actually solved for in the solution
                         point.setSolved();
                     }
                 }
             }
         }
     }
 
     @Override
     public void onClick(SimulationObject obj) {
         super.onClick(obj);
 
         if (StaticsApplication.getApp().getCurrentTool() != null &&
                 StaticsApplication.getApp().getCurrentTool().isActive()) {
 
 
 
             return;
         } // do not select vectors if we have a tool active
 
         EquationModePanel eqPanel = (EquationModePanel) InterfaceRoot.getInstance().getApplicationBar().getModePanel();
         eqPanel.onClick((Load) obj);
     }
 
     @Override
     public void onHover(SimulationObject obj) {
         super.onHover(obj);
 
         EquationModePanel eqPanel = (EquationModePanel) InterfaceRoot.getInstance().getApplicationBar().getModePanel();
         eqPanel.onHover((Load) obj);
 
         if (obj == null) {
             highlightVector(null);
         } else {
             highlightVector(((Load) obj));
         }
     }
 
     @Override
     public void activate() {
         super.activate();
         for (SimulationObject obj : allObjects()) {
             obj.setSelectable(true);
         }
 
         StaticsApplication.getApp().setDefaultAdvice(
                 java.util.ResourceBundle.getBundle("rsrc/Strings").getString("equation_welcome"));
 
         StaticsApplication.getApp().resetAdvice();
     }
     private SelectionFilter selector = new SelectionFilter() {
 
         public boolean canSelect(SimulationObject obj) {
             return obj instanceof Load;
         }
     };
 
     @Override
     public SelectionFilter getSelectionFilter() {
         return selector;
     }
 
     @Override
     public void render(Renderer r) {
         super.render(r);
 
         if (showingCurve) {
             CurveUtil.renderCurve(r, ColorRGBA.blue, curvePoints);
         }
 
         if (momentPoint != null) {
             CurveUtil.renderCircle(r, ColorRGBA.blue, momentPoint.getTranslation(),
                     2, r.getCamera().getDirection());
         }
     }
     private SimulationObject currentHover;
 
     public void highlightVector(final VectorObject v) {
 
         // handle visual highlighting
         if (currentHover != null) {
             currentHover.setDisplayHighlight(false);
         }
         Load load;
         if (v == null) {
             load = null;
         } else {
             load = getLoad(v);
         }
 
         if (load != null) {
             load.setDisplayHighlight(true);
 
 
         }
         currentHover = load;
 
         //sumBar.highlightVector(obj);
         EquationModePanel eqPanel = (EquationModePanel) InterfaceRoot.getInstance().getApplicationBar().getModePanel();
         EquationBar activeEquation = eqPanel.getActiveEquation();
         if (activeEquation.getMath() instanceof EquationMathMoments) {
             if (activeEquation.getMath().getTerm(v) != null || v == null) {
                 //showMomentArm(load);
             }
         }
     //showCurve(vectorMap.get(v), activeEquation.getLineAnchor(v));
     }
     private VectorObject momentArm;
     private Load momentArmTarget;
 
     private void showMomentArm(Load target) {
 
         if (target instanceof Moment) {
             target = null;
         }
 
         // we may get hovers onto the momentarm itself...
         if (momentArmTarget == target || target == momentArm) {
             return;
         }
 
         if (momentArm != null) {
             removeUserObject(momentArm);
             momentArm = null;
             momentArmTarget = null;
         }
 
         if (target == null) {
             return;
         }
 
         //if(!((EquationMathMoments) sumBar.getMath()).getObservationPointSet())
         if (momentPoint == null) {
             return;
         }
 
         //Vector3f observationPoint = ((EquationMathMoments) sumBar.getMath()).getObservationPoint();
         //Vector3f observationPointPos = this.momentPoint.getTranslation();
         Point targetPoint = target.getAnchor();
 
         // have a direction vector pointing from the observation point to the target point
         //Vector3f armDirection = targetPoint.getTranslation().subtract(observationPointPos).mult(-1);
         //Vector3f armDirection = targetPoint.getTranslation().subtract(observationPointPos).mult(-1/StaticsApplication.getApp().getDrawScale());
         Vector3bd armDirection = targetPoint.getPosition().subtract(this.momentPoint.getPosition());
         momentArm = new VectorObject(targetPoint, new Vector(Unit.distance, armDirection, new BigDecimal(armDirection.length())));
         momentArmTarget = target;
 
         ArrowRepresentation rep = new ArrowRepresentation(momentArm, false);
         rep.setAmbient(ColorRGBA.yellow);
         rep.setDiffuse(ColorRGBA.yellow);
         momentArm.addRepresentation(rep);
         momentArm.setSelectable(false);
 
         addUserObject(momentArm);
     }
     private boolean showingCurve = false;
     private Vector3f curvePoints[] = new Vector3f[3];
 
     private void showCurve(Load obj, Vector2f pos) {
         if (obj == null || pos == null) {
             showingCurve = false;
 
             return;
         }
 
         // yet another hack in finding the vector center here....
         curvePoints[  0] = obj.getDisplayCenter().add(obj.getTranslation()).mult(.5f);
         curvePoints[   2] = StaticsApplication.getApp().getCamera().getWorldCoordinates(pos, .1f);
         curvePoints[   1] = new Vector3f(curvePoints[
         2]);
         curvePoints[  1].y += .5f;
         showingCurve = true;
     }
 
     @Override
     public Mode getMode() {
         return EquationMode.instance;
     }
 }
