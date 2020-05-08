 package gameObjects;
 import java.awt.image.BufferedImage;
 

 public abstract class GameObjectFactory {
 
 	protected BufferedImage myImage;
 	protected double myX;
 	protected double myY;
 	protected String myName;
 
 	public abstract GameObject makeObject();
 	
<<<<<<< HEAD
 	public boolean isMyObject(String name) {
 		return myName.equals(name);
 	}
=======
	public abstract boolean isMyObject(String name);
 	
 	
     public String getMyName() {
         return myName;
     }
>>>>>>> 20ffa3ec8a442840efb900264717de8cb6d73974
 
 }
