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
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.jface.layout.TableColumnLayout;
 import org.eclipse.jface.layout.TreeColumnLayout;
 import org.eclipse.jface.util.Util;
 import org.eclipse.jface.viewers.ColumnLayoutData;
 import org.eclipse.jface.viewers.ColumnPixelData;
 import org.eclipse.jface.viewers.ColumnWeightData;
 import org.eclipse.jface.viewers.TableLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.Tree;
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
 	public static void applyColumnWidths(final Table control, final ColumnLayoutData[] columnWidths) {
 		Assert.isNotNull(control);
 		applyColumnWidths(new TableWrapper(control), columnWidths);
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
 	public static void applyColumnWidths(final Tree control, final ColumnLayoutData[] columnWidths) {
 		Assert.isNotNull(control);
 		applyColumnWidths(new TreeWrapper(control), columnWidths);
 	}
 
 	public static void applyColumnWidths(final ITableTreeWrapper controlWrapper, final ColumnLayoutData[] columnWidths) {
 		Assert.isNotNull(controlWrapper);
 		applyColumnWidths(controlWrapper, columnWidths, controlWrapper.getColumnCount());
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
 	public static ColumnLayoutData[] copyWidths(final Object[] source) {
 		ColumnLayoutData[] result = null;
 		if (source != null) {
 			result = new ColumnLayoutData[source.length];
 			for (int i = 0; i < source.length; i++) {
 				if (source[i] instanceof ColumnPixelData) {
 					final ColumnPixelData data = (ColumnPixelData) source[i];
 					result[i] = new ColumnPixelData(data.width, data.resizable, data.addTrim);
 				} else if (source[i] instanceof ColumnWeightData) {
 					final ColumnWeightData data = (ColumnWeightData) source[i];
 					result[i] = new ColumnWeightData(data.weight, data.minimumWidth, data.resizable);
 				} else {
 					final String msg = String.format("Unsupported type in column #%d: %s", i, source[i]); //$NON-NLS-1$
 					throw new IllegalArgumentException(msg);
 				}
 			}
 		}
 		return result;
 	}
 
 	// helping methods
 	//////////////////
 
 	private static void applyColumnWidths(final ITableTreeWrapper controlWrapper,
 			final ColumnLayoutData[] columnWidths, final int expectedCols) {
 		final ColumnLayoutData[] columnData;
 		if (columnWidths == null || columnWidths.length != expectedCols) {
 			columnData = new ColumnLayoutData[expectedCols];
 			for (int i = 0; i < expectedCols; i++) {
 				columnData[i] = new ColumnWeightData(1, true);
 			}
 		} else {
 			columnData = columnWidths;
 		}
 		final Composite control = controlWrapper.getControl();
 		final Composite parent = control.getParent();
 		if (control.getLayout() instanceof TableLayout) {
 			// TableLayout: use columnData instance for each column, apply to control
 			final TableLayout layout = new TableLayout();
 			for (int index = 0; index < expectedCols; index++) {
 				layout.addColumnData(columnData[index]);
 			}
 			control.setLayout(layout);
 			parent.layout(true, true);
 		} else if ((control instanceof Tree && control.getLayout() == null && parent.getLayout() == null && parent
 				.getChildren().length == 1) || parent.getLayout() instanceof TreeColumnLayout) {
 			// TreeColumnLayout: use columnData instance for each column, apply to parent
 			final TreeColumnLayout layout = getOrCreateTreeColumnLayout(parent);
 			for (int index = 0; index < expectedCols; index++) {
 				final Widget column = controlWrapper.getColumn(index);
 				layout.setColumnData(column, columnData[index]);
 			}
 			parent.setLayout(layout);
 			parent.layout();
 		} else if ((control instanceof Table && control.getLayout() == null && parent.getLayout() == null && parent
 				.getChildren().length == 1) || parent.getLayout() instanceof TableColumnLayout) {
 			// TableColumnLayout: use columnData instance for each column, apply to parent
 			final TableColumnLayout layout = getOrCreateTableColumnLayout(parent);
 			for (int index = 0; index < expectedCols; index++) {
 				final Widget column = controlWrapper.getColumn(index);
 				layout.setColumnData(column, columnData[index]);
 			}
 			parent.setLayout(layout);
 			parent.layout();
 		} else {
 			// Other: manually compute width for each columnm, apply to TableColumn
 			// 1. absolute widths: apply absolute widths first
 			// 2. relative widths:
 			//    compute remaining width and total weight; for each column: apply
 			//    the largest value of either the relative width or the minimum width
 			int widthRemaining = control.getClientArea().width;

			// Workaround for Bug 301682
			if (widthRemaining == 0) {
				parent.layout();
				widthRemaining = control.getClientArea().width;
			}
 			int totalWeights = 0;
 			for (int index = 0; index < expectedCols; index++) {
 				final ColumnLayoutData data = columnData[index];
 				if (data instanceof ColumnPixelData) {
 					final ColumnPixelData pixelData = (ColumnPixelData) data;
 					int width = pixelData.width;
 					if (pixelData.addTrim) {
 						width = width + getColumnTrim();
 					}
 					configureColumn(controlWrapper, index, width, data.resizable);
 					widthRemaining = widthRemaining - width;
 				} else if (data instanceof ColumnWeightData) {
 					totalWeights = totalWeights + ((ColumnWeightData) data).weight;
 				}
 			}
 			final int slice = totalWeights > 0 ? Math.max(0, widthRemaining / totalWeights) : 0;
 			for (int index = 0; index < expectedCols; index++) {
 				if (columnData[index] instanceof ColumnWeightData) {
 					final ColumnWeightData data = (ColumnWeightData) columnData[index];
 					final int width = Math.max(data.minimumWidth, data.weight * slice);
 					configureColumn(controlWrapper, index, width, data.resizable);
 				}
 			}
 		}
 	}
 
 	private static void configureColumn(final ITableTreeWrapper controlWrapper, final int index, final int width,
 			final boolean resizable) {
 		controlWrapper.setWidth(index, width);
 		controlWrapper.setResizable(index, resizable);
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
 	private static TableColumnLayout getOrCreateTableColumnLayout(final Composite parent) {
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
 	private static TreeColumnLayout getOrCreateTreeColumnLayout(final Composite parent) {
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
