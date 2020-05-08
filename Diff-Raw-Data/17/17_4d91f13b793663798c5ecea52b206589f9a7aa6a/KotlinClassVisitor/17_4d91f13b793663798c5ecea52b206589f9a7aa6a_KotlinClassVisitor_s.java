 package com.sdc.languages.kotlin.visitors;
 
 import com.sdc.languages.general.languageParts.Method;
 import com.sdc.languages.general.visitors.GeneralClassVisitor;
 import com.sdc.languages.general.visitors.GeneralMethodVisitor;
 import com.sdc.languages.kotlin.languageParts.KotlinClass;
 import com.sdc.languages.kotlin.languageParts.KotlinLanguagePartFactory;
 import com.sdc.util.DeclarationWorker;
 import org.objectweb.asm.*;
 
 import java.io.IOException;
 
 public class KotlinClassVisitor extends GeneralClassVisitor {
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
 
         if (name.contains("$src$") || name.endsWith("Package")) {
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
 
         final GeneralMethodVisitor methodVisitor = (GeneralMethodVisitor) super.visitMethod(access, name, desc, signature, exceptions);
 
         if (name.equals("<init>")) {
             final Method decompiledMethod = methodVisitor.getDecompiledMethod();
             final KotlinClass kotlinClass = getKotlinClass();
             kotlinClass.setConstructor(decompiledMethod);
             kotlinClass.removeMethod(decompiledMethod);
         }
 
         return methodVisitor;
     }
 
     @Override
     public void visitEnd() {
         super.visitEnd();
 
         final String decompiledClassName = getKotlinClass().getSrcClassName();
         if (myDecompiledClass.getName().equals("KotlinPackage") && decompiledClassName != null) {
             try {
                 GeneralClassVisitor cv = myVisitorFactory.createClassVisitor(myDecompiledClass.getTextWidth(), myDecompiledClass.getNestSize());
                 cv.setClassFilesJarPath(myClassFilesJarPath);
                 ClassReader cr = GeneralClassVisitor.getInnerClassClassReader(myClassFilesJarPath, decompiledClassName);
                 cr.accept(cv, 0);
                 myDecompiledClass = cv.getDecompiledClass();
             } catch (IOException e) {
                 throw new RuntimeException(e);
             }
         }
     }
 
     private KotlinClass getKotlinClass() {
         return (KotlinClass) myDecompiledClass;
     }
 }
