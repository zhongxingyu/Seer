 package runtime.interpreter;
 
 import java.util.List;
 import java.io.FileReader;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Stack;
 
 import org.antlr.runtime.tree.CommonTree;
 
 import runtime.memoization.Memoization;
 import runtime.memoization.Result;
 import semantics.Attribute;
 import semantics.Function;
 import semantics.Grammar;
 import semantics.NonTerminal;
 import semantics.SemanticNode;
 import srcparser.AdaptablePEGLexer;
 
 public class Interpreter {
 	
 	private Grammar grammar;
 
 	private FileReader input;
 	private ArrayList<Character> buf;
 	Stack<Environment> environments;	
 	private static final char EOF = (char) -1;
 
 	private Memoization memoization;
 	
 	public Interpreter(Grammar grammar) {
 		this.grammar = grammar;
 		buf = new ArrayList<Character>();
 		environments = new Stack<Environment>();
 		
 		memoization = new Memoization();
 	}
 	
 
 	public void setInputFile(String fileName) throws Exception {
 		input = new FileReader(fileName);
 	}
 
 	private char read(int pos) throws Exception {
 		if (pos < 0) throw new Exception("Index invalid for buffer");
 		if (pos < buf.size()) {
 			return buf.get(pos);
 		}
 		char ch = '?';
 		// I know it will execute at least one iteration of the next loop,
 		// but the compiler does not know it
 		/*for (int i = buf.size(); i <= pos; ++i) {
 			ch = (char) input.read();
 			buf.add(ch);
 		}*/
 		// So, I have changed for this code :)
 		int i = buf.size();
 		do {
 			ch = (char) input.read();
 			buf.add(ch);
 			++i;
 		} while(i <= pos);
 		
 		return ch;
 	}
 	
 	private int match(String s, int pos) throws Exception {
 		for (int i = 0; i < s.length(); ++i) {
 			char ch = read(pos + i);
 			char ch2 = s.charAt(i);
 			if (ch != ch2) {
 				return -1;
 			}
 		}
 		return pos + s.length();
 	}
 
 	public int execute(String nontermName) throws Exception {
 		NonTerminal nt = grammar.getNonTerminal(nontermName);
 		if (nt == null) {
 			throw new Exception("Nonterminal not found: " + nontermName);
 		}
 		Environment env = buildEnvironment(nt);
 		environments.push(env);
 		return process(nt.getPegExpr(), 0);
 	}
 	
 	public Environment buildEnvironment(NonTerminal nt) {
 		Environment env = new Environment(nt.numAttrs());
 		return env;
 	}
 	
 	public Environment currEnvironment() {
 		return environments.peek();
 	}
 
 
 	
 	/**
 	 * Process an expression, reading the first character at position pos of the input file.
 	 * The first call must be with pos=0.
 	 * @param tree
 	 * @param pos
 	 * @return
 	 * @throws Exception
 	 */
 	private int process(CommonTree tree, int pos) throws Exception {
 		switch (tree.token.getType()) {
 		
 		case AdaptablePEGLexer.NONTERM: {
 			// I suppose there are 2 children
 			NonTerminal nt = (NonTerminal) ((SemanticNode) tree.getChild(0)).getSymbol(); // name of nonterminal
 			CommonTree t = (CommonTree) tree.getChild(1); // list of arguments
 				
 			/**
 			 * Code for memoization
 			 * creating a list with the values of the attributes
 			 */
 			List<Object> attr = new ArrayList<Object>();
 			
 			Environment env = buildEnvironment(nt);		
 			for (int i = 0; i < nt.getNumParam(); ++i) {
 				Object x = eval((CommonTree)t.getChild(i));
 				env.setValue(i, x);
 				/**
 				 * Insert on the List
 				 */
 				attr.add(x);
 			}			
 			/**
 			 * code for memoization
 			 */
 			Result result = memoization.getMemoization(nt.getName(), attr, pos);
			if(result != null)
 				return result.getNext_pos();
 			// else
 			
 			environments.push(env);
 			int ret = process(nt.getPegExpr(), pos);
 			environments.pop();
 			/**
 			 * reset the List attr and put the returns values
 			 */
 			List<Object> result_attr = new ArrayList<Object>();
 			if (ret >= 0) {
 				int first = nt.getNumParam();
 				int last = first + nt.getNumRet();
				// This is executed at least one time
				// Can be changed to do statment
 				for (int i = first; i < last; ++i) {
 					Object x = env.getValue(i);					
 					SemanticNode y = (SemanticNode) t.getChild(i);
 					currEnvironment().setValue(((Attribute) y.getSymbol()).getIndex(), x);
 					/**
 					 * adding Object x on the List
 					 */
 					result_attr.add(x);
 				}
 			}
 			/**
 			 * memoizationing the value
 			 */
 			memoization.addMemoization(nt.getName(), attr, pos, ret, result_attr);
 			return ret;
 		}
 		
 		case AdaptablePEGLexer.LAMBDA: {
 			return pos;
 		}
 		
 		case AdaptablePEGLexer.ANY: {
 			char ch = read(pos);
 			if (ch != EOF) {
 				return pos + 1;
 			}
 			return -1;
 		}
 		
 		case AdaptablePEGLexer.STRING_LITERAL: {
 			String s = tree.token.getText();
 			return match(s.substring(1, s.length()-1), pos);
 		}
 		
 		case AdaptablePEGLexer.RANGE: {
 			char ch = read(pos);
 			for (int i = 0; i < tree.getChildCount(); ++i) {
 				CommonTree t = (CommonTree) tree.getChild(i);
 				char ch1 = t.token.getText().charAt(0);
 				char ch2 = t.token.getText().charAt(2);
 				if (ch >= ch1 && ch <= ch2) {
 					return pos + 1;
 				}
 			}
 			return -1;
 		}
 		
 		case AdaptablePEGLexer.SEQ: {
 			int ret = pos;
 			for (int i = 0; i < tree.getChildCount(); ++i) {
 				CommonTree t = (CommonTree) tree.getChild(i);
 				ret = process(t, ret);
 				if (ret < 0) {
 					return -1;
 				}
 			}
 			return ret;
 		}
 		
 		case AdaptablePEGLexer.REPEAT: {
 			// I suppose there is exactly one child
 			CommonTree t = (CommonTree) tree.getChild(0);
 			int ret = pos;
 			int ret1 = ret;
 			while (true) {
 				ret1 = process(t, ret1);
 				if (ret1 < 0) {
 					return ret;
 				}
 				ret = ret1;
 			}
 		}
 		
 		case AdaptablePEGLexer.COND: {
 			// I suppose there is exactly one child
 			CommonTree t = (CommonTree) tree.getChild(0);
 			Boolean b = (Boolean) eval(t);
 			if (b) {
 				return pos;
 			} else {
 				return -1;
 			}
 		}
 
 		case AdaptablePEGLexer.CHOICE: {
 			// I suppose there are 2 children
 			for (int i = 0; i < tree.getChildCount(); ++i) {
 				CommonTree t = (CommonTree) tree.getChild(i);
 				int ret = process(t, pos);
 				if (ret >= 0) {
 					return ret;
 				}
 			}
 			return -1;
 		}
 			
 		case AdaptablePEGLexer.ASSIGNLIST: {
 			// I suppose all childrem are ASSIGN
 			for (int i = 0; i < tree.getChildCount(); ++i) {
 				CommonTree t = (CommonTree) tree.getChild(i);
 				process(t, pos);
 			}
 			return pos;
 		}
 			
 		case AdaptablePEGLexer.ASSIGN: {
 			// I suppose (until now) first child is only ID
 			SemanticNode left = (SemanticNode) tree.getChild(0);
 			CommonTree right = (CommonTree) tree.getChild(1);
 			Object r = eval(right);
 			currEnvironment().setValue((Attribute) left.getSymbol(), r);
 			return pos;
 		}
 		
 		case AdaptablePEGLexer.AND_LOOKAHEAD: {
 			// I suppose there is exactly 1 child
 			CommonTree t = (CommonTree) tree.getChild(0);
 			int ret = process(t, pos);
 			if (ret < 0) {
 				return -1;
 			}
 			return pos;
 		}
 		
 		case AdaptablePEGLexer.NOT_LOOKAHEAD: {
 			// I suppose there is exactly 1 child
 			CommonTree t = (CommonTree) tree.getChild(0);
 			int ret = process(t, pos);
 			if (ret < 0) {
 				return pos;
 			}
 			return -1;
 		}
 			
 		default:
 			throw new Exception("Not implemented: " + tree.token.getType());
 		}
 	}
 	
 	
 	private Object eval(CommonTree tree) throws Exception {
 		switch (tree.token.getType()) {
 		
 		case AdaptablePEGLexer.INT_NUMBER: {
 			return new Integer(Integer.parseInt(tree.getText()));
 		}
 
 		case AdaptablePEGLexer.CALL: {
 			//I suppose there are 2 children, the name of the function and the list of arguments
 			SemanticNode nameNode = (SemanticNode) tree.getChild(0);
 			Method m = ((Function) nameNode.getSymbol()).getMethod();
 			CommonTree t1 = (CommonTree) tree.getChild(1);
 			int nArgs = t1.getChildCount();
 			// I already checked the number of parameters during semantic analysis
 			Object args[] = new Object[nArgs];
 			for (int i = 0; i < nArgs; ++i) {
 				CommonTree t2 = (CommonTree) t1.getChild(i);
 				args[i] = eval(t2);
 			}
 			return m.invoke(null, args);
 		}
 
 		case AdaptablePEGLexer.OP_ADD: {
 			//I suppose there are 2 children
 			int i0 = (Integer) eval((CommonTree) tree.getChild(0));
 			int i1 = (Integer) eval((CommonTree) tree.getChild(1));
 			return new Integer(i0 + i1);
 		}
 
 		case AdaptablePEGLexer.OP_SUB: {
 			//I suppose there are 2 children
 			int i0 = (Integer) eval((CommonTree) tree.getChild(0));
 			int i1 = (Integer) eval((CommonTree) tree.getChild(1));
 			return new Integer(i0 - i1);
 		}
 
 		case AdaptablePEGLexer.OP_MUL: {
 			//I suppose there are 2 children
 			int i0 = (Integer) eval((CommonTree) tree.getChild(0));
 			int i1 = (Integer) eval((CommonTree) tree.getChild(1));
 			return new Integer(i0 * i1);
 		}
 		
 		case AdaptablePEGLexer.OP_GT: {
 			//I suppose there are 2 children
 			int i0 = (Integer) eval((CommonTree) tree.getChild(0));
 			int i1 = (Integer) eval((CommonTree) tree.getChild(1));
 			return new Boolean(i0 > i1);
 		}
 		
 		case AdaptablePEGLexer.OP_EQ: {
 			//I suppose there are 2 children
 			int i0 = (Integer) eval((CommonTree) tree.getChild(0));
 			int i1 = (Integer) eval((CommonTree) tree.getChild(1));
 			return new Boolean(i0 == i1);
 		}
 			
 		case AdaptablePEGLexer.ID: {
 			return currEnvironment().getValue((Attribute) ((SemanticNode) tree).getSymbol());
 		}
 
 		default:
 			throw new Exception("Not implemented: " + tree.token.getType());
 		}
 	}
 
 	
 	
 }
