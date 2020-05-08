 package spaceinvaders.script;
 
 import spaceinvaders.entity.*;
 import org.newdawn.slick.*;
 import org.newdawn.slick.geom.*;
 import org.newdawn.slick.fills.GradientFill;
 
 /**
  * Space Invaders Game
  * 
  * An exciting space invaders game created 
  * by two AP Computer Science students at 
  * Hunter College High School
  * 
  * Copyright (c) 2013 Team Coding Voyage
  *
  * Licensed under the terms of the GNU General Public License v3
  * as published by the Free Software Foundation.
  * 
  * @author Brian Yang
  * @author Edmund Qiu
  * @version 0.5
  */
 
 /**
  * Creates, renders, and updates the actual game window
  */
 public class SpaceInvaders extends BasicGame {
     /** All the scripts to be read */
     ScriptManager scriptCollection;
     /** The actual script reader */
     ScriptReader scriptReader;
     
     /** Manages all the scripting threads */
     ThreadManager threadManager;
         
     /** A test enemy entity that moves */
     MovableEntity testEntity;
     
     /** horizontal resolution */
     public static final int X_RESOLUTION = 1024;
     /** vertical resolution */
     public static final int Y_RESOLUTION = 768;
     
     /** The space background */
     private Image space;
     
     /** The actual player */
     
     /** TEMPORARY TEST VARIABLES */
 
     
     /** Data for all entities */
     private EntityGroup entities;
     
     /**
      * Construct a new game
      */
     public SpaceInvaders() {
        super("We are Team Coding Voyage!");
     }
 
     /**
      * Initialize the game container and start the scripting engine
      * @param gc the new game container
      * @throws SlickException something went wrong with the Slick engine
      */
     @Override
     public void init(GameContainer gc) throws SlickException {
         //This initializes stuff
         gc.setMinimumLogicUpdateInterval(20);
         
         /* Load images */
         space = new Image("src/spaceinvaders/images/bluestar.jpg");
         
         
         /* load the data for all entities */     
         if(loadEntities()) {
             System.out.println("Entity data has successfully been loaded.");
         } else {
             /* The entities failed to load, therefore we will use the default entities */
             System.out.println("WARNING - Entity data has failed to load! Loading blank entity group.");
         }
         
         
     }
 
 
     /**
      * Update the screen
      * @param gc the game container
      * @param delta time interval
      * @throws SlickException something went horribly wrong with Slick
      */
     @Override
     public void update(GameContainer gc, int delta) throws SlickException {
         //Should work UNTIL we start having the ability to create
         //new threads, upon which you should examine this carefully to make
         //sure that there aren't massive off-by-one-errors.
         
        // threadManager.act(delta);
         
         /** user keyboard control */
         //control(gc, delta);
  
     }
 
     /**
      * Render the game window
      * @param gc the game container
      * @param g the graphics engine
      * @throws SlickException something went horribly wrong with Slick
      */
     @Override
     public void render(GameContainer gc, Graphics g) throws SlickException {
         //Any and all graphics/rendering functions which should be called
         //with the drawing of each frame go HERE
 
         // give EntityGroup the Graphics engine so it can used later if needed
         // g is only needed for drawing shapes. Not needed for rendering images.
         // Use EntityGroup.getGraphics()
         EntityGroup.receiveGraphics(g);
         
         // draw the space background
         space.draw(0,0);
         
         // Test spawning an enemy
         Enemy enemy = EntityGroup.getEnemy("Minion");
         //System.out.println(EntityGroup.getEnemyCount());
         //System.out.println(enemy.getImagePath());
        if (!enemy.spawn(300, 200)) {
             System.out.println("Well then, it looks like the enemy failed to spawn.");
         }
         
         g.drawString("Bullet: " + enemy.getX() + ", " + enemy.getY(), 600, 40);
         g.drawString("This game is currently in testing and nothing should work properly.", 300, 50);
     }
 
     /**
      * The main method<br/>
      * Create the game window and start the game!
      * @param args command line arguments
      * @throws SlickException something went horribly wrong with Slick
      */
     public static void main(String[] args) throws SlickException {
         /*
          * Start Game Window Construction
          */
         
         AppGameContainer app = new AppGameContainer(new SpaceInvaders());
 
         app.setDisplayMode(X_RESOLUTION, Y_RESOLUTION, false);
         app.setAlwaysRender(true);
         app.setTargetFrameRate(60);
         //This is important. I found out that with this command, it will limit
         //the frames displayed per second to around 60. We DON'T want frames
         //being drawn 2000 times per second.
 
         app.start();
 
     }
     
     /**
      * Loads all entity data from a separate data file
      * @return a boolean indicating whether or not the entity data have been successfully loaded
      */
     private boolean loadEntities() {
         JsonReader<EntityGroup> reader = new JsonReader<EntityGroup>(EntityGroup.class, "src/spaceinvaders/entity/EntityData.json");
         if(reader.readJson()) {
             entities = reader.getObject();
             return true;
         }
         entities = new EntityGroup();
         return false;
     }
 }
