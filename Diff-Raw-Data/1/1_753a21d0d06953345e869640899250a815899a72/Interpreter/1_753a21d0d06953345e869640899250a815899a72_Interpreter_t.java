 
 import org.antlr.runtime.*;
 import org.antlr.runtime.tree.*;
 
 import java.io.InputStream;
 import java.io.IOException;
 import java.util.List;
 import java.util.Stack;
 import java.util.ArrayDeque;
 import java.util.*;
 import java.text.ParseException;
 
 public class Interpreter {
     
     CommonTree root;               // the AST represents our code memory
     TokenRewriteStream tokens;
     LogoTurtleLexer lex;              // lexer/parser are part of the processor
     LogoTurtleParser parser;
 
     ArrayDeque <MemorySpace> scopeStack = new ArrayDeque <MemorySpace> ();
 
 
 
 	public boolean isdebugging = false;
 
 	public Interpreter()
 	{
 		scopeStack.add( new MemorySpace("main") );
 	}
 
 	
 	private void debug (String st)
 	{ 
 		if(isdebugging){
 			System.out.println(st);
 		}
 	}
 
 	public void interp(InputStream input) throws RecognitionException, IOException {
 			lex = new LogoTurtleLexer(new ANTLRInputStream(input));
 			tokens = new TokenRewriteStream(lex);
 			parser = new LogoTurtleParser(tokens);
 
 			LogoTurtleParser.prog_return r = parser.prog();
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
 						case LogoTurtleParser.BLOCK : 	block(t); break;
 						case LogoTurtleParser.ASSIGN : 	assign(t); break;
 						case LogoTurtleParser.PRINT : 	print(t); break;
 						case LogoTurtleParser.IF : 		ifstat(t); break;
 						case LogoTurtleParser.IFELSE : 	ifelsestat(t); break;
 						case LogoTurtleParser.WHILE : 	whileloop(t); break;
 						case LogoTurtleParser.ADD : 		return op(t);
 						case LogoTurtleParser.SUB : 		return op(t);
 						case LogoTurtleParser.MUL : 		return op(t);
 						case LogoTurtleParser.DIV : 		return op(t);
 						case LogoTurtleParser.MOD :     	return op(t);
 						case LogoTurtleParser.EQ : 		return eq(t); 
 						case LogoTurtleParser.LT : 		return lt(t);
 						case LogoTurtleParser.GT :    	return gt(t);
 						case LogoTurtleParser.LTE :    	return lte(t);
 						case LogoTurtleParser.GTE :   	return gte(t);
 						case LogoTurtleParser.NOT : 	  	return not(t);
 						case LogoTurtleParser.INT : 		return new Value( Integer.parseInt(t.getText()), LogoTurtleParser.INT );
 						case LogoTurtleParser.FLOAT :    return new Value( Float.parseFloat(t.getText()), LogoTurtleParser.FLOAT );
 						case LogoTurtleParser.PAREN : 	return paren(t);
 						case LogoTurtleParser.REF :		return ref(t);
 						case LogoTurtleParser.VAL : 		return load(t);
 		
 						default : // catch unhandled node types
 								throw new UnsupportedOperationException("Node "+
 										t.getText()+"<"+t.getType()+"> not handled");
 				}
 		}
 		catch (Exception e) {
 			System.out.print("Error: Interpretation failed at '");
 			System.out.print(t);
 			System.out.println("'");
 			System.out.print( "    Exception thrown: " );
 			System.out.println(e);
			System.exit(1);
 		}
 			return null;
 	}
 
 	public void block(CommonTree t) {
 		debug("Entered BLOCK");
 		if ( t.getType()!=LogoTurtleParser.BLOCK ) {
 			debug("Problem with BLOCK");
 		}
 		@SuppressWarnings("unchecked")
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
 		@SuppressWarnings("unchecked")
 		List<CommonTree> exprs = t.getChildren();
 		for (CommonTree x : exprs) {
 			if ( x.getType() == LogoTurtleParser.REF )
 			{
 				System.out.print( x.getChild(0).getText() + " " );
 			}
 			else
 			{
 				System.out.print( ((Value)exec(x)).getValueBasedOnType() + " ");
 			}
 		};
 		System.out.println("");
 	}
 
 	public void assign(CommonTree t) {
 		debug("Entered ASSIGN: ");
 
 		CommonTree lhs = (CommonTree)t.getChild(0).getChild(0);   // get operands
 		CommonTree expr = (CommonTree)t.getChild(1);
 		Object value = exec(expr);
 
 		debug( t.getChild(0).getChild(0).getText() + " = " + ((Value)value).getValueBasedOnType() );
 		
 		scopeStack.peekLast().put(lhs.getText(), value);         // store
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
 		Boolean c = (Boolean)exec(condStart);
 		if ( ((Boolean)c).booleanValue() ) exec(codeStart);
 	}
 
 	public void ifelsestat(CommonTree t) {
 		debug("Entered IFELSE");
 		CommonTree condStart = (CommonTree)t.getChild(0);
 		CommonTree codeStart = (CommonTree)t.getChild(1);
 		CommonTree elseStart = (CommonTree)t.getChild(2);
 		
 		Boolean c = (Boolean)exec(condStart);
 		if ( ((Boolean)c).booleanValue() )
 		{
 			debug( "in if codeblock" );
 			exec(codeStart);
 		}
 		else
 		{
 			debug( "in else codeblock" );
 			exec( elseStart );
 		}
 	}
 	
 	public boolean eq(CommonTree t) {
 		debug("Entered EQ");
 		Value a = (Value)exec( (CommonTree)t.getChild(0) );
 		Value b = (Value)exec( (CommonTree)t.getChild(1) );
 		return a.equals(b);
 	}
 
 	public boolean lt(CommonTree t) {
 		debug("Entered LT");
 		Object a = exec( (CommonTree)t.getChild(0) );
 		Object b = exec( (CommonTree)t.getChild(1) );
 		if ( a instanceof Value && b instanceof Value ) {
 			Value x = (Value)a;
 			Value y = (Value)b;
 			return x.floatValue() < y.floatValue();
 		}
 		return false;
 	}
 
 	public boolean gt(CommonTree t) {
 		debug("Entered GT");
 		Object a = exec( (CommonTree)t.getChild(0) );
 		Object b = exec( (CommonTree)t.getChild(1) );
 		if ( a instanceof Value && b instanceof Value ) {
 			Value x = (Value)a;
 			Value y = (Value)b;
 			return x.floatValue() > y.floatValue();
 		}
 		return false;
 	}
 
 	public boolean lte(CommonTree t) {
 		debug("Entered LTE");
 		Object a = exec( (CommonTree)t.getChild(0) );
 		Object b = exec( (CommonTree)t.getChild(1) );
 		if ( a instanceof Value && b instanceof Value ) {
 			Value x = (Value)a;
 			Value y = (Value)b;
 			return x.floatValue() <= y.floatValue();
 		}
 		return false;
 	}
 
 	public boolean gte(CommonTree t) {
 		debug("Entered GTE");
 		Object a = exec( (CommonTree)t.getChild(0) );
 		Object b = exec( (CommonTree)t.getChild(1) );
 		if ( a instanceof Value && b instanceof Value ) {
 			Value x = (Value)a;
 			Value y = (Value)b;
 			return x.floatValue() >= y.floatValue();
 		}
 		return false;
 	}
 	
 	public boolean not(CommonTree t) {
 		debug( "Entered NOT" );
 		return !(Boolean)exec((CommonTree)t.getChild(0));
 	}
 	
 	public Value op(CommonTree oper) throws ParseException {
 		debug("Entered OP");
 		
 		Value a = null;
 		Value b = null;
 		
 		try {
 			a = (Value)exec( (CommonTree)(oper.getChild(0) ) );
 		  b = (Value)exec( (CommonTree)(oper.getChild(1) ) );
 		}
 		catch ( ClassCastException ex ) {
 			throw new ParseException( "Cannot perform arithmetic operations on non-value types.", 0 );
 		}
 		
 		Value retVal = null;
 		
 		if ( isInt( a ) && isInt( b ) ) {
 	    
 			int x = a.intValue();
 			int y = b.intValue();
 			
 			retVal = new Value( performOperation( oper, x, y ), LogoTurtleParser.INT );
 		}
 		else if ( isFloat( a ) && isFloat( b ) ) {
 			float x = a.floatValue();
 			float y = b.floatValue();
 
 		  retVal = new Value( performOperation( oper, x, y ), LogoTurtleParser.FLOAT );
 		}
 		else if ( ( isFloat( a ) && isInt( b ) ) || 
 		          ( isInt( a ) && isFloat( b ) ) ) {
 			System.out.println( "Warning: Promoting integer to float." );
 			
 			float x = a.floatValue();
 			float y = b.floatValue();
 			
 			retVal =  new Value( performOperation( oper, x, y ), LogoTurtleParser.FLOAT );
 		}
 		
 		return retVal;
 	}
 
 	public Integer performOperation( CommonTree node, Integer x, Integer y ) {
 		debug( "performing op on integers" );
 		
 		switch (node.getType()) {
 			case LogoTurtleParser.ADD : return x + y;
 			case LogoTurtleParser.SUB : return x - y;
 			case LogoTurtleParser.MUL : return x * y;
 			case LogoTurtleParser.DIV : return x / y;
 			case LogoTurtleParser.MOD : return x % y;
 			default: throw new UnsupportedOperationException("Node "+ node.getText()+"<"+node.getType()+"> not handled");
 		}
 	}
 	
 	public Float performOperation( CommonTree node, Float x, Float y ) {
 		debug( "performing op on floats" );
 		
 		switch (node.getType()) {
 			case LogoTurtleParser.ADD : return x + y;
 			case LogoTurtleParser.SUB : return x - y;
 			case LogoTurtleParser.MUL : return x * y;
 			case LogoTurtleParser.DIV : return x / y;
 			case LogoTurtleParser.MOD : return x % y;
 			default: throw new UnsupportedOperationException("Node "+ node.getText()+"<"+node.getType()+"> not handled");
 		}
 	}
 		
 	public boolean isInt( Value val ) {
 		return val.getType() == LogoTurtleParser.INT;
 	}
 	
 	public boolean isFloat( Value val ) {
 		return val.getType() == LogoTurtleParser.FLOAT;
 	}
 	
 	public Object paren(CommonTree t) {
 		debug("Entered PAREN");
 		return exec((CommonTree)t.getChild(0));
 	}
 		
 	public Object load(CommonTree t) {
 		debug("Entered LOAD");
 		return scopeStack.peekLast().get(t.getChild(0).getText());
 	}
 
 	public Object ref(CommonTree t) {
 		debug("Entered REF");
 		return t.getChild(0).getText();
 	}
 }
