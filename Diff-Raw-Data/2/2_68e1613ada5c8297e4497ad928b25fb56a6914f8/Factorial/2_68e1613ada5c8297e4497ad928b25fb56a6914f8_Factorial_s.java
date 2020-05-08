 public class Factorial{
 	public static void main(String args[]){
 		int x = IO.readInt();
 		int y = factorial(x);
 		System.out.println(y);
 	}
 
 	public static int factorial(int value){
 		if(value == 1){
 			return 1;
 		}
		return value * factorial(value  - 1);
 	}
 }
