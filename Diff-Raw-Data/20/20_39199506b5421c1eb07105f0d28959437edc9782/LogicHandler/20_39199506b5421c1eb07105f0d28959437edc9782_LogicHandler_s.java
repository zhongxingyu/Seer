 package dungeon.game;
 
 import dungeon.game.messages.*;
 import dungeon.messages.LifecycleEvent;
 import dungeon.messages.Mailman;
 import dungeon.messages.Message;
 import dungeon.messages.MessageHandler;
 import dungeon.models.Player;
 import dungeon.models.World;
 import dungeon.pulse.Pulse;
 import dungeon.ui.messages.*;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 /**
  * Handles the game logic.
  */
 public class LogicHandler implements MessageHandler {
   private static final Logger LOGGER = Logger.getLogger(LogicHandler.class.getName());
 
   private static final double MS_PER_SECOND = 1000;
 
   private final Mailman mailman;
 
   /**
    * Time when the last pulse came in in milliseconds.
    */
   private long lastPulse;
 
   /**
    * Time delta since the last pulse came in in milliseconds.
    */
   private int pulseDelta;
 
   private GameLogic logic;
 
   /**
    * Which players are ready?
    */
   private Map<Integer, Boolean> playersReady = new HashMap<>();
 
   public LogicHandler (Mailman mailman, World world) {
     this.mailman = mailman;
     this.logic = new GameLogic(world);
   }
 
   /**
    * Returns the current state of the world.
    */
   public World getWorld () {
     return this.logic.getWorld();
   }
 
   /**
    * Replaces the world.
    *
    * This only happens when someone is loading a save file.
    */
   public void setWorld (World world) {
     this.playersReady = new HashMap<>();
 
     for (Player player : world.getPlayers()) {
       this.playersReady.put(player.getId(), false);
     }
 
     this.logic.setWorld(world);
   }
 
   public int nextId () {
     return this.logic.nextId();
   }
 
   @Override
   public void handleMessage (Message message) {
     if (message instanceof Pulse) {
       this.pulse();
     } else if (message instanceof PlayerMessage) {
       int playerId = ((PlayerMessage)message).getPlayerId();
 
       if (message instanceof StartCommand) {
         this.startCommand((StartCommand)message);
       } else if (message instanceof EndCommand) {
         this.endCommand((EndCommand)message);
       } else if (message instanceof UseItemCommand) {
         this.logic.useItem(playerId, ((UseItemCommand)message).getItem());
       } else if (message instanceof EquipWeaponCommand) {
         this.logic.equipWeapon(playerId, ((EquipWeaponCommand)message).getWeapon());
       } else if (message instanceof BuyCommand) {
         this.logic.buyItem(playerId, ((BuyCommand)message).getMerchant(), ((BuyCommand)message).getItem());
       } else if (message instanceof SellCommand) {
         this.logic.sellItem(playerId, ((SellCommand)message).getMerchant(), ((SellCommand)message).getItem());
       }
     } else if (message instanceof PlayerJoinCommand) {
       Player player = ((PlayerJoinCommand)message).getPlayer();
 
       this.logic.addPlayer(player);
       this.playersReady.put(player.getId(), false);
     } else if (message instanceof PlayerLeaveCommand) {
       int playerId = ((PlayerLeaveCommand)message).getPlayerId();
 
       this.logic.removePlayer(playerId);
       this.playersReady.remove(playerId);
     } else if (message instanceof PlayerReadyCommand) {
       this.setPlayerReady(((PlayerReadyCommand)message).getPlayerId());
     } else if (message == LifecycleEvent.INITIALIZE) {
       // Initialize the pulse delta. If you don't the first pulse will be from the beginning of the unix epoch until today.
       this.updatePulseDelta();
     }
   }
 
   private void startCommand (StartCommand message) {
     if (this.logic == null) {
       return;
     }
 
     int playerId = message.getPlayerId();
     Command command = message.getCommand();
 
     if (command instanceof MoveCommand) {
       this.logic.activateMoveDirection(playerId, (MoveCommand)command);
     } else if (command instanceof AttackCommand) {
       this.logic.activateAttack(playerId);
     } else if (command instanceof IceBoltAttackCommand) {
       this.logic.activateIceBolt(playerId);
     } else if (command instanceof HealthPotionCommand) {
       this.logic.useHealthPotion(playerId);
     } else if (command instanceof ManaPotionCommand) {
       this.logic.useManaPotion(playerId);
     } else if (command instanceof InteractCommand) {
       this.logic.interact(playerId);
     }
   }
 
   private void endCommand (EndCommand message) {
     if (this.logic == null) {
       return;
     }
 
     int playerId = message.getPlayerId();
     Command command = message.getCommand();
 
     if (command instanceof MoveCommand) {
       this.logic.deactivateMoveDirection(playerId, (MoveCommand)command);
     } else if (command instanceof AttackCommand) {
       this.logic.deactivateAttack(playerId);
     } else if (command instanceof IceBoltAttackCommand) {
       this.logic.deactivateIceBoltAttack(playerId);
     }
   }
 
   /**
    * Apply all the changes to the world, that have happened since the last pulse.
    */
   private void pulse () {
     if (this.logic == null) {
       return;
     }
 
     this.updatePulseDelta();
 
     Transaction transaction = this.logic.pulse(this.getPulseDelta());
 
     for (Message message : transaction.getMessages()) {
       this.mailman.send(message);
     }
 
     switch (this.logic.getGameState()) {
       case VICTORY:
         this.mailman.send(new WinEvent());
 
         this.reset();
         break;
       case DEFEAT:
         this.mailman.send(new DefeatEvent());
 
         this.reset();
         break;
       default:
     }
   }
 
   /**
    * Reset this handler.
    *
    * This is necessary, because otherwise the handler would continually send win or defeat events.
    */
   private void reset () {
     this.logic = null;
     this.lastPulse = 0;
     this.pulseDelta = 0;
   }
 
   /**
    * Recalculate the time since the last pulse.
    */
   private void updatePulseDelta () {
     long now = System.currentTimeMillis();
 
     // If there are more than 2^32 seconds between two pulses, we are already extinct.
     this.pulseDelta = (int)(now - this.lastPulse);
     this.lastPulse = now;
   }
 
   /**
    * @return the time since the last pulse came in in milliseconds
    */
   private double getPulseDelta () {
     return this.pulseDelta / MS_PER_SECOND;
   }
 
   /**
    * Sets the player status to ready and starts the game when all players are ready.
    */
   private void setPlayerReady (int playerId) {
     this.playersReady.put(playerId, true);
 
     boolean allReady = true;
 
     for (Boolean readyStatus : this.playersReady.values()) {
       if (!readyStatus) {
         allReady = false;
       }
     }
 
     if (allReady) {
       LOGGER.info("All players ready -> start game");
 
       this.mailman.send(new StartGame());
     }
   }
 }
