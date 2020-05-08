 package com.example.SubGameObj;
 
 import com.example.SubGameObj.Utils.Position;
 
 /**
  * This class holds methods for drawing objects. 
  * @author anshirtz
  *
  */
 public abstract class ObjectDrawer {
 	
 	public abstract void prepareDrawer();
 	
 	public abstract void postChanges();
 	
	public abstract void drawMap();
	
 	public abstract void drawSubmarine (Position pos);
 	
 	public abstract void drawEnemyShip (Position pos);
 	
 	public abstract void drawTorpedo (Position pos);
 	
 	public abstract void drawDepthCharge (Position pos);
 	
 	public abstract void drawExplosion (Position pos);
 	
 	public abstract void drawSound (Position pos);
 	
 	public abstract void drawSonarPing (Position pos);
 }
