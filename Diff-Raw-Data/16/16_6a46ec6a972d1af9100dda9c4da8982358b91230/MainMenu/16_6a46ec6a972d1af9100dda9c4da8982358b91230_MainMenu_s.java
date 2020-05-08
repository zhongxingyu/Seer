 package org.game.bangyouscreen.menus;
 
 import org.andengine.entity.Entity;
 import org.andengine.entity.modifier.MoveModifier;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.sprite.ButtonSprite;
 import org.andengine.entity.sprite.ButtonSprite.OnClickListener;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 import org.andengine.util.math.MathUtils;
 import org.game.bangyouscreen.gameLevels.GameLevel;
 import org.game.bangyouscreen.managers.ManagedScene;
 import org.game.bangyouscreen.managers.ResourceManager;
 import org.game.bangyouscreen.managers.SceneManager;
 import org.game.bangyouscreen.util.EntityUtil;
 
 
 public class MainMenu extends ManagedScene{
 	
 	private static final MainMenu INSTANCE = new MainMenu();
 	private static final float mCameraWidth = ResourceManager.getCamera().getWidth();
 	private static final float mCameraHeight = ResourceManager.getCamera().getHeight();
 	private VertexBufferObjectManager mVertexBufferObjectManager = ResourceManager.getEngine().getVertexBufferObjectManager();
 	
 	private Entity mainMenuScreen; //主菜单
 	private Sprite mainMenuTitleSprite;//主菜单标题
 	
 	public static MainMenu getInstance(){
 		return INSTANCE;
 	}
 	
 	public MainMenu(){
 		super(0.1f);
 	}
 	
 	@Override
 	public Scene onLoadingScreenLoadAndShown() {
 //		Sprite backgroundSprite = new Sprite(0f,0f, ResourceManager.mainMenuBackgroundTR,mVertexBufferObjectManager);
 //		backgroundSprite.setScale(ResourceManager.getInstance().cameraWidth / ResourceManager.mainMenuBackgroundTR.getWidth());
 //		backgroundSprite.setPosition(mCameraWidth / 2f, mCameraHeight / 2f);
 //		backgroundSprite.setZIndex(-5000);
 //		attachChild(backgroundSprite);
 		
 		ResourceManager.loadMenuResources();
 		ResourceManager.setupForMenus();
 
 		LoadingScene.getInstance().onLoadScene();
 		return LoadingScene.getInstance();
 	}
 
 	@Override
 	public void onLoadingScreenUnloadAndHidden() {
 		LoadingScene.getInstance().unloadScene();
 	}
 	
 	@Override
 	public void onLoadScene() {
 		ResourceManager.loadThemeResources();
 		ResourceManager.loadGameResources();
 		//白云
 //		Sprite mMenuCloudsLayerOne = new Sprite(0.0F, mCameraHeight, ResourceManager.menuClouds1, mVertexBufferObjectManager);
 //		Sprite mMenuCloudsLayerTwo = new Sprite(0.0F, mCameraHeight-100.0F, ResourceManager.menuClouds2, mVertexBufferObjectManager);
 //		AutoParallaxBackground localAutoParallaxBackground = new AutoParallaxBackground(1.0F, 1.0F, 1.0F, 7.0F);
 //		localAutoParallaxBackground.attachParallaxEntity(new ParallaxBackground.ParallaxEntity(0.0F, backgroundSprite));
 //	    localAutoParallaxBackground.attachParallaxEntity(new ParallaxBackground.ParallaxEntity(1.0F, mMenuCloudsLayerOne));
 //	    localAutoParallaxBackground.attachParallaxEntity(new ParallaxBackground.ParallaxEntity(3.0F, mMenuCloudsLayerTwo));
 //	    setBackground(localAutoParallaxBackground);
 //	    setBackgroundEnabled(true);
 
 		
 		Sprite[] CloudSprites; CloudSprites = new Sprite[20];
 		for(Sprite curCloudSprite: CloudSprites){
 			curCloudSprite = new Sprite(
 					MathUtils.random(-(this.getWidth()*this.getScaleX())/2,ResourceManager.getInstance().cameraWidth+(this.getWidth()*this.getScaleX())/2),
 					MathUtils.random(-(this.getHeight()*this.getScaleY())/2,ResourceManager.getInstance().cameraHeight + (this.getHeight()*this.getScaleY())/2),
 					ResourceManager.menuClouds1,mVertexBufferObjectManager) {
 				private float XSpeed = MathUtils.random(0.2f, 2f);
 				private boolean initialized = false;
 				@Override
 				protected void onManagedUpdate(final float pSecondsElapsed) {
 					super.onManagedUpdate(pSecondsElapsed);
 					if(!initialized) {
 						initialized = true;
 						this.setScale(XSpeed/2);
 						this.setZIndex(-4000+Math.round(XSpeed*1000f));
 						MainMenu.getInstance().sortChildren();
 					}
 					if(this.getX()<-(this.getWidth()*this.getScaleX())/2) {
 						XSpeed = MathUtils.random(0.2f, 2f);
 						this.setScale(XSpeed/2);
 						this.setPosition(ResourceManager.getInstance().cameraWidth+(this.getWidth()*this.getScaleX())/2, MathUtils.random(-(this.getHeight()*this.getScaleY())/2,ResourceManager.getInstance().cameraHeight + (this.getHeight()*this.getScaleY())/2));
 						
 						this.setZIndex(-4000+Math.round(XSpeed*1000f));
 						MainMenu.getInstance().sortChildren();
 					}
 					this.setPosition(this.getX()-(XSpeed*(pSecondsElapsed/0.016666f)), this.getY());
 				}
 			};
 			this.attachChild(curCloudSprite);
 		}
 	    
 		mainMenuScreen = new Entity(0,mCameraHeight){
 			boolean hasLoaded = false;
 			@Override
 			protected void onManagedUpdate(final float pSecondsElapsed) {
 				super.onManagedUpdate(pSecondsElapsed);
 				if(!hasLoaded){
 					hasLoaded = true;
 					this.registerEntityModifier(new MoveModifier (1f,0,mCameraHeight,0,0));
 				}
 			}
 		};
 		
 		mainMenuTitleSprite = new Sprite(0f, mCameraHeight + ResourceManager.mainMenuTitleTR.getHeight(), ResourceManager.mainMenuTitleTR, mVertexBufferObjectManager);
 		//mainMenuTitleSprite.setSize(0.5f * mCameraWidth, (0.5f * mCameraWidth)/(mainMenuTitleSprite.getWidth() / mainMenuTitleSprite.getHeight()));
 		EntityUtil.setSize("width", 0.4f, mainMenuTitleSprite);
 		mainMenuTitleSprite.registerEntityModifier(new MoveModifier(1f, mCameraWidth / 2f, 
 				mainMenuTitleSprite.getY(), mCameraWidth / 2f, mCameraHeight - (mainMenuTitleSprite.getHeight() / 2f)));
 		mainMenuTitleSprite.setZIndex(-80);
 		
 		//主题模式
 		ButtonSprite themeModeBS = new ButtonSprite(0f,0f,ResourceManager.mainMenuButtons.getTextureRegion(0),mVertexBufferObjectManager);
 		//singleModeBS.setSize(0.3f * mCameraWidth, (0.3f * mCameraWidth)/(singleModeBS.getWidth() / singleModeBS.getHeight()));
 		EntityUtil.setSize("height", 1f / 7f, themeModeBS);
 		themeModeBS.setPosition(mCameraWidth / 2f, mCameraHeight / 2f);
 		mainMenuScreen.attachChild(themeModeBS);
 		themeModeBS.setOnClickListener(new OnClickListener(){
 			
 			@Override
 			public void onClick(ButtonSprite pButtonSprite, float pTouchAreaLocalX, float pTouchAreaLocalY) {
 				SceneManager.getInstance().showScene(ThemeScene.getInstance());
 				//SceneManager.getInstance().showScene(new GameLevel());
 				
 			}
 		});
 		registerTouchArea(themeModeBS);
 		
 		//装备库
 		ButtonSprite shopModeBS = new ButtonSprite(0f,0f,ResourceManager.mainMenuButtons.getTextureRegion(1),mVertexBufferObjectManager);
 		//singleModeBS.setSize(0.3f * mCameraWidth, (0.3f * mCameraWidth)/(singleModeBS.getWidth() / singleModeBS.getHeight()));
 		EntityUtil.setSize("height", 1f / 7f, shopModeBS);
 		shopModeBS.setPosition(mCameraWidth / 2f, themeModeBS.getY() - themeModeBS.getHeight() - 10f);
 		mainMenuScreen.attachChild(shopModeBS);
 		shopModeBS.setOnClickListener(new OnClickListener(){
 			
 			@Override
 			public void onClick(ButtonSprite pButtonSprite, float pTouchAreaLocalX, float pTouchAreaLocalY) {
 				//SceneManager.getInstance().showScene(new ThemeScene());
 			}
 		});
 		registerTouchArea(shopModeBS);
 		
 		//选项
 		ButtonSprite aboutModeBS = new ButtonSprite(0f,0f,ResourceManager.mainMenuButtons.getTextureRegion(2),mVertexBufferObjectManager);
 		//singleModeBS.setSize(0.3f * mCameraWidth, (0.3f * mCameraWidth)/(singleModeBS.getWidth() / singleModeBS.getHeight()));
 		EntityUtil.setSize("height", 1f / 7f, aboutModeBS);
 		aboutModeBS.setPosition(mCameraWidth / 2f, shopModeBS.getY() - shopModeBS.getHeight() - 10f);
 		mainMenuScreen.attachChild(aboutModeBS);
 		aboutModeBS.setOnClickListener(new OnClickListener(){
 			
 			@Override
 			public void onClick(ButtonSprite pButtonSprite, float pTouchAreaLocalX, float pTouchAreaLocalY) {
 				//SceneManager.getInstance().showScene(new ThemeScene());
 			}
 		});
 		registerTouchArea(aboutModeBS);
 
 	
 		this.attachChild(mainMenuTitleSprite);
 		this.attachChild(this.mainMenuScreen);
 	}
 	
 	@Override
 	public void onShowScene() {
 //		if(!this.backgroundSprite.hasParent()) {
 //			this.attachChild(this.backgroundSprite);
 //			this.sortChildren();
 //		}
 		
 		//sortChildren();
 	}
 
 	@Override
 	public void onUnloadScene() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onHideScene() {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
