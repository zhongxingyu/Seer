 package com.sdc.kotlin;
 
 import com.sdc.abstractLanguage.AbstractClassVisitor;
 import com.sdc.abstractLanguage.AbstractMethod;
 import com.sdc.abstractLanguage.AbstractMethodVisitor;
 import com.sdc.util.DeclarationWorker;
 import org.objectweb.asm.*;
 
 public class KotlinClassVisitor extends AbstractClassVisitor {
     private static final String DEFAULT_EXTENDED_CLASS = "java/lang/Object";
     private static final String DEFAULT_IMPLEMENTED_INTERFACE = "jet/JetObject";
 
     public KotlinClassVisitor(final int textWidth, final int nestSize) {
         super(textWidth, nestSize);
 
         this.myLanguagePartFactory = new KotlinLanguagePartFactory();
         this.myVisitorFactory = new KotlinVisitorFactory();
         this.myLanguage = DeclarationWorker.SupportedLanguage.KOTLIN;
     }
 
     @Override
     protected String getDefaultImplementedInterface() {
         return DEFAULT_IMPLEMENTED_INTERFACE;
     }
 
     @Override
     protected String getDefaultExtendedClass() {
         return DEFAULT_EXTENDED_CLASS;
     }
 
     @Override
     protected boolean checkForAutomaticallyGeneratedAnnotation(String annotationName) {
         return annotationName.startsWith("Jet");
     }
 
     @Override
     public void visit(final int version, final int access, final String name
             , final String signature, final String superName, final String[] interfaces)
     {
         super.visit(version, access, name, signature, superName, interfaces);
 
//        final String className = getClassName(name);
        if (name.contains("$src$")) {
             myDecompiledClass.setIsNormalClass(false);
         }
     }
 
     @Override
     public MethodVisitor visitMethod(final int access, final String name, final String desc
             , final String signature, final String[] exceptions)
     {
         if ((name.startsWith("set") || name.startsWith("get")) && myDecompiledClass.hasField(name.substring(3))) {
             return null;
         }
 
         final AbstractMethodVisitor methodVisitor = (AbstractMethodVisitor) super.visitMethod(access, name, desc, signature, exceptions);
 
         if (name.equals("<init>")) {
             final AbstractMethod decompiledMethod = methodVisitor.getDecompiledMethod();
             final KotlinClass kotlinClass = getKotlinClass();
             kotlinClass.setConstructor(decompiledMethod);
             kotlinClass.removeMethod(decompiledMethod);
         }
 
         return methodVisitor;
     }
 
     private KotlinClass getKotlinClass() {
         return (KotlinClass) myDecompiledClass;
     }
 }
