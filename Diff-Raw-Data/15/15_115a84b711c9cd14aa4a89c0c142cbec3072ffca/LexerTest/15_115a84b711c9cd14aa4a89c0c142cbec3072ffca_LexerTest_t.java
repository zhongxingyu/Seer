 package edu.kit.pp.minijava;
 
 import java.io.IOException;
 import java.io.StringReader;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 public class LexerTest {
 
     private Lexer _lexer;
 
     public LexerTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
 
     @Before
     public void setUp() {
     }
 
     @After
     public void tearDown() {
     }
 
     @Test
     public void shouldHandleCommentsWithStars() throws Exception {
 	setupLexer("/**\n Comment over multiple * lines.\n**/");
 	assertTrue(_lexer.next().isEof());
     }
 
     @Test
     public void shouldNotIgnorePartsAfterCommentsWithStars()throws Exception {
 	setupLexer("/*\n**/class");
 	assertEquals(_lexer.next().getValue(), "class");
     }
 
     @Test
     public void shouldOutputError() throws Exception {
 	setupLexer("@");
 	assertTrue(_lexer.next() instanceof edu.kit.pp.minijava.tokens.Error);
     }
 
    @Test
    public void shouldNotAllowIntegersWithLeadingZeros() throws Exception {
	setupLexer("012");
	assertTrue(_lexer.next() instanceof edu.kit.pp.minijava.tokens.Error);

    }

    @Test
    public void shouldNotAllowIdentifiersWithLeadingZeros() throws Exception {
	setupLexer("0variable");
	assertTrue(_lexer.next() instanceof edu.kit.pp.minijava.tokens.Error);
    }

     private void setupLexer(String s) throws IOException {
 	_lexer = new Lexer(new StringReader(s));
     }
 }
