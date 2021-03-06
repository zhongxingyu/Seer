 /*
  *  Freeplane - mind map editor
  *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
  *
  *  This file is modified by Dimitry Polivaev in 2008.
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.freeplane.features.mindmapmode.addins.time;
 
 import java.util.ArrayList;
 import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
 
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.TableModel;
 
 import org.freeplane.features.mindmapmode.addins.time.NodeList.NodeHolder;
 
 /**
  * @author foltin
  */
 class FlatNodeTableFilterModel extends AbstractTableModel {
 	private class TableModelHandler implements TableModelListener {
 		public void tableChanged(final TableModelEvent arg0) {
 			fireTableDataChanged();
 		}
 	}
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private String mFilterRegexp;
 	/**
 	 * Contains indices or rows matching the filter criteria.
 	 */
 	private ArrayList mIndexArray;
 	/**
 	 * The column that contains the NodeHolder items
 	 */
 	final private int mNodeTextColumn;
 	private Pattern mPattern;
 	final private TableModel mTableModel;
 
 	/**
 	 * @param node_text_column
 	 */
 	public FlatNodeTableFilterModel(final TableModel tableModel, final int node_text_column) {
 		super();
 		mTableModel = tableModel;
 		mNodeTextColumn = node_text_column;
 		tableModel.addTableModelListener(new TableModelHandler());
 		resetFilter();
 	}
 
 	@Override
 	public Class getColumnClass(final int arg0) {
 		return mTableModel.getColumnClass(arg0);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see javax.swing.table.TableModel#getColumnCount()
 	 */
 	public int getColumnCount() {
 		return mTableModel.getColumnCount();
 	}
 
 	@Override
 	public String getColumnName(final int pColumnIndex) {
 		return mTableModel.getColumnName(pColumnIndex);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see javax.swing.table.TableModel#getRowCount()
 	 */
 	public int getRowCount() {
 		return mIndexArray.size();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see javax.swing.table.TableModel#getValueAt(int, int)
 	 */
 	public Object getValueAt(final int row, final int column) {
 		if (row < 0 || row >= getRowCount()) {
 			throw new IllegalArgumentException("Illegal Row specified: " + row);
 		}
 		final int origRow = ((Integer) mIndexArray.get(row)).intValue();
 		return mTableModel.getValueAt(origRow, column);
 	}
 
 	public void resetFilter() {
 		setFilter(".*");
 	}
 
 	public void setFilter(final String filterRegexp) {
 		mFilterRegexp = filterRegexp;
 		//		System.out.println("Setting filter to '" + mFilterRegexp + "'");
		try{
			mPattern = Pattern.compile(mFilterRegexp, Pattern.CASE_INSENSITIVE);
			updateIndexArray();
			fireTableDataChanged();
		}
		catch (PatternSyntaxException e) {
		}
 	}
 
 	private void updateIndexArray() {
 		final ArrayList newIndexArray = new ArrayList();
 		for (int i = 0; i < mTableModel.getRowCount(); i++) {
 			final NodeHolder nodeContent = (NodeHolder) mTableModel.getValueAt(i, mNodeTextColumn);
 			if (mPattern.matcher(nodeContent.toString()).matches()) {
 				newIndexArray.add(new Integer(i));
 			}
 		}
 		mIndexArray = newIndexArray;
 	}
 }
