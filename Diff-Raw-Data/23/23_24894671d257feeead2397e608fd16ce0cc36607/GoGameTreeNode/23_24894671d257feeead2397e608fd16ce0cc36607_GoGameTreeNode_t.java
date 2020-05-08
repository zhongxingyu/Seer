package goplayer;
 import java.util.Hashtable;
 import java.util.Random;
 
 public class GoGameTreeNode {
 
 	GoGameTreeNode parent = null;
 	GoGame myGame;
 	Hashtable<String, GoGameTreeNode> children;
 	double gamesAtThisPoint = 0;
 	double bWins = 0;
 
 	Random myRandom = new Random();
 
 	GoGameTreeNode(GoGame game) {
 		myGame = game;
 
 	}
 
 	public GoGameTreeNode playMove(GoMove theMove) {
 
 		if (children == null)
 			children = new Hashtable<String, GoGameTreeNode>();
 		if (children.containsKey(theMove.toString())) {
 			return children.get(theMove.toString());
 		}
 
 		// have to make new game
 
 		GoGame nextGame = myGame.move(theMove);
 		GoGameTreeNode nextTreeNode = new GoGameTreeNode(nextGame);
 		children.put(theMove.toString(), nextTreeNode);
 		nextTreeNode.parent = this;
 		return nextTreeNode;
 
 	}
 
 	// decide which move to explore in tree
 	synchronized int  decideNextMove() {
 		// System.out.println( "trying best move");
 		double bestMoveValue = -100;
 		int bestMove = -1;
 		double logOfParentGames = Math.log(this.gamesAtThisPoint);
 
 		for (int i = 0; i < myGame.legalMoves.size(); ++i) {
 			double trials = 0;
 
 			// System.out.println( "move i" + i);
 			// myLegalMoves.get(i).printMove();
 
 			double winningPct = 0;
 
 			if (children.containsKey(myGame.legalMoves.get(i).toString())) {
 				trials = children.get(myGame.legalMoves.get(i).toString()).gamesAtThisPoint;
 				winningPct = (children.get(myGame.legalMoves.get(i).toString()).bWins / trials);
 			} else {
 				System.out.println("KABLAH");
 				return -1;
 			}
 
 			winningPct = myGame.isBsTurn ? winningPct : -winningPct;
 
 			// System.out.println( "winning pct " +winningPct + " other term " +
 			// .4 * Math.sqrt( .2* logOfParentTrials/ trials) );
 			double moveValue = winningPct + 10
 					* Math.sqrt(logOfParentGames / trials);
 
 			// System.out.println( "moveValue" + moveValue);
 			if (moveValue > bestMoveValue) {
 				bestMoveValue = moveValue;
 				bestMove = i;
 			}
 		}
 		return bestMove;
 
 	}
 
 	public synchronized boolean addTreeLeaves() {
 		if (children != null)
 			return false;
 		synchronized (myGame) {
 			// System.out.println("bottom of tree adding new leaves");
 			children = new Hashtable<String, GoGameTreeNode>();
 			// will later add fastgo code here
 			// FastGo fastGame = new FastGo(myGame);
 			double overallResult = 0;
 			for (GoMove move : myGame.legalMoves) {
 				// System.out.println("adding " + move.toString());
 				GoGame nextGame = myGame.move(move);
 				// nextGame.printBoard();
 				// FastGo nextGame = fastGame.clone();
 				// nextGame.move(move);
 				GoGame testGame = nextGame.clone();
 				double result = testGame.playRandomGame();
 				GoGameTreeNode nextNode = new GoGameTreeNode(nextGame);
 				nextNode.gamesAtThisPoint++;
 				nextNode.bWins += result;
 				children.put(move.toString(), nextNode);
 
 				overallResult += result;
 
 			}
 
 			passResultUp(myGame.legalMoves.size(), overallResult);
 			childrenReady = true;
 			return true;
 		}
 	}
 
 	boolean childrenReady = false;
 	public void playOneSimulation() {
 		if (children == null) // ie have reached bottom of game tree
 		{
 			addTreeLeaves();
 
 			return;
 
 		}
 		while(!childrenReady)
 		{
 			try {
 				Thread.sleep(100);
 			} catch (InterruptedException e) {
 				return;
 			}
 			
 		}
 
 		if (myGame.legalMoves.size() == 0) {
 			myGame.printBoard();
 			throw new Error();
 		}
 		GoMove nextMove = myGame.legalMoves.get(decideNextMove());
 
 		children.get(nextMove.toString()).playOneSimulation();
 
 	}
 
 	// update all upstream games of the results at the bottom
 	public void passResultUp(double games, double results) {
 		gamesAtThisPoint += games;
 		bWins += results;
 		if (parent != null) {
 			parent.passResultUp(games, results);
 		}
 	}
 
 	public GoMove bestMoveSoFar() {
 		double totalGames = 0;
 		if (children == null)
 			throw new Error("asked for best move with no sims");
 		GoMove bestSoFar = null;
 		double bestValueSoFar = -100000;
 		System.out.println("finding best move so far");
 		for (GoMove move : myGame.legalMoves) {
 			// System.out.println("trying " + move.toString());
 			GoGameTreeNode node = children.get(move.toString());
 			if(node == null) continue;
 			double bW = node.bWins;
 			double value = bW / node.gamesAtThisPoint;
 			if (!myGame.isBsTurn)
 				value = -value;
 			 System.out.println("value of "+ move.toString() + " " + value + " " + bW + " "  + node.gamesAtThisPoint);
 			 totalGames +=node.gamesAtThisPoint;
 			if (value > bestValueSoFar && node.gamesAtThisPoint > 2) {
 				bestValueSoFar = value;
 				bestSoFar = move;
 			}
 		}
 		System.out.println("value is " + bestValueSoFar + " totalGames" + totalGames);
 		if(bestSoFar == null) return new GoMove(0,0);
 		return bestSoFar;
 	}
 }
