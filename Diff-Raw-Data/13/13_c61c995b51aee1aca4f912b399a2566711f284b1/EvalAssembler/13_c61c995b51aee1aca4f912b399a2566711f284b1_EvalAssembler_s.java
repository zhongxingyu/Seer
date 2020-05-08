 package org.libj.xquery.compiler;
 
 import org.libj.xquery.lexer.TokenType;
 import org.libj.xquery.lisp.Cons;
 import org.libj.xquery.namespace.*;
 import org.libj.xquery.parser.*;
 import org.libj.xquery.runtime.Range;
 import org.libj.xquery.xml.XML;
 import org.objectweb.asm.Label;
 import org.objectweb.asm.MethodVisitor;
 import org.objectweb.asm.Opcodes;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import static org.libj.xquery.lexer.TokenType.*;
 import static org.libj.xquery.compiler.Constants.*;
 
 public class EvalAssembler implements Opcodes {
     private String compiledClassName;
     private Namespace namespace;
     private MethodVisitor mv;
     private int locals;
     private Map<String, Symbol> freeVariables;
     private String[] vars;
     private Map<String, Class> externals;
     private boolean generateInnerClasses;
     private List<ClassInfo> innerClasses = new ArrayList<ClassInfo>();
     private boolean hasCallback;
 
     public int LOCAL_CALLBACK_INDEX = _LOCAL_CALLBACK_INDEX;
     public int LOCAL_ENV_INDEX = _LOCAL_ENV_INDEX;
 
     public EvalAssembler(MethodVisitor mv, String compiledClassName, String[] vars, Map<String, Class> externals, Namespace namespace,
                          boolean generateInnerClasses, boolean hasCallback) {
         this.compiledClassName = compiledClassName;
         this.vars = vars;
         this.externals = externals;
         this.namespace = namespace;
         this.mv = mv;
         this.generateInnerClasses = generateInnerClasses;
         this.hasCallback = hasCallback;
         if (!hasCallback) {
             LOCAL_CALLBACK_INDEX--;
             LOCAL_ENV_INDEX--;
         }
     }
 
     public Class visit(Cons ast) {
         Analysis walker = new Analysis(ast, vars, externals, namespace, hasCallback);
         ast = walker.walk();
         locals = walker.getLocals();
         freeVariables = walker.getFreeVariables();
         visitFreeVariables();
         return visitResult(ast);
     }
 
     private void visitFreeVariables() {
         for (Symbol sym: freeVariables.values()) {
             String varName = sym.getName();
             int varIndex = sym.getIndex();
             mv.visitVarInsn(ALOAD, LOCAL_ENV_INDEX);
             mv.visitLdcInsn(varName);
             mv.visitMethodInsn(INVOKEVIRTUAL, ENVIRONMENT, "getVariable", "(Ljava/lang/String;)Ljava/lang/Object;");
             Caster.cast(mv, Object.class, sym.getType());
             mv.visitVarInsn(ASTORE, varIndex);
         }
     }
 
     private Class visitResult(Cons expr) {
         return hasCallback ? visitResultWithCallback(expr) : visitResultWithoutCallback(expr);
     }
 
     private Class visitResultWithoutCallback(Cons expr) {
         switch (AST.getNodeType(expr)) {
             case FLOWER:
                 return visitFlower(expr, -1, -1);
             default:
                 Class t = visitExpr(expr);
                 Caster.cast(mv, t, Object.class);
                 return Object.class;
         }
     }
 
     private Class visitResultWithCallback(Cons expr) {
         switch (AST.getNodeType(expr)) {
             case FLOWER:
                 return visitFlower(expr, LOCAL_CALLBACK_INDEX, -1, true);
             default:
                 mv.visitVarInsn(ALOAD, LOCAL_CALLBACK_INDEX);
                 Class t = visitExpr(expr);
                 pushToCallback(t);
                 return Void.class;
         }
     }
 
     Class visitExpr(Cons expr) {
         switch (AST.getNodeType(expr)) {
             case FLOWER:
                 return visitFlower(expr);
             case FLOWERAT:
                 return visitFlowerAt(expr);
             case IF:
                 return visitIf(expr);
             case LIST:
                 return visitList(expr);
             case CALL:
                 return visitCall(expr);
             case PLUS: case MINUS: case MULTIPLY: case DIV: case NEGATIVE: case MOD:
             case EQ: case NE:
             case LT: case LE: case GT: case GE:
             case AND: case OR:
             case TO: case INDEX: case XPATH: case ATTR_AT:
                 return visitOp(expr);
             case VARIABLE:
                 return visitVariable(expr);
             case STRING:
                 mv.visitLdcInsn(expr.second());
                 return String.class;
             case NUMBER:
                 return visitNumber(expr);
             case CAST:
                 return visitCast(expr);
             case ELEMENT:
                 return visitElement(expr);
             default:
                 throw new RuntimeException("Not Implemented: "+toTypeName(AST.getNodeType(expr)));
         }
     }
 
     private Class visitNumber(Cons expr) {
         Object v = expr.second();
         if (v instanceof Integer) {
             pushConst(((Integer) v).intValue());
             return int.class;
         }
         else if (v instanceof Double) {
             pushConst(((Double) v).doubleValue());
             return double.class;
         }
         else {
             throw new RuntimeException("Not Implemented!");
         }
     }
 
     private Class visitVariable(Cons expr) {
         VariableElement var = (VariableElement) expr.first();
         Class t = var.getType();
         if (t.isPrimitive()) {
             if (t == int.class) {
                 mv.visitVarInsn(ILOAD, var.getRef());
             }
             else if (t == double.class) {
                 mv.visitVarInsn(DLOAD, var.getRef());
             }
             else if (t == long.class) {
                 mv.visitVarInsn(LLOAD, var.getRef());
             }
             else {
                 throw new RuntimeException("Not Implemented!");
             }
         }
         else {
             mv.visitVarInsn(ALOAD, var.getRef());
         }
         return var.getType();
     }
 
 
     private Class visitOp(Cons expr) {
         switch (AST.getNodeType(expr)) {
             case PLUS: case MINUS: case MULTIPLY: case DIV: case MOD:
                 return visitBinaryArithmetic(expr);
             case NEGATIVE:
                 return visitNegative(expr);
             case EQ: case NE:
             case LT: case LE: case GT: case GE:
                 return visitCompare(expr);
             case AND:
                 return visitAnd(expr);
             case OR:
                 return visitOr(expr);
             case TO:
                 return visitRange(expr);
             case INDEX:
                 return visitIndex(expr);
             case XPATH:
                 return visitXPath(expr);
             case ATTR_AT:
                 return visitAttrAt(expr);
             default:
                 throw new RuntimeException("Not Implemented! "+toTypeName(AST.getNodeType(expr)));
         }
     }
 
     private Class visitBinaryArithmetic(Cons expr) {
         visitExpr(AST.nthAST(expr, 1));
         Class t = visitExpr(AST.nthAST(expr, 2));
         if (t == int.class) {
             switch (AST.getNodeType(expr)) {
                 case PLUS:
                     mv.visitInsn(IADD);
                     break;
                 case MINUS:
                     mv.visitInsn(ISUB);
                     break;
                 case MULTIPLY:
                     mv.visitInsn(IMUL);
                     break;
                 case DIV:
                     mv.visitInsn(IDIV);
                     break;
                 case MOD:
                     mv.visitInsn(IREM);
                     break;
                 default:
                     throw new RuntimeException("Not Implemented!");
             }
             return t;
         }
         else if (t == double.class) {
             switch (AST.getNodeType(expr)) {
                 case PLUS:
                     mv.visitInsn(DADD);
                     break;
                 case MINUS:
                     mv.visitInsn(DSUB);
                     break;
                 case MULTIPLY:
                     mv.visitInsn(DMUL);
                     break;
                 case DIV:
                     mv.visitInsn(DDIV);
                     break;
                 case MOD:
                     mv.visitInsn(DREM);
                     break;
                 default:
                     throw new RuntimeException("Not Implemented!");
             }
             return t;
         }
         else if (t == long.class) {
             switch (AST.getNodeType(expr)) {
                 case PLUS:
                     mv.visitInsn(LADD);
                     break;
                 case MINUS:
                     mv.visitInsn(LSUB);
                     break;
                 case MULTIPLY:
                     mv.visitInsn(LMUL);
                     break;
                 case DIV:
                     mv.visitInsn(LDIV);
                     break;
                 case MOD:
                     mv.visitInsn(LREM);
                     break;
                 default:
                     throw new RuntimeException("Not Implemented!");
             }
             return t;
         }
         else if (!t.isPrimitive()) {
             String op;
             switch (AST.getNodeType(expr)) {
                 case PLUS:
                     op = "add";
                     break;
                 case MINUS:
                     op = "subtract";
                     break;
                 case MULTIPLY:
                     op = "multiply";
                     break;
                 case DIV:
                     op = "div";
                     break;
                 case MOD:
                     op = "mod";
                     break;
                 default:
                     throw new RuntimeException("Not Implemented!");
             }
             mv.visitMethodInsn(INVOKESTATIC, OP, op, "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
             return Object.class;
         }
         else {
             throw new RuntimeException("Not Implemented! "+t);
         }
     }
 
     private Class visitNegative(Cons expr) {
         Class t = visitExpr(AST.nthAST(expr, 1));
         if (t == int.class) {
             mv.visitInsn(INEG);
             return t;
         }
         else if (t == double.class) {
             mv.visitInsn(DNEG);
             return t;
         }
         else {
             throw new RuntimeException("Not Implemented!");
         }
     }
 
     private Class visitCompare(Cons expr) {
         Cons left = AST.nthAST(expr, 1);
         Cons right = AST.nthAST(expr, 2);
         if (AST.getNodeType(expr) == EQ || AST.getNodeType(expr) == NE) {
             if (AST.getEvalType(left) == String.class && AST.getEvalType(right) == String.class) {
                 if (left.first() instanceof ConstantElement || right.first() instanceof ConstantElement) {
                     return visitConstantStringCompare(AST.getNodeType(expr), left, right);
                 }
             }
         }
         visitExpr(left);
         Class t = visitExpr(right);
         if (t == int.class) {
             Label trueLabel = new Label();
             Label falseLabel = new Label();
             Label doneLabel = new Label();
             int instruction;
             switch (AST.getNodeType(expr)) {
                 case EQ:
                     instruction = IF_ICMPEQ;
                     break;
                 case NE:
                     instruction = IF_ICMPNE;
                     break;
                 case LT:
                     instruction = IF_ICMPLT;
                     break;
                 case LE:
                     instruction = IF_ICMPLE;
                     break;
                 case GT:
                     instruction = IF_ICMPGT;
                     break;
                 case GE:
                     instruction = IF_ICMPGE;
                     break;
                 default:
                     throw new RuntimeException("Not Implemented!");
             }
             mv.visitJumpInsn(instruction, trueLabel);
             mv.visitLabel(falseLabel);
             mv.visitInsn(ICONST_0);
             mv.visitJumpInsn(GOTO, doneLabel);
             mv.visitLabel(trueLabel);
             mv.visitInsn(ICONST_1);
             mv.visitLabel(doneLabel);
             return boolean.class;
         }
         else if (t == double.class || t == long.class) {
             Label trueLabel = new Label();
             Label falseLabel = new Label();
             Label doneLabel = new Label();
             int instruction;
             switch (AST.getNodeType(expr)) {
                 case EQ:
                     instruction = IFEQ;
                     break;
                 case NE:
                     instruction = IFNE;
                     break;
                 case LT:
                     instruction = IFLT;
                     break;
                 case LE:
                     instruction = IFLE;
                     break;
                 case GT:
                     instruction = IFGT;
                     break;
                 case GE:
                     instruction = IFGE;
                     break;
                 default:
                     throw new RuntimeException("Not Implemented!");
             }
             mv.visitInsn(t == double.class ? DCMPL : LCMP);
             mv.visitJumpInsn(instruction, trueLabel);
             mv.visitLabel(falseLabel);
             mv.visitInsn(ICONST_0);
             mv.visitJumpInsn(GOTO, doneLabel);
             mv.visitLabel(trueLabel);
             mv.visitInsn(ICONST_1);
             mv.visitLabel(doneLabel);
             return boolean.class;
         }
         else if (!t.isPrimitive()) {
             String op;
             switch (AST.getNodeType(expr)) {
                 case EQ:
                     op = "eq";
                     break;
                 case NE:
                     op = "ne";
                     break;
                 case LT:
                     op = "lt";
                     break;
                 case LE:
                     op = "le";
                     break;
                 case GT:
                     op = "gt";
                     break;
                 case GE:
                     op = "ge";
                     break;
                 default:
                     throw new RuntimeException("Not Implemented!");
             }
             mv.visitMethodInsn(INVOKESTATIC, OP, op, "(Ljava/lang/Object;Ljava/lang/Object;)Z");
             return boolean.class;
         }
         else {
             throw new RuntimeException("Not Implemented!");
         }
     }
 
     private Class<Boolean> visitConstantStringCompare(TokenType nodeType, Cons left, Cons right) {
         if (left.first() instanceof ConstantElement) {
             visitExpr(left);
             visitExpr(right);
         }
         else {
             visitExpr(right);
             visitExpr(left);
         }
         mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z");
         switch (nodeType) {
             case EQ:
                 break;
             case NE:
                 Label trueLabel = new Label();
                 mv.visitJumpInsn(IFEQ, trueLabel);
                 mv.visitInsn(ICONST_0);
                 Label endLabel = new Label();
                 mv.visitJumpInsn(GOTO, endLabel);
                 mv.visitLabel(trueLabel);
                 mv.visitInsn(ICONST_1);
                 mv.visitLabel(endLabel);
                 break;
             default:
                 throw new RuntimeException("Must be a bug!");
         }
         return boolean.class;
     }
 
     private Class visitAnd(Cons expr) {
         visitExpr(AST.nthAST(expr, 1));
         Label falseLabel = new Label();
         Label endLabel = new Label();
         mv.visitJumpInsn(IFEQ, falseLabel);
         visitExpr(AST.nthAST(expr, 2));
         mv.visitJumpInsn(GOTO, endLabel);
         mv.visitLabel(falseLabel);
         pushConst(false);
         mv.visitLabel(endLabel);
         return boolean.class;
     }
 
     private Class visitOr(Cons expr) {
         visitExpr(AST.nthAST(expr, 1));
         Label trueLabel = new Label();
         Label endLabel = new Label();
         mv.visitJumpInsn(IFNE, trueLabel);
         visitExpr(AST.nthAST(expr, 2));
         mv.visitJumpInsn(GOTO, endLabel);
         mv.visitLabel(trueLabel);
         pushConst(true);
         mv.visitLabel(endLabel);
         return boolean.class;
     }
 
 
     private Class visitRange(Cons expr) {
         Class<Range> rangeClass = Range.class;
         String rangeClassName = rangeClass.getName().replace('.', '/');
         mv.visitTypeInsn(NEW, rangeClassName);
         mv.visitInsn(DUP);
         visitExpr(AST.nthAST(expr, 1));
         visitExpr(AST.nthAST(expr, 2));
         mv.visitInsn(ICONST_1);
         mv.visitInsn(IADD);
         mv.visitMethodInsn(INVOKESPECIAL, rangeClassName, "<init>", "(II)V");
         return rangeClass;
     }
 
     private Class visitIndex(Cons expr) {
         Cons list = AST.nthAST(expr, 1);
         Cons at = AST.nthAST(expr, 2);
         visitExpr(list);
         visitExpr(at);
         mv.visitMethodInsn(INVOKESTATIC, OP, "elementAt", "(Ljava/lang/Object;I)Ljava/lang/Object;");
         return Object.class;
     }
 
     private Class visitList(Cons expr) {
         if (expr.size() == 1) {
             mv.visitInsn(ACONST_NULL);
             return Void.class;
         }
         Class t = newList();
         for (Object e: Cons.rest(expr)) {
             mv.visitInsn(DUP);
             visitExpr((Cons) e);
             pushToList();
         }
         return t;
     }
 
     private Class visitIf(Cons expr) {
         visitExpr(AST.nthAST(expr, 1));
         Label thenLabel = new Label();
         Label elseLabel = new Label();
         Label endLabel = new Label();
         mv.visitJumpInsn(IFEQ, elseLabel);
         // then
         mv.visitLabel(thenLabel);
         visitExpr(AST.nthAST(expr, 2));
         mv.visitJumpInsn(GOTO, endLabel);
         // else
         mv.visitLabel(elseLabel);
         visitExpr(AST.nthAST(expr, 3));
 
         mv.visitLabel(endLabel);
         return AST.getEvalType(expr);
     }
 
 
     private Class visitFlower(Cons expr) {
         return visitFlower(expr, -1, -1);
     }
 
     private Class visitFlowerAt(Cons expr) {
         visitExpr(AST.nthAST(expr, 2));
         int index = defineAnonymous();
         mv.visitVarInsn(ISTORE, index);
         return visitFlower(AST.nthAST(expr, 1), -1, index);
     }
 
     private Class visitFlower(Cons expr, int result, int lookingForElementAt) {
         return visitFlower(expr, result, lookingForElementAt, false);
     }
 
     private boolean isPureLets(Cons forlets) {
         for (Object x: forlets) {
             Cons forlet = (Cons) x;
             if (AST.getNodeType(forlet) != LET) {
                 return false;
             }
         }
         return true;
     }
 
     private Class visitFlower(Cons expr, int result, int lookingForElementAt, boolean callback) {
         if (callback && result < 0) {
             throw new RuntimeException("Not Implemented!");
         }
         Label breakLabel = null;
         if (lookingForElementAt > 0) {
             breakLabel = new Label();
         }
 
         Cons forlets = AST.nthAST(expr, 1);
 
         if (result < 0) {
             if (!isPureLets(forlets)) {
                 newList();
                 result = defineAnonymous();
                 mv.visitVarInsn(ASTORE, result);
             }
         }
 
         Cons body = expr.next().next();
 
         Class t = visitForLets(forlets, body, result, lookingForElementAt, breakLabel, callback);
 
         if (lookingForElementAt > 0) {
             mv.visitLabel(breakLabel);
         }
         if (result < 0) {
             if (!callback) {
                 return t;
             }
             else {
                 // pop
                 throw new RuntimeException("Not Implemented!");
             }
         }
         else {
             if (!callback) {
                 mv.visitVarInsn(ALOAD, result);
                 return LIST_INTERFACE_CLASS;
             }
             else {
                 return null;
             }
         }
     }
 
     private Class visitForLets(Cons forlets, Cons body, int result, int lookingForElementAt, Label breakLabel, boolean callback) {
         if (forlets == null || Cons.isNil(forlets)) {
             return visitFlowerWhereBody(body, result, lookingForElementAt, breakLabel, callback);
         }
         else {
             switch (((Cons)forlets.first()).size()) {
                 case 3:
                     return visitForLetsNoWhere(forlets, body, result, lookingForElementAt, breakLabel, callback);
                 case 4:
                     Cons where = (Cons) ((Cons) forlets.first()).nth(3);
                     visitCondition(where);
                     Label endif = new Label();
                     Label elseLabel = new Label();
                     if (result < 0) {
                         mv.visitJumpInsn(IFEQ, elseLabel);
                         Class t = visitForLetsNoWhere(forlets, body, result, lookingForElementAt, breakLabel, callback);
                         Caster.cast(mv, t, Object.class);
                         mv.visitJumpInsn(GOTO, endif);
                         mv.visitLabel(elseLabel);
                         mv.visitInsn(ACONST_NULL);
                         mv.visitLabel(endif);
                         return Object.class;
                     }
                     else {
                         mv.visitJumpInsn(IFEQ, endif);
                         visitForLetsNoWhere(forlets, body, result, lookingForElementAt, breakLabel, callback);
                         mv.visitLabel(endif);
                         return null;
                     }
                 default:
                     throw new RuntimeException("Not Implemented!");
             }
         }
     }
 
     private Class visitForLetsNoWhere(Cons forlets, Cons body, int result, int lookingForElementAt, Label breakLabel, boolean callback) {
         switch (AST.getNodeType(((Cons) forlets.first()))) {
             case FOR:
                 visitForGeneral(forlets, body, result, lookingForElementAt, breakLabel, callback);
                 return null;
             case FORRANGE:
                 visitForRange(forlets, body, result, lookingForElementAt, breakLabel, callback);
                 return null;
             case LET:
                 return visitLet(forlets, body, result, lookingForElementAt, breakLabel, callback);
             default:
                 throw new RuntimeException("Not Implemented!");
         }
     }
 
     private Class visitFlowerWhereBody(Cons expr, int result, int lookingForElementAt, Label breakLabel, boolean callback) {
         Cons body = AST.nthAST(expr, 0);
         Cons where = AST.nthAST(expr, 1);
         // loop body
         if (where != null && !Cons.isNil(where)) {
             visitCondition(where);
             Label elseLabel = new Label();
             Label endif = new Label();
             if (result < 0) {
                 mv.visitJumpInsn(IFEQ, elseLabel);
                 // if body
                 Class t = visitFlowerBody(body, result, lookingForElementAt, breakLabel, callback);
                 Caster.cast(mv, t, Object.class);
                 mv.visitJumpInsn(GOTO, endif);
                 // else
                 mv.visitLabel(elseLabel);
                 mv.visitInsn(ACONST_NULL);
                 // end if
                 mv.visitLabel(endif);
                 return Object.class;
             }
             else {
                 mv.visitJumpInsn(IFEQ, endif);
                 // if body
                 visitFlowerBody(body, result, lookingForElementAt, breakLabel, callback);
                 // end if
                 mv.visitLabel(endif);
                 return null;
             }
         } else {
             return visitFlowerBody(body, result, lookingForElementAt, breakLabel, callback);
         }
     }
 
     private Class visitFlowerBody(Cons expr, int result, int lookingForElementAt, Label breakLabel, boolean callback) {
         if (lookingForElementAt <= 0) {
             return visitFlowerBody(expr, result, callback);
         }
         else {
             if (result < 0) {
                 // result = -1 only happens to pure lets, and pure lets should not have this for..at...
                 throw new RuntimeException("Not Implemented!");
             }
             visitFlowerBodyAt(expr, result, lookingForElementAt, breakLabel);
             return null;
         }
     }
 
     private Class visitFlowerBody(Cons body, int result, boolean callback) {
         if (callback) {
             if (result < 0) {
                 throw new RuntimeException("must be a bug");
             }
             mv.visitVarInsn(ALOAD, result);
             Class t = visitExpr(body);
             pushToCallback(t);
             return null;
         }
         else if (result < 0) {
             return visitExpr(body);
         }
         else {
             mv.visitVarInsn(ALOAD, result);
             Class elementType = visitExpr(body);
             Caster.cast(mv, elementType, Object.class);
             pushToList();
             return null;
         }
     }
 
     private void visitFlowerBodyAt(Cons body, int result, int lookingForElementAt, Label breakLabel) {
         mv.visitVarInsn(ILOAD, lookingForElementAt);
         mv.visitJumpInsn(IFLE, breakLabel);
         Class elementType = visitExpr(body);
         if (elementType.isPrimitive()) {
             Label continueLabel = new Label();
             mv.visitIincInsn(lookingForElementAt, -1);
             mv.visitVarInsn(ILOAD, lookingForElementAt);
             mv.visitJumpInsn(IFGT, continueLabel);
             // we are done
             Caster.cast(mv, int.class, Object.class); // XXX: what?
             mv.visitVarInsn(ASTORE, result);
             mv.visitJumpInsn(GOTO, breakLabel);
             // try again
             mv.visitLabel(continueLabel);
             mv.visitInsn(elementType==double.class||elementType==long.class?POP2:POP);
         }
         else {
             Label continueLabel = new Label();
             mv.visitInsn(DUP);
             mv.visitMethodInsn(INVOKESTATIC, OP, "sizeOf", "(Ljava/lang/Object;)I");
             mv.visitInsn(DUP);
             mv.visitVarInsn(ILOAD, lookingForElementAt);
             mv.visitJumpInsn(IF_ICMPLT, continueLabel);
             // we are done
             mv.visitInsn(POP);
             mv.visitVarInsn(ILOAD, lookingForElementAt);
             mv.visitMethodInsn(INVOKESTATIC, OP, "elementAt", "(Ljava/lang/Object;I)Ljava/lang/Object;");
             mv.visitVarInsn(ASTORE, result);
             mv.visitJumpInsn(GOTO, breakLabel);
             // try again
             mv.visitLabel(continueLabel);
             mv.visitVarInsn(ILOAD, lookingForElementAt);
             mv.visitInsn(SWAP);
             mv.visitInsn(ISUB);
             mv.visitInsn(SWAP);
             mv.visitInsn(POP);
             mv.visitVarInsn(ISTORE, lookingForElementAt);
         }
     }
 
     private void visitCondition(Cons where) {
         // TODO: remove this. the cast should be done in walker
         Class t = visitExpr(where);
         if (t.isPrimitive()) {
             if (t == boolean.class) {
                 // already boolean, do nothing
             }
             else if (t == int.class) {
                 Label trueLabel = new Label();
                 Label falseLabel = new Label();
                 Label doneLabel = new Label();
                 mv.visitJumpInsn(IFNE, trueLabel);
                 mv.visitLabel(falseLabel);
                 mv.visitInsn(ICONST_0);
                 mv.visitJumpInsn(GOTO, doneLabel);
                 mv.visitLabel(trueLabel);
                 mv.visitInsn(ICONST_1);
                 mv.visitLabel(doneLabel);
             }
             else {
                 throw new RuntimeException("Not Implemented! "+t);
             }
         }
         else {
             mv.visitMethodInsn(INVOKESTATIC, OP, "asBool", "(Ljava/lang/Object;)Z");
         }
     }
 
     private void visitForRange(Cons forlets, Cons body, int result, int lookingForElementAt, Label breakLabel, boolean callback) {
         Cons expr = (Cons) forlets.first();
         VariableElement variable = (VariableElement) expr.second();
 
         Cons range = (Cons) expr.third();
         Cons from = (Cons) range.first();
         Cons to = (Cons) range.second();
 
         visitExpr(from);
 
         int i = variable.getRef();
         mv.visitVarInsn(ISTORE, i);
 
 
         int maxVar = ((Element)to.first()).getTokenType() == NUMBER ? -1 : defineAnonymous();
 
         if (maxVar != -1) {
             visitExpr(to);
             mv.visitVarInsn(ISTORE, maxVar);
         }
 
         Label condition = new Label();
         Label loop = new Label();
         mv.visitJumpInsn(GOTO, condition);
 
         // do
         mv.visitLabel(loop);
         visitForLets(forlets.next(), body, result, lookingForElementAt, breakLabel, callback);
 
         // i++
         mv.visitIincInsn(i, 1);
 
         // if i < max?
         mv.visitLabel(condition);
         mv.visitVarInsn(ILOAD, i);
         if (maxVar != -1) {
             mv.visitVarInsn(ILOAD, maxVar);
         }
         else {
             visitExpr(to);
         }
         mv.visitJumpInsn(IF_ICMPLE, loop);
     }
 
     private void visitForGeneral(Cons forlets, Cons body, int result, int index, Label breakLabel, boolean callback) {
         Cons expr = (Cons) forlets.first();
         VariableElement variable = (VariableElement) expr.second();
         Cons varExpr = AST.nthAST(expr, 2);
 
         int iterator = defineAnonymous();
         int element = variable.getRef();
         Class collectionType = visitExpr(varExpr);
         Caster.cast(mv, collectionType, Object.class);
         mv.visitMethodInsn(INVOKESTATIC, OP, "asList", "(Ljava/lang/Object;)Ljava/lang/Iterable;");
         mv.visitMethodInsn(INVOKEINTERFACE, "java/lang/Iterable", "iterator", "()Ljava/util/Iterator;");
         mv.visitVarInsn(ASTORE, iterator);
 
         Label condition = new Label();
         Label loop = new Label();
         mv.visitJumpInsn(GOTO, condition);
 
         // loop iteration
         mv.visitLabel(loop);
         mv.visitVarInsn(ALOAD, iterator);
         mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;");
         mv.visitVarInsn(ASTORE, element);
 
         visitForLets(forlets.next(), body, result, index, breakLabel, callback);
 
         // loop condition
         mv.visitLabel(condition);
         mv.visitVarInsn(ALOAD, iterator);
         mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z");
         mv.visitJumpInsn(IFNE, loop);
     }
 
 
 
     private Class visitLet(Cons forlets, Cons body, int result, int lookingForElementAt, Label breakLabel, boolean callback) {
         Cons expr = (Cons) forlets.first();
         VariableElement variable = (VariableElement) expr.second();
         Cons varExpr = AST.nthAST(expr, 2);
 
         Class varType = visitExpr(varExpr);
         int index = variable.getRef();
         if (varType.isPrimitive()) {
             if (varType == int.class) {
                 mv.visitVarInsn(ISTORE, index);
             }
             else if (varType == double.class) {
                 mv.visitVarInsn(DSTORE, index);
             }
             else if (varType == long.class) {
                 mv.visitVarInsn(LSTORE, index);
             }
             else {
                 throw new RuntimeException("Not Implemented!");
             }
         }
         else {
             mv.visitVarInsn(ASTORE, index);
         }
 
         return visitForLets(forlets.next(), body, result, lookingForElementAt, breakLabel, callback);
 
     }
 
 
 
 
     private Class visitCall(Cons expr) {
         Function fn = ((FunctionElement) expr.first()).getFunction();
         Cons arguments = Cons.rest(expr);
         if (fn instanceof JavaFunction) {
             visitJavaFunction((JavaFunction) fn, arguments);
             return ((JavaFunction) fn).getReturnType();
         }
         else {
             throw new RuntimeException("Not Implemented!");
         }
     }
 
     private void visitJavaFunction(JavaFunction fn, Cons arguments) {
         if (fn instanceof NormalConstructorFunction) {
             mv.visitTypeInsn(NEW, fn.getClassName());
             mv.visitInsn(DUP);
         }
         if (fn.isVarArgs()) {
             Class[] parameterTypes = fn.getParameterTypes();
             int parameterSize = parameterTypes.length;
             int argumentSize = arguments.size();
             int normalParamameterNumber = parameterSize - 1;
             int varParameterNumber = argumentSize - normalParamameterNumber;
             if (varParameterNumber < 0) {
                 throw new RuntimeException("Not Implemented!");
             }
             for (int i = 0; i < normalParamameterNumber; i++) {
                 visitExpr(AST.nthAST(arguments, i));
             }
             Class elementType = parameterTypes[normalParamameterNumber];
             if (!elementType.isArray()) {
                 throw new RuntimeException("Not Implemented!");
             }
             elementType = elementType.getComponentType();
             newArray(elementType, varParameterNumber);
             for (int i = 0; i < varParameterNumber; i++) {
                 mv.visitInsn(DUP);
                 pushConst(i);
                 visitExpr(AST.nthAST(arguments, normalParamameterNumber + i));
                 pushToArray(elementType);
             }
         }
         else {
             for (Object arg: arguments) {
                 visitExpr((Cons) arg);
             }
         }
         if (fn instanceof NormalStaticFunction) {
             mv.visitMethodInsn(INVOKESTATIC, fn.getClassName(), fn.getFunctionName(), fn.getSignature());
         }
         else if (fn instanceof NormalMethodFunction) {
             mv.visitMethodInsn(INVOKEVIRTUAL, fn.getClassName(), fn.getFunctionName(), fn.getSignature());
         }
         else if (fn instanceof NormalConstructorFunction) {
             mv.visitMethodInsn(INVOKESPECIAL, fn.getClassName(), fn.getFunctionName(), fn.getSignature());
         }
         else {
             throw new RuntimeException("Not Implemented!");
         }
     }
 
     private Class visitElement(Cons expr) {
         if (generateInnerClasses) {
             StructuredXMLAssembler x = new StructuredXMLAssembler(this, mv, compiledClassName);
             x.visitElement(expr);
             innerClasses.addAll(x.getClasses());
         } else {
             new StringXMLAssembler(this, mv, compiledClassName).visitElement(expr);
         }
         return XML.class;
     }
 
     private Class visitXPath(Cons expr) {
         visitExpr(AST.nthAST(expr, 1));
         String path = (String) expr.third();
         String ns = (String) expr.nth(3);
         if (ns == null || ns.isEmpty()) {
             mv.visitInsn(ACONST_NULL);
         }
         else {
             pushConst(ns);
         }
         pushConst(path);
         mv.visitMethodInsn(INVOKEINTERFACE, XML_INTERFACE, "getElementsByTagNameNS", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;");
         return AST.getEvalType(expr);
     }
 
     private Class visitAttrAt(Cons expr) {
         visitExpr(AST.nthAST(expr, 1));
         String attr = (String) expr.third();
         pushConst(attr);
         mv.visitMethodInsn(INVOKEINTERFACE, XML_INTERFACE, "getAttribute", "(Ljava/lang/String;)Ljava/lang/String;");
         return AST.getEvalType(expr);
     }
 
 
     private Class visitCast(Cons expr) {
         CastElement element = (CastElement) expr.first();
         visitExpr((Cons) expr.second());
         Class source = element.source;
         Class target = element.target;
         return Caster.cast(mv, source, target);
 //        if (source == int.class && target == double.class) {
 //            throw new RuntimeException("Not Implemented!");
 //        }
 //        else {
 //            throw new RuntimeException("Not Implemented! " + source + " to " + target);
 //        }
     }
 
 
     //////////////////////////////////////////////////
     /// helper
     //////////////////////////////////////////////////
 
     private void pushConst(int n) {
         switch (n) {
             case -1:
                 mv.visitInsn(ICONST_M1);
                 return;
             case 0:
                 mv.visitInsn(ICONST_0);
                 return;
             case 1:
                 mv.visitInsn(ICONST_1);
                 return;
             case 2:
                 mv.visitInsn(ICONST_2);
                 return;
             case 3:
                 mv.visitInsn(ICONST_3);
                 return;
             case 4:
                 mv.visitInsn(ICONST_4);
                 return;
             case 5:
                 mv.visitInsn(ICONST_5);
                 return;
         }
         if (-0x80 <= n && n <= 0x7f) {
             mv.visitIntInsn(BIPUSH, n);
         }
         else if (-0x8000 <= n && n <= 0x7fff) {
             mv.visitIntInsn(SIPUSH, n);
         }
         else {
             mv.visitLdcInsn(n);
         }
     }
 
     private void pushConst(boolean b) {
         mv.visitInsn(b ? ICONST_1 : ICONST_0);
     }
 
     private void pushConst(Object o) {
         mv.visitLdcInsn(o);
     }
 
     private void newObject(String className) {
         mv.visitTypeInsn(NEW, className);
         mv.visitInsn(DUP);
         mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "()V");
     }
 
     private void newArray(Class type, int length) {
         pushConst(length);
         if (type.isPrimitive()) {
             throw new RuntimeException("Not Implemented!");
         }
         else {
             mv.visitTypeInsn(ANEWARRAY, type.getName().replace('.', '/'));
         }
     }
 
     private void pushToArray(Class type) {
         if (type.isPrimitive()) {
             throw new RuntimeException("Not Implemented!");
         }
         else {
             mv.visitInsn(AASTORE);
         }
     }
 
     private Class newList() {
         newObject(LIST_IMPLEMENTATION);
         return LIST_INTERFACE_CLASS;
     }
 
     private void pushToList() {
         mv.visitMethodInsn(INVOKEINTERFACE, MUTABLE_LIST, "add", "(Ljava/lang/Object;)V");
     }
 
     private void pushToCallback(Class t) {
         if (t == null || t == Void.class) {
            // do nothing
         }
         else if (t.isPrimitive()) {
             Caster.cast(mv, t, Object.class);
             mv.visitMethodInsn(INVOKEINTERFACE, CALLBACK, "call", "(Ljava/lang/Object;)V");
         }
         else if (t != Object.class && !Iterable.class.isAssignableFrom(t)) {
             // note: this is not a safe test!
             mv.visitMethodInsn(INVOKEINTERFACE, CALLBACK, "call", "(Ljava/lang/Object;)V");
         }
         else {
             // TODO: run more check
             mv.visitMethodInsn(INVOKESTATIC, OP, "addToCallback", "(L"+CALLBACK+";Ljava/lang/Object;)V");
         }
     }
 
     private int defineAnonymous() {
         return locals++;
     }
 
     public Map<String, Symbol> getFreeVariables() {
         return freeVariables;
     }
 
     public List<ClassInfo> getInnerClasses() {
         return innerClasses;
     }
 }
