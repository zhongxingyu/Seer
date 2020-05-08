 package com.alexrnl.commons.utils.object;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Utilities methods for reflection and dynamic Java usage.<br />
  * @author Alex
  */
 public final class ReflectUtils {
 	/** Logger */
 	private static Logger	lg	= Logger.getLogger(ReflectUtils.class.getName());
 	
 	/**
 	 * Constructor #1.<br />
 	 * Private default constructor.
 	 */
 	private ReflectUtils () {
 		super();
 	}
 	
 	/**
 	 * Retrieve the methods for the class marked with the specified annotation.<br />
 	 * @param objClass
 	 *        the class to parse.
 	 * @param annotationClass
 	 *        the annotation to find.
 	 * @return a {@link Set} with the {@link Method} annotated with <code>annotationClass</code>.
 	 * @see Class#getMethods()
 	 */
 	public static Set<Method> retrieveMethods (final Class<?> objClass, final Class<? extends Annotation> annotationClass) {
 		final Set<Method> fieldMethods = new HashSet<>();
 		for (final Method method : objClass.getMethods()) {
			final Annotation annotation = method.getAnnotation(annotationClass);
			if (annotationClass == null || annotation != null) {
 				if (lg.isLoggable(Level.FINE)) {
 					lg.fine("Added method: " + method.getName());
 				}
 				fieldMethods.add(method);
 			}
 		}
 		return fieldMethods;
 	}
 	
 	/**
 	 * Invoke the following list of methods (with no parameter) and return the result in a
 	 * {@link List}.
 	 * @param target
 	 *        the target object.
 	 * @param methods
 	 *        the methods to invoke.
 	 * @return the result of the method call.
 	 * @throws InvocationTargetException
 	 *         if one of the underlying method throws an exception
 	 * @throws IllegalArgumentException
 	 *         if the method is an instance method and the specified object argument is not an
 	 *         instance of the class or interface declaring the underlying method (or of a subclass
 	 *         or implementor thereof); if the number of actual and formal parameters differ; if an
 	 *         unwrapping conversion for primitive arguments fails; or if, after possible
 	 *         unwrapping, a parameter value cannot be converted to the corresponding formal
 	 *         parameter type by a method invocation conversion.
 	 * @throws IllegalAccessException
 	 *         if one of the Method object is enforcing Java language access control and the underlying
 	 *         method is inaccessible.
 	 */
 	public static List<Object> invokeMethods (final Object target, final List<Method> methods)
 			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
 		final List<Object> results = new ArrayList<>(methods.size());
 		
 		for (final Method method : methods) {
 			results.add(method.invoke(target, (Object[]) null));
 		}
 		
 		return results;
 	}
 }
