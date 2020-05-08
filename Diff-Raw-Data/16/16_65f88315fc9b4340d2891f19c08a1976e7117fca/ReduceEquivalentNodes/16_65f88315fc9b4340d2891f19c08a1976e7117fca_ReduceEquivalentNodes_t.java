 package cytoscape.layout;
 
 import y.base.*;
 import y.view.*;
 import y.algo.*;
 
 import cytoscape.*;
 import cytoscape.data.*;
 
 import java.util.Hashtable;
 import java.util.Iterator;
 
 public class ReduceEquivalentNodes {
     GraphObjAttributes nodeAttributes;
     GraphObjAttributes edgeAttributes;
     Graph2D graph;
     GraphHider graphHider;
     NodeMap node2key;
     Hashtable key2nodes;
 
     public ReduceEquivalentNodes(GraphObjAttributes nodeAttributes,
 				 GraphObjAttributes edgeAttributes,
 				 Graph2D graph) {
 	System.out.println("Reducing network");
 	this.nodeAttributes = nodeAttributes;
 	this.edgeAttributes = edgeAttributes;
 	this.graph = graph;
 	this.graphHider = new GraphHider(graph);
 	this.node2key  = graph.createNodeMap();
 	this.key2nodes = new Hashtable();
 
 	computeReduction();  // builds node2key and key2nodes
 	performReduction();  // alters the graph based on this info
 	
 	System.out.println("Reduction Finished");
     }
 
     /************************************************************/
     // main method for this class
     void computeReduction() {
 
 	int i = 0;
 	graphHider.unhideAll();
 	YList listOfNodes;
 	Node n;
 
 	// construct key for each node in graph
 	for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {   
 	    n = nc.node();	
 	    String name = nodeAttributes.getCanonicalName(n);
 	    YList nameTypeList = new YList();
	    //	    System.out.println("Examining Node # " + i++ + " " + name);
 	    
 	    // examine all of this nodes neighbors
 	    for (EdgeCursor ec = n.edges(); ec.ok(); ec.next()) {
 		Edge e = ec.edge();
 		Node n2 = e.opposite(n);
 		String eStr = edgeAttributes.getCanonicalName(e);
 		String type = (String) edgeAttributes.getValue("interaction", eStr);
 		if (type.equals("pd") && (e.target() == n) ) 
 		    type = "dp"; // handle directionality for pd edges
 		String nameType = nodeAttributes.getCanonicalName(n2) + type;
 		nameTypeList.add(nameType);
 	    }
 	    
 	    // convert local topology (nameType string) to key
 	    String key = collapseToString(nameTypeList);
	    //	    System.out.println("  " + key);
 	    node2key.set(n, key);  // record in node map
 
 	    // add this node to hashtable entry for this key
 	    listOfNodes = (YList) key2nodes.get(key);
 	    if (listOfNodes == null) listOfNodes = new YList();
 	    listOfNodes.add(n);
 	    key2nodes.put(key, listOfNodes);
 	}
     }
     /************************************************************/
     // Collapses graph based on key2nodes hash
     void performReduction() {
 	
 	YList listOfNodes;
 	Node n;
 	int i = 0;
 
 	// get equivalent nodes from each hash table entry
 	for (Iterator it = key2nodes.values().iterator(); it.hasNext(); ) {
 	    listOfNodes = (YList) it.next();
 	    if (listOfNodes.size() >= 2) {  // only for clustered nodes
 		String groupString = "Group " + i++;
 		System.out.print(groupString + ": ");
 		// rename first node in list to represent this group
 		YCursor yc = listOfNodes.cursor();
 		n = (Node) yc.current();
 		System.out.print(" " + graph.getLabelText(n));

 		//props.setName(n, groupString);
 		graph.setLabelText(n, groupString);
		nodeAttributes.addNameMapping( groupString, n);

 		// remove other nodes
 		for (yc.next(); yc.ok(); yc.next()) {
 		    n = (Node) yc.current();
 		    System.out.print(" " + graph.getLabelText(n));
 		    graph.removeNode(n);
 		}   
 		System.out.println();
 	    }
 	}
     }
 
     /************************************************************/
     // Collapses a list of strings into a single concatenated string
     String collapseToString(YList list) {
 
 	String collapsed = new String();
 
 	list.sort();  // sort list in alphabetical order
 	for (YCursor yc = list.cursor(); yc.ok(); yc.next()) {
 	    collapsed += yc.current();
 	}
 	return collapsed;
     }
 }
 
