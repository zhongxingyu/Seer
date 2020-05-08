 package pl.edu.agh.student.pathfinding.tests;
 
 import static org.junit.Assert.*;
 
 import java.awt.Point;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import pl.edu.agh.student.pathfinding.Map;
 
 public class MapTest {
 
 	Map map, startEndMap;
 	int[][] pixels;
 	
 	@Before
 	public void setUp() throws Exception {
 		
 		String mapPath = "tests/maps/test1.png";
 		startEndMap = new Map("tests/maps/test_start_end_point.png");
 		map = new Map(mapPath);
 		
 	}
 
 	@Test
 	public void loadTest() {		
		Assert.assertEquals("First pixel red not matching", 0, Map.getRed(map.getPixel(0, 0)));
		Assert.assertEquals("First pixel green not matching", 255, Map.getGreen(map.getPixel(0, 0)));
		Assert.assertEquals("First pixel blue not matching", 0, Map.getBlue(map.getPixel(0, 0)));
		Assert.assertEquals("First pixel grayscale not matching", 255, Map.getGrayscale(map.getPixel(0, 0)));
 	}
 	
 	@Test
 	public void startEndTest() {
 		Assert.assertEquals("Starting point not matching", new Point(0,0), startEndMap.getStartPoint());
 		Assert.assertEquals("Ending point not matching", new Point(1,0), startEndMap.getEndPoint());
 	}
 }
