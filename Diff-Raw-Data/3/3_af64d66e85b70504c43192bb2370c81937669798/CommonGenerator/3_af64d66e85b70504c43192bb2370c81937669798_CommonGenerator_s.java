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
   SourceFileMap sources = new SourceFileMap();
 
   // Used for swf8 loadable libraries, to put stuff into _level0 namespace
   String globalprefix = "";
 
   public CommonGenerator() {
     setGeneratorOptions();
   }
 
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
     setGeneratorOptions();
   }
 
   // does nothing here - may be overridden to set options
   public void setGeneratorOptions() {
   }
 
 
   // Give the generators an option to save the original
   // input source for debugging.
 
   public void setOriginalSource(String source) {
     // no action by default
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
       "\n#file [CommonGenerator.parseFragment]\n#line 0\n" +
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
 
   //skip any passthrough nodes
   SimpleNode passThrough(SimpleNode node)
   {
     if (node instanceof Compiler.PassThroughNode)
       node = ((Compiler.PassThroughNode)node).realNode;
     return node;
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
 
   public void errPassthroughProperty(SimpleNode node, String key, Object value, String errorType) {
     throw new ParseException(key + ": " + errorType +
                              " for #passthrough property in " +
                              node.filename + " (" + node.beginLine + ")");
   }
 
   public void checkPassthroughProperty(SimpleNode node, String key, Object value) {
     // by default, no parameters are allowed
     // this can be overridden by the various runtime generators
     errPassthroughProperty(node, key, value, "unknown key");
   }
 
   public SimpleNode visitPassthroughDirective(SimpleNode node, SimpleNode[] children) {
     // just check parameters here.
     assert node instanceof ASTPassthroughDirective;
     Map props = ((ASTPassthroughDirective)node).getProperties();
     for (Iterator iter = props.keySet().iterator(); iter.hasNext(); ) {
       String key = (String)iter.next();
       Object value = props.get(key);
 
       checkPassthroughProperty(node, key, value);
     }
     return node;
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
 //     System.err.println("enter visitClassDefinition: " +  (new ParseTreePrinter()).text(node));
 
     ASTIdentifier classormixin = (ASTIdentifier)children[0];
     ASTIdentifier classname = (ASTIdentifier)children[1];
     String classnameString = classname.getName();
     SimpleNode superclass = children[2];
     SimpleNode mixins = children[3];
     SimpleNode mixinsandsuper;
     if (mixins instanceof ASTEmptyExpression) {
       if (superclass instanceof ASTEmptyExpression) {
         mixinsandsuper = new ASTLiteral(null);
       } else {
         mixinsandsuper = superclass;
       }
     } else {
       mixinsandsuper = new ASTArrayLiteral(0);
       mixinsandsuper.setChildren(mixins.getChildren());
       if (! (superclass instanceof ASTEmptyExpression)) {
         mixinsandsuper.set(mixinsandsuper.size(), superclass);
       }
     }
 
     SimpleNode[] dirs = (SimpleNode [])(Arrays.asList(children).subList(4, children.length).toArray(new SimpleNode[0]));
     List props = new ArrayList();
     List classProps = new ArrayList();
     List stmts = new ArrayList();
     translateClassDirectivesBlock(dirs, classnameString, props, classProps, stmts, TranslateHow.AS_PROPERTY_LIST);
 
     SimpleNode instanceProperties;
     if (props.isEmpty()) {
       instanceProperties = new ASTLiteral(null);
     } else {
       instanceProperties = new ASTArrayLiteral(0);
       instanceProperties.setChildren((SimpleNode[])(props.toArray(new SimpleNode[0])));
     }
     SimpleNode classProperties;
     if (classProps.isEmpty()) {
       classProperties = new ASTLiteral(null);
     } else {
       classProperties = new ASTArrayLiteral(0);
       classProperties.setChildren((SimpleNode[])(classProps.toArray(new SimpleNode[0])));
     }
     instanceProperties.setLocation(node);
     classProperties.setLocation(node);
 
     Map map = new HashMap();
     String xtor = "class".equals(classormixin.getName())?"Class":"Mixin";
     map.put("_1", classname);
     map.put("_2", mixinsandsuper);
     map.put("_3", instanceProperties);
     map.put("_4", classProperties);
     SimpleNode replNode = (new Compiler.Parser()).substitute(node,
                                                              xtor + ".make(" +
                                                              ScriptCompiler.quote(classnameString) +
                                                              ", _2, _3, _4);",
                                                              map);
     if (! "".equals(globalprefix)) {
       // The classname, with possibly prefix of "_level0.", for SWF8
       // runtime loadable libraries
       ASTIdentifier globalclassname = new ASTIdentifier(globalprefix + classnameString);
       SimpleNode varNode = new ASTVariableDeclaration(0);
       varNode.set(0, globalclassname);
       varNode.set(1, replNode);
       replNode = varNode;
     }
 
     if (! stmts.isEmpty()) {
       SimpleNode statements = new ASTStatementList(0);
       statements.setChildren((SimpleNode[])(stmts.toArray(new SimpleNode[0])));
       map.put("_5", statements);
       SimpleNode stmtNode = (new Compiler.Parser()).substitute(node,
                                                                "(function ($lzsc$c) { with($lzsc$c)"+
                                                                "with($lzsc$c.prototype) { _5 }})("+globalprefix+"_1)",
                                                                map);
       SimpleNode listNode = new ASTStatementList(0);
       listNode.set(0, replNode);
       listNode.set(1, stmtNode);
       replNode = listNode;
     }
     return visitStatement(replNode);
   }
 
   /**
    * If there are formal args that have initializers or indicate variable
    * arguments, add a preamble to the function to perform any needed
    * initialization to simulate the initialization.
    * For example:
    * <pre>
    *   function foo(w, x = null, y = null, ...z) {
    *       some statements
    *   }
    * </pre>
    * is rewritten as:
    * <pre>
    *   function foo(w, x, y) { 
    *      if (arguments.length < 2) { x = null; }
    *      if (arguments.length < 3) { y = null; }
    *      var z = new Array();
    *      for (__i=3; __i<arguments.length; __i++) {
    *          z.push(arguments[i]);
    *      }
    *      some statements
    *   }
    * </pre>
    * @param n function definition node
    * @return modified function definition node
    */
   public SimpleNode formalArgumentsTransformations(SimpleNode n) {
     // children are:
     //   (*) ASTIdentifier (name)      (*) may be omitted
     //       ASTFormalParameterList
     //       ASTStatement (or statement list)
     SimpleNode[] children = n.getChildren();
     int formalPos = 0;
     if (children.length > 0 && children[0] instanceof ASTIdentifier) {
       formalPos++;
     }
     assert (children.length == formalPos+2 && children[formalPos] instanceof ASTFormalParameterList)
       : "function has unexpected children";
 
     SimpleNode[] args = children[formalPos].getChildren();
     ASTIdentifier curid = null;
     List stmts = new ArrayList();
     List newargs = new ArrayList();
     Map map = new HashMap();
     
     // The argument list is a list of ASTIdentifiers and ASTFormalInitializer.
     // When a ASTFormalInitializer appears, it contains an initialization expression
     // that applies to the previous argument.  An ASTIdentifier can be marked
     // with an ellipsis, the grammar should enforce it is the last one.
     int argno = 0;
     String optional = null;
     String rest = null;
     // TODO: [2008-05-30 ptw] Does the parser ensure that args are
     // only permitted in required*, optional*, rest+ pattern?
     for (int i=0; i<args.length; i++) {
       if (args[i] instanceof ASTIdentifier) {
         curid = (ASTIdentifier)args[i];
         if (curid.getEllipsis()) {
           // Unfortunately, arguments is not an Array in some JS
           // runtimes, so we have to apply slice to it...
           rest = "var "+ curid.getName() +" = Array.prototype.slice.call(arguments, "+argno+");\n";
         }
         else {
           assert ((optional == null ||
                    (i+1 < args.length && args[i+1] instanceof ASTFormalInitializer))) :
             "Required argument after optional: " + (new ParseTreePrinter()).text(n);
           assert (rest == null) :
             "Required argument after rest: " + (new ParseTreePrinter()).text(n);
           newargs.add(args[i]);
         }
         argno++;
       }
       // TODO: [2008-05-30 ptw] This is a little bogus:  the parser
       // should be attaching the initializer as a property of the
       // parameter, not inserting it into the parameter list, IMO
       else if (args[i] instanceof ASTFormalInitializer) {
         assert curid != null : "ASTFormalInitializer appears first in list";
         assert rest == null : "Optional argument after rest: " +
           (new ParseTreePrinter()).text(n);
         SimpleNode initialValue = args[i].get(0);
         map.put("_" + argno, initialValue);
         if (optional == null) {
           optional = "switch (arguments.length) {\n";
         }
         optional += "  case " + (argno - 1) + ": " + curid.getName() + " = _" + argno +";\n";
       }
       else {
         throw new IllegalArgumentException("Unexpected item in argument list: " + args[i]);
       }
     }
 
     // any alterations needed?
     if (optional != null || rest != null) {
       String defaults = "";
       if (optional != null) {
         optional += "}\n";
         defaults += optional;
       }
       if (rest != null) {
         defaults += rest;
       }
       SimpleNode[] newNodes = (new Compiler.Parser()).substituteStmts(children[formalPos], defaults, map);
       stmts.addAll(flatten(newNodes));
       // newargs contains arguments without initializers
       children[formalPos].setChildren((SimpleNode[])newargs.toArray(new SimpleNode[0]));
 
       // Build a new statement list, consisting of new stmts for formal
       // initializations, followed by original statements.  Have to
       // keep original statments at the same level, in case there are
       // pragmas that apply to the function body!
       ASTStatementList oldstmt = (ASTStatementList)children[formalPos+1];
       stmts.addAll(Arrays.asList(oldstmt.getChildren()));
       SimpleNode newstmt = new ASTStatementList(0);
       newstmt.setChildren((SimpleNode[])stmts.toArray(new SimpleNode[0]));
       children[formalPos+1] = newstmt;
     }
     return n;
   }
 
   /*enum*/
   public static class TranslateHow {
     /** Runtime initialization of properties and methods */
     public static final TranslateHow AS_PROPERTY_LIST = new TranslateHow();
   
     /** Static definition of properties and methods */
     public static final TranslateHow AS_CLASS = new TranslateHow();
     
     /** Only keep public methods, and omit the method implementation */
     public static final TranslateHow AS_INTERFACE = new TranslateHow();
   
     private TranslateHow() {}
   }
 
   
   static SimpleNode undefined = parseFragment("void 0").get(1).get(0);
 
   static private void addVarProp(SimpleNode v, List p) {
     assert v instanceof ASTVariableDeclaration : v.getClass();
     p.add(new ASTLiteral(((ASTIdentifier)v.get(0)).getName()));
     if (v.getChildren().length > 1) {
       p.add(v.get(1));
     } else {
       p.add(undefined);
     }
   }
 
   // translate the class directives according to the 'how' argument.
   // If how is AS_PROPERTY_LIST, function name/values and variable
   // name/initvalues are added to either classProps (for statics)
   // or props (for non-statics).  For AS_CLASS, these
   // are added to stmts, for AS_INTERFACE, the public ones are added
   // to stmts.  Any other thing in the directive list is added
   // to stmts, no matter what 'how' is.
   public void translateClassDirectivesBlock(SimpleNode[] dirs, String classnameString, List props, List classProps, List stmts, TranslateHow how) {
 
     dirs = (SimpleNode[])(flatten(dirs).toArray(new SimpleNode[0]));
     // Scope #pragma directives to block
     Compiler.OptionMap savedOptions = options;
     try {
       for (int i = 0; i < dirs.length; i++) {
         SimpleNode n = dirs[i];
         List p = props;
 
         // any modifiers, like 'static', 'final' are kept in mod.
         ASTModifiedDefinition mod = null;
         boolean ispublic = false;
         boolean isconstructor = false;
         options = savedOptions.copy();
 
         if (n instanceof ASTModifiedDefinition) {
           assert (n.getChildren().length == 1);
           mod = (ASTModifiedDefinition)n;
           if (mod.isStatic()) {
             p = classProps;
           }
           ispublic = "public".equals(mod.getAccess());
           n = n.get(0);
           mod.verifyClassLevel(n);
         }
         if (n instanceof ASTFunctionDeclaration) {
           SimpleNode[] c = n.getChildren();
           assert c.length == 3;
           ASTIdentifier fid = (ASTIdentifier)c[0];
           String fname = fid.getName();
           isconstructor = classnameString.equals(fname);
           fid.setIsConstructor(isconstructor);
 
           if (how == TranslateHow.AS_PROPERTY_LIST) {
             // Transform constructor into '$lzsc$initialize' method
             if (isconstructor) {
               fname = "$lzsc$initialize";
               c[0] = new ASTIdentifier(fname);
             }
             p.add(new ASTLiteral(fname));
             SimpleNode funexpr = new ASTFunctionExpression(0);
             funexpr.setBeginLocation(n.filename, n.beginLine, n.beginColumn);
             funexpr.setChildren(c);
 
             if (mod != null) {
               mod = (ASTModifiedDefinition)mod.shallowCopy();
               SimpleNode[] newchildren = new SimpleNode[1];
               newchildren[0] = funexpr;
               mod.setChildren(newchildren);
               p.add(mod);
             }
             else {
               p.add(funexpr);
             }
           } else if (how == TranslateHow.AS_INTERFACE) {
             if (!isconstructor && ispublic) {
               stmts.add(n);
             }
           }
           else {
             assert how == TranslateHow.AS_CLASS;
             if (mod != null) {
               stmts.add(mod);
             } else {
               stmts.add(n);
             }
           }
         } else if (n instanceof ASTVariableStatement && how == TranslateHow.AS_PROPERTY_LIST) {
           SimpleNode [] c = n.getChildren();
           for (int j = 0, clen = c.length; j < clen; j++) {
             SimpleNode v = c[j];
             if (v instanceof ASTVariableDeclarationList) {
               SimpleNode [] d = v.getChildren();
               for (int k = 0, dlen = d.length ; k < dlen; k++) {
                 addVarProp(d[k], p);
               }
             } else {
               addVarProp(v, p);
             }
           }
         } else if (n instanceof ASTClassDirectiveBlock) {
           translateClassDirectivesBlock(n.getChildren(), classnameString, props, classProps, stmts, how);
         } else if (n instanceof ASTClassIfDirective) {
           Boolean value = evaluateCompileTimeConditional(n.get(0));
           if (value == null) {
             stmts.add(n);
           } else if (value.booleanValue()) {
             SimpleNode clause = n.get(1);
             translateClassDirectivesBlock(clause.getChildren(), classnameString, props, classProps, stmts, how);
           } else if (n.size() > 2) {
             SimpleNode clause = n.get(2);
             translateClassDirectivesBlock(clause.getChildren(), classnameString, props, classProps, stmts, how);
           }
         } else if (n instanceof ASTPragmaDirective) {
           visitPragmaDirective(n, n.getChildren());
         } else if (n instanceof ASTPassthroughDirective) {
           visitPassthroughDirective(n, n.getChildren());
           stmts.add(n);
         } else {
           if (how == TranslateHow.AS_CLASS && mod != null) {
             stmts.add(mod);
           } else if (how != TranslateHow.AS_INTERFACE) {
             // interfaces can only have functions, those are handled above
             stmts.add(n);
           }
         }
       }
     }
     finally {
       options = savedOptions;
     }
   }
 
   public void translateFormalParameters(SimpleNode params)
   {
     // Warn about any formal param initializers that are not '= null'
     int paramCount = 0;
     for (int i = 0, len = params.size(); i < len; i++) {
       SimpleNode param = passThrough(params.get(i));
       SimpleNode child;
       if (param instanceof ASTIdentifier) {
         paramCount++;
       }
       else if (!(param instanceof ASTFormalInitializer) || param.size() != 1) {
         System.err.println("Warning: unknown formal parameter in " +
                            param.filename + " (" + param.beginLine + ")");
       }
     }
 
     if (!options.getBoolean(Compiler.PASSTHROUGH_FORMAL_INITIALIZERS)) {
       // Remove any initializers seen.
       SimpleNode[] newParams = new SimpleNode[paramCount];
       paramCount = 0;
       for (int i = 0, len = params.size(); i < len; i++) {
         SimpleNode param = passThrough(params.get(i));
         if (param instanceof ASTIdentifier) {
           newParams[paramCount++] = param;
         }
       }
       params.setChildren(newParams);
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
     else if (node instanceof ASTPassthroughDirective) {
       newNode = visitPassthroughDirective(node, children);
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
     else if (node instanceof ASTVariableDeclarationList) {
       newNode = visitVariableDeclarationList(node, children);
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
     else if (node instanceof ASTModifiedDefinition) {
       newNode = visitModifiedDefinition(node, children);
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
 
   public SimpleNode visitVariableDeclarationList(SimpleNode node, SimpleNode[] children) {
     for (int i = 0, len = children.length ; i < len; i++) {
       children[i] = visitStatement(children[i]);
     }
     return node;
   }
 
   // for function prefix/suffix parsing
   public SimpleNode visitDirectiveBlock(SimpleNode node, SimpleNode[] children) {
     return visitStatementList(node, children);
   }
 
   // An ASTModifiedDefinition can appear as a statement (modifying
   // class and function definitions) or as an expression (modifying
   // function expressions)
   public SimpleNode visitModifiedDefinition(SimpleNode node, SimpleNode[] children) {
     // Modifiers, like 'final', are ignored unless this is handled
     // by the runtime.
 
     assert children.length == 1;
     SimpleNode child = children[0];
 
     ((ASTModifiedDefinition)node).verifyTopLevel(child);
 
     return visitStatement(child);
   }
 
   public SimpleNode visitModifiedDefinitionExpression(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     // Modifiers, like 'final', are ignored unless this is handled
     // by the runtime.
 
     assert children.length == 1;
     SimpleNode child = children[0];
 
     ((ASTModifiedDefinition)node).verifyTopLevel(child);
 
     return visitExpression(child, isReferenced);
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
       // super with no selector is the constructor, which will be
       // renamed to $lzsc$initialize in translateClassDirective to
       // mesh with lfc/compiler/Class.lzs framework
       name = "$lzsc$initialize";
     } else {
       name = ((ASTIdentifier)fname).getName();
     }
     Map map = new HashMap();
 
     // Transform `super.setAttribute` to a setter method call
     if ("setAttribute".equals(name)) {
       SimpleNode sargs[] = args.getChildren();
       assert sargs.length == 2;
       SimpleNode property = sargs[0];
       SimpleNode value = sargs[1];
       if (property instanceof ASTLiteral) {
         name = "$lzc$set_" + (String)((ASTLiteral)property).getValue();
         map.put("_1", new ASTLiteral(name));
       } else {
         pattern = "this.nextMethod(arguments.callee, '$lzc$set_' + _1).call(this, _2)";
         map.put("_1", property);
       }
       map.put("_2", value);
     } else {
       map.put("_1", new ASTLiteral(name));
       map.put("_2", new Compiler.Splice(args.getChildren()));
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
     if (ca == null) {
       ;
     } else if ("call".equals(ca)) {
       pattern = "(arguments.callee.superclass?arguments.callee.superclass.prototype[_1]:this.nextMethod(arguments.callee, _1)).call(_2)";
     } else if ("apply".equals(ca)) {
       pattern = "(arguments.callee.superclass?arguments.callee.superclass.prototype[_1]:this.nextMethod(arguments.callee, _1)).apply(_2)";
     } else {
       assert false: "Unhandled super call " + ca;
     }
     SimpleNode n = (new Compiler.Parser()).substitute(node, pattern, map);
     return n;
   }
 
   public SimpleNode visitVariableStatement(SimpleNode node, SimpleNode[] children) {
     return visitChildren(node);
   }
 
   // Hook for any special processing of globals, like noting them in
   // the known list, or emitting a declaration
   void addGlobalVar(String name, String type, String initializer) {
   }
 
   boolean isExpressionType(SimpleNode node) {
     // There are several AST types that end with each of the names that
     // endsWith tests for.
     String name = node.getClass().getName();
     return name.endsWith("Expression") ||
       name.endsWith("FunctionCallParameters") ||
       name.endsWith("ExpressionList") ||
       name.endsWith("ExpressionSequence") ||
       name.endsWith("Identifier") ||
       name.endsWith("Literal") ||
       name.endsWith("Reference");
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
     else if (node instanceof ASTModifiedDefinition) {
       newNode = visitModifiedDefinitionExpression(node, isReferenced, children);
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
 
   boolean isStatic(SimpleNode node) {
     SimpleNode parent = node.getParent();
     return (parent instanceof ASTModifiedDefinition &&
             ((ASTModifiedDefinition)parent).isStatic());
   }
 }
 
 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
 * Copyright 2001-2009 Laszlo Systems, Inc.  All Rights Reserved.              *
 * Use is subject to license terms.                                            *
 * J_LZ_COPYRIGHT_END *********************************************************/
