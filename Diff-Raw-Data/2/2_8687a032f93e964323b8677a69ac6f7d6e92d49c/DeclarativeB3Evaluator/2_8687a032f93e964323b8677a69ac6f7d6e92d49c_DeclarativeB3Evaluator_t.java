 /**
  * Copyright (c) 2010, Cloudsmith Inc.
  * The code, documentation and other materials contained herein have been
  * licensed under the Eclipse Public License - v 1.0 by the copyright holder
  * listed above, as the Initial Contributor under such license. The text of
  * such license is available at www.eclipse.org.
  */
 
 package org.eclipse.b3.backend.evaluator;
 
 import java.lang.reflect.Type;
 import java.util.Collections;
 
 import org.eclipse.b3.backend.core.IB3Evaluator;
 import org.eclipse.b3.backend.core.datatypes.LValue;
 import org.eclipse.b3.backend.evaluator.b3backend.BExecutionContext;
 import org.eclipse.emf.common.util.WrappedException;
 import org.eclipse.xtext.util.Exceptions;
 import org.eclipse.xtext.util.PolymorphicDispatcher;
 import org.eclipse.xtext.util.PolymorphicDispatcher.ErrorHandler;
 
 /**
 * Basic scaffolding for a B3 evaluator.
  * 
  */
 public abstract class DeclarativeB3Evaluator implements IB3Evaluator {
 	private final PolymorphicDispatcher<String[]> parameterNamesDispatcher = new PolymorphicDispatcher<String[]>(
 		"parameterNames", 1, 1, Collections.singletonList(this), new ErrorHandler<String[]>() {
 			public String[] handle(Object[] params, Throwable e) {
 				return handleStringArrayError(params, e);
 			}
 		});
 
 	private final PolymorphicDispatcher<Object> callDispatcher = new PolymorphicDispatcher<Object>(
 		"call", 4, 5, Collections.singletonList(this), new ErrorHandler<Object>() {
 			public Object handle(Object[] params, Throwable e) {
 				return handleError(params, e);
 			}
 		});
 
 	private final PolymorphicDispatcher<BExecutionContext> callPrepareDispatcher = new PolymorphicDispatcher<BExecutionContext>(
 		"callPrepare", 4, 5, Collections.singletonList(this), new ErrorHandler<BExecutionContext>() {
 			public BExecutionContext handle(Object[] params, Throwable e) {
 				return handleContextError(params, e);
 			}
 		});
 
 	private final PolymorphicDispatcher<Object> evalDispatcher = new PolymorphicDispatcher<Object>(
 		"evaluate", 2, 2, Collections.singletonList(this), new ErrorHandler<Object>() {
 			public Object handle(Object[] params, Throwable e) {
 				return handleError(params, e);
 			}
 		});
 
 	private final PolymorphicDispatcher<BExecutionContext> getInnerDispatcher = new PolymorphicDispatcher<BExecutionContext>(
 		"getInnerContext", 2, 2, Collections.singletonList(this), new ErrorHandler<BExecutionContext>() {
 			public BExecutionContext handle(Object[] params, Throwable e) {
 				return handleContextError(params, e);
 			}
 		});
 
 	private final PolymorphicDispatcher<Object> evalDefaultsDispatcher = new PolymorphicDispatcher<Object>(
 		"evaluateDefaults", 3, 3, Collections.singletonList(this), new ErrorHandler<Object>() {
 			public Object handle(Object[] params, Throwable e) {
 				return handleError(params, e);
 			}
 		});
 
 	private final PolymorphicDispatcher<LValue> lvalueDispatcher = new PolymorphicDispatcher<LValue>(
 		"lValue", 2, 2, Collections.singletonList(this), new ErrorHandler<LValue>() {
 			public LValue handle(Object[] params, Throwable e) {
 				return handleLValueError(params, e);
 			}
 		});
 
 	private final PolymorphicDispatcher<Object> definitionDispatcher = new PolymorphicDispatcher<Object>(
 		"define", 2, 3, Collections.singletonList(this), new ErrorHandler<Object>() {
 			public Object handle(Object[] params, Throwable e) {
 				return handleError(params, e);
 			}
 		});
 
 	public Object call(Object o, Object[] parameters, Type[] parameterTypes, BExecutionContext ctx) {
 		throw new UnsupportedOperationException("No suitable 'call' method for object of class:" + o.getClass());
 	}
 
 	public Type define(Object o, BExecutionContext ctx) {
 		throw new UnsupportedOperationException("No suitable 'define' method for object of class:" + o.getClass());
 	}
 
 	public Type define(Object o, BExecutionContext ctx, boolean isWeaving) {
 		throw new UnsupportedOperationException("No suitable 'define weaving' method for object of class:" +
 				o.getClass());
 	}
 
 	public Object doCall(Object element, Object[] parameters, Type[] parameterTypes, BExecutionContext ctx)
 			throws Throwable {
 		try {
 			return callDispatcher.invoke(element, parameters, parameterTypes, ctx);
 		}
 		catch(WrappedException e) {
 			throw e.getCause();
 		}
 	}
 
 	public Object doCall(Object element, Object[] parameters, Type[] parameterTypes, BExecutionContext ctx,
 			boolean prepareContext) throws Throwable {
 		try {
 			return callDispatcher.invoke(element, parameters, parameterTypes, ctx, prepareContext);
 		}
 		catch(WrappedException e) {
 			throw e.getCause();
 		}
 	}
 
 	public BExecutionContext doCallPrepare(Object element, Object[] parameters, Type[] parameterTypes,
 			BExecutionContext ctx) throws Throwable {
 		try {
 			return callPrepareDispatcher.invoke(element, parameters, parameterTypes, ctx);
 		}
 		catch(WrappedException e) {
 			throw e.getCause();
 		}
 	}
 
 	public Object doDefine(Object element, BExecutionContext ctx) throws Throwable {
 		try {
 			return definitionDispatcher.invoke(element, ctx);
 		}
 		catch(WrappedException e) {
 			throw e.getCause();
 		}
 	}
 
 	public Object doDefine(Object element, BExecutionContext ctx, boolean isWeaving) throws Throwable {
 		try {
 			return definitionDispatcher.invoke(element, ctx, isWeaving);
 		}
 		catch(WrappedException e) {
 			throw e.getCause();
 		}
 	}
 
 	public Object doEvaluate(Object element, BExecutionContext ctx) throws Throwable {
 		try {
 			return evalDispatcher.invoke(element, ctx);
 		}
 		catch(WrappedException e) {
 			throw e.getCause();
 		}
 	}
 
 	public Object doEvaluateDefaults(Object element, BExecutionContext ctx, boolean allVisible) throws Throwable {
 		try {
 			return evalDefaultsDispatcher.invoke(element, ctx, allVisible);
 		}
 		catch(WrappedException e) {
 			throw e.getCause();
 		}
 	}
 
 	public BExecutionContext doGetInnerContext(Object element, BExecutionContext ctx) throws Throwable {
 		try {
 			return getInnerDispatcher.invoke(element, ctx);
 		}
 		catch(WrappedException e) {
 			throw e.getCause();
 		}
 	}
 
 	public LValue doLValue(Object element, BExecutionContext ctx) throws Throwable {
 		return lvalueDispatcher.invoke(element, ctx);
 	}
 
 	public String[] doParameterNames(Object element) {
 		return parameterNamesDispatcher.invoke(element);
 	}
 
 	public Type evaluate(Object o, BExecutionContext ctx) {
 		throw new UnsupportedOperationException("No suitable evaluate method for object of class:" + o.getClass());
 	}
 
 	public Type evaluateDefaults(Object o, BExecutionContext ctx, boolean allVisible) {
 		throw new UnsupportedOperationException("No suitable evaluateDefault method for object of class:" +
 				o.getClass());
 	}
 
 	public LValue lValue(Object o, BExecutionContext ctx) {
 		// throw new B3NotLValueException(); ??
 		throw new UnsupportedOperationException("No suitable lValue method for object of class:" + o.getClass());
 	}
 
 	public String[] parameterNames(Object o) {
 		throw new UnsupportedOperationException("No suitable parameterNames method for object of class:" + o.getClass());
 	}
 
 	protected BExecutionContext handleContextError(Object[] params, Throwable e) {
 		if(e instanceof NullPointerException) {
 			return null;
 		}
 		return Exceptions.throwUncheckedException(e);
 	}
 
 	protected Object handleError(Object[] params, Throwable e) {
 		// TODO: don't know how this is supposed to work - callers should expect a type at all times,
 		// and get a "type can not be inferred exception" with some info about why.
 		//
 		// if(e instanceof NullPointerException) {
 		// return Object.class;
 		// }
 		return Exceptions.throwUncheckedException(e);
 	}
 
 	protected LValue handleLValueError(Object[] params, Throwable e) {
 		if(e instanceof NullPointerException) {
 			return null;
 		}
 		return Exceptions.throwUncheckedException(e);
 	}
 
 	protected String[] handleStringArrayError(Object[] params, Throwable e) {
 		// TODO: don't know how this is supposed to work - callers should expect a type at all times,
 		// and get a "type can not be inferred exception" with some info about why.
 		//
 		// if(e instanceof NullPointerException) {
 		// return Object.class;
 		// }
 		return Exceptions.throwUncheckedException(e);
 	}
 
 }
