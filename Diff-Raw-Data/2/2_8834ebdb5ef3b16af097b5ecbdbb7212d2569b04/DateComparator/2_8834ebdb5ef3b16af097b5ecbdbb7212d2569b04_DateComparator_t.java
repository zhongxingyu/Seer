 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity
 /*******************************************************************************
  * Copyright (c) 2011 Ericsson Research Canada
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class extends the default viewer comparator to compare
  * two elements based on their creation date
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.internal.filters;
 
 import java.util.Date;
 
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerComparator;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIComment;
 
 /**
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 public class DateComparator extends ViewerComparator {
 
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Method compare.
 	 * 
 	 * @param viewer
 	 *            Viewer
 	 * @param e1
 	 *            Object
 	 * @param e2
 	 *            Object
 	 * @return int
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public int compare(Viewer viewer, Object e1, Object e2) {
 
 		//For now this comparator applies only to Comment elements
 		if (!(e1 instanceof R4EUIComment && e2 instanceof R4EUIComment)) {
 			return 0;
 		}
 
 		final Date firstDate = ((R4EUIComment) e1).getComment().getCreatedOn();
 		final Date secondDate = ((R4EUIComment) e2).getComment().getCreatedOn();
 
 		//Compare dates
 		if (firstDate.before(secondDate)) {
 			return -1;
 		}
 		if (firstDate.after(secondDate)) {
			return 1;
 		}
 		return 0;
 	}
 }
