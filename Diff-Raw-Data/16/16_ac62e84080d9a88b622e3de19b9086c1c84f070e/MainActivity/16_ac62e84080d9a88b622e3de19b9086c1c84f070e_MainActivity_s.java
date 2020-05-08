 package ultraextreme.controller;
 
 import org.andengine.engine.camera.Camera;
 import org.andengine.engine.options.EngineOptions;
 import org.andengine.engine.options.ScreenOrientation;
 import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.andengine.entity.scene.Scene;
 import org.andengine.opengl.font.Font;
 import org.andengine.opengl.font.FontFactory;
 import org.andengine.ui.activity.SimpleBaseGameActivity;
 
 import ultraextreme.view.SpriteFactory;
 import android.content.Context;
 import android.graphics.Typeface;
 import android.hardware.SensorManager;
 
 public class MainActivity extends SimpleBaseGameActivity implements
 		IControllerListener {
 
 	private GameController gameController;
 	private MainMenuController mainMenuController;
 	private SpriteFactory spriteFactory;
 	// private GameModel gameModel;
 	private Font defaultFont;
 	private Camera camera;
 	private Scene currentScene;
 
 	private static int CAMERA_WIDTH;
 	private static int CAMERA_HEIGHT;
 
 	@Override
 	public EngineOptions onCreateEngineOptions() {
 		CAMERA_WIDTH = getResources().getDisplayMetrics().widthPixels;
 		CAMERA_HEIGHT = getResources().getDisplayMetrics().heightPixels;
 		camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
 		return new EngineOptions(true, ScreenOrientation.PORTRAIT_SENSOR,
 				new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
 	}
 
 	@Override
 	protected void onCreateResources() {
 		spriteFactory = new SpriteFactory(this);
 		defaultFont = FontFactory.create(this.getFontManager(),
 				this.getTextureManager(), 256, 256,
 				Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 32);
 		defaultFont.load();
 	}
 
 	@Override
 	protected Scene onCreateScene() {
 		gameController = new GameController(
 				this.getVertexBufferObjectManager(),
				(SensorManager) this.getSystemService(Context.SENSOR_SERVICE),
 				spriteFactory, this);
 		mainMenuController = new MainMenuController(camera, defaultFont,
 				this.getVertexBufferObjectManager());
 
 		gameController.addListener(this);
 		mainMenuController.addListener(this);
 
 		setScene(mainMenuController.getScene());
 		return currentScene;
 	}
 
 	@Override
 	public void controllerListenerUpdate(ControllerEvent event) {
 		switch (event.getEventType()) {
 		case SWITCH_TO_GAME:
 			setScene(gameController.getScene());
 			break;
 
 		default:
 			break;
 		}
 	}
 
 	private void setScene(Scene scene) {
 		currentScene = scene;
 		getEngine().setScene(currentScene);
 	}
 
 }
