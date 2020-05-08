 package com.spotlightcoding;
  
 import java.util.ArrayList;
 
 import java.awt.Dimension;
  
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.geom.Shape;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.state.StateBasedGame;
 
 import com.spotlightcoding.components.ImageRenderComponent;
 
 public class Entity {
 	//------------------------------CONSTANTS
 	public static final int NORMAL = 0;
 	public static final int FALLING = 1;
 	public static final int JUMPING = 2;
 	public static final int DEAD = 3;
 	
 	
 	
     private String id;
     
     private Dimension size;
     private Vector2f position;
     private float scale;
     private float rotation;
     private int state;
     
  
     ImageRenderComponent renderComponent;
  
     ArrayList<Component> components;
  
     public Entity(String id)
     {
         this.id = id;
         this.state = NORMAL;
         components = new ArrayList<Component>();
         position = new Vector2f(0,0);
         scale = 1;
         rotation = 0;
     }
  
     public void AddComponent(Component component)
     {
         if(ImageRenderComponent.class.isInstance(component)){
         	renderComponent = (ImageRenderComponent)component;
         	size.setSize(renderComponent.getImage().getWidth(), renderComponent.getImage().getHeight());
         }
             
         component.setOwnerEntity(this);
         components.add(component);
     }
  
     public Component getComponent(String id)
     {
         for(Component comp : components)
 		{
 		    if ( comp.getId().equalsIgnoreCase(id) ) {
 		    	return comp;
 		    }
 		}
         return null;
     }
  
     public Vector2f getPosition()
     {
     	return position;
     }
  
     public float getScale()
     {
     	return scale;
     }
  
     public float getRotation()
     {
     	return rotation;
     }
  
     public String getId()
     {
     	return id;
     }
     public int getState()
     {
     	return state;
     }
     
     public Dimension getSize() {
     	return size;
     }
     
     public  void setState(int myState){
     	this.state = myState;
     }
  
     public void setPosition(Vector2f position) {
     	this.position = position;
     }
  
     public void setRotation(float rotate) {
         rotation = rotate;
     }
  
     public void setScale(float scale) {
     	this.scale = scale;
     }
  
     public void update(GameContainer gc, StateBasedGame sb, int delta)
     {
         for(Component component : components)
         {
             component.update(gc, sb, delta);
         }
     }
  
     public void render(GameContainer gc, StateBasedGame sb, Graphics gr)
     {
         if(renderComponent != null) {
         	renderComponent.render(gc, sb, gr);
         }
     }
 }
