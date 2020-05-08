 package org.guodman.coverfighter;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 
 public class Cover {
	public final int x, y, width, height;
 	
 	public Cover(final int x, final int y, final int width, final int height) {
		this.x = x;
		this.y = y;
 		this.width = width;
 		this.height = height;
 	}
 	
 	public void render(GameContainer container, Graphics g) {
 		g.setColor(Color.magenta);
		g.fillRect(x, y, width, height);
 	}
 	
 	public boolean containsPoint(final int x, final int y) {
		if (x > this.x && y > this.y && x <= this.x + width && y < this.y + height) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 }
