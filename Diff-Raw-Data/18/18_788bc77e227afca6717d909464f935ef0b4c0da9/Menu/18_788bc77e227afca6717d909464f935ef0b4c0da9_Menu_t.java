 package javagame;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.UnicodeFont;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 /**
  * @author Kevin Patton
  * This class represents the main menu state of the game.
  */
 public class Menu extends BasicGameState {
 	/** the font size for the title of the game */
 	public static final int FONT_SIZE = 65;
 	
 	public  MenuStrip   menu;
 	private UnicodeFont titleFont;
 	private Image       bg;
 	private Play        play;
 	private int         state;
 	
 	public Menu(int state) {
 		this.state = state;
 	}
 	
 	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
 		//build the MenuStrip
 		int y = 0;
 		Interactable start = new MenuItemString("New Game", 0, y);
 		int lineHeight = start.getHeight();
 		Interactable options = new MenuItemString("Options", 0, y += 
 				lineHeight);
 		Interactable credits = new MenuItemString("Credits", 0, y +=
 				lineHeight);
 		Interactable quit = new MenuItemString("Quit", 0, y +=
 				lineHeight);
 		
 		menu = new MenuStrip(start, options, credits, quit);
 		menu.setLocation(180, 200);
 		
 		//load font
 		titleFont = Utility.getFont(FONT_SIZE);
 		
 		//load background image
 		bg = new Image("res/tetripocalypse.png");
 	}
 	
	//renders the main menu
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
 			throws SlickException {
 		g.drawImage(bg, 0, 0);
 		menu.render(g);
 		//draw a nifty shadow
 		titleFont.drawString(100, 100, "TETRIPOCALYPSE", Color.black);
 		titleFont.drawString(100, 97, "TETRIPOCALYPSE", Color.orange);
 	}
 	
	//updates the main menu
 	public void update(GameContainer gc, StateBasedGame sbg, int delta)
 			throws SlickException {
 		Input input = gc.getInput();
 		int selection = menu.acceptInput(input);
 		switch (selection) {
		case 0: //new game/resume game
 			input.clearKeyPressedRecord();
 			if (!play.inGame)
 				sbg.enterState(5);
 			else
 				sbg.enterState(1);
 			break;
		case 1: //options
 			//TODO implement the options screen
			//sbg.enterState(OPTION_SCREEN);
 			break;
		case 2: //credits
 			input.clearKeyPressedRecord();
 			sbg.enterState(3);
 			break;
		case 3: //quit
 			System.exit(0);
 			break;
 		}
 	}
 	
 	
 	/** Sets a reference to the game so we know whether
 	 *  to start a new game or to resume an existing one.
 	 *  @param p the game */
 	public void setPlay(Play p) {
 		play = p;
 	}
 	
 	/**
 	 * Returns the ID of this BasicGameState.
 	 * @return the ID of this BasicGameState
 	 */
 	public int getID() {
 		return state;
 	}
 }
