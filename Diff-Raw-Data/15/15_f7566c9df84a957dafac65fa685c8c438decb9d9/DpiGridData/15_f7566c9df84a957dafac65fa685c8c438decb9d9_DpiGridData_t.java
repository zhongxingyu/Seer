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
 package org.eclipse.riena.ui.swt.layout;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Control;
 
 import org.eclipse.riena.ui.swt.utils.SwtUtilities;
 
 /**
  * This class is a wrapper of the (final) class {@linkplain GridData}.<br>
  * The fields of {@code GridData} are copied. The pixel values are converted into DPI depending values.
  */
public final class DpiGridData {
 
 	final boolean exclude;
 	final int widthHint;
 	final int heightHint;
 	final int minimumWidth;
 	final int minimumHeight;
 	final int horizontalIndent;
 	final int verticalIndent;
 	final boolean grabExcessHorizontalSpace;
 	final boolean grabExcessVerticalSpace;
 	final int horizontalSpan;
 	final int verticalSpan;
 	final int verticalAlignment;
 	final int horizontalAlignment;
 
 	int cacheWidth = -1, cacheHeight = -1;
 	private int defaultWhint, defaultHhint, defaultWidth = -1, defaultHeight = -1;
 	private int currentWhint, currentHhint, currentWidth = -1, currentHeight = -1;
 
 	/**
 	 * Constructs a new instance of DpiGridData using default values.
 	 */
	public DpiGridData() {
 		this(null);
 	}
 
 	/**
 	 * Constructs a new instance of DpiGridData according to the values of the given {@code GridData} object.
 	 * 
 	 * @param gridData
 	 */
	public DpiGridData(final GridData gridData) {
 		GridData tmpGridData = gridData;
 		if (tmpGridData == null) {
 			tmpGridData = new GridData();
 		}
 		this.exclude = tmpGridData.exclude;
 		if (tmpGridData.widthHint == SWT.DEFAULT) {
 			this.widthHint = tmpGridData.widthHint;
 		} else {
 			this.widthHint = SwtUtilities.convertXToDpi(tmpGridData.widthHint);
 		}
 		if (tmpGridData.heightHint == SWT.DEFAULT) {
 			this.heightHint = tmpGridData.heightHint;
 		} else {
 			this.heightHint = SwtUtilities.convertYToDpi(tmpGridData.heightHint);
 		}
 		this.minimumWidth = SwtUtilities.convertXToDpi(tmpGridData.minimumWidth);
 		this.minimumHeight = SwtUtilities.convertYToDpi(tmpGridData.minimumHeight);
 		this.horizontalIndent = SwtUtilities.convertXToDpi(tmpGridData.horizontalIndent);
 		this.verticalIndent = SwtUtilities.convertYToDpi(tmpGridData.verticalIndent);
 		this.grabExcessHorizontalSpace = tmpGridData.grabExcessHorizontalSpace;
 		this.grabExcessVerticalSpace = tmpGridData.grabExcessVerticalSpace;
 		this.horizontalSpan = tmpGridData.horizontalSpan;
 		this.verticalSpan = tmpGridData.verticalSpan;
 		this.horizontalAlignment = tmpGridData.horizontalAlignment;
 		this.verticalAlignment = tmpGridData.verticalAlignment;
 	}
 
 	void computeSize(final Control control, final int wHint, final int hHint, final boolean flushCache) {
 		if (cacheWidth != -1 && cacheHeight != -1) {
 			return;
 		}
 		if (wHint == this.widthHint && hHint == this.heightHint) {
 			if (defaultWidth == -1 || defaultHeight == -1 || wHint != defaultWhint || hHint != defaultHhint) {
 				final Point size = control.computeSize(wHint, hHint, flushCache);
 				defaultWhint = wHint;
 				defaultHhint = hHint;
 				defaultWidth = size.x;
 				defaultHeight = size.y;
 			}
 			cacheWidth = defaultWidth;
 			cacheHeight = defaultHeight;
 			return;
 		}
 		if (currentWidth == -1 || currentHeight == -1 || wHint != currentWhint || hHint != currentHhint) {
 			final Point size = control.computeSize(wHint, hHint, flushCache);
 			currentWhint = wHint;
 			currentHhint = hHint;
 			currentWidth = size.x;
 			currentHeight = size.y;
 		}
 		cacheWidth = currentWidth;
 		cacheHeight = currentHeight;
 	}
 
