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
 package org.eclipse.riena.ui.ridgets.swt;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.core.databinding.BindingException;
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.core.databinding.beans.BeansObservables;
 import org.eclipse.core.databinding.beans.PojoObservables;
 import org.eclipse.core.databinding.observable.list.IObservableList;
 import org.eclipse.core.databinding.observable.value.ComputedValue;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.swt.widgets.Control;
 
 import org.eclipse.riena.ui.core.marker.ErrorMarker;
 import org.eclipse.riena.ui.core.marker.MandatoryMarker;
 import org.eclipse.riena.ui.ridgets.AbstractCompositeRidget;
 import org.eclipse.riena.ui.ridgets.IActionListener;
 import org.eclipse.riena.ui.ridgets.IActionRidget;
 import org.eclipse.riena.ui.ridgets.IMarkableRidget;
 import org.eclipse.riena.ui.ridgets.IMasterDetailsDelegate;
 import org.eclipse.riena.ui.ridgets.IMasterDetailsRidget;
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.IRidgetContainer;
 import org.eclipse.riena.ui.ridgets.ITableRidget;
 import org.eclipse.riena.ui.ridgets.ITextRidget;
 import org.eclipse.riena.ui.swt.AbstractMasterDetailsComposite;
 import org.eclipse.riena.ui.swt.MasterDetailsComposite;
 
 /**
  * Common functionality that is shared between implementations of the
  * {@link IMasterDetailsRidget}.
  * <p>
  * This class defines several widget-specific abstract methods, which must be
  * implemented by clients. It is expected that clients will write
  * widget-specific subclass of {@link AbstractMasterDetailsComposite}.
  * 
  * @since 1.2
  */
 public abstract class AbstractMasterDetailsRidget extends AbstractCompositeRidget implements IMasterDetailsRidget {
 
 	private IObservableList rowObservables;
 
 	private IMasterDetailsDelegate delegate;
 	private DataBindingContext dbc;
 	private boolean isDirectWriting;
 	private boolean applyRequiresNoErrors;
 	private boolean applyRequiresNoMandatories;
 	private boolean detailsEnabled;
 	private boolean ignoreChanges;
 
 	/*
 	 * The object we are currently editing; null if not editing
 	 */
 	private Object editable;
 
 	/*
 	 * All ridgets from the details area.
 	 */
 	private IRidgetContainer detailRidgets;
 
 	public AbstractMasterDetailsRidget() {
 		addPropertyChangeListener(null, new PropertyChangeListener() {
 			public void propertyChange(PropertyChangeEvent evt) {
 				String propertyName = evt.getPropertyName();
 				if (ignoreChanges
 						|| delegate == null
 						|| editable == null
 						// ignore these events:
 						|| (!applyRequiresNoErrors && !applyRequiresNoMandatories && IMarkableRidget.PROPERTY_MARKER
 								.equals(propertyName)) || IRidget.PROPERTY_ENABLED.equals(propertyName)
 						|| ITextRidget.PROPERTY_TEXT.equals(propertyName)
 						|| IMarkableRidget.PROPERTY_OUTPUT_ONLY.equals(propertyName)) {
 					return;
 				}
 				// System.out.println(String.format("prop: %s %s", evt.getPropertyName(), evt.getSource()));
 				updateApplyButton();
 			}
 		});
 	}
 
 	public final void bindToModel(IObservableList rowObservables, Class<? extends Object> rowClass,
 			String[] columnPropertyNames, String[] columnHeaders) {
 		this.rowObservables = rowObservables;
 		bindTableToModel(rowObservables, rowClass, columnPropertyNames, columnHeaders);
 	}
 
 	public final void bindToModel(Object listHolder, String listPropertyName, Class<? extends Object> rowClass,
 			String[] columnPropertyNames, String[] headerNames) {
 		IObservableList rowObservableList;
 		if (AbstractSWTWidgetRidget.isBean(rowClass)) {
 			rowObservableList = BeansObservables.observeList(listHolder, listPropertyName);
 		} else {
 			rowObservableList = PojoObservables.observeList(listHolder, listPropertyName);
 		}
 		bindToModel(rowObservableList, rowClass, columnPropertyNames, headerNames);
 	}
 
 	@Override
 	public void configureRidgets() {
 		configureTableRidget();
 
 		if (hasNewButton()) {
 			getNewButtonRidget().addListener(new IActionListener() {
 				public void callback() {
 					if (canAdd()) {
 						handleAdd();
 					}
 				}
 			});
 		}
 
 		if (hasRemoveButton()) {
 			getRemoveButtonRidget().addListener(new IActionListener() {
 				public void callback() {
 					if (canRemove()) {
 						handleRemove();
 					}
 				}
 			});
 		}
 
 		getApplyButtonRidget().addListener(new IActionListener() {
 			public void callback() {
 				if (canApply()) {
 					handleApply();
 				}
 			}
 		});
 
 		detailRidgets = new DetailRidgetContainer();
 		setEnabled(false, false);
 
 		final IObservableValue viewerSelection = getSelectionObservable();
 
 		Assert.isLegal(dbc == null);
 		if (hasRemoveButton()) {
 			dbc = new DataBindingContext();
 			bindEnablementToValue(dbc, getRemoveButtonRidget(), new ComputedValue(Boolean.TYPE) {
 				@Override
 				protected Object calculate() {
 					return Boolean.valueOf(viewerSelection.getValue() != null);
 				}
 			});
 		}
 
 		for (IRidget ridget : detailRidgets.getRidgets()) {
 			ridget.addPropertyChangeListener(new PropertyChangeListener() {
 				public void propertyChange(PropertyChangeEvent evt) {
 					if (isDirectWriting == false
 							|| delegate == null
 							|| editable == null
 							// ignore these events:
 							|| IMarkableRidget.PROPERTY_MARKER.equals(evt.getPropertyName())
 							|| IRidget.PROPERTY_ENABLED.equals(evt.getPropertyName())
 							|| ITextRidget.PROPERTY_TEXT.equals(evt.getPropertyName())
 							|| IMarkableRidget.PROPERTY_OUTPUT_ONLY.equals(evt.getPropertyName())) {
 						return;
 					}
 					// this is only reached when direct writing is on and one of 'interesting' events happens
 					delegate.copyBean(delegate.getWorkingCopy(), editable);
 					getTableRidget().updateFromModel();
 					// we are already editing, so we want to invoke getTR().setSelection(editable) instead
 					// of setSelection(editable). This will just select the editable in the table.
 					setTableSelection(editable);
 				}
 			});
 		}
 	}
 
 	public final IMasterDetailsDelegate getDelegate() {
 		return this.delegate;
 	}
 
 	public final Object getSelection() {
 		return getSelectionObservable().getValue();
 	}
 
 	@Override
 	public AbstractMasterDetailsComposite getUIControl() {
 		return (AbstractMasterDetailsComposite) super.getUIControl();
 	}
 
 	public boolean isApplyRequiresNoErrors() {
 		return applyRequiresNoErrors;
 	}
 
 	public boolean isApplyRequiresNoMandatories() {
 		return applyRequiresNoMandatories;
 	}
 
 	public boolean isDirectWriting() {
 		return isDirectWriting;
 	}
 
 	public void setApplyRequiresNoErrors(boolean requiresNoErrors) {
 		if (applyRequiresNoErrors != requiresNoErrors) {
 			applyRequiresNoErrors = requiresNoErrors;
 			updateApplyButton();
 		}
 	}
 
 	public void setApplyRequiresNoMandatories(boolean requiresNoMandatories) {
 		if (applyRequiresNoMandatories != requiresNoMandatories) {
 			applyRequiresNoMandatories = requiresNoMandatories;
 			updateApplyButton();
 		}
 	}
 
 	public void setColumnWidths(Object[] widths) {
 		((ITableRidget) getTableRidget()).setColumnWidths(widths);
 	}
 
 	public final void setDelegate(IMasterDetailsDelegate delegate) {
 		Assert.isLegal(this.delegate == null, "setDelegate can only be called once"); //$NON-NLS-1$
 		Assert.isLegal(delegate != null, "delegate cannot be null"); //$NON-NLS-1$
 		this.delegate = delegate;
 		delegate.configureRidgets(detailRidgets);
 	}
 
 	public void setDirectWriting(boolean directWriting) {
 		if (directWriting != isDirectWriting) {
 			isDirectWriting = directWriting;
 			getApplyButtonRidget().setVisible(!directWriting);
 		}
 	}
 
 	public void setSelection(Object newSelection) {
 		setTableSelection(newSelection);
 		handleSelectionChange(newSelection);
 		AbstractMasterDetailsComposite control = getUIControl();
 		if (control != null) {
 			revealTableSelection();
 		}
 	}
 
 	public void suggestNewEntry(Object entry) {
 		ignoreChanges = true;
 		try {
 			editable = entry;
 			delegate.prepareItemSelected(editable);
 			setEnabled(true, true);
 			updateDetails(editable);
 			ignoreChanges = true;
 			delegate.itemSelected(editable);
 		} finally {
 			ignoreChanges = false;
 		}
 	}
 
 	public final void updateApplyButton() {
 		if (applyRequiresNoErrors || applyRequiresNoMandatories) {
 			boolean noErrors = applyRequiresNoErrors ? !hasErrors() : true;
 			boolean noMandatories = applyRequiresNoMandatories ? !hasMandatories() : true;
 			boolean isEnabled = noErrors && noMandatories && areDetailsChanged();
 			getApplyButtonRidget().setEnabled(isEnabled);
 		} else {
 			getApplyButtonRidget().setEnabled(areDetailsChanged());
 		}
 	}
 
 	@Override
 	public void updateFromModel() {
 		checkDelegate();
 		super.updateFromModel();
 		IRidget tableRidget = getTableRidget();
 		if (tableRidget != null) {
 			tableRidget.updateFromModel();
 		}
 	}
 
 	// protected methods
 	////////////////////
 
 	@Override
 	protected void checkUIControl(Object uiControl) {
 		AbstractSWTRidget.assertType(uiControl, AbstractMasterDetailsComposite.class);
 	}
 
 	protected abstract void bindTableToModel(IObservableList rowObservables, Class<? extends Object> rowClass,
 			String[] columnPropertyNames, String[] columnHeaders);
 
 	protected abstract void configureTableRidget();
 
 	protected abstract void clearTableSelection();
 
 	protected abstract Object getTableSelection();
 
 	protected abstract IObservableValue getSelectionObservable();
 
 	protected abstract void revealTableSelection();
 
 	protected abstract void setTableSelection(Object value);
 
 	protected final boolean areDetailsChanged() {
 		if (detailsEnabled) {
 			return editable != null && delegate.isChanged(editable, delegate.getWorkingCopy());
 		}
 		return false;
 	}
 
 	protected void handleSelectionChange(Object newSelection) {
 		ignoreChanges = true;
 		try {
 			delegate.prepareItemSelected(newSelection);
 			if (newSelection != null) { // selection changed
 				editable = newSelection;
 				setEnabled(false, true);
 				updateDetails(editable);
 			} else { // nothing selected
 				clearSelection();
 				setEnabled(false, false);
 			}
 			ignoreChanges = true;
 			delegate.itemSelected(newSelection);
 		} finally {
 			ignoreChanges = false;
 		}
 	}
 
 	@Override
 	protected boolean isUIControlVisible() {
 		return getUIControl().isVisible();
 	}
 
 	@Override
 	protected final void updateEnabled() {
 		AbstractMasterDetailsComposite control = getUIControl();
 		if (control != null) {
 			if (!isEnabled()) {
 				clearSelection();
 				clearTableSelection();
 				Collection<? extends IRidget> ridgets = getRidgets();
 				for (IRidget ridget : ridgets) {
 					ridget.setEnabled(false);
 				}
 			} else {
 				if (getTableRidget() != null) {
 					getTableRidget().setEnabled(true);
 				}
 				if (getNewButtonRidget() != null) {
 					getNewButtonRidget().setEnabled(true);
 				}
 			}
 			control.setEnabled(isEnabled());
 		}
 	}
 
 	@Override
 	protected final void updateToolTipText() {
 		AbstractMasterDetailsComposite control = getUIControl();
 		if (control != null) {
 			control.setToolTipText(getToolTipText());
 		}
 	}
 
 	@Override
 	protected final void updateVisible() {
 		AbstractMasterDetailsComposite control = getUIControl();
 		if (control != null) {
 			control.setVisible(!isMarkedHidden());
 		}
 	}
 
 	// helping methods
 	//////////////////
 
 	private void assertIsBoundToModel() {
 		if (rowObservables == null) {
 			throw new BindingException("ridget not bound to model"); //$NON-NLS-1$
 		}
 	}
 
 	private void bindEnablementToValue(DataBindingContext dbc, IRidget ridget, IObservableValue value) {
 		Assert.isNotNull(ridget);
 		Assert.isNotNull(value);
 		dbc.bindValue(BeansObservables.observeValue(ridget, IRidget.PROPERTY_ENABLED), value, null, null);
 	}
 
 	private boolean canAdd() {
 		boolean result = true;
 		if (areDetailsChanged()) {
 			result = getUIControl().confirmDiscardChanges();
 		}
 		return result;
 	}
 
 	private boolean canApply() {
 		String reason = delegate.isValid(detailRidgets);
 		if (reason != null) {
 			getUIControl().warnApplyFailed(reason);
 		}
 		return reason == null;
 	}
 
 	private boolean canRemove() {
 		Object selection = getSelection();
 		Assert.isNotNull(selection);
 		return getUIControl().confirmRemove(selection);
 	}
 
 	private void checkDelegate() {
 		if (delegate == null) {
 			throw new IllegalStateException("no delegate: call setDelegate(...)"); //$NON-NLS-1$
 		}
 	}
 
 	private void clearSelection() {
 		updateDetails(delegate.createWorkingCopy());
 		editable = null;
 	}
 
 	private Control getDetailsControl() {
 		Control result = null;
 		AbstractMasterDetailsComposite control = getUIControl();
 		if (control != null) {
 			result = control.getDetails();
 		}
 		return result;
 	}
 
 	private IRidget getTableRidget() {
 		// this is not necessarily an ITableRidget, can be any IRidget
		return getRidget(IRidget.class, MasterDetailsComposite.BIND_ID_TABLE);
 	}
 
 	private IActionRidget getNewButtonRidget() {
 		return getRidget(IActionRidget.class, MasterDetailsComposite.BIND_ID_NEW);
 	}
 
 	private IActionRidget getRemoveButtonRidget() {
 		return getRidget(IActionRidget.class, MasterDetailsComposite.BIND_ID_REMOVE);
 	}
 
 	private IActionRidget getApplyButtonRidget() {
 		return getRidget(IActionRidget.class, MasterDetailsComposite.BIND_ID_APPLY);
 	}
 
 	private boolean hasErrors() {
 		for (IRidget ridget : detailRidgets.getRidgets()) {
 			if (ridget instanceof IMarkableRidget) {
 				IMarkableRidget markableRidget = (IMarkableRidget) ridget;
 				if (!markableRidget.getMarkersOfType(ErrorMarker.class).isEmpty()) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	private boolean hasMandatories() {
 		for (IRidget ridget : detailRidgets.getRidgets()) {
 			if (ridget instanceof IMarkableRidget) {
 				IMarkableRidget markableRidget = (IMarkableRidget) ridget;
 				for (MandatoryMarker marker : markableRidget.getMarkersOfType(MandatoryMarker.class)) {
 					if (!marker.isDisabled()) {
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	private boolean hasNewButton() {
 		return getNewButtonRidget() != null;
 	}
 
 	private boolean hasRemoveButton() {
 		return getRemoveButtonRidget() != null;
 	}
 
 	private void setEnabled(boolean applyEnabled, boolean detailsEnabled) {
 		ignoreChanges = true;
 		try {
 			getApplyButtonRidget().setEnabled(applyEnabled);
 			this.detailsEnabled = detailsEnabled;
 			for (IRidget ridget : detailRidgets.getRidgets()) {
 				ridget.setEnabled(detailsEnabled);
 			}
 		} finally {
 			ignoreChanges = false;
 		}
 	}
 
 	private void setFocusToDetails() {
 		final Control focusable = getDetailsControl();
 		if (focusable != null) {
 			focusable.getDisplay().asyncExec(new Runnable() {
 				public void run() {
 					if (!focusable.isDisposed()) {
 						clearTableSelection();
 						focusable.setFocus();
 					}
 				}
 			});
 		}
 	}
 
 	private void setFocusToTable() {
 		AbstractMasterDetailsComposite control = getUIControl();
 		if (control != null) {
 			final Control table = control.getTable();
 			table.getDisplay().asyncExec(new Runnable() {
 				public void run() {
 					if (!table.isDisposed()) {
 						table.setFocus();
 						// this has to be after table.setFocus() otherwise
 						// the 'focus' rectangle is sometimes lost
 						clearTableSelection();
 					}
 				}
 			});
 		}
 	}
 
 	private void updateDetails(Object bean) {
 		Assert.isNotNull(bean);
 		ignoreChanges = true;
 		try {
 			delegate.copyBean(bean, delegate.getWorkingCopy());
 			delegate.updateDetails(detailRidgets);
 		} finally {
 			ignoreChanges = false;
 		}
 	}
 
 	/**
 	 * Non API; public for testing only.
 	 */
 	public void handleAdd() {
 		if (!isDirectWriting) {
 			// create the editable and update the details
 			editable = delegate.createWorkingCopy();
 			delegate.itemCreated(editable);
 			setEnabled(false, true);
 			updateDetails(editable);
 			clearTableSelection();
 			getUIControl().getDetails().setFocus();
 		} else {
 			// create the editable, add it to the table, update the details
 			editable = delegate.createWorkingCopy();
 			delegate.itemCreated(editable);
 			rowObservables.add(editable);
 			getTableRidget().updateFromModel();
 			setSelection(editable);
 			setEnabled(false, true);
 			updateDetails(editable);
 			getUIControl().getDetails().setFocus();
 		}
 	}
 
 	/**
 	 * Non API; public for testing only.
 	 */
 	public void handleRemove() {
 		assertIsBoundToModel();
 		Object selection = getSelection();
 		Assert.isNotNull(selection);
 		rowObservables.remove(selection);
 		clearSelection();
 		clearTableSelection();
 		getTableRidget().updateFromModel();
 		setEnabled(false, false);
 		delegate.itemRemoved(selection);
 	}
 
 	/**
 	 * Non API; public for testing only.
 	 */
 	public void handleApply() {
 		assertIsBoundToModel();
 		Assert.isNotNull(editable);
 		delegate.copyBean(delegate.getWorkingCopy(), editable);
 		delegate.itemApplied(editable);
 		if (!rowObservables.contains(editable)) { // add to table
 			rowObservables.add(editable);
 			getTableRidget().updateFromModel();
 			setTableSelection(editable);
 			revealTableSelection();
 		} else { // update
 			getTableRidget().updateFromModel();
 		}
 		if (hasNewButton() && !isDirectWriting) {
 			handleAdd(); // automatically hit the 'new/add' button
 			setFocusToDetails();
 		} else {
 			setEnabled(false, false);
 			setFocusToTable();
 		}
 	}
 
 	/**
 	 * IRidgetContainer exposing the 'detail' ridgets only (instead of all
 	 * ridgets).
 	 */
 	private final class DetailRidgetContainer implements IRidgetContainer {
 
 		private final List<IRidget> detailRidgets;
 
 		public DetailRidgetContainer() {
 			detailRidgets = getDetailRidgets();
 		}
 
 		public void addRidget(String id, IRidget ridget) {
 			throw new UnsupportedOperationException("not supported"); //$NON-NLS-1$
 		}
 
 		public void configureRidgets() {
 			throw new UnsupportedOperationException("not supported"); //$NON-NLS-1$
 		}
 
 		public IRidget getRidget(String id) {
 			return AbstractMasterDetailsRidget.this.getRidget(id);
 		}
 
 		public <R extends IRidget> R getRidget(Class<R> ridgetClazz, String id) {
 			return AbstractMasterDetailsRidget.this.getRidget(ridgetClazz, id);
 		}
 
 		public Collection<? extends IRidget> getRidgets() {
 			return detailRidgets;
 		}
 
 		private List<IRidget> getDetailRidgets() {
 			List<IRidget> result = new ArrayList<IRidget>(AbstractMasterDetailsRidget.this.getRidgets());
 			result.remove(getNewButtonRidget());
 			result.remove(getRemoveButtonRidget());
 			result.remove(getApplyButtonRidget());
 			result.remove(getTableRidget());
 			return result;
 		}
 
 	}
 
 }
