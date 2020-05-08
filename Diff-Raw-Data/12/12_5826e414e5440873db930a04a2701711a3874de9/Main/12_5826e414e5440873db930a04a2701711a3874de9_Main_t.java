 package manager;

 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 
 import cards.Deck;
import cards.Pile;
 
 public class Main {
 
 	private Main(){};
 	
 	public static void main(String[] args) {
 		
 		Random r = new Random();
 		
 		List<Pile> piles = new LinkedList<Pile>();
 		for(int i = 0; i < 1000000; i++){
 			Pile deck = Deck.fullDeck();
 			deck.shuffle();
 			piles.add(deck.deal(r.nextInt(3) + 5));
 		}
 		
 		long start,stop;
 		System.out.println("**Start HandRating**");
 		start = System.currentTimeMillis();
 		for(Pile pile : piles){
 			try{
 				HandRating.rate(pile);
 			} catch (Exception e){
 				System.out.println(pile);
 				e.printStackTrace();
 			}
 		}
 		stop = System.currentTimeMillis();
 		long hrTime = stop - start;
 		
 		System.out.println("**Start CardPower**");
 		start = System.currentTimeMillis();
 		for(Pile pile : piles){
 			try{
 				Utility.calcCardPower(pile);
 			} catch (Exception e){
 				System.out.println(pile);
 				e.printStackTrace();
 			}
 		}
 		stop = System.currentTimeMillis();
 		long cpTime = stop - start;
 		
 		System.out.printf("hrTime: %d s, cpTime: %d s%n",hrTime/1000,cpTime/1000);
 		
 	}
 }
