 /*******************************************************************************
  * Copyright (c) 2007, 2013 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.internal.ui.ridgets.swt;
 
 import org.eclipse.core.databinding.BindingException;
 import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
 import org.eclipse.swt.widgets.MenuItem;
 
 import org.eclipse.riena.ui.ridgets.AbstractMarkerSupport;
 import org.eclipse.riena.ui.ridgets.IMenuItemRidget;
 import org.eclipse.riena.ui.swt.utils.SwtUtilities;
 
 /**
  * Ridget of a menu item.
  */
 public class MenuItemRidget extends AbstractItemRidget implements IMenuItemRidget {
 
 	/**
	 * Workaround for bug #397884
	 */
	private final MenuListener enablementWatcher = new MenuAdapter() {
		@Override
		public void menuShown(final MenuEvent e) {
			if (!SwtUtilities.isDisposed(getUIControl()) && isEnabled() != getUIControl().getEnabled()) {
				getUIControl().setEnabled(isEnabled());
			}
		}
	};

	/**
 	 * Returns whether the given menu item is a cascade menu.
 	 * 
 	 * @param menuItem
 	 *            menu item
 	 * @return {@code true} if item is cascade menu; otherwise {@code false}
 	 */
 	protected boolean isMenu(final MenuItem menuItem) {
 		if (menuItem == null) {
 			return false;
 		}
 		return ((menuItem.getStyle() & SWT.CASCADE) == SWT.CASCADE);
 	}
 
 	@Override
 	protected void bindUIControl() {
 		super.bindUIControl();
 		final MenuItem menuItem = getUIControl();
 		if (!SwtUtilities.isDisposed(menuItem)) {
 			menuItem.addSelectionListener(getActionObserver());
			menuItem.getParent().addMenuListener(enablementWatcher);
 		}
 	}
 
 	@Override
 	protected void unbindUIControl() {
 		savedVisibleState = isVisible();
 		final MenuItem menuItem = getUIControl();
 
 		if (!SwtUtilities.isDisposed(menuItem) && !isMenu(menuItem)) {
			menuItem.getParent().removeMenuListener(enablementWatcher);
 			menuItem.removeSelectionListener(getActionObserver());
 		}
 		super.unbindUIControl();
 	}
 
 	@Override
 	protected void checkUIControl(final Object uiControl) {
 		checkType(uiControl, MenuItem.class);
 		if (isMenu((MenuItem) uiControl)) {
 			throw new BindingException("Menu item is a cascade menu item!"); //$NON-NLS-1$
 		}
 	}
 
 	@Override
 	public MenuItem getUIControl() {
 		return (MenuItem) super.getUIControl();
 	}
 
 	@Override
 	protected AbstractMarkerSupport createMarkerSupport() {
 		return new MenuItemMarkerSupport(this, propertyChangeSupport);
 	}
 
 	@Override
 	protected void updateEnabled() {
 		if (!SwtUtilities.isDisposed(getUIControl())) {
 			getUIControl().setEnabled(isEnabled());
 		}
 	}
 
 	@Override
 	AbstractItemProperties createProperties() {
 		return new MenuItemProperties(this);
 	}
 
 }
