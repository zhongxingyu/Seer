 package com.game.pileon;
 
 import android.graphics.Color;
 import android.util.Log;
 
 /**
  * DefaultGameType
  * 
  * This is the default game. 4 colors (R G B Y) and ten cards of each (1 through
  * 10)
  * 
  * @author nscross
  * 
  */
 public class DefaultGameType implements GameType
 {
 
 	private Deck Deck;
 
 	private final static int NUMBEROFCOLORS = 4;
 	private final static int NUMBEROFCARDSPERCOLOR = 10;
 
 	private final static int CARDCOLORS[] =
 	{ Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW };
 
 	public enum CardColorTypes
 	{
 		Red, Green, Blue, Yellow
 	}
 
 	public DefaultGameType()
 	{
 		CreateDeck();
 	}
 
 	public void CreateDeck()
 	{
 		Deck = new Deck();
 
 		for (int colorNdx = 0; colorNdx < NUMBEROFCOLORS; colorNdx++)
 		{
 			for (int cardNdx = 0; cardNdx < NUMBEROFCARDSPERCOLOR; cardNdx++)
 			{
 				DefaultGameCard cardToAdd = new DefaultGameCard(
 						CARDCOLORS[colorNdx], cardNdx + 1);
 				Deck.AddCard(cardToAdd);
				Log.d("PO CreateDeck", cardToAdd.toString());
 			}
 		}
 
 		Deck.Shuffle();
 		//Log.d("PO", Deck.toString());
 	}
 
 }
