 package cytoscape.data.readers;
 //-----------------------------------------------------------------------------------------
 import java.util.*;
 import java.awt.geom.Point2D;
 
 import giny.model.*;
 import giny.view.*;
 import cytoscape.*;
 import cytoscape.util.GinyFactory;
 import cytoscape.data.Semantics;
 
 import java.awt.Color;
 import cytoscape.data.GraphObjAttributes;
 import cern.colt.list.IntArrayList;
 import cern.colt.map.OpenIntIntHashMap;
 import java.io.StringWriter;
 import java.io.IOException;
 import java.text.ParseException;
 
 /**
  * This class is responsible for converting a gml object tree into cytoscape objects
  */
 public class GMLReader2 implements GraphReader {
   /**
    * The following are all taken to be reserved keywords
    * for gml (note that not all of them are actually 
    * keywords according ot the spec
    */
   protected static String GRAPH = "graph";
   protected static String NODE = "node";
   protected static String EDGE = "edge";
   protected static String GRAPHICS = "graphics";
   protected static String LABEL = "label";
   protected static String SOURCE = "source";
   protected static String TARGET = "target";
   protected static String X = "x";
   protected static String Y = "y";
   protected static String H = "h";
   protected static String W = "w";
   protected static String TYPE = "type";
   protected static String ID = "id";
   protected static String RECTANGLE = "rectangle";
   protected static String ELLIPSE = "ellipse";
   protected static String ROOT_INDEX = "root_index";
   protected static String LINE = "Line";
   protected static String POINT = "point";
   protected static String DIAMOND = "diamond";
   protected static String HEXAGON = "hexagon";
   protected static String OCTAGON = "octagon";
  protected static String PARALELLOGRAM = "parallelogram";
   protected static String TRIANGLE = "triangle";
   protected static String VERSION = "Version";
   protected static String CREATOR = "Creator";
   protected static String FILL = "fill";
   protected static String WIDTH = "width";
   protected static String STRAIGHT_LINES = "line";
   protected static String CURVED_LINES = "curved";
   protected static String SOURCE_ARROW = "source_arrow";
   protected static String TARGET_ARROW = "target_arrow";
   protected static String OUTLINE = "outline";
   protected static String OUTLINE_WIDTH = "outline_width";
   protected static String DEFAULT_EDGE_INTERACTION = "pp";
 
   String filename;
   List keyVals;
   OpenIntIntHashMap nodeIDMap;
   IntArrayList nodes,sources,targets;
   Vector node_labels,edge_labels,edge_root_index_pairs,node_root_index_pairs;
   IntArrayList giny_nodes,giny_edges;
   
   public GMLReader2 ( String filename ) {
     this.filename = filename;
   }
 
   public void read( boolean canonicalize ){
     read();
   }
 
   public void read(){
     try{
       keyVals = (new GMLParser(filename)).parseList();
     }catch(IOException io){
       throw new RuntimeException(io.getMessage());
     }catch(ParseException p){
       throw new RuntimeException(p.getMessage());
     }
     initializeStructures();
     readGML(keyVals);
     createGraph();
     releaseStructures();
   }
 
   /**
    * Returns a list containing the gml object tree
    */
   public List getList(){
     return keyVals;
   }
 
   protected void initializeStructures(){
     nodes = new IntArrayList();
     sources = new IntArrayList();
     targets = new IntArrayList();
     node_labels = new Vector();
     edge_labels = new Vector();
     edge_root_index_pairs = new Vector();
     node_root_index_pairs = new Vector();
   }
 
   protected void releaseStructures(){
     nodes = null;
     sources = null;
     targets = null;
     node_labels = null;
     edge_labels = null;
     edge_root_index_pairs = null;
     node_root_index_pairs = null;
   }
 
 
   /**
    * This will create the graph model objects. This function expects node labels
    * to be unique and edge labels to be unique between a particular source and target
    * If this condition is not met, an error will be printed to the console, and the object
    * will be skipped. That is, it is as though that particular object never existed
    * in the gml file. If an edge depends on a node that was skipped, then that edge
    * will be skipped as well.
    */
   protected void createGraph(){
     Cytoscape.ensureCapacity(nodes.size(),sources.size());
     nodeIDMap = new OpenIntIntHashMap(nodes.size());
     giny_nodes = new IntArrayList(nodes.size());
     OpenIntIntHashMap gml_id2order = new OpenIntIntHashMap(nodes.size());
     Set nodeNameSet = new HashSet(nodes.size());
     for(int idx=0;idx<nodes.size();idx++){
       String label = (String)node_labels.get(idx);
       if(nodeNameSet.add(label)){
 	Node node = (Node)Cytoscape.getCyNode(label,true);
 	giny_nodes.add(node.getRootGraphIndex());
 	nodeIDMap.put(nodes.get(idx),node.getRootGraphIndex());
 	gml_id2order.put(nodes.get(idx),idx);
 	((KeyValue)node_root_index_pairs.get(idx)).value = (new Integer(node.getRootGraphIndex()));
       }
       else{
 	throw new GMLException("GML id "+nodes.get(idx)+" has a duplicated label: "+label);
 	//((KeyValue)node_root_index_pairs.get(idx)).value = null;
       }
     }
     nodeNameSet = null;
 
     giny_edges = new IntArrayList(sources.size());
     Set edgeNameSet = new HashSet(sources.size());
     GraphObjAttributes edgeAttributes = Cytoscape.getEdgeNetworkData();
     RootGraph rootGraph = Cytoscape.getRootGraph();
     for(int idx=0;idx<sources.size();idx++){
       if(gml_id2order.containsKey(sources.get(idx)) && gml_id2order.containsKey(targets.get(idx))){
 	String label = (String)edge_labels.get(idx);
 	String sourceName = (String)node_labels.get(gml_id2order.get(sources.get(idx)));
 	String targetName = (String)node_labels.get(gml_id2order.get(targets.get(idx)));
 	String edgeName = sourceName + " ("+label+") "+targetName;
 	if(edgeNameSet.add(edgeName)){
 	  Edge edge = (Edge)Cytoscape.getEdgeNetworkData().getGraphObject(edgeName);
 	  if(edge == null){
 	    Node node_1 = Cytoscape.getCyNode(sourceName);
 	    Node node_2 = Cytoscape.getCyNode(targetName);
 	    edge =  (Edge)rootGraph.getEdge( rootGraph.createEdge (node_1, node_2));
 	    edgeAttributes.add (Semantics.INTERACTION, edgeName, label);
 	    edgeAttributes.addNameMapping (edgeName, edge);
 	  }
 	  //Edge edge = (Edge)Cytoscape.getCyEdge(sourceName,
 	  //				edgeName,
 	  //				targetName,
 	  //				label);
 	  giny_edges.add(edge.getRootGraphIndex());
 	  ((KeyValue)edge_root_index_pairs.get(idx)).value = (new Integer(edge.getRootGraphIndex()));
 	}
 	else{
 	  throw new GMLException("Edges between the same nodes must have unique types: duplicate is between "+sourceName+" and "+targetName);
 	  //((KeyValue)edge_root_index_pairs.get(idx)).value = null;
 	}
       }
       else{
 	throw new GMLException("Non-existant source/target node for edge with gml (source,target): "+sources.get(idx)+","+targets.get(idx));
 	//((KeyValue)edge_root_index_pairs.get(idx)).value = null;
       }
     }
     edgeNameSet = null;
     
   }
 
   /**
    * This function takes the root level list
    * which defines a gml objec tree
    */
   protected void readGML(List list){
     for(Iterator it = list.iterator();it.hasNext();){
       KeyValue keyVal = (KeyValue)it.next();
       if(keyVal.key.equals(GRAPH)){
 	readGraph((List)keyVal.value);
       }
     }
   }
 
   /**
    * This function takes in a list which was given
    * as the value to a "graph" key underneath
    * the main gml list
    */
   protected void readGraph(List list){
     for(Iterator it = list.iterator();it.hasNext();){
       KeyValue keyVal = (KeyValue)it.next();
       if(keyVal.key.equals(NODE)){
 	readNode((List)keyVal.value);
       }
       if(keyVal.key.equals(EDGE)){
 	readEdge((List)keyVal.value);
       }
     }
   }
 
   /**
    * This will extract the model information
    * from the list which is matched
    * a "node" key
    */
   protected void readNode(List list){
     String label = "";
     boolean contains_id = false;
     int id = 0;
     KeyValue root_index_pair = null;
     for(Iterator it = list.iterator();it.hasNext();){
       KeyValue keyVal = (KeyValue)it.next();
       if(keyVal.key.equals(ID)){
 	contains_id = true;
 	id = ((Integer)keyVal.value).intValue();
       }else if(keyVal.key.equals(LABEL)){
 	label = (String)keyVal.value;
       }else if(keyVal.key.equals(ROOT_INDEX)){
 	root_index_pair = keyVal;
       }
     }
  
     if(label.equals("")){
       label = String.valueOf(id);
     }
     if(root_index_pair == null){
       root_index_pair = new KeyValue(ROOT_INDEX,null);
       list.add(root_index_pair);
     }
     if(!contains_id){
       StringWriter stringWriter = new StringWriter();
       try{
 	GMLParser.printList(list,stringWriter);
       }
       catch(Exception e){
 	throw new RuntimeException(e.getMessage());
       }
       throw new GMLException("The node-associated list\n"+stringWriter+"is missing an id field");
     }
     else{
       node_root_index_pairs.add(root_index_pair);
       nodes.add(id);
       node_labels.add(label);
     }
   }
   
 
   /**
    * This will extract the model information
    * from the list which is matched
    * to an "edge" key.
    */
   protected void readEdge(List list){
     String label = DEFAULT_EDGE_INTERACTION;
     boolean contains_source = false, contains_target = false;
     int source = 0,target = 0;
     KeyValue root_index_pair = null;
     for(Iterator it = list.iterator();it.hasNext();){
       KeyValue keyVal = (KeyValue)it.next();
       if(keyVal.key.equals(SOURCE)){
 	contains_source = true;
 	source = ((Integer)keyVal.value).intValue();
       }
       else if(keyVal.key.equals(TARGET)){
 	contains_target = true;
 	target = ((Integer)keyVal.value).intValue();
       }
       else if(keyVal.key.equals(LABEL)){
 	label = (String)keyVal.value;
       }
       else if(keyVal.key.equals(ROOT_INDEX)){
 	root_index_pair = keyVal;
       }
     }
     
     if(root_index_pair == null){
       root_index_pair = new KeyValue(ROOT_INDEX,null);
       list.add(root_index_pair);
     }
     if(!contains_source || !contains_target){
       StringWriter stringWriter = new StringWriter();
       try{
 	GMLParser.printList(list,stringWriter);
       }catch(Exception e){
 	throw new RuntimeException(e.getMessage());
       }
       throw new GMLException("The edge-associated list\n"+stringWriter+" is missing a source or target key");
     }
     else{
       sources.add(source);
       targets.add(target);
       edge_labels.add(label);
       edge_root_index_pairs.add(root_index_pair);
     }
   }
 
   public void layout(GraphView myView){
     if( myView == null || myView.nodeCount() == 0){
       return;
     }
     if(keyVals == null){
       throw new RuntimeException("Failed to read gml file on initialization");
     }
     for(Iterator it = keyVals.iterator();it.hasNext();){
       KeyValue keyVal = (KeyValue)it.next();
       if(keyVal.key.equals(GRAPH)){
 	layoutGraph(myView,(List)keyVal.value);
       }
     }
       
   }
 
   protected void layoutGraph(GraphView myView, List list){
     for(Iterator it = list.iterator();it.hasNext();){
       KeyValue keyVal = (KeyValue)it.next();
       if(keyVal.key.equals(NODE)){
 	layoutNode(myView, (List)keyVal.value);
       }else if(keyVal.key.equals(EDGE)){
 	layoutEdge(myView,(List)keyVal.value);
       }
     }
   }
 
   /**
    * Assign node properties based on the values in the list
    * matched to the "node" key. Mostly just a wrapper
    * around layoutNodeGraphics
    */
   protected void layoutNode(GraphView myView, List list){
     Integer root_index = null;
     List graphics_list = null;
     String label = null;
     NodeView view = null;
     for(Iterator it = list.iterator();it.hasNext();){
       KeyValue keyVal = (KeyValue)it.next();
       if(keyVal.key.equals(ROOT_INDEX)){
 	/*
 	 * For some reason we didn't make an object for this node
 	 * give up now
 	 */
 	if(keyVal.value == null){
 	  return;
 	}
 	root_index = (Integer)keyVal.value;
       }else if(keyVal.key.equals(GRAPHICS)){
 	graphics_list = (List)keyVal.value;
       }else if(keyVal.key.equals(LABEL)){
 	label = (String)keyVal.value;
       }
     }
     
     view = myView.getNodeView(root_index.intValue());
     if(label != null){
       view.getLabel().setText(label);
     }
     if(graphics_list != null){
       layoutNodeGraphics(myView,graphics_list,view);
     }
     
   }
 
   /**
    * This will assign node graphic properties based on the values in the
    * list matches to the "graphics" key word
    */
   protected void layoutNodeGraphics(GraphView myView, List list, NodeView nodeView){
     for(Iterator it = list.iterator();it.hasNext();){
       KeyValue keyVal = (KeyValue)it.next();
       if(keyVal.key.equals(X)){
 	nodeView.setXPosition(((Number)keyVal.value).doubleValue());
       }else if(keyVal.key.equals(Y)){
 	nodeView.setYPosition(((Number)keyVal.value).doubleValue());
       }else if(keyVal.key.equals(H)){
 	nodeView.setHeight(((Number)keyVal.value).doubleValue());
       }else if(keyVal.key.equals(W)){
 	nodeView.setWidth(((Number)keyVal.value).doubleValue());
       }else if(keyVal.key.equals(FILL)){
 	nodeView.setUnselectedPaint(getColor((String)keyVal.value));
       }else if(keyVal.key.equals(OUTLINE)){
 	nodeView.setBorderPaint(getColor((String)keyVal.value));
       }else if(keyVal.key.equals(OUTLINE_WIDTH)){
 	nodeView.setBorderWidth(((Number)keyVal.value).floatValue());
       }else if(keyVal.key.equals(TYPE)){
 	String type = (String)keyVal.value;
 	if(type.equals(ELLIPSE)){
 	  nodeView.setShape(NodeView.ELLIPSE);
 	}else if(type.equals(RECTANGLE)){
 	  nodeView.setShape(NodeView.RECTANGLE);
 	}else if(type.equals(DIAMOND)){
 	  nodeView.setShape(NodeView.DIAMOND);
 	}else if(type.equals(HEXAGON)){
 	  nodeView.setShape(NodeView.HEXAGON);
 	}else if(type.equals(OCTAGON)){
 	  nodeView.setShape(NodeView.OCTAGON);
 	}else if(type.equals(PARALELLOGRAM)){
 	  nodeView.setShape(NodeView.PARALELLOGRAM);
 	}else if(type.equals(TRIANGLE)){
 	  nodeView.setShape(NodeView.TRIANGLE);
 	}
       }
     }
   }
 
   /**
    * Assign edge visual properties based on pairs in the 
    * list matched to the "edge" key world
    */
   protected void layoutEdge(GraphView myView, List list){
     EdgeView edgeView = null;
     List graphics_list = null;
     for(Iterator it = list.iterator();it.hasNext();){
       KeyValue keyVal = (KeyValue)it.next();
       if(keyVal.key.equals(ROOT_INDEX)){
 	/*
 	 * Previously, we didn't make an object for this edge for
 	 * some reason. Don't try to go any further.
 	 */
 	if(keyVal.value == null){
 	  return;
 	}
 	edgeView = myView.getEdgeView(((Integer)keyVal.value).intValue());
       }else if(keyVal.key.equals(GRAPHICS)){
 	graphics_list = (List)keyVal.value;
       }
     }
     
     if(edgeView != null && graphics_list != null){
       layoutEdgeGraphics(myView,graphics_list,edgeView);
     }
   }
 
 
   /**
    * Assign edge graphics properties
    */
   protected void layoutEdgeGraphics(GraphView myView, List list, EdgeView edgeView){
     for(Iterator it = list.iterator();it.hasNext();){
       KeyValue keyVal = (KeyValue)it.next();
       if(keyVal.key.equals(LINE)){
 	layoutEdgeGraphicsLine(myView,(List)keyVal.value,edgeView);
       }else if(keyVal.value.equals(WIDTH)){
 	edgeView.setStrokeWidth(((Number)keyVal.value).floatValue());
       }else if(keyVal.value.equals(FILL)){
 	edgeView.setUnselectedPaint(getColor((String)keyVal.value));
       }
       else if(keyVal.value.equals(TYPE)){
 	String value = (String)keyVal.value;
 	if(value.equals(STRAIGHT_LINES)){
 	  edgeView.setLineType(EdgeView.STRAIGHT_LINES);
 	}else if(value.equals(CURVED_LINES)){
 	  edgeView.setLineType(EdgeView.CURVED_LINES);
 	}
       }else if(keyVal.value.equals(SOURCE_ARROW)){
 	edgeView.setSourceEdgeEnd(((Number)keyVal.value).intValue());
       }else if(keyVal.value.equals(TARGET_ARROW)){
 	edgeView.setTargetEdgeEnd(((Number)keyVal.value).intValue());
       }
     }
   }
 
   /**
    * Assign bend points based on the contents of the list associated with a "Line" key
    * We make sure that there is both an x,y present in the underlying point list
    * before trying to generate a bend point
    */
   protected void layoutEdgeGraphicsLine(GraphView myView, List list, EdgeView edgeView){
     for(Iterator it = list.iterator();it.hasNext();){
       KeyValue keyVal = (KeyValue)it.next();
       if(keyVal.key.equals(POINT)){
 	Number x=null,y=null;
 	for(Iterator pointIt = ((List)keyVal.value).iterator();pointIt.hasNext();){
 	  KeyValue pointVal = (KeyValue)pointIt.next();
 	  if(pointVal.key.equals(X)){
 	    x = (Number)pointVal.value;
 	  }else if(pointVal.key.equals(Y)){
 	    y = (Number)pointVal.value;
 	  }
 	}
 	if(!(x == null || y == null)){
 	  Point2D.Double pt = new Point2D.Double(x.doubleValue(),y.doubleValue());
 	  edgeView.getBend().addHandle(pt);
 	}
       }
     }
   }
 
   /**
    * Part of interface contract
    */
   public int [] getNodeIndicesArray(){
     giny_nodes.trimToSize();
     return giny_nodes.elements();
   }
 
   /**
    * Part of interace contract
    */
   public int [] getEdgeIndicesArray(){
     giny_edges.trimToSize();
     return giny_edges.elements();
   }
 
   
   /**
    * @return null, there is no GML reader available outside of Y-Files right now
    */
   public RootGraph getRootGraph () {
     return Cytoscape.getRootGraph();
   }
 
   /**
    * @return the node attributes that were read in from the GML file.
    */
   public GraphObjAttributes getNodeAttributes () {
     return Cytoscape.getNodeNetworkData();
   }
 
   /**
    * @return the edge attributes that were read in from the GML file.
    */
   public GraphObjAttributes getEdgeAttributes () {
     return Cytoscape.getEdgeNetworkData();
   }
 
   /**
    * Create a color object from the string like
    * it is stored in a gml file
    */
   public Color getColor(String colorString){
     //int red = Integer.parseInt(colorString.substring(1,3),16);
     //int green = Integer.parseInt(colorString.substring(3,5),16);
     //int blue = Integer.parseInt(colorString.substring(5,7),16);
     return new Color(Integer.parseInt(colorString.substring(1),16));
   }
 }
