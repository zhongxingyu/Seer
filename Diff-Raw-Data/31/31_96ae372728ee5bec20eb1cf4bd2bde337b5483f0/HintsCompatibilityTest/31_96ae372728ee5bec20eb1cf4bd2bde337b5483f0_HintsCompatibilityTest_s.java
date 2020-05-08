 package net.anatolich.mahjong.test;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import net.anatolich.mahjong.game.AvailableMove;
 import net.anatolich.mahjong.game.capabilities.Hints;
 import net.anatolich.mahjong.game.capabilities.NoHintsException;
 import static net.anatolich.mahjong.test.PieceBuilder.makePiece;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import org.junit.Test;
 
 /**
  * Set of test that ensures {@link Hints} implementation contract compatibility.
 * <p>In order to use compatibility test one must implement {@link #createHints(java.util.List)} method so it will 
  * create new Hints instance populated with provided hints.</p>
 * 
  * @author Dmytro Kovalchuk
  * @since 0.2
  */
 public abstract class HintsCompatibilityTest {
    
     @Test(expected = NoHintsException.class)
     public void nextMethodOnEmptyHintsThrowsException(){
        Hints hints = createHints(Collections.EMPTY_LIST);
         assertFalse("Hints must be empty", hints.hasHints());
         hints.nextHint(); // Exception is expected
     }
    
     @Test
     public void nextMethodReturnsHintsCyclically(){
         final List<AvailableMove> testHints = getTestHints();
         final AvailableMove hint1 = testHints.get(0);
         final AvailableMove hint2 = testHints.get(1);
        
         Hints hints = createHints(testHints);
        
         int hintNumber = 1;
         assertEquals(String.format("Incorrect hint no %s", hintNumber++), hint1, hints.nextHint());
         assertEquals(String.format("Incorrect hint no %s", hintNumber++), hint2, hints.nextHint());
         assertEquals(String.format("Incorrect hint no %s", hintNumber++), hint1, hints.nextHint());
         assertEquals(String.format("Incorrect hint no %s", hintNumber++), hint2, hints.nextHint());
     }
 
     private List<AvailableMove> getTestHints() {
        return Arrays.asList(new AvailableMove[]{
            new AvailableMove(makePiece().bamboo().one().at(0,0,0), makePiece().bamboo().one().at(2,0,0)),
            new AvailableMove(makePiece().bamboo().two().at(0,2,0), makePiece().bamboo().two().at(2,2,0))
        });
     }
 
     protected abstract Hints createHints(List<AvailableMove> testHints);
 }
