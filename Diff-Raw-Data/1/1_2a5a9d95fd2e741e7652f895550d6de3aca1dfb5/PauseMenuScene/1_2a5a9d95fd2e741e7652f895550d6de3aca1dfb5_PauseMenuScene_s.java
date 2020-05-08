 package topplintowers.scenes;
 
 import java.util.ArrayList;
 import java.util.Enumeration;
 
 import org.andengine.engine.camera.Camera;
 import org.andengine.engine.handler.timer.ITimerCallback;
 import org.andengine.engine.handler.timer.TimerHandler;
 import org.andengine.entity.IEntity;
 import org.andengine.entity.modifier.MoveXModifier;
 import org.andengine.entity.primitive.Rectangle;
 import org.andengine.entity.scene.menu.MenuScene;
 import org.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
 import org.andengine.entity.scene.menu.item.IMenuItem;
 import org.andengine.entity.scene.menu.item.SpriteMenuItem;
 import org.andengine.entity.text.Text;
 import topplintowers.MainActivity;
 import topplintowers.crates.Crate;
 import topplintowers.crates.CrateType;
 import topplintowers.crates.WoodCratePiece;
 import topplintowers.scenes.SceneManager.SceneType;
 import topplintowers.scenes.gamescene.GameScene;
 import topplintowers.scenes.gamescene.hud.CrateContainer;
 import topplintowers.scenes.gamescene.hud.CrateThumbnail;
 import topplintowers.scenes.gamescene.hud.MyHUD;
 
 import com.topplintowers.R;
 
 public class PauseMenuScene extends BaseScene implements IOnMenuItemClickListener {
 	private SpriteMenuItem mResumeButton, mRestartButton, mMainMenuButton, mOptionsButton;
 	private ArrayList<SpriteMenuItem> mButtons;
 	private Rectangle mRectangle;
 	private Text mText;
 	private MenuScene mMenuChildScene;
 	
 	public ArrayList<SpriteMenuItem> getButtons() { return mButtons; }
 	public Rectangle getRectangle() { return mRectangle; }
 	public Text getText() { return mText; }
 	public void setBackgroundPosition(float x, float y) { mRectangle.setPosition(x, y); }
 	public void setTextPosition(float x, float y) { mText.setPosition(x, y); }
 
 	@Override
 	public void createScene() {	
 		setBackgroundEnabled(false);
 		mRectangle = new Rectangle(0, 0, 800, 480, vbom);
 		mRectangle.setColor(0,0,0,0);
 		attachChild(mRectangle);
 
 		mText = SceneCommon.createLargeText(this, activity.getString(R.string.paused));		
 		createMenuChildScene();
 	}
 	
 	public Camera getCamera() { return camera; }
 	
 
 	
 	private void createMenuChildScene()
 	{
 		mMenuChildScene = new MenuScene(camera);
 		mMenuChildScene.setPosition(0, 0);
 		mMenuChildScene.setBackgroundEnabled(false);
 		
 		mButtons = new ArrayList<SpriteMenuItem>();
 		
 		mResumeButton = SceneCommon.createMenuButton(mMenuChildScene, MenuButtonsEnum.RESUME,  activity.getString(R.string.resume));
 		mButtons.add(mResumeButton);
 		
 		mRestartButton = SceneCommon.createMenuButton(mMenuChildScene, MenuButtonsEnum.RESTART,  activity.getString(R.string.restart));
 		mButtons.add(mRestartButton);
 		
 		mOptionsButton = SceneCommon.createMenuButton(mMenuChildScene, MenuButtonsEnum.OPTIONS,  activity.getString(R.string.option));
 		mButtons.add(mOptionsButton);
 		
 		mMainMenuButton = SceneCommon.createMenuButton(mMenuChildScene, MenuButtonsEnum.MAIN_MENU,  activity.getString(R.string.main_menu));
 		mButtons.add(mMainMenuButton);
 		
 		SceneCommon.repositionButtons(mResumeButton.getWidth(), mResumeButton.getHeight(), mButtons);
 		
 		mMenuChildScene.setOnMenuItemClickListener(this);
 		setChildScene(mMenuChildScene);
 	}
 
 	@Override
 	public void onBackKeyPressed() { returnToGameScene();}
 	
 	@Override
 	public void onMenuKeyPressed() { returnToGameScene(); }
 	
 	private void returnToGameScene() { 
 		final GameScene gs = (GameScene)SceneManager.getInstance().getCurrentScene();		
 		
 		fadeOut();
 		
 		engine.registerUpdateHandler(new TimerHandler(0.2f, new ITimerCallback()
         {                      
             @Override
             public void onTimePassed(final TimerHandler pTimerHandler)
             {   
             	gs.clearChildScene();
             	gs.mHud.setVisible(true);
         		SceneCommon.repositionButtons(mResumeButton.getWidth(), mResumeButton.getHeight(), mButtons);
             }
         }));
 	}
 
 	@Override
 	public SceneType getSceneType() { return SceneType.PAUSED; }
 
 	@Override
 	public void disposeScene() { return; }
 	
 	@Override
 	public boolean onMenuItemClicked(final MenuScene pMenuScene, final IMenuItem pMenuItem, float pMenuItemLocalX, float pMenuItemLocalY) {
 		final MoveXModifier moveRight = new MoveXModifier(0.25f, pMenuItem.getX() - 25, camera.getWidth());
 		moveRight.setAutoUnregisterWhenFinished(true);
 		
 		MoveXModifier moveLeft = new MoveXModifier(0.25f, pMenuItem.getX(), pMenuItem.getX() - 25) {
 			@Override
 			protected void onModifierFinished(IEntity pItem) {
 				pItem.registerEntityModifier(moveRight);
 			}
 		};
 	    moveLeft.setAutoUnregisterWhenFinished(true);
 	    pMenuItem.registerEntityModifier(moveLeft);
 	    
 	    engine.registerUpdateHandler(new TimerHandler(0.55f, new ITimerCallback() {                      
             @Override
             public void onTimePassed(final TimerHandler pTimerHandler)
             {            	
             	MenuButtonsEnum button = MenuButtonsEnum.values()[pMenuItem.getID()];
             	switch (button) {
             	 	case RESUME:  
             	 		((MainActivity)activity).onResumeGame();
 	    	        	break;
 	    	        case RESTART:
 	    	        	deleteExistingCrates();
 	    	        	reinitializeContainers();
 	    	        	((MainActivity)activity).onResumeGame();
 	    	    		break;
 	    	        case MAIN_MENU:
 	    	        	SceneManager.getInstance().loadMenuScene(engine);
 	    	        	break;
 	    	        case OPTIONS:
 	    	        	SceneManager.getInstance().loadOptionsScene(engine, false);
 	    	        	break;
 	    	        default:
 						break;
             	}
             	
             }
         }));
 	    return true;
 	}
 	
 	private void deleteExistingCrates() {
 		GameScene gameScene = ((GameScene)getParent());
 		
 		Enumeration<CrateType> crateTypes = GameScene.activeCrates.keys();
 		while (crateTypes.hasMoreElements()) {
 			CrateType type = (CrateType) crateTypes.nextElement();
 			ArrayList<Crate> currentList = GameScene.activeCrates.get(type);
 			for (Crate currentCrate : currentList) {
 				gameScene.mPhysicsWorld.destroyBody(currentCrate.getBox());
 				currentCrate.getSprite().detachSelf();
 				MyHUD.mAvailableCrateCounts.put(type, MyHUD.mAvailableCrateCounts.get(type) + 1);
 			}
 			currentList.clear();
 		}
 		
 		for (WoodCratePiece piece : gameScene.activeWoodCratePieces) {
 			gameScene.mPhysicsWorld.destroyBody(piece.getBody());
 			piece.getSprite().detachSelf();
 		}
 		
 		gameScene.mPhysicsWorld.clearPhysicsConnectors();
 	}	
 	
 	private void reinitializeContainers() {
 		GameScene gameScene = (GameScene) SceneManager.getInstance().getCurrentScene();
 		CrateContainer left = gameScene.mHud.getLeft();
 		CrateContainer right = gameScene.mHud.getRight();
 		
 		expandHiddenCrates(left);
 		expandHiddenCrates(right);
 	}
 	
 	private void expandHiddenCrates(CrateContainer container) {
 		for (int i = 0; i < container.thumbs.size(); i++) {
 			CrateThumbnail current = container.thumbs.get(i);
 			if (current.isHidden())
 				current.expandThumbnail();
 		}
 		
 		container.resizeContainer(container.getSprite().getHeight());
 		container.repositionCrates();
 	}
 	
 	public void fadeIn() {
 		SceneCommon.applyFadeModifier(mRectangle, 0, 0.75f);
 		SceneCommon.applyFadeModifier(mButtons, 0, 1);
 		SceneCommon.applyFadeModifier(mText, 0, 1);
 	}
 	
 	public void fadeOut() {
 		SceneCommon.applyFadeModifier(mRectangle, 0.75f, 0);
 		SceneCommon.applyFadeModifier(mButtons, 1, 0);
 		SceneCommon.applyFadeModifier(mText, 1, 0);
 	}
 
 }
