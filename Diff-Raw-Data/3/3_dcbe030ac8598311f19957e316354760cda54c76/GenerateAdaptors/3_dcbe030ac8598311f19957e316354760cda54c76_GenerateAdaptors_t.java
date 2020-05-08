 package stormpot.jdbc;
 
 import static org.objectweb.asm.Opcodes.*;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.lang.reflect.Method;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.objectweb.asm.ClassWriter;
 import org.objectweb.asm.FieldVisitor;
 import org.objectweb.asm.MethodVisitor;
 
 public class GenerateAdaptors {
   public static void main(String[] args) throws Exception {
     generateAdaptorFactory(40);
     generateAdaptorFactory(41);
   }
   
   private static void generateAdaptorFactory(int qualifier)
       throws Exception {
     String pkg = "stormpot/jdbc/";
     String prefix = pkg + "Jdbc" + qualifier;
     String objectClass = "java/lang/Object";
     
     String factoryClass = prefix + "AdaptorFactory";
     String factoryIface = pkg + "AdaptorFactory";
     
     String connectionIface = "java/sql/Connection";
     String connectionAdaptorIface = pkg + "Jdbc41ConnectionDelegate";
     String connectionAdaptorClass = prefix + "ConnectionAdaptor";
     
     String callableStmtIface = "java/sql/CallableStatement";
     String callableStmtAdaptorIface = pkg + "Jdbc41CallableStatementDelegate";
     String callableStmtAdaptorClass = prefix + "CallableStatementAdaptor";
     
     String preparedStmtIface = "java/sql/PreparedStatement";
     String preparedStmtAdaptorIface = pkg + "Jdbc41PreparedStatementDelegate";
     String preparedStmtAdaptorClass = prefix + "PreparedStatementAdaptor";
     
     // Generating the factory itself:
     ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
     cw.visit(
         V1_6,
         ACC_SUPER,
         factoryClass,
         null,
         objectClass,
         new String[] {factoryIface});
 
     MethodVisitor ctor = cw.visitMethod(
         ACC_PUBLIC,
         "<init>",
         "()V",
         null,
         null);
     ctor.visitVarInsn(ALOAD, 0);
     ctor.visitMethodInsn(
         INVOKESPECIAL,
         "java/lang/Object",
         "<init>",
         "()V");
     ctor.visitInsn(RETURN);
     ctor.visitMaxs(0, 0);
     ctor.visitEnd();
     
     generateAdaptMethod(
         cw,
         connectionIface,
         connectionAdaptorIface,
         connectionAdaptorClass);
     generateAdaptMethod(
         cw,
         callableStmtIface,
         callableStmtAdaptorIface,
         callableStmtAdaptorClass);
     generateAdaptMethod(
         cw,
         preparedStmtIface,
         preparedStmtAdaptorIface,
         preparedStmtAdaptorClass);
     
     cw.visitEnd();
     write(factoryClass, cw);
 
     generateAdaptor(
         connectionAdaptorClass,
         connectionIface,
         connectionAdaptorIface,
         qualifier);
     generateAdaptor(
         callableStmtAdaptorClass,
         callableStmtIface,
         callableStmtAdaptorIface,
         qualifier);
     generateAdaptor(
         preparedStmtAdaptorClass,
         preparedStmtIface,
         preparedStmtAdaptorIface,
         qualifier);
   }
 
   private static void generateAdaptMethod(
       ClassWriter cw,
       String sqlIface,
       String adaptorIface,
       String adaptorClass) {
     MethodVisitor adaptCon = cw.visitMethod(
         ACC_PUBLIC,
         "adapt",
         "(L" + sqlIface + ";)L" + adaptorIface + ";",
         null,
         null);
     adaptCon.visitCode();
     adaptCon.visitTypeInsn(NEW, adaptorClass);
     adaptCon.visitInsn(DUP);
     adaptCon.visitVarInsn(ALOAD, 1);
     adaptCon.visitMethodInsn(
         INVOKESPECIAL,
         adaptorClass,
         "<init>",
         "(L" + sqlIface + ";)V");
     adaptCon.visitInsn(ARETURN);
     adaptCon.visitMaxs(0, 0);
     adaptCon.visitEnd();
   }
 
   private static void generateAdaptor(
       String adaptorName,
       String delegateName,
       String interfaceName,
       int qualifier) throws Exception {
     
     // Figure out what methods to do what with:
     Class<?> cls = Class.forName(interfaceName.replace('/', '.'));
     String rawDelegateMethodName = "_stormpot_delegate";
     Method rawDelegateMethod = cls.getDeclaredMethod(rawDelegateMethodName);
     
     Set<Method> ignored = new HashSet<Method>();
     ignored.addAll(Arrays.asList(Object.class.getDeclaredMethods()));
     ignored.add(rawDelegateMethod);
     
     Set<Method> delegators = new HashSet<Method>();
     delegators.addAll(Arrays.asList(cls.getMethods()));
     delegators.removeAll(ignored);
     
     Set<Method> throwers = new HashSet<Method>();
     if (qualifier == 40) {
      Class<?> compatibilityClass = cls.getInterfaces()[0];
      throwers.addAll(Arrays.asList(compatibilityClass.getDeclaredMethods()));
       throwers.removeAll(ignored);
       delegators.removeAll(throwers);
     }
     
     // Alright, let's generate a class:
     ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
     cw.visit(
         V1_6,
         ACC_SUPER, // package-private visibility
         adaptorName,
         null,
         "java/lang/Object",
         new String[] {interfaceName});
     
     // Add a field to store the delegate:
     String delegateDesc = "L" + delegateName + ";";
     FieldVisitor delegate = cw.visitField(
         ACC_PRIVATE + ACC_FINAL,
         "delegate",
         delegateDesc,
         null,
         null);
     delegate.visitEnd();
     
     // Write to the delegate field in the constructor:
     MethodVisitor ctor = cw.visitMethod(
         ACC_PUBLIC,
         "<init>",
         "(L" + delegateName + ";)V",
         null,
         null);
     ctor.visitVarInsn(ALOAD, 0);
     ctor.visitMethodInsn(
         INVOKESPECIAL,
         "java/lang/Object",
         "<init>",
         "()V");
     ctor.visitVarInsn(ALOAD, 0);
     ctor.visitVarInsn(ALOAD, 1);
     ctor.visitFieldInsn(
         PUTFIELD,
         adaptorName,
         "delegate",
         delegateDesc);
     ctor.visitInsn(RETURN);
     ctor.visitMaxs(0, 0);
     ctor.visitEnd();
     
     // Write the accessor method to the delegate field:
     MethodVisitor accessDelegate = cw.visitMethod(
         ACC_PUBLIC,
         rawDelegateMethodName,
         "()" + delegateDesc,
         null,
         null);
     accessDelegate.visitCode();
     accessDelegate.visitVarInsn(ALOAD, 0);
     accessDelegate.visitFieldInsn(
         GETFIELD,
         adaptorName,
         "delegate",
         delegateDesc);
     accessDelegate.visitInsn(ARETURN);
     accessDelegate.visitMaxs(0, 0);
     accessDelegate.visitEnd();
     
     // Add all the methods that delegate their calls:
     for (Method method : delegators) {
       String methodName = method.getName();
       String methodDesc = desc(method);
       int returnCode = returnCode(method);
       MethodVisitor delegator = cw.visitMethod(
           ACC_PUBLIC,
           methodName,
           methodDesc,
           null,
           exceptions(method));
       delegator.visitCode();
       delegator.visitVarInsn(ALOAD, 0);
       delegator.visitFieldInsn(
           GETFIELD,
           adaptorName,
           "delegate",
           delegateDesc);
       Class<?>[] parameterTypes = method.getParameterTypes();
       for (int i = 0; i < parameterTypes.length; i++) {
         int loadCode = loadCode(parameterTypes[i]);
         delegator.visitVarInsn(loadCode, i + 1);
       }
       delegator.visitMethodInsn(
           INVOKEINTERFACE,
           delegateName,
           methodName,
           methodDesc);
       delegator.visitInsn(returnCode);
       delegator.visitMaxs(0, 0);
       delegator.visitEnd();
     }
     
     // Add all the methods that throws:
     for (Method method : throwers) {
       String exceptionType = "java/sql/SQLFeatureNotSupportedException";
       String methodName = method.getName();
       String methodDesc = desc(method);
       MethodVisitor thrower = cw.visitMethod(
           ACC_PUBLIC,
           methodName,
           methodDesc,
           null,
           exceptions(method));
       thrower.visitCode();
       thrower.visitCode();
       thrower.visitTypeInsn(NEW, exceptionType);
       thrower.visitInsn(DUP);
       thrower.visitMethodInsn(
           INVOKESPECIAL,
           exceptionType,
           "<init>",
           "()V");
       thrower.visitInsn(ATHROW);
       thrower.visitMaxs(0, 0);
       thrower.visitEnd();
     }
     
     cw.visitEnd();
     write(adaptorName, cw);
   }
 
   private static String desc(Method method) {
     StringBuilder sb = new StringBuilder("(");
     for (Class<?> param : method.getParameterTypes()) {
       typeName(sb, param);
     }
     sb.append(')');
     typeName(sb, method.getReturnType());
     return sb.toString();
   }
 
   private static void typeName(StringBuilder sb, Class<?> param) {
     String name = param.getName().replace('.', '/');
     if (name.charAt(0) == 'j') {
       sb.append('L');
       sb.append(name);
       sb.append(';');
     } else if (name.charAt(0) == '[') {
       sb.append(name);
     } else if (name.equals("boolean")) {
       sb.append('Z');
     } else if (name.equals("byte")) {
       sb.append('B');
     } else if (name.equals("char")) {
       sb.append('C');
     } else if (name.equals("double")) {
       sb.append('D');
     } else if (name.equals("float")) {
       sb.append('F');
     } else if (name.equals("int")) {
       sb.append('I');
     } else if (name.equals("long")) {
       sb.append('J');
     } else if (name.equals("short")) {
       sb.append('S');
     } else if (name.equals("void")) {
       sb.append('V');
     } else {
       throw new AssertionError("Don't know how to name type: '" + name + "'");
     }
   }
 
   private static int returnCode(Method method) {
     Class<?> cls = method.getReturnType();
     if (cls == Void.TYPE) {
       return RETURN;
     } else if (!cls.isPrimitive()) {
       return ARETURN;
     } else if (cls == Double.TYPE) {
       return DRETURN;
     } else if (cls == Long.TYPE) {
       return LRETURN;
     } else if (cls == Float.TYPE) {
       return FRETURN;
     }
     return IRETURN;
   }
 
   private static int loadCode(Class<?> cls) {
     if (!cls.isPrimitive()) {
       return ALOAD;
     } else if (cls == Long.TYPE) {
       return LLOAD;
     } else if (cls == Double.TYPE) {
       return DLOAD;
     } else if (cls == Float.TYPE) {
       return FLOAD;
     }
     return ILOAD;
   }
 
   private static String[] exceptions(Method method) {
     Class<?>[] exceptionTypes = method.getExceptionTypes();
     String[] exceptionNames = new String[exceptionTypes.length];
     for (int i = 0; i < exceptionTypes.length; i++) {
       Class<?> cls = exceptionTypes[i];
       exceptionNames[i] = cls.getName().replace('.', '/');
     }
     return exceptionNames;
   }
 
   private static void write(String className, ClassWriter cw)
       throws FileNotFoundException, IOException {
     String filename = "target/classes/" + className + ".class";
     FileOutputStream file = new FileOutputStream(filename);
     file.write(cw.toByteArray());
     file.close();
   }
 }
