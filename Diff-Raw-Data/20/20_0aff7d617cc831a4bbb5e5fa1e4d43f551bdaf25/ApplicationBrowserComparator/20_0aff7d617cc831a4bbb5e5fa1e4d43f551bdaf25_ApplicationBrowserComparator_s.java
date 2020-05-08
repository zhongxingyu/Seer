 /*******************************************************************************
  * Copyright (c) 2012 AGETO Service GmbH and others.
  * All rights reserved.
  *
  * This program and the accompanying materials are made available under the
  * terms of the Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html.
  *
  * Contributors:
  *     Gunnar Wagenknecht - initial API and implementation
  *     Andreas Mihm	- rework new admin ui
  *******************************************************************************/
 package org.eclipse.gyrex.admin.ui.http.internal;
 
 import org.eclipse.gyrex.admin.ui.http.internal.ApplicationBrowserContentProvider.ApplicationItem;
 import org.eclipse.gyrex.admin.ui.http.internal.ApplicationBrowserContentProvider.GroupNode;
 
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerComparator;
 
 import org.apache.commons.lang.StringUtils;
 
 /**
  *
  */
 public final class ApplicationBrowserComparator extends ViewerComparator {
 
 	public static enum SortIndex {
 		ID, PROVIDER_ID, CONTEXT, STATUS, MOUNTS
 	}
 
 	/** serialVersionUID */
 	private static final long serialVersionUID = 1L;
 
 	private SortIndex index = SortIndex.ID;
 	private boolean reverse;
 
 	@Override
 	public int compare(final Viewer viewer, final Object e1, final Object e2) {
 		if ((e1 instanceof ApplicationItem) && (e2 instanceof ApplicationItem)) {
 			return compareAppRegs((ApplicationItem) e1, (ApplicationItem) e2);
 		} else if ((e1 instanceof GroupNode) && (e2 instanceof GroupNode)) {
 			return compareGroupingItems((GroupNode) e1, (GroupNode) e2);
 		}
 
 		// fallback to super
 		return super.compare(viewer, e1, e2);
 	}
 
 	@SuppressWarnings("unchecked")
 	private int compareAppRegs(final ApplicationItem n1, final ApplicationItem n2) {
 		final String t1 = StringUtils.trimToEmpty(getText(n1));
 		final String t2 = StringUtils.trimToEmpty(getText(n2));
 		if (isReverse()) {
 			return getComparator().compare(t2, t1);
 		} else {
 			return getComparator().compare(t1, t2);
 		}
 	}
 
 	/**
 	 * @param e1
 	 * @param e2
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	private int compareGroupingItems(final GroupNode e1, final GroupNode e2) {
 
 		if (SortIndex.ID.equals(getIndex())) {
 			if (isReverse()) {
				return getComparator().compare(e2.getValue(), e1.getValue());
 			} else {
				return getComparator().compare(e1.getValue(), e2.getValue());
 			}
 		}
 
 		return 0;
 	}
 
 	/**
 	 * Returns the index.
 	 * 
 	 * @return the index
 	 */
 	public SortIndex getIndex() {
 		return null != index ? index : SortIndex.ID;
 	}
 
 	private String getText(final ApplicationItem appReg) {
 		switch (getIndex()) {
 			case ID:
 				return appReg.getApplicationId();
 
 			case PROVIDER_ID:
 				return appReg.getProviderLabel();
 
 			case CONTEXT:
 				return appReg.getContextPath();
 			case MOUNTS:
 				return StringUtils.join(appReg.getMounts(), ", ");
 			default:
 				return appReg.getContextPath();
 		}
 	}
 
 	/**
 	 * Returns the reverse.
 	 * 
 	 * @return the reverse
 	 */
 	public boolean isReverse() {
 		return reverse;
 	}
 
 	/**
 	 * Sets the index.
 	 * 
 	 * @param index
 	 *            the index to set
 	 */
 	public void setIndex(final SortIndex index) {
 		this.index = index;
 
 		// setting an index resets the reverse flag
 		reverse = false;
 	}
 
 	/**
 	 * Sets the reverse.
 	 * 
 	 * @param reverse
 	 *            the reverse to set
 	 */
 	public void setReverse(final boolean reverse) {
 		this.reverse = reverse;
 	}
 
 }
