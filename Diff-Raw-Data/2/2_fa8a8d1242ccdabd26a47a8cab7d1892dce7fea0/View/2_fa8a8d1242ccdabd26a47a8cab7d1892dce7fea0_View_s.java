 package carrental;
 
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import java.awt.Font.*;
 import java.util.Calendar;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  * GUI for CarRental project
  * @author CNN
  * @version 2011-12-08
  */
 public class View {
     private JFrame frame;
     private final String title = "CarRental Project :: Renting Cars Made Easy";
     private MainPanel main;
     
     public View() {
         CarRental.getInstance().appendLog("Creating View...");
         
         frame = new JFrame(title);
         
         main = new MainPanel();
         frame.add(main);
         
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.pack();
         frame.setResizable(false);
         frame.setVisible(true);
         
         CarRental.getInstance().appendLog("View created.");
     }
     
     /**
      * Get the reservation panel used
      * @return reservation panel
      */
     public ReservationPanel getReservationPanel() {
         return main.getReservationPanel();
     }
     
     /*
      * Get the maintenance panel used
      * @return maintenance panel
      */
     public MaintenancePanel getMaintenancePanel() {
         return main.getMaintenancePanel();
     }
     
     /**
      * Get the customer panel used
      * @return customer panel
      */
     public CustomerPanel getCustomerPanel() {
         return main.getCustomerPanel();
     }
     
     /**
      * Get the vehicle panel used
      * @return vehicle panel
      */
     public VehiclePanel getVehiclePanel() {
         return main.getVehiclePanel();
     }
     
     /**
      * Sets the viewed panel to the customer panel
      */
     public void viewCustomer() {
         main.selectCustomer();
     }
     
     /**
      * Sets the viewed panel to the reservation panel
      */
     public void viewReservation() {
         main.selectReservation();
     }
     
     /**
      * Sets the viewed panel to the maintenance panel
      */
     public void viewMaintenance() {
         main.selectMaintenance();
     }
     
     /**
      * Sets the viewed panel to the vehicle panel
      */
     public void viewVehicle() {
         main.selectVehicle();
     }
     
     /**
      * Sets the current error message to be displayed.
      * @param error_msg error message to be displayed.
      */
     public void displayError(String error_msg) {
         main.setErrorLabelMessage(error_msg);
     }
     
     /**
      * MainPanel is the primary panel of CarRental - it shows the static portion
      * of the GUI and displays the different panels in its center space.
      */
     class MainPanel extends JPanel {
         private JPanel west = new JPanel(),
                 west_inner = new JPanel(),
                 south = new JPanel(),
                 center_super = new JPanel(),
                 center_north = new JPanel(),
                 center = new JPanel(),
                 northReservation = new JPanel(),
                 northCustomer = new JPanel(),
                 northVehicle = new JPanel(),
                 northMaintenance = new JPanel();
         private MaintenancePanel maintenancePanel = new MaintenancePanel();
         private ReservationPanel reservationPanel = new ReservationPanel();
         private CustomerPanel customerPanel = new CustomerPanel();
         private VehiclePanel vehiclePanel = new VehiclePanel();
         //TODO: not yet added: reservationpanel, maintenancepanel
         private JLabel errorLabel = new JLabel(" ");
        private SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm");
         
         public MainPanel() {
             this.setLayout(new BorderLayout());
             
             //build west
             west.setLayout(new FlowLayout());
             west.setBorder(BorderFactory.createMatteBorder(0,0,0,2,Color.LIGHT_GRAY));
             west.add(west_inner);
             west_inner.setLayout(new GridLayout(0,1));
             
             //build south
             south.setLayout(new FlowLayout(FlowLayout.RIGHT));
             south.add(errorLabel);
             
             //reservation button
             JButton reservationButton = new JButton("Reservation");
             reservationButton.setFont(new Font("Arial",Font.BOLD,16));
             reservationButton.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     selectReservation();
                 }
             });
             
             //customer button
             JButton customerButton = new JButton("Customer");
             customerButton.setFont(new Font("Arial",Font.BOLD,16));
             customerButton.addActionListener(new ActionListener() {
                 @Override    
                 public void actionPerformed(ActionEvent e) {
                     selectCustomer();
                 }
             });
             
             //vehicle button
             JButton vehicleButton = new JButton("Vehicle");
             vehicleButton.setFont(new Font("Arial",Font.BOLD,16));
             vehicleButton.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     selectVehicle();
                 }
             });
             
             //maintenance button
             JButton maintenanceButton = new JButton("Maintenance");
             maintenanceButton.setFont(new Font("Arial",Font.BOLD,16));
             maintenanceButton.addActionListener(new ActionListener() {
                 @Override public void actionPerformed(ActionEvent e) {
                     selectMaintenance();
                 }
             });
             
             //left side menu
             west_inner.add(reservationButton);
             west_inner.add(customerButton);
             west_inner.add(vehicleButton);
             west_inner.add(maintenanceButton);
             
             //build north menu for reservation
             //reservation: create button
             JButton reservationCreate = new JButton("Create");
             reservationCreate.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     reservationPanel.showCreatePanel();
                 }
             });
             //reservation: list button
             JButton reservationList = new JButton("List");
             reservationList.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     reservationPanel.showListPanel();
                 }
             });
             
             northReservation.setLayout(new FlowLayout(FlowLayout.LEFT));
             northReservation.add(reservationCreate);
             northReservation.add(reservationList);
             
             //build north menu for customer
             //customer: create button
             JButton customerCreate = new JButton("Create");
             customerCreate.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     customerPanel.showCreatePanel();
                 }
             });
             //customer: list button
             JButton customerList = new JButton("List");
             customerList.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     customerPanel.showListPanel();
                 }
             });
             northCustomer.setLayout(new FlowLayout(FlowLayout.LEFT));
             northCustomer.add(customerCreate);
             northCustomer.add(customerList);
             
             //build north menu for vehicle
             //vehicle: create button
             JButton vehicleCreate = new JButton("Create");
             vehicleCreate.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     vehiclePanel.showCreatePanel();
                 }
             });
             //vehicle: create type button
             JButton vehicleTypeCreate = new JButton("Create Vehicle Type");
             vehicleTypeCreate.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     vehiclePanel.showAddTypePanel();
                 }
             });
             //vehicle: list button
             JButton vehicleList = new JButton("List");
             vehicleList.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     vehiclePanel.showListPanel();
                 }
             });
             //vehicle: overview button
             JButton vehicleOverview = new JButton("Overview");
             vehicleOverview.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     vehiclePanel.showMainScreenPanel();
                 }
             });
             northVehicle.setLayout(new FlowLayout(FlowLayout.LEFT));
             northVehicle.add(vehicleCreate);
             northVehicle.add(vehicleTypeCreate);
             northVehicle.add(vehicleList);
             northVehicle.add(vehicleOverview);
             
             //build north menu for maintenance
             //maintenance: create button
             JButton maintenanceCreate = new JButton("Create");
             maintenanceCreate.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    maintenancePanel.showCreatePanel();
                }
             });
             //maintenance: create type button
             JButton maintenanceTypeCreate = new JButton("Create Maintenance Type");
             maintenanceTypeCreate.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    maintenancePanel.showAddTypePanel();
                }
             });
             //maintenance: list button
             JButton maintenanceList = new JButton("List");
             maintenanceList.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     maintenancePanel.showListPanel();
                 }
             });
             
             northMaintenance.setLayout(new FlowLayout(FlowLayout.LEFT));
             northMaintenance.add(maintenanceCreate);
             northMaintenance.add(maintenanceTypeCreate);
             northMaintenance.add(maintenanceList);
                     
             //build center
             center.setLayout(new FlowLayout());
             center.setPreferredSize(new Dimension(800,600));
             center.add(vehiclePanel);
             
             center_north.setLayout(new FlowLayout(FlowLayout.LEFT));
             center_north.add(northVehicle);
             center_north.setBorder(BorderFactory.createMatteBorder(0,0,2,0, Color.LIGHT_GRAY));
             
             center_super.setLayout(new BorderLayout());
             center_super.add(center, BorderLayout.CENTER);
             center_super.add(center_north, BorderLayout.NORTH);
             this.add(west, BorderLayout.WEST);
             this.add(center_super, BorderLayout.CENTER);
             this.add(south, BorderLayout.SOUTH);
             
         }
         
         /**
          * Get the reservation panel
          * @return reservation panel
          */
         public ReservationPanel getReservationPanel() {
             return reservationPanel;
         }
         
 //        //TODO: ACtivate
 //        /**
 //         * Get the maintenance panel
 //         * @return maintenance panel
 //         */
 //        public Maintenance getMaintenancePanel() {
 //            return maintenancePanel;
 //        }
                 
         /**
          * get the customer panel
          * @return customer panel
          */
         public CustomerPanel getCustomerPanel() {
             return customerPanel;
         }
         
         /**
          * get the maintenance panel
          * @return maintenance panel
          */
         public MaintenancePanel getMaintenancePanel() {
             return maintenancePanel;
         }
         
         /**
          * get the vehicle panel
          * @return vehicle panel
          */
         public VehiclePanel getVehiclePanel() {
             return vehiclePanel;
         }
         
         /**
          * Select the vehicle panel as the shown panel and change the north
          * menu to the appropriate
          */
         public void selectVehicle() {
             center.removeAll();
             center_north.removeAll();
             frame.pack();
             center.add(vehiclePanel);
             center_north.add(northVehicle);
             frame.pack();
         }
         
         /**
          * Select the maintenance panel as the shown panel and change the north
          * menu to the appropriate
          */
         public void selectMaintenance() {
             center.removeAll();
             center_north.removeAll();
             frame.pack();
             center.add(maintenancePanel);
             center_north.add(northMaintenance);
             frame.pack();
         }
         
         /**
          * Select the reservation panel as the shown panel and change the north
          * menu to the appropriate
          */
         public void selectReservation() {
             center.removeAll();
             center_north.removeAll();
             frame.pack();
             center.add(reservationPanel);
             center_north.add(northReservation);
             frame.pack();
         }
         
         /**
          * Select the customer panel as the shown panel and change the north
          * menu to the appropriate
          */
         public void selectCustomer() {
             center.removeAll();
             center_north.removeAll();
             frame.pack();
             customerPanel.showListPanel();
             center.add(customerPanel);
             center_north.add(northCustomer);
             frame.pack();
         }
         
         /**
          * Set error message to display on the error label
          * @param error_message message to display
          */
         public void setErrorLabelMessage(String error_message) {
             errorLabel.setText(dateFormat.format(new Date(Calendar.getInstance().getTimeInMillis()))+" - "+error_message);
         }
     }
 }
