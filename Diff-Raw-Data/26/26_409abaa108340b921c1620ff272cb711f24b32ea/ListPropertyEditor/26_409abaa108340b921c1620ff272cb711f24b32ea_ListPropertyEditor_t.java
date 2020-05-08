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
 import com.objectgraph.core.Event;
 import com.objectgraph.core.ListNode;
 import com.objectgraph.core.RootedProperty;
 import com.objectgraph.core.eventtypes.changes.Change;
import com.objectgraph.core.eventtypes.changes.ListChange;
 import com.objectgraph.gui.EditorManager;
 import com.objectgraph.gui.PropertyEditor;
 import com.objectgraph.utils.PathUtils;
import javafx.collections.ObservableList;
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.fxml.FXML;
 import javafx.scene.control.*;
 import javafx.util.Callback;
 
 import java.net.URL;
 import java.util.List;
 import java.util.ResourceBundle;
 import java.util.Set;
 
 public class ListPropertyEditor extends PropertyEditor {
 
     @FXML
     private ListView listView;
 
     public ListPropertyEditor() {
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
                     @Override
                     public void startEdit() {
                         super.startEdit();
                         if (getModel() != null && getGraphic() == null) {
                             ListNode list = getModel().getValue();
                             RootedProperty itemModel = list.getRootedProperty(String.valueOf(getIndex()));
                             PropertyEditor editor = EditorManager.getBestEditor(itemModel, true, true);
                             setGraphic(editor);
                         }
                     }
 
                     @Override
                     public void cancelEdit() {
                         super.cancelEdit();
                         if (getGraphic() != null) {
                             PropertyEditor editor = (PropertyEditor) getGraphic();
                            getListView().getItems().set(getIndex(), editor.getModel().getValue());
                             setText(editor.getModel().getValue().toString());
                             editor.detach();
                             setGraphic(null);
                         }
                     }
 
                     @Override
                     protected void updateItem(Object item, boolean empty) {
                         super.updateItem(item, empty);
                         if (item != null)
                             setText(item.toString());
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
 
         menu.getItems().addAll(addElementItem, deleteElementItem);
 
         listView.setContextMenu(menu);
     }
 
     private void deleteElement() {
         if (getModel() != null) {
             ListNode list = getModel().getValue();
            ObservableList selectedItems = listView.getSelectionModel().getSelectedItems();
            list.removeAll(selectedItems);
         }
     }
 
     private void addElement() {
         if (getModel() != null) {
             ListNode list = getModel().getValue();
             Class<?> elementType = list.getElementType();
             try {
                 list.add(elementType.newInstance());
                 listView.getSelectionModel().selectLast();
             } catch (InstantiationException | IllegalAccessException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
         }
     }
 
     @Override
     public void updateModel() {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean requiresViewUpdate(Event event) {
        if (event.getType() instanceof ListChange) {
             // TODO return true only if the number of elements has changed or the name of the elements has changed
             return PathUtils.isPrefix(getModel().getPath(), event.getPath());
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
     public boolean canEdit(RootedProperty model) {
         return model.getValue() instanceof ListNode;
     }
 
     @Override
     public Set<Class<?>> getBaseEditableTypes() {
         return Sets.<Class<?>>newHashSet(List.class, ListNode.class);
     }
 }
