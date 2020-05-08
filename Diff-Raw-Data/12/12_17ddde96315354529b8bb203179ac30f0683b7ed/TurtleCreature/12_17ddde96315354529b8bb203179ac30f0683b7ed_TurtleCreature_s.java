 package se.chalmers.kangaroo.model.creatures;
 
 import java.awt.Polygon;
 
 import se.chalmers.kangaroo.model.Creature;
 import se.chalmers.kangaroo.model.Direction;
 import se.chalmers.kangaroo.model.Position;
 import se.chalmers.kangaroo.utils.Waiter;
 
 /**
  * An enemy in the form of a turtle. Will go into its shell at random tmes and
  * will then be impossible to kill.
  * 
  * @author Arvid
  * 
  */
 public class TurtleCreature implements Creature {
 
 	private boolean inShell = false;
 	private Waiter w;
 	private Position pos;
 	private static final int ID = 112;
 
 
	public TurtleCreature(int id, Position spawnPos, Direction direction) {
 		pos = spawnPos;
 		w = new Waiter();
 	}
 
 	@Override
 	public void updateCreature() {
 		int i = (int) ((6) * Math.random() * 100);
 		if (i == 5)
 			changeState();
 		this.move();
 	}
 
 	/**
 	 * A class that change the state of the turtle. The turtle will be in its
 	 * shell for a random amount of time between 1 and 5 seconds.
 	 */
 	public void changeState() {
 		this.goInShell();
 		w.waitTime((int) ((6) * Math.random()) * 1000);
 		this.goOutOfShell();
 
 	}
 
 	/**
 	 * A method to make the turtle go into its shell.
 	 */
 	private void goInShell() {
 		inShell = true;
 	}
 
 	/**
 	 * A method to make the turtle go out of its shell.
 	 */
 	private void goOutOfShell() {
 		inShell = false;
 	}
 
 	/**
 	 * Will change if the Turtle is killable or not. If the Turtle is in its
 	 * shell, the creature shall be invurnable, and the Kangaroo will die if
 	 * they collides.
 	 */
 	@Override
 	public boolean isKillable() {
 		if (inShell) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * Will move the Turtle. If the turtle is in its shell, the Turtle will stay
 	 * in its place.
 	 */
 	@Override
 	public void move() {
 		if (!inShell) {
 			pos = new Position(pos.getX()-2, pos.getY());
 		}
 	}
 
 	@Override
 	public Polygon getPolygon() {
 		int xs[] = { pos.getX() + 0, pos.getX() + 14, pos.getX() + 14,
 				pos.getX() + 20, pos.getX() + 20, pos.getX() + 44,
 				pos.getX() + 44, pos.getX() + 50, pos.getX() + 50,
 				pos.getX() + 64, pos.getX() + 64, pos.getX() + 54,
 				pos.getX() + 54, pos.getX() + 10, pos.getX() + 10,
 				pos.getX() + 0 };
 		int ys[] = { pos.getY() + 2, pos.getY() + 2, pos.getY() + 0,
 				pos.getY() + 0, pos.getY() + 6, pos.getY() + 6, pos.getY() + 0,
 				pos.getY() + 0, pos.getY() + 2, pos.getY() + 2,
 				pos.getY() + 16, pos.getY() + 16, pos.getY() + 32,
 				pos.getY() + 32, pos.getY() + 16, pos.getY() + 16 };
 		return new Polygon(xs, ys, 16);
 
 	}
 
 	@Override
 	public int getId() {
 		return ID;
 	}
 
 	@Override
 	public Position getPosition() {
 		return pos;
 	}
 
 	@Override
 	public void changeDirection() {
 		//
 	}
 
 }
