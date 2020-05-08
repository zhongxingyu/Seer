 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.internal.navigation.ui.filter;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.riena.core.marker.IMarker;
 import org.eclipse.riena.core.util.StringMatcher;
 import org.eclipse.riena.ui.filter.IUIFilterRuleMarkerRidget;
 import org.eclipse.riena.ui.filter.impl.AbstractUIFilterRuleMarker;
 import org.eclipse.riena.ui.ridgets.IMarkableRidget;
 
 /**
  * Filter rule to provide a marker for a ridget.
  */
 public abstract class AbstractUIFilterRuleRidgetMarker extends AbstractUIFilterRuleMarker implements
 		IUIFilterRuleMarkerRidget {
 
 	private Map<IMarkableRidget, IMarker> markerMap;
 	protected RidgetMatcher matcher;
 
 	/**
 	 * Creates a new instance of {@code AbstractUIFilterRuleRidgetMarker}.
 	 * 
 	 * @param idPattern
 	 *            - pattern ({@link StringMatcher}) for ridget IDs
 	 * @param marker
 	 */
 	public AbstractUIFilterRuleRidgetMarker(String idPattern, IMarker marker) {
 		super(marker);
 		matcher = createMatcher(idPattern);
 		markerMap = new HashMap<IMarkableRidget, IMarker>();
 	}
 
 	/**
 	 * This method compares the ID of this rule and the given ID of a ridget.
 	 * 
 	 * @see org.eclipse.riena.ui.internal.IUIFilterRule.IUIFilterAttribute#matches(java.lang.Object)
 	 */
 
 	public boolean matches(Object... args) {
 		if ((args == null) || (args.length <= 0)) {
 			return false;
 		}
 		return matcher.matches(args);
 	}
 
 	/**
 	 * Adds the marker of this rule to the given object (if the object is an
 	 * markable ridget).
 	 * 
 	 * @see org.eclipse.riena.ui.internal.IUIFilterRule.IUIFilterAttribute#apply(java.lang.Object)
 	 */
 	public void apply(Object object) {
 		if (object instanceof IMarkableRidget) {
 			IMarkableRidget markableRidget = (IMarkableRidget) object;
 			IMarker marker = getMarker();
 			markableRidget.addMarker(marker);
 			markerMap.put(markableRidget, marker);
 		}
 	}
 
 	/**
 	 * Removes the marker of this rule from the given object (if the object is
 	 * an markable ridget).
 	 * 
 	 * @see org.eclipse.riena.ui.internal.IUIFilterRule.IUIFilterAttribute#remove(java.lang.Object)
 	 */
 	public void remove(Object object) {
 		if (object instanceof IMarkableRidget) {
 			IMarkableRidget markableRidget = (IMarkableRidget) object;
 			IMarker marker = markerMap.get(markableRidget);
 			markableRidget.removeMarker(marker);
			markerMap.remove(markableRidget);
 		}
 	}
 
 	public void setId(String idPattern) {
 		matcher.setId(idPattern);
 	}
 
 	protected RidgetMatcher createMatcher(String idPattern) {
 		return new RidgetMatcher(idPattern);
 	}
 
 }
