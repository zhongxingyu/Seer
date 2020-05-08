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
 
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.tcf.te.ui.views.extensions.CategoriesExtensionPointManager;
 import org.eclipse.tcf.te.ui.views.interfaces.ICategory;
 import org.eclipse.ui.IElementFactory;
 import org.eclipse.ui.IMemento;
 /**
  * The element factory for a category.
  */
 public class CategoryFactory implements IElementFactory {
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.IElementFactory#createElement(org.eclipse.ui.IMemento)
 	 */
 	@Override
	public IAdaptable createElement(IMemento memento) {
		String id = memento.getString("id"); //$NON-NLS-1$
		ICategory category = CategoriesExtensionPointManager.getInstance().getCategory(id, false);
		return category instanceof IAdaptable ? (IAdaptable)category : null;
 	}
 }
