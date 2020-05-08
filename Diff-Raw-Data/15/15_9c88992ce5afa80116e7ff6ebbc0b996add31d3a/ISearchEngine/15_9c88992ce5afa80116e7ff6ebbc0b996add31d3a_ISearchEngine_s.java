 /*******************************************************************************
  * Copyright (c) 2009 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *     Zend Technologies
  *******************************************************************************/
 package org.eclipse.dltk.core.index2.search;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.search.IDLTKSearchScope;
 
 /**
  * Basic interface for searching model elements in index.
  * 
  * @author michael
  * @since 2.0
  * 
  */
 public interface ISearchEngine {
 
 	public enum MatchRule {
 
 		/** The pattern name must match exactly the name of search result. */
 		EXACT,
 
 		/** The pattern name is a prefix of search results. */
 		PREFIX,
 
 		/**
 		 * The pattern name consists of upper-case letters of the search result.
 		 */
 		CAMEL_CASE,
 
 		/**
 		 * The pattern is a set of names separated by ',', where one of them
 		 * matches the search result.
 		 */
 		SET,
 
 		/**
 		 * The name is a POSIX pattern ('*' - any string, '?' - any character)
 		 */
 		PATTERN,
 	}
 
 	public enum SearchFor {
 		/** Search for references */
 		REFERENCES,
 
 		/** Search for declarations */
 		DECLARATIONS,
 
 		/** Search for declarations as well as references */
		ALL_OCCURENCES
 	}
 
 	/**
 	 * Search for model elements in index.
 	 * 
 	 * @param elementType
 	 *            Element type ({@link IModelElement#TYPE},
 	 *            {@link IModelElement#METHOD},{@link IModelElement#FIELD},etc.)
 	 * @param qualifier
 	 *            Element qualifier (package name)
 	 * @param elementName
 	 *            Element name pattern
 	 * @param trueFlags
 	 *            Logical OR of flags that must exist in element flags bitset.
 	 *            Set to <code>0</code> to disable filtering by trueFlags.
 	 * @param falseFlags
 	 *            Logical OR of flags that must not exist in the element flags
 	 *            bitset. Set to <code>0</code> to disable filtering by
 	 *            falseFlags.
 	 * @param limit
 	 *            Limit number of results (<code>0</code> - unlimited)
 	 * @param searchFor
 	 *            A combination of {@link #SF_REFS}, {@link #SF_DECLS}
 	 * @param matchRule
 	 *            A combination of {@link #MR_EXACT}, {@link #MR_PREFIX}
 	 * @param scope
 	 *            Search scope
 	 * @param requestor
 	 *            Search requestor
 	 * @param monitor
 	 *            Progress monitor
 	 */
 	public void search(int elementType, String qualifier, String elementName,
 			int trueFlags,
 			int falseFlags, int limit, SearchFor searchFor,
 			MatchRule matchRule, IDLTKSearchScope scope,
 			ISearchRequestor requestor, IProgressMonitor monitor);
 
 }
