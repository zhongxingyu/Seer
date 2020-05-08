 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.logging.Logger;
 
 import org.antlr.runtime.ANTLRStringStream;
 import org.antlr.runtime.TokenRewriteStream;
 import org.antlr.runtime.tree.CommonTree;
 import org.antlr.runtime.tree.Tree;
 
 public class Interpreter {
 	/**
 	 * Implements the {@link Iterable} pattern for {@link Tree}.
 	 * 
 	 * @note This class would normally be in a completely separate file, but we
 	 *       can only turn in specific files for the project.
 	 * 
 	 * @author riggleza
 	 */
 	class IterableTree implements Iterable<Tree> {
 
 		/**
 		 * Implements the {@link Iterator} pattern for {@link Tree}.
 		 * 
 		 * @author riggleza
 		 */
 		class TreeIterator implements Iterator<Tree> {
 			Integer current = -1;
 			Tree t;
 
 			public TreeIterator(Tree t) {
 				this.t = t;
 			}
 
 			@Override
 			public boolean hasNext() {
 				return (current + 1) < t.getChildCount();
 			}
 
 			@Override
 			public Tree next() {
 				return t.getChild(++current);
 			}
 
 			@Override
 			public void remove() {
 				t.deleteChild(current--);
 			}
 		}
 
 		Tree t;
 
 		public IterableTree(Tree t) {
 			this.t = t;
 		}
 
 		@Override
 		public Iterator<Tree> iterator() {
 			return new TreeIterator(t);
 		}
 
 	}
 
 	public static final Boolean FALSE = new Boolean(false);
 
 	// -- Boolean truths
 	public static final Integer ZERO = new Integer(0);
 	Logger log = Logger.getLogger("Interpreter");
 
 	HashMap<String, Object> memory = new HashMap<String, Object>();
 
 	/**
 	 * Instantiate the interpreter based on an InputStream.
 	 * 
 	 * @param in
 	 * @throws Exception
 	 */
 	public Interpreter(ANTLRStringStream in) throws Exception {
 		// -- Parse the inpiut
 		LogoTurtleLexer lexer = new LogoTurtleLexer(in);
 		TokenRewriteStream tokens = new TokenRewriteStream(lexer);
 		LogoTurtleParser parser = new LogoTurtleParser(tokens);
 
 		// -- 'program' is the top-level rule
 		LogoTurtleParser.program_return r = parser.program();
 
 		// -- Check for errors
 		if (parser.getNumberOfSyntaxErrors() != 0)
 			throw new Exception("There were "
 					+ parser.getNumberOfSyntaxErrors() + " syntax errors.");
 
 		// -- Execute the tree
 		exec((CommonTree) r.getTree());
 	}
 
 	public Interpreter(CommonTree t) {
 		exec(t);
 	}
 
 	Object add(Tree t) {
 		log.info("Adding" + t.toStringTree());
 		int x = (Integer) exec(t.getChild(0));
 		int y = (Integer) exec(t.getChild(1));
 		int z = x + y;
 		return z;
 	}
 
 	Object and(Tree t) {
 		log.info("and'ing " + t.toStringTree());
 		Boolean x = (Boolean) exec(t.getChild(0));
 		Boolean y = (Boolean) exec(t.getChild(1));
 		return x && y;
 	}
 
 	Object block(Tree t) {
 		log.info("Executing block" + t.toStringTree());
 
 		for (Tree child : new IterableTree(t))
 			exec(child);
 
 		return null;
 	}
 
 	Object div(Tree t) {
 		log.info("dividing " + t.toStringTree());
 		int x = (Integer) exec(t.getChild(0));
 		int y = (Integer) exec(t.getChild(1));
 		int z = x / y;
 		return z;
 	}
 
 	Object equality(Tree t) {
 		log.info("and'ing " + t.toStringTree());
 
 		return exec(t.getChild(0)).equals(exec(t.getChild(1)));
 	}
 
 	Object exec(Tree t) {
 		switch (t.getType()) {
 		case 0: // Nil, root of the tree, fall through to 'block'
 		case LogoTurtleParser.BLOCK:
 			return block(t);
 
 		case LogoTurtleParser.AND:
 			return and(t); // &&
 		case LogoTurtleParser.BYNAME:
 			return name(t);
 		case LogoTurtleParser.BYVAL:
 			return val(t);
 		case LogoTurtleParser.DIV:
 			return div(t); // /
 		case LogoTurtleParser.EQ:
 			return equality(t); // ==
 		case LogoTurtleParser.GT:
 			return greaterThan(t); // >
 		case LogoTurtleParser.GTE:
 			return greaterThanEquals(t); // >=
 		case LogoTurtleParser.ID:
 			return id(t);
 		case LogoTurtleParser.IF:
			return if_(t);
 		case LogoTurtleParser.IFELSE:
 			return ifelse(t);
 		case LogoTurtleParser.LT:
 			return lessThan(t); // <
 		case LogoTurtleParser.LTE:
 			return lessThanEquals(t); // <=
 		case LogoTurtleParser.MAKE:
 			return make(t);
 		case LogoTurtleParser.MINUS:
 			return minus(t); // -
 		case LogoTurtleParser.MODULO:
 			return modulo(t); // %
 		case LogoTurtleParser.MULT:
 			return mult(t); // *
 		case LogoTurtleParser.NOT:
 			return negate(t); // !
 		case LogoTurtleParser.NUMBER:
 			return Integer.parseInt(t.getText());
 		case LogoTurtleParser.OR:
 			return or(t); // ||
 		case LogoTurtleParser.PLUS:
 			return add(t); // +
 		case LogoTurtleParser.PRINT:
 			return print(t);
 		case LogoTurtleParser.WHILE:
 			return while_(t);
 		default:
 			unhandledTypeError(t);
 		}
 
 		return null;
 	}
 
 	Object greaterThan(Tree t) {
 		Object a = exec(t.getChild(0));
 		Object b = exec(t.getChild(1));
 		if (a instanceof Number && b instanceof Number) {
 
 			Number x = (Number) a;
 			Number y = (Number) b;
 
 			return x.floatValue() > y.floatValue();
 
 		}
 		return false;
 	}
 
 	Object greaterThanEquals(Tree t) {
 		log.info("evaluating " + t.toStringTree());
 		int x = (Integer) exec(t.getChild(0));
 		int y = (Integer) exec(t.getChild(1));
 		return x >= y;
 	}
 
 	String id(Tree t) {
 		return t.getText();
 	}
 
 	Object if_(Tree t) {
 		Object result = exec(t.getChild(0));
 
 		if (FALSE.equals(result) || ZERO.equals(result))
 			return Boolean.FALSE;
 
 		else
 			exec(t.getChild(1));
 
 		return Boolean.TRUE;
 	}
 
 	Object ifelse(Tree t) {
 		Tree condition = t.getChild(0);
 		Tree iftrue = t.getChild(1);
 		Tree iffalse = t.getChild(2);
 
 		Object result = exec(condition);
 
 		// Note that false what we test for
 		if (FALSE.equals(result) || ZERO.equals(result)) {
 			exec(iffalse);
 			return Boolean.FALSE;
 		} else {
 			exec(iftrue);
 			return Boolean.TRUE;
 		}
 	}
 
 	Object lessThan(Tree t) {
 		log.info("evaluating " + t.toStringTree());
 		int x = (Integer) exec(t.getChild(0));
 		int y = (Integer) exec(t.getChild(1));
 		return x < y;
 	}
 
 	Object lessThanEquals(Tree t) {
 		log.info("evaluating " + t.toStringTree());
 		int x = (Integer) exec(t.getChild(0));
 		int y = (Integer) exec(t.getChild(1));
 		return x <= y;
 	}
 
 	Object make(Tree t) {
 		String key = exec(t.getChild(0)).toString();
 		Object value = exec(t.getChild(1));
 
 		log.info("Setting " + key + " = " + value);
 
 		memory.put(key, value);
 
 		return value;
 	}
 
 	Object minus(Tree t) {
 		log.info("subtracting " + t.toStringTree());
 		int x = (Integer) exec(t.getChild(0));
 		int y = (Integer) exec(t.getChild(1));
 		int z = x - y;
 		return z;
 	}
 
 	Object modulo(Tree t) {
 		log.info("Modulo'ing " + t.toStringTree());
 		int x = (Integer) exec(t.getChild(0));
 		int y = (Integer) exec(t.getChild(1));
 		int z = x % y;
 
 		return z;
 	}
 
 	Object mult(Tree t) {
 		log.info("Multiplying " + t.toStringTree());
 		int x = (Integer) exec(t.getChild(0));
 		int y = (Integer) exec(t.getChild(1));
 		int z = x * y;
 		return z;
 	}
 
 	Object name(Tree t) {
 		String variableName = (String) exec(t.getChild(0));
 		log.info("Encountered variable or string " + variableName);
 		return variableName;
 	}
 
 	Object negate(Tree t) {
 		Boolean x = (Boolean) exec(t.getChild(0));
 		return !x;
 	}
 
 	Object or(Tree t) {
 		log.info("or'ing " + t.toStringTree());
 		Boolean x = (Boolean) exec(t.getChild(0));
 		Boolean y = (Boolean) exec(t.getChild(1));
 		return x || y;
 	}
 
 	Object print(Tree t) {
 		log.info("Printing " + t.toStringTree());
 
 		// (print "x "y "z) will print multiple items, so there may be
 		// many children. Only print a space if there are.
 		String space = t.getChildCount() > 1 ? " " : "";
 
 		for (Tree subtree : new IterableTree(t)) {
 			Object printValue = exec(subtree);
 			log.info("Printing [" + printValue + "]");
 			System.out.print(printValue + space);
 		}
 
 		System.out.println();
 
 		return null;
 	}
 
 	void unhandledTypeError(Tree t) {
 		log.severe("Encountered unhandled type " + t.getType() + " ("
 				+ LogoTurtleParser.tokenNames[t.getType()] + ")" + " at \""
 				+ t.getText() + "\"" + " on line " + t.getLine() + ":"
 				+ t.getCharPositionInLine());
 		System.exit(1);
 	}
 
 	Object val(Tree t) {
 		String variableName = (String) exec(t.getChild(0));
 		log.info("Fetching value of " + variableName);
 		return memory.get(variableName);
 	}
 
 	Object while_(Tree t) {
 		Tree condition = t.getChild(0);
 		Tree block = t.getChild(1);
 
 		log.info("Executing while(" + condition.toStringTree() + "){"
 				+ block.toStringTree() + "}");
 
 		while (true) {
 			Object result = exec(condition);
 
 			if (ZERO.equals(result)) {
 				log.info(condition.toStringTree()
 						+ " evaluates to ZERO.  Exiting loop "
 						+ t.toStringTree());
 				break;
 			} else if (FALSE.equals(result)) {
 				log.info(condition.toStringTree()
 						+ " evaluates to FALSE.  Exiting loop "
 						+ t.toStringTree());
 				break;
 			} else {
 				exec(block);
 			}
 		}
 
 		return null;
 	}
 
 }
