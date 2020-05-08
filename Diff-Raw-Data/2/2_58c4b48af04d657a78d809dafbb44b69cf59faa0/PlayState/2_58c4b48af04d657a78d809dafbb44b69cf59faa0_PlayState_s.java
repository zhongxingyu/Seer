 import org.newdawn.slick.*;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Shape;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.tiled.TiledMap;
 
 /**
  * A class that implements the state where the game is played
  */
 public class PlayState extends BasicGameState {
     /** Game map */
     private TiledMap map;
     /** Camera that moves the map */
     private Camera camera;
     /** User-controlled object */
     private Player player;
     /** Map location */
     private final String MAP_PATH = "res/map/DemoMap2.tmx";
     /** Time game has been in play state */
     int time = 0;
     /** Current x-position */
     int currentX = 0;
     /** State ID of the playable game */
     private int stateID;
     private Image timerBox;
 
     /**
      * Sets game to playable state
      * @param stateID
      */
     public PlayState(int stateID) {
         super();
         this.stateID = stateID;
     }
 
     /**
      * Sets the player graphic, position, shape, and loads the map
      * @param gc
      * @param sbg
      * @throws SlickException
      */
     @Override
     public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
         Image playerImage = new Image("res/character/bike.png");
         Vector2f playerPos = new Vector2f(50, 300);
         Shape playerShape = new Rectangle(playerPos.x, playerPos.y, playerImage.getWidth(), playerImage.getHeight());
         playerShape.setLocation(playerPos);
 
         //load map
         map = new TiledMap(MAP_PATH);
         timerBox = new Image("res/misc/TimerBackground.png");
         camera = new Camera(gc, this.map);
         player = new Player("GauchoRunner", playerImage, playerPos, playerShape);
     }
 
     /**
      * Draws map and player and displays time in seconds
      * @param gc
      * @param sbg
      * @param g
      * @throws SlickException
      */
     @Override
     public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException {
         camera.drawMap(0, 0);
         player.render(g);
        timerBox.draw(10 ,10);
         g.drawString("Time: " + time / 1000 + "s", 10, 100);
 
 //        camera.render(gc, sbg, g);
 //        if(app.getGraphics() == null)
 //        {
 //            System.out.println("GRAPHICS ARE NULL");
 //        }
 //        else
 //        {
 //            player.render(app.getGraphics());
 //        }
 //        camera.renderFinish(gc, sbg, g);
     }
 
     /**
      * Updates time, player position, and camera location
      * @param gc
      * @param sbg
      * @param i
      * @throws SlickException
      */
     @Override
     public void update(GameContainer gc, StateBasedGame sbg, int i) throws SlickException {
         time += i;
         //camera.centerOn(player.getCollisionShape());
 
         //TODO: CAN THIS BE MORE EFFICIENT? YES!
         Input input = gc.getInput();
         if (input.isKeyDown(Input.KEY_UP)) {
             player.setPosition(new Vector2f(player.getPosition().getX(), player.getPosition().getY() - 5));
         } else if (input.isKeyDown(Input.KEY_DOWN)) {
             player.setPosition(new Vector2f(player.getPosition().getX(), player.getPosition().getY() + 5));
         }
         if (input.isKeyDown(Input.KEY_RIGHT)) {
             currentX = currentX + 5;
             camera.centerOn(currentX, 0);
             camera.translateGraphics();
         }
     }
 
     /**
      * Gets state ID of the play state
      * @return
      */
     @Override
     public int getID() {
         return this.stateID;
     }
 }
