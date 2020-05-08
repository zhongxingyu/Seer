 package net.vidageek.tellme;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import javassist.CannotCompileException;
 import javassist.ClassPool;
 import javassist.CtClass;
 import javassist.CtMethod;
 import net.vidageek.tellme.exceptions.ProxifyException;
 
 public final class Proxifier {
 
	public void proxify(String className) {
 		ClassPool pool = ClassPool.getDefault();
 		try {
 			CtClass ctClassToProxify = pool.get(className);
 			Collection<CtMethod> methodsToProxify = findMethodsToProxify(ctClassToProxify);
 			proxifyMethods(methodsToProxify);
			ctClassToProxify.toClass(); // Loads the class into the current ClassLoader
 		} catch (Exception e) {
 			throw new ProxifyException(e);
 		}
 
 	}
 
 	private void proxifyMethods(Collection<CtMethod> methodsToProxify) {
 		for (CtMethod methodToProxify : methodsToProxify) {
 			proxifyMethod(methodToProxify);
 		}
 	}
 
 	private void proxifyMethod(CtMethod methodToProxify) {
 		String methodName = methodToProxify.getName();
 		try {
			methodToProxify.setBody("{" +
					"java.lang.reflect.Method thisMethod = $class.getDeclaredMethod(\"" + methodName + "\", $sig);" +
					"net.vidageek.tellme.messaging.MessageQueue.getDefault().addMessage(new net.vidageek.tellme.messaging.MethodCallMessage(this, thisMethod, $args));" +
					"return ($r) null; }");
 		} catch (CannotCompileException e) {
 			throw new ProxifyException(e);
 		}
 	}
 
 	private Collection<CtMethod> findMethodsToProxify(CtClass ctClassToProxify) {
 		Collection<CtMethod> methodsToProxify = new ArrayList<CtMethod>();
 		for (CtMethod method : ctClassToProxify.getDeclaredMethods()) {
 			if (method.hasAnnotation(Async.class)) {
 				methodsToProxify.add(method);
 			}
 		}
 		return methodsToProxify;
 	}
 }
