 package javagame;
 
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.UnicodeFont;
 import org.newdawn.slick.font.effects.ColorEffect;
 
 public class Utility {
	@SuppressWarnings("unchecked")
 	public static UnicodeFont getFont(int size) {
 		UnicodeFont font = null;
 		try {
 			font = new UnicodeFont("data/gamefont.ttf", size, false, false);
 			font.getEffects().add(new ColorEffect(java.awt.Color.white));
 			font.addAsciiGlyphs();
 			font.loadGlyphs();
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 		return font;
 	}
 }
