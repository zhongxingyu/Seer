 package sugarj;
 
 import util.Util;
 import static org.junit.Assert.*;
 import org.junit.Test;
 
 public class UnitTest {
 
	@Test
	public void TestPair() {
 		try {
			int resp = Util.testeGrammar("input/languages/SugarJ/sugarj.apeg",
					                     "input/languages/SugarJ/examples/Pair.sugj", "sugar_declaration");
 			if(resp < 0)
 				resp = -1;
 			else
 				resp = 1;
			assertEquals(1, resp);
 		} catch (Exception e) {
 			System.out.println(e);
			assertTrue(false);
 		}
 	}

 	
 	
 	
 
 	
 }
 
