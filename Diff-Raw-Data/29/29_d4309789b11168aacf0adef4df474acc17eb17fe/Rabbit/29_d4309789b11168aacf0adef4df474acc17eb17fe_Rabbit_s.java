 package project1;
 
 import java.awt.Color;
 
 import uchicago.src.sim.gui.Drawable;
 import uchicago.src.sim.gui.SimGraphics;
 import uchicago.src.sim.space.Object2DGrid;
 
 public class Rabbit {
 	
 	private int x;
 	private int y;
 	private int energy;
 	private World worldSpace;
 	
	public Rabbit(int en) {
 		System.out.println("Create new rabbit");
 		energy = en;
 	}
 	
 	public void setPosXY(int newX,int newY){
 		x = newX;
 		y = newY;
 	}
 	
 	public int getPositionX() {
 		return x;
 	}
 	
 	public int getPositionY() {
 		return y;
 	}
 	
 	public void setWorld(World _worldSpace) {
 		worldSpace = _worldSpace;
 	}
 	
 	public void eatGrass() {
 		energy += worldSpace.eatGrassAt(x,y);
 	}
 	
 	public void draw(SimGraphics G){
 		G.drawFastRoundRect(Color.blue);
 	}
 	
 	public void moveRabbit() {
 		int vx = 0;
 		int vy = 0;
 		
 		vx = (int) Math.floor(Math.random());
 		
 		if (vx > 0) {
 			vy = 0;
 		} else {
 			vy = (int) Math.floor(Math.random());
 		}
 		
 		int newX = worldSpace.checkBoundryX(x+vx);
 		int newY = worldSpace.checkBoundryY(y+vy);
 		
 		if(worldSpace.checkIfRabbitOn(newX, newY)) {
 			worldSpace.removeRabbitAt(x, y);
 			x = newX;
 			y = newY;
 			worldSpace.checkRabbitIn(this);
 		}
 	}
 	
 	public int getEnergy(){
 		return energy;
 	}
 	
 	public void setEnergy(int en) {
 		energy = en;
 	}
 	
 	public void report() {
 		System.out.println("Position x " + x +
 				" and Position y " + y +
 				" with " + energy + " energy.");
 	}
 	
 	
 
 }
