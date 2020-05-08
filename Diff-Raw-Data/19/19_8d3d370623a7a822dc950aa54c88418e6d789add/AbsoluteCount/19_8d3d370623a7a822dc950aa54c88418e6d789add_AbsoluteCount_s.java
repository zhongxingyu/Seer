 import java.util.HashMap;
 
 
 public class AbsoluteCount {
 	
 	public static void main(String[] args)
 	{
 		System.out.println(solution(new int[]{-5, -3, -1, -5, -3, -1, 0, 3, 6}));
 	}
 	
 	public static int solution(int[] A) {
         int count = A.length;
        //boolean[] seen = new boolean[A.length];
         HashMap<Integer, Boolean> seen = new HashMap<Integer, Boolean>();
         for (int i = 0; i < A.length; i++)
         {
        	if (A[i] == 0)
        	{
        		count--;
        	}
        	else
        	{
         		//if an element has already been encountered, skip it
        		Integer key = A[i];
         		if (seen.get(key) == null)
         		{
         			seen.put(key, new Boolean(true));
         		}
         		else if (seen.get(key) == false)
         		{
         			seen.put(key, new Boolean(true));
         		}
         		else
         			count--;
        	}
         }
         return count;
     }
 }
