 package com.example.cards;
 
 import java.util.ArrayList;
 
 import android.widget.RelativeLayout;
 
 public class CardsContainer
 {
 
 	private ArrayList<Card> _cards;
 	
 	private RelativeLayout _associatedLayout;
 	
 	private final static int[] CLUB_CARDS_RESOURCE = {R.raw.cards_ac, R.raw.cards_2c, R.raw.cards_3c, R.raw.cards_4c, R.raw.cards_5c, R.raw.cards_6c, R.raw.cards_7c, R.raw.cards_8c, 
     		R.raw.cards_9c, R.raw.cards_10c, R.raw.cards_jc, R.raw.cards_qc, R.raw.cards_kc};
 	
 	private final int STARTING_X = 20;
 	public static int CARD_DISTANCE = 80;
 	
 	public CardsContainer(RelativeLayout layout) 
 	{
 		_associatedLayout = layout;
 		_cards = getClubCards();
 		
 		_associatedLayout.setOnDragListener(new CardDragListener(this));
 		
 		paint();
 	}
 
 	// Use this method only one time
 	private void paint()
 	{
         int x = STARTING_X;
         
         for(Card card : _cards)
         {       
 	        // Set the position
         	card.getView().setX(x);
         	x += CARD_DISTANCE;
 	        
 	        _associatedLayout.addView(card.getView());
         }
 	}
 	
 	public void refresh()
 	{
 		System.out.println("[REFRESH] " + _cards.size());
 		
 		int x = STARTING_X;
        
         for(Card card : _cards)
         {       
 	        // Set the position
         	card.getView().setX(x);
         	card.getView().bringToFront();
         	x += CARD_DISTANCE;
         }
 	}
 	
 	// Allow to switch the position of one card according to its coordinates (x)
 	public void update(String cardName, float x)
 	{
 		System.out.println("SelectedCard: " + cardName + " - Position: " + x);
 		
 		Card selectedCard = getCardByName(cardName);
 		
 		if(selectedCard != null)
 		{
 			for(int i = 0; i < _cards.size(); i ++)
 			{
 				int position = STARTING_X + i * CARD_DISTANCE;
 				
 				int indexToRemplace = -1;
 				
 				if(x >= position && x < position + Card.CARD_WIDTH)
 				{
 					indexToRemplace = i + 1;
 				}
 				else if(x >= 0 && x < STARTING_X)
 				{
 					indexToRemplace = 0;
 				}
 				
 				if(indexToRemplace >= 0)
 				{
 					_cards.remove(selectedCard);
 					
 					if(indexToRemplace > _cards.size())
 					{
 						_cards.add(selectedCard);
 					}
 					else
 					{
 						_cards.add(indexToRemplace, selectedCard);
 					}
 					
 					refresh();
 					
					//selectedCard.getView().bringToFront();
					
 					return;
 				}
 			}
 		}
 	}
 	
 	public void centerCard(String cardName) 
 	{
 		Card c = getCardByName(cardName);
 		
 		if(c != null)
 		{
 			
 			int middleIndex = (int) (_cards.size() / 2);
 			
 			_cards.remove(c);
 			_cards.add(middleIndex, c);
 			
 			int x = STARTING_X;
 	        
 			boolean hasBeenAdded = false;
 			
 			for(Card card : _cards)
 	        {       
 		        if(card.equals(c) || hasBeenAdded)
 		        {
		        	x += 250;
 		        	hasBeenAdded = !hasBeenAdded;
 		        }
 		       
 		        card.getView().setX(x);
		        
	        	card.getView().bringToFront();
 	        	x += CARD_DISTANCE;
 	        }
 		}
 	}
 	
 	// Return a card from the card list according to its name
 	public Card getCardByName(String cardName)
 	{
 		for(Card c : _cards)
 		{
 			if(c.get_name().equals(cardName)) return c;
 		}
 		
 		return null;
 	}
 	
 	public void removeCard(Card c)
 	{
 		try
 		{
 			_cards.remove(c);
 		}
 		catch(Exception ex)
 		{
 			ex.printStackTrace();
 		}
 	}
 	
 	
 	public ArrayList<Card> get_cards() {
 		return _cards;
 	}
 	
 	public ArrayList<Card> getClubCards()
 	{
 		ArrayList<Card> res = new ArrayList<Card>();
 		
 		int i = 1;
 		
 		for(int r : CLUB_CARDS_RESOURCE)
 		{
 			res.add(new Card(_associatedLayout.getContext(), "Club_" + i, r, i++));
 		}
 		
 		return res;
 	}
 }
