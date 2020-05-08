 package pokerbots.packets;
 
 import pokerbots.utils.HandEvaluator;
 
 public class GameAction {
 	public String actionType;
 	public int minBet; //optional
 	public int maxBet; //optional
 	public int cardToDiscard; //optional
 
 	public GameAction(String input){
		String[] values = input.split(" ");
 		String actionType = values[0];
		System.out.println("WHAT? " + actionType);
 
 		// bet or raise
		if (values.length > 1){
			String[] bets = values[1].split(":");
			minBet = Integer.parseInt(bets[0]);
			maxBet = Integer.parseInt(bets[1]);
 		}
 
 		//discard
		else if (actionType.contains(":")){
			String[] words = actionType.split(":");
			actionType = words[0];
			cardToDiscard = HandEvaluator.stringToCard(words[1]);
 		}
 	}
 }
