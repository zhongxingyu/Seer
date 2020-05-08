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
 
 package org.eclipse.dltk.javascript.internal.parser.tests;
 
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
 import org.eclipse.dltk.javascript.ast.ASTVisitor;
 import org.eclipse.dltk.javascript.ast.ArrayInitializer;
 import org.eclipse.dltk.javascript.ast.AsteriskExpression;
 import org.eclipse.dltk.javascript.ast.BinaryOperation;
 import org.eclipse.dltk.javascript.ast.BooleanLiteral;
 import org.eclipse.dltk.javascript.ast.BreakStatement;
 import org.eclipse.dltk.javascript.ast.CallExpression;
 import org.eclipse.dltk.javascript.ast.CaseClause;
 import org.eclipse.dltk.javascript.ast.CatchClause;
 import org.eclipse.dltk.javascript.ast.CommaExpression;
 import org.eclipse.dltk.javascript.ast.ConditionalOperator;
 import org.eclipse.dltk.javascript.ast.ConstDeclaration;
 import org.eclipse.dltk.javascript.ast.ContinueStatement;
 import org.eclipse.dltk.javascript.ast.DecimalLiteral;
 import org.eclipse.dltk.javascript.ast.DefaultClause;
 import org.eclipse.dltk.javascript.ast.DefaultXmlNamespaceStatement;
 import org.eclipse.dltk.javascript.ast.DeleteStatement;
 import org.eclipse.dltk.javascript.ast.DoWhileStatement;
 import org.eclipse.dltk.javascript.ast.EmptyExpression;
 import org.eclipse.dltk.javascript.ast.ExceptionFilter;
 import org.eclipse.dltk.javascript.ast.FinallyClause;
 import org.eclipse.dltk.javascript.ast.ForEachInStatement;
 import org.eclipse.dltk.javascript.ast.ForInStatement;
 import org.eclipse.dltk.javascript.ast.ForStatement;
 import org.eclipse.dltk.javascript.ast.FunctionStatement;
 import org.eclipse.dltk.javascript.ast.GetAllChildrenExpression;
 import org.eclipse.dltk.javascript.ast.GetArrayItemExpression;
 import org.eclipse.dltk.javascript.ast.GetLocalNameExpression;
 import org.eclipse.dltk.javascript.ast.GetMethod;
 import org.eclipse.dltk.javascript.ast.Identifier;
 import org.eclipse.dltk.javascript.ast.IfStatement;
 import org.eclipse.dltk.javascript.ast.Keyword;
 import org.eclipse.dltk.javascript.ast.Keywords;
 import org.eclipse.dltk.javascript.ast.Label;
 import org.eclipse.dltk.javascript.ast.LabelledStatement;
 import org.eclipse.dltk.javascript.ast.NewExpression;
 import org.eclipse.dltk.javascript.ast.NullExpression;
 import org.eclipse.dltk.javascript.ast.ObjectInitializer;
 import org.eclipse.dltk.javascript.ast.ParenthesizedExpression;
 import org.eclipse.dltk.javascript.ast.PropertyExpression;
 import org.eclipse.dltk.javascript.ast.PropertyInitializer;
 import org.eclipse.dltk.javascript.ast.RegExpLiteral;
 import org.eclipse.dltk.javascript.ast.ReturnStatement;
 import org.eclipse.dltk.javascript.ast.Script;
 import org.eclipse.dltk.javascript.ast.SetMethod;
 import org.eclipse.dltk.javascript.ast.StatementBlock;
 import org.eclipse.dltk.javascript.ast.StringLiteral;
 import org.eclipse.dltk.javascript.ast.SwitchStatement;
 import org.eclipse.dltk.javascript.ast.ThisExpression;
 import org.eclipse.dltk.javascript.ast.ThrowStatement;
 import org.eclipse.dltk.javascript.ast.TryStatement;
 import org.eclipse.dltk.javascript.ast.TypeOfExpression;
 import org.eclipse.dltk.javascript.ast.UnaryOperation;
 import org.eclipse.dltk.javascript.ast.VariableDeclaration;
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
 
 public class ASTVerifier extends ASTVisitor {
 
 	private String source;
 	private ModuleDeclaration root;
 
 	public ASTVerifier(ModuleDeclaration root, String source) {
 		this.root = root;
 		this.source = source;
 	}
 
 	public void verify() {
 		visit(root);
 	}
 
 	private void testChar(char ch, int charAt) {
 		Assert.assertEquals(ch, source.charAt(charAt));
 	}
 
 	private void testCharIfExists(char ch, int charAt) {
 
 		if (charAt > -1)
 			Assert.assertEquals(ch, source.charAt(charAt));
 	}
 
 	private void testCharList(char ch, List<Integer> charIndexes) {
 		for (int i = 0; i < charIndexes.size(); i++) {
 			int charAt = charIndexes.get(i).intValue();
 			Assert.assertTrue(charAt > -1);
 			testChar(ch, charAt);
 		}
 	}
 
 	private void testString(String text, int start, int end) {
 
 		Assert.assertTrue(start >= 0);
 		Assert.assertTrue(end > 0);
 		Assert.assertTrue(end > start);
 
 		Assert.assertEquals(text, source.substring(start, end));
 	}
 
 	public boolean visitArrayInitializer(ArrayInitializer node) {
 
 		testCharList(Keywords.COMMA, node.getCommas());
 		visit(node.getItems());
 		testChar(Keywords.LB, node.getLB());
 		testChar(Keywords.RB, node.getRB());
 
 		return true;
 	}
 
 	public boolean visitBinaryOperation(BinaryOperation node) {
 
 		testString(node.getOperationText(), node.getOperationPosition(), node
 				.getOperationPosition()
 				+ node.getOperationText().length());
 
 		visit(node.getLeftExpression());
 		visit(node.getRightExpression());
 
 		return true;
 	}
 
 	public boolean visitBooleanLiteral(BooleanLiteral node) {
 
 		testString(node.getText(), node.sourceStart(), node.sourceEnd());
 
 		return true;
 	}
 
 	public boolean visitBreakStatement(BreakStatement node) {
 
 		visit(node.getBreakKeyword());
 
 		testChar(Keywords.SEMI, node.getSemicolonPosition());
 
 		return true;
 	}
 
 	public boolean visitCallExpression(CallExpression node) {
 
 		visit(node.getExpression());
 		visit(node.getArguments());
 
 		testChar(Keywords.LP, node.getLP());
 		testChar(Keywords.RP, node.getRP());
 		testCharList(Keywords.COMMA, node.getCommas());
 
 		return true;
 	}
 
 	public boolean visitCaseClause(CaseClause node) {
 
 		visit(node.getCaseKeyword());
 		visit(node.getCondition());
 		visit(node.getStatements());
 
 		testChar(Keywords.COLON, node.getColonPosition());
 
 		return true;
 	}
 
 	public boolean visitCatchClause(CatchClause node) {
 
 		visit(node.getCatchKeyword());
 		visit(node.getException());
 		if (node.getExceptionFilter() != null)
 			visit(node.getExceptionFilter());
 		visit(node.getStatement());
 
 		testChar(Keywords.LP, node.getLP());
 		testChar(Keywords.RP, node.getRP());
 
 		return true;
 	}
 
 	public boolean visitCommaExpression(CommaExpression node) {
 
 		testCharList(Keywords.COMMA, node.getCommas());
 		visit(node.getItems());
 
 		return true;
 	}
 
 	public boolean visitConditionalOperator(ConditionalOperator node) {
 
 		visit(node.getCondition());
 		visit(node.getTrueValue());
 		visit(node.getFalseValue());
 
 		testChar(Keywords.COLON, node.getColonPosition());
 		testChar(Keywords.HOOK, node.getQuestionPosition());
 
 		return true;
 	}
 
 	public boolean visitConstDeclaration(ConstDeclaration node) {
 
 		testCharList(Keywords.COMMA, node.getCommas());
 		visit(node.getConstKeyword());
 		visit(node.getConsts());
 
 		testChar(Keywords.SEMI, node.getSemicolonPosition());
 
 		return true;
 	}
 
 	public boolean visitContinueStatement(ContinueStatement node) {
 
 		visit(node.getContinueKeyword());
 
 		if (node.getLabel() != null)
 			visit(node.getLabel());
 
 		testCharIfExists(Keywords.SEMI, node.getSemicolonPosition());
 
 		return true;
 	}
 
 	public boolean visitDecimalLiteral(DecimalLiteral node) {
 
 		testString(node.getText(), node.sourceStart(), node.sourceEnd());
 
 		return true;
 	}
 
 	public boolean visitDefaultClause(DefaultClause node) {
 
 		testChar(Keywords.COLON, node.getColonPosition());
 
 		visit(node.getDefaultKeyword());
 		visit(node.getStatements());
 
 		return true;
 	}
 
 	public boolean visitDeleteStatement(DeleteStatement node) {
 
 		visit(node.getDeleteKeyword());
 		visit(node.getExpression());
 
 		return true;
 	}
 
 	public boolean visitDoWhileStatement(DoWhileStatement node) {
 
 		visit(node.getDoKeyword());
 		visit(node.getBody());
 		visit(node.getWhileKeyword());
 		visit(node.getCondition());
 
 		testChar(Keywords.LP, node.getLP());
 		testChar(Keywords.RP, node.getRP());
 		testChar(Keywords.SEMI, node.getSemicolonPosition());
 
 		return true;
 	}
 
 	public boolean visitEmptyExpression(EmptyExpression node) {
 
 		// Assert.assertTrue(node.sourceStart() < 0);
 		// Assert.assertTrue(node.sourceEnd() < 0);
 
 		return true;
 	}
 
 	public boolean visitExceptionFilter(ExceptionFilter node) {
 
 		visit(node.getIfKeyword());
 		visit(node.getExpression());
 
 		return true;
 	}
 
 	public boolean visitFinallyClause(FinallyClause node) {
 
 		visit(node.getFinallyKeyword());
 		visit(node.getStatement());
 
 		return true;
 	}
 
 	public boolean visitForEachInStatement(ForEachInStatement node) {
 
 		visit(node.getForKeyword());
 		visit(node.getEachKeyword());
 
 		if (node.getBody() != null)
 			node.getBody();
 
 		visit(node.getItem());
 		visit(node.getInKeyword());
 		visit(node.getIterator());
 
 		testChar(Keywords.LP, node.getLP());
 		testChar(Keywords.RP, node.getRP());
 		testCharIfExists(Keywords.SEMI, node.getSemicolonPosition());
 
 		return true;
 	}
 
 	public boolean visitGetArrayItemExpression(GetArrayItemExpression node) {
 
 		visit(node.getArray());
 		visit(node.getIndex());
 
 		testChar(Keywords.LB, node.getLB());
 		testChar(Keywords.RB, node.getRB());
 
 		return true;
 	}
 
 	public boolean visitGetMethod(GetMethod node) {
 
 		visit(node.getName());
 		visit(node.getGetKeyword());
 		visit(node.getBody());
 		testChar(Keywords.LP, node.getLP());
 		testChar(Keywords.RP, node.getRP());
 
 		return true;
 	}
 
 	public boolean visitIdentifier(Identifier node) {
 
 		testString(node.getName(), node.sourceStart(), node.sourceEnd());
 
 		return true;
 	}
 
 	public boolean visitIfStatement(IfStatement node) {
 
 		visit(node.getIfKeyword());
 		visit(node.getCondition());
 		visit(node.getThenStatement());
 		if (node.getElseStatement() != null) {
 			visit(node.getElseKeyword());
 			visit(node.getElseStatement());
 		}
 		testChar(Keywords.LP, node.getLP());
 		testChar(Keywords.RP, node.getRP());
 
 		return true;
 	}
 
 	public boolean visitKeyword(Keyword node) {
 
 		testString(node.getKeyword(), node.sourceStart(), node.sourceEnd());
 
 		return true;
 	}
 
 	public boolean visitLabel(Label node) {
 
 		testString(node.getText(), node.sourceStart(), node.sourceEnd());
 
 		return true;
 	}
 
 	public boolean visitLabelledStatement(LabelledStatement node) {
 
 		testChar(Keywords.COLON, node.getColonPosition());
 
 		visit(node.getLabel());
 		visit(node.getStatement());
 
 		return true;
 	}
 
 	public boolean visitNewExpression(NewExpression node) {
 
 		visit(node.getNewKeyword());
 		visit(node.getObjectClass());
 
 		return true;
 	}
 
 	public boolean visitNullExpression(NullExpression node) {
 
 		testString(node.getText(), node.sourceStart(), node.sourceEnd());
 
 		return true;
 	}
 
 	public boolean visitObjectInitializer(ObjectInitializer node) {
 
 		testCharList(Keywords.COMMA, node.getCommas());
 		visit(node.getInitializers());
 
 		testChar(Keywords.LC, node.getLC());
 		testChar(Keywords.RC, node.getRC());
 
 		return true;
 	}
 
 	public boolean visitParenthesizedExpression(ParenthesizedExpression node) {
 
 		visit(node.getExpression());
 
 		testChar(Keywords.LP, node.getLP());
 		testChar(Keywords.RP, node.getRP());
 
 		return true;
 	}
 
 	public boolean visitPropertyExpression(PropertyExpression node) {
 
 		testChar(Keywords.DOT, node.getDotPosition());
 
 		visit(node.getObject());
 		visit(node.getProperty());
 
 		return true;
 	}
 
 	public boolean visitPropertyInitializer(PropertyInitializer node) {
 
 		testChar(Keywords.COLON, node.getColon());
 
 		visit(node.getName());
 		visit(node.getValue());
 
 		return true;
 	}
 
 	public boolean visitRegExpLiteral(RegExpLiteral node) {
 
 		testString(node.getText(), node.sourceStart(), node.sourceEnd());
 
 		return true;
 	}
 
 	public boolean visitReturnStatement(ReturnStatement node) {
 
 		visitKeyword(node.getReturnKeyword());
 
 		if (node.getValue() != null)
 			visit(node.getValue());
 
 		testCharIfExists(Keywords.SEMI, node.getSemicolonPosition());
 
 		return true;
 	}
 
 	public boolean visitScript(Script node) {
 
 		Assert.assertEquals(0, node.sourceStart());
 		Assert.assertEquals(source.length(), node.sourceEnd());
 
 		visit(node.getStatements());
 		return true;
 	}
 
 	public boolean visitSetMethod(SetMethod node) {
 
 		visit(node.getSetKeyword());
 		visit(node.getName());
 		visit(node.getArgument());
 		visit(node.getBody());
 
 		testChar(Keywords.LP, node.getLP());
 		testChar(Keywords.RP, node.getRP());
 
 		return true;
 	}
 
 	public boolean visitStatementBlock(StatementBlock node) {
 
 		testChar(Keywords.LC, node.getLC());
 		testChar(Keywords.RC, node.getRC());
 
 		visit(node.getStatements());
 
 		return true;
 	}
 
 	public boolean visitStringLiteral(StringLiteral node) {
 
 		testString(node.getText(), node.sourceStart(), node.sourceEnd());
 
 		return true;
 	}
 
 	public boolean visitSwitchStatement(SwitchStatement node) {
 
 		visit(node.getCondition());
 		visit(node.getCaseClauses());
 
 		testChar(Keywords.LC, node.getLC());
 		testChar(Keywords.RC, node.getRC());
 		testChar(Keywords.LP, node.getLP());
 		testChar(Keywords.RP, node.getRP());
 
 		return true;
 	}
 
 	public boolean visitThisExpression(ThisExpression node) {
 
 		visitKeyword(node.getThisKeyword());
 
 		return true;
 	}
 
 	public boolean visitThrowStatement(ThrowStatement node) {
 
 		visit(node.getThrowKeyword());
 		visit(node.getException());
 
 		if (node.getSemicolonPosition() > 0)
 			testChar(Keywords.SEMI, node.getSemicolonPosition());
 
 		return true;
 	}
 
 	public boolean visitTryStatement(TryStatement node) {
 
 		visit(node.getTryKeyword());
 		visit(node.getBody());
 		visit(node.getCatches());
 
 		if (node.getFinally() != null)
 			visit(node.getFinally());
 
 		return true;
 	}
 
 	public boolean visitTypeOfExpression(TypeOfExpression node) {
 
 		visit(node.getExpression());
 		visit(node.getTypeOfKeyword());
 
 		return true;
 	}
 
 	public boolean visitUnaryOperation(UnaryOperation node) {
 
 		visit(node.getExpression());
 
 		testString(node.getOperationText(), node.getOperationPosition(), node
 				.getOperationPosition()
 				+ node.getOperationText().length());
 
 		return true;
 	}
 
 	public boolean visitVariableDeclaration(VariableDeclaration node) {
 
 		visit(node.getVarKeyword());
 		visit(node.getVariables());
 		testCharList(Keywords.COMMA, node.getCommas());
 
 		return true;
 	}
 
 	public boolean visitVoidExpression(VoidExpression node) {
 
 		testCharIfExists(Keywords.SEMI, node.getSemicolonPosition());
 
 		visit(node.getExpression());
 
 		return true;
 	}
 
 	public boolean visitWhileStatement(WhileStatement node) {
 
 		visit(node.getWhileKeyword());
 		visit(node.getCondition());
 
 		if (node.getBody() != null)
 			visit(node.getBody());
 
 		testChar(Keywords.LP, node.getLP());
 		testChar(Keywords.RP, node.getRP());
 
 		return true;
 	}
 
 	public boolean visitWithStatement(WithStatement node) {
 
 		testChar(Keywords.LP, node.getLP());
 		testChar(Keywords.RP, node.getRP());
 
 		visit(node.getWithKeyword());
 		visit(node.getExpression());
 		visit(node.getStatement());
 
 		return true;
 	}
 
 	public boolean visitForInStatement(ForInStatement node) {
 
 		visit(node.getForKeyword());
 
 		if (node.getBody() != null)
 			node.getBody();
 
 		visit(node.getItem());
 		visit(node.getInKeyword());
 		visit(node.getIterator());
 
 		testChar(Keywords.LP, node.getLP());
 		testChar(Keywords.RP, node.getRP());
 		testCharIfExists(Keywords.SEMI, node.getSemicolonPosition());
 
 		return true;
 	}
 
 	public boolean visitFunctionStatement(FunctionStatement node) {
 
 		visit(node.getArguments());
 		visit(node.getBody());
 		visit(node.getFunctionKeyword());
 
 		if (node.getName() != null)
 			visit(node.getName());
 
 		testChar(Keywords.LP, node.getLP());
 		testChar(Keywords.RP, node.getRP());
 		testCharList(Keywords.COMMA, node.getArgumentCommas());
 
 		return true;
 	}
 
 	public boolean visitForStatement(ForStatement node) {
 
 		visit(node.getForKeyword());
 
 		visit(node.getInitial());
 		visit(node.getCondition());
 		visit(node.getStep());
 
 		if (node.getBody() != null)
 			visit(node.getBody());
 
 		testChar(Keywords.SEMI, node.getConditionalSemicolonPosition());
 		testChar(Keywords.SEMI, node.getInitialSemicolonPosition());
 		testCharIfExists(Keywords.SEMI, node.getSemicolonPosition());
 		testChar(Keywords.LP, node.getLP());
 		testChar(Keywords.RP, node.getRP());
 
 		return true;
 	}
 
 	public boolean visitVoidOperator(VoidOperator node) {
 		visit(node.getExpression());
 		visit(node.getVoidKeyword());
 
 		return true;
 	}
 
 	public boolean visitXmlLiteral(XmlLiteral node) {
		for (XmlFragment fragment : node.getFragments()) {
			if (fragment instanceof XmlTextFragment) {
				XmlTextFragment textFragment = (XmlTextFragment) fragment;
				testString(textFragment.getXml(), fragment.sourceStart(),
						fragment.sourceEnd());
			} else {
				XmlExpressionFragment expression = (XmlExpressionFragment) fragment;
				visit(expression.getExpression());
				// TODO curly braces
			}
		}
 		return true;
 	}
 
 	public boolean visitDefaultXmlNamespace(DefaultXmlNamespaceStatement node) {
 
 		visit(node.getDefaultKeyword());
 		visit(node.getXmlKeyword());
 		visit(node.getNamespaceKeyword());
 
 		testChar(Keywords.ASSIGN, node.getAssignOperation());
 		testCharIfExists(Keywords.SEMI, node.getSemicolonPosition());
 
 		return true;
 	}
 
 	public boolean visitAsteriskExpression(AsteriskExpression node) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public boolean visitGetAllChildrenExpression(GetAllChildrenExpression node) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public boolean visitGetLocalNameExpression(GetLocalNameExpression node) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public boolean visitXmlPropertyIdentifier(XmlAttributeIdentifier node) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public boolean visitYieldOperator(YieldOperator node) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public boolean visitUnknownNode(ASTNode node) {
 		return false;
 	}
 }
