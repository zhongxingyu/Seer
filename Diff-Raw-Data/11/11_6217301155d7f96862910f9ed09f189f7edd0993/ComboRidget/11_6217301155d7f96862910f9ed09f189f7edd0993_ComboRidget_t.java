 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
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
 
 import org.eclipse.core.databinding.Binding;
 import org.eclipse.core.databinding.BindingException;
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.core.databinding.UpdateListStrategy;
 import org.eclipse.core.databinding.UpdateValueStrategy;
 import org.eclipse.core.databinding.beans.BeansObservables;
 import org.eclipse.core.databinding.beans.PojoObservables;
 import org.eclipse.core.databinding.conversion.Converter;
 import org.eclipse.core.databinding.observable.list.IObservableList;
 import org.eclipse.core.databinding.observable.list.WritableList;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.core.databinding.observable.value.IValueChangeListener;
 import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
 import org.eclipse.core.databinding.observable.value.WritableValue;
 import org.eclipse.core.databinding.validation.IValidator;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.databinding.swt.ISWTObservableValue;
 import org.eclipse.jface.databinding.swt.SWTObservables;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Combo;
 
 import org.eclipse.riena.core.util.ReflectionUtils;
 import org.eclipse.riena.ui.common.IComboEntryFactory;
 import org.eclipse.riena.ui.ridgets.IComboRidget;
 import org.eclipse.riena.ui.ridgets.IMarkableRidget;
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.swt.AbstractSWTRidget;
 
 /**
  * Ridget for {@link Combo} widgets.
  */
 public class ComboRidget extends AbstractSWTRidget implements IComboRidget {
 
 	/** List of choices (Objects). */
 	private final IObservableList rowObservables;
 	/** An observable with the current selection. */
 	private final IObservableValue selectionObservable;
 	/**
 	 * Converts from objects (rowObsservables) to strings (Combo) using the
 	 * renderingMethod.
 	 */
 	private final Converter objToStrConverter;
 	/** Convers from strings (Combo) to objects (rowObservables). */
 	private final Converter strToObjConverter;
 	/** Selection validator that allows or cancels a selection request. */
 	private final SelectionBindingValidator selectionValidator;
 	/** IValueChangeListener that allows or cancels a value change */
 	private final IValueChangeListener valueChangeValidator;
 
 	/** If this item is selected, treat it as if nothing is selected */
 	private Object emptySelection;
 
 	/** List observable bean (model) */
 	private IObservableList rowObservablesModel;
 	/** Selection observable bean (model) */
 	private IObservableValue selectionObservableModel;
 	/** A string used for converting from Object to String */
 	private String renderingMethod;
 
 	/**
 	 * Binding between the list of choices in the combo and the rowObservables.
 	 * May be null, when there is no control or model.
 	 */
 	private Binding listBindingInternal;
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
 
 	public ComboRidget() {
 		super();
 		rowObservables = new WritableList();
 		selectionObservable = new WritableValue();
 		selectionObservable.addValueChangeListener(new IValueChangeListener() {
 			public void handleValueChange(ValueChangeEvent event) {
 				Object oldValue = event.diff.getOldValue();
 				Object newValue = event.diff.getNewValue();
 				firePropertyChange(IComboRidget.PROPERTY_SELECTION, oldValue, newValue);
 				disableMandatoryMarkers(hasInput());
 			}
 		});
 		objToStrConverter = new ObjectToStringConverter();
 		strToObjConverter = new StringToObjectConverter();
 		selectionValidator = new SelectionBindingValidator();
 		valueChangeValidator = new ValueChangeValidator();
 		addPropertyChangeListener(IRidget.PROPERTY_ENABLED, new PropertyChangeListener() {
 			public void propertyChange(PropertyChangeEvent evt) {
 				applyEnabled();
 			}
 		});
 	}
 
 	@Override
 	public Combo getUIControl() {
 		return (Combo) super.getUIControl();
 	}
 
 	@Override
 	protected void checkUIControl(Object uiControl) {
 		AbstractSWTRidget.assertType(uiControl, Combo.class);
 		if (uiControl != null) {
 			int style = ((Combo) uiControl).getStyle();
 			if ((style & SWT.READ_ONLY) == 0) {
 				throw new BindingException("Combo must be READ_ONLY"); //$NON-NLS-1$
 			}
 		}
 	}
 
 	@Override
 	protected void bindUIControl() {
 		Combo control = getUIControl();
 		if (rowObservablesModel != null) {
 			// These bindings are only necessary when we have a model
 			DataBindingContext dbc = new DataBindingContext();
 			if (control != null) {
 				// These bindings are only necessary when we have a Combo
 				listBindingInternal = dbc.bindList(SWTObservables.observeItems(control), rowObservables,
						new UpdateListStrategy(UpdateListStrategy.POLICY_ON_REQUEST).setConverter(strToObjConverter),
						new UpdateListStrategy(UpdateValueStrategy.POLICY_ON_REQUEST).setConverter(objToStrConverter));
 				listBindingInternal.updateModelToTarget();
 				applyEnabled();
 			}
			listBindingExternal = dbc
					.bindList(rowObservables, rowObservablesModel, new UpdateListStrategy(
							UpdateListStrategy.POLICY_ON_REQUEST), new UpdateListStrategy(
							UpdateListStrategy.POLICY_ON_REQUEST));
 			selectionBindingExternal = dbc
 					.bindValue(selectionObservable, selectionObservableModel, new UpdateValueStrategy(
 							UpdateValueStrategy.POLICY_UPDATE).setAfterGetValidator(selectionValidator),
 							new UpdateValueStrategy(UpdateValueStrategy.POLICY_ON_REQUEST));
 
 		}
 	}
 
 	@Override
 	protected void unbindUIControl() {
 		disposeBinding(listBindingInternal);
 		listBindingInternal = null;
 		disposeBinding(listBindingExternal);
 		listBindingExternal = null;
 		disposeBinding(selectionBindingInternal);
 		selectionBindingInternal = null;
 		disposeBinding(selectionBindingExternal);
 		selectionBindingExternal = null;
 	}
 
 	@Override
 	public void updateFromModel() {
 		assertIsBoundToModel();
 		super.updateFromModel();
 		// disable the selection binding, because updating the combo items
 		// causes the selection to change temporarily
 		selectionValidator.enableBinding(false);
 		listBindingExternal.updateModelToTarget();
 		if (listBindingInternal != null) {
 			listBindingInternal.updateModelToTarget();
 		}
 		selectionValidator.enableBinding(true);
 		selectionBindingExternal.updateModelToTarget();
 		if (selectionBindingInternal != null) {
 			selectionBindingInternal.updateModelToTarget();
 		}
 	}
 
 	public void bindToModel(IObservableList listObservableValue, Class<? extends Object> rowValueClass,
 			String renderingMethod, IObservableValue selectionObservableValue) {
 		unbindUIControl();
 
 		this.rowObservablesModel = listObservableValue;
 		this.renderingMethod = renderingMethod;
 		this.selectionObservableModel = selectionObservableValue;
 
 		bindUIControl();
 	}
 
 	public void bindToModel(Object listPojo, String listPropertyName, Class<? extends Object> rowValueClass,
 			String renderingMethod, Object selectionPojo, String selectionPropertyName) {
 		IObservableList listObservableValue;
 		if (AbstractSWTWidgetRidget.isBean(rowValueClass)) {
 			listObservableValue = BeansObservables.observeList(listPojo, listPropertyName);
 		} else {
 			listObservableValue = PojoObservables.observeList(listPojo, listPropertyName);
 		}
 		IObservableValue selectionObservableValue = PojoObservables.observeValue(selectionPojo, selectionPropertyName);
 		bindToModel(listObservableValue, rowValueClass, renderingMethod, selectionObservableValue);
 	}
 
 	/**
 	 * @deprecated
 	 */
 	public void bindToModel(Object listPojo, String listPropertyName, Class<? extends Object> rowValueClass,
 			String renderingMethod, Object selectionPojo, String selectionPropertyName, IComboEntryFactory entryFactory) {
 		throw new UnsupportedOperationException();
 	}
 
 	public Object getEmptySelectionItem() {
 		return emptySelection;
 	}
 
 	// TODO [ev] should method return null when not bound? See ListRidget#getObservableList()
 	public IObservableList getObservableList() {
 		return rowObservables;
 	}
 
 	public Object getSelection() {
 		Object selection = selectionObservable.getValue();
 		return selection == emptySelection ? null : selection;
 	}
 
 	public int getSelectionIndex() {
 		int result = -1;
 		Object selection = selectionObservable.getValue();
 		if (emptySelection != selection) {
 			result = rowObservables.indexOf(selection);
 		}
 		return result;
 	}
 
 	public boolean isAddable() {
 		throw new UnsupportedOperationException(); // TODO implement
 	}
 
 	@Override
 	public boolean isDisableMandatoryMarker() {
 		return hasInput();
 	}
 
 	public boolean isListMutable() {
 		throw new UnsupportedOperationException(); // TODO implement
 	}
 
 	public boolean isReadonly() {
 		throw new UnsupportedOperationException(); // TODO implement
 	}
 
 	public void setAddable(boolean addable) {
 		throw new UnsupportedOperationException(); // TODO implement
 	}
 
 	public void setEmptySelectionItem(Object emptySelectionItem) {
 		this.emptySelection = emptySelectionItem;
 	}
 
 	public void setListMutable(boolean mutable) {
 		throw new UnsupportedOperationException(); // TODO implement
 	}
 
 	public void setReadonly(boolean readonly) {
 		throw new UnsupportedOperationException(); // TODO implement
 	}
 
 	public void setSelection(Object newSelection) {
 		assertIsBoundToModel();
 		Object oldSelection = selectionObservable.getValue();
 		if (oldSelection != newSelection) {
 			if (newSelection == null || !rowObservables.contains(newSelection)) {
 				if (getUIControl() != null) {
 					getUIControl().deselectAll();
 				}
 				selectionObservable.setValue(null);
 			} else {
 				selectionObservable.setValue(newSelection);
 			}
 		}
 	}
 
 	public void setSelection(int index) {
 		if (index == -1) {
 			setSelection(null);
 		} else {
 			Object newSelection = rowObservables.get(index);
 			setSelection(newSelection);
 		}
 	}
 
 	// helping methods
 	// ////////////////
 
 	private void applyEnabled() {
 		if (isEnabled()) {
 			bindControlToSelectionAndUpdate();
 		} else {
 			unbindControlFromSelectionAndClear();
 		}
 	}
 
 	private void assertIsBoundToModel() {
 		if (rowObservablesModel == null) {
 			throw new BindingException("ridget not bound to model"); //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * Restores the list of items / selection in the combo, when the ridget is
 	 * enabled.
 	 */
 	private void bindControlToSelectionAndUpdate() {
 		Combo control = getUIControl();
 		if (control != null && listBindingInternal != null) {
 			/* update list of items in combo */
 			listBindingInternal.updateModelToTarget();
 			/* re-create selectionBinding */
 			ISWTObservableValue controlSelection = SWTObservables.observeSelection(control);
 			controlSelection.addValueChangeListener(valueChangeValidator);
 			DataBindingContext dbc = new DataBindingContext();
 			selectionBindingInternal = dbc.bindValue(controlSelection, selectionObservable, new UpdateValueStrategy(
 					UpdateValueStrategy.POLICY_UPDATE).setConverter(strToObjConverter).setAfterGetValidator(
 					selectionValidator), new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE)
 					.setConverter(objToStrConverter));
 			/* update selection in combo */
 			selectionBindingInternal.updateModelToTarget();
 		}
 	}
 
 	private void disposeBinding(Binding binding) {
 		if (binding != null && !binding.isDisposed()) {
 			binding.dispose();
 		}
 	}
 
 	private String getItemFromValue(Object value) {
 		Object valueObject = value;
 		if (value != null && renderingMethod != null) {
 			valueObject = ReflectionUtils.invoke(value, renderingMethod, (Object[]) null);
 		}
 		return String.valueOf(valueObject);
 	}
 
 	private Object getValueFromItem(String item) {
 		String[] items = getUIControl().getItems();
 		for (int i = 0; i < items.length; i++) {
 			if (items[i].equals(item)) {
 				return rowObservables.get(i);
 			}
 		}
 		return item;
 	}
 
 	private boolean hasInput() {
 		Object selection = selectionObservable.getValue();
 		return selection != null && selection != emptySelection;
 	}
 
 	/**
 	 * Clears the list of items in the combo, when the ridget is disabled.
 	 */
 	private void unbindControlFromSelectionAndClear() {
 		Combo control = getUIControl();
 		if (control != null && !isEnabled()) {
 			/* dispose selectionBinding to avoid sync */
 			disposeBinding(selectionBindingInternal);
 			selectionBindingInternal = null;
 			/* clear combo */
 			if (MarkerSupport.HIDE_DISABLED_RIDGET_CONTENT) {
 				control.removeAll();
 			}
 		}
 	}
 
 	// helping classes
 	// ////////////////
 
 	/**
 	 * Convert from model object to combo box items (strings).
 	 */
 	private final class ObjectToStringConverter extends Converter {
 		public ObjectToStringConverter() {
 			super(Object.class, String.class);
 		}
 
 		public Object convert(Object fromObject) {
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
 
 		public Object convert(Object fromObject) {
 			return getValueFromItem((String) fromObject);
 		}
 	}
 
 	/**
 	 * This validator can be used to interrupt an update request
 	 */
 	private final class SelectionBindingValidator implements IValidator {
 
 		private boolean isEnabled = true;
 
 		public IStatus validate(Object value) {
 			IStatus result = Status.OK_STATUS;
 			// disallow control to ridget update, isEnabled == false || output
 			if (!isEnabled || isOutputOnly()) {
 				result = Status.CANCEL_STATUS;
 			}
 			return result;
 		}
 
 		void enableBinding(final boolean isEnabled) {
 			this.isEnabled = isEnabled;
 		}
 	}
 
 	/**
 	 * Ensures the user cannot change the Combo when isOutputOnly is enabled.
 	 * 
 	 * @see IMarkableRidget#setOutputOnly(boolean)
 	 */
 	private final class ValueChangeValidator implements IValueChangeListener {
 
 		private volatile boolean changing = false;
 
 		public void handleValueChange(ValueChangeEvent event) {
 			if (changing || !isOutputOnly()) {
 				return;
 			}
 			changing = true;
 			try {
 				Combo combo = getUIControl();
 				String oldValue = (String) event.diff.getOldValue();
 				int index = oldValue != null ? combo.indexOf(oldValue) : -1;
 				if (index > -1) {
 					combo.select(index);
 				} else {
 					combo.deselectAll();
 				}
 			} finally {
 				changing = false;
 			}
 		}
 
 	}
 }
