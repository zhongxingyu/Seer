 import java.util.HashMap;
 
 
 public class Regex {
 	public interface Node {
 		public <T> T accept(Visitor<T> visitor);
 	}
 	public interface Visitor<T> {
 		T visit(EmptySet node);
 		T visit(EmptyString node);
 		T visit(Symbol node);
 		T visit(Star node);
 		T visit(Sequence node);
 		T visit(Or node);
 	}
 	// Item 1:
 	public static class Printer implements Visitor<String> {
 
 		@Override
 		public String visit(EmptySet node) {
 			return "∅";
 		}
 
 		@Override
 		public String visit(EmptyString node) {
 			return "ε";
 		}
 
 		@Override
 		public String visit(Symbol node) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public String visit(Star node) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public String visit(Sequence node) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public String visit(Or node) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 	}
 	// Reject everything
 	// FIXME: Singleton: only one instance of this class
 	public static class EmptySet implements Node {
 		private static EmptySet emptySet = new EmptySet();
 		private EmptySet() {}
 		public static EmptySet getInstance() {
 			return emptySet;
 		}
 		@Override
 		public <T> T accept(Visitor<T> visitor) {
 			return visitor.visit(this);
 		}
 	}
 	// Matches "" Accept the end of a string
 	// FIXME: Singleton
 	public static class EmptyString implements Node {
 		private EmptyString() {}
 		@Override
 		public <T> T accept(Visitor<T> visitor) {
 			return visitor.visit(this);
 		}
 	}
 	// Match a single symbol
 	// FIXME: Flyweight
 	public static class Symbol implements Node {
 		char symbol;
 		private static HashMap<Character, Symbol> map = new HashMap<Character, Symbol>();
		// It's private, as in, do not use outside this class
 		private Symbol (char symbol) {
 			this.symbol = symbol;
 		}
		// How we actually "construct" a symbol
 		public static Symbol getInstance(char symbol) {
 			if (!map.containsKey(symbol)) {
 				map.put(symbol, new Symbol(symbol));
 			}
 			return map.get(symbol);
 		}
 		@Override
 		public <T> T accept(Visitor<T> visitor) {
 			return visitor.visit(this);
 		}
 	}
 	// Match (child)*
 	// FIXME: Flyweight
 	// FIXME: Compaction
 	public static class Star implements Node {
 		Node child;
		// Make the constructor private and have a hashmap here too
		private Star(Node child) {
 			this.child = child;
 		}
 		// getInstance will return a Node but possibly not a Star
 		// if the child is an emptyString, return emptyString
		public static Node getInstance(Node child) {
			return null;
		}
 		@Override
 		public <T> T accept(Visitor<T> visitor) {
 			return visitor.visit(this);
 		}
 	}
 	// Match a followed by b
 	// FIXME: Flyweight
	// FIXME: Compaction
 	public static class Sequence implements Node {
 		Node a, b;
 		public Sequence(Node a, Node b) {
 			this.a = a; this.b = b;
 		}
 		@Override
 		public <T> T accept(Visitor<T> visitor) {
 			return visitor.visit(this);
 		}
 	}
 	// Match a or b
 	// FIXME: Flyweight
	// FIXME: Compaction
 	public static class Or implements Node {
 		Node a, b;
 		public Or(Node a, Node b) {
 			this.a = a; this.b = b;
 		}
 		@Override
 		public <T> T accept(Visitor<T> visitor) {
 			return visitor.visit(this);
 		}
 	}
 	// Rewrite the regex to match
 	// the rest of a string without the first char c.
 	public static class Derivative implements Visitor<Node> {
 		Nullable nullable = new Nullable();
 		public char c; // Derive with respect to c
 		@Override
 		public Node visit(EmptySet node) {
 			// Dc(0) = 0
 			return node;
 		}
 		@Override
 		public Node visit(EmptyString node) {
 			// Dc("") = 0
 			return EmptySet.getInstance();
 		}
 		@Override
 		public Node visit(Symbol node) {
 			// Dc(c) = ""
 			if (c == node.symbol)
 				return new EmptyString(); // Do the same thing for the empty string
 			// Dc(c') = 0 if c is not c'
 			else
 				return EmptySet.getInstance();
 		}
 		@Override
 		public Node visit(Star node) {
 			// Dc(a*) = Dc(a)a*
 			return new Sequence(node.child.accept(this), node);
 		}
 
 		@Override
 		public Node visit(Sequence node) {
 			Node result = new Sequence(node.a.accept(this), node.b);
 			// Dc(AB) = Dc(A)B if A does not contain the empty string
 			if (!node.a.accept(nullable)) {
 				return result;
 			// Dc(AB) = Dc(A)B | Dc(B) if A contains the empty string
 			} else {
 				return new Or(
 						result, // Dc(AB)
 						node.b.accept(this) // Dc(B)
 						);
 			}
 		}
 		@Override
 		public Node visit(Or node) {
 			// Dc(A | B) = Dc(A) | Dc(B)
 			return new Or(node.a.accept(this), node.b.accept(this));
 		}
 	}
 	// Does the regex match the empty string?
 	public static class Nullable implements Visitor<Boolean> {
 		@Override
 		public Boolean visit(EmptySet node) {
 			return false;
 		}
 		@Override
 		public Boolean visit(EmptyString node) {
 			return true;
 		}
 		@Override
 		public Boolean visit(Symbol node) {
 			return false;
 		}
 		@Override
 		public Boolean visit(Star node) {
 			return true;
 		}
 		@Override
 		public Boolean visit(Sequence node) {
 			return node.a.accept(this) && node.b.accept(this);
 		}
 		@Override
 		public Boolean visit(Or node) {
 			return node.a.accept(this) || node.b.accept(this);
 		}
 	}
 	// Use derivatives to match regular expressions
 	public static boolean match(Node regex, String string) {
 		// Two visitors
 		Derivative d = new Derivative();
 		Nullable nullable = new Nullable();
 		// For debugging, create the printer here
 		Printer printer = new Printer();
 
 		// Just compute the derivative with respect to the first character, then the second, then the third and so on.
 		for (char c : string.toCharArray()) {
 			d.c = c; // Set the first character
			// For debugging purposes,
 			// Print out the new regex
 			System.out.println(regex.accept(printer));
			regex = regex.accept(d); // regex should match what it used to match, sans first character c
 		}
 		// If the final language contains the empty string, then the original string was in the original language.
 		// Does the regex match the empty string?
 		return regex.accept(nullable);
 	}
 	public static void main(String[] args) {
 		String s = "H";
 		s += "ello";
		if("Hello" == (s)) {
 			System.out.println("WTF");
 		}
 		// Does a|b match a?
 		long then = System.nanoTime();
 		for (int i = 0; i < 1; i++)
 			Regex.match(
 				new Sequence(new Symbol('b'), new Sequence(new Symbol('o'), new Symbol('b'))), "a");
 		System.out.println(System.nanoTime() - then);
 	}
 }
