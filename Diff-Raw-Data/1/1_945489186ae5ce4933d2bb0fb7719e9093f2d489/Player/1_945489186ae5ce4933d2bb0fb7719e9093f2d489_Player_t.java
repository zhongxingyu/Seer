 package org.joedog.pinochle.player;
 
 import org.joedog.pinochle.control.GameController;
 import org.joedog.pinochle.view.Setting;
 import org.joedog.pinochle.game.*;
 import org.joedog.pinochle.util.*;
 import java.net.URL;
 import java.awt.Canvas;
 import java.util.Random;
 
 public abstract class Player {
   public static final  int HUMAN    = 0;
   public static final  int COMPUTER = 1;
   protected Hand       hand;
   protected Meld       meld;
   public   int         partner;
   public   int         position;
   public   int         type;
   public   String      name;
   public   Setting     setting;
   public   int         maxBid = 0;
   public   int         myBid  = 0;
   public   int         pBid   = 0; 
   public   Assessment  assessment;
   public   boolean     bidder = false;
   public   String      memory;
   public   String      memtxt = System.getProperty("pinochle.memory");
   public   Knowledge   knowledge = null;
   public   int         distance;
 
   public Player () {
     newHand();
     this.knowledge = knowledge.getInstance();
   }
 
   public void takeCard(Card c) {
     this.hand.add(c);
   }
 
   public void refresh() {
     this.setting.refresh(this.hand);
   }
 
   public synchronized void newHand() {
     this.hand   = new Hand();
     this.myBid  = 0;
     this.maxBid = 0;
     this.pBid   = 0;
   }
 
   public void assessHand() {
     meld = new Meld(this.hand);
     assessment   = meld.assessment(); 
     this.maxBid  = assessment.maxBid();
     // we want to rely on experience but that's 
     // stored in a file that can be removed. If 
     // the file is purged, experience returns the
     // programmatic maxBid
     this.maxBid  = experience(this.hand.asRanks());
     this.maxBid += guts();
     Debug.print("  "+this.name+"'s assessment: "+assessment.toString());
 
     // We need to shave some bid if we're lacking aces 
     // We'll shave even more down below if we don't have
     // adequate meld....
     if (this.distance >= 2) {
       Debug.print("  Too much distance to trust experience....");
       if (assessment.getAces() < 3) {
         Debug.print("  Less than three aces, shaving 3 points");
         this.maxBid -= 3;
       }
    
       // adjust down a mediocre meld low power hand
       if (assessment.getAces() < 3 && assessment.getMeld() < 15) {
         Debug.print("  Mediocre meld, low power.");
         this.maxBid = (assessment.getTrumpCount() >= 5) ? this.maxBid : (this.maxBid - 4);
       }
 
       if (assessment.getAces() < 3) {
         Debug.print("  Less than three aces");
         this.maxBid -= 2;
       }
 
       if (this.maxBid >= 30 && assessment.getMeld() < 10) {
         Debug.print("  Too little meld to bid more than 30");
         this.maxBid -= 2;
       }
 
       // no good ever came from a hand with no aces...
       if (assessment.getAces() == 0) {
         Debug.print("  NO ACES (meld+8)");
         this.maxBid = assessment.getMeld() + 8;
       }
  
       // conversely, good things come to those with aces 
       if (assessment.getAces() >= 3 && this.maxBid < 16) {
         Debug.print("  More than three acess (at least 24)");
         this.maxBid = (assessment.getAces() >= 4) ? 28 : 24;
       }
 
       // Bids in the thirties with 4 cards in trump are nearly 
       // impossible to achieve without a shitload of meld
       if (assessment.getTrumpCount() < 5) {
         Debug.print("  Inadequate trump");
         this.maxBid -= 2;
       }
 
       if (assessment.getTrumpCount() >= 6) {
         Debug.print("  TONS of trump! bumping by six...");
         this.maxBid += 6;
       }
 
       if (assessment.getTrumpCount() > 5 && this.maxBid < 16) {
         Debug.print("  Lot's of trump! We have to make a few bids..");
         this.maxBid = 29;
       }
 
       if (assessment.getTrumpCount() >= 5 && assessment.getAces() >= 3 && this.maxBid < 16) {
         // I'm not sure how this scenario occurs but I've seen it
         Debug.print("  Weirdo scenario....");
         this.maxBid = 30;
       }
     }
     
     if (this.maxBid >= 40 && assessment.getMeld() < 10) {
       this.maxBid = 36;
     }
 
     if (this.maxBid >= 50 && assessment.getMeld() < 25) {
       this.maxBid = 38;
     }
 
     // This is for experience generation. By forcing a high
     // maxBid we're ensured of capturing a lot of different
     // hand combinations...
     if (this.name.equals("Limey")) {
       if (this.maxBid < 28) 
         this.maxBid = 28;
     } 
   }
 
   private int guts() {
     int num = RandomUtils.number(100);
 
     if (num == 100) {
       return 10;
     }
     if (num >= 90) {
       return 5;
     }
     if (num >= 80) {
       return 4;
     }
     if (num >= 70) {
       return 3;
     }
     if (num >= 60) {
       return 2;
     }
     if (num < 11) {
       return -2;
     }
     return 0;
   }
 
   public void setup(Setting setting, int position, int partner, String name) {
     this.setting  = setting;
     this.position = position;
     this.partner  = partner;
     this.name     = name;
   } 
 
   public void setText(String text) {
     this.setting.setText(text);
   }
 
   public void showHand() {
     System.out.println(this.name+" "+this.hand.toString());
   }
 
   public String handToString() {
     return this.hand.toString();
   }
 
   public boolean wonBid() {
     return this.bidder;
   }
 
   public int getPosition() {
     return this.position;
   }
 
   public int getPartner() {
     return this.partner;
   }
 
   public int getMaxBid() {
     return this.maxBid;
   }
 
   public int lastBid() {
     return this.myBid;
   }
 
   /**
    * This is a programmer's helper; it will
    * never be called in the final product....
    */
   public void clearHand() {
     while (this.hand.size() > 0) {
       this.hand.remove(0);
     }  
   }
 
   public String getName() {
     return this.name;
   }
 
   public int getType () {
     return this.type;
   }
 
   public Hand getHand() {
     return this.hand;
   }
 
   private int experience (String hand) {
     int bid = -1;
     int low = 1000;
     RollingAverage avg = new RollingAverage();
 
     if (! FileUtils.exists(this.memtxt)) {
       // Somebody chucked his memory file;
       // we'll revert to Old School guessing
       return this.maxBid;  
     }
 
     String a [] = new String[2];
     for (String line: knowledge.getMemory()) {
       String[] array = line.split("\\|",-1);
       /** 
        * Suits 2 and 3 are diamonds and spades they
        * are treated as constants because of pinochle
        * suits 0 and 1 are hearts and clubs and they
        * are treated interchangeably...
        */
       a[0] = array[0]+array[1]+array[2]+array[3];
       a[1] = array[1]+array[0]+array[2]+array[3];
 
       for (String s: a) {
         int d = computeDistance(hand, s);
         if (d < low) {
           low = d;
           /**
            * Each time d hits a new low; we
            * have to reset the rolling average
            */
           avg.removeAll();
           if (array.length == 5) {
             bid = Integer.parseInt(array[4]);
           }
         }
         if (d == low) {
           if (array.length == 5) {
             int tmp  = Integer.parseInt(array[4]);
             avg.add(tmp);
             bid = (int)avg.average();
           }
         }
       }
     }
     Debug.print("  Distance of the match:    "+low);
     Debug.print("  Average bid returned bid: "+bid);
     this.distance = low;
     // We'll adjust down high bids with high distances
     if (bid >= 35 && low >= 4) return (bid - 6);
     if (bid >= 35 && low >= 3) return (bid - 4);
     if (bid >= 35 && low >= 2) return (bid - 2);
     return bid;
   }
 
   private int computeDistance(String s1, String s2) {
     //s1 = s1.toLowerCase();
     //s2 = s2.toLowerCase();
 
     int[] costs = new int[s2.length() + 1];
     for (int i = 0; i <= s1.length(); i++) {
       int lastValue = i;
       for (int j = 0; j <= s2.length(); j++) {
         if (i == 0)
           costs[j] = j;
         else {
           if (j > 0) {
             int newValue = costs[j - 1];
             if (s1.charAt(i - 1) != s2.charAt(j - 1))
               newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
             costs[j - 1] = lastValue;
             lastValue = newValue;
           }
         }
       }
       if (i > 0)
         costs[s2.length()] = lastValue;
     }
     return costs[s2.length()];
   }
 
   public void remember(int meld, int take) {
     if (!this.bidder) return;
     if (this.memory == null || this.memory.length() < 2) return;
     int game = (meld+take);
 
     this.memory += "|"+game;
     Logger.remember(memtxt, memory);
     this.memory = new String("");
   }
 
   public abstract void remember(Deck cards);
   
   public abstract void remember(Card card);
   
   public abstract Card playCard(Trick trick);
 
   public abstract int bid(int bid); 
 
   public abstract int bid(int bid, int pbid, boolean opponents);
 
   public abstract int nameTrump();
 
   public abstract Deck passCards(boolean bidder);
 
   public abstract void takeCards(Deck d);
 
   public abstract int meld();
 
   public abstract void clearMeld();
 }
