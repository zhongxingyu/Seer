 package com.codefest2013.game.scenes.objects;
 
 import org.andengine.entity.Entity;
 import org.andengine.entity.scene.IOnSceneTouchListener;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.sprite.AnimatedSprite;
 import org.andengine.input.touch.TouchEvent;
 
 import com.codefest2013.game.MainActivity;
 import com.codefest2013.game.ResourcesManager;
 
 public class Player extends Entity implements IOnSceneTouchListener {
     public final static int LEFT_DIRECTION = 0;
     public final static int RIGHT_DIRECTION = 1;
     
     private final float SPEED = ResourcesManager.CAMERA_WIDTH*0.35f;
     private final long ANIMATION_SPEED = 90;
     private final float RATIO = 1.7f;
     
     private AnimatedSprite leftSprite;
     private AnimatedSprite rightSprite;
     
     private float speed = 0;
     private int currentDirection = LEFT_DIRECTION;
     private int fingersNumber = 0;
     
     public Player(float x, float y)
     {   
     	super(x, y);
     	
         float height = ResourcesManager.CAMERA_HEIGHT/6;
         
         leftSprite = new AnimatedSprite(0, 0, RATIO*height, height, 
         		ResourcesManager.getInstance().goblinTiledLeftWalk,
         		MainActivity.getInstance().getVertexBufferObjectManager());
         
         rightSprite = new AnimatedSprite(0, 0, RATIO*height, height,
         		ResourcesManager.getInstance().goblinTiledRightWalk,
         		MainActivity.getInstance().getVertexBufferObjectManager());
 
 		leftSprite.setVisible(true);
 		rightSprite.setVisible(false);
 		attachChild(leftSprite);
 		attachChild(rightSprite);
     }
     
     @Override
     protected void onManagedUpdate(float pSecondsElapsed) {
     	super.onManagedUpdate(pSecondsElapsed);
     	move(pSecondsElapsed);
     }
 
 	@Override
 	public boolean onSceneTouchEvent(Scene arg0, TouchEvent arg1) {
         switch( arg1.getAction() )
         {
             case TouchEvent.ACTION_DOWN:
                 ++fingersNumber;
                 if( arg1.getX() < MainActivity.getInstance().mCamera.getCenterX() )
                 {
                 	setDirection(LEFT_DIRECTION);
                 }
                 else
                 {
                 	setDirection(RIGHT_DIRECTION);
                 }
                 return true;
             case TouchEvent.ACTION_UP:
                 if( --fingersNumber == 0 )
                 {
                 	stop();
                 }
                 return true;
         }
         return false;
 	}
     
     private void setDirection( int direction )
     {
         currentDirection = direction;
         speed = SPEED;
         
         switch( currentDirection )
         {
             case LEFT_DIRECTION:
            	rightSprite.setVisible(false);
         		leftSprite.setVisible(true);
         		leftSprite.animate(ANIMATION_SPEED);
                 break;
             case RIGHT_DIRECTION:
         		leftSprite.setVisible(false);
         		rightSprite.setVisible(true);
         		rightSprite.animate(ANIMATION_SPEED);
                 break;
         }
     }
     
     private void move(float pSecondsElapsed)
     {
         if( getX() < 0.0f )
         {
             setPosition(getX()+speed*pSecondsElapsed, getY());
             return;
         }
         if( getX() > ResourcesManager.WORLD_WIDTH - leftSprite.getWidth() )
         {
             setPosition(getX()-speed*pSecondsElapsed, getY());
             return;
         }
         switch(currentDirection)
         {
             case LEFT_DIRECTION:
                 setPosition(getX()-speed*pSecondsElapsed, getY());
                 break;
             case RIGHT_DIRECTION:
                 setPosition(getX()+speed*pSecondsElapsed, getY());
                 break;
         }
     }
     
     private void stop()
     {
         speed = 0;
         switch(currentDirection)
         {
             case LEFT_DIRECTION:
             	leftSprite.stopAnimation(0);
                 break;
             case RIGHT_DIRECTION:
                 rightSprite.stopAnimation(0);
                 break;
         }
     }
 }
