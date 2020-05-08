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
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.cotrix.web.codelistmanager.client.codelist.event.AttributeSetChangedEvent;
 import org.cotrix.web.codelistmanager.client.codelist.event.AttributeSetChangedEvent.AttributeSetChangedHandler;
 import org.cotrix.web.codelistmanager.client.codelist.event.AttributeSwitchType;
 import org.cotrix.web.codelistmanager.client.codelist.event.AttributeSwitchedEvent;
 import org.cotrix.web.codelistmanager.client.codelist.event.RowSelectedEvent;
 import org.cotrix.web.codelistmanager.client.codelist.event.SwitchAttributeEvent;
 import org.cotrix.web.codelistmanager.client.data.CodelistRowEditor;
 import org.cotrix.web.codelistmanager.client.data.event.DataEditEvent;
 import org.cotrix.web.codelistmanager.client.data.event.DataEditEvent.DataEditHandler;
 import org.cotrix.web.codelistmanager.client.event.EditorBus;
 import org.cotrix.web.codelistmanager.shared.UICodelistRow;
 import org.cotrix.web.share.client.widgets.DoubleClickEditTextCell;
 import org.cotrix.web.share.shared.UIAttribute;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.cell.client.FieldUpdater;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.NodeList;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.resources.client.ImageResource;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.cellview.client.PatchedDataGrid;
 import com.google.gwt.user.cellview.client.SimplePager;
 import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
 import com.google.gwt.user.client.ui.ImageResourceRenderer;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ResizeComposite;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.view.client.SelectionChangeEvent;
 import com.google.gwt.view.client.SingleSelectionModel;
 import com.google.inject.Inject;
 import com.google.web.bindery.event.shared.EventBus;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class CodelistEditor extends ResizeComposite implements AttributeSetChangedHandler {
 
 	interface Binder extends UiBinder<Widget, CodelistEditor> { }
 
 	interface DataGridResources extends PatchedDataGrid.Resources {
 
 		@Source("CodelistEditor.css")
 		DataGridStyle dataGridStyle();
 		
 		@Source("plus.png")
 		ImageResource add();
 		
 		@Source("minus.png")
 		ImageResource remove();
 	}
 
 	interface DataGridStyle extends PatchedDataGrid.Style {
 
 		String groupHeaderCell();
 	}
 
 
 	@UiField(provided = true)
 	PatchedDataGrid<UICodelistRow> dataGrid;
 
 	@UiField(provided = true)
 	SimplePager pager;
 
 	protected ImageResourceRenderer renderer = new ImageResourceRenderer(); 
 	protected DataGridResources resource = GWT.create(DataGridResources.class);
 
 	protected Set<String> attributeAsColumn = new HashSet<String>();
 	protected Map<String, Column<UICodelistRow, String>> attributesColumns = new HashMap<String, Column<UICodelistRow,String>>(); 
 	protected Map<String, Column<UICodelistRow, String>> switchesColumns = new HashMap<String, Column<UICodelistRow,String>>(); 
 
 	private Column<UICodelistRow, String> nameColumn;
 	
 	protected EventBus editorBus;
 
 	protected SingleSelectionModel<UICodelistRow> selectionModel;
 	
 	protected CodelistRowDataProvider dataProvider;
 	protected HandlerRegistration registration;
 	
 	protected CodelistRowEditor rowEditor;
 
 	@Inject
 	public CodelistEditor(@EditorBus EventBus editorBus, CodelistRowDataProvider dataProvider, CodelistRowEditor rowEditor) {
 		this.editorBus = editorBus;
 		this.dataProvider = dataProvider;
 		this.rowEditor = rowEditor;
 
 		dataGrid = new PatchedDataGrid<UICodelistRow>(20, resource, CodelistRowKeyProvider.INSTANCE);
 		dataGrid.setAutoHeaderRefreshDisabled(true);
 		dataGrid.setEmptyTableWidget(new Label("Empty"));
 		dataGrid.setTableWidth(100, Unit.PCT);
 
 		//TODO add sorting
 
 		// Create a Pager to control the table.
 		SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
 		pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
 		pager.setDisplay(dataGrid);
 
 		setupColumns();
 
 
 		selectionModel = new SingleSelectionModel<UICodelistRow>(CodelistRowKeyProvider.INSTANCE);
 		dataGrid.setSelectionModel(selectionModel);
 
 		// Specify a custom table.
 		//dataGrid.setTableBuilder(new CustomTableBuilder());
 		
 		dataProvider.addDataDisplay(dataGrid);
 		
 		bind();
 
 		// Create the UiBinder.
 		Binder uiBinder = GWT.create(Binder.class);
 		initWidget(uiBinder.createAndBindUi(this));
 	}
 	
 	protected void setupColumns() {
 
 		nameColumn = new Column<UICodelistRow, String>(new DoubleClickEditTextCell()) {
 			@Override
 			public String getValue(UICodelistRow object) {
 				return object.getName();
 			}
 		};
 		
 		nameColumn.setFieldUpdater(new FieldUpdater<UICodelistRow, String>() {
 			
 			@Override
 			public void update(int index, UICodelistRow row, String value) {
 				row.setName(value);
 				rowEditor.edited(row);
 			}
 		});
 		
 		//ResizableHeader<UICodelistRow> header = new ResizableHeader<UICodelistRow>("Code", dataGrid, nameColumn);
 
 		dataGrid.addColumn(nameColumn, "Code");
 		//dataGrid.setColumnWidth(2, 1000, Unit.PX);
 	}
 
 	public void showAllAttributesAsColumn()
 	{
 		if (registration == null) registration = dataProvider.addAttributeSetChangedHandler(this);
 		switchAllAttributesToColumn();
 	}
 	
 	public void showAllAttributesAsNormal()
 	{
 		if (registration!=null) registration.removeHandler();
 		registration = null;
 		switchAllAttributesToNormal();
 	}
 	
 	protected void bind()
 	{
 		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 			
 			@Override
 			public void onSelectionChange(SelectionChangeEvent event) {
 				UICodelistRow row = selectionModel.getSelectedObject();
 				Log.trace("onSelectionChange row: "+row);
 				if (row !=null) editorBus.fireEvent(new RowSelectedEvent(row));
 			}
 		});
 		
 		editorBus.addHandler(SwitchAttributeEvent.TYPE, new SwitchAttributeEvent.SwitchAttributeHandler() {
 			
 			@Override
 			public void onSwitchAttribute(SwitchAttributeEvent event) {
 				String attributeName = event.getAttributeName();
 				Log.trace("onSwitchAttribute attribute: "+attributeName+" type: "+event.getSwitchType());
 				switch (event.getSwitchType()) {
 					case TO_COLUMN: switchToColumn(attributeName); break;
 					case TO_NORMAL: switchToNormal(attributeName); break;
 				}
 			}
 		});
 		
 		rowEditor.addDataEditHandler(new DataEditHandler<UICodelistRow>() {
 
 			@Override
 			public void onDataEdit(DataEditEvent<UICodelistRow> event) {
 				Log.trace("onDataEdit row: "+event.getData());
 				int index = dataGrid.getVisibleItems().indexOf(event.getData());
 				Log.trace("index: "+index);
 				if (index>=0) dataGrid.redrawRow(index);
 			}
 		});
 	}
 
 	protected Column<UICodelistRow, String> getAttributeColumn(final String name)
 	{
 		Column<UICodelistRow, String> column = attributesColumns.get(name);
 		if (column == null) {
 			DoubleClickEditTextCell cell = new DoubleClickEditTextCell();
 			column = new Column<UICodelistRow, String>(cell) {
 
 				@Override
 				public String getValue(UICodelistRow row) {
 					if (row == null) return "";
 					UIAttribute attribute = row.getAttribute(name);
 					if (attribute == null) return "";
 					return attribute.getValue();
 				}
 			};
 			column.setFieldUpdater(new FieldUpdater<UICodelistRow, String>() {
 
 				@Override
 				public void update(int index, UICodelistRow row, String value) {
 					UIAttribute attribute = row.getAttribute(name);
 					attribute.setValue(value);
 					rowEditor.edited(row);
 				}
 			});
 			
 			attributesColumns.put(name, column);
 		}
 		return column;
 	}
 
 	protected void switchToColumn(String attributeName)
 	{
 		addAttributeColumn(attributeName);
 		editorBus.fireEvent(new AttributeSwitchedEvent(attributeName, AttributeSwitchType.TO_COLUMN));
 	}
 	
 	protected void addAttributeColumn(final String attributeName)
 	{
 		if (attributeAsColumn.contains(attributeName)) return;
 		Column<UICodelistRow, String> column = getAttributeColumn(attributeName);
 		attributeAsColumn.add(attributeName);
 
 		
 		//ResizableHeader<UICodelistRow> resizableHeader = new ResizableHeader<UICodelistRow>(attributeName, dataGrid, column);
 		dataGrid.addColumn(column, attributeName);
 		//dataGrid.setColumnWidth(dataGrid.getColumnCount()-1, 1, Unit.EM);
 		//dataGrid.clearTableWidth();
 	}
 	
 	protected void switchToNormal(String attributeName)
 	{
 		removeAttributeColumn(attributeName);
 		editorBus.fireEvent(new AttributeSwitchedEvent(attributeName, AttributeSwitchType.TO_NORMAL));
 	}
 	
 	protected void removeAttributeColumn(String attributeName)
 	{
 		if (!attributeAsColumn.contains(attributeName)) return;
 		Column<UICodelistRow, String> column = getAttributeColumn(attributeName);
 		attributeAsColumn.remove(attributeName);
 		dataGrid.removeColumn(column);
 		//removeUnusedDataGridColumns(dataGrid);
 	}
 	
 	/**
 	 * Workaround issue #6711
 	 * https://code.google.com/p/google-web-toolkit/issues/detail?id=6711
 	 * @param dataGrid
 	 */
 	public static void removeUnusedDataGridColumns(PatchedDataGrid<?> dataGrid) {
 		Log.trace("removeUnusedDataGridColumns");
 		int columnCount = dataGrid.getColumnCount();
 		NodeList<Element> colGroups = dataGrid.getElement().getElementsByTagName("colgroup");
 
 		Log.trace("  found "+colGroups.getLength()+" col groups");
 		
 		for (int i = 0; i < colGroups.getLength(); i++) {
 			Element colGroupEle = colGroups.getItem(i);
 			colGroupEle.setId("GROUP"+i);
 			Log.trace("checking group "+i+" ancestor: "+colGroupEle.getParentNode());
 			
 			NodeList<Element> colList = colGroupEle.getElementsByTagName("col");
 			Log.trace("  found "+colList.getLength()+" cols in group "+i+" expected "+columnCount);
 			
 			for (int j = colList.getLength()-1; j >= columnCount; j--) {
 				colGroupEle.removeChild(colList.getItem(j));
 				Log.trace("    removing group "+j);
 			}
 		}
 	}
 
 	@Override
 	public void onAttributeSetChanged(AttributeSetChangedEvent event) {
 		Set<String> attributes = event.getAttributesNames();
 		Log.trace("onAttributeSetChanged attributes: "+attributes);
 		
 		Set<String> columnsToRemove = new HashSet<String>(attributeAsColumn);
 		columnsToRemove.removeAll(attributes);
 		Log.trace("columns to remove: "+columnsToRemove);
 		
 		for (String toRemove:columnsToRemove) removeAttributeColumn(toRemove);
 		
 		for (String attribute:attributes) addAttributeColumn(attribute);
 	}
 	
 	public void switchAllAttributesToColumn() {
 		Log.trace("switchAllAttributesToColumn");
 		
 		Set<String> attributes = new HashSet<String>();
 		
 		for (UICodelistRow row:dataGrid.getVisibleItems()) attributes.addAll(row.getAttributesNames());
 		Log.trace("attributes: "+attributes);
 		
 		attributes.removeAll(attributeAsColumn);	
 		Log.trace("attributes to add: "+attributes);
 		
 		for (String attribute:attributes) switchToColumn(attribute);
 	}
 	
 	public void switchAllAttributesToNormal() {
 		Set<String> attributesToNormal = new HashSet<String>(attributeAsColumn);
 		for (String attribute:attributesToNormal) switchToNormal(attribute);
 	}
 }
