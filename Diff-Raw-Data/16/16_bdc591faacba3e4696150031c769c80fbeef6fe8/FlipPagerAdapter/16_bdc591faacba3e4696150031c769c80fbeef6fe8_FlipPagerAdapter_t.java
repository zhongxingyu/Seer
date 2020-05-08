 package com.bonkler.flipdroid;
 
 import java.util.*;
 
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentStatePagerAdapter;
 
 // TODO: Rewrite this to take a FlipDeck instead of an ArrayList<FlipCard>
 
 public class FlipPagerAdapter extends FragmentStatePagerAdapter {
     
     private ArrayList<FlipCard> cards;
 
     public FlipPagerAdapter(FragmentManager fm) {
         super(fm);
     }
 
     @Override
     public int getCount() {
         return !(cards == null) ? cards.size() : 100;
     }
 
     @Override
     public Fragment getItem(int i) {
         CardFragment cf = new CardFragment();
        if (cards.size() > 0)
            cf.setCard(cards.get(i));
 
         return cf;
     }
 
     @Override
     public int getItemPosition(Object object) {
         return POSITION_NONE;
     }
 
     public FlipCard getCardAtIndex(int i) {
         if (cards != null)
             return cards.get(i);
         return null;
     }
 
     public ArrayList<FlipCard> changeCards(ArrayList<FlipCard> newCards) {
         ArrayList<FlipCard> oldCards = cards;
         cards = newCards;
 
         notifyDataSetChanged();
         return oldCards;
     }
 
 }
