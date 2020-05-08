 package javagame;
 
 import java.util.ArrayList;
 import org.newdawn.slick.*;
 
 /**
  * @author Kevin Patton
  * A menu with various choices. Menu items can be added and removed.
  * Control schemes can be refined, and the menu selection can be captured
  * and returned. Can also launch various game states.
  */
 public class MenuStrip {
 	
 	public static final int DEFAULT_SELECTION = 0;
 	public static final int DEFAULT_NEXT = Input.KEY_DOWN;
 	public static final int DEFAULT_PREVIOUS = Input.KEY_UP;
 	public static final int DEFAULT_SELECT = Input.KEY_ENTER;
 	
 	private ArrayList<Interactable> menuItems;
 	
 	private Animation cursorAnimation;
 	/** The sound the plays when the user changes the menu selection. */
 	private Sound selectSound;
 	/**
 	 * The menu item that currently has 'focus.' In other words,
 	 * if the user presses enter while selection is 2, then the
 	 * second menu item's code will launch.
 	 */
 	private int selection = DEFAULT_SELECTION;
 	/** The key to press in order to go to the next menu item. */
 	private int nextKey = DEFAULT_NEXT;
 	/** The key to press in order to go to the previous menu item. */
 	private int previousKey = DEFAULT_PREVIOUS;
 	/** The key to press in order to select the current menu item. */
 	private int selectKey = DEFAULT_SELECT;
 	private int size = 0;
 	private int x = 0;
 	private int y = 0;
 	private int lineHeight = 0;
 	
 	/**
 	 * Constructor that allows you to initialize the menu items.
 	 * @param items the menu items to be displayed and chooseable
 	 */
 	public MenuStrip(Interactable...items) {
 		menuItems = new ArrayList<Interactable>();
 		Image cursorSheet = null;
 		try {
 			selectSound = new Sound("res/rotate.wav");
 			cursorSheet = new Image("res/cursor.png");
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 		cursorAnimation = new Animation(new SpriteSheet(cursorSheet, 32, 32), 100);
 		for (Interactable item : items) {
 			menuItems.add(item);
 			size++;
 		}
		if (size > 0)
			lineHeight = menuItems.get(0).getHeight();
 	}
 	
 	/**
 	 * Default constructor.
 	 */
 	public MenuStrip() {
 		menuItems = new ArrayList<Interactable>();
 		size = 0;
 	}
 	
 	public void setLocation(int x, int y) {
 		this.x = x;
 		this.y = y;
 	}
 	
 	public void gameActive() {
 		menuItems.get(0).changeItem("Resume Game");
 	}
 	
 	public void gameDone() {
 		menuItems.get(0).changeItem("New Game");
 	}
 	
 	public int getSelection() {
 		return selection;
 	}
 	
 	public void render(Graphics g) {
 		cursorAnimation.draw(x - 40, y + selection * lineHeight);
 		for (Interactable i : menuItems) {
 			i.render(x, y);
 		}
 	}
 	
 	/**
 	 * Obtains input from the parent class and performs
 	 * the necessary updates to this MenuStrip as a result.
 	 * @param input an object containing the user's input information
 	 * @return the menu selection chosen by the player
 	 */
 	public int acceptInput(Input input) {
 		if (input.isKeyPressed(nextKey)) {
 			advanceSelection();
 			selectSound.play();
 		}
 		if (input.isKeyPressed(previousKey)) {
 			regressSelection();
 			selectSound.play();
 		}
 		if (input.isKeyPressed(selectKey)) {
 			System.out.println("Selected " + selection + ".");
 			return selection;
 		}
 		return -1;
 	}
 	
 	/**
 	 * Changes the menu selection to be one before whatever
 	 * it is currently.
 	 */
 	private void regressSelection() {
 		if (selection > 0)
 			selection--;
 		else if (selection == 0)
 			selection = size - 1;
 	}
 	
 	/**
 	 * Changes the menu selection to be one after whatever
 	 * it is currently.
 	 */
 	private void advanceSelection() {
 		if (selection < size - 1)
 			selection++;
 		else if (selection == size - 1)
 			selection = 0;
 	}
 	
 	/**
 	 * Adds a menu item to this MenuStrip.
 	 * @param item the String to be added to the menu items
 	 */
 	public void addMenuItem(Interactable item) {
 		menuItems.add(item);
 		size = menuItems.size();
 	}
 	
 	// Returns this object in a String representation.
 	public String toString() {
 		String estr = "";
 		for (Interactable item : menuItems)
 			estr += item + "\n";
 		return estr;
 	}
 }
