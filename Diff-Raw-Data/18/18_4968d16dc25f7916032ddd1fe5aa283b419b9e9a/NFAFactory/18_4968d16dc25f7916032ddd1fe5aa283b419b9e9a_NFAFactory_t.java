 package Generator.NFA;
 
 import java.util.Map;
 import java.util.TreeMap;
 
 import Generator.Token;
 import Generator.Token.op_code;
 import Generator.Tokenizer;
 import Generator.Character.CharacterClass;
 import Generator.Character.CharacterClassFactory;
 
 public class NFAFactory {
 	CharacterClassFactory clsFactory;
 	Map<String, NFA> cache;	// Map of built NFAs.
 	Tokenizer T;
 	
 	public NFAFactory() {
 		T = new Tokenizer("");
 		clsFactory = new CharacterClassFactory(T);
 		cache = new TreeMap<String, NFA>();
 	}
 	
 	public void clsFactory(CharacterClassFactory ccf) {
 		clsFactory = ccf;
 		clsFactory.tokenizer = T;
 	}
 	
 	public void tokenizer(Tokenizer t) {
 		T = t;
 		clsFactory.tokenizer = t;
 	}
 	
 	public void input(String in) {
 		T.tokenize(in);
 	}
 	
 	public Map<String, NFA> cache() {
 		return cache;
 	}
 	
 	/**
 	 * Builds a named NFA from the input specification. 
 	 * 
 	 * It assumes in has the form "$[token-name] [regex]".
 	 * 
 	 * Always returns a NFA, even if the input defines a CharacterClass.
 	 * CharacterClass specifications will be passed to the CharacterClassFactory.
 	 * 
 	 * @param in - regex specification
 	 * @return NFA 
 	 */
 	public NFA build(String in) {
 		T.tokenize(in);
 		NFA nfa = NFA.EpsilonNFA();
 		Token id = T.peek();
 		Token start = T.peek(2);
 		if(start.operand == op_code.left_brac) {
 			// Potential CharacterClass definition.
 			clsFactory.build(in);
 			if(T.match(op_code.eoi)) {
 				// Built CharacterClass successfully.
 				return new NFA( clsFactory.getCharClass(id.value) );
 			}
 		}
 		// Failed to build CharacterClass, must be NFA.
 		T.reset();
 		if(op_code.id == id.operand) {
 			nfa.id = id.value;
 			cache.put(nfa.id, nfa);
 			T.match(op_code.id);
			T.resetPast();		// Ignore side-effects of matching id.
								// i.e. potentialEpsilon is false
 		}
 		return nfa.and(regex());
 	}
 	
 	public NFA regex() {
 		NFA nfa = unary_list();
 		while(op_code.or == T.peek().operand) {
 			T.match(op_code.or);
 			nfa.or(unary_list());
 		}
 		return nfa;
 	}
 	
 	private NFA unary_list() {
 		NFA nfa = unary_expr();
 		NFA unary = unary_expr();
 		while( op_code.or  != T.peek().operand &&
 			         unary != null				  ) {
 			nfa.and(unary);
 			unary = unary_expr();
 		}
 		return nfa;
 	}
 	
 	private NFA unary_expr() {
 		NFA nfa = term_expr();
 		switch (T.peek().operand) {
 		case star :
 			T.match(op_code.star);
 			nfa.star();
 			break;
 		case plus :
 			T.match(op_code.plus);
 			nfa.plus();
 			break;
 		default:
 			break;
 		}
 		return nfa;
 	}
 	
 	private NFA term_expr() {
 		NFA nfa = null;
 		switch (T.peek().operand) {
 		case re_char :
 			CharacterClass cls = new CharacterClass("");
 			cls.accept(T.peek().value.charAt(0));
 			nfa = new NFA(cls);
 			T.match(op_code.re_char);
 			break;
 		case left_paren :
 			T.match(op_code.left_paren);
 			nfa = regex();
 			T.match(op_code.right_paren);
 			break;
 		default :
 			nfa = char_class_expr();
 		}
 		return nfa;
 	}
 	
 	private NFA char_class_expr() {
 		NFA nfa = null;
 		switch (T.peek().operand) {
 		case left_brac :
 			nfa = new NFA( clsFactory.charClass() );
 			break;
 		case match_all :
 			CharacterClass cls = new CharacterClass();
 			cls.acceptAll();
 			nfa = new NFA( cls );
 			T.match(op_code.match_all);
 			break;
 		case id :
 			nfa = new NFA( clsFactory.getCharClass(T.peek().value) );
 			T.match(op_code.id);
 			break;
 		case epsilon :
 			T.match(op_code.epsilon);
 			nfa = NFA.EpsilonNFA();
 			break;
 		default :
 			// Return null to signal no more terminals to match
 		}
 		return nfa;
 	}
 }
