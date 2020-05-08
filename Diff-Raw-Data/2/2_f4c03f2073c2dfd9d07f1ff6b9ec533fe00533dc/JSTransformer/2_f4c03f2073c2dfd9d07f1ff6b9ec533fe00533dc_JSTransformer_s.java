 /*******************************************************************************
  * Copyright (c) 2009 xored software, Inc.  
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html  
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Vladimir Belov)
  *******************************************************************************/
 package org.eclipse.dltk.javascript.parser;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.Stack;
 
 import org.antlr.runtime.RuleReturnScope;
 import org.antlr.runtime.Token;
 import org.antlr.runtime.tree.Tree;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.AssertionFailedException;
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.compiler.util.Util;
 import org.eclipse.dltk.javascript.ast.Argument;
 import org.eclipse.dltk.javascript.ast.ArrayInitializer;
 import org.eclipse.dltk.javascript.ast.AsteriskExpression;
 import org.eclipse.dltk.javascript.ast.BinaryOperation;
 import org.eclipse.dltk.javascript.ast.BooleanLiteral;
 import org.eclipse.dltk.javascript.ast.BreakStatement;
 import org.eclipse.dltk.javascript.ast.CallExpression;
 import org.eclipse.dltk.javascript.ast.CaseClause;
 import org.eclipse.dltk.javascript.ast.CatchClause;
 import org.eclipse.dltk.javascript.ast.CommaExpression;
 import org.eclipse.dltk.javascript.ast.Comment;
 import org.eclipse.dltk.javascript.ast.ConditionalOperator;
 import org.eclipse.dltk.javascript.ast.ConstStatement;
 import org.eclipse.dltk.javascript.ast.ContinueStatement;
 import org.eclipse.dltk.javascript.ast.DecimalLiteral;
 import org.eclipse.dltk.javascript.ast.DefaultClause;
 import org.eclipse.dltk.javascript.ast.DefaultXmlNamespaceStatement;
 import org.eclipse.dltk.javascript.ast.DeleteStatement;
 import org.eclipse.dltk.javascript.ast.DoWhileStatement;
 import org.eclipse.dltk.javascript.ast.EmptyExpression;
 import org.eclipse.dltk.javascript.ast.EmptyStatement;
 import org.eclipse.dltk.javascript.ast.ErrorExpression;
 import org.eclipse.dltk.javascript.ast.Expression;
 import org.eclipse.dltk.javascript.ast.FinallyClause;
 import org.eclipse.dltk.javascript.ast.ForEachInStatement;
 import org.eclipse.dltk.javascript.ast.ForInStatement;
 import org.eclipse.dltk.javascript.ast.ForStatement;
 import org.eclipse.dltk.javascript.ast.FunctionStatement;
 import org.eclipse.dltk.javascript.ast.GetAllChildrenExpression;
 import org.eclipse.dltk.javascript.ast.GetArrayItemExpression;
 import org.eclipse.dltk.javascript.ast.GetLocalNameExpression;
 import org.eclipse.dltk.javascript.ast.GetMethod;
 import org.eclipse.dltk.javascript.ast.IVariableStatement;
 import org.eclipse.dltk.javascript.ast.Identifier;
 import org.eclipse.dltk.javascript.ast.IfStatement;
 import org.eclipse.dltk.javascript.ast.Keyword;
 import org.eclipse.dltk.javascript.ast.Keywords;
 import org.eclipse.dltk.javascript.ast.Label;
 import org.eclipse.dltk.javascript.ast.LabelledStatement;
 import org.eclipse.dltk.javascript.ast.LoopStatement;
 import org.eclipse.dltk.javascript.ast.Method;
 import org.eclipse.dltk.javascript.ast.MissingType;
 import org.eclipse.dltk.javascript.ast.MultiLineComment;
 import org.eclipse.dltk.javascript.ast.NewExpression;
 import org.eclipse.dltk.javascript.ast.NullExpression;
 import org.eclipse.dltk.javascript.ast.ObjectInitializer;
 import org.eclipse.dltk.javascript.ast.ObjectInitializerPart;
 import org.eclipse.dltk.javascript.ast.ParenthesizedExpression;
 import org.eclipse.dltk.javascript.ast.PropertyExpression;
 import org.eclipse.dltk.javascript.ast.PropertyInitializer;
 import org.eclipse.dltk.javascript.ast.RegExpLiteral;
 import org.eclipse.dltk.javascript.ast.ReturnStatement;
 import org.eclipse.dltk.javascript.ast.Script;
 import org.eclipse.dltk.javascript.ast.SetMethod;
 import org.eclipse.dltk.javascript.ast.SimpleType;
 import org.eclipse.dltk.javascript.ast.SingleLineComment;
 import org.eclipse.dltk.javascript.ast.Statement;
 import org.eclipse.dltk.javascript.ast.StatementBlock;
 import org.eclipse.dltk.javascript.ast.StringLiteral;
 import org.eclipse.dltk.javascript.ast.SwitchComponent;
 import org.eclipse.dltk.javascript.ast.SwitchStatement;
 import org.eclipse.dltk.javascript.ast.ThisExpression;
 import org.eclipse.dltk.javascript.ast.ThrowStatement;
 import org.eclipse.dltk.javascript.ast.TryStatement;
 import org.eclipse.dltk.javascript.ast.Type;
 import org.eclipse.dltk.javascript.ast.TypeOfExpression;
 import org.eclipse.dltk.javascript.ast.UnaryOperation;
 import org.eclipse.dltk.javascript.ast.VariableDeclaration;
 import org.eclipse.dltk.javascript.ast.VariableStatement;
 import org.eclipse.dltk.javascript.ast.VoidExpression;
 import org.eclipse.dltk.javascript.ast.VoidOperator;
 import org.eclipse.dltk.javascript.ast.WhileStatement;
 import org.eclipse.dltk.javascript.ast.WithStatement;
 import org.eclipse.dltk.javascript.ast.XmlAttributeIdentifier;
 import org.eclipse.dltk.javascript.ast.XmlExpressionFragment;
 import org.eclipse.dltk.javascript.ast.XmlFragment;
 import org.eclipse.dltk.javascript.ast.XmlLiteral;
 import org.eclipse.dltk.javascript.ast.XmlTextFragment;
 import org.eclipse.dltk.javascript.ast.YieldOperator;
 import org.eclipse.dltk.javascript.internal.parser.ArgumentTypedDeclaration;
 import org.eclipse.dltk.javascript.internal.parser.FunctionTypedDeclaration;
 import org.eclipse.dltk.javascript.internal.parser.ITypedDeclaration;
 import org.eclipse.dltk.javascript.internal.parser.VariableTypedDeclaration;
 import org.eclipse.dltk.javascript.parser.Reporter.Severity;
 import org.eclipse.dltk.utils.IntList;
 import org.eclipse.osgi.util.NLS;
 
 public class JSTransformer extends JSVisitor<ASTNode> {
 
 	private final List<Token> tokens;
 	private final int[] tokenOffsets;
 	private Stack<ASTNode> parents = new Stack<ASTNode>();
 	private final boolean ignoreUnknown;
 	private final Map<Integer, Comment> documentationMap = new HashMap<Integer, Comment>();
 	private Reporter reporter;
 	private SymbolTable scope = new SymbolTable();
 
 	private static final int MAX_RECURSION_DEEP = 512;
 
 	private void checkRecursionDeep() {
 		if (parents.size() > MAX_RECURSION_DEEP)
 			throw new IllegalArgumentException("Too many AST deep");
 	}
 
 	public JSTransformer(List<Token> tokens) {
 		this(tokens, false);
 	}
 
 	public JSTransformer(List<Token> tokens, boolean ignoreUnknown) {
 		Assert.isNotNull(tokens);
 		this.tokens = tokens;
 		this.ignoreUnknown = ignoreUnknown;
 		tokenOffsets = prepareOffsetMap(tokens);
 	}
 
 	public void setReporter(Reporter reporter) {
 		this.reporter = reporter;
 	}
 
 	public Script transform(RuleReturnScope root) {
 		Assert.isNotNull(root);
 		final Tree tree = (Tree) root.getTree();
 		if (tree == null)
 			return new Script();
 		final Script script = new Script();
 		addComments(script);
 		if (tree.getType() != 0) {
 			script.addStatement(transformStatementNode(tree, script));
 		} else {
 			for (int i = 0; i < tree.getChildCount(); i++) {
 				script.addStatement(transformStatementNode(tree.getChild(i),
 						script));
 			}
 		}
 		script.setStart(0);
 		script.setEnd(tokenOffsets[tokenOffsets.length - 1]);
 		return script;
 	}
 
 	private ASTNode getParent() {
 		if (parents.isEmpty()) {
 			return null;
 		} else {
 			return parents.peek();
 		}
 	}
 
 	private ASTNode transformNode(Tree node, ASTNode parent) {
 		checkRecursionDeep();
 		parents.push(parent);
 		try {
 			checkRecursionDeep();
 			ASTNode result = visitNode(node);
 			Assert.isNotNull(result, node.toString());
 			return result;
 		} catch (AssertionFailedException e) {
 			if (ignoreUnknown) {
 				return createErrorExpression(node);
 			} else {
 				throw e;
 			}
 		} finally {
 			parents.pop();
 		}
 	}
 
 	private static int[] prepareOffsetMap(List<Token> tokens) {
 		final int[] offsets = new int[tokens.size() + 1];
 		int offset = 0;
 		for (int i = 0; i < tokens.size(); i++) {
 			offsets[i] = offset;
 			offset += tokens.get(i).getText().length();
 		}
 		offsets[tokens.size()] = offset;
 		return offsets;
 	}
 
 	private int getTokenOffset(int tokenIndex) {
 		Assert.isTrue(tokenOffsets != null);
 		Assert.isTrue(tokenIndex >= -1 && tokenIndex < tokenOffsets.length);
 
 		return tokenOffsets[tokenIndex];
 	}
 
 	private void setRangeByToken(ASTNode node, int tokenIndex) {
 		node.setStart(getTokenOffset(tokenIndex));
 		node.setEnd(getTokenOffset(tokenIndex + 1));
 	}
 
 	private int getTokenOffset(int tokenType, int startTokenIndex,
 			int endTokenIndex) {
 
 		Assert.isTrue(startTokenIndex >= 0);
 		Assert.isTrue(endTokenIndex > 0);
 		Assert.isTrue(startTokenIndex <= endTokenIndex);
 
 		Token token = null;
 
 		for (int i = startTokenIndex; i <= endTokenIndex; i++) {
 			Token item = tokens.get(i);
 			if (item.getType() == tokenType) {
 				token = item;
 				break;
 			}
 		}
 
 		if (token == null)
 			return -1;
 		else
 			return getTokenOffset(token.getTokenIndex());
 	}
 
 	private int getTokenOffset(int tokenType, int startTokenIndex,
 			int endTokenIndex, int skipCount) {
 
 		Assert.isTrue(startTokenIndex >= 0);
 		Assert.isTrue(endTokenIndex > 0);
 		Assert.isTrue(startTokenIndex <= endTokenIndex);
 
 		Token token = null;
 
 		int skipped = 0;
 
 		for (int i = startTokenIndex; i <= endTokenIndex; i++) {
 			Token item = tokens.get(i);
 			if (item.getType() == tokenType) {
 				if (skipped == skipCount) {
 					token = item;
 					break;
 				} else {
 					skipped++;
 				}
 			}
 		}
 
 		if (token == null)
 			return -1;
 		else
 			return getTokenOffset(token.getTokenIndex());
 
 	}
 
 	private Statement transformStatementNode(Tree node, ASTNode parent) {
 
 		ASTNode expression = transformNode(node, parent);
 
 		if (expression instanceof Statement)
 			return (Statement) expression;
 		else {
 			VoidExpression voidExpression = new VoidExpression(parent);
 			voidExpression.setExpression((Expression) expression);
 
 			if (node.getTokenStopIndex() >= 0
 					&& node.getTokenStopIndex() < tokens.size()) {
 				final Token token = tokens.get(node.getTokenStopIndex());
 				if (token.getType() == JSParser.SEMIC) {
 					voidExpression.setSemicolonPosition(getTokenOffset(token
 							.getTokenIndex()));
 					voidExpression.getExpression().setEnd(
 							Math.min(voidExpression.getSemicolonPosition(),
 									expression.sourceEnd()));
 				}
 			}
 
 			Assert.isTrue(expression.sourceStart() >= 0);
 			Assert.isTrue(expression.sourceEnd() > 0);
 
 			voidExpression.setStart(expression.sourceStart());
 			voidExpression.setEnd(Math.max(expression.sourceEnd(),
 					voidExpression.getSemicolonPosition() + 1));
 
 			return voidExpression;
 		}
 	}
 
 	@Override
 	protected ASTNode visitUnknown(Tree node) {
 		if (ignoreUnknown) {
 			return createErrorExpression(node);
 		}
 		return super.visitUnknown(node);
 	}
 
 	private ASTNode createErrorExpression(Tree node) {
 		if (node != null) {
 			ErrorExpression error = new ErrorExpression(getParent(), node
 					.getText());
 			error.setStart(getTokenOffset(node.getTokenStartIndex()));
 			error.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 			return error;
 		} else {
 			return new ErrorExpression(getParent(), "");
 		}
 	}
 
 	@Override
 	protected ASTNode visitBinaryOperation(Tree node) {
 		if (node.getType() == JSParser.MUL) {
 			switch (node.getChildCount()) {
 			case 0:
 				return visitAsterisk(node);
 			case 1:
 				// HACK
 				return visit(node.getChild(0));
 			}
 
 		}
 
 		Assert.isNotNull(node.getChild(0));
 		Assert.isNotNull(node.getChild(1));
 
 		BinaryOperation operation = new BinaryOperation(getParent());
 
 		operation.setOperation(node.getType());
 
 		operation.setLeftExpression((Expression) transformNode(
 				node.getChild(0), operation));
 
 		operation.setRightExpression((Expression) transformNode(node
 				.getChild(1), operation));
 
 		operation.setOperationPosition(getTokenOffset(node.getType(),
 				getRealTokenStopIndex(node.getChild(0)) + 1, node.getChild(1)
 						.getTokenStartIndex()));
 
 		Assert.isTrue(operation.getOperationPosition() >= operation
 				.getLeftExpression().sourceEnd());
 		Assert.isTrue(operation.getOperationPosition()
 				+ operation.getOperationText().length() <= operation
 				.getRightExpression().sourceStart());
 
 		operation.setStart(operation.getLeftExpression().sourceStart());
 		operation.setEnd(operation.getRightExpression().sourceEnd());
 
 		return operation;
 	}
 
 	@Override
 	protected ASTNode visitBlock(Tree node) {
 
 		StatementBlock block = new StatementBlock(getParent());
 
 		List<Statement> statements = block.getStatements();
 		for (int i = 0; i < node.getChildCount(); i++) {
 			statements.add(transformStatementNode(node.getChild(i), block));
 		}
 
 		block.setLC(getTokenOffset(JSParser.LBRACE, node.getTokenStartIndex(),
 				node.getTokenStopIndex()));
 
 		block.setRC(getTokenOffset(JSParser.RBRACE, node.getTokenStopIndex(),
 				node.getTokenStopIndex()));
 
 		if (block.getLC() > -1) {
 			block.setStart(block.getLC());
 		} else if (!statements.isEmpty()) {
 			block.setStart(statements.get(0).sourceStart());
 		} else {
 			block.setStart(getTokenOffset(node.getTokenStartIndex()));
 		}
 		if (block.getRC() > -1) {
 			block.setEnd(block.getRC() + 1);
 		} else if (!statements.isEmpty()) {
 			block.setEnd(statements.get(statements.size() - 1).sourceStart());
 		} else {
 			block.setEnd(getTokenOffset(node.getTokenStopIndex()));
 		}
 
 		return block;
 	}
 
 	private Keyword createKeyword(Tree node, String text) {
 		assert text.equals(node.getText());
 		// assert text.equals(Keywords.fromToken(node.getType()));
 		final Keyword keyword = new Keyword(text);
 		setRangeByToken(keyword, node.getTokenStartIndex());
 		return keyword;
 	}
 
 	@Override
 	protected ASTNode visitBreak(Tree node) {
 		BreakStatement statement = new BreakStatement(getParent());
 		statement.setBreakKeyword(createKeyword(node, Keywords.BREAK));
 
 		if (node.getChildCount() > 0) {
 			Label label = new Label(statement);
 			final Tree labelNode = node.getChild(0);
 			label.setText(labelNode.getText());
 			setRangeByToken(label, labelNode.getTokenStartIndex());
 			statement.setLabel(label);
 			validateLabel(label);
 		}
 
 		statement.setSemicolonPosition(getTokenOffset(JSParser.SEMIC, node
 				.getTokenStopIndex(), node.getTokenStopIndex()));
 
 		statement.setStart(statement.getBreakKeyword().sourceStart());
 
 		if (statement.getLabel() != null)
 			statement.setEnd(Math.max(statement.getSemicolonPosition() + 1,
 					statement.getLabel().sourceEnd()));
 		else
 			statement.setEnd(Math.max(statement.getSemicolonPosition() + 1,
 					statement.getBreakKeyword().sourceEnd()));
 		if (statement.getLabel() == null) {
 			validateParent(JavaScriptParserProblems.BAD_BREAK, "bad break",
 					statement, LoopStatement.class, SwitchStatement.class);
 		}
 		return statement;
 	}
 
 	@Override
 	protected ASTNode visitCall(Tree node) {
 		CallExpression call = new CallExpression(getParent());
 
 		Assert.isNotNull(node.getChild(0));
 		Assert.isNotNull(node.getChild(1));
 
 		call.setExpression(transformNode(node.getChild(0), call));
 		Tree callArgs = node.getChild(1);
 		IntList commas = new IntList();
 		for (int i = 0; i < callArgs.getChildCount(); ++i) {
 			Tree callArg = callArgs.getChild(i);
 			final ASTNode argument = transformNode(callArg, call);
 			if (i > 0) {
 				commas.add(getTokenOffset(JSParser.COMMA, callArgs.getChild(
 						i - 1).getTokenStopIndex() + 1, callArg
 						.getTokenStartIndex()));
 			}
 			call.addArgument(argument);
 		}
 		call.setCommas(commas);
 
 		call.setLP(getTokenOffset(JSParser.LPAREN, node.getChild(1)
 				.getTokenStartIndex(), node.getChild(1).getTokenStartIndex()));
 		call.setRP(getTokenOffset(JSParser.RPAREN, node.getChild(1)
 				.getTokenStopIndex(), node.getChild(1).getTokenStopIndex()));
 
 		call.setStart(call.getExpression().sourceStart());
 		call.setEnd(call.getRP() + 1);
 
 		return call;
 	}
 
 	@Override
 	protected ASTNode visitCase(Tree node) {
 		CaseClause caseClause = new CaseClause(getParent());
 
 		caseClause.setCaseKeyword(createKeyword(node, Keywords.CASE));
 
 		final Tree condition = node.getChild(0);
 		if (condition != null) {
 			caseClause.setCondition((Expression) transformNode(condition,
 					caseClause));
 			caseClause
 					.setColonPosition(getTokenOffset(JSParser.COLON, condition
 							.getTokenStopIndex() + 1, node.getTokenStopIndex()));
 		} else {
 			caseClause.setCondition(new ErrorExpression(caseClause,
 					Util.EMPTY_STRING));
 			caseClause
 					.setColonPosition(caseClause.getCaseKeyword().sourceEnd());
 		}
 
 		// skip condition
 		for (int i = 1; i < node.getChildCount(); i++) {
 			caseClause.getStatements().add(
 					transformStatementNode(node.getChild(i), caseClause));
 		}
 
 		caseClause.setStart(caseClause.getCaseKeyword().sourceStart());
 		caseClause.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		return caseClause;
 	}
 
 	@Override
 	protected ASTNode visitDecimalLiteral(Tree node) {
 		DecimalLiteral number = new DecimalLiteral(getParent());
 		number.setText(node.getText());
 		number.setStart(getTokenOffset(node.getTokenStartIndex()));
 		number.setEnd(number.sourceStart() + number.getText().length());
 
 		return number;
 	}
 
 	@Override
 	protected ASTNode visitDefault(Tree node) {
 		DefaultClause defaultClause = new DefaultClause(getParent());
 
 		defaultClause.setDefaultKeyword(createKeyword(node, Keywords.DEFAULT));
 
 		defaultClause.setColonPosition(getTokenOffset(JSParser.COLON, node
 				.getTokenStartIndex() + 1, node.getTokenStopIndex() + 1));
 
 		for (int i = 0; i < node.getChildCount(); i++) {
 			defaultClause.getStatements().add(
 					transformStatementNode(node.getChild(i), defaultClause));
 		}
 
 		defaultClause.setStart(defaultClause.getDefaultKeyword().sourceStart());
 		defaultClause.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		return defaultClause;
 	}
 
 	@Override
 	protected ASTNode visitExpression(Tree node) {
 		if (node.getChildCount() > 0)
 			return transformNode(node.getChild(0), getParent());
 		else
 			return new EmptyExpression(getParent());
 	}
 
 	@Override
 	protected ASTNode visitFor(Tree node) {
 		switch (node.getChild(0).getType()) {
 		case JSParser.FORSTEP:
 			return visitForStatement(node);
 
 		case JSParser.FORITER:
 			return visitForInStatement(node);
 
 		case JSParser.BLOCK:
 			if (node.getChildCount() == 1) {
 				// TODO error reporting???? "for() {" case
 				final ForStatement statement = new ForStatement(getParent());
 				statement.setForKeyword(createKeyword(node, Keywords.FOR));
 				statement.setInitial(new EmptyExpression(statement));
 				statement.setCondition(new EmptyExpression(statement));
 				statement.setStep(new EmptyExpression(statement));
 				statement.setBody(transformStatementNode(node.getChild(0),
 						statement));
 				return statement;
 			}
 
 		default:
 			// TODO error reporting & recovery
 			throw new IllegalArgumentException("FORSTEP or FORITER expected");
 		}
 	}
 
 	private ASTNode visitForStatement(Tree node) {
 		ForStatement statement = new ForStatement(getParent());
 
 		statement.setForKeyword(createKeyword(node, Keywords.FOR));
 
 		statement.setLP(getTokenOffset(JSParser.LPAREN, node
 				.getTokenStartIndex() + 1, node.getTokenStopIndex()));
 
 		statement.setInitial((Expression) transformNode(node.getChild(0)
 				.getChild(0), statement));
 
 		statement.setCondition((Expression) transformNode(node.getChild(0)
 				.getChild(1), statement));
 
 		statement.setStep((Expression) transformNode(node.getChild(0).getChild(
 				2), statement));
 
 		if (statement.getInitial() instanceof EmptyExpression) {
 			statement.setInitialSemicolonPosition(getTokenOffset(
 					JSParser.SEMIC, node.getTokenStartIndex() + 2, node
 							.getTokenStopIndex()));
 
 			statement.getInitial().setStart(
 					statement.getInitialSemicolonPosition());
 			statement.getInitial().setEnd(
 					statement.getInitialSemicolonPosition());
 
 			if (statement.getCondition() instanceof EmptyExpression) {
 				statement.setConditionalSemicolonPosition((getTokenOffset(
 						JSParser.SEMIC, node.getTokenStartIndex(), node
 								.getTokenStopIndex(), 1)));
 
 				statement.getCondition().setStart(
 						statement.getConditionalSemicolonPosition());
 				statement.getCondition().setEnd(
 						statement.getConditionalSemicolonPosition());
 
 			} else {
 				statement.setConditionalSemicolonPosition((getTokenOffset(
 						JSParser.SEMIC, node.getChild(0).getChild(1)
 								.getTokenStopIndex() + 1, node
 								.getTokenStopIndex())));
 
 			}
 
 		} else {
 			statement.setInitialSemicolonPosition(getTokenOffset(
 					JSParser.SEMIC, getRealTokenStopIndex(node.getChild(0)
 							.getChild(0)) + 1, node.getTokenStopIndex()));
 
 			if (statement.getCondition() instanceof EmptyExpression) {
 				statement.setConditionalSemicolonPosition((getTokenOffset(
 						JSParser.SEMIC, node.getChild(0).getChild(0)
 								.getTokenStopIndex() + 1, node
 								.getTokenStopIndex(), 1)));
 
 				statement.getCondition().setStart(
 						statement.getConditionalSemicolonPosition());
 				statement.getCondition().setEnd(
 						statement.getConditionalSemicolonPosition());
 
 			} else {
 				statement.setConditionalSemicolonPosition((getTokenOffset(
 						JSParser.SEMIC, getRealTokenStopIndex(node.getChild(0)
 								.getChild(1)) + 1, node.getTokenStopIndex())));
 			}
 		}
 
 		if (statement.getStep() instanceof EmptyExpression) {
 			statement.setStart(statement.getConditionalSemicolonPosition() + 1);
 			statement.setEnd(statement.getConditionalSemicolonPosition() + 1);
 		}
 
 		statement.setRP(getTokenOffset(JSParser.RPAREN, node.getChild(0)
 				.getTokenStopIndex() + 1, node.getTokenStopIndex()));
 
 		if (node.getChildCount() > 1)
 			statement.setBody(transformStatementNode(node.getChild(1),
 					statement));
 
 		statement.setStart(statement.getForKeyword().sourceStart());
 		statement.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		return statement;
 	}
 
 	private ASTNode visitForInStatement(Tree node) {
 		ForInStatement statement = new ForInStatement(getParent());
 
 		statement.setForKeyword(createKeyword(node, Keywords.FOR));
 
 		statement.setLP(getTokenOffset(JSParser.LPAREN, node
 				.getTokenStartIndex() + 1, node.getChild(0)
 				.getTokenStartIndex()));
 
 		statement.setItem((Expression) transformNode(node.getChild(0).getChild(
 				0), statement));
 
 		Keyword inKeyword = new Keyword(Keywords.IN);
 
 		int iteratorStart = node.getChild(0).getChild(1).getTokenStartIndex();
 
 		if (iteratorStart == -1
 				&& node.getChild(0).getChild(1).getType() == JSParser.EXPR
 				&& node.getChild(0).getChild(1).getChildCount() > 0)
 			iteratorStart = node.getChild(0).getChild(1).getChild(0)
 					.getTokenStartIndex();
 
 		inKeyword.setStart(getTokenOffset(JSParser.IN,
 				getRealTokenStopIndex(node.getChild(0).getChild(0)) + 1,
 				iteratorStart));
 		inKeyword.setEnd(inKeyword.sourceStart() + Keywords.IN.length());
 		statement.setInKeyword(inKeyword);
 
 		statement.setIterator((Expression) transformNode(node.getChild(0)
 				.getChild(1), statement));
 
 		statement.setRP(getTokenOffset(JSParser.RPAREN, node.getChild(0)
 				.getTokenStopIndex() + 1, node.getTokenStopIndex()));
 
 		if (node.getChildCount() > 1)
 			statement.setBody(transformStatementNode(node.getChild(1),
 					statement));
 
 		statement.setStart(statement.getForKeyword().sourceStart());
 		statement.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		return statement;
 	}
 
 	private Argument transformArgument(Tree node, ASTNode parent) {
 		Assert.isTrue(node.getType() == JSParser.Identifier
 				|| JSLexer.isIdentifierKeyword(node.getType()));
 
 		Argument argument = new Argument(parent);
 		argument.setIdentifier((Identifier) visitIdentifier(node));
 		argument.setStart(getTokenOffset(node.getTokenStartIndex()));
 		argument.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 		processType(new ArgumentTypedDeclaration(argument), node, 0);
 		return argument;
 	}
 
 	@Override
 	protected ASTNode visitFunction(Tree node) {
 		FunctionStatement fn = new FunctionStatement(getParent());
 
 		int tokenIndex = node.getTokenStartIndex();
 		while (tokenIndex > 0) {
 			--tokenIndex;
 			final Token token = tokens.get(tokenIndex);
 			if (token.getType() == JSParser.WhiteSpace
 					|| token.getType() == JSParser.EOL) {
 				continue;
 			}
 			if (token.getType() == JSParser.MultiLineComment) {
 				final Comment comment = documentationMap.get(token
 						.getTokenIndex());
 				if (comment != null) {
 					assert token.getText().startsWith(
 							MultiLineComment.JSDOC_PREFIX);
 					fn.setDocumentation(comment);
 				}
 			}
 			break;
 		}
 		fn.setFunctionKeyword(createKeyword(node, Keywords.FUNCTION));
 
 		int index = 0;
 
 		if (node.getChild(index).getType() != JSParser.ARGUMENTS) {
 			fn.setName((Identifier) transformNode(node.getChild(index), fn));
 			index++;
 		}
 
 		Tree argsNode = node.getChild(index++);
 		assert argsNode.getType() == JSParser.ARGUMENTS;
 
 		fn.setLP(getTokenOffset(JSParser.LPAREN, node.getTokenStartIndex() + 1,
 				argsNode.getTokenStartIndex()));
 		final SymbolTable functionScope = new SymbolTable();
 		for (int i = 0, childCount = argsNode.getChildCount(); i < childCount; ++i) {
 			final Tree argNode = argsNode.getChild(i);
 			Argument argument = transformArgument(argNode, fn);
 			if (i + 1 < childCount) {
 				argument.setCommaPosition(getTokenOffset(JSParser.COMMA,
 						argNode.getTokenStopIndex() + 1, argsNode.getChild(
 								i + 1).getTokenStartIndex()));
 			}
 			fn.addArgument(argument);
 			if (functionScope.add(argument.getArgumentName(), SymbolKind.PARAM) != null
 					&& reporter != null) {
 				reporter.setMessage(
 						JavaScriptParserProblems.DUPLICATE_PARAMETER, NLS.bind(
 								"Duplicate parameter {0}", argument
 										.getArgumentName()));
 				reporter.setRange(argument.sourceStart(), argument.sourceEnd());
 				reporter.report();
 			}
 		}
 		fn.setRP(getTokenOffset(JSParser.RPAREN, argsNode.getTokenStopIndex(),
 				node.getChild(index).getTokenStartIndex()));
 		index = processType(new FunctionTypedDeclaration(fn), node, index);
 		final Identifier nameNode = fn.getName();
 		if (nameNode != null
 				&& scope.canAdd(nameNode.getName()) == SymbolKind.PARAM) {
 			reporter.setMessage(
 					JavaScriptParserProblems.FUNCTION_HIDES_ARGUMENT, NLS.bind(
 							"Function {0} hides argument", nameNode.getName()));
 			reporter.setRange(nameNode.sourceStart(), nameNode.sourceEnd());
 			reporter.report();
 		}
 
 		final Tree bodyNode = node.getChild(index);
 		final SymbolTable savedScope = scope;
 		scope = functionScope;
 		fn.setBody((StatementBlock) transformNode(bodyNode, fn));
 		scope = savedScope;
 		fn.setStart(fn.getFunctionKeyword().sourceStart());
 		fn.setEnd(fn.getBody().sourceEnd());
 
 		return fn;
 	}
 
 	@Override
 	protected ASTNode visitIdentifier(Tree node) {
 
 		Identifier id = new Identifier(getParent());
 		id.setName(node.getText());
 
 		setRangeByToken(id, node.getTokenStartIndex());
 
 		return id;
 	}
 
 	@Override
 	protected ASTNode visitReturn(Tree node) {
 
 		ReturnStatement returnStatement = new ReturnStatement(getParent());
 
 		returnStatement.setReturnKeyword(createKeyword(node, Keywords.RETURN));
 
 		if (node.getChildCount() > 0) {
 			returnStatement.setValue((Expression) transformNode(node
 					.getChild(0), returnStatement));
 		}
 
 		Token token = tokens.get(node.getTokenStopIndex());
 		if (token.getType() == JSParser.SEMIC) {
 			returnStatement.setSemicolonPosition(getTokenOffset(node
 					.getTokenStopIndex()));
 
 			returnStatement.setEnd(returnStatement.getSemicolonPosition() + 1);
 		} else if (returnStatement.getValue() != null) {
 			returnStatement.setEnd(returnStatement.getValue().sourceEnd());
 		} else {
 			returnStatement.setEnd(returnStatement.getReturnKeyword()
 					.sourceEnd());
 		}
 
 		returnStatement.setStart(returnStatement.getReturnKeyword()
 				.sourceStart());
 		validateParent(JavaScriptParserProblems.INVALID_RETURN,
 				"invalid return", returnStatement, FunctionStatement.class,
 				Method.class);
 		return returnStatement;
 	}
 
 	@Override
 	protected ASTNode visitStringLiteral(Tree node) {
 
 		StringLiteral literal = new StringLiteral(getParent());
 		literal.setText(node.getText());
 
 		literal.setStart(getTokenOffset(node.getTokenStartIndex()));
 		literal.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		return literal;
 	}
 
 	@Override
 	protected ASTNode visitSwitch(Tree node) {
 
 		SwitchStatement statement = new SwitchStatement(getParent());
 
 		statement.setSwitchKeyword(createKeyword(node, Keywords.SWITCH));
 
 		statement.setLP(getTokenOffset(JSParser.LPAREN, node
 				.getTokenStartIndex() + 1, node.getChild(0)
 				.getTokenStartIndex()));
 		statement.setRP(getTokenOffset(JSParser.RPAREN, node.getChild(0)
 				.getTokenStopIndex() + 1, node.getTokenStopIndex()));
 
 		statement.setCondition((Expression) transformNode(node.getChild(0),
 				statement));
 
 		statement.setLC(getTokenOffset(JSParser.LBRACE, node.getChild(0)
 				.getTokenStopIndex() + 1, node.getTokenStopIndex()));
 
 		List<Tree> caseNodes = new ArrayList<Tree>(node.getChildCount() - 1);
 		for (int i = 1; i < node.getChildCount(); i++) {
 			caseNodes.add(node.getChild(i));
 		}
 		Collections.sort(caseNodes, new Comparator<Tree>() {
 			public int compare(Tree o1, Tree o2) {
 				return o1.getTokenStartIndex() - o2.getTokenStartIndex();
 			}
 		});
 		int defaultCount = 0;
 		for (Tree child : caseNodes) {
 			switch (child.getType()) {
 			case JSParser.CASE:
 				statement.addCase((SwitchComponent) transformNode(child,
 						statement));
 				break;
 			case JSParser.DEFAULT:
 				if (defaultCount != 0 && reporter != null) {
 					reporter.setMessage(
 							JavaScriptParserProblems.DOUBLE_SWITCH_DEFAULT,
 							"double default label in the switch statement");
 					reporter.setSeverity(Severity.ERROR);
 					reporter.setStart(reporter.getOffset(child.getLine(), child
 							.getCharPositionInLine()));
 					reporter.setEnd(reporter.getStart()
 							+ child.getText().length());
 					reporter.report();
 				}
 				++defaultCount;
 				statement.addCase((SwitchComponent) transformNode(child,
 						statement));
 				break;
 			default:
 				throw new UnsupportedOperationException();
 			}
 		}
 
 		statement.setRC(getTokenOffset(JSParser.RBRACE, node
 				.getTokenStopIndex(), node.getTokenStopIndex()));
 
 		statement.setStart(statement.getSwitchKeyword().sourceStart());
 		statement.setEnd(statement.getRC() + 1);
 
 		return statement;
 	}
 
 	@Override
 	protected ASTNode visitUnaryOperation(Tree node) {
 
 		UnaryOperation operation = new UnaryOperation(getParent());
 
 		operation.setOperation(node.getType());
 
 		int operationType = node.getType();
 
 		if (operation.isPostfix())
 			operation.setOperationPosition(getTokenOffset(operationType, node
 					.getChild(0).getTokenStopIndex() + 1, node
 					.getTokenStopIndex()));
 		else
 			operation.setOperationPosition(getTokenOffset(operationType, node
 					.getTokenStartIndex(), node.getTokenStopIndex()));
 
 		if (operation.getOperationPosition() == -1) {
 
 			// use compatible operations
 			switch (operationType) {
 			case JSParser.PINC:
 				operationType = JSParser.INC;
 				break;
 
 			case JSParser.PDEC:
 				operationType = JSParser.DEC;
 				break;
 
 			case JSParser.POS:
 				operationType = JSParser.ADD;
 				break;
 
 			case JSParser.NEG:
 				operationType = JSParser.SUB;
 				break;
 			}
 
 			if (operation.isPostfix())
 				operation.setOperationPosition(getTokenOffset(operationType,
 						node.getChild(0).getTokenStopIndex() + 1, node
 								.getTokenStopIndex()));
 			else
 				operation.setOperationPosition(getTokenOffset(operationType,
 						node.getTokenStartIndex(), node.getTokenStopIndex()));
 
 		}
 
 		Assert.isTrue(operation.getOperationPosition() > -1);
 
 		operation.setExpression((Expression) transformNode(node.getChild(0),
 				operation));
 
 		operation.setStart(getTokenOffset(node.getTokenStartIndex()));
 		operation.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		return operation;
 	}
 
 	@Override
 	protected ASTNode visitContinue(Tree node) {
 		ContinueStatement statement = new ContinueStatement(getParent());
 		statement.setContinueKeyword(createKeyword(node, Keywords.CONTINUE));
 
 		if (node.getChildCount() > 0) {
 			Label label = new Label(statement);
 			final Tree labelNode = node.getChild(0);
 			label.setText(labelNode.getText());
 			setRangeByToken(label, labelNode.getTokenStartIndex());
 			statement.setLabel(label);
 			validateLabel(label);
 		}
 
 		statement.setSemicolonPosition(getTokenOffset(JSParser.SEMIC, node
 				.getTokenStopIndex(), node.getTokenStopIndex()));
 		statement.setStart(getTokenOffset(node.getTokenStartIndex()));
 		statement.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 		if (statement.getLabel() == null) {
 			validateParent(JavaScriptParserProblems.BAD_CONTINUE,
 					"continue must be inside loop", statement,
 					LoopStatement.class);
 		}
 		return statement;
 	}
 
 	private void validateLabel(Label label) {
 		if (reporter == null)
 			return;
 		if (!scope.hasLabel(label.getText())) {
 			reporter.setMessage(JavaScriptParserProblems.UNDEFINED_LABEL,
 					"undefined label");
 			reporter.setSeverity(Severity.ERROR);
 			reporter.setRange(label.sourceStart(), label.sourceEnd());
 			reporter.report();
 		}
 	}
 
 	private void validateParent(int messageId, String message,
 			Statement statement, Class<?>... classes) {
 		if (reporter == null)
 			return;
 		for (ListIterator<ASTNode> i = parents.listIterator(parents.size()); i
 				.hasPrevious();) {
 			ASTNode parent = i.previous();
 			for (Class<?> clazz : classes) {
 				if (clazz.isInstance(parent)) {
 					return;
 				}
 			}
 		}
 		reporter.setMessage(messageId, message);
 		reporter.setRange(statement.sourceStart(), statement.sourceEnd());
 		reporter.setSeverity(Severity.ERROR);
 		reporter.report();
 	}
 
 	private Type transformType(Tree node, ASTNode parent) {
 		Assert.isTrue(node.getType() == JSParser.Identifier);
 		SimpleType type = new SimpleType(parent);
 		type.setName(node.getText());
 		setRangeByToken(type, node.getTokenStartIndex());
 		return type;
 	}
 
 	private boolean isType(Tree node) {
 		return node.getType() == JSParser.Identifier;
 	}
 
 	private int processType(ITypedDeclaration target, Tree node, int index) {
 		if (index < node.getChildCount()
 				&& node.getChild(index).getType() == JSParser.COLON) {
 			final int colonPos = getTokenOffset(node.getChild(index)
 					.getTokenStartIndex());
 			target.setColonPosition(colonPos);
 			++index;
 			if (index < node.getChildCount() && isType(node.getChild(index))) {
 				target.setType(transformType(node.getChild(index), target
 						.getNode()));
 				++index;
 			} else {
 				target.setType(new MissingType(target.getNode(), colonPos));
 			}
 		}
 		return index;
 	}
 
 	private VariableDeclaration transformVariableDeclaration(Tree node,
 			IVariableStatement statement) {
 		Assert.isTrue(node.getType() == JSParser.Identifier
 				|| JSLexer.isIdentifierKeyword(node.getType()));
 
 		VariableDeclaration declaration = new VariableDeclaration(
 				(ASTNode) statement);
 		declaration
 				.setIdentifier((Identifier) transformNode(node, declaration));
 		declaration.setStart(getTokenOffset(node.getTokenStartIndex()));
 		declaration.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 		int i = processType(new VariableTypedDeclaration(declaration), node, 0);
 		if (i + 2 <= node.getChildCount()
 				&& node.getChild(i).getType() == JSParser.ASSIGN) {
 			declaration.setAssignPosition(getTokenOffset(node.getChild(i)
 					.getTokenStartIndex()));
 			declaration.setInitializer((Expression) transformNode(node
 					.getChild(i + 1), declaration));
 			i += 2;
 		}
 		return declaration;
 	}
 
 	@Override
 	protected ASTNode visitVarDeclaration(Tree node) {
 		VariableStatement var = new VariableStatement(getParent());
 		var.setVarKeyword(createKeyword(node, Keywords.VAR));
 
 		processVariableDeclarations(node, var, SymbolKind.VAR);
 
 		var.setStart(getTokenOffset(node.getTokenStartIndex()));
 		var.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		return var;
 	}
 
 	private void processVariableDeclarations(Tree node, IVariableStatement var,
 			SymbolKind kind) {
 		for (int i = 0, childCount = node.getChildCount(); i < childCount; i++) {
 			final Tree varNode = node.getChild(i);
 			final VariableDeclaration declaration = transformVariableDeclaration(
 					varNode, var);
 			var.addVariable(declaration);
 			if (i + 1 < childCount) {
 				declaration.setCommaPosition(getTokenOffset(JSParser.COMMA,
 						varNode.getTokenStopIndex() + 1, node.getChild(i + 1)
 								.getTokenStartIndex()));
 			}
 			if (scope.add(declaration.getVariableName(), kind) != null
 					&& reporter != null) {
 				final Identifier identifier = declaration.getIdentifier();
 				reporter.setRange(identifier.sourceStart(), identifier
 						.sourceEnd());
 				reporter
 						.setMessage(
 								kind == SymbolKind.VAR ? JavaScriptParserProblems.VAR_HIDES_ARGUMENT
 										: JavaScriptParserProblems.CONST_HIDES_ARGUMENT,
 								kind.name() + " "
 										+ declaration.getVariableName()
 										+ " hides argument");
 				reporter.report();
 			}
 		}
 	}
 
 	@Override
 	protected ASTNode visitObjectInitializer(Tree node) {
 
 		ObjectInitializer initializer = new ObjectInitializer(getParent());
 
 		IntList commas = new IntList();
 
 		for (int i = 0; i < node.getChildCount(); i++) {
 			final Tree child = node.getChild(i);
 			if (child.getType() == JSParser.COMMA) {
 				commas.add(getTokenOffset(child.getTokenStartIndex()));
 			} else {
 				initializer
 						.addInitializer((ObjectInitializerPart) transformNode(
 								child, initializer));
 			}
 		}
 		if (!commas.isEmpty()
 				&& commas.size() >= initializer.getInitializers().size()) {
 			reporter
 					.setMessage(
 							JavaScriptParserProblems.TRAILING_COMMA_OBJECT_INITIALIZER,
 							"trailing comma is not legal in ECMA-262 object initializers");
 			final int comma = commas.get(commas.size() - 1);
 			reporter.setRange(comma, comma + 1);
 			reporter.report();
 		}
 
 		initializer.setCommas(commas);
 
 		initializer.setLC(getTokenOffset(node.getTokenStartIndex()));
 		initializer.setRC(getTokenOffset(node.getTokenStopIndex()));
 
 		Token LC = tokens.get(node.getTokenStartIndex());
 		Token RC = tokens.get(node.getTokenStopIndex());
 
 		initializer.setMultiline(LC.getLine() != RC.getLine());
 
 		initializer.setStart(initializer.getLC());
 		initializer.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		return initializer;
 	}
 
 	@Override
 	protected ASTNode visitPropertyInitializer(Tree node) {
 
 		PropertyInitializer initializer = new PropertyInitializer(getParent());
 
 		initializer.setName((Expression) transformNode(node.getChild(0),
 				initializer));
 
 		initializer
 				.setColon(getTokenOffset(JSParser.COLON, node.getChild(0)
 						.getTokenStopIndex() + 1, node.getChild(1)
 						.getTokenStartIndex()));
 
 		initializer.setValue((Expression) transformNode(node.getChild(1),
 				initializer));
 
 		initializer.setStart(getTokenOffset(node.getTokenStartIndex()));
 		initializer.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		return initializer;
 	}
 
 	@Override
 	protected ASTNode visitForEachInStatement(Tree node) {
 		ForEachInStatement statement = new ForEachInStatement(getParent());
 
 		statement.setForKeyword(createKeyword(node, Keywords.FOR));
 
 		Keyword eachKeyword = new Keyword(Keywords.EACH);
 		eachKeyword.setStart(getTokenOffset(JSParser.EACH, node
 				.getTokenStartIndex(), node.getTokenStopIndex()));
 		eachKeyword.setEnd(eachKeyword.sourceStart() + Keywords.EACH.length());
 		statement.setEachKeyword(eachKeyword);
 
 		statement.setLP(getTokenOffset(JSParser.LPAREN, node
 				.getTokenStartIndex() + 1, node.getChild(0)
 				.getTokenStartIndex()));
 
 		statement.setItem((Expression) transformNode(node.getChild(0).getChild(
 				0), statement));
 
 		Keyword inKeyword = new Keyword(Keywords.IN);
 		int iteratorStart = node.getChild(0).getChild(1).getTokenStartIndex();
 		if (iteratorStart == -1
 				&& node.getChild(0).getChild(1).getType() == JSParser.EXPR
 				&& node.getChild(0).getChild(1).getChildCount() > 0)
 			iteratorStart = node.getChild(0).getChild(1).getChild(0)
 					.getTokenStartIndex();
 
 		inKeyword.setStart(getTokenOffset(JSParser.IN,
 				getRealTokenStopIndex(node.getChild(0).getChild(0)) + 1,
 				iteratorStart));
 		inKeyword.setEnd(inKeyword.sourceStart() + Keywords.IN.length());
 		statement.setInKeyword(inKeyword);
 
 		statement.setIterator((Expression) transformNode(node.getChild(0)
 				.getChild(1), statement));
 
 		statement.setRP(getTokenOffset(JSParser.RPAREN, node.getChild(0)
 				.getTokenStopIndex() + 1, node.getTokenStopIndex()));
 
 		if (node.getChildCount() > 1)
 			statement.setBody(transformStatementNode(node.getChild(1),
 					statement));
 
 		statement.setStart(getTokenOffset(node.getTokenStartIndex()));
 		statement.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		return statement;
 	}
 
 	private static int getRealTokenStopIndex(Tree node) {
 
 		if (node.getTokenStopIndex() == -1)
 			return getRealTokenStopIndex(node
 					.getChild(node.getChildCount() - 1));
 
 		if (node.getChildCount() > 0) {
 			return Math.max(node.getTokenStopIndex(),
 					getRealTokenStopIndex(node
 							.getChild(node.getChildCount() - 1)));
 		}
 
 		return node.getTokenStopIndex();
 	}
 
 	@Override
 	protected ASTNode visitByField(Tree node) {
 
 		PropertyExpression property = new PropertyExpression(getParent());
 
 		property.setObject((Expression) transformNode(node.getChild(0),
 				property));
 
 		property.setProperty((Expression) transformNode(node.getChild(2),
 				property));
 
 		property.setDotPosition(getTokenOffset(node.getChild(1)
 				.getTokenStartIndex()));
 
 		Assert.isTrue(property.getObject().sourceStart() >= 0);
 		Assert.isTrue(property.getProperty().sourceEnd() > 0);
 
 		property.setStart(property.getObject().sourceStart());
 		property.setEnd(property.getProperty().sourceEnd());
 
 		return property;
 	}
 
 	@Override
 	protected ASTNode visitWhile(Tree node) {
 
 		WhileStatement statement = new WhileStatement(getParent());
 
 		statement.setWhileKeyword(createKeyword(node, Keywords.WHILE));
 
 		statement.setLP(getTokenOffset(JSParser.LPAREN, node
 				.getTokenStartIndex(), node.getChild(0).getTokenStartIndex()));
 
 		statement.setCondition((Expression) transformNode(node.getChild(0),
 				statement));
 
 		statement.setRP(getTokenOffset(JSParser.RPAREN, node.getChild(0)
 				.getTokenStopIndex() + 1, node.getTokenStopIndex()));
 
 		if (node.getChildCount() > 1)
 			statement.setBody(transformStatementNode(node.getChild(1),
 					statement));
 
 		statement.setStart(getTokenOffset(node.getTokenStartIndex()));
 		statement.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		return statement;
 	}
 
 	@Override
 	protected ASTNode visitIf(Tree node) {
 
 		IfStatement ifStatement = new IfStatement(getParent());
 
 		ifStatement.setIfKeyword(createKeyword(node, Keywords.IF));
 
 		ifStatement.setLP(getTokenOffset(JSParser.LPAREN, node
 				.getTokenStartIndex() + 1, node.getChild(0)
 				.getTokenStartIndex()));
 		ifStatement.setCondition((Expression) transformNode(node.getChild(0),
 				ifStatement));
 
 		if (node.getChildCount() > 1) {
 			ifStatement.setRP(getTokenOffset(JSParser.RPAREN, node.getChild(0)
 					.getTokenStopIndex() + 1, node.getChild(1)
 					.getTokenStartIndex()));
 			ifStatement.setThenStatement(transformStatementNode(node
 					.getChild(1), ifStatement));
 		} else {
 			ifStatement.setRP(getTokenOffset(JSParser.RPAREN, node.getChild(0)
 					.getTokenStopIndex() + 1, node.getChild(0)
 					.getTokenStopIndex() + 1));
 		}
 
 		if (node.getChildCount() > 2) {
 			Keyword elseKeyword = new Keyword(Keywords.ELSE);
 			elseKeyword.setStart(getTokenOffset(JSParser.ELSE, node.getChild(1)
 					.getTokenStopIndex() + 1, node.getChild(2)
 					.getTokenStartIndex()));
 			elseKeyword.setEnd(elseKeyword.sourceStart()
 					+ Keywords.ELSE.length());
 			ifStatement.setElseKeyword(elseKeyword);
 
 			ifStatement.setElseStatement(transformStatementNode(node
 					.getChild(2), ifStatement));
 		}
 
 		ifStatement.setStart(ifStatement.getIfKeyword().sourceStart());
		ifStatement.setEnd(node.getTokenStopIndex() + 1);
 
 		return ifStatement;
 	}
 
 	@Override
 	protected ASTNode visitDoWhile(Tree node) {
 		DoWhileStatement statement = new DoWhileStatement(getParent());
 
 		statement.setDoKeyword(createKeyword(node, Keywords.DO));
 
 		statement.setBody(transformStatementNode(node.getChild(0), statement));
 
 		Keyword whileKeyword = new Keyword(Keywords.WHILE);
 		whileKeyword
 				.setStart(getTokenOffset(JSParser.WHILE, node.getChild(0)
 						.getTokenStopIndex() + 1, node.getChild(1)
 						.getTokenStartIndex()));
 		whileKeyword.setEnd(whileKeyword.sourceStart()
 				+ Keywords.WHILE.length());
 		statement.setWhileKeyword(whileKeyword);
 
 		statement
 				.setLP(getTokenOffset(JSParser.LPAREN, node.getChild(0)
 						.getTokenStopIndex() + 1, node.getChild(1)
 						.getTokenStartIndex()));
 
 		statement.setCondition((Expression) transformNode(node.getChild(1),
 				statement));
 
 		statement.setRP(getTokenOffset(JSParser.RPAREN, node.getChild(1)
 				.getTokenStopIndex() + 1, node.getTokenStopIndex()));
 
 		statement
 				.setSemicolonPosition(getTokenOffset(JSParser.SEMIC, node
 						.getChild(1).getTokenStopIndex() + 1, node
 						.getTokenStopIndex()));
 
 		statement.setStart(getTokenOffset(node.getTokenStartIndex()));
 		statement.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		return statement;
 	}
 
 	@Override
 	protected ASTNode visitConditional(Tree node) {
 
 		ConditionalOperator operator = new ConditionalOperator(getParent());
 
 		operator.setCondition((Expression) transformNode(node.getChild(0),
 				operator));
 		operator.setTrueValue((Expression) transformNode(node.getChild(1),
 				operator));
 		operator.setFalseValue((Expression) transformNode(node.getChild(2),
 				operator));
 
 		operator.setQuestionPosition(getTokenOffset(JSParser.QUE, node
 				.getChild(0).getTokenStopIndex() + 1, node.getChild(1)
 				.getTokenStartIndex()));
 
 		operator.setColonPosition(getTokenOffset(JSParser.COLON, node.getChild(
 				1).getTokenStopIndex() + 1, node.getChild(2)
 				.getTokenStartIndex()));
 
 		operator.setStart(getTokenOffset(node.getTokenStartIndex()));
 		operator.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		return operator;
 	}
 
 	@Override
 	protected ASTNode visitParenthesizedExpression(Tree node) {
 
 		ParenthesizedExpression expression = new ParenthesizedExpression(
 				getParent());
 
 		expression.setExpression((Expression) transformNode(node.getChild(0),
 				expression));
 
 		expression.setLP(getTokenOffset(node.getTokenStartIndex()));
 		expression.setRP(getTokenOffset(node.getTokenStopIndex()));
 
 		expression.setStart(expression.getLP());
 		expression.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		return expression;
 	}
 
 	@Override
 	protected ASTNode visitTry(Tree node) {
 
 		TryStatement statement = new TryStatement(getParent());
 
 		statement.setTryKeyword(createKeyword(node, Keywords.TRY));
 
 		statement.setBody((StatementBlock) transformStatementNode(node
 				.getChild(0), statement));
 
 		boolean sawDefaultCatch = false;
 		for (int i = 1 /* miss body */; i < node.getChildCount(); i++) {
 
 			Tree child = node.getChild(i);
 
 			switch (child.getType()) {
 			case JSParser.CATCH:
 				final CatchClause catchClause = (CatchClause) transformNode(
 						child, statement);
 				if (reporter != null && sawDefaultCatch) {
 					reporter
 							.setMessage(
 									JavaScriptParserProblems.CATCH_UNREACHABLE,
 									"any catch clauses following an unqualified catch are unreachable");
 					reporter.setRange(catchClause.sourceStart(), catchClause
 							.getRP() + 1);
 					reporter.report();
 				}
 				if (!sawDefaultCatch
 						&& catchClause.getFilterExpression() == null) {
 					sawDefaultCatch = true;
 				}
 				statement.getCatches().add(catchClause);
 				break;
 
 			case JSParser.FINALLY:
 				statement.setFinally((FinallyClause) transformNode(child,
 						statement));
 				break;
 
 			default:
 				throw new UnsupportedOperationException(
 						"CATCH or FINALLY expected");
 			}
 
 		}
 
 		statement.setStart(getTokenOffset(node.getTokenStartIndex()));
 		statement.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		return statement;
 	}
 
 	@Override
 	protected ASTNode visitThrow(Tree node) {
 
 		ThrowStatement statement = new ThrowStatement(getParent());
 
 		statement.setThrowKeyword(createKeyword(node, Keywords.THROW));
 
 		if (node.getChildCount() > 0) {
 			statement.setException((Expression) transformNode(node.getChild(0),
 					statement));
 		}
 
 		statement.setSemicolonPosition(getTokenOffset(JSParser.SEMIC, node
 				.getTokenStopIndex(), node.getTokenStopIndex()));
 
 		statement.setStart(getTokenOffset(node.getTokenStartIndex()));
 		statement.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		return statement;
 	}
 
 	@Override
 	protected ASTNode visitNew(Tree node) {
 
 		NewExpression expression = new NewExpression(getParent());
 
 		expression.setNewKeyword(createKeyword(node, Keywords.NEW));
 
 		expression.setObjectClass((Expression) transformNode(node.getChild(0),
 				expression));
 
 		expression.setStart(getTokenOffset(node.getTokenStartIndex()));
 		expression.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		return expression;
 	}
 
 	@Override
 	protected ASTNode visitCatch(Tree node) {
 
 		CatchClause catchClause = new CatchClause(getParent());
 
 		catchClause.setCatchKeyword(createKeyword(node, Keywords.CATCH));
 
 		catchClause.setLP(getTokenOffset(JSParser.LPAREN, node
 				.getTokenStartIndex() + 1, node.getChild(0)
 				.getTokenStartIndex()));
 
 		catchClause.setException((Identifier) transformNode(node.getChild(0),
 				catchClause));
 
 		int statementIndex = 1;
 
 		if (statementIndex < node.getChildCount()
 				&& node.getChild(statementIndex).getType() == JSParser.IF) {
 			catchClause.setIfKeyword(createKeyword(node
 					.getChild(statementIndex++), Keywords.IF));
 
 			catchClause.setFilterExpression((Expression) transformNode(node
 					.getChild(statementIndex++), catchClause));
 		}
 
 		if (statementIndex < node.getChildCount()) {
 			catchClause.setRP(getTokenOffset(JSParser.RPAREN, node.getChild(
 					statementIndex - 1).getTokenStopIndex() + 1, node.getChild(
 					statementIndex).getTokenStartIndex()));
 
 			catchClause.setStatement(transformStatementNode(node
 					.getChild(statementIndex), catchClause));
 		}
 
 		catchClause.setStart(getTokenOffset(node.getTokenStartIndex()));
 		catchClause.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		return catchClause;
 	}
 
 	@Override
 	protected ASTNode visitFinally(Tree node) {
 
 		FinallyClause finallyClause = new FinallyClause(getParent());
 
 		finallyClause.setFinallyKeyword(createKeyword(node, Keywords.FINALLY));
 
 		if (node.getChildCount() >= 1) {
 			finallyClause.setStatement(transformStatementNode(node.getChild(0),
 					finallyClause));
 		}
 
 		finallyClause.setStart(getTokenOffset(node.getTokenStartIndex()));
 		finallyClause.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		return finallyClause;
 	}
 
 	@Override
 	protected ASTNode visitArray(Tree node) {
 
 		ArrayInitializer array = new ArrayInitializer(getParent());
 
 		array.setLB(getTokenOffset(JSParser.LBRACK, node.getTokenStartIndex(),
 				node.getTokenStartIndex()));
 
 		List<ASTNode> items = new ArrayList<ASTNode>(node.getChildCount());
 		IntList commas = new IntList();
 
 		for (int i = 0; i < node.getChildCount(); i++) {
 			Tree child = node.getChild(i);
 
 			if (child.getType() != JSParser.ITEM)
 				throw new UnsupportedOperationException("ITEM expected"); //$NON-NLS-1$
 
 			final Tree item = child.getChild(0);
 			if (item != null) {
 				items.add(transformNode(item, array));
 			} else {
 				items.add(new EmptyExpression(getParent()));
 			}
 
 			if (i > 0) {
 				commas.add(getTokenOffset(JSParser.COMMA, node.getChild(i - 1)
 						.getTokenStopIndex() + 1, child.getTokenStartIndex()));
 			}
 		}
 
 		array.setItems(items);
 		array.setCommas(commas);
 
 		array.setRB(getTokenOffset(JSParser.RBRACK, node.getTokenStopIndex(),
 				node.getTokenStopIndex()));
 
 		array.setStart(array.getLB());
 		array.setEnd(array.getRB() + 1);
 
 		return array;
 	}
 
 	@Override
 	protected ASTNode visitByIndex(Tree node) {
 
 		GetArrayItemExpression item = new GetArrayItemExpression(getParent());
 
 		item.setArray((Expression) transformNode(node.getChild(0), item));
 		item.setIndex((Expression) transformNode(node.getChild(1), item));
 
 		item.setLB(getTokenOffset(JSParser.LBRACK, getRealTokenStopIndex(node
 				.getChild(0)) + 1, node.getChild(1).getTokenStartIndex()));
 
 		item.setRB(getTokenOffset(JSParser.RBRACK, node.getChild(1)
 				.getTokenStopIndex() + 1, tokens.size() + 1));
 
 		item.setStart(item.getArray().sourceStart());
 		item.setEnd(item.getRB() + 1);
 
 		return item;
 	}
 
 	@Override
 	protected ASTNode visitCommaExpression(Tree node) {
 
 		CommaExpression expression = new CommaExpression(getParent());
 
 		List<ASTNode> items = new ArrayList<ASTNode>(node.getChildCount());
 		IntList commas = new IntList();
 
 		for (int i = 0; i < node.getChildCount(); i++) {
 			items.add(transformNode(node.getChild(i), expression));
 
 			if (i > 0)
 				commas.add(getTokenOffset(JSParser.COMMA, node.getChild(i - 1)
 						.getTokenStopIndex(), node.getChild(i)
 						.getTokenStartIndex()));
 		}
 
 		expression.setItems(items);
 		expression.setCommas(commas);
 
 		expression.setStart(getTokenOffset(node.getTokenStartIndex()));
 		expression.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		return expression;
 	}
 
 	@Override
 	protected ASTNode visitRegExp(Tree node) {
 		RegExpLiteral regexp = new RegExpLiteral(getParent());
 		regexp.setText(node.getText());
 
 		regexp.setStart(getTokenOffset(node.getTokenStartIndex()));
 		regexp.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		return regexp;
 	}
 
 	@Override
 	protected ASTNode visitWith(Tree node) {
 
 		WithStatement statement = new WithStatement(getParent());
 
 		statement.setWithKeyword(createKeyword(node, Keywords.WITH));
 
 		statement.setLP(getTokenOffset(JSParser.LPAREN, node
 				.getTokenStartIndex(), node.getChild(0).getTokenStartIndex()));
 
 		statement.setExpression((Expression) transformNode(node.getChild(0),
 				statement));
 
 		statement.setRP(getTokenOffset(JSParser.RPAREN, node.getChild(0)
 				.getTokenStopIndex() + 1, node.getTokenStopIndex()));
 
 		if (node.getChildCount() > 1)
 			statement.setStatement(transformStatementNode(node.getChild(1),
 					statement));
 
 		statement.setStart(getTokenOffset(node.getTokenStartIndex()));
 		statement.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		return statement;
 	}
 
 	@Override
 	protected ASTNode visitThis(Tree node) {
 
 		ThisExpression expression = new ThisExpression(getParent());
 
 		expression.setThisKeyword(createKeyword(node, Keywords.THIS));
 
 		expression.setStart(getTokenOffset(node.getTokenStartIndex()));
 		expression.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		return expression;
 	}
 
 	@Override
 	protected ASTNode visitLabelled(Tree node) {
 		LabelledStatement statement = new LabelledStatement(getParent());
 
 		Label label = new Label(statement);
 		label.setText(node.getChild(0).getText());
 		setRangeByToken(label, node.getChild(0).getTokenStartIndex());
 		statement.setLabel(label);
 
 		statement.setColonPosition(getTokenOffset(JSParser.COLON, node
 				.getChild(0).getTokenStopIndex() + 1,
 				node.getTokenStopIndex() + 1));
 
 		if (!scope.addLabel(statement) && reporter != null) {
 			reporter.setMessage(JavaScriptParserProblems.DUPLICATE_LABEL,
 					"duplicate label");
 			reporter.setSeverity(Severity.ERROR);
 			reporter.setRange(label.sourceStart(), label.sourceEnd());
 			reporter.report();
 		}
 
 		if (node.getChildCount() > 1) {
 			statement.setStatement(transformStatementNode(node.getChild(1),
 					statement));
 		}
 
 		statement.setStart(getTokenOffset(node.getTokenStartIndex()));
 		statement.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		return statement;
 	}
 
 	@Override
 	protected ASTNode visitDelete(Tree node) {
 
 		DeleteStatement statement = new DeleteStatement(getParent());
 
 		statement.setDeleteKeyword(createKeyword(node, Keywords.DELETE));
 
 		statement.setExpression((Expression) transformNode(node.getChild(0),
 				statement));
 
 		statement.setStart(getTokenOffset(node.getTokenStartIndex()));
 		statement.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		return statement;
 	}
 
 	@Override
 	protected ASTNode visitGet(Tree node) {
 
 		GetMethod method = new GetMethod(getParent());
 
 		method.setGetKeyword(createKeyword(node, Keywords.GET));
 
 		method.setName((Identifier) transformNode(node.getChild(0), method));
 
 		method
 				.setLP(getTokenOffset(JSParser.LPAREN, node.getChild(0)
 						.getTokenStopIndex() + 1, node.getChild(1)
 						.getTokenStartIndex()));
 
 		method
 				.setRP(getTokenOffset(JSParser.RPAREN, node.getChild(0)
 						.getTokenStopIndex() + 1, node.getChild(1)
 						.getTokenStartIndex()));
 
 		method.setBody((StatementBlock) transformStatementNode(
 				node.getChild(1), method));
 
 		method.setStart(getTokenOffset(node.getTokenStartIndex()));
 		method.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		return method;
 	}
 
 	@Override
 	protected ASTNode visitSet(Tree node) {
 		SetMethod method = new SetMethod(getParent());
 
 		method.setSetKeyword(createKeyword(node, Keywords.SET));
 
 		method.setName((Identifier) transformNode(node.getChild(0), method));
 
 		method
 				.setLP(getTokenOffset(JSParser.LPAREN, node.getChild(0)
 						.getTokenStopIndex() + 1, node.getChild(1)
 						.getTokenStartIndex()));
 
 		method
 				.setArgument((Identifier) transformNode(node.getChild(1),
 						method));
 
 		method
 				.setRP(getTokenOffset(JSParser.RPAREN, node.getChild(0)
 						.getTokenStopIndex() + 1, node.getChild(2)
 						.getTokenStartIndex()));
 
 		method.setBody((StatementBlock) transformStatementNode(
 				node.getChild(2), method));
 
 		method.setStart(getTokenOffset(node.getTokenStartIndex()));
 		method.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		return method;
 	}
 
 	@Override
 	protected ASTNode visitNull(Tree node) {
 
 		NullExpression expression = new NullExpression(getParent());
 
 		expression.setStart(getTokenOffset(node.getTokenStartIndex()));
 		expression.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		return expression;
 	}
 
 	@Override
 	protected ASTNode visitTypeOf(Tree node) {
 
 		TypeOfExpression expression = new TypeOfExpression(getParent());
 
 		expression.setTypeOfKeyword(createKeyword(node, Keywords.TYPEOF));
 
 		expression.setExpression((Expression) transformNode(node.getChild(0),
 				expression));
 
 		expression.setStart(getTokenOffset(node.getTokenStartIndex()));
 		expression.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		return expression;
 	}
 
 	@Override
 	protected ASTNode visitConst(Tree node) {
 		ConstStatement declaration = new ConstStatement(getParent());
 		declaration.setConstKeyword(createKeyword(node, Keywords.CONST));
 
 		processVariableDeclarations(node, declaration, SymbolKind.CONST);
 
 		declaration.setSemicolonPosition(getTokenOffset(JSParser.SEMIC, node
 				.getTokenStopIndex(), node.getTokenStopIndex()));
 
 		declaration.setStart(getTokenOffset(node.getTokenStartIndex()));
 		declaration.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		return declaration;
 	}
 
 	private void addComments(Script script) {
 		for (int i = 0; i < tokens.size(); i++) {
 			final Token token = tokens.get(i);
 			final Comment comment;
 			if (token.getType() == JSParser.MultiLineComment) {
 				Comment c = new MultiLineComment();
 				c.setText(token.getText());
 				c.setStart(getTokenOffset(token.getTokenIndex()));
 				c.setEnd(c.sourceStart() + token.getText().length());
 				comment = c;
 			} else if (token.getType() == JSParser.SingleLineComment) {
 				Comment c = new SingleLineComment();
 				c.setText(token.getText());
 				c.setStart(getTokenOffset(token.getTokenIndex()));
 				c.setEnd(c.sourceStart() + token.getText().length());
 				comment = c;
 			} else {
 				continue;
 			}
 			script.addComment(comment);
 			if (comment.isDocumentation()) {
 				documentationMap.put(token.getTokenIndex(), comment);
 			}
 		}
 	}
 
 	@Override
 	protected ASTNode visitBooleanLiteral(Tree node) {
 
 		BooleanLiteral bool = new BooleanLiteral(getParent());
 		bool.setText(node.getText());
 
 		bool.setStart(getTokenOffset(node.getTokenStartIndex()));
 		bool.setEnd(getTokenOffset(node.getTokenStartIndex() + 1));
 
 		return bool;
 	}
 
 	@Override
 	protected ASTNode visitVoid(Tree node) {
 		VoidOperator expression = new VoidOperator(getParent());
 
 		expression.setVoidKeyword(createKeyword(node, Keywords.VOID));
 
 		expression.setExpression((Expression) transformNode(node.getChild(0),
 				expression));
 
 		expression.setStart(expression.getVoidKeyword().sourceStart());
 		expression.setEnd(expression.getExpression().sourceEnd());
 
 		return expression;
 	}
 
 	@Override
 	protected ASTNode visitXmlLiteral(Tree node) {
 		final XmlLiteral xml = new XmlLiteral(getParent());
 		final List<XmlFragment> fragments = new ArrayList<XmlFragment>();
 		for (int i = 0; i < node.getChildCount(); ++i) {
 			final Tree child = node.getChild(i);
 			if (child.getType() == JSParser.XMLFragment
 					|| child.getType() == JSParser.XMLFragmentEnd) {
 				final XmlTextFragment fragment = new XmlTextFragment(xml);
 				fragment.setStart(getTokenOffset(child.getTokenStartIndex()));
 				fragment.setEnd(getTokenOffset(child.getTokenStopIndex() + 1));
 				fragment.setXml(child.getText());
 				fragments.add(fragment);
 			} else {
 				XmlExpressionFragment fragment = new XmlExpressionFragment(xml);
 				Expression expression = (Expression) transformNode(child,
 						fragment);
 				fragment.setExpression(expression);
 				fragment.setStart(expression.sourceStart());
 				fragment.setEnd(expression.sourceEnd());
 				fragments.add(fragment);
 				// TODO curly braces
 			}
 		}
 		if (fragments.size() > 1) {
 			Collections.sort(fragments, new Comparator<XmlFragment>() {
 				public int compare(XmlFragment o1, XmlFragment o2) {
 					return o1.sourceStart() - o2.sourceStart();
 				}
 			});
 		}
 		xml.setFragments(fragments);
 		xml.setStart(getTokenOffset(node.getTokenStartIndex()));
 		xml.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		return xml;
 	}
 
 	@Override
 	protected ASTNode visitNamespace(Tree node) {
 
 		DefaultXmlNamespaceStatement statement = new DefaultXmlNamespaceStatement(
 				getParent());
 
 		statement.setDefaultKeyword(createKeyword(node.getChild(0),
 				Keywords.DEFAULT));
 		statement.setXmlKeyword(createKeyword(node.getChild(1), Keywords.XML));
 
 		Keyword namespaceKeyword = new Keyword(Keywords.NAMESPACE);
 		namespaceKeyword.setStart(getTokenOffset(JSParser.NAMESPACE, node
 				.getTokenStartIndex(), node.getTokenStopIndex()));
 		namespaceKeyword.setEnd(namespaceKeyword.sourceStart()
 				+ Keywords.NAMESPACE.length());
 		statement.setNamespaceKeyword(namespaceKeyword);
 
 		statement.setAssignOperation(getTokenOffset(node.getChild(2)
 				.getTokenStartIndex()));
 
 		StringLiteral value = new StringLiteral(statement);
 		value.setStart(getTokenOffset(node.getChild(3).getTokenStartIndex()));
 		value.setEnd(getTokenOffset(node.getChild(3).getTokenStartIndex()) + 1);
 		value.setText(node.getChild(3).getText());
 		statement.setValue(value);
 
 		Token token = tokens.get(node.getTokenStopIndex());
 		if (token.getType() == JSParser.SEMIC) {
 			statement.setSemicolonPosition(getTokenOffset(node
 					.getTokenStopIndex()));
 
 			statement.setEnd(statement.getSemicolonPosition() + 1);
 		} else {
 			statement.setEnd(statement.getValue().sourceEnd());
 		}
 		statement.setStart(statement.getDefaultKeyword().sourceStart());
 
 		return statement;
 	}
 
 	@Override
 	protected ASTNode visitXmlAttribute(Tree node) {
 
 		XmlAttributeIdentifier id = new XmlAttributeIdentifier(getParent());
 		final Expression expression = (Expression) transformNode(node
 				.getChild(1), id);
 		id.setExpression(expression);
 
 		id.setStart(getTokenOffset(node.getTokenStartIndex()));
 		id.setEnd(expression.sourceEnd());
 
 		return id;
 	}
 
 	protected ASTNode visitAsterisk(Tree node) {
 		AsteriskExpression asterisk = new AsteriskExpression(getParent());
 
 		asterisk.setStart(getTokenOffset(node.getTokenStartIndex()));
 		asterisk.setEnd(asterisk.sourceStart() + node.getText().length());
 
 		return asterisk;
 	}
 
 	@Override
 	protected ASTNode visitGetAllChildren(Tree node) {
 		GetAllChildrenExpression expression = new GetAllChildrenExpression(
 				getParent());
 
 		expression.setObject((Expression) transformNode(node.getChild(0),
 				expression));
 
 		expression.setProperty((Expression) transformNode(node.getChild(1),
 				expression));
 
 		expression.setDotDotPosition(getTokenOffset(JSParser.DOTDOT,
 				getRealTokenStopIndex(node.getChild(0)) + 1, node.getChild(1)
 						.getTokenStartIndex()));
 
 		Assert.isTrue(expression.getObject().sourceStart() >= 0);
 		Assert.isTrue(expression.getProperty().sourceEnd() > 0);
 
 		expression.setStart(expression.getObject().sourceStart());
 		expression.setEnd(expression.getProperty().sourceEnd());
 
 		return expression;
 	}
 
 	@Override
 	protected ASTNode visitGetLocalName(Tree node) {
 		GetLocalNameExpression expression = new GetLocalNameExpression(
 				getParent());
 
 		expression.setNamespace((Expression) transformNode(node.getChild(0),
 				expression));
 
 		expression.setLocalName((Expression) transformNode(node.getChild(1),
 				expression));
 
 		expression.setColonColonPosition(getTokenOffset(JSParser.COLONCOLON,
 				getRealTokenStopIndex(node.getChild(0)) + 1, node.getChild(1)
 						.getTokenStartIndex()));
 
 		Assert.isTrue(expression.getNamespace().sourceStart() >= 0);
 		Assert.isTrue(expression.getLocalName().sourceEnd() > 0);
 
 		expression.setStart(expression.getNamespace().sourceStart());
 		expression.setEnd(expression.getLocalName().sourceEnd());
 
 		return expression;
 	}
 
 	@Override
 	protected ASTNode visitHexIntegerLiteral(Tree node) {
 		DecimalLiteral number = new DecimalLiteral(getParent());
 		number.setText(node.getText());
 		number.setStart(getTokenOffset(node.getTokenStartIndex()));
 		number.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		return number;
 	}
 
 	@Override
 	protected ASTNode visitOctalIntegerLiteral(Tree node) {
 		DecimalLiteral number = new DecimalLiteral(getParent());
 		number.setText(node.getText());
 		number.setStart(getTokenOffset(node.getTokenStartIndex()));
 		number.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		return number;
 	}
 
 	@Override
 	protected ASTNode visitYield(Tree node) {
 		YieldOperator expression = new YieldOperator(getParent());
 
 		expression.setYieldKeyword(createKeyword(node, Keywords.YIELD));
 
 		expression.setExpression((Expression) transformNode(node.getChild(0),
 				expression));
 
 		expression.setStart(expression.getYieldKeyword().sourceStart());
 		expression.setEnd(expression.getExpression().sourceEnd());
 
 		return expression;
 	}
 
 	@Override
 	protected ASTNode visitEmptyStatement(Tree node) {
 		final EmptyStatement statement = new EmptyStatement(getParent());
 		statement.setStart(getTokenOffset(node.getTokenStartIndex()));
 		statement.setEnd(getTokenOffset(node.getTokenStartIndex() + 1));
 		return statement;
 	}
 
 }
