 /**
  * Tests if abrt-java-connector successfully distinguish between completely
  * caught exception and uncaught exception inside of try finally block with not
  * catch statement.
  * @author Jakub Filak &lt;jfilak@redhat.com&gt;
  */
 public class TryFinallyTest {

    public static void run()
    {
         try {
             System.out.println("TryFinallyTest.java exceptions inside try without catch and with finally");
             SimpleTest.throwAndCatchAllExceptions();
             System.out.println("continue...");
             SimpleTest.throwAndDontCatchException();
             System.out.println("A message after an uncaught exception");
         }
         finally {
             System.out.println("finally block");
         }
     }

    /**
     * Entry point to this TryFinally test.
     */
    public static void main(String args[]) {
        run();
        System.exit(0);
    }
 }
 
 // finito
 
