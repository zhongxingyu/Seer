 package menu;
 
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import app.RPGame;
 import com.golden.gamedev.object.GameFont;
import com.golden.gamedev.object.GameFontManager;
 import com.golden.gamedev.object.Timer;
 
 
 public abstract class Menu {
     protected GameFont font;
     protected BufferedImage menuBackground;
     protected BufferedImage cursor;
     protected int option;
     protected int numOptions;
     protected boolean blink;
     protected Timer blinkTimer = new Timer(400);
     protected RPGame game;
 
 
     public Menu (RPGame game1,
                  String cursorImage,
                  String menuBackgroundImage,
                  String fontImage) {
         game = game1;
         cursor = game.getImage("rsc/menu/Arrow.png");
         menuBackground = game.getImage(menuBackgroundImage, false);
        game.fontManager = new GameFontManager();
         font = game.fontManager.getFont(game.getImage(fontImage));
         option = 0;
     }
 
 
 //    cursor = game.getImage("Arrow.png");
 //    menuBackground = game.getImage("resources/items/itemMenuBackground.gif", false);
 //    font = game.fontManager.getFont(game.getImage("BitmapFont.png"));
 //    star = game.getImage("resources/items/goldStar.gif");
     public abstract void render (Graphics2D g);
 
 
     public abstract void update (long elapsedTime);
 
 }
