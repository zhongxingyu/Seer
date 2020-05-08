 package se.chalmers.tda367.group15.game.states;
 
 import java.awt.Font;
 
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.KeyListener;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.TrueTypeFont;
 
 import se.chalmers.tda367.group15.game.controllers.ScoreController;
 import se.chalmers.tda367.group15.game.settings.Constants;
 
 /**
  * State displayed when game is won
  * 
  * @author Peter
  * 
  */
 public class GameWonState extends AbstractMenuBasedState {
 	private final static int MAX_NAME_LENGTH = 15;
 	private final TrueTypeFont textFont = new TrueTypeFont(new Font(
 			"Monospaced", Font.BOLD, 32), true);
 	private final static String ALLOWED_NAME_CHARS = "abcdefghijklmnopqrstuvwxyz ABCDEFGHIJKLMNOPQRSTUVWXYZ";
 
 	private ScoreController scoreController;
 
 	private String name = "";
 
 	/**
 	 * Creates a new GameWonState.
 	 * 
 	 * @param id
 	 *            The int used to identify the state.
 	 */
 	public GameWonState(int id) {
 		super(id);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void init() {
 		try {
 			setBackground(new Image("res/menu/backgroundEpicWin.png"));
 		} catch (SlickException e) {
 			if (Constants.DEBUG) {
 				e.printStackTrace();
 			}
 		}
 
 		input.addKeyListener(new keyPressListener());
 	}
 
 	public void setScoreController(ScoreController scoreController) {
 		this.scoreController = scoreController;
 	}
 
 	@Override
 	protected void initMenuItems() {
 	}
 
 	@Override
 	public void render(Graphics g) {
 		super.render(g);
 		int nameStrLen = textFont.getWidth("Name:");
 		textFont.drawString(Constants.MENU_UPPER_X - nameStrLen - 10,
 				Constants.MENU_UPPER_Y + 50, "Name:");
 
 		StringBuilder paddedName = new StringBuilder();
		for (int i = 0; i < name.length(); i++) {
 			if (paddedName.length() > 0) {
 				paddedName.append(' ');
 			}
 			paddedName.append(name.charAt(i));
 		}
 		textFont.drawString(Constants.MENU_UPPER_X,
 				Constants.MENU_UPPER_Y + 50, "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _");
 		textFont.drawString(Constants.MENU_UPPER_X,
 				Constants.MENU_UPPER_Y + 50, paddedName);
 
 	}
 
 	@Override
 	public void update(int delta) {
 		super.update(delta);
 
 		// If enter key is pressed, save score and show high score
 		if (input.isKeyPressed(Input.KEY_ENTER)) {
 			saveName();
 			toMenu();
 		}
 	}
 
 	private void saveName() {
 		if (scoreController != null) {
 			scoreController.saveScore(name);
 			// Force a refresh of the high score state
 			((MenuStateHighScore) game
 					.getState(Constants.GAME_STATE_MENU_HIGH_SCORE))
 					.updateHighScoreList();
 		}
 
 	}
 
 	private void toMenu() {
 		name = "";
 		scoreController = null;
 		game.enterState(Constants.GAME_STATE_MENU_HIGH_SCORE);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void escpAction() {
 		toMenu();
 	}
 
 	private class keyPressListener implements KeyListener {
 		@Override
 		public void keyPressed(int keyCode, char c) {
 
 			if (keyCode == Input.KEY_BACK && !name.isEmpty()) {
 				name = name.substring(0, name.length() - 1);
 			} else if (ALLOWED_NAME_CHARS.indexOf(c) != -1
 					&& name.length() < MAX_NAME_LENGTH) {
 				name += c;
 			}
 		}
 
 		@Override
 		public void inputEnded() {
 		}
 
 		@Override
 		public void inputStarted() {
 		}
 
 		@Override
 		public boolean isAcceptingInput() {
 			return scoreController != null;
 		}
 
 		@Override
 		public void setInput(Input input) {
 		}
 
 		@Override
 		public void keyReleased(int keyCode, char c) {
 		}
 	}
 }
