 package see.parser;
 
 import org.parboiled.BaseParser;
 import org.parboiled.Rule;
 import see.tree.Node;
 
 public abstract class MacroGrammar extends BaseParser<Node<Number>> {
     public static final String WHITESPACE = " \t\f\r\n";
 
    public Rule CalcExpression() {
        return Sequence(ExpressionList(), "return", RightExpression(), EOI);
    }

    public Rule Condition() {
        return Sequence(RightExpression(), EOI);
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
        return ZeroOrMore(AnyOf(WHITESPACE)).suppressNode();
     }
 
     @Override
     protected Rule fromStringLiteral(String string) {
        return Sequence(String(string.trim()), Whitespace());
     }
 }
