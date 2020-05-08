 /* -*- mode: Java; c-basic-offset: 2; -*- */
 
 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
 * Copyright 2007-2008 Laszlo Systems, Inc.  All Rights Reserved.              *
 * Use is subject to license terms.                                            *
 * J_LZ_COPYRIGHT_END *********************************************************/
 
 //
 // Parse Tree Printer for SWF9
 //
 
 package org.openlaszlo.sc;
 
 import java.io.*;
 import java.util.*;
 import java.util.regex.Pattern;
 import java.text.SimpleDateFormat;
 import java.text.DecimalFormat;
 
 import org.openlaszlo.server.LPS;
 import org.openlaszlo.sc.parser.*;
 import org.openlaszlo.sc.Translator;
 import org.openlaszlo.sc.Compiler.Ops;
 import org.openlaszlo.sc.Compiler.PassThroughNode;
 
 // Values
 import org.openlaszlo.sc.Values;
 
 // Instructions
 import org.openlaszlo.sc.Instructions;
 import org.openlaszlo.sc.Instructions.Instruction;
 import org.openlaszlo.sc.InstructionPrinter;
 
 // This class supports the Javascript translator
 public class SWF9ParseTreePrinter extends ParseTreePrinter {
 
   /** We collect certain parts of translation units
    * into the 'top level', appearing outside a class.
    */
   public final static int TOP_LEVEL_STREAM = 1;
 
   /** We collect certain parts of translation units
    * into the class level, appearing inside a class.
    */
   public final static int CLASS_LEVEL_STREAM = 2;
 
   /** We collect certain parts of translation units
    * to appear in the constructor for the main class only.
    */
   public final static int MAIN_CONSTRUCTOR_STREAM = 3;
 
   /** Main class gets default constructor inserted with initialization */
   private String mainClassName = null;
 
   /** True if we are creating a shared library */
   private boolean islib;
 
   /** State variable that is true while we are in a mixin */
   private boolean inmixin = false;
 
   // Adjust the known operator names we output to include
   // ones that we know about.
   static {
     OperatorNames[Ops.CAST] = "as";
     OperatorNames[Ops.IS] = "is";
   }
   
   public SWF9ParseTreePrinter() {
     this(false, false, null, false, false, null);
   }
   
   public SWF9ParseTreePrinter(boolean compress) {
     this(compress, false, null, false, false, null);
   }
   
   public SWF9ParseTreePrinter(boolean compress, boolean obfuscate) {
     this(compress, obfuscate, null, false, false, null);
   }
 
   public SWF9ParseTreePrinter(boolean compress, boolean obfuscate, String mainClassName, boolean sharedLibrary, boolean trackLines, String dumpAnnotationsFile) {
     // never compress or obfuscate
     super(false, false, trackLines, dumpAnnotationsFile);
     this.mainClassName = mainClassName;
     this.islib = sharedLibrary;
   }
 
   /**
    * Intercept parent.
    *
    * We need to know if we are inside a mixin, since it changes how we
    * emit its contents.  This must be noticed in previsit rather than in
    * visitClassDefinition because it's already too late there - the
    * unparser works bottom up.  At the moment we do not allow nested
    * classes/mixins, so we can keep a single state variable.
    */
   public SimpleNode previsit(SimpleNode node) {
     if (node instanceof ASTClassDefinition) {
       ASTIdentifier id = (ASTIdentifier)(node.getChildren()[0]);
       if (!"class".equals(id.getName())) {
         this.inmixin = true;
       }
     }
     return super.previsit(node);
   }
 
   // Override parent class.
   //
   // We need a translation unit (class) to put all the executable
   // statements appearing outside the class definitions - initially
   // these are put into the 'default' translation unit.  For building an
   // application or the LFC, we have a 'main' class that must
   // be present to accept these statements.
   //
   public List makeTranslationUnits(String annotated, SourceFileMap sources) {
     List result = super.makeTranslationUnits(annotated, sources);
     TranslationUnit defaultTunit = null;
     TranslationUnit mainTunit = null;
     for (Iterator iter = result.iterator(); iter.hasNext(); ) {
       TranslationUnit tunit = (TranslationUnit)iter.next();
       if (tunit.isDefaultTranslationUnit()) {
         defaultTunit = tunit;
         iter.remove();
       }
       else if (tunit.getName().equals(mainClassName)) {
         mainTunit = tunit;
       }
     }
 
     assert (defaultTunit != null);
 
     // If the main class is not present, we'll need to revisit
     // our assumptions about where we put top level statements.
     if (mainTunit != null) {
       String topstmts = defaultTunit.getContents();
       if (islib) {
         mainTunit.addStreamText(CLASS_LEVEL_STREAM, topstmts);
       }
       else {
         mainTunit.addStreamText(MAIN_CONSTRUCTOR_STREAM, topstmts);
       }
       mainTunit.setMainTranslationUnit(true);
     }
     else {
       throw new CompilerError("Could not find application main or library main class");
     }
 
     return result;
   }
 
   public static final String MULTILINE_COMMENT_BEGIN = "/*";
   public static final String MULTILINE_COMMENT_END = "*/";
 
   // push modifiers string next to the following keywords in the code.
   // modifiers, like 'static' or 'dynamic' normally appear before
   // keywords like 'function', 'class' or 'var'.  However, after
   // a function/class is processed, the resulting string may
   // contain comments, newlines or annotations.
   //
   // The annotation in fact may state that the class is beginning
   // at this point.  In order to get the modifier string into the right
   // file, we need to push it past the annotation.
   //
   // Also keywords like 'static' must appear on the same line as
   // 'function' so as not to tickle a bug in the third party
   // compiler.
   //
   // Moving the modifier next to the next piece of real code
   // resolves all these difficulties.
   protected String prependMods(String code, String mods)
   {
     if (mods.length() == 0)
       return code;
 
     mods += " ";
     // simple 
     int pos = 0;
     int len = code.length();
     pos = skipWhitespace(code, pos);
     if (code.substring(pos).startsWith(MULTILINE_COMMENT_BEGIN)) {
       pos = code.indexOf(MULTILINE_COMMENT_END, pos);
       if (pos < 0)
         return mods + code;
       pos += MULTILINE_COMMENT_END.length();
       pos = skipWhitespace(code, pos);
     }
     return code.substring(0, pos) + mods + code.substring(pos);
   }
 
   public static int skipWhitespace(String s, int pos)
   {
     int len = s.length();
     while (pos < len) {
       char ch = s.charAt(pos);
       if (ANNOTATE_MARKER == ch) {
         pos++;
         while (pos < len) {
           if (s.charAt(pos) == ANNOTATE_MARKER) {
             pos++;
             break;
           }
           pos++;
         }
       }
       else if (Character.isWhitespace(ch)) {
         pos++;
       }
       else
         break;
     }
     return pos;
   }
 
   // override
   public String visitModifiedDefinition(SimpleNode node, String[] children) {
     String mods = ((ASTModifiedDefinition)node).toJavascriptString();
     return prependMods(children[0], mods);
   }
   
   public String visitClassDefinition(SimpleNode node, String[] children) {
     String classnm = unannotate(children[1]);
     StringBuffer sb = new StringBuffer();
 
     // Everything inserted by #passthrough (toplevel:true) goes here.
     sb.append(annotateInsertStream(TOP_LEVEL_STREAM));
     String classtype = unannotate(children[0]);
     if ("class".equals(classtype)) {
       sb.append("class");
     }
     else if ("interface".equals(classtype) || "trait".equals(classtype)) {
       sb.append("interface");
     }
     else {
       throw new CompilerError("Internal error: bad classtype: " + classtype);
     }
     sb.append(SPACE + classnm + SPACE);
     if (unannotate(children[2]).length() > 0)
       sb.append("extends" + SPACE + children[2] + SPACE);
 
     // The meaning of children[3] is 'implements'
     if (unannotate(children[3]).length() > 0) {
       sb.append("implements" + SPACE + children[3] + SPACE);
     }
 
     sb.append("{\n");
     sb.append(annotateInsertStream(CLASS_LEVEL_STREAM));
     if (classnm.equals(mainClassName) && !islib) {
      sb.append("public function " + mainClassName + "(sprite:Sprite) {\n");
       sb.append("super(sprite);\n");
       sb.append(annotateInsertStream(MAIN_CONSTRUCTOR_STREAM));
       sb.append("}\n");
     }
     for (int i=4; i<children.length; i++) {
       sb.append(children[i]);
     }
     sb.append("}\n");
 
     // Note: assumes no nested mixins
     inmixin = false;
 
     return annotateClass(classnm, sb.toString());
   }
 
   // override - don't emit body of methods for mixins
   public String visitFunctionDeclaration(SimpleNode node, String[] children) {
     return doFunctionDeclaration(node, children, true, inmixin);
   }
   
   public String visitPragmaDirective(SimpleNode node, String[] children) {
     return "// (ignored) pragma " + children[0];
   }
   public String visitPassthroughDirective(SimpleNode node, String[] children) {
     ASTPassthroughDirective passthrough = (ASTPassthroughDirective)node;
     String text = passthrough.getText();
     if (passthrough.getBoolean(SWF9Generator.PASSTHROUGH_TOPLEVEL))
       text = annotateStream(TOP_LEVEL_STREAM, text);
     return text;
   }
 
   public String visitIdentifier(SimpleNode node, String[] children) {
     ASTIdentifier ident = (ASTIdentifier)node;
     String name = super.visitIdentifier(node, children);
     String type = ident.getType() == null ? "" : (":" + ident.getType());
     String ellipsis = ident.getEllipsis() ? "..." : "";
 
     return ellipsis + name + type;
   }
 }
   
