 package net.ciderpunk.tanktris.entities;
 
 import java.awt.Graphics2D;
 
 import net.ciderpunk.tanktris.IGameState;
 import net.ciderpunk.tanktris.graphics.Animation;
 import net.ciderpunk.tanktris.graphics.Frame;
 
 public class Shot extends Entity{
 	
 	static Frame oFrameShot;
 	double dX;
 	double dY;
 	double dDX;
 	double dDY;
 	
 	
 	public Shot(int x, int y, double iDirection, double dSpeed) {
 		super(x, y);
 		dX = x;
 		dY = y;
		dDX = Math.sin(iDirection) * dSpeed;
		dDY = Math.cos(iDirection) * dSpeed;
 		
 		
 		// TODO Auto-generated constructor stub
 	}
 
 	public static void loadResources(Frame oSpriteSheet){
		oFrameShot = new Frame(oSpriteSheet,0,0,36,96,19,104);
 	}
 	
 
 	public void update() {
 		dX += dDX;
 		dY += dDY;
 		iX = (int) Math.round(dX);
 		iY = (int) Math.round(dY);
 	}
 
 	@Override
 	public void draw(Graphics2D oG) {
 		// TODO Auto-generated method stub
 		oFrameShot.draw(oG, this.getX(), this.getY());
 		
 	}
 
 	
 	
 	
 }
