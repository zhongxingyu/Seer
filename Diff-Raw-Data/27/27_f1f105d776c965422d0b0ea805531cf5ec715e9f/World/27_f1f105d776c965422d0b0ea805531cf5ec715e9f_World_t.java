 /*
  * World.java
  *
  * Created on June 4, 2007, 3:14 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 
 package edu.gatech.statics;
 
 import com.jme.math.Matrix3f;
 import com.jme.math.Vector3f;
 import com.jme.renderer.Renderer;
 import edu.gatech.statics.application.Exercise;
 import com.jme.scene.Node;
 import com.jme.scene.Spatial;
 import com.jme.scene.state.RenderState;
import edu.gatech.statics.application.StaticsApplication;
 import edu.gatech.statics.objects.representations.LabelRepresentation;
 import edu.gatech.statics.util.SelectableFilter;
 import edu.gatech.statics.util.SelectionListener;
 import edu.gatech.statics.objects.Body;
 import edu.gatech.statics.objects.Point;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class World {
     
     private Exercise exercise;
     public Exercise getExercise() {return exercise;}
     public void setExercise(Exercise exercise) {this.exercise = exercise;}
     
     private List<SimulationObject> allObjects = new ArrayList<SimulationObject>();
     public List<SimulationObject> allObjects() {return Collections.unmodifiableList(allObjects);}
     public void add(SimulationObject obj) {
         if(!allObjects.contains(obj)) {
             allObjects.add(obj);
             invalidateNodes();
         }
     }
     public void remove(SimulationObject obj) {
         allObjects.remove(obj);
         invalidateNodes();
     }
     public void removeAllObjects() {
         allObjects.clear();
         invalidateNodes();
     }
         
     private Map<RepresentationLayer, Node> representationNodes = new HashMap<RepresentationLayer, Node>();
     public Node getNode(RepresentationLayer layer) {return representationNodes.get(layer);}
     public Node getNode(String layer) {return representationNodes.get(RepresentationLayer.getLayer(layer));}
     
     public List<Body> allBodies() {
         List<Body> bodies = new ArrayList<Body>();
         for(SimulationObject obj : allObjects)
             if(obj instanceof Body)
                 bodies.add((Body)obj);
         return bodies;
     }
     
     // we cache the lists because these are refreshed constantly
     // if new ones are returned, lots of Object[] instances are left and hog memory
     private Map<RepresentationLayer, List<Representation>> representationCache = new HashMap<RepresentationLayer, List<Representation>>();
     
     public List<Representation> getRepresentations(RepresentationLayer layer) {
         List<Representation> r =  representationCache.get(layer);
         if(r == null)
              representationCache.put(layer, r = new ArrayList<Representation>());
         
         r.clear();
         for(SimulationObject obj : allObjects)
             r.addAll(obj.getRepresentation(layer));
         return r;
     }
     
     private SelectableFilter filter;
     public void setSelectableFilter(SelectableFilter filter) {this.filter = filter;}
     public SelectableFilter getSelectableFilter() {return filter;}
     
     // OVERRIDE for modes where selection is enabled by default
     public void setSelectableFilterDefault() {this.filter = null;}
     
     private List<SimulationObject> selectedObjects = new ArrayList<SimulationObject>();
     public List<SimulationObject> getSelectedObjects() {return selectedObjects;}
     
     //private SimulationObject selected = null;
     //public SimulationObject getSelected() {return selected;}
     //private SimulationObject hover = null;
     //public SimulationObject getHover() {return hover;}
     
     private CoordinateSystem coordinateSystem = new CoordinateSystem();
     public CoordinateSystem getCoordinateSystem() {return coordinateSystem;}
     public void setCoordinateSystem(CoordinateSystem sys) {this.coordinateSystem = sys;}
     
     private List<SelectionListener> selectionListeners = new ArrayList<SelectionListener>();
     public void addSelectionListener(SelectionListener listener) {selectionListeners.add(listener);}
     public void removeSelectionListener(SelectionListener listener) {selectionListeners.remove(listener);}
     public void removeAllSelectionListeners() {selectionListeners.clear();}
     protected List<SelectionListener> getSelectionListeners() {return selectionListeners;}
     
     private boolean enableManipulatorsOnSelect = true;
     private boolean enableManipulatorsOnSelectDefault = true;
     public void enableManipulatorsOnSelect(boolean enabled) {enableManipulatorsOnSelect = enabled;}
     protected void enableManipulatorsOnSelectDefault(boolean enabled) {enableManipulatorsOnSelectDefault = enabled; enableManipulatorsOnSelect = enabled;}
     public void enableManipulatorsOnSelectDefault() {enableManipulatorsOnSelect = enableManipulatorsOnSelectDefault;}
     protected boolean enableManipulatorsOnSelect() {return enableManipulatorsOnSelect;}
     
     private boolean enableSelectMultiple = true;
     private boolean enableSelectMultipleDefault = true;
     public void enableSelectMultiple(boolean enabled) {enableSelectMultiple = enabled;}
     protected void enableSelectMultipleDefault(boolean enabled) {enableSelectMultipleDefault = enabled; enableSelectMultiple = enabled;}
     public void enableSelectMultipleDefault() {enableSelectMultiple = enableSelectMultipleDefault;}
     protected boolean enableSelectMultiple() {return enableSelectMultiple;}
     
     private List<LabelRepresentation> labels = new ArrayList<LabelRepresentation>();
     public List<LabelRepresentation> getLabels() {return labels;}
     
     /** Creates a new instance of World */
     public World() {
         setSelectableFilterDefault();
     }
     
     private boolean nodesUpdated = false;
     public void invalidateNodes() {nodesUpdated = false;}
     protected void updateNodes() {
         if(nodesUpdated)
             return;
         
         nodesUpdated = true;
         
         representationNodes.clear();
         labels.clear();
         
         List<RepresentationLayer> allLayers = RepresentationLayer.getLayers();
         
         for(RepresentationLayer layer : allLayers) {
             
             Node node = representationNodes.get(layer); //new Node();
             if(node == null) {
                 node = new Node();
                 representationNodes.put(layer, node);
             }
             node.detachAllChildren();
             
             boolean updateRenderState = false;
             
             for(Representation r : getRepresentations(layer)) {
                 if(!r.isHidden()) {
                     node.attachChild(r);
                     addLabels(r);
                 }
                 if(!r.renderUpdated()) {
                     updateRenderState = true;
                     r.setRenderUpdated();
                 }
             }
             
             //if(layer.getRenderStatesChanged()) { 
             if(updateRenderState) {
                 for(RenderState renderState : layer.getRenderStates())
                     node.setRenderState(renderState);
                 node.updateRenderState();
             }
             //}
             
             node.updateGeometricState(0f, true);
             node.updateWorldBound();
             //node.updateModelBound();
             //node.updateRenderState();
         }
     }
     
     private void addLabels(Node node) {
         
         // search children 
         // this seems cheap, but otherwise seems to be the best way to collect
         // label representations...
     
         if(node instanceof Representation &&
                 !((Representation)node).getLayer().isEnabled() )
             return;
         
         if(node.getChildren() != null)
             for(Spatial child : node.getChildren())
                 if(child instanceof Node)
                     addLabels((Node) child);
     
         if(node instanceof LabelRepresentation &&
                 ((LabelRepresentation)node).getLayer().isEnabled())
             labels.add((LabelRepresentation) node);
     }
     
     public void update() {
         updateNodes();
         for(SimulationObject obj : allObjects)
             obj.update();
     }
     
     // change this--
     // hover arguments, call manner is fine
     // but World has set of objects that may be selected, or a boolean test for it canSelect(...)
     // also, World has list of selected objects, which can be built upon
     
     public void hover(SimulationObject obj) {
         
         for(SelectionListener listener : selectionListeners)
             listener.onHover(obj);
         
         // first, clear all highlights
         for(SimulationObject obj1 : allObjects)
             if(obj1 != obj)
                 obj1.setDisplayHighlight(false);
         
         // actually highlight if we can
         if(     filter != null &&
                 obj != null &&
                 obj.isSelectable() &&
                 filter.canSelect(obj)) {
             obj.setDisplayHighlight(true);
         }
     }
     
     public void click(SimulationObject obj) {
         
         // special things may happen depending on mode?
         // certain objects may be defined as selectable, etc.
         
         // notify listeners of click
         // some listeners will remove themselves on the event, so use a new list
         // to prevent comodification
         List<SelectionListener> selectionListeners1 = new ArrayList<SelectionListener>(selectionListeners);
         for(SelectionListener listener : selectionListeners1)
             listener.onClick(obj);
         
         if(obj == null) {
             clearSelection();
             return;
         }
         
         //System.out.println("Clicked... "+obj+" "+getSelectedObjects().contains(obj));
         
         if(getSelectableFilter() != null && obj.isSelectable() && getSelectableFilter().canSelect(obj)) {
             
             if(getSelectedObjects().contains(obj)) {
                 // deselect if already selected
                 getSelectedObjects().remove(obj);
                 obj.setDisplaySelected(false);
                 obj.enableManipulators(false);
                 
             } else {
                 if(!enableSelectMultiple())
                     clearSelection();
                 
                 // select actual object now
                 if(obj.isSelectable()) {
                     select(obj);
                 }
             }
         }
     }
     
     public void select(SimulationObject obj) {
         
         //System.out.println("Selecting...");
         
         // notify listeners
         for(SelectionListener listener : getSelectionListeners())
             listener.onSelect(obj);
 
         // add to selection, and update display
         getSelectedObjects().add(obj);
         obj.setDisplaySelected(true);
 
         if(enableManipulatorsOnSelect())
             obj.enableManipulators(true);
     }
     
     public void clearSelection() {
         
         //System.out.println("Clearing Selection...");
         
         selectedObjects = new ArrayList<SimulationObject>();
         for(SimulationObject obj : allObjects) {
             obj.setDisplaySelected(false);
             obj.enableManipulators(false);
         }
     }
     
     public void activate() {
         setSelectableFilterDefault();
         invalidateNodes();
        StaticsApplication.getApp().setCurrentTool(null);
     }
 
     public void render(Renderer r) {
         
         for(RepresentationLayer layer : RepresentationLayer.getLayers()) {
             //r.clearBuffers();
             if(layer.isEnabled()) {
                 r.draw(getNode(layer));
                 r.renderQueue();
                 //GameTaskQueueManager.getManager().getQueue(GameTaskQueue.RENDER).execute();
                 
                 /*
                 if(layer == RepresentationLayer.vectors)
                     Debugger.setBoundsColor(ColorRGBA.pink);
                 else Debugger.setBoundsColor(ColorRGBA.white);
                 
                 Debugger.drawBounds(getNode(layer), r, true);
                 */
             }
             r.clearZBuffer();
         }
         
     }
 
     public List<Vector3f> getSensibleDirections(Point point) {
         List<Vector3f> directions = new ArrayList<Vector3f>();
         
         directions.add(new Vector3f(1,0,0));
         directions.add(new Vector3f(-1,0,0));
         directions.add(new Vector3f(0,1,0));
         directions.add(new Vector3f(0,-1,0));
         
         for(Body body : allBodies()) {
             if(!body.getAttachedObjects().contains(point))
                 continue;
             
             // body has the point, now get its orientations...
             Matrix3f orientation = body.getRotation();
             
             directions.add( orientation.mult(new Vector3f(1,0,0)) );
             directions.add( orientation.mult(new Vector3f(-1,0,0)) );
             directions.add( orientation.mult(new Vector3f(0,1,0)) );
             directions.add( orientation.mult(new Vector3f(0,-1,0)) );
         }
         
         return directions;
     }
 }
