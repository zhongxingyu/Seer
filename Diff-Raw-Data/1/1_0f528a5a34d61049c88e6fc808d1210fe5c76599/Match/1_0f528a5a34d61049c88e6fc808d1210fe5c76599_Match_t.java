 /*
  * Copyright 2012 Cameron Zemek <grom358@gmail.com>.
  */
 package pokeraichallenge;
 
 import java.security.SecureRandom;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Random;
 import poker.Card;
 import poker.CardList;
 import poker.CardSet;
 import poker.Hand;
 
 /**
  *
  * @author Cameron Zemek <grom358@gmail.com>
  */
 public class Match {
     private Settings settings;
     private List<Player> players;
     private int roundNo;
     private int smallBlind;
     private int bigBlind;
     private int pot; // Called bets go into the pot
     private int sidepot; // The uncalled bet
     private int minRaise;
     private int onButton, onTurn;
     private CardList board;
     private Random random = new SecureRandom();
 
     public Match(Settings settings, List<Player> players) {
         this.settings = settings;
         this.players = players;
         this.roundNo = 0;
         this.smallBlind = 10;
         this.bigBlind = 20;
         this.onButton = 0;
     }
 
     private void debug(String message) {
         System.out.println(message);
     }
 
     /**
      * Play match and return the winner
      */
     public Player play() {
         // Tell players about the settings
         printAll("Settings gameType " + settings.getGameType());
         printAll("Settings gameMode " + settings.getGameMode());
         printAll("Settings timeBank " + settings.getTimeBank());
         printAll("Settings timePerMove " + settings.getTimePerMove());
         printAll("Settings handsPerLevel " + settings.getHandsPerLevel());
         for (Player player : players) {
             player.println("Settings yourBot " + player.getName());
         }
 
         // Print set positions
         printAll(players.get(0).getName() + " seat 0");
         printAll(players.get(1).getName() + " seat 1");
 
         for (;;) {
             if (players.get(0).getStack() == 0) {
                 return players.get(1);
             }
             if (players.get(1).getStack() == 0) {
                 return players.get(0);
             }
             playRound();
 
             roundNo++;
             if (roundNo % settings.getHandsPerLevel() == 0) {
                 increaseBlinds();
             }
 
             // Move button
             onButton = (onButton + 1) % players.size();
         }
     }
 
     /**
      * Play a single hand
      */
     private void playRound() {
         printAll("Match round " + (roundNo + 1));
         printAll("Match smallBlind " + smallBlind);
         printAll("Match bigBlind " + bigBlind);
         printAll("Match onButton " + players.get(onButton).getName());
 
         // Print out stack information
         for (Player player : players) {
             debug(player.getName() + " stack " + player.getStack());
         }
 
         // Post the blinds
         Player small = players.get(onButton);
         Player big = players.get((onButton + 1) % players.size());
         // Since we are playing heads up, if a blind puts a player all in
         // then the amount that puts a player all in becomes the required blind
         int requiredSmall = Math.min(smallBlind, big.getStack());
         int requiredBig = Math.min(bigBlind, small.getStack());
         int postedSmall = post(small, requiredSmall);
         int postedBig = post(big, requiredBig);
         pot = postedSmall << 1;
         sidepot = postedBig - postedSmall;
 
         // Shuffle deck
         List<Card> deck = Card.newDeck();
         Collections.shuffle(deck, random);
 
         // Deal starting hands
         for (Player player : players) {
             CardList hand = new CardList(2);
             hand.add(deck.remove(deck.size() - 1));
             hand.add(deck.remove(deck.size() - 1));
             player.setCards(hand);
             player.println(player.getName() + " hand " + hand);
             debug(player.getName() + " hand " + hand);
         }
 
         board = new CardList(5);
 
         // Pre-flop
         minRaise = bigBlind;
         onTurn = (onButton + 2) % players.size();
         if (bettingRound()) {
             // Flop
             onTurn = (onButton + 1) % players.size();
             board.add(deck.remove(deck.size() - 1));
             board.add(deck.remove(deck.size() - 1));
             board.add(deck.remove(deck.size() - 1));
             printAll("Match table " + board);
             if (bettingRound()) {
                 // Turn
                 onTurn = (onButton + 1) % players.size();
                 board.add(deck.remove(deck.size() - 1));
                 printAll("Match table " + board);
                 if (bettingRound()) {
                     // River
                     onTurn = (onButton + 1) % players.size();
                     board.add(deck.remove(deck.size() - 1));
                     printAll("Match table " + board);
                     if (bettingRound()) {
                         showdown();
                     }
                 }
             }
         }
     }
 
     private void increaseBlinds() {
         if (smallBlind >= 1000) {
             // Maximum small blind
         } else if (smallBlind >= 500) {
             smallBlind += 250;
         } else if (smallBlind >= 200) {
             smallBlind += 100;
         } else if (smallBlind >= 100) {
             smallBlind += 50;
         } else if (smallBlind >= 50) {
             smallBlind += 25;
         } else if (smallBlind >= 30) {
             smallBlind += 20;
         } else {
             smallBlind += 10;
         }
         bigBlind = smallBlind * 2;
     }
 
     private boolean bettingRound() {
         // Skip betting if a player is all-in
         if (players.get(0).isAllIn() || players.get(1).isAllIn()) {
             return true;
         }
 
         // Print stack information
         for (Player player : players) {
             debug(player.getName() + " stack " + player.getStack());
         }
 
         // Print pot information
         debug("Match pot " + pot);
         debug("Match sidepot [" + sidepot + "]");
 
         int playersToAct = 2;
         while (playersToAct > 0) {
             Move move = bet();
             if (move.getAction() == Move.Action.FOLD) {
                 // The remaining bot wins
                 int prize = pot + sidepot;
                 Player winner = players.get(onTurn);
                 winner.giveChips(prize);
                 printAll(winner.getName() + " wins " + prize);
                 return false;
             } else if (move.getAction() == Move.Action.RAISE) {
                 playersToAct = players.size() - 1;
             } else {
                 playersToAct--;
             }
         }
         return true;
     }
 
     private Move bet() {
         Player player = players.get(onTurn);
         Player opponent = players.get((onTurn + 1) % players.size());
 
         Move move = requestMove(player);
 
         // Change check to fold if there is a current bet
         if (sidepot > 0 && move.getAction() == Move.Action.CHECK) {
             move = Move.FOLD;
         }
 
         // Change call to check if no current bet
         if (sidepot == 0 && move.getAction() == Move.Action.CALL) {
             move = Move.CHECK;
         }
 
         if (move.getAction() == Move.Action.RAISE) {
             // If opponent is all in, change to call
             if (opponent.isAllIn()) {
                 move = Move.CALL;
             } else if (player.getStack() < minRaise) {
                 move = Move.CALL;
             }
         }
 
         // Handle moves
         int amount = move.getAmount();
         if (move.getAction() == Move.Action.CALL) {
             amount = Math.min(sidepot, player.getStack());
             amount = player.takeChips(amount);
             pot += amount * 2;
             sidepot -= amount;
         } else if (move.getAction() == Move.Action.RAISE) {
             pot += sidepot + player.takeChips(sidepot); // Call existing bet
             amount = Math.max(minRaise, amount); // Must raise by minimum
             amount = Math.min(amount, player.getStack()); // Cap raise to players stack
            amount = Math.min(amount, opponent.getStack()); // Cap raise to opponents stack
             amount = player.takeChips(amount);
             sidepot = amount;
             minRaise = amount;
         }
 
         printAll(player.getName() + " " + move.getAction().toString().toLowerCase() + " " + amount);
 
         onTurn = (onTurn + 1) % players.size();
         return move;
     }
 
     private void showdown() {
         for (Player player : players) {
             printAll(player.getName() + " hand " + player.getCards());
         }
 
         int p1, p2;
         Hand h1, h2;
 
         List<Card> hand = new ArrayList<>(board);
         hand.addAll(players.get(0).getCards());
         h1 = Hand.eval(new CardSet(hand));
         p1 = h1.getValue();
 
         debug(players.get(0).getName() + " has " + h1);
 
         hand = new ArrayList<>(board);
         hand.addAll(players.get(1).getCards());
         h2 = Hand.eval(new CardSet(hand));
         p2 = h2.getValue();
 
         debug(players.get(1).getName() + " has " + h2);
 
         int prize = pot + sidepot;
         if (p1 == p2) {
             // Split pot
             prize = prize >> 1;
             int leftOver = (pot + sidepot) - (prize << 1);
             int leftPos = (onButton + 1) % players.size();
             for (int i = 0, n = players.size(); i < n; ++i) {
                 int p = prize;
                 if (i == leftPos) {
                     // Left of the dealer gets the left over chips
                     p += leftOver;
                 }
                 players.get(i).giveChips(prize);
                 printAll(players.get(i).getName() + " wins " + prize);
             }
         } else if (p1 < p2) {
             Player winner = players.get(1);
             winner.giveChips(prize);
             printAll(winner.getName() + " wins " + prize);
         } else {
             Player winner = players.get(0);
             winner.giveChips(prize);
             printAll(winner.getName() + " wins " + prize);
         }
     }
 
     private int post(Player player, int amount) {
         amount = player.takeChips(amount);
         printAll(player.getName() + " post " + amount);
         return amount;
     }
 
     /**
      * Print a message to all bots
      */
     private void printAll(String message) {
         for (Player player : players) {
             player.println(message);
         }
         debug(message);
     }
 
     /**
      * Request an action from the bot
      */
     private Move requestMove(Player player) {
         // Print stack information
         for (Player p : players) {
             player.println(p.getName() + " stack " + p.getStack());
         }
         player.println("Match pot " + pot);
         player.println("Match sidepots [" + sidepot + "]");
         String line = player.go();
         try {
             return Move.valueOf(line);
         } catch (IllegalArgumentException e) {
             return Move.FOLD;
         }
     }
 }
