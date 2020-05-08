 package objetcs;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 public class Object {
 	
 	private int positionX;
 	private int positionY;
 	private int width;
 	private int height;
 	private Image img;
 	
 	public Object(int positionX, int positionY, int width, int height) {
 		super();
 		this.positionX = positionX;
 		this.positionY = positionY;
 		this.width = width;
 		this.height = height;
 	}
 	
 	public Object(int positionX, int positionY, String image) throws SlickException {
 		super();
 		this.positionX = positionX;
 		this.positionY = positionY;
 		this.img = new Image(image);
		this.width = img.getWidth();
		this.height = img.getHeight();
 	}
 	
 	public int getPositionX() {
 		return positionX;
 	}
 
 	public void setPositionX(int positionX) {
 		this.positionX = positionX;
 	}
 
 	public int getPositionY() {
 		return positionY;
 	}
 
 	public void setPositionY(int positionY) {
 		this.positionY = positionY;
 	}
 
 	public int getWidth() {
 		return width;
 	}
 
 	public void setWidth(int width) {
 		this.width = width;
 	}
 
 	public int getHeight() {
 		return height;
 	}
 
 	public void setHeight(int height) {
 		this.height = height;
 	}
 
 	public Image getImg() {
 		return img;
 	}
 
 	public void draw(Graphics g) {
 		g.setColor(Color.cyan);
 		g.fillRect(this.getPositionX(), this.getPositionY(), this.getWidth(), this.getHeight());
 	}
 	
 }
