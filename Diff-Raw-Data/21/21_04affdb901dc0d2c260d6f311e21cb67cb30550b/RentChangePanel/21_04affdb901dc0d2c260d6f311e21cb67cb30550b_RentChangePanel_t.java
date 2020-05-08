 package rentalcar;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import org.jdesktop.swingx.JXDatePicker;
 
 import core.DBConnection;
 import core.User.EmployeeUser;
 
 @SuppressWarnings("rawtypes")
 public class RentChangePanel extends JPanel {
     private static final long serialVersionUID = 1L;
     final static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 
     EmployeeUser employee;
     DBConnection connection = new DBConnection();
 
     JLabel RentalChangeReqHeading, RentalInfoHeading, UserAffectedHeading;
     JLabel enterUsername;
     JTextField enterUsernameField;
    JLabel CarModel, Location, origRetTimeDate, UAorigRetTimeDate, NewArrTimeDate, origPickUpTime;
     JTextField CarModelTextField, LocationTextField;
 
     String[] RIOrigRetTimeStrings = {"12:00 AM", "12:30 AM", "1:00 AM",
 			"1:30 AM", "2:00 AM", "02:30 AM", "03:00 AM", "3:30 AM",
 			"04:00 AM", "04:30 AM", "05:00 AM", "05:30 AM", "06:00 AM",
 			"06:30 AM", "07:00 AM", "07:30 AM", "08:00 AM", "08:30 AM",
 			"09:00 AM", "09:30 AM", "10:00 AM", "10:30 AM", "11:00 AM",
 			"11:30 AM", "12:00 PM", "12:30 PM", "1:00 PM", "1:30 PM",
 			"2:00 PM", "02:30 PM", "03:00 PM", "3:30 PM", "04:00 PM",
 			"04:30 PM", "05:00 PM", "05:30 PM", "06:00 PM", "06:30 PM",
 			"07:00 PM", "07:30 PM", "08:00 PM", "08:30 PM", "09:00 PM",
 			"09:30 PM", "10:00 PM", "10:30 PM", "11:00 PM", "11:30 PM"};
     
     String[] RINewArrTimeStrings = {"12:00 AM", "12:30 AM", "1:00 AM",
 			"1:30 AM", "2:00 AM", "02:30 AM", "03:00 AM", "3:30 AM",
 			"04:00 AM", "04:30 AM", "05:00 AM", "05:30 AM", "06:00 AM",
 			"06:30 AM", "07:00 AM", "07:30 AM", "08:00 AM", "08:30 AM",
 			"09:00 AM", "09:30 AM", "10:00 AM", "10:30 AM", "11:00 AM",
 			"11:30 AM", "12:00 PM", "12:30 PM", "1:00 PM", "1:30 PM",
 			"2:00 PM", "02:30 PM", "03:00 PM", "3:30 PM", "04:00 PM",
 			"04:30 PM", "05:00 PM", "05:30 PM", "06:00 PM", "06:30 PM",
 			"07:00 PM", "07:30 PM", "08:00 PM", "08:30 PM", "09:00 PM",
 			"09:30 PM", "10:00 PM", "10:30 PM", "11:00 PM", "11:30 PM"};
     
     String[] UAOrigPickTimeStrings = {"12:00 AM", "12:30 AM", "1:00 AM",
 			"1:30 AM", "2:00 AM", "02:30 AM", "03:00 AM", "3:30 AM",
 			"04:00 AM", "04:30 AM", "05:00 AM", "05:30 AM", "06:00 AM",
 			"06:30 AM", "07:00 AM", "07:30 AM", "08:00 AM", "08:30 AM",
 			"09:00 AM", "09:30 AM", "10:00 AM", "10:30 AM", "11:00 AM",
 			"11:30 AM", "12:00 PM", "12:30 PM", "1:00 PM", "1:30 PM",
 			"2:00 PM", "02:30 PM", "03:00 PM", "3:30 PM", "04:00 PM",
 			"04:30 PM", "05:00 PM", "05:30 PM", "06:00 PM", "06:30 PM",
 			"07:00 PM", "07:30 PM", "08:00 PM", "08:30 PM", "09:00 PM",
 			"09:30 PM", "10:00 PM", "10:30 PM", "11:00 PM", "11:30 PM"};
     
     String[] UAOrigRetTimeStrings = {"12:00 AM", "12:30 AM", "1:00 AM",
 			"1:30 AM", "2:00 AM", "02:30 AM", "03:00 AM", "3:30 AM",
 			"04:00 AM", "04:30 AM", "05:00 AM", "05:30 AM", "06:00 AM",
 			"06:30 AM", "07:00 AM", "07:30 AM", "08:00 AM", "08:30 AM",
 			"09:00 AM", "09:30 AM", "10:00 AM", "10:30 AM", "11:00 AM",
 			"11:30 AM", "12:00 PM", "12:30 PM", "1:00 PM", "1:30 PM",
 			"2:00 PM", "02:30 PM", "03:00 PM", "3:30 PM", "04:00 PM",
 			"04:30 PM", "05:00 PM", "05:30 PM", "06:00 PM", "06:30 PM",
 			"07:00 PM", "07:30 PM", "08:00 PM", "08:30 PM", "09:00 PM",
 			"09:30 PM", "10:00 PM", "10:30 PM", "11:00 PM", "11:30 PM"};
     
     JXDatePicker RIOrigRetTimeDate, RINewArrTimeDate, UAOrigPickTimeDate, UAOrigRetTimeDate;
 	JComboBox RIOrigRetTimeCombo, RINewArrTimeCombo, UAOrigPickTimeCombo, UAOrigRetTimeCombo;
 
     JButton Update;
 
     JLabel UAUsername, UAEmailAddress, UAPhone; 
     JTextField UAUsernameField, UAEmailAddressField, UAPhoneField;
 
     JButton CancelReservation, ShowCarAvailability;
 
     @SuppressWarnings({ "unchecked" })
 	public RentChangePanel(EmployeeUser employee) {
         this.setEmployee(employee);
         this.setBounds(50, 50, 1300, screenSize.height-100);
 
         this.setBackground(java.awt.Color.green);
         this.setLayout(new BorderLayout());
 
         RentalChangeReqHeading = new JLabel("Rental Change Request");
         RentalChangeReqHeading.setFont(new Font("Helvetica", Font.BOLD, 70));
 
         this.add(RentalChangeReqHeading, BorderLayout.NORTH);
         
         enterUsername = new JLabel("Enter Username:");
        enterUsernameField = new JTextField(50);
 
         CarModel = new JLabel("Car Model:");
         Location = new JLabel("Location:");
         CarModelTextField = new JTextField(30);
         LocationTextField = new JTextField(50);
         
         origRetTimeDate = new JLabel("Original Return Time:");
 		java.util.Date RIOrigDate = new java.util.Date();
 		RIOrigRetTimeDate = new JXDatePicker(RIOrigDate);
     	RIOrigRetTimeCombo = new JComboBox(RIOrigRetTimeStrings);
 
     	NewArrTimeDate = new JLabel("New Arrival Time:");
 		java.util.Date RINewArrDate = new java.util.Date();
 		RINewArrTimeDate = new JXDatePicker(RINewArrDate);
     	RINewArrTimeCombo = new JComboBox(RINewArrTimeStrings) ;
 
         Update = new JButton("Update");
         Update.addActionListener(new UpdateButtonListener());
     	
         origPickUpTime = new JLabel("Original Pick Up Time:");
     	java.util.Date UAOrigPickDate = new java.util.Date();
 		UAOrigPickTimeDate = new JXDatePicker(UAOrigPickDate);
 		UAOrigPickTimeCombo = new JComboBox(UAOrigPickTimeStrings);
 
		UAorigRetTimeDate = new JLabel("Original Return Time:");
 		java.util.Date UAOrigRetDate = new java.util.Date();
 		UAOrigRetTimeDate = new JXDatePicker(UAOrigRetDate);
 		UAOrigRetTimeCombo = new JComboBox(UAOrigRetTimeStrings) ;
         
         UAUsername = new JLabel("Username:");
         UAEmailAddress = new JLabel("Email Address:");
         UAPhone = new JLabel("Phone No:"); 
        UAUsernameField = new JTextField(50);
        UAEmailAddressField = new JTextField(50);
        UAPhoneField = new JTextField(15);
 
         CancelReservation = new JButton("Cancel Reservation");
         CancelReservation.addActionListener(new CancelReservationButtonListener());
         ShowCarAvailability = new JButton("Show car availability");
         ShowCarAvailability.addActionListener(new ShowCarAvailabilityButtonListener());
         
         JPanel TwoPanel = new JPanel();
         TwoPanel.setLayout(new GridLayout(1,2));
 
         JPanel RentalInfoPanel = new JPanel();
         RentalInfoPanel.setLayout(new BoxLayout(RentalInfoPanel, BoxLayout.PAGE_AXIS));
 
         RentalInfoHeading = new JLabel("Rental Inormation");
        RentalInfoHeading.setFont(new Font("Helvetica", Font.BOLD, 40));
 
         JPanel p0 = new JPanel();
         p0.add(CarModel);
         p0.add(CarModelTextField);
         JPanel p1 = new JPanel();
         p1.add(Location);
         p1.add(LocationTextField);
         JPanel p2 = new JPanel();
         p2.add(origRetTimeDate);
         p2.add(RIOrigRetTimeDate);
         p2.add(RIOrigRetTimeCombo);
         JPanel p3 = new JPanel();
         p3.add(NewArrTimeDate);
         p3.add(RINewArrTimeDate);
         p3.add(RINewArrTimeCombo);
 
         RentalInfoPanel.add(RentalInfoHeading);
         RentalInfoPanel.add(p0);
         RentalInfoPanel.add(p1);
         RentalInfoPanel.add(p2);
         RentalInfoPanel.add(p3);
 
         JPanel UserAffectedPanel = new JPanel();
         UserAffectedPanel.setLayout(new BoxLayout(UserAffectedPanel, BoxLayout.PAGE_AXIS));
 
         UserAffectedHeading = new JLabel("User Affected");
         UserAffectedHeading.setFont(new Font("Helvetica", Font.BOLD, 40));
 
         JPanel p20 = new JPanel();
         p20.add(UserAffectedHeading);
         JPanel p21 = new JPanel();
         p21.add(UAUsername);
         p21.add(UAUsernameField);
         JPanel p22 = new JPanel();
         p22.add(origPickUpTime);
         p22.add(UAOrigPickTimeDate);
         p22.add(UAOrigPickTimeCombo);
         JPanel p23 = new JPanel();
        p23.add(UAorigRetTimeDate);
         p23.add(UAOrigRetTimeDate);
         p23.add(UAOrigRetTimeCombo);
         JPanel p24 = new JPanel();
         p24.add(UAEmailAddress);
         p24.add(UAEmailAddressField);
         JPanel p25 = new JPanel();
         p25.add(UAPhone);
         p25.add(UAPhoneField);
         JPanel p26 = new JPanel();
         p26.add(CancelReservation);
         p26.add(ShowCarAvailability);
         UserAffectedPanel.add(UserAffectedHeading);
         UserAffectedPanel.add(p20);
         UserAffectedPanel.add(p21);
         UserAffectedPanel.add(p22);
         UserAffectedPanel.add(p23);
         UserAffectedPanel.add(p24);
         UserAffectedPanel.add(p25);
         UserAffectedPanel.add(p26);
 
         TwoPanel.add(RentalInfoPanel);
         TwoPanel.add(UserAffectedPanel);
         this.add(TwoPanel, BorderLayout.CENTER);
     }
 
     public EmployeeUser getEmployee() {
 		return employee;
 	}
 
 	public void setEmployee(EmployeeUser employee) {
 		this.employee = employee;
 	}
 
 	private class UpdateButtonListener implements ActionListener {
         public void actionPerformed(ActionEvent event) {
         	
         }
     }
 
     private class CancelReservationButtonListener implements ActionListener {
         public void actionPerformed(ActionEvent event) {
 
         }
     }
     
 	private class ShowCarAvailabilityButtonListener implements ActionListener {
         public void actionPerformed(ActionEvent event) {
 
         }
     }
 
 }
