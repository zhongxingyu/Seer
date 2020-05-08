 /*******************************************************************************
  * Copyright (c) 2011 NumberFour AG
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     NumberFour AG - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.compiler.problem;
 
import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.dltk.compiler.util.Util;
 import org.eclipse.dltk.internal.core.InternalDLTKLanguageManager;
 
 public class ProblemCategoryManager {
 	private static final String ID_ATTR = "id";
 	private static final String NATURE_ATTR = "nature";
 	private static final String SCOPE_ATTR = "scope";
 
 	private static final String CATEGORY_ELEMENT = "problemCategory";
 	private static final String PROBLEM_ELEMENT = "problem";
 
 	private static ProblemCategoryManager instance = null;
 
 	public static synchronized ProblemCategoryManager getInstance() {
 		if (instance == null) {
 			instance = new ProblemCategoryManager();
 		}
 		return instance;
 	}
 
 	static class Key {
 		final String natureId;
 		final String scopeId;
 
 		public Key(String natureId, String scopeId) {
 			this.natureId = natureId;
 			this.scopeId = scopeId != null ? scopeId : Util.EMPTY_STRING;
 		}
 
 		@Override
 		public int hashCode() {
 			return natureId.hashCode() * 13 + scopeId.hashCode();
 		}
 
 		public boolean equals(Object obj) {
 			if (obj instanceof Key) {
 				final Key other = (Key) obj;
 				return natureId.equals(other.natureId)
 						&& scopeId.equals(other.scopeId);
 			}
 			return false;
 		}
 	}
 
 	private final Map<Key, ScopeDescriptor> scopes = new HashMap<Key, ScopeDescriptor>();
 
 	@SuppressWarnings("serial")
 	static class ScopeDescriptor extends HashMap<String, ProblemCategory> {
 
 	}
 
 	@SuppressWarnings("serial")
 	static class ProblemCategory extends HashSet<IProblemIdentifier> implements
 			IProblemCategory {
 
 		private final String name;
 		private final Collection<IProblemIdentifier> contents;
 
 		public ProblemCategory(String name) {
 			this.name = name;
 			contents = Collections.unmodifiableCollection(this);
 		}
 
 		public String name() {
 			return name;
 		}
 
 		public Collection<IProblemIdentifier> contents() {
 			return contents;
 		}
 
 	}
 	
	/**
	 * returns an array (empty is nothing can be found) of all the category ids
	 * that can be found for this problem.
	 * 
	 * @param natureId
	 * @param scopeId
	 * @param problem
	 * @return
	 */
	public String[] getID(String natureId, String scopeId,
 			IProblemIdentifier problem) {
 		Assert.isNotNull(natureId);
 		ScopeDescriptor scope = getScope(natureId, scopeId);
 		if (scope == null)
			return new String[0];
 		Iterator<Entry<String, ProblemCategory>> iterator = scope.entrySet()
 				.iterator();
		List<String> ids = new ArrayList<String>();
 		while (iterator.hasNext()) {
 			Entry<String, ProblemCategory> entry = iterator.next();
 			if (entry.getValue().contains(problem)) {
				ids.add(entry.getKey());
 			}
 		}
		return ids.toArray(new String[ids.size()]);
 	}
 
 	public IProblemCategory getCategory(String natureId, String scopeId,
 			String id) {
 		Assert.isNotNull(natureId);
 		ScopeDescriptor scope = getScope(natureId, scopeId);
 		if (scope == null)
 			return null;
 		return scope.get(id);
 	}
 
 	/**
 	 * @param natureId
 	 * @param scopeId
 	 * @return
 	 */
 	private ScopeDescriptor getScope(String natureId, String scopeId) {
 		final Key scopeKey = new Key(natureId, scopeId);
 		ScopeDescriptor scope;
 		synchronized (scopes) {
 			scope = scopes.get(scopeKey);
 			if (scope == null && scopes.containsKey(scopeKey)) {
 				return null;
 			}
 		}
 		if (scope == null) {
 			scope = new ScopeDescriptor();
 			final IConfigurationElement[] elements = Platform
 					.getExtensionRegistry()
 					.getConfigurationElementsFor(
 							InternalDLTKLanguageManager.PROBLEM_FACTORY_EXTPOINT);
 			for (IConfigurationElement element : elements) {
 				if (CATEGORY_ELEMENT.equals(element.getName())
 						&& scopeKey.natureId.equals(element
 								.getAttribute(NATURE_ATTR))
 						&& scopeKey.scopeId.equals(element
 								.getAttribute(SCOPE_ATTR))) {
 					final String categoryId = element.getAttribute(ID_ATTR);
 					if (categoryId != null) {
 						ProblemCategory category = scope.get(categoryId);
 						if (category == null) {
 							category = new ProblemCategory(categoryId);
 							scope.put(categoryId, category);
 						}
 						for (IConfigurationElement problem : element
 								.getChildren(PROBLEM_ELEMENT)) {
 							final IProblemIdentifier identifier = DefaultProblemIdentifier
 									.decode(problem.getValue());
 							if (identifier != null
 									&& !(identifier instanceof ProblemIdentifierInt)
 									&& !(identifier instanceof ProblemIdentifierString)) {
 								category.add(identifier);
 							}
 						}
 					}
 				}
 			}
 			synchronized (scopes) {
 				scopes.put(scopeKey, scope);
 			}
 		}
 		return scope;
 	}
 }
