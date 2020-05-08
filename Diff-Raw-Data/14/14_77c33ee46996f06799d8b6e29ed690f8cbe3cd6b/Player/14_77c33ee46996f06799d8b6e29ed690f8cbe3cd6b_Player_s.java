 package spacegamecraft.player;
 
 import spacegamecraft.InputHandler;
 import spacegamecraft.geo.Point;
 import spacegamecraft.gfx.Buffer;
import spacegamecraft.gfx.Color;
 
 public class Player {
 	public Point loc;
 	public int color;
 	
 	public Player(Point loc, int color) {
 		this.loc = loc;
 		this.color = color;
 	}
 	
 	public Buffer draw(Buffer buf) {
 		try {
 			buf.pixels[loc.x][loc.y] = color;
 			buf.pixels[loc.x+1][loc.y] = color;
 			buf.pixels[loc.x][loc.y+1] = color;
 			buf.pixels[loc.x+1][loc.y+1] = color;
 		} catch (Exception e) {
 			System.out.println("Out of bounds");
 		}
 
 		return buf;
 	}
 	
 	public void handleKey(InputHandler input) {
 		if(input.keys[39]) {
 			moveRight();
 		}
 		if(input.keys[38]) {
 			moveUp();
 		}
 		if(input.keys[37]) {
 			moveLeft();
 		}
 		if(input.keys[40]) {
 			moveDown();
 		}
 	}
 	
 	public void moveUp() {
 		loc = loc.add(0, -1);
 	}
 	public void moveDown() {
 		loc = loc.add(0, 1);
 	}
 	public void moveLeft() {
 		loc = loc.add(-1, 0);
 	}
 	public void moveRight() {
 		loc = loc.add(1, 0);
 	}
 }
