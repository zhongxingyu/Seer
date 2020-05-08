 /*
 Copyright 2011 Matt DeVore
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
     http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
 
 package com.github.matvore.andradion2.design;
 
 import javax.swing.JList;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
public class PlaceableItemListSelectionListener implements ListSelectionListener {
   private final LevelEditPanel levelEditPanel;
 
   public PlaceableItemListSelectionListener(LevelEditPanel levelEditPanel) {
     this.levelEditPanel = levelEditPanel;
   }
 
   @Override
   public void valueChanged(ListSelectionEvent event) {
     JList list = (JList)event.getSource();
     PlaceableItem item = PlaceableItemListModel.getInstance().getElementAt(
         list.getSelectedIndex());
     levelEditPanel.setPlaceableItem(item);
   }
 }
