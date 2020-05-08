 package com.amazon.dinorama;
 
 import android.graphics.Rect;
 
 public class Player {
 	private int health = 100;
 	private int dinoType = 0;
 	
 	private boolean facingRight = true;
 	
 	private int width = 50;
 	private int height = 50;
 	private Rect positionBox;	
 	
 	private final int speed = 5;
 	
 	public Player(int dinoType, 
 				boolean facingRight,
 				int originX,
 				int originY){
 		
 		//Origin is centerX, centerY
 		int widthOffSet = width/2;
 		int heightOffSet = height/2;
 		
 		int left = originX-widthOffSet;
 		int right = originX+widthOffSet;
 		int up = originY+heightOffSet;
 		int down = originY-heightOffSet;
 		
 		Rect sizeAndPosition = new Rect(left, up, right, down);
 		
 		this.facingRight = facingRight;
 		this.dinoType = dinoType;
 		this.positionBox = sizeAndPosition;
 	}
 	
 	public void moveRight(double deltaTime){
 		int offset = (int) (speed*deltaTime);
 		positionBox.offset(offset, 0);		
 	}
 	
 	public void moveLeft(double deltaTime){
 		int offset = (int) (speed*deltaTime);
 		positionBox.offset(-offset, 0);		
 	}
 	
 	public void flipPlayer(){
 		facingRight = false;
 	}
 	
 	public Rect getPosition(){
 		return positionBox;
 	}
 	
 	//For attacks, getPosition() then add (for right) say 50 pixels ahead or for left 50 pixels behind
	public PlayerState attackHigh(){
		return PlayerState.HIGHATTACK;
 	}
 	
	public PlayerState attackLow(){
		return PlayerState.LOWATTACK;
 	}
 	
	public PlayerState blockHigh(){
		return PlayerState.HIGHBLOCK;
 	}
 	
	public PlayerState blockLow(){
		return PlayerState.LOWBLOCK;
 	}
 }
