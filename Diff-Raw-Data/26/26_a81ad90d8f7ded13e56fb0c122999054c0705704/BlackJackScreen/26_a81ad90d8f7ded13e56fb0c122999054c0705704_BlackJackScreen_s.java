 package groupproject.wargame;
 
 import java.util.ArrayList;
 import android.graphics.RectF;
 import sofia.graphics.ImageShape;
 import sofia.app.ShapeScreen;
 import sofia.graphics.Color;
 
 // -------------------------------------------------------------------------
 /**
  * Write a one-sentence summary of your class here. Follow it with additional
  * details about its purpose, what abstraction it represents, and how to use it.
  *
 * @author Matt
  * @version Dec 3, 2012
  */
 public class BlackJackScreen
     extends ShapeScreen
 {
     private Blackjack game;
     private float     height;
     private float     width;
     private float     cardHeight;
     private float     cardWidth;
     private ArrayList<ImageShape> dealerCardList;
     private int nextDealerIndex = 0;
     private ArrayList<ImageShape> playerCardList;
     private int nextPlayerIndex = 0;
 
 
     public void initialize()
     {
         game = new Blackjack();
 
         dealerCardList = new ArrayList<ImageShape>(8);
         playerCardList = new ArrayList<ImageShape>(8);
 
         setBackgroundColor(Color.green);
         height = getHeight();
         width = getWidth();
         cardWidth = 72;
         cardHeight = 96;
 
         for (float i = 0; i < 2 * cardHeight; i = i + cardHeight)
         {
             for (float j = 0; j < cardWidth*4; j = j + cardWidth)
             {
                 RectF rect = new RectF(0.0f, 0.0f, cardWidth, cardHeight);
                 ImageShape topCardSpace = new ImageShape("cardspace", rect);
                 topCardSpace.setPosition(j, i);
                 dealerCardList.add(topCardSpace);
                 add(topCardSpace);
             }
         }
 
         for (float i = height - 2*cardHeight; i < height; i = i + cardHeight)
         {
             for (float j = 0; j <cardWidth*4; j = j + cardWidth)
             {
                 RectF rect = new RectF(0.0f, 0.0f, cardWidth, cardHeight);
                 ImageShape bottomCardSpace = new ImageShape("cardspace", rect);
                 bottomCardSpace.setPosition(j, i);
                 playerCardList.add(bottomCardSpace);
                 add(bottomCardSpace);
             }
         }
     }
 
     // ----------------------------------------------------------
     /**
      * Called when the player hits the "Hit Me!" button.
      */
     public void hitButtonClicked()
     {
         Card hit = game.hit("Player");
         playerCardList.get(nextPlayerIndex).setImage(hit.toGraphic());
         nextPlayerIndex++;
         if(game.checkLose())
         {
             playerLost();
         }
     }
 
     public void playerLost()
     {
 
     }
 
     public void playerWon()
     {
 
     }
 }
