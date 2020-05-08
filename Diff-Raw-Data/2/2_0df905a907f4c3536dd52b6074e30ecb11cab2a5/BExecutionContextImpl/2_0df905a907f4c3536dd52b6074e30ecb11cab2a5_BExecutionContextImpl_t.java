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
 import org.eclipse.b3.backend.evaluator.b3backend.BContext;
 import org.eclipse.b3.backend.evaluator.b3backend.BExecutionContext;
 import org.eclipse.b3.backend.evaluator.b3backend.BFileReference;
 import org.eclipse.b3.backend.evaluator.b3backend.BGuardFunction;
 import org.eclipse.b3.backend.evaluator.b3backend.BInnerContext;
 import org.eclipse.b3.backend.evaluator.b3backend.BJavaCallType;
 import org.eclipse.b3.backend.evaluator.b3backend.BJavaFunction;
 import org.eclipse.b3.backend.evaluator.b3backend.BParameterDeclaration;
 import org.eclipse.b3.backend.evaluator.b3backend.BSystemContext;
 import org.eclipse.b3.backend.evaluator.b3backend.BTypeCalculatorFunction;
 
 import org.eclipse.b3.backend.evaluator.b3backend.BInvocationContext;
 import org.eclipse.b3.backend.evaluator.b3backend.IFunction;
 import org.eclipse.b3.backend.evaluator.typesystem.TypeUtils;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.impl.EObjectImpl;
 import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
 import org.eclipse.emf.ecore.util.EObjectResolvingEList;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.util.InternalEList;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>BExecution Context</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.b3.backend.evaluator.b3backend.impl.BExecutionContextImpl#getParentContext <em>Parent Context</em>}</li>
  *   <li>{@link org.eclipse.b3.backend.evaluator.b3backend.impl.BExecutionContextImpl#getChildContexts <em>Child Contexts</em>}</li>
  *   <li>{@link org.eclipse.b3.backend.evaluator.b3backend.impl.BExecutionContextImpl#getValueMap <em>Value Map</em>}</li>
  *   <li>{@link org.eclipse.b3.backend.evaluator.b3backend.impl.BExecutionContextImpl#getFuncStore <em>Func Store</em>}</li>
  *   <li>{@link org.eclipse.b3.backend.evaluator.b3backend.impl.BExecutionContextImpl#getEffectiveConcerns <em>Effective Concerns</em>}</li>
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
 	 * The cached value of the '{@link #getChildContexts() <em>Child Contexts</em>}' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getChildContexts()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<BExecutionContext> childContexts;
 
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
 		if (eContainerFeatureID() != B3backendPackage.BEXECUTION_CONTEXT__PARENT_CONTEXT) return null;
 		return (BExecutionContext)eContainer();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetParentContext(BExecutionContext newParentContext, NotificationChain msgs) {
 		msgs = eBasicSetContainer((InternalEObject)newParentContext, B3backendPackage.BEXECUTION_CONTEXT__PARENT_CONTEXT, msgs);
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setParentContext(BExecutionContext newParentContext) {
 		if (newParentContext != eInternalContainer() || (eContainerFeatureID() != B3backendPackage.BEXECUTION_CONTEXT__PARENT_CONTEXT && newParentContext != null)) {
 			if (EcoreUtil.isAncestor(this, newParentContext))
 				throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
 			NotificationChain msgs = null;
 			if (eInternalContainer() != null)
 				msgs = eBasicRemoveFromContainer(msgs);
 			if (newParentContext != null)
 				msgs = ((InternalEObject)newParentContext).eInverseAdd(this, B3backendPackage.BEXECUTION_CONTEXT__CHILD_CONTEXTS, BExecutionContext.class, msgs);
 			msgs = basicSetParentContext(newParentContext, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, B3backendPackage.BEXECUTION_CONTEXT__PARENT_CONTEXT, newParentContext, newParentContext));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<BExecutionContext> getChildContexts() {
 		if (childContexts == null) {
 			childContexts = new EObjectContainmentWithInverseEList<BExecutionContext>(BExecutionContext.class, this, B3backendPackage.BEXECUTION_CONTEXT__CHILD_CONTEXTS, B3backendPackage.BEXECUTION_CONTEXT__PARENT_CONTEXT);
 		}
 		return childContexts;
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
 		B3FuncStore fs = getFuncStore();
 		if(fs != null)
 			return fs;
 		BExecutionContext p = getParentContext();
 		while(p != null && p.getFuncStore() == null)
 			p = p.getParentContext();
 		return p == null ? null : p.getFuncStore();
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
 		// load all static methods as functions
 		// (under the guidance of B3Backend annotations)
 		//
 		Method[] methods = clazz.getDeclaredMethods();
 		Map<String, BJavaFunction> systemFunctions = new HashMap<String, BJavaFunction>();
 		Map<String, BJavaFunction> guards = new HashMap<String, BJavaFunction>();
 		Map<String, BJavaFunction> typeCalculators = new HashMap<String, BJavaFunction>();
 		
 		Map<String,List<BJavaFunction>> systemProxies = new HashMap<String, List<BJavaFunction>>();
 		Map<String,List<BJavaFunction>> guardedFunctions = new HashMap<String, List<BJavaFunction>>();
 		Map<String,List<BJavaFunction>> typeCalculatedFunctions = new HashMap<String, List<BJavaFunction>>();
 		int counter = 0;
 
 		// create and initialize a BJavaFunction to represent each public static function
 		for(Method m : methods) {
 			counter++; // start on 1
 			final int modifiers = m.getModifiers();
 			if(Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)) {
 				BJavaFunction f = B3backendFactory.eINSTANCE.createBJavaFunction();
 				// TODO: f's Resource ?!?
 //				String fileName = clazz.getCanonicalName();
 //				fileRef.setFileName(fileName == null ? "anonymous class" : fileName);
 
 				f.setName(m.getName());	// set original name (as default)
 				f.setFinal(Modifier.isFinal(modifiers)); // make it final in b3 as well
 				f.setCallType(BJavaCallType.FUNCTION); // may later change to system call
 				f.setMethod(m); // add the thing to call
 
 				// check for annotations
 				B3Backend annotation = m.getAnnotation(B3Backend.class);
 				if(annotation != null) {
 					f.setExecutionMode(BackendHelper.getExecutionMode(annotation));
 					f.setVisibility(BackendHelper.getVisibility(annotation));
 					if(annotation.hideOriginal()) {
 						String[] names = annotation.funcNames();
 						if(names == null || names.length < 1)
 							throw new B3FunctionLoadException("hidesOriginal annotation specified but not funcNames annotation", m);
 						f.setName(names[0]);
 					}
 				}
 				f.setReturnType(m.getGenericReturnType());
 				f.setExceptionTypes(m.getGenericExceptionTypes());
 				f.setParameterTypes(m.getGenericParameterTypes());
 				f.setVarArgs(m.isVarArgs());
 				Annotation[][] pa = m.getParameterAnnotations();
 				String pNames[] = new String[pa.length];
 				f.setParameterNames(pNames);
 				for(int i = 0; i < pa.length; i++) {
 					Annotation[] pan = pa[i];
 					if(pan == null)
 						pNames[i] = String.valueOf('a'+i);
 					else {
 						for(int j = 0; j < pan.length; j++) {
 							if(pan[j] instanceof B3Backend) {
 								pNames[i]=((B3Backend)pan[j]).name();
 								break; // only use first named declared for the parameter
 							}
 						}
 					}
 				}
 				f.setTypeParameters(m.getTypeParameters());
 
 				// If the function is a system function - set it aside.
 				// if it is a function that is a specification of a system function,
 				// add it to a list of functions to patch after the fact.
 				// if it is a guard, set it aside and patch the functions using it after the fact
 				//
 				if(annotation != null && annotation.system()) {
 					systemFunctions.put(f.getName(), f);
 				}
 				else if (annotation != null && annotation.guard()) {
 					guards.put(f.getName(), f);
 					// guards are called using system calling convention
 					f.setCallType(BJavaCallType.SYSTEM);
 				}
 				else if (annotation != null && annotation.typeCalculator()) {
 					typeCalculators.put(f.getName(), f);
 				}
 				else {
 					// add defined function to the func store
 					if(annotation == null || !annotation.hideOriginal())
 						funcStore.defineFunction(m.getName(), f);
 					if(annotation != null)
 						for(String fname : annotation.funcNames())
 							funcStore.defineFunction(fname, f);
 					// if a function is a proxy for a system function
 					if(annotation != null && annotation.systemFunction() != null
 							&& annotation.systemFunction().length() > 0) {
 						String systemFunctionName = annotation.systemFunction();
 						List<BJavaFunction> fs = null;
 						if((fs = systemProxies.get(systemFunctionName)) == null)
 							systemProxies.put(systemFunctionName, fs = new ArrayList<BJavaFunction>());
 						fs.add(f);
 					}
 					// if a function is guarded remember it
 					if(annotation != null && annotation.guardFunction() != null && annotation.guardFunction().length() > 0) {
 						String guardFunctionName = annotation.guardFunction();
 						List<BJavaFunction> gf = null;
 						if((gf = guardedFunctions.get(guardFunctionName)) == null )
 							guardedFunctions.put(guardFunctionName, gf = new ArrayList<BJavaFunction>());
 						gf.add(f);
 					}
 					// if a function has a type calculator remember it
 					if(annotation != null && annotation.typeFunction() != null && annotation.typeFunction().length() > 0) {
 						String typeFunctionName = annotation.typeFunction();
 						List<BJavaFunction> tf = null;
 						if((tf = typeCalculatedFunctions.get(typeFunctionName)) == null )
 							typeCalculatedFunctions.put(typeFunctionName, tf = new ArrayList<BJavaFunction>());
 						tf.add(f);
 					}
 				}
 			}
 
 		}
 		// patch system functions
 		// ---
 		// revisit all functions that reference a system function and replace their method
 		// with the system method, and also indicate that the call should be made as a system
 		// call.
 		for( Entry<String, List<BJavaFunction>> e : systemProxies.entrySet()) {
 			for(BJavaFunction s : e.getValue()) {
 				BJavaFunction sys = systemFunctions.get(e.getKey());
 				if(sys == null)
 					throw new B3FunctionLoadException("reference to system function: "
 							+e.getKey()+" can not be satisfied - no such method found.", s.getMethod());
 				// patch the method in the func store with values from the system function
 				s.setCallType(BJavaCallType.SYSTEM);
 				s.setMethod(sys.getMethod());
 			}
 		}
 		// link guards
 		// ---
 		// revisit all functions that reference a guardFunction and set that function as a guard
 		// 
 		for( Entry<String, List<BJavaFunction>> e : guardedFunctions.entrySet()) {
 			for(BJavaFunction guarded : e.getValue()) {
 				BJavaFunction g = guards.get(e.getKey());
 				if(g == null)
 					throw new B3FunctionLoadException("reference to guard function: "
 							+e.getKey()+" can not be satisfied - no such guard found.", guarded.getMethod());
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
 		for( Entry<String, List<BJavaFunction>> e : typeCalculatedFunctions.entrySet()) {
 			for(BJavaFunction typed : e.getValue()) {
 				BJavaFunction tc = typeCalculators.get(e.getKey());
 				if(tc == null)
 					throw new B3FunctionLoadException("reference to type calculator function: "
 							+e.getKey()+" can not be satisfied - no such function found.", typed.getMethod());
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
 		final int modifiers = m.getModifiers();
 		boolean isStatic = Modifier.isStatic(modifiers);
 		// create and initialize a BJavaFunction to represent the method m
 		
 		if(Modifier.isPublic(modifiers)) {
 			BJavaFunction f = B3backendFactory.eINSTANCE.createBJavaFunction();
 			// TODO: f's resource ?
 			f.setName(m.getName());	
 			f.setFinal(Modifier.isFinal(modifiers)); // make it final in b3 as well
 			f.setClassFunction(isStatic); // invokable via class - i.e. "static" in java
 			f.setCallType(BJavaCallType.METHOD); // i.e. first parameter is not passed as parameter
 			f.setMethod(m); // add the thing to call
 
 			f.setReturnType(TypeUtils.objectify(m.getGenericReturnType()));
 			f.setExceptionTypes(m.getGenericExceptionTypes());
 			// set parameter types, but add the declaring class as the first parameter
 //			f.setParameterTypes(getGenericParameterTypes(m, isStatic));
 			f.setVarArgs(m.isVarArgs());
 			// there may be parameter annotations, but very unlikely - at least there is
 			// an empty array of parameter annotations, so can just as well use this
 			Annotation[][] pa = m.getParameterAnnotations();
 			String pNames[] = new String[pa.length + 1];
 //			f.setParameterNames(pNames);
 			pNames[0] = isStatic ? "aClass" : "this";
 			PARAMETER: for(int i = 0; i < pa.length; i++) {
 				Annotation[] pan = pa[i];
 				if(pan != null) {
 					for(int j = 0; j < pan.length; j++) {
 						if(pan[j] instanceof B3Backend) {
 							pNames[i+1]=((B3Backend)pan[j]).name();
							continue PARAMETER; // only use first name declared for the parameter
 						}
 					}
 				}
 				pNames[i+1] = toAlphabetString(i);
 			}
 			setParameterDeclarations(f, getGenericParameterTypes(m, isStatic), pNames);
 			f.setTypeParameters(m.getTypeParameters());
 
 			funcStore.defineFunction(m.getName(), f);
 			return f;
 		}
 		return null;
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
 	private void setParameterDeclarations(IFunction f, Type[] types, String[] names) {
 		if(types.length != names.length) {
 			throw new IllegalArgumentException("All types must have a name");
 		}
 		EList<BParameterDeclaration> plist = f.getParameters();
 		for(int i = 0; i < types.length; i++) {
 			BParameterDeclaration decl = B3backendFactory.eINSTANCE.createBParameterDeclaration();
 			decl.setName(names[i]);
 			Type t = types[i];
 			if(!(t instanceof EObject)) {
 				B3ParameterizedType b3t = B3backendFactory.eINSTANCE.createB3ParameterizedType();
 				b3t.setRawType(t);
 				t = b3t;
 			}
 			decl.setType(t);
 			plist.add(decl);
 		}
 	}
 	private Type[] getGenericParameterTypes(Method m, boolean isStatic) {
 		Type[] original = m.getGenericParameterTypes();
 		Type[] rewritten = new Type[original.length + 1];
 		if (isStatic) {
 			B3MetaClass metaClass = B3backendFactory.eINSTANCE
 					.createB3MetaClass();
 			metaClass.setInstanceClass(m.getDeclaringClass());
 			rewritten[0] = metaClass;
 		} else {
 			rewritten[0] = m.getDeclaringClass();
 		}
 		for (int i = 0; i < original.length; i++)
 			rewritten[i + 1] = TypeUtils.objectify(original[i]);
 		return rewritten;
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
 					lastError = e3;
 					continue;
 				} catch (B3NoSuchFunctionSignatureException e4) {
 					lastError = e4;
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
 		if(lastError == null)
 			throw new B3InternalError("BExecutionContextImpl#callFunction() did not return ok, and had no last error");
 		
 		throw lastError;
 
 	}
 	private void weaveLoaded(IFunction f, BExecutionContext ctx) throws B3WeavingFailedException{
 		if(ctx == null)
 			return;
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
 					lastError = e3;
 					continue;
 				} catch (B3NoSuchFunctionSignatureException e4) {
 					lastError = e4;
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
 		if(lastError == null)
 			throw new B3InternalError("BExecutionContextImpl#getDeclaredFunctionType() did not return ok, and had no last error");
 		
 		throw lastError;
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
 	public LValue getLValue(String name) throws B3EngineException {
 		if(!getValueMap().containsKey(name)) {
 			BExecutionContext parentContext = null;
 			if((parentContext = getParentContext()) == null)
 				throw new B3NoSuchVariableException(name);
 			return parentContext.getLValue(name);
 		}
 		return new ValueMapLVal(name);
 	}
 	private boolean isWeaving = false;
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public IFunction defineFunction(IFunction function) throws B3EngineException {
 //		if("addedLater".equals(function.getName())) {
 //			function = function;
 //		}
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
 	@SuppressWarnings("unchecked")
 	@Override
 	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case B3backendPackage.BEXECUTION_CONTEXT__PARENT_CONTEXT:
 				if (eInternalContainer() != null)
 					msgs = eBasicRemoveFromContainer(msgs);
 				return basicSetParentContext((BExecutionContext)otherEnd, msgs);
 			case B3backendPackage.BEXECUTION_CONTEXT__CHILD_CONTEXTS:
 				return ((InternalEList<InternalEObject>)(InternalEList<?>)getChildContexts()).basicAdd(otherEnd, msgs);
 		}
 		return super.eInverseAdd(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case B3backendPackage.BEXECUTION_CONTEXT__PARENT_CONTEXT:
 				return basicSetParentContext(null, msgs);
 			case B3backendPackage.BEXECUTION_CONTEXT__CHILD_CONTEXTS:
 				return ((InternalEList<?>)getChildContexts()).basicRemove(otherEnd, msgs);
 		}
 		return super.eInverseRemove(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eBasicRemoveFromContainerFeature(NotificationChain msgs) {
 		switch (eContainerFeatureID()) {
 			case B3backendPackage.BEXECUTION_CONTEXT__PARENT_CONTEXT:
 				return eInternalContainer().eInverseRemove(this, B3backendPackage.BEXECUTION_CONTEXT__CHILD_CONTEXTS, BExecutionContext.class, msgs);
 		}
 		return super.eBasicRemoveFromContainerFeature(msgs);
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
 				return getParentContext();
 			case B3backendPackage.BEXECUTION_CONTEXT__CHILD_CONTEXTS:
 				return getChildContexts();
 			case B3backendPackage.BEXECUTION_CONTEXT__VALUE_MAP:
 				return getValueMap();
 			case B3backendPackage.BEXECUTION_CONTEXT__FUNC_STORE:
 				return getFuncStore();
 			case B3backendPackage.BEXECUTION_CONTEXT__EFFECTIVE_CONCERNS:
 				return getEffectiveConcerns();
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
 			case B3backendPackage.BEXECUTION_CONTEXT__CHILD_CONTEXTS:
 				getChildContexts().clear();
 				getChildContexts().addAll((Collection<? extends BExecutionContext>)newValue);
 				return;
 			case B3backendPackage.BEXECUTION_CONTEXT__FUNC_STORE:
 				setFuncStore((B3FuncStore)newValue);
 				return;
 			case B3backendPackage.BEXECUTION_CONTEXT__EFFECTIVE_CONCERNS:
 				getEffectiveConcerns().clear();
 				getEffectiveConcerns().addAll((Collection<? extends BConcern>)newValue);
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
 			case B3backendPackage.BEXECUTION_CONTEXT__CHILD_CONTEXTS:
 				getChildContexts().clear();
 				return;
 			case B3backendPackage.BEXECUTION_CONTEXT__FUNC_STORE:
 				setFuncStore(FUNC_STORE_EDEFAULT);
 				return;
 			case B3backendPackage.BEXECUTION_CONTEXT__EFFECTIVE_CONCERNS:
 				getEffectiveConcerns().clear();
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
 				return getParentContext() != null;
 			case B3backendPackage.BEXECUTION_CONTEXT__CHILD_CONTEXTS:
 				return childContexts != null && !childContexts.isEmpty();
 			case B3backendPackage.BEXECUTION_CONTEXT__VALUE_MAP:
 				return VALUE_MAP_EDEFAULT == null ? valueMap != null : !VALUE_MAP_EDEFAULT.equals(valueMap);
 			case B3backendPackage.BEXECUTION_CONTEXT__FUNC_STORE:
 				return FUNC_STORE_EDEFAULT == null ? funcStore != null : !FUNC_STORE_EDEFAULT.equals(funcStore);
 			case B3backendPackage.BEXECUTION_CONTEXT__EFFECTIVE_CONCERNS:
 				return effectiveConcerns != null && !effectiveConcerns.isEmpty();
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
 		result.append(')');
 		return result.toString();
 	}
 } //BExecutionContextImpl
