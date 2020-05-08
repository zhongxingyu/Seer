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
 
 import org.eclipse.jface.layout.TableColumnLayout;
 import org.eclipse.jface.layout.TreeColumnLayout;
 import org.eclipse.jface.util.Util;
 import org.eclipse.jface.viewers.ColumnLayoutData;
 import org.eclipse.jface.viewers.ColumnPixelData;
 import org.eclipse.jface.viewers.ColumnWeightData;
 import org.eclipse.jface.viewers.TableLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeColumn;
 import org.eclipse.swt.widgets.Widget;
 
 /**
  * Helper class for layouting columns in a Table or Tree.
  */
 public final class ColumnUtils {
 
 	/**
 	 * Adjust the column widths of the given {@link Table}, according to the
 	 * provided {@link ColumnLayoutData} array. The layout managers supported by
 	 * this method are: TableLayout, TableColumnLayout, other.
 	 * <p>
 	 * If the number of entries in {@code columnWidths} does not match the
 	 * number of columns in the widget, the available width will be distributed
 	 * equally to all columns. The same will happen if {@code columnWidths} is
 	 * null. Future width changes are not taken into account.
 	 * <p>
 	 * If the control has a TableLayout, the ColumnLayoutData will be used
 	 * directy. Future width changes are not taken into account.
 	 * <p>
 	 * If the control's parent has a TableColumnLayout, the ColumnLayoutData
 	 * will be used directly. If control and the parent have no layout, and
 	 * parent only contains the control, then a TableColumnLayout is used as
 	 * well. Future width changes ARE taken into account.
 	 * <p>
 	 * In any other case: the available table width <i>at the time when this
 	 * meethod is invoked</i> is distributed directly to the columns (via
 	 * setWidth(...)). Future width changes are not taken into account.
 	 * 
 	 * @param control
 	 *            a Table instance; never null
 	 * @param columnWidths
 	 *            an Array with width information, one instance per column. The
 	 *            array may be null, in that case the available width is
 	 *            distributed equally to all columns
 	 */
 	public static void applyColumnWidths(Table control, ColumnLayoutData[] columnWidths) {
 		applyColumnWidths(control, columnWidths, control.getColumnCount());
 	}
 
 	/**
 	 * Adjust the column widths of the given {@link Tree}, according to the
 	 * provided {@link ColumnLayoutData} array. The layout managers supported by
 	 * this method are: TableLayout, TableColumnLayout, other.
 	 * <p>
 	 * If the number of entries in {@code columnWidths} does not match the
 	 * number of columns in the widget, the available width will be distributed
 	 * equally to all columns. The same will happen if {@code columnWidths} is
 	 * null. Future width changes are not taken into account.
 	 * <p>
 	 * If the control has a TableLayout, the ColumnLayoutData will be used
 	 * directy. Future width changes are not taken into account.
 	 * <p>
 	 * If the control's parent has a TreeColumnLayout, the ColumnLayoutData will
 	 * be used directly. If control and the parent have no layout, and parent
 	 * only contains the control, then a TreeColumnLayout is used as well.
 	 * Future width changes ARE taken into account.
 	 * <p>
 	 * In any other case: the available table width <i>at the time when this
 	 * meethod is invoked</i> is distributed directly to the columns (via
 	 * setWidth(...)). Future width changes are not taken into account.
 	 * 
 	 * @param control
 	 *            a Tree instance; never null
 	 * @param columnWidths
 	 *            an Array with width information, one instance per column. The
 	 *            array may be null, in that case the available width is
 	 *            distributed equally to all columns
 	 */
 	public static void applyColumnWidths(Tree control, ColumnLayoutData[] columnWidths) {
 		applyColumnWidths(control, columnWidths, control.getColumnCount());
 	}
 
 	/**
 	 * Deep copy an array of {@link ColumnLayoutData} instances.
 	 * 
 	 * @param source
 	 *            an Array; may be null
 	 * @return a deep copy of the array or null (if {@code source} is null)
 	 * @throws RuntimeException
 	 *             if the array contains types other than subclasses of
 	 *             {@link ColumnLayoutData}
 	 */
 	public static ColumnLayoutData[] copyWidths(Object[] source) {
 		ColumnLayoutData[] result = null;
 		if (source != null) {
 			result = new ColumnLayoutData[source.length];
 			for (int i = 0; i < source.length; i++) {
 				if (source[i] instanceof ColumnPixelData) {
 					ColumnPixelData data = (ColumnPixelData) source[i];
 					result[i] = new ColumnPixelData(data.width, data.resizable, data.addTrim);
 				} else if (source[i] instanceof ColumnWeightData) {
 					ColumnWeightData data = (ColumnWeightData) source[i];
 					result[i] = new ColumnWeightData(data.weight, data.minimumWidth, data.resizable);
 				} else {
 					String msg = String.format("Unsupported type in column #%d: %s", i, source[i]); //$NON-NLS-1$
 					throw new IllegalArgumentException(msg);
 				}
 			}
 		}
 		return result;
 	}
 
 	// helping methods
 	//////////////////
 
 	private static void applyColumnWidths(Composite control, ColumnLayoutData[] columnWidths, final int expectedCols) {
 		final ColumnLayoutData[] columnData;
 		if (columnWidths == null || columnWidths.length != expectedCols) {
 			columnData = new ColumnLayoutData[expectedCols];
 			for (int i = 0; i < expectedCols; i++) {
 				columnData[i] = new ColumnWeightData(1, true);
 			}
 		} else {
 			columnData = columnWidths;
 		}
 		Composite parent = control.getParent();
 		if (control.getLayout() instanceof TableLayout) {
 			// TableLayout: use columnData instance for each column, apply to control
 			TableLayout layout = new TableLayout();
 			for (int index = 0; index < expectedCols; index++) {
 				layout.addColumnData(columnData[index]);
 			}
 			control.setLayout(layout);
			if (parent.isVisible()) {
				parent.layout(true, true);
			}
 		} else if ((control instanceof Tree && control.getLayout() == null && parent.getLayout() == null && parent
 				.getChildren().length == 1)
 				|| parent.getLayout() instanceof TreeColumnLayout) {
 			// TreeColumnLayout: use columnData instance for each column, apply to parent
 			TreeColumnLayout layout = getOrCreateTreeColumnLayout(parent);
 			for (int index = 0; index < expectedCols; index++) {
 				Widget column = getColumn(control, index);
 				layout.setColumnData(column, columnData[index]);
 			}
 			parent.setLayout(layout);
			if (parent.isVisible()) {
				parent.layout();
			}
 		} else if ((control instanceof Table && control.getLayout() == null && parent.getLayout() == null && parent
 				.getChildren().length == 1)
 				|| parent.getLayout() instanceof TableColumnLayout) {
 			// TableColumnLayout: use columnData instance for each column, apply to parent
 			TableColumnLayout layout = getOrCreateTableColumnLayout(parent);
 			for (int index = 0; index < expectedCols; index++) {
 				Widget column = getColumn(control, index);
 				layout.setColumnData(column, columnData[index]);
 			}
 			parent.setLayout(layout);
			if (parent.isVisible()) {
				parent.layout();
			}
 		} else {
 			// Other: manually compute width for each columnm, apply to TableColumn
 			// 1. absolute widths: apply absolute widths first
 			// 2. relative widths:
 			//    compute remaining width and total weight; for each column: apply
 			//    the largest value of either the relative width or the minimum width
 			int widthRemaining = control.getClientArea().width;
 			int totalWeights = 0;
 			for (int index = 0; index < expectedCols; index++) {
 				ColumnLayoutData data = columnData[index];
 				if (data instanceof ColumnPixelData) {
 					ColumnPixelData pixelData = (ColumnPixelData) data;
 					int width = pixelData.width;
 					if (pixelData.addTrim) {
 						width = width + getColumnTrim();
 					}
 					configureColumn(control, index, width, data.resizable);
 					widthRemaining = widthRemaining - width;
 				} else if (data instanceof ColumnWeightData) {
 					totalWeights = totalWeights + ((ColumnWeightData) data).weight;
 				}
 			}
 			int slice = totalWeights > 0 ? Math.max(0, widthRemaining / totalWeights) : 0;
 			for (int index = 0; index < expectedCols; index++) {
 				if (columnData[index] instanceof ColumnWeightData) {
 					ColumnWeightData data = (ColumnWeightData) columnData[index];
 					int width = Math.max(data.minimumWidth, data.weight * slice);
 					configureColumn(control, index, width, data.resizable);
 				}
 			}
 		}
 	}
 
 	private static void configureColumn(Control control, int index, int width, boolean resizable) {
 		Widget column = getColumn(control, index);
 		if (column instanceof TreeColumn) {
 			((TreeColumn) column).setWidth(width);
 			((TreeColumn) column).setResizable(resizable);
 		} else if (column instanceof TableColumn) {
 			((TableColumn) column).setWidth(width);
 			((TableColumn) column).setResizable(resizable);
 		}
 	}
 
 	private static Widget getColumn(Control control, int index) {
 		if (control instanceof Table) {
 			return ((Table) control).getColumn(index);
 		}
 		if (control instanceof Tree) {
 			return ((Tree) control).getColumn(index);
 		}
 		throw new IllegalArgumentException("unsupported type: " + control); //$NON-NLS-1$
 	}
 
 	private static int getColumnTrim() {
 		int result = 3;
 		if (Util.isWindows()) {
 			result = 4;
 		} else if (Util.isMac()) {
 			result = 24;
 		}
 		return result;
 	}
 
 	/*
 	 * Workaround for Bug 295404 - reusing existing TableColumnLayout
 	 */
 	private static TableColumnLayout getOrCreateTableColumnLayout(Composite parent) {
 		TableColumnLayout result;
 		if (parent.getLayout() instanceof TableColumnLayout) {
 			result = (TableColumnLayout) parent.getLayout();
 		} else {
 			result = new TableColumnLayout();
 		}
 		return result;
 	}
 
 	/*
 	 * Workaround for Bug 295404 - reusing existing TreeColumnLayout
 	 */
 	private static TreeColumnLayout getOrCreateTreeColumnLayout(Composite parent) {
 		TreeColumnLayout result;
 		if (parent.getLayout() instanceof TreeColumnLayout) {
 			result = (TreeColumnLayout) parent.getLayout();
 		} else {
 			result = new TreeColumnLayout();
 		}
 		return result;
 	}
 
 	private ColumnUtils() {
 		// utility class
 	}
 }
