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
 
import java.util.concurrent.atomic.AtomicReference;

 import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.widgets.Display;
 import org.eclipse.tcf.te.ui.views.extensions.CategoriesExtensionPointManager;
 import org.eclipse.tcf.te.ui.views.interfaces.ICategory;
 import org.eclipse.ui.IElementFactory;
 import org.eclipse.ui.IMemento;
import org.eclipse.ui.PlatformUI;
 /**
  * The element factory for a category.
  */
 public class CategoryFactory implements IElementFactory {
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.IElementFactory#createElement(org.eclipse.ui.IMemento)
 	 */
 	@Override
	public IAdaptable createElement(final IMemento memento) {
		final AtomicReference<ICategory> category = new AtomicReference<ICategory>();

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				String id = memento.getString("id"); //$NON-NLS-1$
				category.set(CategoriesExtensionPointManager.getInstance().getCategory(id, false));
			}
		};

		if (Display.findDisplay(Thread.currentThread()) == null) {
			PlatformUI.getWorkbench().getDisplay().syncExec(runnable);
		} else {
			runnable.run();
		}

		return category.get() instanceof IAdaptable ? (IAdaptable)category.get() : null;
 	}
 }
