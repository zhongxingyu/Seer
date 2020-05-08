 /*
  * Copyright 2010 the original author or authors.
  * Copyright 2009 Paxxis Technology LLC
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.paxxis.chime.client.widgets;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import com.extjs.gxt.ui.client.event.BoxComponentEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.util.Margins;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnData;
 import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
 import com.extjs.gxt.ui.client.widget.grid.Grid;
 import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
 import com.extjs.gxt.ui.client.widget.layout.RowData;
 import com.extjs.gxt.ui.client.widget.layout.RowLayout;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.DeferredCommand;
 import com.paxxis.chime.client.Utils;
 import com.paxxis.chime.client.common.DataField;
 import com.paxxis.chime.client.common.DataFieldValue;
 import com.paxxis.chime.client.common.DataInstance;
 import com.paxxis.chime.client.common.Shape;
 import com.paxxis.chime.client.portal.DataRowModel;
 
 /**
  * A grid cell renderer for Chime field data.  The renderer generates the appropriate widget
  * based on the kind of data in the field, including tabular data.
  *  
  * @author Robert Englander
  *
  */
 public class FieldDataGridCellRenderer implements GridCellRenderer<DataRowModel> {
 	private static final int MINHEIGHT = 100;
 	private static final int MAXHEIGHT = 400;
 	private static final int GRIDINCR = 30;
 	
 	/** the default margins for placing the renderer in the cell */
 	private Margins margins = new Margins(3, 3, 3, 0);
 	
 	public FieldDataGridCellRenderer() {
 	}
 	
 	/**
 	 * Constructor
 	 * @param m the margins to use when placing the renderer in the cell.
 	 */
 	public FieldDataGridCellRenderer(Margins m) {
 		margins = m;
 	}
 	
 	@Override
 	public Object render(DataRowModel model, String property,
 			ColumnData config, int rowIndex, int colIndex,
 			ListStore<DataRowModel> store, Grid<DataRowModel> grid) {
 
 		// put the component inside of a basic layout container so that
 		// the margins can be applied.
 		final LayoutContainer lc = new LayoutContainer();
 
 		if (property.equals(DataRowModel.NAME)) {
 			return model.get(property);
 		} else if (model instanceof DataFieldValueModel) {
 			DataFieldValueModel fieldValueModel = (DataFieldValueModel)model;
 			DataInstance inst = fieldValueModel.getDataInstance();
 			Shape shape = fieldValueModel.getShape();
 			DataField dataField = fieldValueModel.getDataField();
 			
 			// if the dataField is null, then the model represents a data instance, and
 			// the field is defined by the property name.
 			if (dataField == null) {
 				dataField = shape.getField(property);
 			}
 
 			Shape fieldShape = dataField.getShape();
 			
 			if (fieldShape.isTabular()) {
 				lc.setLayout(new RowLayout());
 		        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
 
 		        String lastColId = "";
 		        List<DataField> dataFields = fieldShape.getFields();
 		        for (DataField field : dataFields) {
 			        ColumnConfig column = new ColumnConfig();
 			        column.setId(field.getName());
 			        column.setFixed(false);
 			        column.setHeader(field.getName());
 			        column.setWidth(150);
 			        column.setSortable(false);
 			        column.setMenuDisabled(true);
 			        column.setRenderer(new FieldDataGridCellRenderer());
 			        configs.add(column);
 			        lastColId = field.getName();
 		        }
 
 		        ColumnModel cm = new ColumnModel(configs);
 		        ListStore<DataFieldValueModel> listStore = new ListStore<DataFieldValueModel>();
 		        final ChimeGrid<DataFieldValueModel> fieldGrid = new ChimeGrid<DataFieldValueModel>(listStore, cm);
 		        fieldGrid.getView().setAutoFill(false);
 		        fieldGrid.setSelectionModel(null);
 		        fieldGrid.getView().setForceFit(false);
 		        fieldGrid.setHideHeaders(false);
 		        fieldGrid.setTrackMouseOver(false);
 		        fieldGrid.setStripeRows(true);
 		        fieldGrid.setAutoExpandColumn(lastColId);
 		        fieldGrid.setAutoHeight(false);
 		        fieldGrid.setBorders(true);
 		        
 		        lc.add(fieldGrid, new RowData(1, -1, margins));
 		        lc.addListener(Events.Resize,
 		            new Listener<BoxComponentEvent>() {
 		                public void handleEvent(BoxComponentEvent evt) {
 		                	DeferredCommand.addCommand(
 		                		new Command() {
 		                			public void execute() {
 		                            	lc.layout();
 		                            	fieldGrid.setWidth(lc.getWidth());
 		                            	fieldGrid.getView().refresh(false);
 		                			}
 		                		}
 		                	);
 		                }
 		            }
 		        );
 
 		        List<DataFieldValue> values = model.get(DataFieldValueModel.VALUE);
 		        int gridHeight = 0;
 		        for (DataFieldValue fieldValue : values) {
 		        	DataInstance instance = (DataInstance)fieldValue.getValue();
 		        	DataFieldValueModel fieldModel = new DataFieldValueModel(instance, fieldShape, null);
 		        	listStore.add(fieldModel);
 		        	
 		        	gridHeight += GRIDINCR;
 		        }
 		        
 		        if (gridHeight < MINHEIGHT) {
 		        	gridHeight = MINHEIGHT;
 		        } else if (gridHeight > MAXHEIGHT) {
 		        	gridHeight = MAXHEIGHT;
 		        }
 		        
 		        fieldGrid.setHeight(gridHeight);
 			} else {
 				
 				renderHtml(lc, generateContent(inst, shape, dataField));
 			}
 			
 			return lc;
 		} else {
 			renderHtml(lc, model.get(DataRowModel.VALUE).toString());
 			return lc;
 		}
 	}
 
 	private void renderHtml(final LayoutContainer lc, String content) {
 		lc.setLayout(new RowLayout());
 		final InterceptedHtml html = new InterceptedHtml();
 		html.setHtml(content);
         lc.add(html, new RowData(1, -1, margins));
         lc.addListener(Events.Resize,
             new Listener<BoxComponentEvent>() {
                 public void handleEvent(BoxComponentEvent evt) {
                 	DeferredCommand.addCommand(
                 		new Command() {
                 			public void execute() {
                             	lc.layout();
                             	html.setWidth(lc.getWidth());
                 			}
                 		}
                 	);
                 }
             }
         );
 	}
 	
     private String generateContent(DataInstance dataInstance, Shape shape, DataField dataField) {
     	StringBuffer buffer = new StringBuffer();
         List<DataFieldValue> values = dataInstance.getFieldValues(shape, dataField);
         String stringContent = "";
         String sep = "";
         boolean isInternal = dataField.getShape().isPrimitive();
         for (DataFieldValue value : values) {
             String valueName = value.getValue().toString().trim();
             boolean isImageReference = false;
 
             if (isInternal) {
                 if (shape.getId().equals(Shape.IMAGE_ID) &&
                         dataField.getId().equals(Shape.FILE_ID)) {
                     isImageReference = true;
                     stringContent = sep + value.getValue();
                 }
                 else if (dataField.getShape().getId().equals(Shape.URL_ID))
                 {
                 	String vname = valueName.replaceAll(" ", "&nbsp;");
                     String name = Utils.toExternalUrl(valueName, vname);
                     buffer.append(sep + name);
                     sep = "    ";
                 }
                 else if (dataField.getShape().isNumeric())
                 {
                     // TODO when formatting is added to the field definition, we'll
                     // apply whatever is specified
                     Double dval = Double.valueOf(value.getValue().toString());
                     NumberFormat fmt = NumberFormat.getDecimalFormat();
                     String formatted = fmt.format(dval);
 
                     buffer.append(sep + formatted);
                     sep = "    ";
                 }
                 else if (dataField.getShape().isDate())
                 {
                     Date dval = (Date)value.getValue();
                     DateTimeFormat dtf = DateTimeFormat.getFormat("MMM d, yyyy");
                     String formatted = dtf.format(dval);
                     buffer.append(sep + formatted);
                     sep = "    ";
                 }
                 else
                 {
                     buffer.append(sep + value.getValue());
                     sep = "<br>";
                 }
 
                 if (!isImageReference) {
                     String temp = buffer.toString();
                     temp = temp.replaceAll("<ul", "<ul class='forceul'");
                     temp = temp.replaceAll("<ol", "<ol class='forceol'");
                     stringContent = temp;
                 }
             } else {
             	String vname = valueName.replaceAll(" ", "&nbsp;");
                 String name = Utils.toHoverUrl(value.getReferenceId(), vname);
                 buffer.append(sep + name);
                 stringContent = buffer.toString();
                 sep = "    ";
             }
         }
 
         return stringContent;
     }
 
 }
