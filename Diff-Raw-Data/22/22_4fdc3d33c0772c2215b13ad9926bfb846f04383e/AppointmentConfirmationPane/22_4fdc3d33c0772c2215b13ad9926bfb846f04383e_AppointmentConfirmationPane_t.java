 package gui.main;
 
 import backend.DataService.DataServiceImpl;
 import gui.Constants;
 import gui.DateTimeUtils;
 import gui.main.listeners.AppointmentConfirmationListener;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.Date;
 import java.util.ArrayList;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.table.AbstractTableModel;
 
 import backend.DataTransferObjects.*;
 
 import gui.main.MainWindow;
 
 /**
  * 
  * AppointmentConfirmationPane displays the appointment confirmation pane on the right-hand side of the application
  * when the "Appointment Confirmation" button is clicked. A list of patients scheduled for a particular day will appear,
  * allowing the user to easily find and check off which patients need to be contacted to confirm their appointments. The
  * pane can be closed by clicking on the "Hide Appointment Confirmation" button that appears once the pane is open.
  */
 public class AppointmentConfirmationPane extends JPanel implements ActionListener {
 
 	/** The component (main window) that owns this pane. */
 	private Component owner;
 	/** The table containing the patients for the day. */
 	private JTable table;
 	
 	public JButton confirmButton = new JButton("Confirm Selected Appointment");
 	
 	/**
 	 * This method is called by MainWindow (the owner). It creates the UI for the pane, including all
 	 * labels and the table of results.
 	 * 
 	 * @param owner - the component that owns this pane
 	 * @param dp - an instance of a DayPanel
 	 */
 	public AppointmentConfirmationPane(Component owner) {
 		this.owner = owner;
 
 		setMinimumSize(new Dimension(0,0));
 		setBackground(Color.WHITE);
 		setLayout(new BorderLayout());
 		
 		// Create heading for the appointment confirmation panel
 		JPanel topPanel = new JPanel(new GridLayout(0, 1));
 		JPanel apptConfirmationPanel = new JPanel(new BorderLayout());
 		JPanel buttonPanel = new JPanel(new BorderLayout());
 		
 		apptConfirmationPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
 		
 		String date= DateTimeUtils.prettyPrintMonthDay((((MainWindow)owner).getDayPanel().getDay().getDate()));
 		
 		JLabel apptConfirmationLabel = new JLabel("Appointments to Confirm for " + date + ":");
 		apptConfirmationLabel.setFont(Constants.DIALOG);
 
 		confirmButton.setFont(Constants.DIALOG);
 		confirmButton.setEnabled(false);
 		confirmButton.setActionCommand("confirm");
 		confirmButton.addActionListener(this);
 		
 		// Add header label to the panel
 		apptConfirmationPanel.add(apptConfirmationLabel);
 		buttonPanel.add(confirmButton);
 		topPanel.add(apptConfirmationPanel);
 		topPanel.add(buttonPanel);
 		add(topPanel, BorderLayout.NORTH);
 		
 		// Create panel to display patients for a particular day
 		JPanel tablePanel = new JPanel(new BorderLayout());
 		// list of people to confirm
 		AppointmentConfirmationTableModel model = new AppointmentConfirmationTableModel(
 				(ArrayList<AppointmentDto>)DataServiceImpl.GLOBAL_DATA_INSTANCE.getAllPatientsForDay(
 						((MainWindow)owner).getDayPanel().getDay().getDate()));
 		table = new JTable(model);
 		table.setDragEnabled(true);
 		table.setFont(Constants.DIALOG);
 		table.addMouseListener(new AppointmentConfirmationListener(table, this, (MainWindow) owner));
 		table.getSelectionModel().addListSelectionListener(new AppointmentConfirmationListener(table, this, (MainWindow) owner));
 		table.setAutoCreateRowSorter(true);
     	table.getTableHeader().setReorderingAllowed(false);
     	table.getTableHeader().setFont(Constants.DIALOG);
     	table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
     	tablePanel.add(table.getTableHeader(), BorderLayout.PAGE_START);
     	tablePanel.add(table, BorderLayout.CENTER);
     	JScrollPane scrollPane = new JScrollPane(tablePanel);
     	add(scrollPane, BorderLayout.CENTER);
 	}
 	
 	/** Returns a string of the current date in yyyy-mm-dd format. */
 	public String shortDate(Date d){
 		Date date = new Date(d.getTime());
 		String dateString = date.toString();
 		return dateString;
 	}
 	
 
 	/**
 	 * Resets the table model.
 	 */
 	public void resetModel() {
 		table.setModel(new AppointmentConfirmationTableModel(
 				(ArrayList<AppointmentDto>)DataServiceImpl.GLOBAL_DATA_INSTANCE.getAllPatientsForDay(
 						((MainWindow)owner).getDayPanel().getDay().getDate())));
 	}
 	
 	/**
 	 * AppointmentConfirmationTableModel creates the table itself, defining the rows and columns of the table.
 	 */
 	public class AppointmentConfirmationTableModel extends AbstractTableModel {
 		private String[] columnNames;
 		private ArrayList<AppointmentDto> confirm;
 
 		/**
 		 * Constructor to produce an appointment confirmation table model.
 		 * 
 		 * @param confirm - a list of appointments that need to be confirmed for a chosen day
 		 */
 		public AppointmentConfirmationTableModel(ArrayList<AppointmentDto> confirm) {
 			this.confirm = confirm;
 			columnNames = new String[] {"Confirmed", "Start Time", "First Name", "Last Name", "Phone Number", "Notes"};
 		}
 		
 		/**
 		 * Gets the information for a selected appointment.
 		 * 
 		 * @param row - the row of the table where the desired appointment is located.
 		 * @return the appointment information for a selected row
 		 */
 		public AppointmentDto getAppointment(int row) {
 			return confirm.get(row);
 		}
 		
 		/**
 		 * Returns the number of columns in the table.
 		 * 
 		 * @return number of columns in the table
 		 */
 		public int getColumnCount() {
 			return columnNames.length;
 		}
 		
 		/**
 		 * Returns the name of a specified column in the table.
 		 * 
 		 * @param col - the column number
 		 */
 		public String getColumnName(int col) {
 			return columnNames[col];
 		}
 
 		/**
 		 * Returns the number of appointments to be confirmed for a chosen day.
 		 * 
 		 * @returns the number of rows in the table
 		 */
 		public int getRowCount() {
 			return confirm.size();
 		}
 
 		 public boolean isCellEditable(int row, int col) { 
 			 return false; 
 		 }
 		 
 		/**
 		 * Returns the cell of information specified at a particular row and column.
 		 * 
 		 * @param row - the row number
 		 * @param col - the column number
 		 */
 		public Object getValueAt(int row, int col) {
 			AppointmentDto appt = confirm.get(row);
 			
             PatientDto pat = DataServiceImpl.GLOBAL_DATA_INSTANCE.getPatient(appt.getPatientID());
                         
 			if (col == 0) {
 				if (appt.getConfirmation()) {
 					return "Yes";
 				} else {
 					return "No";
 				}
 			} else if (col == 1) {
 				return appt.prettyPrintStart();
 			} else if (col == 2) {
 				return pat.getFirst();
 			} else if (col == 3) {
 				return pat.getLast();
 			} else if (col == 4){
 				return pat.getPhone();
 			} else {
 				return appt.getNote();
 			}
 		}
 	}
 
 	/**
 	 * Confirms or unconfirms an appointment and resets the results table to reflect the change.
 	 */
 	public void actionPerformed(ActionEvent e) {
 		if (e.getActionCommand() == "confirm") {
 			if (table.getSelectedRow() >= 0) {
 				AppointmentDto appt = ((AppointmentConfirmationTableModel)table.getModel()).getAppointment(table.getSelectedRow());
 				if (!appt.getConfirmation()) {
 					appt.setConfirmation(true);
 					DataServiceImpl.GLOBAL_DATA_INSTANCE.confirmAppointment(appt);
 				} else {
 					appt.setConfirmation(false);
 					DataServiceImpl.GLOBAL_DATA_INSTANCE.unConfirmAppointment(appt);
 				}
                ((MainWindow)owner).refreshAppointments(appt.getApptDate());
 				resetModel();
 			}
 		}
 	}
 }
