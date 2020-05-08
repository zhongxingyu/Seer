 package pokerbots.packets;
 
 import pokerbots.utils.HandEvaluator;
 
 public class GameAction {
 	public String actionType;
 	public int minBet; //optional
 	public int maxBet; //optional
 	public int cardToDiscard; //optional
 
 	public GameAction(String input){
		String[] values = input.split(":");
 		String actionType = values[0];
 
 		// bet or raise
		if (values.length == 3){
			minBet = Integer.parseInt(values[1]);
			maxBet = Integer.parseInt(values[2]);
 		}
 
 		//discard
		else if (values.length == 2){
			cardToDiscard = HandEvaluator.stringToCard(values[1]);
 		}
 	}
 }
