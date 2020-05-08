 package com.adaba.activities;
 
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 import com.adaba.R;
 import com.adaba.cards.Card;
 import com.adaba.cards.Card.Type;
 import com.adaba.deck.Deck;
 import com.adaba.deck.Deck.Hand;
 import com.adaba.deck.DeckCreator;
 
 public class PlayerViewActivity extends Activity {
 	
 	protected void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_view_player);
 		
 		try {
 			Deck deck = DeckCreator.makeCoHDeck(getResources().openRawResource(R.raw.white), Type.WHITE);
 			Hand hand = deck.drawHand();
 			
 			// Get a list of the card names in current hand
 			List<String> stringHand = new LinkedList<String>();
			for(Card c: hand.getCards()) { stringHand.add(c.getText()); }
 			
 			// Create ListView backed by names of Cards in Hand
 			ListView handView = (ListView) findViewById(R.id.handList);
 			ArrayAdapter<String> adapt = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, stringHand);
 			handView.setAdapter(adapt);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
