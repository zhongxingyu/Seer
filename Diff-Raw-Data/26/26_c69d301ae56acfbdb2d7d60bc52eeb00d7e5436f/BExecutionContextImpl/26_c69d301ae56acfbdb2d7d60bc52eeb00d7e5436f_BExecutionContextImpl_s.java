 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.b3.backend.evaluator.b3backend.impl;
 
 import java.lang.reflect.Type;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.eclipse.b3.backend.core.B3Backend;
 import org.eclipse.b3.backend.core.B3EngineException;
 import org.eclipse.b3.backend.core.B3FuncStore;
 import org.eclipse.b3.backend.core.B3FunctionLoadException;
 import org.eclipse.b3.backend.core.B3InternalError;
 import org.eclipse.b3.backend.core.B3NoSuchFunctionException;
 import org.eclipse.b3.backend.core.B3NoSuchFunctionSignatureException;
 import org.eclipse.b3.backend.core.B3WeavingFailedException;
 import org.eclipse.b3.backend.core.LValue;
 import org.eclipse.b3.backend.core.B3FinalVariableRedefinitionException;
 import org.eclipse.b3.backend.core.B3NoContextException;
 import org.eclipse.b3.backend.core.B3NoSuchVariableException;
 import org.eclipse.b3.backend.core.ValueMap;
 import org.eclipse.b3.backend.evaluator.BackendHelper;
 import org.eclipse.b3.backend.evaluator.b3backend.B3MetaClass;
 import org.eclipse.b3.backend.evaluator.b3backend.B3ParameterizedType;
 import org.eclipse.b3.backend.evaluator.b3backend.B3backendFactory;
 import org.eclipse.b3.backend.evaluator.b3backend.B3backendPackage;
 import org.eclipse.b3.backend.evaluator.b3backend.BConcern;
 import org.eclipse.b3.backend.evaluator.b3backend.BConcernContext;
 import org.eclipse.b3.backend.evaluator.b3backend.BContext;
 import org.eclipse.b3.backend.evaluator.b3backend.BDelegatingContext;
 import org.eclipse.b3.backend.evaluator.b3backend.BExecutionContext;
 import org.eclipse.b3.backend.evaluator.b3backend.BGuardFunction;
 import org.eclipse.b3.backend.evaluator.b3backend.BInnerContext;
 import org.eclipse.b3.backend.evaluator.b3backend.BJavaCallType;
 import org.eclipse.b3.backend.evaluator.b3backend.BJavaFunction;
 import org.eclipse.b3.backend.evaluator.b3backend.BParameterDeclaration;
 import org.eclipse.b3.backend.evaluator.b3backend.BSystemContext;
 import org.eclipse.b3.backend.evaluator.b3backend.BTypeCalculatorFunction;
 
 import org.eclipse.b3.backend.evaluator.b3backend.BInvocationContext;
 import org.eclipse.b3.backend.evaluator.b3backend.IFunction;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.b3.backend.evaluator.typesystem.TypeUtils;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.impl.EObjectImpl;
 import org.eclipse.emf.ecore.util.EObjectResolvingEList;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>BExecution Context</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.b3.backend.evaluator.b3backend.impl.BExecutionContextImpl#getParentContext <em>Parent Context</em>}</li>
  *   <li>{@link org.eclipse.b3.backend.evaluator.b3backend.impl.BExecutionContextImpl#getValueMap <em>Value Map</em>}</li>
  *   <li>{@link org.eclipse.b3.backend.evaluator.b3backend.impl.BExecutionContextImpl#getFuncStore <em>Func Store</em>}</li>
  *   <li>{@link org.eclipse.b3.backend.evaluator.b3backend.impl.BExecutionContextImpl#getEffectiveConcerns <em>Effective Concerns</em>}</li>
  *   <li>{@link org.eclipse.b3.backend.evaluator.b3backend.impl.BExecutionContextImpl#getProgressMonitor <em>Progress Monitor</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public abstract class BExecutionContextImpl extends EObjectImpl implements BExecutionContext {
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final String copyright = "Copyright (c) 2009, Cloudsmith Inc and others.\nAll rights reserved. This program and the accompanying materials\nare made available under the terms of the Eclipse Public License v1.0\nwhich accompanies this distribution, and is available at\nhttp://www.eclipse.org/legal/epl-v10.html\n\rContributors:\n- Cloudsmith Inc - initial API and implementation.\r";
 
 	/**
 	 * The cached value of the '{@link #getParentContext() <em>Parent Context</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getParentContext()
 	 * @generated
 	 * @ordered
 	 */
 	protected BExecutionContext parentContext;
 
 	/**
 	 * The default value of the '{@link #getValueMap() <em>Value Map</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * Implementation note: Creates a static instance (that is never used). Generated code modified to set null value. Constructor
 	 * creates a new value.
 	 * <!-- end-user-doc -->
 	 * @see #getValueMap()
 	 * @generated NOT
 	 * @ordered
 	 */
 	protected static final ValueMap VALUE_MAP_EDEFAULT = null; //(ValueMap)B3backendFactory.eINSTANCE.createFromString(B3backendPackage.eINSTANCE.getValueMap(), "");
 	/**
 	 * The cached value of the '{@link #getValueMap() <em>Value Map</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getValueMap()
 	 * @generated
 	 * @ordered
 	 */
 	protected ValueMap valueMap = VALUE_MAP_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getFuncStore() <em>Func Store</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getFuncStore()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final B3FuncStore FUNC_STORE_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getFuncStore() <em>Func Store</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getFuncStore()
 	 * @generated
 	 * @ordered
 	 */
 	protected B3FuncStore funcStore = FUNC_STORE_EDEFAULT;
 
 	/**
 	 * The cached value of the '{@link #getEffectiveConcerns() <em>Effective Concerns</em>}' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getEffectiveConcerns()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<BConcern> effectiveConcerns;
 
 	/**
 	 * The default value of the '{@link #getProgressMonitor() <em>Progress Monitor</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getProgressMonitor()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final IProgressMonitor PROGRESS_MONITOR_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getProgressMonitor() <em>Progress Monitor</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getProgressMonitor()
 	 * @generated
 	 * @ordered
 	 */
 	protected IProgressMonitor progressMonitor = PROGRESS_MONITOR_EDEFAULT;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	protected BExecutionContextImpl() {
 		super();
 		this.valueMap = new ValueMap();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return B3backendPackage.Literals.BEXECUTION_CONTEXT;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public BExecutionContext getParentContext() {
 		if (parentContext != null && parentContext.eIsProxy()) {
 			InternalEObject oldParentContext = (InternalEObject)parentContext;
 			parentContext = (BExecutionContext)eResolveProxy(oldParentContext);
 			if (parentContext != oldParentContext) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, B3backendPackage.BEXECUTION_CONTEXT__PARENT_CONTEXT, oldParentContext, parentContext));
 			}
 		}
 		return parentContext;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public BExecutionContext basicGetParentContext() {
 		return parentContext;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setParentContext(BExecutionContext newParentContext) {
 		BExecutionContext oldParentContext = parentContext;
 		parentContext = newParentContext;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, B3backendPackage.BEXECUTION_CONTEXT__PARENT_CONTEXT, oldParentContext, parentContext));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ValueMap getValueMap() {
 		return valueMap;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setValueMap(ValueMap newValueMap) {
 		ValueMap oldValueMap = valueMap;
 		valueMap = newValueMap;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, B3backendPackage.BEXECUTION_CONTEXT__VALUE_MAP, oldValueMap, valueMap));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public B3FuncStore getFuncStore() {
 		return funcStore;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setFuncStore(B3FuncStore newFuncStore) {
 		B3FuncStore oldFuncStore = funcStore;
 		funcStore = newFuncStore;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, B3backendPackage.BEXECUTION_CONTEXT__FUNC_STORE, oldFuncStore, funcStore));
 	}
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<BConcern> getEffectiveConcerns() {
 		if (effectiveConcerns == null) {
 			effectiveConcerns = new EObjectResolvingEList<BConcern>(BConcern.class, this, B3backendPackage.BEXECUTION_CONTEXT__EFFECTIVE_CONCERNS);
 		}
 		return effectiveConcerns;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * Returns a progress monitor set in the context, or if null, the parent context's progress monitor.
 	 * If the top most context does not have a progress monitor, a NullProgressMonitor is created and set.
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public IProgressMonitor getProgressMonitor() {
 		if(progressMonitor != null)
 			return progressMonitor;
 		else if(getParentContext() != null)
 			return getParentContext().getProgressMonitor();
 		setProgressMonitor(new NullProgressMonitor());
 		return progressMonitor;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setProgressMonitor(IProgressMonitor newProgressMonitor) {
 		IProgressMonitor oldProgressMonitor = progressMonitor;
 		progressMonitor = newProgressMonitor;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, B3backendPackage.BEXECUTION_CONTEXT__PROGRESS_MONITOR, oldProgressMonitor, progressMonitor));
 	}
 
 	/**
 	 * Creates a func store if not already created and links it to the first found parent context
 	 * with a func store.
 	 * (Note: It is not possible for this thread to create new functions in outer context while this
 	 * context is in effect.)
 	 */
 	protected void createFuncStore() {
 		if(funcStore != null)
 			return;
 		setFuncStore(new B3FuncStore(getEffectiveFuncStore()));
 	}
 	/**
 	 * Returns the first found func store (or null, if none is found). The func store to return is
 	 * obtained via {@link #getFuncStore()} thus giving derived classes a chance to override.
 	 * @return
 	 */
 	protected B3FuncStore getEffectiveFuncStore() {
 		BExecutionContext p = this;
 		B3FuncStore fs;
 
 		do {
 			if((fs = p.getFuncStore()) != null)
 				return fs;
 		} while((p = p.getParentContext()) != null);
 
 		return null;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * Loads static functions from a java class. Using B3Backend annotations to
 	 * direct the translation into B3 functions.
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public void loadFunctions(Class<? extends Object> clazz) throws B3EngineException {
 		createFuncStore();
 
 		Map<String, BJavaFunction> systemFunctions = new HashMap<String, BJavaFunction>();
 		Map<String, BJavaFunction> guards = new HashMap<String, BJavaFunction>();
 		Map<String, BJavaFunction> typeCalculators = new HashMap<String, BJavaFunction>();
 
 		Map<String, List<BJavaFunction>> systemProxies = new HashMap<String, List<BJavaFunction>>();
 		Map<String, List<BJavaFunction>> guardedFunctions = new HashMap<String, List<BJavaFunction>>();
 		Map<String, List<BJavaFunction>> typeCalculatedFunctions = new HashMap<String, List<BJavaFunction>>();
 
 		// create and initialize a BJavaFunction to represent each public static function
 		for(Method m : clazz.getDeclaredMethods()) {
 			// the call type of the function may later change to system call
 			BJavaFunction f = createJavaFunction(m, BJavaCallType.FUNCTION);
 
 			if(f == null)
 				continue;
 
 			// check for annotations
 			B3Backend annotation = m.getAnnotation(B3Backend.class);
 
 			// take a shortcut if there is no annotation
 			if(annotation == null) {
 				funcStore.defineFunction(m.getName(), f);
 				continue;
 			}
 
 			if(annotation.hideOriginal()) {
				String[] names = annotation.funcNames();
 				if(names == null || names.length == 0)
 					throw new B3FunctionLoadException("hideOriginal annotation specified but not funcNames annotation",
 							m);
 				f.setName(names[0]);
 			}
 
 			f.setExecutionMode(BackendHelper.getExecutionMode(annotation));
 			f.setVisibility(BackendHelper.getVisibility(annotation));
 
 			// If the function is a system function, set it aside and patch the functions proxying it later.
 			if(annotation.system()) {
 				systemFunctions.put(f.getName(), f);
 				continue;
 			}
 
 			// If the function is a guard, set it aside and patch the functions using it later.
 			if(annotation.guard()) {
 				// guards are called using system calling convention
 				f.setCallType(BJavaCallType.SYSTEM);
 				guards.put(f.getName(), f);
 				continue;
 			}
 
 			// If the function is a type calculator, set it aside and patch the functions using it later.
 			if(annotation.typeCalculator()) {
 				typeCalculators.put(f.getName(), f);
 				continue;
 			}
 
 			// add defined function to the func store
 			if(!annotation.hideOriginal())
 				funcStore.defineFunction(m.getName(), f);
			for(String fname : annotation.funcNames())
				funcStore.defineFunction(fname, f);
 
 			// if a function is a proxy for a system function, remember it
 			{
 				String systemFunctionName = annotation.systemFunction();
 				if(systemFunctionName != null && systemFunctionName.length() > 0) {
 					List<BJavaFunction> fs = null;
 					if((fs = systemProxies.get(systemFunctionName)) == null)
 						systemProxies.put(systemFunctionName, fs = new ArrayList<BJavaFunction>());
 					fs.add(f);
 				}
 			}
 
 			// if a function is guarded, remember it
 			{
 				String guardFunctionName = annotation.guardFunction();
 				if(guardFunctionName != null && guardFunctionName.length() > 0) {
 					List<BJavaFunction> gf = null;
 					if((gf = guardedFunctions.get(guardFunctionName)) == null)
 						guardedFunctions.put(guardFunctionName, gf = new ArrayList<BJavaFunction>());
 					gf.add(f);
 				}
 			}
 
 			// if a function has a type calculator, remember it
 			{
 				String typeCalculatorFunctionName = annotation.typeFunction();
 				if(typeCalculatorFunctionName != null && typeCalculatorFunctionName.length() > 0) {
 					List<BJavaFunction> tf = null;
 					if((tf = typeCalculatedFunctions.get(typeCalculatorFunctionName)) == null)
 						typeCalculatedFunctions.put(typeCalculatorFunctionName, tf = new ArrayList<BJavaFunction>());
 					tf.add(f);
 				}
 			}
 		}
 
 		// patch system functions
 		// ---
 		// revisit all functions that reference a system function and replace their method
 		// with the system method, and also indicate that the call should be made as a system
 		// call.
 		for(Entry<String, List<BJavaFunction>> e : systemProxies.entrySet()) {
 			for(BJavaFunction s : e.getValue()) {
 				BJavaFunction sys = systemFunctions.get(e.getKey());
 				if(sys == null)
 					throw new B3FunctionLoadException("reference to system function: " + e.getKey()
 							+ " can not be satisfied - no such method found.", s.getMethod());
 				// patch the method in the func store with values from the system function
 				s.setCallType(BJavaCallType.SYSTEM);
 				s.setMethod(sys.getMethod());
 			}
 		}
 
 		// link guards
 		// ---
 		// revisit all functions that reference a guardFunction and set that function as a guard
 		// 
 		for(Entry<String, List<BJavaFunction>> e : guardedFunctions.entrySet()) {
 			for(BJavaFunction guarded : e.getValue()) {
 				BJavaFunction g = guards.get(e.getKey());
 				if(g == null)
 					throw new B3FunctionLoadException("reference to guard function: " + e.getKey()
 							+ " can not be satisfied - no such guard found.", guarded.getMethod());
 				// set the guard function, wrapped in a guard
 				BGuardFunction gf = B3backendFactory.eINSTANCE.createBGuardFunction();
 				gf.setFunc(g);
 				guarded.setGuard(gf);
 			}
 		}
 
 		// link type calculators
 		// ---
 		// revisit all functions that reference a typeFunction and set that function as a typeCalculator
 		// 
 		for(Entry<String, List<BJavaFunction>> e : typeCalculatedFunctions.entrySet()) {
 			for(BJavaFunction typed : e.getValue()) {
 				BJavaFunction tc = typeCalculators.get(e.getKey());
 				if(tc == null)
 					throw new B3FunctionLoadException("reference to type calculator function: " + e.getKey()
 							+ " can not be satisfied - no such function found.", typed.getMethod());
 				// set the guard function, wrapped in a guard
 				BTypeCalculatorFunction tcf = B3backendFactory.eINSTANCE.createBTypeCalculatorFunction();
 				tcf.setFunc(tc);
 				typed.setTypeCalculator(tcf);
 			}
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * Loads a single method as a function. The method may be static.
 	 * If the method is not public, null is returned, and a function was not defined.
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public BJavaFunction loadFunction(Method m) throws B3EngineException {
 		createFuncStore();
 
 		BJavaFunction javaFunction = createJavaFunction(m, BJavaCallType.METHOD);
 		if(javaFunction != null)
 			funcStore.defineFunction(javaFunction.getName(), javaFunction);
 
 		return javaFunction;
 	}
 
 	private static BJavaFunction createJavaFunction(Method m, BJavaCallType callType) {
 		int modifiers = m.getModifiers();
 		boolean isStatic;
 
 		// only handle public methods and also require the methods to be static if the call type is different from
 		// BJavaCallType.METHOD
 		if(!(Modifier.isPublic(modifiers) && ((isStatic = Modifier.isStatic(modifiers)) || callType == BJavaCallType.METHOD)))
 			return null;
 
		// create and initialize a BJavaFunction to represent the method m
 		BJavaFunction f = B3backendFactory.eINSTANCE.createBJavaFunction();
 
 		// TODO: f's Resource ?!?
 		// String fileName = m.getDeclaringClass().getCanonicalName();
 		// fileRef.setFileName(fileName == null ? "anonymous class" : fileName);
 
 		f.setName(m.getName());
 		f.setMethod(m); // add the thing to call
 		f.setFinal(Modifier.isFinal(modifiers)); // make it final in b3 as well
 		f.setCallType(callType);
 		f.setVarArgs(m.isVarArgs());
 		f.setTypeParameters(m.getTypeParameters());
 
 		Type[] instanceParameterTypes;
 		String[] instanceParameterNames;
 
 		if(callType == BJavaCallType.METHOD) {
 			// only set this for methods with call type == BJavaCallType.METHOD
 			f.setClassFunction(isStatic); // invokable via class - i.e. "static" in java
 			if(isStatic) {
 				B3MetaClass metaClass = B3backendFactory.eINSTANCE.createB3MetaClass();
 				metaClass.setInstanceClass(m.getDeclaringClass());
 				instanceParameterTypes = new Type[] { metaClass };
 				instanceParameterNames = new String[] { "class" };
 			}
 			else {
 				instanceParameterTypes = new Type[] { m.getDeclaringClass() };
 				instanceParameterNames = new String[] { "this" };
 			}
 		}
 		else {
 			instanceParameterTypes = null;
 			instanceParameterNames = null;
 		}
 
 		TypeUtils.JavaCandidate javaFunctionCandidate = (TypeUtils.JavaCandidate) FunctionCandidateAdapterFactory.eINSTANCE.adapt(f);
 		// this eventually calls f.setParameterTypes()
 		javaFunctionCandidate.processJavaParameterTypes(instanceParameterTypes);
 
 		f.setParameterNames(getParameterNames(m.getParameterAnnotations(), instanceParameterNames));
 
 		setParameterDeclarations(f);
 
 		f.setReturnType(TypeUtils.objectify(m.getGenericReturnType()));
 		f.setExceptionTypes(m.getGenericExceptionTypes());
 
 		return f;
 	}
 	private static void setParameterDeclarations(BJavaFunction f) {
 		Type[] types = f.getParameterTypes();
 		String[] names = f.getParameterNames();
 		if(types.length != names.length)
 			throw new IllegalArgumentException("All types must have a name");
 		EList<BParameterDeclaration> parameters = f.getParameters();
 		for(int i = 0; i < types.length; i++) {
 			BParameterDeclaration decl = B3backendFactory.eINSTANCE.createBParameterDeclaration();
 			Type type = types[i];
 			if(!(type instanceof EObject)) {
 				B3ParameterizedType b3type = B3backendFactory.eINSTANCE.createB3ParameterizedType();
 				b3type.setRawType(type);
 				types[i] = type = b3type; // modify the parameterTypes array as well to keep it in sync
 			}
 			decl.setType(type);
 			decl.setName(names[i]);
 			parameters.add(decl);
 		}
 	}
 	private static String[] getParameterNames(Annotation[][] allParametersAnnotations, String... instanceParameterNames) {
 		int instanceParameterNamesCount = instanceParameterNames != null
 				? instanceParameterNames.length
 				: 0;
 		String parameterNames[] = new String[instanceParameterNamesCount + allParametersAnnotations.length];
 		HashSet<String> usedNames = new HashSet<String>();
 		// first process the instance parameter names - don't allow duplicates
 		for(int i = 0 ; i < instanceParameterNamesCount; ++i) {
 			String name = instanceParameterNames[i];
 			if(!usedNames.contains(name)) {
 				parameterNames[i] = name;
 				usedNames.add(name);
 			}
 		}
 		// now look for parameter names defined by annotations - watch for duplicates again
 		for(int i = 0; i < allParametersAnnotations.length; ++i) {
 			Annotation[] parameterAnnotations = allParametersAnnotations[i];
 			if(parameterAnnotations != null) {
 				for(int j = 0; j < parameterAnnotations.length; ++j) {
 					if(parameterAnnotations[j] instanceof B3Backend) {
 						String name = ((B3Backend) parameterAnnotations[j]).name();
 						if(!usedNames.contains(name)) {
 							parameterNames[instanceParameterNamesCount + i] = name;
 							usedNames.add(name);
 						}
 						break; // only use first name declared for the parameter
 					}
 				}
 			}
 		}
 		// finally supply default unique names for those parameters which have no name assigned by now
 		for(int i = 0, j = 0; i < parameterNames.length; ++i) {
 			if(parameterNames[i] == null) {
 				String name;
 				while(usedNames.contains(name = toAlphabetString(j++)))
 					;
 				parameterNames[i] = name;
 				// usedNames.add(name); // this is not necessary as the toAlphabetString(j++) will not produce
 				// duplicates
 			}
 		}
 		return parameterNames;
 	}
 	private static String toAlphabetString(long number) {
 		if(number < 0)
 			throw new IllegalArgumentException();
 
 		StringBuilder buf = new StringBuilder();
 
 		while(number >= ('z' - 'a' + 1)) {
 			buf.append((char) ('a' + (int) (number % ('z' - 'a' + 1))));
 			number = number / ('z' - 'a' + 1) - 1;
 		}
 		buf.append((char) ('a' + (int) number));
 
 		return buf.reverse().toString();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public Object callFunction(String functionName, Object[] parameters,Type[] types) throws Throwable {
 //		if("addedLater".equals(functionName)) {
 //			functionName = functionName + ""; // dummy for debugging
 //		}
 		B3FuncStore fStore = getEffectiveFuncStore();
 		if(fStore == null)
 			throw new B3InternalError("Could not find an effective function store - engine/context setup is broken!");
 		Throwable lastError = null;
 		Throwable lastStaticError = null;
 		ATTEMPTS: for(int attempts = 0; attempts < 5; attempts++) {
 			switch(attempts) {
 			case 0:	// fall through
 			case 3: // try a call with the parameters as stated
 				try {
 					return fStore.callFunction(functionName, parameters, types, this);
 				} catch (B3NoSuchFunctionException e) {
 					attempts++; // skip second attempt
 					lastError = e;
 					continue;
 				} catch (B3NoSuchFunctionSignatureException e2) {
 					lastError = e2;
 					continue;
 				}
 			case 1: // fall through
 			case 4: // try a static call
 				try {
 					if(parameters.length > 0) {
 					Object[] newParameters = new Object[parameters.length+1];
 					System.arraycopy(parameters, 0, newParameters, 1, parameters.length);
 					B3MetaClass metaClass = B3backendFactory.eINSTANCE.createB3MetaClass();
 					metaClass.setInstanceClass(TypeUtils.getRaw(types[0]));
 					newParameters[0] = types[0];
 					Type[] newTypes = new Type[types.length+1];
 					System.arraycopy(types, 0, newTypes, 1, types.length);
 					newTypes[0] = metaClass;
 					return fStore.callFunction(functionName, newParameters, newTypes, this);
 					}
 				} catch (B3NoSuchFunctionException e3) {
 					lastStaticError = e3;
 					continue;
 				} catch (B3NoSuchFunctionSignatureException e4) {
 					lastStaticError = e4;
 					continue;
 				}
 				break;
 			case 2: // try loading the method in the java context
 				BExecutionContext systemCtx = this.getInvocationContext().getParentContext();
 				if(!(systemCtx instanceof BSystemContext))
 					throw new B3InternalError("The parent of the invocation context must be an instance of BSystemContext");
 				IFunction loaded = null;
 				if((loaded=((BSystemContext)systemCtx).loadMethod(functionName, types)) != null) {
 					weaveLoaded(loaded, this);
 					continue; // attempt calling the (possibly advised) function
 				}
 				break ATTEMPTS;
 			default:
 				throw new B3InternalError("BExecutionContextImpl#callFunction() - broken call strategy loop :" + String.valueOf(attempts));
 			}
 		}
 		// Successful call should have returned result already - only error conditions remain
 		if(lastError == null && lastStaticError == null)
 			throw new B3InternalError("BExecutionContextImpl#callFunction() did not return ok, and had no last error");
 		
 		throw lastError != null ? lastError : lastStaticError;
 
 	}
 	private void weaveLoaded(IFunction f, BExecutionContext ctx) throws B3WeavingFailedException{
 		if(ctx == null)
 			return;
 		B3FuncStore fs = ctx.getFuncStore();
 		if(fs != null)
 			try {
 				fs.updateCache(f.getName());
 			} catch (B3EngineException e1) {
 				throw new B3WeavingFailedException(e1);
 			}
 		weaveLoaded(f, ctx.getParentContext());
 		for(BConcern c : ctx.getEffectiveConcerns()) {
 			boolean savedWeaving = isWeaving;
 			try {
 				isWeaving = true;
 				c.evaluateIfMatching(f, ctx);
 			} catch (Throwable e) {
 				throw new B3WeavingFailedException(e);
 			} finally {
 				isWeaving = savedWeaving;
 			}
 		}
 				
 	}
 	/**
 	 * <!-- begin-user-doc -->
 	 * Looks up the value in the value map obtained by calling {@link #getValueMap()} (which derived classes
 	 * may override).
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public Object getValue(String name) throws B3EngineException {
 		try {
 			return getValueMap().get(name);
 		} catch (B3NoSuchVariableException e) {
 			BExecutionContext parentContext = null;
 			if((parentContext = getParentContext()) == null)
 				throw e;
 			return parentContext.getValue(name);
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public Object defineValue(String name, Object value, Type t) throws B3EngineException {
 		if(isFinal(name))
 			throw new B3FinalVariableRedefinitionException(name);
 		valueMap.defineValue(name, value, t);
 		return value;
 	}
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public Object defineFinalValue(String name, Object value, Type t) throws B3EngineException {
 		if(isFinal(name))
 			throw new B3FinalVariableRedefinitionException(name);
 		valueMap.defineFinalValue(name, value, t);
 		return value;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public Object defineVariableValue(String name, Object value, Type t) throws B3EngineException {
 		if(isFinal(name))
 			throw new B3FinalVariableRedefinitionException(name);
 		valueMap.defineVariable(name, value, t);
 		return value;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public Object defineFinalVariableValue(String name, Object value, Type t) throws B3EngineException {
 		if(isFinal(name))
 			throw new B3FinalVariableRedefinitionException(name);
 		valueMap.defineFinalVariable(name, value, t);
 		return value;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public boolean isFinal(String name) {
 		boolean result = getValueMap().isFinal(name);
 		if(result) return result;
 		if(getParentContext() != null)
 			return getParentContext().isFinal(name);
 		return false;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public boolean isImmutable(String name) {
 		boolean result = getValueMap().isImmutable(name);
 		if(result) return result;
 		if(getParentContext() != null)
 			return getParentContext().isImmutable(name);
 		return false;
 	}
 
 	/**
 	 * Returns the invocation context from the parent chain of contexts. Throws 
 	 * B3NoSuchContextException if the invocation context was not found.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public BInvocationContext getInvocationContext() throws B3EngineException {
 		if(this instanceof BInvocationContext)
 			return (BInvocationContext) this;
 		if(getParentContext() == null)
 			throw new B3NoContextException("InvocationContext");
 		return getParentContext().getInvocationContext();
 	}
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * Creates an inner context (i.e. local blocks which should not be seen from nested outer contexts).
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public BExecutionContext createInnerContext() {
 		BInnerContext inner = B3backendFactory.eINSTANCE.createBInnerContext();
 		inner.setParentContext(this);
 		inner.setOuterContext(this);
 		return inner;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * Produces an Outer-Inner pair with a delegating context between the inner, and 'this' context.
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public BInnerContext createWrappedInnerContext() {
 		// create a BContext (an outer context) that holds the advised information
 		// create a delegating inner context to use while evaluating the nested expression (it has
 		// access to the local inner context.
 		BContext o = B3backendFactory.eINSTANCE.createBContext();
 		BDelegatingContext d = B3backendFactory.eINSTANCE.createBDelegatingContext();
 		BExecutionContext p = this instanceof BInnerContext ? ((BInnerContext)this).getOuterContext() : this;
 		o.setParentContext(p); // parent is the current context's notion of "outer"
 		d.setOuterContext(o); // the delegating context is the outer context to use "downstream"
 		d.setParentContext(this); // the (typically inner) current context is visible in the returned context
 		
 		return (BInnerContext)d.createInnerContext();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public BExecutionContext createOuterContext() {
 		BContext ctx = B3backendFactory.eINSTANCE.createBContext();
 		ctx.setParentContext(this);
 		return ctx;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @throws B3EngineException 
 	 * @generated NOT
 	 */
 	public Type getDeclaredValueType(String name) throws B3EngineException {
 		return getLValue(name).getDeclaredType();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public Type getDeclaredFunctionType(String functionName, Type[] types) throws Throwable {
 		B3FuncStore fStore = getEffectiveFuncStore();
 		if(fStore == null)
 			throw new B3InternalError("Could not find an effective function store - engine/context setup is broken!");
 		Throwable lastError = null;
 		Throwable lastStaticError = null;
 		ATTEMPTS: for(int attempts = 0; attempts < 5; attempts++) {
 			switch(attempts) {
 			case 0:	// fall through
 			case 3: // try a call with the parameters as stated
 				try {
 					return fStore.getDeclaredFunctionType(functionName, types, this);
 				} catch (B3NoSuchFunctionException e) {
 					attempts++; // skip second attempt
 					lastError = e;
 					continue;
 				} catch (B3NoSuchFunctionSignatureException e2) {
 					lastError = e2;
 					continue;
 				}
 			case 1: // fall through
 			case 4: // try a static call
 				try {
 					if(types.length > 0) {
 					B3MetaClass metaClass = B3backendFactory.eINSTANCE.createB3MetaClass();
 					metaClass.setInstanceClass(TypeUtils.getRaw(types[0]));
 					Type[] newTypes = new Type[types.length+1];
 					System.arraycopy(types, 0, newTypes, 1, types.length);
 					newTypes[0] = metaClass;
 					return fStore.getDeclaredFunctionType(functionName, newTypes, this);
 					}
 				} catch (B3NoSuchFunctionException e3) {
 					lastStaticError = e3;
 					continue;
 				} catch (B3NoSuchFunctionSignatureException e4) {
 					lastStaticError = e4;
 					continue;
 				}
 				break;
 			case 2: // try loading the method in the java context
 				BExecutionContext systemCtx = this.getInvocationContext().getParentContext();
 				if(!(systemCtx instanceof BSystemContext))
 					throw new B3InternalError("The parent of the invocation context must be an instance of BSystemContext");
 				IFunction loaded = null;
 				if((loaded = ((BSystemContext)systemCtx).loadMethod(functionName, types)) != null) {
 					weaveLoaded(loaded, this);
 					continue; // attempt calling the (possibly advised) function
 				}
 				break ATTEMPTS;
 			default:
 				throw new B3InternalError("BExecutionContextImpl#getDeclaredFunctionType() - broken call strategy loop :" + String.valueOf(attempts));
 			}
 		}
 		// Successful call should have returned result already - only error conditions remain
 		if(lastError == null && lastStaticError == null)
 			throw new B3InternalError("BExecutionContextImpl#getDeclaredFunctionType() did not return ok, and had no last error");
 		
 		throw lastError != null ? lastError : lastStaticError;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * This, default implementation returns false.
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public boolean isPropertyScope() {
 		return false;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * Returns the first found context that is of the specified class.
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public <T> T getContext(Class<T> clazz) throws B3EngineException {
 		if(clazz.isInstance(this))
 			return clazz.cast(this);
 		if(getParentContext() == null)
 			throw new B3NoContextException(clazz.getName());
 		return getParentContext().getContext(clazz);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * Returns an iterator over the effective func store (i.e. it searches up the parent chain
 	 * to find the first context that actually has functions defined.
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public Iterator<IFunction> getFunctionIterator() {
 		return getEffectiveFuncStore().getFunctionIterator();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * Returns an iterator over the effective func store (i.e. it searches up the parent chain
 	 * to find the first context that actually has functions defined).
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public Iterator<IFunction> getFunctionIterator(String name) {
 		return getEffectiveFuncStore().getFunctionIterator(name);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public Iterator<IFunction> getFunctionIterator(Type type, Class<?> functionType) {
 		return getEffectiveFuncStore().getFunctionIterator(type, functionType);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Iterator<BConcernContext> getConcernIterator(Object candidate) {
 		// TODO: implement this method
 		// Ensure that you remove @generated or mark it @generated NOT
 		throw new UnsupportedOperationException();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public boolean containsValue(String name, boolean allVisible) {
 		boolean result = getValueMap().containsKey(name);
 		if(result || !allVisible) return result;
 		if(getParentContext() != null)
 			return getParentContext().containsValue(name, true);
 		return false;
 
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public LValue getLValue(String name) throws B3EngineException {
 		if(!getValueMap().containsKey(name)) {
 			BExecutionContext parentContext = null;
 			if((parentContext = getParentContext()) == null)
 				throw new B3NoSuchVariableException(name);
 			return parentContext.getLValue(name);
 		}
 		return new ValueMapLVal(name);
 	}
 	private boolean isWeaving = false; // TODO: add as parameter to defineFunction instead
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public IFunction defineFunction(IFunction function) throws B3EngineException {
 		createFuncStore();
 		boolean woven = false;
 		if(!isWeaving)
 			for(BExecutionContext ctx = this; ctx.getParentContext() != null; ctx = ctx.getParentContext()) {
 				for(BConcern c : ctx.getEffectiveConcerns())
 					try {
 						isWeaving = true;
 						if(c.evaluateIfMatching(function, this))
 							woven = true;
 					} catch (Throwable e) {
 						throw new B3WeavingFailedException(e);
 					} finally {
 						isWeaving = false;
 					}
 			}
 		if(!woven)
 			funcStore.defineFunction(function.getName(), function);
 		return function;
 	}
 	protected class ValueMapLVal implements LValue {
 		private String name;
 		ValueMapLVal(String name) {
 			this.name = name;
 		}
 		public Object get() throws B3EngineException {
 			return getValueMap().get(name);
 		}
 
 		public boolean isSettable() {
 			return getValueMap().isImmutable(name);
 		}
 
 		public Object set(Object value) throws B3EngineException {
 			return getValueMap().set(name, value);
 		}
 		public Type getDeclaredType() throws B3EngineException {
 			return getValueMap().getType(name);
 		}
 		public void setDeclaredType(Type t) throws B3EngineException {
 			getValueMap().setType(name, t);
 		}
 		public boolean isGetable() throws B3EngineException {
 			return true;
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case B3backendPackage.BEXECUTION_CONTEXT__PARENT_CONTEXT:
 				if (resolve) return getParentContext();
 				return basicGetParentContext();
 			case B3backendPackage.BEXECUTION_CONTEXT__VALUE_MAP:
 				return getValueMap();
 			case B3backendPackage.BEXECUTION_CONTEXT__FUNC_STORE:
 				return getFuncStore();
 			case B3backendPackage.BEXECUTION_CONTEXT__EFFECTIVE_CONCERNS:
 				return getEffectiveConcerns();
 			case B3backendPackage.BEXECUTION_CONTEXT__PROGRESS_MONITOR:
 				return getProgressMonitor();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 			case B3backendPackage.BEXECUTION_CONTEXT__PARENT_CONTEXT:
 				setParentContext((BExecutionContext)newValue);
 				return;
 			case B3backendPackage.BEXECUTION_CONTEXT__VALUE_MAP:
 				setValueMap((ValueMap)newValue);
 				return;
 			case B3backendPackage.BEXECUTION_CONTEXT__FUNC_STORE:
 				setFuncStore((B3FuncStore)newValue);
 				return;
 			case B3backendPackage.BEXECUTION_CONTEXT__EFFECTIVE_CONCERNS:
 				getEffectiveConcerns().clear();
 				getEffectiveConcerns().addAll((Collection<? extends BConcern>)newValue);
 				return;
 			case B3backendPackage.BEXECUTION_CONTEXT__PROGRESS_MONITOR:
 				setProgressMonitor((IProgressMonitor)newValue);
 				return;
 		}
 		super.eSet(featureID, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eUnset(int featureID) {
 		switch (featureID) {
 			case B3backendPackage.BEXECUTION_CONTEXT__PARENT_CONTEXT:
 				setParentContext((BExecutionContext)null);
 				return;
 			case B3backendPackage.BEXECUTION_CONTEXT__VALUE_MAP:
 				setValueMap(VALUE_MAP_EDEFAULT);
 				return;
 			case B3backendPackage.BEXECUTION_CONTEXT__FUNC_STORE:
 				setFuncStore(FUNC_STORE_EDEFAULT);
 				return;
 			case B3backendPackage.BEXECUTION_CONTEXT__EFFECTIVE_CONCERNS:
 				getEffectiveConcerns().clear();
 				return;
 			case B3backendPackage.BEXECUTION_CONTEXT__PROGRESS_MONITOR:
 				setProgressMonitor(PROGRESS_MONITOR_EDEFAULT);
 				return;
 		}
 		super.eUnset(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public boolean eIsSet(int featureID) {
 		switch (featureID) {
 			case B3backendPackage.BEXECUTION_CONTEXT__PARENT_CONTEXT:
 				return parentContext != null;
 			case B3backendPackage.BEXECUTION_CONTEXT__VALUE_MAP:
 				return VALUE_MAP_EDEFAULT == null ? valueMap != null : !VALUE_MAP_EDEFAULT.equals(valueMap);
 			case B3backendPackage.BEXECUTION_CONTEXT__FUNC_STORE:
 				return FUNC_STORE_EDEFAULT == null ? funcStore != null : !FUNC_STORE_EDEFAULT.equals(funcStore);
 			case B3backendPackage.BEXECUTION_CONTEXT__EFFECTIVE_CONCERNS:
 				return effectiveConcerns != null && !effectiveConcerns.isEmpty();
 			case B3backendPackage.BEXECUTION_CONTEXT__PROGRESS_MONITOR:
 				return PROGRESS_MONITOR_EDEFAULT == null ? progressMonitor != null : !PROGRESS_MONITOR_EDEFAULT.equals(progressMonitor);
 		}
 		return super.eIsSet(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public String toString() {
 		if (eIsProxy()) return super.toString();
 
 		StringBuffer result = new StringBuffer(super.toString());
 		result.append(" (valueMap: ");
 		result.append(valueMap);
 		result.append(", funcStore: ");
 		result.append(funcStore);
 		result.append(", progressMonitor: ");
 		result.append(progressMonitor);
 		result.append(')');
 		return result.toString();
 	}
 } //BExecutionContextImpl
