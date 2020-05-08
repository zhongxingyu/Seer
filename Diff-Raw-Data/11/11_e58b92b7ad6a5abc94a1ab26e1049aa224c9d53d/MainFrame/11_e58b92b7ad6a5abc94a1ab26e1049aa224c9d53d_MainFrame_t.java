 package qos;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JSlider;
 import javax.swing.JSpinner;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.SwingUtilities;
 import javax.swing.SwingWorker;
 import javax.swing.border.LineBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.TableColumnModel;
 
 public class MainFrame extends JFrame {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	private JPanel contentPane;
 	private JCheckBox jCheckBoxMaxCosts;
 	private JCheckBox jCheckBoxMaxResponseTime;
 	private JCheckBox jCheckBoxMinAvailability;
 	
 	private JTextField jTextFieldMaxCosts;
 	private JTextField jTextFieldMaxResponseTime;
 	private JTextField jTextFieldMinAvailability;
 
 	private JSpinner jSpinnerNumberResultTiers;
 	private JSlider jSliderMaxCosts;
 	private JSlider jSliderMaxResponseTime;
 	private JSlider jSliderMinAvailability;
 	
 	private JTable jTableServiceClasses;
 	private JTable jTableWebServices;
 
 	private JLabel jLabelGeneticAlgorithmNumerator;
 	private JLabel jLabelGeneticAlgorithmDenominator;
 	private JLabel jLabelWeightedPenalty;
 	private JLabel jLabelUtilityText;
 
 	private JLabel jLabelColon;
 	private JLabel jLabelTerminationCriterionPercentage;
 	private JTextField jTextFieldTerminationCriterion;
 	
 	private JCheckBox jCheckboxGeneticAlgorithm;
 	private JCheckBox jCheckBoxAntColonyOptimization;
 	private JCheckBox jCheckBoxAnalyticAlgorithm;
 	private JTable jTableAnalyticAlgorithm;
 
 	private JProgressBar jProgressBarGeneticAlgorithm;
 	private JProgressBar jProgressBarAntAlgorithm;
 	private JProgressBar jProgressBarAnalyticAlgorithm;
 
 	private JTable jTableGeneralResults;
 
 	private JTabbedPane jTabbedPane;
 
 	private JButton jButtonStart;
 	private JButton jButtonVisualize;
 
 	private JLabel lblWeightSum;
 	private JSeparator jSeparatorFormula;
 	
 	private JTextArea textAreaLog;
 	
 	private GeneticAlgorithm geneticAlgorithm;
 	private AntAlgorithm antAlgorithm;
 	private AnalyticAlgorithm analyticAlgorithm;
 	
 	private boolean webServicesLoaded = false;
 	private boolean correctWeights = true;
 	private boolean correctPenalty = true;
 
 	private static MainFrame frame;
 
 
 	private int maxCosts = 10000;
 	private int maxResponseTime = 10000;
 	private int maxAvailability = 100;
 	
 	private int minCosts = 0;
 	private int minResponseTime = 0;
 	private int minAvailability = 0;
 	
 	private double cumulatedRuntime;	
 	
 	public static final DecimalFormat DECIMAL_FORMAT_TWO = 
 		new DecimalFormat("###.##");
 	public static final DecimalFormat DECIMAL_FORMAT_FOUR = 
 		new DecimalFormat("###.####");
 
 
 	private List<ServiceClass> serviceClassesList = 
 		new LinkedList<ServiceClass>();
 	private List<ServiceCandidate> serviceCandidatesList = 
 		new LinkedList<ServiceCandidate>();
 	private QosVector qosMax;
 	private QosVector qosMin;
 	private JTextField txtCostsWeight;
 	private JTextField txtResponseTimeWeight;
 	private JTextField txtAvailabilityWeight;
 	private JTextField jTextFieldPenaltyFactor;
 	private JTextField jTextFieldPopulationSize;
 	private JTextField txtAntIterations;
 	private JTextField txtAntAnts;
 	private JTextField txtAntAlpha;
 	private JTextField txtAntBeta;
 	private JTextField txtAntDilution;
 	private JTextField txtAntPi;
 	
 	private JComboBox<String> jComboBoxCrossover;
 	private JComboBox<String> jComboBoxSelection;
 	private JComboBox<String> jComboBoxTerminationCriterion;
 	private JLabel jLabelPopulationPercentage;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					frame = new MainFrame();
 					frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the frame.
 	 */
 	public MainFrame() {
 		// FRAME SETTINGS
 		setTitle("test_gui");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 0, 1000, 850);
 
 		// MAIN CONTENT PANEL CONFIGURATION
 		contentPane = new JPanel();
 		contentPane.setBorder(new LineBorder(new Color(0, 0, 0)));
 		setContentPane(contentPane);
 		GridBagLayout gblContentPane = new GridBagLayout();
 		gblContentPane.columnWeights = new double[]{0.6, 0.2, 0.2};
 		gblContentPane.rowWeights = new double[]{
 				0.025, 0.15, 0.05, 0.275, 0.01, 0.025, 0.415, 0.05};
 		contentPane.setLayout(gblContentPane);
 		Font generalFont = new Font(
 				"generalSettingsFont", Font.BOLD, 16);
 
 		JLabel jLabelSettings = new JLabel("Settings");
 		jLabelSettings.setFont(generalFont);
 		GridBagConstraints gbcJLabelSettings = new GridBagConstraints();
 		gbcJLabelSettings.gridwidth = 2;
 		gbcJLabelSettings.insets = new Insets(0, 0, 5, 0);
 		gbcJLabelSettings.gridx = 0;
 		gbcJLabelSettings.gridy = 0;
 		contentPane.add(jLabelSettings, gbcJLabelSettings);
 
 		JSeparator jSeparatorSettingsResults = new JSeparator();
 		GridBagConstraints gbcJSeparatorSettingsResults = 
 			new GridBagConstraints();
 		gbcJSeparatorSettingsResults.gridwidth = 3;
 		gbcJSeparatorSettingsResults.fill = GridBagConstraints.BOTH;
 		gbcJSeparatorSettingsResults.insets = new Insets(10, 5, 10, 5);
 		gbcJSeparatorSettingsResults.gridx = 0;
 		gbcJSeparatorSettingsResults.gridy = 4;
 		contentPane.add(jSeparatorSettingsResults, 
 				gbcJSeparatorSettingsResults);
 
 		JLabel jLabelResults = new JLabel("Results");
 		jLabelResults.setFont(generalFont);
 		GridBagConstraints gbcJLabelResults = new GridBagConstraints();
 		gbcJLabelResults.gridwidth = 2;
 		gbcJLabelResults.insets = new Insets(0, 0, 5, 5);
 		gbcJLabelResults.gridx = 0;
 		gbcJLabelResults.gridy = 5;
 		contentPane.add(jLabelResults, gbcJLabelResults);
 
 		jButtonVisualize = new JButton("Visualize");
 		jButtonVisualize.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				showResultVisualization();
 			}
 		});
 		jButtonVisualize.setEnabled(false);
 		GridBagConstraints gbcJButtonVisualize = new GridBagConstraints();
 		gbcJButtonVisualize.insets = new Insets(0, 0, 5, 0);
 		gbcJButtonVisualize.gridx = 2;
 		gbcJButtonVisualize.gridy = 5;
 		contentPane.add(jButtonVisualize, gbcJButtonVisualize);
 
 		initializeMenuBar();
 		initializeGeneralSettingsPanel(contentPane);
 		initializeCenterArea(contentPane);
 		initializeGeneticAlgorithmPanel(contentPane);
 		initializeAntAlgorithmPanel(contentPane);
 		initializeAnalyticAlgorithmPanel(contentPane);
 		initializeTabbedResultsPanel(contentPane);
 		initializeGeneralResultsPanel(contentPane);
 		initializeLogPanel(contentPane);
 	}
 
 	private void initializeMenuBar() {
 		JMenuBar jMenuBar = new JMenuBar();
 		setJMenuBar(jMenuBar);
 
 		JMenu jMenuFile = new JMenu("File");
 		jMenuBar.add(jMenuFile);
 		JMenuItem jMenuItemReset = new JMenuItem("Reset");
 		jMenuItemReset.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				resetProgram();
 			}
 		});
 		jMenuFile.add(jMenuItemReset);
 		JMenuItem jMenuItemLoad = new JMenuItem("Load");
 
 		final JFileChooser fileChooser = new JFileChooser() {
 			/**
 			 * 
 			 */
 			private static final long serialVersionUID = 1L;
 
 			{
 				setFileFilter(new FileFilter() {
 					@Override
 					public boolean accept(File f) {
 						return f.getName().toLowerCase().endsWith("csv") || 
 						f.isDirectory();
 					}
 					@Override
 					public String getDescription() {
 						return "CSV Datei (Comma Seperated Values)";
 					}
 				});
 			}
 		};
 
 		jMenuItemLoad.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if (!(fileChooser.showOpenDialog(MainFrame.this) == 
 					JFileChooser.APPROVE_OPTION)) {
 					return;
 				}
 				final File file = fileChooser.getSelectedFile();
 				if (file == null) {
 					return;
 				}
 				loadWebServices(file);
 			}
 		});
 
 		jMenuFile.add(jMenuItemLoad);
 		JMenuItem jMenuItemSave = new JMenuItem("Save");
 		jMenuFile.add(jMenuItemSave);
 		JMenuItem jMenuItemExit = new JMenuItem("Exit");
 		jMenuItemExit.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.exit(0);
 			}
 		});
 		jMenuFile.add(jMenuItemExit);
 
 		JMenu jMenuEdit = new JMenu("Constraints");
 		jMenuBar.add(jMenuEdit);
 		JMenuItem jMenuItemLoadDefaultConstraints = 
 			new JMenuItem("Use Default Values");
 		jMenuItemLoadDefaultConstraints.addActionListener(
 				new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						setDefaultConstraints();
 					}
 				});
 		jMenuEdit.add(jMenuItemLoadDefaultConstraints);
 		JMenuItem jMenuItemLoadRandomConstraints = 
 			new JMenuItem("Use Random Values");
 		jMenuItemLoadRandomConstraints.addActionListener(
 				new ActionListener() {
 					public void actionPerformed(ActionEvent arg0) {
 						setRandomConstraints();
 					}
 				});
 		jMenuEdit.add(jMenuItemLoadRandomConstraints);
 		JMenuItem jMenuItemLoadConstraints = new JMenuItem("Load Constraints");
 		jMenuEdit.add(jMenuItemLoadConstraints);
 		JMenuItem jMenuItemSaveConstraints = new JMenuItem("Save Constraints");
 		jMenuEdit.add(jMenuItemSaveConstraints);
 		
 		JMenuItem jMenuItemLoadRandomSet = new JMenuItem("Load Random Set");
 		jMenuEdit.add(jMenuItemLoadRandomSet);
 		jMenuItemLoadRandomSet.addActionListener(
 				new ActionListener() {
 					public void actionPerformed(ActionEvent arg0) {
 						loadRandomWebServices();
 					}
 				});
 
 		JMenu jMenuOther = new JMenu("?");
 		jMenuBar.add(jMenuOther);
 		JMenuItem jMenuItemHelp = new JMenuItem("Help");
 		jMenuOther.add(jMenuItemHelp);
 		JMenuItem jMenuItemSupport = new JMenuItem("Support");
 		jMenuOther.add(jMenuItemSupport);
 		JMenuItem jMenuItemAbout = new JMenuItem("About");
 		jMenuOther.add(jMenuItemAbout);
 	}
 
 	private void initializeGeneralSettingsPanel (JPanel contentPane) {
 		JPanel jPanelGeneralSettings = new JPanel();
 		GridBagConstraints gbcJPanelGeneralSettings = 
 			new GridBagConstraints();
 		GridBagLayout gblJPanelGeneralSettings = new GridBagLayout();
 		gblJPanelGeneralSettings.columnWeights = new double[]{0.2, 0.2, 0.6};
 		gblJPanelGeneralSettings.rowWeights = new double[]{1.0};
 		jPanelGeneralSettings.setLayout(gblJPanelGeneralSettings);
 		gbcJPanelGeneralSettings.gridheight = 1;
 		gbcJPanelGeneralSettings.gridwidth = 3;
 		gbcJPanelGeneralSettings.fill = GridBagConstraints.BOTH;
 		gbcJPanelGeneralSettings.gridx = 0;
 		gbcJPanelGeneralSettings.gridy = 1;
 		initializeQosConstraintsPanel(jPanelGeneralSettings);
 		initializeServiceClassesPanel(jPanelGeneralSettings);
 		initializeWebServicesPanel(jPanelGeneralSettings);
 		contentPane.add(jPanelGeneralSettings, gbcJPanelGeneralSettings);
 	}
 
 	private void initializeQosConstraintsPanel(JPanel contentPane) {
 		JPanel jPanelQosConstraints = new JPanel();
 		GridBagConstraints gbcJPanelQosConstraints = new GridBagConstraints();
 		gbcJPanelQosConstraints.gridheight = 1;
 		gbcJPanelQosConstraints.insets = new Insets(0, 5, 5, 5);
 		gbcJPanelQosConstraints.fill = GridBagConstraints.BOTH;
 		gbcJPanelQosConstraints.gridx = 0;
 		gbcJPanelQosConstraints.gridy = 0;
 		contentPane.add(jPanelQosConstraints, gbcJPanelQosConstraints);
 		GridBagLayout gblJPanelQosConstraints = new GridBagLayout();
 		gblJPanelQosConstraints.columnWeights = new double[]{
 				0.15, 0.3, 0.4, 0.05, 0.05, 0.05};
 		gblJPanelQosConstraints.rowWeights = new double[]{
 				0.15, 0.15, 0.15, 0.15, 0.15, 0.1, 0.15};
 		jPanelQosConstraints.setLayout(gblJPanelQosConstraints);
 
 		JLabel jLabelQosConstraints = new JLabel("QoS Constraints:");
 		GridBagConstraints gbcJLabelQosConstraints = new GridBagConstraints();
 		gbcJLabelQosConstraints.gridwidth = 4;
 		gbcJLabelQosConstraints.insets = new Insets(0, 0, 5, 5);
 		gbcJLabelQosConstraints.gridx = 0;
 		gbcJLabelQosConstraints.gridy = 0;
 		jPanelQosConstraints.add(
 				jLabelQosConstraints, gbcJLabelQosConstraints);
 
 		
 
 		JLabel lblWeight = new JLabel("Weight");
 		GridBagConstraints gbc_lblWeight = new GridBagConstraints();
 		gbc_lblWeight.insets = new Insets(0, 0, 5, 5);
 		gbc_lblWeight.gridx = 4;
 		gbc_lblWeight.gridy = 1;
 		jPanelQosConstraints.add(lblWeight, gbc_lblWeight);
 
 		jCheckBoxMaxCosts = new JCheckBox("Max. Costs");
 		jCheckBoxMaxCosts.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				buildGeneticAlgorithmFitnessFunction();
 				changeConstraintCheckboxStatus("Costs");
 			}
 		});
 		jCheckBoxMaxCosts.setSelected(true);
 		GridBagConstraints gbcJCheckBoxMaxCosts = new GridBagConstraints();
 		gbcJCheckBoxMaxCosts.insets = new Insets(0, 0, 5, 5);
 		gbcJCheckBoxMaxCosts.gridx = 0;
 		gbcJCheckBoxMaxCosts.gridy = 2;
 		gbcJCheckBoxMaxCosts.anchor = GridBagConstraints.WEST;
 		jPanelQosConstraints.add(jCheckBoxMaxCosts, gbcJCheckBoxMaxCosts);
 
 		jSliderMaxCosts = new JSlider();
 		jSliderMaxCosts.setMaximum(maxCosts);
 		jSliderMaxCosts.setMinimum(0);
 		jSliderMaxCosts.setValue(maxCosts / 2);
 		jSliderMaxCosts.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent e) {
 				useConstraintSlider(jTextFieldMaxCosts, jSliderMaxCosts);
 			}
 		});
 		GridBagConstraints gbcJSliderMaxCosts = new GridBagConstraints();
 		gbcJSliderMaxCosts.insets = new Insets(0, 0, 5, 5);
 		gbcJSliderMaxCosts.gridx = 1;
 		gbcJSliderMaxCosts.gridy = 2;
 		gbcJSliderMaxCosts.fill = GridBagConstraints.BOTH;
 		jPanelQosConstraints.add(jSliderMaxCosts, gbcJSliderMaxCosts);
 
 		jTextFieldMaxCosts = 
 			new JTextField(String.valueOf(jSliderMaxCosts.getValue()));
 		jTextFieldMaxCosts.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				setConstraintValueManually(jSliderMaxCosts, 
 						jTextFieldMaxCosts, jSliderMaxCosts.getMinimum(),
 						jSliderMaxCosts.getMaximum());
 			}
 		});
 		jTextFieldMaxCosts.setHorizontalAlignment(JTextField.RIGHT);
 		GridBagConstraints gbcJTextFieldMaxCosts = new GridBagConstraints();
 		gbcJTextFieldMaxCosts.insets = new Insets(0, 0, 5, 5);
 		gbcJTextFieldMaxCosts.fill = GridBagConstraints.HORIZONTAL;
 		gbcJTextFieldMaxCosts.gridx = 2;
 		gbcJTextFieldMaxCosts.gridy = 2;
 		jPanelQosConstraints.add(jTextFieldMaxCosts, gbcJTextFieldMaxCosts);	
 
 		JLabel jLabelMaxCosts = new JLabel("\u20AC");
 		GridBagConstraints gbcJLabelMaxCosts = new GridBagConstraints();
 		gbcJLabelMaxCosts.anchor = GridBagConstraints.WEST;
 		gbcJLabelMaxCosts.insets = new Insets(0, 0, 5, 5);
 		gbcJLabelMaxCosts.gridx = 3;
 		gbcJLabelMaxCosts.gridy = 2;
 		jPanelQosConstraints.add(jLabelMaxCosts, gbcJLabelMaxCosts);
 
 		txtCostsWeight = new JTextField("34");
 		txtCostsWeight.setHorizontalAlignment(JTextField.RIGHT);
 		txtCostsWeight.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				changeWeight(txtCostsWeight);
 			}
 		});
 		GridBagConstraints gbc_txtCostsWeight = new GridBagConstraints();
 		gbc_txtCostsWeight.insets = new Insets(0, 0, 5, 5);
 		gbc_txtCostsWeight.fill = GridBagConstraints.HORIZONTAL;
 		gbc_txtCostsWeight.gridx = 4;
 		gbc_txtCostsWeight.gridy = 2;
 		jPanelQosConstraints.add(txtCostsWeight, gbc_txtCostsWeight);
 
 		JLabel lblPercentageCostsWeight = new JLabel("%");
 		GridBagConstraints gbc_lblPercentageCostsWeight = 
 			new GridBagConstraints();
 		gbc_lblPercentageCostsWeight.insets = new Insets(0, 0, 5, 0);
 		gbc_lblPercentageCostsWeight.gridx = 5;
 		gbc_lblPercentageCostsWeight.gridy = 2;
 		jPanelQosConstraints.add(
 				lblPercentageCostsWeight, gbc_lblPercentageCostsWeight);
 
 
 
 		jCheckBoxMaxResponseTime = new JCheckBox("Max. Response Time");
 		jCheckBoxMaxResponseTime.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				buildGeneticAlgorithmFitnessFunction();
 				changeConstraintCheckboxStatus("Response Time");
 			}
 		});
 		jCheckBoxMaxResponseTime.setSelected(true);
 		GridBagConstraints gbcJCheckBoxMaxResponseTime = 
 			new GridBagConstraints();
 		gbcJCheckBoxMaxResponseTime.insets = new Insets(0, 0, 5, 5);
 		gbcJCheckBoxMaxResponseTime.gridx = 0;
 		gbcJCheckBoxMaxResponseTime.gridy = 3;
 		gbcJCheckBoxMaxResponseTime.anchor = GridBagConstraints.WEST;
 		jPanelQosConstraints.add(
 				jCheckBoxMaxResponseTime, gbcJCheckBoxMaxResponseTime);
 
 		jSliderMaxResponseTime = new JSlider();
 		jSliderMaxResponseTime.setMaximum(maxResponseTime);
 		jSliderMaxResponseTime.setMinimum(0);
 		jSliderMaxResponseTime.setValue(maxResponseTime / 2);
 		jSliderMaxResponseTime.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent e) {
 				useConstraintSlider(
 						jTextFieldMaxResponseTime, jSliderMaxResponseTime);
 			}
 		});
 		GridBagConstraints gbcJSliderMaxResponseTime = 
 			new GridBagConstraints();
 		gbcJSliderMaxResponseTime.insets = new Insets(0, 0, 5, 5);
 		gbcJSliderMaxResponseTime.gridx = 1;
 		gbcJSliderMaxResponseTime.gridy = 3;
 		gbcJSliderMaxResponseTime.fill = GridBagConstraints.BOTH;
 		jPanelQosConstraints.add(
 				jSliderMaxResponseTime, gbcJSliderMaxResponseTime);
 
 		jTextFieldMaxResponseTime = new JTextField(
 				String.valueOf(jSliderMaxResponseTime.getValue()));
 		jTextFieldMaxResponseTime.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				setConstraintValueManually(jSliderMaxResponseTime, 
 						jTextFieldMaxResponseTime, 
 						jSliderMaxResponseTime.getMinimum(), 
 						jSliderMaxResponseTime.getMaximum());
 			}
 		});
 		jTextFieldMaxResponseTime.setHorizontalAlignment(JTextField.RIGHT);
 		GridBagConstraints gbcJTextFieldMaxResponseTime = 
 			new GridBagConstraints();
 		gbcJTextFieldMaxResponseTime.insets = new Insets(0, 0, 5, 5);
 		gbcJTextFieldMaxResponseTime.fill = GridBagConstraints.HORIZONTAL;
 		gbcJTextFieldMaxResponseTime.gridx = 2;
 		gbcJTextFieldMaxResponseTime.gridy = 3;
 		jPanelQosConstraints.add(
 				jTextFieldMaxResponseTime, gbcJTextFieldMaxResponseTime);
 
 		JLabel jLabelMaxResponseTime = new JLabel("ms");
 		GridBagConstraints gbcJLabelMaxResponseTime = 
 			new GridBagConstraints();
 		gbcJLabelMaxResponseTime.anchor = GridBagConstraints.WEST;
 		gbcJLabelMaxResponseTime.insets = new Insets(0, 0, 5, 5);
 		gbcJLabelMaxResponseTime.gridx = 3;
 		gbcJLabelMaxResponseTime.gridy = 3;
 		jPanelQosConstraints.add(
 				jLabelMaxResponseTime, gbcJLabelMaxResponseTime);
 
 		txtResponseTimeWeight = new JTextField("33");
 		txtResponseTimeWeight.setHorizontalAlignment(JTextField.RIGHT);
 		txtResponseTimeWeight.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				changeWeight(txtResponseTimeWeight);
 			}
 		});
 		GridBagConstraints gbc_txtResponseTimeWeight = 
 			new GridBagConstraints();
 		gbc_txtResponseTimeWeight.insets = new Insets(0, 0, 5, 5);
 		gbc_txtResponseTimeWeight.fill = GridBagConstraints.HORIZONTAL;
 		gbc_txtResponseTimeWeight.gridx = 4;
 		gbc_txtResponseTimeWeight.gridy = 3;
 		jPanelQosConstraints.add(
 				txtResponseTimeWeight, gbc_txtResponseTimeWeight);
 
 		JLabel lblPercentageResponseTimeWeight = new JLabel("%");
 		GridBagConstraints gbc_lblPercentageResponseTimeWeight = 
 			new GridBagConstraints();
 		gbc_lblPercentageResponseTimeWeight.insets = new Insets(0, 0, 5, 0);
 		gbc_lblPercentageResponseTimeWeight.gridx = 5;
 		gbc_lblPercentageResponseTimeWeight.gridy = 3;
 		jPanelQosConstraints.add(
 				lblPercentageResponseTimeWeight, 
 				gbc_lblPercentageResponseTimeWeight);
 
 
 
 		jCheckBoxMinAvailability = new JCheckBox("Min. Availability");
 		jCheckBoxMinAvailability.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				buildGeneticAlgorithmFitnessFunction();
 				changeConstraintCheckboxStatus("Availability");
 			}
 		});
 		jCheckBoxMinAvailability.setSelected(true);
 		GridBagConstraints gbcJCheckBoxMinAvailability = 
 			new GridBagConstraints();
 		gbcJCheckBoxMinAvailability.insets = new Insets(0, 0, 5, 5);
 		gbcJCheckBoxMinAvailability.gridx = 0;
 		gbcJCheckBoxMinAvailability.gridy = 4;
 		gbcJCheckBoxMinAvailability.anchor = GridBagConstraints.WEST;
 		jPanelQosConstraints.add(
 				jCheckBoxMinAvailability, gbcJCheckBoxMinAvailability);
 
 		jSliderMinAvailability = new JSlider();
 		jSliderMinAvailability.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent e) {
 				useConstraintSlider(
 						jTextFieldMinAvailability, jSliderMinAvailability);
 			}
 		});
 		GridBagConstraints gbcJSliderMinAvailability = 
 			new GridBagConstraints();
 		gbcJSliderMinAvailability.insets = new Insets(0, 0, 5, 5);
 		gbcJSliderMinAvailability.gridx = 1;
 		gbcJSliderMinAvailability.gridy = 4;
 		gbcJSliderMinAvailability.fill = GridBagConstraints.BOTH;
 		jPanelQosConstraints.add(
 				jSliderMinAvailability, gbcJSliderMinAvailability);
 
 		jTextFieldMinAvailability = new JTextField(
 				String.valueOf(jSliderMinAvailability.getValue()));
 		jTextFieldMinAvailability.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				setConstraintValueManually(jSliderMinAvailability, 
 						jTextFieldMinAvailability, 
 						jSliderMinAvailability.getMinimum(), 
 						jSliderMinAvailability.getMaximum());
 			}
 		});
 		jTextFieldMinAvailability.setHorizontalAlignment(JTextField.RIGHT);
 		GridBagConstraints gbcJTextFieldMinAvailability = 
 			new GridBagConstraints();
 		gbcJTextFieldMinAvailability.insets = new Insets(0, 0, 5, 5);
 		gbcJTextFieldMinAvailability.fill = GridBagConstraints.HORIZONTAL;
 		gbcJTextFieldMinAvailability.gridx = 2;
 		gbcJTextFieldMinAvailability.gridy = 4;
 		jPanelQosConstraints.add(
 				jTextFieldMinAvailability, gbcJTextFieldMinAvailability);
 
 		JLabel jLabelMinAvailability = new JLabel("%");
 		GridBagConstraints gbcJLabelMinAvailability = new GridBagConstraints();
 		gbcJLabelMinAvailability.anchor = GridBagConstraints.WEST;
 		gbcJLabelMinAvailability.insets = new Insets(0, 0, 5, 5);
 		gbcJLabelMinAvailability.gridx = 3;
 		gbcJLabelMinAvailability.gridy = 4;
 		jPanelQosConstraints.add(
 				jLabelMinAvailability, gbcJLabelMinAvailability);
 
 		txtAvailabilityWeight = new JTextField("33");
 		txtAvailabilityWeight.setHorizontalAlignment(JTextField.RIGHT);
 		txtAvailabilityWeight.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				changeWeight(txtAvailabilityWeight);
 			}
 		});
 		GridBagConstraints gbc_txtAvailabilityWeight = 
 			new GridBagConstraints();
 		gbc_txtAvailabilityWeight.insets = new Insets(0, 0, 5, 5);
 		gbc_txtAvailabilityWeight.fill = GridBagConstraints.HORIZONTAL;
 		gbc_txtAvailabilityWeight.gridx = 4;
 		gbc_txtAvailabilityWeight.gridy = 4;
 		jPanelQosConstraints.add(
 				txtAvailabilityWeight, gbc_txtAvailabilityWeight);
 
 		JLabel lblPercentageAvailabilityWeight = new JLabel("%");
 		GridBagConstraints gbc_lblPercentageAvailabilityWeight = 
 			new GridBagConstraints();
 		gbc_lblPercentageAvailabilityWeight.insets = new Insets(0, 0, 5, 0);
 		gbc_lblPercentageAvailabilityWeight.gridx = 5;
 		gbc_lblPercentageAvailabilityWeight.gridy = 4;
 		jPanelQosConstraints.add(
 				lblPercentageAvailabilityWeight, 
 				gbc_lblPercentageAvailabilityWeight);
 
 		JSeparator separatorWeights = new JSeparator();
 		GridBagConstraints gbc_separatorWeights = new GridBagConstraints();
 		gbc_separatorWeights.insets = new Insets(0, 0, 5, 5);
 		gbc_separatorWeights.fill = GridBagConstraints.HORIZONTAL;
 		gbc_separatorWeights.gridwidth = 2;
 		gbc_separatorWeights.gridx = 4;
 		gbc_separatorWeights.gridy = 5;
 		jPanelQosConstraints.add(separatorWeights, gbc_separatorWeights);
 
 		lblWeightSum = new JLabel("\u03A3     100");
 		lblWeightSum.setForeground(Color.GREEN);
 		GridBagConstraints gbc_lblWeightSum = new GridBagConstraints();
 		gbc_lblWeightSum.insets = new Insets(0, 0, 0, 0);
 		gbc_lblWeightSum.gridx = 4;
 		gbc_lblWeightSum.gridy = 6;
 		gbc_lblWeightSum.anchor = GridBagConstraints.WEST;
 		jPanelQosConstraints.add(lblWeightSum, gbc_lblWeightSum);
 
 		JLabel lblPercentageWeightSum = new JLabel("%");
 		GridBagConstraints gbc_lblPercentageWeightSum = 
 			new GridBagConstraints();
 		gbc_lblPercentageWeightSum.insets = new Insets(0, 0, 0, 0);
 		gbc_lblPercentageWeightSum.gridx = 5;
 		gbc_lblPercentageWeightSum.gridy = 6;
 		jPanelQosConstraints.add(
 				lblPercentageWeightSum, gbc_lblPercentageWeightSum);
 	}
 
 	private void initializeServiceClassesPanel(JPanel contentPane) {
 		JPanel jPanelServiceClasses = new JPanel();
 		GridBagConstraints gbcJPanelServiceClasses = 
 			new GridBagConstraints();
 		gbcJPanelServiceClasses.gridheight = 1;
 		gbcJPanelServiceClasses.insets = new Insets(0, 0, 5, 5);
 		gbcJPanelServiceClasses.fill = GridBagConstraints.BOTH;
 		gbcJPanelServiceClasses.gridx = 1;
 		gbcJPanelServiceClasses.gridy = 0;
 		contentPane.add(jPanelServiceClasses, gbcJPanelServiceClasses);
 		GridBagLayout gblJPanelServiceClasses = new GridBagLayout();
 		gblJPanelServiceClasses.columnWeights = new double[]{1.0};
 		gblJPanelServiceClasses.rowWeights = new double[]{0.0, 1.0};
 		jPanelServiceClasses.setLayout(gblJPanelServiceClasses);
 
 		JLabel jLabelServiceFunctions = new JLabel("Service Classes:");
 		GridBagConstraints gbcJLabelServiceFunctions = 
 			new GridBagConstraints();
 		gbcJLabelServiceFunctions.insets = new Insets(0, 0, 5, 0);
 		gbcJLabelServiceFunctions.gridx = 0;
 		gbcJLabelServiceFunctions.gridy = 0;
 		jPanelServiceClasses.add(
 				jLabelServiceFunctions, gbcJLabelServiceFunctions);
 
 		JScrollPane jScrollPaneServiceClasses = new JScrollPane();
 		GridBagConstraints gbcJScrollPaneServiceClasses = 
 			new GridBagConstraints();
 		gbcJScrollPaneServiceClasses.fill = GridBagConstraints.BOTH;
 		gbcJScrollPaneServiceClasses.gridx = 0;
 		gbcJScrollPaneServiceClasses.gridy = 1;
 		jPanelServiceClasses.add(
 				jScrollPaneServiceClasses, gbcJScrollPaneServiceClasses);
 
 		jTableServiceClasses = new JTable();
 		jTableServiceClasses.setEnabled(false);
 		jTableServiceClasses.setModel(new BasicTableModel(0, 2, false));		
 		jTableServiceClasses.getColumnModel().getColumn(0).setHeaderValue(
 		"ID");
 		jTableServiceClasses.getColumnModel().getColumn(1).setHeaderValue(
 		"Name");
 
 		jScrollPaneServiceClasses.setViewportView(jTableServiceClasses);
 	}
 
 	private void initializeWebServicesPanel(JPanel contentPane) {
 		JPanel jPanelWebServices = new JPanel();
 		GridBagConstraints gbcJPanelWebServices = new GridBagConstraints();
 		gbcJPanelWebServices.gridheight = 1;
 		gbcJPanelWebServices.insets = new Insets(0, 0, 5, 5);
 		gbcJPanelWebServices.fill = GridBagConstraints.BOTH;
 		gbcJPanelWebServices.gridx = 2;
 		gbcJPanelWebServices.gridy = 0;
 		contentPane.add(jPanelWebServices, gbcJPanelWebServices);
 		GridBagLayout gblJPanelWebServices = new GridBagLayout();
 		gblJPanelWebServices.columnWeights = new double[]{1.0};
 		gblJPanelWebServices.rowWeights = new double[]{0.0, 1.0};
 		jPanelWebServices.setLayout(gblJPanelWebServices);
 
 		JLabel jLabelWebServices = new JLabel("Web Services:");
 		GridBagConstraints gbcJLabelWebServices = new GridBagConstraints();
 		gbcJLabelWebServices.insets = new Insets(0, 0, 5, 0);
 		gbcJLabelWebServices.gridx = 0;
 		gbcJLabelWebServices.gridy = 0;
 		jPanelWebServices.add(jLabelWebServices, gbcJLabelWebServices);
 
 		JScrollPane jScrollPaneWebServices = new JScrollPane();
 		jScrollPaneWebServices.setVerticalScrollBarPolicy(
 				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		GridBagConstraints gbcJScrollPaneWebServices = 
 			new GridBagConstraints();
 		gbcJScrollPaneWebServices.fill = GridBagConstraints.BOTH;
 		gbcJScrollPaneWebServices.gridx = 0;
 		gbcJScrollPaneWebServices.gridy = 1;
 		jPanelWebServices.add(
 				jScrollPaneWebServices, gbcJScrollPaneWebServices);
 
 		jTableWebServices = new JTable();
 		jTableWebServices.setEnabled(false);
 		jTableWebServices.setModel(new BasicTableModel(0, 6, false));
 		jTableWebServices.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 		jTableWebServices.getColumnModel().getColumn(0).setHeaderValue(
 				"ServiceClass");
 		jTableWebServices.getColumnModel().getColumn(1).setHeaderValue(
 		"ID");
 		jTableWebServices.getColumnModel().getColumn(2).setHeaderValue(
 		"Name");		
 		jTableWebServices.getColumnModel().getColumn(3).setHeaderValue(
 		"Costs");
 		jTableWebServices.getColumnModel().getColumn(4).setHeaderValue(
 		"ResponseTime");
 		jTableWebServices.getColumnModel().getColumn(5).setHeaderValue(
 		"Availability");		
 		jScrollPaneWebServices.setViewportView(jTableWebServices);
 	}
 
 	private void initializeCenterArea(JPanel contentPane) {
 		JPanel jPanelUtilityFunction = new JPanel();
 		jPanelUtilityFunction.setBorder(new LineBorder(new Color(0, 0, 0)));
 		GridBagConstraints gbcJPanelUtilityFunction = 
 			new GridBagConstraints();
 		gbcJPanelUtilityFunction.gridwidth = 3;
 		gbcJPanelUtilityFunction.insets = new Insets(0, 5, 5, 5);
 		gbcJPanelUtilityFunction.fill = GridBagConstraints.BOTH;
 		gbcJPanelUtilityFunction.gridx = 0;
 		gbcJPanelUtilityFunction.gridy = 2;
 		contentPane.add(jPanelUtilityFunction, gbcJPanelUtilityFunction);
 		GridBagLayout gblJPanelUtilityFunction = new GridBagLayout();
 		gblJPanelUtilityFunction.columnWeights = 
 			new double[]{0.25, 0.5, 0.2, 0.05};
 		gblJPanelUtilityFunction.rowWeights = new double[]{0.33, 0.33, 0.33};
 		jPanelUtilityFunction.setLayout(gblJPanelUtilityFunction);
 
 		Font fontUtilityFunction = 
 			new Font("utilityFunction", Font.BOLD, 16);
 		JLabel jLabelUtilityFunction = new JLabel("Utility Function:");
 		jLabelUtilityFunction.setFont(fontUtilityFunction);
 		GridBagConstraints gbc_lblUtilityFunction = new GridBagConstraints();
 		gbc_lblUtilityFunction.gridheight = 3;
 		gbc_lblUtilityFunction.fill = GridBagConstraints.BOTH;
 		gbc_lblUtilityFunction.anchor = GridBagConstraints.CENTER;
 		gbc_lblUtilityFunction.insets = new Insets(0, 25, 0, 5);
 		gbc_lblUtilityFunction.gridx = 0;
 		gbc_lblUtilityFunction.gridy = 0;
 		jPanelUtilityFunction.add(
 				jLabelUtilityFunction, gbc_lblUtilityFunction);
 		
 		
 		Font fontUtilityText = 
 				new Font("utilityText", Font.ITALIC, 14);
 		jLabelUtilityText = new JLabel();
 		getUtilityFunction();	
 		jLabelUtilityText.setFont(fontUtilityText);
 		GridBagConstraints gbc_lblUtilityText = new GridBagConstraints();
 		gbc_lblUtilityText.gridheight = 1;
 		gbc_lblUtilityText.gridwidth = 2;
 		gbc_lblUtilityText.fill = GridBagConstraints.BOTH;
 		gbc_lblUtilityText.anchor = GridBagConstraints.WEST;
 		gbc_lblUtilityText.insets = new Insets(0, 25, 0, 5);
 		gbc_lblUtilityText.gridx = 1;
 		gbc_lblUtilityText.gridy = 1;
 		jPanelUtilityFunction.add(
 				jLabelUtilityText, gbc_lblUtilityText);
 		 
 		jButtonStart = new JButton("Start");
 		jButtonStart.setEnabled(false);
 		jButtonStart.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				pressStartButton();
 			}
 		});
 		GridBagConstraints gbcJButtonStart = new GridBagConstraints();
 		gbcJButtonStart.gridheight = 3;
 		gbcJButtonStart.fill = GridBagConstraints.VERTICAL;
 		gbcJButtonStart.anchor = GridBagConstraints.EAST;
 		gbcJButtonStart.insets = new Insets(5, 0, 5, 5);
 		gbcJButtonStart.gridx = 3;
 		gbcJButtonStart.gridy = 0;
 		jPanelUtilityFunction.add(jButtonStart, gbcJButtonStart);
 	}
 
 	private void initializeGeneticAlgorithmPanel(JPanel contentPane) {
 		JPanel jPanelGeneticAlgorithm = new JPanel();
 		GridBagLayout gblJPanelGeneticAlgorithm = new GridBagLayout();
 		gblJPanelGeneticAlgorithm.columnWeights = new double[]{1.0};
 		gblJPanelGeneticAlgorithm.rowWeights = 
 			new double[]{0.1, 0.8, 0.1};
 		jPanelGeneticAlgorithm.setLayout(gblJPanelGeneticAlgorithm);
 		GridBagConstraints gbcGeneticAlgorithm = new GridBagConstraints();
 		gbcGeneticAlgorithm.gridheight = 1;
 		gbcGeneticAlgorithm.insets = new Insets(0, 5, 5, 5);
 		gbcGeneticAlgorithm.fill = GridBagConstraints.BOTH;
 		gbcGeneticAlgorithm.gridx = 0;
 		gbcGeneticAlgorithm.gridy = 3;
 		contentPane.add(jPanelGeneticAlgorithm, gbcGeneticAlgorithm);
 
 		jCheckboxGeneticAlgorithm = new JCheckBox("Genetic Algorithm");
 		jCheckboxGeneticAlgorithm.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				chooseAlgorithm("genAlg");
 			}
 		});
 		jCheckboxGeneticAlgorithm.setSelected(true);
 		GridBagConstraints gbcJCheckboxGeneticAlgorithm = 
 			new GridBagConstraints();
 		gbcJCheckboxGeneticAlgorithm.anchor = GridBagConstraints.NORTH;
 		gbcJCheckboxGeneticAlgorithm.insets = new Insets(0, 0, 5, 0);
 		gbcJCheckboxGeneticAlgorithm.gridx = 0;
 		gbcJCheckboxGeneticAlgorithm.gridy = 0;
 		jPanelGeneticAlgorithm.add(
 				jCheckboxGeneticAlgorithm, gbcJCheckboxGeneticAlgorithm);
 
 		JScrollPane jScrollPaneGeneticAlgorithm = new JScrollPane();
 		GridBagConstraints gbcJScrollPaneGeneticAlgorithm = 
 			new GridBagConstraints();
 		gbcJScrollPaneGeneticAlgorithm.insets = new Insets(0, 0, 5, 0);
 		gbcJScrollPaneGeneticAlgorithm.fill = GridBagConstraints.BOTH;
 		gbcJScrollPaneGeneticAlgorithm.gridx = 0;
 		gbcJScrollPaneGeneticAlgorithm.gridy = 1;
 		jPanelGeneticAlgorithm.add(
 				jScrollPaneGeneticAlgorithm, gbcJScrollPaneGeneticAlgorithm);
 
 		JPanel jPanelGeneticAlgorithmSettings = new JPanel();
 		jScrollPaneGeneticAlgorithm.setViewportView(
 				jPanelGeneticAlgorithmSettings);
 		GridBagLayout gblJPanelGeneticAlgorithmSettings = new GridBagLayout();
 		gblJPanelGeneticAlgorithmSettings.columnWeights = 
 			new double[]{0.3, 1.0, 1.0, 1.0};
 		gblJPanelGeneticAlgorithmSettings.rowWeights = 
 			new double[]{0.2, 0.1, 0.2, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0};
 		jPanelGeneticAlgorithmSettings.setLayout(
 				gblJPanelGeneticAlgorithmSettings);
 
 		JLabel jLabelFitnessFunction = new JLabel("Fitness:");
 		GridBagConstraints gbcJLabelFitnessFunction = new GridBagConstraints();
 		gbcJLabelFitnessFunction.insets = new Insets(5, 5, 5, 5);
 		gbcJLabelFitnessFunction.gridheight = 3;
 		gbcJLabelFitnessFunction.anchor = GridBagConstraints.WEST;
 		gbcJLabelFitnessFunction.gridx = 0;
 		gbcJLabelFitnessFunction.gridy = 0;
 		jPanelGeneticAlgorithmSettings.add(
 				jLabelFitnessFunction, gbcJLabelFitnessFunction);
 		
 		JPanel jPanelFitness = new JPanel();
 		GridBagLayout gblJPanelFitness = new GridBagLayout();
 		gblJPanelFitness.columnWeights = new double[] {1.0, 1.0};
 		gblJPanelFitness.rowWeights = new double[] {0.4, 0.2, 0.4};
 		jPanelFitness.setLayout(gblJPanelFitness);
 		GridBagConstraints gbcJPanelFitness = 
 			new GridBagConstraints();
 		gbcJPanelFitness.anchor = GridBagConstraints.WEST;
 		gbcJPanelFitness.insets = new Insets(5, 10, 5, 5);
 		gbcJPanelFitness.gridwidth = 2;
 		gbcJPanelFitness.gridx = 1;
 		gbcJPanelFitness.gridy = 0;
 		jPanelGeneticAlgorithmSettings.add(jPanelFitness, gbcJPanelFitness);
 
 		Font fontFormula = new Font("formula", Font.ITALIC, 10);
 		jLabelGeneticAlgorithmNumerator = new JLabel();
 		GridBagConstraints gbcJLabelNumerator = new GridBagConstraints();
 		gbcJLabelNumerator.insets = new Insets(0, 0, 5, 0);
 		gbcJLabelNumerator.anchor = GridBagConstraints.SOUTH;
 		gbcJLabelNumerator.gridx = 0;
 		gbcJLabelNumerator.gridy = 0;
 		jPanelFitness.add(
 				jLabelGeneticAlgorithmNumerator, gbcJLabelNumerator);
 		jLabelGeneticAlgorithmNumerator.setFont(fontFormula);
 
 		jSeparatorFormula = new JSeparator();
 		GridBagConstraints gbcJSeparatorFormula = new GridBagConstraints();
 		gbcJSeparatorFormula.fill = GridBagConstraints.HORIZONTAL;
 		gbcJSeparatorFormula.gridx = 0;
 		gbcJSeparatorFormula.gridy = 1;
 		jPanelFitness.add(
 				jSeparatorFormula, gbcJSeparatorFormula);
 
 		jLabelGeneticAlgorithmDenominator = new JLabel();
 		GridBagConstraints gbcJLabelDenominator = new GridBagConstraints();
 		gbcJLabelDenominator.insets = new Insets(5, 0, 0, 0);
 		gbcJLabelDenominator.anchor = GridBagConstraints.NORTH;
 		gbcJLabelDenominator.gridx = 0;
 		gbcJLabelDenominator.gridy = 2;
 		jPanelFitness.add(
 				jLabelGeneticAlgorithmDenominator, gbcJLabelDenominator);
 		jLabelGeneticAlgorithmDenominator.setFont(fontFormula);
 
 		jLabelWeightedPenalty = new JLabel();
 		GridBagConstraints gbcJLabelWeightedPenalty = 
 			new GridBagConstraints();
 		gbcJLabelWeightedPenalty.gridx = 1;
 		gbcJLabelWeightedPenalty.gridy = 0;
 		gbcJLabelWeightedPenalty.gridheight = 3;
 		gbcJLabelWeightedPenalty.anchor = GridBagConstraints.WEST;
 		jPanelFitness.add(
 				jLabelWeightedPenalty, gbcJLabelWeightedPenalty);
 		jLabelWeightedPenalty.setFont(fontFormula);
 
 		JLabel jLabelPenaltyFactor = new JLabel("Penalty Factor:");
 		GridBagConstraints gbcJLabelPenaltyFactor = new GridBagConstraints();
 		gbcJLabelPenaltyFactor.anchor = GridBagConstraints.WEST;
 		gbcJLabelPenaltyFactor.insets = new Insets(5, 5, 5, 5);
 		gbcJLabelPenaltyFactor.gridx = 0;
 		gbcJLabelPenaltyFactor.gridy = 3;
 		jPanelGeneticAlgorithmSettings.add(
 				jLabelPenaltyFactor, gbcJLabelPenaltyFactor);
 		
 		JPanel jPanelPenaltyFactor = new JPanel();
 		GridBagLayout gblJPanelPenaltyFactor = new GridBagLayout();
 		gblJPanelPenaltyFactor.columnWeights = new double[] {1.0, 1.0};
 		gblJPanelPenaltyFactor.rowWeights = new double[] {1.0};
 		jPanelPenaltyFactor.setLayout(gblJPanelPenaltyFactor);
 		GridBagConstraints gbcJPanelPenaltyFactor = new GridBagConstraints();
 		gbcJPanelPenaltyFactor.anchor = GridBagConstraints.WEST;
 		gbcJPanelPenaltyFactor.gridwidth = 2;
 		gbcJPanelPenaltyFactor.gridx = 1;
 		gbcJPanelPenaltyFactor.gridy = 3;
 		jPanelGeneticAlgorithmSettings.add(
 				jPanelPenaltyFactor, gbcJPanelPenaltyFactor);
 
 		jTextFieldPenaltyFactor = new JTextField("0");
 		jTextFieldPenaltyFactor.setColumns(3);
 		jTextFieldPenaltyFactor.setHorizontalAlignment(JTextField.RIGHT);
 		jTextFieldPenaltyFactor.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				setPenaltyFactor();
 			}
 		});
 		GridBagConstraints gbcJTextFieldPenaltyFactor = 
 			new GridBagConstraints();
 		gbcJTextFieldPenaltyFactor.insets = new Insets(5, 10, 5, 5);
 		gbcJTextFieldPenaltyFactor.anchor = GridBagConstraints.EAST;
 		gbcJTextFieldPenaltyFactor.gridx = 0;
 		gbcJTextFieldPenaltyFactor.gridy = 0;
 		jPanelPenaltyFactor.add(
 				jTextFieldPenaltyFactor, gbcJTextFieldPenaltyFactor);
 
 		JLabel jLabelPercentagePenalty = new JLabel("%");
 		GridBagConstraints gbcJLabelPercentagePenalty = new GridBagConstraints();
 		gbcJLabelPercentagePenalty.insets = new Insets(5, 0, 5, 5);
 		gbcJLabelPercentagePenalty.anchor = GridBagConstraints.WEST;
 		gbcJLabelPercentagePenalty.gridx = 1;
 		gbcJLabelPercentagePenalty.gridy = 0;
 		jPanelPenaltyFactor.add(jLabelPercentagePenalty, gbcJLabelPercentagePenalty);
 		
 		
 		
 		JLabel jLabelPopulationSize = new JLabel("Population Size:");
 		GridBagConstraints gbcJLabelPopulationSize = 
 			new GridBagConstraints();
 		gbcJLabelPopulationSize.anchor = GridBagConstraints.WEST;
 		gbcJLabelPopulationSize.insets = new Insets(5, 5, 5, 5);
 		gbcJLabelPopulationSize.gridx = 0;
 		gbcJLabelPopulationSize.gridy = 4;
 		jPanelGeneticAlgorithmSettings.add(jLabelPopulationSize, 
 				gbcJLabelPopulationSize);
 		
 		JPanel jPanelPopulationSize = new JPanel();
 		GridBagLayout gblJPanelPopulationSize = new GridBagLayout();
 		gblJPanelPopulationSize.columnWeights = new double[] {1.0, 1.0};
 		gblJPanelPopulationSize.rowWeights = new double[] {1.0};
 		jPanelPopulationSize.setLayout(gblJPanelPopulationSize);
 		GridBagConstraints gbcJPanelPopulationSize = new GridBagConstraints();
 		gbcJPanelPopulationSize.anchor = GridBagConstraints.WEST;
 		gbcJPanelPopulationSize.gridwidth = 2;
 		gbcJPanelPopulationSize.gridx = 1;
 		gbcJPanelPopulationSize.gridy = 4;
 		jPanelGeneticAlgorithmSettings.add(jPanelPopulationSize, 
 				gbcJPanelPopulationSize);
 		
		jTextFieldPopulationSize = new JTextField("100");
 		jTextFieldPopulationSize.setColumns(3);
 		jTextFieldPopulationSize.setHorizontalAlignment(JTextField.RIGHT);
 		jTextFieldPopulationSize.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				checkInputValue(jTextFieldPopulationSize);
 			}
 		});
 		GridBagConstraints gbcJTextFieldPopulationSize = 
 				new GridBagConstraints();
 		gbcJTextFieldPopulationSize.insets = new Insets(5, 10, 5, 5);
 		gbcJTextFieldPopulationSize.anchor = GridBagConstraints.EAST;
 		gbcJTextFieldPopulationSize.gridx = 0;
 		gbcJTextFieldPopulationSize.gridy = 0;
 		jPanelPopulationSize.add(jTextFieldPopulationSize, 
 				gbcJTextFieldPopulationSize);
 
 		jLabelPopulationPercentage = new JLabel();
 		GridBagConstraints gbcJLabelStartPopulationPercentage = 
 			new GridBagConstraints();
 		gbcJLabelStartPopulationPercentage.insets = new Insets(5, 0, 5, 5);
 		gbcJLabelStartPopulationPercentage.anchor = GridBagConstraints.WEST;
 		gbcJLabelStartPopulationPercentage.gridx = 1;
 		gbcJLabelStartPopulationPercentage.gridy = 0;
 		jPanelPopulationSize.add(jLabelPopulationPercentage, 
 				gbcJLabelStartPopulationPercentage);
 		
 		JLabel jLabelSelection = new JLabel("Selection Method:");
 		GridBagConstraints gbcJLabelSelection = new GridBagConstraints();
 		gbcJLabelSelection.anchor = GridBagConstraints.WEST;
 		gbcJLabelSelection.insets = new Insets(5, 5, 5, 5);
 		gbcJLabelSelection.gridx = 0;
 		gbcJLabelSelection.gridy = 5;
 		jPanelGeneticAlgorithmSettings.add(
 				jLabelSelection, gbcJLabelSelection);
 		
 		JPanel jPanelSelection = new JPanel();
 		GridBagLayout gblJPanelSelection = new GridBagLayout();
 		gblJPanelSelection.columnWeights = new double[] {1.0};
 		gblJPanelSelection.rowWeights = new double[] {1.0};
 		jPanelSelection.setLayout(gblJPanelSelection);
 		GridBagConstraints gbcJPanelSelection = 
 			new GridBagConstraints();
 		gbcJPanelSelection.anchor = GridBagConstraints.WEST;
 		gbcJPanelSelection.gridwidth = 2;
 		gbcJPanelSelection.gridx = 1;
 		gbcJPanelSelection.gridy = 5;
 		jPanelGeneticAlgorithmSettings.add(jPanelSelection, gbcJPanelSelection);
 		
 		jComboBoxSelection = new JComboBox<String>();
 		jComboBoxSelection.addItem("Elitism Based");
 		jComboBoxSelection.addItem("Roulette Wheel Selection");
 		jComboBoxSelection.addItem("Tournament Selection");
 		GridBagConstraints gbcJComboBoxSelection = 
 			new GridBagConstraints();
 		gbcJComboBoxSelection.insets = new Insets(5, 10, 5, 5);
 		gbcJComboBoxSelection.anchor = GridBagConstraints.EAST;
 		gbcJComboBoxSelection.gridx = 0;
 		gbcJComboBoxSelection.gridy = 0;
 		jPanelSelection.add(jComboBoxSelection, 
 				gbcJComboBoxSelection);
 		
 		// TODO: Hier ein Text Field einfgen, in das man die Elitism Rate 
 		//		 eintragen kann. Idealerweise wird es nur angezeigt, wenn man 
 		//		 "Elitism Based" ausgewhlt hat. Sollte man fr alle 
 		//		 Selektionsmethoden ein Text Field brauchen, ist es aber 
 		//		 ohnehin egal.
 
 		
 		JLabel jLabelCrossover = new JLabel("Crossover Method:");
 		GridBagConstraints gbcJLabelCrossover = new GridBagConstraints();
 		gbcJLabelCrossover.anchor = GridBagConstraints.WEST;
 		gbcJLabelCrossover.insets = new Insets(5, 5, 5, 5);
 		gbcJLabelCrossover.gridx = 0;
 		gbcJLabelCrossover.gridy = 6;
 		jPanelGeneticAlgorithmSettings.add(
 				jLabelCrossover, gbcJLabelCrossover);
 		
 		JPanel jPanelCrossover = new JPanel();
 		GridBagLayout gblJPanelCrossover = new GridBagLayout();
 		gblJPanelCrossover.columnWeights = new double[] {1.0};
 		gblJPanelCrossover.rowWeights = new double[] {1.0};
 		jPanelCrossover.setLayout(gblJPanelCrossover);
 		GridBagConstraints gbcJPanelCrossover = 
 			new GridBagConstraints();
 		gbcJPanelCrossover.anchor = GridBagConstraints.WEST;
 		gbcJPanelCrossover.gridwidth = 2;
 		gbcJPanelCrossover.gridx = 1;
 		gbcJPanelCrossover.gridy = 6;
 		jPanelGeneticAlgorithmSettings.add(jPanelCrossover, gbcJPanelCrossover);
 		
 		jComboBoxCrossover = new JComboBox<String>();
 		jComboBoxCrossover.addItem("One-Point Crossover");
 		jComboBoxCrossover.addItem("Two-Point Crossover");
 		jComboBoxCrossover.addItem("Uniform Crossover");
 		jComboBoxCrossover.addItem("Half-Uniform Crossover");
 		jComboBoxCrossover.setPreferredSize(
 				jComboBoxSelection.getPreferredSize());
 		GridBagConstraints gbcJComboBoxCrossover = 
 			new GridBagConstraints();
 		gbcJComboBoxCrossover.insets = new Insets(5, 10, 5, 5);
 		gbcJComboBoxCrossover.anchor = GridBagConstraints.EAST;
 		gbcJComboBoxCrossover.gridx = 0;
 		gbcJComboBoxCrossover.gridy = 0;
 		jPanelCrossover.add(jComboBoxCrossover, 
 				gbcJComboBoxCrossover);
 		
 		
 		
 		JLabel jLabelTerminationCriterion = new JLabel(
 				"Termination Criterion:");
 		GridBagConstraints gbcJLabelTerminationCriterion = 
 				new GridBagConstraints();
 		gbcJLabelTerminationCriterion.anchor = GridBagConstraints.WEST;
 		gbcJLabelTerminationCriterion.insets = new Insets(5, 5, 0, 5);
 		gbcJLabelTerminationCriterion.gridx = 0;
 		gbcJLabelTerminationCriterion.gridy = 7;
 		jPanelGeneticAlgorithmSettings.add(
 				jLabelTerminationCriterion, gbcJLabelTerminationCriterion);
 		
 		JPanel jPanelTerminationCriterion = new JPanel();
 		GridBagLayout gblJPanelTerminationCriterion = new GridBagLayout();
 		gblJPanelTerminationCriterion.columnWeights = 
 				new double[] {1.0, 0.1, 1.0};
 		gblJPanelTerminationCriterion.rowWeights = new double[] {1.0};
 		jPanelTerminationCriterion.setLayout(gblJPanelTerminationCriterion);
 		GridBagConstraints gbcJPanelTerminationCriterion = 
 				new GridBagConstraints();
 		gbcJPanelTerminationCriterion.gridwidth = 2;
 		gbcJPanelTerminationCriterion.anchor = GridBagConstraints.WEST;
 		gbcJPanelTerminationCriterion.gridx = 1;
 		gbcJPanelTerminationCriterion.gridy = 7;
 		jPanelGeneticAlgorithmSettings.add(
 				jPanelTerminationCriterion, gbcJPanelTerminationCriterion);
 		
 		jComboBoxTerminationCriterion = new JComboBox<String>();
 		jComboBoxTerminationCriterion.addItem("Max. Number of Iterations");
 		jComboBoxTerminationCriterion.addItem(
 				"Max. Number of Consecutive Equal Generations");
 		jComboBoxTerminationCriterion.addItem(
 				"Min. Improvement per Generation");
 		jComboBoxTerminationCriterion.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				isJLabelTerminationCriterionPercentageVisible();
 			}
 		});
 		GridBagConstraints gbcJComboBoxTerminationCriterion = 
 			new GridBagConstraints();
 		gbcJComboBoxTerminationCriterion.insets = new Insets(5, 10, 5, 5);
 		gbcJComboBoxTerminationCriterion.anchor = GridBagConstraints.EAST;
 		gbcJComboBoxTerminationCriterion.gridx = 0;
 		gbcJComboBoxTerminationCriterion.gridy = 0;
 		jPanelTerminationCriterion.add(jComboBoxTerminationCriterion, 
 				gbcJComboBoxTerminationCriterion);
 		
 		// TODO: Den Doppelpunkt knnte man evtl. sogar wegmachen. Knnte 
 		//		 ohne auch nicht schlecht aussehen.
 		jLabelColon = new JLabel(":");
 		GridBagConstraints gbcJLabelColon = new GridBagConstraints();
 		gbcJLabelColon.insets = new Insets(5, 5, 5, 5);
 		gbcJLabelColon.anchor = GridBagConstraints.WEST;
 		gbcJLabelColon.gridx = 1;
 		gbcJLabelColon.gridy = 0;
 		jPanelTerminationCriterion.add(jLabelColon, gbcJLabelColon);
 		
		jTextFieldTerminationCriterion = new JTextField("100");
 		jTextFieldTerminationCriterion.setColumns(3);
 		jTextFieldTerminationCriterion.setHorizontalAlignment(
 				JTextField.RIGHT);
 		GridBagConstraints gbcJTextFieldTerminationCriterion = 
 			new GridBagConstraints();
 		gbcJTextFieldTerminationCriterion.insets = new Insets(5, 5, 5, 5);
 		gbcJTextFieldTerminationCriterion.anchor = GridBagConstraints.WEST;
 		gbcJTextFieldTerminationCriterion.gridx = 2;
 		gbcJTextFieldTerminationCriterion.gridy = 0;
 		jPanelTerminationCriterion.add(jTextFieldTerminationCriterion, 
 				gbcJTextFieldTerminationCriterion);
 		
 		jLabelTerminationCriterionPercentage = new JLabel("%");
 		jLabelTerminationCriterionPercentage.setVisible(false);
 		GridBagConstraints gbcJLabelTerminationCriterionPercentage = 
 			new GridBagConstraints();
 		gbcJLabelTerminationCriterionPercentage.insets = 
 			new Insets(5, 5, 5, 5);
 		gbcJLabelTerminationCriterionPercentage.anchor = 
 			GridBagConstraints.WEST;
 		gbcJLabelTerminationCriterionPercentage.gridx = 3;
 		gbcJLabelTerminationCriterionPercentage.gridy = 0;
 		jPanelTerminationCriterion.add(jLabelTerminationCriterionPercentage, 
 				gbcJLabelTerminationCriterionPercentage);
 		
 		buildGeneticAlgorithmFitnessFunction();
 		jProgressBarGeneticAlgorithm = new JProgressBar();
 		jProgressBarGeneticAlgorithm.setStringPainted(true);
 		GridBagConstraints gbcJProgressBarGeneticAlgorithm = 
 			new GridBagConstraints();
 		gbcJProgressBarGeneticAlgorithm.fill = GridBagConstraints.HORIZONTAL;
 		gbcJProgressBarGeneticAlgorithm.anchor = GridBagConstraints.SOUTH;
 		gbcJProgressBarGeneticAlgorithm.gridx = 0;
 		gbcJProgressBarGeneticAlgorithm.gridy = 3;
 		jPanelGeneticAlgorithm.add(
 				jProgressBarGeneticAlgorithm, gbcJProgressBarGeneticAlgorithm);
 	}
 
 	private void initializeAntAlgorithmPanel(JPanel contentPane) {
 		JPanel jPanelAntAlgorithm = new JPanel();
 		GridBagConstraints gbcPanelAntAlgorithm = new GridBagConstraints();
 		gbcPanelAntAlgorithm.gridheight = 1;
 		gbcPanelAntAlgorithm.insets = new Insets(0, 5, 5, 5);
 		gbcPanelAntAlgorithm.fill = GridBagConstraints.BOTH;
 		gbcPanelAntAlgorithm.gridx = 1;
 		gbcPanelAntAlgorithm.gridy = 3;
 		contentPane.add(jPanelAntAlgorithm, gbcPanelAntAlgorithm);
 		GridBagLayout gblJPanelAntAlgorithm = new GridBagLayout();
 		gblJPanelAntAlgorithm.columnWeights = new double[]{1.0};
 		gblJPanelAntAlgorithm.rowWeights = new double[]{0.1, 0.8, 0.1};
 		jPanelAntAlgorithm.setLayout(gblJPanelAntAlgorithm);
 
 		jCheckBoxAntColonyOptimization = 
 			new JCheckBox("Ant Colony Optimization Algorithm");
 		jCheckBoxAntColonyOptimization.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				chooseAlgorithm("antAlg");
 			}
 		});
 		jCheckBoxAntColonyOptimization.setSelected(true);
 		GridBagConstraints gbcJCheckBoxAntAlgorithm = 
 			new GridBagConstraints();
 		gbcJCheckBoxAntAlgorithm.anchor = GridBagConstraints.NORTH;
 		gbcJCheckBoxAntAlgorithm.insets = new Insets(0, 0, 5, 0);
 		gbcJCheckBoxAntAlgorithm.gridx = 0;
 		gbcJCheckBoxAntAlgorithm.gridy = 0;
 		jPanelAntAlgorithm.add(jCheckBoxAntColonyOptimization, 
 				gbcJCheckBoxAntAlgorithm);
 
 		JScrollPane jScrollPaneAntAlgorithm = new JScrollPane();
 		GridBagConstraints gbcJScrollPaneAntAlgorithm = 
 			new GridBagConstraints();
 		gbcJScrollPaneAntAlgorithm.insets = new Insets(0, 0, 5, 0);
 		gbcJScrollPaneAntAlgorithm.fill = GridBagConstraints.BOTH;
 		gbcJScrollPaneAntAlgorithm.gridx = 0;
 		gbcJScrollPaneAntAlgorithm.gridy = 1;
 		jPanelAntAlgorithm.add(
 				jScrollPaneAntAlgorithm, gbcJScrollPaneAntAlgorithm);
 			
 		JPanel panelAntAlgorithmSettings = new JPanel();
 		jScrollPaneAntAlgorithm.setViewportView(panelAntAlgorithmSettings);
 		GridBagLayout gbl_panelAntAlgorithmSettings = new GridBagLayout();
 		gbl_panelAntAlgorithmSettings.columnWeights = 
 			new double[]{0.5, 0.5};
 		gbl_panelAntAlgorithmSettings.rowWeights = 
 			new double[]{0.2, 0.16, 0.16, 0.16, 0.16, 0.16};
 		panelAntAlgorithmSettings.setLayout(gbl_panelAntAlgorithmSettings);
 		
 		JLabel jLabelAntIterations = new JLabel("Iterations:");		
 		GridBagConstraints gbcJLabelAntIterations = new GridBagConstraints();
 		gbcJLabelAntIterations.gridwidth = 1;
 		gbcJLabelAntIterations.insets = new Insets(5, 0, 5, 25);
 		gbcJLabelAntIterations.gridx = 0;
 		gbcJLabelAntIterations.gridy = 0;
 		gbcJLabelAntIterations.anchor = GridBagConstraints.EAST;
 		panelAntAlgorithmSettings.add(
 				jLabelAntIterations, gbcJLabelAntIterations);
 		
 		txtAntIterations = new JTextField("100");
 		txtAntIterations.setColumns(5);
 		txtAntIterations.setHorizontalAlignment(JTextField.RIGHT);		
 		GridBagConstraints gbc_AntIterations = new GridBagConstraints();
 		gbc_AntIterations.insets = new Insets(5, 0, 5, 50);
 		gbc_AntIterations.fill = GridBagConstraints.NONE;
 		gbc_AntIterations.gridx = 1;
 		gbc_AntIterations.gridy = 0;
 		panelAntAlgorithmSettings.add(txtAntIterations, gbc_AntIterations);
 		
 		JLabel jLabelAntAnts = new JLabel("Ants:");
 		GridBagConstraints gbcJLabelAntAnts = new GridBagConstraints();
 		gbcJLabelAntAnts.gridwidth = 1;
 		gbcJLabelAntAnts.insets = new Insets(0, 0, 5, 25);
 		gbcJLabelAntAnts.gridx = 0;
 		gbcJLabelAntAnts.gridy = 1;
 		gbcJLabelAntAnts.anchor = GridBagConstraints.EAST;
 		panelAntAlgorithmSettings.add(
 				jLabelAntAnts, gbcJLabelAntAnts);
 		
 		txtAntAnts = new JTextField("10");
 		txtAntAnts.setColumns(5);
 		txtAntAnts.setHorizontalAlignment(JTextField.RIGHT);		
 		GridBagConstraints gbc_AntAnts = new GridBagConstraints();
 		gbc_AntAnts.insets = new Insets(0, 0, 5, 50);
 		gbc_AntAnts.fill = GridBagConstraints.NONE;
 		gbc_AntAnts.gridx = 1;
 		gbc_AntAnts.gridy = 1;
 		panelAntAlgorithmSettings.add(txtAntAnts, gbc_AntAnts);
 		
 		JLabel jLabelAntAlpha = new JLabel("Alpha:");
 		GridBagConstraints gbcJLabelAntAlpha = new GridBagConstraints();
 		gbcJLabelAntAlpha.gridwidth = 1;
 		gbcJLabelAntAlpha.insets = new Insets(0, 0, 5, 25);
 		gbcJLabelAntAlpha.gridx = 0;
 		gbcJLabelAntAlpha.gridy = 2;
 		gbcJLabelAntAlpha.anchor = GridBagConstraints.EAST;
 		panelAntAlgorithmSettings.add(
 				jLabelAntAlpha, gbcJLabelAntAlpha);
 		
 		txtAntAlpha = new JTextField("1.0");
 		txtAntAlpha.setColumns(5);
 		txtAntAlpha.setHorizontalAlignment(JTextField.RIGHT);		
 		GridBagConstraints gbc_AntAlpha = new GridBagConstraints();
 		gbc_AntAlpha.insets = new Insets(0, 0, 5, 50);
 		gbc_AntAlpha.fill = GridBagConstraints.NONE;
 		gbc_AntAlpha.gridx = 1;
 		gbc_AntAlpha.gridy = 2;
 		panelAntAlgorithmSettings.add(txtAntAlpha, gbc_AntAlpha);
 		
 		JLabel jLabelAntBeta = new JLabel("Beta:");
 		GridBagConstraints gbcJLabelAntBeta = new GridBagConstraints();
 		gbcJLabelAntBeta.gridwidth = 1;
 		gbcJLabelAntBeta.insets = new Insets(0, 0, 5, 25);
 		gbcJLabelAntBeta.gridx = 0;
 		gbcJLabelAntBeta.gridy = 3;
 		gbcJLabelAntBeta.anchor = GridBagConstraints.EAST;
 		panelAntAlgorithmSettings.add(
 				jLabelAntBeta, gbcJLabelAntBeta);
 		
 		txtAntBeta = new JTextField("1.0");
 		txtAntBeta.setColumns(5);
 		txtAntBeta.setHorizontalAlignment(JTextField.RIGHT);		
 		GridBagConstraints gbc_AntBeta = new GridBagConstraints();
 		gbc_AntBeta.insets = new Insets(0, 0, 5, 50);
 		gbc_AntBeta.fill = GridBagConstraints.NONE;
 		gbc_AntBeta.gridx = 1;
 		gbc_AntBeta.gridy = 3;
 		panelAntAlgorithmSettings.add(txtAntBeta, gbc_AntBeta);
 		
 		JLabel jLabelAntDilution = new JLabel("Dilution:");
 		GridBagConstraints gbcJLabelAntDilution = new GridBagConstraints();
 		gbcJLabelAntDilution.gridwidth = 1;
 		gbcJLabelAntDilution.insets = new Insets(0, 0, 5, 25);
 		gbcJLabelAntDilution.gridx = 0;
 		gbcJLabelAntDilution.gridy = 4;
 		gbcJLabelAntDilution.anchor = GridBagConstraints.EAST;
 		panelAntAlgorithmSettings.add(
 				jLabelAntDilution, gbcJLabelAntDilution);
 		
 		txtAntDilution = new JTextField("0.1");
 		txtAntDilution.setColumns(5);
 		txtAntDilution.setHorizontalAlignment(JTextField.RIGHT);		
 		GridBagConstraints gbc_AntDilution = new GridBagConstraints();
 		gbc_AntDilution.insets = new Insets(0, 0, 5, 50);
 		gbc_AntDilution.fill = GridBagConstraints.NONE;
 		gbc_AntDilution.gridx = 1;
 		gbc_AntDilution.gridy = 4;
 		panelAntAlgorithmSettings.add(txtAntDilution, gbc_AntDilution);
 		
 		JLabel jLabelAntPi = new JLabel("Pi Init-Value:");
 		GridBagConstraints gbcJLabelAntPi = new GridBagConstraints();
 		gbcJLabelAntPi.gridwidth = 1;
 		gbcJLabelAntPi.insets = new Insets(0, 0, 5, 25);
 		gbcJLabelAntPi.gridx = 0;
 		gbcJLabelAntPi.gridy = 5;
 		gbcJLabelAntPi.anchor = GridBagConstraints.EAST;
 		panelAntAlgorithmSettings.add(
 				jLabelAntPi, gbcJLabelAntPi);
 		
 		txtAntPi = new JTextField("10.0");
 		txtAntPi.setColumns(5);
 		txtAntPi.setHorizontalAlignment(JTextField.RIGHT);		
 		GridBagConstraints gbc_AntPi = new GridBagConstraints();
 		gbc_AntPi.insets = new Insets(0, 0, 5, 50);
 		gbc_AntPi.fill = GridBagConstraints.NONE;
 		gbc_AntPi.gridx = 1;
 		gbc_AntPi.gridy = 5;
 		panelAntAlgorithmSettings.add(txtAntPi, gbc_AntPi);
 
 		jProgressBarAntAlgorithm = new JProgressBar();
 		jProgressBarAntAlgorithm.setStringPainted(true);
 		GridBagConstraints gbcJProgressBarAntAlgorithm = 
 			new GridBagConstraints();
 		gbcJProgressBarAntAlgorithm.fill = GridBagConstraints.HORIZONTAL;
 		gbcJProgressBarAntAlgorithm.anchor = GridBagConstraints.SOUTH;
 		gbcJProgressBarAntAlgorithm.gridx = 0;
 		gbcJProgressBarAntAlgorithm.gridy = 2;
 		jPanelAntAlgorithm.add(
 				jProgressBarAntAlgorithm, gbcJProgressBarAntAlgorithm);
 	}
 
 	private void initializeAnalyticAlgorithmPanel(JPanel contentPane) {
 		JPanel jPanelAnalyticAlgorithm = new JPanel();
 		GridBagConstraints gbcJPanelAnalyticAlgorithm =
 			new GridBagConstraints();
 		gbcJPanelAnalyticAlgorithm.gridheight = 1;
 		gbcJPanelAnalyticAlgorithm.insets = new Insets(0, 5, 5, 5);
 		gbcJPanelAnalyticAlgorithm.fill = GridBagConstraints.BOTH;
 		gbcJPanelAnalyticAlgorithm.gridx = 2;
 		gbcJPanelAnalyticAlgorithm.gridy = 3;
 		contentPane.add(jPanelAnalyticAlgorithm, gbcJPanelAnalyticAlgorithm);
 		GridBagLayout gblJPanelAnalyticAlgorithm = new GridBagLayout();
 		gblJPanelAnalyticAlgorithm.columnWeights = new double[]{1.0};
 		gblJPanelAnalyticAlgorithm.rowWeights = 
 				new double[]{0.1, 0.8, 0.1};
 		jPanelAnalyticAlgorithm.setLayout(gblJPanelAnalyticAlgorithm);
 
 		jCheckBoxAnalyticAlgorithm = 
 			new JCheckBox("Analytic Algorithm");
 		jCheckBoxAnalyticAlgorithm.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				chooseAlgorithm("analyticAlg");
 			}
 		});
 		jCheckBoxAnalyticAlgorithm.setSelected(true);
 		GridBagConstraints gbcJCheckBoxAnalyticAlgorithm = 
 			new GridBagConstraints();
 		gbcJCheckBoxAnalyticAlgorithm.anchor = GridBagConstraints.NORTH;
 		gbcJCheckBoxAnalyticAlgorithm.insets = new Insets(3, 0, 5, 0);
 		gbcJCheckBoxAnalyticAlgorithm.gridx = 0;
 		gbcJCheckBoxAnalyticAlgorithm.gridy = 0;
 		jPanelAnalyticAlgorithm.add(
 				jCheckBoxAnalyticAlgorithm, gbcJCheckBoxAnalyticAlgorithm);
 		
 		JPanel panelAnalyticBody = new JPanel();
 		GridBagLayout gbl_panelAnalyticBody = new GridBagLayout();
 		gbl_panelAnalyticBody.columnWeights = new double[] {1.0, 1.0};
 		gbl_panelAnalyticBody.rowWeights = new double[] {0.8, 0.2};
 		panelAnalyticBody.setLayout(gbl_panelAnalyticBody);
 		GridBagConstraints gbc_panelAnalyticBody = new GridBagConstraints();
 		gbc_panelAnalyticBody.fill = GridBagConstraints.BOTH;
 		gbc_panelAnalyticBody.gridx = 0;
 		gbc_panelAnalyticBody.gridy = 1;
 		jPanelAnalyticAlgorithm.add(
 				panelAnalyticBody, gbc_panelAnalyticBody);
 
 		JScrollPane jScrollPaneAnalyticAlgorithm = new JScrollPane();
 		GridBagConstraints gbcJScrollPaneAnalyticAlgorithm = 
 			new GridBagConstraints();
 		gbcJScrollPaneAnalyticAlgorithm.insets = new Insets(0, 0, 5, 0);
 		gbcJScrollPaneAnalyticAlgorithm.fill = GridBagConstraints.BOTH;
 		gbcJScrollPaneAnalyticAlgorithm.gridwidth = 2;
 		gbcJScrollPaneAnalyticAlgorithm.gridx = 0;
 		gbcJScrollPaneAnalyticAlgorithm.gridy = 0;
 		panelAnalyticBody.add(
 				jScrollPaneAnalyticAlgorithm, gbcJScrollPaneAnalyticAlgorithm);
 
 		jTableAnalyticAlgorithm = new JTable();
 		jTableAnalyticAlgorithm.setEnabled(false);
 		// TODO: Look for a better solution for this listener.
 		//		 But not important as long as only one 
 		//		 analytic algorithm can be selected.
 		jTableAnalyticAlgorithm.addMouseListener(new MouseAdapter() {
 			public void mouseClicked(MouseEvent e) {
 				if (jTableAnalyticAlgorithm.getSelectedRow() == 0 && 
 						jTableAnalyticAlgorithm.getSelectedColumn() == 0) {
 					if (jTableAnalyticAlgorithm.getValueAt(
 							0, 0).equals(true)) {
 						jTableAnalyticAlgorithm.setValueAt(false, 1, 0);
 					}
 					else {
 						jTableAnalyticAlgorithm.setValueAt(true, 1, 0);
 					}
 				}
 				else if (jTableAnalyticAlgorithm.getSelectedRow() == 1 && 
 						jTableAnalyticAlgorithm.getSelectedColumn() == 0) {
 					if (jTableAnalyticAlgorithm.getValueAt(
 							1, 0).equals(true)) {
 						jTableAnalyticAlgorithm.setValueAt(false, 0, 0);
 					}
 					else {
 						jTableAnalyticAlgorithm.setValueAt(true, 0, 0);
 					}
 				}
 			}
 		});
 		jTableAnalyticAlgorithm.setModel(new BasicTableModel(2, 2, true));
 		jTableAnalyticAlgorithm.getColumnModel().getColumn(0).setHeaderValue(
 		"Selection");
 		jTableAnalyticAlgorithm.getColumnModel().getColumn(1).setHeaderValue(
 		"Service Title");
 		jTableAnalyticAlgorithm.setValueAt(true, 0, 0);
 		jTableAnalyticAlgorithm.setValueAt(false, 1, 0);
 		jTableAnalyticAlgorithm.setValueAt("Enumeration", 0, 1);
 		jTableAnalyticAlgorithm.setValueAt("Branch and Bound", 1, 1);
 		jScrollPaneAnalyticAlgorithm.setViewportView(jTableAnalyticAlgorithm);
 		
 		JLabel jLabelResultTiers = new JLabel("Number of Result Tiers:");
 		GridBagConstraints gbcJLabelResultTiers = new GridBagConstraints();
 		gbcJLabelResultTiers.insets = new Insets(0, 5, 5, 5);
 		gbcJLabelResultTiers.anchor = GridBagConstraints.CENTER;
 		gbcJLabelResultTiers.gridx = 0;
 		gbcJLabelResultTiers.gridy = 1;
 		panelAnalyticBody.add(jLabelResultTiers, gbcJLabelResultTiers);
 
 		jSpinnerNumberResultTiers = 
 			new JSpinner(new SpinnerNumberModel(1, 1, 3, 1));
 		((JSpinner.DefaultEditor) jSpinnerNumberResultTiers.getEditor()).
 		getTextField().setEditable(false);
 		jSpinnerNumberResultTiers.setPreferredSize(new Dimension(35, 25));
 		GridBagConstraints gbcJSpinnerNumberResultTiers = 
 			new GridBagConstraints();
 		gbcJSpinnerNumberResultTiers.insets = new Insets(0, 0, 5, 5);
 		gbcJSpinnerNumberResultTiers.anchor = GridBagConstraints.WEST;
 		gbcJSpinnerNumberResultTiers.gridx = 1;
 		gbcJSpinnerNumberResultTiers.gridy = 1;
 		panelAnalyticBody.add(
 				jSpinnerNumberResultTiers, gbcJSpinnerNumberResultTiers);
 
 		jProgressBarAnalyticAlgorithm = new JProgressBar();
 		jProgressBarAnalyticAlgorithm.setStringPainted(true);
 		GridBagConstraints gbcJProgressBarAnalyticAlgorithm = 
 			new GridBagConstraints();
 		gbcJProgressBarAnalyticAlgorithm.fill = GridBagConstraints.HORIZONTAL;
 		gbcJProgressBarAnalyticAlgorithm.anchor = GridBagConstraints.SOUTH;
 		gbcJProgressBarAnalyticAlgorithm.gridwidth = 2;
 		gbcJProgressBarAnalyticAlgorithm.gridx = 0;
 		gbcJProgressBarAnalyticAlgorithm.gridy = 2;
 		jPanelAnalyticAlgorithm.add(
 				jProgressBarAnalyticAlgorithm, 
 				gbcJProgressBarAnalyticAlgorithm);
 	}
 
 	private void initializeTabbedResultsPanel(JPanel contentPane) {
 		jTabbedPane = new JTabbedPane(JTabbedPane.TOP);
 		GridBagConstraints gbcJTabbedPane = new GridBagConstraints();
 		gbcJTabbedPane.gridheight = 1;
 		gbcJTabbedPane.gridwidth = 2;
 		gbcJTabbedPane.insets = new Insets(0, 5, 0, 5);
 		gbcJTabbedPane.fill = GridBagConstraints.BOTH;
 		gbcJTabbedPane.gridx = 0;
 		gbcJTabbedPane.gridy = 6;
 		contentPane.add(jTabbedPane, gbcJTabbedPane);
 		jTabbedPane.setPreferredSize(new Dimension(0, 0));
 	}
 
 	private void initializeGeneralResultsPanel(JPanel contentPane) {
 		JPanel jPanelGeneralResults = new JPanel();
 		GridBagConstraints gbcJPanelGeneralResults = new GridBagConstraints();
 		gbcJPanelGeneralResults.gridheight = 1;
 		gbcJPanelGeneralResults.fill = GridBagConstraints.BOTH;
 		gbcJPanelGeneralResults.gridx = 2;
 		gbcJPanelGeneralResults.gridy = 6;
 		contentPane.add(jPanelGeneralResults, gbcJPanelGeneralResults);
 		GridBagLayout gblJPanelGeneralResults = new GridBagLayout();
 		gblJPanelGeneralResults.columnWeights = new double[]{1.0};
 		gblJPanelGeneralResults.rowWeights = new double[]{1.0};
 		jPanelGeneralResults.setLayout(gblJPanelGeneralResults);
 
 		String[][] generalResultsData = {
 				{"Runtime:", ""},
 				{"     Genetic Algorithm:", ""},
 				{"     Ant Algorithm:", ""},
 				{"     Analytic Algorithm:", ""}
 		};
 		String[] generalResultsColumnNames = {"Variable", "Value"};
 
 		JScrollPane jScrollPaneResults = new JScrollPane();
 		jScrollPaneResults.setVerticalScrollBarPolicy(
 				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		GridBagConstraints gbcJScrollPaneResults = new GridBagConstraints();
 		gbcJScrollPaneResults.insets = new Insets(0, 0, 0, 5);
 		gbcJScrollPaneResults.fill = GridBagConstraints.BOTH;
 		gbcJScrollPaneResults.gridx = 0;
 		gbcJScrollPaneResults.gridy = 0;
 		jPanelGeneralResults.add(jScrollPaneResults, gbcJScrollPaneResults);
 		jTableGeneralResults = new JTable(
 				generalResultsData, generalResultsColumnNames);
 		setColumnWidthRelative(jTableGeneralResults, new double[] {0.8, 0.2});
 		setColumnTextAlignment(jTableGeneralResults, 1, 
 				DefaultTableCellRenderer.RIGHT);
 		jTableGeneralResults.setEnabled(false);
 		jScrollPaneResults.setViewportView(jTableGeneralResults);
 	}
 
 	private void initializeLogPanel(JPanel contentPane) {
 		JScrollPane jScrollPaneLog = new JScrollPane();
 		jScrollPaneLog.setVerticalScrollBarPolicy(
 				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		GridBagConstraints gbcJScrollPaneResults = new GridBagConstraints();
 		gbcJScrollPaneResults.insets = new Insets(0, 5, 0, 5);
 		gbcJScrollPaneResults.fill = GridBagConstraints.BOTH;
 		gbcJScrollPaneResults.gridwidth = 3;
 		gbcJScrollPaneResults.gridx = 0;
 		gbcJScrollPaneResults.gridy = 7;
 		contentPane.add(jScrollPaneLog, gbcJScrollPaneResults);
 		textAreaLog = new JTextArea();
 		textAreaLog.setEditable(false);
 		textAreaLog.setForeground(Color.RED);
 		jScrollPaneLog.setViewportView(textAreaLog);
 	}
 	
 	
 	// Load web services from a CSV file.
 	private void loadWebServices(File file) {
 		// Delete previously loaded web services.
 		serviceCandidatesList.removeAll(serviceCandidatesList);
 		serviceClassesList.removeAll(serviceClassesList);
 
 		BufferedReader bufferedReader = null;
 		try {
 			bufferedReader = new BufferedReader(new FileReader(file));
 
 			// Load service candidates headers.
 			String[] headerArray = bufferedReader.readLine().split(";");
 
 			// Load web services data.
 			String[] serviceCandidateArray;
 			while (bufferedReader.ready()) {
 				serviceCandidateArray = bufferedReader.readLine().split(";");
 				int serviceClassId = Integer.parseInt(
 						serviceCandidateArray[0]);
 				String serviceClassName = serviceCandidateArray[1];
 				int serviceCandidateId = Integer.parseInt(
 						serviceCandidateArray[2]);
 				String name = serviceCandidateArray[3];
 				double costs = Double.parseDouble(serviceCandidateArray[4]);
 				double responseTime = Double.parseDouble(
 						serviceCandidateArray[5]);
 				double availability = Double.parseDouble(
 						serviceCandidateArray[6]);
 
 				// Create and save service candidates.
 				QosVector qosVector = new QosVector(costs, responseTime, 
 						availability);
 				ServiceCandidate serviceCandidate = new ServiceCandidate(
 						serviceClassId, serviceCandidateId, 
 						name, qosVector);
 				serviceCandidatesList.add(serviceCandidate);
 
 				// Create and save service classes. Assign service candidates 
 				// to service classes.
 				boolean serviceClassAlreadyCreated = false;
 				for (ServiceClass serviceClass : serviceClassesList) {
 					if (serviceClass.getServiceClassId() == serviceClassId) {
 						serviceClassAlreadyCreated = true;
 						serviceClass.getServiceCandidateList().add(
 								serviceCandidate);
 						break;
 					}
 				}
 				if (! serviceClassAlreadyCreated) {
 					ServiceClass serviceClass = new ServiceClass(
 							serviceClassId, serviceClassName, 
 							new LinkedList<ServiceCandidate>());
 					serviceClassesList.add(serviceClass);
 					serviceClass.getServiceCandidateList().add(
 							serviceCandidate);
 				}
 			}
 
 			// Write service classes headers.
 			jTableServiceClasses.setModel(new BasicTableModel(
 					serviceClassesList.size(), 2, false));
 			setColumnWidthRelative(jTableServiceClasses, 
 					new double[] {0.3, 0.7});
 			TableColumnModel serviceClassesColumnModel = 
 				jTableServiceClasses.getColumnModel();
 			serviceClassesColumnModel.getColumn(0).setHeaderValue("ID");
 			serviceClassesColumnModel.getColumn(1).setHeaderValue("Name");
 			setColumnTextAlignment(
 					jTableServiceClasses, 0, DefaultTableCellRenderer.CENTER);
 
 			// Write service classes data.
 			for (int k = 0 ; k < serviceClassesList.size() ; k++) {
 				ServiceClass serviceClass = serviceClassesList.get(k);
 				jTableServiceClasses.setValueAt(
 						serviceClass.getServiceClassId(), k, 0);
 				jTableServiceClasses.setValueAt(serviceClass.getName(), k, 1);
 			}
 
 			jTableWebServices.setModel(new BasicTableModel(
 					serviceCandidatesList.size(), 6, false));
 			TableColumnModel webServicesColumnModel = 
 				jTableWebServices.getColumnModel();
 			int innerCount = 0;
 			for (int k = 0 ; k < 6 ; k++) {
 				if (k == 1) { 
 					innerCount++;
 				}
 				webServicesColumnModel.getColumn(k).setHeaderValue(
 						headerArray[innerCount]);
 				innerCount++;
 			}
 			setColumnTextAlignment(
 					jTableWebServices, 0, DefaultTableCellRenderer.CENTER);
 			setColumnTextAlignment(
 					jTableWebServices, 1, DefaultTableCellRenderer.CENTER);
 			for (int count = 3; count < 6; count++) {
 				setColumnTextAlignment(jTableWebServices, count, 
 						DefaultTableCellRenderer.RIGHT);
 			}
 			// Write service candidates data.
 			for (int k = 0 ; k < serviceCandidatesList.size() ; k++) {
 				ServiceCandidate serviceCandidate = 
 					serviceCandidatesList.get(k);
 				QosVector qosVector = serviceCandidate.getQosVector();
 				jTableWebServices.setValueAt(
 						serviceCandidate.getServiceClassId(), k, 0);
 				jTableWebServices.setValueAt(
 						serviceCandidate.getServiceCandidateId(), k, 1);
 				jTableWebServices.setValueAt(serviceCandidate.getName(), k, 2);
 				jTableWebServices.setValueAt(qosVector.getCosts(), k, 3);
 				jTableWebServices.setValueAt(
 						qosVector.getResponseTime(), k, 4);
 				jTableWebServices.setValueAt
 				(qosVector.getAvailability(), k, 5);
 			}
 			webServicesLoaded = true;
 			checkEnableStartButton();
 			setSliderExtremeValues();
 			checkInputValue(jTextFieldPopulationSize);
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				bufferedReader.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void useConstraintSlider(JTextField textfield, JSlider slider) {
 		textfield.setText(String.valueOf(slider.getValue()));
 	}
 
 	private void buildResultTable() {
 		if (jTabbedPane.getTabCount() > 0) {
 			jTabbedPane.removeAll();
 		}
 		Map<String, Algorithm> algorithmsMap = getChosenAlgorithms();
 		if (algorithmsMap == null) {
 			return;
 		}
 		// COUNTER FOR EVERY CHOSEN ALGORITHM
 		for (Map.Entry<String, Algorithm> entry : algorithmsMap.entrySet()) {
 			showAlgorithmResults(entry.getValue(), entry.getKey());
 		}		
 	}
 	
 	private void pressStartButton() {
 		final Map<String, Constraint> constraintsMap = getChosenConstraints();
 		printChosenConstraintsToConsole(constraintsMap);
 		cumulatedRuntime = 0;
 		
 		// Calculate the utility value for all service candidates.
 		for (ServiceCandidate serviceCandidate : serviceCandidatesList) {
 			serviceCandidate.determineUtilityValue(
 					constraintsMap, qosMax, qosMin);
 		}
 		
 		final Thread progressBarThread = new Thread() {
 			@Override
 			public void run() {
 				while(!isInterrupted()) {
 					if (jCheckboxGeneticAlgorithm.isSelected()) {
 						jProgressBarGeneticAlgorithm.setValue(
 								geneticAlgorithm.getWorkPercentage());
 					}
 					if (jCheckBoxAnalyticAlgorithm.isSelected()) {
 						jProgressBarAnalyticAlgorithm.setValue(
 								analyticAlgorithm.getWorkPercentage());
 					}
 					try {
 						sleep(1000);
 					} catch (InterruptedException e) {}
 				}
 			}
 		};
 
 		
 		// Handle the progressbar
 		if (jCheckboxGeneticAlgorithm.isSelected()) {
 			geneticAlgorithm = new GeneticAlgorithm(
 					serviceClassesList, constraintsMap, 
 					Integer.parseInt(jTextFieldPopulationSize.getText()), 
 					Integer.parseInt(jTextFieldTerminationCriterion.getText()),
 					((String) jComboBoxCrossover.getSelectedItem()),
 					((String) jComboBoxTerminationCriterion.
 							getSelectedItem()));
 			jProgressBarGeneticAlgorithm.setValue(0);
 			
 		}
 		if (jCheckBoxAnalyticAlgorithm.isSelected()) {
 			analyticAlgorithm = new AnalyticAlgorithm(
 					serviceClassesList, constraintsMap, 
 					(Integer) jSpinnerNumberResultTiers.getValue());
 			jProgressBarAnalyticAlgorithm.setValue(0);
 		}
 		if (jCheckboxGeneticAlgorithm.isSelected() || 
 				jCheckBoxAnalyticAlgorithm.isSelected()) {
 			progressBarThread.start();
 		}
 		// TODO: Try to find a better solution for enabling the
 		//		 frame (in order to allow cursor definitions)
 		//		 -> idea: global mouse listener (no action performed
 		// 				  if cursor = wait_cursor)
 		// Outsourcing of the calculation 
 		// in order to prevent freezing the gui
 		new SwingWorker<Void, Void>() {
 			@Override
 			protected Void doInBackground() throws Exception {
 				setEnabled(false);
 				jButtonStart.setEnabled(false);
 //				setCursor(new Cursor(Cursor.WAIT_CURSOR));
 				if (jCheckboxGeneticAlgorithm.isSelected()) {
 					doGeneticAlgorithm();
 					progressBarThread.interrupt();
 				}
 				if (jCheckBoxAnalyticAlgorithm.isSelected()) {
 					doEnumeration(constraintsMap);
 				}
 				if (jCheckboxGeneticAlgorithm.isSelected() || 
 						jCheckBoxAnalyticAlgorithm.isSelected()) {
 					progressBarThread.interrupt();
 				}
 				if (jCheckBoxAntColonyOptimization.isSelected()) {
 					doAntAlgorithm(constraintsMap);
 					cumulatedRuntime += antAlgorithm.getRuntime();
 				}
 				// Wait for algorithm results before showing them
 				SwingUtilities.invokeLater(new Runnable() {
 					@Override
 					public void run() {
 						if (cumulatedRuntime > 120000) {
 							jTableGeneralResults.setValueAt(
 									cumulatedRuntime / 60000 + " min", 0, 1);
 						}
 						else if (cumulatedRuntime > 1000) {
 							jTableGeneralResults.setValueAt(
 									cumulatedRuntime / 1000 + " s", 0, 1);
 						}
 						else {
 							jTableGeneralResults.setValueAt(
 									cumulatedRuntime + " ms", 0, 1);
 						}
 						buildResultTable();
 						jButtonVisualize.setEnabled(true);
 						jButtonStart.setEnabled(true);
 						setEnabled(true);
 //						contentPane.setCursor(
 //								new Cursor(Cursor.DEFAULT_CURSOR));
 					}
 				});
 				return null;
 			}
 		}.execute();
 	}
 
 	private void chooseAlgorithm(String algorithm) {
 		if (algorithm.equals("genAlg")) {
 			if (!jCheckboxGeneticAlgorithm.isSelected()) {
 				jTextFieldPenaltyFactor.setEditable(false);
 				jTextFieldPopulationSize.setEditable(false);
 				jTextFieldTerminationCriterion.setEditable(false);
 				jComboBoxCrossover.setEnabled(false);
 				jComboBoxTerminationCriterion.setEnabled(false);
 			}
 			else {
 				jTextFieldPenaltyFactor.setEditable(true);
 				jTextFieldPopulationSize.setEditable(true);
 				jTextFieldTerminationCriterion.setEditable(true);
 				jComboBoxCrossover.setEnabled(true);
 				jComboBoxTerminationCriterion.setEnabled(true);
 			}
 		}
 		else if (algorithm.equals("antAlg")) {
 
 		}
 		else {
 			if (!jCheckBoxAnalyticAlgorithm.isSelected()) {
 				jTableAnalyticAlgorithm.setEnabled(false);
 			}
 			else {
 				jTableAnalyticAlgorithm.setEnabled(true);
 			}
 		}
 	}
 
 	private void buildGeneticAlgorithmFitnessFunction() {
 		int weightCount = 1;
 		String numerator = "";
 		String denominator = "";
 		if (jCheckBoxMaxCosts.isSelected()) {
 			numerator = "w" + weightCount + " * MaxCosts";
 			weightCount++;
 		}
 		if (jCheckBoxMaxResponseTime.isSelected()) {
 			if (numerator.equals("")) {
 				numerator = "w" + weightCount + " * MaxResponseTime";
 			}
 			else {
 				numerator += " + w" + weightCount + " * MaxResponseTime";
 			}
 			weightCount++;
 		}
 		if (jCheckBoxMinAvailability.isSelected()) {
 			denominator = "w" + weightCount + " * MinAvailability ";
 			weightCount++;
 		}
 		if (numerator.equals("")) {
 			numerator = "1";
 		}
 		if (denominator.equals("")) {
 			denominator = "1";
 		}
 		jLabelGeneticAlgorithmNumerator.setText(numerator);
 		jLabelGeneticAlgorithmDenominator.setText(denominator);
 		if (numerator.equals("1") && 
 				denominator.equals("1")) {
 			jLabelGeneticAlgorithmNumerator.setVisible(false);
 			jSeparatorFormula.setVisible(false);
 			jLabelGeneticAlgorithmDenominator.setVisible(false);
 			jLabelWeightedPenalty.setText("w" + weightCount + 
 					" * PenaltyFactor");
 		}
 		else {
 			jLabelGeneticAlgorithmNumerator.setVisible(true);
 			jSeparatorFormula.setVisible(true);
 			jLabelGeneticAlgorithmDenominator.setVisible(true);
 			jLabelWeightedPenalty.setText(" + w" + weightCount + 
 					" * PenaltyFactor");
 		}
 	}
 
 	private void doEnumeration(Map<String, Constraint> constraintsMap) {
 		analyticAlgorithm.start();
 		cumulatedRuntime += analyticAlgorithm.getRuntime();
 		if (analyticAlgorithm.getRuntime() > 120000) {
 			jTableGeneralResults.setValueAt(
 					analyticAlgorithm.getRuntime() / 60000.0 + " min", 3, 1);
 		}
 		else if (analyticAlgorithm.getRuntime() > 1000) {
 			jTableGeneralResults.setValueAt(
 					analyticAlgorithm.getRuntime() / 1000.0 + " s", 3, 1);
 		}
 		else {
 			jTableGeneralResults.setValueAt(
 					analyticAlgorithm.getRuntime() + " ms", 3, 1);
 		}
 	}
 
 	// Sum of elements of double[] columnWidthPercentages has to be 1.
 	private void setColumnWidthRelative(
 			JTable table, double[] columnWidthPercentages) {
 		double tableWidth = table.getPreferredSize().getWidth();
 		for (int count = 0; count < columnWidthPercentages.length; count++) {
 			table.getColumnModel().getColumn(count).setPreferredWidth(
 					(int)((columnWidthPercentages[count] * tableWidth) + 0.5));
 		}
 	}
 
 	private void setColumnTextAlignment(
 			JTable table, int column, int columnAlignment) {
 		DefaultTableCellRenderer defaultRenderer = 
 			new DefaultTableCellRenderer();
 		defaultRenderer.setHorizontalAlignment(columnAlignment);
 		table.getColumnModel().getColumn(column).setCellRenderer(
 				defaultRenderer);
 	}
 
 	private Map<String, Constraint> getChosenConstraints() {
 		Map<String, Constraint> constraintsMap = 
 			new HashMap<String, Constraint>();
 		if (jCheckBoxMaxCosts.isSelected()) {
 			Constraint constraintCosts = new Constraint(Constraint.COSTS, 
 					Double.valueOf(jTextFieldMaxCosts.getText()), 
 					Double.parseDouble(txtCostsWeight.getText()));
 			constraintsMap.put(constraintCosts.getTitle(), constraintCosts);
 		}
 		if (jCheckBoxMaxResponseTime.isSelected()) {
 			Constraint constraintResponseTime = new Constraint(
 					Constraint.RESPONSE_TIME, Double.valueOf(
 							jTextFieldMaxResponseTime.getText()), 
 							Double.parseDouble(
 									txtResponseTimeWeight.getText()));
 			constraintsMap.put(constraintResponseTime.getTitle(), 
 					constraintResponseTime);
 		}
 		if (jCheckBoxMinAvailability.isSelected()) {
 			Constraint constraintAvailability = new Constraint(
 					Constraint.AVAILABILITY, (Double.valueOf(
 							jTextFieldMinAvailability.getText())) / 100.0, 
 							Double.parseDouble(
 									txtAvailabilityWeight.getText()));
 			constraintsMap.put(constraintAvailability.getTitle(), 
 					constraintAvailability);
 		}
 		Constraint constraintPenaltyFactor = new Constraint(
 				Constraint.PENALTY_FACTOR, 0, Double.parseDouble(
 						jTextFieldPenaltyFactor.getText()));
 		constraintsMap.put(constraintPenaltyFactor.getTitle(), 
 				constraintPenaltyFactor);
 		return constraintsMap;
 	}
 
 	private void printChosenConstraintsToConsole(
 			Map<String, Constraint> constraintsMap) {
 		System.out.println("CHOSEN CONSTRAINTS:\n--------------");
 		if (constraintsMap.get(Constraint.COSTS) != null) {
 			System.out.println(constraintsMap.get(Constraint.COSTS));
 		}
 		if (constraintsMap.get(Constraint.RESPONSE_TIME) != null) {
 			System.out.println(constraintsMap.get(Constraint.RESPONSE_TIME));
 		}
 		if (constraintsMap.get(Constraint.AVAILABILITY) != null) {
 			System.out.println(constraintsMap.get(Constraint.AVAILABILITY));
 		}
 	}
 	
 	private void setRandomConstraints() {
 		jSliderMaxCosts.setValue(minCosts + 
 				(int)(Math.random() * (maxCosts - minCosts)));
 		jSliderMaxResponseTime.setValue(minResponseTime + 
 				(int) (Math.random() * (maxResponseTime - minResponseTime)));
 		jSliderMinAvailability.setValue(minAvailability + 
 				(int) (Math.random() * (maxAvailability - minAvailability)));
 	}
 	
 	private void setDefaultConstraints() {
 		jSliderMaxCosts.setValue((maxCosts + minCosts) / 2);
 		jSliderMaxResponseTime.setValue(
 				(maxResponseTime + minResponseTime) / 2);
 		jSliderMinAvailability.setValue(
 				(maxAvailability + minAvailability) / 2);
 	}
 
 	private void resetProgram() {
 		frame.dispose();
 		frame = new MainFrame();
 		frame.setVisible(true);
 	}
 
 	private void changeWeight(JTextField textField) {
 		try {
 			Integer.parseInt(textField.getText());
 		} catch (Exception e) {
 			textField.setText("0");
 		}
 		int cumulatedPercentage = 0;
 		if (jCheckBoxMaxCosts.isSelected()) {
 			cumulatedPercentage += Integer.parseInt(txtCostsWeight.getText());
 		}
 		if (jCheckBoxMaxResponseTime.isSelected()) {
 			cumulatedPercentage += Integer.parseInt(
 					txtResponseTimeWeight.getText());
 		}
 		if (jCheckBoxMinAvailability.isSelected()) {
 			cumulatedPercentage += Integer.parseInt(
 					txtAvailabilityWeight.getText());
 		}
 		lblWeightSum.setText("\u03A3     " + 
 				String.valueOf(cumulatedPercentage));
 		if (cumulatedPercentage != 100) {
 			lblWeightSum.setForeground(Color.RED);
 			correctWeights = false;
 			writeErrorLogEntry(
 					"Sum of active constraint weights has to be 100%");
 		}
 		else {
 			lblWeightSum.setForeground(Color.GREEN);
 			correctWeights = true;
 			getUtilityFunction();
 		}		
 		checkEnableStartButton();
 	}
 
 	private void changeConstraintCheckboxStatus(String constraint) {
 		int lblWeights;
 		if (constraint.equals("Costs")) {
 			jSliderMaxCosts.setEnabled(jCheckBoxMaxCosts.isSelected());
 			jTextFieldMaxCosts.setEditable(jCheckBoxMaxCosts.isSelected());
 			lblWeights = 
 				Integer.parseInt(lblWeightSum.getText().substring(2));
 			lblWeights -= Integer.parseInt(txtCostsWeight.getText());
 			lblWeightSum.setText("\u03A3     " + String.valueOf(lblWeights));
 			txtCostsWeight.setText("0");
 			txtCostsWeight.setEditable(jCheckBoxMaxCosts.isSelected());
 			changeWeight(txtCostsWeight);
 		}
 		else if (constraint.equals("Response Time")) {
 			jSliderMaxResponseTime.setEnabled(
 					jCheckBoxMaxResponseTime.isSelected());
 			jTextFieldMaxResponseTime.setEditable(
 					jCheckBoxMaxResponseTime.isSelected());
 			lblWeights = 
 				Integer.parseInt(lblWeightSum.getText().substring(2));
 			lblWeights -= Integer.parseInt(txtResponseTimeWeight.getText());
 			lblWeightSum.setText("\u03A3     " + String.valueOf(lblWeights));
 			txtResponseTimeWeight.setText("0");
 			txtResponseTimeWeight.setEditable(
 					jCheckBoxMaxResponseTime.isSelected());
 			changeWeight(txtResponseTimeWeight);
 		}
 		else if (constraint.equals("Availability")) {
 			jSliderMinAvailability.setEnabled(
 					jCheckBoxMinAvailability.isSelected());
 			jTextFieldMinAvailability.setEditable(
 					jCheckBoxMinAvailability.isSelected());
 			lblWeights = 
 				Integer.parseInt(lblWeightSum.getText().substring(2));
 			lblWeights -= Integer.parseInt(txtAvailabilityWeight.getText());
 			lblWeightSum.setText("\u03A3    " + String.valueOf(lblWeights));
 			txtAvailabilityWeight.setText("0");
 			txtAvailabilityWeight.setEditable(
 					jCheckBoxMinAvailability.isSelected());
 			changeWeight(txtAvailabilityWeight);
 		}
 	}
 	
 	private void setPenaltyFactor() {
 		try {
 			Integer.parseInt(jTextFieldPenaltyFactor.getText());
 		} catch (Exception e1) {
 			jTextFieldPenaltyFactor.setText("0");
 			writeErrorLogEntry("Input has to be from the type Integer");
 		}
 		if (Integer.parseInt(jTextFieldPenaltyFactor.getText()) < 0 || 
 				Integer.parseInt(jTextFieldPenaltyFactor.getText()) > 100) {
 			jTextFieldPenaltyFactor.setText("0");
 			// TODO: Dass der Penalty Factor weniger als 100% sein muss, ist 
 			//		 evtl. nicht zwingend der Fall. Msste man noch prfen.
 			writeErrorLogEntry("Penalty Factor has to be between 0 and 100%");
 			correctPenalty = false;
 		}
 		else {
 			correctPenalty = true;
 		}
 		checkEnableStartButton();
 	}
 	
 	private void writeErrorLogEntry(String entry) {
 		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss: ");
 		textAreaLog.append("\n" + dateFormat.format(new Date()) + entry);
 	}
 	
 	private void checkEnableStartButton() {
 		if (webServicesLoaded && correctWeights) {
 			if (jCheckboxGeneticAlgorithm.isSelected()) {
 				if (correctPenalty) {
 					jButtonStart.setEnabled(true);
 				}
 				else {
 					jButtonStart.setEnabled(false);
 				}
 			}
 			else {
 				jButtonStart.setEnabled(true);
 			}
 		}
 		else {
 			jButtonStart.setEnabled(false);
 		}
 	}
 	
 	private void setConstraintValueManually(
 			JSlider slider, JTextField textField, int minValue, int maxValue) {
 		int average = (minValue + maxValue) / 2;
 		try {
 			Integer.parseInt(textField.getText());
 		} catch (Exception e) {
 			textField.setText(String.valueOf(average));
 			slider.setValue(average);
 			writeErrorLogEntry("Value has to be from the type Integer!");
 		}
 		if (Integer.parseInt(textField.getText()) < minValue || 
 				Integer.parseInt(textField.getText()) > maxValue) {
 			textField.setText(String.valueOf(average));
 			slider.setValue(average);
 			writeErrorLogEntry("Value has to be between " + 
 					minValue + " and " + maxValue);
 		}
 		else {
 			slider.setValue(Integer.parseInt(textField.getText()));
 		}
 	}
 	
 	// TODO: CHECK SPINNER INPUTS
 	private void loadRandomWebServices() {
 		final JSpinner spinnerNumberOfServiceClasses = new JSpinner(
 				new SpinnerNumberModel(1, 1, 1000, 1));		
 		((JSpinner.DefaultEditor) spinnerNumberOfServiceClasses.getEditor()).
 		getTextField().setHorizontalAlignment(JTextField.CENTER);
 		JSpinner spinnerNumberOfWebServices = new JSpinner(
 				new SpinnerNumberModel(1, 1, 1000, 1));
 		((JSpinner.DefaultEditor) spinnerNumberOfWebServices.getEditor()).
 		getTextField().setEditable(true);
 		((JSpinner.DefaultEditor) spinnerNumberOfWebServices.getEditor()).
 		getTextField().setHorizontalAlignment(JTextField.CENTER);
 		JComponent[] dialogComponents = new JComponent[] {
 				new JLabel("Number of Service Classes:"),
 				spinnerNumberOfServiceClasses,
 				new JLabel("Number of Web Services " +
 						"(per Class):"),
 						spinnerNumberOfWebServices
 		};
 		if (JOptionPane.showConfirmDialog(null, dialogComponents, 
 				"Random Set Properties", 
 				JOptionPane.OK_CANCEL_OPTION, 
 				JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
 			return;
 		}
 		int numberOfServiceClasses = 
 			(Integer) spinnerNumberOfServiceClasses.getValue();
 		int numberOfWebServices = 
 			(Integer) spinnerNumberOfWebServices.getValue();
 		
 		// Delete previously loaded web services.
 		serviceCandidatesList.removeAll(serviceCandidatesList);
 		serviceClassesList.removeAll(serviceClassesList);
 		
 		// Use RandomSetGenerator to create web services data.
 		List<ServiceClass> servicesList = RandomSetGenerator.generateSet(
 				numberOfServiceClasses, numberOfWebServices);
 		serviceClassesList = servicesList;
 		
 		// Write service classes headers.
 		jTableServiceClasses.setModel(new BasicTableModel(
 				serviceClassesList.size(), 2, false));
 		setColumnWidthRelative(jTableServiceClasses, 
 				new double[] {0.3, 0.7});
 		TableColumnModel serviceClassesColumnModel = 
 			jTableServiceClasses.getColumnModel();
 		serviceClassesColumnModel.getColumn(0).setHeaderValue("ID");
 		serviceClassesColumnModel.getColumn(1).setHeaderValue("Name");
 		setColumnTextAlignment(
 				jTableServiceClasses, 0, DefaultTableCellRenderer.CENTER);
 
 		// Write service classes data. Load service candidates into list.
 		for (int k = 0; k < serviceClassesList.size(); k++) {
 			ServiceClass serviceClass = serviceClassesList.get(k);
 			jTableServiceClasses.setValueAt(
 					serviceClass.getServiceClassId(), k, 0);
 			jTableServiceClasses.setValueAt(serviceClass.getName(), k, 1);
 			
 			for (ServiceCandidate serviceCandidate : 
 				serviceClass.getServiceCandidateList()) {
 				serviceCandidatesList.add(serviceCandidate);
 			}
 		}
 
 		// Write service candidates headers.
 		jTableWebServices.setModel(new BasicTableModel(
 				serviceCandidatesList.size(), 6, false));
 		TableColumnModel webServicesColumnModel = 
 			jTableWebServices.getColumnModel();
 		String[] headerArray = new String[] {"Service Class ", "ID", 
 			"Name", "Costs", "ResponseTime", "Availability"};
 		for (int k = 0; k < 6; k++) {
 			webServicesColumnModel.getColumn(k).setHeaderValue(
 					headerArray[k]);
 		}
 		setColumnTextAlignment(
 				jTableWebServices, 0, DefaultTableCellRenderer.CENTER);
 		setColumnTextAlignment(
 				jTableWebServices, 1, DefaultTableCellRenderer.CENTER);
 		for (int count = 4; count < 6; count++) {
 			setColumnTextAlignment(jTableWebServices, count, 
 					DefaultTableCellRenderer.RIGHT);
 		}
 		// Write service candidates data.
 		for (int k = 0; k < serviceCandidatesList.size(); k++) {
 			ServiceCandidate serviceCandidate = 
 				serviceCandidatesList.get(k);
 			QosVector qosVector = serviceCandidate.getQosVector();
 			jTableWebServices.setValueAt(
 					serviceCandidate.getServiceClassId(), k, 0);
 			jTableWebServices.setValueAt(
 					serviceCandidate.getServiceCandidateId(), k, 1);
 			jTableWebServices.setValueAt(serviceCandidate.getName(), k, 2);
 			jTableWebServices.setValueAt(qosVector.getCosts(), k, 3);
 			jTableWebServices.setValueAt(
 					qosVector.getResponseTime(), k, 4);
 			jTableWebServices.setValueAt(qosVector.getAvailability(), k, 5);
 		}
 		webServicesLoaded = true;
 		checkEnableStartButton();
 		setSliderExtremeValues();
 		checkInputValue(jTextFieldPopulationSize);
 	}
 	
 	private Map<String, Algorithm> getChosenAlgorithms() {
 		Map<String, Algorithm> algorithmMap = new HashMap<String, Algorithm>();
 		if (jCheckboxGeneticAlgorithm.isSelected()) {
 			algorithmMap.put("Genetic Algorithm", geneticAlgorithm);
 		}
 		if (jCheckBoxAntColonyOptimization.isSelected()) {
 			algorithmMap.put("Ant Colony Algorithm", antAlgorithm);
 		}
 		if (jCheckBoxAnalyticAlgorithm.isSelected()) {
 			algorithmMap.put("Analytic Algorithm", analyticAlgorithm);
 		}
 		if (algorithmMap.size() == 0) {
 			return null;
 		}
 		return algorithmMap;
 	}
 	
 	private void showAlgorithmResults(Algorithm algorithm, 
 			String algorithmTitle) {
 		
 		int compositionNumber = 1;
 		
 		JScrollPane jScrollPane = new JScrollPane();
 		this.jTabbedPane.addTab(algorithmTitle, jScrollPane);
 
 		JPanel jPanelAlgorithmResult = new JPanel();
 		GridBagLayout gblJPanelAlgorithmResult = new GridBagLayout();
 		gblJPanelAlgorithmResult.columnWeights = new double[]{1.0};
 		double[] rows = 
 			new double[algorithm.getAlgorithmSolutionTiers().size() * 2];
 		if (rows.length == 2) {
 			rows[0] = 0.1;
 			rows[1] = 0.9;
 		}
 		else {
 			for (int rowCount = 0; rowCount < rows.length; rowCount++) {
 				if (rowCount % 2 == 0) {
 					rows[rowCount] = 90.0 / (rows.length / 2 + 1);
 				}
 				else {
 					rows[rowCount] = 10.0 / (rows.length / 2 + 1);
 				}
 			}
 		}
 		
 		gblJPanelAlgorithmResult.rowWeights = rows;
 		jPanelAlgorithmResult.setLayout(gblJPanelAlgorithmResult);
 		jScrollPane.setViewportView(jPanelAlgorithmResult);
 
 
 		// COUNTER FOR ALL TIER TABLES
 		// TODO: Wieso "innerCount"?
 		for (int innerCount = 1; 
 		innerCount < rows.length; innerCount = innerCount + 2) {
 			List<Composition> tierServiceCompositionList = 
 				new LinkedList<Composition>(
 						algorithm.getAlgorithmSolutionTiers().get(
 								innerCount / 2).getServiceCompositionList());
 			int numberOfRows = 0;
 			// COUNTER FOR COMPUTING THE NUMBER OF COMPOSITIONS PER TIER
 			for (int rowCount = 0; rowCount < 
 			tierServiceCompositionList.size(); rowCount++) {
 				numberOfRows += tierServiceCompositionList.get(
 						rowCount).getServiceCandidatesList().size();
 			}
 			
 			// TABLE CONSTRUCTION
 			// TODO: "# Composition" ist berflssig.
 			String[] tierTablesColumnNames = {"# Composition", "# Service", 
 					"Service Title", "Service Class", "Utility Value", "Costs", 
 					"Response Time", "Availability"};
 			JTable jTableTier = new JTable(new BasicTableModel(
 					numberOfRows, tierTablesColumnNames.length, false));
 			GridBagConstraints gbcJTableTier = new GridBagConstraints();
 			gbcJTableTier.gridx = 0;
 			gbcJTableTier.gridy = innerCount;
 			gbcJTableTier.fill = GridBagConstraints.HORIZONTAL;
 			gbcJTableTier.anchor = GridBagConstraints.NORTH;
 			jPanelAlgorithmResult.add(jTableTier, gbcJTableTier);
 			
 			setColumnTextAlignment(
 					jTableTier, 1, DefaultTableCellRenderer.CENTER);
 			setColumnTextAlignment(
 					jTableTier, 3, DefaultTableCellRenderer.CENTER);
 			
 			// COUNTER FOR CONSTRUCTION OF TABLE HEADERS
 			for (int columnCount = 0; columnCount < 
 			tierTablesColumnNames.length; columnCount++) {
 				jTableTier.getColumnModel().getColumn(
 						columnCount).setHeaderValue(
 								tierTablesColumnNames[columnCount]);
 			}
 			if (innerCount == 1) {
 				GridBagConstraints gbc_tableHeader = new GridBagConstraints();
 				gbc_tableHeader.gridx = 0;
 				gbc_tableHeader.gridy = 0;
 				gbc_tableHeader.fill = GridBagConstraints.HORIZONTAL;
 				gbc_tableHeader.anchor = GridBagConstraints.SOUTH;
 				jTableTier.getTableHeader().setVisible(true);
 				jPanelAlgorithmResult.add(
 						jTableTier.getTableHeader(), gbc_tableHeader);
 			}
 			// COUNTER FOR ALL ROWS OF A TIER
 			for (int rowCount = 0; rowCount < numberOfRows; rowCount++) {
 				jTableTier.setValueAt("Composition " + 
 						compositionNumber++, rowCount, 0);
 				jTableTier.setValueAt(
 						DECIMAL_FORMAT_FOUR.format(
 								tierServiceCompositionList.get(
 										rowCount).getUtility()), rowCount, 4);
 				int candidateCount = 0;
 				// COUNTER FOR ALL SERVICE CANDIDATES PER COMPOSITION
 				for (candidateCount = 0; candidateCount < 
 				tierServiceCompositionList.get(rowCount).
 				getServiceCandidatesList().size(); candidateCount++) {
 					// SERVICE CANDIDATE ID
 					jTableTier.setValueAt(
 							tierServiceCompositionList.get(rowCount).
 							getServiceCandidatesList().
 							get(candidateCount).getServiceCandidateId(), 
 							rowCount + candidateCount, 1);
 					// SERVICE CANDIDATE TITLE
 					jTableTier.setValueAt(
 							tierServiceCompositionList.get(rowCount).
 							getServiceCandidatesList().
 							get(candidateCount).getName(), 
 							rowCount + candidateCount, 2);
 					// SERVICE CLASS ID
 					jTableTier.setValueAt(
 							tierServiceCompositionList.get(rowCount).
 							getServiceCandidatesList().
 							get(candidateCount).getServiceClassId(), 
 							rowCount + candidateCount, 3);
 					// COSTS
 					jTableTier.setValueAt(DECIMAL_FORMAT_TWO.format(
 							tierServiceCompositionList.get(rowCount).
 							getServiceCandidatesList().get(
 									candidateCount).getQosVector().
 									getCosts()), 
 									rowCount + candidateCount, 5);
 					// RESPONSE TIME
 					jTableTier.setValueAt(DECIMAL_FORMAT_TWO.format(
 							tierServiceCompositionList.get(rowCount).
 							getServiceCandidatesList().get(
 									candidateCount).getQosVector().
 									getResponseTime()), 
 									rowCount + candidateCount, 6);
 					// AVAILABILITY
 					jTableTier.setValueAt(DECIMAL_FORMAT_TWO.format(
 							tierServiceCompositionList.get(rowCount).
 							getServiceCandidatesList().get(
 									candidateCount).getQosVector().
 									getAvailability()), 
 									rowCount + candidateCount, 7);
 				}
 				rowCount = rowCount + candidateCount - 1;
 				
 				// TODO: Es wre gut, wenn man die aggregierten QoS-Werte fr 
 				// 		 die Composition angezeigt bekommt. Sollte eigentlich 
 				//		 kein Problem sein.
 			}
 			jTableTier.setEnabled(false);
 			
 			if (innerCount + 1 < rows.length) {
 				JSeparator jSeparatorTierTables = new JSeparator();
 				GridBagConstraints gbcJSeparatorTierTables = 
 					new GridBagConstraints();
 				gbcJSeparatorTierTables.gridx = 0;
 				gbcJSeparatorTierTables.gridy = innerCount + 1;
 				gbcJSeparatorTierTables.fill = GridBagConstraints.HORIZONTAL;
 				gbcJSeparatorTierTables.anchor = GridBagConstraints.NORTH;
 				gbcJSeparatorTierTables.insets = new Insets(10, 5, 10, 5);
 				jPanelAlgorithmResult.add(
 						jSeparatorTierTables, gbcJSeparatorTierTables);
 			}
 		}
 	}
 	
 	private void doAntAlgorithm(Map<String, Constraint> constraintsMap) {		
 		int iterations;
 		int ants;
 		double alpha;
 		double beta;
 		double dilution;
 		double piInit;
 		try {
 			iterations = Integer.parseInt(txtAntIterations.getText());
 			ants = Integer.parseInt(txtAntAnts.getText());
 			alpha = Double.parseDouble(txtAntAlpha.getText());
 			beta = Double.parseDouble(txtAntBeta.getText());
 			dilution = Double.parseDouble(txtAntDilution.getText());
 			piInit = Double.parseDouble(txtAntPi.getText());
 		} catch (Exception e) {
 			iterations = 100;
 			ants = 10;
 			alpha = 1;
 			beta = 1;
 			dilution = 0.1;
 			piInit = 10;
 		}
 		antAlgorithm = new AntAlgorithm(
 				serviceClassesList, serviceCandidatesList, constraintsMap,
 				iterations, ants, alpha, beta,
 				dilution, piInit);
 		antAlgorithm.start();        
 		long runtime = antAlgorithm.getRuntime();
 		cumulatedRuntime += antAlgorithm.getRuntime();
 		jTableGeneralResults.setValueAt(runtime + " ms", 2, 1);    
 	} 
 	
 	private QosVector determineQosMax() {
 		QosVector max = new QosVector(0.0, 0.0, 0.0);
 		for (ServiceCandidate serviceCandidate : serviceCandidatesList) {
 			QosVector qos = serviceCandidate.getQosVector();
 			if (qos.getCosts() > max.getCosts()) {
 				max.setCosts(qos.getCosts());
 			}
 			if (qos.getResponseTime() > max.getResponseTime()) {
 				max.setResponseTime(qos.getResponseTime());
 			}
 			if (qos.getAvailability() > max.getAvailability()) {
 				max.setAvailability(qos.getAvailability());
 			}
 		}
 		return max;
 	}
 	
 	private QosVector determineQosMin() {
 		QosVector min = new QosVector(100000.0, 100000.0, 1.0);
 		for (ServiceCandidate serviceCandidate : serviceCandidatesList) {
 			QosVector qos = serviceCandidate.getQosVector();
 			if (qos.getCosts() < min.getCosts()) {
 				min.setCosts(qos.getCosts());
 			}
 			if (qos.getResponseTime() < min.getResponseTime()) {
 				min.setResponseTime(qos.getResponseTime());
 			}
 			if (qos.getAvailability() < min.getAvailability()) {
 				min.setAvailability(qos.getAvailability());
 			}
 		}
 		return min;
 	}
 	
 	private void getUtilityFunction() {
 		String utilityText = "<html>Utility Value(Composition)  =  ";
 		boolean noConstraintsChosen = true;
 		if (jCheckBoxMaxCosts.isSelected()) {
 			utilityText += "(Costs<sub><small>norm</small></sub> * " + 
 			(Double.parseDouble(txtCostsWeight.getText()) / 100.0) + ") + ";
 			noConstraintsChosen = false;
 		}
 		if (jCheckBoxMaxResponseTime.isSelected()) {
 			utilityText += "(Response Time<sub><small>norm</small></sub> * " + 
 			(Double.parseDouble(
 					txtResponseTimeWeight.getText()) / 100.0) + ") + ";
 			noConstraintsChosen = false;
 		}
 		if (jCheckBoxMinAvailability.isSelected()) {
 			utilityText += "(Availability<sub><small>norm</small></sub> * " + 
 			(Double.parseDouble(
 					txtAvailabilityWeight.getText()) / 100.0) + ")";
 			noConstraintsChosen = false;
 		}
 		if (noConstraintsChosen) {
 			utilityText = "";
 		}
 		if (utilityText.endsWith("+ ")) {
 			utilityText = utilityText.substring(0, utilityText.length() - 3);
 		}
 		jLabelUtilityText.setText(utilityText + "</html>");
 	}
 	
 	private void doGeneticAlgorithm() {
 		geneticAlgorithm.start();
 		jProgressBarGeneticAlgorithm.setValue(100);
 		cumulatedRuntime += geneticAlgorithm.getRuntime();
 		if (geneticAlgorithm.getRuntime() > 120000) {
 			jTableGeneralResults.setValueAt(
 					geneticAlgorithm.getRuntime() / 60000.0 + " min", 1, 1);
 		}
 		else if (geneticAlgorithm.getRuntime() > 1000) {
 			jTableGeneralResults.setValueAt(
 					geneticAlgorithm.getRuntime() / 1000.0 + " s", 1, 1);
 		}
 		else {
 			jTableGeneralResults.setValueAt(
 					geneticAlgorithm.getRuntime() + " ms", 1, 1);
 		}
 	}
 	
 	private void checkInputValue(JTextField textField) {
 		if (serviceClassesList == null) { 
 			return;
 		}
 		try {
 			Integer.parseInt(textField.getText());
 		} catch (Exception e1) {
 			textField.setText("1");
 			writeErrorLogEntry("Input has to be from the type Integer");
 		}
 		// TODO: Variable stt sehr schnell an ihre Grenzen. Long bringt auch 
 		//		 eher wenig. berprfung daher nochmal berdenken. Wre 
 		//		 prinzipiell notwendig, sollte aber aufgrund extrem schnell 
 		//		 steigender Zahlen begrenzt werden (z.B. auf 9999).
 		int maxPopulationSize = 1;
 		for (ServiceClass serviceClass : serviceClassesList) {
 			maxPopulationSize *= 
 					serviceClass.getServiceCandidateList().size();
 		}
 		int populationSize = 
 				Integer.parseInt(jTextFieldPopulationSize.getText()) * 100;
 		if (Integer.parseInt(textField.getText()) > maxPopulationSize) {
 			textField.setText(String.valueOf(maxPopulationSize));
 			jLabelPopulationPercentage.setText("( = 100 % )");
 			writeErrorLogEntry(
 					"Input has to be between 1 and " + maxPopulationSize);
 		}
 		else if (Integer.parseInt(textField.getText()) < 1) {
 			textField.setText("1");
 			jLabelPopulationPercentage.setText(
 					"( = " + DECIMAL_FORMAT_FOUR.format(
 							1.0 / maxPopulationSize) + " %)");
 			writeErrorLogEntry(
 					"Input has to be between 1 and " + maxPopulationSize);
 		}
 		else {
 			// TODO: Fraglich, ob das (auer bei kleinen Input-Sets) einen 
 			//		 wirklichen Mehrwert hat.
 			jLabelPopulationPercentage.setText(
 					"( = " + DECIMAL_FORMAT_FOUR.format((double)
 							populationSize / maxPopulationSize) + " % )");
 		}
 	}
 	
 	private void isJLabelTerminationCriterionPercentageVisible() {
 		if (jComboBoxTerminationCriterion.getSelectedIndex() != 2) {
 			jLabelTerminationCriterionPercentage.setVisible(false);
 		}
 		else {
 			jLabelTerminationCriterionPercentage.setVisible(true);
 		}
 	}
 	
 	private void setSliderExtremeValues() {
 //		double maxCosts = 0.0;
 //		double minCosts = 0.0;
 //		double maxResponseTime = 0.0;
 //		double minResponseTime = 0.0;
 //		double maxAvailability = 1.0;
 //		double minAvailability = 1.0;
 //		for (int i = 0; i < serviceClassesList.size(); i++) {
 //			double[] extremeUtilityValues = {
 //				0.0,
 //				Double.MAX_VALUE,
 //				0.0,
 //				Double.MAX_VALUE,
 //				0.0,
 //				Double.MAX_VALUE
 //			};
 //			for (ServiceCandidate candidate : 
 //				serviceClassesList.get(i).getServiceCandidateList()) {
 //				if (extremeUtilityValues[0] < 
 //						candidate.getQosVector().getCosts()) {
 //					extremeUtilityValues[0] = 
 //							candidate.getQosVector().getCosts();
 //				}
 //				if (extremeUtilityValues[1] > 
 //				candidate.getQosVector().getCosts()) {
 //					extremeUtilityValues[1] = 
 //							candidate.getQosVector().getCosts();
 //				}
 //				if (extremeUtilityValues[2] < 
 //						candidate.getQosVector().getResponseTime()) {
 //					extremeUtilityValues[2] = 
 //							candidate.getQosVector().getResponseTime();
 //				}
 //				if (extremeUtilityValues[3] > 
 //				candidate.getQosVector().getResponseTime()) {
 //					extremeUtilityValues[3] = 
 //							candidate.getQosVector().getResponseTime();
 //				}
 //				if (extremeUtilityValues[4] < 
 //						candidate.getQosVector().getAvailability()) {
 //					extremeUtilityValues[4] = 
 //							candidate.getQosVector().getAvailability();
 //				}
 //				if (extremeUtilityValues[5] > 
 //				candidate.getQosVector().getAvailability()) {
 //					extremeUtilityValues[5] = 
 //							candidate.getQosVector().getAvailability();
 //				}
 //			}
 //			maxCosts += extremeUtilityValues[0];
 //			minCosts += extremeUtilityValues[1];
 //			maxResponseTime += extremeUtilityValues[2];
 //			minResponseTime += extremeUtilityValues[3];
 //			maxAvailability *= extremeUtilityValues[4];
 //			minAvailability *= extremeUtilityValues[5];
 //		}
 //		maxAvailability *= 100;
 //		minAvailability *= 100;
 
 		qosMax = determineQosMax();
 		qosMin = determineQosMin();
 		maxCosts = (int) Math.ceil(
 				qosMax.getCosts() * serviceClassesList.size());
 		minCosts = (int) Math.floor(
 				qosMin.getCosts() * serviceClassesList.size());
 		maxResponseTime = (int) Math.ceil(
 				qosMax.getResponseTime() * serviceClassesList.size());
 		minResponseTime = (int) Math.floor(
 				qosMin.getResponseTime() * serviceClassesList.size());
 		maxAvailability = (int) Math.ceil(Math.pow(
 				qosMax.getAvailability(), serviceClassesList.size()) * 100);
 		minAvailability = (int) Math.floor(Math.pow(
 				qosMin.getAvailability(), serviceClassesList.size()) * 100);
 
 		jSliderMaxCosts.setMaximum(maxCosts);
 		jSliderMaxCosts.setValue(
 				(int) Math.round((maxCosts + minCosts) / 2.0));
 		jSliderMaxCosts.setMinimum(minCosts);
 		jSliderMaxResponseTime.setMaximum(maxResponseTime);
 		jSliderMaxResponseTime.setValue(
 				(int) Math.round((maxResponseTime + minResponseTime) / 2.0));
 		jSliderMaxResponseTime.setMinimum(minResponseTime);
 		jSliderMinAvailability.setMaximum(maxAvailability);
 		jSliderMinAvailability.setValue(
 				(int) Math.round((maxAvailability + minAvailability) / 2.0));
 		jSliderMinAvailability.setMinimum(minAvailability);
 	}
 	
 	// TODO: First attempt to visualize the computed results
 	private void showResultVisualization() {
 		int[] serviceCandidatesPerClass = 
 			new int[serviceClassesList.size()];
 		for (int i = 0; i < serviceClassesList.size(); i++) {
 			serviceCandidatesPerClass[i] = 
 				serviceClassesList.get(i).
 				getServiceCandidateList().size();
 		}
 		new GeneticAlgorithmsVisualization(serviceCandidatesPerClass,
 				geneticAlgorithm.getStartPopulationVisualization(), 
 				geneticAlgorithm.getNumberOfDifferentSolutions(), 
 				Integer.parseInt(jTextFieldPopulationSize.getText()));
 	}
 }
