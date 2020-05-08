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
 
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.Map;
 
 import javax.swing.ToolTipManager;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.jface.viewers.AbstractTableViewer;
 import org.eclipse.jface.viewers.IBaseLabelProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.ViewerColumn;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.ControlListener;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseMoveListener;
 import org.eclipse.swt.events.MouseTrackAdapter;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Item;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.Widget;
 
 import org.eclipse.riena.core.util.RAPDetector;
 import org.eclipse.riena.core.util.StringUtils;
 import org.eclipse.riena.ui.core.marker.RowErrorMessageMarker;
 import org.eclipse.riena.ui.ridgets.IColumnFormatter;
 import org.eclipse.riena.ui.ridgets.listener.ClickEvent;
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
 public class TableRidget extends AbstractTableRidget {
 
 	private TableTooltipManager tooltipManager;
 	private ControlListener columnResizeListener;
 	private final Listener itemEraser;
 
 	public TableRidget() {
 		super();
 		sortListener = new ColumnSortListener();
 		itemEraser = new TableItemEraser();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * <p>
 	 * The given {@code uiControl} must be a {@link Table}.
 	 */
 	@Override
 	protected final void checkUIControl(final Object uiControl) {
 		checkType(uiControl, Table.class);
 	}
 
 	@Override
 	protected final void bindUIControl() {
 		super.bindUIControl();
 		final Table control = getUIControl();
 		if (control != null) {
 
 			columnResizeListener = new ControlListener() {
 				public void controlResized(final ControlEvent e) {
 					applyTableColumnHeaders();
 				}
 
 				public void controlMoved(final ControlEvent e) {
 					applyTableColumnHeaders();
 				}
 			};
 
 			for (final TableColumn column : control.getColumns()) {
 				column.addSelectionListener(sortListener);
 				column.addControlListener(columnResizeListener);
 			}
 			control.addSelectionListener(selectionTypeEnforcer);
 			final SWTFacade facade = SWTFacade.getDefault();
 			facade.addEraseItemListener(control, itemEraser);
 		}
 	}
 
 	@Override
 	protected final void unbindUIControl() {
 		super.unbindUIControl();
 		final Table control = getUIControl();
 		if (control != null) {
 			for (final TableColumn column : control.getColumns()) {
 				column.removeSelectionListener(sortListener);
 				column.removeControlListener(columnResizeListener);
 			}
 			final SWTFacade facade = SWTFacade.getDefault();
 			facade.removeEraseItemListener(control, itemEraser);
 			if (tooltipManager != null) {
 				facade.removeMouseTrackListener(control, tooltipManager);
 				facade.removeMouseMoveListener(control, tooltipManager);
 			}
 		}
 	}
 
 	@Override
 	protected final int getUiSelectionCount() {
 		final Table control = getUIControl();
 		return control == null ? -1 : control.getSelectionCount();
 	}
 
 	//	@Override
 	//	protected final int getUiSelectionIndex() {
 	//		final Table control = getUIControl();
 	//		return control == null ? -1 : control.getSelectionIndex();
 	//	}
 	//
 	//	@Override
 	//	protected final void setUiSelection(final int index) {
 	//		final Table control = getUIControl();
 	//		if (control == null) {
 	//			return;
 	//		}
 	//		control.setSelection(index);
 	//	}
 
 	@Override
 	protected final void setUiSelection(final Widget item) {
 		Assert.isTrue(item instanceof TableItem);
 		final Table control = getUIControl();
 		if (control != null) {
 			control.setSelection((TableItem) item);
 		}
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
 
 	@Override
 	public Table getUIControl() {
 		return (Table) super.getUIControl();
 	}
 
 	@Override
 	protected TableViewer getTableViewer() {
 		return (TableViewer) super.getTableViewer();
 	}
 
 	@Override
 	protected final void applyColumns() {
 		final Table control = getUIControl();
 		if (control == null) {
 			return;
 		}
 		final int expectedCols = getExpectedColumnCount();
 		if (getColumnCount() != expectedCols) {
 			for (final TableColumn column : control.getColumns()) {
 				column.dispose();
 			}
 			for (int i = 0; i < expectedCols; i++) {
 				new TableColumn(control, SWT.NONE);
 			}
 			applyColumnWidths();
 		}
 		final TableColumn[] columns = control.getColumns();
 		for (int columnIndex = 0; columnIndex < columns.length; columnIndex++) {
 			final ViewerColumn viewerColumn = new TableViewerColumn(getTableViewer(), columns[columnIndex]);
 			applyEditingSupport(viewerColumn, columnIndex);
 		}
 	}
 
 	@Override
 	protected final void applyColumnsMovable() {
 		final Table control = getUIControl();
 		if (control == null) {
 			return;
 		}
 		for (final TableColumn column : control.getColumns()) {
 			column.setMoveable(hasMoveableColumns());
 		}
 	}
 
 	@Override
 	protected final void applyComparator(final Map<Integer, Comparator<?>> comparatorMap) {
 		if (getTableViewer() != null) {
 			Comparator<?> compi = null;
 			if (getSortedColumn() != -1) {
 				final Integer key = Integer.valueOf(getSortedColumn());
 				compi = comparatorMap.get(key);
 			}
 			final Table table = getUIControl();
 			if (compi != null) {
 				final TableColumn column = table.getColumn(getSortedColumn());
 				table.setSortColumn(column);
 				final int direction = getSortDirection();
 				table.setSortDirection(direction);
 				final SortableComparator sortableComparator = new SortableComparator(this, compi);
 				getTableViewer().setComparator(new TableComparator(sortableComparator));
 			} else {
 				getTableViewer().setComparator(null);
 				table.setSortColumn(null);
 				table.setSortDirection(SWT.NONE);
 			}
 		}
 	}
 
 	@Override
 	protected final void applyTableColumnHeaders() {
 		final Table control = getUIControl();
 		if (control == null) {
 			return;
 		}
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
 
 	@Override
 	protected int getColumnStyle(final int columnIndex) {
 		checkColumnRange(columnIndex);
 		final Table control = getUIControl();
 		if (control == null) {
 			return SWT.DEFAULT;
 		}
 		final TableColumn[] columns = control.getColumns();
 		return columns[columnIndex].getStyle();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * <p>
 	 * This TableRidget provides two different tool tip supports.
 	 * <ul>
 	 * <li>native: The inner class {@link ToolTipManager} shows appropriate tool
 	 * tips (cell item text, error marker text, etc.) that looks like native
 	 * tool tips.</li>
 	 * <li>JFace: The class {@link TableRidgetToolTipSupportFacade} shows also
 	 * appropriate tool tips. But the look of the tool tips can be configured
 	 * (see {@link IColumnFormatter}). Also images can be displayed.</li>
 	 * </ul>
 	 */
 	@Override
 	protected final void updateToolTipSupport() {
 		final SWTFacade facade = SWTFacade.getDefault();
 		if (isNativeToolTip() || !TableRidgetToolTipSupportFacade.getDefault().isSupported()) {
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
 			if (getTableViewer() instanceof TableRidgetTableViewer) {
 				TableRidgetToolTipSupportFacade.getDefault().enableFor(getTableViewer());
 			}
 		}
 	}
 
 	@Override
 	protected AbstractTableViewer createTableViewer() {
 		return new TableRidgetTableViewer(this);
 	}
 
 	@Override
 	protected TableWrapper createTableWrapper() {
 		Assert.isNotNull(getUIControl());
 		return new TableWrapper(getUIControl());
 	}
 
 	@Override
 	protected ClickEvent createClickEvent(final MouseEvent e) {
 		final Table table = (Table) e.widget;
 		final int colIndex = SwtUtilities.findColumn(table, new Point(e.x, e.y));
 		// x = 0 gets us an item even not using SWT.FULL_SELECTION
 		final Item item = getItem(new Point(0, e.y));
 		final Object rowData = item != null ? item.getData() : null;
 		final ClickEvent event = new ClickEvent(this, e.button, colIndex, rowData);
 		return event;
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
 
 	// helping classes
 	// ////////////////
 
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
 			if (columnIndex == getSortedColumn()) {
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
 
 	/**
 	 * Shows the appropriate tooltip (error tooltip / regular tooltip / no
 	 * tooltip) for the current hovered row.
 	 */
 	private final class TableTooltipManager extends MouseTrackAdapter implements MouseMoveListener {
 
 		private String defaultToolTip;
 
 		public void init(final Control table) {
 			final String tableToolTip = table.getToolTipText();
 			defaultToolTip = tableToolTip != null ? tableToolTip : ""; //$NON-NLS-1$
 		}
 
 		public void mouseMove(final MouseEvent event) {
 			final Control table = (Control) event.widget;
 			hideToolTip(table);
 		}
 
 		@Override
 		public void mouseExit(final MouseEvent event) {
 			final Control table = (Control) event.widget;
 			resetToolTip(table);
 		}
 
 		@Override
 		public void mouseHover(final MouseEvent event) {
 			String errorToolTip = ""; //$NON-NLS-1$
 			String itemToolTip = ""; //$NON-NLS-1$
 			final Point mousePt = new Point(event.x, event.y);
 			final TableItem item = getItem(mousePt);
 			if (item != null) {
 				errorToolTip = getErrorToolTip(item);
 				itemToolTip = getItemToolTip(item, mousePt);
 			}
 			final Control table = (Control) event.widget;
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
 
 		private TableItem getItem(final Point point) {
 			final Table control = getUIControl();
 			if (control == null) {
 				return null;
 			}
 			return control.getItem(point);
 		}
 
 		private String getItemToolTip(final TableItem item, final Point mousePt) {
 			String result = null;
 			final int column = SwtUtilities.findColumn(item.getParent(), mousePt);
 			if (column != -1) {
 				final IBaseLabelProvider labelProvider = getTableViewer().getLabelProvider();
 				if (labelProvider != null) {
 					final Object element = item.getData();
 					result = ((TableRidgetLabelProvider) labelProvider).getToolTipText(element, column);
 				}
 				if (result == null) {
 					result = item.getText(column);
 				}
 			}
 			return result;
 		}
 
 		private String getErrorToolTip(final Item item) {
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
 
 		private void hideToolTip(final Control table) {
 			if (!"".equals(table.getToolTipText())) { //$NON-NLS-1$
 				table.setToolTipText(""); //$NON-NLS-1$
 			}
 		}
 
 		private void resetToolTip(final Control table) {
 			if (table.getToolTipText() == null || !table.getToolTipText().equals(defaultToolTip)) {
 				table.setToolTipText(defaultToolTip);
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
 				if (isErrorMarked(event.item)) {
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
 
 		private void markRow(final Event event) {
 			final GC gc = event.gc;
 			final Color oldForeground = gc.getForeground();
 			gc.setForeground(borderColor);
 			try {
 				int x = 0, y = 0, width = 0, height = 0;
 				final int colCount = getColumnCount();
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
 
 }
