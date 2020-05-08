 import java.io.*;
 import java.util.*;
 
 class SUMOFPOW
 {
   Integer a,b;
   Map<Integer,Long> lookupTable;
   public SUMOFPOW(Integer a, Integer b)
   {
     this.a = a;
     this.b = b;
     lookupTable = new HashMap<Integer,Long>();
   }
 
   public Long computeExponential(int n)
   {
     //base cases
     if(n == 0)
     {
      return 1L;
     }
     else if(n == 1)
     {
       return new Long(a);
     }
     else
     {
       if(lookupTable.get(n) != null)
       {
         return lookupTable.get(n);
       }
       else
       {
         lookupTable.put(n, a * computeExponential(n-1) - b * computeExponential(n-2));
         return lookupTable.get(n);
       }
     }
   }
 }
 
 class Main
 {
   public static void main(String [] args) throws IOException
   {
     BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
     int numTests = Integer.parseInt(br.readLine());
     StringTokenizer strtok = null;
     for(int i=0; i<numTests; ++i)
     {
       strtok = new StringTokenizer(br.readLine());
       SUMOFPOW obj = new SUMOFPOW(Integer.parseInt(strtok.nextToken()),Integer
           .parseInt(strtok.nextToken()));
       System.out.println(obj.computeExponential(Integer.parseInt(strtok
               .nextToken())));
     }
   }
 }
