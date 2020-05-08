 package hw2_P2;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 
 
 /**
  * this is an a* based cooperative multi robot motion solver
  * nice
  * @author tim tregubov
  * for: CS44 W10
  *
  */
 public class RobotSolver
 {
 	public final int searchDepth = 10; //we only search this far per iteration
 
 	public MMap map; //the map object
 	
 	private ArrayList<Map<Loc,Double>> waveFronts;  //generated complete distance maps to use as true heuristic
 	
 	public ArrayList<LinkedList<Loc>> solutions; // the final cooperative paths for display
 	public LinkedList<MLoc> mSolutions; // the final cooperative paths for display
 
 		
 	private Set<TimeLoc> blocked;  //this holds the paths that are currently blocked
 	
 	private Random random;
 	
 	private Boolean[] terminalStops; // records finishes per robot index
 		
 	/**
 	 * constructor, initializes the robots and some maps. 
 	 */
 	public RobotSolver(MMap m)
 	{
 		map = m;
 		
 		waveFronts = new ArrayList<Map<Loc,Double>>();
 		solutions = new ArrayList<LinkedList<Loc>>();
 		mSolutions = new LinkedList<MLoc>();
 		blocked = new HashSet<TimeLoc>();
 		terminalStops = new Boolean[map.numRobots];
 		random = new Random();
 							
 		// build our wavefront plans!
 		for (int i = 0; i < map.numRobots; i++)
 		{
 			waveFronts.add(astar2d(map.starts[i], map.finishes[i]));
 			solutions.add(new LinkedList<Loc>()); //add empty lists
 		}
 		
 		astarMulti(map.starts, map.finishes);
 		
 		// separate out the result states into separate robot moves
 		for (int i = 0; i < mSolutions.size(); i++)
 		{
 			for (int j = 0; j < map.numRobots; j++)
 			{
 				solutions.get(j).add(mSolutions.get(i).locs[j]);
 			}
 		}
 		
 		//print em out
 		for (int i=0; i< map.numRobots; i++)
 		{
 			System.out.println("robot: " + i + " moves: " + solutions.get(i).toString());
 		}
 		
 
 //		//figure out who should plan first, farthest distance goes first
 //		Node[] SD = new Node[map.numRobots];
 //		for (int i = 0; i < map.numRobots; i++)
 //		{
 //			 solutions.add(new LinkedList<Loc>());  //fill in empty
 //			 SD[i] = (new Node(heuristic(map.starts[i], i), i)); //get starting distance from wavefront
 //		}
 //		
 //		
 //		Arrays.sort(SD); //sort them by distance
 //			
 //		//run a search for each bot
 //		int minDepth = 0;
 //		for (int i = map.numRobots -1 ; i >= 0; i--)
 //		{
 //				int bot = SD[i].rid;
 //				System.out.println("bot goes: " + bot + " minDepth: " + minDepth);
 //				astar3d(map.starts[bot], map.finishes[bot], bot, minDepth);
 //				minDepth = Math.max(solutions.get(i).size(), minDepth); //update to the length of path
 //		}		
 	}
 	
 
 	private void astarMulti(Loc[] fs, Loc[] ff)
 	{
 		MLoc s = new MLoc(fs);
 		MLoc f = new MLoc(ff);
 		
 		int turnKeeper = 0;
 		
 		MPriorityHashQueue frontier = new MPriorityHashQueue(); //states to look at
 		Map<MLoc,Double> explored = new HashMap<MLoc,Double>();   //explored locations
 		
 		LinkedList<MLoc> solution = new LinkedList<MLoc>(); //the solutions
 		
 		//TODO: do I need a parent if I'm resuming from a partial solution?
 		frontier.add(new MNode(s, null, 0 + heuristic(s))); //add the first robot state
 		
 		//run the search
 		while (!frontier.isEmpty())
 		{
 			MNode current = frontier.poll(); //chooses the lowest cost node in frontier
 									
 			// build plan if goal and we've planned enough steps to cover longest running bot
 			if (goalTest(current.state, f))
 			{
 				for (MNode node = current; node != null; node = node.parent)
 				{
 					solution.addFirst(node.state);  //add path nodes to solution
 				}
 				System.out.println("found goal!");
 				mSolutions.addAll(solution);
 				return;
 			}
 			
 			explored.put(current.state, current.distance);
 			
 			ArrayList<MLoc> possibles = getMoves(current.state, turnKeeper);
 			
 			for (int i=0; i < possibles.size(); i++) // for all the children
 			{
 				MLoc possib = possibles.get(i); 
 								
 				// if we haven't explored it yet
 				// add it to frontier with a distance
 				if (! explored.containsKey(possib))
 				{
 					frontier.add(new MNode(possib, current, current.distance + 1  + heuristic(current.state)));
 				}
 				else // if we've explored it check that its not at a lower cost now
 				{
 					MNode n = frontier.get(current.state);
 					if (n != null && n.distance > current.distance)// + 1)
 					{
 						frontier.update(current, new MNode(possib, current, current.distance + 1  + heuristic(current.state)));
 					}
 				}
 			}
			turnKeeper = (turnKeeper + 1) % map.numRobots;
 		}
 		if (solution.isEmpty())
 		{
 			System.out.println("FAILED TO FIND SOLUTION");
 		}
 	}	
 	
 	
 	/**
 	 * does an a* search, using the true heuristic and the builds/looks at a
 	 * blocked list of other robots to prevent run ins
 	 * @param strt is start
 	 * @param f is finish
 	 * @param robotindex the index of the current robot
 	 * @return 
 	 */
 	private void astar3d(Loc strt, Loc f, int robotindex, int minDepth)
 	{
 		TimeLoc s = new TimeLoc(strt.x, strt.y, 0); //make the start at 0 time
 		int depth = 0;   //depth level counter
 		
 		TPriorityHashQueue frontier = new TPriorityHashQueue(); //states to look at
 		Map<Loc,Double> explored = new HashMap<Loc,Double>();   //explored locations
 		
 		LinkedList<Loc> solution = new LinkedList<Loc>(); //this robots solution
 				
 		//TODO: do I need a parent if I'm resuming from a partial solution?
 		frontier.add(new TimeNode(s, null, 0 + heuristic(s, robotindex))); //add the first robot state
 		
 		//run the search
 		while (!frontier.isEmpty())
 		{
 			TimeNode current = frontier.poll(); //chooses the lowest cost node in frontier
 			
 			if (goalTest(new Loc(current.state), f))
 			{
 				System.out.println("got to goal! " + current.state);
 			}
 			
 			// build plan if goal and we've planned enough steps to cover longest running bot
 			if (goalTest(new Loc(current.state), f)  && current.state.t >= minDepth)
 			{
 				terminalStops[robotindex] = true; //note that we've finished
 				
 				for (TimeNode node = current; node != null; node = node.parent)
 				{
 					solution.addFirst(new Loc(node.state));  //add path nodes to solution
 					blocked.add(node.state); //add nodes so others don't try to go here
 					blocked.add(new TimeLoc(node.state.x, node.state.y, node.state.t + 1));  //to prevent swaps
 				}
 				System.out.println("bot: " + robotindex + " length: " + current.state.t + " current path:" + solution.toString());
 				System.out.println("blocked list: " + blocked);
 				solutions.get(robotindex).addAll(solution);
 				return;
 			}
 			
 			explored.put(new Loc(current.state), current.distance);
 			
 			ArrayList<TimeLoc> possibles = getMoves(current.state, robotindex);
 			
 			for (int i=0; i < possibles.size(); i++) // for all the children
 			{
 				TimeLoc possib = possibles.get(i); 
 				
 				//if we are paused on the goal just chill
 				if (goalTest(new Loc(current.state), f) && goalTest(new Loc(possib), f))
 				{
 					System.out.println("pausing is OK!");
 					//frontier.add(new TimeNode(possib, current, current.distance + 0 ));
 					frontier.add(new TimeNode(possib, current, current.distance + 1  + heuristic(current.state, robotindex)));
 				}
 				// if we haven't explored it yet
 				// add it to frontier with a distance
 				else if (! explored.containsKey(new Loc(possib)))
 				{
 					frontier.add(new TimeNode(possib, current, current.distance + 1  + heuristic(current.state, robotindex)));
 				}
 				else // if we've explored it check that its not at a lower cost now
 				{
 					TimeNode n = frontier.get(current.state);
 					if (n != null && n.distance > current.distance)// + 1)
 					{
 						frontier.update(current, new TimeNode(possib, current, current.distance + 1  + heuristic(current.state, robotindex)));
 					}
 				}
 			}
 			depth++;
 		}
 		if (solution.isEmpty())
 		{
 			System.out.println("FAILED TO FIND PATH FOR BOT: " + robotindex);
 		}
 	}
 	
 	
 	
 	/**
 	 * build wavefront heuristic
 	 * instead of returning the solution here though, we return the explored graph
 	 * which is now going to be the best heuristic
 	 * remember to goes backwards from finish to start
 	 * @param s is start
 	 * @param f is finish
 	 * @return a hashset of Nodes of all locations (hashed by location state)
 	 */
 	private Map<Loc,Double> astar2d(Loc s, Loc f)
 	{
 		
 		PriorityHashQueue frontier = new PriorityHashQueue();
 		Map<Loc,Double> explored = new HashMap<Loc,Double>();		
 		
 		frontier.add(new Node(f, null, 0 + manhattan(f, s))); //add the first robot state
 		
 		while (!frontier.isEmpty())
 		{
 			Node current = frontier.poll(); //chooses the lowest cost node in frontier
 			
 			//NOTE we do not backtrack and make a solution here
 			//NOTE we also don't return once we've found the solution
 			//NOTE instead we consider all options exhaustively
 			//NOTE to create a wavefront planned heuristic
 			
 			explored.put(current.state, current.distance);
 			
 			ArrayList<Loc> possibles = getMoves(current.state);
 			
 			for (int i=0; i < possibles.size(); i++)
 			{
 				if (! explored.containsKey(possibles.get(i)))
 				{
 					frontier.add(new Node(possibles.get(i), current, current.distance + 1  + manhattan(current.state, s)));
 				}
 				else
 				{
 					Node n = frontier.get(current.state);
 					if (n != null && n.distance > current.distance + 1)
 					{
 						frontier.update(current, new Node(possibles.get(i), current, current.distance + 1  + manhattan(current.state, s)));
 					}
 				}
 			}
 		}
 		return explored;  
 	}
 	
 	
 	/**
 	 * checks the blocked list 
 	 * @param tl
 	 * @param ri
 	 * @return
 	 */
 	private boolean onBlocked(TimeLoc tl, int ri)
 	{
 		boolean b = blocked.contains(tl);
 		
 		for (int i =0; i< map.numRobots; i++)
 		{
 			if (i != ri)
 			{
 				// check for other terminal positions
 				//TODO: not needed?
 				if (new Loc(tl) == map.finishes[i] && terminalStops[i] !=null && terminalStops[i])
 				{
 					System.out.println("CHECKING: " + tl);
 					b = true;
 				}
 			}
 		}
 		return b;
 	}
 	
 	
 	/**
 	 * gets all the *possible* moves for a robot on the map
 	 * @param r
 	 * @return
 	 * TODO: add checking for other robots!!!
 	 */
 	private ArrayList<Loc> getMoves(Loc r)
 	{
 		ArrayList<Loc> sts = new ArrayList<Loc>();
 		
 		
 		Loc up = new Loc(r.x, r.y - 1);
 		Loc down = new Loc(r.x, r.y + 1);
 		Loc left = new Loc(r.x - 1, r.y);
 		Loc right = new Loc(r.x + 1, r.y);
 		
 		if (r.y > 0  && !map.isCollision(up))
 		{
 			sts.add(up);
 		}
 		if (r.y < map.gridSize -1 && !map.isCollision(down))
 		{
 			sts.add(down);
 		}
 		
 		if (r.x > 0 && !map.isCollision(left))
 		{
 			sts.add(left);
 		}
 		
 		if (r.x < map.gridSize -1  && !map.isCollision(right))
 		{
 			sts.add(right);
 		}
 		return sts;		
 	}
 	
 	
 	private ArrayList<Loc> getMovesWithPause(Loc r)
 	{
 		ArrayList<Loc> sts = getMoves(r);
 		
 		sts.add(r); //include pause
 		
 		return sts;		
 	}
 	
 	
 	/**
 	 * get all the *possible* moves with time in mind
 	 * available to populate the graph
 	 * this includes a pause action
 	 * @param r
 	 * @return
 	 */
 	private ArrayList<TimeLoc> getMoves(TimeLoc r, int ri)
 	{
 		ArrayList<Loc> sts = getMoves(new Loc(r));
 		
 		ArrayList<TimeLoc> tsts = new ArrayList<TimeLoc>();
 		
 		for (int i = 0; i < sts.size(); i++)
 		{
 			TimeLoc n = new TimeLoc(sts.get(i), r.t + 1);
 			if (!onBlocked(n, ri))  //checks for time block
 			{
 				tsts.add(n);
 			}
 		}
 		
 		// create a pause action
 		// check if its blocked
 		TimeLoc pauseState = new TimeLoc(r.x, r.y, r.t + 1 );
 		if (!onBlocked(pauseState, ri))
 		{
 			tsts.add(new TimeLoc(r.x, r.y, r.t + 1 )); //also have a pause action!
 		}
 		else
 		{
 			System.out.println("pause action blocked! " + pauseState);
 		}
 		
 		return tsts;
 	}
 	
 	
 	public ArrayList<MLoc> getMoves(MLoc ml, int r)
 	{
 		ArrayList<Loc> ls = getMovesWithPause(ml.locs[r]);
 				
 		ArrayList<MLoc> mls = new ArrayList<MLoc>();
		
 		for (int i = 0; i < ls.size(); i++)
 		{
 			if (!colTest(ls.get(i), ml, r))
 			{
 				MLoc nml = ml.clone();
 				nml.locs[r] = ls.get(i);
 				mls.add(nml);
 			}
 		}
 		return mls;
 		
 	}
	
 	
 	private boolean colTest(Loc l, MLoc ml, int r)
 	{
 		boolean b = true;
 		for (int i = 0; i < map.numRobots; i++)
 		{
 			if (r != i)
 			{
 				b = b && l.equals(ml.locs[i]);
 			}
 		}
 		return b;
 	}
 	
 		
 	/**
 	 * tests if we're at goal state
 	 * @param r
 	 * @return
 	 */
 	private boolean goalTest(Loc r, Loc g)
 	{
 		return r.equals(g);
 	}
 
 	private boolean goalTest(MLoc r, MLoc g)
 	{
 		return r.equals(g);
 	}
 	
 	
 	/**
 	 * manhattan distance heuristic in squares from the goal
 	 * 
      * It is optimistic because it is the shortest distance (it doesn't consider obstacles, non-diagonal moves, or other robots).
      * Thus it is either exactly correct or the actual path was longer and so it is guaranteed to be optimistic.
 	 * @param r the robot position
 	 * @param t the target position (robot object holding coordinates)
 	 * @return
 	 */
 	private int manhattan(Loc r, Loc t)
 	{
 		return Math.abs((t.x - r.x) + (t.y - r.y));
 	}
 	
 	
 	/**
 	 * distance heuristic from the wavefront plan given
 	 * @param r the location to test
 	 * @param i the index of the robot
 	 * @return the distance value recorded
 	 */
 	private double heuristic(TimeLoc r, int i)
 	{
 		return heuristic(new Loc(r.x, r.y), i);
 	}
 	
 	private double heuristic(Loc r, int i)
 	{
 		return waveFronts.get(i).get(r);
 	}
 	
 	private double heuristic(MLoc r)
 	{
 		double cost = 0;
 		for (int i =0; i< map.numRobots; i++)
 		{
 			cost = cost + waveFronts.get(i).get(r.locs[i]); //sum of distances from wavefront
 		}
 		return cost;
 	}
 	
 	
 	/**
 	 * checks if all robots have finished
 	 * @return
 	 */
 	private boolean terminalStopCheck()
 	{
 		int c = 0;
 		for (int i = 0; i < terminalStops.length; i++)
 		{
 			if (terminalStops[i] != null && terminalStops[i])
 			{
 				c = c + 1;
 			}
 
 		}
 		if (c == map.numRobots)
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 
 }
