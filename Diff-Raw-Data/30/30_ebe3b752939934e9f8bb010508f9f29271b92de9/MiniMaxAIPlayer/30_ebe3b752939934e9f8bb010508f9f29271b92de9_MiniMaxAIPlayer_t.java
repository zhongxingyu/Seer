 package polarizedladder;
 
 import java.util.Iterator;
 
 import tree.Node;
 
 //for this to work the Parent must keep a pointer?? to the child with minimum or max value??
 public class MiniMaxAIPlayer {
 
 	Board maxBoard;
 	Board minBoard;
 
 	public Board getMaxMove(Node<Board> n){
 		
 		int max =  Integer.MIN_VALUE;
 		int maxtemp = Integer.MIN_VALUE;//must be some other number???
     
         
 		Iterator<Node<Board>> it = n.getChildren().iterator();
 		maxBoard =it.next().getData(); 
 		max = maxBoard.getHeuristic();
 		//System.out.println("h  from minimax " + maxBoard.getHeuristic());		
 
 		while( it.hasNext() )
 		{
             Board	boardTemp = it.next().getData();
 			maxtemp =  boardTemp.getHeuristic();
 			if(maxtemp != 0){
 			if(maxtemp >= max){
 				 maxBoard = boardTemp;
 				 max = maxtemp;
 				 }
 		    }
 			n.getData().setHeuristic(max);
 			maxBoard.setHeuristic(max);
 			
 		}
 		//maxBoard = n.getData();
 		//System.out.println("h  from max " + n.getData().getHeuristic());
 		return maxBoard;
 	}
 
 	public Board getMinMove(Node<Board> n){
 
 		int min =  Integer.MAX_VALUE;
 		int mintemp = Integer.MAX_VALUE;
        //we need to shallow the search at the end of the game ie depth at 3 points = 2 at 2 = 1 etc so....
 		Iterator<Node<Board>> it = n.getChildren().iterator();
 		minBoard =it.next().getData(); 
 		min = minBoard.getHeuristic();
 
 		while( it.hasNext() )
 		{
 			Board	boardTemp = it.next().getData();
 			mintemp =  boardTemp.getHeuristic();
 			if(mintemp <= min){
 				minBoard = boardTemp;
 			    min = mintemp;	
 			}
 			n.getData().setHeuristic(min);
 			minBoard.setHeuristic(min);
 			//System.out.println("h  from min " + minBoard.getHeuristic());
 		}
 		//System.out.println("h  from min " + n.getData().getHeuristic());
 		return minBoard;
 
 	}
 
 }
