 package com.touchlabs.jamjam;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 
 
 
 public class GameModel implements java.io.Serializable {
 	
 	private static final long serialVersionUID = 1L;
 
 	private GroundTop groundTop;
 	private GroundMid groundMid;
 	private GroundBot groundBot;
 	//private EnemyWall enemyWall;
 	private Dront mDront;
 	private PowerUp mPowerUp;	
 	private List<EnemyWall> listEnemyWall = new ArrayList<EnemyWall>();
 	private Background mBg;
 	
 	private boolean lost = false;
 	private float incSpeedTimer = 5; //each 10 sec increase the globalGameSpeed
 	private double globalGameSpeed = 100;
 	private int distance = 0;
 	private double distanceMeter = 1;
 	private double timeTick = 0;
 
 	private Context context;
 	
 	private SoundManager mSoundManager;
 	/**
 	 * Constructor
 	 */
 	public GameModel(SoundManager sm) {
 
 		mSoundManager = sm;
 		groundTop = new GroundTop();
 		groundMid = new GroundMid();
 		groundBot = new GroundBot();
 
 		mDront = new Dront();
 		mPowerUp = new PowerUp();
 		
 		mBg = new Background();
 		
 		Random generator = new Random();
 		for(int i = 0; i < 6; i++){
 			if (i == 0 || i == 1)
 				listEnemyWall.add(new EnemyWall((generator.nextInt(9) + 1), 1));
 			if (i == 2 || i == 3)
 				listEnemyWall.add(new EnemyWall((generator.nextInt(9) + 1), 2));
 			if (i == 4 || i == 5)
 				listEnemyWall.add(new EnemyWall((generator.nextInt(9) + 1), 3));
 		}
 	}
 	
 	/**
 	 * Initializes all variables of the GameModel.
 	 */
 	public  void initialize(Context context) {
 		this.context = context;
 	
 	}
 	
 	/**
 	 * This class is called from the GameThread. 
 	 * @param timeDelta 
 	 */
 	public  void updateModel(float timeDelta) {
 
 		
 		
 		if(!lost){
 			groundTop.setXPos(timeDelta);
 			groundMid.setXPos(timeDelta);
 			groundBot.setXPos(timeDelta);
 			mDront.updateDront(timeDelta);
 			mPowerUp.updatePowerUp(timeDelta);
 			mBg.setXPos(timeDelta);
 
 			int count = listEnemyWall.size();
 			
 			if(mDront.getX() + 80 > mPowerUp.getX() && mDront.getX() < mPowerUp.getX()){
 				if(mDront.getY() + 80 > mPowerUp.getY() && mDront.getY() < mPowerUp.getY() +80){
 					if (mPowerUp.getType() == 0){
 						mDront.drontPowerUp();
 						distance += 1;
 						mSoundManager.playSound(2);
 					}
 					else if (mPowerUp.getType() == 1)  {
 						globalGameSpeed *= 0.5;
 						mDront.setAnimationWalk(1.5f);
 						//Update speed for all 
 						//Update speed for walls
 						for(int i = 0; i < count; i++){
 							listEnemyWall.get(i).setSpeed(globalGameSpeed);
 						}
 						groundTop.setSpeed(globalGameSpeed);
 						groundMid.setSpeed(globalGameSpeed);
 						groundBot.setSpeed(globalGameSpeed);
 						mPowerUp.setSpeed(globalGameSpeed);
 
 						mSoundManager.playSound(4);
 						
 					}
 					else if (mPowerUp.getType() == 2) {
 						mDront.frontFreeze();
 						mSoundManager.playSound(3);						
 					}
 					
 					mPowerUp.updatePowerUp(600);
 				}
 			}
 			
 			timeTick -= timeDelta;
 			boolean showWall = false;
 			if (timeTick <= 0) {
				timeTick = 1.5;
 				showWall = true;
 			}
 			
 			int y1 = 0;
 			int y2 = 0;
 			int y3 = 0;
 			
 			for(int i = 0; i < count; i++){
 
 				if (i == 0) {
 					boolean temp = listEnemyWall.get(i).updateWall(timeDelta,showWall);
 					if (temp)
 						y1 = 1;
 				}
 				if (i == 1) {
 					boolean temp = false;
 					if (y1 == 1)
 						temp = listEnemyWall.get(i).updateWall(timeDelta,false);
 					else {
 						temp = listEnemyWall.get(i).updateWall(timeDelta,showWall);
 						if (temp)
 							y1 = 1;
 					}
 				}
 				if (i == 2) {
 					boolean temp = listEnemyWall.get(i).updateWall(timeDelta,showWall);
 					if (temp)
 						y2 = 1;
 				}
 				if (i == 3) {
 					boolean temp = false;
 					if (y2 == 1)
 						temp = listEnemyWall.get(i).updateWall(timeDelta,false);
 					else {
 						temp = listEnemyWall.get(i).updateWall(timeDelta,showWall);
 						if (temp)
 							y2 = 1;
 					}
 				}
 				if (i == 4) {
 					boolean temp = false;
 					if (y1 == 1 && y2 == 1)
 						temp = listEnemyWall.get(i).updateWall(timeDelta,false);
 					else 
 						temp = listEnemyWall.get(i).updateWall(timeDelta,showWall);
 					if (temp)
 						y3 = 1;
 				}
 				if (i == 5) {
 					boolean temp = false;
 					if (y3 == 1)
 						temp = listEnemyWall.get(i).updateWall(timeDelta,false);
 					else {
 						if (y1 == 1 && y2 == 1) 
 							temp = listEnemyWall.get(i).updateWall(timeDelta,false);
 						else 
 							temp = listEnemyWall.get(i).updateWall(timeDelta,showWall);
 						if (temp)
 							y3 = 1;
 					}
 				} 
 
 			}
 			
 			showWall = false;
 			
 			incSpeedTimer -= timeDelta;
 			if(incSpeedTimer < 0){
 				globalGameSpeed *= 1.1;
 				mDront.setAnimationWalk(0.9f);
 				//Update speed for all 
 				//Update speed for walls
 				for(int i = 0; i < count; i++){
 					listEnemyWall.get(i).setSpeed(globalGameSpeed);
 				}
 				groundTop.setSpeed(globalGameSpeed);
 				groundMid.setSpeed(globalGameSpeed);
 				groundBot.setSpeed(globalGameSpeed);
 				mPowerUp.setSpeed(globalGameSpeed);
 				
 				incSpeedTimer = 5;
 			}
 			
 			//Score in distance
 			distanceMeter += timeDelta * globalGameSpeed * 0.01;
 			if(distanceMeter > 2){
 				distance += 1;
 				distanceMeter = 1;
 			}
 			
 			
 			for(int i = 0; i < count; i++){
 				if(mDront.getX() + 80 > listEnemyWall.get(i).getX1() && mDront.getX() < listEnemyWall.get(i).getX1()){
 					if(mDront.getY() + 80 > listEnemyWall.get(i).getY() && mDront.getY() < listEnemyWall.get(i).getY() +80){
 						mDront.hitDront();
 						mSoundManager.playSound(1);
 						distance -= 1;
 						listEnemyWall.get(i).setNewRandomTime();	
 					}
 				}
 			}
 			
 			//Lost?
 			if(mDront.getX() == 0){
 				lost = true;
 			}
 		}
 
 
 	}
 	
 	public Background getBackgroun(){
 		return mBg;
 	}
 	
 	public int getDistance(){
 		return distance;
 	}
 	
 	public boolean getLost(){
 		return lost;
 	}
 	//Return the dront
 	public Dront getDront() {
 		return mDront;
 	}
 
 	//Return the powerup
 	public PowerUp getPowerUp() {
 		return mPowerUp;
 	}
 
 	public GroundTop getGroundTop(){
 		return groundTop;
 	}
 	
 	public GroundMid getGroundMid(){
 		return groundMid;
 	}
 	
 	public GroundBot getGroundBot(){
 		return groundBot;
 	}
 	
 	public List<EnemyWall> getListEnemyWall(){
 		return listEnemyWall;
 	}
 
 	public  void release() {
 
 		// TODO Auto-generated method stub
 		
 
 	}
 }
