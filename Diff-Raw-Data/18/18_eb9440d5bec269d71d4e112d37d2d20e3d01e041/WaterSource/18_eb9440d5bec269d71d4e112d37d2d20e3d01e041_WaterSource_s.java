 package com.androidmontreal.rhok.pieces;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.badlogic.gdx.graphics.g2d.Sprite;
 
 /**
  * This piece represents a water source where the water needed by the monster village is available.
  */
 public class WaterSource implements Piece {
 
 	private Gate sourceExit;
 	private Boolean ticked;
 	
 	private Point position = new Point(0,0);  // DEFAULT
 	
 	@Override
 	public List<Gate> getGates() {
 		if (sourceExit == null) {
 			return null;
 		}
 		ArrayList<Gate> gates = new ArrayList<Gate>();
 		gates.add(sourceExit);
 		return gates;
 	}
 
 	@Override
 	public Point getPosition() {
 		return position;
 	}
 
 	@Override
 	public void tick(long timedelta) {
 		// TODO Auto-generated method stub
 		
 		this.ticked = true;
 	}
 
 	@Override
 	public boolean isTicked() {
 		return this.ticked;
 	}
 
 	@Override
 	public void resetTick() {
 		this.ticked = false;
 	}
 
 	@Override
 	public Sprite getCurrentSprite() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public double getWater() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public void setWater(double volume) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	public double pullWater(double volume) {
 		return volume; // for now, the water source is unlimited
 	}
 
	@Override
	public double pullWater(double volume) {
		// TODO Auto-generated method stub
		return 0;
	}

	
 }
