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
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.databinding.BindingException;
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.core.databinding.beans.BeansObservables;
 import org.eclipse.core.databinding.observable.list.IObservableList;
 import org.eclipse.core.databinding.observable.value.ComputedValue;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.core.databinding.observable.value.IValueChangeListener;
 import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.layout.TableColumnLayout;
 import org.eclipse.jface.viewers.ColumnWeightData;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 
 import org.eclipse.riena.internal.ui.ridgets.swt.TableRidget.ITableRidgetDelegate;
 import org.eclipse.riena.ui.ridgets.AbstractCompositeRidget;
 import org.eclipse.riena.ui.ridgets.IActionListener;
 import org.eclipse.riena.ui.ridgets.IActionRidget;
 import org.eclipse.riena.ui.ridgets.IMarkableRidget;
 import org.eclipse.riena.ui.ridgets.IMasterDetailsDelegate;
 import org.eclipse.riena.ui.ridgets.IMasterDetailsRidget;
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.ITableRidget;
 import org.eclipse.riena.ui.ridgets.databinding.UnboundPropertyWritableList;
 import org.eclipse.riena.ui.ridgets.swt.AbstractSWTRidget;
 import org.eclipse.riena.ui.swt.MasterDetailsComposite;
 
 /**
  * A ridget for the {@link MasterDetailsComposite}.
  */
 public class MasterDetailsRidget extends AbstractCompositeRidget implements IMasterDetailsRidget {
 
 	private IObservableList rowObservables;
 
 	private IMasterDetailsDelegate delegate;
 	private DataBindingContext dbc;
 
 	/*
 	 * The object we are currently editing; null if not editing
 	 */
 	private Object editable;
 
 	/*
 	 * Markable ridgets from the details area.
 	 */
 	private List<IMarkableRidget> detailRidgets;
 
 	public MasterDetailsRidget() {
 		addPropertyChangeListener(null, new PropertyChangeListener() {
 			public void propertyChange(PropertyChangeEvent evt) {
 				if (delegate == null
 						|| editable == null
 						// ignore these events:
 						|| IMarkableRidget.PROPERTY_MARKER.equals(evt.getPropertyName())
 						|| IMarkableRidget.PROPERTY_ENABLED.equals(evt.getPropertyName())
 						|| IMarkableRidget.PROPERTY_OUTPUT_ONLY.equals(evt.getPropertyName())) {
 					return;
 				}
 				boolean isChanged = delegate.isChanged(editable, delegate.getWorkingCopy());
 				// System.out.println(isChanged + " : " + evt.getPropertyName() + " " + evt.getNewValue());
 				getApplyButtonRidget().setEnabled(isChanged);
 			}
 		});
 	}
 
 	public void setDelegate(IMasterDetailsDelegate delegate) {
 		Assert.isLegal(this.delegate == null, "setDelegate can only be called once"); //$NON-NLS-1$
 		Assert.isLegal(delegate != null, "delegate cannot be null"); //$NON-NLS-1$
 		this.delegate = delegate;
 		delegate.configureRidgets(this);
 	}
 
 	@Override
 	public MasterDetailsComposite getUIControl() {
 		return (MasterDetailsComposite) super.getUIControl();
 	}
 
 	@Override
 	public void setUIControl(Object uiControl) {
 		AbstractSWTRidget.assertType(uiControl, MasterDetailsComposite.class);
 		super.setUIControl(uiControl);
 	}
 
	public void bindToModel(IObservableList rowBeansObservable, Class<? extends Object> rowBeanClass,
 			String[] columnPropertyNames, String[] columnHeaders) {
		rowObservables = rowBeansObservable;
		getTableRidget().bindToModel(rowObservables, rowBeanClass, columnPropertyNames, columnHeaders);
 	}
 
	public void bindToModel(Object listBean, String listPropertyName, Class<? extends Object> rowBeanClass,
 			String[] columnPropertyNames, String[] headerNames) {
		IObservableList listObservableValue = new UnboundPropertyWritableList(listBean, listPropertyName);
		bindToModel(listObservableValue, rowBeanClass, columnPropertyNames, headerNames);
 	}
 
 	@Override
 	public final void configureRidgets() {
 		((TableRidget) getTableRidget()).setDelegate(new TablePreparer());
 
 		getNewButtonRidget().addListener(new IActionListener() {
 			public void callback() {
 				handleAdd();
 			}
 		});
 		getRemoveButtonRidget().addListener(new IActionListener() {
 			public void callback() {
 				handleRemove();
 			}
 		});
 		getApplyButtonRidget().addListener(new IActionListener() {
 			public void callback() {
 				if (canApply()) {
 					handleApply();
 				}
 			}
 		});
 
 		detailRidgets = getDetailRidgets();
 		setEnabled(false, false);
 
 		final IObservableValue viewerSelection = getTableRidget().getSingleSelectionObservable();
 		viewerSelection.addValueChangeListener(new IValueChangeListener() {
 			public void handleValueChange(ValueChangeEvent event) {
 				handleSelectionChange(event.getObservableValue().getValue());
 			}
 		});
 
 		Assert.isLegal(dbc == null);
 		dbc = new DataBindingContext();
 		bindEnablementToValue(dbc, getRemoveButtonRidget(), new ComputedValue(Boolean.TYPE) {
 			@Override
 			protected Object calculate() {
 				return Boolean.valueOf(viewerSelection.getValue() != null);
 			}
 		});
 	}
 
 	public Object getSelection() {
 		return getTableRidget().getSingleSelectionObservable().getValue();
 	}
 
 	public void setSelection(Object newSelection) {
 		getTableRidget().setSelection(newSelection);
 		MasterDetailsComposite control = getUIControl();
 		if (control != null) {
 			control.getTable().showSelection();
 		}
 	}
 
 	@Override
 	public void updateFromModel() {
 		super.updateFromModel();
 		ITableRidget tableRidget = getTableRidget();
 		if (tableRidget != null) {
 			tableRidget.updateFromModel();
 		}
 	}
 
 	// protected methods
 	//////////////////
 
 	@Override
 	protected final void updateToolTipText() {
 		MasterDetailsComposite control = getUIControl();
 		if (control != null) {
 			control.setToolTipText(getToolTipText());
 		}
 	}
 
 	@Override
 	protected final void updateVisible() {
 		MasterDetailsComposite control = getUIControl();
 		if (control != null) {
 			control.setVisible(isVisible());
 		}
 	}
 
 	// helping methods
 	//////////////////
 
 	private void assertIsBoundToModel() {
 		if (rowObservables == null) {
 			throw new BindingException("ridget not bound to model"); //$NON-NLS-1$
 		}
 	}
 
 	private void bindEnablementToValue(DataBindingContext dbc, IMarkableRidget ridget, IObservableValue value) {
 		Assert.isNotNull(ridget);
 		Assert.isNotNull(value);
 		dbc.bindValue(BeansObservables.observeValue(ridget, IMarkableRidget.PROPERTY_ENABLED), value, null, null);
 	}
 
 	private boolean canApply() {
 		String result = delegate.isValid(this);
 		if (result != null) {
 			Shell shell = getUIControl().getShell();
 			String title = Messages.MasterDetailsRidget_dialogTitle_applyFailed;
 			String message = title + " " + result; //$NON-NLS-1$
 			MessageDialog.openWarning(shell, title, message);
 		}
 		return result == null;
 	}
 
 	private void clearSelection() {
 		updateDetails(delegate.createWorkingCopy());
 		editable = null;
 	}
 
 	private ITableRidget getTableRidget() {
 		return (ITableRidget) getRidget(MasterDetailsComposite.BIND_ID_TABLE);
 	}
 
 	private IActionRidget getNewButtonRidget() {
 		return (IActionRidget) getRidget(MasterDetailsComposite.BIND_ID_NEW);
 	}
 
 	private IActionRidget getRemoveButtonRidget() {
 		return (IActionRidget) getRidget(MasterDetailsComposite.BIND_ID_REMOVE);
 	}
 
 	private IActionRidget getApplyButtonRidget() {
 		return (IActionRidget) getRidget(MasterDetailsComposite.BIND_ID_APPLY);
 	}
 
 	private List<IMarkableRidget> getDetailRidgets() {
 		List<IMarkableRidget> result = new ArrayList<IMarkableRidget>();
 		for (IRidget ridget : getRidgets()) {
 			if (ridget instanceof IMarkableRidget) {
 				result.add((IMarkableRidget) ridget);
 			}
 		}
 		result.remove(getNewButtonRidget());
 		result.remove(getRemoveButtonRidget());
 		result.remove(getApplyButtonRidget());
 		result.remove(getTableRidget());
 		return result;
 	}
 
 	private void setEnabled(boolean applyEnabled, boolean detailsEnabled) {
 		getApplyButtonRidget().setEnabled(applyEnabled);
 		for (IMarkableRidget ridget : detailRidgets) {
 			ridget.setEnabled(detailsEnabled);
 		}
 	}
 
 	private void updateDetails(Object bean) {
 		Assert.isNotNull(bean);
 		delegate.copyBean(bean, delegate.getWorkingCopy());
 		delegate.updateDetails(this);
 	}
 
 	/**
 	 * Non API; public for testing only.
 	 */
 	public void handleAdd() {
 		getTableRidget().clearSelection();
 		editable = delegate.createWorkingCopy();
 		updateDetails(editable);
 		setEnabled(false, true);
 		getUIControl().getDetails().setFocus();
 	}
 
 	/**
 	 * Non API; public for testing only.
 	 */
 	public void handleRemove() {
 		assertIsBoundToModel();
 		Object selection = getSelection();
 		Assert.isNotNull(selection);
 		rowObservables.remove(selection);
 		getTableRidget().clearSelection();
 		clearSelection();
 		setEnabled(false, false);
 	}
 
 	/**
 	 * Non API; public for testing only.
 	 */
 	public void handleApply() {
 		assertIsBoundToModel();
 		Assert.isNotNull(editable);
 		delegate.copyBean(delegate.getWorkingCopy(), editable);
 		if (!rowObservables.contains(editable)) { // add to table
 			rowObservables.add(editable);
 			setSelection(editable);
 		} else { // update
 			getTableRidget().updateFromModel();
 		}
 		setEnabled(false, false);
 		Table table = getUIControl().getTable();
 		table.select(table.getSelectionIndex());
 		table.setFocus();
 	}
 
 	private void handleSelectionChange(Object newSelection) {
 		if (newSelection != null) { // selection changed
 			editable = newSelection;
 			updateDetails(editable);
 			setEnabled(false, true);
 		} else { // nothing selected
 			clearSelection();
 			setEnabled(false, false);
 		}
 	}
 
 	// helping classes
 	//////////////////
 
 	private static final class TablePreparer implements ITableRidgetDelegate {
 		public void prepareTable(Table control, int expectedCols) {
 			int actualCols = control.getColumnCount() == 0 ? 1 : control.getColumnCount();
 			if (actualCols != expectedCols) {
 				for (TableColumn column : control.getColumns()) {
 					column.dispose();
 				}
 				TableColumnLayout layout = new TableColumnLayout();
 				for (int i = 0; i < expectedCols; i++) {
 					TableColumn column = new TableColumn(control, SWT.NONE);
 					layout.setColumnData(column, new ColumnWeightData(10));
 				}
 				Composite tableComposite = control.getParent();
 				tableComposite.setLayout(layout);
 				tableComposite.layout(true);
 			}
 		}
 	}
 
 }
