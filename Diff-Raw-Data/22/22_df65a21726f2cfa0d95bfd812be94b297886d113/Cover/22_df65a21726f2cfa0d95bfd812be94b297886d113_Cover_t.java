 package org.guodman.coverfighter;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 
 public class Cover {
	public final int mapx, mapy, width, height;
 	
 	public Cover(final int x, final int y, final int width, final int height) {
		this.mapx = x;
		this.mapy = y;
 		this.width = width;
 		this.height = height;
 	}
 	
 	public void render(GameContainer container, Graphics g) {
 		g.setColor(Color.magenta);
		g.fillRect(mapx-CoverFighterGame.screenXOffset, mapy-CoverFighterGame.screenYOffset, width, height);
 	}
 	
 	public boolean containsPoint(final int x, final int y) {
		if (x > this.mapx && y > this.mapy && x <= this.mapx + width && y < this.mapy + height) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 }
