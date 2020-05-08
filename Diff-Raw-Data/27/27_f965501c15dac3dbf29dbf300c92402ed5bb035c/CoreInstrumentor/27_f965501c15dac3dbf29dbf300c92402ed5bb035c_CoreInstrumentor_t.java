 /*
  * Copyright (c) 2008-2010, Intel Corporation.
  * Copyright (c) 2006-2007, The Trustees of Stanford University.
  * All rights reserved.
  * Licensed under the terms of the New BSD License.
  */
 package chord.instr;
 
 import java.util.Map;
 import java.util.HashMap;
 
 import javassist.NotFoundException;
 import javassist.CannotCompileException;
 import javassist.CtBehavior;
 import javassist.expr.ExprEditor;
 import javassist.expr.NewExpr;
 import javassist.expr.NewArray;
 import javassist.expr.ArrayAccess;
 import javassist.expr.FieldAccess;
 import javassist.expr.MethodCall;
 import javassist.expr.MonitorEnter;
 import javassist.expr.MonitorExit;
 import javassist.CtClass;
 
 import chord.project.Messages;
 import chord.project.Config;
 
 /**
  * Bytecode instrumentor providing hooks for transforming classes,
  * methods, and instructions.
  * 
  * @author Mayur Naik (mhn@cs.stanford.edu)
  */
 public class CoreInstrumentor extends ExprEditor {
     public final static String INSTRUMENTOR_CLASS_KEY = "instrumentor_class_name";
 
 	private final static String EXPLICITLY_EXCLUDING_CLASS =
 		"WARN: Not instrumenting class %s as it is excluded by chord.scope.exclude.";
 	private final static String IMPLICITLY_EXCLUDING_CLASS =
 		"WARN: Not instrumenting class %s.";
 
 	protected boolean silent;
 	protected final JavassistPool pool;
 	protected String[] scopeExcludeAry;
 	protected Map<String, String> argsMap;
 
 	/**
 	 * Constructor.
 	 *
 	 * @param	argsMap	Arguments to the instrumentor in the form of a
 	 *			map of (key, value) pairs.
 	 */
 	public CoreInstrumentor(Map<String, String> argsMap) {
 		assert (argsMap != null);
 		this.argsMap = argsMap;
 		scopeExcludeAry = Config.scopeExcludeAry;
 		silent = Config.dynamicSilent;
 		String mainClassPathName = Config.mainClassPathName;
 		String userClassPathName = Config.classPathName;
 		pool = new JavassistPool(mainClassPathName, userClassPathName);
 	}
 
     public JavassistPool getPool() {
         return pool;
     }
 
 	public boolean isExplicitlyExcluded(String cName) {
 		for (String s : scopeExcludeAry) {
 			if (cName.startsWith(s))
 				return true;
 		}
 		return false;
 	}
 	
 	public boolean isImplicitlyExcluded(String cName) {
 		return cName.equals("java.lang.J9VMInternals") ||
 			cName.startsWith("sun.reflect.Generated") ||
 			cName.startsWith("java.lang.ref.");
 	}
 
 	public boolean isExcluded(String cName) {
 		if (isImplicitlyExcluded(cName)) {
 			if (!silent) Messages.log(IMPLICITLY_EXCLUDING_CLASS, cName);
 			return true;
 		}
 		if (isExplicitlyExcluded(cName)) {
 			if (!silent) Messages.log(EXPLICITLY_EXCLUDING_CLASS, cName);
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Provides a hook to instrument a class specified by name.
 	 *
 	 * The default implementation excludes instrumenting classes that
 	 * are excluded implicitly or explicitly; for each class that is
 	 * not excluded, it calls the {@link #edit(CtClass)} method.
 	 * 
 	 * @param	cName	Name of the class to be instrumented
 	 *			(e.g., java.lang.Object).
 	 * @return	The instrumented class in Javassist's representation.
 	 *			It must be null if the class is not instrumented.
 	 * @throws	NotFoundException	If Javassist fails to find the class.
 	 * @throws	CannotCompileException	If Javassist fails to correctly
 	 *			instrument the class.
 	 */
 	public CtClass edit(String cName)
 			throws NotFoundException, CannotCompileException {
 		if (isExcluded(cName))
 			return null;
 		CtClass clazz = pool.get(cName);
 		return edit(clazz);
 	}
 
 	/**
 	 * Provides a hook to instrument a class specified in Javassist's
 	 * representation.
 	 *
 	 * The default implementation calls the hooks to instrument all
 	 * its methods, including its class initializer method (if any),
 	 * all its declared constructors, and all its declared methods.
 	 *
 	 * @param	clazz	Javassist's representation of the class to be
 	 *			instrumented.
 	 * @return	The instrumented class in Javassist's representation.	
 	 *			It must be null if the class is not instrumented.
 	 * @throws	CannotCompileException	If Javassist fails to correctly
 	 *			instrument the class. 
 	 */
 	public CtClass edit(CtClass clazz) throws CannotCompileException {
 		CtBehavior clinit = clazz.getClassInitializer();
 		if (clinit != null)
 			edit(clinit);
         CtBehavior[] inits = clazz.getDeclaredConstructors();
         for (CtBehavior m : inits)
 			edit(m);
         CtBehavior[] meths = clazz.getDeclaredMethods();
 		for (CtBehavior m : meths)
 			edit(m);
 		return clazz;
 	}
 
 	/**
 	 * Provides a hook to instrument a method specified in Javassist's
 	 * representation.
 	 *
 	 * The default implementation visits each bytecode instruction in
	 * the method's code, calling the {@link #insertBefore(int)}
 	 * method for each instruction, ws well as the relevant edit
 	 * method for certain kinds of instructions (namely, object
 	 * allocation, field access, array access, monitor enter/exit,
 	 * and method invocation).
 	 *
 	 * @param	method	Javassist's representation of the method to be
 	 *			instrumented in the currently instrumented class.
 	 * @throws	CannotCompileException	If Javassist fails to correctly
 	 *			instrument the class.
 	 */
 	public void edit(CtBehavior method) throws CannotCompileException {
 		method.instrument(this);
 	}
 
 	/**
 	 * Provides a hook to insert instrumentation just before the
 	 * specified bytecode instruction in its containing method.
 	 *
 	 * @param	pos	Index of a bytecode instruction in the currently
 	 *			instrumented method.
 	 * @return	Code string to be inserted just before the index.
 	 */
     public String insertBefore(int pos) {
 		return null;
 	}
 
 	public void edit(NewExpr e) throws CannotCompileException { }
 
 	public void edit(NewArray e) throws CannotCompileException { }
 
 	public void edit(FieldAccess e) throws CannotCompileException { }
 
 	public void edit(ArrayAccess e) throws CannotCompileException { }
 
 	public void edit(MonitorEnter e) throws CannotCompileException { }
 
 	public void edit(MonitorExit e) throws CannotCompileException { }
 
 	public void edit(MethodCall e) throws CannotCompileException { }
 }
 
