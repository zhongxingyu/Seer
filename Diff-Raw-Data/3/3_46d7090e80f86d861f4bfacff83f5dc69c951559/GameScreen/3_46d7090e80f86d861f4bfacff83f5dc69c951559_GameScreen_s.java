 package cc.game.SteampunkZelda.screen.gamescreen;
 
 import cc.game.SteampunkZelda.SteampunkZelda;
 import cc.game.SteampunkZelda.screen.Screen;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 import java.io.*;
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: calv
  * Date: 30/03/13
  * Time: 14:29
  * To change this template use File | Settings | File Templates.
  */
 public abstract class GameScreen extends Screen {
     private Image bgImage;
     protected int levelID;
     protected ArrayList<int[]> collisions;
 
     protected GameScreen(SteampunkZelda game, int levelID, int MAP_WIDTH, int MAP_HEIGHT) {
         super(game, MAP_WIDTH, MAP_HEIGHT);
         this.levelID = levelID;
         try {
             this.bgImage = new Image("res/screens/gamescreens/" + this.levelID + ".png");
         } catch (SlickException e) {
             System.err.println("Couldn't load background image.");
             e.printStackTrace();
         }
         this.collisions = new ArrayList<int[]>();
         loadCollisions(this.levelID);
     }
 
     private void loadCollisions(int levelID) {
         try {
             FileInputStream fis = new FileInputStream("res/collisions/" + levelID + ".csv");
             BufferedReader br = new BufferedReader(new InputStreamReader(fis));
             String line;
             while ((line = br.readLine()) != null) {
                 if (line.isEmpty() || line.startsWith("#")) {
                     continue;
                 }
                 else {
                     int[] pos = new int[4];
                     String[] split = line.split(",");
                     for (int i = 0; i < split.length; i++) {
                         pos[i] = Integer.parseInt(split[i]);
                     }
                     this.collisions.add(pos);
                 }
             }
         } catch (FileNotFoundException e) {
             System.err.println("Couldn't load collision file!");
             e.printStackTrace();
         } catch (IOException e) {
             System.err.println("Couldn't read the collision file!");
             e.printStackTrace();
         }
     }
 
     @Override
     public void update(GameContainer gameContainer, int deltaTime) throws SlickException {
 
     }
 
     @Override
     public void render(GameContainer paramGameContainer) {
         bgImage.draw();
     }
 }
