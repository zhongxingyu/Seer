 package javagame;
 
 import org.newdawn.slick.*;
import org.newdawn.slick.font.effects.*;
 
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
		font = null;
		try {
			font = new UnicodeFont("data/gamefont.ttf", FONT_SIZE, false, false);
			font.getEffects().add(new ColorEffect(java.awt.Color.white));
			font.addAsciiGlyphs();
			font.loadGlyphs();
		} catch (SlickException e) {
			e.printStackTrace();
		}
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
