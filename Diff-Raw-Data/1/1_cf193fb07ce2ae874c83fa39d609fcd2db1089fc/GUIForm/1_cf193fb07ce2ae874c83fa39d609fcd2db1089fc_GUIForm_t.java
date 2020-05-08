 package be.artesis.timelog.gui;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.HeadlessException;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Vector;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.DefaultListModel;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.JTree;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.SwingConstants;
 import javax.swing.border.BevelBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreeNode;
 
 import net.fortuna.ical4j.model.ValidationException;
 
 import org.json.JSONException;
 
 import be.artesis.timelog.checkboxtree.CheckBoxNode;
 import be.artesis.timelog.checkboxtree.CheckBoxNodeEditor;
 import be.artesis.timelog.checkboxtree.CheckBoxNodeRenderer;
 import be.artesis.timelog.checkboxtree.NamedVector;
 import be.artesis.timelog.clock.Clock;
 import be.artesis.timelog.ics.IcsExporteren;
 import be.artesis.timelog.model.Validator;
 import be.artesis.timelog.model.WebserviceException;
 import be.artesis.timelog.view.DataInputException;
 import be.artesis.timelog.view.Opdrachtgever;
 import be.artesis.timelog.view.Project;
 import be.artesis.timelog.view.Taak;
 import be.artesis.timelog.view.Tijdspanne;
 
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 import com.toedter.calendar.JDateChooser;
 import javax.swing.JComboBox;
 import javax.swing.SwingConstants;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.AbstractListModel;
 import java.awt.SystemColor;
 import javax.swing.UIManager;
 
 /**
  * @author Gilliam
  */
 @SuppressWarnings({ "unchecked", "rawtypes", "serial" })
 public class GUIForm extends javax.swing.JFrame {
 
 	// ================================================================================
 	// Properties
 	// ================================================================================
 
 	LoginForm login;
 	Validator validator;
 	boolean creatingProject;
 	final String NEWCLIENTITEM = "< New client >";
 	final String NEWTASKITEM = "< New task >";
 	final String NEWPROJECTITEM = "< New project >";
 
 	public GUIForm(Validator validator) {
 		this.validator = validator;
 		initComponents();
 	}
 
 	/* Begin gegenereerde code */
 	// Initialiseer GUI componenten
 	private void initComponents() {
 		creatingProject = false;
 		contentJTabbedPane = new javax.swing.JTabbedPane();
 		homeJPanel = new javax.swing.JPanel();
 		homeJLabel = new javax.swing.JLabel();
 		homeJLabel.setBounds(10, 11, 62, 16);
 		workJButton = new javax.swing.JButton();
 		workJButton.setBounds(10, 33, 664, 42);
 		projectsJPanel = new javax.swing.JPanel();
 		projectsJLabel = new javax.swing.JLabel();
 		projectsJLabel.setBounds(10, 11, 204, 16);
 		jScrollPane1 = new javax.swing.JScrollPane();
 		jScrollPane1.setBounds(10, 33, 204, 336);
 		setCurrentProjectJButton = new javax.swing.JButton();
 		setCurrentProjectJButton.setBounds(10, 404, 204, 36);
 		filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
 		filler2.setBounds(220, 152, 0, 0);
 		removeProjectJButton = new javax.swing.JButton();
 		removeProjectJButton.setBounds(10, 375, 204, 23);
 		tasksJPanel = new javax.swing.JPanel();
 		tasksJLabel = new javax.swing.JLabel();
 		tasksJLabel.setBounds(10, 14, 204, 16);
 		jScrollPane3 = new javax.swing.JScrollPane();
 		jScrollPane3.setBounds(10, 40, 204, 366);
 		removeTaskJButton = new javax.swing.JButton();
 		removeTaskJButton.setBounds(10, 417, 204, 23);
 		scheduleJPanel = new javax.swing.JPanel();
 		scheduleJLabel = new javax.swing.JLabel();
 		scheduleJLabel.setBounds(10, 11, 66, 19);
 		optionsJPanel = new javax.swing.JPanel();
 		settingsJLabel = new javax.swing.JLabel();
 		headerJPanel = new javax.swing.JPanel();
 		titleLabel = new javax.swing.JLabel();
 		ingelogdJLabel = new javax.swing.JLabel();
 		currentProjectJLabel = new javax.swing.JLabel();
 		clockJLabel = new javax.swing.JLabel();
 
 		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
 		setTitle("Time Management System");
 		setBackground(new java.awt.Color(51, 51, 51));
 		this.getContentPane().setBackground(Color.darkGray);
 		setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
 		setForeground(new java.awt.Color(51, 51, 51));
 		setIconImages(null);
 		setMaximumSize(new java.awt.Dimension(2000, 2000));
 		setName("guiFrame");
 		setResizable(false);
 		addWindowListener(new java.awt.event.WindowAdapter() {
 			public void windowOpened(java.awt.event.WindowEvent evt) {
 				guiOpened(evt);
 			}
 		});
 
 		contentJTabbedPane.setBackground(new java.awt.Color(51, 51, 51));
 		contentJTabbedPane.setTabPlacement(javax.swing.JTabbedPane.LEFT);
 		contentJTabbedPane.setName("tabContainer");
 
 		homeJPanel.setBackground(Color.GRAY);
 		homeJPanel.setForeground(new java.awt.Color(65, 152, 134));
 
 		homeJLabel.setFont(new java.awt.Font("Tw Cen MT", 1, 14));
 		homeJLabel.setForeground(new java.awt.Color(255, 255, 255));
 		homeJLabel.setText("Home");
 
 		workJButton.setBackground(new java.awt.Color(14, 196, 188));
 		workJButton.setFont(new java.awt.Font("Tahoma", 1, 18));
 		workJButton.setForeground(new java.awt.Color(204, 204, 204));
 		workJButton.setText("Work");
 		workJButton.addMouseListener(new java.awt.event.MouseAdapter() {
 			public void mouseClicked(java.awt.event.MouseEvent evt) {
 				workClicked(evt);
 			}
 		});
 
 		contentJTabbedPane.addTab("", new javax.swing.ImageIcon(getClass().getResource("/be/artesis/timelog/gui/icons/HomeNeonIcon.png")), homeJPanel, "Home"); // NOI18N
 		homeJPanel.setLayout(null);
 		homeJPanel.add(workJButton);
 		homeJPanel.add(homeJLabel);
 
 		homeFieldsJPanel = new JPanel();
 		homeFieldsJPanel.setBackground(Color.DARK_GRAY);
 		homeFieldsJPanel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
 		homeFieldsJPanel.setBounds(10, 86, 352, 354);
 		homeJPanel.add(homeFieldsJPanel);
 		homeFieldsJPanel.setLayout(null);
 
 		homeProjectsJLabel = new JLabel();
 		homeProjectsJLabel.setText("Projects");
 		homeProjectsJLabel.setForeground(new Color(0, 153, 153));
 		homeProjectsJLabel.setFont(new Font("Tw Cen MT", Font.BOLD, 14));
 		homeProjectsJLabel.setBounds(10, 11, 157, 16);
 		homeFieldsJPanel.add(homeProjectsJLabel);
 
 		homeTaskJLabel = new JLabel();
 		homeTaskJLabel.setText("Tasks");
 		homeTaskJLabel.setForeground(new Color(0, 153, 153));
 		homeTaskJLabel.setFont(new Font("Tw Cen MT", Font.BOLD, 14));
 		homeTaskJLabel.setBounds(177, 11, 159, 16);
 		homeFieldsJPanel.add(homeTaskJLabel);
 
 		scrollPane = new JScrollPane();
 		scrollPane.setBounds(11, 34, 155, 302);
 		homeFieldsJPanel.add(scrollPane);
 
 		homeProjectsJList = new JList();
 		homeProjectsJList.addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyPressed(KeyEvent arg0) {
 				setCurrentProjectWithEnter(arg0);
 			}
 		});
 		homeProjectsJList.addListSelectionListener(new ListSelectionListener() {
 			public void valueChanged(ListSelectionEvent arg0) {
 				homeProjectListValueChanged(arg0);
 			}
 		});
 		homeProjectsJList.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
 		scrollPane.setViewportView(homeProjectsJList);
 		homeProjectsJList.setToolTipText("Press enter to set the current project");
 
 		scrollPane_1 = new JScrollPane();
 		scrollPane_1.setBounds(177, 34, 159, 302);
 		homeFieldsJPanel.add(scrollPane_1);
 
 		homeTasksJList = new JList();
 		homeTasksJList.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
 		scrollPane_1.setViewportView(homeTasksJList);
 
 		projectsJPanel.setBackground(Color.GRAY);
 
 		projectsJLabel.setFont(new java.awt.Font("Tw Cen MT", 1, 14));
 		projectsJLabel.setForeground(new java.awt.Color(255, 255, 255));
 		projectsJLabel.setText("Projects");
 
 		setCurrentProjectJButton.setText("Set as current project");
 		setCurrentProjectJButton.setEnabled(false);
 		setCurrentProjectJButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				setCurrentProjectJButtonActionPerformed(evt);
 			}
 		});
 
 		removeProjectJButton.setText("Remove project");
 		removeProjectJButton.setEnabled(false);
 		removeProjectJButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				deleteProject();
 			}
 		});
 		projectsJList = new javax.swing.JList();
 		projectsJList.addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyPressed(KeyEvent arg0) {
 				setCurrentProjectWithEnter(arg0);
 			}
 		});
 		jScrollPane1.setViewportView(projectsJList);
 
 		projectsJList.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
 		projectsJList.setModel(new javax.swing.AbstractListModel() {
 			String[] strings = {};
 
 			public int getSize() {
 				return strings.length;
 			}
 
 			public Object getElementAt(int i) {
 				return strings[i];
 			}
 		});
 		projectsJList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
 			public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
 				projectsJListValueChanged(evt);
 			}
 		});
 
 		contentJTabbedPane.addTab("", new javax.swing.ImageIcon(getClass().getResource("/be/artesis/timelog/gui/icons/ProjectsNeonIcon.png")), projectsJPanel, "Projects");
 		projectsJPanel.setLayout(null);
 		projectsJPanel.add(removeProjectJButton);
 		projectsJPanel.add(setCurrentProjectJButton);
 		projectsJPanel.add(jScrollPane1);
 		projectsJPanel.add(filler2);
 		projectsJPanel.add(projectsJLabel);
 
 		projectFieldsJPanel = new JPanel();
 		projectFieldsJPanel.setBorder(null);
 		projectFieldsJPanel.setBackground(Color.GRAY);
 		projectFieldsJPanel.setBounds(224, 33, 450, 407);
 		projectsJPanel.add(projectFieldsJPanel);
 		projectFieldsJPanel.setLayout(null);
 
 		projectEditFieldsJPanel = new JPanel();
 		projectEditFieldsJPanel.setBounds(112, 0, 338, 175);
 		projectFieldsJPanel.add(projectEditFieldsJPanel);
 		projectEditFieldsJPanel.setBackground(Color.DARK_GRAY);
 		projectEditFieldsJPanel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
 		projectEditFieldsJPanel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.UNRELATED_GAP_COLSPEC, ColumnSpec.decode("65px"), FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("max(53dlu;default):grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("123px"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] { FormFactory.UNRELATED_GAP_ROWSPEC, RowSpec.decode("20px"), FormFactory.LINE_GAP_ROWSPEC, FormFactory.PREF_ROWSPEC, FormFactory.LINE_GAP_ROWSPEC, FormFactory.PREF_ROWSPEC, FormFactory.UNRELATED_GAP_ROWSPEC, RowSpec.decode("20px"), FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("21px"), RowSpec.decode("23px"), }));
 		namecompJLabel = new javax.swing.JLabel();
 		projectEditFieldsJPanel.add(namecompJLabel, "2, 2, left, center");
 
 		namecompJLabel.setForeground(new java.awt.Color(255, 255, 255));
 		namecompJLabel.setText("Naam");
 		projectNameJTextField = new javax.swing.JTextField();
 		projectEditFieldsJPanel.add(projectNameJTextField, "4, 2, 3, 1, fill, top");
 		startdatecompJLabel = new javax.swing.JLabel();
 		projectEditFieldsJPanel.add(startdatecompJLabel, "2, 4, 2, 1, left, center");
 
 		startdatecompJLabel.setForeground(new java.awt.Color(255, 255, 255));
 		startdatecompJLabel.setText("Start date");
 
 		projectStartDateChooser = new JDateChooser();
 		projectStartDateChooser.setDateFormatString("dd/MM/yyyy");
 		projectEditFieldsJPanel.add(projectStartDateChooser, "4, 4, fill, fill");
 		enddatecompJLabel1 = new javax.swing.JLabel();
 		projectEditFieldsJPanel.add(enddatecompJLabel1, "2, 6, 2, 1, left, center");
 
 		enddatecompJLabel1.setForeground(new java.awt.Color(255, 255, 255));
 		enddatecompJLabel1.setText("End date");
 
 		projectEndDateChooser = new JDateChooser();
 		projectEndDateChooser.setDateFormatString("dd/MM/yyyy");
 		projectEditFieldsJPanel.add(projectEndDateChooser, "4, 6, fill, fill");
 		clientcompJLabel1 = new javax.swing.JLabel();
 		projectEditFieldsJPanel.add(clientcompJLabel1, "2, 8, left, top");
 
 		clientcompJLabel1.setForeground(new java.awt.Color(255, 255, 255));
 		clientcompJLabel1.setText("Client");
 
 		projectClientsJComboBox = new JComboBox();
 		projectClientsJComboBox.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				clientsJComboBoxValueChanged(arg0);
 			}
 		});
 		projectEditFieldsJPanel.add(projectClientsJComboBox, "4, 8, 3, 1, fill, default");
 		saveProjectJButton = new javax.swing.JButton();
 		projectEditFieldsJPanel.add(saveProjectJButton, "2, 11, 5, 1, fill, top");
 
 		saveProjectJButton.setText("Save");
 		saveProjectJButton.setEnabled(false);
 
 		projectTasksJList = new JList();
 		projectTasksJList.setBounds(187, 218, 263, 96);
 		projectFieldsJPanel.add(projectTasksJList);
 		projectTasksJList.setEnabled(false);
 		projectTasksJList.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
 		projectTasksJList.setBackground(Color.LIGHT_GRAY);
 		taskscompJLabel = new javax.swing.JLabel();
 		taskscompJLabel.setBounds(112, 221, 65, 14);
 		projectFieldsJPanel.add(taskscompJLabel);
 
 		taskscompJLabel.setForeground(new java.awt.Color(255, 255, 255));
 		taskscompJLabel.setText("Tasks");
 		percentageCompleteJProgressBar = new javax.swing.JProgressBar();
 		percentageCompleteJProgressBar.setBounds(187, 186, 263, 21);
 		projectFieldsJPanel.add(percentageCompleteJProgressBar);
 		percentageCompletecompJLabel = new javax.swing.JLabel();
 		percentageCompletecompJLabel.setBounds(112, 186, 65, 21);
 		projectFieldsJPanel.add(percentageCompletecompJLabel);
 
 		percentageCompletecompJLabel.setForeground(new java.awt.Color(255, 255, 255));
 		percentageCompletecompJLabel.setText("Complete");
 		saveProjectJButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				try {
 					String name = projectNameJTextField.getText();
 					long startdate;
 					long enddate;
 					try {
 						startdate = projectStartDateChooser.getDate().getTime() / 1000;
 						enddate = projectEndDateChooser.getDate().getTime() / 1000;
 					} catch (NullPointerException e) {
 						throw new DataInputException("Please choose a valid date");
 					}
 					if (projectClientsJComboBox.getSelectedIndex() == -1 || projectClientsJComboBox.getSelectedItem().equals(NEWCLIENTITEM)) {
 						throw new DataInputException("Select or create a client first");
 					}
 					Opdrachtgever o = (Opdrachtgever) projectClientsJComboBox.getSelectedItem();
 					int opdrachtgeverID = o.getID();
 
 					if (projectsJList.getSelectedValue().equals(NEWPROJECTITEM)) {
 						createProject(name, startdate, enddate, opdrachtgeverID);
 					} else {
 						updateProject(name, startdate, enddate, opdrachtgeverID);
 					}
 				} catch (DataInputException e) {
 					JOptionPane.showMessageDialog(GUIForm.this, e.getMessage());
 					e.printStackTrace();
 				}
 				toggleButtonStates();
 			}
 		});
 
 		projectNameJTextField.addFocusListener(new java.awt.event.FocusAdapter() {
 			public void focusGained(java.awt.event.FocusEvent evt) {
 				editFieldsFocused(evt);
 			}
 		});
 
 		tasksJPanel.setBackground(Color.GRAY);
 
 		tasksJLabel.setFont(new java.awt.Font("Tw Cen MT", 1, 14));
 		tasksJLabel.setForeground(new java.awt.Color(255, 255, 255));
 		tasksJLabel.setText("Tasks");
 
 		removeTaskJButton.setText("Remove task");
 		removeTaskJButton.setEnabled(false);
 		removeTaskJButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				deleteTask();
 			}
 		});
 
 		contentJTabbedPane.addTab("", new javax.swing.ImageIcon(getClass().getResource("/be/artesis/timelog/gui/icons/TasksNeonIcon.png")), tasksJPanel, "Tasks");
 		tasksJPanel.setLayout(null);
 		tasksJPanel.add(tasksJLabel);
 		tasksJPanel.add(jScrollPane3);
 
 		tasksJList = new JList();
 		tasksJList.setModel(new AbstractListModel() {
 			String[] values = new String[] { "Select a current project first!" };
 
 			public int getSize() {
 				return values.length;
 			}
 
 			public Object getElementAt(int index) {
 				return values[index];
 			}
 		});
 		tasksJList.addListSelectionListener(new ListSelectionListener() {
 			public void valueChanged(ListSelectionEvent arg0) {
 				taskJListValueChanged(arg0);
 			}
 		});
 		tasksJList.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
 		jScrollPane3.setViewportView(tasksJList);
 		tasksJPanel.add(removeTaskJButton);
 
 		taskFieldsJPanel = new JPanel();
 		taskFieldsJPanel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
 		taskFieldsJPanel.setBackground(Color.DARK_GRAY);
 		taskFieldsJPanel.setBounds(275, 40, 399, 400);
 		tasksJPanel.add(taskFieldsJPanel);
 		taskFieldsJPanel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.UNRELATED_GAP_COLSPEC, ColumnSpec.decode("56px"), FormFactory.UNRELATED_GAP_COLSPEC, ColumnSpec.decode("1px"), FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("110px:grow"), FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("55px"), ColumnSpec.decode("51px"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(27dlu;default)"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(15dlu;default)"), }, new RowSpec[] { FormFactory.UNRELATED_GAP_ROWSPEC, RowSpec.decode("20px"), FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("20px:grow"), FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("20px:grow"), RowSpec.decode("38px"), RowSpec.decode("51px"), FormFactory.UNRELATED_GAP_ROWSPEC, RowSpec.decode("51px"), FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("37px"), FormFactory.UNRELATED_GAP_ROWSPEC, RowSpec.decode("23px"), FormFactory.UNRELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.UNRELATED_GAP_ROWSPEC, RowSpec.decode("41px"), }));
 
 		label_5 = new JLabel();
 		label_5.setText("Name");
 		label_5.setForeground(Color.WHITE);
 		taskFieldsJPanel.add(label_5, "2, 2, fill, center");
 
 		taskNameJTextField = new JTextField();
 		taskNameJTextField.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusGained(FocusEvent e) {
 				editFieldsFocused(e);
 			}
 		});
 		taskFieldsJPanel.add(taskNameJTextField, "6, 2, 7, 1, fill, top");
 
 		label_6 = new JLabel();
 		label_6.setText("Start date");
 		label_6.setForeground(Color.WHITE);
 		taskFieldsJPanel.add(label_6, "2, 4, fill, center");
 
 		taskStartDateChooser = new JDateChooser();
 		taskStartDateChooser.setDateFormatString("dd/MM/yyyy");
 		taskFieldsJPanel.add(taskStartDateChooser, "6, 4, fill, center");
 
 		label_7 = new JLabel();
 		label_7.setText("Completed");
 		label_7.setForeground(Color.WHITE);
 		taskFieldsJPanel.add(label_7, "9, 4, 2, 1, center, center");
 
 		taskCompletedJCheckBox = new JCheckBox();
 		taskCompletedJCheckBox.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusGained(FocusEvent arg0) {
 				editFieldsFocused(arg0);
 			}
 		});
 		taskCompletedJCheckBox.setBackground(Color.DARK_GRAY);
 		taskFieldsJPanel.add(taskCompletedJCheckBox, "12, 4, left, center");
 
 		label_8 = new JLabel();
 		label_8.setText("End date");
 		label_8.setForeground(Color.WHITE);
 		taskFieldsJPanel.add(label_8, "2, 6, fill, center");
 
 		taskEndDateChooser = new JDateChooser();
 		taskEndDateChooser.setDateFormatString("dd/MM/yyyy");
 		taskFieldsJPanel.add(taskEndDateChooser, "6, 6, fill, center");
 
 		label_9 = new JLabel();
 		label_9.setText("Comment");
 		label_9.setForeground(Color.WHITE);
 		taskFieldsJPanel.add(label_9, "2, 8, fill, top");
 
 		lblWorked = new JLabel();
 		lblWorked.setText("Worked");
 		lblWorked.setForeground(Color.WHITE);
 		taskFieldsJPanel.add(lblWorked, "2, 10, 3, 1, fill, top");
 
 		saveTaskJButton = new JButton();
 		saveTaskJButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					String name = taskNameJTextField.getText();
 					String comment = taskCommentJTextArea.getText();
 					boolean completed = taskCompletedJCheckBox.isSelected();
 					long startdate = taskStartDateChooser.getDate().getTime() / 1000;
 					long enddate = taskEndDateChooser.getDate().getTime() / 1000;
 					
 					if (tasksJList.getSelectedValue().equals(NEWTASKITEM)) {
 						createTask(name, startdate, enddate, comment, completed);
 					}
 					else {
 						updateTask(name, startdate, enddate, comment, completed);
 					}
 				} catch (NullPointerException ex) {
 					ex.printStackTrace();
 					JOptionPane.showMessageDialog(GUIForm.this, "Please choose a valid date");
 				} finally {
 					toggleButtonStates();
 					loadProjectInfo(projectsJList.getSelectedIndex());
 				}
 			}
 			});
 
 		taskTotalWorkedJLabel = new JLabel("Total worked");
 		taskTotalWorkedJLabel.setForeground(Color.WHITE);
 		taskFieldsJPanel.add(taskTotalWorkedJLabel, "2, 14, 2, 1, left, default");
 
 		taskTotalWorkedJTextField = new JTextField();
 		taskTotalWorkedJTextField.setEditable(false);
 		taskFieldsJPanel.add(taskTotalWorkedJTextField, "6, 14, 3, 1, fill, default");
 		taskTotalWorkedJTextField.setColumns(10);
 
 		lblTotalPaused = new JLabel("Total paused");
 		lblTotalPaused.setForeground(Color.WHITE);
 		taskFieldsJPanel.add(lblTotalPaused, "2, 16, 2, 1, left, default");
 
 		taskTotalPauseJTextField = new JTextField();
 		taskTotalPauseJTextField.setEditable(false);
 		taskFieldsJPanel.add(taskTotalPauseJTextField, "6, 16, 3, 1, fill, default");
 		taskTotalPauseJTextField.setColumns(10);
 		saveTaskJButton.setText("Save");
 		saveTaskJButton.setEnabled(false);
 		taskFieldsJPanel.add(saveTaskJButton, "2, 18, 11, 1, fill, top");
 
 		taskCommentJTextArea = new JTextArea();
 		taskCommentJTextArea.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusGained(FocusEvent e) {
 				editFieldsFocused(e);
 			}
 		});
 		taskFieldsJPanel.add(taskCommentJTextArea, "6, 8, 7, 1, fill, fill");
 
 		workedTimeJList = new JList();
 		taskFieldsJPanel.add(workedTimeJList, "6, 10, 7, 3, fill, fill");
 		clientsJPanel = new javax.swing.JPanel();
 		clientsJLabel = new javax.swing.JLabel();
 		jScrollPane5 = new javax.swing.JScrollPane();
 		removeClientJButton = new javax.swing.JButton();
 
 		clientsJPanel.setBackground(Color.GRAY);
 
 		clientsJLabel.setFont(new java.awt.Font("Tw Cen MT", 1, 14));
 		clientsJLabel.setForeground(new java.awt.Color(255, 255, 255));
 		clientsJLabel.setText("Clients");
 
 		removeClientJButton.setText("Remove client");
 		removeClientJButton.setEnabled(false);
 		removeClientJButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				deleteClient();
 			}
 		});
 
 		clientFieldsJPanel = new JPanel();
 		clientFieldsJPanel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
 		clientFieldsJPanel.setBackground(Color.DARK_GRAY);
 
 		javax.swing.GroupLayout clientsJPanelLayout = new javax.swing.GroupLayout(clientsJPanel);
 		clientsJPanelLayout.setHorizontalGroup(clientsJPanelLayout.createParallelGroup(Alignment.LEADING).addGroup(clientsJPanelLayout.createSequentialGroup().addContainerGap().addGroup(clientsJPanelLayout.createParallelGroup(Alignment.LEADING).addGroup(clientsJPanelLayout.createSequentialGroup().addGroup(clientsJPanelLayout.createParallelGroup(Alignment.LEADING).addComponent(removeClientJButton).addComponent(jScrollPane5, GroupLayout.PREFERRED_SIZE, 245, GroupLayout.PREFERRED_SIZE)).addPreferredGap(ComponentPlacement.RELATED, 95, Short.MAX_VALUE).addComponent(clientFieldsJPanel, GroupLayout.PREFERRED_SIZE, 324, GroupLayout.PREFERRED_SIZE)).addComponent(clientsJLabel)).addContainerGap()));
 		clientsJPanelLayout.setVerticalGroup(clientsJPanelLayout.createParallelGroup(Alignment.TRAILING).addGroup(clientsJPanelLayout.createSequentialGroup().addGroup(clientsJPanelLayout.createParallelGroup(Alignment.LEADING).addGroup(clientsJPanelLayout.createSequentialGroup().addGap(33).addComponent(clientFieldsJPanel, GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE)).addGroup(clientsJPanelLayout.createSequentialGroup().addContainerGap().addComponent(clientsJLabel).addPreferredGap(ComponentPlacement.RELATED).addComponent(jScrollPane5, GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE))).addPreferredGap(ComponentPlacement.RELATED).addComponent(removeClientJButton, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE).addContainerGap()));
 
 		clientsJList = new JList();
 		clientsJList.addListSelectionListener(new ListSelectionListener() {
 			public void valueChanged(ListSelectionEvent arg0) {
 				clientsJListValueChanged(arg0);
 			}
 		});
 		jScrollPane5.setViewportView(clientsJList);
 		clientFieldsJPanel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.UNRELATED_GAP_COLSPEC, ColumnSpec.decode("79px"), ColumnSpec.decode("220px"), }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("20px"), FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("20px"), FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("20px"), FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("20px"), FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("20px"), RowSpec.decode("192px"), RowSpec.decode("17px"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));
 
 		JLabel label = new JLabel();
 		label.setText("Name");
 		label.setForeground(Color.WHITE);
 		clientFieldsJPanel.add(label, "2, 2, fill, center");
 
 		clientNameJTextField = new JTextField();
 		clientNameJTextField.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusGained(FocusEvent e) {
 				editFieldsFocused(e);
 			}
 		});
 		clientFieldsJPanel.add(clientNameJTextField, "3, 2, fill, top");
 
 		JLabel label_1 = new JLabel();
 		label_1.setText("First name");
 		label_1.setForeground(Color.WHITE);
 		clientFieldsJPanel.add(label_1, "2, 4, fill, center");
 
 		clientFirstNameJTextField = new JTextField();
 		clientFirstNameJTextField.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusGained(FocusEvent e) {
 				editFieldsFocused(e);
 			}
 		});
 		clientFieldsJPanel.add(clientFirstNameJTextField, "3, 4, fill, top");
 
 		JLabel label_2 = new JLabel();
 		label_2.setText("Company");
 		label_2.setForeground(Color.WHITE);
 		clientFieldsJPanel.add(label_2, "2, 6, fill, center");
 
 		clientCompanyJTextField = new JTextField();
 		clientCompanyJTextField.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusGained(FocusEvent e) {
 				editFieldsFocused(e);
 			}
 		});
 		clientFieldsJPanel.add(clientCompanyJTextField, "3, 6, fill, top");
 
 		JLabel label_3 = new JLabel();
 		label_3.setText("E-mail");
 		label_3.setForeground(Color.WHITE);
 		clientFieldsJPanel.add(label_3, "2, 8, fill, center");
 
 		clientEmailJTextField = new JTextField();
 		clientEmailJTextField.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusGained(FocusEvent e) {
 				editFieldsFocused(e);
 			}
 		});
 		clientFieldsJPanel.add(clientEmailJTextField, "3, 8, fill, top");
 
 		JLabel lblPhone = new JLabel();
 		lblPhone.setText("Phone");
 		lblPhone.setForeground(Color.WHITE);
 		clientFieldsJPanel.add(lblPhone, "2, 10, fill, center");
 
 		clientPhoneNumberJTextField = new JTextField();
 		clientPhoneNumberJTextField.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusGained(FocusEvent e) {
 				editFieldsFocused(e);
 			}
 		});
 		clientFieldsJPanel.add(clientPhoneNumberJTextField, "3, 10, fill, top");
 
 		saveClientJButton = new JButton();
 		saveClientJButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				String naam = clientNameJTextField.getText();
 				String voornaam = clientFirstNameJTextField.getText();
 				String bedrijfsnaam = clientCompanyJTextField.getText();
 				String email = clientEmailJTextField.getText();
 				String telefoonnummer = clientPhoneNumberJTextField.getText();
 
 				if (clientsJList.getSelectedValue().equals(NEWCLIENTITEM)) {
 					createClient(naam, voornaam, bedrijfsnaam, email, telefoonnummer);
 				} else {
 					updateClient(naam, voornaam, bedrijfsnaam, email, telefoonnummer);
 				}
 				toggleButtonStates();
 			}
 		});
 		saveClientJButton.setText("Save");
 		saveClientJButton.setEnabled(false);
 		clientFieldsJPanel.add(saveClientJButton, "2, 14, 2, 1, fill, top");
 		clientsJPanel.setLayout(clientsJPanelLayout);
 
 		contentJTabbedPane.addTab("", new javax.swing.ImageIcon(getClass().getResource("/be/artesis/timelog/gui/icons/ClientsNeonIcon.png")), clientsJPanel, "Clients");
 
 		scheduleJPanel.setBackground(Color.GRAY);
 
 		scheduleJLabel.setFont(new java.awt.Font("Tw Cen MT", 1, 14));
 		scheduleJLabel.setForeground(new java.awt.Color(255, 255, 255));
 		scheduleJLabel.setText("Schedule");
 
 		contentJTabbedPane.addTab("", new javax.swing.ImageIcon(getClass().getResource("/be/artesis/timelog/gui/icons/CalendarNeonIcon.png")), scheduleJPanel, "Schedule");
 		scheduleJPanel.setLayout(null);
 		scheduleJPanel.add(scheduleJLabel);
 
 		importExportJPanel = new JPanel();
 		importExportJPanel.setBackground(Color.GRAY);
 		contentJTabbedPane.addTab("", new ImageIcon(GUIForm.class.getResource("/be/artesis/timelog/gui/icons/ImportExportNeonIcon.png")), importExportJPanel, "Import / Export");
 		importExportJPanel.setLayout(null);
 
 		importExportTabbedPane = new JTabbedPane(SwingConstants.TOP);
 		importExportTabbedPane.setForeground(Color.GRAY);
 		importExportTabbedPane.setBorder(null);
 		importExportTabbedPane.setBackground(Color.GRAY);
 		importExportTabbedPane.setBounds(10, 11, 664, 429);
 		importExportJPanel.add(importExportTabbedPane);
 
 		exportJPanel = new JPanel();
 		exportJPanel.setBackground(Color.DARK_GRAY);
 		exportJPanel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
 		importExportTabbedPane.addTab("Export", null, exportJPanel, "Export your tasks here");
 		importExportTabbedPane.setForegroundAt(0, Color.WHITE);
 		exportJPanel.setLayout(null);
 		exportJScrollPane = new JScrollPane();
 		exportJScrollPane.setBounds(10, 11, 320, 347);
 		exportJPanel.add(exportJScrollPane);
 
 		exportTree = new JTree();
 		exportJScrollPane.setViewportView(exportTree);
 		exportTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("Tasks to export show here") {
 			{
 			}
 		}));
 
 		exportJButton = new JButton("Export");
 		exportJButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				exportButtonClicked(arg0);
 			}
 		});
 		exportJButton.setBounds(10, 367, 320, 23);
 		exportJPanel.add(exportJButton);
 		importExportTabbedPane.setBackgroundAt(0, Color.DARK_GRAY);
 
 		importJPanel = new JPanel();
 		importJPanel.setBackground(Color.DARK_GRAY);
 		importJPanel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
 		importExportTabbedPane.addTab("Import", null, importJPanel, "Import tasks here");
 		importExportTabbedPane.setForegroundAt(1, Color.WHITE);
 		importExportTabbedPane.setBackgroundAt(1, Color.DARK_GRAY);
 		importJPanel.setLayout(null);
 
 		importTree = new JTree();
 		importTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("Projects to import show here") {
 			{
 			}
 		}));
 		importJScrollPane = new JScrollPane(importTree);
 		importJScrollPane.setBounds(10, 11, 320, 347);
 		importJPanel.add(importJScrollPane);
 
 		importJButton = new JButton("Import");
 		importJButton.setBounds(10, 367, 320, 23);
 		importJPanel.add(importJButton);
 
 		optionsJPanel.setBackground(Color.GRAY);
 
 		settingsJLabel.setFont(new java.awt.Font("Tw Cen MT", 1, 14));
 		settingsJLabel.setForeground(new java.awt.Color(255, 255, 255));
 		settingsJLabel.setText("Settings");
 
 		javax.swing.GroupLayout optionsJPanelLayout = new javax.swing.GroupLayout(optionsJPanel);
 		optionsJPanel.setLayout(optionsJPanelLayout);
 		optionsJPanelLayout.setHorizontalGroup(optionsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(optionsJPanelLayout.createSequentialGroup().addContainerGap().addComponent(settingsJLabel).addContainerGap(627, Short.MAX_VALUE)));
 		optionsJPanelLayout.setVerticalGroup(optionsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(optionsJPanelLayout.createSequentialGroup().addContainerGap().addComponent(settingsJLabel).addContainerGap(369, Short.MAX_VALUE)));
 
 		contentJTabbedPane.addTab("", new javax.swing.ImageIcon(getClass().getResource("/be/artesis/timelog/gui/icons/SettingsNeonIcon.png")), optionsJPanel, "Settings");
 
 		headerJPanel.setBackground(new java.awt.Color(64, 64, 64));
 
 		titleLabel.setFont(new java.awt.Font("Tempus Sans ITC", 1, 18));
 		titleLabel.setForeground(new java.awt.Color(255, 255, 255));
 		titleLabel.setText("ChronoMatic");
 
 		ingelogdJLabel.setBackground(new java.awt.Color(255, 255, 255));
 		ingelogdJLabel.setFont(new java.awt.Font("Tahoma", 1, 11));
 		ingelogdJLabel.setForeground(new java.awt.Color(255, 0, 0));
 		ingelogdJLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
 		ingelogdJLabel.setText("Not logged in");
 		ingelogdJLabel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
 		ingelogdJLabel.setName("");
 		ingelogdJLabel.setOpaque(true);
 
 		currentProjectJLabel.setBackground(new java.awt.Color(255, 255, 255));
 		currentProjectJLabel.setFont(new java.awt.Font("Tahoma", 1, 11));
 		currentProjectJLabel.setForeground(new java.awt.Color(0, 204, 204));
 		currentProjectJLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
 		currentProjectJLabel.setText("Current project: ...");
 		currentProjectJLabel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
 		currentProjectJLabel.setName("");
 		currentProjectJLabel.setOpaque(true);
 
 		clockJLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/be/artesis/timelog/gui/icons/ClockNeonIcon.png")));
 
 		javax.swing.GroupLayout headerJPanelLayout = new javax.swing.GroupLayout(headerJPanel);
 		headerJPanel.setLayout(headerJPanelLayout);
 		headerJPanelLayout.setHorizontalGroup(headerJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, headerJPanelLayout.createSequentialGroup().addContainerGap().addGroup(headerJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false).addComponent(currentProjectJLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE).addComponent(ingelogdJLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(titleLabel).addGap(18, 18, 18).addComponent(clockJLabel).addGap(6, 6, 6)));
 		headerJPanelLayout.setVerticalGroup(headerJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(headerJPanelLayout.createSequentialGroup().addContainerGap().addGroup(headerJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(titleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(clockJLabel, javax.swing.GroupLayout.Alignment.TRAILING).addGroup(headerJPanelLayout.createSequentialGroup().addComponent(ingelogdJLabel).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(currentProjectJLabel).addGap(0, 0, Short.MAX_VALUE))).addContainerGap()));
 
 		clockJLabel.getAccessibleContext().setAccessibleName("iconJLabel");
 
 		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
 		getContentPane().setLayout(layout);
 		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(headerJPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(contentJTabbedPane));
 		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addComponent(headerJPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(contentJTabbedPane)));
 		pack();
 
 		// set form in center
 		final Toolkit toolkit = Toolkit.getDefaultToolkit();
 		final Dimension screenSize = toolkit.getScreenSize();
 		final int x = (screenSize.width - this.getWidth()) / 2;
 		final int y = (screenSize.height - this.getHeight()) / 2;
 		this.setLocation(x, y);
 	}
 
 	/* Einde gegenereerde code */
 
 	// FIXME edit fields van elk panel disablen als niks geselecteerd is
 
 	// ================================================================================
 	// Save / Edit methods
 	// ================================================================================
 
 	// Create new project
 	private void createProject(String name, long startdate, long enddate, int opdrachtgeverID) {
 		try {
 			UserInterface.createProject(name, startdate, enddate, opdrachtgeverID);
 			JOptionPane.showMessageDialog(this, "Project added!");
 			refreshProjectsList(projectsJList, homeProjectsJList);
 		} catch (DataInputException | ParseException | IOException | WebserviceException | JSONException e) {
 			JOptionPane.showMessageDialog(this, e.getMessage());
 			e.printStackTrace();
 		}
 	}
 	
 	// Update existing project
 	private void updateProject(String name, long startdate, long enddate, int opdrachtgeverID){
 		try {
 			UserInterface.updateProject(projectsJList.getSelectedIndex(), name, startdate, enddate, opdrachtgeverID);
 			JOptionPane.showMessageDialog(this, "Project edited!");
 			refreshProjectsList(projectsJList, homeProjectsJList);;
 		} catch (DataInputException | IOException | WebserviceException | ParseException e) {
 			JOptionPane.showMessageDialog(this, e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	// Create new task
 	private void createTask(String name, long startdate, long enddate, String comment, boolean completed){
 		try {
 			UserInterface.createTask(name, startdate, enddate, comment, completed);
 			JOptionPane.showMessageDialog(this, "Task added!");
 			refreshTasksList(UserInterface.getCurrentProject(), tasksJList);
 		} catch (DataInputException | ParseException | GUIException | IOException | WebserviceException | JSONException e) {
 			JOptionPane.showMessageDialog(this, e.getMessage());
 			e.printStackTrace();
 		}
 	}
 	
 	// Update existing task
 	private void updateTask(String name, long startdate, long enddate, String comment, boolean completed){
 		try {
 			UserInterface.updateTask(tasksJList.getSelectedIndex(), name, startdate, enddate, comment, completed);
 			JOptionPane.showMessageDialog(this, "Task edited!");
 			refreshTasksList(UserInterface.getCurrentProject(), tasksJList);
 		} catch (GUIException | DataInputException | ParseException | IOException | WebserviceException e) {
 			JOptionPane.showMessageDialog(this, e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	// Create new client
 	private void createClient(String naam, String voornaam, String bedrijfsnaam, String email, String telefoonnummer) {
 		Opdrachtgever o = null;
 		try {
 			o = UserInterface.createClient(naam, voornaam, bedrijfsnaam, email, telefoonnummer);
 			JOptionPane.showMessageDialog(this, "Client added!");
 			if (projectsJList.getSelectedValue().equals(NEWPROJECTITEM)) {
 				refreshClientsComboBox(null, projectClientsJComboBox);
 			} else {
 				refreshClientsComboBox((Project) projectsJList.getSelectedValue(), projectClientsJComboBox);
 			}
 			refreshClientsList(clientsJList);
 			if (creatingProject) {
 				contentJTabbedPane.setSelectedIndex(1);
 				projectClientsJComboBox.setSelectedItem(o);
 				creatingProject = false;
 			}
 		} catch (DataInputException | JSONException | IOException | WebserviceException e) {
 			JOptionPane.showMessageDialog(this, e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	// Update existing client
 	private void updateClient(String naam, String voornaam, String bedrijfsnaam, String email, String telefoonnummer) {
 		try {
 			UserInterface.updateClient(clientsJList.getSelectedIndex(), voornaam, voornaam, bedrijfsnaam, email, telefoonnummer);
 			JOptionPane.showMessageDialog(this, "Client edited!");
 			refreshClientsList(clientsJList);
 		} catch (DataInputException | IOException | WebserviceException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	// ================================================================================
 	// Remove methods
 	// ================================================================================
 
 	// Remove PROJECT
 	private void deleteProject() {
 		try {
 			if (UserInterface.getCurrentProjectIndex() != -1 && projectsJList.getSelectedValue() == UserInterface.getCurrentProject()) {
 				JOptionPane.showMessageDialog(this, "Can't remove current project");
 			} else {
 				int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this project?", null, JOptionPane.YES_NO_OPTION);
 				if (result == JOptionPane.YES_OPTION) {
 					try {
 						UserInterface.deleteProject((Project) projectsJList.getSelectedValue());
 						JOptionPane.showMessageDialog(this, "Project removed!");
 						refreshProjectsList(projectsJList, homeProjectsJList);
 					} catch (IOException | WebserviceException ex) {
 						ex.printStackTrace();
 						JOptionPane.showMessageDialog(this, ex.getMessage());
 					} finally {
 						clearFieldsOnPanel(projectEditFieldsJPanel);
 						toggleButtonStates();
 						selectNewItem(projectsJList, tasksJList);
 					}
 				}
 			}
 		} catch (GUIException e) {
 			e.printStackTrace();
 		}
 	}
 
 	// Remove TASK
 	private void deleteTask() {
 		int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this task?", null, JOptionPane.YES_NO_OPTION);
 		if (result == JOptionPane.YES_OPTION) {
 			try {
 				UserInterface.deleteTask((Taak) tasksJList.getSelectedValue());
				refreshTasksList(UserInterface.getCurrentProject(), tasksJList);
 				selectNewItem(tasksJList);
 				JOptionPane.showMessageDialog(this, "Task removed!");
 			} catch (GUIException | IOException | WebserviceException ex) {
 				ex.printStackTrace();
 				JOptionPane.showMessageDialog(this, ex.getMessage());
 			} finally {
 				clearFieldsOnPanel(taskFieldsJPanel);
 				toggleButtonStates();
 			}
 		}
 	}
 
 	// Remove CLIENT
 	private void deleteClient() {
 		int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this client?", null, JOptionPane.YES_NO_OPTION);
 		if (result == JOptionPane.YES_OPTION) {
 			try {
 				UserInterface.deleteClient((Opdrachtgever) clientsJList.getSelectedValue());
 				refreshClientsList(clientsJList);
 				clearFieldsOnPanel(clientFieldsJPanel);
 				toggleButtonStates();
 				selectNewItem(clientsJList);
 				JOptionPane.showMessageDialog(this, "Client removed!");
 			} catch (GUIException | IOException | WebserviceException ex) {
 				ex.printStackTrace();
 				JOptionPane.showMessageDialog(this, ex.getMessage());
 			}
 		}
 	}
 
 	// ================================================================================
 	// Refresh methods
 	// ================================================================================
 
 	// Refresh all PROJECT lists
 	private void refreshProjectsList(JList... lists) {
 		for (JList list : lists) {
 			int selectedIndex = list.getSelectedIndex();
 			DefaultListModel listmodel = new DefaultListModel();
 
 			for (Iterator<Project> it = UserInterface.getProjects().iterator(); it.hasNext();) {
 				Project p = it.next();
 				listmodel.addElement(p);
 			}
 
 			if (list.equals(projectsJList)) {
 				listmodel.addElement(NEWPROJECTITEM);
 				list.setModel(listmodel);
 			} else {
 				list.setModel(listmodel);
 			}
 			list.setCellRenderer(new ProjectCellRenderer());
 			list.setSelectedIndex(selectedIndex);
 		}
 		refreshTreeView(exportTree, UserInterface.getProjects());
 	}
 
 	// Refresh all TASK lists
 	private void refreshTasksList(Project p, JList... lists) {
 		for (JList list : lists) {
 			int selectedIndex = list.getSelectedIndex();
 			DefaultListModel listmodel = new DefaultListModel();
 
 			for (Iterator<Taak> it = p.getTaken().iterator(); it.hasNext();) {
 				Taak t = it.next();
 				listmodel.addElement(t);
 			}
 
 			if (list.equals(tasksJList)) {
 				listmodel.addElement(NEWTASKITEM);
 				list.setModel(listmodel);
 			} else {
 				list.setModel(listmodel);
 			}
 			list.setCellRenderer(new TaskCellRenderer());
 			list.setSelectedIndex(selectedIndex);
 		}
 		refreshTreeView(exportTree, UserInterface.getProjects());
 	}
 
 	// Refresh all CLIENT lists
 	private void refreshClientsList(JList... lists) {
 		for (JList list : lists) {
 			int selectedIndex = list.getSelectedIndex();
 			DefaultListModel listmodel = new DefaultListModel();
 
 			for (Iterator<Opdrachtgever> it = UserInterface.getClients().iterator(); it.hasNext();) {
 				Opdrachtgever o = it.next();
 				listmodel.addElement(o);
 			}
 
 			if (list == clientsJList) {
 				listmodel.addElement(NEWCLIENTITEM);
 				list.setModel(listmodel);
 			} else {
 				list.setModel(listmodel);
 			}
 			list.setCellRenderer(new ClientCellRenderder());
 			list.setSelectedIndex(selectedIndex);
 		}
 	}
 
 	private void refreshClientsComboBox(Project p, JComboBox... boxes) {
 		for (JComboBox box : boxes) {
 			DefaultComboBoxModel listmodel = new DefaultComboBoxModel();
 			Opdrachtgever og = null;
 			for (Opdrachtgever o : UserInterface.getClients()) {
 				if (p != null && o.getID() == p.getOpdrachtgeverId()) {
 					og = o;
 				}
 				listmodel.addElement(o);
 			}
 			listmodel.addElement(NEWCLIENTITEM);
 			box.setModel(listmodel);
 			box.setSelectedItem(og);
 		}
 	}
 
 	// Refresh import (/ export?) tree view
 	private void refreshTreeView(JTree tree, ArrayList projects) {
 		//TreeNode root = new TreeNode();
 		//DefaultTreeModel treeModel = new DefaultTreeModel(, true)
 
 		Object rootNodes[] = new Object[projects.size()];
 
 		Vector projectVector = null;
 		CheckBoxNode projectOptions[] = new CheckBoxNode[projects.size()];
 
 		for (int i = 0; i < projects.size(); i++) {
 			Project p = ((ArrayList<Project>) projects).get(i);
 			projectOptions[i] = new CheckBoxNode(p.getNaam(), false);
 
 			CheckBoxNode taskOptions[] = new CheckBoxNode[p.getTaken().size()];
 			for (int j = 0; j < p.getTaken().size(); j++) {
 				Taak t = p.getTaken().get(j);
 				taskOptions[j] = new CheckBoxNode(t.getNaam(), false);
 			}
 
 			projectVector = new NamedVector(p.getNaam(), taskOptions);
 			rootNodes[i] = projectVector;
 		}
 
 		Vector rootVector = new NamedVector("Projects", rootNodes);
 		tree = new JTree(rootVector);
 
 		CheckBoxNodeRenderer renderer = new CheckBoxNodeRenderer();
 		tree.setCellRenderer(renderer);
 
 		tree.setCellEditor(new CheckBoxNodeEditor(tree));
 		tree.setEditable(true);
 	}
 
 	// ================================================================================
 	// Loading methods
 	// ================================================================================
 
 	// Load info from PROJECT with index parameter
 	private void loadProjectInfo(int index) {
 		if (index != -1) {
 			Project p = UserInterface.getProject(index);
 			projectNameJTextField.setText(p.getNaam());
 			projectStartDateChooser.setDate(new Date(p.getBegindatum() * 1000));
 			projectEndDateChooser.setDate(new Date(p.getEinddatum() * 1000));
 
 			refreshClientsComboBox(p, projectClientsJComboBox);
 			refreshTasksList(p, projectTasksJList);
 			percentageCompleteJProgressBar.setValue((int) (((Project) projectsJList.getSelectedValue()).getPercentageComplete() * 100));
 		}
 	}
 
 	// Load info from TASK with index parameter
 	private void loadTaskInfo(int index) throws GUIException {
 		if (index != -1) {
 			Taak t = (Taak) tasksJList.getSelectedValue();
 			taskNameJTextField.setText(t.getNaam());
 			taskStartDateChooser.setDate(new Date(t.getBegindatum() * 1000));
 			taskEndDateChooser.setDate(new Date(t.getGeschatteEinddatum() * 1000));
 			taskCommentJTextArea.setText(t.getCommentaar());
 			taskCompletedJCheckBox.setSelected(t.getCompleted());
 			DefaultListModel listmodel = new DefaultListModel();
 
 			for (Iterator<Tijdspanne> it = t.getTotaleTijd().iterator(); it.hasNext();) {
 				Tijdspanne ts = it.next();
 				if (!ts.isPauze()) {
 					listmodel.addElement(ts);
 				}
 			}
 
 			workedTimeJList.setModel(listmodel);
 			taskTotalWorkedJTextField.setText(Clock.longTimeToString(t.getTotaleWerktijd(), false));
 			taskTotalPauseJTextField.setText(Clock.longTimeToString(t.getTotalePauze(), false));
 		}
 	}
 
 	// Load info from CLIENT with index parameter
 	private void loadClientInfo(int index) {
 		if (index != -1) {
 			Opdrachtgever o = (Opdrachtgever) clientsJList.getSelectedValue();
 			clientNameJTextField.setText(o.getNaam());
 			clientFirstNameJTextField.setText(o.getVoornaam());
 			clientCompanyJTextField.setText(o.getBedrijfsnaam());
 			clientEmailJTextField.setText(o.getEmail());
 			clientPhoneNumberJTextField.setText(o.getTelefoonnummer());
 		}
 	}
 
 	// ================================================================================
 	// Other methods, FIXME nakijken
 	// ================================================================================
 
 	private void workClicked(java.awt.event.MouseEvent evt) {
 		try {
 			Project p = UserInterface.getCurrentProject();
 			if (!p.tasksAvailable()) {
 				throw new GUIException("Current project contains no available tasks");
 			}
 			setVisible(false);
 			WorkDialog work = new WorkDialog(this, true, validator);
 			work.setVisible(true);
 			setVisible(true);
 			loadTaskInfo(tasksJList.getSelectedIndex());
 			toggleButtonStates();
 		} catch (GUIException ex) {
 			ex.printStackTrace();
 			JOptionPane.showMessageDialog(this, ex.getMessage());
 		}
 	}
 
 	// ================================================================================
 	// GUI methods
 	// ================================================================================
 
 	// Clear all fields on the panels in parameter
 	private void clearFieldsOnPanel(JPanel panel) {
 		Component[] clientPanelComps = panel.getComponents();
 		for (Component c : clientPanelComps) {
 			if (c instanceof JPanel) {
 				clearFieldsOnPanel((JPanel) c);
 			} else if (c instanceof JTextField) {
 				((JTextField) c).setText(null);
 			} else if (c instanceof JList) {
 				((JList) c).setModel(new DefaultListModel());
 			} else if (c instanceof JTextArea) {
 				((JTextArea) c).setText(null);
 			} else if (c instanceof JCheckBox) {
 				((JCheckBox) c).setSelected(false);
 			} else if (c instanceof JDateChooser) {
 				((JDateChooser) c).setDate(null);
 			} else if (c instanceof JComboBox) {
 				((JComboBox) c).setModel(new DefaultComboBoxModel());
 			}
 		}
 	}
 
 	// FIXME methode maken met 2 parameters en overal waar deze aangeroepen wordt de juiste parameters meegeven
 	// Change button states (enabled / disabled) looking at selected indexes
 	private void toggleButtonStates() {
 		boolean taskSelected = tasksJList.getSelectedIndex() != -1;
 		boolean projectSelected = projectsJList.getSelectedIndex() != -1;
 		boolean newItemSelected = projectsJList.getSelectedIndex() != -1 && !projectsJList.getSelectedValue().equals(NEWPROJECTITEM);
 		boolean clientSelected = clientsJList.getSelectedIndex() != -1;
 		saveTaskJButton.setEnabled(taskSelected);
 		removeTaskJButton.setEnabled(taskSelected);
 		setCurrentProjectJButton.setEnabled(projectSelected && newItemSelected);
 		saveProjectJButton.setEnabled(projectSelected);
 		removeProjectJButton.setEnabled(projectSelected && newItemSelected);
 		saveClientJButton.setEnabled(clientSelected);
 		removeClientJButton.setEnabled(clientSelected);
 	}
 
 	// ================================================================================
 	// Event handlers, FIXME afsplitsen!
 	// ================================================================================
 
 	private void setCurrentProjectJButtonActionPerformed(java.awt.event.ActionEvent evt) {
 		setCurrentProjectGUI(projectsJList.getSelectedIndex());
 	}
 
 	private void setCurrentProjectGUI(int index) {
 		try {
 			UserInterface.setCurrentProjectIndex(index);
 			currentProjectJLabel.setText("Current project: " + UserInterface.getCurrentProject().getNaam());
 			saveTaskJButton.setText("Save to " + UserInterface.getCurrentProject().getNaam());
 			projectsJList.setSelectedIndex(index);
 			refreshProjectsList(projectsJList, homeProjectsJList);
 			refreshTasksList(UserInterface.getCurrentProject(), tasksJList);
 			clearFieldsOnPanel(taskFieldsJPanel);
 			selectNewItem(tasksJList);
 		} catch (GUIException ex) {
 			ex.printStackTrace();
 			JOptionPane.showMessageDialog(this, ex.getMessage());
 		}
 	}
 
 	private void clientsJListValueChanged(ListSelectionEvent arg0) {
 		if (clientsJList.getSelectedIndex() != -1) {
 			if (clientsJList.getSelectedValue().equals(NEWCLIENTITEM)) {
 				clearFieldsOnPanel(clientFieldsJPanel);
 				saveClientJButton.setText("Save [new]");
 			} else {
 				loadClientInfo(clientsJList.getSelectedIndex());
 				saveClientJButton.setText("Save");
 			}
 			toggleButtonStates();
 		}
 	}
 
 	private void projectsJListValueChanged(javax.swing.event.ListSelectionEvent evt) {
 		if (projectsJList.getSelectedIndex() != -1) {
 			if (projectsJList.getSelectedValue().equals(NEWPROJECTITEM)) {
 				clearFieldsOnPanel(projectEditFieldsJPanel);
 				refreshClientsComboBox(null, projectClientsJComboBox);
 				saveProjectJButton.setText("Save [new]");
 			} else {
 				loadProjectInfo(projectsJList.getSelectedIndex());
 				saveProjectJButton.setText("Save");
 			}
 			toggleButtonStates();
 		}
 	}
 
 	// Initialiseer statusvelden (linksboven) en project en client lijsten
 	private void guiOpened(java.awt.event.WindowEvent evt) {
 		ingelogdJLabel.setText(UserInterface.getUser().getVolledigeNaam());
 		ingelogdJLabel.setForeground(Color.GREEN);
 		refreshProjectsList(projectsJList, homeProjectsJList);
 		refreshClientsList(clientsJList);
 		selectNewItem(projectsJList, tasksJList, clientsJList);
 	}
 
 	// sets selected item to < new ... >
 	private void selectNewItem(JList... lists) {
 		for (JList list : lists) {
 			list.setSelectedIndex(list.getModel().getSize() - 1);
 			list.ensureIndexIsVisible(list.getSelectedIndex());
 		}
 		refreshTreeView(exportTree, UserInterface.getProjects());
 	}
 
 	// Event handlers for all the edit fields
 	private void editFieldsFocused(java.awt.event.FocusEvent evt) {
 		toggleButtonStates();
 	}
 
 	// FIXME, samenvoegen met andere listvaluechanged event handlers
 	private void homeProjectListValueChanged(ListSelectionEvent evt) {
 		if (homeProjectsJList.getSelectedIndex() != -1) {
 			refreshTasksList((Project) homeProjectsJList.getSelectedValue(), homeTasksJList);
 		}
 	}
 
 	private void exportButtonClicked(ActionEvent arg0) {
 		JFileChooser fileChooser = new JFileChooser();
 		fileChooser.showDialog(this, "Export");
 		ArrayList<Taak> toExport = new ArrayList();
 		Taak[] t = new Taak[toExport.size()];
 		for (Object o : exportTree.getSelectionPaths()) {
 			if (o instanceof Taak)
 				toExport.add((Taak) o);
 		}
 		try {
 			IcsExporteren.export(toExport.toArray(t), fileChooser.getSelectedFile().toPath().toString());
 		} catch (IOException | ValidationException e) {
 			e.printStackTrace();
 			JOptionPane.showMessageDialog(this, e.getMessage());
 		}
 	}
 
 	private void taskJListValueChanged(ListSelectionEvent arg0) {
 		if (tasksJList.getSelectedIndex() != -1) {
 			if (tasksJList.getSelectedValue().getClass().equals(String.class)) {
 				clearFieldsOnPanel(taskFieldsJPanel);
 				saveTaskJButton.setText("Save [new]");
 			} else {
 				try {
 					loadTaskInfo(((JList) arg0.getSource()).getSelectedIndex());
 					toggleButtonStates();
 				} catch (GUIException e) {
 					e.printStackTrace();
 					JOptionPane.showMessageDialog(this, e.getMessage());
 				} finally {
 					saveTaskJButton.setText("Save");
 				}
 			}
 		}
 	}
 
 	private void clientsJComboBoxValueChanged(ActionEvent arg0) {
 		JComboBox combobox = (JComboBox) arg0.getSource();
 		if (combobox.getSelectedIndex() != -1) {
 			if (combobox.getSelectedItem().getClass().equals(String.class)) {
 				creatingProject = true;
 				contentJTabbedPane.setSelectedIndex(3);
 				selectNewItem(clientsJList);
 			}
 		}
 	}
 
 	private void setCurrentProjectWithEnter(KeyEvent arg0) {
 		if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
 			setCurrentProjectGUI(((JList) arg0.getSource()).getSelectedIndex());
 		}
 	}
 
 	private javax.swing.JTabbedPane contentJTabbedPane;
 	private javax.swing.JLabel clientcompJLabel1;
 	private javax.swing.JLabel clientsJLabel;
 	private javax.swing.JPanel clientsJPanel;
 	private javax.swing.JLabel clockJLabel;
 	private javax.swing.JLabel currentProjectJLabel;
 	private javax.swing.JLabel enddatecompJLabel1;
 	private javax.swing.Box.Filler filler2;
 	private javax.swing.JPanel headerJPanel;
 	private javax.swing.JLabel homeJLabel;
 	private javax.swing.JPanel homeJPanel;
 	private javax.swing.JLabel ingelogdJLabel;
 	private javax.swing.JScrollPane jScrollPane1;
 	private javax.swing.JScrollPane jScrollPane3;
 	private javax.swing.JScrollPane jScrollPane5;
 	private javax.swing.JTextField projectNameJTextField;
 	private javax.swing.JLabel namecompJLabel;
 	private javax.swing.JPanel optionsJPanel;
 	private javax.swing.JProgressBar percentageCompleteJProgressBar;
 	private javax.swing.JLabel percentageCompletecompJLabel;
 	private javax.swing.JLabel projectsJLabel;
 	private javax.swing.JList projectsJList;
 	private javax.swing.JPanel projectsJPanel;
 	private javax.swing.JButton removeClientJButton;
 	private javax.swing.JButton removeProjectJButton;
 	private javax.swing.JButton removeTaskJButton;
 	private javax.swing.JButton saveProjectJButton;
 	private javax.swing.JLabel scheduleJLabel;
 	private javax.swing.JPanel scheduleJPanel;
 	private javax.swing.JButton setCurrentProjectJButton;
 	private javax.swing.JLabel settingsJLabel;
 	private javax.swing.JLabel startdatecompJLabel;
 	private javax.swing.JLabel tasksJLabel;
 	private javax.swing.JPanel tasksJPanel;
 	private javax.swing.JLabel taskscompJLabel;
 	private javax.swing.JLabel titleLabel;
 	private javax.swing.JButton workJButton;
 	private JTextField clientNameJTextField;
 	private JTextField clientFirstNameJTextField;
 	private JTextField clientCompanyJTextField;
 	private JTextField clientEmailJTextField;
 	private JTextField clientPhoneNumberJTextField;
 	private JPanel clientFieldsJPanel;
 	private JButton saveClientJButton;
 	private JPanel taskFieldsJPanel;
 	private JLabel label_5;
 	private JTextField taskNameJTextField;
 	private JLabel label_6;
 	private JLabel label_7;
 	private JCheckBox taskCompletedJCheckBox;
 	private JLabel label_8;
 	private JLabel label_9;
 	private JLabel lblWorked;
 	private JButton saveTaskJButton;
 	private JTextArea taskCommentJTextArea;
 	private JList workedTimeJList;
 	private JPanel projectEditFieldsJPanel;
 	private JList tasksJList;
 	private JList projectTasksJList;
 	private JDateChooser taskStartDateChooser;
 	private JDateChooser taskEndDateChooser;
 	private JDateChooser projectStartDateChooser;
 	private JDateChooser projectEndDateChooser;
 	private JPanel homeFieldsJPanel;
 	private JList homeProjectsJList;
 	private JLabel homeProjectsJLabel;
 	private JLabel homeTaskJLabel;
 	private JList homeTasksJList;
 	private JScrollPane scrollPane;
 	private JScrollPane scrollPane_1;
 	private JPanel importExportJPanel;
 	private JTabbedPane importExportTabbedPane;
 	private JPanel importJPanel;
 	private JPanel exportJPanel;
 	private JScrollPane importJScrollPane;
 	private JButton importJButton;
 	private JScrollPane exportJScrollPane;
 	private JButton exportJButton;
 	private JComboBox projectClientsJComboBox;
 	private JList clientsJList;
 	private JTextField taskTotalWorkedJTextField;
 	private JTextField taskTotalPauseJTextField;
 	private JLabel taskTotalWorkedJLabel;
 	private JLabel lblTotalPaused;
 	private JTree exportTree;
 	private JTree importTree;
 	private JPanel projectFieldsJPanel;
 }
