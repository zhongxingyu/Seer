 package com.sdc.abstractLanguage;
 
 import com.sdc.ast.controlflow.*;
 import com.sdc.ast.expressions.*;
 import com.sdc.ast.expressions.InstanceInvocation;
 import com.sdc.ast.expressions.Invocation;
 import com.sdc.ast.expressions.identifiers.Field;
 import com.sdc.ast.expressions.identifiers.Identifier;
 import com.sdc.ast.expressions.identifiers.Variable;
 import com.sdc.ast.expressions.nestedclasses.LambdaFunction;
 
 import com.sdc.cfg.ExceptionHandler;
 import com.sdc.cfg.Node;
 import com.sdc.cfg.Switch;
 import com.sdc.cfg.functionalization.AnonymousClass;
 import com.sdc.cfg.functionalization.Generator;
 
 import com.sdc.util.DeclarationWorker;
 
 import org.objectweb.asm.*;
 import org.objectweb.asm.util.Printer;
 
 import java.util.*;
 
 import static org.objectweb.asm.Opcodes.ASM4;
 
 public abstract class AbstractMethodVisitor  extends MethodVisitor {
     protected AbstractMethod myDecompiledMethod;
 
     protected final String myDecompiledOwnerFullClassName;
     protected final String myDecompiledOwnerSuperClassName;
 
     protected Stack<Expression> myBodyStack = new Stack<Expression>();
     protected List<Statement> myStatements = new ArrayList<Statement>();
 
     protected List<Node> myNodes = new ArrayList<Node>();
     protected List<Label> myLabels = new ArrayList<Label>();
     protected Map<Label, List<Integer>> myMap1 = new HashMap<Label, List<Integer>>();  // for GOTO
     protected Map<Integer, Label> myMap2 = new HashMap<Integer, Label>(); // for IF ELSE Branch
     protected List<Label> myNodeInnerLabels = new ArrayList<Label>();
 
     protected boolean myHasDebugInformation = false;
 
     protected AbstractLanguagePartFactory myLanguagePartFactory;
     protected AbstractVisitorFactory myVisitorFactory;
 
     protected DeclarationWorker.SupportedLanguage myLanguage;
 
     public AbstractMethodVisitor(final AbstractMethod abstractMethod, final String decompiledOwnerFullClassName, final String decompiledOwnerSuperClassName) {
         super(ASM4);
         this.myDecompiledMethod = abstractMethod;
         this.myDecompiledOwnerFullClassName = decompiledOwnerFullClassName;
         this.myDecompiledOwnerSuperClassName = decompiledOwnerSuperClassName;
     }
 
     protected abstract boolean checkForAutomaticallyGeneratedAnnotation(final String annotationName);
 
     protected AbstractFrame getCurrentFrame() {
         return myDecompiledMethod.getCurrentFrame();
     }
 
     public AbstractMethod getDecompiledMethod() {
         return myDecompiledMethod;
     }
 
     protected AnnotationVisitor visitAnnotation(final int parameter, final String desc, final boolean visible) {
         List<String> annotationsImports = new ArrayList<String>();
         final String annotationName = getDescriptor(desc, 0, annotationsImports);
         if (!checkForAutomaticallyGeneratedAnnotation(annotationName)) {
             AbstractAnnotation annotation = myLanguagePartFactory.createAnnotation();
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
         if (type == 2) {
             // F_CHOP
             myDecompiledMethod.setCurrentFrame(getCurrentFrame().getParent());
         } else if (type == 3) {
             // F_SAME
             AbstractFrame newAbstractFrame = myLanguagePartFactory.createFrame();
             newAbstractFrame.setSameFrame(getCurrentFrame());
             newAbstractFrame.setParent(getCurrentFrame().getParent());
             if (getCurrentFrame().getParent() != null) {
                 getCurrentFrame().getParent().addChild(newAbstractFrame);
             } else {
                 getCurrentFrame().addChild(newAbstractFrame);
             }
 
             myDecompiledMethod.setCurrentFrame(newAbstractFrame);
         } else {
             AbstractFrame newAbstractFrame = myLanguagePartFactory.createFrame();
 
             newAbstractFrame.setParent(getCurrentFrame());
             getCurrentFrame().addChild(newAbstractFrame);
 
             myDecompiledMethod.setCurrentFrame(newAbstractFrame);
 
             if (nStack > 0) {
                 String stackedVariableType;
 
                 if (stack[0] instanceof Integer) {
                     stackedVariableType = DeclarationWorker.getDescriptorByInt((Integer) stack[0], myLanguage);
                 } else {
                     final String className = (String) stack[0];
                     myDecompiledMethod.addImport(DeclarationWorker.getDecompiledFullClassName(className));
                     stackedVariableType = getClassName(className) + " ";
                 }
 
                 getCurrentFrame().setStackedVariableType(stackedVariableType);
             }
         }
     }
 
     @Override
     public void visitInsn(final int opcode) {
         final String opString = Printer.OPCODES[opcode];
 
         if (opString.contains("ADD") || opString.contains("SUB")
                 || opString.contains("MUL") || opString.contains("DIV")) {
             Expression e1 = getTopOfBodyStack();
             Expression e2 = getTopOfBodyStack();
             Expression res = new BinaryExpression(BinaryExpression.OperationType.valueOf(opString.substring(1)), e2, e1);
             myBodyStack.push(res);
         } else if (opString.contains("NEG")) {
             myBodyStack.push(new UnaryExpression(UnaryExpression.OperationType.NEGATE, getTopOfBodyStack()));
         } else if (opString.contains("CONST_")) {
             myBodyStack.push(new Constant(opString.substring(7).toLowerCase(), false));
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
             Expression e1 = getTopOfBodyStack();
             Expression e2 = getTopOfBodyStack();
             myBodyStack.push(new BinaryExpression(e2, e1));
         } else if (opString.contains("ATHROW")) {
             myStatements.add(new Throw(getTopOfBodyStack()));
         } else if (opString.equals("SWAP")) {
             Expression expr1 = myBodyStack.pop();
             Expression expr2 = myBodyStack.pop();
             myBodyStack.push(expr1);
             myBodyStack.push(expr2);
         } else if (opString.equals("DUP") && !myBodyStack.isEmpty()) {
//            myBodyStack.push(myBodyStack.peek());
         } else if (opString.equals("DUP_X1")) {
             Expression expr1 = myBodyStack.pop();
             Expression expr2 = myBodyStack.pop();
             myBodyStack.push(expr1);
             myBodyStack.push(expr2);
             myBodyStack.push(expr1);
         } else if (opString.contains("ALOAD")) {
             final Expression arrayIndex = getTopOfBodyStack();
             myBodyStack.push(new Variable(arrayIndex, (Identifier) getTopOfBodyStack()));
         } else if (opString.contains("ASTORE")) {
             final Expression expr = getTopOfBodyStack();
             final Expression arrayIndex = getTopOfBodyStack();
             final Identifier v = new Variable(arrayIndex, (Identifier) getTopOfBodyStack());
 
             myStatements.add(new Assignment(v, expr));
         }
     }
 
     @Override
     public void visitIntInsn(final int opcode, final int operand) {
         final String opString = Printer.OPCODES[opcode];
 
         if (opString.contains("IPUSH")) {
             myBodyStack.push(new Constant(operand, false));
         } else if (opString.contains("NEWARRAY")) {
             List<Expression> dimensions = new ArrayList<Expression>();
             dimensions.add(getTopOfBodyStack());
             myBodyStack.push(new NewArray(1, Printer.TYPES[operand].substring(2).toLowerCase(), dimensions));
         }
     }
 
     @Override
     public void visitVarInsn(final int opcode, final int var) {
         final String opString = Printer.OPCODES[opcode];
 
         final boolean currentFrameHasStack = getCurrentFrame().checkStack();
         String variableType = null;
 
         if (opString.contains("LOAD")) {
             myBodyStack.push(new Variable(var, getCurrentFrame()));
         } else if (opString.contains("STORE") && !currentFrameHasStack) {
             Identifier v = new Variable(var, getCurrentFrame());
             final Expression expr = getTopOfBodyStack();
             myStatements.add(new Assignment(v, expr));
             if (expr instanceof com.sdc.ast.expressions.Invocation) {
                 variableType = ((com.sdc.ast.expressions.Invocation) expr).getReturnType();
             } else if (expr instanceof New) {
                 variableType = ((New) expr).getReturnType();
             } else if (expr instanceof NewArray) {
                 variableType = ((NewArray) expr).getFullType();
             } else if (expr instanceof Identifier) {
                 variableType = ((Identifier) expr).getType();
             } else if (expr instanceof LambdaFunction) {
                 variableType = ((LambdaFunction) expr).getType();
             }
         }
 
         if (!opString.contains("LOAD") && var > myDecompiledMethod.getLastLocalVariableIndex()) {
             final String name = "y" + var;
             myDecompiledMethod.addLocalVariableName(var, name);
 
             String descriptorType;
             if (currentFrameHasStack) {
                 descriptorType = getCurrentFrame().getStackedVariableType();
                 getCurrentFrame().setStackedVariableIndex(var);
             } else {
                 descriptorType = getDescriptor(opString, 0, myDecompiledMethod.getImports());
             }
 
             if (!descriptorType.equals("Object ") && !descriptorType.equals("Any") || variableType == null) {
                 variableType = descriptorType;
             }
 
             myDecompiledMethod.addLocalVariableType(var, variableType);
         }
     }
 
     @Override
     public void visitTypeInsn(final int opcode, final String type) {
         final String opString = Printer.OPCODES[opcode];
 
         if (opString.contains("NEWARRAY")) {
             List<Expression> dimensions = new ArrayList<Expression>();
             dimensions.add(getTopOfBodyStack());
             myBodyStack.push(new NewArray(1, getClassName(type), dimensions));
         } else if (opString.contains("INSTANCEOF")) {
             myBodyStack.push(new InstanceOf(getClassName(type), getTopOfBodyStack()));
         }
     }
 
     @Override
     public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
         final String opString = Printer.OPCODES[opcode];
         final String fieldName = myDecompiledMethod.getDecompiledClass().isLambdaFunctionClass() ? name.substring(1) : name;
 
         if (opString.contains("PUTFIELD")) {
             if (myDecompiledOwnerFullClassName.endsWith(myDecompiledMethod.getName()) && !myBodyStack.isEmpty() && myBodyStack.peek() instanceof Constant) {
                 myDecompiledMethod.addInitializerToField(name, getTopOfBodyStack());
             } else {
                 final Identifier v = new Field(fieldName, getDescriptor(desc, 0, myDecompiledMethod.getImports()));
                 final Expression e = getTopOfBodyStack();
                 myStatements.add(new Assignment(v, e));
             }
             removeThisVariableFromStack();
         } else if (opString.contains("GETFIELD")) {
             removeThisVariableFromStack();
             myBodyStack.push(new Field(fieldName, getDescriptor(desc, 0, myDecompiledMethod.getImports())));
         }
     }
 
     @Override
     public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc) {
         final String opString = Printer.OPCODES[opcode];
 
         final String decompiledOwnerFullClassName = DeclarationWorker.getDecompiledFullClassName(owner);
         final String ownerClassName = getClassName(owner);
 
         List<Expression> arguments = getInvocationArguments(desc);
         String returnType = getInvocationReturnType(desc);
         String invocationName = name;
 
         boolean isStaticInvocation = false;
 
         if (opString.contains("INVOKEVIRTUAL") || opString.contains("INVOKEINTERFACE")
                 || (decompiledOwnerFullClassName.equals(myDecompiledOwnerFullClassName) && !name.equals("<init>")))
         {
             if (!myBodyStack.isEmpty() && myBodyStack.peek() instanceof Variable) {
                 appendInstanceInvocation(name, returnType, arguments, (Variable) myBodyStack.pop());
                 return;
             } else {
                 invocationName = "." + name;
             }
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
 
         appendInvocationOrConstructor(isStaticInvocation, name, invocationName, returnType, arguments);
     }
 
     @Override
     public void visitInvokeDynamicInsn(final String name, final String desc, final Handle bsm, final Object... bsmArgs) {
     }
 
     @Override
     public void visitJumpInsn(final int opcode, final Label label) {
         final String opString = Printer.OPCODES[opcode];
         //System.out.println(opString + ": " + label);
         if (opString.contains("IF")) {
             final Label myLastIFLabel = label;
             if (myNodes.isEmpty() || !myNodeInnerLabels.isEmpty() || (myNodes.get(getLeftEmptyNodeIndex() - 1).getCondition() == null)) {
                 myLabels.add(myLastIFLabel);
                 myMap2.put(myNodes.size(), label);
                 applyNode();
                 final int last = myNodes.size() - 1;
                 myNodes.get(last).setCondition(new BinaryExpression(null, null));
                 myNodes.get(last).setEmpty(true);
             }
         } else if (opString.contains("GOTO")) {
             myLabels.add(label);
             final int value = getLeftEmptyNodeIndex();
             if (!myMap1.containsKey(label)) {
                 List<Integer> list = new ArrayList<Integer>();
                 list.add(value);
                 myMap1.put(label, list);
             } else {
                 myMap1.get(label).add(value);
             }
         }
     }
 
     @Override
     public void visitLabel(final Label label) {
         if (myLabels.contains(label) && (!myStatements.isEmpty())) {
             applyNode();
             myLabels.remove(label);
         }
         myNodeInnerLabels.add(label);
 
         //System.out.println(label);
     }
 
     @Override
     public void visitLdcInsn(final Object cst) {
         myBodyStack.push(new Constant(cst, cst instanceof String));
     }
 
     @Override
     public void visitIincInsn(final int var, final int increment) {
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
         Node switch_node = new Switch(myBodyStack.pop(), keys, list);
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
         Node switch_node = new Switch(myBodyStack.pop(), keys, list);
         myNodes.add(switch_node);
     }
 
     @Override
     public void visitMultiANewArrayInsn(final String desc, final int dims) {
         List<Expression> dimensions = new ArrayList<Expression>();
         for (int i = 0; i < dims; i++) {
             dimensions.add(0, getTopOfBodyStack());
         }
 
         final String className = getDescriptor(desc.substring(dims), 0, myDecompiledMethod.getImports()).trim();
         myBodyStack.push(new NewArray(dims, className, dimensions));
     }
 
     @Override
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
             myMap1.put(handler, list);
 
         }
         //  myMap1.put(handler, list);
     }
 
     @Override
     public void visitLocalVariable(final String name, final String desc,
                                    final String signature, final Label start, final Label end,
                                    final int index)
     {
         if (!myHasDebugInformation) {
             myHasDebugInformation = true;
         }
 
        myDecompiledMethod.addLocalVariableName(index, name);
         final String description = signature != null ? signature : desc;
         myDecompiledMethod.addLocalVariableFromDebugInfo(index, name, getDescriptor(description, 0, myDecompiledMethod.getImports()));
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
         // GOTO
         for (final Label lbl : myMap1.keySet()) {
             for (final Node node : myNodes) {
                 if (node.containsLabel(lbl)) {
                     for (final Integer i : myMap1.get(lbl)) {
                         myNodes.get(i).addTail(node);
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
                             break;
                         }
                     }
                 }
             } else if (node.getListOfTails().isEmpty() && !node.isLastStatementReturn()) {
                 node.addTail(myNodes.get(i + 1));
             }
         }
         // IF ELSE Branch
         for (final Integer index : myMap2.keySet()) {
             for (final Node node : myNodes) {
                 if (node.containsLabel(myMap2.get(index))) {
                     myNodes.get(index).addTail(node);
                     break;
                 }
             }
         }
 
         Generator generator = new Generator(myNodes);
         AnonymousClass aClass = generator.genAnonymousClass();
         // myKotlinMethod.setAnonymousClass(aClass);
         myDecompiledMethod.setBody(myStatements);
         myDecompiledMethod.setNodes(myNodes);
         //myKotlinMethod.drawCFG();
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
             Node node = new Node(new ArrayList<Statement>(myStatements), new ArrayList<Label>(myNodeInnerLabels));
             if (node.getStatements().isEmpty()) {
                 node.setEmpty(true);
             }
             myNodes.add(node);
         }
         myNodeInnerLabels.clear();
         // myStatements.clear();
     }
 
     protected Expression getTopOfBodyStack() {
         if (myBodyStack.isEmpty()) {
             final int lastIndex = myStatements.size() - 1;
             final Statement lastStatement = myStatements.get(lastIndex);
 
             if (lastStatement instanceof com.sdc.ast.controlflow.InstanceInvocation) {
                 com.sdc.ast.controlflow.InstanceInvocation invoke = (com.sdc.ast.controlflow.InstanceInvocation) lastStatement;
                 myStatements.remove(lastIndex);
                 return new com.sdc.ast.expressions.InstanceInvocation(invoke.getFunction(), invoke.getReturnType(), invoke.getArguments(), invoke.getVariable());
             } else if (lastStatement instanceof com.sdc.ast.controlflow.Invocation) {
                 com.sdc.ast.controlflow.Invocation invoke = (com.sdc.ast.controlflow.Invocation) lastStatement;
                 myStatements.remove(lastIndex);
                 return new com.sdc.ast.expressions.Invocation(invoke.getFunction(), invoke.getReturnType(), invoke.getArguments());
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
         if (myDecompiledMethod.isNormalClassMethod() && !myBodyStack.isEmpty()
                 && myBodyStack.peek() instanceof Variable && ((Variable) myBodyStack.peek()).getName().equals("this"))
         {
             myBodyStack.pop();
         }
     }
 
     protected void replaceInvocationsFromExpressionsToStatements() {
         for (final Expression expression : myBodyStack) {
             if (expression instanceof InstanceInvocation) {
                 final InstanceInvocation invocation = (InstanceInvocation) expression;
                 myStatements.add(new com.sdc.ast.controlflow.InstanceInvocation(invocation.getFunction(), invocation.getReturnType(), invocation.getArguments(), invocation.getVariable()));
             } else if (expression instanceof Invocation) {
                 final Invocation invocation = (Invocation) expression;
                 myStatements.add(new com.sdc.ast.controlflow.Invocation(invocation.getFunction(), invocation.getReturnType(), invocation.getArguments()));
             }
         }
     }
 
     protected void appendInstanceInvocation(final String function, final String returnType, final List<Expression> arguments, final Variable variable) {
         if (myBodyStack.isEmpty()) {
             myStatements.add(new com.sdc.ast.controlflow.InstanceInvocation(function, returnType, arguments, variable));
         } else {
             myBodyStack.push(new com.sdc.ast.expressions.InstanceInvocation(function, returnType, arguments, variable));
         }
     }
 
     protected void appendInvocation(final String function, final String returnType, final List<Expression> arguments) {
         if (myBodyStack.isEmpty()) {
             myStatements.add(new com.sdc.ast.controlflow.Invocation(function, returnType, arguments));
         } else {
             myBodyStack.push(new com.sdc.ast.expressions.Invocation(function, returnType, arguments));
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
 
     protected boolean checkForSuperClassConstructor(final String invocationName) {
         return myDecompiledOwnerFullClassName.endsWith(myDecompiledMethod.getName()) && myDecompiledOwnerSuperClassName.endsWith(invocationName);
     }
 
     protected void processSuperClassConstructorInvocation(final String invocationName, final String returnType, final List<Expression> arguments) {
         myStatements.add(new com.sdc.ast.controlflow.Invocation("super", returnType, arguments));
     }
 
     protected void appendInvocationOrConstructor(final boolean isStaticInvocation, final String visitMethodName,
                                                  final String invocationName, final String returnType, final List<Expression> arguments)
     {
         if (visitMethodName.equals("<init>")) {
             if (checkForSuperClassConstructor(invocationName)) {
                 removeThisVariableFromStack();
                 processSuperClassConstructorInvocation(invocationName, returnType, arguments);
             } else {
                 if (!myDecompiledMethod.getDecompiledClass().hasAnonymousClass(invocationName)) {
                     myBodyStack.push(new New(new com.sdc.ast.expressions.Invocation(invocationName, returnType, arguments)));
                 } else {
                     myBodyStack.push(new com.sdc.ast.expressions.nestedclasses.AnonymousClass(myDecompiledMethod.getDecompiledClass().getAnonymousClass(invocationName), arguments));
                 }
             }
         } else {
             if (!isStaticInvocation) {
                 removeThisVariableFromStack();
             }
 
             appendInvocation(invocationName, returnType, arguments);
         }
     }
 
     protected Expression replaceBooleanConstant(final Expression expression) {
         if (expression instanceof Constant && myDecompiledMethod.getReturnType().toLowerCase().contains("boolean")) {
             return new Constant(((Constant) expression).getValue().toString().equals("1"), false);
         } else {
             return expression;
         }
     }
 
     protected String getClassName(final String fullClassName) {
         return myDecompiledMethod.getDecompiledClass().getClassName(fullClassName);
     }
 
     protected String getDescriptor(final String descriptor, final int pos, List<String> imports) {
         return myDecompiledMethod.getDecompiledClass().getDescriptor(descriptor, pos, imports, myLanguage);
     }
 }
