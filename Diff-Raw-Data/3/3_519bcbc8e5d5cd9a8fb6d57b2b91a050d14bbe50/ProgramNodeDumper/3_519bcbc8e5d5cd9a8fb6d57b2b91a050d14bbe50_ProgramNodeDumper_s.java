 package tw.maple;
 
 
 import java.io.PrintWriter;
 import java.util.List;
 import java.util.ArrayList;
 
 import macromedia.asc.parser.*;
 import macromedia.asc.semantics.Value;
 import macromedia.asc.util.Context;
 //import sun.org.mozilla.javascript.internal.EvaluatorException;
 import tw.maple.generated.*;
 import static macromedia.asc.parser.Tokens.*;
 
 public final class ProgramNodeDumper implements Evaluator 
 {
 	AstDumper.Client thrift_cli;
     public ProgramNodeDumper(AstDumper.Client cli)
     {
     	thrift_cli = cli;
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
 		return null;
 	}
 
 	public Value evaluate(Context cx, DeleteExpressionNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, IdentifierNode node)
 	{
         if(node instanceof TypeIdentifierNode)
         {
         }
         else if (node.isAttr())
         {
         }
         
         else
         {
         	try
         	{
         		Identifier id = new Identifier();
         		id.name = node.name;
         		thrift_cli.identifierExpression( id );
         	}
         	catch (org.apache.thrift.TException e1) 
     		{
     			
     		}
         }
         
 		return null;
 	}
 
 	public Value evaluate(Context cx, InvokeNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, ThisExpressionNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, QualifiedIdentifierNode node) 
 	{
 		if (node.qualifier != null)
         {
             node.qualifier.evaluate(cx, this);
         }
 		
 		try 
 		{
 //			tw.maple.generated.LiteralString str = new tw.maple.generated.LiteralString();
 //			str.value = node.name;
 //			thrift_cli.identifierExpression( str );
 			
     		Identifier id = new Identifier();
     		id.name = node.name;
     		thrift_cli.identifierExpression( id );
 		}
 		catch (org.apache.thrift.TException e1) 
 		{
 			
 		}
 //        node.qualifier.evaluate(cx, this);
 		return null;
 	}
 
     public Value evaluate(Context cx, QualifiedExpressionNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
     public Value evaluate(Context cx, LiteralBooleanNode node)
     {
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
 		try 
 		{
 //			tw.maple.generated.Identifier id = new tw.maple.generated.Identifier( );
 //			id.name = node.value;
 //			thrift_cli.identifierExpression( id );
 			
 			tw.maple.generated.Literal str = new tw.maple.generated.Literal();
 			str.value = node.value;
 			thrift_cli.literalStringExpression( str );
 		}
 		catch (org.apache.thrift.TException e1) 
 		{
 			
 		}
 		
 		return null;
 	}
 
 	public Value evaluate(Context cx, LiteralNullNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, LiteralRegExpNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, LiteralXMLNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, FunctionCommonNode node)
 	{
 //		System.out.println((new Throwable()).getStackTrace()[0].toString());
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
 
 	public Value evaluate(Context cx, ParenExpressionNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, ParenListExpressionNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, LiteralObjectNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, LiteralFieldNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, LiteralArrayNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 	
 	public Value evaluate(Context cx, LiteralVectorNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, SuperExpressionNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, SuperStatementNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, MemberExpressionNode node)
 	{
         if (node.base != null)
         {
             node.base.evaluate(cx, this);
         }
 
         if (node.selector != null)
         {
             node.selector.evaluate(cx, this);
         }  
 		return null;
 	}
 
 	public Value evaluate(Context cx, CallExpressionNode node)
 	{
 		CallExpression call_expression;
 		
 		try {
 			call_expression = new CallExpression();
 			call_expression.is_new = node.is_new;
 			call_expression.mode = (node.getMode() == LEFTBRACKET_TOKEN ? " bracket" :
 	            	node.getMode() == LEFTPAREN_TOKEN ? " filter" :
 	                node.getMode() == DOUBLEDOT_TOKEN ? " descend" :
 	                node.getMode() == EMPTY_TOKEN ? " lexical" : " dot");
 			thrift_cli.startCallExpression(call_expression);
 			
 			if (node.expr != null) {
 				// Callee
 				node.expr.evaluate(cx, this);
 			}
 
 			if (node.args != null) {
 				thrift_cli.startAgumentList();
 					node.args.evaluate(cx, this);
 				thrift_cli.endAgumentList();
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
 //		System.out.println((new Throwable()).getStackTrace()[0].toString());
 		if (node.expr != null)
         {
             node.expr.evaluate(cx, this);
         }
 		return null;
 	}
 
 	public Value evaluate(Context cx, SetExpressionNode node) 
 	{
 		try {
 			thrift_cli.startAssignment();
 			thrift_cli.startExpressionList();
 			if (node.expr != null) 
 				node.expr.evaluate(cx, this);
 			thrift_cli.endExpressionList();
 			thrift_cli.startExpressionList();
 			if (node.args != null) 
 				node.args.evaluate(cx, this);
 			thrift_cli.endExpressionList();
 			thrift_cli.endAssignment();
 		} catch (org.apache.thrift.TException e1) {
 		}
 		return null;
 	}
 
     public Value evaluate(Context cx, ApplyTypeExprNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, UnaryExpressionNode node) 
 	{
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
 		System.out.println((new Throwable()).getStackTrace()[0].toString());
 		return null;
 	}
 
 	public Value evaluate(Context cx, ArgumentListNode node)
 	{
         for (Node n : node.items)
         {
             n.evaluate(cx, this);
         }
 		return null;
 	}
 
 	public Value evaluate(Context cx, ListNode node)
 	{
 	
 		for ( Node n : node.items )
 		{
 			n.evaluate( cx, this );
 		}
 		return null;
 	}
 
 	// Statements
 
 	public Value evaluate(Context cx, StatementListNode node)
 	{
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
 
 	public Value evaluate(Context cx, EmptyElementNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, EmptyStatementNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
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
 
 	public Value evaluate(Context cx, LabeledStatementNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, IfStatementNode node) 
 	{
 		try { thrift_cli.startIfStatement();
 		} catch (org.apache.thrift.TException e1) { }
 		
 		try { thrift_cli.startIfStatement_Condition();
 		} catch (org.apache.thrift.TException e1) { }
 		if (node.condition != null) {
 			node.condition.evaluate(cx, this);
 		}
 		try { thrift_cli.endIfStatement_Condition();
 		} catch (org.apache.thrift.TException e1) { }
 
 		try { thrift_cli.startIfStatement_Then();
 		} catch (org.apache.thrift.TException e1) { }
 		if (node.thenactions != null)
 			node.thenactions.evaluate(cx, this);
 		try { thrift_cli.endIfStatement_Then();
 		} catch (org.apache.thrift.TException e1) { }
 
 		try { thrift_cli.startIfStatement_Else();
 		} catch (org.apache.thrift.TException e1) { }
 		if (node.elseactions != null)
 			node.elseactions.evaluate(cx, this);
 		try { thrift_cli.endtIfStatement_Else();
 		} catch (org.apache.thrift.TException e1) { }
 		
 		try { thrift_cli.endIfStatement();
 		} catch (org.apache.thrift.TException e1) { }
 		return null;
 	}
 
 	public Value evaluate(Context cx, SwitchStatementNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, CaseLabelNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, DoStatementNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, WhileStatementNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, ForStatementNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, WithStatementNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, ContinueStatementNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, BreakStatementNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
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
 
 	public Value evaluate(Context cx, ThrowStatementNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, TryStatementNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, CatchClauseNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, FinallyClauseNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, UseDirectiveNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, IncludeDirectiveNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, ImportNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, MetaDataNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 	
 	public Value evaluate(Context cx, DocCommentNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	// Definitions
 
 	public Value evaluate(Context cx, ImportDirectiveNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, AttributeListNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
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
 			thrift_cli.startVariableDeclare();
 		
 	        if (node.attrs != null)
 	        {
 	            node.attrs.evaluate(cx, this);
 	        }
 	        if (node.list != null)
 	        {
 	            node.list.evaluate(cx, this);
 	        }
         
         
 			thrift_cli.endVariableDeclare();
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
 
 	public Value evaluate(Context cx, UntypedVariableBindingNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
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
     	if (node.expr != null)
         {
             node.expr.evaluate(cx, this);
         }
     	return null;
     }
 
 	public Value evaluate(Context cx, FunctionDefinitionNode node)
 	{
 //		System.out.println((new Throwable()).getStackTrace()[0].toString());  
 		try {
 			thrift_cli.startFunctionDefinition();
 			if (node.attrs != null) {
 				node.attrs.evaluate(cx, this);
 			}
 		
 
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
 		System.out.println((new Throwable()).getStackTrace()[0].toString());  
         return null;	
     }
 
 	public Value evaluate(Context cx, FunctionNameNode node)
 	{
 		try {
 			thrift_cli.startFunctionName();
 		} catch (org.apache.thrift.TException e1) {
 
 		}
 		if (node.identifier != null) {
 			node.identifier.evaluate(cx, this);
 		}
 
 		try {
 			thrift_cli.endFunctionName();
 		} catch (org.apache.thrift.TException e1) {
 
 		}
 		return null;
 	}
 
 	public Value evaluate(Context cx, FunctionSignatureNode node) 
 	{
 //		System.out.println((new Throwable()).getStackTrace()[0].toString());
 		try {
 
 			thrift_cli.startFunctionSignature();
 
 			thrift_cli.startFunctionSignatureReturnType();
 			if (node.result != null) {
 				node.result.evaluate(cx, this);
 			}
 			thrift_cli.endFunctionSignatureReturnType();
 			
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
 			thrift_cli.startFunctionSignatureParameterMember();
 		} catch (org.apache.thrift.TException e1) {
 		}
 		
 		if (node.identifier != null)
         {
             node.identifier.evaluate(cx, this);
         }
         if (node.init != null)
         {
             node.init.evaluate(cx, this);
         }
         if (node.type != null)
         {
             node.type.evaluate(cx, this);
         }
         try{
 			thrift_cli.endFunctionSignatureParameterMember();
 		} catch (org.apache.thrift.TException e1) {
 		}
 		return null;
 	}
 
 	public Value evaluate(Context cx, ParameterListNode node) 
 	{
 		try{
 			thrift_cli.startFunctionSignatureParameters();
 		} catch (org.apache.thrift.TException e1) {
 		}
         for (int i = 0, size = node.items.size(); i < size; i++)
         {
             ParameterNode param = node.items.get(i);
 
             if (param != null)
             {
                 param.evaluate(cx, this);
             }
         }
         try{
 			thrift_cli.endFunctionSignatureParameters();
 		} catch (org.apache.thrift.TException e1) {
 		}
 		return null;
 	}
 
 	public Value evaluate(Context cx, RestExpressionNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, RestParameterNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, InterfaceDefinitionNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, ClassDefinitionNode node) 
 	{
 	
 		try {
 			ClassDefine class_define = new ClassDefine();
 			class_define.has_attr = (node.attrs != null);
 			class_define.has_baseclass = (node.baseclass != null);
 			class_define.has_interface = (node.interfaces != null);
 			class_define.has_stmt = (node.statements != null);
 			
 			thrift_cli.startClassDefine( class_define );
 
 			if (node.attrs != null) {
 				node.attrs.evaluate(cx, this);
 			}
 			if (node.name != null) {
 				thrift_cli.startClassName();
 				node.name.evaluate(cx, this);
 				thrift_cli.endClassName();
 			}
 
 			if (node.baseclass != null) {
 				thrift_cli.startClassBase();
 				node.baseclass.evaluate(cx, this);
 				thrift_cli.endClassBase();
 			}
 			if (node.interfaces != null) {
 				thrift_cli.startClassInterface();
 				node.interfaces.evaluate(cx, this);
 				thrift_cli.endClassInterface();
 			}
 			if (node.statements != null) {
 				thrift_cli.startClassStmt();
 				node.statements.evaluate(cx, this);
 				thrift_cli.endClassStmt();
 			}
 		} catch (org.apache.thrift.TException e1) {
 		}
 		return null;
 	}
 
     public Value evaluate(Context cx, BinaryClassDefNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
     public Value evaluate(Context cx, BinaryInterfaceDefinitionNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, ClassNameNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, InheritanceNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, NamespaceDefinitionNode node)
 	{
 		System.out.println((new Throwable()).getStackTrace()[0].toString());  
 		return null;
 	}
 
 	public Value evaluate(Context cx, ConfigNamespaceDefinitionNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
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
 			
 			thrift_cli.startPackage( pkg_name_list );
 			
 		} catch( org.apache.thrift.TException e1 ) {
 			System.out.print("\nERROR - "+e1.toString());
 			System.exit(1);
 		}
 		
 		return null;
 	}
 
 	public Value evaluate(Context cx, PackageIdentifiersNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, PackageNameNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, ProgramNode node)
 	{
 		try {
 			thrift_cli.startProgram();
 		    	if (node.statements != null)
 		    	{
 		    		node.statements.evaluate(cx, this);
 		    	}
 			thrift_cli.endProgram();
 		} catch( org.apache.thrift.TException e1 ) {
 			System.out.print("\nERROR - "+e1.toString());
 			System.exit(1);
 		}
 		return null;
 	}
 
     public Value evaluate(Context cx, BinaryProgramNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, ErrorNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, ToObjectNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, LoadRegisterNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, StoreRegisterNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
     public Value evaluate(Context cx, RegisterNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, HasNextNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
     public Value evaluate(Context cx, BoxNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, CoerceNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, PragmaNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
     public Value evaluate(Context cx, UsePrecisionNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, UseNumericNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	public Value evaluate(Context cx, UseRoundingNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;} 
 
     public Value evaluate(Context cx, PragmaExpressionNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
     public Value evaluate(Context cx, DefaultXMLNamespaceNode node){System.out.println((new Throwable()).getStackTrace()[0].toString());  return null;}
 
 	
 }
 
