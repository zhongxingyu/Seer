 package com.amazon.dinorama;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 
 public class Player extends DisplayableObject {
 	//Images
 	private HashMap<Integer, ArrayList<Bitmap>> playerImages;
 	
 	private void initImageMap(){
 		playerImages = new HashMap<Integer, ArrayList<Bitmap>>();
 		Bitmap idle, step1, step2, hA1, hA2, lA1, lA2, hB, lB;
 		if(dinoType == 1){
 			playerImages.put(Integer.valueOf(1), new ArrayList<Bitmap>());
 			idle = BitmapFactory.decodeResource(res, R.drawable.stego_1);
 			hA1 = BitmapFactory.decodeResource(res, R.drawable.stego_2);
 			hA2 = BitmapFactory.decodeResource(res, R.drawable.stego_3);
 			lA1 = BitmapFactory.decodeResource(res, R.drawable.stego_4);
 			lA2 = BitmapFactory.decodeResource(res, R.drawable.stego_5);
 			step1 = BitmapFactory.decodeResource(res, R.drawable.stego_6);
 			step2 = BitmapFactory.decodeResource(res, R.drawable.stego_7);
 			
 			playerImages.get(dinoType).add(idle);
 			playerImages.get(dinoType).add(hA1);
 			playerImages.get(dinoType).add(hA2);
 			playerImages.get(dinoType).add(lA1);
 			playerImages.get(dinoType).add(lA2);
 			playerImages.get(dinoType).add(step1);
 			playerImages.get(dinoType).add(step2);
 			
 			//Add Blocking
 		}
 		
 	}
 	
 	//Constants
 	private final int speed = 4;
 	
 	//Stats
 	private int dinoType = DinoNumWrapper.dinoNum;
 	private int health = 100;
 
 	//State
 	private boolean buttonPressed = false;
 	private boolean attackComplete = true;
 	private PlayerState currentState = PlayerState.IDLE;
 	private int stateCounter = 0;
 	
 	public Player(Resources res, int originX, int originY){
		super(res, originX, originY);
 		initImageMap();
 	}
 	
 	
 	private void idle(){
 		ArrayList<Bitmap> images = playerImages.get(dinoType);
 		setImageDisplayed(images.get(0));
 		stateCounter = 0;
 	}
 		
 	private void step(){		
 		ArrayList<Bitmap> images = playerImages.get(dinoType);
 		
 		if((stateCounter >= 0) && (stateCounter < 19)){
 			setImageDisplayed(images.get(5));
 		}else if((stateCounter >= 19) && (stateCounter < 38)){
 			setImageDisplayed(images.get(6));
 		}else{
 			stateCounter = 0;
 		}
 	}
 	
 	private void movementRight(){
 		step();
 		originX += speed;
 	}
 	private void movementLeft(){
 		step();
 		originX -= speed;
 	}
 	
 	private void attackingHigh(){
 		ArrayList<Bitmap> images = playerImages.get(dinoType);
 		
 		if((stateCounter >= 0) && (stateCounter < 5)){
 			setImageDisplayed(images.get(1));
 		}else if((stateCounter >= 5) && (stateCounter < 20)){
 			setImageDisplayed(images.get(2));
 		}else if((stateCounter >= 20) && (stateCounter < 25)){
 			setImageDisplayed(images.get(1));
 		}else{
 			setImageDisplayed(images.get(0));
 			attackComplete = true;
 		}
 	}
 	
 	private void attackingLow(){
 ArrayList<Bitmap> images = playerImages.get(dinoType);
 		
 		if((stateCounter >= 0) && (stateCounter < 5)){
 			setImageDisplayed(images.get(3));
 		}else if((stateCounter >= 5) && (stateCounter < 20)){
 			setImageDisplayed(images.get(4));
 		}else if((stateCounter >= 20) && (stateCounter < 25)){
 			setImageDisplayed(images.get(3));
 		}else{
 			setImageDisplayed(images.get(0));
 			attackComplete = true;
 		}
 	}
 
 	public void update(){
 		stateCounter++;
 		if(currentState == PlayerState.IDLE){
 			idle();
 		}
 
 		if(buttonPressed){
 			if(currentState == PlayerState.MOVERIGHT){
 				movementRight();
 			}else if(currentState == PlayerState.MOVELEFT){
 					movementLeft();
 			}else if(currentState == PlayerState.HIGHATTACK){
 				if(attackComplete == false){
 					attackingHigh();
 				}
 			}else if(currentState == PlayerState.LOWATTACK){
 				if(attackComplete == false){
 					attackingLow();
 				}
 			}
 		}
 
 	}	
 	
 	public void forceIdle(){
 		buttonPressed = false;
 		stateCounter = 0;
 		currentState = PlayerState.IDLE;
 		
 	}
 	
 	public void moveRight(){
 		buttonPressed = true;
 		currentState = PlayerState.MOVERIGHT;
 	}
 	
 	public void moveLeft(){
 		buttonPressed = true;
 		currentState = PlayerState.MOVELEFT;
 	}
 	
 	public void attackHigh(){
 		buttonPressed = true;
 		attackComplete = false;
 		currentState = PlayerState.HIGHATTACK;	
 	}
 	
 	public void attackLow(){
 		buttonPressed = true;
 		attackComplete = false;
 		currentState = PlayerState.LOWATTACK;
 	}
 		
 
 }
