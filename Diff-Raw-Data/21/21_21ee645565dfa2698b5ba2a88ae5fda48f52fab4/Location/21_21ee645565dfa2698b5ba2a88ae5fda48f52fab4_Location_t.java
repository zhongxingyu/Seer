 package cs2114.tiletraveler;
 
 // -------------------------------------------------------------------------
 /**
  * Location class to keep track of grid
  *
  * @author Luciano Biondi (lbiondi)
  * @author Ezra Richards (MrZchuck)
  * @author Jacob Stenzel (sjacob95)
  * @version 2013.12.08
  */
 public class Location
 {
     private int xCoor;
     private int yCoor;
 
 
     // ----------------------------------------------------------
     /**
      * Create a new Location object.
      *
      * @param x
      *            x coordinate of location
      * @param y
      *            y coordinate of location
      */
     public Location(int x, int y)
     {
         xCoor = x;
         yCoor = y;
     }
 
 
     // ----------------------------------------------------------
     /**
      * Get the x of location
      *
      * @return xCoor x coordinate of location
      */
     public int x()
     {
         return xCoor;
     }
 
 
     // ----------------------------------------------------------
     /**
      * Get the y of location
      *
      * @return yCoor y coordinate of location
      */
     public int y()
     {
         return yCoor;
     }
 
 
     /**
      * @return the Location's neighbor in a given Direction
      * @param direction
      *            is the direction desired
      */
     public Location getNeighbor(Direction direction)
     {
         if (direction.equals(Direction.EAST))
         {
             return east();
         }
         else if (direction.equals(Direction.NORTH))
         {
             return north();
         }
         else if (direction.equals(Direction.SOUTH))
         {
             return south();
         }
         else
        // if (direction.equals(Direction.WEST))
         {
            return west();
         }
     }
 
 
     /**
      * @return one unit to the north of the current one
      */
     public Location north()
     {
         return new Location(xCoor, yCoor + 1);
     }
 
 
     /**
      * @return one unit to the south of the current one
      */
     public Location south()
     {
         return new Location(xCoor, yCoor - 1);
     }
 
 
     /**
      * @return one unit to the east of the current one
      */
     public Location east()
     {
         return new Location(xCoor + 1, yCoor);
     }
 
 
     /**
      * @return one unit to the west of the current one
      */
     public Location west()
     {
         return new Location(xCoor - 1, yCoor);
     }
 
 
     /**
      * Moves the Location north by an increment
      *
      * @param inc
      *            - the increment
      * @return the new Location
      */
     public Location north(int inc)
     {
         return new Location(xCoor, yCoor + inc);
     }
 
 
     /**
      * Moves the Location south by an increment
      *
      * @param inc
      *            - the increment
      * @return the new Location
      */
     public Location south(int inc)
     {
         return new Location(xCoor, yCoor - inc);
     }
 
 
     /**
      * Moves the Location east by an increment
      *
      * @param inc
      *            - the increment
      * @return the new Location
      */
     public Location east(int inc)
     {
         return new Location(xCoor + inc, yCoor);
     }
 
 
     /**
      * Moves the Location west by an increment
      *
      * @param inc
      *            - the increment
      * @return the new Location
      */
     public Location west(int inc)
     {
         return new Location(xCoor - inc, yCoor);
     }
 
 
     /**
      * @return whether or not the provided Location is due North of the current
      * @param loc
      *            - the provided Location to be compared
      */
     public boolean isDueNorth(Location loc)
     {
         if (loc == null)
         {
             return false;
         }
         return (xCoor == loc.x() && yCoor < loc.y());
     }
 
 
     /**
      * @return whether or not the provided Location is due South of the current
      * @param loc
      *            - the provided Location to be compared
      */
     public boolean isDueSouth(Location loc)
     {
         if (loc == null)
         {
             return false;
         }
         return (xCoor == loc.x() && yCoor > loc.y());
     }
 
 
     /**
      * @return whether or not the provided Location is due East of the current
      * @param loc
      *            - the provided Location to be compared
      */
     public boolean isDueEast(Location loc)
     {
         if (loc == null)
         {
             return false;
         }
         return (xCoor < loc.x() && yCoor == loc.y());
     }
 
 
     /**
      * @return whether or not the provided Location is due West of the current
      * @param loc
      *            - the provided Location to be compared
      */
     public boolean isDueWest(Location loc)
     {
         if (loc == null)
         {
             return false;
         }
         return (xCoor > loc.x() && yCoor == loc.y());
     }
 
 
     /**
     * @return the distance between this and the provided Location (assuming the
     *         two points share an x or y coordinate)
      * @param loc
      *            - the second Location
      */
     public int getDistance(Location loc)
     {
         if (!(xCoor == loc.x() && yCoor == loc.y()))
         {
             if (xCoor == loc.x())
             {
                 return Math.abs(yCoor - loc.y());
             }
             else if (yCoor == loc.y())
             {
                 return Math.abs(xCoor - loc.x());
             }
             else
             {
                 throw new UnsupportedOperationException(
                     "This method cannot be called on points that do not share"
                         + " a y or x coordinate");
             }
         }
         else
         {
             return 0;
         }
 
     }
 
 
     // ----------------------------------------------------------
     /**
      * Converts a Direction to a Location representing displacement
      *
      * @param direction
      *            - the Direction to be converted
      * @return the Location equivalent
      */
     public static Location convertToDisplacement(Direction direction)
     {
         switch (direction)
         {
             case NORTH:
                 return new Location(0, 1);
 
             case SOUTH:
                 return new Location(0, -1);
 
             case EAST:
                 return new Location(1, 0);
 
            default: // WEST:
                 return new Location(-1, 0);
         }
     }
 
 
     @Override
     public boolean equals(Object obj)
     {
         if (obj instanceof Location && ((Location)obj).x() == xCoor
             && ((Location)obj).y() == yCoor)
         {
             return true;
         }
         return false;
     }
 
 
     @Override
     public String toString()
     {
         StringBuilder builder = new StringBuilder();
         builder.append("(");
         builder.append(xCoor);
         builder.append(", ");
         builder.append(yCoor);
         builder.append(")");
         return builder.toString();
     }
 
 }
