 package dungeon.game;
 
 import dungeon.game.messages.DefeatEvent;
 import dungeon.load.messages.LevelLoadedEvent;
 import dungeon.messages.Mailman;
 import dungeon.messages.Message;
 import dungeon.messages.MessageHandler;
 import dungeon.models.Enemy;
 import dungeon.models.Player;
 import dungeon.models.Tile;
 import dungeon.models.World;
 import dungeon.models.messages.IdentityTransform;
 import dungeon.models.messages.Transform;
 import dungeon.ui.events.MoveCommand;
 
 /**
  * Hier wird die eigentliche Logik des Spiels durchgeführt.
  */
 public class LogicHandler implements MessageHandler {
   private static final int SPEED = 100;
 
   private final Mailman mailman;
 
   private World world;
 
   public LogicHandler (Mailman mailman) {
     this.mailman = mailman;
   }
 
   @Override
   public void handleMessage (Message message) {
     if (message instanceof LevelLoadedEvent) {
       this.world = ((LevelLoadedEvent)message).getWorld();
     } else if (message instanceof MoveCommand) {
       move((MoveCommand)message);
     }
   }
 
   private void move (MoveCommand command) {
     Transform movementTransform = handleMovement(command);
     movementTransform = filterWalls(movementTransform);
     movementTransform = filterBorders(movementTransform);
 
     this.world = this.world.apply(movementTransform);
     this.mailman.send(movementTransform);
 
     Transform enemyTransform = handleEnemies();
     this.world = this.world.apply(enemyTransform);
     this.mailman.send(enemyTransform);
 
     handleDefeat();
   }
 
   private Transform handleMovement (MoveCommand command) {
     switch (command) {
       case UP:
         return new Player.MoveTransform(0, -SPEED);
       case DOWN:
         return new Player.MoveTransform(0, SPEED);
       case LEFT:
         return new Player.MoveTransform(-SPEED, 0);
       case RIGHT:
         return new Player.MoveTransform(SPEED, 0);
       default:
     }
 
     return new IdentityTransform();
   }
 
   private Transform handleEnemies () {
     for (Enemy enemy : this.world.getCurrentRoom().getEnemies()) {
       if (this.world.getPlayer().touches(enemy)) {
         return new Player.HitpointTransform(-1);
       }
     }
 
     return new IdentityTransform();
   }
 
   private Transform filterWalls (Transform transform) {
     Player movedPlayer = this.world.getPlayer().apply(transform);
 
     for (Tile tile : this.world.getCurrentRoom().getTiles()) {
       if (tile.isBlocking()) {
         if (movedPlayer.touches(tile)) {
           return new IdentityTransform();
         }
       }
     }
 
     return transform;
   }
 
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
 
  private void handleDefeat () {
     if (this.world.getPlayer().getHitPoints() == 0) {
       this.mailman.send(new DefeatEvent());
     }
   }
 }
