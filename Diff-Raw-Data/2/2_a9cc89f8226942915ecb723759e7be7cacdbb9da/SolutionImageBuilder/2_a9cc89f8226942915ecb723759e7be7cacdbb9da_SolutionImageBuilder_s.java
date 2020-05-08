 package pl.edu.agh.student.pathfinding.util;
 
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.image.BufferedImage;
 
 import pl.edu.agh.student.pathfinding.Solution;
 import pl.edu.agh.student.pathfinding.map.IMap;
 
 public class SolutionImageBuilder {
 	
	private static int PATH_COLOR = 0x00FF0000;
 	
 	public static Image generateImage(Solution solution) {
 		IMap map = solution.getMap();		
 		BufferedImage image = map.getMapImage();
 		
 		for(Point p : solution.getSteps()) {
 			image.setRGB(p.x, p.y, PATH_COLOR);
 		}
 		
 		return image;
 	}
 
 }
