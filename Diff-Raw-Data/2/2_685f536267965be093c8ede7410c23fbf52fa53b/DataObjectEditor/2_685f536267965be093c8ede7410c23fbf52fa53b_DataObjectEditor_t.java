 /**
  * Copyright 2012 JBoss Inc
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
 
 package org.jbpm.datamodeler.editor.client.editors;
 
 import com.github.gwtbootstrap.client.ui.Breadcrumbs;
 import com.github.gwtbootstrap.client.ui.Button;
 import com.github.gwtbootstrap.client.ui.CellTable;
 import com.github.gwtbootstrap.client.ui.TooltipCellDecorator;
 import com.github.gwtbootstrap.client.ui.constants.IconType;
 import com.google.gwt.cell.client.Cell;
 import com.google.gwt.cell.client.FieldUpdater;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Style;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.resources.client.ImageResource;
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.cellview.client.ColumnSortEvent;
 import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
 import com.google.gwt.user.cellview.client.TextColumn;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.ui.*;
 import com.google.gwt.view.client.ListDataProvider;
 import com.google.gwt.view.client.SelectionChangeEvent;
 import com.google.gwt.view.client.SingleSelectionModel;
 import org.jbpm.datamodeler.editor.client.editors.resources.i18n.Constants;
 import org.jbpm.datamodeler.editor.client.editors.resources.images.ImagesResources;
 import org.jbpm.datamodeler.editor.model.DataModelTO;
 import org.jbpm.datamodeler.editor.model.DataObjectTO;
 import org.jbpm.datamodeler.editor.model.ObjectPropertyTO;
 import org.jbpm.datamodeler.editor.model.PropertyTypeTO;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 
 public class DataObjectEditor  extends Composite {
 
     interface DataObjectEditorUIBinder
             extends UiBinder<Widget, DataObjectEditor> {
 
     };
 
     private static DataObjectEditorUIBinder uiBinder = GWT.create(DataObjectEditorUIBinder.class);
 
     @UiField
     VerticalPanel mainPanel;
 
     @UiField (provided = true)
     Breadcrumbs dataObjectNavigation = new DataObjectBreadcrums(5);
 
     @UiField
     Label objectName;
 
     @UiField(provided = true)
     CellTable<ObjectPropertyTO> dataObjectPropertiesTable = new CellTable<ObjectPropertyTO>(1000, GWT.<CellTable.SelectableResources>create(CellTable.SelectableResources.class));
 
     @UiField
     TextBox newPropertyName;
 
     @UiField
     com.github.gwtbootstrap.client.ui.ListBox newPropertyType;
 
     @UiField
     Button newPropertyButton;
 
     @UiField
     com.github.gwtbootstrap.client.ui.RadioButton newPropertyBasicType;
 
     @UiField
     com.github.gwtbootstrap.client.ui.RadioButton newPropertyDataObjectType;
 
     @UiField
     com.github.gwtbootstrap.client.ui.CheckBox newPropertyIsMultiple;
 
     //@UiField(provided = true)
     //SimplePager pager = new SimplePager(SimplePager.TextLocation.RIGHT, false, true);
 
     final SingleSelectionModel<ObjectPropertyTO> selectionModel = new SingleSelectionModel<ObjectPropertyTO>();
 
     private DataModelTO dataModel;
     
     private DataObjectTO dataObject;
 
     private ListDataProvider<ObjectPropertyTO> dataObjectPropertiesProvider = new ListDataProvider<ObjectPropertyTO>();
 
     private List<ObjectPropertyTO> dataObjectProperties = new ArrayList<ObjectPropertyTO>();
 
     private DataModelEditorPresenter modelEditorPresenter;
 
     private List<PropertyTypeTO> baseTypes;
 
     public DataObjectEditor() {
         initWidget(uiBinder.createAndBindUi(this));
 
         objectName.setText(Constants.INSTANCE.objectEditor_objectUnknown());
         dataObjectPropertiesProvider.setList(dataObjectProperties);
 
         //Init data objects table
 
         dataObjectPropertiesTable.setEmptyTableWidget(new com.github.gwtbootstrap.client.ui.Label(Constants.INSTANCE.objectEditor_emptyTable()));
 
         //Init delete column
         ClickableImageResourceCell clickableImageResourceCell = new ClickableImageResourceCell(true);
         final TooltipCellDecorator<ImageResource> decorator = new TooltipCellDecorator<ImageResource>(clickableImageResourceCell);
         decorator.setText(Constants.INSTANCE.objectEditor_action_deleteProperty());
 
         final Column<ObjectPropertyTO, ImageResource> deletePropertyColumnImg = new Column<ObjectPropertyTO, ImageResource>(decorator) {
             @Override
             public ImageResource getValue( final ObjectPropertyTO global ) {
                 return ImagesResources.INSTANCE.Delete();
             }
         };
 
         deletePropertyColumnImg.setFieldUpdater( new FieldUpdater<ObjectPropertyTO, ImageResource>() {
             public void update( final int index,
                                 final ObjectPropertyTO property,
                                 final ImageResource value ) {
 
                 Command deleteCommand = modelEditorPresenter.createDeleteCommand(dataObject, property, index);
                 deleteCommand.execute();
             }
         } );
 
         /// bbb
         /*
         final com.github.gwtbootstrap.client.ui.ButtonCell deletePropertyButton = new com.github.gwtbootstrap.client.ui.ButtonCell();
         deletePropertyButton.setType( ButtonType.DEFAULT );
         deletePropertyButton.setIcon( IconType.REMOVE );
 
         final TooltipCellDecorator<String> decorator = new TooltipCellDecorator<String>(deletePropertyButton);
         decorator.setText("click to delete this property");
 
         final Column<ObjectPropertyTO, String> deletePropertyColumn = new Column<ObjectPropertyTO, String>(decorator) {
             @Override
             public String getValue( final ObjectPropertyTO global ) {
                 return "";
             }
         };
         deletePropertyColumn.setFieldUpdater( new FieldUpdater<ObjectPropertyTO, String>() {
             public void update( final int index,
                                 final ObjectPropertyTO property,
                                 final String value ) {
 
                 Command deleteCommand = modelEditorPresenter.createDeleteCommand(dataObject, property, index);
                 deleteCommand.execute();
             }
         } );
         */
 
         //dataObjectPropertiesTable.addColumn(deletePropertyColumn);
         dataObjectPropertiesTable.addColumn(deletePropertyColumnImg);
         dataObjectPropertiesTable.setColumnWidth(deletePropertyColumnImg, 20, Style.Unit.PX);
 
 
         //Init property name column
 
         final TextColumn<ObjectPropertyTO> propertyNameColumn = new TextColumn<ObjectPropertyTO>() {
 
             @Override
             public void render(Cell.Context context, ObjectPropertyTO object, SafeHtmlBuilder sb) {
                 SafeHtml startDiv = new SafeHtml() {
                     @Override
                     public String asString() {
                         return "<div style=\"cursor: pointer;\">";
                     }
                 };
                 SafeHtml endDiv = new SafeHtml() {
                     @Override
                     public String asString() {
                         return "</div>";
                     }
                 };
 
                 sb.append(startDiv);
                 super.render(context, object, sb);
                 sb.append(endDiv);
             }
 
             @Override
             public String getValue( final ObjectPropertyTO objectProperty) {
                 return objectProperty.getName();
             }
         };
 
 
         propertyNameColumn.setSortable(true);
         dataObjectPropertiesTable.addColumn(propertyNameColumn, Constants.INSTANCE.objectEditor_columnName());
         //dataObjectPropertiesTable.setColumnWidth(propertyNameColumn, 100, Style.Unit.PX);
 
 
         ColumnSortEvent.ListHandler<ObjectPropertyTO> propertyNameColHandler = new ColumnSortEvent.ListHandler<ObjectPropertyTO>(dataObjectPropertiesProvider.getList());
         propertyNameColHandler.setComparator(propertyNameColumn, new ObjectPropertyComparator("name"));
         dataObjectPropertiesTable.addColumnSortHandler(propertyNameColHandler);
 
         //Init property type column
         /*
         final TextColumn<ObjectPropertyTO> propertyTypeColumn = new TextColumn<ObjectPropertyTO>() {
 
             @Override
             public String getValue(final ObjectPropertyTO objectProperty) {
                 return propertyTypeDisplay(objectProperty);
             }
 
         };
         */
         final Column<ObjectPropertyTO, String> propertyTypeColumn = new Column<ObjectPropertyTO, String>(new PropertyTypeCell(true, this))  {
 
             @Override
             public String getValue(final ObjectPropertyTO objectProperty) {
                 return propertyTypeDisplay(objectProperty);
             }
 
         };
         propertyTypeColumn.setSortable(true);
         dataObjectPropertiesTable.addColumn(propertyTypeColumn, Constants.INSTANCE.objectEditor_columnType());
         //dataObjectPropertiesTable.setColumnWidth(propertyTypeColumn, 100, Style.Unit.PX);
 
 
         ColumnSortEvent.ListHandler<ObjectPropertyTO> propertyTypeColHandler = new ColumnSortEvent.ListHandler<ObjectPropertyTO>(dataObjectPropertiesProvider.getList());
         propertyTypeColHandler.setComparator(propertyTypeColumn, new ObjectPropertyComparator("className"));
         dataObjectPropertiesTable.addColumnSortHandler(propertyTypeColHandler);
 
 
         dataObjectPropertiesTable.getColumnSortList().push(propertyTypeColumn);
         dataObjectPropertiesTable.getColumnSortList().push(propertyNameColumn);
 
         //Init the selection model
         dataObjectPropertiesTable.setSelectionModel(selectionModel);
         selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 
             @Override
             public void onSelectionChange(SelectionChangeEvent event) {
                 ObjectPropertyTO selectedPropertyTO = ((SingleSelectionModel<ObjectPropertyTO>)dataObjectPropertiesTable.getSelectionModel()).getSelectedObject();
                 Command selectCommand = modelEditorPresenter.createSelectCommand(selectedPropertyTO);
                 selectCommand.execute();
             }
         });
 
         dataObjectPropertiesTable.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.BOUND_TO_SELECTION);
 
 
         //pager.setDisplay(dataObjectPropertiesTable);
         //pager.setPageSize(10);
 
         dataObjectPropertiesProvider.addDataDisplay(dataObjectPropertiesTable);
         dataObjectPropertiesProvider.refresh();
 
         newPropertyIsMultiple.setVisible(false);
         newPropertyIsMultiple.setValue(false);
         newPropertyBasicType.setValue(true);
         newPropertyButton.setIcon(IconType.PLUS_SIGN);
 
     }
 
     private void populateBaseTypes() {
         newPropertyType.clear();
         for (PropertyTypeTO type : baseTypes) {
             newPropertyType.addItem(type.getName(), type.getClassName());
         }
     }
 
     private void populateObjectTypes() {
         List<DataObjectTO> dataObjects = dataModel.getDataObjects();
         newPropertyType.clear();
         for (DataObjectTO dataObject : dataObjects) {
             newPropertyType.addItem(dataObject.getName(), dataObject.getClassName());
         }        
     }
 
     @UiHandler("newPropertyButton")
     void newPropertyClick(ClickEvent event) {
 
         Command createPropertyCommand = modelEditorPresenter.createAddDataObjectPropertyCommand(dataObject, newPropertyName.getText(), newPropertyType.getValue(), newPropertyIsMultiple.getValue(), newPropertyBasicType.getValue());
         createPropertyCommand.execute();
     }
 
     @UiHandler("newPropertyDataObjectType")
     void dataObjectTypeSelected(ClickEvent event) {
         newPropertyIsMultiple.setVisible(true);
         populateObjectTypes();
     }
 
     @UiHandler("newPropertyBasicType")
     void basicTypeSelected(ClickEvent event) {
         newPropertyIsMultiple.setVisible(false);
         newPropertyIsMultiple.setValue(false);
         populateBaseTypes();
     }
 
     public void notifyDataModelChanged() {
        if (newPropertyDataObjectType.getValue()) populateObjectTypes();
     }
 
     public void setDataModel(DataModelTO dataModel) {
         this.dataModel = dataModel;
     }
 
     public void setDataObject(DataObjectTO dataObject, boolean cleanBreadcrumbs) {
         this.dataObject = dataObject;
         objectName.setText(dataObject.getName());
 
         //We create a new selection model due to a bug found in GWT when we change e.g. from one data object with 9 rows
         // to one with 3 rows and the table was sorted.
         //Several tests has been done and the final workaround (not too bad) we found is to
         // 1) sort the table again
         // 2) create a new selection model
         // 3) populate the table with new items
         // 3) select the first row
         dataObjectPropertiesTable.getColumnSortList().push(dataObjectPropertiesTable.getColumn(0));
         SingleSelectionModel selectionModel2 = new SingleSelectionModel<ObjectPropertyTO>();
         dataObjectPropertiesTable.setSelectionModel(selectionModel2);
 
         selectionModel2.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 
             @Override
             public void onSelectionChange(SelectionChangeEvent event) {
                 ObjectPropertyTO selectedPropertyTO = ((SingleSelectionModel<ObjectPropertyTO>)dataObjectPropertiesTable.getSelectionModel()).getSelectedObject();
                 Command selectCommand = modelEditorPresenter.createSelectCommand(selectedPropertyTO);
                 selectCommand.execute();
             }
         });
 
         ArrayList<ObjectPropertyTO> sortBuffer = new ArrayList<ObjectPropertyTO>();
         sortBuffer.addAll(dataObject.getProperties());
         Collections.sort(sortBuffer, new ObjectPropertyComparator("name"));
 
         dataObjectProperties = dataObject.getProperties();
         dataObjectPropertiesProvider.getList().clear();
         dataObjectPropertiesProvider.getList().addAll(sortBuffer);
         dataObjectPropertiesProvider.flush();
         dataObjectPropertiesProvider.refresh();
 
         dataObjectPropertiesTable.getColumnSortList().push(dataObjectPropertiesTable.getColumn(0));
 
         if (dataObjectProperties.size() > 0) {
             dataObjectPropertiesTable.setKeyboardSelectedRow(0);
             selectionModel2.setSelected(sortBuffer.get(0), true);
         }
 
         //set the first row selected again. Sounds crazy, but's part of the workaround, don't remove this line.
         if (dataObjectProperties.size() > 0) {
             dataObjectPropertiesTable.setKeyboardSelectedRow(0);
         }
 
         ColumnSortEvent.fire(dataObjectPropertiesTable, dataObjectPropertiesTable.getColumnSortList());
 
         addBreadcrumb(dataObject, cleanBreadcrumbs);
     }
 
     public void addDataObjectProperty(ObjectPropertyTO objectProperty) {
         dataObjectPropertiesProvider.getList().add(objectProperty);
         dataObjectPropertiesProvider.flush();
         dataObjectPropertiesProvider.refresh();
         dataObjectPropertiesTable.setKeyboardSelectedRow(dataObjectPropertiesProvider.getList().size() - 1);
     }
 
     public void deleteDataObjectProperty(ObjectPropertyTO objectProperty, int index) {
         dataObjectPropertiesProvider.getList().remove(index);
         dataObjectPropertiesProvider.flush();
         dataObjectPropertiesProvider.refresh();
     }
 
     public void setModelEditorPresenter(DataModelEditorPresenter modelEditorPresenter) {
         this.modelEditorPresenter = modelEditorPresenter;
     }
 
     public DataModelEditorPresenter getModelEditorPresenter() {
         return modelEditorPresenter;
     }
 
     public void setBaseTypes(List<PropertyTypeTO> baseTypes) {
         this.baseTypes = baseTypes;
         populateBaseTypes();
     }
 
     public void addBreadcrumb(DataObjectTO dataObject, boolean clear) {
         if (clear) dataObjectNavigation.clear();
         ((DataObjectBreadcrums)dataObjectNavigation).add(dataObject, modelEditorPresenter.createSelectCommand(dataObject, false));
     }
 
     public void refresh() {
         dataObjectPropertiesProvider.refresh();
     }
 
     private String propertyTypeDisplay(ObjectPropertyTO propertyTO) {
         String className = propertyTO.getClassName();
         if (propertyTO.isMultiple()) {
             className += "[1..N]";
         }
         return className;
     }
 }
