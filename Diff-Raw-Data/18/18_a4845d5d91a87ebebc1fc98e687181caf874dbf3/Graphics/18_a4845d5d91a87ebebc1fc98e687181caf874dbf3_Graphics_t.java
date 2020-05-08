 package ua.org.dector.moon_lander;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 
 import static ua.org.dector.moon_lander.AppConfig.*;
 
 /**
  * @author dector (dector9@gmail.com)
  */
 public class Graphics {
     private static SpriteBatch sb = new SpriteBatch();
     private static BitmapFont defaultFont = new BitmapFont();
     private static BitmapFont smallFont =
             ResourceLoader.loadFont(SMALL_FONT_FILE, SMALL_FONT_IMG);
     private static BitmapFont bigFont =
             ResourceLoader.loadFont(BIG_FONT_FILE, BIG_FONT_IMG);
 
     private static GL10 gl10 = Gdx.graphics.getGL10();
 
     public static void init() {
         gl10.glClearColor(0, 0, 0, 1);
     }
 
     public static void clearColor(Color c) {
         gl10.glClearColor(c.r, c.g, c.b, c.a);
     }
 
     public static void begin() {
         sb.begin();
     }
 
     public static void end() {
         sb.end();
     }
 
     public static void draw(TextureRegion tex, float x, float y) {
         sb.draw(tex, x, y);
     }
 
     public static void draw(TextureRegion tex, float x, float y,
                             int width, int height) {
         sb.draw(tex, x, y, width, height);
     }
 
     public static void draw(TextureRegion tex, float x, float y,
                             int width, int height, float rotation) {
         draw(tex, x, y, width / 2, height / 2, width, height, rotation);
     }
 
     public static void draw(TextureRegion tex, float x, float y, int originX, int originY,
                             int width, int height, float rotation) {
         sb.draw(tex, x, y, originX, originY, width, height, 1, 1, rotation - 90);
     }
 
     public static void draw(String string, int x, int y) {
         draw(string, x, y, FontSize.DEFAULT);
     }
 
     public static void drawCentered(String string, int x, int y, FontSize size) {
         BitmapFont font = getFont(size);
         BitmapFont.TextBounds bounds = font.getBounds(string);
 
         x -= bounds.width / 2;
         y += bounds.height / 2;
 
         draw(string, x, y, size);
     }
 
     private static BitmapFont getFont(FontSize size) {
         BitmapFont font;
 
         switch (size) {
             case SMALL:
                 font = smallFont;
                 break;
             case BIG:
                 font = bigFont;
                 break;
             case DEFAULT:
             default:
                 font = defaultFont;
                 break;
         }
 
         return font;
     }
 
     public static void draw(String string, int x, int y, FontSize size) {
         getFont(size).draw(sb, string, x, y);
     }
 
     public static void draw(int x, int y, int stepY, String... strings) {
         for (String s : strings) {
            draw(s, x, y, FontSize.SMALL);
             y -= stepY;
         }
     }
 
     public static void clear() {
         gl10.glClear(GL10.GL_COLOR_BUFFER_BIT);
     }
 
     public static enum FontSize {
         DEFAULT, SMALL, BIG
     }
 }
