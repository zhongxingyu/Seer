 package carrental;
 
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Locale;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.text.JTextComponent;
 
 /**
  * This is the main panel regarding vehicles.
  * It contains JPanels for every relevant screen, when dealing with vehicles.
  * These are implemented as inner classes.
  * @author CNN
  * @version 2011-12-11
  */
 public class VehiclePanel extends SuperPanel {
 
     private Vehicle vehicleToView; //specific vehicle, used to view details
     private VehicleType vehicleTypeToView; //specific vehicle type, used to view details
     private ArrayList<Vehicle> vehicleList;
     private ArrayList<VehicleType> vehicleTypes;
     private GraphicAlternate graph;
     private CreatePanel createPanel;
     private ViewVehiclePanel viewVehiclePanel;
     private ViewVehicleTypePanel viewVehicleTypePanel;
     private ListPanel listPanel;
 
     /**
      * Sets up the vehiclepanel and all its subpanels.
      */
     public VehiclePanel() {
         vehicleList = CarRental.getInstance().requestVehicles();
         vehicleTypes = CarRental.getInstance().requestVehicleTypes();
         createPanel = new CreatePanel();
         viewVehiclePanel = new ViewVehiclePanel();
         viewVehicleTypePanel = new ViewVehicleTypePanel();
         listPanel = new ListPanel();
         //Sets the different subpanels. Also adds them to this object with JPanel.add().
         AssignAndAddSubPanels(new MainScreenPanel(), createPanel, viewVehiclePanel, new AddTypePanel(), viewVehicleTypePanel, listPanel);
         this.setPreferredSize(new Dimension(800, 600));
         showMainScreenPanel();
     }
 
     @Override
     public void showCreatePanel() {
         createPanel.update();
         super.showCreatePanel();
     }
 
     @Override
     public void showViewEntityPanel() {
         viewVehiclePanel.update();
         super.showViewEntityPanel();
     }
 
     @Override
     public void showListPanel() {
         listPanel.update();
         super.showListPanel();
     }
 
     @Override
     public void showViewTypePanel() {
         viewVehicleTypePanel.update();
         super.showViewTypePanel();
     }
 
     //Temporary Main
     //TODO: Remove
     public static void main(String[] args) {
         JFrame frame = new JFrame("VehicleFrame");
         frame.setPreferredSize(new Dimension(800, 600));
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         Container contentPane = frame.getContentPane();
         contentPane.add(new VehiclePanel());
         frame.pack();
         frame.setVisible(true);
     }
 
     /**
      * This inner class creates a JPanel with an overview of reservations for the vehicles.
      */
     public class MainScreenPanel extends JPanel {
 
         /**
          * Creates the panel with the overview.
          */
         public MainScreenPanel() {
             graph = new GraphicAlternate();
             graph.setPreferredSize(new Dimension(800, 600));
             add(graph);
             System.out.println(graph.toString());
         }
     }
 
     /**
      * This inner class creates a JPanel with the functionality to create a vehicle.
      */
     public class CreatePanel extends JPanel {
 
         private JTextField descriptionField, licensePlateField, vinField, drivenField;
         private JTextArea additionalArea;
         private DefaultComboBoxModel vehicleTypeComboModel;
 
         /**
          * Sets up the basic functionality needed to create a vehicle.
          */
         public CreatePanel() {
 
             JPanel centerPanel, buttonPanel, vehicleTypePanel, descriptionPanel, licensePlatePanel, vinPanel, drivenPanel, additionalPanel;
             JLabel vehicleTypeLabel, descriptionLabel, licensePlateLabel, vinLabel, drivenLabel, kilometerLabel, additionalLabel;
             JScrollPane additionalScrollPane;
             JButton createButton, cancelButton;
             final JComboBox vehicleTypeCombo;
             final int defaultJTextFieldColumns = 20, strutDistance = 0;
 
             //Panel settings
             setLayout(new BorderLayout());
             setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Create a vehicle"));
             //Center Panel
             centerPanel = new JPanel();
             centerPanel.setLayout(
                     new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
             centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 40));
 
             add(centerPanel, BorderLayout.CENTER);
 
             //Colors
             setBackground(
                     new Color(216, 216, 208));
             centerPanel.setBackground(
                     new Color(239, 240, 236));
 
             //Vehicle Type
             vehicleTypeLabel = new JLabel("Vehicle Type");
             vehicleTypeComboModel = new DefaultComboBoxModel();
             vehicleTypeCombo = new JComboBox(vehicleTypeComboModel);
 
             for (VehicleType vehicleType : vehicleTypes) {
                 vehicleTypeComboModel.addElement(vehicleType.getName());
             }
             vehicleTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
             vehicleTypePanel.add(Box.createRigidArea(new Dimension(5, 0)));
             vehicleTypePanel.add(vehicleTypeLabel);
 
             vehicleTypePanel.add(Box.createRigidArea(new Dimension(48 + strutDistance, 0)));
             vehicleTypePanel.add(vehicleTypeCombo);
 
             centerPanel.add(vehicleTypePanel);
 
             //Name
             descriptionLabel = new JLabel("Description");
             descriptionField = new JTextField(defaultJTextFieldColumns);
             descriptionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
             descriptionPanel.add(Box.createRigidArea(new Dimension(5, 0)));
             descriptionPanel.add(descriptionLabel);
 
             descriptionPanel.add(Box.createRigidArea(new Dimension(55 + strutDistance, 0)));
             descriptionPanel.add(descriptionField);
 
             centerPanel.add(descriptionPanel);
 
             //LicensePlate
             licensePlateLabel = new JLabel("License Plate");
             licensePlateField = new JTextField(defaultJTextFieldColumns);
             licensePlatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
             licensePlatePanel.add(Box.createRigidArea(new Dimension(5, 0)));
             licensePlatePanel.add(licensePlateLabel);
 
             licensePlatePanel.add(Box.createRigidArea(new Dimension(43 + strutDistance, 0)));
             licensePlatePanel.add(licensePlateField);
 
             centerPanel.add(licensePlatePanel);
 
             //VIN
             vinLabel = new JLabel("VIN");
             vinField = new JTextField(defaultJTextFieldColumns);
             vinPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
             vinPanel.add(Box.createRigidArea(new Dimension(5, 0)));
             vinPanel.add(vinLabel);
 
             vinPanel.add(Box.createRigidArea(new Dimension(101 + strutDistance, 0)));
             vinPanel.add(vinField);
 
             centerPanel.add(vinPanel);
 
             //Driven
             drivenLabel = new JLabel("Distance driven");
             drivenField = new JTextField(defaultJTextFieldColumns);
             kilometerLabel = new JLabel("kilometers");
             drivenPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
             drivenPanel.add(Box.createRigidArea(new Dimension(5, 0)));
             drivenPanel.add(drivenLabel);
 
             drivenPanel.add(Box.createRigidArea(new Dimension(32 + strutDistance, 0)));
             drivenPanel.add(drivenField);
 
             drivenPanel.add(kilometerLabel);
 
             centerPanel.add(drivenPanel);
 
             //Additional Comment
             additionalLabel = new JLabel("Additional comments");
             additionalArea = new JTextArea(5, 30);
             additionalScrollPane = new JScrollPane(additionalArea);
 
             additionalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
             additionalPanel.add(Box.createRigidArea(new Dimension(5, 0)));
             additionalPanel.add(additionalLabel);
 
             additionalPanel.add(Box.createRigidArea(new Dimension(strutDistance, 0)));
             additionalPanel.add(additionalScrollPane);
 
             centerPanel.add(additionalPanel);
 
             //ButtonPanels
             buttonPanel = new JPanel();
 
             add(buttonPanel, BorderLayout.SOUTH);
 
             buttonPanel.setLayout(
                     new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
             buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15)); //add some space between the right edge of the screen
             buttonPanel.add(Box.createHorizontalGlue());
 
             //cancel-Button
             cancelButton = new JButton("Cancel");
 
             cancelButton.addActionListener(
                     new ActionListener() {
 
                         @Override
                         public void actionPerformed(ActionEvent e) {
                             update();
                             showMainScreenPanel();
                         }
                     });
             buttonPanel.add(cancelButton);
 
             buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
 
             //create-button
             createButton = new JButton("Create");
 
             createButton.addActionListener(
                     new ActionListener() {
 
                         @Override
                         public void actionPerformed(ActionEvent e) {
                             if (!descriptionField.getText().trim().isEmpty()
                                     && !licensePlateField.getText().trim().isEmpty()
                                     && !vinField.getText().trim().isEmpty()
                                     && !drivenField.getText().trim().isEmpty()) {
                                 //Checks if VIN number is in use already
                                 boolean VinTaken = false;
                                 for (Vehicle vehicle : vehicleList) {
                                     if (vinField.getText().trim().equals(vehicle.getVin())) {
                                         VinTaken = true;
                                     }
                                 }
                                 if (!VinTaken) {
                                     try {
                                         Vehicle newVehicle = new Vehicle(CarRental.getInstance().requestNewVehicleId(), vehicleTypes.get(vehicleTypeCombo.getSelectedIndex()).getID(),
                                                 descriptionField.getText().trim(), licensePlateField.getText().trim(),
                                                 vinField.getText().trim(), Integer.parseInt(drivenField.getText().trim()), additionalArea.getText().trim());
 
                                         CarRental.getInstance().saveVehicle(newVehicle);
                                         CarRental.getInstance().appendLog("Vehicle \"" + descriptionField.getText().trim() + "\" added to the database.");
                                         CarRental.getView().displayError("Vehicle \"" + descriptionField.getText().trim() + "\" added to the database.");
                                         vehicleList = CarRental.getInstance().requestVehicles();
                                     } catch (NumberFormatException ex) {
                                         CarRental.getView().displayError("Your \"Distance driven\" field does not consist of numbers only or was too long. The vehicle wasn't created.");
                                     }
 
                                 } else {
                                     CarRental.getView().displayError("A vehicle with VIN \"" + vinField.getText().trim() + "\" already exists.");
                                 }
                             } else {
                                 CarRental.getView().displayError("The vehicle wasn't created. Fill out the text fields.");
                             }
                         }
                     });
             buttonPanel.add(createButton);
         }
 
         /**
          * Updates the panel. Textfields are set blank and the vehicle types are updated.
          */
         public void update() {
             //Check for an added type for the JComboBox
             vehicleTypeComboModel.removeAllElements();
             for (VehicleType vehicleType : vehicleTypes) {
                 vehicleTypeComboModel.addElement(vehicleType.getName());
             }
             //Sets all text fields blank
             descriptionField.setText(null);
             licensePlateField.setText(null);
             vinField.setText(null);
             drivenField.setText(null);
             additionalArea.setText(null);
         }
     }
 
     /**
      * This inner class creates a JPanel which shows a certain vehicle. The vehicle is selected in the ListPanel-class
      */
     public class ViewVehiclePanel extends JPanel {
 
         private JTextField descriptionField, licensePlateField, vinField, drivenField;
         private DefaultComboBoxModel vehicleTypeComboModel;
         private JComboBox vehicleTypeCombo;
         private JTextArea additionalArea;
         private DefaultTableModel reservationTableModel, maintenanceTableModel;
 
         /**
          * Sets up the basic funtionality needed to view a vehicle.
          */
         public ViewVehiclePanel() {
 
             JPanel centerPanel, reservationPanel, maintenancePanel, buttonPanel, vehicleTypePanel, descriptionPanel, licensePlatePanel, vinPanel, drivenPanel, additionalPanel;
             JLabel vehicleTypeLabel, descriptionLabel, licensePlateLabel, vinLabel, drivenLabel, kilometerLabel, additionalLabel;
             JButton backButton, editButton, deleteButton, viewSelectedTypeButton;
             JScrollPane reservationScrollPane, maintenanceScrollPane, additionalScrollPane;
             String[] tableColumn;
             JTable reservationTable, maintenanceTable;
             final int defaultJTextFieldColumns = 20, strutDistance = 0;
 
             //Panel settings
             setLayout(new BorderLayout());
             setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Viewing Vehicle"));
 
             //Center Panel
             centerPanel = new JPanel();
             centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
             centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 40));
             add(centerPanel, BorderLayout.CENTER);
 
             //Colors
             setBackground(new Color(216, 216, 208));
             centerPanel.setBackground(new Color(239, 240, 236));
 
             //Vehicle Type
             vehicleTypeLabel = new JLabel("Vehicle Type");
             vehicleTypeComboModel = new DefaultComboBoxModel();
             vehicleTypeCombo = new JComboBox(vehicleTypeComboModel); //this JComboBox selections are added in the update() method
 
             viewSelectedTypeButton = new JButton("View selected Type");
             viewSelectedTypeButton.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     vehicleTypeToView = vehicleTypes.get(vehicleTypeCombo.getSelectedIndex());
                     showViewTypePanel();
                     CarRental.getView().displayError("Showing vehicle type \"" + vehicleTypeToView.getName() + "\" now.");
                 }
             });
 
             vehicleTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
             vehicleTypePanel.add(Box.createRigidArea(new Dimension(5, 0)));
             vehicleTypePanel.add(vehicleTypeLabel);
 
             vehicleTypePanel.add(Box.createRigidArea(new Dimension(48 + strutDistance, 0)));
             vehicleTypePanel.add(vehicleTypeCombo);
 
             vehicleTypePanel.add(Box.createRigidArea(new Dimension(5 + strutDistance, 0)));
             vehicleTypePanel.add(viewSelectedTypeButton);
 
             centerPanel.add(vehicleTypePanel);
 
             //Description
             descriptionLabel = new JLabel("Description");
             descriptionField = new JTextField(defaultJTextFieldColumns);
             descriptionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
             descriptionPanel.add(Box.createRigidArea(new Dimension(5, 0)));
             descriptionPanel.add(descriptionLabel);
 
             descriptionPanel.add(Box.createRigidArea(new Dimension(55 + strutDistance, 0)));
             descriptionPanel.add(descriptionField);
 
             centerPanel.add(descriptionPanel);
 
             //LicensePlate
             licensePlateLabel = new JLabel("License Plate");
             licensePlateField = new JTextField(defaultJTextFieldColumns);
             licensePlatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
             licensePlatePanel.add(Box.createRigidArea(new Dimension(5, 0)));
             licensePlatePanel.add(licensePlateLabel);
 
             licensePlatePanel.add(Box.createRigidArea(new Dimension(43 + strutDistance, 0)));
             licensePlatePanel.add(licensePlateField);
 
             centerPanel.add(licensePlatePanel);
 
             //VIN
             vinLabel = new JLabel("VIN");
             vinField = new JTextField(defaultJTextFieldColumns);
             vinPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
             vinPanel.add(Box.createRigidArea(new Dimension(5, 0)));
             vinPanel.add(vinLabel);
 
             vinPanel.add(Box.createRigidArea(new Dimension(101 + strutDistance, 0)));
             vinPanel.add(vinField);
 
             centerPanel.add(vinPanel);
 
             //Driven
             drivenLabel = new JLabel("Distance driven");
             drivenField = new JTextField(defaultJTextFieldColumns);
             kilometerLabel = new JLabel("kilometers");
             drivenPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
             drivenPanel.add(Box.createRigidArea(new Dimension(5, 0)));
             drivenPanel.add(drivenLabel);
 
             drivenPanel.add(Box.createRigidArea(new Dimension(32 + strutDistance, 0)));
             drivenPanel.add(drivenField);
             
             drivenPanel.add(kilometerLabel);
 
             centerPanel.add(drivenPanel);
 
             //Additional Comment
             additionalLabel = new JLabel("Additional comments");
             additionalArea = new JTextArea(3, 30);
             additionalScrollPane = new JScrollPane(additionalArea);
 
             additionalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
             additionalPanel.add(Box.createRigidArea(new Dimension(5, 0)));
             additionalPanel.add(additionalLabel);
 
             additionalPanel.add(Box.createRigidArea(new Dimension(strutDistance, 0)));
             additionalPanel.add(additionalScrollPane);
 
             centerPanel.add(additionalPanel);
 
             //Adding a small rigid area
             centerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
 
             //ReservationPanel
             reservationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             reservationPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK, 2), "Reservations"));
             reservationPanel.setBackground(new Color(216, 216, 208));
             centerPanel.add(reservationPanel);
 
             //Creating the reservation table model
             tableColumn = new String[]{"Customer", "Phone number", "From", "To"};
             reservationTableModel = new DefaultTableModel(tableColumn, 0);
 
             //Creating the JTable
             reservationTable = new JTable(reservationTableModel);
 
             reservationScrollPane = new JScrollPane(reservationTable);
 
             //Setting the default size for the table in this scrollpane
             reservationTable.setPreferredScrollableViewportSize(new Dimension(700, 100));
             reservationPanel.add(reservationScrollPane);
 
             //Adding a small rigid area
             centerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
 
             //MaintenancePanel
             maintenancePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             maintenancePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK, 2), "Maintenances"));
             maintenancePanel.setBackground(new Color(216, 216, 208));
             centerPanel.add(maintenancePanel);
 
             //Creating the maintenance table model
             tableColumn = new String[]{"Maintenance type", "Service check", "From", "To"};
             maintenanceTableModel = new DefaultTableModel(tableColumn, 0);
 
             //Creating the JTable
             maintenanceTable = new JTable(maintenanceTableModel);
 
             maintenanceScrollPane = new JScrollPane(maintenanceTable);
 
             //Setting the default size for the table in this scrollpane
             maintenanceTable.setPreferredScrollableViewportSize(new Dimension(700, 100));
             maintenancePanel.add(maintenanceScrollPane);
 
             //ButtonPanels
             buttonPanel = new JPanel();
             add(buttonPanel, BorderLayout.SOUTH);
             buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
             buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15)); //add some space between the right edge of the screen
             buttonPanel.add(Box.createHorizontalGlue());
 
             //Cancel-Button
             backButton = new JButton("Back");
             backButton.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     showListPanel();
                 }
             });
             buttonPanel.add(backButton);
             buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
 
             //Delete-Button
             deleteButton = new JButton("Delete");
             deleteButton.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     CarRental.getInstance().deleteVehicle(vehicleToView.getID());
                     CarRental.getInstance().appendLog("Vehicle \"" + vehicleToView.getDescription() + "\" deleted from the database.");
                     CarRental.getView().displayError("Vehicle \"" + vehicleToView.getDescription() + "\" deleted from the database.");
                     vehicleList = CarRental.getInstance().requestVehicles();
                     showListPanel();
                 }
             });
             buttonPanel.add(deleteButton);
             buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
 
             //Create-button
             editButton = new JButton("Edit");
             editButton.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     assert (vehicleToView != null); //VehicleToView should never be null here
                     if (!descriptionField.getText().trim().isEmpty()
                             && !licensePlateField.getText().trim().isEmpty()
                             && !vinField.getText().trim().isEmpty()
                             && !drivenField.getText().trim().isEmpty()) {
                         //Checks if VIN number is in use already
                         boolean VinTaken = false;
                         for (Vehicle vehicle : vehicleList) {                  //TODO hvorfor virker !vehicle.equals(vehicleToView) ikke her!!
                             if (vinField.getText().trim().equals(vehicle.getVin()) && vehicle.getID() != vehicleToView.getID()) { //if the vin is in use and it´s not from the currently viewed vehicle
                                 VinTaken = true;
                             }
                         }
                         if (!VinTaken) {
                             try {
                                 Vehicle updatedVehicle = new Vehicle(vehicleToView.getID(), vehicleTypes.get(vehicleTypeCombo.getSelectedIndex()).getID(),
                                         descriptionField.getText().trim(), licensePlateField.getText().trim(),
                                         vinField.getText().trim(), Integer.parseInt(drivenField.getText().trim()), additionalArea.getText().trim());
 
                                 CarRental.getInstance().saveVehicle(updatedVehicle);
                                 CarRental.getInstance().appendLog("Vehicle \"" + descriptionField.getText().trim() + "\" changed in the database.");
                                 CarRental.getView().displayError("Vehicle \"" + descriptionField.getText().trim() + "\" changed in the database.");
                                 vehicleList = CarRental.getInstance().requestVehicles();
                             } catch (NumberFormatException ex) {
                                 CarRental.getView().displayError("Your \"Distance driven\" field does not consist of numbers only or was too long. The vehicle wasn't created.");
                             }
                         } else {
                             CarRental.getView().displayError("Another vehicle with VIN \"" + vinField.getText().trim() + "\" already exists.");
                         }
                     }
                 }
             });
             buttonPanel.add(editButton);
         }
 
         /**
          * Updates the panel to show the selected vehicle. This type is selected in the ViewVehiclePanel-class
          */
         public void update() {
             SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
             ArrayList<Reservation> reservations = new ArrayList<Reservation>(); //The bookings are sorted into reservations -
             ArrayList<Maintenance> maintenances = new ArrayList<Maintenance>();    // and maintenances in the code
             String[] tableRow;
             int typeIndex = 0;
 
             //refresh the vehicle types in the Combobox and get the index to be displayed
             vehicleTypeComboModel.removeAllElements();
 
             for (int i = 0;
                     i < vehicleTypes.size();
                     i++) {
                 vehicleTypeComboModel.addElement(vehicleTypes.get(i).getName()); //add the row
                 if (vehicleTypes.get(i).getID() == vehicleToView.getVehicleType()) {
                     typeIndex = i;
                 }
             }
 
             //Refresh the textfields
             vehicleTypeCombo.setSelectedIndex(typeIndex);
 
             descriptionField.setText(vehicleToView.getDescription());
             licensePlateField.setText(vehicleToView.getLicensePlate());
             vinField.setText(vehicleToView.getVin());
             drivenField.setText(Integer.toString(vehicleToView.getOdo()));
             additionalArea.setText(vehicleToView.getAdditional());
 
             //Splits bookings into reservations and maintenances
 
             for (Booking booking : CarRental.getInstance().requestBookingsByVehicle(vehicleToView.getID())) {
                 if (!booking.isMaintenance()) {
                     reservations.add((Reservation) booking);
                 } else {
                     maintenances.add((Maintenance) booking);
                 }
             }
 
             //Removes the old rows before adding the new ones
             reservationTableModel.setRowCount(
                     0); //TODO lol er det her virkelig den eneste måde at tømme tabellen på? reservationTable.removeAll() virker ikke, så prøv ikke den
             maintenanceTableModel.setRowCount(
                     0);
 
             //Add the rows with reservations
 
             for (Reservation reservation : reservations) {
                 tableRow = new String[]{
                     CarRental.getInstance().requestCustomer(reservation.getCustomerID()).getName(),
                     Integer.toString(CarRental.getInstance().requestCustomer(reservation.getCustomerID()).getTelephone()),
                     dateFormat.format(reservation.getTStart()),
                     dateFormat.format(reservation.getTEnd())};
                 reservationTableModel.addRow(tableRow);
             }
 
             assert (reservations.size() == reservationTableModel.getRowCount()) : "size: " + reservations.size() + " rows: " + reservationTableModel.getRowCount();
 
             //Add the rows with maintenances
 
             for (Maintenance maintenance : maintenances) {
                 String serviceCheck;
                 if (CarRental.getInstance().requestMaintenanceType(maintenance.getTypeID()).getIs_service()) {
                     serviceCheck = "Yes";
                 } else {
                     serviceCheck = "No";
                 }
                 tableRow = new String[]{
                     CarRental.getInstance().requestMaintenanceType(maintenance.getTypeID()).getName(),
                     serviceCheck,
                     dateFormat.format(maintenance.getTStart()),
                     dateFormat.format(maintenance.getTEnd())};
                 maintenanceTableModel.addRow(tableRow);
             }
 
             assert (maintenances.size() == maintenanceTableModel.getRowCount()) : "size: " + maintenances.size() + " rows: " + maintenanceTableModel.getRowCount();
         }
     }
 
     /**
      * This inner class creates a JPanel which shows a certain vehicle type. The vehicletype is selected in the ViewVehiclePanel-class
      */
     public class ViewVehicleTypePanel extends JPanel {
 
         private JButton backButton, editButton, deleteButton;
         private VehicleTypePanel vehicleTypePanel;
 
         /**
          * Sets up the basic funtionalit needed to view a vehicle type.
          */
         public ViewVehicleTypePanel() {
 
             //Panel settings
             setLayout(new BorderLayout());
             setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Viewing vehicle type"));
             setBackground(new Color(216, 216, 208));
 
             //Create the panel for viewing the vehicle type.
             vehicleTypePanel = new VehicleTypePanel();
             add(vehicleTypePanel, BorderLayout.CENTER);
 
             //Create the buttons needed
             //Back-button
             backButton = new JButton("Back");
             backButton.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     showViewEntityPanel();
                     CarRental.getView().displayError("Showing vehicle \"" + vehicleToView.getDescription() + "\" now.");
                 }
             });
 
             //Delete-button
             deleteButton = new JButton("Delete");
             deleteButton.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     boolean inUse = false;
                     for (Vehicle vehicle : vehicleList) {
                         if (vehicle.getVehicleType() == vehicleTypeToView.getID()) {
                             inUse = true;
                         }
                     }
                     if (!inUse) {
                         CarRental.getInstance().deleteVehicleType(vehicleTypeToView.getID());
                         CarRental.getInstance().appendLog("Vehicle type \"" + vehicleTypeToView.getName() + "\" deleted from the database.");
                         CarRental.getView().displayError("Vehicle type \"" + vehicleTypeToView.getName() + "\" deleted from the database.");
                         vehicleTypes = CarRental.getInstance().requestVehicleTypes();
                         showViewEntityPanel();
                     } else {
                         CarRental.getView().displayError("Vehicle type \"" + vehicleTypeToView.getName() + "\" is in use by at least one car. Could not be deleted.");
                     }
                 }
             });
 
             // Edit-button
             editButton = new JButton("Edit");
             editButton.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                    ArrayList<JTextComponent> vehicleTypeTextList = vehicleTypePanel.getTextComponents();
                     if (!vehicleTypeTextList.get(0).getText().trim().isEmpty()
                             && !vehicleTypeTextList.get(1).getText().trim().isEmpty()
                             && !vehicleTypeTextList.get(2).getText().trim().isEmpty()) {
                         //Checks if name is in use already
                         boolean nameTaken = false;
                         for (VehicleType vehicleType : vehicleTypes) {
                             if (vehicleTypeTextList.get(0).getText().trim().equals(vehicleType.getName()) && vehicleType.getID() != vehicleTypeToView.getID()) { //if the name is in use and it´s not from the currently viewed vehicle
                                 nameTaken = true;
                             }
                         }
                         if (!nameTaken) {
                             try {
                                 VehicleType updatedVehicleType = new VehicleType(vehicleTypeToView.getID(), vehicleTypeTextList.get(0).getText().trim(), vehicleTypeTextList.get(2).getText().trim(),
                                         Integer.parseInt(vehicleTypeTextList.get(1).getText().trim()));
 
                                 CarRental.getInstance().saveVehicleType(updatedVehicleType);
                                 CarRental.getInstance().appendLog("Vehicle type \"" + vehicleTypeTextList.get(0).getText().trim() + "\" changed in the database.");
                                 CarRental.getView().displayError("Vehicle type \"" + vehicleTypeTextList.get(0).getText().trim() + "\" changed in the database.");
                                 vehicleTypes = CarRental.getInstance().requestVehicleTypes(); //update ment for if name check is implemented
                             } catch (NumberFormatException ex) {
                                 CarRental.getView().displayError("Your \"price per day\" field does not consist of numbers only or was too long. The vehicle type wasn't created.");
                             }
                         } else {
                             CarRental.getView().displayError("Another vehicle type with the name \"" + vehicleTypeTextList.get(0).getText().trim() + "\" already exists.");
                         }
                     } else {
                         CarRental.getView().displayError("The vehicle type wasn't edited. You need to enter text in all the fields.");
                     }
                 }
             });
         }
 
         /**
          * Updates the panel to show the selected vehicle type. This type is selected in the ViewVehiclePanel-class
          */
         public void update() {
             vehicleTypePanel.setPanel(vehicleTypeToView, backButton, deleteButton, editButton);
         }
     }
 
     /**
      * This inner class creates a JPanel with the functionality to create a new vehicle type.
      */
     public class AddTypePanel extends JPanel {
 
         private JButton cancelButton, createButton;
         private VehicleTypePanel vehicleTypePanel;
 
         /**
          * Sets up the basic functionality needed to create a new vehicle type.
          */
         public AddTypePanel() {
             //Panel settings
             setLayout(new BorderLayout());
             setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Create a vehicle type"));
             setBackground(new Color(216, 216, 208));
             //Create the panel for viewing the vehicle type.
             vehicleTypePanel = new VehicleTypePanel();
 
             //Create the buttons needed
             //Cancel-button
             cancelButton = new JButton("Cancel");
             cancelButton.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     vehicleTypePanel.setPanel(null, null, null, null); //resets the panel
                     showMainScreenPanel();
                 }
             });
 
             // Create-button
             createButton = new JButton("Create");
             createButton.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     ArrayList<JTextComponent> vehicleTypeTextList = vehicleTypePanel.getTextComponents();
                     if (!vehicleTypeTextList.get(0).getText().trim().isEmpty()
                             && !vehicleTypeTextList.get(1).getText().trim().isEmpty()
                             && !vehicleTypeTextList.get(2).getText().trim().isEmpty()) {
                         //Checks if name is in use already
                         boolean nameTaken = false;
                         for (VehicleType vehicleType : vehicleTypes) {
                             if (vehicleTypeTextList.get(0).getText().trim().equals(vehicleType.getName())) {
                                 nameTaken = true;
                             }
                         }
                         if (!nameTaken) {
                             try {
                                 VehicleType newVehicleType = new VehicleType(CarRental.getInstance().requestNewVehicleTypeId(), vehicleTypeTextList.get(0).getText().trim(), vehicleTypeTextList.get(2).getText().trim(),
                                         Integer.parseInt(vehicleTypeTextList.get(1).getText().trim()));
 
                                 CarRental.getInstance().saveVehicleType(newVehicleType);
                                 CarRental.getInstance().appendLog("Vehicle type \"" + vehicleTypeTextList.get(0).getText().trim() + "\" added to the database.");
                                 CarRental.getView().displayError("Vehicle type \"" + vehicleTypeTextList.get(0).getText().trim() + "\" added to the database.");
                                 vehicleTypes = CarRental.getInstance().requestVehicleTypes();
                             } catch (NumberFormatException ex) {
                                 CarRental.getView().displayError("Your \"price per day\" field does not consist of numbers only or was too long. The vehicle type wasn't created.");
                             }
                         } else {
                             CarRental.getView().displayError("A vehicle type with the name \"" + vehicleTypeTextList.get(0).getText().trim() + "\" already exists.");
                         }
                     } else {
                         CarRental.getView().displayError("The vehicle type wasn't created. You need to enter text in all the fields.");
                     }
                 }
             });
             vehicleTypePanel.setPanel(null, cancelButton, null, createButton);
             add(vehicleTypePanel, BorderLayout.CENTER);
         }
     }
 
     /**
      * This inner class creates a JPanel with a list of vehicles. It is possible to search in the list.
      */
     public class ListPanel extends JPanel {
 
         private DefaultTableModel vehicleTableModel;
         private JComboBox vehicleTypeCombo;
         private DefaultComboBoxModel vehicleTypeComboModel;
         private JTextField descriptionField, licensePlateField, vinField, drivenField;
         private int currentVehicleTypeIndex = -1; //this is for storing the currently selected choice from the combobox.
 
         /**
          * Sets up the basic functionality needed to show the list of vehicles.
          */
         public ListPanel() {
 
             JPanel centerPanel, vehicleListPanel, filterPanel, topFilterPanel, middleFilterPanel, bottomFilterPanel, buttonPanel;
             JLabel vehicleTypeLabel, descriptionLabel, licensePlateLabel, vinLabel, drivenLabel;
             JButton cancelButton, viewButton;
             final JTable vehicleTable;
             JScrollPane listScrollPane;
             final int defaultJTextFieldColumns = 20, strutDistance = 0;
 
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
                     showMainScreenPanel();
                 }
             });
             buttonPanel.add(cancelButton);
             buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
 
             //View-button
             viewButton = new JButton("View selected");
             viewButton.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     if (vehicleTable.getSelectedRow() >= 0) { //getSelectedRow returns -1 if no row is selected
                         for (Vehicle vehicle : vehicleList) {
                             if (vehicle.getVin().equals(vehicleTableModel.getValueAt(vehicleTable.getSelectedRow(), 3))) {
                                 vehicleToView = vehicle;
                                 break;
                             }
                         }
                         showViewEntityPanel();
                         CarRental.getView().displayError("Showing vehicle \"" + vehicleToView.getDescription() + "\" now.");
                     }
                 }
             });
             buttonPanel.add(viewButton);
         }
 
         /**
          * Updates the panel to show an updated list of vehicles.
          */
         public void update() {
 
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
 }
