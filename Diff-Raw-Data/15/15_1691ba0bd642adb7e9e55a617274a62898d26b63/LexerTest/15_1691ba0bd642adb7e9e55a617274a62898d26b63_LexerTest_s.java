 package nd.regex;
 
 import junit.framework.TestCase;
 import static nd.regex.Token.Type;
 
 /**
  *
  */
 public class LexerTest extends TestCase {
 
     public void test_special() {
         Lexer lexer = new LexerImpl("[]{}()|[^");
         assertEquals(new Token(Type.LEFT_BRACKET, "["), lexer.nextToken());
         assertEquals(new Token(Type.RIGHT_BRACKET, "]"), lexer.nextToken());
         assertEquals(new Token(Type.LEFT_CURLY_BRACKET, "{"), lexer.nextToken());
         assertEquals(new Token(Type.RIGHT_CURLY_BRACKET, "}"), lexer.nextToken());
         assertEquals(new Token(Type.LEFT_PAREN, "("), lexer.nextToken());
         assertEquals(new Token(Type.RIGHT_PAREN, ")"), lexer.nextToken());
         assertEquals(new Token(Type.OR, "|"), lexer.nextToken());
         assertEquals(new Token(Type.LEFT_BRACKET_CARET, "[^"), lexer.nextToken());
         assertEquals(new Token(Type.EOF, "EOF"), lexer.nextToken());
     }
 
     public void test_usual_characters() {
         Lexer lexer = new LexerImpl("a\\.\\+\\|\\^\\$\\(\\)\\{\\}\\[\\]\\\\");
         assertEquals(new Token(Type.CHARACTER, "a"), lexer.nextToken());
         assertEquals(new Token(Type.CHARACTER, "."), lexer.nextToken());
         assertEquals(new Token(Type.CHARACTER, "+"), lexer.nextToken());
         assertEquals(new Token(Type.CHARACTER, "|"), lexer.nextToken());
         assertEquals(new Token(Type.CHARACTER, "^"), lexer.nextToken());
         assertEquals(new Token(Type.CHARACTER, "$"), lexer.nextToken());
         assertEquals(new Token(Type.CHARACTER, "("), lexer.nextToken());
         assertEquals(new Token(Type.CHARACTER, ")"), lexer.nextToken());
         assertEquals(new Token(Type.CHARACTER, "{"), lexer.nextToken());
         assertEquals(new Token(Type.CHARACTER, "}"), lexer.nextToken());
         assertEquals(new Token(Type.CHARACTER, "["), lexer.nextToken());
         assertEquals(new Token(Type.CHARACTER, "]"), lexer.nextToken());
         assertEquals(new Token(Type.CHARACTER, "\\"), lexer.nextToken());
     }
 
     public void test_predefined_character_classes() {
         Lexer lexer = new LexerImpl(".\\d\\D\\s\\S\\w\\W");
         assertEquals(new Token(Type.CLASS_ANY_CHARACTER, "."), lexer.nextToken());
         assertEquals(new Token(Type.CLASS_DIGIT, "d"), lexer.nextToken());
         assertEquals(new Token(Type.CLASS_NON_DIGIT, "D"), lexer.nextToken());
         assertEquals(new Token(Type.CLASS_WHITESPACE, "s"), lexer.nextToken());
         assertEquals(new Token(Type.CLASS_NON_WHITESPACE, "S"), lexer.nextToken());
         assertEquals(new Token(Type.CLASS_WORD_CHARACTER, "w"), lexer.nextToken());
         assertEquals(new Token(Type.CLASS_NON_WORD_CHARACTER, "W"), lexer.nextToken());
     }
 
     public void test_boundary_matches() {
         Lexer lexer = new LexerImpl("^\\b\\B$");
         assertEquals(new Token(Type.CARET, "^"), lexer.nextToken());
         assertEquals(new Token(Type.WORD_BOUNDARY, "b"), lexer.nextToken());
         assertEquals(new Token(Type.NON_WORD_BOUNDARY, "B"), lexer.nextToken());
     }
 
     public void test_greedy_quantifiers() {
         Lexer lexer = new LexerImpl("?*+");
         assertEquals(new Token(Type.ZERO_OR_ONE, "?"), lexer.nextToken());
         assertEquals(new Token(Type.ZERO_OR_MORE, "*"), lexer.nextToken());
         assertEquals(new Token(Type.ONE_OR_MORE, "+"), lexer.nextToken());
     }
 }
