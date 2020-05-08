 package net.umc.ludumdare.common;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class Sprite {
 	
 	// Private Members
 	private Image[] images;
 	private float x, y, velX, velY;
 	private int spriteCnt;
 	
 	// Constructors
 	public Sprite() {
 		images = null;
 		x = y = velX = velY = 0;
 		spriteCnt = 0;
 	}
 
 	public Sprite(Image image) {
 		images = new Image[] { image };
 		x = y = velX = velY = 0;
 		spriteCnt = 0;
 	}
 	
 	public Sprite(Image[] images) {
 		this.images = images;
 		x = y = velX = velY = 0;
 		spriteCnt = 0;
 	}
 	
 	public Sprite(Image[] images, float x, float y, float velX, float velY) {
 		this.images = images;
 		this.x = x;
 		this.y = y;
 		this.velX = velX;
 		this.velY = velY;
 		spriteCnt = 0;
 	}
 	
 	public Sprite(Image image, float x, float y, float velX, float velY) {
 		images = new Image[] { image };
 		this.x = x;
 		this.y = y;
 		this.velX = velX;
 		this.velY = velY;
 		spriteCnt = 0;
 	}
 	
 	// Accessors
 	public void setImages(Image[] images) {
 		this.images = images;
 	}
 	
 	public Image[] getImages() {
 		return images;
 	}
 	
 	public void setImage(Image image) {
 		images = new Image[] { image };
 	}
 	
 	public Image getImage() {
 		return images.length >= 1 ? images[0] : null;
 	}
 	
 	public void setX(float x) { this.x = x; }
 	public void setY(float y) { this.y = y; }
 	public void setVelX(float velX) { this.velX = velX; }
 	public void setVelY(float velY) { this.velY = velY; }
 	
 	public float getX() { return x; }
 	public float getY() { return y; }
 	public float getVelX() { return velX; }
 	public float getVelY() { return velY; }
 	
 	// Public Methods
 	public void addImage(Image image) {
 		if (images == null) {
 			images = new Image[] { image };
 			return;
 		}
 		
 		Image[] temp = new Image[images.length + 1];
 		for (int i = 0; i < images.length; i++) {
 			temp[i] = images[i];
 		}
 		temp[images.length] = image;
 		images = temp;
 	}
 	
 	public void render(GameContainer gc, Graphics g){
 		g.drawImage(images[spriteCnt/15], x, y);
 	}
 	
 	public void render(GameContainer gc, Graphics g, float scale){
		images[spriteCnt/15].draw(x - (64 * scale / 2), y - (64 * scale / 2), scale);
 	}
 	
 	public void update(GameContainer gc, StateBasedGame sbg, int delta){
 		spriteCnt++;
 		if(spriteCnt>=15*images.length){
 			spriteCnt = 0;
 		}
 		x += velX;
 		y += velY;
 	}
 	
 }
