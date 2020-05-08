 package gui.main;
 
 import gui.main.listeners.AppointmentConfirmationListener;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridLayout;
import java.sql.Date;
import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.table.AbstractTableModel;
 
 import backend.DataTransferObjects.AppointmentDto;
 
 /**
  * AppointmentConfirmationPane displays the appointment confirmation pane on the right-hand side of the application
  * when the "Appointment Confirmation" button is clicked. A list of patients scheduled for a particular day will appear,
  * allowing the user to easily find and check off which patients need to be contacted to confirm their appointments. The
  * pane can be closed by clicking on the "Hide Appointment Confirmation" button that appears once the pane is open.
  */
 public class AppointmentConfirmationPane extends JPanel {
 
 	private Component owner;
 	private JTable table;
 	//private AppointmentConfirmationManager acm = new AppointmentConfirmationManager();
 	
 	private Font font = new Font("Arial", Font.PLAIN, 16);
 	
 	/**
 	 * This method is called by MainWindow (the owner). It creates the UI for the pane, including all
 	 * labels and the table of results.
 	 * 
 	 * @param owner - the component that owns this pane
 	 * @param dp - an instance of a DayPanel
 	 */
 	public AppointmentConfirmationPane(Component owner, DayPanel dp) {
 		this.owner = owner;
 		setMinimumSize(new Dimension(0,0));
 		setBackground(Color.WHITE);
 		setLayout(new BorderLayout());
 		
 		// Create heading for the appointment confirmation panel
 		JPanel apptConfirmationPanel = new JPanel(new GridLayout(0, 1));
 		apptConfirmationPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		String date = toFormalString(dp.getDay().getDate());
 		JLabel apptConfirmationLabel = new JLabel("Appointments to Confirm for " + date + ":");
 		apptConfirmationLabel.setFont(font);
 		
 		// Add header label to the panel
 		apptConfirmationPanel.add(apptConfirmationLabel);
 		add(apptConfirmationPanel, BorderLayout.NORTH);
 		
 		// Create panel to display patients for a particular day
 		JPanel tablePanel = new JPanel(new BorderLayout());
		// list of people to confirm
 		AppointmentConfirmationTableModel model = new AppointmentConfirmationTableModel(new ArrayList<Appointment>()); // CHANGE THIS LATER!!!
 		table = new JTable(model);
 		table.setDragEnabled(true);
 		table.setFont(font);
 		table.addMouseListener(new AppointmentConfirmationListener(table, this));
 		table.setAutoCreateRowSorter(true);
     	table.getTableHeader().setReorderingAllowed(false);
     	table.getTableHeader().setFont(font);
     	table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
     	tablePanel.add(table.getTableHeader(), BorderLayout.PAGE_START);
     	tablePanel.add(table, BorderLayout.CENTER);
     	JScrollPane scrollPane = new JScrollPane(tablePanel);
     	add(scrollPane, BorderLayout.CENTER);
 	}
	// convert a date object to the mm/dd/yyyy format
	private String toFormalString(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("mm/dd/yyyy");
		return sdf.format(date);
	}

 	/**
 	 * Resets the table model.
 	 */
 	public void resetModel() {
 		//table.setModel(new AppointmentConfirmationTableModel(acm.getConfirmationList()));
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
 			columnNames = new String[] {"Confirmed", "First Name", "Last Name", "Phone Number"};
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
 
 		/**
 		 * Returns the cell of information specified at a particular row and column.
 		 * 
 		 * @param row - the row number
 		 * @param col - the column number
 		 */
 		public Object getValueAt(int row, int col) {
 			AppointmentDto appt = confirm.get(row);
 			
 			if (col == 0) {
 				return appt.getConfirmed();
 			} else if (col == 1) {
 				return appt.getPatient().getFirstName();
 			} else if (col ==2) {
 				return appt.getPatient().getLastName();
 			} else {
 				return appt.getPatient().getNumberString();
 			}
 		}
 	}
 }
