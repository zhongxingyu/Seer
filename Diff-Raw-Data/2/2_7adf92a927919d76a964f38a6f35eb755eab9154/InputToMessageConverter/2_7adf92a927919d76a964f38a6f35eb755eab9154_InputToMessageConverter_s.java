 package dungeon.ui.screens;
 
 import dungeon.messages.Mailman;
 import dungeon.ui.messages.*;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.concurrent.atomic.AtomicReference;
 
 /**
  * This converts input events like key presses in internal messages that can then be interpreted by the other modules.
  *
  * WARNING: For this class to be thread safe, handleMessage() has to be called on the EDT.
  */
 public class InputToMessageConverter implements KeyListener {
   private final Mailman mailman;
 
   private final AtomicReference<Integer> localPlayerId;
 
   public InputToMessageConverter (Mailman mailman, AtomicReference<Integer> localPlayerId) {
     this.mailman = mailman;
     this.localPlayerId = localPlayerId;
   }
 
   @Override
   public void keyTyped (KeyEvent keyEvent) {
 
   }
 
   @Override
   public void keyPressed (KeyEvent keyEvent) {
     Command command = this.commandForKey(keyEvent.getKeyChar());
 
     if (command != null) {
       this.mailman.send(new StartCommand(this.localPlayerId.get(), command));
     }  else if (keyEvent.getKeyChar() == 'i') {
       this.mailman.send(new ShowInventory(this.localPlayerId.get()));
     }
   }
 
   @Override
   public void keyReleased (KeyEvent keyEvent) {
     Command command = this.commandForKey(keyEvent.getKeyChar());
 
     if (command != null) {
       this.mailman.send(new EndCommand(this.localPlayerId.get(), command));
     }
   }
 
   /**
   * @return The command that is associated with the #key.
    */
   private Command commandForKey (Character key) {
     switch (key) {
       case 'w':
         return MoveCommand.UP;
       case 'a':
         return MoveCommand.LEFT;
       case 's':
         return MoveCommand.DOWN;
       case 'd':
         return MoveCommand.RIGHT;
       case 'j':
         return new AttackCommand();
       case 'k':
         return new IceBoltAttackCommand();
       case 'h':
         return new HealthPotionCommand();
       case 'm':
         return new ManaPotionCommand();
       case 'z':
         return new InteractCommand();
       default:
         return null;
     }
   }
 }
