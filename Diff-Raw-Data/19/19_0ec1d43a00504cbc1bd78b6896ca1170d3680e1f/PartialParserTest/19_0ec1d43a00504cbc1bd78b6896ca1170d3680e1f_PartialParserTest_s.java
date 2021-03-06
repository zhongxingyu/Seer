 /*******************************************************************************
  * Copyright (c) 2008 itemis AG (http://www.itemis.eu) and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.xtext.parser;
 
 import java.util.Collections;
 import java.util.Iterator;
 
 import org.eclipse.emf.common.util.AbstractTreeIterator;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.xtext.ParserRule;
 import org.eclipse.xtext.parser.impl.PartialParsingPointers;
 import org.eclipse.xtext.parser.impl.PartialParsingUtil;
 import org.eclipse.xtext.parsetree.AbstractNode;
 import org.eclipse.xtext.parsetree.CompositeNode;
 import org.eclipse.xtext.parsetree.LeafNode;
 import org.eclipse.xtext.parsetree.NodeUtil;
 import org.eclipse.xtext.resource.XtextResource;
 import org.eclipse.xtext.testlanguages.LookaheadLanguageStandaloneSetup;
 import org.eclipse.xtext.testlanguages.ReferenceGrammarStandaloneSetup;
 import org.eclipse.xtext.testlanguages.SimpleExpressionsStandaloneSetup;
 import org.eclipse.xtext.util.StringInputStream;
 
 /**
  * @author Jan Khnlein - Initial contribution and API
  * @author Sebastian Zarnekow
  */
 public class PartialParserTest extends AbstractPartialParserTest {
 
 	public void testExpression() throws Exception {
 		with(SimpleExpressionsStandaloneSetup.class);
 		String model = "(a+b+c)*(c/d)";
 		parseAndCompareAllSubstrings(model);
 	}
 
 	public void testLookahead() throws Exception {
 		with(LookaheadLanguageStandaloneSetup.class);
 		String model = "bar a foo bar c b d foo bar b c";
 		parseAndCompareAllSubstrings(model);
 	}
 
 	private void parseAndCompareAllSubstrings(String model) throws Exception {
 		CompositeNode rootNode = getRootNode(model);
 		for (int i = 0; i < model.length() - 1; ++i) {
 			for (int j = 1; j + i < model.length(); ++j) {
 				partiallyParseAndCompare(rootNode, i, j);
 			}
 		}
 	}
 
 	public void testErrorMarkers() throws Exception {
 		with(ReferenceGrammarStandaloneSetup.class);
 		String model = "spielplatz 1 {kind (k 1}"; // model contains an error
 		// due to missing ) at idx
 		// 23
 		XtextResource resource = getResourceFromString(model);
 		assertEquals(1, resource.getErrors().size());
 		assertEquals(1, resource.getParseResult().getParseErrors().size());
 		CompositeNode rootNode = resource.getParseResult().getRootNode();
 		AbstractNode leaf = NodeUtil.findLeafNodeAtOffset(rootNode, 24);
 		assertTrue(leaf.getSyntaxError() != null);
 		// resource.update(23, 0, ")");
 		// assertTrue(resource.getParseResult().getParseErrors().isEmpty());
 		IParseResult reparse = PartialParsingUtil.reparse(getParser(), rootNode, 23, 0, ")");
 		rootNode = reparse.getRootNode();
 		String expectedFixedModel = "spielplatz 1 {kind (k 1)}";
 		String fixedModel = rootNode.serialize();
 		assertEquals("serialized model as expected", expectedFixedModel, fixedModel);
 		resource = getResourceFromString(fixedModel);
 		assertEquals("full reparse is fine", 0, resource.getErrors().size());
 		assertTrue("partial reparse is fine", reparse.getParseErrors() == null || reparse.getParseErrors().isEmpty());
 	}
 
 	public void testGrammarElementAssigned() throws Exception {
 		with(ReferenceGrammarStandaloneSetup.class);
 		String model = "spielplatz 1 {kind (k 1)\n}";
 		XtextResource resource = getResourceFromString(model);
 		CompositeNode rootNode = resource.getParseResult().getRootNode();
 		checkGrammarAssigned(rootNode);
 		IParseResult reparse = PartialParsingUtil.reparse(getParser(), rootNode, model.length() - 2, 0, "\n");
 		rootNode = reparse.getRootNode();
 		checkGrammarAssigned(rootNode);
 	}
 
 	@SuppressWarnings("serial")
 	private void checkGrammarAssigned(CompositeNode rootNode) {
 		TreeIterator<AbstractNode> iter = new AbstractTreeIterator<AbstractNode>(rootNode) {
 			@Override
 			protected Iterator<? extends AbstractNode> getChildren(Object object) {
 				if (object instanceof CompositeNode)
 					return ((CompositeNode) object).getChildren().iterator();
 				return Collections.<AbstractNode> emptyList().iterator();
 			}
 		};
 		while (iter.hasNext()) {
 			AbstractNode node = iter.next();
 			assertNotNull(node.getGrammarElement());
 			EObject grammarElement = node.getGrammarElement();
 			assertEquals(node.getParent() == null, grammarElement instanceof ParserRule);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public void testNodeState() throws Exception {
 		with(SimpleExpressionsStandaloneSetup.class);
 		String model = "(a\r\n+(b\r\n*c\r\n)+d\r\n)";
 		CompositeNode rootNode = getRootNode(model);
 		Iterator iter = rootNode.getLeafNodes().iterator();
 		boolean found = false;
 		while (iter.hasNext()) {
 			LeafNode leaf = (LeafNode) iter.next();
 			if (leaf.getText().equals("c")) {
 				assertEquals("before", 3, leaf.getTotalLine());
 				assertEquals("before", 10, leaf.getTotalOffset());
 				found = true;
 			}
 		}
 		assertTrue("node c found", found);
 		IParseResult reparse = PartialParsingUtil.reparse(getParser(), rootNode, model.indexOf('c'), 1, "xy");
 		assertTrue(reparse.getParseErrors() == null || reparse.getParseErrors().isEmpty());
 		iter = rootNode.getLeafNodes().iterator();
 		found = false;
 		while (iter.hasNext()) {
 			LeafNode leaf = (LeafNode) iter.next();
 			if (leaf.getText().equals("xy")) {
 				assertEquals("after", 3, leaf.getTotalLine());
 				assertEquals("after", 10, leaf.getTotalOffset());
 				found = true;
 			}
 		}
 		assertTrue("node xy found", found);
 	}
 
 	private void partiallyParseAndCompare(CompositeNode rootNode, int offset, int length) throws Exception {
 		PartialParsingPointers parsingPointers = PartialParsingUtil.calculatePartialParsingPointers(rootNode, offset,
 				length);
 		String entryRuleName = parsingPointers.findEntryRuleName();
 		IParseResult parseResult = ((AbstractParser) getParser()).parse(entryRuleName, new StringInputStream(
 				parsingPointers.findReparseRegion()), getASTFactory());
 		comparator.assertSameStructure(parsingPointers.getDefaultReplaceRootNode(), parseResult.getRootNode());
 		comparator.assertSameStructure(parsingPointers.findASTReplaceElement(), parseResult.getRootASTElement());
 		assertEquals(parsingPointers.getDefaultReplaceRootNode().serialize(), parseResult.getRootNode().serialize());
 	}
 }
