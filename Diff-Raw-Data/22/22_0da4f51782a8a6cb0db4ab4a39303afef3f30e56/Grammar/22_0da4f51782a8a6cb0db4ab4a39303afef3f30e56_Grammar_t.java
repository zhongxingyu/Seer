 package bruno.lang.grammar;
 
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.IdentityHashMap;
 import java.util.List;
 import java.util.Map.Entry;
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
 			if (min == star.min && max == star.max) {
 				return "*";
 			}
 			if (min == plus.min && max == plus.max) {
 				return "+";
 			}
 			if (min == qmark.min && max == qmark.max) {
 				return "?";
 			}
 			return "{"+min+":"+max+"}";
 		}
 
 	}
 	
 	public static final Terminal any = new Any();
 	public static final Terminal whitespace = new Whitespace();
 
 	public static interface Terminal {
 
 		int length(ByteBuffer input, int position);
 	}
 	
 	private static final byte[] NO_CHARACTER = new byte[0];
 	
 	private final IdentityHashMap<String, Rule> rulesByName;
 	private final Rule[] rulesById;
 	
 	public Grammar(Rule... roots) { //OPEN maybe we need other starting points because of links
 		super();
 		this.rulesByName = new IdentityHashMap<>();
 		List<Rule> idOrder = new ArrayList<>();
 		idOrder.add(null); // zero index is unused
 		for (Rule root : roots) {
 			init(root, idOrder);
 		}
 		this.rulesById = idOrder.toArray(new Rule[idOrder.size()]);
 		link(rulesByName.values().toArray(new Rule[0]), new HashSet<String>());
 	}
 	
 	private void init(Rule rule, List<Rule> idOrder) {
 		if (rule.type == RuleType.LINK) {
 			return;
 		}
 		if (!rule.name.isEmpty()) {
			rulesByName.put(rule.name.substring(rule.name.charAt(0) == '-' ? 1 : 0), rule);
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
		//FIXME below: when linked rule uses -minus to not capture the capture has to be unboxed
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
		boolean noCapture = name.charAt(0) == '-';
		Rule r = rulesByName.get(name.substring(noCapture ? 1 : 0).intern());
		if (r != null) {
			return noCapture && r.type == RuleType.CAPTURE ? r.elements[0].as(name) : r;
 		}
 		throw new NoSuchElementException("Missing rule: "+name);
 	}
 	
 	@Override
 	public String toString() {
 		StringBuilder b = new StringBuilder();
 		for (Entry<String,Rule> e : rulesByName.entrySet()) {
 			Rule r = e.getValue();
 			int l = 15;
 			if (r.separation != Rule.ANY_WHITESPACE) {
 				b.append('[');
 				b.append(r.separation.name);
 				b.append(']');
 				b.append(' ');
 				l -= 3 + r.separation.name.length();
 			}
 			b.append(String.format("%-"+l+"s: ", e.getKey()));
 			for (Rule elem : r.elements) {
 				b.append(elem);
 				b.append(' ');
 			}
 			b.append('\n');
 		}
 		return b.toString();
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
 		LITERAL("lit"), TERMINAL("trm"), ITERATION("itr"), SEQUENCE("seq"), SELECTION("sel"), COMPLETION("cmp"), LINK("lnk"), CAPTURE("cap");
 		
 		public final String code;
 
 		private RuleType(String code) {
 			this.code = code;
 		}
 		
 		public boolean bytes() {
 			return this == LITERAL || this == TERMINAL;
 		}
 	}
 
 	public static final class Rule {
 		
 		public static final Rule ANY_WHITESPACE = terminal(whitespace).occurs(star);
 		public static final Rule EMPTY_STRING = literal('$').occurs(never);
 		
 		public static Rule completion(Rule to) {
 			return new Rule(RuleType.COMPLETION, "", new Rule[] { to }, EMPTY_STRING, once, null, NO_CHARACTER);
 		}
 		
 		public static Rule link(String name) {
 			return new Rule(RuleType.LINK, name, new Rule[0], ANY_WHITESPACE, once, null, NO_CHARACTER);
 		}
 		
 		public static Rule selection(Rule...elements) {
 			return new Rule(RuleType.SELECTION, "", elements, EMPTY_STRING, once, null, NO_CHARACTER);
 		}
 		
 		public static Rule sequence(Rule...elements) {
 			for (int i = 0; i < elements.length; i++) {
 				if (elements[i].type == RuleType.COMPLETION && elements[i].elements[0] == null) {
 					elements[i].elements[0] = elements[i+1];
 				}
 			}
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
 			if (name.length() > 0 && name.charAt(0) == '-') {
 				return new Rule(type, name, elements, separation, occur, terminal, literal);
 			}
 			Rule[] elems = type == RuleType.CAPTURE ? elements : new Rule[] { this };
 			return new Rule(RuleType.CAPTURE, name, elems, separation, Grammar.once, null, NO_CHARACTER);
 		}
 		
 		public Rule plus() {
 			return occurs(plus);
 		}
 		
 		public Rule occurs(Occur occur) {
 			if (occur == once)
 				return this;
 			return new Rule(RuleType.ITERATION, "", new Rule[] { this }, separation, occur, null, NO_CHARACTER);
 		}
 		
 		public Rule separate(Rule separation) {
 			return new Rule(type, name, elements, separation, occur, terminal, literal);
 		}
 		
 		@Override
 		public String toString() {
 			if (type == RuleType.CAPTURE) {
 				return name;
 			}
			if (type == RuleType.TERMINAL) {
				return terminal.toString();
			}
 			if (type == RuleType.LITERAL) {
 				return "'"+ new String(literal)+"'";
 			}
 			if (type == RuleType.LINK) {
 				return "@"+name;
 			}
 			if (type == RuleType.SELECTION) {
 				StringBuilder b = new StringBuilder();
 				for (Rule e : elements) {
 					b.append(" | ").append(e);
 				}
 				return "("+b.substring(3)+")";
 			}
 			if (type == RuleType.SEQUENCE) {
 				StringBuilder b = new StringBuilder();
 				for (Rule e : elements) {
 					b.append(" ").append(e);
 				}
 				return "("+b.substring(1)+")";
 			}
 			if (type == RuleType.ITERATION) {
 				String iter= occur.toString();
 				return elements[0]+iter;
 			}
 			return type+" "+Arrays.toString(elements);
 		}
 
 		public Rule star() {
 			return occurs(star);
 		}
 
 		public Rule qmark() {
 			return occurs(qmark);
 		}
 
 	}
 
 
 	static String print( byte character ) {
 		return "'" + Character.valueOf((char) character) + "'";
 	}
 	
 	public static int utf8length(ByteBuffer input, int position) {
 		byte b = input.get(position);
 		if (b >= 0)
 			return 1;
 		int p = position;
 		while (input.get(++p) < 0) { ; }
 		return p - position;
 		
 	}
 
 	private static final class Whitespace implements Terminal {
 
 		@Override
 		public int length(ByteBuffer input, int position) {
 			return Character.isWhitespace(input.get(position)) ? 1 : 0;
 		}
 		
 		@Override
 		public String toString() {
 			return "(whitespace)";
 		}
 		
 	}
 	
 	private static final class Any implements Terminal {
 		@Override
 		public int length(ByteBuffer input, int position) {
 			return utf8length(input, position);
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
 		public int length(ByteBuffer input, int position) {
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
 		public int length(ByteBuffer input, int position) {
 			int l = excluded.length(input, position);
 			return l > 0 ? 0 : utf8length(input, position);
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
 		public int length(ByteBuffer input, int position) {
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
 		public int length(ByteBuffer input, int position) {
 			return Math.max(a.length(input, position), b.length(input, position));
 		}
 
 		@Override
 		public String toString() {
 			return a + " | " + b;
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
 		public int length(ByteBuffer input, int position) {
 			return input.get(position) == s ? 1 : 0;
 		}
 		
 		@Override
 		public String toString() {
 			return Grammar.print(s);
 		}
 	}
 
 }
