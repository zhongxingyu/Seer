 import java.util.Scanner;
 import java.util.Locale;
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 
 class TicketLottery {
 
     public static void main(String[] args) {
         Scanner sc = new Scanner(System.in);
 
         System.out.println(getProbabilities(sc.nextLine()));
     }
 
     public static String getProbabilities(String input) {
 
         DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(10);
         df.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
 
         int[] numbers = splitNumbers(input);
 
         /* m : total # of people
          * n : # of winners
          * t : # tickets per winners
          * p : people in the team
          */
 
         int m = numbers[0],
             n = numbers[1],
             t = numbers[2],
             p = numbers[3];
 
         // needed # of winners
         int w = (int)Math.ceil(p / (t + 0.0));
 
         if (m == n) return "1";
        if (w  > n) return "0";
 
         return df.format(getProbability(m, n, w, p));
     }
 
     private static double getProbability(int m, int n, int w, int p) {
 
         if ( n == 0 ) {
            return w <= 0 ? 1 : 0;
         }
 
         return        p  / (m + 0.0) * getProbability(m-1, n-1, w-1, p-1)
                + (m - p) / (m + 0.0) * getProbability(m-1, n-1, w  , p  );
 
     }
 
     private static int[] splitNumbers(String input) {
         String[] parts = input.split(" ");
 
         return new int[] {
             Integer.parseInt(parts[0]),
             Integer.parseInt(parts[1]),
             Integer.parseInt(parts[2]),
             Integer.parseInt(parts[3])
         };
     }
 }
