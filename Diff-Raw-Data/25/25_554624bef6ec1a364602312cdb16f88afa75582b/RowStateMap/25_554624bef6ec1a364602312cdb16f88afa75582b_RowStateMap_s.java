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
 
 package org.icefaces.ace.model.table;
 
 import org.icefaces.ace.util.CollectionUtils;
 import org.icefaces.ace.util.collections.EntrySetToKeyListTransformer;
 import org.icefaces.ace.util.collections.Predicate;
 
 import javax.faces.model.DataModel;
 import java.io.Serializable;
 import java.util.*;
 
 public class RowStateMap implements Map<Object, RowState>, Serializable {
     private Map<Object, RowState> map = new HashMap<Object, RowState>();
 
     private static Predicate selectedPredicate = new SelectedPredicate();
     private static Predicate selectablePredicate = new SelectablePredicate();
     private static Predicate editablePredicate = new EditablePredicate();
     private static Predicate expandablePredicate = new ExpandablePredicate();
     private static Predicate expandedPredicate = new ExpandedPredicate();
     private static Predicate visiblePredicate = new VisiblePredicate();
 
 
     public RowState put(Object o, RowState s) {
         return map.put(o, s);
     }
 
     public void add(Object o) {
         map.put(o, new RowState());
     }
 
     public int size() {
         return map.size();
     }
 
     public boolean isEmpty() {
         return map.isEmpty();
     }
 
     public boolean containsKey(Object key) {
         return map.containsKey(key);
     }
 
     public boolean containsValue(Object value) {
         return map.containsValue(value);
     }
 
     public RowState get(Object o) {
         RowState s = map.get(o);
         if (s == null) {
             add(o);
             s = map.get(o);
         }
         return s;
     }
 
     public RowState remove(Object o) {
         return map.remove(o);
     }
 
     public void putAll(Map m) {
         map.putAll(m);
     }
 
     public void clear() {
         map.clear();
     }
 
     public Set keySet() {
         return map.keySet();
     }
 
     public Collection values() {
         return map.values();
     }
 
     public Set entrySet() {
         return map.entrySet();
     }
 
     public void updateData(Object o) {
         map.put(o, map.remove(o));
     }
 
     /**
      * This method will look up state entries with keys in the input list, and update their
      * key with the copy from the input list.
      *
      * This method is useful in applications where row data is updated by replacing a row
      * with an object copy that is equal() but contains differences that must be reflected in methods
      * that return row data from the rowStateMap. Without updating, in this circumstance, the key is still a
      * reference to the old copy of the data, and though the state will be correctly correlated to the new key,
      * methods returning the key, will return the old key.
      *
      * @param data - A list of row data equal() to some key already in the rowStateMap, but with changes that require the current key to be replaced.
      */
     public void updateData(List data) {
         List<RowState> values = new ArrayList<RowState>(data.size());
         for (Object d : data) {
             values.add(map.remove(d));
         }
         int i = 0;
         for (RowState v : values) {
             if (v != null) map.put(data.get(i), v);
             //else {
                 // System.out.println("Updating a null state!");
             //}
             i++;
         }
     }
 
     /**
      * This method will remove all entries from the map with keys in this list.
      * @param data the row data to remove states for
      */
     public void removeStates(List data) {
         Set keys = map.keySet();
         keys.removeAll(data);
     }
 
     /**
      * This method will remove all entries from the map but those with keys in this list.
      * @param data the row data list to keep states for
      */
     public void filterStates(List data) {
         Set keys = map.keySet();
         keys.retainAll(data);
     }
 
     /**
      * This method will remove all entries from the map but those with keys in this data model.
     * @param data the data model to keep states for
      */
     public void filterStates(DataModel model) {
         Set keys = map.keySet();
         ArrayList rowDataList = new ArrayList<Object>();
         int rowsToProcess = model.getRowCount();
 
         // TODO: lazy handling?
         while (rowsToProcess > 0) {
             rowsToProcess--;
             model.setRowIndex(rowsToProcess);
             if (model.isRowAvailable()) rowDataList.add(model.getRowData());
         }
 
         keys.retainAll(rowDataList);
     }
 
     public void setExpandableByTreeModel(TreeDataModel dataModel) {
         String currentRootId = "";
         int rowCount = dataModel.getRowCount();
         int i = 0;
         // Handle row and loop down the tree if expanded.
         while (i < rowCount) {
             dataModel.setRowIndex(i++);
             try {
                 do {
                     // Decodes row/node in tree case.
                     // Handle recursive case
                     RowState currentModel = get(dataModel.getRowData());
                     
                     if (dataModel.getCurrentRowChildCount() == 0) {
                         currentModel.setExpandable(false);
                     }
 
                     if (dataModel.getCurrentRowChildCount() > 0) {
                         currentModel.setExpandable(true);
                         currentRootId =  currentRootId.equals("") ? (dataModel.getRowIndex()+"") : (currentRootId + "." + dataModel.getRowIndex());
                         dataModel.setRootIndex(currentRootId);
                         dataModel.setRowIndex(0);
                     } else if (dataModel.getRowIndex() < dataModel.getRowCount()-1) {
                         dataModel.setRowIndex(dataModel.getRowIndex() + 1);
                     } else if (!currentRootId.equals("")) {
                         // changing currrent node id to reflect pop
                         int lastSiblingRowIndex = dataModel.pop();
                         // if we are the last child of a set of siblings we've popped back to,
                         // continue popping until an uncounted sibling exists
                         while (lastSiblingRowIndex == (dataModel.getRowCount() - 1) && dataModel.isRootIndexSet()) {
                             lastSiblingRowIndex = dataModel.pop();
                             currentRootId = (currentRootId.lastIndexOf('.') != -1)  ?
                                 currentRootId.substring(0,currentRootId.lastIndexOf('.')) :
                                 "";
                         }
                         dataModel.setRowIndex(lastSiblingRowIndex + 1);
                         currentRootId = (currentRootId.lastIndexOf('.') != -1)  ?
                                 currentRootId.substring(0,currentRootId.lastIndexOf('.')) :
                                 "";
                     }
                     // Break out of expansion recursion to continue root node
                     if (currentRootId.equals("")) break;
                 } while (true);
             } finally {
                 dataModel.setRowIndex(-1);
                 dataModel.setRootIndex(null);
             }
         }
     }
 
     public List getSelected() {
         return EntrySetToKeyListTransformer.transform(CollectionUtils.select(map.entrySet(), selectedPredicate));
     }
     public List getSelectable() {
         return EntrySetToKeyListTransformer.transform(CollectionUtils.select(map.entrySet(), selectablePredicate));
     }
     public List getEditable() {
         return EntrySetToKeyListTransformer.transform(CollectionUtils.select(map.entrySet(), editablePredicate));
     }
     public List getExpanded() {
         return EntrySetToKeyListTransformer.transform(CollectionUtils.select(map.entrySet(), expandedPredicate));
     }
     public List getExpandable() {
         return EntrySetToKeyListTransformer.transform(CollectionUtils.select(map.entrySet(), expandablePredicate));
     }
     public List getVisible() {
         return EntrySetToKeyListTransformer.transform(CollectionUtils.select(map.entrySet(), visiblePredicate));
     }
     public List getRowExpanders() { return null; }
     public List getPanelExpanders() { return null; }
     public List getType(String type) { return null; }
 
 
     
     public void setAllSelected(boolean value) {
         for (RowState s : map.values()) s.setSelected(value);
     }
     public void setAllSelectable(boolean value) {
         for (RowState s : map.values()) s.setSelectable(value);
     }
     public void setAllEditable(boolean value) {
         for (RowState s : map.values()) s.setEditable(value);
     }
     public void setAllExpanded(boolean value) {
         for (RowState s : map.values()) s.setExpanded(value);
     }
     public void setAllExpandable(boolean value) {
         for (RowState s : map.values()) s.setExpandable(value);
     }
     public void setAllVisible(boolean value) {
         for (RowState s : map.values()) s.setVisible(value);
     }
     public void setAllRowExpansion() {
         for (RowState s : map.values()) {
             s.setExpansionType(RowState.ExpansionType.ROW);
         }
     }
     public void setAllPanelExpansion() {
     for (RowState s : map.values()) {
             s.setExpansionType(RowState.ExpansionType.PANEL);
         }
     }
 
 
     
     static class SelectedPredicate implements Predicate {
         public boolean evaluate(Object o) {
             if (o instanceof Entry)
                 if (((RowState)((Entry)o).getValue()).isSelected()) return true;
             return false;
         }
     }
     static class SelectablePredicate implements Predicate {
         public boolean evaluate(Object o) {
             if (o instanceof Entry)
                 if (((RowState)((Entry)o).getValue()).isSelectable()) return true;
             return false;
         }
     }
     static class ExpandedPredicate implements Predicate {
         public boolean evaluate(Object o) {
             if (o instanceof Entry)
                 if (((RowState)((Entry)o).getValue()).isExpanded()) return true;
             return false;
         }
     }
     static class ExpandablePredicate implements Predicate {
         public boolean evaluate(Object o) {
             if (o instanceof Entry)
                 if (((RowState)((Entry)o).getValue()).isExpandable()) return true;
             return false;
         }
     }
     static class EditablePredicate implements Predicate {
         public boolean evaluate(Object o) {
             if (o instanceof Entry)
                 if (((RowState)((Entry)o).getValue()).isEditable()) return true;
             return false;
         }
     }
     static class VisiblePredicate implements Predicate {
         public boolean evaluate(Object o) {
             if (o instanceof Entry)
                 if (((RowState)((Entry)o).getValue()).isVisible()) return true;
             return false;
         }
     }
 }
