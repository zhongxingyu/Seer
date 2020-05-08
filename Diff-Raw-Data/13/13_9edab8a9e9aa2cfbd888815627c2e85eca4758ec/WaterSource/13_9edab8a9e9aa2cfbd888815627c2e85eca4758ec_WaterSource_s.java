 package com.androidmontreal.rhok.pieces;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.badlogic.gdx.graphics.g2d.Sprite;
 
 /**
  * This piece represents a water source where the water needed by the monster village is available.
  */
 public class WaterSource implements Piece {
 
 	private Gate sourceExit;
 	
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
		// TODO Auto-generated method stub
		return null;
 	}
 
 	@Override
 	public void tick(long timedelta) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public boolean isTicked() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void resetTick() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public Sprite getCurrentSprite() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	
 }
