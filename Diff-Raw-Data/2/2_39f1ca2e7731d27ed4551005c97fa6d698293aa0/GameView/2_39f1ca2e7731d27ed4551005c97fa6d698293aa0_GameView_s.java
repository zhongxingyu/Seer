 package com.games.test1;
 
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Random;
 import java.util.Timer;
 import java.util.Vector;
 
 import org.xml.sax.InputSource;
 
 import com.games.test1.GameView.GameThread.BasicGameState;
 import com.games.test1.aal.AALExecutionState;
 import com.games.test1.astraal.*;
 import com.games.test1.ui.GameUI;
 import com.games.test1.ui.UIControlButton;
 import com.games.test1.ui.UIControlButtonImage;
 import com.games.test1.ui.UIControlButtonListItem;
 import com.games.test1.ui.UIControlCaption;
 import com.games.test1.ui.UIControlInventory;
 import com.games.test1.ui.UIControlNavigator;
 import com.games.test1.ui.UIEvent;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.BlurMaskFilter;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.DrawFilter;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.graphics.Typeface;
 import android.graphics.Paint.Align;
 import android.graphics.drawable.Drawable;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.Display;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.TextView;
 
 
 public class GameView extends SurfaceView implements SurfaceHolder.Callback {
 
 	/** Handle to the application context, used to e.g. fetch Drawables. */
 	public static Context context;
 
 	/** Thread handling the actual game logic & rendering. */
 	public GameThread thread;
 
 	// Dimensions of screen
 	public int mCanvasWidth;
 	public int mCanvasHeight;
 
 	private Bundle mStateBundle;
 
 	public ASTRAALRoot mRoot;
 
 	public Bitmap tempImage;
 	
 	public static Typeface captionTypeface;
 	public static Bitmap iconCompass;
 	public static Bitmap iconJournal;
 	public static Bitmap imageNavigatorRight;
 	public static Bitmap iconInventory;
 	
 	/**
 	 * Thread for the actual game logic and rendering.
 	 */
 	public class GameThread extends Thread {
 		private static final int CAMERA_DEFAULT_WIDTH = 800;
 
 		private static final int CAMERA_DEFAULT_HEIGHT = 480;
 
 		private static final int MAX_MOUSE_DEVIATION_FOR_CLICK = 20;
 
 		public static final String JOURNAL_LOCKED_LABEL = "???";
 		private static final String JOURNAL_LOCKED_TEXT = "This page has not yet been found.";
 
 		public static final int DEFAULT_JOURNAL_UI_PADDING = 4;
 
 
 		/** Handle to the surface manager object we interact with */
 		private SurfaceHolder mSurfaceHolder;
 
 		/** Event handler if we need to interact with the outside world. */
 		private Handler mHandler;
 
 		/** Flag to indicate whether this thread should shut down or not. */
 		private boolean mShouldKeepRunning = false;
 
 		/* Resource containers. */
 		private Bitmap mBackgroundImage; 
 
 		/** Currently-active state. Made public for convenience/speed. */
 		public com.games.test1.State mActiveState = null;
 
 		/** Map of state enums to their instances. **/
 		private HashMap<StateType, com.games.test1.State> stateMap;
 
 		private GameExecutor mExecutor;
 
 		private Resources mRes;
 
 		/** Loading must be delayed, so save the bundle temporarily. */
 		private Bundle mBundleToLoad;
 
 
 		/**
 		 * Initialize the GameThread, but don't start running yet. Wait until
 		 * run() is called to do anything.
 		 * @param holder
 		 * @param context
 		 * @param handler
 		 */
 		public GameThread(SurfaceHolder holder, Context context, Handler handler) {
 			// Get handles to some important objects.
 			mSurfaceHolder = holder;
 			mHandler = handler;     
 
 			mCanvasWidth = holder.getSurfaceFrame().width();
 			mCanvasHeight = holder.getSurfaceFrame().height();
 
 			// Initialize the state map.
 			stateMap = new HashMap<StateType, com.games.test1.State>();
 			
 			// Load resources.
 			mRes = context.getResources();
 			
 			// Do static resource loads.
 			captionTypeface = Typeface.createFromAsset(context.getAssets(), "imposs.ttf");
 			iconCompass = BitmapFactory.decodeResource(mRes, R.drawable.compass);
 			iconJournal = BitmapFactory.decodeResource(mRes, R.drawable.journal);
 			iconInventory = BitmapFactory.decodeResource(mRes, R.drawable.inv);
 			imageNavigatorRight = BitmapFactory.decodeResource(mRes, R.drawable.side_highlight_right);
 	
 
 			// Make sure we don't start trying to draw anything, not until startGame
 			// is called by our run() method.			
 			mActiveState = new com.games.test1.State();
 			
 			// Begin in the loading state, and transfer back as soon as initial loading is done.
 			setState(StateType.Initialization);
 		}
 
 
 		/** Actually start the game. */
 		public void startGame(String astraalFilePath) {
 			// Create the ASTRAAL parser and load in the game.
 			ASTRAALParser parser = new ASTRAALParser();
 
 			// Create the root object and give it a Resource object to use.			
 			try {
 				mRoot = parser.parse(new InputSource(mRes.getAssets().open(astraalFilePath)));
 				mRoot.attachToReality(mRes);
 			} catch (IOException e) {
 				e.printStackTrace();
 				return;
 			}
 			
 			// We leave creation of the Executor to the MainGameState.			
 
 		}
 
 
 		/**
 		 * Tear down the current scene, so we can build up a new one.
 		 */
 		public void resetScene(int width, int height) {
 			getMainGameState().doBeforeSceneChange();
 			getMainGameState().setScene(new Scene(width, height));
 		}
 
 		/**
 		 * Reset the camera to a default position and size.
 		 */
 		public void resetCamera() {
 			int w = GameView.this.getWidth(),
 				h = GameView.this.getHeight();
 			getMainGameState().setCamera(new Camera(
 					getCurrentScene().getWidth() / 2 - w/2, 
 					0, 
 					w, 
 					h, 
 					getMainGameState().getScene()));
 
 		}		
 
 		private WindowManager getSystemService(String windowService) {
 			return null;
 		}
 
 
 		/**
 		 * Set the background within the game thread to the specified animation.
 		 * @param animation
 		 */
 		public void setBackground(Animation animation) {
 			getMainGameState().setBackground(animation);
 		}
 
 		/** Change the camera's zoom level for the main game state. */
 		public void adjustZoom(boolean zoomIn) {
 			float zoom = getMainGameState().getCamera().getMag();
 			if (zoomIn) {
 				zoom = Math.min(zoom + 0.25f, 3.0f);
 			} else {
 				zoom = Math.max(zoom - 0.25f, 1.0f);
 			}
 			
 			getMainGameState().getCamera().setMag(zoom);
 		}
 		
 		/**
 		 * Add the specified object to the main game's scene.
 		 * @param dObj
 		 */
 		public void addObject(DrawnObject dObj) {
 			getMainGameState().getScene().addObject(dObj);			
 		}
 
 		/** Update the execution state to the scene. */
 		public void setExecutionState(AALExecutionState executionState) {
 			getMainGameState().getScene().setExecutionState(executionState);
 		}
 		
 		/**
 		 * Get the current real Scene.
 		 */
 		public Scene getCurrentScene() {
 			Scene check = getMainGameState().getScene();
 			if (check == null) {
 				return new Scene(); 
 			} else {
 				return check;
 			}
 		}
 
 		/**
 		 * Helper method to get the main game state instance, whether or not
 		 * it's currently being run.
 		 */
 		public MainGameState getMainGameState() {
 			return (MainGameState)(getStateFromType(StateType.Main));
 		}
 
 		/**
 		 * Helper method to get the caption card state instance, whether or not
 		 * it's currently being run.
 		 */
 		public CaptionCardState getCaptionCardState() {
 			return (CaptionCardState)(getStateFromType(StateType.Caption));
 		}
 		
 		/**
 		 * Helper method to get the journal state instance, whether or not
 		 * it's currently being run.
 		 */
 		public JournalState getJournalState() {
 			return (JournalState)(getStateFromType(StateType.Journal));
 		}
 		
 
 		/** Switch to the caption state and display the given messages in order. */
 		public void showFullCaption(Vector<String> captions) {
 			getCaptionCardState().setCaptions(captions);
 			startCaptionState();									
 		}
 
 
 		/** Add a caption UI element to the screen. */
 		public void showHalfCaption(Vector<String> captions) {
 			getMainGameState().showHalfCaption(captions);
 		}
 
 		/** Show the journal screen. */
 		public void showJournal() {
 			startJournalState();
 		}
 		
 		/** Get main game state's inventory. */
 		public Inventory getInventory() {
 			return getMainGameState().getInventory();
 		}
 		
 		/** Start up the caption state as needed. */
 		private void startCaptionState() {
 			getCaptionCardState().setScene(new Scene(mCanvasWidth, mCanvasHeight));
 			getCaptionCardState().setCamera(new Camera(0, 0, 
 					mCanvasWidth, mCanvasHeight, 
 					getCaptionCardState().getScene()));
 			getCaptionCardState().setBackground(ASTRAALResourceFactory.createAnimation("caption_card_bg"));			
 
 			setState(StateType.Caption);
 		}
 		
 		/** Switch to the journal screen. */
 		private void startJournalState() {
 			getJournalState().setScene(new Scene(mCanvasWidth, mCanvasHeight));
 			getJournalState().setCamera(new Camera(0, 0, 
 					mCanvasWidth, mCanvasHeight, 
 					getJournalState().getScene()));
 			getJournalState().setBackground(ASTRAALResourceFactory.createAnimation("caption_card_bg"));			
 	
 			setState(StateType.Journal);
 		}
 		
 		/**
 		 * Set the active state of the game the given state instance.
 		 * @remark state should be obtained from the getInstance()
 		 * method of a State. Take care to handle the possibility of
 		 * getInstance() returning null, as the full Singleton pattern
 		 * cannot be implemented in this context.
 		 * @param state State to set as active. The currently-active
 		 * state will be stopped first.
 		 */
 		public void setState(StateType newState) {
 			// Stop the current state.
 			if (mActiveState != null) {
 				mActiveState.stop();
 			}
 			// Start up the new one.
 			mActiveState = getStateFromType(newState);
 			mActiveState.start();			
 		}
 
 		/**
 		 * Return the instance of the given state. This is needed because
 		 * of how inner classes work.
 		 */
 		public com.games.test1.State getStateFromType(StateType newState) {
 			if (!stateMap.containsKey(newState)) {
 				// Key not found, so we need to instantiate this class.
 				com.games.test1.State tState = null;
 				switch (newState) {
 				case Main:
 					tState = new MainGameState();
 					break;
 				case Caption:
 					tState = new CaptionCardState();
 					break;
 				case Journal:
 					tState = new JournalState();
 					break;
 				case MainMenu:
 					tState = new MainMenuState();
 					break;
 				case Initialization:
 					tState = new InitializationState();
 					break;
 				case Loading:
 					tState = new LoadingState();
 					break;
 				case SanityMiniGame:
 					tState = new SanityMiniGameState();
 					break;
 				}
 
 				// Add to the map for future reference.
 				stateMap.put(newState, tState);
 			}
 
 			return stateMap.get(newState);
 		}
 		
 		public StateType getCurrentStateType() {
 			return mActiveState.getType();
 		}
 
 		/**
 		 * Run the game logic.
 		 */
 		@Override
 		public void run() {
 			Log.w("Miskatonic", "Entering main thread.");
 			while (mShouldKeepRunning) {
 				update();
 			}
 			Log.w("Miskatonic", "Exiting main thread.");
 		}
 
 
 		public synchronized void update() {
 			Canvas c = null;
 			try {
 				// First, update the game.
 				mActiveState.run();				
 
 				c = mSurfaceHolder.lockCanvas(null);				
 				synchronized (mSurfaceHolder) {					
 					mActiveState.draw(c);
 				}				
 			} finally {
 				// do this in a finally so that if an exception is thrown
 				// during the above, we don't leave the Surface in an
 				// inconsistent state
 				if (c != null) {
 					mSurfaceHolder.unlockCanvasAndPost(c);
 				}
 			}
 		}
 
 		/** Pause the game logic. */
 		public void pause() {
 			// Nothing to do atm. Pausing isn't yet a necessity for the engine.
 		}
 
 		/** Returns true if the screen size is deemed "small". */
 		public boolean isSmallScreen() {
 			return Math.min(getWidth(), getHeight()) < 400;
 		}
 
 		/** Callback invoked when the surface dimensions change. */
 		public void setSurfaceSize(int width, int height) {
 			// synchronized to make sure these all change atomically
 			synchronized (mSurfaceHolder) {
 				mCanvasWidth = width;
 				mCanvasHeight = height;
 			
 			// At this point we don't have a game state. The plus side of that is that
 			// the load() routine in MGS can safely use getWidth()/getHeight() and we
 			// don't need this cludge in the first place. Left here in case we do =P	
 			//	getMainGameState().getCamera().setWidth(width);
 			//	getMainGameState().getCamera().setHeight(height);
 			}                        
 		}
 		
 		public GameExecutor getExecutor() {
 			return mExecutor; 
 		}
 
 		/** Save the game to a bundle for later restoration. */
 		public void saveToBundle(Bundle b) {
 			try {
 				/** Use our GameExecutor, which knows more about these things. */
 				mExecutor.saveToBundle(b);
 			} catch (Exception e) {
 				Log.w("Miskatonic", "Error saving: " + e.getMessage());
 			}
 		}
 
 		/** Load the game from a bundle. */
 		public void loadFromBundle(Bundle b) {
 			mBundleToLoad = b;			
 		}
 
 		/** Save the game to a file. 
 		 * @return Whether or not the save succeeded. */
 		public boolean saveToFile(String filename) {
 			try {
 				FileOutputStream out = 
 						context.openFileOutput(filename, Context.MODE_WORLD_WRITEABLE);
 				mExecutor.save(out);
 												
 				return true;
 			} catch (Exception e) {
 				e.printStackTrace();
 				return false;
 			}			
 		}
 		
 		/** Load the game from a file. 
 		 * @return Whether or not the load succeeded. */
 		public boolean loadFromFile(String filename) {
 			try {
 				FileInputStream in = 
 						context.openFileInput(filename);
 				mExecutor.load(in);
 												
 				return true;
 			} catch (Exception e) {
 				e.printStackTrace();
 				return false;
 			}			
 		}
 
 
 		/**
 		 * Control for shutting down the thread softly.
 		 * @param b Whether or not the thread should continue to run.
 		 */ 
 		public void setRunning(boolean b) {
 			mShouldKeepRunning = b;		
 		}
 
 		/** Handle a "mouse" down event. */
 		public void onMousePress(MotionEvent event) {			
 			int x = (int)event.getX();
 			int y = (int)event.getY();
 			mActiveState.onMousePress(x, y);
 		}
 
 		/** Handle a "mouse" move event. */
 		public void onMouseMove(MotionEvent event) {
 			int x = (int)event.getX();
 			int y = (int)event.getY();
 			mActiveState.onMouseMove(x, y); 
 		}
 
 		/** Handle the release of the "mouse". */
 		public void onMouseRelease(MotionEvent event) {
 			int x = (int)event.getX();
 			int y = (int)event.getY();
 			mActiveState.onMouseRelease(x, y);			
 		}
 		
 		private synchronized void loadAndStartMainGameState() {
 			mActiveState = new com.games.test1.State();
 			setState(StateType.Main);
 			getMainGameState().load();			
 		}
 
 		/** An abstract state that uses a scene-and-camera system. */
 		public class BasicGameState extends com.games.test1.State {
 			protected Scene mScene;
 			protected Camera mCamera;		
 			
 			/**
 			 * Set the background of the scene to the given animation.
 			 * @param animation
 			 */
 			public void setBackground(Animation animation) {
 				mScene.addObject(new BackgroundObject(
 						animation, 0, 0, 
 						mScene.getWidth(), mScene.getHeight())
 				);	
 			}
 			
 			/** Draw the scene via the camera. */
 			public void draw(Canvas c) {
 				//c.drawColor(Color.BLUE);
 				c.save();
 				mCamera.drawScene(c);
 				c.restore();				
 			}
 			
 			public Scene getScene() {
 				return mScene;
 			}
 
 			public void setScene(Scene mScene) {
 				this.mScene = mScene;
 			}
 
 			public Camera getCamera() {
 				return mCamera;
 			}
 
 			public void setCamera(Camera mCamera) {
 				this.mCamera = mCamera;
 			}
 		}
 		
 		/**
 		 * The main game state.
 		 */
 		public class MainGameState extends BasicGameState {
 			private boolean mMouseDown;
 
 			private GameUI mUI;
 
 			private int mMouseX;
 			private int mMouseY;
 			private int mMouseDownX;
 			private int mMouseDownY;
 			private int mOldCameraCX;
 			private int mOldCameraCY;
 
 			private Inventory mInventory = new Inventory();
 			/** Identifies which item the user has picked up and is trying to use. */
 			private InventoryItem mSelectedInventoryItem = null;
 			
 			public MainGameState() { 
 			
 			}
 
 			public void start() {				
 				Log.w("Miskatonic", "STARTING MAIN GAME STATE ***");
 				if (mExecutor != null)
 					mExecutor.executeBuffer();
 			}
 			
 			public void load() {
 				Log.w("Miskatonic",	"Beginning load sequence for main game...");
 				
 				// Create the executor, which will actually run the game.
 				mExecutor = new GameExecutor(GameThread.this, mRoot);
 				mExecutor.startGame();
 
 				if (mBundleToLoad != null) {
 					Log.w("Miskatonic", "Restoring state from Bundle " + mBundleToLoad.toString());
 					mExecutor.loadFromBundle(mBundleToLoad);
 					mBundleToLoad = null;
 				}
 				
 				Log.w("Miskatonic",	"Load complete.");
 
 				setupUI();							
 			}
 			
 			/** Initialize the user interface for the main game. Only done once. */	
 			public void setupUI() {
 				// Create the UI.
 				mUI = new GameUI(GameView.this.getWidth(), GameView.this.getHeight(), GameThread.this);
 				mUI.addControl(new UIControlButton(25, 25, "+", new UIEvent() {
 					public void execute(GameThread game) {
 						game.adjustZoom(true);
 					}					
 				}), GameUI.POSITION_TOPRIGHT);
 				
 				mUI.addControl(new UIControlButton(25, 25, "-", new UIEvent() {
 					public void execute(GameThread game) {
 						game.adjustZoom(false);
 					}						
 				}), GameUI.POSITION_BOTTOMRIGHT);
 				
 				mUI.addControl(new UIControlButtonImage(48, 48, iconInventory, new UIEvent() {
 					public void execute(GameThread game) {
 						game.getMainGameState().showInventory();
 					}						
 				}), GameUI.POSITION_TOPLEFT);
 
 				mUI.addControl(new UIControlButtonImage(48, 48, iconJournal, new UIEvent() {
 					public void execute(GameThread game) {
 						game.showJournal();
 					}						
 				}), GameUI.POSITION_TOPLEFT);
 			}
 			
 			/** Hook for right before a scene change. */
 			public void doBeforeSceneChange() {
 				if (mUI != null)
 					mUI.removeControlsFromPosition(GameUI.POSITION_BOTTOM);
 			}
 			
 			/** Show a caption on the screen. */
 			public void showHalfCaption(Vector<String> captions) {
 				mUI.removeControlsFromPosition(GameUI.POSITION_BOTTOM);
 				mUI.addControl(new UIControlCaption((int) (getWidth() * .8), 
 						(int)(getHeight() * .25),captions), GameUI.POSITION_BOTTOM, false);				
 			}
 
 			/** Open the inventory panel. */
 			public void showInventory() {
 				mUI.removeControlsFromPosition(GameUI.POSITION_CENTER);
 				mUI.addControl(new UIControlInventory(mInventory, 
						(int)(getWidth() * .8), (int)(getHeight() * .8)), GameUI.POSITION_CENTER);
 			}
 
 			/** Add a navigator object to an edge of the scene. Called via AAL, essentially. */
 			public void addNavigationCue(String side, String sceneID) {
 				// TODO: Extend DrawnObject to add a Navigator, which handles adding the AAL script and such.
 				// Position the Navigator (FIXME: Abstract this along with GameUI's positioning)
 				int x = 0, y = mScene.getHeight()/2 - NavigationCue.NAVIGATOR_HEIGHT/2;
 				NavigationCue.Direction dir = NavigationCue.Direction.Right;
 				if (side.equals("left")) {
 					x = 0;
 					dir = NavigationCue.Direction.Left;
 				} else if (side.equals("right")) {					
 					x = mScene.getWidth() - NavigationCue.NAVIGATOR_WIDTH;					
 				}
 
 				mScene.addObject(new NavigationCue(x, y, sceneID, dir));
 			}
 			
 			/**
 			 * Hover the given item over the screen until the user taps (not scrolls) again, at
 			 * which point trigger an onCombine event for the target item (if any).
 			 */
 			public void selectInventoryItem(InventoryItem item) {
 				mSelectedInventoryItem = item;
 			}
 			
 			/** Draw the currently-selected inventory item in the center of the screen. */
 			public void drawSelectedInventoryItem(Canvas c) {
 				mSelectedInventoryItem.draw(c, getWidth()/2, getHeight()/2, InventoryItem.SIZE_BIG);
 			}
 			
 			/** Unselect and stop drawing the displayed inventory item. */
 			private void dropInventoryItem() {
 				mSelectedInventoryItem = null;				
 			}
 			
 			/** Should probably replace this with reflection, but eh. */
 			public StateType getType() { return StateType.Main; }
 
 			
 		
 
 
 			public void run() {
 				mScene.updateScene();
 				
 				// Handle view-scrolling.
 				if (mMouseDown) {					
 					int moveByX = mMouseX - mMouseDownX, 
 					moveByY = mMouseY - mMouseDownY;
 					mCamera.centerAt(mOldCameraCX - moveByX, mOldCameraCY - moveByY);
 				}
 			}
 
 			@Override
 			public void onMousePress(int x, int y) {
 				//getCamera().setEffect(new CameraEffectFade(true));
 				
 				// First, try the UI.
 				if (mUI.onClick(x, y)) {
 					Log.w("Miskatonic", "UI is blocking click!");
 				} else {				
 					mOldCameraCX = mCamera.getCenterX();
 					mOldCameraCY = mCamera.getCenterY();
 					mMouseDownX = x;
 					mMouseDownY = y;
 					mMouseX = x;
 					mMouseY = y;	
 	
 					mMouseDown = true;
 				}
 			}
 
 			public void onMouseMove(int x, int y) {				
 				mMouseX = x;
 				mMouseY = y;				
 			}
 
 			public void onMouseRelease(int x, int y) {
 				if (mMouseDown) { // If UI event was not triggered on the press.
 					if (Math.abs(mMouseDownX - x) + Math.abs(mMouseDownY - y) <= MAX_MOUSE_DEVIATION_FOR_CLICK) {
 						// Only do this if we didn't scroll the view.
 						
 						if (mSelectedInventoryItem != null) {
 							mScene.doOnCombine(mCamera, x, y, mSelectedInventoryItem);
 							dropInventoryItem();
 						} else {
 							mScene.doOnClick(mCamera, x, y);
 						}
 					}
 
 					mMouseDown = false;
 				}
 			}
 
 
 
 			public void draw(Canvas c) {
 				if (mUI == null)
 					return;
 				
 				super.draw(c);
 				
 				if (mSelectedInventoryItem != null)
 					drawSelectedInventoryItem(c);
 				
 				mUI.draw(c);
 			}
 
 			public int getMouseX() {
 				return mMouseX;
 			}
 
 			public void setMouseX(int mouseX) {
 				mMouseX = mouseX;
 			}
 
 			public int getMouseY() {
 				return mMouseY;
 			}
 
 			public void setMouseY(int mouseY) {
 				mMouseY = mouseY;
 			}
 
 			public Inventory getInventory() {
 				return mInventory;
 			}
 
 			public DrawnObject getBackground() {
 				return getScene().getAllObjects().get(0);
 			}
 
 		}
 		
 		/** Displays a full-screen caption card. */
 		public class CaptionCardState extends BasicGameState {
 			/** The captions to display. */
 			private Vector<String> mCaptions;
 			private String mCurrentCaption;
 			private int mTextHeight;
 			private int yStart;
 			private Vector<String> lines = new Vector<String>();
 			
 			private Paint captionPaint = new Paint();
 
 			public CaptionCardState() {
 				computeTextHeight();
 			}
 			
 			public void start() {				
 				Log.w("Miskatonic", "STARTING CAPTION CARD STATE ***");
 			}
 
 			public void setCaptions(Vector<String> captions) {
 				mCaptions = captions;
 				updateCaption();
 			}
 			
 			public void computeTextHeight() {
 				setupCaptionFont();
 				Rect bounds = new Rect();
 				captionPaint.getTextBounds("AAA", 0, 1, bounds);
 				mTextHeight = (int) (Math.abs(bounds.top - bounds.bottom) * 1.5);
 			}
 			
 			public void draw(Canvas c) {
 				synchronized(lines) {
 					super.draw(c);
 					setupCaptionFont();
 									
 					for (int i = 0; i < lines.size(); i++) {					
 						c.drawText(lines.get(i), 
 								getWidth()/2, 
 								yStart + i * mTextHeight,
 								captionPaint);		 			
 					}
 				}
 			}
 
 			private void setupCaptionFont() {
 				captionPaint.setColor(Color.WHITE);
 				captionPaint.setTypeface(captionTypeface);
 				captionPaint.setTextAlign(Align.CENTER);
 				// Scale font size based on screen size.
 				if (isSmallScreen()) {
 					captionPaint.setTextSize(19.0f);
 				} else {
 					captionPaint.setTextSize(22.0f);
 				}
 					
 				captionPaint.setAntiAlias(true);
 			}
 
 			/** Update the caption display. */
 			private void updateCaption() {
 				synchronized(lines) {					
 					mCurrentCaption = mCaptions.get(0);
 					
 					// TODO: Use a REAL algorithm that DOESN'T suck. And precalculate!					
 					lines = new Vector<String>();
 					setupCaptionFont();
 					Utility.typesetText(mCurrentCaption, (int) (getWidth() * .7), lines, captionPaint);
 					
 					yStart = getHeight()/2 - (lines.size() * mTextHeight) / 2 + 10;
 				}
 			}
 			
 			@Override
 			public void onMousePress(int x, int y) {				
 				// Go to the next caption, if one exists.
 				mCaptions.removeElementAt(0);
 				if (mCaptions.isEmpty()) {
 					setState(StateType.Main);
 				} else {
 					updateCaption();
 				}
 			}
 			
 			public StateType getType() { return StateType.Caption; }
 		}
 
 		/** Navigates and displays journal entries. */
 		public class JournalState extends BasicGameState {			
 			private GameUI mUI;
 			private Vector<String> mLines;
 			private int mYStart;
 			private int mTextHeight;
 			
 			public JournalState() {
 				// Precalculate this.
 				computeTextHeight();
 				
 				showJournalSelection();
 			}
 			
 			public void computeTextHeight() {
 				setupJournalFont();
 				Rect bounds = new Rect();
 				GameUI.scratchPaint.getTextBounds("AAA", 0, 1, bounds);
 				mTextHeight = (int) (Math.abs(bounds.top - bounds.bottom) * 1.5);
 			}
 			
 			public void setupUI() {
 				mUI = new GameUI(getWidth(), getHeight(), GameThread.this);
 			}
 			
 			/** Show the journal selection screen. */
 			public void showJournalSelection() {
 				mLines = null;
 				
 				// Reset the UI.
 				setupUI();
 								
 				// Add the "back" button.
 				mUI.addControl(
 						new UIControlButton(72, 32, "Go Back",
 							new UIEvent() {								
 								public void execute(GameThread game) {
 									game.setState(StateType.Main);
 								}
 						}),						
 						GameUI.POSITION_BOTTOM);
 				
 				for (final ASTRAALJournal j : mExecutor.getJournalList()) {
 					mUI.addControl(new UIControlButtonListItem(80, 35, j.getTitle(), new UIEvent() {
 						public void execute(GameThread game) {
 							game.getJournalState().showJournal(j.getID());
 						}						
 					}), GameUI.POSITION_CENTER, true, DEFAULT_JOURNAL_UI_PADDING);
 				}
 
 			}
 			
 			/** Show a list of pages within a journal. */
 			public void showJournal(String id) {
 				mLines = null;
 				
 				// Reset the UI.
 				setupUI();
 				
 				// Add the "back" button.
 				mUI.addControl(
 						new UIControlButton(72, 32, "Go Back",
 							new UIEvent() {								
 								public void execute(GameThread game) {
 									game.getJournalState().showJournalSelection();
 								}
 						}),						
 						GameUI.POSITION_BOTTOM);
 				
 				// List entries.
 				final String jid = id;
 				for (final ASTRAALJournalPage p : mExecutor.getJournalPages(id) ) {
 					String label = (p.isUnlocked() ? p.getTitle() : JOURNAL_LOCKED_LABEL);					
 					mUI.addControl(
 							new UIControlButtonListItem((int)(getWidth() * .6), 32, label,
 									new UIEvent() {								
 								public void execute(GameThread game) {
 									if (p.isUnlocked()) {
 										game.getJournalState().showJournalPage(jid, p.getID());
 									}
 								}
 							}),						
 							GameUI.POSITION_CENTER, true, DEFAULT_JOURNAL_UI_PADDING);
 
 				}
 			}
 			
 			/** Show a journal page. */
 			public void showJournalPage(final String jid, String pid) {
 				setupUI();
 				// Add the "back" button.
 				mUI.addControl(
 						new UIControlButton(72, 32, "Go Back",
 							new UIEvent() {								
 								public void execute(GameThread game) {
 									game.getJournalState().showJournal(jid);
 								}
 						}),						
 						GameUI.POSITION_BOTTOM);
 				
 				
 				// Typeset the text.
 				ASTRAALJournalPage page = mExecutor.getJournalPage(jid, pid);
 				mLines = new Vector<String>(); 
 				Utility.typesetText(page.isUnlocked() ? page.getText() : JOURNAL_LOCKED_TEXT, (int) (getWidth() * .6), mLines);
 				
 				mYStart = getHeight()/2 - (mLines.size() * mTextHeight) / 2 + 10;
 			}
 
 			/** Pass down the click to the UI. */
 			public void onMouseRelease(int x, int y) {	
 				mUI.onClick(x, y);			
 			}
 			
 			private void setupJournalFont() {				
 				GameUI.scratchPaint.setColor(Color.WHITE);
 				GameUI.scratchPaint.setTypeface(captionTypeface);
 				GameUI.scratchPaint.setTextAlign(Align.CENTER);
 				GameUI.scratchPaint.setTextSize(18.0f);
 				GameUI.scratchPaint.setAntiAlias(true);
 			}
 			
 			@Override
 			public void draw(Canvas c) {
 				super.draw(c);
 				mUI.draw(c);
 				
 				if (mLines != null) {
 					setupJournalFont();
 					
 					for (int i = 0; i < mLines.size(); i++) {					
 						c.drawText(mLines.get(i), 
 								getWidth()/2, 
 								mYStart + i * mTextHeight,
 								GameUI.scratchPaint);					
 					}
 				}
 			}
 		} // JournalState
 		
 		
 		/** Main menu for the game. */
 		public class MainMenuState extends BasicGameState {
 			private GameUI mUI;
 			
 			public MainMenuState() {
 				setScene(new Scene(mCanvasWidth, mCanvasHeight));
 				setCamera(new Camera(0, 0, 
 						mCanvasWidth, mCanvasHeight, 
 						getScene()));
 				
 				setupUI();
 			}
 			
 			public void start() {				
 				Log.w("Miskatonic", "STARTING MAIN MENU STATE ***");
 			}
 			
 			public void setupUI() {
 				mUI = new GameUI(getWidth(), getHeight(), GameThread.this);
 				mUI.addControl(new UIControlButton(100, 40, "Start Game", new UIEvent() {
 					public void execute(GameThread t) {						
 						t.loadAndStartMainGameState();						
 					}
 				}),GameUI.POSITION_CENTER);
 			}
 			
 			//FIXME: Refactor common UI stuff to a superclass.
 			/** Pass down the click to the UI. */
 			public void onMouseRelease(int x, int y) {	
 				mUI.onClick(x, y);			
 			}
 			
 			@Override
 			public void draw(Canvas c) {
 				super.draw(c);
 				c.drawColor(Color.WHITE);
 				mUI.draw(c);
 			}
 			
 			public StateType getType() { return StateType.MainMenu; }
 		} // MainMenuState
 		
 		/** Displays the sanity minigame. */
 		public class SanityMiniGameState extends com.games.test1.State {
 			private static final int 	SANITY_MINIGAME_WINDOW_SIZE = 5;
 			private static final float 	SANITY_MINIGAME_STARTING_FREQUENCY = 2.0f;
 			private static final float 	SANITY_MINIGAME_STEP_SIZE = 0.2f;
 			private static final float 	SANITY_MINIGAME_ENDING_FREQUENCY = 1.0f;
 			private static final int 	SANITY_MINIGAME_STEPS_TO_CONFIRM = 5;
 			private static final float 	SANITY_MINIGAME_TOLERANCE_PERCENT = 0.04f;
 			
 			private Bitmap mSanityBackgroundImage;
 			
 			private long mTimeOfLastTap;
 			private Queue<Long> mTapIntervals = new LinkedList<Long>();			
 						
 			/** Current frequency that needs to be matched, in beats-per-second. */
 			private float mFrequency;
 			
 			/** Player's current average interval between taps. */
 			private int mPlayerAverageInterval;
 			
 			/** How long the player has successfully been matching the current frequency. */
 			private int mPlayerStepsConfirmed;
 			
 			
 			public SanityMiniGameState() {
 				
 			}
 			
 			public void start() {
 				Log.w("Miskatonic", "STARTING SANITY MINIGAME STATE ***");
 				
 				// Use the MGS's current background as ours.
 				mSanityBackgroundImage = getMainGameState().getBackground().getAnimation().getBitmap();
 				
 				mFrequency = SANITY_MINIGAME_STARTING_FREQUENCY;
 			}
 			
 			public void draw(Canvas c) {
 				c.save();
 				c.drawColor(Color.WHITE);				
 				
 				int A = 100;
 				float t = getTimeInSeconds();
 				
 				// TEMP DRAWING STUFF
 				GameUI.scratchPaint.setColor(Color.RED);
 				GameUI.scratchPaint.setTextAlign(Paint.Align.LEFT);
 				c.drawRect(0,0,(float) (A + A * Math.sin(mFrequency * 2 * Math.PI * t)),50, GameUI.scratchPaint);
 				c.drawText("" + mTimeOfLastTap, 10, 80, GameUI.scratchPaint);
 				synchronized (mTapIntervals) {
 					c.drawText(mTapIntervals.toString(), 10, 100, GameUI.scratchPaint);
 					
 					c.drawText("" + mPlayerAverageInterval + " vs " + (1000f / mFrequency), 10, 120, GameUI.scratchPaint);
 				}
 				/*
 				int numSlices = 10;
 				for (int i = 0; i < numSlices; i++) {					
 					c.translate((float) Math.sin(mMod++ + (float)i/10) * 5, 0);
 					Rect src = new Rect(0, i * (tempImage.getHeight() / numSlices), tempImage.getWidth(), (i+1) * (tempImage.getHeight() / numSlices));
 					Rect dst = new Rect(0, i * (tempImage.getHeight() / numSlices), tempImage.getWidth(), (i+1) * (tempImage.getHeight() / numSlices));
 					c.drawBitmap(tempImage, src, dst, GameUI.scratchPaint);
 				}
 				*/
 				
 				c.restore();
 			}
 
 			private float getTimeInSeconds() {
 				return (float)(System.currentTimeMillis() % 100000) / 1000f;
 			}
 			
 			@Override
 			public void onMousePress(int x, int y) {				
 				long currentTimeMillis = System.currentTimeMillis();
 				synchronized (mTapIntervals) {
 					// Register the new tap and recompute the average using it.
 					registerTap(currentTimeMillis);					
 					recalculateAverageInterval();					
 				}
 				
 				// If the new average is within tolerance, increment confirmed counter. Otherwise, reset it.
 				int interval = convertFrequencyToInterval(mFrequency);
 				if (Math.abs(mPlayerAverageInterval - interval) 
 						< SANITY_MINIGAME_TOLERANCE_PERCENT * interval) {
 					if (++mPlayerStepsConfirmed > SANITY_MINIGAME_STEPS_TO_CONFIRM) {
 						decreaseFrequency();
 					}
 				} else {
 					mPlayerStepsConfirmed = 0;
 				}
 				
 			}
 
 			/** Increase velocity, decrease altitude, reverse direction! */
 			private void decreaseFrequency() {
 				mFrequency -= SANITY_MINIGAME_STEP_SIZE;				
 				mPlayerStepsConfirmed = 0;				
 				
 				if (mFrequency < SANITY_MINIGAME_ENDING_FREQUENCY) {
 					onSuccess();
 				}
 			}
 
 			/** Callback when the player wins the game. */
 			private void onSuccess() {
 				setState(StateType.Main);				
 			}
 
 			private int convertFrequencyToInterval(float freq) {				
 				return (int) (1000f / freq);
 			}
 
 			private void recalculateAverageInterval() {
 				int sum = 0;					
 				for (long interval : mTapIntervals) {
 					sum += interval;
 				}
 				if (sum > 0)
 					mPlayerAverageInterval = sum / mTapIntervals.size();
 			}
 
 			private void registerTap(long currentTimeMillis) {
 				if (mTimeOfLastTap != 0) {
 					mTapIntervals.add(currentTimeMillis - mTimeOfLastTap);
 					if (mTapIntervals.size() > SANITY_MINIGAME_WINDOW_SIZE) {
 						mTapIntervals.remove();
 					}
 				}
 				mTimeOfLastTap = currentTimeMillis;
 			}
 			
 			public StateType getType() { return StateType.SanityMiniGame; }
 
 		}
 		
 		/** Temporary state that switches back immediately. */
 		public class InitializationState extends com.games.test1.State {
 
 			public void start() {				
 				Log.w("Miskatonic", "STARTING INIT STATE ***");
 			}
 
 			public void draw(Canvas c) {
 				c.drawColor(Color.BLACK);
 			}
 			
 			public void run() {
 				startGame("astraal_basic.xml");	
 				getMainGameState();
 				
 				// If restoring, do not show main menu. Otherwise, do.
 				if (mBundleToLoad != null) {
 					loadAndStartMainGameState();					
 				} else {
 					setState(StateType.MainMenu);
 				}
 				
 			}
 			
 			public StateType getType() { return StateType.Initialization; }
 		} // InitializationState
 		
 		/** State that simply draws a black screen to hide any on-going processes. */
 		public class LoadingState extends com.games.test1.State {
 			public void start() {
 				Log.w("Miskatonic", "STARTING LOADING STATE ***");
 			}
 
 			public void draw(Canvas c) {
 				c.drawColor(Color.BLACK);
 				// Maybe later we can throw in a "loading" message.
 			}					
 			
 			public StateType getType() { return StateType.Loading; }
 
 		}		
 	} // GameThread
 
 	/**
 	 * Create thread, do misc stuff.
 	 * @param context
 	 * @param attrs
 	 */
 	public GameView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		this.context = context;
 
 		// register our interest in hearing about changes to our surface
 		SurfaceHolder holder = getHolder();
 		holder.addCallback(this);
 
 		// create thread only; it's started in surfaceCreated()
 		thread = new GameThread(holder, context, new Handler() {      });
 
 		setFocusable(true); // make sure we get key events
 	}
 
 
 	/** Functions from LunarView, which control the thread and pass input into it. **/
 	/**
 	 * Fetches the animation thread corresponding to this LunarView.
 	 * 
 	 * @return the animation thread
 	 */
 	public GameThread getThread() {
 		return thread;
 	}
 
 	/**
 	 * Standard window-focus override. Notice focus lost so we can pause on
 	 * focus lost. e.g. user switches to take a call.
 	 */
 	@Override
 	public void onWindowFocusChanged(boolean hasWindowFocus) {
 		if (!hasWindowFocus) thread.pause();
 	}
 
 	/** Callback on "mouse" events. */
 	public boolean onTouchEvent(MotionEvent event) {
 		if (event.getAction() == MotionEvent.ACTION_UP) {
 			// Pass into thread.
 			thread.onMouseRelease(event);
 		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
 			thread.onMouseMove(event);
 		} else if (event.getAction() == MotionEvent.ACTION_DOWN) {
 			thread.onMousePress(event);
 		}
 		return true;
 	}
 
 
 
 	/** Callback invoked when the surface dimensions change. */
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 		thread.setSurfaceSize(width, height);
 	}
 
 	/**
 	 * Callback invoked when the Surface has been created and is ready to be
 	 * used.
 	 */
 	public void surfaceCreated(SurfaceHolder holder) {
 		// start the thread here so that we don't busy-wait in run()
 		// waiting for the surface to be created
 		
 		if (thread.getState() == Thread.State.TERMINATED) {
 			thread = new GameThread(getHolder(), getContext(), getHandler());
 			thread.loadFromBundle(mStateBundle);
 		}
 		thread.setRunning(true);		
 		thread.start();
  	}
 
 	/**
 	 * Callback invoked when the Surface has been destroyed and must no longer
 	 * be touched. WARNING: after this method returns, the Surface/Canvas must
 	 * never be touched again!
 	 */
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		// we have to tell thread to shut down & wait for it to finish, or else
 		// it might touch the Surface after we return and explode
 		boolean retry = true;
 		thread.setRunning(false);
 		
 		saveState();
 		while (retry) {
 			try {
 				thread.join();
 				retry = false;
 			} catch (InterruptedException e) {
 				
 			}
 		}
 	}
 
 	/** Internally save our state. */
 	private void saveState() {
 		mStateBundle = new Bundle();
 		thread.saveToBundle(mStateBundle);
 	}
 }
