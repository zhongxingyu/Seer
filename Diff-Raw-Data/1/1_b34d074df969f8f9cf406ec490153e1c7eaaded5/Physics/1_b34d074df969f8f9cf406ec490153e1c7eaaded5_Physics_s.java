 package bmc.game;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.graphics.Canvas;
 import bmc.game.gameobjects.GameObject;
 import bmc.game.gameobjects.Laser;
 import bmc.game.gameobjects.Player;
 import bmc.game.gameobjects.Sprite;
 import bmc.game.level.Level;
 import bmc.game.level.Level.CollisionStates;
 import bmc.game.level.LevelManager;
 //physics class
 public class Physics {
 	private Level mLevel;
 	private LevelManager mLevelManager;
 	private List<GameObject> gameObjects = new ArrayList<GameObject>();
 	private List<GameObject> gameObjectsAdd = new ArrayList<GameObject>();
 	private Player mPlayer;
 	private Sprite[] mSprites;
 	private float gameSpeed = 3f;
 	private float fall = 1f,jump = -20f,run = 3f,stop = -1f;
 	
 	public Physics(Sprite[] sprites,LevelManager level)
 	{
 		mSprites = sprites;
 		mPlayer = new Player(sprites);
 
 		level.LoadLevels();
 		mLevel = level.getLevel(0);
 		mLevelManager = level;
 		
 		mPlayer.setX(mLevel.getStartX());
         mPlayer.setY(mLevel.getStartY());
 		
 		//gameObjects.add(new Laser(sprites, 200, 200, 300, 400));
 		
 		mLevel.setPlayerInObjects(mPlayer);
         mLevel.setListInObjects(gameObjects);
 	}
 	public void reset()
 	{
 		mPlayer.setX(mLevel.getStartX());
         mPlayer.setY(mLevel.getStartY());
         //every level starts at 0,0
         mLevel.setX(0);
         mLevel.setY(0);
         Panel.end();
 	}
 	public void logic()
 	{
 		//if(level.onGround(mPlayer.getDestination){
 		//else
 		
 		if(mPlayer.getRect().top > Panel.mHeight)
 		{
 			reset();
 		}
         for (GameObject gameObject : gameObjects) {
         	gameObject.addVelocityY(fall);
         }
 		mPlayer.addVelocityY(fall);
 		mLevel.addX(gameSpeed);
 		CollisionStates collision = mLevel.IsCollidingWithLevel(mPlayer.getRect());
 		switch(collision)
 		{
 			case BOTTOMANDLEFT:
 			case BOTTOMANDRIGHT:
 			case BOTTOM:
 				if(mPlayer.getVelocityY() > 0)
 					mPlayer.setVelocityY(0);
 				break;
 			case TOPANDLEFT:
 			case TOPANDRIGHT:
 			case TOP:
 				if(mPlayer.getVelocityY() < 0)
 					mPlayer.setVelocityY(0);
 	        	break;
 			
 		}
 		switch(collision)
 		{
 			case TOPANDLEFT:
 			case BOTTOMANDLEFT:
 			case LEFT:
 				if(mPlayer.getVelocityX() < 0)
 					mPlayer.setVelocityX(0);
 				break;
 			case BOTTOMANDRIGHT:
 			case TOPANDRIGHT:
 			case RIGHT:
 				if(mPlayer.getVelocityX() > 0)
 					mPlayer.setVelocityX(0);
 	        	break;
 		
 		}
 		
 	}
 	public void jump()
 	{
 		if(mPlayer.getmPlayerState().compareTo(PlayerState.Running) == 0)
 			mPlayer.addVelocityY(jump);
 	}
 	public void animate(long elapsedTime) {
 		// TODO Auto-generated method stub
 
 	    synchronized (gameObjects) {
 	    	logic();
 	        for (GameObject gameObject : gameObjects) {
 	        	gameObject.animate(elapsedTime);
 	        }
 
 	        synchronized (gameObjectsAdd) {
 		    	for (GameObject gameObject : gameObjectsAdd) {
 		        	gameObjects.add(gameObject);
 		        }
 	        }
 	    	gameObjectsAdd.clear();
 	    }
 		mPlayer.animate(elapsedTime);
 		//if(mPlayer.getmPlayerState().compareTo(PlayerState.Running)==0)
 			mLevel.animate(elapsedTime, gameSpeed, 0);
 		//mLevel.animate(elapsedTime, 0, 0);
 		if(mLevel.reachedEnd())
 		{
 			Panel.beatLevel();
 		}
 	    
 	}
 
 	public void doDraw(Canvas canvas) {
 		// TODO Auto-generated method stub
 		mLevel.doDraw(canvas);
 	    synchronized (gameObjects) {
 	        for (GameObject gameObject : gameObjects) {
 	        	gameObject. doDraw(canvas);
 	        }
 	    }
 	    mPlayer.doDraw(canvas);
 	}
 
 	public Level getmLevel() 
 	{
 		return mLevel;
 	}
 
 	public void setmLevel(Level mLevel) 
 	{
 		this.mLevel = mLevel;
 	}
 }
