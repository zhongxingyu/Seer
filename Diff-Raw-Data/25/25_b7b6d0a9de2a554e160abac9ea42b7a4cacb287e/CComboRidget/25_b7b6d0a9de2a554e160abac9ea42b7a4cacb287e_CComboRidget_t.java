 /*******************************************************************************
  * Copyright (c) 2007, 2012 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.internal.ui.ridgets.swt;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import org.eclipse.core.databinding.BindingException;
 import org.eclipse.core.databinding.observable.list.IObservableList;
 import org.eclipse.jface.databinding.swt.ISWTObservableValue;
 import org.eclipse.jface.databinding.swt.SWTObservables;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Color;
 
 import org.eclipse.riena.ui.ridgets.ICComboRidget;
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.swt.AbstractComboRidget;
 
 /**
  * Ridget for {@link CCombo} widgets.
  */
 public class CComboRidget extends AbstractComboRidget implements ICComboRidget {
 
 	private static final String ORIGINAL_BACKGROUND_KEY = "CCR.orBaKe"; //$NON-NLS-1$
 
 	private final ModifyListener modifyListener;
 
 	public CComboRidget() {
 		modifyListener = new CComboModifyListener();
 		addPropertyChangeListener(IRidget.PROPERTY_ENABLED, new PropertyChangeListener() {
 			public void propertyChange(final PropertyChangeEvent evt) {
 				if (getUIControl() != null) {
 					final boolean isEnabled = ((Boolean) evt.getNewValue()).booleanValue();
 					updateBgColor(isEnabled);
 				}
 			}
 		});
 
 	}
 
 	@Override
 	public CCombo getUIControl() {
 		return (CCombo) super.getUIControl();
 	}
 
 	@Override
 	protected void addSelectionListener(final org.eclipse.swt.events.SelectionListener listener) {
 		getUIControl().addSelectionListener(listener);
 	}
 
 	@Override
 	protected void addTextModifyListener() {
 		getUIControl().addModifyListener(modifyListener);
 	}
 
 	@Override
 	protected void bindUIControl() {
 		super.bindUIControl();
 		if (getUIControl() != null) {
 			updateBgColor(isEnabled());
 		}
 	}
 
 	@Override
 	protected void checkUIControl(final Object uiControl) {
 		checkType(uiControl, CCombo.class);
 		if (uiControl != null) {
 			final int style = ((CCombo) uiControl).getStyle();
 			if ((style & SWT.READ_ONLY) == 0) {
 				throw new BindingException("Combo must be READ_ONLY"); //$NON-NLS-1$
 			}
 		}
 	}
 
 	@Override
 	protected void clearUIControlListSelection() {
 		getUIControl().deselectAll();
 		// Workaround for an SWT feature: when the user clicks in the list,
 		// an asynchronous selection event is added to the end of the event 
 		// queue, which will silenty an item. We asynchronously clear the list 
 		// as well
 		getUIControl().getDisplay().asyncExec(new Runnable() {
 			public void run() {
 				final CCombo combo = getUIControl();
 				if (combo != null && !combo.isDisposed()) {
 					combo.clearSelection(); // this does not change the text
 				}
 			}
 		});
 	}
 
 	@Override
	public void setSelection(final Object newSelection) {
		// Workaround for Bug 372221
		// we put this call to the end of the event queue since
		// there may be other selection events pending
		if (getUIControl() != null) {
			getUIControl().getDisplay().asyncExec(new Runnable() {
				public void run() {
					superSetSelection(newSelection);
				}
			});
		} else {
			superSetSelection(newSelection);
		}
	}

	private void superSetSelection(final Object newSelection) {
		super.setSelection(newSelection);
	}

	@Override
 	protected String[] getUIControlItems() {
 		return getUIControl().getItems();
 	}
 
 	@Override
 	protected IObservableList getUIControlItemsObservable() {
 		return SWTObservables.observeItems(getUIControl());
 	}
 
 	@Override
 	protected ISWTObservableValue getUIControlSelectionObservable() {
 		return SWTObservables.observeSelection(getUIControl());
 	}
 
 	@Override
 	protected String getUIControlText() {
 		return getUIControl().getText();
 	}
 
 	@Override
 	protected int indexOfInUIControl(final String item) {
 		return getUIControl().indexOf(item);
 	}
 
 	@Override
 	protected void removeAllFromUIControl() {
 		getUIControl().removeAll();
 	}
 
 	@Override
 	protected void removeSelectionListener(final SelectionListener listener) {
 		getUIControl().removeSelectionListener(listener);
 	}
 
 	@Override
 	protected void removeTextModifyListener() {
 		getUIControl().removeModifyListener(modifyListener);
 	}
 
 	@Override
 	protected void selectInUIControl(final int index) {
 		getUIControl().select(index);
 	}
 
 	@Override
 	protected void setItemsToControl(final String[] arrItems) {
 		getUIControl().setItems(arrItems);
 	}
 
 	@Override
 	protected void setTextToControl(final String text) {
 		getUIControl().setText(text);
 	}
 
 	@Override
 	protected void updateEditable() {
 		// unused
 	}
 
 	// helping methods
 	//////////////////
 
 	/**
 	 * This was introduced as to address Bug 323449.
 	 * <p>
	 * The Text/CCombo widgets will not change their background to gray when disabled, once a non-null background color has been used in the enabled state.
 	 */
 	private void updateBgColor(final boolean isEnabled) {
 		// pre-condition: control is known to be != null
 		final CCombo control = getUIControl();
 		if (isEnabled) {
 			if (control.getData(ORIGINAL_BACKGROUND_KEY) != null) {
 				control.setBackground((Color) control.getData(ORIGINAL_BACKGROUND_KEY));
 				control.setData(ORIGINAL_BACKGROUND_KEY, null);
 			}
 		} else {
 			if (control.getData(ORIGINAL_BACKGROUND_KEY) == null) {
 				control.setData(ORIGINAL_BACKGROUND_KEY, control.getBackground());
 			}
 			final Color disabledBg = control.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
 			// note: the DisabledPainter will paint the DISABLED_MARKER_BACKGROUND over this, so we are good
 			control.setBackground(disabledBg);
 		}
 		updateMarkers();
 	}
 
 	// helping classes
 	//////////////////
 
 	/**
 	 * Keeps the ridget's text value in-sync with the widget's text value
 	 */
 	private final class CComboModifyListener implements ModifyListener {
 		public void modifyText(final ModifyEvent e) {
 			if (!isOutputOnly()) {
 				setText(getUIControlText());
 			}
 		}
 	}
 }
