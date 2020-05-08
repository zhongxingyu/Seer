 package de.unisb.cs.depend.ccs_sem.parser;
 
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 import java.util.TreeSet;
 
 import de.unisb.cs.depend.ccs_sem.exceptions.LexException;
 import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
 import de.unisb.cs.depend.ccs_sem.lexer.CCSLexer;
 import de.unisb.cs.depend.ccs_sem.lexer.tokens.*;
 import de.unisb.cs.depend.ccs_sem.semantics.expressions.*;
 import de.unisb.cs.depend.ccs_sem.semantics.expressions.adapters.TopMostExpression;
 import de.unisb.cs.depend.ccs_sem.semantics.types.Declaration;
 import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
 import de.unisb.cs.depend.ccs_sem.semantics.types.Program;
 import de.unisb.cs.depend.ccs_sem.semantics.types.actions.Action;
 import de.unisb.cs.depend.ccs_sem.semantics.types.actions.InputAction;
 import de.unisb.cs.depend.ccs_sem.semantics.types.actions.OutputAction;
 import de.unisb.cs.depend.ccs_sem.semantics.types.actions.SimpleAction;
 import de.unisb.cs.depend.ccs_sem.semantics.types.actions.TauAction;
 import de.unisb.cs.depend.ccs_sem.semantics.types.ranges.IntervalRange;
 import de.unisb.cs.depend.ccs_sem.semantics.types.ranges.Range;
 import de.unisb.cs.depend.ccs_sem.semantics.types.ranges.SetRange;
 import de.unisb.cs.depend.ccs_sem.semantics.types.values.*;
 
 /**
  * This Parser parses the following grammar:
  *
  * program            --> (constDecl | rangeDecl | recursiveDecl)*  expression
  * constDecl          --> "const" identifier "=" arithExpression ";"
  * rangeDecl          --> "range" identifier "=" range ";"
  * recursiveDecl      --> recursionVariable = expression ";"
  * recursionVariable  --> identifier ( "[" ( ( parameter "," )* parameter)? "]"  )?
  *
  * expression          --> restrictExpression
  * restrictExpression  --> parallelExpression
  *                          | restrictExpression "\" "{" ( ( action "," )* action )? "}"
  * parallelExpression  --> choiceExpression
  *                          | parallelExpression "|" choiceExpression
  * choiceExpression    --> prefixExpression
  *                          | choiceExpression "+" prefixExpression
  * prefixExpression    --> whenExpression
  *                          | action "." prefixExpression
  * whenExpression      --> baseExpression | "when" arithExpression prefixExpression
  * baseExpression      --> "0"
  *                          | "ERROR"
  *                          | "(" expression ")"
  *                          | recursionVariable
  *                          | action
  *
  * action              --> identifier ( "?" inputValue | "!" outputValue )?
  * identifier          --> character ( digit | character ) *
  * character           --> "a" | ... | "z" | "A" | ... | "Z" | "_"
  * digit               --> "0" | ... | "9"
  * inputValue          --> inputParameter | "" | arithBase
  * outputValue         --> arithBase | ""
  * parameter           --> identifier ( ":" rangeDef)
  * inputParameter      --> identifier ( ":" range)
  *
  * range               --> rangeAdd
  * rangeAdd            --> rangeDef | rangeAdd ( "+" | "-" ) rangeDef
  * rangeDef            --> Identifier | arithExpression ".." arithExpression
  *                          | "{" ( ( arithExpression "," )* arithExpression)? "}"
  * rangeBase           --> Identifier
  * rangeElem           --> integer | Identifier
  *
  * integer             --> ( "+" | "-" )? digit+
  *
  * arithExpression     --> arithCond
  * arithCond           --> arithOr | arithOr "?" arithCond ":" arithCond
  * arithOr             --> arithAnd | arithOr "||" arithAnd
  * arithAnd            --> arithEq | arithAnd "&&" arithEq
  * arithEq             --> arithComp | arithEq ("==" | "!=" | "=") arithComp
  * arithComp           --> arithShift | arithShift ("<" | "<=" | ">" | ">=") arithShift
  * arithShift          --> arithAdd | arithShift (">>" | "<<") arithAdd
  * arithAdd            --> arithMult | arithAdd ("+" | "-") arithMult
  * arithMult           --> arithNot | arithMult ("*" | "/" | "%" | "mod") arithUnary
  * arithUnary          --> arithBase | "!" arithUnary | "+" arithUnary | "-" arithUnary
  * arithBase           --> integer | "true" | "false" | "(" arithExpression ")" | identifier
  *
  *
  * @author Clemens Hammacher
  */
 public class CCSParser implements Parser {
 
     // Stack of the currently read parameters, it's increased and decreased by
     // the read... methods. When a string is read, we try to match it with one
     // of these parameters *from top to bottom*.
     // i.e. we can "overwrite" parameters by just pushing them to this stack.
     private Stack<Parameter> parameters;
 
     // a Map of all constants that are defined in the current program
     private Map<String, ConstantValue> constants;
 
     // a Map of all ranges that are defined in the current program
     private Map<String, Range> ranges;
 
     public Program parse(Reader input) throws ParseException, LexException {
         return parse(new CCSLexer().lex(input));
     }
 
     public Program parse(String input) throws ParseException, LexException {
         return parse(new CCSLexer().lex(input));
     }
 
     // synchronized to make sure that this method is only called once at a time
     public synchronized Program parse(List<Token> tokens) throws ParseException {
         final ArrayList<Declaration> declarations = new ArrayList<Declaration>();
         parameters = new Stack<Parameter>();
         constants = new HashMap<String, ConstantValue>();
         ranges = new HashMap<String, Range>();
 
         final ExtendedIterator<Token> it = new ExtendedIterator<Token>(tokens);
 
         try {
             readDeclarations(it, declarations);
 
             // then, read the ccs expression
             Expression expr = readExpression(it);
 
             // now make it a "top most expression"
             expr = new TopMostExpression(expr);
 
             if (it.hasNext())
                 throw new ParseException("Syntax error: Unexpected '" + it.next() + "'");
 
             final Program program = new Program(declarations, expr);
 
             return program;
         } catch (final ParseException e) {
             e.setEnvironment(getEnvironment(tokens, it.previousIndex(), 5));
             throw e;
         }
     }
 
     private void readDeclarations(final ExtendedIterator<Token> it,
             final ArrayList<Declaration> declarations) throws ParseException {
 
          while (it.hasNext()) {
             if (it.peek() instanceof ConstToken) {
                 it.next();
                 final Token nextToken = it.next();
                 if (!(nextToken instanceof Identifier))
                     throw new ParseException("Expected an identifier after 'const' keyword.");
 
                 final String constName = ((Identifier)nextToken).getName();
 
                 // check for double constant name
                 if (constants.get(constName) != null)
                     throw new ParseException("Constant name \"" + constName + "\" already used.");
 
                 if (!it.hasNext() || !(it.next() instanceof Assign))
                     throw new ParseException("Expected '=' after const identifier.");
 
                 final Value constValue = readArithmeticExpression(it);
 
                 if (!(it.next() instanceof Semicolon))
                     throw new ParseException("Expected ';' after constant declaration.");
 
                 if (!(constValue instanceof ConstantValue))
                     throw new ParseException("Expecting constant value here.");
 
                 constants.put(constName, (ConstantValue)constValue);
             } else if (it.peek() instanceof RangeToken) {
                 it.next();
                 final Token nextToken = it.next();
                 if (!(nextToken instanceof Identifier))
                     throw new ParseException("Expected an identifier after 'range' keyword.");
 
                 final String rangeName = ((Identifier)nextToken).getName();
 
                 // check for double range name
                 if (ranges.get(rangeName) != null)
                     throw new ParseException("Range name \"" + rangeName + "\" already used.");
 
                 if (!it.hasNext() || !(it.next() instanceof Assign))
                     throw new ParseException("Expected '=' after range identifier.");
 
                 final Range range = readRange(it);
 
                 if (!(it.next() instanceof Semicolon))
                     throw new ParseException("Expected ';' after constant declaration.");
 
                 ranges.put(rangeName, range);
             } else {
                 final int oldPosition = it.nextIndex();
                 final Declaration nextDeclaration = readDeclaration(it);
                 if (nextDeclaration == null) {
                     it.setPosition(oldPosition);
                     break;
                 }
 
                 // check if a declaration with the same name and number of parameters is already known
                 for (final Declaration decl: declarations)
                     if (decl.getName().equals(nextDeclaration.getName())
                             && decl.getParamNr() == nextDeclaration.getParamNr())
                         throw new ParseException("Duplicate recursion variable definition ("
                             + nextDeclaration.getName() + "[" + nextDeclaration.getParamNr() + "]");
 
                 declarations.add(nextDeclaration);
             }
         }
         declarations.trimToSize();
     }
 
     private String getEnvironment(List<Token> tokens, int position, int width) {
         final int from = position < width ? 0 : position - width;
         final int to = position > tokens.size() - width ? tokens.size(): position + width;
 
         final StringBuilder environment = new StringBuilder();
         for (int i = from; i < to; ++i) {
             if (i != from)
                 environment.append(' ');
             environment.append(tokens.get(i));
         }
 
         return environment.toString();
     }
 
     /**
      * @return <code>null</code>, if there is no more declaration
      */
     private Declaration readDeclaration(ExtendedIterator<Token> tokens) throws ParseException {
         Token token1 = null;
         Token token2 = null;
         if (tokens.hasNext())
             token1 = tokens.next();
         if (tokens.hasNext())
             token2 = tokens.next();
         if (token1 == null || token2 == null)
             // there is no declaration
             return null;
 
         if (!(token1 instanceof Identifier))
             return null;
 
         final Identifier identifier = (Identifier) token1;
         if (identifier.isQuoted())
             return null;
         List<Parameter> myParameters;
         Expression expr;
 
         if (token2 instanceof Assign) {
             expr = readExpression(tokens);
             myParameters = Collections.emptyList();
         } else if (token2 instanceof LBracket) {
             myParameters = readParameters(tokens);
             if (myParameters == null || !tokens.hasNext()
                     || !(tokens.next() instanceof Assign))
                 return null;
             final Stack<Parameter> oldParameters = parameters;
             parameters = new Stack<Parameter>();
             parameters.addAll(myParameters);
             try {
                 expr = readExpression(tokens);
             } finally {
                 parameters = oldParameters;
             }
         } else
             return null;
 
         if (!tokens.hasNext() || !(tokens.next() instanceof Semicolon))
             throw new ParseException("Expected ';' after this declaration");
 
         return new Declaration(identifier.getName(), myParameters, expr);
     }
 
     private Range readRange(ExtendedIterator<Token> tokens) throws ParseException {
         return readRangeAdd(tokens);
     }
 
     private Range readRangeAdd(ExtendedIterator<Token> tokens) throws ParseException {
         Range range = readRangeDef(tokens);
         while (tokens.hasNext() && (tokens.peek() instanceof Plus || tokens.peek() instanceof Minus)) {
             final boolean isSub = tokens.next() instanceof Minus;
             final Range secondRange = readRangeDef(tokens);
             range = isSub ? range.subtract(secondRange) : range.add(secondRange);
         }
 
         return range;
     }
 
     private Range readRangeDef(ExtendedIterator<Token> tokens) throws ParseException {
         if (tokens.hasNext()) {
             // just a range definition in parenthesis?
             if (tokens.peek() instanceof LParenthesis) {
                 tokens.next();
                 final Range range = readRange(tokens);
                 if (!tokens.hasNext() || !(tokens.next() instanceof RParenthesis))
                     throw new ParseException("Expected ')'.");
                 return range;
             }
 
             // or a set of independant values
             if (tokens.peek() instanceof LBrace) {
                 tokens.next();
                 final Set<Value> rangeValues = readRangeValues(tokens);
                 return new SetRange(rangeValues);
             }
 
             // or a range of integer values
             final Value startValue = readArithmeticExpression(tokens);
             // are there '..'?
             if (tokens.hasNext() && (tokens.peek() instanceof IntervalDots)) {
                 if (!(startValue instanceof ConstIntegerValue))
                     throw new ParseException("Expected constant integer expression before '..'.");
 
                 tokens.next();
 
                 final Value endValue = readArithmeticExpression(tokens);
                 if (!(endValue instanceof ConstIntegerValue))
                     throw new ParseException("Expected constant integer expression after '..'.");
 
                 return new IntervalRange(((ConstIntegerValue)startValue).getValue(),
                     ((ConstIntegerValue)endValue).getValue());
             }
 
             // or another range (if the value was a string value)
             if (startValue instanceof ConstString) {
                 final Range referencedRange = ranges.get(((ConstString)startValue).getValue());
                 if (referencedRange != null)
                     return referencedRange;
             }
 
             // otherwise, there is an error
             throw new ParseException("No valid range definition.");
         }
 
         throw new ParseException("Unexpected EOF.");
     }
 
     private Set<Value> readRangeValues(ExtendedIterator<Token> tokens) throws ParseException {
         if (tokens.hasNext() && tokens.peek() instanceof RBrace) {
             tokens.next();
             return Collections.emptySet();
         }
 
         final Set<Value> values = new TreeSet<Value>();
 
         while (tokens.hasNext()) {
             final Value value = readArithmeticExpression(tokens);
 
             /*
             if (!(value instanceof ConstantValue))
                 throw new ParseException("Only constant values allowed in ranges.");
             */
 
             values.add(value);
 
             if (!tokens.hasNext())
                 throw new ParseException("Expected '}'");
             final Token nextToken = tokens.next();
 
             if (nextToken instanceof RBrace)
                 return values;
 
             if (!(nextToken instanceof Comma))
                 throw new ParseException("Expected ',' or '}'");
         }
 
         throw new ParseException("Expected '}'");
     }
 
     /**
      * Read all parameters up to the next RBracket (this token is read too).
      * @return <code>null</code> if there was no declaration
      * @throws ParseException if there was definitly a declaration, but it had
      *                        syntactical errors
      */
     private List<Parameter> readParameters(ExtendedIterator<Token> tokens) throws ParseException {
         final ArrayList<Parameter> parameters = new ArrayList<Parameter>();
         final Set<String> readParameters = new HashSet<String>();
 
         if (tokens.hasNext() && tokens.peek() instanceof RBracket) {
             tokens.next();
             return Collections.emptyList();
         }
 
         while (tokens.hasNext()) {
 
             // read one parameter
             Token nextToken = tokens.next();
             if (nextToken instanceof Identifier) {
                 final Identifier identifier = (Identifier)nextToken;
                 if (identifier.isQuoted())
                     return null;
                 final String name = identifier.getName();
                 if ("i".equals(name))
                     return null;
                 if (!readParameters.add(name))
                     // duplicate parameter name
                     return null;
 
                 // range?
                 Range range = null;
                 if (tokens.hasNext() && tokens.peek() instanceof Colon) {
                     tokens.next();
                     range = readRange(tokens);
                 }
                 final Parameter nextParameter = new Parameter(name, range);
 
                 parameters.add(nextParameter);
             } else
                 return null;
 
             if (!tokens.hasNext())
                 return null;
 
             nextToken = tokens.next();
 
             if (nextToken instanceof RBracket) {
                 parameters.trimToSize();
                 return parameters;
             }
             if (!(nextToken instanceof Comma))
                 return null;
         }
         return null;
     }
 
     /**
      * Read all parameter values up to the next RBracket (this token is read too).
      */
     private List<Value> readParameterValues(ExtendedIterator<Token> tokens) throws ParseException {
 
         if (tokens.hasNext() && tokens.peek() instanceof RBracket) {
             tokens.next();
             return Collections.emptyList();
         }
 
         final ArrayList<Value> readParameters = new ArrayList<Value>();
 
         while (tokens.hasNext()) {
 
             if (tokens.peek() instanceof RBracket) {
                 tokens.next();
                 readParameters.trimToSize();
                 return readParameters;
             }
 
             final Value nextValue = readArithmeticExpression(tokens);
 
             readParameters.add(nextValue);
 
             if (!tokens.hasNext())
                 throw new ParseException("Expected ']'");
 
             final Token nextToken = tokens.next();
 
             if (nextToken instanceof RBracket) {
                 readParameters.trimToSize();
                 return readParameters;
             }
             if (!(nextToken instanceof Comma))
                 throw new ParseException("Expected ',' or ']'");
         }
         throw new ParseException("Expected ']'");
     }
 
     /**
      * Read one "main expression".
      */
     private Expression readExpression(ExtendedIterator<Token> tokens) throws ParseException {
         // the topmost operator is restriction:
         return readRestrictExpression(tokens);
     }
 
     /**
      * Read one restriction expression.
      */
     private Expression readRestrictExpression(ExtendedIterator<Token> tokens) throws ParseException {
         Expression expr = readParallelExpression(tokens);
         while (tokens.hasNext() && tokens.peek() instanceof Restrict) {
             tokens.next();
             if (!tokens.hasNext() || !(tokens.next() instanceof LBrace))
                 throw new ParseException("Expected '{'");
             final Set<Action> restricted = readRestrictionActionSet(tokens);
             expr = ExpressionRepository.getExpression(new RestrictExpression(expr, restricted));
         }
 
         return expr;
     }
 
     /**
      * Read all actions up to the next RBrace (this token is read too).
      */
     private Set<Action> readRestrictionActionSet(ExtendedIterator<Token> tokens) throws ParseException {
         final Set<Action> actions = new TreeSet<Action>();
 
         if (tokens.hasNext() && tokens.peek() instanceof RBrace) {
             tokens.next();
             return Collections.emptySet();
         }
 
         while (tokens.hasNext()) {
 
             final Action newAction = readAction(tokens, false);
             if (newAction == null)
                 throw new ParseException("Expected an action here");
 
             actions.add(newAction);
 
             if (!tokens.hasNext())
                 throw new ParseException("Expected '}'");
             final Token nextToken = tokens.next();
 
             if (nextToken instanceof RBrace)
                 return actions;
 
             if (!(nextToken instanceof Comma))
                 throw new ParseException("Expected ',' or '}'");
         }
 
         throw new ParseException("Expected '}'");
     }
 
     private Action readAction(ExtendedIterator<Token> tokens, boolean tauAllowed) throws ParseException {
         if (tokens.hasNext()) {
             final Channel channel = readChannel(tokens);
             if (channel == null)
                 return null;
             if (channel instanceof TauChannel) {
                 if (!tauAllowed)
                     throw new ParseException("Tau action not allowed here");
                 return TauAction.get();
             }
             if (tokens.hasNext()) {
                 final Token nextToken = tokens.next();
                 if (nextToken instanceof QuestionMark) {
                     // read the input value
                     if (tokens.hasNext()) {
                         // either a parameter
                         if (tokens.peek() instanceof Identifier) {
                             final Identifier identifier = (Identifier)tokens.next();
                            if (!identifier.isQuoted()) {
 	                            Range range = null;
 	                            if (tokens.hasNext() && tokens.peek() instanceof Colon) {
 	                                tokens.next();
 	                                range = readRangeDef(tokens);
 	                            }
 	                            return new InputAction(channel, new Parameter(identifier.getName(), range));
                             }
                         }
                        
                         // ELSE:
                         // an arithmetic expression (if it is more complex,
                         // it must have parenthesis around it)
 
                         // save the old position
                         final int oldPosition = tokens.nextIndex();
                         try {
                             final Value value = readArithmeticBaseExpression(tokens);
                             if (value instanceof ParameterReference)
                                 ((ParameterReference)value).getParam().setType(Parameter.Type.VALUE);
                             return new InputAction(channel, value);
                         } catch (final ParseException e) {
                             // ok, there was no arithmetic expression
                             tokens.setPosition(oldPosition);
                         }
                     }
                     return new InputAction(channel, (Value)null);
                 } else if (nextToken instanceof Exclamation) {
                     // we have an output value
                     final Value value = readOutputValue(tokens);
                     return new OutputAction(channel, value);
                 }
                 tokens.previous();
             }
             // no tau, no input, no output ==> it's a simple action
             return new SimpleAction(channel);
         }
         throw new ParseException("Expected action identifier.");
     }
 
     // returns null if there is no output value
     private Value readOutputValue(ExtendedIterator<Token> tokens) {
         final int oldPosition = tokens.nextIndex();
         try {
             return readArithmeticBaseExpression(tokens);
         } catch (final ParseException e) {
             tokens.setPosition(oldPosition);
             return null;
         }
     }
 
     private Channel readChannel(ExtendedIterator<Token> tokens) throws ParseException {
         if (tokens.hasNext()) {
             final Token nextToken = tokens.next();
             if (nextToken instanceof Identifier) {
                 final Identifier identifier = (Identifier)nextToken;
                 if ("i".equals(identifier.getName()))
                     return TauChannel.get();
                 if (!identifier.isQuoted()) {
                     for (final Parameter param: parameters) {
                         if (param.getName().equals(identifier.getName())) {
                             param.setType(Parameter.Type.CHANNEL);
                             return new ParameterRefChannel(param);
                         }
                     }
                 }
                 return new ConstStringChannel(identifier.getName());
             }
             tokens.previous();
         }
         return null;
     }
 
     /**
      * Read one parallel expression.
      */
     private Expression readParallelExpression(ExtendedIterator<Token> tokens) throws ParseException {
         Expression expr = readChoiceExpression(tokens);
         while (tokens.hasNext() && tokens.peek() instanceof Parallel) {
             tokens.next();
             final Expression newExpr = readChoiceExpression(tokens);
             expr = ParallelExpression.create(expr, newExpr);
         }
 
         return expr;
     }
 
     /**
      * Read one choice expression.
      */
     private Expression readChoiceExpression(ExtendedIterator<Token> tokens) throws ParseException {
         Expression expr = readPrefixExpression(tokens);
         while (tokens.hasNext() && tokens.peek() instanceof Plus) {
             tokens.next();
             final Expression newExpr = readPrefixExpression(tokens);
             expr = ChoiceExpression.create(expr, newExpr);
         }
 
         return expr;
     }
 
     /**
      * Read one prefix expression.
      */
     private Expression readPrefixExpression(ExtendedIterator<Token> tokens) throws ParseException {
         if (tokens.hasNext()) {
             // this is not very nice: we have to save the iterator position to
             // (possibly) reset it
             final Action action = readAction(tokens, true);
             if (action == null)
                 return readWhenExpression(tokens);
 
             if (tokens.hasNext() && tokens.peek() instanceof Dot) {
                 tokens.next();
                 // if the read action is an InputAction with a parameter, we
                 // have to add this parameter to the list of parameters
                 Parameter newParam = null;
                 if (action instanceof InputAction) {
                     newParam = ((InputAction)action).getParameter();
                     if (newParam != null) {
                         // push the new parameter on the stack
                         parameters.push(newParam);
                     }
                 }
                 final Expression target = readPrefixExpression(tokens);
                 if (newParam != null) {
                     final Parameter removedParam = parameters.pop();
                     assert removedParam == newParam;
                 }
                 return ExpressionRepository.getExpression(new PrefixExpression(action, target));
             }
             // if it was not a SimpleAction, it must be a
             // PrefixExpression (followed by Stop)
             // otherwise try to read the parameters
             if (action instanceof SimpleAction) {
                 List<Value> myParameters = Collections.emptyList();
                 if (tokens.hasNext() && tokens.peek() instanceof LBracket) {
                     tokens.next();
                     myParameters = readParameterValues(tokens);
                 }
                 return ExpressionRepository.getExpression(new UnknownString(action.getLabel(), myParameters));
             } else {
                 return ExpressionRepository.getExpression(new PrefixExpression(action, StopExpression.get()));
             }
         }
         return readWhenExpression(tokens);
     }
 
     private Expression readWhenExpression(ExtendedIterator<Token> tokens) throws ParseException {
         if (tokens.hasNext() && tokens.peek() instanceof When) {
             tokens.next();
             final Value condition = readArithmeticExpression(tokens);
             ensureBoolean(condition, "Expected boolean expression after 'when'.");
 
             // if there is a "then" now, ignore it
             if (tokens.hasNext() && tokens.peek() instanceof Then)
                 tokens.next();
             final Expression consequence = readPrefixExpression(tokens);
 
             Expression condExpr = ConditionalExpression.create(condition, consequence);
 
             // we allow an "else" here to declare an alternative, but internally,
             // it is mapped to a "(when (x) <consequence>) + (when (!x) <alternative>)"
             if (tokens.hasNext() && tokens.peek() instanceof Else) {
                 tokens.next();
                 Expression alternative = readPrefixExpression(tokens);
                 // build negated condition
                 final Value negatedCondition = condition instanceof NotValue
                     ? ((NotValue)condition).getNegatedValue()
                     : NotValue.create(condition);
                 alternative = ConditionalExpression.create(negatedCondition, alternative);
                 condExpr = ChoiceExpression.create(condExpr, alternative);
             }
             return condExpr;
         }
         return readBaseExpression(tokens);
     }
 
     /**
      * Read one base expression (stop, expression in parentheses, or an identifier/action).
      */
     private Expression readBaseExpression(ExtendedIterator<Token> tokens) throws ParseException {
         if (tokens.hasNext()) {
             final Token nextToken = tokens.next();
 
             if (nextToken instanceof Stop)
                 return ExpressionRepository.getExpression(StopExpression.get());
 
             if (nextToken instanceof ErrorToken)
                 return ExpressionRepository.getExpression(ErrorExpression.get());
 
             if (nextToken instanceof LParenthesis) {
                 final Expression expr = readExpression(tokens);
                 if (!tokens.hasNext() || !(tokens.next() instanceof RParenthesis))
                     throw new ParseException("Expected ')'");
                 return expr;
             }
 
             throw new ParseException("Syntax error. Unexpected '" + nextToken + "'");
         }
 
         throw new ParseException("Unexpected end of file");
     }
 
     private Value readArithmeticExpression(ExtendedIterator<Token> tokens) throws ParseException {
         return readArithmeticConditionalExpression(tokens);
     }
 
     private Value readArithmeticConditionalExpression(ExtendedIterator<Token> tokens) throws ParseException {
         final Value orValue = readArithmeticOrExpression(tokens);
         if (tokens.hasNext() && tokens.peek() instanceof QuestionMark) {
             tokens.next();
             ensureBoolean(orValue, "Boolean expression required before '?:' construct.");
             final Value thenValue = readArithmeticConditionalExpression(tokens);
             if (!tokens.hasNext() || !(tokens.next() instanceof Colon))
                 throw new ParseException("Expected ':'");
             final Value elseValue = readArithmeticConditionalExpression(tokens);
             ensureEqualTypes(thenValue, elseValue, "Expression in '?:' construct must have the same type.");
             return ConditionalValue.create(orValue, thenValue, elseValue);
         }
 
         return orValue;
     }
 
     private Value readArithmeticOrExpression(ExtendedIterator<Token> tokens) throws ParseException {
         Value value = readArithmeticAndExpression(tokens);
         while (tokens.hasNext() && tokens.peek() instanceof Or) {
             tokens.next();
             ensureBoolean(value, "Boolean expression required before '||'.");
             final Value secondValue = readArithmeticAndExpression(tokens);
             ensureBoolean(secondValue, "Boolean expression required after '||'.");
             value = OrValue.create(value, secondValue);
         }
 
         return value;
     }
 
     private Value readArithmeticAndExpression(ExtendedIterator<Token> tokens) throws ParseException {
         Value value = readArithmeticEqExpression(tokens);
         while (tokens.hasNext() && tokens.peek() instanceof And) {
             tokens.next();
             ensureBoolean(value, "Boolean expression required before '&&'.");
             final Value secondValue = readArithmeticEqExpression(tokens);
             ensureBoolean(secondValue, "Boolean expression required after '&&'.");
             value = AndValue.create(value, secondValue);
         }
 
         return value;
     }
 
     private Value readArithmeticEqExpression(ExtendedIterator<Token> tokens) throws ParseException {
         Value value = readArithmeticCompExpression(tokens);
         while (tokens.hasNext() && tokens.peek() instanceof Equals || tokens.peek() instanceof Neq) {
             final boolean isNeq = tokens.next() instanceof Neq;
             final Value secondValue = readArithmeticCompExpression(tokens);
             ensureEqualTypes(value, secondValue, "Values to compare must have the same type.");
             value = EqValue.create(value, secondValue, isNeq);
         }
 
         return value;
     }
 
     private Value readArithmeticCompExpression(ExtendedIterator<Token> tokens) throws ParseException {
         final Value value = readArithmeticShiftExpression(tokens);
         if (tokens.hasNext()) {
             final Token nextToken = tokens.peek();
             CompValue.Type type = null;
             if (nextToken instanceof Less)
                 type = CompValue.Type.LESS;
             else if (nextToken instanceof Leq)
                 type = CompValue.Type.LEQ;
             else if (nextToken instanceof Geq)
                 type = CompValue.Type.GEQ;
             else if (nextToken instanceof Greater)
                 type = CompValue.Type.GREATER;
 
             if (type != null) {
                 tokens.next();
                 ensureInteger(value, "Only integer values can be compared.");
                 final Value secondValue = readArithmeticShiftExpression(tokens);
                 ensureInteger(secondValue, "Only integer values can be compared.");
                 return CompValue.create(value, secondValue, type);
             }
         }
 
         return value;
     }
 
     private Value readArithmeticShiftExpression(ExtendedIterator<Token> tokens) throws ParseException {
         Value value = readArithmeticAddExpression(tokens);
         while (tokens.hasNext() && (tokens.peek() instanceof LeftShift
                 || tokens.peek() instanceof RightShift)) {
             ensureInteger(value, "Only integer values can be shifted.");
             final boolean shiftRight = tokens.next() instanceof RightShift;
             final Value secondValue = readArithmeticAddExpression(tokens);
             ensureInteger(secondValue, "Shifting width must be an integer.");
             value = ShiftValue.create(value, secondValue, shiftRight);
         }
 
         return value;
     }
 
     private Value readArithmeticAddExpression(ExtendedIterator<Token> tokens) throws ParseException {
         Value value = readArithmeticMultExpression(tokens);
         while (tokens.hasNext() && (tokens.peek() instanceof Plus
                 || tokens.peek() instanceof Minus)) {
             ensureInteger(value, "Both sides of an addition must be integers.");
             final boolean isSubtraction = tokens.next() instanceof Minus;
             final Value secondValue = readArithmeticMultExpression(tokens);
             ensureInteger(secondValue, "Both sides of an addition must be integers.");
             value = AddValue.create(value, secondValue, isSubtraction);
         }
 
         return value;
     }
 
     private Value readArithmeticMultExpression(ExtendedIterator<Token> tokens) throws ParseException {
         Value value = readArithmeticUnaryExpression(tokens);
         while (tokens.hasNext()) {
             final Token nextToken = tokens.peek();
             MultValue.Type type = null;
             if (nextToken instanceof Multiplication)
                 type = MultValue.Type.MULT;
             else if (nextToken instanceof Division)
                 type = MultValue.Type.DIV;
             else if (nextToken instanceof Modulo)
                 type = MultValue.Type.MOD;
 
             if (type == null)
                 break;
 
             tokens.next();
 
             ensureInteger(value, "Both sides of a multiplication/division must be integer expressions.");
             final Value secondValue = readArithmeticUnaryExpression(tokens);
             ensureInteger(secondValue, "Both sides of a multiplication/division must be integer expressions.");
             value = MultValue.create(value, secondValue, type);
         }
 
         return value;
     }
 
     private Value readArithmeticUnaryExpression(ExtendedIterator<Token> tokens) throws ParseException {
         if (tokens.hasNext()) {
             final Token nextToken = tokens.peek();
             if (nextToken instanceof Exclamation) {
                 tokens.next();
                 final Value negatedValue = readArithmeticUnaryExpression(tokens);
                 ensureBoolean(negatedValue, "The negated value must be a boolean expression.");
                 return NotValue.create(negatedValue);
             } else if (nextToken instanceof Plus) {
                 tokens.next();
                 return readArithmeticUnaryExpression(tokens);
             } else if (nextToken instanceof Minus) {
                 tokens.next();
                 final Value negativeValue = readArithmeticUnaryExpression(tokens);
                 ensureInteger(negativeValue, "The negated value must be an integer expression.");
                 return NegativeValue.create(negativeValue);
             }
         }
         return readArithmeticBaseExpression(tokens);
     }
 
     private Value readArithmeticBaseExpression(ExtendedIterator<Token> tokens) throws ParseException {
         if (!tokens.hasNext())
             throw new ParseException("Unexpected EOF.");
         final Token nextToken = tokens.next();
         if (nextToken instanceof IntegerToken)
             return new ConstIntegerValue(((IntegerToken)nextToken).getValue());
         // a stop is the integer "0" here...
         if (nextToken instanceof Stop)
             return new ConstIntegerValue(0);
         if (nextToken instanceof True)
             return ConstBooleanValue.get(true);
         if (nextToken instanceof False)
             return ConstBooleanValue.get(false);
         if (nextToken instanceof Identifier) {
             final Identifier id = (Identifier)nextToken;
             final String name = id.getName();
             if (!id.isQuoted()) {
                 // search if this identifier is a parameter
                 for (final Parameter param: parameters)
                     if (param.getName().equals(name))
                         return new ParameterReference(param);
                 // search if it is a constant
                 final ConstantValue constant = constants.get(name);
                 if (constant != null)
                     return constant;
             }
             return new ConstString(name);
         }
         if (nextToken instanceof LParenthesis) {
             final Value value = readArithmeticExpression(tokens);
             if (!tokens.hasNext() || !(tokens.next() instanceof RParenthesis))
                 throw new ParseException("Expected ')'.");
             return value;
         }
 
         throw new ParseException("Expected some expression here.");
     }
 
     private void ensureEqualTypes(Value value1, Value value2, String message) throws ParseException {
         if (value1 instanceof IntegerValue && value2 instanceof IntegerValue)
             return;
         if (value1 instanceof BooleanValue && value2 instanceof BooleanValue)
             return;
         if (value1 instanceof ConstString && value2 instanceof ConstString)
             return;
         if (value1 instanceof ParameterReference || value1 instanceof ParameterRefChannel) {
             ((ParameterReference)value1).getParam().match(value2);
             return;
         }
         if (value2 instanceof ParameterReference || value2 instanceof ParameterRefChannel) {
             ((ParameterReference)value2).getParam().match(value1);
             return;
         }
         if (value1 instanceof ConditionalValue) {
             ensureEqualTypes(((ConditionalValue)value1).getThenValue(), value2, message);
             ensureEqualTypes(((ConditionalValue)value1).getElseValue(), value2, message);
         } else if (value2 instanceof ConditionalValue) {
             ensureEqualTypes(value1, ((ConditionalValue)value2).getThenValue(), message);
             ensureEqualTypes(value1, ((ConditionalValue)value2).getElseValue(), message);
         }
         throw new ParseException(message + " The values \"" + value1 + "\" and \"" + value2 + "\" have different types.");
     }
 
     private void ensureBoolean(Value value, String message) throws ParseException {
         if (value instanceof BooleanValue)
             return;
         if (value instanceof IntegerValue)
             throw new ParseException(message + " The value \"" + value + "\" has type integer.");
         if (value instanceof ConstString)
             throw new ParseException(message + " The value \"" + value + "\" has type string.");
         if (value instanceof ParameterReference) {
             ((ParameterReference)value).getParam().setType(Parameter.Type.BOOLEANVALUE);
             return;
         }
         if (value instanceof ConditionalValue) {
             ensureBoolean(((ConditionalValue)value).getThenValue(), message);
             ensureBoolean(((ConditionalValue)value).getElseValue(), message);
         }
         assert false;
         throw new ParseException(message);
     }
 
     private void ensureInteger(Value value, String message) throws ParseException {
         if (value instanceof IntegerValue)
             return;
         if (value instanceof BooleanValue)
             throw new ParseException(message + " The value \"" + value + "\" has type boolean.");
         if (value instanceof ConstString)
             throw new ParseException(message + " The value \"" + value + "\" has type string.");
         if (value instanceof ParameterReference) {
             ((ParameterReference)value).getParam().setType(Parameter.Type.INTEGERVALUE);
             return;
         }
         if (value instanceof ConditionalValue) {
             ensureInteger(((ConditionalValue)value).getThenValue(), message);
             ensureInteger(((ConditionalValue)value).getElseValue(), message);
         }
         assert false;
         throw new ParseException(message);
     }
 
 }
