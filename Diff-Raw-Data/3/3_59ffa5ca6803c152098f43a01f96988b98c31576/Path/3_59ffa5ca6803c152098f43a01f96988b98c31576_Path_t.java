 package cs2114.tiletraveler;
 
 import java.util.ArrayList;
 
 // -------------------------------------------------------------------------
 /**
  * Creates a path for a moving enemy to follow in the form of a
  * CircularLinkedList
  *
  * @author Luciano Biondi (lbiondi)
  * @author Ezra Richards (MrZchuck)
  * @author Jacob Stenzel (sjacob95)
  * @version 2013.12.08
  */
 public class Path
 {
 
     private Map                          map;
 
     // Whether to allow or disallow Tiles in ArrayList conditions
     private PathSetting                  setting;
     // Tiles to be allowed or disallowed
     private Tile[]                       conditions;
 
     // the final path
     private CircularLinkedList<Location> tpath;
 
 
     /**
      * THIS PATH CONSTRUCTOR IS ONLY FOR TESTING PURPOSES
      *
      * @param setting
      *            - Whether to allow or disallow Tiles in the array conditions
      * @param conditions
      *            - tiles to be allowed or disallowed
      * @param map
      *            - the Map that is active
      */
     public Path(PathSetting setting, Tile[] conditions, Map map)
     {
         this.setting = setting;
         this.conditions = conditions;
         this.map = map;
     }
 
 
     /**
      * Creates a Path with the provided points as corners according to the
      * provided conditions on the current Map. If the last Location matches the
      * first, it will automatically adopt a cyclical path.
      *
      * @param setting
      *            - Whether to allow or disallow Tiles in the array conditions
      * @param conditions
      *            - tiles to be allowed or disallowed
      * @param map
      *            - the Map that is active
      * @param locations
      *            - the Locations of the path's corners
      */
     public Path(
         PathSetting setting,
         Tile[] conditions,
         Map map,
         Location[] locations)
     {
         this.setting = setting;
         this.conditions = conditions;
         this.map = map;
 
         checkValidPath(locations);
 
         checkWithinBounds(locations);
 
         tpath = finalizePath(interpolatePath(locations));
 
     }
 
 
     // ----------------------------------------------------------
     /**
      * returns a Location[] containing the two Locations and all of the
      * Locations in between
      *
      * @param loc1
      *            - the first Location
      * @param loc2
      *            - the second Location
      * @return a Location[] containing the two Locations and all of the
      *         Locations in between
      * @throws InvalidLineException
      * @throws PathObstructionException
      */
     public Location[] interpolatePath(Location loc1, Location loc2)
         throws InvalidLineException,
         PathObstructionException
 
     {
         Location[] newPath;
         if (loc1.isDueNorth(loc2))
         {
             int distance = loc1.getDistance(loc2);
             newPath = new Location[distance + 1];
             newPath[0] = loc1;
             for (int i = 1; i <= distance; i++)
             {
                 Location temp = new Location(loc1.x(), loc1.y() + i);
                 checkObstruction(temp);
                 newPath[i] = temp;
             }
         }
 
         else if (loc1.isDueSouth(loc2))
         {
             int distance = loc1.getDistance(loc2);
             newPath = new Location[distance + 1];
             newPath[0] = loc1;
             for (int i = 1; i <= distance; i++)
             {
                 Location temp = new Location(loc1.x(), loc1.y() - i);
                 checkObstruction(temp);
                 newPath[i] = temp;
             }
         }
 
         else if (loc1.isDueEast(loc2))
         {
             int distance = loc1.getDistance(loc2);
             newPath = new Location[distance + 1];
             newPath[0] = loc1;
             for (int i = 1; i <= distance; i++)
             {
                 Location temp = new Location(loc1.x() + i, loc1.y());
                 checkObstruction(temp);
                 newPath[i] = temp;
             }
         }
 
         else if (loc1.isDueWest(loc2))
         {
             int distance = loc1.getDistance(loc2);
             newPath = new Location[distance + 1];
             newPath[0] = loc1;
             for (int i = 1; i <= distance; i++)
             {
                 Location temp = new Location(loc1.x() - i, loc1.y());
                 checkObstruction(temp);
                 newPath[i] = temp;
             }
         }
 
         else
         {
             throw new InvalidLineException(loc1, loc2);
         }
 
         return newPath;
     }
 
 
     /**
      * returns a Location[] containing the two Locations and all of the
      * Locations in between precondition: locs contains two or more points
      *
      * @param locs
      *            - the Locations to be interpolated
      * @return an ArrayList<Location> containing the provided Locations and all
      *         of the Locations in between each pair
      */
     public ArrayList<Location> interpolatePath(Location[] locs)
     {
         ArrayList<Location> tempPath = new ArrayList<Location>();
         for (int i = 1; i < locs.length; i++)
         {
             Location[] pointSequence = interpolatePath(locs[i - 1], locs[i]);
 
            if (pointSequence[pointSequence.length - 1] != null)
             {
                 for (int j = 0; j < pointSequence.length; j++)
                 {
                     // checks for adjacent repeated points
                     if (tempPath.isEmpty()
                         || !tempPath.get(tempPath.size() - 1).equals(
                             pointSequence[j]))
                     {
                         tempPath.add(pointSequence[j]);
                     }
                 }
             }
         }
         return tempPath;
     }
 
 
     /**
      * @param loc
      *            - the Location to be checked
      */
     public void checkObstruction(Location loc)
     {
         Tile tile = map.getTile(loc);
         boolean contains = false;
         for (int i = 0; i < conditions.length; i++)
         {
             if (conditions[i].equals(tile))
             {
                 contains = true;
                 break;
             }
         }
         if ((!contains && setting.equals(PathSetting.ALLOW)) || contains
             && setting.equals(PathSetting.DISALLOW))
         {
             throw new PathObstructionException(loc, tile);
         }
     }
 
 
     // ----------------------------------------------------------
     /**
      * @param locs
      *            - the proposed path
      */
     public void checkValidPath(Location[] locs)
     {
         if (locs.length <= 1)
         {
             throw new IncompletePathException();
         }
     }
 
 
     /**
      * @param loc
      *            - the Location to be checked
      */
     public void checkWithinBounds(Location loc)
     {
         if (this.map.getTile(loc).equals(Tile.INVALID))
         {
             throw new OutsideMapException(loc, this.map);
         }
     }
 
 
     /**
      * @param locs
      *            - the Locations to be checked
      */
     public void checkWithinBounds(Location[] locs)
     {
         for (Location loc : locs)
         {
             checkWithinBounds(loc);
         }
     }
 
 
     // ----------------------------------------------------------
     /**
      * Takes in an ArrayList containing the Location objects that makes up the
      * path, and returns a Circular Linked List of those objects.
      *
      * @param locs
      *            The Locations to be made into a path
      * @return the Path CircularLinkedList
      */
     public CircularLinkedList<Location> finalizePath(ArrayList<Location> locs)
     {
         CircularLinkedList<Location> list;
         if (locs.get(locs.size() - 1).equals(locs.get(0)))
         {
             locs.remove(locs.size() - 1);
         }
         else
         {
             for (int i = locs.size() - 2; i > 0; i--)
             {
                 locs.add(locs.get(i));
             }
         }
         list = new CircularLinkedList<Location>();
         for (int i = 0; i < locs.size(); i++)
         {
             list.add(locs.get(i));
         }
         return list;
     }
 
 
     // ----------------------------------------------------------
     /**
      * @return the Path CircularLinkedList
      */
     public CircularLinkedList<Location> get()
     {
         return tpath;
     }
 }
