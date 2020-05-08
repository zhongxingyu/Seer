 package be.kuleuven.cs.ogp.project;
 
 import be.kuleuven.cs.ogp.project.borders.Door;
 import be.kuleuven.cs.ogp.project.borders.NoBorder;
 import be.kuleuven.cs.ogp.project.borders.Wall;
 import be.kuleuven.cs.ogp.project.squares.Teleport;
 import be.kuleuven.cs.ogp.project.tools.Point3D;
 import be.kuleuven.cs.ogp.project.tools.Tools;
 import be.kuleuven.cs.som.annotate.Basic;
 import be.kuleuven.cs.som.annotate.Model;
 import be.kuleuven.cs.som.annotate.Raw;
 
 import javax.xml.transform.Result;
 import java.util.*;
 
 /**
  * This class represents a square or tile in the game.
  *
  * @invar   The temperature assigned to the square can not be smaller than -200°C or larger than 5000°C.
  *          | (this.getTemp() >= -200) && (this.getTemp() <= 5000)
  * @invar   The humidity value assigned to the square can never be smaller than 0 or larger than 100.
  *          | (this.getHumidity() >= 0) && (this.getHumidity() <= 100)
  * @invar   The square can never contain more than 3 door borders.
  *          | count = 0
  *          | for (Direction d : Direction.values())
  *          |    if (square.getBorder(dir) instanceof Door)
  *          |        count++
  *          | count <= 3
  *
  * @author  Frederic Hannes (http://www.freddy1990.net/)
  */
 
 public class Square {
 
     /**
      * Set to false to disable the editing of borders after a border is part of 2 squares.
      */
     private static final boolean CAN_EDIT_BORDERS = true;
 
     /**
      * The minimum temperature below which cold damage can occur.
      */
     @Model
     private static final int DMG_COLD_MIN = -5;
 
     /**
      * The number of degrees below the minimum for cold damage required to receive a damage point.
      */
     @Model
     private static final int DMG_COLD_INTERVAL = 10;
 
     /**
      * The maximum temperature above which heat damage can occur.
      */
     private static int DMG_HEAT_MAX = 35;
 
     /**
      * The number of degrees above the maximum for heat damage required to
      * receive a damage point.
      */
     private static int DMG_HEAT_INTERVAL = 15;
 
     /**
      * The maximum humidity above which rust damage can occur.
      */
     @Model
     private static final int DMG_RUST_MAX = 30;
 
     /**
      * The percentage above the maximum for rust damage required to receive a damage point.
      */
     @Model
     private static final int DMG_RUST_INTERVAL = 7;
 
     /**
      * The weight used to calculate the new temperature of 2 tiles when they are being merged.
      */
     @Model
     private static final double MERGE_WEIGHT = 0.2; // 0.1 <= MERGE_WEIGHT <= 0.4
 
     /**
      * The dungeon to which this square is linked.
      */
     private Dungeon dungeon = null;
 
     /**
      * The position of the square in the dungeon it's assigned to.
      */
     private Point3D pos = null;
 
     /**
      * The avatar that's positioned on the square.
      */
     private Avatar avatar = null;
 
     /**
      * Stores the temperature of the square in degrees Celsius.
      */
     private int temp;
 
     /**
      * Stores the humidity of the square as a percentage with an accuracy of 2
      * decimals.
      */
     private double humidity;
 
     /**
      * Equals true if the floor of the square is slippery.
      */
     private boolean slipperyFloor;
 
     /**
      * Imposes the class invariant restrictions of the subclasses.
      */
     private boolean restricted = false;
 
     /**
      * Contains all of the borders of the square.
      */
     private Map<Direction, Border> borders = new HashMap<>();
 
     /**
      * Creates a new instance of a square with a given temperature and humidity.
      *
      * @param   temp
      *          The given temperature.
      * @param   humidity
      *          The given humidity.
      * @effect  Sets the floor to being not slippery by default.
      *          | setSlipperyFloor(false)
      * @effect  Sets the given temperature as the object's temperature.
      *          | setTemp(temp)
      * @effect  Sets the given humidity as the object's humidity.
      *          | setHumidity(humidity)
      * @effect  Sets all borders to NoBorder by default except those specified by dirs.
      *          | if ((dirs == null) || (!dirs.contains(dir)))
      *          |   setBorder(new NoBorder(), dir)
      * @effect  Sets all borders specified by dirs to a random wall or door.
      *          | if ((dirs != null) && (dirs.contains(dir)))
      *          |   if (Tools.randBool())
      *          |       setBorder(new Wall(Tools.randBool()), dir)
      *          |   else setBorder(new Door(Tools.randBool()), dir)
      */
     public Square(int temp, double humidity, List<Direction> dirs) {
         setTemp(temp);
         setHumidity(humidity);
         setSlipperyFloor(false);
         Door door = new Door(false);
         Door wall = new Door(false);
         for (Direction dir : Direction.values())
             if ((dirs != null) && (dirs.contains(dir))) {
                 if (!door.canLink(this, dir) || Tools.randBool())
                     setBorder(wall, dir);
                 else
                     setBorder(door, dir);
             } else
                 setBorder(new NoBorder(), dir);
     }
 
     /**
      * Creates a new instance of a square with a given temperature and humidity.
      *
      * @param   temp
      *          The given temperature.
      * @param   humidity
      *          The given humidity.
      * @effect  Creates the instance with the default settings except for temperature and humidity.
      *          | this(temp, humidity, null);
      */
     public Square(int temp, double humidity) {
         this(temp, humidity, null);
     }
 
     /**
      * Creates a new instance of a square.
      *
      * @effect  Creates the instance with the default settings.
      *          | this(0, 0, null);
      */
     public Square() {
         this(0, 0, null);
     }
 
     /**
      * Returns the dungeon to which this square is linked. Null is returned if the square has not been assigned to a
      * dungeon yet.
      */
     @Basic
     public Dungeon getDungeon() {
         return dungeon;
     }
 
     /**
      * Sets the dungeon to which the square will be linked.
      *
      * @param   dungeon
      *          The given dungeon.
      * @throws  IllegalArgumentException
      *          Throws an illegal argument exception when the given dungeon is invalid.
      *          | dungeon == null
      * @throws  IllegalArgumentException
      *          Throws an illegal argument exception when the square is already linked to a dungeon.
      *          | this.dungeon != null
      * @post    The parent dungeon is set to the given dungeon.
      *          | new.getDungeonAt() == dungeon
      */
     @Model
     void setDungeon(Dungeon dungeon) throws IllegalArgumentException {
         if (dungeon == null)
             throw new IllegalArgumentException("Invalid dungeon!");
         if (this.dungeon != null)
             throw new IllegalArgumentException("The square is already linked to a dungeon!");
         this.dungeon = dungeon;
     }
 
     /**
      * Returns the relative position of the square in it's parent dungeon if it is assigned to one. If the square has
      * not yet been assigned to a dungeon, null is returned. The returned object is a clone of the internal object to
      * prevent the internal data from being modified.
      */
     public Point3D getPos() {
         if (pos == null)
             return null;
         else
             return (Point3D) pos.clone();
     }
 
     /**
      * Returns the absolute position of the square on the playing field if it is assigned to one. If the square has not
      * yet been assigned to a dungeon, null is returned. The returned object is a clone of the internal object to
      * prevent the internal data from being modified.
      *
      * @result  If the square has a parent dungeon, the absolute position is returned, recursively factoring in that
      *          parent.
      *          | if ((this.getDungeonAt() != null) && (this.getPos() != null))
      *          |   result == this.getDungeonAt().getPos().add(this.getPos())
      * @result  If the square does not have a parent dungeon, null is returned.
      *          | if ((this.getDungeonAt() == null) || (this.getPos() == null))
      *          |   result == null;
      */
     public Point3D getAbsolutePos() {
         if ((this.getDungeon() != null) && (this.getPos() != null))
             return this.getDungeon().getPos().add(this.getPos());
         else return null;
     }
 
     /**
      * Sets the position of the square in it's dungeon. The saved position is cloned to prevent the internal data from
      * being modified.
      *
      * @param   pos
      *          The given position.
      * @post    The new position equals the given position.
      *          | new.getPos() == pos
      */
     @Model
     void setPos(Point3D pos) {
         this.pos = (Point3D) pos.clone();
     }
 
     /**
      * Returns the temperature assigned to the object.
      */
     @Basic @Raw
     public int getTemp() {
         return temp;
     }
 
     /**
      * Assigns a new temperature to the object given by temperature.
      *
      * @param   temp
      *          The given temperature.
      * @post    The new temperature of the object equals the given temperature.
      *          | new.getTemperature() == temp
      * @throws  IllegalArgumentException
      *          Throws an illegal argument exception when the given temperature is not valid.
      *          | !isValidTemp(temp)
      * @throws  IllegalArgumentException
      *          Throws an illegal argument exception when the given temperature is not valid.
      *          | !isValidTemp(temp)
      */
     @Basic
     public void setTemp(int temp) throws IllegalArgumentException {
         if (!canChangeTemp())
             throw new IllegalArgumentException("Can't change temperature!");
         if (isValidTemp(temp))
             this.temp = temp;
         else
             throw new IllegalArgumentException("Invalid temperature given!");
     }
 
     /**
      * Returns true if you can change the temperature of the square. This method can be overridden in subclasses.
      */
     public boolean canChangeTemp() {
         return true;
     }
 
     /**
      * Checks whether the temperature is a valid one to be stored in an object of Square. The temperature is valid if
      * it's not smaller than -200°C and not larger than 5000°C.
      *
      * @param    temp
      *           The given temperature.
      * @return   True if the temperature is valid.
      *           | result == (temp >= -200) && (temp <= 5000)
      */
     public static boolean isValidTemp(int temp) {
         return (temp >= -200) && (temp <= 5000);
     }
 
     /**
      * Returns the cold damage received on this square.
      *
      * @return  Returns 0 if the temperature is above the cold damage temperature boundary.
      *          | if (getTemp() > DMG_COLD_MIN)
      *          |   result == 0
      * @return  If the temperature is below the cold damage temperature boundary, the number of damage points received
      *          is calculated and returned.
      *          | if (getTemp() <= DMG_COLD_MIN)
      *          |   result == Math.floor((getTemp() - DMG_COLD_MIN) / DMG_COLD_INTERVAL);
      */
     @Raw
     public int getColdDmg() {
         if (getTemp() <= DMG_COLD_MIN)
             return (int) Math.floor((0 - (getTemp() - DMG_COLD_MIN)) / DMG_COLD_INTERVAL);
         else
             return 0;
     }
 
     /**
      * Returns the maximum temperature allowed before heat damage can occur.
      */
     @Basic
     public static int getDmgHeatMax() {
         return DMG_HEAT_MAX;
     }
 
     /**
      * Sets the new maximum temperature above which heat damage can occur for all squares.
      *
      * @param   temp
      *          The given new temperature.
      * @post    The new maximum temperature equals the given temperature.
      *          | new.getDmgHeatMax() = temp
      */
     @Basic
     public static void setDmgHeatMax(int temp) {
         DMG_HEAT_MAX = temp;
     }
 
     /**
      * Returns the interval after which a heat damage point is received.
      */
     @Basic
     public static int getDmgHeatInterval() {
         return DMG_HEAT_INTERVAL;
     }
 
     /**
      * Sets the interval after which a heat damage point is received for all squares.
      *
      * @param   interval
      *          The new temperature interval.
      * @post    The new temperature interval equals the given interval.
      *          | new.getDmgHeatInterval() == interval
      */
     @Basic
     public static void setDmgHeatInterval(int interval) {
         DMG_HEAT_INTERVAL = interval;
     }
 
 
     /**
      * Returns the heat damage received on this square.
      *
      * @return  Returns 0 if the temperature is below the heat damage temperature boundary.
      *          | if (getTemp() < DMG_HEAT_MAX)
      *          |   result == 0;
      * @return  If the temperature is above the heat damage temperature boundary, returns the number of damage points
      *          received is calculated and returned.
      *          | if (getTemp() >= DMG_HEAT_MAX)
      *          |   result == Math.floor((getTemp() - DMG_HEAT_MAX) / DMG_HEAT_INTERVAL)
      */
     @Raw
     public int getHeatDmg() {
         if (getTemp() >= DMG_HEAT_MAX)
             return (int) Math.floor((getTemp() - DMG_HEAT_MAX) / DMG_HEAT_INTERVAL);
         else
             return 0;
     }
 
     /**
      * Returns the humidity of the square. The humidity is returned as a percentage with an accuracy of 2 decimals.
      */
     @Basic @Raw
     public double getHumidity() {
         return humidity;
     }
 
     /**
      * Sets the current humidity of the square.
      *
      * @param   humidity
      *          The given humidity.
      * @pre     The value for humidity must be valid and changing the humidity must be allowed.
      *          | (isValidHumidity(humidity) && canChangeHumidity()) == true
      * @post    The new humidity equals the given humidity.
      *          | new.getHumidity() == humidity
      */
     public void setHumidity(double humidity) {
         assert(isValidHumidity(humidity) && canChangeHumidity());
         this.humidity = Tools.roundTo(humidity, 2);
     }
 
     /**
      * Checks whether a given value for humidity is valid.
      *
      * @param   humidity
      *          The given humidity.
      * @return  True if the humidity value is valid. The value is valid if it's not smaller than 0 or larger than 100.
      *          | result == ((humidity >= 0) && (humidity <= 100))
      */
     public static boolean isValidHumidity(double humidity) {
         return (humidity >= 0) && (humidity <= 100);
     }
 
     /**
      * Returns true if you can change the humidity of the square. This method can be overridden in subclasses.
      */
     public boolean canChangeHumidity() {
         return true;
     }
 
     /**
      * Calculates the damage received by rust from this square based on the
      * current humidity.
      *
      * @return  Returns 0 if the humidity is smaller than the maximum humidity under which no rust damage will occur.
      *          | if (getTemp() < DMG_RUST_MAX)
      *          |   result == 0
      * @return  If the humidity is larger than the maximum humidity under which no rust damage will occur, the rust
      *          damage is calculated with 1 point for each specific interval above this level.
      *          | if (getTemp() >= DMG_RUST_MAX)
      *          |   result == Math.floor((getTemp() - DMG_RUST_MAX) / DMG_RUST_INTERVAL)
      */
     @Raw
     public int getRustDmg() {
         if (getTemp() >= DMG_RUST_MAX)
             return (int) Math.floor((getTemp() - DMG_RUST_MAX) / DMG_RUST_INTERVAL);
         else
             return 0;
     }
 
     /**
      * Returns true if the floor of the square is slippery.
      */
     @Basic
     public boolean isSlipperyFloor() {
         return slipperyFloor;
     }
 
     /**
      * Sets the slippery indicator for the floor of the square.
      *
      * @param   slipperyFloor
      *          The given slippery indicator.
      * @post    The new slippery value of the square equals the given value.
      *          | new.isSlipperyFloor() == slipperyFloor
      */
     @Basic
     public void setSlipperyFloor(boolean slipperyFloor) {
         this.slipperyFloor = slipperyFloor;
     }
 
     /**
      * Determines whether the square is slippery.
      *
      * @return  Returns true if the square is slippery because of a slippery floor, or when the humidity of the square
     *          is 100% and the temperature is larger than 0°C, or when the humidity is larger than 10% and the
      *          temperature of the square is not positive.
      *          | result == isSlipperyFloor() || ((getHumidity() == 100) && (getTemp() > 0)) || ((getHumidity() > 10)
      *          |   && (getTemp() <= 0));
      */
     @Raw
     public boolean isSlippery() {
         return isSlipperyFloor() || ((getHumidity() == 100) && (getTemp() > 0)) || ((getHumidity() > 10) &&
                 (getTemp() <= 0));
     }
 
     /**
      * Calculates the inhabitability of a square.
      *
      * @return  The inhabitability index of the square after being calculated with a specific formula.
      *          | result == - (Math.sqrt(Math.pow(getHeatDmg(), 3)) / Math.sqrt(101 - getHumidity()) +
      *          |   Math.sqrt(getColdDmg()));
      */
     @Raw
     public double getInhabitability() {
         return - (Math.sqrt(Math.pow(getHeatDmg(), 3)) / Math.sqrt(101 - getHumidity()) + Math.sqrt(getColdDmg()));
     }
 
     /**
      * Internal getter to access the map containing the borders.
      */
     @Basic @Model
     private Map<Direction, Border> getBorders() {
         return borders;
     }
 
     /**
      * Returns the border at the given direction.
      *
      * @param   dir
      *          The given direction.
      * @return  Returns null if the direction is invalid or if the direction does not have a border associated with it.
      *          | if ((dir == null) || (!getBorders().containsKey(dir)))
      *          |   result == null
      * @return  Returns the border associated with the direction if the direction is valid and a border is associated
      *          with it.
      *          | if ((dir != null) && (getBorders().containsKey(dir)))
      *          |   getBorders().get(dir)
      * @note    If the given direction is valid, a border should ALWAYS be associated with it, unless a subclass did not
      *          implement it's constructors properly.
      */
     @Raw
     public Border getBorder(Direction dir) {
         if ((dir != null) && (getBorders().containsKey(dir)))
             return getBorders().get(dir);
         return null;
     }
 
     /**
      * Internal method to update a border at a certain direction if one is already set.
      *
      * @param   border
      *          The given border.
      * @param   dir
      *          The given direction.
      * @return  The old border object that was present at the direction after removing it's reference to the square.
      *          | result == getBorders().remove(dir)
      */
     private Border updateBorder(Border border, Direction dir) {
         if ((border != null) && (dir != null) && (getBorders().containsKey(dir))) {
             Border old = getBorders().remove(dir);
             old.setSquare(null);
             border.setSquare(this);
             getBorders().put(dir, border);
             return old;
         }
         return null;
     }
 
     /**
      * Sets a new border for a certain direction. The border is cloned before storing to prevent a single border being
      * added to several squares.
      *
      * @param   border
      *          The border object.
      * @param   dir
      *          The border's direction.
      * @post    The new border for the given direction equals the given border.
      *          | new.getBorder(dir) == border
      */
     public void setBorder(Border border, Direction dir) {
         if ((border != null) && (dir != null) && canChangeBorder()) {
             border = (Border) border.clone();
             if (getBorders().containsKey(dir)) {
                 if (!border.canLink(this, dir))
                     return;
                 Border old = updateBorder(border, dir);
                 if (old.getAdjacent() != null) {
                     if (!CAN_EDIT_BORDERS) {
                         updateBorder(old, dir);
                         return;
                     }
                     Border newBorder = (Border) border.clone();
                     border.setAdjacent(newBorder);
                     newBorder.setAdjacent(border);
                     Square neighbour = old.getAdjacent().getSquare();
                     old.setAdjacent(null);
                     if (!newBorder.canLink(neighbour, dir.opposite())) {
                         updateBorder(old, dir);
                         return;
                     }
                     old = neighbour.updateBorder(newBorder, dir.opposite());
                     old.setAdjacent(null);
                 }
             } else {
                 getBorders().put(dir, border);
                 border.setSquare(this);
             }
         }
     }
 
     /**
      * Returns true if you can change the borders of the square. This method can be overridden in subclasses.
      */
     public boolean canChangeBorder() {
         return true;
     }
 
     /**
      * Merges the properties of 2 squares at a certain border.
      *
      * @param   square
      *          The given square.
      * @param   dir
      *          The given border direction.
      * @throws  IllegalArgumentException
      *          Throws an illegal argument exception if the given square is invalid.
      *          | square == null
      * @throws  IllegalArgumentException
      *          Throws an illegal argument exception if the given square is the current square object.
      *          | square.equals(this)
      * @throws  IllegalArgumentException
      *          Throws an illegal argument exception if the given border direction is invalid.
      *          | dir == null
      * @effect  The given border is removed for the current and given square.
      *          | this.setAdjacent(new NoBorder(), dir)
      *          | square.setAdjacent(new NoBorder(), dir)
      * @effect  Both squares are assigned the arithmic mean of their humidities.
      *          | newHumidity = (getHumidity() + square.getHumidity()) / 2
      *          | this.setHumidity(newHumidity)
      *          | square.setHumidity(newHumidity)
      * @effect  Calculates a weight for the temperature of both squares and uses these to calculate a new temperature.
      *          | weight = 2 * (1 - MERGE_WEIGHT) * this.getHumidity() / (square.getHumidity() + this.getHumidity())
      *          | newTemp = ((2 - weight) * square.getTemp() + weight * this.getTemp()) / 2
      *          | square.setTemp(Math.round(newTemp))
      *          | this.setTemp(Math.round(newTemp))
      */
     @Raw
     public void mergeWith(Square square, Direction dir) throws IllegalArgumentException {
         if (square == null)
             throw new IllegalArgumentException("Invalid square!");
         if (square.equals(this))
             throw new IllegalArgumentException("A square can not be merged with itself!");
         if (dir == null)
             throw new IllegalArgumentException("The given border direction is not valid!");
 
         this.setBorder(new NoBorder(), dir);
         square.setBorder(new NoBorder(), dir);
 
         double newHumidity = (getHumidity() + square.getHumidity()) / 2;
         this.setHumidity(newHumidity);
         square.setHumidity(newHumidity);
 
         double weight = 2 * (1 - MERGE_WEIGHT) * this.getHumidity() / (square.getHumidity() + this.getHumidity());
         double newTemp = ((2 - weight) * square.getTemp() + weight * this.getTemp()) / 2;
         square.setTemp((int) Math.round(newTemp));
         this.setTemp((int) Math.round(newTemp));
     }
 
     /**
      * Is called when the square is linked to a neighbour. Should be overridden in subclasses.
      */
     protected void linked() { }
 
     /**
      * Internal method to link 2 squares together.
      *
      * @param   square
      *          The given square.
      * @pre     Before calling this method both squares have to have their coordinates and dungeon set to allow the
      *          method to check whether they are next to each other and in the same dungeon.
      * @throws  IllegalArgumentException
      *          Throws an illegal argument exception if the given square is invalid.
      *          | square == null
      * @throws  IllegalArgumentException
      *          Throws an illegal argument exception if the dungeon isn't set for one of the squares.
      *          | (this.getDungeonAt() == null) || (square.getDungeonAt() == null)
      * @throws  IllegalArgumentException
      *          Throws an illegal argument exception if the position isn't set for one of the squares.
      *          | (this.getPos() == null) || (square.getPos() == null)
      * @throws  IllegalArgumentException
      *          Throws an illegal argument exception if the squares are located in different dungeons.
      *          | !this.getDungeonAt().equals(square.getDungeonAt())
      * @throws  IllegalArgumentException
      *          Throws an illegal argument exception if the squares are not next to each other in the given direction.
      *          | !square.getPos().equals(dir.move(this.getPos()))
      * @throws  IllegalArgumentException
      *          Throws an illegal argument exception if the squares have a different type of border and both can't have
      *          their borders modified.
      *          | !(border.getClass().getName().equals(border2.getClass().getName())) && !(this.canChangeBorder() &&
      *          |   square.canChangeBorder())
      * @post    The squares now share a linked border.
      *          | new.getBorder(dir).getAdjacent().getSquare().equals(square)
      */
     void link(Square square, Direction dir) throws IllegalArgumentException {
         if (square == null)
             throw new IllegalArgumentException("Invalid square!");
         if ((this.getDungeon() == null) || (square.getDungeon() == null))
             throw new IllegalArgumentException("Dungeon not set!");
         if ((this.getPos() == null) || (square.getPos() == null))
             throw new IllegalArgumentException("Position not set!");
         if (!this.getDungeon().equals(square.getDungeon()))
             throw new IllegalArgumentException("Squares are located in different dungeons!");
         if (!square.getPos().equals(dir.move(this.getPos())))
             throw new IllegalArgumentException("Squares are not located next to each other in the given direction!");
         // Determine the proper border to place between both squares
         Border border = this.getBorder(dir);
         Border border2 = square.getBorder(dir.opposite());
         if (!(border.getClass().getName().equals(border2.getClass().getName()))) {
             if (this.canChangeBorder() && !square.canChangeBorder())
                 border = border2;
             else if (!(this.canChangeBorder() && square.canChangeBorder()))
                 throw new IllegalArgumentException("Can't change the border of 2 new neighbouring squares!");
             else if (!border2.overridden(border))
                 border = border2;
         }
         // Link the squares
         this.setBorder(border, dir);
         square.setBorder(border, dir.opposite());
         Border newBorder = this.getBorder(dir);
         Border newBorder2 = square.getBorder(dir.opposite());
         newBorder.setAdjacent(newBorder2);
         newBorder.setSquare(this);
         newBorder2.setAdjacent(newBorder);
         newBorder2.setSquare(square);
         // Notify both squares that they're linked
         this.linked();
         square.linked();
     }
 
     /**
      * Internal method to unlink a square from it's dungeon and all neighbouring squares.
      *
      * @post    The square is no longer linked to it's dungeon.
      *          | new.getDungeonAt() == null
      */
     void unlink() {
         for (Direction dir : Direction.values()) {
             Border border = getBorder(dir);
             if (border.getAdjacent() != null)
                 border.getAdjacent().setAdjacent(null);
             border.setAdjacent(null);
         }
         setDungeon(null);
         setPos(null);
     }
 
     /**
      * Checks whether a square can be reached directly from this square. The time complexity of this method is O(6n) as
      * it uses the getSpace() method in Dungeon which also has this time complexity.
      *
      * @param   square
      *          The given square.
      * @return  Returns false if the current square does not belong to a dungeon.
      *          | if (getDungeonAt() == null)
      *          |   result == false
      * @return  Returns false if the given square is invalid or solid.
      *          | if ((square == null) || (square.isSolid()))
      *          |   result == false
      * @return  If the current square belongs to a dungeon, returns true if the given square can be reached from it.
      *          | space = getDungeonAt().getSpace(getPos())
      *          |   space.contains(square)
      */
     public boolean canReachDirect(Square square) {
         if (getDungeon() == null)
             return false;
         if ((square == null) || (square.isSolid()))
             return false;
         List<Square> space = getDungeon().getSpace(getPos());
         return space.contains(square);
     }
 
     /**
      * Checks whether a square can be reached directly or though teleports from this square. The time complexity of this
      * method is O(6n) as it uses the getTeleSpace() method in Dungeon which also has this time complexity.
      *
      * @param   square
      *          The given square.
      * @return  Returns false if the current square does not belong to a dungeon.
      *          | if (getDungeonAt() == null)
      *          |   result == false
      * @return  Returns false if the given square is invalid or solid.
      *          | if ((square == null) || (square.isSolid()))
      *          |   result == false
      * @return  If the current square belongs to a dungeon, returns true if the given square can be reached from it.
      *          | space = getDungeonAt().getTeleSpace(getPos())
      *          |   space.contains(square)
      */
     public boolean canReach(Square square) {
         if (getDungeon() == null)
             return false;
         if ((square == null) || (square.isSolid()))
             return false;
         List<Square> space = getDungeon().getTeleSpace(getPos());
         return space.contains(square);
     }
 
     /**
      * Returns true if the restrictions of the subclass invariants currently apply. This should always be true if an
      * end-user calls this method.
      */
     protected boolean isRestricted() {
         return restricted;
     }
 
     /**
      * Sets whether the restrictions of subclass invariants currently apply. This allows internal classes to change the
      * properties of these subclasses when needed.
      *
      * @param   restricted
      *          The given restricted flag.
      * @post    The new restricted flag equals the given restricted flag.
      *          | new.isRestricted() == restricted
      */
     protected void setRestricted(boolean restricted) {
         this.restricted = restricted;
     }
 
     /**
      * Returns true if the square is solid and can not contain anything.
      */
     public boolean isSolid() {
         return false;
     }
 
     /**
      * Filters all squares of a certain class type from a list.
      *
      * @param   squares
      *          The given list of squares.
      * @param   clazz
      *          The square class type.
      * @return  If the squares list or the class type are not valid, the method returns null.
      *          | if ((squares == null) && (clazz == null))
      *          |   result == null
      * @return  If the squares list and the class type are valid, the method creates a list containing all squares of
      *          that type found in the given list.
      *          | res = new ArrayList<>()
      *          | for (sq : squares)
      *          |   if (sq.getClass().getName().equals(clazz.getName()))
      *          |       res.add(sq)
      *          | result == res
      */
     public static List<Square> filter(List<Square> squares, Class<? extends Square> clazz) {
         if ((squares == null) && (clazz == null))
             return null;
         List<Square> res = new ArrayList<>();
         for (Square sq : squares)
             if (sq.getClass().getName().equals(clazz.getName()))
                 res.add(sq);
         return res;
     }
 
     /**
      * Returns a string representation of the object.
      */
     @Override
     public String toString() {
         return "Square(temp:" + getTemp() + ";humidity:" + getHumidity() + ";slippery:" + isSlipperyFloor() + ";pos:" +
             getPos() + ")";
     }
 
     /**
      * Returns the avatar currently positioned on the square.
      */
     @Basic
     public Avatar getAvatar() {
         return avatar;
     }
 
     /**
      * Internal method to assign an avatar to a square.
      *
      * @param   avatar
      *          The given avatar
      * @throws  IllegalArgumentException
      *          Throws an illegal argument exception if the given avatar is invalid.
      *          | avatar == null
      * @throws  IllegalArgumentException
      *          Throws an illegal argument exception if the square is solid.
      *          | this.isSolid() == true
      * @post    The new avatar equals the given avatar.
      *          | new.getAvatar() == avatar
      */
     protected void setAvatar(Avatar avatar) throws IllegalArgumentException {
         if (this.isSolid())
             throw new IllegalArgumentException("Can't position an avatar on a solid square!");
         if (avatar == null)
             this.avatar = null;
         else if (this.avatar == null)
             this.avatar = avatar;
         else
             if (Tools.randBool()) {
                 this.avatar.kill();
                 this.avatar = avatar;
             } else avatar.kill();
     }
 
 }
