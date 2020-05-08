 package javagame;
 
 //import org.lwjgl.input.Mouse;
 import org.newdawn.slick.*;
 import org.newdawn.slick.state.*;
 
 public class Menu extends BasicGameState {
 	
 	//private String mousePositionInfo = "No input yet!";
 	//private Image gordon;
 	//private int gordonX = 200;
 	//private int gordonY = 200;
 	public static final int FONT_SIZE = 65;
 	
 	public MenuStrip menu;
 	private UnicodeFont titleFont;
 	private Play play;
 	
 	public Menu(int state) {
 	}
 	
 	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
 		
		titleFont = Utility.getFont(FONT_SIZE);
 		
 		int y = 0;
 		Interactable start = new MenuItemString("New Game", 0, y);
 		int lineHeight = start.getHeight();
 		Interactable options = new MenuItemString("Options", 0, y += lineHeight);
 		Interactable credits = new MenuItemString("Credits", 0, y += lineHeight);
 		Interactable quit = new MenuItemString("Quit", 0, y += lineHeight);
 		menu = new MenuStrip(start, options, credits, quit);
 		menu.setLocation(180, 200);
 	}
 	
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException {
 		g.drawImage(new Image("res/tetripocalypse.png"), 0, 0);
 		menu.render(g);
 		titleFont.drawString(100, 100, "TETRIPOCALYPSE", Color.black);
 		titleFont.drawString(100, 97, "TETRIPOCALYPSE", Color.orange);
 	}
 	
 	public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException {
 		Input input = gc.getInput();
 		int selection = menu.acceptInput(input);
 		switch (selection) {
 		case 0:
 			input.clearKeyPressedRecord();
 			menu.gameActive();
 			if (!play.inGame)
 				sbg.enterState(5);
 			else
 				sbg.enterState(1);
 			break;
 		case 1:
 			//TODO: this stuff.
 			//sbg.enterState(OPTION SCREEN);
 			break;
 		case 2:
 			input.clearKeyPressedRecord();
 			sbg.enterState(3);
 			break;
 		case 3:
 			System.exit(0);
 			break;
 		}
 		
 		//int mouseXPos = Mouse.getX();
 		//int mouseYPos = Mouse.getY();
 		//mouse = "Mouse position x: " + mouseXPos + " y: " + mouseYPos;
 		//Input input = gc.getInput();
 		//if (input.isKeyDown(Input.KEY_UP)) { gordonY -= 4; }
 		//if (input.isKeyDown(Input.KEY_DOWN)) { gordonY += 4; }
 		//if (input.isKeyDown(Input.KEY_LEFT)) { gordonX -= 4; }
 		//if (input.isKeyDown(Input.KEY_RIGHT)) { gordonX += 4; }
 	}
 	
 	public void setPlay(Play p) {
 		play = p;
 	}
 	
 	public int getID() {
 		return 0; //yeah, look at the constructor..
 	}
 }
