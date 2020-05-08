 package cytoscape.util.unitTests;
 
 import junit.framework.*;
 import java.util.*;
 import java.lang.Object;
 
 import cytoscape.util.GraphSetUtils;
 import cytoscape.unitTests.AllTests;
 import cytoscape.Cytoscape;
 import cytoscape.CytoscapeInit;
 import cytoscape.CyNetwork;
 import cytoscape.data.CyAttributes;
 import cytoscape.giny.CytoscapeRootGraph;
 
 import giny.model.RootGraph;
 
 
 public class GraphSetUtilsTest extends TestCase {
 protected List networklist;
protected int a,b,c,d,e,ab,bc,ac,bd,be,cd;
 protected CyNetwork net1; 
 protected CyNetwork net2;
 	
 public void setUp() throws Exception {
 	networklist = new ArrayList();
 	RootGraph root = Cytoscape.getRootGraph();
 	a = root.createNode();
 	b = root.createNode();
 	c = root.createNode();
 	d = root.createNode();
 	e = root.createNode();
 	
 	int[] nodes1 = new int[] {a,b,c,d};
 	int[] nodes2 = new int[] {b,c,d,e};
 	
 	ab = root.createEdge(a,b,true);
 	bc = root.createEdge(b,c,true);
 	ac = root.createEdge(a,c,true);
 	bd = root.createEdge(b,d,true);
 	be = root.createEdge(b,e,true);
 	cd = root.createEdge(c,d,true);
 	
 	int[] edges1 = new int[] {ab,bc,ac,bd};
 	int[] edges2 = new int[] {bd,bc,be};
 	
 	net1 = Cytoscape.createNetwork(nodes1,edges1,"graph1");
 	net2 = Cytoscape.createNetwork(nodes2,edges2,"graph2");
 	networklist.add(0, net1); 
 	networklist.add(1, net2);
 }
 
 public void testNetwork() {
 
 	//verify created nodes exist
 	assertTrue (net1.containsNode(net1.getNode(a)));
 	assertTrue (net1.containsNode(net1.getNode(b)));
 	assertTrue (net1.containsNode(net1.getNode(c)));
 	assertTrue (net1.containsNode(net1.getNode(d)));
 	
 	assertTrue (net2.containsNode(net2.getNode(b)));
 	assertTrue (net2.containsNode(net2.getNode(c)));
 	assertTrue (net2.containsNode(net2.getNode(d)));
 	assertTrue (net2.containsNode(net2.getNode(e)));
 	
 	assertTrue (net1.containsEdge(net1.getEdge(ab)));
 	assertTrue (net1.containsEdge(net1.getEdge(bc)));
 	assertTrue (net1.containsEdge(net1.getEdge(ac)));
 	assertTrue (net1.containsEdge(net1.getEdge(bd)));
 	
 	assertTrue (net2.containsEdge(net2.getEdge(bd)));
 	assertTrue (net2.containsEdge(net2.getEdge(bc)));
 	assertTrue (net2.containsEdge(net2.getEdge(be)));
 	
 }
 	
 public void tearDown() throws Exception {
 		
 }
 
 //-------------------------------------------------------------------------
 public void testIntersection(){
 
 	CyNetwork n = GraphSetUtils.createIntersectionGraph(networklist, true, "intersect");
 	
 	assertTrue (n.containsEdge(n.getEdge(bc)));
 	assertTrue (n.containsEdge(n.getEdge(bd)));
 	//assertFalse (n.containsEdge(n.getEdge(ab)));
 	assertNull (n.getEdge(ab));
 	assertNull (n.getEdge(be));
 	
 	assertTrue (n.containsNode(n.getNode(b)));
 	assertTrue (n.containsNode(n.getNode(c)));
 	assertTrue (n.containsNode(n.getNode(d)));
 	assertNull (n.getNode(a));
 	assertNull (n.getNode(e));
 	
 }// testIntersection
 //-------------------------------------------------------------------------
 
 public void testDifference(){
 
 	CyNetwork x = GraphSetUtils.createDifferenceGraph(networklist, true, "difference");
 	
 	
 	assertTrue (x.containsNode(x.getNode(a)));
	assertTrue (x.containsNode(x.getNode(b)));
	assertTrue (x.containsNode(x.getNode(c)));
	assertNull (x.getNode(d));
	assertNull (x.getNode(e));
	
	assertTrue (x.containsEdge(x.getEdge(ab)));
	assertNull (x.getEdge(bc));
	assertTrue (x.containsEdge(x.getEdge(ac)));
	assertNull (x.getEdge(be));
	assertNull (x.getEdge(bd));
	
 
 
 }// testDifference
 //-------------------------------------------------------------------------
 public void testUnion(){
 
 	CyNetwork y = GraphSetUtils.createUnionGraph(networklist, true, "union");
 	
 	
 	assertTrue (y.containsEdge(y.getEdge(ab)));
 	assertTrue (y.containsEdge(y.getEdge(bc)));
 	assertTrue (y.containsEdge(y.getEdge(ac)));
 	assertTrue (y.containsEdge(y.getEdge(bd)));
 	
 	assertTrue (y.containsNode(y.getNode(a)));
 	assertTrue (y.containsNode(y.getNode(b)));
 	assertTrue (y.containsNode(y.getNode(c)));
 	assertTrue (y.containsNode(y.getNode(d)));
 	assertTrue (y.containsNode(y.getNode(e)));
 	
 }// testUnion
 //-------------------------------------------------------------------------
 }
 
