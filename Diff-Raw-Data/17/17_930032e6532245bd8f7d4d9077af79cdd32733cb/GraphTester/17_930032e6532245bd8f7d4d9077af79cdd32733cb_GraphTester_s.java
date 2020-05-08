 package tests;
 
 import vitro.*;
 import vitro.graph.*;
 import java.util.*;
 
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 public class GraphTester {
 	
 	@Test
 	public void testBuild() {
 		Graph g = new Graph();
 		Node v1 = g.createNode();
 		Node v2 = g.createNode();
 		Node v3 = g.createNode();
 		
 		Edge e1 = g.createEdge(v1,v2);
 		Edge e2 = g.createEdge(v2,v3);
 		
 		assertTrue(g.nodes.contains(v1));
 		assertTrue(g.nodes.contains(v2));
 		assertTrue(g.nodes.contains(v3));
 		assertTrue(g.nodes.size() == 3);
 		
 		assertTrue(g.edges.contains(e1));
 		assertTrue(g.edges.contains(e2));
 		assertTrue(g.edges.size() == 2);
 		
 		assertTrue(v1.edges.contains(e1));
 		assertTrue(v1.edges.size() == 1);
 		assertTrue(v2.edges.contains(e2));
 		assertTrue(v2.edges.size() == 1);
 
 		boolean check1 = false;
 		try { g.createEdge(v1, null); }
 		catch(IllegalArgumentException e) { check1 = true; }
 		assertTrue(check1);
 
 		boolean check2 = false;
 		Node other = new Graph().createNode();
 		try { g.createEdge(v1, other); }
 		catch(IllegalArgumentException e) { check2 = true; }
 		assertTrue(check2);
 	}
 	
 	@Test
 	public void testAddActor() {
 		Graph g = new Graph();
 		Node v1 = g.createNode();
 		Node v2 = g.createNode();
 		Node v3 = g.createNode();
 		
 		Actor a = new Actor();
 		Actor b = new Actor();
 		
 		assertFalse(g.actors.contains(a));
 
 		g.actors.add(a);
 
 		assertTrue(g.actors.contains(a));		
 		assertTrue(g.getLocation(a) == null);
 		
 		assertFalse(g.actors.contains(b));
 		assertFalse(v2.actors.contains(b));
 
 		v2.actors.add(b);
 
 		assertTrue(g.actors.contains(b));
 		assertTrue(v2.actors.contains(b));
 		assertTrue(g.getLocation(b) == v2);
 	}
 	
 	@Test
 	public void testRemoveActor() {
 		Graph g = new Graph();
 		Node v1 = g.createNode();
 		Node v2 = g.createNode();
 		Node v3 = g.createNode();
 		
 		Actor a = new Actor();
 		
 		v2.actors.add(a);
 		assertTrue(g.actors.contains(a));
 		assertTrue(v2.actors.contains(a));
 		
 		v2.actors.remove(a);
 		assertFalse(g.actors.contains(a));
 		assertFalse(v2.actors.contains(a));
 	}
 	
 	@Test
 	public void testMoveActor() {
 		Graph g = new Graph();
 		Node v1 = g.createNode();
 		Node v2 = g.createNode();
 		Node v3 = g.createNode();
 		
 		Actor a = new Actor();
 		
 		v2.actors.add(a);
 		assertTrue(g.actors.contains(a));
 		assertTrue(v2.actors.contains(a));
 		
 		v1.actors.add(a);
 		assertTrue(g.actors.contains(a));
 		assertTrue(v1.actors.contains(a));
 		assertFalse(v2.actors.contains(a));
 
 		g.actors.add(a);
 		assertTrue(g.actors.size() == 1);
 	}
 
 	@Test
 	public void testReachable() {
 		Graph g = new Graph();
 		Node v1 = g.createNode();
 		Node v2 = g.createNode();
 		Node v3 = g.createNode();
 		Node v4 = g.createNode();
 		Node v5 = g.createNode();
 		Node v6 = g.createNode();
 		Node v7 = g.createNode();
 		
 		g.createEdge(v2, v3);
 		g.createEdge(v3, v2);
 		
 		g.createEdge(v4, v5);
 		g.createEdge(v5, v6);
 		g.createEdge(v6, v7);
 		
 		// isolated vertex
 		Set<Node> a = v1.reachable();
 		assertTrue(a.equals(new HashSet<Node>(Arrays.asList(v1))));
 		
 		// cyclic vertices
 		Set<Node> b = v2.reachable();
 		assertTrue(b.equals(new HashSet<Node>(Arrays.asList(v2,v3))));
 		
 		// chained vertices
 		Set<Node> c = v4.reachable();
 		assertTrue(c.equals(new HashSet<Node>(Arrays.asList(v4,v5,v6,v7))));
 	}
 
 	@Test
 	public void testReachableActors() {
 		Graph g = new Graph();
 		Node v1 = g.createNode();
 		Node v2 = g.createNode();
 		Node v3 = g.createNode();
 		Node v4 = g.createNode();
 		
 		g.createEdge(v2, v3);
 		g.createEdge(v3, v4);
 		
 		Actor a1 = new Actor();
 		Actor a2 = new Actor();
 		
 		v1.actors.add(a1);
 		v4.actors.add(a2);
 		
 		Set<Actor> a = v1.reachableActors();
 		assertTrue(a.size() == 1);
 		assertTrue(a.contains(a1));
 		
 		Set<Actor> b = v2.reachableActors();
 		assertTrue(b.size() == 1);
 		assertTrue(b.contains(a2));
 	}
 	
 	@Test
 	public void testPath() {
 		Graph g = new Graph();
 		Node v1 = g.createNode();
 		Node v2 = g.createNode();
 		Node v3 = g.createNode();
 		Node v4 = g.createNode();
 		Node v5 = g.createNode();
 		
 		Edge e1 = g.createEdge(v1, v2);
 		Edge e2 = g.createEdge(v1, v3);
 		Edge e3 = g.createEdge(v3, v4);
 		Edge e4 = g.createEdge(v3, v2);
 		Edge e5 = g.createEdge(v4, v5);
 		
 		List<Edge> a = v1.path(v5);
 		assertTrue(a.equals(Arrays.asList(e2, e3, e5)));
 		
 		List<Edge> b = v1.path(v1);
 		assertTrue(b.equals(new ArrayList<Edge>()));
 		
 		List<Node> c = v1.pathNodes(v1);
 		assertTrue(c.equals(Arrays.asList(v1)));
 		
 		List<Edge> d = v5.path(v1);
 		assertTrue(d == null);
 		
 		List<Node> e = v1.pathNodes(v5);
 		assertTrue(e.equals(Arrays.asList(v1, v3, v4, v5)));
 	}
 
 	@Test
 	public void testRemoveNodes() {
 		Graph g = new Graph();
 		Node v1 = g.createNode();
 		Node v2 = g.createNode();
 		Node v3 = g.createNode();
 		
 		Edge e1 = g.createEdge(v1, v2);
 		Edge e2 = g.createEdge(v2, v3);
 		Edge e3 = g.createEdge(v3, v1);
 
 		assertTrue(g.edges.size() == 3);
 
 		g.nodes.remove(v1);
 
 		assertTrue(g.edges.contains(e2));
 		assertTrue(g.edges.size() == 1);
 	}
 	
 	@Test
 	public void testRemoveEdges() {
 		Graph g = new Graph();
 		Node v1 = g.createNode();
 		Node v2 = g.createNode();
 		Node v3 = g.createNode();
 		
 		Edge e1 = g.createEdge(v1, v2);
 		Edge e2 = g.createEdge(v2, v3);
 		Edge e3 = g.createEdge(v3, v1);
 
 		assertTrue(g.edges.size() == 3);
 
 		v2.edges.remove(e2);
 
 		assertTrue(g.edges.contains(e1));
 		assertTrue(g.edges.contains(e3));
 		assertTrue(g.edges.size() == 2);
 
 		g.edges.remove(e1);
 		assertTrue(v1.edges.size() == 0);
 	}
 
 	@Test
 	public void testAddEdges() {
 		Graph g = new Graph();
 		Node v1 = g.createNode();
 		Node v2 = g.createNode();
 		Edge e1 = g.createEdge(v1, v2);
 
 		g.edges.remove(e1);
 
 		assertTrue(g.edges.size() == 0);
 
 		g.edges.add(e1);
 
 		assertTrue(v1.edges.contains(e1));
 		assertTrue(v1.edges.size() == 1);
 
 		g.nodes.remove(v2);
 
 		assertTrue(g.edges.size() == 0);
 
 		// we can't add an edge that
 		// does not point to valid nodes:
 		boolean except = false;
 		try { g.edges.add(e1); }
 		catch(IllegalArgumentException e) { except = true; }
 		assertTrue(except);
 	}
 
 	@Test
 	public void testAddNodes() {
 		Graph g1 = new Graph();
 		Node v1 = g1.createNode();
 		Node v2 = g1.createNode();
 		
 		g1.nodes.remove(v1);
 		assertTrue(g1.nodes.size() == 1);
 
 		g1.nodes.add(v1);
 		assertTrue(g1.nodes.size() == 2);
 		g1.createEdge(v1, v2);
 
 		Graph g2 = new Graph();
 		Node alien = g2.createNode();
 		boolean check1 = false;
 		try { g1.nodes.add(alien); }
 		catch(IllegalArgumentException e) { check1 = true; }
 		assertTrue(check1);
 	}
 
 	@Test
 	public void testToStrings() {
 		Graph g = new Graph();
 		Node v1 = g.createNode();
 		Node v2 = g.createNode();
 		Edge e1 = g.createEdge(v1, v2);
 		Actor a1 = new Actor();
 		Actor a2 = new Actor();
 		v1.actors.add(a1);
 		v2.actors.add(a2);
 
 		/*
 		System.out.println(new CreateAction(g, v1, new Actor()));
 		System.out.println(new CreateEdgeAction(g, v1, v2));
 		System.out.println(new CreateNodeAction(g));
 		System.out.println(new DestroyAction(g, a1, a2));
 		System.out.println(new DestroyEdgeAction(g, e1));
 		System.out.println(new DestroyNodeAction(g, v1));
 		System.out.println(new MoveAction(g, e1, a1));
 		*/
 	}
}
