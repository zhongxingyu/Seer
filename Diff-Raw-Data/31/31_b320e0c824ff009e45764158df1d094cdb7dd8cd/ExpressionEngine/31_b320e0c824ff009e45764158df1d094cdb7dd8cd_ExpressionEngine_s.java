 /*******************************************************************************
  * Copyright (c) May 20, 2011 NetXForge.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *  You should have received a copy of the GNU Lesser General Public License
  *   along with this program.  If not, see <http://www.gnu.org/licenses/>
  * 
  * Contributors: Martin Taal - initial API and implementation and/or
  * initial documentation
  *******************************************************************************/
 package com.netxforge.engine;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.xtext.diagnostics.ExceptionDiagnostic;
 import org.eclipse.xtext.linking.lazy.LazyLinkingResource;
 import org.eclipse.xtext.resource.IResourceFactory;
 import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
 import org.eclipse.xtext.scoping.IGlobalScopeProvider;
 import org.eclipse.xtext.util.CancelIndicator;
 import org.eclipse.xtext.util.StringInputStream;
 
 import com.google.inject.Inject;
 import com.netxforge.internal.RuntimeActivator;
 import com.netxforge.interpreter.IInterpreter;
 import com.netxforge.interpreter.IInterpreterContextFactory;
 import com.netxforge.netxscript.Mod;
 import com.netxforge.netxstudio.common.context.IComputationContext;
 import com.netxforge.netxstudio.common.model.ModelUtils;
 import com.netxforge.netxstudio.generics.DateTimeRange;
 import com.netxforge.netxstudio.library.BaseExpressionResult;
 import com.netxforge.netxstudio.library.BaseResource;
 import com.netxforge.netxstudio.library.Component;
 import com.netxforge.netxstudio.library.Expression;
 import com.netxforge.netxstudio.library.ExpressionResult;
 import com.netxforge.netxstudio.operators.Node;
 import com.netxforge.netxstudio.services.Service;
 import com.netxforge.scoping.IExternalContextAware;
 
 /**
  * Runs an expression and collects the results in a list of
  * {@link ExpressionResult} objects.
  * 
  * This Engine, has the following potential errors:
  * <ul>
  * <li>The expression might not load, because of syntax issues.</li>
  * <li>Linking (resolve) could fail, CDO connection is required</li>
  * <li>Expression results not be written out</li>
  * </ul>
  * 
  * @author Martin Taal
  * @author Christophe Bouhier
  */
 public class ExpressionEngine implements IExpressionEngine {
 	private Expression expression;
 	private List<BaseExpressionResult> expressionResult = new ArrayList<BaseExpressionResult>();
 
	private XtextResource xResource;

	@Inject
	private XtextResourceSet xResourceSet;
 
 	@Inject
 	private IResourceFactory xResourceFactory;
 
 	@Inject
 	private IInterpreterContextFactory xInterpreterContextFactory;
 
 	@Inject
 	private IInterpreter xInterpreter;
 
 	@Inject
 	private ModelUtils modelUtils;
 
 	@Inject
 	private IGlobalScopeProvider scopeProvider;
 
 	private List<Object> context = new ArrayList<Object>();
 
 	private Throwable throwable;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.netxforge.netxstudio.server.logic.expression.IExpressionEngine#run()
 	 */
 	public void run() {
 
 		throwable = null;
 		getExpressionResult().clear();
 
 		final String asString = this.modelUtils.expressionAsString(expression);
 		if (asString == null || asString.trim().length() == 0) {
 			return;
 		}
 
 		try {
 
 			// Prepare the external context.
 
 			final List<IComputationContext> contextList = new ArrayList<IComputationContext>();
 			for (final Object o : context) {
 				contextList.add(xInterpreterContextFactory.createContext(o));
 			}
 			final IComputationContext[] contextArray = new IComputationContext[contextList
 					.size()];
 			contextList.toArray(contextArray);
 
 			if (scopeProvider != null
 					&& scopeProvider instanceof IExternalContextAware) {
 				((IExternalContextAware) scopeProvider).clearExternalContext();
 				((IExternalContextAware) scopeProvider)
 						.setExternalContext(contextArray);
 			}
 
 			if (RuntimeActivator.DEBUG) {
 				RuntimeActivator.TRACE.trace(
 						RuntimeActivator.TRACE_NETXSCRIPT_EXPRESSION_OPTION,
 						"Parsing/linking expression: " + asString);
 			}
 
			xResource = getResourceFromString(asString);
 
 			if (RuntimeActivator.DEBUG) {
 				RuntimeActivator.TRACE.trace(
 						RuntimeActivator.TRACE_NETXSCRIPT_EXPRESSION_OPTION,
 						"Parsing/linking done");
 			}
 
 			// Get the parse tree.
			final Mod m = (Mod) this.getModel(xResource);
 			// Do we have a root?
 			if( m == null ){
 				return;
 			}
 			// print the context.
 			if (RuntimeActivator.DEBUG) {
 				printInterpreterContext(contextList);
 			}
 
 			// Clear the interpreter and Set the context.
 			if (xInterpreter instanceof IExternalContextAware) {
 				((IExternalContextAware) xInterpreter).clearExternalContext();
 				((IExternalContextAware) xInterpreter)
 						.setExternalContext(contextArray);
 			}
 
 			// What is returned from the evaluation are variables from
 			// the last (outer) scope, which we don't use here.
 			xInterpreter.clearResults();
 			xInterpreter.evaluate(m);
 			setExpressionResult(xInterpreter.getResult());
			xResource.unload();
 		} catch (final Throwable t) {
 			throwable = t;
 		}
 	}
 
 	public boolean errorOccurred() {
 		return throwable != null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.netxforge.netxstudio.server.logic.expression.IExpressionEngine#
 	 * getExpression()
 	 */
 	public Expression getExpression() {
 		return expression;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.netxforge.netxstudio.server.logic.expression.IExpressionEngine#
 	 * setExpression(com.netxforge.netxstudio.library.Expression)
 	 */
 	public void setExpression(Expression expression) {
 		this.expression = expression;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.netxforge.netxstudio.server.logic.expression.IExpressionEngine#
 	 * getExpressionResult()
 	 */
 	public List<BaseExpressionResult> getExpressionResult() {
 		return expressionResult;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.netxforge.netxstudio.server.logic.expression.IExpressionEngine#
 	 * setExpressionResult(java.util.List)
 	 */
 	public void setExpressionResult(List<BaseExpressionResult> expressionResult) {
 		this.expressionResult = expressionResult;
 	}
 
 	private EObject getModel(XtextResource resource) {
 		return resource.getParseResult().getRootASTElement();
 	}
 
 	private final XtextResource getResourceFromString(String model)
 			throws Exception {
 		return getResource(getAsStream(model));
 	}
 
 	private InputStream getAsStream(String model) {
 		return new StringInputStream(model);
 	}
 
 	private final XtextResource getResource(InputStream in) throws Exception {
 
 		// TODO, consider some algorithm for URI generation, could be usefull
 		// for troubleshooting?
 		// return getResource(in,
 		// URI.createURI("mytestmodel." + getCurrentFileExtension()));
 
 		return getResource(in, URI.createURI("mytestmodel." + "netxscript"));
 
 	}
 
 	private final XtextResource getResource(InputStream in, URI uri)
 			throws Exception {
 		final XtextResource resource = doGetResource(in, uri);
 		for (final Diagnostic d : resource.getErrors()) {
 			if (d instanceof ExceptionDiagnostic) {
 				// TODO Do something with the resource erros.
 			}
 		}
 		for (final Diagnostic d : resource.getWarnings())
 			System.out.println("Resource Warning: " + d);
 		return resource;
 	}
 
 	protected XtextResource doGetResource(InputStream in, URI uri)
 			throws Exception {
		xResourceSet.setClasspathURIContext(getClass());
		final XtextResource resource = (XtextResource) xResourceFactory
 				.createResource(uri);
		xResourceSet.getResources().add(resource);
 		resource.load(in, null);
 		if (resource instanceof LazyLinkingResource) {
 			// Linking process here.
 			((LazyLinkingResource) resource)
 					.resolveLazyCrossReferences(CancelIndicator.NullImpl);
 		} else {
 			EcoreUtil.resolveAll(resource);
 		}
 
 		return resource;
 	}
 
 	public Throwable getThrowable() {
 		return throwable;
 	}
 
 	public List<Object> getContext() {
 		return context;
 	}
 
 	public void setContext(List<Object> context) {
 		this.context = context;
 	}
 
 	private void printInterpreterContext(List<IComputationContext> contexts) {
 		RuntimeActivator.TRACE.trace(
 				RuntimeActivator.TRACE_NETXSCRIPT_EXPRESSION_OPTION,
 				"# context= " + contexts.size());
 		for (IComputationContext c : contexts) {
 			Object context = c.getContext();
 			if (context instanceof DateTimeRange) {
 				RuntimeActivator.TRACE
 						.trace(RuntimeActivator.TRACE_NETXSCRIPT_EXPRESSION_OPTION,
 								"Context DTR:"
 										+ modelUtils
 												.periodToStringMore((DateTimeRange) context));
 			}
 			if (context instanceof Node) {
 				RuntimeActivator.TRACE.trace(
 						RuntimeActivator.TRACE_NETXSCRIPT_EXPRESSION_OPTION,
 						"Context Node:" + ((Node) context).getNodeID());
 			}
 			if (context instanceof Component) {
 				RuntimeActivator.TRACE.trace(
 						RuntimeActivator.TRACE_NETXSCRIPT_EXPRESSION_OPTION,
 						"Context Component:" + ((Component) context).getName());
 			}
 			if (context instanceof Service) {
 				RuntimeActivator.TRACE.trace(
 						RuntimeActivator.TRACE_NETXSCRIPT_EXPRESSION_OPTION,
 						"Context Service:"
 								+ ((Service) context).getServiceName());
 			}
 			if (context instanceof BaseResource) {
 				RuntimeActivator.TRACE.trace(
 						RuntimeActivator.TRACE_NETXSCRIPT_EXPRESSION_OPTION,
 						"Context Resource:"
 								+ ((BaseResource) context).getExpressionName());
 			}
 		}
 
 	}
 
 }
