 package gameObjects;
 import java.awt.image.BufferedImage;
 
 public abstract class GameObjectFactory {
 
 	protected BufferedImage myImage;
 	protected double myX;
 	protected double myY;
 	protected String myName;
 
 	public abstract GameObject makeObject();
 	
 	public boolean isMyObject(String name) {
 		return myName.equals(name);
 	}
 	
 	
     public String getMyName() {
         return myName;
     }
 
 }
