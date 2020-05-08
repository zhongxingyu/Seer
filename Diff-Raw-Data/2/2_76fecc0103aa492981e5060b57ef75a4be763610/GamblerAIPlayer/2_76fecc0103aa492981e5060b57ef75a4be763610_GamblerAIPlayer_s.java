 package greedGame.model.player;
 
 import greedGame.model.Dice;
 import greedGame.model.GreedGameModel;
 
 import java.util.List;
 
 
 
 public class GamblerAIPlayer extends AIPlayer {
 	
 	//the class constructor.
 	public GamblerAIPlayer(String name, GreedGameModel model) {
 		super(name, model);
 	}
 	
 	@Override
 	public void decide() {
 		selectAllCombinations(); //selects the dice to keep or bank.
 		
		List<Dice> diceList = getUnselectedDice(); //gets the unreserved dice and puts it in a local list.
 		
 			// as long as the gambler still has 2 dice or more left he will roll again
 			if(diceList.size() >=2 && getScore() < 10000)
 			{
 				setDecision(AIDecision.KEEP_ROLLING);
 			}
 			else
 			{
 				setDecision(AIDecision.BANK);
 			}
 		
 		//end of while loop
 
 	}
 
 }
