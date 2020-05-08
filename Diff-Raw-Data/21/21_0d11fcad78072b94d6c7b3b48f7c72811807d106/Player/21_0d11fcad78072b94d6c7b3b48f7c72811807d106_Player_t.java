 package models;
 
 import java.lang.reflect.Type;
 import java.util.Random;
 
 import util.IdGenerator;
 import util.RichUtil;
 
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonPrimitive;
 import com.google.gson.JsonSerializationContext;
 import com.google.gson.JsonSerializer;
 
 import exception.GameException;
 
 /**
  * Player model.
  * 
  * @author GuoLin
  *
  */
 public class Player {
 
     public Integer id;
 
     public String name;
 
     public Role role;
     
     public Cell currentCell;
     
     public Game game;
 
     public int cash;
     
     public boolean survive;
     
     public long lastActive;
     
     private static final int DEFAULT_CASH = 10008;
 
     public Player(String name) {
         this(name, Role.RANDOM);
     }
 
     public Player(String name, Role role) {
         this.id = IdGenerator.generate();
         this.name = name;
         this.role = role;
         this.cash = DEFAULT_CASH;
         this.survive = true;
     }
     
     /**
      * Random a start position for the player in the given map.
      * The cell id would be set to the value of the cell.
      * @param map The given map.
      */
     public void randomStart(GameMap map) {
         currentCell = map.randomStartCell();
     }
     
     /**
      * Roll the dice and return the value of the dice.
      * A {@link DiceEvent} would be generated in the process.
      * @return The value of the dice.
      */
     public int roll() {
         DiceEvent diceEvent = new DiceEvent();
         game.events.publish(diceEvent);
         return diceEvent.value;
     }
     
     /**
      * The player move for the given steps.
      * {@link GameException} would be thrown in the following situation(s):
      *     One of the cell on the road cannot be stepped in.
      * A {@link MoveEvent} would be generated in the process.
      * @param step The step to move.
      */
     public void move(int step) {
         while (step-- > 0) {
             currentCell = currentCell.next;
             if (currentCell == null) {
                 throw new GameException("Cannot move to the target cell");
             }
         }
         MoveEvent moveEvent = new MoveEvent(step);
         game.events.publish(moveEvent);
         this.pass();
     }
     
     /**
      * The logic of a player to buy the cell where he currently stands on.
      * {@link GameException} would be thrown in the following situations:
      *     The current cell is cannot be bought.
      *     The player do not have enough cash to afford the cell.
      * A {@link CashChangeEvent} would be generated in the process.
      */
     public void buyCell() {
         if (currentCell == null || !currentCell.canBuy()) {
             throw new GameException("Not a estate cell, cannot buy it");
         }
         EstateCell estateCell = (EstateCell)currentCell;
         if (!canAfford(estateCell)) {
             throw new GameException("Not enough money to buy this cell");
         }
         this.cash -= estateCell.price;
         game.events.publish(new CashChangeEvent(-estateCell.price, this.name));
         estateCell.changeOwner(this);
         game.changeCellOwner(this.name, currentCell);
     }
     
     /**
      * The player passed the cell which belongs to other player.
      * The cash of current player would be deducted by the price of the cell.
      * The cash of the owner would be added by the same amount of money.
      * You could know that the transaction is free of tax.
      * Two events of {@link CashChangeEvent} would be generated to indicate the changes of the two players in cash.
      */
     public void pass() {
         if (currentCell == null) {
             throw new GameException("Stepped onto a null cell.");
         }
         if (currentCell instanceof EmptyCell) {
             game.nextPlayer();
         }
         if (currentCell instanceof EstateCell) {
            EstateCell estateCell = (EstateCell)currentCell;
            if (!(this.equals(estateCell.owner)) && estateCell.owner != null) {
                 int offer = this.cash >= estateCell.price ? estateCell.price: this.cash;
                 game.events.publish(new CashChangeEvent(-offer, this.name));
                 estateCell.owner.cash += estateCell.price;
                 game.events.publish(new CashChangeEvent(offer, estateCell.owner.name));
                 this.cash = this.cash - estateCell.price;
                 if (this.cash <= 0) {
                     this.backrupt();
                 }
             }
             game.nextPlayer();
         }
     }
     
     public void recordLastActive() {
         this.lastActive = System.currentTimeMillis();
     }
     
     public boolean canAfford(EstateCell estateCell) {
         return cash >= estateCell.price;
     }
     
     public void backrupt() {
         this.survive = false;
         game.events.publish(new BackruptEvent(name));
         game.gameMap.clearOwner(this);
     }
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((name == null) ? 0 : name.hashCode());
         return result;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         Player other = (Player) obj;
         if (name == null) {
             if (other.name != null) {
                 return false;
             }
         } else if (!name.equals(other.name)) {
             return false;
         }
         return true;
     }
     
     public static class DiceEvent extends Event {
         
         public int value;
         
         public DiceEvent() {
             this.value = RichUtil.randomDice();
         }
         
     }
     
     public static class MoveEvent extends Event {
         
         public final int step;
         
         public MoveEvent(int step) {
             this.step = step;
         }
         
     }
     
     public static class CashChangeEvent extends Event {
         
         public final int cashChange;
         
         public final String playerName;
         
         public CashChangeEvent(int cashChange, String playerName) {
             this.cashChange = cashChange;
             this.playerName = playerName;
         }
         
     }
     
     public static class BackruptEvent extends Event {
         
         public final String playerName;
         
         public BackruptEvent(String playerName) {
             this.playerName = playerName;
         }
     }
     
     public static class Serializer implements JsonSerializer<Player> {
 
         @Override
         public JsonElement serialize(Player src, Type type,
                 JsonSerializationContext ctx) {
             JsonObject playerObject = new JsonObject();
             playerObject.add("id", new JsonPrimitive(src.id));
             playerObject.add("name", new JsonPrimitive(src.name));
             playerObject.add("role", ctx.serialize(src.role));
             playerObject.add("currentCellId", new JsonPrimitive(src.currentCell.id));
             playerObject.add("cash", new JsonPrimitive(src.cash));
             return playerObject;
         }
     }
     
 }
