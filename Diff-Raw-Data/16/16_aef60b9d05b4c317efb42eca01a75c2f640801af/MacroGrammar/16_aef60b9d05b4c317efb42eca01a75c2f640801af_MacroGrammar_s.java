 package see.parser;
 
 import org.parboiled.BaseParser;
 import org.parboiled.Rule;
 import see.tree.Node;
 
 public abstract class MacroGrammar extends BaseParser<Node<Number>> {
     public static final String WHITESPACE = " \t\f\r\n";
 
    Rule CalcExpression() {
        return Sequence(ExpressionList(), "return", RightExpression());
     }
 
     Rule ExpressionList() {
         return Sequence(Expression(), ZeroOrMore(';', Optional(Expression())));
     }
 
     Rule Expression() {
         return FirstOf(AssignExpression(), Conditional(), RightExpression());
     }
 
     Rule AssignExpression() {
         return Sequence(Variable(), '=', Expression());
     }
 
     Rule Conditional() {
         return Sequence("if", "(", RightExpression(), ")",
                 "then", "{", ExpressionList(), "}",
                 Optional("else", "{", ExpressionList(), "}"));
     }
 
     Rule Variable() {
         return Identifier();
     }
 
     abstract Rule RightExpression();
 
     abstract Rule Identifier();
 
 
     Rule Whitespace() {
        return ZeroOrMore(AnyOf(WHITESPACE));
     }
 
     @Override
     protected Rule fromStringLiteral(String string) {
        return string.endsWith(WHITESPACE) ?
                Sequence(String(string.trim()), Whitespace()) :
                String(string);
     }
 }
