 package toritools.entity.sprite;
 
 import java.awt.Graphics;
 import java.awt.Image;
 
 import toritools.math.Vector2;
 
 public class Sprite {
 	private int xSplit = 1, ySplit = 1, x = 0, y = 0, w, h;
 	public Image image;
 	private Vector2 bRight;
 	public int timeStretch = 1;
 	public float sizeOffset = 0;
 
 	public Sprite(final Image image, final int xTiles, final int yTiles) {
 		this.image = image;
 		this.xSplit = xTiles;
 		this.ySplit = yTiles;
 		w = image.getWidth(null);
 		h = image.getHeight(null);
 		bRight = new Vector2(w / xSplit, h / ySplit);
 	}
 
 	public void nextFrame() {
 		x = ++x % (xSplit * timeStretch);
 	}
 
 	public void setFrame(final int frame) {
		y = frame % ySplit;
 	}
 
 	public void setCylcle(final int cycle) {
 		y = cycle % ySplit;
 	}
 	
 	public void set(final int x, final int y){
 		this.x = x;
 		this.y = y;
 	}
 
 	public void draw(Graphics g, final Vector2 posO, final Vector2 dimO) {
 		int x = this.x / timeStretch;
 		Vector2 dim = dimO.add(sizeOffset * 2);
 		Vector2 pos = posO.sub(sizeOffset);
 		g.drawImage(image, (int) pos.x, (int) pos.y, (int) pos.x + (int) dim.x,
 				(int) pos.y + (int) dim.y, x * (int) bRight.x, y
 						* (int) bRight.y, x * (int) bRight.x + (int) bRight.x,
 				y * (int) bRight.y + (int) bRight.y, null);
 	}
 
 }
