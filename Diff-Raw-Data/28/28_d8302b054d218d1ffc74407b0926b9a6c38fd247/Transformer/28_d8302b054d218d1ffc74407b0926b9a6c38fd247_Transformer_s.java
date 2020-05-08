 package transform;
 
 import java.lang.instrument.ClassFileTransformer;
 import java.lang.instrument.IllegalClassFormatException;
 import java.security.ProtectionDomain;
 import org.objectweb.asm.ClassReader;
 import org.objectweb.asm.ClassVisitor;
 import org.objectweb.asm.ClassWriter;
 import agent.Settings;
 
 /**
  * A {@link ClassFileTransformer} used by the agent to instrument user program
  * classes upon loading. The classes of this project are filtered and not
  * transformed.
  *
  * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
  */
 public class Transformer implements ClassFileTransformer {
 
     @Override
     public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
             ProtectionDomain protectionDomain, byte[] classfileBuffer)
             throws IllegalClassFormatException {
 
         /* Do not instrument project's classes at runtime. */
         boolean shouldInstrument = !(className.startsWith("agent/") || className.startsWith("comms")
                 || className.startsWith("transform/")
 
                 /* The two boolean expressions below are related to issue 004. */
                 /* These prevent stack overflows when trying to notify event subscribers. */
                 || className.startsWith("java/util/")   /* TODO: To be removed. */
                 || className.startsWith("sun/reflect")  /* TODO: To be removed. */
 
                 /* Takes care of stack overflows when getting & storing IDs, null pointer exs. */
                || className.startsWith("com/google/common/collect/")); /* TODO: To be removed. */
 
         Settings.INSTANCE.println("Loaded " + ((shouldInstrument) ? "[i]" : "   ")
                 + " " + className);
 
         return (shouldInstrument) ? transformClass(classfileBuffer) : classfileBuffer;
     }
 
     /** Instruments classes' bytecode using an ASM {@link ClassVisitor}. */
     public static byte[] transformClass(byte[] classfileBuffer) {
         ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
         ClassVisitor sc = new DataCollector(writer);
         new ClassReader(classfileBuffer).accept(sc, 0);
 
         return writer.toByteArray();
     }
 }
