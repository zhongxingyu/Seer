 package com.adaba.deck;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
import com.adaba.cards.*;
 
 public class DeckCreator {
 
 	public static List<Card> makeCoHDeck(InputStream cardFile, Type type) throws IOException {
 		InputStreamReader isReader = new InputStreamReader(cardFile);
 		BufferedReader reader = new BufferedReader(isReader);
 		List<Card> cardList = new ArrayList<Card>();
 
 		for (int i = 0; i <= 10; i++) {
 
 			String cardLine = reader.readLine();
 			if (cardLine.equals("###")) {
 				break;
 			} else {
 				String text = cardLine;
 				Card card = new Card(text, type);
 				cardList.add(card);
 			}
 		}
 		reader.close();
 		return cardList;
 
 	}
 
 }
