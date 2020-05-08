 package spaceinvaders.entity;
 
 import spaceinvaders.script.*;
 import org.newdawn.slick.*;
 import org.newdawn.slick.geom.*;
 
 /**
  * An Entity is any object that appears on the map 
  * that is not part of the menu, controls, or map itself
  * 
  * Will be declared as abstract later
  * 
  * @author Brian Yang
  * @author Edmund Qiu
  */
 
 public class Entity extends ScriptableClass {
     
     /** Name of Entity */
     private String name;
     /** ID (index) of Entity */
     private Integer id;
     /** Description of Entity */
     private String description;
     /** Image name of Entity */
     private String image;
     /** Actual image of Entity */
     private Image sprite;
        
     /** x coordinates */
     private float x;
     /** y coordinates */
     private float y = 300;
     /** Default scaling factor */
     private float scale = 0.5f;
     
     /** Image base */
     private static final String IMAGE_PATH = "src/spaceinvaders/images/";
     
     /** The Graphics engine */
     private Graphics g;
     
     /** origin */
     public static final double ORIGIN = 0.0;
     
     /** map boundary factor */
     public static final float EDGE_FACTOR = 75.0f;
     /** velocity conversion from miles to pixels */
     public static final double VELOCITY_FACTOR = 0.01;
     
     /** rotation size */
     public static final float ROTATION_SIZE = 0.2f;
     /** step size */
     public static final float STEP_SIZE = 0.4f;
     /** back up size */
     public static final float BACK_SIZE = 0.075f;
     
     private Rectangle rect;
     
     /**
      * Calls ScriptableClass<br/>
      * Instance fields should be set by a data file like JSON
      */
     public Entity() {
         super();
         name = "You";
         id = 0;
         description = "I am you.";
        g = EntityGroup.getGraphics();
     }
 
     /**
      * Constructs a new Entity and calls ScriptableClass
      * @param name name of Entity
      * @param id id (index) of Entity
      * @param description description of Entity
      */
     public Entity(String name, int id, String description) {
         super();
         this.name = name;
         this.id = id;
         this.description = description;
     }
         
     /**
      * Place an entity at a certain location on the map
      * @param x x coordinate
      * @param y y coordinate
      */
     public void place(float x, float y) {
         this.x = x;
         this.y = y;
     }
     
     /**
      * Change coordinates
      * @param x x-distance to move
      * @param y y-distance to move
      */
     public void move(double x, double y) {
         this.x += x;
         this.y += y;
     }
     
     /**
      * Accessors for Name
      * @return name of entity
      */
     public String getName() {
         return name;
     }
     
     /**
      * Accessors for ID
      * @return id (index) of entity
      */
     public int getId() {
         return id;
     }
     
     /**
      * Accessors for Description
      * @return description of entity
      */
     public String getDescription() {
         return description;
     }
     
     /**
      * Get X
      * @return the entity's x-coordinate
      */    
     public float getX() {
         return x;
     }
 
     /**
      * Get Y
      * @return the entity's y-coordinate
      */    
     public float getY() {
         return y;
     }
     
     /**
      * Get the Image file name
      * @return the entity's image file name
      */
     public String getImagePath() {
         return IMAGE_PATH + image;
     }
     
     /** 
      * Render graphics
      * @param g the graphics engine
      * @param x x coordinate to spawn at
      * @param y y coordinate to spawn at
      * @return a boolean indicating whether or not the graphics have successfully been drawn
      */
    private boolean renderGraphics(Graphics g, float x, float y) {
         try {
 
             // the sprite depends on the image path loaded from JSON so we can't load this in the constructor
             if (sprite == null) // If we haven't loaded the sprite image, then do so
                 sprite = new Image(IMAGE_PATH + image);
 
             sprite.draw(x, y, scale); // draw the entity at the specified location
             g.drawString("Muahaha. I am enemy. I kill you. Right. Now.", x, y);
             return true;
             
         } catch (SlickException e) {
             
             System.out.println("Captain, we failed to draw the image. We're going down!");
             return false;
             
         }
     }
     
     /**
      * Spawn entity
      * @param x x coordinate to spawn at
      * @param y y coordinate to spawn at
      * @return a boolean indicating whether or not the entity has successfully spawned
      */
    public boolean spawn(Graphics g, float x, float y) {
        return renderGraphics(g, x, y);
         // add this entity to the global active entity list
     }
 }
