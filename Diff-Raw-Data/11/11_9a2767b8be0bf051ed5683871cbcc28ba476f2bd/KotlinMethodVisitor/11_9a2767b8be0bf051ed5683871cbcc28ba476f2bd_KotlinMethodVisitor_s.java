 package com.sdc.kotlin;
 
 import com.sdc.abstractLanguage.AbstractClass;
 import com.sdc.abstractLanguage.AbstractClassVisitor;
 import com.sdc.abstractLanguage.AbstractMethod;
 import com.sdc.abstractLanguage.AbstractMethodVisitor;
 import com.sdc.ast.expressions.Expression;
 import com.sdc.ast.expressions.identifiers.Variable;
 import com.sdc.ast.expressions.nestedclasses.LambdaFunction;
 import com.sdc.util.DeclarationWorker;
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
     public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
         final String opString = Printer.OPCODES[opcode];
 
         if (opString.contains("GETSTATIC") && tryVisitLambdaFunction(owner)) {
             return;
         } else if (opString.contains("PUTFIELD") && myDecompiledOwnerFullClassName.endsWith(myDecompiledMethod.getName()) && !myDecompiledMethod.hasFieldInitializer(name)) {
             myDecompiledMethod.addInitializerToField(name, getTopOfBodyStack());
             return;
         }
 
         super.visitFieldInsn(opcode, owner, name, desc);
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
 
         if (opString.contains("INVOKEVIRTUAL") || opString.contains("INVOKEINTERFACE")) {
             if (!name.equals("<init>")) {
                if (!myBodyStack.isEmpty() && myBodyStack.peek() instanceof Variable) {
                    appendInstanceInvocation(name, returnType, arguments, myBodyStack.pop());
                    return;
                } else {
                    invocationName = "." + name;
                }
             }
         }
 
         if (opString.contains("INVOKESPECIAL")) {
             if (name.equals("<init>")) {
                 if (tryVisitLambdaFunction(owner)) {
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
             if (!ownerClassName.equals("KotlinPackage")) {
                 if (!decompiledOwnerFullClassName.contains("$src$")) {
                     invocationName = ownerClassName + "." + name;
                 } else {
                     invocationName = name;
                 }
             } else {
                 appendInstanceInvocation(name, returnType, arguments, arguments.remove(0));
                 return;
             }
 
             isStaticInvocation = true;
             if (name.equals("checkParameterIsNotNull")) {
                 ((KotlinFrame) getCurrentFrame()).addNotNullVariable(((Variable) arguments.get(0)).getIndex());
                 return;
             }
         }
 
         appendInvocationOrConstructor(isStaticInvocation, name, invocationName, returnType, arguments, decompiledOwnerFullClassName);
     }
 
     @Override
     public void visitLocalVariable(final String name, final String desc,
                                    final String signature, final Label start, final Label end,
                                    final int index)
     {
         if (index == 0 && name.equals("$receiver")) {
             myDecompiledMethod.addLocalVariableName(index, name);
             return;
         }
 
         super.visitLocalVariable(name, desc, signature, start, end, index);
     }
 
     @Override
     protected void processSuperClassConstructorInvocation(final String invocationName, final String returnType, final List<Expression> arguments) {
         ((KotlinClass) myDecompiledMethod.getDecompiledClass()).setSuperClassConstructor(new com.sdc.ast.expressions.Invocation(invocationName, returnType, arguments));
     }
 
     private boolean tryVisitLambdaFunction(final String owner) {
         final String decompiledOwnerName = DeclarationWorker.getDecompiledFullClassName(owner);
         final int srcIndex = myDecompiledOwnerFullClassName.indexOf("$src$");
         final String methodOwner = srcIndex == -1 ? myDecompiledOwnerFullClassName : myDecompiledOwnerFullClassName.substring(0, srcIndex);
        if (decompiledOwnerName.contains(methodOwner) && decompiledOwnerName.contains(myDecompiledMethod.getName())) {
             try {
                 AbstractClassVisitor cv = myVisitorFactory.createClassVisitor(myDecompiledMethod.getTextWidth(), myDecompiledMethod.getNestSize());
                 cv.setIsLambdaFunction(true);
                 ClassReader cr = AbstractClassVisitor.getInnerClassClassReader(myClassFilesJarPath, owner);
                 cr.accept(cv, 0);
                 final AbstractClass decompiledClass = cv.getDecompiledClass();
                 final LambdaFunction lf = new LambdaFunction(decompiledClass, decompiledClass.getSuperClass().replace("Impl", ""));
                 if (lf.isKotlinLambda()) {
                     myBodyStack.push(lf);
                     return true;
                 }
             } catch (IOException e) {
                 throw new RuntimeException(e);
             }
         }
         return false;
     }
 }
 
