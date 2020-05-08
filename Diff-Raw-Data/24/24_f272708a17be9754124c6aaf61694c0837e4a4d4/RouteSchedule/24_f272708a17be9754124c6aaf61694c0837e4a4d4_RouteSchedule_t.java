 //Marcus Hayes
 //Computer Engineeering
 //Senior
 //ECE 1186
 //Th 6-9
 //TLTTC - Scheduler/MBO
 
 package TLTTC;
 
 import java.util.*;
 
 public class RouteSchedule
 {
  	public static long ROUTE_LENGTH = 60 * 60 * 1000; //Time in milliseconds of the timeframe to calculate the route
 	public static double PERCENT_SPEED = .75; //Constant to limit the speed of the train during the route
 
 	private Hashtable<Integer, TrainRoute> route;
 
 	public static void main(String[] args) throws Exception
 	{
 		int numBlocks = 7;
 		int numNodes = numBlocks + 1;
 		Block[] blocks = new Block[numBlocks];
 		Node[] nodes = new Node[numNodes];
 		ArrayList<Train> train = new ArrayList<Train>();
 		OperatorSchedule schedule = new OperatorSchedule();
 		RouteSchedule rs = new RouteSchedule();
 
 		nodes[0] = new YardNode(0, 0, 0);
 		
 		for(int i = 1; i < nodes.length - 1;i++)
 		{
 			nodes[i]= new ConnectorNode(i * i * i * i * i, i * i * i * i * i, i * i * i * i * i);
 		}
 
 		nodes[nodes.length - 1] = new YardNode((nodes.length - 1) * (nodes.length - 1), (nodes.length - 1) * (nodes.length - 1), (nodes.length - 1) * (nodes.length - 1));
 
 		for(int i = 0; i < blocks.length; i++)
 		{
 			blocks[i] = new LinearBlock(nodes[i], nodes[i+1], i, 0);
 			nodes[i].setOutput(blocks[i]);
 			nodes[(i+1) % blocks.length].setInput(blocks[i]);
 		}
 
 		train.add(new Train(0, 0));
 		schedule.add("first","last",0,System.currentTimeMillis(),OperatorStatus.SHIFTFIRSTHALF);
 
 		train.get(0).setBlock(blocks[0], nodes[0], nodes[1], 0);
 
 		train.add(new Train(1, 0));
 		schedule.add("first","last",1,System.currentTimeMillis(),OperatorStatus.SHIFTFIRSTHALF);
 
 		train.get(1).setBlock(blocks[1], nodes[1], nodes[2], 0);
 		
 		rs.routeTrains(System.currentTimeMillis(), train, schedule);		
 
 		System.out.println(rs.toString());
 	}
 
 	public RouteSchedule()
 	{
 		route = new Hashtable<Integer, TrainRoute>();
 	}
 
 	public String toString()
 	{
 		StringBuilder sb = new StringBuilder();
 
 		for(TrainRoute tr : route.values())
 		{
 			sb.append("\n" + tr);			
 		}
 
 		return sb.toString();
 	}
 
 	public boolean add(TrainRoute tr)
 	{
 		if(!route.containsKey(tr.getTrainNumber()))
 		{
 			route.put(tr.getTrainNumber(), tr);
 
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 
 	public boolean remove(TrainRoute tr)
 	{
 		return (route.remove(tr) != null);
 	}
 
 	public int size()
 	{
 		return route.size();
 	}
 
 	public boolean remove(int trainNumber)
 	{
 		TrainRoute tr = route.get(trainNumber);
 
 		if(tr == null)
 		{
 			return false;
 		}
 
 		return remove(tr);
 	}
 
 	public Iterator<TrainRoute> getIterator()
 	{
 		return route.values().iterator();
 	}
 
 	public TrainRoute getRoute(int trainNumber)
 	{
 		return route.get(trainNumber);
 	}
 
 	/*Routes the trains*/
 
 	public void routeTrains(long start, ArrayList<Train> trains, OperatorSchedule schedule) throws Exception
 	{
 
 		if(trains.size() != schedule.size())
 		{
 			throw new Exception("Schedule and trains sizes are not equal!!!"); //As of right now, trains must be the same size of the schedule.
 		}
 
 		int size = schedule.size();
 
 		if(size == 0)
 		{
 			throw new Exception("Schedule and trains are empty!!!");
 		}
 
 		route.clear(); //First clears the previous routes
 
 		for(int i = 0; i < size; i++)
 		{
 			Train train = trains.get(i);
 			Operator operator = schedule.getOperator(train.trainNumber);
 			TrainRoute tr;
 
 			if(operator == null)
 			{
 				throw new Exception("Cannot find train in schedule!!!");
 			}
 
 			/*Adds trains to route based on trains in the schedule*/
 
 			else if(operator.status == OperatorStatus.SHIFTFIRSTHALF)
 			{
 				tr = new TrainRoute(train.trainNumber, start, operator.breakStart);
 			}
 			else if(operator.status == OperatorStatus.SHIFTSECONDHALF)
 			{
 				tr = new TrainRoute(train.trainNumber, start, operator.shiftEnd);
 			}
 			else
 			{
 				throw new Exception("Cannot route trains that are in the yard!!!");
 			}
 
 			//long t = (long)Math.ceil(1000 * train.getBlock().getLength() / (0 + .5 * (RouteSchedule.PERCENT_SPEED * train.getBlock().getSpeedLimit() - 0)));
 			tr.add(new BlockSchedule(start, 0, RouteSchedule.PERCENT_SPEED * train.getBlock().getSpeedLimit(), train.getBlock(), train.getPreviousNode(), train.getNextNode()));
			//System.out.println(tr);
 			//Node previousNode = train.getNextNode();
 			//Block block = previousNode.getNextBlock(train.getBlock());
 			//Node nextNode = block.getNextNode(previousNode);
 			//tr.add(new BlockSchedule(start + t, 0, RouteSchedule.PERCENT_SPEED * block.getSpeedLimit(), block, previousNode, nextNode));
 
 			if(!add(tr))
 			{
 				throw new Exception("Schedule has duplicate operators!!!");
 			}				
 		}
 
 		routeTrains();
 	}
 
 	/*This function calculates the routes using pseudo-recursion, "recursing" on switches and stalling on train waits*/
 	private void routeTrains() throws Exception
 	{
 		Stack<int[]> startStack; //saves the index for the array to return to
 		Stack<boolean[]> directionStack; //saves the prior direction the train took on a switch
 		TrainRoute[] trainRoute;
 		int[] start;
 		boolean[] direction;
 		boolean doingWork;
 
 		startStack = new Stack<int[]>();
 		directionStack = new Stack<boolean[]>();
 		trainRoute = route.values().toArray(new TrainRoute[route.size()]);
 		start = new int[trainRoute.length];
 		direction = new boolean[trainRoute.length];
 
 		/*initilize array*/
 
 		for(int i = 0; i < trainRoute.length; i++)
 		{
 			start[i] = 0;
 			direction[i] = true;
 		}
 
 		/*runs until all trains either timeout and/or routed to the yard*/
 
 		do
 		{
 			doingWork = false;
 
 			for(int i = 0; i < trainRoute.length; i++)
 			{
 				Block block;
 				Node previousNode;
 				Node nextNode;
 				BlockSchedule bs;
 				long entryTime;
 				long traverseTime;
 				double v;
 				double vo;
 				double d;
 				double t;
 				int size;
 
 				/*checks time used for routing*/
 
 				if(trainRoute[i].getRouteDuration() >= RouteSchedule.ROUTE_LENGTH)
 				{
 					continue;
 				}
 
 				
 				bs = trainRoute[i].get(start[i]);
 				block = bs.getBlock();
 				previousNode = bs.getPreviousNode();
 				nextNode = bs.getNextNode();
 
 				/*checks if train is in yard*/
 
 				if(previousNode.getNodeType() == constData.NodeType.Yard && nextNode.getNodeType() == constData.NodeType.Yard)
 				{
 					continue;
 				}
 
 				doingWork = true; //route will be calculated so loop will continue
 				entryTime = bs.getEntryTime();
 				traverseTime = bs.getTraverseTime();
 				size = trainRoute[i].size();
 
 				if(size == 1)
 				{
 					vo = 0;
 				}
 				else
 				{
 					vo = trainRoute[i].get(size - 2).getSpeed();
 				}
 
 				d = block.getLength();
 				v = RouteSchedule.PERCENT_SPEED * block.getSpeedLimit();
 				t = d / (vo + .5 * (v - vo)); // v = vo + a * t, d = vo * t + .5 * a * t ^ 2, replace a and solve for t
 				traverseTime = traverseTime + (long)Math.ceil(1000 * t);
 				bs.setTraverseTime(traverseTime);
 				
 				/*checks for a collision when trains are going in the same direction*/
 
 				if(!isForwardCollision(i, trainRoute, block, previousNode, entryTime, traverseTime))
 				{
 					BlockSchedule temp;
 
 					/*if it is a switch, push values to stack before adding next route*/
 
 					if(nextNode.getNodeType() == constData.NodeType.Switch)
 					{
 						startStack.push(start);
 						directionStack.push(direction);
 						start = Arrays.copyOf(start, start.length);
 						direction = Arrays.copyOf(direction, direction.length);
 						direction[i] = true;
 						previousNode = nextNode;
 						block = previousNode.getNextBlock(block);
 						nextNode = block.getNextNode(previousNode);
 						temp = new BlockSchedule(entryTime + traverseTime, 0, RouteSchedule.PERCENT_SPEED * block.getSpeedLimit(), block, previousNode, nextNode);
 					}
 
 					/*if you are at a yard node, and it is time to go to the yard, set the conditions to stop calculating the route*/
 
 					else if(nextNode.getNodeType() == constData.NodeType.Yard && trainRoute[i].getYardTime() >= trainRoute[i].getLast().getExitTime())
 					{
 						previousNode = nextNode;
 						block = null;
 						temp = new BlockSchedule(entryTime + traverseTime, 0, 0, block, previousNode, nextNode);
 					}
 
 					/*add block normally*/
 					else
 					{
 						previousNode = nextNode;
 						block = previousNode.getNextBlock(block);
 						nextNode = block.getNextNode(previousNode);
 						temp = new BlockSchedule(entryTime + traverseTime, 0, RouteSchedule.PERCENT_SPEED * block.getSpeedLimit(), block, previousNode, nextNode);
 					}
 
 					/*ignore collisions if going to yard*/
 
 					if(block == null)
 					{
 						trainRoute[i].set(start[i], temp);
 					}
 
 					/*check for collisions in the case of the train ahead of you is waiting. If so, you wait.*/
 
 					else if(!isRearCollision(i, trainRoute, block, previousNode, entryTime + traverseTime, 0))
 					{
 						if(start[i] < size - 1)
 						{
 							trainRoute[i].set(start[i], temp);
 						}
 						else
 						{
 							trainRoute[i].add(temp);
 						}
 
 						start[i]++;
 					}
 				}
 
 				/*if so, return to previous values on the stacks*/
 
 				else
 				{
 					start = startStack.pop();
 					direction = directionStack.pop();
 
 					/*if the previous direction was the main direction, go in the divergent direction. Otherwise, do not add (this is a wait)*/
 
 					if(direction[i])
 					{
 						BlockSchedule temp;
 
 						direction[i] = false;
 						bs = trainRoute[i].get(start[i]);
 						previousNode = bs.getNextNode();
 						//block = previousNode.getDivergentBlock(bs.getBlock());
 						nextNode = block.getNextNode(previousNode);
 						temp = new BlockSchedule(entryTime + traverseTime, 0, RouteSchedule.PERCENT_SPEED * block.getSpeedLimit(), block, previousNode, nextNode);
 	
 						if(start[i] < size - 1)
 						{
 							trainRoute[i].set(start[i], temp);
 						}
 						else
 						{
 							trainRoute[i].add(temp);
 						}
 
 						start[i]++;
 					}
 				}				
 			}
 		}
 		while(doingWork);
 	}
 
 	/*calls each train's train route, except your's, to see if a collision will occur*/
 
 	private boolean isRearCollision(int index, TrainRoute[] tr, Block block, Node previousNode, long entryTime, long traverseTime)
 	{
 		for(int i = 0; i < tr.length; i++)
 		{
 			if(i != index && tr[i].isRearCollision(block, previousNode, entryTime, entryTime + traverseTime))
 			{
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	private boolean isForwardCollision(int index, TrainRoute[] tr, Block block, Node previousNode, long entryTime, long traverseTime)
 	{
 		for(int i = 0; i < tr.length; i++)
 		{
 			if(i != index && tr[i].isForwardCollision(block, previousNode, entryTime, entryTime + traverseTime))
 			{
 				return true;
 			}
 		}
 
 		return false;
 	}
 }
