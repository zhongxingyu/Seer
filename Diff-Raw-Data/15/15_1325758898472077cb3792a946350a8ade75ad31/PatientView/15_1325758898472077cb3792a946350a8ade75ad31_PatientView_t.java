 package com.medicalmanager.views;
 
 import java.awt.CardLayout;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.print.PageFormat;
 import java.awt.print.Paper;
 import java.awt.print.PrinterException;
 import java.awt.print.PrinterJob;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.print.PrintService;
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
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTextArea;
 import javax.swing.JToolBar;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.SwingConstants;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import com.medicalmanager.controllers.Database;
 import com.medicalmanager.controllers.Print;
 import com.medicalmanager.controllers.Test;
 import com.medicalmanager.models.Patient;
 
 public class PatientView extends JFrame {
 
 	private CardLayout card = new CardLayout(0, 0);
 
 	private static JPanel contentPane;
 	private JPanel patientInfoEditPanel;
 	
 	private JMenu fileMenu;
 	private JMenu helpMenu;
 	
 	private JMenuItem helpMenuPreferencesItem;
 	private JMenuItem helpMenuTutorialItem;
 	private JMenuItem helpMenuFAQItem;
 	private JMenuItem fileMenuPrintItem;
 	private JMenuItem fileMenuSaveAsItem;
 	private JMenuItem fileMenuOpenItem;
 	
 	private JButton mainAppButton;
 	private JButton aboutButton;
 	private JButton newPatientButton;
 	private JButton searchButton;
 	private JButton sortPatientList;
 	private JButton editPatientButton;
 	private JButton adjustSeverityButton;
 	private JButton prescribeMedicationButton;
 	private JButton setDiagnosisButton;
 
 	private static JList patientList;
 	public static DefaultListModel<String> listModel;
 	private JScrollPane infoScollPane;
 	
 	private JTextArea patientInfoArea;
 	
 	private static Patient selected;
 	
 	private static boolean isSelected = false;
 
 	public static ArrayList<Patient> sortedArray = new ArrayList<Patient>();
 	public static ArrayList<Patient> patientArray = new ArrayList<Patient>();
 	private JPanel aboutPanel;
 	private JToolBar patientToolBar;
 	private JMenuItem helpMenuMainMenuItem;
 
 
 	/**
 	 * Bootstrap the entire GUI
 	 * 
 	 * @throws IOException
 	 */
 	public PatientView() throws IOException {
 		setTitle("Medical Manager");
 		// Ensures the panel closes when you press the close button
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		// Opening size
 		setBounds(100, 100, 838, 609);
 		
 		// Base panel of the application
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		contentPane.setLayout(card); // Card layout to access the other components such as Patient or About
 
 		setContentPane(contentPane);
 		
 		// Place the menu on the content pane
 		placeMenu();
 		
 		// Instantiate the welcome panel
 		makeWelcomePanel();
 		
 		// Instantiate the patient panel
 		makePatientPanel();
 		
 		// Bootstrap the event handlers
 		actionTime();
 		
 		// Set the standard write directory - possibly add settings to change where this is
 		Database.setWriteDirectory(System.getProperty("user.home") + "\\My Documents\\Medical Manager\\");
 		
 		// Set the standard file that is written to
 		Database.setFile("patients.txt");
 		
 		// Create the directory and file if it isn't already there
 		Database.prepareFile();
 		
		new Test();
 		
 		// Read all the patients from the file dumping them into an array list for use later
 		Database.readAllPatientsFromFile();
		System.out.println(Database.linSearch(patientArray, 0).getFullName());
 	}
 
 	public void placeMenu() {
 		JMenuBar menuBar = new JMenuBar();
 		setJMenuBar(menuBar);
 
 		fileMenu = new JMenu("File");
 		menuBar.add(fileMenu);
 
 		fileMenuSaveAsItem = new JMenuItem("Save As");
 		fileMenu.add(fileMenuSaveAsItem);
 
 		fileMenuOpenItem = new JMenuItem("Open File");
 		fileMenu.add(fileMenuOpenItem);
 
 		fileMenuPrintItem = new JMenuItem("Print");
 		fileMenu.add(fileMenuPrintItem);
 
 		helpMenu = new JMenu("Help");
 		menuBar.add(helpMenu);
 
 		helpMenuTutorialItem = new JMenuItem("Tutorial");
 		helpMenu.add(helpMenuTutorialItem);
 
 		helpMenuFAQItem = new JMenuItem("FAQ");
 		helpMenu.add(helpMenuFAQItem);
 		
 		helpMenuMainMenuItem = new JMenuItem("Main Menu");
 		helpMenu.add(helpMenuMainMenuItem);
 
 		helpMenuPreferencesItem = new JMenuItem("Preferences");
 		helpMenu.add(helpMenuPreferencesItem);
 	}
 
 	public void makePatientPanel() throws IOException {
 		JPanel patientPanel = new JPanel();
 		contentPane.add(patientPanel, "patientPanel");
 
 		JSplitPane splitPane = new JSplitPane();
 
 		patientToolBar = new JToolBar();
 		GroupLayout patientLayout = new GroupLayout(patientPanel);
 		patientLayout.setHorizontalGroup(patientLayout
 				.createParallelGroup(Alignment.LEADING)
 				.addComponent(splitPane, Alignment.TRAILING,
 						GroupLayout.DEFAULT_SIZE, 812, Short.MAX_VALUE)
 				.addComponent(patientToolBar, GroupLayout.DEFAULT_SIZE, 812,
 						Short.MAX_VALUE));
 		patientLayout.setVerticalGroup(patientLayout.createParallelGroup(
 				Alignment.TRAILING).addGroup(
 				Alignment.LEADING,
 				patientLayout
 						.createSequentialGroup()
 						.addComponent(patientToolBar,
 								GroupLayout.PREFERRED_SIZE, 21,
 								GroupLayout.PREFERRED_SIZE)
 						.addPreferredGap(ComponentPlacement.RELATED)
 						.addComponent(splitPane, GroupLayout.DEFAULT_SIZE, 513,
 								Short.MAX_VALUE)));
 
 		newPatientButton = new JButton("New Patient");
 		searchButton = new JButton("Search");
 		sortPatientList = new JButton("Sort List");
 
 		patientToolBar.add(searchButton);
 		patientToolBar.add(sortPatientList);
 		patientToolBar.add(newPatientButton);
 
 		editPatientButton = new JButton("Edit Patient");
 		adjustSeverityButton = new JButton("Adjust Severity");
 		prescribeMedicationButton = new JButton("Prescribe Medication");
 		setDiagnosisButton = new JButton("Set Diagnosis");
 
 		JScrollPane scrollPane = new JScrollPane();
 		scrollPane.setMinimumSize(new Dimension(100, 23));
 		splitPane.setLeftComponent(scrollPane);
 
 		listModel = new DefaultListModel<String>();
 		patientList = new JList<String>(listModel);
 		patientList.setSize(new Dimension(200, 0));
 		patientList.setMinimumSize(new Dimension(200, 0));
 		scrollPane.setViewportView(patientList);
 		
 		patientInfoEditPanel = new JPanel();
 		splitPane.setRightComponent(patientInfoEditPanel);
 		patientInfoEditPanel.setLayout(new CardLayout());
 		
 		infoScollPane = new JScrollPane();
 		patientInfoEditPanel.add(infoScollPane, "infoPane");
 		
 		patientInfoArea = new JTextArea();
 		patientInfoArea.setFont(new Font("Monospaced", patientInfoArea.getFont().getStyle(), patientInfoArea.getFont().getSize()));
 		patientInfoArea.setLineWrap(true);
 		patientInfoArea.setWrapStyleWord(true);
 		infoScollPane.setViewportView(patientInfoArea);
 		
 		patientPanel.setLayout(patientLayout);
 		
 		aboutPanel = new JPanel();
 		contentPane.add(aboutPanel, "aboutPanel");
 		aboutPanel.setLayout(new CardLayout(0, 0));
 
 		for (Patient p : patientArray) {
 			listModel.addElement(p.getFirstName());
 		}
 	}
 
 	public void makeWelcomePanel() {
 		JPanel welcomePanel = new JPanel();
 		contentPane.add(welcomePanel, "welcomePanel");
 		card.show(contentPane, "welcomePanel");
 
 		JLabel welcomeLabel = new JLabel("Welcome to Medical Manager!");
 		welcomeLabel.setFont(new Font("Tahoma", Font.PLAIN, 36));
 		welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
 
 		mainAppButton = new JButton("Manage Patients");
 		aboutButton = new JButton("About Medical Manager");
 
 		JLabel createdByLabel = new JLabel("Created By: Ben Vest");
 		createdByLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		GroupLayout welcomeLayout = new GroupLayout(welcomePanel);
 		welcomeLayout
 				.setHorizontalGroup(welcomeLayout
 						.createParallelGroup(Alignment.LEADING)
 						.addGroup(
 								welcomeLayout
 										.createSequentialGroup()
 										.addContainerGap()
 										.addComponent(createdByLabel,
 												GroupLayout.DEFAULT_SIZE, 792,
 												Short.MAX_VALUE)
 										.addContainerGap())
 						.addGroup(
 								welcomeLayout
 										.createSequentialGroup()
 										.addContainerGap()
 										.addComponent(welcomeLabel,
 												GroupLayout.DEFAULT_SIZE, 792,
 												Short.MAX_VALUE)
 										.addContainerGap())
 						.addGroup(
 								Alignment.TRAILING,
 								welcomeLayout
 										.createSequentialGroup()
 										.addGap(62)
 										.addGroup(
 												welcomeLayout
 														.createParallelGroup(
 																Alignment.TRAILING)
 														.addComponent(
 																aboutButton,
 																Alignment.LEADING,
 																GroupLayout.DEFAULT_SIZE,
 																662,
 																Short.MAX_VALUE)
 														.addComponent(
 																mainAppButton,
 																Alignment.LEADING,
 																GroupLayout.DEFAULT_SIZE,
 																662,
 																Short.MAX_VALUE))
 										.addGap(88)));
 		welcomeLayout.setVerticalGroup(welcomeLayout.createParallelGroup(
 				Alignment.LEADING).addGroup(
 				welcomeLayout
 						.createSequentialGroup()
 						.addContainerGap()
 						.addComponent(welcomeLabel, GroupLayout.PREFERRED_SIZE,
 								53, GroupLayout.PREFERRED_SIZE)
 						.addGap(40)
 						.addComponent(mainAppButton,
 								GroupLayout.PREFERRED_SIZE, 116,
 								GroupLayout.PREFERRED_SIZE)
 						.addGap(18)
 						.addComponent(aboutButton, GroupLayout.PREFERRED_SIZE,
 								119, GroupLayout.PREFERRED_SIZE)
 						.addGap(146)
 						.addComponent(createdByLabel,
 								GroupLayout.PREFERRED_SIZE, 26,
 								GroupLayout.PREFERRED_SIZE).addContainerGap()));
 		welcomePanel.setLayout(welcomeLayout);
 	}
 
 	public void actionTime() {
 		mainAppButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				card.next(contentPane); 
 			}
 		});
 		
 		aboutButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				card.show(contentPane, "aboutPanel");
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
 
 		sortPatientList.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				SelectSortMethod ssm = new SelectSortMethod();
 				ssm.setVisible(true);
 			}
 		});
 		
 		fileMenuSaveAsItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				SavePatientDialog dialog = new SavePatientDialog();
 				dialog.setVisible(true);
 			}
 		});
 		
 		
 		fileMenuPrintItem.addActionListener(new ActionListener() {
 			 public void actionPerformed(ActionEvent e) {
 				 boolean isTrue = true;
 				 if(isTrue) {
 				        PrinterJob printJob = PrinterJob.getPrinterJob(); 
 				        PrintService printer = printJob.getPrintService();
 				        PageFormat pf = printJob.defaultPage();
 				        Paper paper = new Paper();
 				        
 				        double margin = 36; // half inch
 				        paper.setImageableArea(margin, margin, 
 				        		paper.getWidth() - margin * 2, 
 				        		paper.getHeight() - margin * 2);
 				        pf.setPaper(paper);
 	
 				        if(printer == null) {
 				          JOptionPane.showMessageDialog(contentPane, 
 				                                        "No default printer available.",
 				                                        "Printer Error",
 				                                        JOptionPane.ERROR_MESSAGE);
 				          return;
 				        }
 				        
 				     
 				        printJob.setPrintable(new Print(false), pf);
 				        
 				        if(printJob.printDialog()) {                      
 				          try {
 				            printJob.print();                           
 				          } catch(PrinterException pe) {
 				            System.out.println(pe);
 				            JOptionPane.showMessageDialog(contentPane,
 				                                          "Error printing",
 				                                          "Printer Error",
 				                                          JOptionPane.ERROR_MESSAGE);
 				          }
 				        }
 				 }
 			 }
 		 });
 		
 		helpMenuMainMenuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				card.show(contentPane, "welcomePanel"); 
 			}
 		});
 		
 		editPatientButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if(PatientView.isSelected){
 					EditPatientDialog dialog = new EditPatientDialog();
 					dialog.setVisible(true);	
 				} else {
 					 JOptionPane.showMessageDialog(contentPane,
 	                          "No Patient Selected!",
 	                          "Edit Error",
 	                          JOptionPane.ERROR_MESSAGE);
 				}
 			}
 		});
 		
 		adjustSeverityButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if(PatientView.isSelected){
 					
 					
 				} else {
 					 JOptionPane.showMessageDialog(contentPane,
 	                          "No Patient Selected!",
 	                          "Edit Error",
 	                          JOptionPane.ERROR_MESSAGE);
 				}
 			}
 		});
 		
 		prescribeMedicationButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 			}
 		});
 		
 		setDiagnosisButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 			}
 		});
 		
 		patientList.addListSelectionListener(new ListSelectionListener() {
 			public void valueChanged(ListSelectionEvent arg0) {
 				PatientView.isSelected = true;
 				boolean isNew = true;
 				if(!patientList.isSelectionEmpty()){
 					if(isNew){
 						patientToolBar.add(editPatientButton);
 						// commenting these out unless i have time to do it
 //						patientToolBar.add(adjustSeverityButton);
 //						patientToolBar.add(prescribeMedicationButton);
 //						patientToolBar.add(setDiagnosisButton);
 						isNew = false;
 					}
 					
 					Patient p = patientArray.get(patientList.getSelectedIndex());
 					PatientView.setSelected(p);
 					
 					String newLine = "\n";
 					String space = "  ";
 					
 					patientInfoArea.setText(
 							space+ "----------------------------------- Patient Info ---------------------------------------\n"
 							+ space + "** Information regarding " + p.getFullName() + " is listed below.  \n"
 							+ space + "** This Patient is under the care of Doctor Blah \n"
 							+ space + "** Remember to leave detailed remarks on any edits to the patients file \n"
 							+ space + "** If you encounter any difficulty consult the Help Menu \n"
 							+ space + "** If immediate attention by others is required raise the patients severity level \n"
 							+ newLine
 							+ formatField("Age", p.getAge())
 							+ newLine
 							+ formatField("Name", p.getFirstName())
 							+ newLine
 							+ formatField("Height", p.getHeight())
 							+ newLine
 							+ formatField("Weight", p.getWeight())
 							+ newLine
 							+ formatField("Date of Birth", p.getDOB())
 							+ newLine
 							+ formatField("Body Mass Index", p.getBMI())
 							+ newLine
 							+ formatField("Address", p.getAddress())
 							+ newLine
 							+ formatField("Home Phone", p.getHomePhoneNumber())
 							+ newLine
 							+ formatField("Cell Phone", p.getCellPhoneNumber())
 							+ newLine
 							+ formatField("Current Medications", p.getCurrentMedications())
 							+ newLine
 							+ formatField("Additional Medical Information", p.getAdditionalMedicalInformation()));
 				}
 			}
 				
 		});
 	}
 	
 	/**
 	 * Update's the PatientList by adding the patient to the listModel
 	 * 
 	 * @param p Patient being used to update the list, will be added to the bottom
 	 */
 	public static void updateList(Patient p) {
 		patientArray.add(p);
 		listModel.addElement(p.getFirstName());
 	}
 	
 	/**
 	 * Adds the elements of the patient array 
 	 * 
 	 * to the top of the array list
 	 * @param pRay Patient array that will be sent to the top of the patient list
 	 */
 	public static void toTop(Patient[] pRay){
 		try{
 			int index = 0;
 			for(Patient p: pRay){
 				System.out.println(p.getFirstName());
 				
 				int remIndex = patientArray.indexOf(p);
 				listModel.remove(remIndex);
 				listModel.add(index, p.getFirstName());
 				index++;
 			}
 		}catch(NullPointerException e){
 			System.out.println(e);
 		}
 	}
 	
 	/**
 	 * Updates the list to reflect name change after a patient is edited
 	 * 
 	 * @param index Original index on the patientList 
 	 * @param p Patient that is being re-added to the list after being edited
 	 */
 	public static void updateListAfterPatientEdit(int index, Patient p){
 		patientArray.remove(p);
 		patientArray.add(index, p);
 		
 		patientList.clearSelection();
 		patientList.setSelectedIndex(index);
 		
 		listModel.remove(index);
 		listModel.add(index, p.getFirstName());
 	}
 	
 	/**
 	 * Moves a Patient around in the list
 	 * 
 	 * @param p The patient being moved
 	 * @param prior How high a patients priority is, it's relative so
 	 * 				1 is really low 10 is really high
 	 */
 	public static void changePriority(Patient p, int prior){
 		patientArray.remove(p);
 		listModel.add(prior, p.getFirstName());
 	}
 	
 	/**
 	 * Helper method that reorders the list based on a 
 	 * passed in ArrayList.
 	 * 
 	 * @param newOrder ArrayList that the new order will follow
 	 */
 	public static void sortList(ArrayList<Patient> newOrder){
 		listModel.clear();
 		PatientView.setSortedArray(newOrder);
 		for (Patient p : newOrder) {
 			listModel.addElement(p.getFirstName());
 		}
 	}
 	
 	/**
 	 * Formats things properly for being used in the patientInfoArea.
 	 * Aligns everything making it more readable.
 	 * 
 	 * @param attr The attribute of a patient
 	 * @param val The corresponding value of a patients attribute
 	 * @return A single formatted string that looks like 'blah: =>  dah'
 	 */
 	public static String formatField(String attr, Comparable<?> val){
 		return String.format("  %30s: => %-4s", attr, val) + "\n";
 	}
 	
 	/**
 	 * Assigns the PatientViews sorted array for use in other methods
 	 * 
 	 * @param sorted An ArrayList of Patients that is sorted
 	 */
 	public static void setSortedArray(ArrayList<Patient> sorted){
 		PatientView.sortedArray = sorted;
 	}
 	
 	/**
 	 * 
 	 * 
 	 * @return
 	 */
 	public static Patient getSelected() {
 		return selected;
 	}
 
 	/**
 	 * 
 	 * @param selected
 	 */
 	public static void setSelected(Patient selected) {
 		PatientView.selected = selected;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public static ArrayList<Patient> getPatientArray(){
 		return PatientView.patientArray;
 	}
 	
 	/**
 	 * 
 	 * @param msg
 	 * @param title
 	 */
 	public static void showError(String msg, String title){
 		 JOptionPane.showMessageDialog(PatientView.contentPane, msg, title, JOptionPane.ERROR_MESSAGE);
 	}
 }
