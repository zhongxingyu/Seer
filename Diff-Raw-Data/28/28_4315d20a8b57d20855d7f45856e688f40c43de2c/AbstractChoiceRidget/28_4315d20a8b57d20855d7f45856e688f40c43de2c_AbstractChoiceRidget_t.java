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
 
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 
 import org.eclipse.riena.ui.ridgets.swt.AbstractSWTRidget;
 import org.eclipse.riena.ui.swt.ChoiceComposite;
 import org.eclipse.riena.ui.swt.utils.SWTControlFinder;
 import org.eclipse.riena.ui.swt.utils.SwtUtilities;
 
 /**
  * Baseclass for all ChoiceRidgets.
  */
 public abstract class AbstractChoiceRidget extends AbstractSWTRidget {
 
 	protected void disposeChildren(final ChoiceComposite control) {
 		if (control != null && !control.isDisposed()) {
 			for (final Control child : control.getChildrenButtons()) {
 				child.dispose();
 			}
 		}
 	}
 
 	/**
 	 * Returns the number of the children of the given UI control.
	 * <p>
	 * this method is not API, visibility for testing
 	 * 
 	 * @param control
 	 *            UI control
 	 * 
 	 * @return number of children
 	 */
	public int getChildrenCount(final ChoiceComposite control) {
 		if (SwtUtilities.isDisposed(control)) {
 			return 0;
 		}
		return control.getChildrenButtons().length;
 	}
 
 	@Override
 	public boolean hasFocus() {
 		if (getUIControl() != null) {
 			final Control control = getUIControl();
 
 			if (control.isFocusControl()) {
 				return true;
 			}
 
 			if (!(control instanceof Composite)) {
 				return false;
 			}
 
 			final ChildFocusChecker checker = new ChildFocusChecker((Composite) control);
 			checker.run();
 
 			return checker.childHasFocus;
 		}
 		return false;
 	}
 
 	/**
 	 * Iterates over the child controls of a given composite and checks if one them has the focus.
 	 */
 	private static class ChildFocusChecker extends SWTControlFinder {
 
 		private boolean childHasFocus = false;
 
 		public ChildFocusChecker(final Composite composite) {
 			super(composite);
 		}
 
 		@Override
 		public void handleBoundControl(final Control control, final String bindingProperty) {
 			super.handleControl(control);
 		}
 
 		@Override
 		public void handleControl(final Control control) {
 			super.handleControl(control);
 			if (control.isFocusControl()) {
 				childHasFocus = true;
 			}
 		}
 	}
 
 }
