 package com.vulcastudios.actors;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.state.StateBasedGame;
 
import com.vulcastudios.util.ResourceManager;

 public class Door {
 
 	private String name;
 
 	private int x;
 	private int y;
 	private int width;
 	private int height;
 
 	private boolean open;
 	private boolean initialOpen;
 
	private ResourceManager rm;
 
	public Door(ResourceManager rm, String name, int x, int y, int width, int height, String initialState){
 		this.x = x;
 		this.y = y;
 		this.width = width;
 		this.height = height;
		this.rm = rm;
 
 		if(initialState.equals("open")){
 			open = true;
 			this.setInitialOpen(true);
 		}else{
 			open = false;
 			this.setInitialOpen(false);
 		}
 
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public int getX() {
 		return x;
 	}
 
 	public void setX(int x) {
 		this.x = x;
 	}
 
 	public int getY() {
 		return y;
 	}
 
 	public void setY(int y) {
 		this.y = y;
 	}
 
 	public int getWidth() {
 		return width;
 	}
 
 	public void setWidth(int width) {
 		this.width = width;
 	}
 
 	public int getHeight() {
 		return height;
 	}
 
 	public void setHeight(int height) {
 		this.height = height;
 	}
 
 	public boolean isOpen() {
 		return open;
 	}
 
 	public void setOpen(boolean open) {
 		this.open = open;
 	}
 
 	public void render(GameContainer container, StateBasedGame game, Graphics g){
 
 		Color prev = g.getColor();
 		if(this.isOpen())
 			g.setColor(Color.white);
 		else
 			g.setColor(Color.orange);
 		g.drawRect(this.getX(), this.getY(), this.getWidth(), this.getHeight());
 		g.setColor(prev);
 
 	}
 
 	public Rectangle getBounds(){
 		return new Rectangle(this.getX(), this.getY(), this.getWidth(), this.getHeight());
 	}
 
 	public boolean isInitialOpen() {
 		return initialOpen;
 	}
 
 	public void setInitialOpen(boolean initialOpen) {
 		this.initialOpen = initialOpen;
 	}
 
 
 }
