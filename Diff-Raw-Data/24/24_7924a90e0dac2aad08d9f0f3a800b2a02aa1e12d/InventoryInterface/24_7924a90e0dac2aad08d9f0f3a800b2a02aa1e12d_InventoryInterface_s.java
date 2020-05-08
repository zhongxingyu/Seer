 package GUI;
 
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
import java.applet.Applet;
 import datatstructures.*;
 
 public class InventoryInterface extends JFrame implements ActionListener {
 
     JPanel pnlTable;
     JPanel pnlButtons;
     JTextField txtSearch;
     JButton btnGO;
     JTextArea txaInfo;
     JButton btnAdd;
     JButton btnModify;
     JButton btnDelete;
     JTextField txtQuantity;
     JMenuBar menuBar;
     JMenu file;
     JMenuItem logOut;
     JScrollPane scrollPane;
     JTable table;
     JTable table2;
     JLabel lblInventory;
     Font fntLabels = new Font("Arial Narrow", Font.BOLD, 20);
     JLabel lblQuantity;
     JLabel lblCost;
     ButtonGroup btnGroup;
     ButtonGroup btnGroup2;
     JRadioButton rbtnFind;
     JRadioButton rbtnSort;
     JRadioButton rbtnNumber;
     JRadioButton rbtnName;
     JRadioButton rbtnPrice;
     JLabel lblOr;
     JLabel lblItemBy;
     Dialog addDialog;
     Dialog modDialog;
     Dialog delDialog;
 
     public InventoryInterface(String title) {
         super(title);
         setLayout(null);
         setSize(1000, 800);
         setVisible(true);
         init();
     }
 
     public static void main(String args[]) {
     }
 
     public void init() {
 
         initializeTable();
         initializeButtons();
     }
 
     public void initializeTable() {
         pnlTable = new JPanel();
         pnlTable.setLayout(null);
         pnlTable.setBounds(350, 0, 700, 800);
         lblInventory = new JLabel("Inventory");
         lblInventory.setBounds(280, 10, 200, 100);
         lblInventory.setFont(fntLabels);
         pnlTable.add(lblInventory);
 
         String[] columnNames = {"Item #",
             "Description",
             "Quantity",
            "Cost", "Price",
        };
 
         Object[][] data = {{"1", "Campbell's Soup",
                 "2", "$3.00", "$ 5.00"
             }
         };
 
         table = new JTable(data, columnNames);
         table.setEnabled(false);
         scrollPane = new JScrollPane(table);
         scrollPane.setBounds(20, 100, 600, 500);
         pnlTable.add(scrollPane);
 
         lblQuantity = new JLabel("Total Quantity: " + 39);
         lblQuantity.setBounds(150, 650, 200, 50);
         lblQuantity.setFont(fntLabels);
         lblCost = new JLabel("Total Cost: $" + 200.00);
         lblCost.setBounds(350, 650, 200, 50);
         lblCost.setFont(fntLabels);
 
         pnlTable.add(lblQuantity);
         pnlTable.add(lblCost);
         this.add(pnlTable);
 
     }
 
     public void initializeButtons() {
         pnlButtons = new JPanel();
         pnlButtons.setLayout(null);
         pnlButtons.setBounds(0, 0, 350, 800);
 
         rbtnFind = new JRadioButton("Find");
         rbtnFind.setBounds(20, 40, 80, 50);
         rbtnFind.setFont(fntLabels);
         rbtnFind.setSelected(true);
         rbtnSort = new JRadioButton("Sort");
         rbtnSort.setBounds(180, 40, 120, 50);
         rbtnSort.setFont(fntLabels);
         rbtnNumber = new JRadioButton("Number");
         rbtnNumber.setBounds(30, 140, 150, 50);
         rbtnNumber.setFont(fntLabels);
         rbtnNumber.setSelected(true);
         rbtnName = new JRadioButton("Name");
         rbtnName.setBounds(30, 180, 80, 50);
         rbtnName.setFont(fntLabels);
         rbtnPrice = new JRadioButton("Price");
         rbtnPrice.setBounds(30, 220, 80, 50);
         rbtnPrice.setFont(fntLabels);
 
         lblOr = new JLabel("OR");
         lblOr.setBounds(120, 55, 60, 20);
         lblOr.setFont(fntLabels);
         pnlButtons.add(lblOr);
         lblItemBy = new JLabel("item by:");
         lblItemBy.setBounds(100, 90, 180, 50);
         lblItemBy.setFont(fntLabels);
         pnlButtons.add(lblItemBy);
 
         btnGroup = new ButtonGroup();
         btnGroup.add(rbtnFind);
         btnGroup.add(rbtnSort);
         btnGroup2 = new ButtonGroup();
         btnGroup2.add(rbtnNumber);
         btnGroup2.add(rbtnName);
         btnGroup2.add(rbtnPrice);
 
 
         txtSearch = new JTextField(50);
         txtSearch.setBounds(20, 300, 160, 30);
         txtSearch.setFont(fntLabels);
         pnlButtons.add(txtSearch);
         btnGO = new JButton("GO");
         btnGO.setBounds(200, 300, 100, 30);
         btnGO.setFont(fntLabels);
         pnlButtons.add(btnGO);
 
 
         String[] columnNames = {"Item #",
             "Description",
             "Quantity",
            "Cost", "Price",
        };
         Object[][] data = {{"1", "Campbell's Soup",
                 "2", "$ 5.00", "$3.00"
             }
         };
         table2 = new JTable(data, columnNames);
         table2.setEnabled(false);
         scrollPane = new JScrollPane(table2);
         scrollPane.setBounds(10, 370, 340, 33);
         pnlButtons.add(scrollPane);
 
 
         btnAdd = new JButton("Add");
         btnAdd.setBounds(80, 450, 150, 50);
         btnAdd.setFont(fntLabels);
         btnAdd.addActionListener(this);
         btnModify = new JButton("Modify");
         btnModify.setBounds(80, 550, 150, 50);
         btnModify.setFont(fntLabels);
         btnModify.addActionListener(this);
         btnDelete = new JButton("Delete");
         btnDelete.setBounds(80, 650, 150, 50);
         btnDelete.setFont(fntLabels);
         btnDelete.addActionListener(this);
 
 
 
 
         pnlButtons.add(btnAdd);
         pnlButtons.add(btnModify);
         pnlButtons.add(btnDelete);
         pnlButtons.add(rbtnFind);
         pnlButtons.add(rbtnSort);
         pnlButtons.add(rbtnNumber);
         pnlButtons.add(rbtnName);
         pnlButtons.add(rbtnPrice);
 
         this.add(pnlButtons);
     }
 
     public void addDialog() {
         addDialog = new Dialog(this, "Add New Item");
         SpringLayout layout = new SpringLayout();
         addDialog.setLayout(layout);
 
         JLabel lblItem = new JLabel("Item #");
         layout.putConstraint(SpringLayout.WEST, lblItem, 10, SpringLayout.WEST, this);
         layout.putConstraint(SpringLayout.NORTH, lblItem, 10, SpringLayout.NORTH, this);
         JLabel lblDesc = new JLabel("Description");
         layout.putConstraint(SpringLayout.WEST, lblDesc, 10, SpringLayout.WEST, this);
         layout.putConstraint(SpringLayout.NORTH, lblDesc, 40, SpringLayout.NORTH, this);
         JLabel lblCost = new JLabel("Cost");
         layout.putConstraint(SpringLayout.WEST, lblCost, 10, SpringLayout.WEST, this);
         layout.putConstraint(SpringLayout.NORTH, lblCost, 70, SpringLayout.NORTH, this);
         JLabel lblPrice = new JLabel("Price");
         layout.putConstraint(SpringLayout.WEST, lblPrice, 10, SpringLayout.WEST, this);
         layout.putConstraint(SpringLayout.NORTH, lblPrice, 100, SpringLayout.NORTH, this);
         JLabel lblQuantity = new JLabel("Quantity");
         layout.putConstraint(SpringLayout.WEST, lblQuantity, 10, SpringLayout.WEST, this);
         layout.putConstraint(SpringLayout.NORTH, lblQuantity, 130, SpringLayout.NORTH, this);
 
         JTextField txtItem = new JTextField(20);
         layout.putConstraint(SpringLayout.WEST, txtItem, 80, SpringLayout.WEST, this);
         layout.putConstraint(SpringLayout.NORTH, txtItem, 10, SpringLayout.NORTH, this);
         JTextField txtDesc = new JTextField(20);
         layout.putConstraint(SpringLayout.WEST, txtDesc, 80, SpringLayout.WEST, this);
         layout.putConstraint(SpringLayout.NORTH, txtDesc, 40, SpringLayout.NORTH, this);
         JTextField txtCost = new JTextField(20);
         layout.putConstraint(SpringLayout.WEST, txtCost, 80, SpringLayout.WEST, this);
         layout.putConstraint(SpringLayout.NORTH, txtCost, 70, SpringLayout.NORTH, this);
         JTextField txtPrice = new JTextField(20);
         layout.putConstraint(SpringLayout.WEST, txtPrice, 80, SpringLayout.WEST, this);
         layout.putConstraint(SpringLayout.NORTH, txtPrice, 100, SpringLayout.NORTH, this);
         JTextField txtQuantity = new JTextField(20);
         layout.putConstraint(SpringLayout.WEST, txtQuantity, 80, SpringLayout.WEST, this);
         layout.putConstraint(SpringLayout.NORTH, txtQuantity, 130, SpringLayout.NORTH, this);
 
         JButton btnOK = new JButton("Add");
         layout.putConstraint(SpringLayout.WEST, btnOK, 90, SpringLayout.WEST, this);
         layout.putConstraint(SpringLayout.NORTH, btnOK, 170, SpringLayout.NORTH, this);
         JButton btnCancel = new JButton("Cancel");
         layout.putConstraint(SpringLayout.WEST, btnCancel, 170, SpringLayout.WEST, this);
         layout.putConstraint(SpringLayout.NORTH, btnCancel, 170, SpringLayout.NORTH, this);
 
         addDialog.add(lblItem);
         addDialog.add(txtItem);
         addDialog.add(lblDesc);
         addDialog.add(txtDesc);
         addDialog.add(lblCost);
         addDialog.add(txtCost);
         addDialog.add(lblPrice);
         addDialog.add(txtPrice);
         addDialog.add(lblQuantity);
         addDialog.add(txtQuantity);
         addDialog.add(btnOK);
         addDialog.add(btnCancel);
 
         addDialog.setSize(350, 250);
         addDialog.setVisible(true);
 
         btnOK.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 /* Blank out the dialog box*/
 
                 addDialog.setVisible(false);
                 addDialog.dispose();
             }
         });
 
 
         btnCancel.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 /* Blank out the dialog box*/
 
                 addDialog.setVisible(false);
                 addDialog.dispose();
             }
         });
     }
 
     public boolean isNumeric(String input) {
         try {
             Integer.parseInt(input);
             return true;
         } catch (NumberFormatException e) {
             // input not numeric
             return false;
         }
     }
 
     public void modDialog() {
         modDialog = new Dialog(this, "Modify Item");
         SpringLayout layout = new SpringLayout();
         modDialog.setLayout(layout);
 
         JLabel lblQuantity = new JLabel("Quantity Left");
         layout.putConstraint(SpringLayout.WEST, lblQuantity, 10, SpringLayout.WEST, this);
         layout.putConstraint(SpringLayout.NORTH, lblQuantity, 10, SpringLayout.NORTH, this);
         JLabel lblDesc = new JLabel("Description");
         layout.putConstraint(SpringLayout.WEST, lblDesc, 10, SpringLayout.WEST, this);
         layout.putConstraint(SpringLayout.NORTH, lblDesc, 40, SpringLayout.NORTH, this);
         JLabel lblCost = new JLabel("Cost");
         layout.putConstraint(SpringLayout.WEST, lblCost, 10, SpringLayout.WEST, this);
         layout.putConstraint(SpringLayout.NORTH, lblCost, 70, SpringLayout.NORTH, this);
         JLabel lblPrice = new JLabel("Price");
         layout.putConstraint(SpringLayout.WEST, lblPrice, 10, SpringLayout.WEST, this);
         layout.putConstraint(SpringLayout.NORTH, lblPrice, 100, SpringLayout.NORTH, this);
 
 
        JTextField txtQuantity = new JTextField(20);
         layout.putConstraint(SpringLayout.WEST, txtQuantity, 90, SpringLayout.WEST, this);
         layout.putConstraint(SpringLayout.NORTH, txtQuantity, 10, SpringLayout.NORTH, this);
         JTextField txtDesc = new JTextField(20);
         layout.putConstraint(SpringLayout.WEST, txtDesc, 90, SpringLayout.WEST, this);
         layout.putConstraint(SpringLayout.NORTH, txtDesc, 40, SpringLayout.NORTH, this);
         JTextField txtCost = new JTextField(20);
         layout.putConstraint(SpringLayout.WEST, txtCost, 90, SpringLayout.WEST, this);
         layout.putConstraint(SpringLayout.NORTH, txtCost, 70, SpringLayout.NORTH, this);
         JTextField txtPrice = new JTextField(20);
         layout.putConstraint(SpringLayout.WEST, txtPrice, 90, SpringLayout.WEST, this);
         layout.putConstraint(SpringLayout.NORTH, txtPrice, 100, SpringLayout.NORTH, this);
 
 
         JButton btnOK = new JButton("Add");
         layout.putConstraint(SpringLayout.WEST, btnOK, 90, SpringLayout.WEST, this);
         layout.putConstraint(SpringLayout.NORTH, btnOK, 150, SpringLayout.NORTH, this);
         JButton btnCancel = new JButton("Cancel");
         layout.putConstraint(SpringLayout.WEST, btnCancel, 170, SpringLayout.WEST, this);
         layout.putConstraint(SpringLayout.NORTH, btnCancel, 150, SpringLayout.NORTH, this);
 
         modDialog.add(lblQuantity);
         modDialog.add(txtQuantity);
         modDialog.add(lblDesc);
         modDialog.add(txtDesc);
         modDialog.add(lblCost);
         modDialog.add(txtCost);
         modDialog.add(lblPrice);
         modDialog.add(txtPrice);
         modDialog.add(btnOK);
         modDialog.add(btnCancel);
 
         modDialog.setSize(350, 250);
         modDialog.setVisible(true);
 
         btnOK.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 /* Blank out the dialog box*/
                 if (isNumeric(txtQuantity.getText())) {
                     modDialog.setVisible(false);
                 }
                 modDialog.dispose();
             }
         });
 
 
         btnCancel.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 /* Blank out the dialog box*/
 
                 modDialog.setVisible(false);
                 modDialog.dispose();
             }
         });
     }
 
     public void delDialog() {
         delDialog = new Dialog(this, "Delete Inventory");
         SpringLayout layout = new SpringLayout();
         delDialog.setLayout(layout);
 
         JLabel lblQuantity = new JLabel("Quantity Left");
         layout.putConstraint(SpringLayout.WEST, lblQuantity, 10, SpringLayout.WEST, this);
         layout.putConstraint(SpringLayout.NORTH, lblQuantity, 10, SpringLayout.NORTH, this);
 
 
 
         JTextField txtQuantity = new JTextField(20);
         layout.putConstraint(SpringLayout.WEST, txtQuantity, 90, SpringLayout.WEST, this);
         layout.putConstraint(SpringLayout.NORTH, txtQuantity, 10, SpringLayout.NORTH, this);
 
 
 
         JButton btnOK = new JButton("Add");
         layout.putConstraint(SpringLayout.WEST, btnOK, 90, SpringLayout.WEST, this);
         layout.putConstraint(SpringLayout.NORTH, btnOK, 150, SpringLayout.NORTH, this);
         JButton btnCancel = new JButton("Cancel");
         layout.putConstraint(SpringLayout.WEST, btnCancel, 170, SpringLayout.WEST, this);
         layout.putConstraint(SpringLayout.NORTH, btnCancel, 150, SpringLayout.NORTH, this);
 
         delDialog.add(lblQuantity);
         delDialog.add(txtQuantity);
         delDialog.add(btnOK);
         delDialog.add(btnCancel);
 
         delDialog.setSize(350, 250);
         delDialog.setVisible(true);
 
         btnOK.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 /* Blank out the dialog box*/
 
                 delDialog.setVisible(false);
                 delDialog.dispose();
             }
         });
 
 
         btnCancel.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 /* Blank out the dialog box*/
 
                 delDialog.setVisible(false);
                 delDialog.dispose();
             }
         });
     }
 
     public void actionPerformed(ActionEvent e) {
 
         String arg = e.getActionCommand();
         Object source = e.getSource();
 
 
         if (source instanceof JButton) {
             if (arg.equals("Add")) {
                 addDialog();
             } else if (arg.equals("Modify")) {
                 modDialog();
             } else if (arg.equals("Delete")) {
                 delDialog();
             }
         }
 
     }
 }
