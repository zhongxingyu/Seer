 package Items;
 
 import java.awt.Rectangle;
 import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
 
 import Core.Canvas;
 
 public abstract class Item {
 	private BufferedImage image;
 	private int posX;
 	private int posY;
 	private int WIDTH;
 	private int HEIGHT;
 	private Rectangle boundingBox;
 	private Canvas canvas;
 	
 	public Item(BufferedImage image, Canvas canvas){
 		this.image = image;
 		this.canvas = canvas;
 		this.WIDTH = image.getWidth();
 		this.HEIGHT = image.getHeight();
		try {
			this.image = ImageIO.read(getClass().getResourceAsStream("data/Bottle.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 	}
 	
 	/**
 	 * @return the canvas
 	 */
 	public Canvas getCanvas() {
 		return canvas;
 	}
 
 	/**
 	 * @param canvas the canvas to set
 	 */
 	public void setCanvas(Canvas canvas) {
 		this.canvas = canvas;
 	}
 
 	/**
 	 * @return the image
 	 */
 	public BufferedImage getImage() {
 		return image;
 	}
 	/**
 	 * @param image the image to set
 	 */
 	public void setImage(BufferedImage image) {
 		this.image = image;
 	}
 	/**
 	 * @return the posX
 	 */
 	public int getPosX() {
 		return posX;
 	}
 	/**
 	 * @param posX the posX to set
 	 */
 	public void setPosX(int posX) {
 		this.posX = posX;
 	}
 	/**
 	 * @return the posY
 	 */
 	public int getPosY() {
 		return posY;
 	}
 	/**
 	 * @param posY the posY to set
 	 */
 	public void setPosY(int posY) {
 		this.posY = posY;
 	}
 	/**
 	 * @return the wIDTH
 	 */
 	public int getWIDTH() {
 		return WIDTH;
 	}
 	/**
 	 * @param wIDTH the wIDTH to set
 	 */
 	public void setWIDTH(int wIDTH) {
 		WIDTH = wIDTH;
 	}
 	/**
 	 * @return the hEIGHT
 	 */
 	public int getHEIGHT() {
 		return HEIGHT;
 	}
 	/**
 	 * @param hEIGHT the hEIGHT to set
 	 */
 	public void setHEIGHT(int hEIGHT) {
 		HEIGHT = hEIGHT;
 	}
 	/**
 	 * @return the boundingBox
 	 */
 	public Rectangle getBoundingBox() {
 		return boundingBox;
 	}
 	/**
 	 * @param boundingBox the boundingBox to set
 	 */
 	public void setBoundingBox(Rectangle boundingBox) {
 		this.boundingBox = boundingBox;
 	}
 	
 	
 	
 	
 }
