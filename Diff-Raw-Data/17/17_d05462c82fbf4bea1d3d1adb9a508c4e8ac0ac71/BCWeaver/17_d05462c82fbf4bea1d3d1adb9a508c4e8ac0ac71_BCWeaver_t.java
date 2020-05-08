 package testenough.weaver;
 
 import javassist.*;
 import javassist.bytecode.MethodInfo;
 import testenough.Configuration;
 
 import java.io.ByteArrayInputStream;
 import java.lang.instrument.ClassFileTransformer;
 import java.lang.instrument.IllegalClassFormatException;
 import java.security.ProtectionDomain;
 
 public class BCWeaver implements ClassFileTransformer {
 
     private Configuration configuration;
 
     public BCWeaver(Configuration configuration) {
         this.configuration = configuration;
     }
 
     public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
         if (!configuration.shouldWeave(className)) return null;
         return weave(loader, classfileBuffer);
     }
 
     private byte[] weave(ClassLoader loader, byte[] byteCode) {
         try {
             CtClass ctClass = byteCodeAsClass(loader, copyOf(byteCode));
             for (CtMethod method : ctClass.getDeclaredMethods()) {
                 if (shouldEnhance(method)) {
                     try {
                         method.insertBefore(codeToBeInserted());
                     } catch (CannotCompileException e) {
                         System.out.println("Couldn't instrument: " + method + ". Caused by: " + e);
                     }
                 }
             }
             return ctClass.toBytecode();
         } catch (Exception e) {
            System.out.println("Could not instrument. " + e.getMessage());
             throw new RuntimeException("Could not instrument", e);
         }
     }
 
     private byte[] copyOf(byte[] byteCode) {
         byte[] copy = new byte[byteCode.length];
         System.arraycopy(byteCode, 0, copy, 0, byteCode.length);
         return copy;
     }
 
     private boolean shouldEnhance(CtMethod method) {
         MethodInfo methodInfo = method.getMethodInfo();
         return methodInfo.isMethod() && !methodInfo.isStaticInitializer() && !methodInfo.isConstructor() && !method.isEmpty() && !Modifier.isAbstract(method.getModifiers());
     }
 
     private CtClass byteCodeAsClass(ClassLoader loader, byte[] copy) throws Exception {
         Class<?> aClass = loader.loadClass(classPoolClassName());
         ClassPool aDefault = (ClassPool) aClass.getMethod("getDefault").invoke(null);
         aDefault.appendClassPath(new ClassClassPath(loader.loadClass(sampleClassFromProd())));
         return aDefault.makeClass(new ByteArrayInputStream(copy));
     }
 
     private String classPoolClassName() {
         //Any reference to the class object in order to get the full qualified name programatically loads the class to be loaded. We want to load it using the given class loader.
         return "javassist.ClassPool";
     }
 
     private String sampleClassFromProd() {
         return configuration.sampleClassFromProd();
     }
 
     private String codeToBeInserted() {
         return configuration.codeToBeInserted();
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         BCWeaver bcWeaver = (BCWeaver) o;
         return !(configuration != null ? !configuration.equals(bcWeaver.configuration) : bcWeaver.configuration != null);
     }
 
     @Override
     public int hashCode() {
         return configuration != null ? configuration.hashCode() : 0;
     }
 }
