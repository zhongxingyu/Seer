 package sandbox.core;
 
 import static forplay.core.ForPlay.assetManager;
 import sandbox.core.display.DisplayManager;
 import sandbox.core.entities.Hero;
 import sandbox.core.fsm.GameStateManager;
 import sandbox.core.state.Gaming;
 import sandbox.core.world.GoblinzDungeonWorld;
 import forplay.core.Game;
 import forplay.core.Image;
 
 /**
  * 
  * @author ahingsaka
 * @version 0.2.0
  */
 public class GoblinzDungeon implements Game {
 
     public static final int SCREEN_WIDTH = 800;
     public static final int SCREEN_HEIGHT = 480;
     public static String BACKGROUND = "images/black_background.png";
 
     private DisplayManager displayManager;
     private GameStateManager gameStateManager;
 
     @Override
     public void init() {
 
         GoblinzDungeonWorld world = new GoblinzDungeonWorld();
         Globals.getInstance().setWorld(world);
 
         displayManager = new DisplayManager();
         displayManager.setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
         displayManager.setWorld(world);
 
         Image image = assetManager().getImage(BACKGROUND);
         displayManager.createBackground(image);
 
         Hero hero = new Hero(0, 6);
         Globals.getInstance().setHero(hero);
 
         // Load the states
         gameStateManager = new GameStateManager();
 
         // TODO put a display for each state ?
         gameStateManager.setDisplayManager(displayManager);
         gameStateManager.addFirstState(new Gaming());
 
         // Start
         gameStateManager.init();
     }
 
     @Override
     public void update(float delta) {
         gameStateManager.update(delta);
     }
 
     @Override
     public void paint(float alpha) {
         displayManager.paint(alpha);
     }
 
     @Override
     public int updateRate() {
         return 25;
     }
 
 }
