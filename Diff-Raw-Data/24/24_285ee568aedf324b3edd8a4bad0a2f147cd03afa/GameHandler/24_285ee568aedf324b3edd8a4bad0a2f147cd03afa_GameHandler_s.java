 package dungeon;
 
 import dungeon.messages.Message;
 import dungeon.messages.MessageHandler;
 import dungeon.messages.Mailman;
 import dungeon.models.Player;
 import dungeon.models.World;
 import dungeon.ui.events.MoveCommand;
 
 /**
  * Hier wird die eigentliche Logik des Spiels durchgeführt.
  */
 public class GameHandler implements MessageHandler {
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
        transform = new Player.MoveTransform(0, -0.1f);
         break;
       case DOWN:
        transform = new Player.MoveTransform(0, 0.1f);
         break;
       case LEFT:
        transform = new Player.MoveTransform(-0.1f, 0);
         break;
       case RIGHT:
        transform = new Player.MoveTransform(0.1f, 0);
         break;
       default:
         return;
     }
 
     this.world = this.world.apply(transform);
 
    mailman.send(transform);
   }
 }
