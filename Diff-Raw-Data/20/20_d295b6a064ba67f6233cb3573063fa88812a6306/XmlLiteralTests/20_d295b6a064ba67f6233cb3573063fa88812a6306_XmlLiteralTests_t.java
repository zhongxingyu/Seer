 /*******************************************************************************
  * Copyright (c) 2010 xored software, Inc.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.javascript.parser.tests;
 
 import java.util.List;
 
 import org.eclipse.dltk.ast.utils.ASTUtil;
import org.eclipse.dltk.core.tests.util.StringList;
 import org.eclipse.dltk.javascript.ast.BinaryOperation;
 import org.eclipse.dltk.javascript.ast.CallExpression;
 import org.eclipse.dltk.javascript.ast.Expression;
 import org.eclipse.dltk.javascript.ast.Identifier;
 import org.eclipse.dltk.javascript.ast.PropertyExpression;
 import org.eclipse.dltk.javascript.ast.Script;
 import org.eclipse.dltk.javascript.ast.StatementBlock;
 import org.eclipse.dltk.javascript.ast.VariableDeclaration;
 import org.eclipse.dltk.javascript.ast.VariableStatement;
 import org.eclipse.dltk.javascript.ast.WhileStatement;
 import org.eclipse.dltk.javascript.ast.XmlExpressionFragment;
 import org.eclipse.dltk.javascript.ast.XmlLiteral;
 import org.eclipse.dltk.javascript.ast.XmlTextFragment;
 
 @SuppressWarnings("nls")
 public class XmlLiteralTests extends AbstractJSParserTest {
 
 	public void testNoXmlLiteral() {
 		final Script script = parse("while (a<b.c) { advance(); }");
 		assertFalse(reporter.hasErrors());
 		final List<?> statements = script.getStatements();
 		assertEquals(1, statements.size());
 		final WhileStatement whileStatement = (WhileStatement) statements
 				.get(0);
 		final BinaryOperation compare = (BinaryOperation) whileStatement
 				.getCondition();
 		assertEquals("a", ((Identifier) compare.getLeftExpression()).getName());
 		final PropertyExpression right = (PropertyExpression) compare
 				.getRightExpression();
 		assertEquals("b", ((Identifier) right.getObject()).getName());
 		assertEquals("c", ((Identifier) right.getProperty()).getName());
 		final StatementBlock block = (StatementBlock) whileStatement.getBody();
 		assertEquals(1, block.getStatements().size());
 		final CallExpression call = uniqueResult(ASTUtil.select(block,
 				CallExpression.class));
 		assertTrue(call.getArguments().isEmpty());
 		assertEquals("advance", ((Identifier) call.getExpression()).getName());
 	}
 
 	public void testXmlLiteralSimple() {
 		parse("var x = <xml/>;");
 		assertFalse(reporter.hasErrors());
 	}
 
 	public void testXmlLiteralTextContent() {
 		parse("var x = <xml>CONTENT</xml>;");
 		assertFalse(reporter.hasErrors());
 	}
 
 	public void testXmlLiteralAttributes() {
 		parse("var x = <xml a='1' b='2'>CONTENT</xml>;");
 		assertFalse(reporter.hasErrors());
 	}
 
 	public void testXmlLiteralEmptyTagName() {
 		parse("var x = <></>;");
 		assertFalse(reporter.hasErrors());
 	}
 
	public void testXmlLiteralExtraRightBrace() {
		final StringList code = new StringList();
		code.add("var TOC = <xml>");
		code.add("<entry label={homePageTitle}/>");
		code.add("</xml>");
		code.add("}");
		code.add("");

		parse(code.toString());
		assertTrue(reporter.hasErrors());
	}

 	public void testXmlLiteralExpressions() throws Exception {
 		Script script = parse("var x = <xml>{value}</xml>;");
 		assertFalse(reporter.getErrors().toString(), reporter.hasErrors());
 		List<?> statements = script.getStatements();
 		assertEquals(1, statements.size());
 		final List<VariableStatement> declarations = ASTUtil.select(script,
 				VariableStatement.class);
 		assertEquals(1, declarations.size());
 		final List<VariableDeclaration> vars = declarations.get(0)
 				.getVariables();
 		assertEquals(1, vars.size());
 		VariableDeclaration var0 = vars.get(0);
 		assertEquals("x", var0.getIdentifier().getName());
 		final XmlLiteral literal = (XmlLiteral) var0.getInitializer();
 		assertEquals(3, literal.getFragments().size());
 		assertTrue(literal.getFragments().get(0) instanceof XmlTextFragment);
 		assertTrue(literal.getFragments().get(1) instanceof XmlExpressionFragment);
 		assertTrue(literal.getFragments().get(2) instanceof XmlTextFragment);
 		assertEquals("<xml>",
 				((XmlTextFragment) literal.getFragments().get(0)).getXml());
 		Expression expression = ((XmlExpressionFragment) literal.getFragments()
 				.get(1)).getExpression();
 		assertTrue(expression instanceof Identifier);
 		assertEquals("value", ((Identifier) expression).getName());
 		assertEquals("</xml>",
 				((XmlTextFragment) literal.getFragments().get(2)).getXml());
 	}
 }
