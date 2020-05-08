 /*******************************************************************************
  * Copyright (c) 2011, 2013 Formal Mind GmbH and University of Dusseldorf.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Michael Jastram - initial API and implementation
  ******************************************************************************/
 package org.eclipse.rmf.reqif10.pror.editor.agilegrid;
 
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import org.agilemore.agilegrid.AgileGrid;
 import org.agilemore.agilegrid.Cell;
 import org.agilemore.agilegrid.IContentProvider;
 import org.agilemore.agilegrid.SWTResourceManager;
 import org.agilemore.agilegrid.renderers.TextCellRenderer;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.edit.provider.ItemProviderAdapter;
 import org.eclipse.rmf.reqif10.AttributeValue;
 import org.eclipse.rmf.reqif10.AttributeValueBoolean;
 import org.eclipse.rmf.reqif10.AttributeValueXHTML;
 import org.eclipse.rmf.reqif10.EnumValue;
 import org.eclipse.rmf.reqif10.XhtmlContent;
 import org.eclipse.rmf.reqif10.common.util.ReqIF10Util;
 import org.eclipse.rmf.reqif10.pror.editor.presentation.Reqif10EditorPlugin;
 import org.eclipse.rmf.reqif10.pror.util.ProrUtil;
 import org.eclipse.rmf.reqif10.pror.util.ProrXhtmlSimplifiedHelper;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * @author Lukas Ladenberger
  * @author Ingo Weigelt
  */
 public class AbstractProrCellRenderer extends TextCellRenderer {
 
 	protected final AdapterFactory adapterFactory;
 
 	private final Image IMG_WARN_FALSE = PlatformUI.getWorkbench()
 			.getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK);
 
 	private final Image IMG_WARN_TRUE = PlatformUI.getWorkbench()
 			.getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK);
 
 	private Shell xhtmlSimplifiedToolTip;
 
 	private boolean isXhtmlSimplifiedListenerInit = false;
 
 	private IContentProvider contentProvider;
 
 	/**
 	 * @param agileGrid
 	 */
 	public AbstractProrCellRenderer(AgileGrid agileGrid,
 			AdapterFactory adapterFactory) {
 		super(agileGrid);
 		this.adapterFactory = adapterFactory;
 		this.contentProvider = agileGrid.getContentProvider();
 	}
 
 	public AbstractProrCellRenderer(AgileGrid agileGrid) {
 		this(agileGrid, null);
 	}
 
 	protected int doDrawCellContentDefault(GC gc, Rectangle rect, Object value) {
 		String stringValue;
 		Image img = null;
 		boolean defaultValue = false;
 		if (value instanceof AttributeValue) {
 			defaultValue = ((AttributeValue) value).eContainer() == null;
 			Object v = ReqIF10Util.getTheValue((AttributeValue) value);
 			if (v instanceof GregorianCalendar) {
 				GregorianCalendar cal = (GregorianCalendar) v;
 				Date date = cal.getTime();
 				stringValue = DateFormat.getDateInstance().format(date);
 			} else if (v instanceof List<?>) {
 				stringValue = convertListToString((List<?>) v);
 			} else if (v instanceof XhtmlContent && v != null) {
 				if (!isXhtmlSimplifiedListenerInit)
 					initXhtmlSimplifiedToolTipListener();
 				XhtmlContent xhtmlContent = (XhtmlContent) v;
 				AttributeValueXHTML atrXhtml = (AttributeValueXHTML) value;
 				stringValue = ProrXhtmlSimplifiedHelper
 						.xhtmlToSimplifiedString(xhtmlContent);
 				boolean formattedAttribute = ProrXhtmlSimplifiedHelper
 						.isFormattedAttribute(xhtmlContent);
 				if (!atrXhtml.isSetSimplified() && formattedAttribute) {
 					img = IMG_WARN_FALSE;
 				} else if (atrXhtml.isSetSimplified()
 						&& atrXhtml.getTheOriginalValue() != null) {
 					img = IMG_WARN_TRUE;
 				}
 			} else if (value instanceof AttributeValueBoolean) {
 				if (!((AttributeValueBoolean) value).isSetTheValue()) {
 					stringValue = "";
 				} else {
 					stringValue = (Boolean) v ? "\u2612" : "\u2610";
 				}
 
 			} else {
 				stringValue = v == null ? "" : v.toString();
 			}
 		} else {
 			stringValue = value != null ? value.toString() : "";
 		}
 
 		int alignment = getAlignment();
		// We must replace tabs with spaces, as it can screw up the wrapping algorithm.
		String wrappedText = wrapText(gc, stringValue.replace('\t', ' '), rect.width);
 		gc.setForeground(defaultValue ? COLOR_LINE_DARKGRAY : COLOR_TEXT);
 		drawTextImage(gc, wrappedText, alignment, img, alignment, rect.x + 3,
 				rect.y + 2, rect.width - 6, rect.height - 4);
 		
 		return gc.textExtent(wrappedText).y;
 	}
 
 	private String convertListToString(List<?> list) {
 		String stringValue;
 		StringBuffer sb = new StringBuffer();
 		for (Object object : list) {
 			if (object instanceof EnumValue) {
 				ItemProviderAdapter itemProvider = ProrUtil.getItemProvider(
 						adapterFactory, object);
 				sb.append(itemProvider.getText(object));
 
 			} else {
 				sb.append(object.toString());
 			}
 			sb.append("\n");
 		}
 		if (sb.length() > 0)
 			sb.delete(sb.length() - 1, sb.length());
 		stringValue = sb.toString();
 		return stringValue;
 	}
 
 	// Workaround: Upon closing a UIEditor and reopening a new one, the color
 	// got
 	// disposed. No idea why. This is a workaround.
 	protected void initialColor(int row, int col) {
 		if (agileGrid.isCellSelected(row, col)) {
 			background = SWTResourceManager.getColor(223, 227, 237);
 		}
 	}
 
 	@Override
 	protected void drawCellContent(GC gc, Rectangle rect, int row, int col) {
 
 		this.foreground = this.getDefaultForeground();
 		this.background = this.getDefaultBackground();
 
 		if (agileGrid instanceof ProrAgileGrid) {
 			ProrAgileGrid grid = (ProrAgileGrid) agileGrid;
 			if (grid.dndHoverCell != null && row == grid.dndHoverCell.row
 					&& grid.dndHoverDropMode == ProrAgileGrid.DND_DROP_AS_CHILD) {
 				this.background = COLOR_BGROWSELECTION;
 			}
 		}
 
 		// initial color for current cell.
 		initialColor(row, col);
 
 		// Clear background.
 		clearCellContentRect(gc, rect);
 
 		// draw text and image in the given area.
 		doDrawCellContent(gc, rect, row, col);
 	}
 
 	/**
 	 * This method initializes the tool tip listener for keywords. TODO:
 	 * Messages should be more generic!
 	 */
 	private void initXhtmlSimplifiedToolTipListener() {
 
 		Listener l = new Listener() {
 			public void handleEvent(Event e) {
 				switch (e.type) {
 				case SWT.Dispose:
 				case SWT.KeyDown:
 				case SWT.MouseMove: {
 					if (xhtmlSimplifiedToolTip == null)
 						break;
 					xhtmlSimplifiedToolTip.dispose();
 					xhtmlSimplifiedToolTip = null;
 					break;
 				}
 				case SWT.MouseHover: {
 					Point mousePointer = new Point(e.x, e.y);
 					Cell cell = agileGrid.getCell(mousePointer.x,
 							mousePointer.y);
 					Rectangle cellRect = agileGrid.getCellRect(cell.row,
 							cell.column);
 					Rectangle rectNew = new Rectangle(cellRect.x, cellRect.y
 							+ (cellRect.height / 2) - 12, 25, 25);
 					if (rectNew.contains(mousePointer)) {
 						if (xhtmlSimplifiedToolTip != null
 								&& !xhtmlSimplifiedToolTip.isDisposed())
 							xhtmlSimplifiedToolTip.dispose();
 
 						Point displayPointer = agileGrid
 								.toDisplay(mousePointer);
 
 						Object contentAt = contentProvider.getContentAt(
 								cell.row, cell.column);
 						if (contentAt instanceof AttributeValueXHTML) {
 
 							AttributeValueXHTML atrXhtml = (AttributeValueXHTML) contentAt;
 
 							String msg = "_UI_Reqif10XhtmlIsSimplifiedFalse";
 
 							if (atrXhtml.isSimplified()
 									&& atrXhtml.getTheOriginalValue() != null)
 								msg = "_UI_Reqif10XhtmlIsSimplifiedTrue";
 
 							xhtmlSimplifiedToolTip = showTooltip(Display
 									.getDefault().getActiveShell(),
 									displayPointer.x + 10,
 									displayPointer.y + 10,
 									Reqif10EditorPlugin.INSTANCE.getString(msg));
 
 						}
 
 					}
 
 				}
 				}
 			}
 		};
 
 		agileGrid.addListener(SWT.Dispose, l);
 		agileGrid.addListener(SWT.KeyDown, l);
 		agileGrid.addListener(SWT.MouseMove, l);
 		agileGrid.addListener(SWT.MouseHover, l);
 
 		isXhtmlSimplifiedListenerInit = true;
 
 	}
 
 	private Shell showTooltip(Shell parent, int x, int y, String msg) {
 		Shell tooltip = new Shell(parent, SWT.TOOL | SWT.ON_TOP);
 		tooltip.setLayout(new GridLayout());
 
 		tooltip.setBackground(tooltip.getDisplay().getSystemColor(
 				SWT.COLOR_INFO_BACKGROUND));
 		tooltip.setBackgroundMode(SWT.INHERIT_FORCE);
 
 		Label lbContent = new Label(tooltip, SWT.NONE);
 		lbContent.setText(msg);
 
 		Point lbContentSize = lbContent.computeSize(SWT.DEFAULT, SWT.DEFAULT);
 
 		int width = lbContentSize.x + 10;
 		int height = lbContentSize.y + 10;
 
 		tooltip.setBounds(x, y, width, height);
 		tooltip.setVisible(true);
 		return tooltip;
 	}
 
 }
