 package tests;
 
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
import gui.swing.SwingWall;
 
 import logic.Wall;
 
 public class TestWall extends Wall {
 
 	public TestWall(int guid, Point2D pos, int hitpoints) {
 		super(guid, pos, hitpoints);
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public Rectangle2D GetBoundingBox() {
 		// TODO Auto-generated method stub
		return new Rectangle2D.Double(Position.getX() - SwingWall.SIZE / 2, Position.getY() - SwingWall.SIZE / 2, SwingWall.SIZE, SwingWall.SIZE);
 	}
 
 }
