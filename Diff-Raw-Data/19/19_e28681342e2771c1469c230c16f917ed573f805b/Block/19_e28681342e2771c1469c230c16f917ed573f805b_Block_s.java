 package com.github.spocot.mythra;
 
 import java.awt.Color;
 import java.awt.Graphics;
 
public class Block {
 	
 	//Height and width
 	private static final int width = 10;
 	private static final int height = 10;
 	
 	//Variable for collision
 	private boolean canCollide;
 	
 	//Color variable
 	private Color color;
 	
 	//Location of the block
 	private int x;
 	private int y;
 	
 	public Block(boolean canCollide, Color color, int x, int y){
 		this.canCollide = canCollide;
 		this.color = color;
 		this.x = x;
 		this.y = y;
 	}
 	
 	public void setPosition(int x, int y){
 		this.x = x;
 		this.y = y;
 	}
 	
 	public void render(Graphics g){
 		g.setColor(color);
 		g.fillRect(x, y, width, height);
 	}
 	
 	public boolean checkCollide(Player player){
 		
 		//Return false if the block is not collidable
 		if(!canCollide)return false;
 		
 		int pX = player.getX();
 		int pY = player.getY();
 		int pWidth = player.getWidth();
 		int pHeight = player.getHeight();
 		
 		//Check collision
 		if(pX + pWidth > x && pX < x + width && pY + pHeight > y && pY < y + height)
 			return true;
 		else
 			return false;
 	}
 	
 	public int getX(){return x;}
 	public int getY(){return y;}
 	
 	public boolean isCollideable(){return canCollide;}
 	
 	public Color getColor(){return color;}
 }
