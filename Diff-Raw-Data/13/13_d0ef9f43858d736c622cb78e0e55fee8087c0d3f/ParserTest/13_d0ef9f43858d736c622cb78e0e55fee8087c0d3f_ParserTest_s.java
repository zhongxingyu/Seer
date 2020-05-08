 package cs444.parser;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.junit.Test;
 
 import cs444.lexer.ILexer;
 import cs444.lexer.Lexer;
 import cs444.lexer.LexerException;
 import cs444.lexer.Token;
 import cs444.parser.symbols.ANonTerminal;
 import cs444.parser.symbols.ISymbol;
 import cs444.parser.symbols.JoosNonTerminal;
 import cs444.parser.symbols.NonTerminal;
 import cs444.parser.symbols.Terminal;
 import cs444.parser.symbols.ast.AMethodSymbol;
 import cs444.parser.symbols.ast.AModifiersOptSymbol.ImplementationLevel;
 import cs444.parser.symbols.ast.AModifiersOptSymbol.ProtectionLevel;
 import cs444.parser.symbols.ast.CharacterLiteralSymbol;
 import cs444.parser.symbols.ast.ClassSymbol;
 import cs444.parser.symbols.ast.ConstructorSymbol;
 import cs444.parser.symbols.ast.DclSymbol;
 import cs444.parser.symbols.ast.IntegerLiteralSymbol;
 import cs444.parser.symbols.ast.MethodOrConstructorSymbol;
 import cs444.parser.symbols.ast.NameSymbol;
 import cs444.parser.symbols.ast.NameSymbol.Type;
 import cs444.parser.symbols.ast.StringLiteralSymbol;
 import cs444.parser.symbols.ast.expressions.AddExprSymbol;
 import cs444.parser.symbols.ast.expressions.AndExprSymbol;
 import cs444.parser.symbols.ast.expressions.AssignmentExprSymbol;
 import cs444.parser.symbols.ast.expressions.DivideExprSymbol;
 import cs444.parser.symbols.ast.expressions.EqExprSymbol;
 import cs444.parser.symbols.ast.expressions.InstanceOfExprSymbol;
 import cs444.parser.symbols.ast.expressions.LeExprSymbol;
 import cs444.parser.symbols.ast.expressions.LtExprSymbol;
 import cs444.parser.symbols.ast.expressions.MultiplyExprSymbol;
 import cs444.parser.symbols.ast.expressions.NeExprSymbol;
 import cs444.parser.symbols.ast.expressions.NegOpExprSymbol;
 import cs444.parser.symbols.ast.expressions.NotOpExprSymbol;
 import cs444.parser.symbols.ast.expressions.OrExprSymbol;
 import cs444.parser.symbols.ast.expressions.RemainderExprSymbol;
 import cs444.parser.symbols.ast.expressions.SubtractExprSymbol;
 import cs444.parser.symbols.ast.factories.ASTSymbolFactory;
 import cs444.parser.symbols.ast.factories.IntegerLiteralFactory;
 import cs444.parser.symbols.ast.factories.ListedSymbolFactory;
 import cs444.parser.symbols.ast.factories.OneChildFactory;
 import cs444.parser.symbols.ast.factories.StringLiteralFactory;
 import cs444.parser.symbols.exceptions.IllegalModifierException;
 import cs444.parser.symbols.exceptions.OutOfRangeException;
 import cs444.parser.symbols.exceptions.UnexpectedTokenException;
 import cs444.parser.symbols.exceptions.UnsupportedException;
 
 public class ParserTest {
 
     private final Parser parser;
 
     public ParserTest() throws IOException{
         parser = new Parser(new TestRule());
     }
 
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
     public void testGoodSequence() throws Exception{
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
         assertEquals(expected, start.getRule());
         ListedSymbolFactory listRed = new ListedSymbolFactory();
         start = (JoosNonTerminal)listRed.convertAll(start);
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
         assertEquals(expected, start.getRule());
 
         OneChildFactory childFact = new OneChildFactory();
         start = (JoosNonTerminal)childFact.convertAll(start);
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
         assertEquals(expected, start.getRule());
     }
 
     @Test(expected = UnexpectedTokenException.class)
     public void testBadSequence() throws Exception{
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
     public void basicNumbers() throws Exception {
         IntegerLiteralFactory fact = new IntegerLiteralFactory();
         Terminal term = new Terminal(new Token(Token.Type.DECIMAL_INTEGER_LITERAL, "2147483647"));
 
         assertEquals(2147483647, ((IntegerLiteralSymbol)fact.convertAll(term)).intVal);
 
         ISymbol [] children = new ISymbol[2];
         children = new Terminal[2];
         children[0] = new Terminal(new Token(Token.Type.MINUS, "-"));
         children[1] = new Terminal(new Token(Token.Type.DECIMAL_INTEGER_LITERAL, "2147483648"));
         JoosNonTerminal nonTerm = new JoosNonTerminal("UNARYEXPRESSION", children);
         ISymbol converted = fact.convertAll(nonTerm);
         assertEquals(-2147483648, ((IntegerLiteralSymbol)converted).intVal);
     }
 
     @Test(expected = OutOfRangeException.class)
     public void numberTooSmall() throws Exception{
         IntegerLiteralFactory fact = new IntegerLiteralFactory();
         Terminal [] children = new Terminal[2];
         children[0] = new Terminal(new Token(Token.Type.MINUS, "-"));
         children[1] = new Terminal(new Token(Token.Type.DECIMAL_INTEGER_LITERAL, "2147483649"));
         JoosNonTerminal nonTerm = new JoosNonTerminal("UNARYEXPRESSION", children);
         nonTerm = (JoosNonTerminal) fact.convertAll(nonTerm);
     }
 
     @Test(expected = OutOfRangeException.class)
     public void numberTooBig() throws Exception{
         IntegerLiteralFactory fact = new IntegerLiteralFactory();
         Terminal [] children = new Terminal[1];
         children[0] = new Terminal(new Token(Token.Type.DECIMAL_INTEGER_LITERAL, "2147483648"));
         JoosNonTerminal nonTerm = new JoosNonTerminal("UNARYEXPRESSION", children);
         nonTerm = (JoosNonTerminal) fact.convertAll(nonTerm);
     }
 
     private static void testCharEscape(String lexeme, char expected) throws Exception {
     	StringLiteralFactory fact = new StringLiteralFactory();
         Terminal t = new Terminal(new Token(Token.Type.CHAR_LITERAL, lexeme));
         CharacterLiteralSymbol literal = (CharacterLiteralSymbol)fact.convertAll(t);
         assertEquals(expected, literal.charVal);
     }
 
     @Test
     public void validCharacterLiteralWithEscape() throws Exception {
     	testCharEscape("\'\\b\'", '\b');
     	testCharEscape("\'\\t\'", '\t');
     	testCharEscape("\'\\n\'", '\n');
     	testCharEscape("\'\\f\'", '\f');
     	testCharEscape("\'\\r\'", '\r');
     	testCharEscape("\'\\\"\'", '\"');
     	testCharEscape("\'\\\'\'", '\'');
     	testCharEscape("\'\\\\\'", '\\');
     	testCharEscape("\'\\177\'", '\177');
     }
 
     private static void testStringEscape(String lexeme, String expected) throws Exception {
     	Terminal t = new Terminal(new Token(Token.Type.STR_LITERAL, lexeme));
         ISymbol symbol = new StringLiteralFactory().convertAll(t);
         StringLiteralSymbol literal = (StringLiteralSymbol)symbol;
         assertEquals(expected, literal.strValue);
     }
 
     @Test
     public void validStringLiteralWithEscape() throws Exception {
     	testStringEscape("\"Some text \\b\"", "Some text \b");
     	testStringEscape("\"Some text \\t\"", "Some text \t");
     	testStringEscape("\"Some text \\n\"", "Some text \n");
     	testStringEscape("\"Some text \\f\"", "Some text \f");
     	testStringEscape("\"Some text \\r\"", "Some text \r");
     	testStringEscape("\"Some text \\\"\"", "Some text \"");
     	testStringEscape("\"Some text \\\'\"", "Some text \'");
     	testStringEscape("\"Some text \\\\\"", "Some text \\");
     	testStringEscape("\"Some text \\177\"", "Some text \177");
     	testStringEscape("\"Some text \\17\"", "Some text \17");
     	testStringEscape("\"Some text \\7\"", "Some text \7");
     	testStringEscape("\"Some text \\177 \"", "Some text \177 ");
     	testStringEscape("\"Some text \\17 \"", "Some text \17 ");
     	testStringEscape("\"Some text \\7 \"", "Some text \7 ");
     }
 
     @Test
     public void validCharacterLiteralWithoutEscape() throws Exception {
     	testCharEscape("\'b\'", 'b');
     	testCharEscape("\'t\'", 't');
     	testCharEscape("\'n\'", 'n');
     	testCharEscape("\'f\'", 'f');
     	testCharEscape("\'r\'", 'r');
     }
 
     @Test
     public void validStringLiteralWithoutEscape() throws Exception {
     	testStringEscape("\"Some text \\\\b\"", "Some text \\b");
     	testStringEscape("\"Some text \\\\t\"", "Some text \\t");
     	testStringEscape("\"Some text \\\\n\"", "Some text \\n");
     	testStringEscape("\"Some text \\\\f\"", "Some text \\f");
     	testStringEscape("\"Some text \\\\r\"", "Some text \\r");
     	testStringEscape("\"Some text \\\\177\"", "Some text \\177");
     	testStringEscape("\"Some text \\\\17\"", "Some text \\17");
     	testStringEscape("\"Some text \\\\1\"", "Some text \\1");
     	testStringEscape("\"Some text \\\\177 \"", "Some text \\177 ");
     	testStringEscape("\"Some text \\\\17 \"", "Some text \\17 ");
     	testStringEscape("\"Some text \\\\1 \"", "Some text \\1 ");
     }
 
     @Test
     public void stringLiteralWithMultipleEscapes() throws Exception {
     	testStringEscape("\"Some text \\\\\"\"", "Some text \\\"");
     	testStringEscape("\"Some text \\\\\'\"", "Some text \\\'");
     	testStringEscape("\"Some text \\\\\\\\\"", "Some text \\\\");
     }
 
     @Test
     public void testEmptyClass() throws Exception{
         Parser parser = new Parser(new TextReadingRules());
         Reader reader = new FileReader("CompleteCompUnit.java");
         Lexer lexer = new Lexer(reader);
         ANonTerminal start = parser.parse(lexer);
 
         IASTBuilder builder = new JoosASTBuilder();
         start = (ANonTerminal)builder.build("CompleteCompUnit.java", start);
 
         ClassSymbol classSymbol = (ClassSymbol) start;
         assertEquals("CompleteCompUnit", classSymbol.dclName);
         assertTrue(classSymbol.getProtectionLevel() == ProtectionLevel.PUBLIC);
         assertTrue(classSymbol.getImplementationLevel() == ImplementationLevel.NORMAL);
 
         assertEquals(2, start.children.size());
 
         ANonTerminal empty = (ANonTerminal) start.children.get(0);
         assertEquals("emptystatement", empty.name.toLowerCase());
 
         NameSymbol id = classSymbol.pkgImports.iterator().next();
         assertEquals("my.pkg.lol.simple", id.value);
         assertEquals(NameSymbol.Type.PACKAGE, id.type);
     }
 
     @Test
     public void testSmallClass() throws Exception{
         Parser parser = new Parser(new TextReadingRules());
         Reader reader = new FileReader("CompWithMethods.java");
         Lexer lexer = new Lexer(reader);
         ANonTerminal start = parser.parse(lexer);
 
         IASTBuilder builder = new JoosASTBuilder();
         start = (ANonTerminal)builder.build("CompWithMethods.java", start);
 
         assertEquals(9, start.children.size());
 
         ClassSymbol classSymbol = (ClassSymbol) start;
         assertSmallClassDeclaration(classSymbol);
 
         assertSmallClassImports(classSymbol.pkgImports.iterator());
 
         assertSmallClassFields(classSymbol.getFields().iterator());
 
         assertSmallClassMethods(classSymbol.getMethods().iterator());
 
         assertSmallClassConstructors(classSymbol.getConstructors().iterator());
     }
 
     @Test
     public void unaryOpReduceTest() throws OutOfRangeException, UnsupportedException, IllegalModifierException{
         ISymbol number = new Terminal(new Token(Token.Type.DECIMAL_INTEGER_LITERAL, "2147483647"));
         ISymbol neg = new Terminal(new Token(Token.Type.MINUS, "-"));
         ISymbol neg2 = new Terminal(new Token(Token.Type.MINUS, "-"));
         ISymbol not = new Terminal(new Token(Token.Type.EXCLAMATION, "!"));
 
         ANonTerminal unary = new JoosNonTerminal(JoosNonTerminal.UNARY_EXPRESSION, new ISymbol [] {neg, number});
         ANonTerminal unary2 = new JoosNonTerminal(JoosNonTerminal.UNARY_EXPRESSION, new ISymbol [] {neg2, unary});
         ISymbol head = new JoosNonTerminal("POSTFIXEXPRESSION", new ISymbol[] {not, unary2});
 
         for(ASTSymbolFactory fact : JoosASTBuilder.simplifications) head = fact.convertAll(head);
 
         assertTrue(head instanceof NotOpExprSymbol);
         head = ((ANonTerminal) head).firstOrDefault(NegOpExprSymbol.myName);
         assertTrue(head instanceof NegOpExprSymbol);
         head = ((ANonTerminal) head).firstOrDefault(IntegerLiteralSymbol.myName);
         assertTrue(head instanceof IntegerLiteralSymbol);
         assertEquals(-2147483647, ((IntegerLiteralSymbol) head).intVal);
     }
 
     /*This test would not type check, but is testing the factory.  The test was easier to make this way.
      * Type checking binary operators will happen with JOOS.
     */
     @Test
     public void binaryOpReduceTest() throws OutOfRangeException, UnsupportedException, IllegalModifierException{
         ISymbol mult = new Terminal(new Token(Token.Type.STAR, "*"));
         ISymbol div = new Terminal(new Token(Token.Type.SLASH, "/"));
         ISymbol rem = new Terminal(new Token(Token.Type.PCT, "%"));
         ISymbol sub = new Terminal(new Token(Token.Type.MINUS, "-"));
         ISymbol add = new Terminal(new Token(Token.Type.PLUS, "+"));
         ISymbol lt = new Terminal(new Token(Token.Type.LT, "<"));
         ISymbol le = new Terminal(new Token(Token.Type.LE, "<="));
         ISymbol gt = new Terminal(new Token(Token.Type.GT, ">"));
         ISymbol ge = new Terminal(new Token(Token.Type.GE, ">="));
         ISymbol inst = new Terminal(new Token(Token.Type.INSTANCEOF, "instanceof"));
         ISymbol eq = new Terminal(new Token(Token.Type.EQ, "=="));
         ISymbol ne = new Terminal(new Token(Token.Type.NE, "!="));
         ISymbol becomes = new Terminal(new Token(Token.Type.BECOMES, "="));
         ISymbol and = new Terminal(new Token(Token.Type.DAMPERSAND, "&&"));
         ISymbol or = new Terminal(new Token(Token.Type.DPIPE, "||"));
 
         ISymbol b1 = new Terminal(new Token(Token.Type.BOOLEAN, "true"));
         ISymbol b2 = new Terminal(new Token(Token.Type.BOOLEAN, "true"));
 
         ANonTerminal multt = new JoosNonTerminal ("MULTIPLICATIVEEXPRESSION",new ISymbol [] {b1, mult, b2});
         ANonTerminal divt = new JoosNonTerminal ("MULTIPLICATIVEEXPRESSION",new ISymbol [] {b1, div, multt});
         ANonTerminal remt = new JoosNonTerminal ("MULTIPLICATIVEEXPRESSION",new ISymbol [] {b1, rem, divt});
         ANonTerminal subt = new JoosNonTerminal ("ADDITIVEEXPRESSION",new ISymbol [] {b1, sub, remt});
         ANonTerminal addt = new JoosNonTerminal ("ADDITIVEEXPRESSION",new ISymbol [] {b1, add, subt});
         ANonTerminal get= new JoosNonTerminal ("RELATIONALEXPRESSION",new ISymbol [] {addt, ge, b2});
         ANonTerminal let = new JoosNonTerminal ("RELATIONALEXPRESSION",new ISymbol [] {b1, le, get});
         ANonTerminal gtt = new JoosNonTerminal ("RELATIONALEXPRESSION",new ISymbol [] {let, gt, b2});
         ANonTerminal ltt = new JoosNonTerminal ("RELATIONALEXPRESSION",new ISymbol [] {b1, lt, gtt});
         ANonTerminal instt = new JoosNonTerminal ("RELATIONALEXPRESSION",new ISymbol [] {b1, inst, ltt});
         ANonTerminal eqt = new JoosNonTerminal ("EQUALITYEXPRESSION",new ISymbol [] {b1, eq, instt});
         ANonTerminal net = new JoosNonTerminal ("EQUALITYEXPRESSION",new ISymbol [] {b1, ne, eqt});
         ANonTerminal becomest = new JoosNonTerminal ("ASSIGNMENTEXPRESSION",new ISymbol [] {b1, becomes, net});
         ANonTerminal andt = new JoosNonTerminal ("CONDITIONALANDEXPRESSION",new ISymbol [] {b1, and, becomest});
         ANonTerminal ort =  new JoosNonTerminal("INCLUSIVEOREXPRESSION", new ISymbol [] {b1, or, andt});
 
         ANonTerminal head = ort;
         for(ASTSymbolFactory fact : JoosASTBuilder.simplifications) head = (ANonTerminal)fact.convertAll(head);
         assertTrue(head instanceof OrExprSymbol);
         head = (ANonTerminal)head.children.get(1);
         assertTrue(head instanceof AndExprSymbol);
         head = (ANonTerminal)head.children.get(1);
         assertTrue(head instanceof AssignmentExprSymbol);
         head = (ANonTerminal)head.children.get(1);
         assertTrue(head instanceof NeExprSymbol);
         head = (ANonTerminal)head.children.get(1);
         assertTrue(head instanceof EqExprSymbol);
         head = (ANonTerminal)head.children.get(1);
         assertTrue(head instanceof InstanceOfExprSymbol);
         head = (ANonTerminal)head.children.get(1);
         assertTrue(head instanceof LtExprSymbol);
         head = (ANonTerminal)head.children.get(1);
         assertTrue(head instanceof LtExprSymbol);
         head = (ANonTerminal)head.children.get(1);
         assertTrue(head instanceof LeExprSymbol);
         head = (ANonTerminal)head.children.get(1);
         assertTrue(head instanceof LeExprSymbol);
         head = (ANonTerminal)head.children.get(1);
         assertTrue(head instanceof AddExprSymbol);
         head = (ANonTerminal)head.children.get(1);
         assertTrue(head instanceof SubtractExprSymbol);
         head = (ANonTerminal)head.children.get(1);
         assertTrue(head instanceof RemainderExprSymbol);
         head = (ANonTerminal)head.children.get(1);
         assertTrue(head instanceof DivideExprSymbol);
         head = (ANonTerminal)head.children.get(1);
         assertTrue(head instanceof MultiplyExprSymbol);
     }
 
     private void assertSmallClassDeclaration(ClassSymbol classSymbol) {
         assertEquals("CompWithMethods", classSymbol.dclName);
         assertTrue(classSymbol.getProtectionLevel() == ProtectionLevel.PUBLIC);
         assertTrue(classSymbol.getImplementationLevel() == ImplementationLevel.ABSTRACT);
         NameSymbol id = classSymbol.pkgImports.iterator().next();
         assertEquals("my.pkg.lol.simple", id.value);
         assertEquals(Type.PACKAGE, id.type);
     }
 
     private void assertSmallClassImports(Iterator<NameSymbol> imports) {
         imports.next();
         String [] names = {"my.pkg.lol.X", "your.pkg.lol"};
         Type [] types = {Type.IMPORT, Type.STAR_IMPORT };
 
         for(int i = 0; i < 2; i++){
             NameSymbol id = imports.next();
             assertEquals(names[i], id.value);
             assertEquals(types[i], id.type);
         }
         assertFalse(imports.hasNext());
     }
 
     private void assertSmallClassMethods(Iterator<AMethodSymbol> methods) {
         String [] methodNames = new String [] { "getValue", "valueReturn", "voidMethod", "doStuff" };
 
         ProtectionLevel [] methodProtections = new ProtectionLevel [] {
             ProtectionLevel.PUBLIC, ProtectionLevel.PROTECTED, ProtectionLevel.PROTECTED,
             ProtectionLevel.PUBLIC};
 
         ImplementationLevel [] implLevel = new ImplementationLevel [] {
             ImplementationLevel.FINAL, ImplementationLevel.NORMAL, ImplementationLevel.NORMAL,
             ImplementationLevel.ABSTRACT
         };
 
         String [] types = new String [] {"int", "boolean", "void", "void"};
 
         boolean [] isStatics = { false, true, false, false };
         boolean [] hasBody = { true, true, true, false };
 
         for(int i = 0; i < 4; i++){
             AMethodSymbol method = methods.next();
             assertEquals(methodNames[i], method.dclName);
             assertEquals(methodProtections[i], method.getProtectionLevel());
             assertEquals(types[i], method.type.value);
             assertEquals(implLevel[i], method.getImplementationLevel());
             assertEquals(isStatics[i], method.isStatic());
             assertEquals(hasBody[i], method.children.size() != 0);
             if(method.dclName.equals("getValue")){
                 ANonTerminal block = (NonTerminal) method.firstOrDefault("Block");
                 block = (ANonTerminal) block.firstOrDefault("BLOCKSTATEMENTS");
                assertSmallClassLocalVars(block.getAll("Assignment").iterator());
             }
         }
 
         if(methods.hasNext()) assertTrue(false);
 
     }
 
     private void assertSmallClassLocalVars(Iterator<ISymbol> members){
         String [] types = new String [] { "int", "int" };
         boolean [] isArray = { false, true };
         String [] names = { "n", "j" };
 
         for(int i = 0; i < 2; i++){
            AssignmentExprSymbol assigSymbol = (AssignmentExprSymbol)members.next();
            DclSymbol symbol = (DclSymbol)assigSymbol.children.get(0);
            assertEquals(names[i], symbol.dclName);
            assertEquals(isArray[i], symbol.type.isArray);
            assertEquals(types[i], symbol.type.value);
         }
 
         if(members.hasNext()) assertTrue(false);
     }
 
     private void assertSmallClassFields(Iterator<DclSymbol> fields) {
         String [] fieldNames = new String [] { "a", "b", "c", "d" };
         ProtectionLevel [] fieldProtections = new ProtectionLevel [] {
             ProtectionLevel.PUBLIC, ProtectionLevel.PUBLIC, ProtectionLevel.PUBLIC, ProtectionLevel.PROTECTED};
 
         boolean [] isStatics = { true, false, false, false };
         boolean [] isInit = { true, true, true, false };
 
         for(int i = 0; i < 4; i++){
             DclSymbol field = fields.next();
             assertEquals(fieldNames[i], field.dclName);
             assertEquals(fieldProtections[i], field.getProtectionLevel());
             assertEquals(isStatics[i], field.isStatic());
             assertFalse(field.isNative());
             assertEquals(isInit[i], field.children.size() != 0);
         }
 
         if(fields.hasNext()) assertTrue(false);
     }
 
     private void assertSmallClassConstructors(Iterator<ConstructorSymbol> constructors){
         MethodOrConstructorSymbol constructor = constructors.next();
         assertFalse(constructors.hasNext());
         assertEquals(ProtectionLevel.PUBLIC, constructor.getProtectionLevel());
         assertEquals(ImplementationLevel.NORMAL, constructor.getImplementationLevel());
     }
 }
