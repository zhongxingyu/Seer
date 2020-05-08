 /*******************************************************************************
  * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.ui.views.internal.categories;
 
 import org.eclipse.core.expressions.PropertyTester;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITreeSelection;
 import org.eclipse.jface.viewers.TreePath;
 import org.eclipse.tcf.te.ui.views.interfaces.ICategory;
 
 /**
  * Category property tester.
  */
 public class CategoryPropertyTester extends PropertyTester {
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
 	 */
 	@Override
 	public boolean test(final Object receiver, final String property, final Object[] args, final Object expectedValue) {
 		if (receiver instanceof IStructuredSelection) {
 			// Analyze the selection
			testSelection((IStructuredSelection)receiver, property, args, expectedValue);
 		}
 		return false;
 	}
 
 	/**
 	 * Test the specific selection properties.
 	 *
 	 * @param selection The selection. Must not be <code>null</code>.
 	 * @param property The property to test.
 	 * @param args The property arguments.
 	 * @param expectedValue The expected value.
 	 *
 	 * @return <code>True</code> if the property to test has the expected value, <code>false</code>
 	 *         otherwise.
 	 */
 	protected boolean testSelection(IStructuredSelection selection, String property, Object[] args, Object expectedValue) {
 		Assert.isNotNull(selection);
 
 		if ("parentCategoryId".equals(property) && !selection.isEmpty()) { //$NON-NLS-1$
 			// Only single element selection is supported
 			Object element = selection.getFirstElement();
 
 			TreePath[] pathes = selection instanceof ITreeSelection ? ((ITreeSelection)selection).getPathsFor(element) : null;
 			if (pathes != null) {
 				for (TreePath path : pathes) {
 					// Find the category within the tree path
 					TreePath parentPath = path.getParentPath();
 					while (parentPath != null) {
 						Object lastSegment = parentPath.getLastSegment();
 						if (lastSegment instanceof ICategory
 								&& ((ICategory)lastSegment).getId().equals(expectedValue)) {
 							return true;
 						}
 						parentPath = parentPath.getParentPath();
 					}
 				}
 			}
 
 		}
 
 		return false;
 	}
 }
