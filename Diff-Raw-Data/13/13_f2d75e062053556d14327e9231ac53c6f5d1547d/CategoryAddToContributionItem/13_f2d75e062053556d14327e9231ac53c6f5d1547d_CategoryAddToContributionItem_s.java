 /*******************************************************************************
  * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.ui.views.internal.categories;
 
 import java.util.Iterator;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.tcf.te.ui.views.Managers;
 import org.eclipse.tcf.te.ui.views.interfaces.ICategory;
 import org.eclipse.tcf.te.ui.views.interfaces.categories.ICategorizable;
 import org.eclipse.tcf.te.ui.views.nls.Messages;
 
 
 /**
  * Dynamic menu contribution: Add To &lt;Category&gt;
  */
 public class CategoryAddToContributionItem extends AbstractCategoryContributionItem {
 
 	/**
 	 * Category action implementation.
 	 */
 	protected static class AddToCategoryAction extends AbstractCategoryAction {
 
 		/**
 		 * Constructor.
          *
          * @param item The parent contribution item. Must not be <code>null</code>:
          * @param selection The selection. Must not be <code>null</code>.
          * @param category The category. Must not be <code>null</code>.
          * @param single <code>True</code> if the action is the only item added, <code>false</code> otherwise.
 		 */
         public AddToCategoryAction(AbstractCategoryContributionItem item, ISelection selection, ICategory category, boolean single) {
 	        super(item, selection, category, single);
         }
 
         /* (non-Javadoc)
          * @see org.eclipse.tcf.te.ui.views.internal.categories.AbstractCategoryContributionItem.AbstractCategoryAction#makeSingleText(java.lang.String)
          */
         @Override
         protected String makeSingleText(String text) {
         	Assert.isNotNull(text);
             return NLS.bind(Messages.AddToCategoryAction_single_text, text);
         }
 
         /* (non-Javadoc)
          * @see org.eclipse.tcf.te.ui.views.internal.categories.AbstractCategoryContributionItem.AbstractCategoryAction#execute(java.lang.Object, org.eclipse.tcf.te.ui.views.interfaces.ICategory)
          */
         @Override
         protected boolean execute(Object element, ICategory category) {
         	Assert.isNotNull(element);
         	Assert.isNotNull(category);
 
         	boolean refresh = false;
 
         	ICategorizable categorizable = getCategorizable(element);
         	Assert.isNotNull(categorizable);
 
         	if (!Managers.getCategoryManager().belongsTo(category.getId(), categorizable.getId())) {
         		Managers.getCategoryManager().add(category.getId(), categorizable.getId());
         		refresh = true;
         	}
 
         	return refresh;
         }
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.views.internal.categories.AbstractCategoryContributionItem#createAction(org.eclipse.tcf.te.ui.views.internal.categories.AbstractCategoryContributionItem, org.eclipse.jface.viewers.ISelection, org.eclipse.tcf.te.ui.views.interfaces.ICategory, boolean)
 	 */
 	@Override
 	protected IAction createAction(AbstractCategoryContributionItem item, ISelection selection, ICategory category, boolean single) {
 	    return new AddToCategoryAction(item, selection, category, single);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.views.internal.categories.AbstractCategoryContributionItem#isValid(org.eclipse.tcf.te.ui.views.interfaces.ICategory, java.lang.Object, org.eclipse.tcf.te.ui.views.interfaces.ICategory)
 	 */
 	@Override
 	protected boolean isValid(ICategory parentCategory, Object element, ICategory category) {
		Assert.isNotNull(parentCategory);
 		Assert.isNotNull(element);
 		Assert.isNotNull(category);
 
 		// Delegate to the categorizable element
 	    ICategorizable categorizable = getCategorizable(element);
 	    Assert.isNotNull(categorizable);
 
     	return categorizable.isValid(ICategorizable.OPERATION.ADD, parentCategory, category);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.views.internal.categories.AbstractCategoryContributionItem#isEnabled(org.eclipse.jface.viewers.ISelection, org.eclipse.tcf.te.ui.views.interfaces.ICategory)
 	 */
 	@Override
 	protected boolean isEnabled(ISelection selection, ICategory category) {
 		Assert.isNotNull(selection);
 		Assert.isNotNull(category);
 
 		boolean enabled = false;
 
     	if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
     		enabled = true;
     		Iterator<?> iterator = ((IStructuredSelection)selection).iterator();
     		while (iterator.hasNext()) {
     			Object element = iterator.next();
 
     			// Delegate to the categorizable element
     		    ICategorizable categorizable = getCategorizable(element);
     		    Assert.isNotNull(categorizable);
     			enabled &= categorizable.isEnabled(ICategorizable.OPERATION.ADD, category);
     			if (!enabled) break;
     		}
     	}
 
     	return enabled;
 	}
 }
