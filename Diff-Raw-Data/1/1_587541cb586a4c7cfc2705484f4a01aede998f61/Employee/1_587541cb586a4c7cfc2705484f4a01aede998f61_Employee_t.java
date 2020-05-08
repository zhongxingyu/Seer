 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package view.employee;
 
 import connectivity.*;
 import java.awt.Component;
 import java.awt.Frame;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.List;
 import java.util.ResourceBundle;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.table.DefaultTableModel;
 import main.*;
 import model.Customer;
 import model.Luggage;
 import model.Resort;
 
 /**
  *
  * @author Team AwesomeSauce
  */
 public class Employee extends javax.swing.JFrame {
 
     private final QueryManager query = new QueryManager();
     private Luggage luggageModel = new Luggage();
     private Customer customerModel = new Customer();
     private Resort resortModel = new Resort();
 
     private List<Luggage> luggages;
     private List<Customer> customers;
     private List<Resort> resorts;
 
     private DefaultTableModel modelLuggage1, modelCustomer1, modelLuggage2, modelCustomer2, modelResort, modelResort1;
 
     public static int customerId;
     public static int luggageId;
     public static int resortId;
 
     private String customerToLink = null;
     private String luggageToLink = null;
     private String resortToLink = null;
 
     public boolean isLinked = false;
     public static String customerFullName;
 
     private Component ErrorPopUp;
     private Component LuggagePopUp;
     private Component confirmationPopUp;
 
     //    private Locale locale = new Locale("nl", "NL");
     private final ResourceBundle BUNDLE = ResourceBundle.getBundle("languages.ResourceBundle"); //, locale
 
     public Employee() {
         initComponents();
         modelLuggage1 = (DefaultTableModel) this.luggageTable1.getModel();
         modelCustomer1 = (DefaultTableModel) this.customerTable1.getModel();
         modelLuggage2 = (DefaultTableModel) this.luggageTable2.getModel();
         modelCustomer2 = (DefaultTableModel) this.customerTable2.getModel();
         modelResort = (DefaultTableModel) this.tblResort.getModel();
         modelResort1 = (DefaultTableModel)this.tblResort1.getModel();
 
         searchCustomerTable1(9999, "");
         searchLuggageTable1(9999, "", 0);
         searchCustomerTable2(9999, "");
         searchLuggageTable2(9999, "", 0);
         searchResortTable(9999, "");
         searchResortTable1(9999, "");
        
 
     }
 
     /**
      * Searches through the luggage database and loads the results into the
      * first luggage table. This method takes the parameters and calls upon the
      * searchLuggageList method to produce the intended result.
      *
      * @param dbField specifies the field to search in. Setting this parameter
      * to 0 searches all fields.
      * @param searchArg argument used to search the database.
      * @param showHandled when this parameter is 1 it will show luggage that has
      * already been handled
      */
     public void searchLuggageTable1(int dbField, String searchArg, int showHandled) {
         modelLuggage1.setRowCount(0); //nodig voor 
         luggages = query.searchLuggageList(dbField, searchArg, showHandled);
         for (Luggage luggage : luggages) {
             modelLuggage1.addRow(new Object[]{new Integer(luggage.getLuggageId()),
                 (luggage.getCustomerId() == 0) ? BUNDLE.getString("unassigned")
                 : luggage.getCustomerId(),
                 luggage.getDescription(),
                 luggage.getLocation(),
                 luggage.getDateLost(),
                 (luggage.getStatus() == 2) ? BUNDLE.getString("found")
                 : (luggage.getStatus() == 1) ? BUNDLE.getString("lost") : BUNDLE.getString("handled")});
         }
     }
 
     /**
      * Searches through the luggage database and loads the results into the
      * second luggage table. This method takes the parameters and calls upon the
      * searchLuggageList method to produce the intended result.
      *
      * @param dbField specifies the field to search in. Setting this parameter
      * to 0 searches all fields.
      * @param searchArg argument used to search the database.
      * @param showHandled when this parameter is 1 it will show luggage that has
      * already been handled
      */
     private void searchLuggageTable2(int dbField, String searchArg, int showHandled) {
         modelLuggage2.setRowCount(0); //nodig voor 
         luggages = query.searchLuggageList(dbField, searchArg, showHandled);
         for (Luggage luggage : luggages) {
             modelLuggage2.addRow(new Object[]{new Integer(luggage.getLuggageId()),
                 (luggage.getCustomerId() == 0) ? BUNDLE.getString("unassigned")
                 : luggage.getCustomerId(),
                 luggage.getDescription(),
                 luggage.getLocation(),
                 luggage.getDateLost(),
                 (luggage.getStatus() == 2) ? BUNDLE.getString("found")
                 : (luggage.getStatus() == 1) ? BUNDLE.getString("lost") : BUNDLE.getString("handled")});
 
             //System.out.println(user.getFirstName());
         }
     }
 
     /**
      * Searches through the customer database and loads the result into the
      * first customer table. This method takes the parameters and calls upon the
      * searchCustomerList method.
      *
      * @param dbField specifies the field to search in. Setting this parameter
      * to 0 searches all fields.
      * @param searchArg argument used to search the database.
      */
     public void searchCustomerTable1(int dbField, String searchArg) {
         modelCustomer1.setRowCount(0); //nodig voor 
         customers = query.searchCustomerList(dbField, searchArg);
         for (Customer customer : customers) {
             modelCustomer1.addRow(new Object[]{new Integer(customer.getCustomerId()), (customer.getResortId()== 0) ? BUNDLE.getString("unassigned")
                 : customer.getResortId(),
                 customer.getFirstName(),
                 customer.getLastName(),
                 customer.getAddress(),
                 customer.getPostalCode(),
                 customer.getCity(),
                 customer.getCountry(),
                 customer.getEmail(),
                 customer.getPhoneHome(),
                 customer.getPhoneMobile()
                 });
 
             //System.out.println(user.getFirstName());
         }
     }
 
     /**
      * Searches through the customer database and loads the result into the
      * first customer table. This method takes the parameters and calls upon the
      * searchCustomerList method.
      *
      * @param dbField specifies the field to search in. Setting this parameter
      * to 0 searches all fields.
      * @param searchArg argument used to search the database.
      */
     private void searchCustomerTable2(int dbField, String searchArg) {
         modelCustomer2.setRowCount(0); //nodig voor 
         customers = query.searchCustomerList(dbField, searchArg);
         for (Customer customer : customers) {
             modelCustomer2.addRow(new Object[]{new Integer(customer.getCustomerId()),
                 customer.getFirstName(),
                 customer.getLastName(),
                 customer.getAddress(),
                 customer.getPostalCode(),
                 customer.getCity(),
                 customer.getCountry(),
                 customer.getEmail(),
                 customer.getPhoneHome(),
                 customer.getPhoneMobile()});
         }
     }
     
     private void searchResortTable1(int dbfield, String searchArgs) {
         modelResort1.setRowCount(0);
         resorts = query.searchResortList(dbfield, searchArgs);
         for (Resort resort : resorts) {
             modelResort1.addRow(new Object[]{new Integer(resort.getId()),
                 resort.getName(),
                 resort.getCountry(),
                 resort.getCity(),
                 resort.getAddress(),
                 resort.getPostalCode(),
                 resort.getPhone(),
                 resort.getEmail()
 
             });
         }
 
     }
     
     private void searchResortTable(int dbField, String searchArgs) {
         modelResort.setRowCount(0);
         resorts = query.searchResortList(dbField, searchArgs);
         for (Resort resort : resorts) {
             modelResort.addRow(new Object[]{new Integer(resort.getId()),
                 resort.getName(),
                 resort.getCountry(),
                 resort.getCity(),
                 resort.getAddress(),
                 resort.getPostalCode(),
                 resort.getPhone(),
                 resort.getEmail()
 
             });
         }
 
     }
 
     /**
      * Method used to quickly clear all the text fields.
      */
     private void clearFields() {
         tfAddress1.setText("");
         tfAddress2.setText("");
         tfCity.setText("");
         tfEmail1.setText("");
         tfEmail2.setText("");
         tfFirstName.setText("");
         tfLastName.setText("");
         tfPhoneHome.setText("");
         tfPhoneMobile.setText("");
         tfPostalCode.setText("");
     }
 
     /**
      * Method that shows or hides handled luggage depending on the state of the
      * check box showHandledLuggage1
      *
      * @return int returns 0 when the checkbox is selected, otherwise returns 1
      */
     public int getShowHandled1() {
         if (showHandledLuggage1.isSelected()) {
             return 1;
         } else {
             return 0;
         }
     }
 
     /**
      * Method that shows or hides handled luggage depending on the state of the
      * check box showHandledLuggage1
      *
      * @return int returns 0 when the checkbox is selected, otherwise returns 1
      */
     public int getShowHandled2() {
         if (chbShowHandledLuggage2.isSelected()) {
             return 1;
         } else {
             return 0;
         }
     }
 
     /**
      * Method used to refresh the tables in the link luggage section
      */
     public void refreshLink() {
         searchCustomerTable1(cbSearchLuggage.getSelectedIndex(), customerSearchField.getText());
         searchLuggageTable1(cbSearchCustomer.getSelectedIndex(), luggageSearchField.getText(), getShowHandled1());
     }
 
     /**
      * Method used to create and display an error message used for
      * error-handling.
      *
      * @param errorMessage String message displayed on the pop-up.
      */
     private void errorPopUp(String errorMessage) {
         JOptionPane.showMessageDialog(ErrorPopUp, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
     }
 
     /**
      * Method for checking input errors, will return a boolean if all fields are
      * filled in correctly.
      *
      * @return boolean returns true if the user has made no errors
      */
     private boolean errorCheckCreateCustomer() {
         boolean[] correctInput = new boolean[6];
         boolean totalCorrectInput = false;
 
         String newFirstName = tfFirstName.getText().trim();
         String newLastName = tfLastName.getText().trim();
         String newAddress = tfAddress1.getText().trim() + " " + tfAddress2.getText().trim();
         String newPostalCode = tfPostalCode.getText().trim().toUpperCase();
         String newCity = tfCity.getText().trim();
         String newEmail = tfEmail1.getText().trim() + "@" + tfEmail2.getText().trim();
 
         if (newFirstName.equals("") || newFirstName.length() > 50) {
             correctInput[0] = false;
             errorPopUp("Vul een voornaam in.");
         } else {
             correctInput[0] = true;
         }
 
         if (newLastName.equals("") || newLastName.length() > 50) {
             correctInput[1] = false;
             errorPopUp("Vul een achternaam in.");
         } else {
             correctInput[1] = true;
         }
 
         if (newAddress.equals(" ") || tfAddress1.getText().equals("") || tfAddress2.getText().equals("")) {
             correctInput[2] = false;
             errorPopUp("Vul een adres in.");
         } else {
             correctInput[2] = true;
         }
 
         if (newPostalCode.equals("")) {
             correctInput[3] = false;
             errorPopUp("Vul een postcode in.");
 //        } else if (!tfPostalCode.getText().matches("[0-9]+")) {
 //            errorPopUp("Vul een geldige postcode in.");
 //            correctInput[3] = false;
         } else {
             correctInput[3] = true;
         }
 
         if (newCity.equals("")) {
             correctInput[4] = false;
             errorPopUp("Vul een stad in.");
         } else {
             correctInput[4] = true;
         }
 
         if (newEmail.length() > 75) {
             correctInput[5] = false;
             errorPopUp("Het gegeven email is te lang.");
         } else if (!tfEmail2.getText().contains(".")) {
             errorPopUp("Het gegeven email bevat geen punt.");
             correctInput[5] = false;
         } else {
             correctInput[5] = true;
         }
 
         for (int i = 0; i < correctInput.length; i++) {
             if (correctInput[i] == false) {
                 totalCorrectInput = false;
                break;
             } else {
                 totalCorrectInput = true;
             }
 
         }
         return totalCorrectInput;
     }
 
     /**
      * Method to display a yes-no pop-up that prompts the user to confirm their
      * earlier choice.
      *
      * @param message String the message the user will see.
      * @return
      */
     private boolean confirmationPopUp(String message) {
         boolean confirm = false;
         final JOptionPane createUserPopPane = new JOptionPane(message,
                 JOptionPane.WARNING_MESSAGE,
                 JOptionPane.YES_NO_OPTION);
         final JDialog dialog = new JDialog((Frame) confirmationPopUp, "Let op!", true);
         dialog.setContentPane(createUserPopPane);
         createUserPopPane.addPropertyChangeListener(
                 new PropertyChangeListener() {
                     public void propertyChange(PropertyChangeEvent e) {
                         String prop = e.getPropertyName();
 
                         if (dialog.isVisible()
                         && (e.getSource() == createUserPopPane)
                         && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
                             dialog.setVisible(false);
                         }
                     }
                 });
         dialog.pack();
         dialog.setVisible(true);
 
         int value = ((Integer) createUserPopPane.getValue()).intValue();
         if (value == JOptionPane.YES_OPTION) {
             confirm = true;
         }
         return confirm;
     }
 
     /**
      * Method pulls all the data from the textfields to create and insert a new
      * user into the database.
      */
     private void doCreateCustomer() {
 
         String newFirstName = tfFirstName.getText().trim();
         String newLastName = tfLastName.getText().trim();
         String newAddress = tfAddress1.getText().trim() + " " + tfAddress2.getText().trim();
         String newPostalCode = tfPostalCode.getText().trim().toUpperCase();
         String newCountry = tfCountry.getSelectedItem().toString();
         String newCity = tfCity.getText().trim();
         String newEmail = tfEmail1.getText().trim() + "@" + tfEmail2.getText().trim();
         String newPhoneHome = tfPhoneHome.getText().trim();
         String newPhoneMobile = tfPhoneMobile.getText().trim();
 
         query.setNewCustomer(newFirstName, newLastName,
                 newAddress, newPostalCode, newCity, newCountry, newEmail,
                 newPhoneHome, newPhoneMobile);
         clearFields();
         searchCustomerTable2(9999, "");
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         buttonGroup1 = new javax.swing.ButtonGroup();
         jPanel3 = new javax.swing.JPanel();
         overviewPane = new javax.swing.JTabbedPane();
         customer = new javax.swing.JPanel();
         customerRegistrationPanel = new javax.swing.JPanel();
         jLabel7 = new javax.swing.JLabel();
         tfAddress2 = new javax.swing.JTextField();
         tfAddress1 = new javax.swing.JTextField();
         jLabel12 = new javax.swing.JLabel();
         tfFirstName = new javax.swing.JTextField();
         jLabel13 = new javax.swing.JLabel();
         jLabel14 = new javax.swing.JLabel();
         tfLastName = new javax.swing.JTextField();
         jLabel15 = new javax.swing.JLabel();
         tfEmail1 = new javax.swing.JTextField();
         jLabel16 = new javax.swing.JLabel();
         tfEmail2 = new javax.swing.JTextField();
         tfPhoneHome = new javax.swing.JTextField();
         jLabel17 = new javax.swing.JLabel();
         tfPhoneMobile = new javax.swing.JTextField();
         jLabel18 = new javax.swing.JLabel();
         createCustomer1 = new javax.swing.JButton();
         jButton4 = new javax.swing.JButton();
         jLabel19 = new javax.swing.JLabel();
         jLabel20 = new javax.swing.JLabel();
         tfCity = new javax.swing.JTextField();
         tfCountry = new javax.swing.JComboBox();
         warningLabel1 = new javax.swing.JLabel();
         tfPostalCode = new javax.swing.JTextField();
         customerTablePanel = new javax.swing.JPanel();
         customerSearchField1 = new javax.swing.JTextField();
         customerSearchButton2 = new javax.swing.JButton();
         jScrollPane3 = new javax.swing.JScrollPane();
         customerTable2 = new javax.swing.JTable();
         cbSearchCustomer1 = new javax.swing.JComboBox();
         refreshCustomerTable2 = new javax.swing.JButton();
         customerOptionsPanel = new javax.swing.JPanel();
         jButton1 = new javax.swing.JButton();
         customerDeleteButton = new javax.swing.JButton();
         luggage = new javax.swing.JPanel();
         luggageRegistrationPanel = new javax.swing.JLayeredPane();
         lblCustomerID4 = new javax.swing.JLabel();
         tfCustomerID1 = new javax.swing.JTextField();
         lblCustomerID5 = new javax.swing.JLabel();
         lblCustomerID6 = new javax.swing.JLabel();
         tfLocation1 = new javax.swing.JTextField();
         lblCustomerID7 = new javax.swing.JLabel();
         btCreateLuggage = new javax.swing.JButton();
         btCancel = new javax.swing.JButton();
         jScrollPane2 = new javax.swing.JScrollPane();
         tfDescription = new javax.swing.JTextArea();
         cbStatus = new javax.swing.JComboBox();
         luggageTablePanel = new javax.swing.JPanel();
         luggageSearchField1 = new javax.swing.JTextField();
         luggageSearchButton2 = new javax.swing.JButton();
         jScrollPane7 = new javax.swing.JScrollPane();
         luggageTable2 = new javax.swing.JTable();
         cbSearchLuggage1 = new javax.swing.JComboBox();
         refreshLuggageTable2 = new javax.swing.JButton();
         chbShowHandledLuggage2 = new javax.swing.JCheckBox();
         luggageOptionsPanel = new javax.swing.JPanel();
         btChangeLuggage = new javax.swing.JButton();
         btPrintReceipt = new javax.swing.JButton();
         btDeleteLuggage = new javax.swing.JButton();
         linkLuggage = new javax.swing.JPanel();
         linkTableSplitter = new javax.swing.JSplitPane();
         linkCustomerTablePanel = new javax.swing.JPanel();
         customerSearchField = new javax.swing.JTextField();
         customerSearchButton1 = new javax.swing.JButton();
         jScrollPane1 = new javax.swing.JScrollPane();
         customerTable1 = new javax.swing.JTable();
         cbSearchCustomer = new javax.swing.JComboBox();
         jPanel8 = new javax.swing.JPanel();
         jTabbedPane1 = new javax.swing.JTabbedPane();
         linkLuggageTablePanel = new javax.swing.JPanel();
         luggageSearchField = new javax.swing.JTextField();
         luggageSearchButton1 = new javax.swing.JButton();
         jScrollPane6 = new javax.swing.JScrollPane();
         luggageTable1 = new javax.swing.JTable();
         cbSearchLuggage = new javax.swing.JComboBox();
         showHandledLuggage1 = new javax.swing.JCheckBox();
         jPanel9 = new javax.swing.JPanel();
         tfSearchResort1 = new javax.swing.JTextField();
         jScrollPane5 = new javax.swing.JScrollPane();
         tblResort1 = new javax.swing.JTable();
         buttonSearchResort1 = new javax.swing.JButton();
         cbResort1 = new javax.swing.JComboBox();
         linkOptionsPanel = new javax.swing.JPanel();
         linkButton = new javax.swing.JButton();
         refreshTables1 = new javax.swing.JButton();
         cancelButton = new javax.swing.JButton();
         btnLinkResort = new javax.swing.JButton();
         jPanel4 = new javax.swing.JPanel();
         jPanel1 = new javax.swing.JPanel();
         jPanel2 = new javax.swing.JPanel();
         jPanel5 = new javax.swing.JPanel();
         buttonUpdateResort = new javax.swing.JButton();
         buttonDeleteResort = new javax.swing.JButton();
         jPanel6 = new javax.swing.JPanel();
         btnCreateResort = new javax.swing.JButton();
         tfResortEmail2 = new javax.swing.JTextField();
         jLabel1 = new javax.swing.JLabel();
         tfResortEmail1 = new javax.swing.JTextField();
         jLabel8 = new javax.swing.JLabel();
         jLabel6 = new javax.swing.JLabel();
         jLabel5 = new javax.swing.JLabel();
         jLabel4 = new javax.swing.JLabel();
         jLabel3 = new javax.swing.JLabel();
         jLabel2 = new javax.swing.JLabel();
         lblNaam = new javax.swing.JLabel();
         tfResortName = new javax.swing.JTextField();
         tfResortCountry = new javax.swing.JTextField();
         tfResortCity = new javax.swing.JTextField();
         tfResortAddress = new javax.swing.JTextField();
         tfResortPostalCode = new javax.swing.JTextField();
         tfResortPhone = new javax.swing.JTextField();
         jPanel10 = new javax.swing.JPanel();
         tfSearchResort = new javax.swing.JTextField();
         jScrollPane4 = new javax.swing.JScrollPane();
         tblResort = new javax.swing.JTable();
         buttonSearchResort = new javax.swing.JButton();
         cbResort = new javax.swing.JComboBox();
         buttonRefresh = new javax.swing.JButton();
         menuBar = new javax.swing.JMenuBar();
         userMenu = new javax.swing.JMenu();
         changePassword = new javax.swing.JMenuItem();
         logout = new javax.swing.JMenuItem();
 
         javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
         jPanel3.setLayout(jPanel3Layout);
         jPanel3Layout.setHorizontalGroup(
             jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 100, Short.MAX_VALUE)
         );
         jPanel3Layout.setVerticalGroup(
             jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 100, Short.MAX_VALUE)
         );
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setTitle(BUNDLE.getString("employee") + " - " + Session.storedFirstName + " " + Session.storedLastName);
         setIconImage(getToolkit().getImage(getClass().getResource("/img/corendon.png")));
         setMinimumSize(new java.awt.Dimension(820, 460));
 
         overviewPane.setToolTipText("tooltip");
         overviewPane.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
 
         customerRegistrationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(BUNDLE.getString("registerCustomer")));
 
         jLabel7.setText(BUNDLE.getString("address"));
 
         tfAddress2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 tfAddress2ActionPerformed(evt);
             }
         });
         tfAddress2.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 tfAddress2KeyPressed(evt);
             }
         });
 
         tfAddress1.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 tfAddress1KeyPressed(evt);
             }
         });
 
         jLabel12.setText(BUNDLE.getString("postalCode"));
 
         tfFirstName.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 tfFirstNameActionPerformed(evt);
             }
         });
         tfFirstName.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 tfFirstNameKeyPressed(evt);
             }
         });
 
         jLabel13.setText(BUNDLE.getString("firstName"));
 
         jLabel14.setText(BUNDLE.getString("lastName"));
 
         tfLastName.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 tfLastNameKeyPressed(evt);
             }
         });
 
         jLabel15.setText(BUNDLE.getString("email"));
 
         tfEmail1.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 tfEmail1KeyPressed(evt);
             }
         });
 
         jLabel16.setText("@");
 
         tfEmail2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 tfEmail2ActionPerformed(evt);
             }
         });
         tfEmail2.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 tfEmail2KeyPressed(evt);
             }
         });
 
         tfPhoneHome.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 tfPhoneHomeKeyPressed(evt);
             }
         });
 
         jLabel17.setText(BUNDLE.getString("phoneHome"));
 
         tfPhoneMobile.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 tfPhoneMobileKeyPressed(evt);
             }
         });
 
         jLabel18.setText(BUNDLE.getString("phoneMobile"));
 
         createCustomer1.setText(BUNDLE.getString("add"));
         createCustomer1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 createCustomer1ActionPerformed(evt);
             }
         });
 
         jButton4.setText(BUNDLE.getString("cancel"));
         jButton4.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton4ActionPerformed(evt);
             }
         });
 
         jLabel19.setText(BUNDLE.getString("city"));
 
         jLabel20.setText(BUNDLE.getString("country"));
 
         tfCity.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 tfCityKeyPressed(evt);
             }
         });
 
         tfCountry.setModel(new javax.swing.DefaultComboBoxModel(BUNDLE.getStringArray("countriesComboBox")));
 
         warningLabel1.setForeground(new java.awt.Color(255, 0, 0));
 
         tfPostalCode.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 tfPostalCodeKeyPressed(evt);
             }
         });
 
         javax.swing.GroupLayout customerRegistrationPanelLayout = new javax.swing.GroupLayout(customerRegistrationPanel);
         customerRegistrationPanel.setLayout(customerRegistrationPanelLayout);
         customerRegistrationPanelLayout.setHorizontalGroup(
             customerRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(customerRegistrationPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(customerRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(warningLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, customerRegistrationPanelLayout.createSequentialGroup()
                         .addGap(0, 0, Short.MAX_VALUE)
                         .addComponent(jButton4)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(createCustomer1))
                     .addGroup(customerRegistrationPanelLayout.createSequentialGroup()
                         .addGroup(customerRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel15)
                             .addComponent(jLabel17)
                             .addComponent(jLabel18))
                         .addGroup(customerRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(customerRegistrationPanelLayout.createSequentialGroup()
                                 .addGap(138, 138, 138)
                                 .addComponent(jLabel16)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                 .addComponent(tfEmail2, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addGroup(customerRegistrationPanelLayout.createSequentialGroup()
                                 .addGap(39, 39, 39)
                                 .addGroup(customerRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addComponent(tfPhoneMobile)
                                     .addComponent(tfPhoneHome)))))
                     .addGroup(customerRegistrationPanelLayout.createSequentialGroup()
                         .addGroup(customerRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel13)
                             .addComponent(jLabel14)
                             .addComponent(jLabel12)
                             .addComponent(jLabel7)
                             .addComponent(jLabel20)
                             .addComponent(jLabel19))
                         .addGap(39, 39, 39)
                         .addGroup(customerRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(tfLastName)
                             .addComponent(tfFirstName, javax.swing.GroupLayout.Alignment.TRAILING)
                             .addGroup(customerRegistrationPanelLayout.createSequentialGroup()
                                 .addGroup(customerRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                     .addComponent(tfCountry, 0, 116, Short.MAX_VALUE)
                                     .addComponent(tfCity, javax.swing.GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE)
                                     .addComponent(tfEmail1, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addComponent(tfPostalCode))
                                 .addGap(0, 0, Short.MAX_VALUE))
                             .addGroup(customerRegistrationPanelLayout.createSequentialGroup()
                                 .addComponent(tfAddress1, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(tfAddress2)))))
                 .addContainerGap())
         );
         customerRegistrationPanelLayout.setVerticalGroup(
             customerRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(customerRegistrationPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(customerRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(tfFirstName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel13))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(customerRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel14)
                     .addComponent(tfLastName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(customerRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel7)
                     .addComponent(tfAddress1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(tfAddress2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(customerRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(tfPostalCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel12))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(customerRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(tfCity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel19))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(customerRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(tfCountry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel20))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(customerRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(customerRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(tfEmail2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(jLabel15))
                     .addGroup(customerRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(tfEmail1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(jLabel16)))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(customerRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabel17)
                     .addComponent(tfPhoneHome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(customerRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabel18)
                     .addComponent(tfPhoneMobile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(warningLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 17, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(customerRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(createCustomer1)
                     .addComponent(jButton4))
                 .addContainerGap())
         );
 
         customerTablePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(BUNDLE.getString("customer")));
 
         customerSearchField1.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 customerSearchField1KeyPressed(evt);
             }
         });
 
         customerSearchButton2.setText(BUNDLE.getString("search"));
         customerSearchButton2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 customerSearchButton2ActionPerformed(evt);
             }
         });
 
         customerTable2.setAutoCreateRowSorter(true);
         customerTable2.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             BUNDLE.getStringArray("customerTableFields")
         ) {
             Class[] types = new Class [] {
                 java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
             };
             boolean[] canEdit = new boolean [] {
                 false, false, false, false, false, false, false, false, false, false
             };
 
             public Class getColumnClass(int columnIndex) {
                 return types [columnIndex];
             }
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         jScrollPane3.setViewportView(customerTable2);
 
         cbSearchCustomer1.setMaximumRowCount(11);
         cbSearchCustomer1.setModel(new javax.swing.DefaultComboBoxModel(BUNDLE.getStringArray("customerSearchFields")));
 
         refreshCustomerTable2.setText(BUNDLE.getString("resetOverview"));
         refreshCustomerTable2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 refreshCustomerTable2ActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout customerTablePanelLayout = new javax.swing.GroupLayout(customerTablePanel);
         customerTablePanel.setLayout(customerTablePanelLayout);
         customerTablePanelLayout.setHorizontalGroup(
             customerTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(customerTablePanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(customerTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(customerTablePanelLayout.createSequentialGroup()
                         .addComponent(customerSearchField1, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(customerSearchButton2)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(cbSearchCustomer1, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(refreshCustomerTable2))
                     .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 998, Short.MAX_VALUE))
                 .addContainerGap())
         );
         customerTablePanelLayout.setVerticalGroup(
             customerTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(customerTablePanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(customerTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(customerSearchField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(customerSearchButton2)
                     .addComponent(cbSearchCustomer1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(refreshCustomerTable2))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 610, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         customerOptionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(BUNDLE.getString("options")));
 
         jButton1.setText(BUNDLE.getString("extensiveCustomerData"));
         jButton1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton1ActionPerformed(evt);
             }
         });
 
         customerDeleteButton.setText(BUNDLE.getString("deleteCustomer"));
         customerDeleteButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 customerDeleteButtonActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout customerOptionsPanelLayout = new javax.swing.GroupLayout(customerOptionsPanel);
         customerOptionsPanel.setLayout(customerOptionsPanelLayout);
         customerOptionsPanelLayout.setHorizontalGroup(
             customerOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(customerOptionsPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(customerOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(customerDeleteButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         customerOptionsPanelLayout.setVerticalGroup(
             customerOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(customerOptionsPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jButton1)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(customerDeleteButton)
                 .addContainerGap(30, Short.MAX_VALUE))
         );
 
         javax.swing.GroupLayout customerLayout = new javax.swing.GroupLayout(customer);
         customer.setLayout(customerLayout);
         customerLayout.setHorizontalGroup(
             customerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(customerLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(customerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(customerRegistrationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 344, Short.MAX_VALUE)
                     .addComponent(customerOptionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(customerTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addContainerGap())
         );
         customerLayout.setVerticalGroup(
             customerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(customerLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(customerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(customerLayout.createSequentialGroup()
                         .addComponent(customerTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addContainerGap())
                     .addGroup(customerLayout.createSequentialGroup()
                         .addComponent(customerRegistrationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(customerOptionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(0, 0, Short.MAX_VALUE))))
         );
 
         overviewPane.addTab(BUNDLE.getString("summaryCustomerRegister")
             , customer);
 
         luggageRegistrationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(BUNDLE.getString("registerLuggage")));
 
         lblCustomerID4.setText(BUNDLE.getString("customerId"));
 
         lblCustomerID5.setText(BUNDLE.getString("description"));
 
         lblCustomerID6.setText(BUNDLE.getString("location"));
 
         tfLocation1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 tfLocation1ActionPerformed(evt);
             }
         });
 
         lblCustomerID7.setText(BUNDLE.getString("status"));
 
         btCreateLuggage.setText(BUNDLE.getString("add"));
         btCreateLuggage.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btCreateLuggageActionPerformed(evt);
             }
         });
 
         btCancel.setText(BUNDLE.getString("cancel"));
         btCancel.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btCancelActionPerformed(evt);
             }
         });
 
         tfDescription.setColumns(20);
         tfDescription.setFont(new java.awt.Font("DialogInput", 0, 11)); // NOI18N
         tfDescription.setRows(5);
         jScrollPane2.setViewportView(tfDescription);
 
         cbStatus.setModel(new javax.swing.DefaultComboBoxModel(BUNDLE.getStringArray("statuses")));
 
         javax.swing.GroupLayout luggageRegistrationPanelLayout = new javax.swing.GroupLayout(luggageRegistrationPanel);
         luggageRegistrationPanel.setLayout(luggageRegistrationPanelLayout);
         luggageRegistrationPanelLayout.setHorizontalGroup(
             luggageRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(luggageRegistrationPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(luggageRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, luggageRegistrationPanelLayout.createSequentialGroup()
                         .addGap(0, 0, Short.MAX_VALUE)
                         .addComponent(btCancel)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(btCreateLuggage, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(luggageRegistrationPanelLayout.createSequentialGroup()
                         .addGroup(luggageRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(lblCustomerID4)
                             .addComponent(lblCustomerID5)
                             .addComponent(lblCustomerID6, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(lblCustomerID7, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(luggageRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
                             .addGroup(luggageRegistrationPanelLayout.createSequentialGroup()
                                 .addGroup(luggageRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addComponent(tfCustomerID1, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addGroup(luggageRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                         .addComponent(cbStatus, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                         .addComponent(tfLocation1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)))
                                 .addGap(0, 0, Short.MAX_VALUE)))))
                 .addContainerGap())
         );
         luggageRegistrationPanelLayout.setVerticalGroup(
             luggageRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(luggageRegistrationPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(luggageRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(lblCustomerID4)
                     .addComponent(tfCustomerID1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(luggageRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(luggageRegistrationPanelLayout.createSequentialGroup()
                         .addComponent(lblCustomerID5)
                         .addGap(0, 0, Short.MAX_VALUE))
                     .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(luggageRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(lblCustomerID6)
                     .addComponent(tfLocation1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(9, 9, 9)
                 .addGroup(luggageRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(lblCustomerID7)
                     .addComponent(cbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(55, 55, 55)
                 .addGroup(luggageRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(btCreateLuggage)
                     .addComponent(btCancel))
                 .addContainerGap())
         );
         luggageRegistrationPanel.setLayer(lblCustomerID4, javax.swing.JLayeredPane.DEFAULT_LAYER);
         luggageRegistrationPanel.setLayer(tfCustomerID1, javax.swing.JLayeredPane.DEFAULT_LAYER);
         luggageRegistrationPanel.setLayer(lblCustomerID5, javax.swing.JLayeredPane.DEFAULT_LAYER);
         luggageRegistrationPanel.setLayer(lblCustomerID6, javax.swing.JLayeredPane.DEFAULT_LAYER);
         luggageRegistrationPanel.setLayer(tfLocation1, javax.swing.JLayeredPane.DEFAULT_LAYER);
         luggageRegistrationPanel.setLayer(lblCustomerID7, javax.swing.JLayeredPane.DEFAULT_LAYER);
         luggageRegistrationPanel.setLayer(btCreateLuggage, javax.swing.JLayeredPane.DEFAULT_LAYER);
         luggageRegistrationPanel.setLayer(btCancel, javax.swing.JLayeredPane.DEFAULT_LAYER);
         luggageRegistrationPanel.setLayer(jScrollPane2, javax.swing.JLayeredPane.DEFAULT_LAYER);
         luggageRegistrationPanel.setLayer(cbStatus, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         luggageTablePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(BUNDLE.getString("luggage")));
 
         luggageSearchField1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 luggageSearchField1ActionPerformed(evt);
             }
         });
         luggageSearchField1.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 luggageSearchField1KeyPressed(evt);
             }
         });
 
         luggageSearchButton2.setText(BUNDLE.getString("search"));
         luggageSearchButton2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 luggageSearchButton2ActionPerformed(evt);
             }
         });
 
         luggageTable2.setAutoCreateRowSorter(true);
         luggageTable2.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             BUNDLE.getStringArray("luggageTableFields")
         ) {
             Class[] types = new Class [] {
                 java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
             };
             boolean[] canEdit = new boolean [] {
                 false, false, false, false, false, false
             };
 
             public Class getColumnClass(int columnIndex) {
                 return types [columnIndex];
             }
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         luggageTable2.setVerifyInputWhenFocusTarget(false);
         jScrollPane7.setViewportView(luggageTable2);
 
         cbSearchLuggage1.setModel(new javax.swing.DefaultComboBoxModel(BUNDLE.getStringArray("luggageSearchFields")));
         cbSearchLuggage1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cbSearchLuggage1ActionPerformed(evt);
             }
         });
 
         refreshLuggageTable2.setText(BUNDLE.getString("resetOverview"));
         refreshLuggageTable2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 refreshLuggageTable2ActionPerformed(evt);
             }
         });
 
         chbShowHandledLuggage2.setText(BUNDLE.getString("hideHandled"));
         chbShowHandledLuggage2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 chbShowHandledLuggage2ActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout luggageTablePanelLayout = new javax.swing.GroupLayout(luggageTablePanel);
         luggageTablePanel.setLayout(luggageTablePanelLayout);
         luggageTablePanelLayout.setHorizontalGroup(
             luggageTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(luggageTablePanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(luggageTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(luggageTablePanelLayout.createSequentialGroup()
                         .addComponent(luggageSearchField1, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(luggageSearchButton2)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(cbSearchLuggage1, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(chbShowHandledLuggage2)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 386, Short.MAX_VALUE)
                         .addComponent(refreshLuggageTable2))
                     .addComponent(jScrollPane7))
                 .addContainerGap())
         );
         luggageTablePanelLayout.setVerticalGroup(
             luggageTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(luggageTablePanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(luggageTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addGroup(luggageTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(luggageSearchField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(cbSearchLuggage1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(refreshLuggageTable2)
                         .addComponent(chbShowHandledLuggage2))
                     .addComponent(luggageSearchButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 610, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         luggageOptionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(BUNDLE.getString("options")));
 
         btChangeLuggage.setText(BUNDLE.getString("modifyLuggage"));
         btChangeLuggage.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btChangeLuggageActionPerformed(evt);
             }
         });
 
         btPrintReceipt.setText(BUNDLE.getString("proofPrint"));
         btPrintReceipt.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btPrintReceiptActionPerformed(evt);
             }
         });
 
         btDeleteLuggage.setText(BUNDLE.getString("deleteLuggage"));
         btDeleteLuggage.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btDeleteLuggageActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout luggageOptionsPanelLayout = new javax.swing.GroupLayout(luggageOptionsPanel);
         luggageOptionsPanel.setLayout(luggageOptionsPanelLayout);
         luggageOptionsPanelLayout.setHorizontalGroup(
             luggageOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(luggageOptionsPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(luggageOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(btChangeLuggage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(btPrintReceipt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(btDeleteLuggage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         luggageOptionsPanelLayout.setVerticalGroup(
             luggageOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(luggageOptionsPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(btChangeLuggage)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btPrintReceipt)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btDeleteLuggage)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         javax.swing.GroupLayout luggageLayout = new javax.swing.GroupLayout(luggage);
         luggage.setLayout(luggageLayout);
         luggageLayout.setHorizontalGroup(
             luggageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(luggageLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(luggageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(luggageRegistrationPanel)
                     .addComponent(luggageOptionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(luggageTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addContainerGap())
         );
         luggageLayout.setVerticalGroup(
             luggageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(luggageLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(luggageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(luggageTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addGroup(luggageLayout.createSequentialGroup()
                         .addComponent(luggageRegistrationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(luggageOptionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(0, 0, Short.MAX_VALUE)))
                 .addContainerGap())
         );
 
         overviewPane.addTab(BUNDLE.getString("summaryLuggageRegister")
             , luggage);
 
         linkLuggage.setPreferredSize(new java.awt.Dimension(1024, 768));
 
         linkTableSplitter.setBorder(javax.swing.BorderFactory.createTitledBorder(BUNDLE.getString("summary")));
         linkTableSplitter.setDividerLocation(600);
 
         linkCustomerTablePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(BUNDLE.getString("customer")));
 
         customerSearchField.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 customerSearchFieldKeyPressed(evt);
             }
         });
 
         customerSearchButton1.setText(BUNDLE.getString("search"));
         customerSearchButton1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 customerSearchButton1ActionPerformed(evt);
             }
         });
 
         customerTable1.setAutoCreateRowSorter(true);
         customerTable1.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             BUNDLE.getStringArray("customerTableFieldsMin")
         ) {
             Class[] types = new Class [] {
                 java.lang.Integer.class,java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
             };
             boolean[] canEdit = new boolean [] {
                 false,false, false, false, false, false, false, false, false, false
             };
 
             public Class getColumnClass(int columnIndex) {
                 return types [columnIndex];
             }
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         customerTable1.setDragEnabled(true);
         jScrollPane1.setViewportView(customerTable1);
 
         cbSearchCustomer.setMaximumRowCount(11);
         cbSearchCustomer.setModel(new javax.swing.DefaultComboBoxModel(BUNDLE.getStringArray("customerSearchFieldsMin")));
 
         javax.swing.GroupLayout linkCustomerTablePanelLayout = new javax.swing.GroupLayout(linkCustomerTablePanel);
         linkCustomerTablePanel.setLayout(linkCustomerTablePanelLayout);
         linkCustomerTablePanelLayout.setHorizontalGroup(
             linkCustomerTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(linkCustomerTablePanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(linkCustomerTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(linkCustomerTablePanelLayout.createSequentialGroup()
                         .addComponent(customerSearchField, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(customerSearchButton1)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(cbSearchCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(0, 0, Short.MAX_VALUE))
                     .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                 .addContainerGap())
         );
         linkCustomerTablePanelLayout.setVerticalGroup(
             linkCustomerTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(linkCustomerTablePanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(linkCustomerTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(customerSearchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(customerSearchButton1)
                     .addComponent(cbSearchCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 566, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         linkTableSplitter.setRightComponent(linkCustomerTablePanel);
 
         jPanel8.setMinimumSize(new java.awt.Dimension(400, 400));
 
         jTabbedPane1.setMinimumSize(new java.awt.Dimension(550, 48));
         jTabbedPane1.setName(""); // NOI18N
 
         luggageSearchField.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 luggageSearchFieldActionPerformed(evt);
             }
         });
         luggageSearchField.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 luggageSearchFieldKeyPressed(evt);
             }
         });
 
         luggageSearchButton1.setText(BUNDLE.getString("search"));
         luggageSearchButton1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 luggageSearchButton1ActionPerformed(evt);
             }
         });
 
         luggageTable1.setAutoCreateRowSorter(true);
         luggageTable1.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             BUNDLE.getStringArray("luggageTableFields")
         ) {
             Class[] types = new Class [] {
                 java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
             };
             boolean[] canEdit = new boolean [] {
                 false, false, false, false, false, false
             };
 
             public Class getColumnClass(int columnIndex) {
                 return types [columnIndex];
             }
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         luggageTable1.setVerifyInputWhenFocusTarget(false);
         jScrollPane6.setViewportView(luggageTable1);
 
         cbSearchLuggage.setModel(new javax.swing.DefaultComboBoxModel(BUNDLE.getStringArray("luggageSearchFields")));
 
         showHandledLuggage1.setText(BUNDLE.getString("hideHandled"));
         showHandledLuggage1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 showHandledLuggage1ActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout linkLuggageTablePanelLayout = new javax.swing.GroupLayout(linkLuggageTablePanel);
         linkLuggageTablePanel.setLayout(linkLuggageTablePanelLayout);
         linkLuggageTablePanelLayout.setHorizontalGroup(
             linkLuggageTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(linkLuggageTablePanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(linkLuggageTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(linkLuggageTablePanelLayout.createSequentialGroup()
                         .addComponent(luggageSearchField, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(luggageSearchButton1)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(cbSearchLuggage, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(showHandledLuggage1))
                     .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 570, Short.MAX_VALUE))
                 .addContainerGap())
         );
         linkLuggageTablePanelLayout.setVerticalGroup(
             linkLuggageTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(linkLuggageTablePanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(linkLuggageTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(luggageSearchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(luggageSearchButton1)
                     .addComponent(cbSearchLuggage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(showHandledLuggage1))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 539, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         jTabbedPane1.addTab(BUNDLE.getString("luggage")
             , linkLuggageTablePanel);
 
         tblResort1.setAutoCreateRowSorter(true);
         tblResort1.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             BUNDLE.getStringArray("resortTableTitles")
         ));
         jScrollPane5.setViewportView(tblResort1);
 
         buttonSearchResort1.setText(BUNDLE.getString("search"));
         buttonSearchResort1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 buttonSearchResort1ActionPerformed(evt);
             }
         });
 
         cbResort1.setModel(new javax.swing.DefaultComboBoxModel(BUNDLE.getStringArray("resortTableAllFieldsLink")));
 
         javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
         jPanel9.setLayout(jPanel9Layout);
         jPanel9Layout.setHorizontalGroup(
             jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel9Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane5)
                     .addGroup(jPanel9Layout.createSequentialGroup()
                         .addComponent(tfSearchResort1, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(buttonSearchResort1)
                         .addGap(6, 6, 6)
                         .addComponent(cbResort1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(0, 309, Short.MAX_VALUE)))
                 .addContainerGap())
         );
         jPanel9Layout.setVerticalGroup(
             jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel9Layout.createSequentialGroup()
                 .addGap(16, 16, 16)
                 .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(cbResort1, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(buttonSearchResort1)
                         .addComponent(tfSearchResort1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 535, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         jTabbedPane1.addTab(BUNDLE.getString("resort"), jPanel9);
 
         javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
         jPanel8.setLayout(jPanel8Layout);
         jPanel8Layout.setHorizontalGroup(
             jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         jPanel8Layout.setVerticalGroup(
             jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
 
         jTabbedPane1.getAccessibleContext().setAccessibleName(BUNDLE.getString("luggage"));
         jTabbedPane1.getAccessibleContext().setAccessibleDescription("Aapje");
 
         linkTableSplitter.setLeftComponent(jPanel8);
 
         linkOptionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(BUNDLE.getString("options")));
 
         linkButton.setText(BUNDLE.getString("connect"));
         linkButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 linkButtonActionPerformed(evt);
             }
         });
 
         refreshTables1.setText(BUNDLE.getString("resetOverview"));
         refreshTables1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 refreshTables1ActionPerformed(evt);
             }
         });
 
         cancelButton.setText(BUNDLE.getString("cancel"));
         cancelButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cancelButtonActionPerformed(evt);
             }
         });
 
         btnLinkResort.setText("Verblijf koppelen");
         btnLinkResort.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnLinkResortActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout linkOptionsPanelLayout = new javax.swing.GroupLayout(linkOptionsPanel);
         linkOptionsPanel.setLayout(linkOptionsPanelLayout);
         linkOptionsPanelLayout.setHorizontalGroup(
             linkOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, linkOptionsPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(refreshTables1)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addComponent(cancelButton)
                 .addGap(33, 33, 33)
                 .addComponent(btnLinkResort)
                 .addGap(18, 18, 18)
                 .addComponent(linkButton)
                 .addContainerGap())
         );
         linkOptionsPanelLayout.setVerticalGroup(
             linkOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(linkOptionsPanelLayout.createSequentialGroup()
                 .addGroup(linkOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(linkButton)
                     .addComponent(refreshTables1)
                     .addComponent(cancelButton)
                     .addComponent(btnLinkResort))
                 .addGap(0, 11, Short.MAX_VALUE))
         );
 
         javax.swing.GroupLayout linkLuggageLayout = new javax.swing.GroupLayout(linkLuggage);
         linkLuggage.setLayout(linkLuggageLayout);
         linkLuggageLayout.setHorizontalGroup(
             linkLuggageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(linkLuggageLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(linkLuggageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(linkTableSplitter, javax.swing.GroupLayout.DEFAULT_SIZE, 1431, Short.MAX_VALUE)
                     .addComponent(linkOptionsPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
         linkLuggageLayout.setVerticalGroup(
             linkLuggageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(linkLuggageLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(linkTableSplitter)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(linkOptionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
 
         linkTableSplitter.getAccessibleContext().setAccessibleName("BUNDLE.getString(\"overviews\")");
 
         overviewPane.addTab(BUNDLE.getString("linkCustomer"), linkLuggage);
 
         jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(BUNDLE.getString("newResort")));
 
         jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(BUNDLE.getString("options")));
 
         buttonUpdateResort.setText(BUNDLE.getString("updateResort"));
         buttonUpdateResort.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 buttonUpdateResortActionPerformed(evt);
             }
         });
 
         buttonDeleteResort.setText(BUNDLE.getString("deleteResort"));
         buttonDeleteResort.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 buttonDeleteResortActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
         jPanel5.setLayout(jPanel5Layout);
         jPanel5Layout.setHorizontalGroup(
             jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel5Layout.createSequentialGroup()
                 .addGap(24, 24, 24)
                 .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(buttonDeleteResort, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(buttonUpdateResort, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         jPanel5Layout.setVerticalGroup(
             jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel5Layout.createSequentialGroup()
                 .addGap(21, 21, 21)
                 .addComponent(buttonUpdateResort)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(buttonDeleteResort)
                 .addContainerGap(32, Short.MAX_VALUE))
         );
 
         jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(BUNDLE.getString("createResort")));
 
         btnCreateResort.setText(BUNDLE.getString("add"));
         btnCreateResort.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnCreateResortActionPerformed(evt);
             }
         });
 
         jLabel1.setText("@");
 
         jLabel8.setText(BUNDLE.getString("email"));
 
         jLabel6.setText(BUNDLE.getString("phoneNumber"));
 
         jLabel5.setText(BUNDLE.getString("postalCode"));
 
         jLabel4.setText(BUNDLE.getString("address"));
 
         jLabel3.setText(BUNDLE.getString("city"));
 
         jLabel2.setText(BUNDLE.getString("country"));
 
         lblNaam.setText(BUNDLE.getString("name"));
 
         javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
         jPanel6.setLayout(jPanel6Layout);
         jPanel6Layout.setHorizontalGroup(
             jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel6Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel6Layout.createSequentialGroup()
                         .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel5)
                             .addComponent(jLabel6)
                             .addComponent(jLabel2)
                             .addComponent(lblNaam)
                             .addComponent(jLabel4)
                             .addComponent(jLabel8)
                             .addComponent(jLabel3))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(tfResortCountry, javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(tfResortName)
                             .addComponent(tfResortCity, javax.swing.GroupLayout.Alignment.TRAILING)
                             .addGroup(jPanel6Layout.createSequentialGroup()
                                 .addComponent(tfResortEmail1, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(jLabel1)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(tfResortEmail2, javax.swing.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE))
                             .addComponent(tfResortPhone)
                             .addGroup(jPanel6Layout.createSequentialGroup()
                                 .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addComponent(tfResortAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addComponent(tfResortPostalCode, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE))
                                 .addGap(0, 0, Short.MAX_VALUE))))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                         .addGap(0, 0, Short.MAX_VALUE)
                         .addComponent(btnCreateResort)))
                 .addContainerGap())
         );
 
         jPanel6Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {tfResortAddress, tfResortEmail1, tfResortPostalCode});
 
         jPanel6Layout.setVerticalGroup(
             jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel6Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(lblNaam)
                     .addComponent(tfResortName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel2)
                     .addComponent(tfResortCountry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel3)
                     .addComponent(tfResortCity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel4)
                     .addComponent(tfResortAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel5)
                     .addComponent(tfResortPostalCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel6)
                     .addComponent(tfResortPhone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel8)
                     .addComponent(tfResortEmail1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(tfResortEmail2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel1))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
                 .addComponent(btnCreateResort)
                 .addContainerGap())
         );
 
         jPanel6Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {tfResortAddress, tfResortCity, tfResortCountry, tfResortEmail1, tfResortName, tfResortPhone, tfResortPostalCode});
 
         javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
         jPanel2.setLayout(jPanel2Layout);
         jPanel2Layout.setHorizontalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel2Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                     .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         jPanel2Layout.setVerticalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel2Layout.createSequentialGroup()
                 .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder(BUNDLE.getString("summaryResort")));
 
         tblResort.setAutoCreateRowSorter(true);
         tblResort.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             BUNDLE.getStringArray("resortTableTitles")
         ));
         jScrollPane4.setViewportView(tblResort);
 
         buttonSearchResort.setText(BUNDLE.getString("search"));
         buttonSearchResort.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 buttonSearchResortActionPerformed(evt);
             }
         });
 
         cbResort.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Alle velden", "Naam", "Adres", "Land", "Stad", "Telefoon", "Email", "Postcode" }));
 
         buttonRefresh.setText(BUNDLE.getString("resetOverview"));
 
         javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
         jPanel10.setLayout(jPanel10Layout);
         jPanel10Layout.setHorizontalGroup(
             jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel10Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 1019, Short.MAX_VALUE)
                     .addGroup(jPanel10Layout.createSequentialGroup()
                         .addComponent(tfSearchResort, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(buttonSearchResort)
                         .addGap(6, 6, 6)
                         .addComponent(cbResort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(buttonRefresh)))
                 .addContainerGap())
         );
         jPanel10Layout.setVerticalGroup(
             jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel10Layout.createSequentialGroup()
                 .addGap(10, 10, 10)
                 .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(cbResort, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(buttonRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(buttonSearchResort)
                         .addComponent(tfSearchResort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 606, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addContainerGap())
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
 
         javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
         jPanel4.setLayout(jPanel4Layout);
         jPanel4Layout.setHorizontalGroup(
             jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         jPanel4Layout.setVerticalGroup(
             jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
 
         overviewPane.addTab(BUNDLE.getString("resortTab"), jPanel4);
 
         userMenu.setText(BUNDLE.getString("options"));
 
         changePassword.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/changePassword.png"))); // NOI18N
         changePassword.setText(BUNDLE.getString("changePassword"));
         changePassword.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 changePasswordActionPerformed(evt);
             }
         });
         userMenu.add(changePassword);
 
         logout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/logout.png"))); // NOI18N
         logout.setText(BUNDLE.getString("logout"));
         logout.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 logoutActionPerformed(evt);
             }
         });
         userMenu.add(logout);
 
         menuBar.add(userMenu);
 
         setJMenuBar(menuBar);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(overviewPane)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(overviewPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 715, Short.MAX_VALUE)
         );
 
         overviewPane.getAccessibleContext().setAccessibleName("Tabbed Pane");
 
         pack();
         setLocationRelativeTo(null);
     }// </editor-fold>//GEN-END:initComponents
 
     private void changePasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changePasswordActionPerformed
         Main.displayChangePassword();
     }//GEN-LAST:event_changePasswordActionPerformed
 
     private void logoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logoutActionPerformed
         dispose();
         Main.displayLogin();
     }//GEN-LAST:event_logoutActionPerformed
 
     private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
         dispose();
     }//GEN-LAST:event_cancelButtonActionPerformed
 
     private void refreshTables1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshTables1ActionPerformed
         searchCustomerTable1(9999, "");
         searchLuggageTable1(9999, "", getShowHandled1());
         //        updateUserTable();
     }//GEN-LAST:event_refreshTables1ActionPerformed
 
     private void tfLocation1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfLocation1ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_tfLocation1ActionPerformed
 
     private void tfAddress2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfAddress2ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_tfAddress2ActionPerformed
 
     private void tfFirstNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfFirstNameActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_tfFirstNameActionPerformed
 
     private void tfEmail2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfEmail2ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_tfEmail2ActionPerformed
 
     private void createCustomer1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createCustomer1ActionPerformed
         boolean totalCorrectInput = errorCheckCreateCustomer();
         if (totalCorrectInput == true) {
             doCreateCustomer();
         } else {
             errorPopUp("Vul alle velden volledig in en probeer het nog eens.");
         }
     }//GEN-LAST:event_createCustomer1ActionPerformed
 
     private void refreshCustomerTable2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshCustomerTable2ActionPerformed
         searchCustomerTable2(9999, "");
     }//GEN-LAST:event_refreshCustomerTable2ActionPerformed
 
     private void refreshLuggageTable2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshLuggageTable2ActionPerformed
         searchLuggageTable2(9999, "", getShowHandled2());
     }//GEN-LAST:event_refreshLuggageTable2ActionPerformed
 
     private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
         clearFields();
     }//GEN-LAST:event_jButton4ActionPerformed
 
     private void btChangeLuggageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btChangeLuggageActionPerformed
         boolean isError = false;
         try {
             Session.storedLuggageId = luggageTable2.getValueAt(luggageTable2.getSelectedRow(), 0).toString();
         } catch (IndexOutOfBoundsException e) {
             errorPopUp("Maak een selectie in de tabel en probeer het nog eens.");
             isError = true;
         }
         if (isError == false) {
             Main.displayChangeLuggage();
         }
     }//GEN-LAST:event_btChangeLuggageActionPerformed
 
     private void linkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_linkButtonActionPerformed
         boolean confirmation;
         isLinked = true;
         try {
             customerToLink = customerTable1.getValueAt(customerTable1.getSelectedRow(), 0).toString();
             luggageToLink = luggageTable1.getValueAt(luggageTable1.getSelectedRow(), 0).toString();
             String customerFirstName = customerTable1.getValueAt(customerTable1.getSelectedRow(), 1).toString() + " ";
             String customerLastName = customerTable1.getValueAt(customerTable1.getSelectedRow(), 2).toString();
             customerFullName = customerFirstName + customerLastName;
         } catch (IndexOutOfBoundsException e) {
             JOptionPane.showMessageDialog(ErrorPopUp,
                     "Maak een selectie in de tabel en probeer het nog eens.");
             isLinked = false;
         }
         boolean isError = false;
 
         customerId = Integer.parseInt(customerToLink);
         luggageId = Integer.parseInt(luggageToLink);
         String message = "Weet u zeker dat u klant: " + customerFullName + "\n"
                 + "Wilt koppelen aan baggagestuk: " + luggageId;
 
         confirmation = confirmationPopUp(message);
         if (confirmation == true) {
             Luggage luggage = new Luggage();
             query.linkCustomerId(customerId, luggageId);
             searchCustomerTable1(cbSearchLuggage.getSelectedIndex(), customerSearchField.getText());
             searchLuggageTable1(cbSearchCustomer.getSelectedIndex(), luggageSearchField.getText(), getShowHandled1());
         }
 
     }//GEN-LAST:event_linkButtonActionPerformed
 
     private void customerSearchButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customerSearchButton2ActionPerformed
         int searchField = cbSearchCustomer1.getSelectedIndex();
         searchCustomerTable2(searchField, customerSearchField1.getText().trim());
     }//GEN-LAST:event_customerSearchButton2ActionPerformed
 
     private void customerSearchButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customerSearchButton1ActionPerformed
         int searchField = cbSearchCustomer.getSelectedIndex();
         searchCustomerTable1(searchField, customerSearchField.getText().trim());
     }//GEN-LAST:event_customerSearchButton1ActionPerformed
 
     private void luggageSearchButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_luggageSearchButton1ActionPerformed
         int searchField = cbSearchLuggage.getSelectedIndex();
         searchLuggageTable1(searchField, luggageSearchField.getText().trim(), getShowHandled1());
     }//GEN-LAST:event_luggageSearchButton1ActionPerformed
 
     private void luggageSearchButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_luggageSearchButton2ActionPerformed
         int searchField = cbSearchLuggage1.getSelectedIndex();
         searchLuggageTable2(searchField, luggageSearchField1.getText().trim(), getShowHandled2());
     }//GEN-LAST:event_luggageSearchButton2ActionPerformed
 
     private void btCreateLuggageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCreateLuggageActionPerformed
         Luggage luggage = new Luggage();
         boolean[] correctInput = new boolean[3];
         boolean totalCorrectInput = false;
         int status;
         String customerId;
         int storedUserId;
 
         String description = tfDescription.getText().trim();
         if (description.equals("")) {
             errorPopUp("Voer een omschrijving in en probeer het nog eens.");
             correctInput[0] = false;
         } else {
             correctInput[0] = true;
         }
 
         String location = tfLocation1.getText().trim();
         if (location.equals("")) {
             errorPopUp("Voer een locatie in en probeer het nog eens.");
             correctInput[1] = false;
         } else {
             correctInput[1] = true;
         }
 
         customerId = tfCustomerID1.getText().trim();
         if (!customerId.matches("^[0-9]+$") && !customerId.equals("")) {
             errorPopUp("Klant ID kan alleen nummers bevatten!");
             correctInput[2] = false;
         } else {
             correctInput[2] = true;
         }
 
         status = cbStatus.getSelectedIndex() + 1;
 
         for (int i = 0; i < correctInput.length; i++) {
             if (correctInput[i] == false) {
                 totalCorrectInput = false;
 
                 break;
             } else {
                 totalCorrectInput = true;
             }
         }
 
         System.out.println(totalCorrectInput);
         if (totalCorrectInput) {
             query.createLuggage(customerId, description, location, status);
             tfCustomerID1.setText("");
             tfDescription.setText("");
             tfLocation1.setText("");
             searchLuggageTable2(9999, "", getShowHandled2());
         }
         // TODO add your handling code here:
     }//GEN-LAST:event_btCreateLuggageActionPerformed
 
     private void customerSearchField1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_customerSearchField1KeyPressed
         if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
             System.out.println("Enter pressed");
             int searchField = cbSearchCustomer1.getSelectedIndex();
             searchCustomerTable2(searchField, customerSearchField1.getText().trim());
         }
     }//GEN-LAST:event_customerSearchField1KeyPressed
 
     private void luggageSearchField1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_luggageSearchField1KeyPressed
         if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
             int searchField = cbSearchLuggage1.getSelectedIndex();
             searchLuggageTable2(searchField, luggageSearchField1.getText().trim(), getShowHandled2());
         }
     }//GEN-LAST:event_luggageSearchField1KeyPressed
 
     private void luggageSearchFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_luggageSearchFieldKeyPressed
         if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
             int searchField = cbSearchLuggage.getSelectedIndex();
             searchLuggageTable1(searchField, luggageSearchField.getText().trim(), getShowHandled1());
         }
     }//GEN-LAST:event_luggageSearchFieldKeyPressed
 
     private void customerSearchFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_customerSearchFieldKeyPressed
         if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
             int searchField = cbSearchCustomer.getSelectedIndex();
             searchCustomerTable1(searchField, customerSearchField.getText().trim());
         }
     }//GEN-LAST:event_customerSearchFieldKeyPressed
 
     private void luggageSearchField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_luggageSearchField1ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_luggageSearchField1ActionPerformed
 
     private void luggageSearchFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_luggageSearchFieldActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_luggageSearchFieldActionPerformed
 
     private void cbSearchLuggage1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbSearchLuggage1ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_cbSearchLuggage1ActionPerformed
 
     private void chbShowHandledLuggage2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chbShowHandledLuggage2ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_chbShowHandledLuggage2ActionPerformed
 
     private void showHandledLuggage1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showHandledLuggage1ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_showHandledLuggage1ActionPerformed
 
     private void tfFirstNameKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfFirstNameKeyPressed
         if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
             boolean totalCorrectInput = errorCheckCreateCustomer();
             if (totalCorrectInput == true) {
                 doCreateCustomer();
             } else {
                 errorPopUp("Vul alle velden volledig in en probeer het nog eens.");
             }
         }
     }//GEN-LAST:event_tfFirstNameKeyPressed
 
     private void tfLastNameKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfLastNameKeyPressed
         if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
             boolean totalCorrectInput = errorCheckCreateCustomer();
             if (totalCorrectInput == true) {
                 doCreateCustomer();
             } else {
                 errorPopUp("Vul alle velden volledig in en probeer het nog eens.");
             }
         }
     }//GEN-LAST:event_tfLastNameKeyPressed
 
     private void tfAddress1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfAddress1KeyPressed
         if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
             boolean totalCorrectInput = errorCheckCreateCustomer();
             if (totalCorrectInput == true) {
                 doCreateCustomer();
             } else {
                 errorPopUp("Vul alle velden volledig in en probeer het nog eens.");
             }
         }
     }//GEN-LAST:event_tfAddress1KeyPressed
 
     private void tfAddress2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfAddress2KeyPressed
         if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
             boolean totalCorrectInput = errorCheckCreateCustomer();
             if (totalCorrectInput == true) {
                 doCreateCustomer();
             } else {
                 errorPopUp("Vul alle velden volledig in en probeer het nog eens.");
             }
         }
     }//GEN-LAST:event_tfAddress2KeyPressed
 
     private void tfPostalCodeKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfPostalCodeKeyPressed
         if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
             boolean totalCorrectInput = errorCheckCreateCustomer();
             if (totalCorrectInput == true) {
                 doCreateCustomer();
             } else {
                 errorPopUp("Vul alle velden volledig in en probeer het nog eens.");
             }
         }
     }//GEN-LAST:event_tfPostalCodeKeyPressed
 
     private void tfCityKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfCityKeyPressed
         if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
             boolean totalCorrectInput = errorCheckCreateCustomer();
             if (totalCorrectInput == true) {
                 doCreateCustomer();
             } else {
                 errorPopUp("Vul alle velden volledig in en probeer het nog eens.");
             }
         }
     }//GEN-LAST:event_tfCityKeyPressed
 
     private void tfEmail1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfEmail1KeyPressed
         if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
             boolean totalCorrectInput = errorCheckCreateCustomer();
             if (totalCorrectInput == true) {
                 doCreateCustomer();
             } else {
                 errorPopUp("Vul alle velden volledig in en probeer het nog eens.");
             }
         }
     }//GEN-LAST:event_tfEmail1KeyPressed
 
     private void tfEmail2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfEmail2KeyPressed
         if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
             boolean totalCorrectInput = errorCheckCreateCustomer();
             if (totalCorrectInput == true) {
                 doCreateCustomer();
             } else {
                 errorPopUp("Vul alle velden volledig in en probeer het nog eens.");
             }
         }
     }//GEN-LAST:event_tfEmail2KeyPressed
 
     private void tfPhoneHomeKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfPhoneHomeKeyPressed
         if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
             boolean totalCorrectInput = errorCheckCreateCustomer();
             if (totalCorrectInput == true) {
                 doCreateCustomer();
             } else {
                 errorPopUp("Vul alle velden volledig in en probeer het nog eens.");
             }
         }
     }//GEN-LAST:event_tfPhoneHomeKeyPressed
 
     private void tfPhoneMobileKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfPhoneMobileKeyPressed
         if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
             boolean totalCorrectInput = errorCheckCreateCustomer();
             if (totalCorrectInput == true) {
                 doCreateCustomer();
             } else {
                 errorPopUp("Vul alle velden volledig in en probeer het nog eens.");
             }
         }
     }//GEN-LAST:event_tfPhoneMobileKeyPressed
 
     private void customerDeleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customerDeleteButtonActionPerformed
         boolean isError = false;
         boolean confirmation = false;
         Customer customer = new Customer();
         try {
             String customerFirstName = customerTable2.getValueAt(customerTable2.getSelectedRow(), 1).toString() + " ";
             String customerLastName = customerTable2.getValueAt(customerTable2.getSelectedRow(), 2).toString();
             customerFullName = customerFirstName + customerLastName;
         } catch (IndexOutOfBoundsException e) {
             errorPopUp("Maak een selectie in de tabel en probeer het nog eens.");
             isError = true;
         }
         if (isError == false) {
             String message = "Weet u zeker dat u klant: " + customerFullName + " wilt verwijderen?";
             confirmation = confirmationPopUp(message);
             if (confirmation == true) {
                 String customerID = customerTable2.getValueAt(customerTable2.getSelectedRow(), 0).toString();
                 query.deleteCustomer(customerID);
                 searchCustomerTable2(9999, "");
             }
         }
     }//GEN-LAST:event_customerDeleteButtonActionPerformed
 
     private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
         boolean isError = false;
         try {
             Session.storedCustomerId = customerTable2.getValueAt(customerTable2.getSelectedRow(), 0).toString();
         } catch (IndexOutOfBoundsException e) {
             errorPopUp("Maak een selectie in de tabel en probeer het nog eens.");
             isError = true;
         }
         if (isError == false) {
             Main.displayExtendedCustomer();
 
         }
 
     }//GEN-LAST:event_jButton1ActionPerformed
 
     private void btDeleteLuggageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btDeleteLuggageActionPerformed
         boolean isError = false;
         boolean confirmation = false;
         Luggage luggage = new Luggage();
 
         try {
             String luggageId = luggageTable2.getValueAt(luggageTable2.getSelectedRow(), 0).toString();
         } catch (IndexOutOfBoundsException e) {
             errorPopUp("Maak een selectie in de tabel en probeer het nog eens.");
             isError = true;
         }
         if (isError == false) {
             String message = "Weet u zeker dat u baggagestuk: "
                     + luggageTable2.getValueAt(luggageTable2.getSelectedRow(),
                             0).toString() + " wilt verwijderen?";
             confirmation = confirmationPopUp(message);
         }
         if (confirmation == true) {
             String luggageId = luggageTable2.getValueAt(luggageTable2.getSelectedRow(), 0).toString();
             query.deleteLuggage(luggageId);
             searchLuggageTable2(9999, "", getShowHandled2());
         }
     }//GEN-LAST:event_btDeleteLuggageActionPerformed
 
     private void btCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCancelActionPerformed
         tfCustomerID1.setText("");
         tfDescription.setText("");
         tfLocation1.setText("");
     }//GEN-LAST:event_btCancelActionPerformed
 
     private void btPrintReceiptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btPrintReceiptActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_btPrintReceiptActionPerformed
 
     private void btnCreateResortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreateResortActionPerformed
 
         Resort resort = new Resort();
         
 
         String newName = tfResortName.getText().trim();
         String newAddress = tfResortAddress.getText().trim();
         String newCountry = tfResortCountry.getText().trim();
         String newCity = tfResortCity.getText().trim();
         String newPhone = tfResortPhone.getText().trim();
         String newEmail = tfResortEmail1.getText().trim() + "@" + tfResortEmail2.getText().trim();
         String newPostalCode = tfResortPostalCode.getText().trim();
 
         boolean correctInput[] = new boolean[7];
         boolean totalCorrectInput = true;
         boolean finalCheck;
 
         if (newName.equals("")) {
             errorPopUp("Vul een naam in en probeer het nog eens.");
             correctInput[0] = false;
         } else {
             correctInput[0] = true;
         }
 
         if (newAddress.equals("")) {
             errorPopUp("Vul een adres in en probeer het nog eens.");
             correctInput[1] = false;
         } else {
             correctInput[1] = true;
         }
 
         if (newCountry.equals("")) {
             errorPopUp("Vul een land in en probeer het nog eens.");
             correctInput[2] = false;
         } else {
             correctInput[2] = true;
         }
 
         if (newCity.equals("")) {
             errorPopUp("Vul een stad in en probeer het nog eens.");
             correctInput[3] = false;
         } else {
             correctInput[3] = true;
         }
 
         if (newPhone.equals("")) {
             errorPopUp("Vul een telefoonnummer in en probeer het nog eens.");
             correctInput[4] = false;
         } else {
             correctInput[4] = true;
         }
 
         if (newEmail.equals("")) {
             errorPopUp("Vul een email in en probeer het nog eens.");
             correctInput[5] = false;
         } else {
             correctInput[5] = true;
         }
 
         if (newPostalCode.equals("")) {
             errorPopUp("Vul een postcode in en probeer het nog eens.");
             correctInput[6] = false;
         } else {
             correctInput[6] = true;
         }
 
         for (int i = 0; i < correctInput.length; i++) {
             if (correctInput[i] == false) {
                 totalCorrectInput = false;
                 break;
             } else {
                 totalCorrectInput = true;
             }
             System.out.println(correctInput[i]);
         }
         System.out.println(totalCorrectInput);
         if (totalCorrectInput == true) {
             finalCheck = confirmationPopUp("Nieuwe resortgegevens:         " + "   \n"
                     + "Naam: " + newName + "   \n" + "Adres: " + newAddress + "   \n"
                     + "Land: " + newCountry + "   \n" + "Stad: " + newCity + "   \n"
                     + "Telefoonnummmer" + newPhone + "   \n" + "Email: " + newEmail + "   \n"
                     + "Postcode: " + newPostalCode);
         } else {
             finalCheck = false;
         }
 
         if (finalCheck == true) {
             query.setNewResort(tfResortName.getText().trim(),
                     tfResortAddress.getText().trim(),
                     tfResortCountry.getText().trim(),
                     tfResortCity.getText().trim(),
                     tfResortPhone.getText().trim(),
                     tfResortEmail1.getText() + "@" + tfResortEmail2.getText(),
                     tfResortPostalCode.getText().trim());
 
             searchResortTable(9999, "");
 
         }
     }//GEN-LAST:event_btnCreateResortActionPerformed
 
     private void buttonDeleteResortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonDeleteResortActionPerformed
 
         boolean isError = false;
         boolean confirmation = false;
         Resort resort = new Resort();
 
         try {
             String resortId = tblResort.getValueAt(tblResort.getSelectedRow(), 0).toString();
         } catch (IndexOutOfBoundsException e) {
             errorPopUp("Maak een selectie in de tabel en probeer het nog eens.");
             isError = true;
         }
         if (isError == false) {
             String message = "Weet u zeker dat u dit Resort wilt verwijderen";
             confirmation = confirmationPopUp(message);
         }
         if (confirmation == true) {
             String resortId = tblResort.getValueAt(tblResort.getSelectedRow(), 0).toString();
             query.deleteResort(resortId);
             searchResortTable(99999, "");
             searchResortTable1(9999, "");
         }
 
     }//GEN-LAST:event_buttonDeleteResortActionPerformed
 
     private void buttonUpdateResortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonUpdateResortActionPerformed
         boolean isError = false;
         try {
             Session.storedResortId = tblResort.getValueAt(tblResort.getSelectedRow(), 0).toString();
         } catch (IndexOutOfBoundsException e) {
             errorPopUp("Maak een selectie in de tabel en probeer het nog eens.");
             isError = true;
         }
         if (isError == false) {
             Main.displayChangeResort();
         }
     }//GEN-LAST:event_buttonUpdateResortActionPerformed
 
     private void btnLinkResortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLinkResortActionPerformed
         boolean confirmation;
         isLinked = true;
         try {
             customerToLink = customerTable1.getValueAt(customerTable1.getSelectedRow(), 0).toString();
             resortToLink = tblResort1.getValueAt(tblResort1.getSelectedRow(), 0).toString();
             String customerFirstName = customerTable1.getValueAt(customerTable1.getSelectedRow(), 1).toString() + " ";
             String customerLastName = customerTable1.getValueAt(customerTable1.getSelectedRow(), 2).toString();
             customerFullName = customerFirstName + customerLastName;
         } catch (IndexOutOfBoundsException e) {
             JOptionPane.showMessageDialog(ErrorPopUp,
                     "Maak een selectie in de tabel en probeer het nog eens.");
             isLinked = false;
         }
         boolean isError = false;
 
         customerId = Integer.parseInt(customerToLink);
         resortId = Integer.parseInt(resortToLink);
         String message = "Weet u zeker dat u klant: " + customerFullName + "\n"
                 + "Wilt koppelen aan verblijf: " + resortId;
 
         confirmation = confirmationPopUp(message);
         if (confirmation == true) {
             Luggage luggage = new Luggage();
             query.linkCustomerIdToResort(customerId, resortId);
             searchCustomerTable1(cbSearchLuggage.getSelectedIndex(), customerSearchField.getText());
             searchResortTable(9999, "");
         }
     }//GEN-LAST:event_btnLinkResortActionPerformed
 
     private void buttonSearchResort1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSearchResort1ActionPerformed
         int searchField = cbResort1.getSelectedIndex();
         searchResortTable1(searchField, tfSearchResort1.getText().trim());
     }//GEN-LAST:event_buttonSearchResort1ActionPerformed
 
     private void buttonSearchResortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSearchResortActionPerformed
      int searchField = cbResort.getSelectedIndex();
         searchResortTable(searchField, tfSearchResort.getText().trim());
     }//GEN-LAST:event_buttonSearchResortActionPerformed
 
     /**
      * @param args the command line arguments
      */
 //    public static void main(String args[]) {
 //        /* Set the Nimbus look and feel */
 //        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
 //        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
 //         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
 //         */
 //        try {
 //            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
 //                if ("Nimbus".equals(info.getName())) {
 //                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
 //                    break;
 //                }
 //            }
 //        } catch (ClassNotFoundException ex) {
 //            java.util.logging.Logger.getLogger(Employee.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
 //        } catch (InstantiationException ex) {
 //            java.util.logging.Logger.getLogger(Employee.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
 //        } catch (IllegalAccessException ex) {
 //            java.util.logging.Logger.getLogger(Employee.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
 //        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
 //            java.util.logging.Logger.getLogger(Employee.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
 //        }
 //        //</editor-fold>
 //
 //        /* Create and display the form */
 //        java.awt.EventQueue.invokeLater(new Runnable() {
 //            public void run() {
 //                new Employee().setVisible(true);
 //            }
 //        });
 //    }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btCancel;
     private javax.swing.JButton btChangeLuggage;
     private javax.swing.JButton btCreateLuggage;
     private javax.swing.JButton btDeleteLuggage;
     private javax.swing.JButton btPrintReceipt;
     private javax.swing.JButton btnCreateResort;
     private javax.swing.JButton btnLinkResort;
     private javax.swing.JButton buttonDeleteResort;
     private javax.swing.ButtonGroup buttonGroup1;
     private javax.swing.JButton buttonRefresh;
     private javax.swing.JButton buttonSearchResort;
     private javax.swing.JButton buttonSearchResort1;
     private javax.swing.JButton buttonUpdateResort;
     private javax.swing.JButton cancelButton;
     private javax.swing.JComboBox cbResort;
     private javax.swing.JComboBox cbResort1;
     private javax.swing.JComboBox cbSearchCustomer;
     private javax.swing.JComboBox cbSearchCustomer1;
     private javax.swing.JComboBox cbSearchLuggage;
     private javax.swing.JComboBox cbSearchLuggage1;
     private javax.swing.JComboBox cbStatus;
     private javax.swing.JMenuItem changePassword;
     private javax.swing.JCheckBox chbShowHandledLuggage2;
     private javax.swing.JButton createCustomer1;
     private javax.swing.JPanel customer;
     private javax.swing.JButton customerDeleteButton;
     private javax.swing.JPanel customerOptionsPanel;
     private javax.swing.JPanel customerRegistrationPanel;
     private javax.swing.JButton customerSearchButton1;
     private javax.swing.JButton customerSearchButton2;
     private javax.swing.JTextField customerSearchField;
     private javax.swing.JTextField customerSearchField1;
     private javax.swing.JTable customerTable1;
     private javax.swing.JTable customerTable2;
     private javax.swing.JPanel customerTablePanel;
     private javax.swing.JButton jButton1;
     private javax.swing.JButton jButton4;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel12;
     private javax.swing.JLabel jLabel13;
     private javax.swing.JLabel jLabel14;
     private javax.swing.JLabel jLabel15;
     private javax.swing.JLabel jLabel16;
     private javax.swing.JLabel jLabel17;
     private javax.swing.JLabel jLabel18;
     private javax.swing.JLabel jLabel19;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel20;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JLabel jLabel7;
     private javax.swing.JLabel jLabel8;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel10;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JPanel jPanel4;
     private javax.swing.JPanel jPanel5;
     private javax.swing.JPanel jPanel6;
     private javax.swing.JPanel jPanel8;
     private javax.swing.JPanel jPanel9;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JScrollPane jScrollPane3;
     private javax.swing.JScrollPane jScrollPane4;
     private javax.swing.JScrollPane jScrollPane5;
     private javax.swing.JScrollPane jScrollPane6;
     private javax.swing.JScrollPane jScrollPane7;
     private javax.swing.JTabbedPane jTabbedPane1;
     private javax.swing.JLabel lblCustomerID4;
     private javax.swing.JLabel lblCustomerID5;
     private javax.swing.JLabel lblCustomerID6;
     private javax.swing.JLabel lblCustomerID7;
     private javax.swing.JLabel lblNaam;
     private javax.swing.JButton linkButton;
     private javax.swing.JPanel linkCustomerTablePanel;
     private javax.swing.JPanel linkLuggage;
     private javax.swing.JPanel linkLuggageTablePanel;
     private javax.swing.JPanel linkOptionsPanel;
     private javax.swing.JSplitPane linkTableSplitter;
     private javax.swing.JMenuItem logout;
     private javax.swing.JPanel luggage;
     private javax.swing.JPanel luggageOptionsPanel;
     private javax.swing.JLayeredPane luggageRegistrationPanel;
     private javax.swing.JButton luggageSearchButton1;
     private javax.swing.JButton luggageSearchButton2;
     private javax.swing.JTextField luggageSearchField;
     private javax.swing.JTextField luggageSearchField1;
     private javax.swing.JTable luggageTable1;
     private javax.swing.JTable luggageTable2;
     private javax.swing.JPanel luggageTablePanel;
     private javax.swing.JMenuBar menuBar;
     private javax.swing.JTabbedPane overviewPane;
     private javax.swing.JButton refreshCustomerTable2;
     private javax.swing.JButton refreshLuggageTable2;
     private javax.swing.JButton refreshTables1;
     private javax.swing.JCheckBox showHandledLuggage1;
     private javax.swing.JTable tblResort;
     private javax.swing.JTable tblResort1;
     private javax.swing.JTextField tfAddress1;
     private javax.swing.JTextField tfAddress2;
     private javax.swing.JTextField tfCity;
     private javax.swing.JComboBox tfCountry;
     private javax.swing.JTextField tfCustomerID1;
     private javax.swing.JTextArea tfDescription;
     private javax.swing.JTextField tfEmail1;
     private javax.swing.JTextField tfEmail2;
     private javax.swing.JTextField tfFirstName;
     private javax.swing.JTextField tfLastName;
     private javax.swing.JTextField tfLocation1;
     private javax.swing.JTextField tfPhoneHome;
     private javax.swing.JTextField tfPhoneMobile;
     private javax.swing.JTextField tfPostalCode;
     private javax.swing.JTextField tfResortAddress;
     private javax.swing.JTextField tfResortCity;
     private javax.swing.JTextField tfResortCountry;
     private javax.swing.JTextField tfResortEmail1;
     private javax.swing.JTextField tfResortEmail2;
     private javax.swing.JTextField tfResortName;
     private javax.swing.JTextField tfResortPhone;
     private javax.swing.JTextField tfResortPostalCode;
     private javax.swing.JTextField tfSearchResort;
     private javax.swing.JTextField tfSearchResort1;
     private javax.swing.JMenu userMenu;
     private javax.swing.JLabel warningLabel1;
     // End of variables declaration//GEN-END:variables
 }
