 // GMLReader.java
 
 /** Copyright (c) 2002 Institute for Systems Biology and the Whitehead Institute
  **
  ** This library is free software; you can redistribute it and/or modify it
  ** under the terms of the GNU Lesser General Public License as published
  ** by the Free Software Foundation; either version 2.1 of the License, or
  ** any later version.
  ** 
  ** This library is distributed in the hope that it will be useful, but
  ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  ** documentation provided hereunder is on an "as is" basis, and the
  ** Institute for Systems Biology and the Whitehead Institute 
  ** have no obligations to provide maintenance, support,
  ** updates, enhancements or modifications.  In no event shall the
  ** Institute for Systems Biology and the Whitehead Institute 
  ** be liable to any party for direct, indirect, special,
  ** incidental or consequential damages, including lost profits, arising
  ** out of the use of this software and its documentation, even if the
  ** Institute for Systems Biology and the Whitehead Institute 
  ** have been advised of the possibility of such damage.  See
  ** the GNU Lesser General Public License for more details.
  ** 
  ** You should have received a copy of the GNU Lesser General Public License
  ** along with this library; if not, write to the Free Software Foundation,
  ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
  **/
 
 //-------------------------------------------------------------------------------------
 // $Revision$   
 // $Date$ 
 // $Author$
 //-----------------------------------------------------------------------------------
 package cytoscape.data.readers;
 //-----------------------------------------------------------------------------------------
 import java.util.*;
 
 import giny.model.*;
 import giny.view.*;
 import cytoscape.data.CyNetwork;
 import cytoscape.util.GinyFactory;
 
 import cytoscape.data.GraphObjAttributes;
 
 // add text here
 
 //-------------------------------------------------------------------------------------
 /**
  * Reader for graph in GML format. Given the filename provides the graph and
  * attributes objects constructed from the file.
  */
 public class GMLReader implements GraphReader {
   private String filename;
   GraphObjAttributes edgeAttributes;// = new GraphObjAttributes ();
   GraphObjAttributes nodeAttributes;// = new GraphObjAttributes ();
   //GraphObjAttributes gmlGraphicsAtt = new GraphObjAttributes ();
 
   RootGraph rootGraph;
   GMLTree gmlTree;
 
   /**
    * @param filename The GML file to be read in
    */
   public GMLReader ( String filename ) {
     this.filename = filename;
     //read();
   }
 
   /**
    * Equivalent to read(), as names in a GML file are not currently canonicalized.
    */
   public void read ( boolean canonicalize ) {
     read();
   }
 
   
   /**
    * This will read AND create a Graph from the file specified in the constructor
    */
 
   public void read () {
     //move this to later when we know how many nodes
     //edges we are going to add
     //rootGraph = GinyFactory.createRootGraph();
     // create and read the GML file
     gmlTree = new GMLTree(filename);
     nodeAttributes = new GraphObjAttributes();
     edgeAttributes = new GraphObjAttributes();
     //    gmlGraphicsAtt = new GraphObjAttributes();
     Vector nodeIds = gmlTree.getVector("graph|node|id","|",GMLTree.INTEGER);
     Vector nodeLabels = gmlTree.getVector("graph|node|label","|",GMLTree.STRING);
 
     // in case gml node ids are not ordered consecutively (0..n)
     Hashtable nodeNameMap  = new Hashtable(nodeIds.size());
     for(int i=0; i<nodeIds.size(); i++) {
       nodeNameMap.put(nodeIds.get(i), nodeLabels.get(i));
     }
 
     Vector edgeSources = gmlTree.getVector("graph|edge|source","|",GMLTree.INTEGER);
     Vector edgeTargets = gmlTree.getVector("graph|edge|target","|",GMLTree.INTEGER);
     Vector edgeLabels  = gmlTree.getVector("graph|edge|label","|",GMLTree.STRING);
        
     //Use the number of ids to get the number of nodes in hte graph
     //Use the number of source ids to get the number of edges in hte graph
     //(since every edge must have some source node)
     rootGraph = GinyFactory.createRootGraph(nodeIds.size(),edgeSources.size());
     //---------------------------------------------------------------------------
     // set a default edge type if it's not defined in the GML file
     // need a better system for defining the default...
     String et = "pp";
     if(edgeLabels.isEmpty()) {
       for(int i=0; i < edgeSources.size(); i++) {
 	edgeLabels.add(et);
       }
     }
        
     //---------------------------------------------------------------------------
     // loop through all of the nodes (using a hash to avoid duplicates)
     // adding nodes to the rootGraph. 
     // Create the nodeName mapping in nodeAttributes
     //---------------------------------------------------------------------------
     //Hashtable nodeHash = new Hashtable ();
     Hashtable gmlId2GINYId = new Hashtable();
     String nodeName, interactionType;
     rootGraph.createNodes(nodeIds.size());
   
     //for(int i=0; i<nodeIds.size(); i++) {
     for (Iterator nodeIt = rootGraph.nodesIterator(),idIt = nodeIds.iterator();nodeIt.hasNext();) {
       //nodeName = (String) nodeNameMap.get(nodeIds.get(i));
       Integer gmlId = (Integer)idIt.next();
       nodeName = (String)nodeNameMap.get(gmlId);
       //if(canonicalize) nodeName = canonicalizeName(nodeName);	      
       //if (!nodeHash.containsKey(nodeName)) {
       //Node node = rootGraph.getNode(rootGraph.createNode());
       Node node = (Node)nodeIt.next();
       node.setIdentifier(nodeName);
       //nodeHash.put(nodeName, node);
       gmlId2GINYId.put(gmlId,new Integer(node.getRootGraphIndex()));
       nodeAttributes.addNameMapping(nodeName, node);
 	//}
     }
 
     //Vector nodeHeight = gmlTree.getVector("graph|node|graphics|h","|",GMLTree.DOUBLE);
     //Vector nodeWidth  = gmlTree.getVector("graph|node|graphics|w","|",GMLTree.DOUBLE);
     //Vector nodeType   = gmlTree.getVector("graph|node|graphics|type","|",GMLTree.STRING);
     //if( (nodeHeight.size() == nodeIds.size())  &&
     //(nodeWidth.size()  == nodeIds.size()) &&
     //(nodeType.size()   == nodeIds.size())
     //) {
 
     //for(int i=0; i<nodeIds.size(); i++) {
     //nodeName = (String) nodeNameMap.get(nodeIds.get(i));
     //gmlGraphicsAtt.set("node.h", nodeName, (Double) nodeHeight.get(i));
     //gmlGraphicsAtt.set("node.w", nodeName, (Double) nodeWidth.get(i));
     //gmlGraphicsAtt.set("node.type", nodeName, (String) nodeType.get(i));
     //}
     //}
 
     //---------------------------------------------------------------------------
     // loop over the interactions creating edges between all sources and their 
     // respective targets.
     // for each edge, save the source-target pair, and their interaction type,
     // in edgeAttributes -- a hash of a hash of name-value pairs, like this:
     // ???  edgeAttributes ["interaction"] = interactionHash
     // ???  interactionHash [sourceNode::targetNode] = "pd"
     //---------------------------------------------------------------------------
     
    
     int [] sources,targets;
     sources = new int[edgeSources.size()];
     targets = new int[edgeTargets.size()];
     //Vector edgeNames = new Vector(edgeSources.size());
     for (int i=0; i < edgeSources.size(); i++) {
       //Node sourceNode = (Node) nodeHash.get(sourceName);
       //Node targetNode = (Node) nodeHash.get(targetName);
       sources[i] = ((Integer)gmlId2GINYId.get(edgeSources.get(i))).intValue();
       targets[i] = ((Integer)gmlId2GINYId.get(edgeTargets.get(i))).intValue();
       //Edge edge = rootGraph.getEdge(rootGraph.createEdge(sourceNode, targetNode));
       //int edge = rootGraph.createEdge(sourceID,targetID);
       
       //determine the name for the edge
       //sourceName = (String) nodeNameMap.get(edgeSources.get(i));
       //targetName = (String) nodeNameMap.get(edgeTargets.get(i));
       //interactionType = (String) edgeLabels.get(i);
       //edgeName = sourceName + " (" + interactionType + ") " + targetName;
       
  
       //int previousMatchingEntries = edgeAttributes.countIdentical(edgeName);
       //if (previousMatchingEntries > 0)
       //edgeName = edgeName + "_" + previousMatchingEntries;
       //edgeAttributes.add("interaction", edgeName, interactionType);
       //edgeAttributes.addNameMapping(edgeName, edge);
     }
     rootGraph.createEdges(sources,targets,false);
    for ( Iterator edgeIt = rootGraph.edgesList().iterator(),sourceIt = edgeSources.iterator(),targetIt = edgeTargets.iterator(),labelIt = edgeLabels.iterator();edgeIt.hasNext();) {
       interactionType = (String)labelIt.next();
       String edgeName = ""+nodeNameMap.get(sourceIt.next())+" ("+interactionType+") "+nodeNameMap.get(targetIt.next());
       int previousMatchingEntries = edgeAttributes.countIdentical(edgeName);
       if (previousMatchingEntries > 0){
 	edgeName = edgeName + "_" + previousMatchingEntries;
       }
       edgeAttributes.add("interaction", edgeName, interactionType);
       edgeAttributes.addNameMapping(edgeName, edgeIt.next());
     } // end of for ()
     
   } // read
 
   /**
    * @return null, there is no GML reader available outside of Y-Files right now
    */
   public RootGraph getRootGraph () {
     return rootGraph;
   }
 
   /**
    * @return the node attributes that were read in from the GML file.
    */
   public GraphObjAttributes getNodeAttributes () {
     return nodeAttributes;
   }
 
   /**
    * @return the edge attributes that were read in from the GML file.
    */
   public GraphObjAttributes getEdgeAttributes () {
     return edgeAttributes;
   }
   /**
    * @return the gmlGraphics attributes that were read in from the GML file.
    */
   //public GraphObjAttributes getGMLAttributes () {
   //  return gmlGraphicsAtt;
   //}
   //------------------------------------------------------------------------------
   // called only when we have a CyWindow
   //
   // GMLReader.layoutByGML(geometryFilename, cyWindow.getView(), cyWindow.getNetwork());
   //
   //public static void layoutByGML(String geometryFilename, GraphView myView, CyNetwork myNetwork){
   public void layout(GraphView myView){
     
       if(gmlTree == null){
 	throw new RuntimeException("Failed to read gml file on initialization");
       }
       else {
 	Vector nodeLabels  = gmlTree.getVector("graph|node|label","|",GMLTree.STRING);
 	Vector nodeX = gmlTree.getVector("graph|node|graphics|x","|",GMLTree.DOUBLE);
 	Vector nodeY = gmlTree.getVector("graph|node|graphics|y","|",GMLTree.DOUBLE);
 
 	Vector nodeHeight = gmlTree.getVector("graph|node|graphics|h","|",GMLTree.DOUBLE);
 	Vector nodeWidth  = gmlTree.getVector("graph|node|graphics|w","|",GMLTree.DOUBLE);
 	Vector nodeType   = gmlTree.getVector("graph|node|graphics|type","|",GMLTree.STRING);
 	  
 	if( (nodeLabels.size() == myView.getRootGraph().getNodeCount()) &&
 	    (nodeX.size() == myView.getRootGraph().getNodeCount()) &&
 	    (nodeY.size() == myView.getRootGraph().getNodeCount()) ) {
 	      
 	  //Iterator it = nodeLabels.iterator();
 	  //GraphObjAttributes nodeAttributes = myNetwork.getNodeAttributes();
 	  //for(int i=0;i<nodeLabels.size();i++){
 	  String ELLIPSE = "ellipse";
 	  String RECTANGLE = "rectangle";
 	  int i=0;
 	  for (Iterator nodeIt = rootGraph.nodesIterator();nodeIt.hasNext();i++) {
 	    Node current = (Node)nodeIt.next();
 	    String nodeName = (String)nodeLabels.get(i);
 	    if ( !current.getIdentifier().equals(nodeName)) {
 	      throw new RuntimeException("Unexpected node ordering when laying out");
 	    } // end of if ()
 	    Number X = (Number) nodeX.get(i);
 	    Number Y = (Number) nodeY.get(i);
 	    //get the node associated with that node label
 	    //Node current = (Node)nodeAttributes.getGraphObject(nodeName);
 	    NodeView currentView = myView.getNodeView(current);
 	    currentView.setXPosition(X.doubleValue());
 	    currentView.setYPosition(Y.doubleValue());
 	    if( nodeHeight.size() == nodeLabels.size() )
 	      currentView.setHeight( ((Double) nodeHeight.get(i)).doubleValue());
 	    if( nodeWidth.size() == nodeLabels.size() )
 	      currentView.setWidth( ((Double) nodeWidth.get(i)).doubleValue());
 	    if( nodeType.size() == nodeLabels.size() ) {
 	      String nType = (String) nodeType.get(i);
 	      if( nType.equals(ELLIPSE) ) currentView.setShape(NodeView.ELLIPSE);
 	      else if( nType.equals(RECTANGLE) ) currentView.setShape(NodeView.RECTANGLE);		      
 	    }
 	  }
 	}
       }
   }
 }
 
 
 
 
