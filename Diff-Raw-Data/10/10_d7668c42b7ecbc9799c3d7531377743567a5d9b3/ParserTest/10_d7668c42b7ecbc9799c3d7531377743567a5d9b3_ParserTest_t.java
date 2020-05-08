 /*******************************************************************************
  * Copyright (c) 2008 xored software, Inc.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.ruby.formatter.tests;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.dltk.compiler.util.Util;
 import org.eclipse.dltk.formatter.nodes.FormatterDocument;
 import org.eclipse.dltk.formatter.nodes.IFormatterContainerNode;
 import org.eclipse.dltk.formatter.nodes.IFormatterDocument;
 import org.eclipse.dltk.formatter.nodes.IFormatterNode;
 import org.eclipse.dltk.ruby.formatter.internal.RubyFormatterNodeBuilder;
 import org.eclipse.dltk.ruby.formatter.internal.RubyParser;
 import org.eclipse.dltk.ruby.formatter.internal.nodes.FormatterBlockWithBeginEndNode;
import org.eclipse.dltk.ui.formatter.FormatterException;
 import org.jruby.ast.ArgumentNode;
 import org.jruby.ast.ListNode;
 import org.jruby.ast.Node;
 import org.jruby.ast.ext.HeredocNode;
 import org.jruby.ast.visitor.AbstractVisitor;
 import org.jruby.evaluator.Instruction;
 import org.jruby.lexer.yacc.ISourcePosition;
 import org.jruby.parser.RubyParserResult;
 
 public class ParserTest extends AbstractFormatterTest {
 
	public void testEndKeyword() throws FormatterException {
 		final String input = "class Test" + Util.LINE_SEPARATOR + "end"
 				+ Util.LINE_SEPARATOR;
 		final RubyParserResult result = RubyParser.parse(input);
 		assertNotNull(result);
 		final RubyFormatterNodeBuilder builder = new RubyFormatterNodeBuilder();
 		final IFormatterDocument document = new FormatterDocument(input);
 		final IFormatterContainerNode root = builder.build(result, document);
 		assertNotNull(root);
 		List children = root.getChildren();
 		assertEquals(2, children.size());
 		final FormatterBlockWithBeginEndNode classNode = (FormatterBlockWithBeginEndNode) children
 				.get(0);
 		IFormatterNode endNode = classNode.getEnd();
 		assertEquals(3, endNode.getEndOffset() - endNode.getStartOffset());
 		assertEquals(input.indexOf("end"), endNode.getStartOffset());
 	}
 
	public void testEmptyHereDoc() throws FormatterException {
 		final String input = "a = <<THIS" + Util.LINE_SEPARATOR + "THIS"
 				+ Util.LINE_SEPARATOR;
 		RubyParserResult result = RubyParser.parse(input);
 		assertNotNull(result);
 	}
 
	public void testHereDocParser() throws FormatterException {
 		final String id1 = "THIS";
 		final String[] hereDoc1 = new String[] { "Line 1=a", "Line 2" };
 		final String id2 = "THAT";
 		final String[] hereDoc2 = new String[] { "Line 3=b", "Line 4" };
 		String input = createInput(id1, hereDoc1, id2, hereDoc2);
 		RubyParserResult result = RubyParser.parse(input);
 		assertNotNull(result);
 		final List heredocNodes = selectHeredocNodes(result);
 		assertEquals(2, heredocNodes.size());
 		final HeredocNode doc1 = (HeredocNode) heredocNodes.get(0);
 		assertTrue(substring(input, doc1.getEndMarkerPosition()).indexOf(id1) >= 0);
 		final String content1 = substring(input, doc1.getContentPosition());
 		assertTrue(content1.indexOf(id1) < 0);
 		assertTrue(content1.indexOf(hereDoc1[0]) >= 0);
 		assertTrue(content1.indexOf(hereDoc1[1]) >= 0);
 		final HeredocNode doc2 = (HeredocNode) heredocNodes.get(1);
 		assertTrue(substring(input, doc2.getEndMarkerPosition()).indexOf(id2) >= 0);
 		final String content2 = substring(input, doc2.getContentPosition());
 		assertTrue(content2.indexOf(id2) < 0);
 		assertTrue(content2.indexOf(hereDoc2[0]) >= 0);
 		assertTrue(content2.indexOf(hereDoc2[1]) >= 0);
 	}
 
 	/**
 	 * @param input
 	 * @param position
 	 */
 	private String substring(String input, ISourcePosition position) {
 		return input.substring(position.getStartOffset(), position
 				.getEndOffset());
 	}
 
 	private String createInput(final String id1, final String[] hereDoc1,
 			final String id2, final String[] hereDoc2) {
 		List lines = new ArrayList();
 		lines.add("def myfunc");
 		lines.add("print <<\"" + id1 + "\", <<\"" + id2
 				+ "\",1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19");
 		lines.addAll(Arrays.asList(hereDoc1));
 		lines.add(id1);
 		lines.addAll(Arrays.asList(hereDoc2));
 		lines.add(id2);
 		lines.add("end");
 		String input = joinLines(lines);
 		return input;
 	}
 
 	private List selectHeredocNodes(RubyParserResult result) {
 		final List heredocNodes = new ArrayList();
 		result.getAST().accept(new AbstractVisitor() {
 
 			protected Instruction visitNode(Node visited) {
 				if (visited instanceof HeredocNode) {
 					heredocNodes.add(visited);
 				}
 				visitChildren(visited);
 				return null;
 			}
 
 			private void visitChildren(Node visited) {
 				List children = visited.childNodes();
 				for (Iterator i = children.iterator(); i.hasNext();) {
 					final Node child = (Node) i.next();
 					visitChild(child);
 				}
 			}
 
 			private void visitChild(final Node child) {
 				if (child != null && isVisitable(child)) {
 					child.accept(this);
 				}
 			}
 
 			private boolean isVisitable(Node node) {
 				return !(node instanceof ArgumentNode)
 						&& node.getClass() != ListNode.class;
 			}
 
 		});
 		return heredocNodes;
 	}
 
 }
