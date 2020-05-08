 /**
  * A maze solver that uses a stack
  */
 package mazeSolve;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Stack;
 
 import mazeElements.*;
 
 /**
  * @author JKidney
  *
  */
 public class StackMazePathFinder extends MazePathFinder {
 
 	/**
 	 * @param myMaze
 	 */
 	public StackMazePathFinder(Maze myMaze) 
 	{
 		super(myMaze);
 	}
 
 	
 	/* (non-Javadoc)
 	 * @see mazeSolve.MazeSolver#findPath()
 	 */
 	@Override
 	public MazePath findPath()
 	{
 		MazePath foundPath = new MazePath();
 		ArrayList<MazeEntity> visited = new ArrayList<MazeEntity>(0);
 	    MazeEntity startLocation = myMaze.getStartLoc();
 	    MazeEntity endLocation = myMaze.getEndLoc();
 	    MazeEntity nextChoice = null;
 	    Stack<StackElement> stackPath = new Stack<StackElement>();
 	     
 	    stackPath.add(new StackElement(startLocation, myMaze));
 	    foundPath.add(startLocation);
 	    visited.add(startLocation);
 	    stackPath.get(0).printBiases();
	    while (!foundPath.get(foundPath.size() - 1).isEndLocation() && foundPath.size() > 0)
 	    {
 	    	nextChoice = stackPath.get(stackPath.size() - 1).nextStep(visited);
 	    	
 	    	if (nextChoice == null)
 	    	{
 	    		visited.add(foundPath.get(foundPath.size() - 1));
 	    		foundPath.removeLast();
 	    		stackPath.remove(stackPath.size() - 1);
 	    	}
 	    	else
 	    	{
 	    		foundPath.add(nextChoice);
 	    		stackPath.add(new StackElement(nextChoice, myMaze));
 	    	}
 	    }
 	    
 	    optimizePath(foundPath);
 		return foundPath;
 	}
 	
 	private void optimizePath(MazePath foundPath)
 	{
 		int index = foundPath.size() - 1; //get last element in the path found
 		ArrayList<MazeEntity> currentOpenLocs;
 		int lowestIndexFound = -1;
 		
 		for (; index > 0; index--)
 		{
 			currentOpenLocs = myMaze.getOpenLocationsAround(foundPath.get(index));
 			for (int i = 0; i < foundPath.size() && lowestIndexFound == -1; i++)
 			{
 				if (currentOpenLocs.contains(foundPath.get(i)))
 					lowestIndexFound = i;
 			}
 			
 			if (lowestIndexFound != -1)
 			{
 				while (foundPath.get(lowestIndexFound + 1) != foundPath.get(index))
 					if (foundPath.get(lowestIndexFound + 1) != myMaze.getEndLoc())
 					{
 						foundPath.remove(foundPath.get(lowestIndexFound + 1));
 						index--;
 					}
 				lowestIndexFound = -1;
 			}
 		}		
 	}
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 	 
