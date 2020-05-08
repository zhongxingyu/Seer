 /*******************************************************************************
  * Copyright (c) 2010 xored software, Inc.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.internal.javascript.ti;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Stack;
 
 import org.eclipse.dltk.annotations.Nullable;
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.compiler.problem.IProblemIdentifier;
 import org.eclipse.dltk.javascript.ast.ASTVisitor;
 import org.eclipse.dltk.javascript.core.JavaScriptPlugin;
 import org.eclipse.dltk.javascript.parser.JSProblemReporter;
 import org.eclipse.dltk.javascript.parser.Reporter;
 import org.eclipse.dltk.javascript.typeinference.IFunctionValueCollection;
 import org.eclipse.dltk.javascript.typeinference.IValueCollection;
 import org.eclipse.dltk.javascript.typeinference.IValueReference;
 import org.eclipse.dltk.javascript.typeinfo.ITypeChecker;
 import org.eclipse.dltk.javascript.typeinfo.ITypeInferenceHandler;
 import org.eclipse.dltk.javascript.typeinfo.ITypeInferenceHandlerFactory;
 import org.eclipse.dltk.javascript.typeinfo.ITypeInferencerVisitor;
 import org.eclipse.dltk.javascript.typeinfo.TypeInfoManager;
 
 public abstract class TypeInferencerVisitorBase extends
 		ASTVisitor<IValueReference> implements ITypeInferencerVisitor {
 
 	protected final ITypeInferenceContext context;
 
 	private Stack<IValueCollection> contexts = new Stack<IValueCollection>();
 
 	public ITypeInferenceContext getContext() {
 		return context;
 	}
 
 	public IValueCollection peekContext() {
 		return !contexts.isEmpty() ? contexts.peek() : null;
 	}
 
 	public void enterContext(IValueCollection collection) {
 		contexts.push(collection);
 	}
 
 	public IValueCollection leaveContext() {
 		return contexts.pop();
 	}
 
 	protected boolean inFunction() {
 		return inFunction(false);
 	}
 
 	protected boolean inFunction(boolean ignoreBlocks) {
 		for (int i = contexts.size(); --i >= 0;) {
 			final IValueCollection collection = contexts.get(i);
 			if (collection instanceof IFunctionValueCollection) {
 				if (ignoreBlocks) {
 					if (((IFunctionValueCollection) collection).isInlineBlock()) {
 						continue;
 					}
 				}
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public TypeInferencerVisitorBase(ITypeInferenceContext context) {
 		this.context = context;
 	}
 
 	private ITypeInferenceHandler[] handlers = null;
 
 	public void initialize() {
 		contexts.clear();
 		contexts.push(new TopValueCollection(context));
 		final List<ITypeInferenceHandler> handlers = createHandlers();
 		if (handlers != null && !handlers.isEmpty()) {
 			this.handlers = handlers.toArray(new ITypeInferenceHandler[handlers
 					.size()]);
 		} else {
 			this.handlers = null;
 		}
 	}
 
 	protected List<ITypeInferenceHandler> createHandlers() {
 		final List<ITypeInferenceHandler> handlers = new ArrayList<ITypeInferenceHandler>();
 		for (ITypeInferenceHandlerFactory factory : TypeInfoManager
 				.getNodeHandlerFactories()) {
 			final ITypeInferenceHandler handler = factory.create(context, this);
 			if (handler != null) {
 				handlers.add(handler);
 			}
 		}
 		return handlers;
 	}
 
 	@Override
 	public IValueReference visit(ASTNode node) {
 		if (handlers != null) {
 			for (ITypeInferenceHandler handler : handlers) {
 				final IValueReference result = handler.handle(node);
 				if (result != ITypeInferenceHandler.CONTINUE) {
 					return result;
 				}
 			}
 		}
 		try {
 			return super.visit(node);
 		} catch (PositionReachedException e) {
 			throw e;
 		} catch (RuntimeException e) {
			JavaScriptPlugin.error(buildErrorMessage(node), e);
 			throw e;
 		} catch (AssertionError e) {
			JavaScriptPlugin.error(buildErrorMessage(node), e);
 			throw e;
 		}
 	}
 
	private String buildErrorMessage(ASTNode node) {
 		final StringBuilder sb = new StringBuilder();
 		sb.append("Error processing ");
 		sb.append(node.getClass().getName());
 		try {
 			final String message = node.toString();
 			sb.append(" \"").append(message).append("\"");
 		} catch (Throwable t) {
 			// ignore potential errors in .toString()
 		}
 		sb.append(" in ").append(context.getSource());
 		return sb.toString();
 	}
 
 	public void done() {
 		IValueCollection collection = getCollection();
 		if (collection instanceof ValueCollection) {
 			IValue value = ((ValueCollection) collection).getValue();
 			if (value instanceof Value) {
 				((Value) value).resolveLazyValues(new HashSet<Value>());
 			}
 		}
 	}
 
 	public IValueCollection getCollection() {
 		return contexts.get(0);
 	}
 
 	/**
 	 * @param value1
 	 * @param value2
 	 * @return
 	 */
 	protected IValueReference merge(IValueReference value1,
 			IValueReference value2) {
 		final AnonymousValue reference = new AnonymousValue();
 		reference.setValue(value1);
 		reference.addValue(value2, false);
 		return reference;
 	}
 
 	protected JSProblemReporter reporter;
 
 	public JSProblemReporter getProblemReporter() {
 		return reporter;
 	}
 
 	public void suppressProblems(IProblemIdentifier... identifiers) {
 		if (reporter != null) {
 			((Reporter) reporter).suppressProblems(identifiers);
 		}
 	}
 
 	protected ITypeChecker typeChecker;
 
 	/**
 	 * Returns the type checker which can be used for type validation. Non-null
 	 * during validation.
 	 */
 	@Nullable
 	public ITypeChecker getTypeChecker() {
 		return typeChecker;
 	}
 
 }
