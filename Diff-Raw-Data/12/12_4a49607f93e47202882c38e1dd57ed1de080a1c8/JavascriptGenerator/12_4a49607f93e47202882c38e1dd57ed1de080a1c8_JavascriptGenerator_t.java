 /* -*- mode: Java; c-basic-offset: 2; -*- */
 
 /**
  * Javascript Generation
  *
  * @author steele@osteele.com
  * @author ptw@openlaszlo.org
  * @description: JavaScript -> JavaScript translator
  *
  * Transform the parse tree from ECMA ~4 to ECMA 3.  Includes
  * analyzing constraint functions and generating their dependencies.
  */
 
 // TODO: [2006-01-25 ptw] Share this with the SWF Code generator, from
 // which this was derived.
 
 package org.openlaszlo.sc;
 import java.io.*;
 import java.util.*;
 
 import org.openlaszlo.sc.parser.*;
 
 public class JavascriptGenerator extends CommonGenerator implements Translator {
 
   protected void setRuntime(String runtiem) {
     assert org.openlaszlo.compiler.Compiler.SCRIPT_RUNTIMES.contains(runtime) : "unknown runtime " + runtime;
   }
 
   // Make Javascript globals 'known'
   Set globals = new HashSet(Arrays.asList(new String[] {
     "NaN", "Infinity", "undefined",
     "eval", "parseInt", "parseFloat", "isNaN", "isFinite",
     "decodeURI", "decodeURIComponent", "encodeURI", "encodeURIComponent",
     "Object", "Function", "Array", "String", "Boolean", "Number", "Date",
     "RegExp", "Error", "EvalError", "RangeError", "ReferenceError",
     "SyntaxError", "TypeError", "URIError",
     "Math"}));
 
   public SimpleNode translate(SimpleNode program) {
     // TODO: [2003-04-15 ptw] bind context slot macro
     try {
       context = new TranslationContext(ASTProgram.class, context);
       context.setProperty(TranslationContext.VARIABLES, globals);
       return translateInternal(program, "b", true);
     }
     finally {
       context = context.parent;
     }
   }
 
   public String newLabel(SimpleNode node) {
     throw new CompilerImplementationError("nyi: newLabel");
   }
 
   int tempNum = 0;
 
   String newTemp() {
     return newTemp("$lzsc$");
   }
 
   String newTemp(String prefix) {
     return prefix + tempNum++;
   }
 
   static LessHalfAssedHashMap XfixInstrs = new LessHalfAssedHashMap();
   static {
     XfixInstrs.put(ParserConstants.INCR, "+");
     XfixInstrs.put(ParserConstants.DECR, "-");
   };
 
   static LessHalfAssedHashMap AssignOpTable = new LessHalfAssedHashMap();
   static {
     AssignOpTable.put(ParserConstants.PLUSASSIGN, "+");
     AssignOpTable.put(ParserConstants.MINUSASSIGN, "-");
     AssignOpTable.put(ParserConstants.STARASSIGN, "*");
     AssignOpTable.put(ParserConstants.SLASHASSIGN, "/");
     AssignOpTable.put(ParserConstants.ANDASSIGN, "&");
     AssignOpTable.put(ParserConstants.ORASSIGN, "|");
     AssignOpTable.put(ParserConstants.XORASSIGN, "^");
     AssignOpTable.put(ParserConstants.REMASSIGN, "%");
     AssignOpTable.put(ParserConstants.LSHIFTASSIGN, "<<");
     AssignOpTable.put(ParserConstants.RSIGNEDSHIFTASSIGN, ">>");
     AssignOpTable.put(ParserConstants.RUNSIGNEDSHIFTASSIGN, ">>>");
   };
 
   // Code to meter a function call.  If name is set, uses that,
   // otherwise uses arguments.callee._dbg_name.  This code must be appended
   // to the function prefix or suffix, as appropriate.
   //
   // NOTE: [2006-06-24 ptw] This is an inline version of the LFC
   // `LzProfile.event` method and must be kept in sync with that.
   SimpleNode meterFunctionEvent(SimpleNode node, String event, String name) {
     String getname;
     if (name != null) {
       getname = "'" + name + "'";
     } else {
       getname = "arguments.callee._dbg_name";
     }
 
     // Note _root.$lzprofiler can be undedefined to disable profiling
     // at run time.
 
     // N.B., According to the Javascript spec, getTime() returns
     // the time in milliseconds, but we have observed that the
     // Flash player on some platforms tries to be accurate to
     // microseconds (by including fractional milliseconds).  On
     // other platforms, the time is not even accurate to
     // milliseconds, hence the kludge to manually increment the
     // clock to create a monotonic ordering.
 
     // The choice of 0.01 to increment by is based on the
     // observation that when floats are used as member names in an
     // object they are coerced to strings with only 15 significant
     // digits.  This should suffice for the next (10^13)-1
     // microseconds (about 300 years).
 
     return parseFragment(
       "var $lzsc$lzp = global['$lzprofiler'];" +
       "if ($lzsc$lzp) {" +
       "  var $lzsc$tick = $lzsc$lzp.tick;" +
       "  var $lzsc$now = (new Date).getTime();" +
       "  if ($lzsc$tick >= $lzsc$now) {" +
       "    $lzsc$now = $lzsc$tick + 0.0078125;" +
       "  }" +
       "  $lzsc$lzp.tick = $lzsc$now;" +
       "  $lzsc$lzp." + event + "[$lzsc$now] = " + getname + ";" +
       "}");
   }
 
   // Only used by warning generator, hence not metered.
   // FIXME: [2006-01-17 ptw] Regression compatibility Object -> String
   String report(String reportMethod, SimpleNode node, Object message) {
     return reportMethod + "(" + message + "," + node.filename + "," + node.beginLine + ")";
   }
 
   // Only used by warning generator, hence not metered.
   // FIXME: [2006-01-17 ptw] Regression compatibility Object -> String
   String report(String reportMethod, SimpleNode node, Object message, String extraArg) {
     return reportMethod + "(" + message + "," + node.filename + "," + node.beginLine + "," + extraArg + ")";
   }
 
   // Emits code to check that a function is defined.  If reference is
   // set, expects the function reference to be at the top of the stack
   // when called, otherwise expects the function object.
   // TODO: [2006-01-04 ptw] Rewrite as a source transform
   SimpleNode checkUndefinedFunction(SimpleNode node, JavascriptReference reference) {
     if (options.getBoolean(Compiler.DEBUG) && options.getBoolean(Compiler.WARN_UNDEFINED_REFERENCES) && node.filename != null) {
       return parseFragment(
         "typeof " + reference.get() + " != 'function' ? " + 
         report("$reportNotFunction", node, reference.get()) + " : " +
         reference.get());
     } 
     return null;
   }
 
   // Emits code to check that an object method is defined.  Does a trial
   // fetch of methodName to verify that it is a function.
   SimpleNode checkUndefinedMethod(SimpleNode node, JavascriptReference reference, String methodName) {
     if (options.getBoolean(Compiler.DEBUG) && options.getBoolean(Compiler.WARN_UNDEFINED_REFERENCES) && node.filename != null) {
       String o = newTemp();
       String om = newTemp();
       return parseFragment(
         "var " + o + " = " + reference.get() + ";" +
         "if (typeof(" + o + ") == undefined) {" +
         "  " + report("$reportUndefinedObjectProperty", node, methodName) +
         "}" +
         "var " + om + " = " + o + "[" + methodName + "];" + 
         "if (typeof(" + om + ") != 'function') {" +
         "  " + report("$reportUndefinedMethod", node, methodName, om) +
         "}");
     }
     return null;
   }
 
   SimpleNode translateInternal(SimpleNode program, String cpass, boolean top) {
     assert program instanceof ASTProgram;
     // TODO: [2003-04-15 ptw] bind context slot macro
     try {
       context = new TranslationContext(ASTProgram.class, context);
       return visitProgram(program, program.getChildren(), cpass, top);
     }
     finally {
       context = context.parent;
     }
   }
 
   void showStats(SimpleNode node) {
     // No implementation to collect stats for Javascript
   }
 
   public SimpleNode visitProgram(SimpleNode node, SimpleNode[] directives, String cpass) {
     return visitProgram(node, directives, cpass, false);
   }
 
   public SimpleNode visitProgram(SimpleNode node, SimpleNode[] directives, String cpass, boolean top) {
     // cpass is "b"oth, 1, or 2
     assert "b".equals(cpass) || "1".equals(cpass) || "2".equals(cpass) : "bad pass: " + cpass;
     if ("b".equals(cpass)) {
       node = visitProgram(node, directives, "1", top);
       // Everything is done in one pass for now.
 //       directives = node.getChildren();
 //       node = visitProgram(node, directives, "2", top);
       return node;
     }
     if ("1".equals(cpass) && top) {
       // emit compile-time contants to runtime
       Map constants = (Map)options.get(Compiler.COMPILE_TIME_CONSTANTS);
       if (constants != null) {
         String code = "";
         for (Iterator i = constants.entrySet().iterator(); i.hasNext(); ) {
           Map.Entry entry = (Map.Entry)i.next();
           Object value = entry.getValue();
           // Python cruft
           if (value instanceof String) {
             value = "\"" + value + "\"";
           } else if ((new Integer(0)).equals(value)) {
             value = "false";
           } else if ((new Integer(1)).equals(value)) {
             value = "true";
           }
           code += "var " + entry.getKey() + " = " + value + ";";
         }
         List c = new ArrayList();
         c.add(parseFragment(code));
         c.addAll(Arrays.asList(directives));
         directives = (SimpleNode[])c.toArray(directives);
         node.setChildren(directives);
       }
     }
 //     System.err.println("visitProgram: " + cpass);
     for (int index = 0, len = directives.length; index < len; index++) {
       SimpleNode directive = directives[index];
       SimpleNode newDirective = directive;
       SimpleNode[] children = directive.getChildren();
       if (directive instanceof ASTDirectiveBlock) {
         Compiler.OptionMap savedOptions = options;
         try {
           options = options.copy();
           newDirective = visitProgram(directive, children, cpass);
         }
         finally {
           options = savedOptions;
         }
       } else if (directive instanceof ASTIfDirective) {
         if (! options.getBoolean(Compiler.CONDITIONAL_COMPILATION)) {
           // TBD: different type; change to CONDITIONALS
           throw new CompilerError("`if` at top level");
         }
         Boolean value = evaluateCompileTimeConditional(directive.get(0));
         if (value == null) {
           newDirective = visitIfStatement(directive, children);
         } else if (value.booleanValue()) {
           SimpleNode clause = directive.get(1);
           newDirective = visitProgram(clause, clause.getChildren(), cpass);
         } else if (directive.size() > 2) {
           SimpleNode clause = directive.get(2);
           newDirective = visitProgram(clause, clause.getChildren(), cpass);
         } else {
           newDirective = new ASTEmptyExpression(0);
         }
       } else if (directive instanceof ASTIncludeDirective) {
         // Disabled by default, since it isn't supported in the
         // product.  (It doesn't go through the compilation
         // manager for dependency tracking.)
         if (! options.getBoolean(Compiler.INCLUDES)) {
           throw new UnimplementedError("unimplemented: #include", directive);
         }
         String userfname = (String)((ASTLiteral)directive.get(0)).getValue();
         newDirective = translateInclude(userfname, cpass);
       } else if (directive instanceof ASTProgram) {
         // This is what an include looks like in pass 2
         newDirective = visitProgram(directive, children, cpass);
       } else if (directive instanceof ASTPragmaDirective) {
         newDirective = visitPragmaDirective(directive, directive.getChildren());
       } else {
         if ("1".equals(cpass)) {
           // Function, class, and top-level expressions are processed in pass 1
           if (directive instanceof ASTFunctionDeclaration) {
             newDirective = visitStatement(directive);
           } else if (directive instanceof ASTClassDefinition) {
             newDirective = visitStatement(directive);
           } else if (directive instanceof ASTStatement) {
             // Statements are processed in pass 1 for now
             newDirective = visitStatement(directive);
             ;
           } else {
             newDirective = visitExpression(directive, false);
           }
         }
         if ("2".equals(cpass)) {
           // There is no pass 2 any more
           assert false : "bad pass " + cpass;
         }
       }
       if (! newDirective.equals(directive)) {
 //         System.err.println("directive: " + directive + " -> " + newDirective);
         directives[index] = newDirective;
       }
     }
     showStats(node);
     return node;
   }
 
   SimpleNode translateInclude(String userfname, String cpass) {
 
     if (Compiler.CachedInstructions == null) {
       Compiler.CachedInstructions = new ScriptCompilerCache();
     }
 
     File file = includeNameToFile(userfname);
     String source = includeFileToSourceString(file, userfname);
 
     try {
       String optionsKey = 
         getCodeGenerationOptionsKey(Collections.singletonList(
                                       // The constant pool isn't cached, so it doesn't affect code
                                       // generation so far as the cache is concerned.
                                       Compiler.DISABLE_CONSTANT_POOL));
       // If these could be omitted from the key for files that didn't
       // reference them, then the cache could be shared between krank
       // and krank debug.  (The other builds differ either on OBFUSCATE,
       // RUNTIME, NAMEFUNCTIONS, or PROFILE, so there isn't any other
       // possible sharing.)
       String instrsKey = file.getAbsolutePath();
       // Only cache on file and pass, to keep cache size resonable,
       // but check against optionsKey
       String instrsChecksum = "" + file.lastModified() + optionsKey; // source;
       // Use previously modified parse tree if it exists
       SimpleNode instrs = (SimpleNode)Compiler.CachedInstructions.get(instrsKey + cpass, instrsChecksum);
       if (instrs == null) {
         ParseResult result = parseFile(file, userfname, source);
         if ("1".equals(cpass)) {
           instrs = result.parse;
           instrs = translateInternal(instrs, cpass, false);
         } else if ("2".equals(cpass)) {
           instrs = (SimpleNode)Compiler.CachedInstructions.get(instrsKey + "1", instrsChecksum);
           assert instrs != null : "pass 2 before pass 1?";
           instrs = translateInternal(instrs, cpass, false);
         } else {
           assert false : "bad pass " + cpass;
         }
         if (! result.hasIncludes) {
           if (options.getBoolean(Compiler.CACHE_COMPILES)) {
             Compiler.CachedInstructions.put(instrsKey + cpass, instrsChecksum, instrs);
           }
         }
       }
       return instrs;
     }
     catch (ParseException e) {
       System.err.println("while compiling " + file.getAbsolutePath());
       throw e;
     }
   }
 
   public SimpleNode visitFunctionDeclaration(SimpleNode node, SimpleNode[] ast) {
     // Inner functions are handled by translateFunction
     if (context.findFunctionContext() != null) {
       return null;
     } else {
       assert (! options.getBoolean(Compiler.CONSTRAINT_FUNCTION));
       // Make sure all our top-level functions have root context
       if (false && ASTProgram.class.equals(context.type)) {
         Map map = new HashMap();
         map.put("_1", new Compiler.Splice(ast));
         SimpleNode newNode = (new Compiler.Parser()).substitute("with (_root) { _1 }", map);
         return visitStatement(newNode);
       } else {
         return translateFunction(node, true, ast);
       }
     }
   }
 
   //
   // Statements
   //
 
   public SimpleNode visitVariableDeclaration(SimpleNode node, SimpleNode[] children) {
     ASTIdentifier id = (ASTIdentifier)children[0];
     boolean scriptElement = options.getBoolean(Compiler.SCRIPT_ELEMENT);
     if (scriptElement) {
       if (children.length > 1) {
         // In script, variables are declared at the top of the
         // function so we convert the declaration into an assignment
         // here.
         SimpleNode newNode = new ASTAssignmentExpression(0);
         newNode.set(0, children[0]);
         ASTOperator assign = new ASTOperator(0);
         assign.setOperator(ParserConstants.ASSIGN);
         newNode.set(1, assign);
         newNode.set(2, children[1]);
         return visitExpression(newNode);
       } else {
         // Declarations already handled in a script
         return new ASTEmptyExpression(0);
       }
     } else {
       if (children.length > 1) {
         SimpleNode initValue = children[1];
         JavascriptReference ref = translateReference(id);
         children[1] = visitExpression(initValue);
         children[0] = ref.init();
         return node;
       } else {
         JavascriptReference ref = translateReference(id);
         children[0] = ref.declare();
         return node;
       }
     }
   }
 
   public SimpleNode visitIfStatement(SimpleNode node, SimpleNode[] children) {
     SimpleNode test = children[0];
     SimpleNode a = children[1];
     SimpleNode b = (children.length > 2) ? children[2] : null;
     // Compile-time conditional evaluations
 //     System.err.println("visitIfStatement: " +  (new Compiler.ParseTreePrinter()).visit(node));
     Boolean value = evaluateCompileTimeConditional(test);
     if (value != null) {
 //       System.err.println("" + test + " == " + value);
       if (value.booleanValue()) {
         return visitStatement(a);
       } else if (b != null) {
         return visitStatement(b);
       } else {
         return new ASTEmptyExpression(0);
       }
     } else if (b != null) {
       children[0] = visitExpression(test);
       children[1] = visitStatement(a);
       children[2] = visitStatement(b);
     } else {
       children[0] = visitExpression(test);
       children[1] = visitStatement(a);
     }
     return node;
   }
 
   public SimpleNode visitWhileStatement(SimpleNode node, SimpleNode[] children) {
     SimpleNode test = children[0];
     SimpleNode body = children[1];
     // TODO: [2003-04-15 ptw] bind context slot macro
     try {
       context = new TranslationContext(ASTWhileStatement.class, context);
       children[0] = visitExpression(test);
       children[1] = visitStatement(body);
       return node;
     }
     finally {
       context = context.parent;
     }
   }
 
   public SimpleNode visitDoWhileStatement(SimpleNode node, SimpleNode[] children) {
     SimpleNode body = children[0];
     SimpleNode test = children[1];
     // TODO: [2003-04-15 ptw] bind context slot macro
     try {
       context = new TranslationContext(ASTDoWhileStatement.class, context);
       children[0] = visitStatement(body);
       children[1] = visitExpression(test);
       return node;
     }
     finally {
       context = context.parent;
     }
   }
 
   public SimpleNode visitForStatement(SimpleNode node, SimpleNode[] children) {
     return translateForStatement(node, children);
   }
 
   public SimpleNode visitForVarStatement(SimpleNode node, SimpleNode[] children) {
     return translateForStatement(node, children);
   }
 
   SimpleNode translateForStatement(SimpleNode node, SimpleNode[] children) {
     SimpleNode init = children[0];
     SimpleNode test = children[1];
     SimpleNode step = children[2];
     SimpleNode body = children[3];
     // TODO: [2003-04-15 ptw] bind context slot macro
     Compiler.OptionMap savedOptions = options;
     try {
       options = options.copy();
       context = new TranslationContext(ASTForStatement.class, context);
       options.putBoolean(Compiler.WARN_GLOBAL_ASSIGNMENTS, true);
       children[0] = visitStatement(init);
       options.putBoolean(Compiler.WARN_GLOBAL_ASSIGNMENTS, false);
       children[1] = visitExpression(test);
       children[3] = visitStatement(body);
       children[2] = visitStatement(step);
       return node;
     }
     finally {
       context = context.parent;
       options = savedOptions;
     }
   }
 
   public SimpleNode visitForInStatement(SimpleNode node, SimpleNode[] children) {
     SimpleNode var = children[0];
     SimpleNode obj = children[1];
     SimpleNode body = children[2];
     // TODO: [2003-04-15 ptw] bind context slot macro
     try {
       context = new TranslationContext(ASTForInStatement.class, context);
       children[1] = visitExpression(obj);
       JavascriptReference ref = translateReference(var);
       children[0] = ref.set(true);
       children[2] = visitStatement(body);
       return node;
     }
     finally {
       context = context.parent;
     }
   }
 
   // This works because keys are always strings, and enumerate pushes
   // a null before all the keys
   public void unwindEnumeration(SimpleNode node) {
   }
 
   SimpleNode translateForInStatement(SimpleNode node, SimpleNode var,
                                Instructions.Instruction varset, SimpleNode obj,
                                SimpleNode body) {
     // TODO: [2003-04-15 ptw] bind context slot macro
     try {
       SimpleNode[] children = node.getChildren();
       context = new TranslationContext(ASTForInStatement.class, context);
       children[2] = visitExpression(obj);
       JavascriptReference ref = translateReference(var);
       if (varset == Instructions.VarEquals) {
         children[0] = ref.init();
       } else {
         children[0] = ref.set(true);
       }
       children[3] = visitStatement(body);
       return node;
     }
     finally {
       context = context.parent;
     }
   }
 
   SimpleNode translateAbruptCompletion(SimpleNode node, String type, ASTIdentifier label) {
     return node;
   }
 
   public SimpleNode visitReturnStatement(SimpleNode node, SimpleNode[] children) {
     SimpleNode value = children[0];
     children[0] = visitExpression(value);
     return node;
   }
 
   public SimpleNode visitWithStatement(SimpleNode node, SimpleNode[] children) {
     SimpleNode expr = children[0];
     SimpleNode stmt = children[1];
     children[0] = visitExpression(expr);
     children[1] = visitStatement(stmt);
     return node;
   }
 
   public SimpleNode visitTryStatement(SimpleNode node, SimpleNode[] children) {
     SimpleNode block = children[0];
     int len = children.length;
     assert len == 2 || len == 3;
     children[0] = visitStatement(block);
     if (len == 2) {
       // Could be catch or finally clause
       SimpleNode catfin = children[1];
       if (catfin instanceof ASTCatchClause) {
         // Treat the catch identifier as a binding.  This is not quite
         // right, need to integrate with variable analyzer, but this is
         // the one case in ECMAScript where a variable does have block
         // extent.
         catfin.set(0, translateReference(catfin.get(0)).declare());
         catfin.set(1, visitStatement(catfin.get(1)));
       } else {
         assert catfin instanceof ASTFinallyClause;
         catfin.set(0, visitStatement(catfin.get(0)));
       }
     } else if (len == 3) {
       SimpleNode cat = children[1];
       SimpleNode fin = children[2];
       assert cat instanceof ASTCatchClause;
       // Treat the catch identifier as a binding.  This is not quite
       // right, need to integrate with variable analyzer, but this is
       // the one case in ECMAScript where a variable does have block
       // extent.
       cat.set(0, translateReference(cat.get(0)).declare());
       cat.set(1, visitStatement(cat.get(1)));
       assert fin instanceof ASTFinallyClause;
       fin.set(0, visitStatement(fin.get(0)));
     }
     return node;
   }
   public SimpleNode visitThrowStatement(SimpleNode node, SimpleNode[] children) {
     SimpleNode expr = children[0];
     children[0] = visitExpression(expr);
     return node;
   }
 
   public SimpleNode visitSwitchStatement(SimpleNode node, SimpleNode[] children) {
     SimpleNode expr = children[0];
     // TODO: [2003-04-15 ptw] bind context slot macro
     try {
       context = new TranslationContext(ASTSwitchStatement.class, context);
       children[0] = visitExpression(expr);
       for (int i = 1, len = children.length; i < len; i++) {
         SimpleNode clause = children[i];
         if (clause instanceof ASTDefaultClause) {
           if (clause.size() > 0) {
             clause.set(0, visitStatement(clause.get(0)));
           }
         } else {
           assert clause instanceof ASTCaseClause : "case clause expected";
           clause.set(0, visitExpression(clause.get(0)));
           if (clause.size() > 1) {
             clause.set(1, visitStatement(clause.get(1)));
           }
         }
       }
       return node;
     }
     finally {
       context = context.parent;
     }
   }
 
   //
   // Expressions
   //
 
   boolean isExpressionType(SimpleNode node) {
     if (node instanceof Compiler.PassThroughNode) {
       node = ((Compiler.PassThroughNode)node).realNode;
     }
     // There are several AST types that end with each of the names that
     // endsWith tests for.
     String name = node.getClass().getName();
     return name.endsWith("Expression") ||
       name.endsWith("FunctionCallParameters") ||
       // TODO: [2006-01-11 ptw] Noone can explain this vestigial code, remove it
       //         name.substring(5).equals("Identifier") ||
       name.endsWith("ExpressionList") ||
       name.endsWith("ExpressionSequence") ||
       name.endsWith("Identifier") ||
       name.endsWith("Literal") ||
       name.endsWith("Reference");
   }
 
   public SimpleNode visitExpression(SimpleNode node) {
     return visitExpression(node, true);
   }
 
   /* This function, unlike the other expression visitors, can be
      applied to any expression node, so it dispatches based on the
      node's class. */
   public SimpleNode visitExpression(SimpleNode node, boolean isReferenced) {
     assert isExpressionType(node) : "" + node + ": " + (new Compiler.ParseTreePrinter()).visit(node) + " is not an expression";
 
     if (this.debugVisit) {
       System.err.println("visitExpression: " + node.getClass());
     }
 
     SimpleNode newNode = dispatchExpression(node, isReferenced);
 
     if ((! isReferenced) && (newNode == null)) {
       newNode = new ASTEmptyExpression(0);
     }
     if (this.debugVisit) {
       if (! newNode.equals(node)) {
         System.err.println("expression: " + node + " -> " + newNode);
       }
     }
     return newNode;
   }
 
   public SimpleNode visitIdentifier(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     // Side-effect free expressions can be suppressed if not referenced
     // Following is disabled by default for regression testing.
     // TODO: [2003-02-17 ows] enable this
     if ((! isReferenced) && options.getBoolean(Compiler.ELIMINATE_DEAD_EXPRESSIONS)) {
       return null;
     }
     if ("_root".equals(((ASTIdentifier)node).getName()) && (! options.getBoolean(Compiler.ALLOW_ROOT))) {
       throw new SemanticError("Illegal variable name: " + node, node);
     }
     return translateReference(node).get();
   }
 
   public SimpleNode visitLiteral(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     // Side-effect free expressions can be suppressed if not referenced
     // Following is disabled by default for regression testing.
     // TODO: [2003-02-17 ows] enable this
     if ((! isReferenced) && options.getBoolean(Compiler.ELIMINATE_DEAD_EXPRESSIONS)) {
       return null;
     }
     return translateLiteralNode(node);
   }
 
   public SimpleNode visitExpressionList(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     // all but last expression will not be referenced, so
     // visitExpression will pop it.  If the list is not referenced,
     // then the last will be popped too
     int i = 0, len = children.length - 1;
     for ( ; i < len; i++) {
       children[i] = visitExpression(children[i], false);
     }
     children[len] = visitExpression(children[len], isReferenced);
     return node;
   }
 
   public SimpleNode visitEmptyExpression(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     // Side-effect free expressions can be suppressed if not referenced
     if ((! isReferenced)) {
       return null;
     }
     return node;
   }
 
   public SimpleNode visitThisReference(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     // Side-effect free expressions can be suppressed if not referenced
     if ((! isReferenced)) {
       return null;
     }
     return translateReference(node).get();
   }
 
   public SimpleNode visitArrayLiteral(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     boolean suppressed = (! isReferenced);
     for (int i = 0, len = children.length; i <len; i++) {
       children[i] = visitExpression(children[i], isReferenced);
     }
     return node;
   }
 
   public SimpleNode visitObjectLiteral(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     boolean isKey = true;
     for (int i = 0, len = children.length; i < len; i++) {
       SimpleNode item = children[i];
       if (isKey && item instanceof ASTIdentifier) {
         // Identifiers are a shorthand for a literal string, should
         // not be evaluated (or remapped).
         ;
       } else {
         children[i] = visitExpression(item);
       }
       isKey = (! isKey);
     }
     return node;
   }
 
   public SimpleNode visitFunctionExpression(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     Compiler.OptionMap savedOptions = options;
     try {
       options = options.copy();
       options.putBoolean(Compiler.CONSTRAINT_FUNCTION, false);
       // Make sure all our top-level functions have root context
 //       if (ASTProgram.class.equals(context.type)) {
 //         Map map = new HashMap();
 //         map.put("_1", new Compiler.Splice(children));
 //         SimpleNode newNode = (new Compiler.Parser()).substitute("with (_root) { _1 }", map);
 //         visitStatement(newNode);
 //       } else {
         return translateFunction(node, false, children);
 //       }
     }
     finally {
       options = savedOptions;
     }
   }
 
   public SimpleNode visitFunctionCallParameters(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     translateFunctionCallParameters(node, isReferenced, children);
     return node;
   }
 
   public SimpleNode[] translateFunctionCallParameters(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     for (int i = 0, len = children.length; i < len; i++) {
       children[i] = visitExpression(children[i]);
     }
     return children;
   }
 
   public SimpleNode visitPropertyIdentifierReference(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     return translateReference(node).get();
   }
 
   public SimpleNode visitPropertyValueReference(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     return translateReference(node).get();
   }
 
   public SimpleNode makeCheckedNode(SimpleNode node) {
     if (options.getBoolean(Compiler.DEBUG) && options.getBoolean(Compiler.WARN_UNDEFINED_REFERENCES)) {
       String file = "null";
       String line = "null";
       if (node.filename != null) {
         file = ScriptCompiler.quote(node.filename);
         line = "" + node.beginLine;
       }
       Map map = new HashMap();
       map.put("_1", node);
       return new Compiler.PassThroughNode((new Compiler.Parser()).substitute(
         "(Debug.evalCarefully(" + file + ", " + line + ", function () { return _1; }, this))", map));
     }
     return node;
   }
 
   // Could do inline expansions here, like setAttribute
   public SimpleNode visitCallExpression(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     SimpleNode fnexpr = children[0];
     SimpleNode[] args = children[1].getChildren();
     int arglen = args.length;
 
     // TODO: [2002-12-03 ptw] There should be a more general
     // mechanism for matching patterns against AST's and replacing
     // them.
     // FIXME: [2002-12-03 ptw] This substitution is not correct
     // because it does not verify that the method being inlined is
     // actually LzNode.setAttribute.
     if (
       // Here this means 'compiling the lfc'
       options.getBoolean(Compiler.FLASH_COMPILER_COMPATABILITY) &&
       (! options.getBoolean("passThrough")) &&
       (fnexpr instanceof ASTPropertyIdentifierReference)) {
       SimpleNode[] fnchildren = fnexpr.getChildren();
       String name = ((ASTIdentifier)fnchildren[1]).getName();
       // We can't expand this if an expression value is expected,
       // since we don't have 'let'
       if (name.equals("setAttribute") && (! isReferenced)) {
         SimpleNode scope = fnchildren[0];
         SimpleNode property = args[0];
         SimpleNode value = args[1];
         List newBody = new ArrayList();
         String thisvar = "$lzsc$" + UUID().toString();
         String propvar = "$lzsc$" + UUID().toString();
         String valvar = "$lzsc$" + UUID().toString();
         String changedvar = "$lzsc$" + UUID().toString();
         String svar = "$lzsc$" + UUID().toString();
         String evtvar = "$lzsc$" + UUID().toString();
         String decls = "";
         Compiler.ParseTreePrinter ptp = new Compiler.ParseTreePrinter();
         if (scope instanceof ASTIdentifier || scope instanceof ASTThisReference) {
           thisvar = ptp.visit(scope);
         } else {
           decls += "var " + thisvar + " = " + ptp.visit(scope) + ";";
         }
         if (property instanceof ASTLiteral || property instanceof ASTIdentifier) {
           propvar = ptp.visit(property);
           if (property instanceof ASTLiteral) {
             assert propvar.startsWith("\"") || propvar.startsWith("'");
             evtvar = propvar.substring(0,1) + "on" + propvar.substring(1);
           }
         } else {
           decls += "var " + propvar + " = " + ptp.visit(property) + ";";
         }
         if (value instanceof ASTLiteral || value instanceof ASTIdentifier) {
           valvar = ptp.visit(value);
         } else {
           decls += "var " + valvar + " = " + ptp.visit(value) + ";";
         }
         if (arglen > 2) {
           SimpleNode ifchanged = args[2];
           if (ifchanged instanceof ASTLiteral || ifchanged instanceof ASTIdentifier) {
             changedvar = ptp.visit(ifchanged);
           } else {
             decls += "var " + changedvar + " = " + ptp.visit(ifchanged) + ";";
           }
         }
         newBody.add(parseFragment(decls));
         String fragment = "if (! (" + thisvar + ".__LZdeleted " +
             ((arglen > 2) ? ("|| (" + changedvar + " && (" + thisvar + "[" + propvar + "] == " + valvar + "))") : "") +
             ")) {" +
             "var " + svar + " = " + thisvar + ".setters;" +
             "if (" + svar + " && (" + propvar + " in " + svar + ")) {" +
             "    " + thisvar + "[" + svar + "[" + propvar + "]](" + valvar + ");" +
             "} else {" +
             "    if ($debug) {" +
             "        if (" + svar + " == null) {" +
             "            Debug.warn('null setters on', " + thisvar + ", " + propvar + ", " + valvar + ");" +
             "        }" +
             "    }" +
             "    " + thisvar + "[ " + propvar + " ] = " + valvar + ";" +
           ((property instanceof ASTLiteral) ? "" : ("    var " + evtvar + " = (\"on\" + " + propvar + ");")) +
             "    if (" + evtvar + " in " + thisvar + ") {" +
             "        if (" + thisvar + "[" + evtvar + "].ready) {" + thisvar + "[ " + evtvar + " ].sendEvent( " + valvar + " ); }" +
             "    }" +
           "}}";
         newBody.add(parseFragment(fragment));
         SimpleNode newStmts = new ASTStatementList(0);
         newStmts.setChildren((SimpleNode[])newBody.toArray(new SimpleNode[0]));
         return visitStatement(newStmts);
       }
     }
 
     children[1].setChildren(translateFunctionCallParameters(node, isReferenced, args));
     children[0] = translateReferenceForCall(fnexpr, true, node);
 //     if (options.getBoolean(Compiler.WARN_UNDEFINED_REFERENCES)) {
 //       return makeCheckedNode(node);
 //     }
     // Note current call-site in a function context and backtracing
     if ((options.getBoolean(Compiler.DEBUG_BACKTRACE) && (node.beginLine != 0)) &&
         (context.findFunctionContext() != null)) {
       SimpleNode newNode = new ASTExpressionList(0);
       newNode.set(0, (new Compiler.Parser()).parse("$lzsc$a.lineno = " + node.beginLine).get(0).get(0));
       newNode.set(1, new Compiler.PassThroughNode(node));
       return visitExpression(newNode);
     }
     return node;
   }
 
   public SimpleNode visitSuperCallExpression(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     SimpleNode n = translateSuperCallExpression(node, isReferenced, children);
     return visitExpression(n, isReferenced);
   }
 
   public SimpleNode visitNewExpression(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     for (int i = 0, len = children.length; i < len; i++) {
       SimpleNode child = children[i];
       children[i] = visitExpression(child, isReferenced);
     }
     return node;
   }
 
   public SimpleNode visitPrefixExpression(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     int op = ((ASTOperator)children[0]).getOperator();
     SimpleNode ref = children[1];
     if (translateReference(ref).isChecked()) {
       // The undefined reference checker needs to have this expanded
       // to work
       Map map = new HashMap();
       map.put("_1", ref);
       String pattern = "(function () { var $lzsc$tmp = _1; return _1 = $lzsc$tmp " + XfixInstrs.get(op) + " 1; })()";
       SimpleNode n = (new Compiler.Parser()).substitute(pattern, map);
       return visitExpression(n);
     }
     children[1] = translateReference(ref, 2).get();
     return node;
   }
 
   public SimpleNode visitPostfixExpression(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     SimpleNode ref = children[0];
     int op = ((ASTOperator)children[1]).getOperator();
     if (translateReference(ref).isChecked()) {
       // The undefined reference checker needs to have this expanded
       // to work
       Map map = new HashMap();
       map.put("_1", ref);
       String pattern = "(function () { var $lzsc$tmp = _1; _1 = $lzsc$tmp " + XfixInstrs.get(op) + " 1; return $lzsc$tmp; })()";
       SimpleNode n = (new Compiler.Parser()).substitute(pattern, map);
       return visitExpression(n);
     }
     children[0] = translateReference(ref, 2).get();
     return node;
   }
 
   public SimpleNode visitUnaryExpression(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     int op = ((ASTOperator)children[0]).getOperator();
     // I guess the parser doesn't know the difference
     if (ParserConstants.INCR == (op) || ParserConstants.DECR == (op)) {
       return visitPrefixExpression(node, isReferenced, children);
     }
     SimpleNode arg = children[1];
     // special-case typeof(variable) to not emit undefined-variable
     // checks so there is a warning-free way to check for undefined
     if (ParserConstants.TYPEOF == (op) &&
         (arg instanceof ASTIdentifier ||
          arg instanceof ASTPropertyValueReference ||
          arg instanceof ASTPropertyIdentifierReference)) {
       children[1] = translateReference(arg).get(false);
     } else {
       children[1] = visitExpression(arg);
     }
     return node;
   }
 
   public SimpleNode visitBinaryExpressionSequence(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     SimpleNode a = children[0];
     SimpleNode op = children[1];
     SimpleNode b = children[2];
     children[0] = visitExpression(a);
     children[2] = visitExpression(b);
     return node;
   }
 
   public SimpleNode visitBinaryExpression(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     SimpleNode op = children[0];
     SimpleNode a = children[1];
     SimpleNode b = children[2];
     children[1] = visitExpression(a);
     children[2] = visitExpression(b);
     return node;
   }
 
   SimpleNode translateAndOrExpression(SimpleNode node, boolean isand, SimpleNode a, SimpleNode b) {
     SimpleNode[] children = node.getChildren();
     children[0] = visitExpression(a);
     children[1] = visitExpression(b);
     return node;
   }
 
   public SimpleNode visitConditionalExpression(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     SimpleNode test = children[0];
     SimpleNode a = children[1];
     SimpleNode b = children[2];
     children[0] = visitExpression(test);
     children[1] = visitExpression(a);
     children[2] = visitExpression(b);
     return node;
   }
 
   public SimpleNode visitAssignmentExpression(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     SimpleNode lhs = children[0];
     int op = ((ASTOperator)children[1]).getOperator();
     SimpleNode rhs = children[2];
     if (op != ParserConstants.ASSIGN &&
         translateReference(lhs).isChecked()) {
       // The undefined reference checker needs to have this expanded
       // to work
       Map map = new HashMap();
       map.put("_1", lhs);
       map.put("_2", rhs);
       String pattern = "(function () { var $lzsc$tmp = _1; return _1 = $lzsc$tmp " + AssignOpTable.get(op) + " _2; })()";
       SimpleNode n = (new Compiler.Parser()).substitute(pattern, map);
       return visitExpression(n);
     }
     children[2] = visitExpression(rhs);
     children[0] = translateReference(lhs).set();
     return node;
   }
 
   // useName => declaration not expression
   SimpleNode translateFunction(SimpleNode node, boolean useName, SimpleNode[] children) {
     // TODO: [2003-04-15 ptw] bind context slot macro
     SimpleNode[] result;
     // methodName and scriptElement
     Compiler.OptionMap savedOptions = options;
     try {
       options = options.copy();
       context = new TranslationContext(ASTFunctionExpression.class, context);
       result = translateFunctionInternal(node, useName, children);
     }
     finally {
       options = savedOptions;
       context = context.parent;
     }
     node = result[0];
     // Dependency function is not compiled in the function context
     if (result[1] != null) {
       SimpleNode dependencies = result[1];
       Map map = new HashMap();
       map.put("_1", node);
       map.put("_2", translateFunction(dependencies, false, dependencies.getChildren()));
       SimpleNode newNode = (new Compiler.Parser()).substitute(
         "(function () {var $lzsc$f = _1; $lzsc$f.dependencies = _2; return $lzsc$f })();", map);
       return newNode;
     }
     return node;
   }
 
   static java.util.regex.Pattern identifierPattern = java.util.regex.Pattern.compile("[\\w$_]+");
 
   // Internal helper function for above
   // useName => declaration not expression
   SimpleNode[] translateFunctionInternal(SimpleNode node, boolean useName, SimpleNode[] children) {
     // ast can be any of:
     //   FunctionDefinition(name, args, body)
     //   FunctionDeclaration(name, args, body)
     //   FunctionDeclaration(args, body)
     // Handle the two arities:
     String functionName = null;
     SimpleNode params;
     SimpleNode stmts;
     SimpleNode depExpr = null;
     int stmtsIndex;
     ASTIdentifier functionNameIdentifier = null;
     if (children.length == 3) {
       functionNameIdentifier = (ASTIdentifier)children[0];
       params = children[1];
       stmts = children[stmtsIndex = 2];
       functionName = functionNameIdentifier.getName();
     } else {
       params = children[0];
       stmts = children[stmtsIndex = 1];
     }
     // inner functions do not get scriptElement treatment, shadow any
     // outer declaration
     options.putBoolean(Compiler.SCRIPT_ELEMENT, false);
     // or the magic with(this) treatment
     options.putBoolean(Compiler.WITH_THIS, false);
     // function block
     String userFunctionName = null;
     String filename = node.filename != null? node.filename : "unknown file";
     String lineno = "" + node.beginLine;
     if (functionName != null) {
       userFunctionName = functionName;
       if (! useName) {
         if ((! identifierPattern.matcher(functionName).matches())
             // TODO: [2007-02-21 ptw] Remove after Leopard
             || options.getBoolean("debugSafari")) {
           // This is a function-expression that has been annotated
           // with a non-legal function name, so remove that and put it
           // in _dbg_name (below)
           functionName = null;
           children[0] = new ASTEmptyExpression(0);
         }
       }
     } else {
       userFunctionName = "" + filename + "#" +  lineno + "/" + node.beginColumn;
     }
     // Tell metering to look up the name at runtime if it is not a
     // global name (this allows us to name closures more
     // mnemonically at runtime
     String meterFunctionName = useName ? functionName : null;
     Set pnames = new LinkedHashSet();
     SimpleNode[] paramIds = params.getChildren();
     for (int i = 0, len = paramIds.length; i < len; i++) {
       pnames.add(((ASTIdentifier)paramIds[i]).getName());
     }
     // Pull all the pragmas from the beginning of the
     // statement list: process them, and remove them
     assert stmts instanceof ASTStatementList;
     List stmtList = new ArrayList(Arrays.asList(stmts.getChildren()));
     for (int i = 0, len = stmtList.size(); i < len; i++) {
       SimpleNode stmt = (SimpleNode)stmtList.get(i);
       if (stmt instanceof ASTPragmaDirective) {
         SimpleNode newNode = visitStatement(stmt);
         if (! newNode.equals(stmt)) {
           stmtList.set(i, newNode);
         }
       } else {
         break;
       }
     }
     String methodName = (String)options.get(Compiler.METHOD_NAME);
     // Backwards compatibility with tag compiler
     if (methodName != null && functionNameIdentifier != null) {
         functionNameIdentifier.setName(functionName = methodName);
     }
     if (options.getBoolean(Compiler.CONSTRAINT_FUNCTION)) {
 //       assert (functionName != null);
       if (ReferenceCollector.DebugConstraints) {
         System.err.println("stmts: " + stmts);
       }
       // Find dependencies.
       //
       // Compute this before any transformations on the function body.
       //
       // The job of a constraint function is to compute a value.
       // The current implementation inlines the call to set the
       // attribute that the constraint is attached to, within the
       // constraint function it  Walking the statements of
       // the function will process the expression that computes
       // the value; it will also process the call to
       // setAttribute, but ReferenceCollector knows to ignore
       //
       ReferenceCollector dependencies = new ReferenceCollector(options.getBoolean(Compiler.COMPUTE_METAREFERENCES));
       // Only visit original body
       for (Iterator i = stmtList.iterator(); i.hasNext(); ) {
         SimpleNode stmt = (SimpleNode)i.next();
         dependencies.visit(stmt);
       }
       depExpr = dependencies.computeReferences(userFunctionName);
       if (options.getBoolean(Compiler.PRINT_CONSTRAINTS)) {
         (new Compiler.ParseTreePrinter()).print(depExpr);
       }
     }
     List prefix = new ArrayList();
     List suffix = new ArrayList();
     if (options.getBoolean(Compiler.DEBUG_BACKTRACE)) {
       prefix.add(parseFragment(
                    "var $lzsc$s = Debug['backtraceStack'];" +
                    "if ($lzsc$s) {" +
                    "  var $lzsc$a = Array.prototype.slice.call(arguments, 0);" +
                    "  $lzsc$a.callee = arguments.callee;" +
                    "  $lzsc$a['this'] = this;" +
                    "  $lzsc$s.push($lzsc$a);" +
                    "  if ($lzsc$s.length > $lzsc$s.maxDepth) {Debug.stackOverflow()};" +
                    "}"));
       suffix.add(parseFragment(
                     "if ($lzsc$s) {" +
                     "  $lzsc$s.pop();" +
                     "}"));
     }
     if (options.getBoolean(Compiler.PROFILE)) {
       prefix.add((meterFunctionEvent(node, "calls", meterFunctionName)));
       suffix.add((meterFunctionEvent(node, "returns", meterFunctionName)));
     }
 
     // Analyze local variables (and functions)
     VariableAnalyzer analyzer = new VariableAnalyzer(params, options.getBoolean(Compiler.FLASH_COMPILER_COMPATABILITY));
     for (Iterator i = prefix.iterator(); i.hasNext(); ) {
       analyzer.visit((SimpleNode)i.next());
     }
     for (Iterator i = stmtList.iterator(); i.hasNext(); ) {
       analyzer.visit((SimpleNode)i.next());
     }
     for (Iterator i = suffix.iterator(); i.hasNext(); ) {
       analyzer.visit((SimpleNode)i.next());
     }
     analyzer.computeReferences();
     // Parameter _must_ be in order
     LinkedHashSet parameters = analyzer.parameters;
     // Linked for determinism for regression testing
     Set variables = analyzer.variables;
     LinkedHashMap fundefs = analyzer.fundefs;
     Set closed = analyzer.closed;
     Set free = analyzer.free;
     // Note usage due to activation object and withThis
     if (! free.isEmpty()) {
       // TODO: [2005-06-29 ptw] with (_root) should not be
       // necessary for the activation object case now that it is
       // done at top level to get [[scope]] right.
       if (options.getBoolean(Compiler.ACTIVATION_OBJECT)) {
         analyzer.incrementUsed("_root");
       }
       if (options.getBoolean(Compiler.WITH_THIS)) {
         analyzer.incrementUsed("this");
       }
     }
     Map used = analyzer.used;
     // If this is a closure, annotate the Username for metering
     if ((! closed.isEmpty()) && (userFunctionName != null) && options.getBoolean(Compiler.PROFILE)) {
       // Is there any other way to construct a closure in js
       // other than a function returning a function?
       if (context.findFunctionContext().parent.findFunctionContext() != null) {
         userFunctionName = "" + closed + "." + userFunctionName;
       }
     }
     if (false) {
       System.err.println(userFunctionName +
                          ":: parameters: " + parameters +
                          ", variables: " + variables +
                          ", fundefs: " + fundefs +
                          ", used: " + used +
                          ", closed: " + closed +
                          ", free: " + free);
     }
     // Deal with warnings
     if (options.getBoolean(Compiler.WARN_UNUSED_PARAMETERS)) {
       Set unusedParams = new LinkedHashSet(parameters);
       unusedParams.removeAll(used.keySet());
       for (Iterator i = unusedParams.iterator(); i.hasNext(); ) {
         System.err.println("Warning: parameter " + i.next() + " of " + userFunctionName +
                            " unused in " + filename + "(" + lineno + ")");
       }
     }
     if (options.getBoolean(Compiler.WARN_UNUSED_LOCALS)) {
       Set unusedVariables = new LinkedHashSet(variables);
       unusedVariables.removeAll(used.keySet());
       for (Iterator i = unusedVariables.iterator(); i.hasNext(); ) {
         System.err.println("Warning: variable " + i.next() + " of " + userFunctionName +
                            " unused in " + filename + "(" + lineno + ")");
       }
     }
     // auto-declared locals
     Set auto = new LinkedHashSet();
     auto.add("this");
     auto.add("arguments");
     auto.retainAll(used.keySet());
     // parameters, locals, and auto-registers
     Set known = new LinkedHashSet(parameters);
     known.addAll(variables);
     known.addAll(auto);
     // for now, ensure that super has a value
     known.remove("super");
 
     // Look for #pragma
     boolean scriptElement = options.getBoolean(Compiler.SCRIPT_ELEMENT);
     Map registerMap = new HashMap();
     if (! scriptElement) {
       // All parameters and locals are remapped to 'registers' of the
       // form `$n`.  This prevents them from colliding with member
       // slots due to implicit `with (this)` added below, and also makes
       // the emitted code more compact.
       int regno = 1;
       boolean debug = options.getBoolean(Compiler.NAME_FUNCTIONS);
       for (Iterator i = (new LinkedHashSet(known)).iterator(); i.hasNext(); ) {
         String k = (String)i.next();
         String r;
         if (auto.contains(k) || closed.contains(k)) {
           ;
         } else {
           if (debug) {
             r = "$" + regno++ + "_" + k;
           } else {
             r = "$" + regno++;
           }
           registerMap.put(k, r);
           // remove from known map
           known.remove(k);
         }
       }
     }
     // Always set register map.  Inner functions should not see
     // parent registers (which they would if the setting of the
     // registermap were conditional on function vs. function2)
     context.setProperty(TranslationContext.REGISTERS, registerMap);
     // Set the knownSet.  This includes the parent's known set, so
     // closed over variables are not treated as free.
     Set knownSet = new LinkedHashSet(known);
     // Add parent known
     Set parentKnown = (Set)context.parent.get(TranslationContext.VARIABLES);
     if (parentKnown != null) {
       knownSet.addAll(parentKnown);
     }
     context.setProperty(TranslationContext.VARIABLES, knownSet);
 
     // Replace params
     for (int i = 0, len = paramIds.length; i < len; i++) {
       ASTIdentifier oldParam = (ASTIdentifier)paramIds[i];
       SimpleNode newParam = translateReference(oldParam).declare();
       params.set(i, newParam);
     }
 
 
     List newBody = new ArrayList();
 
     int activationObjectSize = 0;
     if (scriptElement) {
       // Create all variables (including inner functions) in global scope
       if (! variables.isEmpty()) {
         String code = "";
         for (Iterator i = variables.iterator(); i.hasNext(); ) {
           code += "_root." + (String)i.next() + "= void 0;";
         }
         newBody.add(parseFragment(code));
       }
     } else {
       // Leave var declarations as is
       // Emit function declarations here
       if (! fundefs.isEmpty()) {
         String code = "";
         for (Iterator i = fundefs.keySet().iterator(); i.hasNext(); ) {
           code += "var " + (String)i.next() + ";";
         }
         newBody.add(parseFragment(code));
       }
     }

    // Cf. LPP-4850: Prefix has to come after declarations (above).
    // FIXME: (LPP-2075) [2006-05-19 ptw] Wrap body in try and make
    // suffix be a finally clause, so suffix will not be skipped by
    // inner returns.
    newBody.addAll(prefix);

     // Now emit functions in the activation context
     // Note: variable has already been declared so assignment does the
     // right thing (either assigns to global or local
     for (Iterator i = fundefs.keySet().iterator(); i.hasNext(); ) {
       String name = (String)i.next();
       if (scriptElement || used.containsKey(name)) {
         SimpleNode fundecl = (SimpleNode)fundefs.get(name);
         SimpleNode funexpr = new ASTFunctionExpression(0);
         funexpr.setBeginLocation(fundecl.filename, fundecl.beginLine, fundecl.beginColumn);
         funexpr.setChildren(fundecl.getChildren());
         Map map = new HashMap();
         map.put("_1", funexpr);
         // Do I need a new one of these each time?
         newBody.add((new Compiler.Parser()).substitute(name + " = _1;", map));
       }
     }
     if ((! free.isEmpty()) && options.getBoolean(Compiler.WITH_THIS)) {
       SimpleNode newStmts = new ASTStatementList(0);
       newStmts.setChildren((SimpleNode[])stmtList.toArray(new SimpleNode[0]));
       SimpleNode withNode = new ASTWithStatement(0);
       SimpleNode id = new ASTThisReference(0);
       withNode.set(0, id);
       withNode.set(1, newStmts);
       newBody.add(withNode);
     } else {
       newBody.addAll(stmtList);
     }
     // FIXME: (LPP-2075) [2006-05-19 ptw] Wrap body in try and make
     // suffix be a finally clause, so suffix will not be skipped by
     // inner returns.
     if (! suffix.isEmpty()) {
       SimpleNode newStmts = new ASTStatementList(0);
       newStmts.setChildren((SimpleNode[])newBody.toArray(new SimpleNode[0]));
       SimpleNode tryNode = new ASTTryStatement(0);
       tryNode.set(0, newStmts);
       SimpleNode finallyNode = new ASTFinallyClause(0);
       SimpleNode suffixStmts = new ASTStatementList(0);
       suffixStmts.setChildren((SimpleNode[])suffix.toArray(new SimpleNode[0]));
       finallyNode.set(0, suffixStmts);
       tryNode.set(1, finallyNode);
       newBody = new ArrayList();
       newBody.add(tryNode);
     }
     // Process amended body
     SimpleNode newStmts = new ASTStatementList(0);
     newStmts.setChildren((SimpleNode[])newBody.toArray(new SimpleNode[0]));
     newStmts = visitStatement(newStmts);
     // Finally replace the function body with that whole enchilada
     children[stmtsIndex] = newStmts;
     if ( options.getBoolean(Compiler.NAME_FUNCTIONS)) {
       // TODO: [2007-09-04 ptw] Come up with a better way to
       // distinguish LFC from user stack frames.  See
       // lfc/debugger/LzBactrace
       String fn = (options.getBoolean(Compiler.FLASH_COMPILER_COMPATABILITY) ? "lfc/" : "") + filename;
       if (functionName != null &&
           // Either it is a declaration or we are not doing
           // backtraces, so the name will be available from the
           // runtime
           (useName || (! (options.getBoolean(Compiler.DEBUG_BACKTRACE))))) {
         if (options.getBoolean(Compiler.DEBUG_BACKTRACE)) {
           SimpleNode newNode = new ASTStatementList(0);
           newNode.set(0, new Compiler.PassThroughNode(node));
           newNode.set(1, parseFragment(functionName + "._dbg_filename = " + ScriptCompiler.quote(fn)));
           newNode.set(2, parseFragment(functionName + "._dbg_lineno = " + lineno));
           node = visitStatement(newNode);
         }
       } else {
         Map map = new HashMap();
         map.put("_1", node);
         SimpleNode newNode = new Compiler.PassThroughNode((new Compiler.Parser()).substitute(
           "(function () {" +
           "   var $lzsc$temp = _1;" +
           "   $lzsc$temp._dbg_name = " + ScriptCompiler.quote(userFunctionName) + ";" +
           ((options.getBoolean(Compiler.DEBUG_BACKTRACE)) ?
            ("   $lzsc$temp._dbg_filename = " + ScriptCompiler.quote(fn) + ";" +
             "   $lzsc$temp._dbg_lineno = " + lineno + ";") : 
            "") +
           "   return $lzsc$temp})()",
           map));
         node = newNode;
       }
     }
     if (options.getBoolean(Compiler.CONSTRAINT_FUNCTION)) {
       return new SimpleNode[] { node, depExpr };
     }
     return new SimpleNode[] { node, null };
   }
 
   SimpleNode translateLiteralNode(SimpleNode node) {
     return node;
   }
 
   SimpleNode translateReferenceForCall(SimpleNode ast) {
     return translateReferenceForCall(ast, false, null);
   }
 
   /* Contract is to leave a reference on the stack that will be
      dereferenced by CallFunction, etc.  Returns true if it
      succeeds.  Returns false if the ast is such that only the
      value of the reference can be pushed.  In this case, the
      callee, must use "CallMethod UNDEF" to call the value
      instead */
   SimpleNode translateReferenceForCall(SimpleNode ast, boolean checkDefined, SimpleNode node) {
     SimpleNode[] children = ast.getChildren();
     if (checkDefined) {
       assert node != null : "Must supply node for checkDefined";
     }
     if (ast instanceof ASTPropertyIdentifierReference) {
       JavascriptReference ref = translateReference(children[0]);
       String name = ((ASTIdentifier)children[1]).getName();
 //       if (checkDefined) {
 //         // TODO: needs to transform node
 //         checkUndefinedMethod(node, ref, name);
 //       }
       children[0] = ref.get();
     }
     if (ast instanceof ASTPropertyValueReference) {
       // TODO: [2002-10-26 ptw] (undefined reference coverage) Check
       JavascriptReference ref = translateReference(children[0]);
       children[1] = visitExpression(children[1]);
       children[0] = ref.get();
     }
     // The only other reason you visit a reference is to make a funcall
     boolean isref = true;
     if (ast instanceof ASTIdentifier) {
       JavascriptReference ref = translateReference(ast);
       ast = ref.preset();
     } else {
       ast = visitExpression(ast);
     }
     // TODO: wrap into node
 //     if (checkDefined) {
 //       checkUndefinedFunction(
 //         node,
 //         isref && ast instanceof ASTIdentifier ? ((ASTIdentifier)ast).getName() : null);
 //     }
     return ast;
   }
 
   JavascriptReference translateReference(SimpleNode node) {
     return translateReference(node, 1);
   }
 
   static public class JavascriptReference {
     protected Compiler.OptionMap options;
     SimpleNode node;
     SimpleNode checkedNode = null;
 
     public JavascriptReference(Translator translator, SimpleNode node, int referenceCount) {
       this.options = translator.getOptions();
       this.node = node;
     }
 
     public boolean isChecked() {
       return checkedNode != null;
     }
 
     public SimpleNode get(boolean checkUndefined) {
       if (checkUndefined && checkedNode != null) {
         return checkedNode;
       }
       return this.node;
     }
 
     public SimpleNode get() {
       return get(true);
     }
 
     public SimpleNode preset() {
       return this.node;
     }
 
     public SimpleNode set (Boolean warnGlobal) {
       return this.node;
     }
 
     public SimpleNode set() {
       return set(null);
     }
 
     public SimpleNode set(boolean warnGlobal) {
       return set(Boolean.valueOf(warnGlobal));
     }
 
     public SimpleNode declare() {
       return this.node;
     }
 
     public SimpleNode init() {
       return this.node;
     }
   }
 
   static public abstract class MemberReference extends JavascriptReference {
     protected SimpleNode object;
 
     public MemberReference(Translator translator, SimpleNode node, int referenceCount, 
                           SimpleNode object) {
       super(translator, node, referenceCount);
       this.object = object;
     }
   }
 
   static public class VariableReference extends JavascriptReference {
     TranslationContext context;
     public final String name;
 
     public VariableReference(Translator translator, SimpleNode node, int referenceCount, String name) {
       super(translator, node, referenceCount);
       this.name = name;
       this.context = (TranslationContext)translator.getContext();
       Map registers = (Map)context.get(TranslationContext.REGISTERS);
       // Replace identifiers with their 'register' (i.e. rename them)
       if (registers != null && registers.containsKey(name)) {
         String register = (String)registers.get(name);
         ASTIdentifier newNode = new ASTIdentifier(0);
         newNode.setName(register);
         this.node = new Compiler.PassThroughNode(newNode);
         return;
       }
       if (options.getBoolean(Compiler.WARN_UNDEFINED_REFERENCES)) {
         Set variables = (Set)context.get(TranslationContext.VARIABLES);
         if (variables != null) {
           boolean known = variables.contains(name);
           // Ensure undefined is "defined"
           known |= "undefined".equals(name);
           if (! known) {
             this.checkedNode = ((JavascriptGenerator)translator).makeCheckedNode(node);
           }
         }
       }
     }
 
     public SimpleNode declare() {
       Set variables = (Set)context.get(TranslationContext.VARIABLES);
       if (variables != null) {
         variables.add(this.name);
       }
       return this.node;
     }
 
     public SimpleNode init() {
       Set variables = (Set)context.get(TranslationContext.VARIABLES);
       if (variables != null) {
         variables.add(this.name);
       }
       return this.node;
     }
 
     public SimpleNode get(boolean checkUndefined) {
       if (checkUndefined && checkedNode != null) {
         return checkedNode;
       }
       return node;
     }
 
     public SimpleNode set(Boolean warnGlobal) {
       if (warnGlobal == null) {
         if (context.type instanceof ASTProgram) {
           warnGlobal = Boolean.FALSE;
         } else {
           warnGlobal = Boolean.valueOf(options.getBoolean(Compiler.WARN_GLOBAL_ASSIGNMENTS));
         }
       }
       if ((checkedNode != null) && warnGlobal.booleanValue()) {
         System.err.println("Warning: Assignment to free variable " + name +
                            " in " + node.filename + 
                            " (" + node.beginLine + ")");
       }
       return node;
     }
   }
 
   static public Set uncheckedProperties = new HashSet(Arrays.asList(new String[] {"call", "apply", "prototype"}));
 
   static public class PropertyReference extends MemberReference {
     String propertyName;
 
     public PropertyReference(Translator translator, SimpleNode node, int referenceCount, 
                                SimpleNode object, ASTIdentifier propertyName) {
       super(translator, node, referenceCount, object);
       this.propertyName = (String)propertyName.getName();
       // TODO: [2006-04-24 ptw] Don't make checkedNode when you know
       // that the member exists
       // This is not right, but Opera does not support [[Call]] on
       // call or apply, so we can't check for them
 //       if (! uncheckedProperties.contains(this.propertyName)) {
 //         this.checkedNode = ((JavascriptGenerator)translator).makeCheckedNode(node);
 //       }
     }
   }
 
   static public class IndexReference extends MemberReference {
     SimpleNode indexExpr;
 
     public IndexReference(Translator translator, SimpleNode node, int referenceCount, 
                           SimpleNode object, SimpleNode indexExpr) {
       super(translator, node, referenceCount, object);
       this.indexExpr = indexExpr;
       // We don't check index references for compatibility with SWF compiler
     }
   }
 
 
   JavascriptReference translateReference(SimpleNode node, int referenceCount) {
     if (node instanceof ASTIdentifier) {
       return new VariableReference(this, node, referenceCount, ((ASTIdentifier)node).getName());
     }
 
     SimpleNode[] args = node.getChildren();
     if (node instanceof ASTPropertyIdentifierReference) {
       args[0] = visitExpression(args[0]);
       // If args[1] is an identifier, it is a literal, otherwise
       // translate it.
       if (! (args[1] instanceof ASTIdentifier)) {
         args[1] = visitExpression(args[1]);
       }
       return new PropertyReference(this, node, referenceCount, args[0], (ASTIdentifier)args[1]);
     } else if (node instanceof ASTPropertyValueReference) {
       args[0] = visitExpression(args[0]);
       args[1] = visitExpression(args[1]);
       return new IndexReference(this, node, referenceCount, args[0], args[1]);
     }
 
     return new JavascriptReference(this, node, referenceCount);
   }
 }
 
 /**
  * @copyright Copyright 2006-2007 Laszlo Systems, Inc.  All Rights
  * Reserved.  Use is subject to license terms.
  */
 
