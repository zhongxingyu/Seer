 package tekai;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import org.junit.Test;
 
 public class ParserTest {
 
     @Test
     public void justAnAtom() {
         assertParsing("Just a number", "[1]:NUMBER", "1");
         assertParsing("Just an identifier", "[abc]:IDENTIFIER", "abc");
     }
 
     @Test
     public void simpleExpression() {
         assertParsing("Simple infix", "([+]:PLUS [1]:NUMBER [2]:NUMBER)", "1 + 2");
         assertParsing("Double infix (left associativity)", "([+]:PLUS ([+]:PLUS [1]:NUMBER [2]:NUMBER) [3]:NUMBER)", "1 + 2 + 3");
         assertParsing("Double infix with parenthesis", "([+]:PLUS [1]:NUMBER ([+]:PLUS [2]:NUMBER [3]:NUMBER))", "1 + (2 + 3)");
     }
 
     @Test
     public void functions() {
         assertParsing("[abc]:FUNCTION", "abc()");
         assertParsing("([abc]:FUNCTION [1]:NUMBER)", "abc(1)");
         assertParsing("([abc]:FUNCTION [1]:NUMBER [2]:NUMBER)", "abc(1, 2)");
         assertParsing("([abc]:FUNCTION [1]:NUMBER [2]:NUMBER [3]:NUMBER)", "abc(1, 2, 3)");
         assertParsing("([+]:PLUS ([abc]:FUNCTION [4]:NUMBER) ([def]:FUNCTION [3]:NUMBER [2]:NUMBER))", "abc(4) + def(3, 2)");
         assertParsing("([abc]:FUNCTION ([+]:PLUS ([+]:PLUS [2]:NUMBER [1]:NUMBER) [3]:NUMBER))", "abc((2 + 1) + 3)");
         assertParsing("([+]:PLUS ([+]:PLUS ([+]:PLUS [1]:NUMBER ([abc]:FUNCTION ([+]:PLUS [2]:NUMBER [3]:NUMBER) [4]:NUMBER)) [5]:NUMBER) [6]:NUMBER)", "(1 + abc(2 + 3, 4) + 5) + 6");
         assertParsing("([abc]:FUNCTION ([def]:FUNCTION [1]:NUMBER) ([ghi]:FUNCTION [2]:NUMBER))", "abc(def(1), ghi(2))");
     }
 
     @Test
     public void selectFrom() {
         assertParsing("([SQL]:SQL ([SELECT]:SELECT [campo1]:IDENTIFIER [campo2]:IDENTIFIER) ([FROM]:FROM [tabela]:IDENTIFIER))", "SELECT campo1, campo2 FROM tabela");
         assertParsing("([SQL]:SQL ([SELECT]:SELECT [*]:IDENTIFIER) ([FROM]:FROM [tabela]:IDENTIFIER))", "SELECT * FROM tabela");
        assertParsing("([SQL]:SQL ([SELECT]:SELECT [*]:IDENTIFIER) ([FROM]:FROM [tabela]:IDENTIFIER ([ INNER JOIN]:JOIN [outra_tabela]:IDENTIFIER [xxx]:IDENTIFIER)))", "SELECT * FROM tabela INNER JOIN outra_tabela ON xxx");
     }
 
     @Test
     public void exceptions() {
         // TODO Launch specific exception to specific problems
         // TODO Add more and more contextual information to error messages
         try {
             parse("1 +");
             fail("Expected not able to parse an incomplete expression \"1 +\"");
         } catch (Exception e) {
             // success
         }
     }
 
     // == Helpers ==
 
     private void assertParsing(String expected, String source) {
         assertParsing(null, expected, source);
     }
 
     private void assertParsing(String message, String expected, String source) {
         Expression expression = parse(source);
         assertEquals(message, expected, expression.toString());
     }
 
     private Expression parse(String source) {
         Parser parser = new Parser(source);
         configureParser(parser);
         Expression expression = parser.parse();
         return expression;
     }
 
     private void configureParser(Parser parser) {
         // PRECEDENCE (What to parse first. Higher numbers means more precedence)
         int x = 1;
         final int ATOM = x++;
         final int PLUS = x++;
         final int FUNCTION = x++;
         final int SELECT = x++;
 
         // SQL
         parser.register(new Parselet(SELECT) {
             @Override
             public boolean isPrefixParselet() {
                 return true;
             }
 
             @Override
             public String startingRegularExpression() {
                 return "SELECT";
             }
 
             @Override
             public Expression parse() {
                 Expression fields = new Expression("SELECT", "SELECT");
                 do {
                     fields.addChildren(nextExpression());
                 } while (canConsume(","));
 
                 consumeIf("FROM");
 
                 Expression from = new Expression("FROM", "FROM");
                 from.addChildren(nextExpression());
 
                 if (canConsume("INNER( OUTER|RIGHT)? JOIN")) {
                    Expression join = new Expression("JOIN", getLastMatchTrimmed());
                     join.addChildren(nextExpression());
                     consumeIf("ON");
                     join.addChildren(nextExpression());
                     from.addChildren(join);
                 }
 
                 Expression result = new Expression("SQL", "SQL");
                 result.addChildren(fields, from);
                 return result;
             }
         });
 
         // NUMBER
         parser.register(new Parselet(ATOM) {
             @Override
             public boolean isPrefixParselet() {
                 return true;
             }
 
             @Override
             public String startingRegularExpression() {
                 return "\\d+";
             }
 
             @Override
             public Expression parse() {
                 return new Expression("NUMBER", getMatchTrimmed());
             }
         });
 
         // IDENTIFIER
         parser.register(new Parselet(ATOM) {
             @Override
             public boolean isPrefixParselet() {
                 return true;
             }
 
             @Override
             public String startingRegularExpression() {
                 return "\\w+|\\*";
             }
 
             @Override
             public Expression parse() {
                 return new Expression("IDENTIFIER", getMatchTrimmed());
             }
         });
 
         // PLUS
         parser.register(new Parselet(PLUS) {
             @Override
             protected boolean isLeftAssociativity() {
                 return true;
             }
 
             @Override
             public boolean isPrefixParselet() {
                 return false;
             }
 
             @Override
             public String startingRegularExpression() {
                 return "\\+";
             }
 
             @Override
             public Expression parse() {
                 Expression result = new Expression("PLUS", getMatchTrimmed());
                 result.addChildren(left(), right());
                 return result;
             }
         });
 
         // GROUPING (parenthesis)
         parser.register(new Parselet(FUNCTION) {
             @Override
             public boolean isPrefixParselet() {
                 return true;
             }
 
             @Override
             public String startingRegularExpression() {
                 return "\\(";
             }
 
             @Override
             public Expression parse() {
                 Expression result = right();
                 consumeIf("\\)");
                 return result;
             }
         });
 
         // FUNCTION
         parser.register(new Parselet(FUNCTION) {
             @Override
             public boolean isPrefixParselet() {
                 return false;
             }
 
             @Override
             public String startingRegularExpression() {
                 return "\\(";
             }
 
             @Override
             public Expression parse() {
                 Expression result = new Expression("FUNCTION", left().getValue());
 
                 if (canConsume("\\)")) return result;
 
                 do {
                     result.addChildren(nextExpression());
                 } while (canConsume(","));
 
                 consumeIf("\\)");
 
                 return result;
             }
         });
     }
 }
