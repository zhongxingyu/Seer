 package bfrc.java;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import javassist.CannotCompileException;
 import javassist.CtClass;
 
 public class JITBackend extends AbstractJavassistBackend<RuntimeException> {
 
 	public JITBackend() {
		this(null);
 	}
 
 	public JITBackend(String className) {
		super("Temp");
 	}
 
 	@Override
 	public void write(CtClass clazz) {
 		Method m;
 		try {
 			Class<?> compiled = clazz.toClass();
 			m = compiled.getMethod("main");
 		} catch (CannotCompileException | NoSuchMethodException e) {
 			throw new IllegalArgumentException("invalid class", e);
 		}
 		// call static method
 		try {
 			m.invoke(null);
 		} catch (IllegalAccessException | IllegalArgumentException
 				| InvocationTargetException e) {
 			throw new RuntimeException("exception calling compiled code", e);
 		}
 	}
 }
