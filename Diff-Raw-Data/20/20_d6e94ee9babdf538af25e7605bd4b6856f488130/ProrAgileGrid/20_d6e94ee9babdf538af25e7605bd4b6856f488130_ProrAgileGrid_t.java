 /*******************************************************************************
  * Copyright (c) 2011 Formal Mind GmbH and University of Dusseldorf.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Michael Jastram - initial API and implementation
  ******************************************************************************/
 package org.eclipse.rmf.pror.reqif10.editor.agilegrid;
 
 import org.agilemore.agilegrid.AgileGrid;
 import org.agilemore.agilegrid.Cell;
import org.agilemore.agilegrid.EditorActivationEvent;
 import org.agilemore.agilegrid.ILayoutAdvisor;
import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 
 /**
  * We override the default implementation to slightly change the behavior when
  * opening the context menu.
  * 
  * @author jastram
  * 
  */
 public class ProrAgileGrid extends AgileGrid {
 
 	public static final int DND_DROP_AS_SIBLING = 0;
 	public static final int DND_DROP_AS_CHILD = 1;
 
 	protected Cell dndHoverCell;
 	protected int dndHoverDropMode;
 	
 
 	public ProrAgileGrid(Composite parent, int style) {
 		super(parent, style);
 
 		// By default, the selection doesn't change if we right-click an
 		// unselected cell. But that's what we want.
 		// TODO This doesn't work, and therefore enableing this code is deceiving.
 //		addMenuDetectListener(new MenuDetectListener() {
 //
 //			@Override
 //			public void menuDetected(MenuDetectEvent e) {
 //				Point pos = toControl(e.x, e.y);
 //				Cell clickedCell = getCell(pos.x, pos.y);
 //				if (!isCellSelected(clickedCell.row, clickedCell.column)) {
 //					selectCells(new Cell[] { clickedCell });
 //				}
 //			}
 //		});
 	}
 
 	/**
 	 * Returns the upper y-coordinate of a row. 
 	 * Returns a negative value id the row does not exist or is invisible  
 	 * @param row
 	 * @return
 	 */
 	public int getYForRow(int row) {
 		if (row < 0 || row > topRow + getRowsVisible() - 1) {
 			return Integer.MIN_VALUE;
 		}
 		
 		ILayoutAdvisor layoutAdvisor = getLayoutAdvisor();
 		int fixedRowCount = layoutAdvisor.getFixedRowCount();
 
 		if (row < fixedRowCount){
 			return Integer.MIN_VALUE;
 		}
 		
 
 		int y = getLinePixels();
 		
 		if (layoutAdvisor.isTopHeaderVisible()) {
 			y += layoutAdvisor.getTopHeaderHeight();
 			y += getLinePixels();
 		}
 		
 		for (int i = 0; i < fixedRowCount; i++) {
 			if (row == i) {
 				return y;
 			}
 			y += layoutAdvisor.getRowHeight(i);
 			y += getLinePixels();
 		}
 		
 		for (int i = topRow; i <= topRow + getRowsVisible() - 1; i++) {
 			if (row == i) {
 				return y;
 			}
 			y += layoutAdvisor.getRowHeight(i);
 			y += getLinePixels();
 		}		
 		
 
 		return Integer.MIN_VALUE;
 	}
 	
	@Override
	public void triggerEditorActivationEvent(
			EditorActivationEvent editorActivationEvent, Object hint) {
		if (editorActivationEvent.eventType == EditorActivationEvent.KEY_PRESSED
				&& editorActivationEvent.keyCode == SWT.DEL) {
			/*
			 * This is a workaround for Bug 374183 - Deleting multiple
			 * SpecElements in Specification editor via DEL key does not work No
			 * need to activate an Editor since the rows are deleted. Activating
			 * the editor would reset the selection to one single cell
			 */
			return;
		}
		super.triggerEditorActivationEvent(editorActivationEvent, hint);
	}
	
 }
