 package indaprojekt;
 
 import indaprojekt.Button.ActionPerformer;
 
 import java.awt.geom.Rectangle2D;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class PauseState extends ButtonMenuState {
 
 	private final int MIDDLE_X = Game.WINDOW_WIDTH/2;
 	private int pauseTextY = 50;
 	private int resumeGameY = 250;
 	private int mainMenuY = 400;
 		
 	public PauseState(int stateID) {
 		super(stateID);
 	}
 
 	@Override
 	public void init(final GameContainer gc, final StateBasedGame game)
 			throws SlickException {
 		//background is in ButtonMenuState
 		background = new Image("res//images//bakgrund.png");
 		
 		// Resume button:
 		Image resumeOption = new Image("res//images//resumeGame.png");
 		int resumeGameX = MIDDLE_X - (resumeOption.getWidth()/2);
 		int resumeW = resumeOption.getWidth();
 		int resumeH = resumeOption.getHeight();
 		Button resumeButton = new Button(resumeOption, resumeOption,
 				new Rectangle2D.Float(resumeGameX, resumeGameY, resumeW, resumeH));
 		
 		resumeButton.setAction(new ActionPerformer() {
 			@Override
 			public void doAction() {
 				game.enterState(IceIceBabyGame.GAME_PLAY_STATE);
 			}
 		});
 		addButton(resumeButton);
 
 		Image pauseImage = new Image("res//images//pause.png");
 		int pauseTextX = MIDDLE_X - (pauseImage.getWidth()/2);
 		Button pauseText = new Button(pauseImage, pauseImage, 
 				new Rectangle2D.Float(pauseTextX, pauseTextY, 
 						pauseImage.getWidth(), pauseImage.getHeight()));
 		addButton(pauseText);
 		
 		// Main menu button.
 		Image mainMenuImage = new Image("res//images//mainMenu.png");
 		Image mainMenuImageH = new Image("res//images//mainMenu.png");
 		int mainMenuX = MIDDLE_X - (mainMenuImage.getWidth()/2);
 		Button mainMenuButton = new Button(mainMenuImage, mainMenuImageH,
 				new Rectangle2D.Float(mainMenuX, mainMenuY,
 						mainMenuImage.getWidth(), mainMenuImage.getHeight()));
		mainMenuButton.setAction(new ActionPerformer() {
 			@Override
 			public void doAction() {
 				game.enterState(IceIceBabyGame.MAIN_MENU_STATE);
 			}
 		});
 		addButton(mainMenuButton);
 	}
 }
