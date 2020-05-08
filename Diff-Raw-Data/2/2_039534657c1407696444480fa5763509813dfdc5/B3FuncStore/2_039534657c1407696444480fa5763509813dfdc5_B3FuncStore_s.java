 package org.eclipse.b3.backend.core;
 
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.b3.backend.evaluator.b3backend.BExecutionContext;
 import org.eclipse.b3.backend.evaluator.b3backend.BFunction;
 import org.eclipse.b3.backend.evaluator.b3backend.BGuard;
 import org.eclipse.b3.backend.evaluator.typesystem.TypeUtils;
 
 public class B3FuncStore {
 	private Set<String> dirtyFunctions = new HashSet<String>();
 	private Map<String, List<BFunction>> defined;
 	private Map<String, List<BFunction>> effective;
 	private B3FuncStore parentStore;
 	
 	public B3FuncStore (B3FuncStore parent) {
 		parentStore = parent;
 		defined = new HashMap<String, List<BFunction>>();
 		effective = new HashMap<String, List<BFunction>>();
 	}
 	/**
 	 * - If a function with the exact same name and parameters is already installed then:
 	 *     - is this allowed ? would mean an override of same function in same scope
 	 *     
 	 * @param name
 	 * @param func
 	 */
 	public void defineFunction(String name, BFunction func) {
 		dirtyFunctions.add(name);
 		effective.remove(name);
 		
 		List<BFunction> list = defined.get(name);
 		if(list == null)
 			defined.put(name, list = new ArrayList<BFunction>());
 		list.add(func);
 		
 	}
 	public void undefineFunction(String name, BFunction func) {
 		dirtyFunctions.add(name);
 		effective.remove(name);
 		if(defined == null)
 			return;
 		List<BFunction> fList = defined.get(name);
 		if(fList == null)
 			return;
 		fList.remove(func);
 	}
 	private List<BFunction> getFunctionsByName(String name) {
 		if(defined == null)
 			return Collections.emptyList();
 		return defined.get(name);
 	}
 	
 	public Object callFunction(String functionName, Object[] parameters, Type[] types, BExecutionContext ctx) throws Throwable {
 		// find the best matching function, call it, or throw B3NoSuchFunctionException
 		
 		// if there is no function here for the name, delegate the task (do not want to add things to this
 		// cache for non overloaded functions).
 		List<BFunction> f = getFunctionsByName(functionName);
 		if(f == null || f.size() < 1) {
 			if(parentStore == null)
 				throw new B3NoSuchFunctionException(functionName);
 			else
 				return parentStore.callFunction(functionName, parameters, types, ctx);
 		}
 		// if the cache is dirty for name - update the cache
 		if(dirtyFunctions.contains(functionName))
 			updateCache(functionName);
 	
 		BFunction toBeCalled = getBestFunction(functionName, parameters, types, ctx);
 		if(toBeCalled == null)
 			throw new B3NoSuchFunctionSignatureException(functionName, types);
 		return toBeCalled.internalCall(ctx.createOuterContext(), parameters, types); 
 	}
 	private void updateCache(String name) throws B3EngineException {
 		effective.put(name, getEffectiveList(name, getFunctionsByName(name).size()));
 		dirtyFunctions.remove(name);
 	}
 	private List<BFunction> getEffectiveList(String name, int size) throws B3EngineException {
 		if(parentStore == null) {
 			List<BFunction> thisList = getFunctionsByName(name);
 			List<BFunction> result = new ArrayList<BFunction>(size+thisList.size());
 			result.addAll(thisList);
 			return result;
 		}
 		List<BFunction> thisList = getFunctionsByName(name);
 		List<BFunction> parentList = parentStore.getEffectiveList(name, size+thisList.size());
 		List<BFunction> overloaded = new ArrayList<BFunction>();
 		for(BFunction f : thisList)
 			for(BFunction f2 : parentList) {
 				// if f has same signature as f2, make it hide predecessor
 				if(hasEqualSignature(f,f2)) {
 					if(!hasCompatibleReturnType(f2, f))
 						throw new B3IncompatibleReturnTypeException(f2.getReturnType(), f.getReturnType());
 					overloaded.add(f2);
 					// TODO: f.setOverloads(f2); // for 'proceed' functionality
 				}
 			}
 		parentList.removeAll(overloaded);
 		parentList.addAll(thisList);
 		return parentList;
 	}
 	/**
 	 * Returns true if the overloaded type is assignable from the overloading type.
 	 * @param overloaded
 	 * @param overloading
 	 * @return
 	 */
 	private boolean hasCompatibleReturnType(BFunction overloaded, BFunction overloading)
 	{
 		Type at = overloaded.getReturnType();
 		Type bt = overloading.getReturnType();
 		return at.getClass().isAssignableFrom(bt.getClass());
 	}
 	
 	/**
 	 * Returns true if the functions have the same number of parameters, compatible var args flag,
 	 * and equal parameter types.
 	 * @param a
 	 * @param b
 	 * @return
 	 */
 	private boolean hasEqualSignature(BFunction a, BFunction b) {
 		Type[] pta = a.getParameterTypes();
 		Type[] ptb = b.getParameterTypes();
 		if(pta.length != ptb.length)
 			return false;
 		if(a.isVarArgs() != b.isVarArgs())
 			return false;
 		for(int i = 0; i < pta.length; i++)
 			if(! pta[i].equals(ptb[i]))
 				return false;
 		return true;
 	}
 	/**
 	 * Find the best function matching the parameters.
 	 * @param name
 	 * @param parameters
 	 * @param types
 	 * @param ctx
 	 * @return best function, or null, if none was found.
 	 * @throws B3EngineException if there are exceptions while evaluating guards, or underlying issues.
 	 */
 	private BFunction getBestFunction(String name, Object[] parameters, Type[] types, BExecutionContext ctx) throws B3EngineException {
 		List<BFunction> list = effective.get(name);
 		List<BFunction> candidates = null;
 		BFunction found = null;
 		if(list.size() < 1)
 			throw new B3NoSuchFunctionException(name); // something is really wrong if that happens here
 		perFunction : for(BFunction f : list) {
 			Type[] pt = f.getParameterTypes();
 			if(!f.isVarArgs()) {
 				if(types.length != pt.length)
 					continue perFunction; // not a match
 				for(int i = 0; i < pt.length; i++)
 					if(!TypeUtils.isAssignableFrom(pt[i], types[i]))
 						continue perFunction;
 				// found a candidate
 				if(found != null) {
 					// lazy creation of candidate list (typically there is only one candidate)
 					if(candidates == null)
 						candidates = new ArrayList<BFunction>();
 					candidates.add(found);
 					}
 				found = f;
 				}
 			else {
 				// for varargs
 				if(!TypeUtils.isArray(pt[pt.length-1]))
 					throw new IllegalArgumentException(
						"A method with name: '"+ name + "', declared to have varargs does not have an array as its last parameter");
 				// list can be one item shorter than the list
 				if(types.length < pt.length -1)
 					continue perFunction; // not a match - list is too short
 
 				// compare all types except last (which is the varargs spec)
 				int limit = pt.length-1;
 				for(int i = 0; i < limit; i++)
 					if(!TypeUtils.isAssignableFrom(pt[i], types[i]))
 						continue perFunction;
 				
 				Class<?> varArgsType;
 				// check compatibility of varargs
 				if(types.length >= pt.length) {
 					varArgsType = TypeUtils.getArrayComponentClass(pt[pt.length-1]);
 					if(varArgsType != Object.class) // no need to check if type is object - anything goes
 						for(int i = limit; i < types.length; i++)
 							if(! TypeUtils.isAssignableFrom(varArgsType, types[i])) // incompatible var arg
 								continue perFunction;
 					
 				// found a candidate
 				if(found != null) {
 					// lazy creation of candidate list (typically there is only one candidate)
 					if(candidates != null)
 						candidates = new ArrayList<BFunction>();
 					candidates.add(found);
 					}
 				found = f;
 				}
 			}
 		}
 		// all candidates found - now return best match
 		if(candidates == null) {// if there were < 2 candidates
 			if(found == null)
 				return null;
 
 			BGuard guard = found.getGuard();
 			if(guard != null ) {
 				try {
 					if(guard.accepts(found, ctx, parameters, types))
 						return found;
 					throw new B3NotAcceptedByGuardException(name, types);
 				} catch (Throwable e) {
 					throw new B3EngineException("evaluation of guard ended with exception", e);
 				}
 			}
 			return found;
 		}
 		candidates.add(found); // to make it easier to process all
 		int best = Integer.MAX_VALUE;
 		BFunction bestFunc = null;
 		List<Throwable> exceptions = null;
 		eachCandidate: for(BFunction candidate : candidates) {
 			BGuard guard = candidate.getGuard();
 			if(guard != null ) {
 				try {
 					if(!guard.accepts(candidate, ctx, parameters, types))
 						continue eachCandidate;
 				} catch (Throwable e) {
 					if(exceptions == null) 
 						exceptions = new ArrayList<Throwable>();
 					exceptions.add(e);
 					e.printStackTrace();
 					continue eachCandidate;
 				}
 			}
 			
 			int distance = specificity(candidate, types);
 			if(distance < best) {
 				if(distance == 0)
 					return candidate; // unbeatable
 				best = distance;
 				bestFunc = candidate;
 			}
 		}
 		
 		// TODO: HANDLE GUARD FAILURES DIFFERENTLY ? GIVE UP AT ONCE ?
 		if(bestFunc == null) {
 			if(exceptions != null)
 				throw new B3EngineException(
 						"evaluation of guard ended with exception(s) see stacktraces - showing first out of "
 						+ Integer.toString(exceptions.size()), 
 						exceptions.get(0));
 
 			throw new B3NotAcceptedByGuardException(name, types);
 		}	
 		return bestFunc;
 	}
 	/**
 	 * Computes the specificity distance of the function.
 	 * @param f
 	 * @param types
 	 * @return parameter distance from stated types - 0 is most specific
 	 */
 	public int specificity(BFunction f, Type[] types) {
 		Type[] pt = f.getParameterTypes();
 		int distance = 0;
 		for(int i = 0; i < pt.length;i++)
 			distance += TypeUtils.typeDistance(pt[i], types[i]);
 //		
 //			Class ptc = pt[i].getClass();
 //			if(ptc.isInterface())
 //				distance += (1+TypeUtils.interfaceDistance(ptc, types[i]));
 //			else
 //				distance += TypeUtils.classDistance(ptc, types[i]);
 //		}
 		return distance;
 	}
 	public Type getDeclaredFunctionType(String functionName, Type[] types, BExecutionContext ctx) throws B3EngineException {
 		// find the best matching function, and return its type, or throw B3NoSuchFunctionException
 		
 		// if there is no function here for the name, delegate the task (do not want to add things to this
 		// cache for non overloaded functions).
 		List<BFunction> f = getFunctionsByName(functionName);
 		if(f == null || f.size() < 1) {
 			if(parentStore == null)
 				throw new B3NoSuchFunctionException(functionName);
 			else
 				return parentStore.getDeclaredFunctionType(functionName, types, ctx);
 		}
 		// if the cache is dirty for name - update the cache
 		if(dirtyFunctions.contains(functionName))
 			updateCache(functionName);
 	
 		// TODO: CHEATING !!! When evaluating the type, the parameter values are not present.
 		// Most guards would only look at the types, but an instance guard actually cares. However,
 		// the important is to find the return type of the function. Guards needs to know that they will
 		// be called with all parameters set to null (i.e. they can't guard against that). A better typesystem
 		// solution is required. There are several bad situations (e.g. finding a function that when not instance 
 		// guarded is not found) - but no worse than where there are no guards :)
 		// TODO: TYPESYSTEM.
 		//
 		Object[] fakeParameters = new Object[types.length];
 		BFunction toBeCalled = getBestFunction(functionName, fakeParameters, types, ctx);
 		if(toBeCalled == null)
 			throw new B3NoSuchFunctionSignatureException(functionName, types);
 
 		return toBeCalled.getReturnTypeForParameterTypes(types,ctx); 
 	}
 
 
 }
