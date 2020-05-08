 package org.zwobble.shed.parser.parsing;
 
 import org.zwobble.shed.parser.parsing.nodes.ExpressionNode;
 
 public class Expressions {
     public static Rule<ExpressionNode> expression() {
        return Rules.firstOf(
             Literals.numberLiteral(),
             Literals.stringLiteral()
         );
     }
 }
