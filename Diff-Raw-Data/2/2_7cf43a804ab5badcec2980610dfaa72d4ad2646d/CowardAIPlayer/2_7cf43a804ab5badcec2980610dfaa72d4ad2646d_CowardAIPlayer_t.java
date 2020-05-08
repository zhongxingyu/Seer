 package greedGame.model.player;
 
 import java.util.List;
 
 import greedGame.model.Dice;
 import greedGame.model.GreedGameModel;
 import greedGame.model.ScoringCombination;
 import greedGame.model.ScoringRules;
 
 public class CowardAIPlayer extends AIPlayer {
 
 	public CowardAIPlayer(String name, GreedGameModel model) {
 		super(name, model);
 	}
 	
	public void decide()
 	{
 		rollDice();
 		List<Dice> diceList = getUnreservedDice();
 		ScoringRules rules = getScoringRules();
 		List<ScoringCombination> combinations = rules.getScoringCombinations(diceList);
 		
 		for(ScoringCombination forCombo : combinations)
 		{
 			if(forCombo.getScore() > 0)
 			{
 				for(Dice forDice : diceList)
 				{
 					selectDice(forDice);
 				}
 			}
 		}
 		bank();
 	}
 }
