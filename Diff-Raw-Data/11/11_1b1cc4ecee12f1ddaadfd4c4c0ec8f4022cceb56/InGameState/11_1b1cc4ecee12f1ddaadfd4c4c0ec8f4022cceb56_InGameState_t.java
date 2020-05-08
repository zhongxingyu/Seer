 package projectrts.controller;
 
 import java.util.List;
 
 import projectrts.io.ImageManager;
 import projectrts.model.IGame;
 import projectrts.view.GameGUIView;
 import projectrts.view.GameView;
 
 import com.jme3.app.Application;
 import com.jme3.app.SimpleApplication;
 import com.jme3.app.state.AbstractAppState;
 import com.jme3.app.state.AppStateManager;
 import com.jme3.math.Vector3f;
 
 import de.lessvoid.nifty.Nifty;
 
 /**
  * The in-game state that controls everything inside the game.
  * 
  * @author Markus Ekstrm
  * 
  */
 public class InGameState extends AbstractAppState {
 	private SimpleApplication app;
 	private final IGame game;
 	private InputController input;
 	private GameGUIView guiView;
 	private final Nifty nifty;
 
 	// TODO Markus: Add javadoc (for this public field)
 	public static final float MODEL_TO_WORLD = 0.05f;
 
 	// TODO Markus: Add javadoc
 	public InGameState(IGame game, Nifty nifty) {
 		super();
 		this.game = game;
 		this.nifty = nifty;
 	}
 
 	/**
 	 * Initializes the state.
 	 * 
 	 * Do not manually call this method! This method is invoked automatically by
 	 * SimpleApplication.
 	 * 
 	 * @param stateManager
 	 *            SimpleApplication's stateManager
 	 * @param app
 	 *            A class extending SimpleApplication
 	 */
 	@Override
 	public void initialize(AppStateManager stateManager, Application app) {
 		super.initialize(stateManager, app);
 		this.app = (SimpleApplication) app; // cast to a more specific class
 		// init stuff that is independent of whether state is PAUSED or RUNNING
 		// // modify scene graph...
 		initializeImages();
 		GameView view = new GameView(this.app, game);
 		guiView = new GameGUIView(nifty, game);
 
 		input = new InputController(this.app, game, view);
 		new InGameGUIController(input, nifty, guiView, game.getAbilityManager());
 
 		initializeCamera();
 		// Initialize view last, after model and controller, since its
 		// initialization is dependent on the other's.
 		view.initialize();
 		guiView.initialize();
 		input.addListener(guiView);
 	}
 
 	private void initializeImages() {
 		// Add images to ImageManager
 		List<String> names = game.getAbilityManager().getExistingAbilityNames();
 		for (String str : names) {
 			ImageManager.addImage(str, "/assets/gui/" + str + ".png");
 		}
 		ImageManager.addImage("NoImage", "/assets/gui/NoImage.bmp");
 	}
 
 	/**
 	 * This method is probably called automatically when changing states, I
 	 * haven't looked it up though but I think it is not supposed to be called
 	 * manually.
 	 */
 	@Override
 	public void cleanup() {
 		// TODO Markus: PMD: Overriding method merely calls super
 		super.cleanup();
 		// unregister all my listeners, detach all my nodes, etc... // modify scene graph...
 	}
 
 	/**
 	 * The update loop, do not call manually!
 	 * 
 	 * Invoked automatically by SimpleApplication.
 	 * 
 	 * @param tpf
 	 *            Time-per-frame
 	 */
 	@Override
 	public void update(float tpf) {
 		if (isEnabled()) {
 			// do the following while game is RUNNING // modify scene graph...
 			input.update(tpf, this.isEnabled());
 			game.update(tpf);
 			guiView.update(tpf);
 		}
 		// do something in an else statement while game is PAUSED, e.g. play an
 		// idle animation.
 		// ...
 	}
 
 	/**
 	 * Initializes the camera to the center of the playable world.
 	 */
 	private void initializeCamera() {
 		int worldWidth = game.getWorld().getWorldWidth();
 		int worldHeight = game.getWorld().getWorldHeight();
 		app.getCamera().setLocation(
 				new Vector3f((worldWidth / 2) * MODEL_TO_WORLD,
 						-(worldHeight / 2) * MODEL_TO_WORLD, app.getCamera()
 								.getLocation().getZ()));
 	}
 }
