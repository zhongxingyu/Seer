 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 
 
 public class Block {
 	private int d_x;
 	private int d_y;
 	
 	private int d_width;
 	private int d_height;
 	
 	public Block(int x, int y, int width, int height) {
 		d_x = x;
 		d_y = y;
 		d_width = width;
 		d_height = height;
 	}
 	
 	public void update(int x, int y, int width, int height) {
 		d_x = x;
 		d_y = y;
 		d_width = width;
 		d_height = height;
 	}
 	
 	public int x() {
 		return d_x;
 	}
 	
 	public int y() {
 		return d_y;
 	}
 	
 	public int width() {
 		return d_width;
 	}
 	
 	public int height() {
 		return d_height;
 	}
 	
     public boolean pointCollision(int x, int y) {
     	return x >= d_x && y >= d_y && x <= d_x + d_width && y <= d_y + d_height;
     }
 
 	public void draw(Graphics2D g2d) {
 
 		g2d.fillRect(d_x, d_y, d_width, d_height);
 	}
 }
