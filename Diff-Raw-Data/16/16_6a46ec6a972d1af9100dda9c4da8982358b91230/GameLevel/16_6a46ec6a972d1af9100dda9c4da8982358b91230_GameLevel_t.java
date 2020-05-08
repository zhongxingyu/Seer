 package org.game.bangyouscreen.gameLevels;
 
 import java.util.Arrays;
 
 import org.andengine.engine.handler.IUpdateHandler;
 import org.andengine.engine.handler.physics.PhysicsHandler;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.sprite.AnimatedSprite;
 import org.andengine.entity.sprite.AnimatedSprite.IAnimationListener;
 import org.andengine.entity.sprite.ButtonSprite;
 import org.andengine.entity.sprite.ButtonSprite.OnClickListener;
 import org.andengine.entity.sprite.Sprite;
import org.andengine.input.touch.controller.MultiTouchController;
import org.andengine.input.touch.controller.SingleTouchController;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 import org.game.bangyouscreen.managers.ManagedScene;
 import org.game.bangyouscreen.managers.ResourceManager;
 import org.game.bangyouscreen.util.EntityUtil;
 import org.game.bangyouscreen.util.GameScore;
 import org.game.bangyouscreen.util.GameTimer;
 
 public class GameLevel extends ManagedScene {
 	
 	private static final GameLevel INSTANCE = new GameLevel();
 	private static final float mCameraWidth = ResourceManager.getCamera().getWidth();
 	private static final float mCameraHeight = ResourceManager.getCamera().getHeight();
 	private VertexBufferObjectManager mVertexBufferObjectManager = ResourceManager.getEngine().getVertexBufferObjectManager();
 	
 	//private static final String TIME_FORMAT = "00:00";
 	//private Text mTimeText;
 	private float gameTime = 30f;
 	private int mScore = 0;
 	private GameTimer mGameTime;
 	private boolean mTenSeconds = false;
 	private GameScore mGameScore;
 	
 	private AnimatedSprite aidSkill1AS;
 	private AnimatedSprite angelbossAS;
 	private static final float BOSS_VELOCITY = 50.0f;
 	private PhysicsHandler mPhysicsHandler;
 	private long[] frameDur;
 	
 	private int bossBloodNum = 100;
 	private float xue3P;
 	private float xue3S;
 	private Sprite xue1Sprite;
 	private Sprite xue2Sprite;
 	
 	private ButtonSprite greenButtonBS;
 	private ButtonSprite redButtonBS;
 	
 	public static GameLevel getInstance(){
 		return INSTANCE;
 	}
 
 	@Override
 	public Scene onLoadingScreenLoadAndShown() {
 		return null;
 	}
 
 	@Override
 	public void onLoadingScreenUnloadAndHidden() {
 
 	}
 
 	@Override
 	public void onLoadScene() {
 		ResourceManager.getInstance().engine.getEngineOptions().getTouchOptions().setNeedsMultiTouch(true);
		ResourceManager.getInstance().engine.setTouchController(new MultiTouchController());
 		
 		//背景
 		Sprite bg1 = new Sprite(0f,0f,ResourceManager.gameBG10,mVertexBufferObjectManager);
 		bg1.setScale(ResourceManager.getInstance().cameraHeight / ResourceManager.gameBG10.getHeight());
 		bg1.setPosition(mCameraWidth/2f,mCameraHeight/2f);
 		bg1.setZIndex(-90);
 		attachChild(bg1);
 		
 		//倒计时
 		mGameTime = new GameTimer(this);
 		mGameTime.addToLayer(this);
 		registerUpdateHandler(gameRunTimer);
 		
 		//得分
 		mGameScore = new GameScore();
 		mGameScore.addToLayer(this);
 		
 		//BOSS血条
 		xue1Sprite = new Sprite(0f,0f,ResourceManager.xue1,mVertexBufferObjectManager);
 		xue1Sprite.setPosition(mCameraWidth/2f, mCameraHeight - xue1Sprite.getHeight()/2f-5f);
 		xue1Sprite.setSize(0.6875f*mCameraWidth, 0.05833f*mCameraHeight);
 		//xue1Sprite.setSize(0.5f*mCameraWidth, 0.1f*mCameraHeight);
 		//EntityUtil.setSize("width", 0.6875f, xue1Sprite);
 		attachChild(xue1Sprite);
 		xue2Sprite = new Sprite(0f,0f,ResourceManager.xue2,mVertexBufferObjectManager);
 		xue2Sprite.setPosition(xue1Sprite.getWidth()/2f, xue1Sprite.getHeight()/2f);
 		xue2Sprite.setSize(xue1Sprite.getWidth(), xue1Sprite.getHeight());
 		//EntityUtil.setSize("width", 0.67875f, xue2Sprite);
 		xue1Sprite.attachChild(xue2Sprite);
 		//xue2Sprite.setScaleCenterX(xue1Sprite.getWidth());
 //		xue3Sprite = new Sprite(0f,0f,ResourceManager.xue3,mVertexBufferObjectManager);
 //		xue3Sprite.setPosition(xue1Sprite.getWidth()/2f, xue3Sprite.getHeight()/2f);
 //		xue3Sprite.setSize(xue1Sprite.getWidth()-10f, xue1Sprite.getHeight()-10f);
 		//xue1Sprite.attachChild(xue3Sprite);
 		xue3P = (xue2Sprite.getWidth()/2f)/bossBloodNum;
 		xue3S = (xue2Sprite.getWidth())/bossBloodNum;
 		
 		//BOSS
 		angelbossAS = new AnimatedSprite(0f,0f,ResourceManager.boss19,mVertexBufferObjectManager);
 		angelbossAS.setPosition(mCameraWidth/2f, mCameraHeight/2f);
 		EntityUtil.setSize("width", 0.32f, angelbossAS);
 		//angelbossAS.setScale(2f);
 		frameDur = new long[2];
 		Arrays.fill(frameDur, 300);
 		angelbossAS.animate(frameDur,0,1,true);
 		//angelbossAS.registerEntityModifier(pEntityModifier);
 		mPhysicsHandler = new PhysicsHandler(angelbossAS);
 		angelbossAS.registerUpdateHandler(mPhysicsHandler);
 		mPhysicsHandler.setVelocity(BOSS_VELOCITY);
 		attachChild(angelbossAS);
 		
 		//绿色按钮
 		greenButtonBS = new ButtonSprite(0f,0f,ResourceManager.greenButtonTTR,mVertexBufferObjectManager);
 		EntityUtil.setSize("height", 1f / 4f, greenButtonBS);
 		greenButtonBS.setPosition(greenButtonBS.getWidth() / 2f, greenButtonBS.getHeight() / 2f);
 		greenButtonBS.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(ButtonSprite pButtonSprite,
 					float pTouchAreaLocalX, float pTouchAreaLocalY) {
 				mScore++;
 				bossBloodNum--;
 				//血条变化
 				//float bloodLength = xue3Sprite.getWidth()-xue3S;
 				if(bossBloodNum >= 0 && gameTime > 0){
 					xue2Sprite.setPosition(xue2Sprite.getX()-xue3P, xue2Sprite.getY());
 					xue2Sprite.setSize(xue2Sprite.getWidth()-xue3S, xue2Sprite.getHeight());
 					mGameScore.adjustScore(mScore);
 					//xue2Sprite.setScaleX(bossBloodNum / 100.0F);
 					
 					
 					if(bossBloodNum == 0 && gameTime > 0){
 						angelbossAS.unregisterUpdateHandler(mPhysicsHandler);
 						unregisterUpdateHandler(gameRunTimer);
 					}
 				}
 				
 				
 //				if(mScore % 5 == 0){
 //					angelbossAS.animate(frameDur,1,2,true);
 //					angelbossAS.stopAnimation(2);
 //					angelbossAS.animate(frameDur,0,1,true);
 //				}
 			}});
 		attachChild(greenButtonBS);
 		registerTouchArea(greenButtonBS);
 		
 		//蓝色按钮
 		redButtonBS = new ButtonSprite(0f,0f,ResourceManager.redButtonTTR,mVertexBufferObjectManager);
 		EntityUtil.setSize("height", 1f / 4f, redButtonBS);
 		redButtonBS.setPosition(mCameraWidth - redButtonBS.getWidth() / 2f, redButtonBS.getHeight() / 2f);
 		redButtonBS.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(ButtonSprite pButtonSprite,
 					float pTouchAreaLocalX, float pTouchAreaLocalY) {
 				mScore++;
 				bossBloodNum--;
 				
 				//血条变化
 				if(bossBloodNum >= 0 && gameTime > 0){
 					xue2Sprite.setPosition(xue2Sprite.getX()-xue3P, xue2Sprite.getY());
 					xue2Sprite.setSize(xue2Sprite.getWidth()-xue3S, xue2Sprite.getHeight());
 					//xue2Sprite.setScaleX(bossBloodNum / 100.0F);
 					mGameScore.adjustScore(mScore);
 					if(bossBloodNum == 0 && gameTime > 0){
 						angelbossAS.unregisterUpdateHandler(mPhysicsHandler);
 						unregisterUpdateHandler(gameRunTimer);
 					}
 				}
 				
 				
 //				if(mScore % 5 == 0){
 //					//angelbossAS.stopAnimation(2);
 //					angelbossAS.animate(frameDur,1,2,true);
 //					angelbossAS.stopAnimation(2);
 //					angelbossAS.animate(frameDur,0,1,true);
 //				}
 			}});
 		attachChild(redButtonBS);
 		registerTouchArea(redButtonBS);
 		
 		//时钟
 		ButtonSprite clockSprite = new ButtonSprite(0f,0f,ResourceManager.clockTR,mVertexBufferObjectManager);
 		//EntityUtil.setSize("width", 1f / 8f, clockSprite);
 		clockSprite.setPosition((1f/2f)*mCameraWidth, clockSprite.getHeight()/2f);
 		clockSprite.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(ButtonSprite pButtonSprite,
 					float pTouchAreaLocalX, float pTouchAreaLocalY) {
 				gameTime+=30f;
 				
 			}});
 		attachChild(clockSprite);
 		registerTouchArea(clockSprite);
 		
 		//头像
 		ButtonSprite aiderHeadSprite = new ButtonSprite(0f,0f,ResourceManager.clockTR,mVertexBufferObjectManager);
 		aiderHeadSprite.setPosition((1f / 2f) * mCameraWidth + clockSprite.getWidth(), clockSprite.getHeight()/2f);
 		aiderHeadSprite.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(ButtonSprite pButtonSprite,
 					float pTouchAreaLocalX, float pTouchAreaLocalY) {
 				//辅助技能
 				aidSkill1AS = new AnimatedSprite(mCameraWidth/2f,mCameraHeight/2f,ResourceManager.aidSkill2Temp,mVertexBufferObjectManager);
 				aidSkill1AS.setScale(2f);
 				aidSkill1AS.animate(100,3,new IAnimationListener(){
 
 					@Override
 					public void onAnimationStarted(AnimatedSprite pAnimatedSprite,
 							int pInitialLoopCount) {
 						angelbossAS.stopAnimation(2);
 					}
 
 					@Override
 					public void onAnimationFrameChanged(AnimatedSprite pAnimatedSprite,
 							int pOldFrameIndex, int pNewFrameIndex) {
 						
 					}
 
 					@Override
 					public void onAnimationLoopFinished(AnimatedSprite pAnimatedSprite,
 							int pRemainingLoopCount, int pInitialLoopCount) {
 						
 					}
 
 					@Override
 					public void onAnimationFinished(AnimatedSprite pAnimatedSprite) {
 						detachChild(aidSkill1AS);
 						aidSkill1AS = null;
 						angelbossAS.animate(frameDur,0,1,true);
 					}
 				});
 				attachChild(aidSkill1AS);
 				
 			}});
 		attachChild(aiderHeadSprite);
 		registerTouchArea(aiderHeadSprite);
 		
 		
 
 		
 
 		
 		
 	}
 
 	@Override
 	public void onUnloadScene() {
 		ResourceManager.getInstance().engine.getEngineOptions().getTouchOptions().setNeedsMultiTouch(false);
		ResourceManager.getInstance().engine.setTouchController(new SingleTouchController());
 	}
 
 	@Override
 	public void onShowScene() {
 
 	}
 
 	@Override
 	public void onHideScene() {
 
 	}
 	
 	//游戏时间倒计时,BOSS移动
 	private IUpdateHandler gameRunTimer = new IUpdateHandler() {
 
 		@Override
 		public void onUpdate(float pSecondsElapsed) {
 			//倒计时
 			gameTime-=pSecondsElapsed;
 			if(gameTime<=0) {
 				//mTimeText.setText(TimeUtils.formatSeconds(0));
 				mGameTime.adjustTime(0f);
 				unregisterUpdateHandler(this);
 				angelbossAS.unregisterUpdateHandler(mPhysicsHandler);
 				mGameTime.mDigitsSprite[3].clearEntityModifiers();
 				mGameTime.mDigitsSprite[2].clearEntityModifiers();
 				mGameTime.mDigitsSprite[3].setScale(1.0F);
 				mGameTime.mDigitsSprite[2].setScale(1.0F);
 			} else {
 				if((!mTenSeconds) && gameTime <= 10f){
 					mTenSeconds = true;
 					mGameTime.mBounceOut1.reset();
 					mGameTime.mBounceOut2.reset();
 					mGameTime.mDigitsSprite[3].registerEntityModifier(mGameTime.mBounceOut1);
 					mGameTime.mDigitsSprite[2].registerEntityModifier(mGameTime.mBounceOut2);
 					mGameTime.mColorOut1.reset();
 					mGameTime.mColorOut2.reset();
 					mGameTime.mDigitsSprite[3].registerEntityModifier(mGameTime.mColorOut1);
 					mGameTime.mDigitsSprite[2].registerEntityModifier(mGameTime.mColorOut2);
 				}else if(mTenSeconds && gameTime > 10f){
 					mTenSeconds = false;
 					//mGameTime.mDigitsSprite[3].registerEntityModifier(mGameTime.mColorOut1);
 					//mGameTime.mDigitsSprite[2].registerEntityModifier(mGameTime.mColorOut2);
 					mGameTime.mDigitsSprite[3].clearEntityModifiers();
 					mGameTime.mDigitsSprite[2].clearEntityModifiers();
 					mGameTime.mDigitsSprite[3].setScale(1.0F);
 					mGameTime.mDigitsSprite[2].setScale(1.0F);
 				}
 				mGameTime.adjustTime(gameTime);
 			}
 			
 			//BOSS移动
 			if(angelbossAS.getY() - angelbossAS.getHeight()/2f < redButtonBS.getY() + redButtonBS.getHeight()/2f) {
 				mPhysicsHandler.setVelocityY(BOSS_VELOCITY);
 			} else if(angelbossAS.getY() + (angelbossAS.getHeight() * 0.5f) > xue1Sprite.getY() - xue1Sprite.getHeight()/2f) {
 				mPhysicsHandler.setVelocityY(-BOSS_VELOCITY);
 			}
 			if(angelbossAS.getX() - angelbossAS.getWidth()/2f < 2f) {
 				mPhysicsHandler.setVelocityX(BOSS_VELOCITY);
 			} else if(angelbossAS.getX() + (angelbossAS.getWidth() * 0.5f) > mCameraWidth - 2f) {
 				mPhysicsHandler.setVelocityX(-BOSS_VELOCITY);
 			}
 			
 			//统计得分
 			mGameScore.adjustScore(mScore);
 		}
 
 		@Override
 		public void reset() {}
 	};
 
 }
