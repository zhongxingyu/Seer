 package see;
 
 import org.parboiled.Parboiled;
 import org.parboiled.Rule;
 import see.evaluator.SimpleEvaluator;
 import see.parser.BasicParser;
 import see.parser.Parser;
 import see.parser.config.ConfigBuilder;
 import see.parser.config.GrammarConfiguration;
 import see.parser.grammar.EntryPoints;
 import see.tree.Node;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Facade to parse/evaluate operations.
  * All operations are thread-safe. Parse results are immutable and can be reused/cached between different instances.
 * Warning: evaluation can modify passed context. If that is undesirable, create new HashMap via copy constructor or
 * pass ImmutableMap instance.
  */
 public class See {
 
     private final GrammarConfiguration config;
 
     public See() {
         config = ConfigBuilder.defaultConfig().build();
     }
 
     public See(GrammarConfiguration config) {
         this.config = config;
     }
 
     /**
      * Parse a single expression
      *
      * @param expression text to parse
      * @return parsed tree
      */
     public Node<Object> parseExpression(String expression) {
         return parse(expression, getGrammar().Condition());
     }
 
     /**
      * Parse semicolon-separated list of expressions
      *
      * @param expression text to parse
      * @return parsed tree
      */
     public Node<Object> parseExpressionList(String expression) {
         return parse(expression, getGrammar().Statements());
     }
 
     /**
      * Parse a expression with 'return' keyword
      *
      * @param expression text to parse
      * @return parsed tree
      */
     public Node<Object> parseReturnExpression(String expression) {
         return parse(expression, getGrammar().CalcExpression());
     }
 
     /**
      * Evaluate tree with supplied variables
      *
      * @param tree tree to evaluate
      * @param context variable->value mapping
      * @param <T> return type
      * @return evaluated value
      */
     public <T> T evaluate(Node<T> tree, Map<String, ?> context) {
         return new SimpleEvaluator(config.getNumberFactory()).evaluate(tree, context);
     }
 
     /**
      * Evaluate tree with empty context. 
      *
      * @param tree tree to evaluate
      * @param <T> return type
      * @return evaluated value
      */
     public <T> T evaluate(Node<T> tree) {
         return new SimpleEvaluator(config.getNumberFactory()).evaluate(tree, new HashMap<String, Object>());
     }
 
     /**
      * Parse and evaluate simple expression.
      * Equivalent to evaluate(parseExpression(expression), context).
      * @param expression expression to evaluate
      * @param context variable->value mapping
      * @return evaluated value
      */
     public Object eval(String expression, Map<String, ?> context) {
         return evaluate(parseExpression(expression), context);
     }
 
     public Object eval(String expression) {
         Node<Object> tree = parseExpression(expression);
         return evaluate(tree);
     }
 
     private Node<Object> parse(String expression, Rule rule) {
         Parser<Object> parser = new BasicParser<Object>(rule);
         return parser.parse(expression);
     }
 
     private EntryPoints getGrammar() {
         return Parboiled.createParser(EntryPoints.class, config);
     }
 
 }
