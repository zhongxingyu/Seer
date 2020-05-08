 package semantics;
 
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Hashtable;
 import java.util.Stack;
 import org.antlr.runtime.tree.CommonTree;
 import srcparser.AdaptablePEGLexer;
 
 public class Grammar {
 	
 	private Hashtable<String, NonTerminal> nonTerms = new Hashtable<String, NonTerminal>();
 	private Hashtable<String, Method> functions = new Hashtable<String, Method>();
 	
 	private FileReader input;
 	private ArrayList<Character> buf;
 //	private int currPos;
 	Stack<Environment> environments;
 	
 	private static char EOF = (char) -1;
 	
 	public Grammar() {
 		buf = new ArrayList<Character>();
 		environments = new Stack<Environment>();
 	}
 	
 	public Environment currEnvironment() {
 		return environments.peek();
 	}
 	
 	public NonTerminal getNonTerminal(String name) {
 		return nonTerms.get(name);
 	}
 
 	public NonTerminal addNonTerminal(String name) {
 		if (getNonTerminal(name) != null) {
 			return null;
 		}
 		NonTerminal n = new NonTerminal(name);
 		nonTerms.put(name, n);
 		return n;
 	}
 	
 	public Method getFunction(String name) {
 		return functions.get(name);
 	}
 	
 	public Method addFunction(Method m) {
 		if (getFunction(m.getName()) != null) {
 			return null;
 		}
 		functions.put(m.getName(), m);
 		return m;
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
		for (int i = buf.size(); i <= pos; ++i) {
 			ch = (char) input.read();
 			buf.add(ch);
		}
 		return ch;
 	}
 	
 	private int match(String s, int pos) throws Exception {
 		for (int i = 0; i < s.length(); ++i) {
			char ch = read(pos);
 			char ch2 = s.charAt(i);
 			if (ch != ch2) {
 				return -1;
 			}
 		}
 		return pos + s.length();
 	}
 
 	public int execute(String nontermName) throws Exception {
 		NonTerminal nt = getNonTerminal(nontermName);
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
 	
 	/**
 	 * Process an expression, reading the first character at position pos of the input file.
 	 * The first call must be with pos=0.
 	 * @param tree
 	 * @param pos
 	 * @return
 	 * @throws Exception
 	 */
 	public int process(CommonTree tree, int pos) throws Exception {
 		switch (tree.token.getType()) {
 		
 		case AdaptablePEGLexer.NONTERM: {
 			// I suppose there are 2 children
 			NonTerminal nt = (NonTerminal) ((SemanticNode) tree.getChild(0)).getSymbol(); // name of nonterminal
 			CommonTree t = (CommonTree) tree.getChild(1); // list of arguments
 			Environment env = buildEnvironment(nt);
 			for (int i = 0; i < nt.getNumParam(); ++i) {
 				Object x = eval((CommonTree)t.getChild(i));
 				env.setValue(i, x);
 			}
 			environments.push(env);
 			int ret = process(nt.getPegExpr(), pos);
 			environments.pop();
 			if (ret >= 0) {
 				int first = nt.getNumParam();
 				int last = first + nt.getNumRet();
 				for (int i = first; i < last; ++i) {
 					Object x = env.getValue(i);					
 					SemanticNode y = (SemanticNode) t.getChild(i);
 					currEnvironment().setValue(((Attribute) y.getSymbol()).getIndex(), x);
 				}
 			}
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
 
 	
 	
 	public static void main(String args[]) throws Exception {
 		FileReader f = new FileReader("input/teste01.txt");
 		int ich;
 		while ((ich = f.read()) != -1) {
 			char ch = (char) ich;
 			System.out.print(ch);
 		}
 	}
 	
 }
