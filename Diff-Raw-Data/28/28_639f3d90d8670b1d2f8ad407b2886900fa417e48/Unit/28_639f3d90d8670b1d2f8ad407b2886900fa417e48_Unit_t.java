 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
package Unit;
 
 import B3.trunk.src.Weapon.Weapon;
 import java.awt.Shape;
 import java.awt.Color;
 
 /**
  *
  * @author michal
  */
 public abstract class Unit {
 
     protected int health;
     protected int speed;
     protected Shape shape;
     protected Weapon weaponType;
     //protected int team;
     protected int pointValue;
 	protected int xCoord, yCoord;
 	protected Color color;
 
     public Unit(int health, int speed, Shape shape, Weapon weaponType, int pointValue, int xCoord, int yCoord, Color color) {
         this.health = health;
         this.speed = speed;
         this.shape = shape;
         this.weaponType = weaponType;
         this.pointValue = pointValue;
 		this.xCoord = xCoord;
 		this.yCoord = yCoord;
 		this.color = color;
     }
 
 	/**
 	* set/get methods for x/y coordinated and Unit color
 	* @author danielcecil
 	*/
	public void setX(int newX){
 		xCoord = newX;
 	}
 	
 	public int getX(){
 		return xCoord;
 	}
 	
	public void setY(int newY){
 		yCoord = newY;
 	}
 	
 	public int getY(){
 		return yCoord;
 	}
 	
	public void setColor(Color newColor){
 		color = newColor;
 	}
 	
 	public Color getColor(){
 		return color;
 	}
 	
 }
