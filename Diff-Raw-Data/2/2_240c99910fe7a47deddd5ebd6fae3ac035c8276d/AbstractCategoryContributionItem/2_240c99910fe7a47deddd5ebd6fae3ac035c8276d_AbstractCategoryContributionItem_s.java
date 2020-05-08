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
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.expressions.IEvaluationContext;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.ActionContributionItem;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITreeSelection;
 import org.eclipse.jface.viewers.TreePath;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.tcf.te.ui.views.ViewsUtil;
 import org.eclipse.tcf.te.ui.views.extensions.CategoriesExtensionPointManager;
 import org.eclipse.tcf.te.ui.views.interfaces.ICategory;
 import org.eclipse.tcf.te.ui.views.interfaces.IUIConstants;
 import org.eclipse.tcf.te.ui.views.interfaces.categories.ICategorizable;
 import org.eclipse.ui.ISources;
 import org.eclipse.ui.actions.CompoundContributionItem;
 import org.eclipse.ui.handlers.IHandlerService;
 import org.eclipse.ui.menus.IWorkbenchContribution;
 import org.eclipse.ui.services.IServiceLocator;
 
 /**
  * Abstract categories dynamic menu contribution implementation.
  */
 public abstract class AbstractCategoryContributionItem extends CompoundContributionItem implements IWorkbenchContribution {
 	// Service locator to located the handler service.
 	protected IServiceLocator serviceLocator;
 
 	/**
 	 * Abstract category action implementation.
 	 */
 	protected abstract static class AbstractCategoryAction extends Action {
 		// The parent contribution item
 		private final AbstractCategoryContributionItem item;
 		// The selection
 		private final ISelection selection;
 		// The category
 		private final ICategory category;
 
 		/**
          * Constructor.
          *
          * @param item The parent contribution item. Must not be <code>null</code>:
          * @param selection The selection. Must not be <code>null</code>.
          * @param category The category. Must not be <code>null</code>.
          * @param single <code>True</code> if the action is the only item added, <code>false</code> otherwise.
          */
         public AbstractCategoryAction(AbstractCategoryContributionItem item, ISelection selection, ICategory category, boolean single) {
         	super();
 
         	Assert.isNotNull(item);
         	this.item = item;
         	Assert.isNotNull(selection);
         	this.selection = selection;
         	Assert.isNotNull(category);
         	this.category = category;
 
         	initialize(single);
         }
 
         /**
          * Initialize the action state.
          *
          * @param single <code>True</code> if the action is the only item added, <code>false</code> otherwise.
          */
         protected void initialize(boolean single) {
         	setText(single ? makeSingleText(category.getLabel()) : category.getLabel());
 
         	Image image = category.getImage();
         	if (image != null) setImageDescriptor(ImageDescriptor.createFromImage(image));
         }
 
         /**
          * Returns the action label in "single" mode.
          *
          * @param text The original label. Must not be <code>null</code>.
          * @return The "single" mode label.
          */
         protected String makeSingleText(String text) {
         	Assert.isNotNull(text);
         	return text;
         }
 
         /* (non-Javadoc)
          * @see org.eclipse.jface.action.Action#run()
          */
         @Override
         public void run() {
         	if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
         		boolean refresh = false;
         		Iterator<?> iterator = ((IStructuredSelection)selection).iterator();
         		while (iterator.hasNext()) {
         			Object element = iterator.next();
         			refresh |= execute(element, category);
         		}
 
         		// Refresh the view
         		if (refresh) ViewsUtil.refresh(IUIConstants.ID_EXPLORER);
         	}
         }
 
     	/**
     	 * Returns the categorizable for the given element.
     	 *
     	 * @param element The element or <code>null</code>.
     	 * @return The categorizable or <code>null</code>.
     	 */
     	protected ICategorizable getCategorizable(Object element) {
     		return item.getCategorizable(element);
     	}
 
         /**
          * Executes the operation to do on the given element.
          *
          * @param selection The selection. Must not be <code>null</code>.
          * @param category The category. Must not be <code>null</code>.
          *
          * @return <code>True</code> if the view needs refreshment, <code>false</code> otherwise.
          */
         protected abstract boolean execute(Object element, ICategory category);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.menus.IWorkbenchContribution#initialize(org.eclipse.ui.services.IServiceLocator)
 	 */
 	@Override
 	public void initialize(IServiceLocator serviceLocator) {
 		this.serviceLocator = serviceLocator;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.actions.CompoundContributionItem#getContributionItems()
 	 */
 	@Override
 	protected IContributionItem[] getContributionItems() {
 		// Get the selected node.
 		IHandlerService service = (IHandlerService)serviceLocator.getService(IHandlerService.class);
 		IEvaluationContext state = service.getCurrentState();
 		ISelection selection = (ISelection)state.getVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME);
 		IStructuredSelection iss = (IStructuredSelection)selection;
 
 		List<IContributionItem> items = new ArrayList<IContributionItem>();
		ICategory[] categories = getCategories(iss, false);
 
 		// Generate the action contribution items
 		for (ICategory category : categories) {
 			IAction action = createAction(this, iss, category, categories.length == 1);
 			if (action != null) {
 				action.setEnabled(isEnabled(iss, category));
 				items.add(new ActionContributionItem(action));
 			}
 		}
 
 		return items.toArray(new IContributionItem[items.size()]);
 	}
 
 	/**
 	 * Returns the list of valid categories for the given selection.
 	 *
 	 * @param selection The selection. Must not be <code>null</code>.
 	 * @param onlyEnabled If <code>true</code>, returns categories which are valid and enabled only.
 	 *
 	 * @return The list of valid categories for the given selection, or an empty list.
 	 */
 	protected ICategory[] getCategories(IStructuredSelection selection, boolean onlyEnabled) {
 		Assert.isNotNull(selection);
 
 		List<ICategory> categories = new ArrayList<ICategory>();
 		ICategory[] allCategories = CategoriesExtensionPointManager.getInstance().getCategories(false);
 
 		// Analyze the selection and add categories valid for all items in the selection
 		boolean firstRun = true;
 		Iterator<?> iterator = selection.iterator();
 		while (iterator.hasNext()) {
 			Object element = iterator.next();
 			if (getCategorizable(element) == null) continue;
 
 			ICategory parentCategory = getParentCategory(element, selection);
 
 			List<ICategory> candidates = new ArrayList<ICategory>();
 			for (ICategory category : allCategories) {
 				if (isValid(parentCategory, element, category)
 						&& (!onlyEnabled || isEnabled(selection, category))) {
 					candidates.add(category);
 				}
 			}
 
 			// On first run, we remember the candidates as is
 			if (firstRun) {
 				categories.addAll(candidates);
 				firstRun = false;
 			} else {
 				// Eliminate all categories not being listed as candidate too
 				Iterator<ICategory> catIterator = categories.iterator();
 				while (catIterator.hasNext()) {
 					ICategory category = catIterator.next();
 					if (!candidates.contains(category)) {
 						catIterator.remove();
 					}
 				}
 			}
 		}
 
 		return categories.toArray(new ICategory[categories.size()]);
 	}
 
 	/**
 	 * Creates the category action instance.
 	 *
      * @param item The parent contribution item. Must not be <code>null</code>:
 	 * @param selection The selection. Must not be <code>null</code>.
 	 * @param category The category. Must not be <code>null</code>.
 	 * @param single <code>True</code> if the action is the only item added, <code>false</code> otherwise.
 	 *
 	 * @return The category action instance.
 	 */
 	protected abstract IAction createAction(AbstractCategoryContributionItem item, ISelection selection, ICategory category, boolean single);
 
 	/**
 	 * Tests if the given combination is valid. If not valid, the combination
 	 * will not be added to the menu.
 	 *
 	 * @param parentCategory The parent category or <code>null</code>.
 	 * @param element The element. Must not be <code>null</code>.
 	 * @param category The category. Must not be <code>null</code>.
 	 *
 	 * @return <code>True</code> if the given combination is valid, <code>false</code> otherwise.
 	 */
 	protected abstract boolean isValid(ICategory parentCategory, Object element, ICategory category);
 
 	/**
 	 * Tests if the given combination is enabled.
 	 *
 	 * @param element The selection. Must not be <code>null</code>.
 	 * @param category The category. Must not be <code>null</code>.
 	 *
 	 * @return <code>True</code> if the given combination is enabled, <code>false</code> otherwise.
 	 */
 	protected abstract boolean isEnabled(ISelection selection, ICategory category);
 
 	/**
 	 * Determines the parent category for the given element, based on the
 	 * given selection.
 	 *
 	 * @param element The element. Must not be <code>null</code>.
 	 * @param selection The selection. Must not be <code>null</code>.
 	 *
 	 * @return The parent category or <code>null</code>.
 	 */
 	protected ICategory getParentCategory(Object element, IStructuredSelection selection) {
 		Assert.isNotNull(element);
 		Assert.isNotNull(selection);
 
 		ICategory parent = null;
 
 		if (selection instanceof ITreeSelection) {
 			TreePath[] pathes = ((ITreeSelection)selection).getPathsFor(element);
 			for (TreePath path : pathes) {
 				TreePath parentPath = path.getParentPath();
 				while (parentPath != null) {
 					if (parentPath.getLastSegment() instanceof ICategory) {
 						parent = (ICategory)parentPath.getLastSegment();
 						break;
 					}
 					parentPath = parentPath.getParentPath();
 				}
 				if (parent != null) break;
 			}
 		}
 
 		return parent;
 	}
 
 	/**
 	 * Returns the categorizable for the given element.
 	 *
 	 * @param element The element or <code>null</code>.
 	 * @return The categorizable or <code>null</code>.
 	 */
 	protected ICategorizable getCategorizable(Object element) {
 	    ICategorizable categorizable = element instanceof IAdaptable ? (ICategorizable)((IAdaptable)element).getAdapter(ICategorizable.class) : null;
     	if (categorizable == null) categorizable = (ICategorizable)Platform.getAdapterManager().getAdapter(element, ICategorizable.class);
     	return categorizable;
 	}
 }
