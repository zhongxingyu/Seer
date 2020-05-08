 
 import org.antlr.runtime.*;
 import org.antlr.runtime.tree.*;
 
 import java.io.InputStream;
 import java.io.IOException;
 import java.util.List;
 import java.util.Stack;
 
 public class Interpreter {
     
     CommonTree root;               // the AST represents our code memory
     TokenRewriteStream tokens;
     LogoAST2Lexer lex;              // lexer/parser are part of the processor
     LogoAST2Parser parser;
     MemorySpace space = new MemorySpace("main");
 
 	public boolean isdebugging = false;
 	
 	private void debug (String st)
 	{ 
 		if(isdebugging){
 			System.out.println(st);
 		}
 	}
 
 	
 
 	public void interp(InputStream input) throws RecognitionException, IOException {
 			lex = new LogoAST2Lexer(new ANTLRInputStream(input));
 			tokens = new TokenRewriteStream(lex);
 			parser = new LogoAST2Parser(tokens);
 
 			LogoAST2Parser.prog_return r = parser.prog();
 			if ( parser.getNumberOfSyntaxErrors()==0 ) {
 					root = (CommonTree)r.getTree();
 					debug("tree: "+root.toStringTree());
 					block(root);
 			}
 	}
 
 	/** visitor dispatch according to node token type */
 	public Object exec(CommonTree t) {
 		try {
 				switch ( t.getType() ) {
 						case LogoAST2Parser.BLOCK : 	block(t); break;
 						case LogoAST2Parser.ASSIGN : 	assign(t); break;
 						case LogoAST2Parser.PRINT : 	print(t); break;
 						case LogoAST2Parser.IF : 		ifstat(t); break;
 						case LogoAST2Parser.WHILE : 	whileloop(t); break;
 						case LogoAST2Parser.ADD : 		return op(t);
 						case LogoAST2Parser.SUB : 		return op(t);
 						case LogoAST2Parser.MUL : 		return op(t);
 						case LogoAST2Parser.DIV : 		return op(t);
 						case LogoAST2Parser.MOD :     return op(t);
 						case LogoAST2Parser.EQ : 		return eq(t); 
 						case LogoAST2Parser.LT : 		return lt(t);
 						case LogoAST2Parser.GT :    return gt(t);
 						case LogoAST2Parser.LTE:    return lte(t);
 						case LogoAST2Parser.GTE :   return gte(t);
 						case LogoAST2Parser.NOT: 	  return not(t);
 						case LogoAST2Parser.INT : 		return Integer.parseInt(t.getText()); 
 						case LogoAST2Parser.PAREN : 	return paren(t);
 						case LogoAST2Parser.VAL : 		return load(t);
 		
 						default : // catch unhandled node types
 								throw new UnsupportedOperationException("Node "+
 										t.getText()+"<"+t.getType()+"> not handled");
 				}
 		}
 		catch (Exception e) {
 			System.out.print("Caught an error in the switch: ");
 			System.out.println(t);
 			System.out.println(e);
 		}
 			return null;
 	}
 
 	public void block(CommonTree t) {
 		debug("Entered BLOCK");
 		if ( t.getType()!=LogoAST2Parser.BLOCK ) {
 			debug("Problem with BLOCK");
 		}
 		List<CommonTree> stats = t.getChildren();
 		for (CommonTree x : stats) {
 			// System.out.println("Running expr"+x.toStringTree());
 			exec(x);
 		}
 	}
 
 	public void print(CommonTree t) {
 		debug("PRINT: ");
 		//CommonTree expr = (CommonTree)t.getChild(0);
 		//System.out.println( exec(expr) );
 		// Extended for expression lists! //
 		List<CommonTree> exprs = t.getChildren();
 		for (CommonTree x : exprs) {
 			System.out.print( exec(x) + " ");
 		};
 		System.out.println("");
 	}
 
 	public void assign(CommonTree t) {
 		debug("Entered ASSIGN: ");
 		debug(t.getChild(0).getChild(0).getText());
 
 		CommonTree lhs = (CommonTree)t.getChild(0).getChild(0);   // get operands
 		CommonTree expr = (CommonTree)t.getChild(1);
 		Object value = exec(expr);            // walk/evaluate expr
 		space.put(lhs.getText(), value);         // store
 	}
 
 	public void whileloop(CommonTree t) {
 		debug("Entered WHILE:");
 		CommonTree condStart = (CommonTree)t.getChild(0);
 		CommonTree codeStart = (CommonTree)t.getChild(1);
 		Boolean c = (Boolean)exec(condStart);
 		while ( c ) {
 			exec(codeStart);
 			c = (Boolean)exec(condStart);
 		}
 	}
 
 	public void ifstat(CommonTree t) {
 		debug("Entered IF");
 		CommonTree condStart = (CommonTree)t.getChild(0);
 		CommonTree codeStart = (CommonTree)t.getChild(1);
		CommonTree elseCodeStart = null;
		if ( t.getChildCount()==3 ) elseCodeStart = (CommonTree)t.getChild(2);
			Boolean c = (Boolean)exec(condStart);
			if ( ((Boolean)c).booleanValue() ) exec(codeStart);
			else if ( elseCodeStart!=null ) exec(elseCodeStart);
 	}
 
 	public boolean eq(CommonTree t) {
 		debug("Entered EQ");
 		Object a = exec( (CommonTree)t.getChild(0) );
 		Object b = exec( (CommonTree)t.getChild(1) );
 		return a.equals(b);
 	}
 
 	public boolean lt(CommonTree t) {
 		debug("Entered LT");
 		Object a = exec( (CommonTree)t.getChild(0) );
 		Object b = exec( (CommonTree)t.getChild(1) );
 		if ( a instanceof Number && b instanceof Number ) {
 			Number x = (Number)a;
 			Number y = (Number)b;
 			return x.floatValue() < y.floatValue();
 		}
 		return false;
 	}
 
 	public boolean gt(CommonTree t) {
 		debug("Entered GT");
 		Object a = exec( (CommonTree)t.getChild(0) );
 		Object b = exec( (CommonTree)t.getChild(1) );
 		if ( a instanceof Number && b instanceof Number ) {
 			Number x = (Number)a;
 			Number y = (Number)b;
 			return x.floatValue() > y.floatValue();
 		}
 		return false;
 	}
 
 	public boolean lte(CommonTree t) {
 		debug("Entered LTE");
 		Object a = exec( (CommonTree)t.getChild(0) );
 		Object b = exec( (CommonTree)t.getChild(1) );
 		if ( a instanceof Number && b instanceof Number ) {
 			Number x = (Number)a;
 			Number y = (Number)b;
 			return x.floatValue() <= y.floatValue();
 		}
 		return false;
 	}
 
 	public boolean gte(CommonTree t) {
 		debug("Entered GTE");
 		Object a = exec( (CommonTree)t.getChild(0) );
 		Object b = exec( (CommonTree)t.getChild(1) );
 		if ( a instanceof Number && b instanceof Number ) {
 			Number x = (Number)a;
 			Number y = (Number)b;
 			return x.floatValue() >= y.floatValue();
 		}
 		return false;
 	}
 	
 	public boolean not(CommonTree t) {
 		debug( "Entered NOT" );
 		return !(Boolean)exec((CommonTree)t.getChild(0));
 	}
 	
 	public Object op(CommonTree t) {
 		debug("Entered OP");
 		Object a = exec( (CommonTree)t.getChild(0) );
 		Object b = exec( (CommonTree)t.getChild(1) );
 		
 		if ( a instanceof Float || b instanceof Float ) {
 			float x = ((Number)a).floatValue();
 			float y = ((Number)b).floatValue();
 
 			switch (t.getType()) {
 				case LogoAST2Parser.ADD : return x + y;
 				case LogoAST2Parser.SUB : return x - y;
 				case LogoAST2Parser.MUL : return x * y;
 				case LogoAST2Parser.DIV : return x / y;
 				case LogoAST2Parser.MOD : return x % y;
 			}
 		}
 		if ( a instanceof Integer || b instanceof Integer ) {
 	    //System.out.println("Operating on Ints");
 	    //System.out.println(t.getType());
 			int x = ((Number)a).intValue();
 			int y = ((Number)b).intValue();
 			
 			switch (t.getType()) {
 				case LogoAST2Parser.ADD : return x + y;
 				case LogoAST2Parser.SUB : return x - y;
 				case LogoAST2Parser.MUL : return x * y;
 				case LogoAST2Parser.DIV : return x / y;
 				case LogoAST2Parser.MOD : return x % y;
 			}
 		}
 		return 0;
 	}
 
 	public Object paren(CommonTree t) {
 		debug("Entered PAREN");
 		return exec((CommonTree)t.getChild(0));
 	}
 		
 	public Object load(CommonTree t) {
 		debug("Entered LOAD");
 		return space.get(t.getChild(0).getText());
 	}
 }
