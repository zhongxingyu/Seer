 import java.io.*;
 import java.util.*;
 
 class Main
 {
   public static void main(String [] args) throws IOException
   {
     BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
     String [] input = null;
     int numTests = Integer.parseInt( br.readLine() );
     while(numTests-- > 0)
     {
       //read line1 which has N and C
       input = br.readLine().split(" ");
       int N = Integer.parseInt(input[0]);
       long C = Long.parseLong(input[1]);
       //Read line2 which has the Ak values
       input = br.readLine().split(" ");
       for(int i=0; i<input.length; ++i)
       {
        C -= Integer.parseInt(input[i]);
       }
      if(C >= 0) System.out.println("Yes");
       else System.out.println("No");
     }
   }
 }
