 package org.jackie.asmtools.test;
 
 import sun.misc.HexDumpEncoder;
import org.testng.annotations.Test;
 import org.jackie.asmtools.ClassBuilder;
import org.jackie.asmtools.MethodBuilder;
 import org.jackie.asmtools.ConstructorBuilder;
import static org.jackie.utils.JavaHelper.getMethod;
import static org.jackie.utils.JavaHelper.getConstructor;
import org.jackie.utils.Assert;
import org.objectweb.asm.Type;
 import org.objectweb.asm.ClassReader;
 import org.objectweb.asm.util.TraceClassVisitor;
 
 import java.io.PrintStream;
 import java.io.PrintWriter;
 
 /**
  * @author Patrik Beno
  */
 public class ClassBuilderTest {
 
 	@Test
 	public void run() throws Exception {
 		ClassBuilder cb = new ClassBuilder() {
 
 			public String classname() {
 				return "test.Generated";
 			}
 
 			protected Class superclass() {
 				return Sample.class;
 			}
 
 			protected void constructors() {
 				execute(new ConstructorBuilder(this) {
 					protected void body() {
 						// todo provide API for constructors
 						mv.visitVarInsn(ALOAD, 0);
 						mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
 						mv.visitInsn(RETURN);
 						mv.visitMaxs(1, 1);
 					}
 				});
 			}
 
 			protected void methods() {
 				execute(new MethodBuilder(this, "test", void.class) {
 					protected void body() {
 						get(field(System.class, "out"));
 						push("Hello, World!");
 						invoke(method(PrintStream.class, "println", String.class));
 						doreturn();
 					}
 				});
 			}
 		};
 
 		final byte[] bytes = cb.build();
 		new HexDumpEncoder().encode(bytes, System.out);
 
 		ClassReader cr = new ClassReader(bytes);
 		cr.accept(new TraceClassVisitor(new PrintWriter(System.out)), 0);
 
 		Class c = Class.forName("test.Generated", true, new ClassLoader(getClass().getClassLoader()) {
 			protected Class<?> findClass(String name) throws ClassNotFoundException {
 				return defineClass(name, bytes, 0, bytes.length);
 			}
 		});
 
 		Sample sample = (Sample) c.newInstance();
 		sample.test();
 
 	}
 
 }
