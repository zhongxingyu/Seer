 package com.github.rocketsurgery;
 
 import java.util.List;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Circle;
 import org.newdawn.slick.state.StateBasedGame;
 
 @SuppressWarnings("serial")
 public abstract class Node extends Circle implements DisplayElement {
 	
 	public static final float sizeOnScreen = 30f;
 	public static final Color nodeColor = Color.blue;
 	
 	public Node(float centerPointX, float centerPointY) {
 		super(centerPointX, centerPointY, sizeOnScreen);
 	}
 	
 	@Override
 	public void Render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException {
 		g.setColor(nodeColor);
 		g.setAntiAlias(false);
 		g.fill(this);
 	}
 	
 	public abstract boolean attach(Wire wire);
 	
 	public void swapLocations(Node other) {
		float tempX = this.getCenterX();
		float tempY = this.getCenterY();
		this.setCenterX(other.getCenterX());
		this.setCenterY(other.getCenterY());
 		other.setX(tempX);
 		other.setY(tempY);
 	}
 	
 	public boolean isOver(int x, int y) {
 		return Math.sqrt(Math.pow(getCenterX() - x, 2) + Math.pow(getCenterY() - y, 2)) < sizeOnScreen;
 	}
 	
 	public abstract List<Wire> getWires();
 
 }
