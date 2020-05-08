 package forkbomb.backend;
 
 import java.util.ArrayList;
 
 import forkbomb.backend.bytemarks.*;
 import forkbomb.intermediate.icodeimpl.*;
 import forkbomb.intermediate.symtabimpl.*;
 import forkbomb.intermediate.typeimpl.*;
 import forkbomb.util.Mercury;
 import lure.LureConstants;
 import wci.intermediate.*;
 
 import static forkbomb.intermediate.icodeimpl.ICodeKeyImpl.*;
 
 public class LureCompiler {
   private Instructor instructor;
   private int globalLabeCounter;
 
   public void printICTree(ICodeNode root) {
     for (ICodeNode n : root.getChildren()) {
       printICTree(n);
     }
   }
 
   public void compile(ICodeNode root) {
     if (System.getenv("LUREPATH") != null) {
       JasminInstructor.setOutputDirectory(System.getenv("LUREPATH"));
     }
     instructor = new JasminInstructor((String)root.getAttribute(VALUE));
     globalLabeCounter = 0;
 
     (new Generator(root)).generate();
   }
 
   private class Generator {
     private ICodeNode node;
     public Generator(ICodeNode node) {
       this.node = node;
     }
 
     public void generate() {
       Mercury.debug("Generating " + node.toString());
       switch((ICodeNodeTypeImpl)node.getType()) {
         case SCRIPT:
           generateScript();
           return;
         case ASSIGN:
           generateAssign();
           return;
         case STRING_CONSTANT:
           generateStringConstant();
           return;
         case INTEGER_CONSTANT:
           generateIntegerConstant();
           return;
         case VARIABLE:
           generateVariable();
           return;
         case CALL:
           generateCall();
           return;
         case FUNCTION:
           generateFunction();
           return;
         case IF:
           generateIf();
           return;
         case LOOP:
           generateLoop();
           return;
         case ACCESS:
           generateAccess();
           return;
         case FIELD:
           generateField();
           return;
         case NEWARRAY:
           generateNewArray();
           return;
       }
     }
 
     private void generateScript() {
       instructor.static_method("main([Ljava/lang/String;)V");
       // TODO limit locals to symtab count;
       instructor.limit_stack(32);
       instructor.limit_locals(32);
       for (ICodeNode n : node.getChildren()) {
         Generator g = new Generator(n);
         g.generate();
       }
       instructor._return();
       instructor.end_method();
       instructor.finish();
     }
 
     private void generateStringConstant() {
       instructor.ldc((String)node.getAttribute(VALUE));
     }
 
     private void generateIntegerConstant() {
       instructor.ldc((Integer)node.getAttribute(VALUE));
       instructor.invokestatic("java/lang/Integer/valueOf(I)Ljava/lang/Integer;");
     }
 
     private void generateAssign() {
       if (node.getChildren().size() > 1) {
         Mercury.warn("Multiple children of ASSIGN");
       }
 
       SymTabEntry e = (SymTabEntry)node.getAttribute(VALUE);
 
       /* Generate bytecode for the expression to be assigned. */
       (new Generator(node.getChildren().get(0))).generate();
       instructor.astore(e.getIndex());
     }
 
     private void generateAccess() {
       for (ICodeNode n : node.getChildren()) {
         (new Generator(n)).generate();
       }
     }
 
     private void generateVariable() {
       SymTabEntry e = (SymTabEntry)node.getAttribute(VALUE);
 
       if (e.isGlobal()) {
         instructor.getstatic((String)e.getAttribute(SymTabKeyImpl.GLOBAL_FIELD_SPEC),
             "Ljava/lang/Object;");
       } else {
         instructor.aload(e.getIndex());
       }
     }
 
     private void generateCall() {
       ICodeNode functionValue = node.getChildren().remove(0);
       (new Generator(functionValue)).generate();
 
       int arity = node.getChildren().size();
       for (ICodeNode arg : node.getChildren()) {
         (new Generator(arg)).generate();
       }
       instructor.invokevirtual(
           methodSignature(LureConstants.FUNCTION_CALL_SLUG, arity));
     }
 
     private void generateFunction() {
       /* Set up separate instructor for Function sublass. */
       Instructor mainInstructor = instructor;
       String className = (String)node.getAttribute(VALUE);
       instructor = new JasminInstructor(className, LureConstants.FUNCTION_CLASS);
       instructor.public_method("<init>()V");
       instructor.aload(0);
       instructor.invokenonvirtual(LureConstants.FUNCTION_INIT);
       instructor._return();
       instructor.end_method();
       // XXX LOLARITYCHECKINGWAHT?
       instructor.public_method(
           methodSignature(LureConstants.FUNCTION_METHOD_NAME,
            (Integer)((TypeSpecImpl)node.getTypeSpec()).getAttribute(TypeKeyImpl.ARITY)));
       instructor.limit_stack(32);
       instructor.limit_locals(32);
       for (ICodeNode n : node.getChildren()) {
         (new Generator(n)).generate();
       }
 
       instructor.areturn();
       instructor.end_method();
       instructor.finish();
       /* Restore original instructor. */
       instructor = mainInstructor;
       instructor._new(className);
       instructor.dup();
       instructor.invokespecial(className + "/<init>()V");
     }
 
     public void generateIf() {
       ArrayList<ICodeNode> children = node.getChildren();
       if (children.size() > 3) {
         Mercury.fatal("Dude, Conditional has too many kids.");
       }
 
       /* put test expression on the stack and test it */
       (new Generator(children.get(0))).generate();
       instructor.invokestatic(LureConstants.TEST_METHOD_SPEC);
 
       /* Generate label names for true and false/ */
       String truthyLabel = nextLabel(), falsyLabel = nextLabel();
 
       Mercury.debug("Generating " + children.size() + "-clause conditional");
       if (children.size() == 3) /* has an else clause */ {
         instructor.ifeq(truthyLabel);
         /* code for the else clause follows in case of fall through. */
         (new Generator(children.get(2))).generate();
         instructor._goto(falsyLabel);
         instructor.label(truthyLabel);
         (new Generator(children.get(1))).generate();
       } else /* no else clause */ {
         /* in this case just jump over the truthy clause */
         instructor.ifne(falsyLabel);
         (new Generator(children.get(1))).generate();
       }
       /* no matter what we end with the falsey label jumping over truthy. */
       instructor.label(falsyLabel);
     }
 
     public void generateLoop() {
       String testLabel = nextLabel(), endLabel = nextLabel();
       instructor.label(testLabel);
       (new Generator(node.getChildren().remove(0))).generate();
       instructor.invokestatic(LureConstants.TEST_METHOD_SPEC);
       instructor.ifne(endLabel);
       for (ICodeNode n : node.getChildren()) {
         (new Generator(n)).generate();
       }
       instructor._goto(testLabel);
       instructor.label(endLabel);
     }
 
     public void generateField() {
       instructor.ldc((String)node.getAttribute(VALUE));
       instructor.invokeinterface(LureConstants.OBJECT_GET_SPEC, 1);
     }
 
     public void generateNewArray() {
       instructor._new(LureConstants.LIST_CLASS_SPEC);
       instructor.dup();
       instructor.invokespecial(LureConstants.LIST_CLASS_SPEC + "/<init>()V");
     }
 
     /* Helpers */
     private String methodSignature(String slug, int arity) {
       StringBuilder s = new StringBuilder();
       s.append(slug);
       s.append("(");
       for (int i = 0; i < arity; i++) {
         s.append("Ljava/lang/Object;");
       }
       s.append(")Ljava/lang/Object;");
       return s.toString();
     }
 
     private String nextLabel() {
       return String.format(LABEL_FORMAT, globalLabeCounter++);
     }
   }
   private static final String LABEL_FORMAT = "L%03d";
 }
