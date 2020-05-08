 package GUIPanes;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Vector;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.SpinnerDateModel;
 import javax.swing.table.DefaultTableModel;
 
 import Database.Database;
 import GUI.MainWindow;
 import Identifiers.TestResults;
import Identifiers.identifier;
 import Other.AutoCompleteJComboBox;
 
 @SuppressWarnings("serial")
 public class MedicalTestPane extends JPanel implements ActionListener {
 
 	/* Search Mode */
 	protected AutoCompleteJComboBox patient;
 	protected AutoCompleteJComboBox doctor;
 	protected AutoCompleteJComboBox test;
 	protected JButton search; // Search button
 	private Vector<TestResults> searchResults;
 	private TestResults selectedTest;
 
 	/* Results Mode */
 	protected JButton back;
 	protected JButton next;
 	protected JTable results;
 
 	/* Medical test mode */
 	protected AutoCompleteJComboBox lab;
 	protected JTextField test_results;
 	protected JTextField record_id;
 	protected JSpinner date;
 	protected JButton save;
 	protected JButton back_test;
 
 	public MedicalTestPane() {
 		/* Search Mode */
 		
 		this.setName("MedicalTestPane");
 		
 		patient = new AutoCompleteJComboBox();
 		doctor = new AutoCompleteJComboBox();
 		test = new AutoCompleteJComboBox();
 		test.setMode('t');
 		doctor.setMode('d');
 		patient.setMode('p');
 
 		// Search button
 		search = new JButton("Search");
 		search.setFont(MainWindow.getFont());
 		search.addActionListener(this);
 
 		/* Results Mode */
 		back = new JButton("Back");
 		back.setFont(MainWindow.getFont());
 		back.addActionListener(this);
 
 		next = new JButton("Next");
 		next.setFont(MainWindow.getFont());
 		next.addActionListener(this);
 
 		/* Medical test mode */
 		record_id = new JTextField();
 		lab = new AutoCompleteJComboBox();
 		test_results = new JTextField();
 		SpinnerDateModel Spinner = new SpinnerDateModel();
 		date = new JSpinner(Spinner);
 		date.setEditor(new JSpinner.DateEditor(date, "dd/MM/yyyy"));
 		
 		save = new JButton("Save");
 		save.setFont(MainWindow.getFont());
 		save.addActionListener(this);
 		back_test = new JButton("Back");
 		back_test.setFont(MainWindow.getFont());
 		back_test.addActionListener(this);
 
 		renderSearch();
 	}
 
 	private void renderSearch() {
 		GridLayout layout = new GridLayout(8, 2);
 		layout.setVgap(6);
 		super.setLayout(layout);
 
 		super.setBorder(MainWindow.getBorder("Search Test Record"));
 
 		// Patient name or ID
 		JLabel patient_label = new JLabel("Name or ID of patient");
 		patient_label.setFont(MainWindow.getFont());
 		add(patient_label);
 		add(patient);
 
 		// Doctor name or ID
 		JLabel doctor_label = new JLabel("Name or ID of doctor");
 		doctor_label.setFont(MainWindow.getFont());
 		add(doctor_label);
 		add(doctor);
 
 		// Test Name
 		JLabel test_label = new JLabel("Test Name");
 		test_label.setFont(MainWindow.getFont());
 		add(test_label);
 		add(test);
 		test.setMode('t');
 
 		// Record ID
 		JLabel Record_id = new JLabel("Record ID");
 		Record_id.setFont(MainWindow.getFont());
 		add(Record_id);
 		add(record_id);
 
 		// Search button
 		add(new JPanel());
 		JPanel button = new JPanel();
 		BorderLayout border = new BorderLayout();
 		button.setLayout(border);
 		button.add(search, BorderLayout.EAST);
 		add(button);
 		
 		doctor.setSelectedIndex(-1);
 		test.setSelectedIndex(-1);
 		patient.setSelectedIndex(-1);
 
 		add(new JPanel());
 		add(new JPanel());
 
 		add(new JPanel());
 		add(new JPanel());
 
 		add(new JPanel());
 		add(new JPanel());
 	}
 
 	private void renderResults() {
 
 		setLayout(new BorderLayout());
 
 		DefaultTableModel ptable = new DefaultTableModel();
 		results = new JTable(ptable);
 
 		ptable.addColumn("Health Care No: Patient Name");
 
 		ptable.addColumn("Employee Id: Doctor Name");
 
 		ptable.addColumn("Test Id: Test Name");
 
 		ptable.addColumn("Prescription Date");
 
 		String PatientID;
 //		if (patient.getSelectedItem() == null
 //				|| !(patient.getSelectedItem() instanceof identifier)) {
 //			PatientID = "";
 //		} else {
 //			PatientID = ((identifier) patient.getSelectedItem()).getId();
 //
 //		}
 //
 		String DoctorID;
 //		if (doctor.getSelectedItem() == null
 //				|| !(patient.getSelectedItem() instanceof identifier)) {
 //			DoctorID = "";
 //		} else {
 //			DoctorID = ((identifier) doctor.getSelectedItem()).getId();
 //
 //		}
 //
 		String TestID;
 //		if (test.getSelectedItem() == null
 //				|| !(patient.getSelectedItem() instanceof identifier)) {
 //			TestID = "";
 //		} else {
 //			TestID = ((identifier) test.getSelectedItem()).getId();
 //
 //		}
 //
 	String recordID;
 	if (record_id.getText().equals("")||record_id.getText()==null) {
 			recordID = "";
 	} else {
 			recordID = record_id.getText();
 
 	}
 //		
 		try{
			PatientID = ((identifier) patient.getSelectedItem()).getId();
 		} catch (Exception e){
 			PatientID = "";
 		}
 		
 		try{
			DoctorID = ((identifier) doctor.getSelectedItem()).getId();
 		} catch (Exception e){
 			DoctorID = "";
 		}
 
 		try{
			TestID = ((identifier) test.getSelectedItem()).getId();
 		} catch (Exception e){
 			TestID = "";
 		}
 		
 		
 		
 		
 		
 		searchResults = Database.TestRecordSearch(PatientID, DoctorID, TestID,
 				recordID);
 		
 
 		
 		if (searchResults != null) {
 
 			for (int i = 0; i < searchResults.size(); i++) {
 
 				ptable.addRow(new Object[] {
 						searchResults.elementAt(i).getPatientInfo(),
 						searchResults.elementAt(i).getDoctorInfo(),
 						searchResults.elementAt(i).getTestInfo(),
 						searchResults.elementAt(i).getPrescribedDate(), });
 
 				results.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 
 				results.getColumnModel().getColumn(0).setPreferredWidth(95);
 				results.getColumnModel().getColumn(1).setPreferredWidth(100);
 				results.getColumnModel().getColumn(2).setPreferredWidth(200);
 				results.getColumnModel().getColumn(3).setPreferredWidth(100);
 
 				results.getSelectionModel().setSelectionMode(
 						ListSelectionModel.SINGLE_SELECTION);
 
 				if (searchResults.size() >= 1)
 					results.getSelectionModel().setSelectionInterval(0, 0);
 
 				results.setRowSelectionAllowed(true);
 				results.setColumnSelectionAllowed(false);
 			}
 		} else {
 			JOptionPane.showMessageDialog(this, "No Results recieved", "Error",
 					JOptionPane.INFORMATION_MESSAGE);
 			super.removeAll();
 			renderSearch();
 			updateUI();
 			return;
 		}
 
 		JScrollPane scrollPane = new JScrollPane(results);
 
 		add(scrollPane, BorderLayout.CENTER);
 		JPanel button = new JPanel();
 		BorderLayout border = new BorderLayout();
 		button.setLayout(border);
 		button.add(back, BorderLayout.WEST);
 		button.add(next, BorderLayout.EAST);
 		add(button, BorderLayout.SOUTH);
 	}
 
 	private void renderMedicalTest() {
 
 		selectedTest = searchResults.get(results.getSelectedRow());
 
 		GridLayout layout = new GridLayout(9, 2);
 		layout.setVgap(5);
 		super.setLayout(layout);
 
 		super.setBorder(MainWindow.getBorder("Test Record"));
 
 		/* Test_ID (not editable) */
 		JLabel test_id = new JLabel("Record Id");
 		test_id.setFont(MainWindow.getFont());
 		JLabel test_test_id = new JLabel(selectedTest.getRecordId());
 		test_test_id.setFont(MainWindow.getFont());
 		add(test_id);
 		add(test_test_id);
 
 		/* Patient name (not editable) */
 		JLabel test_patient = new JLabel("Patient");
 		test_patient.setFont(MainWindow.getFont());
 		JLabel test_patient_result = new JLabel(selectedTest.getPatientInfo());
 		test_patient_result.setFont(MainWindow.getFont());
 		add(test_patient);
 		add(test_patient_result);
 
 		/* Doctor name (not editable) */
 		JLabel test_doctor = new JLabel("Doctor");
 		test_doctor.setFont(MainWindow.getFont());
 		JLabel test_doctor_result = new JLabel(selectedTest.getDoctorInfo());
 		test_doctor_result.setFont(MainWindow.getFont());
 		add(test_doctor);
 		add(test_doctor_result);
 
 		/* Test name (not editable) */
 		JLabel test_test = new JLabel("Test Name");
 		test_test.setFont(MainWindow.getFont());
 		JLabel test_test_result = new JLabel(selectedTest.getTestInfo());
 		test_test_result.setFont(MainWindow.getFont());
 		add(test_test);
 		add(test_test_result);
 
 		/* Prescribe date (not editable) */
 		JLabel test_prescribeDate = new JLabel("Prescribe Date");
 		test_prescribeDate.setFont(MainWindow.getFont());
 		JLabel test_prescribeDate_result = new JLabel(
 				selectedTest.getPrescribedDate());
 		test_prescribeDate_result.setFont(MainWindow.getFont());
 		add(test_prescribeDate);
 		add(test_prescribeDate_result);
 
 		/* Lab name */
 
 		lab.setMode('l');
 		
 		JLabel test_lab = new JLabel("Medical Lab");
 		test_lab.setFont(MainWindow.getFont());
 		add(test_lab);
 		add(lab);
 		lab.DefaultValue(selectedTest.getMedicalLab());
 		
 		//Get Ray to ADD -BUG!!!!!!!!!!
 		try{ lab.setSelectedIndex(0); }
 		catch (Exception e) { lab.setSelectedIndex(-1); } 
 		
 		/* Result */
 		JLabel test_result = new JLabel("Result");
 		test_result.setFont(MainWindow.getFont());
 		add(test_result);
 		add(test_results);
 		
 		//BUG!!!!
 		test_results.setText("");
 		
 		
 		/* Test Date */
 		JLabel test_date = new JLabel("Test Date");
 		test_date.setFont(MainWindow.getFont());
 		add(test_date);
 		add(date);
 
 		// Save button
 		JPanel button1 = new JPanel();
 		BorderLayout border1 = new BorderLayout();
 		button1.setLayout(border1);
 		button1.add(back_test, BorderLayout.WEST);
 		add(button1);
 
 		JPanel button = new JPanel();
 		BorderLayout border = new BorderLayout();
 		button.setLayout(border);
 		button.add(save, BorderLayout.EAST);
 		add(button);
 	}
 
 	// DUSTIN CHANGED NOV 7
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource() == search) {
 			super.removeAll();
 			renderResults();
 			updateUI();
 		} else if (e.getSource() == back) {
 			super.removeAll();
 			renderSearch();
 			updateUI();
 		} else if (e.getSource() == next) {
 
 			//BUG move inside loop - 2 lines down
 			
 			if(results.getSelectedRow()!=-1){
 				super.removeAll();//MOVE ME
 			renderMedicalTest();
 			}
 			else {
 				JOptionPane.showMessageDialog(this,
 						"No Result Selected", "Failure",
 						JOptionPane.ERROR_MESSAGE);	
 			}
 			updateUI();
 		} else if (e.getSource() == back_test) {
 			super.removeAll();
 			renderResults();
 			updateUI();
 		} else if (e.getSource() == save) {
 			if (!MainWindow.DEBUG) {
 
 				try {
 					
 					Statement stmt = Database.getConnection().createStatement();
 
 					// Execute the Update
 					// System.out.println(sqldate.toString());
 					String Lab;
 					
 					try{
						Lab = ((identifier) lab.getSelectedItem()).getName();
 					} catch (Exception f){
 						JOptionPane.showMessageDialog(this,
 								"Invalid/No Lab Specified", "Failure",
 								JOptionPane.ERROR_MESSAGE);
 						return;
 					}
 					
 					
 					if(Database.CanConduct(Lab, selectedTest.getTestId())){
 					java.sql.Date sqldate = new java.sql.Date(
 							((java.util.Date) date.getValue()).getTime());
 					int rows = stmt
 							.executeUpdate("UPDATE test_record SET result = '"
 									+ test_results.getText()
 									+ "', medical_lab='"
 									+ Lab
 									+ "', test_date=to_date('"
 									+ sqldate.toString()
 									+ "','YYYY-MM-DD') WHERE test_id = "
 									+ selectedTest.getRecordId());
 
 					stmt.close();
 					} else {
 						JOptionPane.showMessageDialog(this,
 								"Lab Cannot Conduct this Lab", "Failure",
 								JOptionPane.ERROR_MESSAGE);
 						return;
 					}
 					
 				} catch (SQLException g) {
 					JOptionPane.showMessageDialog(this, "Test Record Updating Failed",
 							"Update Failed", JOptionPane.INFORMATION_MESSAGE);
 					return;
 				}
 
 			}
 			JOptionPane.showMessageDialog(this, "Test Record Saved", "Success",
 					JOptionPane.INFORMATION_MESSAGE);
 		}
 	}
 }
