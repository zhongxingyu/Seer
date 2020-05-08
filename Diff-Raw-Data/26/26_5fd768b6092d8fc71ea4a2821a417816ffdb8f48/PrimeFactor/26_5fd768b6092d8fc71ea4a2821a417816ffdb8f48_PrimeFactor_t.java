 import java.util.ArrayList;
 
 
 public class PrimeFactor {
 	
 	public static ArrayList<Integer> prime (int i)
 	{
		return prime(i, new ArrayList<Integer>());
	}

	public static ArrayList<Integer> prime (int i, ArrayList<Integer> list)
	{
 		if (i > 1)
 		{
			boolean iPrime = true;
			for (int j = 2; j <= i/2; j++)
			{
				if(i%j == 0)
				{
					iPrime = false;
					prime(j, list);
				}
			}
			if (iPrime)
			{
				list.add(i);
			}
			return list;
 		}
 		return null;
 	}
 }
