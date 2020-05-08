 package edu.mines.csci598.recycler.frontend.graphics;
 
 import edu.mines.csci598.recycler.frontend.GameLogic;
 import edu.mines.csci598.recycler.frontend.Hand;
 import edu.mines.csci598.recycler.frontend.utils.GameConstants;
 import edu.mines.csci598.recycler.frontend.utils.Log;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.util.ConcurrentModificationException;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 /**
  * The GameScreen class is responsible for drawing the sprites with their updated time.
  * <p/>
  * <p/>
  * Created with IntelliJ IDEA.
  * User: jzeimen
  * Date: 10/20/12
  * Time: 9:13 PM
  */
 public class GameScreen {
     private static GameScreen INSTANCE;
     private Sprite s;
     private Sprite backgroundLeft;
     private Sprite backgroundRight;
     private Sprite player1PrimaryHand;
     private LinkedList<Sprite> sprites = new LinkedList<Sprite>();
     private double scaledWidth;
     private double scaledHeight;
 
     private GameScreen() {
         backgroundLeft = new Sprite("src/main/resources/SpriteImages/background_b.png", 0, 0);
        backgroundRight = new Sprite("src/main/resources/SpriteImages/background_b_R.png", GraphicsConstants.GAME_SCREEN_WIDTH, 0);
         s = new Sprite("src/main/resources/SpriteImages/glass.png", 0, GraphicsConstants.GAME_SCREEN_HEIGHT - 200);
         scaledWidth = GraphicsConstants.GAME_SCREEN_WIDTH * GraphicsConstants.SCALE_FACTOR;
         scaledHeight = GraphicsConstants.GAME_SCREEN_HEIGHT * GraphicsConstants.SCALE_FACTOR;
     }
 
     public static final GameScreen getInstance() {
         if (INSTANCE == null) {
             INSTANCE = new GameScreen();
         }
         return INSTANCE;
     }
 
 
     public synchronized void paint(Graphics2D g2d, Component canvas) {
         g2d.drawImage(backgroundLeft.getImage(), backgroundLeft.getX(), backgroundLeft.getY(), canvas);
         g2d.drawImage(backgroundRight.getImage(), backgroundRight.getX(), backgroundRight.getY(), canvas);
 
         g2d.setFont(new Font("TimesRoman", Font.BOLD, 20));
         g2d.setColor(Color.green);
         g2d.drawString("SCORE: ", 0, 20);
         g2d.drawString(GameLogic.getInstance().getScoreString(), 100, 20);
 
         g2d.setFont(new Font("TimesRoman", Font.BOLD, 20));
         g2d.setColor(Color.red);
         g2d.drawString("STRIKES: ", 130, 20);
         g2d.drawString(GameLogic.getInstance().getStrikesString(), 220, 20);
 
         g2d.setFont(new Font("TimesRoman", Font.BOLD, 20));
         g2d.setColor(Color.black);
         g2d.drawString("Paper ", (int) (scaledWidth * GraphicsConstants.LEFT_X_SCALE), (int) (scaledHeight * GraphicsConstants.LEFT_TOP_Y_SCALE));
 
         g2d.setFont(new Font("TimesRoman", Font.BOLD, 20));
         g2d.setColor(Color.black);
         g2d.drawString("Glass", (int) (scaledWidth * GraphicsConstants.LEFT_X_SCALE), (int) (scaledHeight * GraphicsConstants.LEFT_BOTTOM_Y_SCALE));
 
         g2d.setFont(new Font("TimesRoman", Font.BOLD, 20));
         g2d.setColor(Color.black);
         g2d.drawString("Plastic", (int) (scaledWidth * GraphicsConstants.RIGHT_X_SCALE), (int) (scaledHeight * GraphicsConstants.RIGHT_TOP_Y_SCALE));
 
         g2d.setFont(new Font("TimesRoman", Font.BOLD, 20));
         g2d.setColor(Color.black);
         g2d.drawString("Plastic", (int) (scaledWidth * GraphicsConstants.RIGHT_X_SCALE), (int) (scaledHeight * GraphicsConstants.RIGHT_BOTTOM_Y_SCALE));
 
         for (Sprite sprite : sprites) {
             try {
                 g2d.drawImage(sprite.getImage(), sprite.getScaledX(), sprite.getScaledY(), canvas);
             } catch (ConcurrentModificationException e) {
                 Log.logError("ERROR: ConcurrentModificationException trying to draw sprite " + s);
             }
         }
 
         g2d.drawImage(player1PrimaryHand.getImage(), player1PrimaryHand.getX(), player1PrimaryHand.getY(), canvas);
     }
 
     public synchronized void addSprite(Sprite s) {
         try {
             s.setState(Sprite.TouchState.UNTOUCHABLE);
             sprites.addLast(s);
         } catch (ConcurrentModificationException e) {
             Log.logError("ERROR: ConcurrentModificationException trying to add sprite " + s);
         }
     }
 
     public synchronized boolean removeSprite(Sprite s) {
         return sprites.remove(s);
     }
 
     public void addHandSprite(Sprite s) {
         player1PrimaryHand = s;
     }
 
 }
