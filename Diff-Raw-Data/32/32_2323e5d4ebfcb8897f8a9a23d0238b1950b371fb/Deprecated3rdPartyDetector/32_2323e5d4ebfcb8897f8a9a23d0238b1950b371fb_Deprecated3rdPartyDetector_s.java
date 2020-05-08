 package com.youdevise.fbplugins.deprecate3rdparty;
 
 import java.util.List;
 
 import org.objectweb.asm.AnnotationVisitor;
 import org.objectweb.asm.Attribute;
 import org.objectweb.asm.ClassVisitor;
 import org.objectweb.asm.FieldVisitor;
 import org.objectweb.asm.Label;
 import org.objectweb.asm.MethodVisitor;
 
 import edu.umd.cs.findbugs.BugReporter;
 import edu.umd.cs.findbugs.Detector;
 import edu.umd.cs.findbugs.asm.FBClassReader;
 import edu.umd.cs.findbugs.ba.ClassContext;
 import edu.umd.cs.findbugs.classfile.CheckedAnalysisException;
 import edu.umd.cs.findbugs.classfile.ClassDescriptor;
 import edu.umd.cs.findbugs.classfile.Global;
 
 public class Deprecated3rdPartyDetector implements Detector {
 
     private final Detector thisPluginDetector;
     private final BugReporter bugReporter;
     private final DeprecatedSettings settings;
 
     public Deprecated3rdPartyDetector(Detector thisPluginDetector, BugReporter bugReporter, DeprecatedSettings settings) {
         this.thisPluginDetector = thisPluginDetector;
         this.bugReporter = bugReporter;
         this.settings = settings;
     }
 
     @Override
     public void report() { }
 
     @Override
     public void visitClassContext(ClassContext classContext) {
         List<String> deprecatedClasses = settings.deprecatedClasses();
         
         ClassDescriptor classDescriptor = classContext.getClassDescriptor();
         
         FBClassReader reader = null;
         DeprecatedClassVisitor deprecatedClassVisitor = new DeprecatedClassVisitor(deprecatedClasses);
         try {
             reader = Global.getAnalysisCache().getClassAnalysis(FBClassReader.class, classDescriptor);
             reader.accept(deprecatedClassVisitor, 0);
         } catch (CheckedAnalysisException e) {
             e.printStackTrace();
         }
     }
 
     private static class DeprecatedClassVisitor implements ClassVisitor, FieldVisitor, MethodVisitor {
 
         private final List<String> deprecatedClasses;
 
         public DeprecatedClassVisitor(List<String> deprecatedClasses) {
             this.deprecatedClasses = deprecatedClasses;
         }
 
         @Override
         public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
             
         }
 
         @Override
         public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
             return null;
         }
 
         @Override
         public void visitAttribute(Attribute attribute) {
             
         }
 
         @Override
         public void visitEnd() {
             
         }
 
         @Override
        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
             return this;
         }
 
         @Override
         public void visitInnerClass(String name, String outerName, String innerName, int access) {
             
         }
 
         @Override
         public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
             return this;
         }
 
         @Override
         public void visitOuterClass(String owner, String name, String desc) {
             
         }
 
         @Override
         public void visitSource(String source, String debug) {
             
         }
 
         @Override
         public AnnotationVisitor visitAnnotationDefault() {
             return null;
         }
 
         @Override
         public void visitCode() {
             
         }
 
         @Override
         public void visitFieldInsn(int arg0, String arg1, String arg2, String arg3) {
             
         }
 
         @Override
         public void visitFrame(int arg0, int arg1, Object[] arg2, int arg3, Object[] arg4) {
             
         }
 
         @Override
         public void visitIincInsn(int arg0, int arg1) {
             
         }
 
         @Override
         public void visitInsn(int arg0) {
             
         }
 
         @Override
         public void visitIntInsn(int arg0, int arg1) {
             
         }
 
         @Override
         public void visitJumpInsn(int arg0, Label arg1) {
             
         }
 
         @Override
         public void visitLabel(Label arg0) {
             
         }
 
         @Override
         public void visitLdcInsn(Object arg0) {
             
         }
 
         @Override
         public void visitLineNumber(int arg0, Label arg1) {
             
         }
 
         @Override
         public void visitLocalVariable(String arg0, String arg1, String arg2, Label arg3, Label arg4, int arg5) {
             
         }
 
         @Override
         public void visitLookupSwitchInsn(Label arg0, int[] arg1, Label[] arg2) {
             
         }
 
         @Override
         public void visitMaxs(int arg0, int arg1) {
             
         }
 
         @Override
         public void visitMethodInsn(int arg0, String arg1, String arg2, String arg3) {
             
         }
 
         @Override
         public void visitMultiANewArrayInsn(String arg0, int arg1) {
             
         }
 
         @Override
         public AnnotationVisitor visitParameterAnnotation(int arg0, String arg1, boolean arg2) {
             return null;
         }
 
         @Override
         public void visitTableSwitchInsn(int arg0, int arg1, Label arg2, Label[] arg3) {
             
         }
 
         @Override
         public void visitTryCatchBlock(Label arg0, Label arg1, Label arg2, String arg3) {
             
         }
 
         @Override
         public void visitTypeInsn(int arg0, String arg1) {
             
         }
 
         @Override
         public void visitVarInsn(int arg0, int arg1) {
             
         }
         
     }
     
 }
