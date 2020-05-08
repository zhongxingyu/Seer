 package tests;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Test;
 
 import src.Polygon;
 import src.utils.Point;
 
 public class PolygonTest {
 	
 	public Polygon setupTest() {
 		String name = "TestPolygon";
 		
 		ArrayList<Point> points = setupPoints();
 		
 		Polygon polygon = new Polygon(name, points);
 		return polygon;
 		
 	}
 
 	private ArrayList<Point> setupPoints() {
 		Point pt1 = new Point(0, 0);
 		Point pt2 = new Point(0, 5);
 		Point pt3 = new Point(5, 5);
 		Point pt4 = new Point(5, 0);
 		
 		ArrayList<Point> points = new ArrayList<Point>();
 		
 		points.add(pt1);
 		points.add(pt2);
 		points.add(pt3);
 		points.add(pt4);
 		return points;
 	}
 	
 	@Test
 	public void testConstructPolygon() {
 
 		Polygon polygon = setupTest();
 		assertEquals("TestPolygon", polygon.getName());
 		assertEquals((List<Point>)setupPoints(), polygon.getPoints());
 	}
 	
 	@Test
 	public void testAddPoints() {
 
 		Polygon polygon = setupTest();
 		Point p = new Point(6,0);
 		polygon.addPoint(p);
 		
 		ArrayList<Point> pointList = setupPoints();
 		pointList.add(p);
 		
 		assertEquals(pointList, polygon.getPoints());
 		
 	}
 	
 	@Test
 	public void testDeletePoints() {
 	
 		Polygon polygon = setupTest();
 		Point p = new Point(6,0);
 		polygon.removeLastPoint();
 		
 		ArrayList<Point> pointList = setupPoints();
 		pointList.remove(pointList.size() - 1);
 		
 		assertEquals(pointList, polygon.getPoints());
 		
 	}
 	
 	@Test
 	public void testRedoPoints() {
 		Polygon polygon = setupTest();
 		polygon.removeLastPoint();
 		polygon.redoPoint();
 		
 		ArrayList<Point> pointList = setupPoints();
 		assertEquals(pointList, polygon.getPoints());
 		
 		polygon.redoPoint();
 		assertEquals(pointList, polygon.getPoints());
 	}
 	
 	@Test
 	public void testAbleToRedo() {
 		Polygon polygon = setupTest();
 		assertFalse(polygon.canRedo());
 		
 		polygon.removeLastPoint();
 		
 		assertTrue(polygon.canRedo());
 		
 		polygon.redoPoint();
 		
 		assertFalse(polygon.canRedo());
 	}
 }
