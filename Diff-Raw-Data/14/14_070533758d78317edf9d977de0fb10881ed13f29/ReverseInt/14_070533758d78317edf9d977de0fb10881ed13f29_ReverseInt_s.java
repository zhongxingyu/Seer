 import java.util.InputMismatchException;
 import java.util.Scanner;
 
 
 public class ReverseInt
 {
 	
	private static int flipInt(int num)
 	{
 		boolean sign = num < 0;
 		num = Math.abs(num);
 		
		int ret = 0;
 		while (num > 0)
 		{
 			ret *= 10;
 			ret += num % 10;
 			num /= 10;
 		}
 		if (sign) 
 			ret = -ret;
 		
 		return ret;
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args)
 	{
 		Scanner sc = new Scanner(System.in);
 		System.out.print("Input an integer: ");
		int val;
 		try
 		{
			val = sc.nextInt();
 		}
 		catch (InputMismatchException e)
 		{
 			System.out.println("That's not an integer!");
 			sc.close();
 			return;
 		}
 		
		System.out.println("The flipped value is " + flipInt(val));
 		sc.close();
 	}
 
 }
