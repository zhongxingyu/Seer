 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.gatech.statics.modes.truss;
 
 import com.jme.math.Vector2f;
 import com.jme.math.Vector3f;
 import com.jme.renderer.ColorRGBA;
 import com.jme.renderer.Renderer;
 import edu.gatech.statics.Mode;
 import edu.gatech.statics.application.StaticsApplication;
 import edu.gatech.statics.exercise.BodySubset;
 import edu.gatech.statics.exercise.Diagram;
 import edu.gatech.statics.exercise.Exercise;
 import edu.gatech.statics.modes.fbd.FBDMode;
 import edu.gatech.statics.modes.truss.ui.TrussModePanel;
 import edu.gatech.statics.modes.truss.zfm.ZeroForceMember;
 import edu.gatech.statics.objects.Body;
 import edu.gatech.statics.objects.SimulationObject;
 import edu.gatech.statics.objects.bodies.PointBody;
 import edu.gatech.statics.objects.bodies.TwoForceMember;
 import edu.gatech.statics.objects.representations.CurveUtil;
 import edu.gatech.statics.ui.InterfaceRoot;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class TrussSectionDiagram extends Diagram<TrussSectionState> {
 
     private SectionTool sectionTool;
     private SectionCut currentCut;
     private int selectionSide;
 
     public SectionCut getCurrentCut() {
         return currentCut;
     }
 
     public TrussSectionDiagram() {
         super(null);
         sectionTool = new SectionTool();
     }
 
     /**
      * This actually selects the side specified.
      * This will create a new FBD for the side and move to FBD mode.
      * @param side
      */
     public void selectSection(int side) {
         List<Body> bodiesOnSide = new ArrayList<Body>();
         for (Body body : allBodies()) {
 
             if (body instanceof TwoForceMember) {
                 TwoForceMember member = (TwoForceMember) body;
                 Vector3f end1_3d = StaticsApplication.getApp().getCamera().getScreenCoordinates(member.getEndpoint1().toVector3f());
                 Vector3f end2_3d = StaticsApplication.getApp().getCamera().getScreenCoordinates(member.getEndpoint2().toVector3f());
                 Vector2f end1 = new Vector2f(end1_3d.x, end1_3d.y);
                 Vector2f end2 = new Vector2f(end2_3d.x, end2_3d.y);
                 if (lineSegmentIntersection(currentCut.getSectionStart(), currentCut.getSectionEnd(), end1, end2)) {
                     // reject all bodies on the cut line.
                     continue;
                 }
             }
 
             if (side == getSideOfBody(body)) {
                 bodiesOnSide.add(body);
             }
         }
 
         String specialName = "Section ";
         //boolean first = true;
         for (Body body : bodiesOnSide) {
             if (body instanceof PointBody) {
                 //if (!first) {
                 //    specialName += "";
                 //}
                 specialName += ((PointBody) body).getAnchor().getName();
 
             //first = false;
             }
         }
 
         BodySubset bodies = new BodySubset(bodiesOnSide);
         bodies.setSpecialName(specialName);
 
         // attempt to get the most recent diagram
         Diagram recentDiagram = Exercise.getExercise().getRecentDiagram(bodies);
         if (recentDiagram == null) {
             // try to create a FBD
             recentDiagram = Exercise.getExercise().createNewDiagram(bodies, FBDMode.instance.getDiagramType());
         }
         recentDiagram.getMode().load(bodies);
     }
 
     @Override
     protected TrussSectionState createInitialState() {
         return new TrussSectionState();
     }
 
     @Override
     public void completed() {
         // do nothing.
     }
 
     @Override
     public Mode getMode() {
         return TrussSectionMode.instance;
     }
 
     @Override
     protected List<SimulationObject> getBaseObjects() {
         List<SimulationObject> baseObjects = new ArrayList<SimulationObject>();
         for (Body body : getSchematic().allBodies()) {
             if (body instanceof ZeroForceMember) {
                 // ignore ZFMs
                 continue;
             }
             baseObjects.add(body);
         }
         return baseObjects;
     }
 
     @Override
     public void activate() {
         super.activate();
         StaticsApplication.getApp().enableDrag(false);
         StaticsApplication.getApp().setCurrentTool(sectionTool);
         sectionTool.setEnabled(true);
         currentCut = null;
 
         StaticsApplication.getApp().setDefaultAdvice("Click and drag to create a section");
         StaticsApplication.getApp().resetAdvice();
     }
 
     @Override
     public void deactivate() {
         super.deactivate();
         StaticsApplication.getApp().enableDrag(true);
         sectionTool.cancel();
 
         TrussModePanel modePanel = (TrussModePanel) InterfaceRoot.getInstance().getModePanel(TrussSectionMode.instance.getModeName());
         modePanel.hideSectionBoxes();
         clearHighlights();
     }
 
     @Override
     public void render(Renderer r) {
         super.render(r);
 
         if (sectionTool.isEnabled() && sectionTool.isMouseDown()) {
             SectionCut sectionCut = sectionTool.getSectionCut();
             drawCut(r, sectionCut);
         } else if (currentCut != null) {
             drawCut(r, currentCut);
         }
     }
 
     void onCancel() {
         currentCut = null;
     }
 
     private void drawCut(Renderer r, SectionCut sectionCut) {
         Vector2f sectionStart = sectionCut.getSectionStart();
         Vector2f sectionEnd = sectionCut.getSectionEnd();
         float length = sectionStart.subtract(sectionEnd).length();
         if (length < 1) {
             return;
         }
         // scale the points away so that the line takes up the whole screen.
         float scaleBy = 2000f / length;
         //System.out.println("length: " + length + " scaleby: " + scaleBy);
         Vector2f sectionDifference = sectionEnd.subtract(sectionStart);
         sectionStart = sectionStart.subtract(sectionDifference.mult(scaleBy / 2));
         sectionEnd = sectionEnd.add(sectionDifference.mult(scaleBy / 2));
         Vector3f sectionStart3d = StaticsApplication.getApp().getCamera().getWorldCoordinates(sectionStart, 0.1f);
         Vector3f sectionEnd3d = StaticsApplication.getApp().getCamera().getWorldCoordinates(sectionEnd, 0.1f);
 
         CurveUtil.renderLine(r, ColorRGBA.blue, sectionStart3d, sectionEnd3d);
     }
 
     public void onCreateSection(SectionCut section) {
         //System.out.println("***** Drawing a section");
 
         List<TwoForceMember> cutMembers = getAllCutMembers(section);
 
         // first, make sure that the section is valid at all.
         // if it is, actually make the selection, otherwise
         if (cutMembers.size() > 0) {
             currentCut = section;
             TrussModePanel modePanel = (TrussModePanel) InterfaceRoot.getInstance().getModePanel(TrussSectionMode.instance.getModeName());
             modePanel.showSectionBoxes(section);
         } else {
             currentCut = null;
             TrussModePanel modePanel = (TrussModePanel) InterfaceRoot.getInstance().getModePanel(TrussSectionMode.instance.getModeName());
             modePanel.hideSectionBoxes();
             clearHighlights();
         }
     }
 
     /**
      * Remove all of the highlights in the bodies.
      */
    @Override
    public void clearHighlights() {
         for (Body body : allBodies()) {
             body.setDisplayHighlight(false);
             body.setDisplayGrayed(false);
         }
     }
 
     /**
      * This is called when the mouse has moved to one or the other side of the hover.
      * This method assumes that the section exists and is non null. If it is null, the method returns.
      * @param side
      */
     public void setSelectionHover(int side) {
         if (selectionSide == side || currentCut == null) {
             return;
         }
 
         List<TwoForceMember> allCutMembers = getAllCutMembers(currentCut);
 
         // highlight things appropriately
         for (Body body : allBodies()) {
             if (getSideOfBody(body) == side && !allCutMembers.contains(body)) {
                 body.setDisplayHighlight(true);
                 body.setDisplayGrayed(false);
             } else {
                 body.setDisplayHighlight(false);
                 body.setDisplayGrayed(true);
             }
         }
     }
 
     /**
      * Returns 1 or -1 depending on what side of the current cut the given body falls on.
      * @return
      */
     private int getSideOfBody(Body body) {
         Vector3f screenCenter3d = StaticsApplication.getApp().getCamera().getScreenCoordinates(body.getTranslation());
         Vector2f screenCenter = new Vector2f(screenCenter3d.x, screenCenter3d.y);
         return currentCut.getCutSide(screenCenter);
     }
 
     /**
      * returns a list of the 2FMs that were cut by the given section.
      * @param section
      * @return
      */
     private List<TwoForceMember> getAllCutMembers(SectionCut section) {
 
         List<TwoForceMember> cutMembers = new ArrayList<TwoForceMember>();
 
         for (Body body : allBodies()) {
             if (body instanceof TwoForceMember) {
                 if (intersects2FM(section, (TwoForceMember) body)) {
                     cutMembers.add((TwoForceMember) body);
                 }
             }
         }
         return cutMembers;
     }
 
     /**
      * Checks to see if the section cut intersects the given 2 force member.
      * This calculates the cut as though the section were a whole line.
      * @param section
      * @return
      */
     private boolean intersects2FM(SectionCut section, TwoForceMember member) {
         // first project the ends of the 2fm onto the 2d camera plane.
         Vector3f end1_3d = StaticsApplication.getApp().getCamera().getScreenCoordinates(member.getEndpoint1().toVector3f());
         Vector3f end2_3d = StaticsApplication.getApp().getCamera().getScreenCoordinates(member.getEndpoint2().toVector3f());
         Vector2f end1 = new Vector2f(end1_3d.x, end1_3d.y);
         Vector2f end2 = new Vector2f(end2_3d.x, end2_3d.y);
 
         return //lineSegmentIntersection(end1, end2, section.getSectionStart(), section.getSectionEnd()) &&
                 lineSegmentIntersection(section.getSectionStart(), section.getSectionEnd(), end1, end2);
     }
 
     /**
      * Calculates whether the line and the segment intersect. The line is assumed to be infinite,
      * and the segment is finite.
      * @return
      */
     private boolean lineSegmentIntersection(Vector2f linePoint1, Vector2f linePoint2, Vector2f segmentPoint1, Vector2f segmentPoint2) {
 
         // The line defining the cut is infinite,
         // so we check to see if the ends of the member are on opposite sides of the line.
         // this fomula calculates the distance between the endpoints of the member and the line
         // if they have opposite signs, then they overlap.
         Vector2f lineDirection = linePoint2.subtract(linePoint1).normalize();
         Vector2f linePerp = new Vector2f(lineDirection.y, -lineDirection.x);
 
         float distance1 = linePerp.dot(segmentPoint1.subtract(linePoint1));
         float distance2 = linePerp.dot(segmentPoint2.subtract(linePoint1));
 
         // if the values are on opposite sides, they will have opposite signs,
         // and the product will be negative. Otherwise, the product will be positive.
         return distance1 * distance2 < 0;
     }
 }
