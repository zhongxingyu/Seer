 /*******************************************************************************
  * Copyright (c) 2007, 2014 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.internal.ui.ridgets.swt;
 
 import org.eclipse.core.databinding.observable.list.WritableList;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 
 import org.eclipse.riena.ui.ridgets.swt.AbstractSWTRidget;
 import org.eclipse.riena.ui.swt.ChoiceComposite;
 import org.eclipse.riena.ui.swt.lnf.LnFUpdater;
 import org.eclipse.riena.ui.swt.utils.SWTControlFinder;
 import org.eclipse.riena.ui.swt.utils.SwtUtilities;
 
 /**
  * Baseclass for all ChoiceRidgets.
  */
 public abstract class AbstractChoiceRidget extends AbstractSWTRidget {
 	protected static final String CHOICE_RIDGET_LISTENER = "choiceRidget.listener"; //$NON-NLS-1$
 	protected final static LnFUpdater LNF_UPDATER = LnFUpdater.getInstance();
 
 	protected String[] optionLabels;
 
 	/** The list of available options. */
 	protected WritableList optionsObservable = new WritableList();
 
 	public AbstractChoiceRidget() {
 		optionsObservable = new WritableList();
 	}
 
 	protected void layoutNewChildren() {
 		final ChoiceComposite control = (ChoiceComposite) getUIControl();
 		if (SwtUtilities.isDisposed(control)) {
 			return;
 		}
 		final Control[] childrenButtonsOld = control.getChildrenButtons();
 		final int oldCount = childrenButtonsOld.length;
 		final int newCount = optionsObservable.toArray().length;
 
 		//		disposeChildren(control);
 		createChildren(control, true);
 
 		if (oldCount != newCount) {
 			// if the number of children has changed
 			// update the layout of the parent composite
 			control.getParent().layout(true, false);
 		}
 	}
 
 	protected void createChildren(final ChoiceComposite control) {
 		createChildren(control, false);
 	}
 
 	protected void createChildren(final ChoiceComposite control, final boolean reuseButtons) {
 		if (control != null && !control.isDisposed()) {
 			final Object[] values = optionsObservable.toArray();
 			final Control[] childrenOld = control.getChildrenButtons();
 
 			int i;
 			for (i = 0; i < values.length; i++) {
 				final Object value = values[i];
 				final String caption = optionLabels != null ? optionLabels[i] : String.valueOf(value);
 
 				Button button;
 				if (reuseButtons && i < childrenOld.length) {
 					button = (Button) childrenOld[i];
 					unconfigureOptionButton(button);
 					button.setText(caption);
 				} else {
 					button = control.createChild(caption);
 				}
 				button.setData(value);
 				configureOptionButton(button);
 			}
 
 			if (reuseButtons) {
 				// dispose buttons that are not needed anymore
 				while (i < childrenOld.length) {
 					childrenOld[i++].dispose();
 				}
 			}
 
 			updateSelection(control);
 			LNF_UPDATER.updateUIControls(control, true);
 		}
 	}
 
 	protected abstract void updateSelection(final ChoiceComposite control);
 
 	protected abstract void configureOptionButton(final Button button);
 
 	protected abstract void unconfigureOptionButton(final Button button);
 
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
		if (!SwtUtilities.isDisposed(getUIControl())) {
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
