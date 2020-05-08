 package cytoscape.data.readers;
 import java.util.*;
 import java.awt.geom.Point2D;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.awt.Color;
 import java.text.DecimalFormat;
 import giny.view.*;
 import giny.model.*;
 import cytoscape.data.GraphObjAttributes;
 import cytoscape.view.CyNetworkView;
 import cytoscape.CyNetwork;
 import cytoscape.Cytoscape;
 import cytoscape.data.Semantics;
 
 
 /**
  * The purpse of this class is to translate cytoscape data structures
  * into a gml object tree, we can then use the gml parser to write this
  * tree out into a file
  */
 public class GMLWriter{
 
   /**
    * It is possible that nodes have been added to the graph since it was loaded.
    * This set will keep track of nodes and edges that are currently in the perspective
    * that have no corresponding entry in the object tree that was loaded with the
    * netowrk
    */
   Set newNodes,newEdges;
 
   /**
    * Given an object tree given in oldList, update it with the information 
    * provided in network and optionall view (if view is not null). The GML spec
    * requires that we remember all information provided in the original gml file.
    * Therefore, we pass in the old object tree as oldList, as execute functions which
    * will update that data structure. We would also like to save files that may
    * not have been loaded from a gml file. This list is empty in that case. Those
    * same update functions must be able to create all relevant key-value pairs
    * as well then.
    */
   public void writeGML(CyNetwork network, CyNetworkView view, List oldList){
     /*
      * Initially all the nodes and edges have not been seen
      */
     newNodes = new HashSet(network.getNodeCount());
     newEdges = new HashSet(network.getEdgeCount());
     for(Iterator it = network.nodesIterator();it.hasNext();){
       newNodes.add(new Integer(((Node)it.next()).getRootGraphIndex()));
     }
     for(Iterator it = network.edgesIterator();it.hasNext();){
       newEdges.add(new Integer(((Edge)it.next()).getRootGraphIndex()));
     }
     
     /*
      * We are going to make sure the keys graph,creator,and version
      * are present and update them fi they are already present
      */
     KeyValue graph = null, creator = null, version = null;
     for(Iterator it = oldList.iterator();it.hasNext();){
       KeyValue keyVal = (KeyValue)it.next();
       if(keyVal.key.equals(GMLReader2.GRAPH)){
 	graph = keyVal;
       }else if(keyVal.key.equals(GMLReader2.CREATOR)){
 	creator = keyVal;
       }else if(keyVal.key.equals(GMLReader2.VERSION)){
 	version = keyVal;
       }
     }
     if(creator == null){
       creator = new KeyValue(GMLReader2.CREATOR,null);
       oldList.add(creator);
     }
     if(version == null){
       version = new KeyValue(GMLReader2.VERSION,null);
       oldList.add(version);
     }
     if(graph == null){
       graph = new KeyValue(GMLReader2.GRAPH,new Vector());
       oldList.add(graph);
     }
     /*
      * Update the list associated with the graph
      * pair
      */
     writeGraph(network,view,(List)graph.value);
     creator.value = "Cytoscape";
     version.value = new Double(1.0);
 
 
     /*
      * After update all of the graph objects that were already present in the object tree
      * check and see if there are any objects in the current perspective that were not updated
      * For these objects, create an empty key-value mapping and then update it
      */
     List graph_list = (List)graph.value;
     while(!newNodes.isEmpty()){
       KeyValue nodePair = new KeyValue(GMLReader2.NODE,new Vector());
       graph_list.add(nodePair);
       ((List)nodePair.value).add(new KeyValue(GMLReader2.ROOT_INDEX,newNodes.iterator().next()));
       writeGraphNode(network,view,(List)nodePair.value);
     }
     while(!newEdges.isEmpty()){
       KeyValue edgePair = new KeyValue(GMLReader2.EDGE,new Vector());
       graph_list.add(edgePair);
       ((List)edgePair.value).add(new KeyValue(GMLReader2.ROOT_INDEX,newEdges.iterator().next()));
       writeGraphEdge(network,view,(List)edgePair.value);
     }
     
   }
   
   /**
    * Update the list associated with a graph key
    */
   private void writeGraph(CyNetwork network, CyNetworkView view, List oldList){
     
     for(Iterator it = oldList.iterator();it.hasNext();){
       KeyValue keyVal = (KeyValue)it.next();
       /*
        * For all nodes in the object tree, update the list
        * that is associated with that key. If this node
        * is no longer present in the perpsective, then
        * we must remove it from the ojbect tree. Also
        * do the same thing for the edges.
        */
       if(keyVal.key.equals(GMLReader2.NODE)){
 	if(!writeGraphNode(network,view,(List)keyVal.value)){
 	  it.remove();
 	}
       }else if(keyVal.key.equals(GMLReader2.EDGE)){
 	if(!writeGraphEdge(network,view,(List)keyVal.value)){
 	  it.remove();
 	}
       }
     }
 
   }
   
   /**
    * Update the list associated with a node key
    */
   private boolean writeGraphNode(CyNetwork network, CyNetworkView view, List oldList){
     /*
      * We expect a list associated with node key to potentially
      * have a graphic key, id key, and root_index key
      */
     Integer root_index = null;
     KeyValue graphicsPair = null;
     KeyValue labelPair = null;
     KeyValue idPair = null;
     for(Iterator it = oldList.iterator();it.hasNext();){
       KeyValue keyVal = (KeyValue)it.next();
       if(keyVal.key.equals(GMLReader2.ROOT_INDEX)){
 	root_index = (Integer)keyVal.value;
       }else if(keyVal.key.equals(GMLReader2.GRAPHICS)){
 	graphicsPair = keyVal;
       }else if(keyVal.key.equals(GMLReader2.LABEL)){
 	labelPair = keyVal;
       }else if(keyVal.key.equals(GMLReader2.ID)){
 	idPair = keyVal;
       }
     }
     
     /*
      * Check to see if this nodes is still in the perspective
      */
     if(root_index == null){
       return false;
     }
     Node node = Cytoscape.getRootGraph().getNode(root_index.intValue());
     if(!network.containsNode(node)){
       return false;
     }
     
     /*
      * Mark this node as seen
      */
     newNodes.remove(root_index);
     
     /*
      * Update or create the id key-value pair for this list
      */
     if(idPair == null){
       idPair = new KeyValue(GMLReader2.ID,null);
       oldList.add(idPair);
     }
     idPair.value = root_index;
     
     /*
      * Optionall update/create the graphics key-value pair for this
      * list if there is currently defined. NOte that if no view is defined,
      * the previously loaded view information will remain intact
      */
     if(view != null){
       if(graphicsPair == null){
 	graphicsPair = new KeyValue(GMLReader2.GRAPHICS,new Vector());
 	oldList.add(graphicsPair);
       }
       writeGraphNodeGraphics(network,view.getNodeView(node),(List)graphicsPair.value);
     }
     
     /*
      * Update/create the label key-value pair. We have co-opted this field
      * to mean the canoncial name
      */
     if(labelPair == null){
       labelPair = new KeyValue(GMLReader2.LABEL,null);
       oldList.add(labelPair);
     }
     labelPair.value = network.getNodeAttributeValue(node,Semantics.CANONICAL_NAME);
     return true;
     
   }
   
   
   /**
    * Update the list associated with an edge key
    */
   private boolean writeGraphEdge(CyNetwork network, CyNetworkView view, List oldList){
     /*
      * An edge key will definitely have a root_index, labelPair (we enforce this on loading),
      * source key, and a target key
      */
     Integer root_index = null;
     KeyValue graphicsPair = null;
     KeyValue labelPair = null;
     KeyValue sourcePair = null;
     KeyValue targetPair = null;
   
     for(Iterator it = oldList.iterator();it.hasNext();){
       KeyValue keyVal = (KeyValue)it.next();
       if(keyVal.key.equals(GMLReader2.GRAPHICS)){
 	graphicsPair = keyVal;
       }else if(keyVal.key.equals(GMLReader2.LABEL)){
 	labelPair = keyVal;
       }else if(keyVal.key.equals(GMLReader2.ROOT_INDEX)){
 	root_index = (Integer)keyVal.value;
       }else if(keyVal.key.equals(GMLReader2.SOURCE)){
 	sourcePair = keyVal;
       }else if(keyVal.key.equals(GMLReader2.TARGET)){
 	targetPair = keyVal;
       }
     }
     
     /*
      * Make sure the edge is still present in this perspective
      */
     if(root_index == null){
       return false;
     }
     Edge edge = Cytoscape.getRootGraph().getEdge(root_index.intValue());
     if(!network.containsEdge(edge)){
       return false;
     }
 
     newEdges.remove(root_index);
     if(targetPair == null){
       targetPair = new KeyValue(GMLReader2.TARGET,null);
       oldList.add(targetPair);
     }
     targetPair.value = new Integer(edge.getTarget().getRootGraphIndex());
 
     if(sourcePair == null){
       sourcePair = new KeyValue(GMLReader2.SOURCE,null);
       oldList.add(sourcePair);
     }
     sourcePair.value = new Integer(edge.getSource().getRootGraphIndex());
 
 
     if(view != null){
       if(graphicsPair == null){
 	graphicsPair = new KeyValue(GMLReader2.GRAPHICS,new Vector());
 	//will eventually make a new graphics pair here
       }
       writeGraphEdgeGraphics(network,view.getEdgeView(edge),(List)graphicsPair.value);
     }
     if(labelPair == null){
       labelPair = new KeyValue(GMLReader2.LABEL,null);
       oldList.add(labelPair);
     }
     labelPair.value = network.getEdgeAttributeValue(edge,Semantics.INTERACTION);
     return true;
 
   }
   
   /**
    * This writes all the graphical information for a particular node into
    * an object tree
    */
   private void writeGraphNodeGraphics(CyNetwork network, NodeView nodeView, List oldList){
     KeyValue x = null,y = null,w=null,h=null,type=null,fill = null,outline = null,outline_width = null;
     for(Iterator it = oldList.iterator();it.hasNext();){
       KeyValue keyVal = (KeyValue)it.next();
       if(keyVal.key.equals(GMLReader2.X)){
 	x = keyVal;
       }else if(keyVal.key.equals(GMLReader2.Y)){
 	y = keyVal;
       }else if(keyVal.key.equals(GMLReader2.W)){
 	w = keyVal;
       }else if(keyVal.key.equals(GMLReader2.H)){
 	h = keyVal;
       }else if(keyVal.key.equals(GMLReader2.TYPE)){
 	type = keyVal;
       }else if(keyVal.key.equals(GMLReader2.FILL)){
 	fill = keyVal;
       }else if(keyVal.key.equals(GMLReader2.OUTLINE)){
 	outline = keyVal;
       }else if(keyVal.key.equals(GMLReader2.OUTLINE_WIDTH)){
 	outline_width = keyVal;
       }
     }
     
     if(x == null){
       x = new KeyValue(GMLReader2.X,null);
       oldList.add(x);
     }
     if(y == null){
       y = new KeyValue(GMLReader2.Y,null);
       oldList.add(y);
     }
     if(w == null){
       w = new KeyValue(GMLReader2.W,null);
       oldList.add(w);
     }
     if(h == null){
       h = new KeyValue(GMLReader2.H,null);
       oldList.add(h);
     }
     if(fill == null){
       fill = new KeyValue(GMLReader2.FILL,null);
       oldList.add(fill);
     }
     if(type == null){
       type = new KeyValue(GMLReader2.TYPE,null);
       oldList.add(type);
     }
     if(outline == null){
       outline = new KeyValue(GMLReader2.OUTLINE,null);
       oldList.add(outline);
     }
     if(outline_width == null){
       outline_width = new KeyValue(GMLReader2.OUTLINE_WIDTH,null);
       oldList.add(outline_width);
     }
 
     x.value = new Double(nodeView.getXPosition());
     y.value = new Double(nodeView.getYPosition());
     w.value = new Double(nodeView.getWidth());
     h.value = new Double(nodeView.getHeight());
     fill.value = getColorHexString((Color)nodeView.getUnselectedPaint());
     outline.value = getColorHexString((Color)nodeView.getBorderPaint());
     outline_width.value = new Double(nodeView.getBorderWidth());
     switch(nodeView.getShape()){
     case NodeView.RECTANGLE:
       type.value = GMLReader2.RECTANGLE;break;
     case NodeView.ELLIPSE:
       type.value = GMLReader2.ELLIPSE;break;
     case NodeView.DIAMOND:
       type.value = GMLReader2.DIAMOND;break;
     case NodeView.HEXAGON:
       type.value = GMLReader2.HEXAGON;break;
     case NodeView.OCTAGON:
       type.value = GMLReader2.OCTAGON;break;
     case NodeView.PARALELLOGRAM:
       type.value = GMLReader2.PARALELLOGRAM;break;
     case NodeView.TRIANGLE:
       type.value = GMLReader2.TRIANGLE;break;
     }
   }
   
 
   private void writeGraphEdgeGraphics(CyNetwork network, EdgeView edgeView, List oldList){
     KeyValue width = null,fill = null,line = null,type = null,source_arrow = null,target_arrow = null;
     for(Iterator it = oldList.iterator();it.hasNext();){
       KeyValue keyVal = (KeyValue)it.next();
       if(keyVal.key.equals(GMLReader2.WIDTH)){
 	width = keyVal;
       }else if(keyVal.key.equals(GMLReader2.FILL)){
 	fill = keyVal;
       }else if(keyVal.key.equals(GMLReader2.LINE)){
 	line = keyVal;
       }else if(keyVal.key.equals(GMLReader2.TYPE)){
 	type = keyVal;
       }else if(keyVal.key.equals(GMLReader2.SOURCE_ARROW)){
 	source_arrow = keyVal;
       }else if(keyVal.key.equals(GMLReader2.TARGET_ARROW)){
 	target_arrow = keyVal;
       }
     }
     
     if(width == null){
       width = new KeyValue(GMLReader2.WIDTH,null);
       oldList.add(width);
     }
     width.value = new Double(edgeView.getStrokeWidth());
 
     if(fill == null){
       fill = new KeyValue(GMLReader2.FILL,null);
       oldList.add(fill);
     }
     fill.value = getColorHexString((Color)edgeView.getUnselectedPaint());
 
     if(type == null){
       type = new KeyValue(GMLReader2.TYPE,null);
       oldList.add(type);
     }
     switch(edgeView.getLineType()){
     case EdgeView.STRAIGHT_LINES:
       type.value = GMLReader2.STRAIGHT_LINES;break;
     case EdgeView.CURVED_LINES:
       type.value = GMLReader2.CURVED_LINES;break;
     }
       
     if(line == null){
       line = new KeyValue(GMLReader2.LINE,null);
       oldList.add(line);
     }
     Point2D [] pointsArray = edgeView.getBend().getDrawPoints();
     Vector points = new Vector(pointsArray.length);
    // CTW funny thing with anchor points, need to trim off the first and last
    for(int idx=1;idx<pointsArray.length-1;idx++){
       Vector coords = new Vector(2);
       coords.add(new KeyValue(GMLReader2.X,new Double(pointsArray[idx].getX())));
       coords.add(new KeyValue(GMLReader2.Y,new Double(pointsArray[idx].getY())));
       points.add(new KeyValue(GMLReader2.POINT,coords));
     }
     line.value = points;
     
     if(source_arrow == null){
       source_arrow = new KeyValue(GMLReader2.SOURCE_ARROW,null);
       oldList.add(source_arrow);
     }
     source_arrow.value = new Integer(edgeView.getSourceEdgeEnd());
 
     if(target_arrow == null){
       target_arrow = new KeyValue(GMLReader2.TARGET_ARROW,null);
       oldList.add(target_arrow);
     }
     target_arrow.value = new Integer(edgeView.getTargetEdgeEnd());
           
   }
   
 
 
   
   /**
    * Get the String representation of the 6 character hexidecimal RGB values
    * i.e. #ff000a
    * @param Color The color to be converted
    */
   private static String getColorHexString(Color c) {
     return ("#"//+Integer.toHexString(c.getRGB());
       +Integer.toHexString(256+c.getRed()).substring(1)
       +Integer.toHexString(256+c.getGreen()).substring(1)
       +Integer.toHexString(256+c.getBlue()).substring(1)
       );
   }
 
 
 }
