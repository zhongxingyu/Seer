 /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.icefaces.ace.component.dataexporter;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.ArrayList;
 import javax.el.MethodExpression;
 import javax.faces.component.UIColumn;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import org.icefaces.ace.component.datatable.DataTable;
 import org.icefaces.ace.component.panelexpansion.PanelExpansion;
 import org.icefaces.ace.component.columngroup.ColumnGroup;
 import org.icefaces.ace.component.row.Row;
 import org.icefaces.ace.model.table.RowState;
 import org.icefaces.ace.model.table.RowStateMap;
 
 /**
  * This custom exporter is only available by using the 'customExporter' attribute.
  * It invokes InnerTableCSVExporter.
  * The ICEfaces showcase application contains an example of how to use it.
  */
 public class OuterTableCSVExporter extends CSVExporter {
 
 	private String innerTableId;
 	private DataTable innerTable;
 
 	public OuterTableCSVExporter(String innerTableId) {
 		this.innerTableId = innerTableId;
 	}
 	
 	public OuterTableCSVExporter(String innerTableId, DataTable innerTable) {
 		this.innerTableId = innerTableId;
 		this.innerTable = innerTable;
 	}
 	
 	@Override
 	public String export(FacesContext facesContext, DataExporter component, DataTable table) throws IOException {
 		setUp(component, table);
 		StringBuilder builder = new StringBuilder();
 		List<UIColumn> columns = getColumnsToExport(table, excludeColumns);
 		if (this.innerTable == null) {
 			setInnerTable(table);
 		}
 		List<UIColumn> innerColumns = getColumnsToExport(this.innerTable, null);
 		List<UIColumn> innerTableHeaders = getInnerTableHeaders(innerColumns);
     	
     	if (includeHeaders) {
 			ColumnGroup columnGroup = getColumnGroupHeader(table);
 			if (columnGroup != null) {
 				ArrayList<Row> rows = (ArrayList<Row>) getRows(columnGroup);
 				int size = rows.size();
 				for (int i = 0; i < size; i++) {
 					Row row = rows.get(i);
 					List<UIColumn> rowColumns = getRowColumnsToExport(row, table, excludeColumns);
 					if (i < (size-1)) {
 						addFacetColumns(builder, rowColumns, ColumnType.HEADER);
 					} else {
 						List<UIColumn> allColumns = new ArrayList<UIColumn>();
 						allColumns.addAll(rowColumns);
 						if (innerTableHeaders != null) {
 							allColumns.addAll(innerTableHeaders);
 						}
 						addFacetColumns(builder, allColumns, ColumnType.HEADER);
 					}
 				}
 			} else {
 				List<UIColumn> allColumns = new ArrayList<UIColumn>();
 				allColumns.addAll(columns);
 				if (innerTableHeaders != null) {
 					allColumns.addAll(innerTableHeaders);
 				}
 				addFacetColumns(builder, allColumns, ColumnType.HEADER);
 			}
 		}
     	
 		int rowCount = table.getRowCount();
     	int first = pageOnly ? table.getFirst() : 0;
     	int size = pageOnly ? (first + table.getRows()) : rowCount;
 		size = size > rowCount ? rowCount : size;
 
 		RowStateMap rowStateMap = table.getStateMap();    	
 
 		String rowIndexVar = table.getRowIndexVar();
 		rowIndexVar = rowIndexVar == null ? "" : rowIndexVar;
     	for (int i = first; i < size; i++) {
     		table.setRowIndex(i);
 			boolean exportRow = true;
 			Object rowData = table.getRowData();
 			if (selectedRowsOnly) {
 				RowState rowState = rowStateMap.get(rowData);
 				if (!rowState.isSelected()) exportRow = false;
 			}
 			if (exportRow) {
 				if (!"".equals(rowIndexVar)) {
 					facesContext.getExternalContext().getRequestMap().put(rowIndexVar, i);
 				}
 				StringBuilder rowBuilder = new StringBuilder();
 				addColumnValues(rowBuilder, columns);
 				boolean exportedInnerTables = false;
 				PanelExpansion pe = table.getPanelExpansion();
 				if (pe != null) {
 					exportedInnerTables = exportInnerTables(pe, builder, rowBuilder, facesContext, component);
 				}
 				if (!exportedInnerTables) {
 					builder.append(rowBuilder.toString());
 					builder.append("\n");
 				}
 			}
 		}
 
         if (hasColumnFooter(columns) && includeFooters) {
             addFacetColumns(builder, columns, ColumnType.FOOTER);
         }
     	
     	table.setRowIndex(-1);
         
 		byte[] bytes = builder.toString().getBytes();
 		
 		return registerResource(bytes, filename + ".csv", "text/csv");
 	}
 	
 	private boolean exportInnerTables(UIComponent uiComponent, StringBuilder builder, StringBuilder rowBuilder, FacesContext facesContext, DataExporter dataExporter) throws IOException {
 		boolean exportedInnerTables = false;
 		for (UIComponent kid : uiComponent.getChildren()) {
 			String id = kid.getId();
 			if (kid instanceof DataTable) {
 				if (innerTableId == null || "".equals(innerTableId) || innerTableId.equals(id)) {
 					exportedInnerTables = true;
 					InnerTableCSVExporter innerExporter = new InnerTableCSVExporter(rowBuilder.toString() + ",");
 					String innerTable = innerExporter.export(facesContext, dataExporter, (DataTable) kid);
 					builder.append(innerTable);
 					break;
 				}
 			}
 			if (kid.getChildren().size() > 0) {
 				exportedInnerTables = exportInnerTables(kid, builder, rowBuilder, facesContext, dataExporter);
 			}
 		}
 		return exportedInnerTables;
 	}
 	
 	private void setInnerTable(DataTable table) throws IOException {
 		try {
 			PanelExpansion pe = table.getPanelExpansion();
 			if (pe != null) {
				this.innerTable = findInnerTable(pe);
 				if (this.innerTable == null) {
 					throw new NullPointerException("Required inner table not found in outer table with id '" + table.getId() + "'.");
 				}
 			} else {
 				throw new NullPointerException("Outer table with id '" + table.getId() + "' doesn't have required ace:panelExpansion.");
 			}
 		} catch (NullPointerException npe) {
 			npe.printStackTrace();
 		}
 	}
 	
	private DataTable findInnerTable(UIComponent component) throws IOException {
		DataTable result = null;
		for (UIComponent kid : component.getChildren()) {
			String id = kid.getId();
			if (kid instanceof DataTable) {
				if (this.innerTableId == null || "".equals(this.innerTableId) || this.innerTableId.equals(id)) {
					result = (DataTable) kid;
					break;
				}
			}
			if (kid.getChildren().size() > 0) {
				result = findInnerTable(kid);
				if (result != null) break;
			}
		}
		return result;
	}
	
 	private List<UIColumn> getInnerTableHeaders(List<UIColumn> innerColumns) {
 		ColumnGroup columnGroup = getColumnGroupHeader(this.innerTable);
 		if (columnGroup != null) {
 			ArrayList<Row> rows = (ArrayList<Row>) getRows(columnGroup);
 			int size = rows.size();
 			Row row = rows.get(size-1);
 			return getRowColumnsToExport(row, this.innerTable, new int[0]);
 		} else {
 			return innerColumns;
 		}
 	}
 }
