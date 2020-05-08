 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.PriorityQueue;
 import java.util.Queue;
 import java.util.Set;
 import java.util.Stack;
 
 class VectorNode implements Comparable<VectorNode>
 {
 	int distance;
 	P goal;
 	int distanceWalked;
 
 	@Override
 	public int compareTo(VectorNode vectorNode)
 	{
 		if(distance < vectorNode.distance)
 			return -1;
 		if(distance > vectorNode.distance)
 			return +1;
 		return 0;
 	}
 }
 
 public class DanielPlayer2
 {
 	private class N implements Comparable<N>
 	{
 		State state;
 		N parent;
 		int score;
 
 		@Override
 		public int compareTo(N b)
 		{
 			if(score < b.score) return -1;
     		if(score > b.score) return +1;
     		return 0;
 		}
 
 		public void print()
 		{
 			System.err.println("score: " + score);
 			state.Print();
 		}
 	}
 
 	HashMap<State, N> visited = new HashMap<State, N>();
     Queue<N> queue = new PriorityQueue<N>();
 
     HashMap<State, N> reverseVisited = new HashMap<State, N>();
     Queue<N> reverseQueue = new PriorityQueue<N>();
     State startState;
     boolean vectorMapConstruction = true;
 
     Stack<N> stack = new Stack<N>(); //for dfs testing
 
     HashMap<P, Queue<VectorNode>> vectorMap = new HashMap<P, Queue<VectorNode>>();
     Set<P> unsafePositions = new HashSet<P>();
     int static_deadlock = 0;
     int dynamic_deadlock = 0;
 
     private int h0(State s)
     {
     	int score = 0;
 //    	if(vectorMapConstruction)
 //    	{
     		for(P goal : s.goals)
     		{
 				for(P box : s.boxes)
 				{
 					score += State.ManhattanDistance(goal, box);
 				}
     		}
     		return score;
 //    	}
 ////    	for(P goal : s.goals)
 ////    	{
 ////    		if(s.boxes.contains(goal))
 ////    			score += 10;
 ////    	}
 
 //		for(P box : s.boxes){
 //			Queue<VectorNode> q = vectorMap.get(box);
 //			if(q.isEmpty()){//Deadlock check of static deadlocks
 //				score += Integer.MAX_VALUE;
 //			}
 //			score += q.peek().distanceWalked;
 //			//TODO If we have time and energy: check that the heuristic is not for distance to goal with box on it.
 ////			for(VectorNode node : q){
 ////				if(node.goal.)
 ////			}
 //		}
 
 //    	return score;
     }
     //TODO Copy paste the hashmap to make a reverse search between init state of boxes and goals.
     private int ReverseH0(State s)
     {
     	int score = 0;
 
 //    	for(P box : s.boxes)
 //    	{
 //    		if(startState.boxes.contains(box))
 //    			score += 10;
 //    	}
 
     	for(P state_box : s.boxes)
     	{
 			for(P start_box : startState.boxes)
 			{
 				score += startState.ManhattanDistance(state_box, start_box);
 			}
     	}
 
     	//ignore the player position
 
     	return score;
     }
 
     private void BidirectionalAnswer(N forward, N backward, StringBuilder answer)
     {
     	System.err.println("bidirectional answer");
     	N n;
 
     	LinkedList<N> list = new LinkedList();
     	n = forward;
     	while(true)
     	{
     		if(n == null)
     			break;
 
     		list.addFirst(n);
     		n = n.parent;
     	}
 
     	n = backward;
     	while(true)
     	{
     		if(n == null)
     			break;
 
     		list.addLast(n);
     		n = n.parent;
     	}
 
     	N from = null;
     	int counter =0;
     	for(N sequential : list)
     	{
     		sequential.print();
     		if(from != null) //TODO maybe need to make sure that the last step is not included if that is no box push
     		{
     				String pathstring = State.getPath2(from.state, sequential.state);
     				System.err.println(pathstring);
     				answer.append(pathstring);
 
     			
     		}
     		++counter;
     		
 
     		from = sequential;
     	}
     }
 
     public String SearchSolution(State startState)
     {
     	StringBuilder answer = new StringBuilder();
 
     	//forward queue
 		{
 			//add start state
 			this.startState = startState;
 			N startNode = new N();
 			startNode.state = startState;
 			startNode.parent = null;
 			startNode.score = h0(startNode.state);
 			queue.add(startNode);
 		}
 
 		//backward queue
 		{
 			//add all possible end states
 			Collection<State> endings = startState.GetAllPossibleEndings();
 
 			for(State ending : endings)
 			{
 				//ending.Print();
 
 				//dont add thoose that are equivalent to any node in the list already
 				{
 					if(reverseVisited.containsKey(ending))
 						continue;
 
 					reverseVisited.put(ending, null);
 				}
 
 				N endNode = new N();
 				endNode.state = ending;
 				endNode.parent = null;
 				endNode.score = ReverseH0(endNode.state);
 				reverseQueue.add(endNode);
 				//stack.push(endNode);
 				//ending.Print();
 			}
 
 			reverseVisited.clear();
 		}
 
 		int expanded = 0;
 		int limit = Integer.MAX_VALUE;
 
 		while(true)
 		{
 			//forward search----------------------------------------------------------------------
 			if(queue.size() == 0)
 			{
 				System.err.println("no forward solution found");
 				break;
 			}
 
 			{
 				N pn = queue.poll(); //parent node
 				System.err.println("Forward search:");
 				pn.state.Print();
 				visited.put(pn.state, pn);
 				expanded++;
 
 				if(pn.state.isWin())
 				{
 					System.err.println("found forward solution");
 			    	N n;
 
 			    	LinkedList<N> list = new LinkedList();
 			    	n = pn;
 			    	while(true)
 			    	{
 			    		if(n == null)
 			    			break;
 
 			    		list.addFirst(n);
 			    		n = n.parent;
 			    	}
 
 			    	N from = null;
 
 			    	for(N sequential : list)
 			    	{
 			    		if(from != null)
 			    		{
 							answer.append(State.getPath2(from.state, sequential.state));
 			    		}
 
 			    		from = sequential;
 //			    		sequential.print();
 			    	}
 					break;
 				}
 
 				if(reverseVisited.containsKey(pn.state))
 				{
 					//pn.print();
 					//((N)reverseVisited.get(pn.state)).print();
 					BidirectionalAnswer(pn, reverseVisited.get(pn.state), answer);
 					System.err.println("found bidirection solution, from forward search");
 					break;
 				}
 
 				//expanded all child states
 				Collection<State> c = new LinkedList<State>(); //collection
 				//if macro move do that otherwize backup one step and try possible advanced
 				pn.state.PossibleAdvanced(c);
 
 				for(State cs : c) //child node
 				{
 					if(visited.containsKey(cs))
 						continue;
					/*
 					if(unsafePositions.contains(cs.boxMoved))
 					{
 						//System.err.println("Static Deadlock Detected");
 						//cs.Print();
 						static_deadlock++;
 						continue;
 					}
 					
 					
 					
 					if(Deadlock.isDynamicDeadlocks(cs))
 					{
 						//System.err.println("Dynamic Deadlock Detected");
 						//cs.Print();
 						dynamic_deadlock++;
 						continue;
 					}
					*/
 					
 					N cn = new N();
 					cn.state = cs;
 					cn.parent = pn;
 					cn.score = h0(cn.state);
 					queue.add(cn);
 					visited.put(cs, cn);
 					//cn.print();
 				}
 			}
 
 			//reverse search----------------------------------------------------------------------------------------------
 			if(reverseQueue.size() == 0)
 			{
 				System.err.println("no reverse solution found");
 				break;
 			}
 
 			{
 				N pn = reverseQueue.poll(); //parent node
 				//N pn = (N)stack.pop();
 				System.err.println("Backward search:");
 				pn.state.Print();
 				reverseVisited.put(pn.state, pn);
 				expanded++;
 
 				if(pn.state.isWin(startState))
 				{
 					System.err.println("found reverse solution");
 					//pn.print();
 					//startState.Print();
 
 					LinkedList<N> list = new LinkedList();
 					N n;
 			    	n = pn;
 			    	while(true)
 			    	{
 			    		if(n == null)
 			    			break;
 
 			    		list.addLast(n);
 			    		n = n.parent;
 			    	}
 
 			    	N from = null;
 
 			    	for(N sequential : list)
 			    	{
 			    		if(from != null)
 			    		{
 							answer.append(State.getPath2(from.state, sequential.state));
 			    		}
 
 			    		from = sequential;
 			    		//sequential.print();
 			    	}
 			    	break;
 				}
 
 				if(visited.containsKey(pn.state))
 				{
 					//pn.print();
 					//((N)visited.get(pn.state)).print();
 					BidirectionalAnswer(visited.get(pn.state), pn, answer);
 					System.err.println("found bidirection solution, from backward search");
 					break;
 				}
 
 				//expanded all possible child states
 				Collection<State> c = new LinkedList<State>(); //collection
 				pn.state.reversePossibleAdvanced(c);
 
 				for(State cs : c) //child node
 				{
 					if(reverseVisited.containsKey(cs))
 						continue;
 
 					N cn = new N();
 					cn.state = cs;
 					cn.parent = pn;
 					cn.score = ReverseH0(cn.state);
 					reverseVisited.put(cs, cn);
 					reverseQueue.add(cn);
 					//stack.push(cn);
 					//cn.print();
 				}
 			}
 		}
 
 		System.err.println("queue size: " + queue.size());
 		System.err.println("reverse queue size: " + reverseQueue.size());
 		System.err.println("expanded nodes: " + expanded);
 		System.err.println("deadlocks detected static:" + static_deadlock + " dynamic:" + dynamic_deadlock);
 		/*
 		while(queue.size() != 0)
 		{
 			N p = (N)queue.poll();
 			System.err.println("score: " + p.score);
 		}
 		*/
 
 
 		return answer.toString();
     }
 
 	public void play(State startState)
 	{
 //
 //		State empty = startState.copyState();
 //		empty.boxes.clear();
 //		empty.goals.clear();
 //		LinkedList<P> reachables = new LinkedList<P>();
 //		HashSet<P> bfvisited = new HashSet<P>();
 //		Queue<P> queuebf = new LinkedList<P>();
 //		queuebf.add(empty.player);
 //
 //		while(!queuebf.isEmpty())
 //		{
 //			P playerCurrent = queuebf.poll();
 //			bfvisited.add(playerCurrent);
 //
 //			for(P direction : adjacent_lrud)
 //			{
 //				P playerNext = new P(playerCurrent.x + direction.x, playerCurrent.y + direction.y);
 //
 //				if(empty.walls.contains(playerNext))
 //					continue;
 //				if(bfvisited.contains(playerNext))
 //					continue;
 //
 //				reachables.add(playerNext);
 //				queuebf.add(playerNext);
 //			}
 //		}
 //
 //		for(P box : reachables)
 //		{
 //			Queue<VectorNode> thisBoxQueue = new PriorityQueue<VectorNode>();
 //
 //			for(P goal : startState.goals)
 //			{
 //				//solve simplified sub problem
 //				State subState = empty.copyState();
 //				subState.boxes.add(box);
 //				subState.goals.add(goal);
 //
 ////				String answer = SearchSolution(subState);
 ////
 ////				if(answer == null)
 ////					continue;
 //
 //				Queue<VectorNode> queuebfs = new PriorityQueue<VectorNode>();
 //				bfvisited.clear();
 //				VectorNode goalNode = null;
 //				VectorNode firstNode = new VectorNode();
 //				firstNode.distance = State.ManhattanDistance(box, goal);
 //				firstNode.goal = box;
 //				firstNode.distanceWalked = 0;
 //				queuebfs.add(firstNode);
 //
 //				while(!queuebfs.isEmpty())
 //				{
 //					//OBS! vectornode goal is where you are currently
 //					VectorNode next = queuebfs.poll();
 //					bfvisited.add(next.goal);
 //
 //					if(next.goal.equals(goal))
 //					{
 //						goalNode = next;
 //					}
 //
 //					for(P direction : adjacent_lrud)
 //					{
 //						P nextPosition = new P(next.goal.x + direction.x, next.goal.y + direction.y);
 //
 //						if(empty.walls.contains(nextPosition))
 //							continue;
 //						if(bfvisited.contains(nextPosition))
 //							continue;
 //
 //						VectorNode child = new VectorNode();
 //						child.distance = State.ManhattanDistance(nextPosition, goal);
 //						child.goal = nextPosition;
 //						child.distanceWalked = next.distanceWalked +1;
 //						queuebfs.add(child);
 //					}
 //				}
 //
 //				if(goalNode != null){
 //					VectorNode nodeForMap = new VectorNode();
 //					nodeForMap.goal = goal;
 //					nodeForMap.distance = goalNode.distanceWalked;
 //					thisBoxQueue.add(nodeForMap);
 //				}
 //			}
 //
 //			vectorMap.put(box, thisBoxQueue);
 //		}
 //
 //		System.err.println("Check vectorMap:");
 //		System.err.println("Size of vectormap for this pos: " + vectorMap.get(new P(4, 3)).size());
 //		PriorityQueue<VectorNode> pq = (PriorityQueue<VectorNode>)vectorMap.get(new P(4, 3));
 //
 //		for (VectorNode v : pq) {
 //			System.err.println("This goal x, y: " + v.goal.x + " " + v.goal.y + " distance: " + v.distance);
 //		}
 //		System.err.println(startState.goals.size());
 //		vectorMapConstruction = false;
 //
 
 		try
 		{
//			unsafePositions = Deadlock.staticDeadlocks(startState);
 		}
 		catch(Exception ex)
 		{
 //			System.err.println(ex);
 			//System.out.println("no path");
 		}
 
 		String solution_answer = SearchSolution(startState);
 
 		if(solution_answer == null)
 			throw new ArrayIndexOutOfBoundsException("roowoo");
 		else
 		{
 			for(int i=0; i<solution_answer.length(); i++)
 			{
 				System.out.print(solution_answer.charAt(i) + " ");
 			}
 			System.out.println();
 		}
 
 		System.err.println("State.hashCollissionCounter: " + State.hashCollissionCounter);
 	}
 
 	private class GreedyDFSBoxNode
 	{
 
 	}
 
 	private HashMap<State, GreedyDFSBoxNode> GreedyDFSBox_visited = new HashMap<State, GreedyDFSBoxNode>();
 	private static P[] adjacent_lrud = new P[]{new P(-1, 0), new P(+1, 0), new P(0, -1), new P(0, +1)}; //left right up down
 	private static P[] adjacent_lu = new P[]{new P(-1, 0), new P(0, -1)}; //left up
 
 	private void GreedyDFSBox()
 	{
 		Queue<GreedyDFSBoxNode> GreedyDFSBox_queue = new PriorityQueue<GreedyDFSBoxNode>();
 
 
 	}
 
 	public void GreedyDFSBoxSearch()
 	{
 
 	}
 
 	public boolean reverse_macro(State state, P move_box, P start_box)
     {
     	//pull a box to it's starting position without moving other boxes
     	for(P adjacent : adjacent_lrud)
     	{
     		P move_box_next = new P(move_box.x + adjacent.x, move_box.y + adjacent.y);
     		P player_next = new P(move_box.x + adjacent.x * 2, move_box.y + adjacent.y * 2);
 
     		if(state.boxes.contains(move_box_next))
     			return false;
     		if(state.walls.contains(move_box_next))
     			return false;
     		if(state.boxes.contains(player_next))
     			return false;
     		if(state.walls.contains(player_next))
     			return false;
 
 			//todo search a path to the next player position
 
 
     		//pull boxes towards target position
     		state.pull(player_next, adjacent);
     	}
 
     	return false;
     }
 
     public void reverse_macro(State state)
     {
     	//move as many boxes as possible directly to goal
     	for(P box : state.boxes)
     	{
     		//skip boxes that already have a goal
     		if(state.goals.contains(box))
     			continue;
 
     		for(P goal : state.goals)
     		{
     			//skip goals that already have a box
 	    		if(state.boxes.contains(goal))
 	    			continue;
 
     			reverse_macro(state, box, goal);
     		}
     	}
     }
 
 //	public static void main(String args[])
 //	{
 ////		LinkedList<VectorNode> test = new LinkedList<VectorNode>();
 ////		System.err.println("next test");
 ////		VectorNode[] array = test.toArray(new VectorNode[0]);
 ////		Arrays.sort(array);
 ////
 ////		for(VectorNode v: array){
 ////			System.err.println(v.distance);
 ////		}
 ////		{
 ////			VectorNode v = new VectorNode();
 ////			v.distance = 3;
 ////			v.goal = new P(3,4 );
 ////			test.add(v);
 ////		}
 ////		System.err.println("next test");
 ////		for(VectorNode v: test){
 ////			System.err.println(v.distance);
 ////		}
 ////		{
 ////			VectorNode v = new VectorNode();
 ////			v.distance = 7;
 ////			v.goal = new P(3,4 );
 ////			test.add(v);
 ////		}
 ////		System.err.println("next test");
 ////		for(VectorNode v: test){
 ////			System.err.println(v.distance);
 ////		}
 ////		{
 ////			VectorNode v = new VectorNode();
 ////			v.distance = 4;
 ////			v.goal = new P(3,4 );
 ////			test.add(v);
 ////		}
 ////		System.err.println("next test");
 ////		for(VectorNode v: test){
 ////			System.err.println(v.distance);
 ////		}
 ////		{
 ////			VectorNode v = new VectorNode();
 ////			v.distance = 5;
 ////			v.goal = new P(3,4 );
 ////			test.add(v);
 ////		}
 ////		System.err.println("next test");
 ////		for(VectorNode v: test){
 ////			System.err.println(v.distance);
 ////		}
 ////		{
 ////			VectorNode v = new VectorNode();
 ////			v.distance = 4;
 ////			v.goal = new P(3,4 );
 ////			test.add(v);
 ////		}
 ////		System.err.println("next test");
 ////		for(VectorNode v: array){
 ////			System.err.println(v.distance);
 ////		}
 ////		{
 ////			VectorNode v = new VectorNode();
 ////			v.distance = 4;
 ////			v.goal = new P(3,4 );
 ////			test.add(v);
 ////		}
 //		DanielPlayer2 player = new DanielPlayer2();
 //		State slc_state1 = MapsSLC.LoadMap(1);
 //		System.err.println("rows: " + slc_state1.rows + " columns: " + slc_state1.columns);
 //		long start = System.currentTimeMillis();
 //
 //		player.play(slc_state1);
 //
 //		long end = System.currentTimeMillis();
 //		long elapsed = end - start;
 //		System.err.println("elapsed time(ms): " + elapsed);
 //	}
 }
