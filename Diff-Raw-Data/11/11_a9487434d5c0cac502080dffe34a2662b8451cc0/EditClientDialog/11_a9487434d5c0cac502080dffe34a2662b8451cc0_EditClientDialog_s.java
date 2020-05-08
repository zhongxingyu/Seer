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
 
 import census.business.*;
 import census.business.api.BusinessException;
 import census.business.api.SecurityException;
 import census.business.api.ValidationException;
 import census.business.dto.*;
 import census.presentation.MainFrame;
 import census.presentation.forms.ClientForm;
 import census.presentation.forms.ClientProfileForm;
 import census.presentation.util.AttendancesTableModel;
 import census.presentation.util.FreezesTableModel;
import com.jgoodies.forms.debug.FormDebugPanel;
 import com.jgoodies.forms.factories.CC;
 import com.jgoodies.forms.layout.FormLayout;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.text.MessageFormat;
 import java.util.List;
 import javax.swing.*;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableModel;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import org.joda.time.DateMidnight;
 
 /**
  * This dialog allows user to view and edit the client's information. Features
  * are:
  *
  * <ul>
  *
  * <li> Basic information </li> <li> Freezes </li> <li> Profile information
  * </li> <li> Previous attendances </li>
  *
  * </ul>
  *
  * Session variables:
  *
  * <ul>
  *
  * <li> clientId - the ID of the client to be shown and edited. </li>
  *
  * </ul>
  *
  * A transaction is required to be active, and a session to be open, upon the
  * dialog's creation.
  *
  * This dialog supports hot swapping. The session variables can be set and reset
  * after the
  * <code>setVisible(true)</code> was called.
  *
  * @author Danylo Vashchilenko
  */
 public class EditClientDialog extends CensusDialog {
 
     /**
      * Creates a new instance of this class.
      *
      * The dialog will position itself in the center of its parent.
      *
      * @param parent the parent frame
      */
     public EditClientDialog(JFrame parent) {
         super(parent, true);
 
         sessionsService = SessionsService.getInstance();
         clientsService = ClientsService.getInstance();
         clientProfilesService = ClientProfilesService.getInstance();
         attendancesService = AttendancesService.getInstance();
         freezesService = FreezesService.getInstance();
         ordersService = OrdersService.getInstance();
 
         client = new ClientDTO();
         clientProfile = new ClientProfileDTO();
 
         buildDialog();
     }
 
     @Override
     public void setVisible(boolean visible) {
         if (visible) {
             setLocationRelativeTo(getParent());
         }
         super.setVisible(true);
     }
 
     private void buildDialog() {
         setLayout(new FormLayout("4dlu, d:g, 4dlu", "4dlu, f:[100dlu, m]:g, 4dlu, d, 4dlu"));
 
         add(createTabbedPane(),   CC.xy(2, 2));
         add(createButtonsPanel(), CC.xy(2, 4));
 
         getRootPane().setDefaultButton(okButton);
         setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
         pack();
     }
     
     private JComponent createTabbedPane() {
         tabbedPane = new JTabbedPane();
 
         tabbedPane.addTab(getString("Tab.Client"), createBasicTab()); // NOI18N
         tabbedPane.addTab(getString("Text.Profile"), createProfileTab()); // NOI18N
         tabbedPane.addTab(getString("Tab.Attendances"), createAttendanceTab()); // NOI18N
         tabbedPane.addTab(getString("Tab.Freezes"), createFreezesTab()); // NOI18N
         tabbedPane.addTab(getString("Tab.Orders"), createOrdersTab()); // NOI18N
         
         return tabbedPane;
     }
 
     private JPanel createBasicTab() {
         JPanel panel = new JPanel();
         panel.setLayout(new FormLayout("f:p:g", "4dlu, f:p:g"));
 
         clientPanel = new ClientForm();
         panel.add(clientPanel, CC.xy(1, 2));
 
         return panel;
     }
 
     private JPanel createProfileTab() {
         JPanel panel = new JPanel();
         panel.setLayout(new FormLayout("p:g", "4dlu, p, 3dlu, p"));
 
         attachedCheckBox = new JCheckBox();
         attachedCheckBox.setText(getString("Text.Attached")); // NOI18N
         attachedCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
 
         panel.add(attachedCheckBox, CC.xy(1, 2));
 
         clientProfilePanel = new ClientProfileForm();
         panel.add(clientProfilePanel, CC.xy(1, 4));
 
         return panel;
     }
 
     private JComponent createAttendanceTab() {
         JScrollPane scrollPane = new JScrollPane();
 
         attendancesTable = new JTable();
         scrollPane.setViewportView(attendancesTable);

         return scrollPane;
     }
 
     private JPanel createFreezesTab() {
         JPanel panel = new JPanel(new FormLayout("p", "p, 4dlu, p"));
 
         freezesTableScrollPane = new JScrollPane();
         freezesTable = new JTable();
 
         FreezesTableModel.Column[] freezesTableColumns =
                 new FreezesTableModel.Column[]{
             FreezesTableModel.Column.ADMINISTRATOR_FULL_NAME,
             FreezesTableModel.Column.DATE_ISSUED,
             FreezesTableModel.Column.DAYS,
             FreezesTableModel.Column.DATE_EXPIRED,
             FreezesTableModel.Column.NOTE
         };
 
         freezesTableModel = new FreezesTableModel(freezesTableColumns);
         freezesTable.setModel(freezesTableModel);
 
         int[] freezesTableColumnWidths = new int[]{94, 83, 41, 84, 88};
         TableColumn column;
         for (int i = 0; i < freezesTableColumnWidths.length; i++) {
             column = freezesTable.getColumnModel().getColumn(i);
             column.setPreferredWidth(freezesTableColumnWidths[i]);
         }
         freezesTableScrollPane.setViewportView(freezesTable);
 
         panel.add(freezesTableScrollPane, CC.xy(1, 1));
 
         freezeNoteScrollPane = new JScrollPane();
         freezeNoteTextArea = new JTextArea();
         freezeNoteTextArea.setColumns(20);
         freezeNoteTextArea.setRows(5);
         freezeNoteTextArea.setEnabled(false);
         freezeNoteScrollPane.setViewportView(freezeNoteTextArea);
         freezeNoteScrollPane.setBorder(BorderFactory.createTitledBorder(getString("Text.Note"))); // NOI18N
 
         panel.add(freezeNoteScrollPane, CC.xy(1, 3));
 
         freezesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
 
             @Override
             public void valueChanged(ListSelectionEvent e) {
                 if (freezesTable.getSelectedRowCount() > 1) {
                     freezeNoteTextArea.setText(" ... ");
                 } else if (freezesTable.getSelectedRowCount() < 1) {
                     freezeNoteTextArea.setText(null);
                 } else {
                     freezeNoteTextArea.setText(freezesTableModel.getFreezeAt(freezesTable.getSelectedRow()).getNote());
                 }
             }
         });
 
         return panel;
     }
 
     private JPanel createOrdersTab() {
         JPanel panel = new JPanel(new FormLayout("r:d, 3dlu, f:d:g", "4dlu, d, 4dlu, f:d:g"));
 
         purchasesFilterLabel = new JLabel(getString("Label.Filter")); // NOI18N
         panel.add(purchasesFilterLabel,     CC.xy(1, 2));
         
         String[] periods = new String[]{
             getString("Text.Last7Days"),
             getString("Text.LastMonth"),
             getString("Text.Last3Months"),
             getString("Text.All")
         };
         
         purchasesFilterComboBox = new JComboBox();
         purchasesFilterComboBox.setModel(new DefaultComboBoxModel(periods));
         purchasesFilterComboBox.addItemListener(new ItemListener() {
             @Override
             public void itemStateChanged(ItemEvent evt) {
                 purchasesFilterComboBoxItemStateChanged(evt);
             }
         });
         panel.add(purchasesFilterComboBox,  CC.xy(3, 2));
 
         purchasesTree = new JTree();
         purchasesTree.setRootVisible(false);
         
         purchasesTreeScrollPane = new JScrollPane();
         purchasesTreeScrollPane.setViewportView(purchasesTree);
         panel.add(purchasesTreeScrollPane, CC.xywh(1, 4, 3, 1));
         
         return panel;
     }
 
     private JPanel createButtonsPanel() {
         JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
 
         okButton = new JButton(getOkAction());
         panel.add(okButton);
 
         cancelButton = new JButton(getCancelAction());
         panel.add(cancelButton);
 
         okButton.setPreferredSize(cancelButton.getPreferredSize());
 
         return panel;
     }
 
     /**
      * Processes an OK button click event.
      *
      * @param evt an optional action event
      */
     @Override
     protected void onOkActionPerformed(ActionEvent evt) {
         /*
          * clientPanel is required to be valid, while clientProfilePanel has to
          * be valid only if it is (or is going to be) attached.
          */
         if (!clientPanel.trySave() || (attachedCheckBox.isSelected() && !clientProfilePanel.trySave())) {
             return;
         }
 
         try {
             /*
              * Updates the client
              */
             clientsService.updateClient(client, sessionsService.getPermissionsLevel().equals(SessionsService.PL_ALL));
 
             /*
              * Updates the profile, it the check box was selected.
              */
             if (attachedCheckBox.isSelected()) {
                 // New profiles do not have propper ID, so overwrite the value just in case.
                 clientProfile.setClientId(client.getId());
 
                 ClientProfilesService.getInstance().updateClientProfile(clientProfile);
                 /*
                  * Removes the profile, if it exists
                  */
             } else if (clientProfileAttached) {
                 ClientProfilesService.getInstance().detachClientProfile(clientProfile.getClientId());
             }
         } catch (SecurityException ex) {
             /*
              * GUI garantess that restricted operations can not be permored, so
              * this is probably a bug.
              */
             setResult(EditOrderDialog.RESULT_EXCEPTION);
             setException(new RuntimeException(ex));
             dispose();
             return;
         } catch (BusinessException | ValidationException ex) {
             MainFrame.getGlobalCensusExceptionListenersStack().peek().processException(ex);
             return;
         } catch (RuntimeException ex) {
             /*
              * The exception is unexpected. We got to shutdown the dialog for
              * the state of the transaction is now unknown.
              */
             setResult(EditOrderDialog.RESULT_EXCEPTION);
             setException(ex);
             dispose();
             return;
         }
 
         setResult(RESULT_OK);
         dispose();
     }
 
     private void purchasesFilterComboBoxItemStateChanged(ItemEvent evt) {
         reloadPurchasesTab();
     }
 
     /**
      * Gets the client's ID.
      *
      * The client's ID is the ID that was set with
      * <code>setClientId</code>.
      *
      * @return the client's ID
      */
     public Short getClientId() {
         return clientId;
     }
 
     /**
      * Sets the client's ID. This method causes all components to be reloaded in
      * order to correspond with the new client.
      *
      * @param clientId the client's ID
      * @see EditClientDialog for details about hot swapping
      */
     public void setClientId(Short clientId) {
         this.clientId = clientId;
 
         try {
             client = clientsService.getById(getClientId());
         } catch (ValidationException ex) {
             throw new RuntimeException(ex);
         }
 
         try {
             clientProfile = clientProfilesService.getById(clientId);
             clientProfileAttached = true;
         } catch (ValidationException ex) {
             clientProfile = new ClientProfileDTO();
             clientProfileAttached = false;
         }
 
         /*
          * Title
          */
         setTitle(MessageFormat.format(getString("Title.Client.withFullName"), new Object[]{client.getFullName()}));
 
         /*
          * Client tab
          */
         clientPanel.setClient(client);
 
         /*
          * Profile tab
          */
         clientProfilePanel.setClientProfile(clientProfile);
         attachedCheckBox.setSelected(clientProfileAttached);
         // The administrator needs to have PL_ALL permissions level to detach a profile
         attachedCheckBox.setEnabled(!clientProfileAttached || sessionsService.getPermissionsLevel().equals(census.business.SessionsService.PL_ALL));
 
         /*
          * Attendances tab
          */
         TableModel attendancesTableModel;
 
         // The administrator needs to have PL_ALL permissions level to view old attendances
         if (sessionsService.getPermissionsLevel().equals(SessionsService.PL_ALL)) {
             AttendancesTableModel.Column[] attendancesTableColumns =
                     new AttendancesTableModel.Column[]{
                 AttendancesTableModel.Column.BEGIN_DATE,
                 AttendancesTableModel.Column.BEGIN,
                 AttendancesTableModel.Column.KEY,
                 AttendancesTableModel.Column.END
             };
             List<AttendanceDTO> attendances;
             try {
                 attendances = attendancesService.findAttendancesByClient(client.getId());
             } catch (ValidationException ex) {
                 throw new RuntimeException(ex);
             }
             attendancesTableModel = new AttendancesTableModel(attendancesTableColumns, attendances);
             attendancesTable.setModel(attendancesTableModel);
         } else {
             tabbedPane.setComponentAt(tabbedPane.indexOfComponent(attendancesTableScrollPane), new JLabel(getString("Message.AccessIsDenied"), JLabel.CENTER));
         }
 
         /*
          * Freezes tab
          */
         try {
             freezesTableModel.setFreezes(freezesService.findFreezesForClient(clientId));
         } catch (ValidationException ex) {
             throw new RuntimeException(ex);
         }
 
         /*
          * Purchases tab
          */
         reloadPurchasesTab();
 
     }
 
     private void reloadPurchasesTab() {
         /*
          * Purchases tab
          */
         List<OrderDTO> ordersDTO;
         DateMidnight end = new DateMidnight();
         DateMidnight begin;
         if (purchasesFilterComboBox.getSelectedIndex() == 0) {
             begin = end.minusDays(7);
         } else if (purchasesFilterComboBox.getSelectedIndex() == 1) {
             begin = end.minusMonths(1);
         } else if (purchasesFilterComboBox.getSelectedIndex() == 2) {
             begin = end.minusMonths(3);
         } else {
             begin = client.getRegistrationDate();
         }
 
         try {
             ordersDTO = ordersService.findForClientWithinPeriod(clientId, begin, end);
         } catch (ValidationException ex) {
             throw new RuntimeException(ex);
         }
 
         DefaultMutableTreeNode topNode = new DefaultMutableTreeNode();
         DefaultMutableTreeNode dateNode;
         DefaultMutableTreeNode itemNode;
         for (OrderDTO orderDTO : ordersDTO) {
             String text = MessageFormat.format(getString("Text.Order.withDateAndTotalAndPaid"),
                     new Object[]{orderDTO.getDate().toDate(), //NOI18N
                         orderDTO.getTotal().toPlainString(),
                         orderDTO.getPayment().toPlainString()
                     });
             dateNode = new DefaultMutableTreeNode(text);
 
             for (OrderLineDTO orderLine : orderDTO.getOrderLines()) {
                 String nodeText = MessageFormat.format(
                         getString("Text.OrderLine(ItemTitle,Quantity,DiscountTitle)"),
                         new Object[]{
                             orderLine.getItemTitle(),
                             orderLine.getQuantity(),
                             orderLine.getDiscountTitle() == null ? getString("Text.Discount.None") : orderLine.getDiscountTitle()
                         });
                 itemNode = new DefaultMutableTreeNode(nodeText);
                 dateNode.add(itemNode);
             }
 
             topNode.add(dateNode);
         }
 
         purchasesTree.setModel(new DefaultTreeModel(topNode));
     }
     /*
      * Business services
      */
     private SessionsService sessionsService;
     private ClientsService clientsService;
     private ClientProfilesService clientProfilesService;
     private AttendancesService attendancesService;
     private FreezesService freezesService;
     private OrdersService ordersService;
     /*
      * Presentation
      */
     private Short clientId;
     private Boolean clientProfileAttached;
     private FreezesTableModel freezesTableModel;
     /*
      * Components
      */
     private JCheckBox attachedCheckBox;
     private JTable attendancesTable;
     private JScrollPane attendancesTableScrollPane;
     private JButton cancelButton;
     private ClientDTO client;
     private ClientForm clientPanel;
     private ClientProfileDTO clientProfile;
     private ClientProfileForm clientProfilePanel;
     private JScrollPane freezeNoteScrollPane;
     private JTextArea freezeNoteTextArea;
     private JTable freezesTable;
     private JScrollPane freezesTableScrollPane;
     private JButton okButton;
     private JComboBox purchasesFilterComboBox;
     private JLabel purchasesFilterLabel;
     private JTree purchasesTree;
     private JScrollPane purchasesTreeScrollPane;
     private JTabbedPane tabbedPane;
 }
