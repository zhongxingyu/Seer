 package test.DynamicProgramming;
 import java.util.*;
 import main.DynamicProgramming.MountainClimbing;
 
 public class TestMountainClimbing
 {
 	public static boolean success = true;
 	public static void main(String[] args)
 	{
		MountainClimbing MC = new MountainClimbing();
 		testOne(true);
 		if(!success)
 		{
 			System.out.println("FAILED: TestMountainClimbing");
 			System.exit(1);
 		}
 		testTwo(true);
 		if(!success)
 		{
 			System.out.println("FAILED: TestMountainClimbing");
 			System.exit(1);
 		}
 		testThree(true);
 		if(!success)
 		{
 			System.out.println("FAILED: TestMountainClimbing");
 			System.exit(1);
 		}
 		testFour(true);
 		if(!success)
 		{
 			System.out.println("FAILED: TestMountainClimbing");
 			System.exit(1);
 		}
 		testFive(true);
 		if(!success)
 		{
 			System.out.println("FAILED: TestMountainClimbing");
 			System.exit(1);
 		}
 		else
 		{
 			System.exit(0);
 		}
 	}
 
 	public static void testOne(boolean printDebug)
 	{
 		long[][] test_array = {{0,0,0,10,0},
 								{0,0,0,10,0},
 								{0,0,0,10,0},
 								{0,0,0,10,0}};
 		
 		long result = MC.climbing(test_array);
 		
 		if(result != 40)
 		{
 			success = false;
 			return;
 		}
 		else
 		{
 			return;
 		}
 	}
 	public static void testTwo(boolean printDebug)
 	{
 		long[][] test_array = {{10,0,0,0},
 								{0,10,0,0},
 								{0,0,10,0},
 								{0,0,0,10}};
 
 		long result = MC.climbing(test_array);
 
 		if(result != 40)
 		{
 			success = false;
 			return;
 		}
 		else
 		{
 			return;
 		}
 	}
 	
 	public static void testThree(boolean printDebug)
 	{
 		long[][] test_array = {{0,0,0,10},
 								{0,0,10,0},
 								{0,10,0,0},
 								{10,0,0,0}};
 
 		long result = MC.climbing(test_array);
 		
 		if(result != 40)
 		{
 			success = false;
 			return;
 		}
 		else
 		{
 			return;
 		}
 	}
 	
 	public static void testFour(boolean printDebug)
 	{
 		long[][] test_array = {{5,0,0,0},
 								{5,0,0,5},
 								{5,0,0,5},
 								{5,0,0,30}};
 		
 		long result = MC.climbing(test_array);
 		
 		if(result != 40)
 		{
 			success = false;
 			return;
 		}
 		else
 		{
 			return;
 		}
 	}
 	
 	public static void testFive(boolean printDebug)
 	{
 		long[][] test_array = {{5,0,0,30},
 								{5,0,0,5},
 								{5,0,0,5},
 								{5,0,0,0}};
 		
 		long result = MC.climbing(test_array);
 		
 		if(result != 40)
 		{
 			success = false;
 			return;
 		}
 		else
 		{
 			return;
 		}
 	}
 }
