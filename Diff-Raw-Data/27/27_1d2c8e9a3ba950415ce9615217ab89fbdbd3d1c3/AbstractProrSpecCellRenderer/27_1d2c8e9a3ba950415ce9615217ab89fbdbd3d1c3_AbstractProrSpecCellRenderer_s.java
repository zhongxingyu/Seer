 /*******************************************************************************
  * Copyright (c) 2011, 2013 Formal Mind GmbH and University of Dusseldorf.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Lukas Ladenberger - initial API and implementation
  ******************************************************************************/
 package org.eclipse.rmf.reqif10.pror.editor.agilegrid;
 
 import org.agilemore.agilegrid.AgileGrid;
 import org.agilemore.agilegrid.SWTResourceManager;
 import org.agilemore.agilegrid.SWTX;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.rmf.reqif10.SpecRelation;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Rectangle;
 
 public abstract class AbstractProrSpecCellRenderer extends AbstractProrCellRenderer {
 
 	public static Color COLOR_LINK = SWTResourceManager.getColor(240, 240, 240);
 
 	public AbstractProrSpecCellRenderer(AgileGrid agileGrid,
 			AdapterFactory adapterFactory) {
 		super(agileGrid, adapterFactory);
 	}
 
 	@Override
 	protected void drawGridLines(GC gc, Rectangle rect, int row, int col) {
 
 		Color vBorderColor = COLOR_LINE_LIGHTGRAY;
 		Color hBorderColor = COLOR_LINE_LIGHTGRAY;
 
 		if (agileGrid instanceof ProrAgileGrid) {
 			ProrAgileGrid grid = (ProrAgileGrid) agileGrid;
 			if (grid.dndHoverCell != null
 					&& row == grid.dndHoverCell.row
 					&& grid.dndHoverDropMode == ProrAgileGrid.DND_DROP_AS_SIBLING) {
 				hBorderColor = COLOR_LINE_DARKGRAY;
 			}
 		}
 
 		if ((style & INDICATION_SELECTION_ROW) != 0) {
 			vBorderColor = COLOR_BGROWSELECTION;
 			hBorderColor = COLOR_BGROWSELECTION;
 		}
 
 		if ((agileGrid.getStyle() & SWTX.NOT_SHOW_GRID_LINE) == SWTX.NOT_SHOW_GRID_LINE) {
 			vBorderColor = COLOR_BACKGROUND;
 			hBorderColor = COLOR_BACKGROUND;
 		}
 
 		ProrAgileGridContentProvider contentProvider = (ProrAgileGridContentProvider) agileGrid
 				.getContentProvider();
 
 		int nextRow = row + 1;
 		if (nextRow < contentProvider.getRowCount()) {
 			ProrRow prorRowNext = contentProvider.getProrRow(nextRow);
 			if (prorRowNext != null
 					&& prorRowNext.getSpecElement() instanceof SpecRelation)
 				hBorderColor = COLOR_LINK;
 		}
 		
 		drawDefaultCellLine(gc, rect, vBorderColor, hBorderColor);
 
 	}
 	
 }
