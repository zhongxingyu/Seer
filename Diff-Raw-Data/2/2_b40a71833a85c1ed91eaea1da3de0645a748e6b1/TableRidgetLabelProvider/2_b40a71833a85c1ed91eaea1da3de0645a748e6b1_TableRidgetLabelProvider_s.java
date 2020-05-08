 /*******************************************************************************
  * Copyright (c) 2007, 2014 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.internal.ui.ridgets.swt;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.osgi.service.log.LogService;
 
 import org.eclipse.core.databinding.observable.map.IObservableMap;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.equinox.log.Logger;
 import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
 import org.eclipse.jface.viewers.ITableColorProvider;
 import org.eclipse.jface.viewers.ITableFontProvider;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CLabel;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 
 import org.eclipse.riena.core.Log4r;
 import org.eclipse.riena.internal.ui.swt.utils.RcpUtilities;
 import org.eclipse.riena.ui.ridgets.IColumnFormatter;
 import org.eclipse.riena.ui.ridgets.ITableFormatter;
 import org.eclipse.riena.ui.ridgets.swt.TableFormatter;
 import org.eclipse.riena.ui.swt.utils.SwtUtilities;
 
 /**
  * Label provider that formats the columns of a {@link TableRidget}. {@link IColumnFormatter}s can be used to modify the text, image, foreground color,
  * background color or font of a particular column.
  * <p>
  * The appropriate image for a column is computed in the following fashion:
  * <ul>
  * <li>if a column has a formatter, use the image from the formatter, if not null</li>
  * <li>if the column has a boolean or Boolean value, use the default image for boolean values (i.e. checked / unchecked box)</li>
  * <li>otherwise no image is shown</li>
  * </ul>
  * 
  * @see TableRidget
  */
 public class TableRidgetLabelProvider extends ObservableMapLabelProvider implements ITableColorProvider, ITableFontProvider {
 
 	private final static Logger LOGGER = Log4r.getLogger(TableRidgetLabelProvider.class);
 	private final static ITableFormatter DEFAULT_TABLE_FORMATTER = new TableFormatter();
 
 	private final int numColumns;
 	private IColumnFormatter[] formatters;
 	private ITableFormatter tableFormatter;
 	private Map<Object, Image> imageMap;
 	private boolean checkBoxInFirstColumn;
 
 	/**
 	 * Create a new instance
 	 * 
 	 * @param viewer
 	 *            a non-null {@link TreeViewer} instance
 	 * @param attributeMap
 	 *            a non-null {@link IObservableMap} instance
 	 * @param formatters
 	 *            an array of objects that implement {@link IColumnFormatter}. The array must have the same number of entries as attributeMap, however
 	 *            individual entries can be null.
 	 * @throws RuntimeException
 	 *             if attributeMap and labelProviders have not the same number of entries
 	 */
 	public TableRidgetLabelProvider(final IObservableMap[] attributeMap, final IColumnFormatter[] formatters) {
 		this(attributeMap, formatters, attributeMap.length);
 	}
 
 	protected TableRidgetLabelProvider(final IObservableMap[] attributeMap, final IColumnFormatter[] formatters, final int numColumns) {
 		super(attributeMap);
 		Assert.isLegal(numColumns == formatters.length, String.format("expected %d formatters, got %d", numColumns, //$NON-NLS-1$
 				formatters.length));
 		this.numColumns = numColumns;
 		this.formatters = new IColumnFormatter[formatters.length];
 		System.arraycopy(formatters, 0, this.formatters, 0, this.formatters.length);
 		imageMap = new HashMap<Object, Image>();
 		checkBoxInFirstColumn = false;
 	}
 
 	@Override
 	public Image getImage(final Object element) {
 		return getColumnImage(element, 0);
 	}
 
 	@Override
 	public Image getColumnImage(final Object element, final int columnIndex) {
 
 		Object formatterImage = null;
 		final IColumnFormatter formatter = getFormatter(columnIndex);
 		if (formatter != null) {
 			formatterImage = formatter.getImage(element);
 		} else {
 			formatterImage = getTableFormatter().getImage(element, getColumnValue(element, columnIndex), columnIndex);
 		}
 		Image result = null;
 		if (formatterImage instanceof Image) {
 			result = (Image) formatterImage;
 		} else if (formatterImage instanceof ImageData) {
 			final ImageData formatterImageData = (ImageData) formatterImage;
 			final Display display = getDisplay();
 			if (display != null) {
 				final Image oldImage = imageMap.get(element);
 				if (!SwtUtilities.isDisposed(oldImage)) {
 					oldImage.dispose();
 				}
 				result = new Image(display, formatterImageData);
 				imageMap.put(element, result);
 			}
 		}
 		if (result == null && columnIndex < attributeMaps.length) {
 			final Object value = attributeMaps[columnIndex].get(element);
 			if (value instanceof Boolean) {
 				if ((columnIndex == 0) && isCheckBoxInFirstColumn()) {
 					return null;
 				}
 				final String key = ((Boolean) value).booleanValue() ? SharedImages.IMG_CHECKED : SharedImages.IMG_UNCHECKED;
				result = Activator.getSharedImage(key, true);
 			}
 		}
 		return result;
 	}
 
 	@Override
 	public void dispose() {
 		super.dispose();
 		disposeImages();
 		imageMap = null;
 	}
 
 	/**
 	 * Disposes all the images that this label provider has created.
 	 */
 	public void disposeImages() {
 		for (final Map.Entry<Object, Image> entry : imageMap.entrySet()) {
 			final Image image = entry.getValue();
 			if (!SwtUtilities.isDisposed(image)) {
 				image.dispose();
 			}
 		}
 		imageMap.clear();
 	}
 
 	/**
 	 * Disposes the image of the given element if the image was create from this label provider.
 	 * 
 	 * @param element
 	 *            an element that was deleted
 	 */
 	public void disposeImageOfElement(final Object element) {
 		final Image image = imageMap.get(element);
 		if (image == null) {
 			LOGGER.log(LogService.LOG_WARNING, "No image found for element: " + element.toString()); //$NON-NLS-1$
 			return;
 		}
 		image.dispose();
 		imageMap.remove(element);
 	}
 
 	private Display getDisplay() {
 		final Shell shell = RcpUtilities.getWorkbenchShell();
 		if ((shell == null) || shell.isDisposed()) {
 			LOGGER.log(LogService.LOG_WARNING, "No shell of the application found!"); //$NON-NLS-1$
 			return null;
 		}
 		return shell.getDisplay();
 	}
 
 	@Override
 	public String getColumnText(final Object element, final int columnIndex) {
 		String result = null;
 		final IColumnFormatter formatter = getFormatter(columnIndex);
 		if (formatter != null) {
 			result = formatter.getText(element);
 		} else {
 			result = getTableFormatter().getText(element, getColumnValue(element, columnIndex), columnIndex);
 		}
 		if (result == null) {
 			result = super.getColumnText(element, columnIndex);
 		}
 		final Object value = getColumnValue(element, columnIndex);
 		if (value instanceof Boolean) {
 			if ((columnIndex == 0) && isCheckBoxInFirstColumn()) {
 				return null;
 			}
 		}
 		return result;
 	}
 
 	public Color getForeground(final Object element, final int columnIndex) {
 		final IColumnFormatter formatter = getFormatter(columnIndex);
 		Object color;
 		if (formatter != null) {
 			color = formatter.getForeground(element);
 		} else {
 			color = getTableFormatter().getForeground(element, getColumnValue(element, columnIndex), columnIndex);
 		}
 		if (color instanceof Color) {
 			return (Color) color;
 		}
 		return null;
 	}
 
 	public Color getBackground(final Object element, final int columnIndex) {
 		final IColumnFormatter formatter = getFormatter(columnIndex);
 		Object color;
 		if (formatter != null) {
 			color = formatter.getBackground(element);
 		} else {
 			color = getTableFormatter().getBackground(element, getColumnValue(element, columnIndex), columnIndex);
 		}
 		if (color instanceof Color) {
 			return (Color) color;
 		}
 		return null;
 	}
 
 	public Font getFont(final Object element, final int columnIndex) {
 		final IColumnFormatter formatter = getFormatter(columnIndex);
 		Object font;
 		if (formatter != null) {
 			font = formatter.getFont(element);
 		} else {
 			font = getTableFormatter().getFont(element, getColumnValue(element, columnIndex), columnIndex);
 		}
 		if (font instanceof Font) {
 			return (Font) font;
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
 	public Object getColumnValue(final Object element, final int columnIndex) {
 		if (columnIndex < attributeMaps.length) {
 			return attributeMaps[columnIndex].get(element);
 		}
 		return null;
 	}
 
 	/**
 	 * Get the text displayed in the tool tip for object.
 	 * 
 	 * <p>
 	 * <b>If {@link #getToolTipText(Object)} and {@link #getToolTipImage(Object)} both return <code>null</code> the control is set back to standard behavior</b>
 	 * </p>
 	 * 
 	 * @param element
 	 *            the element for which the tool tip is shown
 	 * @param columnIndex
 	 *            column index for which the tool tip is shown
 	 * @return the {@link String} or <code>null</code> if there is not text to display
 	 */
 	public String getToolTipText(final Object element, final int columnIndex) {
 		final IColumnFormatter formatter = getFormatter(columnIndex);
 		if (formatter != null) {
 			return formatter.getToolTip(element);
 		} else {
 			return getTableFormatter().getToolTip(element, getColumnValue(element, columnIndex), columnIndex);
 		}
 	}
 
 	/**
 	 * Get the image displayed in the tool tip for object.
 	 * 
 	 * <p>
 	 * <b>If {@link #getToolTipText(Object)} and {@link #getToolTipImage(Object)} both return <code>null</code> the control is set back to standard behavior</b>
 	 * </p>
 	 * 
 	 * @param object
 	 *            the element for which the tool tip is shown
 	 * @param columnIndex
 	 *            column index for which the tool tip is shown
 	 * @return {@link Image} or <code>null</code> if there is not image.
 	 */
 
 	public Image getToolTipImage(final Object object, final int columnIndex) {
 		final IColumnFormatter formatter = getFormatter(columnIndex);
 		Object image;
 		if (formatter != null) {
 			image = formatter.getToolTipImage(object);
 		} else {
 			image = getTableFormatter().getToolTipImage(object, getColumnValue(object, columnIndex), columnIndex);
 		}
 		if (image instanceof Image) {
 			return (Image) image;
 		}
 		return null;
 	}
 
 	/**
 	 * Return the background color used for the tool tip
 	 * 
 	 * @param object
 	 *            the {@link Object} for which the tool tip is shown
 	 * @param columnIndex
 	 *            column index for which the tool tip is shown
 	 * 
 	 * @return the {@link Color} used or <code>null</code> if you want to use the default color {@link SWT#COLOR_INFO_BACKGROUND}
 	 * @see SWT#COLOR_INFO_BACKGROUND
 	 */
 	public Color getToolTipBackgroundColor(final Object object, final int columnIndex) {
 		final IColumnFormatter formatter = getFormatter(columnIndex);
 		Object color;
 		if (formatter != null) {
 			color = formatter.getToolTipBackgroundColor(object);
 		} else {
 			color = getTableFormatter().getToolTipBackgroundColor(object, getColumnValue(object, columnIndex), columnIndex);
 		}
 		if (color instanceof Color) {
 			return (Color) color;
 		}
 		return null;
 	}
 
 	/**
 	 * The foreground color used to display the the text in the tool tip
 	 * 
 	 * @param object
 	 *            the {@link Object} for which the tool tip is shown
 	 * @param columnIndex
 	 *            column index for which the tool tip is shown
 	 * @return the {@link Color} used or <code>null</code> if you want to use the default color {@link SWT#COLOR_INFO_FOREGROUND}
 	 * @see SWT#COLOR_INFO_FOREGROUND
 	 */
 	public Color getToolTipForegroundColor(final Object object, final int columnIndex) {
 		final IColumnFormatter formatter = getFormatter(columnIndex);
 		Object color;
 		if (formatter != null) {
 			color = formatter.getToolTipForegroundColor(object);
 		} else {
 			color = getTableFormatter().getToolTipForegroundColor(object, getColumnValue(object, columnIndex), columnIndex);
 		}
 		if (color instanceof Color) {
 			return (Color) color;
 		}
 		return null;
 	}
 
 	/**
 	 * Get the {@link Font} used to display the tool tip
 	 * 
 	 * @param object
 	 *            the element for which the tool tip is shown
 	 * @param columnIndex
 	 *            column index for which the tool tip is shown
 	 * @return {@link Font} or <code>null</code> if the default font is to be used.
 	 */
 	public Font getToolTipFont(final Object object, final int columnIndex) {
 		final IColumnFormatter formatter = getFormatter(columnIndex);
 		Object font;
 		if (formatter != null) {
 			font = formatter.getToolTipFont(object);
 		} else {
 			font = getTableFormatter().getToolTipFont(object, getColumnValue(object, columnIndex), columnIndex);
 		}
 		if (font instanceof Font) {
 			return (Font) font;
 		}
 		return null;
 	}
 
 	/**
 	 * Return the amount of pixels in x and y direction you want the tool tip to pop up from the mouse pointer. The default shift is 10px right and 0px below
 	 * your mouse cursor. Be aware of the fact that you should at least position the tool tip 1px right to your mouse cursor else click events may not get
 	 * propagated properly.
 	 * 
 	 * @param object
 	 *            the element for which the tool tip is shown
 	 * @param columnIndex
 	 *            column index for which the tool tip is shown
 	 * @return {@link Point} to shift of the tool tip or <code>null</code> if the default shift should be used.
 	 */
 	public Point getToolTipShift(final Object object, final int columnIndex) {
 		final IColumnFormatter formatter = getFormatter(columnIndex);
 		Object point;
 		if (formatter != null) {
 			point = formatter.getToolTipShift(object);
 		} else {
 			point = getTableFormatter().getToolTipShift(object, getColumnValue(object, columnIndex), columnIndex);
 		}
 		if (point instanceof Point) {
 			return (Point) point;
 		}
 		return null;
 	}
 
 	/**
 	 * The time in milliseconds the tool tip is shown for.
 	 * 
 	 * @param object
 	 *            the {@link Object} for which the tool tip is shown
 	 * @param columnIndex
 	 *            column index for which the tool tip is shown
 	 * @return time in milliseconds the tool tip is shown for
 	 */
 	public int getToolTipTimeDisplayed(final Object object, final int columnIndex) {
 		final IColumnFormatter formatter = getFormatter(columnIndex);
 		if (formatter != null) {
 			return formatter.getToolTipTimeDisplayed(object);
 		} else {
 			return getTableFormatter().getToolTipTimeDisplayed(object, getColumnValue(object, columnIndex), columnIndex);
 		}
 	}
 
 	/**
 	 * The time in milliseconds until the tool tip is displayed.
 	 * 
 	 * @param object
 	 *            the {@link Object} for which the tool tip is shown
 	 * @param columnIndex
 	 *            column index for which the tool tip is shown
 	 * @return time in milliseconds until the tool tip is displayed
 	 */
 	public int getToolTipDisplayDelayTime(final Object object, final int columnIndex) {
 		final IColumnFormatter formatter = getFormatter(columnIndex);
 		if (formatter != null) {
 			return formatter.getToolTipDisplayDelayTime(object);
 		} else {
 			return getTableFormatter().getToolTipDisplayDelayTime(object, getColumnValue(object, columnIndex), columnIndex);
 		}
 	}
 
 	/**
 	 * The {@link SWT} style used to create the {@link CLabel} (see there for supported styles). By default {@link SWT#SHADOW_NONE} is used.
 	 * 
 	 * @param object
 	 *            the element for which the tool tip is shown
 	 * @param columnIndex
 	 *            column index for which the tool tip is shown
 	 * @return the style used to create the label
 	 * @see CLabel
 	 */
 	public int getToolTipStyle(final Object object, final int columnIndex) {
 		final IColumnFormatter formatter = getFormatter(columnIndex);
 		if (formatter != null) {
 			return formatter.getToolTipStyle(object);
 		} else {
 			return getTableFormatter().getToolTipStyle(object, getColumnValue(object, columnIndex), columnIndex);
 		}
 	}
 
 	// protected methods
 	////////////////////
 
 	/**
 	 * @return a formatter that was set or {@code null} if formatter of the column wasn't set
 	 */
 	protected IColumnFormatter getFormatter(final int columnIndex) {
 		return columnIndex < formatters.length ? formatters[columnIndex] : null;
 	}
 
 	/**
 	 * @return a formatter that was set or a default formatter (never return {@code null})
 	 */
 	protected ITableFormatter getTableFormatter() {
 		return tableFormatter != null ? tableFormatter : DEFAULT_TABLE_FORMATTER;
 	}
 
 	// helping methods
 	//////////////////
 
 	public int getColumnCount() {
 		return this.formatters.length;
 	}
 
 	public void setFormatters(final IColumnFormatter[] formatters) {
 		Assert.isLegal(numColumns == formatters.length, String.format("expected %d formatters, got %d", numColumns, //$NON-NLS-1$
 				formatters.length));
 		this.formatters = new IColumnFormatter[formatters.length];
 		System.arraycopy(formatters, 0, this.formatters, 0, this.formatters.length);
 	}
 
 	public void setTableFormatter(final ITableFormatter formatter) {
 		tableFormatter = formatter;
 	}
 
 	public boolean isCheckBoxInFirstColumn() {
 		return checkBoxInFirstColumn;
 	}
 
 	public void setCheckBoxInFirstColumn(final boolean checkBoxInFirstColumn) {
 		this.checkBoxInFirstColumn = checkBoxInFirstColumn;
 	}
 
 }
