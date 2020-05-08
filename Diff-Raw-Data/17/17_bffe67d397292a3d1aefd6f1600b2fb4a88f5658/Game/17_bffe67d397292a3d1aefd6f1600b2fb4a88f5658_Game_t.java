 package org.sankozi.rogueland.control;
 
 import com.google.common.collect.Lists;
 import java.awt.Point;
 import java.util.List;
 import org.apache.log4j.Logger;
 import org.sankozi.rogueland.model.Actor;
 import org.sankozi.rogueland.model.AiActor;
 import org.sankozi.rogueland.model.Controls;
 import org.sankozi.rogueland.model.Damage;
 import org.sankozi.rogueland.model.Destroyable.Param;
 import org.sankozi.rogueland.model.Level;
 import org.sankozi.rogueland.model.LevelGenerator;
 import org.sankozi.rogueland.model.Locator;
 import org.sankozi.rogueland.model.Move;
 import org.sankozi.rogueland.model.Player;
 import org.sankozi.rogueland.model.Tile;
 
 /**
  * Object responsible for single game instance
  * @author sankozi
  */
 public class Game {
     private final static Logger LOG = Logger.getLogger(Game.class);
 
     private Level level = new Level(300, 200);
     private GameLog log = new GameLog();
     private Player player = null;
     private List<Actor> actors = Lists.newArrayList();
 	private List<Actor> toBeRemoved = Lists.newArrayList();
 
 	private Locator locator;
 
     private GameRunnable runningGame;
 
 	{
 		LevelGenerator.generate(level);
 		locator = new GameLevelLocator(this, level);
 	}
 
     public Runnable provideRunnable(){
         if(runningGame == null){
             runningGame = new GameRunnable();
             return runningGame;
         } else {
             throw new IllegalArgumentException("game has already started");
         }
     }
 
     /**
      * Sets controls for Player character
      * @param controls
      */
     public void setControls(Controls controls){
         this.player = new Player(controls);
         player.setLocation(new Point(5,5));
         actors.add(player);
         Actor ai = new AiActor();
         ai.setLocation(new Point(10,10));
         actors.add(ai);
     }
 
     public void addLogListener(LogListener logListener){
         log.addListener(logListener);
     }
 
     private class GameRunnable implements Runnable {
         @Override
         public void run() {
             GameLog.initThreadLog(log);
             GameLog.info("Game has started");
             do {
                 processActors();
            } while (!player.isDestroyed());
         }
 
         private void processActor(Actor actor) {
             Move m;
             Point newLocation;
             Point actorLocation = actor.getLocation();
             final Tile[][] tiles = level.getTiles();
             do {
                 tiles[actorLocation.x][actorLocation.y].actor = actor;
                 m = actor.act(level, locator);
                 newLocation = actorLocation.getLocation();
                 processMove(m, newLocation);
 //                LOG.info("actor : " + actor + " move : " + newLocation);
                 tiles[actorLocation.x][actorLocation.y].actor = null;
             } while (!validLocation(newLocation, tiles));
 
             Tile tile = tiles[newLocation.x][newLocation.y];
             if(tile.actor != null){
                 interact(actor, tile.actor);
             } else {
                 actorLocation = newLocation;
             }
             tiles[actorLocation.x][actorLocation.y].actor = actor;
             actor.setLocation(actorLocation);
         }
 
         private void processActors() {
             for(Actor actor:actors){
                 actor.heal(actor.destroyableParam(Param.HEALTH_REGEN));
                 processActor(actor);
             }
 			actors.removeAll(toBeRemoved);
 			toBeRemoved.clear();
         }
     }
 
     private void removeActor(Actor actor){
 		if(actor == player){
 			getLog().log("You die...", MessageType.INFO);
 		}
 		toBeRemoved.add(actor);
         level.getTiles()[actor.getLocation().x][actor.getLocation().y ].actor = null;
     }
 
 	
 
     /**
      * Default attack handle
      * @param actor
      * @param target
      */
     private void interact(Actor actor, Actor target) {
         Damage dam = actor.getPower();
         float res = target.protection(dam.type);
         
         if(res < dam.value){
             GameLog.info(actor.getName() + " attacked " + target.getName() + " for " + dam + "[" + res + " resisted]");
             target.damage(dam.value - (int) res);
             if(target.isDestroyed()){
                 GameLog.info(target.getName() + " is destroyed!");
                 removeActor(target);
             }
         }
     }
 
     private static void processMove(Move m, Point newLocation) {
         if(m instanceof Move.Go){
             switch ((Move.Go) m) {
                 case EAST:
                     newLocation.x++;
                     break;
                 case NORTH:
                     newLocation.y--;
                     break;
                 case WEST:
                     newLocation.x--;
                     break;
                 case SOUTH:
                     newLocation.y++;
                     break;
                 case NORTHEAST:
                     newLocation.y--;
                     newLocation.x++;
                     break;
                 case NORTHWEST:
                     newLocation.y--;
                     newLocation.x--;
                     break;
                 case SOUTHEAST:
                     newLocation.y++;
                     newLocation.x++;
                     break;
                 case SOUTHWEST:
                     newLocation.y++;
                     newLocation.x--;
                     break;
                 default:
                     LOG.error("unhandled move : " + m);
             }
         } else if(m == Move.WAIT) {
             GameLog.info("Waiting");
         } else {
             LOG.error("unhandled move : " + m);
         }
     }
 
     private static boolean validLocation(Point playerLocation, Tile[][] tiles) {
         //location outside level
         if(playerLocation.x < 0 || playerLocation.y < 0
                 || playerLocation.x >= tiles.length
                 || playerLocation.y >= tiles[0].length) {
             return false;
         } else {
             //location inaccessible and that cannot be intercarted with
             Tile tile = tiles[playerLocation.x][playerLocation.y];
             return tile.type != Tile.Type.WALL;
         }
     }
 
 	public Level getLevel() {
         return level;
     }
 
     public GameLog getLog() {
         return log;
     }
 
     public Player getPlayer() {
         return player;
     }
 }
