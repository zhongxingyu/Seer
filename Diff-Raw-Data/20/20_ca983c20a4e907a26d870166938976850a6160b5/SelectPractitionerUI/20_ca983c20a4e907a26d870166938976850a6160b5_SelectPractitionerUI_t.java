 package gui.sub;
 
 import gui.Constants;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.util.ArrayList;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.TableColumn;
 
 import backend.DataService.DataServiceImpl;
 import backend.DataTransferObjects.PractitionerDto;
 import backend.DataTransferObjects.TypeDto;
 
 /**
  * Popup that appears when the schedule practitioner button is clicked.
  * It allows for practitioners to be scheduled for a day.
  * It also allows his hours to be specified.
  * New practitioners can also be created from here.
  */
 public class SelectPractitionerUI extends JDialog implements ActionListener,ListSelectionListener {
 
 	private static SelectPractitionerUI selectPractitionerUI;
 	private ArrayList<PractitionerDto> prac =
 		(ArrayList<PractitionerDto>) DataServiceImpl.GLOBAL_DATA_INSTANCE.getAllPractitioners();
 	private JTextField startTimeField = new JTextField();
 	private JTextField endTimeField = new JTextField();
 	private JTextField firstNameField = new JTextField();
 	private JTextField lastNameField = new JTextField();
 	private JComboBox typeCombo;
 	JTabbedPane tabbedPane;
 	private JTextField apptLengthField = new JTextField();
 	private JTextArea noteField = new JTextArea();
 	private JButton okButton1 = new JButton("OK");
 	private JButton cancelButton1 = new JButton("Cancel");
 	private JButton okButton2 = new JButton("OK");
 	private JButton cancelButton2 = new JButton("Cancel");
 
 	private int dayStart;
 	private int dayEnd;
 
 	public int startTime;
 	public int endTime;
 	
 	JTable pracTable;
 	
 	private static PractitionerDto practitioner;
 	
 	private SelectPractitionerUI(String name) {
 		practitioner = null;
 		setModal(true);
 		setTitle(name);
 		setLayout(new BorderLayout());
 		
     	JPanel hoursPanel = new JPanel(new BorderLayout());
     	JPanel hoursFieldPanel = new JPanel();
     	hoursFieldPanel.setLayout(new BoxLayout(hoursFieldPanel, BoxLayout.X_AXIS));
     	JLabel hoursLabel = new JLabel("Work hours (00:00 - 23:59)");
     	hoursLabel.setFont(Constants.PARAGRAPH);
     	startTimeField.setFont(Constants.PARAGRAPH);
     	endTimeField.setFont(Constants.PARAGRAPH);
     	hoursFieldPanel.add(startTimeField);
     	hoursFieldPanel.add(new JLabel("  -  "));
     	hoursFieldPanel.add(endTimeField);
     	hoursPanel.add(hoursLabel, BorderLayout.PAGE_START);
     	hoursPanel.add(hoursFieldPanel, BorderLayout.CENTER);
     	add(hoursPanel, BorderLayout.PAGE_START);
 		
 		tabbedPane = new JTabbedPane();
 		JComponent panel1 = makeExisPracPanel();
 		tabbedPane.addTab("Existing Practitioner", null, panel1,
 		                  "Select an Existing Practitioner");
 		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
 
 		JComponent panel2 = makeNewPracPanel();
 		tabbedPane.addTab("New Practitioner", null, panel2,
 		                  "Select a New Practitioner");
 		tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
 		
 		tabbedPane.setPreferredSize(new Dimension(500,250));
 		tabbedPane.setFont(Constants.PARAGRAPH);
 		add(tabbedPane, BorderLayout.CENTER);
 		setResizable(false);
 		
 		okButton1.setActionCommand("okOld");
     	okButton1.addActionListener(this);
     	cancelButton1.addActionListener(this);
     	okButton2.setActionCommand("okNew");
     	okButton2.addActionListener(this);
     	cancelButton2.addActionListener(this);
 	}
     
     private JComponent makeExisPracPanel() {
     	JPanel p = new JPanel(new BorderLayout());
     	JPanel panel = new JPanel(new BorderLayout());
     	
     	pracTable = new JTable(new PracTableModel());
     	pracTable.getTableHeader().setReorderingAllowed(false);
     	TableColumn column = null;
     	for (int i = 0; i < pracTable.getColumnCount(); i++) {
     	    column = pracTable.getColumnModel().getColumn(i);
     	    if (i == 2) {
     	        column.setPreferredWidth(200); //third column is bigger
     	    } else {
     	        column.setPreferredWidth(100);
     	    }
     	}
     	pracTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
     	pracTable.setFont(Constants.PARAGRAPH);
     	pracTable.setBorder(new EmptyBorder(5,5,5,5));
     	pracTable.getTableHeader().setFont(Constants.PARAGRAPH);
     	panel.add(pracTable.getTableHeader(), BorderLayout.PAGE_START);
     	panel.add(pracTable, BorderLayout.CENTER);
     	
     	JPanel buttonPanel = new JPanel(new FlowLayout());
     	okButton1.setFont(Constants.DIALOG);
     	cancelButton1.setFont(Constants.DIALOG);
     	buttonPanel.add(okButton1);
     	buttonPanel.add(cancelButton1);
     	
     	//Create the scroll pane and add the table to it.
         JScrollPane scrollPane = new JScrollPane(panel);
         //scrollPane.setPreferredSize(new Dimension(410,200));
         
         p.add(scrollPane, BorderLayout.CENTER);
         p.add(buttonPanel, BorderLayout.SOUTH);
         return p;
     }
     
     private JComponent makeNewPracPanel() {
     	JPanel panel = new JPanel(new BorderLayout());
     	
     	JPanel topSubpanel = new JPanel(new GridLayout(0, 1));
     	
     	JPanel nameLengthPanel = new JPanel(new GridLayout(0, 3));
     	JPanel firstNamePanel = new JPanel(new BorderLayout());
     	JPanel lastNamePanel = new JPanel(new BorderLayout());
     	JLabel firstNameLabel = new JLabel("First Name: ");
     	JLabel lastNameLabel = new JLabel("Last Name: ");
     	firstNameLabel.setFont(Constants.PARAGRAPH);
     	lastNameLabel.setFont(Constants.PARAGRAPH);
     	firstNameField.setFont(Constants.PARAGRAPH);
     	lastNameField.setFont(Constants.PARAGRAPH);
     	firstNamePanel.add(firstNameLabel, BorderLayout.NORTH);
     	firstNamePanel.add(firstNameField, BorderLayout.CENTER);
     	lastNamePanel.add(lastNameLabel, BorderLayout.NORTH);
     	lastNamePanel.add(lastNameField, BorderLayout.CENTER);
     	JPanel lengthPanel = new JPanel(new BorderLayout());
     	JLabel label = new JLabel("Appt Length (Min): ");
     	label.setFont(Constants.PARAGRAPH);
     	apptLengthField.setFont(Constants.PARAGRAPH);
     	lengthPanel.add(label, BorderLayout.NORTH);
     	lengthPanel.add(apptLengthField, BorderLayout.CENTER);
     	nameLengthPanel.add(firstNamePanel);
     	nameLengthPanel.add(lastNamePanel);
     	nameLengthPanel.add(lengthPanel);
     	topSubpanel.add(nameLengthPanel);
     	
     	JPanel typePanel = new JPanel(new BorderLayout());
     	label = new JLabel("Practitioner Type: ");
     	label.setFont(Constants.PARAGRAPH);
     	typePanel.add(label, BorderLayout.NORTH);
     	JPanel typeComboPanel = new JPanel(new BorderLayout());
         
         ArrayList<TypeDto> typeList = (ArrayList<TypeDto>) DataServiceImpl.GLOBAL_DATA_INSTANCE.getAllPractitionerTypes();
     	typeCombo = new JComboBox(typeList.toArray());
     	if (typeList.size() > 0)
     		typeCombo.setSelectedIndex(0);
     	typeCombo.setFont(Constants.PARAGRAPH);
     	typeComboPanel.add(typeCombo, BorderLayout.CENTER);
     	JButton newTypeButton = new JButton("New Type");
     	newTypeButton.setFont(Constants.DIALOG);
     	newTypeButton.addActionListener(this);
     	newTypeButton.setActionCommand("New Type");
     	typeComboPanel.add(newTypeButton, BorderLayout.EAST);
     	typePanel.add(typeComboPanel, BorderLayout.CENTER);
     	topSubpanel.add(typePanel);
     	
     	
     	
     	panel.add(topSubpanel, BorderLayout.NORTH);
     	
     	JPanel notePanel = new JPanel(new BorderLayout());
     	label = new JLabel("Practitioner notes: ", JLabel.CENTER);
     	label.setFont(Constants.PARAGRAPH);
     	notePanel.add(label, BorderLayout.NORTH);
     	noteField.setFont(Constants.PARAGRAPH);
     	noteField.setLineWrap(true);
     	noteField.setWrapStyleWord(true);
     	JScrollPane notePane = new JScrollPane(noteField);
     	notePanel.add(notePane, BorderLayout.CENTER);
     	panel.add(notePanel, BorderLayout.CENTER);
     	
     	JPanel buttonPanel = new JPanel(new FlowLayout());
     	
     	okButton2.setFont(Constants.DIALOG);
     	cancelButton2.setFont(Constants.DIALOG);
     	buttonPanel.add(okButton2);
     	buttonPanel.add(cancelButton2);
     	
     	panel.add(buttonPanel, BorderLayout.SOUTH);
     	
     	return panel;
     }
 	
 	public static SelectPractitionerUI ShowDialog(Component owner, int startTime, int endTime) {
 		selectPractitionerUI = new SelectPractitionerUI("Select Practitioner");
 		selectPractitionerUI.setStartTime(startTime);
 		selectPractitionerUI.setEndTime(endTime);
 		selectPractitionerUI.pack();
 		selectPractitionerUI.setLocationRelativeTo(owner);
 		selectPractitionerUI.setVisible(true);
 		return selectPractitionerUI;
 	}
 	
 	public PractitionerDto getPractitioner() {
 		return practitioner;
 	}
 	
 	private void setStartTime(int startTime) {
 		this.dayStart = startTime;
 		this.startTime = startTime;
 		String startTimeString = "";
 		int hours = startTime/60;
 		int minutes = startTime%60;
 		startTimeString += hours < 10 ? "0" + hours : hours;
 		startTimeString += ":" + (minutes < 10 ? "0" + minutes : minutes);
 		startTimeField.setText(startTimeString);
 	}
 	
 	private void setEndTime(int endTime) {
 		this.dayEnd = endTime;
 		this.endTime = endTime;
 		String endTimeString = "";
 		int hours = endTime/60;
 		int minutes = endTime%60;
 		endTimeString += hours < 10 ? "0" + hours : hours;
 		endTimeString += ":" + (minutes < 10 ? "0" + minutes : minutes);
 		endTimeField.setText(endTimeString);
 	}
 	
 	private boolean checkTimes() {
 		String start = startTimeField.getText();
 		String end = endTimeField.getText();
 		if (start.matches("[0-2][0-9]:[0-5][0-9]") && end.matches("[0-2][0-9]:[0-5][0-9]")) {
 			this.startTime =
 				(60 * Integer.parseInt(start.substring(0,2))) + Integer.parseInt(start.substring(3));
 			this.endTime =
 				(60 * Integer.parseInt(end.substring(0,2))) + Integer.parseInt(end.substring(3));
 			return startTime < endTime;
 		}
 		return false;
 	}
 	
 	public void actionPerformed(ActionEvent e) {
 		if (e.getActionCommand().equals("New Type")) {
 			TypeDto t = NewTypeUI.ShowDialog(this);
 			if (t == null) return;
 			tabbedPane.remove(1);
 			JComponent panel2 = makeNewPracPanel();
 			tabbedPane.addTab("New Practitioner", null, panel2,
 			                  "Select a New Practitioner");
 			remove(tabbedPane);
 			add(tabbedPane, BorderLayout.CENTER);
 			repaint();
 			validate();
 			tabbedPane.setSelectedIndex(1);
 			return;
 		}
 		else if (e.getActionCommand().equals("okNew")) {
 			JLabel msg = new JLabel();
 			msg.setFont(Constants.PARAGRAPH);
 			String firstName = firstNameField.getText();
 			if (!checkTimes()) {
				msg.setText("Please enter valid times: (00:00 - 23:59) with the start time less than the end time.");
 				JOptionPane.showMessageDialog(this, msg, "Error!", JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 			if (firstName.equals("")) {
 				msg.setText( "Please enter a first name.");
 				JOptionPane.showMessageDialog(this, msg, "Error!", JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 			String lastName = lastNameField.getText();
 			if (lastName.equals("")) {
 				msg.setText("Please enter a last name.");
 				JOptionPane.showMessageDialog(this, msg, "Error!", JOptionPane.ERROR_MESSAGE);
 				return;
 			}
			String type = "";
			if (typeCombo.getSelectedIndex() == -1) {
				msg.setText("Please select a type.");
 				JOptionPane.showMessageDialog(this, msg, "Error!", JOptionPane.ERROR_MESSAGE);
 				return;
			} else {
				type = ((TypeDto)typeCombo.getSelectedItem()).getTypeName();
				if (type.equals("")) {
					msg.setText("Please select a type.");
					JOptionPane.showMessageDialog(this, msg, "Error!", JOptionPane.ERROR_MESSAGE);
					return;
				}
 			}
 			int apptLength;
 			try {
 				apptLength = Integer.parseInt(apptLengthField.getText());
 			} catch (Exception ex) {
 				msg.setText("Appointment Length must be an integer value.");
 				JOptionPane.showMessageDialog(this, msg, "Error!", JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 			if (apptLength <= 0) {
 				msg.setText( "Appointment length must be positive.");
 				JOptionPane.showMessageDialog(this, msg, "Error!", JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 			String note = noteField.getText().replaceAll("[\r\n]+", "\t\t");
 			
 			TypeDto t = DataServiceImpl.GLOBAL_DATA_INSTANCE.getType(type);
 			if (t == null) {
 				t = DataServiceImpl.GLOBAL_DATA_INSTANCE.addNewPractitionerType(type);
 			}
 			
 			practitioner = DataServiceImpl.GLOBAL_DATA_INSTANCE.addPractitioner(
                                 t.getTypeID(), firstName, lastName, apptLength, "" , note);
 		} else if (e.getActionCommand().equals("okOld")) {
 			if (!checkTimes()) {
				JLabel msg = new JLabel("Please enter valid times: (00:00 - 23:59) with the start time less than the end time.");
 				msg.setFont(Constants.PARAGRAPH);
 				JOptionPane.showMessageDialog(this, msg, "Error!", JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 			if (startTime > dayEnd || endTime < dayStart) {
 				JLabel msg = new JLabel("Please enter a time range within the hours of operation.");
 				msg.setFont(Constants.PARAGRAPH);
 				JOptionPane.showMessageDialog(this, msg, "Error!", JOptionPane.ERROR_MESSAGE);
 				setStartTime(dayStart);
 				setEndTime(dayEnd);
 				return;
 			}
 			if (startTime < dayStart || endTime > dayEnd) {
 				if (JOptionPane.showConfirmDialog(
 						this, "The specified time range exceeds the range of the day. " +
 								"Continue with the specified range truncated?",
 						"Warning", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) return;
 				if (startTime < dayStart) startTime = dayStart;
 				if (endTime > dayEnd) endTime = dayEnd;
 			}
 			if (pracTable.getSelectedRow() > -1)
 				practitioner = prac.get(pracTable.getSelectedRow());
 			else {
 				JLabel msg = new JLabel("Please select one of the practitioners in the table, or add a new one.");
 				msg.setFont(Constants.PARAGRAPH);
 				JOptionPane.showMessageDialog(this, msg, "Error!", JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 		}
 		selectPractitionerUI.setVisible(false);
     }
 	
 	@Override
 	public void valueChanged(ListSelectionEvent le) {
 		if (pracTable.getSelectedRow() > -1) {
 			PractitionerDto pract = prac.get(pracTable.getSelectedColumn());
 			
 			
 			
 			startTimeField.setText("NOT WORKING YET");
 		} else {
 			startTimeField.setText("NOT WORKING YET");
 			endTimeField.setText("");
 		}
 	}
 	
 	class PracTableModel extends AbstractTableModel {
 
 		private String[] columnNames = { "Name", "Type", "Appointment Length (Minutes)" };
 		
 		public int getColumnCount() {
 			return columnNames.length;
 		}
 		
 		public String getColumnName(int col) {
 			return columnNames[col];
 		}
 
 		public int getRowCount() {
 			return prac.size();
 		}
 
 		public Object getValueAt(int row, int col) {
 			PractitionerDto p = prac.get(row);
 			if (col == 0) 
 				return p.getFirst() + " " + p.getLast();
 			else if (col == 1) 
 				return p.getTypeName();
 			else
 				return p.getApptLength();
 		}
 		
 		public boolean isCellEditable(int row, int col) {
 			return false;
 		}
 		
 	}
 	
 }
