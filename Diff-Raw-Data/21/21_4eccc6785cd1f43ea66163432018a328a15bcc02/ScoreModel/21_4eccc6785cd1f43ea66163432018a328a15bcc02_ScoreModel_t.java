 package org.joedog.pinochle.model;
 
 import java.util.Properties;
 import org.joedog.pinochle.game.Pinochle;
 import org.joedog.pinochle.control.*;
 import org.joedog.pinochle.util.*;
 
 public class ScoreModel extends AbstractModel {
   private int bid      = 0;
   private int over     = 0;
   private int status   = GameController.DEAL;
   private int dealer   = Pinochle.SOUTH;
   private int trump    = Pinochle.HEARTS;
   private int active   = Pinochle.WEST;
   private int wscore   = 300;
   private Stats stats;
 
   public ScoreModel() {
     this.stats = new Stats(new String []{"NS","EW"});
   } 
 
   public ScoreModel(String[] teams) {
     this.stats = new Stats(teams);
   }
 
   /**
    * This method sets the status of the hand in terms
    * of the phase we're in, i.e., bid, meld, etc.
    * @param  status - a string interpretation of GameController.STATUS
    * @return void
    */
   public void setGameStatus(String status) {
     this.status = Integer.parseInt(status);
   }
 
   public int getGameStatus() {
     return this.status;
   }
 
   public void setDealer(String player) {
     int i = Integer.parseInt(player);
     this.dealer = i;
   }
 
   public void setGameWinningScore(String score) {
     int i = Integer.parseInt(score);
     this.wscore = i;
   }
 
   public void resetHand() {
    stats.resetHand();
     firePropertyChange(GameController.RESET, "RESET", "hand");
   }
 
   public void resetGame() {
     this.resetHand();
    this.stats.reset();
     firePropertyChange(GameController.RESET, "RESET", "game");
   }
 
   public void resetGame(String [] teams) {
     this.resetHand();
     this.stats   = new Stats(teams);
     firePropertyChange(GameController.RESET, "RESET", "game");
   }
 
   public String getDealer() {
     return ""+this.dealer;
   }
 
   public void setGameBid(String b) {
     this.bid = Integer.parseInt(b);
     firePropertyChange(GameController.HIGH_BID, "HIBID", ""+this.bid);
   }
 
   public String getGameBid() {
     return ""+this.bid;
   }
 
   public void setBidder(String player) {
     int i = Integer.parseInt(player);
     this.active = i;
     if (i % 2 == 0) 
       this.stats.setBidder("NS");
     else
       this.stats.setBidder("EW");
   }
 
   /**
    * This method sets trump for the hand in progress
    * @param  trump - a string interpretation of Pinochle.SUIT
    * @return void
    * @see    game/Pinochle.java
    */
   public void setGameTrump(String trump) {
     this.trump = Integer.parseInt(trump);
     firePropertyChange(GameController.TRUMP, "TRUMP", ""+this.trump);
   }
 
   public String getGameTrump() {
     return ""+this.trump;
   }
 
   public void setActivePlayer(String position) {
     this.active = Integer.parseInt(position);
   }
 
   public String getActivePlayer() {
     return ""+this.active;
   }
 
   public void setMeld(String m) {
     String[] str = m.split("\\|",-1); 
     int i = Integer.parseInt(str[1]);
     stats.addMeld(str[0], i);
     firePropertyChange(GameController.MELD_SCORE, str[0]+"MELD", ""+stats.getMeld(str[0]));
   }
 
   public void setTake(String c) {
     String[] str = c.split("\\|",-1); 
     int i = Integer.parseInt(str[1]);
     stats.addTake(str[0], i);
     firePropertyChange(GameController.TAKE_SCORE, str[0]+"TAKE", ""+stats.getTake(str[0]));
   }
   
   public void addScore() {
     stats.tally(this.bid, this.wscore);
     if (stats.hasWinner()) {
       this.status = GameController.OVER;
     }
     firePropertyChange(GameController.MELD_SCORE, "NSMELD", ""+stats.getMeld("NS"));
     firePropertyChange(GameController.MELD_SCORE, "EWMELD", ""+stats.getMeld("EW"));
     firePropertyChange(GameController.TAKE_SCORE, "NSTAKE", ""+stats.getTake("NS"));
     firePropertyChange(GameController.TAKE_SCORE, "EWTAKE", ""+stats.getTake("EW"));
     firePropertyChange(GameController.HAND_SCORE, "NSHAND", ""+stats.getHand("NS"));
     firePropertyChange(GameController.HAND_SCORE, "EWHAND", ""+stats.getHand("EW"));
     firePropertyChange(GameController.GAME_SCORE, "NSGAME", ""+stats.getGame("NS"));
     firePropertyChange(GameController.GAME_SCORE, "EWGAME", ""+stats.getGame("EW"));
     if (stats.hasWinner()) {
       firePropertyChange(GameController.WINNER, "NONE", stats.getWinner());
     }
   }
 
   /******************************
   public String getGameScore() {
     String s = String.format(
       "%6s %4d  %4d\n%6s %4d  %4d\n%6s %4d  %4d\n%6s %4d  %4d\n",
       "Meld:", meld[0], meld[1],
       "Take:", take[0], take[1],
       "Hand:", hand[0], hand[1],
       "Game:", game[0], game[1]
     );
     return s;
   }
   ******************************/
 
   /******************************
   public synchronized void addScore() {
     if (this.take[0] == 0) {
       // NO MELD FOR YOU!!!
       this.meld[0] = 0;
       this.hand[0] = 0;
       firePropertyChange(GameController.MELD_SCORE, "NSMELD", ""+this.meld[0]);
     } else {
       this.hand[0] = this.meld[0]+this.take[0];
     }
     if (this.bidder % 2 == 0 && this.hand[0] < this.bid) {
       this.hand[0] = (this.bid * -1);
     }
     firePropertyChange(GameController.HAND_SCORE, "NSHAND", ""+this.hand[0]);
     this.hand[1] = this.meld[1]+this.take[1];
     if (this.bidder % 2 != 0 && this.hand[1] < this.bid) {
       this.hand[1] = (this.bid * -1);
     }
 
     if (this.take[1] == 0) {
       // NO MELD FOR YOU!!!
       this.meld[1] = 0;
       this.hand[1] = 0;
       firePropertyChange(GameController.MELD_SCORE, "EWMELD", ""+this.meld[1]);
     } else {
       this.hand[1] = this.meld[1]+this.take[1];
     }
     firePropertyChange(GameController.HAND_SCORE, "EWHAND", ""+this.hand[1]);
 
     this.game[0] += this.hand[0];
     this.game[1] += this.hand[1];
     firePropertyChange(GameController.GAME_SCORE, "NSGAME", ""+this.game[0]);
     firePropertyChange(GameController.GAME_SCORE, "EWGAME", ""+this.game[1]);
 
     if (this.game[0] > this.wscore && this.game[1] >= this.wscore) {
       if (this.bidder % 2 == 0) {
         firePropertyChange(GameController.WINNER, "NONE", "NORTH_SOUTH");
       } else  {
         firePropertyChange(GameController.WINNER, "NONE", "EAST_WEST");
       }
       this.status = GameController.OVER;
     } else if (this.game[0] >= this.wscore) {
       firePropertyChange(GameController.WINNER, "NONE", "NORTH_SOUTH");
       this.status = GameController.OVER;
     } else if (this.game[1] >= this.wscore) {
       this.status = GameController.OVER;
       firePropertyChange(GameController.WINNER, "NONE", "EAST_WEST");
     }
     if (this.status == GameController.OVER) {
       System.out.println("Total hands: "+stats.getHands());
       System.out.println("N/S Meld:    "+stats.getNSMeld()+" E/W Meld:     "+stats.getEWMeld());
       System.out.println("N/S Take:    "+stats.getNSTake()+" E/W Take:     "+stats.getEWTake());
       System.out.println("N/S Take Pct.: "+(double)stats.getNSTake()/(stats.getNSTake()+stats.getNSMeld())+" E/W Take Pct.: "+(double)stats.getEWTake()/(stats.getEWTake()+stats.getEWMeld())); 
     } 
   }
   ******************************/
 }
