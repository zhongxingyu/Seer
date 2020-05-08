 package com.medicalmanager.views;
 
 import java.awt.CardLayout;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.swing.DefaultListModel;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTextArea;
 import javax.swing.JToolBar;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.SwingConstants;
 import javax.swing.border.EmptyBorder;
 
 import com.medicalmanager.helpers.DataHelper;
 import com.medicalmanager.models.Patient;
 
 public class PatientView extends JFrame {
 
 	private CardLayout card = new CardLayout(0,0);
 	
 	private JPanel contentPane;
 	
 	private JButton mainAppButton;
 	private JButton aboutButton;
 	private JButton newPatientButton;
 	private JButton searchButton;
 	private JButton sortByIDButton;
 	
 	public static JTextArea patientInfoArea;
 	public static DefaultListModel listModel;
 	public static ArrayList<Patient> sortedArray = new ArrayList<Patient>();
 	public static ArrayList<Patient> patientArray = new ArrayList<Patient>();
 	private JList patientList;
 
 	/**
 	 * Create the frame.
 	 * @throws IOException 
 	 */
 	public PatientView() throws IOException {
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		setBounds(100, 100, 838, 609);
 		
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		contentPane.setLayout(card);
 		
 		setContentPane(contentPane);
 	
 		placeMenu();
 		makeWelcomePanel();
 		makePatientPanel();
 		actionTime();	
 		DataHelper.prepareFile();
 		DataHelper.readAllPatients();
 	
 	}
 
 	public void placeMenu(){
 		JMenuBar menuBar = new JMenuBar();
 		setJMenuBar(menuBar);
 		
 		JMenu fileMenu = new JMenu("File");
 		menuBar.add(fileMenu);
 		
 		JMenuItem mntmNewMenuItem = new JMenuItem("Save Patients");
 		mntmNewMenuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				// startup dat savepatientdialog
 			}
 		});
 		fileMenu.add(mntmNewMenuItem);
 		
 		JMenu helpMenu = new JMenu("Patient");
 		menuBar.add(helpMenu);
 		
 		JMenuItem menuItem = new JMenuItem("New menu item");
 		helpMenu.add(menuItem);
 	}
 	
 	public void makePatientPanel() throws IOException{
 		JPanel patientPanel = new JPanel();
 		contentPane.add(patientPanel, "patientPanel");
 
 		JSplitPane splitPane = new JSplitPane();
 		
 		JToolBar patientToolBar = new JToolBar();
 		GroupLayout patientLayout = new GroupLayout(patientPanel);
 		patientLayout.setHorizontalGroup(
 			patientLayout.createParallelGroup(Alignment.LEADING)
 				.addComponent(splitPane, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 812, Short.MAX_VALUE)
 				.addComponent(patientToolBar, GroupLayout.DEFAULT_SIZE, 812, Short.MAX_VALUE)
 		);
 		patientLayout.setVerticalGroup(
 			patientLayout.createParallelGroup(Alignment.TRAILING)
 				.addGroup(Alignment.LEADING, patientLayout.createSequentialGroup()
 					.addComponent(patientToolBar, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(splitPane, GroupLayout.DEFAULT_SIZE, 513, Short.MAX_VALUE))
 		);
 		
 		newPatientButton = new JButton("New Patient");
 		searchButton = new JButton("Search");
 		sortByIDButton = new JButton("Sort By ID");
 		
 		patientToolBar.add(searchButton);
 		patientToolBar.add(sortByIDButton);
 		patientToolBar.add(newPatientButton);
 		
 		patientInfoArea = new JTextArea();
 		JScrollPane infoScrollPane = new JScrollPane();
 		splitPane.setRightComponent(infoScrollPane);
 		infoScrollPane.setViewportView(patientInfoArea);
 		
 		
 		JScrollPane scrollPane = new JScrollPane();
 		scrollPane.setMinimumSize(new Dimension(100, 23));
 		splitPane.setLeftComponent(scrollPane);
 		
 		listModel = new DefaultListModel<Object>();
 		patientList = new JList(listModel);
 		patientList.setSize(new Dimension(200, 0));
 		patientList.setMinimumSize(new Dimension(200, 0));
 		scrollPane.setViewportView(patientList);
 		patientPanel.setLayout(patientLayout);
 		
 		for(Patient p: patientArray){
 			listModel.addElement(p.getName());
 		}	
 	}
 
 	public static void updateList(Patient p){
 		int theSize = patientArray.size();
 		patientArray.add(p);
 		listModel.addElement(p.getName());
 	}
 		
 	public void makeWelcomePanel(){
 		JPanel welcomePanel = new JPanel();
 		contentPane.add(welcomePanel, "welcomePanel");
 		card.show(contentPane, "welcomePanel");
 		
 		JLabel welcomeLabel = new JLabel("Welcome to Medical Manager!");
 		welcomeLabel.setFont(new Font("Tahoma", Font.PLAIN, 36));
 		welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		
 		mainAppButton = new JButton("Go To The Patient Area");
 		
 		aboutButton = new JButton("Go To The About Area");
 		
 		JLabel createdByLabel = new JLabel("Created By: Ben Vest");
 		createdByLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		
 		JButton btnNewButton = new JButton("Go To The User Area");
 		GroupLayout welcomeLayout = new GroupLayout(welcomePanel);
 		welcomeLayout.setHorizontalGroup(
 			welcomeLayout.createParallelGroup(Alignment.TRAILING)
 				.addGroup(Alignment.LEADING, welcomeLayout.createSequentialGroup()
 					.addGap(140)
 					.addComponent(welcomeLabel, GroupLayout.PREFERRED_SIZE, 580, GroupLayout.PREFERRED_SIZE)
 					.addContainerGap(92, Short.MAX_VALUE))
 				.addGroup(welcomeLayout.createSequentialGroup()
 					.addGap(250)
 					.addGroup(welcomeLayout.createParallelGroup(Alignment.TRAILING)
 						.addComponent(btnNewButton, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE)
 						.addComponent(aboutButton, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE)
 						.addComponent(mainAppButton, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE))
 					.addGap(234))
 				.addGroup(welcomeLayout.createSequentialGroup()
 					.addContainerGap(337, Short.MAX_VALUE)
 					.addComponent(createdByLabel, GroupLayout.PREFERRED_SIZE, 145, GroupLayout.PREFERRED_SIZE)
 					.addGap(330))
 		);
 		welcomeLayout.setVerticalGroup(
 			welcomeLayout.createParallelGroup(Alignment.LEADING)
 				.addGroup(welcomeLayout.createSequentialGroup()
 					.addContainerGap()
 					.addComponent(welcomeLabel, GroupLayout.PREFERRED_SIZE, 53, GroupLayout.PREFERRED_SIZE)
 					.addPreferredGap(ComponentPlacement.UNRELATED)
 					.addComponent(mainAppButton, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
 					.addGap(18)
 					.addComponent(aboutButton, GroupLayout.PREFERRED_SIZE, 62, GroupLayout.PREFERRED_SIZE)
 					.addGap(18)
 					.addComponent(btnNewButton, GroupLayout.PREFERRED_SIZE, 68, GroupLayout.PREFERRED_SIZE)
 					.addGap(202)
 					.addComponent(createdByLabel, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE)
 					.addContainerGap())
 		);
 		welcomePanel.setLayout(welcomeLayout);
 	}
 	
 	public void actionTime(){
 		mainAppButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				card.next(contentPane);
 			}
 		});
 		
 		newPatientButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				PatientDialog newUsar = new PatientDialog();
 				newUsar.setVisible(true);
 			}
 		});
 		
 		searchButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				SearchDialog dialog = new SearchDialog();
 				dialog.setVisible(true);
 			}
 		});
 		
 		sortByIDButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {		
 				DataHelper.sortPatients(patientArray);
 				listModel.clear();
 				for(Patient p: patientArray){
 					listModel.addElement(p.getName());
 				}
 			}
 		});
 		
<<<<<<< HEAD
=======
		printOutOptionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				OutputController.displayAllPatients(patientArray);	
			}
		});
 		
>>>>>>> 2565d6fdf926962808946af4e4b737490382ad3f
 		patientList.addListSelectionListener(new ListSelectionListener() {
 			public void valueChanged(ListSelectionEvent arg0) {
 				Patient rawr = patientArray.get(patientList.getSelectedIndex());
 				patientInfoArea.setText("AGE: " + rawr.getAge() + "\n" 
 										 + "Name: " + rawr.getName() + "\n"  
 										 + "HEIGHT: " + rawr.getHeight() + "\n" 
 										 + "DOB: " + rawr.getDOB() + "\n"
 										 + "BMI: " + rawr.getCalculatedBMI());
 			}
 		});
 	}
 }
 
 //sortedArray = DataHelper.sortPatients(patientArray);
 //int theSize = sortedArray.size();
 //
 //listModel.clear();
 //System.out.println(listModel);
 //
 //for(int i = 0; i < theSize; i++){
 //	listModel.addElement(sortedArray.get(i).getName());
 //}
 //
 //sortedArray.clear();
 //}
