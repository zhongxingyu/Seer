 package see.parser.grammar;
 
 import com.google.common.collect.ImmutableList;
 import org.parboiled.Parboiled;
 import org.parboiled.Rule;
 import org.parboiled.annotations.BuildParseTree;
 import org.parboiled.annotations.SuppressNode;
 import org.parboiled.annotations.SuppressSubnodes;
 import org.parboiled.support.Var;
 import see.evaluator.DoubleNumberFactory;
 import see.evaluator.NumberFactory;
 import see.tree.ConstNode;
 import see.tree.FunctionNode;
 import see.tree.Node;
 import see.tree.VarNode;
 
 @SuppressWarnings({"InfiniteRecursion"})
 @BuildParseTree
 public class Expressions extends AbstractGrammar {
     final Literals literals = Parboiled.createParser(Literals.class);
 
     // TODO: add proper injection
     final NumberFactory numberFactory = new DoubleNumberFactory();
     final FunctionResolver functions = new FunctionResolver();
 
     public Rule CalcExpression() {
         return Sequence(ReturnExpression(), EOI);
     }
 
     public Rule Condition() {
         return Sequence(RightExpression(), EOI);
     }
 
     Rule ReturnExpression() {
         NodeListVar statements = new NodeListVar();
         return Sequence(Optional(ExpressionList(), statements.append(pop())),
                 "return", RightExpression(), statements.append(pop()),
                 push(makeSeqNode(statements.get())));
     }
 
     Rule ExpressionList() {
         NodeListVar statements = new NodeListVar();
         return Sequence(
                 Expression(), statements.append(pop()),
                 ZeroOrMore(";", Optional(Expression(), statements.append(pop()))),
                 push(makeSeqNode(statements.get())));
     }
 
     /**
      * Wraps list of expressions in FunctionNode with Sequence function
      * Short-circuits if list has only one element.
      * Expects sequence function to map to operator ';'
      * @param statements list of expressions to wrap
      * @return constructed node
      */
     Node<Object> makeSeqNode(ImmutableList<Node<Object>> statements) {
         return statements.size() > 1 ? makeFNode(";", statements): statements.get(0);
     }
 
     Rule Expression() {
         return FirstOf(AssignExpression(), Conditional(), RightExpression());
     }
 
     Rule AssignExpression() {
         return Sequence(Variable(), "=", Expression(), pushBinOp("="));
     }
 
     Rule Conditional() {
         return FirstOf(
                 Sequence(IfThenClause(), ElseClause(),
                         swap3() && push(makeFNode("if", ImmutableList.of(pop(), pop(), pop())))),
                 Sequence(IfThenClause(),
                         swap() && push(makeFNode("if", ImmutableList.of(pop(), pop()))))
         );
     }
 
     Rule IfThenClause() {
         return Sequence(
                 "if", "(", RightExpression(), ")",
                 "then", "{", ExpressionList(), "}"
         );
     }
 
     Rule ElseClause() {
         return Sequence("else", "{", ExpressionList(), "}");
     }
 
     Rule RightExpression() {
         return OrExpression();
     }
 
     Rule OrExpression() {
         return repeatWithOperator(AndExpression(), "||");
     }
 
     Rule AndExpression() {
         return repeatWithOperator(EqualExpression(), "&&");
     }
 
     Rule EqualExpression() {
         return repeatWithOperator(RelationalExpression(), FirstOf("!=", "=="));
     }
 
     Rule RelationalExpression() {
        return repeatWithOperator(AdditiveExpression(), FirstOf("<", ">", "<=", ">="));
     }
 
     Rule AdditiveExpression() {
         return repeatWithOperator(MultiplicativeExpression(), FirstOf("+", "-"));
     }
 
     Rule MultiplicativeExpression() {
         return repeatWithOperator(UnaryExpression(), FirstOf("*", "/"));
     }
 
     Rule UnaryExpression() {
         return FirstOf(Sequence(AnyOf("+-!"), UnaryExpression()), PowerExpression());
     }
 
 
     Rule PowerExpression() {
         return Sequence(UnaryExpressionNotPlusMinus(),
                 Optional("^", UnaryExpression(), pushBinOp("^")));
     }
 
     Rule UnaryExpressionNotPlusMinus() {
         return FirstOf(Constant(), Function(), Variable(), Sequence("(", Expression(), ")"));
     }
 
     /**
      * Repeat rule with separator, combining results into binary tree.
      * Matches like rep1sep, but combines results.
      * @param rule rule to match. Expected to push one node.
      * @param separator separator between rules
      * @return rule
      */
     Rule repeatWithOperator(Rule rule, Object separator) {
         Var<String> operator = new Var<String>("");
         return Sequence(rule,
                 ZeroOrMore(separator, operator.set(matchTrim()),
                         rule,
                         pushBinOp(operator.get()))
         );
     }
 
     /**
      * Combines two entries on top of the stack into FunctionNode with specified operator
      * @param operator function name
      * @return true if operation succeded
      */
     boolean pushBinOp(String operator) {
         return swap() && push(makeFNode(operator, ImmutableList.of(pop(), pop())));
     }
 
     /**
      * Construct function node with resolved function
      * @param function function name
      * @param args argument list
      * @return constructed node
      */
     FunctionNode<Object, Object> makeFNode(String function, ImmutableList<Node<Object>> args) {
         return new FunctionNode<Object, Object>(functions.get(function), args);
     }
 
     /**
      * Constant. Pushes ConstNode(value)
      * @return rule
      */
     @SuppressSubnodes
     Rule Constant() {
         return FirstOf(String(), Float(), Int());
     }
 
     /**
      * Function application. Pushes FunctionNode(f, args).
      * @return rule
      */
     Rule Function() {
         Var<String> function = new Var<String>("");
         NodeListVar args = new NodeListVar();
         return Sequence(
                 Identifier(),
                 function.set(matchTrim()),
                 "(", ArgumentList(args), ")",
                 push(makeFNode(function.get(), args.get()))
         );
     }
 
     Rule ArgumentList(NodeListVar args) {
         return repsep(Sequence(Expression(), args.append(pop())), ArgumentSeparator());
     }
 
     @SuppressNode
     Rule ArgumentSeparator() {
         return fromStringLiteral(",");
     }
 
     Rule Variable() {
         return Sequence(Identifier(), push(new VarNode<Object>(matchTrim())));
     }
 
     /**
      * String literal. Expected to push it's value/
      * @return rule
      */
     Rule String() {
         return Sequence(literals.StringLiteral(), push(new ConstNode<Object>(matchTrim())));
     }
 
     /**
      * Floating point literal. Expected to push it's value
      * @return rule
      */
     Rule Float() {
         return Sequence(literals.FloatLiteral(), push(new ConstNode<Object>(matchNumber())));
     }
 
     /**
      * Integer literal. Expected to push it's value.
      * @return constructed rule
      */
     Rule Int() {
         return Sequence(literals.IntLiteral(), push(new ConstNode<Object>(matchNumber())));
     }
 
     @SuppressSubnodes
     Rule Identifier() {
         return Sequence(literals.Letter(), ZeroOrMore(literals.LetterOrDigit()), Whitespace());
     }
 
     Number matchNumber() {
         return numberFactory.getNumber(matchTrim());
     }
 }
