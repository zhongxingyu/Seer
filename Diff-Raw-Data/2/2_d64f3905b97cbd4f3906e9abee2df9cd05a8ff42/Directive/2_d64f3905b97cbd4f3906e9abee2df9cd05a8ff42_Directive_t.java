 package org.webmacro.directive;
 
 import java.io.*;
 import java.util.*;
 import org.webmacro.*;
 import org.webmacro.engine.*;
 
 /**
  * Directive is an abstract class which directives can extend.  
  * Nested within Directive (as static classes) are a host of classes used
  * for building directive argument lists.  
  * 
  * Directives are Macros, so they must implement the Macro interface.  
  * For convenience, an implementation of evaluate() (written in terms of
  * write()) is provided in the base class.)  
  * 
  * Directives must implement the following static method:
  *   public DirectiveDescriptor getDescriptor();
  * 
  * It is expected that all directives will build a copy of their descriptor
  * statically, using the various XxxArg() constructors, and return a reference
  * to that from getDescriptor().  
  */ 
 
 public abstract class Directive implements Macro, Visitable { 
 
   public static final int ArgType_CONDITION    = 1;
   public static final int ArgType_LVALUE       = 2;
   public static final int ArgType_RVALUE       = 3;
   public static final int ArgType_KEYWORD      = 4;
   public static final int ArgType_ASSIGN       = 5;
   public static final int ArgType_BLOCK        = 6;
   public static final int ArgType_LITBLOCK     = 7;
   public static final int ArgType_SUBDIRECTIVE = 8;
   public static final int ArgType_QUOTEDSTRING = 9;
   public static final int ArgType_GROUP        = 50;
   public static final int ArgType_CHOICE       = 51;
 
   /**
    * Directives must implement a build() method.  The build method
    * should examine the directive arguments (available through the
    * DirectiveBuilder) and return a macro describing the built
    * directive.  In most cases, build() will just set up the
    * directive's private fields and return 'this', but in some
    * cases, no macro needs be returned (such as directives with only
    * side effects on the build context) or some other macro may be
    * returned (such as in the case of "#if (true) { }" -- no IfDirective
    * object need be returned, just return the block.)  
    */
 
   public abstract Object build(DirectiveBuilder b, 
                                BuildContext bc)
   throws BuildException;
 
   /* Convenience implementation of evaluate() which Directives can inherit */
   public Object evaluate(Context context)
     throws ContextException {
       try {
         ByteArrayOutputStream os = new ByteArrayOutputStream(256);
         FastWriter fw = new FastWriter(os, "UTF8");
         write(fw,context);
         fw.flush();
         return os.toString("UTF8");
       } catch (IOException e) {
          context.getBroker().getLog("engine").error(
            "Directive.evaluate: IO exception on write to StringWriter", e);
          return "";
       }
   }  
 
   /**
    * Convenience methods for directives to write HTML warnings into the output
    * stream.  Eventually this will be parameterizable so that HTML is not
    * assumed to be the only underlying language. 
    */
 
   protected static String getWarningText(String warning) 
   throws IOException {
     return "<!--\nWARNING: " + warning + " \n-->";
   }
 
   protected static void writeWarning(String warning, FastWriter writer) 
   throws IOException {
     writer.write(getWarningText(warning));
   }
 
   public void accept(TemplateVisitor v) {
     v.visitUnknownMacro(this.getClass().getName(), this);
   }
 
   /* Nested static classes */ 
 
 
   public static class ArgDescriptor {
     public final int id;
     public final int type;
     public boolean optional = false;
     public int subordinateArgs = 0, nextArg = 0;
     public int[] children;
     public String keyword;
 
     protected ArgDescriptor(int id, int type) { 
       this.id = id;
       this.type = type;
     }
 
     public int getSubordinateArgs() {
       return subordinateArgs;
     }
 
     protected void setOptional() {
       optional = true;
     }
 
     protected void setOptional(int subordinateArgs) {
       optional = true;
       this.subordinateArgs = subordinateArgs;
     }
   }
 
 
   public static class ConditionArg extends ArgDescriptor {
     public ConditionArg(int id) { 
       super(id, ArgType_CONDITION); 
     }
   }
 
   public static class BlockArg extends ArgDescriptor {
     public BlockArg(int id) { 
       super(id, ArgType_BLOCK); 
     }
   }
 
   public static class LiteralBlockArg extends ArgDescriptor {
     public LiteralBlockArg(int id) { 
       super(id, ArgType_LITBLOCK); 
     }
   }
 
   public static class RValueArg extends ArgDescriptor {
     public RValueArg(int id) { 
       super(id, ArgType_RVALUE); 
     }
   }
 
   public static class LValueArg extends ArgDescriptor {
     public LValueArg(int id) { 
       super(id, ArgType_LVALUE); 
     }
   }
 
   public static class QuotedStringArg extends ArgDescriptor {
     public QuotedStringArg(int id) { 
       super(id, ArgType_QUOTEDSTRING); 
     }
   }
 
   public static class KeywordArg extends ArgDescriptor {
     public KeywordArg(int id, String keyword) { 
       super(id, ArgType_KEYWORD); 
       this.keyword = keyword;
     }
   }
 
   public static class AssignmentArg extends ArgDescriptor {
     public AssignmentArg() { 
       super(0, ArgType_ASSIGN); 
     }
   }
 
   public static class OptionalGroup extends ArgDescriptor {
     public OptionalGroup(int argCount) { 
       super(0, ArgType_GROUP); 
      setOptional(argCount);
     }
   }
 
   public static class OptionChoice extends ArgDescriptor {
     public OptionChoice(int groupCount) { 
       super(0, ArgType_CHOICE); 
       setOptional(groupCount);
     }
   }
 
   public static class Subdirective extends ArgDescriptor {
     public final String name; 
     public final ArgDescriptor[] args;
     public boolean repeating = false;
     
     public Subdirective(int id, String name,
                         ArgDescriptor[] args) {
       super(id, ArgType_SUBDIRECTIVE);
       this.name = name;
       this.args = args;
     }
   }
 
   public static class OptionalSubdirective extends Subdirective {
     public OptionalSubdirective(int id, String name,
                                 ArgDescriptor[] args) {
       super(id, name, args);
       setOptional();
     }
   }
 
   public static class OptionalRepeatingSubdirective
     extends OptionalSubdirective {
     public OptionalRepeatingSubdirective(int id, String name,
                                          ArgDescriptor[] args) {
       super(id, name, args);
       repeating = true;
     }
   }
 
 
 }
 
