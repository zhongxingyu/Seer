 package com.lolcode.jgenerator;
 
 import com.lolcode.tree.*;
 import com.lolcode.tree.exception.BaseAstException;
 import org.objectweb.asm.*;
 import org.objectweb.asm.util.CheckClassAdapter;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 
 public class CodeGenerator implements BaseASTVisitor {
     //CONST
     private static final String RUNTIME_PACKAGE = "com/lolcode/runtime/";
 
     private class Class {
         public static final String LOLARRAY = RUNTIME_PACKAGE + "LolArray";
         public static final String LOLBOOL = RUNTIME_PACKAGE + "LolBool";
         public static final String LOLDOUBLE = RUNTIME_PACKAGE + "LolDouble";
         public static final String LOLINT = RUNTIME_PACKAGE + "LolInt";
         public static final String LOLOBJECT = RUNTIME_PACKAGE + "LolObject";
         public static final String LOLRTBADCOMPAREEXCEPTION = RUNTIME_PACKAGE + "LolRtBadCompareException";
         public static final String LOLRTBINOPWRONGCODE = RUNTIME_PACKAGE + "LolRtBinOpWrongTypeException";
         public static final String LOLRTNOSUCHKEYEXCEPTION = RUNTIME_PACKAGE + "LolRtNoSuchKeyException";
         public static final String LOLRTUNDEFINEDTYPEEXCEPTION = RUNTIME_PACKAGE + "LolRtUndefinedTypeException";
         public static final String LOLRTUNSUPPORTEDOPEXCEPTION = RUNTIME_PACKAGE + "LolRtUnsupportedOpException";
         public static final String LOLRUNTIMEEXCEPTION = RUNTIME_PACKAGE + "LolRuntimeException";
         public static final String LOLSTDLIB = RUNTIME_PACKAGE + "LolStdLib";
         public static final String LOLSTRING = RUNTIME_PACKAGE + "LolString";
         public static final String LOLTYPE = RUNTIME_PACKAGE + "LolType";
     }
 
     private class Type {
         public static final String LOLARRAY = "L"+Class.LOLARRAY+";";
         public static final String LOLBOOL = "L"+Class.LOLBOOL+";";
         public static final String LOLDOUBLE = "L"+Class.LOLDOUBLE+";";
         public static final String LOLINT = "L"+Class.LOLINT+";";
         public static final String LOLOBJECT = "L"+Class.LOLOBJECT+";";
         public static final String LOLSTRING = "L"+Class.LOLSTRING+";";
         public static final String LOLSTDLIB = "L"+Class.LOLSTDLIB+";";
     }
 
     //FIELDS
 
     private MethodVisitor mv;
     private ClassWriter cw;
     private String fileName;
     private HashMap<String,Integer> localVars;
     private enum Context {FUNCTION, LOOP, CASE, NONE};
     private Context ctx = Context.NONE;
     private Stack<Label> labels = new Stack<>();
 
     //CONSTRUCTOR
     public CodeGenerator() {
         super();
     }
 
     private void writeFile(ClassWriter cw, String className) {
         try {
             byte[] code = cw.toByteArray();
             FileOutputStream fos = new FileOutputStream(className + ".class");
             fos.write(code);
             fos.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public Object visit(TreeFunction func) throws BaseAstException {
         Context oldCtx = ctx;
         ctx = Context.FUNCTION;
         //get signature
         localVars = new HashMap<>();
         StringBuilder signatureBuilder = new StringBuilder("(");
         for (TreeFunctionParameter param : func.getParams()) {
             localVars.put(param.getName(),localVars.size()+1);
             //signature.append(param.getType())
             signatureBuilder.append(Type.LOLOBJECT);
         }
         signatureBuilder.append(")"+Type.LOLOBJECT);
         String signature = signatureBuilder.toString();
 
         mv = cw.visitMethod(Opcodes.ACC_PUBLIC,"_lol_"+func.getName(),signature,null,null);
         //clear local vars numbering
 
         //go into body
        boolean topLevelReturn = false;
         for (TreeNode node : func.getBody()){
             node.accept(this);

            if (node.getClass().equals(TreeReturnStmt.class) || node.getClass().equals(TreeBreakStmt.class)) {
                topLevelReturn = true;
            }
        }

        if (!topLevelReturn) {
            visit(new TreeBreakStmt());
         }
 
         mv.visitMaxs(0,0);
         mv.visitEnd();
         ctx = oldCtx;
         return null;
     }
 
     @Override
     public Object visit(TreeModule module) throws BaseAstException {
         cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
         fileName = new File(module.getModuleName()).getName();
         fileName = fileName.substring(0,fileName.lastIndexOf('.'));
         cw.visit(Opcodes.V1_6,Opcodes.ACC_PUBLIC,"com/lolcode/" + fileName,null,"java/lang/Object",null);
 
         //STDLIB FIELD
         cw.visitField(Opcodes.ACC_PUBLIC, "STDLIB", Type.LOLSTDLIB, null, null);
 
         //Constructor, create our object and init STDLIB Field
         mv = cw.visitMethod(Opcodes.ACC_PUBLIC,"<init>","()V",null, null);
         mv.visitVarInsn(Opcodes.ALOAD, 0);
         mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
         mv.visitVarInsn(Opcodes.ALOAD, 0);
         mv.visitTypeInsn(Opcodes.NEW, Class.LOLSTDLIB);
         mv.visitInsn(Opcodes.DUP);
         mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Class.LOLSTDLIB, "<init>", "()V");
         mv.visitFieldInsn(Opcodes.PUTFIELD, "com/lolcode/" + fileName, "STDLIB", Type.LOLSTDLIB);
         mv.visitInsn(Opcodes.RETURN);
         mv.visitMaxs(0, 0);
         mv.visitEnd();
 
         //visit functions?
         for (TreeFunction func : module.getFunctions()) {
             visit(func);
         }
 
         //static main -> program entry point
         //mv = cw.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,"main","([Ljava/lang/String;)V",null, null);
         /*mv.visitFieldInsn(Opcodes.GETSTATIC,
                 "java/lang/System",
                 "out",
                 "Ljava/io/PrintStream;");
         mv.visitLdcInsn("hello");
         mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                 "java/io/PrintStream",
                 "println",
                 "(Ljava/lang/String;)V");
         */
         /*
         //try printing using STDLIB
         //get new Arrays()
         mv.visitTypeInsn(Opcodes.NEW,"com/lolcode/"+fileName);
         mv.visitInsn(Opcodes.DUP);
         mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "com/lolcode/" + fileName, "<init>", "()V");
         //get Arrays.STDLIB
         mv.visitFieldInsn(Opcodes.GETFIELD,"com/lolcode/"+fileName,"STDLIB",Type.LOLSTDLIB);
         //get new LolString("hell")
         mv.visitTypeInsn(Opcodes.NEW, Class.LOLSTRING);
         mv.visitInsn(Opcodes.DUP);
         mv.visitLdcInsn("hell");
         mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Class.LOLSTRING, "<init>", "(Ljava/lang/String;)V");
         //get print()
         mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,Class.LOLSTDLIB,"print","("+Type.LOLOBJECT+")V");
         */
         //try readline + print
 
         //static main -> calls mainbody
         mv = cw.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,"main","([Ljava/lang/String;)V",null, null);
         mv.visitTypeInsn(Opcodes.NEW, "com/lolcode/" + fileName);
         mv.visitInsn(Opcodes.DUP);
         mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "com/lolcode/" + fileName, "<init>", "()V");
         mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,"com/lolcode/"+fileName,"mainbody","()V");
         mv.visitInsn(Opcodes.RETURN);
         mv.visitMaxs(0, 0);
         mv.visitEnd();
 
         //create "mainbody"
         mv = cw.visitMethod(Opcodes.ACC_PUBLIC,"mainbody","()V",null,null);
         localVars = new HashMap<>();
         for (TreeNode node : module.getBody()) {
             node.accept(this);
         }
         mv.visitInsn(Opcodes.RETURN);
         mv.visitMaxs(0, 0);
         mv.visitEnd(); 
         cw.visitEnd();
 
         PrintWriter pw = new PrintWriter(System.out);
         System.out.println(cw.toString());
         CheckClassAdapter.verify(new ClassReader(cw.toByteArray()), true, pw);
         writeFile(cw, "generated/com/lolcode/" + fileName);
         return null;
     }
 
     @Override
     public Object visit(TreeFunctionParameter param) throws BaseAstException {
         //System.out.println("PARAM");
         //put
         mv.visitVarInsn(Opcodes.ALOAD,localVars.get(param.getName()));
         //create local variable with name?
         return null;
     }
 
     @Override
     public Object visit(TreeIfStmt ifStmt) throws BaseAstException {
         Label elseBlock = new Label();
         boolean elseIf = true;
         if (ifStmt.endLabel == null) {
             elseIf = false;
             ifStmt.endLabel = new Label();
         }
         ifStmt.getCondition().accept(this);
         mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Class.LOLOBJECT, "toInt", "()I");
         mv.visitJumpInsn(Opcodes.IFEQ,elseBlock);
         for (TreeNode node : ifStmt.getTrueBranch()) {
             node.accept(this);
         }
         mv.visitJumpInsn(Opcodes.GOTO,ifStmt.endLabel);
         mv.visitLabel(elseBlock);
         for (TreeIfStmt node : ifStmt.getElseIfs()) {
             node.endLabel = ifStmt.endLabel;
             node.accept(this);
         }
 
         for (TreeNode node : ifStmt.getFalseBranch()) {
             node.accept(this);
         }
         if (!elseIf) {
             mv.visitLabel(ifStmt.endLabel);
         }
         return null;
     }
 
     @Override
     public Object visit(TreeLoopStmt loopStmt) throws BaseAstException {
         Context oldCtx = ctx;
         ctx = Context.LOOP;
 
         Label loopStart = new Label();
         Label loopEnd = new Label();
         labels.push(loopEnd);
         mv.visitLabel(loopStart);
         for (TreeNode node : loopStmt.getBody()) {
             node.accept(this);
         }
         switch (loopStmt.getoType()) {
             case UPPIN:
                 mv.visitTypeInsn(Opcodes.NEW,Class.LOLINT);
                 mv.visitInsn(Opcodes.DUP);
                 mv.visitLdcInsn(1);
                 mv.visitMethodInsn(Opcodes.INVOKESPECIAL,Class.LOLINT,"<init>","(I)V");
                 loopStmt.getVariable().accept(this);
                 mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,Class.LOLOBJECT,"add","("+Type.LOLOBJECT+")"+Type.LOLOBJECT);
                 mv.visitVarInsn(Opcodes.ASTORE,localVars.get(loopStmt.getVariable().getName()));
                 break;
             case NERFIN:
                 mv.visitTypeInsn(Opcodes.NEW,Class.LOLINT);
                 mv.visitInsn(Opcodes.DUP);
                 mv.visitLdcInsn(-1);
                 mv.visitMethodInsn(Opcodes.INVOKESPECIAL,Class.LOLINT,"<init>","(I)V");
                 loopStmt.getVariable().accept(this);
                 mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,Class.LOLOBJECT,"add","("+Type.LOLOBJECT+")"+Type.LOLOBJECT);
                 mv.visitVarInsn(Opcodes.ASTORE,localVars.get(loopStmt.getVariable().getName()));
                 break;
             case EMPTY:
                 break;
         }
         switch (loopStmt.getlType()) {
             case TIL:
                 loopStmt.getExitCondition().accept(this);
                 mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Class.LOLOBJECT,"toInt","()I");
                 mv.visitJumpInsn(Opcodes.IFNE,loopEnd);
                 break;
             case WHILE:
                 loopStmt.getExitCondition().accept(this);
                 mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Class.LOLOBJECT,"toInt","()I");
                 mv.visitJumpInsn(Opcodes.IFEQ,loopEnd);
                 break;
             case EMPTY:
                 break;
         }
         mv.visitJumpInsn(Opcodes.GOTO,loopStart);
         mv.visitLabel(loopEnd);
         labels.pop();
         ctx = oldCtx;
         return null;
     }
 
     @Override
     public Object visit(TreeAssignStmt assignStmt) throws BaseAstException {
         assignStmt.getRhs().accept(this);
         mv.visitVarInsn(Opcodes.ASTORE,localVars.get(assignStmt.getLhs().getName()));
         return null;
     }
 
     @Override
     public Object visit(TreeCaseStmt caseStmt) throws BaseAstException {
         Context oldCtx = ctx;
         ctx = Context.CASE;
         Label caseEnd = new Label();
         labels.push(caseEnd);
         Label nextCase, nextCaseAfterCheck;
         boolean first = true;
         nextCase = new Label();
         nextCaseAfterCheck = new Label();
 
         //visit 1 statement, to LolObject.EQ and cast it to bool
 
         for (Map.Entry<TreeConstant, List<TreeStatement>> entry : caseStmt.getBody().entrySet()) {
 
 
 
             //put case var
             caseStmt.getVal().accept(this);
 
             //put constant
             entry.getKey().accept(this);
             mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,Class.LOLOBJECT,"eq","("+Type.LOLOBJECT+")"+Type.LOLOBJECT);
             mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,Class.LOLOBJECT,"toInt","()I");
             mv.visitJumpInsn(Opcodes.IFEQ,nextCase);
             if (!first) {
                 mv.visitLabel(nextCaseAfterCheck);
             }
             //gtfo statement
             for (TreeNode node : entry.getValue()) {
                 node.accept(this);
             }
 
 
             if (!first) {
                 nextCaseAfterCheck = new Label();
             } else {
                 first = false;
             }
             mv.visitJumpInsn(Opcodes.GOTO,nextCaseAfterCheck);
 
 
 
             mv.visitLabel(nextCase);
             nextCase = new Label();
 
         }
 
         //defaultbranch
         for (TreeNode node : caseStmt.getDefaultBranch()) {
             node.accept(this);
         }
         mv.visitLabel(caseEnd);
         labels.pop();
         ctx = oldCtx;
         return null;
     }
 
     @Override
     public Object visit(TreeVarDeclStmt varDeclStmt) throws BaseAstException {
         if (varDeclStmt.getInitialValue() == null) {
             mv.visitTypeInsn(Opcodes.NEW, Class.LOLOBJECT);
             mv.visitInsn(Opcodes.DUP);
             mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Class.LOLOBJECT, "<init>", "()V");
         } else {
         varDeclStmt.getInitialValue().accept(this);
         }
         localVars.put(varDeclStmt.getVar().getName(),localVars.size()+1);
         mv.visitVarInsn(Opcodes.ASTORE,localVars.size());
         return null;
     }
 
     @Override
     public Object visit(TreeArrayDeclStmt arrayDeclStmt) throws BaseAstException {
         mv.visitTypeInsn(Opcodes.NEW,Class.LOLARRAY);
         mv.visitInsn(Opcodes.DUP);
         mv.visitMethodInsn(Opcodes.INVOKESPECIAL,Class.LOLARRAY,"<init>","()V");
         localVars.put(arrayDeclStmt.getArray().getName(),localVars.size()+1);
         mv.visitVarInsn(Opcodes.ASTORE,localVars.size());
         return null;
     }
 
     @Override
     public Object visit(TreeVisibleStmt visibleStmt) throws BaseAstException {
         //put argument on stack
 
         //call print
         mv.visitVarInsn(Opcodes.ALOAD, 0);
         mv.visitFieldInsn(Opcodes.GETFIELD, "com/lolcode/" + fileName, "STDLIB", Type.LOLSTDLIB);
 
         visibleStmt.getArgument().accept(this);
 
         mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,Class.LOLSTDLIB,"print","("+Type.LOLOBJECT+")V");
 
         return null;
     }
 
     @Override
     public Object visit(TreeGimmehStmt gimmehStmt) throws BaseAstException {
         //put on stack result of STDLIB.read
         mv.visitVarInsn(Opcodes.ALOAD, 0);
         mv.visitFieldInsn(Opcodes.GETFIELD,"com/lolcode/"+fileName,"STDLIB",Type.LOLSTDLIB);
         mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,Class.LOLSTDLIB,"read","()"+Type.LOLOBJECT);
         mv.visitVarInsn(Opcodes.ASTORE,localVars.get(gimmehStmt.getVariable().getName()));
         return null;
     }
 
     @Override
     public Object visit(TreeFuncCallExpr funcCallStmt) throws BaseAstException {
         mv.visitVarInsn(Opcodes.ALOAD,0);
         //put arguments on stack
         for (TreeNode node : funcCallStmt.getArguments()) {
             node.accept(this);
         }
         //get siignature
 
         StringBuilder signatureBuilder = new StringBuilder("(");
         for (TreeFunctionParameter param : funcCallStmt.getBoundFunction().getParams()) {
             //signature.append(param.getType())
             signatureBuilder.append(Type.LOLOBJECT);
         }
         signatureBuilder.append(")"+Type.LOLOBJECT);
         String signature = signatureBuilder.toString();
 
         mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,"com/lolcode/"+fileName,"_lol_"+funcCallStmt.getFuncName(),signature);
         return null;
     }
 
     @Override
     public Object visit(TreeDummyStmt dummyStmt) throws BaseAstException {
         dummyStmt.getBody().accept(this);
         return null;
     }
 
     @Override
     public Object visit(TreeBreakStmt breakStmt) throws BaseAstException {
         //System.out.println("BRK");
         switch (ctx) {
             case FUNCTION:
                 mv.visitTypeInsn(Opcodes.NEW,Class.LOLOBJECT);
                 mv.visitInsn(Opcodes.DUP);
                 mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Class.LOLOBJECT, "<init>", "()V");
                 mv.visitInsn(Opcodes.ARETURN);
                 break;
             case LOOP:
             case CASE:
                 mv.visitJumpInsn(Opcodes.GOTO,labels.peek());
                 break;
         }
 
         return null;
     }
 
     @Override
     public Object visit(TreeReturnStmt returnStmt) throws BaseAstException {
         System.out.println("ReT");
         returnStmt.getRetValue().accept(this);
         mv.visitInsn(Opcodes.ARETURN);
         return null;
     }
 
     @Override
     public Object visit(TreeVariable variable) throws BaseAstException {
         //put variable on stack from local
         mv.visitVarInsn(Opcodes.ALOAD,localVars.get(variable.getName()));
         return null;
     }
 
     @Override
     public Object visit(TreeConstant constant) throws BaseAstException {
         //System.out.println("Constant");
         switch (constant.getType()) {
             case BOOL:
                 //put new LOLBOOL on stack
                 mv.visitTypeInsn(Opcodes.NEW, Class.LOLBOOL);
                 mv.visitInsn(Opcodes.DUP);
                 mv.visitLdcInsn((constant.getRealValue().equals("WIN")) ? true : false);
                 mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Class.LOLBOOL, "<init>", "(Z)V");
                 break;
             case STRING:
                 mv.visitTypeInsn(Opcodes.NEW, Class.LOLSTRING);
                 mv.visitInsn(Opcodes.DUP);
                 mv.visitLdcInsn(constant.getRealValue());
                 mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Class.LOLSTRING, "<init>", "(Ljava/lang/String;)V");
                 break;
             case INT:
                 mv.visitTypeInsn(Opcodes.NEW, Class.LOLINT);
                 mv.visitInsn(Opcodes.DUP);
                 mv.visitLdcInsn(Integer.parseInt(constant.getRealValue()));
                 mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Class.LOLINT, "<init>", "(I)V");
                 break;
             case FLOAT:
                 mv.visitTypeInsn(Opcodes.NEW, Class.LOLDOUBLE);
                 mv.visitInsn(Opcodes.DUP);
                 mv.visitLdcInsn(Double.parseDouble(constant.getRealValue()));
                 mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Class.LOLDOUBLE, "<init>", "(D)V");
                 break;
         }
         return null;
     }
 
     @Override
     public Object visit(TreeSumExpr sumExpr) throws BaseAstException {
         //put left on stack, then put right on stack, then left.add
         sumExpr.getLhs().accept(this);
         sumExpr.getRhs().accept(this);
         mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,Class.LOLOBJECT,"add","("+Type.LOLOBJECT+")" + Type.LOLOBJECT);
 
         return null;
     }
 
     @Override
     public Object visit(TreeSubExpr subExpr) throws BaseAstException {
         subExpr.getLhs().accept(this);
         subExpr.getRhs().accept(this);
         mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,Class.LOLOBJECT,"sub","("+Type.LOLOBJECT+")" + Type.LOLOBJECT);
         return null;
     }
 
     @Override
     public Object visit(TreeMulExpr mulExpr) throws BaseAstException {
         mulExpr.getLhs().accept(this);
         mulExpr.getRhs().accept(this);
         mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,Class.LOLOBJECT,"mul","("+Type.LOLOBJECT+")" + Type.LOLOBJECT);
         return null;
     }
 
     @Override
     public Object visit(TreeDivExpr divExpr) throws BaseAstException {
         divExpr.getLhs().accept(this);
         divExpr.getRhs().accept(this);
         mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,Class.LOLOBJECT,"div","("+Type.LOLOBJECT+")" + Type.LOLOBJECT);
         return null;
     }
 
     @Override
     public Object visit(TreeModExpr modExpr) throws BaseAstException {
         modExpr.getLhs().accept(this);
         modExpr.getRhs().accept(this);
         mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,Class.LOLOBJECT,"sub","("+Type.LOLOBJECT+")" + Type.LOLOBJECT);
         return null;
     }
 
     @Override
     public Object visit(TreeArrayPutStmt arrayPutExpr) throws BaseAstException {
         //put array
         mv.visitVarInsn(Opcodes.ALOAD,localVars.get(arrayPutExpr.getArray().getName()));
         //put value
         arrayPutExpr.getValue().accept(this);
         //put key
         arrayPutExpr.getKey().accept(this);
 
         //invoke
         mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Class.LOLOBJECT, "put", "(" + Type.LOLOBJECT + Type.LOLOBJECT + ")V");
         return null;
     }
 
     @Override
     public Object visit(TreeArrayGetExpr arrayGetExpr) throws BaseAstException {
         //put array
         mv.visitVarInsn(Opcodes.ALOAD,localVars.get(arrayGetExpr.getArray().getName()));
         //put key
         arrayGetExpr.getKey().accept(this);
         //invoke
         mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Class.LOLOBJECT, "get", "(" + Type.LOLOBJECT + ")" + Type.LOLOBJECT);
         return null;
     }
 
     @Override
     public Object visit(TreeMaxExpr maxExpr) throws BaseAstException {
         maxExpr.getLhs().accept(this);
         maxExpr.getRhs().accept(this);
         mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,Class.LOLOBJECT,"max","("+Type.LOLOBJECT+")" + Type.LOLOBJECT);
         return null;
     }
 
     @Override
     public Object visit(TreeMinExpr minExpr) throws BaseAstException {
         minExpr.getLhs().accept(this);
         minExpr.getRhs().accept(this);
         mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,Class.LOLOBJECT,"min","("+Type.LOLOBJECT+")" + Type.LOLOBJECT);
         return null;
     }
 
     @Override
     public Object visit(TreeAndExpr andExpr) throws BaseAstException {
         andExpr.getLhs().accept(this);
         andExpr.getRhs().accept(this);
         mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,Class.LOLOBJECT,"and","("+Type.LOLOBJECT+")" + Type.LOLOBJECT);
         return null;
     }
 
     @Override
     public Object visit(TreeOrExpr orExpr) throws BaseAstException {
         orExpr.getLhs().accept(this);
         orExpr.getRhs().accept(this);
         mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,Class.LOLOBJECT,"or","("+Type.LOLOBJECT+")" + Type.LOLOBJECT);
         return null;
     }
 
     @Override
     public Object visit(TreeXorExpr xorExpr) throws BaseAstException {
         xorExpr.getLhs().accept(this);
         xorExpr.getRhs().accept(this);
         mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,Class.LOLOBJECT,"xor","("+Type.LOLOBJECT+")" + Type.LOLOBJECT);
         return null;
     }
 
     @Override
     public Object visit(TreeNotExpr notExpr) throws BaseAstException {
         notExpr.getExpr().accept(this);
         mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,Class.LOLOBJECT,"not","("+Type.LOLOBJECT+")" + Type.LOLOBJECT);
         return null;
     }
 
     @Override
     public Object visit(TreeEqualExpr equalExpr) throws BaseAstException {
         equalExpr.getLhs().accept(this);
         equalExpr.getRhs().accept(this);
         mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,Class.LOLOBJECT,"eq","("+Type.LOLOBJECT+")" + Type.LOLOBJECT);
         return null;
     }
 
     @Override
     public Object visit(TreeNequalExpr nequalExpr) throws BaseAstException {
         nequalExpr.getLhs().accept(this);
         nequalExpr.getRhs().accept(this);
         mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,Class.LOLOBJECT,"neq","("+Type.LOLOBJECT+")" + Type.LOLOBJECT);
         return null;
     }
 }
