 package al.franzis.osgi.weaving.core.equinox;
 
 import java.lang.reflect.Method;
 
 import javassist.CtClass;
 import javassist.CtMethod;
 import javassist.NotFoundException;
 
 
 public class Helpers {
 
 	public static Method findMethod(Class<?> clazz, String methodName, String methodDescription) {
		  Method[] methods = clazz.getDeclaredMethods();
 	        int n = methods.length;
 	        for (int i = 0; i < n; i++)
 	            if (methods[i].getName().equals(methodName) 
 	                && makeDescriptor(methods[i]).equals(methodDescription))
 	            return methods[i];
 
 	        return null;
 	}
 	
 	public static void find2Methods(Class<?> clazz, String methodName, String forwarderMethodName, int methodIndex, String methodDescription, Method[] methodsArray) {
 		 if ( methodsArray[methodIndex] == null )
 		 {
 			methodsArray[methodIndex] = findMethod(clazz, methodName, methodDescription);
 			methodsArray[methodIndex + 1] = findMethod(clazz, forwarderMethodName, methodDescription);
 		 }
 	}
 	
 	/**
 	 * Makes a descriptor for a given method.
 	 */
 	public static String makeDescriptor(Method m) {
 		Class<?>[] params = m.getParameterTypes();
 		return makeDescriptor(params, m.getReturnType());
 	}
 
 	public static String makeDescriptor(Class<?>[] params, Class<?> retType) {
 		StringBuffer sbuf = new StringBuffer();
 		sbuf.append('(');
 		for (int i = 0; i < params.length; i++)
 			makeDesc(sbuf, params[i]);
 
 		sbuf.append(')');
 		makeDesc(sbuf, retType);
 		return sbuf.toString();
 	}
 
 	private static void makeDesc(StringBuffer sbuf, Class<?> type) {
 		if (type.isArray()) {
 			sbuf.append('[');
 			makeDesc(sbuf, type.getComponentType());
 		} else if (type.isPrimitive()) {
 			if (type == Void.TYPE)
 				sbuf.append('V');
 			else if (type == Integer.TYPE)
 				sbuf.append('I');
 			else if (type == Byte.TYPE)
 				sbuf.append('B');
 			else if (type == Long.TYPE)
 				sbuf.append('J');
 			else if (type == Double.TYPE)
 				sbuf.append('D');
 			else if (type == Float.TYPE)
 				sbuf.append('F');
 			else if (type == Character.TYPE)
 				sbuf.append('C');
 			else if (type == Short.TYPE)
 				sbuf.append('S');
 			else if (type == Boolean.TYPE)
 				sbuf.append('Z');
 			else
 				throw new RuntimeException("bad type: " + type.getName());
 		} else
 			sbuf.append('L').append(type.getName().replace('.', '/'))
 					.append(';');
 	}
 
 	
 	/**
 	 * Makes a descriptor for a given method.
 	 * @throws NotFoundException 
 	 */
 	public static String makeDescriptor(CtMethod m) throws NotFoundException {
 		CtClass[] params = m.getParameterTypes();
 		return makeDescriptor2(params, m.getReturnType());
 	}
 
 	public static String makeDescriptor2(CtClass[] params, CtClass retType) throws NotFoundException {
 		StringBuffer sbuf = new StringBuffer();
 		sbuf.append('(');
 		for (int i = 0; i < params.length; i++)
 			makeDesc2(sbuf, params[i]);
 
 		sbuf.append(')');
 		makeDesc2(sbuf, retType);
 		return sbuf.toString();
 	}
 
 	private static void makeDesc2(StringBuffer sbuf, CtClass type) throws NotFoundException {
 		if (type.isArray()) {
 			sbuf.append('[');
 			makeDesc2(sbuf, type.getComponentType());
 		} else if (type.isPrimitive()) {
 			if (type == CtClass.voidType)
 				sbuf.append('V');
 			else if (type == CtClass.intType)
 				sbuf.append('I');
 			else if (type == CtClass.byteType)
 				sbuf.append('B');
 			else if (type == CtClass.longType)
 				sbuf.append('J');
 			else if (type == CtClass.doubleType)
 				sbuf.append('D');
 			else if (type == CtClass.floatType)
 				sbuf.append('F');
 			else if (type == CtClass.charType)
 				sbuf.append('C');
 			else if (type == CtClass.shortType)
 				sbuf.append('S');
 			else if (type == CtClass.booleanType)
 				sbuf.append('Z');
 			else
 				throw new RuntimeException("bad type: " + type.getName());
 		} else
 			sbuf.append('L').append(type.getName().replace('.', '/'))
 					.append(';');
 	}
 
 }
