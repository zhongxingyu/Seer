 import java.util.Scanner;
 public class ArrayMaxFinder
 {
    public static int arrayMax(int[] arr, int first)
    {
       if(first == arr.length)
       {
          return 0;
       }
       int temp = arrayMax(arr, first +1);
       if(arr[first] >temp)
       {
          temp = arr[first];
       }
       return temp;
    }
    
    
    public static void main(String[] args)
    {
       Scanner scan = new Scanner(System.in);
       int[] arr = new int[10];
       int i = 0 ;
       System.out.println("enter 10 integers");
       
      while ( i < 10 )
       {
         while( scan.hasNextInt())
         {
             arr[i++] = scan.nextInt();
         }
       }
       System.out.println("Thanks! The max is: "+ arrayMax(arr, 0));
    }
 }
