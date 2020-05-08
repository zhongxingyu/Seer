 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.internal.callhierarchy;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.dltk.ast.references.SimpleReference;
 import org.eclipse.dltk.compiler.SourceElementRequestorAdaptor;
 import org.eclipse.dltk.compiler.env.MethodSourceCode;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.ICalleeProcessor;
 import org.eclipse.dltk.core.IMethod;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.ISourceElementParser;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.search.IDLTKSearchScope;
 import org.eclipse.dltk.core.search.SearchEngine;
 import org.eclipse.dltk.core.search.SearchParticipant;
 import org.eclipse.dltk.core.search.SearchPattern;
 import org.eclipse.dltk.core.search.SearchRequestor;
 import org.eclipse.dltk.ruby.core.RubyNature;
 
 public class RubyCalleeProcessor implements ICalleeProcessor {
 	protected static int EXACT_RULE = SearchPattern.R_EXACT_MATCH
 			| SearchPattern.R_CASE_SENSITIVE;
 
 	private Map fSearchResults = new HashMap();
 
 	private IMethod method;
 
 	// private IDLTKSearchScope scope;
 
 	public RubyCalleeProcessor(IMethod method, IProgressMonitor monitor,
 			IDLTKSearchScope scope) {
 		this.method = method;
 		// this.scope = scope;
 	}
 
 	private class CaleeSourceElementRequestor extends
 			SourceElementRequestorAdaptor {
 
		public void acceptMethodReference(char[] methodName, int argCount,
 				int sourcePosition, int sourceEndPosition) {
 			String name = new String(methodName);
 			int off = 0;
 			try {
 				off = method.getSourceRange().getOffset();
 			} catch (ModelException e) {
 				e.printStackTrace();
 			}
 			SimpleReference ref = new SimpleReference(off + sourcePosition, off
 					+ sourceEndPosition, name);
 			IMethod[] methods = findMethods(name, argCount, off
 					+ sourceEndPosition - 1);
 			fSearchResults.put(ref, methods);
 		}
 
 	}
 
 	public Map doOperation() {
 		CaleeSourceElementRequestor requestor = new CaleeSourceElementRequestor();
 		ISourceElementParser parser = null;
 
 		parser = DLTKLanguageManager
 				.getSourceElementParser(RubyNature.NATURE_ID);
 
 		parser.setRequestor(requestor);
 
 		// parser.parseModule(null, methodSource, null );
 		parser.parseSourceModule(new MethodSourceCode(method), null);
 
 		return fSearchResults;
 	}
 
 	public IMethod[] findMethods(final String methodName, int argCount,
 			int sourcePosition) {
 		final List methods = new ArrayList();
 		ISourceModule module = this.method.getSourceModule();
 		try {
 			IModelElement[] elements = module.codeSelect(sourcePosition, /*
 																		 * methodName.
 																		 * length
 																		 * ()
 																		 */
 			1);
 			for (int i = 0; i < elements.length; ++i) {
 				if (elements[i] instanceof IMethod) {
 					methods.add(elements[i]);
 				}
 			}
 		} catch (ModelException e) {
 			e.printStackTrace();
 		}
 
 		return (IMethod[]) methods.toArray(new IMethod[methods.size()]);
 	}
 
 	protected void search(String patternString, int searchFor, int limitTo,
 			IDLTKSearchScope scope, SearchRequestor resultCollector)
 			throws CoreException {
 		search(patternString, searchFor, limitTo, EXACT_RULE, scope,
 				resultCollector);
 	}
 
 	protected void search(String patternString, int searchFor, int limitTo,
 			int matchRule, IDLTKSearchScope scope, SearchRequestor requestor)
 			throws CoreException {
 		if (patternString.indexOf('*') != -1
 				|| patternString.indexOf('?') != -1) {
 			matchRule |= SearchPattern.R_PATTERN_MATCH;
 		}
 		SearchPattern pattern = SearchPattern.createPattern(patternString,
 				searchFor, limitTo, matchRule, scope.getLanguageToolkit());
 		new SearchEngine().search(pattern,
 				new SearchParticipant[] { SearchEngine
 						.getDefaultSearchParticipant() }, scope, requestor,
 				null);
 	}
 }
