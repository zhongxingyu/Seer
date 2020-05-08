 /* -*- mode: Java; c-basic-offset: 2; -*- */
 
 /**
  * Common Code for Translation and Code Generation
  *
  * @author steele@osteele.com
  * @author ptw@openlaszlo.org
  * @author dda@ddanderson.com
  * @description: Common baseclass for code generators
  *
  * This class is extended by CodeGenerator, JavascriptGenerator.
  */
 
 //
 // Code Generation
 //
 
 // The CodeGenerator recurses over the parse tree, sending instructions
 // to an InstructionCollector.  The entry point is translate(), and it
 // does its work by calling two mutually recursive functions,
 // visitStatement and visitExpression, which dispatch to visitor
 // functions for specific statement and expression types based on the
 // name of the class of the parser node.  (A declaration or definition
 // is considered to be a statement.)
 
 package org.openlaszlo.sc;
 import java.io.*;
 import java.util.*;
 import java.nio.ByteBuffer;
 
 import org.openlaszlo.sc.parser.*;
 import org.openlaszlo.sc.Instructions;
 import org.openlaszlo.sc.Instructions.Instruction;
 
 import org.openlaszlo.cache.PersistentMap;
 
 
 // The code generator dispatches a node whose class is named ASTName to
 // a method visitName, passing the node, a context, and the node's
 // children as arguments.  The context for a statement visitor is a
 // TranslationContext, defined above.  The context for an expression
 // visitor is a boolean value, that is true iff the value of the
 // expression is used.  The return value of a statement visitor is
 // ignored.  The return value of an expression visitor is true iff it
 // generated code that did NOT leave a value on the stack.  (This is so
 // that an expression visitor that ignores its context need do nothing
 // special to indicate that it ignored it: the default return value of
 // null signals this.)
 //
 // Methods of the form visitName are AST node visitors, and follow the
 // protocol described above.  Methods of the form translateName are
 // helper functions for the visitors, and have arbitrary parameter
 // lists and return values.
 
 // TODO: [2006-01-17 ptw] Remove some day
 // Replace instruction subsequences by a BLOB instruction that
 // represents the same bytes.  By default, the BLOB instructions are
 // separated by PUSH's (which depend on the constant pool), and
 // branches and targets (since they can't be resolved until the size of
 // the PUSH instructions is known).  When noConstantPool=true, PUSH's
 // are compiled against a null constant pool, and branches and targets
 // are compiled, so the instructions combine to a single BLOB.
 // public void combineInstructions(instrsIn, noConstantPool=false) {
 //     instrsOut = [];
 //     buffer = ByteBuffer.allocate(64000);
 //     public void flush(instrsOut=instrsOut,buffer=buffer) {
 //         if (buffer.position()) {
 //             import jarray;
 //             bytes = jarray.zeros(buffer.position(), "b");
 //             buffer.flip();
 //             buffer.get(bytes);
 //             buffer.clear();
 //             instrsOut.append(BLOB("bytes", bytes));
 //     for (instr in instrsIn) {
 //         if (noConstantPool || instr.isPush || instr.isLabel || instr.hasTarget) {
 //             flush();
 //             instrsOut.append(instr);
 //         } else {
 //             instr.writeBytes(buffer, null);
 //     flush();
 //     return instrsOut;
 // }
 
 public abstract class CommonGenerator implements ASTVisitor {
 
   Compiler.OptionMap options = new Compiler.OptionMap();
   String runtime;
   TranslationContext context = null;
   boolean debugVisit = false;
   InstructionCollector collector = null;
 
   public Compiler.OptionMap getOptions() {
     return options;
   }
 
   public TranslationContext getContext() {
     return context;
   }
 
   protected abstract void setRuntime(String runtime);
 
   public void setOptions(Compiler.OptionMap options) {
     this.options = options;
     this.runtime = ((String)options.get(Compiler.RUNTIME)).intern();
     setRuntime(this.runtime);
    assert org.openlaszlo.compiler.Compiler.SWF_RUNTIMES.contains(runtime) : "unknown runtime " + runtime;
    Instructions.setRuntime(runtime);
   }
 
   public InstructionCollector getCollector() {
     return collector;
   }
 
   // Options that don't affect code generation.  This is used to decide
   // what it's okay to cache across LFC build versions.  It's okay if
   // it's too small.
   static Set NonCodeGenerationOptions = new HashSet();
   static {
     NonCodeGenerationOptions.add(Compiler.CACHE_COMPILES);
     NonCodeGenerationOptions.add(Compiler.INSTR_STATS);
     NonCodeGenerationOptions.add(Compiler.PRINT_COMPILER_OPTIONS);
     NonCodeGenerationOptions.add(Compiler.PRINT_CONSTRAINTS);
     NonCodeGenerationOptions.add(Compiler.PROFILE_COMPILER);
     NonCodeGenerationOptions.add(Compiler.PROGRESS);
     NonCodeGenerationOptions.add(Compiler.RESOLVER);
     // These affect the default settings for the options above, but
     // do not themselves make a difference.
     NonCodeGenerationOptions.add(Compiler.DEBUG);
   }
 
   static class LessHalfAssedHashMap extends HashMap {
     LessHalfAssedHashMap() {
       super();
     }
 
     Object get(int key) {
       return get(new Integer(key));
     }
 
     Object put(int key, Object value) {
       return put (new Integer(key), value);
     }
 
     Object put(int key, int value) {
       return put (new Integer(key), new Integer(value));
     }
   }
 
   static SimpleNode parseFragment(String code) {
     if (code.equals("\"\"") || code == null) {
         code = "";
     }
     code =
       "{" +
       "\n#pragma 'warnUndefinedReferences=false'\n" +
       "\n#file JavascriptGenerator.parseFragment\n#line 0\n" +
       code +
       "}";
     // Extract the statement list from the program
     try {
       return (new Compiler.Parser()).parse(code).get(0);
     } catch (ParseException e) {
       System.err.println("while compiling " + code);
       throw e;
     }
   }
 
   // TODO: [2007-08-20 ptw] Replace with Java 1.5 UUID
   private Boolean usePredictable = null;
   private Random rand = new Random();
   private int uuidCounter = 1;
   protected Integer UUID() {
     if (usePredictable == null) {
       usePredictable = new Boolean(options.getBoolean(Compiler.GENERATE_PREDICTABLE_TEMPS));
     }
     if (usePredictable.equals(Boolean.TRUE)) {
       return new Integer(uuidCounter++);
     }
     else {
       return new Integer(rand.nextInt(Integer.MAX_VALUE));
     }
   }
 
   Boolean evaluateCompileTimeConditional(SimpleNode node) {
     Object value = null;
     if (node instanceof ASTIdentifier) {
       String name = ((ASTIdentifier)node).getName();
       Map constants = (Map)options.get(Compiler.COMPILE_TIME_CONSTANTS);
       if (constants != null) {
         if (constants.containsKey(name)) {
           value = constants.get(name);
 //           if (value != null) {
 //             + ": " + value + "(" + value.getClass() + ")");
 //           }
         }
       }
     }
 //     if (value != null) {
 //       System.err.println(" => " + value + "(" + value.getClass() + ")");
 //     }
     return (Boolean)value;
   }
 
   static class ParseResult {
     SimpleNode parse;
     boolean hasIncludes;
 
     ParseResult(SimpleNode parse, boolean hasIncludes) {
       this.parse = parse;
       this.hasIncludes = hasIncludes;
     }
 
     public boolean equals(Object o) {
       if (o != null && o instanceof ParseResult) {
         ParseResult pr = (ParseResult)o;
         return parse.equals(pr.parse) && hasIncludes == pr.hasIncludes;
       }
       return false;
     }
   }
 
   static java.util.regex.Pattern includePattern =
   java.util.regex.Pattern.compile(".*#\\s*include\\s*\".*", java.util.regex.Pattern.DOTALL);
 
   ParseResult parseFile(File file, String userfname, String source) {
     if (Compiler.CachedParses == null) {
       Compiler.CachedParses = new ScriptCompilerCache();
     }
     String sourceKey = file.getAbsolutePath();
     String sourceChecksum = "" + file.lastModified(); // source;
     ParseResult entry = (ParseResult)Compiler.CachedParses.get(sourceKey, sourceChecksum);
     if ((entry == null) || options.getBoolean(Compiler.VALIDATE_CACHES)) {
       boolean hasIncludes = includePattern.matcher(source).matches();
       if (options.getBoolean(Compiler.PROGRESS)) {
         // Even though code generation is re-run
         // for every file, just print this for
         // files that are re-parsed, to indicate
         // what's being changed.
         System.err.println("Compiling " + userfname + "...");
       }
       SimpleNode program = (new Compiler.Parser()).parse(source);
       // Always cache the parse tree, since this
       // helps even when the compilation is only one
       // once.  This is because each pass processes
       // the #include again.
       ParseResult realentry = new ParseResult(program, hasIncludes);
       Compiler.CachedParses.put(sourceKey, sourceChecksum, realentry);
       if ((entry != null) && options.getBoolean(Compiler.VALIDATE_CACHES)) {
         if (! realentry.equals(entry)) {
           System.err.println("Bad parse cache for " + sourceKey + ": " + entry + " != " + realentry);
         }
       }
       entry = realentry;
     }
     return entry;
   }
 
   private String mapToString(Map map) {
     StringBuffer result = new StringBuffer();
     result.append("{");
     TreeMap sorted = new TreeMap(map);
     for (Iterator i = sorted.keySet().iterator(); i.hasNext(); ) {
       Object key = i.next();
       result.append(key);
       result.append(": ");
       result.append(sorted.get(key));
       if (i.hasNext()) {
         result.append(", ");
       }
     }
     result.append("}");
     return result.toString();
   }
 
   String getCodeGenerationOptionsKey(List ignore) {
     Map options = new HashMap(this.options);
     options.keySet().removeAll(NonCodeGenerationOptions);
     if (ignore != null) {
       options.keySet().removeAll(ignore);
     }
     return mapToString(options);
   }
 
   public SimpleNode visitPragmaDirective(SimpleNode node, SimpleNode[] children) {
     String key = (String)((ASTLiteral)children[0]).getValue();
     String value = "true";
     int equals = key.indexOf('=');
     if (equals > 0) {
       value = key.substring(equals + 1);
       key = key.substring(0, equals);
     }
     if ("false".equalsIgnoreCase(value) ||
         "true".equalsIgnoreCase(value)) {
       options.putBoolean(key, value);
     } else {
       options.put(key, value);
     }
     return new ASTEmptyExpression(0);
   }
 
   // Flatten nested StatementList structures
   private List flatten(SimpleNode[] src) {
     List dst = new ArrayList();
     for (int i = 0; i < src.length; i++) {
       SimpleNode node = src[i];
       if (node instanceof ASTStatementList) {
         dst.addAll(flatten(node.getChildren()));
       } else {
         dst.add(node);
       }
     }
     return dst;
   }
 
   public SimpleNode visitClassDefinition(SimpleNode node, SimpleNode[] children) {
 //     System.err.println("enter visitClassDefinition: " +  (new Compiler.ParseTreePrinter()).visit(node));
     ASTIdentifier classortrait = (ASTIdentifier)children[0];
     ASTIdentifier classname = (ASTIdentifier)children[1];
     String classnameString = classname.getName();
     SimpleNode superclass = children[2];
     SimpleNode traits = children[3];
     SimpleNode traitsandsuper;
     if (traits instanceof ASTEmptyExpression) {
       if (superclass instanceof ASTEmptyExpression) {
         traitsandsuper = new ASTLiteral(null);
       } else {
         traitsandsuper = superclass;
       }
     } else {
       traitsandsuper = new ASTArrayLiteral(0);
       traitsandsuper.setChildren(traits.getChildren());
       if (! (superclass instanceof ASTEmptyExpression)) {
         traitsandsuper.set(traitsandsuper.size(), superclass);
       }
     }
 
     SimpleNode[] dirs = (SimpleNode [])(Arrays.asList(children).subList(4, children.length).toArray(new SimpleNode[0]));
     List props = new ArrayList();
     List classProps = new ArrayList();
     List stmts = new ArrayList();
     translateClassDirectivesBlock(dirs, classnameString, props, classProps, stmts);
 
     SimpleNode instanceProperties;
     if (props.isEmpty()) {
       instanceProperties = new ASTLiteral(null);
     } else {
       instanceProperties = new ASTObjectLiteral(0);
       instanceProperties.setChildren((SimpleNode[])(props.toArray(new SimpleNode[0])));
     }
     SimpleNode classProperties;
     if (classProps.isEmpty()) {
       classProperties = new ASTLiteral(null);
     } else {
       classProperties = new ASTObjectLiteral(0);
       classProperties.setChildren((SimpleNode[])(classProps.toArray(new SimpleNode[0])));
     }
 
     Map map = new HashMap();
     String xtor = "class".equals(classortrait.getName())?"Class":"Trait";
     map.put("_1", classname);
     map.put("_2", traitsandsuper);
     map.put("_3", instanceProperties);
     map.put("_4", classProperties);
     SimpleNode newNode = (new Compiler.Parser()).substitute(xtor + ".make(" +
                                                             ScriptCompiler.quote(classnameString) +
                                                             ", _2, _3, _4);",
                                                             map);
     SimpleNode varNode = new ASTVariableDeclaration(0);
     varNode.set(0, classname);
     varNode.set(1, newNode);
     SimpleNode replNode = varNode;
 
     if (! stmts.isEmpty()) {
       SimpleNode statements = new ASTStatementList(0);
       statements.setChildren((SimpleNode[])(stmts.toArray(new SimpleNode[0])));
       map.put("_5", statements);
       SimpleNode stmtNode = (new Compiler.Parser()).substitute("(function () { with(_1) with(_1.prototype) { _5 }})()",
                                                                map);
       replNode = new ASTStatementList(0);
       replNode.set(0, varNode);
       replNode.set(1, stmtNode);
     }
 //     System.err.println("exit visitClassDefinition: " +  (new Compiler.ParseTreePrinter()).visit(replNode));
     return visitStatement(replNode);
   }
 
   public void translateClassDirectivesBlock(SimpleNode[] dirs, String classnameString, List props, List classProps, List stmts) {
     dirs = (SimpleNode[])(flatten(dirs).toArray(new SimpleNode[0]));
 
     // Scope #pragma directives to block
     Compiler.OptionMap savedOptions = options;
     try {
       options = options.copy();
       for (int i = 0; i < dirs.length; i++) {
         SimpleNode n = dirs[i];
         List p = props;
         if (n instanceof ASTClassProperty) {
           n = n.get(0);
           p = classProps;
         }
         if (n instanceof ASTFunctionDeclaration) {
           SimpleNode[] c = n.getChildren();
           assert c.length == 3;
           p.add(c[0]);
           SimpleNode funexpr = new ASTFunctionExpression(0);
           funexpr.setBeginLocation(n.filename, n.beginLine, n.beginColumn);
           funexpr.setChildren(c);
           p.add(funexpr);
         } else if (n instanceof ASTVariableStatement) {
           SimpleNode [] c = n.getChildren();
           for (int j = 0, len = c.length; j < len; j++) {
             SimpleNode v = c[j];
             assert v instanceof ASTVariableDeclaration : v.getClass();
             p.add(v.get(0));
             if (v.getChildren().length > 1) {
               p.add(v.get(1));
             } else {
               p.add(new ASTLiteral(null));
             }
           }
         } else if (n instanceof ASTClassDirectiveBlock) {
           translateClassDirectivesBlock(n.getChildren(), classnameString, props, classProps, stmts);
         } else if (n instanceof ASTClassIfDirective) {
           Boolean value = evaluateCompileTimeConditional(n.get(0));
           if (value == null) {
             stmts.add(n);
           } else if (value.booleanValue()) {
             SimpleNode clause = n.get(1);
             translateClassDirectivesBlock(clause.getChildren(), classnameString, props, classProps, stmts);
           } else if (n.size() > 2) {
             SimpleNode clause = n.get(2);
             translateClassDirectivesBlock(clause.getChildren(), classnameString, props, classProps, stmts);
           }
         } else if (n instanceof ASTPragmaDirective) {
           visitPragmaDirective(n, n.getChildren());
         } else {
           stmts.add(n);
         }
       }
     }
     finally {
       options = savedOptions;
     }
   }
 
   //
   // Statements
   //
 
   public SimpleNode visitStatement(SimpleNode node) {
     return visitStatement(node, node.getChildren());
   }
 
   public SimpleNode visitStatement(SimpleNode node, SimpleNode[] children) {
     /* This function, unlike the other statement visitors, can be
        applied to any statement node, so it dispatches based on the
        node's class. */
     assert context instanceof TranslationContext;
     showStats(node);
     SimpleNode newNode = node;
 
     if (this.debugVisit) {
       System.err.println("visitStatement: " + node.getClass());
     }
 
     // Are we doing OO programming yet?
     if (node instanceof ASTPragmaDirective) {
       newNode = visitPragmaDirective(node, children);
     }
     else if (node instanceof ASTClassDefinition) {
       newNode = visitClassDefinition(node, children);
     }
     else if (node instanceof ASTStatementList) {
       newNode = visitStatementList(node, children);
     }
     else if (node instanceof ASTDirectiveBlock) {
       newNode = visitDirectiveBlock(node, children);
     }
     else if (node instanceof ASTFunctionDeclaration) {
       newNode = visitFunctionDeclaration(node, children);
     }
     else if (node instanceof ASTStatement) {
       // an empty statement, introduced by an extra ";", has no children
       if (children.length > 0) {
         children[0] = visitStatement(children[0], children[0].getChildren());
       } else {
         newNode = new ASTEmptyExpression(0);
       }
     }
     else if (node instanceof ASTLabeledStatement) {
       newNode = visitLabeledStatement(node, children);
     }
     else if (node instanceof ASTVariableDeclaration) {
       newNode = visitVariableDeclaration(node, children);
     }
     else if (node instanceof ASTVariableStatement) {
       newNode = visitVariableStatement(node, children);
     }
     else if (node instanceof ASTIfStatement) {
       newNode = visitIfStatement(node, children);
     }
     else if (node instanceof ASTIfDirective) {
       newNode = visitIfDirective(node, children);
     }
     else if (node instanceof ASTWhileStatement) {
       newNode = visitWhileStatement(node, children);
     }
     else if (node instanceof ASTDoWhileStatement) {
       newNode = visitDoWhileStatement(node, children);
     }
     else if (node instanceof ASTForStatement) {
       newNode = visitForStatement(node, children);
     }
     else if (node instanceof ASTForVarStatement) {
       newNode = visitForVarStatement(node, children);
     }
     else if (node instanceof ASTForInStatement) {
       newNode = visitForInStatement(node, children);
     }
     else if (node instanceof ASTForVarInStatement) {
       newNode = visitForVarInStatement(node, children);
     }
     else if (node instanceof ASTContinueStatement) {
       newNode = visitContinueStatement(node, children);
     }
     else if (node instanceof ASTBreakStatement) {
       newNode = visitBreakStatement(node, children);
     }
     else if (node instanceof ASTReturnStatement) {
       newNode = visitReturnStatement(node, children);
     }
     else if (node instanceof ASTWithStatement) {
       newNode = visitWithStatement(node, children);
     }
     else if (node instanceof ASTTryStatement) {
       newNode = visitTryStatement(node, children);
     }
     else if (node instanceof ASTThrowStatement) {
       newNode = visitThrowStatement(node, children);
     }
     else if (node instanceof ASTSwitchStatement) {
       newNode = visitSwitchStatement(node, children);
     }
     else if (node instanceof Compiler.PassThroughNode) {
       newNode = node;
     } else {
       // Not a statement, must be an expression
       newNode = visitExpression(node, false);
     }
     // Check for elided statments
     if (newNode == null) {
       newNode = new ASTEmptyExpression(0);
     }
     if (this.debugVisit) {
       if (! newNode.equals(node)) {
         System.err.println("statement: " + node + " -> " + newNode);
       }
     }
     return newNode;
   }
 
   public SimpleNode visitStatementList(SimpleNode node, SimpleNode[] stmts) {
     int i = 0;
     // ensure dynamic extent of #pragma in a block
     Compiler.OptionMap prevOptions = options;
     Compiler.OptionMap newOptions = options.copy();
     // TODO: [2003-04-15 ptw] bind context slot macro
     try {
       options = newOptions;
       while (i < stmts.length) {
         SimpleNode stmt = stmts[i];
         stmts[i] = visitStatement(stmt);
         i += 1;
       }
     }
     finally {
       options = prevOptions;
     }
     return node;
   }
 
   // for function prefix/suffix parsing
   public SimpleNode visitDirectiveBlock(SimpleNode node, SimpleNode[] children) {
     return visitStatementList(node, children);
   }
 
   public SimpleNode visitLabeledStatement(SimpleNode node, SimpleNode[] children) {
     ASTIdentifier name = (ASTIdentifier)children[0];
     SimpleNode stmt = children[1];
     // TODO: [2003-04-15 ptw] bind context slot macro
     try {
       context = new TranslationContext(ASTLabeledStatement.class, context, name.getName());
       // TODO: [2002 ows] throw semantic error for duplicate label
       children[1] = visitStatement(stmt);
       return node;
     }
     finally {
       context = context.parent;
     }
   }
 
   // for function prefix/suffix parsing
   public SimpleNode visitIfDirective(SimpleNode node, SimpleNode[] children) {
     return visitIfStatement(node, children);
   }
 
   public SimpleNode visitForVarInStatement(SimpleNode node, SimpleNode[] children) {
     SimpleNode var = children[0];
     // SimpleNode _ = children[1];
     SimpleNode obj = children[2];
     SimpleNode body = children[3];
     if (options.getBoolean(Compiler.ACTIVATION_OBJECT)) {
       return translateForInStatement(node, var, Instructions.SetVariable, obj, body);
     }
     return translateForInStatement(node, var, Instructions.VarEquals, obj, body);
   }
 
   public SimpleNode visitContinueStatement(SimpleNode node, SimpleNode[] children) {
     SimpleNode label = children.length > 0 ? children[0] : null;
     return translateAbruptCompletion(node, "continue", (ASTIdentifier)label);
   }
 
   public SimpleNode visitBreakStatement(SimpleNode node, SimpleNode[] children) {
     SimpleNode label = children.length > 0 ? children[0] : null;
     return translateAbruptCompletion(node, "break", (ASTIdentifier)label);
   }
 
   public SimpleNode visitAndExpressionSequence(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     SimpleNode a = children[0];
     SimpleNode b = children[1];
     return translateAndOrExpression(node, true, a, b);
   }
 
   public SimpleNode visitOrExpressionSequence(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     SimpleNode a = children[0];
     SimpleNode b = children[1];
     return translateAndOrExpression(node, false, a, b);
   }
 
   static class DoubleCollator implements Comparator {
     public boolean equals(Object o1, Object o2) {
       return ((Double)o1).equals((Double)o2);
     }
 
     public int compare(Object o1, Object o2) {
       return ((Double)o1).compareTo((Double)o2);
     }
   }
 
   public SimpleNode visitChildren(SimpleNode node) {
     SimpleNode[] children = node.getChildren();
     for (int i = 0, len = children.length; i < len; i++) {
       SimpleNode child = children[i];
       children[i] = visitStatement(child);
     }
     return node;
   }
 
   public SimpleNode translateSuperCallExpression(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
 
     assert children.length == 3;
     SimpleNode fname = children[0];
     SimpleNode callapply = children[1];
     SimpleNode args = children[2];
     String name;
     String ca = null;
     String pattern = "(arguments.callee.superclass?arguments.callee.superclass.prototype[_1]:this.nextMethod(arguments.callee, _1)).call(this, _2)";
     if (fname instanceof ASTEmptyExpression) {
       name = "constructor";
     } else {
       name = ((ASTIdentifier)fname).getName();
     }
     if (callapply instanceof ASTIdentifier) {
       ca = ((ASTIdentifier)callapply).getName();
     }
     // FIXME: [2005-03-09 ptw] (LPP-98 "Compiler source-source
     // transformations should be in separate phase") This should be
     // in a phase before the compiler, so that register analysis
     // sees it.  [Or this should be eliminated altogether and we
     // should use swf7's real super call, but that will mean we
     // have to solve the __proto__ vs. super in constructor
     // problem.]
     Map map = new HashMap();
     map.put("_1", new ASTLiteral(name));
     map.put("_2", new Compiler.Splice(args.getChildren()));
     if (ca == null) {
       ;
     } else if ("call".equals(ca)) {
       pattern = "(arguments.callee.superclass?arguments.callee.superclass.prototype[_1]:this.nextMethod(arguments.callee, _1)).call(_2)";
     } else if ("apply".equals(ca)) {
       pattern = "(arguments.callee.superclass?arguments.callee.superclass.prototype[_1]:this.nextMethod(arguments.callee, _1)).apply(_2)";
     } else {
       assert false: "Unhandled super call " + ca;
     }
     SimpleNode n = (new Compiler.Parser()).substitute(pattern, map);
     return n;
   }
 
   public SimpleNode visitVariableStatement(SimpleNode node, SimpleNode[] children) {
     return visitChildren(node);
   }
 
   public SimpleNode dispatchExpression(SimpleNode node, boolean isReferenced) {
     // Are we doing OO programming yet?
     SimpleNode[] children = node.getChildren();
     SimpleNode newNode = null;
 
     if (node instanceof ASTIdentifier) {
       newNode = visitIdentifier(node, isReferenced, children);
     }
     else if (node instanceof ASTLiteral) {
       newNode = visitLiteral(node, isReferenced, children);
     }
     else if (node instanceof ASTExpressionList) {
       newNode = visitExpressionList(node, isReferenced, children);
     }
     else if (node instanceof ASTEmptyExpression) {
       newNode = visitEmptyExpression(node, isReferenced, children);
     }
     else if (node instanceof ASTThisReference) {
       newNode = visitThisReference(node, isReferenced, children);
     }
     else if (node instanceof ASTArrayLiteral) {
       newNode = visitArrayLiteral(node, isReferenced, children);
     }
     else if (node instanceof ASTObjectLiteral) {
       newNode = visitObjectLiteral(node, isReferenced, children);
     }
     else if (node instanceof ASTFunctionExpression) {
       newNode = visitFunctionExpression(node, isReferenced, children);
     }
     else if (node instanceof ASTFunctionCallParameters) {
       newNode = visitFunctionCallParameters(node, isReferenced, children);
     }
     else if (node instanceof ASTPropertyIdentifierReference) {
       newNode = visitPropertyIdentifierReference(node, isReferenced, children);
     }
     else if (node instanceof ASTPropertyValueReference) {
       newNode = visitPropertyValueReference(node, isReferenced, children);
     }
     else if (node instanceof ASTCallExpression) {
       newNode = visitCallExpression(node, isReferenced, children);
     }
     else if (node instanceof ASTSuperCallExpression) {
       newNode = visitSuperCallExpression(node, isReferenced, children);
     }
     else if (node instanceof ASTNewExpression) {
       newNode = visitNewExpression(node, isReferenced, children);
     }
     else if (node instanceof ASTPostfixExpression) {
       newNode = visitPostfixExpression(node, isReferenced, children);
     }
     else if (node instanceof ASTUnaryExpression) {
       newNode = visitUnaryExpression(node, isReferenced, children);
     }
     else if (node instanceof ASTBinaryExpressionSequence) {
       newNode = visitBinaryExpressionSequence(node, isReferenced, children);
     }
     else if (node instanceof ASTAndExpressionSequence) {
       newNode = visitAndExpressionSequence(node, isReferenced, children);
     }
     else if (node instanceof ASTOrExpressionSequence) {
       newNode = visitOrExpressionSequence(node, isReferenced, children);
     }
     else if (node instanceof ASTConditionalExpression) {
       newNode = visitConditionalExpression(node, isReferenced, children);
     }
     else if (node instanceof ASTAssignmentExpression) {
       newNode = visitAssignmentExpression(node, isReferenced, children);
     }
     else if (node instanceof Compiler.PassThroughNode) {
       newNode = node;
     }
     else {
       throw new CompilerImplementationError("unknown expression " + node, node);
     }
     return newNode;
   }
 
   abstract SimpleNode translateForInStatement(SimpleNode node, SimpleNode var,
                                               Instructions.Instruction varset,
                                               SimpleNode obj,
                                               SimpleNode body);
   abstract SimpleNode translateAbruptCompletion(SimpleNode node, String type,
                                                 ASTIdentifier label);
   abstract SimpleNode translateAndOrExpression(SimpleNode node, boolean isand,
                                                SimpleNode a, SimpleNode b);
 
   /** Collect runtime statistics at this point in the program if asked for.
    */
   abstract void showStats(SimpleNode node);
 
 
   File includeNameToFile(String userfname) {
     try {
       String fname = userfname;
 
       if (options.containsKey(Compiler.RESOLVER)) {
         fname = ((lzsc.Resolver)options.get(Compiler.RESOLVER)).resolve(userfname);
       }
       return new File(new File(fname).getCanonicalPath());
     }
     catch (IOException e) {
       throw new CompilerError("error reading include: " + e);
     }
   }
 
   String includeFileToSourceString(File file, String userfname) {
     String source;
     try {
       FileInputStream stream = new FileInputStream(file);
       try {
         int n = stream.available();
         byte[] b = new byte[n];
         stream.read(b);
         source = "#file " + userfname + "\n#line 1\n" + new String(b, "UTF-8");
       }
       finally {
         stream.close();
       }
     }
     catch (FileNotFoundException e) {
       throw new CompilerError("error reading include: " + e);
     }
     catch (UnsupportedEncodingException e) {
       throw new CompilerError("error reading include: " + e);
     }
     catch (IOException e) {
       throw new CompilerError("error reading include: " + e);
     }
     return source;
   }
 }
 
 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
 * Copyright 2001-2007 Laszlo Systems, Inc.  All Rights Reserved.              *
 * Use is subject to license terms.                                            *
 * J_LZ_COPYRIGHT_END *********************************************************/
