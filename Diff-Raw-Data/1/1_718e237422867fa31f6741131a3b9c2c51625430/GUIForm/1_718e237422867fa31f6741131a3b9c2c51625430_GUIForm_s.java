 package be.artesis.timelog.gui;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.net.MalformedURLException;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.DefaultListModel;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.JTree;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.SwingConstants;
 import javax.swing.UIManager;
 import javax.swing.border.BevelBorder;
 import javax.swing.border.LineBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreePath;
 
 import net.fortuna.ical4j.data.ParserException;
 import net.fortuna.ical4j.model.ValidationException;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import be.artesis.timelog.clock.Clock;
 import be.artesis.timelog.excel.Excel;
 import be.artesis.timelog.ics.IcsExporteren;
 import be.artesis.timelog.ics.IcsImporteren;
 import be.artesis.timelog.lokaleopslag.LocalDatabaseReader;
 import be.artesis.timelog.lokaleopslag.LocalDatabaseSynch;
 import be.artesis.timelog.lokaleopslag.LocalDatabaseWriter;
 import be.artesis.timelog.model.Validator;
 import be.artesis.timelog.model.WebserviceException;
 import be.artesis.timelog.secure.WinRegistry;
 import be.artesis.timelog.view.DataInputException;
 import be.artesis.timelog.view.Gebruiker;
 import be.artesis.timelog.view.Opdrachtgever;
 import be.artesis.timelog.view.Project;
 import be.artesis.timelog.view.Taak;
 import be.artesis.timelog.view.Tijdspanne;
 
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 import com.sun.javafx.scene.layout.region.Border;
 import com.toedter.calendar.JDateChooser;
 
 import eu.floraresearch.lablib.gui.checkboxtree.CheckboxTree;
 
 /**
  * @author Gilliam
  */
 @SuppressWarnings({ "unchecked", "rawtypes", "serial" })
 public class GUIForm extends JFrame {
 
 	// ================================================================================
 	// Properties
 	// ================================================================================
 
 	Validator validator;
 	boolean creatingProject;
 	ArrayList<Taak> importedTasks = new ArrayList<Taak>();
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
 		contentJTabbedPane.setForeground(Color.WHITE);
 		homeJPanel = new javax.swing.JPanel();
 		homeJLabel = new javax.swing.JLabel();
 		homeJLabel.setBounds(10, 11, 62, 16);
 		workJButton = new javax.swing.JButton();
 		workJButton.setToolTipText("Open a work dialog and start timing your work");
 		workJButton.setBounds(231, 11, 230, 64);
 		projectsJPanel = new javax.swing.JPanel();
 		projectsJLabel = new javax.swing.JLabel();
 		projectsJLabel.setBackground(new Color(70, 130, 180));
 		projectsJLabel.setBounds(10, 11, 204, 16);
 		jScrollPane1 = new javax.swing.JScrollPane();
 		jScrollPane1.setBounds(10, 40, 200, 330);
 		setCurrentProjectJButton = new javax.swing.JButton();
 		setCurrentProjectJButton.setBounds(10, 417, 200, 23);
 		filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
 		filler2.setBounds(220, 152, 0, 0);
 		removeProjectJButton = new javax.swing.JButton();
 		removeProjectJButton.setBounds(10, 380, 200, 23);
 		tasksJPanel = new javax.swing.JPanel();
 		tasksJLabel = new javax.swing.JLabel();
 		tasksJLabel.setBounds(10, 14, 204, 16);
 		jScrollPane3 = new javax.swing.JScrollPane();
 		jScrollPane3.setBounds(10, 40, 200, 360);
 		removeTaskJButton = new javax.swing.JButton();
 		removeTaskJButton.setBounds(10, 411, 200, 29);
 		optionsJPanel = new javax.swing.JPanel();
 		headerJPanel = new javax.swing.JPanel();
 		logoLabel = new javax.swing.JLabel();
 		ingelogdJLabel = new javax.swing.JLabel();
 		currentProjectJLabel = new javax.swing.JLabel();
 
 		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
 		setTitle("Time Management System");
 		setBackground(new java.awt.Color(51, 51, 51));
 		this.getContentPane().setBackground(new Color(70, 130, 180));
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
 
 			@Override
 			public void windowClosing(WindowEvent arg0) {
 				//sync();
 			}
 		});
 
 		contentJTabbedPane.setBackground(Color.DARK_GRAY);
 		contentJTabbedPane.setTabPlacement(javax.swing.JTabbedPane.LEFT);
 		contentJTabbedPane.setName("tabContainer");
 
 		homeJPanel.setBackground(new Color(211, 211, 211));
 		homeJPanel.setForeground(new java.awt.Color(65, 152, 134));
 
 		homeJLabel.setFont(new java.awt.Font("Tw Cen MT", 1, 14));
 		homeJLabel.setForeground(new java.awt.Color(255, 255, 255));
 		homeJLabel.setText("Home");
 
 		workJButton.setBackground(Color.DARK_GRAY);
 		workJButton.setBorder(new BevelBorder(BevelBorder.RAISED));
 		workJButton.setFont(new java.awt.Font("Tahoma", 1, 18));
 		workJButton.setForeground(new java.awt.Color(204, 204, 204));
 		workJButton.setText("Start working");
 		workJButton.addMouseListener(new java.awt.event.MouseAdapter() {
 			public void mouseClicked(java.awt.event.MouseEvent evt) {
 				openWorkDialog();
 			}
 
 			@Override
 			public void mouseEntered(MouseEvent e) {
 				workJButton.setBackground(Color.GRAY);
 			}
 
 			@Override
 			public void mouseExited(MouseEvent e) {
 				workJButton.setBackground(Color.DARK_GRAY);
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
 		homeProjectsJLabel.setForeground(Color.WHITE);
 		homeProjectsJLabel.setFont(new Font("Tw Cen MT", Font.BOLD, 14));
 		homeProjectsJLabel.setBounds(10, 11, 157, 16);
 		homeFieldsJPanel.add(homeProjectsJLabel);
 
 		homeTaskJLabel = new JLabel();
 		homeTaskJLabel.setText("Tasks");
 		homeTaskJLabel.setForeground(Color.WHITE);
 		homeTaskJLabel.setFont(new Font("Tw Cen MT", Font.BOLD, 14));
 		homeTaskJLabel.setBounds(177, 11, 159, 16);
 		homeFieldsJPanel.add(homeTaskJLabel);
 
 		scrollPane = new JScrollPane();
 		scrollPane.setBounds(11, 34, 155, 302);
 		homeFieldsJPanel.add(scrollPane);
 
 		homeProjectsJList = new JList();
 		homeProjectsJList.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				setCurrentProjectWithMouse(arg0);
 			}
 		});
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
 
 		projectsJPanel.setBackground(new Color(211, 211, 211));
 
 		projectsJLabel.setFont(new java.awt.Font("Tw Cen MT", 1, 14));
 		projectsJLabel.setForeground(Color.DARK_GRAY);
 		projectsJLabel.setText("Projects");
 
 		setCurrentProjectJButton.setText("Set as current project");
 		setCurrentProjectJButton.setEnabled(false);
 		setCurrentProjectJButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				setCurrentProjectGUI(projectsJList.getSelectedIndex());
 			}
 		});
 
 		removeProjectJButton.setText("Remove project");
 		removeProjectJButton.setEnabled(false);
 		removeProjectJButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				deleteProject((Project) projectsJList.getSelectedValue());
 			}
 		});
 
 		contentJTabbedPane.addTab("", new javax.swing.ImageIcon(getClass().getResource("/be/artesis/timelog/gui/icons/ProjectsNeonIcon.png")), projectsJPanel, "Projects");
 		projectsJPanel.setLayout(null);
 		projectsJPanel.add(removeProjectJButton);
 		projectsJPanel.add(setCurrentProjectJButton);
 		projectsJPanel.add(jScrollPane1);
 		projectsJList = new javax.swing.JList();
 		jScrollPane1.setViewportView(projectsJList);
 		projectsJList.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				JList list = (JList) arg0.getSource();
 				if (arg0.getClickCount() == 2) {
 					int index = list.locationToIndex(arg0.getPoint());
 					if (index != -1 && list.getSelectedValue().getClass().equals(Project.class)) {
 						setCurrentProjectGUI(index);
 					}
 				}
 			}
 		});
 		projectsJList.addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyPressed(KeyEvent arg0) {
 				setCurrentProjectWithEnter(arg0);
 			}
 		});
 
 		projectsJList.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
 
 		projectsJList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
 			public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
 				projectsJListValueChanged(evt);
 			}
 		});
 		projectsJPanel.add(filler2);
 		projectsJPanel.add(projectsJLabel);
 
 		projectFieldsJPanel = new JPanel();
 		projectFieldsJPanel.setBorder(null);
 		projectFieldsJPanel.setBackground(new Color(211, 211, 211));
 		projectFieldsJPanel.setBounds(224, 11, 461, 429);
 		projectsJPanel.add(projectFieldsJPanel);
 		projectFieldsJPanel.setLayout(null);
 
 		projectEditFieldsJPanel = new JPanel();
 		projectEditFieldsJPanel.setBounds(62, 0, 399, 175);
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
 
 		projectStatusFieldsJPanel = new JPanel();
 		projectStatusFieldsJPanel.setBackground(new Color(128, 128, 128));
 		projectStatusFieldsJPanel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
 		projectStatusFieldsJPanel.setBounds(62, 186, 399, 221);
 		projectFieldsJPanel.add(projectStatusFieldsJPanel);
 		projectStatusFieldsJPanel.setLayout(null);
 
 		projectTasksJList = new JList();
 		projectTasksJList.setBounds(113, 43, 215, 113);
 		projectStatusFieldsJPanel.add(projectTasksJList);
 		projectTasksJList.setEnabled(false);
 		projectTasksJList.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
 		projectTasksJList.setBackground(Color.LIGHT_GRAY);
 		taskscompJLabel = new javax.swing.JLabel();
 		taskscompJLabel.setBounds(10, 46, 93, 21);
 		projectStatusFieldsJPanel.add(taskscompJLabel);
 
 		taskscompJLabel.setForeground(new java.awt.Color(255, 255, 255));
 		taskscompJLabel.setText("Tasks");
 		percentageCompletecompJLabel = new javax.swing.JLabel();
 		percentageCompletecompJLabel.setBounds(10, 10, 92, 21);
 		projectStatusFieldsJPanel.add(percentageCompletecompJLabel);
 
 		percentageCompletecompJLabel.setForeground(new java.awt.Color(255, 255, 255));
 		percentageCompletecompJLabel.setText("Complete");
 		percentageCompleteJProgressBar = new javax.swing.JProgressBar();
 		percentageCompleteJProgressBar.setBounds(113, 11, 215, 21);
 		projectStatusFieldsJPanel.add(percentageCompleteJProgressBar);
 		percentageCompleteJProgressBar.setToolTipText("Displays the percentage of completion of the project");
 		percentageCompleteJProgressBar.setStringPainted(true);
 		saveProjectJButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				saveProjectButtonClicked();
 			}
 		});
 
 		tasksJPanel.setBackground(new Color(211, 211, 211));
 
 		tasksJLabel.setFont(new java.awt.Font("Tw Cen MT", 1, 14));
 		tasksJLabel.setForeground(Color.DARK_GRAY);
 		tasksJLabel.setText("Tasks");
 
 		removeTaskJButton.setText("Remove task");
 		removeTaskJButton.setEnabled(false);
 		removeTaskJButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				deleteTask((Taak) tasksJList.getSelectedValue());
 			}
 		});
 
 		contentJTabbedPane.addTab("", new javax.swing.ImageIcon(getClass().getResource("/be/artesis/timelog/gui/icons/TasksNeonIcon.png")), tasksJPanel, "Tasks");
 		tasksJPanel.setLayout(null);
 		tasksJPanel.add(tasksJLabel);
 		tasksJPanel.add(jScrollPane3);
 
 		tasksJList = new JList();
 		DefaultListModel tasksListmodel = new DefaultListModel();
 		tasksListmodel.addElement("Select a current project first!");
 		tasksJList.setModel(tasksListmodel);
 		jScrollPane3.add(tasksJList);
 
 		tasksJList.addListSelectionListener(new ListSelectionListener() {
 			public void valueChanged(ListSelectionEvent arg0) {
 				tasksJListValueChanged(arg0);
 			}
 		});
 		tasksJList.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
 
 		jScrollPane3.setViewportView(tasksJList);
 		tasksJPanel.add(removeTaskJButton);
 
 		taskFieldsJPanel = new JPanel();
 		taskFieldsJPanel.setBorder(null);
 		taskFieldsJPanel.setBackground(new Color(211, 211, 211));
 		taskFieldsJPanel.setBounds(284, 14, 401, 426);
 		tasksJPanel.add(taskFieldsJPanel);
 		taskFieldsJPanel.setLayout(null);
 
 		taskEditFieldsJPanel = new JPanel();
 		taskEditFieldsJPanel.setBounds(0, 0, 399, 245);
 		taskFieldsJPanel.add(taskEditFieldsJPanel);
 		taskEditFieldsJPanel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
 		taskEditFieldsJPanel.setBackground(Color.DARK_GRAY);
 		taskEditFieldsJPanel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.UNRELATED_GAP_COLSPEC, ColumnSpec.decode("56px"), FormFactory.UNRELATED_GAP_COLSPEC, ColumnSpec.decode("1px"), FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("110px:grow"), FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("55px"), ColumnSpec.decode("51px"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(27dlu;default)"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(15dlu;default)"), }, new RowSpec[] { FormFactory.UNRELATED_GAP_ROWSPEC, RowSpec.decode("20px"), FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("20px:grow"), FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("20px:grow"), RowSpec.decode("38px"), RowSpec.decode("51px"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.UNRELATED_GAP_ROWSPEC, RowSpec.decode("51px"), FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("37px"), FormFactory.UNRELATED_GAP_ROWSPEC, RowSpec.decode("23px"), FormFactory.UNRELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.UNRELATED_GAP_ROWSPEC, RowSpec.decode("41px"), }));
 
 		label_5 = new JLabel();
 		label_5.setText("Name");
 		label_5.setForeground(Color.WHITE);
 		taskEditFieldsJPanel.add(label_5, "2, 2, fill, center");
 
 		taskNameJTextField = new JTextField();
 		taskEditFieldsJPanel.add(taskNameJTextField, "6, 2, 7, 1, fill, top");
 
 		label_6 = new JLabel();
 		label_6.setText("Start date");
 		label_6.setForeground(Color.WHITE);
 		taskEditFieldsJPanel.add(label_6, "2, 4, fill, center");
 
 		taskStartDateChooser = new JDateChooser();
 		taskStartDateChooser.setDateFormatString("dd/MM/yyyy");
 		taskEditFieldsJPanel.add(taskStartDateChooser, "6, 4, fill, center");
 
 		label_7 = new JLabel();
 		label_7.setText("Completed");
 		label_7.setForeground(Color.WHITE);
 		taskEditFieldsJPanel.add(label_7, "9, 4, 2, 1, center, center");
 
 		taskCompletedJCheckBox = new JCheckBox();
 		taskCompletedJCheckBox.setBackground(Color.DARK_GRAY);
 		taskEditFieldsJPanel.add(taskCompletedJCheckBox, "12, 4, left, center");
 
 		label_8 = new JLabel();
 		label_8.setText("End date");
 		label_8.setForeground(Color.WHITE);
 		taskEditFieldsJPanel.add(label_8, "2, 6, fill, center");
 
 		taskEndDateChooser = new JDateChooser();
 		taskEndDateChooser.setDateFormatString("dd/MM/yyyy");
 		taskEditFieldsJPanel.add(taskEndDateChooser, "6, 6, fill, center");
 
 		label_9 = new JLabel();
 		label_9.setText("Comment");
 		label_9.setForeground(Color.WHITE);
 		taskEditFieldsJPanel.add(label_9, "2, 8, fill, top");
 
 		taskCommentJTextArea = new JTextArea();
 		taskEditFieldsJPanel.add(taskCommentJTextArea, "6, 8, 7, 1, fill, fill");
 
 		saveTaskJButton = new JButton();
 		saveTaskJButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				saveTaskButtonClicked();
 			}
 		});
 		saveTaskJButton.setText("Save");
 		taskEditFieldsJPanel.add(saveTaskJButton, "6, 12, 7, 1, fill, top");
 
 		taskStatusFieldsJPanel = new JPanel();
 		taskStatusFieldsJPanel.setBackground(new Color(128, 128, 128));
 		taskStatusFieldsJPanel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
 		taskStatusFieldsJPanel.setBounds(0, 256, 399, 170);
 		taskFieldsJPanel.add(taskStatusFieldsJPanel);
 		taskStatusFieldsJPanel.setLayout(null);
 
 		lblWorked = new JLabel();
 		lblWorked.setBounds(10, 12, 68, 14);
 		taskStatusFieldsJPanel.add(lblWorked);
 		lblWorked.setText("Worked");
 		lblWorked.setForeground(Color.WHITE);
 
 		workedTimeJList = new JList();
 		workedTimeJList.addListSelectionListener(new ListSelectionListener() {
 			public void valueChanged(ListSelectionEvent arg0) {
 				workedTimeJListValueChange(arg0);
 			}
 		});
 		workedTimeJList.setBounds(88, 11, 288, 87);
 		taskStatusFieldsJPanel.add(workedTimeJList);
 
 		taskTotalWorkedJLabel = new JLabel("Total worked");
 		taskTotalWorkedJLabel.setBounds(10, 112, 76, 14);
 		taskStatusFieldsJPanel.add(taskTotalWorkedJLabel);
 		taskTotalWorkedJLabel.setForeground(Color.WHITE);
 
 		taskTotalWorkedJTextField = new JTextField();
 		taskTotalWorkedJTextField.setBounds(88, 109, 135, 20);
 		taskStatusFieldsJPanel.add(taskTotalWorkedJTextField);
 		taskTotalWorkedJTextField.setEditable(false);
 		taskTotalWorkedJTextField.setColumns(10);
 
 		lblTotalPaused = new JLabel("Total paused");
 		lblTotalPaused.setBounds(10, 142, 76, 14);
 		taskStatusFieldsJPanel.add(lblTotalPaused);
 		lblTotalPaused.setForeground(Color.WHITE);
 
 		taskTotalPauseJTextField = new JTextField();
 		taskTotalPauseJTextField.setBounds(88, 139, 135, 20);
 		taskStatusFieldsJPanel.add(taskTotalPauseJTextField);
 		taskTotalPauseJTextField.setEditable(false);
 		taskTotalPauseJTextField.setColumns(10);
 
 		addTimeJButton = new JButton("Add time");
 		addTimeJButton.setEnabled(false);
 		addTimeJButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				openAddTimeDialog();
 			}
 		});
 		addTimeJButton.setBounds(258, 108, 118, 23);
 		taskStatusFieldsJPanel.add(addTimeJButton);
 
 		removeTimeButton = new JButton("Remove time");
 		removeTimeButton.setEnabled(false);
 		removeTimeButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				deleteTimeSpan();
 			}
 		});
 		removeTimeButton.setBounds(258, 138, 118, 23);
 		taskStatusFieldsJPanel.add(removeTimeButton);
 		clientsJPanel = new javax.swing.JPanel();
 		clientsJPanel.setForeground(new Color(211, 211, 211));
 		clientsJLabel = new javax.swing.JLabel();
 		jScrollPane5 = new javax.swing.JScrollPane();
 		removeClientJButton = new javax.swing.JButton();
 
 		clientsJPanel.setBackground(new Color(211, 211, 211));
 
 		clientsJLabel.setFont(new java.awt.Font("Tw Cen MT", 1, 14));
 		clientsJLabel.setForeground(Color.DARK_GRAY);
 		clientsJLabel.setText("Clients");
 
 		removeClientJButton.setText("Remove client");
 		removeClientJButton.setEnabled(false);
 		removeClientJButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				deleteClient((Opdrachtgever) clientsJList.getSelectedValue());
 			}
 		});
 
 		clientFieldsJPanel = new JPanel();
 		clientFieldsJPanel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
 		clientFieldsJPanel.setBackground(Color.DARK_GRAY);
 
 		javax.swing.GroupLayout clientsJPanelLayout = new javax.swing.GroupLayout(clientsJPanel);
 		clientsJPanelLayout.setHorizontalGroup(clientsJPanelLayout.createParallelGroup(Alignment.LEADING).addGroup(clientsJPanelLayout.createSequentialGroup().addContainerGap().addGroup(clientsJPanelLayout.createParallelGroup(Alignment.LEADING).addComponent(clientsJLabel).addGroup(clientsJPanelLayout.createSequentialGroup().addGroup(clientsJPanelLayout.createParallelGroup(Alignment.TRAILING, false).addComponent(removeClientJButton, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(jScrollPane5, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE)).addPreferredGap(ComponentPlacement.RELATED, 106, Short.MAX_VALUE).addComponent(clientFieldsJPanel, GroupLayout.PREFERRED_SIZE, 324, GroupLayout.PREFERRED_SIZE))).addContainerGap()));
 		clientsJPanelLayout.setVerticalGroup(clientsJPanelLayout.createParallelGroup(Alignment.TRAILING).addGroup(clientsJPanelLayout.createSequentialGroup().addGroup(clientsJPanelLayout.createParallelGroup(Alignment.LEADING).addGroup(clientsJPanelLayout.createSequentialGroup().addGap(33).addComponent(clientFieldsJPanel, GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE)).addGroup(clientsJPanelLayout.createSequentialGroup().addContainerGap().addComponent(clientsJLabel).addPreferredGap(ComponentPlacement.RELATED).addComponent(jScrollPane5, GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE))).addPreferredGap(ComponentPlacement.RELATED).addComponent(removeClientJButton, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE).addContainerGap()));
 
 		clientsJList = new JList();
 		clientsJList.setSelectedIndex(0);
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
 		clientFieldsJPanel.add(clientNameJTextField, "3, 2, fill, top");
 
 		JLabel label_1 = new JLabel();
 		label_1.setText("First name");
 		label_1.setForeground(Color.WHITE);
 		clientFieldsJPanel.add(label_1, "2, 4, fill, center");
 
 		clientFirstNameJTextField = new JTextField();
 		clientFieldsJPanel.add(clientFirstNameJTextField, "3, 4, fill, top");
 
 		JLabel label_2 = new JLabel();
 		label_2.setText("Company");
 		label_2.setForeground(Color.WHITE);
 		clientFieldsJPanel.add(label_2, "2, 6, fill, center");
 
 		clientCompanyJTextField = new JTextField();
 		clientFieldsJPanel.add(clientCompanyJTextField, "3, 6, fill, top");
 
 		JLabel label_3 = new JLabel();
 		label_3.setText("E-mail");
 		label_3.setForeground(Color.WHITE);
 		clientFieldsJPanel.add(label_3, "2, 8, fill, center");
 
 		clientEmailJTextField = new JTextField();
 		clientFieldsJPanel.add(clientEmailJTextField, "3, 8, fill, top");
 
 		JLabel lblPhone = new JLabel();
 		lblPhone.setText("Phone");
 		lblPhone.setForeground(Color.WHITE);
 		clientFieldsJPanel.add(lblPhone, "2, 10, fill, center");
 
 		clientPhoneNumberJTextField = new JTextField();
 		clientFieldsJPanel.add(clientPhoneNumberJTextField, "3, 10, fill, top");
 
 		saveClientJButton = new JButton();
 		saveClientJButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				saveClientButtonClicked();
 			}
 		});
 		saveClientJButton.setText("Save");
 		clientFieldsJPanel.add(saveClientJButton, "2, 14, 2, 1, fill, top");
 		clientsJPanel.setLayout(clientsJPanelLayout);
 
 		contentJTabbedPane.addTab("", new javax.swing.ImageIcon(getClass().getResource("/be/artesis/timelog/gui/icons/ClientsNeonIcon.png")), clientsJPanel, "Clients");
 
 		importExportJPanel = new JPanel();
 		importExportJPanel.setBackground(new Color(211, 211, 211));
 		contentJTabbedPane.addTab("", new ImageIcon(GUIForm.class.getResource("/be/artesis/timelog/gui/icons/ImportExportNeonIcon.png")), importExportJPanel, "Import / Export");
 		importExportJPanel.setLayout(null);
 
 		importExportTabbedPane = new JTabbedPane(SwingConstants.TOP);
 		importExportTabbedPane.setForeground(Color.WHITE);
 		importExportTabbedPane.setBorder(null);
 		importExportTabbedPane.setBackground(Color.WHITE);
 		importExportTabbedPane.setBounds(10, 11, 664, 429);
 		importExportJPanel.add(importExportTabbedPane);
 
 		exportJPanel = new JPanel();
 		exportJPanel.setBackground(Color.DARK_GRAY);
 		exportJPanel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
 		importExportTabbedPane.addTab("Export", null, exportJPanel, "Export your tasks here");
 		importExportTabbedPane.setForegroundAt(0, Color.WHITE);
 		exportJPanel.setLayout(null);
 		exportJScrollPane = new JScrollPane();
 		exportJScrollPane.setBounds(10, 11, 260, 379);
 		exportJPanel.add(exportJScrollPane);
 
 		exportJCheckBoxTree = new CheckboxTree();
 		exportJCheckBoxTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("Projects") {
 			{
 			}
 		}));
 		exportJScrollPane.setViewportView(exportJCheckBoxTree);
 
 		exportJButton = new JButton("Export");
 		exportJButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				exportTasks();
 			}
 		});
 		exportJButton.setBounds(550, 11, 99, 23);
 		exportJPanel.add(exportJButton);
 
 		toExportProjectJComboBox = new JComboBox();
 		toExportProjectJComboBox.setBounds(419, 370, 230, 20);
 		exportJPanel.add(toExportProjectJComboBox);
 
 		exportToExcelJButton = new JButton("Export project to Excel");
 		exportToExcelJButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				exportProjectToExcel();
 			}
 		});
 		exportToExcelJButton.setBounds(419, 336, 230, 23);
 		exportJPanel.add(exportToExcelJButton);
 
 		importExportTabbedPane.setBackgroundAt(0, new Color(70, 130, 180));
 
 		importJPanel = new JPanel();
 		importJPanel.setBackground(Color.DARK_GRAY);
 		importJPanel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
 		importExportTabbedPane.addTab("Import", null, importJPanel, "Import tasks here");
 		importExportTabbedPane.setForegroundAt(1, Color.WHITE);
 		importExportTabbedPane.setBackgroundAt(1, new Color(70, 130, 180));
 		importJPanel.setLayout(null);
 		importProjectsJScrollPane = new JScrollPane();
 		importProjectsJScrollPane.setBounds(10, 11, 260, 379);
 		importJPanel.add(importProjectsJScrollPane);
 
 		importJCheckBoxTree = new CheckboxTree();
 		importJCheckBoxTree.setRootVisible(false);
 		importJCheckBoxTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("Projects") {
 			{
 			}
 		}));
 		importProjectsJScrollPane.setViewportView(importJCheckBoxTree);
 
 		importJButton = new JButton("Import");
 		importJButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				importTasks();
 			}
 		});
 		importJButton.setBounds(550, 11, 99, 23);
 		importJPanel.add(importJButton);
 
 		projectsJComboBox = new JComboBox();
 		projectsJComboBox.setEnabled(false);
 		projectsJComboBox.setBounds(280, 367, 181, 23);
 		importJPanel.add(projectsJComboBox);
 
 		importToProjectJButton = new JButton("Save to");
 		importToProjectJButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				saveToProjectButtonClicked(arg0);
 			}
 		});
 		importToProjectJButton.setEnabled(false);
 		importToProjectJButton.setBounds(280, 333, 181, 23);
 		importJPanel.add(importToProjectJButton);
 
 		optionsJPanel.setBackground(new Color(211, 211, 211));
 
 		settingsJTabbedPane = new JTabbedPane(JTabbedPane.TOP);
 		settingsJTabbedPane.setBackground(Color.GRAY);
 
 		javax.swing.GroupLayout optionsJPanelLayout = new javax.swing.GroupLayout(optionsJPanel);
 		optionsJPanelLayout.setHorizontalGroup(optionsJPanelLayout.createParallelGroup(Alignment.LEADING).addGroup(optionsJPanelLayout.createSequentialGroup().addContainerGap().addComponent(settingsJTabbedPane, GroupLayout.PREFERRED_SIZE, 667, GroupLayout.PREFERRED_SIZE).addContainerGap(18, Short.MAX_VALUE)));
 		optionsJPanelLayout.setVerticalGroup(optionsJPanelLayout.createParallelGroup(Alignment.LEADING).addGroup(optionsJPanelLayout.createSequentialGroup().addContainerGap().addComponent(settingsJTabbedPane, GroupLayout.PREFERRED_SIZE, 425, GroupLayout.PREFERRED_SIZE).addContainerGap(15, Short.MAX_VALUE)));
 
 		userSettingsJPanel = new JPanel();
 		userSettingsJPanel.setBackground(Color.DARK_GRAY);
 		settingsJTabbedPane.addTab("User Settings", null, userSettingsJPanel, null);
 		settingsJTabbedPane.setBackgroundAt(0, new Color(70, 130, 180));
 		settingsJTabbedPane.setForegroundAt(0, Color.WHITE);
 		userSettingsJPanel.setLayout(null);
 
 		firstNameJTextField = new JTextField();
 		firstNameJTextField.setBounds(115, 84, 152, 20);
 		userSettingsJPanel.add(firstNameJTextField);
 		firstNameJTextField.setColumns(10);
 
 		lastNameJTextField = new JTextField();
 		lastNameJTextField.setBounds(115, 115, 152, 20);
 		userSettingsJPanel.add(lastNameJTextField);
 		lastNameJTextField.setColumns(10);
 
 		emailJTextField = new JTextField();
 		emailJTextField.setBounds(115, 147, 152, 20);
 		userSettingsJPanel.add(emailJTextField);
 		emailJTextField.setColumns(10);
 
 		firstNameJLabel = new JLabel("First name");
 		firstNameJLabel.setForeground(Color.WHITE);
 		firstNameJLabel.setBounds(36, 87, 69, 14);
 		userSettingsJPanel.add(firstNameJLabel);
 
 		lastNameJLabel = new JLabel("Last name");
 		lastNameJLabel.setForeground(Color.WHITE);
 		lastNameJLabel.setBounds(36, 118, 69, 14);
 		userSettingsJPanel.add(lastNameJLabel);
 
 		emailJLabel = new JLabel("Email");
 		emailJLabel.setForeground(Color.WHITE);
 		emailJLabel.setBounds(36, 150, 69, 14);
 		userSettingsJPanel.add(emailJLabel);
 
 		addressJTextField = new JTextField();
 		addressJTextField.setBounds(115, 178, 152, 20);
 		userSettingsJPanel.add(addressJTextField);
 		addressJTextField.setColumns(10);
 
 		addressJLabel = new JLabel("Address");
 		addressJLabel.setForeground(Color.WHITE);
 		addressJLabel.setBounds(36, 181, 69, 14);
 		userSettingsJPanel.add(addressJLabel);
 
 		telephoneJTextField = new JTextField();
 		telephoneJTextField.setBounds(115, 209, 152, 20);
 		userSettingsJPanel.add(telephoneJTextField);
 		telephoneJTextField.setColumns(10);
 
 		lblTelephone = new JLabel("Telephone");
 		lblTelephone.setForeground(Color.WHITE);
 		lblTelephone.setBounds(36, 212, 69, 14);
 		userSettingsJPanel.add(lblTelephone);
 
 		personalInfoJLabel = new JLabel("Personal info");
 		personalInfoJLabel.setForeground(Color.WHITE);
 		personalInfoJLabel.setFont(new Font("Tahoma", Font.BOLD, 13));
 		personalInfoJLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		personalInfoJLabel.setBounds(115, 11, 152, 31);
 		userSettingsJPanel.add(personalInfoJLabel);
 
 		vatJTextField = new JTextField();
 		vatJTextField.setBounds(420, 84, 171, 20);
 		userSettingsJPanel.add(vatJTextField);
 		vatJTextField.setColumns(10);
 
 		vatJLabel = new JLabel("VAT number");
 		vatJLabel.setForeground(Color.WHITE);
 		vatJLabel.setBounds(330, 87, 80, 14);
 		userSettingsJPanel.add(vatJLabel);
 
 		ibanJLabel = new JLabel("IBAN");
 		ibanJLabel.setForeground(Color.WHITE);
 		ibanJLabel.setBounds(330, 118, 80, 14);
 		userSettingsJPanel.add(ibanJLabel);
 
 		ibanJTextField = new JTextField();
 		ibanJTextField.setBounds(420, 115, 171, 20);
 		userSettingsJPanel.add(ibanJTextField);
 		ibanJTextField.setColumns(10);
 
 		bicJTextField = new JTextField();
 		bicJTextField.setBounds(420, 147, 171, 20);
 		userSettingsJPanel.add(bicJTextField);
 		bicJTextField.setColumns(10);
 
 		bicJLabel = new JLabel("BIC");
 		bicJLabel.setForeground(Color.WHITE);
 		bicJLabel.setBounds(330, 150, 80, 14);
 		userSettingsJPanel.add(bicJLabel);
 
 		businessInfoJLabel = new JLabel("Business info");
 		businessInfoJLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		businessInfoJLabel.setForeground(Color.WHITE);
 		businessInfoJLabel.setFont(new Font("Tahoma", Font.BOLD, 13));
 		businessInfoJLabel.setBounds(420, 11, 171, 31);
 		userSettingsJPanel.add(businessInfoJLabel);
 
 		updateUserJButton = new JButton("Save");
 		updateUserJButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				String firstName = firstNameJTextField.getText();
 				String lastName = lastNameJTextField.getText();
 				String email = emailJTextField.getText();
 				updateUser(firstName, lastName, email);
 			}
 		});
 		updateUserJButton.setBounds(115, 297, 116, 23);
 		userSettingsJPanel.add(updateUserJButton);
 
 		usernameJTextField = new JTextField();
 		usernameJTextField.setEditable(false);
 		usernameJTextField.setBounds(115, 53, 152, 20);
 		userSettingsJPanel.add(usernameJTextField);
 		usernameJTextField.setColumns(10);
 
 		usernameJLabel = new JLabel("Username");
 		usernameJLabel.setForeground(Color.WHITE);
 		usernameJLabel.setBounds(36, 53, 69, 14);
 		userSettingsJPanel.add(usernameJLabel);
 
 		companyNameJLabel = new JLabel("Company name");
 		companyNameJLabel.setForeground(Color.WHITE);
 		companyNameJLabel.setBounds(330, 56, 91, 14);
 		userSettingsJPanel.add(companyNameJLabel);
 
 		companyNameJTextField = new JTextField();
 		companyNameJTextField.setColumns(10);
 		companyNameJTextField.setBounds(420, 53, 171, 20);
 		userSettingsJPanel.add(companyNameJTextField);
 		optionsJPanel.setLayout(optionsJPanelLayout);
 
 		contentJTabbedPane.addTab("", new javax.swing.ImageIcon(getClass().getResource("/be/artesis/timelog/gui/icons/SettingsNeonIcon.png")), optionsJPanel, "Settings");
 
 		headerJPanel.setBackground(new Color(70, 130, 180));
 
 		// FIXME zorg dat icon effectief gedisplayed wordt
 		logoLabel.setFont(new java.awt.Font("Tempus Sans ITC", 1, 18));
 		ImageIcon ii = new ImageIcon("/be/artesis/timelog/gui/icons/logo.png");
 		Image image = ii.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
 		ImageIcon icon = new ImageIcon(image);
 		logoLabel.setIcon(icon);
 		logoLabel.setForeground(new java.awt.Color(255, 255, 255));
 
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
 
 		logoutJButton = new JButton("Logout");
 		logoutJButton.setToolTipText("Log yourself out so you can log in with another account");
 		logoutJButton.setForeground(Color.DARK_GRAY);
 		logoutJButton.setBackground(UIManager.getColor("Button.background"));
 		logoutJButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				logout();
 			}
 		});
 
 		syncButton = new JButton("Sync");
 		syncButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				File file = new File(LocalDatabaseWriter.URL);
 				if (file.isDirectory()) {
 					if (file.list().length == 0) {
 						showGUIMessage("There is no new localdata te synchronise", false);
 					} else {
 						sync();
 						showGUIMessage("Synchronisation succesfull", false);
 					}
 
 				} else {
 					showGUIMessage("the hardcoded url in the class localdatabasewriter is(no longer) a directory, contact the developer", true);
 					//geen directory (wat niet zou mogen kunnen)
 				}
 			}
 		});
 
 		errorJLabel = new JLabel();
 		errorJLabel.setOpaque(true);
 		errorJLabel.setName("");
 		errorJLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		errorJLabel.setForeground(new Color(0, 204, 204));
 		errorJLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
 		errorJLabel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
 		errorJLabel.setBackground(Color.WHITE);
 
 		javax.swing.GroupLayout headerJPanelLayout = new javax.swing.GroupLayout(headerJPanel);
 		headerJPanelLayout.setHorizontalGroup(headerJPanelLayout.createParallelGroup(Alignment.TRAILING).addGroup(headerJPanelLayout.createSequentialGroup().addContainerGap().addGroup(headerJPanelLayout.createParallelGroup(Alignment.LEADING, false).addComponent(currentProjectJLabel, GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE).addComponent(ingelogdJLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addPreferredGap(ComponentPlacement.RELATED).addGroup(headerJPanelLayout.createParallelGroup(Alignment.LEADING).addGroup(headerJPanelLayout.createSequentialGroup().addComponent(logoutJButton).addPreferredGap(ComponentPlacement.RELATED, 215, Short.MAX_VALUE).addComponent(syncButton, GroupLayout.PREFERRED_SIZE, 68, GroupLayout.PREFERRED_SIZE)).addComponent(errorJLabel, GroupLayout.DEFAULT_SIZE, 348, Short.MAX_VALUE)).addPreferredGap(ComponentPlacement.UNRELATED).addComponent(logoLabel, GroupLayout.PREFERRED_SIZE, 187, GroupLayout.PREFERRED_SIZE).addContainerGap()));
 		headerJPanelLayout.setVerticalGroup(headerJPanelLayout.createParallelGroup(Alignment.LEADING).addGroup(headerJPanelLayout.createSequentialGroup().addContainerGap().addGroup(headerJPanelLayout.createParallelGroup(Alignment.LEADING).addComponent(logoLabel, GroupLayout.DEFAULT_SIZE, 64, Short.MAX_VALUE).addGroup(headerJPanelLayout.createSequentialGroup().addGroup(headerJPanelLayout.createParallelGroup(Alignment.BASELINE).addComponent(ingelogdJLabel).addComponent(logoutJButton, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE).addComponent(syncButton)).addPreferredGap(ComponentPlacement.UNRELATED).addGroup(headerJPanelLayout.createParallelGroup(Alignment.LEADING).addComponent(errorJLabel, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE).addComponent(currentProjectJLabel)))).addContainerGap()));
 		headerJPanel.setLayout(headerJPanelLayout);
 
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
 
 	// ================================================================================
 	// Save / Edit methods
 	// ================================================================================
 
 	/**
 	 * Update USER info
 	 * @param 	firstName	first name of the user to be updated
 	 * @param	lastName	last name of the user to be updated
 	 * @param	email		email of the user to be updated
 	 */
 
 	//FIXME resterende info updaten
 	private void updateUser(String firstName, String lastName, String email) {
 		try {
 			UserInterface.updateUser(firstName, lastName, email);
 			showGUIMessage("User information updated!", false);
 			loadUserInfo();
 		} catch (DataInputException | IOException | WebserviceException e) {
 			e.printStackTrace();
 			showGUIMessage(e.getMessage(), true);
 		}
 	}
 
 	/**
 	 * Create new PROJECT
 	 * @param 	name		name of new project
 	 * @param	startdate	startdate of new project
 	 * @param	enddate 	enddate of new project
 	 * @param 	opdrachtgeverID	ID of client to link to the new project
 	 */
 	private void createProject(String name, long startdate, long enddate, int opdrachtgeverID) {
 		try {
 			UserInterface.createProject(name, startdate, enddate, opdrachtgeverID);
 			showGUIMessage("Project added!", false);
 			refreshProjectsList(projectsJList, homeProjectsJList);
 		} catch (DataInputException | ParseException | IOException | WebserviceException | JSONException e) {
 			e.printStackTrace();
 			showGUIMessage(e.getMessage(), true);
 		}
 	}
 
 	/**
 	 * Update existing PROJECT
 	 * @param 	name		name of to update project
 	 * @param	startdate	startdate of to update project
 	 * @param	enddate 	enddate of to update project
 	 * @param 	opdrachtgeverID	ID of client to link to the to update project
 	 */
 	private void updateProject(String name, long startdate, long enddate, int opdrachtgeverID) {
 		try {
 			UserInterface.updateProject(projectsJList.getSelectedIndex(), name, startdate, enddate, opdrachtgeverID);
 			showGUIMessage("Project edited!", false);
 			refreshProjectsList(projectsJList, homeProjectsJList);
 		} catch (DataInputException | IOException | WebserviceException | ParseException | JSONException e) {
 			e.printStackTrace();
 			showGUIMessage(e.getMessage(), true);
 		}
 	}
 
 	/**
 	 * Create new TASK
 	 * @param 	name		name of new task
 	 * @param	startdate	startdate of new task
 	 * @param	enddate 	enddate of new task
 	 * @param 	comment		ID of client to link to the new task
 	 * @param	completed	shows if the new task is already completed (false is logical)
 	 */
 	private void createTask(String name, long startdate, long enddate, String comment, boolean completed) {
 		try {
 			UserInterface.createTask(name, startdate, enddate, comment, completed, UserInterface.getCurrentProject().getId());
 			showGUIMessage("Task added!", false);
 			refreshTasksList(UserInterface.getCurrentProject(), tasksJList);
 		} catch (DataInputException | ParseException | GUIException | IOException | WebserviceException | JSONException e) {
 			e.printStackTrace();
 			showGUIMessage(e.getMessage(), true);
 		}
 	}
 
 	/**
 	 * Update existing TASK
 	 * @param 	name		name of to update task
 	 * @param	startdate	startdate of to update task
 	 * @param	enddate 	enddate of to update task
 	 * @param 	comment		contents of the comment field
 	 * @param	completed	shows if the task is completed
 	 * @param projectId 
 	 */
 	private void updateTask(String name, long startdate, long enddate, String comment, boolean completed, int projectId) {
 		try {
 			UserInterface.updateTask(tasksJList.getSelectedIndex(), name, startdate, enddate, comment, completed, projectId);
 			showGUIMessage("Task edited!", false);
 			refreshTasksList(UserInterface.getCurrentProject(), tasksJList);
 		} catch (GUIException | DataInputException | ParseException | IOException | WebserviceException | JSONException e) {
 			e.printStackTrace();
 			showGUIMessage(e.getMessage(), true);
 		}
 	}
 
 	/**
 	 * Create new CLIENT
 	 * @param 	name		name of new client
 	 * @param	firstName	first name of new client
 	 * @param	companyName	company of new client
 	 * @param 	email		email address from the new client
 	 * @param	phoneNumber	phone number of new client
 	 */
 	private void createClient(String naam, String voornaam, String bedrijfsnaam, String email, String telefoonnummer) {
 		Opdrachtgever o = null;
 		try {
 			o = UserInterface.createClient(naam, voornaam, bedrijfsnaam, email, telefoonnummer);
 			showGUIMessage("Client added!", false);
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
 			e.printStackTrace();
 			showGUIMessage(e.getMessage(), true);
 		}
 	}
 
 	/**
 	 * Update existing CLIENT
 	 * @param 	name		name of to update client
 	 * @param	firstName	first name of to update client
 	 * @param	companyName	company of to update client
 	 * @param 	email		email address from the to update client
 	 * @param	phoneNumber	phone number of to update client
 	 */
 	private void updateClient(String naam, String voornaam, String bedrijfsnaam, String email, String telefoonnummer) {
 		try {
 			UserInterface.updateClient(((Opdrachtgever) clientsJList.getSelectedValue()).getID(), naam, voornaam, bedrijfsnaam, email, telefoonnummer);
 			showGUIMessage("Client edited!", false);
 			refreshClientsList(clientsJList);
 		} catch (DataInputException | IOException | WebserviceException | JSONException e) {
 			e.printStackTrace();
 			showGUIMessage(e.getMessage(), true);
 		}
 	}
 
 	// ================================================================================
 	// Remove methods
 	// ================================================================================
 
 	/**
 	 * Remove a PROJECT
 	 * @param 	project	the project that's going to be removed if possible
 	 */
 	private void deleteProject(Project project) {
 		try {
 			if (UserInterface.getCurrentProjectIndex() != -1 && project == UserInterface.getCurrentProject()) {
 				showGUIMessage("Can't remove current project", true);
 			} else {
 				int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this project?", null, JOptionPane.YES_NO_OPTION);
 				if (result == JOptionPane.YES_OPTION) {
 					try {
 						UserInterface.deleteProject(project);
 						refreshProjectsList(projectsJList, homeProjectsJList);
 					} catch (IOException | WebserviceException | JSONException ex) {
 						ex.printStackTrace();
 						showGUIMessage(ex.getMessage(), true);
 					} finally {
 						clearFieldsOnPanel(projectFieldsJPanel);
 						selectNewItem(projectsJList, tasksJList);
 					}
 				}
 			}
 		} catch (GUIException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Remove a TASK
 	 * @param 	task	the task that's going to be removed
 	 */
 	private void deleteTask(Taak task) {
 		int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this task?", null, JOptionPane.YES_NO_OPTION);
 		if (result == JOptionPane.YES_OPTION) {
 			try {
 				UserInterface.deleteTask(task);
 				refreshTasksList(UserInterface.getCurrentProject(), tasksJList, projectTasksJList, homeTasksJList);
 				selectNewItem(tasksJList);
 				showGUIMessage("Task removed!", false);
 			} catch (GUIException | IOException | WebserviceException | JSONException ex) {
 				ex.printStackTrace();
 				showGUIMessage(ex.getMessage(), true);
 			} finally {
 				clearFieldsOnPanel(taskFieldsJPanel);
 			}
 		}
 	}
 
 	/**
 	 * Remove a CLIENT
 	 * @param 	client	the client that's going to be removed
 	 */
 	private void deleteClient(Opdrachtgever client) {
 		int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this client?", null, JOptionPane.YES_NO_OPTION);
 		if (result == JOptionPane.YES_OPTION) {
 			try {
 				UserInterface.deleteClient(client);
 				refreshClientsList(clientsJList);
 				clearFieldsOnPanel(clientFieldsJPanel);
 				selectNewItem(clientsJList);
 				showGUIMessage("Client removed!", true);
 			} catch (GUIException | IOException | WebserviceException | JSONException ex) {
 				ex.printStackTrace();
 				showGUIMessage(ex.getMessage(), false);
 			}
 		}
 	}
 
 	private void deleteTimeSpan() {
 		Taak t = (Taak) tasksJList.getSelectedValue();
 		int result = JOptionPane.showConfirmDialog(this, "Are you sure?", "Removing timespan", JOptionPane.YES_NO_OPTION);
 		if (result == JOptionPane.YES_OPTION) {
 			try {
 				UserInterface.deleteTimespan((Tijdspanne) workedTimeJList.getSelectedValue(), t);
 				refreshWorkedTime(t, workedTimeJList);
 				showGUIMessage("Timespan removed", false);
 			} catch (IOException | WebserviceException | JSONException e) {
 				showGUIMessage(e.getMessage(), true);
 				e.printStackTrace();
 			}
 		}
 	}
 
 	// ================================================================================
 	// Refresh methods
 	// ================================================================================
 
 	/**
 	 * Refresh all PROJECT lists
 	 * @param 	lists	the project lists that will be reloaded
 	 */
 	private void refreshProjectsList(JList... lists) {
 		for (JList list : lists) {
 			int selectedIndex = list.getSelectedIndex();
 			DefaultListModel listmodel = new DefaultListModel();
 			if (!UserInterface.getProjects().isEmpty()) {
 				for (Iterator<Project> it = UserInterface.getProjects().iterator(); it.hasNext();) {
 					Project p = it.next();
 					listmodel.addElement(p);
 				}
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
 		refreshTree(exportJCheckBoxTree, UserInterface.getProjects());
 		refreshProjectsComboBox(projectsJComboBox, toExportProjectJComboBox);
 	}
 
 	/**
 	 * Refresh all PROJECT comboboxes
 	 * @param 	boxes	the project comboboxes that will be reloaded
 	 */
 	private void refreshProjectsComboBox(JComboBox... boxes) {
 		for (JComboBox box : boxes) {
 			DefaultComboBoxModel listmodel = new DefaultComboBoxModel();
 			for (Project p : UserInterface.getProjects()) {
 				listmodel.addElement(p);
 			}
 			box.setModel(listmodel);
 		}
 	}
 
 	/**
 	 * Refresh all TASK lists
 	 * @param 	p		the project wherefrom tasks should be gotten	
 	 * @param 	lists	the task lists that will be reloaded
 	 */
 	private void refreshTasksList(Project p, JList... lists) {
 		for (JList list : lists) {
 
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
 
 			selectNewItem(tasksJList);
 		}
 		refreshTree(exportJCheckBoxTree, UserInterface.getProjects());
 	}
 
 	/**
 	 * Refresh all CLIENT lists
 	 * @param 	lists	the client lists that will be reloaded
 	 */
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
 
 	/**
 	 * Refresh all CLIENT comboboxes
 	 * @param 	boxes	the client comboboxes that will be reloaded
 	 */
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
 
 	/**
 	 * Refresh IMPORT & EXPORT trees
 	 * @param 	tree		the tree that should be filled with projects and tasks
 	 * @param	projects	the projects that should be loaded in the tree
 	 */
 	private void refreshTree(JTree tree, ArrayList<Project> projects) {
 		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Project");
 		for (Project p : projects) {
 			if (p != null && !p.getTaken().isEmpty()) {
 				DefaultMutableTreeNode project = new DefaultMutableTreeNode(p);
 				for (Taak t : p.getTaken()) {
 					project.add(new DefaultMutableTreeNode(t));
 				}
 				root.add(project);
 			}
 		}
 		DefaultTreeModel treeModel = new DefaultTreeModel(root);
 		tree.setModel(treeModel);
 	}
 
 	private void refreshWorkedTime(Taak t, JList... lists) {
 		for (JList list : lists) {
 			DefaultListModel listmodel = new DefaultListModel();
 			for (Iterator<Tijdspanne> it = t.getTotaleTijd().iterator(); it.hasNext();) {
 				Tijdspanne ts = it.next();
 				if (!ts.isPauze()) {
 					listmodel.addElement(ts);
 				}
 			}
 			list.setModel(listmodel);
 		}
 	}
 
 	// ================================================================================
 	// Loading info into GUI methods
 	// ================================================================================
 
 	/**
 	 * Load USER info
 	 */
 	private void loadUserInfo() {
 		Gebruiker u = UserInterface.getUser();
 		usernameJTextField.setText(u.getGebruikersnaam());
 		firstNameJTextField.setText(u.getVoornaam());
 		lastNameJTextField.setText(u.getNaam());
 		emailJTextField.setText(u.getEmail());
 		emailJTextField.setCaretPosition(0);
 		ingelogdJLabel.setText(UserInterface.getUser().getVolledigeNaam());
 		ingelogdJLabel.setForeground(Color.GREEN);
 	}
 
 	/**
 	 * Load info from PROJECT
 	 * @param	index	the list index (should equal arraylist index from projects) from the project to be loaded
 	 */
 	private void loadProjectInfo(int index) {
 		Project p = UserInterface.getProject(index);
 		projectNameJTextField.setText(p.getNaam());
 		projectStartDateChooser.setDate(new Date(p.getBegindatum() * 1000));
 		projectEndDateChooser.setDate(new Date(p.getEinddatum() * 1000));
 
 		refreshClientsComboBox(p, projectClientsJComboBox);
 		refreshTasksList(p, projectTasksJList);
 		percentageCompleteJProgressBar.setValue((int) (((Project) projectsJList.getSelectedValue()).getPercentageComplete() * 100));
 	}
 
 	/**
 	 * Load info from TASK
 	 * @param	index	the list index (should equal arraylist index from tasks) from the task to be loaded
 	 */
 	private void loadTaskInfo(int index) throws GUIException {
 		Taak t = (Taak) tasksJList.getSelectedValue();
 		taskNameJTextField.setText(t.getNaam());
 		taskStartDateChooser.setDate(new Date(t.getBegindatum() * 1000));
 		taskEndDateChooser.setDate(new Date(t.getGeschatteEinddatum() * 1000));
 		taskCommentJTextArea.setText(t.getCommentaar());
 		taskCompletedJCheckBox.setSelected(t.getCompleted());
 
 		refreshWorkedTime(t, workedTimeJList);
 
 		taskTotalWorkedJTextField.setText(Clock.longTimeToString(t.getTotaleWerktijd(), false));
 		taskTotalPauseJTextField.setText(Clock.longTimeToString(t.getTotalePauze(), false));
 	}
 
 	/**
 	 * Load info from CLIENT
 	 * @param	index	the list index (should equal arraylist index from clients) from the client to be loaded
 	 */
 	private void loadClientInfo(int index) {
 		Opdrachtgever o = (Opdrachtgever) clientsJList.getSelectedValue();
 		clientNameJTextField.setText(o.getNaam());
 		clientFirstNameJTextField.setText(o.getVoornaam());
 		clientCompanyJTextField.setText(o.getBedrijfsnaam());
 		clientEmailJTextField.setText(o.getEmail());
 		clientPhoneNumberJTextField.setText(o.getTelefoonnummer());
 	}
 
 	/**
 	 * Sync local changes with the database, NOT USED YET
 	 */
 	private void sync() {
 		try {
 			LocalDatabaseSynch lds = new LocalDatabaseSynch(Validator.getInstance());
 			lds.synch();
 			// LoginForm.loadUserData();
 		} catch (JSONException | IOException | WebserviceException | DataInputException e) {
 			e.printStackTrace();
 			showGUIMessage(e.getMessage(), true);
 		}
 	}
 
 	// ================================================================================
 	// GUI methods
 	// ================================================================================
 
 	/**
 	 * Open a new work dialog if current project is set and contains running tasks
 	 */
 	private void openWorkDialog() {
 		try {
 			Project p = UserInterface.getCurrentProject();
 			if (!p.tasksAvailable()) {
 				throw new GUIException("Current project contains no running tasks");
 			}
 			setVisible(false);
 			WorkDialog work = new WorkDialog(this, true, validator);
 			work.setVisible(true);
 			setVisible(true);
 			loadTaskInfo(tasksJList.getSelectedIndex());
 		} catch (GUIException ex) {
 			ex.printStackTrace();
 			showGUIMessage(ex.getMessage(), true);
 		}
 	}
 
 	private void openAddTimeDialog() {
 		addTimeDialog addTime = new addTimeDialog(this, true, (Taak) tasksJList.getSelectedValue());
 		addTime.setLocationRelativeTo(this);
 		addTime.setVisible(true);
 		try {
 			loadTaskInfo(tasksJList.getSelectedIndex());
 		} catch (GUIException e) {
 			e.printStackTrace();
 			showGUIMessage(e.getMessage(), true);
 		}
 	}
 
 	/**
 	 * CLEAR ALL FIELDS on the panels in parameter except for labels and buttons
 	 * @param 	panel	the panel on which to clear the components
 	 */
 	private void clearFieldsOnPanel(JPanel panel) {
 		Component[] panelComponents = panel.getComponents();
 		for (Component c : panelComponents) {
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
 			} else if (c instanceof JProgressBar) {
 				((JProgressBar) c).setValue(0);
 			}
 		}
 	}
 
 	/**
 	 * Set the CURRENT PROJECT with GUI responding right
 	 * 
 	 * @param index	the index (GUI list and arraylist) from the project that's
 	 *            	going to be the current project
 	 */
 	private void setCurrentProjectGUI(int index) {
 		try {
 			UserInterface.setCurrentProjectIndex(index);
 			currentProjectJLabel.setText("Current project: " + UserInterface.getCurrentProject().getNaam());
 			saveTaskJButton.setText("Save to " + UserInterface.getCurrentProject().getNaam());
 			refreshProjectsList(projectsJList, homeProjectsJList);
 			refreshTasksList(UserInterface.getCurrentProject(), tasksJList);
 			selectCurrentProject();
 			selectNewItem(tasksJList);
 			showGUIMessage("Current project: " + UserInterface.getCurrentProject().getNaam(), false);
 		} catch (GUIException ex) {
 			ex.printStackTrace();
 			showGUIMessage(ex.getMessage(), true);
 		}
 	}
 
 	private void selectCurrentProject() {
 		try {
 			Project currProj = UserInterface.getCurrentProject();
 			homeProjectsJList.setSelectedValue(currProj, true);
 			projectsJList.setSelectedValue(currProj, true);
 		} catch (GUIException ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	/**
 	 * Log user out and show new loginForm
 	 */
 	private void logout() {
 		try {
 			WinRegistry.deleteKey(WinRegistry.HKEY_CURRENT_USER, "SOFTWARE\\ChronoMatic");
 			this.dispose();
 			LoginForm f = new LoginForm(new GUIForm(validator), validator);
 			f.setVisible(true);
 
 		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Toggle all button states in the GUI
 	 * @param 	enabled		the dependency if the button(s) should be enabled
 	 * @param	buttons		the buttons that should be dependent on 'enabled'
 	 */
 	private void toggleButtonStates(boolean disabled, JButton... buttons) {
 		for (JButton b : buttons)
 			b.setEnabled(!disabled);
 	}
 
 	/**
 	 * Show a nice looking GUI message
 	 * @param 	message		the error message
 	 * @param 	isError		specifies if this is an error message or just a notification
 	 */
 	private void showGUIMessage(String message, boolean isError) {
 		Color c = (isError) ? Color.RED : Color.GREEN;
 		long showTime = (isError) ? 6000 : 4000;
 		errorJLabel.setForeground(c);
 		errorJLabel.setText(message);
 		errorJLabel.setVisible(true);
 		TimerTask task = new TimerTask() {
 			@Override
 			public void run() {
 				errorJLabel.setVisible(false);
 			}
 		};
 		Timer t = new Timer();
 		t.schedule(task, showTime);
 	}
 
 	// ================================================================================
 	// Import & Export methods
 	// ================================================================================
 
 	/**
 	 * Export all the selected tasks in the export tree to an ics file
 	 */
 	private void exportTasks() {
 		try {
 			ArrayList<Taak> toExport = new ArrayList();
 			TreePath[] paths = exportJCheckBoxTree.getCheckingPaths();
 
 			for (TreePath p : paths) {
 				if (p.getPathCount() == 3) {
 					Project project = UserInterface.getProject(p.getParentPath().getLastPathComponent().toString());
 					Taak toAddTask = UserInterface.getTaak(project, p.getLastPathComponent().toString());
 					toExport.add(toAddTask);
 				}
 			}
 
 			if (!toExport.isEmpty()) {
 				JFileChooser fileChooser = new JFileChooser();
 				fileChooser.setFileFilter(new FileNameExtensionFilter("ics files (*.ics)", "ics"));
 				fileChooser.setDialogTitle("Export tasks");
 				fileChooser.showSaveDialog(this);
 
 				Taak[] t = new Taak[toExport.size()];
 				if (fileChooser.getSelectedFile() != null)
 					IcsExporteren.export(toExport.toArray(t), fileChooser.getSelectedFile().toPath().toString());
 				JOptionPane.showMessageDialog(this, "Tasks exported");
 			} else {
 				JOptionPane.showMessageDialog(this, "Select tasks to export");
 			}
 		} catch (IOException | ValidationException | GUIException e) {
 			e.printStackTrace();
 			JOptionPane.showMessageDialog(this, e.getMessage());
 		}
 	}
 
 	/**
 	 * Import all tasks from selected .ics file
 	 */
 	private void importTasks() {
 		try {
 			JFileChooser fileChooser = new JFileChooser();
 			fileChooser.setFileFilter(new FileNameExtensionFilter("ics files (*.ics)", "ics"));
 			fileChooser.setDialogTitle("Import tasks");
 			fileChooser.showOpenDialog(this);
 			if (fileChooser.getSelectedFile() != null) {
 				importedTasks = IcsImporteren.importTasks(fileChooser.getSelectedFile().toPath().toString());
 				refreshTree(importJCheckBoxTree, IcsImporteren.importTasksInProject(fileChooser.getSelectedFile().toPath().toString()));
 				importToProjectJButton.setEnabled(true);
 				projectsJComboBox.setEnabled(true);
 			}
 		} catch (IOException | ParserException e) {
 			e.printStackTrace();
 			JOptionPane.showMessageDialog(this, e.getMessage());
 		}
 	}
 
 	/**
 	 * Export the selected project to an Excel file
 	 */
 	private void exportProjectToExcel() {
 		if (toExportProjectJComboBox.getItemCount() != 0) {
 			Project p = ((Project) toExportProjectJComboBox.getSelectedItem());
 			JFileChooser fileChooser = new JFileChooser();
 			fileChooser.setFileFilter(new FileNameExtensionFilter("excel files (*.xls)", "xls"));
 			fileChooser.setDialogTitle("Export project to excel");
 			fileChooser.showSaveDialog(this);
 			if (fileChooser.getSelectedFile() != null) {
 				Excel excel = new Excel(p, fileChooser.getSelectedFile().toPath().toString());
 				try {
 					excel.makeFile();
 				} catch (IOException | DataInputException e) {
 					e.printStackTrace();
 					showGUIMessage(e.getMessage(), true);
 				}
 			}
 		}
 	}
 
 	// ================================================================================
 	// Event handlers, FIXME afsplitsen!
 	// ================================================================================
 
 	/**
 	 * Initialiseer statusvelden (linksboven) en project en client lijsten
 	 * @param evt
 	 */
 	private void guiOpened(WindowEvent evt) {
 		refreshProjectsList(projectsJList, homeProjectsJList);
 		refreshClientsList(clientsJList);
 		refreshTree(exportJCheckBoxTree, UserInterface.getProjects());
 		loadUserInfo();
 		selectNewItem(projectsJList, clientsJList);
 	}
 
 	/**
 	 * Call the setCurrenProjectGUI method when enter is pressed in the home screen
 	 * @param 	arg0
 	 */
 	private void setCurrentProjectWithEnter(KeyEvent arg0) {
 		JList list = (JList) arg0.getSource();
 		if (arg0.getKeyCode() == KeyEvent.VK_ENTER && list.getSelectedValue().getClass().equals(Project.class)) {
 			setCurrentProjectGUI(list.getSelectedIndex());
 		}
 	}
 
 	/**
 	 * Call the setCurrenProjectGUI method when project is double clicked in home screen
 	 * @param 	arg0
 	 */
 	private void setCurrentProjectWithMouse(MouseEvent arg0) {
 		JList list = (JList) arg0.getSource();
 		if (arg0.getClickCount() == 2) {
 			int index = list.locationToIndex(arg0.getPoint());
 			if (index != -1 && list.getSelectedValue().getClass().equals(Project.class)) {
 				setCurrentProjectGUI(index);
 			}
 		}
 	}
 
 	/**
 	 * Save the imported tasks to a project when the saveToProjectButton is clicked
 	 * @param 	arg0
 	 */
 	private void saveToProjectButtonClicked(ActionEvent arg0) {
 		ArrayList<Taak> toSave = new ArrayList();
 		TreePath[] paths = importJCheckBoxTree.getCheckingPaths();
 
 		for (TreePath p : paths) {
 			if (p.getPathCount() == 3) {
 				for (Taak t : importedTasks) {
 					if (t.getNaam().equals(p.getLastPathComponent().toString())) {
 						toSave.add(t);
 					}
 				}
 			}
 		}
 		Taak[] saveArray = new Taak[toSave.size()];
 		if (!toSave.isEmpty()) {
 			// FIXME, wordt momenteel enkel lokaal opgeslagen, UserInterface moet functie createTasks hebben
 			((Project) projectsJComboBox.getSelectedItem()).addTaken(toSave.toArray(saveArray));
 		}
 	}
 
 	/**
 	 * Refresh the task list on the home screen when a new project gets selected
 	 * @param evt
 	 */
 	private void homeProjectListValueChanged(ListSelectionEvent evt) {
 		if (homeProjectsJList.getSelectedIndex() != -1) {
 			refreshTasksList((Project) homeProjectsJList.getSelectedValue(), homeTasksJList);
 		}
 	}
 
 	/**
 	 * Check if new item is selected when projectsJList selected index changes and
 	 * clear fields and set buttons accordingly
 	 * @param 	evt
 	 */
 	private void projectsJListValueChanged(ListSelectionEvent evt) {
 		boolean newSelected = NEWPROJECTITEM.equals(projectsJList.getSelectedValue());
 		if (newSelected) {
 			projectStartDateChooser.setDate(new Date());
 			projectEndDateChooser.setDate(new Date());
 			clearFieldsOnPanel(projectFieldsJPanel);
 			refreshClientsComboBox(null, projectClientsJComboBox);
 			toggleButtonStates(newSelected, setCurrentProjectJButton, removeProjectJButton);
 			saveProjectJButton.setText("Save [new]");
 		} else if (projectsJList.getSelectedValue() != null) {
 			loadProjectInfo(projectsJList.getSelectedIndex());
 			toggleButtonStates(newSelected, setCurrentProjectJButton, removeProjectJButton);
 			saveProjectJButton.setText("Save");
 		}
 	}
 
 	/**
 	 * Load client info when the selected index changes, and change button states
 	 * @param 	arg0
 	 */
 	private void clientsJListValueChanged(ListSelectionEvent arg0) {
 		if (clientsJList.getSelectedIndex() != -1) {
 			if (clientsJList.getSelectedValue().equals(NEWCLIENTITEM)) {
 				clearFieldsOnPanel(clientFieldsJPanel);
 				saveClientJButton.setText("Save [new]");
 				removeClientJButton.setEnabled(false);
 			} else {
 				loadClientInfo(clientsJList.getSelectedIndex());
 				saveClientJButton.setText("Save");
 				removeClientJButton.setEnabled(true);
 			}
 		}
 	}
 
 	/**
 	 * Load task info or clear panels and change buttons according to
 	 * selected value in taskJList when selected value changes
 	 * @param arg0
 	 */
 	private void tasksJListValueChanged(ListSelectionEvent arg0) {
 		try {
 			if (tasksJList.getSelectedIndex() != -1) {
 				boolean newSelected = NEWTASKITEM.equals(tasksJList.getSelectedValue());
 				if (newSelected) {
 					clearFieldsOnPanel(taskFieldsJPanel);
 					saveTaskJButton.setText("Save [new]");
 				} else if (tasksJList.getSelectedValue() instanceof String) {
 					clearFieldsOnPanel(taskFieldsJPanel);
 				} else {
 					loadTaskInfo(tasksJList.getSelectedIndex());
 					saveTaskJButton.setText("Save");
 					addTimeJButton.setEnabled(true);
 				}
 				toggleButtonStates(newSelected, removeTaskJButton);
 			} else {
 				clearFieldsOnPanel(taskFieldsJPanel);
 			}
 			removeTimeButton.setEnabled(false);
 		} catch (GUIException e) {
 			e.printStackTrace();
 			showGUIMessage(e.getMessage(), true);
 		}
 	}
 
 	private void workedTimeJListValueChange(ListSelectionEvent arg0) {
 		if (workedTimeJList.getSelectedIndex() != -1) {
 			removeTimeButton.setEnabled(true);
 		}
 	}
 
 	/**
 	 * Sets selected item to < new ... >
 	 * @param 	lists	the lists in which the selected item should be set to < new ... >
 	 */
 	private void selectNewItem(JList... lists) {
 		for (JList list : lists) {
 			list.setSelectedIndex(list.getModel().getSize() - 1);
 			list.ensureIndexIsVisible(list.getSelectedIndex());
 		}
 	}
 
 	private void clientsJComboBoxValueChanged(ActionEvent arg0) {
 		JComboBox combobox = (JComboBox) arg0.getSource();
 		if (combobox.getSelectedIndex() != -1) {
 			if (combobox.getSelectedItem().equals(NEWCLIENTITEM)) {
 				creatingProject = true;
 				contentJTabbedPane.setSelectedIndex(3);
 				selectNewItem(clientsJList);
 			}
 		}
 	}
 
 	// ================================================================================
 	// Event handlers for save buttons
 	// ================================================================================
 
 	/**
 	 * Save or update client (dependent on selected value in clientsJList)
 	 */
 	private void saveClientButtonClicked() {
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
 	}
 
 	/**
 	 * Save or update project (dependent on selected value in projectsJList)
 	 */
 	private void saveProjectButtonClicked() {
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
 			e.printStackTrace();
 			showGUIMessage(e.getMessage(), true);
 		}
 	}
 
 	/**
 	 * Save or update task (dependent on selected value in tasksJList)
 	 */
 	private void saveTaskButtonClicked() {
 		int selectedIndex = tasksJList.getSelectedIndex();
 		try {
 			String name = taskNameJTextField.getText();
 			String comment = taskCommentJTextArea.getText();
 			boolean completed = taskCompletedJCheckBox.isSelected();
 			long startdate = taskStartDateChooser.getDate().getTime() / 1000;
 			long enddate = taskEndDateChooser.getDate().getTime() / 1000;
 			int projectId = UserInterface.getCurrentProject().getId();
 
 			if (tasksJList.getSelectedValue().equals(NEWTASKITEM)) {
 				createTask(name, startdate, enddate, comment, completed);
 			} else {
 				updateTask(name, startdate, enddate, comment, completed, projectId);
 			}
 		} catch (NullPointerException ex) {
 			ex.printStackTrace();
 			showGUIMessage("Please choose a valid date", true);
 		} catch (GUIException e1) {
 			e1.printStackTrace();
 			showGUIMessage(e1.getMessage(), true);
 		} finally {
 			loadProjectInfo(projectsJList.getSelectedIndex());
 			refreshProjectsList(homeProjectsJList);
 			if (projectsJList.getSelectedValue() instanceof Project)
 				refreshTasksList((Project) projectsJList.getSelectedValue(), homeTasksJList);
 			tasksJList.setSelectedIndex(selectedIndex);
 		}
 	}
 
 	// ================================================================================
 	// Component variable declaration
 	// ================================================================================
 	private javax.swing.JTabbedPane contentJTabbedPane;
 	private javax.swing.JLabel clientcompJLabel1;
 	private javax.swing.JLabel clientsJLabel;
 	private javax.swing.JPanel clientsJPanel;
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
 	private javax.swing.JButton setCurrentProjectJButton;
 	private javax.swing.JLabel startdatecompJLabel;
 	private javax.swing.JLabel tasksJLabel;
 	private javax.swing.JPanel tasksJPanel;
 	private javax.swing.JLabel taskscompJLabel;
 	private javax.swing.JLabel logoLabel;
 	private javax.swing.JButton workJButton;
 	private JTextField clientNameJTextField;
 	private JTextField clientFirstNameJTextField;
 	private JTextField clientCompanyJTextField;
 	private JTextField clientEmailJTextField;
 	private JTextField clientPhoneNumberJTextField;
 	private JPanel clientFieldsJPanel;
 	private JButton saveClientJButton;
 	private JPanel taskEditFieldsJPanel;
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
 	private JScrollPane importProjectsJScrollPane;
 	private JButton importJButton;
 	private JScrollPane exportJScrollPane;
 	private JButton exportJButton;
 	private JComboBox projectClientsJComboBox;
 	private JList clientsJList;
 	private JTextField taskTotalWorkedJTextField;
 	private JTextField taskTotalPauseJTextField;
 	private JLabel taskTotalWorkedJLabel;
 	private JLabel lblTotalPaused;
 	private JPanel projectFieldsJPanel;
 	private JPanel taskFieldsJPanel;
 	private JTabbedPane settingsJTabbedPane;
 	private JPanel userSettingsJPanel;
 	private CheckboxTree exportJCheckBoxTree;
 	private JTextField firstNameJTextField;
 	private JTextField lastNameJTextField;
 	private JTextField emailJTextField;
 	private JLabel firstNameJLabel;
 	private JLabel lastNameJLabel;
 	private JLabel emailJLabel;
 	private JTextField addressJTextField;
 	private JLabel addressJLabel;
 	private JTextField telephoneJTextField;
 	private JLabel lblTelephone;
 	private JLabel personalInfoJLabel;
 	private JTextField vatJTextField;
 	private JLabel vatJLabel;
 	private JLabel ibanJLabel;
 	private JTextField ibanJTextField;
 	private JTextField bicJTextField;
 	private JLabel bicJLabel;
 	private JLabel businessInfoJLabel;
 	private JButton updateUserJButton;
 	private JTextField usernameJTextField;
 	private JLabel usernameJLabel;
 	private JButton logoutJButton;
 	private CheckboxTree importJCheckBoxTree;
 	private JComboBox projectsJComboBox;
 	private JButton importToProjectJButton;
 	private JPanel projectStatusFieldsJPanel;
 	private JPanel taskStatusFieldsJPanel;
 	private JButton syncButton;
 	private JButton addTimeJButton;
 	private JList tasksJList;
 	private JButton removeTimeButton;
 	private JLabel companyNameJLabel;
 	private JTextField companyNameJTextField;
 	private JComboBox toExportProjectJComboBox;
 	private JButton exportToExcelJButton;
 	private JLabel errorJLabel;
 }
