 package elements;
 
 import basic.Oriented;
 import values.BrickColor;
 import values.GameType;
 import values.Orientation;
 
 /**
  * Class implements brick functionality.
  * 
  * @author X-Stranger
  */
 @SuppressWarnings("serial")
 public class Brick extends Oriented {
 
     /** Black brick. */
     public static final Brick BLACK = new Brick(); 
     
     /** Brick colors. */
     private BrickColor color;
 
     /** Brick parent collection. */
     private Oriented parentCollection = null;
     
     /**
      * Parameterized constructor.
      * 
      * @param color - brick color value
      */
     public Brick(BrickColor color) {
         super(color.getColor());
         this.color = color;
         this.setOrientation(Orientation.NONE);
     }
     
     /**
      * Parameterized constructor.
      *
      * @param color - brick color value
      * @param orientation - brick orientation
      */
     public Brick(BrickColor color, Orientation orientation) {
         super(color.getColor());
         this.color = color;
         this.setOrientation(orientation);
     }
 
     /**
      * Default constructor.
      */
     public Brick() {
         this(BrickColor.BLACK);
     }
     
     /**
      * Parameterized constructor.
      * 
      * @param level - max color value
      */
     public Brick(int level) {
        this(BrickColor.generate(level, GameType.ARCADE));
     }
     
     /**
      * Parameterized constructor.
      *
      * @param level - max color value
      * @param type - game type
      */
     public Brick(int level, GameType type) {
         this(BrickColor.generate(level, type));
     }
 
     /**
      * Sets new brick orientation.
      * 
      * @param orientation - new Orientation value
      */
     public void setOrientation(Orientation orientation) {
         super.setOrientation(orientation);
         this.setIcon(color.getColor(orientation));
     }
 
     /**
      * Getter for parent collection property.
      * 
      * @return Oriented object
      */
     public Oriented getParentCollection() {
         return this.parentCollection;
     }
 
     /**
      * Getter for brick color property.
      * 
      * @return BrickColor object
      */
     public final BrickColor getColor() {
         return this.color;
     }
 
     /**
      * Method invokes brick image repaint.
      */
     public void update() {
         this.setIcon(color.getColor(this.getOrientation()));
         this.repaint();
     }
 
     /**
      * Getter for brick color property.
      *
      * @param color - new BrickColor object
      */
     public void setColor(BrickColor color) {
         this.color = color;
         this.setIcon(color.getColor(this.getOrientation()));
     }
 
     /**
      * Getter for parent collection property.
      * 
      * @param parentCollection - Oriented object
      */
     public void setParentCollection(Oriented parentCollection) {
         this.parentCollection = parentCollection;
     }
     
     /**
      * Returns true if brick has the same color with passed brick.
      * 
      * @param brick - brick to compare color to
      * @return boolean value
      */
     public boolean hasSameColor(Brick brick) {
         return (this.color.compareTo(brick.color));
     }
 
     /**
      * Returns true if brick has the same color with passed brick.
      * 
      * @param color - brick color to compare to
      * @return boolean value
      */
     public boolean hasSameColor(BrickColor color) {
         return (this.color.compareTo(color));
     }
 }
