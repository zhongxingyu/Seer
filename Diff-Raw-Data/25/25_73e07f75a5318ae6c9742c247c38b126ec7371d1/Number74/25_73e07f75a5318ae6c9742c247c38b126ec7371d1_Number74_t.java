 import java.util.ArrayList;
 public class Number74
 {
 	public static void main(String[] args)
 	{
 		Stopwatch s = new Stopwatch();
 		int finalCount = 0;
		for (int startingNumber = 1000; startingNumber < 1000000; startingNumber++)
 		{
			int countTerms = 1; //counting startingNumber as 1
 			ArrayList<Integer> usedTerms = new ArrayList<Integer>();
 			boolean repeatTerms = false;
 			int copyNumber = startingNumber;
 			while (countTerms < 60)
 			{
 				int sumDigits = 0;
 				while (copyNumber >= 1)
 				{
 					int digit = copyNumber % 10;
 					sumDigits += factorial(digit);
 					copyNumber = copyNumber / 10;
 				}
 				for (int used : usedTerms)
 				{
 					if (used == sumDigits)
 					{
 						//In a loop
 						repeatTerms = true;
 						break;
 					}
 				}
 				if (repeatTerms)
 					break;
 				usedTerms.add(sumDigits);
 				countTerms++;
 				copyNumber = sumDigits;
 			}
 			if (countTerms == 60)
 				finalCount++;
 		}
 		System.out.println(finalCount);
 		System.out.println(s.elapsedTime());
 	}
 	public static int factorial(int i)
 	{
		if (i <= 1)
 			return 1;
 		else
 			return i * factorial(i-1);
 	}
 }
