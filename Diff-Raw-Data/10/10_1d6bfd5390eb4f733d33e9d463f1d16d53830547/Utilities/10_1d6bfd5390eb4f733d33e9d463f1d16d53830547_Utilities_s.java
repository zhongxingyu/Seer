 package GPFinalProject;
 
 import java.util.Random;
 
 public class Utilities {
 
 	public static int GetRandomNumber(int minimumNum, int maximumNum)
 	{
 		int diff = maximumNum - minimumNum +1;
 		Random rand = new Random();
 		int output = rand.nextInt(diff);
 		output += minimumNum;		
 		return output;
 	}
 	
 }
