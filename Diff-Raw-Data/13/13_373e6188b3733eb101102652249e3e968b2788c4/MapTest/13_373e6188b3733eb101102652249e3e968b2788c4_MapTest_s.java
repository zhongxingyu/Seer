 package pl.edu.agh.student.pathfinding.tests;
 
 import static org.junit.Assert.*;
 
 import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;

import javax.imageio.ImageIO;
 
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
		Assert.assertEquals("First pixel red not matching", 27, Map.getRed(map.getPixel(0, 0)));
		Assert.assertEquals("First pixel green not matching", 229, Map.getGreen(map.getPixel(0, 0)));
		Assert.assertEquals("First pixel blue not matching", 51, Map.getBlue(map.getPixel(0, 0)));
		Assert.assertEquals("First pixel grayscale not matching", 51 + 229 + 27, Map.getGrayscale(map.getPixel(0, 0)));
 	}
 	
 	@Test
 	public void startEndTest() {
 		Assert.assertEquals("Starting point not matching", new Point(0,0), startEndMap.getStartPoint());
 		Assert.assertEquals("Ending point not matching", new Point(1,0), startEndMap.getEndPoint());
 	}
 }
