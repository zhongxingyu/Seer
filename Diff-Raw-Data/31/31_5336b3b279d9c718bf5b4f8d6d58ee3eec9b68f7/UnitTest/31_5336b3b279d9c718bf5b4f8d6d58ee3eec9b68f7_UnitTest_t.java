 package sugarj;
 
 import util.Util;
 import static org.junit.Assert.*;
 import org.junit.Test;
 
 public class UnitTest {
 
	public int executeGrammar(String grammar, String file, String start) {
 		try {
			int resp = Util.testeGrammar(grammar, file, start);
 			if(resp < 0)
 				resp = -1;
 			else
 				resp = 1;
			return resp;
 		} catch (Exception e) {
 			System.out.println(e);
			return -1;
 		}
 	}
 	
	@Test
	public void TestPair() {
		int resp = executeGrammar("input/languages/SugarJ/sugarj.apeg",
                                  "input/languages/SugarJ/examples/Pair.sugj",
                                  "sugar_declaration");
		assertEquals(1, resp);
	}

	@Test
	public void TestClousure() {
		int resp = executeGrammar("input/languages/SugarJ/sugarj.apeg",
                "input/languages/SugarJ/examples/Closure.sugj",
                "sugar_declaration");
		assertEquals(1, resp);
	}
 	
 	
 
 	
 }
 
