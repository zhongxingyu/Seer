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
 package census.presentation.dialogs;
 
 import census.business.ClientsService;
 import census.business.api.ValidationException;
 import census.business.dto.ClientDTO;
 import census.presentation.MainFrame;
 import census.presentation.util.ClientsTableModel;
 import census.presentation.util.ClientsTableModel.Column;
 import com.jgoodies.forms.debug.FormDebugPanel;
 import com.jgoodies.forms.factories.CC;
 import com.jgoodies.forms.layout.FormLayout;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.text.MessageFormat;
 import java.util.LinkedList;
 import java.util.List;
 import javax.swing.*;
 import javax.swing.table.TableColumn;
 
 /**
  *
  * @author Danylo Vashchilenko
  */
 public class PickClientDialog extends CensusDialog {
 
     /**
      * Creates new form PickClientDialog
      */
     public PickClientDialog(JFrame parent) {
         super(parent, true);
         initComponents();
         buildDialog();
 
         idRadioButton.doClick();
     }
 
     private void initComponents() {
 
         modeButtonGroup = new ButtonGroup();
 
         searchCriteriasPanel = new JPanel();
         searchCriteriasPanel.setBorder(BorderFactory.createTitledBorder(getString("Text.SearchCriterias"))); // NOI18N
 
         /*
          * ID
          */
         idRadioButton = new JRadioButton();
         idRadioButton.setText(getString("Label.ID")); // NOI18N
         idRadioButton.setActionCommand("id"); // NOI18N
         idRadioButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent evt) {
                 radioButtonsActionPerformed(evt);
             }
         });
         modeButtonGroup.add(idRadioButton);
 
         idTextField = new JTextField();
         idTextField.setColumns(20);
 
         /*
          * Card
          */
         cardRadioButton = new JRadioButton();
         cardRadioButton.setText(getString("Label.Card")); // NOI18N
         cardRadioButton.setActionCommand("card"); // NOI18N
         cardRadioButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent evt) {
                 radioButtonsActionPerformed(evt);
             }
         });
         modeButtonGroup.add(cardRadioButton);
 
         cardTextField = new JTextField();
         cardTextField.setColumns(20);
 
         /*
          * Full name
          */
         fullNameRadioButton = new JRadioButton();
         fullNameRadioButton.setText(getString("Label.FullName")); // NOI18N
         fullNameRadioButton.setActionCommand("fullName"); // NOI18N
         fullNameRadioButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent evt) {
                 radioButtonsActionPerformed(evt);
             }
         });
         modeButtonGroup.add(fullNameRadioButton);
 
         fullNameTextField = new JTextField();
         fullNameTextField.setColumns(20);
 
         /*
          * Search button
          */
         searchButton = new JButton();
         searchButton.setText(getString("Button.Search")); // NOI18N
         searchButton.setIcon(new ImageIcon(getClass().getResource("/census/presentation/resources/search16.png")));
         searchButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent evt) {
                 searchButtonActionPerformed(evt);
             }
         });
 
         /*
          * Clients table
          */
         clientsTableScrollPane = new JScrollPane();
         clientsTable = new JTable();
 
         Column[] columns = new Column[]{
             Column.ID,
             Column.CARD,
             Column.FULL_NAME
         };
         clientsTableModel = new ClientsTableModel(columns);
         clientsTable.setModel(clientsTableModel);
         int[] widths = new int[]{45, 79, 205};
         TableColumn column;
         for (int i = 0; i < widths.length; i++) {
             column = clientsTable.getColumnModel().getColumn(i);
             column.setPreferredWidth(widths[i]);
         }
         clientsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         clientsTableScrollPane.setViewportView(clientsTable);
 
         okButton = new JButton(getOkAction());
         cancelButton = new JButton(getCancelAction());
 
         /*
          * Resize all buttons to have the same size.
          */
         Dimension commonSize = cancelButton.getPreferredSize();
 
         okButton.setPreferredSize(commonSize);
         cancelButton.setPreferredSize(commonSize);
         searchButton.setPreferredSize(commonSize);
 
         getRootPane().setDefaultButton(okButton);
 
         /*
          * Smart Input (see issue #22).
          */
         addHotKey(KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, 0), new IdFocusAction());
         addHotKey(KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, 0), new CardFocusAction());
         addHotKey(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SLASH, 0), new FullNameFocusAction());
         
     }
 
     private void buildDialog() {
         setLayout(new FormLayout("4dlu, f:p:g, 4dlu, p, 4dlu", "4dlu, f:p:g, f:p, 4dlu"));
 
         buildSearchCriteriasPanel();
 
         /*
          * Search control panel
          */
         JPanel searchControlPanel = new JPanel();
         {
             searchControlPanel.setLayout(new FormLayout("c:p:g", "p, t:p:g, p, p"));
 
             /*
              * Criterias panel
              */
             {
                 searchCriteriasPanel.setLayout(new FormLayout("f:p:g", "p, 3dlu, p, 5dlu, p, 3dlu, p, 5dlu, p, 3dlu, p, 5dlu"));
 
                 searchCriteriasPanel.add(idRadioButton, CC.xy(1, 1));
                 searchCriteriasPanel.add(idTextField, CC.xy(1, 3));
                 searchCriteriasPanel.add(cardRadioButton, CC.xy(1, 5));
                 searchCriteriasPanel.add(cardTextField, CC.xy(1, 7));
                 searchCriteriasPanel.add(fullNameRadioButton, CC.xy(1, 9));
                 searchCriteriasPanel.add(fullNameTextField, CC.xy(1, 11));
             }
 
             searchControlPanel.add(searchCriteriasPanel, CC.xy(1, 1));
             searchControlPanel.add(searchButton, CC.xy(1, 2));
         }
         add(searchControlPanel, CC.xy(4, 2));
 
         /*
          * Clients table
          */
         add(clientsTableScrollPane, CC.xy(2, 2));
         
         JPanel buttonsPanel = new JPanel();
         {
             buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
             buttonsPanel.add(okButton);
             buttonsPanel.add(cancelButton);
         }
         add(buttonsPanel, CC.xywh(2, 3, 3, 1));
 
         setTitle(getString("Title.PickClient"));
         pack();
         setMinimumSize(getSize());
         setLocationRelativeTo(getParent());
     }
 
     private void buildSearchCriteriasPanel() {
     }
 
     @Override
     public void setVisible(boolean visible) {
         if (visible) {
             setLocationRelativeTo(getParent());
         }
 
         super.setVisible(visible);
     }
 
     private void radioButtonsActionPerformed(ActionEvent evt) {
         if (idRadioButton.isSelected()) {
             idTextField.setEnabled(true);
             idTextField.requestFocusInWindow();
         } else {
             idTextField.setEnabled(false);
             idTextField.setText(null);
         }
 
         if (cardRadioButton.isSelected()) {
             cardTextField.setEnabled(true);
             cardTextField.requestFocusInWindow();
         } else {
             cardTextField.setEnabled(false);
             cardTextField.setText(null);
         }
 
         if (fullNameRadioButton.isSelected()) {
             fullNameTextField.setEnabled(true);
             fullNameTextField.requestFocusInWindow();
         } else {
             fullNameTextField.setEnabled(false);
             fullNameTextField.setText(null);
         }
     }
 
     private void searchButtonActionPerformed(ActionEvent evt) {
         List<ClientDTO> clients;
 
         try {
             if (idRadioButton.isSelected()) {
                 Short id;
 
                 try {
                     id = Short.parseShort(idTextField.getText().trim());
                 } catch (NumberFormatException ex) {
                     throw new ValidationException(MessageFormat.format(getString("Message.FieldIsNotFilledInCorrectly.withFieldName"), getString("Text.ID")));
                 }
 
                 clients = new LinkedList<>();
                 clients.add(ClientsService.getInstance().getById(id));
             } else if (cardRadioButton.isSelected()) {
                 Integer card;
                 try {
                     card = Integer.parseInt(cardTextField.getText().trim());
                 } catch (NumberFormatException ex) {
                     throw new ValidationException(MessageFormat.format(getString("Message.FieldIsNotFilledInCorrectly.withFieldName"), getString("Text.Card")));
                 }
 
                 ClientsService clientsService = ClientsService.getInstance();
                 Short clientId = clientsService.findByCard(card);
 
                 clients = new LinkedList<>();
                 if (clientId != null) {
                     clients.add(clientsService.getById(clientId));
                 }
 
             } else {
                 clients = ClientsService.getInstance().findByFullName(fullNameTextField.getText().trim(), false);
             }
         } catch (ValidationException ex) {
             clients = new LinkedList<>();
         } catch (RuntimeException ex) {
             /*
              * The exception is unexpected. We got to shutdown the dialog for
              * the state of the transaction is now unknown.
              */
             setResult(census.presentation.dialogs.EditOrderDialog.RESULT_EXCEPTION);
             setException(ex);
             dispose();
             return;
         }
 
         clientsTableModel.setClients(clients);
 
         if (!clients.isEmpty()) {
             clientsTable.getSelectionModel().setSelectionInterval(0, 0);
         }
     }
 
     @Override
     protected void onOkActionPerformed(ActionEvent evt) {
         int index = clientsTable.getSelectedRow();
         if (index == -1) {
             MainFrame.getGlobalCensusExceptionListenersStack().peek().processException(new ValidationException(getString("Message.SelectClientFirst")));
             return;
         }
         ClientDTO client = clientsTableModel.getClients().get(index);
         setClientId(client.getId());
 
         super.onOkActionPerformed(evt);
     }
 
     protected class CardFocusAction extends AbstractAction {
 
         @Override
         public void actionPerformed(ActionEvent e) {
             cardRadioButton.doClick(0);
         }
     }
 
     protected class IdFocusAction extends AbstractAction {
 
         @Override
         public void actionPerformed(ActionEvent e) {
             idRadioButton.doClick(0);
         }
     }
 
     protected class FullNameFocusAction extends AbstractAction {
 
         @Override
         public void actionPerformed(ActionEvent e) {
             fullNameRadioButton.doClick(0);
         }
     }
 
     /*
      * Session variables
      */
     private Short clientId;
     /*
      * GUI
      */
     private ClientsTableModel clientsTableModel;
 
     public Short getClientId() {
         return clientId;
     }
 
     public void setClientId(Short clientId) {
         this.clientId = clientId;
     }
     private JButton cancelButton;
     private JRadioButton cardRadioButton;
     private JTextField cardTextField;
     private JTable clientsTable;
     private JScrollPane clientsTableScrollPane;
     private JRadioButton fullNameRadioButton;
     private JTextField fullNameTextField;
     private JRadioButton idRadioButton;
     private JTextField idTextField;
     private ButtonGroup modeButtonGroup;
     private JButton okButton;
     private JButton searchButton;
     private JPanel searchCriteriasPanel;
 }
