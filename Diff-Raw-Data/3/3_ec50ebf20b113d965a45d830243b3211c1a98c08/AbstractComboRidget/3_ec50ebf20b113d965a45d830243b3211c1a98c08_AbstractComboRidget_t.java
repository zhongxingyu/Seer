 /*******************************************************************************
  * Copyright (c) 2007, 2010 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.ridgets.swt;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.databinding.Binding;
 import org.eclipse.core.databinding.BindingException;
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.core.databinding.UpdateListStrategy;
 import org.eclipse.core.databinding.UpdateValueStrategy;
 import org.eclipse.core.databinding.beans.BeansObservables;
 import org.eclipse.core.databinding.beans.PojoObservables;
 import org.eclipse.core.databinding.conversion.Converter;
 import org.eclipse.core.databinding.conversion.IConverter;
 import org.eclipse.core.databinding.observable.list.IObservableList;
 import org.eclipse.core.databinding.observable.list.WritableList;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.core.databinding.observable.value.IValueChangeListener;
 import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
 import org.eclipse.core.databinding.observable.value.WritableValue;
 import org.eclipse.core.databinding.validation.IValidator;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.databinding.swt.ISWTObservableValue;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Widget;
 
 import org.eclipse.riena.core.util.ListenerList;
 import org.eclipse.riena.core.util.ReflectionUtils;
 import org.eclipse.riena.core.util.StringUtils;
 import org.eclipse.riena.ui.core.marker.ErrorMarker;
 import org.eclipse.riena.ui.core.marker.ErrorMessageMarker;
 import org.eclipse.riena.ui.ridgets.IColumnFormatter;
 import org.eclipse.riena.ui.ridgets.IComboRidget;
 import org.eclipse.riena.ui.ridgets.IMarkableRidget;
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.listener.ISelectionListener;
 import org.eclipse.riena.ui.ridgets.listener.SelectionEvent;
 import org.eclipse.riena.ui.ridgets.swt.nls.Messages;
 import org.eclipse.riena.ui.swt.CompletionCombo;
 
 /**
  * Superclass of ComboRidget that does not depend on the Combo SWT control. May
  * be reused for custom Combo controls.
  */
 public abstract class AbstractComboRidget extends AbstractSWTRidget implements IComboRidget {
 	/**
 	 * List of available options (ridget).
 	 * 
 	 * @since 3.0
 	 */
 	protected final IObservableList rowObservables;
 	/**
 	 * The selected option (ridget).
 	 * 
 	 * @since 3.0
 	 */
 	protected final IObservableValue selectionObservable;
 	/** Selection validator that allows or cancels a selection request. */
 	private final SelectionBindingValidator selectionValidator;
 	/**
 	 * Listener that can undo a user's selection if the ridget is in 'readOnly'
 	 * mode.
 	 */
 	private final SelectionEnforcer selectionEnforcer;
 	/** IValueChangeListener that fires a selection event on change. */
 	private final IValueChangeListener valueChangeNotifier;
 	/** A list of selection listeners. */
 	private ListenerList<ISelectionListener> selectionListeners;
 
 	/** If this item is selected, treat it as if nothing is selected */
 	private Object emptySelection;
 
 	/** List of available options (model). */
 	private IObservableList optionValues;
 	/** Class of the optional values. */
 	private Class<? extends Object> rowClass;
 	/** The selected option (model). */
 	private IObservableValue selectionValue;
 	/**
 	 * Optional IColumnFormatter providing an Image or Text for each model
 	 * entry.
 	 */
 	private IColumnFormatter formatter;
 	/** A string used for converting from Object to String */
 	private String renderingMethod;
 	/**
 	 * Converts from objects (rowObsservables) to strings (Combo) using the
 	 * renderingMethod.
 	 */
 	private IConverter objToStrConverter;
 	/**
 	 * Converts from strings (Combo) to objects (rowObservables).
 	 */
 	private IConverter strToObjConverter;
 	/**
 	 * The list of items to show in the combo. These entries are created from
 	 * the optionValues by applying the current conversion stategy to them.
 	 */
 	private List<String> items;
 	/**
 	 * Binding between the rowObservables and the list of choices from the
 	 * model. May be null, when there is no model.
 	 */
 	private Binding listBindingExternal;
 	/**
 	 * Binding between the selection in the combo and the selectionObservable.
 	 * May be null, when there is no control or model.
 	 */
 	private Binding selectionBindingInternal;
 	/**
 	 * Binding between the selectionObservable and the selection in the model.
 	 * May be null, when there is no model.
 	 */
 	private Binding selectionBindingExternal;
 	/**
 	 * If true, it will cause an error marker to be shown, once the selected
 	 * value in the combo is no longer available in the list of selectable
 	 * values. This can occur if the selection was removed from the bound model
 	 * and {@link #updateFromModel()} is called.
 	 * <p>
 	 * The default setting is false.
 	 * 
 	 * @see Bug 304733
 	 */
 	private boolean markSelectionMismatch;
 	/**
 	 * The {@link ErrorMarker} used when a 'selection mismatch' occurs
 	 */
 	private ErrorMarker selectionMismatchMarker;
 	/**
 	 * The text value shown in the combo. Note: this is not necessarily a valid
 	 * selection. Use {@link #getSelection()} to get the current selection.
 	 */
 	private String text;
 
 	public AbstractComboRidget() {
 		rowClass = null;
 		rowObservables = new WritableList();
 		selectionObservable = new WritableValue();
 		objToStrConverter = new ObjectToStringConverter();
 		strToObjConverter = new StringToObjectConverter();
 		selectionValidator = new SelectionBindingValidator();
 		selectionEnforcer = new SelectionEnforcer();
 		valueChangeNotifier = new ValueChangeNotifier();
 		text = ""; //$NON-NLS-1$
 		addPropertyChangeListener(IRidget.PROPERTY_ENABLED, new PropertyChangeListener() {
 			public void propertyChange(final PropertyChangeEvent evt) {
 				applyEnabled();
 			}
 		});
 		addPropertyChangeListener(IMarkableRidget.PROPERTY_OUTPUT_ONLY, new PropertyChangeListener() {
 			public void propertyChange(final PropertyChangeEvent evt) {
 				if (getUIControl() != null) {
 					updateEditable();
 				}
 			}
 		});
 	}
 
 	@Override
 	protected void bindUIControl() {
 		if (getUIControl() != null) {
 			applyText();
 			addTextModifyListener();
 			addSelectionListener(selectionEnforcer);
 			updateEditable();
 		}
 		if (optionValues != null) {
 			// These bindings are only necessary when we have a model
 			final DataBindingContext dbc = new DataBindingContext();
 			if (getUIControl() != null) {
 				applyEnabled();
 			}
 			listBindingExternal = dbc
 					.bindList(rowObservables, optionValues,
 							new UpdateListStrategy(UpdateListStrategy.POLICY_ON_REQUEST), new UpdateListStrategy(
 									UpdateListStrategy.POLICY_ON_REQUEST));
 			selectionBindingExternal = dbc.bindValue(selectionObservable, selectionValue, new UpdateValueStrategy(
 					UpdateValueStrategy.POLICY_UPDATE).setAfterGetValidator(selectionValidator),
 					new UpdateValueStrategy(UpdateValueStrategy.POLICY_ON_REQUEST));
 			// Ensure valueChangeNotifier is not added more that once. 
 			selectionObservable.removeValueChangeListener(valueChangeNotifier);
 			// We have to add the notifier after installing selectionBindingExternal,   
 			// to guarantee that the binding updates the selection value before 
 			// the valueChangeNotifier sends the selection changed event (bug 287740)
 			selectionObservable.addValueChangeListener(valueChangeNotifier);
 		}
 		selectionEnforcer.saveSelection();
 	}
 
 	@Override
 	protected void unbindUIControl() {
 		super.unbindUIControl();
 		if (getUIControl() != null) {
 			removeTextModifyListener();
 			removeSelectionListener(selectionEnforcer);
 		}
 		disposeBinding(listBindingExternal);
 		listBindingExternal = null;
 		disposeBinding(selectionBindingInternal);
 		selectionBindingInternal = null;
 		disposeBinding(selectionBindingExternal);
 		selectionBindingExternal = null;
 	}
 
 	/**
 	 * Provides access to an optional {@link IColumnFormatter} that provides an
 	 * Image or Text for each model entry of the Combo.
 	 * 
 	 * @return an {@link IColumnFormatter} or null (if not set)
 	 * 
 	 * @since 3.0
 	 */
 	protected final IColumnFormatter getColumnFormatter() {
 		return formatter;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * <p>
 	 * Implementation note: the {@link ISelectionListener} will receive a list
 	 * with the selected values. Since the combo only supports a single
 	 * selection, the value will be the one element in the list. If there is no
 	 * selection or the 'empty' selection entry is selected, the list will be
 	 * empty.
 	 */
 	public void addSelectionListener(final ISelectionListener selectionListener) {
 		Assert.isNotNull(selectionListener, "selectionListener is null"); //$NON-NLS-1$
 		if (selectionListeners == null) {
 			selectionListeners = new ListenerList<ISelectionListener>(ISelectionListener.class);
 			addPropertyChangeListener(IComboRidget.PROPERTY_SELECTION, new PropertyChangeListener() {
 				public void propertyChange(final PropertyChangeEvent evt) {
 					notifySelectionListeners(evt.getOldValue(), evt.getNewValue());
 				}
 			});
 		}
 		selectionListeners.add(selectionListener);
 	}
 
 	public void removeSelectionListener(final ISelectionListener selectionListener) {
 		if (selectionListeners != null) {
 			selectionListeners.remove(selectionListener);
 		}
 	}
 
 	public void bindToModel(final IObservableList optionValues, final Class<? extends Object> rowClass,
 			final String renderingMethod, final IObservableValue selectionValue) {
 		unbindUIControl();
 
 		this.optionValues = optionValues;
 		this.rowClass = rowClass;
 		this.renderingMethod = renderingMethod;
 		this.selectionValue = selectionValue;
 
 		bindUIControl();
 	}
 
 	public void bindToModel(final Object listHolder, final String listPropertyName,
 			final Class<? extends Object> rowClass, final String renderingMethod, final Object selectionHolder,
 			final String selectionPropertyName) {
 		IObservableList listObservableValue;
 		if (AbstractSWTWidgetRidget.isBean(rowClass)) {
 			listObservableValue = BeansObservables.observeList(listHolder, listPropertyName);
 		} else {
 			listObservableValue = PojoObservables.observeList(listHolder, listPropertyName);
 		}
 		IObservableValue selectionObservableValue;
 		if (AbstractSWTWidgetRidget.isBean(selectionHolder.getClass())) {
 			selectionObservableValue = BeansObservables.observeValue(selectionHolder, selectionPropertyName);
 		} else {
 			selectionObservableValue = PojoObservables.observeValue(selectionHolder, selectionPropertyName);
 		}
 		bindToModel(listObservableValue, rowClass, renderingMethod, selectionObservableValue);
 	}
 
 	public Object getEmptySelectionItem() {
 		return emptySelection;
 	}
 
 	// TODO [ev] should method return null when not bound? See ListRidget#getObservableList()
 	public IObservableList getObservableList() {
 		return rowObservables;
 	}
 
 	public String getText() {
 		return text;
 	}
 
 	public Object getSelection() {
 		final Object selection = selectionObservable.getValue();
 		return selection == emptySelection ? null : selection;
 	}
 
 	public int getSelectionIndex() {
 		int result = -1;
 		final Object selection = selectionObservable.getValue();
 		if (emptySelection != selection) {
 			result = rowObservables.indexOf(selection);
 		}
 		return result;
 	}
 
 	@Override
 	public boolean isDisableMandatoryMarker() {
 		return hasInput();
 	}
 
 	public boolean isMarkSelectionMismatch() {
 		return markSelectionMismatch;
 	}
 
 	/**
 	 * @since 3.0
 	 */
 	public void setColumnFormatter(final IColumnFormatter formatter) {
 		this.formatter = formatter;
 	}
 
 	public void setEmptySelectionItem(final Object emptySelection) {
 		this.emptySelection = emptySelection;
 	}
 
 	public void setMarkSelectionMismatch(final boolean mark) {
 		if (mark != markSelectionMismatch) {
 			if (mark && selectionMismatchMarker == null) {
 				selectionMismatchMarker = new ErrorMessageMarker(
 						Messages.AbstractComboRidget_markerMessage_selectionMismatch);
 			}
 			markSelectionMismatch = mark;
 			applyMarkSelectionMismatch();
 		}
 	}
 
 	public void setSelection(final Object newSelection) {
 		assertIsBoundToModel();
 		final Object oldSelection = selectionObservable.getValue();
 		if (oldSelection != newSelection) {
 			if (newSelection == null || !rowObservables.contains(newSelection)) {
 				if (getUIControl() != null) {
 					clearUIControlListSelection();
 				}
 			}
 			selectionObservable.setValue(newSelection);
 			selectionEnforcer.saveSelection();
 		}
 	}
 
 	public void setSelection(final int index) {
 		if (index == -1) {
 			setSelection(null);
 		} else {
 			final Object newSelection = rowObservables.get(index);
 			setSelection(newSelection);
 		}
 	}
 
 	public void setText(final String text) {
 		Assert.isNotNull(text);
 		if (!StringUtils.equals(text, this.text)) {
 			final String oldText = this.text;
 			this.text = text;
 			firePropertyChange(PROPERTY_TEXT, oldText, this.text);
 			applyText();
 		}
 	}
 
 	public void setModelToUIControlConverter(final IConverter converter) {
 		objToStrConverter = (converter != null) ? converter : new ObjectToStringConverter();
 	}
 
 	public void setUIControlToModelConverter(final IConverter converter) {
 		strToObjConverter = (converter != null) ? converter : new StringToObjectConverter();
 	}
 
 	@Override
 	public void updateFromModel() {
 		// dont do anything if no model is bound
 		if (optionValues == null) {
 			return;
 		}
 		assertIsBoundToModel();
 		super.updateFromModel();
 		// disable the selection binding, because updating the combo items
 		// causes the selection to change temporarily
 		selectionValidator.enableBinding(false);
 		try {
 			listBindingExternal.updateModelToTarget();
 			items = new ArrayList<String>();
 			updateValueToItem();
 		} finally {
 			selectionValidator.enableBinding(true);
 		}
 		selectionBindingExternal.updateModelToTarget();
 		if (selectionBindingInternal != null) {
 			selectionBindingInternal.updateModelToTarget();
 		}
 		// Bug 304733: clear selection if not in rowObservables
 		applyMarkSelectionMismatch();
 		selectionEnforcer.saveSelection();
 	}
 
 	/**
 	 * @since 3.0
 	 */
 	protected void assertIsBoundToModel() {
 		if (optionValues == null) {
			final String msg = String.format("ridget with ID '%s' is not bound to  a model", getID()); //$NON-NLS-1$
			throw new BindingException(msg);
 		}
 	}
 
 	// abstract methods
 	///////////////////
 
 	/**
 	 * Attach a given {@link SelectionListener} to the combo.
 	 * 
 	 * @param listener
 	 *            {@link SelectionListener}; never null.
 	 * @since 3.0
 	 */
 	protected abstract void addSelectionListener(SelectionListener listener);
 
 	/**
 	 * Attach a text modify listener to the combo. The listener must invoke
 	 * {@code ridget.setText(...)} if the control's text is modified.
 	 */
 	protected abstract void addTextModifyListener();
 
 	/**
 	 * Deselects all selected items in the controls list.
 	 */
 	protected abstract void clearUIControlListSelection();
 
 	/**
 	 * @return The items of the controls list. May be an empty array.
 	 */
 	protected abstract String[] getUIControlItems();
 
 	/**
 	 * @return an observable observing the items attribute of the control.
 	 */
 	protected abstract IObservableList getUIControlItemsObservable();
 
 	/**
 	 * @return an observable observing the selection attribute of the control.
 	 */
 	protected abstract ISWTObservableValue getUIControlSelectionObservable();
 
 	/**
 	 * Return the current text in the combo.
 	 * 
 	 * @return a String; never null; may be empty
 	 * @since 2.0
 	 */
 	protected abstract String getUIControlText();
 
 	/**
 	 * Selects the item in the controls list.
 	 */
 	protected abstract void selectInUIControl(int index);
 
 	/**
 	 * @return The index of the item in the controls list or -1 if no such item
 	 *         is found.
 	 */
 	protected abstract int indexOfInUIControl(String item);
 
 	/**
 	 * Removes all of the items from the controls list and clears the controls
 	 * text field.
 	 */
 	protected abstract void removeAllFromUIControl();
 
 	/**
 	 * Remove a given {@link SelectionListener} from the combo.
 	 * 
 	 * @param listener
 	 *            the {@link SelectionListener}; never null.
 	 * @since 3.0
 	 */
 	protected abstract void removeSelectionListener(SelectionListener listener);
 
 	/**
 	 * Remove the text modify listener from the combo.
 	 * 
 	 * @see #addTextModifyListener()
 	 * @since 2.0
 	 */
 	protected abstract void removeTextModifyListener();
 
 	/**
 	 * Make the given array the list of selectable items in the combo.
 	 * 
 	 * @param arrItems
 	 *            an array; never null.
 	 * @since 1.2
 	 */
 	protected abstract void setItemsToControl(String[] arrItems);
 
 	/**
 	 * Set the given {@code text} to the combo.
 	 * 
 	 * @param text
 	 *            a String; never null; may be empty
 	 * @since 2.0
 	 */
 	protected abstract void setTextToControl(String text);
 
 	/**
 	 * Updates the editable state of the ridget's control.
 	 * <p>
 	 * <b>Implementation note</b>: This method is invoked when
 	 * <ul>
 	 * <li>a control is bound to the ridget</li>
 	 * <li>the 'output only' or 'enabled' markers change state. For some combo
 	 * widgets, not editable implies disabled so the two states overlap.</li>
 	 * </ul>
 	 * 
 	 * @since 3.0
 	 */
 	protected abstract void updateEditable();
 
 	// helping methods
 	//////////////////
 
 	private void applyEnabled() {
 		if (super.isEnabled()) {
 			bindControlToSelectionAndUpdate();
 		} else {
 			unbindControlFromSelectionAndClear();
 		}
 	}
 
 	private void applyText() {
 		if (getUIControl() != null) {
 			if (!StringUtils.equals(text, getUIControlText())) {
 				setTextToControl(text);
 			}
 		}
 	}
 
 	private void applyMarkSelectionMismatch() {
 		final Object selection = selectionObservable.getValue();
 		if (markSelectionMismatch && selection != null && !rowObservables.contains(selection)) {
 			Assert.isNotNull(markSelectionMismatch);
 			addMarker(selectionMismatchMarker);
 		} else {
 			if (selectionMismatchMarker != null) {
 				removeMarker(selectionMismatchMarker);
 			}
 		}
 	}
 
 	/**
 	 * Restores the list of items / selection in the combo, when the ridget is
 	 * enabled.
 	 */
 	private void bindControlToSelectionAndUpdate() {
 		if (getUIControl() != null) {
 			/* update list of items in combo */
 			updateValueToItem();
 			/* re-create selectionBinding */
 			final ISWTObservableValue controlSelection = getUIControlSelectionObservable();
 			final UpdateValueStrategy controlSelectionBindingStrategy = new UpdateValueStrategy(
 					UpdateValueStrategy.POLICY_UPDATE);
 			controlSelectionBindingStrategy.setConverter(strToObjConverter).setAfterGetValidator(selectionValidator);
 			controlSelectionBindingStrategy.setBeforeSetValidator(new IValidator() {
 
 				public IStatus validate(final Object value) {
 					if (isOutputOnly()) {
 						return Status.CANCEL_STATUS;
 					}
 					return Status.OK_STATUS;
 				}
 			});
 			final DataBindingContext dbc = new DataBindingContext();
 			selectionBindingInternal = dbc.bindValue(controlSelection, selectionObservable,
 					controlSelectionBindingStrategy,
 					new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE).setConverter(objToStrConverter));
 			/* update selection in combo */
 			selectionBindingInternal.updateModelToTarget();
 		}
 	}
 
 	private void disposeBinding(final Binding binding) {
 		if (binding != null && !binding.isDisposed()) {
 			binding.dispose();
 		}
 	}
 
 	private String getItemFromValue(final Object value) {
 		Object valueObject = value;
 		if (valueObject != null && formatter != null) {
 			valueObject = formatter.getText(value);
 		}
 		if (valueObject != null && renderingMethod != null) {
 			valueObject = ReflectionUtils.invoke(value, renderingMethod, (Object[]) null);
 		}
 		String result;
 		if (valueObject == null || valueObject.toString() == null) {
 			result = ""; //$NON-NLS-1$
 		} else {
 			result = valueObject.toString();
 		}
 		return result;
 	}
 
 	/**
 	 * Returns the value of the given item.
 	 * 
 	 * @param item
 	 *            item of combo box
 	 * @return value relevant object; {@code null} or empty string if no
 	 *         relevant object exists
 	 */
 	private Object getValueFromItem(final String item) {
 
 		final String[] uiItems = getUIControlItems();
 		for (int i = 0; i < uiItems.length; i++) {
 			if (uiItems[i].equals(item)) {
 				return rowObservables.get(i);
 			}
 		}
 
 		if (rowClass == String.class) {
 			return ""; //$NON-NLS-1$
 		} else {
 			return null;
 		}
 
 	}
 
 	private boolean hasInput() {
 		final Object selection = selectionObservable.getValue();
 		return selection != null && selection != emptySelection;
 	}
 
 	private void notifySelectionListeners(final Object oldValue, final Object newValue) {
 		if (selectionListeners != null) {
 			final List<Object> oldSelectionList = new ArrayList<Object>();
 			if (oldValue != null) {
 				oldSelectionList.add(oldValue);
 			}
 			final List<Object> newSelectionList = new ArrayList<Object>();
 			if (newValue != null) {
 				newSelectionList.add(newValue);
 			}
 			final SelectionEvent event = new SelectionEvent(this, oldSelectionList, newSelectionList);
 			for (final ISelectionListener listener : selectionListeners.getListeners()) {
 				listener.ridgetSelected(event);
 			}
 		}
 	}
 
 	/**
 	 * Clears the list of items in the combo, when the ridget is disabled.
 	 */
 	private void unbindControlFromSelectionAndClear() {
 		if (getUIControl() != null && !super.isEnabled()) {
 			/* dispose selectionBinding to avoid sync */
 			disposeBinding(selectionBindingInternal);
 			selectionBindingInternal = null;
 			/* clear combo */
 			if (MarkerSupport.isHideDisabledRidgetContent()) {
 				removeAllFromUIControl();
 			}
 		}
 	}
 
 	private void updateValueToItem() {
 		if (items != null) {
 			items.clear();
 			try {
 				for (final Object value : rowObservables) {
 					if (value == null || value.toString() == null) {
 						throw new NullPointerException("The item value for a model element is null"); //$NON-NLS-1$
 					}
 					final String item = (String) objToStrConverter.convert(value);
 					items.add(item);
 				}
 			} finally {
 				if (getUIControl() != null) {
 					final String[] arrItems = items.toArray(new String[items.size()]);
 					setItemsToControl(arrItems);
 				}
 			}
 		}
 	}
 
 	// helping classes
 	//////////////////
 
 	/**
 	 * Convert from model object to combo box items (strings).
 	 */
 	private final class ObjectToStringConverter extends Converter {
 		public ObjectToStringConverter() {
 			super(Object.class, String.class);
 		}
 
 		public Object convert(final Object fromObject) {
 			return getItemFromValue(fromObject);
 		}
 	}
 
 	/**
 	 * Convert from combo box items (strings) to model objects.
 	 */
 	private final class StringToObjectConverter extends Converter {
 		public StringToObjectConverter() {
 			super(String.class, Object.class);
 		}
 
 		public Object convert(final Object fromObject) {
 			return getValueFromItem((String) fromObject);
 		}
 	}
 
 	/**
 	 * This validator can be used to interrupt an update request
 	 */
 	private final class SelectionBindingValidator implements IValidator {
 
 		private boolean isEnabled = true;
 
 		public IStatus validate(final Object value) {
 			IStatus result = Status.OK_STATUS;
 			// disallow control to ridget update, isEnabled == false || output
 			if (!isEnabled) {
 				result = Status.CANCEL_STATUS;
 			}
 			return result;
 		}
 
 		void enableBinding(final boolean isEnabled) {
 			this.isEnabled = isEnabled;
 		}
 	}
 
 	/**
 	 * Upon a selection change:
 	 * <ul>
 	 * <li>fire a PROPERTY_SELECTION event and</li>
 	 * <li>update the mandatory marker state</li>
 	 * </ul>
 	 */
 	private final class ValueChangeNotifier implements IValueChangeListener {
 		public void handleValueChange(final ValueChangeEvent event) {
 			final Object oldValue = event.diff.getOldValue();
 			final Object newValue = event.diff.getNewValue();
 			try {
 				firePropertyChange(IComboRidget.PROPERTY_SELECTION, oldValue, newValue);
 			} finally {
 				disableMandatoryMarkers(hasInput());
 				applyMarkSelectionMismatch();
 			}
 		}
 	}
 
 	/**
 	 * TwoWayAdapter that saves the current selection, when outputOnly changes
 	 * and applies it again after the user tries to select an entry in the
 	 * combo.
 	 * 
 	 * @since 3.0
 	 */
 	private final class SelectionEnforcer extends SelectionAdapter {
 
 		private int selectionIndex;
 
 		@Override
 		public void widgetSelected(final org.eclipse.swt.events.SelectionEvent e) {
 			if (getUIControl() != null && isOutputOnly()) {
 				final Widget uiControl = e.widget;
 				resetSelection(uiControl);
 			}
 		}
 
 		/**
 		 * Save the currently selected value of the Combo.
 		 */
 		public void saveSelection() {
 			if (isBound()) {
 				selectionIndex = getSelectionIndex();
 				// System.out.println("## saveSelection; selectionIndex: " + selectionIndex);
 			}
 		}
 
 		private boolean isBound() {
 			return listBindingExternal != null && selectionBindingExternal != null;
 		}
 
 		private void resetSelection(final Widget uiControl) {
 			// System.out.println("## propertyChange; resetSel: " + selectionIndex); 
 			if (uiControl instanceof CCombo) {
 				if (selectionIndex == -1) {
 					((CCombo) uiControl).deselectAll();
 				} else {
 					((CCombo) uiControl).select(selectionIndex);
 				}
 			} else if (uiControl instanceof Combo) {
 				if (selectionIndex == -1) {
 					((Combo) uiControl).deselectAll();
 				} else {
 					((Combo) uiControl).select(selectionIndex);
 				}
 			} else if (uiControl instanceof CompletionCombo) {
 				if (selectionIndex == -1) {
 					((CompletionCombo) uiControl).deselectAll();
 				} else {
 					((CompletionCombo) uiControl).select(selectionIndex);
 				}
 			}
 		}
 	}
 }
