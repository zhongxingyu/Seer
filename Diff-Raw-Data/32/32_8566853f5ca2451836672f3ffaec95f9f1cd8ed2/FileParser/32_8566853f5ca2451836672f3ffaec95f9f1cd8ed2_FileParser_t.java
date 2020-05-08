 package net.dracolair.games.applestoapples;
 
 import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
 import java.util.LinkedList;
 import java.util.List;
 
 import net.dracolair.games.applestoapples.card.Card;
 import net.dracolair.games.applestoapples.card.CardRenderer;
 
 import static net.dracolair.games.applestoapples.Factories.*;
 
 public class FileParser {
 	
	public static List<Card> loadCardsFromFile(InputStream path, CardRenderer cardRenderer) {
 		List<Card> cards = new LinkedList<Card>();
 		BufferedReader reader = null;
 		
 		try {
			
			reader = new BufferedReader(new InputStreamReader(path));
 			String line;
 			while((line=reader.readLine()) != null) {
 				String[] sp = line.split(" - ", 2);
 				cards.add(CARD(sp[0], sp[1], cardRenderer));
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return cards;
 	}
 	
 }
