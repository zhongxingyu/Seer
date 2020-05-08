 package com.kongentertainment.android.cardtactics.view;
 
 import java.util.HashMap;
 
 import com.kongentertainment.android.cardtactics.R;
 
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 
 /**
  * Keeps a collection of relevant Card bitmaps to draw on the screen
  */
 public class CardViewManager {
 
     //PERF: Convert these to an arraylist implementation if performance is an issue
     //If a card is in either deck, its full image should be in here:
     private HashMap<Integer, Bitmap> mBigCards;
     //If a card is in either deck, its small image should be in here:
     private HashMap<Integer, Bitmap> mSmallCards;
 
     public CardViewManager () {
         mBigCards   = new HashMap<Integer, Bitmap>();
         mSmallCards = new HashMap<Integer, Bitmap>();
     }
     /** DEBUG ONLY */
     public CardViewManager (Resources res) {
         this();
         Bitmap bigCard = BitmapFactory.decodeResource(res,
                     R.drawable.bigcard);
         putBigCard(1, bigCard);
        Bitmap smallCard = BitmapFactory.decodeResource(res,
                R.drawable.card);
        putSmallCard(1, smallCard);
     }
 
     public Bitmap getBigCard(Integer cardID) {
         return mBigCards.get(cardID);
     }
     public Bitmap getSmallCard(Integer cardID) {
         return mSmallCards.get(cardID);
     }
 
     public void putBigCard(Integer cardID, Bitmap bitmap) {
         mBigCards.put(cardID, bitmap);
     }
     public void putSmallCard(Integer cardID, Bitmap bitmap) {
         mSmallCards.put(cardID, bitmap);
     }
 }
