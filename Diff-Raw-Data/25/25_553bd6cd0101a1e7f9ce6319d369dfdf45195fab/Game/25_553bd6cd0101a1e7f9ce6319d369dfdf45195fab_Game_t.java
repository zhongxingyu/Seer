 package org.joedog.pinochle.game;
 
 import org.joedog.pinochle.control.*;
 import org.joedog.pinochle.model.*;
 import org.joedog.pinochle.game.*;
 import org.joedog.pinochle.player.*;
 import org.joedog.pinochle.view.*;
 import org.joedog.pinochle.view.actions.*;
 import org.joedog.pinochle.util.*;
 
 import java.util.Random;
 import java.util.concurrent.TimeUnit;
 import javax.swing.UIManager;
 import javax.swing.SwingUtilities;
 
 /**
  * @author Jeffrey Fulmer
  */
 public class Game {
   private GameController controller;
   private GameView       view;
   private Player[]       players;
   private PlayerFactory  factory;
 
 
   public Game(GameController controller, GameView view) {
     this.controller = controller;
     this.view       = view;
     this.players    = this.getPlayers();
   }
 
   /**
    * Creates all the players necessary to play the game; player
    * types are picked up from the game configuration.
    * <p>
    * @param  none
    * @return Player[] 
    */
   public Player [] getPlayers () {
     this.factory      = new PlayerFactoryImpl();
     Player [] players = {
       factory.getPlayer(this.controller, controller.getIntProperty("PlayerNorthType")),
       factory.getPlayer(this.controller, controller.getIntProperty("PlayerEastType")),
       factory.getPlayer(this.controller, controller.getIntProperty("PlayerSouthType")), 
       factory.getPlayer(this.controller, controller.getIntProperty("PlayerWestType"))
     };
 
     for (int i = 0; i < players.length; i++) {
       int    partner = -1;
       String name    = "default";
       switch (i) {
         case Pinochle.NORTH:
           partner = Pinochle.SOUTH;
           name    = controller.getProperty("PlayerNorthName");
           break;
         case Pinochle.EAST:
           partner = Pinochle.WEST;
           name    = controller.getProperty("PlayerEastName");
           break;
         case Pinochle.SOUTH:
           partner = Pinochle.NORTH;
           name    = controller.getProperty("PlayerSouthName");
           break;
         case Pinochle.WEST:
           partner = Pinochle.EAST;
           name    = controller.getProperty("PlayerWestName");
           break;
       }
       players[i].setup(view.getSetting(i), i, partner, name);
     }
     return players;
   }
  
   /**
    * Invoked by the GameThread this method starts and conducts
    * the flow of a pinochle game.
    * <p>
    * @param  none
    * @return void
    */ 
   public synchronized void start() {
     int x      = 0;
     int turn   = 0;
     int status = GameController.DEAL;
 
     controller.setProperty("GameWinningScore", controller.getProperty("WinningScore"));
 
     while (status != GameController.OVER) {
       status = controller.gameStatus();
       switch (status) {
         case GameController.DEAL:
           this.deal(players);
           break;
         case GameController.BID:
           this.bid(players);
           break;
         case GameController.PASS:
           this.pass(players);
           break;
         case GameController.MELD:
           this.meld(players);
           break;
         case GameController.PLAY:
           this.play(players);
           break;
         case GameController.SCORE:
           break;
         case GameController.OVER:
          if (controller.getHeadless() == true) {
            System.exit(0);
          }
           controller.pause(true);
           break;
       }
       turn++;
     }
   }
 
   /**
    * Deals the cards to all game participants
    * <p>
    * @param  Player[] 
    * @return void
    */
   private void deal(Player[] players) {
     if (controller.over()) return;
 
     Deck deck = new Deck(1);
     int turn  = controller.getIntProperty("Dealer");
     int next  = (turn < players.length-1)? turn + 1 : 0;
 
     for (int i = 0; i < players.length; i++)
       players[i].newHand();
 
     deck.shuffle();
     for (int i = 0; i < deck.count(); i++) {
       players[turn%players.length].takeCard(deck.dealCard(i));
       turn++;
     }
 
     controller.store("GameTrump",    "-1");
     controller.store("GameStatus",   ""+GameController.BID);
     controller.store("ActivePlayer", ""+next);
     controller.store("Dealer",       ""+next);
     return;
   }
 
   /**
    * Referees the bidding phase of a pinochle game
    * <p>
    * @param  Player[] 
    * @return void
    */
   public void bid(Player[] players) {
     int bid       = (controller.getIntProperty("MinimumBid") - 1);
     int turn      = 0;
     int sum       = 0;  // sum of the number of passes
     int mark      = 0;
     int [] passes = new int[players.length];
 
     for (Player player : players) {
       player.refresh();
       player.assessHand();
     }
 
     while (sum < (players.length - 1)) {
       int ret = players[turn%players.length].bid(bid);
       if (ret < bid) {
         passes[turn%players.length] = 1;
       } else {
         bid  = ret;
       }
       turn++;
       sum = 0;
       for (int i : passes) sum += i; // get the total passes
     }
 
     for (int i = 0; i < passes.length; i++) {
       if (passes[i] == 0) mark = i;
     }
     int trump = players[mark].nameTrump();
     controller.store("GameBid",    ""+bid);
     controller.store("Bidder",     ""+mark);
     controller.store("GameTrump",  ""+trump);
     controller.store("GameStatus", ""+GameController.PASS);
     return;
   }
  
   /**
    * Facilitates the pass between the parters who won the bid
    * <p>
    * @param  Player[] 
    * @return void
    */
   public void pass(Player[] players) {
     //XXX: some variations won't pass
     int bidder = controller.getIntProperty("ActivePlayer");
     Deck a     = null;
     Deck b     = null;
     switch (bidder) {
       case Pinochle.NORTH:
         a = players[Pinochle.SOUTH].passCards(false);
         players[Pinochle.NORTH].takeCards(a);
         b = players[Pinochle.NORTH].passCards(true);
         players[Pinochle.SOUTH].takeCards(b);
         break;
       case Pinochle.SOUTH:
         a = players[Pinochle.NORTH].passCards(false);
         players[Pinochle.SOUTH].takeCards(a);
         b = players[Pinochle.SOUTH].passCards(true);
         players[Pinochle.NORTH].takeCards(b);
         break;
       case Pinochle.EAST:
         a = players[Pinochle.WEST].passCards(false);
         players[Pinochle.EAST].takeCards(a);
         b = players[Pinochle.EAST].passCards(true);
         players[Pinochle.WEST].takeCards(b);
         break;
       case Pinochle.WEST:
         a = players[Pinochle.EAST].passCards(false);
         players[Pinochle.WEST].takeCards(a);
         b = players[Pinochle.WEST].passCards(true);
         players[Pinochle.EAST].takeCards(b);
         break;
     }
     controller.store("GameStatus", ""+GameController.MELD);
   }
 
   /**
    * Overseas the meld phase for all participating players
    * <p>
    * @param  Player[]
    * @return void
    */
   public void meld(Player[] players) {
     int score = 0;
     int ns    = 0;
     int ew    = 0;
     for (int i = 0; i < players.length; i++) {
       if (players[i].getType() == Player.HUMAN) {
         score = players[i].meld();
         if (i == Pinochle.NORTH || i == Pinochle.SOUTH) {
           ns += score;
         } else {
           ew += score;
         }
         players[i].refresh();
       }
     }
     for (int i = 0; i < players.length; i++) {
       if (players[i].getType() == Player.COMPUTER) {
         score = players[i].meld();
         if (i == Pinochle.NORTH || i == Pinochle.SOUTH) {
           ns += score;
         } else {
           ew += score;
         }
         players[i].refresh();
       }
     }
     controller.resetHand();
     controller.store("NSMeld", ""+ns);
     controller.store("EWMeld", ""+ew);
     controller.store("GameStatus", ""+GameController.PLAY);
     controller.pause(true);
     controller.addPlayButton();
     return;
   }
   
   /** 
    * Referees the trick taking phase of a pinochle hand.
    * <p>
    * @param  Player[]
    * @return void
    */
   public void play(Player[] players) {
     int count   = controller.getIntProperty("CardCount");
     int turn    = controller.getIntProperty("ActivePlayer");
     boolean sim = controller.getBooleanProperty("Simulation");
     int tricks  = count/players.length;
     while (controller.isPaused()) {
       try {
         if (
           players[0].getType()==Player.COMPUTER &&
           players[1].getType()==Player.COMPUTER &&
           players[2].getType()==Player.COMPUTER &&
           players[3].getType()==Player.COMPUTER ) {
           controller.pause(false);
         } else {
           Thread.sleep(500);
         }
       } catch (Exception e) {}
     }
     for (Player player : players) {
       player.clearMeld();
       player.refresh();
     }
 
     for (int i = 0; i < tricks; i++) {
       Trick trick   = new Trick(controller.getTrump());
       Card [] cards = new Card[4];
       Debug.print("Trump: "+Pinochle.suitname(controller.getTrump()));
       for (Player player : players) {
         Debug.print(player.handToString());
       }
       for (int j = 0; j < players.length; j++) {
         Card card = null;
         if (trick.isEmpty()) {
           controller.setStatus(
             players[turn%players.length].getName()+" it's your turn. You have the lead..."
           );
         } else {
           controller.setStatus(
             players[turn%players.length].getName()+" it's your turn." +
             Pinochle.suitname(trick.getLeadingSuit())+" was led."
           );
         }
         card = players[turn%players.length].playCard(trick);
         cards[j] = card;
        /**
         * Each computer player will commit this play to memory...
         */
        for (Player player : players) {
          player.remember(card);
        }
         trick.add(players[turn%players.length], card);
         switch (players[turn%players.length].getPosition()) {
           case Pinochle.NORTH:
             controller.display("NorthPlay", card);
             break;
           case Pinochle.SOUTH:
             controller.display("SouthPlay", card);
             break;
           case Pinochle.EAST:
             controller.display("EastPlay", card);
             break;
           case Pinochle.WEST:
             controller.display("WestPlay", card);
             break;
         }
         controller.setPlayable(false);
         try {
          int lo = (sim==true) ?  20 : 300;
          int hi = (sim==true) ?  80 : 700;
           Thread.sleep(randInt(lo, hi));
         } catch (Exception e) {}
         turn++;
       }
       turn = trick.winner();
       Debug.print("    TRICK: "+trick.toString());
       if (turn % 2 == 0) {
         controller.store("NSTake", ""+trick.counters());
       } else {
         controller.store("EWTake", ""+trick.counters());
       }
       controller.clear();
       controller.display("DisplayTrick", cards);
      Debug.print("");
     }
     // Award the last trick
     if (turn % 2 == 0) {
       controller.store("NSTake", "1");
     } else {
       controller.store("EWTake", "1");
     }
     controller.addScore();
    Debug.print(controller.getGameScore()+"\n");
     if (!controller.over()) {
       controller.newHand();
     }
   }
 
   /**
    * Returns a psuedo random int between min and max
    * <p>
    * @param  int    the minimum number in the range
    * @param  int    the maximum number in the range
    * @return int    a psuedo random number between min and max
    */  
   private static int randInt(int min, int max) {
     Random rand = new Random();
 
     int randomNum = rand.nextInt((max - min) + 1) + min;
     return randomNum;
   }
 }
 
