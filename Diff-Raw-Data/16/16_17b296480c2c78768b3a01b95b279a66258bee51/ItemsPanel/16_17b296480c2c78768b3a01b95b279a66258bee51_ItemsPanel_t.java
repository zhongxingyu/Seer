 /*
  * Copyright 2012-2013 Danylo Vashchilenko
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
 package org.key2gym.client.panels;
 
 import java.awt.BorderLayout;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.SwingUtilities;
 
 import org.apache.log4j.Logger;
 import org.key2gym.business.api.SecurityViolationException;
 import org.key2gym.business.api.dtos.ItemDTO;
 import org.key2gym.business.api.services.ItemsService;
 import org.key2gym.client.ContextManager;
 import org.key2gym.client.DataRefreshPulse;
 import org.key2gym.client.util.ItemsTableModel;
 
 /**
  * 
  * @author Danylo Vashchilenko
  */
 public class ItemsPanel extends javax.swing.JPanel {
 
     /**
      * Creates new ItemsPanel
      */
     public ItemsPanel() {
         itemsService = ContextManager.lookup(ItemsService.class);
 
         initComponents();
         buildPanel();
 
         DataRefreshPulse.getInstance().addObserver(new DataRefreshObserver());
     }
 
     /**
      * Initializes the panel's components.
      */
     private void initComponents() {
 
         itemsTable = new javax.swing.JTable();
 
         /*
          * Columns of the items table
          */
         ItemsTableModel.Column[] itemsTableColumns = new ItemsTableModel.Column[] {
                 ItemsTableModel.Column.TITLE, ItemsTableModel.Column.QUANTITY,
                 ItemsTableModel.Column.PRICE, ItemsTableModel.Column.BARCODE };
 
         itemsTableModel = new ItemsTableModel(itemsTableColumns);
         itemsTable.setModel(itemsTableModel);
         itemsTable
                 .setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
 
         itemsScrollPane = new javax.swing.JScrollPane();
         itemsScrollPane.setViewportView(itemsTable);
     }
 
     /**
      * Builds the panel by placing the components on it.
      */
     private void buildPanel() {
         setLayout(new BorderLayout());
         add(itemsScrollPane, BorderLayout.CENTER);
     }
 
     /**
      * Sets the items group to display.
      * 
      * @param items
      *            the new items group
      */
     public void setItems(ItemsGroup items) throws SecurityViolationException {
         this.itemsGroup = items;
 
         refresh();
     }
 
     private void refresh() throws SecurityViolationException {
         refreshData();
         refreshGUI();
     }
 
     /**
      * Reloads the data.
      * 
      * @throws SecurityViolationException
      *             if the access to the current date was denied
      */
     private void refreshData() throws SecurityViolationException {
         if (itemsGroup.equals(ItemsGroup.ALL)) {
             items = itemsService.getAllItems();
         } else if (itemsGroup.equals(ItemsGroup.AVAILABLE)) {
             items = itemsService.getItemsAvailable();
         } else if (itemsGroup.equals(ItemsGroup.PURE)) {
             items = itemsService.getPureItems();
         } else {
             throw new RuntimeException(
                     "This ItemsGroup has not been yet implemented.");
         }
     }
 
     private void refreshGUI() {
         itemsTableModel.setItems(items);
     }
 
     /**
      * Used to refresh the items at the data refresh rate.
      * 
      * TODO: do proper SecurityViolationException handling (e.g. closing itself)
      */
     private class DataRefreshObserver implements Observer {
 
         @Override
         public void update(Observable observable, Object arg) {
 
             if (itemsGroup == null || !ContextManager.getInstance().isContextAvailable()) {
                 return;
             }
 
             /*
              * Loads the data synchronously on the timer's thread.
              */
             try {
                 refreshData();
             } catch (SecurityViolationException ex) {
                Logger.getLogger(ItemsPanel.class).error(
                         "Failed to refresh the items!", ex);
             }
 
             /*
              * Updates the GUI asynchronously on the Swing thread.
              */
             SwingUtilities.invokeLater(new Runnable() {
                 @Override
                 public void run() {
                     refreshGUI();
                 }
             });
         }
     }
 
     /*
      * Business
      */
     private ItemsService itemsService;
     /*
      * Presentation
      */
     private ItemsGroup itemsGroup;
     private ItemsTableModel itemsTableModel;
     private JScrollPane itemsScrollPane;
     private JTable itemsTable;
     /*
      * Data
      */
     private List<ItemDTO> items;
 
     public enum ItemsGroup {
 
         ALL, AVAILABLE, PURE
     };
 }
