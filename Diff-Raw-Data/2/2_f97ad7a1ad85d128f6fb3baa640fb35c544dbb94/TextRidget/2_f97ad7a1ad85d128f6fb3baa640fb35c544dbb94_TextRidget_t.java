 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
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
 
 import org.eclipse.core.databinding.beans.BeansObservables;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.riena.ui.ridgets.IMarkableRidget;
 import org.eclipse.riena.ui.ridgets.ITextRidget;
 import org.eclipse.riena.ui.ridgets.ValueBindingSupport;
 import org.eclipse.riena.ui.ridgets.validation.IValidationRuleStatus;
 import org.eclipse.riena.ui.ridgets.validation.ValidationRuleStatus;
 import org.eclipse.riena.ui.ridgets.validation.ValidatorCollection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.VerifyEvent;
 import org.eclipse.swt.events.VerifyListener;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Text;
 
 /**
  * Ridget for an SWT <code>Text</code> widget.
  */
 public class TextRidget extends AbstractEditableRidget implements ITextRidget {
 
 	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
 
 	private final FocusListener focusListener;
 	private final KeyListener crKeyListener;
 	private final ModifyListener modifyListener;
 	private final ValidationListener verifyListener;
 	private String textValue = EMPTY_STRING;
 	private boolean isDirectWriting;
 
 	public TextRidget() {
 		crKeyListener = new CRKeyListener();
 		focusListener = new FocusManager();
 		modifyListener = new SyncModifyListener();
 		verifyListener = new ValidationListener();
 		isDirectWriting = false;
 		addPropertyChangeListener(IMarkableRidget.PROPERTY_ENABLED, new PropertyChangeListener() {
 			public void propertyChange(PropertyChangeEvent evt) {
 				forceTextToControl(getTextInternal());
 			}
 		});
 		addPropertyChangeListener(IMarkableRidget.PROPERTY_OUTPUT_ONLY, new PropertyChangeListener() {
 			public void propertyChange(PropertyChangeEvent evt) {
 				Text control = getUIControl();
 				if (control != null && !control.isDisposed()) {
 					control.setEditable(isOutputOnly() ? false : true);
 				}
 			}
 		});
 	}
 
 	protected TextRidget(String initialValue) {
 		this();
 		Assert.isNotNull(initialValue);
 		textValue = initialValue;
 	}
 
 	/**
 	 * @deprecated use BeansObservables.observeValue(ridget instance,
 	 *             ITextRidget.PROPERTY_TEXT);
 	 */
 	public IObservableValue getRidgetObservable() {
 		return BeansObservables.observeValue(this, ITextRidget.PROPERTY_TEXT);
 	}
 
 	@Override
 	protected void checkUIControl(Object uiControl) {
 		AbstractSWTRidget.assertType(uiControl, Text.class);
 	}
 
 	protected boolean isNotEmpty(String input) {
 		return input.length() > 0;
 	}
 
 	@Override
 	protected final synchronized void bindUIControl() {
 		Text control = getUIControl();
 		if (control != null) {
 			control.setText(textValue);
 			control.setSelection(0, 0); // move cursor to 0
 			control.setEditable(isOutputOnly() ? false : true);
 			addListeners(control);
 		}
 	}
 
 	@Override
 	protected final synchronized void unbindUIControl() {
 		Text control = getUIControl();
 		if (control != null) {
 			removeListeners(control);
 		}
 	}
 
 	/**
 	 * Template method for adding listeners to the control. Will be called
 	 * automatically. Children can overide but must call super.
 	 * 
 	 * @param control
 	 *            a Text instance (never null)
 	 */
 	protected synchronized void addListeners(Text control) {
 		control.addKeyListener(crKeyListener);
 		control.addFocusListener(focusListener);
 		control.addModifyListener(modifyListener);
 		control.addVerifyListener(verifyListener);
 	}
 
 	/**
 	 * Template method for removing listeners from the control. Will be called
 	 * automatically. Children can overide but must call super.
 	 * 
 	 * @param control
 	 *            a Text instance (never null)
 	 */
 	protected synchronized void removeListeners(Text control) {
 		control.removeKeyListener(crKeyListener);
 		control.removeFocusListener(focusListener);
 		control.removeModifyListener(modifyListener);
 		control.removeVerifyListener(verifyListener);
 	}
 
 	@Override
 	public Text getUIControl() {
 		return (Text) super.getUIControl();
 	}
 
 	public synchronized String getText() {
 		return textValue;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * <p>
 	 * Invoking this method will copy the given text into the ridget and the
 	 * widget regardless of the validation outcome. If the text does not pass
 	 * validation the error marker will be set and the text will <b>not</b> be
 	 * copied into the model. If validation passes the text will be copied into
 	 * the model as well.
 	 */
 	public synchronized void setText(String text) {
 		Assert.isNotNull(text);
 		String oldValue = textValue;
 		textValue = text;
 		forceTextToControl(textValue);
 		disableMandatoryMarkers(isNotEmpty(textValue));
 		IStatus onEdit = checkOnEditRules(text);
 		validationRulesCheckedMarkFlash(onEdit);
 		if (onEdit.isOK()) {
 			firePropertyChange(ITextRidget.PROPERTY_TEXT, oldValue, textValue);
 		}
 	}
 
 	public synchronized boolean revalidate() {
 		Text control = getUIControl();
 		String text = control != null ? control.getText() : textValue;
 		textValue = null; // textValue != text in order to propage prop. change
 		setText(text);
 		return !isErrorMarked();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * <p>
 	 * Invoking this method will copy the model value into the ridget and the
 	 * widget regardless of the validation outcome. If the model value does not
 	 * pass validation, the error marker will be set.
 	 */
 	@Override
 	public synchronized void updateFromModel() {
 		super.updateFromModel();
 		IStatus onEdit = checkOnEditRules(textValue);
 		IStatus onUpdate = checkOnUpdateRules(textValue);
 		IStatus joinedStatus = ValidationRuleStatus.join(new IStatus[] { onEdit, onUpdate });
 		validationRulesCheckedMarkFlash(joinedStatus);
 	}
 
 	public synchronized boolean isDirectWriting() {
 		return isDirectWriting;
 	}
 
 	public synchronized void setDirectWriting(boolean directWriting) {
 		if (this.isDirectWriting != directWriting) {
 			this.isDirectWriting = directWriting;
 		}
 	}
 
 	@Override
 	public final boolean isDisableMandatoryMarker() {
 		return isNotEmpty(textValue);
 	}
 
 	public int getAlignment() {
 		// TODO Auto-generated method stub
 		throw new UnsupportedOperationException("not implemented"); //$NON-NLS-1$
 	}
 
 	/**
 	 * In SWT the alignment is set when creating the Text widget and cannot be
 	 * changed afterwards.
 	 * 
 	 * @throw {@link UnsupportedOperationException} when called
 	 */
 	public final void setAlignment(int alignment) {
 		throw new UnsupportedOperationException("not supported"); //$NON-NLS-1$
 	}
 
 	// helping methods
 	// ////////////////
 
 	private IStatus checkOnEditRules(String newValue) {
 		ValueBindingSupport vbs = getValueBindingSupport();
 		ValidatorCollection onEditValidators = vbs.getOnEditValidators();
 		IStatus result = onEditValidators.validate(newValue);
 		return result;
 	}
 
 	private IStatus checkOnUpdateRules(String newValue) {
 		ValueBindingSupport vbs = getValueBindingSupport();
 		ValidatorCollection afterGetValidators = vbs.getAfterGetValidators();
 		IStatus result = afterGetValidators.validate(newValue);
 		return result;
 	}
 
 	private synchronized void forceTextToControl(String newValue) {
 		Text control = getUIControl();
 		if (control != null) {
 			Listener[] listeners = control.getListeners(SWT.Verify);
 			for (Listener listener : listeners) {
 				control.removeListener(SWT.Verify, listener);
 			}
 			boolean hideValue = !isEnabled() && MarkerSupport.HIDE_DISABLED_RIDGET_CONTENT;
 			control.setText(hideValue ? EMPTY_STRING : newValue);
 			control.setSelection(0, 0);
 			for (Listener listener : listeners) {
 				control.addListener(SWT.Verify, listener);
 			}
 		}
 	}
 
 	private synchronized String getTextInternal() {
 		return textValue;
 	}
 
 	/**
 	 * Returns the given status object, without the ERROR_BLOCK_WITH_FLASH
 	 * status code.
 	 */
 	private IStatus suppressBlockWithFlash(IStatus status) {
 		IStatus theStatus;
 		if (status.getCode() == IValidationRuleStatus.ERROR_BLOCK_WITH_FLASH) {
			final int newCode = IValidationRuleStatus.ERROR_ALLOW_WITH_MESSAGE;
 			theStatus = new Status(status.getSeverity(), status.getPlugin(), newCode, status.getMessage(), status
 					.getException());
 		} else {
 			theStatus = status;
 		}
 		return theStatus;
 	}
 
 	private synchronized void updateTextValue() {
 		String oldValue = textValue;
 		String newValue = getUIControl().getText();
 		if (!oldValue.equals(newValue)) {
 			textValue = newValue;
 			if (checkOnEditRules(newValue).isOK()) {
 				firePropertyChange(ITextRidget.PROPERTY_TEXT, oldValue, newValue);
 			}
 		}
 	}
 
 	private synchronized void updateTextValueWhenDirectWriting() {
 		if (isDirectWriting) {
 			updateTextValue();
 		}
 	}
 
 	/*
 	 * This method is called from setText() / updateFromModel() to update the
 	 * validation state of the ridget. Text typed by the user triggers a
 	 * validationRulesChecked(status) call directly - see event listeners below.
 	 */
 	private void validationRulesCheckedMarkFlash(IStatus status) {
 		IStatus newStatus = suppressBlockWithFlash(status);
 		validationRulesChecked(newStatus);
 	}
 
 	// helping classes
 	// ////////////////
 
 	/**
 	 * Update text value in ridget when ENTER is pressed
 	 */
 	private final class CRKeyListener extends KeyAdapter implements KeyListener {
 		@Override
 		public void keyReleased(KeyEvent e) {
 			if (e.character == '\r') {
 				updateTextValue();
 			}
 		}
 	}
 
 	/**
 	 * Manages activities trigger by focus changed:
 	 * <ol>
 	 * <li>select single line text fields, when focus is gained by keyboard</li>
 	 * <li>update text value in ridget, when focus is lost</li>
 	 * <ol>
 	 */
 	private final class FocusManager implements FocusListener {
 		public void focusGained(FocusEvent e) {
 			if (isFocusable()) {
 				Text text = (Text) e.getSource();
 				// if not multi line text field
 				if ((text.getStyle() & SWT.MULTI) == 0) {
 					text.selectAll();
 				}
 			}
 		}
 
 		public void focusLost(FocusEvent e) {
 			updateTextValue();
 		}
 	}
 
 	/**
 	 * Updates the text value in the ridget, if direct writing is enabled.
 	 */
 	private final class SyncModifyListener implements ModifyListener {
 		public void modifyText(ModifyEvent e) {
 			updateTextValueWhenDirectWriting();
 			String text = ((Text) e.widget).getText();
 			disableMandatoryMarkers(isNotEmpty(text));
 		}
 	}
 
 	/**
 	 * Validation listener that checks 'on edit' validation rules when the text
 	 * widget's contents are modified by the user. If the new text value does
 	 * not pass the test and outcome is ERROR_BLOCK_WITH_FLASH, the change will
 	 * be rejected. If the new text passed the test, or fails the test without
 	 * blocking, the value is copied into the ridget. This will fire a proprty
 	 * change event (see {@link TextRidget#setText(String)}) causing the 'on
 	 * update' validation rules to be checked and will copy the value into the
 	 * model if it passes those checks.
 	 */
 	private final class ValidationListener implements VerifyListener {
 
 		public synchronized void verifyText(VerifyEvent e) {
 			if (!e.doit) {
 				return;
 			}
 			String newText = getText(e);
 			IStatus status = checkOnEditRules(newText);
 			boolean doit = !(status.getCode() == IValidationRuleStatus.ERROR_BLOCK_WITH_FLASH);
 			e.doit = doit;
 			validationRulesChecked(status);
 		}
 
 		private String getText(VerifyEvent e) {
 			Text widget = (Text) e.widget;
 			String oldText = widget.getText();
 			String newText;
 			// deletion
 			if (e.keyCode == 127 || e.keyCode == 8) {
 				newText = oldText.substring(0, e.start) + oldText.substring(e.end);
 			} else { // addition / replace
 				newText = oldText.substring(0, e.start) + e.text + oldText.substring(e.end);
 			}
 			return newText;
 		}
 	}
 
 }
