 package dapij;
 
 import java.io.IOException;
 import java.lang.instrument.ClassFileTransformer;
 import java.lang.instrument.IllegalClassFormatException;
 import java.lang.instrument.Instrumentation;
 import java.security.ProtectionDomain;
 import org.objectweb.asm.ClassReader;
 import org.objectweb.asm.ClassVisitor;
 import org.objectweb.asm.ClassWriter;
 
 /**
  *
  * @author Nikolay Pulev
  */
 public class Dapij implements ClassFileTransformer {
     
     @Override
     public byte[] transform(ClassLoader loader, String className,
             Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
             byte[] classfileBuffer) throws IllegalClassFormatException {
         
         /* Do not instrument agent classes */
         // TODO: should remove guava classes from blacklist!
         if (className.startsWith("dapij/") || 
                 className.startsWith("com/google/common/collect/") || 
                 className.startsWith("java/io/") ) {
             System.out.println("Did not instument " + className + "!");
             return classfileBuffer;
         }
         
         System.out.println("Instrumenting " + className + " ...");
         return transformClass(classfileBuffer);
     }
 
     /**
      * Reads and instruments class bytecode using a StatsCollector visitor.
      */
     static byte[] transformClass(byte[] classfileBuffer) {
         ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
         /*
          * Uncomment lines below and pass tcv to the constructor of sc_visitor
          * to print the instrumented bytecode on System.out.
          */
         //TraceClassVisitor tcv = new TraceClassVisitor(writer,
         //        new PrintWriter(System.out));
         
         ClassVisitor sc_visitor = new StatsCollector(writer);
         new ClassReader(classfileBuffer).accept(sc_visitor, 0);
         return writer.toByteArray();
     }
 
     public static void premain(String argString, Instrumentation inst)
             throws IOException {
         int i = 0;
         /* Split arglsit on one or more whitespaces */
        String[] args = argString.split("\\s+", 0);
        
        while (i < args.length) {
            if (args[i].equals("-o") && i + 1 < args.length) {
                Settings.INSTANCE.setProp(Settings.XML_OUT_SETT, args[++i]);
             }
            /* Add more arguments if needed */
            //else if (args[i].equals(...)) {
            //...
            //} 
            i++;
         }
        
         Settings.INSTANCE.addBreakpt(
                 new Breakpoint("HelloAzura.java", 37, true));
 
         inst.addTransformer(new Dapij());
     }
 }
