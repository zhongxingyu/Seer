 package carrental;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import javax.swing.*;
 import javax.swing.border.*;
 
 /**
  * This is the main panel regarding vehicles.
  * It contains JPanels for every relevant screen, when dealing with vehicles.
  * These are implemented as inner classes.
  * @author CNN
  */
 public class VehiclePanel extends SuperPanel {
 
     private JPanel mainScreenPanel, createPanel, viewVehiclePanel, addTypePanel, listPanel;
     private JScrollPane centerScrollPane_View;
     VehicleTypePanel vehicleTypeInstance;
 
     public VehiclePanel() {
         vehicleTypeInstance = new VehicleTypePanel(); //Used to get the addType-panel from the VehicleTypeClass - to avoid some code duplication
         RemakeAll();
         //Sets the different subpanels (defined as inner classes below). Also adds them to this object with JPanel.add().
         AssignAndAddSubPanels(mainScreenPanel, createPanel, viewVehiclePanel, addTypePanel, listPanel);
         this.setPreferredSize(new Dimension(800,600));
 
         //Removes the default gaps between components
         setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));

        setBackground(Color.BLACK);
         showMainScreenPanel();
     }
 
     //Temporary Main
     public static void main(String[] args) {
         JFrame frame = new JFrame("VehicleFrame");
         frame.setPreferredSize(new Dimension(800, 600));
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         Container contentPane = frame.getContentPane();
         contentPane.add(new VehiclePanel());
         frame.pack();
         frame.setVisible(true);
 
     }
 
     @Override
     public void makeMainScreenPanel() {
         mainScreenPanel = new JPanel();
         JButton createButton, addTypeButton, listButton, viewVehicleButton;
         JPanel centerPanel, buttonFlowPanel, buttonGridPanel;
         TitledBorder titleBorder;
         //Panel settings
         mainScreenPanel.setLayout(new BorderLayout());
         titleBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Vehicles (Overview)");
         mainScreenPanel.setBorder(titleBorder);
 
         centerPanel = new JPanel();
         mainScreenPanel.add(centerPanel, BorderLayout.CENTER);
         //Button panel with a gridlayout for vertical alignment.
         buttonGridPanel = new JPanel();
         buttonGridPanel.setLayout(new BoxLayout(buttonGridPanel, BoxLayout.PAGE_AXIS));
         //extra buttonpanel with a default flowlayout, to shrink the button to minimum size,
         buttonFlowPanel = new JPanel();
         buttonFlowPanel.add(buttonGridPanel);
         centerPanel.add(buttonFlowPanel);
         //Colors
         mainScreenPanel.setBackground(new Color(216, 216, 208));
         centerPanel.setBackground(new Color(239, 240, 236));
         //Create-button
         createButton = new JButton("Create a new vehicle");
         createButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 showCreatePanel();
             }
         });
 
         buttonGridPanel.add(createButton);
         //Create some blank space between the buttons:
         buttonGridPanel.add(Box.createRigidArea(new Dimension(0, 10)));
 
         //addType-Button
         addTypeButton = new JButton("Add a new vehicle type");
         addTypeButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 showAddTypePanel();
             }
         });
         buttonGridPanel.add(addTypeButton);
         //Create some blank space between the buttons:
         buttonGridPanel.add(Box.createRigidArea(new Dimension(0, 10)));
 
         //View vehicle-Button
         viewVehicleButton = new JButton("View a vehicle");
         viewVehicleButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 showViewEntityPanel();
             }
         });
         buttonGridPanel.add(viewVehicleButton);
         //Create some blank space between the buttons:
         buttonGridPanel.add(Box.createRigidArea(new Dimension(0, 10)));
 
         //Show list-Button
         listButton = new JButton("Show a list of vehicles");
         listButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 showListPanel();
             }
         });
         buttonGridPanel.add(listButton);
 
     }
 
     @Override
     public void makeCreatePanel() {
         createPanel = new JPanel();
         JPanel centerPanel, buttonPanel, vehicleTypePanel, namePanel, licensePlatePanel, vinPanel, drivenPanel, serviceDistancePanel, additionalPanel;
         JLabel vehicleTypeLabel, nameLabel, licensePlateLabel, vinLabel, drivenLabel, serviceDistanceLabel, additionalLabel;
         JComboBox vehicleTypeCombo;
         JTextField nameField, licensePlateField, vinField, drivenField, serviceDistanceField;
         JTextArea additionalArea;
         JScrollPane centerScrollPane;
         JButton createButton, cancelButton;
         final String[] temporaryTypes = {"Station Car", "Truck", "Optimus Prime"};
         final int defaultJTextFieldColumns = 20, strutDistance = 0;
 
         //Panel settings
         createPanel.setLayout(new BorderLayout());
         createPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Create a vehicle"));
         //Center Panel
 
         centerPanel = new JPanel();
         centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
         centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 40));
 
         //Center ScrollPane
         centerScrollPane = new JScrollPane(centerPanel);
         createPanel.add(centerScrollPane, BorderLayout.CENTER);
 
         //Colors
         createPanel.setBackground(new Color(216, 216, 208));
         centerPanel.setBackground(new Color(239, 240, 236));
 
         //Vehicle Type
         vehicleTypeLabel = new JLabel("Vehicle Type");
         vehicleTypeCombo = new JComboBox(temporaryTypes);
         vehicleTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
         vehicleTypePanel.add(Box.createHorizontalStrut(5));
         vehicleTypePanel.add(vehicleTypeLabel);
         vehicleTypePanel.add(Box.createHorizontalStrut(48 + strutDistance));
         vehicleTypePanel.add(vehicleTypeCombo);
         centerPanel.add(vehicleTypePanel);
 
         //Name
         nameLabel = new JLabel("Name");
         nameField = new JTextField(defaultJTextFieldColumns);
         namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
         namePanel.add(Box.createHorizontalStrut(5));
         namePanel.add(nameLabel);
         namePanel.add(Box.createHorizontalStrut(87 + strutDistance));
         namePanel.add(nameField);
         centerPanel.add(namePanel);
 
 
         //LicensePlate
         licensePlateLabel = new JLabel("License Plate");
         licensePlateField = new JTextField(defaultJTextFieldColumns);
         licensePlatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
         licensePlatePanel.add(Box.createHorizontalStrut(5));
         licensePlatePanel.add(licensePlateLabel);
         licensePlatePanel.add(Box.createHorizontalStrut(43 + strutDistance));
         licensePlatePanel.add(licensePlateField);
         centerPanel.add(licensePlatePanel);
 
         //VIN
         vinLabel = new JLabel("VIN");
         vinField = new JTextField(defaultJTextFieldColumns);
         vinPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
         vinPanel.add(Box.createHorizontalStrut(5));
         vinPanel.add(vinLabel);
         vinPanel.add(Box.createHorizontalStrut(101 + strutDistance));
         vinPanel.add(vinField);
         centerPanel.add(vinPanel);
 
         //Driven
         drivenLabel = new JLabel("Distance driven");
         drivenField = new JTextField(defaultJTextFieldColumns);
         drivenPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
         drivenPanel.add(Box.createHorizontalStrut(5));
         drivenPanel.add(drivenLabel);
         drivenPanel.add(Box.createHorizontalStrut(32 + strutDistance));
         drivenPanel.add(drivenField);
         centerPanel.add(drivenPanel);
 
         //Distance untill next servicecheck. 
 //        serviceDistanceLabel = new JLabel("Distance to service");
 //        serviceDistanceField = new JTextField(defaultJTextFieldColumns);
 //        serviceDistancePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 //
 //        serviceDistancePanel.add(Box.createHorizontalStrut(5));
 //        serviceDistancePanel.add(serviceDistanceLabel);
 //        serviceDistancePanel.add(Box.createHorizontalStrut(11 + strutDistance));
 //        serviceDistancePanel.add(serviceDistanceField);
 //        centerPanel.add(serviceDistancePanel);
 
         //Additional Comment
         additionalLabel = new JLabel("Additional comments");
         additionalArea = new JTextArea(3, 30);
         additionalArea.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
         additionalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
         additionalPanel.add(Box.createHorizontalStrut(5));
         additionalPanel.add(additionalLabel);
         additionalPanel.add(Box.createHorizontalStrut(strutDistance));
         additionalPanel.add(additionalArea);
         centerPanel.add(additionalPanel);
 
         //ButtonPanels
         buttonPanel = new JPanel();
         createPanel.add(buttonPanel, BorderLayout.SOUTH);
         buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
         buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15)); //add some space between the right edge of the screen
         buttonPanel.add(Box.createHorizontalGlue());
 
         //cancel-Button
         cancelButton = new JButton("Cancel");
         cancelButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 //TODO NICLASONLY Set text() for all fields aka blank
                 showMainScreenPanel();
             }
         });
         buttonPanel.add(cancelButton);
         buttonPanel.add(Box.createHorizontalStrut(5));
 
         //create-button
         createButton = new JButton("Create");
         createButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 //TODO NICLASONLY make the database update here
             }
         });
         buttonPanel.add(createButton);
     }
 
     @Override
     public void makeViewEntityPanel() {
         viewVehiclePanel = new JPanel();
         JPanel centerPanel, reservationPanel, buttonPanel, vehicleTypePanel, namePanel, licensePlatePanel, vinPanel, drivenPanel, serviceDistancePanel, additionalPanel;
         JLabel vehicleTypeLabel, nameLabel, licensePlateLabel, vinLabel, drivenLabel, serviceDistanceLabel, additionalLabel;
         JTextField vehicleTypeField, nameField, licensePlateField, vinField, drivenField, serviceDistanceField;
         JTextArea additionalArea;
         JButton backButton;
         final int defaultJTextFieldColumns = 20, strutDistance = 0;
         //some temporary strings for testing the GUI
         String vehicleTypeString, nameString, licensePlateString, vinString, drivenString, serviceDistanceString, additionalString;
 
 
         //Panel settings
         viewVehiclePanel.setLayout(new BorderLayout());
         viewVehiclePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Viewing Vehicle"));
         //Center Panel
         centerPanel = new JPanel();
         centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
         centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 40));
 
         //Center ScrollPane
         centerScrollPane_View = new JScrollPane(centerPanel);
         viewVehiclePanel.add(centerScrollPane_View, BorderLayout.CENTER);
 
         //Colors
         viewVehiclePanel.setBackground(new Color(216, 216, 208));
         centerPanel.setBackground(new Color(239, 240, 236));
 
         //Vehicle Type
         vehicleTypeLabel = new JLabel("Vehicle Type");
         vehicleTypeString = "Station Car";
         vehicleTypeField = new JTextField(vehicleTypeString, defaultJTextFieldColumns);
         vehicleTypeField.setEditable(false);
         vehicleTypeField.setBackground(Color.WHITE);
         vehicleTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
         vehicleTypePanel.add(Box.createHorizontalStrut(5));
         vehicleTypePanel.add(vehicleTypeLabel);
         vehicleTypePanel.add(Box.createHorizontalStrut(48 + strutDistance));
         vehicleTypePanel.add(vehicleTypeField);
         centerPanel.add(vehicleTypePanel);
 
         //Name
         nameLabel = new JLabel("Name");
         nameString = "Citröen C5";
         nameField = new JTextField(nameString, defaultJTextFieldColumns);
         nameField.setEditable(false);
         nameField.setBackground(Color.WHITE);
         namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
         namePanel.add(Box.createHorizontalStrut(5));
         namePanel.add(nameLabel);
         namePanel.add(Box.createHorizontalStrut(87 + strutDistance));
         namePanel.add(nameField);
         centerPanel.add(namePanel);
 
 
         //LicensePlate
         licensePlateLabel = new JLabel("License Plate");
         licensePlateString = "ZYX 547262";
         licensePlateField = new JTextField(licensePlateString, defaultJTextFieldColumns);
         licensePlateField.setEditable(false);
         licensePlateField.setBackground(Color.WHITE);
         licensePlatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
         licensePlatePanel.add(Box.createHorizontalStrut(5));
         licensePlatePanel.add(licensePlateLabel);
         licensePlatePanel.add(Box.createHorizontalStrut(43 + strutDistance));
         licensePlatePanel.add(licensePlateField);
         centerPanel.add(licensePlatePanel);
 
         //VIN
         vinLabel = new JLabel("VIN");
         vinString = "4241424421";
         vinField = new JTextField(vinString, defaultJTextFieldColumns);
         vinField.setEditable(false);
         vinField.setBackground(Color.WHITE);
         vinPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
         vinPanel.add(Box.createHorizontalStrut(5));
         vinPanel.add(vinLabel);
         vinPanel.add(Box.createHorizontalStrut(101 + strutDistance));
         vinPanel.add(vinField);
         centerPanel.add(vinPanel);
 
         //Driven
         drivenLabel = new JLabel("Distance driven");
         drivenString = "3000 miles";
         drivenField = new JTextField(drivenString, defaultJTextFieldColumns);
         drivenField.setEditable(false);
         drivenField.setBackground(Color.WHITE);
         drivenPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
         drivenPanel.add(Box.createHorizontalStrut(5));
         drivenPanel.add(drivenLabel);
         drivenPanel.add(Box.createHorizontalStrut(32 + strutDistance));
         drivenPanel.add(drivenField);
         centerPanel.add(drivenPanel);
 
         //Distance untill next servicecheck. 
 //        serviceDistanceLabel = new JLabel("Distance to service");
 //        serviceDistanceString = "5.500 miles";
 //        serviceDistanceField = new JTextField(serviceDistanceString, defaultJTextFieldColumns);
 //        serviceDistanceField.setEditable(false);
 //        serviceDistanceField.setBackground(Color.WHITE);
 //        serviceDistancePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 //
 //        serviceDistancePanel.add(Box.createHorizontalStrut(5));
 //        serviceDistancePanel.add(serviceDistanceLabel);
 //        serviceDistancePanel.add(Box.createHorizontalStrut(11 + strutDistance));
 //        serviceDistancePanel.add(serviceDistanceField);
 //        centerPanel.add(serviceDistancePanel);
 
         //Additional Comment
         additionalLabel = new JLabel("Additional comments");
         additionalString = "This is like the best car ever. \n Not joking. \n SO AWESOME!!!";
         additionalArea = new JTextArea(additionalString, 3, 30);
         additionalArea.setEditable(false);
         additionalArea.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
         additionalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
         additionalPanel.add(Box.createHorizontalStrut(5));
         additionalPanel.add(additionalLabel);
         additionalPanel.add(Box.createHorizontalStrut(strutDistance));
         additionalPanel.add(additionalArea);
         centerPanel.add(additionalPanel);
 
         //ReservationPanel
         reservationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
         reservationPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK, 2), "Reservations"));
         centerPanel.add(reservationPanel);
 //        //ReservationTablePanel
 //        reservationTablePanel = new JPanel();
 //        reservationTablePanel.setBackground(Color.GREEN);
 //        reservationTablePanel.setBorder(BorderFactory.createEmptyBorder(5, 30, 5, 30));
 
 
 
 
         //TEMP - simulating that it has an arraylist with customers - skipped
 //
 //        Customer testCustomer = new Customer(100, 88427362, "Poul Poulsen", "Bossengade 26 \n 2630 \n Taastrup", "poul@gmail.com");
 //        Customer testCustomer2 = new Customer(130, 75834920, "Jens Jensen", "Johnsgade 30 \n 2630 \n Taastrup", "poul@gmail.com");
 //        ArrayList<Customer> customerArrayList = new ArrayList<Customer>();
 //        customerArrayList.add(testCustomer);
 //        customerArrayList.add(testCustomer2);
 //
 //
 //        Reservation testReservation = new Reservation(100, 3, Timestamp.valueOf("1991-12-24 13:37:00"), Timestamp.valueOf("1991-12-31 13:37:00"), 2);
 //        Reservation testReservation2 = new Reservation(20, 2, Timestamp.valueOf("1913-06-13 13:37:00"), Timestamp.valueOf("2010-03-25 13:37:00"), 3);
 //        //Customer testCustomer2 = new Customer(130, 75834920, "Jens Jensen", "Johnsgade 30 \n 2630 \n Taastrup", "poul@gmail.com");
 //        ArrayList<Reservation> reservationArrayList = new ArrayList<Reservation>();
 //        reservationArrayList.add(testReservation);
 //        reservationArrayList.add(testReservation2);
 
         //Testing Table setup
         Object[] ColumnNames = {"Customer", "Phone number", "From", "To"};
         ArrayList<ArrayList<String>> reservationData = new ArrayList<>();
         ArrayList<String> rowData = new ArrayList<>();
         //getting the data in the arrayList - this might be unnecessary in final implementation depending on how this class receives the simple type objects.
         Customer testCustomer = new Customer(130, 75834920, "Jens Jensen", "Johnsgade 30 \n 2630 \n Taastrup", "poul@gmail.com");
         Reservation testReservation = new Reservation(100, 3, Timestamp.valueOf("1991-12-24 13:37:00"), Timestamp.valueOf("1991-12-31 13:37:00"), 2);
         for (int i = 0; i < 10; i++) {
             rowData.add(testCustomer.getName());
             rowData.add("" + testCustomer.getTelephone());
             rowData.add("" + testReservation.getTStart());
             rowData.add("" + testReservation.getTEnd());
             reservationData.add(rowData);
             //reservation.get(i).size() should be equal to columnNames.length here.
         }
 
         //Converting to Object[][] for the JTable
         Object[][] tableData = new Object[reservationData.size()][ColumnNames.length];
         for (int i = 0; i < reservationData.size(); i++) { //  'i' represents a row
             for (int j = 0; j < ColumnNames.length; j++) { //'j' represents a certain cell on row 'i'
                 tableData[i][j] = reservationData.get(i).get(j); //out of bounds cannot happen because of the conditions in the for loops.
             }
         }
         //Creating the table
         JTable reservationTable = new JTable(tableData, ColumnNames);
         //adding it to it's own scrollpane
         JScrollPane scrollPane = new JScrollPane(reservationTable);
         //Setting the default size for the scrollpane
         reservationTable.setPreferredScrollableViewportSize(new Dimension(500, 120));
         reservationPanel.add(scrollPane);
 
         //ButtonPanels
         buttonPanel = new JPanel();
         viewVehiclePanel.add(buttonPanel, BorderLayout.SOUTH);
         buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
         buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15)); //add some space between the right edge of the screen
         buttonPanel.add(Box.createHorizontalGlue());
 
         //cancel-Button
         backButton = new JButton("Back");
         backButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 //TODO NICLASONLY Set text() for all fields aka blank
                 showMainScreenPanel();
             }
         });
         buttonPanel.add(backButton);
 
     }
 
     @Override
     public void makeAddTypePanel() { //TODO Try to fix code duplication
         //The functionality here is pretty much implemented in VehicleType's createPanel()-method. I'm using the centerpanel from there, but I'm writing the 
         //buttons + border etc. again, as I can't figure out how to reuse it all (Cancel has a new function here) 
         JPanel buttonPanel, centralpanel;
         JScrollPane scrollPane;
         JButton cancelButton, createButton;
         addTypePanel = new JPanel();
 
         addTypePanel.setLayout(new BorderLayout());
         addTypePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Create a vehicle type"));
         //Colors
         addTypePanel.setBackground(new Color(216, 216, 208));
         //Get the centerPanel used to create a VehicleType from VehicleTypePanel.
         centralpanel = vehicleTypeInstance.getCenterJPanel_create();
         centralpanel.setVisible(true);
         scrollPane = new JScrollPane(centralpanel);
         addTypePanel.add(scrollPane);
 
 
         //ButtonPanels
         buttonPanel = new JPanel();
         //Add the scrollpane to the mainPanel of the Create-functionality
         addTypePanel.add(buttonPanel, BorderLayout.SOUTH);
         buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
         buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15)); //add some space between the right edge of the screen
         buttonPanel.add(Box.createHorizontalGlue());
         //cancel-Button
         cancelButton = new JButton("Cancel");
         cancelButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 //TODO NICLASONLY Set text() for all fields aka blank
                 showMainScreenPanel();
             }
         });
         buttonPanel.add(cancelButton);
         buttonPanel.add(Box.createHorizontalStrut(5));
 
         //create-button
         createButton = new JButton("Create");
         createButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 //TODO NICLASONLY make the database update here
             }
         });
         buttonPanel.add(createButton);
     }
 
     @Override
     public void makeListPanel() {
         listPanel = new JPanel();
         JPanel centerPanel, vehicleListPanel, filterPanel, topFilterPanel, middleFilterPanel, bottomFilterPanel, buttonPanel;
         JScrollPane scrollPane;
         JTable vehicleTable;
         JLabel vehicleTypeLabel, nameLabel, licensePlateLabel, vinLabel, drivenLabel, serviceDistanceLabel; // make "additional" search filter too?
         JComboBox vehicleTypeCombo;
         JTextField nameField, licensePlateField, vinField, drivenField, serviceDistanceField;
         JButton cancelButton, filterButton;
         final int defaultJTextFieldColumns = 20, strutDistance = 0;
         final String[] temporaryTypes = {"Station Car", "Truck", "Optimus Prime"};
 
 
         //Panel settings
         listPanel.setLayout(new BorderLayout());
         listPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "List of vehicles"));
         //CenterPanel
         centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
         centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
         centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 40));
         listPanel.add(centerPanel, BorderLayout.CENTER);
         //VehicleListPanel.
         vehicleListPanel = new JPanel();
         //Colors
         listPanel.setBackground(new Color(216, 216, 208));
 
         //Testing Table setup
         Object[] ColumnNames = {"Type", "Name", "LicensePlate", "VIN", "Distance driven"}; //, "Distance to service"
         ArrayList<ArrayList<String>> vehicleData = new ArrayList<ArrayList<String>>();
         ArrayList<String> rowData = new ArrayList<>();
         //getting the data in the arrayList - this might be unnecessary in final implementation depending on how this class receives the simple type objects.
         Vehicle testVehicle = new Vehicle(100, 3, "Citröen Berlingo", "HJX-47362", "943843hfjhdf", 40, "This is additional");
         for (int i = 0; i < 30; i++) {
             rowData.add("" + testVehicle.getVehicleType());
             rowData.add(testVehicle.getDescription());
             rowData.add(testVehicle.getLicensplate());
             rowData.add("" + testVehicle.getVIN());
             rowData.add("" + testVehicle.getOdo());
             // distance to service rowData.add("7.290");
             vehicleData.add(rowData);
             //reservation.get(i).size() should be equal to columnNames.length here.
         }
         ArrayList<String> test1 = new ArrayList<>();
         //Converting to Object[][] for the JTable
         Object[][] tableData = new Object[vehicleData.size()][ColumnNames.length];
         for (int i = 0; i < vehicleData.size(); i++) { //  'i' represents a row
             for (int j = 0; j < ColumnNames.length; j++) { //'j' represents a certain cell on row 'i'
                 tableData[i][j] = vehicleData.get(i).get(j); //out of bounds cannot happen because of the conditions in the for loops.
             }
         }
         //Creating the table
         vehicleTable = new JTable(tableData, ColumnNames);
         //adding it to it's own scrollpane
         scrollPane = new JScrollPane(vehicleTable);
         //Setting the default size for the scrollpane
         vehicleTable.setPreferredScrollableViewportSize(new Dimension(680, 200));
         vehicleListPanel.add(scrollPane);
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
         vehicleTypeCombo = new JComboBox(temporaryTypes);
 
         topFilterPanel.add(vehicleTypeLabel);
         topFilterPanel.add(Box.createHorizontalStrut(16 + strutDistance));
         topFilterPanel.add(vehicleTypeCombo);
         topFilterPanel.add(Box.createHorizontalStrut(105));
 
         //Name
         nameLabel = new JLabel("Name");
         nameField = new JTextField(defaultJTextFieldColumns);
 
         topFilterPanel.add(Box.createHorizontalStrut(5));
         topFilterPanel.add(nameLabel);
         topFilterPanel.add(Box.createHorizontalStrut(77 + strutDistance));
         topFilterPanel.add(nameField);
 
         //Middle Filter panel
         middleFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
         filterPanel.add(middleFilterPanel);
 
         //LicensePlate
         licensePlateLabel = new JLabel("License Plate");
         licensePlateField = new JTextField(defaultJTextFieldColumns);
 
         middleFilterPanel.add(licensePlateLabel);
         middleFilterPanel.add(Box.createHorizontalStrut(11 + strutDistance));
         middleFilterPanel.add(licensePlateField);
 
 
         //VIN
         vinLabel = new JLabel("VIN");
         vinField = new JTextField(defaultJTextFieldColumns);
 
         middleFilterPanel.add(Box.createHorizontalStrut(5));
         middleFilterPanel.add(vinLabel);
         middleFilterPanel.add(Box.createHorizontalStrut(90 + strutDistance));
         middleFilterPanel.add(vinField);
 
         //Bottom Filter panel
         bottomFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
         filterPanel.add(bottomFilterPanel);
 
         //Driven
         drivenLabel = new JLabel("Distance driven");
         drivenField = new JTextField(defaultJTextFieldColumns);
 
         bottomFilterPanel.add(drivenLabel);
         bottomFilterPanel.add(Box.createHorizontalStrut(strutDistance));
         bottomFilterPanel.add(drivenField);
 
         //Distance untill next servicecheck. 
 //        serviceDistanceLabel = new JLabel("Distance to service");
 //        serviceDistanceField = new JTextField(defaultJTextFieldColumns);
 //
 //        bottomFilterPanel.add(Box.createHorizontalStrut(5));
 //        bottomFilterPanel.add(serviceDistanceLabel);
 //        bottomFilterPanel.add(Box.createHorizontalStrut(strutDistance));
 //        bottomFilterPanel.add(serviceDistanceField);
 
         //ButtonPanels
         buttonPanel = new JPanel();
         listPanel.add(buttonPanel, BorderLayout.SOUTH);
         buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
         buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15)); //add some space between the right edge of the screen
         buttonPanel.add(Box.createHorizontalGlue());
 
         //cancel-Button
         cancelButton = new JButton("Back");
         cancelButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 //TODO NICLASONLY Set text() for all fields aka blank
                 showMainScreenPanel();
             }
         });
         buttonPanel.add(cancelButton);
         buttonPanel.add(Box.createHorizontalStrut(5));
 
         //filter-button
         filterButton = new JButton("Filter");
         filterButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 //TODO NICLASONLY make the database update here
             }
         });
         buttonPanel.add(filterButton);
 
 
     }
 }
