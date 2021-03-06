 /*******************************************************************************
  * Copyright (c) 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Fredy Dobler <fredy@dobler.net> - bug 159600
  ******************************************************************************/
 
 package org.eclipse.jface.viewers;
 
 import java.util.Timer;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CLabel;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 
 /**
  * The TooltipSupport is the class that provides tooltips for
  * ColumnViewers.
  * @since 3.3
  * <strong>EXPERIMENTAL</strong> This class or interface has been added as
  * part of a work in progress. This API may change at any given time. Please 
  * do not use this API without consulting with the Platform/UI team.
  *
  */
 class ToolTipSupport {
 	private ColumnViewer viewer;
 	private Listener listener = new MouseListener();
 	private static final int DEFAULT_SHIFT_X = 10;
 	private static final int DEFAULT_SHIFT_Y = 0;
 	
 	ToolTipSupport(ColumnViewer viewer) {
 		this.viewer = viewer;
 	}
 	
 	/**
 	 * activate tooltip support for this viewer
 	 */
 	public void activate() {
 		deactivate();
 		viewer.getControl().addListener(SWT.Dispose, listener);
 		viewer.getControl().addListener(SWT.MouseHover, listener);
 		viewer.getControl().addListener(SWT.MouseMove, listener);
 		viewer.getControl().addListener(SWT.MouseExit, listener);
 		viewer.getControl().addListener(SWT.MouseDown, listener);
 	}
 	
 	/**
 	 * deactivate tooltip support for this viewer
 	 */
 	public void deactivate() {
 		viewer.getControl().removeListener(SWT.Dispose, listener);
 		viewer.getControl().removeListener(SWT.MouseHover, listener);
 		viewer.getControl().removeListener(SWT.MouseMove, listener);
 		viewer.getControl().removeListener(SWT.MouseExit, listener);
 		viewer.getControl().removeListener(SWT.MouseDown, listener);
 	}
 	
 	private class MouseListener implements Listener {
 		private Shell tip;
 		private TooltipHideListener hideListener = new TooltipHideListener();
 		private Timer timer = new Timer(true);
 		
 		/* (non-Javadoc)
 		 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
 		 */
 		public void handleEvent(Event event) {
 			switch (event.type) {
 			case SWT.Dispose:
 				if( timer != null ) {
 					timer.cancel();
 				}
 			case SWT.KeyDown:
 			case SWT.MouseMove:
 			case SWT.MouseDown:
 				disposeTooltip(tip);
 				break;
 			case SWT.MouseHover:
                if (!(event.widget instanceof Control))
                  break;
                // map receiver-relative coordinates to display-relative coordinates
                ViewerRow row = viewer.getRowPart(((Control) event.widget).toDisplay(
                    event.x, event.y));
                viewer.getControl().setToolTipText(""); //$NON-NLS-1$
 
                if (row != null)
                {
                  // use receiver-relative coordinates
                  popupTooltip(row, new Point(event.x, event.y));
                }
                break;
 			}
 		}
 		
 		/**
 		 * Popup a tooltip for the row at Point p.
 		 * @param row
 		 * @param p
 		 */
 		private void popupTooltip(ViewerRow row, Point p) {
 			Object element = row.getItem().getData();
 			disposeTooltip(tip);
 
 			ViewerColumn viewPart = viewer.getViewerColumn(row.getColumnIndex(p));
 			
 			if( viewPart == null ) {
 				return;
 			}
 			
 			CellLabelProvider labelProvider = viewPart.getLabelProvider();
 			
 			String text = labelProvider.getToolTipText(element);
 			
 			if( text != null ) {
 				
 				if( labelProvider.useNativeToolTip(element) ) {
 					viewer.getControl().setToolTipText(text);
 					return;
 				}
 				
 				tip = new Shell(viewer.getControl().getShell(), SWT.ON_TOP | SWT.TOOL);
 				tip.setLayout(new FillLayout());
 				CLabel label = new CLabel(tip, labelProvider.getToolTipStyle(element));
 				label.setText(text);
 				label.addListener(SWT.MouseExit, hideListener);
 				label.addListener(SWT.MouseDown, hideListener);
 				
 				Image img =  labelProvider.getToolTipImage(element);
 				
 				if( img != null) {
 					label.setImage(img);
 				}
 				
 				Color color = labelProvider.getToolTipForegroundColor(element);
 				if( color == null ) {
 					color = viewer.getControl().getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND);
 				}
 				label.setForeground(color);
 				
 				color = labelProvider.getToolTipBackgroundColor(element);
 				if( color == null ) {
 					color = viewer.getControl().getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
 				}
 				label.setBackground(color);
 				
 				Font font = labelProvider.getToolTipFont(element);
 				
 				if( font != null ) {
 					label.setFont(font);
 				}
 				
 				Point pt = viewer.getControl().toDisplay(p);
 				Point shift = labelProvider.getToolTipShift(element);
 				
 				if( shift == null ) {
 					pt.x += DEFAULT_SHIFT_X;
 					pt.y += DEFAULT_SHIFT_Y;
 				} else {
 					pt.x += shift.x;
 					pt.y += shift.y;
 				}
 				
 				tip.pack();
 				tip.setLocation(pt);
 				tip.setVisible(true);
 			}
 		}
 		
 		/**
 		 * Dispose the tooltip.
 		 * @param tip
 		 */
 		private void disposeTooltip(Shell tip) {
 			if (tip != null && !tip.isDisposed()) {
 				tip.dispose();
 			}
 
 			tip = null;
 		}
 	}
 	
 	/**
 	 * TooltipHideListener is a listener for tooltip removal.
 	 * @since 3.3
 	 *
 	 */
 	private class TooltipHideListener implements Listener {
 		/* (non-Javadoc)
 		 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
 		 */
 		public void handleEvent(Event event) {
 			CLabel label = (CLabel) event.widget;
 			Shell shell = label.getShell();
 			switch (event.type) {
 			case SWT.MouseDown:
 				viewer.setSelection(new StructuredSelection());
 				// fall through
 			case SWT.MouseExit:
 				viewer.getControl().setFocus();
 				shell.dispose();
 				break;
 			}
 		}
 	}
 }
