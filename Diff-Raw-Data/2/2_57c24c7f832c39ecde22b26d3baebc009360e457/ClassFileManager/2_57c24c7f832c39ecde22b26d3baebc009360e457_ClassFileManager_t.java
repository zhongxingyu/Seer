 package compiler.java;
 
 import javax.tools.FileObject;
 import javax.tools.ForwardingJavaFileManager;
 import javax.tools.JavaFileObject;
 import javax.tools.StandardJavaFileManager;
 import java.io.IOException;
 import java.security.SecureClassLoader;
 
 public class ClassFileManager extends
         ForwardingJavaFileManager<StandardJavaFileManager> {
     /**
      * Instance of JavaClassObject that will store the
      * compiled bytecode of our class
      */
     private JavaClassObject jClassObject;
 
     /**
      * Will initialize the manager with the specified
      * standard java file manager
      *
      * @param standardManger
      */
     public ClassFileManager(StandardJavaFileManager
                                     standardManager) {
         super(standardManager);
     }
 
     /**
      * Will be used by us to get the class loader for our
      * compiled class. It creates an anonymous class
      * extending the SecureClassLoader which uses the
      * byte code created by the compiler and stored in
      * the JavaClassObject, and returns the Class for it
      */
     @Override
     public ClassLoader getClassLoader(javax.tools.JavaFileManager.Location location) {
         return new SecureClassLoader() {
             @Override
             protected Class<?> findClass(String name)
                     throws ClassNotFoundException {
                 byte[] b = jClassObject.getBytes();
                 return super.defineClass(name, jClassObject
                         .getBytes(), 0, b.length);
             }
         };
     }
 
     /**
      * Gives the compiler an instance of the JavaClassObject
      * so that the compiler can write the byte code into it.
      */
     @Override
    public JavaFileObject getJavaFileForOutput(javax.tools.JavaFileManager.Location location,
                                                String className, JavaFileObject.Kind kind, FileObject sibling)
             throws IOException {
         jClassObject = new JavaClassObject(className, kind);
         return jClassObject;
     }
 }
 
