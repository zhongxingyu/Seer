 package gameObjects;
 import java.awt.image.BufferedImage;
import GameObject;
 
 
 
 public class Barrier extends GameObject {
 	
 	public Barrier(double x, double y, BufferedImage image){
 		myX = x;
 		myY = y;
 		myImage = image;
 	}
 	
 	public static class BarrierFactory extends GameObjectFactory{
 		
 		public BarrierFactory(double x, double y, BufferedImage image){
 			super.myName = "barrier";
 		    myX = x;
 			myY = y;
 			myImage = image;
 		}
 
 		@Override
 		public GameObject makeObject() {
 			return new Barrier(myX, myY, myImage);
 		}
 
 		@Override
 		public boolean isMyObject(String name) {
 			return myName.equals(name);
 		}
 		
 	}
 
 }
