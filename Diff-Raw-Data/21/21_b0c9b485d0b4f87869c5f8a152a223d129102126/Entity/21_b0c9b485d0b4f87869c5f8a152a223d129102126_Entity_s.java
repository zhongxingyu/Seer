 package io.furyhunter.ld26editor;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.io.PrintWriter;
 
 public class Entity {
 	public float x;
 	public float y;
 	public float width = 32;
 	public float height = 32;
	public String metadata;
 	
 	public void draw(Graphics g, int offx, int offy) {
 		g.setColor(Color.RED);
 		g.drawString("?", Math.round(x) - offy, Math.round(y) - offy);
 	}
 	
 	public boolean touch(int x, int y) {
 		if (x > this.x && x < this.x + width) {
 			if (y > this.y && y < this.y + height) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	public void save(PrintWriter write) {
 		write.println("Unknown");
 	}
 }
