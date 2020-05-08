 /**
  * 
  */
 package org.cam.card;
 
 /**
  * @author Cam Moore
  *
  */
 public interface ICard extends Comparable<ICard> {
   /** SPADES a int. */
  public static final int SPADES = 3;
   /** HEARTS a int. */
  public static final int HEARTS = 2;
   /** DIAMONDS a int. */
  public static final int DIAMONDS = 1;
   /** CLUBS a int. */
  public static final int CLUBS = 0;
   /** PAI_GAO_JOKER a int. */
   public static final int PAI_GAO_JOKER = 4;
   
   /** TWO a int. */
   public static final int TWO = 2;
   /** THREE a int. */
   public static final int THREE = 3;
   /** FOUR a int. */
   public static final int FOUR = 4;
   /** FIVE a int. */
   public static final int FIVE = 5;
   /** SIX a int. */
   public static final int SIX = 6;
   /** SEVEN a int. */
   public static final int SEVEN = 7;
   /** EIGHT a int. */
   public static final int EIGHT = 8;
   /** NINE a int. */
   public static final int NINE = 9;
   /** TEN a int. */
   public static final int TEN = 10;
   /** JACK a int. */
   public static final int JACK = 11;
   /** QUEEN a int. */
   public static final int QUEEN = 12;
   /** KING a int. */
   public static final int KING = 13;
   /** ACE a int. */
   public static final int ACE = 14;
   /** JOKER a int. */
   public static final int JOKER = 0;
   /**
    * @return the suit.
    */
   public int getSuit();
   /**
    * @return the rank.
    */
   public int getRank();
   /**
    * @return true if is a joker.
    */
   public boolean isJoker();
 }
