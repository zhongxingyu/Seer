 /*
  * Copyright 2013 Emanuele Tamponi
  *
  * This file is part of object-graph.
  *
  * object-graph is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * object-graph is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with object-graph.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.objectgraph.gui.editors;
 
 import com.google.common.collect.Sets;
 import com.objectgraph.core.*;
 import com.objectgraph.gui.EditorManager;
 import com.objectgraph.gui.PropertyEditor;
 import com.objectgraph.utils.ClassUtils;
 import com.objectgraph.utils.PathUtils;
 import javafx.application.Platform;
import javafx.collections.ObservableList;
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.fxml.FXML;
 import javafx.scene.control.*;
 import javafx.util.Callback;
 import org.pcollections.PSet;
 
 import java.net.URL;
 import java.util.List;
 import java.util.ResourceBundle;
 import java.util.Set;
 
 public class ListNodePropertyEditor extends PropertyEditor {
 
     private static class ListItemViewer extends Label implements EventRecipient {
 
         private RootedProperty itemModel;
 
         public void setItemModel(RootedProperty model) {
             this.itemModel = model;
             itemModel.getRoot().addParentPath(this, "");
             handleEvent(null, null);
         }
 
         @Override
         public void handleEvent(Event e, PSet<EventRecipient> visited) {
             Platform.runLater(new Runnable() {
                 @Override
                 public void run() {
                     try {
                         if (itemModel.getValue() != null)
                             setText(itemModel.getValue().toString());
                         else
                             setText("<null>");
                     } catch (PropertyNotExistsException ex) {
                         setText("");
                     }
                 }
             });
         }
     }
 
     @FXML
     private ListView listView;
 
     public ListNodePropertyEditor() {
         super("ListEditorView.fxml");
     }
 
     @Override
     public void initialize(URL url, ResourceBundle resourceBundle) {
         listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
 
         initializeContextMenu();
 
         initializeListCell();
     }
 
     private void initializeListCell() {
         listView.setCellFactory(new Callback<ListView, ListCell>() {
             @Override
             public ListCell call(ListView listView) {
                 ListCell cell = new ListCell() {
                     {
                         setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                     }
                    ListItemViewer viewer = new ListItemViewer();
                     @Override
                     public void startEdit() {
                         super.startEdit();
                         if (getModel() != null && getGraphic() == viewer) {
                             ListNode list = getModel().getValue();
                             RootedProperty itemModel = list.getRootedProperty(String.valueOf(getIndex()));
                             PropertyEditor editor = EditorManager.getBestEditor(itemModel, false, true);
                            setManaged(false);
                             setGraphic(editor);
                         }
                     }
 
                     @Override
                     public void cancelEdit() {
                         super.cancelEdit();
                         if (getGraphic() != viewer) {
                             PropertyEditor editor = (PropertyEditor) getGraphic();
                             editor.detach();
                            setManaged(true);
                             setGraphic(viewer);
                         }
                     }
 
                     @Override
                     protected void updateItem(Object item, boolean empty) {
                         super.updateItem(item, empty);
                         if (!empty) {
                             ListNode list = getModel().getValue();
                             RootedProperty itemModel = list.getRootedProperty(String.valueOf(getIndex()));
                             viewer.setItemModel(itemModel);
                             setGraphic(viewer);
                         }
                     }
                 };
                 return cell;
             }
         });
     }
 
     private void initializeContextMenu() {
         ContextMenu menu = new ContextMenu();
 
         MenuItem addElementItem = new MenuItem("Add element");
         addElementItem.setOnAction(new EventHandler<ActionEvent>() {
             @Override
             public void handle(ActionEvent actionEvent) {
                 addElement();
             }
         });
         MenuItem deleteElementItem = new MenuItem("Delete selected");
         deleteElementItem.setOnAction(new EventHandler<ActionEvent>() {
             @Override
             public void handle(ActionEvent actionEvent) {
                 deleteElement();
             }
         });
         MenuItem editElementItem = new MenuItem("Edit in new window");
         editElementItem.setOnAction(new EventHandler<ActionEvent>() {
             @Override
             public void handle(ActionEvent actionEvent) {
                 editInNewWindow();
             }
         });
 
         menu.getItems().addAll(addElementItem, deleteElementItem, editElementItem);
 
         listView.setContextMenu(menu);
     }
 
     private void editInNewWindow() {
         int index = listView.getFocusModel().getFocusedIndex();
         if (getModel() != null && index >= 0) {
             ListNode list = getModel().getValue();
             RootedProperty itemModel = list.getRootedProperty(String.valueOf(index));
             EditorManager.openBestEditorStage(itemModel, false, false);
         }
     }
 
     private void deleteElement() {
         if (getModel() != null) {
             ListNode list = getModel().getValue();
            ObservableList<Integer> selectedIndices = listView.getSelectionModel().getSelectedIndices();
             list.removeIndices(selectedIndices);
         }
     }
 
     private void addElement() {
         if (getModel() != null) {
             ListNode list = getModel().getValue();
             Class<?> elementType = list.getElementType();
             try {
                 if (ClassUtils.isConcrete(elementType)) {
                     list.add(elementType.newInstance());
                    //listView.edit(list.size()-1);
                 } else {
                     list.add(null);
                    //listView.edit(list.size()-1);
                 }
             } catch (InstantiationException | IllegalAccessException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
         }
     }
 
     @Override
     public boolean requiresViewUpdate(Event event) {
         if (event.getType() instanceof ListChange) {
             return PathUtils.samePath(getModel().getPath(), event.getPath());
         } else {
             return false;
         }
     }
 
     @Override
     public void updateView() {
         listView.getItems().clear();
         if (getModel() != null) {
            List content = getModel().getValue();
            listView.getItems().addAll(content);
         }
     }
 
     @Override
     public boolean canEdit(Class<?> valueType) {
         return valueType != null && ListNode.class.isAssignableFrom(valueType);
     }
 
     @Override
     public Set<Class<?>> getBaseEditableTypes() {
         return Sets.<Class<?>>newHashSet(ListNode.class);
     }
 }
