 package org.eclipse.b3.backend.evaluator.typesystem;
 
 import java.lang.ref.WeakReference;
 import java.lang.reflect.Field;
 import java.lang.reflect.GenericArrayType;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.lang.reflect.TypeVariable;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.b3.backend.evaluator.b3backend.B3FunctionType;
 import org.eclipse.b3.backend.evaluator.b3backend.B3JavaImport;
 import org.eclipse.b3.backend.evaluator.b3backend.B3MetaClass;
 
 public class TypeUtils {
 
 	private static WeakReference<TypeDistance> typeDistance = new WeakReference<TypeDistance>(null);
 
 	private static final String lock = "";
 
 	/**
 	 * mapping from object to primitive types
 	 */
 	private static final Map<Type, Type> objectToPrimitiveMap = new HashMap<Type, Type>();
 
 	/**
 	 * mapping from primitive to object types
 	 */
 	private static final Map<Type, Type> primitiveToObjectMap = new HashMap<Type, Type>();
 
 	/**
 	 * map of possible automatic coercions for all appropriate types
 	 */
 	private static final Map<Type, Set<Type>> coerceMap = new HashMap<Type, Set<Type>>();
 
 	static {
 		Class<?> objectTypeClasses[][] = { { Void.class, Boolean.class, Double.class }, { Float.class },
 				{ Long.class }, { Integer.class }, { Character.class, Short.class }, { Byte.class } };
 
 		Set<Type> coerceTypeSet = Collections.emptySet();
 		Set<Type> previousCoerceTypeSet = coerceTypeSet;
 
 		for(Class<?> objectTypes[] : objectTypeClasses) {
 			for(Class<?> objectType : objectTypes) {
 				Class<?> primitiveType = getPrimitiveTypeReflectively(objectType);
 
 				objectToPrimitiveMap.put(objectType, primitiveType);
 				primitiveToObjectMap.put(primitiveType, objectType);
 
 				coerceTypeSet = new HashSet<Type>(previousCoerceTypeSet);
 				coerceTypeSet.add(primitiveType);
 
 				coerceMap.put(objectType, coerceTypeSet);
 				coerceMap.put(primitiveType, coerceTypeSet);
 			}
 
 			previousCoerceTypeSet = coerceTypeSet;
 		}
 	}
 
 	public abstract static class Candidate {
 
 		protected enum CandidateLevel {
 
 			NONE, VARIABLE_ARITY_BY_CONVERSION, VARIABLE_ARITY_BY_SUBTYPING, FIXED_ARITY_BY_CONVERSION, FIXED_ARITY_BY_SUBTYPING;
 
 		}
 
 		protected abstract Class<?>[] getParameterTypes();
 
 		protected abstract boolean isVarArgs();
 
 		protected boolean nameMatches(String name) {
 			return true;
 		}
 
 		protected CandidateLevel isApplicable(String name, Class<?>[] parameterTypes, CandidateLevel level) {
 			// return immediately if the candidate name doesn't match
 			if(!nameMatches(name))
 				return CandidateLevel.NONE;
 
 			Class<?>[] candidateParameterTypes = getParameterTypes();
 			boolean needsConversion = false;
 			Boolean applicable;
 
 			if(isVarArgs() && level.ordinal() <= CandidateLevel.VARIABLE_ARITY_BY_SUBTYPING.ordinal()) {
 				// perform variable arity matching (level <= VARIABLE_ARITY_BY_SUBTYPING means we haven't found any
 				// fixed arity candidate yet so we need to perform the variable arity matching if the current candidate
 				// indeed is of variable arity)
 
 				int fixedParameterCount = candidateParameterTypes.length - 1;
 
 				// don't bother if there are too few arguments
 				if(fixedParameterCount > parameterTypes.length)
 					return CandidateLevel.NONE;
 
 				for(int i = 0; i < fixedParameterCount; ++i) {
 					if(!((applicable = isApplicableBySubtyping(candidateParameterTypes[i], parameterTypes[i])) != null && (applicable
 							.booleanValue() || (needsConversion = true))))
 						return CandidateLevel.NONE;
 				}
 
 				VARIABLE_ARITY: {
 					if(candidateParameterTypes.length <= parameterTypes.length) {
 						// check for fixed arity match if the counts of parameters and arguments match
 						if(candidateParameterTypes.length == parameterTypes.length) {
 							if(candidateParameterTypes[fixedParameterCount]
 									.isAssignableFrom(parameterTypes[fixedParameterCount]))
 								// the candidate is applicable as a fixed arity candidate
 								break VARIABLE_ARITY;
 						}
 
 						// we need to check the remaining arguments against the variable arity parameter component type
 						Class<?> varargComponentType = candidateParameterTypes[fixedParameterCount].getComponentType();
 
 						for(int i = fixedParameterCount; i < parameterTypes.length; ++i) {
 							if(!((applicable = isApplicableBySubtyping(varargComponentType, parameterTypes[i])) != null && (applicable
 									.booleanValue() || (needsConversion = true))))
 								return CandidateLevel.NONE;
 						}
 
 						// the candidate is applicable as a variable arity candidate with a non empty array of arguments
 						// for the variable arity parameter
 					} else {
 						// the candidate is applicable as a variable arity candidate with an empty array of arguments
 						// for the variable arity parameter
 					}
 
 					return needsConversion
 							? CandidateLevel.VARIABLE_ARITY_BY_CONVERSION : CandidateLevel.VARIABLE_ARITY_BY_SUBTYPING;
 				}
 			} else {
 				// perform fixed arity matching (either because the current candidate is of fixed arity or
 				// because level > VARIABLE_ARITY_BY_SUBTYPING which means we have found some fixed arity candidate
 				// before so we don't need to perform variable arity matching any longer)
 
 				// don't bother if the parameter and argument counts don't match
 				if(candidateParameterTypes.length != parameterTypes.length)
 					return CandidateLevel.NONE;
 
 				for(int i = 0; i < candidateParameterTypes.length; ++i) {
 					if(!((applicable = isApplicableBySubtyping(candidateParameterTypes[i], parameterTypes[i])) != null && (applicable
 							.booleanValue() || (needsConversion = true))))
 						return CandidateLevel.NONE;
 				}
 			}
 
 			return needsConversion
 					? CandidateLevel.FIXED_ARITY_BY_CONVERSION : CandidateLevel.FIXED_ARITY_BY_SUBTYPING;
 		}
 
 		/**
 		 * Check whether a value of <code>questionedType</code> can be passed (is applicable) as an argument (possibly
 		 * after applying allowed conversions) to a method expecting a value of type <code>referenceType</code> in the
 		 * corresponding parameter.
 		 * 
 		 * @param referenceType
 		 *            the type of the value of the argument passed to the method
 		 * @param questionedType
 		 *            the type of the value the method expects in the corresponding parameter
 		 * @return <code>null</code> if not applicable at all, {@link Boolean#TRUE} if applicable by subtyping only,
 		 *         {@link Boolean#FALSE} if method invocation conversion of some sort is needed
 		 */
 		protected static Boolean isApplicableBySubtyping(Class<?> referenceType, Class<?> questionedType) {
 			return referenceType.isAssignableFrom(questionedType)
 					? Boolean.TRUE : isCoercibleFrom(referenceType, questionedType)
 							? Boolean.FALSE : null;
 		}
 
 		protected static boolean isConvertibleFrom(Class<?> baseType, Class<?> fromType) {
 			return (baseType.isAssignableFrom(fromType) || isCoercibleFrom(baseType, fromType));
 		}
 
 		protected static boolean isConvertibleFrom(Class<?>[] baseTypes, Class<?>[] fromTypes, int length) {
 			for(int i = 0; i < length; ++i) {
 				if(!isConvertibleFrom(baseTypes[i], fromTypes[i]))
 					return false;
 			}
 			return true;
 		}
 
 		protected static boolean isConvertibleFrom(Class<?> baseTypes[], int offset, Class<?> fromType) {
 			for(int i = offset; i < baseTypes.length; ++i) {
 				if(!isConvertibleFrom(baseTypes[i], fromType))
 					return false;
 			}
 			return true;
 		}
 
 		protected static boolean isConvertibleFrom(Class<?> baseType, Class<?> fromTypes[], int offset) {
 			for(int i = offset; i < fromTypes.length; ++i) {
 				if(!isConvertibleFrom(baseType, fromTypes[i]))
 					return false;
 			}
 			return true;
 		}
 
 		protected static boolean isMoreSpecific(Class<?>[] referenceTypes, Class<?>[] questionedTypes) {
 			return isConvertibleFrom(questionedTypes, referenceTypes, referenceTypes.length);
 		}
 
 		protected static boolean isMoreSpecificVararg(Class<?>[] referenceTypes, int fixedParamCount,
 				Class<?>[] questionedTypes) {
 			if(!isConvertibleFrom(questionedTypes, referenceTypes, fixedParamCount))
 				return false;
 
 			Class<?> referenceVarargComponentType = referenceTypes[fixedParamCount].getComponentType();
 
 			if(!isConvertibleFrom(questionedTypes, fixedParamCount, referenceVarargComponentType))
 				return false;
 
 			Class<?> questionedVarargComponentType = questionedTypes[questionedTypes.length - 1].getComponentType();
 
 			if(!isConvertibleFrom(questionedVarargComponentType, referenceVarargComponentType))
 				return false;
 
 			return true;
 		}
 
 		protected static boolean isMoreSpecificVararg(Class<?>[] referenceTypes, Class<?>[] questionedTypes,
 				int fixedParamCount) {
 			if(!isConvertibleFrom(questionedTypes, referenceTypes, fixedParamCount))
 				return false;
 
 			Class<?> questionedVarargComponentType = questionedTypes[fixedParamCount].getComponentType();
 
 			if(!isConvertibleFrom(questionedVarargComponentType, referenceTypes, fixedParamCount))
 				return false;
 
 			Class<?> referenceVarargComponentType = referenceTypes[referenceTypes.length - 1].getComponentType();
 
 			if(!isConvertibleFrom(questionedVarargComponentType, referenceVarargComponentType))
 				return false;
 
 			return true;
 		}
 
 		protected static int specificityCompare(Candidate left, Candidate right, CandidateLevel level) {
 			Class<?>[] leftParameterTypes = left.getParameterTypes();
 			Class<?>[] rightParameterTypes = right.getParameterTypes();
 
 			if(level.ordinal() <= CandidateLevel.VARIABLE_ARITY_BY_SUBTYPING.ordinal()) {
 				// perform variable arity specificity comparison
 
 				if(rightParameterTypes.length >= leftParameterTypes.length) {
 					// check if the left candidate is more specific than right
 					if(isMoreSpecificVararg(leftParameterTypes, leftParameterTypes.length - 1, rightParameterTypes))
 						return 1;
 
 					// check if the right candidate is more specific than the left
 					if(isMoreSpecificVararg(rightParameterTypes, leftParameterTypes, leftParameterTypes.length - 1))
 						return -1;
 				} else {
 					// check if the left candidate is more specific than right
 					if(isMoreSpecificVararg(leftParameterTypes, rightParameterTypes, rightParameterTypes.length - 1))
 						return 1;
 
 					// check if the right candidate is more specific than the left
 					if(isMoreSpecificVararg(rightParameterTypes, rightParameterTypes.length - 1, leftParameterTypes))
 						return -1;
 				}
 			} else {
 				// perform fixed arity specificity comparison
 
 				// check if the left candidate is more specific than right
 				if(isMoreSpecific(leftParameterTypes, rightParameterTypes))
 					return 1;
 
 				// check if the right candidate is more specific than the left
 				if(isMoreSpecific(rightParameterTypes, leftParameterTypes))
 					return -1;
 			}
 
 			// neither of the candidates is more specific than the other so they are ambiguous
 			return 0;
 		}
 
 		public static <C extends Candidate> LinkedList<C> findMostSpecificApplicableCandidates(String name,
 				Class<?>[] parameterTypes, Iterable<C> allCandidates) {
 
 			// level:
 			//
 			// NONE
 			// no applicable candidate found
 			// 
 			// VARIABLE_ARITY_BY_CONVERSION
 			// variable arity candidate(s) (§15.12.2.4 in Java Language Specification) applicable by method invocation
 			// conversion found
 			// 
 			// VARIABLE_ARITY_BY_SUBTYPING
 			// variable arity candidate(s) (§15.12.2.4 in Java Language Specification) applicable by subtyping found
 			// 
 			// FIXED_ARITY_BY_CONVERSION
 			// fixed arity candidate(s) applicable by method invocation conversion (§15.12.2.3 in Java Language
 			// Specification) found
 			// 
 			// FIXED_ARITY_BY_SUBTYPING
 			// fixed arity candidate(s) applicable by subtyping (§15.12.2.2 in Java Language Specification) found
 			CandidateLevel level = CandidateLevel.NONE;
 
 			// applicable candidates
 			LinkedList<C> candidates = new LinkedList<C>();
 
 			for(C current : allCandidates) {
 				CandidateLevel nextLevel = current.isApplicable(name, parameterTypes, level);
 				if(nextLevel == CandidateLevel.NONE)
 					continue;
 
 				if(nextLevel != level) {
 					if(nextLevel.ordinal() > level.ordinal()) {
 						candidates.clear();
 						candidates.add(current);
 						level = nextLevel;
 					}
 					continue;
 				}
 
 				// perform less specific candidates elimination
 				Iterator<C> candidateIterator = candidates.iterator();
 				boolean isLessSpecific = true;
 
 				while(candidateIterator.hasNext()) {
 					C candidate = candidateIterator.next();
 					int specificityCompareResult = specificityCompare(current, candidate, level);
 
 					if(specificityCompareResult >= 0) {
 						if(specificityCompareResult > 0)
 							// the "current" is more specific then "candidate", we can remove the "candidate"
 							candidateIterator.remove();
						// at this point we know the "current" is not less specific than the "candidate" (at worst
 						// it is ambiguous)
 						isLessSpecific = false;
 					}
 				}
 
 				// if the "current" candidate is not less specific than all the other candidates, then add it
 				// to the candidate list
 				if(!isLessSpecific)
 					candidates.add(current);
 			}
 
 			return candidates;
 		}
 
 	}
 
 	private static Class<?> getPrimitiveTypeReflectively(Class<?> objectType) {
 		Field f;
 
 		try {
 			f = objectType.getField("TYPE");
 		} catch(SecurityException e) {
 			throw new ExceptionInInitializerError(e);
 		} catch(NoSuchFieldException e) {
 			throw new ExceptionInInitializerError(e);
 		}
 
 		try {
 			return (Class<?>) f.get(null);
 		} catch(IllegalArgumentException e) {
 			throw new ExceptionInInitializerError(e);
 		} catch(IllegalAccessException e) {
 			throw new ExceptionInInitializerError(e);
 		}
 	}
 
 	public static Class<?> getRaw(Type t) {
 		if(t instanceof Class<?>)
 			return (Class<?>) t;
 		if(t instanceof ParameterizedType) {
 			ParameterizedType pt = ParameterizedType.class.cast(t);
 			return getRaw(pt.getRawType());
 		}
 		if(t instanceof GenericArrayType) {
 			GenericArrayType ga = GenericArrayType.class.cast(t);
 			return ga.getClass();
 		}
 		if(t instanceof B3JavaImport) {
 			return getRaw(B3JavaImport.class.cast(t).getType());
 		}
 		if(t instanceof B3FunctionType) {
 			B3FunctionType ft = B3FunctionType.class.cast(t);
 			return getRaw(ft.getFunctionType()); // i.e. what type of function this is B3, or Java
 		}
 		if(t instanceof B3MetaClass) {
 			return ((B3MetaClass)t).getInstanceClass();
 		}
 		if(t instanceof TypeVariable<?>) {
 			TypeVariable<?> tv = TypeVariable.class.cast(t);
 			// TODO: OMG - this is cheating...
 			return getRaw(tv.getBounds()[0]);
 		}
 		throw new UnsupportedOperationException("UNSUPPORTED TYPE CLASS - was: " + t);
 	}
 
 	public static boolean isAssignableFrom(Type baseType, Type fromType) {
 		if(baseType instanceof B3FunctionType)
 			return ((B3FunctionType) baseType).isAssignableFrom(fromType);
 		if(baseType instanceof B3MetaClass)
 			return ((B3MetaClass)baseType).isAssignableFrom(fromType);
 		return getRaw(baseType).isAssignableFrom(getRaw(fromType));
 	}
 
 	public static boolean isCoercibleFrom(Type baseType, Type fromType) {
 		Set<Type> coerceTypes = coerceMap.get(fromType);
 
 		return coerceTypes != null && coerceTypes.contains(baseType);
 	}
 	/**
 	 * Is equivalent to calling {@link #isAssignableFrom(Type, Type)} with baseType, value.getClass(), but 
 	 * handles the special case when value is null.
 	 * @param baseType
 	 * @param value
 	 * @return
 	 */
 	public static boolean isAssignableFrom(Type baseType, Object value) {
 		if(value == null)
 			return true;
 		return isAssignableFrom(baseType, value.getClass());
 	}
 
 	public static boolean isArray(Type baseType) {
 		return baseType instanceof GenericArrayType
 				|| (baseType instanceof Class<?> && ((Class<?>) baseType).isArray());
 		// return getRaw(baseType).isArray();
 	}
 
 	/**
 	 * Returns the (best) specificity distance for an interface. (A class may restate its implementation of an inherited
 	 * interface - this will give a shorter distance).
 	 * 
 	 * @param ptc
 	 * @param pc
 	 * @return
 	 */
 	@SuppressWarnings ("unchecked")
 	public static int interfaceDistance(Class ptc, Class pc) {
 		if(ptc == pc)
 			return 0;
 		int best = Integer.MAX_VALUE;
 		for(Class i : pc.getInterfaces()) {
 			int distance = interfaceDistance(ptc, i);
 			if(distance < best) {
 				best = distance;
 				if(best == 0)
 					break; // unbeatable
 			}
 		}
 		return best != Integer.MAX_VALUE
 				? best + 1 : best;
 	}
 
 	public static int typeDistance(Type baseType, Type queriedType) {
 		Class<?> baseClass = getRaw(baseType);
 		if(baseClass.isInterface())
 			return interfaceDistance(baseClass, getRaw(queriedType));
 		return classDistance(baseClass, getRaw(queriedType));
 	}
 
 	/**
 	 * Computes the specificity distance of a class
 	 * 
 	 * @param ptc
 	 * @param pc
 	 * @return
 	 */
 	@SuppressWarnings ("unchecked")
 	public static int classDistance(Class ptc, Class pc) {
 		if(pc == null)
 			throw new IllegalArgumentException("Internal error: type is not a specialization of the class");
 		if(ptc == pc)
 			return 0;
 		if(pc.isInterface())
 			return classDistance(ptc, Object.class) + 1;
 		return classDistance(ptc, pc.getSuperclass()) + 1;
 	}
 
 	/**
 	 * Returns the element type of a list or the value type of map
 	 * 
 	 * @param baseType
 	 * @return
 	 */
 	public static Type getElementType(Type baseType) {
 		if(baseType instanceof ParameterizedType) {
 			ParameterizedType pt = ParameterizedType.class.cast(baseType);
 			int returnTypeIdx = -1;
 			if(List.class.isAssignableFrom(getRaw(pt.getRawType())))
 				returnTypeIdx = 0;
 			else if(Map.class.isAssignableFrom(getRaw(pt.getRawType())))
 				returnTypeIdx = 1;
 			Type[] typeargs = pt.getActualTypeArguments();
 			// TODO: probably too relaxed, if it was not a List<?> or Map<?>, then Object is returned
 			//
 			if(returnTypeIdx < 0 || typeargs == null || typeargs.length <= returnTypeIdx)
 				return Object.class; // unspecified
 			return typeargs[returnTypeIdx];
 		}
 		// TODO: probably too relaxed
 		return Object.class;
 	}
 
 	public static Type getCommonSuperType(Type[] types) {
 		TypeDistance td = null;
 		;
 		synchronized(lock) {
 			td = typeDistance.get();
 			if(td == null)
 				typeDistance = new WeakReference<TypeDistance>(td = new TypeDistance());
 		}
 		return td.getMostSpecificCommonType(types);
 		// Class<?>[] classes = new Class[types.length];
 		// for(int i = 0; i < types.length; i++)
 		// classes[i] = getRaw(types[i]);
 		//		
 		// int limit = classes.length;
 		// nexti: for(int i = 0; i < limit; i++) {
 		// nextj: for(int j = 0; j < limit; j++) {
 		// if(i == j)
 		// continue nextj;
 		// if(!classes[i].isAssignableFrom(classes[j]))
 		// continue nexti;
 		// }
 		// return classes[i];
 		// }
 		// return Object.class; // did not find any commonality
 	}
 
 	public static Class<?> getArrayComponentClass(Type type) {
 		if(type instanceof Class<?>)
 			return ((Class<?>) type).getComponentType();
 		if(type instanceof GenericArrayType)
 			type = ((GenericArrayType) type).getGenericComponentType();
 		else
 			throw new IllegalArgumentException("Not possible to get array component type from type:" + type);
 
 		return getRaw(type);
 	}
 
 	/**
 	 * Returns the object type corresponding to a primitive type.
 	 * 
 	 * @param type
 	 * @return
 	 */
 	public static Type objectify(Type primitiveType) {
 		Type objectType = primitiveToObjectMap.get(primitiveType);
 		return objectType != null ? objectType : primitiveType;
 	}
 
 	public static Type primitivize(Type objectType) {
 		Type primitiveType = objectToPrimitiveMap.get(objectType);
 		return primitiveType != null ? primitiveType : objectType;
 	}
 
 }
