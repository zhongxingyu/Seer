 package poker;
 
 public class Human extends Player {
 	
 	
 	public int getBlind(boolean big){
 		Poker.sleep(500);
 		int blind;
 		if (big)
 			blind = Poker.BIG_BLIND;
 		else
 			blind = Poker.BIG_BLIND / 2;
 				changeCash(-blind); 
 				speak("puts $" + blind + " forward as blind.");
 				currentBid = blind;
 				window.redrawScore();
 				return blind;
 	}
 	
 	
 	public int getBid(int past){
 		int bid = 0;
 		Poker.sleep(500);
 		while (bid < past){
 			window.print("Please enter your bid");
 			window.print("You need to bid " + (past - currentBid) + " in order to call");
 			Poker.setLock(this);
 			synchronized (Poker.getLock()){
 				try {
 					wait();
 				} catch (InterruptedException e) {
					e.printStackTrace();
 				}
 			}
 			if ("folding".equals(Poker.dropbox)){
 				speak("folds");
 				folding = true;
 				return 0;
 			}
 			
 			if ("call".equals(Poker.dropbox)){
 				bid = past - currentBid;
 				speak("calls the bid by putting in "+ bid);
 				changeCash(-bid);
 				currentBid += bid;
 				return bid;
 			}
 			
 			bid = (int) Poker.dropbox;
 		}
 		changeCash(-bid);
 		currentBid += bid;
 		return bid;
 	}
 	
 	public void showHand(){
 		System.out.println(getName() + "'s cards are");
 		for (int i = 0; i < getHand().length(); i++){
 			System.out.print( (i+1) + ": " );
 			getHand().getCard(i).display();
 		}
 	}
 	
 }
