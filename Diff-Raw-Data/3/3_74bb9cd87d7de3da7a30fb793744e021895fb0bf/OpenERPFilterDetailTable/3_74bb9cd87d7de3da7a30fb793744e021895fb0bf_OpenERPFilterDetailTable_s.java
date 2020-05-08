 package com.debortoliwines.openerp.reporting.ui;
 
 /*
  *   This file is part of OpenERPJavaReportHelper
  *
  *   OpenERPJavaAPI is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU Lesser General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   OpenERPJavaAPI is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU Lesser General Public License for more details.
  *
  *   You should have received a copy of the GNU Lesser General Public License
  *   along with OpenERPJavaAPI.  If not, see <http://www.gnu.org/licenses/>.
  *
  *   Copyright 2012 De Bortoli Wines Pty Limited (Australia)
  */
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import javax.swing.table.AbstractTableModel;
 import com.debortoliwines.openerp.reporting.di.OpenERPFilterInfo;
 
 /**
  * Custom model to hold filter data.
  * All filter data is kept by the model, but subsections of the data is shown
  * by calling setCurrentView with the modelPath and instanceNumber you are interested in
  * @author Pieter van der Merwe
  * @since  Jan 5, 2012
  */
 public class OpenERPFilterDetailTable extends AbstractTableModel {
 
 	private static final long serialVersionUID = -4866256815912624560L;
 	private String currentModelPath = "";
 	private int currentInstanceNum = 0;
 	
 	private ArrayList<OpenERPFilterInfo> fullList = new ArrayList<OpenERPFilterInfo>();
 	
 	public void addFilter(String operator, String fieldName, String comparator, String value){
 		if (this.currentModelPath.length() == 0)
 			return;
 		
 		OpenERPFilterInfo newInfo = new OpenERPFilterInfo(this.currentModelPath, this.currentInstanceNum, operator, fieldName, comparator, value);
 		fullList.add(newInfo);
 		fireTableDataChanged();
 	}
 	
 	/**
 	 * Instructs this model to only show results for a specific modelPath and instance
 	 * @param currentModelPath
 	 * @param instanceNum
 	 */
 	public void setCurrentView(String modelPath, int instanceNum) {
 		this.currentModelPath = modelPath;
 		this.currentInstanceNum = instanceNum;
 		fireTableDataChanged();
 	}
 	
 	
 	/**
 	 * Function to only return a subset of the data for internal use
 	 * @return
 	 */
 	private ArrayList<OpenERPFilterInfo> getCurrentModelList(){
 		ArrayList<OpenERPFilterInfo> modelPathList = new ArrayList<OpenERPFilterInfo>();
 		for (OpenERPFilterInfo item : fullList){
			if (item.getModelPath().equals(this.currentModelPath)
 					&& item.getInstanceNum() == this.currentInstanceNum){
 				modelPathList.add(item);
 			}
 		}
 		return modelPathList;
 	}
 	
 	public void removeFilter(int rowIndex){
 		if (rowIndex < 0)
 			return;
 		
 		OpenERPFilterInfo filter = getCurrentModelList().get(rowIndex);
 		fullList.remove(filter);
 		
 		fireTableRowsDeleted(rowIndex, rowIndex);
 	}
 	
 	public void removeFilters(int [] rowIndexes){
 		Arrays.sort(rowIndexes);
 		
 		for (int i = rowIndexes.length -1; i >=0; i--){
 			removeFilter(rowIndexes[i]);
 		}
 	}
 
 	@Override
 	public int getColumnCount() {
 		return 5;
 	}
 	
 	@Override
 	public int getRowCount() {
 		return getCurrentModelList().size();
 	}
 
 	@Override
 	public Object getValueAt(final int rowIndex, final int columnIndex){
 		OpenERPFilterInfo filterRow = getCurrentModelList().get(rowIndex);
 		
 		if (columnIndex == 0)
 			return rowIndex;
 		else if (columnIndex == 1)
 			return filterRow.getOperator();
 		else if (columnIndex == 2)
 			return filterRow.getFieldName();
 		else if (columnIndex == 3)
 			return filterRow.getComparator();
 		else if (columnIndex == 4)
 			return filterRow.getValue();
 		
 		return null;
 	}
 	
 	@Override
 	public String getColumnName(int columnIndex){
 		if (columnIndex == 0)
 			return "#";
 		else if (columnIndex == 1)
 			return "Operator";
 		else if (columnIndex == 2)
 			return "Field";
 		else if (columnIndex == 3)
 			return "Comparator";
 		else if (columnIndex == 4)
 			return "Value";
 		
 		return "";
 	}
 	
 	@Override
 	public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex){
 		if (aValue == null)
 			return;
 		
 		if (rowIndex >= getCurrentModelList().size())
 		  return;
 		
 		OpenERPFilterInfo filterRow = getCurrentModelList().get(rowIndex);
 		
 		if (columnIndex == 1)
 			filterRow.setOperator(aValue.toString());
 		else if (columnIndex == 2)
 			filterRow.setFieldName(aValue.toString());
 		else if (columnIndex == 3)
 			filterRow.setComparator(aValue.toString());
 		else if (columnIndex == 4)
 			filterRow.setValue(aValue.toString());
 		
 		fireTableCellUpdated(rowIndex, columnIndex);
 	}
 	
 	@Override
 	public boolean isCellEditable(final int rowIndex, final int columnIndex)
 	{
 		if (columnIndex > 0)
 			return true;
 		
 		return false;
 	}
 
 	public ArrayList<OpenERPFilterInfo> getFilterData() {
 		return fullList;
 	}
 
 	public void setFilterData(ArrayList<OpenERPFilterInfo> filterData) {
 		this.fullList = filterData;
 		fireTableDataChanged();
 	}
 }
