 package image;
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 public class SpriteSheet {
 
 	private int x;
 	private int y;
 	private int spriteWidth;
 	private int spriteHeight;
 	
 	private BufferedImage image;
 	
 	public SpriteSheet(int x, int y, int spriteWidth, int spriteHeight, String filename){
 		this.x = x;
 		this.y = y;
 		this.spriteHeight = spriteHeight;
 		this.spriteWidth = spriteWidth;
 		try {
			image = new ImageLoader().getImage(filename);
		} catch (Error e) {
			System.out.println("That image doesn't exist, or atleast that is probably the problem");
 		}
 	}
 	
 	public Image getImage(){
 		return image.getSubimage(x*spriteWidth, y*spriteHeight, spriteWidth, spriteHeight);
 	}
 	
 	public int getX() {
 		return x;
 	}
 	
 	public void setX(int x) {
 		this.x = x;
 	}
 
 	public int getY() {
 		return y;
 	}
 
 	public void setY(int y) {
 		this.y = y;
 	}
 	
 	public int getSpriteWidth(){
 		return spriteWidth;
 	}
 	public void setSpriteWidth(int width){
 		this.spriteWidth = width;
 	}
 	
 	public int getSpriteHeight(){
 		return spriteHeight;
 	}
 	
 	public void setSpriteHeight(int height){
 		this.spriteHeight = height;
 	}
 	
 	/**
 	 * The width of the sprtie sheet, i.e. how many sprites there are in the row
 	 * @return
 	 */
 	public int getWidth(){
 		return image.getWidth()/spriteWidth;
 	}
 	
 	public int getHeight(){
 		return image.getHeight()/spriteHeight;
 	}
 
 }
