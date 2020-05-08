 package com.psddev.dari.db;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.objectweb.asm.AnnotationVisitor;
 import org.objectweb.asm.ClassReader;
 import org.objectweb.asm.FieldVisitor;
 import org.objectweb.asm.MethodAdapter;
 import org.objectweb.asm.MethodVisitor;
 import org.objectweb.asm.Opcodes;
 import org.objectweb.asm.Type;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.psddev.dari.util.ClassEnhancer;
 import com.psddev.dari.util.ObjectUtils;
 
 /**
  * Enables lazily loading fields that are expensive to initialize.
  * If the project uses Apache Maven to manage the build and inherits
  * from {@code com.psddev:dari-parent}, this enhancer is automatically
  * applied to all model classes.
  *
  * <p>Note that this is an optional performance optimization. It's always
  * safe not to enable it.</p>
  *
  * @see <a href="http://maven.apache.org/">Apache Maven</a>
  */
 public class LazyLoadEnhancer extends ClassEnhancer {
 
     private static final String ANNOTATION_DESCRIPTOR = Type.getDescriptor(LazyLoad.class);
     private static final Logger LOGGER = LoggerFactory.getLogger(LazyLoadEnhancer.class);
 
     private boolean missingClasses;
     private String enhancedClassName;
     private boolean alreadyEnhanced;
     private final Set<String> transientFields = new HashSet<String>();
     private final Set<String> recordableFields = new HashSet<String>();
 
     // --- ClassEnhancer support ---
 
     // Returns the class associated with the given className
     // only if it's compatible with Recordable.
     private Class<?> findRecordableClass(String className) {
         Class<?> objectClass = ObjectUtils.getClassByName(className.replace('/', '.'));
 
         if (objectClass == null) {
             LOGGER.warn("Can't find [{}] referenced by [{}]!", className, enhancedClassName);
             missingClasses = true;
             return null;
 
         } else if (Recordable.class.isAssignableFrom(objectClass)) {
             return objectClass;
 
         } else {
             return null;
         }
     }
 
     @Override
     public boolean canEnhance(ClassReader reader) {
         enhancedClassName = reader.getClassName();
 
         return !enhancedClassName.startsWith("com/psddev/dari/") &&
                 findRecordableClass(enhancedClassName) != null;
     }
 
     @Override
     public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
         if (!alreadyEnhanced && desc.equals(ANNOTATION_DESCRIPTOR)) {
             alreadyEnhanced = true;
         }
 
         return super.visitAnnotation(desc, visible);
     }
 
     @Override
     public FieldVisitor visitField(
             int access,
             String name,
             String desc,
             String signature,
             Object value) {
 
        if ((access & Opcodes.ACC_TRANSIENT) > 0) {
             transientFields.add(name);
 
         } else {
             Class<?> objectClass = findRecordableClass(Type.getType(desc).getClassName());
 
             if (objectClass != null) {
                 Recordable.Embedded embedded = objectClass.getAnnotation(Recordable.Embedded.class);
 
                 if (embedded == null || !embedded.value()) {
                     recordableFields.add(name);
                 }
             }
         }
 
         return super.visitField(access, name, desc, signature, value);
     }
 
     @Override
     public MethodVisitor visitMethod(
             int access,
             String name,
             String desc,
             String signature,
             String[] exceptions) {
 
         MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
 
         if (alreadyEnhanced) {
             return visitor;
 
         } else {
             return new MethodAdapter(visitor) {
                 @Override
                 public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                     if (!transientFields.contains(name)) {
                         if (opcode == Opcodes.GETFIELD) {
                             if (recordableFields.contains(name)) {
                                 visitInsn(Opcodes.DUP);
                                 visitMethodInsn(Opcodes.INVOKEINTERFACE, "com/psddev/dari/db/Recordable", "getState", "()Lcom/psddev/dari/db/State;");
                                 visitInsn(Opcodes.DUP);
                                 visitLdcInsn(name);
                                 visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/psddev/dari/db/State", "beforeFieldGet", "(Ljava/lang/String;)V");
                                 visitLdcInsn(name);
                                 visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/psddev/dari/db/State", "resolveReference", "(Ljava/lang/String;)V");
 
                             } else {
                                 visitInsn(Opcodes.DUP);
                                 visitMethodInsn(Opcodes.INVOKEINTERFACE, "com/psddev/dari/db/Recordable", "getState", "()Lcom/psddev/dari/db/State;");
                                 visitLdcInsn(name);
                                 visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/psddev/dari/db/State", "beforeFieldGet", "(Ljava/lang/String;)V");
                             }
 
                         } else if (opcode == Opcodes.PUTFIELD &&
                                 recordableFields.contains(name)) {
                             visitInsn(Opcodes.SWAP);
                             visitInsn(Opcodes.DUP);
                             visitMethodInsn(Opcodes.INVOKEINTERFACE, "com/psddev/dari/db/Recordable", "getState", "()Lcom/psddev/dari/db/State;");
                             visitLdcInsn(name);
                             visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/psddev/dari/db/State", "resolveReference", "(Ljava/lang/String;)V");
                             visitInsn(Opcodes.SWAP);
                         }
                     }
 
                     super.visitFieldInsn(opcode, owner, name, desc);
                 }
             };
         }
     }
 
     @Override
     public void visitEnd() {
         if (!missingClasses && !alreadyEnhanced) {
             AnnotationVisitor annotation = super.visitAnnotation(ANNOTATION_DESCRIPTOR, true);
 
             annotation.visitEnd();
         }
 
         super.visitEnd();
     }
 }
