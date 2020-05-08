 package gui.sub;
 
 import gui.sub.SelectPatientUI.PatTableModel;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.ListSelectionModel;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.TableModel;
 
 import backend.DataService.DataServiceImpl;
 import backend.DataTransferObjects.*;
 
 @SuppressWarnings("serial")
 public class DisplayWaitingPatientUI extends JDialog implements ActionListener {
 	private static DisplayWaitingPatientUI displayWaitingPatientUI;
 	
 	private ArrayList<AppointmentDto> apt;
 	private JTable aptTable;
 	
 	private JButton okButton = new JButton("OK");
 	private JButton cancelButton = new JButton("Cancel");
 	//private JTextArea textArea;
 	private JTextArea noteArea;
 	private Font font = new Font("Arial", Font.PLAIN, 16);
 	
 	private static String comment;
 	
 	private DisplayWaitingPatientUI(String name, WaitlistDto wp) {
 		setModal(true);
 		setTitle(name);
 				
 		apt= DataServiceImpl.GLOBAL_DATA_INSTANCE.searchForAppointments(wp.getTypeID());
 		JComponent panel= displayAvailAppts();
 		panel.setPreferredSize(new Dimension(500,250));
 		
 		setLayout(new BorderLayout());
 
 		add(panel);
 		setResizable(false);
 		
 	}
 	
 	private JComponent displayAvailAppts() {
 		
 		JPanel p = new JPanel(new BorderLayout());
     	JPanel panel = new JPanel(new BorderLayout());
     	
     	
     	//searchField.addKeyListener(this);
     	
     	
     	aptTable = new JTable(new ApptTableModel(apt)) {  
     		//Implement table cell tool tips.
     		/*
     		public String getToolTipText(MouseEvent e) {
     		    String tip = null;
     		    java.awt.Point p = e.getPoint();
     		    int rowIndex = rowAtPoint(p);
     		    
     		    if (rowIndex >= 0) {
     		    	TableModel model = getModel();
     		    	String firstName = (String)model.getValueAt(rowIndex,0);
     		    	String lastName = (String)model.getValueAt(rowIndex,1);
     		    	String phoneNumber = (String)model.getValueAt(rowIndex, 2);
     		    	String note = (String)model.getValueAt(rowIndex, 3);
 
     		    	
     		    	tip = "<html>Patient: " + firstName + " " + lastName + "<br>" +
     		    		  "Phone Number: " + phoneNumber + "<br>" + 
     		    		  "Notes: ";
 
     		    	tip = tip.substring(0, tip.length() - 4);
     		    } else tip = "";
     		    return tip;
     		 
     		}
     		*/
     	};
     	
     	aptTable.setAutoCreateRowSorter(true);
     	aptTable.getTableHeader().setReorderingAllowed(false);
 
     	aptTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
     	aptTable.setFont(font);
     	aptTable.getTableHeader().setFont(font);
     	panel.add(aptTable.getTableHeader(), BorderLayout.PAGE_START);
     	panel.add(aptTable, BorderLayout.CENTER);
     	    	
     	
     	JPanel buttonPanel = new JPanel(new FlowLayout());
     	okButton.setActionCommand("OK");
     	okButton.addActionListener(this);
     	okButton.setFont(font);
     	cancelButton.addActionListener(this);
     	cancelButton.setFont(font);
     	buttonPanel.add(okButton);
     	buttonPanel.add(cancelButton);
     	
     	
     	//Create the scroll pane and add the table to it.
         JScrollPane scrollPane = new JScrollPane(panel);
         
         //searchField.setFont(font);
         //p.add(searchField, BorderLayout.NORTH);
         p.add(scrollPane, BorderLayout.CENTER);
         p.add(buttonPanel, BorderLayout.SOUTH);
         return p;
 		
 		
 		//return null;
 	}
     
 	
 	public static String ShowDialog(Component owner, WaitlistDto waitp) {
 		
 		String name= "Schedule Appointment for " + waitp.getPatient().getFullName();
 		displayWaitingPatientUI = new DisplayWaitingPatientUI(name, waitp);
 		displayWaitingPatientUI.pack();
 		displayWaitingPatientUI.setLocationRelativeTo(owner);
 		displayWaitingPatientUI.setVisible(true);
 		return comment;
 	}
 	
 	public void actionPerformed(ActionEvent e) {
 		if (e.getActionCommand().equals("ok")) {
 			comment = noteArea.getText();
 		} 
 		displayWaitingPatientUI.setVisible(false);
     }
 	
 	class ApptTableModel extends AbstractTableModel {	
 
 		ArrayList<AppointmentDto> appointments = new ArrayList<AppointmentDto>();
 		
 		public ApptTableModel(ArrayList<AppointmentDto> appointments) {
 			this.appointments = appointments;
 		}
 		
 		public AppointmentDto getAppointment(int row) {
 			return appointments.get(row);
 		}
 		
 		private String[] columnNames = { "Date", "Start Time", "End Time", "Practitioner" };
 		
 		public int getColumnCount() {
 			return columnNames.length;
 		}
 		
 		public String getColumnName(int col) {
 			return columnNames[col];
 		}
 
 		public int getRowCount() {
 			return appointments.size();
 		}
 
 		public Object getValueAt(int row, int col) {
 			AppointmentDto a = appointments.get(row);
 			if (col == 0) 
 				return a.getApptDate();
 			else  if (col == 1)
				return a.prettyPrintStart();
 			else if (col == 2)
				return a.prettyPrintEnd();
 			else
 				return a.getPractSchedID();
 		}
 		
 		public boolean isCellEditable(int row, int col) {
 			return false;
 		}
 		
 	}
 	
 }
