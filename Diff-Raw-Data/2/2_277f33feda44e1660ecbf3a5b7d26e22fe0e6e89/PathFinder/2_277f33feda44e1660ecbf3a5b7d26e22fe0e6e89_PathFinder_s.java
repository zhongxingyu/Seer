 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.LinkedList;
 
 
 public class PathFinder {
 	// works backwards and forwards to find a path between endtray and start tray
 	// has two linkedlists to store moves possible
 	// has two hashsets to keep track of what moves have already been examined
 	// has a path arrayList for constructing the solution
 	
 	String myTrayCode;
 	LinkedList<Tray> startTrays;
 	LinkedList<Tray> endTrays;
 	
 	HashSet<Tray> prevStartTrays;
 	HashSet<Tray> prevEndTrays;
 	
 	ArrayList<Tray> path;
 	
 	public PathFinder(Tray startTray, Tray endTray)
 	{
 		// takes in Tray representing initial state, tray representing goal
 		// adds startTray to Linklist startTrays, endTray to LinkList endTrays
 		// initializes the rest of the instance variables
 		startTrays = new LinkedList<Tray>();
 		startTrays.add(startTray);
 		
 		endTrays = new LinkedList<Tray>();
 		endTrays.add(endTray);
 		
 		prevStartTrays = new HashSet<Tray>();
 		prevEndTrays = new HashSet<Tray>();
 		
 		path = new ArrayList<Tray>();
 	}
 	
 	public String [] solution() throws NoAnswerException
 	{
 		while(true)
 		{
 			//Debugging
 			System.out.println("Running");
 			System.out.println(startTrays.size());
 			System.out.println(endTrays.size());
 			
 			//Inefficient as FUCK!
 			//iterates through all of startTray and endTray, looking for a match
 			//if a match is found, return path
 			for(Tray startTray : prevStartTrays)
 			{
 				for(Tray endTray : prevEndTrays)
 				{
 					if(startTray.equals(endTray))
 					{
 						System.out.print("Found");
 						return findPath(startTray,endTray);
 					}
 				}
 			}
 			
 			//initialize new fringes
 			LinkedList<Tray> startFringe = new LinkedList<Tray>();
 			LinkedList<Tray> endFringe = new LinkedList<Tray>();
 			//add all possible moves to startfringe
 			while(startTrays.size()!=0)
 			{
 				prevStartTrays.add(startTrays.peek());
 				
 				/*
 				if(startTrays.peek().equals(endTrays.peek()))
 				{
 					return findPath(startTrays.pop(),endTrays.pop());
 				}
 				
 				if(endTrays.contains(startTrays.peek()))
 				{
 					System.out.print("Found!!!");
 					return findPath(startTrays.pop(),endTrays.pop());
 				}
 				if(startTrays.contains(endTrays.peek()))
 				{
 					System.out.print("Found!!!");
 					return findPath(startTrays.pop(),endTrays.pop());
 				}
 				*/
 				
 				//getMoves Returns iterator
 				startTrays.pop().getMoves(startFringe);
 			}
 			//add all possible moves to end fringe (going backwards)
 			while(endTrays.size()!=0)
 			{
 				prevEndTrays.add(endTrays.peek());
 				
 				/*
 				if(endTrays.peek().equals(startTrays.peek()))
 				{
 					return findPath(startTrays.pop(),endTrays.pop());
 				}
 				
 				if(startTrays.contains(endTrays.peek()))
 				{
 					System.out.print("Found!!!");
 					return findPath(startTrays.pop(),endTrays.pop());
 				}
 				if(endTrays.contains(startTrays.peek()))
 				{
 					System.out.print("Found!!!");
 					return findPath(startTrays.pop(),endTrays.pop());
 				}
 				*/
 				
 				//getMoves Returns collection
 				endTrays.pop().getMoves(endFringe);
 			}
 
 			//Can make this more efficient
 			startFringe.removeAll(prevStartTrays);
 			//removes all things stored in hashtables
 			startTrays = startFringe;
 			
 			//Can make this more efficient
 			endFringe.removeAll(prevEndTrays);
 			
 			endTrays = endFringe;
 		}
 	}
 	
 	public String [] solution2() throws NoAnswerException
 	{
 		while(true)
 		{
 			//Debugging
 			/*
 			System.out.println("Running");
 			System.out.println(startTrays.size());
 			System.out.println(endTrays.size());
 			*/
 
 			//Inefficient as FUCK!
 			//iterates through all of startTray and endTray, looking for a match
 			//if a match is found, return path
 			for(Tray startTray : prevStartTrays)
 			{
 				for(Tray endTray : prevEndTrays)
 				{
					if(startTray.equals(endTray))
 					{
 						System.out.print("Found");
 						return findPath(startTray,endTray);
 					}
 				}
 			}
 
 			//initialize new fringes
 			LinkedList<Tray> startFringe = new LinkedList<Tray>();
 			LinkedList<Tray> endFringe = new LinkedList<Tray>();
 			//add all possible moves to startfringe
 			while(startTrays.size()!=0)
 			{
 				prevStartTrays.add(startTrays.peek());
 				startTrays.pop().getMoves(startFringe);
 			}
 
 			//Can make this more efficient
 			startFringe.removeAll(prevStartTrays);
 			//removes all things stored in hashtables
 			startTrays = startFringe;
 		}
 	}
 
 	public String[] findPath(Tray toStart, Tray toFinish) {
 		
 		//For Debugging
 		/*
 		int [][] boardStart = toStart.myBoardState;
 		for(int[] arraySrt : boardStart)
 		{
 			for(int intergerSrt : arraySrt)
 			{
 				System.out.print(intergerSrt);
 			}
 			System.out.println();
 		}
 		System.out.println();
 		
 
 		int [][] boardEnd = toFinish.myBoardState;
 		for(int[] arrayEnd : boardEnd)
 		{
 			for(int intergerEnd : arrayEnd)
 			{
 				System.out.print(intergerEnd);
 			}
 			System.out.println();
 		}
 		System.out.println();
 		
 		System.out.println(toStart.equals(toFinish));
 		*/
 		
 		path.add(toStart);
 		findPathBack(toStart);
 		findPathForward(toFinish);
 		// creates string array with cell for each tray in the path
 		// store string returned by Tray.movemade of each move made
 		String[] rtnPath = new String[path.size()];
 
 		for (int i = 0; i < path.size()-1; i++) {
 			rtnPath[i] = path.get(i).moveMade(path.get(i + 1));
 			
 			//For Debugging
 			/*
 			int [][] board = path.get(i).myBoardState;
 			for(int[] array : board)
 			{
 				for(int interger : array)
 				{
 					System.out.print(interger);
 				}
 				System.out.println();
 			}
 			System.out.println();
 			*/
 			
 		}
 
 		return rtnPath;
 	}
 
 	public void findPathBack(Tray t) {
 		//recursively uses myPreviousTray to follows trays back to origin
 		Tray prevT = t.myPreviousTray;
 
 		if (prevT != null) {
 			path.add(0, prevT);
 			findPathBack(prevT);
 		}
 	}
 
 	public void findPathForward(Tray t) {
 		//recursively uses myPreviousTray to follows trays back to origin
 		Tray prevT = t.myPreviousTray;
 
 		if (prevT != null) {
 			path.add(prevT);
 			findPathForward(prevT);
 		}
 	}
 }
