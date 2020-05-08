 /**
  * 
  */
 package Test;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import Generator.DFA.DFA;
 import Generator.DFA.DFAFactory;
 import Generator.NFA.NFA;
 import Generator.NFA.NFAFactory;
 import Generator.NFA.NFASimulator;
 
 /**
  * @author eric
  *
  */
 public class DFAFactoryTest {
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	/**
 	 * Test method for {@link Generator.DFA.DFAFactory#build(Generator.NFA.NFA)}.
 	 */
 	//@Test
 	public void testBuild() {
 		NFAFactory nfafactory = new NFAFactory();
 		NFASimulator nfasim = new NFASimulator();
 		DFAFactory dfafactory = new DFAFactory();
 		
 		String code = "$DIGIT   [0-9]";
 		nfafactory.build(code);
 		code = "$ALPHA   [a-zA-Z]";
 		nfafactory.build(code);
 		code = "$MULT   [*]";
 		nfafactory.build(code);
 		code = "$ADD    [+]";
 		nfafactory.build(code);
 		code = "$NUMBER    ($DIGIT)+";
 		code = "$VAR       abc(|$MULT|$ADD)($ALPHA|$DIGIT)*Z";
 		NFA nfa = nfafactory.build(code);
 		System.out.println(nfasim.parse(nfa, "abc999"));
 		System.out.println(nfasim.parse(nfa, "bc999"));
 		
 		DFA dfa = dfafactory.build(nfa);
 		
 		DFA min_dfa = dfafactory.minimize(dfa);
 		
 		//System.out.println(dfa);
 		System.out.println(min_dfa);
 	}
 	
 	@Test
 	public void testEpsilon() {
 		NFAFactory nfafactory = new NFAFactory();
 		NFASimulator nfasim = new NFASimulator();
 		DFAFactory dfafactory = new DFAFactory();
 		
 		String code = "$VAR       |";
 		NFA nfa = nfafactory.build(code);
 		
 		DFA dfa = dfafactory.build(nfa);
 		
 		DFA min_dfa = dfafactory.minimize(dfa);
 		
 		//System.out.println(dfa);
 		System.out.println(min_dfa);
 	}
 
 }
