 /*******************************************************************************
  * Copyright (c) 2007, 2011 compeople AG and others.
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
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.databinding.Binding;
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.core.databinding.UpdateListStrategy;
 import org.eclipse.core.databinding.UpdateValueStrategy;
 import org.eclipse.core.databinding.beans.BeansObservables;
 import org.eclipse.core.databinding.beans.PojoObservables;
 import org.eclipse.core.databinding.observable.list.IListChangeListener;
 import org.eclipse.core.databinding.observable.list.IObservableList;
 import org.eclipse.core.databinding.observable.list.ListChangeEvent;
 import org.eclipse.core.databinding.observable.list.ListDiffEntry;
 import org.eclipse.core.databinding.observable.list.WritableList;
 import org.eclipse.core.databinding.observable.map.IObservableMap;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.jface.databinding.viewers.IViewerObservableList;
 import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
 import org.eclipse.jface.databinding.viewers.ViewersObservables;
 import org.eclipse.jface.viewers.ColumnLayoutData;
 import org.eclipse.jface.viewers.ColumnPixelData;
 import org.eclipse.jface.viewers.ColumnWeightData;
 import org.eclipse.jface.viewers.IBaseLabelProvider;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.ControlListener;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.MouseMoveListener;
 import org.eclipse.swt.events.MouseTrackAdapter;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.Widget;
 
 import org.eclipse.riena.core.util.ListenerList;
 import org.eclipse.riena.core.util.RAPDetector;
 import org.eclipse.riena.core.util.StringUtils;
 import org.eclipse.riena.ui.common.ISortableByColumn;
 import org.eclipse.riena.ui.core.marker.RowErrorMessageMarker;
 import org.eclipse.riena.ui.ridgets.IActionListener;
 import org.eclipse.riena.ui.ridgets.IColumnFormatter;
 import org.eclipse.riena.ui.ridgets.IMarkableRidget;
 import org.eclipse.riena.ui.ridgets.ISelectableRidget;
 import org.eclipse.riena.ui.ridgets.ITableRidget;
 import org.eclipse.riena.ui.ridgets.listener.ClickEvent;
 import org.eclipse.riena.ui.ridgets.listener.IClickListener;
 import org.eclipse.riena.ui.ridgets.swt.AbstractSWTWidgetRidget;
 import org.eclipse.riena.ui.ridgets.swt.AbstractSelectableIndexedRidget;
 import org.eclipse.riena.ui.ridgets.swt.ColumnFormatter;
 import org.eclipse.riena.ui.ridgets.swt.MarkerSupport;
 import org.eclipse.riena.ui.ridgets.swt.SortableComparator;
 import org.eclipse.riena.ui.swt.facades.SWTFacade;
 import org.eclipse.riena.ui.swt.facades.TableRidgetToolTipSupportFacade;
 import org.eclipse.riena.ui.swt.lnf.LnfKeyConstants;
 import org.eclipse.riena.ui.swt.lnf.LnfManager;
 import org.eclipse.riena.ui.swt.utils.SwtUtilities;
 
 /**
  * Ridget for SWT {@link Table} widgets.
  */
 public class TableRidget extends AbstractSelectableIndexedRidget implements ITableRidget {
 
 	private final Listener itemEraser;
 	private final SelectionListener selectionTypeEnforcer;
 	private final MouseListener clickForwarder;
	private TableTooltipManager tooltipManager;
 	private final ColumnSortListener sortListener;
 	private ListenerList<IClickListener> clickListeners;
 	private ListenerList<IActionListener> doubleClickListeners;
 
 	private DataBindingContext dbc;
 	/*
 	 * Binds the viewer's multiple selection to the multiple selection
 	 * observable. This binding has to be disposed when the ridget is set to
 	 * output-only, to avoid updating the model. It has to be recreated when the
 	 * ridget is set to not-output-only.
 	 */
 	private Binding viewerMSB;
 
 	private TableViewer viewer;
 	private String[] columnHeaders;
 	private ColumnLayoutData[] columnWidths;
 	/*
 	 * Data we received in bindToModel(...). May change without our doing.
 	 */
 	private IObservableList modelObservables;
 	/*
 	 * Data the viewer is bound to. It is updated from modelObservables on
 	 * updateFromModel().
 	 */
 	private IObservableList viewerObservables;
 
 	private Class<?> rowClass;
 	private String[] renderingMethods;
 
 	private boolean isSortedAscending;
 	private int sortedColumn;
 	private final Map<Integer, Boolean> sortableColumnsMap;
 	private final Map<Integer, Comparator<Object>> comparatorMap;
 	private final Map<Integer, IColumnFormatter> formatterMap;
 	private boolean moveableColumns;
 	private ControlListener columnResizeListener;
 	private boolean nativeToolTip = true;
 
 	public TableRidget() {
 		itemEraser = new TableItemEraser();
 		selectionTypeEnforcer = new SelectionTypeEnforcer();
 		clickForwarder = new ClickForwarder();
 		sortListener = new ColumnSortListener();
 		isSortedAscending = true;
 		sortedColumn = -1;
 		sortableColumnsMap = new HashMap<Integer, Boolean>();
 		comparatorMap = new HashMap<Integer, Comparator<Object>>();
 		formatterMap = new HashMap<Integer, IColumnFormatter>();
 		addPropertyChangeListener(IMarkableRidget.PROPERTY_OUTPUT_ONLY, new PropertyChangeListener() {
 			public void propertyChange(final PropertyChangeEvent evt) {
 				if (isOutputOnly()) {
 					disposeMultipleSelectionBinding();
 				} else {
 					createMultipleSelectionBinding();
 				}
 			}
 		});
 	}
 
 	@Override
 	protected void checkUIControl(final Object uiControl) {
 		checkType(uiControl, Table.class);
 	}
 
 	@Override
 	protected void bindUIControl() {
 		final Table control = getUIControl();
 		if (control != null) {
 			viewer = new TableRidgetTableViewer(this);
 			configureControl(control);
 
 			if (viewerObservables != null) {
 				configureViewer(viewer);
 			}
 
 			dbc = new DataBindingContext();
 			// viewer to single selection binding
 			final IObservableValue viewerSelection = ViewersObservables.observeSingleSelection(viewer);
 			dbc.bindValue(viewerSelection, getSingleSelectionObservable(), new UpdateValueStrategy(
 					UpdateValueStrategy.POLICY_UPDATE).setAfterGetValidator(new OutputAwareValidator(this)),
 					new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE));
 			// viewer to to multi-selection binding
 			viewerMSB = null;
 			if (!isOutputOnly()) {
 				createMultipleSelectionBinding();
 			}
 
 			columnResizeListener = new ControlListener() {
 				public void controlResized(final ControlEvent e) {
 					applyTableColumnHeaders(control);
 				}
 
 				public void controlMoved(final ControlEvent e) {
 					applyTableColumnHeaders(control);
 				}
 			};
 
 			for (final TableColumn column : control.getColumns()) {
 				column.addSelectionListener(sortListener);
 				column.addControlListener(columnResizeListener);
 			}
 			control.addSelectionListener(selectionTypeEnforcer);
 			control.addMouseListener(clickForwarder);
 			updateToolTipSupport();
 			final SWTFacade facade = SWTFacade.getDefault();
 			facade.addEraseItemListener(control, itemEraser);
 		}
 	}
 
 	private void updateToolTipSupport() {
 		final SWTFacade facade = SWTFacade.getDefault();
 		if (isNativeToolTip() && TableRidgetToolTipSupportFacade.getDefault().isSupported()) {
 			TableRidgetToolTipSupportFacade.getDefault().disable();
 			if (tooltipManager == null) {
 				tooltipManager = new TableTooltipManager();
 				tooltipManager.init(getUIControl());
 			}
 			facade.addMouseTrackListener(getUIControl(), tooltipManager);
 			facade.addMouseMoveListener(getUIControl(), tooltipManager);
 		} else {
 			if (tooltipManager != null) {
 				facade.removeMouseTrackListener(getUIControl(), tooltipManager);
 				facade.removeMouseMoveListener(getUIControl(), tooltipManager);
 			}
 			if (viewer instanceof TableRidgetTableViewer) {
 				TableRidgetToolTipSupportFacade.getDefault().enableFor(viewer);
 			}
 		}
 	}
 
 	@Override
 	protected void unbindUIControl() {
 		super.unbindUIControl();
 		if (dbc != null) {
 			disposeMultipleSelectionBinding();
 			dbc.dispose();
 			dbc = null;
 		}
 		final Table control = getUIControl();
 		if (control != null) {
 			for (final TableColumn column : control.getColumns()) {
 				column.removeSelectionListener(sortListener);
 				column.removeControlListener(columnResizeListener);
 			}
 			control.removeSelectionListener(selectionTypeEnforcer);
 			control.removeMouseListener(clickForwarder);
 			final SWTFacade facade = SWTFacade.getDefault();
 			if (tooltipManager != null) {
 				facade.removeMouseTrackListener(control, tooltipManager);
 				facade.removeMouseMoveListener(control, tooltipManager);
 			}
 			facade.removeEraseItemListener(control, itemEraser);
 		}
 		viewer = null;
 	}
 
 	@Override
 	protected java.util.List<?> getRowObservables() {
 		return viewerObservables;
 	}
 
 	public void addClickListener(final IClickListener listener) {
 		Assert.isNotNull(listener, "listener is null"); //$NON-NLS-1$
 		if (clickListeners == null) {
 			clickListeners = new ListenerList<IClickListener>(IClickListener.class);
 		}
 		clickListeners.add(listener);
 	}
 
 	public void addDoubleClickListener(final IActionListener listener) {
 		Assert.isNotNull(listener, "listener is null"); //$NON-NLS-1$
 		if (doubleClickListeners == null) {
 			doubleClickListeners = new ListenerList<IActionListener>(IActionListener.class);
 		}
 		doubleClickListeners.add(listener);
 	}
 
 	public void bindToModel(final IObservableList rowObservables, final Class<? extends Object> aRowClass,
 			final String[] columnPropertyNames, final String[] columnHeaders) {
 		if (columnHeaders != null) {
 			final String msg = "Mismatch between number of columnPropertyNames and columnHeaders"; //$NON-NLS-1$
 			Assert.isLegal(columnPropertyNames.length == columnHeaders.length, msg);
 		}
 		unbindUIControl();
 
 		rowClass = aRowClass;
 		modelObservables = rowObservables;
 		viewerObservables = null;
 		renderingMethods = new String[columnPropertyNames.length];
 		System.arraycopy(columnPropertyNames, 0, renderingMethods, 0, renderingMethods.length);
 
 		if (columnHeaders != null) {
 			this.columnHeaders = new String[columnHeaders.length];
 			System.arraycopy(columnHeaders, 0, this.columnHeaders, 0, this.columnHeaders.length);
 		} else {
 			this.columnHeaders = null;
 		}
 
 		bindUIControl();
 	}
 
 	public void bindToModel(final Object listHolder, final String listPropertyName,
 			final Class<? extends Object> rowClass, final String[] columnPropertyNames, final String[] columnHeaders) {
 		IObservableList rowValues;
 		if (AbstractSWTWidgetRidget.isBean(rowClass)) {
 			rowValues = BeansObservables.observeList(listHolder, listPropertyName);
 		} else {
 			rowValues = PojoObservables.observeList(listHolder, listPropertyName);
 		}
 		bindToModel(rowValues, rowClass, columnPropertyNames, columnHeaders);
 	}
 
 	public IObservableList getObservableList() {
 		return viewerObservables;
 	}
 
 	@Override
 	public Object getOption(final int index) {
 		if (getRowObservables() == null || index < 0 || index >= getOptionCount()) {
 			throw new IllegalArgumentException("index: " + index); //$NON-NLS-1$
 		}
 		if (viewer != null) {
 			return viewer.getElementAt(index); // sorted
 		}
 		return getRowObservables().get(index); // unsorted
 	}
 
 	@Override
 	public int getSelectionIndex() {
 		final Table control = getUIControl();
 		return control == null ? -1 : control.getSelectionIndex();
 	}
 
 	@Override
 	public int[] getSelectionIndices() {
 		final Table control = getUIControl();
 		return control == null ? new int[0] : control.getSelectionIndices();
 	}
 
 	public int getSortedColumn() {
 		final boolean isSorted = sortedColumn != -1 && isColumnSortable(sortedColumn);
 		return isSorted ? sortedColumn : -1;
 	}
 
 	@Override
 	public Table getUIControl() {
 		return (Table) super.getUIControl();
 	}
 
 	public boolean hasMoveableColumns() {
 		return moveableColumns;
 	}
 
 	@Override
 	public int indexOfOption(final Object option) {
 		final Table control = getUIControl();
 		if (control != null) {
 			// implies viewer != null
 			final int optionCount = control.getItemCount();
 			for (int i = 0; i < optionCount; i++) {
 				if (viewer.getElementAt(i).equals(option)) {
 					return i;
 				}
 			}
 		}
 		return -1;
 	}
 
 	public boolean isColumnSortable(final int columnIndex) {
 		checkColumnRange(columnIndex);
 		boolean result = false;
 		final Integer key = Integer.valueOf(columnIndex);
 		final Boolean sortable = sortableColumnsMap.get(columnIndex);
 		if (sortable == null || Boolean.TRUE.equals(sortable)) {
 			result = comparatorMap.get(key) != null;
 		}
 		return result;
 	}
 
 	/**
 	 * Always returns true because mandatory markers do not make sense for this
 	 * ridget.
 	 */
 	@Override
 	public boolean isDisableMandatoryMarker() {
 		return true;
 	}
 
 	public boolean isSortedAscending() {
 		return getSortedColumn() != -1 && isSortedAscending;
 	}
 
 	public void refresh(final Object element) {
 		if (viewer != null) {
 			viewer.refresh(element, true);
 		}
 	}
 
 	public void removeDoubleClickListener(final IActionListener listener) {
 		if (doubleClickListeners != null) {
 			doubleClickListeners.remove(listener);
 		}
 	}
 
 	public void removeClickListener(final IClickListener listener) {
 		if (clickListeners != null) {
 			clickListeners.remove(listener);
 		}
 	}
 
 	public void setColumnFormatter(final int columnIndex, final IColumnFormatter formatter) {
 		checkColumnRange(columnIndex);
 		if (formatter != null) {
 			Assert.isLegal(formatter instanceof ColumnFormatter, "formatter must sublass ColumnFormatter"); //$NON-NLS-1$
 		}
 		final Integer key = Integer.valueOf(columnIndex);
 		formatterMap.put(key, formatter);
 	}
 
 	public void setColumnSortable(final int columnIndex, final boolean sortable) {
 		checkColumnRange(columnIndex);
 		final Integer key = Integer.valueOf(columnIndex);
 		final Boolean newValue = Boolean.valueOf(sortable);
 		Boolean oldValue = sortableColumnsMap.put(key, newValue);
 		if (oldValue == null) {
 			oldValue = Boolean.TRUE;
 		}
 		if (!newValue.equals(oldValue)) {
 			firePropertyChange(ISortableByColumn.PROPERTY_COLUMN_SORTABILITY, null, columnIndex);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * <p>
 	 * Implementation note: if the array is non-null, its elements must be
 	 * {@link ColumnPixelData} or {@link ColumnWeightData} instances.
 	 * 
 	 * @throws RuntimeException
 	 *             if an unsupported array element is encountered
 	 */
 	public void setColumnWidths(final Object[] widths) {
 		columnWidths = ColumnUtils.copyWidths(widths);
 		final Table control = getUIControl();
 		if (control != null) {
 			applyColumnWidths(control);
 		}
 	}
 
 	public void setComparator(final int columnIndex, final Comparator<Object> compi) {
 		checkColumnRange(columnIndex);
 		final Integer key = Integer.valueOf(columnIndex);
 		if (compi != null) {
 			comparatorMap.put(key, compi);
 		} else {
 			comparatorMap.remove(key);
 		}
 		if (columnIndex == sortedColumn) {
 			applyComparator();
 		}
 	}
 
 	public void setMoveableColumns(final boolean moveableColumns) {
 		if (this.moveableColumns != moveableColumns) {
 			this.moveableColumns = moveableColumns;
 			final Table control = getUIControl();
 			if (control != null) {
 				applyColumnsMoveable(control);
 			}
 		}
 	}
 
 	public void setSortedAscending(final boolean ascending) {
 		if (isSortedAscending != ascending) {
 			final boolean oldSortedAscending = isSortedAscending;
 			isSortedAscending = ascending;
 			applyComparator();
 			firePropertyChange(ISortableByColumn.PROPERTY_SORT_ASCENDING, oldSortedAscending, isSortedAscending);
 		}
 	}
 
 	public void setSortedColumn(final int columnIndex) {
 		if (columnIndex != -1) {
 			checkColumnRange(columnIndex);
 		}
 		if (sortedColumn != columnIndex) {
 			final int oldSortedColumn = sortedColumn;
 			sortedColumn = columnIndex;
 			applyComparator();
 			firePropertyChange(ISortableByColumn.PROPERTY_SORTED_COLUMN, oldSortedColumn, sortedColumn);
 		}
 	}
 
 	public boolean isNativeToolTip() {
 		return nativeToolTip;
 	}
 
 	public void setNativeToolTip(final boolean nativeToolTip) {
 		if (this.nativeToolTip != nativeToolTip) {
 			this.nativeToolTip = nativeToolTip;
 			updateToolTipSupport();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void updateFromModel() {
 		super.updateFromModel();
 		if (modelObservables != null) {
 			final List<Object> copy = new ArrayList<Object>(modelObservables);
 			viewerObservables = new WritableList(copy, rowClass);
 		}
 		if (viewer != null) {
 			if (!isViewerConfigured()) {
 				configureControl(viewer.getTable());
 				configureViewer(viewer);
 			} else {
 				refreshViewer(viewer);
 			}
 		} else {
 			refreshSelection();
 		}
 	}
 
 	private void applyColumns(final Table control) {
 		final int expectedCols = renderingMethods.length;
 		if (control.getColumnCount() != expectedCols) {
 			for (final TableColumn column : control.getColumns()) {
 				column.dispose();
 			}
 			for (int i = 0; i < expectedCols; i++) {
 				new TableColumn(control, SWT.NONE);
 			}
 			applyColumnWidths(control);
 		}
 	}
 
 	private void applyColumnWidths(final Table control) {
 		ColumnUtils.applyColumnWidths(control, columnWidths);
 	}
 
 	private void applyColumnsMoveable(final Table control) {
 		for (final TableColumn column : control.getColumns()) {
 			column.setMoveable(moveableColumns);
 		}
 	}
 
 	private void applyComparator() {
 		if (viewer != null) {
 			final Table table = viewer.getTable();
 			Comparator<Object> compi = null;
 			if (sortedColumn != -1) {
 				final Integer key = Integer.valueOf(sortedColumn);
 				compi = comparatorMap.get(key);
 			}
 			if (compi != null) {
 				final TableColumn column = table.getColumn(sortedColumn);
 				table.setSortColumn(column);
 				final int direction = isSortedAscending ? SWT.UP : SWT.DOWN;
 				table.setSortDirection(direction);
 				final SortableComparator sortableComparator = new SortableComparator(this, compi);
 				viewer.setComparator(new TableComparator(sortableComparator));
 			} else {
 				viewer.setComparator(null);
 				table.setSortColumn(null);
 				table.setSortDirection(SWT.NONE);
 			}
 		}
 	}
 
 	private void applyTableColumnHeaders(final Table control) {
 		final boolean headersVisible = columnHeaders != null;
 		control.setHeaderVisible(headersVisible);
 		if (headersVisible) {
 			final TableColumn[] columns = control.getColumns();
 			for (int i = 0; i < columns.length; i++) {
 				String columnHeader = ""; //$NON-NLS-1$
 				if (i < columnHeaders.length && columnHeaders[i] != null) {
 					columnHeader = columnHeaders[i];
 				}
 				columns[i].setText(columnHeader);
 				final String tooltip = isShowColumnTooltip(columns[i], columnHeader) ? columnHeader : ""; //$NON-NLS-1$
 				columns[i].setToolTipText(tooltip);
 			}
 		}
 	}
 
 	/**
 	 * Returns the width of the table column in pixel.
 	 * 
 	 * @param control
 	 * @param str
 	 * @return
 	 */
 	private int columnTextWidth(final TableColumn control, final String str) {
 		final GC g = new GC(control.getParent());
 		final Font of = g.getFont();
 		g.setFont(control.getParent().getFont());
 		final Point extent = g.stringExtent(str);
 		g.setFont(of);
 		g.dispose();
 		// TODO check if offset in table column differs on various platforms
 		return extent.x + 16;
 	}
 
 	private boolean isShowColumnTooltip(final TableColumn col, final String columnText) {
 		if (RAPDetector.isRAPavailable()) {
 			return false;
 		}
 
 		return col.getWidth() < columnTextWidth(col, columnText);
 	}
 
 	private void checkColumnRange(final int columnIndex) {
 		final Table table = getUIControl();
 		if (table != null) {
 			final int range = table.getColumnCount();
 			final String msg = "columnIndex out of range (0 - " + range + " ): " + columnIndex; //$NON-NLS-1$ //$NON-NLS-2$
 			Assert.isLegal(-1 < columnIndex, msg);
 			Assert.isLegal(columnIndex < range, msg);
 		}
 	}
 
 	private void createMultipleSelectionBinding() {
 		if (viewerMSB == null && dbc != null && viewer != null) {
 			final StructuredSelection currentSelection = new StructuredSelection(getSelection());
 			final IViewerObservableList viewerSelections = ViewersObservables.observeMultiSelection(viewer);
 			viewerMSB = dbc.bindList(viewerSelections, getMultiSelectionObservable(), new UpdateListStrategy(
 					UpdateListStrategy.POLICY_UPDATE), new UpdateListStrategy(UpdateListStrategy.POLICY_UPDATE));
 			viewer.setSelection(currentSelection);
 		}
 	}
 
 	private void configureControl(final Table control) {
 		if (renderingMethods != null) {
 			applyColumns(control);
 		}
 		applyColumnsMoveable(control);
 		applyTableColumnHeaders(control);
 		applyComparator();
 	}
 
 	private void configureViewer(final TableViewer viewer) {
 		final ObservableListContentProvider viewerCP = new ObservableListContentProvider();
 		final TableRidgetLabelProvider labelProvider = createLabelProvider(viewerCP);
 		viewer.setLabelProvider(labelProvider);
 		viewer.setContentProvider(viewerCP);
 		viewer.setInput(viewerObservables);
 	}
 
 	/**
 	 * Creates the label provider of this table.
 	 * 
 	 * @param viewerCP
 	 * @return label provider
 	 */
 	private TableRidgetLabelProvider createLabelProvider(final ObservableListContentProvider viewerCP) {
 		IObservableMap[] attrMap;
 		if (AbstractSWTWidgetRidget.isBean(rowClass)) {
 			attrMap = BeansObservables.observeMaps(viewerCP.getKnownElements(), rowClass, renderingMethods);
 		} else {
 			attrMap = PojoObservables.observeMaps(viewerCP.getKnownElements(), rowClass, renderingMethods);
 		}
 		final IColumnFormatter[] formatters = getColumnFormatters(attrMap.length);
 		final TableRidgetLabelProvider labelProvider = new TableRidgetLabelProvider(attrMap, formatters);
 		viewerObservables.addListChangeListener(new IListChangeListener() {
 			public void handleListChange(final ListChangeEvent event) {
 				if ((event.diff == null) && (!event.diff.isEmpty())) {
 					for (final ListDiffEntry diffEntry : event.diff.getDifferences()) {
 						if (!diffEntry.isAddition()) {
 							// an element of the table was removed, 
 							// dispose the according image (stored inside the label provider) 
 							final Object deletedElement = diffEntry.getElement();
 							if (deletedElement != null) {
 								labelProvider.disposeImageOfElement(deletedElement);
 							}
 						}
 					}
 				}
 			}
 		});
 		return labelProvider;
 	}
 
 	private void disposeMultipleSelectionBinding() {
 		if (viewerMSB != null) { // implies dbc != null
 			viewerMSB.dispose();
 			dbc.removeBinding(viewerMSB);
 			viewerMSB = null;
 		}
 	}
 
 	private IColumnFormatter[] getColumnFormatters(final int numColumns) {
 		Assert.isLegal(numColumns >= 0);
 		final IColumnFormatter[] result = new IColumnFormatter[numColumns];
 		for (int i = 0; i < numColumns; i++) {
 			final IColumnFormatter columnFormatter = formatterMap.get(Integer.valueOf(i));
 			if (columnFormatter != null) {
 				result[i] = columnFormatter;
 			}
 		}
 		return result;
 	}
 
 	private boolean isViewerConfigured() {
 		return viewer.getLabelProvider() instanceof TableRidgetLabelProvider;
 	}
 
 	private void refreshViewer(final TableViewer viewer) {
 		final IBaseLabelProvider labelProvider = viewer.getLabelProvider();
 		if (labelProvider != null) {
 			((TableRidgetLabelProvider) labelProvider).disposeImages();
 		}
 		viewer.getControl().setRedraw(false); // prevent flicker during update
 		final StructuredSelection currentSelection = new StructuredSelection(getSelection());
 		try {
 			final TableRidgetLabelProvider tableLabelProvider = (TableRidgetLabelProvider) labelProvider;
 			final IColumnFormatter[] formatters = getColumnFormatters(tableLabelProvider.getColumnCount());
 			tableLabelProvider.setFormatters(formatters);
 			viewer.setInput(viewerObservables);
 		} finally {
 			viewer.setSelection(currentSelection);
 			viewer.getControl().setRedraw(true);
 		}
 	}
 
 	// helping classes
 	// ////////////////
 
 	/**
 	 * Enforces selection in the control:
 	 * <ul>
 	 * <li>disallows selection changes when the ridget is "output only"</li>
 	 * <li>disallows multiple selection is the selection type of the ridget is
 	 * {@link ISelectableRidget.SelectionType#SINGLE}</li>
 	 * </ul>
 	 */
 	private final class SelectionTypeEnforcer extends SelectionAdapter {
 		@Override
 		public void widgetSelected(final SelectionEvent e) {
 			if (isOutputOnly()) {
 				// undo user selection when "output only"
 				viewer.setSelection(new StructuredSelection(getSelection()));
 			} else if (SelectionType.SINGLE.equals(getSelectionType())) {
 				final Table control = (Table) e.widget;
 				if (control.getSelectionCount() > 1) {
 					// ignore this event
 					e.doit = false;
 					// set selection to most recent item
 					control.setSelection(control.getSelectionIndex());
 					// fire event
 					final Event event = new Event();
 					event.type = SWT.Selection;
 					event.doit = true;
 					control.notifyListeners(SWT.Selection, event);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Erase listener for custom painting of table cells. It is responsible
 	 * for:[
 	 * <ul>
 	 * <li>erasing (emptying) all cells when this ridget is disabled and
 	 * {@link LnfKeyConstants#DISABLED_MARKER_HIDE_CONTENT} is true</li>
 	 * <li>drawing a red border around cells that have been marked with a
 	 * {@link RowErrorMessageMarker} (unless disabled)</li>
 	 * </ul>
 	 * 
 	 * @see '<a href=
 	 *      "http://www.eclipse.org/articles/article.php?file=Article-CustomDrawingTableAndTreeItems/index.html"
 	 *      >Custom Drawing Table and Tree Items</a>'
 	 */
 	private final class TableItemEraser implements Listener {
 
 		private final Color borderColor;
 		private final int borderThickness;
 
 		public TableItemEraser() {
 			borderColor = LnfManager.getLnf().getColor(LnfKeyConstants.ERROR_MARKER_BORDER_COLOR);
 			borderThickness = LnfManager.getLnf().getIntegerSetting(LnfKeyConstants.ROW_ERROR_MARKER_BORDER_THICKNESS,
 					1);
 		}
 
 		/*
 		 * Called EXTREMELY frequently. Must be as efficient as possible.
 		 */
 		public void handleEvent(final Event event) {
 			if (isHidingWhenDisabled()) {
 				hideContent(event);
 			} else {
 				if (isMarked(event.item)) {
 					markRow(event);
 				}
 			}
 		}
 
 		// helping methods
 		//////////////////
 
 		private void hideContent(final Event event) {
 			// we indicate custom fg drawing, but don't draw foreground => hide
 			event.detail &= ~SWT.FOREGROUND;
 		}
 
 		private boolean isHidingWhenDisabled() {
 			return !isEnabled() && MarkerSupport.isHideDisabledRidgetContent();
 		}
 
 		private boolean isMarked(final Widget item) {
 			final Object data = item.getData();
 			final Collection<RowErrorMessageMarker> markers = getMarkersOfType(RowErrorMessageMarker.class);
 			for (final RowErrorMessageMarker marker : markers) {
 				if (marker.getRowValue() == data) {
 					return true;
 				}
 			}
 			return false;
 		}
 
 		private void markRow(final Event event) {
 			final GC gc = event.gc;
 			final Color oldForeground = gc.getForeground();
 			gc.setForeground(borderColor);
 			try {
 				int x = 0, y = 0, width = 0, height = 0;
 				final Table table = (Table) event.widget;
 				final int colCount = table.getColumnCount();
 				if (colCount > 0) {
 					final TableItem item = (TableItem) event.item;
 					for (int i = 0; i < colCount; i++) {
 						final Rectangle bounds = item.getBounds(i);
 						if (i == 0) {
 							// start 3px to the left of first column
 							x = bounds.x - 3;
 							y = bounds.y;
 							width += 3;
 						}
 						width += bounds.width;
 						height = Math.max(height, bounds.height);
 					}
 					width = Math.max(0, width - 1);
 					height = Math.max(0, height - 1);
 				} else {
 					width = Math.max(0, event.width - 1);
 					height = Math.max(0, event.height - 1);
 					x = event.x;
 					y = event.y;
 				}
 				for (int i = 0; i < borderThickness; i++) {
 					int arc = 3;
 					if (i > 0) {
 						arc = 0;
 					}
 					gc.drawRoundRectangle(x + i, y + i, width - 2 * i, height - 2 * i, arc, arc);
 				}
 			} finally {
 				gc.setForeground(oldForeground);
 			}
 		}
 	}
 
 	/**
 	 * Shows the appropriate tooltip (error tooltip / regular tooltip / no
 	 * tooltip) for the current hovered row.
 	 */
 	private final class TableTooltipManager extends MouseTrackAdapter implements MouseMoveListener {
 
 		private String defaultToolTip;
 
 		public void init(final Table table) {
 			final String tableToolTip = table.getToolTipText();
 			defaultToolTip = tableToolTip != null ? tableToolTip : ""; //$NON-NLS-1$
 		}
 
 		public void mouseMove(final MouseEvent event) {
 			final Table table = (Table) event.widget;
 			hideToolTip(table);
 		}
 
 		@Override
 		public void mouseExit(final MouseEvent event) {
 			final Table table = (Table) event.widget;
 			resetToolTip(table);
 		}
 
 		@Override
 		public void mouseHover(final MouseEvent event) {
 			final Table table = (Table) event.widget;
 			final Point mousePt = new Point(event.x, event.y);
 			final TableItem item = table.getItem(mousePt);
 			final String errorToolTip = getErrorToolTip(item);
 			final String itemToolTip = getItemToolTip(item, mousePt);
 			if (!StringUtils.isEmpty(errorToolTip)) {
 				table.setToolTipText(errorToolTip);
 			} else if (!StringUtils.isEmpty(itemToolTip)) {
 				table.setToolTipText(itemToolTip);
 			} else {
 				resetToolTip(table);
 			}
 		}
 
 		// helping methods
 		//////////////////
 
 		private String getErrorToolTip(final TableItem item) {
 			if (item != null) {
 				final Object data = item.getData();
 				final Collection<RowErrorMessageMarker> markers = getMarkersOfType(RowErrorMessageMarker.class);
 				for (final RowErrorMessageMarker marker : markers) {
 					if (marker.getRowValue() == data) {
 						return marker.getMessage();
 					}
 				}
 			}
 			return null;
 		}
 
 		private String getItemToolTip(final TableItem item, final Point mousePt) {
 			String result = null;
 			if (item != null) {
 				final int column = SwtUtilities.findColumn(item.getParent(), mousePt);
 				if (column != -1) {
 					final IBaseLabelProvider labelProvider = viewer.getLabelProvider();
 					if (labelProvider != null) {
 						final Object element = item.getData();
 						result = ((TableRidgetLabelProvider) labelProvider).getToolTipText(element, column);
 					}
 					if (result == null) {
 						result = item.getText(column);
 					}
 				}
 			}
 			return result;
 		}
 
 		private void hideToolTip(final Table table) {
 			if (!"".equals(table.getToolTipText())) { //$NON-NLS-1$
 				table.setToolTipText(""); //$NON-NLS-1$
 			}
 		}
 
 		private void resetToolTip(final Table table) {
 			if (table.getToolTipText() == null || !table.getToolTipText().equals(defaultToolTip)) {
 				table.setToolTipText(defaultToolTip);
 			}
 		}
 	}
 
 	/**
 	 * Notifies single- and double-click listeners when the bound widget is
 	 * clicked.
 	 */
 	private final class ClickForwarder extends MouseAdapter {
 
 		@Override
 		public void mouseDown(final MouseEvent e) {
 			if (clickListeners != null) {
 				final ClickEvent event = createClickEvent(e);
 				for (final IClickListener listener : clickListeners.getListeners()) {
 					listener.callback(event);
 				}
 			}
 		}
 
 		@Override
 		public void mouseDoubleClick(final MouseEvent e) {
 			if (doubleClickListeners != null) {
 				for (final IActionListener listener : doubleClickListeners.getListeners()) {
 					listener.callback();
 				}
 			}
 		}
 
 		// helping methods
 		//////////////////
 
 		private ClickEvent createClickEvent(final MouseEvent e) {
 			final Table table = (Table) e.widget;
 			final int colIndex = SwtUtilities.findColumn(table, new Point(e.x, e.y));
 			// x = 0 gets us an item even not using SWT.FULL_SELECTION
 			final TableItem item = table.getItem(new Point(0, e.y));
 			final Object rowData = item != null ? item.getData() : null;
 			final ClickEvent event = new ClickEvent(colIndex, e.button, rowData);
 			return event;
 		}
 	}
 
 	/**
 	 * Selection listener for table headers that changes the sort order of a
 	 * column according to the information stored in the ridget.
 	 */
 	private final class ColumnSortListener extends SelectionAdapter {
 		@Override
 		public void widgetSelected(final SelectionEvent e) {
 			final TableColumn column = (TableColumn) e.widget;
 			final int columnIndex = column.getParent().indexOf(column);
 			final int direction = column.getParent().getSortDirection();
 			if (columnIndex == sortedColumn) {
 				if (direction == SWT.UP) {
 					setSortedAscending(false);
 				} else if (direction == SWT.DOWN) {
 					setSortedColumn(-1);
 				}
 			} else if (isColumnSortable(columnIndex)) {
 				setSortedColumn(columnIndex);
 				if (direction == SWT.NONE) {
 					setSortedAscending(true);
 				}
 			}
 			column.getParent().showSelection();
 		}
 	}
 
 }
