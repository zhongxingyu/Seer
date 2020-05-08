 package com.foursquare.heapaudit;
 
 import com.sun.tools.attach.VirtualMachine;
 import java.io.IOException;
 import java.lang.instrument.ClassFileTransformer;
 import java.lang.instrument.Instrumentation;
 import java.lang.instrument.UnmodifiableClassException;
 import java.security.ProtectionDomain;
 import java.util.ArrayList;
 import org.objectweb.asm.ClassReader;
 import org.objectweb.asm.ClassWriter;
 
 public class HeapAudit extends HeapUtil implements ClassFileTransformer {
 
     static {
 	/*
         Runtime.getRuntime().addShutdownHook(new Thread() {
 
 		@Override public void run() {
 
 		    HeapRecorder.instrumentation = null;
 
 		}
 
 	    });
 	*/
     }
 
    public static void main(String[] args) throws Exception {
 
 	StringBuffer s = new StringBuffer(args.length > 1 ? args[1] : "");
 
 	for (int i = 2; i < args.length; ++i) {
 
 	    s.append(' ');
 
 	    s.append(args[i]);
 
 	}
 
 	load(args[0],
 	     s.toString());
 
     }
 
     public static void load(String pid,
			    String args) throws Exception {
 
 	VirtualMachine vm = VirtualMachine.attach(pid);
 
 	vm.loadAgent(HeapAudit.class.getProtectionDomain().getCodeSource().getLocation().getPath(),
 		     args);
 
 	vm.detach();
 
     }
 
     public static void agentmain(String args,
 				 Instrumentation instrumentation) throws UnmodifiableClassException {
 
 	main(args,
 	     instrumentation,
 	     true);
 
     }
 
     public static void premain(String args,
 			       Instrumentation instrumentation) throws UnmodifiableClassException {
 
 	main(args,
 	     instrumentation,
 	     false);
 
     }
 
     private static void main(String args,
 			     Instrumentation instrumentation,
 			     boolean dynamic) throws UnmodifiableClassException {
 
         HeapSettings.parse(args,
 			   dynamic);
         
         HeapRecorder.isAuditing = true;
 
 	HeapRecorder.instrumentation = instrumentation;
 
 	ClassFileTransformer transformer = new HeapAudit();
 
 	instrumentation.addTransformer(transformer,
 				       true);
 
 	if (instrumentation.isRetransformClassesSupported()) {
	    //(dynamic || (HeapAudit.class.getClassLoader() == null))) {
 
 	    ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
 
 	    for (Class<?> c: instrumentation.getAllLoadedClasses()) {
 
 		if (instrumentation.isModifiableClass(c)) {
 
 		    classes.add(c);
 
 		}
 
 	    }
 
 	    instrumentation.retransformClasses(classes.toArray(new Class<?>[classes.size()]));
 
 	}
 
 	if (dynamic) {
 
 	    instrumentation.removeTransformer(transformer);
 
 	}
 
     }
 
     // The following is the main entry point for transforming the bytecode.
 
     public byte[] transform(ClassLoader loader,
 			    String className,
 			    Class<?> classBeingRedefined,
 			    ProtectionDomain protectionDomain,
 			    byte[] classfileBuffer) {
 
 	if (HeapSettings.avoidClass(className)) {
 
 	    return null;
 
 	}
 
 	ClassReader cr = new ClassReader(classfileBuffer);
 
 	ClassWriter cw = new ClassWriter(cr,
 					 ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
 
 	cr.accept(new HeapClass(cw,
 				className),
 	          ClassReader.SKIP_FRAMES);
 
 	return cw.toByteArray();
 
     }
 
 }
