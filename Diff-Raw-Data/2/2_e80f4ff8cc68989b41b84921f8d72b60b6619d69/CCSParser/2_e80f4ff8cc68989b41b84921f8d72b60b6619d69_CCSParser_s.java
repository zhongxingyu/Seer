 package de.unisb.cs.depend.ccs_sem.parser;
 
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import de.unisb.cs.depend.ccs_sem.exceptions.ArithmeticError;
 import de.unisb.cs.depend.ccs_sem.exceptions.LexException;
 import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
 import de.unisb.cs.depend.ccs_sem.lexer.CCSLexer;
 import de.unisb.cs.depend.ccs_sem.lexer.tokens.*;
 import de.unisb.cs.depend.ccs_sem.lexer.tokens.categories.Token;
 import de.unisb.cs.depend.ccs_sem.semantics.expressions.*;
 import de.unisb.cs.depend.ccs_sem.semantics.expressions.adapters.TopMostExpression;
 import de.unisb.cs.depend.ccs_sem.semantics.types.ChannelSet;
 import de.unisb.cs.depend.ccs_sem.semantics.types.Parameter;
 import de.unisb.cs.depend.ccs_sem.semantics.types.ParameterList;
 import de.unisb.cs.depend.ccs_sem.semantics.types.ProcessVariable;
 import de.unisb.cs.depend.ccs_sem.semantics.types.Program;
 import de.unisb.cs.depend.ccs_sem.semantics.types.ValueList;
 import de.unisb.cs.depend.ccs_sem.semantics.types.ValueSet;
 import de.unisb.cs.depend.ccs_sem.semantics.types.actions.Action;
 import de.unisb.cs.depend.ccs_sem.semantics.types.actions.InputAction;
 import de.unisb.cs.depend.ccs_sem.semantics.types.actions.OutputAction;
 import de.unisb.cs.depend.ccs_sem.semantics.types.actions.SimpleAction;
 import de.unisb.cs.depend.ccs_sem.semantics.types.actions.TauAction;
 import de.unisb.cs.depend.ccs_sem.semantics.types.ranges.IntervalRange;
 import de.unisb.cs.depend.ccs_sem.semantics.types.ranges.Range;
 import de.unisb.cs.depend.ccs_sem.semantics.types.ranges.SetRange;
 import de.unisb.cs.depend.ccs_sem.semantics.types.values.*;
 import de.unisb.cs.depend.ccs_sem.utils.Pair;
 
 /**
  * This Parser parses the following grammar:
  *
  * program            --> (constDecl | rangeDecl | recursiveDecl)*  expression
  * constDecl          --> "const" identifier ":=" arithExpression ";"
  * rangeDecl          --> "range" identifier ":=" range ";"
  * recursiveDecl      --> recursionVariable := expression ";"
  * recursionVariable  --> ucIdentifier ( "[" ( ( parameter "," )* parameter)? "]"  )?
  *
  * expression          --> restrictExpression
  * restrictExpression  --> parallelExpression
  *                          | restrictExpression "\" "{" ( ( channel "," )* channel )? "}"
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
  * action              --> channel ( "?" inputValue | "!" outputValue )?
  * channel             --> lcIdentifier
  * identifier          --> character ( digit | character ) *
  * lcIdentifier        --> lcCharacter ( digit | character ) *
  * ucIdentifier        --> ucCharacter ( digit | character ) *
  * character           --> "a" | ... | "z" | "A" | ... | "Z" | "_"
  * ucCharacter         --> "A" | ... | "Z" | "_"
  * lcCharacter         --> "a" | ... | "z" | "_"
  * digit               --> "0" | ... | "9"
  * inputValue          --> inputParameter | "" | arithBase
  * outputValue         --> arithBase | ""
  * parameter           --> identifier ( ":" rangeDef)
  * inputParameter      --> identifier ( ":" range)
  *
  * range               --> rangeAdd
  * rangeAdd            --> rangeDef | rangeAdd ( "+" | "-" ) rangeDef
  * rangeDef            --> Identifier | arithBase ".." arithBase
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
  * arithEq             --> arithComp | arithEq ("==" | "!=" ) arithComp
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
 
     private final List<IParsingProblemListener> listeners
         = new ArrayList<IParsingProblemListener>();
 
     // "Stack" of the currently read parameters, it's increased and decreased by
     // the read... methods. When a string is read, we try to match it with one
     // of these parameters *from left to right*.
     // i.e. we can "overwrite" parameters by just adding them on the left to this list.
     private LinkedList<Parameter> parameters;
 
     // a Map of all constants that are defined in the current program
     private Map<String, ConstantValue> constants;
 
     // a Map of all ranges that are defined in the current program
     private Map<String, Range> ranges;
 
     /**
      * Parses a CCS program from an input reader.
      *
      * @param input the Reader that provides the input
      * @return the parsed CCS program, or <code>null</code> if there was an error
      *         (use {@link #addProblemListener(IParsingProblemListener)} to fetch the error)
      */
     public Program parse(Reader input) {
         try {
             return parse(getDefaultLexer().lex(input));
         } catch (final LexException e) {
             reportProblem(new ParsingProblem(ParsingProblem.ERROR, "Error lexing: " + e.getMessage(), e.getPosition(), e.getPosition()));
             return null;
         }
     }
 
     protected CCSLexer getDefaultLexer() {
         return new CCSLexer();
     }
 
     /**
      * Parses a CCS program from an input string.
      *
      * @param input the input source code
      * @return the parsed CCS program, or <code>null</code> if there was an error
      *         (use {@link #addProblemListener(IParsingProblemListener)} to fetch the error)
      */
     public Program parse(String input) {
         try {
             return parse(getDefaultLexer().lex(input));
         } catch (final LexException e) {
             reportProblem(new ParsingProblem(ParsingProblem.ERROR, "Error lexing: " + e.getMessage(), e.getPosition(), e.getPosition()));
             return null;
         }
     }
 
     /**
      * Parses a CCS program from a token list.
      *
      * @param tokens the token list to parse
      * @return the parsed CCS program, or <code>null</code> if there was an error
      *         (use {@link #addProblemListener(IParsingProblemListener)} to fetch the error)
      */
     public synchronized Program parse(List<Token> tokens) {
         final ArrayList<ProcessVariable> processVariables = new ArrayList<ProcessVariable>();
         parameters = new LinkedList<Parameter>();
         constants = new HashMap<String, ConstantValue>();
         ranges = new HashMap<String, Range>();
 
         final ExtendedListIterator<Token> it = new ExtendedListIterator<Token>(tokens);
 
         readDeclarations(it, processVariables);
 
         // then, read the ccs expression
         Expression mainExpr;
         try {
             mainExpr = readMainExpression(it);
         } catch (final ParseException e) {
             reportProblem(new ParsingProblem(e));
             return null;
         }
 
         // now make it a "top most expression"
         mainExpr = ExpressionRepository.getExpression(new TopMostExpression(mainExpr));
 
         final Token eof = it.next();
         if (!(eof instanceof EOFToken)) {
             reportProblem(new ParsingProblem(ParsingProblem.ERROR, "Unexpected token", eof));
         }
 
         Program program = null;
         try {
             program = new Program(processVariables, mainExpr);
         } catch (final ParseException e) {
             reportProblem(new ParsingProblem(e));
         }
 
         if (program != null) {
             // search if the alphabet contains parameterized input actions
             final Map<Action, Action> alphabet = program.getAlphabet();
             for (final Entry<Action, Action> e: alphabet.entrySet()) {
                 final Action act = e.getKey();
                 if (!(act instanceof InputAction))
                     continue;
                 final Parameter param = ((InputAction)act).getParameter();
                 if (param == null)
                     continue;
                 final Range range = param.getRange();
                 if (range != null && range.isRangeRestricted())
                     continue;
 
                 // ok, we have an unrestricted and not range restricted action.
                 reportUnboundInputParameter(act, e.getValue());
             }
         }
 
         return program;
     }
 
     private void readDeclarations(final ExtendedListIterator<Token> tokens,
             final ArrayList<ProcessVariable> processVariables) {
 
         while (tokens.hasNext() && !(tokens.peek() instanceof EOFToken)) {
             final int oldPosition = tokens.nextIndex();
             try {
                 if (tokens.peek() instanceof ConstToken) {
                     tokens.next();
                     Token nextToken = tokens.next();
                     if (!(nextToken instanceof Identifier))
                         throw new ParseException("Expected an identifier after 'const' keyword.", nextToken);
 
                     final String constName = ((Identifier)nextToken).getName();
 
                     // check for double constant name
                     if (constants.get(constName) != null)
                         throw new ParseException("Constant name \"" + constName + "\" already used.", nextToken);
 
                     if (!tokens.hasNext() || !(tokens.next() instanceof Assign))
                         throw new ParseException("Expected ':=' after const identifier.", tokens.peekPrevious());
 
                     final Token startToken = tokens.peek();
                     final Value constValue = readArithmeticExpression(tokens);
 
                     identifierParsed((Identifier)nextToken, constValue);
 
                     nextToken = tokens.next();
                     // TODO remove this ambiguousness
                     if (!(nextToken instanceof Semicolon) && !(nextToken instanceof Comma))
                         throw new ParseException("Expected ';' after constant declaration.", tokens.peekPrevious());
 
                     if (!(constValue instanceof ConstantValue))
                         throw new ParseException("Expected constant value.",
                             startToken.getStartPosition(), tokens.peekPrevious().getEndPosition());
 
                     constants.put(constName, (ConstantValue)constValue);
                 } else if (tokens.peek() instanceof RangeToken) {
                     tokens.next();
                     Token nextToken = tokens.next();
                     if (!(nextToken instanceof Identifier))
                         throw new ParseException("Expected an identifier after 'range' keyword.", nextToken);
 
                     final String rangeName = ((Identifier)nextToken).getName();
 
                     // check for double range name
                     if (ranges.get(rangeName) != null)
                         throw new ParseException("Range name \"" + rangeName + "\" already used.", nextToken);
 
                     if (!tokens.hasNext() || !(tokens.next() instanceof Assign))
                         throw new ParseException("Expected ':=' after range identifier.", tokens.peekPrevious());
 
                     final Range range = readRange(tokens);
 
                     identifierParsed((Identifier)nextToken, range);
 
                     nextToken = tokens.next();
                     // TODO remove this ambiguousness
                     if (!(nextToken instanceof Semicolon) && !(nextToken instanceof Comma))
                         throw new ParseException("Expected ';' after constant declaration.", tokens.peekPrevious());
 
                     ranges.put(rangeName, range);
                 } else {
                     final Token nextToken = tokens.peek();
                     final ProcessVariable nextProcessVariable = readProcessDeclaration(tokens);
                     if (nextProcessVariable == null) {
                         tokens.setPosition(oldPosition);
                         break;
                     }
 
                     // check if a process variable with the same name and number of parameters is already known
                     for (final ProcessVariable proc: processVariables)
                         if (proc.getName().equals(nextProcessVariable.getName())
                                 && proc.getParamCount() == nextProcessVariable.getParamCount()) {
                             reportProblem(new ParsingProblem(ParsingProblem.ERROR,
                                     "Duplicate process variable definition (" + nextProcessVariable.getName()
                                     + "[" + nextProcessVariable.getParamCount() + "]", nextToken));
                             break;
                         }
 
                     processVariables.add(nextProcessVariable);
                 }
             } catch (final ParseException e) {
                 reportProblem(new ParsingProblem(e));
                 // ... but continue parsing (readProcessDeclaration moved
                 // forward to the next semicolon)
             }
         }
         processVariables.trimToSize();
     }
 
     /**
      * @return <code>null</code>, if there are no more declarations
      */
     protected ProcessVariable readProcessDeclaration(ExtendedListIterator<Token> tokens) {
         final Token token1 = tokens.hasNext() ? tokens.next() : null;
         final Token token2 = tokens.hasNext() ? tokens.next() : null;
         if (token1 == null || token2 == null)
             // there is no declaration
             return null;
 
         if (!(token1 instanceof Identifier))
             return null;
 
         final Identifier identifier = (Identifier) token1;
         if (identifier.isQuoted() || !identifier.isUpperCase())
             return null;
         ParameterList myParameters = null;
         Expression expr = null;
 
         try {
             if (token2 instanceof Assign) {
                 myParameters = new ParameterList(0);
                 expr = readExpression(tokens);
             } else if (token2 instanceof LBracket) {
                 final List<Pair<Parameter, Pair<Token, Token>>> readParameters =
                         readProcessParameters(tokens);
                 if (readParameters == null || !(tokens.next() instanceof Assign))
                     return null;
                 myParameters = new ParameterList(readParameters.size());
                 for (final Pair<Parameter, Pair<Token, Token>> param: readParameters)
                     myParameters.add(param.getFirst());
                 // now that we know that we have a process declaration, check the
                 // parameters (may throw a ParseException)
                 checkProcessParameters(readParameters);
                 // save old parameters
                 final LinkedList<Parameter> oldParameters = parameters;
                 try {
                     // set new parameters
                     parameters = new LinkedList<Parameter>(myParameters);
                     expr = readExpression(tokens);
                 } finally {
                     // restore old parameters
                     parameters = oldParameters;
                 }
             } else
                 return null;
         } catch (final ParseException e) {
             if (myParameters == null)
                 myParameters = new ParameterList(0);
             reportProblem(new ParsingProblem(e));
         }
 
        final Token nextToken = tokens.next();
         // TODO remove this ambiguousness
         if (!(nextToken instanceof Semicolon) && !(nextToken instanceof Comma)) {
             // only report the error if the expression was read correctly
             if (expr != null)
                 reportProblem(new ParsingProblem(ParsingProblem.ERROR,
                     "Expected ';' after the declaration", tokens.peekPrevious()));
             // move forward to the next semicolon
             while (tokens.hasNext())
                 if (tokens.next() instanceof Semicolon)
                     break;
             // we don't want to read the EOFToken
             if (tokens.peekPrevious() instanceof EOFToken)
                 tokens.previous();
             // ... and continue parsing
         }
 
         if (expr == null)
             expr = StopExpression.get();
 
         final ProcessVariable proc = new ProcessVariable(identifier.getName(), myParameters, expr);
         // hook for logging:
         identifierParsed(identifier, proc);
         return proc;
     }
 
     /**
      * Read a Range definition.
      *
      * @param tokens
      * @return a {@link Range} read from the tokens
      * @throws ParseException on syntax errors
      */
     private Range readRange(ExtendedListIterator<Token> tokens) throws ParseException {
         return readRangeAdd(tokens);
     }
 
     private Range readRangeAdd(ExtendedListIterator<Token> tokens) throws ParseException {
         Range range = readRangeDef(tokens);
         while (tokens.peek() instanceof Plus || tokens.peek() instanceof Minus) {
             final boolean isSub = tokens.next() instanceof Minus;
             final Range secondRange = readRangeDef(tokens);
             range = isSub ? range.subtract(secondRange) : range.add(secondRange);
         }
 
         return range;
     }
 
     private Range readRangeDef(ExtendedListIterator<Token> tokens) throws ParseException {
         final Token nextToken = tokens.peek();
         // just a range definition in parenthesis?
         if (nextToken instanceof LParenthesis) {
             tokens.next();
             final Range range = readRange(tokens);
             if (!tokens.hasNext() || !(tokens.next() instanceof RParenthesis))
                 throw new ParseException("Expected ')'.", tokens.peekPrevious());
             return range;
         }
 
         // or a set of independant values
         if (nextToken instanceof LBrace) {
             tokens.next();
             final ValueSet rangeValues = readRangeValues(tokens);
             return new SetRange(rangeValues);
         }
 
         // or a range of integer values
         int posStart = nextToken.getStartPosition();
         final Value startValue = readArithmeticBaseExpression(tokens, false);
         // are there '..'?
         if (tokens.peek() instanceof IntervalDots) {
             ensureInteger(startValue, "Expected constant integer expression before '..'.", posStart, tokens.peekPrevious().getEndPosition());
 
             tokens.next();
 
             posStart = tokens.peek().getStartPosition();
             final Value endValue = readArithmeticBaseExpression(tokens, false);
             ensureInteger(endValue, "Expected constant integer expression after '..'.", posStart, tokens.peekPrevious().getEndPosition());
 
             return new IntervalRange(startValue, endValue);
         }
 
         // or another range (if the value was a string value)
         if (startValue instanceof ConstString) {
             final Range referencedRange = ranges.get(((ConstString)startValue).getStringValue());
             if (referencedRange != null) {
                 // hook for logging:
                 changedIdentifierMeaning((ConstString)startValue, referencedRange);
                 return referencedRange;
             }
         }
 
         // otherwise, there is an error
         throw new ParseException("No valid range definition.", nextToken);
     }
 
     private ValueSet readRangeValues(ExtendedListIterator<Token> it) throws ParseException {
         final ValueSet values = new ValueSet();
 
         while (true) {
             final Value value = readArithmeticExpression(it);
 
             values.add(value);
 
             final Token nextToken = it.next();
 
             if (nextToken instanceof RBrace)
                 return values;
 
             if (!(nextToken instanceof Comma))
                 throw new ParseException("Expected ',' or '}'", nextToken);
         }
     }
 
     /**
      * Read all parameters up to the next RBracket (this token is read too).
      *
      * @return a List containing read parameters and the Token range where they
      *         occured, or <code>null</code> if there was no declaration
      * @throws ParseException if there was definitly a declaration, but it had
      *                        syntactical errors
      */
     private List<Pair<Parameter, Pair<Token, Token>>> readProcessParameters(ExtendedListIterator<Token> tokens) throws ParseException {
         final ArrayList<Pair<Parameter, Pair<Token, Token>>> parameters = new ArrayList<Pair<Parameter,Pair<Token,Token>>>();
 
         if (tokens.peek() instanceof RBracket) {
             tokens.next();
             return Collections.emptyList();
         }
 
         while (true) {
             // read one parameter
             Token nextToken = tokens.next();
             if (nextToken instanceof Identifier) {
                 final Identifier identifier = (Identifier)nextToken;
                 if (identifier.isQuoted())
                     return null;
                 final String name = identifier.getName();
 
                 // range?
                 Range range = null;
                 if (tokens.hasNext() && tokens.peek() instanceof Colon) {
                     tokens.next();
                     range = readRange(tokens);
                 }
                 final Parameter nextParameter = new Parameter(name, range);
                 // hook for logging:
                 identifierParsed(identifier, nextParameter);
                 parameters.add(new Pair<Parameter, Pair<Token,Token>>(nextParameter,
                         new Pair<Token, Token>(identifier, tokens.peekPrevious())));
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
     }
 
     private void checkProcessParameters(List<Pair<Parameter, Pair<Token, Token>>> readParameters) throws ParseException {
         final Set<String> names = new HashSet<String>(readParameters.size()*4/3+1);
         for (final Pair<Parameter, Pair<Token, Token>> param: readParameters) {
             final String name = param.getFirst().getName();
             if ("i".equals(name))
                 throw new ParseException("\"i\" cannot be used as parameter identifier",
                     param.getSecond().getFirst().getStartPosition(),
                     param.getSecond().getSecond().getStartPosition());
             if (!names.add(name))
                 throw new ParseException("Duplicate parameter identifier \"" + name + "\"",
                     param.getSecond().getFirst().getStartPosition(),
                     param.getSecond().getSecond().getStartPosition());
         }
     }
 
     /**
      * Read all parameter values up to the next RBracket (this token is read too).
      */
     private ValueList readParameterValues(ExtendedListIterator<Token> tokens) throws ParseException {
 
         final ValueList readParameters = new ValueList();
 
         while (true) {
             if (tokens.peek() instanceof RBracket) {
                 tokens.next();
                 readParameters.trimToSize();
                 return readParameters;
             }
 
             final Value nextValue = readArithmeticExpression(tokens);
 
             readParameters.add(nextValue);
 
             final Token nextToken = tokens.next();
 
             if (nextToken instanceof RBracket) {
                 readParameters.trimToSize();
                 return readParameters;
             }
             if (!(nextToken instanceof Comma))
                 throw new ParseException("Expected ',' or ']'", nextToken);
         }
     }
 
     /**
      * Read one Expression.
      */
     private Expression readExpression(ExtendedListIterator<Token> tokens) throws ParseException {
         // the topmost operator is restriction:
         return readRestrictExpression(tokens);
     }
 
     /**
      * Read the "main expression".
      */
     protected Expression readMainExpression(ExtendedListIterator<Token> tokens) throws ParseException {
         if (!tokens.hasNext() || tokens.peek() instanceof EOFToken)
             throw new ParseException("Missing main expression", tokens.hasNext() ? tokens.next() : tokens.peekPrevious());
         return readExpression(tokens);
     }
 
     /**
      * Read one restriction expression.
      */
     private Expression readRestrictExpression(ExtendedListIterator<Token> tokens) throws ParseException {
         Expression expr = readParallelExpression(tokens);
         while (tokens.peek() instanceof Restrict) {
             tokens.next();
             if (!(tokens.next() instanceof LBrace))
                 throw new ParseException("Expected '{'", tokens.peekPrevious());
             final ChannelSet restricted = readRestrictionChannelSet(tokens);
             expr = ExpressionRepository.getExpression(new RestrictExpression(expr, restricted));
         }
 
         return expr;
     }
 
     /**
      * Read all actions up to the next RBrace (this token is read too).
      */
     private ChannelSet readRestrictionChannelSet(ExtendedListIterator<Token> tokens) throws ParseException {
         final ChannelSet channels = new ChannelSet();
 
         while (true) {
             final Token startToken = tokens.peek();
             final Channel newChannel = readChannel(tokens);
             if (newChannel == null)
                 throw new ParseException("Expected a channel here", startToken);
             if (newChannel instanceof TauChannel)
                 throw new ParseException("Tau channel cannot be restricted", startToken.getStartPosition(), tokens.peekPrevious().getEndPosition());
 
             channels.add(newChannel);
 
             final Token nextToken = tokens.next();
 
             if (nextToken instanceof RBrace)
                 return channels;
 
             if (!(nextToken instanceof Comma))
                 throw new ParseException("Expected ',' or '}'", nextToken);
         }
     }
 
     /**
      * Read an Action.
      *
      * @param tokens
      * @return the read Action, or <code>null</code> if there is no action in the tokens.
      *         In this case, the iterator is not changed.
      * @throws ParseException
      */
     protected Action readAction(ExtendedListIterator<Token> tokens) throws ParseException {
         final Channel channel = readChannel(tokens);
         if (channel == null)
             return null;
         if (channel instanceof TauChannel) {
             if (tokens.peek() instanceof QuestionMark)
                 throw new ParseException("Tau cannot be used as input channel",
                     tokens.peek());
             if (tokens.peek() instanceof Exclamation)
                 throw new ParseException("Tau cannot be used as output channel",
                     tokens.peek());
             return TauAction.get();
         }
 
         if (tokens.peek() instanceof QuestionMark) {
             tokens.next();
             // read the input value
 
             // either a parameter
             if (tokens.peek() instanceof Identifier) {
                 final Identifier identifier = (Identifier)tokens.next();
                 if (identifier.isQuoted())
                     tokens.previous();
                 else {
                     Range range = null;
                     if (tokens.peek() instanceof Colon) {
                         tokens.next();
                         range = readRangeDef(tokens);
                     }
                     final Parameter parameter = new Parameter(identifier.getName(), range);
                     try {
                         parameter.setType(Parameter.Type.INTEGERVALUE, false);
                     } catch (final ParseException e) {
                         // this should never occur since we are setting the type of a fresh parameter
                         throw new ParseException("Only integers can be passed as values. " + e.getMessage(),
                             identifier.getStartPosition(), tokens.peekPrevious().getEndPosition());
                     }
                     // hook for logging:
                     identifierParsed(identifier, parameter);
                     return new InputAction(channel, parameter);
                 }
             }
 
             // ELSE:
             // an arithmetic expression (if it is more complex,
             // it must have parenthesis around it)
 
             final int posStart = tokens.peek().getStartPosition();
             final Value value = readArithmeticBaseExpression(tokens, true); // may return null
             if (value != null)
                 ensureInteger(value, "Only integers can be passed as values.", posStart, tokens.peekPrevious().getEndPosition());
             return new InputAction(channel, value);
         } else if (tokens.peek() instanceof Exclamation) {
             tokens.next();
             // we have an output value (may be null)
             final Value value = readOutputValue(tokens);
             return new OutputAction(channel, value);
         }
 
         // no tau, no input, no output ==> it's a simple action
         return new SimpleAction(channel);
     }
 
     // returns null if there is no output value
     private Value readOutputValue(ExtendedListIterator<Token> tokens) throws ParseException {
         final int posStart = tokens.peek().getStartPosition();
         final Value value = readArithmeticBaseExpression(tokens, true); // may return null
         if (value != null)
             ensureInteger(value, "Only integers can be passed as values.", posStart, tokens.peekPrevious().getEndPosition());
         return value;
     }
 
     private Channel readChannel(ExtendedListIterator<Token> tokens) throws ParseException {
         if (!(tokens.peek() instanceof Identifier))
             return null;
 
         final Identifier identifier = (Identifier)tokens.next();
         Channel channel = null;
         if ("i".equals(identifier.getName())) {
             channel = TauChannel.get();
         } else {
             if (!identifier.isQuoted()) {
                 for (final Parameter param: parameters) {
                     if (param.getName().equals(identifier.getName())) {
                         try {
                             param.setType(Parameter.Type.CHANNEL, false);
                         } catch (final ParseException e) {
                             throw new ParseException(e.getMessage(), identifier);
                         }
                         channel = new ParameterRefChannel(param);
                         break;
                     }
                 }
                 if (channel == null) {
                     final ConstantValue val = constants.get(identifier.getName());
                     if (val != null) {
                         if (val instanceof ConstStringChannel) {
                             channel = (Channel) val;
                         } else if (val instanceof ConstString) {
                             final ConstString str = (ConstString) val;
                             channel = new ConstStringChannel(str.getStringValue(), str.isQuoted());
                         } else {
                             throw new ParseException("This constant cannot be used as channel identifier", identifier);
                         }
                     }
                     if (channel != null) {
                         // check if we have to quote the value
                         if (channel instanceof ConstStringChannel) {
                             final ConstStringChannel csc = (ConstStringChannel) channel;
                             boolean needsQuotes = false;
                             for (final Parameter param: parameters)
                                 needsQuotes |= param.getName().equals(csc.getStringValue());
                             if (needsQuotes && !csc.isQuoted())
                                 channel = new ConstStringChannel(csc.getStringValue(), true);
                         }
                     }
                 }
             }
             if (channel == null && identifier.isLowerCase())
                 channel = new ConstStringChannel(identifier.getName(), identifier.isQuoted());
         }
 
         if (channel == null) {
             tokens.previous();
             return null;
         }
 
         // hook for logging:
         identifierParsed(identifier, channel);
         return channel;
     }
 
     /**
      * Read one parallel expression.
      */
     private Expression readParallelExpression(ExtendedListIterator<Token> tokens) throws ParseException {
         Expression expr = readChoiceExpression(tokens);
         while (tokens.peek() instanceof Parallel) {
             tokens.next();
             final Expression newExpr = readChoiceExpression(tokens);
             expr = ParallelExpression.create(expr, newExpr);
         }
 
         return expr;
     }
 
     /**
      * Read one choice expression.
      */
     private Expression readChoiceExpression(ExtendedListIterator<Token> tokens) throws ParseException {
         Expression expr = readPrefixExpression(tokens);
         while (tokens.peek() instanceof Plus) {
             tokens.next();
             final Expression newExpr = readPrefixExpression(tokens);
             expr = ChoiceExpression.create(expr, newExpr);
         }
 
         return expr;
     }
 
     /**
      * Read one prefix expression.
      */
     private Expression readPrefixExpression(ExtendedListIterator<Token> tokens) throws ParseException {
         final Action action = readAction(tokens);
         if (action == null)
             return readWhenExpression(tokens);
 
         if (tokens.peek() instanceof Dot) {
             tokens.next();
             // if the read action is an InputAction with a parameter, we
             // have to add this parameter to the list of parameters
             Parameter newParam = null;
             if (action instanceof InputAction) {
                 newParam = ((InputAction)action).getParameter();
                 if (newParam != null) {
                     // add the new parameter in front of the list
                     parameters.addFirst(newParam);
                 }
             }
             final Expression target = readPrefixExpression(tokens);
             if (newParam != null) {
                 final Parameter removedParam = parameters.removeFirst();
                 assert removedParam == newParam;
             }
             return ExpressionRepository.getExpression(new PrefixExpression(action, target));
         }
         // otherwise, we append ".0" (i.e. we make a PrefixExpression with target = STOP
         return ExpressionRepository.getExpression(new PrefixExpression(action, StopExpression.get()));
     }
 
     private Expression readWhenExpression(ExtendedListIterator<Token> tokens) throws ParseException {
         if (tokens.peek() instanceof When) {
             tokens.next();
             final int startPos = tokens.peek().getStartPosition();
             final Value condition = readArithmeticExpression(tokens);
             ensureBoolean(condition, "Expected boolean expression after 'when'.", startPos, tokens.peekPrevious().getEndPosition());
 
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
      * Read one base expression (stop, error, expression in parentheses, or recursion variable).
      */
     private Expression readBaseExpression(ExtendedListIterator<Token> tokens) throws ParseException {
         final Token nextToken = tokens.next();
 
         if (nextToken instanceof Stop)
             return ExpressionRepository.getExpression(StopExpression.get());
 
         if (nextToken instanceof ErrorToken)
             return ExpressionRepository.getExpression(ErrorExpression.get());
 
         if (nextToken instanceof LParenthesis) {
             final Expression expr = readExpression(tokens);
             if (!tokens.hasNext() || !(tokens.next() instanceof RParenthesis))
                 throw new ParseException("Expected ')'", tokens.peekPrevious());
             return expr;
         }
 
         if (nextToken instanceof Identifier) {
             final Identifier id = (Identifier) nextToken;
             if (id.isUpperCase()) {
                 final ValueList myParameters;
                 if (tokens.hasNext() && tokens.peek() instanceof LBracket) {
                     tokens.next();
                     myParameters = readParameterValues(tokens);
                 } else
                     myParameters = new ValueList(0);
                 final Expression expression = ExpressionRepository.getExpression(new UnknownRecursiveExpression(id.getName(), myParameters, id.getStartPosition(), tokens.peekPrevious().getEndPosition()));
                 // hook for logging:
                 identifierParsed(id, expression);
                 return expression;
             }
         }
 
         tokens.previous();
         throw new ParseException(nextToken instanceof EOFToken ? "Unexpected end of file" : "Syntax error (unexpected token)", nextToken);
     }
 
     private Value readArithmeticExpression(ExtendedListIterator<Token> tokens) throws ParseException {
         return readArithmeticConditionalExpression(tokens);
     }
 
     private Value readArithmeticConditionalExpression(ExtendedListIterator<Token> tokens) throws ParseException {
         final Token startToken = tokens.peek();
         final Value orValue = readArithmeticOrExpression(tokens);
         if (tokens.peek() instanceof QuestionMark) {
             tokens.next();
             ensureBoolean(orValue, "Boolean expression required before '?:' construct.",
                 startToken.getStartPosition(), tokens.peekPrevious().getEndPosition());
             final Value thenValue = readArithmeticConditionalExpression(tokens);
             if (!(tokens.next() instanceof Colon))
                 throw new ParseException("Expected ':'", tokens.previous());
             final Value elseValue = readArithmeticConditionalExpression(tokens);
             ensureEqualTypes(thenValue, elseValue, "Expression in '?:' construct must have the same type.",
                 startToken.getStartPosition(), tokens.peekPrevious().getEndPosition());
             return ConditionalValue.create(orValue, thenValue, elseValue);
         }
 
         return orValue;
     }
 
     private Value readArithmeticOrExpression(ExtendedListIterator<Token> tokens) throws ParseException {
         Token startToken = tokens.peek();
         Value value = readArithmeticAndExpression(tokens);
         while (tokens.peek() instanceof Or) {
             tokens.next();
             ensureBoolean(value, "Boolean expression required before '||'.",
                 startToken.getStartPosition(), tokens.peekPrevious().getEndPosition());
             startToken = tokens.peek();
             final Value secondValue = readArithmeticAndExpression(tokens);
             ensureBoolean(secondValue, "Boolean expression required after '||'.",
                 startToken.getStartPosition(), tokens.peekPrevious().getEndPosition());
             value = OrValue.create(value, secondValue);
         }
 
         return value;
     }
 
     private Value readArithmeticAndExpression(ExtendedListIterator<Token> tokens) throws ParseException {
         Token startToken = tokens.peek();
         Value value = readArithmeticEqExpression(tokens);
         while (tokens.peek() instanceof And) {
             tokens.next();
             ensureBoolean(value, "Boolean expression required before '&&'.",
                 startToken.getStartPosition(), tokens.peekPrevious().getEndPosition());
             startToken = tokens.peek();
             final Value secondValue = readArithmeticEqExpression(tokens);
             ensureBoolean(secondValue, "Boolean expression required after '&&'.",
                 startToken.getStartPosition(), tokens.peekPrevious().getEndPosition());
             value = AndValue.create(value, secondValue);
         }
 
         return value;
     }
 
     private Value readArithmeticEqExpression(ExtendedListIterator<Token> tokens) throws ParseException {
         final Token startToken = tokens.peek();
         Value value = readArithmeticCompExpression(tokens);
         while (tokens.peek() instanceof Equals
                 || tokens.peek() instanceof Neq) {
             final boolean isNeq = tokens.next() instanceof Neq;
             final Value secondValue = readArithmeticCompExpression(tokens);
             final int posAfter = tokens.peekPrevious().getEndPosition();
             ensureEqualTypes(value, secondValue, "Values to compare must have the same type.",
                 startToken.getStartPosition(), posAfter);
             value = EqValue.create(value, secondValue, isNeq);
         }
 
         return value;
     }
 
     private Value readArithmeticCompExpression(ExtendedListIterator<Token> tokens) throws ParseException {
         Token startToken = tokens.peek();
         final Value value = readArithmeticShiftExpression(tokens);
 
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
             Token endToken = tokens.peekPrevious();
             tokens.next();
             ensureInteger(value, "Only integer values can be compared.",
                 startToken.getStartPosition(), endToken.getEndPosition());
             startToken = tokens.peek();
             final Value secondValue = readArithmeticShiftExpression(tokens);
             endToken = tokens.peekPrevious();
             ensureInteger(secondValue, "Only integer values can be compared.",
                 startToken.getStartPosition(), endToken.getEndPosition());
             return CompValue.create(value, secondValue, type);
         }
 
         return value;
     }
 
     private Value readArithmeticShiftExpression(ExtendedListIterator<Token> tokens) throws ParseException {
         Token startToken = tokens.peek();
         Value value = readArithmeticAddExpression(tokens);
         while (tokens.peek() instanceof LeftShift
                 || tokens.peek() instanceof RightShift) {
             ensureInteger(value, "Only integer values can be shifted.",
                 startToken.getStartPosition(), tokens.peekPrevious().getEndPosition());
             final boolean shiftRight = tokens.next() instanceof RightShift;
             startToken = tokens.peek();
             final Value secondValue = readArithmeticAddExpression(tokens);
             ensureInteger(secondValue, "Shifting width must be an integer.",
                 startToken.getStartPosition(), tokens.peekPrevious().getEndPosition());
             value = ShiftValue.create(value, secondValue, shiftRight);
         }
 
         return value;
     }
 
     private Value readArithmeticAddExpression(ExtendedListIterator<Token> tokens) throws ParseException {
         Token startToken = tokens.peek();
         Value value = readArithmeticMultExpression(tokens);
         while (tokens.peek() instanceof Plus
                 || tokens.peek() instanceof Minus) {
             ensureInteger(value, "Both sides of an addition must be integers.",
                 startToken.getStartPosition(), tokens.peekPrevious().getEndPosition());
             final boolean isSubtraction = tokens.next() instanceof Minus;
             startToken = tokens.peek();
             final Value secondValue = readArithmeticMultExpression(tokens);
             ensureInteger(secondValue, "Both sides of an addition must be integers.",
                 startToken.getStartPosition(), tokens.peekPrevious().getEndPosition());
             value = AddValue.create(value, secondValue, isSubtraction);
         }
 
         return value;
     }
 
     private Value readArithmeticMultExpression(ExtendedListIterator<Token> tokens) throws ParseException {
         Token startToken = tokens.peek();
         Value value = readArithmeticUnaryExpression(tokens);
         while (true) {
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
 
             ensureInteger(value, "Both sides of a multiplication/division must be integer expressions.",
                 startToken.getStartPosition(), tokens.peekPrevious().getEndPosition());
             tokens.next();
             startToken = tokens.peek();
             final Value secondValue = readArithmeticUnaryExpression(tokens);
             ensureInteger(secondValue, "Both sides of a multiplication/division must be integer expressions.",
                 startToken.getStartPosition(), tokens.peekPrevious().getEndPosition());
             try {
                 value = MultValue.create(value, secondValue, type);
             } catch (final ArithmeticError e) {
                 throw new ParseException("Arithmetic error: " + e.getMessage(),
                     startToken.getStartPosition(), tokens.peekPrevious().getEndPosition());
             }
         }
 
         return value;
     }
 
     private Value readArithmeticUnaryExpression(ExtendedListIterator<Token> tokens) throws ParseException {
         final Token nextToken = tokens.peek();
         if (nextToken instanceof Exclamation) {
             tokens.next();
             final int posStart = tokens.peek().getStartPosition();
             final Value negatedValue = readArithmeticUnaryExpression(tokens);
             ensureBoolean(negatedValue, "The negated value must be a boolean expression.", posStart, tokens.peekPrevious().getEndPosition());
             return NotValue.create(negatedValue);
         } else if (nextToken instanceof Plus) {
             tokens.next();
             return readArithmeticUnaryExpression(tokens);
         } else if (nextToken instanceof Minus) {
             tokens.next();
             final int posStart = tokens.peek().getStartPosition();
             final Value negativeValue = readArithmeticUnaryExpression(tokens);
             ensureInteger(negativeValue, "The negated value must be an integer expression.", posStart, tokens.peekPrevious().getEndPosition());
             return NegativeValue.create(negativeValue);
         }
 
         // else:
         return readArithmeticBaseExpression(tokens, false);
     }
 
     private Value readArithmeticBaseExpression(ExtendedListIterator<Token> tokens,
             boolean allowNull) throws ParseException {
         final Token nextToken = tokens.next();
         if (nextToken instanceof IntegerToken)
             return new ConstIntegerValue(((IntegerToken)nextToken).getValue());
         // a stop is the integer "0" here...
         if (nextToken instanceof Stop) {
             // change the token in the token list (for highlighting etc.)
             tokens.set(tokens.previousIndex(),
                     new IntegerToken(nextToken.getStartPosition(),
                             nextToken.getEndPosition(), 0));
             return new ConstIntegerValue(0);
         }
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
                     if (param.getName().equals(name)) {
                         final ParameterReference parameterReference = new ParameterReference(param);
                         // hook for logging:
                         identifierParsed(id, parameterReference);
                         return parameterReference;
                     }
                 // search if it is a constant
                 final ConstantValue constant = constants.get(name);
                 if (constant != null) {
                     // hook for logging:
                     identifierParsed(id, constant);
                     return constant;
                 }
             }
             final ConstString constString = new ConstString(name, id.isQuoted());
             // hook for logging:
             identifierParsed(id, constString);
             return constString;
         }
         if (nextToken instanceof LParenthesis) {
             final Value value = readArithmeticExpression(tokens);
             if (!(tokens.next() instanceof RParenthesis))
                 throw new ParseException("Expected ')'.", tokens.peekPrevious());
             return value;
         }
         tokens.previous();
         if (!allowNull)
             throw new ParseException("Expected arithmetic expression", nextToken);
         return null;
     }
 
     private void ensureEqualTypes(Value value1, Value value2, String message, int startPos, int endPos) throws ParseException {
         if (value1 instanceof IntegerValue && value2 instanceof IntegerValue)
             return;
         if (value1 instanceof BooleanValue && value2 instanceof BooleanValue)
             return;
         if (value1 instanceof ConstString && value2 instanceof ConstString)
             return;
         try {
             if (value1 instanceof ParameterReference || value1 instanceof ParameterRefChannel) {
                 ((ParameterReference)value1).getParam().match(value2, false);
                 return;
             }
             if (value2 instanceof ParameterReference || value2 instanceof ParameterRefChannel) {
                 ((ParameterReference)value2).getParam().match(value1, false);
                 return;
             }
         } catch (final ParseException e) {
             throw new ParseException(e.getMessage(), startPos, endPos);
         }
         if (value1 instanceof ConditionalValue) {
             ensureEqualTypes(((ConditionalValue)value1).getThenValue(), value2, message, startPos, endPos);
             ensureEqualTypes(((ConditionalValue)value1).getElseValue(), value2, message, startPos, endPos);
         } else if (value2 instanceof ConditionalValue) {
             ensureEqualTypes(value1, ((ConditionalValue)value2).getThenValue(), message, startPos, endPos);
             ensureEqualTypes(value1, ((ConditionalValue)value2).getElseValue(), message, startPos, endPos);
             return;
         }
         throw new ParseException(message + " The values \"" + value1 + "\" and \"" + value2 + "\" have different types.", startPos, endPos);
     }
 
     private void ensureBoolean(Value value, String message, int startPos, int endPos) throws ParseException {
         if (value instanceof BooleanValue)
             return;
         if (value instanceof IntegerValue)
             throw new ParseException(message + " The value \"" + value + "\" has type integer.", startPos, endPos);
         if (value instanceof ConstString)
             throw new ParseException(message + " The value \"" + value + "\" has type string.", startPos, endPos);
         if (value instanceof ParameterReference) {
             try {
                 ((ParameterReference)value).getParam().setType(Parameter.Type.BOOLEANVALUE, false);
             } catch (final ParseException e) {
                 throw new ParseException(message + " " + e.getMessage(), startPos, endPos);
             }
             return;
         }
         if (value instanceof ConditionalValue) {
             ensureBoolean(((ConditionalValue)value).getThenValue(), message, startPos, endPos);
             ensureBoolean(((ConditionalValue)value).getElseValue(), message, startPos, endPos);
             return;
         }
         assert false;
         throw new ParseException(message, startPos, endPos);
     }
 
     private void ensureInteger(Value value, String message, int startPos, int endPos) throws ParseException {
         if (value instanceof IntegerValue)
             return;
         if (value instanceof BooleanValue)
             throw new ParseException(message + " The value \"" + value + "\" has type boolean.", startPos, endPos);
         if (value instanceof ConstString)
             throw new ParseException(message + " The value \"" + value + "\" has type string.", startPos, endPos);
         if (value instanceof ParameterReference) {
             try {
                 ((ParameterReference)value).getParam().setType(Parameter.Type.INTEGERVALUE, false);
             } catch (final ParseException e) {
                 throw new ParseException(message + " " + e.getMessage(), startPos, endPos);
             }
             return;
         }
         if (value instanceof ConditionalValue) {
             ensureInteger(((ConditionalValue)value).getThenValue(), message, startPos, endPos);
             ensureInteger(((ConditionalValue)value).getElseValue(), message, startPos, endPos);
             return;
         }
         assert false;
         throw new ParseException(message, startPos, endPos);
     }
 
     protected void identifierParsed(Identifier identifier, Object semantic) {
         // ignore in this implementation
     }
 
     protected void changedIdentifierMeaning(ConstString constString,
             Range range) {
         // ignore in this implementation
     }
 
     public void addProblemListener(IParsingProblemListener listener) {
         listeners.add(listener);
     }
 
     public void removeProblemListener(IParsingProblemListener listener) {
         listeners.remove(listener);
     }
 
     public void reportProblem(ParsingProblem problem) {
         for (final IParsingProblemListener listener: listeners)
             listener.reportParsingProblem(problem);
     }
 
     protected void reportUnboundInputParameter(Action act, Action origin) {
         reportProblem(new ParsingProblem(ParsingProblem.ERROR,
             "The action \"" + act + "\" is not restricted and without a range. "
                 + "This would lead to infinitely many transitions.",
             -1, -1));
     }
 
 }
