 package de.htwg.se.dog.controller.impl;
 
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 
 import de.htwg.se.dog.controller.GameTableInterface;
 import de.htwg.se.dog.models.CardInterface;
 import de.htwg.se.dog.models.FieldInterface;
 import de.htwg.se.dog.models.GameFieldInterface;
 import de.htwg.se.dog.models.PlayerInterface;
 import de.htwg.se.dog.models.impl.GameField;
 import de.htwg.se.dog.models.impl.Player;
 import de.htwg.se.dog.util.Observable;
 
 public class GameTable extends Observable implements GameTableInterface {
 
     private static final int FIELDSTILLHOUSE = 16;
     private static final int HOUSECOUNT = 4;
 
     private final GameField game;
     private final List<PlayerInterface> players;
     private Queue<PlayerInterface> turnPlayer;
     private final CardDealer dealer;
     private final Movement movement;
     private PlayerInterface currentPlayer;
 
     /**
      * Constructor to generate a new gametable
      * 
      * @param playerCount
     *        number of players
      * @param figCount
     *        number of figures per player
      */
     public GameTable(int playerCount) {
         game = new GameField(FIELDSTILLHOUSE, playerCount, HOUSECOUNT);
         players = new LinkedList<PlayerInterface>();
         dealer = new CardDealer(playerCount);
         movement = new Movement(game);
         addPlayers(playerCount, HOUSECOUNT);
     }
 
     /**
      * Returns the Gamefield currently playing on
      * 
      * @return
      */
     @Override
     public GameFieldInterface getGameField() {
         return this.game;
     }
 
     /**
      * Returns weather the playerqueue is empty or not
      * 
      * @return true if empty otherwise false
      */
     @Override
     public boolean playerQueueIsEmpty() {
         return turnPlayer.isEmpty();
     }
 
     /**
      * Add all Players to playerlist
      * 
      * @param playerCount
      * @param figCount
      */
     private void addPlayers(int playerCount, int figCount) {
         for (int i = 1; i <= playerCount; i++) {
             players.add(new Player(i, figCount));
         }
     }
 
     /**
      * Sets the player that can play first
      * 
      * @param playernum
      */
     @Override
     public void setStartingPlayer(int playernum) {
         Collections.rotate(players, -playernum + 1);
     }
 
     /**
      * Refills the Playerqueue and deals cards to every player
      */
     @Override
     public void dealCards() {
 
         //now put all players into the queue
         turnPlayer = new LinkedList<PlayerInterface>();
         for (PlayerInterface p : players) {
             dealer.dealCards(p);
             turnPlayer.add(p);
 
         }
         //rotate player list clockwise
         Collections.rotate(players, -1);
     }
 
     @Override
     public void newRound() {
         dealCards();
         dealer.newRound();
     }
 
     @Override
     public void nextPlayer() {
         PlayerInterface temp;
         do {
             if (turnPlayer.isEmpty()) {
                 //TODO update neue runde print
                 newRound();
             }
            System.out.println("turnPlayerSize: " + turnPlayer.size());
            System.out.println("Liste: " + turnPlayer);
             temp = turnPlayer.poll();
             if (!canPlay(temp)) {
                 temp = null;
             }
             currentPlayer = temp;
         } while (currentPlayer == null);
        System.out.println("ListeNachPoll: " + turnPlayer);
     }
 
     /**
      * Returns true if the Player has a card that can be played
      * 
      * @param p
     *        the Player that wants to play
      * @return true if he can play, otherwise false
      */
     @Override
     public boolean canPlay(PlayerInterface p) {
         boolean retval = false;
         if (!possibleCards(p).isEmpty()) {
             retval = true;
             turnPlayer.offer(currentPlayer);
         }
         return retval;
     }
 
     @Override
     public boolean playerHasCard(int cardval) {
         boolean retval = false;
         for (CardInterface c : possibleCards(currentPlayer)) {
             if (c.getValue() == cardval) {
                 retval = true;
             }
         }
         return retval;
     }
 
     /**
      * Returns a list containing the cards that can be played by Player p
      * 
      * @param p
     *        the player that wants to play
      * @return a list containing the cards that can be played
      */
     // TODO: implement moveStart as possible playable Card
     @Override
     public List<CardInterface> possibleCards(PlayerInterface p) {
         List<CardInterface> cards = new LinkedList<CardInterface>(p.getCardList());
         Iterator<CardInterface> it = cards.iterator();
         while (it.hasNext()) {
             CardInterface c = it.next();
             //Put new Figure on field
             boolean validMoveStartCard = (c.getValue() == 1 || c.getValue() == 14 || c.getValue() == 13);
             if (!game.getField()[movement.getPlayerStart(p)].isBlocked() && !p.getFigureList().isEmpty() && validMoveStartCard) {
                 continue;
             }
 
             //move figure on field
             for (Integer field : p.getFigureRegister()) {
                 movement.setMoveStrategie(c.getValue());
                 if (movement.validMove(c.getValue(), field)) {
                     continue;
                 }
             }
             it.remove();
         }
         return cards;
     }
 
     @Override
     public boolean playerHaswon(GameFieldInterface gamefield, PlayerInterface player) {
         boolean retval = false;
         FieldInterface[] array = gamefield.getField();
         if (player.getFigureRegister().size() == gamefield.getHouseCount()) {
             for (Integer fieldID : player.getFigureRegister()) {
                 if (!array[fieldID].isHouse()) {
                     retval = false;
                     break;
                 }
                 retval = true;
             }
         }
         return retval;
     }
 
     @Override
     public int getFigureOwnerID(int fieldnr) {
         return game.getFieldForNum(fieldnr).getFigureOwnerNr();
     }
 
     @Override
     public boolean fieldIsEmpty(int fieldnr) {
         return movement.fieldEmpty(game.getFieldForNum(fieldnr));
     }
 
     @Override
     public PlayerInterface getCurrentPlayer() {
         return currentPlayer;
     }
 
     @Override
     public String getGameFieldString() {
         return game.toString();
     }
 
     @Override
     public String getPlayerString() {
         return currentPlayer.toString();
     }
 
     @Override
     public String getPlayerHandString() {
         return currentPlayer.printCardsOnHand();
     }
 
     @Override
     public void playCard(int steps, int fieldNr) {
         // TODO Auto-generated method stub
 
     }
 
    @Override
     public boolean moveFigureToStart() {
         boolean retval = false;
         if (movement.moveStart(currentPlayer)) {
             retval = true;
         }
         return retval;
     }
 
 }
