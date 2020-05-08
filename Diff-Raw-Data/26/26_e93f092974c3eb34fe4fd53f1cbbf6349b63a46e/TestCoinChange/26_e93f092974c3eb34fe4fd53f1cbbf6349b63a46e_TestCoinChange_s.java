 package dynprog;
 
 import junit.framework.Assert;
 import org.junit.Test;
 
 /**
  * User: absharma
  * Date: 4/9/12
  */
 public class TestCoinChange {
 
     @Test
     public void testDynamicProgramming() {
         CoinChange c = new CoinChange();
         int[] denominations = new int[] {2, 1};
         int num = c.dynamicProgramming(6, denominations);
         System.out.println("num = " + num);
         
         denominations = new int[] {25, 10, 5, 1};
         num = c.dynamicProgramming(67, denominations);
         System.out.println("num = " + num);
 
         denominations = new int[] {40, 25, 20, 1};
         num = c.dynamicProgramming(48, denominations);
         System.out.println("num = " + num);
     }
 
     @Test
     public void testGreedy() {
         CoinChange c = new CoinChange();
         int[] denominations = new int[] {25, 10, 5, 1};
         int[] out = c.greedy(67, denominations);
         printAndAssert(67, denominations, out, new int[] {2, 1, 1, 2});
 
         System.out.println("----------");
         out = c.greedy(17, denominations);
         printAndAssert(17, denominations, out, new int[] {0, 1, 1, 2});
 
         System.out.println("----------");
         out = c.greedy(4, denominations);
         printAndAssert(4, denominations, out,new int[] {0, 0, 0, 4});
 
         System.out.println("----------");
         denominations = new int[] {10, 5, 1};
         out = c.greedy(18, denominations);
         printAndAssert(18, denominations, out, new int[] {1, 1, 3});
 
         denominations = new int[] {10, 5, 2};
         try {
             c.greedy(18, denominations);
             Assert.fail("This not solvable - should have failed ");
         } catch (Exception e) {
             System.out.println("Expected ERROR " + e);
         }
 
         denominations = new int[] {25, 10, 5, 1};
         out = c.greedy(25, denominations);
         printAndAssert(25, denominations, out, new int[] {1, 0, 0, 0});
 
         denominations = new int[] {2, 1};
         out = c.greedy(5, denominations);
         printAndAssert(5, denominations, out, new int[] {2, 1});
 
         denominations = new int[] {25, 10, 10, 5, 1};
         out = c.greedy(67, denominations);
         printAndAssert(67, denominations, out, new int[] {2, 1, 0, 1, 2});
 
         denominations = new int[] {25, 10, 10, 5, 5, 1};
         out = c.greedy(67, denominations);
         printAndAssert(67, denominations, out, new int[] {2, 1, 0, 1, 0, 2});
 
         /**
          * Not optimal by greedy approach
          */
         denominations = new int[] {40, 25, 20, 1};
         out = c.greedy(48, denominations);
         printAndAssert(48, denominations, out, new int[] {1, 0, 0, 8});
     }
 
     private void printAndAssert(int expected, int[] denominations, int[] out, int[] expArr) {
         System.out.println("----------");
         int actual = 0;
         for (int i = 0; i < out.length; i++) {
             if(out[i] != 0) {
                 System.out.println(out[i] + " * " + denominations[i]);
                 actual = actual + out[i] * denominations[i];
             }
         }
         System.out.println("----------");
         System.out.println(actual + " : " + expected);
         Assert.assertEquals(expected, actual);
         if(expArr.length != out.length) {
             Assert.fail("arrays not same");
         }
         for(int i = 0; i < expArr.length; i++) {
             if(expArr[i] != out[i]) {
                 Assert.fail("Expected and actual array not equal @ index " + i  + expArr[i]  + " : " + out[i]);
             }
         }
     }
 
 }
