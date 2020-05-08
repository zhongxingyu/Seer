 package mazeSolve;
 
 import java.util.ArrayList;
 
 import mazeElements.*;
 
 /**
  * <p>This will be the class to hold our StackElements. Each StackElement
  * will contain a MazeEntity that will hold its location as well as
  * an ArrayList<MazeEntity> that will hold the open positions around
  * it. </p>
  * <p>When created, the list will be ranked. That is, preferred elements
  * will be determined and ranked earlier in the list accordingly.
  * Each time an open position is taken, we will remove it from the
  * open locations list.</p>
  * @author Ryan
  *
  */
 public class StackElement {
 	private MazeEntity location;
 	private ArrayList<MazeEntity> rankedLocations;
 	
 	private boolean biasNorth; //these are boolean controllers for our ranking
 	private boolean biasSouth; //algorithm. A given path choice is ranked based
 	private boolean biasWest;  //on how much closer it will move towards the end
 	private boolean biasEast;  //location.
 	
 	/**
 	 * 
 	 */
 	public StackElement(MazeEntity loc, Maze myMaze) {
 		// TODO Auto-generated constructor stub
 		this.location = loc;
 		setBiases(myMaze.getEndLoc()); //set our biases based on where the end loc is
 		rankPathChoices(myMaze);
 	}
 
 	/**
 	 * 
 	 * @param end This is the end entity of the Maze. Used for setting our directional
 	 * movement biases.
 	 */
 	private void setBiases(EndEntity end)
 	{
 		//initialize our biases
 		biasNorth = false;
 		biasSouth = false;
 		biasWest = false;
 		biasEast = false;
 		
 		//set our biases to true if they match any of the conditions
 		if (location.getRow() > end.getRow())
 			biasNorth = true;
 		else if (location.getRow() < end.getRow())
 			biasSouth = true;
 		
 		if (location.getCol() > end.getCol())
 			biasWest = true;
		else if (location.getRow() < end.getCol())
 			biasEast = true;
 		
 	}
 	
 	public void printBiases()
 	{
 		System.out.println("North = " + biasNorth + "  South = " + biasSouth);
 		System.out.println("West = " + biasWest + "  East = " + biasEast);
 	}
 	
 	//TODO OPTOMIZE this ordering
 	private void rankPathChoices(Maze myMaze) {
 		ArrayList<MazeEntity> temp = myMaze.getOpenLocationsAround(location);
 		rankedLocations = new ArrayList<MazeEntity>(0);
 		boolean added = false;
 		EndEntity end = myMaze.getEndLoc();
 		
 		rankedLocations.add(temp.get(0));
 		
 		for (int i = 1; i < temp.size(); i++)
 		{
 			for (int ind = 0; ind < rankedLocations.size() && !added; ind++)
 			{
 				if ( stepVal(temp.get(i), end) > stepVal(rankedLocations.get(ind), end))
 				{
 					rankedLocations.add(ind, temp.get(i));
 					added = true;
 				}
 			}
 			if (!added)
 				rankedLocations.add(temp.get(i));
 			
 			added = false;
 		}
 	}
 	
 	/**
 	 * 
 	 * @param visited This is the list of elements already visited for tracking purposes
 	 * @return
 	 */
 	public MazeEntity nextStep(ArrayList<MazeEntity> visited)
 	{
 		MazeEntity choice = null;
 		boolean choiceFound = false;
 		
 		while(rankedLocations.size()  > 0 && !choiceFound)
 		{
 			choice = rankedLocations.get(0);
 			
 			if (visited.contains(choice))
 			{
 				rankedLocations.remove(0);
 				choice = null;
 			}
 			else
 			{
 				choiceFound = true;
 				visited.add(choice);
 				rankedLocations.remove(0);
 			}
 		}
 		//System.out.println(choice);
 		return choice;
 	}
 	
 	public String toString()
 	{
 		return location.toString();
 	}
 	
 	
 	/** NOT IMPLEMENTED YET
 	 * 
 	 * @param pathChoice
 	 * @return
 	 */
 	private int stepVal(MazeEntity pathChoice, EndEntity end)
 	{
 		int choiceVal = 0;
 		boolean movesRowCloser = false;
 		boolean movesColCloser = false;
 		
 		if(pathChoice.getCol() > location.getCol() && biasEast)
 		{
 			choiceVal++;
 			movesColCloser = true;
 		}
 		else if (pathChoice.getCol() < location.getCol() && biasWest)
 		{
 			choiceVal++;
 			movesColCloser = true;
 		}
 		if (pathChoice.getRow() > location.getRow() && biasSouth)
 		{
 			choiceVal++;
 			movesRowCloser = true;
 		}	
 		else if (pathChoice.getRow() < location.getRow() && biasNorth)
 		{
 			choiceVal++;
 			movesRowCloser = true;
 		}
 		
 		if (pathChoice.getRow() == end.getRow() && movesRowCloser)
 		{
 			choiceVal += 2;
 		}
 		
 		if (pathChoice.getCol() == end.getCol() && movesColCloser)
 		{
 			choiceVal +=2;	
 		}
 		
 		if (pathChoice.getRow() > location.getRow() && biasNorth)
 			choiceVal--;
 		else if (pathChoice.getRow() < location.getRow() && biasSouth)
 			choiceVal--;
 		
 		if (pathChoice.getCol() > location.getCol() && biasWest)
 			choiceVal--;
 		else if (pathChoice.getCol() < location.getCol() && biasEast)
 			choiceVal--;
 		
 		if (movesRowCloser && movesColCloser)
 			choiceVal += 10;
 		
 		if( pathChoice.isEndLocation())
 			choiceVal += 50;
 		
 		return choiceVal;
 	}
 }
