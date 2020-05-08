 package bio;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
 
 public class IsEulerTest {
 	@Test
 	public void testIsEulerFull() throws WrongOligonucleotidesLengthException {
 		Graph graph = new Graph("ATC", "TCA", "CAT");
 		assertTrue(graph.isFullEuler());
 	}
 
 	@Test
 	public void testIsNotEulerFull() throws WrongOligonucleotidesLengthException {
 		Graph graph = new Graph("ATC", "TCA", "CAG", "AGT", "GTC", "CAT");
 		assertFalse(graph.isFullEuler());
 	}
 
 	@Test
 	public void testIsNotEulerFull2() throws WrongOligonucleotidesLengthException {
 		Graph graph = new Graph("ATC", "TCA", "CAT", "TCG", "CAG");
 		assertFalse(graph.isFullEuler());
 	}
 
 	@Test
 	public void testIsNotEulerHalf() throws WrongOligonucleotidesLengthException {
 		Graph graph = new Graph("ATC", "TCA", "CAT");
 		assertFalse(graph.isHalfEuler());
 	}
 
 	@Test
 	public void testIsHalfEuler() throws WrongOligonucleotidesLengthException {
 		Graph graph = new Graph("ATC", "TCA", "CAG", "AGT", "GTC", "CAT");
 		assertTrue(graph.isHalfEuler());
 	}
 
 	@Test
 	public void testIsNotHalfEuler2() throws WrongOligonucleotidesLengthException {
 		Graph graph = new Graph("ATC", "TCA", "CAT", "TCG", "CAG");
 		assertFalse(graph.isHalfEuler());
 	}
 
 	@Test
 	public void testNotConnected() throws WrongOligonucleotidesLengthException {
 		Graph graph = new Graph("ATC", "TCA", "CAT", "GGG");
		assertTrue(graph.isFullEuler());
 		assertFalse(graph.isHalfEuler());
 	}
 }
