 package se.helino.mjc.backends.jvm;
 
 import java.io.PrintWriter;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.File;
 
 import java.util.ArrayList;
 import se.helino.mjc.frame.vm.*;
 import se.helino.mjc.parser.*;
 import se.helino.mjc.symbol.*;
 
 public class JasminFormatter implements Visitor {
 
     PrintWriter out;
     String basePath;
     ProgramTable symbolTable;
     ClassTable currentClass;
     MethodTable currentMethod;
     String activeConditionLabel;
     ArrayList<String> filenames = new ArrayList<String>();
 
     public JasminFormatter(String basePath, ProgramTable symbolTable) {
         this.symbolTable = symbolTable;
         this.basePath = basePath;
     }
 
     private String path(String name) {
         return basePath + File.separator + name + ".j";
     }
 
     private PrintWriter newFile(String name) {
         PrintWriter ret;
         try{
             String fname = path(name);
             filenames.add(fname);
             ret = new PrintWriter(
                     new BufferedWriter(
                         new FileWriter(fname)));
             return ret;
         } catch (java.io.IOException e) { 
             System.out.println("IOException in JasminFormatter!");
             System.out.println(e.toString());
         }
         return null;
     }
 
     private VMAccess getAccess(String name) {
         VMAccess a;
         if(currentMethod != null) {
             a = currentMethod.getVMFrame().getAccess(name);
             if(a != null)
                 return a;
         }
         if(currentClass != null) {
             a = currentClass.getVMRecord().getAccess(name);
             if(a != null)
                 return a;
         }
         throw new IllegalStateException();
     }
 
     private void beginClass(String name) {
         out = newFile(name);
         out.println(".class '" + name + "'");
         out.println(".super java/lang/Object");
     }
 
     private void printConstructor() {
         out.println(".method public <init>()V");
         out.println("\taload_0");
         out.println("\tinvokespecial java/lang/Object/<init>()V");
         out.println("\treturn");
         out.println(".end method");
     }
 
     public ArrayList<String> generate(MJProgram p) {
         p.accept(this);
         return filenames;
     }
 
     public void visit(MJProgram n) { 
         n.getMJMainClass().accept(this);
         for(MJClass c : n.getMJClasses()) {
             c.accept(this);
         }
     }
 
     public void visit(MJMainClass n) {
         String name = n.getClassId().getName();
         beginClass(name);
         printConstructor();
 
         // Main method
         out.println(".method public static main([Ljava/lang/String;)V");
         out.println(".limit locals 1");
         out.println(".limit stack " + symbolTable.getMainStackLimit());
         for(MJStatement s : n.getStatements()) {
             s.accept(this);
         }
         out.println("return");
         out.println(".end method");
         out.close();
     }
 
     public void visit(MJClass n) {
         String name = n.getId().getName();
         beginClass(name);
 
         currentClass = symbolTable.getClassTable(name);
         for(MJVarDecl vd : n.getVariableDeclarations()) {
             vd.accept(this);
         }
 
         printConstructor();
 
         for(MJMethodDecl m : n.getMethods()) {
             m.accept(this);
         }
         out.close();
     }
 
     public void visit(MJVarDecl n) { 
         out.println(getAccess(n.getId().getName()).declare());
     }
 
     public void visit(MJMethodDecl n) {
         String name = n.getId().getName();
         currentMethod = currentClass.getMethodTable(name);
         VMFrame frame = currentMethod.getVMFrame();
         out.print(".method public " + name + "("); 
         for(TypeNamePair p : currentMethod.getParams()) {
             out.print(Utils.convertType(p.getType()));
         }
         out.print(")");
         out.println(Utils.convertType(currentMethod.getReturnType()));
         out.println(".limit locals " + frame.getLocalLimit());
         out.println(".limit stack " + frame.getStackLimit());
         out.println(getAccess("this").declare());
         for(TypeNamePair p : currentMethod.getParams()) {
             out.println(getAccess(p.getName()).declare());
         }
         for(TypeNamePair p : currentMethod.getLocals()) {
             out.println(getAccess(p.getName()).declare());
         }
         for(MJStatement s : n.getBody().getMJStatements()) {
             s.accept(this);
         }
         n.getReturnExpression().accept(this);
         out.println(Utils.toTypePrefix(currentMethod.getReturnType()) + 
                     "return");
         out.println(".end method");
     }
     
     public void visit(MJCall n) {
         String label = activeConditionLabel;
         activeConditionLabel = null;
 
         n.getExpression().accept(this); 
         for(MJExpression e : n.getParameters()) {
             e.accept(this);
         }
         MJIdentifierType callee = symbolTable.getCalleeType(n);
         MethodTable mt = symbolTable.getClassTable(callee.getName()).
                                      getMethodTable(n.getMethodId().getName());
         StringBuffer sb = new StringBuffer();
         sb.append("invokevirtual ").
            append(callee.toString()).
            append("/").
            append(mt.getName()).
            append("(");
         for(TypeNamePair tp : mt.getParams()) {
             sb.append(Utils.convertType(tp.getType()));
         }
         sb.append(")");
         sb.append(Utils.convertType(mt.getReturnType()));
         out.println(sb.toString());
 
         if(label != null) {
             out.println("ifeq " + label);
         }
     }
 
     public void visit(MJAssign n) {
         VMAccess a = getAccess(n.getId().getName());
         if(a instanceof JVMField)
             out.println("aload 0");
         n.getExpression().accept(this);
         out.println(a.store());
     }
 
     public void visit(MJArrayAssign n) {
         VMAccess a = getAccess(n.getId().getName());
         out.println(a.load());
         n.getBracketExpression().accept(this);
         n.getExpression().accept(this);
         out.println("iastore");
     }
 
     public void visit(MJIf n) { 
         if(n.getCondition() instanceof MJTrue) {
             n.getIfStatement().accept(this);
         } else if(n.getCondition() instanceof MJFalse) {
             n.getElseStatement().accept(this);
         } else {
             String elseLabel = Utils.createLabel();
             String uniteLabel = Utils.createLabel();
             activeConditionLabel = elseLabel;
             
             MJExpression cond = n.getCondition();
             cond.accept(this);
             n.getIfStatement().accept(this);
             out.println("goto " + uniteLabel);
             
             out.println(elseLabel + ":");
             n.getElseStatement().accept(this);
            
             out.println(uniteLabel + ":");
         }
     }
 
     public void visit(MJBlock n) {
         for(MJStatement s : n.getStatements()) {
             s.accept(this);
         }
     }
 
     public void visit(MJWhile n) { 
         if(n.getCondition() instanceof MJFalse)
             return;
         String condLabel = Utils.createLabel();
         String uniteLabel = Utils.createLabel();
         activeConditionLabel = uniteLabel;
         out.println(condLabel + ":");
         n.getCondition().accept(this);
         n.getStatement().accept(this);
         out.println("goto " + condLabel);
         out.println(uniteLabel + ":");
     }
 
     public void visit(MJPrint n) {
         out.println("getstatic java/lang/System/out Ljava/io/PrintStream;");
         n.getExpression().accept(this);
         MJType param = symbolTable.getPrintParameterType(n);
         out.println("invokevirtual java/io/PrintStream/println(" + 
                     Utils.convertType(param) + ")V");
     }
 
     public void visit(MJIdentifierExp n) { 
         String label = activeConditionLabel;
         activeConditionLabel = null;
         out.println(getAccess(n.getName()).load());
         if(label != null) {
             out.println("ifeq " + label);
         }
     }
 
     public void visit(MJAnd n) { 
         String label = activeConditionLabel;
         activeConditionLabel = null;
         
         String falseLabel = Utils.createLabel();
         String trueLabel = Utils.createLabel();
         if(label != null) 
             falseLabel = label;
 
         n.getLeft().accept(this);
         out.println("ifeq " + falseLabel);
 
         n.getRight().accept(this);
         out.println("ifeq " + falseLabel);
         if(label == null) {
             String unite = Utils.createLabel();
             out.println("iconst_1");
             out.println("goto " + unite);
             out.println(falseLabel + ":");
             out.println("iconst_0");
             out.println(unite + ":");
         }
     }
 
     public void visit(MJLess n) {
         String label = activeConditionLabel;
         activeConditionLabel = null;
         n.getLeft().accept(this);
         n.getRight().accept(this);
         if(label != null) {
             out.println("if_icmpge " + label);
         }
         else {
             label = Utils.createLabel();
             String unite = Utils.createLabel();
             out.println("if_icmplt " + label);
             out.println("iconst_0");
             out.println("goto " + unite);
             out.println(label + ":");
             out.println("iconst_1");
             out.println(unite + ":");
         }
     }
     public void visit(MJPlus n) {
         n.getLeft().accept(this);
         n.getRight().accept(this);
         out.println("iadd");
     }
     public void visit(MJMinus n) {
         n.getLeft().accept(this);
         n.getRight().accept(this);
         out.println("isub");
     }
     public void visit(MJTimes n) {
         n.getLeft().accept(this);
         n.getRight().accept(this);
         out.println("imul");
     }
 
     public void visit(MJNot n) {
         String label = activeConditionLabel;
         activeConditionLabel = null;
         n.getExpression().accept(this);
         if(label != null) {
             out.println("ifne " + label);
         } else {
             label = Utils.createLabel();
             String unite = Utils.createLabel();
             out.println("ifeq " + label);
             out.println("iconst_0");
             out.println("goto " + unite);
             out.println(label + ":");
             out.println("iconst_1");
             out.println(unite + ":");
         }
     }
 
     public void visit(MJArrayLength n) {
         n.getExpression().accept(this);
         out.println("arraylength");
     }
 
     public void visit(MJArrayLookup n) {
         n.getLeft().accept(this);
         n.getRight().accept(this);
         out.println("iaload");
     }
 
     public void visit(MJNewObject n) { 
         String name = n.getId().getName();
         out.println("new '" + name + "'");
         out.println("dup");
         out.println("invokespecial " + name + "/<init>()V");
     }
 
     public void visit(MJNewArray n) {
         n.getExpression().accept(this);
         out.println("newarray int");
     }
 
     public void visit(MJTrue n) {
         out.println("iconst_1");
     }
 
     public void visit(MJFalse n) {
         out.println("iconst_0");
     }
 
     public void visit(MJIntegerLiteral n) { 
         int val = n.getValue();
         if (val == -1) {
             out.println("iconst_m1");
         } else if(val >=0 && val <= 5) {
             out.println("iconst_" + val); 
         } else if(val >= Byte.MIN_VALUE && val <= Byte.MAX_VALUE) {
             out.println("bipush " + val);
         }
         else if(val >= Short.MIN_VALUE && val <= Short.MAX_VALUE) {
             out.println("sipush " + val);
         }
         else {
             out.println("ldc " + val);
         }
     }
 
     public void visit(MJThis n) {
         out.println("aload 0");
     }
     
     public void visit(MJIdentifier n) { }
     public void visit(MJIntType n) { }
     public void visit(MJIntArrayType n) { }
     public void visit(MJBooleanType n) { }
     public void visit(MJIdentifierType n) { }
     public void visit(MJMethodArg n) { }
 }
