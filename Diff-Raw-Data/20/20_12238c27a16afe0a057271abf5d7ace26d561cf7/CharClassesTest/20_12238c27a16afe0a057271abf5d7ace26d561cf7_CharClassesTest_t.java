 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.junit.Test;
 
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

 public class CharClassesTest {
 
   @Test
   public void naiveCharClassesShouldProduceValidMap() throws BadSpecException {
     Map<String, Set<Character>> classes = CharClasses.buildMapFromSpec("$DIGIT [0-2]\n$ALPHA [a-cA-B]");
     Set<Character> expectedDigits = new HashSet<Character>(Arrays.asList(new Character[]{'0','1','2'}));
     Set<Character> expectedAlpha = new HashSet<Character>(Arrays.asList(new Character[]{'a','b','c', 'A', 'B'}));
     assertEquals(classes.get("$DIGIT"), expectedDigits);
     assertEquals(classes.get("$ALPHA"), expectedAlpha);
   }
 
   @Test
   public void charClassesShouldHandleEmptyClass() throws BadSpecException {
     CharClasses.buildMapFromSpec("EMPTY []");
   }
 
   @Test
   public void shouldNotAcceptInvalidRanges() {
     try {
       CharClasses.buildMapFromSpec("$A [9-9]");
       fail();
     } catch (BadSpecException e) {
       System.out.println("Caught A BadSpecException");
     }
     try {
       CharClasses.buildMapFromSpec("$A [B-A]");
       fail();
     } catch (BadSpecException e) {
       // Shouldn't fail
     }
   }
 
   @Test
   public void shouldNotAcceptMissingInOperator() {
     try {
       CharClasses.buildMapFromSpec("$DIGIT [0-9]\n$A [^0-5] EN $DIGIT");
     } catch (BadSpecException e) {
       System.out.println("Caught A BadSpecException");
     }
   }
 
 }
