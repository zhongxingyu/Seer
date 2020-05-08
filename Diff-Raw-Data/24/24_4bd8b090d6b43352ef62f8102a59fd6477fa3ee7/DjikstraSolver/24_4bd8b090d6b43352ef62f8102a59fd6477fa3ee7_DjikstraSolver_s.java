 package routefiend;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Locale;
 import java.util.Map;
 import java.util.PriorityQueue;
 import java.util.Set;
 
 public class DjikstraSolver
 {
 	public static final int INFINITE_DISTANCE = Integer.MAX_VALUE;
 	private static final int INITIAL_CAPACITY = 8;
 	private static final long MAX_LATE = 900000; //15 minutes 
 	
 	private final Comparator<Intersection> shortestDistanceComparator = new Comparator<Intersection>()
 	{
 		public int compare(Intersection left, Intersection right)
 		{
 			// note that this trick doesn't work for huge distances, close to Integer.MAX_VALUE
 			int result = getShortestDistance(left) - getShortestDistance(right);
 
 			return (result == 0) ? left.compareTo(right) : result;
 		}
 	};
 	
 	private final PriorityQueue<Intersection> unvisitedNodes = new PriorityQueue<Intersection>(INITIAL_CAPACITY, shortestDistanceComparator);
 	private final Set<Intersection> visitedNodes = new HashSet<Intersection>();
 	
 	private final Map<Intersection, Integer> shortestDistances = new HashMap<Intersection, Integer>();
 	private final Map<Intersection, Intersection> predecessors = new HashMap<Intersection, Intersection>();
 	
 	class DateCompare implements Comparator<Date>
 	{
 		    public int compare(Date one, Date two)
 		    {
 		    	return one.compareTo(two);
 		    }
 	}
 	
 	//Reinitializes the algorithm for new solve
     private void init(Intersection start)
     {
     	visitedNodes.clear();
         unvisitedNodes.clear();
         
         shortestDistances.clear();
         predecessors.clear();
         
         // add source
         setShortestDistance(start, 0);
         unvisitedNodes.add(start);
     }
     
     //Constructor, that preloads the DB data from a given address and credentials
     public DjikstraSolver(String address, String username, String password)
     {
     	Street.readStreetsFrom(address, username, password);
     	Intersection.readIntersectionFrom(address, username, password);
     	TrafficLoad.readTrafficsFrom(address, username, password);
     }
     
     //Gives the optimal route for a given set of destinations and corresponding expected times
     //The return value is an array of Intersections, that compose the optimal path.
     //If there is a set of destinations, that are impossible to be reached in a reasonable time, the special value of '-1' is inserted in the array
     //and the unreachable destinations are added after it
     @SuppressWarnings("deprecation")
 	public ArrayList<Integer> solveForDestinationsAndTimes(int[] destinationIds, String[] times)
     {
     	ArrayList<Date> objTimes = new ArrayList<Date>();
     	ArrayList<Integer> unreachables = new ArrayList<Integer>();
     	HashMap<Date, Integer> timedDestinations = new HashMap<Date, Integer>();
     	DateCompare compare = new DateCompare();
     	Date last; //The destination time, that has be considered reachable last
     	
     	
     	ArrayList<Integer> result = new ArrayList<Integer>();
     	//Iterate over the times, create objects from them and store them into an array.
     	//Associate each destination with it's expected time
     	for(int i = 0; i < times.length; i++)
     	{
     		try
     		{
 				Date time = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).parse(times[i]);
 				
 				while(timedDestinations.containsKey(time))
 				{
 					System.out.println("Same keys");
 				}
 				
 				timedDestinations.put(time, destinationIds[i]);
 				objTimes.add(time);
 			} catch (ParseException e) 
 			{
 				// Auto-generated catch block
 				e.printStackTrace();
 			}
     	}
     	//Now sort the destination times
     	Collections.sort(objTimes, compare);
     	
     	//Start from the first destination and find the time needed to get to the second
     	//If it's after it's destination time + some reasonable delay interval (e.g. 15 minutes) check the next destination
     	last = objTimes.get(0);
     	result.add(destinationIds[0]);
     	for(int i = 0; i < (objTimes.size() - 1); i++)
     	{
     		Date time1 = objTimes.get(i);
     		if(last != time1) //Check if the currently selected destination time corresponds to the destination considered reachable last.
     			continue;	  //Otherwise skip
     		
     		//Iterate over the destination that follow the one selected last
     		for(int j = 1; (j + i) < objTimes.size(); j++)
     		{
 	    		Date time2 = objTimes.get(j + i);
 	    		Calendar cal1 = Calendar.getInstance();
 	    		Calendar cal2 = Calendar.getInstance();
 	    		int id1 = timedDestinations.get(time1);
 	    		int id2 = timedDestinations.get(time2);
 	    		int[] route = findShortestDistance(id1, id2, time1.getHours());
 	    		int minutes = TrafficLoad.getCostForRoute(route, time1);
 	    		System.out.println("Projected minutes: " + minutes);
 	    		
 	    		cal1.setTime(objTimes.get(i));
 	    		cal2.setTime(objTimes.get(j + i));
 	    		cal1.add(Calendar.MINUTE, minutes);
 	    		//Check if it's possible to get to the next destination in reasonable time

	    		Date date1 = cal1.getTime();
	    		Date date2 = cal2.getTime();
	    		System.out.println(date1.toString() + " " + date2.toString());
 	    		
 				Long difference = cal1.getTimeInMillis() - cal2.getTimeInMillis();
 				System.out.println("Difference: " + difference.toString());
 	    			if((cal1.getTimeInMillis() - cal2.getTimeInMillis()) > MAX_LATE)
 	    			{
 		    			System.out.println("Unreachable destination");
 	    				unreachables.add(id2);
 	    				continue;
 	    			}
 		    		else
 		    		{
 		    			last = time2;
 		    			for(int k = (route.length - 2); k > 0; k--)
 		    				result.add(route[k]);
 		    			result.add(id2);
 		    			break;
 		    		}
     		}
     	}
     	
     	//If some destinations are considered unreachable add them to the result set
     	if(unreachables.size() > 0)
     	{
 	    	result.add(-1);
 	    	for(int id : unreachables)
 	    	{
 	    		result.add(id);
 	    	}
     	}
 
 		return result;
     	
     }
     
     //Djikstra. Solve the graph for a given start end end node and time
     public int[] findShortestDistance(int startId, int endId, int hour)
     {
     	Intersection start = Intersection.getIntersectionForId(startId);
     	Intersection end = Intersection.getIntersectionForId(endId);
     	Intersection current;
     	int pathLength = 0; //The predecessors map contains death end paths and is generally larger than the real path
     	init(start);
     	
     	//Iterate over the unvisited nodes
     	while((current = unvisitedNodes.poll()) != null)
     	{
     		//If the currently examined node is actually the end node, that we look for print the path so far and break
     		if(current == end)
     		{
     			Intersection ancient = predecessors.get(current);
     			System.out.print(current.name() + " ");
     			pathLength++;
     			//Iterate over all nodes that lead to this end one
     			while(ancient != null)
     			{
     				System.out.print(ancient.name() + " ");
     				ancient = predecessors.get(ancient);
     				pathLength++;
     			}
     			System.out.println();
     			break;
     		}
     		
     		//Add the currently examined node to the list of visited ones
     		visitedNodes.add(current);
     		//Find the nearest neighbour. This also inserts it into the unvisited queue
     		computeShortestToNeighbours(current, hour);
     	}
     	
     	//Once all the nodes have been visited or the path from start to end is found
     	//Build the array of nodes forming the path
     	int[] route = new int[pathLength];
 //    	Integer length = pathLength;
 //    	System.out.println("Found route with length " + length.toString());
     	int index = 1;
     	route[0] = end.id();
     	while(current != null)
     	{
     		current = predecessors.get(current);
     		if(current == null)
     			continue;
 
     		route[index] = current.id();
     		index++;
     	}
     	
     	return route;
     }
     
     //Consider all neighbours of a given node and find the closest neighbour
     public void computeShortestToNeighbours(Intersection intersect, int hour)
     {
     	//Iterate over all neighbours
     	for(TrafficLoad route : TrafficLoad.getNeighbours(intersect))
     	{
     		//the getNeighbours() method returns a list of TrafficLoads representing a connection between the examined node
     		//and every other directly connected node 
     		Intersection neighbour = (route.intersection1() == intersect) ? route.intersection2() : route.intersection1();
     		//If the currently examined neighbour had already been examined skip it
     		if(visitedNodes.contains(neighbour))
     			continue;
     		
     		int shortest = getShortestDistance(intersect) + route.load()[hour];
     		//If the distance to the currently examined neighbour is the shortest so far - record it
     		if(shortest < getShortestDistance(neighbour))
     		{
     			setShortestDistance(neighbour, shortest);
     			predecessors.put(neighbour, intersect);
     		}
     			
     	}
     }
     
     public int getShortestDistance(Intersection intersect)
     {
         Integer d = shortestDistances.get(intersect);
         return (d == null) ? INFINITE_DISTANCE : d;
     }
     
     //When a shortest distance between a node and it's neighbour has been found
     //store the neighbour for later examination as next node
     //The removal and reinsertion of the neighbour is needed to rebalance the queue, greatly increasing performance
     //(The reason we use a balanced queue in the first place
     private void setShortestDistance(Intersection intersect, int distance)
     {
     	unvisitedNodes.remove(intersect);
     	shortestDistances.put(intersect, distance);
     	unvisitedNodes.add(intersect);
     }
 }
