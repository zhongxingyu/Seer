 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.b3.backend.evaluator.b3backend.impl;
 
 import java.lang.reflect.Array;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Type;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import org.eclipse.b3.backend.core.B3AmbiguousFunctionSignatureException;
 import org.eclipse.b3.backend.core.B3NoSuchFunctionSignatureException;
 import org.eclipse.b3.backend.evaluator.b3backend.B3backendPackage;
 import org.eclipse.b3.backend.evaluator.b3backend.BExecutionContext;
 import org.eclipse.b3.backend.evaluator.b3backend.BSystemContext;
 import org.eclipse.b3.backend.evaluator.typesystem.TypeUtils;
 
 import org.eclipse.emf.ecore.EClass;
 
 /**
  * <!-- begin-user-doc --> 
  * An implementation of the model object '<em><b>BSystem Context</b></em>'. 
  * <!-- end-user-doc -->
  * <p>
  * </p>
  *
  * @generated
  */
 public class BSystemContextImpl extends BExecutionContextImpl implements BSystemContext {
 	/**
 	 * <!-- begin-user-doc --> 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final String copyright = "Copyright (c) 2009, Cloudsmith Inc and others.\nAll rights reserved. This program and the accompanying materials\nare made available under the terms of the Eclipse Public License v1.0\nwhich accompanies this distribution, and is available at\nhttp://www.eclipse.org/legal/epl-v10.html\n\rContributors:\n- Cloudsmith Inc - initial API and implementation.\r";
 
 	/**
 	 * <!-- begin-user-doc --> 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected BSystemContextImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc --> 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return B3backendPackage.Literals.BSYSTEM_CONTEXT;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> 
 	 * TODO: Translate exceptions from not finding method etc. into B3 Exceptions 
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public Object callFunction(String functionName, Object[] parameters, Type[] types, BExecutionContext ctx)
 			throws Throwable {
 		Method method = findMethod(functionName, types);
 		Class<?>[] methodParameterTypes = method.getParameterTypes();
 		Object[] callParameters = new Object[methodParameterTypes.length];
 
 		if(method.isVarArgs()
 				&& (types.length - 1 != methodParameterTypes.length || !methodParameterTypes[methodParameterTypes.length - 1]
 						.isAssignableFrom(TypeUtils.getRaw(types[methodParameterTypes.length])))) {
 
 			for(int i = 1; i < methodParameterTypes.length; ++i)
 				callParameters[i - 1] = parameters[i];
 
 			Class<?> varargComponentType = methodParameterTypes[methodParameterTypes.length - 1].getComponentType();
 
 			Object varargArray = Array.newInstance(varargComponentType, types.length - methodParameterTypes.length);
 
 			for(int i = methodParameterTypes.length; i < types.length; ++i)
 				Array.set(varargArray, i - methodParameterTypes.length, parameters[i]);
 
 			callParameters[methodParameterTypes.length - 1] = varargArray;
 		} else {
 			for(int i = 1; i < methodParameterTypes.length + 1; ++i)
 				callParameters[i - 1] = parameters[i];
 		}
 
 		try {
 			// invoke handles unwrap of non primitive types
 			return method.invoke(parameters[0], callParameters);
 		} catch(InvocationTargetException e) {
 			throw e.getCause();
 		}
 	}
 
 	private Method findMethod(String functionName, Type[] types) throws Throwable {
 		// in Java, all calls refer to an instance/class (which must be in the first parameter)
 		if(types.length == 0)
 			throw new B3NoSuchFunctionSignatureException(functionName, types);
 
 		Class<?> objectType = TypeUtils.getRaw(types[0]);
 		Class<?>[] parameterTypes = new Class<?>[types.length - 1];
 
 		for(int i = 1; i < types.length; ++i)
 			parameterTypes[i - 1] = TypeUtils.getRaw(types[i]);
 
 		// candidate methods
 		LinkedList<Method> candidates = new LinkedList<Method>();
 		// state:
 		//
 		// NONE
 		// no candidate method found
 		// 
 		// VARIABLE_ARITY_BY_CONVERSION
 		// variable arity candidate method(s) (§15.12.2.4 in Java Language Specification) applicable
 		// by method invocation conversion found
 		// 
 		// VARIABLE_ARITY_BY_SUBTYPING
 		// variable arity candidate method(s) (§15.12.2.4 in Java Language Specification) applicable
 		// by subtyping found
 		// 
 		// FIXED_ARITY_BY_CONVERSION
 		// fixed arity candidate method(s) applicable by method invocation conversion (§15.12.2.3
 		// in Java Language Specification) found
 		// 
 		// FIXED_ARITY_BY_SUBTYPING
 		// fixed arity candidate method(s) applicable by subtyping (§15.12.2.2 in Java Language
 		// Specification) found
 		TypeUtils.CandidateState state = TypeUtils.CandidateState.NONE;
 
 		METHOD: for(Method method : objectType.getMethods()) {
 			// continue if the method name doesn't match
 			if(!functionName.equals(method.getName()))
 				continue METHOD;
 
 			Class<?>[] methodParameterTypes = method.getParameterTypes();
 			boolean needsConversion = false;
 
 			if(method.isVarArgs() && state.ordinal() <= TypeUtils.CandidateState.VARIABLE_ARITY_BY_SUBTYPING.ordinal()) {
 				// perform variable arity method matching (state <= VARIABLE_ARITY_BY_SUBTYPING means we haven't found
 				// any fixed arity candidate yet so we need to perform the variable arity method matching if the current
 				// method indeed is a variable arity method)
 
 				int fixedParameterCount = methodParameterTypes.length - 1;
 
 				// don't bother if there are too few arguments
 				if(fixedParameterCount > parameterTypes.length)
 					continue METHOD;
 
 				for(int i = 0; i < fixedParameterCount; ++i) {
 					if(!(methodParameterTypes[i].isAssignableFrom(parameterTypes[i]) || TypeUtils.isCoercibleFrom(
 							methodParameterTypes[i], parameterTypes[i])
 							&& (needsConversion = true)))
 						continue METHOD;
 				}
 
 				VARIABLE_ARITY: {
 					if(methodParameterTypes.length <= parameterTypes.length) {
 						// check for fixed arity match if the counts of parameters and arguments match
 						if(methodParameterTypes.length == parameterTypes.length) {
 							if(methodParameterTypes[fixedParameterCount]
 									.isAssignableFrom(parameterTypes[fixedParameterCount]))
 								// the method is applicable as a fixed arity method
 								break VARIABLE_ARITY;
 						}
 
 						// we need to check the remaining arguments against the variable arity parameter component type
 						Class<?> varargComponentType = methodParameterTypes[fixedParameterCount].getComponentType();
 
 						for(int i = fixedParameterCount; i < parameterTypes.length; ++i) {
 							if(!(varargComponentType.isAssignableFrom(parameterTypes[i]) || TypeUtils.isCoercibleFrom(
 									varargComponentType, parameterTypes[i])
 									&& (needsConversion = true)))
 								continue METHOD;
 						}
 
 						// the method is applicable as a variable arity method with non empty array of arguments
 						// for the variable arity parameter
 					} else {
 						// the method is applicable as a variable arity method with an empty array of arguments
 						// for the variable arity parameter
 					}
 
 					TypeUtils.CandidateState nextState = needsConversion
 							? TypeUtils.CandidateState.VARIABLE_ARITY_BY_CONVERSION
 							: TypeUtils.CandidateState.VARIABLE_ARITY_BY_SUBTYPING;
 
 					if(state != nextState) {
 						if(state.ordinal() < nextState.ordinal()) {
 							candidates.clear();
 							candidates.add(method);
 							state = nextState;
 						}
 						continue METHOD;
 					}
 
 					// perform less specific variable arity candidates elimination
 					Iterator<Method> candidateIterator = candidates.iterator();
 					boolean isLessSpecific = true;
 
 					while(candidateIterator.hasNext()) {
 						Method candidate = candidateIterator.next();
 						Class<?>[] candidateParameterTypes = candidate.getParameterTypes();
 
 						if(candidateParameterTypes.length >= methodParameterTypes.length) {
 							// check if the method is more specific than the current candidate
 							IS_MORE_SPECIFIC: {
 								for(int i = 0; i < parameterTypes.length - 1; ++i) {
 									if(!(candidateParameterTypes[i].isAssignableFrom(methodParameterTypes[i]) || TypeUtils
 											.isCoercibleFrom(candidateParameterTypes[i], methodParameterTypes[i])))
 										break IS_MORE_SPECIFIC;
 								}
 
 								Class<?> varargComponentType = parameterTypes[parameterTypes.length - 1]
 										.getComponentType();
 
 								for(int i = parameterTypes.length - 1; i < candidateParameterTypes.length - 1; ++i) {
 									if(!(candidateParameterTypes[i].isAssignableFrom(varargComponentType) || TypeUtils
 											.isCoercibleFrom(candidateParameterTypes[i], varargComponentType)))
 										break IS_MORE_SPECIFIC;
 								}
 
 								Class<?> candidateVarargComponentType = candidateParameterTypes[candidateParameterTypes.length - 1]
 										.getComponentType();
 
 								if(!(candidateVarargComponentType.isAssignableFrom(varargComponentType) || TypeUtils
 										.isCoercibleFrom(candidateVarargComponentType, varargComponentType)))
 									break IS_MORE_SPECIFIC;
 
 								// the method is more specific than the current candidate, so no need to keep the
 								// candidate around
 								candidateIterator.remove();
 								isLessSpecific = false;
 								continue;
 							}
 
 							// verify that the method is less specific than the current candidate
 							IS_LESS_SPECIFIC: {
 								for(int i = 0; i < parameterTypes.length - 1; ++i) {
 									if(!(methodParameterTypes[i].isAssignableFrom(candidateParameterTypes[i]) || TypeUtils
 											.isCoercibleFrom(methodParameterTypes[i], candidateParameterTypes[i])))
 										break IS_LESS_SPECIFIC;
 								}
 
 								Class<?> varargComponentType = parameterTypes[parameterTypes.length - 1]
 										.getComponentType();
 
 								for(int i = parameterTypes.length - 1; i < candidateParameterTypes.length - 1; ++i) {
 									if(!(varargComponentType.isAssignableFrom(candidateParameterTypes[i]) || TypeUtils
 											.isCoercibleFrom(varargComponentType, candidateParameterTypes[i])))
 										break IS_LESS_SPECIFIC;
 								}
 
 								Class<?> candidateVarargComponentType = candidateParameterTypes[candidateParameterTypes.length - 1]
 										.getComponentType();
 
 								if(!(varargComponentType.isAssignableFrom(candidateVarargComponentType) || TypeUtils
 										.isCoercibleFrom(varargComponentType, candidateVarargComponentType)))
 									break IS_LESS_SPECIFIC;
 
 								// the method is less specific than the current candidate
 								continue;
 							}
 						} else {
 							// check if the method is more specific than the current candidate
 							IS_MORE_SPECIFIC: {
 								for(int i = 0; i < candidateParameterTypes.length - 1; ++i) {
 									if(!(candidateParameterTypes[i].isAssignableFrom(methodParameterTypes[i]) || TypeUtils
 											.isCoercibleFrom(candidateParameterTypes[i], methodParameterTypes[i])))
 										break IS_MORE_SPECIFIC;
 								}
 
 								Class<?> candidateVarargComponentType = candidateParameterTypes[candidateParameterTypes.length - 1]
 										.getComponentType();
 
 								for(int i = candidateParameterTypes.length - 1; i < parameterTypes.length - 1; ++i) {
 									if(!(candidateVarargComponentType.isAssignableFrom(methodParameterTypes[i]) || TypeUtils
 											.isCoercibleFrom(candidateVarargComponentType, methodParameterTypes[i])))
 										break IS_MORE_SPECIFIC;
 								}
 
 								Class<?> varargComponentType = parameterTypes[parameterTypes.length - 1]
 										.getComponentType();
 
 								if(!(candidateVarargComponentType.isAssignableFrom(varargComponentType) || TypeUtils
 										.isCoercibleFrom(candidateVarargComponentType, varargComponentType)))
 									break IS_MORE_SPECIFIC;
 
 								// the method is more specific than the current candidate, so no need to keep the
 								// candidate around
 								candidateIterator.remove();
 								isLessSpecific = false;
 								continue;
 							}
 
 							// verify that the method is less specific than the current candidate
 							IS_LESS_SPECIFIC: {
 								for(int i = 0; i < candidateParameterTypes.length - 1; ++i) {
 									if(!(methodParameterTypes[i].isAssignableFrom(candidateParameterTypes[i]) || TypeUtils
 											.isCoercibleFrom(methodParameterTypes[i], candidateParameterTypes[i])))
 										break IS_LESS_SPECIFIC;
 								}
 
 								Class<?> candidateVarargComponentType = candidateParameterTypes[candidateParameterTypes.length - 1]
 										.getComponentType();
 
 								for(int i = candidateParameterTypes.length - 1; i < parameterTypes.length - 1; ++i) {
 									if(!(methodParameterTypes[i].isAssignableFrom(candidateVarargComponentType) || TypeUtils
 											.isCoercibleFrom(methodParameterTypes[i], candidateVarargComponentType)))
 										break IS_LESS_SPECIFIC;
 								}
 
 								Class<?> varargComponentType = parameterTypes[parameterTypes.length - 1]
 										.getComponentType();
 
 								if(!(varargComponentType.isAssignableFrom(candidateVarargComponentType) || TypeUtils
 										.isCoercibleFrom(varargComponentType, candidateVarargComponentType)))
 									break IS_LESS_SPECIFIC;
 
 								// the method is less specific than the current candidate
 								continue;
 							}
 						}
 
 						// the method is ambiguous with respect to the current candidate, which means it is not less
 						// specific
 						isLessSpecific = false;
 					}
 
 					// if the method is not less specific than all the candidates, then add it to the candidate list
 					if(!isLessSpecific)
 						candidates.add(method);
 
 					continue METHOD;
 				}
 
 				// fall through to the fixed arity candidates handling
 			} else {
 				// perform fixed arity method matching (either because the current method is a fixed arity method or
 				// because state > VARIABLE_ARITY_BY_SUBTYPING which means we have found some fixed arity candidate
 				// before so we don't need to perform variable arity method matching any longer)
 
 				// don't bother if the parameter counts don't match
 				if(methodParameterTypes.length != parameterTypes.length)
 					continue METHOD;
 
 				for(int i = 0; i < methodParameterTypes.length; ++i) {
 					if(!(methodParameterTypes[i].isAssignableFrom(parameterTypes[i]) || TypeUtils.isCoercibleFrom(
 							methodParameterTypes[i], parameterTypes[i])
 							&& (needsConversion = true)))
 						continue METHOD;
 				}
 			}
 
 			TypeUtils.CandidateState nextState = needsConversion
 					? TypeUtils.CandidateState.FIXED_ARITY_BY_CONVERSION
 					: TypeUtils.CandidateState.FIXED_ARITY_BY_SUBTYPING;
 
 			if(state != nextState) {
 				if(state.ordinal() < nextState.ordinal()) {
 					candidates.clear();
 					candidates.add(method);
 					state = nextState;
 				}
 				continue METHOD;
 			}
 
 			// perform less specific fixed arity candidates elimination
 			Iterator<Method> candidateIterator = candidates.iterator();
 			boolean isLessSpecific = true;
 
 			while(candidateIterator.hasNext()) {
 				Method candidate = candidateIterator.next();
 				Class<?>[] candidateParameterTypes = candidate.getParameterTypes();
 
 				// check if the method is more specific than the current candidate
 				IS_MORE_SPECIFIC: {
 					for(int i = 0; i < parameterTypes.length; ++i) {
 						if(!(candidateParameterTypes[i].isAssignableFrom(methodParameterTypes[i]) || TypeUtils
 								.isCoercibleFrom(candidateParameterTypes[i], methodParameterTypes[i])))
 							break IS_MORE_SPECIFIC;
 					}
 					// the method is more specific than the current candidate, so no need to keep the candidate around
 					candidateIterator.remove();
 					isLessSpecific = false;
 					continue;
 				}
 
 				// verify that the method is less specific than the current candidate
 				IS_LESS_SPECIFIC: {
 					for(int i = 0; i < parameterTypes.length; ++i) {
 						if(!(methodParameterTypes[i].isAssignableFrom(candidateParameterTypes[i]) || TypeUtils
 								.isCoercibleFrom(methodParameterTypes[i], candidateParameterTypes[i])))
 							break IS_LESS_SPECIFIC;
 					}
 					// the method is less specific than the current candidate
 					continue;
 				}
 
 				// the method is ambiguous with respect to the current candidate, which means it is not less specific
 				isLessSpecific = false;
 			}
 
 			// if the method is not less specific than all the candidates, then add it to the candidate list
 			if(!isLessSpecific)
 				candidates.add(method);
 		}
 
 		switch(candidates.size()) {
 		case 0: // no candidate method found
 			throw new B3NoSuchFunctionSignatureException(functionName, types);
 		case 1: { // one candidate method found
 			Method candidate = candidates.getFirst();
 
 			// return the result of a call to java.lang.Class.getMethod() to get the most specific implementation
 			// of the candidate method
 			return objectType.getMethod(candidate.getName(), candidate.getParameterTypes());
 		}
 		default: // more than one candidate method found (the method call is ambiguous)
 			throw new B3AmbiguousFunctionSignatureException(functionName, types);
 		}
 	}
 
 	@Override
 	public Type getDeclaredFunctionType(String functionName, Type[] types) throws Throwable {
 		Method m = findMethod(functionName, types);
 
 		return TypeUtils.objectify(m.getReturnType());
 	}
 
 } // BSystemContextImpl
