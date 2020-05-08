 package com.github.rocketsurgery;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.geom.Line;
 import org.newdawn.slick.geom.Shape;
 
 @SuppressWarnings("serial")
 public class Wire extends Line {
 
 	private Node end1, end2;
 	
 	private Color wireColor = Color.red;
	private float wireWidth = 5f;
 	
 	public Wire(Node first, Node second) {
 		super(first.getX(), first.getY(), second.getX(), second.getY());
 		first.attach(this);
 		second.attach(this);
 		this.end1 = first;
 		this.end2 = second;
 	}
 	
 	public void resetEnds() {
 		this.set(end1.getX(), end1.getY(), end2.getX(), end2.getY());
 	}
 	
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException {
 		g.setColor(wireColor);
 		g.setLineWidth(wireWidth);
 		g.setAntiAlias(true);
 		g.drawLine(end1.getCenterX(), end1.getCenterY(), end2.getCenterX(), end2.getCenterY());
 		g.fillOval(end1.getCenterX() - wireWidth / 2, end1.getCenterY() - wireWidth / 2, wireWidth, wireWidth);
 		g.fillOval(end2.getCenterX() - wireWidth / 2, end2.getCenterY() - wireWidth / 2, wireWidth, wireWidth);
 	}
 	
 	@Override
 	public boolean intersects(Shape shape) {
 		if (!(shape instanceof Wire))
 			throw new IllegalArgumentException();
 		Wire wire = (Wire) shape;
 		if (super.intersects(wire)) {
 			return !(wire.hasEnd(end1) || wire.hasEnd(end2));
 		}
 		return false;
 	}
 	
 	public boolean hasEnd(Node node) {
 		return end1 == node || end2 == node;
 	}
 	
 }
