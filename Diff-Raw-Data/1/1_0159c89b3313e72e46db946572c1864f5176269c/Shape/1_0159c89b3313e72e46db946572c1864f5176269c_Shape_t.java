 package shapes;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.util.*;
 
 /**
  * Represents a shape that is drawn to the screen and can interact with other
  * shapes.
  * <p>
  * Shapes are the basic building blocks of games built with the Shapes
  * framework. They can be of different colors and sizes, can move around the
  * screen, can collide with one another, and can say things, to name a few.
  * <p>
  * You won't use this class directly; you'll use one of its subclasses:
  * {@link Circle}, {@link Rectangle} or {@link Triangle}.
  *
  * @author Nate Sullivan
  * @version v0
  */
 public abstract class Shape {
   private Color color;
   private boolean fill; 
   private boolean invisible; 
   private boolean solid;
   private String speech;
   private int speechDuration;
   private Color speechColor;
   private boolean destroyed;
   protected Direction direction;
   private double speed;
   protected Point center;
 
   /**
    * Initializes the Shape. When you subclass shape, you'll
    * override this method to do things like set the shape's color, set its
    * starting position, etc.
    */
   abstract public void setup();
   /**
    * Updates the shape once per frame. When you subclass shape, you'll
    * override this method to do things like move the shape, change its
    * size, etc.
    */
   abstract public void update();
 
   /**
    * Draws the shape to the canvas.
    */
   abstract void render(Graphics2D g);
 
   /**
    * Checks if this shape contains another given shape.
    *
    * @param  s  the shape that may be contained in this shape.
    * @return    true if s is entirely inside this shape, false otherwise.
    */
   abstract public boolean contains(Shape s);
 
   /**
    * Checks if this shape contains a given point.
    *
    * @param p   a point to check for inside this shape.
    * @return    true if p is inside or on the border of this shape.
    */
   abstract public boolean contains(Point p);
 
   /**
    * Updates the shape automatically.
    *
    * This method moves the shape when it has a speed, etc.
    */
   void autoUpdate() {
     if (this.isSpeaking()) {
       speechDuration--;
     }
     if (Math.abs(speed) > Geometry.EPSILON) {
       move(getDirection(), speed);
     }
   }
 
   // Overriding constructors should call super() and call setup() at the end
   /**
    * Constructs a new shape with default values.
    * <p>
    * Overriding constructors should call super() to ensure that Game.addShape()
    * is called, and should call setup().
    */
   public Shape() {
     Game.addShape(this);
     // set default values
     setColor(Color.GREEN);
     setFilled(true);
     setSpeechColor(Color.BLACK);
     setDirection(Direction.RIGHT);
     destroyed = false;
   }
 
   /**
    * Changes the location of this shape.
    *
    * @param x the x-coordinate of this shape's new center
    * @param y the y-coordinate of this shape's new center
    */
   public void setCenter(double x, double y) {
     setCenter(new Point(x, y));
   }
 
   /**
    * Checks if this shape is touching a given segment.
    *
    * @param   seg the segment to check.
    * @return      true if this shape is touching <code>seg</code>, false 
    *              otherwise.
    */
   boolean isTouching(Segment seg) {
     return Geometry.touching(this, seg);
   }
 
   /**
    * Checks if this shape is touching another shape.
    *
    * @param   s the shape to check.
    * @return    true if this shape is touching <code>s</code>, false otherwise.
    */
   public boolean isTouching(Shape s) {
     return Geometry.touching(this, s);
   }
 
   /**
    * TODO
    */
   abstract public boolean isOffscreen();
 
   /**
    * Checks if this shape is touching the border of the window.
    *
    * @return  true if any part of the shape is touching the border of the
    *          window, or if the shape is entirely outside the window, and false
    *          otherwise.
    */
   public boolean isTouchingBorder() {
     if (isOffscreen()) {
       return true;
     }
     for (Segment border : Game.getCanvas().getBorders()) { 
       if (isTouching(border)) {
         return true;
       }
     }
     return false;
   }
 
   /**
    * Finds the farthest this shape can go towards a target with an obstacle in
    * the way.
    * <p>
    * If this shape tries to move to <code>target</code>, then
    * <code>obstacle</code> may get in its way. Finds the farthest this shape
    * can move towards <code>target</code>, and returns the location where the
    * shape's center would be if it advanced as far as possible.
    *
    * @param   target    the point towards which this shape is moving.
    * @param   obstacle  another shape which may obstruct this shape's movement.
    * @return            a point representing the farthest this shape's center
    *                    can move towards <code>target</code>.
    */
   abstract Point maxMovement(Point target, Shape obstacle);
 
   /**
    * Moves in the shape's direction.
    * <p>
    * Moves <code>pixels</code> pixels in the direction set using
    * {@link #setDirection(Direction)}. Won't move through solid shapes.
    *
    * @param   pixels  the distance to move.
    * @see     #move(Direction, double)
    * @see     #setDirection(Direction)
    */
   public void move(double pixels) {
     move(getDirection(), pixels);
   }
 
   /**
    * Moves in the given direction.
    * <p>
    * Moves <code>pixels</code> pixels in the given direction.
    * Won't move through solid shapes.
    *
    * @param   pixels    the distance to move.
    * @param   direction the direction in which to move.
    * @see     #move(double)
    */
   public void move(Direction direction, double pixels) {
     if (direction == null || Math.abs(pixels) < Geometry.EPSILON) {
       return;
     }
     Point end = getCenter().translation(new Vector(direction, pixels));
     Point maxMovement = end;
     Set<Shape> solids = Game.getSolids();
     for (Shape solid : solids) {
      if (solid == this) continue;
       Point blockedEnd = this.maxMovement(end, solid);
       if (Geometry.distance(getCenter(), blockedEnd) < Geometry.distance(getCenter(), maxMovement)) {
         maxMovement = blockedEnd;
       }
     }
     setCenter(maxMovement);
   }
 
   // TODO: test
   /**
    * Checks if this shape was clicked since the previous frame.
    *
    * Returns true if:
    * <ul>
    *  <li>the mouse button was clicked inside this shape since the last
    *  frame</li>
    *  <li>the mouse was dragged into this shape while the mouse button was
    *  down, and is currently inside this shape</li>
    * </ul>
    *
    * @return  true if this shape was clicked since the previous frame, false otherwise.
    * @see     Mouse#clickLocation()
    */
   public boolean isClicked() {
     return this.contains(Mouse.clickLocation());
   }
 
   /**
    * Moves right.
    * <p>
    * Moves <code>pixels</code> pixels to the right.
    * Won't move through solid shapes.
    *
    * @param   pixels  the distance to move.
    * @see     #move(Direction, double)
    */
   public void moveRight(double pixels) {
     move(Direction.RIGHT, pixels);
   }
 
   /**
    * Moves left.
    * <p>
    * Moves <code>pixels</code> pixels to the left.
    * Won't move through solid shapes.
    *
    * @param   pixels  the distance to move.
    * @see     #move(Direction, double)
    */
   public void moveLeft(double pixels) {
     move(Direction.LEFT, pixels);
   }
 
   /**
    * Moves up.
    * <p>
    * Moves <code>pixels</code> pixels to the up.
    * Won't move through solid shapes.
    *
    * @param   pixels  the distance to move.
    * @see     #move(Direction, double)
    */
   public void moveUp(double pixels) {
     move(Direction.UP, pixels);
   }
   
   /**
    * Moves down.
    * <p>
    * Moves <code>pixels</code> pixels to the down.
    * Won't move through solid shapes.
    *
    * @param   pixels  the distance to move.
    * @see     #move(Direction, double)
    */
   public void moveDown(double pixels) {
     move(Direction.DOWN, pixels);
   }
 
   /**
    * Display text near this shape.
    * <p>
    * Continues speaking until this method is called again with new text.
    *
    * @param   speech  what the shape says.
    * @see     #say(String, int)
    * @see     #setSpeechColor(Color)
    * @see     #getSpeech()
    * @see     #isSpeaking()
    */
   public void say(String speech) {
     this.speech = speech;
     this.speechDuration = -1;
   }
   
   /**
    * Displays text near this shape for a limited time.
    * 
    * @param   speech  what the shape says.
    * @param   frames  how long the shape speaks.
    * @see     #say(String)
    * @see     #setSpeechColor(Color)
    * @see     #getSpeech()
    * @see     #isSpeaking()
    */
   public void say(String speech, int frames) {
     this.speech = speech;
     this.speechDuration = frames;
   }
 
   /**
    * Returns what this shape is saying.
    *
    * @return  what this shape is saying, or <code>null</code> if the shape
    *          isn't saying anything.
    * @see     #say(String, int)
    * @see     #say(String)
    */
   public String getSpeech() {
     if (isSpeaking()) {
       return speech;
     } else {
       return null;
     }
   }
 
   /**
    * Sets the color of this shape's speech.
    *
    * @param   speechColor the color of this shape's speech.
    * @see     #getSpeechColor()
    */
   public void setSpeechColor(Color speechColor) {
     this.speechColor = speechColor;
   }
 
   /**
    * Returns the color of this shape's speech.
    *
    * @return  the color of this shape's speech.
    * @see     #setSpeechColor(Color)
    */
   public Color getSpeechColor() {
     return speechColor;
   }
 
   /**
    * Returns true if the shape is currently speaking.
    *
    * @return  true if the shape is speaking, false otherwise.
    * @see     #say(String, int)
    * @see     #say(String)
    * @see     #getSpeech()
    */
   public boolean isSpeaking() {
     return speechDuration != 0;
   }
 
   /**
    * Returns the direction of a given point relative to this shape.
    * <p>
    * Can be used to {@link #move} this shape towards a point:
    * <code>this.move(this.towards(targetPoint), 10.0);</code>
    *
    * @param   target  the point to aim towards.
    * @return          the direction of <code>target</code> relative to this
    *                  shape, or null if <code>target</code> is null.
    * @see             #towards(Shape)
    * @see             #move(Direction, double)
    * @see             #setDirection(Direction)
    */
   public Direction towards(Point target) {
     if (target == null) return null;
     Vector v = new Vector(this.getCenter(), target);
     return v.getDirection();
   }
 
   /**
    * Returns the direction of a shape relative to this shape.
    * <p>
    * Can be used to {@link #move} this shape towards another shape:
    * <code>this.move(this.towards(targetShape), 10.0);</code>
    *
    * @param   target  the shape to aim towards.
    * @return          the direction of <code>target</code>'s relative to this
    *                  shape's center.
    * @see             #towards(Point)
    * @see             #move(Direction, double)
    * @see             #setDirection(Direction)
    */
   public Direction towards(Shape target) {
     return towards(target.getCenter());
   }
 
   /**
    * Returns the distance between this shape and another shape.
    *
    * @param target  the shape whose distance away to find.
    * @return  distance in pixels from this shape to <code>target</code>.
    */
   public double distanceTo(Shape target) {
     return Geometry.distance(this, target);
   }
 
   /**
    * Destroys this shape so it will no longer be rendered. Call this method
    * when you have finished using the shape.
    *
    * A destroyed shape will not appear on the screen and will not interact with
    * other shapes. A destroyed shape cannot be undestroyed.
    */
   public void destroy() {
     // TODO: who remove the same from Game?
     destroyed = true;
   }
 
   /**
    * Returns true if this shape has been destroyed.
    *
    * @return  true if {@link #destroy()} has been called on this shape,
    *          false otherwise.
    */
   public boolean isDestroyed() {
     return destroyed;
   }
 
   /**
    * Rotate this shape's direction. Passing a negative value to 
    * <code>degrees</code> causes a clockwise rotation.
    *
    * @param degrees the number of degrees by which to rotate.
    */
   public void rotate(double degrees) {
     setDirection(getDirection().rotation(degrees));
   }
 
   // Getters & setters
 
   /**
    * Set whether this shape is filled or outlined.
    * <p>
    * A filled shape has a colored interior, and a non-filled shape has a
    * colored outline but a transparent interior. Fill affects how the shape
    * appears but not its functionality.
    *
    * @param fill  true for fill, false for outline.
    */
   public void setFilled(boolean fill) {
     this.fill = fill;
   }
 
   /**
    * Returns whether this shape is filled or outlined.
    * <p>
    * A filled shape has a colored interior, and a non-filled shape has a
    * colored outline but a transparent interior. Fill affects how the shape
    * appears but not its functionality.
    *
    * @return  true if filled, false if outline.
    */
   public boolean isFilled() {
     return fill;
   }
 
   /**
    * Set this shape's visibility.
    * <p>
    * Invisible shapes won't appear on the canvas, but still exist and will
    * interact with other shapes.
    *
    * @param invisible true for invisible, false for visible.
    */
   public void setInvisible(boolean invisible) {
     this.invisible = invisible;
   }
 
   /**
    * Returns whether this shape is invisible.
    * <p>
    * Invisible shapes won't appear on the canvas, but still exist and will
    * interact with other shapes.
    *
    * @return  true if invisible, false if visible.
    */
   public boolean isInvisible() {
     return invisible;
   }
   
   /**
    * Set this shape's color.
    *
    * @param color the color the shape will be drawn in.
    */
   public void setColor(Color color) {
     this.color = color;
   }
 
   /**
    * Get this shape's color.
    *
    * @return  the color the shape is drawn in.
    */
   public Color getColor() {
     return color;
   }
 
   /**
    * Set whether other shapes can overlap with this shape.
    * <p>
    * An example of a solid shape is a wall that other shapes can't pass
    * through.
    * <p>
    * No shape can overlap with a solid shape. A non-solid shape can overlap
    * with other non-solid shapes.
    *
    * @param solid true for a shape that can't be overlapped, false for a shape
    *              that can be.
    */
   public void setSolid(boolean solid) {
     if (this.solid == solid) {
       return;
     }
 
     if (solid) {
       Game.addSolid(this);
     } else {
       Game.removeSolid(this);
     }
 
     this.solid = solid;
   }
 
   /**
    * Get whether other shapes can overlap with this shape.
    * <p>
    * An example of a solid shape is a wall that other shapes can't pass
    * through.
    * <p>
    * No shape can overlap with a solid shape. A non-solid shape can overlap
    * with other non-solid shapes.
    *
    * @return true for a shape that can't be overlapped, false for a shape
    *         that can be.
    */
   public boolean isSolid() {
     return solid;
   }
 
   /**
    * Set the direction this shape is facing.
    *
    * A shape's direction determines:
    * <ul>
    *  <li>The orientation in which the shape is drawn.</li>
    *  <li>
    *    The direction the shape will move in calls to {@link #move(double)}.
    *    (To move in other directions, see
    *    {@link #move(Direction, double)}).
    *   </li>
    *  <li>
    *    The direction the shape will move if it has a speed (see
    *    {@link #setSpeed(double)}).
    *   </li>
    * </ul>
    *
    * @param  direction the direction the shape will face.
    */
   public void setDirection(Direction direction) {
     this.direction = direction;
   }
 
   /**
    * Get the direction this shape is facing.
    *
    * A shape's direction determines:
    * <ul>
    *  <li>The orientation in which the shape is drawn.</li>
    *  <li>
    *    The direction the shape will move in calls to {@link #move(double)}.
    *    (To move in other directions, see
    *    {@link #move(Direction, double)}).
    *   </li>
    *  <li>
    *    The direction the shape will move if it has a speed (see
    *    {@link #setSpeed(double)}).
    *   </li>
    * </ul>
    *
    * @return  the direction the shape is facing.
    */
   public Direction getDirection() {
     return direction;
   }
 
   /**
    * Set the shape's speed in pixels per frame.
    * <p>
    * This shape will automatically advance every frame in the direction set
    * using {@link #setDirection(Direction)}.
    *
    * @param speed the number of pixels to move each frame.
    */
   public void setSpeed(double speed) {
     this.speed = speed;
   }
 
   /**
    * Get the shape's speed in pixels per frame.
    * <p>
    * This shape automatically advances every frame in the direction set
    * using {@link #setDirection(Direction)}.
    *
    * @return the number of pixels this shape moves each frame.
    */
   public double getSpeed() {
     return speed;
   }
 
   /**
    * Get the location of the shape's center.
    *
    * @return  a point representing the shape's center.
    */
   public Point getCenter() {
     return center;
   }
   
   /**
    * Set the location of the shape's center.
    * <p>
    * Note that this is the only way to move a shape that will allow it to enter
    * a solid shape. Moving this shape into a solid shape has undefined behavior.
    * (If you haven't called {@link #setSolid(boolean)}, you don't have
    * to worry about this.)
    *
    * @param center  a point representing the location of the shape's center.
    */
   public void setCenter(Point center) {
     this.center = center;
   }
 }
