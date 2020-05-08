 import java.util.Scanner;
 
 public class ArraySummer
 {
    public static int arraySum(int[] arr, int first)
    {
       if(first == arr.length)
       {
          return 0;
       }
          return arraySum(arr,first+1) + arr[first];
    }
    
    public static void main(String[] args)
    {
       Scanner scan = new Scanner(System.in);
       int[] arr = new int[10];
       int i = 0 ;
       System.out.println("enter 10 integers");
       
      while ( i < 10 && scan.hasNextInt())
       {
             arr[i++] = scan.nextInt();
         
       }
       System.out.println("Thanks! The sum is: "+ arraySum(arr, 0));
    }
 }
