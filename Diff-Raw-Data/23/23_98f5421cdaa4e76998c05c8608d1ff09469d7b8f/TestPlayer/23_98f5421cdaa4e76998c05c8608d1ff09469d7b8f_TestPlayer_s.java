 package tests;
 
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 
 import logic.Player;
 
 public class TestPlayer extends Player {
 
 	public TestPlayer(int guid, Point2D pos, String name) {
 		super(guid, pos, name);
 	}
 
 	@Override
 	public Rectangle2D GetBoundingBox() {
 		// TODO Auto-generated method stub
		return null;
 	}
 
 }
