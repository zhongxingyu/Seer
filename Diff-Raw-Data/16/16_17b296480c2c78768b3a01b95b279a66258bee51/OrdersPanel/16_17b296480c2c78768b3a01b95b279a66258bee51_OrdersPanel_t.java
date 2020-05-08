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
 
 import java.awt.event.ActionEvent;
 import java.awt.event.FocusEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.math.BigDecimal;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.ResourceBundle;
 
 import javax.swing.JLabel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 import javax.swing.table.TableColumn;
 
 import org.apache.log4j.Logger;
 import org.joda.time.DateMidnight;
 import org.key2gym.business.api.SecurityViolationException;
 import org.key2gym.business.api.dtos.OrderDTO;
 import org.key2gym.business.api.services.CashService;
 import org.key2gym.business.api.services.OrdersService;
 import org.key2gym.client.ContextManager;
 import org.key2gym.client.DataRefreshPulse;
 import org.key2gym.client.actions.BasicAction;
 import org.key2gym.client.actions.EditOrderAction;
 import org.key2gym.client.renderers.OrdersTableCellRenderer;
 import org.key2gym.client.util.OrdersTableModel;
 import org.key2gym.client.util.OrdersTableModel.Column;
 
 import com.jgoodies.forms.factories.CC;
 import com.jgoodies.forms.layout.FormLayout;
 
 /**
  * 
  * @author Danylo Vashchilenko
  */
 public class OrdersPanel extends javax.swing.JPanel {
 
     /**
      * Creates new form OrdersPanel
      */
     public OrdersPanel() {
         ordersService = ContextManager.lookup(OrdersService.class);
         cashService = ContextManager.lookup(CashService.class);
         strings = ResourceBundle
                 .getBundle("org/key2gym/client/resources/Strings");
 
         initComponents();
         buildPanel();
 
         DataRefreshPulse.getInstance().addObserver(new DataRefreshObserver());
     }
 
     private void initComponents() {
 
         cashTextField = new JTextField();
         cashTextField.setEditable(false);
         cashTextField.setColumns(8);
 
         totalTextField = new JTextField();
         totalTextField.setEditable(false);
         totalTextField.setColumns(8);
 
         ordersTableScrollPane = new JScrollPane();
         ordersTable = new JTable();
 
         /*
          * This renderer highlights orders with payment due
          */
         ordersTable.setDefaultRenderer(String.class,
                 new OrdersTableCellRenderer());
 
         /*
          * Columns of the orders table
          */
         Column[] сolumns = new Column[] { Column.ID, Column.SUBJECT,
                 Column.TOTAL, Column.PAID };
 
         ordersTableModel = new OrdersTableModel(сolumns);
         ordersTable.setModel(ordersTableModel);
 
         /*
          * Sets appropriate sizes of the columns.
          */
         int[] widths = new int[] { 50, 200, 50, 50 };
         TableColumn column;
         for (int i = 0; i < widths.length; i++) {
             column = ordersTable.getColumnModel().getColumn(i);
             column.setPreferredWidth(widths[i]);
         }
         ordersTable
                 .setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
         ordersTable.addFocusListener(new java.awt.event.FocusAdapter() {
             @Override
             public void focusLost(java.awt.event.FocusEvent evt) {
                 financialActivitiesTableFocusLost(evt);
             }
         });
         ordersTableScrollPane.setViewportView(ordersTable);
 
         /*
          * Opens the selected order on double left-click.
          */
         ordersTable.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseClicked(MouseEvent e) {
                 if (e.getClickCount() > 1
                         && e.getButton() == MouseEvent.BUTTON1) {
                     new EditOrderAction().actionPerformed(new ActionEvent(this,
                             0, BasicAction.ACTION_CONTEXT));
                 }
             }
         });
     }
 
     private void buildPanel() {
         FormLayout layout = new FormLayout(
                 "5dlu, default, 3dlu, fill:default, 3dlu, default, 3dlu, fill:default, default:grow",
                 "5dlu, default, 3dlu, fill:default:grow");
         setLayout(layout);
 
         add(new JLabel(strings.getString("Label.Total")), CC.xy(2, 2));
         add(totalTextField, CC.xy(4, 2));
         add(new JLabel(strings.getString("Label.Cash")), CC.xy(6, 2));
         add(cashTextField, CC.xy(8, 2));
 
         add(ordersTableScrollPane, CC.xywh(1, 4, 9, 1));
     }
 
     /**
      * Called when the orders table looses the focus.
      * 
      * @param evt
      *            the focus event
      */
     private void financialActivitiesTableFocusLost(FocusEvent evt) {
         ordersTable.clearSelection();
     }
 
     public OrderDTO getSelectedOrder() {
         int index = ordersTable.getSelectedRow();
 
         if (index == -1) {
             return null;
         }
 
         return ordersTableModel.getOrderAt(index);
     }
 
     /**
      * Sets the current journal's date.
      * 
      * @param date
      *            the date to use
      * @throws SecurityViolationException
      *             if the access to the date was denied
      */
     public void setDate(DateMidnight date) throws SecurityViolationException {
         this.date = date;
 
         refresh();
     }
 
     public DateMidnight getDate() {
         return date;
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
         orders = ordersService.findAllByDate(date);
         total = ordersService.getTotalForDate(date);
         cash = cashService.getCashByDate(date);
     }
 
     private void refreshGUI() {
         ordersTableModel.setOrders(orders);
         totalTextField.setText(total.toPlainString());
         cashTextField.setText(cash.toPlainString());
     }
 
     /**
      * Used to refresh the orders at the data refresh rate.
      * 
      * TODO: do proper SecurityViolationException handling (e.g. closing itself)
      */
     private class DataRefreshObserver implements Observer {
 
         @Override
         public void update(Observable observable, Object arg) {
 
             if (date == null || !ContextManager.getInstance().isContextAvailable()) {
                 return;
             }
 
             /*
              * Loads the data synchronously on the timer's thread.
              */
             try {
                 refreshData();
             } catch (SecurityViolationException ex) {
                Logger.getLogger(OrdersPanel.class).error(
                        "Failed to refresh the orders!", ex);
             }
            
            final int selection = ordersTable.getSelectedRow();
 
             /*
              * Updates the GUI asynchronously on the Swing thread.
              */
             SwingUtilities.invokeLater(new Runnable() {
                 @Override
                 public void run() {
                     refreshGUI();
                    ordersTable.getSelectionModel().setSelectionInterval(selection, selection);
                 }
             });
         }
     }
 
     /*
      * Business
      */
     private OrdersService ordersService;
     private CashService cashService;
 
     /*
      * Presentation
      */
     private ResourceBundle strings;
     private DateMidnight date;
     private OrdersTableModel ordersTableModel;
     private JTextField cashTextField;
     private JTable ordersTable;
     private JScrollPane ordersTableScrollPane;
     private JTextField totalTextField;
     /*
      * Data
      */
     private List<OrderDTO> orders;
     private BigDecimal total;
     private BigDecimal cash;
 }
