 /**
  * 
  */
 package routefiend;
 
 import java.util.ArrayList;
 
/*
 public class RoutingTest 
 {
     private static final String JAVABRIDGE_PORT="8097";
     private static final php.java.bridge.JavaBridgeRunner runner = php.java.bridge.JavaBridgeRunner.getInstance(JAVABRIDGE_PORT);
     
 	private static DjikstraSolver solver;
 
 	public static void main(String[] args) 
 	{
 		try
 		{
 			runner.waitFor();
 		} catch (InterruptedException e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	public static void  init(String address, String username, String password)
 	{
 		solver = new DjikstraSolver(address, username, password);
 	}
 	
	public static int[] findShortestDistance(int startId, int endId, int hour)
 	{
		return solver.findShortestDistance(startId, endId, hour);
 	}
 }
 //*/
 
 
//*
 public class RoutingTest 
 {
 	DjikstraSolver solver;
 
 	public RoutingTest()
 	{
 
 		solver = new DjikstraSolver("//localhost/RouteFiend", "root", "ext3!fs");
 		//solver.findShortestDistance(1, 33, 9);
 		int[] destinations = {1, 33, 62};
		String[] times = {"09:00:00", "10:00:00", "08:00:00"};
 		
 		ArrayList<Integer> solution = solver.solveForDestinationsAndTimes(destinations, times);
 		for(Integer id : solution)
 		{
 			if(id == -1)
 			{
 				System.out.print(" _Unreachables follow_");
 				continue;
 			}
 			System.out.print(" " + Intersection.getIntersectionForId(id).name());
 		}
 		
 	}
 
 	@SuppressWarnings("unused")
 	public static void main(String[] args) 
 	{
 		RoutingTest test = new RoutingTest();
 	}
 }
 //*/
