 package es.dev.game.app;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import org.cocos2d.actions.instant.CCCallFuncN;
 import org.cocos2d.actions.interval.CCMoveTo;
 import org.cocos2d.actions.interval.CCSequence;
 import org.cocos2d.layers.CCColorLayer;
 import org.cocos2d.layers.CCScene;
 import org.cocos2d.nodes.CCDirector;
 import org.cocos2d.nodes.CCLabel;
 import org.cocos2d.nodes.CCNode;
 import org.cocos2d.nodes.CCSprite;
 import org.cocos2d.nodes.CCLabel.TextAlignment;
 import org.cocos2d.types.CGPoint;
 import org.cocos2d.types.CGRect;
 import org.cocos2d.types.CGSize;
 import org.cocos2d.types.ccColor4B;
 import org.cocos2d.sound.*;
 
 
 import android.content.Context;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.view.SoundEffectConstants;
 import android.widget.TextView;
 
 import static es.dev.game.config.Constants.*;
 
 public class GameLayer extends CCColorLayer implements SensorEventListener{
 
 	private Context context; 
	private  static CCLabel punctuation;
 	protected ArrayList<CCSprite> targets;
 	
 	//private static CCSprite background;
     private static CCSprite player;
     private static CGPoint point;
     private static CGSize winSize;
     private static float playerWidth;
     private static float playerHeigth;
    private static int points;
 	
 	protected GameLayer(ccColor4B color) {
 		super(color);
 		
 		context = CCDirector.sharedDirector().getActivity();
 		
 		punctuation = CCLabel.makeLabel("Ptos: 0", CGSize.make(100.0f, 25.0f), TextAlignment.CENTER, "DEFAULT", 20.0f);
 		punctuation.setPosition(CGPoint.make(720, 450));
 		addChild(punctuation);
	
         this.registerWithAccelerometer();
         
         targets = new ArrayList<CCSprite>();
         
 		winSize = CCDirector.sharedDirector().displaySize();
 		player = CCSprite.sprite("player.png");
 		
 		playerWidth = player.getContentSize().width;
 		playerHeigth = player.getContentSize().height; 
 		 
 		point = CGPoint.ccp(winSize.width / 2.0f, winSize.height / 2.0f);
 		player.setPosition(point);
 		 
 		addChild(player);
 		
 		/*Adds a background to the scene*/
 		//background = CCSprite.sprite("background.png");		//TODO: puede ser en 2D!!!
 		//background.setPosition(CGPoint.ccp(winSize.width / 2.0f, winSize.height / 2.0f));
 		//addChild(background, 0);
 		/*
 		 * adds a call to the gameLogic method every 1/2 second
 		 */
 		this.schedule("gameLogic", 0.5f);
 		this.schedule("update");
 	}
 
 	/**
 	 * Creates a new scene 
 	 * @return
 	 */
 	public static CCScene scene(){
 		CCScene scene = CCScene.node();
 		CCColorLayer layer = new GameLayer(ccColor4B.ccc4(0, 0, 0, 0));
 
 		scene.addChild(layer);
 		
 		return scene;
 	}
 	
 	/**
 	 * Adds a new object to the scene
 	 */
 	protected void addTarget(){
 		Random rand = new Random();
 		CCSprite target = CCSprite.sprite("target.png");
 		
 		//Determine where to spawn the target along the y axis
 		CGSize winSize = CCDirector.sharedDirector().displaySize();
 		int minY = (int)(target.getContentSize().height /2.0f);
 		int maxY = (int)(winSize.height - target.getContentSize().height / 2.0f);
 	    int rangeY = maxY - minY;
 	    int actualY = rand.nextInt(rangeY) + minY;
 	 
 	    // Create the target slightly off-screen along the right edge,
 	    // and along a random position along the Y axis as calculated above
 	    CGPoint pos = new CGPoint();
 	    pos.x = winSize.width + (target.getContentSize().width / 2.0f);
 	    pos.y = actualY;
 	    target.setPosition(pos);
 	    addChild(target);
 	    
 	    targets.add(target);
 	 
 	    // Determine speed of the target
 	    int minDuration = 2;
 	    int maxDuration = 4;
 	    int rangeDuration = maxDuration - minDuration;
 	    int actualDuration = rand.nextInt(rangeDuration) + minDuration;
 	 
 	    // Create the actions
 	    CCMoveTo actionMove = CCMoveTo.action(actualDuration, CGPoint.ccp(-target.getContentSize().width / 2.0f, actualY));
 	    CCCallFuncN actionMoveDone = CCCallFuncN.action(this, "spriteMoveFinished");
 	    CCSequence actions = CCSequence.actions(actionMove, actionMoveDone);
 	 
 	    target.runAction(actions);
 	}
 	
 	/**
 	 * Destroys a sprite when it comes over the screen
 	 * @param sender
 	 */
 	public void spriteMoveFinished(Object sender)
 	{
 	    CCSprite sprite = (CCSprite)sender;
 	    this.removeChild(sprite, true);
 	}
 	
 	/**
 	 * Adds the game logic to plays against
 	 * @param dt
 	 */
 	public void gameLogic(float dt)
 	{
 	    addTarget();
 	}
 	
 	/**
 	 * Not implemented
 	 */
 	@Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		if (DEBUG)
 			System.out.println("GameLayer.Class,  OnAccuracyChanged");    }
 
 	/**
 	 * Manage Accelerometer to gameLogic
 	 */
 	@Override
     public void onSensorChanged(SensorEvent event) {
 		if (DEBUG)
 			System.out.println("GameLayer.Class,  OnSensorChanged");
 		if (point.x >= playerWidth && point.x <= (winSize.width - playerWidth)){			//Dentro de las posiciones validas del eje X
 			point.x += event.values[AxisX];
 		}else{
 			if ( point.x < playerWidth && point.x + event.values[AxisX] >= point.x){		//Retorno de minima pos eje X
 				point.x += event.values[AxisX];
 			}else if ( point.x > (winSize.width - playerWidth) && point.x + event.values[AxisX] <= point.x ){		//Retorno de max. pos eje y
 				point.x += event.values[AxisX];
 			}
 		}
 		
 		if (point.y >= playerHeigth && point.y <= (winSize.height - playerHeigth)){			//Dentro de las posiciones validas del eje X
 			point.y -= event.values[AxisY];
 		}else {
 			if ( point.y < playerHeigth && point.y - event.values[AxisY] >= point.y){		//Retorno de minima pos eje Y
 				point.y -= event.values[AxisY];
 			}else if ( point.y > (winSize.height - playerHeigth) && point.y - event.values[AxisY] <= point.y ){		//Retorno de max. pos eje y
 				point.y -= event.values[AxisY];
 			}
 		}
 		player.setPosition(point);
 	}
 	
 	/**
 	 * Updates the IU calculating the collisions
 	 * @param dt
 	 */
 	public void update(float dt){
 		/*
 		 * Creates a RECT for player
 		*/ 
 		 CGRect playerRect = CGRect.make(player.getPosition().x - (player.getContentSize().width / 2.0f),
                  player.getPosition().y - (player.getContentSize().height / 2.0f),
                  player.getContentSize().width -25,
                  player.getContentSize().height - 20);		
 
 		 /* Search for intersections between player and enemies*/
         for (CCSprite target : targets){
             CGRect targetRect = CGRect.make(target.getPosition().x - (target.getContentSize().width),
                                             target.getPosition().y - (target.getContentSize().height),
                                             target.getContentSize().width,
                                             target.getContentSize().height);
  
             if (CGRect.intersects(playerRect, targetRect)){
             	SoundEngine.sharedEngine().playEffect(context, R.raw.eating);
             	targets.remove(target);
             	removeChild(target, true);
             	points++;
            	removeChild(punctuation, true);
            	punctuation = CCLabel.makeLabel("Ptos: "+points, CGSize.make(100.0f, 25.0f), TextAlignment.CENTER, "DEFAULT", 20.0f);
        		punctuation.setPosition(CGPoint.make(720, 450));
        		addChild(punctuation);
             }
         }
 	    
 	}
 
 	
 	/**
 	 * Called when go out from this layer
 	 */
 	@Override
 	public void onExit() {
 		super.onExit();
 		this.unregisterWithAccelerometer();
 	}
 
 }
