 package j3chess.test;
 
 import static org.junit.Assert.fail;
 import j3chess.Field;
 import j3chess.HumanController;
 import j3chess.Player;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class HumanControllerSelectionTest {
 	
 	/** Human controller used for testing. */
     private static HumanController mHumanController;
 
     /** Set up the test environment. */
     @BeforeClass
     public static void setup() {
        mHumanController = new HumanController(Player.ONE);
     }
 
     /**
      * This tests wether a HumanController without a game will
     * throw a proper exception, instead of showing undefined
     * behaviour.
      */
     @Test
     public final void failWithoutGame() {
         try {
             mHumanController.notifyFieldClicked(new Field(0, 0), null);
         } catch (Exception e) {
            // Test passed
         }
        fail();
     }
 
     /**
      * This tests wether a HumanController without a game and
     * a field will throw a proper exception, instead of showing
     * undefined behaviour.
      */
     @Test
     public final void failWithoutValues() {
         try {
             mHumanController.notifyFieldClicked(null, null);
         } catch (Exception e) {
            // Test passed
         }
        fail();
     }
 }
