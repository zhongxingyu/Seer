 import java.awt.Color;    
 import java.awt.Font;
 import java.awt.SystemColor;
 import javax.swing.JFrame;
 import java.awt.CardLayout;
 import javax.swing.BorderFactory;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JButton;
 import javax.swing.UIManager;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import javax.swing.JOptionPane;
 import javax.swing.JTextField;
 import javax.swing.JComboBox;
 import javax.swing.JTextArea;
 import javax.swing.JSeparator;
 import javax.swing.JMenuBar;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import java.awt.Component;
 import java.io.IOException;
 import javax.swing.JTabbedPane;
 import javax.swing.border.Border;
 import javax.swing.JList;
 import javax.swing.JScrollPane;
 import javax.swing.border.BevelBorder;
 import javax.swing.ImageIcon;
 import java.awt.Toolkit;
 import org.jfree.ui.RefineryUtilities;
 import javax.swing.SwingConstants;
 
 
 public class PatientGUI {
 	public JFrame frame;
 	private JPanel enterDailyInformation;
 	private JComboBox<String> dayComboBox;
 	private JComboBox<String> monthComboBox;
 	private JComboBox<String> yearComboBox;
 	private int day, month, year;
 	private JTextArea informationWindow;
 	private JTextField WeightBox;
 	private JTextField bloodPressureTopBox;
 	private JTextField bloodPressureBotBox;
 	private JTextField bloodSugarBox;
 	private JComboBox<String> dayComboBox2;
 	private JComboBox<String> monthComboBox2;
 	private JComboBox<String> yearComboBox2;
 	private JPanel mainWindow;
 	private JPanel editAccount;
 	private JTextField addressField;
 	private JTextField insuranceField;
 	private JTextField phoneNumberField;
 	private JTextArea informationDisplay2;
 	private JTextArea textArea;
 	@SuppressWarnings("rawtypes")
 	private JList list;
 	
 
 	/**
 	 * Create the application.
 	 */
 	public PatientGUI() {
 		initialize();
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	private void initialize() {
 		frame = new JFrame();
		frame.setIconImage(Toolkit.getDefaultToolkit().getImage(PatientGUI.class.getResource("/images/app_window_black_icon&32.png")));
 		frame.setResizable(false);
 		frame.setBounds(700, 350, 694, 565);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.getContentPane().setLayout(new CardLayout(0, 0));
 
 		mainWindow = new JPanel();
 		frame.getContentPane().add(mainWindow, "name_18970707890334");
 		mainWindow.setBackground(SystemColor.textHighlight);
 		mainWindow.setLayout(null);
 
 		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
 		tabbedPane.setBorder(UIManager.getBorder("CheckBox.border"));
 		tabbedPane.setBounds(0, 0, 688, 515);
 		mainWindow.add(tabbedPane);
 
 		JPanel AccountInformation = new JPanel();
 		AccountInformation.setBackground(Color.WHITE);
 		tabbedPane.addTab("My Account", null, AccountInformation, null);
 		AccountInformation.setLayout(null);
 
 		textArea = new JTextArea();
 		textArea.setBounds(126, 62, 509, 297);
 		AccountInformation.add(textArea);
 		textArea.setText(Globals.userDatabase.getCurrentUser().print());
 		textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
 
 		textArea.setEditable(false);
 
 		JLabel lblAccountInformation = new JLabel("Account Information");
 		lblAccountInformation.setForeground(SystemColor.textHighlight);
 		lblAccountInformation.setFont(new Font("Consolas", Font.BOLD, 17));
 		lblAccountInformation.setBounds(248, 11, 190, 27);
 		AccountInformation.add(lblAccountInformation);
 
 		JLabel lblToEditThis = new JLabel("To edit this information");
 		lblToEditThis.setFont(new Font("Consolas", Font.PLAIN, 13));
 		lblToEditThis.setBounds(248, 444, 353, 14);
 		AccountInformation.add(lblToEditThis);
 
 		JLabel lblGoToMenuedit = new JLabel("Go to Menu->Edit Account");
 		lblGoToMenuedit.setFont(new Font("Consolas", Font.PLAIN, 13));
 		lblGoToMenuedit.setBounds(248, 458, 353, 14);
 		AccountInformation.add(lblGoToMenuedit);
 		Border borderInfoWindow = BorderFactory.createLineBorder(Color.BLACK);
 
 		Patient currentPatient = (Patient) Globals.userDatabase
 				.getCurrentUser();
 
 		JPanel searchMedicalHistory = new JPanel();
 		tabbedPane.addTab("Medical History", null, searchMedicalHistory, null);
 		searchMedicalHistory.setBackground(Color.WHITE);
 		searchMedicalHistory.setLayout(null);
 		dayComboBox2 = new JComboBox<String>();
 		monthComboBox2 = new JComboBox<String>();
 		yearComboBox2 = new JComboBox<String>();
 
 		monthComboBox2.setForeground(UIManager.getColor("Menu.foreground"));
 		monthComboBox2.setBackground(Color.WHITE);
 		monthComboBox2.setFont(new Font("Consolas", Font.PLAIN, 11));
 		monthComboBox2
 				.setModel(new DefaultComboBoxModel<String>(new String[] { "1",
 						"2", "3", "4", "5", "6", "7", "8", "9", "10", "11",
 						"12" }));
 		monthComboBox2.setToolTipText("1, 2 ,3\r\n");
 		monthComboBox2.setBounds(73, 314, 52, 20);
 		searchMedicalHistory.add(monthComboBox2);
 
 		dayComboBox2.setBackground(Color.WHITE);
 		dayComboBox2.setFont(new Font("Consolas", Font.PLAIN, 11));
 		dayComboBox2.setModel(new DefaultComboBoxModel<String>(new String[] {
 				"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12",
 				"13", "14", "15", "16", "17", "18", "19", "20", "21", "22",
 				"23", "24", "25", "26", "27", "28", "29", "30", "31" }));
 		dayComboBox2.setToolTipText("1, 2 ,3\r\n");
 		dayComboBox2.setBounds(123, 314, 50, 20);
 		searchMedicalHistory.add(dayComboBox2);
 
 		yearComboBox2.setBackground(Color.WHITE);
 		yearComboBox2.setFont(new Font("Consolas", Font.PLAIN, 11));
 		yearComboBox2.setModel(new DefaultComboBoxModel<String>(new String[] {
 				"2013", "2014", "2015", "2016", "2017", "2018", "2019", "2020",
 				"2021", "2022" }));
 		yearComboBox2.setToolTipText("1, 2 ,3\r\n");
 		yearComboBox2.setBounds(172, 314, 58, 20);
 		searchMedicalHistory.add(yearComboBox2);
 
 		JButton btnGetMostRecent = new JButton("Get Most Recent ");
 		btnGetMostRecent.setBorder(new BevelBorder(BevelBorder.RAISED,
 				Color.LIGHT_GRAY, null, null, null));
 		btnGetMostRecent.setForeground(Color.BLACK);
 		btnGetMostRecent.setBackground(Color.WHITE);
 		btnGetMostRecent.setFont(new Font("Consolas", Font.PLAIN, 11));
 		btnGetMostRecent.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 
 				Patient tempPatient = (Patient) Globals.userDatabase
 						.getCurrentUser();
 				Visit tempVisit = tempPatient.getMedicalRecord().getLastVisit();
 				if (tempVisit != null) {
 					informationWindow.setText(tempVisit.print());
 				}
 
 			}
 		});
 		btnGetMostRecent.setBounds(73, 430, 157, 42);
 		JLabel lblViewMedicalInformation = new JLabel("Search By Date");
 
 		JButton btnSearchVisit = new JButton("Search");
 		btnSearchVisit.setBorder(new BevelBorder(BevelBorder.RAISED, new Color(
 				192, 192, 192), null, null, null));
 		JLabel lblMonth = new JLabel("Month");
 		JLabel lblDay = new JLabel("Day");
 		JLabel lblYear = new JLabel("Year");
 
 		searchMedicalHistory.add(btnGetMostRecent);
 		lblMonth.setFont(new Font("Consolas", Font.PLAIN, 10));
 		lblMonth.setBounds(79, 299, 46, 14);
 
 		searchMedicalHistory.add(lblMonth);
 		lblDay.setFont(new Font("Consolas", Font.PLAIN, 10));
 		lblDay.setBounds(129, 299, 44, 14);
 
 		searchMedicalHistory.add(lblDay);
 		lblYear.setFont(new Font("Consolas", Font.PLAIN, 10));
 		lblYear.setBounds(184, 299, 46, 14);
 
 		searchMedicalHistory.add(lblYear);
 		lblViewMedicalInformation
 				.setFont(new Font("Monospaced", Font.BOLD, 13));
 		lblViewMedicalInformation.setBounds(95, 244, 135, 44);
 
 		searchMedicalHistory.add(lblViewMedicalInformation);
 		btnSearchVisit.setForeground(Color.BLACK);
 		btnSearchVisit.setBackground(Color.WHITE);
 		btnSearchVisit.setFont(new Font("Consolas", Font.PLAIN, 11));
 		btnSearchVisit.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 
 				day = Integer.parseInt(dayComboBox2.getSelectedItem()
 						.toString());
 				month = Integer.parseInt(monthComboBox2.getSelectedItem()
 						.toString());
 				year = Integer.parseInt(yearComboBox2.getSelectedItem()
 						.toString());
 				Patient tempPatient = (Patient) Globals.userDatabase
 						.getCurrentUser();
 				Visit tempVisit = tempPatient.getMedicalRecord().getVisit(
 						month, day, year);
 				if (tempVisit != null) {
 					informationWindow.setText(tempVisit.print());
 				}
 				if (tempVisit == null) {
 					informationWindow.setText("No Information to Display.");
 				}
 			}
 		});
 		btnSearchVisit.setBounds(73, 345, 157, 42);
 
 		searchMedicalHistory.add(btnSearchVisit);
 
 		informationWindow = new JTextArea();
 		informationWindow.setFont(new Font("Monospaced", Font.PLAIN, 13));
 		informationWindow.setBounds(332, 42, 337, 430);
 		informationWindow.setBorder(BorderFactory.createCompoundBorder(
 				borderInfoWindow,
 				BorderFactory.createEmptyBorder(10, 10, 10, 10)));
 		searchMedicalHistory.add(informationWindow);
 		informationWindow.setEditable(false);
 
 		JLabel lblMedicalRecord = new JLabel("Medical Record");
 		lblMedicalRecord.setForeground(SystemColor.textHighlight);
 		lblMedicalRecord.setFont(new Font("Consolas", Font.BOLD, 17));
 		lblMedicalRecord.setBounds(424, 11, 190, 27);
 		searchMedicalHistory.add(lblMedicalRecord);
 
 		JLabel lblOr = new JLabel("OR");
 		lblOr.setFont(new Font("Monospaced", Font.BOLD, 13));
 		lblOr.setBounds(142, 380, 46, 44);
 		searchMedicalHistory.add(lblOr);
 
 		JButton btnGetSelected = new JButton("Get Selected");
 		btnGetSelected.setBorder(new BevelBorder(BevelBorder.RAISED, new Color(
 				192, 192, 192), null, null, null));
 		btnGetSelected.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				int index = list.getSelectedIndex();
 				if (index >= 0) {
 					Patient tempPatient = (Patient) Globals.userDatabase
 							.getCurrentUser();
 					Visit tempVisit = tempPatient.getMedicalRecord().getVisit(
 							index);
 					if (tempVisit != null) {
 						informationWindow.setText(tempVisit.print());
 					}
 				}
 			}
 		});
 		btnGetSelected.setForeground(Color.BLACK);
 		btnGetSelected.setFont(new Font("Consolas", Font.PLAIN, 11));
 		btnGetSelected.setBackground(Color.WHITE);
 		btnGetSelected.setBounds(73, 149, 157, 42);
 		searchMedicalHistory.add(btnGetSelected);
 
 		JLabel label_1 = new JLabel("OR");
 		label_1.setFont(new Font("Monospaced", Font.BOLD, 13));
 		label_1.setBounds(142, 202, 31, 44);
 		searchMedicalHistory.add(label_1);
 
 		JScrollPane scrollPane = new JScrollPane();
 		scrollPane.setBounds(61, 29, 181, 101);
 		searchMedicalHistory.add(scrollPane);
 
 		list = new JList(currentPatient.getMedicalRecord().toArray());
 		scrollPane.setViewportView(list);
 		list.setFont(new Font("Monospaced", Font.BOLD, 13));
 		list.setBorder(UIManager.getBorder("List.focusCellHighlightBorder"));
 
 		JPanel viewProgress = new JPanel();
 		viewProgress.setBackground(Color.WHITE);
 		tabbedPane.addTab("Charts and Analysis", null, viewProgress, null);
 		viewProgress.setLayout(null);
 		
 		JButton btnBloodPressure = new JButton("Blood Pressure");
 		btnBloodPressure.setBorder(new BevelBorder(BevelBorder.RAISED, Color.LIGHT_GRAY, null, null, null));
 		btnBloodPressure.setFont(new Font("Consolas", Font.PLAIN, 12));
 		btnBloodPressure.setBackground(Color.WHITE);
 		btnBloodPressure.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				
 				 	Chart bloodPressureChart = new Chart(Globals.BLOODPRESSURE, Globals.BARCHART3D, (Patient) Globals.userDatabase.getCurrentUser());
 				 	bloodPressureChart.pack();
 		            RefineryUtilities.centerFrameOnScreen(bloodPressureChart);
 		            bloodPressureChart.setVisible(true);
 			}
 		});
 		btnBloodPressure.setBounds(28, 178, 157, 48);
 		viewProgress.add(btnBloodPressure);
 		
 		JButton btnHeightWeightChart = new JButton("Weight vs Height");
 		btnHeightWeightChart.setBorder(new BevelBorder(BevelBorder.RAISED, new Color(192, 192, 192), null, null, null));
 		btnHeightWeightChart.setFont(new Font("Consolas", Font.PLAIN, 12));
 		btnHeightWeightChart.setBackground(Color.WHITE);
 		btnHeightWeightChart.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				Chart heightWeightChart = new Chart(Globals.HEIGHTWEIGHT,Globals.BARCHART3D, (Patient) Globals.userDatabase.getCurrentUser());
 				heightWeightChart.pack();
 	            RefineryUtilities.centerFrameOnScreen(heightWeightChart);
 	            heightWeightChart.setVisible(true);
 			}
 		});
 		btnHeightWeightChart.setBounds(28, 115, 157, 48);
 		viewProgress.add(btnHeightWeightChart);
 		
 		JButton btnWeightLineChart = new JButton("Weight");
 		btnWeightLineChart.setBorder(new BevelBorder(BevelBorder.RAISED, Color.LIGHT_GRAY, null, null, null));
 		btnWeightLineChart.setBackground(Color.WHITE);
 		btnWeightLineChart.setFont(new Font("Consolas", Font.PLAIN, 12));
 		btnWeightLineChart.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				
 				  	Chart weightLineChart = new Chart(Globals.WEIGHT, Globals.LINECHART, (Patient) Globals.userDatabase.getCurrentUser());
 		            weightLineChart.pack();
 		            RefineryUtilities.centerFrameOnScreen(weightLineChart);
 		            weightLineChart.setVisible(true);
 			}
 		});
 		btnWeightLineChart.setBounds(260, 178, 157, 48);
 		viewProgress.add(btnWeightLineChart);
 		
 		JButton btnHeightLineChart = new JButton("Height");
 		btnHeightLineChart.setBorder(new BevelBorder(BevelBorder.RAISED, Color.LIGHT_GRAY, null, null, null));
 		btnHeightLineChart.setBackground(Color.WHITE);
 		btnHeightLineChart.setFont(new Font("Consolas", Font.PLAIN, 12));
 		btnHeightLineChart.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				
 					Chart heightLineChart = new Chart(Globals.HEIGHT, Globals.LINECHART, (Patient) Globals.userDatabase.getCurrentUser());
 		            heightLineChart.pack();
 		            RefineryUtilities.centerFrameOnScreen(heightLineChart);
 		            heightLineChart.setVisible(true);
 			}
 		});
 		btnHeightLineChart.setBounds(260, 237, 157, 48);
 		viewProgress.add(btnHeightLineChart);
 		
 	
 		
 		JButton btnSugarLineChart = new JButton("Blood Sugar");
 		btnSugarLineChart.setBorder(new BevelBorder(BevelBorder.RAISED, Color.LIGHT_GRAY, null, null, null));
 		btnSugarLineChart.setFont(new Font("Consolas", Font.PLAIN, 12));
 		btnSugarLineChart.setBackground(Color.WHITE);
 		btnSugarLineChart.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				
 				  Chart sugarLineChart = new Chart(Globals.BLOODSUGAR, Globals.LINECHART, (Patient) Globals.userDatabase.getCurrentUser());
 		            sugarLineChart.pack();
 		            RefineryUtilities.centerFrameOnScreen(sugarLineChart);
 		            sugarLineChart.setVisible(true);
 			}
 		});
 		btnSugarLineChart.setBounds(260, 115, 157, 48);
 		viewProgress.add(btnSugarLineChart);
 		
 		JButton btnWeightBarChart = new JButton("Weight");
 		btnWeightBarChart.setBorder(new BevelBorder(BevelBorder.RAISED, Color.LIGHT_GRAY, null, null, null));
 		btnWeightBarChart.setBackground(Color.WHITE);
 		btnWeightBarChart.setFont(new Font("Consolas", Font.PLAIN, 12));
 		btnWeightBarChart.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				
 				   Chart weightBarChart = new Chart(Globals.WEIGHT, Globals.BARCHART, (Patient) Globals.userDatabase.getCurrentUser());
 		            weightBarChart.pack();
 		            RefineryUtilities.centerFrameOnScreen(weightBarChart);
 		            weightBarChart.setVisible(true);
 			}
 		});
 		btnWeightBarChart.setBounds(498, 178, 157, 48);
 		viewProgress.add(btnWeightBarChart);
 		
 		JButton btnHeightBarChart = new JButton("Height");
 		btnHeightBarChart.setBorder(new BevelBorder(BevelBorder.RAISED, Color.LIGHT_GRAY, null, null, null));
 		btnHeightBarChart.setBackground(Color.WHITE);
 		btnHeightBarChart.setFont(new Font("Consolas", Font.PLAIN, 12));
 		btnHeightBarChart.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				
 			    Chart heightBarChart = new Chart(Globals.HEIGHT, Globals.BARCHART, (Patient) Globals.userDatabase.getCurrentUser());
 	            heightBarChart.pack();
 	            RefineryUtilities.centerFrameOnScreen(heightBarChart);
 	            heightBarChart.setVisible(true);
 	            
 			}
 		});
 		btnHeightBarChart.setBounds(498, 237, 157, 48);
 		viewProgress.add(btnHeightBarChart);
 		
 		JButton btnSugarBarChart = new JButton("Blood Sugar");
 		btnSugarBarChart.setBorder(new BevelBorder(BevelBorder.RAISED, Color.LIGHT_GRAY, null, null, null));
 		btnSugarBarChart.setBackground(Color.WHITE);
 		btnSugarBarChart.setFont(new Font("Consolas", Font.PLAIN, 12));
 		btnSugarBarChart.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				
 			      	Chart sugarBarChart = new Chart(Globals.BLOODSUGAR, Globals.BARCHART, (Patient) Globals.userDatabase.getCurrentUser());
 		            sugarBarChart.pack();
 		            RefineryUtilities.centerFrameOnScreen(sugarBarChart);
 		            sugarBarChart.setVisible(true);
 			}
 		});
 		btnSugarBarChart.setBounds(498, 115, 157, 48);
 		viewProgress.add(btnSugarBarChart);
 		
 		JLabel lblBarCharts = new JLabel("Bar Charts");
 		lblBarCharts.setFont(new Font("Consolas", Font.BOLD, 17));
 		lblBarCharts.setForeground(SystemColor.textHighlight);
 		lblBarCharts.setBackground(Color.WHITE);
 		lblBarCharts.setBounds(529, 46, 140, 29);
 		viewProgress.add(lblBarCharts);
 		
 		JLabel lblLineCharts = new JLabel("Line Charts");
 		lblLineCharts.setForeground(SystemColor.textHighlight);
 		lblLineCharts.setFont(new Font("Consolas", Font.BOLD, 17));
 		lblLineCharts.setBackground(Color.WHITE);
 		lblLineCharts.setBounds(279, 43, 126, 35);
 		viewProgress.add(lblLineCharts);
 		
 		JLabel lbldBarCharts = new JLabel("3D Bar Charts");
 		lbldBarCharts.setForeground(SystemColor.textHighlight);
 		lbldBarCharts.setFont(new Font("Consolas", Font.BOLD, 17));
 		lbldBarCharts.setBackground(Color.WHITE);
 		lbldBarCharts.setBounds(39, 43, 146, 35);
 		viewProgress.add(lbldBarCharts);
 		
 		JSeparator separator = new JSeparator();
 		separator.setBackground(Color.BLACK);
 		separator.setOrientation(SwingConstants.VERTICAL);
 		separator.setBounds(222, 0, 21, 483);
 		viewProgress.add(separator);
 		
 		JSeparator separator_2 = new JSeparator();
 		separator_2.setOrientation(SwingConstants.VERTICAL);
 		separator_2.setBackground(Color.BLACK);
 		separator_2.setBounds(455, 0, 35, 483);
 		viewProgress.add(separator_2);
 		
 		JButton btnheightWeightLineChart = new JButton("Weight vs Height");
 		btnheightWeightLineChart.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				
 				Chart heightWeightChart2 = new Chart(Globals.HEIGHTWEIGHT, Globals.LINECHART, (Patient) Globals.userDatabase.getCurrentUser());
 				heightWeightChart2.pack();
 	            RefineryUtilities.centerFrameOnScreen(heightWeightChart2);
 	            heightWeightChart2.setVisible(true);
 			}
 		});
 		btnheightWeightLineChart.setFont(new Font("Consolas", Font.PLAIN, 12));
 		btnheightWeightLineChart.setBorder(new BevelBorder(BevelBorder.RAISED, Color.LIGHT_GRAY, null, null, null));
 		btnheightWeightLineChart.setBackground(Color.WHITE);
 		btnheightWeightLineChart.setBounds(260, 296, 157, 48);
 		viewProgress.add(btnheightWeightLineChart);
 		
 		JButton btnBloodPressure2 = new JButton("Blood Pressure");
 		btnBloodPressure2.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				
 				Chart bloodPressureChart2 = new Chart(Globals.BLOODPRESSURE, Globals.LINECHART, (Patient) Globals.userDatabase.getCurrentUser());
 				bloodPressureChart2.pack();
 	            RefineryUtilities.centerFrameOnScreen(bloodPressureChart2);
 	            bloodPressureChart2.setVisible(true);
 			}
 		});
 		btnBloodPressure2.setFont(new Font("Consolas", Font.PLAIN, 12));
 		btnBloodPressure2.setBorder(new BevelBorder(BevelBorder.RAISED, Color.LIGHT_GRAY, null, null, null));
 		btnBloodPressure2.setBackground(Color.WHITE);
 		btnBloodPressure2.setBounds(260, 358, 157, 48);
 		viewProgress.add(btnBloodPressure2);
          
 
 		
 		editAccount = new JPanel();
 		frame.getContentPane().add(editAccount, "name_46184208883226");
 		editAccount.setBackground(Color.WHITE);
 		editAccount.setLayout(null);
 
 		addressField = new JTextField();
 		addressField.setBorder(new BevelBorder(BevelBorder.LOWERED,
 				Color.LIGHT_GRAY, null, null, null));
 		addressField.setBounds(35, 426, 355, 23);
 		editAccount.add(addressField);
 		addressField.setColumns(10);
 
 		insuranceField = new JTextField();
 		insuranceField.setBorder(new BevelBorder(BevelBorder.LOWERED,
 				Color.LIGHT_GRAY, null, null, null));
 		insuranceField.setBounds(220, 258, 170, 20);
 		editAccount.add(insuranceField);
 		insuranceField.setColumns(10);
 
 		phoneNumberField = new JTextField();
 		phoneNumberField.setBorder(new BevelBorder(BevelBorder.LOWERED,
 				Color.LIGHT_GRAY, null, null, null));
 		phoneNumberField.setColumns(10);
 		phoneNumberField.setBounds(35, 258, 170, 20);
 		editAccount.add(phoneNumberField);
 
 		JButton btnUpdateInsurance = new JButton("Update Insurance");
 		btnUpdateInsurance.setBorder(new BevelBorder(BevelBorder.RAISED,
 				Color.LIGHT_GRAY, null, null, null));
 		btnUpdateInsurance.setForeground(Color.BLACK);
 		btnUpdateInsurance.setBackground(Color.WHITE);
 		btnUpdateInsurance.setFont(new Font("Monospaced", Font.BOLD, 13));
 		btnUpdateInsurance.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				String insurance = insuranceField.getText();
 				if (insurance.length() > 0) {
 					Patient tempPatient = (Patient) Globals.userDatabase
 							.getCurrentUser();
 					tempPatient.setInsurance(insurance);
 					insuranceField.setText("");
 				}
 
 			}
 		});
 		btnUpdateInsurance.setBounds(220, 289, 170, 33);
 		editAccount.add(btnUpdateInsurance);
 
 		JButton btnUpdateNumber = new JButton("Update Number");
 		btnUpdateNumber.setBorder(new BevelBorder(BevelBorder.RAISED,
 				Color.LIGHT_GRAY, null, null, null));
 		btnUpdateNumber.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 
 				String phoneNumber = phoneNumberField.getText();
 				if (phoneNumber.length() > 0) {
 
 					Patient tempPatient = (Patient) Globals.userDatabase
 							.getCurrentUser();
 					tempPatient.setPhoneNumber(phoneNumber);
 					phoneNumberField.setText("");
 				}
 			}
 
 		});
 		btnUpdateNumber.setForeground(Color.BLACK);
 		btnUpdateNumber.setBackground(Color.WHITE);
 		btnUpdateNumber.setFont(new Font("Monospaced", Font.BOLD, 13));
 		btnUpdateNumber.setBounds(35, 289, 170, 33);
 		editAccount.add(btnUpdateNumber);
 
 		JButton btnUpdateAddress = new JButton("Update Address");
 		btnUpdateAddress.setBorder(new BevelBorder(BevelBorder.RAISED,
 				Color.LIGHT_GRAY, null, null, null));
 		btnUpdateAddress.setForeground(Color.BLACK);
 		btnUpdateAddress.setBackground(Color.WHITE);
 		btnUpdateAddress.setFont(new Font("Monospaced", Font.BOLD, 13));
 		btnUpdateAddress.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				String address = addressField.getText();
 				if (address.length() > 0) {
 					Patient tempPatient = (Patient) Globals.userDatabase
 							.getCurrentUser();
 					tempPatient.setAddress(address);
 					addressField.setText("");
 				}
 			}
 		});
 		btnUpdateAddress.setBounds(221, 460, 169, 33);
 		editAccount.add(btnUpdateAddress);
 
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
 		btnChangePassword.setBounds(442, 426, 211, 67);
 		editAccount.add(btnChangePassword);
 
 		informationDisplay2 = new JTextArea();
 		informationDisplay2.setBorder(UIManager.getBorder("TextArea.border"));
 		informationDisplay2.setEditable(false);
 		informationDisplay2.setBackground(Color.WHITE);
 		informationDisplay2.setBounds(78, 11, 530, 205);
 		informationDisplay2.setText(Globals.userDatabase.getCurrentUser()
 				.print());
 		informationDisplay2.setFont(new Font("Monospaced", Font.PLAIN, 13));
 		editAccount.add(informationDisplay2);
 
 		JSeparator separator_1 = new JSeparator();
 		separator_1.setForeground(Color.LIGHT_GRAY);
 		separator_1.setBackground(Color.BLACK);
 		separator_1.setBounds(0, 227, 684, 8);
 		editAccount.add(separator_1);
 
 		JButton btnRefresh = new JButton("");
		btnRefresh.setIcon(new ImageIcon(PatientGUI.class.getResource("/images/refresh_icon&32.png")));
 		btnRefresh.setBorder(null);
 		btnRefresh.setForeground(Color.BLACK);
 		btnRefresh.setBackground(Color.WHITE);
 		btnRefresh.setFont(new Font("Monospaced", Font.BOLD, 13));
 		btnRefresh.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 
 				informationDisplay2.setText(Globals.userDatabase
 						.getCurrentUser().print());
 			}
 		});
 		btnRefresh.setBounds(618, 0, 71, 42);
 		editAccount.add(btnRefresh);
 
 		JLabel lblNewLabel = new JLabel("Phone Number");
 		lblNewLabel.setFont(new Font("Consolas", Font.PLAIN, 13));
 		lblNewLabel.setBounds(35, 243, 89, 14);
 		editAccount.add(lblNewLabel);
 
 		JLabel lblInsurance = new JLabel("Insurance");
 		lblInsurance.setFont(new Font("Courier New", Font.PLAIN, 13));
 		lblInsurance.setBounds(220, 242, 89, 14);
 		editAccount.add(lblInsurance);
 
 		JLabel lblAddress = new JLabel("Address");
 		lblAddress.setFont(new Font("Consolas", Font.PLAIN, 13));
 		lblAddress.setBounds(35, 411, 89, 14);
 		editAccount.add(lblAddress);
 
 		JButton btnBack = new JButton("");
		btnBack.setIcon(new ImageIcon(PatientGUI.class.getResource("/images/round_arrow_left_icon&32.png")));
 		btnBack.setBorder(null);
 		btnBack.setFont(new Font("Monospaced", Font.BOLD, 13));
 		btnBack.setBackground(Color.WHITE);
 		btnBack.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 
 				frame.getContentPane().removeAll();
 				frame.getContentPane().invalidate();
 
 				frame.getContentPane().add(mainWindow);
 				frame.getContentPane().revalidate();
 				frame.repaint();
 				textArea.setText(Globals.userDatabase.getCurrentUser().print());
 			}
 		});
 		btnBack.setBounds(0, 0, 71, 42);
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
 		btnSetUpSecurity.setBounds(442, 258, 211, 64);
 		editAccount.add(btnSetUpSecurity);
 
 		enterDailyInformation = new JPanel();
 		frame.getContentPane()
 				.add(enterDailyInformation, "name_46171595047936");
 		enterDailyInformation.setBackground(Color.WHITE);
 		enterDailyInformation.setLayout(null);
 
 		// ------------------------------------------------------------------
 		// Search Medical History
 		// --------------------------------------------------------------------
 
 		dayComboBox = new JComboBox<String>();
 		monthComboBox = new JComboBox<String>();
 		yearComboBox = new JComboBox<String>();
 		enterDailyInformation.setLayout(null);
 
 		JButton btnEnter = new JButton("SUBMIT");
 		btnEnter.setBorder(new BevelBorder(BevelBorder.RAISED,
 				Color.LIGHT_GRAY, null, null, null));
 		btnEnter.setFont(new Font("Monospaced", Font.BOLD, 17));
 		btnEnter.setForeground(SystemColor.textHighlight);
 		btnEnter.setBackground(Color.WHITE);
 		btnEnter.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 
 				
 				String[] doubleInputs = {bloodPressureTopBox.getText(), bloodPressureBotBox.getText(),bloodSugarBox.getText(),
 						 WeightBox.getText()};
 				System.out.println(WeightBox.getText());
 				Double[] doubleArray = Globals.inputHandler.checkDoubleInputs(doubleInputs);
 				if(doubleArray != null)
 				{
 					int day = Integer.parseInt(dayComboBox.getSelectedItem()
 							.toString());
 					int month = Integer.parseInt(monthComboBox.getSelectedItem()
 							.toString());
 					int year = Integer.parseInt(yearComboBox.getSelectedItem()
 							.toString());
 
 					User tempUser = Globals.userDatabase.getCurrentUser();
 
 					if (tempUser instanceof Patient) 
 					{
 
 						Visit newVisit = new Visit(month, day, year,
 								doubleArray[0], doubleArray[1], doubleArray[2],
 								doubleArray[3]);
 						Patient tempPatient = (Patient) tempUser;
 						tempPatient.getMedicalRecord().addVisit(newVisit);
 
 					}
 				}
 			
 				
 			}
 
 		});
 
 		btnEnter.setBounds(281, 438, 148, 40);
 		enterDailyInformation.add(btnEnter);
 
 		WeightBox = new JTextField();
 		WeightBox.setBorder(new BevelBorder(BevelBorder.LOWERED,
 				Color.LIGHT_GRAY, null, null, null));
 		WeightBox.setBackground(Color.WHITE);
 		WeightBox.setBounds(300, 100, 103, 20);
 		enterDailyInformation.add(WeightBox);
 		WeightBox.setColumns(10);
 
 		bloodPressureTopBox = new JTextField();
 		bloodPressureTopBox.setBorder(new BevelBorder(BevelBorder.LOWERED,
 				Color.LIGHT_GRAY, null, null, null));
 		bloodPressureTopBox.setBackground(Color.WHITE);
 		bloodPressureTopBox.setBounds(366, 306, 37, 20);
 		enterDailyInformation.add(bloodPressureTopBox);
 		bloodPressureTopBox.setColumns(10);
 
 		bloodPressureBotBox = new JTextField();
 		bloodPressureBotBox.setBorder(new BevelBorder(BevelBorder.LOWERED,
 				Color.LIGHT_GRAY, null, null, null));
 		bloodPressureBotBox.setBackground(Color.WHITE);
 		bloodPressureBotBox.setBounds(300, 306, 37, 20);
 		enterDailyInformation.add(bloodPressureBotBox);
 		bloodPressureBotBox.setColumns(10);
 
 		bloodSugarBox = new JTextField();
 		bloodSugarBox.setBorder(new BevelBorder(BevelBorder.LOWERED,
 				Color.LIGHT_GRAY, null, null, null));
 		bloodSugarBox.setBackground(Color.WHITE);
 		bloodSugarBox.setBounds(300, 200, 103, 20);
 		enterDailyInformation.add(bloodSugarBox);
 		bloodSugarBox.setColumns(10);
 
 		JLabel weightLabel = new JLabel("Weight ");
 		weightLabel.setFont(new Font("Consolas", Font.BOLD, 13));
 		weightLabel.setBounds(298, 80, 73, 14);
 		enterDailyInformation.add(weightLabel);
 
 		JLabel bloodPressureTopLabel = new JLabel("Blood Pressure ");
 		bloodPressureTopLabel.setFont(new Font("Consolas", Font.BOLD, 13));
 		bloodPressureTopLabel.setBounds(300, 280, 137, 14);
 		enterDailyInformation.add(bloodPressureTopLabel);
 
 		JLabel bloodSugarLabel = new JLabel("Blood Sugar ");
 		bloodSugarLabel.setFont(new Font("Consolas", Font.BOLD, 13));
 		bloodSugarLabel.setBounds(300, 180, 109, 14);
 		enterDailyInformation.add(bloodSugarLabel);
 
 		monthComboBox.setForeground(UIManager.getColor("Menu.foreground"));
 		monthComboBox.setBackground(SystemColor.text);
 		monthComboBox.setFont(new Font("Consolas", Font.PLAIN, 11));
 		monthComboBox
 				.setModel(new DefaultComboBoxModel<String>(new String[] { "1",
 						"2", "3", "4", "5", "6", "7", "8", "9", "10", "11",
 						"12" }));
 		monthComboBox.setToolTipText("1, 2 ,3\r\n");
 		monthComboBox.setBounds(280, 377, 46, 20);
 		enterDailyInformation.add(monthComboBox);
 
 		dayComboBox.setBackground(SystemColor.text);
 		dayComboBox.setFont(new Font("Consolas", Font.PLAIN, 11));
 		dayComboBox.setModel(new DefaultComboBoxModel<String>(new String[] {
 				"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12",
 				"13", "14", "15", "16", "17", "18", "19", "20", "21", "22",
 				"23", "24", "25", "26", "27", "28", "29", "30", "31" }));
 		dayComboBox.setToolTipText("1, 2 ,3\r\n");
 		dayComboBox.setBounds(325, 377, 46, 20);
 		enterDailyInformation.add(dayComboBox);
 
 		yearComboBox.setBackground(SystemColor.text);
 		yearComboBox.setFont(new Font("Consolas", Font.PLAIN, 11));
 		yearComboBox.setModel(new DefaultComboBoxModel<String>(new String[] {
 				"2013", "2014", "2015", "2016", "2017", "2018", "2019", "2020",
 				"2021", "2022" }));
 		yearComboBox.setToolTipText("1, 2 ,3\r\n");
 		yearComboBox.setBounds(371, 377, 58, 20);
 		enterDailyInformation.add(yearComboBox);
 
 		JLabel lblDate = new JLabel("Date");
 		lblDate.setFont(new Font("Tahoma", Font.BOLD, 11));
 		lblDate.setBounds(246, 379, 46, 14);
 		enterDailyInformation.add(lblDate);
 
 		JLabel lblMonth2 = new JLabel("Month");
 		lblMonth2.setFont(new Font("Tahoma", Font.PLAIN, 9));
 		lblMonth2.setBounds(289, 363, 37, 14);
 		enterDailyInformation.add(lblMonth2);
 
 		JLabel lblDay2 = new JLabel("Day");
 		lblDay2.setFont(new Font("Tahoma", Font.PLAIN, 9));
 		lblDay2.setBounds(338, 363, 29, 14);
 		enterDailyInformation.add(lblDay2);
 
 		JLabel lblYear2 = new JLabel("Year");
 		lblYear2.setFont(new Font("Tahoma", Font.PLAIN, 9));
 		lblYear2.setBounds(381, 362, 37, 17);
 		enterDailyInformation.add(lblYear2);
 
 		JButton btnCancel = new JButton("CANCEL");
 		btnCancel.setBorder(new BevelBorder(BevelBorder.RAISED,
 				SystemColor.controlShadow, null, null, null));
 		btnCancel.setFont(new Font("Monospaced", Font.PLAIN, 13));
 		btnCancel.setForeground(Color.BLACK);
 		btnCancel.setBackground(Color.LIGHT_GRAY);
 		btnCancel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				frame.getContentPane().removeAll();
 				frame.getContentPane().invalidate();
 
 				frame.getContentPane().add(mainWindow);
 				frame.getContentPane().revalidate();
 				frame.repaint();
 			}
 		});
 		btnCancel.setBounds(10, 11, 89, 23);
 		enterDailyInformation.add(btnCancel);
 
 		JLabel lblmgdl = new JLabel("(mg/dL)");
 		lblmgdl.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
 		lblmgdl.setBounds(413, 204, 58, 14);
 		enterDailyInformation.add(lblmgdl);
 
 		JLabel lbllbs = new JLabel("(lbs)");
 		lbllbs.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
 		lbllbs.setBounds(413, 104, 73, 14);
 		enterDailyInformation.add(lbllbs);
 
 		JLabel label = new JLabel("/");
 		label.setFont(new Font("Consolas", Font.PLAIN, 12));
 		label.setBounds(347, 310, 29, 14);
 		enterDailyInformation.add(label);
 
 		JLabel lblsystolicdiastolic = new JLabel("(Systolic/Diastolic)");
 		lblsystolicdiastolic.setFont(new Font("Segoe UI Semibold", Font.PLAIN,
 				12));
 		lblsystolicdiastolic.setBounds(413, 309, 109, 14);
 		enterDailyInformation.add(lblsystolicdiastolic);
 
 		JLabel lblNewEntry = new JLabel("Medical Entry Form");
 		lblNewEntry.setForeground(SystemColor.textHighlight);
 		lblNewEntry.setFont(new Font("Consolas", Font.BOLD, 28));
 		lblNewEntry.setBounds(220, 11, 342, 62);
 		enterDailyInformation.add(lblNewEntry);
 
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
 
 		JMenuItem menuSave = new JMenuItem("Save");
		menuSave.setIcon(new ImageIcon(PatientGUI.class.getResource("/images/save_icon&16.png")));
 		mnNewMenu.add(menuSave);
 		menuSave.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 //				try {
 //					Globals.fileHandler.saveDatabase();
 //					System.out.println("saved");
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
 
 		JMenuItem menuNewEntry = new JMenuItem("New Entry");
		menuNewEntry.setIcon(new ImageIcon(PatientGUI.class.getResource("/images/doc_plus_icon&16.png")));
 		mnNewMenu.add(menuNewEntry);
 		menuNewEntry.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				frame.getContentPane().removeAll();
 				frame.getContentPane().invalidate();
 
 				frame.getContentPane().add(enterDailyInformation);
 				frame.getContentPane().revalidate();
 				frame.repaint();
 			}
 		});
 
 		JMenuItem menuEditAccount = new JMenuItem("Edit Account");
		menuEditAccount.setIcon(new ImageIcon(PatientGUI.class.getResource("/images/doc_edit_icon&16.png")));
 		mnNewMenu.add(menuEditAccount);
 		menuEditAccount.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				frame.getContentPane().removeAll();
 				frame.getContentPane().invalidate();
 
 				frame.getContentPane().add(editAccount);
 				frame.getContentPane().revalidate();
 				frame.repaint();
 			}
 		});
 
 		JMenuItem menuLogout = new JMenuItem("Logout");
		menuLogout.setIcon(new ImageIcon(PatientGUI.class.getResource("/images/1366296517_logout.png")));
 		mnNewMenu.add(menuLogout);
 		menuLogout.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 
 				frame.dispose();
 				LoginGUI window = new LoginGUI();
 				window.frame.setVisible(true);
 			}
 		});
 
 	}
 }
