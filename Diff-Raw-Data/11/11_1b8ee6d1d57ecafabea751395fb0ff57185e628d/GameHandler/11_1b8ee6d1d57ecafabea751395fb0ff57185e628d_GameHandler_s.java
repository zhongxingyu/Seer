 package dungeon;
 
 import dungeon.messages.Mailman;
 import dungeon.messages.Message;
 import dungeon.messages.MessageHandler;
 import dungeon.models.Enemy;
 import dungeon.models.Player;
 import dungeon.models.World;
 import dungeon.models.messages.IdentityTransform;
 import dungeon.models.messages.Transform;
 import dungeon.ui.events.MoveCommand;
 
 /**
  * Hier wird die eigentliche Logik des Spiels durchgeführt.
  */
 public class GameHandler implements MessageHandler {
   private static final float SPEED = 0.1f;
 
   private final Mailman mailman;
 
   private World world;
 
   public GameHandler (Mailman mailman) {
     this.mailman = mailman;
   }
 
   @Override
   public void handleMessage (Message message) {
     if (message instanceof LevelLoadHandler.LevelLoadedEvent) {
       this.world = ((LevelLoadHandler.LevelLoadedEvent) message).getWorld();
     } else if (message instanceof MoveCommand) {
       move((MoveCommand) message);
     }
   }
 
   private void move (MoveCommand command) {
     Transform movementTransform = handleMovement(command);
    Transform enemyTransform = handleEnemy(command);
 
     this.world = this.world.apply(movementTransform);
     this.world = this.world.apply(enemyTransform);
 
     this.mailman.send(movementTransform);
     this.mailman.send(enemyTransform);
   }
 
   private Transform handleMovement (MoveCommand command) {
     switch (command) {
 
       case UP:
         if (world.getPlayer().getPosition().getY() - SPEED < 0) {
           return new IdentityTransform();
         }
         else {
           return new Player.MoveTransform(0, -SPEED);
         }
       case DOWN:
         if (world.getPlayer().getPosition().getY() + 1 + SPEED > world.getCurrentRoom().getSize()) {
           return new IdentityTransform();
         }
         else {
           return new Player.MoveTransform(0, SPEED);
         }
       case LEFT:
         if (world.getPlayer().getPosition().getX() - SPEED < 0) {
           return new IdentityTransform();
         }
         else {
           return new Player.MoveTransform(-SPEED, 0);
         }
       case RIGHT:
         if (world.getPlayer().getPosition().getX() + 1 + SPEED > world.getCurrentRoom().getSize()) {
           return new IdentityTransform();
         }
         else {
           return new Player.MoveTransform(SPEED, 0);
         }
       default:
     }
     return new IdentityTransform();
   }
 
  private Transform handleEnemy (MoveCommand command) {
     switch (command) {
 
       case UP:
         for (Enemy enemy : world.getCurrentRoom().getEnemies()) {
           if (world.getPlayer().getPosition().getY() == enemy.getPosition().getY() + 1 - SPEED ) {
             return new Player.HitpointTransform(-1);
           }
         }
         break;
       case DOWN:
         for (Enemy enemy : world.getCurrentRoom().getEnemies()) {
           if (world.getPlayer().getPosition().getY() == enemy.getPosition().getY() + SPEED ) {
             return new Player.HitpointTransform(-1);
           }
         }
         break;
       case LEFT:
         for (Enemy enemy : world.getCurrentRoom().getEnemies()) {
           if (world.getPlayer().getPosition().getX() == enemy.getPosition().getX() + 1 - SPEED ) {
             return new Player.HitpointTransform(-1);
           }
         }
         break;
       case RIGHT:
         for (Enemy enemy : world.getCurrentRoom().getEnemies()) {
           if (world.getPlayer().getPosition().getX() == enemy.getPosition().getX() + SPEED ) {
             return new Player.HitpointTransform(-1);
           }
         }
         break;
       default:
     }
     return new IdentityTransform();
   }


}
