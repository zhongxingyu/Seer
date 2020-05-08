 package se.chalmers.tda367.group15.game.states;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 import se.chalmers.tda367.group15.game.constants.Constants;
 import se.chalmers.tda367.group15.game.menu.Button;
 
 /**
  * A state representing a Main Menu in a graphical application. Class based on
  * tutorial for writing menus in Slick2D Originally posted on
  * http://slick.javaunlimited.net/ by user shiroto. Remade to suit our purpose.
  * 
  * @author Carl Jansson
  * @version 2.0
  */
 public class MenuStateMain extends AbstractMenuBasedState {
 
 	/**
 	 * Resume button must be possible to set visible at will.
 	 */
 	private Button resumeGameButton;
 
 	/**
 	 * the upper left corner of button group
 	 */
 	private int MENUX = 200;
 	private int MENUY = 100;
 
 	/**
 	 * True if you have started a game.
 	 */
 	private boolean existsGameCurrently;
 
 	/**
 	 * creates a new MainMenuState.
 	 * 
 	 * @param id
 	 *            The int used to identify the state.
 	 */
 	public MenuStateMain(int id) {
 		super(id);
 		this.existsGameCurrently = false;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void init() {
 		this.initMenuItems();
 		try {
 			setBackground(new Image("res/menu/background.png"));
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void initMenuItems() {
 		try {
 			this.createButtons();
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Creates the buttons. Separate method to avoid big try catch.
 	 * 
 	 * @throws SlickException
 	 *             If a image fail to load.
 	 */
 	private void createButtons() throws SlickException {
 		Image newGameImage = new Image("res/menu/newGame.png");
 		Image newGameImageMO = new Image("res/menu/newGameMO.png");
 		Image quitImage = new Image("res/menu/quit.png");
 		Image quitImageMO = new Image("res/menu/quitMO.png");
 		Image optionsImage = new Image("res/menu/options.png");
 		Image optionsImageMO = new Image("res/menu/optionsMO.png");
 		Image resumeImage = new Image("res/menu/resumeGame.png");
 		Image resumeImageMO = new Image("res/menu/resumeGameMO.png");
 
 		// Resume game button. This Button should only be visible when there is
 		// an active game.
 		resumeGameButton = new Button(container, resumeImage, resumeImageMO,
 				MENUX, MENUY) {
 			@Override
 			public void performAction() {
 				// Returns to currently active game.
 				game.enterState(Constants.GAME_STATE_PLAYING);
 			}
 		};
 		// Start a new game.
 		Button newGameButton = new Button(container, newGameImage,
 				newGameImageMO, MENUX, MENUY + 50) {
 			@Override
 			public void performAction() {
 
 				try { // Init PlayState to reset game.
 					if (existsGameCurrently) {
 						game.getState(Constants.GAME_STATE_PLAYING).init(
 								container, game);
 					} else {
 						existsGameCurrently = true;
 					}
 				} catch (SlickException e) {
 					e.printStackTrace();
 				}
 
 				game.enterState(Constants.GAME_STATE_PLAYING);
 				resumeGameButton.setButtonActive(true);
 			}
 		};
 		// open options
 		Button optionsButton = new Button(container, optionsImage,
 				optionsImageMO, MENUX, MENUY + 100) {
 			@Override
 			public void performAction() {
 				game.enterState(Constants.GAME_STATE_OPTIONS_MENU);
 			}
 		};
 		// Quit application
 		Button exitButton = new Button(container, quitImage, quitImageMO,
 				MENUX, MENUY + 150) {
 			@Override
 			public void performAction() {
 				if (((StateController) game).closeRequested()) {
 					container.exit();
 				}
 			}
 		};
 
 		// Resume game button should not be visible before new game.
 		this.resumeGameButton.setButtonActive(false);
 
 		// add items to state.
 		this.addMenuItem(resumeGameButton);
 		this.addMenuItem(newGameButton);
 		this.addMenuItem(optionsButton);
 		this.addMenuItem(exitButton);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void escpAction() {
 		// Escape only usable if game started.
 		if (existsGameCurrently) {
 			// Returns you to game.
 			game.enterState(Constants.GAME_STATE_PLAYING);
 		}
 	}
 
 	@Override
 	public void gameOver(boolean win) {
 		resumeGameButton.setButtonActive(false);
 	}
 }
