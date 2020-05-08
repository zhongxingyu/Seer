 package game;
 
import java.util.concurrent.locks.Lock;

 /**
 *
 * @author maweki
 */
 public class World {
    public static GameWorld w;
    
    /**
     * Reset the GameWorld
     * @return the new World
     */
    static public GameWorld reset() {
	   if (null != World.w) {
		   /* if there is a world in existance,
		    * use the old lock
		    */
		   Lock oldlock = game.World.w.WorldLock;
		   game.World.w = new GameWorld();
		   game.World.w.WorldLock = oldlock;
	   }
	   else {
		   game.World.w = new GameWorld();
	   }
 	   return game.World.w;
    }
 }
