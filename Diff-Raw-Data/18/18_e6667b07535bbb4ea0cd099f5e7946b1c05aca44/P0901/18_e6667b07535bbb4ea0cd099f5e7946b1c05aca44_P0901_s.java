 /* P0901
  * A child is running up a staircase with n steps, and can hop either 1 step,
  * 2 steps, or 3 steps at a time. Implement a method to count how many possible
  * ways the child can run up the stairs.
  * */
 
 public class P0901 {
  public static int countWays(int n) {
     if (n == 0)
       return 1;
     if (n < 0)
       return 0;
 
     return countWays(n - 1) + countWays(n - 2) + countWays(n - 3);
 
   }
 
  public static int countWaysDP(int n) {
    int[] record = new int[n + 1];
     for (int i = 0; i < n + 1; i++)
       record[i] = -1;
     record[0] = 0;
 
     return countWaysDPHelper(n, record);
   }
 
  private static int countWaysDPHelper(int n, int[] record) {
     if (n < 0)
       return 0;
 
     if (n == 0)
       return 1;
 
     if (record[n] > 0)
       return record[n];
 
     record[n] = countWaysDPHelper(n - 1, record)
               + countWaysDPHelper(n - 2, record)
               + countWaysDPHelper(n - 3, record);
 
     return record[n];
 
   }
 
   public static void main(String[] args) {
    //int res = P0901.countWays(3);
    int res = P0901.countWaysDP(3);
 
     System.out.println(res);
   }
 }
