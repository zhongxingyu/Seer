 package fr.larez.danmaku.utils;
 
 import java.awt.Font;
 
 import org.lwjgl.opengl.GL11;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.TrueTypeFont;
 import org.newdawn.slick.util.ResourceLoader;
 
 public class DrawingUtils {
 
     private static TrueTypeFont ttfFont;
 
    static {
         try {
             Font awtFont = Font.createFont(Font.TRUETYPE_FONT, ResourceLoader.getResourceAsStream("res/font.ttf"));
             awtFont = awtFont.deriveFont(20.f);
             ttfFont = new TrueTypeFont(awtFont, true);
         } catch(Exception e) {
             System.err.println("Exception happened loading font");
             e.printStackTrace();
             System.exit(1);
         }
     }
 
     private DrawingUtils()
     {
         assert(false);
     }
 
     public static void drawRect(float x1, float y1, float x2, float y2)
     {
         GL11.glBegin(GL11.GL_QUADS);
         GL11.glTexCoord2f(0.f, 0.f); GL11.glVertex2f(x1, y1);
         GL11.glTexCoord2f(1.f, 0.f); GL11.glVertex2f(x2, y1);
         GL11.glTexCoord2f(1.f, 1.f); GL11.glVertex2f(x2, y2);
         GL11.glTexCoord2f(0.f, 1.f); GL11.glVertex2f(x1, y2);
         GL11.glEnd();
     }
 
     public static void drawText(float x, float y, String text)
     {
         ttfFont.drawString(x, y, text, Color.white);
     }
 
     public static void drawText(float x, float y, String text, Color color)
     {
         ttfFont.drawString(x, y, text, color);
     }
 
     public static void translate(float x, float y)
     {
         GL11.glTranslated(x, y, 0.0f);
     }
 
     public static void reset()
     {
         GL11.glLoadIdentity();
     }
 
 }
