 /*******************************************************************************
  * Copyright (c) 2004, 2005 Actuate Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *  Actuate Corporation  - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.birt.core.data;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.birt.core.exception.BirtException;
 import org.eclipse.birt.core.exception.CoreException;
 import org.eclipse.birt.core.i18n.ResourceConstants;
 import org.mozilla.javascript.CompilerEnvirons;
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.FunctionNode;
 import org.mozilla.javascript.Node;
 import org.mozilla.javascript.Parser;
 import org.mozilla.javascript.ScriptOrFnNode;
 import org.mozilla.javascript.Token;
 
 /**
  * This utility class is to compile expression to get a list of column
  * expression. The returned column expression is marked as dataSetRow["name"] or
  * dataSetRow[index]
  */
 class ExpressionParserUtility
 {
 	private final String pluginId = "org.eclipse.birt.core";
 	private final String ROW_INDICATOR = "row";
 	private final String DATASETROW_INDICATOR = "dataSetRow";
 	
 	private static ExpressionParserUtility instance = new ExpressionParserUtility( );
 	
 	/**
 	 * compile the expression
 	 * 
 	 * @param expression
 	 * @return List contains all column reference
 	 * @throws BirtException
 	 */
 	static List compileColumnExpression( String expression )
 			throws BirtException
 	{
 		if ( expression == null || expression.trim( ).length( ) == 0 )
 			return null;
 		List columnExprList = new ArrayList( );
 		columnExprList.clear( );
 		Context context = Context.enter( );
 		try
 		{
 			ScriptOrFnNode tree = instance.parse( expression, context );
 			instance.CompiledExprFromTree( expression, context, tree, columnExprList );
 		}
 		catch ( Exception ex )
 		{
 			throw new CoreException( instance.pluginId,
 					ResourceConstants.INVALID_EXPRESSION,
 					ex );
 		}
 		finally
 		{
 			Context.exit( );
 		}
 		return columnExprList;
 	}
 
 	/**
 	 * compile the expression from a script tree
 	 * 
 	 * @param expression
 	 * @param context
 	 * @param tree
 	 * @param columnExprList
 	 * @throws BirtException
 	 */
 	private void CompiledExprFromTree( String expression,
 			Context context, ScriptOrFnNode tree, List columnExprList )
 			throws BirtException
 	{
 		if ( tree.getFirstChild( ) == tree.getLastChild( ) )
 		{
 			if ( tree.getFirstChild( ).getType( ) == Token.FUNCTION )
 			{
 				int index = getFunctionIndex( tree.getFirstChild( ).getString( ),
 						tree );
 				compileFunctionNode( tree.getFunctionNode( index ),
 						tree,
 						columnExprList );
 			}
 			else
 			{
 				// A single expression
 				if ( tree.getFirstChild( ).getType( ) != Token.EXPR_RESULT
 						&& tree.getFirstChild( ).getType( ) != Token.EXPR_VOID
 						&& tree.getFirstChild( ).getType( ) != Token.BLOCK )
 				{
 					// This should never happen?
 					throw new CoreException( pluginId,
 							ResourceConstants.INVALID_EXPRESSION );
 				}
 				Node exprNode = tree.getFirstChild( );
 				Node child = exprNode.getFirstChild( );
 				assert ( child != null );
 				processChild( exprNode, child, tree, columnExprList );
 			}
 		}
 		else
 		{
 			compileComplexExpr( tree, tree, columnExprList );
 		}
 	}
 
 	/**
 	 * parse the expression into a script tree
 	 * 
 	 * @param expression
 	 * @param cx
 	 * @return
 	 */
 	private ScriptOrFnNode parse( String expression, Context cx )
 	{
 		CompilerEnvirons compilerEnv = new CompilerEnvirons( );
 		Parser p = new Parser( compilerEnv, cx.getErrorReporter( ) );
 		return p.parse( expression, null, 0 );
 	}
 
 	/**
 	 * process child node
 	 * 
 	 * @param parent
 	 * @param child
 	 * @param tree
 	 * @param columnExprList
 	 * @throws BirtException
 	 */
 	private void processChild( Node parent, Node child,
 			ScriptOrFnNode tree, List columnExprList ) throws BirtException
 	{
 		switch ( child.getType( ) )
 		{
 			case Token.NUMBER :
 			case Token.STRING :
 			case Token.NULL :
 			case Token.TRUE :
 			case Token.FALSE :
 				break;
 
 			case Token.GETPROP :
 			case Token.GETELEM :
 				compileDirectColRefExpr( child, tree, columnExprList );
 				break;
 
 			case Token.CALL :
 				compileAggregateExpr( parent, child, tree, columnExprList );
 				break;
 			default :
 				compileComplexExpr( child, tree, columnExprList );
 		}
 
 	}
 
 	/**
 	 * compile column reference expression
 	 * 
 	 * @param refNode
 	 * @throws BirtException
 	 */
 	private void compileDirectColRefExpr( Node refNode,
 			ScriptOrFnNode tree, List columnExprList ) throws BirtException
 	{
 		assert ( refNode.getType( ) == Token.GETPROP || refNode.getType( ) == Token.GETELEM );
 
 		Node rowName = refNode.getFirstChild( );
 		assert ( rowName != null );
 		if ( rowName.getType( ) != Token.NAME )
 		{
 			compileComplexExpr( refNode, tree, columnExprList );
 		}
 
 		String str = rowName.getString( );
 		assert ( str != null );
 		if ( !str.equals( ROW_INDICATOR ) )
 			return;
 
 		Node rowColumn = rowName.getNext( );
 		assert ( rowColumn != null );
 
 		if ( refNode.getType( ) == Token.GETPROP
 				&& rowColumn.getType( ) == Token.STRING )
 		{
 			ColumnBinding binding = new ColumnBinding( rowColumn.getString( ),
 					DATASETROW_INDICATOR
 							+ "[\"" + rowColumn.getString( ) + "\"]" );
 			columnExprList.add( binding );
 		}
 
 		if ( refNode.getType( ) == Token.GETELEM )
 		{
 			if ( rowColumn.getType( ) == Token.NUMBER )
 			{
 //				columnExprList.add( DATASETROW_INDICATOR
 //						+ "[" + (int) rowColumn.getDouble( ) + "]" );
 			}
 			else if ( rowColumn.getType( ) == Token.STRING )
 			{
 				ColumnBinding binding = new ColumnBinding( rowColumn.getString( ),
 						DATASETROW_INDICATOR
 								+ "[\"" + rowColumn.getString( ) + "\"]" );
 				columnExprList.add( binding );
 			}
 		}
 	}
 
 	/**
 	 * compile aggregate expression
 	 * 
 	 * @param context
 	 * @param parent
 	 * @param callNode
 	 * @throws BirtException
 	 */
 	private void compileAggregateExpr( Node parent, Node callNode,
 			ScriptOrFnNode tree, List columnExprList ) throws BirtException
 	{
 		assert ( callNode.getType( ) == Token.CALL );
 		compileAggregationFunction( callNode, tree, columnExprList );
 		extractArguments( callNode, tree, columnExprList );
 	}
 
 	/**
 	 * 
 	 * @param callNode
 	 * @param tree
 	 * @param columnExprList
 	 * @throws BirtException
 	 */
 	private void compileAggregationFunction( Node callNode,
 			ScriptOrFnNode tree, List columnExprList ) throws BirtException
 	{
 		Node firstChild = callNode.getFirstChild( );
 		compileComplexExpr( firstChild, tree, columnExprList );
 	}
 
 	/**
 	 * extract arguments from aggregation expression
 	 * 
 	 * @param context
 	 * @param callNode
 	 * @throws BirtException
 	 */
 	private void extractArguments( Node callNode, ScriptOrFnNode tree,
 			List columnExprList ) throws BirtException
 	{
 		Node arg = callNode.getFirstChild( ).getNext( );
 
 		while ( arg != null )
 		{
 			// need to hold on to the next argument because the tree extraction
 			// will cause us to lose the reference otherwise
 			Node nextArg = arg.getNext( );
 			processChild( callNode, arg, tree, columnExprList );
 
 			arg = nextArg;
 		}
 	}
 
 	/**
 	 * compile the complex expression
 	 * 
 	 * @param complexNode
 	 * @throws BirtException
 	 */
 	private void compileComplexExpr( Node complexNode,
 			ScriptOrFnNode tree, List columnExprList ) throws BirtException
 	{
 		Node child = complexNode.getFirstChild( );
 
 		while ( child != null )
 		{
 			if ( child.getType( ) == Token.FUNCTION )
 			{
 				int index = getFunctionIndex( child.getString( ), tree );
 				compileFunctionNode( tree.getFunctionNode( index ),
 						tree,
 						columnExprList );
 			}
 			// keep reference to next child, since subsequent steps could lose
 			// the reference to it
 			Node nextChild = child.getNext( );
 
 			// do not include constants into the sub-expression list
 			if ( child.getType( ) == Token.NUMBER
 					|| child.getType( ) == Token.STRING
 					|| child.getType( ) == Token.TRUE
 					|| child.getType( ) == Token.FALSE
 					|| child.getType( ) == Token.NULL )
 			{
 				processChild( complexNode, child, tree, columnExprList );
 				child = nextChild;
 				continue;
 			}
 
 			processChild( complexNode, child, tree, columnExprList );
 			child = nextChild;
 		}
 	}
 
 	/**
 	 * compile the function expression
 	 * 
 	 * @param node
 	 * @param tree
 	 * @param columnExprList
 	 * @throws BirtException
 	 */
 	private void compileFunctionNode( FunctionNode node,
 			ScriptOrFnNode tree, List columnExprList ) throws BirtException
 	{
 		compileComplexExpr( node, tree, columnExprList );
 	}
 
 	/**
 	 * get the function node index
 	 * 
 	 * @param functionName
 	 * @param tree
 	 * @return
 	 */
 	private int getFunctionIndex( String functionName,
 			ScriptOrFnNode tree )
 	{
 		int index = -1;
 		for ( int i = 0; i < tree.getFunctionCount( ); i++ )
 		{
 			if ( tree.getFunctionNode( i )
 					.getFunctionName( )
 					.equals( functionName ) )
 			{
 				index = i;
 				break;
 			}
 		}
 		return index;
 	}
 }
