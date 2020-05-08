 package heuristics;
 
 import java.util.ArrayList;
 
import Problem.Element;
import Problem.Knapsack;
 
 /**
  * TODO add javadoc
  * @author jorgebg90
  *
  */
 public class TabuSearch extends Heuristic {
 
 	ArrayList<Element> tabuList;
 	
 	private Knapsack bSol;
 	private Knapsack aSol;
 	private Knapsack btSol;
 	
 	private Element tElement;
 	
 	private int iT;
 	
 	
 	/**
 	 * 
 	 * @param kInherited knapsack inherited from agent class
 	 * @param nIter max number of iterations for the execution
 	 * 
 	 */
 	
 	
 	public TabuSearch(int nIter, Knapsack kInherited)
 	{
 	
 		this.bSol=kInherited;
 		this.iT = nIter;
 		
 	}
 
 	@Override
 	public Knapsack executeOnce(Knapsack knapsack) {
 		
 		
 		// TODO Auto-generated method stub
 		
	
 	}
 	
 	public Knapsack TabuSearch ()
 	{
 		
 		//In the first iteration, the best solution is given by the agent
 		btSol = bSol;
 		
 		// tElement contains a random element value from Agent propose
 		tElement = btSol.getNotInsertedElements().get(0); /*Random value*/
 		
 		//It iterates a defined number of steps given by the Agent
 		for(int i=0; i <= iT;i++)
 		{
 			for (int j=0; j<= btSol.getNotInsertedElements().size();j++)
 			{
 				if(tElement.getWeight() >  btSol.getNotInsertedElements().get(j).getWeight())
 				{
 					//The discarded element is added to the tabu list
 					tabuList.add(btSol.getNotInsertedElements().get(j));
 					tElement = btSol.getNotInsertedElements().get(j);
 					
 				}
 				
 				if(btSol.getTotalWeight() + tElement.getWeight() <= bSol.getCapacity()){
 									
 					//The element is swapped from notInsertedElement list to inserted Element list
 					bSol.swapElement();
 					}
 				
 				else{
 					break;
 				}
 				
 				
 			}
 			
 		}
 		
 		
 		return bSol;
 	}
 }
