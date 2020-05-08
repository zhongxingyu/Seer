 /**
  * 
  */
 package rogueshadow.XionEngine;
 
 import org.newdawn.slick.Image;
 
 /**
  * @author Adam
  *
  */
 public class Player {
 	public Integer getX() {
 		return x;
 	}
 
 	public void setX(Integer x) {
 		this.x = x;
 	}
 
 	public Integer getY() {
 		return y;
 	}
 
 	public void setY(Integer y) {
 		this.y = y;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public Image getImage() {
 		return image;
 	}
 
 	public void setImage(Image image) {
 		this.image = image;
 	}
 
 	private Integer x;
 	private Integer y;
 	private String name;
 	public String getAccount() {
 		return account;
 	}
 
 	private String account;
 	private Image image;
 	
 	public Player(String account, String name, Integer x, Integer y, Image image) {
 		this.account = account;
 		this.name = name;
 		this.x = x;
 		this.y = y;
 		this.image = image;
 	}
 	
 	public void movePlayer(Integer deltaX, Integer deltaY) {
 		this.x += deltaX;
 		this.y += deltaY;
 	}
 	
	public void draw(){
		image.draw(x,y);
 		
 	}
 	
 }