 	void flushCache() {
 		cacheWidth = cacheHeight = -1;
 		defaultWidth = defaultHeight = -1;
 		currentWidth = currentHeight = -1;
 	}
 
 	private String getName() {
 		final String string = getClass().getName();
 		final int index = string.lastIndexOf('.');
 		if (index == -1) {
 			return string;
 		}
 		return string.substring(index + 1, string.length());
 	}
 
 	/**
 	 * Returns a string containing a concise, human-readable description of the receiver.
 	 * 
 	 * @return a string representation of the DpiGridData object
 	 */
 	@Override
 	public String toString() {
 		String hAlign = ""; //$NON-NLS-1$
 		switch (horizontalAlignment) {
 		case SWT.FILL:
 			hAlign = "SWT.FILL"; //$NON-NLS-1$
 			break;
 		case SWT.BEGINNING:
 			hAlign = "SWT.BEGINNING"; //$NON-NLS-1$
 			break;
 		case SWT.LEFT:
 			hAlign = "SWT.LEFT"; //$NON-NLS-1$
 			break;
 		case SWT.END:
 			hAlign = "SWT.END"; //$NON-NLS-1$
 			break;
 		case GridData.END:
 			hAlign = "GridData.END"; //$NON-NLS-1$
 			break;
 		case SWT.RIGHT:
 			hAlign = "SWT.RIGHT"; //$NON-NLS-1$
 			break;
 		case SWT.CENTER:
 			hAlign = "SWT.CENTER"; //$NON-NLS-1$
 			break;
 		case GridData.CENTER:
 			hAlign = "GridData.CENTER"; //$NON-NLS-1$
 			break;
 		default:
 			hAlign = "Undefined " + horizontalAlignment; //$NON-NLS-1$
 			break;
 		}
 		String vAlign = ""; //$NON-NLS-1$
 		switch (verticalAlignment) {
 		case SWT.FILL:
 			vAlign = "SWT.FILL"; //$NON-NLS-1$
 			break;
 		case SWT.BEGINNING:
 			vAlign = "SWT.BEGINNING"; //$NON-NLS-1$
 			break;
 		case SWT.TOP:
 			vAlign = "SWT.TOP"; //$NON-NLS-1$
 			break;
 		case SWT.END:
 			vAlign = "SWT.END"; //$NON-NLS-1$
 			break;
 		case GridData.END:
 			vAlign = "GridData.END"; //$NON-NLS-1$
 			break;
 		case SWT.BOTTOM:
 			vAlign = "SWT.BOTTOM"; //$NON-NLS-1$
 			break;
 		case SWT.CENTER:
 			vAlign = "SWT.CENTER"; //$NON-NLS-1$
 			break;
 		case GridData.CENTER:
 			vAlign = "GridData.CENTER"; //$NON-NLS-1$
 			break;
 		default:
 			vAlign = "Undefined " + verticalAlignment; //$NON-NLS-1$
 			break;
 		}
 		String string = getName() + " {"; //$NON-NLS-1$
 		string += "horizontalAlignment=" + hAlign + " "; //$NON-NLS-1$ //$NON-NLS-2$
 		if (horizontalIndent != 0) {
 			string += "horizontalIndent=" + horizontalIndent + " "; //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		if (horizontalSpan != 1) {
 			string += "horizontalSpan=" + horizontalSpan + " "; //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		if (grabExcessHorizontalSpace) {
 			string += "grabExcessHorizontalSpace=" + grabExcessHorizontalSpace + " "; //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		if (widthHint != SWT.DEFAULT) {
 			string += "widthHint=" + widthHint + " "; //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		if (minimumWidth != 0) {
 			string += "minimumWidth=" + minimumWidth + " "; //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		string += "verticalAlignment=" + vAlign + " "; //$NON-NLS-1$ //$NON-NLS-2$
 		if (verticalIndent != 0) {
 			string += "verticalIndent=" + verticalIndent + " "; //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		if (verticalSpan != 1) {
 			string += "verticalSpan=" + verticalSpan + " "; //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		if (grabExcessVerticalSpace) {
 			string += "grabExcessVerticalSpace=" + grabExcessVerticalSpace + " "; //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		if (heightHint != SWT.DEFAULT) {
 			string += "heightHint=" + heightHint + " "; //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		if (minimumHeight != 0) {
 			string += "minimumHeight=" + minimumHeight + " "; //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		if (exclude) {
 			string += "exclude=" + exclude + " "; //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		string = string.trim();
 		string += "}"; //$NON-NLS-1$
 		return string;
 	}
 
 }
