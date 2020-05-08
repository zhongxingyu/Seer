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
 
 import javax.faces.model.DataModel;
 import java.io.Serializable;
 import java.util.*;
 
 
 public class TreeDataModel extends DataModel implements Serializable  {
     private Stack<List<Map.Entry<Object, List>>> mapStack = new Stack<List<Map.Entry<Object, List>>>();
     private List<Integer> rootIndex = null;
     private List<Map.Entry<Object, List>> rootMap;
     private List<Map.Entry<Object, List>> currentMap;
     private Integer rowIndex = -1;
 
 
 
     public TreeDataModel() {
         rowIndex = -1;
         setWrappedData(null);
     }
     public TreeDataModel(List<Map.Entry<Object, List>> inputModel) {
         rowIndex = -1;
         rootMap = inputModel;
         currentMap = rootMap;               
     }
 
 
 
     @Override
     public Object getWrappedData() {
         return rootMap;
     }
     @Override
     public int getRowCount() {
         return currentMap.size();
 
     }
     public Map.Entry<Object, List> getRowEntry() {
         return currentMap.get(rowIndex);
     }
     @Override
     public Object getRowData() {
         if (currentMap == null) {
             return (null);
         } else if (!isRowAvailable()) {
             throw new IllegalArgumentException();
         } else {
             return currentMap.get(rowIndex).getKey();
         }
     }
     public Object getRootData() {
         return mapStack.peek().get(rootIndex.get(rootIndex.size() - 1)).getKey();
     }
     @Override
     public int getRowIndex() {
         return rowIndex;
     }
     @Override
     public boolean isRowAvailable() {
        return (rowIndex >= 0 && rowIndex.compareTo(currentMap.size()) < 0);
    }
     @Override
     public void setWrappedData(Object wrappedData) {
         if (wrappedData instanceof List) {
             Object testObj  = ((List) wrappedData).get(0);
             if (testObj instanceof Map.Entry) {
                 rootMap = (List<Map.Entry<Object, List>>) wrappedData;
                 // Expectation is this method will not be run during a subtree rendering event and setting the currentMap
                 // to the root is the desired behavior.
                 currentMap = rootMap;
             }
         }
     }
     @Override
     public void setRowIndex(int rowIndex) {
         this.rowIndex = rowIndex;
     }
 
 
 
 
     public void setRootIndex(String expandedRowId) {
         if (expandedRowId == null) {
             // Wipe internal root state
             currentMap = rootMap;
             rootIndex = null;
             while (!mapStack.empty()) mapStack.pop();
             return;
         }
 
         if (expandedRowId.contains(".")) {
             String[] split = expandedRowId.split("\\.");
 
             rootIndex = new ArrayList<Integer>();
             for (int i = 0; i<split.length; i++) rootIndex.add(Integer.parseInt(split[i]));
         } else {
             rootIndex = new ArrayList<Integer>();
             rootIndex.add(Integer.parseInt(expandedRowId));
         }
 
         Iterator<Integer> indexIterator = rootIndex.iterator();
         Integer target = null;
         currentMap = rootMap;
         while(indexIterator.hasNext()) {
             target = indexIterator.next();
             mapStack.push(currentMap);
             currentMap = currentMap.get(target).getValue();
         }
     }
     
     public boolean isRootIndexSet() {
         if (rootIndex == null || rootIndex.size() == 0) return false;
         else return true;
     }
 
     public String getRootIndex() {
         if (rootIndex == null) return "";
         return join(rootIndex,".");
     }
 
     public int pop() {
         currentMap = mapStack.pop();
         return rootIndex.remove(rootIndex.size()-1);
     }
 
     private String join(Collection<Integer> strCollection, String delimiter) {
         String joined = "";
         int noOfItems = 0;
         for (Integer item : strCollection) {
             joined += item;
             if (++noOfItems < strCollection.size())
                 joined += delimiter;
         }
         return joined;
     }
 
     public int getCurrentRowChildCount() {
         List value = currentMap.get(rowIndex).getValue();
         if (value == null) return 0;
         return value.size();
     }
 }
