 package org.zwobble.shed.parser.parsing;
 
 import org.junit.Test;
 import org.zwobble.shed.parser.parsing.nodes.NumberLiteralNode;
 import org.zwobble.shed.parser.parsing.nodes.StringLiteralNode;
 import org.zwobble.shed.parser.tokeniser.Tokeniser;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 
 public class ExpressionsTest {
     @Test public void
     numberLiteralIsExpression() {
         assertThat(Expressions.expression().parse(tokens("42")),
             is((Object)Result.success(new NumberLiteralNode("42"))));
     }
     
     @Test public void
     stringLiteralIsExpression() {
         assertThat(Expressions.expression().parse(tokens("\"Nom nom nom\"")),
             is((Object)Result.success(new StringLiteralNode("Nom nom nom"))));
     }
     
     private TokenIterator tokens(String input) {
         return new TokenIterator(new Tokeniser().tokenise(input));
     }
 }
