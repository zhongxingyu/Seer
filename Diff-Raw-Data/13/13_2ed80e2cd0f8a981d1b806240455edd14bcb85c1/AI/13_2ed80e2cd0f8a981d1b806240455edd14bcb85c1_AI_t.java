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
 	private double aggressivess = rand.nextDouble() ;
 	
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
		//sees if too much to continue
		if (past > 10 * currentScore() + 30){
			speak("folds");
			folding = true;
			return 0;
		}
		
 		int howmuch=0;
 		double reccurance = 1.0;
 		if (lastRound == game.getRound()){
 			reccurance -= 1;
 		}
 		else {
 			lastRound = game.getRound();
 		}
 		if (lastScore != currentScore() && timesBid > 0){
 			timesBid -= 1;
 			lastScore = currentScore();
 		}
 		reccurance -= ((double)(timesBid )) / 1.5;
 		
 		double probability = (currentScore()-1.0) / 5.0;
 		probability *= reccurance;
 		probability *= aggressivess + .1;
 		if (rand.nextDouble() < probability){
 			//increase bid
 			howmuch = past - currentBid + (int) (rand.nextDouble() * aggressivess * currentScore() * 8);
 			timesBid +=1;
 		}
 		else {
 			//call
 			howmuch = past - currentBid;
 			
 		}
 		if (howmuch > cash) {
 			howmuch = cash;
 			allIn = true;
 		}
 		if (allIn){
 			speak("goes all-in");
 		}
 		else if (howmuch == past - currentBid){
 			//call message
 			speak("calls the bid by putting in "+ howmuch);
 		}
 		else {
 			//bid message
 			speak("bids "+ howmuch);
 		}
 		
 		changeCash(-howmuch);
 		currentBid += howmuch;
 		return howmuch;
 		
 	}
 	
 
 }
