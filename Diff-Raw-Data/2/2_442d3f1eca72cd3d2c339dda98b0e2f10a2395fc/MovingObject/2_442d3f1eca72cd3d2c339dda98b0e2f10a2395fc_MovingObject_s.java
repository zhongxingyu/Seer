 package bitcity;
 
 import java.awt.Point;
 import java.awt.Graphics2D;
 
 public abstract class MovingObject extends WorldObject {
 	protected Point pos;
 	protected char direction;
 	
 	public MovingObject(Point startPos, char direction) {
 		this.pos = startPos;
 		this.direction = direction;
 	}
 	
 	abstract void draw(Graphics2D g);
 	
 	public Point getNextPos(char direction, Point pos) throws Exception {
 		switch (direction) {
 		case '+':
 			return new Point(pos.x - 1, pos.y);
 		case '-':
 			return new Point(pos.x + 1, pos.y);
 		case '<':
 			return new Point(pos.x, pos.y - 1);
 		case '>':
 			return new Point(pos.x, pos.y + 1);
 		default:
			throw new Exception("Invalid direction '" + direction + "'");
 		}
 	}
 	
 	public char getDirection() {
 		return this.direction;
 	}
 	
 	public void whereAmI() {
 		System.out.println(this.pos.toString());
 	}
 }
