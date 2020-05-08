 //----------------------------------------------------------------------------
 // $Revision$
 // $Date$
 // $Author$
 //----------------------------------------------------------------------------
 package cytoscape.visual;
 //----------------------------------------------------------------------------
 import java.util.Iterator;
 import java.util.Properties;
 import java.util.ArrayList;
 import java.util.logging.Logger;
 
 import giny.model.Node;
 import giny.model.Edge;
 import giny.view.GraphView;
 import giny.view.NodeView;
 import giny.view.EdgeView;
 
 import cytoscape.data.CyNetwork;
 import cytoscape.view.NetworkView;
 import cytoscape.visual.ui.VizMapUI;
 
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ChangeEvent;
 //----------------------------------------------------------------------------
 /**
  * Top-level class for controlling the visual appearance of nodes and edges
  * according to data attributes, as well as some global visual attributes.
  * This class holds a reference to a NetworkView that displays the network,
  * a CalculatorCatalog that holds the set of known visual styles and
  * calculators, and a current VisualStyle that is used to determine the
  * values of the visual attributes. A Logger is also supplied to report errors.
  *
  * Note that a null VisualStyle is not allowed; this class always provides
  * at least a default object.
  *
  * The key methods are the apply* methods. These methods first recalculate
  * the visual appearances by delegating to the calculators contained in the
  * current visual style. The usual return value of these methods is an
  * Appearance object that contains the visual attribute values; these
  * values are then applied to the network by calling the appropriate set
  * methods in the graph view API.
  */
 public class VisualMappingManager extends SubjectBase {
     NetworkView networkView;      //the object displaying the network
     CalculatorCatalog catalog;    //catalog of visual styles and calculators
     VisualStyle visualStyle;      //the currently active visual style
     Logger logger;                //for reporting errors
     VizMapUI vizMapUI;            //the UI, to report visual style changes
 
     public VisualMappingManager(NetworkView networkView,
                                 CalculatorCatalog catalog,
                                 VisualStyle style,
                                 Logger logger) {
         this.networkView = networkView;
         this.catalog = catalog;
         this.logger = logger;
         if (style != null) {
             setVisualStyle(style);
         } else {//get a default from the catalog
             VisualStyle defStyle = catalog.getVisualStyle("default");
             setVisualStyle(defStyle);
         }
     }
 
     /**
      * This method should be called after creating the user interface
      * to the vizmapper (which requires this object to be created first).
      * This is needed when setting a new visual style, to tell the UI
      * to update to the new style.
      *
      * It would be better to set up a listener architecture, and then
      * change this method to addListener. The UI would then receive
      * an event when the visual style is changed here.
      */
     public void setUI(VizMapUI vizMapUI) {
         this.vizMapUI = vizMapUI;
     }
 
 
     public NetworkView getNetworkView() {return networkView;}
 
     public CyNetwork getNetwork() {
       return networkView.getNetwork();
     }
 
     public CalculatorCatalog getCalculatorCatalog() {return catalog;}
 
     public VisualStyle getVisualStyle() {return visualStyle;}
 
     /**
      * Sets a new visual style, and returns the old style. Also
      * notifies the UI to update to the new style.
      *
      * If the argument is null, no change is made, an error message
      * is passed to the logger, and null is returned.
      */
     public VisualStyle setVisualStyle(VisualStyle vs) {
         if (vs != null) {
             VisualStyle tmp = visualStyle;
             visualStyle = vs;
             // Added by iliana
             if (vizMapUI != null && vizMapUI.getStyleSelector() != null) {
               vizMapUI.getStyleSelector().setVisualStyle(vs);
             }
             //---
             this.fireStateChanged();
             return tmp;
         } else {
             String s = "VisualMappingManager: Attempt to set null VisualStyle";
             logger.severe(s);
             return null;
         }
     }
 
     /**
      * Sets a new visual style. Attempts to get the style with the given
      * name from the catalog and pass that to setVisualStyle(VisualStyle).
      * The return value is the old style.
      *
      * If no visual style with the given name is found, no change is made,
      * an error message is passed to the logger, and null is returned.
      */
     public VisualStyle setVisualStyle(String name) {
         VisualStyle vs = catalog.getVisualStyle(name);
         if (vs != null) {
             return setVisualStyle(vs);
         } else {
             String s = "VisualMappingManager: unknown VisualStyle: " + name;
             logger.severe(s);
             return null;
         }
     }
 
     /**
      * Recalculates and reapplies just the fill color visual attribute
      * to all nodes.
      *
      * I suspect this is intended for performance reasons if one only
      * wants to reset the fill color, but the gain is limited since the
      * node appearance calculator still recalculates all of the visual
      * attributes. It would be better to change things to only recalculate
      * the fill color.
      */
     public void applyNodeFillColor() {
         CyNetwork network = getNetwork();
         GraphView graphView = networkView.getView();
         NodeAppearanceCalculator nodeAppearanceCalculator =
                 visualStyle.getNodeAppearanceCalculator();
         for (Iterator i = graphView.getNodeViewsIterator(); i.hasNext(); ) {
             NodeView nodeView = (NodeView)i.next();
             Node node = nodeView.getNode();
             nodeView.setUnselectedPaint(
                     nodeAppearanceCalculator.calculateNodeFillColor(node, network) );
         }
     }
 
     /**
      * Recalculates and reapplies all of the node appearances. The
      * visual attributes are calculated by delegating to the
      * NodeAppearanceCalculator member of the current visual style.
      */
     public void applyNodeAppearances() {
         CyNetwork network = getNetwork();
         GraphView graphView = networkView.getView();
         NodeAppearanceCalculator nodeAppearanceCalculator =
                 visualStyle.getNodeAppearanceCalculator();
         for (Iterator i = graphView.getNodeViewsIterator(); i.hasNext(); ) {
             NodeView nodeView = (NodeView)i.next();
             Node node = nodeView.getNode();
             NodeAppearance na = new NodeAppearance();
             nodeAppearanceCalculator.calculateNodeAppearance(na,node,network);
 
             nodeView.setUnselectedPaint(na.getFillColor());
             nodeView.setBorderPaint(na.getBorderColor());
             nodeView.setBorder(na.getBorderLineType().getStroke());
             nodeView.setHeight(na.getHeight());
             nodeView.setWidth(na.getWidth());
             nodeView.setShape( ShapeNodeRealizer.getGinyShape(na.getShape()) );
             nodeView.setLabel(na.getLabel());
             //can't set font yet
             //can't set tooltip yet
         }
     }
 
     /**
      * Recalculates and reapplies all of the edge appearances. The
      * visual attributes are calculated by delegating to the
      * EdgeAppearanceCalculator member of the current visual style.
      */
     public void applyEdgeAppearances() {
         CyNetwork network = getNetwork();
         GraphView graphView = networkView.getView();
 
         EdgeAppearanceCalculator edgeAppearanceCalculator =
                 visualStyle.getEdgeAppearanceCalculator();
         for (Iterator i = graphView.getEdgeViewsIterator(); i.hasNext(); ) {
             EdgeView edgeView = (EdgeView)i.next();
             Edge edge = edgeView.getEdge();
             EdgeAppearance ea = new EdgeAppearance();
             edgeAppearanceCalculator.calculateEdgeAppearance(ea,edge,network);
 
             edgeView.setUnselectedPaint(ea.getColor());
             edgeView.setStroke(ea.getLineType().getStroke());
             edgeView.setSourceEdgeEnd(ea.getSourceArrow().getGinyArrow());
             edgeView.setTargetEdgeEnd(ea.getTargetArrow().getGinyArrow());
             //edgeView.setLabel(ea.getLabel());  //can't set edge label yet
             //can't set font yet
             //can't set tooltip yet
         }
     }
 
     /**
      * Recalculates and reapplies the global visual attributes. The
      * recalculation is done by delegating to the GlobalAppearanceCalculator
      * member of the current visual style.
      */
     public void applyGlobalAppearances() {
         CyNetwork network = getNetwork();
         GraphView graphView = networkView.getView();
         GlobalAppearanceCalculator globalAppearanceCalculator =
                 visualStyle.getGlobalAppearanceCalculator();
         GlobalAppearance ga =
                 globalAppearanceCalculator.calculateGlobalAppearance(network);
 
         graphView.setBackgroundPaint(ga.getBackgroundColor());
         //will ignore sloppy selection color for now
     }
 
     /**
      * Recalculates and reapplies all of the node, edge, and global
      * visual attributes. This method delegates to, in order,
      * applyNodeAppearances, applyEdgeAppearances, and
      * applyGlobalAppearances.
      */
     public void applyAppearances() {
         /** first apply the node appearance to all nodes */
         applyNodeAppearances();
         /** then apply the edge appearance to all edges */
         applyEdgeAppearances();
         /** now apply global appearances */
         applyGlobalAppearances();
         /** we rely on the caller to redraw the graph as needed */
   }
 }
