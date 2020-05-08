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
 
 import org.eclipse.core.databinding.observable.map.IObservableMap;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
 import org.eclipse.jface.viewers.ITableColorProvider;
 import org.eclipse.jface.viewers.ITableFontProvider;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.Image;
 
 import org.eclipse.riena.ui.ridgets.IColumnFormatter;
 
 /**
  * Label provider that shows checked / unchecked icons, if the attribute in a
  * column is of Boolean type.
  * 
  * @see TableRidget
  */
 public class TableRidgetLabelProvider extends ObservableMapLabelProvider implements ITableColorProvider,
 		ITableFontProvider {
 
 	private final int numColumns;
 	private final IObservableMap[] attributeMap;
 	private IColumnFormatter[] formatters;
 
 	/**
 	 * Create a new instance
 	 * 
 	 * @param viewer
 	 *            a non-null {@link TreeViewer} instance
 	 * @param attributeMap
 	 *            a non-null {@link IObservableMap} instance
 	 * @param formatters
 	 *            an array of objects that implement {@link IColumnFormatter}.
 	 *            The array must have the same number of entries as
 	 *            attributeMap, however individual entries can be null.
 	 * @throws RuntimeException
 	 *             if attributeMap and labelProviders have not the same number
 	 *             of entries
 	 */
 	public TableRidgetLabelProvider(IObservableMap[] attributeMap, IColumnFormatter[] formatters) {
 		this(attributeMap, formatters, attributeMap.length);
 	}
 
 	protected TableRidgetLabelProvider(IObservableMap[] attributeMap, IColumnFormatter[] formatters, int numColumns) {
 		super(attributeMap);
 		Assert.isLegal(numColumns == formatters.length, String.format("expected %d formatters, got %d", numColumns, //$NON-NLS-1$
 				formatters.length));
 		this.numColumns = numColumns;
 		this.attributeMap = new IObservableMap[attributeMap.length];
 		System.arraycopy(attributeMap, 0, this.attributeMap, 0, this.attributeMap.length);
 		this.formatters = new IColumnFormatter[formatters.length];
 		System.arraycopy(formatters, 0, this.formatters, 0, this.formatters.length);
 	}
 
 	@Override
 	public Image getImage(Object element) {
 		return getColumnImage(element, 0);
 	}
 
 	@Override
 	public Image getColumnImage(Object element, int columnIndex) {
 		Image result = null;
 		if (columnIndex < attributeMap.length) {
 			IColumnFormatter formatter = this.formatters[columnIndex];
 			if (formatter != null) {
 				result = (Image) formatter.getImage(element);
			}
			if (result == null) {
 				Object value = attributeMap[columnIndex].get(element);
 				if (value instanceof Boolean) {
 					String key = ((Boolean) value).booleanValue() ? SharedImages.IMG_CHECKED
 							: SharedImages.IMG_UNCHECKED;
 					result = Activator.getSharedImage(key);
 				}
 			}
 		}
 		return result;
 	}
 
 	@Override
 	public String getColumnText(Object element, int columnIndex) {
 		String result = null;
 		if (columnIndex < formatters.length) {
 			IColumnFormatter formatter = this.formatters[columnIndex];
 			if (formatter != null) {
 				result = formatter.getText(element);
 			}
 		}
 		if (result == null) {
 			result = super.getColumnText(element, columnIndex);
 		}
 		return result;
 	}
 
 	public Color getForeground(Object element, int columnIndex) {
 		if (columnIndex < formatters.length) {
 			IColumnFormatter formatter = this.formatters[columnIndex];
 			if (formatter != null) {
 				return (Color) formatter.getForeground(element);
 			}
 		}
 		return null;
 	}
 
 	public Color getBackground(Object element, int columnIndex) {
 		if (columnIndex < formatters.length) {
 			IColumnFormatter formatter = this.formatters[columnIndex];
 			if (formatter != null) {
 				return (Color) formatter.getBackground(element);
 			}
 		}
 		return null;
 	}
 
 	public Font getFont(Object element, int columnIndex) {
 		if (columnIndex < formatters.length) {
 			IColumnFormatter formatter = this.formatters[columnIndex];
 			if (formatter != null) {
 				return (Font) formatter.getFont(element);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the value of the given element at the specified column index.
 	 * 
 	 * @param element
 	 * @param columnIndex
 	 *            column index
 	 * @return value or {@code null} if column index is not correct
 	 */
 	public Object getColumnValue(Object element, int columnIndex) {
 		if (columnIndex < attributeMap.length) {
 			return attributeMap[columnIndex].get(element);
 		}
 		return null;
 	}
 
 	// protected methods
 	////////////////////
 
 	protected IColumnFormatter getFormatter(int columnIndex) {
 		return columnIndex < formatters.length ? formatters[columnIndex] : null;
 	}
 
 	// helping methods
 	//////////////////
 
 	int getColumnCount() {
 		return this.formatters.length;
 	}
 
 	void setFormatters(IColumnFormatter[] formatters) {
 		Assert.isLegal(numColumns == formatters.length, String.format("expected %d formatters, got %d", numColumns, //$NON-NLS-1$
 				formatters.length));
 		this.formatters = new IColumnFormatter[formatters.length];
 		System.arraycopy(formatters, 0, this.formatters, 0, this.formatters.length);
 	}
 
 }
