 /*******************************************************************************
  * Copyright (c) 2011, 2012 Formal Mind GmbH and University of Dusseldorf.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Michael Jastram - initial API and implementation
  ******************************************************************************/
 package org.eclipse.rmf.reqif10.pror.editor.agilegrid;
 
 import java.util.HashMap;
 
 import org.agilemore.agilegrid.AgileGrid;
 import org.agilemore.agilegrid.DefaultLayoutAdvisor;
 import org.agilemore.agilegrid.ICellRenderer;
 import org.eclipse.rmf.reqif10.SpecHierarchy;
 import org.eclipse.rmf.reqif10.Specification;
 
 public class ProrLayoutAdvisor extends DefaultLayoutAdvisor {
 
 	private final HashMap<Integer, String> topHeaderLabel = new HashMap<Integer, String>();
 	private int columnCount;
 	private int rowCount;
 
 	private final HashMap<Integer, HashMap<Integer, Integer>> cachedCellHeights = new HashMap<Integer, HashMap<Integer, Integer>>();
 
 	public ProrLayoutAdvisor(AgileGrid agileGrid) {
 		super(agileGrid);
 
 	}
 
 	@Override
 	public String getTopHeaderLabel(int col) {
 		String label;
 		if (col == columnCount - 1) {
 			label = "Link";
 		} else {
 			label = topHeaderLabel.get(col);
 		}
 		return label != null ? label : ("Column " + (col + 1));
 	}
 
 	@Override
 	protected void doSetTopHeaderLabel(int col, String label) {
 		topHeaderLabel.put(col, label);
 	}
 
 	@Override
 	protected void doSetColumnCount(int columnCount) {
 		this.columnCount = columnCount;
 	}
 
 	@Override
 	public int getColumnCount() {
 		return columnCount;
 	}
 
 	@Override
 	protected void doSetRowCount(int rowCount) {
 		this.rowCount = rowCount;
 	}
 
 	@Override
 	public int getRowCount() {
 		return rowCount;
 	}
 
 	/**
 	 * Used by some {@link ICellRenderer}s to store the cell's height. The
 	 * method checks whether the row height has changed and notifies the
 	 * associated {@link ProrAgileGrid}.
 	 */
 	void setCellHeight(int row, int col, int height) {
 		int oldRowHeight = getRowHeight(row);
 		HashMap<Integer, Integer> cols = cachedCellHeights.get(row);
 		if (cols == null) {
 			cols = new HashMap<Integer, Integer>();
 			cachedCellHeights.put(row, cols);
 		}
 		cols.put(col, height);
 
 		int newRowHeight = getRowHeight(row);
 		if (oldRowHeight != newRowHeight) {
 			super.setRowHeight(row, newRowHeight);
 			agileGrid.redraw();
 		}
 	}
 	
 
 	@Override
 	public int getRowHeight(int row) {
 		ProrRow prorRow = ((ProrAgileGridContentProvider) agileGrid
 				.getContentProvider()).getProrRow(row);
		if (prorRow != null && ! prorRow.isVisible()) return 1;
 
 		int height = 18;
 		if (cachedCellHeights.get(row) == null) {
 			return height;
 		}
 		HashMap<Integer, Integer> cols = cachedCellHeights.get(row);
 		if (cols == null) {
 			return height;
 		}
 		for (Integer h : cols.values()) {
 			if (h > height) {
 				height = h;
 			}
 		}
 		return height;
 	}
 
 	@Override
 	public String getLeftHeaderLabel(int row) {
 		StringBuffer sb = new StringBuffer();
 		
 		ProrRow prorRow = ((ProrAgileGridContentProvider) agileGrid
 				.getContentProvider()).getProrRow(row);
 		if (!(prorRow instanceof ProrRow.ProrRowSpecHierarchy)) {
 			return "";			
 		}
 		SpecHierarchy specHierarchy = ((ProrRow.ProrRowSpecHierarchy) prorRow).getSpecHierarchy();
 
 		while (specHierarchy.eContainer() instanceof SpecHierarchy) {
 			SpecHierarchy parent = (SpecHierarchy) specHierarchy.eContainer();
 			int level = parent.getChildren().indexOf(specHierarchy) + 1;
 			sb.insert(0, level);
 			sb.insert(0, ".");
 			specHierarchy = parent;
 		}
 		if (specHierarchy.eContainer() instanceof Specification) {
 			Specification parent = (Specification) specHierarchy
 					.eContainer();
 			int level = parent.getChildren().indexOf(specHierarchy) + 1;
 			sb.insert(0, level);
 		}
 		return sb.toString();
 	}
 
 }
