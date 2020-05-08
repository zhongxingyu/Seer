 package com.secondhand.model.randomlevelgenerator.sat;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.secondhand.debug.MyDebug;
 import com.secondhand.model.physics.Vector2;
 import com.secondhand.model.util.sat.Circle;
 import com.secondhand.model.util.sat.Polygon;
 import com.secondhand.model.util.sat.PolygonFactory;
 import com.secondhand.model.util.sat.World;
 
 
 import junit.framework.TestCase;
 
 public class WorldTest extends TestCase {
 
 	
 	public void testIsWithinWorldBoundsPolygon() {
 			
 		World world = new World(100, 100);
 		
 		List<Vector2> edges = new ArrayList<Vector2>();
 		edges.add(new Vector2(0, 0));
 		edges.add(new Vector2(3,1));
 		edges.add(new Vector2(4,3));
 		edges.add(new Vector2(3,4));
 		edges.add(new Vector2(0,3));
 		
 		Polygon polygon = new Polygon(new Vector2(0,0), edges);
 		
 		assertTrue(world.isUnoccupied(polygon));
 		
 		List<Vector2> edges2 = new ArrayList<Vector2>();
 		edges2.add(new Vector2(0,1));
 		edges2.add(new Vector2(2,0));
 		edges2.add(new Vector2(2,1));
 		Polygon polygon2 = new Polygon(new Vector2(3,2), edges2);
 		
 		assertTrue(world.isUnoccupied(polygon2));
 		
 		world.addToWorld(polygon);
 		
 		assertFalse(world.isUnoccupied(polygon2));
 		
 		Polygon polygon3 = new Polygon(new Vector2(-10,10), edges2);
 		assertFalse(world.isUnoccupied(polygon3));
 	}
 	
 	public void testCreateRectangle() {
 				
 		World world = new World(100, 100);
 		
 		Polygon rect1 = PolygonFactory.createRectangle(new Vector2(0,0), 10, 10);
 		world.addToWorld(rect1);
 		
 		Polygon rect2 = PolygonFactory.createRectangle(new Vector2(5,5), 10, 10);
 		assertFalse(world.isUnoccupied(rect2));
 		
 		Polygon rect3 = PolygonFactory.createRectangle(new Vector2(11, 11), 10, 10);
 		assertTrue(world.isUnoccupied(rect3));
 		
 	}
 	
 	public void testCreateCircle() {
 
 		MyDebug.d("testCreateCircle");
 
 		World world = new World(100, 100);
 
 		
 		
 		// outside world bounds
 		Circle circ = new Circle(new Vector2(0.5f,0.5f), 1);
 		assertFalse(world.isUnoccupied(circ));
 
 		// outsode world bounds
 		circ = new Circle(new Vector2(99,10), 2);
 		assertFalse(world.isUnoccupied(circ));
 
 		circ = new Circle(new Vector2(99,99), 2);
 		assertFalse(world.isUnoccupied(circ));
 
 		
 		circ = new Circle(new Vector2(1,1), 1);
 		assertTrue(world.isUnoccupied(circ));
 
 		world.addToWorld(circ);
 
 		circ = new Circle(new Vector2(1.5f,1), 1);
 		assertFalse(world.isUnoccupied(circ));
 
 		circ = new Circle(new Vector2(1.95f,1), 1);
 		assertFalse(world.isUnoccupied(circ));
 
 
 		circ = new Circle(new Vector2(3.5f, 1), 1);
 		assertTrue(world.isUnoccupied(circ));
 		world.addToWorld(circ);
 		
 		circ = new Circle(new Vector2(2.3f, 1f), 0.1f);
 		assertTrue(world.isUnoccupied(circ));
 		
 		
 		circ = new Circle(new Vector2(2.3f, 1f), 0.3f);
 		assertFalse(world.isUnoccupied(circ));
 		
 		circ = new Circle(new Vector2(2.3f, 1f), 0.1f);
 		world.addToWorld(circ);
 		
 		circ = new Circle(new Vector2(2.3f, 0.65f), 0.1f);
 		assertTrue(world.isUnoccupied(circ));
 		
 	}
 
 	// with a circle and a polygon. 
 	public void testCreateCircle2() {
 		
 		MyDebug.d("testCreateCircle2");
 
 		World world = new World(100, 100);
 
 		List<Vector2> edges = new ArrayList<Vector2>();
 		edges.add(new Vector2(0, 0));
 		edges.add(new Vector2(3,1));
 		edges.add(new Vector2(4,3));
 		edges.add(new Vector2(3,4));
 		edges.add(new Vector2(0,3));
 		Polygon polygon = new Polygon(new Vector2(0,0), edges);
 		
 		world.addToWorld(polygon);
 	
 		
 		Circle rect2 =new Circle(new Vector2(6,3), 1);
 		assertTrue(world.isUnoccupied(rect2));
 
 		Circle rect3 =new Circle(new Vector2(4, 3), 1);
 		assertFalse(world.isUnoccupied(rect3));
 	}
 	
 	public void testPolygonContainment() {
 		// test polygon in polygon containment
 		
 		World world = new World(100, 100);
 
 		Polygon rect1 = PolygonFactory.createRectangle(new Vector2(0,0), 10, 10);
 		world.addToWorld(rect1);
 
 		Polygon rect2 = PolygonFactory.createRectangle(new Vector2(3,3), 2, 2);
 		assertFalse(world.isUnoccupied(rect2));
 	}
 	
 	public void testCircleContainment() {
 		// test circle in circle containment
 		
 		World world = new World(100, 100);
 
 
 		Circle circ = new Circle(new Vector2(5,5), 3);
 
 		world.addToWorld(circ);
 
 
 		circ = new Circle(new Vector2(5,5), 2);
 		assertFalse(world.isUnoccupied(circ));
 	}
 	
 	public void testCircleInPolygonContainment() {
 		// test circle in polygon containment
 		
 		World world = new World(100, 100);
 
 		Polygon rect1 = PolygonFactory.createRectangle(new Vector2(0,0), 10, 10);
 		world.addToWorld(rect1);
 
 		Circle circ = new Circle(new Vector2(5,5), 3);
 		assertFalse(world.isUnoccupied(circ));
 	}
 
 	public void testPolygonInCircleContainment() {
 		// test polygon in circle containment
 		
 		World world = new World(100, 100);
 
 
 		Circle circ = new Circle(new Vector2(5,5), 3);
 		world.addToWorld(circ);
 
 		Polygon rect1 = PolygonFactory.createRectangle(new Vector2(5,5), 2, 2);
		assertFalse(world.isUnoccupied(circ));
 	}
 	
 }
