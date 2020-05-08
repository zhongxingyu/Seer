 // Card.java
 // Copyright (c) 2012 Andrew Downing
 // Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 // The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 // THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 
 import java.util.*;
 import java.awt.*;
 import javax.swing.*;
 import java.io.*;
 
 /** represents a single playing card and stores images for each card */
 public class Card implements Serializable {
   public enum Suit {
     Clubs, Diamonds, Hearts, Spades
   }
 
   // rank constants
   public static final int Jack = 11;
   public static final int Queen = 12;
   public static final int King = 13;
   public static final int Ace = 14;
   public static final int MinRank = 2;
   public static final int MaxRank = Ace;
 
   // constants related to card image paths
   private static final String ImgPath = "cards/"; /**< folder where card images are saved */
   private static final String BackImgPath = ImgPath + "b.gif"; /**< file path of card back image */
 
   // properties of this card
   private int rank; /**< rank of the card between 2 and 14, where 11-13 are face cards and 14 is an ace (which has the greatest worth in German Whist */
   private Suit suit; /**< suit of the card (Clubs, Diamonds, Heats, or Spades) */
   private boolean faceUp; /**< whether this card is face up */
 
   // variables related to card images
   private static boolean imgsLoaded = false; /**< whether all images have been loaded */
   private static int imgWidth = 0; /**< width (in pixels) of card image */
   private static int imgHeight = 0; /**< height (in pixels) of card image */
   private static ImageIcon backImg; /**< image of back of card */
   /** image of front of card
       (this was originally an ArrayList of ArrayLists, but reading out of it was very slow due to required casts to ArrayList and ImageIcon (freezing my computer at times), so I changed it to a multidimensional array) */
   private static ImageIcon[][] frontImgs;
 
   /** constructor for playing card */
   public Card(int newRank, Suit newSuit, boolean newFaceUp) {
     // throw exception if rank is invalid
     if (newRank < MinRank || newRank > MaxRank) {
       throw new IllegalArgumentException("Rank must be between " + MinRank + " and " + MaxRank);
     }
     // remember suit, rank, and whether is face up
     rank = newRank;
     suit = newSuit;
     faceUp = newFaceUp;
   }
 
   /** load playing card images (must call this once before drawing any cards) */
   public static void loadImgs() throws FileNotFoundException {
     // throw exception if back image file does not exist
     if (!new File(BackImgPath).exists()) {
       throw new FileNotFoundException("Could not find file \"" + BackImgPath + "\".\n"
                                      + "Make sure the card images were extracted to \"" + ImgPath + "\".");
     }
     // load back image
     backImg = new ImageIcon(BackImgPath);
     // remember width and height of card image
     imgWidth = backImg.getIconWidth();
     imgHeight = backImg.getIconHeight();
     // load front images
     frontImgs = new ImageIcon[MaxRank - MinRank + 1][Suit.values().length];
     for (int currRank = MinRank; currRank <= MaxRank; currRank++) {
       for (Suit currSuit : Suit.values()) {
         StringBuilder path = new StringBuilder();
         path.append(ImgPath); // append folder
         // append rank
         switch (currRank) {
           case 10:
             path.append("t");
             break;
           case Jack:
             path.append("j");
             break;
           case Queen:
             path.append("q");
             break;
           case King:
             path.append("k");
             break;
           case Ace:
             path.append("a");
             break;
           default:
             path.append(currRank);
         }
         // append suit
         path.append(currSuit.toString().toLowerCase().charAt(0));
         // append file extension
         path.append(".gif");
         // throw exception if back image file does not exist
         if (!new File(path.toString()).exists()) {
           throw new FileNotFoundException("Could not find file \"" + path + "\".\n"
                                           + "Make sure the card images were extracted to \"" + ImgPath + "\".");
         }
         // load this front image
         frontImgs[currRank - MinRank][currSuit.ordinal()] = new ImageIcon(path.toString());
       }
     }
     imgsLoaded = true;
   }
 
   /** draw card centered at specified position */
   public final void draw(Component c, Graphics2D g2, Point pos) {
     ImageIcon cardImg;
     // throw exception if card images are not loaded
     if (!imgsLoaded) {
       throw new IllegalStateException("Card images have not been loaded yet");
     }
     if (faceUp) {
       cardImg = frontImgs[rank - MinRank][suit.ordinal()];
     }
     else {
       cardImg = backImg;
     }
     cardImg.paintIcon(c, g2, pos.x - imgWidth / 2, pos.y - imgHeight / 2);
   }
 
   /** set whether this card is face up */
   public void setFaceUp(boolean newFaceUp) {
     faceUp = newFaceUp;
   }
 
   /** returns whether has same rank and suit as specified card */
   public final boolean equals(Card otherCard) {
     return rank == otherCard.getRank() && suit == otherCard.getSuit();
   }
 
   /** returns rank and first character of suit as String
       (returns rank simply as number since this is only used for debugging) */
   public final String getString() {
     return "" + rank + suit.toString().charAt(0);
   }
 
   /** getter for rank */
   public final int getRank() {
     return rank;
   }
 
   /** getter for suit */
   public final Suit getSuit() {
     return suit;
   }
 
   /** getter for card image width */
   public static final int getImgWidth() {
     return imgWidth;
   }
 
   /** getter for card image height */
   public static final int getImgHeight() {
     return imgHeight;
   }
 }
