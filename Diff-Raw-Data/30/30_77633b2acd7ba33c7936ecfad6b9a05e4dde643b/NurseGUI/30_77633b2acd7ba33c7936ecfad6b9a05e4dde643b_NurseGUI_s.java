 import javax.swing.ButtonGroup; 
 import javax.swing.JFrame;
 import javax.swing.JButton;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextField;
 import javax.swing.JLabel;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.List;
 import javax.swing.JComboBox;
 import javax.swing.DefaultComboBoxModel;
 import java.awt.SystemColor;
 import javax.swing.UIManager;
 import java.awt.Font;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import javax.swing.JTextPane;
 import java.awt.CardLayout;
 import java.io.IOException;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.border.BevelBorder;
 import javax.swing.border.EtchedBorder;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JSeparator;
 import javax.swing.SwingConstants;
 import javax.swing.ImageIcon;
 import java.awt.Toolkit;
 
 public class NurseGUI {
 
 	public JFrame frame;
 	private JTextField temperatureBox;
 	private JTextField weightBox;
 	private JTextField heightBox;
 	private JTextField bloodPressureTopBox;
 	private JTextField bloodPressureBotBox;
 	private JTextField bloodSugarBox;
 	private JComboBox<String> dayComboBox = new JComboBox<String>();
 	private JComboBox<String> monthComboBox = new JComboBox<String>();
 	private JComboBox<String> yearComboBox = new JComboBox<String>();
 	private JTextPane symptomsBox;
 	private String symptoms;
 	private JTextField firstNameField;
 	private JTextField lastNameField;
 	private JTextField addressField;
 	private JTextField insuranceField;
 	private JTextField numberField;
 	private JLabel lblInsurance, lblAddress, lblPhoneNumber;
 	private JRadioButton nurseButton, patientButton;
 	private JTextField doctorField;
 	private JLabel lblDoctor, lblPatientFound, lblPatientNotFound;
 	private List doctorList;
 	private List patientList;
 	private JPanel nurseMain;
 	private JTextArea textArea;
 	private JPanel editAccount;
 	private JTextField nameBox;
 
 	public NurseGUI() {
 		initialize();
 	}
 
 	private void initialize() {
 
 		frame = new JFrame();
 		frame.setIconImage(Toolkit.getDefaultToolkit().getImage(NurseGUI.class.getResource("/images/app_window_black_icon&32.png")));
 		frame.setResizable(false);
 		frame.setBounds(700, 350, 686, 554);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		frame.getContentPane().setLayout(new CardLayout(0, 0));
 		JPanel mainWindow = new JPanel();
 
 		frame.getContentPane().add(mainWindow, "name_18970707890334");
 		mainWindow.setBackground(SystemColor.textHighlight);
 		mainWindow.setLayout(null);
 
 		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
 		tabbedPane.setBounds(0, 0, 681, 508);
 		mainWindow.add(tabbedPane);
 		tabbedPane.setBorder(UIManager.getBorder("CheckBox.border"));
 
 		nurseMain = new JPanel();
 		nurseMain.setBackground(Color.WHITE);
 		tabbedPane.addTab("My Account", null, nurseMain, null);
 		nurseMain.setLayout(null);
 
 		textArea = new JTextArea();
 		textArea.setEditable(false);
 		textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
 		textArea.setText(Globals.userDatabase.getCurrentUser().print());
 		textArea.setBounds(158, 109, 384, 126);
 		nurseMain.add(textArea);
 
 		JLabel label = new JLabel("Account Information");
 		label.setForeground(SystemColor.textHighlight);
 		label.setFont(new Font("Consolas", Font.BOLD, 17));
 		label.setBounds(240, 11, 190, 27);
 		nurseMain.add(label);
 
 		JLabel lblNurse = new JLabel("Nurse");
 		lblNurse.setForeground(SystemColor.textHighlight);
 		lblNurse.setFont(new Font("Consolas", Font.BOLD, 17));
 		lblNurse.setBounds(594, 11, 78, 27);
 		nurseMain.add(lblNurse);
 
 		JPanel createAccount = new JPanel();
 		tabbedPane.addTab("Create a new Account", null, createAccount, null);
 		createAccount.setBackground(Color.WHITE);
 		createAccount.setLayout(null);
 
 		firstNameField = new JTextField();
 		firstNameField.setBounds(179, 154, 109, 25);
 		createAccount.add(firstNameField);
 		firstNameField.setColumns(10);
 		patientButton = new JRadioButton("Patient");
 		patientButton.setBackground(Color.WHITE);
 		patientButton.setFont(new Font("Monospaced", Font.PLAIN, 12));
 		patientButton.setBounds(266, 53, 137, 23);
 
 		createAccount.add(patientButton);
 		patientButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg1) {
 				lblDoctor.setVisible(true);
 				doctorField.setVisible(true);
 				insuranceField.setVisible(true);
 				numberField.setVisible(true);
 				addressField.setVisible(true);
 				lblInsurance.setVisible(true);
 				lblPhoneNumber.setVisible(true);
 				lblAddress.setVisible(true);
 			}
 		});
 		nurseButton = new JRadioButton("Nurse");
 		nurseButton.setBackground(Color.WHITE);
 		nurseButton.setFont(new Font("Monospaced", Font.PLAIN, 12));
 		nurseButton.setBounds(266, 96, 137, 23);
 
 		createAccount.add(nurseButton);
 		nurseButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg1) {
 				lblDoctor.setVisible(false);
 				doctorField.setVisible(false);
 				insuranceField.setVisible(false);
 				numberField.setVisible(false);
 				addressField.setVisible(false);
 				lblInsurance.setVisible(false);
 				lblPhoneNumber.setVisible(false);
 				lblAddress.setVisible(false);
 			}
 		});
 
 		lastNameField = new JTextField();
 		lastNameField.setBounds(342, 154, 109, 25);
 		createAccount.add(lastNameField);
 		lastNameField.setColumns(10);
 
 		addressField = new JTextField();
 		addressField.setColumns(10);
 		addressField.setBounds(179, 289, 263, 25);
 		createAccount.add(addressField);
 
 		insuranceField = new JTextField();
 		insuranceField.setColumns(10);
 		insuranceField.setBounds(342, 227, 109, 25);
 		createAccount.add(insuranceField);
 
 		numberField = new JTextField();
 		numberField.setColumns(10);
 		numberField.setBounds(179, 227, 109, 25);
 		createAccount.add(numberField);
 
 		JLabel lblAccountType = new JLabel("Account Type");
 		lblAccountType.setFont(new Font("Consolas", Font.PLAIN, 13));
 		lblAccountType.setBounds(151, 73, 109, 14);
 		createAccount.add(lblAccountType);
 
 		JLabel lblFirstName = new JLabel("First Name");
 		lblFirstName.setFont(new Font("Consolas", Font.PLAIN, 13));
 		lblFirstName.setBounds(200, 140, 109, 14);
 		createAccount.add(lblFirstName);
 
 		JLabel lblLastName = new JLabel("Last Name");
 		lblLastName.setFont(new Font("Consolas", Font.PLAIN, 13));
 		lblLastName.setBounds(370, 140, 109, 14);
 		createAccount.add(lblLastName);
 
 		lblPhoneNumber = new JLabel("Phone Number");
 		lblPhoneNumber.setFont(new Font("Consolas", Font.PLAIN, 13));
 		lblPhoneNumber.setBounds(179, 214, 109, 14);
 		createAccount.add(lblPhoneNumber);
 
 		lblInsurance = new JLabel("Insurance");
 		lblInsurance.setFont(new Font("Consolas", Font.PLAIN, 13));
 		lblInsurance.setBounds(342, 214, 109, 14);
 		createAccount.add(lblInsurance);
 
 		lblAddress = new JLabel("Address");
 		lblAddress.setFont(new Font("Consolas", Font.PLAIN, 13));
 		lblAddress.setBounds(179, 273, 109, 14);
 		createAccount.add(lblAddress);
 
 		JLabel lblAccountCreation = new JLabel("Create Account");
 		lblAccountCreation.setForeground(SystemColor.textHighlight);
 		lblAccountCreation.setFont(new Font("Consolas", Font.BOLD, 15));
 		lblAccountCreation.setBackground(Color.WHITE);
 		lblAccountCreation.setBounds(273, 0, 143, 48);
 		createAccount.add(lblAccountCreation);
 
 		JButton btnCreate = new JButton("Create");
 		btnCreate.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (patientButton.isSelected()) {
 					User tempUser;
 					String doctorID = doctorField.getText();
 
 					tempUser = Globals.userDatabase.searchUserID(doctorID);
 
 					if (tempUser != null) {
 						if(firstNameField.getText().length() > 0 && lastNameField.getText().length() > 0)
 						{
 						Doctor patientDoctor = (Doctor) tempUser;
 						String newPin = Globals.userDatabase.generatePassword();
 						String newName = firstNameField.getText()
 								+ lastNameField.getText();
 						Patient newPatient = new Patient(newName, newPin,
 								patientDoctor, insuranceField.getText(),
 								numberField.getText(), addressField.getText());
 						String patientID = Globals.userDatabase
 								.addUser(newPatient);
 						Object[] options = { "OK" };
 						JOptionPane.showOptionDialog(null,
 								"Account was created\nUser ID: " + patientID
 										+ "\nPassword: " + newPin,
 								"Account Created", JOptionPane.DEFAULT_OPTION,
 								JOptionPane.INFORMATION_MESSAGE, null, options,
 								options[0]);
 						frame.dispose();
 						NurseGUI window = new NurseGUI();
 						window.frame.setVisible(true);
 						}
 						else 
 						{
 							Object[] options = { "OK" };
 							JOptionPane.showOptionDialog(null,
 							"Please do not leave fields for First and Last name blank!",
 							"Empty Fields", JOptionPane.DEFAULT_OPTION,
 							JOptionPane.WARNING_MESSAGE, null, options,
 							options[0]);
 						}
 					} else {
 						Object[] options = { "OK" };
 						JOptionPane.showOptionDialog(null,
 								"Please enter the correct Doctor ID",
 								"Wrong Doctor ID", JOptionPane.DEFAULT_OPTION,
 								JOptionPane.WARNING_MESSAGE, null, options,
 								options[0]);
 					}
 
 				}
 				if (nurseButton.isSelected()) {
 					
 					if(firstNameField.getText().length() > 0 && lastNameField.getText().length() > 0)
 					{
 					String newPin = Globals.userDatabase.generatePassword();
 					String newName = firstNameField.getText()
 							+ lastNameField.getText();
 					Nurse newNurse = new Nurse(newName, newPin);
 					String nurseID = Globals.userDatabase.addUser(newNurse);
 					Object[] options = { "OK" };
 					JOptionPane.showOptionDialog(null,
 							"Account was created\nUser ID: " + nurseID
 									+ "\n Password: " + newPin,
 							"Account Created", JOptionPane.DEFAULT_OPTION,
 							JOptionPane.WARNING_MESSAGE, null, options,
 							options[0]);
 					frame.dispose();
 					NurseGUI window = new NurseGUI();
 					window.frame.setVisible(true);
 					}
 					else
 					{
 						Object[] options = { "OK" };
 						JOptionPane.showOptionDialog(null,
 								"Please enter the correct Doctor ID",
 								"Wrong Doctor ID", JOptionPane.DEFAULT_OPTION,
 								JOptionPane.WARNING_MESSAGE, null, options,
 								options[0]);
 					}
 
 				}
 			}
 		});
 		btnCreate.setForeground(Color.BLACK);
 		btnCreate.setFont(new Font("Monospaced", Font.BOLD, 13));
 		btnCreate.setBackground(SystemColor.textHighlight);
 		btnCreate.setBounds(245, 416, 130, 23);
 		createAccount.add(btnCreate);
 
 		doctorField = new JTextField();
 		doctorField.setColumns(10);
 		doctorField.setBounds(179, 352, 109, 25);
 		createAccount.add(doctorField);
 
 		lblDoctor = new JLabel("Doctor ID");
 		lblDoctor.setFont(new Font("Consolas", Font.PLAIN, 13));
 		lblDoctor.setBounds(179, 339, 109, 14);
 		createAccount.add(lblDoctor);
 
 		JMenuBar menuBar = new JMenuBar();
 		menuBar.setBorder(UIManager.getBorder("MenuBar.border"));
 		menuBar.setAlignmentX(Component.LEFT_ALIGNMENT);
 		menuBar.setBackground(Color.WHITE);
 		menuBar.setForeground(Color.BLACK);
 		frame.setJMenuBar(menuBar);
 
 		JMenu mnNewMenu = new JMenu("Menu");
 		mnNewMenu.setForeground(Color.BLACK);
 		mnNewMenu.setBackground(Color.WHITE);
 		menuBar.add(mnNewMenu);
 
 		JMenuItem menuSave = new JMenuItem("Save ");
 		menuSave.setIcon(new ImageIcon(NurseGUI.class.getResource("/images/save_icon&16.png")));
 		mnNewMenu.add(menuSave);
 		menuSave.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 //				try {
 //					Globals.loadSave.saveDatabase();
 //				} catch (IOException e1) {
 //					Object[] options = { "OK" };
 //					JOptionPane.showOptionDialog(null,
 //							"There was a problem with saving to the file",
 //							"Error", JOptionPane.DEFAULT_OPTION,
 //							JOptionPane.WARNING_MESSAGE, null, options,
 //							options[0]);
 //					e1.printStackTrace();
 //				}
 			}
 		});
 		JMenuItem menuEditAccount = new JMenuItem("Edit Account");
		menuEditAccount.setIcon(new ImageIcon(NurseGUI.class.getResource("/images/doc_edit_icon&16.png")));
 		mnNewMenu.add(menuEditAccount);
 		
 				JMenuItem menuLogout = new JMenuItem("Logout");
				menuLogout.setIcon(new ImageIcon(NurseGUI.class.getResource("/images/1366296517_logout.png")));
 				mnNewMenu.add(menuLogout);
 				menuLogout.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 
 						frame.dispose();
 						LoginGUI window = new LoginGUI();
 						window.frame.setVisible(true);
 					}
 				});
 		menuEditAccount.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				frame.getContentPane().removeAll();
 				frame.getContentPane().invalidate();
 
 				frame.getContentPane().add(editAccount);
 				frame.getContentPane().revalidate();
 				frame.repaint();
 			}
 		});
 
 		ButtonGroup radioGroup = new ButtonGroup();
 		radioGroup.add(nurseButton);
 		radioGroup.add(patientButton);
 		JPanel enterPatientInformation = new JPanel();
 		enterPatientInformation.setBackground(Color.WHITE);
 		tabbedPane.addTab("New Patient Entry", null, enterPatientInformation,
 				null);
 		enterPatientInformation.setLayout(null);
 
 		JButton btnEnter = new JButton("ENTER");
 		btnEnter.setBorder(new BevelBorder(BevelBorder.RAISED,
 				Color.LIGHT_GRAY, null, null, null));
 		btnEnter.setFont(new Font("Monospaced", Font.BOLD, 13));
 		btnEnter.setForeground(SystemColor.textHighlight);
 		btnEnter.setBackground(Color.WHITE);
 		btnEnter.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 
 				String[] doubleInputs = { bloodPressureTopBox.getText(),
 						bloodPressureBotBox.getText(), bloodSugarBox.getText(),
 						weightBox.getText(), heightBox.getText(),
 						temperatureBox.getText() };
 				
 				Double[] doubleArray = Globals.inputHandler
 						.checkDoubleInputs(doubleInputs);
 				if (doubleArray != null) 
 				{
 
 					symptoms = symptomsBox.getText();
 
 					int day = Integer.parseInt(dayComboBox.getSelectedItem()
 							.toString());
 					int month = Integer.parseInt(monthComboBox
 							.getSelectedItem().toString());
 					int year = Integer.parseInt(yearComboBox.getSelectedItem()
 							.toString());
 
 					int patientIndex = patientList.getSelectedIndex();
 					Patient tempPatient = null;
 					if (patientIndex >= 0) 
 					{
 						int doctorIndex = doctorList.getSelectedIndex();
 						Doctor doctorSelected = (Doctor) Globals.userDatabase
 								.getUser(doctorIndex);
 						tempPatient = doctorSelected.getPatient(patientIndex);
 
 					}
 					else if(nameBox.getText().length() > 0)
 					{
 						tempPatient = (Patient) Globals.userDatabase.searchUserName(nameBox.getText());
 						
 					}
 					if(tempPatient != null)
 					{
 					
 						Visit newVisit = new Visit(month, day, year,
 								doubleArray[0], doubleArray[1], doubleArray[2],
 								doubleArray[3], doubleArray[4], doubleArray[5],
 								symptoms);
 
 						tempPatient.getMedicalRecord().addVisit(newVisit);
 
 						Object[] options = { "OK" };
 						JOptionPane.showOptionDialog(null,
 								"Patient Form for " + tempPatient.getUserName() + " was added to the database.",
 								"Entry Submitted", JOptionPane.DEFAULT_OPTION,
 								JOptionPane.INFORMATION_MESSAGE, null, options,
 								options[0]);
 						frame.dispose();
 						NurseGUI window = new NurseGUI();
 						window.frame.setVisible(true);
 					}
 					else 
 					{
 						Object[] options = { "OK" };
 						JOptionPane.showOptionDialog(null,
 								"Please select a patient from the list or enter patient's name in the Name box",
 								"No Patient Selected", JOptionPane.DEFAULT_OPTION,
 								JOptionPane.WARNING_MESSAGE, null, options,
 								options[0]);
 					}
 
 					}
 					else 
 					{
 					Object[] options = { "OK" };
 					JOptionPane.showOptionDialog(null,
 							"Please check inputs for errors",
 							"Wrong input type", JOptionPane.DEFAULT_OPTION,
 							JOptionPane.WARNING_MESSAGE, null, options,
 							options[0]);
 					}
 
 			}
 
 		});
 		btnEnter.setBounds(492, 389, 116, 41);
 		enterPatientInformation.add(btnEnter);
 		nameBox = new JTextField();
 		nameBox.setFont(new Font("Monospaced", Font.PLAIN, 12));
 		nameBox.setBounds(241, 200, 146, 20);
 		enterPatientInformation.add(nameBox);
 		nameBox.setColumns(10);
 
 		JLabel lblPatientNameOr = new JLabel("Patient Name");
 		lblPatientNameOr.setForeground(Color.BLACK);
 		lblPatientNameOr.setFont(new Font("Consolas", Font.BOLD, 13));
 		lblPatientNameOr.setBounds(268, 185, 103, 14);
 		enterPatientInformation.add(lblPatientNameOr);
 
 		JButton btnCheck = new JButton("SEARCH");
 		btnCheck.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 
 				User tempUser = Globals.userDatabase.searchUserName(nameBox
 						.getText());
 				if (tempUser != null) {
 					if (tempUser instanceof Patient) {
 						lblPatientFound.setVisible(true);
 						lblPatientNotFound.setVisible(false);
 					} else {
 						lblPatientFound.setVisible(false);
 						lblPatientNotFound.setVisible(true);
 					}
 				} else {
 					lblPatientFound.setVisible(false);
 					lblPatientNotFound.setVisible(true);
 				}
 
 			}
 		});
 		btnCheck.setBorder(new BevelBorder(BevelBorder.RAISED,
 				Color.LIGHT_GRAY, null, null, null));
 		btnCheck.setFont(new Font("Consolas", Font.PLAIN, 13));
 		btnCheck.setForeground(SystemColor.textHighlight);
 		btnCheck.setBackground(Color.WHITE);
 		btnCheck.setBounds(241, 254, 146, 41);
 		enterPatientInformation.add(btnCheck);
 
 		temperatureBox = new JTextField();
 		temperatureBox.setBorder(new BevelBorder(BevelBorder.LOWERED,
 				Color.LIGHT_GRAY, null, null, null));
 		temperatureBox.setBounds(586, 170, 46, 20);
 		enterPatientInformation.add(temperatureBox);
 		temperatureBox.setColumns(10);
 
 		weightBox = new JTextField();
 		weightBox.setBorder(new BevelBorder(BevelBorder.LOWERED,
 				Color.LIGHT_GRAY, null, null, null));
 		weightBox.setBounds(586, 201, 46, 20);
 		enterPatientInformation.add(weightBox);
 		weightBox.setColumns(10);
 
 		heightBox = new JTextField();
 		heightBox.setBorder(new BevelBorder(BevelBorder.LOWERED,
 				Color.LIGHT_GRAY, null, null, null));
 		heightBox.setBounds(586, 232, 46, 20);
 		enterPatientInformation.add(heightBox);
 		heightBox.setColumns(10);
 
 		bloodPressureTopBox = new JTextField();
 		bloodPressureTopBox.setBorder(new BevelBorder(BevelBorder.LOWERED,
 				Color.LIGHT_GRAY, null, null, null));
 		bloodPressureTopBox.setBounds(586, 263, 46, 20);
 		enterPatientInformation.add(bloodPressureTopBox);
 		bloodPressureTopBox.setColumns(10);
 
 		bloodPressureBotBox = new JTextField();
 		bloodPressureBotBox.setBorder(new BevelBorder(BevelBorder.LOWERED,
 				Color.LIGHT_GRAY, null, null, null));
 		bloodPressureBotBox.setBounds(586, 294, 46, 20);
 		enterPatientInformation.add(bloodPressureBotBox);
 		bloodPressureBotBox.setColumns(10);
 
 		bloodSugarBox = new JTextField();
 		bloodSugarBox.setBorder(new BevelBorder(BevelBorder.LOWERED,
 				Color.LIGHT_GRAY, null, null, null));
 		bloodSugarBox.setBounds(586, 325, 46, 20);
 		enterPatientInformation.add(bloodSugarBox);
 		bloodSugarBox.setColumns(10);
 
 		JLabel temperatureLabel = new JLabel("Temperature (F)");
 		temperatureLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
 		temperatureLabel.setBounds(443, 174, 124, 14);
 		enterPatientInformation.add(temperatureLabel);
 
 		JLabel weightLabel = new JLabel("Weight (lbs)");
 		weightLabel.setFont(new Font("Courier New", Font.PLAIN, 12));
 		weightLabel.setBounds(443, 205, 103, 14);
 		enterPatientInformation.add(weightLabel);
 
 		JLabel heightLabel = new JLabel("Height (inches)");
 		heightLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
 		heightLabel.setBounds(443, 238, 124, 14);
 		enterPatientInformation.add(heightLabel);
 
 		JLabel bloodPressureTopLabel = new JLabel("Blood Pressure Top");
 		bloodPressureTopLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
 		bloodPressureTopLabel.setBounds(443, 267, 146, 14);
 		enterPatientInformation.add(bloodPressureTopLabel);
 
 		JLabel bloodSugarLabel = new JLabel("Blood Sugar (mg/dL)");
 		bloodSugarLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
 		bloodSugarLabel.setBounds(443, 328, 165, 14);
 		enterPatientInformation.add(bloodSugarLabel);
 
 		JLabel bloodPressureBotLabel = new JLabel("Blood Pressure Bot");
 		bloodPressureBotLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
 		bloodPressureBotLabel.setBounds(443, 297, 146, 14);
 		enterPatientInformation.add(bloodPressureBotLabel);
 
 		JLabel lblSymptoms = new JLabel("Symptoms:");
 		lblSymptoms.setFont(new Font("Consolas", Font.PLAIN, 12));
 		lblSymptoms.setBounds(443, 94, 74, 14);
 		enterPatientInformation.add(lblSymptoms);
 
 		monthComboBox.setForeground(UIManager.getColor("Menu.foreground"));
 		monthComboBox.setBackground(SystemColor.text);
 		monthComboBox.setFont(new Font("Consolas", Font.PLAIN, 11));
 		monthComboBox
 				.setModel(new DefaultComboBoxModel<String>(new String[] { "1",
 						"2", "3", "4", "5", "6", "7", "8", "9", "10", "11",
 						"12" }));
 		monthComboBox.setToolTipText("1, 2 ,3\r\n");
 		monthComboBox.setBounds(489, 69, 40, 20);
 		enterPatientInformation.add(monthComboBox);
 
 		dayComboBox.setBackground(SystemColor.text);
 		dayComboBox.setFont(new Font("Consolas", Font.PLAIN, 11));
 		dayComboBox.setModel(new DefaultComboBoxModel<String>(new String[] {
 				"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12",
 				"13", "14", "15", "16", "17", "18", "19", "20", "21", "22",
 				"23", "24", "25", "26", "27", "28", "29", "30", "31" }));
 		dayComboBox.setToolTipText("1, 2 ,3\r\n");
 		dayComboBox.setBounds(530, 69, 43, 20);
 		enterPatientInformation.add(dayComboBox);
 
 		yearComboBox.setBackground(SystemColor.text);
 		yearComboBox.setFont(new Font("Consolas", Font.PLAIN, 11));
 		yearComboBox.setModel(new DefaultComboBoxModel<String>(new String[] {
 				"2013", "2014", "2015", "2016", "2017", "2018", "2019", "2020",
 				"2021", "2022" }));
 		yearComboBox.setToolTipText("1, 2 ,3\r\n");
 		yearComboBox.setBounds(574, 69, 58, 20);
 		enterPatientInformation.add(yearComboBox);
 
 		JLabel lblDate = new JLabel("Date");
 		lblDate.setFont(new Font("Consolas", Font.BOLD, 12));
 		lblDate.setBounds(447, 72, 46, 14);
 		enterPatientInformation.add(lblDate);
 
 		JLabel lblMonth = new JLabel("Month");
 		lblMonth.setFont(new Font("Tahoma", Font.PLAIN, 9));
 		lblMonth.setBounds(492, 52, 37, 14);
 		enterPatientInformation.add(lblMonth);
 
 		JLabel lblDay = new JLabel("Day");
 		lblDay.setFont(new Font("Tahoma", Font.PLAIN, 9));
 		lblDay.setBounds(538, 52, 29, 14);
 		enterPatientInformation.add(lblDay);
 
 		JLabel lblYear = new JLabel("Year");
 		lblYear.setFont(new Font("Tahoma", Font.PLAIN, 9));
 		lblYear.setBounds(586, 52, 29, 14);
 		enterPatientInformation.add(lblYear);
 
 		doctorList = new List();
 		doctorList.setForeground(SystemColor.textHighlight);
 		doctorList.setBackground(SystemColor.window);
 		doctorList.setFont(new Font("Monospaced", Font.PLAIN, 14));
 		doctorList.setBounds(10, 69, 184, 173);
 		enterPatientInformation.add(doctorList);
 		patientList = new List();
 		patientList.setBackground(SystemColor.window);
 		patientList.setForeground(SystemColor.textHighlight);
 		patientList.setFont(new Font("Monospaced", Font.PLAIN, 14));
 		patientList.setBounds(10, 293, 184, 173);
 		enterPatientInformation.add(patientList);
 
 		JLabel lblDoctorList = new JLabel("Doctor List");
 		lblDoctorList.setFont(new Font("Consolas", Font.BOLD, 13));
 		lblDoctorList.setBounds(61, 53, 88, 14);
 		enterPatientInformation.add(lblDoctorList);
 
 		JLabel lblPatientList = new JLabel("Patient List");
 		lblPatientList.setFont(new Font("Consolas", Font.BOLD, 13));
 		lblPatientList.setBounds(61, 273, 88, 14);
 		enterPatientInformation.add(lblPatientList);
 
 		JScrollPane scrollPane = new JScrollPane();
 		scrollPane.setBounds(443, 111, 189, 48);
 		enterPatientInformation.add(scrollPane);
 
 		symptomsBox = new JTextPane();
 		scrollPane.setViewportView(symptomsBox);
 		symptomsBox.setBorder(new EtchedBorder(EtchedBorder.LOWERED,
 				Color.LIGHT_GRAY, null));
 		symptomsBox.setFont(new Font("Arial", Font.PLAIN, 12));
 		symptomsBox.setBackground(Color.WHITE);
 
 		JLabel lblPatientVisitForm = new JLabel("Patient Visit Form");
 		lblPatientVisitForm.setForeground(SystemColor.textHighlight);
 		lblPatientVisitForm.setFont(new Font("Consolas", Font.BOLD, 17));
 		lblPatientVisitForm.setBounds(453, 14, 190, 27);
 		enterPatientInformation.add(lblPatientVisitForm);
 
 		JSeparator separator = new JSeparator();
 		separator.setForeground(new Color(0, 0, 0));
 		separator.setOrientation(SwingConstants.VERTICAL);
 		separator.setBounds(412, -22, 18, 498);
 		enterPatientInformation.add(separator);
 
 		lblPatientFound = new JLabel("Patient Found");
 		lblPatientFound.setForeground(new Color(60, 179, 113));
 		lblPatientFound.setFont(new Font("Times New Roman", Font.BOLD, 13));
 		lblPatientFound.setBounds(272, 220, 112, 23);
 		enterPatientInformation.add(lblPatientFound);
 		lblPatientFound.setVisible(false);
 
 		lblPatientNotFound = new JLabel("Patient Not Found");
 		lblPatientNotFound.setForeground(new Color(255, 0, 0));
 		lblPatientNotFound.setFont(new Font("Times New Roman", Font.BOLD, 13));
 		lblPatientNotFound.setBounds(259, 220, 112, 23);
 		enterPatientInformation.add(lblPatientNotFound);
 		
 		JLabel lblPatientSelection = new JLabel("Patient Selection");
 		lblPatientSelection.setForeground(SystemColor.textHighlight);
 		lblPatientSelection.setFont(new Font("Consolas", Font.BOLD, 17));
 		lblPatientSelection.setBounds(140, 14, 190, 27);
 		enterPatientInformation.add(lblPatientSelection);
 		lblPatientNotFound.setVisible(false);
 
 		doctorList.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				patientList.removeAll();
 				int index = doctorList.getSelectedIndex();
 				Doctor doctorSelected = (Doctor) Globals.userDatabase
 						.getUser(index);
 				String[] patientNames = doctorSelected.getPatientNames();
 				index = 0;
 				if (patientNames != null) {
 					int arraySize = patientNames.length;
 					while (index < arraySize) {
 						patientList.add(patientNames[index]);
 						index++;
 					}
 				}
 
 			}
 		});
 
 		editAccount = new JPanel();
 		frame.getContentPane().add(editAccount, "name_46184208883226");
 		editAccount.setBackground(Color.WHITE);
 		editAccount.setLayout(null);
 
 		JButton btnChangePassword = new JButton("Change Password");
 		btnChangePassword.setBorder(new BevelBorder(BevelBorder.RAISED,
 				new Color(128, 128, 128), null, null, null));
 		btnChangePassword.setForeground(Color.BLACK);
 		btnChangePassword.setFont(new Font("Monospaced", Font.BOLD, 13));
 		btnChangePassword.setBackground(Color.WHITE);
 		btnChangePassword.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				RecoverAccountGUI window = new RecoverAccountGUI();
 				window.frame.setVisible(true);
 				window.setToPassword();
 			}
 		});
 		btnChangePassword.setBounds(222, 290, 211, 67);
 		editAccount.add(btnChangePassword);
 
 		JButton btnBack = new JButton("BACK");
 		btnBack.setBorder(new BevelBorder(BevelBorder.RAISED, new Color(160,
 				160, 160), null, null, null));
 		btnBack.setFont(new Font("Monospaced", Font.BOLD, 13));
 		btnBack.setBackground(Color.LIGHT_GRAY);
 		btnBack.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 
 				frame.dispose();
 				NurseGUI window = new NurseGUI();
 				window.frame.setVisible(true);
 
 			}
 		});
 		btnBack.setBounds(10, 11, 114, 23);
 		editAccount.add(btnBack);
 
 		JButton btnSetUpSecurity = new JButton("Add Security Features");
 		btnSetUpSecurity.setBorder(new BevelBorder(BevelBorder.RAISED,
 				new Color(128, 128, 128), null, null, null));
 		btnSetUpSecurity.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 
 				RecoverAccountGUI window = new RecoverAccountGUI();
 				window.frame.setVisible(true);
 				window.setToAnswerQuestions();
 			}
 		});
 		btnSetUpSecurity.setForeground(Color.BLACK);
 		btnSetUpSecurity.setFont(new Font("Monospaced", Font.BOLD, 13));
 		btnSetUpSecurity.setBackground(Color.WHITE);
 		btnSetUpSecurity.setBounds(222, 146, 211, 64);
 		editAccount.add(btnSetUpSecurity);
 
 		String[] doctorNames = Globals.userDatabase.getDoctorNames();
 		int newIndex = 0;
 		if (doctorNames != null) {
 			int arraySize = doctorNames.length;
 			if (arraySize > 0) {
 				while (newIndex < arraySize) {
 					doctorList.add(doctorNames[newIndex]);
 					newIndex++;
 				}
 			}
 		}
 
 	}
 }
