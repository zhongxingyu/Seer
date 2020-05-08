 package lecture.tdd;
 
import static org.junit.Assert.*;

 import org.junit.Test;
 
 public class Classifier1Test {
 	@Test public void is_factor() {
 		assertTrue(Classifier1.isFactor(1, 10));
 	}
 }
