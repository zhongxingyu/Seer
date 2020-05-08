 package net.thempra.overgame;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import net.thempra.overmind.MainActivity;
 import net.thempra.overmind.R;
 import net.thempra.overmind.R.raw;
 
 import org.cocos2d.actions.instant.CCCallFuncN;
 import org.cocos2d.actions.interval.CCMoveTo;
 import org.cocos2d.actions.interval.CCSequence;
 import org.cocos2d.layers.CCColorLayer;
 import org.cocos2d.layers.CCScene;
 import org.cocos2d.nodes.CCDirector;
 import org.cocos2d.nodes.CCSprite;
 import org.cocos2d.sound.SoundEngine;
 import org.cocos2d.types.CGPoint;
 import org.cocos2d.types.CGRect;
 import org.cocos2d.types.CGSize;
 import org.cocos2d.types.ccColor4B;
 
 import com.androidplot.series.XYSeries;
 import com.androidplot.xy.LineAndPointFormatter;
 import com.androidplot.xy.SimpleXYSeries;
 import com.androidplot.xy.XYPlot;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.os.Handler;
 import android.os.Looper;
 import android.os.Message;
 import android.view.MotionEvent;
 
 public class GameLayer extends CCColorLayer
 {
 	private  int MULTIPLIER_COUNTER =2;
 	protected ArrayList<CCSprite> _targets;
 	protected ArrayList<CCSprite> _projectiles;
 	protected int _projectilesDestroyed;
 	
 	private static int selectedP1=-1;
 	private static int selectedP2=-1;
 	
 	private int powerP1, powerP2, powerBar;
 	CCSprite player1, player2, powerBarSprite; 
 	//private Handler mHandler;
 	private static boolean pause=true;
 	
 	
 	Timer timer;
 	public static CCScene scene()
 	{
 		CCScene scene = CCScene.node();
 		CCColorLayer layer = new GameLayer(ccColor4B.ccc4(255, 255, 255, 255));
 		
 		scene.addChild(layer);
 		
 		
 		return scene;
 	}
 	
 	protected GameLayer(ccColor4B color)
 	{
 		super(color);
 		
 		this.setIsTouchEnabled(true);
 		
 		_targets = new ArrayList<CCSprite>();
 		_projectiles = new ArrayList<CCSprite>();
 		_projectilesDestroyed = 0;
 		
 		CGSize winSize = CCDirector.sharedDirector().displaySize();
 		 player1 = CCSprite.sprite("eva01.png");
 		 player2 = CCSprite.sprite("sachiel.png");
 		 powerBarSprite= CCSprite.sprite("powerbar.png");
 		
 		
 
 		CCSprite bckgImage = CCSprite .sprite("game_background2.jpg");
 		bckgImage.setScaleX( winSize.width /  bckgImage.getContentSize().width);
 		bckgImage.setScaleY( winSize.height /  bckgImage.getContentSize().height);
 		bckgImage.setPosition(CGPoint.ccp(winSize.width / 2.0f, winSize.height / 2.0f));
 		
 		addChild(bckgImage);
 		
 		
 		powerP1=powerP2=0;
 		powerBar =(int) winSize.width/2;
 		
 		player1.setPosition(CGPoint.ccp(player1.getContentSize().width/2.0f, winSize.height / 2.0f));
 		player2.setPosition(CGPoint.ccp(winSize.width - player2.getContentSize().width/2.0f  , winSize.height / 2.0f));
 		powerBarSprite.setPosition(powerBar  ,CCDirector.sharedDirector().displaySize().height-powerBarSprite.getContentSize().height/2.0f);
 		
 		
 		addChild(player1);
 		addChild(player2);
 		addChild(powerBarSprite);
 		
 		
 		
 		// Handle sound
 		Context context = CCDirector.sharedDirector().getActivity();
 		SoundEngine.sharedEngine().preloadEffect(context, R.raw.pew_pew_lei);
 		//SoundEngine.sharedEngine().playSound(context, R.raw.background_music_aac, true);
 		
 		this.schedule("gameLogic", 1.0f);
 		this.schedule("update");
 		this.schedule("projectileLogic", 0.2f);
 		
 	/*	
 		try
 		{
 			if (mHandler == null)
 			{
 				 //Looper.prepare();
 				mHandler = new Handler();
 				mHandler.removeCallbacks(mGetMindValues);
 			    mHandler.postDelayed(mGetMindValues, 1000);
 			}
 		}catch (Exception e)
 		{
 			System.out.print(e.getMessage());
 		}
 	   
 	    */
 	}
 	
 	
 
 	@Override
 	public boolean ccTouchesEnded(MotionEvent event)
 	{
 		// Choose one of the touches to work with
 		//CGPoint location = CCDirector.sharedDirector().convertToGL(CGPoint.ccp(event.getX(), event.getY()));
 		
 		return projectileP1() &&  projectileP2() ;
 	}
 
 	
 	/**************** projectile P1 ********************/
 
 	private boolean projectileP1() {
 		// Set up initial location of projectile
 		CGSize winSize = CCDirector.sharedDirector().displaySize();
 		
 		CCSprite projectile = CCSprite.sprite("fireball_blue.png");
 
 		
 		projectile.setPosition(60, winSize.height / 2.0f);
 		
 	/*	// Determine offset of location to projectile
 		int offX = (int)(location.x - projectile.getPosition().x);
 		int offY = (int)(location.y - projectile.getPosition().y);
 		
 		
 		// Bail out if we are shooting down or backwards
 		if (offX <= 0)
 			return true;
 		*/
 		// Ok to add now - we've double checked position
 		addChild(projectile);
 		
 		projectile.setTag(2);
 		_projectiles.add(projectile);
 		
 		// Determine where we wish to shoot the projectile to ( Diana location )
 		//int realX = (int) player2.getPosition().x;
 		int realX =powerBar;
 	//	float ratio = (float)offY / (float)offX;
 		//int realY = (int)((realX * ratio) + projectile.getPosition().y);
 		
 		Random r=new Random();
 		int rMax =(int) (player2.getContentSize().height/2);
 		int rMin= -(int) (player2.getContentSize().height/2);
 		int rnd=(r.nextInt(rMax-rMin)+rMin);
 		int realY = (int) player2.getPosition().y + rnd ;
 		
 		CGPoint realDest = CGPoint.ccp(realX, realY);
 		
 		
 		
 		// Determine the length of how far we're shooting
 		int offRealX = (int)(realX - projectile.getPosition().x);
 		int offRealY = (int)(realY - projectile.getPosition().y);
 		float length = (float)Math.sqrt((offRealX * offRealX) + (offRealY * offRealY));
 		float velocity = 480.0f / 1.0f; // 480 pixels / 1 sec
 		float realMoveDuration = length / velocity;
 		
 		// Move projectile to actual endpoint
 		projectile.runAction(CCSequence.actions(
 				CCMoveTo.action(realMoveDuration, realDest),
 				CCCallFuncN.action(this, "spriteMoveFinished")));
 		
 		// Pew!
 		Context context = CCDirector.sharedDirector().getActivity();
 		SoundEngine.sharedEngine().playEffect(context, R.raw.pew_pew_lei);
 		
 		return true;
 	}
 	
 	
 	/**************** projectile P2 ********************/
 	
 	private boolean projectileP2() {
 		// Set up initial location of projectile
 		CGSize winSize = CCDirector.sharedDirector().displaySize();
 		CCSprite projectile = CCSprite.sprite("fireball.png");
 		
 		
 		projectile.setPosition(winSize.width-60, winSize.height / 2.0f);
 		
 /*		// Determine offset of location to projectile
 		int offX = (int)(projectile.getPosition().x - location.x  );
 		int offY = (int)(projectile.getPosition().y - location.y );
 		
 		
 		// Bail out if we are shooting down or backwards
 		//if (offX >= winSize.height)
 		if (offX <= 0)
 			return true;
 	*/	
 		// Ok to add now - we've double checked position
 		addChild(projectile);
 		
 		projectile.setTag(2);
 		_projectiles.add(projectile);
 		
 		// Determine where we wish to shoot the projectile to ( Diana location )
 		//int realX = (int) player1.getPosition().x;
 		int realX =powerBar;
 		//float ratio = (float)offY / (float)offX;
 		
 		Random r=new Random();
 		int rMax =(int) (player1.getContentSize().height/2);
 		int rMin= -(int) (player1.getContentSize().height/2);
 		int rnd=(r.nextInt(rMax-rMin)+rMin);
 		
 		int realY = (int) player1.getPosition().y + rnd ;
 		CGPoint realDest = CGPoint.ccp(realX, realY);
 		
 
 		// Determine the length of how far we're shooting
 		int offRealX = (int)(realX - projectile.getPosition().x);
 		int offRealY = (int)(realY - projectile.getPosition().y);
 		float length = (float)Math.sqrt((offRealX * offRealX) - (offRealY * offRealY));
 		float velocity = 480.0f / 1.0f; // 480 pixels / 1 sec
 		float realMoveDuration = length / velocity;
 		
 		// Move projectile to actual endpoint
 		projectile.runAction(CCSequence.actions(
 				CCMoveTo.action(realMoveDuration, realDest),
 				CCCallFuncN.action(this, "spriteMoveFinished")));
 		
 		// Pew!
 		Context context = CCDirector.sharedDirector().getActivity();
 		SoundEngine.sharedEngine().playEffect(context, R.raw.pew_pew_lei);
 		
 		return true;
 	}
 	
 	public void gameLogic(float dt)
 	{
 		//Only for DEMO PLAY
 	/*	Random r = new Random();		
 		if ((selectedP1==-1) && (!pause))
 			powerP1=r.nextInt(200-30)+30/MULTIPLIER_COUNTER;
 		if ((selectedP2==-1) && (!pause))
 			powerP2=r.nextInt(100-30)+30/MULTIPLIER_COUNTER;
 		*/
 		
 		this.powerP1= (MainActivity.currentEeg[selectedP1].getAttention() + MainActivity.currentEeg[selectedP1].getMeditation())/MULTIPLIER_COUNTER;
		this.powerP2= (MainActivity.currentEeg[selectedP2].getAttention() + MainActivity.currentEeg[selectedP2].getMeditation())/MULTIPLIER_COUNTER;
 		/*
 		try
 		{
 		 if ((selectedP1>=0) && (!pause))
 			{
 				powerP1= (MainActivity.currentEeg[selectedP1].getAttention() + MainActivity.currentEeg[selectedP1].getMeditation())/MULTIPLIER_COUNTER;
 			}
 		 if ((selectedP2>=0) && (!pause))
 			{
 				powerP2= (MainActivity.currentEeg[selectedP2].getAttention()+MainActivity.currentEeg[selectedP2].getMeditation())/MULTIPLIER_COUNTER	;
 			}
 		}catch (Exception e)
 		{
 			System.out.print(e.getMessage());
 		}
 		*/
 		powerBar=powerBar+ (this.powerP1-this.powerP2);
 		powerBarSprite.setPosition(powerBar  ,CCDirector.sharedDirector().displaySize().height-powerBarSprite.getContentSize().height/2.0f);
 		
 	}
 	
 	public void projectileLogic(float dt)
 	{
 		
 		for(int i=0;i< this.powerP1/20;i++)
 			projectileP1();
 	
 		for(int i=0;i< this.powerP2/20;i++)
 			projectileP2();
 	}
 	
 	public void update(float dt)
 	{
 		ArrayList<CCSprite> projectilesToDelete = new ArrayList<CCSprite>();
 		
 		//powerP1= getValueForDemoMode(1);
 		//powerP2= getValueForDemoMode(2);
 		
 		
 		
 		for (CCSprite projectile : _projectiles)
 		{
 			CGRect projectileRect = CGRect.make(projectile.getPosition().x - (projectile.getContentSize().width / 2.0f),
 												projectile.getPosition().y - (projectile.getContentSize().height / 2.0f),
 												projectile.getContentSize().width,
 												projectile.getContentSize().height);
 			
 			ArrayList<CCSprite> targetsToDelete = new ArrayList<CCSprite>();
 			
 			for (CCSprite target : _targets)
 			{
 				CGRect targetRect = CGRect.make(target.getPosition().x - (target.getContentSize().width),
 												target.getPosition().y - (target.getContentSize().height),
 												target.getContentSize().width,
 												target.getContentSize().height);
 				
 				if (CGRect.intersects(projectileRect, targetRect))
 					targetsToDelete.add(target);
 			}
 			
 			
 			for (CCSprite target : targetsToDelete)
 			{
 				_targets.remove(target);
 				removeChild(target, true);
 			}
 			
 			if (targetsToDelete.size() > 0)
 				projectilesToDelete.add(projectile);
 		}
 		/*
 		for (CCSprite projectile : projectilesToDelete)
 		{
 			_projectiles.remove(projectile);
 			removeChild(projectile, true);
 			
 			if (++_projectilesDestroyed > 30)
 			{
 				_projectilesDestroyed = 0;
 				CCDirector.sharedDirector().replaceScene(GameOverLayer.scene("You Win!"));
 			}
 		}*/
 		
 		
 		if (powerBar < 0)
 		{
 			CCDirector.sharedDirector().replaceScene(GameOverLayer.scene("Player2 wins"));
 		}
 		if (powerBar > (int) CCDirector.sharedDirector().displaySize().width)
 		{
 			CCDirector.sharedDirector().replaceScene(GameOverLayer.scene("Player1 wins"));
 		}
 	}
 	
 	
 	protected void addTarget()
 	{
 		Random rand = new Random();
 		CCSprite target = CCSprite.sprite("Target.png");
 		
 		// Determine where to spawn the target along the Y axis
 		CGSize winSize = CCDirector.sharedDirector().displaySize();
 		int minY = (int)(target.getContentSize().height / 2.0f);
 		int maxY = (int)(winSize.height - target.getContentSize().height / 2.0f);
 		int rangeY = maxY - minY;
 		int actualY = rand.nextInt(rangeY) + minY;
 		
 		// Create the target slightly off-screen along the right edge,
 		// and along a random position along the Y axis as calculated above
 		target.setPosition(winSize.width + (target.getContentSize().width / 2.0f), actualY);
 		addChild(target);
 		
 		target.setTag(1);
 		_targets.add(target);
 		
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
 	
 	public void spriteMoveFinished(Object sender)
 	{
 		CCSprite sprite = (CCSprite)sender;
 		
 		if (sprite.getTag() == 1)
 		{
 			_targets.remove(sprite);
 			
 			_projectilesDestroyed = 0;
 			
 			
 			CCDirector.sharedDirector().replaceScene(GameOverLayer.scene("You Lose :("));
 		}
 		else if (sprite.getTag() == 2)
 			_projectiles.remove(sprite);
 		
 		this.removeChild(sprite, true);
 	}
 	
 	
 	
 	/*
 	 * Function to game simulated
 	 */
 /*	
 	private int getValueForDemoMode(int player) {
 		
 		Random r = new Random();
 		int i1=r.nextInt(100);
 		try {
 			Thread.sleep(500);
 			
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		switch (player)
 		{
 			case 1:
 				 projectileP1();
 				 break;
 			case 2:
 				projectileP2() ;
 				break;
 		}
 		
 		return  i1;
 		
 	}
 */
 	/*
 	 private Runnable mGetMindValues = new Runnable() {
 		 
 		 public void run() {
 			 
 			 if ((selectedP1>=0) && (!pause))
 				{
 					powerP1= (MainActivity.currentEeg[selectedP1].getAttention() + MainActivity.currentEeg[selectedP1].getMeditation())/MULTIPLIER_COUNTER;
 				}
 				if ((selectedP2>=0) && (!pause))
 				{
 					powerP2= (MainActivity.currentEeg[selectedP2].getAttention()+MainActivity.currentEeg[selectedP2].getMeditation())/MULTIPLIER_COUNTER	;
 				}
 			//	if (mHandler !=null)
 			//	{
 			//		mHandler.removeCallbacks(mGetMindValues);
 			//	    mHandler.postDelayed(mGetMindValues, 1000);
 			//	}
 	
 	  }
 		 
 	 };
 
 */
 
 
 	public static void setPlayers(int p1, int p2) {
 		selectedP1=p1;
 		selectedP2=p2;
 		
 	}
 
 	public static void pause(boolean b) {
 		pause = b;
 		
 	}
 	
 }
