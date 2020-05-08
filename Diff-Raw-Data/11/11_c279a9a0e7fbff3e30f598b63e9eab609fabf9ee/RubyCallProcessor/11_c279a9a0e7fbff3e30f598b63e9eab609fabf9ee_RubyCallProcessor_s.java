 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.internal.callhierarchy;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.dltk.ast.references.SimpleReference;
 import org.eclipse.dltk.core.ICallProcessor;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.search.IDLTKSearchConstants;
 import org.eclipse.dltk.core.search.IDLTKSearchScope;
 import org.eclipse.dltk.core.search.SearchEngine;
 import org.eclipse.dltk.core.search.SearchMatch;
 import org.eclipse.dltk.core.search.SearchParticipant;
 import org.eclipse.dltk.core.search.SearchPattern;
 import org.eclipse.dltk.core.search.SearchRequestor;
 
 public class RubyCallProcessor implements ICallProcessor {
 	public final static int GENERICS_AGNOSTIC_MATCH_RULE = SearchPattern.R_EXACT_MATCH
 			| SearchPattern.R_CASE_SENSITIVE | SearchPattern.R_ERASURE_MATCH;
 
 	private SearchEngine searchEngine = new SearchEngine();
 
 	public Map process(final IModelElement parent, IModelElement element,
 			IDLTKSearchScope scope, IProgressMonitor monitor) {
 		final Map elements = new HashMap();
 		SearchRequestor requestor = new SearchRequestor() {
 
 			public void acceptSearchMatch(SearchMatch match) {
 				if ((match.getAccuracy() != SearchMatch.A_ACCURATE)) {
 					return;
 				}
 
 				if (match.isInsideDocComment()) {
 					return;
 				}
 
 				if (match.getElement() != null
 						&& match.getElement() instanceof IModelElement) {
 					IModelElement member = (IModelElement) match.getElement();
					ISourceModule module = (ISourceModule) member
							.getAncestor(IModelElement.SOURCE_MODULE);
 					SimpleReference ref = new SimpleReference(
 							match.getOffset(), match.getOffset()
 									+ match.getLength(), ""); //$NON-NLS-1$
 					// try {
 					// IModelElement[] e = module.codeSelect(
 					// match.getOffset(), match.getLength());
 					// for (int j = 0; j < e.length; ++j) {
 					// if (e[j].equals(parent)) {
 					elements.put(ref, member);
 					// }
 					// }
 					//
 					// } catch (ModelException e) {
 					// e.printStackTrace();
 					// }
 				}
 			}
 		};
 
 		SearchPattern pattern = SearchPattern.createPattern(element,
 				IDLTKSearchConstants.REFERENCES, GENERICS_AGNOSTIC_MATCH_RULE,
 				scope.getLanguageToolkit());
 		try {
 			searchEngine
 					.search(pattern, new SearchParticipant[] { SearchEngine
 							.getDefaultSearchParticipant() }, scope, requestor,
 							monitor);
 		} catch (CoreException e) {
 			e.printStackTrace();
 		}
 		return elements;
 	}
 }
