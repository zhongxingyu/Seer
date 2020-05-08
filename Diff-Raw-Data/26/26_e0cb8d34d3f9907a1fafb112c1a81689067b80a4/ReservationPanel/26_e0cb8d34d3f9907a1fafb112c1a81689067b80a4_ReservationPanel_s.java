 package carrental;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.sql.Timestamp;
 import java.util.*;
 import javax.swing.*;
 import javax.swing.border.TitledBorder;
 import javax.swing.table.DefaultTableModel;
 import java.text.SimpleDateFormat;
 
 /**
  * This is the main panel for reservations
  * It contains JPanels for every relevant screen, when dealing with reservations.
  * @author CNN
  * @version 2011-12-17
  */
 public class ReservationPanel extends SuperPanel {
 
     private Reservation reservationToView; //specific customer, used to view details
     private ArrayList<Reservation> reservations; //array of reservations
     private final CreatePanel createPanel = new CreatePanel();
     private final ViewEntityPanel viewEntityPanel = new ViewEntityPanel();
     private final ListPanel listPanel = new ListPanel();
     private final GetCustomerPanel getCustomerPanel = new GetCustomerPanel();
     private final GetVehiclePanel getVehiclePanel = new GetVehiclePanel();
     private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
 
     /**
      * Constructor for reservation panel
      */
     public ReservationPanel() {
         this.reservations = CarRental.getInstance().requestReservations();
         if (!reservations.isEmpty()) {
             reservationToView = reservations.get(0);
         } else {
             reservationToView = CarRental.getInstance().requestReservation();
         }
 
         //Sets the different subpanels. Also adds them to this object with JPanel.add().
         AssignAndAddSubPanels(null, createPanel, viewEntityPanel, null, null, listPanel);
         this.setPreferredSize(new Dimension(800, 600));
 
         //Removes the default gaps between components
         setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
 
         listPanel.update();
         showListPanel();
     }
 
     /**
      * Shows create panel. Overrides super SuperPanel.
      */
     @Override
     public void showCreatePanel() {
         super.showCreatePanel();
         createPanel.centerPanel.setVisible(true);
     }
 
     /**
      * Shows get customer panel. Overrides super SuperPanel.
      */
     public void showGetCustomerPanel() {
         hideAll();
         createPanel.setVisible(true);
         createPanel.getCustomerPanel.setCustomerTable();
         createPanel.getCustomerPanel.setVisible(true);
     }
 
     /**
      * Shows get vehicle panel. Overrides super SuperPanel.
      */
     public void showGetVehiclePanel() {
         hideAll();
         createPanel.setVisible(true);
         createPanel.getVehiclePanel.update();
         createPanel.getVehiclePanel.setVisible(true);
     }
 
     /**
      * Hides all panels. Overrides super SuperPanel.
      */
     @Override
     public void hideAll() {
         super.hideAll();
         createPanel.centerPanel.setVisible(false);
         createPanel.getCustomerPanel.setVisible(false);
         createPanel.getVehiclePanel.setVisible(false);
     }
 
     /**
      * Sets the reservation to be viewed by view entity panel
      * @param reservation 
      */
     public void setReservationToView(Reservation reservation) {
         reservationToView = reservation;
     }
 
     /**
      * Sets an array of bookings
      * @param reservations 
      */
     public void setBookingsList(ArrayList<Reservation> reservations) {
         this.reservations = reservations;
     }
 
     /**
      * Updates entire panel
      */
     public void updateReservationPanel() {
         reservations = CarRental.getInstance().requestReservations();
         if (reservations.get(0) != null) {
             reservationToView = reservations.get(0);
         } else {
             reservationToView = CarRental.getInstance().requestReservation();
         }
 
         createPanel.update();
         createPanel.clearCustomerAndVehicleID();
         viewEntityPanel.update();
         listPanel.update();
     }
 
     /**
      * Inner class. Get vehicle panel extends JPanel.
      */
     public class GetVehiclePanel extends JPanel {
 
         private DefaultTableModel vehicleTableModel;
         private JComboBox vehicleTypeCombo;
         private DefaultComboBoxModel vehicleTypeComboModel;
         private JTextField descriptionField, licensePlateField, vinField, drivenField;
         private int currentVehicleTypeIndex = -1; //this is for storing the currently selected choice from the combobox.
         private ArrayList<Vehicle> vehicleList;
         private ArrayList<VehicleType> vehicleTypes;
 
         public GetVehiclePanel() {
             JPanel centerPanel, vehicleListPanel, filterPanel, topFilterPanel, middleFilterPanel, bottomFilterPanel, buttonPanel;
             JLabel vehicleTypeLabel, descriptionLabel, licensePlateLabel, vinLabel, drivenLabel;
             JButton cancelButton, setButton;
             final JTable vehicleTable;
             JScrollPane listScrollPane;
             final int defaultJTextFieldColumns = 20, strutDistance = 0;
             vehicleList = CarRental.getInstance().requestVehicles();
             vehicleTypes = CarRental.getInstance().requestVehicleTypes();
 
             //Panel settings
             setLayout(new BorderLayout());
             setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "List of vehicles"));
 
             //CenterPanel
             centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
             centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 40));
             add(centerPanel, BorderLayout.CENTER);
 
             //VehicleListPanel.
             vehicleListPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
             //Colors
             setBackground(new Color(216, 216, 208));
 
             //Creating the table model
             vehicleTableModel = new DefaultTableModel(new Object[]{"Type", "Description", "LicensePlate", "VIN", "Distance driven"}, 0);
 
             //Creating the JTable
             vehicleTable = new JTable(vehicleTableModel);
 
             listScrollPane = new JScrollPane(vehicleTable);
             //Setting the default size for the table in this scrollpane
             vehicleTable.setPreferredScrollableViewportSize(new Dimension(700, 200));
             vehicleListPanel.add(listScrollPane);
             centerPanel.add(vehicleListPanel);
 
             //FilterPanel
             filterPanel = new JPanel();
             filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.PAGE_AXIS));
             filterPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK, 2), "Filters"));
             centerPanel.add(filterPanel);
 
             //top row of filters
             topFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             filterPanel.add(topFilterPanel);
 
             //Vehicle Type
             vehicleTypeLabel = new JLabel("Vehicle Type");
             vehicleTypeComboModel = new DefaultComboBoxModel();
             vehicleTypeCombo = new JComboBox(vehicleTypeComboModel);
             vehicleTypeCombo.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     if (currentVehicleTypeIndex == -1 && vehicleTypeCombo.getSelectedIndex() > 0) { //if the current selection hasn't been set and it was not just set to "All"
                         filter();
                         currentVehicleTypeIndex = vehicleTypeCombo.getSelectedIndex();
                     } else if (currentVehicleTypeIndex > -1 && currentVehicleTypeIndex != vehicleTypeCombo.getSelectedIndex()) {
                         filter();
                         currentVehicleTypeIndex = vehicleTypeCombo.getSelectedIndex();
                     }
                 }
             });
             topFilterPanel.add(vehicleTypeLabel);
             topFilterPanel.add(Box.createRigidArea(new Dimension(16 + strutDistance, 0)));
             topFilterPanel.add(vehicleTypeCombo);
             topFilterPanel.add(Box.createRigidArea(new Dimension(91, 0)));
 
             //Description
             descriptionLabel = new JLabel("Description");
             descriptionField = new JTextField(defaultJTextFieldColumns);
             descriptionField.addKeyListener(new KeyAdapter() {
 
                 public void keyReleased(KeyEvent e) {
                     filter();
                 }
             });
             topFilterPanel.add(Box.createRigidArea(new Dimension(5, 0)));
             topFilterPanel.add(descriptionLabel);
             topFilterPanel.add(Box.createRigidArea(new Dimension(45 + strutDistance, 0)));
             topFilterPanel.add(descriptionField);
 
             //Middle Filter panel
             middleFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             filterPanel.add(middleFilterPanel);
 
             //LicensePlate
             licensePlateLabel = new JLabel("License Plate");
             licensePlateField = new JTextField(defaultJTextFieldColumns);
             licensePlateField.addKeyListener(new KeyAdapter() {
 
                 public void keyReleased(KeyEvent e) {
                     filter();
                 }
             });
             middleFilterPanel.add(licensePlateLabel);
             middleFilterPanel.add(Box.createRigidArea(new Dimension(11 + strutDistance, 0)));
             middleFilterPanel.add(licensePlateField);
 
 
             //VIN
             vinLabel = new JLabel("VIN");
             vinField = new JTextField(defaultJTextFieldColumns);
             vinField.addKeyListener(new KeyAdapter() {
 
                 public void keyReleased(KeyEvent e) {
                     filter();
                 }
             });
 
             middleFilterPanel.add(Box.createRigidArea(new Dimension(5, 0)));
             middleFilterPanel.add(vinLabel);
             middleFilterPanel.add(Box.createRigidArea(new Dimension(90 + strutDistance, 0)));
             middleFilterPanel.add(vinField);
 
             //Bottom Filter panel
             bottomFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             filterPanel.add(bottomFilterPanel);
 
             //Driven
             drivenLabel = new JLabel("Distance driven");
             drivenField = new JTextField(defaultJTextFieldColumns);
             drivenField.addKeyListener(new KeyAdapter() {
 
                 public void keyReleased(KeyEvent e) {
                     filter();
                 }
             });
             bottomFilterPanel.add(drivenLabel);
             bottomFilterPanel.add(Box.createRigidArea(new Dimension(strutDistance, 0)));
             bottomFilterPanel.add(drivenField);
 
             //ButtonPanels
             buttonPanel = new JPanel();
             add(buttonPanel, BorderLayout.SOUTH);
             buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
             buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15)); //add some space between the right edge of the screen
             buttonPanel.add(Box.createHorizontalGlue());
 
             //cancel-Button
             cancelButton = new JButton("Back");
             cancelButton.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     update();
                     showCreatePanel();
                 }
             });
             buttonPanel.add(cancelButton);
             buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
 
             //Set-button
             setButton = new JButton("Set Vehicle");
             setButton.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     if (vehicleTable.getSelectedRow() >= 0) {
                         
 
                         //Sets all filter text fields blank
                         descriptionField.setText(null);
                         licensePlateField.setText(null);
                         vinField.setText(null);
                         drivenField.setText(null);
 
                         showCreatePanel();
                         createPanel.setVehicle(Integer.toString(vehicleList.get(vehicleTable.getSelectedRow()).getID()));
                     }
                 }
             });
             buttonPanel.add(setButton);
         }
 
         /**
          * Updates the panel to show an updated list of vehicles.
          */
         public void update() {
             vehicleList = CarRental.getInstance().requestVehicles();
 
             //reset the selected vehicle type
             currentVehicleTypeIndex = -1;
 
             //Delete exisiting rows
             vehicleTableModel.setRowCount(0);
 
             //Add the updated rows with vehicles
             for (Vehicle vehicle : vehicleList) {
                 vehicleTableModel.addRow(new String[]{
                             CarRental.getInstance().requestVehicleType(vehicle.getVehicleType()).getName(),
                             vehicle.getDescription(),
                             vehicle.getLicensePlate(),
                             vehicle.getVin(),
                             Integer.toString(vehicle.getOdo())});
             }
             assert (vehicleList.size() == vehicleTableModel.getRowCount()) : "size: " + vehicleList.size() + " rows: " + vehicleTableModel.getRowCount();
 
             //Update the JComboBox
             vehicleTypeComboModel.removeAllElements();
             vehicleTypeComboModel.addElement("All");
             for (VehicleType vehicleType : vehicleTypes) {
                 vehicleTypeComboModel.addElement(vehicleType.getName());
             }
 
             //Sets all text fields blank
             descriptionField.setText(null);
             licensePlateField.setText(null);
             vinField.setText(null);
             drivenField.setText(null);
         }
 
         /**
          * Rearranges the list of vehicles so that only entries matching the filters will be shown.
          */
         public void filter() {
 
             //Delete exisiting rows
             vehicleTableModel.setRowCount(0);
 
             //Add the rows that match the filter
             for (Vehicle vehicle : vehicleList) {
 
                 //As long as -
                 if (((vehicleTypeCombo.getSelectedIndex() == -1 || vehicleTypeCombo.getSelectedIndex() == 0) || //vehicle type is not chosen or set to "All" OR
                         vehicle.getVehicleType() == vehicleTypes.get(vehicleTypeCombo.getSelectedIndex() - 1).getID()) && //Vehicle's type is the vehicle type chosen AND
                         (descriptionField.getText().trim().isEmpty() || //description field is empty OR
                         vehicle.getDescription().toLowerCase(Locale.ENGLISH).contains(descriptionField.getText().trim().toLowerCase(Locale.ENGLISH))) && //vehicles descripton equals the description given AND
                         (licensePlateField.getText().trim().isEmpty() || //License plate field is empty OR
                         vehicle.getLicensePlate().toLowerCase(Locale.ENGLISH).contains(licensePlateField.getText().trim().toLowerCase(Locale.ENGLISH))) && //vehicles license plate number equals the license plate number given AND
                         (vinField.getText().trim().isEmpty() || //VIN field is empty OR
                         vehicle.getVin().toLowerCase(Locale.ENGLISH).contains(vinField.getText().trim().toLowerCase(Locale.ENGLISH))) && //vehicles VIN equals the VIN given AND
                         (drivenField.getText().trim().isEmpty() || //driven field is empty OR
                         Integer.toString(vehicle.getOdo()).toLowerCase(Locale.ENGLISH).contains(drivenField.getText().trim().toLowerCase(Locale.ENGLISH)))) { //vehicles ODO equals the "distance driven" given
 
                     // - does the vehicle match the filter, and following row is added to the table
                     vehicleTableModel.addRow(new String[]{
                                 CarRental.getInstance().requestVehicleType(vehicle.getVehicleType()).getName(),
                                 vehicle.getDescription(),
                                 vehicle.getLicensePlate(),
                                 vehicle.getVin(),
                                 Integer.toString(vehicle.getOdo())});
                 }
             }
         }
     }
 
     /**
      * Inner class. Panel lets you choose a customer on a list.
      */
     public class GetCustomerPanel extends JPanel {
 
         private JTextField filterAdressTextField, filterPhoneTextField, filterNameTextField, filterIDTextField;
         private JTable customerTable;
         private DefaultTableModel customerTableModel;
         private ArrayList<Customer> customers;
 
         public GetCustomerPanel() {
             customers = CarRental.getInstance().requestCustomers();
 
             //Fields
             JPanel centerPanel, customerListPanel, filterPanel, topFilterPanel, bottomFilterPanel, buttonPanel;
             JScrollPane scrollPane;
             JButton cancelButton, setButton;
             final int defaultJTextFieldColumns = 20, strutDistance = 0;
 
             //listPanel
             setLayout(new BorderLayout());
             setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "List of customers"));
 
             //centerPanel
             centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
             centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 40));
             add(centerPanel, BorderLayout.CENTER);
 
             //customerListPanel.
             customerListPanel = new JPanel();
 
             //Colors
             setBackground(new Color(216, 216, 208));
 
             //Creating the table model
             customerTableModel = new DefaultTableModel(new Object[]{"ID", "Phone", "Name", "Adress", "E-Mail"}, 0);
             setCustomerTable();
 
             //Creating the table
             customerTable = new JTable(customerTableModel);
             //adding it to it's own scrollpane
             scrollPane = new JScrollPane(customerTable);
             //Setting the default size for the scrollpane
             customerTable.setPreferredScrollableViewportSize(new Dimension(680, 200));
             customerListPanel.add(scrollPane);
             centerPanel.add(customerListPanel);
 
             //FilterPanel
             JLabel filterAdressLabel, filterPhoneLabel, filterNameLabel, filterIDLabel;
 
             filterPanel = new JPanel();
             filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.PAGE_AXIS));
             filterPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK, 2), "Filters"));
             centerPanel.add(filterPanel);
 
             //top row of filters
             topFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             filterPanel.add(topFilterPanel);
 
             //ID
             filterIDLabel = new JLabel("ID");
             filterIDTextField = new JTextField(defaultJTextFieldColumns);
             filterIDTextField.addKeyListener(new KeyAdapter() {
 
                 public void keyReleased(KeyEvent e) {
                     filter();
                 }
             });
 
             topFilterPanel.add(Box.createRigidArea(new Dimension(0, 0)));
             topFilterPanel.add(filterIDLabel);
             topFilterPanel.add(Box.createRigidArea(new Dimension(68 + strutDistance, 0)));
             topFilterPanel.add(filterIDTextField);
 
             //Name
             filterNameLabel = new JLabel("Name");
             filterNameTextField = new JTextField(defaultJTextFieldColumns);
             filterNameTextField.addKeyListener(new KeyAdapter() {
 
                 public void keyReleased(KeyEvent e) {
                     filter();
                 }
             });
 
             topFilterPanel.add(Box.createRigidArea(new Dimension(5, 0)));
             topFilterPanel.add(filterNameLabel);
             topFilterPanel.add(Box.createRigidArea(new Dimension(12 + strutDistance, 0)));
             topFilterPanel.add(filterNameTextField);
 
             //Bottom Filter panel
             bottomFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             filterPanel.add(bottomFilterPanel);
 
             //Phone
             filterPhoneLabel = new JLabel("Phone number");
             filterPhoneTextField = new JTextField(defaultJTextFieldColumns);
             filterPhoneTextField.addKeyListener(new KeyAdapter() {
 
                 public void keyReleased(KeyEvent e) {
                     filter();
                 }
             });
 
             bottomFilterPanel.add(filterPhoneLabel);
             bottomFilterPanel.add(Box.createRigidArea(new Dimension(strutDistance, 0)));
             bottomFilterPanel.add(filterPhoneTextField);
 
             //Adress
             filterAdressLabel = new JLabel("Adress");
             filterAdressTextField = new JTextField(defaultJTextFieldColumns);
             filterAdressTextField.addKeyListener(new KeyAdapter() {
 
                 public void keyReleased(KeyEvent e) {
                     filter();
                 }
             });
 
             bottomFilterPanel.add(Box.createRigidArea(new Dimension(5, 0)));
             bottomFilterPanel.add(filterAdressLabel);
             bottomFilterPanel.add(Box.createRigidArea(new Dimension(5 + strutDistance, 0)));
             bottomFilterPanel.add(filterAdressTextField);
 
             //ButtonPanels
             buttonPanel = new JPanel();
             add(buttonPanel, BorderLayout.SOUTH);
             buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
             buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15)); //add some space between the right edge of the screen
             buttonPanel.add(Box.createHorizontalGlue());
 
             //cancel-Button
             cancelButton = new JButton("Back");
             cancelButton.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     setFilterTextFields();
                     showCreatePanel();
                 }
             });
             buttonPanel.add(cancelButton);
             buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
 
             //Set-button
             setButton = new JButton("Set selected");
             setButton.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     if (customerTable.getSelectedRow() >= 0) {
                         createPanel.setCustomer(Integer.toString(customers.get(customerTable.getSelectedRow()).getID()));
                         setFilterTextFields();
                         showCreatePanel();
                     }
                 }
             });
             buttonPanel.add(setButton);
         }
 
         /**
          * Updates customer array and updates list.
          */
         public void setCustomerTable() {
             customers = CarRental.getInstance().requestCustomers(); //update customers array
             customerTableModel.setRowCount(0);
 
             for (Customer customer : customers) { //update table
                 String[] split = customer.getAdress().split("\n"); //for nicer look
                 String displayed = split[0];
                 for (int i = 1; i < split.length; i++) {
                     displayed = displayed + ", " + split[i];
                 }
 
                 customerTableModel.addRow(new Object[]{customer.getID(), //ID
                             customer.getTelephone(), //Phone
                             customer.getName(), //Name
                             displayed, //Adress
                             customer.getEMail() //E-Mail
                         });
             }
         }
 
         /**
          * Resets filter text fields
          */
         public void setFilterTextFields() {
             filterAdressTextField.setText("");
             filterPhoneTextField.setText("");
             filterNameTextField.setText("");
             filterIDTextField.setText("");
         }
 
         /**
          * Updates list of customers. Only shows entries that matches parameters.
          */
         public void filter() {
             //Delete exisiting rows
             customerTableModel.setRowCount(0);
             //Add the rows that match the filter
             for (Customer customer : customers) {
                 //parameters
                 if (filterIDTextField.getText().trim().isEmpty() || //Filter ID is empty OR
                         Integer.toString(customer.getID()).trim().toLowerCase(Locale.ENGLISH).contains(filterIDTextField.getText().toLowerCase(Locale.ENGLISH)) && //Customer matches criteria
                         filterNameTextField.getText().trim().isEmpty() || //Filter name is empty OR
                         customer.getName().trim().toLowerCase(Locale.ENGLISH).contains(filterNameTextField.getText().trim().toLowerCase(Locale.ENGLISH)) && //Customer matches criteria
                         filterPhoneTextField.getText().trim().isEmpty() || //Filter Phone is empty OR
                         Integer.toString(customer.getTelephone()).trim().toLowerCase(Locale.ENGLISH).contains(filterPhoneTextField.getText().trim().toLowerCase(Locale.ENGLISH)) &&//Customer matches criteria
                         filterAdressTextField.getText().trim().isEmpty() || //Adress field is empty OR
                         customer.getAdress().trim().toLowerCase(Locale.ENGLISH).contains(filterAdressTextField.getText().trim().toLowerCase(Locale.ENGLISH))) //Customer matches criteria
                 {
                     customerTableModel.addRow(new Object[]{customer.getID(), //ID
                                 customer.getTelephone(), //Phone
                                 customer.getName(), //Name
                                 customer.getAdress(), //Adress
                                 customer.getEMail() //E-Mail
                             });
                 }
             }
         }
     }
 
     /**
      * Inner class. Panel for creating reservation.
      */
     public class CreatePanel extends SubPanel {
         //Uses Calendar libary to create Timestamps
         //Dropdown of VehicleTypes
 
         private JTextField vehicleIDTextField, reservationIDTextField, customerIDTextField, startDateTextField, endDateTextField;
         private GetCustomerPanel getCustomerPanel;
         private GetVehiclePanel getVehiclePanel;
         private JPanel centerPanel;
 
         /**
          * Constructor
          */
         public CreatePanel() {
             //Fields
             JPanel panelContainer, vehiclePanel, endDatePanel, startDatePanel, reservationIDPanel, customerPanel, buttonPanel;
             JLabel vehicleIDLabel, dateFormatLabel, dateFormatLabel2, reservationIDLabel, customerIDLabel, startDateLabel, endDateLabel;
             JButton findVehicleButton, findCustomerButton, createButton, cancelButton;
             final int defaultJTextFieldColumns = 20, strutDistance = 0;
 
             //createPanel
             setLayout(new BorderLayout());
             setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Make a reservation"));
 
             //Panel Container
             panelContainer = new JPanel(new FlowLayout());
             add(panelContainer, BorderLayout.CENTER);
 
             getCustomerPanel = new GetCustomerPanel();
             getVehiclePanel = new GetVehiclePanel();
 
             panelContainer.add(getCustomerPanel);
             panelContainer.add(getVehiclePanel);
 
             getCustomerPanel.setVisible(false);
             getVehiclePanel.setVisible(false);
 
             //Center Panel
             centerPanel = new JPanel();
             centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
             centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 40));
             panelContainer.add(centerPanel);
 
             //Visibility
             panelContainer.setVisible(true);
             centerPanel.setVisible(true);
 
             //Colors
             setBackground(new Color(216, 216, 208));
             centerPanel.setBackground(new Color(239, 240, 236));
 
             //Reservation ID
             reservationIDLabel = new JLabel("Reservation ID");
            reservationIDTextField = new JTextField(" Automaticly generated", defaultJTextFieldColumns);
             reservationIDTextField.setEditable(false);
             reservationIDPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
             reservationIDPanel.add(Box.createRigidArea(new Dimension(7, 0)));
             reservationIDPanel.add(reservationIDLabel);
             reservationIDPanel.add(Box.createRigidArea(new Dimension(13 + strutDistance, 0)));
             reservationIDPanel.add(reservationIDTextField);
             centerPanel.add(reservationIDPanel);
 
             //Customer ID
             customerIDLabel = new JLabel("Customer ID");
             customerIDTextField = new JTextField("", defaultJTextFieldColumns);
             customerIDTextField.setBackground(Color.WHITE);
             findCustomerButton = new JButton("Find Customer");
             findCustomerButton.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     getCustomerPanel.setCustomerTable();
                     showGetCustomerPanel();
                 }
             });
             customerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             customerPanel.add(Box.createRigidArea(new Dimension(5, 0)));
             customerPanel.add(customerIDLabel);
             customerPanel.add(Box.createRigidArea(new Dimension(28 + strutDistance, 0)));
             customerPanel.add(customerIDTextField);
             customerPanel.add(Box.createRigidArea(new Dimension(13, 0)));
             customerPanel.add(findCustomerButton);
             centerPanel.add(customerPanel);
 
             //Vehicle ID
             vehicleIDLabel = new JLabel("Vehicle ID");
             vehicleIDTextField = new JTextField(defaultJTextFieldColumns);
             findVehicleButton = new JButton("Find Vehicle");
             findVehicleButton.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     getVehiclePanel.update();
                     showGetVehiclePanel();
                 }
             });
 
             vehiclePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             vehiclePanel.add(Box.createRigidArea(new Dimension(5, 0)));
             vehiclePanel.add(vehicleIDLabel);
             vehiclePanel.add(Box.createRigidArea(new Dimension(42 + strutDistance, 0)));
             vehiclePanel.add(vehicleIDTextField);
             vehiclePanel.add(Box.createRigidArea(new Dimension(14 + strutDistance, 0)));
             vehiclePanel.add(findVehicleButton);
             centerPanel.add(vehiclePanel);
 
             //Start date
             startDateLabel = new JLabel("Start date");
             startDateTextField = new JTextField(defaultJTextFieldColumns);
             dateFormatLabel = new JLabel("dd-mm-yyyy");
 
             startDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             startDatePanel.add(Box.createRigidArea(new Dimension(5, 0)));
             startDatePanel.add(startDateLabel);
             startDatePanel.add(Box.createRigidArea(new Dimension(42 + strutDistance, 0)));
             startDatePanel.add(startDateTextField);
             startDatePanel.add(Box.createRigidArea(new Dimension(5 + strutDistance, 0)));
             startDatePanel.add(dateFormatLabel);
             centerPanel.add(startDatePanel);
 
             //End date
             endDateLabel = new JLabel("End date");
             endDateTextField = new JTextField(defaultJTextFieldColumns);
             dateFormatLabel2 = new JLabel("dd-mm-yyyy");
 
             endDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             endDatePanel.add(Box.createRigidArea(new Dimension(5, 0)));
             endDatePanel.add(endDateLabel);
             endDatePanel.add(Box.createRigidArea(new Dimension(49 + strutDistance, 0)));
             endDatePanel.add(endDateTextField);
             endDatePanel.add(Box.createRigidArea(new Dimension(5 + strutDistance, 0)));
             endDatePanel.add(dateFormatLabel2);
             centerPanel.add(endDatePanel);
 
             //ButtonPanel
             buttonPanel = new JPanel();
             add(buttonPanel, BorderLayout.SOUTH);
             buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
             buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15)); //add some space between the right edge of the screen
             buttonPanel.add(Box.createHorizontalGlue());
 
             //cancelButton
             cancelButton = new JButton("Cancel");
             cancelButton.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     listPanel.update();
                     update();
                     clearCustomerAndVehicleID();
                     showListPanel();
                 }
             });
             buttonPanel.add(cancelButton);
             buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
 
             //create-button
             createButton = new JButton("Create");
             createButton.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     if (!reservationIDTextField.getText().trim().isEmpty()
                             && !vehicleIDTextField.getText().trim().isEmpty()
                             && !startDateTextField.getText().trim().isEmpty()
                             && !endDateTextField.getText().trim().isEmpty()) {
                         boolean customer = false;
                         boolean vehicle = false;
                         if (CarRental.getInstance().requestVehicle(Integer.parseInt(vehicleIDTextField.getText().trim())) != null) {
                             vehicle = true;
                         } else {
                             CarRental.getInstance().appendLog("Vehicle doesn't exist");
                         }
                         if (CarRental.getInstance().requestCustomer(Integer.parseInt(customerIDTextField.getText().trim())) != null) {
                             customer = true;
                         } else {
                             CarRental.getInstance().appendLog("Customer doesn't exist");
                         }
                         if (customer && vehicle) {
                             try {
                                 CarRental.getInstance().saveReservation(new Reservation(
                                        CarRental.getInstance().requestNewReservationId(),
                                         Integer.parseInt(vehicleIDTextField.getText()),
                                         new Timestamp(dateFormat.parse(startDateTextField.getText().trim()).getTime()),
                                         new Timestamp(dateFormat.parse(endDateTextField.getText().trim()).getTime()),
                                         Integer.parseInt(customerIDTextField.getText())));
                                CarRental.getInstance().appendLog("Reservation " + reservationIDTextField.getText() + " edited");
                                 reservations = CarRental.getInstance().requestReservations();
                                 update();
                                 clearCustomerAndVehicleID();
                                 showListPanel();
                             } catch (NumberFormatException ex) {
                                 CarRental.getInstance().appendLog("Vehicle ID number must be numbers only");
                             } catch (java.text.ParseException ex) {
                                 CarRental.getInstance().appendLog("Time fields must be in format dd-mm-yyyy");
                             }
                         }
                     } else { //A TextFild is empty
                         CarRental.getInstance().appendLog("A text field is empty");
                     }
                 }
             });
             buttonPanel.add(createButton);
         }
 
         /**
          * Sets customer ID text field
          * @param customerID 
          */
         public void setCustomer(String customerID) {
             customerIDTextField.setText(customerID);
         }
 
         /**
          * Sets vehicle ID text field
          * @param vehicleID 
          */
         public void setVehicle(String vehicleID) {
             vehicleIDTextField.setText(vehicleID);
         }
 
         /**
          * Resets text fields
          */
         public void update() {
            reservationIDTextField.setText(" Automaticly generated");
             startDateTextField.setText("");
             endDateTextField.setText("");
         }
         
         /**
          * These have to be cleared seperetly since we don't always want to clear them
          */
         public void clearCustomerAndVehicleID(){
             vehicleIDTextField.setText("");
             customerIDTextField.setText("");
         }
     }
 
     /**
      * Inner class. Shows a specific entity. Extends JPanel.
      */
     public class ViewEntityPanel extends SubPanel {
 
         private JTextField customerNameTextField, vehicleIDTextField, reservationIDTextField, customerIDTextField, startDateTextField, endDateTextField;
         private JPanel centerPanel;
         private CustomerListPanel customerListPanel;
         private VehicleListPanel vehicleListPanel;
 
         public ViewEntityPanel() {
             customerListPanel = new CustomerListPanel();
             vehicleListPanel = new VehicleListPanel();
             vehicleListPanel.setTableSize(700, 100);
             customerListPanel.setTableSize(700, 100);
 
             //Fields
             JPanel panelContainer, customerNamePanel, vehiclePanel, endDatePanel, startDatePanel, reservationIDPanel, customerPanel, buttonPanel;
             JLabel customerNameLabel, vehicleIDLabel, dateFormatLabel, dateFormatLabel2, reservationIDLabel, customerIDLabel, startDateLabel, endDateLabel;
             JButton changeVehicleButton, changeCustomerButton, deleteButton, editButton, cancelButton;
             final int defaultJTextFieldColumns = 20, strutDistance = 0;
 
             //View entity panel
             setLayout(new BorderLayout());
             setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "View a reservation"));
 
             //Panel Container
             panelContainer = new JPanel();
             panelContainer.setLayout(new BoxLayout(panelContainer, BoxLayout.PAGE_AXIS));
             add(panelContainer, BorderLayout.CENTER);
 
             //Center Panel
             centerPanel = new JPanel();
             centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
             centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 40));
             panelContainer.add(centerPanel);
 
             panelContainer.add(customerListPanel);
             panelContainer.add(vehicleListPanel);
 
             //Colors
             setBackground(new Color(216, 216, 208));
             centerPanel.setBackground(new Color(239, 240, 236));
 
             //Visibility
             panelContainer.setVisible(true);
             centerPanel.setVisible(true);
             customerListPanel.setVisible(true);
             vehicleListPanel.setVisible(true);
 
             //Reservation ID
             reservationIDLabel = new JLabel("Reservation ID");
            reservationIDTextField = new JTextField(" Automaticly generated", defaultJTextFieldColumns);
             reservationIDTextField.setEditable(false);
             reservationIDPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
             reservationIDPanel.add(Box.createRigidArea(new Dimension(7, 0)));
             reservationIDPanel.add(reservationIDLabel);
             reservationIDPanel.add(Box.createRigidArea(new Dimension(10 + strutDistance, 0)));
             reservationIDPanel.add(reservationIDTextField);
             centerPanel.add(reservationIDPanel);
 
             //Customer ID
             customerIDLabel = new JLabel("Customer ID");
             customerIDTextField = new JTextField("", defaultJTextFieldColumns);
             customerIDTextField.setBackground(Color.WHITE);
 
             customerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             customerPanel.add(Box.createRigidArea(new Dimension(5, 0)));
             customerPanel.add(customerIDLabel);
             customerPanel.add(Box.createRigidArea(new Dimension(33 + strutDistance, 0)));
             customerPanel.add(customerIDTextField);
             centerPanel.add(customerPanel);
 
             //Customer Name
             customerNameLabel = new JLabel("Customer Name");
             customerNameTextField = new JTextField("", defaultJTextFieldColumns);
             customerNameTextField.setBackground(Color.WHITE);
 
             customerNamePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             customerNamePanel.add(Box.createRigidArea(new Dimension(5, 0)));
             customerNamePanel.add(customerIDLabel);
             customerNamePanel.add(Box.createRigidArea(new Dimension(23 + strutDistance, 0)));
             customerNamePanel.add(customerIDTextField);
             customerNamePanel.add(Box.createRigidArea(new Dimension(13, 0)));
             centerPanel.add(customerNamePanel);
 
             //Vehicle ID
             vehicleIDLabel = new JLabel("Vehicle ID");
             vehicleIDTextField = new JTextField(defaultJTextFieldColumns);
 
             vehiclePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             vehiclePanel.add(Box.createRigidArea(new Dimension(5, 0)));
             vehiclePanel.add(vehicleIDLabel);
             vehiclePanel.add(Box.createRigidArea(new Dimension(38 + strutDistance, 0)));
             vehiclePanel.add(vehicleIDTextField);
             centerPanel.add(vehiclePanel);
 
             //Start date
             startDateLabel = new JLabel("Start date");
             startDateTextField = new JTextField(defaultJTextFieldColumns);
             dateFormatLabel = new JLabel("dd-mm-yyyy");
 
             startDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             startDatePanel.add(Box.createRigidArea(new Dimension(5, 0)));
             startDatePanel.add(startDateLabel);
             startDatePanel.add(Box.createRigidArea(new Dimension(37 + strutDistance, 0)));
             startDatePanel.add(startDateTextField);
             startDatePanel.add(Box.createRigidArea(new Dimension(5 + strutDistance, 0)));
             startDatePanel.add(dateFormatLabel);
             centerPanel.add(startDatePanel);
 
             //End date
             endDateLabel = new JLabel("End date");
             endDateTextField = new JTextField(defaultJTextFieldColumns);
             dateFormatLabel2 = new JLabel("dd-mm-yyyy");
 
             endDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             endDatePanel.add(Box.createRigidArea(new Dimension(5, 0)));
             endDatePanel.add(endDateLabel);
             endDatePanel.add(Box.createRigidArea(new Dimension(44 + strutDistance, 0)));
             endDatePanel.add(endDateTextField);
             endDatePanel.add(Box.createRigidArea(new Dimension(5 + strutDistance, 0)));
             endDatePanel.add(dateFormatLabel2);
             centerPanel.add(endDatePanel);
 
             //ButtonPanel
             buttonPanel = new JPanel();
             add(buttonPanel, BorderLayout.SOUTH);
             buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
             buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15)); //add some space between the right edge of the screen
             buttonPanel.add(Box.createHorizontalGlue());
 
             //Change customer
             changeCustomerButton = new JButton("Change Customer");
             changeCustomerButton.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     setCustomerIDTextField(customerListPanel.getCustomerID());
                 }
             });
 
             buttonPanel.add(changeCustomerButton);
             buttonPanel.add(Box.createRigidArea(new Dimension(8 + strutDistance, 0)));
 
             //Change vehicle
             changeVehicleButton = new JButton("Change Vehicle");
             changeVehicleButton.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     setVehicleIDTextField(vehicleListPanel.getVehicleID());
                 }
             });
 
             buttonPanel.add(changeVehicleButton);
             buttonPanel.add(Box.createRigidArea(new Dimension(50 + strutDistance, 0)));
 
             //deleteButton
             deleteButton = new JButton("Delete");
             deleteButton.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     String id = Integer.toString(reservationToView.getID());
                     delete(reservationToView);
                     CarRental.getInstance().appendLog("Succesfully deleted reservation " + id);
                     update();
                     listPanel.update();
                     showListPanel();
                 }
             });
             buttonPanel.add(deleteButton);
             buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
 
             //editButton
             editButton = new JButton("Save changes");
             editButton.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     if (!reservationIDTextField.getText().trim().isEmpty()
                             && !vehicleIDTextField.getText().trim().isEmpty()
                             && !startDateTextField.getText().trim().isEmpty()
                             && !endDateTextField.getText().trim().isEmpty()) {
                         boolean customer = false;
                         boolean vehicle = false;
                         if (CarRental.getInstance().requestVehicle(Integer.parseInt(vehicleIDTextField.getText().trim())) != null) {
                             vehicle = true;
                         } else {
                             CarRental.getInstance().appendLog("Vehicle doesn't exist");
                         }
                         if (CarRental.getInstance().requestCustomer(Integer.parseInt(customerIDTextField.getText().trim())) != null) {
                             customer = true;
                         } else {
                             CarRental.getInstance().appendLog("Customer doesn't exist");
                         }
                         if (customer && vehicle) {
                             try {
                                 CarRental.getInstance().saveReservation(new Reservation(
                                         Integer.parseInt(reservationIDTextField.getText().trim()),
                                         Integer.parseInt(vehicleIDTextField.getText().trim()),
                                         new Timestamp(dateFormat.parse(startDateTextField.getText().trim()).getTime()),
                                         new Timestamp(dateFormat.parse(endDateTextField.getText().trim()).getTime()),
                                         Integer.parseInt(customerIDTextField.getText().trim())));
                                 reservations = CarRental.getInstance().requestReservations();
                                 CarRental.getInstance().appendLog("Reservation " + reservationIDTextField.getText() + " edited");
                                 reservationToView = CarRental.getInstance().requestReservation(reservationToView.getID());
                                 listPanel.update();
                                 update();
                                 showViewEntityPanel();
                             } catch (java.text.ParseException ex) {
                                 CarRental.getInstance().appendLog("Time fields must be in format dd-mm-yyyy");
                             } catch (NumberFormatException ex) {
                                 CarRental.getInstance().appendLog("NumberFormatExceptopm...");
                             }
                         }
                     } else { //A TextFild is empty
                         CarRental.getInstance().appendLog("A text field is empty");
                     }
                 }
             });
             buttonPanel.add(editButton);
             buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
 
             //cancelButton
             cancelButton = new JButton("Back");
             cancelButton.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     update();
                     showListPanel();
                     listPanel.update();
                 }
             });
             buttonPanel.add(cancelButton);
             buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
         }
 
         /**
          * Deletes the given reservation.
          * @param reservation 
          */
         private void delete(Reservation reservation) {
             CarRental.getInstance().deleteReservation(reservation.getID());
         }
 
         /**
          * Sets the customer ID text field if it is or is above zero
          * @param id 
          */
         public void setCustomerIDTextField(int id) {
             if (id > -1) {
                 customerIDTextField.setText(" " + id);
             }
         }
 
         /**
          * Sets the vehicle ID text field if it is or is above zero
          * @param id 
          */
         public void setVehicleIDTextField(int id) {
             if (id > -1) {
                 vehicleIDTextField.setText(" " + id);
             }
         }
 
         /**
          * Updates the text fields
          */
         public void update() {
             customerNameTextField.setText(" ");
             customerIDTextField.setText(" " + reservationToView.getCustomerID());
             reservationIDTextField.setText(" " + reservationToView.getID());
             vehicleIDTextField.setText(" " + reservationToView.getVehicleID());
             startDateTextField.setText(" " + dateFormat.format(new Date(reservationToView.getTStart().getTime())));
             endDateTextField.setText(" " + dateFormat.format(new Date(reservationToView.getTEnd().getTime())));
         }
     }
 
     /**
      * Inner classes. Shows a list of reservations.
      */
     public class ListPanel extends SubPanel {
 
         private JTextField filterCustomerIDTextField, filterReservationIDTextField, filterVehicleIDTextField, filterStartDateTextField, filterEndDateTextField;
         private JTable reservationTable;
         private DefaultTableModel reservationTableModel;
 
         public ListPanel() {
 
             //Fields
             JPanel centerPanel, reservationListPanel, filterPanel, topFilterPanel, middleFilterPanel, bottomFilterPanel, buttonPanel;
             JScrollPane scrollPane;
             JButton cancelButton, viewButton;
             final int defaultJTextFieldColumns = 20, strutDistance = 0;
 
             //listPanel
             setLayout(new BorderLayout());
             setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "List of Reservations"));
 
             //centerPanel
             centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
             centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 40));
             add(centerPanel, BorderLayout.CENTER);
 
             //customerListPanel
             reservationListPanel = new JPanel();
 
             //Colors
             setBackground(new Color(216, 216, 208));
 
             //Creating the table model
             reservationTableModel = new DefaultTableModel(new Object[]{"ID", "VehicleID", "Start date", "End date", "CustomerID"}, 0);
 
             //Creating the table
             reservationTable = new JTable(reservationTableModel);
             //adding it to it's own scrollpane
             scrollPane = new JScrollPane(reservationTable);
             //Setting the default size for the scrollpane
             reservationTable.setPreferredScrollableViewportSize(new Dimension(680, 200));
             reservationListPanel.add(scrollPane);
             centerPanel.add(reservationListPanel);
 
             //FilterPanel
             JLabel filterReservationIDLabel, filterCustomerIDLabel, filterVehicleIDLabel, filterStartDateLabel, filterEndDateLabel;
 
             filterPanel = new JPanel();
             filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.PAGE_AXIS));
             filterPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK, 2), "Filters"));
             centerPanel.add(filterPanel);
 
             //top row of filters
             topFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             filterPanel.add(topFilterPanel);
 
             //Reservation ID
             filterReservationIDLabel = new JLabel("Reservation ID");
             filterReservationIDTextField = new JTextField(defaultJTextFieldColumns);
             filterReservationIDTextField.addKeyListener(new KeyAdapter() {
 
                 public void keyReleased(KeyEvent e) {
                     filter();
                 }
             });
 
             topFilterPanel.add(Box.createRigidArea(new Dimension(0, 0)));
             topFilterPanel.add(filterReservationIDLabel);
             topFilterPanel.add(Box.createRigidArea(new Dimension(17 + strutDistance, 0)));
             topFilterPanel.add(filterReservationIDTextField);
 
             //Vehicle ID
             filterVehicleIDLabel = new JLabel("Vehicle ID");
             filterVehicleIDTextField = new JTextField(defaultJTextFieldColumns);
             filterVehicleIDTextField.addKeyListener(new KeyAdapter() {
 
                 public void keyReleased(KeyEvent e) {
                     filter();
                 }
             });
 
             topFilterPanel.add(Box.createRigidArea(new Dimension(19, 0)));
             topFilterPanel.add(filterVehicleIDLabel);
             topFilterPanel.add(Box.createRigidArea(new Dimension(12 + strutDistance, 0)));
             topFilterPanel.add(filterVehicleIDTextField);
 
             //Customer ID
             filterCustomerIDLabel = new JLabel("Customer ID");
             filterCustomerIDTextField = new JTextField(defaultJTextFieldColumns);
             filterCustomerIDTextField.addKeyListener(new KeyAdapter() {
 
                 public void keyReleased(KeyEvent e) {
                     filter();
                 }
             });
 
             middleFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             filterPanel.add(middleFilterPanel);
 
             middleFilterPanel.add(Box.createRigidArea(new Dimension(0, 0)));
             middleFilterPanel.add(filterCustomerIDLabel);
             middleFilterPanel.add(Box.createRigidArea(new Dimension(28 + strutDistance, 0)));
             middleFilterPanel.add(filterCustomerIDTextField);
 
             //Bottom Filter panel
             bottomFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             filterPanel.add(bottomFilterPanel);
 
             //Start Date
             filterStartDateLabel = new JLabel("Start date");
             filterStartDateTextField = new JTextField(defaultJTextFieldColumns);
             filterStartDateTextField.addKeyListener(new KeyAdapter() {
 
                 public void keyReleased(KeyEvent e) {
                     filter();
                 }
             });
 
             bottomFilterPanel.add(Box.createRigidArea(new Dimension(0 + strutDistance, 0)));
             bottomFilterPanel.add(filterStartDateLabel);
             bottomFilterPanel.add(Box.createRigidArea(new Dimension(42 + strutDistance, 0)));
             bottomFilterPanel.add(filterStartDateTextField);
 
             //End Date
             filterEndDateLabel = new JLabel("End date");
             filterEndDateTextField = new JTextField(defaultJTextFieldColumns);
             filterEndDateTextField.addKeyListener(new KeyAdapter() {
 
                 public void keyReleased(KeyEvent e) {
                     filter();
                 }
             });
 
             bottomFilterPanel.add(Box.createRigidArea(new Dimension(20 + strutDistance, 0)));
             bottomFilterPanel.add(filterEndDateLabel);
             bottomFilterPanel.add(Box.createRigidArea(new Dimension(20 + strutDistance, 0)));
             bottomFilterPanel.add(filterEndDateTextField);
 
             //ButtonPanels
             buttonPanel = new JPanel();
             add(buttonPanel, BorderLayout.SOUTH);
             buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
             buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15)); //add some space between the right edge of the screen
             buttonPanel.add(Box.createHorizontalGlue());
 
             //View-button
             viewButton = new JButton("View selected");
             viewButton.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     if (reservationTable.getSelectedRow() >= 0) {
                         reservationToView = reservations.get(reservationTable.getSelectedRow());
                         viewEntityPanel.update();
                         showViewEntityPanel();
                         CarRental.getInstance().appendLog("Showing reservation \"" + reservationToView.getID() + "\" now.");
                     }
                 }
             });
             buttonPanel.add(viewButton);
         }
 
         /**
          * Resets filter text fields and reloads the list of reservations.
          */
         public void update() {
             setFilterTextFields();
             reservations = CarRental.getInstance().requestReservations(); //update array
 
             reservationTableModel.setRowCount(0);
             for (Reservation reservation : reservations) { //update table
                 reservationTableModel.addRow(new Object[]{
                             reservation.getID(), //ID
                             reservation.getVehicleID(), //Vehicle ID
                             dateFormat.format(new Date(reservation.getTStart().getTime())), //TStart 
                             dateFormat.format(new Date(reservation.getTEnd().getTime())), //TEnd 
                             reservation.getCustomerID() //Customer
                         });
             }
         }
 
         /**
          * Resets the filter text fields.
          */
         public void setFilterTextFields() {
             filterCustomerIDTextField.setText("");
             filterEndDateTextField.setText("");
             filterReservationIDTextField.setText("");
             filterStartDateTextField.setText("");
             filterVehicleIDTextField.setText("");
         }
 
         /**
          * Reloads the list, showing only entries matching parameters.
          */
         public void filter() {
             //Delete exisiting rows
             reservationTableModel.setRowCount(0);
             //Add the rows that match the filter
             reservations = CarRental.getInstance().requestReservations();
 
             for (Reservation reservation : reservations) {
                 Timestamp tStart = new Timestamp(0);
                 Timestamp tEnd = new Timestamp(0);
                 if (!(filterStartDateTextField.getText().trim().isEmpty()) || !(filterEndDateTextField.getText().trim().isEmpty())) {
                     try {
                         tStart.setTime(dateFormat.parse(filterStartDateTextField.getText().trim()).getTime());
                         tEnd.setTime(dateFormat.parse(filterEndDateTextField.getText().trim()).getTime());
                     } catch (java.text.ParseException ex) {
                         CarRental.getInstance().appendLog("Time fields must be in format dd-mm-yyyy");
                     }
                 }
 
                 //parameters
                 if (filterReservationIDTextField.getText().trim().isEmpty() || //Filter ID is empty OR
                         Integer.toString(reservation.getID()).trim().toLowerCase(Locale.ENGLISH).contains(filterReservationIDTextField.getText().toLowerCase(Locale.ENGLISH)) && //Customer matches criteria
                         filterCustomerIDTextField.getText().trim().isEmpty() || //Filter Phone is empty OR
                         Integer.toString(reservation.getCustomerID()).trim().toLowerCase(Locale.ENGLISH).contains(filterCustomerIDTextField.getText().trim().toLowerCase(Locale.ENGLISH)) && //Customer matches criteria
                         filterVehicleIDTextField.getText().trim().isEmpty() || //Adress field is empty OR
                         Integer.toString(reservation.getVehicleID()).trim().toLowerCase(Locale.ENGLISH).contains(filterVehicleIDTextField.getText().trim().toLowerCase(Locale.ENGLISH)) && //Customer matches criteria
                         filterStartDateTextField.getText().trim().isEmpty()
                         || tStart.before(reservation.getTEnd())
                         && filterEndDateTextField.getText().trim().isEmpty()
                         || tEnd.before(reservation.getTStart())) {
 
                     reservationTableModel.addRow(new Object[]{
                                 reservation.getID(), //ID
                                 reservation.getVehicleID(), //Vehicle ID
                                 dateFormat.format(new Date(reservation.getTStart().getTime())), //TStart
                                 dateFormat.format(new Date(reservation.getTEnd().getTime())), //TEnd
                                 reservation.getCustomerID(),});
                 }
             }
         }
     }
 }
