 package au.net.netstorm.boost.test.cases;
 
 import au.net.netstorm.boost.test.automock.InteractionTestStrategy;
 import au.net.netstorm.boost.test.automock.UsesMocks;
 
 public class StrategyTestCase extends BoooostCase {
     // FIX 1524 Remove the cast.
     private TestStrategy strategy = new InteractionTestStrategy((UsesMocks) this);
 
     public void runBare() throws Throwable {
         initialise();
         try {
             super.runTest();
             verify();
         } finally {
             destroy();
         }
     }
 
     protected void verify() {
         strategy.verify();
     }
 
     private void initialise() {
         strategy.init();
     }
 
     private void destroy() {
         strategy.destroy();
     }
 }
