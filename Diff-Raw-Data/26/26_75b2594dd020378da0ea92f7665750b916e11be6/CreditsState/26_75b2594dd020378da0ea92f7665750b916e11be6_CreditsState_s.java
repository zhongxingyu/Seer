 package rsmg.controller;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.UnicodeFont;
 import org.newdawn.slick.font.effects.ColorEffect;
 import org.newdawn.slick.state.StateBasedGame;
 
 /**
  * The game enters this state when the last level has been completed.
  * 
  * @author Daniel Jonsson
  *
  */
 class CreditsState extends State {
 
 	/**
 	 * Font to draw text on the screen.
 	 */
 	private UnicodeFont font;
 	
 	/**
 	 * The text that should be rolling on the screen.
 	 */
 	private String credits;
 	
 	/**
 	 * On which coordinate the credits should be drawn.
 	 */
 	private float yPosition;
 	private float xPosition;
 
 	CreditsState(int stateID) {
 		super(stateID);
 	}
 
 	/**
	 * Initialise the credit text
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
 		
 		// Load the font which is used to write text to the screen
 		font = new UnicodeFont(new java.awt.Font("Courier New", java.awt.Font.PLAIN, 30));
 		font.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
 		font.addAsciiGlyphs();
 		font.loadGlyphs();
 		
 		// Credits text
 		credits = "Robots Stole My Girlfriend";
		
		credits += "\n\n\nStory writer: Johan Gr�nvall";
		credits += "\n\nProgramming: Daniel Jonsson\n             Johan Gr�nvall\n             Johan Rign�s";
		credits += "\n\nArt design: Daniel Jonsson\n            Johan Gr�nvall\n            Johan Rign�s";
 
		credits += "\n\n\n\nIn memory to Johannes Ulén and Victor R�dmark.";
 		
 		// Position for the text
 		xPosition = gc.getWidth() / 10;
 		yPosition = gc.getHeight();
 	}
 
 	/**
 	 * Draw out the credit-text to the screen and listen for when to stop and go to level-selection screen.
 	 */
 	@Override
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException {
 		font.drawString(xPosition, yPosition, credits);
 		if (gc.getInput().isKeyPressed(Input.KEY_ESCAPE)) {
 			MusicHandler.startTrack(MusicHandler.Track.MENU_MUSIC);
 			sbg.enterState(Controller.LEVEL_SELECTION_STATE);
 		}
 	}
 
 	/**
 	 * Make the credit-text move upwards
 	 */
 	@Override
 	public void update(GameContainer gc, StateBasedGame sbg, int delta)
 			throws SlickException {
 		yPosition -= (float)delta/10;
 	}
 
 }
