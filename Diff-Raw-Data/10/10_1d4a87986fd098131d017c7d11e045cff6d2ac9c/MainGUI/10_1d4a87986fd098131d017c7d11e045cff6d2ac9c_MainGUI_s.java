 /*
 Copyright 2011-2013 The Cassandra Consortium (cassandra-fp7.eu)
 
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
 
 package eu.cassandra.training.gui;
 
 import java.awt.BorderLayout;
 import java.awt.EventQueue;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import javax.swing.ButtonGroup;
 import javax.swing.DefaultListModel;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JSlider;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.LookAndFeel;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.EtchedBorder;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.CaretEvent;
 import javax.swing.event.CaretListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import org.jfree.chart.ChartPanel;
 
 import eu.cassandra.training.behaviour.BehaviourModel;
 import eu.cassandra.training.entities.Appliance;
 import eu.cassandra.training.entities.Installation;
 import eu.cassandra.training.response.ResponseModel;
 import eu.cassandra.training.utils.ChartUtils;
 import eu.cassandra.training.utils.Constants;
 
 public class MainGUI extends JFrame
 {
 
   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   private JPanel contentPane;
   private final ButtonGroup dataMeasurementsButtonGroup = new ButtonGroup();
   private final ButtonGroup timesDailyButtonGroup = new ButtonGroup();
   private final ButtonGroup startTimeButtonGroup = new ButtonGroup();
   private final ButtonGroup durationButtonGroup = new ButtonGroup();
   private final ButtonGroup responseModelButtonGroup = new ButtonGroup();
   private Installation installation = new Installation();
   private final ButtonGroup powerButtonGroup = new ButtonGroup();
   private double[] basicScheme = new double[Constants.MINUTES_PER_DAY];
   private double[] newScheme = new double[Constants.MINUTES_PER_DAY];
 
   /**
    * Launch the application.
    */
   public static void main (String[] args)
   {
     EventQueue.invokeLater(new Runnable() {
       public void run ()
       {
         try {
           MainGUI frame = new MainGUI();
           frame.setVisible(true);
         }
         catch (Exception e) {
           e.printStackTrace();
         }
       }
     });
   }
 
   /**
    * Create the frame.
    * 
    * @throws UnsupportedLookAndFeelException
    * @throws IllegalAccessException
    * @throws InstantiationException
    * @throws ClassNotFoundException
    */
   public MainGUI () throws ClassNotFoundException, InstantiationException,
     IllegalAccessException, UnsupportedLookAndFeelException,
     FileNotFoundException
   {
     addWindowListener(new WindowAdapter() {
       @Override
       public void windowClosing (WindowEvent e)
       {
         cleanFiles();
         System.exit(0);
       }
     });
     LookAndFeel lnf = new javax.swing.plaf.nimbus.NimbusLookAndFeel();
     UIManager.setLookAndFeel(lnf);
     setTitle("Training Module (BETA)");
     setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     setBounds(100, 100, 1228, 852);
 
     JMenuBar menuBar = new JMenuBar();
     setJMenuBar(menuBar);
 
     JMenu mnNewMenu = new JMenu("File");
     menuBar.add(mnNewMenu);
 
     JMenuItem mntmExit = new JMenuItem("Exit");
     mntmExit.addActionListener(new ActionListener() {
       public void actionPerformed (ActionEvent e)
       {
         cleanFiles();
         System.exit(0);
       }
     });
     mnNewMenu.add(mntmExit);
 
     JMenu mnExit = new JMenu("Help");
     menuBar.add(mnExit);
 
     JMenuItem mntmManual = new JMenuItem("Manual");
     mnExit.add(mntmManual);
 
     JMenuItem mntmAbout = new JMenuItem("About");
     mnExit.add(mntmAbout);
     contentPane = new JPanel();
     contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
     setContentPane(contentPane);
 
     final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
     GroupLayout gl_contentPane = new GroupLayout(contentPane);
     gl_contentPane.setHorizontalGroup(gl_contentPane
             .createParallelGroup(Alignment.LEADING)
             .addComponent(tabbedPane, Alignment.TRAILING,
                           GroupLayout.DEFAULT_SIZE, 1202, Short.MAX_VALUE));
     gl_contentPane.setVerticalGroup(gl_contentPane
             .createParallelGroup(Alignment.LEADING)
             .addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 719,
                           Short.MAX_VALUE));
 
     // TABS //
 
     final JPanel importTab = new JPanel();
     tabbedPane.addTab("Import Data", null, importTab, null);
     tabbedPane.setDisplayedMnemonicIndexAt(0, 0);
     tabbedPane.setEnabledAt(0, true);
     importTab.setLayout(null);
 
     final JPanel trainingTab = new JPanel();
     tabbedPane.addTab("Training Behavior Models", null, trainingTab, null);
     tabbedPane.setDisplayedMnemonicIndexAt(1, 1);
     tabbedPane.setEnabledAt(1, false);
     trainingTab.setLayout(null);
 
     final JPanel createResponseTab = new JPanel();
 
     tabbedPane.addTab("Create Response Models", null, createResponseTab, null);
     tabbedPane.setEnabledAt(2, false);
     createResponseTab.setLayout(null);
 
     final JPanel exportTab = new JPanel();
     tabbedPane.addTab("Export Models", null, exportTab, null);
     tabbedPane.setEnabledAt(3, false);
     exportTab.setLayout(null);
 
     // PANELS //
 
     // DATA IMPORT TAB //
 
     final JPanel dataFilePanel = new JPanel();
     dataFilePanel.setBorder(new TitledBorder(null, "Data File",
                                              TitledBorder.LEADING,
                                              TitledBorder.TOP, null, null));
     dataFilePanel.setBounds(6, 6, 622, 284);
     importTab.add(dataFilePanel);
     dataFilePanel.setLayout(null);
 
     final JPanel disaggregationPanel = new JPanel();
     disaggregationPanel.setLayout(null);
     disaggregationPanel
             .setBorder(new TitledBorder(null, "Disaggregation",
                                         TitledBorder.LEADING, TitledBorder.TOP,
                                         null, null));
     disaggregationPanel.setBounds(629, 6, 567, 284);
     importTab.add(disaggregationPanel);
 
     final JPanel dataReviewPanel = new JPanel();
     dataReviewPanel.setBorder(new TitledBorder(null, "Data Preview",
                                                TitledBorder.LEADING,
                                                TitledBorder.TOP, null, null));
     dataReviewPanel.setBounds(6, 293, 622, 451);
     importTab.add(dataReviewPanel);
     dataReviewPanel.setLayout(new BorderLayout(0, 0));
 
     final JPanel consumptionModelPanel = new JPanel();
     consumptionModelPanel.setBounds(629, 293, 567, 451);
     importTab.add(consumptionModelPanel);
     consumptionModelPanel.setBorder(new TitledBorder(null, "Consumption Model",
                                                      TitledBorder.LEADING,
                                                      TitledBorder.TOP, null,
                                                      null));
     consumptionModelPanel.setLayout(new BorderLayout(0, 0));
 
     // TRAINING BEHAVIOR TAB //
 
     final JPanel trainingParametersPanel = new JPanel();
     trainingParametersPanel.setLayout(null);
     trainingParametersPanel.setBorder(new TitledBorder(null,
                                                        "Training Parameters",
                                                        TitledBorder.LEADING,
                                                        TitledBorder.TOP, null,
                                                        null));
     trainingParametersPanel.setBounds(6, 6, 621, 256);
     trainingTab.add(trainingParametersPanel);
 
     final JPanel applianceSelectionPanel = new JPanel();
     applianceSelectionPanel.setLayout(null);
     applianceSelectionPanel.setBorder(new TitledBorder(null,
                                                        "Appliance Selection",
                                                        TitledBorder.LEADING,
                                                        TitledBorder.TOP, null,
                                                        null));
     applianceSelectionPanel.setBounds(630, 6, 557, 256);
     trainingTab.add(applianceSelectionPanel);
 
     final JPanel distributionPreviewPanel = new JPanel();
     distributionPreviewPanel.setBorder(new TitledBorder(UIManager
             .getBorder("TitledBorder.border"), "Distribution Preview",
                                                         TitledBorder.LEADING,
                                                         TitledBorder.TOP, null,
                                                         null));
     distributionPreviewPanel.setBounds(6, 261, 621, 449);
     trainingTab.add(distributionPreviewPanel);
     distributionPreviewPanel.setLayout(new BorderLayout(0, 0));
 
     final JPanel consumptionPreviewPanel = new JPanel();
     consumptionPreviewPanel
             .setBorder(new TitledBorder(null, "Consumption Model Preview",
                                         TitledBorder.LEADING, TitledBorder.TOP,
                                         null, null));
     consumptionPreviewPanel.setBounds(630, 261, 557, 483);
     trainingTab.add(consumptionPreviewPanel);
     consumptionPreviewPanel.setLayout(new BorderLayout(0, 0));
 
     // RESPONSE MODEL TAB //
 
     final JPanel responseParemetersPanel = new JPanel();
     responseParemetersPanel.setLayout(null);
     responseParemetersPanel.setBorder(new TitledBorder(null,
                                                        "Response Parameters",
                                                        TitledBorder.LEADING,
                                                        TitledBorder.TOP, null,
                                                        null));
     responseParemetersPanel.setBounds(6, 6, 391, 244);
     createResponseTab.add(responseParemetersPanel);
 
     final JPanel behaviorModelSelectionPanel = new JPanel();
     behaviorModelSelectionPanel.setLayout(null);
     behaviorModelSelectionPanel
             .setBorder(new TitledBorder(null, "Behavior Model Selection",
                                         TitledBorder.LEADING, TitledBorder.TOP,
                                         null, null));
     behaviorModelSelectionPanel.setBounds(6, 248, 391, 254);
     createResponseTab.add(behaviorModelSelectionPanel);
 
     final JPanel responsePanel = new JPanel();
     responsePanel.setBorder(new TitledBorder(UIManager
             .getBorder("TitledBorder.border"), "Behavioral Change Preview",
                                              TitledBorder.LEADING,
                                              TitledBorder.TOP, null, null));
     responsePanel.setBounds(401, 6, 786, 438);
     createResponseTab.add(responsePanel);
     responsePanel.setLayout(new BorderLayout(0, 0));
     contentPane.setLayout(gl_contentPane);
 
     final JPanel pricingPreviewPanel = new JPanel();
     pricingPreviewPanel
             .setBorder(new TitledBorder(UIManager
                     .getBorder("TitledBorder.border"),
                                         "Pricing Scheme Preview",
                                         TitledBorder.LEADING, TitledBorder.TOP,
                                         null, null));
     pricingPreviewPanel.setBounds(401, 444, 786, 300);
     createResponseTab.add(pricingPreviewPanel);
     pricingPreviewPanel.setLayout(new BorderLayout(0, 0));
 
     final JPanel pricingSchemePanel = new JPanel();
     pricingSchemePanel.setLayout(null);
     pricingSchemePanel
             .setBorder(new TitledBorder(UIManager
                     .getBorder("TitledBorder.border"),
                                         "Pricing Scheme Selection",
                                         TitledBorder.LEADING, TitledBorder.TOP,
                                         null, null));
     pricingSchemePanel.setBounds(6, 501, 391, 243);
     createResponseTab.add(pricingSchemePanel);
 
     // EXPORT TAB //
 
     JPanel modelExportPanel = new JPanel();
     modelExportPanel.setLayout(null);
     modelExportPanel.setBorder(new TitledBorder(UIManager
             .getBorder("TitledBorder.border"), "Model Export Selection",
                                                 TitledBorder.LEADING,
                                                 TitledBorder.TOP, null, null));
     modelExportPanel.setBounds(10, 11, 596, 267);
     exportTab.add(modelExportPanel);
 
     final JPanel exportPreviewPanel = new JPanel();
     exportPreviewPanel
             .setBorder(new TitledBorder(UIManager
                     .getBorder("TitledBorder.border"), "Export Model Preview",
                                         TitledBorder.LEADING, TitledBorder.TOP,
                                         null, null));
     exportPreviewPanel.setBounds(10, 285, 1177, 425);
     exportTab.add(exportPreviewPanel);
     exportPreviewPanel.setLayout(new BorderLayout(0, 0));
 
     JPanel exportButtonsPanel = new JPanel();
     exportButtonsPanel.setBounds(368, 711, 482, 33);
     exportTab.add(exportButtonsPanel);
 
     JPanel connectionPanel = new JPanel();
     connectionPanel.setLayout(null);
     connectionPanel.setBorder(new TitledBorder(UIManager
             .getBorder("TitledBorder.border"), "Connection Properties",
                                                TitledBorder.LEADING,
                                                TitledBorder.TOP, null, null));
     connectionPanel.setBounds(606, 11, 581, 267);
     exportTab.add(connectionPanel);
 
     // COMPONENTS //
 
     // IMPORT TAB //
 
     // DATA IMPORT //
 
     final JLabel lblSource = new JLabel("Data Source:");
     lblSource.setBounds(23, 47, 71, 16);
     dataFilePanel.add(lblSource);
 
     final JTextField pathField = new JTextField();
     pathField.setEditable(false);
     pathField.setBounds(99, 41, 405, 28);
     dataFilePanel.add(pathField);
     pathField.setColumns(10);
 
     final JButton dataBrowseButton = new JButton("Browse");
     dataBrowseButton.setBounds(516, 41, 87, 28);
     dataFilePanel.add(dataBrowseButton);
 
     final JButton resetButton = new JButton("Reset");
     resetButton.setBounds(516, 81, 87, 28);
     dataFilePanel.add(resetButton);
 
     final JLabel lblDataMeasurementsFrom =
       new JLabel("Data Measurements From:");
     lblDataMeasurementsFrom.setBounds(23, 90, 154, 16);
     dataFilePanel.add(lblDataMeasurementsFrom);
 
     final JRadioButton singleApplianceRadioButton =
       new JRadioButton("Single Appliance");
     singleApplianceRadioButton.setEnabled(false);
     dataMeasurementsButtonGroup.add(singleApplianceRadioButton);
     singleApplianceRadioButton.setBounds(242, 110, 115, 18);
     dataFilePanel.add(singleApplianceRadioButton);
 
     final JRadioButton installationRadioButton =
       new JRadioButton("Installation");
     installationRadioButton.setSelected(true);
     installationRadioButton.setEnabled(false);
     dataMeasurementsButtonGroup.add(installationRadioButton);
     installationRadioButton.setBounds(242, 89, 115, 18);
     dataFilePanel.add(installationRadioButton);
 
     final JLabel labelConsumptionModel = new JLabel("Consumption Model:");
     labelConsumptionModel.setBounds(23, 179, 120, 16);
     dataFilePanel.add(labelConsumptionModel);
 
     final JButton importDataButton = new JButton("Import Data");
     importDataButton.setEnabled(false);
     importDataButton.setBounds(23, 237, 126, 28);
     dataFilePanel.add(importDataButton);
 
     final JButton disaggregateButton = new JButton("Disaggregate");
     disaggregateButton.setEnabled(false);
     disaggregateButton.setBounds(216, 237, 147, 28);
     dataFilePanel.add(disaggregateButton);
 
     final JButton createEventsButton = new JButton("Create Events Dataset");
     createEventsButton.setEnabled(false);
     createEventsButton.setBounds(422, 237, 181, 28);
     dataFilePanel.add(createEventsButton);
 
     final JTextField consumptionPathField = new JTextField();
     consumptionPathField.setEnabled(false);
     consumptionPathField.setEditable(false);
     consumptionPathField.setColumns(10);
     consumptionPathField.setBounds(99, 197, 405, 28);
     dataFilePanel.add(consumptionPathField);
 
     final JButton consumptionBrowseButton = new JButton("Browse");
     consumptionBrowseButton.setEnabled(false);
     consumptionBrowseButton.setBounds(516, 197, 87, 28);
     dataFilePanel.add(consumptionBrowseButton);
 
     JLabel lblTypeOfMeasurements = new JLabel("Type of Measurements");
     lblTypeOfMeasurements.setBounds(23, 141, 154, 16);
     dataFilePanel.add(lblTypeOfMeasurements);
 
     final JRadioButton activePowerRadioButton =
       new JRadioButton("Active Power (P)");
     powerButtonGroup.add(activePowerRadioButton);
     activePowerRadioButton.setSelected(true);
     activePowerRadioButton.setEnabled(false);
     activePowerRadioButton.setBounds(242, 140, 115, 18);
     dataFilePanel.add(activePowerRadioButton);
 
     final JRadioButton activeAndReactivePowerRadioButton =
       new JRadioButton("Active and Reactive Power (P, Q)");
     powerButtonGroup.add(activeAndReactivePowerRadioButton);
     activeAndReactivePowerRadioButton.setEnabled(false);
     activeAndReactivePowerRadioButton.setBounds(242, 161, 262, 18);
     dataFilePanel.add(activeAndReactivePowerRadioButton);
 
     // //////////////////
     // DISAGGREGATION //
     // /////////////////
 
     final JLabel lblAppliancesDetected = new JLabel("Appliances Detected");
     lblAppliancesDetected.setBounds(18, 33, 130, 16);
     disaggregationPanel.add(lblAppliancesDetected);
 
     JScrollPane scrollPane_2 = new JScrollPane();
     scrollPane_2.setBounds(145, 31, 396, 231);
     disaggregationPanel.add(scrollPane_2);
 
     final JList<String> detectedApplianceList = new JList<String>();
     scrollPane_2.setViewportView(detectedApplianceList);
     detectedApplianceList.setEnabled(false);
     detectedApplianceList.setBorder(new EtchedBorder(EtchedBorder.LOWERED,
                                                      null, null));
 
     // ////////////////
     // TRAINING TAB //
     // ////////////////
 
     // TRAINING PARAMETERS //
 
     final JLabel label_1 = new JLabel("Times Per Day");
     label_1.setBounds(19, 40, 103, 16);
     trainingParametersPanel.add(label_1);
 
     final JRadioButton timesHistogramRadioButton =
       new JRadioButton("Histogram");
     timesHistogramRadioButton.setSelected(true);
     timesDailyButtonGroup.add(timesHistogramRadioButton);
     timesHistogramRadioButton.setBounds(160, 38, 87, 18);
     trainingParametersPanel.add(timesHistogramRadioButton);
 
     final JRadioButton timesNormalRadioButton =
       new JRadioButton("Normal Distribution");
     timesDailyButtonGroup.add(timesNormalRadioButton);
     timesNormalRadioButton.setBounds(304, 40, 137, 18);
     trainingParametersPanel.add(timesNormalRadioButton);
 
     JRadioButton timesGaussianRadioButton =
       new JRadioButton("Gaussian Mixture");
     timesDailyButtonGroup.add(timesGaussianRadioButton);
     timesGaussianRadioButton.setBounds(478, 38, 137, 18);
     trainingParametersPanel.add(timesGaussianRadioButton);
 
     final JLabel label_2 = new JLabel("Start Time");
     label_2.setBounds(19, 133, 103, 16);
     trainingParametersPanel.add(label_2);
 
     final JRadioButton startHistogramRadioButton =
       new JRadioButton("Histogram");
     startHistogramRadioButton.setSelected(true);
     startTimeButtonGroup.add(startHistogramRadioButton);
     startHistogramRadioButton.setBounds(160, 131, 87, 18);
     trainingParametersPanel.add(startHistogramRadioButton);
 
     final JRadioButton startNormalRadioButton =
       new JRadioButton("Normal Distribution");
     startTimeButtonGroup.add(startNormalRadioButton);
     startNormalRadioButton.setBounds(304, 133, 137, 18);
     trainingParametersPanel.add(startNormalRadioButton);
 
     final JRadioButton startGaussianRadioButton =
       new JRadioButton("Gaussian Mixture");
     startTimeButtonGroup.add(startGaussianRadioButton);
     startGaussianRadioButton.setBounds(478, 131, 137, 18);
     trainingParametersPanel.add(startGaussianRadioButton);
 
     final JLabel label_3 = new JLabel("Duration");
     label_3.setBounds(19, 86, 103, 16);
     trainingParametersPanel.add(label_3);
 
     final JRadioButton durationHistogramRadioButton =
       new JRadioButton("Histogram");
     durationHistogramRadioButton.setSelected(true);
     durationButtonGroup.add(durationHistogramRadioButton);
     durationHistogramRadioButton.setBounds(160, 84, 87, 18);
     trainingParametersPanel.add(durationHistogramRadioButton);
 
     final JRadioButton durationNormalRadioButton =
       new JRadioButton("Normal Distribution");
     durationButtonGroup.add(durationNormalRadioButton);
     durationNormalRadioButton.setBounds(304, 86, 137, 18);
     trainingParametersPanel.add(durationNormalRadioButton);
 
     final JRadioButton durationGaussianRadioButton =
       new JRadioButton("Gaussian Mixture");
     durationButtonGroup.add(durationGaussianRadioButton);
     durationGaussianRadioButton.setBounds(478, 84, 137, 18);
     trainingParametersPanel.add(durationGaussianRadioButton);
 
     JButton trainingButton = new JButton("Training");
     trainingButton.setBounds(251, 194, 115, 28);
     trainingParametersPanel.add(trainingButton);
 
     // APPLIANCE SELECTION //
 
     final JLabel label_4 = new JLabel("Selected Appliance");
     label_4.setBounds(18, 33, 130, 16);
     applianceSelectionPanel.add(label_4);
 
     JScrollPane scrollPane_1 = new JScrollPane();
     scrollPane_1.setBounds(128, 29, 419, 216);
     applianceSelectionPanel.add(scrollPane_1);
 
     final JList<String> selectedApplianceList = new JList<String>();
     scrollPane_1.setViewportView(selectedApplianceList);
     selectedApplianceList.setEnabled(false);
     selectedApplianceList.setBorder(new EtchedBorder(EtchedBorder.LOWERED,
                                                      null, null));
 
     // DISTRIBUTION SELECTION //
 
     JPanel distributionSelectionPanel = new JPanel();
     distributionSelectionPanel.setBounds(57, 711, 482, 33);
     trainingTab.add(distributionSelectionPanel);
 
     final JButton dailyTimesButton = new JButton("Daily Times");
     dailyTimesButton.setEnabled(false);
     distributionSelectionPanel.add(dailyTimesButton);
 
     final JButton durationButton = new JButton("Duration");
     durationButton.setEnabled(false);
     distributionSelectionPanel.add(durationButton);
 
     final JButton startTimeButton = new JButton("Start Time");
     startTimeButton.setEnabled(false);
     distributionSelectionPanel.add(startTimeButton);
 
     final JButton startTimeBinnedButton = new JButton("Start Time Binned");
     startTimeBinnedButton.setEnabled(false);
     distributionSelectionPanel.add(startTimeBinnedButton);
 
     // /////////////////
     // RESPONSE TAB //
     // ////////////////
 
     // RESPONSE PARAMETERS //
 
     final JLabel label_5 = new JLabel("Monetary Incentive");
     label_5.setBounds(10, 28, 103, 16);
     responseParemetersPanel.add(label_5);
 
     final JSlider monetarySlider = new JSlider();
     monetarySlider.setEnabled(false);
     monetarySlider.setPaintLabels(true);
     monetarySlider.setSnapToTicks(true);
     monetarySlider.setPaintTicks(true);
     monetarySlider.setMinorTickSpacing(10);
     monetarySlider.setMajorTickSpacing(10);
     monetarySlider.setBounds(138, 28, 214, 45);
     responseParemetersPanel.add(monetarySlider);
 
     final JLabel label_6 = new JLabel("Environmental Awareness");
     label_6.setBounds(10, 79, 157, 16);
     responseParemetersPanel.add(label_6);
 
     final JSlider environmentalSlider = new JSlider();
     environmentalSlider.setEnabled(false);
     environmentalSlider.setPaintLabels(true);
     environmentalSlider.setPaintTicks(true);
     environmentalSlider.setMajorTickSpacing(10);
     environmentalSlider.setMinorTickSpacing(10);
     environmentalSlider.setSnapToTicks(true);
     environmentalSlider.setBounds(138, 79, 214, 45);
     responseParemetersPanel.add(environmentalSlider);
 
     final JLabel label_7 = new JLabel("Response Model");
     label_7.setBounds(10, 153, 103, 16);
     responseParemetersPanel.add(label_7);
 
     final JRadioButton bestCaseRadioButton =
       new JRadioButton("Best Case Scenario");
     responseModelButtonGroup.add(bestCaseRadioButton);
     bestCaseRadioButton.setBounds(138, 131, 146, 18);
     responseParemetersPanel.add(bestCaseRadioButton);
 
     final JRadioButton normalCaseRadioButton =
       new JRadioButton("Normal Case Scenario");
     normalCaseRadioButton.setSelected(true);
     responseModelButtonGroup.add(normalCaseRadioButton);
     normalCaseRadioButton.setBounds(138, 152, 157, 18);
     responseParemetersPanel.add(normalCaseRadioButton);
 
     final JRadioButton worstCaseRadioButton =
       new JRadioButton("Worst Case Scenario");
     worstCaseRadioButton.setSelected(true);
     responseModelButtonGroup.add(worstCaseRadioButton);
     worstCaseRadioButton.setBounds(138, 173, 157, 18);
     responseParemetersPanel.add(worstCaseRadioButton);
 
     final JButton previewResponseButton = new JButton("Preview Response Model");
     previewResponseButton.setEnabled(false);
     previewResponseButton.setBounds(24, 198, 157, 28);
     responseParemetersPanel.add(previewResponseButton);
 
     final JButton createResponseButton = new JButton("Create Response Model");
     createResponseButton.setEnabled(false);
     createResponseButton.setBounds(191, 198, 162, 28);
     responseParemetersPanel.add(createResponseButton);
 
     // SELECT BEHAVIOR MODEL //
 
     final JLabel label_8 = new JLabel("Selected Appliance");
     label_8.setBounds(10, 21, 130, 16);
     behaviorModelSelectionPanel.add(label_8);
 
     JScrollPane behaviorListScrollPane = new JScrollPane();
     behaviorListScrollPane.setBounds(10, 48, 365, 195);
     behaviorModelSelectionPanel.add(behaviorListScrollPane);
 
     final JList<String> behaviorSelectList = new JList<String>();
     behaviorListScrollPane.setViewportView(behaviorSelectList);
     behaviorSelectList.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null,
                                                   null));
 
     final JButton commitButton = new JButton("Commit");
     commitButton.setEnabled(false);
     commitButton.setBounds(151, 209, 89, 23);
     pricingSchemePanel.add(commitButton);
 
     JLabel lblBasicSchema = new JLabel("Basic Schema (Start - End - Value)");
     lblBasicSchema.setBounds(10, 18, 177, 14);
     pricingSchemePanel.add(lblBasicSchema);
 
     JLabel lblNewSchemastart = new JLabel("New Schema (Start - End - Value)");
     lblNewSchemastart.setBounds(197, 18, 177, 14);
     pricingSchemePanel.add(lblNewSchemastart);
 
     JScrollPane basicPricingSchemeScrollPane = new JScrollPane();
     basicPricingSchemeScrollPane.setBounds(10, 43, 177, 161);
     pricingSchemePanel.add(basicPricingSchemeScrollPane);
 
     final JTextPane basicPricingSchemePane = new JTextPane();
     basicPricingSchemeScrollPane.setViewportView(basicPricingSchemePane);
     basicPricingSchemePane.setText("00:00-23:59-0.05");
 
     JScrollPane newPricingScrollPane = new JScrollPane();
     newPricingScrollPane.setBounds(197, 43, 177, 161);
     pricingSchemePanel.add(newPricingScrollPane);
 
     final JTextPane newPricingSchemePane = new JTextPane();
 
     newPricingScrollPane.setViewportView(newPricingSchemePane);
 
     // //////////////////
     // EXPORT TAB ///////
     // /////////////////
 
     JLabel exportModelLabel = new JLabel("Select Model");
     exportModelLabel.setBounds(10, 34, 151, 16);
     modelExportPanel.add(exportModelLabel);
 
     JScrollPane scrollPane = new JScrollPane();
     scrollPane.setBounds(171, 32, 415, 212);
     modelExportPanel.add(scrollPane);
 
     final JList<String> exportModelList = new JList<String>();
     scrollPane.setViewportView(exportModelList);
     exportModelList.setEnabled(false);
     exportModelList
             .setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
 
     // EXPORT TAB //
 
     final JButton exportDailyButton = new JButton("Daily Times");
     exportDailyButton.setEnabled(false);
     exportButtonsPanel.add(exportDailyButton);
 
     final JButton exportDurationButton = new JButton("Duration");
     exportDurationButton.setEnabled(false);
     exportButtonsPanel.add(exportDurationButton);
 
     final JButton exportStartButton = new JButton("Start Time");
     exportStartButton.addActionListener(new ActionListener() {
       public void actionPerformed (ActionEvent arg0)
       {
       }
     });
     exportStartButton.setEnabled(false);
     exportButtonsPanel.add(exportStartButton);
 
     final JButton exportStartBinnedButton = new JButton("Start Time Binned");
     exportStartBinnedButton.setEnabled(false);
     exportButtonsPanel.add(exportStartBinnedButton);
 
     JLabel usernameLabel = new JLabel("Username:");
     usernameLabel.setBounds(46, 27, 71, 16);
     connectionPanel.add(usernameLabel);
 
     final JTextField usernameTextField;
     usernameTextField = new JTextField();
     usernameTextField.setText("antonis");
     usernameTextField.setColumns(10);
     usernameTextField.setBounds(122, 21, 405, 28);
     connectionPanel.add(usernameTextField);
 
     final JButton exportButton = new JButton("Export");
     exportButton.setEnabled(false);
     exportButton.setBounds(201, 217, 147, 28);
     connectionPanel.add(exportButton);
 
     final JButton exportAllButton = new JButton("Export All");
     exportAllButton.setEnabled(false);
     exportAllButton.setBounds(390, 217, 181, 28);
     connectionPanel.add(exportAllButton);
 
     JLabel passwordLabel = new JLabel("Password:");
     passwordLabel.setBounds(46, 89, 71, 16);
     connectionPanel.add(passwordLabel);
 
     JLabel UrlLabel = new JLabel("URL:");
     UrlLabel.setBounds(46, 153, 71, 16);
     connectionPanel.add(UrlLabel);
 
     final JTextField urlTextField;
     urlTextField = new JTextField();
     urlTextField.setText("https://xant.ee.auth.gr:8443/cassandra/api/usr");
     urlTextField.setColumns(10);
     urlTextField.setBounds(122, 147, 405, 28);
     connectionPanel.add(urlTextField);
 
     final JButton connectButton = new JButton("Connect");
     connectButton.setBounds(30, 217, 147, 28);
     connectionPanel.add(connectButton);
 
     final JPasswordField passwordField;
     passwordField = new JPasswordField();
     passwordField.setBounds(122, 87, 405, 28);
     connectionPanel.add(passwordField);
 
     // //////////////////
     // ACTIONS ///////
     // /////////////////
 
     // IMPORT TAB //
 
     // DATA IMPORT ////
 
     dataBrowseButton.addActionListener(new ActionListener() {
       public void actionPerformed (ActionEvent e)
       {
 
         JFileChooser fc = new JFileChooser("./");
 
         fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
         fc.setFileFilter(new MyFilter2());
 
         int returnVal = fc.showOpenDialog(contentPane);
 
         if (returnVal == JFileChooser.APPROVE_OPTION) {
           File file = fc.getSelectedFile();
           // This is where a real application would open the file.
           pathField.setText(file.getAbsolutePath());
           importDataButton.setEnabled(true);
           activePowerRadioButton.setEnabled(true);
           activeAndReactivePowerRadioButton.setEnabled(true);
           installationRadioButton.setEnabled(true);
           singleApplianceRadioButton.setEnabled(true);
         }
 
       }
     });
 
     consumptionBrowseButton.addActionListener(new ActionListener() {
       public void actionPerformed (ActionEvent e)
       {
         JFileChooser fc = new JFileChooser("./");
 
         fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
         fc.setFileFilter(new MyFilter());
 
         int returnVal = fc.showOpenDialog(contentPane);
 
         if (returnVal == JFileChooser.APPROVE_OPTION) {
           File file = fc.getSelectedFile();
           // This is where a real application would open the file.
           consumptionPathField.setText(file.getAbsolutePath());
           createEventsButton.setEnabled(true);
         }
 
       }
     });
 
     resetButton.addActionListener(new ActionListener() {
       public void actionPerformed (ActionEvent e)
       {
         pathField.setText("");
         consumptionPathField.setText("");
         importDataButton.setEnabled(false);
         disaggregateButton.setEnabled(false);
         createEventsButton.setEnabled(false);
         installation = new Installation();
         dataBrowseButton.setEnabled(true);
         consumptionBrowseButton.setEnabled(false);
         installationRadioButton.setEnabled(false);
         installationRadioButton.setSelected(true);
         singleApplianceRadioButton.setEnabled(false);
         activePowerRadioButton.setSelected(true);
         activePowerRadioButton.setEnabled(false);
         activeAndReactivePowerRadioButton.setEnabled(false);
         dataReviewPanel.removeAll();
         dataReviewPanel.updateUI();
         consumptionModelPanel.removeAll();
         consumptionModelPanel.updateUI();
         detectedApplianceList.setListData(new String[0]);
         detectedApplianceList.repaint();
 
         distributionPreviewPanel.removeAll();
         distributionPreviewPanel.updateUI();
         consumptionPreviewPanel.removeAll();
         consumptionPreviewPanel.updateUI();
         selectedApplianceList.setListData(new String[0]);
         selectedApplianceList.repaint();
 
         monetarySlider.setValue(50);
         environmentalSlider.setValue(50);
         normalCaseRadioButton.setSelected(true);
         previewResponseButton.setEnabled(false);
         createResponseButton.setEnabled(false);
         pricingPreviewPanel.removeAll();
         pricingPreviewPanel.updateUI();
         responsePanel.removeAll();
         responsePanel.updateUI();
         behaviorSelectList.setListData(new String[0]);
         behaviorSelectList.repaint();
         basicPricingSchemePane.setText("00:00-23:59-0.05");
         newPricingSchemePane.setText("");
         commitButton.setEnabled(false);
 
         exportModelList.setListData(new String[0]);
         exportModelList.repaint();
         exportPreviewPanel.removeAll();
         exportPreviewPanel.updateUI();
         exportDailyButton.setEnabled(false);
         exportDurationButton.setEnabled(false);
         exportStartButton.setEnabled(false);
         exportStartBinnedButton.setEnabled(false);
         exportButton.setEnabled(false);
         exportAllButton.setEnabled(false);
 
         tabbedPane.setEnabledAt(1, false);
         tabbedPane.setEnabledAt(2, false);
         tabbedPane.setEnabledAt(3, false);
 
         cleanFiles();
 
       }
     });
 
     singleApplianceRadioButton.addActionListener(new ActionListener() {
       public void actionPerformed (ActionEvent e)
       {
         consumptionPathField.setEnabled(false);
         consumptionBrowseButton.setEnabled(false);
         consumptionPathField.setText("");
       }
     });
 
     installationRadioButton.addActionListener(new ActionListener() {
       public void actionPerformed (ActionEvent e)
       {
         consumptionPathField.setEnabled(false);
         consumptionBrowseButton.setEnabled(false);
         consumptionPathField.setText("");
       }
     });
 
     importDataButton.addActionListener(new ActionListener() {
       public void actionPerformed (ActionEvent e)
       {
 
         disaggregateButton.setEnabled(false);
         createEventsButton.setEnabled(false);
 
         if (installationRadioButton.isSelected()) {
           disaggregateButton.setEnabled(true);
         }
         else if (singleApplianceRadioButton.isSelected()) {
           consumptionPathField.setEnabled(true);
           consumptionBrowseButton.setEnabled(true);
         }
 
         installationRadioButton.setEnabled(false);
         singleApplianceRadioButton.setEnabled(false);
         importDataButton.setEnabled(false);
         dataBrowseButton.setEnabled(false);
         activePowerRadioButton.setEnabled(false);
         activeAndReactivePowerRadioButton.setEnabled(false);
 
         boolean power = activePowerRadioButton.isSelected();
         try {
           installation = new Installation(pathField.getText(), power);
         }
         catch (IOException e2) {
           e2.printStackTrace();
         }
 
         ChartPanel chartPanel = null;
         try {
           chartPanel = installation.measurementsChart(power);
         }
         catch (IOException e1) {
           e1.printStackTrace();
         }
 
         dataReviewPanel.add(chartPanel, BorderLayout.CENTER);
         dataReviewPanel.validate();
 
       }
     });
 
     disaggregateButton.addActionListener(new ActionListener() {
       public void actionPerformed (ActionEvent e)
       {
 
         // TODO Stuff done by disaggregation
 
         int temp = 10 + ((int) (Math.random() * 2));
 
         DefaultListModel<String> dlm = new DefaultListModel<String>();
 
         for (int i = 0; i < temp; i++) {
 
           String name = "Appliance " + i;
           String conModel = "";
           switch (i % 3) {
           case 0:
             conModel =
               "{ \"n\" : 0, \"params\" : [{ \"n\" : 1, \"values\" : [ {\"p\" : 140.0, \"d\" : 20, \"s\": 0.0}, {\"p\" : 117.0, \"d\" : 18, \"s\": 0.0}, {\"p\" : 0.0, \"d\" : 73, \"s\": 0.0}]},{ \"n\" : 1, \"values\" : [ {\"p\" : 14.0, \"d\" : 20, \"s\": 0.0}, {\"p\" : 11.0, \"d\" : 18, \"s\": 0.0}, {\"p\" : 5.0, \"d\" : 73, \"s\": 0.0}]}]}";
             break;
           case 1:
             conModel =
               "{ \"n\" : 0, \"params\" : [{ \"n\" : 1, \"values\" : [ {\"p\" : 140.0, \"d\" : 20, \"s\": 0.0}]}]}";
             break;
           case 2:
             conModel =
               "{ \"n\" : 0, \"params\" : [{ \"n\" : 1, \"values\" : [ {\"p\" : 140.0, \"d\" : 20, \"s\": 0.0}, {\"p\" : 117.0, \"d\" : 18, \"s\": 0.0}, {\"p\" : 0.0, \"d\" : 73, \"s\": 0.0}]},{ \"n\" : 1, \"values\" : [ {\"p\" : 14.0, \"d\" : 20, \"s\": 0.0}, {\"p\" : 11.0, \"d\" : 18, \"s\": 0.0}, {\"p\" : 355.0, \"d\" : 73, \"s\": 0.0}]}]}";
             break;
           }
 
           double[] mesTemp = new double[100];
           double[] mesTemp2 = new double[100];
 
           for (int j = 0; j < mesTemp.length; j++) {
             mesTemp[j] = Math.random() * 100;
             mesTemp2[j] = Math.random() * 100;
           }
 
           Appliance tempAppliance =
             new Appliance(installation.getName() + " " + name, conModel,
                          "eventsAll11.csv", mesTemp, mesTemp2);
 
           installation.addAppliance(tempAppliance);
           dlm.addElement(tempAppliance.getName());
 
         }
 
         detectedApplianceList.setEnabled(true);
         detectedApplianceList.setModel(dlm);
         detectedApplianceList.setSelectedIndex(0);
 
         tabbedPane.setEnabledAt(1, true);
         selectedApplianceList.setEnabled(true);
         selectedApplianceList.setModel(dlm);
 
         exportModelList.setEnabled(true);
         exportModelList.setModel(dlm);
         tabbedPane.setEnabledAt(3, true);
 
         disaggregateButton.setEnabled(false);
         createEventsButton.setEnabled(false);
 
       }
     });
 
     createEventsButton.addActionListener(new ActionListener() {
       public void actionPerformed (ActionEvent e)
       {
 
         DefaultListModel<String> dlm = new DefaultListModel<String>();
 
         File file = new File(consumptionPathField.getText());
         String temp = file.getName();
         temp = temp.replace(".", " ");
         String name = temp.split(" ")[0];
 
         Appliance appliance = null;
         try {
           appliance =
             new Appliance(installation.getName() + " " + name,
                          consumptionPathField.getText(), "eventsAll11.csv",
                          installation, activePowerRadioButton.isSelected());
         }
         catch (IOException e1) {
           e1.printStackTrace();
         }
 
         installation.addAppliance(appliance);
 
         dlm.addElement(appliance.getName());
         detectedApplianceList.setEnabled(true);
         detectedApplianceList.setModel(dlm);
         detectedApplianceList.setSelectedIndex(0);
 
         tabbedPane.setEnabledAt(1, true);
         selectedApplianceList.setEnabled(true);
         selectedApplianceList.setModel(dlm);
 
         exportModelList.setEnabled(true);
         exportModelList.setModel(dlm);
         tabbedPane.setEnabledAt(3, true);
 
         disaggregateButton.setEnabled(false);
         createEventsButton.setEnabled(false);
 
       }
     });
 
     // APPLIANCE DETECTION //
 
     detectedApplianceList.addListSelectionListener(new ListSelectionListener() {
       public void valueChanged (ListSelectionEvent e)
       {
 
         consumptionModelPanel.removeAll();
         consumptionModelPanel.updateUI();
         dataReviewPanel.removeAll();
         dataReviewPanel.updateUI();
 
         String selection = detectedApplianceList.getSelectedValue();
 
         Appliance current = installation.findAppliance(selection);
 
         // System.out.println("Appliance:" + current.getName());
 
         ChartPanel chartPanel =
           ChartUtils.createHistogram("Test", "Time Step", "Power",
                                      current.getConsumptionModel());
 
         consumptionModelPanel.add(chartPanel, BorderLayout.CENTER);
         consumptionModelPanel.validate();
 
         ChartPanel chartPanel2 =
           ChartUtils.createLineDiagram("Test", "Time Step", "Power",
                                        current.getActivePower());
 
         dataReviewPanel.add(chartPanel2, BorderLayout.CENTER);
         dataReviewPanel.validate();
 
       }
     });
 
     // // TRAINING TAB //
 
     trainingTab.addComponentListener(new ComponentAdapter() {
       @Override
       public void componentShown (ComponentEvent arg0)
       {
         selectedApplianceList.setSelectedIndex(0);
       }
     });
 
     trainingButton.addActionListener(new ActionListener() {
       public void actionPerformed (ActionEvent e)
       {
         tabbedPane.setEnabledAt(2, true);
         Appliance current =
           installation.findAppliance(selectedApplianceList.getSelectedValue());
 
         String startTime, startTimeBinned, duration, dailyTimes;
 
         if (timesHistogramRadioButton.isSelected())
           dailyTimes = "Histogram";
         else if (timesNormalRadioButton.isSelected())
           dailyTimes = "Normal";
         else
           dailyTimes = "GMM";
 
         if (durationHistogramRadioButton.isSelected())
           duration = "Histogram";
         else if (durationNormalRadioButton.isSelected())
           duration = "Normal";
         else
           duration = "GMM";
 
         if (startHistogramRadioButton.isSelected()) {
           startTime = "Histogram";
           startTimeBinned = "Histogram";
         }
         else if (startNormalRadioButton.isSelected()) {
           startTime = "Normal";
           startTimeBinned = "Normal";
         }
         else {
           startTime = "GMM";
           startTimeBinned = "GMM";
         }
 
         String[] distributions =
           { dailyTimes, duration, startTime, startTimeBinned };
 
         try {
           installation.getPerson().train(current, distributions);
         }
         catch (FileNotFoundException e1) {
           e1.printStackTrace();
         }
 
         System.out.println("Training OK!");
 
         distributionPreviewPanel.removeAll();
         distributionPreviewPanel.updateUI();
 
         BehaviourModel behaviourModel =
           installation.getPerson().findBehaviour(current.getName()
                                                          + " Behaviour Model");
 
         ChartPanel chartPanel =
           behaviourModel.createDailyTimesDistributionChart();
         distributionPreviewPanel.add(chartPanel, BorderLayout.CENTER);
         distributionPreviewPanel.validate();
         int size = behaviorSelectList.getModel().getSize();
         DefaultListModel<String> dlm;
         if (size > 0) {
           dlm = (DefaultListModel<String>) behaviorSelectList.getModel();
           if (dlm.contains(behaviourModel.getName()) == false)
             dlm.addElement(behaviourModel.getName());
         }
         else {
           dlm = new DefaultListModel<String>();
           dlm.addElement(behaviourModel.getName());
           behaviorSelectList.setEnabled(true);
         }
 
         behaviorSelectList.setModel(dlm);
 
         size = exportModelList.getModel().getSize();
         DefaultListModel<String> dlm2;
         if (size > 0) {
           dlm2 = (DefaultListModel<String>) exportModelList.getModel();
           if (dlm2.contains(behaviourModel.getName()) == false)
             dlm2.addElement(behaviourModel.getName());
         }
         else {
           dlm2 = new DefaultListModel<String>();
           dlm2.addElement(behaviourModel.getName());
           exportModelList.setEnabled(true);
         }
 
         dailyTimesButton.setEnabled(true);
         durationButton.setEnabled(true);
         startTimeButton.setEnabled(true);
         startTimeBinnedButton.setEnabled(true);
 
         exportModelList.setModel(dlm2);
         if (!tabbedPane.isEnabledAt(3))
           tabbedPane.setEnabledAt(3, true);
 
         exportDailyButton.setEnabled(true);
         exportDurationButton.setEnabled(true);
         exportStartButton.setEnabled(true);
         exportStartBinnedButton.setEnabled(true);
       }
 
     });
 
     dailyTimesButton.addActionListener(new ActionListener() {
       public void actionPerformed (ActionEvent arg0)
       {
 
         distributionPreviewPanel.removeAll();
         distributionPreviewPanel.updateUI();
 
         String selection = selectedApplianceList.getSelectedValue();
 
         Appliance current = installation.findAppliance(selection);
 
         BehaviourModel behaviourModel =
           installation.getPerson().findBehaviour(current.getName()
                                                          + " Behaviour Model");
 
         ChartPanel chartPanel =
           behaviourModel.createDailyTimesDistributionChart();
         distributionPreviewPanel.add(chartPanel, BorderLayout.CENTER);
         distributionPreviewPanel.validate();
 
       }
     });
 
     startTimeBinnedButton.addActionListener(new ActionListener() {
       public void actionPerformed (ActionEvent arg0)
       {
 
         distributionPreviewPanel.removeAll();
         distributionPreviewPanel.updateUI();
 
         String selection = selectedApplianceList.getSelectedValue();
 
         Appliance current = installation.findAppliance(selection);
 
         BehaviourModel behaviourModel =
           installation.getPerson().findBehaviour(current.getName()
                                                          + " Behaviour Model");
 
         ChartPanel chartPanel =
           behaviourModel.createStartTimeBinnedDistributionChart();
         distributionPreviewPanel.add(chartPanel, BorderLayout.CENTER);
         distributionPreviewPanel.validate();
 
       }
     });
 
     startTimeButton.addActionListener(new ActionListener() {
       public void actionPerformed (ActionEvent arg0)
       {
 
         distributionPreviewPanel.removeAll();
         distributionPreviewPanel.updateUI();
 
         String selection = selectedApplianceList.getSelectedValue();
 
         Appliance current = installation.findAppliance(selection);
 
         BehaviourModel behaviourModel =
           installation.getPerson().findBehaviour(current.getName()
                                                          + " Behaviour Model");
 
         ChartPanel chartPanel =
           behaviourModel.createStartTimeDistributionChart();
         distributionPreviewPanel.add(chartPanel, BorderLayout.CENTER);
         distributionPreviewPanel.validate();
 
       }
     });
 
     durationButton.addActionListener(new ActionListener() {
       public void actionPerformed (ActionEvent arg0)
       {
 
         distributionPreviewPanel.removeAll();
         distributionPreviewPanel.updateUI();
 
         String selection = selectedApplianceList.getSelectedValue();
 
         Appliance current = installation.findAppliance(selection);
 
         BehaviourModel behaviourModel =
           installation.getPerson().findBehaviour(current.getName()
                                                          + " Behaviour Model");
 
         ChartPanel chartPanel =
           behaviourModel.createDurationDistributionChart();
         distributionPreviewPanel.add(chartPanel, BorderLayout.CENTER);
         distributionPreviewPanel.validate();
 
       }
     });
 
     selectedApplianceList.addListSelectionListener(new ListSelectionListener() {
       public void valueChanged (ListSelectionEvent arg0)
       {
 
         consumptionPreviewPanel.removeAll();
         consumptionPreviewPanel.updateUI();
         distributionPreviewPanel.removeAll();
         distributionPreviewPanel.updateUI();
 
         String selection = selectedApplianceList.getSelectedValue();
 
         Appliance current = installation.findAppliance(selection);
 
         ChartPanel chartPanel =
           ChartUtils.createHistogram("Test Second", "Time Step", "Power",
                                      current.getConsumptionModel());
 
         consumptionPreviewPanel.add(chartPanel, BorderLayout.CENTER);
         consumptionPreviewPanel.validate();
 
         BehaviourModel behaviourModel =
           installation.getPerson().findBehaviour(current.getName()
                                                          + " Behaviour Model");
 
         if (behaviourModel != null) {
 
           ChartPanel chartPanel2 =
             behaviourModel.createDurationDistributionChart();
           distributionPreviewPanel.add(chartPanel2, BorderLayout.CENTER);
           distributionPreviewPanel.validate();
           distributionPreviewPanel.updateUI();
 
         }
       }
     });
 
     // RESPONSE TAB //
 
     createResponseTab.addComponentListener(new ComponentAdapter() {
       @Override
       public void componentShown (ComponentEvent arg0)
       {
         behaviorSelectList.setSelectedIndex(0);
 
        System.out.println("YEAH");
       }
     });
 
     // previewResponseButton.addActionListener(new ActionListener() {
     // public void actionPerformed (ActionEvent arg0)
     // {
     //
     // Appliance current =
     // installation.findAppliance(behaviorSelectList.getSelectedValue());
     //
     // int response = -1;
     //
     // if (bestCaseRadioButton.isSelected())
     // response = 0;
     // else if (normalCaseRadioButton.isSelected())
     // response = 1;
     // else
     // response = 2;
     //
     // basicScheme = ImportUtils.parseScheme(basicPricingSchemePane.getText());
     // newScheme = ImportUtils.parseScheme(newPricingSchemePane.getText());
     //
     // ChartPanel chartPanel =
     // current.responsePreview(response, basicScheme, newScheme);
     // responsePanel.add(chartPanel, BorderLayout.CENTER);
     // responsePanel.validate();
     //
     // createResponseButton.setEnabled(true);
     // }
     // });
     //
     // createResponseButton.addActionListener(new ActionListener() {
     // public void actionPerformed (ActionEvent arg0)
     // {
     // exportPreviewPanel.removeAll();
     // exportPreviewPanel.updateUI();
     //
     // int response = -1;
     // String responseString = "";
     // if (bestCaseRadioButton.isSelected()) {
     // response = 0;
     // responseString = "Best";
     // }
     // else if (normalCaseRadioButton.isSelected()) {
     // response = 1;
     // responseString = "Normal";
     // }
     // else if (worstCaseRadioButton.isSelected()) {
     // response = 2;
     // responseString = "Worst";
     // }
     //
     // basicScheme = ImportUtils.parseScheme(basicPricingSchemePane.getText());
     // newScheme = ImportUtils.parseScheme(newPricingSchemePane.getText());
     //
     // Appliance current =
     // installation.findAppliance(behaviorSelectList.getSelectedValue());
     //
     // Appliance newAppliance = null;
     //
     // try {
     // newAppliance =
     // current.createModel(current.getName() + "(" + responseString + ")",
     // response, basicScheme, newScheme);
     // }
     // catch (FileNotFoundException e) {
     //
     // e.printStackTrace();
     // }
     //
     // installation.add(newAppliance);
     //
     // System.out.println(newAppliance.getName());
     //
     // int size = exportModelList.getModel().getSize();
     // // System.out.println(size);
     // DefaultListModel<String> dlm;
     // if (size > 0) {
     // dlm = (DefaultListModel<String>) exportModelList.getModel();
     // if (dlm.contains(newAppliance.getName()) == false)
     // dlm.addElement(newAppliance.getName());
     // }
     // else {
     // dlm = new DefaultListModel<String>();
     // dlm.addElement(newAppliance.getName());
     // exportModelList.setEnabled(true);
     // }
     // exportModelList.setModel(dlm);
     //
     // exportDailyButton.setEnabled(true);
     // exportDurationButton.setEnabled(true);
     // exportStartButton.setEnabled(true);
     // exportStartBinnedButton.setEnabled(true);
     // }
     // });
     //
     newPricingSchemePane.addKeyListener(new KeyAdapter() {
       @Override
       public void keyTyped (KeyEvent arg0)
       {
         commitButton.setEnabled(true);
       }
     });
 
     basicPricingSchemePane.addCaretListener(new CaretListener() {
       public void caretUpdate (CaretEvent arg0)
       {
         commitButton.setEnabled(true);
       }
     });
 
     commitButton.addActionListener(new ActionListener() {
       public void actionPerformed (ActionEvent arg0)
       {
 
         // System.out.println("Basic: " + basicPricingSchemePane.getText());
         // System.out.println("New: " + newPricingSchemePane.getText());
 
         boolean basicScheme = false;
         boolean newScheme = false;
 
         if (basicPricingSchemePane.getText().equalsIgnoreCase("") == false)
           basicScheme = true;
 
         if (newPricingSchemePane.getText().equalsIgnoreCase("") == false)
           newScheme = true;
 
         // System.out.println("Basic: " + basicScheme + " New: " + newScheme);
 
         if (basicScheme && newScheme) {
           ChartPanel chartPanel =
             ChartUtils.parsePricingScheme(basicPricingSchemePane.getText(),
                                           newPricingSchemePane.getText());
 
           pricingPreviewPanel.add(chartPanel, BorderLayout.CENTER);
           pricingPreviewPanel.validate();
 
           previewResponseButton.setEnabled(true);
 
         }
         else if (basicScheme) {
 
           ChartPanel chartPanel2 =
             ChartUtils.parsePricingScheme(basicPricingSchemePane.getText());
 
           pricingPreviewPanel.add(chartPanel2, BorderLayout.CENTER);
           pricingPreviewPanel.validate();
 
           previewResponseButton.setEnabled(false);
 
         }
         else {
           previewResponseButton.setEnabled(false);
         }
       }
     });
 
     // EXPORT TAB //
 
     exportTab.addComponentListener(new ComponentAdapter() {
       @Override
       public void componentShown (ComponentEvent arg0)
       {
         exportModelList.setSelectedIndex(0);
 
       }
     });
 
     exportModelList.addListSelectionListener(new ListSelectionListener() {
       public void valueChanged (ListSelectionEvent arg0)
       {
 
         exportPreviewPanel.removeAll();
         exportPreviewPanel.updateUI();
 
         String selection = exportModelList.getSelectedValue();
 
         Appliance appliance = installation.findAppliance(selection);
 
         BehaviourModel behaviour =
           installation.getPerson().findBehaviour(selection);
 
         ResponseModel response =
           installation.getPerson().findResponse(selection);
 
         ChartPanel chartPanel = null;
 
         if (appliance != null) {
 
           chartPanel =
             ChartUtils.createHistogram("Test", "Time Step", "Power",
                                        appliance.getConsumptionModel());
 
           exportDailyButton.setEnabled(false);
           exportDurationButton.setEnabled(false);
           exportStartButton.setEnabled(false);
           exportStartBinnedButton.setEnabled(false);
 
         }
         else if (behaviour != null) {
 
           chartPanel = behaviour.createDailyTimesDistributionChart();
 
           exportDailyButton.setEnabled(true);
           exportDurationButton.setEnabled(true);
           exportStartButton.setEnabled(true);
           exportStartBinnedButton.setEnabled(true);
         }
         else if (response != null) {
 
           chartPanel = response.createDailyTimesDistributionChart();
 
           exportDailyButton.setEnabled(true);
           exportDurationButton.setEnabled(true);
           exportStartButton.setEnabled(true);
           exportStartBinnedButton.setEnabled(true);
         }
 
         exportPreviewPanel.add(chartPanel, BorderLayout.CENTER);
         exportPreviewPanel.validate();
 
       }
     });
 
     exportDailyButton.addActionListener(new ActionListener() {
       public void actionPerformed (ActionEvent arg0)
       {
 
         exportPreviewPanel.removeAll();
         exportPreviewPanel.updateUI();
 
         String selection = exportModelList.getSelectedValue();
 
         BehaviourModel behaviour =
           installation.getPerson().findBehaviour(selection);
 
         ResponseModel response =
           installation.getPerson().findResponse(selection);
 
         ChartPanel chartPanel = null;
 
         if (behaviour != null)
           chartPanel = behaviour.createDailyTimesDistributionChart();
 
         else
           chartPanel = response.createDailyTimesDistributionChart();
 
         exportPreviewPanel.add(chartPanel, BorderLayout.CENTER);
         exportPreviewPanel.validate();
       }
     });
 
     exportStartBinnedButton.addActionListener(new ActionListener() {
       public void actionPerformed (ActionEvent arg0)
       {
 
         exportPreviewPanel.removeAll();
         exportPreviewPanel.updateUI();
 
         String selection = exportModelList.getSelectedValue();
 
         BehaviourModel behaviour =
           installation.getPerson().findBehaviour(selection);
 
         ResponseModel response =
           installation.getPerson().findResponse(selection);
 
         ChartPanel chartPanel = null;
 
         if (behaviour != null)
           chartPanel = behaviour.createStartTimeBinnedDistributionChart();
 
         else
           chartPanel = response.createStartTimeBinnedDistributionChart();
 
         exportPreviewPanel.add(chartPanel, BorderLayout.CENTER);
         exportPreviewPanel.validate();
 
       }
     });
 
     exportStartButton.addActionListener(new ActionListener() {
       public void actionPerformed (ActionEvent arg0)
       {
 
         exportPreviewPanel.removeAll();
         exportPreviewPanel.updateUI();
 
         String selection = exportModelList.getSelectedValue();
 
         BehaviourModel behaviour =
           installation.getPerson().findBehaviour(selection);
 
         ResponseModel response =
           installation.getPerson().findResponse(selection);
 
         ChartPanel chartPanel = null;
 
         if (behaviour != null)
           chartPanel = behaviour.createStartTimeDistributionChart();
         else
           chartPanel = response.createStartTimeDistributionChart();
 
         exportPreviewPanel.add(chartPanel, BorderLayout.CENTER);
         exportPreviewPanel.validate();
 
       }
     });
 
     exportDurationButton.addActionListener(new ActionListener() {
       public void actionPerformed (ActionEvent arg0)
       {
 
         exportPreviewPanel.removeAll();
         exportPreviewPanel.updateUI();
 
         String selection = exportModelList.getSelectedValue();
 
         BehaviourModel behaviour =
           installation.getPerson().findBehaviour(selection);
 
         ResponseModel response =
           installation.getPerson().findResponse(selection);
 
         ChartPanel chartPanel = null;
 
         if (behaviour != null)
           chartPanel = behaviour.createDurationDistributionChart();
         else
           chartPanel = response.createDurationDistributionChart();
 
         exportPreviewPanel.add(chartPanel, BorderLayout.CENTER);
         exportPreviewPanel.validate();
 
       }
     });
 
     connectButton.addActionListener(new ActionListener() {
       public void actionPerformed (ActionEvent e)
       {
 
         System.out.println("Username: " + usernameTextField.getText()
                            + " Password: "
                            + String.valueOf(passwordField.getPassword())
                            + " URL: " + urlTextField.getText());
 
         exportButton.setEnabled(true);
         exportAllButton.setEnabled(true);
 
       }
     });
   }
 
   private void cleanFiles ()
   {
 
     File directory = new File("Files");
     File files[] = directory.listFiles();
     for (int index = 0; index < files.length; index++) {
       {
 
         boolean wasDeleted = files[index].delete();
         if (!wasDeleted) {
           System.out.println("Not Deleted File " + files[index].toString());
         }
       }
     }
   }
 }
