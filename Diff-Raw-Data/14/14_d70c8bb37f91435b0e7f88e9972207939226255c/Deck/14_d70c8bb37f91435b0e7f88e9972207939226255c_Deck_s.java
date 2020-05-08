 package com.example.pyramids;
 //import android.app.DrawView;
 
 import java.util.Random;
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Paint;
 import android.graphics.Color;
 import android.view.View;
 import android.view.MotionEvent;
 import android.widget.ImageView;
 import android.util.Log;
 
 public class Deck extends View
 {
     private static final String TAG = "Deck";
 
     public static final int WON = 1;
     public static final int LOST = 2;
     
     private int height, width;
 
     private ImageView boardView;
     private Bitmap board;
     private Bitmap blankCard;
 
     private int[] cardArray = {
         R.drawable.card0,
         R.drawable.card1,
         R.drawable.card2,
         R.drawable.card3,
         R.drawable.card4,
         R.drawable.card5,
         R.drawable.card6,
         R.drawable.card7,
         R.drawable.card8,
         R.drawable.card9,
         R.drawable.card10,
         R.drawable.card11,
         R.drawable.card12,
         R.drawable.card13,
         R.drawable.card14,
         R.drawable.card15,
         R.drawable.card16,
         R.drawable.card17,
         R.drawable.card18,
         R.drawable.card19,
         R.drawable.card20,
         R.drawable.card21,
         R.drawable.card22,
         R.drawable.card23,
         R.drawable.card24,
         R.drawable.card25,
         R.drawable.card26,
         R.drawable.card27,
         R.drawable.card28,
         R.drawable.card29,
         R.drawable.card30,
         R.drawable.card31,
         R.drawable.card32,
         R.drawable.card33,
         R.drawable.card34,
         R.drawable.card35,
         R.drawable.card36,
         R.drawable.card37,
         R.drawable.card38,
         R.drawable.card39,
         R.drawable.card40,
         R.drawable.card41,
         R.drawable.card42,
         R.drawable.card43,
         R.drawable.card44,
         R.drawable.card45,
         R.drawable.card46,
         R.drawable.card47,
         R.drawable.card48,
         R.drawable.card49,
         R.drawable.card50,
         R.drawable.card51
     };
 
     private final int deleted = -1;
     private final int rows = 4;
 
     // TODO dynamic padding
     private final int horizontalPadding = 100;
     private final int verticalPadding = 60;
 
     private final int cardWidth = 54;
     private final int cardHeight = 72;
 
     private final int[][] cardPositions =
         {
             {81, 243, 405},
             {54, 108, 216, 270, 378, 432},
             {27, 81, 135, 189, 243, 297, 351, 405, 459},
             {0, 54, 108, 162, 216, 270, 324, 378, 432, 486}
         };
     private final int[][] spaceCode =
         {
             {6,10,10},
             {4,2,6,2,6,2},
             {2,2,2,2,2,2,2,2,2},
             {0,2,2,2,2,2,2,2,2,2}
         };
     private final int[] childrenMarkers =
         {3, 5, 7, 9, 10, 12, 13, 15, 16, 18, 19, 20, 21, 22, 23, 24, 25, 26};
     //private final String[] colors = { "♣", "♠", "♥", "♦" };
     private final String[] colors = { "C", "S", "H", "D" };
     private final String[] faces = { "T", "J", "Q", "K", "A" };
     private final int numberOfCards = 52;
 
     private int[] cards;
     private Bitmap[] cardImages;
     private ImageView[] cardViews;
 
     private int stackCard;
     private int stackPos;
     private int stackRest;
     private int cardsToGo;
 
     private int run;
     private int score;
     private int round;
 
     /* http://games.yahoo.com/help/rules/py&ss=1 */
     private int currentWorth;
     private int pyramidBonus;
 
     private void setBitmaps(Context context)
     {
         this.blankCard = BitmapFactory.decodeResource(getResources(),
             R.drawable.cardblank);
 
         this.board = BitmapFactory.decodeResource(getResources(),
             R.drawable.board);
         this.boardView = new ImageView(context);
         this.boardView.setImageBitmap(this.board);
 
         for (int i = 0; i < this.numberOfCards; ++i)
         {
             this.cardImages[i] = BitmapFactory.decodeResource(getResources(),
                 this.cardArray[i]);
             /*
             this.cardViews[i] = new ImageView(context);
             this.cardViews[i] = (ImageView) findViewById(this.cardArray[i]);
             this.cardViews[i].setOnClickListener(new OnClickListener()
                     {
                         public void onClick(View v)
                         {
                             //Deck currentDeck = (Deck) v;
                             //currentDeck.cards[5] = -1;
                         }
                     });
                     */
         }
     }
 
     private void shuffle()
     {
         Random generator = new Random();
         int temp, randomCard;
 
         for (int i = 0; i < this.numberOfCards; ++i)
         {
             randomCard = generator.nextInt(this.numberOfCards);
             temp = this.cards[i];
             this.cards[i] = this.cards[randomCard];
             this.cards[randomCard] = temp;
         }
     }
 
     private String getValue(int num)
     {
         return ((num % 13) > 7
                 ? this.faces[(num % 13) - 8]
                 : (num % 13 + 2))
                 + this.colors[num / 13];
     }
 
     private void printSpaces(int n)
     {
         for (int i = 0; i < n; ++i)
             System.out.print(" ");
     }
 
     private boolean isDeleted(int pos)
     {
         return this.cards[pos] == this.deleted;
     }
 
     private boolean isFree(int pos)
     {
         return isDeleted(childrenMarkers[pos]) &&
             isDeleted(childrenMarkers[pos]+1);
     }
 
     private void printValue(int pos)
     {
         System.out.print(getValue(this.cards[pos]));
     }
 
     private int getParent(int pos)
     {
         for (int i = 0; i < childrenMarkers.length; ++i)
         {
             if (childrenMarkers[i] == pos || childrenMarkers[i] == pos-1)
                 return i;
         }
         return -1;
     }
 
     private int valueToNum(String s)
     {
         String faceString, colorString;
         int face = -1;
         int color = -1;
         s = s.toUpperCase();
 
         if (s.length() != 2)
             return -1;
 
         faceString = s.substring(0,1);
         colorString = s.substring(1,2);
 
         for (int i = 0; i < 8; ++i)
         {
             if (faceString.equals((new Integer(i+2)).toString()))
                 face = i;
         }
         for (int i = 8; i < 13; ++i)
         {
             if (faceString.equals(this.faces[i-8]))
                 face = i;
         }
         if (face < 0)
             return -1;
 
         for (int i = 0; i < 4; ++i)
         {
             if (colorString.equals(this.colors[i]))
                 color = i;
         }
         if (color < 0)
             return -1;
 
         return color*13 + face;
     }
 
     private void deleteCard(int pos)
     {
         this.cards[pos] = -1;
     }
 
     private boolean isNeighbor(int pos)
     {
         int x = this.cards[pos] % 13;
         int y = this.stackCard % 13;
         if ((x == 12 && y == 0) || (y == 12 && x == 0))
             return true;
         return Math.abs(x-y) == 1;
     }
 
     public boolean haveIWon()
     {
         return this.cardsToGo == 0;
     }
 
     private int nextStack()
     {
         if (this.stackRest == 1)
         {
             return 2;
         }
         ++this.stackPos;
         --this.stackRest;
 
         String s = ">>> stackPos: " + this.stackPos;
         Log.v(TAG, s);
 
         this.stackCard = cards[this.stackPos];
         return 0;
     }
 
     private void score(int pos)
     {
         if (pos < 3)
         {
             this.score += this.pyramidBonus;
             this.pyramidBonus += 250;
         }
         this.score += this.currentWorth;
         if (this.run < 7)
             this.currentWorth *= 2;
         else if (this.run == 7 || this.run == 8)
             this.currentWorth *= 1.5;
         else
             this.currentWorth += 100;
     }
 
     private void newRun()
     {
         this.run = 0;
         this.currentWorth = 10;
     }
 
     public int currentRound()
     {
         return this.round;
     }
 
     public int currentScore()
     {
         return this.score;
     }
 
     public int move(int i)
     {
         if (isNeighbor(i))
         {
             this.stackCard = cards[i];
             --this.cardsToGo;
             ++this.run;
             deleteCard(i);
             score(i);
             if (haveIWon())
                 return 1;
             invalidate();
         }
         return 0;
     }
 
     private String printStack()
     {
         String s = "";
         for (int i = 0; i < this.stackRest - 1; ++i)
             s += "#";
         return s;
     }
 
     public void print()
     {
         int pos = 0;
         System.out.println();
         for (int i = 0; i < this.rows; ++i)
         {
             for (int j = 0; j < spaceCode[i].length; ++j)
             {
                 printSpaces(spaceCode[i][j]);
                 if (isDeleted(pos))
                     System.out.print("  ");
                 else
                 {
                     if (i < this.rows-1)
                     {
                         if (isFree(pos))
                             printValue(pos);
                         else
                             System.out.print("@@");
                     }
                     else
                     {
                         printValue(pos);
                     }
                 }
                 ++pos;
             }
             System.out.println();
         }
         System.out.println();
         System.out.println("Card:  " + getValue(this.stackCard));
         System.out.println("Stack: " + printStack());
         System.out.println();
         System.out.println("Run:   " + this.run);
         System.out.println("Score: " + this.score);
         System.out.println("Round: " + this.round);
         System.out.println();
     }
 
     @Override
     public boolean onTouchEvent(final MotionEvent ev)
     {
         int pos = 0;
         int x = ((int)ev.getX()) - this.horizontalPadding;
         int y = ((int)ev.getY()) - this.verticalPadding;
 
         if (ev.getAction() != MotionEvent.ACTION_DOWN)
             return true;
 
         // Stack
         if (x >= 0 && x <= (this.stackRest-2)*12 + this.cardWidth &&
                 y >= 215 && y <= 215 + this.cardHeight)
         {
             newRun();
             nextStack();
             invalidate();
         }
 
         for (int i = 0; i < this.cardPositions.length; ++i)
         {
             for (int j = 0; j < this.cardPositions[i].length; ++j)
             {
                 int cardX = this.cardPositions[i][j];
                 int cardY = i*36;
 
                 if (
                         (x >= cardX) &&
                         (x <= cardX + this.cardWidth) &&
                         (y >= cardY) &&
                         (y <= cardY + this.cardHeight) &&
                         (pos > 17 || isFree(pos))
                         )
                 {
                     String s = ": " + pos;
                     Log.v(TAG, s);
                     move(pos);
                 }
                 ++pos;
             }
         }
         return true;
     }
 
     @Override
     public void onDraw(Canvas canvas)
     {
         int pos = 0;
 
         Bitmap currentCard;
 
         canvas.drawBitmap(this.board,
                 this.horizontalPadding-5,
                 this.verticalPadding-5, null);
 
         Paint paint = new Paint();
         paint.setColor(Color.BLACK);
         paint.setTextSize(18);
         paint.setAntiAlias(true);
 
         String text = "Run: " + this.run;
         canvas.drawText(text,
                 horizontalPadding + 10,
                 verticalPadding - 10,
                 paint);
         text = "Score: " + this.score;
         canvas.drawText(text,
                 horizontalPadding + 230,
                 verticalPadding - 10,
                 paint);
         text = "Round: " + this.round;
         canvas.drawText(text,
                 horizontalPadding + 460,
                 verticalPadding - 10,
                 paint);
 
         for (int i = 0; i < this.rows; ++i)
         {
             for (int j = 0; j < this.cardPositions[i].length; ++j)
             {
                 if (!isDeleted(pos))
                 {
                     if ((i < this.rows-1 && isFree(pos)) || (i >= this.rows-1))
                         currentCard = this.cardImages[this.cards[pos]];
                     else
                         currentCard = this.blankCard;
 
                     canvas.drawBitmap(currentCard,
                             this.horizontalPadding + this.cardPositions[i][j],
                             this.verticalPadding + i*36, null);
                 }
                 ++pos;
             }
         }
 
         for (int i = 0; i < this.stackRest-1; ++i)
         {
             canvas.drawBitmap(this.blankCard,
                     this.horizontalPadding + i*12,
                     this.verticalPadding + 215, null);
         }
 
        String s = ">>> stackCard: " + this.stackCard;
         Log.v(TAG, s);
 
         canvas.drawBitmap(this.cardImages[this.stackCard],
                 this.horizontalPadding + 350,
                 this.verticalPadding + 215, null);
     }
 
     public Deck(Context context)
     {
         super(context);
         cards = new int[numberOfCards];
         cardImages = new Bitmap[numberOfCards];
         cardViews = new ImageView[numberOfCards];
         for (int i = 0; i < numberOfCards; ++i)
         {
             cards[i] = i;
         }
         shuffle();
         this.stackPos = 28;
         this.stackRest = this.numberOfCards - this.stackPos;
         this.stackCard = cards[this.stackPos];
         this.cardsToGo = 28;
         this.run = 0;
         this.score = 0;
         this.currentWorth = 10;
         this.round = 1;
         this.pyramidBonus = 500;
         setBitmaps(context);
     }
 
     public Deck(Context context, int round, int score)
     {
         this(context);
         this.round = round;
         this.score = score;
         this.pyramidBonus = (round+1)*250;
     }
 }
