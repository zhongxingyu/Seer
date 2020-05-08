 package pokerbots.packets;
 
 import pokerbots.utils.HandEvaluator;
 
 public class GetActionObject{
 
 	// GETACTION potSize numBoardCards [boardCards] numLastActions [lastActions] numLegalActions [legalActions] timebank
 
 	public int potSize;
 	public int[] boardCards;
 	public String[] lastActions;
 	public GameAction[] legalActions;
 	public float timebank;
 
 	
 	public GetActionObject(String input){
 		String[] values = input.split(" ");
 		
 		potSize = Integer.parseInt(values[1]);
 
 		int i = 2;
		boardCards = new int[Integer.parseInt(values[i])];
 		for (int j=0; j<boardCards.length; j++){
 			boardCards[j] = Integer.parseInt(values[i+j]);
 		}
 
 		//TODO: parse first/second player actions?
 		i += boardCards.length + 1;
		lastActions = new String[Integer.parseInt(values[i])];
 		for (int j=0; j<lastActions.length; j++){
 			lastActions[j] = values[i+j];
 		}
 
 		i += lastActions.length + 1;
		legalActions = new GameAction[Integer.parseInt(values[i])];
 		for (int j=0; j<legalActions.length; j++){
 			legalActions[j] = new GameAction(values[i+j]);
 		}
 
 		timebank = Float.parseFloat(values[i + legalActions.length + 1]);
 	}
 }
