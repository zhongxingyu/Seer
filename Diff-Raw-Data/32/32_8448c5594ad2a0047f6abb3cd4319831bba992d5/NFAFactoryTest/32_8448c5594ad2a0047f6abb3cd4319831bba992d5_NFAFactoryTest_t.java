 package Test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import Generator.NFA.NFA;
 import Generator.NFA.NFAFactory;
 import Generator.NFA.NFASimulator;
 import Generator.Token.op_code;
 
 public class NFAFactoryTest {
 	NFASimulator NFASim;
 	NFAFactory Factory;
 
 	@Before
 	public void setUp() throws Exception {
 		NFASim = new NFASimulator();
 		Factory = new NFAFactory();
 	}
 	
	//@Test
 	public void testEpsilon() {
 		NFA epsNFA = NFA.EpsilonNFA();
 		
 		epsNFA = Factory.build("$NULL ()()()()()*||||a");
 		
 		//System.out.println(epsNFA);
 		
 		assertEquals("a", NFASim.parse(epsNFA, "a").token);
 		assertEquals("Failed", NFASim.parse(epsNFA, " ").token);
 	}
 	
	//@Test
 	public void testSample() {
 		NFA test = Factory.build("$TEST (1)(2)*(3)(4|5|6)+(|^-)");
 		
 		//System.out.println(test);
 		
 		assertEquals("135", NFASim.parse(test, "135").token);
 		assertEquals("1223654^-", NFASim.parse(test, "1223654^-").token);
 		assertEquals("1223654", NFASim.parse(test, "1223654").token);
 		assertEquals("13555^-", NFASim.parse(test, "13555^-").token);
 		
 		test = Factory.build("$TEST \\[(\\ )*\\]");
 		String code = "[     ]";
 		assertEquals(code, NFASim.parse(test, code).token);
 		code = "[             ]";
 		assertEquals(code, NFASim.parse(test, code).token);
 	}
	
	@Test
	public void testAddMultExpr() {
		NFA test = Factory.build("$TEST (0|1|2|3|4|5|6|7|8|9)+(\\ |((\\+|\\*)(\\ )*(0|1|2|3|4|5|6|7|8|9)+)*)*");
		String code = "12 + 56 + 78 * 9";
		System.out.println(NFASim.parse(test, code));
		assertEquals(code, NFASim.parse(test, code).token);
	}
	
	@Test
	public void testDoubleStar() {
		NFA test = Factory.build("$TEST (a|()*)*");
		System.out.println(test);
		String code = "aaaaaa";
		System.out.println(NFASim.parse(test, code));
		assertEquals(code, NFASim.parse(test, code).token);
	}
 }
