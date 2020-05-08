 /*
  * Copyright 2012 Danylo Vashchilenko
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
 package org.key2gym.client.dialogs;
 
 import com.jgoodies.forms.factories.CC;
 import com.jgoodies.forms.layout.FormLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.List;
 import javax.swing.*;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import org.key2gym.business.api.BusinessException;
 import org.key2gym.business.api.SecurityViolationException;
 import org.key2gym.business.api.UserException;
 import org.key2gym.business.api.ValidationException;
 import org.key2gym.business.api.dtos.ItemDTO;
 import org.key2gym.business.api.remote.ItemsServiceRemote;
 import org.key2gym.client.ContextManager;
 import org.key2gym.client.UserExceptionHandler;
 import org.key2gym.client.factories.FormPanelDialogsFactory;
 import org.key2gym.client.util.ItemsTableModel;
 import org.key2gym.client.util.ItemsTableModel.Column;
 
 /**
  *
  * @author Danylo Vashchilenko
  */
 public class ManageItemsDialog extends AbstractDialog {
 
     /**
      * Creates new form ItemsDialog
      */
     public ManageItemsDialog(JFrame parent) throws SecurityViolationException {
         super(parent, true);
 
         initComponents();
         buildDialog();
 
 	refreshTable();
     }
 
     private void initComponents() {
 
         /*
          * Items table
          */
         itemsScrollPane = new JScrollPane();
         itemsTable = new JTable();
         Column[] columns =
                 new Column[]{Column.TITLE,
             Column.QUANTITY,
             Column.PRICE,
             Column.BARCODE};
         itemsTableModel = new ItemsTableModel(columns);
         itemsTable.setModel(itemsTableModel);
         itemsScrollPane.setViewportView(itemsTable);
 
         /*
          * Add button
          */
         addButton = new JButton();
         addButton.setIcon(new ImageIcon(getClass().getResource("/org/key2gym/client/resources/plus32.png"))); // NOI18N
         addButton.setText(getString("Button.Add")); // NOI18N
         addButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 addOrEditButtonActionPerformed(evt);
             }
         });
 
         /*
          * Edit button
          */
         editButton = new JButton();
         editButton.setIcon(new ImageIcon(getClass().getResource("/org/key2gym/client/resources/edit32.png"))); // NOI18N
         editButton.setText(getString("Button.Edit")); // NOI18N
         editButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 addOrEditButtonActionPerformed(evt);
             }
         });
 
         /*
          * Remove button
          */
         removeButton = new JButton();
         removeButton.setIcon(new ImageIcon(getClass().getResource("/org/key2gym/client/resources/remove32.png"))); // NOI18N
         removeButton.setText(getString("Button.Remove")); // NOI18N
         removeButton.addActionListener(new java.awt.event.ActionListener() {
             @Override
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 removeButtonActionPerformed(evt);
             }
         });
 
         /*
          * Listens to the table to know when to enable the edit button
          */
         itemsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
             @Override
             public void valueChanged(ListSelectionEvent e) {
                 onItemsTableSelectionChanged();
             }
         });
         onItemsTableSelectionChanged();
 
         /*
          * Close button
          */
         closeButton = new JButton();
         closeButton.setAction(getCloseAction());
         getRootPane().setDefaultButton(closeButton);
     }
 
     private void buildDialog() {
         FormLayout layout = new FormLayout("4dlu, [400dlu, p]:g, 4dlu, p, 4dlu",
                 "4dlu, f:[200dlu, p]:g, 4dlu");
         setLayout(layout);
 
         add(itemsScrollPane, CC.xy(2, 2));
 
         JPanel buttonsPanel = new JPanel();
         {
             buttonsPanel.setLayout(new FormLayout("d", "b:d:g, c:d, t:d:g, d"));
             buttonsPanel.add(addButton, CC.xy(1, 1));
             buttonsPanel.add(editButton, CC.xy(1, 2));
             buttonsPanel.add(removeButton, CC.xy(1, 3));
             buttonsPanel.add(closeButton, CC.xy(1, 4));
         }
         add(buttonsPanel, CC.xy(4, 2));
 
         setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
         setTitle(getString("Title.ManageItems")); // NOI18N
         pack();
         setMinimumSize(getPreferredSize());
         setResizable(true);
         setLocationRelativeTo(getParent());
     }
 
     private void addOrEditButtonActionPerformed(ActionEvent evt) {
 
         ItemDTO item;
 
         if (evt.getSource().equals(addButton)) {
             item = new ItemDTO();
         } else {
             item = items.get(itemsTable.getSelectedRow());
         }
 
         FormDialog dialog = FormPanelDialogsFactory.createItemEditor(this, item);
 
         dialog.setVisible(true);
 
         if (dialog.getResult().equals(FormDialog.Result.OK)) {
 	    refreshTable();
         }
 	
     }
 
     private void removeButtonActionPerformed(ActionEvent evt) {
         if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(this, getString("Message.AreYouSureYouWantToRemoveItems"), getString("Title.Confirmation"), JOptionPane.YES_NO_OPTION)) {
             return;
         }
 
         ItemsServiceRemote itemsService = ContextManager.lookup(ItemsServiceRemote.class);
 
         for (int index : itemsTable.getSelectedRows()) {
             try {
                 itemsService.removeItem(items.get(index).getId());
             } catch (ValidationException | BusinessException | SecurityViolationException ex) {
                 UserExceptionHandler.getInstance().processException(ex);
             }
         }
 
 	refreshTable();
     }
 
     private void onItemsTableSelectionChanged() {
         if (itemsTable.getSelectedRowCount() == 0) {
             removeButton.setEnabled(false);
             editButton.setEnabled(false);
         } else if (itemsTable.getSelectedRowCount() == 1) {
             editButton.setEnabled(true);
             removeButton.setEnabled(true);
         } else {
             removeButton.setEnabled(true);
             editButton.setEnabled(false);
         }
     }
 
     private void refreshTable() {
 	try { 
 	    items = ContextManager.lookup(ItemsServiceRemote.class).getPureItems(); 
 	} catch (SecurityViolationException ex) {
 	    UserExceptionHandler.getInstance().processException(ex);
 	    getCloseAction().actionPerformed(null);
	    return;
 	}
         itemsTableModel.setItems(items);
     }
 
     /*
      * Business
      */
     private List<ItemDTO> items;
 
     /*
      * Presentation
      */
     private ItemsTableModel itemsTableModel;
     private JButton addButton;
     private JButton editButton;
     private JScrollPane itemsScrollPane;
     private JTable itemsTable;
     private JButton closeButton;
     private JButton removeButton;
 }
