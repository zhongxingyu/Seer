 package bwmcts.mcts.uctcd;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Random;
 
 import bwmcts.sparcraft.EvaluationMethods;
 import bwmcts.sparcraft.Game;
 import bwmcts.sparcraft.GameState;
 import bwmcts.sparcraft.Players;
 import bwmcts.sparcraft.StateEvalScore;
 import bwmcts.sparcraft.UnitAction;
 import bwmcts.sparcraft.players.Player;
 import bwmcts.sparcraft.players.Player_NoOverKillAttackValue;
 
 public class UCTCD {
 
 	private double K = 1.6f;
 	private int maxChildren = 20;
 	private int maxPlayerIndex = 0;
 	private int minPlayerIndex = 1;
 	private boolean debug;
 	private int simulationSteps;
 	
 	public UCTCD() {
 		
 	}
 
 	public UCTCD(double k, int maxChildren, int minPlayerIndex,
 			int maxPlayerIndex, int simulationSteps, boolean debug) {
 		super();
 		this.K = k;
 		this.maxChildren = maxChildren;
 		this.minPlayerIndex = minPlayerIndex;
 		this.maxPlayerIndex = maxPlayerIndex;
 		this.debug = debug;
 		this.simulationSteps = simulationSteps;
 	}
 
 	public List<UnitAction> search(GameState state, long timeBudget){
 		
 		if (maxPlayerIndex == 0 && state.whoCanMove() == Players.Player_Two){
 			return new ArrayList<UnitAction>(); 
 		} else if (maxPlayerIndex == 1 && state.whoCanMove() == Players.Player_One){
 			return new ArrayList<UnitAction>(); 
 		}
 		
 		Date start = new Date();
 		
 		UctNode root = new UctNode(null, NodeType.ROOT, new ArrayList<UnitAction>(), maxPlayerIndex);
 		//root.setVisits(1);
 		
 		int t = 0;
 		while(new Date().getTime() <= start.getTime() + timeBudget){
 			
 			traverse(root, state.clone());
 			t++;
 			
 		}
 		System.out.println("Traversals " + (t++));
 		
 		UctNode best = mostVisitedChildOf(root);
 		
 		if (debug){
 			String out = root.print(0);
 			writeToFile(out, "tree.xml");
 		}
 		
 		return best.getMove();
 		
 	}
 
 	private float traverse(UctNode node, GameState state) {
 		
 		float score = 0;
 		if (node.getVisits() == 0){
 			updateState(node, state, true);
			score = evaluate(state.clone());
 		} else {
 			updateState(node, state, false);
 			if (state.isTerminal()){
				score = evaluate(state.clone());
 			} else {
 				if (!node.isFullyExpanded())
 					generateChildren(node, state);
 				score = traverse(selectNode(node), state);
 			}
 		}
 		node.setVisits(node.getVisits() + 1);
 		node.setTotalScore(node.getTotalScore() + score);
 		return score;
 	}
 
 	private float evaluate(GameState state) {
 		
 		// get the players
 	    Player p1 = new Player_NoOverKillAttackValue(Players.Player_One.ordinal());
 	    Player p2 = new Player_NoOverKillAttackValue(Players.Player_Two.ordinal());
 
 	    // contruct the game
 	    Game g=new Game(state, p1, p2, simulationSteps, false);
 	    
 	    // play the game
 	    g.play();
 
 	    // you can access the resulting game state after g has been played via getState
 	    GameState finalState = g.getState();
 	    // you can now evaluate the state however you wish. let's use an LTD2 evaluation from the point of view of player one
 	    StateEvalScore score = finalState.eval(maxPlayerIndex, EvaluationMethods.LTD2);
 
 		return score._val;
 	}
 
 	private void generateChildren(UctNode node, GameState state) {
 		
 		// Figure out who is next to move
 		int playerToMove = getPlayerToMove(node, state);
 		
 		HashMap<Integer, List<UnitAction>> map = new HashMap<Integer, List<UnitAction>>();
 		
 		try {
 			state.generateMoves(map, playerToMove);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		List<UnitAction> move = getNextMove(playerToMove, state, map); // Possible moves?
 		if (move == null)
 			return;
 		
 		if (uniqueMove(move, node)){
			UctNode child = new UctNode(node, getChildNodeType(node, state), move, playerToMove);
 			node.getChildren().add(child);
 		}
 		node.setExpansions(node.getExpansions() + 1);
 
 		if (node.getExpansions() >= maxChildren)
 			node.setFullyExpanded(true);
 		
 	}
 
 	private int getPlayerToMove(UctNode node, GameState state) {
 		
 		if (state.whoCanMove() == Players.Player_Both){
 		
 			if (node.getType() == NodeType.ROOT)
 				
 				return maxPlayerIndex;
 			
 			if (node.getType() == NodeType.FIRST)
 				
 				return state.getEnemy(node.getMovingPlayerIndex());
 			
 			return node.getMovingPlayerIndex();
 			
 		}
 		
 		if (state.whoCanMove() == Players.Player_One)
 			return 0;
 		else if (state.whoCanMove() == Players.Player_Two)
 			return 1;
 		
 		return -1;
 		
 	}
 	
 	private boolean uniqueMove(List<UnitAction> move, UctNode node) {
 
 		if(node.getChildren().isEmpty())
 			return true;
 		
 		for (UctNode child : node.getChildren()){
 			boolean identical = true;
 			if (child.getMove().size() != move.size()){
 				identical = false;
 			} else {
 				for(int i = 0; i < move.size(); i++){
 					if (!child.getMove().get(i).equals(move.get(i))){
 						identical = false;
 						break;
 					}
 				}
 			}
 			if (identical){
 				return false;
 			}
 		}
 		
 		return true;
 		
 	}
 
 	private int enemy(int player) {
 		
 		if (player == minPlayerIndex)
 			return maxPlayerIndex;
 		else if (player == maxPlayerIndex)
 			return minPlayerIndex;
 		
 		return -1;
 	}
 
 	private NodeType getChildNodeType(UctNode parent, GameState prevState) {
 		
 		if(!prevState.bothCanMove()){
 			
 			return NodeType.SOLO;
 			
 		} else { 
 			
 			if (parent.getType() == NodeType.ROOT)
 		
 				return NodeType.FIRST;
 			
 			if (parent.getType() == NodeType.SOLO)
 				
 				return NodeType.FIRST;
 			
 			if (parent.getType() == NodeType.SECOND)
 				
 				return NodeType.FIRST;
 			
 			if (parent.getType() == NodeType.FIRST)
 				
 				return NodeType.SECOND;
 		}
 			
 		return NodeType.DEFAULT;
 	}
 
 	private List<UnitAction> getNextMove(int playerToMove, GameState state, HashMap<Integer, List<UnitAction>> map) {
 		
 		ArrayList<UnitAction> move = new ArrayList<UnitAction>();
 		
 		Random r = new Random();
 		for(Integer i : map.keySet()){
 			
 			// Add random possible action
 			move.add(map.get(i).get(r.nextInt(map.get(i).size())));
 			
 		}
 		
 		return move;
 		
 	}
 
 	private void updateState(UctNode node, GameState state, boolean leaf) {
 		
 		if (node.getType() != NodeType.FIRST || leaf){
 			
 			if (node.getType() == NodeType.SECOND){
 				
 				try {
 					state.makeMoves(node.getParent().getMove());
 				} catch (Exception e) {e.printStackTrace();}
 			}
 			
 			try {
 				state.makeMoves(node.getMove());
 			} catch (Exception e) {e.printStackTrace();}
 			
 			state.finishedMoving();
 			
 		}
 		
 	}
 
 	private UctNode selectNode(UctNode parent) {
 		
 		float bestScore = Float.MAX_VALUE;
 		//.getChildren().get(0)
 		boolean maxPlayer = parent.getMovingPlayerIndex() == maxPlayerIndex;
 		if (maxPlayer)
 			bestScore = -Float.MAX_VALUE;
 			
 		UctNode bestNode = null;
 		for(UctNode child : parent.getChildren()){
 			
 			if (child.getVisits() > 0){
 				
 	            float winRate = child.getTotalScore() / child.getVisits();
 	            float uctVal = (float) (K * Math.sqrt(Math.log(parent.getVisits()) / child.getVisits()));
 	            float currentVal = maxPlayer ? (winRate + uctVal) : (winRate - uctVal);
 	            
 	            child.setUctValue(currentVal);
 				
 			} else {
 				
 				return child;
 				
 			}
 			
 			if (maxPlayer && child.getUctValue() > bestScore){
 				bestScore = child.getUctValue();
 				bestNode = child;
 			}
 			if (!maxPlayer && child.getUctValue() < bestScore){
 				bestScore = child.getUctValue();
 				bestNode = child;
 			}
 			
 		}
 		
 		return bestNode;
 	}
 	
 
 	private void writeToFile(String out, String filename) {
 		// Write to file
 		System.out.println("Ready to write file.");
         FileWriter fw = null;
 		try {
 			File old = new File(filename);
 			if (old.exists()){
 				old.delete();
 				System.out.println(filename + " deleted");
 			}
 			File file = new File(filename);
 			fw = new FileWriter(file);
 			fw.write(out);
 			fw.close();
 			System.out.println(filename + " saved.");
 		} catch (FileNotFoundException e1) {
 			System.out.println("Error saving " + filename + ". " + e1);
 		} catch (IOException e2) {
 			System.out.println("Error saving " + filename + ". " + e2);
 		}
 	}
 
 
 	private UctNode mostVisitedChildOf(UctNode parent) {
 		int mostVisits = -1;
 		UctNode best = null;
 		for(UctNode node : parent.getChildren()){
 			if (node.getVisits()>mostVisits){
 				best = node;
 				mostVisits = node.getVisits();
 			}
 		}
 		return best;
 	}
 
 	public double getK() {
 		return K;
 	}
 
 	public void setK(double k) {
 		K = k;
 	}
 
 	public int getMaxChildren() {
 		return maxChildren;
 	}
 
 	public void setMaxChildren(int maxChildren) {
 		this.maxChildren = maxChildren;
 	}
 
 	public int getMinPlayerIndex() {
 		return minPlayerIndex;
 	}
 
 	public void setMinPlayerIndex(int minPlayerIndex) {
 		this.minPlayerIndex = minPlayerIndex;
 	}
 
 	public int getMaxPlayerIndex() {
 		return maxPlayerIndex;
 	}
 
 	public void setMaxPlayerIndex(int maxPlayerIndex) {
 		this.maxPlayerIndex = maxPlayerIndex;
 	}
 	
 }
