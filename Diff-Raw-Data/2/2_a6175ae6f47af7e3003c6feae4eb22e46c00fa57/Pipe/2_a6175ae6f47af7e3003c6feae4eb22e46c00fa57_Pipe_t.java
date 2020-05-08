 package com.androidmontreal.rhok.pieces;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.badlogic.gdx.graphics.g2d.Sprite;
 
 /**
  * This piece represents a pipe between two other pieces (pipes, water source,
  * pump, no piece)
  */
 
 public class Pipe implements Piece {
 
 	List<Gate> gates;
 	Point position;
 	Boolean ticked;
 
 	public Pipe() {
 	}
 
 	public Pipe(int x, int y) {
 		position.x = x;
 		position.y = y;
 	}
 
 	@Override
 	public List<Gate> getGates() {
 		if (gates == null) {
			gates =  new ArrayList<Gate>();
 		}
 		return gates;
 	}
 
 	@Override
 	public Point getPosition() {
 		return null;
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
 
 }
