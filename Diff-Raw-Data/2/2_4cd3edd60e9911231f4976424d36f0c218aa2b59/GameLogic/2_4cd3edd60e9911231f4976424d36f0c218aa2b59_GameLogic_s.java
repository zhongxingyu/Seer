 package dungeon.game;
 
 import dungeon.models.*;
 import dungeon.models.messages.IdentityTransform;
 import dungeon.models.messages.Transform;
 import dungeon.ui.messages.MoveCommand;
 import dungeon.util.Vector;
 
 import java.util.ArrayList;
 import java.util.EnumSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  * The game logic.
  *
  * It works a bit like a state machine. Every time you call {@link GameLogic#pulse(double)}, it evaluates
  * what happened in the amount of time, that you pass to {@link GameLogic#pulse(double)}, with respect to the flags,
  * that are set.
  *
  * Think of commands, that the player can send (like walking up), as flags. Let's say for example the UP and LEFT flag
  * are set and you call {@code pulse(150)}. This will return transforms that correspond to moving in up-left direction
  * for 150ms.
  */
 public class GameLogic {
   private static final int SPEED = 1000;
 
   private final Set<MoveCommand> activeMoveDirections = EnumSet.noneOf(MoveCommand.class);
 
   private GameState gameState = GameState.PLAYING;
 
   private World world;
 
   public GameLogic (World world) {
     this.world = world;
   }
 
   /**
    * Set a move flag.
    */
   public void activateMoveDirection (MoveCommand command) {
     this.activeMoveDirections.add(command);
   }
 
   /**
    * Reset a move flag.
    */
   public void deactivateMoveDirection (MoveCommand command) {
     this.activeMoveDirections.remove(command);
   }
 
   /**
    * Returns the current game state.
    *
    * You can use this to check, if the player has died, won, etc.
    */
   public GameState getGameState () {
     return this.gameState;
   }
 
   /**
    * Compute all changes, that have happened in the last #delta seconds.
    *
    * @return A list of all changes that have happened
    */
   public List<Transform> pulse (double delta) {
     List<Transform> transformLog = new ArrayList<>();
 
     this.applyTransform(this.handleMovement(delta), transformLog);
     this.applyTransform(this.handleEnemies(), transformLog);
     this.applyTransform(this.handleTeleporters(), transformLog);
 
     this.handleDefeat();
     this.handleWin();
 
     return transformLog;
   }
 
   /**
    * Apply all transforms to the internal world object and log them.
    */
   private void applyTransforms (List<Transform> transforms, List<Transform> log) {
     for (Transform transform : transforms) {
       this.applyTransform(transform, log);
     }
   }
 
   /**
    * Apply a transform to the internal world object and log it.
    */
   private void applyTransform (Transform transform, List<Transform> log) {
     this.world = this.world.apply(transform);
 
     log.add(transform);
   }
 
   private Transform handleMovement (double delta) {
     Transform movementTransform = moveTransform(delta);
     movementTransform = filterWalls(movementTransform);
 
     return filterBorders(movementTransform);
   }
 
   /**
    * Create the appropriate MoveTransform with respect to the currently active directions.
    */
   private Transform moveTransform (double delta) {
     Vector direction = new Vector(0, 0);
 
     for (MoveCommand moveCommand : this.activeMoveDirections) {
       direction = direction.plus(moveCommand.getDirection());
     }
 
     if (direction.isZero()) {
       return new IdentityTransform();
     } else {
       direction = direction.normalize();
       direction = direction.times(SPEED * delta);
 
       return new Player.MoveTransform((int)direction.getX(), (int)direction.getY());
     }
   }
 
   /**
    * Prevent movement if the player would walk on a wall.
    */
   private Transform filterWalls (Transform transform) {
     Player movedPlayer = this.world.getPlayer().apply(transform);
 
     for (Tile wall : this.world.getCurrentRoom().getWalls()) {
       if (movedPlayer.touches(wall)) {
         return new IdentityTransform();
       }
     }
 
     return transform;
   }
 
   /**
    * Prevent movement if the player would leave the playing field.
    */
   private Transform filterBorders (Transform transform) {
     Player movedPlayer = this.world.getPlayer().apply(transform);
 
     if (movedPlayer.getPosition().getY() < 0
       || movedPlayer.getPosition().getY() + Player.SIZE > this.world.getCurrentRoom().getYSize()
       || movedPlayer.getPosition().getX() < 0
       || movedPlayer.getPosition().getX() + Player.SIZE > this.world.getCurrentRoom().getXSize()) {
       return new IdentityTransform();
     } else {
       return transform;
     }
   }
 
   /**
    * Translate enemy contact into a transform.
    */
   private Transform handleEnemies () {
     for (Enemy enemy : this.world.getCurrentRoom().getEnemies()) {
       if (this.world.getPlayer().touches(enemy)) {
         return new Player.HitpointTransform(-1);
       }
     }
 
     return new IdentityTransform();
   }
 
   /**
   * Create a teleport transform it the player touches a teleporter.
    */
   private Transform handleTeleporters () {
     for (TeleporterTile teleporter : this.world.getCurrentRoom().getTeleporters()) {
       if (this.world.getPlayer().touches(teleporter)) {
         TeleporterTile.Target target = teleporter.getTarget();
 
         return new Player.TeleportTransform(target.getRoomId(), target.getX(), target.getY());
       }
     }
 
     return new IdentityTransform();
   }
 
   /**
    * Set the game state to DEFEAT when the player's hit points drop to 0.
    */
   private void handleDefeat () {
     if (this.world.getPlayer().getHitPoints() == 0) {
       this.gameState = GameState.DEFEAT;
     }
   }
 
   /**
    * Set the game state to VICTORY when the player touches a victory tile.
    */
   private void handleWin () {
     for (VictoryTile tile : this.world.getCurrentRoom().getVictoryTiles()) {
       if (this.world.getPlayer().touches(tile)) {
         this.gameState = GameState.VICTORY;
       }
     }
   }
 }
