 package com.androidmontreal.rhok.pieces;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.badlogic.gdx.graphics.g2d.Sprite;
 
 /**
  * This piece represents the monster village where the water need to go.
  */
 public class MonsterVillage implements Piece {
 
 	private Gate sourceEntry;
 	
	private Point position;
 	
 	@Override
 	public List<Gate> getGates() {
 		if (sourceEntry == null) {
 			return null;
 		}
 		ArrayList<Gate> gates = new ArrayList<Gate>();
 		gates.add(sourceEntry);
 		return gates;
 	}
 
 	@Override
 	public Point getPosition() {
 		return position;
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
