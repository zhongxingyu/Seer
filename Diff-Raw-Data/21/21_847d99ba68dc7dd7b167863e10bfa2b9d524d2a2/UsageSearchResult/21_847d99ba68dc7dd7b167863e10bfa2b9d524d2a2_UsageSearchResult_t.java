 /*******************************************************************************
  * Copyright (c) 2014 Hussein Mhanna
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Hussein Mhanna - initial API and implementation
  ******************************************************************************/
 package org.eclipse.rmf.reqif10.search.ui;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
import java.util.Map.Entry;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.search.ui.ISearchQuery;
 import org.eclipse.search.ui.ISearchResult;
 import org.eclipse.search.ui.ISearchResultListener;
 
 /**
  * @author Hussein MHANNA
  * 
  */
 public class UsageSearchResult implements ISearchResult {
 
 	private final ReqIFSearchQuery query;
 
 	private final Map<Resource, Collection<EObject>> searchEntries;
 
 	private final Collection<ISearchResultListener> searchResultListeners = new ArrayList<ISearchResultListener>();
 
 	public UsageSearchResult(final ReqIFSearchQuery query) {
 		this.query = query;
 		this.searchEntries = new HashMap<Resource, Collection<EObject>>();
 	}
 
 	/**
 	 * do nothing
 	 * 
 	 */
 	@Override
 	public void addListener(final ISearchResultListener l) {
 		searchResultListeners.add(l);
 	}
 
 	/**
 	 * do nothing
 	 */
 	@Override
 	public void removeListener(final ISearchResultListener l) {
 		searchResultListeners.remove(l);
 	}
 
 	/**
 	 * Return the label for this search result
 	 * 
 	 * @return The label for this search result
 	 */
 	@Override
 	public String getLabel() {
 		StringBuilder builder = new StringBuilder("Search in files '");
		int resultFoundSize = 0;
		for (Entry<Resource, Collection<EObject>> entry : searchEntries
				.entrySet()) {
			if (false == entry.getValue().isEmpty()) {
				resultFoundSize++;
			}
		}
 		// to limit the label, we take only max three resources
		int max = resultFoundSize > 0 ? Math.min(3, resultFoundSize) : 3;
 		int count = 0;
		Collection<Resource> resources = searchEntries.keySet();
 		for (Iterator<Resource> iterator = resources.iterator(); iterator
 				.hasNext() && count < max;) {
 			Resource resource = iterator.next();
 			builder.append(resource.getURI().lastSegment()).append(" - ");
 			count++;
 		}
		if (count < resultFoundSize) {
 			builder.append("... - ");
 		}
		builder.append("' result : ").append(resultFoundSize)
 				.append(" occurences");
 		return builder.toString();
 	}
 
 	/**
 	 * Return <code>null</code>
 	 * 
 	 * @return <code>null</code>
 	 */
 	@Override
 	public String getTooltip() {
 		return null;
 	}
 
 	/**
 	 * Return <code>null</code>
 	 * 
 	 * @return <code>null</code>
 	 */
 	@Override
 	public ImageDescriptor getImageDescriptor() {
 		return null;
 	}
 
 	/**
 	 * Returns the query that produced this search result
 	 * 
 	 * @return the query producing this result
 	 */
 	@Override
 	public ISearchQuery getQuery() {
 		return query;
 	}
 
 	/**
 	 * Return the collection of search entries
 	 * 
 	 * @return The collection of search entries
 	 */
 	public Map<Resource, Collection<EObject>> getSearchEntries() {
 		return searchEntries;
 	}
 }
