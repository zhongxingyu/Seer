 package javagame;
 
 import org.newdawn.slick.*;
 
 public class MenuItemString implements Interactable {
 	
 	public static final int FONT_SIZE = 40;
 
 	private String item;
 	private int x;
 	private int y;
 	
 	public static UnicodeFont font;
 	public static int height;
 	
 	private static boolean displayShadows = true;
 	
 	/**
 	 * Primary constructor.
 	 * @param item the String representing this menu item
 	 * @param x x location in pixels
 	 * @param y y location in pixels
 	 */
 	public MenuItemString(String item, int x, int y) {
		font = Utility.getFont(FONT_SIZE);
 		height = font.getLineHeight();
 		this.item = item;
 		this.x = x;
 		this.y = y;
 	}
 	
 	/** Render this MenuItemString to the screen. */
 	public void render(int x, int y) {
 		if (displayShadows)
 			font.drawString(this.x + x, this.y + y + 3, item, Color.black);
 		font.drawString(this.x + x, this.y + y, item);
 	}
 	
 	@Override
 	public int getHeight() {
 		return height;
 	}
 	
 	public void changeItem(String newItem) {
 		item = newItem;
 	}
 	
 	// Returns this object in a String representation.
 	public String toString() {
 		return item;
 	}
 }
