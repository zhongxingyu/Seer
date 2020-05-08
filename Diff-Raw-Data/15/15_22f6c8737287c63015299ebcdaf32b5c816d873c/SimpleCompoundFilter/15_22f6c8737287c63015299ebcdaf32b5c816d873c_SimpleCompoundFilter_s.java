 /*******************************************************************************
  * Copyright (c) 2014 Formal Mind GmbH.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Ingo Weigelt - initial API and implementation
  ******************************************************************************/
 package org.eclipse.rmf.reqif10.search.filter;
 
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.eclipse.rmf.reqif10.SpecElementWithAttributes;
 
 
 /**
  * A filter that is composed of other Filters. 
  * 
  * The match method is an AND or OR composition of other filters. 
  *
  */
 
 public class SimpleCompoundFilter implements IFilter {
 
 	
 	private boolean orFilter;
 	private final List<IFilter> filters;	
 	
 	/**
 	 * 
 	 * @param filters
 	 *            the list of filters to apply to the SpecObject
 	 * 
 	 * @param orFilter
 	 *            if true the match method will return true if any of the
 	 *            filters returns true.
 	 *            if false the match method will return true if ALL of the
 	 *            filters return true.
 	 * 
 	 */
 	public SimpleCompoundFilter(List<IFilter> filters, boolean orFilter) {
 		this.orFilter = orFilter ;
 		this.filters = new LinkedList<IFilter>(filters);
 	}
 	
 	public boolean isOrFilter() {
 		return orFilter;
 	}
 	
 	public List<IFilter> getFilters() {
 		return Collections.unmodifiableList(filters);
 	}
 	
 	
 	@Override
 	public boolean match(SpecElementWithAttributes element) {
		boolean result = false;
 		for (IFilter filter : filters) {
 			boolean match = filter.match(element);
			if (match && orFilter){
				return true;
 			}
			result &= match;
 		}
		return result;
 	}
 
 	@Override
 	public Object getAttribute() {
 		return null;
 	}
 
 	
 
 }
