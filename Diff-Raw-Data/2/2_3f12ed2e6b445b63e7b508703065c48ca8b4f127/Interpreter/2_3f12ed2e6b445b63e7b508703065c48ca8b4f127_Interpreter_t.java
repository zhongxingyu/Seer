 package kaygan;
 
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import kaygan.ast.*;
 
 public class Interpreter
 {
 	public static List<Object> interpret(String input)
 	{
 		Parser parser = new Parser(new StringReader(input));
 		
 		return interpret(parser.program(), new Scope());
 	}
 	
 	public static List<Object> interpret(String input, Scope scope)
 	{
 		Parser parser = new Parser(new StringReader(input));
 		
 		return interpret(parser.program(), scope);
 	}
 	
 	public static List<Object> interpret(List<Exp> exps, Scope scope)
 	{
 		List<Object> results = new ArrayList<Object>();
 		
 		for( Exp exp : exps )
 		{
 			Object result = interpret(exp, scope);
 			if( result != null )
 			{
 				results.add( result );
 			}
 		}
 		
 		return results;
 	}
 	
 	protected static Object interpret(Exp exp, Scope scope)
 	{
 		if( exp instanceof Function )
 		{
 			return intrFunction( (Function)exp, scope );
 		}
 		else if( exp instanceof Array )
 		{
 			return intrArray( (Array)exp, scope );
 		}
 		else if( exp instanceof Callsite )
 		{
 			return intrCallsite( (Callsite)exp, scope );
 		}
 		else if( exp instanceof Bind )
 		{
 			return intrBind((Bind)exp, scope);
 		}
 		else if( exp instanceof Range )
 		{
 			return intrRange((Range)exp, scope);
 		}
 		else if( exp instanceof Symbol )
 		{
 			Symbol symbol = ((Symbol)exp);
 			Object resolved = scope.get( symbol.symbol() );
 			if( resolved == null )
 			{
 				return error(symbol, "Unresolved symbol: " + symbol.symbol());
 			}
 			return resolved;
 		}
 		else if( exp instanceof Value )
 		{
 			return intrValue( ((Value)exp), scope );
 		}
 		
 		return error(exp, "Invalid expression: " + exp);
 	}
 	
 	protected static Object intrFunction(Function f, Scope parentScope)
 	{
 		f.scope = parentScope.newSubScope();
 		return f;
 	}
 	
 	protected static Object intrArray(Array a, Scope parentScope)
 	{
 		List<Object> result = new ArrayList<Object>();
 		for( Exp exp : a.contents )
 		{
 			result.add( interpret(exp, parentScope) );
 		}
 		return result;
 	}
 	
 	protected static Object intrCallsite(Callsite c, Scope parentScope)
 	{
 		Exp first = c.contents.get(0);
 		Object o = interpret( first, parentScope );
 		
 		// TODO how a call site is interpreted by its receiver
 		//      really depends on the receiver...
 		//
 		//      receiver: a: { b c | b + c }
 		//      callsite: (a d 2)
 		//
 		//        this receiver is going to want to lookup 'd' in calling scope
 		//
 		//      receiver:   a: [ b: { c | print "Hello" + c } ]
 		//      callsite:   (a b f)
 		//
 		//        this receiver is going to want to lookup 'b' in the
 		//        scope of a receiver to resolve a method, but lookup 'f'
 		//        (which for the purposes of this discussion let's say
 		//         is the string "Brian') in the scope of the caller
 		//
 		//      so for a general rule, functions look up arguments
 		
 		if( o instanceof Function )
 		{
 			Function f = (Function)o;
 			
 			if( f.args.size() != c.contents.size() - 1 )
 			{
 				// function arguments should match callsite size
 				//
 				// TODO this should be configurable by binding
				error(c, 
 						"Expected " + f.args.size() 
 							+ " arguments, found " + (c.contents.size() - 1) );
 			}
 			
 			System.out.println("building arguments for " + f);
 			
 			// bind the parameters to a new function scope
 			Scope functionScope = f.scope.newSubScope(); //parentScope.newSubScope();
 			
 			for( int i=1; i<c.contents.size(); i++ )
 			{
 				Exp argSender = c.contents.get(i);
 				
 				Exp argReceiver = f.args.get(i-1);
 				
 				if( argReceiver instanceof Symbol )
 				{
 					functionScope.set(	((Symbol)argReceiver).symbol(),
 										interpret(argSender, parentScope) );
 				}
 				else if( argReceiver instanceof Bind )
 				{
 					functionScope.set(	((Bind)argReceiver).symbol.symbol(),
 										interpret(argSender, parentScope) );
 				}
 				else
 				{
 					//throw new RuntimeException("Invalid receiver: " + argReceiver);
 					error(argReceiver, "Invalid receiver");
 				}
 			}
 			
 			System.out.println("executing function with scope: " + functionScope);
 			
 			Object last = null;
 			for( Exp fExp : f.contents )
 			{
 				last = interpret(fExp, functionScope);
 				System.out.println("function intermediate result: " + last);
 			}
 			return last;
 		}
 		else
 		{
 			
 		
 //			for( int i=1; i<c.contents.size(); i++ )
 //			{
 //				Exp exp = c.contents.get(i);
 //				if( exp instanceof Symbol )
 //				{
 //					
 //				}
 //			}
 		}
 		return error(first, "Unknown expression: " + first);
 	}
 	
 	protected static Object intrBind(Bind bind, Scope scope)
 	{
 		scope.set(bind.symbol.symbol(), interpret(bind.exp, scope));
 		return null;
 	}
 	
 	protected static Object intrRange(Range range, Scope scope)
 	{
 		Object from = interpret(range.from, scope);
 		Object to = interpret(range.to, scope);
 		
 		return new StringBuilder()
 			.append("<Range ").append(from).append("..").append(to).append(">");
 	}
 	
 	protected static Object intrValue(Value value, Scope scope)
 	{
 		if( value instanceof Num )
 		{
 			// interpret token value
 			Token token = ((Num)value).token;
 			switch( token.type )
 			{
 			case Binary:
 				return Integer.parseInt(token.value.substring(2), 2);
 				
 			case Hex:
 				return Integer.parseInt(token.value.substring(2), 16);
 				
 			case Int:
 				return Integer.parseInt(token.value);
 				
 			case Real:
 				return Double.parseDouble(token.value);
 				
 			default:
 				return error(value, "Invalid number: " + token.value);
 			}
 		}
 		else if( value instanceof Str )
 		{
 			return ((Str)value).token.value;
 		}
 		else
 		{
 			return error(value, "Invalid value: " + value);
 		}
 	}
 	
 	protected static Object error(ASTNode node, String message)
 	{
 		throw new InterpretException(node, message);
 	}
 	
 	
 	public static class InterpretException extends RuntimeException
 	{
 		private static final long serialVersionUID = 1L;
 		
 		public final ASTNode astNode;
 		
 		public InterpretException(ASTNode astNode, String message)
 		{
 			super( message );
 			
 			this.astNode = astNode;
 		}
 	}
 	
 }
