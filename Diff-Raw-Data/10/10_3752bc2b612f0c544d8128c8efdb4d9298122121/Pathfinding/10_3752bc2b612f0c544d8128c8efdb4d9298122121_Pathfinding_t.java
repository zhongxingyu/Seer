 package chris;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.PriorityQueue;
 import java.util.Queue;
 import java.util.Set;
 
 import levelObjects.*;
 
 import chris.TaskDispenser.*;
 import client.Command;
 import client.Parser;
 import constants.Constants;
 import constants.Constants.*;
 import dataStructures.AgentInState;
 import dataStructures.BoxInState;
 import dataStructures.GoalActionSequence;
 import dataStructures.GoalSequenceNode;
 import dataStructures.ObjectInState;
 
 public class Pathfinding {
 	
 	final static int hMultiplier = 2;
 
 
 	// Should be called by planner to find commands for an agent
 	private static Queue<Command> commandsForAgentToField(Level l, Agent a, Field aTo)
 	{
 		return commandsForAgentAndBoxToFields(l, a, aTo, null, null);
 	}
 	
 	// Should be called by planner to find commands for an agent
 	private static LinkedList<Command> commandsForAgentAndBoxToFields(Level l, Agent a, Field aTo, Box b, Field bTo)
 	{
 		boolean withBox = (b!=null);
 		
 		PriorityQueue<AStarStateNode> openSet = new PriorityQueue<AStarStateNode>();
 		Field[][] fieldMap = l.getFieldMap(); // don't alter this
 		
 		// h
 		ArrayList<Field> path;
 		if (withBox) {
 			// make path for box
 			path = AStar(l,a,b.getAtField(),bTo); // we try to follow this path by using it as heuristic
 		} else {
 			// make path for agent
 			path = AStar(l,a,a.getAtField(),aTo);
 		}
 		int pathSize = path.size();
 		
 		// Start node
 		AStarStateNode start = new AStarStateNode();
 		start.pathProgress = 1;
 		start.g = 0;
 		start.w = 0;
 		start.h = pathSize-start.pathProgress; start.h *=hMultiplier;
 		start.f = start.g + start.w + start.h;
 		start.agentCurrentField = a.getAtField();
 		openSet.add(start);
 		
 		Field[][] partialFieldMap; // 5x5
 
 		System.err.println("\nSimple Path: "+path);
 		
 		int statesTotal = 0;
 		int statesVisited = 0; System.err.println("\nAgent loc : Command \t: f : h : w : g");
 		
 		// A*
 		AStarStateNode current;
 		Field aField;
 		while (openSet.size() > 0) 
 		{
 			statesVisited++;
 			
 			current = openSet.poll();
 			
 			// TEST
 			//System.err.println("\nStates visited/created: "+statesVisited+"/"+statesTotal+"\n");
 			Field testAField = current.agentCurrentField;
 			if (statesVisited < 50) {
 				if (current.parent != null) {
 					System.err.print(current.parent.agentCurrentField.x+"x"+current.parent.agentCurrentField.y+" > "+testAField.x+"x"+testAField.y+" : "+current.cmd);
 					System.err.println(" \t: "+ current.f + " : " + current.h + " : " + current.w + " : " + current.g);
 				} else {
 					System.err.print("nil > "+testAField.x+"x"+testAField.y+" : "+current.cmd);
 					System.err.println("    \t: "+ current.f + " : " + current.h + " : " + current.w + " : " + current.g);
 				}
 			}
 			// END TEST
 			
 			aField = current.agentCurrentField;
 			partialFieldMap = getPartialStateFromNode(current, fieldMap);
 			aField = partialFieldMap[2][2];
 			
 			// check for goal reached
 			if (statesTotal > 100000) return pathOfCommandsFromAStarStateNode(current);
 			if (withBox) {
 				int x = bTo.x-aField.x+2;
 				int y = bTo.y-aField.y+2;
 				if (x>=0 && y>=0 && x<5 && y<5)
 				{
 					Field goal = partialFieldMap[bTo.x-aField.x+2][bTo.y-aField.y+2];
 					if (goal != null && goal.object != null && (goal.object instanceof Box) && ((Box)goal.object).getId()==b.getId())
 					{
 						System.err.println("\nStates visited/created: "+statesVisited+"/"+statesTotal+"\n");
 						return pathOfCommandsFromAStarStateNode(current); // a box with the right id is on goal... good enough?
 					}
 				}
 			} else {
 				if (aField.x == aTo.x && aField.y == aTo.y) {
 					System.err.println("\nStates visited/created: "+statesVisited+"/"+statesTotal+"\n");
 					return pathOfCommandsFromAStarStateNode(current); // goal reached
 				}
 			}
 			
 			
 			// what cmds can we run from this map?
 			ArrayList<Command> cmds = possibleCommandsFromPartialFieldMap(partialFieldMap);
 			
 			// for each cmd: create new node and add to openSet
 			for (Command c : cmds)
 			{
 				statesTotal++;
 				
 				AStarStateNode node = new AStarStateNode();
 				node.cmd = c;
 				node.parent = current;
 				node.g = current.g + 1;
 				
 				if (c.cmd == "Move") {
 					//if (aField.neighbours[1] == null) System.err.println("aField.neighbour is null");
 					node.agentCurrentField = aField.neighbors[c.dir1.ordinal()];
 				} else if (c.cmd == "Pull") {
 					Field bField = aField.neighbors[c.dir2.ordinal()];
 					node.agentCurrentField = aField.neighbors[c.dir1.ordinal()];
 					node.boxChange = new BoxChange((Box)bField.object, bField, aField);
 				} else if (c.cmd == "Push") {
 					Field bField = aField.neighbors[c.dir1.ordinal()];
 					node.agentCurrentField = bField;
 					node.boxChange = new BoxChange((Box)bField.object, bField, bField.neighbors[c.dir2.ordinal()]);
 				} else { // NoOp
 					node.agentCurrentField = aField;
 				}
 				
 				// h
 				// This method of doing h is fairly cheap, but not that accurate
 				// We strongly favor staying on path instead of straying
 				
 				if (withBox)
 				{
 					// check if a box with the right id runs into one of the next 3 in path.
 					BoxChange bc = node.boxChange;
 					int pProgress = node.parent.pathProgress;
 					
 					if (bc != null && bc.box.getId()==b.getId()) { 
 						if (pathSize > pProgress && bc.to.x == path.get(pProgress).x && bc.to.y == path.get(pProgress).y) {
 							node.pathProgress = pProgress+1;
 							//System.err.println(c.cmd +" "+path.get(pProgress));
 						} else if (pathSize > pProgress+1 && bc.to.x == path.get(pProgress+1).x && bc.to.y == path.get(pProgress+1).y) {
 							node.pathProgress = pProgress+2;
 						} else if (pathSize > pProgress+2 && bc.to.x == path.get(pProgress+2).x && bc.to.y == path.get(pProgress+2).y) {
 							node.pathProgress = pProgress+3;
 						}
 					}
 					node.h = pathSize-node.pathProgress; node.h *=hMultiplier;
 				}
 				else
 				{
 					Field acf = node.agentCurrentField;
 					int pProgress = node.parent.pathProgress;
 					
 					// check if we run into one of the next 3 in path.
 					if (pathSize > pProgress && acf.x == path.get(pProgress).x && acf.y == path.get(pProgress).y) {
 						node.pathProgress = pProgress+1;
 					} else if (pathSize > pProgress+1 && acf.x == path.get(pProgress+1).x && acf.y == path.get(pProgress+1).y) {
 						node.pathProgress = pProgress+2;
 					} else if (pathSize > pProgress+2 && acf.x == path.get(pProgress+2).x && acf.y == path.get(pProgress+2).y) {
 						node.pathProgress = pProgress+3;
 					}
 					node.h = pathSize-node.pathProgress; node.h *=hMultiplier;
 				}
 				
 				
 				
 				// w
 				node.w = 0;
 				BoxChange bc = node.boxChange;
 				if (bc != null)
 				{
 					if (!withBox) node.w++;
 					
 					Field bfrom = fieldMap[bc.from.x][bc.from.y];
 					Field bto   = fieldMap[bc.from.x][bc.from.y];
 					
 					if (bfrom instanceof Goal && ((Goal) bfrom).getId() == bc.box.getId()
 						&& (!(bto instanceof Goal) || ((Goal)bto).getId() != bc.box.getId()) )
 					{
 						node.w += 2; // moving box from goal to non-goal
 					}
 					if (bto instanceof Goal && ((Goal)bto).getId() == bc.box.getId()
 						&& (!(bfrom instanceof Goal) || ((Goal) bfrom).getId() != bc.box.getId()) ) 
 					{
 						node.w -= 2; // moving box from non-goal to goal
 					}
 					
 					/*if (withBox) {
 						if (bc.box.getId() != b.getId()) {
 							if (pathSize > node.pathProgress) {
 								Field nextOnPath = path.get(node.pathProgress);
 								if (nextOnPath.x == bfrom.x && nextOnPath.y == bfrom.y) {
 									node.w -= 5;
 								} else if (nextOnPath.x == bto.x && nextOnPath.y == bto.y) {
 									node.w += 5;
 								}
 							}
 						}
 					}*/
 				}
 				
 				node.f = node.g + node.w + node.h;
 
 				// lose neighbours to save space per node
 				/*Field temp;
 				if (bc != null) {
 					temp = new Field(node.boxChange.from);
 					node.boxChange.from = temp;
 					temp = new Field(node.boxChange.to);
 					node.boxChange.to = temp;
 				}
 				temp = new Field(node.agentCurrentField);
 				node.agentCurrentField = temp;*/
 				
 				openSet.add(node);
 			}
 		}
 		
 		// run around in rings if we don't find a path
 		LinkedList<Command> q = new LinkedList<Command>();
 		q.add(new Command(dir.N));
 		q.add(new Command(dir.E));
 		q.add(new Command(dir.S));
 		q.add(new Command(dir.W));
 		return q;
 	}
 	
 
 	private static LinkedList<Command> pathOfCommandsFromAStarStateNode(AStarStateNode node)
 	{
 		LinkedList<Command> path = new LinkedList<Command>();
 		
 		AStarStateNode current = node;
 		while (current.parent != null) { // skips the root node (it has no cmd)
 			//System.err.println(current.field.x +"x"+ current.field.y + ", g:"+current.g+" w:"+current.w+" h:"+current.h);
 			path.add(current.cmd);
 			current = current.parent;
 		}
 		Collections.reverse(path);  // optimize?
 		
 		return path;
 	}
 
 
 	private static class BoxChange
 	{
 		Field from, to;
 		Box box;
 		
 		public BoxChange(Box box, Field from, Field to) {
 			this.box = box;
 			this.from = from;
 			this.to = to;
 		}
 	}
 
 
 	
 	
 	private static class AStarStateNode implements Comparable<AStarStateNode>
 	{
 		Command cmd;
 		AStarStateNode parent;
 		Field agentCurrentField; // use just a Field instead?
 		BoxChange boxChange; // might be null
 		int f,g,w,h;
 		int pathProgress;
 		
 		public int compareTo(AStarStateNode o) {
 			return f - o.f;
 		}
 	}
 	
 	
 	private static Field[][] getPartialStateFromNode(AStarStateNode n, Field[][] fieldMap)
 	{
 		Field[][] partialFieldMap = new Field[5][5];
 		
 		Field aField = n.agentCurrentField; // this is our offset
 		int xOffset = aField.x - 2;
 		int yOffset = aField.y - 2;
 		
 		// clone the field we can reach from agent (radius of 2 from agent)
 		partialFieldMap[2][0] = cloneField(fieldMap,xOffset+2,yOffset  );
 		partialFieldMap[1][1] = cloneField(fieldMap,xOffset+1,yOffset+1);
 		partialFieldMap[2][1] = cloneField(fieldMap,xOffset+2,yOffset+1);
 		partialFieldMap[3][1] = cloneField(fieldMap,xOffset+3,yOffset+1);
 		partialFieldMap[0][2] = cloneField(fieldMap,xOffset  ,yOffset+2);
 		partialFieldMap[1][2] = cloneField(fieldMap,xOffset+1,yOffset+2);
 		partialFieldMap[2][2] = cloneField(fieldMap,xOffset+2,yOffset+2);
 		partialFieldMap[3][2] = cloneField(fieldMap,xOffset+3,yOffset+2);
 		partialFieldMap[4][2] = cloneField(fieldMap,xOffset+4,yOffset+2);
 		partialFieldMap[1][3] = cloneField(fieldMap,xOffset+1,yOffset+3);
 		partialFieldMap[2][3] = cloneField(fieldMap,xOffset+2,yOffset+3);
 		partialFieldMap[3][3] = cloneField(fieldMap,xOffset+3,yOffset+3);
 		partialFieldMap[2][4] = cloneField(fieldMap,xOffset+2,yOffset+4);
 		
 		// establish neighbour links again (cloning can't do that)
 		for (int x=1; x<5; x++) {
 			for (int y=1; y<5; y++) {
 				Field f = partialFieldMap[x][y];
 				if (f != null) {
 					Field north = partialFieldMap[x][y-1];
 					Field west =  partialFieldMap[x-1][y];
 					if (north != null) Parser.LinkFieldsVertical(f, north);
 					if (west != null) Parser.LinkFieldsHorizontal(f, west);
 				}
 			}
 		}
 		
 		
 		// gather changes in ancestors
 		ArrayList<BoxChange> boxChanges = new ArrayList<BoxChange>();
 		AStarStateNode current = n;
 		while (current != null)
 		{
 			if (current.boxChange != null) boxChanges.add(current.boxChange);
 			current = current.parent;
 		}
 		
 		// apply changes backwards (so newer changes overwrite older)
 		for (int i=boxChanges.size()-1; i>=0; i--)
 		{
 			BoxChange bc = boxChanges.get(i);
 			if (bc == null) continue;
 			
 			// add box to toField
 			Field t = bc.to;
 			if (xOffset<=t.x && t.x<xOffset+4 && 
 				yOffset<=t.y && t.y<yOffset+4 &&
 				partialFieldMap[t.x-xOffset][t.y-yOffset] != null)
 			{
 				partialFieldMap[t.x-xOffset][t.y-yOffset].object = bc.box;
 			}
 			// clear from
 			Field f = bc.from;
 			if (xOffset<=f.x && f.x<xOffset+4 && 
 				yOffset<=f.y && f.y<yOffset+4 &&
 				partialFieldMap[f.x-xOffset][f.y-yOffset] != null)
 			{
 				partialFieldMap[f.x-xOffset][f.y-yOffset].object = null;
 			}
 			
 		}
 		
 		// TEST
 		//System.err.println("pMap middle east neighbor field: "+partialFieldMap[2][2].neighbours[dir.E.ordinal()]);
 		// TEST END
 		
 		return partialFieldMap;
 	}
 	
 	private static Field cloneField(Field[][] fMap, int x, int y) 
 	{
 		if (x<0 || y<0 || x>=fMap.length || y>=fMap[0].length) return null;
 		
 		Field f = fMap[x][y];
 		return (f == null) ? null : new Field(f);
 	}
 	
 	
 	private static ArrayList<Command> possibleCommandsFromPartialFieldMap(Field[][] pMap)
 	{
 		ArrayList<Command> possibleCommands = new ArrayList<Command>();
 		
 		Field aField = pMap[2][2];
 		
 		for (dir aDir : dir.values()) {
 			// Push/Pull
 			possibleCommands.addAll(possibleBoxCommandsInDirection(aDir,aField));
 			
 			// Move.. move this to the function?
 			Field n = aField.neighbors[aDir.ordinal()];
 			if (n != null && n.object == null)
 				possibleCommands.add(new Command(aDir));
 		}
 		
 		return possibleCommands;
 	}
 	
 	private static ArrayList<Command> possibleBoxCommandsInDirection(dir aDir, Field aField)
 	{
 		ArrayList<Command> possibleCommands = new ArrayList<Command>();
 		
 		Field bFrom; 
 
 		// Find possible pull commands
 		for (dir bDir : dir.values()) {
 			bFrom = aField.neighbors[bDir.ordinal()];
 			if (bFrom == null || !(bFrom.object instanceof Box)) continue;
 			
 			// we cannot pull a box forward in the box's direction
 			Field aTo = aField.neighbors[aDir.ordinal()];
 			if (bDir != aDir && aTo != null && aTo.object == null) {
 				possibleCommands.add(new Command("Pull", aDir, bDir));
 			}
 		}
 
 		// Find possible push commands
 		bFrom = aField.neighbors[aDir.ordinal()];
 		if (bFrom != null && bFrom.object instanceof Box) {
 			for (dir bDir : dir.values()) {
 				// We cannot push a box backwards in the agents direction
 				Field bTo = bFrom.neighbors[bDir.ordinal()];
 				if (bDir != Constants.oppositeDir(aDir) && bTo != null && bTo.object == null) {
 					possibleCommands.add(new Command("Push", aDir, bDir));
 				}
 			}
 		}
 
 		return possibleCommands;
 	}
 	
 /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 	
 	// given a linear path, this returns the commands to follow it
 	public static LinkedList<Command> commandsForAgentToField(Agent agent, MoveTask taskMove)
 	{
 		ArrayList<Field> path = taskMove.path; int pathSize = path.size();
 		LinkedList<Command> cmds = new LinkedList<Command>();
 		
 		System.err.println("cmdsForAgentToField from "+path.get(0)+" to "+path.get(pathSize-1));
 		
 		// it is assumed that first field in path is where agent is already
 		// thus we get (pathSize-1) commands total
 		for (int i=0; i<pathSize-1; i++)
 		{
 			dir d;
 			Field from = path.get(i);
 			Field to   = path.get(i+1);
 			
 			if (from.x == to.x) {
 				if (from.y < to.y) {
 					d = dir.S;
 				} else {
 					d = dir.N;
 				}
 			} else {
 				if (from.x < to.x) {
 					d = dir.E;
 				} else {
 					d = dir.W;
 				}
 			}
 			
 			cmds.add(new Command(d));
 		}
 		
 		return cmds;
 	}
 	
 	
 /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 	
 	// returns path for an agent to a field
 	public static ArrayList<Field> AStar(Level l, Agent a, Field from, Field to)
 	{
 		System.err.println("AStar from "+from+" to "+to);
 		
 		// Data handling
 		AStarNode[][] nodeMap = new AStarNode[l.fieldMap.length][l.fieldMap[0].length];
 		PriorityQueue<AStarNode> openSet = new PriorityQueue<AStarNode>();
 		Set<AStarNode> closedSet = new HashSet<AStarNode>();
 		
 		// Start node
 		AStarNode start = new AStarNode(from);
 		start.g = 0;
 		start.w = 0;
 		start.h = manhattanDist(start.field, to);
 		start.f = start.g + start.w + start.h;
 		nodeMap[from.x][from.y] = start;
 		openSet.add(start);
 		
 		// A*
 		AStarNode current;
 		while (openSet.size() > 0)
 		{
 			current = openSet.poll();
 			closedSet.add(current);
 			Field f = current.field;
 			
 			if (f == to) {
 				//printNodeMap(nodeMap); // TODO print
 				return pathFromAStarNode(current); // goal reached
 			}
 			
 			Field n;
 			for (int i=0; i<4; i++) {
 				n = f.neighbors[i];
 				if (n != null) {
 					int tentative_g = current.g + 1;
 					// check if there's already a node for this neighbor field
 					AStarNode nNode;
 					boolean nNodeIsNew = false; // temp value
 					if (nodeMap[n.x][n.y] != null) { // already created node
 						nNode = nodeMap[n.x][n.y];
 						if (closedSet.contains(nNode)) {
 							if (tentative_g >= nNode.g) continue;
 						}
 					} else {
 						nNode = new AStarNode(n);
 						nodeMap[n.x][n.y] = nNode;
 						nNodeIsNew = true;
 					}
 					
 					boolean nNodeInOpenSet = false;
 					if (!nNodeIsNew) nNodeInOpenSet = openSet.contains(nNode); // optimize this away !
 					
 					if (!nNodeInOpenSet || tentative_g < nNode.g) 
 					{
 						nNode.field = n;
 						nNode.parent = current;
 						nNode.g = tentative_g;
 						if (nNodeIsNew) {
 							nNode.h = manhattanDist(nNode.field, to);
 							if (nNode.field != to) nNode.w = weightOfField(nNode.field, a);
 						}
 						nNode.f = nNode.g + nNode.w + nNode.h;
 						
 						if (!nNodeInOpenSet) openSet.add(nNode);
 					}
 				}
 			}
 		}
 		
 		System.err.println("!!! Simple AStar could not find a path");
 		
 		return null;
 	}
 	
 	private static void printNodeMap(AStarNode[][] nodeMap) 
 	{
 		for (int y=0; y<nodeMap[0].length; y++) {
 			for (int x=0; x<nodeMap.length; x++) {
 				if (nodeMap[x][y] != null) 
 					//System.err.print(nodeMap[x][y].f+"\t");
 					System.err.printf("%3d", nodeMap[x][y].f); // TODO printing here!
 				else 
 					System.err.print("   ");
 			}
 			System.err.println();
 		}
 	}
 	
 
 	private static void printPartialFieldMap(Field[][] pMap) 
 	{
 		System.err.println("pMap");
 		for (int y=0; y<pMap[0].length; y++) {
 			for (int x=0; x<pMap.length; x++) {
 				if (pMap[x][y] != null) {
 					Object o = pMap[x][y].object;
 					if (x==2 && y==2) System.err.print(" a");
 					if (o instanceof Box) System.err.print(" b");
 					if (o == null) System.err.print("  ");
 				} else { 
 					System.err.print(" +");
 				}
 			}
 			System.err.println();
 		}
 	}
 
 
 	private static ArrayList<Field> pathFromAStarNode(AStarNode node)
 	{
 		ArrayList<Field> path = new ArrayList<Field>();
 		
 		AStarNode current = node;
 		while (current != null) {
 			//System.err.println(current.field.x +"x"+ current.field.y + ", g:"+current.g+" w:"+current.w+" h:"+current.h); // TODO printing here!
 			path.add(current.field);
 			current = current.parent;
 		}
 		Collections.reverse(path);
 		
 		return path;
 	}
 	
 	
 	private static class AStarNode implements Comparable<AStarNode>
 	{
 		AStarNode parent;
 		Field field;
 		int f,g,w,h;
 
 		public AStarNode(Field f){
 			field = f;
 		}
 		
 		public int compareTo(AStarNode o) {
 			return f - o.f;
 		}
 	}
 	
 	
 	private static int manhattanDist(Field from, Field to)
 	{
 		return Math.abs(from.x - to.x) 
 			 + Math.abs(from.y - to.y);
 	}
 	
 	private static int weightOfField(Field f, Agent a) 
 	{
 		int weight = 0;
 		
 		if (f instanceof Goal) weight += 0; // Moving over a goal. Should this even give extra weight?
 		
 		Object o = f.object;
 		if (o instanceof Box) {
 			//System.err.println("box found in a star");
 			if (((Box) o).getColor() == a.getColor())
 				weight += 1000; // Agent can move box
 			else
 				weight += 2000; // Agent can't move box
 		}
 		
 		return weight;
 	}
 	
 	public static Field findNearestFreeField(Level level, Field from, List<Field> bannedFields){
 		
 		LinkedList<Field> frontier = new LinkedList<Field>();
 		
 		frontier.add(from);
 		
 		Field currentField = frontier.poll();
 				
 		while(bannedFields.contains(currentField) && currentField != null){
 			
 			for (Field neighbour : currentField.neighbors) {
 				if(neighbour != null && neighbour.object == null && !frontier.contains(neighbour)){
 					frontier.add(neighbour);
 				}
 			}
 			
 			currentField = frontier.poll();
 		}
 
 		return currentField;
 	}
 	
 	public static Field[] findNearestTwoFreeFields(Level level, Box curB, Agent curA, Field from, List<Field> bannedFields){
 			
 //		for (Field field : bannedFields) {
 //			System.err.println("Banned fields " + field.toString());
 //		}
 		
 		Field[] returnFields = new Field[2];
 		
 		LinkedList<Field> frontier = new LinkedList<Field>();
 		
 		frontier.add(from);
 		
 		Field currentField = frontier.poll();
 		Field currentFieldNeighbour = fieldHasFreeNeighbour(currentField,bannedFields);
 				
 		while(!fieldsLegit(level, currentField, currentFieldNeighbour, bannedFields)){
 
 			for (Field neighbour : currentField.neighbors) {
 				if(neighbour != null 
 				    && (neighbour.object == null || neighbour.object == curB || neighbour.object == curA) 
 				    && !frontier.contains(neighbour)){
 					frontier.add(neighbour);
 				}
 			}
 			
 			currentField = frontier.poll();
 			currentFieldNeighbour = fieldHasFreeNeighbour(currentField,bannedFields);
 			if(currentField == null) break; 
 		}
 
 //		System.err.println("Currentfield in banned " + bannedFields.contains(currentField));
 		returnFields[0] = currentField;
 		returnFields[1] = fieldHasFreeNeighbour(currentField,bannedFields);
 		
 		return returnFields;
 	}
 
 	private static boolean fieldsLegit(Level level, Field currentField,
 			Field currentFieldNeighbour, List<Field> bannedFields) {
 		
 		if(currentField == null || currentFieldNeighbour == null) return false;
 		
 		if(bannedFields.contains(currentField) || bannedFields.contains(currentFieldNeighbour)) return false;
 		
 		if(currentField.object != null || currentFieldNeighbour.object != null) return false;
 		
 		if(isNeighbours(currentField, currentFieldNeighbour));
 		
 		return true;
 	}
 
 	public static Field fieldHasFreeNeighbour(Field currentField,
 			List<Field> bannedFields) {
 		for (Field field : currentField.neighbors) {
 			if(field != null && !bannedFields.contains(field)) return field;
 		}
 		
 		return null;
 	}
 
 	public static Field findFreeNeighbour(Field currentField) {
 		for (Field field : currentField.neighbors) {
 			if(field != null && field.object == null) return field;
 		}
 		
 		return null;
 	}
 	
 	private static boolean isNeighbours(Field currentField, Field lastField) {
 		
 		for (Field n : currentField.neighbors) {
 			if (n != null && n.equals(lastField)) {
 				return true;
 			}
 		}
 	
 		return false;
 	}
 	
 	
 	public static Object[] findGoalRoute(Level l, Agent agent,
 			Box box, Field agentFromField, Field agentToField,
 			Field boxFromField, Field boxToField) {
 		
 		dir boxDir = null;
 
 		GoalSequenceNode root = new GoalSequenceNode(boxFromField,
 				agentFromField, null);
 
 		LinkedList<GoalSequenceNode> queue = new LinkedList<GoalSequenceNode>();
 
 		// prune looped states (if agent and box ends up in a state already
 		// explored)
 		HashMap<Field, ArrayList<Field>> closedSet = new HashMap<Field, ArrayList<Field>>();
 
 		// adding initial state to list set of explored states
 		ArrayList<Field> tempList = new ArrayList<Field>();
 		tempList.add(boxFromField);
 		closedSet.put(agentFromField, tempList);
 
 		// Add a closed set.
 		queue.add(root);
 		GoalSequenceNode currentNode = queue.poll();
 
 		while (currentNode != null && (currentNode.boxLocation != boxToField || currentNode.agentLocation != agentToField)) {
 			
 //			System.err.println("From " + agentToField.toString() + " " + boxToField.toString() + " to " + currentNode.agentLocation.toString() + " " + currentNode.boxLocation.toString());
 
 			boxDir = Agent.getBoxDirection(currentNode.agentLocation,
 					currentNode.boxLocation);
 			
 //			System.err.println("Foud boxdir is " + boxDir + " locations: " + currentNode.agentLocation + " and box " + currentNode.boxLocation);
 
 			ArrayList<Command> foundCommands = addPossibleBoxCommandsForDirection(boxDir, currentNode.agentLocation, currentNode.boxLocation, l);
 
 			for (Command command : foundCommands) {
 				Field boxLocation = null;
 				Field agentLocation = null;
 
 				if (command.cmd.equals("Push")) {
 					agentLocation = currentNode.boxLocation;
 					boxLocation = currentNode.boxLocation.neighbors[command.dir2
 							.ordinal()];
 				} else {
 					boxLocation = currentNode.agentLocation;
 					agentLocation = currentNode.agentLocation.neighbors[command.dir1
 							.ordinal()];
 				}
 
 				// Do we already have a way to get to this state?
 				if (closedSet.containsKey(agentLocation)) {
 
 					if (closedSet.get(agentLocation).contains(boxLocation)) {
 						continue;
 
 					} else { // the agent has been here before but without the
 								// box in same location
 						closedSet.get(agentLocation).add(boxLocation);
 					}
 				} else { // neither the agent or the box has been here before.
 							// Update DS and create node in BTtree:
 					ArrayList<Field> tempListe = new ArrayList<Field>();
 					tempListe.add(boxLocation);
 					closedSet.put(agentLocation, tempListe);
 				}
 
 				GoalSequenceNode node = new GoalSequenceNode(boxLocation,
 						agentLocation, command);
 
 				node.parent = currentNode;
 
 				queue.add(node);
 
 			}
 			if (queue.isEmpty()) { // we have searched to the end without
 						// access to goals
 				System.err.println("Returning null goalRoute");
 				Object[] returnObjects = new Object[2];
 				returnObjects[0] = null;
 				returnObjects[1] = null;
 				return returnObjects;
 			}
 			currentNode = queue.poll();
 		}
 
 		LinkedList<Command> commands = new LinkedList<Command>();
 		ArrayList<Field> usedFields = new ArrayList<Field>();
 		
 		while (currentNode.parent != null) {
 			commands.addFirst(currentNode.action);
 			usedFields.add(0,currentNode.agentLocation);
 			usedFields.add(0,currentNode.boxLocation);
 			
 			currentNode = currentNode.parent;
 		}
 		
 		Object[] returnObjects = new Object[2];
 		returnObjects[0] = commands;
 		returnObjects[1] = usedFields;
 
 		return returnObjects;
 	}
 
 	public static ArrayList<Command> addPossibleBoxCommandsForDirection(
 			dir direction, Field agentLocationInPlan, Field boxLocationInPlan,
 			Level l) {
 		ArrayList<Command> possibleCommands = new ArrayList<Command>();
 
 		// Find possible pull commands
 		for (dir d : dir.values()) {
 
 			// we cannot pull a box forward in the box's direction
 			if (d != direction
 					&& agentLocationInPlan.neighbors[d.ordinal()] != null
 					&& agentLocationInPlan.neighbors[d.ordinal()].object == null) {
 				possibleCommands.add(new Command("Pull", d, direction));
 			}
 		}
 		// Find possible push commands
 		for (dir d : dir.values()) {
 			// We cannot push a box backwards in the agents direction
 			if (d != Constants.oppositeDir(direction)
 					&& boxLocationInPlan.neighbors[d.ordinal()] != null
 					&& boxLocationInPlan.neighbors[d.ordinal()].object == null) {
 				possibleCommands.add(new Command("Push", direction, d));
 			}
 		}
 
 		return possibleCommands;
 	}
 	
 
 }
