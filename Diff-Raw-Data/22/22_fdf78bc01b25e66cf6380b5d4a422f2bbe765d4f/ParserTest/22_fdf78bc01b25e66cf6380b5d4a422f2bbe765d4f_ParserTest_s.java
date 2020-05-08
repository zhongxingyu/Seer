 package cs444.parser;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.junit.Test;
 
 import cs444.lexer.ILexer;
 import cs444.lexer.LexerException;
 import cs444.lexer.Token;
 import cs444.parser.symbols.NonTerminal;
 import cs444.parser.symbols.Terminal;
 import cs444.parser.symbols.ast.IntegerLiteralSymbol;
 import cs444.parser.symbols.ast.factories.IntegerLiteralFactory;
 import cs444.parser.symbols.ast.factories.ListedSymbolFactory;
 import cs444.parser.symbols.ast.factories.OneChildFactory;
 import cs444.parser.symbols.exceptions.OutOfRangeException;
 import cs444.parser.symbols.exceptions.UnexpectedTokenException;
 
 public class ParserTest {
 
     private final Parser parser = new Parser(new TestRule());
 
     private static class MockLexer implements ILexer{
 
         private final Iterator<Token> tokenIt;
 
         public MockLexer(List<Token> tokens){
             tokenIt = tokens.iterator();
         }
 
         public Token getNextToken(){
             return tokenIt.next();
         }
     }
 
     @Test
    public void testGoodSequence() throws IOException, LexerException, UnexpectedTokenException, OutOfRangeException{
         List<Token> tokens = new LinkedList<Token>();
         tokens.add(new Token(Token.Type.INT, "int"));
         tokens.add(new Token(Token.Type.ID, "i"));
         tokens.add(new Token(Token.Type.EQ, "="));
         tokens.add(new Token(Token.Type.DECIMAL_INTEGER_LITERAL, "11"));
         tokens.add(new Token(Token.Type.SEMI, ";"));
         tokens.add(new Token(Token.Type.INT, "int"));
         tokens.add(new Token(Token.Type.ID, "x"));
         tokens.add(new Token(Token.Type.EQ, "="));
         tokens.add(new Token(Token.Type.ID, "q"));
         tokens.add(new Token(Token.Type.SEMI, ";"));
         tokens.add(new Token(Token.Type.INT, "int"));
         tokens.add(new Token(Token.Type.ID, "z"));
         tokens.add(new Token(Token.Type.EQ, "="));
         tokens.add(new Token(Token.Type.ID, "w"));
         tokens.add(new Token(Token.Type.SEMI, ";"));
         tokens.add(new Token(Token.Type.ID, "d"));
         tokens.add(new Token(Token.Type.PLUS, "+"));
         tokens.add(new Token(Token.Type.EQ, "="));
         tokens.add(new Token(Token.Type.ID, "o"));
         tokens.add(new Token(Token.Type.ID, "x"));
         tokens.add(new Token(Token.Type.MINUS, "-"));
         tokens.add(new Token(Token.Type.EQ, "="));
         tokens.add(new Token(Token.Type.DECIMAL_INTEGER_LITERAL, "100"));
         tokens.add(new Token(Token.Type.ID, "zz"));
         tokens.add(new Token(Token.Type.EQ, "="));
         tokens.add(new Token(Token.Type.ID, "yy"));
         tokens.add(new Token(Token.Type.EOF, "<EOF>"));
         tokens.add(null);
         MockLexer lexer = new MockLexer(tokens);
         NonTerminal start = parser.parse(lexer);
 
         String expected =  "DCLS_BECOMES -> DCLS ASSIGNS \n" +
                 "DCLS -> DCLS DCL \n" +
                 "DCLS -> DCLS DCL \n" +
                 "DCLS -> DCL \n" +
                 "DCL -> INT ID EQ ID_NUM \n" +
                 "INT -> int\n" +
                 "ID -> i\n" +
                 "EQ -> =\n" +
                 "ID_NUM -> DECIMAL_INTEGER_LITERAL \n" +
                 "DECIMAL_INTEGER_LITERAL -> 11\n" +
                 "DCL -> INT ID EQ ID_NUM \n" +
                 "INT -> int\n" +
                 "ID -> x\n" +
                 "EQ -> =\n" +
                 "ID_NUM -> ID \n" +
                 "ID -> q\n" +
                 "DCL -> INT ID EQ ID_NUM \n" +
                 "INT -> int\n" +
                 "ID -> z\n" +
                 "EQ -> =\n" +
                 "ID_NUM -> ID \n" +
                 "ID -> w\n" +
                 "ASSIGNS -> ASSIGN ASSIGNS \n" +
                 "ASSIGN -> ID PLUS EQ ID_NUM \n" +
                 "ID -> d\n" +
                 "PLUS -> +\n" +
                 "EQ -> =\n" +
                 "ID_NUM -> ID \n" +
                 "ID -> o\n" +
                 "ASSIGNS -> ASSIGN ASSIGNS \n" +
                 "ASSIGN -> ID MINUS EQ ID_NUM \n" +
                 "ID -> x\n" +
                 "MINUS -> -\n" +
                 "EQ -> =\n" +
                 "ID_NUM -> DECIMAL_INTEGER_LITERAL \n" +
                 "DECIMAL_INTEGER_LITERAL -> 100\n" +
                 "ASSIGNS -> ASSIGN \n" +
                 "ASSIGN -> ID EQ ID_NUM \n" +
                 "ID -> zz\n" +
                 "EQ -> =\n" +
                 "ID_NUM -> ID \n" +
                 "ID -> yy";
         assertEquals(expected, start.rule());
         ListedSymbolFactory listRed = new ListedSymbolFactory();
         start = (NonTerminal)listRed.convertAll(start);
         expected =  "DCLS_BECOMES -> DCLS ASSIGNS \n" +
                 "DCLS -> DCL DCL DCL \n" +
                 "DCL -> INT ID EQ ID_NUM \n" +
                 "INT -> int\n" +
                 "ID -> i\n" +
                 "EQ -> =\n" +
                 "ID_NUM -> DECIMAL_INTEGER_LITERAL \n" +
                 "DECIMAL_INTEGER_LITERAL -> 11\n" +
                 "DCL -> INT ID EQ ID_NUM \n" +
                 "INT -> int\n" +
                 "ID -> x\n" +
                 "EQ -> =\n" +
                 "ID_NUM -> ID \n" +
                 "ID -> q\n" +
                 "DCL -> INT ID EQ ID_NUM \n" +
                 "INT -> int\n" +
                 "ID -> z\n" +
                 "EQ -> =\n" +
                 "ID_NUM -> ID \n" +
                 "ID -> w\n" +
                 "ASSIGNS -> ASSIGN ASSIGN ASSIGN \n" +
                 "ASSIGN -> ID PLUS EQ ID_NUM \n" +
                 "ID -> d\n" +
                 "PLUS -> +\n" +
                 "EQ -> =\n" +
                 "ID_NUM -> ID \n" +
                 "ID -> o\n" +
                 "ASSIGN -> ID MINUS EQ ID_NUM \n" +
                 "ID -> x\n" +
                 "MINUS -> -\n" +
                 "EQ -> =\n" +
                 "ID_NUM -> DECIMAL_INTEGER_LITERAL \n" +
                 "DECIMAL_INTEGER_LITERAL -> 100\n" +
                 "ASSIGN -> ID EQ ID_NUM \n" +
                 "ID -> zz\n" +
                 "EQ -> =\n" +
                 "ID_NUM -> ID \n" +
                 "ID -> yy";
         assertEquals(expected, start.rule());
 
         OneChildFactory childFact = new OneChildFactory();
         start = (NonTerminal)childFact.convertAll(start);
         expected =  "DCLS_BECOMES -> DCLS ASSIGNS \n" +
                 "DCLS -> DCL DCL DCL \n" +
                 "DCL -> INT ID EQ DECIMAL_INTEGER_LITERAL \n" +
                 "INT -> int\n" +
                 "ID -> i\n" +
                 "EQ -> =\n" +
                 "DECIMAL_INTEGER_LITERAL -> 11\n" +
                 "DCL -> INT ID EQ ID \n" +
                 "INT -> int\n" +
                 "ID -> x\n" +
                 "EQ -> =\n" +
                 "ID -> q\n" +
                 "DCL -> INT ID EQ ID \n" +
                 "INT -> int\n" +
                 "ID -> z\n" +
                 "EQ -> =\n" +
                 "ID -> w\n" +
                 "ASSIGNS -> ASSIGN ASSIGN ASSIGN \n" +
                 "ASSIGN -> ID PLUS EQ ID \n" +
                 "ID -> d\n" +
                 "PLUS -> +\n" +
                 "EQ -> =\n" +
                 "ID -> o\n" +
                 "ASSIGN -> ID MINUS EQ DECIMAL_INTEGER_LITERAL \n" +
                 "ID -> x\n" +
                 "MINUS -> -\n" +
                 "EQ -> =\n" +
                 "DECIMAL_INTEGER_LITERAL -> 100\n" +
                 "ASSIGN -> ID EQ ID \n" +
                 "ID -> zz\n" +
                 "EQ -> =\n" +
                 "ID -> yy";
         assertEquals(expected, start.rule());
     }
 
     @Test(expected = UnexpectedTokenException.class)
     public void testBadSequence() throws IOException, LexerException, UnexpectedTokenException{
         List<Token> tokens = new LinkedList<Token>();
         tokens.add(new Token(Token.Type.INT, "int"));
         tokens.add(new Token(Token.Type.ID, "i"));
         tokens.add(new Token(Token.Type.WHITESPACE, "  "));
         tokens.add(new Token(Token.Type.EQ, "="));
         tokens.add(new Token(Token.Type.DECIMAL_INTEGER_LITERAL, "11"));
         tokens.add(new Token(Token.Type.SEMI, ";"));
         tokens.add(new Token(Token.Type.INT, "int"));
         tokens.add(new Token(Token.Type.ID, "x"));
         tokens.add(new Token(Token.Type.EQ, "="));
         tokens.add(new Token(Token.Type.ID, "q"));
         tokens.add(new Token(Token.Type.SEMI, ";"));
         tokens.add(new Token(Token.Type.ID, "w"));
         tokens.add(new Token(Token.Type.EQ, "="));
         tokens.add(new Token(Token.Type.DECIMAL_INTEGER_LITERAL, "10"));
         tokens.add(new Token(Token.Type.ID, "d"));
         tokens.add(new Token(Token.Type.PLUS, "-"));
         tokens.add(new Token(Token.Type.EQ, "="));
         tokens.add(new Token(Token.Type.EOF, "<EOF>"));
         tokens.add(null);
         MockLexer lexer = new MockLexer(tokens);
         parser.parse(lexer);
     }
 
     @Test(expected = UnexpectedTokenException.class)
     public void testExcessTokens() throws IOException, LexerException, UnexpectedTokenException{
         List<Token> tokens = new LinkedList<Token>();
         tokens.add(new Token(Token.Type.INT, "int"));
         tokens.add(new Token(Token.Type.ID, "i"));
         tokens.add(new Token(Token.Type.EQ, "="));
         tokens.add(new Token(Token.Type.DECIMAL_INTEGER_LITERAL, "11"));
         tokens.add(new Token(Token.Type.SEMI, ";"));
         tokens.add(new Token(Token.Type.INT, "int"));
         tokens.add(new Token(Token.Type.ID, "x"));
         tokens.add(new Token(Token.Type.EQ, "="));
         tokens.add(new Token(Token.Type.ID, "q"));
         tokens.add(new Token(Token.Type.SEMI, ";"));
         tokens.add(new Token(Token.Type.ID, "w"));
         tokens.add(new Token(Token.Type.EQ, "="));
         tokens.add(new Token(Token.Type.DECIMAL_INTEGER_LITERAL, "10"));
         tokens.add(new Token(Token.Type.ID, "d"));
         tokens.add(new Token(Token.Type.PLUS, "+"));
         tokens.add(new Token(Token.Type.EQ, "="));
         tokens.add(new Token(Token.Type.ID, "o"));
         tokens.add(new Token(Token.Type.INT, "int"));
         tokens.add(new Token(Token.Type.ID, "i"));
         tokens.add(new Token(Token.Type.EQ, "="));
         tokens.add(new Token(Token.Type.DECIMAL_INTEGER_LITERAL, "11"));
         tokens.add(new Token(Token.Type.SEMI, ";"));
         tokens.add(new Token(Token.Type.ID, "l"));
         tokens.add(new Token(Token.Type.EOF, "<EOF>"));
         tokens.add(null);
         MockLexer lexer = new MockLexer(tokens);
         parser.parse(lexer);
     }
 
     @Test
    public void basicNumbers() throws OutOfRangeException {
         IntegerLiteralFactory fact = new IntegerLiteralFactory();
         Terminal [] children = new Terminal[1];
         children[0] = new Terminal(new Token(Token.Type.DECIMAL_INTEGER_LITERAL, "2147483647"));
         NonTerminal nonTerm = new NonTerminal("numHolder", children);
         nonTerm = (NonTerminal) fact.convertAll(nonTerm);
 
         assertEquals(1, nonTerm.children.size());
 
         IntegerLiteralSymbol result = (IntegerLiteralSymbol)nonTerm.children.get(0);
         assertEquals(2147483647, result.value);
 
         children = new Terminal[2];
         children[0] = new Terminal(new Token(Token.Type.MINUS, "-"));
         children[1] = new Terminal(new Token(Token.Type.DECIMAL_INTEGER_LITERAL, "2147483648"));
         nonTerm = new NonTerminal("numHolder", children);
         nonTerm = (NonTerminal) fact.convertAll(nonTerm);
 
         assertEquals(1, nonTerm.children.size());
 
         result = (IntegerLiteralSymbol)nonTerm.children.get(0);
         assertEquals(-2147483648, result.value);
     }
 
     @Test(expected = OutOfRangeException.class)
    public void numberTooSmall() throws OutOfRangeException{
         IntegerLiteralFactory fact = new IntegerLiteralFactory();
         Terminal [] children = new Terminal[2];
         children[0] = new Terminal(new Token(Token.Type.MINUS, "-"));
         children[1] = new Terminal(new Token(Token.Type.DECIMAL_INTEGER_LITERAL, "2147483649"));
         NonTerminal nonTerm = new NonTerminal("numHolder", children);
         nonTerm = (NonTerminal) fact.convertAll(nonTerm);
     }
 
     @Test(expected = OutOfRangeException.class)
    public void numberTooBig() throws OutOfRangeException{
         IntegerLiteralFactory fact = new IntegerLiteralFactory();
         Terminal [] children = new Terminal[1];
         children[0] = new Terminal(new Token(Token.Type.DECIMAL_INTEGER_LITERAL, "2147483648"));
         NonTerminal nonTerm = new NonTerminal("numHolder", children);
         nonTerm = (NonTerminal) fact.convertAll(nonTerm);
     }
 }
