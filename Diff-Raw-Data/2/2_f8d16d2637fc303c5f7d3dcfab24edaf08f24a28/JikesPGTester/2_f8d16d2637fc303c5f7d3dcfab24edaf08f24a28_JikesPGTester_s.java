 package com.ibm.watson.safari.xform.pattern.matching.tests;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import org.eclipse.uide.utils.StreamUtils;
 import org.jikespg.uide.parser.JikesPGLexer;
 import org.jikespg.uide.parser.JikesPGParser;
 import org.jikespg.uide.parser.JikesPGParser.ASTNode;
 import com.ibm.watson.safari.xform.pattern.ASTAdapter;
 import com.ibm.watson.safari.xform.pattern.matching.JikesPGASTAdapter;
 
 public class JikesPGTester extends MatchTester {
     protected Object parseSourceFile(String srcFilePath) throws Exception {
         JikesPGLexer lexer= new JikesPGLexer(); // Create the lexer
         JikesPGParser parser= new JikesPGParser(lexer.getLexStream()); // Create the parser
         File file= new File(srcFilePath);
         InputStream is= new FileInputStream(file);
 
         lexer.initialize(StreamUtils.readStreamContents(is, "US-ASCII").toCharArray(), srcFilePath);
 	parser.getParseStream().resetTokenStream();
 	parser.setMessageHandler(new SystemOutMessageHandler());
 	lexer.lexer(null, parser.getParseStream()); // Lex the char stream to produce the token stream
 
         ASTNode ast= (ASTNode) parser.parser();
 
 	return ast;
     }
 
     protected ASTAdapter getASTAdapter() {
 	return new JikesPGASTAdapter();
     }
 
     protected void dumpSource(Object astRoot) {
 	ASTNode node= (ASTNode) astRoot;
 	System.out.println(node.toString());
     }
 
     public void test1() {
 	testHelper("[nonTerm n]", "leg.g");
     }
 
     public void test2() {
	testHelper("[nonTerm n { name == 'foo' }]", "leg.g");
     }
 
     public void testAll1() {
 	testAllHelper("[nonTerm n]", "leg.g");
     }
 }
