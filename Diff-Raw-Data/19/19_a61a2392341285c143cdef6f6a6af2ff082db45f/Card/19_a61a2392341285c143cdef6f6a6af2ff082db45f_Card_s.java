 /**
 Copyright 2010 David Minor
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
   http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 
 File: Card.java - basic playing card
 */
 
 package minor.cards;
 
 public class Card implements Comparable<Card> {
 
   public static final Card $AC = new Card(Rank.ACE, Suit.CLUBS);
   public static final Card $AD = new Card(Rank.ACE, Suit.DIAMONDS);
   public static final Card $AH = new Card(Rank.ACE, Suit.HEARTS);
   public static final Card $AS = new Card(Rank.ACE, Suit.SPADES);
   public static final Card $2C = new Card(Rank.TWO, Suit.CLUBS);
   public static final Card $2D = new Card(Rank.TWO, Suit.DIAMONDS);
   public static final Card $2H = new Card(Rank.TWO, Suit.HEARTS);
   public static final Card $2S = new Card(Rank.TWO, Suit.SPADES);
   public static final Card $3C = new Card(Rank.THREE, Suit.CLUBS);
   public static final Card $3D = new Card(Rank.THREE, Suit.DIAMONDS);
   public static final Card $3H = new Card(Rank.THREE, Suit.HEARTS);
   public static final Card $3S = new Card(Rank.THREE, Suit.SPADES);
   public static final Card $4C = new Card(Rank.FOUR, Suit.CLUBS);
   public static final Card $4D = new Card(Rank.FOUR, Suit.DIAMONDS);
   public static final Card $4H = new Card(Rank.FOUR, Suit.HEARTS);
   public static final Card $4S = new Card(Rank.FOUR, Suit.SPADES);
   public static final Card $5C = new Card(Rank.FIVE, Suit.CLUBS);
   public static final Card $5D = new Card(Rank.FIVE, Suit.DIAMONDS);
   public static final Card $5H = new Card(Rank.FIVE, Suit.HEARTS);
   public static final Card $5S = new Card(Rank.FIVE, Suit.SPADES);
   public static final Card $6C = new Card(Rank.SIX, Suit.CLUBS);
   public static final Card $6D = new Card(Rank.SIX, Suit.DIAMONDS);
   public static final Card $6H = new Card(Rank.SIX, Suit.HEARTS);
   public static final Card $6S = new Card(Rank.SIX, Suit.SPADES);
   public static final Card $7C = new Card(Rank.SEVEN, Suit.CLUBS);
   public static final Card $7D = new Card(Rank.SEVEN, Suit.DIAMONDS);
   public static final Card $7H = new Card(Rank.SEVEN, Suit.HEARTS);
   public static final Card $7S = new Card(Rank.SEVEN, Suit.SPADES);
   public static final Card $8C = new Card(Rank.EIGHT, Suit.CLUBS);
   public static final Card $8D = new Card(Rank.EIGHT, Suit.DIAMONDS);
   public static final Card $8H = new Card(Rank.EIGHT, Suit.HEARTS);
   public static final Card $8S = new Card(Rank.EIGHT, Suit.SPADES);
   public static final Card $9C = new Card(Rank.NINE, Suit.CLUBS);
   public static final Card $9D = new Card(Rank.NINE, Suit.DIAMONDS);
   public static final Card $9H = new Card(Rank.NINE, Suit.HEARTS);
   public static final Card $9S = new Card(Rank.NINE, Suit.SPADES);
   public static final Card $TC = new Card(Rank.TEN, Suit.CLUBS);
   public static final Card $TD = new Card(Rank.TEN, Suit.DIAMONDS);
   public static final Card $TH = new Card(Rank.TEN, Suit.HEARTS);
   public static final Card $TS = new Card(Rank.TEN, Suit.SPADES);
   public static final Card $JC = new Card(Rank.JACK, Suit.CLUBS);
   public static final Card $JD = new Card(Rank.JACK, Suit.DIAMONDS);
   public static final Card $JH = new Card(Rank.JACK, Suit.HEARTS);
   public static final Card $JS = new Card(Rank.JACK, Suit.SPADES);
   public static final Card $QC = new Card(Rank.QUEEN, Suit.CLUBS);
   public static final Card $QD = new Card(Rank.QUEEN, Suit.DIAMONDS);
   public static final Card $QH = new Card(Rank.QUEEN, Suit.HEARTS);
   public static final Card $QS = new Card(Rank.QUEEN, Suit.SPADES);
   public static final Card $KC = new Card(Rank.KING, Suit.CLUBS);
   public static final Card $KD = new Card(Rank.KING, Suit.DIAMONDS);
   public static final Card $KH = new Card(Rank.KING, Suit.HEARTS);
   public static final Card $KS = new Card(Rank.KING, Suit.SPADES);
   
   private Rank rank;
   private Suit suit;
 
   /**
    * Creates a new card. Prefer the public static cards
    * over creating new card objects.
    */
   public Card(Rank rank, Suit suit) {
     this.rank = rank;
     this.suit = suit;
   }
 
   /**
    * Get the rank of the card.
    */
   public Rank getRank() {
     return rank;
   }
 
   /**
    * Get the suit of the card.
    */
   public Suit getSuit() {
     return suit;
   }
 
   /**
    * Compare to cards.
    */
   public int compareTo(Card card) {
     if (this == card)
       return 0;
 
    int comp = suit.compareTo(card.getSuit());
     if (comp != 0)
       return comp;
 
    return rank.compareTo(card.getRank());
   }
 
   private String name;
 
   public String toString() {
     if (name == null)
       name = rank.toString() + suit.toString();
     
     return name;
   }
 }
 
