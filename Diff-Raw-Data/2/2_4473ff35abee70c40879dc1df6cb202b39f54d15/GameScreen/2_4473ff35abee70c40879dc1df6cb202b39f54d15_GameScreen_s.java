 package cc.game.TestGame.screen.gamescreen;
 
 import cc.game.TestGame.Camera;
 import cc.game.TestGame.World;
 import cc.game.TestGame.screen.Screen;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
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
 
     protected GameScreen(World game, int levelID) {
         super(game);
         this.levelID = levelID;
         try {
            this.bgImage = new Image("/res/screens/gamescreens" + this.levelID + ".png");
         } catch (SlickException e) {
             System.err.println("Couldn't load background image.");
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
