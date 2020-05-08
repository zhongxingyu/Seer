 package net.yangeorget.jelly;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 public class PuzzleTest {
     private static final Logger LOG = LoggerFactory.getLogger(PuzzleTest.class);
 
     @Test(groups = "fast")
     public void test1() {
         solve(1);
     }
 
     @Test(groups = "fast")
     public void test() {
         solve(2);
     }
 
     @Test(groups = "fast")
     public void test3() {
         solve(3);
     }
 
     @Test(groups = "fast")
     public void test4() {
         solve(4);
     }
 
     @Test(groups = "fast")
     public void test5() {
         solve(5);
     }
 
     @Test(groups = "fast")
     public void test6() {
         solve(6);
     }
 
     @Test(groups = "fast")
     public void test7() {
         solve(7);
     }
 
     @Test(groups = "fast")
     public void test8() {
         solve(8);
     }
 
     @Test(groups = "fast")
     public void test9() {
         solve(9);
     }
 
     @Test(groups = "fast")
     public void test10() {
         solve(10);
     }
 
     @Test(groups = "fast")
     public void test11() {
         solve(11);
     }
 
     @Test(groups = "fast")
     public void test12() {
         solve(12);
     }
 
     @Test(groups = "fast")
     public void test13() {
         solve(13);
     }
 
     @Test(groups = "fast")
     public void test14() {
         solve(14);
     }
 
     @Test(groups = "fast")
     public void test15() {
         solve(15);
     }
 
     @Test(groups = "fast")
     public void test16() {
         solve(16);
     }
 
     @Test(groups = "slow")
     public void test17() {
         solve(17);
     }
 
     @Test(groups = "fast")
     public void test18() {
         solve(18);
     }
 
     @Test(groups = "fast")
     public void test19() {
         solve(19);
     }
 
     @Test(groups = "fast")
     public void test20() {
         solve(20);
     }
 
     @Test(groups = "fast")
     public void test21() {
         solve(21);
     }
 
     @Test(groups = "fast")
     public void test22() {
         solve(22);
     }
 
     @Test(groups = "fast")
     public void test23() {
         solve(23);
     }
 
     @Test(groups = "slow")
     public void test24() {
         solve(24);
     }
 
     @Test(groups = "fast")
     public void test25() {
         solve(25);
     }
 
     @Test(groups = "fast")
     public void test26() {
        solve(26);
     }
 
     private void solve(final int level) {
         final Game game = new GameImpl(Board.LEVELS[level - 1]);
         final long time = System.currentTimeMillis();
         final State state = game.solve();
         LOG.info("solved " + level + " in " + (System.currentTimeMillis() - time) + " ms");
         Assert.assertNotNull(state);
         game.explain(state);
     }
 
     public static void main(final String[] args) {
         for (int i = 0; i < Board.LEVELS.length; i++) {
             new GameImpl(Board.LEVELS[i]).solve();
         }
     }
 }
