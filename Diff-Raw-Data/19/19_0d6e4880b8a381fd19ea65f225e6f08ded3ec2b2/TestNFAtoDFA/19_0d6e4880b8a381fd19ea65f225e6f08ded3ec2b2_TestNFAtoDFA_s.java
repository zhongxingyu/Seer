 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 
 public class TestNFAtoDFA {
 
 	@Test
 	public void test() {
 		NFA nfa = new NFA(new State("Start"));
 		State start = nfa.getStart();
 		
 		State state1 = new State("1");
 		nfa.addState(state1);
 		state1.setAccepts(true);
 		
 		nfa.addTransition(start, 'h', state1);
 		
 		DFA dfa = nfa.toDFA();
 		
 		System.out.println(nfa.toString());
 		System.out.println(dfa.toString());
 	}
 
 }
