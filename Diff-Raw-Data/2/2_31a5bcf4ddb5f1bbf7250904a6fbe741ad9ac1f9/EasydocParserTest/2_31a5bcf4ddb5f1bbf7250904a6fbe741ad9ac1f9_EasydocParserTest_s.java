 package com.github.easydoc.test;
 
 import java.util.List;
 
 import org.antlr.runtime.ANTLRInputStream;
 import org.antlr.runtime.ANTLRStringStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 import org.junit.Assert;
 import org.junit.Test;
 
 import com.github.easydoc.EasydocLexer;
 import com.github.easydoc.EasydocParser;
 import com.github.easydoc.model.Directive;
 import com.github.easydoc.model.Doc;
 
 public class EasydocParserTest {
 	
 	private static final String SEP = System.getProperty("line.separator");
 
 	@Test
 	public void testSingleDocWithoutParams() throws RecognitionException {
 		String input = "@@easydoc-start@@ \\\\, Doc \\@\\@ @ doc @@easydoc-end@@";
 		
 		EasydocLexer lexer = new EasydocLexer(new ANTLRStringStream(input));
 		EasydocParser parser = new EasydocParser(new CommonTokenStream(lexer));
 		List<Doc> docs = parser.document();
 		
 		Assert.assertEquals(1, docs.size());
 		Doc doc = docs.get(0);
 		Assert.assertEquals(0, doc.getParams().size());
 		Assert.assertEquals(" \\, Doc @@ @ doc ", doc.getText());
 		Assert.assertEquals(1, doc.getSourceLink().getStartLine());
 		Assert.assertEquals(1, doc.getSourceLink().getEndLine());
 		Assert.assertNull(doc.getSourceLink().getFile());
 	}
 	
 	@Test
 	public void testSingleDocWithParams() throws RecognitionException {
 		String input = "@@easydoc-start,id=main-header,ignore=\\\\\\,#@@ \\\\, Doc \\@\\@ @ doc @@easydoc-end@@";
 		
 		EasydocLexer lexer = new EasydocLexer(new ANTLRStringStream(input));
 		EasydocParser parser = new EasydocParser(new CommonTokenStream(lexer));
 		List<Doc> docs = parser.document();
 		
 		Assert.assertEquals(1, docs.size());
 		Doc doc = docs.get(0);
 		Assert.assertEquals("main-header", doc.getParams().get("id"));
 		Assert.assertEquals("\\,#", doc.getParams().get("ignore"));
 		Assert.assertEquals(" \\, Doc @@ @ doc ", doc.getText());
 		Assert.assertEquals(1, doc.getSourceLink().getStartLine());
 		Assert.assertEquals(1, doc.getSourceLink().getEndLine());
 		Assert.assertNull(doc.getSourceLink().getFile());
 	}
 	
 	@Test
 	public void testXmlComment() throws Exception {		
 		EasydocLexer lexer = new EasydocLexer(
 				new ANTLRInputStream(getClass().getResourceAsStream("/normal-pom.xml"))
 		);
 		EasydocParser parser = new EasydocParser(new CommonTokenStream(lexer));
 		List<Doc> docs = parser.document();
 		
 		Assert.assertEquals(2, docs.size());
 		Doc doc = docs.get(0);
 		Assert.assertEquals("doc1", doc.getParams().get("id"));
 		Assert.assertTrue(doc.getText().contains("Documentation in pom.xml"));
 		Assert.assertEquals(7, doc.getSourceLink().getStartLine());
 		Assert.assertEquals(11, doc.getSourceLink().getEndLine());
 		Assert.assertNull(doc.getSourceLink().getFile());
 	}
 	
 	@Test
 	public void testSingleDocWithDirective2Line() throws RecognitionException {
 		String input = "@@easydoc-start@@" + SEP + " Doc @@include, id=doc2@@ text @@easydoc-end@@";
 		
 		EasydocLexer lexer = new EasydocLexer(new ANTLRStringStream(input));
 		EasydocParser parser = new EasydocParser(new CommonTokenStream(lexer));
 		List<Doc> docs = parser.document();
 		
 		Assert.assertEquals(1, docs.size());
 		Doc doc = docs.get(0);
 		Assert.assertEquals(0, doc.getParams().size());
		Assert.assertEquals("\n Doc  text ", doc.getText());
 		Assert.assertEquals(1, doc.getSourceLink().getStartLine());
 		Assert.assertEquals(2, doc.getSourceLink().getEndLine());
 		Assert.assertNull(doc.getSourceLink().getFile());
 		
 		Assert.assertEquals(1, doc.getDirectives().size());
 		Directive d = doc.getDirectives().get(0);
 		Assert.assertEquals("include", d.getName());
 		Assert.assertEquals("doc2", d.getParams().get("id"));
 		Assert.assertEquals(1, d.getLine());
 		Assert.assertEquals(5, d.getColumn());
 		Assert.assertEquals(6, d.computePosition(doc.getText()));
 	}
 	
 	@Test
 	public void testSingleDocWithDirectiveSingleLine() throws RecognitionException {
 		String input = "@@easydoc-start@@ Doc @@include, id=doc2@@ text @@easydoc-end@@";
 		
 		EasydocLexer lexer = new EasydocLexer(new ANTLRStringStream(input));
 		EasydocParser parser = new EasydocParser(new CommonTokenStream(lexer));
 		List<Doc> docs = parser.document();
 		
 		Assert.assertEquals(1, docs.size());
 		Doc doc = docs.get(0);
 		Assert.assertEquals(0, doc.getParams().size());
 		Assert.assertEquals(" Doc  text ", doc.getText());
 		Assert.assertEquals(1, doc.getSourceLink().getStartLine());
 		Assert.assertEquals(1, doc.getSourceLink().getEndLine());
 		Assert.assertNull(doc.getSourceLink().getFile());
 		
 		Assert.assertEquals(1, doc.getDirectives().size());
 		Directive d = doc.getDirectives().get(0);
 		Assert.assertEquals("include", d.getName());
 		Assert.assertEquals("doc2", d.getParams().get("id"));
 		Assert.assertEquals(0, d.getLine());
 		Assert.assertEquals(5, d.getColumn());
 		Assert.assertEquals(5, d.computePosition(doc.getText()));
 	}
 	
 	@Test
 	public void testSingleDocWithDirectiveNoSpaceNoParams() throws RecognitionException {
 		String input = "@@easydoc-start@@@@include@@@@easydoc-end@@";
 		
 		EasydocLexer lexer = new EasydocLexer(new ANTLRStringStream(input));
 		EasydocParser parser = new EasydocParser(new CommonTokenStream(lexer));
 		List<Doc> docs = parser.document();
 		
 		Assert.assertEquals(1, docs.size());
 		Doc doc = docs.get(0);
 		Assert.assertEquals(0, doc.getParams().size());
 		Assert.assertEquals("", doc.getText());
 		Assert.assertEquals(1, doc.getSourceLink().getStartLine());
 		Assert.assertEquals(1, doc.getSourceLink().getEndLine());
 		Assert.assertNull(doc.getSourceLink().getFile());
 		
 		Assert.assertEquals(1, doc.getDirectives().size());
 		Directive d = doc.getDirectives().get(0);
 		Assert.assertEquals("include", d.getName());
 		Assert.assertEquals(0, d.getParams().size());
 		Assert.assertEquals(0, d.getLine());
 		Assert.assertEquals(0, d.getColumn());
 		Assert.assertEquals(0, d.computePosition(doc.getText()));
 	}
 
 }
