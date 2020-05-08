 package main;
 
 import java.io.IOException;
 import java.lang.instrument.ClassDefinition;
 import java.lang.instrument.ClassFileTransformer;
 import java.lang.instrument.IllegalClassFormatException;
 import java.lang.instrument.Instrumentation;
 import java.lang.reflect.InvocationTargetException;
 import java.security.ProtectionDomain;
 
 import org.objectweb.asm.ClassReader;
 import org.objectweb.asm.ClassWriter;
 
 import transform.AddTimerAdapter;
 
 import com.sun.tools.attach.AgentInitializationException;
 import com.sun.tools.attach.AgentLoadException;
 import com.sun.tools.attach.AttachNotSupportedException;
 import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
 
 public class Main {
 	private static final String CLASS_PATH = System
 			.getProperty("java.class.path");
 
 	private static final String INSTR_JAR_NAME = "TransformTest.jar";
 
 	private static final String OS_NAME = System.getProperty("os.name");
 
 	Tempt tempt = new Tempt();
 
 	private static Instrumentation instrumentation = null;
 
 	public Main() {
 		System.out.println(Main.class.getClassLoader());
 		printHello("Hello");
 
 		tempt.tempt();
 
 		tempt.answer("Not on you life");
 		printCL();
 	}
 
 	private void printHello(String msg) {
 		System.out.println(msg);
 	}
 
 	public void printCL() {
 		System.out.println("Main ClassLoader: " + Main.class.getClassLoader());
 	}
 
 	public static void premain(String agentArgs, Instrumentation inst) {
 		System.out.println("premain");
 		inst.addTransformer(new ClassFileTransformer() {
 			public byte[] transform(ClassLoader l, String name, Class<?> c,
 					ProtectionDomain d, byte[] b)
 					throws IllegalClassFormatException {
 				ClassReader cr = new ClassReader(b);
 				ClassWriter cw = new ClassWriter(cr, 0);
 				// ClassVisitor cc = new CheckClassAdapter(cw);
 				// ClassVisitor tv =
 				// new TraceClassVisitor(cc, new PrintWriter(System.out));
 
 				AddTimerAdapter adapter = new AddTimerAdapter(cw);
 				cr.accept(adapter, 0);
 
 				return cw.toByteArray();
 			}
 		});
 	}
 
 	public static void agentmain(String agentArgs, Instrumentation inst) {
 		instrumentation = inst;
 		System.out.println("agent running");
 		premain(agentArgs, inst);
 	}
 
 	public static void redefineClasses(ClassDefinition... definitions)
 			throws Exception {
 		if (instrumentation == null) {
 			throw new RuntimeException(
 					"Agent has not been started. Do not have handle to instrumentation");
 		}
 
 		instrumentation.redefineClasses(definitions);
 	}
 
 	/**
 	 * @param args
 	 * @throws ClassNotFoundException
 	 * @throws NoSuchMethodException
 	 * @throws SecurityException
 	 * @throws InvocationTargetException
 	 * @throws IllegalAccessException
 	 * @throws IllegalArgumentException
 	 * @throws InstantiationException
 	 * @throws IOException
 	 * @throws AttachNotSupportedException
 	 * @throws AgentInitializationException
 	 * @throws AgentLoadException
 	 */
 	@SuppressWarnings("unchecked")
 	public static void main(String[] args) throws Exception {
 		if (args.length > 0) {
 			System.out.println(args[0]);
 		}
 		if (Class.class.getClassLoader() == null && args.length == 0) {
 			System
 					.setProperty("java.system.class.loader",
 							"main.MyClassLoader");
			VirtualMachineDescriptor vmDescriptor = null;
			for(VirtualMachineDescriptor descr: VirtualMachine.list()) {
				System.out.println("descriptor: " + descr.displayName());
				if(descr.displayName().endsWith(INSTR_JAR_NAME) || descr.displayName().endsWith("Main")) {
					vmDescriptor = descr;
					break;
				}
			}
			VirtualMachine vm = VirtualMachine.attach(vmDescriptor);
 			String splitter = OS_NAME.equalsIgnoreCase("Windows") ? ";" : ":";
 			String agentPath = null;
 			for (String entry : CLASS_PATH.split(splitter)) {
 				System.out.println(entry);
 				if (entry.endsWith(INSTR_JAR_NAME)) {
 					agentPath = entry;
 					break;
 				}
 			}
 			if (agentPath != null) {
 				System.out.println(agentPath);
 				vm.loadAgent(agentPath);
 			}
 			vm.detach();
 			// try {
 			// MyClassLoader loader = (MyClassLoader)
 			// ClassLoader.getSystemClassLoader();
 			// // loader.addFileToClasspath("TransformTest.jar");
 
 			// MyClassLoader loader = new
 			// MyClassLoader(Main.class.getClassLoader());
 			// Class inst = loader.loadClass("main.Main");
 			// Constructor con = inst.getConstructor();
 			// con.newInstance();
 			// Method printCL = inst.getMethod("printCL", null);
 			// printCL.invoke(null, new Object[0]);
 			// Class mainArgType[] = { (new String[0]).getClass() };
 			//
 			// Method mainm = inst.getMethod("main", mainArgType);
 			// String[] args2 = {"One"};
 			// Object argsArray[] = {args2};
 			// mainm.invoke(null, argsArray);
 			// } else {
 
 			// } catch (Exception exception) {
 			// exception.printStackTrace();
 			// }
 
 			Main main = new Main();
 			main.printHello("Goodbye");
 		}
 
 		// System.out.println(main.test);
 	}
 }
