 package cs2114.tiletraveler;
 
 // -------------------------------------------------------------------------
 /**
  *  A stage for the application
  *
  * @author Luciano Biondi (lbiondi)
  * @author Ezra Richards  (MrZchuck)
  * @author Jacob Stenzel  (sjacob95)
  * @version 2013.12.08
  */
 public class Stage5
     extends Stage
 {
 
     private static Map map = new Map(
                                "WWWWWWWWDWWWWWWWW",
                                "W~~~~~~~Q~~~~~~~W",
                                "W~~~~Q~QQ~~~~Q~~W",
                                "W~~~~~~~~~~~~Q~~W",
                                "W~~~~Q~~~~~~~~~~W",
                                "W~~QQQ~~~~Q~~Q~~W",
                                "W~~Q~~~~~~~~~Q~~W",
                                "W~~~~~~~~~~Q~Q~~W",
                                "W~~QQ~Q~Q~Q~QQ~~W",
                                "W~~~~~~~~~~Q~~~~W",
                                "W~~Q~~Q~~~~~~~~~W",
                                "W~~~~Q~Q~Q~Q~~~~W",
                                "W~~~~~Q~Q~QQ~~~~W",
                                "W~~~~~~~~~~~~~~~W",
                                "WWWWWWWWOWWWWWWWW");
 
     private static Location startLoc = new Location(8, 0);
 
     // ----------------------------------------------------------
     /**
      * Create a new Stage5 object.
      * @param tileSize
      *            The size of one Tile in pixels.
      */
     public Stage5(float tileSize)
     {
         super(map, startLoc);
        getEnemyMap().addEnemy(new Floater(tileSize, this, new Location(5, 1), new Location(5, 13)));
        getEnemyMap().addEnemy(new Floater(tileSize, this, new Location(7, 13), new Location(7, 1)));
         getEnemyMap().addEnemy(new Floater(tileSize, this, new Location(12, 6), new Location(7, 6)));
     }
 
     /**
      * reset the stage
      * @param tileSize
      *          The size of one Tile in pixels
      */
     public Stage reset(float tileSize)
     {
         return new Stage5(tileSize);
     }
 }
