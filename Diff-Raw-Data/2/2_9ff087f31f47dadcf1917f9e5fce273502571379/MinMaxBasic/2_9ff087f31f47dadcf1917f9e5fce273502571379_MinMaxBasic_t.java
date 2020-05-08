 package pope.minmax;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import pope.interfaces.moveEvaluator;
 
 import fi.zem.aiarch.game.hierarchy.Engine;
 import fi.zem.aiarch.game.hierarchy.Move;
 import fi.zem.aiarch.game.hierarchy.Side;
 import fi.zem.aiarch.game.hierarchy.Situation;
 
 /**
  * 
  * Classic MinMax adversarial search Algorithm with tree memorization.
  * 
  * @author Sami Airaksinen
  */
 
 public class MinMaxBasic implements moveEvaluator 
 {	
 	public static Engine game;
 	
 	public static Side side;
 	
 	private GameTreeNode gameTree;
 		
 	public MinMaxBasic(Situation root)
 	{
		gameTree = new GameTreeNode(root);
 	}
 		
 	@Override
 	public Move getBesMove(Situation state, Integer timeLimit) {
 		try {
 			return minMaxDecision(state);
 		} catch (Exception e) {			
 			e.printStackTrace();
 			//FALLBACK MODE
 			return state.legal().get(0); 
 		}
 	}
 	
 	/**
 	 * @param state current State of The game.
 	 * @return best possible move to MaxPlayer (player that has turn)
 	 * @throws Exception 
 	 */
 	public Move minMaxDecision(Situation state) throws Exception
 	{
 		Integer value = maxValue(gameTree);
 		
 		Situation bestNextSitutation;		
 		for (GameTreeNode current : gameTree.getChilds()) 
 		{
 			if (current.value == value)
 			{
 				bestNextSitutation = current.state;
 				return bestNextSitutation.getPreviousMove();
 			}
 		}
 		return null;
 	}
 	
 	private Integer maxValue(GameTreeNode node) throws Exception
 	{
 		if (terminalTest(node.state)) 
 		{
 			return utility(node.state);
 		}
 		
 		Integer value = Integer.MIN_VALUE;
 				
 		for (Situation current : nextPossibleStates(node.state) ) 
 		{
 			GameTreeNode nextNode = new GameTreeNode(current);
 			value = Math.max(value, minValue(nextNode));
 			nextNode.value = value;
 			node.addChild(nextNode);
 		}
 		return value;
 	}
 	
 	private Integer minValue(GameTreeNode node) throws Exception
 	{
 		if (terminalTest(node.state)) 
 		{
 			return utility(node.state);
 		}
 		
 		int value = Integer.MAX_VALUE;
 		
 		for (Situation current : nextPossibleStates(node.state) ) 
 		{
 			GameTreeNode nextNode = new GameTreeNode(current);
 			value = Math.min(value, maxValue(nextNode));
 			nextNode.value = value;
 			node.addChild(nextNode);
 		}
 		return value;
 	}
 	
 	private Integer utility(Situation state) throws Exception
 	{
 		Integer util;
 		if (state.getWinner() == side)
 		{
 			util = 1;
 		}
 		else if (state.getWinner() == side.opposite())
 		{
 			util = -1;
 		}
 		else if (state.getWinner() == Side.NONE)
 		{
 			util = 0;
 		}
 		else
 		{
 			throw new Exception("Utility function accessed in non-finished state!");
 		}
 		return util;
 	}
 	
 	private Boolean terminalTest(Situation state)
 	{
 		return state.isFinished();
 	}
 	
 	private Collection<Situation> nextPossibleStates(Situation state)
 	{
 		Collection<Situation> nextStates = new ArrayList<Situation>(); 
 		
 		for (Move current : state.legal(state.getTurn())) 
 		{
 			nextStates.add(state.copyApply(current));
 		}		
 		return nextStates;
 	}
 }
