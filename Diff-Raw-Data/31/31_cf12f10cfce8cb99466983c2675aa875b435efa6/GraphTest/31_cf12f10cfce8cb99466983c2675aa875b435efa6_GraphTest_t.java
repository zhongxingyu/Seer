 package bio;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.Arrays;
 
 import org.junit.Test;
 
 public class GraphTest {
 
 	@Test
 	public void testGenerateVerices() {
 		assertEquals(Arrays.asList(""), Graph.generateVerices(0));
 		assertEquals(Arrays.asList("A", "C", "T", "G"), Graph.generateVerices(1));
 		assertEquals(Arrays.asList("AA", "AC", "AT", "AG", "CA", "CC", "CT", "CG", "TA", "TC", "TT", "TG", "GA", "GC", "GT", "GG"),
 				Graph.generateVerices(2));
 	}
 }
