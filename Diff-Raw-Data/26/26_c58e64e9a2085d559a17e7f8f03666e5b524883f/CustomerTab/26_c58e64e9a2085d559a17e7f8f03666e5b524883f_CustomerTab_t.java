 package westernstyle.gui;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.ArrayList;
 import javax.swing.border.LineBorder;
 import javax.swing.table.DefaultTableModel;
 
 import westernstyle.core.Customer;
 import westernstyle.DB.CustomerDB;
 import westernstyle.DB.SalesOrderDB;
 import westernstyle.core.SalesOrder;
 import westernstyle.DB.GetMax;
 
 public class CustomerTab extends JPanel
 {
     private JTable custTable;
     private CustomerDB customerDB;
     private SalesOrderTab salesOrderTab;
     
     public CustomerTab(SalesOrderTab salesOrderTab)
     {
         this.salesOrderTab = salesOrderTab;
         customerDB = new CustomerDB();
         
         this.setLayout(new GridLayout(1, 0));
         String columns[] = {"#", "id", "Name", "Address", "Zip Code", "city", 
             "Phone Nr"};
         custTable = createTable(columns);
         updateCustTable();
 
         JScrollPane custScrollPane = new JScrollPane(custTable);
         JButton add = new JButton("Add");
         add.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 addCustomer();
             }
         });
         JButton edit = new JButton("Edit");
         edit.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 if (customerDB.getCustomers().isEmpty())
                     showError("No customers have been added.",
                             "Error");
                 else if (custTable.getSelectedRowCount() == 0)
                     showError("Customer must be selected", "Error");
                 else
                     editCustomer((Integer)custTable.
                             getValueAt(custTable.getSelectedRow(),1));
             }
         });
         
         final JTextField searchField = new JTextField();
         JLabel searchLabel = new JLabel(" Name: ");
         JButton remove = new JButton("Remove");
         remove.addActionListener(new ActionListener() 
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 if (customerDB.getCustomers().isEmpty())
                     showError("No customers have been added.",
                             "Error");
                 else if (custTable.getSelectedRowCount() == 0)
                     showError("Customer must be selected", "Error");
                 else
                     removeCustomer((Integer)custTable.
                             getValueAt(custTable.getSelectedRow(), 1));
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
 
         JPanel custPanel = new JPanel();
         custPanel.setLayout(new BorderLayout());
         custPanel.add(toolBar, BorderLayout.PAGE_START);
         custPanel.add(custScrollPane);
 
         this.add(custPanel);
         this.setVisible(true);
     }
     
     private JTable createTable(Object[] columnNames)
     {
         JTable table;
 
         DefaultTableModel model = new DefaultTableModel();
 
         for (Object columnName: columnNames)
             model.addColumn(columnName);
 
         table = new JTable(model);
         table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 
         return table;
     }
     
     private void removeCustomer(int id)
     {
         Customer customer = customerDB.getCustomer(id);
         SalesOrderDB salesOrderDB = new SalesOrderDB();
         ArrayList<SalesOrder> salesOrders = salesOrderDB.
                 getSalesOrdersByCustomerId(id);
         int choice;
         String custName = customer.getName();
         if (!salesOrders.isEmpty())
         {
             choice = JOptionPane.showConfirmDialog(
                     this,
                     "Removing customer \""+ custName +"\" will also remove all"
                     + " associated sales orders. Are you sure you want to "
                     + "do that?",
                     "Removal",
                     JOptionPane.YES_NO_OPTION);
             if (choice == 0)
             {
                 for (int i = 0; i < salesOrders.size(); i++)
                 {
                     salesOrderDB.delete(salesOrders.get(i).getId());
                 }
                 salesOrderTab.updateTable();
             }
         }
         else
         {
             choice = JOptionPane.showConfirmDialog(
                     this,
                     "Are you sure you want to remove \""+ custName +"\" ?",
                     "Removal",
                     JOptionPane.YES_NO_OPTION);
         }
         if (choice == 0)
         {
             customerDB.delete(id);
             updateCustTable();
         }
     }
     
     private void showSearchResults(String name)
     {
         DefaultTableModel model = (DefaultTableModel) custTable.getModel();
         while (model.getRowCount() > 0)
             model.removeRow(0);
         
         int custCount = customerDB.getCustomersByName(name).size();
         for (int i = 0; i < custCount; i++)
         {
             Customer customer = customerDB.getCustomersByName(name).get(i);
            Object[] custData = {i+1, customer.getId(),
                 customer.getName(), customer.getAddress(),
                 customer.getOrderZipCode(), customer.getCity(), 
                 customer.getPhoneno()};
             model.addRow(custData);
         }
     }
     
     private void updateCustTable()
     {
         DefaultTableModel model = (DefaultTableModel) custTable.getModel();
         while (model.getRowCount() > 0)
             model.removeRow(0);
         
         int custCount = customerDB.getCustomers().size();
         for (int i = 1; i <= custCount; i++)
         {
             Customer customer = customerDB.getCustomer(i);
             
             if (customer != null)
             {
                 Object[] custData = {i, customer.getId(),
                     customer.getName(), customer.getAddress(),
                     customer.getOrderZipCode(), customer.getCity(), 
                     customer.getPhoneno()};
                 model.addRow(custData);
             }
             else
                 custCount++;
         }
     }
     
     private void addCustomer()
     {
         final JDialog addCustDialog = dialog("Add Customer");
         JPanel myPanel = (JPanel)addCustDialog.getContentPane().getComponent(0);
         
         final JTextField name = ((JTextField)myPanel.getComponent(1));
         final JTextField address = ((JTextField)myPanel.getComponent(3));
         final JSpinner zipCode = ((JSpinner)myPanel.
                 getComponent(5));
         final JTextField city = ((JTextField)myPanel.getComponent(7));
         final JTextField phoneNr = ((JTextField)myPanel.getComponent(9));
 
         JButton okButton = ((JButton)myPanel.getComponent(10));
         okButton.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 int nextId = GetMax.getMaxId("select max(id) from customer") + 1;
                 Customer customer = new Customer(nextId);
                 customer.setAddress(address.getText());
                 customer.setName(name.getText());
                 customer.setCity(city.getText());
                 customer.setPhoneno(phoneNr.getText());
                 customer.setOrderZipCode((Integer)zipCode.getValue());
                 customerDB.insertCustomer(customer);
                 addCustDialog.setVisible(false);
                 updateCustTable();
             }
         });
 
         addCustDialog.setVisible(true);
     }
     
     private JDialog dialog(String title)
     {
         final JDialog custDialog = new JDialog();
         custDialog.setTitle(title);
         JPanel myPanel = new JPanel(new GridLayout(6, 2, 10, 10));
         custDialog.getContentPane().add(myPanel);
         myPanel.setBorder(new LineBorder(myPanel.getBackground(), 10));
 
         final JTextField name = new JTextField();
         final JTextField address = new JTextField();
         final JTextField phoneNr = new JTextField();
         final JTextField city = new JTextField();
         SpinnerNumberModel model = new SpinnerNumberModel(1000, 1000, 9999, 1);
         final JSpinner zipCode = new JSpinner(model);
         final JComponent[] inputs = new JComponent[] {name, address, zipCode,
             city, phoneNr};
         final JComponent[] labels = new JComponent[] {new JLabel("Name"),
                 new JLabel("Address"), new JLabel("Zip Code"),
                 new JLabel("City"), new JLabel("Phone Nr")};
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
                 custDialog.setVisible(false);
             }
         });
         myPanel.add(cancelButton);
         custDialog.setResizable(false);
         custDialog.pack();
 
         return custDialog;
     }
     
     private void showError(String message, String title)
     {
         JOptionPane.showMessageDialog(this, message, title,
             JOptionPane.ERROR_MESSAGE);
     }
     
     private void editCustomer(int id)
     {
         final JDialog editCustDialog = dialog("Edit Customer");
         JPanel myPanel = (JPanel)editCustDialog.getContentPane().getComponent(0);
         
         final Customer customer = customerDB.getCustomer(id);
         final JTextField name = ((JTextField)myPanel.getComponent(1));
         name.setText(customer.getName());
         final JTextField address = ((JTextField)myPanel.getComponent(3));
         address.setText(customer.getAddress());
         final JSpinner zipCode = ((JSpinner)myPanel.
                 getComponent(5));
         zipCode.setValue(customer.getOrderZipCode());
         final JTextField city = ((JTextField)myPanel.getComponent(7));
         city.setText(customer.getCity());
         final JTextField phoneNr = ((JTextField)myPanel.getComponent(9));
         phoneNr.setText(customer.getPhoneno());
         
 
         JButton okButton = ((JButton)myPanel.getComponent(10));
         okButton.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 customer.setName(name.getText());
                 customer.setAddress(address.getText());
                 customer.setOrderZipCode((Integer)zipCode.getValue());
                 customer.setCity(city.getText());
                 customer.setPhoneno(phoneNr.getText());
                 customerDB.updateCustomer(customer);
                 editCustDialog.setVisible(false);
                 updateCustTable();
             }
         });
 
         editCustDialog.setVisible(true);
     }
 }
