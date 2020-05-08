 package bruno.lang.grammar;
 
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.IdentityHashMap;
 import java.util.List;
 import java.util.NoSuchElementException;
 
 
 /**
  * The model of a formal grammar. 
  *  
  * @author jan
  */
 public final class Grammar {
 
 
 	/**
 	 * <pre>
 	 * { min, max }
 	 * </pre>
 	 */
 	public static Occur occur( int min, int max ) {
 		return new Occur(min, max);
 	}
 
 	public static final Occur never = occur(0, 0);
 	
 	
 	/**
 	 * <pre>
 	 * *
 	 * </pre>
 	 */
 	public static final Occur once = occur(1 , 1);
 
 	/**
 	 * <pre>
 	 * *
 	 * </pre>
 	 */
 	public static final Occur star = occur(0 , 1000);
 
 	/**
 	 * <pre>
 	 * +
 	 * </pre>
 	 */
 	public static final Occur plus = occur(1 , 1000);
 
 	/**
 	 * <pre>
 	 * ?
 	 * </pre>
 	 */
 	public static final Occur qmark = occur(0 , 1);
 
 	public static final class Occur {
 
 		public final int min;
 		public final int max;
 
 		Occur( int min, int max ) {
 			super();
 			this.min = min;
 			this.max = max;
 		}
 		
 		@Override
 		public String toString() {
 			return "{"+min+":"+max+"}";
 		}
 
 	}
 	
 	public static final Terminal any = new Any();
 	public static final Terminal whitespace = new Whitespace();
 
 	public static interface Terminal {
 
 		int matching(ByteBuffer input, int position);
 	}
 	
 	private static final byte[] NO_CHARACTER = new byte[0];
 	
 	private final IdentityHashMap<String, Rule> rulesByName;
 	private final Rule[] rulesById;
 	
 	public Grammar(Rule root) {
 		super();
 		this.rulesByName = new IdentityHashMap<>();
 		List<Rule> idOrder = new ArrayList<>();
 		idOrder.add(null); // zero index is unused
 		init(root, idOrder);
 		this.rulesById = idOrder.toArray(new Rule[idOrder.size()]);
 		link(rulesByName.values().toArray(new Rule[0]), new HashSet<String>());
 	}
 	
 	private void init(Rule rule, List<Rule> idOrder) {
 		if (rule.type == RuleType.LINK) {
 			return;
 		}
 		if (!rule.name.isEmpty()) {
 			rulesByName.put(rule.name, rule);
 		}
 		if (rule.id() == 0) {
 			rule.id(idOrder.size());
 			idOrder.add(rule);
 		}
 		for (Rule r : rule.elements) {
 			init(r, idOrder);
 		}
 	}
 
 	private void link(Rule[] rules, java.util.Set<String> followed) {
 		for (int i = 0; i < rules.length; i++) {
 			link(rules[i], followed);
 		}
 	}
 
 	private void link(Rule rule, java.util.Set<String> followed) {
 		Rule[] elements = rule.elements;
 		if (elements.length > 0) { 
 			if (!followed.contains(rule.name)) {
 				if (!rule.name.isEmpty()) {
 					followed = new HashSet<>(followed);
 					followed.add(rule.name);
 				}
 				for (int i = 0; i < elements.length; i++) {
 					Rule e = elements[i];
 					if (e.type == RuleType.LINK) {
 						elements[i] = rule(e.name);
 					} else {
 						link(e, followed);
 					}
 				}
 			} else {
 				for (int i = 0; i < elements.length; i++) {
 					Rule e = elements[i];
 					if (e.type == RuleType.LINK) {
 						elements[i] = rule(e.name);
 					}
 				}
 			}
 		}
 	}
 	
 	public Rule rule(int id) {
 		return rulesById[id];
 	}
 
 	public Rule rule(String name) {
 		Rule r = rulesByName.get(name);
 		if (r != null)
 			return r;
 		throw new NoSuchElementException("Missing rule: "+name);
 	}
 	
 	@Override
 	public String toString() {
 		return rulesByName.toString();
 	}
 
 	public static Terminal not( Terminal excluded ) {
 		return new Not(excluded);
 	}
 
 	public static Terminal not( char s ) {
 		return not(new Is(toByte(s)));
 	}
 
 	public static Terminal or( Terminal s, Terminal... more ) {
 		Terminal or = s;
 		for ( Terminal m : more ) {
 			or = or(or , m);
 		}
 		return or;
 	}
 
 	public static Terminal or( Terminal a, Terminal b ) {
 		return new Or(a, b);
 	}
 
 	public static Terminal set( char low, char high ) {
 		return set(toByte(low) , toByte(high));
 	}
 
 	public static Terminal set( byte low, byte high ) {
 		return new Set(low, high);
 	}
 
 	public static Terminal in( char... cs ) {
 		byte[] bs = new byte[cs.length];
 		for ( int i = 0; i < bs.length; i++ ) {
 			bs[i] = toByte(cs[i]);
 		}
 		return new In(bs);
 	}
 
 	public static byte toByte( char c ) {
 		return String.valueOf(c).getBytes()[0];
 	}
 	
 	public static enum RuleType {
 		LITERAL("lit"), TERMINAL("trm"), ITERATION("itr"), SEQUENCE("seq"), SELECTION("sel"), LINK("lnk"), CAPTURE("cap");
 		
 		public final String code;
 
 		private RuleType(String code) {
 			this.code = code;
 		}
 	}
 
 	public static final class Rule {
 		
 		public static final Rule ANY_WHITESPACE = Rule.terminal(whitespace).occur(star);
 		public static final Rule EMPTY_STRING = Rule.literal('$').occur(Grammar.never);
 		
 		public static Rule link(String name) {
 			return new Rule(RuleType.LINK, name, new Rule[0], ANY_WHITESPACE, once, null, NO_CHARACTER);
 		}
 		
 		public static Rule selection(Rule...elements) {
			return new Rule(RuleType.SELECTION, "", elements, EMPTY_STRING, once, null, NO_CHARACTER);
 		}
 		
 		public static Rule sequence(Rule...elements) {
 			return new Rule(RuleType.SEQUENCE, "", elements, ANY_WHITESPACE, once, null, NO_CHARACTER);
 		}
 		
 		public static Rule token(Rule...elements) {
 			return sequence(elements).separate(EMPTY_STRING);
 		}
 		
 		public static Rule literal( char l ) {
 			return literal(String.valueOf(l));
 		}
 		
 		public static Rule literal(String l) {
 			return new Rule(RuleType.LITERAL, "", new Rule[0], EMPTY_STRING, once, null, l.getBytes());
 		}
 		
 		public static Rule terminal(Terminal...seq) {
 			if (seq.length == 1) {
 				return terminal(seq[0]);
 			}
 			Rule[] sequence = new Rule[seq.length];
 			for (int i = 0; i < sequence.length; i++) {
 				sequence[i] = terminal(seq[i]);
 			}
 			return token(sequence);
 		}
 
 		private static Rule terminal(Terminal terminal) {
 			return new Rule(RuleType.TERMINAL, "", new Rule[0], EMPTY_STRING,  once, terminal, NO_CHARACTER);
 		}
 		
 		public final RuleType type;
 		public final String name;
 		public final Rule[] elements;
 		public final Rule separation;
 		public final Occur occur;
 		public final Terminal terminal;
 		public final byte[] literal;
 		private int id = 0;
 		
 		public Rule(RuleType type, String name, Rule[] elements, Rule separation,
 				Occur occur, Terminal terminal, byte[] literal) {
 			super();
 			this.type = type;
 			this.name = name.intern();
 			this.elements = elements;
 			this.separation = separation;
 			this.occur = occur;
 			this.terminal = terminal;
 			this.literal = literal;
 		}
 		
 		public int id() {
 			return id;
 		}
 		
 		public void id(int id) {
 			if (this.id == 0) {
 				this.id = id;
 			}
 		}
 		
 		public Rule as(String name) {
 			Rule[] elems = type == RuleType.CAPTURE ? elements : new Rule[] { this };
 			return new Rule(RuleType.CAPTURE, name, elems, separation, Grammar.once, null, NO_CHARACTER);
 		}
 		
 		public Rule plus() {
 			return occur(plus);
 		}
 		
 		public Rule occur(Occur occur) {
 			return new Rule(RuleType.ITERATION, "", new Rule[] { this }, separation, occur, null, NO_CHARACTER);
 		}
 		
 		public Rule separate(Rule separation) {
 			return new Rule(type, name, elements, separation, occur, terminal, literal);
 		}
 		
 		@Override
 		public String toString() {
 			if (type == RuleType.TERMINAL) {
 				return terminal.toString();
 			}
 			if (type == RuleType.CAPTURE) {
 				return name;
 			}
 			if (type == RuleType.LITERAL) {
 				return new String(literal);
 			}
 			if (type == RuleType.LINK) {
 				return "@"+name;
 			}
 			return type+" "+Arrays.toString(elements);
 		}
 
 		public Rule star() {
 			return occur(star);
 		}
 
 		public Rule qmark() {
 			return occur(qmark);
 		}
 
 	}
 
 
 	static String print( byte character ) {
 		return "'" + Character.valueOf((char) character) + "'";
 	}
 	
 	public static int length(ByteBuffer input, int position) {
 		byte b = input.get(position);
 		if (b >= 0)
 			return 1;
 		int p = position;
 		while (input.get(++p) < 0) { ; }
 		return p - position;
 		
 	}
 
 	private static final class Whitespace implements Terminal {
 
 		@Override
 		public int matching(ByteBuffer input, int position) {
 			return Character.isWhitespace(input.get(position)) ? 1 : 0;
 		}
 		
 		@Override
 		public String toString() {
 			return "(whitespace)";
 		}
 		
 	}
 	
 	private static final class Any implements Terminal {
 		@Override
 		public int matching(ByteBuffer input, int position) {
 			return length(input, position);
 		}
 
 		@Override
 		public String toString() {
 			return ".";
 		}
 	}
 	
 	private static final class Set
 			implements Terminal {
 
 		final byte low;
 		final byte high;
 
 		Set( byte low, byte high ) {
 			super();
 			this.low = low;
 			this.high = high;
 		}
 		
 		@Override
 		public int matching(ByteBuffer input, int position) {
 			int c = input.get(position);
 			return c >= low && c <= high ? 1 : 0;
 		}
 		
 		@Override
 		public String toString() {
 			return Grammar.print(low) + " - " + Grammar.print(high);
 		}
 	}
 
 	private static final class Not
 			implements Terminal {
 
 		private final Terminal excluded;
 
 		Not( Terminal excluded ) {
 			super();
 			this.excluded = excluded;
 		}
 
 		@Override
 		public int matching(ByteBuffer input, int position) {
 			int l = excluded.matching(input, position);
 			return l > 0 ? 0 : length(input, position);
 		}
 
 		@Override
 		public String toString() {
 			return "!" + excluded;
 		}
 	}
 
 	private static final class In
 			implements Terminal {
 
 		private final byte[] members;
 
 		In( byte[] members ) {
 			super();
 			this.members = members;
 		}
 
 		@Override
 		public int matching(ByteBuffer input, int position) {
 			byte c = input.get(position);
 			for ( int i = 0; i < members.length; i++ ) {
 				if ( members[i] == c ) {
 					return 1;
 				}
 			}
 			return 0;
 		}
 
 		@Override
 		public String toString() {
 			StringBuilder b = new StringBuilder();
 			for ( byte c : members ) {
 				b.append(" | ").append(Grammar.print(c));
 			}
 			return b.substring(3);
 		}
 	}
 
 	private static final class Or
 			implements Terminal {
 
 		private final Terminal a;
 		private final Terminal b;
 
 		Or( Terminal a, Terminal b ) {
 			super();
 			this.a = a;
 			this.b = b;
 		}
 		
 		@Override
 		public int matching(ByteBuffer input, int position) {
 			return Math.max(a.matching(input, position), b.matching(input, position));
 		}
 
 		@Override
 		public String toString() {
 			return a + " | " + b;
 		}
 
 	}
 	
 	private static final class Are implements Terminal {
 
 		private final byte[] string;
 		
 		Are(byte[] string) {
 			super();
 			this.string = string;
 		}
 
 		@Override
 		public int matching(ByteBuffer input, int position) {
 			for (int i = 0; i < string.length; i++) {
 				if (string[i] != input.get(position+i)) {
 					return 0;
 				}
 			}
 			return string.length;
 		}
 		
 	}
 
 	private static final class Is
 			implements Terminal {
 
 		private final byte s;
 
 		Is( byte s ) {
 			super();
 			this.s = s;
 		}
 
 		@Override
 		public int matching(ByteBuffer input, int position) {
 			return input.get(position) == s ? 1 : 0;
 		}
 		
 		@Override
 		public String toString() {
 			return Grammar.print(s);
 		}
 	}
 
 }
