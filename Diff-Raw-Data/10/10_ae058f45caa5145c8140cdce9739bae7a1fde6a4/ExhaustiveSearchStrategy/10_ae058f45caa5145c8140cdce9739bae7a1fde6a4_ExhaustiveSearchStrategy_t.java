 package is.ru.gapl.strategy;
 
 import is.ru.gapl.model.ActionNodePair;
 import is.ru.gapl.model.Node;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.palamedes.gdl.core.model.IGameNode;
 import org.eclipse.palamedes.gdl.core.model.IGameState;
 import org.eclipse.palamedes.gdl.core.model.IMove;
 import org.eclipse.palamedes.gdl.core.simulation.strategies.AbstractStrategy;
 
 public class ExhaustiveSearchStrategy extends AbstractStrategy {
 
 	private Node searchTreeRoot;
 	private ArrayList<Node> fringe;
 	private String roleName;
 	
     /**
 	 * @param searchTreeRoot
 	 * @param fringe
 	 */
 	public ExhaustiveSearchStrategy() {
 		
 		super();
 		this.searchTreeRoot = null;
 		this.fringe			= new ArrayList<Node>();
 	}
 	
 	@Override
 	public IMove getMove(IGameNode currentNode) {
 		
 		IMove[] previousMoves = currentNode.getMoves();
 		if(previousMoves != null) {
 			
 			this.searchTreeRoot = new Node();
 			this.searchTreeRoot.setState(currentNode.getState());
 			this.searchTreeRoot.setParentNode(null);
 			this.searchTreeRoot.setScore(-1);
 			this.fringe.add(this.searchTreeRoot);
 		}else {
 			Node startNode = new Node();
 			startNode.setState(currentNode.getState());
 			startNode.setParentNode(null);
 			startNode.setScore(-1);
 			this.fringe.add(startNode);
 			this.searchTreeRoot = startNode;
 			this.roleName = game.getRoleNames()[playerNumber];
 		}
 		// TODO Auto-generated method stub
 		return solve(searchTreeRoot);
 	}
 
 	private IMove solve(Node searchTR) {
 		
 		expandTree(fringe, match.getPlayTime());
 		return bestMove(searchTR);
 	} 
 	
 	private void expandTree(ArrayList<Node> fringe, int playTime) {
 		
		long endTime = System.currentTimeMillis()+ (1000 * playTime -2500);

 		while(System.currentTimeMillis() < endTime && !fringe.isEmpty()) {
 			
 			Node node = fringe.remove(fringe.size()-1);
 			if(node.getScore() == -1) {
 				List<Node> expandNodes = expand(node);
 				fringe.addAll(expandNodes);
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * @param node
 	 * @return
 	 */
 	private IMove bestMove(Node node) {
 		
 		int maxScore = 0;
 		ArrayList<ActionNodePair> actionList = node.getActionList();
 
 
			ActionNodePair actionNode = actionList.remove(0);
 			IMove bestAction = actionNode.getAction();
            maxScore = maxScore(actionNode.getNode());
 			
 			for(ActionNodePair aNP : actionList) {
 				
 				int score = maxScore(aNP.getNode());
 				if(score == 100) { return aNP.getAction(); }
 				if(score > maxScore) { 
 					maxScore = score;
 					bestAction = aNP.getAction();
 				}
 			}
 		
 		return bestAction;
 	}
 	
 	/**
 	 * 
 	 * @param node
 	 * @return
 	 */
 	private int maxScore(Node node) {
 		
 		if (node.getScore() > -1) { return node.getScore();}
 		if(node.getActionList().isEmpty()) { return -1;}
 		
 		int maxScore = 0;
 		for(ActionNodePair aNP : node.getActionList()) {
 			
 			int score = maxScore(aNP.getNode());
 			if(score == 100 || score == -1) { return score;}
 			if(score > maxScore) { maxScore = score;}
 		}
 		return maxScore;
 	}
 
 	/**
 	 * 
 	 * 
 	 * @param node
 	 * @return
 	 */
 	private ArrayList<Node> expand(Node node) {
 		
 		ArrayList<Node> nodeList				= new ArrayList<Node>();
 		ArrayList<ActionNodePair> actionList	= new ArrayList<ActionNodePair>();
 		Node childNode = null;
 		try {
 			IMove[] moves = game.getReasoner().getLegalMoves(roleName, node.getState());
 			
 			for (IMove move : moves) {
 				
 				IMove[] singleMove = new IMove[1];
 				singleMove[0] = move;
 			
 				IGameState newState	= game.getReasoner().getNextState(node.getState(), singleMove);
 				childNode		= new Node();
 				childNode.setState(newState);
 				childNode.setParentNode(null);
 				childNode.setScore(-1);
 
 				if(newState.isTerminal()) { 
 					childNode.setScore(game.getReasoner().getGoalValue(roleName, newState));
 				}
 				
 				nodeList.add(childNode);
 				ActionNodePair newACP = new ActionNodePair();
 				newACP.setAction(singleMove[0]);
 				newACP.setNode(childNode);
 				actionList.add(newACP);
 			}	
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		//node.getActionList().add(new ActionNodePair(null,null));
 		node.setActionList(actionList);
 		
 		return nodeList;
 	}
 }
