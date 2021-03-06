 package org.mvel;
 
 import static org.mvel.Operator.*;
 import org.mvel.ast.*;
 import org.mvel.integration.Interceptor;
 import org.mvel.util.ExecutionStack;
 import org.mvel.util.ParseTools;
 import static org.mvel.util.ParseTools.*;
 import static org.mvel.util.PropertyTools.isDigit;
 import static org.mvel.util.PropertyTools.isIdentifierPart;
 import org.mvel.util.ThisLiteral;
 
 import static java.lang.Boolean.FALSE;
 import static java.lang.Boolean.TRUE;
 import static java.lang.Character.isWhitespace;
 import static java.lang.System.arraycopy;
 import static java.util.Collections.synchronizedMap;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.WeakHashMap;
 
 /**
  * @author Christopher Brock
  */
 public class AbstractParser {
     protected char[] expr;
     protected int cursor;
     protected int length;
     protected int fields;
 
     protected boolean greedy = true;
     protected boolean lastWasIdentifier = false;
     protected boolean lastWasLineLabel = false;
 
     protected boolean debugSymbols = false;
     private int line = 1;
 
     protected ASTNode lastNode;
 
     protected static final int FRAME_END = -1;
     protected static final int FRAME_CONTINUE = 0;
     protected static final int FRAME_NEXT = 1;
     protected static final int FRAME_RETURN = 2;
 
     private static Map<String, char[]> EX_PRECACHE;
 
     public static final Map<String, Object> LITERALS =
             new HashMap<String, Object>(35, 0.4f);
 
     public static final Map<String, Integer> OPERATORS =
             new HashMap<String, Integer>(25 * 2, 0.4f);
 
     protected Map<String, Class> imports;
     protected Map<String, Interceptor> interceptors;
 
     protected ExecutionStack splitAccumulator = new ExecutionStack();
 
     protected static ThreadLocal<ParserContext> parserContext;
 
     protected String sourceFile;
 
     static class ParserContext {
         private String sourceFile;
         private int lineCount;
 
         public ParserContext(String sourceFile, int lineCount) {
             this.sourceFile = sourceFile;
             this.lineCount = lineCount;
         }
 
         public String getSourceFile() {
             return sourceFile;
         }
 
         public void setSourceFile(String sourceFile) {
             this.sourceFile = sourceFile;
         }
 
         public int getLineCount() {
             return lineCount;
         }
 
         public void setLineCount(int lineCount) {
             this.lineCount = lineCount;
         }
     }
 
 
     static {
         configureFactory();
 
         /**
          * Setup the basic literals
          */
         AbstractParser.LITERALS.put("true", TRUE);
         AbstractParser.LITERALS.put("false", FALSE);
 
         AbstractParser.LITERALS.put("null", null);
         AbstractParser.LITERALS.put("nil", null);
 
         AbstractParser.LITERALS.put("empty", BlankLiteral.INSTANCE);
 
         AbstractParser.LITERALS.put("this", ThisLiteral.class);
 
         /**
          * Add System and all the class wrappers from the JCL.
          */
         LITERALS.put("System", System.class);
 
         LITERALS.put("String", String.class);
 
         LITERALS.put("Integer", Integer.class);
         LITERALS.put("int", Integer.class);
 
         LITERALS.put("Long", Long.class);
         LITERALS.put("long", Long.class);
 
         LITERALS.put("Boolean", Boolean.class);
         LITERALS.put("boolean", Boolean.class);
 
         LITERALS.put("Short", Short.class);
         LITERALS.put("short", Short.class);
 
         LITERALS.put("Character", Character.class);
         LITERALS.put("char", Character.class);
 
         LITERALS.put("Double", Double.class);
         LITERALS.put("double", double.class);
 
         LITERALS.put("Float", Float.class);
         LITERALS.put("float", float.class);
 
         LITERALS.put("Math", Math.class);
         LITERALS.put("Void", Void.class);
         LITERALS.put("Object", Object.class);
 
         LITERALS.put("Class", Class.class);
         LITERALS.put("ClassLoader", ClassLoader.class);
         LITERALS.put("Runtime", Runtime.class);
         LITERALS.put("Thread", Thread.class);
         LITERALS.put("Compiler", Compiler.class);
         LITERALS.put("StringBuffer", StringBuffer.class);
         LITERALS.put("ThreadLocal", ThreadLocal.class);
         LITERALS.put("SecurityManager", SecurityManager.class);
         LITERALS.put("StrictMath", StrictMath.class);
 
         LITERALS.put("Array", java.lang.reflect.Array.class);
 
         float version = Float.parseFloat(System.getProperty("java.version").substring(0, 2));
         if (version >= 1.5) {
             try {
                 LITERALS.put("StringBuilder", Class.forName("java.lang.StringBuilder"));
             }
             catch (Exception e) {
                 throw new RuntimeException("cannot resolve a built-in literal", e);
             }
         }
 
        _loadLanguageFeaturesByLevel(4);
     }
 
 
     static void configureFactory() {
         if (MVEL.THREAD_SAFE) {
             EX_PRECACHE = synchronizedMap(new WeakHashMap<String, char[]>(10));
         }
         else {
             EX_PRECACHE = new WeakHashMap<String, char[]>(10);
         }
     }
 
     /**
      * Retrieve the next token in the expression.
      *
      * @return -
      */
     protected ASTNode nextToken() {
         /**
          * If the cursor is at the end of the expression, we have nothing more to do:
          * return null.
          */
         if (cursor >= length) {
             return null;
         }
         else if (!splitAccumulator.isEmpty()) {
             return lastNode = (ASTNode) splitAccumulator.pop();
         }
 
         int brace, start = cursor;
 
         /**
          * Because of parser recursion for sub-expression parsing, we sometimes need to remain
          * certain field states.  We do not reset for assignments, boolean mode, list creation or
          * a capture only mode.
          */
         fields = fields & (ASTNode.INLINE_COLLECTION | ASTNode.COMPILE_IMMEDIATE);
 
         boolean capture = false;
         boolean union = false;
 
         if (!debugSymbols && parserContext != null && parserContext.get() != null) {
             debugSymbols = true;
             if (expr[cursor] != '\n') lastWasLineLabel = true;
         }
 
         if (debugSymbols && !lastWasLineLabel && (expr[cursor] == '\n' || cursor == 0)) {
             if (parserContext == null) {
                 if (sourceFile == null) {
                     throw new CompileException("unable to produce debugging symbols: source name must be provided.");
                 }
 
                 (parserContext = new ThreadLocal<ParserContext>())
                         .set(new ParserContext(sourceFile, 0));
             }
             else {
                 line = parserContext.get().getLineCount();
             }
 
             lastWasLineLabel = true;
 
             int scan = cursor;
 
             while (expr[scan] == '\n') {
                 scan++;
                 line++;
             }
 
             ParserContext pCtx = parserContext.get();
             pCtx.setLineCount(line);
             return new LineLabel(pCtx.getSourceFile(), line);
         }
         else {
             lastWasLineLabel = false;
         }
 
         /**
          * Skip any whitespace currently under the starting point.
          */
         while (start < length && isWhitespace(expr[start])) start++;
 
         /**
          * From here to the end of the method is the core MVEL parsing code.  Fiddling around here is asking for
          * trouble unless you really know what you're doing.
          */
         for (cursor = start; cursor < length;) {
             if (isIdentifierPart(expr[cursor])) {
                 /**
                  * If the current character under the cursor is a valid
                  * part of an identifier, we keep capturing.
                  */
                 capture = true;
                 cursor++;
             }
             else if (capture) {
                 String t;
                 if (OPERATORS.containsKey(t = new String(expr, start, cursor - start))) {
                     switch (OPERATORS.get(t)) {
                         case NEW:
                             start = cursor + 1;
                             captureToEOT();
                             return new NewObjectNode(subArray(start, cursor), fields);
 
                         case ASSERT:
                             start = cursor + 1;
                             captureToEOS();
                             return new AssertNode(subArray(start, cursor--), fields);
 
                         case RETURN:
                             start = cursor + 1;
                             captureToEOS();
                             return new ReturnNode(subArray(start, cursor), fields);
 
                         case IF:
                             fields |= ASTNode.BLOCK_IF;
                             return captureCodeBlock();
 
                         case FOREACH:
                             fields |= ASTNode.BLOCK_FOREACH;
                             return captureCodeBlock();
 
                         case WITH:
                             fields |= ASTNode.BLOCK_WITH;
                             return captureCodeBlock();
 
                         case IMPORT:
                             start = cursor + 1;
                             captureToEOS();
                             ImportNode importNode = new ImportNode(subArray(start, cursor--), fields);
                             addImport(getSimpleClassName(importNode.getImportClass()), importNode.getImportClass());
                             return importNode;
 
                         case IMPORT_STATIC:
                             start = cursor + 1;
                             captureToEOS();
                             return new StaticImportNode(subArray(start, cursor--), fields);
                     }
                 }
 
                 /**
                  * If we *were* capturing a token, and we just hit a non-identifier
                  * character, we stop and figure out what to do.
                  */
                 skipWhitespace();
 
                 if (expr[cursor] == '(') {
                     fields |= ASTNode.METHOD;
 
                     /**
                      * If the current token is a method call or a constructor, we
                      * simply capture the entire parenthesized range and allow
                      * reduction to be dealt with through sub-parsing the property.
                      */
                     cursor++;
                     for (brace = 1; cursor < length && brace > 0;) {
                         switch (expr[cursor++]) {
                             case'(':
                                 brace++;
                                 break;
                             case')':
                                 brace--;
                                 break;
                         }
                     }
 
                     /**
                      * If the brace counter is greater than 0, we know we have
                      * unbalanced braces in the expression.  So we throw a
                      * optimize error now.
                      */
                     if (brace > 0)
                         throw new CompileException("unbalanced braces in expression: (" + brace + "):", expr, cursor);
                 }
 
                 /**
                  * If we encounter any of the following cases, we are still dealing with
                  * a contiguous token.
                  */
                 if (cursor < length) {
                     switch (expr[cursor]) {
                         case'+':
                             if (isAt('+', 1)) {
                                 ASTNode n = new PostFixIncNode(subArray(start, cursor), fields);
                                 cursor += 2;
                                 return n;
                             }
                             else {
                                 break;
                             }
 
                         case'-':
                             if (isAt('-', 1)) {
                                 ASTNode n = new PostFixDecNode(subArray(start, cursor), fields);
                                 cursor += 2;
                                 return n;
                             }
                             else {
                                 break;
                             }
 
                         case']':
                         case'[':
                             balancedCapture('[');
                             cursor++;
                             continue;
                         case'.':
                             union = true;
                             cursor++;
                             continue;
                         case'=':
                             if (greedy && !isAt('=', 1)) {
                                 cursor++;
 
                                 fields |= ASTNode.ASSIGN;
 
                                 skipWhitespace();
                                 captureToEOS();
 
                                 if (union) {
                                     return new DeepAssignmentNode(subArray(start, cursor), fields);
                                 }
                                 else if (lastWasIdentifier) {
                                     /**
                                      * Check for typing information.
                                      */
                                     if (lastNode.getLiteralValue() instanceof String) {
                                         if (hasImport((String) lastNode.getLiteralValue())) {
                                             lastNode.setLiteralValue(getImport((String) lastNode.getLiteralValue()));
                                             lastNode.setAsLiteral();
                                         }
                                         else {
                                             try {
                                                 /**
                                                  *  take a stab in the dark and try and load the class
                                                  */
                                                 lastNode.setLiteralValue(createClass((String) lastNode.getLiteralValue()));
                                                 lastNode.setAsLiteral();
                                             }
                                             catch (ClassNotFoundException e) {
                                                 /**
                                                  * Just fail through.
                                                  */
                                             }
                                         }
                                     }
 
                                     if (lastNode.isLiteral()) {
                                         if (lastNode.getLiteralValue() instanceof Class) {
                                             lastNode.setDiscard(true);
 
                                             captureToEOS();
                                             return new TypedVarNode(subArray(start, cursor), fields, (Class)
                                                     lastNode.getLiteralValue());
                                         }
                                     }
 
                                     throw new ParseException("unknown class: " + lastNode.getLiteralValue());
                                 }
                                 else {
                                     return new AssignmentNode(subArray(start, cursor), fields);
                                 }
                             }
                     }
                 }
 
                 /**
                  * Produce the token.
                  */
                 trimWhitespace();
 
                 return createToken(expr, start, cursor, fields);
             }
             else
                 switch (expr[cursor]) {
                     case'@': {
                         start++;
                         captureToEOT();
 
                         String interceptorName = new String(expr, start, cursor - start);
 
                         if (!interceptors.containsKey(interceptorName)) {
                             throw new CompileException("reference to undefined interceptor: " + interceptorName, expr, cursor);
                         }
 
                         return new InterceptorWrapper(interceptors.get(interceptorName), nextToken());
                     }
 
                     case'=':
                         return createToken(expr, start, (cursor += 2), fields);
 
                     case'-':
                         if (isAt('-', 1)) {
                             start = cursor += 2;
                             captureToEOT();
                             return new PreFixDecNode(subArray(start, cursor), fields);
                         }
                         else if ((cursor > 0 && !isWhitespace(lookBehind(1))) || !isDigit(lookAhead(1))) {
                             return createToken(expr, start, cursor++ + 1, fields);
                         }
                         else if ((cursor - 1) < 0 || (!isDigit(lookBehind(1))) && isDigit(lookAhead(1))) {
                             cursor++;
                             break;
                         }
 
                     case'+':
                         if (isAt('+', 1)) {
                             start = cursor += 2;
                             captureToEOT();
                             return new PreFixIncNode(subArray(start, cursor), fields);
                         }
                         return createToken(expr, start, cursor++ + 1, fields);
 
                     case'*':
                         if (isAt('*', 1)) {
                             cursor++;
                         }
                         return createToken(expr, start, cursor++ + 1, fields);
 
                     case';':
                         cursor++;
                         lastWasIdentifier = false;
                         return lastNode = new EndOfStatement();
 
                     case'#':
                     case'/':
                         if (isAt(expr[cursor], 1)) {
                             /**
                              * Handle single line comments.
                              */
                             while (cursor < length && expr[cursor] != '\n') cursor++;
                             if ((start = ++cursor) >= length) return null;
                             continue;
                         }
                         else if (expr[cursor] == '/' && isAt('*', 1)) {
                             /**
                              * Handle multi-line comments.
                              */
                             int len = length - 1;
                             while (true) {
                                 cursor++;
 
                                 /**
                                  * Since multi-line comments may cross lines, we must keep track of any line-break
                                  * we encounter.
                                  */
                                 if (debugSymbols && expr[cursor] == '\n') {
                                     line++;
                                 }
 
                                 if (cursor == len) {
                                     throw new CompileException("unterminated block comment", expr, cursor);
                                 }
                                 if (expr[cursor] == '*' && isAt('/', 1)) {
                                     if ((cursor += 2) >= length) return null;
                                     skipWhitespace();
                                     start = cursor;
                                     break;
                                 }
                             }
                             continue;
                         }
 
                     case'?':
                     case':':
                     case'^':
                     case'%': {
                         return createToken(expr, start, cursor++ + 1, fields);
                     }
 
                     case'(': {
                         cursor++;
 
                         for (brace = 1; cursor < length && brace > 0;) {
                             switch (expr[cursor++]) {
                                 case'(':
                                     brace++;
                                     break;
                                 case')':
                                     brace--;
                                     break;
                                 case'i':
                                     if (cursor < length && expr[cursor] == 'n' && isWhitespace(expr[cursor + 1])) {
                                         fields |= ASTNode.FOLD;
                                     }
                                     break;
                             }
                         }
                         if (brace > 0)
                             throw new CompileException("unbalanced braces in expression: (" + brace + "):", expr, cursor);
 
                         if ((fields & ASTNode.FOLD) != 0) {
                             if (cursor < length && expr[cursor] == '.') {
                                 cursor++;
                                 continue;
                             }
 
                             return createToken(expr, start, cursor, ASTNode.FOLD);
                         }
 
                         return new Substatement(expr, start + 1, cursor - 1, fields);
                     }
 
                     case'}':
                     case']':
                     case')': {
                         throw new ParseException("unbalanced braces", expr, cursor);
                     }
 
                     case'>': {
                         if (expr[cursor + 1] == '>') {
                             if (expr[cursor += 2] == '>') cursor++;
                             return createToken(expr, start, cursor, fields);
                         }
                         else if (expr[cursor + 1] == '=') {
                             return createToken(expr, start, cursor += 2, fields);
                         }
                         else {
                             return createToken(expr, start, ++cursor, fields);
                         }
                     }
 
                     case'<': {
                         if (expr[++cursor] == '<') {
                             if (expr[++cursor] == '<') cursor++;
                             return createToken(expr, start, cursor, fields);
                         }
                         else if (expr[cursor] == '=') {
                             return createToken(expr, start, ++cursor, fields);
                         }
                         else {
                             return createToken(expr, start, cursor, fields);
                         }
                     }
 
                     case'\'':
                         while (++cursor < length && expr[cursor] != '\'') {
                             if (expr[cursor] == '\\') handleEscapeSequence(expr[++cursor]);
                         }
 
                         if (cursor == length || expr[cursor] != '\'') {
                             throw new CompileException("unterminated literal", expr, cursor);
                         }
 
                         return createToken(expr, start + 1, ++cursor - 1, ASTNode.STR_LITERAL | ASTNode.LITERAL);
 
 
                     case'"':
                         while (++cursor < length && expr[cursor] != '"') {
                             if (expr[cursor] == '\\') handleEscapeSequence(expr[++cursor]);
                         }
                         if (cursor == length || expr[cursor] != '"') {
                             throw new CompileException("unterminated literal", expr, cursor);
                         }
                         else {
                             return createToken(expr, start + 1, ++cursor - 1, ASTNode.STR_LITERAL | ASTNode.LITERAL);
                         }
 
                     case'&': {
                         if (expr[cursor++ + 1] == '&') {
                             return createToken(expr, start, ++cursor, fields);
                         }
                         else {
                             return createToken(expr, start, cursor, fields);
                         }
                     }
 
                     case'|': {
                         if (expr[cursor++ + 1] == '|') {
                             return createToken(expr, start, ++cursor, fields);
                         }
                         else {
                             return createToken(expr, start, cursor, fields);
                         }
                     }
 
                     case'~':
                         if ((cursor - 1 < 0 || !isIdentifierPart(lookBehind(1)))
                                 && isDigit(expr[cursor + 1])) {
 
                             fields |= ASTNode.INVERT;
                             start++;
                             cursor++;
                             break;
                         }
                         else if (expr[cursor + 1] == '(') {
                             fields |= ASTNode.INVERT;
                             start = ++cursor;
                             continue;
                         }
                         else {
                             if (expr[cursor + 1] == '=') cursor++;
                             return createToken(expr, start, ++cursor, fields);
                         }
 
                     case'!': {
                         if (isIdentifierPart(expr[++cursor]) || expr[cursor] == '(') {
                             start = cursor;
                             fields |= ASTNode.NEGATION;
                             continue;
                         }
                         else if (expr[cursor] != '=')
                             throw new CompileException("unexpected operator '!'", expr, cursor, null);
                         else {
                             return createToken(expr, start, ++cursor, fields);
                         }
                     }
 
                     case'[':
                     case'{':
                         if (balancedCapture(expr[cursor]) == -1) {
                             if (cursor >= length) cursor--;
                             throw new CompileException("unbalanced brace: in inline map/list/array creation", expr, cursor);
                         }
 
                         if (cursor < (length - 1) && expr[cursor + 1] == '.') {
                             fields |= ASTNode.INLINE_COLLECTION;
                             cursor++;
                             continue;
                         }
 
                         return new InlineCollectionNode(expr, start, ++cursor, fields);
 
                     default:
                         cursor++;
                 }
         }
 
         return createPropertyToken(start, cursor);
     }
 
 
     protected int balancedCapture(char type) {
         int depth = 1;
         char term = type;
         switch (type) {
             case'[':
                 term = ']';
                 break;
             case'{':
                 term = '}';
                 break;
             case'(':
                 term = ')';
                 break;
         }
 
         for (cursor++; cursor < length; cursor++) {
             if (expr[cursor] == type) {
                 depth++;
             }
             else if (expr[cursor] == term && --depth == 0) {
                 return cursor;
             }
         }
 
         return -1;
     }
 
 
     /**
      * Most of this method should be self-explanatory.
      *
      * @param expr   -
      * @param start  -
      * @param end    -
      * @param fields -
      * @return -
      */
     private ASTNode createToken(final char[] expr, final int start, final int end, int fields) {
         ASTNode tk = new ASTNode(expr, start, end, fields);
         lastWasIdentifier = tk.isIdentifier();
         return lastNode = tk;
     }
 
     private char[] subArray(final int start, final int end) {
         char[] newA = new char[end - start];
         arraycopy(expr, start, newA, 0, newA.length);
         return newA;
     }
 
     private ASTNode createPropertyToken(int start, int end) {
         return new PropertyASTNode(expr, start, end, fields);
     }
 
     private ASTNode createBlockToken(final int condStart,
                                      final int condEnd, final int blockStart, final int blockEnd) {
 
         lastWasIdentifier = false;
 
         cursor++;
 
         if (!isStatementManuallyTerminated()) {
             splitAccumulator.add(new EndOfStatement());
         }
 
         if (isFlag(ASTNode.BLOCK_IF)) {
             return new IfNode(subArray(condStart, condEnd), subArray(blockStart, blockEnd), fields);
         }
         else if (isFlag(ASTNode.BLOCK_FOREACH)) {
             return new ForEachNode(subArray(condStart, condEnd), subArray(blockStart, blockEnd), fields);
         }
         // DONT'T REMOVE THIS COMMENT!
         //       else if (isFlag(ASTNode.BLOCK_WITH)) {
         else {
             return new WithNode(subArray(condStart, condEnd), subArray(blockStart, blockEnd), fields);
         }
     }
 
     private ASTNode captureCodeBlock() {
         boolean cond = true;
 
         ASTNode first = null;
         ASTNode tk = null;
 
         if (isFlag(ASTNode.BLOCK_IF)) {
             do {
                 if (tk != null) {
                     skipToNextTokenJunction();
                     skipWhitespace();
 
                     cond = expr[cursor] != '{' && expr[cursor] == 'i' && expr[++cursor] == 'f'
                             && (isWhitespace(expr[++cursor]) || expr[cursor] == '(');
                 }
 
                 if (((IfNode) (tk = _captureBlock(tk, expr, cond))).getElseBlock() != null) {
                     cursor++;
                     return first;
                 }
 
                 if (first == null) first = tk;
 
                 if (cursor < length && expr[cursor] != ';') {
                     cursor++;
                 }
             }
             while (blockContinues());
         }
         else if (isFlag(ASTNode.BLOCK_FOREACH) || isFlag(ASTNode.BLOCK_WITH)) {
             skipToNextTokenJunction();
             skipWhitespace();
             return _captureBlock(null, expr, true);
         }
 
 
         return first;
     }
 
     private ASTNode _captureBlock(ASTNode node, final char[] expr, boolean cond) {
         skipWhitespace();
         int startCond = 0;
         int endCond = 0;
 
         if (cond) {
             startCond = ++cursor;
             endCond = balancedCapture('(');
             cursor++;
         }
 
         int blockStart;
         int blockEnd;
 
         skipWhitespace();
 
         if (expr[cursor] == '{') {
             blockStart = cursor;
             if ((blockEnd = balancedCapture('{')) == -1) {
                 throw new CompileException("unbalanced braces { }", expr, cursor);
             }
         }
         else {
             blockStart = cursor - 1;
             captureToEOLorOF();
             blockEnd = cursor + 1;
         }
 
         if (isFlag(ASTNode.BLOCK_IF)) {
             IfNode ifNode = (IfNode) node;
 
             if (node != null) {
                 if (!cond) {
                     ifNode.setElseBlock(subArray(trimRight(blockStart + 1), trimLeft(blockEnd - 1)));
                     return node;
                 }
                 else {
                     IfNode tk = (IfNode) createBlockToken(startCond, endCond, trimRight(blockStart + 1),
                             trimLeft(blockEnd));
 
                     ifNode.setElseIf(tk);
 
                     return tk;
                 }
             }
             else {
                 return createBlockToken(startCond, endCond, blockStart + 1,
                         blockEnd);
             }
         }
         // DON"T REMOVE THIS COMMENT!
         // else if (isFlag(ASTNode.BLOCK_FOREACH) || isFlag(ASTNode.BLOCK_WITH)) {
         else {
             return createBlockToken(startCond, endCond, trimRight(blockStart + 1), trimLeft(blockEnd));
         }
     }
 
     protected boolean blockContinues() {
         if ((cursor + 4) < length) {
             skipWhitespace();
             return expr[cursor] == 'e' && expr[cursor + 1] == 'l' && expr[cursor + 2] == 's' && expr[cursor + 3] == 'e'
                     && (isWhitespace(expr[cursor + 4]) || expr[cursor] == '{');
         }
         return false;
     }
 
     protected void captureToEOS() {
         while (cursor < length && expr[cursor] != ';') {
             cursor++;
         }
     }
 
     protected void captureToEOLorOF() {
         while (cursor < length && (expr[cursor] != '\n' && expr[cursor] != '\r' && expr[cursor] != ';')) {
             cursor++;
         }
     }
 
     protected void captureToEOT() {
         while (++cursor < length) {
             switch (expr[cursor]) {
                 case'(':
                 case'[':
                 case'{':
                     if ((cursor = ParseTools.balancedCapture(expr, cursor, expr[cursor])) == -1) {
                         throw new CompileException("unbalanced braces", expr, cursor);
                     }
                     break;
 
                 case';':
                     return;
 
                 default:
                     if (isWhitespace(expr[cursor])) {
                         skipWhitespace();
 
                         if (expr[cursor] == '.') break;
                         else {
                             trimWhitespace();
                             return;
                         }
                     }
             }
 
         }
     }
 
     protected int trimLeft(int pos) {
         while (pos > 0 && isWhitespace(expr[pos - 1])) pos--;
         return pos;
     }
 
     protected int trimRight(int pos) {
         while (pos < length && isWhitespace(expr[pos])) pos++;
         return pos;
     }
 
     protected void skipWhitespace() {
         while (isWhitespace(expr[cursor])) cursor++;
     }
 
     protected void skipToNextTokenJunction() {
         while (cursor < length) {
             switch (expr[cursor]) {
                 case'(':
                     return;
                 default:
                     if (isWhitespace(expr[cursor])) return;
                     cursor++;
             }
         }
     }
 
     protected void trimWhitespace() {
         while (cursor > 0 && isWhitespace(expr[cursor - 1])) cursor--;
     }
 
     protected ASTNode captureTokenToEOS() {
         int start = cursor;
         captureToEOS();
         return new ASTNode(expr, start, cursor, 0);
     }
 
     protected void setExpression(String expression) {
         if (expression != null && !"".equals(expression)) {
             if (!EX_PRECACHE.containsKey(expression)) {
                 length = (this.expr = expression.toCharArray()).length;
 
                 // trim any whitespace.
                 while (isWhitespace(this.expr[length - 1])) length--;
 
                 char[] e = new char[length];
                 arraycopy(this.expr, 0, e, 0, length);
 
                 EX_PRECACHE.put(expression, e);
             }
             else {
                 length = (expr = EX_PRECACHE.get(expression)).length;
             }
         }
     }
 
     protected void setExpression(char[] expression) {
         length = (this.expr = expression).length;
         while (length != 0 && isWhitespace(this.expr[length - 1])) length--;
     }
 
     private boolean isFlag(int bit) {
         return (fields & bit) != 0;
     }
 
     public static boolean isReservedWord(String name) {
         return LITERALS.containsKey(name) || OPERATORS.containsKey(name);
     }
 
     protected char lookBehind(int range) {
         if ((cursor - range) <= 0) return 0;
         else {
             return expr[cursor - range];
         }
     }
 
     protected char lookAhead(int range) {
         if ((cursor + range) >= length) return 0;
         else {
             return expr[cursor + range];
         }
     }
 
     protected boolean isStatementManuallyTerminated() {
         int c = cursor;
         while (c < length && isWhitespace(expr[c])) c++;
         return (c < length && expr[c] == ';');
     }
 
     protected boolean isRemain(int range) {
         return (cursor + range) < length;
     }
 
     protected boolean isAt(char c, int range) {
         return lookAhead(range) == c;
     }
 
     public void addImport(String name, Class cls) {
         if (imports == null) imports = new HashMap<String, Class>();
         imports.put(name, cls);
     }
 
     protected Class getImport(String name) {
         return imports != null ? imports.get(name) : null;
     }
 
     protected boolean hasImport(String name) {
         return imports != null && imports.containsKey(name);
     }
 
 
     public Map<String, Interceptor> getInterceptors() {
         return interceptors;
     }
 
     public void setInterceptors(Map<String, Interceptor> interceptors) {
         this.interceptors = interceptors;
     }
 
 
     public String getSourceFile() {
         return sourceFile;
     }
 
     public void setSourceFile(String sourceFile) {
         this.sourceFile = sourceFile;
     }
 
     public boolean isDebugSymbols() {
         return debugSymbols;
     }
 
     public void setDebugSymbols(boolean debugSymbols) {
         this.debugSymbols = debugSymbols;
     }
 
     protected static String getCurrentSourceFileName() {
         if (parserContext != null && parserContext.get() != null) {
             return parserContext.get().getSourceFile();
         }
         return null;
     }
 
 
     public static final int LEVEL_5_CONTROL_FLOW = 5;
     public static final int LEVEL_4_ASSIGNMENT = 4;
     public static final int LEVEL_3_ITERATION = 3;
     public static final int LEVEL_2_MULTI_STATEMENT = 2;
     public static final int LEVEL_1_BASIC_LANG = 1;
     public static final int LEVEL_0_PROPERTY_ONLY = 0;
 
     public static void setLanguageLevel(int level) {
         OPERATORS.clear();
         _loadLanguageFeaturesByLevel(level);
     }
 
     private static void _loadLanguageFeaturesByLevel(int languageLevel) {
         switch (languageLevel) {
             case 5:  // control flow operations
                 OPERATORS.put("if", IF);
                 OPERATORS.put("else", ELSE);
                 OPERATORS.put("?", Operator.TERNARY);
                 OPERATORS.put("switch", SWITCH);
 
             case 4: // assignment
                 OPERATORS.put("=", Operator.ASSIGN);
                 OPERATORS.put("var", TYPED_VAR);
                 OPERATORS.put("+=", ASSIGN_ADD);
                 OPERATORS.put("-=", ASSIGN_SUB);
 
 
             case 3: // iteration
                 OPERATORS.put("foreach", FOREACH);
                 OPERATORS.put("while", WHILE);
                 OPERATORS.put("for", FOR);
                 OPERATORS.put("do", DO);
 
 
             case 2: // multi-statement
                 OPERATORS.put("return", RETURN);
                 OPERATORS.put(";", END_OF_STMT);
 
 
             case 1: // boolean, math ops, projection, assertion, objection creation, block setters, imports
                 OPERATORS.put("+", ADD);
                 OPERATORS.put("-", SUB);
                 OPERATORS.put("*", MULT);
                 OPERATORS.put("**", POWER);
                 OPERATORS.put("/", DIV);
                 OPERATORS.put("%", MOD);
                 OPERATORS.put("==", EQUAL);
                 OPERATORS.put("!=", NEQUAL);
                 OPERATORS.put(">", GTHAN);
                 OPERATORS.put(">=", GETHAN);
                 OPERATORS.put("<", LTHAN);
                 OPERATORS.put("<=", LETHAN);
                 OPERATORS.put("&&", AND);
                 OPERATORS.put("and", AND);
                 OPERATORS.put("||", OR);
                 OPERATORS.put("or", CHOR);
                 OPERATORS.put("~=", REGEX);
                 OPERATORS.put("instanceof", INSTANCEOF);
                 OPERATORS.put("is", INSTANCEOF);
                 OPERATORS.put("contains", CONTAINS);
                 OPERATORS.put("soundslike", SOUNDEX);
                 OPERATORS.put("strsim", SIMILARITY);
                 OPERATORS.put("convertable_to", CONVERTABLE_TO);
 
                 OPERATORS.put("#", STR_APPEND);
 
                 OPERATORS.put("&", BW_AND);
                 OPERATORS.put("|", BW_OR);
                 OPERATORS.put("^", BW_XOR);
                 OPERATORS.put("<<", BW_SHIFT_LEFT);
                 OPERATORS.put("<<<", BW_USHIFT_LEFT);
                 OPERATORS.put(">>", BW_SHIFT_RIGHT);
                 OPERATORS.put(">>>", BW_USHIFT_RIGHT);
 
                 OPERATORS.put("new", Operator.NEW);
                 OPERATORS.put("in", PROJECTION);
 
 
                 OPERATORS.put("with", WITH);
 
                 OPERATORS.put("assert", ASSERT);
                 OPERATORS.put("import", IMPORT);
                 OPERATORS.put("import_static", IMPORT_STATIC);
 
                 OPERATORS.put("++", INC);
                 OPERATORS.put("--", DEC);
 
             case 0: // Property access and inline collections
                 OPERATORS.put(":", TERNARY_ELSE);
         }
 
     }
 
 
 }
