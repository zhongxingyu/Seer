 package org.eclipse.b3.backend.evaluator.typesystem;
 
 import java.lang.ref.WeakReference;
 import java.lang.reflect.Array;
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
 import org.eclipse.b3.backend.evaluator.b3backend.B3ParameterizedType;
 import org.eclipse.b3.backend.evaluator.b3backend.B3Type;
 import org.eclipse.b3.backend.evaluator.b3backend.B3backendFactory;
 import org.eclipse.b3.backend.evaluator.b3backend.BFunction;
 import org.eclipse.b3.backend.evaluator.b3backend.BGuard;
 import org.eclipse.b3.backend.evaluator.b3backend.IFunction;
 import org.eclipse.b3.backend.evaluator.b3backend.impl.FunctionCandidateAdapterFactory;
 import org.eclipse.b3.backend.inference.FunctionUtils;
 import org.eclipse.emf.ecore.EObject;
 
 public class TypeUtils {
 
 	public static abstract class AdaptingJavaCandidate<A> extends JavaCandidate {
 
 		protected A adaptedObject;
 
 		private Type[] parameterTypes;
 
 		private Type varargArrayType;
 
 		protected AdaptingJavaCandidate(A object) {
 			adaptedObject = object;
 			processJavaParameterTypes();
 		}
 
 		public final Type[] getParameterTypes() {
 			return parameterTypes;
 		}
 
 		public final Type getVarargArrayType() {
 			return varargArrayType;
 		}
 
 		@Override
 		protected final void setParameterTypes(Type[] types) {
 			parameterTypes = types;
 		}
 
 		@Override
 		protected final void setVarargArrayType(Type type) {
 			varargArrayType = type;
 		}
 
 	}
 
 	public static abstract class Candidate implements ICandidate {
 
 		protected enum CandidateLevel {
 
 			NONE, VARIABLE_ARITY_BY_CONVERSION, VARIABLE_ARITY_BY_SUBTYPING, FIXED_ARITY_BY_CONVERSION, FIXED_ARITY_BY_SUBTYPING;
 
 		}
 
 		public static <C extends ICandidate> LinkedList<C> findMostSpecificApplicableCandidates(String name,
 				Type[] parameterTypes, CandidateSource<C> candidateSource) {
 
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
 
 			for(C current : candidateSource) {
 				CandidateLevel nextLevel = isCandidateApplicable(current, name, parameterTypes, level);
 				if(nextLevel == CandidateLevel.NONE)
 					continue;
 				if(!candidateSource.isCandidateAccepted(current, parameterTypes))
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
 
 		public static <C extends ICandidate> LinkedList<C> findMostSpecificApplicableCandidates(Type[] parameterTypes,
 				CandidateSource<C> candidateSource) {
 			return findMostSpecificApplicableCandidates(null, parameterTypes, candidateSource);
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
 		 * @return <code>null</code> if not applicable at all, {@link Boolean#TRUE} if applicable by subtyping only, {@link Boolean#FALSE} if method
 		 *         invocation conversion of some sort is needed
 		 */
 		protected static Boolean isApplicableBySubtyping(Type referenceType, Type questionedType) {
 			Boolean specialCase = isAssignableFromSpecialCase(referenceType, questionedType);
 			if(specialCase != null)
 				return specialCase.booleanValue()
 						? Boolean.TRUE
 						: null;
 
 			Class<?> rawReferenceType = getRaw(referenceType);
 			Class<?> rawQuestionedType = getRaw(questionedType);
 
 			return rawReferenceType.isAssignableFrom(rawQuestionedType)
 					? Boolean.TRUE
 					: isCoercibleFrom(rawReferenceType, rawQuestionedType)
 							? Boolean.FALSE
 							: null;
 		}
 
 		protected static CandidateLevel isCandidateApplicable(ICandidate candidate, String name, Type[] parameterTypes,
 				CandidateLevel level) {
 			// return immediately if the candidate name doesn't match
 			if(name != null && !name.equals(candidate.getName()))
 				return CandidateLevel.NONE;
 
 			Type[] candidateParameterTypes = candidate.getParameterTypes();
 			boolean needsConversion = false;
 			Boolean applicable;
 
 			if(candidate.isVarArgs() && level.ordinal() <= CandidateLevel.VARIABLE_ARITY_BY_SUBTYPING.ordinal()) {
 				// perform variable arity matching (level <= VARIABLE_ARITY_BY_SUBTYPING means we haven't found any
 				// fixed arity candidate yet so we need to perform the variable arity matching if the current candidate
 				// indeed is of variable arity)
 
 				int lastCandidateParameterIndex = candidateParameterTypes.length - 1;
 
 				// don't bother if there are too few arguments
 				if(lastCandidateParameterIndex > parameterTypes.length)
 					return CandidateLevel.NONE;
 
 				for(int i = 0; i < lastCandidateParameterIndex; ++i) {
 					if(!((applicable = isApplicableBySubtyping(candidateParameterTypes[i], parameterTypes[i])) != null && (applicable.booleanValue() || (needsConversion = true))))
 						return CandidateLevel.NONE;
 				}
 
 				VARIABLE_ARITY: {
 					if(candidateParameterTypes.length <= parameterTypes.length) {
 						// check for fixed arity match if the counts of parameters and arguments match
 						if(candidateParameterTypes.length == parameterTypes.length) {
 							Type varargParameterArrayType = candidate.getVarargArrayType();
 							if(varargParameterArrayType != null &&
 									isAssignableFrom(
 										varargParameterArrayType, parameterTypes[lastCandidateParameterIndex]))
 								// the candidate is applicable as a fixed arity candidate
 								break VARIABLE_ARITY;
 						}
 
 						// we need to check the remaining arguments against the variable arity parameter component type
 						Type varargComponentType = candidateParameterTypes[lastCandidateParameterIndex];
 
 						for(int i = lastCandidateParameterIndex; i < parameterTypes.length; ++i) {
 							if(!((applicable = isApplicableBySubtyping(varargComponentType, parameterTypes[i])) != null && (applicable.booleanValue() || (needsConversion = true))))
 								return CandidateLevel.NONE;
 						}
 
 						// the candidate is applicable as a variable arity candidate with a non empty array of arguments
 						// for the variable arity parameter
 					}
 					else {
 						// the candidate is applicable as a variable arity candidate with an empty array of arguments
 						// for the variable arity parameter
 					}
 
 					return needsConversion
 							? CandidateLevel.VARIABLE_ARITY_BY_CONVERSION
 							: CandidateLevel.VARIABLE_ARITY_BY_SUBTYPING;
 				}
 			}
 			else {
 				// perform fixed arity matching (either because the current candidate is of fixed arity or
 				// because level > VARIABLE_ARITY_BY_SUBTYPING which means we have found some fixed arity candidate
 				// before so we don't need to perform variable arity matching any longer)
 
 				// don't bother if the parameter and argument counts don't match
 				if(candidateParameterTypes.length != parameterTypes.length)
 					return CandidateLevel.NONE;
 
 				for(int i = 0; i < candidateParameterTypes.length; ++i) {
 					if(!((applicable = isApplicableBySubtyping(candidateParameterTypes[i], parameterTypes[i])) != null && (applicable.booleanValue() || (needsConversion = true))))
 						return CandidateLevel.NONE;
 				}
 			}
 
 			return needsConversion
 					? CandidateLevel.FIXED_ARITY_BY_CONVERSION
 					: CandidateLevel.FIXED_ARITY_BY_SUBTYPING;
 		}
 
 		protected static boolean isConvertibleFrom(Type baseTypes[], int offset, Type fromType) {
 			for(int i = offset; i < baseTypes.length; ++i) {
 				if(!isConvertibleFrom(baseTypes[i], fromType))
 					return false;
 			}
 			return true;
 		}
 
 		protected static boolean isConvertibleFrom(Type baseType, Type fromType) {
 			Boolean specialCase = isAssignableFromSpecialCase(baseType, fromType);
 			if(specialCase != null)
 				return specialCase.booleanValue();
 
 			Class<?> rawBaseType = getRaw(baseType);
 			Class<?> rawFromType = getRaw(fromType);
 
 			return rawBaseType.isAssignableFrom(rawFromType) || isCoercibleFrom(rawBaseType, rawFromType);
 		}
 
 		protected static boolean isConvertibleFrom(Type baseType, Type fromTypes[], int offset) {
 			for(int i = offset; i < fromTypes.length; ++i) {
 				if(!isConvertibleFrom(baseType, fromTypes[i]))
 					return false;
 			}
 			return true;
 		}
 
 		protected static boolean isConvertibleFrom(Type[] baseTypes, Type[] fromTypes, int length) {
 			for(int i = 0; i < length; ++i) {
 				if(!isConvertibleFrom(baseTypes[i], fromTypes[i]))
 					return false;
 			}
 			return true;
 		}
 
 		protected static boolean isMoreSpecific(Type[] referenceTypes, Type[] questionedTypes) {
 			return isConvertibleFrom(questionedTypes, referenceTypes, referenceTypes.length);
 		}
 
 		protected static boolean isMoreSpecificVararg(Type[] referenceTypes, int fixedParamCount, Type[] questionedTypes) {
 			if(!isConvertibleFrom(questionedTypes, referenceTypes, fixedParamCount))
 				return false;
 
 			Type referenceVarargComponentType = referenceTypes[fixedParamCount];
 
 			if(!isConvertibleFrom(questionedTypes, fixedParamCount, referenceVarargComponentType))
 				return false;
 
 			Type questionedVarargComponentType = questionedTypes[questionedTypes.length - 1];
 
 			if(!isConvertibleFrom(questionedVarargComponentType, referenceVarargComponentType))
 				return false;
 
 			return true;
 		}
 
 		protected static boolean isMoreSpecificVararg(Type[] referenceTypes, Type[] questionedTypes, int fixedParamCount) {
 			if(!isConvertibleFrom(questionedTypes, referenceTypes, fixedParamCount))
 				return false;
 
 			Type questionedVarargComponentType = questionedTypes[fixedParamCount];
 
 			if(!isConvertibleFrom(questionedVarargComponentType, referenceTypes, fixedParamCount))
 				return false;
 
 			Type referenceVarargComponentType = referenceTypes[referenceTypes.length - 1];
 
 			if(!isConvertibleFrom(questionedVarargComponentType, referenceVarargComponentType))
 				return false;
 
 			return true;
 		}
 
 		protected static int specificityCompare(ICandidate left, ICandidate right, CandidateLevel level) {
 			Type[] leftParameterTypes = left.getParameterTypes();
 			Type[] rightParameterTypes = right.getParameterTypes();
 
 			if(level.ordinal() <= CandidateLevel.VARIABLE_ARITY_BY_SUBTYPING.ordinal()) {
 				// perform variable arity specificity comparison
 
 				if(leftParameterTypes.length > rightParameterTypes.length) {
 					// check if the left candidate is more specific than right
 					if(isMoreSpecificVararg(leftParameterTypes, rightParameterTypes, rightParameterTypes.length - 1))
 						return 1;
 
 					// check if the right candidate is more specific than the left
 					if(isMoreSpecificVararg(rightParameterTypes, rightParameterTypes.length - 1, leftParameterTypes))
 						return -1;
 				}
 				else {
 					// check if the left candidate is more specific than right
 					if(isMoreSpecificVararg(leftParameterTypes, leftParameterTypes.length - 1, rightParameterTypes))
 						return 1;
 
 					// check if the right candidate is more specific than the left
 					if(isMoreSpecificVararg(rightParameterTypes, leftParameterTypes, leftParameterTypes.length - 1))
 						return -1;
 				}
 			}
 			else {
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
 
 		public String getName() {
 			return null;
 		}
 
 	}
 
 	public static abstract class CandidateSource<C extends ICandidate> implements Iterable<C> {
 
 		/**
 		 * Check if the specified parameter types are accepted for the specified <code>candidate</code> in the context
 		 * of the current <code>CandidateSource</code>.
 		 * 
 		 * @param candidate
 		 *            the candidate to check the specified parameters against
 		 * @param types
 		 *            the parameter types to check against the specified candidate
 		 * @return true if the specified parameter types are accepted for the specified <code>candidate</code> in the
 		 *         context of the current <code>CandidateSource</code>
 		 */
 		public boolean isCandidateAccepted(C candidate, Type[] parameterTypes) {
 			return true;
 		}
 
 	}
 
 	protected static class GenericArrayTypeImpl implements GenericArrayType {
 
 		private final Type componentType;
 
 		public GenericArrayTypeImpl(Type aComponentType) {
 			componentType = aComponentType;
 		}
 
 		public Type getGenericComponentType() {
 			return componentType;
 		}
 
 	}
 
 	public static class GuardedFunctionCandidateSource extends
 			CandidateSource<FunctionCandidateAdapterFactory.IFunctionCandidateAdapter> {
 
 		private class FunctionCandidateIterator implements
 				Iterator<FunctionCandidateAdapterFactory.IFunctionCandidateAdapter> {
 
 			private Iterator<IFunction> functionListIterator = functionList.iterator();
 
 			public boolean hasNext() {
 				return functionListIterator.hasNext();
 			}
 
 			public FunctionCandidateAdapterFactory.IFunctionCandidateAdapter next() {
 				return FunctionCandidateAdapterFactory.eINSTANCE.adapt(functionListIterator.next());
 			}
 
 			public void remove() {
 				throw new UnsupportedOperationException();
 			}
 
 		}
 
 		private List<IFunction> functionList;
 
 		// private BExecutionContext callContext;
 
 		// private Object[] callParameters;
 
 		public GuardedFunctionCandidateSource(List<IFunction> list /* , BExecutionContext context, Object[] parameters */) {
 			functionList = list;
 			// we need the following context parameters to be able to call guards
 			// callContext = context;
 			// callParameters = parameters;
 		}
 
 		@Override
 		public boolean isCandidateAccepted(FunctionCandidateAdapterFactory.IFunctionCandidateAdapter candidateAdapter,
 				Type[] parameterTypes) {
 			IFunction candidate = candidateAdapter.getTarget();
 			BGuard guard = candidate.getGuard();
 
 			if(guard == null)
 				return true;
 
 			try {
 				// return guard.accepts(candidate, callContext, callParameters, parameterTypes);
 				return guard.accepts(candidate, parameterTypes);
 			}
 			catch(Throwable t) {
 				// TODO we may want to collect the errors and report them later
 				throw new Error(t);
 			}
 		}
 
 		public Iterator<FunctionCandidateAdapterFactory.IFunctionCandidateAdapter> iterator() {
 			return new FunctionCandidateIterator();
 		}
 
 	}
 
 	public interface ICandidate {
 
 		String getName();
 
 		Type[] getParameterTypes();
 
 		Type getVarargArrayType();
 
 		boolean isVarArgs();
 
 	}
 
 	public static abstract class JavaCandidate extends Candidate {
 
 		protected int instanceParametersCount;
 
 		public int getInstanceParametersCount() {
 			return instanceParametersCount;
 		}
 
 		protected abstract Type[] getJavaParameterTypes();
 
 		public Object[] prepareJavaCallParameters(Type[] actualParameterTypes, Object[] actualParameters) {
 			Type[] declaredParameterTypes = getParameterTypes();
 			Type varargParameterArrayType = getVarargArrayType();
 			int lastDeclaredParameterIndex = declaredParameterTypes.length - 1;
 			int offset = instanceParametersCount;
 
 			if(varargParameterArrayType != null // this is a vararg candidate which possibly needs to have the variable
 												// arity arguments turned into an array
 					&&
 					!(actualParameterTypes.length == declaredParameterTypes.length && isAssignableFrom(
 						varargParameterArrayType, actualParameterTypes[lastDeclaredParameterIndex]))) {
 				Object[] callParameters = new Object[declaredParameterTypes.length - offset];
 
 				System.arraycopy(actualParameters, offset, callParameters, 0, lastDeclaredParameterIndex - offset);
 
 				Class<?> varargComponentType = getRaw(declaredParameterTypes[lastDeclaredParameterIndex]);
 				Object varargArray = Array.newInstance(varargComponentType, actualParameterTypes.length -
 						lastDeclaredParameterIndex);
 
 				for(int i = 0; i < actualParameterTypes.length - lastDeclaredParameterIndex; ++i)
 					Array.set(varargArray, i, actualParameters[lastDeclaredParameterIndex + i]);
 
 				callParameters[lastDeclaredParameterIndex - offset] = varargArray;
 
 				return callParameters;
 			}
 
 			if(offset != 0) {
 				Object[] callParameters = new Object[declaredParameterTypes.length - offset];
 
 				System.arraycopy(actualParameters, offset, callParameters, 0, declaredParameterTypes.length - offset);
 
 				return callParameters;
 			}
 
 			return actualParameters;
 		}
 
 		public void processJavaParameterTypes(Type... instanceParameterTypes) {
 			Type[] parameterTypes = getJavaParameterTypes();
 			int instanceParameterTypesCount = instanceParameterTypes != null
 					? instanceParameterTypes.length
 					: 0;
 
 			if(instanceParameterTypesCount > 0) {
 				Type[] newParameterTypes = new Type[instanceParameterTypesCount + parameterTypes.length];
 
 				System.arraycopy(instanceParameterTypes, 0, newParameterTypes, 0, instanceParameterTypesCount);
 				System.arraycopy(
 					parameterTypes, 0, newParameterTypes, instanceParameterTypesCount, parameterTypes.length);
 
 				parameterTypes = newParameterTypes;
 			}
 			else if(parameterTypes.getClass().getComponentType() != Type.class) {
 				// ensure that the component type of the parameter types array is Type, as we may want to store values
 				// of the type Type in that array
 				Type[] newParameterTypes = new Type[parameterTypes.length];
 
 				System.arraycopy(parameterTypes, 0, newParameterTypes, 0, parameterTypes.length);
 
 				parameterTypes = newParameterTypes;
 			}
 
 			Type varargArrayType;
 
 			if(isVarArgs()) {
 				int varargParameterIndex = parameterTypes.length - 1;
 
 				varargArrayType = parameterTypes[varargParameterIndex];
 				parameterTypes[varargParameterIndex] = getArrayComponentType(varargArrayType);
 			}
 			else
 				varargArrayType = null;
 
 			setParameterTypes(parameterTypes);
 			setVarargArrayType(varargArrayType);
 
 			instanceParametersCount = instanceParameterTypesCount;
 		}
 
 		protected abstract void setParameterTypes(Type[] types);
 
 		protected abstract void setVarargArrayType(Type type);
 
 	}
 
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
 		Class<?> objectTypeClasses[][] = {
 				{ Void.class, Boolean.class, Double.class }, { Float.class }, { Long.class }, { Integer.class },
 				{ Character.class, Short.class }, { Byte.class } };
 
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
 
 	public static Type coerceToEObjectType(Type aType) {
 		if(aType == null || aType instanceof EObject)
 			return aType;
 		B3Type t = B3backendFactory.eINSTANCE.createB3Type();
 		t.setRawType(aType);
 		return t;
 	}
 
 	// /**
 	// * Computes the specificity distance of a class
 	// *
 	// * @param ptc
 	// * @param pc
 	// * @return
 	// */
 	// @SuppressWarnings({ "rawtypes" })
 	// public static int classDistance(Class ptc, Class pc) {
 	// if(pc == null)
 	// throw new IllegalArgumentException("Internal error: type is not a specialization of the class");
 	// if(ptc == pc)
 	// return 0;
 	// if(pc.isInterface())
 	// return classDistance(ptc, Object.class) + 1;
 	// return classDistance(ptc, pc.getSuperclass()) + 1;
 	// }
 
 	public static Type getArrayComponentType(Type type) {
 		Type arrayType = type;
 		if(arrayType instanceof Class<?> || arrayType instanceof B3ParameterizedType &&
 				(arrayType = ((B3ParameterizedType) arrayType).getRawType()) instanceof Class<?>) {
 			Class<?> componentType = ((Class<?>) arrayType).getComponentType();
 
 			if(componentType != null)
 				return componentType;
 		}
 		else if(arrayType instanceof GenericArrayType)
 			return ((GenericArrayType) arrayType).getGenericComponentType();
 
 		throw new IllegalArgumentException("Not possible to get array component type from type: " + type);
 	}
 
 	public static Type getArrayType(Type componentType) {
 		if(componentType instanceof Class<?>) {
 			Class<?> componentClass = (Class<?>) componentType;
 
 			if(componentClass.isPrimitive())
 				throw new UnsupportedOperationException("Primitive types not supported:" + componentType);
 
 			String componentClassName = componentClass.getName();
 			StringBuilder arrayClassName = new StringBuilder(componentClassName.length() + 3);
 
 			arrayClassName.append('[');
 			if(!componentClass.isArray())
 				arrayClassName.append('L').append(componentClassName).append(';');
 			else
 				arrayClassName.append(componentClassName);
 
 			try {
 				return Class.forName(arrayClassName.toString());
 			}
 			catch(ClassNotFoundException e) {
 				throw new Error("Failed to construct array type for component type: " + componentType, e);
 			}
 		}
 		return new GenericArrayTypeImpl(componentType);
 	}
 
 	public static Type getCommonSuperType(Type[] types) {
 		TypeDistance td = null;
 
 		synchronized(lock) {
 			td = typeDistance.get();
 			if(td == null)
 				typeDistance = new WeakReference<TypeDistance>(td = new TypeDistance());
 		}
 		return td.getMostSpecificCommonType(types);
 	}
 
 	/**
 	 * Returns the element type of a list or the value type of map
 	 * 
 	 * @param baseType
 	 * @return
 	 */
 	public static Type getElementType(Type baseType) {
		if(baseType instanceof B3Type)
			baseType = ((B3Type) baseType).getRawType();
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
 
 	private static Class<?> getPrimitiveTypeReflectively(Class<?> objectType) {
 		Field f;
 
 		try {
 			f = objectType.getField("TYPE");
 		}
 		catch(SecurityException e) {
 			throw new ExceptionInInitializerError(e);
 		}
 		catch(NoSuchFieldException e) {
 			throw new ExceptionInInitializerError(e);
 		}
 
 		try {
 			return (Class<?>) f.get(null);
 		}
 		catch(IllegalArgumentException e) {
 			throw new ExceptionInInitializerError(e);
 		}
 		catch(IllegalAccessException e) {
 			throw new ExceptionInInitializerError(e);
 		}
 	}
 
 	protected static Class<?> getRaw(GenericArrayType type) {
 		StringBuilder rawArrayClassName = new StringBuilder();
 		GenericArrayType arrayType = type;
 		Type componentType;
 
 		while(true) {
 			rawArrayClassName.append('[');
 
 			componentType = arrayType.getGenericComponentType();
 			if(!(componentType instanceof GenericArrayType))
 				break;
 
 			arrayType = (GenericArrayType) componentType;
 		}
 
 		rawArrayClassName.append('L').append(getRaw(componentType).getName()).append(';');
 
 		try {
 			return Class.forName(rawArrayClassName.toString());
 		}
 		catch(ClassNotFoundException e) {
 			throw new Error("Failed to construct raw array type for generic array type: " + type, e);
 		}
 	}
 
 	public static Class<?> getRaw(Type t) {
 		if(t instanceof Class<?>)
 			return (Class<?>) t;
 		if(t instanceof B3Type)
 			return getRaw(((B3Type) t).getRawType());
 		if(t instanceof ParameterizedType)
 			return getRaw(((ParameterizedType) t).getRawType());
 		if(t instanceof GenericArrayType)
 			// return (Class<?>) getArrayType(getRaw(((GenericArrayType) t).getGenericComponentType()));
 			return getRaw((GenericArrayType) t); // optimization
 		if(t instanceof B3JavaImport)
 			return getRaw(((B3JavaImport) t).getType());
 		if(t instanceof B3FunctionType) {
 			Type u = ((B3FunctionType) t).getFunctionType();
 			return u == null
 					? BFunction.class
 					: getRaw(u); // i.e. what type of function this is B3, or Java
 		}
 		if(t instanceof B3MetaClass)
 			return ((B3MetaClass) t).getInstanceClass();
 		if(t instanceof TypeVariable<?>)
 			// TODO: OMG - this is cheating...
 			return getRaw(((TypeVariable<?>) t).getBounds()[0]);
 		throw new UnsupportedOperationException("UNSUPPORTED TYPE CLASS - was: " + t);
 	}
 
 	/**
 	 * Returns true if the overloaded type is assignable from the overloading type.
 	 * 
 	 * @param overloaded
 	 * @param overloading
 	 * @return
 	 */
 	public static boolean hasCompatibleReturnType(IFunction overloaded, IFunction overloading) {
 		Type at = overloaded.getReturnType();
 		Type bt = overloading.getReturnType();
 		return at.getClass().isAssignableFrom(bt.getClass());
 	}
 
 	/**
 	 * Returns true if the functions have the same number of parameters, compatible var args flag,
 	 * compatible classFunction flag,
 	 * and equal parameter types.
 	 * 
 	 * @param a
 	 * @param b
 	 * @return
 	 */
 	public static boolean hasEqualSignature(IFunction a, IFunction b) {
 		Type[] pta = FunctionUtils.getParameterTypes(a);
 		Type[] ptb = FunctionUtils.getParameterTypes(b);
 		if(pta.length != ptb.length)
 			return false;
 		if(a.isClassFunction() != b.isClassFunction())
 			return false;
 		if(a.isVarArgs() != b.isVarArgs())
 			return false;
 		for(int i = 0; i < pta.length; i++)
 			if(!pta[i].equals(ptb[i]))
 				return false;
 		return true;
 	}
 
 	public static boolean isArray(Type baseType) {
 		if(baseType instanceof B3ParameterizedType)
 			baseType = ((B3ParameterizedType) baseType).getRawType();
 
 		return (baseType instanceof Class<?>) && ((Class<?>) baseType).isArray() ||
 				(baseType instanceof GenericArrayType);
 	}
 
 	/**
 	 * Is equivalent to calling {@link #isAssignableFrom(Type, Type)} with baseType, value.getClass(), but handles the
 	 * special case when value is null.
 	 * 
 	 * @param baseType
 	 * @param value
 	 * @return
 	 */
 	public static boolean isAssignableFrom(Type baseType, Object value) {
 		if(value == null)
 			return true;
 		Boolean specialCase = isAssignableFromSpecialCase(baseType, value.getClass());
 		if(specialCase != null)
 			return specialCase.booleanValue();
 
 		return getRaw(baseType).isInstance(value);
 		// return isAssignableFrom(baseType, value.getClass());
 	}
 
 	public static boolean isAssignableFrom(Type baseType, Type fromType) {
 		Boolean specialCase = isAssignableFromSpecialCase(baseType, fromType);
 		if(specialCase != null)
 			return specialCase.booleanValue();
 		return getRaw(baseType).isAssignableFrom(getRaw(fromType));
 	}
 
 	protected static Boolean isAssignableFromSpecialCase(Type baseType, Type fromType) {
 		if(baseType instanceof B3FunctionType)
 			return Boolean.valueOf(((B3FunctionType) baseType).isAssignableFrom(fromType));
 		if(baseType instanceof B3MetaClass)
 			return Boolean.valueOf(((B3MetaClass) baseType).isAssignableFrom(fromType));
 		return null;
 	}
 
 	public static boolean isCoercibleFrom(Type baseType, Type fromType) {
 		Set<Type> coerceTypes = coerceMap.get(fromType);
 
 		return coerceTypes != null && coerceTypes.contains(baseType);
 	}
 
 	/**
 	 * Returns the object type corresponding to a primitive type.
 	 * 
 	 * @param type
 	 * @return
 	 */
 	public static Type objectify(Type primitiveType) {
 		Type objectType = primitiveToObjectMap.get(primitiveType);
 		return objectType != null
 				? objectType
 				: primitiveType;
 	}
 
 	public static Type primitivize(Type objectType) {
 		Type primitiveType = objectToPrimitiveMap.get(objectType);
 		return primitiveType != null
 				? primitiveType
 				: objectType;
 	}
 }
