 import java.util.ArrayList;
 
 
 public class PrimeFactor {
 	
 	public static ArrayList<Integer> prime (int i)
 	{
		ArrayList<Integer> temp = new ArrayList<Integer>();
		
 		if (i > 1)
 		{
			temp.add(i);
			return temp;
 		}
		
 		return null;
 	}
 }
