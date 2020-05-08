 package com.sdc.kotlin;
 
 import com.sdc.abstractLanguage.AbstractClass;
 import com.sdc.abstractLanguage.AbstractClassVisitor;
 import com.sdc.abstractLanguage.AbstractMethod;
 import com.sdc.abstractLanguage.AbstractMethodVisitor;
 import com.sdc.ast.controlflow.Assignment;
 import com.sdc.ast.controlflow.Statement;
 import com.sdc.ast.expressions.Expression;
 import com.sdc.ast.expressions.New;
 import com.sdc.ast.expressions.NewArray;
 import com.sdc.ast.expressions.nestedclasses.LambdaFunction;
 import com.sdc.cfg.nodes.Node;
 import com.sdc.util.ConstructionBuilder;
 import com.sdc.util.DeclarationWorker;
 import com.sdc.util.DominatorTreeGenerator;
 import org.objectweb.asm.ClassReader;
 import org.objectweb.asm.Label;
 import org.objectweb.asm.util.Printer;
 
 import java.io.IOException;
 import java.util.List;
 
 public class KotlinMethodVisitor extends AbstractMethodVisitor {
     public KotlinMethodVisitor(final AbstractMethod abstractMethod, final String decompiledOwnerFullClassName, final String decompiledOwnerSuperClassName) {
         super(abstractMethod, decompiledOwnerFullClassName, decompiledOwnerSuperClassName);
 
         this.myLanguagePartFactory = new KotlinLanguagePartFactory();
         this.myVisitorFactory = new KotlinVisitorFactory();
         this.myLanguage = DeclarationWorker.SupportedLanguage.KOTLIN;
     }
 
     @Override
     protected boolean checkForAutomaticallyGeneratedAnnotation(String annotationName) {
         return annotationName.startsWith("Jet");
     }
 
     @Override
     protected NewArray createNewArray(final int dimensionsCount, final String type, final List<Expression> dimensions) {
         return new KotlinNewArray(dimensionsCount, DeclarationWorker.convertJavaPrimitiveClassToKotlin(type), dimensions);
     }
 
     @Override
     public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
         final String opString = Printer.OPCODES[opcode];
 
         if (opString.contains("GETSTATIC")) {
             final Expression lambdaFunction = tryVisitLambdaFunction(owner);
             if (lambdaFunction != null) {
                 myBodyStack.push(lambdaFunction);
                 return;
             }
         } else if (opString.contains("PUTFIELD") && myDecompiledOwnerFullClassName.endsWith(myDecompiledMethod.getName()) && !myDecompiledMethod.hasFieldInitializer(name)) {
             myDecompiledMethod.addInitializerToField(name, getTopOfBodyStack());
             return;
         }
 
         super.visitFieldInsn(opcode, owner, name, desc);
     }
 
     @Override
     public void visitVarInsn(final int opcode, final int var) {
         super.visitVarInsn(opcode, var);
 
         if (!myStatements.isEmpty()) {
             final Statement lastStatement = myStatements.get(myStatements.size() - 1);
             if (lastStatement instanceof Assignment
                     && ((Assignment) lastStatement).getRight() instanceof New
                     && KotlinVariable.isSharedVar(((New) ((Assignment) lastStatement).getRight()).getReturnType()))
             {
                 myStatements.remove(myStatements.size() - 1);
             }
         }
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
 
         if (opString.contains("INVOKEVIRTUAL") || opString.contains("INVOKEINTERFACE")) {
             if (!name.equals("<init>")) {
                 appendInstanceInvocation(name, hasVoidReturnType ? "" : returnType, arguments, myBodyStack.pop());
                 return;
             }
         }
 
         if (opString.contains("INVOKESPECIAL")) {
             if (name.equals("<init>")) {
                 final Expression lambdaFunction = tryVisitLambdaFunction(owner);
                 if (lambdaFunction != null) {
                     myBodyStack.pop();
                     myBodyStack.pop();
 
                     myBodyStack.push(lambdaFunction);
                     return;
                 }
 
                 myDecompiledMethod.addImport(decompiledOwnerFullClassName);
                 invocationName = ownerClassName;
                 returnType = invocationName + " ";
             } else if (!myDecompiledOwnerFullClassName.equals(decompiledOwnerFullClassName)) {
                 invocationName = "super<" + ownerClassName + ">."  + name;
             }
         }
 
         if (opString.contains("INVOKESTATIC")) {
             myDecompiledMethod.addImport(decompiledOwnerFullClassName);
             if (!ownerClassName.equals("KotlinPackage") && !ownerClassName.contains(myDecompiledMethod.getDecompiledClass().getName())) {
                 if (!decompiledOwnerFullClassName.contains(".src.")) {
                     if (ownerClassName.contains("..")) {
                         invocationName = "super<" + ownerClassName.substring(0, ownerClassName.indexOf("..")) + ">."  + name;
                         appendInstanceInvocation(invocationName, hasVoidReturnType ? "" : returnType, arguments, arguments.remove(0));
                         return;
                     } else {
                         invocationName = ownerClassName + "." + name;
                     }
                 } else {
                     ((KotlinClass) myDecompiledMethod.getDecompiledClass()).setSrcClassName(owner);
                     invocationName = name;
                 }
             } else {
                 appendInstanceInvocation(name, hasVoidReturnType ? "" : returnType, arguments, arguments.remove(0));
                 return;
             }
 
             isStaticInvocation = true;
             if (name.equals("checkParameterIsNotNull")) {
                 ((KotlinVariable) arguments.get(0)).setIsNotNull(true);
                 return;
             }
         }
 
         appendInvocationOrConstructor(isStaticInvocation, name, invocationName, hasVoidReturnType ? "" : returnType, arguments, decompiledOwnerFullClassName);
     }
 
     @Override
     public void visitLocalVariable(final String name, final String desc,
                                    final String signature, final Label start, final Label end,
                                    final int index)
     {
         if (index == 0 && name.equals("$receiver")) {
             ((KotlinMethod) myDecompiledMethod).dragReceiverFromMethodParameters();
             super.visitLocalVariable("this$", desc, signature, start, end, index);
             return;
         }
         if (index <= myDecompiledMethod.getLastLocalVariableIndex()) {
             if (!myHasDebugInformation) {
                 myHasDebugInformation = true;
             }
             myDecompiledMethod.updateVariableNameFromDebugInfo(index, name, start, end);
             return;
         }
 
         super.visitLocalVariable(name, desc, signature, start, end, index);
     }
 
     @Override
     protected ConstructionBuilder createConstructionBuilder(final List<Node> myNodes, final DominatorTreeGenerator gen) {
         return new KotlinConstructionBuilder(myNodes, gen);
     }
 
     @Override
     protected void processSuperClassConstructorInvocation(final String invocationName, final String returnType, final List<Expression> arguments) {
         ((KotlinClass) myDecompiledMethod.getDecompiledClass()).setSuperClassConstructor(new com.sdc.ast.expressions.Invocation(invocationName, returnType, arguments));
     }
 
     private Expression tryVisitLambdaFunction(final String owner) {
         final String decompiledOwnerName = DeclarationWorker.decompileFullClassName(owner);
         final int srcIndex = myDecompiledOwnerFullClassName.indexOf(".src.");
         final String methodOwner = srcIndex == -1 ? myDecompiledOwnerFullClassName : myDecompiledOwnerFullClassName.substring(0, srcIndex);
         if (!decompiledOwnerName.equals(methodOwner)
                 && decompiledOwnerName.contains(methodOwner)
                 && (decompiledOwnerName.contains(myDecompiledMethod.getName()) || myDecompiledMethod.getName().equals("invoke")))
         {
             try {
                 AbstractClassVisitor cv = myVisitorFactory.createClassVisitor(myDecompiledMethod.getTextWidth(), myDecompiledMethod.getNestSize());
                 cv.setIsLambdaFunction(true);
                cv.setClassFilesJarPath(myClassFilesJarPath);
                 ClassReader cr = AbstractClassVisitor.getInnerClassClassReader(myClassFilesJarPath, owner);
                 cr.accept(cv, 0);
                 final AbstractClass decompiledClass = cv.getDecompiledClass();
                 final LambdaFunction lf = new LambdaFunction(decompiledClass, decompiledClass.getSuperClass().replace("Impl", ""));
                 if (lf.isKotlinLambda()) {
                     return lf;
                 }
             } catch (IOException e) {
                 throw new RuntimeException(e);
             }
         }
         return null;
     }
 }
 
