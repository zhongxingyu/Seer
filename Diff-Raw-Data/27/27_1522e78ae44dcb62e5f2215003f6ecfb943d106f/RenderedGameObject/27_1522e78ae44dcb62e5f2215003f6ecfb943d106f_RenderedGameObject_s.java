 package de.xftl.game.framework.ui;
 
 import java.util.ArrayList;
 
 import de.xftl.game.framework.GameObject;
 import de.xftl.game.framework.XftlGameRenderer;
 
 public abstract class RenderedGameObject extends GameObject {
 	
 	private ArrayList<RenderedGameObject> _children;
 
 	protected RenderedGameObject(XftlGameRenderer game) {
 		super(game);
 		_children = new ArrayList<RenderedGameObject>();  
 	}
 	
 	protected void addChild(RenderedGameObject child) {
 		_children.add(child);
 	}
 	
 	public void setPosition(float x, float y) {
 		float differenceX = x - getX();
 		float differenY = y - getY();
 		for(RenderedGameObject child : _children) {
 			child.setPosition(child.getX() + differenceX, child.getY() + differenY);
 		}
 	}
 	
 	@Override
 	public void update(float elapsedTime) {
 		for(RenderedGameObject gameObject : _children) {
 			gameObject.update(elapsedTime);
 		}
 	}
 	
 	public abstract float getX();
 	public abstract float getY();
 	
 	public void draw() {
 		for(RenderedGameObject child : _children) {
 			child.draw();
 		}
 	}
 }
