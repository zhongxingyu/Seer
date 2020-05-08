 /*******************************************************************************
  * Copyright (c) 2011 Wojciech Galanciak
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     wojciech.galanciak@gmail.com - initial API and implementation
  *******************************************************************************/
 package org.zend.usagedata.internal.swt;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.zend.usagedata.internal.swt.filters.AbstractFilter;
 
 /**
  * Implementation of {@link Listener}. Provide additional ability to connect one
  * or more {@link AbstractFilter} with one {@link EventListener}. It allows to
  * handle multiple component type handlers.
  * 
  * @author wojciech.galanciak@gmail.com
  */
 public class EventListener implements Listener {
 
 	private final int type;
 
 	private List<AbstractFilter> filters;
 
 	public EventListener(int type) {
 		this.filters = new ArrayList<AbstractFilter>();
 		this.type = type;
 	}
 
 	public EventListener(int type, List<AbstractFilter> filters) {
 		this(type);
 		this.filters.addAll(filters);
 	}
 
 	public void handleEvent(Event event) {
		for (AbstractFilter filter : filters) {
			filter.handleEvent(event);
 		}
 	}
 
 	/**
 	 * Add additional filter which should be considered during event handling.
 	 * 
 	 * @param filter
 	 */
 	public void addFilter(AbstractFilter filter) {
 		if (filter.hasType(type)) {
 			filters.add(filter);
 		}
 	}
 
 	/**
 	 * @return listener event type
 	 */
 	public int getType() {
 		return type;
 	}
 
 }
