 package com.alexrnl.commons.utils.object;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Class which automate hash code and comparison method on objects.
  * @author Alex
  */
 public final class AutoCompare {
 	/** Logger */
 	private static Logger						lg			= Logger.getLogger(AutoCompare.class.getName());
 	
 	/** Unique instance of the class */
 	private static AutoCompare					singleton	= new AutoCompare();
 	
 	/** Method for equals per class */
 	private final Map<Class<?>, Set<Method>>	equalsMethods;
 	/** Comparator for the equals method */
 	private final AttributeComparator			comparator;
 	
 	/**
 	 * Constructor #1.<br />
 	 * Default private constructor.
 	 */
 	private AutoCompare () {
 		super();
 		comparator = new AttributeComparator();
 		equalsMethods = new HashMap<>();
 	}
 	
 	/**
 	 * Return the unique instance of the class.
 	 * @return the singleton.
 	 */
 	public static AutoCompare getInstance () {
 		return singleton;
 	}
 	
 	/**
 	 * Retrieve the methods used to compare objects.
 	 * @param objClass
 	 *        the class of the objects.
 	 * @return the set with the equals methods.
 	 */
 	private Set<Method> getEqualsMethods (final Class<?> objClass) {
 		if (!equalsMethods.containsKey(objClass)) {
 			equalsMethods.put(objClass, retrieveMethods(objClass));
 		}
 		return equalsMethods.get(objClass);
 	}
 	
 	/**
 	 * Compare two object using the return value of the methods marked as {@link Field}.
 	 * @param left
 	 *        the first object to compare.
 	 * @param right
 	 *        the second object to compare.
 	 * @param <T>
 	 *        the type of the objects to be compared
 	 * @return <code>true</code> if all the attributes marked {@link Field} are equals.
 	 */
 	public <T> boolean compare (final T left, final T right) {
 		// Basic cases
 		if (left == right) {
 			return true;
 		}
 		if (left == null || right == null) {
 			return false;
 		}
 		if (!left.getClass().equals(right.getClass())) {
 			return false;
 		}
 		
		// Retrieving attributes, synchronized to avoid mixing comparisons when called on different threads.
		// FIXME maybe invoke the Field annotated method outside of the synchronized block? To avoid dead lock with awkward developpers.
 		synchronized (this) {
 			comparator.clear();
			for (final Method method : getEqualsMethods(left.getClass())) {
				try {
					comparator.add(method.invoke(left, (Object[]) null), method.invoke(right, (Object[]) null));
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					lg.warning("Could not add the value of the method " + method.getName() + " because "
							+ e.getClass() + "; " + e.getMessage());
					// XXX re-throw exception? the result will be inconsistent, it might be better to do it.
				}
 			}
 			
 			return comparator.areEquals();
 		}
 	}
 	
 	/**
 	 * Retrieve the methods for the class marked for comparison (annotated with {@link Field}).<br />
 	 * @param objClass
 	 *        the class to parse.
 	 * @return a {@link Set} with the {@link Method} annotates with {@link Field}.
 	 */
 	public static Set<Method> retrieveMethods (final Class<?> objClass) {
 		return retrieveMethods(objClass, false);
 	}
 	
 	/**
 	 * Retrieve the methods for the class marked for comparison (annotated with {@link Field}).<br />
 	 * @param objClass
 	 *        the class to parse.
 	 * @param forHashCode
 	 *        <code>true</code> if the methods are retrieved for a hash code computation
 	 *        (thus making use of the {@link Field#useForHashCode()} attribute.
 	 * @return a {@link Set} with the {@link Method} annotates with {@link Field}.
 	 */
 	public static Set<Method> retrieveMethods (final Class<?> objClass, final boolean forHashCode) {
 		final Set<Method> fieldMethods = new HashSet<>();
 		for (final Method method : objClass.getMethods()) {
 			final Annotation annotation = method.getAnnotation(Field.class);
 			if (annotation instanceof Field) {
 				if (lg.isLoggable(Level.FINE)) {
 					lg.fine("method: " + method.getName());
 				}
 				if (!forHashCode || ((Field) annotation).useForHashCode()) {
 					fieldMethods.add(method);
 				}
 			}
 		}
 		return fieldMethods;
 	}
 	
 }
