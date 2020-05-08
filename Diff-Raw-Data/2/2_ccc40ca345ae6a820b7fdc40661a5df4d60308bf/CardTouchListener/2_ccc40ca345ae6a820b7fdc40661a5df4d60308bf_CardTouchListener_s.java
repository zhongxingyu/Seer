 package com.zuehlke.jhp.bucamp.android.jass;
 
 import android.content.ClipData;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import ch.mbaumeler.jass.core.card.Card;
 
 public class CardTouchListener implements OnTouchListener {
    	
 	private Card card;
 	
 	public CardTouchListener(Card card) {
 		this.card = card;
 	}
 	
 	public boolean onTouch(View view, MotionEvent motionEvent) {
 	
       if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
         String serializedCard = serialize(card);
 		ClipData data = ClipData.newPlainText(serializedCard, serializedCard);
         view.startDrag(data, new View.DragShadowBuilder(view), view, 0);
        view.setVisibility(View.INVISIBLE);
         return true;
       } else {
         return false;
       }
     }
 	
 	private String serialize(Card card) {
 		return card.getSuit() + "_" + card.getValue();
 	}
 }
