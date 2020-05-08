 /*******************************************************************************
  * Copyright (c) Sep 24, 2012 NetXForge.
  * 
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details. You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>
  * 
  * Contributors: Christophe Bouhier - initial API and implementation and/or
  * initial documentation
 *******************************************************************************/
 package com.netxforge.netxstudio.screens.f2;
 
 import java.util.List;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.xtext.parser.IParseResult;
 import org.eclipse.xtext.resource.XtextResource;
 import org.eclipse.xtext.ui.editor.model.IXtextDocument;
 import org.eclipse.xtext.util.concurrent.IUnitOfWork;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 import com.google.inject.name.Named;
 import com.netxforge.interpreter.IInterpreter;
 import com.netxforge.interpreter.IInterpreterContext;
 import com.netxforge.interpreter.IInterpreterContextFactory;
 import com.netxforge.netxscript.Mod;
 import com.netxforge.netxstudio.common.guice.IInjectorProxy;
 import com.netxforge.netxstudio.common.model.ModelUtils;
 import com.netxforge.netxstudio.generics.DateTimeRange;
 import com.netxforge.netxstudio.library.BaseExpressionResult;
 import com.netxforge.netxstudio.library.Component;
 import com.netxforge.netxstudio.library.Expression;
 import com.netxforge.netxstudio.library.NetXResource;
 import com.netxforge.netxstudio.operators.Node;
import com.netxforge.scoping.IExternalContextAware;
 
 /**
 * Helper, which can build a context.
 * 
  * @author Christophe
  */
 public class ExpressionSupport {
 
 	private Injector nextscriptInjector;

 	private IInterpreter interpreter;
 
 	private IInterpreterContextFactory interpreterContextFactory;
 
 	@Inject
	public ExpressionSupport(@Named("Netxscript") IInjectorProxy injectorProxy) {

 		nextscriptInjector = injectorProxy
 				.getInjector("com.netxforge.Netxscript");
 		interpreter = nextscriptInjector.getInstance(IInterpreter.class);
 		interpreterContextFactory = nextscriptInjector
 				.getInstance(IInterpreterContextFactory.class);
 
 	}
 
 	@Inject
 	ModelUtils modelUtils;
 
 	/**
 	 * infer the Node and the Component from the NetXResource and set as
 	 * context.
 	 * 
 	 * @param timeRange
 	 * @param component
 	 * @return
 	 */
 	public ImmutableList<IInterpreterContext> buildContext(
 			DateTimeRange timeRange, Component component) {
 		Node node = modelUtils.nodeFor(component);
 		return buildContext(timeRange, new Object[] { node, component });
 	}
 
 	public ImmutableList<IInterpreterContext> buildContext(
 			DateTimeRange timeRange, NetXResource resource) {
 		Component component = resource.getComponentRef();
 		if (component != null) {
 			Node node = modelUtils.nodeFor(component);
 			return buildContext(timeRange, new Object[] { node, component,
 					resource });
 		}
 		return null;
 	}
 
 	public ImmutableList<IInterpreterContext> buildContext(
 			DateTimeRange timeRange, Object... objects) {
 
 		if (timeRange == null)
 			throw new IllegalArgumentException("Should set a period context");
 
 		if (objects == null || objects.length < 1)
 			throw new IllegalArgumentException(
 					"Should have one or more context objects");
 
 		assert interpreterContextFactory != null : "Context factory is not initialized (check guice binding)";
 
 		// Context initialization.
 		final IInterpreterContext periodContext = interpreterContextFactory
 				.createPeriodContext(timeRange);
 		final List<IInterpreterContext> contextList = Lists
 				.newArrayList(periodContext);
 
 		for (Object o : objects) {
 			final IInterpreterContext objectContext = interpreterContextFactory
 					.createContext(o);
 			contextList.add(objectContext);
 		}
 
 		return ImmutableList.copyOf(contextList);
 	}
 
 	/**
	 * Not used.
	 * 
 	 * @param expression
 	 * @param contextList
 	 * @param doc
 	 * @deprecated
 	 * @return
 	 */
 	public List<BaseExpressionResult> testExpression(Expression expression,
 			ImmutableList<IInterpreterContext> contextList, IXtextDocument doc) {
 
 		List<BaseExpressionResult> result = null;
 
 		// final IXtextDocument doc = xtextEditor.getDocument();
 		assert !documentHasErrors(doc) : "Intepreter cancelled, as errors exist in script: "
 				+ doc.get();
 
 		final IInterpreterContext[] contextArray = new IInterpreterContext[contextList
 				.size()];
		if (interpreter instanceof IExternalContextAware) {
			((IExternalContextAware) interpreter)
					.setExternalContext(contextList.toArray(contextArray));
		}
 
 		result = doc
 				.readOnly(new IUnitOfWork<List<BaseExpressionResult>, XtextResource>() {
 					public List<BaseExpressionResult> exec(
 							XtextResource resource) throws Exception {
 						if (resource.getContents().isEmpty()) {
 							return null;
 						}
 						try {
 							EObject rootASTElement = resource.getParseResult()
 									.getRootASTElement();
 
 							if ((rootASTElement instanceof Mod)) {
 								Mod root = (Mod) resource.getContents().get(0);
 								interpreter.evaluate(root);
 								return interpreter.getResult();
 							}
 						} catch (Throwable t) {
 							// something with t.
 							t.printStackTrace();
 						}
 						return null;
 					}
 				});
 
 		return result;
 	}
 
 	public boolean documentHasErrors(final IXtextDocument xtextDocument) {
 		return (xtextDocument
 				.readOnly(new IUnitOfWork<Boolean, XtextResource>() {
 					public Boolean exec(XtextResource state) throws Exception {
 						IParseResult parseResult = state.getParseResult();
 						// CB Change.
 						return !state.getErrors().isEmpty()
 								|| parseResult == null
 								|| parseResult.getSyntaxErrors().iterator()
 										.hasNext();
 					}
 				}));
 	}
 
 }
