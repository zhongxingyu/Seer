 package dungeon;
 
 import dungeon.messages.Message;
 import dungeon.messages.MessageHandler;
 import dungeon.messages.Mailman;
 import dungeon.models.Player;
 import dungeon.models.Tile;
 import dungeon.models.World;
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
     Player.MoveTransform transform;
 
     switch (command) {
       case UP:
         if (this.world.getPlayer().getPosition().getY() - SPEED < 0) {
           return;
         }
         transform = new Player.MoveTransform(0, -SPEED);
         break;
       case DOWN:
         if (this.world.getPlayer().getPosition().getY() + 1 + SPEED > this.world.getCurrentRoom().getSize()) {
           return;
         }



         transform = new Player.MoveTransform(0, SPEED);
         break;
       case LEFT:
         if (this.world.getPlayer().getPosition().getX() - SPEED < 0) {
           return;
         }
         transform = new Player.MoveTransform(-SPEED, 0);
         break;
       case RIGHT:
         if (this.world.getPlayer().getPosition().getX() + 1 + SPEED > this.world.getCurrentRoom().getSize()) {
           return;
         }
         transform = new Player.MoveTransform(SPEED, 0);
         break;
       default:
         return;
     }
 
     this.world = this.world.apply(transform);
 
     this.mailman.send(transform);
   }
 }
