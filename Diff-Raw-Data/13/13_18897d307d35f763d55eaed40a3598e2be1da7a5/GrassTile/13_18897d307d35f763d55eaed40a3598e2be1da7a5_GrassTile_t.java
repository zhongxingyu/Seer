 package net.lifesizedesign.rain.level.tile;
 
 import net.lifesizedesign.rain.graphics.Screen;
 import net.lifesizedesign.rain.graphics.Sprite;
 
 public class GrassTile extends Tile {
 
 	public GrassTile(Sprite sprite) {
 		super(sprite);
 	}
 
 	public void render(int x, int y, Screen screen) {
 		screen.renderTile(x, y, this);
 	}
 }
