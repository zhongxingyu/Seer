 import java.awt.Point;
 
 public class Node {
	Point loc;
	int direction, angle;
 
 	public Node() {
 		loc = new Point();
 	}
 	
 	public Node(int x, int y) {
 		loc = new Point(x,y);
 		direction = 0;
 		angle = 360;
 	}
 	
 	boolean equals (Node n) {
 		return (this.loc.x == n.loc.x && this.loc.y == n.loc.y);
 	}
 	
 	//returns x coordinate
 	public int getX(){
 		return loc.x;
 	}
 	//returns y coordinate
 	public int getY(){
 		return loc.y;
 	}
 	
 	public int getDirection(){
 		return direction;
 	}
 	
 	public int getAngle(){
 		return angle;
 	}
 }
