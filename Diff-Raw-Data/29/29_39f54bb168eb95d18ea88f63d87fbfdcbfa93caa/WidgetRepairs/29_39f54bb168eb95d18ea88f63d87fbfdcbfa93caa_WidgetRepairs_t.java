 package srm150div2;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Subramaniam S
  * Date: 6/14/13
  * Time: 1:02 PM
  */
 public class WidgetRepairs
 {
 	public int days(int[] arrivals, int numPerDay)
 	{
 		int sum = 0;
		int balance = 0;
		for (int i=0; i<arrivals.length; i++)
 		{
			balance = balance+arrivals[i];
			if (balance != 0)
 			{
				sum++;
				if (balance > numPerDay)
					balance = balance - numPerDay;
				else
					balance = 0;
 			}
 		}

		if (balance != 0)
		{
			sum += balance/numPerDay;
			if (balance%numPerDay != 0)
				sum++;
		}
 		return sum;
 	}
 
 	public static void main(String[] args)
 	{
 		WidgetRepairs obj = new WidgetRepairs();
 
 		System.out.println(obj.days(new int[] { 10, 0, 0, 4, 20 }, 8));
 		System.out.println(obj.days(new int[] { 0, 0, 0 }, 10));
 		System.out.println(obj.days(new int[] { 100, 100 }, 10));
 		System.out.println(obj.days(new int[] { 27, 0, 0, 0, 0, 9 }, 9));
 		System.out.println(obj.days(new int[] { 6, 5, 4, 3, 2, 1, 0, 0, 1, 2, 3, 4, 5, 6 }, 3));
		System.out.println(obj.days(new int[] { 100 }, 3));
 	}
 }
