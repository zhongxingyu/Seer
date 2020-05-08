 /*
 
 Entity.java
 
 */
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.ListIterator;
 
 public class Entity implements Comparable<Entity>, Updateable
 {
     /*
      * Enumerations
      */
     
     public enum Type
     {
         UNKNOWN(-1),
         BEING(0),
         DIRT(1),
         STONE(2),
         WHEAT(3),
         STONEMILL(4);
         
         private int id;
         
         private Type(int id)
         {
             this.id = id;
         }
         
         public int getID()
         {
             return id;
         }
     }
     
     public enum Attribute
     {
         FETCHABLE,
         CONSUMABLE
     }
     
     /*
      * Properties
      */
     
     protected Type type;
     protected Location location;
     protected Direction direction;
     protected double speed; // in "tiles per second"
     protected String description;
     protected GameTime age;
     protected HashSet<Attribute> attributes;
     protected ArrayList<EntityState> states;
     protected ListIterator<EntityState> stateCursor;
     protected EntityState currentState;
     
     /*
      * Constructors
      */
     
     public Entity()
     {
         type = Type.UNKNOWN;
         location = new Location(0, 0);
         direction = Direction.North;
         speed = 0;
         description = "";
         age = new GameTime();
         
         attributes = new HashSet<Attribute>();
         attributes.add(Attribute.CONSUMABLE);
         
         states = new ArrayList<EntityState>();
         states.add(new EntityState(0, description));
         stateCursor = states.listIterator();
         currentState = stateCursor.next();
     }
     
     // DESTROY
     public Entity(Type type)
     {
         this();
         
         this.type = type;
     }
     
     public Entity(Entity copy)
     {
         type = copy.type;
         location = new Location(copy.location);
         direction = new Direction(copy.direction);
         speed = copy.speed;
         description = copy.description;
         age = new GameTime(copy.age);
         attributes = new HashSet<Attribute>(copy.attributes);
         states = new ArrayList<EntityState>(copy.states);
         stateCursor = states.listIterator();
         currentState = stateCursor.next();
     }
     
     /*
      * Accessors
      */
     
     public Type getType()
     {
         return type;
     }
     
     public Location getLocation()
     {
         return location;
     }
     
     public Direction getDirection()
     {
         return direction;
     }
     
     public double getSpeed()
     {
         return speed;
     }
     
     public String getDescription()
     {
         if (currentState != null)
         {
             return currentState.getDescription();
         }
         
         return description;
     }
     
     public boolean hasAttribute(Attribute attribute)
     {
         if (currentState != null)
         {
             return currentState.hasAttribute(attribute);
         }
         
         return attributes.contains(attribute);
     }
     
     public EntityState getState()
     {
         return currentState;
     }
     
     /*
      * Public Methods
      */
     
     public boolean equals(Object other)
     {
         if (other == this)
         {
             return true;
         }
         
         if (!(other instanceof Entity))
         {
             return false;
         }
         
        return hashCode() == ((Entity)other).hashCode();
     }
     
     public int hashCode()
     {
        return type.getID() * 100 + (currentState != null ?
                                     currentState.getID() : 0);
     }
     
     public int compareTo(Entity other)
     {
         return (type.getID() * 100 + (currentState != null ?
                                       currentState.getID() : 0)) -
                (other.type.getID() * 100 + (other.currentState != null ?
                                             other.currentState.getID() : 0));
     }
     
     public void update(long ms)
     {
         age.addMilliseconds(ms);
     }
     
     public void turn(Direction direction)
     {
         this.direction = new Direction(direction);
     }
     
     public void move(double distance)
     {
         location.move(this.direction, distance);
     }
     
     public void move(Direction direction, double distance)
     {
         turn(direction);
         move(distance);
     }
 }
