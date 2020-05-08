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
 import java.awt.geom.Point2D;
 
 import giny.model.*;
 import giny.view.*;
 import cytoscape.*;
 import cytoscape.util.GinyFactory;
 
 import cytoscape.data.GraphObjAttributes;
 import cern.colt.list.IntArrayList;
 // add text here
 
 //-------------------------------------------------------------------------------------
 /**
  * Reader for graph in GML format. Given the filename provides the graph and
  * attributes objects constructed from the file.
  */
 public class GMLReader implements GraphReader {
   private String filename;
  
   GMLTree gmlTree;
 
   IntArrayList nodes;
   int[] edge_indices_array;
 
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
     //Cytoscape.getRootGraph() = GinyFactory.createRootGraph();
     // create and read the GML file
     gmlTree = new GMLTree(filename);
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
     Cytoscape.ensureCapacity( nodeIds.size(), edgeSources.size() );
     nodes = new IntArrayList( nodeIds.size() );
    
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
     // adding nodes to the Cytoscape.getRootGraph(). 
     // Create the nodeName mapping in Cytoscape.getNodeNetworkData()
     //---------------------------------------------------------------------------
     //Hashtable nodeHash = new Hashtable ();
     Hashtable gmlId2GINYId = new Hashtable();
     String nodeName, interactionType;
     Cytoscape.getRootGraph().createNodes(nodeIds.size());
   
     //System.out.println( "GML: "+nodeIds.size()+" nodes created"+ " RG: "+Cytoscape.getRootGraph().nodesList().size() );
     
 
     //for(int i=0; i<nodeIds.size(); i++) {
     for (Iterator nodeIt = Cytoscape.getRootGraph().nodesIterator(),idIt = nodeIds.iterator();nodeIt.hasNext();) {
       //nodeName = (String) nodeNameMap.get(nodeIds.get(i));
       Integer gmlId = (Integer)idIt.next();
       nodeName = (String)nodeNameMap.get(gmlId);
       //if(canonicalize) nodeName = canonicalizeName(nodeName);	      
       //if (!nodeHash.containsKey(nodeName)) {
       //Node node = Cytoscape.getRootGraph().getNode(Cytoscape.getRootGraph().createNode());
       Node node = (Node)nodeIt.next();
       nodes.add( node.getRootGraphIndex() );
       node.setIdentifier(nodeName);
       //nodeHash.put(nodeName, node);
       gmlId2GINYId.put(gmlId,new Integer(node.getRootGraphIndex()));
       Cytoscape.getNodeNetworkData().addNameMapping(nodeName, node);
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
     // in Cytoscape.getEdgeNetworkData() -- a hash of a hash of name-value pairs, like this:
     // ???  Cytoscape.getEdgeNetworkData() ["interaction"] = interactionHash
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
       //Edge edge = Cytoscape.getRootGraph().getEdge(Cytoscape.getRootGraph().createEdge(sourceNode, targetNode));
       //int edge = Cytoscape.getRootGraph().createEdge(sourceID,targetID);
       
       //determine the name for the edge
       //sourceName = (String) nodeNameMap.get(edgeSources.get(i));
       //targetName = (String) nodeNameMap.get(edgeTargets.get(i));
       //interactionType = (String) edgeLabels.get(i);
       //edgeName = sourceName + " (" + interactionType + ") " + targetName;
       
  
       //int previousMatchingEntries = Cytoscape.getEdgeNetworkData().countIdentical(edgeName);
       //if (previousMatchingEntries > 0)
       //edgeName = edgeName + "_" + previousMatchingEntries;
       //Cytoscape.getEdgeNetworkData().add("interaction", edgeName, interactionType);
       //Cytoscape.getEdgeNetworkData().addNameMapping(edgeName, edge);
     }

     edge_indices_array = Cytoscape.getRootGraph().createEdges(sources,targets,false);
     for ( Iterator edgeIt = Cytoscape.getRootGraph().edgesList().iterator(),sourceIt = edgeSources.iterator(),targetIt = edgeTargets.iterator(),labelIt = edgeLabels.iterator();edgeIt.hasNext();) {

       interactionType = (String)labelIt.next();
       String edgeName = ""+nodeNameMap.get(sourceIt.next())+" ("+interactionType+") "+nodeNameMap.get(targetIt.next());
       int previousMatchingEntries = Cytoscape.getEdgeNetworkData().countIdentical(edgeName);
       if (previousMatchingEntries > 0){
 	edgeName = edgeName + "_" + previousMatchingEntries;
       }
       Cytoscape.getEdgeNetworkData().add("interaction", edgeName, interactionType);
       Cytoscape.getEdgeNetworkData().addNameMapping(edgeName, edgeIt.next());
     } // end of for ()
     
   } // read
 
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
 	  //GraphObjAttributes Cytoscape.getNodeNetworkData() = myNetwork.getNodeAttributes();
 	  //for(int i=0;i<nodeLabels.size();i++){
 	  String ELLIPSE = "ellipse";
 	  String RECTANGLE = "rectangle";

 	  int i=0;
 	  for (Iterator nodeIt = Cytoscape.getRootGraph().nodesIterator();nodeIt.hasNext();i++) {

 	    Node current = (Node)nodeIt.next();
 	    String nodeName = (String)nodeLabels.get(i);
 	    if ( !current.getIdentifier().equals(nodeName)) {
 	      throw new RuntimeException("Unexpected node ordering when laying out");
 	    } // end of if ()
 	    Number X = (Number) nodeX.get(i);
 	    Number Y = (Number) nodeY.get(i);
 	    //get the node associated with that node label
 	    //Node current = (Node)Cytoscape.getNodeNetworkData().getGraphObject(nodeName);
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
         // vector of GMLTrees rooted at "edge" 
 	Vector edgeGML  = gmlTree.getVector("graph|edge","|",GMLTree.GMLTREE);
 
 	if( edgeGML.size() == myView.getRootGraph().getEdgeCount() ) {
 
 	    // ASSUMING THE ORDER IN THE GML IS THE SAME AS THAT RETURNED BY THE ITERATOR!!!!
 	    int i = 0;
	    for (Iterator edgeIt = Cytoscape.getRootGraph().edgesIterator(); edgeIt.hasNext(); i++) {
 		Edge current = (Edge)edgeIt.next();
 		GMLTree tmpGML = (GMLTree)edgeGML.get(i);
 		Vector tmpLine = tmpGML.getVector("graphics|Line", "|", GMLTree.GMLTREE);
 		if(tmpLine.size()==1) {
 		    GMLTree Line = (GMLTree)tmpLine.get(0); 
 		    EdgeView currentView = myView.getEdgeView(current);
 		    currentView.getBend().removeAllHandles();
 		    //currentView.getBend().setHandles(); // we want to do this
 		    Vector tmpXs = Line.getVector("point|x", "|",GMLTree.DOUBLE);
 		    Vector tmpYs = Line.getVector("point|y", "|",GMLTree.DOUBLE);
 		    // Edge Handles are reversed for some reason??
 		    for(int j=tmpXs.size()-2; j>0; j--) {
 			Point2D.Double pt = new Point2D.Double(((Double)tmpXs.get(j)).doubleValue(), ((Double)tmpYs.get(j)).doubleValue());
 			currentView.getBend().addHandle( pt );
 		    }
 		} // END if(tmpLine.size()==1)
 	    } // END for(Iterator...
 	} // END if( edgeGML.size()...
       }
   }
 
 
   public int[] getNodeIndicesArray() {
     nodes.trimToSize();
     //System.out.println( "GML: nodes array size: "+nodes.size() );
     return nodes.elements();
   }
 
   public int[] getEdgeIndicesArray() {
     //System.out.println( "GML: edges array size: "+edge_indices_array.length );
     return edge_indices_array;
   }
 
 }
 
 
 
 
