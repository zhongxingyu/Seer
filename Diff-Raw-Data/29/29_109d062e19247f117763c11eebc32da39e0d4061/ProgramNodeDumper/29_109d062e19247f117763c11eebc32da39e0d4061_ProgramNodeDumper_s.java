 package tw.maple;
 
 
 import java.io.PrintWriter;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import macromedia.asc.parser.*;
 import macromedia.asc.semantics.Value;
 import macromedia.asc.semantics.StringValue;
 import macromedia.asc.semantics.StringListValue;
 import macromedia.asc.semantics.QNValue;
 import macromedia.asc.util.Context;
 //import sun.org.mozilla.javascript.internal.EvaluatorException;
 import tw.maple.generated.*;
 import tw.maple.StringEvaluator;
 import static macromedia.asc.parser.Tokens.*;
 
 public final class ProgramNodeDumper implements Evaluator 
 {
 	AstDumper.Client thrift_cli;
 	StringEvaluator	string_evaluator;
 	private boolean is_abstract_function = false;
	private boolean DEBUG = true;
 	
     public ProgramNodeDumper(AstDumper.Client cli)
     {
     	thrift_cli = cli;
     	string_evaluator = new StringEvaluator();
     }
     public boolean checkFeature(Context cx, Node node)
     {
     	return true;
     }
 
 	// Base node
 
 	public Value evaluate(Context cx, Node node)
 	{
 		return null;
 	}
 
 	// Expression evaluators
 
 	public Value evaluate(Context cx, IncrementNode node)
 	{
 		try {
 //			Identifier id = new Identifier();
 //			id.name = node.name;
 			thrift_cli.startIncrementExpression( 
 					node.isPostfix ?IncrementType.TYPE_POSTFIX:IncrementType.TYPE_PREFIX
 					, Token.getTokenClassName(node.op)
 					);
 			node.expr.evaluate(cx, this);
 			thrift_cli.endIncrementExpression( );
 		} catch (org.apache.thrift.TException e1) {
 
 		}			
 		return null;
 	}
 
 	public Value evaluate(Context cx, DeleteExpressionNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, IdentifierNode node)
 	{
 		System.out.println((new Throwable()).getStackTrace()[0].toString()+":"+node.name);
         if(node instanceof TypeIdentifierNode)
         {
         	System.out.println( " is type identifier ");
         }
         else if (node.isAttr())
         {
     		System.out.println( " is attr");
         }
         else
         {
 				try {
 					Identifier id = new Identifier();
 					id.name = node.name;
 					thrift_cli.identifierExpression(id);
 				} catch (org.apache.thrift.TException e1) {

 				}			
         }
         
         
 		return null;
 	}
 
	public Value evaluate(Context cx, InvokeNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, ThisExpressionNode node)
 	{
 //		if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  
 		
 //		return null;
 		try {
 			thrift_cli.thisExpression();
 		} catch (org.apache.thrift.TException e1) {
 		}
 		return null;
 	}
 
 	public Value evaluate(Context cx, QualifiedIdentifierNode node) 
 	{
 		if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}
 		try {
 			Identifier id = new Identifier();
 			id.name = node.name;
 			id.qualifier = "";
 
 			if (node.qualifier != null) {
 				{
 					Value ret_value = node.qualifier.evaluate(cx,
 							string_evaluator);
 					id.qualifier = Extract2String(ret_value);
 				}
 			}
 			thrift_cli.identifierExpression(id);
 		} catch (org.apache.thrift.TException e1) {
 		}		
 		return null;
 	}
 
 	public Value evaluate(Context cx, QualifiedExpressionNode node) {
 		if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}
 		return null;
 	}
 
     public Value evaluate(Context cx, LiteralBooleanNode node)
     {
     	if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}
     	try 
 		{
 			tw.maple.generated.Literal str = new tw.maple.generated.Literal();
 			if( node.value == true )
 				str.value = "true";
 			else
 				str.value = "false";
 			thrift_cli.literalBooleanExpression( str );
 		}
 		catch (org.apache.thrift.TException e1) 
 		{	
 		}
 		return null;
     }
 
 	public Value evaluate(Context cx, LiteralNumberNode node)
 	{
 		if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}
 		try 
 		{
 			tw.maple.generated.Literal str = new tw.maple.generated.Literal();
 			str.value = node.value;
 			thrift_cli.literalNumberExpression( str );
 		}
 		catch (org.apache.thrift.TException e1) 
 		{
 		}	
 		return null;
 	}
 
 	public Value evaluate(Context cx, LiteralStringNode node)
 	{
 		try {
 			tw.maple.generated.Literal str = new tw.maple.generated.Literal();
 			str.value = node.value;
 			thrift_cli.literalStringExpression(str);
 		} catch (org.apache.thrift.TException e1) {
 		}
 		
 		return null;
 	}
 
 	public Value evaluate(Context cx, LiteralNullNode node)
 	{
 		if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}
 		
 		try {
 			thrift_cli.literalNull();
 		} catch (org.apache.thrift.TException e1) {
 		}
 	
 		return null;
 	}
 
 	public Value evaluate(Context cx, LiteralRegExpNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, LiteralXMLNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, FunctionCommonNode node)
 	{
 //		if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}
         if (node.signature != null)
         {
             node.signature.evaluate(cx, this);
         }
         if (node.body != null)
         {
             node.body.evaluate(cx, this);
         }
 		return null;
 	}
 
 	public Value evaluate(Context cx, ParenExpressionNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, ParenListExpressionNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, LiteralObjectNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, LiteralFieldNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, LiteralArrayNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 	
 	public Value evaluate(Context cx, LiteralVectorNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, SuperExpressionNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, SuperStatementNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, MemberExpressionNode node)
 	{
 		try
 		{		
 			thrift_cli . startMemberExpression();
 			
 	        if (node.base != null)
 	        	node.base.evaluate(cx, this);
 	        else
 	        	thrift_cli . empty();
 	        
 	        if (node.selector != null)
 	            node.selector.evaluate(cx, this);
 	        else
 	        	thrift_cli . empty();
 	        
 	       	thrift_cli . endMemberExpression( );
 	        
 			return null;
 
 		} 
 		catch (org.apache.thrift.TException e1)
 		{
 		}
 
 		return null;
 	}
 
 	public Value evaluate(Context cx, CallExpressionNode node)
 	{
 		CallExpression call_expression;
 		
 		try {
 			call_expression = new CallExpression();
 			call_expression.is_new = node.is_new;
 			call_expression.mode = (node.getMode() == LEFTBRACKET_TOKEN ? "bracket" :
 	            	node.getMode() == LEFTPAREN_TOKEN ? "filter" :
 	                node.getMode() == DOUBLEDOT_TOKEN ? "descend" :
 	                node.getMode() == EMPTY_TOKEN ? "lexical" : "dot");
 			
 			if (node.expr != null) {
 				// Callee
 //				node.expr.evaluate(cx, this);
 				
 				Value ret_value = node.expr.evaluate(cx,
 						string_evaluator);
 				call_expression.callee = Extract2StringList(ret_value);
 				
 				for( int idx = 0 ; idx < call_expression.callee.size() ; idx ++ )
 					System.out.println(" callee["+idx+"/"+call_expression.callee.size()+"]  "+ call_expression.callee.get(idx)  );
 			}
 			
 			thrift_cli.startCallExpression(call_expression);
 
 			if (node.args != null) {
 				thrift_cli.startArgumentList();
 					node.args.evaluate(cx, this);
 				thrift_cli.endArgumentList();
 			}
 			
 			thrift_cli.endCallExpression();
 		} 
 		catch (org.apache.thrift.TException e1) 
 		{
 		}
 
 		return null;
 	}
 
 	public Value evaluate(Context cx, GetExpressionNode node)
 	{
 		if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}
 	
 //		if( node.getMode() == EMPTY_TOKEN )
 //			return node.expr.evaluate(cx, this);
 			
 		try{
 			thrift_cli.startGetExpression(
 					(node.getMode() == LEFTBRACKET_TOKEN ? "bracket" :
 			            node.getMode() == LEFTPAREN_TOKEN ? "filter" :
 			            node.getMode() == DOUBLEDOT_TOKEN ? "descend" :
 			            node.getMode() == EMPTY_TOKEN ? "lexical" : "dot")
 				);
 			if (node.expr != null)
				return node.expr.evaluate(cx, this);
//			else
//				thrift_cli.empty();
        	
 			thrift_cli.endGetExpression();
 		} catch (org.apache.thrift.TException e1) {
 		}
 		return null;
 	}
 
 	public Value evaluate(Context cx, SetExpressionNode node) 
 	{
 		if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}
 		try {
 
 			thrift_cli.startSetExpression(
 					(node.getMode() == LEFTBRACKET_TOKEN ? "bracket" :
 			            node.getMode() == LEFTPAREN_TOKEN ? "filter" :
 			            node.getMode() == DOUBLEDOT_TOKEN ? "descend" :
 			            node.getMode() == EMPTY_TOKEN ? "lexical" : "dot")
 			      );
 			
 			if (node.expr != null) 
 				node.expr.evaluate(cx, this);
 			else
 				thrift_cli.empty();
 				
 			if (node.args != null) 
 				node.args.evaluate(cx, this);
 			else
 				thrift_cli.empty();
 			
 			thrift_cli.endSetExpression();
 		} catch (org.apache.thrift.TException e1) {
 		}
 		return null;
 	}
 
     public Value evaluate(Context cx, ApplyTypeExprNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, UnaryExpressionNode node) 
 	{
 		if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}
 		try {
 			UnaryExpression unary_expression = new UnaryExpression();
 			unary_expression.op = Token.getTokenClassName(node.op);
 			thrift_cli.startUnaryExpression(unary_expression);
 			if (node.expr != null) {
 				node.expr.evaluate(cx, this);
 			}
 			thrift_cli.endUnaryExpression();
 		} catch (org.apache.thrift.TException e1) {
 		}
 		return null;
 	}
 
 	public Value evaluate(Context cx, BinaryExpressionNode node) 
 	{	
 		if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}
 		try {
 			if( Token.getTokenClassName(node.op) == "instanceof" ) {
 				thrift_cli.startInstanceOfExpression();
 				if (node.lhs != null) {
 					node.lhs.evaluate(cx, this);
 				}
 
 				if (node.rhs != null) {
 					node.rhs.evaluate(cx, this);
 				}
 				thrift_cli.endInstanceOfExpression();	
 			} else if( Token.getTokenClassName(node.op) == "is" ) {
 				thrift_cli.startIsOperator();
 				if (node.lhs != null) {
 					node.lhs.evaluate(cx, this);
 				}
 
 				if (node.rhs != null) {
 					node.rhs.evaluate(cx, this);
 				}
 				thrift_cli.endIsOperator();
 			} else if( Token.getTokenClassName(node.op) == "as" ) {
 				thrift_cli.startAsOperator();
 				if (node.lhs != null) {
 					node.lhs.evaluate(cx, this);
 				}
 
 				if (node.rhs != null) {
 					node.rhs.evaluate(cx, this);
 				}
 				thrift_cli.endAsOperator();
 			} else {
 				BinaryExpression binary_expression = new BinaryExpression();
 				binary_expression.op = Token.getTokenClassName(node.op);
 				thrift_cli.startBinaryExpression(binary_expression);
 				if (node.lhs != null) {
 					node.lhs.evaluate(cx, this);
 				}
 
 				if (node.rhs != null) {
 					node.rhs.evaluate(cx, this);
 				}
 				thrift_cli.endBinaryExpression();				
 			}
 		} catch (org.apache.thrift.TException e1) {
 		}
 	
 		return null;
 	}
 
 	public Value evaluate(Context cx, ConditionalExpressionNode node) 
 	{
 		if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}
 		
 		try {
 			
 			thrift_cli.startConditionExpression();
 			
 	        if (node.condition != null)	        
 	            node.condition.evaluate(cx, this);
 	        else
 	        	thrift_cli.empty();
 
 	        if (node.thenexpr != null)
 	            node.thenexpr.evaluate(cx, this);
 	        else
 	        	thrift_cli.empty();
 	        	
 	        if (node.elseexpr != null)
 	            node.elseexpr.evaluate(cx, this);
 	        else
 	        	thrift_cli.empty();
 					
 			thrift_cli.endConditionExpression();
 		} catch (org.apache.thrift.TException e1) {
 		}
 		return null;
 	}
 
 	public Value evaluate(Context cx, ArgumentListNode node)
 	{
 		if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}
 		try {
 			for (Node n : node.items) {
 				thrift_cli.startOneArgument();
 					n.evaluate(cx, this);
 				thrift_cli.endOneArgument();
 			}
 		} catch (org.apache.thrift.TException e1) {
 		}
 
 		return null;
 	}
 
 	public Value evaluate(Context cx, ListNode node)
 	{
 		if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}
 		StringListValue values = new StringListValue();
 		for ( Node n : node.items )
 		{
 			Value v = n.evaluate( cx, this );
 			values.values.add( Extract2String( v ) );
 		}
 
 		return values;
 	}
 
 	// Statements
 
 	public Value evaluate(Context cx, StatementListNode node)
 	{
 		if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}
 		try {
 			thrift_cli.startStmtList();
 			for (Node n : node.items) {
 				if (n != null) {
 					n.evaluate(cx, this);
 				}
 			}
 			thrift_cli.endStmtList();
 		} catch (org.apache.thrift.TException e1) {
 		}        
         return null;
 	}
 
 	public Value evaluate(Context cx, EmptyElementNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, EmptyStatementNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, ExpressionStatementNode node)
 	{
 		try 
 		{
 			thrift_cli.startStmtExpression();
 			thrift_cli.startExpressionList();
         		if (node.expr != null)
         		{
         			node.expr.evaluate(cx, this);
         		}
         	thrift_cli.endExpressionList();
         	thrift_cli.endStmtExpression();
 		} catch( org.apache.thrift.TException e1 ) {
 			System.out.print("\nERROR - "+e1.toString());
 			System.exit(1);		
 		}
 
 		return null;
 	}
 
 	public Value evaluate(Context cx, LabeledStatementNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, IfStatementNode node) 
 	{
 		try { 
 			thrift_cli.startIfStatement();
 			
 			if (node.condition != null) 
 				node.condition.evaluate(cx, this);
 			else
 				thrift_cli.empty();
 		 
 
 			thrift_cli.startScope();
 
 			if (node.thenactions != null)
 				node.thenactions.evaluate(cx, this);
 			thrift_cli.endScope();
 
 			thrift_cli.startScope();
 
 			if (node.elseactions != null)
 				node.elseactions.evaluate(cx, this);
 			thrift_cli.endScope();
 
 			thrift_cli.endIfStatement();
 		} 
 		catch (org.apache.thrift.TException e1) { }
 		
 		return null;
 	}
 
 	public Value evaluate(Context cx, SwitchStatementNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, CaseLabelNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, DoStatementNode node)
 	{
 		if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}
 		try{
 			thrift_cli.startDoStatement();
 		
 	        if (node.expr != null)
 	        {
 	            node.expr.evaluate(cx, this);
 	        }
 	        else
 	        	thrift_cli.empty();
 	        
 	     
 	        thrift_cli.startScope();
 		        if (node.statements != null)
 		        	node.statements.evaluate(cx, this);
 	        thrift_cli.endScope();
 		
 	        thrift_cli.endDoStatement();
 		} catch (org.apache.thrift.TException e1) {
 		}
 		return null;
 	}
 
 	public Value evaluate(Context cx, WhileStatementNode node)
 	{
 		if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}
 		try{
 			thrift_cli.startWhileStatement();
 		
 	        if (node.expr != null)
 	        {
 	            node.expr.evaluate(cx, this);
 	        }
 	        else
 	        	thrift_cli.empty();
 	        
 	     
 	        thrift_cli.startScope();
 		        if (node.statement != null)
 		        	node.statement.evaluate(cx, this);
 	        thrift_cli.endScope();
 		
 	        thrift_cli.endWhileStatement();
 		} catch (org.apache.thrift.TException e1) {
 		}
 		return null;
 	}
 
 	public Value evaluate(Context cx, ForStatementNode node)
 	{
 		if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}
 		try{
 			thrift_cli.startForStatement();
 		
 			thrift_cli.startForInit();
 			if (node.initialize != null)
 	            node.initialize.evaluate(cx, this);
 			thrift_cli.endForInit();
 			
 	        if (node.test != null)
 	            node.test.evaluate(cx, this);
 	        else
 	        	thrift_cli.empty();
 	        
 	        thrift_cli.startForStep();
 	        if (node.increment != null)
 	        	node.increment.evaluate(cx, this);
 	        thrift_cli.endForStep();
 	        thrift_cli.startScope();
 		        if (node.statement != null)
 		        	node.statement.evaluate(cx, this);
 	        thrift_cli.endScope();
 		
 	        thrift_cli.endForStatement();
 		} catch (org.apache.thrift.TException e1) {
 		}
 
 		return null;
 	}
 
 	public Value evaluate(Context cx, WithStatementNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, ContinueStatementNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, BreakStatementNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, ReturnStatementNode node)
 	{
 		if (node.expr == null) 
 			return null;
 		
 		try {
 			thrift_cli.startReturnStatement();
 			if (node.expr != null) {
 				node.expr.evaluate(cx, this);
 			}
 			thrift_cli.endReturnStatement();
 		} catch (org.apache.thrift.TException e1) {
 			System.out.print("\nERROR - " + e1.toString());
 			System.exit(1);
 		}
 
 		return null;
 		
 	}
 
 	public Value evaluate(Context cx, ThrowStatementNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, TryStatementNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, CatchClauseNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, FinallyClauseNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, UseDirectiveNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, IncludeDirectiveNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, ImportNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, MetaDataNode node)
 	{
 		if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}
 		MetaData metadata = new MetaData();
         metadata.keyvalues = new HashMap<String,String>();
         metadata.values = new ArrayList<String>();
         
         if (node.data != null)
         {   
             MetaDataEvaluator mde = new MetaDataEvaluator();
             mde.evaluate(cx, node);
         }
 
 		
 		metadata.id = node.getId() !=null? node.getId() :"";
         System.out.println("meta data '"+metadata.id+"'" );
 		for (int idx = 0, length = (node.getValues() == null) ? 0 : node.getValues().length; idx < length; idx++)
         {
             Value v = node.getValues()[idx];
             if (v instanceof MetaDataEvaluator.KeyValuePair)
             {
                 MetaDataEvaluator.KeyValuePair pair = (MetaDataEvaluator.KeyValuePair) v;
                 metadata.keyvalues.put(pair.key, pair.obj);
                 System.out.println("meta data k '"+pair.key+"' v '"+pair.obj+"'");
             }
 
             else if (v instanceof MetaDataEvaluator.KeylessValue)
             {
                 MetaDataEvaluator.KeylessValue val = (MetaDataEvaluator.KeylessValue) v;
                 //out.print("[" + val.obj + "]");
                 metadata.values.add( val.obj );
 		        System.out.println("meta data v"+val.obj);
             }
 
         }
 		try {
 			thrift_cli.defineMetaData( metadata );
 		} catch (org.apache.thrift.TException e1) {
 		}
 		return null;
 	}
 	
 	public Value evaluate(Context cx, DocCommentNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	// Definitions
 
 	public Value evaluate(Context cx, ImportDirectiveNode node)
 	{
 		if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}
 	
 		try{
 			List<String> pkg_name_list = new ArrayList<String>();
 			if (node.name != null) {
 				for (IdentifierNode id : node.name.id.list) {
 					pkg_name_list.add(id.name);
 				}
 			}
 
 			thrift_cli.executeImport(pkg_name_list);
 
 		} catch (org.apache.thrift.TException e1) {
 		}
 		return null;
 	}
 
 	public Value evaluate(Context cx, AttributeListNode node) {
 		if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}
 		// This Function always evaluate "value" insteadof "thrift"
 
 		StringListValue attrs = new StringListValue();
 		
         for (Node n : node.items)
         {
             Value v = n.evaluate(cx, string_evaluator);
             attrs.values.add( Extract2String( v ) );
         }
        
 		return attrs;
 	}
 
 	public Value evaluate(Context cx, VariableDefinitionNode node) 
 	{
 //        if (node.kind == CONST_TOKEN)
 //        {
 //            out.print("const");
 //        }
 //        else
 //        {
 //            out.print("var");
 //        }
 		try {
 			
 			List<String> str_attrs = new ArrayList<String>();
 	        if (node.attrs != null)
 	        {
 	            Value v = node.attrs.evaluate( cx, string_evaluator );
 
 	            str_attrs =  Extract2StringList( v );
 	        }
 //	        if (node.list != null)
 //	        {
 //	            node.list.evaluate(cx, this);
 //	        }
         
 	        for (Node n : node.list.items)
 			{
 	        	if( n instanceof VariableBindingNode ){
 
 	        		VariableBindingNode vbnode = (VariableBindingNode)(n);
 	        			        		
 	        		VariableDeclare var_decl = new VariableDeclare();
 	        		{
 		        		var_decl.attributes = str_attrs;
 		        		Value type_value = vbnode.variable.type.evaluate(cx, string_evaluator);
 		        		var_decl.type = Extract2StringList( type_value );
 		        		var_decl.name = vbnode.variable.identifier.name;
 		        		var_decl.has_initialize = (vbnode.initializer != null);
 	        		}
 	        		thrift_cli.startVariableDeclare( var_decl );
 	        		if(vbnode.initializer != null)
 	        			vbnode.initializer.evaluate(cx, this);
 	        		thrift_cli.endVariableDeclare();		
 	        	} else{
 	        		System.out.println("variable declare - ERROR!!");
 	        	}
 			}
         
 		} catch (org.apache.thrift.TException e1) {
 		}
 		return null;
 	}
 
 	public Value evaluate(Context cx, VariableBindingNode node) {
 		if (node.variable != null)
         {
             node.variable.evaluate(cx, this);
         }
         
 		
 		if (node.initializer != null) {
 			try {
 				thrift_cli.startExpressionList();
 
 				node.initializer.evaluate(cx, this);
 
 				thrift_cli.endExpressionList();
 			} catch (org.apache.thrift.TException e1) {
 			}
 		}
         
 		return null;
 	}
 
 	public Value evaluate(Context cx, UntypedVariableBindingNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, TypedIdentifierNode node) 
 	{
 	
 		if (node.identifier != null)
         {
             node.identifier.evaluate(cx, this);
         }
 		if (node.type != null)
         {
             node.type.evaluate(cx, this);
         }
 		return null;
 	}
 
     public Value evaluate(Context cx, TypeExpressionNode node)
     {
     	if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}
     	try{
     		
     		thrift_cli.startTypeExpression();
     		
     		if (node.expr != null)
     		{
     			return node.expr.evaluate(cx, this);
     		}
     		else
     			thrift_cli.empty();
     	
     		thrift_cli.endTypeExpression();
     	} catch (org.apache.thrift.TException e1) {
     	}
     	
     	return null;
     }
 
 	public Value evaluate(Context cx, FunctionDefinitionNode node)
 	{
 		if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  
 		try {
 			thrift_cli.startFunctionDefinition( is_abstract_function );
 			
 			if (node.attrs != null) {
 				StringListValue sv = (StringListValue)(node.attrs.evaluate(cx, this));
 				thrift_cli.functionAttribute( sv.values );
 			} else
 				thrift_cli.functionAttribute( new ArrayList<String>() );
 			
 			if (node.name != null) {
 				node.name.evaluate(cx, this);
 			}
 		
 			thrift_cli.startFunctionCommon();
 				if (node.fexpr != null) {
 					node.fexpr.evaluate(cx, this);
 				}
 			thrift_cli.endFunctionCommon();
 			thrift_cli.endFunctionDefinition();
 		} catch (org.apache.thrift.TException e1) {
 
 		}
 		return null;
 	}
 
     public Value evaluate(Context cx, BinaryFunctionDefinitionNode node)
     {
 		if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  
         return null;	
     }
 
 	public Value evaluate(Context cx, FunctionNameNode node)
 	{
 		if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}
 		try {
 
 			String str_fname = "_unknown_";
 			if (node.identifier != null) {
 				
 				Value str_value = node.identifier.evaluate(cx, string_evaluator);
 				str_fname =  Extract2String( str_value );
 			}
 
 			FunctionType func_type = 
 				(node.kind == GET_TOKEN ? FunctionType.TF_GETTER :
 		         node.kind == SET_TOKEN ? FunctionType.TF_SETTER : 
 		        	 FunctionType.TF_NORMAL );
 			thrift_cli.functionName(str_fname, func_type );
 		} catch (org.apache.thrift.TException e1) {
 
 		}
 		return null;
 	}
 
 	public Value evaluate(Context cx, FunctionSignatureNode node) 
 	{
 		if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}
 		try {
 
 			String str_func_type = "";
 			if (node.result != null) {
 				Value v = node.result.evaluate(cx, string_evaluator);
 				str_func_type =  Extract2String( v );
 			} else
 				str_func_type =  "Void";
 			
 			thrift_cli.startFunctionSignature( str_func_type );
 			
 			if (node.parameter != null) {
 				node.parameter.evaluate(cx, this);
 			}
 			if (node.inits != null) {
 				node.inits.evaluate(cx, this);
 			}
 
 			thrift_cli.endFunctionSignature();
 		} catch (org.apache.thrift.TException e1) {
 
 		}
 		return null;
 	}
 
 	public Value evaluate(Context cx, ParameterNode node)
 	{
 		try{
 			String para_name = "";
 			String init_value = "";
 			
 			if (node.identifier != null) {
 				Value v = node.identifier.evaluate(cx, string_evaluator);
 				para_name =  Extract2String( v );
 			}
 			if (node.init != null)
 			{
 				System.out.println( " node has init variable");
 				
 				Value v = node.init.evaluate(cx, string_evaluator);
 				if( node.init instanceof LiteralStringNode)
 					init_value =  "\""+ Extract2String( v ) + "\"";
 				else
 					init_value =  Extract2String( v );
 			}
 			
 			List<String> para_type = new ArrayList<String>();
 			if (node.type != null)
 			{
 				Value v = node.type.evaluate(cx, string_evaluator);
 				para_type =  Extract2StringList( v );
 			} 
 				
 //			if( node.init == null)
 //				thrift_cli.startFunctionSignatureParameterMember( para_name, para_type );
 //			else
 				thrift_cli.startFunctionSignatureParameterMember( para_name, para_type, node.init != null, init_value );
 			thrift_cli.endFunctionSignatureParameterMember();
 		} catch (org.apache.thrift.TException e1) {
 		}
 		return null;
 	}
 
 	public Value evaluate(Context cx, ParameterListNode node) 
 	{
 		try{
 			thrift_cli.startFunctionSignatureParameters();
 	        for (int i = 0, size = node.items.size(); i < size; i++)
 	        {
 	            ParameterNode param = node.items.get(i);
 	
 	            if (param != null)
 	            {
 	                param.evaluate(cx, this);
 	            }
 	        }
         	thrift_cli.endFunctionSignatureParameters();
 		} catch (org.apache.thrift.TException e1) {
 		}
 		return null;
 	}
 
 	public Value evaluate(Context cx, RestExpressionNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, RestParameterNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, InterfaceDefinitionNode node)
 	{
 		if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}
 		try {
 			ClassDefinition class_define = new ClassDefinition();
 			class_define.has_attr = (node.attrs != null);
 			class_define.has_baseclass = (node.baseclass != null);
 			class_define.has_interface = (node.interfaces != null);
 			class_define.has_stmt = (node.statements != null);
 			class_define.object_type = ObjectType.TYPE_INTERFACE;
 			
 
 //			if (node.attrs != null) 
 //				node.attrs.evaluate(cx, this);
 			
 			String s_classname = "";
 			if (node.name != null) {
 				Value v = node.name.evaluate(cx, string_evaluator );
 				s_classname = Extract2String( v );
 			}
 
 			List<String>	sl_inherits = null;
 			if (node.baseclass != null) {
 				Value v = node.baseclass.evaluate(cx, string_evaluator);
 				if( v instanceof StringListValue )
 				{
 					StringListValue sv = (StringListValue)( v );
 					sl_inherits = sv.values;
 				}				
 			} else {
 				sl_inherits = new ArrayList<String>();
 			}
 			
 			List<String>	sl_interfaces  = null;
 			if (node.interfaces != null) {
 				Value v = node.interfaces.evaluate(cx, string_evaluator);
 				if( v instanceof StringListValue )
 				{
 					StringListValue sv = (StringListValue)( v );
 					sl_interfaces = sv.values;
 				}				
 			} else {
 				sl_interfaces = new ArrayList<String>();
 			}
 			
 			class_define.name = s_classname;
 			class_define.inherits = sl_inherits;
 			class_define.interfaces = sl_interfaces;
 			thrift_cli.startClassDefinition( class_define );
 			
 				is_abstract_function = true;
 				if (node.statements != null) {
 					thrift_cli.startClassStmt();
 					node.statements.evaluate(cx, this);
 					thrift_cli.endClassStmt();
 				}
 				is_abstract_function = false;
 			
 			thrift_cli.endClassDefinition( );
 
 		} catch (org.apache.thrift.TException e1) {
 		}
 		if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}
 		return null;
 
 	}
 
 	public Value evaluate(Context cx, ClassDefinitionNode node) 
 	{
 		if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}
 		try {
 			ClassDefinition class_define = new ClassDefinition();
 			class_define.has_attr = (node.attrs != null);
 			class_define.has_baseclass = (node.baseclass != null);
 			class_define.has_interface = (node.interfaces != null);
 			class_define.has_stmt = (node.statements != null);
 			class_define.object_type = ObjectType.TYPE_CLASS;
 
 			if (node.attrs != null) {
 				Value v = node.attrs.evaluate(cx, this);
 				class_define.attributes = Extract2StringList( v );
 			}
 			
 			String s_classname = "";
 			
 			if (node.name != null) {
 				Value v = node.name.evaluate(cx, string_evaluator );
 				s_classname = Extract2String( v );
 			}
 
 			List<String>	sl_inherits = null;
 			if (node.baseclass != null) {
 				Value v = node.baseclass.evaluate(cx, string_evaluator);
 				if( v instanceof StringListValue )
 				{
 					StringListValue sv = (StringListValue)( v );
 					sl_inherits = sv.values;
 				}				
 			} else {
 				sl_inherits = new ArrayList<String>();
 			}
 			
 			List<String>	sl_interfaces  = null;
 			if (node.interfaces != null) {
 				Value v = node.interfaces.evaluate(cx, string_evaluator);
 				if( v instanceof StringListValue )
 				{
 					StringListValue sv = (StringListValue)( v );
 					sl_interfaces = sv.values;
 				}				
 			} else {
 				sl_interfaces = new ArrayList<String>();
 			}
 			
 			class_define.name = s_classname;
 			class_define.inherits = sl_inherits;
 			class_define.interfaces = sl_interfaces;
 			
 			boolean do_pkgdef = false;
 			if( node.pkgdef != null && node.pkgdef.pkg_inside_dirty == false) {
 				node.pkgdef.evaluate(cx, this);
 				do_pkgdef = true;
 			}
 			
 			thrift_cli.startClassDefinition( class_define );
 			
 				if (node.statements != null) {
 					thrift_cli.startClassStmt();
 					node.statements.evaluate(cx, this);
 					thrift_cli.endClassStmt();
 				}
 			
 			thrift_cli.endClassDefinition( );
 			
 			if( node.pkgdef != null && do_pkgdef == true) {
 				node.pkgdef.evaluate(cx, this);
 			}
 
 		} catch (org.apache.thrift.TException e1) {
 		}
 		return null;
 	}
 
     public Value evaluate(Context cx, BinaryClassDefNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
     public Value evaluate(Context cx, BinaryInterfaceDefinitionNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, ClassNameNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, InheritanceNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, NamespaceDefinitionNode node)
 	{
 		if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  
 		return null;
 	}
 
 	public Value evaluate(Context cx, ConfigNamespaceDefinitionNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, PackageDefinitionNode node)
 	{
 		try {
 			List<String> pkg_name_list = new ArrayList<String>();
 			if( node.name != null )
 			{
 		        for (IdentifierNode id : node.name.id.list)
 		        {
 		            pkg_name_list.add( id.name );
 		        }
 			}
 			
 			if( node.flipDirty() )
 				thrift_cli.endPackage( pkg_name_list );
 			else
 				thrift_cli.startPackage( pkg_name_list );
         
 		} catch( org.apache.thrift.TException e1 ) {
 		}
 		
 		return null;
 	}
 
 	public Value evaluate(Context cx, PackageIdentifiersNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, PackageNameNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, ProgramNode node)
 	{
 		try {
 			thrift_cli.startProgram( Constants.PROTO_VERSION, Constants.PROTO_COUNTER );
 		    	if (node.statements != null)
 		    	{
 		    		node.statements.evaluate(cx, this);
 		    	}
 			thrift_cli.endProgram();
 		} catch( org.apache.thrift.TException e1 ) {
 		}
 		return null;
 	}
 
     public Value evaluate(Context cx, BinaryProgramNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, ErrorNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, ToObjectNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, LoadRegisterNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, StoreRegisterNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
     public Value evaluate(Context cx, RegisterNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, HasNextNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
     public Value evaluate(Context cx, BoxNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, CoerceNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, PragmaNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
     public Value evaluate(Context cx, UsePrecisionNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, UseNumericNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
 	public Value evaluate(Context cx, UseRoundingNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;} 
 
     public Value evaluate(Context cx, PragmaExpressionNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
     public Value evaluate(Context cx, DefaultXMLNamespaceNode node){if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}  return null;}
 
     
     private String Extract2String( Value v )
     {
 		if( v!=null && v instanceof StringValue ) {
 			StringValue sv = (StringValue)(v);
 			return sv.getValue();
 		} else if( v!=null && v instanceof StringListValue ) {
 			StringListValue sv = (StringListValue)(v);
 			return sv.values.size() > 0 ? sv.values.get(0) : "";
 		} else if( v instanceof QNValue)   {
 			QNValue qual_value = (QNValue)(v);
 			return qual_value.getName();
 		} else
 			return "";
     }
 	
     private List<String> Extract2StringList( Value v )
     {
 		if( v!=null && v instanceof StringValue ) {
 			if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}
 			StringValue sv = (StringValue)(v);
 			List<String> string_list = new ArrayList<String>();
 			string_list.add( sv.getValue() );
 			return string_list;
 		} else if( v!=null && v instanceof StringListValue ) {
 			if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}
 			StringListValue sv = (StringListValue)(v);
 			List<String> answer = new ArrayList<String>();
 			for( int idx=0; idx < sv.values.size() ; idx ++ ) 
 			{
 				List<String> tokens = SplitQualifiedName( sv.values.get(idx) );
 				for( int tIdx = 0 ; tIdx < tokens.size() ; tIdx ++)
 					answer.add( tokens.get(tIdx) );
 			}
 			return answer;
 		} else if( v instanceof QNValue)   {
 			if(DEBUG){System.out.println((new Throwable()).getStackTrace()[0].toString());}
 			QNValue qual_value = (QNValue)(v);
 			
 			List<String> string_list = new ArrayList<String>();
 			for( int idx=0; idx < qual_value.getQualifier().size() ; idx ++ ) 
 			{
 				List<String> tokens = SplitQualifiedName( qual_value.getQualifier().get(idx) );
 				for( int tIdx = 0 ; tIdx < tokens.size() ; tIdx ++)
 					string_list.add( tokens.get(tIdx) );
 			}
 			string_list.add( qual_value.getName() );
 
 			
 			return string_list;
 		} 
 		
 		return null;	
     }
     
     private List<String> SplitQualifiedName( String token )
     {
     	String[] splited =  token.split("\\.");
     	List<String> answer = new ArrayList<String>();
     	for( int idx = 0 ; idx < splited.length ; idx ++ )
     		answer . add( splited[idx]) ;
     	return answer;
     }
 //    private boolean dont_throw_thrift = false;
 }
 
