 package westernstyle.gui;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.ArrayList;
 import java.util.Date;
 import javax.swing.border.LineBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.DefaultTableModel;
 
 import net.sf.nachocalendar.CalendarFactory;
 import net.sf.nachocalendar.components.DateField;
 
 import westernstyle.core.SalesOrder;
 import westernstyle.core.Invoice;
 import westernstyle.core.Customer;
 import westernstyle.DB.SalesOrderDB;
 import westernstyle.DB.CustomerDB;
 import westernstyle.DB.InvoiceDB;
 import westernstyle.DB.GetMax;
 import westernstyle.DB.PurchaseDB;
 import westernstyle.core.Clothing;
 import westernstyle.core.Equipment;
 import westernstyle.core.GunReplica;
 import westernstyle.core.Purchase;
 import westernstyle.core.Product;
 
 public class SalesOrderTab extends JPanel
 {
     private JTable salesOrdertable;
     private JTable productTable;
     private SalesOrderDB salesOrderDB;
     
     public SalesOrderTab()
     {
         salesOrderDB = new SalesOrderDB();
         
         //JPanel panel = new JPanel();
         this.setLayout(new GridLayout(1, 0));
         
         String salesOrderColumns[] = {"#", "id", "Date", "Amount", 
             "Delivery Status", "Delivery Date", "Customer Id", "Invoice Id"};
         salesOrdertable = createTable(salesOrderColumns);
         updateTable();
         
        String productColumns[] = {"id", "Name", "Purchase Pr", "Sales Pr",
            "Rent Pr", "Country", "MIN Stock", "Type"};
         productTable = createTable(productColumns);
         
         salesOrdertable.getSelectionModel().addListSelectionListener(
         new ListSelectionListener() 
         {
             @Override
             public void valueChanged(ListSelectionEvent e)
             {
                 if (salesOrdertable.getSelectedRowCount() > 0)
                 {
                     int id = (Integer)salesOrdertable.getValueAt(salesOrdertable.
                                 getSelectedRow(), 1);
                     PurchaseDB purchaseDB = new PurchaseDB();
                     ArrayList<Purchase> purchases = purchaseDB.
                             getPurchasesBySalesOrderId(id);
                     updateProductTable(purchases);
                 }
             }
         });
 
         JScrollPane salesOrderScrollPane = new JScrollPane(salesOrdertable);
         JScrollPane productScrollPane = new JScrollPane(productTable);
         JButton add = new JButton("Add");
         final CustomerDB customerDB = new CustomerDB();
         final InvoiceDB invoiceDB = new InvoiceDB();
         
         add.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 if (customerDB.getCustomers().isEmpty())
                     showError("At least one customer should be added first.", 
                             "Error");
                 else if (invoiceDB.getInvoices().isEmpty())
                     showError("At least one invoice should be added first.", 
                             "Error");
                 else
                     add();
             }
         });
         JButton edit = new JButton("Edit");
         edit.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 if (salesOrderDB.getSalesOrders().isEmpty())
                     showError("No sales orders have been added.",
                             "Error");
                 else if (salesOrdertable.getSelectedRowCount() == 0)
                     showError("Sales order must be selected", "Error");
                 else
                     edit((Integer)salesOrdertable.getValueAt(salesOrdertable.
                             getSelectedRow(), 1));
             }
         });
         final JTextField searchField = new JTextField();
         JLabel searchLabel = new JLabel(" Delivery Status: ");
         JButton remove = new JButton("Remove");
         remove.addActionListener(new ActionListener() 
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 if (salesOrderDB.getSalesOrders().isEmpty())
                     showError("No sales orders have been added.",
                             "Error");
                 else if (salesOrdertable.getSelectedRowCount() == 0)
                     showError("Sales order must be selected", "Error");
                 else
                     removeSalesOrder((Integer)salesOrdertable.
                             getValueAt(salesOrdertable.getSelectedRow(), 1));
             }
         });
         JButton search = new JButton("Search");
         search.addActionListener(new ActionListener() 
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 showSearchResults(searchField.getText());
             }
         });
         JToolBar toolBar = new JToolBar();
         toolBar.add(add);
         toolBar.add(edit);
         toolBar.add(remove);
         toolBar.add(searchLabel);
         toolBar.add(searchField);
         toolBar.add(search);
 
         JPanel salesOrderPanel = new JPanel();
         salesOrderPanel.setLayout(new BorderLayout());
         salesOrderPanel.add(toolBar, BorderLayout.PAGE_START);
         salesOrderPanel.add(salesOrderScrollPane);
         JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            salesOrderPanel, productScrollPane);
         splitPane.setDividerLocation(200);
         
         this.add(splitPane);
         this.setVisible(true);
     }
     
     private JTable createTable(Object[] columnNames)
     {
         JTable localTable;
 
         DefaultTableModel model = new DefaultTableModel()
         {
             @Override
             public boolean isCellEditable(int row, int column) 
             {
                return false;
             }
         };
 
         for (Object columnName: columnNames)
             model.addColumn(columnName);
 
         localTable = new JTable(model);
         localTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 
         return localTable;
     }
     
     private void removeSalesOrder(int id)
     {
         int choice = JOptionPane.showConfirmDialog(
                 this,
                 "Are you sure you want to remove sales order?", "Removal",
                 JOptionPane.YES_NO_OPTION);
 
         if (choice == 0)
         {
             salesOrderDB.delete(id);
             updateTable();
         }
     }
     
     private void showSearchResults(String deliveryStatus)
     {
         DefaultTableModel model = (DefaultTableModel)salesOrdertable.getModel();
         while (model.getRowCount() > 0)
             model.removeRow(0);
         
         int count = salesOrderDB.getSalesOrdersByStatus(deliveryStatus).size();
         for (int i = 0; i < count; i++)
         {
             SalesOrder salesOrder = salesOrderDB.
                     getSalesOrdersByStatus(deliveryStatus).get(i);
             Object[] data = {i+1, salesOrder.getId(),
                 salesOrder.getDate(), salesOrder.getAmount(), 
                 salesOrder.getDeliveryStatus(), salesOrder.getDeliveryDate(),
                 salesOrder.getCustomer().getId(), 
                 salesOrder.getInvoice().getId()};
             model.addRow(data);
         }
     }
     
     private void updateProductTable(ArrayList<Purchase> purchases)
     {
         DefaultTableModel model = (DefaultTableModel)productTable.getModel();
         while (model.getRowCount() > 0)
             model.removeRow(0);
         
         String type = "";
         for (int i = 0; i < purchases.size(); i++)
         {
             Product product = purchases.get(i).getProduct();
             if (product instanceof Clothing)
                 type = "clothing";
             else if (product instanceof Equipment)
                 type = "equipment";
             else if (product instanceof GunReplica)
                 type = "gun replica";
             
             Object[] data = {product.getId(), product.getName(), 
                 product.getPurchasePrice(), product.getSalesPrice(), 
                 product.getRentPrice(), product.getCountryOfOrigin(),
                 product.getMinStock(), type};
             model.addRow(data);
         }
     }
     
    public void updateTable()
     {
         DefaultTableModel model = (DefaultTableModel)salesOrdertable.getModel();
         while (model.getRowCount() > 0)
             model.removeRow(0);
         
         int count = salesOrderDB.getSalesOrders().size();
         for (int i = 1; i <= count; i++)
         {
             SalesOrder salesOrder = salesOrderDB.getSalesOrder(i);
             if (salesOrder != null)
             {
                 Object[] data = {i, salesOrder.getId(),
                     salesOrder.getDate(), salesOrder.getAmount(), 
                     salesOrder.getDeliveryStatus(), salesOrder.getDeliveryDate(),
                     salesOrder.getCustomer().getId(), 
                     salesOrder.getInvoice().getId()};
                 model.addRow(data);
             }
             else
                 count++;
         }
     }
     
     private void add()
     {
         final JDialog addDialog = dialog("Add Sales Order");
         JPanel myPanel = (JPanel)addDialog.getContentPane().getComponent(0);
         
         final DateField date = ((DateField)myPanel.getComponent(1));
         final JSpinner amount = ((JSpinner)myPanel.getComponent(3));
         final JTextField deliveryStatus = ((JTextField)myPanel.getComponent(5));
         final DateField deliveryDate = (DateField)myPanel.getComponent(7);
         final JSpinner customerId = (JSpinner)myPanel.getComponent(9);
         final JSpinner invoiceId = (JSpinner)myPanel.getComponent(11);
 
         JButton okButton = ((JButton)myPanel.getComponent(12));
         okButton.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 int nextId = GetMax.getMaxId("select max(id) from salesOrder")+1;
                 SalesOrder salesOrder = new SalesOrder(nextId);
                 salesOrder.setDate((Date)date.getValue());
                 salesOrder.setAmount((Integer)amount.getValue());
                 salesOrder.setDeliveryStatus(deliveryStatus.getText());
                 salesOrder.setDeliveryDate((Date)deliveryDate.getValue());
                 CustomerDB customerDB = new CustomerDB();
                 int id = (Integer)customerId.getValue(); 
                 Customer customer = customerDB.getCustomer(id);
                 if (customer == null)
                 {
                     showError("Customer with specified id doesn't exist!", 
                             "Error");
                     return;
                 }
                 InvoiceDB invoiceDB = new InvoiceDB();
                 id = (Integer)invoiceId.getValue();
                 Invoice invoice = invoiceDB.getInvoice(id);
                 if (invoice == null)
                 {
                     showError("invoice with specified id doesn't exist!", 
                             "Error");
                     return;
                 }
                 salesOrder.setInvoice(invoice);
                 salesOrder.setCustomer(customer);
                 salesOrderDB.insertSalesOrder(salesOrder);
                 addDialog.setVisible(false);
                 updateTable();
             }
         });
 
         addDialog.setVisible(true);
     }
     
     private JDialog dialog(String title)
     {
         final JDialog dialog = new JDialog();
         dialog.setTitle(title);
         JPanel myPanel = new JPanel(new GridLayout(7, 2, 10, 10));
         dialog.getContentPane().add(myPanel);
         myPanel.setBorder(new LineBorder(myPanel.getBackground(), 10));
 
         final DateField date = CalendarFactory.createDateField();
         date.setValue(new Date());
         SpinnerNumberModel model = new SpinnerNumberModel(1, 1, 999, 1);
         final JSpinner amount = new JSpinner(model);
         JTextField deliveryStatus = new JTextField();
         final DateField deliveryDate = CalendarFactory.createDateField();
         date.setValue(new Date());
         model = new SpinnerNumberModel(1, 1, 999, 1);
         JSpinner customerId = new JSpinner(model);
         model = new SpinnerNumberModel(1, 1, 999, 1);
         JSpinner invoiceId = new JSpinner(model);
         
         final JComponent[] inputs = new JComponent[] {date, amount, 
             deliveryStatus, deliveryDate, customerId, invoiceId};
         final JComponent[] labels = new JComponent[] {new JLabel("Date"),
                 new JLabel("Amount"), new JLabel("Delivery Status"), 
                 new JLabel("Delivery Date"), new JLabel("Customer Id"), 
                 new JLabel("Invoice Id")};
         for (int i = 0; i < inputs.length; i++)
         {
             labels[i].setSize(120, 20);
             inputs[i].setSize(150,20);
             myPanel.add(labels[i]);
             myPanel.add(inputs[i]);
         }
         JButton okButton = new JButton("Ok");
         myPanel.add(okButton);
         JButton cancelButton = new JButton("Cancel");
         cancelButton.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 dialog.setVisible(false);
             }
         });
         myPanel.add(cancelButton);
         dialog.setResizable(false);
         dialog.pack();
 
         return dialog;
     }
     
     private void showError(String message, String title)
     {
         JOptionPane.showMessageDialog(this, message, title,
             JOptionPane.ERROR_MESSAGE);
     }
     
     private void edit(int id)
     {
         final JDialog editDialog = dialog("Edit Sales Order");
         JPanel myPanel = (JPanel)editDialog.getContentPane().getComponent(0);
         
         final SalesOrder salesOrder = salesOrderDB.getSalesOrder(id);
         final DateField date = ((DateField)myPanel.getComponent(1));
         date.setValue(salesOrder.getDate());
         final JSpinner amount = ((JSpinner)myPanel.getComponent(3));
         amount.setValue(salesOrder.getAmount());
         final JTextField deliveryStatus = ((JTextField)myPanel.getComponent(5));
         deliveryStatus.setText(salesOrder.getDeliveryStatus());
         final DateField deliveryDate = (DateField)myPanel.getComponent(7);
         deliveryDate.setValue(salesOrder.getDeliveryDate());
         final JSpinner customerId = (JSpinner)myPanel.getComponent(9);
         customerId.setValue(salesOrder.getCustomer().getId());
         final JSpinner invoiceId = (JSpinner)myPanel.getComponent(11);
         invoiceId.setValue(salesOrder.getInvoice().getId());
 
         JButton okButton = ((JButton)myPanel.getComponent(12));
         okButton.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 salesOrder.setDate((Date)date.getValue());
                 salesOrder.setAmount((Integer)amount.getValue());
                 salesOrder.setDeliveryStatus(deliveryStatus.getText());
                 salesOrder.setDeliveryDate((Date)deliveryDate.getValue());
                 CustomerDB customerDB = new CustomerDB();
                 int id = (Integer)customerId.getValue(); 
                 Customer customer = customerDB.getCustomer(id);
                 InvoiceDB invoiceDB = new InvoiceDB();
                 id = (Integer)invoiceId.getValue();
                 Invoice invoice = invoiceDB.getInvoice(id);
                 salesOrder.setInvoice(invoice);
                 salesOrder.setCustomer(customer);
                 salesOrderDB.updateSalesOrder(salesOrder);
                 editDialog.setVisible(false);
                 updateTable();
             }
         });
 
         editDialog.setVisible(true);
     }
 }
