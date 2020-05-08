 
 public class Runtime
 {
 	public static void printInt(int x)
 	{
 		System.out.println(x);
 	}
 	
 	public static void printDouble(double x)
 	{
 		System.out.println(x);
 	}
 	
 	public static int readInt()
 	{
		//java.util.Scanner in = new java.util.Scanner(System.in);
		return scanner.nextInt();
 	}
 	
 	public static double readDouble()
 	{
		//java.util.Scanner in = new java.util.Scanner(System.in);
		return scanner.nextDouble();
 	}
 	
 	public static void printString(String s)
 	{
 		System.out.println(s);
 	}
  
  static java.util.Scanner scanner = new java.util.Scanner(System.in) ;
}
