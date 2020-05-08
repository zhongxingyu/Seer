 package polarizedladder;
 
 import java.awt.Point;
 import java.util.Iterator;
 
 import tree.Tree;
 import tree.Node;
 
 public class AIPlayer extends Player{
     
 	private char AIPlayerToken;
 	private String AIPlayerString;
 	private String OpponentString;
 	private int maxDepth;
 	private MiniMaxAIPlayer minimax;
     
 	private LadderPatternStrategy heuristics;
 	
 	private AIPlayer aip;
 	private Player p;
     
 	private Board nextBoard;
 	
 	public AIPlayer(){
 		
 		super();
 	}
 	
 	public AIPlayer(String playerName, char playerToken, int discs, Board board) {
         
 		super.playerName  = playerName;
 		super.playerToken = playerToken;
 		super.discs 	  = discs;
 		this.board        = board;
 		nextBoard		  = new Board();
 		heuristics 		  = new LadderPatternStrategy();
         minimax = new MiniMaxAIPlayer();
 		AIPlayerToken 	  = playerToken;
 		AIPlayerString 	  = String.valueOf(AIPlayerToken);
 		
 		System.out.println("AIPlayer " + playerName);
 	}
     
 	public Point doAIPlayerTurn(AIPlayer ai, Player p, SearchLists searchList){
 		
 		Point discCoordinates;
         
 		this.aip  			= ai;
 		this.p 	  			= p;
 		this.OpponentString = String.valueOf(p.playerToken);
 		
 		
 		try
 		{
 			discCoordinates = aip.move(this.board, searchList);
 			searchList.removePoint(discCoordinates, searchList);
 			return discCoordinates;
 		}
 		catch (ArrayIndexOutOfBoundsException aiob)
 		{
 			System.out.println("Invalid move.");
 			return doPlayerTurn(ai);
 		}
 		catch(NumberFormatException nf)
 		{
 			System.out.println("Invalid move.");
 			return doPlayerTurn(ai);
 		}
         
 	}
     
 	private Point move(Board board, SearchLists searchList)
 	{
 		// local variables
 		int startTreeDepth = 1;
        if(board.getBoardEmptySpaces() > 32){
         	maxDepth = 2;
         	System.out.println("Depth : " + maxDepth);
         }
        else if(board.getBoardEmptySpaces() > 22){  
         maxDepth = 3;
         System.out.println("Depth : " + maxDepth);
         }
         
        else{
         	
         	maxDepth = 4;
         	System.out.println("Depth : " + maxDepth);
         }
         // create new tree with board
 		Tree<Board> searchTree = createTree(board);
         
         // generate all potential next moves
         createStateSpace(searchTree.getRoot(), searchList, startTreeDepth, AIPlayerString, board);
         
         return nextBoard.getLastBoardPosition();
 
 	}
 	
 	public Tree<Board> createTree(Board board)
 	{
         // create new tree with board
         Tree<Board> searchTree = new Tree<Board>(board);
         Node<Board> root       = new Node<Board>();
         
         searchTree.setRoot(root);
         root.setData(board);
         
         return searchTree;
 	}
 	
 
 	
 	public void createStateSpace(Node<Board> parentNode, SearchLists searchList, int depthOfTree, String currPlayer, Board oldState)
 	{
 		// generate all potential next moves
         Iterator<Point> openPoints = searchList.getIterator();														
       
         // and shared with sub-trees.
 		if (depthOfTree == maxDepth)
 		{
 			while (openPoints.hasNext())
 			{
 				// prepare new board
 				Board newBoard = new Board();
 				newBoard.setState(oldState.cloneArray());
                 
 				// generate next move
 				Point nextPoint 	  = openPoints.next();
 				Position nextPosition = new Position(nextPoint, currPlayer);							// get next move
 			
 				if ( newBoard.setObjectPosition(nextPosition) )
 				{	
 					newBoard.setHeuristic(heuristics.calculate( (Player) aip, this.p, newBoard) );
 				//	newBoard.printBoard();
 					// calculate next move heuristics (at leaves only)
 				//	System.out.println("H from building tree " + newBoard.getHeuristic());
 					// add next move child node to tree
 					Node<Board> nextChild = new Node<Board>();
 					nextChild.setParent(parentNode);
 					nextChild.setData(newBoard);
 					parentNode.addChild(nextChild);
 					
 				}
 			//	else
 			//	{
 			//		System.out.println("not created" );
 			//	}
 			}
 		
 	    	miniMaxMove(parentNode, depthOfTree);	// minimax
 
 		}
 	
 		else
 		{
 			while (openPoints.hasNext())
 			{
 				// prepare new board
 				Board newBoard = new Board();
 				newBoard.setState(oldState.cloneArray());
 				
 				// generate next move
 				Point nextPoint 	  = openPoints.next();
 				Position nextPosition = new Position(nextPoint, currPlayer);							// get next move
 	
 				if ( newBoard.setObjectPosition(nextPosition) )
 				{
 					//newBoard.printBoard();
 					
 					// add next move child node to tree
 					Node<Board> nextChild = new Node<Board>();
 					nextChild.setParent(parentNode);
 					nextChild.setData(newBoard);
 					parentNode.addChild(nextChild);
 				}
 			}
 
         	for (Node<Board> childNode : parentNode.getChildren())
         	{
         		// populate child sub-tree
         		String nextToken = (currPlayer == AIPlayerString) ? OpponentString : AIPlayerString;
         		
         		createStateSpace(childNode, searchList, depthOfTree + 1, nextToken, childNode.getData());
         	}
 		}
 	}
 
 public void miniMaxMove(Node<Board> parentNode, int depthOfTree){
 	
 	if(depthOfTree == 1)
 	{
 		Board maxBoard = minimax.getMaxMove(parentNode);
 	    parentNode.setData(maxBoard);
 	    nextBoard = maxBoard;
 	   // System.out.println("MiniMax Best Move " + maxBoard.getHeuristic() + " - depth - " + depthOfTree);
 	    return;
 	}
 	      //if depth % 2 == 1 then max
 		if(depthOfTree%2 == 1){
 			
 			Board maxBoard = minimax.getMaxMove(parentNode);
 			//maxBoard.printBoard();
 			parentNode.setHeuristic(maxBoard.getHeuristic());
 			
 		//	System.out.println("MiniMax Max Move " + maxBoard.getHeuristic() + " - depth - " + depthOfTree); // + parentNode.getChildren().size() + " leaf nodes." );
 		//	System.out.println("Created: " + parentNode.getChildren().size() + " leaf nodes." );
 			miniMaxMove(parentNode.getParent(), depthOfTree-1);
 		
 		} else if(depthOfTree%2 == 0){
 			
 			Board minBoard = minimax.getMinMove(parentNode);
 		//	minBoard.printBoard();
 			
 			parentNode.setHeuristic(minBoard.getHeuristic());
 		//	System.out.println("MiniMax Min Move " + minBoard.getHeuristic() + " " + depthOfTree); // + parentNode.getChildren().size() + " leaf nodes." );
 		//	System.out.println("Created: " + parentNode.getChildren().size() + " leaf nodes." );
 		    miniMaxMove(parentNode.getParent(), depthOfTree-1);
 		}
 	
 }
 
 public int getMaxDepth() {
 	return maxDepth;
 }
 
 public void setMaxDepth(int maxDepth) {
 	this.maxDepth = maxDepth;
 }
 
 
 }
