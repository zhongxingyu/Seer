 package com.tiny.tank;
 
 import org.newdawn.slick.*;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Vector2f;
 public class Button {
 	private Image button;
 //	private int posX;
 //	private int posY;
 	private Vector2f buttonPos;
 //	private int mousePosX;
 //	private int mousePosY;
 	
 	public Button(String path, int startingX,int startingY) {
 		try {
 			this.button=new Image(path);
 		} catch (SlickException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		this.buttonPos = new Vector2f(startingX,startingY);
 	}
 	
 	
 	public void drawButton(Graphics g) {
 		button.draw(buttonPos.x,buttonPos.y);
 		//this is just for debugging
 //		g.draw(new Rectangle(buttonPos.x,buttonPos.y,button.getWidth(),button.getHeight()));
 	}
 	public Boolean isMouseOverButton(int mousePosX,int mousePosY) {
		boolean mouseOver= mousePosX > buttonPos.x && mousePosX < buttonPos.x + button.getWidth() && mousePosY > buttonPos.y && mousePosY < buttonPos.y+button.getWidth(); 
 		return mouseOver;
 	}
 	
 }
