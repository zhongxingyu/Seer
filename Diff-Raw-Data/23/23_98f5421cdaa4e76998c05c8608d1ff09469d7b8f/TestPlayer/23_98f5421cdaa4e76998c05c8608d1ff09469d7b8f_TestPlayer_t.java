 package tests;
 
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 
 import logic.Player;
import gui.swing.SwingPlayer;
 
 public class TestPlayer extends Player {
 
 	public TestPlayer(int guid, Point2D pos, String name) {
 		super(guid, pos, name);
 	}
 
 	@Override
 	public Rectangle2D GetBoundingBox() {
 		// TODO Auto-generated method stub
		return new Rectangle2D.Double(Position.getX() - SwingPlayer.SIZE_WIDTH / 2 - 1, Position.getY() - SwingPlayer.SIZE_HEIGHT / 2 - 1, SwingPlayer.SIZE_WIDTH + 2, SwingPlayer.SIZE_HEIGHT + 2);
 	}
 
 }
