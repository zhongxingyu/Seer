 package com.sdc.kotlin;
 
 import com.sdc.abstractLanguage.AbstractFrame;
 import com.sdc.abstractLanguage.AbstractMethodVisitor;
 import com.sdc.ast.controlflow.*;
 import com.sdc.ast.expressions.*;
 import com.sdc.ast.expressions.identifiers.Field;
 import com.sdc.ast.expressions.identifiers.Identifier;
 import com.sdc.ast.expressions.identifiers.Variable;
 import com.sdc.cfg.ExceptionHandler;
 import com.sdc.cfg.Node;
 import com.sdc.cfg.Switch;
 import com.sdc.cfg.functionalization.AnonymousClass;
 import com.sdc.cfg.functionalization.Generator;
 import com.sdc.util.DeclarationWorker;
 
 import org.objectweb.asm.AnnotationVisitor;
 import org.objectweb.asm.Attribute;
 import org.objectweb.asm.Handle;
 import org.objectweb.asm.Label;
 import org.objectweb.asm.util.Printer;
 
 import java.util.*;
 
 public class KotlinMethodVisitor extends AbstractMethodVisitor {
     private KotlinMethod myKotlinMethod;
 
     private final String myDecompiledOwnerFullClassName;
     private final String myDecompiledOwnerSuperClassName;
 
     private Stack<Expression> myBodyStack = new Stack<Expression>();
     private List<Statement> myStatements = new ArrayList<Statement>();
 
     private List<Node> myNodes = new ArrayList<Node>();
     private List<Label> myLabels = new ArrayList<Label>();
     private Map<Label, List<Integer>> myMap1 = new HashMap<Label, List<Integer>>();  // for GOTO
     private Map<Integer, Label> myMap2 = new HashMap<Integer, Label>(); // for IF ELSE Branch
     private List<Label> myNodeInnerLabels = new ArrayList<Label>();
 
     private boolean myHasDebugInformation = false;
 
     public KotlinMethodVisitor(KotlinMethod kotlinMethod, final String decompiledOwnerFullClassName, final String decompiledOwnerSuperClassName) {
         this.myKotlinMethod = kotlinMethod;
         this.myDecompiledOwnerFullClassName = decompiledOwnerFullClassName;
         this.myDecompiledOwnerSuperClassName = decompiledOwnerSuperClassName;
     }
 
     private AbstractFrame getCurrentFrame() {
         return myKotlinMethod.getCurrentFrame();
     }
 
     @Override
     public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
         List<String> annotationsImports = new ArrayList<String>();
         final String annotationName = DeclarationWorker.getKotlinDescriptor(desc, 0, annotationsImports);
         if (!annotationName.startsWith("Jet")) {
             KotlinAnnotation annotation = new KotlinAnnotation();
             annotation.setName(annotationName);
             myKotlinMethod.appendAnnotation(annotation);
             myKotlinMethod.getImports().addAll(annotationsImports);
             return new KotlinAnnotationVisitor(annotation);
         } else {
             return null;
         }
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
         List<String> annotationsImports = new ArrayList<String>();
         final String annotationName = DeclarationWorker.getKotlinDescriptor(desc, 0, annotationsImports);
         if (!annotationName.startsWith("Jet")) {
             KotlinAnnotation annotation = new KotlinAnnotation();
             annotation.setName(annotationName);
             myKotlinMethod.appendParameterAnnotation(parameter, annotation);
             myKotlinMethod.getImports().addAll(annotationsImports);
             return new KotlinAnnotationVisitor(annotation);
         } else {
             return null;
         }
     }
 
     @Override
     public void visitCode() {
     }
 
     @Override
     public void visitFrame(final int type, final int nLocal, final Object[] local, final int nStack, final Object[] stack) {
         if (type == 2) {
             // F_CHOP
             myKotlinMethod.setCurrentFrame(getCurrentFrame().getParent());
         } else if (type == 3) {
             // F_SAME
             AbstractFrame newAbstractFrame = new KotlinFrame();
             newAbstractFrame.setSameFrame(getCurrentFrame());
             newAbstractFrame.setParent(getCurrentFrame().getParent());
             getCurrentFrame().getParent().addChild(newAbstractFrame);
 
             myKotlinMethod.setCurrentFrame(newAbstractFrame);
         } else {
             AbstractFrame newAbstractFrame = new KotlinFrame();
 
             newAbstractFrame.setParent(getCurrentFrame());
             getCurrentFrame().addChild(newAbstractFrame);
 
             myKotlinMethod.setCurrentFrame(newAbstractFrame);
 
             if (nStack > 0) {
                 String stackedVariableType = "";
 
                 if (stack[0] instanceof Integer) {
                     switch ((Integer) stack[0]) {
                         case 1:
                             stackedVariableType = "int ";
                             break;
                         case 2:
                             stackedVariableType = "float ";
                             break;
                         case 3:
                             stackedVariableType = "double ";
                             break;
                         case 4:
                             stackedVariableType = "long ";
                             break;
                     }
                 } else {
                     final String className = (String) stack[0];
                     myKotlinMethod.addImport(DeclarationWorker.getDecompiledFullClassName(className));
                     stackedVariableType = DeclarationWorker.getClassName(className) + " ";
                 }
 
                 getCurrentFrame().setStackedVariableType(stackedVariableType);
             }
         }
     }
 
     @Override
     public void visitInsn(final int opcode) {
         final String opString = Printer.OPCODES[opcode];
         //System.out.println(opString);
 
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
             myStatements.add(new Return());
         } else if (opString.contains("RETURN")) {
             myStatements.add(new Return(getTopOfBodyStack()));
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
             myBodyStack.push(myBodyStack.peek());
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
         //System.out.println(opString + " " + var);
 
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
             }
         }
 
         if (!opString.contains("LOAD") && var > myKotlinMethod.getLastLocalVariableIndex()) {
             final String name = "y" + var;
             myKotlinMethod.addLocalVariableName(var, name);
 
             String descriptorType;
             if (currentFrameHasStack) {
                 descriptorType = getCurrentFrame().getStackedVariableType();
                 getCurrentFrame().setStackedVariableIndex(var);
             } else {
                 descriptorType = DeclarationWorker.getKotlinDescriptor(opString, 0, myKotlinMethod.getImports());
             }
 
             if (!descriptorType.equals("Object ") || variableType == null) {
                 variableType = descriptorType;
             }
 
             myKotlinMethod.addLocalVariableType(var, variableType);
         }
     }
 
     @Override
     public void visitTypeInsn(final int opcode, final String type) {
         final String opString = Printer.OPCODES[opcode];
 
         if (opString.contains("NEWARRAY")) {
             List<Expression> dimensions = new ArrayList<Expression>();
             dimensions.add(getTopOfBodyStack());
             myBodyStack.push(new NewArray(1, DeclarationWorker.getClassName(type), dimensions));
         }
     }
 
     @Override
     public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
         final String opString = Printer.OPCODES[opcode];
 
         if (opString.contains("PUTFIELD")) {
             if (!myDecompiledOwnerFullClassName.endsWith(myKotlinMethod.getName())) {
                 final Identifier v = new Field(name, DeclarationWorker.getKotlinDescriptor(desc, 0, myKotlinMethod.getImports()));
                 final Expression e = getTopOfBodyStack();
                 myStatements.add(new Assignment(v, e));
             } else {
                 myKotlinMethod.addInitializerToField(name, getTopOfBodyStack());
             }
             removeThisVariableFromStack();
         } else if (opString.contains("GETFIELD")) {
             removeThisVariableFromStack();
             myBodyStack.push(new Field(name, DeclarationWorker.getKotlinDescriptor(desc, 0, myKotlinMethod.getImports())));
         }
     }
 
     @Override
     public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc) {
         final String opString = Printer.OPCODES[opcode];
         //System.out.println(opString + " " + owner + " " + name + " " + desc);
 
         List<Expression> arguments = new ArrayList<Expression>();
         for (int i = 0; i < DeclarationWorker.getParametersCount(desc); i++) {
             arguments.add(0, getTopOfBodyStack());
         }
 
         final int returnTypeIndex = desc.indexOf(')') + 1;
         String returnType = DeclarationWorker.getKotlinDescriptor(desc, returnTypeIndex, myKotlinMethod.getImports());
 
         final String decompiledOwnerClassName = DeclarationWorker.getDecompiledFullClassName(owner);
 
         String invocationName = "";
         boolean needToRemoveThisFromStack = true;
 
         if (opString.contains("INVOKEVIRTUAL") || opString.contains("INVOKEINTERFACE")
                 || (decompiledOwnerClassName.equals(myDecompiledOwnerFullClassName) && !name.equals("<init>"))) {
             Variable v = (Variable) myBodyStack.pop();
             if (myBodyStack.isEmpty()) {
                 myStatements.add(new com.sdc.ast.controlflow.InstanceInvocation(name, returnType, arguments, v));
             } else {
                 myBodyStack.push(new com.sdc.ast.expressions.InstanceInvocation(name, returnType, arguments, v));
             }
             return;
         } else if (opString.contains("INVOKESPECIAL")) {
             if (name.equals("<init>")) {
                 myKotlinMethod.addImport(decompiledOwnerClassName);
                 invocationName = DeclarationWorker.getClassName(owner);
                 returnType = invocationName + " ";
             } else {
                 invocationName = "super<" + DeclarationWorker.getClassName(owner) + ">."  + name;
             }
         } else if (opString.contains("INVOKESTATIC")) {
             myKotlinMethod.addImport(decompiledOwnerClassName);
             invocationName = DeclarationWorker.getClassName(owner) + "." + name;
             needToRemoveThisFromStack = false;
             if (name.equals("checkParameterIsNotNull")) {
                 ((KotlinFrame) getCurrentFrame()).addNotNullVariable(((Variable) arguments.get(0)).getIndex());
                 return;
             }
         }
 
         if (name.equals("<init>")) {
             if (myDecompiledOwnerFullClassName.endsWith(myKotlinMethod.getName()) && myDecompiledOwnerSuperClassName.endsWith(invocationName)) {
                 removeThisVariableFromStack();
                 myKotlinMethod.getKotlinClass().setSuperClassConstructor(new com.sdc.ast.expressions.Invocation(invocationName, returnType, arguments));
             } else {
                 myBodyStack.push(new New(new com.sdc.ast.expressions.Invocation(invocationName, returnType, arguments)));
             }
         } else {
             if (needToRemoveThisFromStack) {
                 removeThisVariableFromStack();
             }
 
             if (myBodyStack.isEmpty()) {
                 myStatements.add(new com.sdc.ast.controlflow.Invocation(invocationName, returnType, arguments));
             } else {
                 myBodyStack.push(new com.sdc.ast.expressions.Invocation(invocationName, returnType, arguments));
             }
         }
     }
 
     @Override
     public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
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
 
        myBodyStack.push(new NewArray(dims, DeclarationWorker.getKotlinDescriptor(desc.substring(dims), 0, myKotlinMethod.getImports()).trim(), dimensions));
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
                                    final int index) {
         if (!myHasDebugInformation) {
             myHasDebugInformation = true;
         }
 
         final String description = signature != null ? signature : desc;
         myKotlinMethod.addLocalVariableFromDebugInfo(index, name, DeclarationWorker.getKotlinDescriptor(description, 0, myKotlinMethod.getImports()));
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
         myKotlinMethod.setBody(myStatements);
         myKotlinMethod.setNodes(myNodes);
         //myKotlinMethod.drawCFG();
     }
 
     private Integer getLeftEmptyNodeIndex() {
         for (Node node : myNodes) {
             if (node.statementsIsEmpty() && !node.isEmpty()) {
                 return myNodes.indexOf(node);
             }
         }
         return myNodes.size();
     }
 
     private void applyNode() {
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
 
     private Expression getTopOfBodyStack() {
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
         } else if ((myNodes.size() > 1) && (myBodyStack.size() >= 2) && (myNodes.get(myNodes.size() - 2).getCondition() != null)) {
             final int lastIndex = myNodes.size() - 1;
             myNodes.remove(lastIndex);
             final Node ifNode = myNodes.get(lastIndex - 1);
             final Expression condition = ifNode.getCondition();
             ifNode.setCondition(null);
             final TernaryExpression ternaryExpression = new TernaryExpression(condition, getTopOfBodyStack(), getTopOfBodyStack());
             myBodyStack.push(ternaryExpression);
         }
         return myBodyStack.pop();
     }
 
     private void removeThisVariableFromStack() {
         if (!myBodyStack.isEmpty()) {
             myBodyStack.pop();
         }
     }
 }
 
