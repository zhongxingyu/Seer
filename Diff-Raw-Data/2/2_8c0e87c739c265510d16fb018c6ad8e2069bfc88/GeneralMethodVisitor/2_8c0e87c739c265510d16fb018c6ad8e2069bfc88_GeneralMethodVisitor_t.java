 package com.sdc.languages.general.visitors;
 
 import com.sdc.ast.ExpressionType;
 import com.sdc.ast.Type;
 import com.sdc.ast.controlflow.*;
 import com.sdc.ast.expressions.*;
 import com.sdc.ast.expressions.InstanceInvocation;
 import com.sdc.ast.expressions.Invocation;
 import com.sdc.ast.expressions.New;
 import com.sdc.ast.expressions.identifiers.Field;
 import com.sdc.ast.expressions.identifiers.Identifier;
 import com.sdc.ast.expressions.identifiers.Variable;
 import com.sdc.cfg.nodes.DoWhile;
 import com.sdc.cfg.nodes.Node;
 import com.sdc.cfg.nodes.Switch;
 import com.sdc.languages.general.astUtils.Frame;
 import com.sdc.languages.general.languageParts.Annotation;
 import com.sdc.languages.general.languageParts.LanguagePartFactory;
 import com.sdc.languages.general.languageParts.Method;
 import com.sdc.languages.general.ConstructionBuilder;
 import com.sdc.util.DeclarationWorker;
 import com.sdc.util.DominatorTreeGenerator;
 import org.objectweb.asm.*;
 import org.objectweb.asm.util.Printer;
 
 import java.util.*;
 
 import static com.sdc.ast.ExpressionType.*;
 import static com.sdc.ast.expressions.IntConstant.*;
 import static org.objectweb.asm.Opcodes.ASM4;
 
 public abstract class GeneralMethodVisitor extends MethodVisitor {
     protected Method myDecompiledMethod;
 
     protected final String myDecompiledOwnerFullClassName;
     protected final String myDecompiledOwnerSuperClassName;
 
     protected Stack<Expression> myBodyStack = new Stack<Expression>();
     protected List<Statement> myStatements = new ArrayList<Statement>();
 
     protected List<Node> myNodes = new ArrayList<Node>();
     protected List<Label> myLabels = new ArrayList<Label>();
     protected Map<Label, List<Integer>> myGoToMap = new HashMap<Label, List<Integer>>();  // for GOTO
     protected Map<Integer, Label> myIfElseMap = new HashMap<Integer, Label>(); // for IF ELSE Branch
     protected List<Label> myNodeInnerLabels = new ArrayList<Label>();
 
     protected boolean myHasDebugInformation = false;
 
     protected String myClassFilesJarPath = "";
 
     protected LanguagePartFactory myLanguagePartFactory;
     protected GeneralVisitorFactory myVisitorFactory;
 
     protected DeclarationWorker.SupportedLanguage myLanguage;
 
     public GeneralMethodVisitor(final Method method, final String decompiledOwnerFullClassName, final String decompiledOwnerSuperClassName) {
         super(ASM4);
         this.myDecompiledMethod = method;
         this.myDecompiledOwnerFullClassName = decompiledOwnerFullClassName;
         this.myDecompiledOwnerSuperClassName = decompiledOwnerSuperClassName;
     }
 
     protected abstract boolean checkForAutomaticallyGeneratedAnnotation(final String annotationName);
 
     protected Frame getCurrentFrame() {
         return myDecompiledMethod.getCurrentFrame();
     }
 
     public Method getDecompiledMethod() {
         return myDecompiledMethod;
     }
 
     public void setClassFilesJarPath(final String classFilesJarPath) {
         this.myClassFilesJarPath = classFilesJarPath;
     }
 
     public String getDecompiledOwnerFullClassName() {
         return myDecompiledOwnerFullClassName;
     }
 
     public String getDecompiledOwnerSuperClassName() {
         return myDecompiledOwnerSuperClassName;
     }
 
     protected AnnotationVisitor visitAnnotation(final int parameter, final String desc, final boolean visible) {
         List<String> annotationsImports = new ArrayList<String>();
         final String annotationName = getDescriptor(desc, 0, annotationsImports);
         if (!checkForAutomaticallyGeneratedAnnotation(annotationName)) {
             Annotation annotation = myLanguagePartFactory.createAnnotation();
             annotation.setName(annotationName);
             if (parameter == -1) {
                 myDecompiledMethod.appendAnnotation(annotation);
             } else {
                 myDecompiledMethod.appendParameterAnnotation(parameter, annotation);
             }
             myDecompiledMethod.getImports().addAll(annotationsImports);
             return myVisitorFactory.createAnnotationVisitor(annotation);
         } else {
             return null;
         }
     }
 
     @Override
     public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
         return visitAnnotation(-1, desc, visible);
     }
 
     @Override
     public void visitAttribute(final Attribute attr) {
     }
 
     @Override
     public AnnotationVisitor visitAnnotationDefault() {
         return null;
     }
 
     @Override
     public AnnotationVisitor visitParameterAnnotation(final int parameter, final String desc, final boolean visible) {
         return visitAnnotation(parameter, desc, visible);
     }
 
     @Override
     public void visitCode() {
     }
 
     @Override
     public void visitFrame(final int type, final int nLocal, final Object[] local, final int nStack, final Object[] stack) {
         Frame currentFrame = getCurrentFrame();
 
         Frame newFrame = null;
 
         if (type == 0) {
             // F_FULL
             newFrame = currentFrame.createNextFrameWithAbsoluteBound(nLocal);
         } else if (type == 1) {
             // F_APPEND
             newFrame = currentFrame.createNextFrameWithRelativeBound(nLocal);
         } else if (type == 2) {
             // F_CHOP
             newFrame = currentFrame.createNextFrameWithRelativeBound(-nLocal);
         } else if (type == 3 || type == 4) {
             // F_SAME F_SAME1
             newFrame = currentFrame.createNextFrameWithRelativeBound(0);
         }
 
         myDecompiledMethod.addNewFrame(newFrame);
 
         if (nStack > 0) {
             String stackedVariableType;
 
             if (stack[0] instanceof Integer) {
                 stackedVariableType = DeclarationWorker.getDescriptorByInt((Integer) stack[0], myLanguage);
             } else {
                 final String className = (String) stack[0];
                 stackedVariableType = getDescriptor(className, 0, myDecompiledMethod.getImports()) + " ";
             }
 
             getCurrentFrame().setStackedVariableType(stackedVariableType);
         }
     }
 
     @Override
     public void visitInsn(final int opcode) {
         int size = myBodyStack.size();
         final String opString = Printer.OPCODES[opcode];
         if (opString.contains("ADD") || opString.contains("SUB")
                 || opString.contains("MUL") || opString.contains("DIV") || opString.contains("REM")
                 || opString.contains("USHR") || opString.contains("SHL")
                 || opString.contains("XOR") || opString.contains("SHR")
                 || opString.contains("IOR") || opString.contains("LOR") || opString.contains("AND")) {
             Expression e1 = getTopOfBodyStack();
             Expression e2 = getTopOfBodyStack();
             String type = opString.substring(1);
             if (opString.contains("OR") || opString.contains("AND")) {
                 type = "BITWISE_" + type;
             }
             Expression res;
             res = new BinaryExpression(ExpressionType.valueOf(type), e2, e1);
             myBodyStack.push(res);
         } else if (opString.contains("NEG")) {
             Expression e = getTopOfBodyStack();
             myBodyStack.push(new UnaryExpression(NEGATE, e));
         } else if (opString.contains("CONST_M1")) {
             myBodyStack.push(M_ONE);
         } else if (opString.contains("CONST_NULL")) {
             myBodyStack.push(Constant.NULL);
         } else if (opString.contains("CONST_")) {
             if (opString.contains("ICONST_")) {
                 myBodyStack.push(new IntConstant(Integer.valueOf(opString.substring(7).toLowerCase())));
             } else {
                 String descriptor = DeclarationWorker.getJavaDescriptor(opString, 0, null);
                 myBodyStack.push(new Constant(opString.substring(7).toLowerCase(), false, new Type(descriptor)));
             }
         } else if (opString.equals("RETURN")) {
             replaceInvocationsFromExpressionsToStatements();
             Return returnStatement = new Return();
             returnStatement.setNeedToPrintReturn(!myDecompiledMethod.getDecompiledClass().isLambdaFunctionClass());
             myStatements.add(returnStatement);
         } else if (opString.contains("RETURN")) {
             Expression expression = replaceBooleanConstant(getTopOfBodyStack());
             replaceInvocationsFromExpressionsToStatements();
             Return returnStatement = new Return(expression);
             returnStatement.setNeedToPrintReturn(!myDecompiledMethod.getDecompiledClass().isLambdaFunctionClass());
             myStatements.add(returnStatement);
         } else if (opString.contains("CMP")) {
             Expression b = getTopOfBodyStack();
             Expression a = getTopOfBodyStack();
             myBodyStack.push(new TernaryExpression(
                     new BinaryExpression(EQ, a, b),
                     ZERO,
                     new TernaryExpression(new BinaryExpression(LT, a, b), M_ONE, ONE)
             ));
         } else if (opString.contains("ATHROW")) {
             myStatements.add(new Throw(getTopOfBodyStack()));
         } else if (opString.equals("SWAP")) {
             if (size < 2) return;
             Expression expr1 = myBodyStack.pop();
             Expression expr2 = myBodyStack.pop();
             if (expr1.hasDoubleLength() || expr2.hasDoubleLength()) {
                 //There is a wrong condition, we return elements of stack as they were there
                 multiPush(expr2, expr1);
             } else {
                 multiPush(expr1, expr2);
             }
         } else if (opString.equals("DUP") && !myBodyStack.isEmpty()) {
             Expression expr = myBodyStack.peek();
             if (expr.hasDoubleLength()) return;
             myBodyStack.push(expr);
         } else if (opString.equals("DUP_X1")) {
             if (size < 2) return;
             Expression expr1 = myBodyStack.pop();
             Expression expr2 = myBodyStack.pop();
             if (expr1.hasDoubleLength() || expr2.hasDoubleLength()) {
                 //There is a wrong condition, we return elements of stack as they were there
                 multiPush(expr2, expr1);
                 return;
             }
             multiPush(expr1, expr2, expr1);
         } else if (opString.equals("DUP_X2")) {
             if (size < 2) return;
             Expression expr1 = myBodyStack.pop();
             Expression expr2 = myBodyStack.pop();
             if (expr1.hasDoubleLength()) {
                 //There is a wrong condition, we return elements of stack as they were there
                 multiPush(expr2, expr1);
                 return;
             }
             if (expr2.hasDoubleLength()) {
                 //Form 2
                 multiPush(expr1, expr2, expr1);
             } else {
                 if (size < 3 || myBodyStack.peek().hasDoubleLength()) {
                     //There is a wrong condition, we return elements of stack as they were there
                     multiPush(expr2, expr1);
                     return;
                 }
                 Expression expr3 = myBodyStack.pop();
                 //Form 1
                 multiPush(expr1, expr3, expr2, expr1);
             }
         } else if (opString.equals("DUP2")) {
             if (size < 1) return;
             Expression expr1 = myBodyStack.pop();
             if (expr1.hasDoubleLength()) {
                 //Form 2
                 multiPush(expr1, expr1);
             } else {
                 if (size < 2 || myBodyStack.peek().hasDoubleLength()) {
                     //There is a wrong condition, we return elements of stack as they were there
                     multiPush(expr1);
                     return;
                 }
                 Expression expr2 = myBodyStack.pop();
                 //Form 1
                 multiPush(expr2, expr1, expr2, expr1);
             }
         } else if (opString.equals("DUP2_X1")) {
             if (size < 2) return;
             Expression expr1 = myBodyStack.pop();
             Expression expr2 = myBodyStack.pop();
             if (expr1.hasDoubleLength() && !expr2.hasDoubleLength()) {
                 //Form 2
                 multiPush(expr1, expr2, expr1);
             } else {
                 if (size < 3 || myBodyStack.peek().hasDoubleLength()
                         || expr1.hasDoubleLength() || expr2.hasDoubleLength()) {
                     //There is a wrong condition, we return elements of stack as they were there
                     multiPush(expr2, expr1);
                     return;
                 }
                 Expression expr3 = myBodyStack.pop();
                 //Form 1
                 multiPush(expr2, expr1, expr3, expr2, expr1);
             }
         } else if (opString.equals("DUP2_X2")) {
             if (size < 2) return;
             Expression expr1 = myBodyStack.pop();
             Expression expr2 = myBodyStack.pop();
             if (!expr1.hasDoubleLength() && !expr2.hasDoubleLength()) {
                 if (size < 3) {
                     //There is a wrong condition, we return elements of stack as they were there
                     multiPush(expr2, expr1);
                     return;
                 }
                 Expression expr3 = myBodyStack.pop();
                 if (expr3.hasDoubleLength()) {
                     //Form 3
                     multiPush(expr2, expr1, expr3, expr2, expr1);
                     return;
                 }
                 if (size < 4 || myBodyStack.peek().hasDoubleLength()) {
                     //There is a wrong condition, we return elements of stack as they were there
                     multiPush(expr3, expr2, expr1);
                     return;
                 }
                 Expression expr4 = myBodyStack.pop();
                 //Form 1
                 multiPush(expr2, expr1, expr4, expr3, expr2, expr1);
             } else if (expr1.hasDoubleLength() && !expr2.hasDoubleLength()) {
                 if (size < 3 || myBodyStack.peek().hasDoubleLength()) {
                     //There is a wrong condition, we return elements of stack as they were there
                     multiPush(expr2, expr1);
                     return;
                 }
                 Expression expr3 = myBodyStack.pop();
                 //Form 2
                 multiPush(expr1, expr3, expr2, expr1);
             } else if (expr1.hasDoubleLength() && expr2.hasDoubleLength()) {
                 //Form 4
                 multiPush(expr1, expr2, expr1);
             }
         } else if (opString.equals("POP")) {
             if (size < 1 || myBodyStack.peek().hasDoubleLength()) {
                 return;
             }
 
             if (myBodyStack.peek() instanceof Invocation) {
                 myStatements.add(convertInvocationFromExpressionToStatement((Invocation) myBodyStack.pop()));
             } else if (myBodyStack.peek() instanceof New) {
                 myStatements.add(new com.sdc.ast.controlflow.New((New) myBodyStack.pop()));
             } else {
                 myBodyStack.pop();
             }
         } else if (opString.equals("POP2")) {
             if (size < 1) return;
             Expression expr1 = myBodyStack.pop();
             if (expr1.hasDoubleLength()) {
                 //Form 2
             } else {
                 if (size < 2 || myBodyStack.peek().hasDoubleLength()) {
                     //There is a wrong condition, we return elements of stack as they were there
                     multiPush(expr1);
                     return;
                 }
                 //Form 1
                 myBodyStack.pop();
             }
         } else if (opString.contains("ALOAD")) {
             final Expression arrayIndex = getTopOfBodyStack();
             Expression ref = getTopOfBodyStack();
             myBodyStack.push(new SquareBrackets(ref, arrayIndex));
         } else if (opString.contains("ASTORE")) {
             final Expression expr = getTopOfBodyStack();
             final Expression arrayIndex = getTopOfBodyStack();
             Expression ref = getTopOfBodyStack();
             if (ref instanceof NewArray) {
                 ((NewArray) ref).addNewInitializationValue(expr);
             } else {
                 myStatements.add(new Assignment(new SquareBrackets(ref, arrayIndex), expr));
             }
         } else if (opString.equals("NOP")) {
             //do nothing
         } else if ((opString.contains("I2L") || opString.contains("F2L") || opString.contains("D2L")) && !myBodyStack.empty()) {
             myBodyStack.push(new Cast(LONG_CAST, getTopOfBodyStack()));
         } else if ((opString.contains("I2D") || opString.contains("F2D") || opString.contains("L2D")) && !myBodyStack.isEmpty()) {
             myBodyStack.push(new Cast(DOUBLE_CAST, getTopOfBodyStack()));
         } else if ((opString.contains("I2F") || opString.contains("L2F") || opString.contains("D2F")) && !myBodyStack.isEmpty()) {
             myBodyStack.push(new Cast(FLOAT_CAST, getTopOfBodyStack()));
         } else if ((opString.contains("L2I") || opString.contains("F2I") || opString.contains("D2I")) && !myBodyStack.isEmpty()) {
             myBodyStack.push(new Cast(INT_CAST, getTopOfBodyStack()));
         } else if ((opString.contains("I2B")) && !myBodyStack.isEmpty()) {
             myBodyStack.push(new Cast(BYTE_CAST, getTopOfBodyStack()));
         } else if ((opString.contains("I2C")) && !myBodyStack.isEmpty()) {
             myBodyStack.push(new Cast(CHAR_CAST, getTopOfBodyStack()));
         } else if ((opString.contains("I2S")) && !myBodyStack.isEmpty()) {
             myBodyStack.push(new Cast(SHORT_CAST, getTopOfBodyStack()));
         } else if (opString.contains("ARRAYLENGTH")) {
             Expression e = getTopOfBodyStack();
             myBodyStack.push(new ArrayLength(e));
         }
         // All opcodes :
         //  +NOP, +ACONST_NULL, +ICONST_M1, +CONST_0, +ICONST_1, +ICONST_2, +ICONST_3, +ICONST_4, +ICONST_5,
         //  +LCONST_0, +LCONST_1, +FCONST_0, +FCONST_1, +FCONST_2, +DCONST_0, +DCONST_1, +IALOAD, +LALOAD, +FALOAD, +DALOAD,
         //  +AALOAD, +BALOAD, +CALOAD, +SALOAD, +IASTORE, +LASTORE, +FASTORE, +DASTORE, +AASTORE, +BASTORE, +CASTORE, +SASTORE,
         //  +POP, +POP2, +DUP, +DUP_X1, +DUP_X2, +DUP2, +DUP2_X1, +DUP2_X2, +SWAP, +IADD, +LADD, +FADD, +DADD, +ISUB, +LSUB, +FSUB,
         //  +DSUB, +IMUL, +LMUL, +FMUL, +DMUL, +IDIV, +LDIV, +FDIV, +DDIV, +IREM, +LREM, +FREM, +DREM, +INEG, +LNEG, +FNEG, +DNEG,
         //  +ISHL, +LSHL, +ISHR, +LSHR, +IUSHR, +LUSHR, +IAND, +LAND, +IOR, +LOR, +IXOR, +LXOR, +I2L, +I2F, +I2D, +L2I, +L2F, +L2D,
         //  +F2I, +F2L, +F2D, +D2I, +D2L, +D2F, +I2B, +I2C, +I2S, LCMP, FCMPL, FCMPG, DCMPL, DCMPG, +IRETURN, +LRETURN, +FRETURN,
         //  +DRETURN, +ARETURN, +RETURN, ARRAYLENGTH, +ATHROW, MONITORENTER, or MONITOREXIT.
     }
 
     @Override
     public void visitIntInsn(final int opcode, final int operand) {
         final String opString = Printer.OPCODES[opcode];
 
         if (opString.contains("IPUSH")) {
             myBodyStack.push(new IntConstant(operand));
         } else if (opString.contains("NEWARRAY")) {
             List<Expression> dimensions = new ArrayList<Expression>();
             dimensions.add(getTopOfBodyStack());
             myBodyStack.push(createNewArray(1, Printer.TYPES[operand].substring(2).toLowerCase(), dimensions));
         }
     }
 
     @Override
     public void visitVarInsn(final int opcode, final int var) {
         final String opString = Printer.OPCODES[opcode];
 
         final Frame currentFrame = getCurrentFrame();
         final boolean currentFrameHasStack = currentFrame.checkStack() && myBodyStack.isEmpty();
 
         Type variableType = null;
 
         if (opString.contains("LOAD")) {
             if (myStatements.isEmpty()) {
                 myBodyStack.push(currentFrame.getVariable(var));
             } else {
                 int lastStatementIndex = myStatements.size() - 1;
                 Statement lastStatement = myStatements.get(lastStatementIndex);
                 if (opString.contains("ILOAD") && lastStatement instanceof Increment
                         && ((Increment) lastStatement).getVariable().getIndex() == var) {
                     Increment increment = (Increment) lastStatement;
                     myStatements.remove(lastStatementIndex);
                     ExpressionType type = increment.getOperationType();
                     switch (type) {
                         case INC:
                             type = INC_REV;
                             break;
                         case DEC:
                             type = DEC_REV;
                             break;
                         default:
                     }
                     myBodyStack.push(new ExprIncrement(increment.getVariable(), increment.getIncrementExpression(), type));
                 } else {
                     myBodyStack.push(currentFrame.getVariable(var));
                 }
             }
         } else if (opString.contains("STORE") && !currentFrameHasStack) {
             Identifier v = currentFrame.getVariable(var);
             final Expression expr = getTopOfBodyStack();
             myStatements.add(new Assignment(v, expr));
             variableType = expr.getType();
             checkIncrements(var, expr);
         }
 
         if (!opString.contains("LOAD") && var > myDecompiledMethod.getLastLocalVariableIndex()) {
 
 
             String descriptorType;
             if (currentFrameHasStack) {
                 descriptorType = currentFrame.getStackedVariableType();
             } else {
                 descriptorType = getDescriptor(opString, 0, myDecompiledMethod.getImports());
             }
 
             if (!descriptorType.equals("Object ") && !descriptorType.equals("Any") || variableType == null) {
                 variableType = new Type(descriptorType);
             }
             Variable variable = currentFrame.getVariable(var);
             Constant name = null;
             if (variable.isUndefined()) {
                 name = new Constant(myDecompiledMethod.getNewTypeName(variableType), false, Type.VOID);
             }
             currentFrame.updateVariableInformation(var, variableType, name);
 
         }
     }
 
     private void checkIncrements(int var, Expression expr) {
         int lastIndex = myStatements.size() - 1;
         Expression expr2 = (myBodyStack.empty() ? null : myBodyStack.peek());
         if (expr instanceof BinaryExpression && ((BinaryExpression) expr).isIncrementCastableType()) {
             BinaryExpression binaryExpression = (BinaryExpression) expr;
             Expression left = binaryExpression.getLeft();
             Expression right = binaryExpression.getRight();
             ExpressionType type = binaryExpression.getExpressionType();
             if (left instanceof Variable && ((Variable) left).getIndex() == var) {
                 myStatements.remove(lastIndex);
                 if (expr.equals(expr2)) {
                     myBodyStack.pop();
                     myBodyStack.add(new ExprIncrement((Variable) left, right, type));
                 } else {
                     myStatements.add(new Increment((Variable) left, right, type));
                 }
             } else if (right instanceof Variable && ((Variable) right).getIndex() == var
                     && binaryExpression.isAssociative()) {
                 myStatements.remove(lastIndex);
                 if (expr.equals(expr2)) {
                     myBodyStack.pop();
                     myBodyStack.add(new ExprIncrement((Variable) right, left, type));
                 } else {
                     myStatements.add(new Increment((Variable) right, left, type));
                 }
             } else if (left instanceof ExprIncrement && ((ExprIncrement) left).getVariable().getIndex() == var) {
                 myStatements.remove(lastIndex);
                 myStatements.add(new Increment((ExprIncrement) left));
                 if (expr.equals(expr2)) {
                     myBodyStack.pop();
                     myBodyStack.add(new ExprIncrement(((ExprIncrement) left).getVariable(), right, type));
                 } else {
                     myStatements.add(new Increment(((ExprIncrement) left).getVariable(), right, type));
                 }
             } else if (right instanceof ExprIncrement && ((ExprIncrement) right).getVariable().getIndex() == var
                     && binaryExpression.isAssociative()) {
                 myStatements.remove(lastIndex);
                 myStatements.add(new Increment((ExprIncrement) right));
                 if (expr.equals(expr2)) {
                     myBodyStack.pop();
                     myBodyStack.add(new ExprIncrement(((ExprIncrement) right).getVariable(), left, type));
                 } else {
                     myStatements.add(new Increment(((ExprIncrement) right).getVariable(), left, type));
                 }
             }
         }
     }
 
     @Override
     public void visitTypeInsn(final int opcode, final String type) {
         final String opString = Printer.OPCODES[opcode];
         final boolean needToGetDescriptor = type.contains("[") || type.contains(";");
         final String actualType = needToGetDescriptor ? getDescriptor(type, 0, myDecompiledMethod.getImports()) : decompileClassNameWithOuterClasses(type);
 
         if (opString.contains("NEWARRAY")) {
             List<Expression> dimensions = new ArrayList<Expression>();
             dimensions.add(getTopOfBodyStack());
             myBodyStack.push(createNewArray(1, actualType, dimensions));
         } else if (opString.contains("INSTANCEOF")) {
             myBodyStack.push(new InstanceOf(new Type(actualType), getTopOfBodyStack()));
         } else if (opString.contains("CHECKCAST") && !myBodyStack.empty()) {
             myBodyStack.push(new Cast(CHECK_CAST, myBodyStack.pop(), actualType));
         } else if (opString.equals("NEW")) {
             myBodyStack.push(new New(null));
         }
     }
 
     @Override
     public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
         final String opString = Printer.OPCODES[opcode];
         final String fieldName = (myDecompiledMethod.getDecompiledClass().isLambdaFunctionClass() || myDecompiledMethod.getDecompiledClass().isNestedClass())
                 && name.startsWith("$") ? name.substring(1) : name;
         Field field = new Field(fieldName, new Type(getDescriptor(desc, 0, myDecompiledMethod.getImports())));
 
         Expression e = null;
         if (opString.contains("PUTFIELD") || opString.contains("PUTSTATIC")) {
             e = getTopOfBodyStack();
         }
 
         if (opString.contains("PUTFIELD") || opString.contains("GETFIELD")) {
             final Expression fieldOwner = getTopOfBodyStack();
             field.setOwner(fieldOwner);
         } else {
             final String fieldOwner = decompileClassNameWithOuterClasses(owner);
             field.setStaticOwnerName(fieldOwner);
         }
 
         if (opString.contains("PUTFIELD") || opString.contains("PUTSTATIC")) {
             if ((myDecompiledMethod.getName().equals("<clinit>") || myDecompiledOwnerFullClassName.endsWith(myDecompiledMethod.getName()))
                     && isInitializationValueCorrect(e) && !myDecompiledMethod.hasFieldInitializer(name))
             {
                 myDecompiledMethod.addInitializerToField(name, e);
             } else if (!name.startsWith("this$")) {
                 myStatements.add(new Assignment(field, e));
             }
         } else if (opString.contains("GETFIELD") || opString.contains("GETSTATIC")) {
             myBodyStack.push(field);
         }
     }
 
     private boolean isInitializationValueCorrect(Expression value) {
         //todo
         boolean res;
         List<Variable> params = myDecompiledMethod.getParameters();
         res = true;
         for (Variable v : params) {
             res = res && !value.findVariable(v);
         }
         //res = res && !value.hasNotStaticInvocations();
         return res;
     }
 
     @Override
     public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc) {
         final String opString = Printer.OPCODES[opcode];
 
         final String decompiledOwnerFullClassName = DeclarationWorker.decompileFullClassName(owner);
         final String ownerClassName = decompileClassNameWithOuterClasses(owner);
 
         List<Expression> arguments = getInvocationArguments(desc);
         String returnType = getInvocationReturnType(desc);
         final boolean hasVoidReturnType = hasVoidReturnType(desc);
         String invocationName = name;
 
         boolean isStaticInvocation = false;
 
         if (opString.contains("INVOKEVIRTUAL") || opString.contains("INVOKEINTERFACE")
                 || (!opString.contains("INVOKESTATIC") && decompiledOwnerFullClassName.equals(myDecompiledOwnerFullClassName) && !name.equals("<init>"))) {
             appendInstanceInvocation(name, hasVoidReturnType ? Type.VOID : new Type(returnType), arguments, getTopOfBodyStack());
             return;
         }
 
         if (opString.contains("INVOKESPECIAL")) {
             if (name.equals("<init>")) {
                 myDecompiledMethod.addImport(decompiledOwnerFullClassName);
                 invocationName = ownerClassName;
                 returnType = invocationName + " ";
             } else {
                 invocationName = "super." + name;
             }
         }
 
         if (opString.contains("INVOKESTATIC")) {
             myDecompiledMethod.addImport(decompiledOwnerFullClassName);
             invocationName = ownerClassName + "." + name;
             isStaticInvocation = true;
         }
 
         appendInvocationOrConstructor(isStaticInvocation, name, invocationName, hasVoidReturnType ? Type.VOID : new Type(returnType), arguments, decompiledOwnerFullClassName);
     }
 
     @Override
     public void visitInvokeDynamicInsn(final String name, final String desc, final Handle bsm, final Object... bsmArgs) {
     }
 
     @Override
     public void visitJumpInsn(final int opcode, final Label label) {
         final String opString = Printer.OPCODES[opcode];
         if (opString.contains("IF")) {
             final Label myLastIFLabel = label;
             if (myNodes.isEmpty() || !myNodeInnerLabels.isEmpty() || (myNodes.get(getLeftEmptyNodeIndex() - 1).getCondition() == null)) {
                 for (Node node : myNodes) {
                     if (!(node instanceof DoWhile) && node.getInnerLabels().contains(label) && myNodes.get(myNodes.size() - 1).getCondition() == null) {
                         myIfElseMap.put(myNodes.size(), label);
                         int index = node.getInnerLabels().indexOf(label);
                         DoWhile dw = new DoWhile(null, new ArrayList<Label>(myNodeInnerLabels), myNodes.size());
                         dw.setStatements(new ArrayList<Statement>(node.getStatements().subList(0, index)));
                         dw.getInnerLabels().addAll(new ArrayList<Label>(node.getInnerLabels().subList(0, index)));
                         dw.setCondition(getConditionFromStack(opString));
                         dw.setEmpty(true);
                         myNodes.add(dw);
 
                         node.setStatements(new ArrayList<Statement>(node.getStatements().subList(index, node.getStatements().size())));
                         node.setInnerLabels(new ArrayList<Label>(node.getInnerLabels().subList(index, node.getInnerLabels().size())));
                         myNodeInnerLabels.clear();
                         return;
                     }
                 }
                 if (myNodeInnerLabels.contains(label)) {
                     int index = myNodeInnerLabels.indexOf(label);
                     Node beforeNode = new Node(null, null, myNodes.size());
                     beforeNode.setStatements(new ArrayList<Statement>(myStatements.subList(0, index)));
                     beforeNode.setInnerLabels(new ArrayList<Label>(myNodeInnerLabels.subList(0, index)));
                     beforeNode.setEmpty(true);
                     myNodes.add(beforeNode);
 
                     Node innerNode = new Node(null, null, myNodes.size());
                     innerNode.setStatements(new ArrayList<Statement>(myStatements.subList(index, myStatements.size())));
                     innerNode.setInnerLabels(new ArrayList<Label>(myNodeInnerLabels.subList(index, myNodeInnerLabels.size())));
                     innerNode.setEmpty(true);
                     myNodes.add(innerNode);
 
                     myIfElseMap.put(myNodes.size(), label);
 
                     DoWhile dw = new DoWhile(new ArrayList<Statement>(), new ArrayList<Label>(), myNodes.size());
                     dw.setCondition(getConditionFromStack(opString));
                     dw.setEmpty(true);
                     myNodes.add(dw);
 
                     myStatements.clear();
                     myNodeInnerLabels.clear();
                     return;
                 }
                 myLabels.add(myLastIFLabel);
                 myIfElseMap.put(myNodes.size(), label);
                 applyNode();
                 final int last = myNodes.size() - 1;
                 myNodes.get(last).setCondition(getConditionFromStack(opString));
                 myNodes.get(last).setEmpty(true);
             }
         } else if (opString.contains("GOTO")) {
             myLabels.add(label);
             final int value = getLeftEmptyNodeIndex();
             if (!myGoToMap.containsKey(label)) {
                 List<Integer> list = new ArrayList<Integer>();
                 list.add(value);
                 myGoToMap.put(label, list);
             } else {
                 myGoToMap.get(label).add(value);
             }
         }
     }
 
     @Override
     public void visitLabel(final Label label) {
         getCurrentFrame().addLabel(label);
 
         if (myLabels.contains(label)) {
             applyNode();
             myLabels.remove(label);
         }
         myNodeInnerLabels.add(label);
     }
 
     @Override
     public void visitLdcInsn(final Object cst) {
         final boolean hasDoubleLength = true;
         myBodyStack.push(new Constant(cst, cst instanceof String, new Type("String")));
     }
 
     @Override
     public void visitIincInsn(final int var, final int increment) {
         if (!myBodyStack.empty()) {
             Expression expr = myBodyStack.peek();
             if (expr != null && expr instanceof Variable && ((Variable) expr).getIndex() == var) {
                 myBodyStack.pop();
                 myBodyStack.push(new ExprIncrement((Variable) expr, increment));
                 return;
             }
         }
         myStatements.add(new Increment(getCurrentFrame().getVariable(var), increment));
     }
 
     @Override
     public void visitTableSwitchInsn(final int min, final int max, final Label dflt, final Label... labels) {
         int[] keys = new int[max - min + 1];
         List<Label> list = new ArrayList<Label>();
         for (int i = 0; i < labels.length; i++) {
             myLabels.add(labels[i]);
             keys[i] = min + i;
             list.add(labels[i]);
         }
         list.add(dflt);
         myLabels.add(dflt);
         Node switch_node = new Switch(myBodyStack.pop(), keys, list, myNodes.size());
         myNodes.add(switch_node);
     }
 
     @Override
     public void visitLookupSwitchInsn(final Label dflt, final int[] keys, final Label[] labels) {
         List<Label> list = new ArrayList<Label>();
         for (Label label : labels) {
             myLabels.add(label);
             list.add(label);
         }
         list.add(dflt);
         myLabels.add(dflt);
         Node switch_node = new Switch(myBodyStack.pop(), keys, list, myNodes.size());
         myNodes.add(switch_node);
     }
 
     @Override
     public void visitMultiANewArrayInsn(final String desc, final int dims) {
         List<Expression> dimensions = new ArrayList<Expression>();
         for (int i = 0; i < dims; i++) {
             dimensions.add(0, getTopOfBodyStack());
         }
 
         final String className = getDescriptor(desc.substring(dims), 0, myDecompiledMethod.getImports()).trim();
         myBodyStack.push(createNewArray(dims, className, dimensions));
     }
 
 /*    @Override
     public void visitTryCatchBlock(final Label start, final Label end, final Label handler, final String type) {
         //System.out.println(start + " " + end + " " + handler + " " + type);
 
         ExceptionHandler exceptionHandler = new ExceptionHandler(type);
         myNodes.add(exceptionHandler);
         //  applyNode();
         final Node node = new Node();
         myNodes.add(node);
         final int temp = getLeftEmptyNodeIndex();
 //        myNodes.get(temp - 1).addTail(myNodes.get(myNodes.size() - 1));
         myLabels.add(start);
 //        myLabels.add(end);
         myLabels.add(handler);
         List<Integer> list = new ArrayList<Integer>();
         list.add(temp);
         if (type == null) {
         } else {
             myGoToMap.put(handler, list);
 
         }
         //  myGoToMap.put(handler, list);
     }*/
 
     @Override
     public void visitLocalVariable(final String name, final String desc,
                                    final String signature, final Label start, final Label end,
                                    final int index) {
         if (!myHasDebugInformation) {
             myHasDebugInformation = true;
         }
 
         final String description = signature != null ? signature : desc;
 
         String descriptor = getDescriptor(description, 0, myDecompiledMethod.getImports());
         myDecompiledMethod.updateVariableInformationFromDebugInfo(index, new Type(descriptor), new Constant(name, false, Type.VOID), start, end);
     }
 
     @Override
     public void visitLineNumber(final int line, final Label start) {
     }
 
     @Override
     public void visitMaxs(final int maxStack, final int maxLocals) {
     }
 
     @Override
     public void visitEnd() {
         applyNode();
 
         placeEdges();
 //        printDebugInfo();
 
         DominatorTreeGenerator gen = new DominatorTreeGenerator(myNodes);
         ConstructionBuilder cb = createConstructionBuilder(myNodes, gen);
 
         myDecompiledMethod.setBegin(cb.build());
     }
 
     private void printDebugInfo() {
         for (Node node : myNodes) {
             System.out.print(node.getIndex() + ": ");
             for (Node tail : node.getListOfTails()) {
                 System.out.print(tail.getIndex() + " ");
             }
             System.out.println();
         }
     }
 
 
     private void placeEdges() {
         // GOTO
         for (final Label lbl : myGoToMap.keySet()) {
             for (final Node node : myNodes) {
                 if (node.containsLabel(lbl)) {
                     for (final Integer i : myGoToMap.get(lbl)) {
                         if (i != myNodes.indexOf(node)) {
                             myNodes.get(i).addTail(node);
                             node.addAncestor(myNodes.get(i));
                         }
                     }
                     break;
                 }
             }
         }
         // Switch + sequence
         for (int i = 0; i < myNodes.size(); i++) {
             final Node node = myNodes.get(i);
             if (node instanceof Switch) {
                 for (final Label label : ((Switch) node).getLabels()) {
                     for (int j = i + 1; j < myNodes.size(); j++) {
                         if (myNodes.get(j).containsLabel(label)) {
                             node.addTail(myNodes.get(j));
                             myNodes.get(j).addAncestor(node);
                             break;
                         }
                     }
                 }
             } else if (node.getListOfTails().isEmpty() && !node.isLastStatementReturn() && node.getIndex() != myNodes.size() - 1) {
                 node.addTail(myNodes.get(i + 1));
                 myNodes.get(i + 1).addAncestor(node);
             }
         }
         // IF ELSE Branch
         for (final Integer index : myIfElseMap.keySet()) {
             for (final Node node : myNodes) {
                 if (node.containsLabel(myIfElseMap.get(index))) {
                     myNodes.get(index).addTail(node);
                     node.addAncestor(myNodes.get(index));
                     break;
                 }
             }
         }
         // Remove last return
         List<Statement> lastNodeStatements = myNodes.get(myNodes.size() - 1).getStatements();
         if (lastNodeStatements.size() != 0) {
             final Statement lastStatement = lastNodeStatements.get(lastNodeStatements.size() - 1);
             if (lastStatement instanceof Return && ((Return) lastStatement).getReturnValue() == null) {
                 lastNodeStatements.remove(lastNodeStatements.size() - 1);
             }
         }
     }
 
     private Expression getConditionFromStack(final String opString) {
         if (opString.contains("IF_")) {
             Expression e1 = getTopOfBodyStack();
             Expression e2 = getTopOfBodyStack();
             return new BinaryExpression(ExpressionType.valueOf(opString.substring(7)), e2, e1);
         } else {
             Expression e = getTopOfBodyStack();
             if (opString.contains("NONNULL")) {
                 return new BinaryExpression(ExpressionType.NE, e, Constant.NULL);
             } else if (opString.contains("NULL")) {
                 return new BinaryExpression(ExpressionType.EQ, e, Constant.NULL);
             } else {
                 if (e.isBoolean()) {
                     if (opString.contains("EQ")) {
                         return e.invert();
                     }
                     return e;
                 } else {
                     return new BinaryExpression(ExpressionType.valueOf(opString.substring(2)), e, IntConstant.ZERO);
                 }
             }
         }
     }
 
     protected Integer getLeftEmptyNodeIndex() {
         for (Node node : myNodes) {
             if (node.statementsIsEmpty() && !node.isEmpty()) {
                 return myNodes.indexOf(node);
             }
         }
         return myNodes.size();
     }
 
     protected void applyNode() {
         Integer i = getLeftEmptyNodeIndex();
         if (i != myNodes.size()) {
             myNodes.get(i).setStatements(new ArrayList<Statement>(myStatements));
             myNodes.get(i).setInnerLabels(new ArrayList<Label>(myNodeInnerLabels));
             if (myNodes.get(i).getStatements().isEmpty()) {
                 myNodes.get(i).setEmpty(true);
             }
         } else {
             Node node = new Node(new ArrayList<Statement>(myStatements), new ArrayList<Label>(myNodeInnerLabels), myNodes.size());
             if (node.getStatements().isEmpty()) {
                 node.setEmpty(true);
             }
             myNodes.add(node);
         }
         myNodeInnerLabels.clear();
         myStatements.clear();
     }
 
     protected ConstructionBuilder createConstructionBuilder(final List<Node> myNodes, final DominatorTreeGenerator gen) {
         return new ConstructionBuilder(myNodes, gen);
     }
 
     protected Expression getTopOfBodyStack() {
         if (myBodyStack.isEmpty()) {
             final int lastIndex = myStatements.size() - 1;
             final Statement lastStatement = myStatements.get(lastIndex);
 
             if (lastStatement instanceof com.sdc.ast.controlflow.Invocation) {
                 com.sdc.ast.controlflow.InstanceInvocation invoke = (com.sdc.ast.controlflow.InstanceInvocation) lastStatement;
                 myStatements.remove(lastIndex);
                 return invoke.toExpression();
             } else if (lastStatement instanceof Assignment) {
                 return ((Assignment) lastStatement).getRight();
             }
         }
         // ternary expression under construction
         /*
             else if ((myNodes.size() > 1) && (myBodyStack.size() >= 2) && (myNodes.get(myNodes.size() - 2).getCondition() != null)) {
             final int lastIndex = myNodes.size() - 1;
             myNodes.remove(lastIndex);
             final Node ifNode = myNodes.get(lastIndex - 1);
             final Expression condition = ifNode.getCondition();
             ifNode.setCondition(null);
             final TernaryExpression ternaryExpression = new TernaryExpression(condition, getTopOfBodyStack(), getTopOfBodyStack());
             myBodyStack.push(ternaryExpression);
         }
         */
         return myBodyStack.pop();
     }
 
     protected void removeThisVariableFromStack() {
         if (isThisVariableOnTopOfStack()) {
             myBodyStack.pop();
         }
     }
 
     protected boolean isThisVariableOnTopOfStack() {
         return myDecompiledMethod.isNormalClassMethod() && !myBodyStack.isEmpty() && myBodyStack.peek() instanceof Variable && ((Variable) myBodyStack.peek()).isThis();
     }
 
     protected void replaceInvocationsFromExpressionsToStatements() {
         for (final Expression expression : myBodyStack) {
             if (expression instanceof Invocation) {
                 myStatements.add(convertInvocationFromExpressionToStatement((Invocation) expression));
             }
         }
     }
 
     protected com.sdc.ast.controlflow.Invocation convertInvocationFromExpressionToStatement(final Invocation expression) {
         if (expression instanceof InstanceInvocation) {
             final InstanceInvocation invocation = (InstanceInvocation) expression;
             return new com.sdc.ast.controlflow.InstanceInvocation(invocation);
         } else {
             return new com.sdc.ast.controlflow.Invocation(expression);
         }
     }
 
     protected void appendInstanceInvocation(final String function, final Type returnType, final List<Expression> arguments, final Expression instance) {
         if (returnType.isVOID()) {
             myStatements.add(new com.sdc.ast.controlflow.InstanceInvocation(new InstanceInvocation(function, returnType, arguments, instance)));
         } else {
             myBodyStack.push(new InstanceInvocation(function, returnType, arguments, instance));
         }
     }
 
     protected void appendInvocation(final String function, final Type returnType, final List<Expression> arguments) {
         if (returnType.isVOID()) {
             myStatements.add(new com.sdc.ast.controlflow.Invocation(new Invocation(function, returnType, arguments)));
         } else {
             myBodyStack.push(new Invocation(function, returnType, arguments));
         }
     }
 
     protected List<Expression> getInvocationArguments(final String descriptor) {
         List<Expression> arguments = new ArrayList<Expression>();
         for (int i = 0; i < DeclarationWorker.getParametersCount(descriptor); i++) {
             arguments.add(0, getTopOfBodyStack());
         }
         return arguments;
     }
 
     protected String getInvocationReturnType(final String descriptor) {
         final int returnTypeIndex = descriptor.indexOf(')') + 1;
         return getDescriptor(descriptor, returnTypeIndex, myDecompiledMethod.getImports());
     }
 
     protected boolean hasVoidReturnType(final String descriptor) {
         final int returnTypeIndex = descriptor.indexOf(')') + 1;
         return descriptor.charAt(returnTypeIndex) == 'V';
     }
 
     protected boolean checkForSuperClassConstructor(final String invocationName, final String decompiledOwnerFullClassName) {
         return isThisVariableOnTopOfStack()
                 && (myDecompiledOwnerFullClassName.endsWith(myDecompiledMethod.getName()) && myDecompiledOwnerSuperClassName.endsWith(invocationName)
                 || myDecompiledOwnerSuperClassName.isEmpty() && decompiledOwnerFullClassName.equals("java.lang.Object"));
     }
 
     protected void processSuperClassConstructorInvocation(final String invocationName, final Type returnType, final List<Expression> arguments) {
         if (!arguments.isEmpty()) {
             myStatements.add(new com.sdc.ast.controlflow.Invocation(new Invocation("super", returnType, arguments)));
         }
     }
 
     protected void appendInvocationOrConstructor(final boolean isStaticInvocation, final String visitMethodName,
                                                  final String invocationName, final Type returnType, final List<Expression> arguments, final String decompiledOwnerFullClassName) {
 
         if (visitMethodName.equals("<init>")) {
             if (checkForSuperClassConstructor(invocationName, decompiledOwnerFullClassName)) {
                 removeThisVariableFromStack();
                 processSuperClassConstructorInvocation(invocationName, returnType, arguments);
             } else {
                 if (!myDecompiledMethod.getDecompiledClass().hasAnonymousClass(invocationName)) {
                     myBodyStack.pop();
                     myBodyStack.pop();
 
                     myBodyStack.push(new New(new com.sdc.ast.expressions.Invocation(invocationName, returnType, arguments)));
                 } else {
                     myBodyStack.push(new com.sdc.ast.expressions.nestedclasses.AnonymousClass(myDecompiledMethod.getDecompiledClass().getAnonymousClass(invocationName), arguments));
                 }
             }
         } else {
             if (!isStaticInvocation) {
                 removeThisVariableFromStack();
             }
 
            if (!myDecompiledMethod.getModifier().contains("synthetic static") || invocationName.contains(".")) {
                 appendInvocation(invocationName, returnType, arguments);
             } else {
                 appendInstanceInvocation(invocationName, returnType, arguments, myBodyStack.pop());
             }
         }
     }
 
     protected NewArray createNewArray(final int dimensionsCount, final String type, final List<Expression> dimensions) {
         return new NewArray(dimensionsCount, type, dimensions);
     }
 
     private void multiPush(Expression... expressions) {
         for (Expression expr : expressions) {
             myBodyStack.push(expr);
         }
     }
 
     protected Expression replaceBooleanConstant(final Expression expression) {
         if (expression instanceof Constant && myDecompiledMethod.getReturnType().toLowerCase().contains("boolean")) {
             return new Constant(((Constant) expression).getValue().toString().equals("1"), false, Type.BOOLEAN_TYPE);
         } else {
             return expression;
         }
     }
 
     protected String decompileClassNameWithOuterClasses(final String fullClassName) {
         if (fullClassName.contains(myDecompiledMethod.getName())) {
             return DeclarationWorker.decompileSimpleClassName(fullClassName);
         }
 
         return myDecompiledMethod.getDecompiledClass().decompileClassNameWithOuterClasses(fullClassName);
     }
 
     protected String getDescriptor(final String descriptor, final int pos, List<String> imports) {
         final String decompiledDescriptor = myDecompiledMethod.getDecompiledClass().getDescriptor(descriptor, pos, imports, myLanguage);
 
         return decompiledDescriptor.contains(myDecompiledMethod.getName()) ? DeclarationWorker.decompileSimpleClassName(decompiledDescriptor) : decompiledDescriptor;
     }
 }
