 package game;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.CopyOnWriteArrayList;
 import org.apache.log4j.Logger;
 
 /**
  * The Time to live class contains code that checks the time to live of certain
  * objects (such as bonus drops, projectiles) and removes them from the game
  * once it is time for them to be removed.
  *
  * @author Michael
  */
 public class timeToLive implements Runnable {
 
     private GameState gameState;
     private final static Logger log = Logger.getLogger(timeToLive.class.getName());
 
     public timeToLive(GameState gs) {
         gameState = gs;
         log.setLevel(Logic.LOG_LEVEL);
     }
 
     @Override
     public void run() {
         checkTTL();
     }
 
     private void checkTTL() {
         //work on all bonus drops, explosions, projectiles
         TTLLogic(gameState.getBonusDrops());
         TTLLogic(gameState.getExplosions());
         TTLLogic(gameState.getProjectiles());
     }
 
     private void TTLLogic(List<? extends MapObjectTTL> objectList) {
         if (!objectList.isEmpty()) {
             log.debug("Starting TTL Check");
            ArrayList<Object> toRemove = new ArrayList();
             for (MapObjectTTL object : objectList) {             
                 //TTL is in seconds : if it still has life left we simply set it for the next one, else, it's gone
                 if (object.getTTL() > 0) {
                     object.setTTL(object.getTTL() - 1);
                 } 
                 else {
                     toRemove.add(object);
                 }
             }
             
             //now that we have a list of objects, we remove them
             //deal with types as appropriate.  OK to pull first entry always because we make sure befor that the list is not empty
             if (objectList.get(0) instanceof Projectile)
             {
                gameState.removeListOfProjectiles(toRemove); 
             }
             else if (objectList.get(0) instanceof MapObjectTTL)
             {
                 gameState.removeListOfExplosions(toRemove);
             }
             //last possibility
             else
             {
                 gameState.removeListOfBonusDrops(toRemove);
             }
         }
     }
 }
