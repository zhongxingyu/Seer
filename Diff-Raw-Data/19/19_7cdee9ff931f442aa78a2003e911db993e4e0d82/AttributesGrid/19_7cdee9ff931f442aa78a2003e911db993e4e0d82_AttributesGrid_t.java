 /*
  * Copyright 2011 Google Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.cotrix.web.codelistmanager.client.codelist;
 
 import java.util.Collections;
 import java.util.EnumMap;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.cotrix.web.codelistmanager.client.codelist.event.AttributeChangedEvent;
 import org.cotrix.web.codelistmanager.client.codelist.event.AttributeChangedEvent.AttributeChangedHandler;
 import org.cotrix.web.codelistmanager.client.codelist.event.AttributeChangedEvent.HasAttributeChangedHandlers;
 import org.cotrix.web.share.client.util.EventUtil;
 import org.cotrix.web.share.client.widgets.DoubleClickEditTextCell;
 import org.cotrix.web.share.shared.UIAttribute;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.cell.client.Cell.Context;
 import com.google.gwt.cell.client.ClickableTextCell;
 import com.google.gwt.cell.client.FieldUpdater;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.builder.shared.BodyBuilder;
 import com.google.gwt.dom.builder.shared.DivBuilder;
 import com.google.gwt.dom.builder.shared.TableBuilder;
 import com.google.gwt.dom.builder.shared.TableCellBuilder;
 import com.google.gwt.dom.builder.shared.TableRowBuilder;
 import com.google.gwt.dom.client.BrowserEvents;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.NodeList;
 import com.google.gwt.dom.client.Style.BorderStyle;
 import com.google.gwt.dom.client.Style.FontWeight;
 import com.google.gwt.dom.client.Style.OutlineStyle;
import com.google.gwt.dom.client.Style.Overflow;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.cellview.client.AbstractCellTable.Style;
 import com.google.gwt.user.cellview.client.AbstractCellTableBuilder;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.cellview.client.DataGrid;
 import com.google.gwt.user.cellview.client.Header;
 import com.google.gwt.user.cellview.client.RowStyles;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
 import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ResizeComposite;
 import com.google.gwt.view.client.ListDataProvider;
 import com.google.gwt.view.client.SelectionModel;
 import com.google.gwt.view.client.SingleSelectionModel;
 
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class AttributesGrid extends ResizeComposite implements HasAttributeChangedHandlers {
 	
 	protected enum AttributeField {NAME, TYPE, LANGUAGE, VALUE};
 
 	interface DataGridResources extends DataGrid.Resources {
 
 		@Source("AttributesGrid.css")
 		DataGridStyle dataGridStyle();
 	}
 
 	interface DataGridStyle extends DataGrid.Style {
 	}
 
 	DataGrid<UIAttribute> dataGrid;
 	
 	private DataGridResources resource = GWT.create(DataGridResources.class);
 
 	private final Set<String> showExpanded = new HashSet<String>();
 
 	protected Map<String, EnumMap<AttributeField, Column<UIAttribute, String>>> attributesPropertiesColumns = new HashMap<String, EnumMap<AttributeField,Column<UIAttribute,String>>>();
 
 	private Column<UIAttribute, String> attributeNameColumn;
 
 	private ListDataProvider<UIAttribute> dataProvider;
 
 	private Header<String> header;
 
 	public AttributesGrid(ListDataProvider<UIAttribute> dataProvider, Header<String> header) {
 		
 		this.dataProvider = dataProvider;
 		this.header = header;
 
 		dataGrid = new DataGrid<UIAttribute>(20, resource);
 		
 		//We need to listen dbclick events in order to enable editing
 		EventUtil.sinkEvents(dataGrid, Collections.singleton(BrowserEvents.DBLCLICK));
 		
 		dataGrid.setAutoHeaderRefreshDisabled(true);
 		dataGrid.setEmptyTableWidget(new Label("Empty"));
 
 		setupColumns();
 
 		// Add a selection model so we can select cells.
 		final SelectionModel<UIAttribute> selectionModel = new SingleSelectionModel<UIAttribute>();
 		dataGrid.setSelectionModel(selectionModel);
 
 		// Specify a custom table.
 		dataGrid.setTableBuilder(new CustomTableBuilder());
 
 		dataProvider.addDataDisplay(dataGrid);
 
 		initWidget(dataGrid);
 	}
 	
 	public Column<UIAttribute, String> getAttributePropertyColumn(final String name, final AttributeField field)
 	{
 		EnumMap<AttributeField, Column<UIAttribute, String>> attributePropertiesColumns = attributesPropertiesColumns.get(name);
 		if (attributePropertiesColumns == null) {
 			attributePropertiesColumns = new EnumMap<AttributesGrid.AttributeField, Column<UIAttribute,String>>(AttributeField.class);
 			attributesPropertiesColumns.put(name, attributePropertiesColumns);
 		}
 		
 		Column<UIAttribute, String> column = attributePropertiesColumns.get(field);
 		if (column == null) {
 			DoubleClickEditTextCell cell = new DoubleClickEditTextCell();
 			column = new Column<UIAttribute, String>(cell) {
 
 				@Override
 				public String getValue(UIAttribute attribute) {
 					if (attribute == null) return "";
 					switch (field) {
 						case NAME: return attribute.getName();
 						case LANGUAGE: return attribute.getLanguage();
 						case TYPE: return attribute.getType();
 						case VALUE: return attribute.getValue();
 						default: return "";
 					}
 				}
 			};
 			
 			column.setFieldUpdater(new FieldUpdater<UIAttribute, String>() {
 
 				@Override
 				public void update(int index, UIAttribute attribute, String value) {
 					String oldName = attribute.getName();
 					switch (field) {
 						case NAME: attribute.setName(value); break;
 						case LANGUAGE: attribute.setLanguage(value); break;
 						case TYPE: attribute.setType(value); break;
 						case VALUE: attribute.setValue(value); break;
 					}
 					AttributeChangedEvent.fire(AttributesGrid.this, oldName, attribute);
 				}
 			});
 			
 			attributePropertiesColumns.put(field, column);
 		}
 		return column;
 	}
 
 	protected void refreshAttribute(String attributeName)
 	{
 		Log.trace("refreshAttribute attributeName: "+attributeName);
 		List<UIAttribute> attributes = dataProvider.getList();
 		for (int i = 0; i < attributes.size(); i++) {
 			UIAttribute attribute = attributes.get(i);
 			if (attribute.getName().equals(attributeName)) {
 				attributes.set(i, attribute);
 				return;
 			}
 		}
 		Log.warn("attribute "+attributeName+" not found in data provider");
 	}
 
 	private void setupColumns() {
 
 		attributeNameColumn = new Column<UIAttribute, String>(new ClickableTextCell()) {
 			@Override
 			public String getValue(UIAttribute object) {
 				return object.getName();
 			}
 		};
 
 		attributeNameColumn.setFieldUpdater(new FieldUpdater<UIAttribute, String>() {
 			@Override
 			public void update(int index, UIAttribute object, String value) {
 
 				Log.trace("expand "+index+" "+object.getId());
 
 				if (showExpanded.contains(object.getId())) {
 					showExpanded.remove(object.getId());
 				} else {
 					showExpanded.add(object.getId());
 				}
 
 				// Redraw the modified row.
 				dataGrid.redrawRow(index);
 			}
 		});
 
 		dataGrid.addColumn(attributeNameColumn, header);
 	}
 	
 	public void removeUnusedDataGridColumns() {
 		removeUnusedDataGridColumns(dataGrid);
 	}
 
 	/**
 	 * Workaround issue #6711
 	 * https://code.google.com/p/google-web-toolkit/issues/detail?id=6711
 	 * @param dataGrid
 	 */
 	protected static void removeUnusedDataGridColumns(DataGrid<?> dataGrid) {
 		int columnCount = dataGrid.getColumnCount();
 		NodeList<Element> colGroups = dataGrid.getElement().getElementsByTagName("colgroup");
 
 		for (int i = 0; i < colGroups.getLength(); i++) {
 			Element colGroupEle = colGroups.getItem(i);
 			NodeList<Element> colList = colGroupEle.getElementsByTagName("col");
 
 			for (int j = colList.getLength()-1; j >= columnCount; j--) {
 				colGroupEle.removeChild(colList.getItem(j));
 			}
 		}
 	}
 
 	private class CustomTableBuilder extends AbstractCellTableBuilder<UIAttribute> {
 
 		private final String evenRowStyle;
 		private final String oddRowStyle;
 		private final String selectedRowStyle;
 		private final String cellStyle;
 		private final String evenCellStyle;
 		private final String oddCellStyle;
 		private final String selectedCellStyle;
 		private final String firstColumnStyle;
 		private final String lastColumnStyle;
 
 		public CustomTableBuilder() {
 			super(dataGrid);
 
 			// Cache styles for faster access.
 			Style style = dataGrid.getResources().style();
 			evenRowStyle = style.evenRow();
 			oddRowStyle = style.oddRow();
 			selectedRowStyle = " " + style.selectedRow();
 			cellStyle = style.cell() + " " + style.evenRowCell();
 			selectedCellStyle = " " + style.selectedRowCell();
 			evenCellStyle = " " + style.evenRowCell();
 			oddCellStyle = " " + style.oddRowCell();
 			firstColumnStyle = " " + style.firstColumn();
 			lastColumnStyle = " " + style.lastColumn();
 		}
 
 
 
 		@Override
 		public void buildRowImpl(UIAttribute rowValue, int absRowIndex) {
 			buildStandarRow(rowValue, absRowIndex);
 
 			if (showExpanded.contains(rowValue.getId())) {
 
 				TableRowBuilder row = startRow();
 				TableCellBuilder td = row.startTD().colSpan(dataGrid.getColumnCount()).className(resource.dataGridStyle().dataGridCell());
 				td.style().trustedBackgroundColor("#efefef").endStyle();
 
 				TableBuilder table = td.startTable();
 				buildPropertiesTable(table, rowValue, absRowIndex);
 
 				td.end();
 				row.endTR();
 			}
 		}
 
 		protected void buildPropertiesTable(TableBuilder table, UIAttribute rowValue, int absRowIndex)
 		{
 			Log.trace("buildPropertiesTable for row "+rowValue.getId());
 
 			table.style().borderStyle(BorderStyle.SOLID).borderWidth(1, Unit.PX).trustedBorderColor("#8C8C8C")
 			.trustedProperty("border-collapse", "collapse").width(100, Unit.PCT);
 
 			BodyBuilder body = table.startBody();
 
 			UIAttribute attribute = rowValue;
 
 
 			addRow(body, "Name", attribute.getName(), absRowIndex, rowValue, AttributeField.NAME);
 
 			addRow(body, "Type", attribute.getType(), absRowIndex, rowValue, AttributeField.TYPE);
 
 			addRow(body, "Language", attribute.getLanguage(), absRowIndex, rowValue, AttributeField.LANGUAGE);
 
 			addRow(body, "Value", attribute.getValue(), absRowIndex, rowValue, AttributeField.VALUE);
 
 			body.end();
 			table.end();
 		}
 
 		protected void addRow(BodyBuilder body, String label, String value, int absRowIndex, UIAttribute attribute, AttributeField field)
 		{
 			TableRowBuilder tr = body.startTR();
 
 			addCell(tr, label);
 			/*addCell(tr, value);*/
 
 			Column<UIAttribute, String> propColumn = getAttributePropertyColumn(attribute.getName(), field);
 			renderCell(tr, absRowIndex, propColumn, attribute);
 
 			tr.end();
 		}
 
 		protected void addCell(TableRowBuilder tr, String cellValue)
 		{
 			cellValue = cellValue!=null?cellValue:"";
 
 			TableCellBuilder td = tr.startTD();
			setCellStyle(td);
			td.style().fontWeight(FontWeight.BOLD);
 			td.startDiv().text(cellValue).end();
 			td.end();
 		}
 
 		protected void renderCell(TableRowBuilder tr, int absRowIndex, Column<UIAttribute, String> column, UIAttribute rowValue)
 		{
 			TableCellBuilder td = tr.startTD();
			setCellStyle(td);
 
 			Context context = new Context(absRowIndex, -1, "key");
 			renderCell(td, context, column, rowValue);
 
 			td.end();
 		}
		
		protected void setCellStyle(TableCellBuilder td)
		{
			td.style().borderStyle(BorderStyle.SOLID).borderWidth(1, Unit.PX).trustedBorderColor("#C2C2C2")
			.paddingBottom(8, Unit.PX).paddingLeft(10, Unit.PX).paddingRight(8, Unit.PX).paddingTop(8, Unit.PX).overflow(Overflow.HIDDEN);
		}
 
 		public void buildStandarRow(UIAttribute rowValue, int absRowIndex) {
 
 			// Calculate the row styles.
 			SelectionModel<? super UIAttribute> selectionModel = cellTable.getSelectionModel();
 			boolean isSelected =
 					(selectionModel == null || rowValue == null) ? false : selectionModel.isSelected(rowValue);
 			boolean isEven = absRowIndex % 2 == 0;
 			StringBuilder trClasses = new StringBuilder(isEven ? evenRowStyle : oddRowStyle);
 			if (isSelected) {
 				trClasses.append(selectedRowStyle);
 			}
 
 			// Add custom row styles.
 			RowStyles<UIAttribute> rowStyles = cellTable.getRowStyles();
 			if (rowStyles != null) {
 				String extraRowStyles = rowStyles.getStyleNames(rowValue, absRowIndex);
 				if (extraRowStyles != null) {
 					trClasses.append(" ").append(extraRowStyles);
 				}
 			}
 
 			// Build the row.
 			TableRowBuilder tr = startRow();
 			tr.className(trClasses.toString());
 
 			// Build the columns.
 			int columnCount = cellTable.getColumnCount();
 			for (int curColumn = 0; curColumn < columnCount; curColumn++) {
 				Column<UIAttribute, ?> column = cellTable.getColumn(curColumn);
 				// Create the cell styles.
 				StringBuilder tdClasses = new StringBuilder(cellStyle);
 				tdClasses.append(isEven ? evenCellStyle : oddCellStyle);
 				if (curColumn == 0) {
 					tdClasses.append(firstColumnStyle);
 				}
 				if (isSelected) {
 					tdClasses.append(selectedCellStyle);
 				}
 				// The first and last column could be the same column.
 				if (curColumn == columnCount - 1) {
 					tdClasses.append(lastColumnStyle);
 				}
 
 				// Add class names specific to the cell.
 				Context context = new Context(absRowIndex, curColumn, cellTable.getValueKey(rowValue));
 				String cellStyles = column.getCellStyleNames(context, rowValue);
 				if (cellStyles != null) {
 					tdClasses.append(" " + cellStyles);
 				}
 
 				// Build the cell.
 				HorizontalAlignmentConstant hAlign = column.getHorizontalAlignment();
 				VerticalAlignmentConstant vAlign = column.getVerticalAlignment();
 				TableCellBuilder td = tr.startTD();
 				td.className(tdClasses.toString());
 				if (hAlign != null) {
 					td.align(hAlign.getTextAlignString());
 				}
 				if (vAlign != null) {
 					td.vAlign(vAlign.getVerticalAlignString());
 				}
 
 				// Add the inner div.
 				DivBuilder div = td.startDiv();
 				div.style().outlineStyle(OutlineStyle.NONE).endStyle();
 
 				// Render the cell into the div.
 				renderCell(div, context, column, rowValue);
 
 				// End the cell.
 				div.endDiv();
 				td.endTD();
 			}
 
 			// End the row.
 			tr.endTR();
 		}
 	}
 
 	public void redrawHeaders() {
 		dataGrid.redrawHeaders();		
 	}
 
 	public void insertColumn(int beforeIndex, Column<UIAttribute, ?> col) {
 		dataGrid.insertColumn(beforeIndex, col, header);
 	}
 
 	/**
 	 * @param column
 	 * @param width
 	 * @param unit
 	 * @see com.google.gwt.user.cellview.client.AbstractCellTable#setColumnWidth(int, double, com.google.gwt.dom.client.Style.Unit)
 	 */
 	public void setColumnWidth(int column, double width, Unit unit) {
 		dataGrid.setColumnWidth(column, width, unit);
 	}
 
 	/**
 	 * @param absRowIndex
 	 * @see com.google.gwt.user.cellview.client.AbstractHasData#redrawRow(int)
 	 */
 	public void redrawRow(int absRowIndex) {
 		dataGrid.redrawRow(absRowIndex);
 	}
 
 	/**
 	 * @param col
 	 * @see com.google.gwt.user.cellview.client.AbstractCellTable#removeColumn(com.google.gwt.user.cellview.client.Column)
 	 */
 	public void removeColumn(Column<UIAttribute, ?> col) {
 		dataGrid.removeColumn(col);
 	}
 
 	@Override
 	public HandlerRegistration addAttributeChangedHandler(AttributeChangedHandler handler) {
 		return addHandler(handler, AttributeChangedEvent.getType());
 	}
 }
