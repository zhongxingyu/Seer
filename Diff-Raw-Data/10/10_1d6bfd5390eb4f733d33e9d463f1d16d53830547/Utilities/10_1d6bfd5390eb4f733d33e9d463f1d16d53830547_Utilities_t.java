 package GPFinalProject;
 
 import java.util.Random;
 
 public class Utilities {
 
 	public static int GetRandomNumber(int minimumNum, int maximumNum)
 	{
 		int diff = maximumNum - minimumNum +1;
		if (diff < 1)
		{
			return 0;
		}
 		Random rand = new Random();
 		int output = rand.nextInt(diff);
 		output += minimumNum;		
 		return output;
 	}
 	
	public static double GetRandomDouble()
	{
		return Math.random();
	}
	
 }
