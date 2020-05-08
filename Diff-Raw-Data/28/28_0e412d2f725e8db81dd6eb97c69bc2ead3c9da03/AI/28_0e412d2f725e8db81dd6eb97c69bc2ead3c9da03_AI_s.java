 package poker;
 
 import java.util.Random;
 
 
 /**
  * AI class for five card poker
  * 
  * @author Paul Steele
  *
  */
 public class AI extends Player{
 	
 	private Random rand = new Random();
 	private int lastRound =-1; //the last round bet on
 	public AI(Poker game){
 		super(game);
 	}
 	private double aggressivess = 3;
 	
 	public int getBlind(boolean big) throws InterruptedException{
 		Poker.sleep(500);
 		Poker.sleep(1500);
 		int ret;
 		int blind;
 		if (big)
 			blind = Poker.BIG_BLIND;
 		else
 			blind = Poker.BIG_BLIND / 2;
 		if (getCash() > blind){
 			speak("puts $" + blind + " forward as blind.");
 			changeCash(-blind);
 			currentBid = blind;
 			ret = blind;
 		}
 		else {
 			folding = true;
 			speak("folds");
 			ret = 0;
 		}
 		
 		return ret;
 	}
 	
 	public int getBid(int past) throws InterruptedException{
 		Poker.sleep(500);
 		Poker.sleep(750);
 		int howmuch=0;
 		double reccurance = 1.0;
 		if (lastRound == game.getRound()){
 			reccurance -= 1;
 		}
 		else {
 			lastRound = game.getRound();
 		}
 		
 		double probability = (currentScore()-1.0);
 		probability *= reccurance;
 		probability *= aggressivess;
 		if (rand.nextDouble() < probability){
 			//increase bid
 			howmuch = past - currentBid + 10;
			speak("bids "+ howmuch);
 		}
 		else {
 			//call
 			howmuch = past - currentBid;
 			speak("calls the bid by putting in "+ howmuch);
 		}
 		changeCash(-howmuch);
 		currentBid += howmuch;
 		return howmuch;
 		
 	}
 	
 
 }
