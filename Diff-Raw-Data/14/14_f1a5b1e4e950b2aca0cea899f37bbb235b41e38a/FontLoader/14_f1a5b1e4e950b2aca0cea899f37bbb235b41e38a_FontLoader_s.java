 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package starparty.utilities;
 
 import java.awt.Font;
 import java.io.File;
 import org.newdawn.slick.UnicodeFont;
 import org.newdawn.slick.font.effects.ColorEffect;
 
 /**
  *
  * @author Tyler
  */
 public class FontLoader {
   public static UnicodeFont load(String name, int size, boolean bold) {
     UnicodeFont font = null;
     
     try {
       Font baseFont = Font.createFont(Font.TRUETYPE_FONT, new File("resources/font/" + name));
       baseFont.deriveFont(java.awt.Font.PLAIN, 12);
       
       int style = (bold ? java.awt.Font.BOLD : java.awt.Font.PLAIN);
       font = new UnicodeFont(baseFont.deriveFont(style, size));
       font.addAsciiGlyphs();
       font.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
       font.loadGlyphs();
     } catch (Exception e) {
       System.out.println("Error loading font: " + name);
       e.printStackTrace();
     }
     
     return font;
   }
   
   public static UnicodeFont load(String name, int size) {
     return load(name, size, false);
   }
 }
