 package hms.views;
 
 import hms.controllers.NurseController;
 import hms.controllers.PatientManager;
 import hms.controllers.UserController;
 
 import javax.swing.JFrame;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.JLabel;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextField;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.JSeparator;
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.JButton;
 import javax.swing.JScrollPane;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.Toolkit;
 import java.sql.SQLException;
 import java.util.Vector;
 import hms.models.*;
 
 import hms.models.PatientTableModel;
 import javax.swing.JRadioButton;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ChangeEvent;
 import javax.swing.JPanel;
 import javax.swing.border.MatteBorder;
 import java.awt.Color;
 import javax.swing.border.EtchedBorder;
 import java.awt.Font;
 import java.awt.Component;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 public class MainView {
 
 	public JFrame frmMain;
 	private JTable tablePatients;
 	private JTable tableNurses;
 	private PatientManager patientManager;
 	private PatientTableModel patientTableModel;
 	private NurseTableModel nurseTableModel;
 	private UserController userController;
 	private NurseController nurseController;
 	private JLabel wardNumber;
 	private JLabel email;
 	private JLabel phoneNumber;
 	private JLabel address;
 	private JLabel room;
 	private JLabel bed;
 	private String searchTerm;
 	private JButton btnSearch;
 	private JTextField txtSearchBar;
 
 	/**
 	 * Create the application.
 	 */
 	public MainView() {
 		initialize();
 
 		address.setText("");
 		phoneNumber.setText("");
 		email.setText("");
 		wardNumber.setText("");
 		room.setText("");
 		bed.setText("");
 
 		maximizeWindow();
 		patientManager = new PatientManager(patientTableModel);
 		nurseController = new NurseController(nurseTableModel);
 	}
 
 	private void maximizeWindow() {
 		frmMain.setExtendedState(frmMain.getExtendedState() | JFrame.MAXIMIZED_BOTH);
 	}
 	
 	private String[] getSelectedNurse() {
 		Object[][] content = nurseTableModel.getContent();
 		Object[] selectedRow = content[tableNurses.getSelectedRow()];
 		String[] strings = new String[content[tableNurses.getSelectedRow()].length];
 		for(int i = 0; i< strings.length; i++){
 			strings[i] = content[tableNurses.getSelectedRow()][i].toString();
 		}
 		
 		return strings;
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frmMain = new JFrame();
 		frmMain.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getClassLoader().getResource("icon.png")));
 		frmMain.setTitle("Main");
 		frmMain.setBounds(100, 100, 2000, 2000);
 		frmMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		JLabel lblNewLabel = new JLabel("Hello");
 
 		JLabel lblUsername = new JLabel("Username");
 
 		patientTableModel = new PatientTableModel();
 		
 		nurseTableModel = new NurseTableModel();
 
 		JButton deletePatientButton = new JButton("Delete Patient");
 
 		JTabbedPane patientsUsersNursesTabbedPane = new JTabbedPane(JTabbedPane.TOP);
 
 		GroupLayout groupLayout = new GroupLayout(frmMain.getContentPane());
 		groupLayout.setHorizontalGroup(
 			groupLayout.createParallelGroup(Alignment.LEADING)
 				.addGroup(groupLayout.createSequentialGroup()
 					.addGap(18)
 					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 						.addGroup(groupLayout.createSequentialGroup()
 							.addComponent(lblNewLabel)
 							.addGap(18)
 							.addComponent(lblUsername, GroupLayout.PREFERRED_SIZE, 137, GroupLayout.PREFERRED_SIZE))
 						.addComponent(patientsUsersNursesTabbedPane, GroupLayout.PREFERRED_SIZE, 1337, GroupLayout.PREFERRED_SIZE))
 					.addContainerGap(15, Short.MAX_VALUE))
 		);
 		groupLayout.setVerticalGroup(
 			groupLayout.createParallelGroup(Alignment.LEADING)
 				.addGroup(groupLayout.createSequentialGroup()
 					.addContainerGap()
 					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
 						.addComponent(lblNewLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 						.addComponent(lblUsername, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(patientsUsersNursesTabbedPane, GroupLayout.PREFERRED_SIZE, 708, GroupLayout.PREFERRED_SIZE)
 					.addContainerGap())
 		);
 
 		JPanel panel = new JPanel();
 		patientsUsersNursesTabbedPane.addTab("Patients", null, panel, null);
 		tablePatients = new JTable(patientTableModel);
 		tablePatients.setAutoCreateRowSorter(true);
 		JScrollPane jsp = new JScrollPane(tablePatients);
 
 		JButton btnCreatePatient = new JButton("Create Patient");
 		btnCreatePatient.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				patientManager.CreatePatient(frmMain);
 			}
 		});
 
 
 		JButton btnEditPatient = new JButton("Edit Patient");
 		btnEditPatient.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				if(tablePatients.getSelectedRowCount() == 1){
 					Object[][] content = patientTableModel.getContent();
 					Object[] selectedRow = content[tablePatients.getSelectedRow()];
 					String[] strs = new String[content[tablePatients.getSelectedRow()].length];
 					for(int i = 0; i< strs.length; i++){
 						strs[i] = content[tablePatients.getSelectedRow()][i].toString();
 					}
 					patientManager.EditPatient(frmMain, strs);
 				}
 			}
 		});
 
 		JButton btnDeletePatient = new JButton("Delete Patient");
 		btnDeletePatient.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if(tablePatients.getSelectedRowCount() == 1){
 					Object[][] content = patientTableModel.getContent();
 					String healthcareNumber = content[tablePatients.getSelectedRow()][0].toString();
 					patientManager.deletePatient(frmMain, healthcareNumber);
 				}
 			}
 		});
 
 		JButton btnRefresh = new JButton("Refresh");
 		btnRefresh.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				patientTableModel.fireTableDataChanged();
 			}
 		});
 		
 		txtSearchBar = new JTextField("search");
 		txtSearchBar.setForeground(Color.GRAY);
 		txtSearchBar.addKeyListener(new KeyListener() {
 			@Override
 			public void keyPressed(KeyEvent arg0) {
 				//do nothing
 			}
 			@Override
 			public void keyReleased(KeyEvent arg0) {
 				//do nothing
 			}
 			@Override
 			public void keyTyped(KeyEvent arg0) {
 				btnSearch.setText("search");
 			}
 		});
 		txtSearchBar.addFocusListener(new FocusListener () {
 
 			@Override
 			public void focusGained(FocusEvent arg0) {
 				if(txtSearchBar.getText().equals("search"))
 				{
 					txtSearchBar.setForeground(Color.BLACK);
 					txtSearchBar.setText("");
 				}
 			}
 
 			@Override
 			public void focusLost(FocusEvent arg0) {
 				if(txtSearchBar.getText().isEmpty())
 				{
 					txtSearchBar.setForeground(Color.GRAY);
 					txtSearchBar.setText("search");
 				}
 			}
 			
 		});
 		
 		btnSearch = new JButton("search");
 		btnSearch.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				searchTerm = txtSearchBar.getText();
 				if (btnSearch.getText().equals("clear")) {
 					patientTableModel.fireTableDataChanged();
 					btnSearch.setText("search");
 					txtSearchBar.setForeground(Color.GRAY);
 					txtSearchBar.setText("search");
 				} else {
 					patientTableModel.fireTableDataChanged("SELECT * FROM patient WHERE name = '" + searchTerm + "'");
 					btnSearch.setText("clear");
 				}
 			}
 		});
 
 		final JRadioButton viewAllRadioButton = new JRadioButton("View All");
 
 		final JRadioButton inHospitalRadioButton = new JRadioButton("In Hospital");
 		inHospitalRadioButton.addActionListener( new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if(((JRadioButton)e.getSource()).isSelected())
 				{
					patientTableModel.fireTableDataChanged("SELECT * FROM patient WHERE (in_hospital = 'Y' OR in_hospital = 'L')");
 				} else {
 					patientTableModel.fireTableDataChanged();
 				}
 			}
 		});
 
 		viewAllRadioButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				inHospitalRadioButton.setSelected(!viewAllRadioButton.isSelected());
 			}
 		});
 		inHospitalRadioButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				viewAllRadioButton.setSelected(!inHospitalRadioButton.isSelected());
 			}
 		});
 		
 		JLabel lblWard = new JLabel("Ward");
 		
 		wardNumber = new JLabel("WardNumber");
 		
 		JLabel lblPhoneNumber = new JLabel("Phone Number");
 		
 		phoneNumber = new JLabel("PhoneNumber");
 		
 		JLabel lblEmail = new JLabel("Email");
 		
 		email = new JLabel("Email");
 		
 		JLabel lblAddress = new JLabel("Address");
 		
 		address = new JLabel("Address");
 		
 		JLabel lblRoom = new JLabel("Room");
 		
 		room = new JLabel("room");
 		
 		JLabel lblBed = new JLabel("Bed");
 		
 		bed = new JLabel("bed");
 
 		GroupLayout gl_panel = new GroupLayout(panel);
 		gl_panel.setHorizontalGroup(
 			gl_panel.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel.createSequentialGroup()
 					.addContainerGap()
 					.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING)
 						.addGroup(gl_panel.createSequentialGroup()
 							.addComponent(txtSearchBar, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
 							.addGap(6)
 							.addComponent(btnSearch, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
 							.addGap(6)
 							.addComponent(btnCreatePatient, GroupLayout.PREFERRED_SIZE, 102, GroupLayout.PREFERRED_SIZE)
 							.addGap(6)
 							.addComponent(btnEditPatient, GroupLayout.PREFERRED_SIZE, 103, GroupLayout.PREFERRED_SIZE)
 							.addGap(6)
 							.addComponent(btnDeletePatient, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
 							.addGap(6)
 							.addComponent(btnRefresh, GroupLayout.PREFERRED_SIZE, 103, GroupLayout.PREFERRED_SIZE)
 							.addGap(22))
 							
 						.addGroup(gl_panel.createSequentialGroup()
 							.addComponent(inHospitalRadioButton, GroupLayout.PREFERRED_SIZE, 87, GroupLayout.PREFERRED_SIZE)
 							.addGap(18)
 							.addComponent(viewAllRadioButton)
 							.addGap(29))
 						.addGroup(gl_panel.createSequentialGroup()
 							.addComponent(jsp, GroupLayout.PREFERRED_SIZE, 636, GroupLayout.PREFERRED_SIZE)
 							.addPreferredGap(ComponentPlacement.UNRELATED)
 							.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
 								.addGroup(gl_panel.createSequentialGroup()
 									.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
 										.addComponent(lblPhoneNumber, GroupLayout.PREFERRED_SIZE, 110, GroupLayout.PREFERRED_SIZE)
 										.addComponent(lblBed, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
 										.addComponent(lblRoom, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
 										.addComponent(lblWard, GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE))
 									.addPreferredGap(ComponentPlacement.RELATED)
 									.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
 										.addComponent(phoneNumber, GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE)
 										.addComponent(bed, GroupLayout.PREFERRED_SIZE, 128, GroupLayout.PREFERRED_SIZE)
 										.addComponent(room, GroupLayout.PREFERRED_SIZE, 128, GroupLayout.PREFERRED_SIZE)
 										.addComponent(wardNumber, GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE)))
 								.addGroup(gl_panel.createSequentialGroup()
 									.addComponent(lblEmail, GroupLayout.PREFERRED_SIZE, 110, GroupLayout.PREFERRED_SIZE)
 									.addPreferredGap(ComponentPlacement.RELATED)
 									.addComponent(email, GroupLayout.PREFERRED_SIZE, 126, GroupLayout.PREFERRED_SIZE))
 								.addGroup(gl_panel.createSequentialGroup()
 									.addComponent(lblAddress, GroupLayout.PREFERRED_SIZE, 110, GroupLayout.PREFERRED_SIZE)
 									.addPreferredGap(ComponentPlacement.RELATED)
 									.addComponent(address, GroupLayout.PREFERRED_SIZE, 126, GroupLayout.PREFERRED_SIZE)))
 							.addGap(540))))
 		);
 		gl_panel.setVerticalGroup(
 			gl_panel.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel.createSequentialGroup()
 					.addContainerGap()
 					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
 						.addComponent(viewAllRadioButton)
 						.addComponent(inHospitalRadioButton))
 					.addPreferredGap(ComponentPlacement.RELATED, 619, Short.MAX_VALUE)
 					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
 						.addComponent(txtSearchBar)
 						.addComponent(btnSearch)
 						.addComponent(btnCreatePatient)
 						.addComponent(btnEditPatient)
 						.addComponent(btnDeletePatient)
 						.addComponent(btnRefresh))
 					.addGap(8))
 				.addGroup(gl_panel.createSequentialGroup()
 					.addGap(32)
 					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
 						.addComponent(jsp, GroupLayout.PREFERRED_SIZE, 610, GroupLayout.PREFERRED_SIZE)
 						.addGroup(gl_panel.createSequentialGroup()
 							.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
 								.addGroup(gl_panel.createSequentialGroup()
 									.addComponent(lblPhoneNumber)
 									.addPreferredGap(ComponentPlacement.RELATED)
 									.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
 										.addComponent(lblEmail)
 										.addComponent(email))
 									.addPreferredGap(ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
 									.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
 										.addComponent(lblAddress)
 										.addComponent(address))
 									.addGap(38)
 									.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
 										.addComponent(wardNumber)
 										.addComponent(lblWard)))
 								.addGroup(gl_panel.createSequentialGroup()
 									.addComponent(phoneNumber)
 									.addGap(100)))
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
 								.addComponent(room)
 								.addComponent(lblRoom))
 							.addGap(8)
 							.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
 								.addComponent(bed)
 								.addComponent(lblBed))
 							.addGap(460)))
 					.addContainerGap(32, Short.MAX_VALUE))
 		);
 		panel.setLayout(gl_panel);
 
 		JPanel usersPanel = new JPanel();
 		patientsUsersNursesTabbedPane.addTab("Users", null, usersPanel, null);
 
 		JScrollPane usersScrollPane = new JScrollPane((Component) null);
 
 		JButton buttonRefreshUsers = new JButton("Refresh");
 
 		JButton btnDeleteUser = new JButton("Delete User");
 
 		JButton btnEditUser = new JButton("Edit User");
 		btnEditUser.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				userController.EditUser(frmMain);
 			}
 		});
 
 		JButton btnCreateUser = new JButton("Create User");
 		btnCreateUser.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				userController.CreateUser(frmMain);
 			}
 		});
 		
 		GroupLayout gl_usersPanel = new GroupLayout(usersPanel);
 		gl_usersPanel.setHorizontalGroup(
 			gl_usersPanel.createParallelGroup(Alignment.TRAILING)
 				.addGroup(gl_usersPanel.createSequentialGroup()
 					.addContainerGap(879, Short.MAX_VALUE)
 					.addComponent(btnCreateUser, GroupLayout.PREFERRED_SIZE, 102, GroupLayout.PREFERRED_SIZE)
 					.addGap(6)
 					.addComponent(btnEditUser, GroupLayout.PREFERRED_SIZE, 103, GroupLayout.PREFERRED_SIZE)
 					.addGap(6)
 					.addComponent(btnDeleteUser, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
 					.addGap(6)
 					.addComponent(buttonRefreshUsers, GroupLayout.PREFERRED_SIZE, 103, GroupLayout.PREFERRED_SIZE)
 					.addGap(27))
 				.addGroup(Alignment.LEADING, gl_usersPanel.createSequentialGroup()
 					.addContainerGap()
 					.addComponent(usersScrollPane, GroupLayout.PREFERRED_SIZE, 1303, GroupLayout.PREFERRED_SIZE)
 					.addContainerGap(37, Short.MAX_VALUE))
 		);
 		gl_usersPanel.setVerticalGroup(
 			gl_usersPanel.createParallelGroup(Alignment.LEADING)
 				.addGroup(Alignment.TRAILING, gl_usersPanel.createSequentialGroup()
 					.addContainerGap(14, Short.MAX_VALUE)
 					.addComponent(usersScrollPane, GroupLayout.PREFERRED_SIZE, 626, GroupLayout.PREFERRED_SIZE)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addGroup(gl_usersPanel.createParallelGroup(Alignment.LEADING)
 						.addComponent(btnCreateUser)
 						.addComponent(btnEditUser)
 						.addComponent(btnDeleteUser)
 						.addComponent(buttonRefreshUsers))
 					.addContainerGap())
 		);
 		usersPanel.setLayout(gl_usersPanel);
 
 		JPanel panel_2 = new JPanel();
 		patientsUsersNursesTabbedPane.addTab("Nurses", null, panel_2, null);
 		tableNurses = new JTable(nurseTableModel);
 		JScrollPane nursesScrollPane = new JScrollPane(tableNurses);
 
 		JButton btnCreateNurse = new JButton("Create Nurse");
 		btnCreateNurse.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				nurseController.CreateNurse();
 			}
 		});
 
 		JButton btnEditNurse = new JButton("Edit Nurse");
 		btnEditNurse.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				String[] strings = getSelectedNurse();
 				nurseController.EditNurse(strings);
 			}
 		});
 
 		JButton btnDeleteNurse = new JButton("Delete Nurse");
 		btnDeleteNurse.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				String[] strings = getSelectedNurse();
 				int idNumber = Integer.parseInt(strings[6]);
 				
 				nurseController.DeleteNurse(idNumber, nurseTableModel);
 			}
 		});
 
 		JButton buttonRefreshNurses = new JButton("Refresh");
 		buttonRefreshNurses.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				nurseTableModel.fireTableDataChanged();
 			}
 		});
 		GroupLayout gl_panel_2 = new GroupLayout(panel_2);
 		gl_panel_2.setHorizontalGroup(
 			gl_panel_2.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel_2.createSequentialGroup()
 					.addContainerGap()
 					.addComponent(nursesScrollPane, GroupLayout.PREFERRED_SIZE, 1304, GroupLayout.PREFERRED_SIZE)
 					.addContainerGap(28, Short.MAX_VALUE))
 				.addGroup(Alignment.TRAILING, gl_panel_2.createSequentialGroup()
 					.addContainerGap(879, Short.MAX_VALUE)
 					.addComponent(btnCreateNurse, GroupLayout.PREFERRED_SIZE, 102, GroupLayout.PREFERRED_SIZE)
 					.addGap(6)
 					.addComponent(btnEditNurse, GroupLayout.PREFERRED_SIZE, 103, GroupLayout.PREFERRED_SIZE)
 					.addGap(6)
 					.addComponent(btnDeleteNurse, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
 					.addGap(6)
 					.addComponent(buttonRefreshNurses, GroupLayout.PREFERRED_SIZE, 103, GroupLayout.PREFERRED_SIZE)
 					.addGap(27))
 		);
 		gl_panel_2.setVerticalGroup(
 			gl_panel_2.createParallelGroup(Alignment.LEADING)
 				.addGroup(Alignment.TRAILING, gl_panel_2.createSequentialGroup()
 					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 					.addComponent(nursesScrollPane, GroupLayout.PREFERRED_SIZE, 625, GroupLayout.PREFERRED_SIZE)
 					.addPreferredGap(ComponentPlacement.UNRELATED)
 					.addGroup(gl_panel_2.createParallelGroup(Alignment.LEADING)
 						.addComponent(btnCreateNurse)
 						.addComponent(btnEditNurse)
 						.addComponent(btnDeleteNurse)
 						.addComponent(buttonRefreshNurses))
 					.addGap(21))
 		);
 		panel_2.setLayout(gl_panel_2);
 		frmMain.getContentPane().setLayout(groupLayout);
 
 		tablePatients.addMouseListener(new MouseAdapter() {
 			public void mouseClicked(MouseEvent e) {
 				
 				if(tablePatients.getSelectedRowCount() == 1){
 					Object[][] content = patientTableModel.getContent();
 					Object[] selectedRow = content[tablePatients.getSelectedRow()];
 					String[] strings = new String[content[tablePatients.getSelectedRow()].length];
 					for(int i = 0; i< strings.length; i++){
 						strings[i] = content[tablePatients.getSelectedRow()][i].toString();
 					}
 					
 					int healthCareNumber = Integer.parseInt(strings[0]);
 					Patient patient = null;
 					
 					try {
 						patient = patientManager.GetPatient(healthCareNumber);
 					}
 					catch (SQLException ex) {}
 					String roomId = Integer.toString(patient.room_id);
 					String bedId = Integer.toString(patient.bed_id);
 					
 					address.setText(patient.address);
 					phoneNumber.setText(patient.phone_number);
 					email.setText(patient.email);
 					wardNumber.setText(patientManager.getPatientSingleWardName(patient.ward_id));
 					room.setText(roomId);
 					bed.setText(bedId);
 				}
 			}
 		});
 	}
 }
