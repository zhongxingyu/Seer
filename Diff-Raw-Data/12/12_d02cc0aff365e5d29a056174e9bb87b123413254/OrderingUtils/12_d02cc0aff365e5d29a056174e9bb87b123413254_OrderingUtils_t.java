 /*******************************************************************************
  * Copyright (c) 2006, 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.ui.util;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.emf.compare.diff.metamodel.DiffElement;
 import org.eclipse.emf.compare.diff.metamodel.DiffGroup;
 import org.eclipse.emf.compare.ui.viewer.filter.IDifferenceFilter;
 
 /**
  * Utility class providing services about ordering facility on structure viewer.
  * 
  * @author <a href="mailto:cedric.notot@obeo.fr">Cedric Notot</a>
  * @since 1.3
  */
 public final class OrderingUtils {
 
 	/**
 	 * Constructor.
 	 */
 	private OrderingUtils() {
		/*
		 * nothing special here.
		 */
 	}
 
 	/**
 	 * Checks if the element is hidden by a filter at least.
 	 * 
 	 * @param element
 	 *            The element.
 	 * @param selectedFilters
 	 *            The selected filters.
 	 * @return true if it is hidden, false otherwise.
 	 */
 	public static boolean isHidden(DiffElement element, List<IDifferenceFilter> selectedFilters) {
 		if (selectedFilters != null) {
 			final Iterator<IDifferenceFilter> it = selectedFilters.iterator();
 			while (it.hasNext()) {
 				final IDifferenceFilter diffFilter = it.next();
 				// We do not support filtering of DiffGroup
 				// (as its DifferenceKind is always "Change" -- see DiffElementImpl#getKind() for how)
 				if (!(element instanceof DiffGroup) && diffFilter.hides(element)) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 }
