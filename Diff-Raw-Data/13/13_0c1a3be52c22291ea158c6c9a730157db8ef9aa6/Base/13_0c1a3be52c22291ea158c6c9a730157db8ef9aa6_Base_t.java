 package engine;
 
import java.awt.Image;

 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.vecmath.Vector2f;
 
 public class Base {
 	private final Vector2f position;
 	private int capacity;
 	private int nbAgents;
 	private final JLabel baseSprite;
 	
 	public Base(int posX, int posY, int capacity){
 		super();
 		this.position = new Vector2f(posX, posY);
 		this.capacity = capacity;
 		this.nbAgents = capacity/2;
 		this.baseSprite = new JLabel();
 	}
 	
 	public void setSprite(ImageIcon img){
		//Resize the base sprite
		int baseSize = 10*capacity;
		Image originalImage = img.getImage();
		Image resizedImage = originalImage.getScaledInstance(baseSize, baseSize, Image.SCALE_FAST);
		img.setImage(resizedImage);
		
		//Attach the base sprite
 		this.baseSprite.setIcon(img);
		this.baseSprite.setBounds((int)this.position.x, (int)this.position.y, baseSize, baseSize);
 	}
 	
 	public JLabel getSprite(){
 		return this.baseSprite;
 	}
 	
 	public int getXCoord(){
 		return (int) this.position.x;
 	}
 	
 	public int getYCoord(){
 		return (int) this.position.y;
 	}
 	
 	public int getCapacity(){
 		return this.capacity;
 	}
 }
