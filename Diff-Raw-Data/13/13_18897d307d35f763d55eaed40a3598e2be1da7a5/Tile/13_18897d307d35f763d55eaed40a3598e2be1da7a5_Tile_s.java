 package net.lifesizedesign.rain.level.tile;
 
 import net.lifesizedesign.rain.graphics.Screen;
 import net.lifesizedesign.rain.graphics.Sprite;
 
 public class Tile {
 
 	public int x, y;
 	public Sprite sprite;
 
 	public Tile(Sprite sprite) {
 		this.sprite = sprite;
 	}
 
 	public void render(int x, int y, Screen screen) {
 	}
 
 	public boolean solid() {
 		return false;
 	}
}
