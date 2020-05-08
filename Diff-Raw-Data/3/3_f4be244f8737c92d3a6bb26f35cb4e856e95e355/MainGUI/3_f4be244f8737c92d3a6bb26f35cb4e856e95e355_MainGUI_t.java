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
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Cursor;
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
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Scanner;
 
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
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JSlider;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.LookAndFeel;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.EtchedBorder;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.CaretEvent;
 import javax.swing.event.CaretListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import org.apache.http.auth.AuthenticationException;
 import org.jfree.chart.ChartPanel;
 
 import eu.cassandra.disaggregation.Disaggregate;
 import eu.cassandra.training.activity.ActivityModel;
 import eu.cassandra.training.entities.ActivityTemp;
 import eu.cassandra.training.entities.Appliance;
 import eu.cassandra.training.entities.ApplianceTemp;
 import eu.cassandra.training.entities.Installation;
 import eu.cassandra.training.response.ResponseModel;
 import eu.cassandra.training.utils.APIUtilities;
 import eu.cassandra.training.utils.ChartUtils;
 import eu.cassandra.training.utils.Constants;
 import eu.cassandra.training.utils.Utils;
 
 /**
  * This class, which extends the Java's JFrame class, implements the overall GUI
  * of the Training Module of Cassandra Project. Through this, the user can
  * upload his dataset, disaggregate itsconsumption, create entities, activity
  * and response models and upload them to the main Cassandra Platform to his
  * personal User Library.
  * 
  * @author Antonios Chrysopoulos
  * @version 0.9, Date: 29.07.2013
  */
 public class MainGUI extends JFrame
 {
 
   /**
    * This variable is used for the correct serializing of the class' objects
    */
   private static final long serialVersionUID = 1L;
 
   /**
    * This variable is the main panel where all the graphical objects of the GUI
    * are added.
    */
   private JPanel contentPane;
 
   /**
    * This variable is used to check if one or more items are processed at the
    * same time.
    */
   private boolean manyFlag = false;
 
   /**
    * This is the variable controlling over the radio buttons used for choosing
    * the Data Measurement Type (Single Appliance or Installation) in the Data
    * Import tab.
    */
   private final ButtonGroup dataMeasurementsButtonGroup = new ButtonGroup();
 
   /**
    * This is the variable controlling over the radio buttons used for declaring
    * the activeOnly consumption data contained within the data set provided from
    * the
    * user (Active Power only or Active and Reactive activeOnly) in the Data
    * Import
    * tab.
    */
   private final ButtonGroup powerButtonGroup = new ButtonGroup();
 
   /**
    * This is the variable controlling over the radio buttons used for choosing
    * the Daily Times distribution on the Training Activity Models tab
    * (Histogram, Normal Distribution or Gaussian Mixture Models).
    */
   private final ButtonGroup timesDailyButtonGroup = new ButtonGroup();
 
   /**
    * This is the variable controlling over the radio buttons used for choosing
    * the Start Time distribution on the Training Activity Models tab
    * (Histogram, Normal Distribution or Gaussian Mixture Models).
    */
   private final ButtonGroup startTimeButtonGroup = new ButtonGroup();
 
   /**
    * This is the variable controlling over the radio buttons used for choosing
    * the Duration distribution on the Training Activity Models tab (Histogram,
    * Normal Distribution or Gaussian Mixture Models).
    */
   private final ButtonGroup durationButtonGroup = new ButtonGroup();
 
   /**
    * This is the variable controlling over the radio buttons used for choosing
    * the Response Model type for the visualization or extraction of the new
    * activity models (optimal, normal and discrete case scenario) in the Create
    * Response Models tab.
    */
   private final ButtonGroup responseModelButtonGroup = new ButtonGroup();
 
   /**
    * This is the Installation Entity model as created when the consumption
    * data set is imported by the user to the Training Module. The rest of the
    * entities created thereafter are added under this higher level Entity in
    * order to simulate an electrical installation.
    */
   private Installation installation = new Installation();
 
   // private static int threshold = 2;
 
   /**
    * This is an arraylist of the temporary Appliance Entity models that are
    * extracted from the disaggregation procedure and are utilized for the
    * creation of the actual Appliance Entity models of the training procedure
    * after further analysis.
    */
   private static ArrayList<ApplianceTemp> tempAppliances =
     new ArrayList<ApplianceTemp>();
 
   /**
    * This is an arraylist of the temporary Activity Entity model that are
    * extracted from the disaggregation procedure and are utilized for the
    * creation of the actual Activity Entity models of the training procedure
    * after further analysis.
    */
   private static ArrayList<ActivityTemp> tempActivities =
     new ArrayList<ActivityTemp>();
 
   /**
    * This is a list of the final detected Appliance Entity models as they are
    * extracted after temporary Appliances' analysis or as given from the user.
    * They appear as a list in the Detected Appliances panel on the Data Import
    * tab.
    */
   private DefaultListModel<String> detectedAppliances =
     new DefaultListModel<String>();
 
   /**
    * This is a list of the final selected Appliance Entity model (in case of a
    * single appliance) or Activity Entity models (in case of disaggregated
    * consumption data set) as they are extracted after temporary appliances /
    * activity analysis. They appear as a list in the Appliances / Activities
    * Selection panel on the Training Activity Models tab.
    */
   private DefaultListModel<String> selectedAppliances =
     new DefaultListModel<String>();
 
   /**
    * This is a list of the trained Activity models as they are created
    * after user demand in the training after temporary appliances/activity
    * analysis. They appear as a list in the Activity Model Selection panel on
    * the Create Response Models tab.
    */
   private DefaultListModel<String> activityModels =
     new DefaultListModel<String>();
 
   /**
    * This is a list of the ready-to-export Entity models as they are extracted
    * from the consumption data set analysis or created after user demand during
    * training or response modelling procedure. They appear as a list in the
    * Model Export Selection panel on the Export Models tab.
    */
   private DefaultListModel<String> exportModels =
     new DefaultListModel<String>();
 
   /**
    * This is the main function that launches the application. No arguments are
    * needed to run.
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
    * Constructor of the Training Module GUI.
    * 
    * @throws UnsupportedLookAndFeelException
    * @throws IllegalAccessException
    * @throws InstantiationException
    * @throws ClassNotFoundException
    * @throws FileNotFoundException
    */
   public MainGUI () throws ClassNotFoundException, InstantiationException,
     IllegalAccessException, UnsupportedLookAndFeelException,
     FileNotFoundException
   {
     setForeground(new Color(0, 204, 51));
 
     // Enable the closing of the frame when pressing the x on the upper corner
     // of the window
     addWindowListener(new WindowAdapter() {
       @Override
       public void windowClosing (WindowEvent e)
       {
         Utils.cleanFiles();
         System.exit(0);
       }
     });
 
     // Cleaning temporary files from the temp folder when starting the GUI.
     Utils.cleanFiles();
 
     // Change the platforms look and feel to Nimbus
     LookAndFeel lnf = new javax.swing.plaf.nimbus.NimbusLookAndFeel();
     UIManager.put("NimbusLookAndFeel", Color.GREEN);
     UIManager.setLookAndFeel(lnf);
 
     // Setting the basic attributes of the Training Module GUI
     setTitle("Training Module (BETA)");
     setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     setBounds(100, 100, 1228, 852);
 
     // Creating the menu bar and adding the menu items
     JMenuBar menuBar = new JMenuBar();
     setJMenuBar(menuBar);
 
     JMenu mnNewMenu = new JMenu("File");
     menuBar.add(mnNewMenu);
 
     JMenuItem mntmExit = new JMenuItem("Exit");
     mntmExit.addActionListener(new ActionListener() {
       public void actionPerformed (ActionEvent e)
       {
         Utils.cleanFiles();
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
 
     // Adding the tabbed pane to the content pane
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
     tabbedPane.addTab("Train Activity Models", null, trainingTab, null);
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
 
     // TRAINING ACTIVITY TAB //
 
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
     applianceSelectionPanel.setBorder(new TitledBorder(UIManager
             .getBorder("TitledBorder.border"), "Appliance/Activity Selection",
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
             .setBorder(new TitledBorder(UIManager
                     .getBorder("TitledBorder.border"),
                                         "Example Consumption Model Preview",
                                         TitledBorder.LEADING, TitledBorder.TOP,
                                         null, null));
     consumptionPreviewPanel.setBounds(630, 261, 557, 483);
     trainingTab.add(consumptionPreviewPanel);
     consumptionPreviewPanel.setLayout(new BorderLayout(0, 0));
 
     // RESPONSE MODEL TAB //
 
     final JPanel responseParametersPanel = new JPanel();
     responseParametersPanel.setLayout(null);
     responseParametersPanel.setBorder(new TitledBorder(null,
                                                        "Response Parameters",
                                                        TitledBorder.LEADING,
                                                        TitledBorder.TOP, null,
                                                        null));
     responseParametersPanel.setBounds(6, 6, 391, 271);
     createResponseTab.add(responseParametersPanel);
 
     final JPanel activityModelSelectionPanel = new JPanel();
     activityModelSelectionPanel.setLayout(null);
     activityModelSelectionPanel
             .setBorder(new TitledBorder(UIManager
                     .getBorder("TitledBorder.border"),
                                         "Activity Model Selection",
                                         TitledBorder.LEADING, TitledBorder.TOP,
                                         null, null));
     activityModelSelectionPanel.setBounds(6, 276, 391, 226);
     createResponseTab.add(activityModelSelectionPanel);
 
     final JPanel responsePanel = new JPanel();
     responsePanel.setBorder(new TitledBorder(UIManager
             .getBorder("TitledBorder.border"), "Activity Model Change Preview",
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
     exportPreviewPanel.setBounds(20, 283, 1177, 395);
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
     activePowerRadioButton.setEnabled(false);
     activePowerRadioButton.setBounds(242, 140, 115, 18);
     dataFilePanel.add(activePowerRadioButton);
 
     final JRadioButton activeAndReactivePowerRadioButton =
       new JRadioButton("Active and Reactive Power (P, Q)");
     activeAndReactivePowerRadioButton.setSelected(true);
     powerButtonGroup.add(activeAndReactivePowerRadioButton);
     activeAndReactivePowerRadioButton.setEnabled(false);
     activeAndReactivePowerRadioButton.setBounds(242, 161, 262, 18);
     dataFilePanel.add(activeAndReactivePowerRadioButton);
 
     // //////////////////
     // DISAGGREGATION //
     // /////////////////
 
     final JLabel lblAppliancesDetected = new JLabel("Detected Appliances ");
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
     timesNormalRadioButton.setEnabled(false);
     timesDailyButtonGroup.add(timesNormalRadioButton);
     timesNormalRadioButton.setBounds(304, 40, 137, 18);
     trainingParametersPanel.add(timesNormalRadioButton);
 
     JRadioButton timesGaussianRadioButton =
       new JRadioButton("Gaussian Mixture");
     timesGaussianRadioButton.setEnabled(false);
     timesDailyButtonGroup.add(timesGaussianRadioButton);
     timesGaussianRadioButton.setBounds(478, 38, 137, 18);
     trainingParametersPanel.add(timesGaussianRadioButton);
 
     final JLabel label_2 = new JLabel("Start Time");
     label_2.setBounds(19, 133, 103, 16);
     trainingParametersPanel.add(label_2);
 
     final JRadioButton startHistogramRadioButton =
       new JRadioButton("Histogram");
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
     startGaussianRadioButton.setSelected(true);
     startTimeButtonGroup.add(startGaussianRadioButton);
     startGaussianRadioButton.setBounds(478, 131, 137, 18);
     trainingParametersPanel.add(startGaussianRadioButton);
 
     final JLabel label_3 = new JLabel("Duration");
     label_3.setBounds(19, 86, 103, 16);
     trainingParametersPanel.add(label_3);
 
     final JRadioButton durationHistogramRadioButton =
       new JRadioButton("Histogram");
     durationButtonGroup.add(durationHistogramRadioButton);
     durationHistogramRadioButton.setBounds(160, 84, 87, 18);
     trainingParametersPanel.add(durationHistogramRadioButton);
 
     final JRadioButton durationNormalRadioButton =
       new JRadioButton("Normal Distribution");
     durationNormalRadioButton.setSelected(true);
     durationButtonGroup.add(durationNormalRadioButton);
     durationNormalRadioButton.setBounds(304, 86, 137, 18);
     trainingParametersPanel.add(durationNormalRadioButton);
 
     final JRadioButton durationGaussianRadioButton =
       new JRadioButton("Gaussian Mixture");
     durationButtonGroup.add(durationGaussianRadioButton);
     durationGaussianRadioButton.setBounds(478, 84, 137, 18);
     trainingParametersPanel.add(durationGaussianRadioButton);
 
     final JButton trainingButton = new JButton("Train");
     trainingButton.setBounds(125, 194, 115, 28);
     trainingParametersPanel.add(trainingButton);
 
     final JButton trainAllButton = new JButton("Train All");
     trainAllButton.setBounds(366, 194, 115, 28);
     trainingParametersPanel.add(trainAllButton);
 
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
     responseParametersPanel.add(label_5);
 
     final JSlider monetarySlider = new JSlider();
     monetarySlider.setEnabled(false);
     monetarySlider.setPaintLabels(true);
     monetarySlider.setSnapToTicks(true);
     monetarySlider.setPaintTicks(true);
     monetarySlider.setMinorTickSpacing(10);
     monetarySlider.setMajorTickSpacing(10);
     monetarySlider.setBounds(138, 28, 214, 45);
     responseParametersPanel.add(monetarySlider);
 
     final JLabel label_6 = new JLabel("Environmental Awareness");
     label_6.setBounds(10, 79, 157, 16);
     responseParametersPanel.add(label_6);
 
     final JSlider environmentalSlider = new JSlider();
     environmentalSlider.setEnabled(false);
     environmentalSlider.setPaintLabels(true);
     environmentalSlider.setPaintTicks(true);
     environmentalSlider.setMajorTickSpacing(10);
     environmentalSlider.setMinorTickSpacing(10);
     environmentalSlider.setSnapToTicks(true);
     environmentalSlider.setBounds(138, 79, 214, 45);
     responseParametersPanel.add(environmentalSlider);
 
     final JLabel label_7 = new JLabel("Response Model");
     label_7.setBounds(10, 153, 103, 16);
     responseParametersPanel.add(label_7);
 
     final JRadioButton optimalCaseRadioButton =
       new JRadioButton("Optimal Case Scenario");
     responseModelButtonGroup.add(optimalCaseRadioButton);
     optimalCaseRadioButton.setBounds(138, 131, 146, 18);
     responseParametersPanel.add(optimalCaseRadioButton);
 
     final JRadioButton normalCaseRadioButton =
       new JRadioButton("Normal Case Scenario");
     normalCaseRadioButton.setSelected(true);
     responseModelButtonGroup.add(normalCaseRadioButton);
     normalCaseRadioButton.setBounds(138, 152, 157, 18);
     responseParametersPanel.add(normalCaseRadioButton);
 
     final JRadioButton discreteCaseRadioButton =
       new JRadioButton("Discrete Case Scenario");
     discreteCaseRadioButton.setSelected(true);
     responseModelButtonGroup.add(discreteCaseRadioButton);
     discreteCaseRadioButton.setBounds(138, 173, 157, 18);
     responseParametersPanel.add(discreteCaseRadioButton);
 
     final JButton previewResponseButton = new JButton("Preview Response Model");
     previewResponseButton.setEnabled(false);
     previewResponseButton.setBounds(24, 198, 157, 28);
     responseParametersPanel.add(previewResponseButton);
 
     final JButton createResponseButton = new JButton("Create Response Model");
     createResponseButton.setEnabled(false);
     createResponseButton.setBounds(191, 198, 162, 28);
     responseParametersPanel.add(createResponseButton);
 
     final JButton createResponseAllButton = new JButton("Create Response All");
     createResponseAllButton.setEnabled(false);
     createResponseAllButton.setBounds(111, 232, 157, 28);
     responseParametersPanel.add(createResponseAllButton);
 
     // SELECT ACTIVITY MODEL //
 
     final JLabel lblSelectedActivity = new JLabel("Selected Activity");
     lblSelectedActivity.setBounds(10, 21, 130, 16);
     activityModelSelectionPanel.add(lblSelectedActivity);
 
     JScrollPane activityListScrollPane = new JScrollPane();
     activityListScrollPane.setBounds(20, 39, 355, 176);
     activityModelSelectionPanel.add(activityListScrollPane);
 
     final JList<String> activitySelectList = new JList<String>();
     activityListScrollPane.setViewportView(activitySelectList);
     activitySelectList.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null,
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
     scrollPane.setBounds(83, 32, 503, 212);
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
     usernameTextField.setText("user");
     usernameTextField.setColumns(10);
     usernameTextField.setBounds(122, 21, 405, 28);
     connectionPanel.add(usernameTextField);
 
     final JButton exportButton = new JButton("Export Entity");
     exportButton.setEnabled(false);
     exportButton.setBounds(46, 178, 147, 28);
     connectionPanel.add(exportButton);
 
     final JButton exportAllBaseButton = new JButton("Export All Base");
     exportAllBaseButton.setEnabled(false);
     exportAllBaseButton.setBounds(203, 178, 177, 28);
     connectionPanel.add(exportAllBaseButton);
 
     final JButton exportAllResponseButton = new JButton("Export All Response");
     exportAllResponseButton.setEnabled(false);
     exportAllResponseButton.setBounds(390, 178, 181, 28);
     connectionPanel.add(exportAllResponseButton);
 
     JLabel passwordLabel = new JLabel("Password:");
     passwordLabel.setBounds(46, 62, 71, 16);
     connectionPanel.add(passwordLabel);
 
     JLabel UrlLabel = new JLabel("URL:");
     UrlLabel.setBounds(46, 105, 71, 16);
     connectionPanel.add(UrlLabel);
 
     final JTextField urlTextField;
     urlTextField = new JTextField();
     urlTextField.setText("https://160.40.50.233:8443/cassandra/api");
     urlTextField.setColumns(10);
     urlTextField.setBounds(122, 99, 405, 28);
     connectionPanel.add(urlTextField);
 
     final JButton connectButton = new JButton("Connect");
     connectButton.setEnabled(false);
     connectButton.setBounds(217, 138, 147, 28);
     connectionPanel.add(connectButton);
 
     final JPasswordField passwordField;
     passwordField = new JPasswordField();
     passwordField.setBounds(122, 60, 405, 28);
     connectionPanel.add(passwordField);
 
     final JTextField householdNameTextField;
     householdNameTextField = new JTextField();
     householdNameTextField.setEnabled(false);
     householdNameTextField.setBounds(166, 225, 405, 31);
     connectionPanel.add(householdNameTextField);
     householdNameTextField.setColumns(10);
 
     final JLabel householdNameLabel = new JLabel("Export Household Name:");
     householdNameLabel.setBounds(24, 233, 147, 14);
     connectionPanel.add(householdNameLabel);
 
     // //////////////////
     // ACTIONS ///////
     // /////////////////
 
     // IMPORT TAB //
 
     // DATA IMPORT ////
 
     dataBrowseButton.addActionListener(new ActionListener() {
       /**
        * This function is called when the user presses the browse button to
        * input the data file on the Data File panel of the Import Data tab.
        * 
        */
       public void actionPerformed (ActionEvent e)
       {
         // Opens the browse panel to find the data set file
         JFileChooser fc = new JFileChooser("./");
 
         // Adds a filter to the type of files acceptable for selection
         fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
         fc.setFileFilter(new MyFilter2());
 
         int returnVal = fc.showOpenDialog(contentPane);
 
         // After choosing the file some of the options in the Data File panel
         // are unlocked
         if (returnVal == JFileChooser.APPROVE_OPTION) {
           File file = fc.getSelectedFile();
 
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
       /**
        * This function is called when the user presses the browse button to
        * input the consumption model file on the Data File panel of the Import
        * Data tab.
        * 
        */
       public void actionPerformed (ActionEvent e)
       {
         // Opens the browse panel to find the consumption model file
         JFileChooser fc = new JFileChooser("./");
 
         // Adds a filter to the type of files acceptable for selection
         fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
         fc.setFileFilter(new MyFilter());
 
         int returnVal = fc.showOpenDialog(contentPane);
 
         // After choosing the file some of the options in the Data File panel
         // are unlocked
         if (returnVal == JFileChooser.APPROVE_OPTION) {
           File file = fc.getSelectedFile();
 
           consumptionPathField.setText(file.getAbsolutePath());
           createEventsButton.setEnabled(true);
         }
 
       }
     });
 
     resetButton.addActionListener(new ActionListener() {
       /**
        * This function is called when the user presses the reset button
        * on the Data File panel of the Import Data tab. All the imported and
        * created entities are removed and the Training Module goes back to its
        * initial state.
        * 
        */
       public void actionPerformed (ActionEvent e)
       {
 
         // Cleaning the Import Data tab components
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
         activePowerRadioButton.setEnabled(false);
         activeAndReactivePowerRadioButton.setEnabled(false);
         activeAndReactivePowerRadioButton.setSelected(true);
         dataReviewPanel.removeAll();
         dataReviewPanel.updateUI();
         consumptionModelPanel.removeAll();
         consumptionModelPanel.updateUI();
         detectedApplianceList.setSelectedIndex(-1);
         detectedAppliances.clear();
         detectedApplianceList.setListData(new String[0]);
         detectedApplianceList.repaint();
 
         // Cleaning the Training Activity Models tab components
         distributionPreviewPanel.removeAll();
         distributionPreviewPanel.updateUI();
         consumptionPreviewPanel.removeAll();
         consumptionPreviewPanel.updateUI();
         selectedApplianceList.setSelectedIndex(-1);
         selectedAppliances.clear();
         selectedApplianceList.setListData(new String[0]);
         selectedApplianceList.repaint();
         timesHistogramRadioButton.setSelected(true);
         durationNormalRadioButton.setSelected(true);
         startGaussianRadioButton.setSelected(true);
 
         // Cleaning the Create Response Models tab components
         monetarySlider.setValue(50);
         environmentalSlider.setValue(50);
         normalCaseRadioButton.setSelected(true);
         previewResponseButton.setEnabled(false);
         createResponseButton.setEnabled(false);
         createResponseAllButton.setEnabled(false);
         pricingPreviewPanel.removeAll();
         pricingPreviewPanel.updateUI();
         responsePanel.removeAll();
         responsePanel.updateUI();
         activitySelectList.setSelectedIndex(-1);
         activityModels.clear();
         activitySelectList.setListData(new String[0]);
         activitySelectList.repaint();
         basicPricingSchemePane.setText("00:00-23:59-0.05");
         newPricingSchemePane.setText("");
         commitButton.setEnabled(false);
 
         // Cleaning the Export Models tab components
         exportModelList.setSelectedIndex(-1);
         exportModels.clear();
         exportModelList.setListData(new String[0]);
         exportModelList.repaint();
         exportPreviewPanel.removeAll();
         exportPreviewPanel.updateUI();
         exportDailyButton.setEnabled(false);
         exportDurationButton.setEnabled(false);
         exportStartButton.setEnabled(false);
         exportStartBinnedButton.setEnabled(false);
         exportButton.setEnabled(false);
         exportAllBaseButton.setEnabled(false);
         exportAllResponseButton.setEnabled(false);
         householdNameTextField.setEnabled(false);
 
         // Disabling the necessary tabs
         tabbedPane.setEnabledAt(1, false);
         tabbedPane.setEnabledAt(2, false);
         tabbedPane.setEnabledAt(3, false);
 
         // Clearing the arrayList in need
         tempAppliances.clear();
         tempActivities.clear();
 
         // Removing temporary files
         Utils.cleanFiles();
 
       }
     });
 
     singleApplianceRadioButton.addActionListener(new ActionListener() {
       /**
        * This function is called when the user presses the Single Appliance
        * radio button on the Data File panel of the Import Data tab.
        */
       public void actionPerformed (ActionEvent e)
       {
         consumptionPathField.setEnabled(false);
         consumptionBrowseButton.setEnabled(false);
         consumptionPathField.setText("");
       }
     });
 
     installationRadioButton.addActionListener(new ActionListener() {
       /**
        * This function is called when the user presses the Installation
        * radio button on the Data File panel of the Import Data tab.
        */
       public void actionPerformed (ActionEvent e)
       {
         consumptionPathField.setEnabled(false);
         consumptionBrowseButton.setEnabled(false);
         consumptionPathField.setText("");
       }
     });
 
     importDataButton.addActionListener(new ActionListener() {
       /**
        * This function is called when the user presses the Import Data
        * button on the Data File panel of the Import Data tab.
        */
       public void actionPerformed (ActionEvent e)
       {
         Component root = SwingUtilities.getRoot((JButton) e.getSource());
 
         try {
 
           root.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 
           // Change the state of some components
           installationRadioButton.setEnabled(false);
           singleApplianceRadioButton.setEnabled(false);
           importDataButton.setEnabled(false);
           dataBrowseButton.setEnabled(false);
           activePowerRadioButton.setEnabled(false);
           activeAndReactivePowerRadioButton.setEnabled(false);
 
           // Check if both active and reactive activeOnly data set are available
           boolean power = activePowerRadioButton.isSelected();
           int parse = -1;
 
           // Parsing the measurements file
           try {
             parse = Utils.parseMeasurementsFile(pathField.getText(), power);
           }
           catch (IOException e2) {
             e2.printStackTrace();
           }
 
           // If everything is OK
           if (parse == -1) {
             try {
               // Creating new installation
               installation = new Installation(pathField.getText(), power);
             }
             catch (IOException e2) {
               e2.printStackTrace();
             }
 
             // Show the measurements in the preview chart
             ChartPanel chartPanel = null;
             try {
               chartPanel = installation.measurementsChart();
             }
             catch (IOException e1) {
               e1.printStackTrace();
             }
 
             dataReviewPanel.add(chartPanel, BorderLayout.CENTER);
             dataReviewPanel.validate();
 
             disaggregateButton.setEnabled(false);
             createEventsButton.setEnabled(false);
 
             // Enable the appropriate buttons given source of measurements
             if (installationRadioButton.isSelected()) {
               disaggregateButton.setEnabled(true);
             }
             else if (singleApplianceRadioButton.isSelected()) {
               consumptionPathField.setEnabled(true);
               consumptionBrowseButton.setEnabled(true);
 
             }
 
             // Add installation to the export models list
             exportModels.addElement(installation.toString());
             exportModels.addElement(installation.getPerson().getName());
             householdNameTextField.setText(installation.getName());
 
             // Enable Export Models tab
             exportModelList.setEnabled(true);
             exportModelList.setModel(exportModels);
             tabbedPane.setEnabledAt(3, true);
 
           }
           // In case of an error during the measurement parsing show the line of
           // error and reset settings.
           else {
             JFrame error = new JFrame();
 
             JOptionPane
                     .showMessageDialog(error,
                                        "Parsing measurements file failed. The problem seems to be in line "
                                                + parse
                                                + ".Check the selected buttons and the file provided and try again.",
                                        "Inane error", JOptionPane.ERROR_MESSAGE);
             resetButton.doClick();
           }
         }
         finally {
           root.setCursor(Cursor.getDefaultCursor());
         }
       }
     });
 
     disaggregateButton.addActionListener(new ActionListener() {
       /**
        * This function is called when the user presses the Disaggregate
        * button on the Data File panel of the Import Data tab in order to
        * automatically analyse the data set and extract the appliances and
        * activities within.
        */
       public void actionPerformed (ActionEvent e)
       {
 
         Component root = SwingUtilities.getRoot((JButton) e.getSource());
 
         try {
 
           root.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 
           // Get auxiliary files containing appliances and activities which are
           // the output of the disaggregation process.
           String filename = pathField.getText();
 
           if (Constants.FILED == false) {
             try {
               Disaggregate dis = new Disaggregate(filename);
             }
             catch (Exception e2) {
               System.out.println("Missing File");
               e2.printStackTrace();
             }
           }
 
           filename =
             pathField.getText().substring(0, pathField.getText().length() - 4);
           File appliancesFile = new File(filename + "ApplianceList.csv");
           File activitiesFile = new File(filename + "ActivityList.csv");
 
           // If these exist, disaggregation was successful and the procedure can
           // continue
           if (appliancesFile.exists() && activitiesFile.exists()) {
 
             // Read appliance file and start appliance parsing
             Scanner input = null;
             try {
               input = new Scanner(appliancesFile);
             }
             catch (FileNotFoundException e1) {
               e1.printStackTrace();
             }
             String nextLine;
             String[] line;
 
             while (input.hasNext()) {
               nextLine = input.nextLine();
               line = nextLine.split(",");
 
               String name = line[line.length - 2] + " " + line[line.length - 1];
               String activity = line[line.length - 2];
               String[] temp = line[line.length - 1].split(" ");
 
               String type = "";
 
               if (temp.length == 1)
                 type = temp[0];
               else {
                 for (int i = 0; i < temp.length - 1; i++)
                   type += temp[i] + " ";
                 type = type.trim();
 
               }
               double p = Double.parseDouble(line[0]);
               double q = Double.parseDouble(line[1]);
               // For each appliance found in the file, an temporary Appliance
               // Entity is created.
               tempAppliances.add(new ApplianceTemp(name,
                                                    installation.getName(),
                                                    type, activity, p, q));
 
             }
 
             System.out.println("Appliances:" + tempAppliances.size());
 
             input.close();
 
             // Read activity file and start activity parsing
 
             try {
               input = new Scanner(activitiesFile);
             }
             catch (FileNotFoundException e1) {
               // TODO Auto-generated catch block
               e1.printStackTrace();
             }
 
             while (input.hasNext()) {
               nextLine = input.nextLine();
               line = nextLine.split(",");
 
               String name = line[line.length - 2];
               int start = Integer.parseInt(line[0]);
               int end = Integer.parseInt(line[1]);
 
               // Search for existing activity
               int activityIndex = findActivity(name);
 
               // if not found, create a new one
               if (activityIndex == -1) {
 
                 ActivityTemp newActivity = new ActivityTemp(name);
                 newActivity.addEvent(start, end);
                 tempActivities.add(newActivity);
 
               }
               // else add data to the found activity
               else
                 tempActivities.get(activityIndex).addEvent(start, end);
             }
 
             // This is hard copied for now
             int activityIndex = findActivity("Refrigeration");
             if (activityIndex != -1) {
               tempActivities.remove(activityIndex);
               System.out.println("Refrigeration Removed");
             }
             // TODO Add these lines in case we want to remove activities with
             // small sampling number
 
             // System.out.println(tempActivities.size());
             // for (int i = tempActivities.size() - 1; i >= 0; i--)
             // if (tempActivities.get(i).getEvents().size() < threshold)
             // tempActivities.remove(i);
 
             // Create an event file for each activity, in order to be able to
             // use
             // it for training the beahviour models if asked from the user
             for (int i = 0; i < tempActivities.size(); i++) {
               // tempActivities.get(i).status();
               try {
                 tempActivities.get(i).createEventFile();
               }
               catch (IOException e1) {
                 e1.printStackTrace();
               }
             }
 
             input.close();
 
             // Add each found appliance (after converting temporary appliance to
             // normal appliance) in the installation Entity, to the detected
             // appliance and export models list
             for (ApplianceTemp temp: tempAppliances) {
 
               Appliance tempAppliance = temp.toAppliance();
 
               installation.addAppliance(tempAppliance);
               detectedAppliances.addElement(tempAppliance.toString());
               exportModels.addElement(tempAppliance.toString());
 
             }
 
             // Add appliances corresponding to each activity, remove activities
             // without appliances and add activities to the selected activities
             // list.
             for (int i = tempActivities.size() - 1; i >= 0; i--) {
 
               tempActivities.get(i).setAppliances(findAppliances(tempActivities
                                                           .get(i)));
               if (tempActivities.get(i).getAppliances().size() == 0) {
                 tempActivities.remove(i);
               }
               else
                 selectedAppliances.addElement(tempActivities.get(i).toString());
 
             }
 
           }
           // Demonstration of the disaggregation in case it was not successful.
           // For presentation purposes only.
           else {
 
             int temp = 8 + ((int) (Math.random() * 2));
 
             for (int i = 0; i < temp; i++) {
 
               String name = "Appliance " + i;
               String powerModel = "";
               String reactiveModel = "";
               int tempIndex = i % 5;
               switch (tempIndex) {
               case 0:
                 powerModel =
                   "{\"n\":1,\"params\":[{\"n\":1,\"values\":[{\"p\":1900,\"d\":1,\"s\":0}]},{\"n\":0,\"values\":[{\"p\":300,\"d\":1,\"s\":0}]}]}";
                 reactiveModel =
                   "{\"n\":1,\"params\":[{\"n\":1,\"values\":[{\"q\":-40,\"d\":1,\"s\":0}]},{\"n\":0,\"values\":[{\"q\":-10,\"d\":1,\"s\":0}]}]}";
                 break;
               case 1:
                 powerModel =
                   "{ \"n\" : 0, \"params\" : [{ \"n\" : 1, \"values\" : [ {\"p\" : 140.0, \"d\" : 20, \"s\": 0.0}]}]}";
                 reactiveModel =
                   "{ \"n\" : 0, \"params\" : [{ \"n\" : 1, \"values\" : [ {\"q\" : 120.0, \"d\" : 20, \"s\": 0.0}]}]}";
                 break;
               case 2:
                 powerModel =
                   "{ \"n\" : 0, \"params\" : [{ \"n\" : 1, \"values\" : [ {\"p\" : 95.0, \"d\" : 20, \"s\": 0.0}, {\"p\" :80.0, \"d\" : 18, \"s\": 0.0}, {\"p\" : 0.0, \"d\" : 73, \"s\": 0.0}]}]}]}";
                 reactiveModel =
                   "{ \"n\" : 0, \"params\" : [{ \"n\" : 1, \"values\" : [ {\"q\" : 0.0, \"d\" : 20, \"s\": 0.0}, {\"q\" : 0.0, \"d\" : 18, \"s\": 0.0}, {\"q\" : 0.0, \"d\" : 73, \"s\": 0.0}]}]}]}";
                 break;
               case 3:
                 powerModel =
                   "{ \"n\" : 0, \"params\" : [{ \"n\" : 1, \"values\" : [ {\"p\" : 30.0, \"d\" : 20, \"s\": 0.0}]}]}";
                 reactiveModel =
                   "{ \"n\" : 0, \"params\" : [{ \"n\" : 1, \"values\" : [ {\"q\" : -5.0, \"d\" : 20, \"s\": 0.0}]}]}";
                 break;
               case 4:
                 powerModel =
                   "{\"n\":1,\"params\":[{\"n\":1,\"values\":[{\"p\":150,\"d\":25,\"s\":0},{\"p\":2000,\"d\":13,\"s\":0},{\"p\":100,\"d\":62,\"s\":0}]}]}";
                 reactiveModel =
                   "{\"n\":1,\"params\":[{\"n\":1,\"values\":[{\"q\":400,\"d\":25,\"s\":0},{\"q\":200,\"d\":13,\"s\":0},{\"q\":300,\"d\":62,\"s\":0}]}]}";
                 break;
               }
 
               Appliance tempAppliance =
                 new Appliance(name, installation.getName(), powerModel,
                               reactiveModel, "Demo/eventsAll" + tempIndex
                                              + ".csv");
 
               installation.addAppliance(tempAppliance);
               detectedAppliances.addElement(tempAppliance.toString());
               selectedAppliances.addElement(tempAppliance.toString());
               exportModels.addElement(tempAppliance.toString());
             }
           }
 
           // Enable all appliance/activity lists
           detectedApplianceList.setEnabled(true);
           detectedApplianceList.setModel(detectedAppliances);
           detectedApplianceList.setSelectedIndex(0);
 
           tabbedPane.setEnabledAt(1, true);
           selectedApplianceList.setEnabled(true);
           selectedApplianceList.setModel(selectedAppliances);
 
           // exportModelList.setEnabled(true);
           // exportModelList.setModel(exportModels);
           // tabbedPane.setEnabledAt(3, true);
 
           // Disable unnecessary buttons.
           disaggregateButton.setEnabled(false);
           createEventsButton.setEnabled(false);
         }
         finally {
           root.setCursor(Cursor.getDefaultCursor());
         }
       }
     });
 
     createEventsButton.addActionListener(new ActionListener() {
       /**
        * This function is called when the user presses the Create Events
        * button on the Data File panel of the Import Data tab. This button is
        * used when there is a single appliance with an known consumption model
        * so that the events can be extracted automatically from the data set.
        * Used for presentation purposes only since is depricated by the
        * disaggregation function.
        */
       public void actionPerformed (ActionEvent e)
       {
         Component root = SwingUtilities.getRoot((JButton) e.getSource());
 
         try {
 
           root.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 
           // Parse the consumption model file
           File file = new File(consumptionPathField.getText());
           String temp = file.getName();
           temp = temp.replace(".", " ");
           String name = temp.split(" ")[0];
 
           Appliance appliance = null;
           try {
 
             int rand = (int) (Math.random() * 5);
 
             appliance =
               new Appliance(name, consumptionPathField.getText(),
                             consumptionPathField.getText(), "Demo/eventsAll"
                                                             + rand + ".csv",
                             installation, activePowerRadioButton.isSelected());
           }
           catch (IOException e1) {
             e1.printStackTrace();
           }
           // Add appliance to the installation entity
           installation.addAppliance(appliance);
 
           // Enable all appliance/activity lists
           detectedAppliances.addElement(appliance.toString());
           selectedAppliances.addElement(appliance.toString());
           exportModels.addElement(appliance.toString());
 
           detectedApplianceList.setEnabled(true);
           detectedApplianceList.setModel(detectedAppliances);
           detectedApplianceList.setSelectedIndex(0);
 
           tabbedPane.setEnabledAt(1, true);
           selectedApplianceList.setEnabled(true);
           selectedApplianceList.setModel(selectedAppliances);
 
           // exportModelList.setEnabled(true);
           // exportModelList.setModel(exportModels);
           // tabbedPane.setEnabledAt(3, true);
 
           // Disable unnecessary buttons.
           disaggregateButton.setEnabled(false);
           createEventsButton.setEnabled(false);
         }
         finally {
           root.setCursor(Cursor.getDefaultCursor());
         }
       }
     });
 
     // APPLIANCE DETECTION //
     detectedApplianceList.addListSelectionListener(new ListSelectionListener() {
       /**
        * This function is called when the user selects an appliance from the
        * list of Detected Appliances on the Disaggregation panel of the Import
        * Data tab. Then the corresponding consumption model is presented in the
        * Consumption Model Preview panel.
        */
       public void valueChanged (ListSelectionEvent e)
       {
 
         consumptionModelPanel.removeAll();
         consumptionModelPanel.updateUI();
 
         if (detectedAppliances.size() >= 1) {
 
           String selection = detectedApplianceList.getSelectedValue();
 
           Appliance current = installation.findAppliance(selection);
 
           ChartPanel chartPanel = current.consumptionGraph();
 
           consumptionModelPanel.add(chartPanel, BorderLayout.CENTER);
           consumptionModelPanel.validate();
 
         }
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
       /**
        * This function is called when the user presses the Train button on
        * the Training Parameters panel of the Train Activity Models tab. It
        * contains the procedure needed to create an activity model based on the
        * event set of the appliance or activity.
        */
       public void actionPerformed (ActionEvent e)
       {
 
         Component root = SwingUtilities.getRoot((JButton) e.getSource());
 
         try {
 
           root.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 
           // Searching for existing activity or appliance.
           String selection = selectedApplianceList.getSelectedValue();
           ActivityTemp activity = null;
 
           if (tempActivities.size() > 0)
             activity = tempActivities.get(findActivity(selection));
 
           Appliance current = installation.findAppliance(selection);
 
           String startTime, duration, dailyTimes;
 
           // Check for the selected distribution methods for training.
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
 
           if (startHistogramRadioButton.isSelected())
             startTime = "Histogram";
           else if (startNormalRadioButton.isSelected())
             startTime = "Normal";
           else
             startTime = "GMM";
 
           String[] distributions =
             { dailyTimes, duration, startTime, "Histogram" };
 
           // If the selected object from the list is an appliance the training
           // procedure for the appliance begins.
           if (activity == null) {
 
             try {
               installation.getPerson().train(current, distributions);
             }
             catch (IOException e1) {
               e1.printStackTrace();
             }
           }
           // If the selected object from the list is an activity the training
           // procedure for the activity begins.
           else {
 
             try {
               installation.getPerson().train(activity, distributions);
             }
             catch (IOException e1) {
               e1.printStackTrace();
             }
 
           }
 
           // System.out.println("Training OK!");
 
           distributionPreviewPanel.removeAll();
           distributionPreviewPanel.updateUI();
 
           // Show the distribution created on the Distribution Preview Panel
           ActivityModel activityModel =
             installation.getPerson().findActivity(selection, true);
 
           if (activityModel == null)
             activityModel = installation.getPerson().findActivity(current);
 
           ChartPanel chartPanel =
             activityModel.createDailyTimesDistributionChart();
           distributionPreviewPanel.add(chartPanel, BorderLayout.CENTER);
           distributionPreviewPanel.validate();
 
           // Add the Activity model to the list of trained Activity models of
           // the Create Response Models tab
           int size = activitySelectList.getModel().getSize();
 
           if (size > 0) {
             activityModels =
               (DefaultListModel<String>) activitySelectList.getModel();
             if (activityModels.contains(activityModel.getName()) == false)
               activityModels.addElement(activityModel.getName());
           }
           else {
             activityModels = new DefaultListModel<String>();
             activityModels.addElement(activityModel.getName());
             activitySelectList.setEnabled(true);
           }
 
           activitySelectList.setModel(activityModels);
 
           // Add the trained model to the export list also.
           size = exportModelList.getModel().getSize();
           if (size > 0) {
             exportModels =
               (DefaultListModel<String>) exportModelList.getModel();
             if (exportModels.contains(activityModel.getName()) == false)
               exportModels.addElement(activityModel.getName());
           }
           else {
             exportModels = new DefaultListModel<String>();
             exportModels.addElement(activityModel.getName());
             exportModelList.setEnabled(true);
           }
 
           // Enable some buttons necessary to show the results.
           dailyTimesButton.setEnabled(true);
           durationButton.setEnabled(true);
           startTimeButton.setEnabled(true);
           startTimeBinnedButton.setEnabled(true);
 
           exportModelList.setModel(exportModels);
 
           exportDailyButton.setEnabled(true);
           exportDurationButton.setEnabled(true);
           exportStartButton.setEnabled(true);
           exportStartBinnedButton.setEnabled(true);
 
           tabbedPane.setEnabledAt(2, true);
         }
 
         finally {
           root.setCursor(Cursor.getDefaultCursor());
         }
       }
     });
 
     trainAllButton.addActionListener(new ActionListener() {
       /**
        * This function is called when the user presses the Train All button on
        * the Training Parameters panel of the Train Activity Models tab. It
        * is iterating the aforementioned training procedure to each of the
        * objects on the list.
        */
       public void actionPerformed (ActionEvent e)
       {
         for (int i = 0; i < selectedApplianceList.getModel().getSize(); i++) {
           selectedApplianceList.setSelectedIndex(i);
           trainingButton.doClick();
         }
       }
     });
 
     dailyTimesButton.addActionListener(new ActionListener() {
       /**
        * This function is called when the user presses the Daily Times button on
        * the Distribution Preview panel of the Train Activity Models tab. It
        * shows the Daily Times Distribution for the selected object from the
        * list if available.
        */
       public void actionPerformed (ActionEvent arg0)
       {
 
         distributionPreviewPanel.removeAll();
         distributionPreviewPanel.updateUI();
 
         String selection = selectedApplianceList.getSelectedValue();
 
         Appliance current = installation.findAppliance(selection);
 
         ActivityModel activityModel =
           installation.getPerson().findActivity(selection, true);
 
         if (activityModel == null)
           activityModel = installation.getPerson().findActivity(current);
 
         ChartPanel chartPanel =
           activityModel.createDailyTimesDistributionChart();
         distributionPreviewPanel.add(chartPanel, BorderLayout.CENTER);
         distributionPreviewPanel.validate();
 
       }
     });
 
     startTimeBinnedButton.addActionListener(new ActionListener() {
       /**
        * This function is called when the user presses the Start Time Binned
        * button on the Distribution Preview panel of the Train Activity
        * Models tab. It shows the Start Time Binned Distribution for the
        * selected object from the list if available.
        */
       public void actionPerformed (ActionEvent arg0)
       {
 
         distributionPreviewPanel.removeAll();
         distributionPreviewPanel.updateUI();
 
         String selection = selectedApplianceList.getSelectedValue();
 
         Appliance current = installation.findAppliance(selection);
 
         ActivityModel activityModel =
           installation.getPerson().findActivity(selection, true);
 
         if (activityModel == null)
           activityModel = installation.getPerson().findActivity(current);
 
         ChartPanel chartPanel =
           activityModel.createStartTimeBinnedDistributionChart();
         distributionPreviewPanel.add(chartPanel, BorderLayout.CENTER);
         distributionPreviewPanel.validate();
 
       }
     });
 
     startTimeButton.addActionListener(new ActionListener() {
       /**
        * This function is called when the user presses the Start Time
        * button on the Distribution Preview panel of the Train Activity
        * Models tab. It shows the Start Time Distribution for the selected
        * object from the list if available.
        */
       public void actionPerformed (ActionEvent arg0)
       {
 
         distributionPreviewPanel.removeAll();
         distributionPreviewPanel.updateUI();
 
         String selection = selectedApplianceList.getSelectedValue();
 
         Appliance current = installation.findAppliance(selection);
 
         ActivityModel activityModel =
           installation.getPerson().findActivity(selection, true);
 
         if (activityModel == null)
           activityModel = installation.getPerson().findActivity(current);
 
         ChartPanel chartPanel =
           activityModel.createStartTimeDistributionChart();
         distributionPreviewPanel.add(chartPanel, BorderLayout.CENTER);
         distributionPreviewPanel.validate();
 
       }
     });
 
     durationButton.addActionListener(new ActionListener() {
       /**
        * This function is called when the user presses the Duration
        * button on the Distribution Preview panel of the Train Activity
        * Models tab. It shows the Duration Distribution for the selected
        * object from the list if available.
        */
       public void actionPerformed (ActionEvent arg0)
       {
 
         distributionPreviewPanel.removeAll();
         distributionPreviewPanel.updateUI();
 
         String selection = selectedApplianceList.getSelectedValue();
 
         Appliance current = installation.findAppliance(selection);
 
         ActivityModel activityModel =
           installation.getPerson().findActivity(selection, true);
 
         if (activityModel == null)
           activityModel = installation.getPerson().findActivity(current);
 
         ChartPanel chartPanel = activityModel.createDurationDistributionChart();
         distributionPreviewPanel.add(chartPanel, BorderLayout.CENTER);
         distributionPreviewPanel.validate();
 
       }
     });
 
     selectedApplianceList.addListSelectionListener(new ListSelectionListener() {
       /**
        * This function is called when the user selects an appliance or activity
        * from the list of Selected Appliances on the Appliance / Activity
        * Selection panel of the Train Activity Models tab. Then an example
        * corresponding consumption model is presented in the Consumption Model
        * Preview panel.
        */
       public void valueChanged (ListSelectionEvent arg0)
       {
 
         consumptionPreviewPanel.removeAll();
         consumptionPreviewPanel.updateUI();
         distributionPreviewPanel.removeAll();
         distributionPreviewPanel.updateUI();
 
         // If there are any appliances / activities on the list
         if (selectedAppliances.size() >= 1) {
 
           // Find the corresponding appliance / activity and show its
           // consumption model
           String selection = selectedApplianceList.getSelectedValue();
 
           Appliance currentAppliance = installation.findAppliance(selection);
 
           ChartPanel chartPanel = null;
           if (currentAppliance != null)
             chartPanel = currentAppliance.consumptionGraph();
           else {
             ActivityTemp currentActivity =
               tempActivities.get(findActivity(selection));
             chartPanel = currentActivity.consumptionGraph();
           }
 
           consumptionPreviewPanel.add(chartPanel, BorderLayout.CENTER);
           consumptionPreviewPanel.validate();
 
           // If there is also an Activity model trained, show the corresponding
           // distribution charts on the Distribution Preview panel
           ActivityModel activityModel = null;
 
           if (currentAppliance != null)
             activityModel =
               installation.getPerson().findActivity(currentAppliance);
 
           if (activityModel == null)
             activityModel =
               installation.getPerson().findActivity(selection, true);
 
           if (activityModel != null) {
 
             ChartPanel chartPanel2 =
               activityModel.createDailyTimesDistributionChart();
             distributionPreviewPanel.add(chartPanel2, BorderLayout.CENTER);
             distributionPreviewPanel.validate();
             distributionPreviewPanel.updateUI();
 
           }
         }
 
       }
     });
 
     // RESPONSE TAB //
 
     createResponseTab.addComponentListener(new ComponentAdapter() {
       @Override
       public void componentShown (ComponentEvent arg0)
       {
         activitySelectList.setSelectedIndex(0);
       }
     });
 
     previewResponseButton.addActionListener(new ActionListener() {
       /**
        * This function is called when the user presses the Preview Response
        * button on the Response Parameters panel of the Create Response Models
        * tab. This button is enabled after selecting activity model, response
        * type and pricing for testing and presents a preview of the response
        * model that may be extracted.
        */
       public void actionPerformed (ActionEvent e)
       {
 
         Component root = SwingUtilities.getRoot((JButton) e.getSource());
 
         try {
 
           root.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 
           responsePanel.removeAll();
 
           // Find the selected activity
           ActivityModel activity =
             installation.getPerson().findActivity(activitySelectList
                                                           .getSelectedValue(),
                                                   false);
 
           int response = -1;
 
           // Check for the selected response type
           if (optimalCaseRadioButton.isSelected())
             response = 0;
           else if (normalCaseRadioButton.isSelected())
             response = 1;
           else
             response = 2;
 
           // Parse the pricing schemes
           double[] basicScheme =
             Utils.parseScheme(basicPricingSchemePane.getText());
           double[] newScheme =
             Utils.parseScheme(newPricingSchemePane.getText());
 
           // Create a preview chart of the response model
           ChartPanel chartPanel =
             installation.getPerson().previewResponse(activity, response,
                                                      basicScheme, newScheme);
           responsePanel.add(chartPanel, BorderLayout.CENTER);
           responsePanel.validate();
 
           createResponseButton.setEnabled(true);
           createResponseAllButton.setEnabled(true);
         }
         finally {
           root.setCursor(Cursor.getDefaultCursor());
         }
       }
     });
 
     createResponseButton.addActionListener(new ActionListener() {
       /**
        * This function is called when the user presses the Create Response Model
        * button on the Response Parameters panel of the Create Response Models
        * tab. This button is enabled after preview results of the selected
        * activity model, response type and pricing for testing and creates the
        * response model for the user.
        */
       public void actionPerformed (ActionEvent e)
       {
         Component root = SwingUtilities.getRoot((JButton) e.getSource());
 
         try {
 
           root.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 
           exportPreviewPanel.removeAll();
           exportPreviewPanel.updateUI();
 
           int responseType = -1;
           String responseString = "";
           // Check for the selected response type
           if (optimalCaseRadioButton.isSelected()) {
             responseType = 0;
             responseString = "Optimal";
           }
           else if (normalCaseRadioButton.isSelected()) {
             responseType = 1;
             responseString = "Normal";
           }
           else if (discreteCaseRadioButton.isSelected()) {
             responseType = 2;
             responseString = "Discrete";
           }
 
           // Parse the pricing schemes
           double[] basicScheme =
             Utils.parseScheme(basicPricingSchemePane.getText());
           double[] newScheme =
             Utils.parseScheme(newPricingSchemePane.getText());
 
           // Create the response model
           ActivityModel activity =
             installation.getPerson().findActivity(activitySelectList
                                                           .getSelectedValue(),
                                                   false);
 
           String response = "";
 
           try {
             response =
               installation.getPerson().createResponse(activity, responseType,
                                                       basicScheme, newScheme);
           }
           catch (IOException exc) {
 
             exc.printStackTrace();
           }
 
           // Add the response model extracted to the export model list.
           int size = exportModelList.getModel().getSize();
           // System.out.println(size);
 
           if (size > 0) {
             exportModels =
               (DefaultListModel<String>) exportModelList.getModel();
 
             String response2 = "", response3 = "";
             if (responseString.equalsIgnoreCase("Optimal")) {
               response2 = response.replace(responseString, "Normal");
               response3 = response.replace(responseString, "Discrete");
             }
             else if (responseString.equalsIgnoreCase("Normal")) {
               response2 = response.replace(responseString, "Optimal");
               response3 = response.replace(responseString, "Discrete");
             }
             else {
               response2 = response.replace(responseString, "Optimal");
               response3 = response.replace(responseString, "Normal");
             }
 
             if (exportModels.contains(response2))
               exportModels.removeElement(response2);
             if (exportModels.contains(response3))
               exportModels.removeElement(response3);
 
             if (exportModels.contains(response) == false)
               exportModels.addElement(response);
           }
           else {
             exportModels = new DefaultListModel<String>();
             exportModels.addElement(response);
             exportModelList.setEnabled(true);
           }
           exportModelList.setModel(exportModels);
 
           if (manyFlag == false) {
 
             JFrame success = new JFrame();
 
             JOptionPane
                     .showMessageDialog(success, "The response model "
                                                 + response
                                                 + " was created successfully",
                                        "Response Model Created",
                                        JOptionPane.INFORMATION_MESSAGE);
           }
         }
 
         finally {
           root.setCursor(Cursor.getDefaultCursor());
         }
       }
     });
 
     createResponseAllButton.addActionListener(new ActionListener() {
       /**
        * This function is called when the user presses the Create Response All
        * button on the Response Parameters panel of the Create Response Models
        * tab. This is achieved by iterating the procedure above for all the
        * available activity models in the list.
        */
       public void actionPerformed (ActionEvent arg0)
       {
         manyFlag = true;
 
         for (int i = 0; i < activitySelectList.getModel().getSize(); i++) {
           activitySelectList.setSelectedIndex(i);
           createResponseButton.doClick();
         }
 
         JFrame success = new JFrame();
 
         JOptionPane
                 .showMessageDialog(success,
                                    "The response models were created successfully",
                                    "Response Models Created",
                                    JOptionPane.INFORMATION_MESSAGE);
 
         manyFlag = false;
       }
     });
 
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
       /**
        * This function is called when the user presses the Commit button on the
        * Pricing Scheme panel of the Create Response Models tab. This button is
        * enabled after adding the two pricing schemes that are prerequisites for
        * the creation of a response model.
        */
       public void actionPerformed (ActionEvent e)
       {
 
         Component root = SwingUtilities.getRoot((JButton) e.getSource());
 
         try {
 
           root.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 
           boolean basicScheme = false;
           boolean newScheme = false;
           int parseBasic = 0;
           int parseNew = 0;
 
           pricingPreviewPanel.removeAll();
 
           // Check if both pricing schemes are entered
           if (basicPricingSchemePane.getText().equalsIgnoreCase("") == false)
             basicScheme = true;
 
           if (newPricingSchemePane.getText().equalsIgnoreCase("") == false)
             newScheme = true;
 
           // Parse the pricing schemes for errors
           if (basicScheme)
             parseBasic =
               Utils.parsePricingScheme(basicPricingSchemePane.getText());
 
           if (newScheme)
             parseNew = Utils.parsePricingScheme(newPricingSchemePane.getText());
 
           // If errors are found then present the line the error may be at
           if (parseBasic != -1) {
             JFrame error = new JFrame();
 
             JOptionPane
                     .showMessageDialog(error,
                                        "Basic Pricing Scheme is not defined correctly. Please check your input in line "
                                                + parseBasic + " and try again.",
                                        "Inane error", JOptionPane.ERROR_MESSAGE);
           }
           else if (parseNew != -1) {
             JFrame error = new JFrame();
 
             JOptionPane.showMessageDialog(error,
                                           "New Pricing Scheme is not defined correctly. Please check your input in line "
                                                   + parseNew
                                                   + " and try again.",
                                           "Inane error",
                                           JOptionPane.ERROR_MESSAGE);
           }
           // If no errors are found make a preview chart of the two pricing
           // schemes
           else {
             if (basicScheme && newScheme) {
               ChartPanel chartPanel =
                 ChartUtils.parsePricingScheme(basicPricingSchemePane.getText(),
                                               newPricingSchemePane.getText());
 
               pricingPreviewPanel.add(chartPanel, BorderLayout.CENTER);
               pricingPreviewPanel.validate();
 
               previewResponseButton.setEnabled(true);
 
             }
             else {
               JFrame error = new JFrame();
 
               JOptionPane
                       .showMessageDialog(error,
                                          "You have not defined both pricing schemes.Please check your input and try again.",
                                          "Inane error",
                                          JOptionPane.ERROR_MESSAGE);
               previewResponseButton.setEnabled(false);
             }
           }
         }
 
         finally {
           root.setCursor(Cursor.getDefaultCursor());
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
       /**
        * This function is called when the user selects an entity from the
        * list of models on the Model Export Selection panel of the Export Models
        * tab. Then the corresponding preview of the entity model is presented in
        * the
        * Export Model Preview panel.
        */
       public void valueChanged (ListSelectionEvent arg0)
       {
         if (tabbedPane.getSelectedIndex() == 3) {
           exportPreviewPanel.removeAll();
           exportPreviewPanel.updateUI();
 
           // Checking if the list has any object
           if (exportModels.size() > 1) {
             String selection = exportModelList.getSelectedValue();
 
             // Check to see what type of entity is selected (Installation,
             // Person, Appliance, Activity, Response)
             Appliance appliance = installation.findAppliance(selection);
 
             ActivityModel activity =
               installation.getPerson().findActivity(selection, false);
 
             ResponseModel response =
               installation.getPerson().findResponse(selection);
 
             // Create the appropriate chart for the selected entity and show it.
             ChartPanel chartPanel = null;
 
             if (selection.equalsIgnoreCase(installation.getName())) {
 
               try {
                 chartPanel = installation.measurementsChart();
 
                 exportDailyButton.setEnabled(false);
                 exportDurationButton.setEnabled(false);
                 exportStartButton.setEnabled(false);
                 exportStartBinnedButton.setEnabled(false);
               }
               catch (IOException e1) {
                 e1.printStackTrace();
               }
 
             }
             else if (selection.equalsIgnoreCase(installation.getPerson()
                     .getName())) {
 
               chartPanel = installation.getPerson().statisticGraphs();
 
               exportDailyButton.setEnabled(false);
               exportDurationButton.setEnabled(false);
               exportStartButton.setEnabled(false);
               exportStartBinnedButton.setEnabled(false);
 
             }
             else if (appliance != null) {
 
               chartPanel = appliance.consumptionGraph();
 
               exportDailyButton.setEnabled(false);
               exportDurationButton.setEnabled(false);
               exportStartButton.setEnabled(false);
               exportStartBinnedButton.setEnabled(false);
 
             }
             else if (activity != null) {
 
               chartPanel = activity.createDailyTimesDistributionChart();
 
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
         }
       }
     });
 
     exportDailyButton.addActionListener(new ActionListener() {
       /**
        * This function is called when the user presses the Daily Times
        * button on the Entity Preview panel of the Export Models tab. It shows
        * the Daily Times Distribution for the selected object from the list.
        */
       public void actionPerformed (ActionEvent arg0)
       {
 
         exportPreviewPanel.removeAll();
         exportPreviewPanel.updateUI();
 
         String selection = exportModelList.getSelectedValue();
 
         ActivityModel activity =
           installation.getPerson().findActivity(selection, false);
 
         ResponseModel response =
           installation.getPerson().findResponse(selection);
 
         ChartPanel chartPanel = null;
 
         if (activity != null)
           chartPanel = activity.createDailyTimesDistributionChart();
 
         else
           chartPanel = response.createDailyTimesDistributionChart();
 
         exportPreviewPanel.add(chartPanel, BorderLayout.CENTER);
         exportPreviewPanel.validate();
       }
     });
 
     exportStartBinnedButton.addActionListener(new ActionListener() {
       /**
        * This function is called when the user presses the Start Time Binned
        * button on the Entity Preview panel of the Export Models tab. It shows
        * the Start Time Binned Distribution for the selected object from the
        * list.
        */
       public void actionPerformed (ActionEvent arg0)
       {
 
         exportPreviewPanel.removeAll();
         exportPreviewPanel.updateUI();
 
         String selection = exportModelList.getSelectedValue();
 
         ActivityModel activity =
           installation.getPerson().findActivity(selection, false);
 
         ResponseModel response =
           installation.getPerson().findResponse(selection);
 
         ChartPanel chartPanel = null;
 
         if (activity != null)
           chartPanel = activity.createStartTimeBinnedDistributionChart();
 
         else
           chartPanel = response.createStartTimeBinnedDistributionChart();
 
         exportPreviewPanel.add(chartPanel, BorderLayout.CENTER);
         exportPreviewPanel.validate();
 
       }
     });
 
     exportStartButton.addActionListener(new ActionListener() {
       /**
        * This function is called when the user presses the Start Time
        * button on the Entity Preview panel of the Export Models tab. It shows
        * the Start Time Distribution for the selected object from the list.
        */
       public void actionPerformed (ActionEvent arg0)
       {
 
         exportPreviewPanel.removeAll();
         exportPreviewPanel.updateUI();
 
         String selection = exportModelList.getSelectedValue();
 
         ActivityModel activity =
           installation.getPerson().findActivity(selection, false);
 
         ResponseModel response =
           installation.getPerson().findResponse(selection);
 
         ChartPanel chartPanel = null;
 
         if (activity != null)
           chartPanel = activity.createStartTimeDistributionChart();
         else
           chartPanel = response.createStartTimeDistributionChart();
 
         exportPreviewPanel.add(chartPanel, BorderLayout.CENTER);
         exportPreviewPanel.validate();
 
       }
     });
 
     exportDurationButton.addActionListener(new ActionListener() {
       /**
        * This function is called when the user presses the Duration
        * button on the Entity Preview panel of the Export Models tab. It shows
        * the Duration Distribution for the selected object from the list.
        */
       public void actionPerformed (ActionEvent arg0)
       {
 
         exportPreviewPanel.removeAll();
         exportPreviewPanel.updateUI();
 
         String selection = exportModelList.getSelectedValue();
 
         ActivityModel activity =
           installation.getPerson().findActivity(selection, false);
 
         ResponseModel response =
           installation.getPerson().findResponse(selection);
 
         ChartPanel chartPanel = null;
 
         if (activity != null)
           chartPanel = activity.createDurationDistributionChart();
         else
           chartPanel = response.createDurationDistributionChart();
 
         exportPreviewPanel.add(chartPanel, BorderLayout.CENTER);
         exportPreviewPanel.validate();
 
       }
     });
 
     connectButton.addActionListener(new ActionListener() {
       /**
        * This function is called when the user presses the Connect button on the
        * Connection Properties panel of the Export Models tab. It helps the user
        * to connect to his Cassandra Library and export the models he created
        * there.
        */
       public void actionPerformed (ActionEvent e)
       {
         boolean result = false;
 
         // Reads the user credentials and the server to connect to.
         try {
           APIUtilities.setUrl(urlTextField.getText());
 
           result =
             APIUtilities.sendUserCredentials(usernameTextField.getText(),
                                              passwordField.getPassword());
         }
         catch (Exception e1) {
           e1.printStackTrace();
         }
 
         // If the use credentials are correct
         if (result) {
           exportButton.setEnabled(true);
           exportAllBaseButton.setEnabled(true);
           exportAllResponseButton.setEnabled(true);
           householdNameTextField.setEnabled(true);
         }
         // Else a error message appears.
         else {
           JFrame error = new JFrame();
 
           JOptionPane
                   .showMessageDialog(error,
                                      "User Credentials are not correct! Please try again.",
                                      "Inane error", JOptionPane.ERROR_MESSAGE);
           passwordField.setText("");
         }
 
       }
     });
 
     passwordField.addCaretListener(new CaretListener() {
       public void caretUpdate (CaretEvent e)
       {
         String pass = String.valueOf(passwordField.getPassword());
 
         if (pass.equals("")) {
           connectButton.setEnabled(false);
         }
         else
           connectButton.setEnabled(true);
       }
     });
 
     exportButton.addActionListener(new ActionListener() {
       /**
        * This function is called when the user presses the Export button on the
        * Connection Properties panel of the Export Models tab. The entity model
        * selected from the list is then exported to the User Library in
        * Cassandra Platform.
        */
       public void actionPerformed (ActionEvent e)
       {
         Component root = SwingUtilities.getRoot((JButton) e.getSource());
 
         try {
 
           root.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 
           // Parsing the selected entity and find out what type of entity it is.
           String selection = exportModelList.getSelectedValue();
 
           Appliance appliance = installation.findAppliance(selection);
 
           ActivityModel activity =
             installation.getPerson().findActivity(selection, false);
 
           ResponseModel response =
             installation.getPerson().findResponse(selection);
 
           // If it is installation
           if (selection.equalsIgnoreCase(installation.getName())) {
             String oldName = installation.getName();
             installation.setName(householdNameTextField.getText());
 
             try {
               installation.setInstallationID(APIUtilities
                       .sendEntity(installation.toJSON(APIUtilities.getUserID())
                               .toString(), "/inst"));
 
             }
             catch (IOException | AuthenticationException
                    | NoSuchAlgorithmException e1) {
               e1.printStackTrace();
             }
 
             installation.setName(oldName);
 
             JFrame success = new JFrame();
 
             JOptionPane.showMessageDialog(success,
                                           "The installation model "
                                                   + installation.getName()
                                                   + " was exported successfully",
                                           "Installation Model Exported",
                                           JOptionPane.INFORMATION_MESSAGE);
 
           }
           // If it is person
           else if (selection.equalsIgnoreCase(installation.getPerson()
                   .getName())) {
 
             try {
               installation
                       .getPerson()
                       .setPersonID(APIUtilities.sendEntity(installation
                                                                    .getPerson()
                                                                    .toJSON(APIUtilities
                                                                                    .getUserID())
                                                                    .toString(),
                                                            "/pers"));
             }
             catch (IOException | AuthenticationException
                    | NoSuchAlgorithmException e1) {
               e1.printStackTrace();
             }
 
             JFrame success = new JFrame();
 
             JOptionPane
                     .showMessageDialog(success, "The person model "
                                                 + installation.getPerson()
                                                         .getName()
                                                 + " was exported successfully",
                                        "Person Model Exported",
                                        JOptionPane.INFORMATION_MESSAGE);
 
           }
           // If it is appliance
           else if (appliance != null) {
 
             try {
               appliance.setApplianceID(APIUtilities.sendEntity(appliance
                       .toJSON(APIUtilities.getUserID()).toString(), "/app"));
 
               APIUtilities.sendEntity(appliance.powerConsumptionModelToJSON()
                       .toString(), "/consmod");
 
             }
             catch (IOException | AuthenticationException
                    | NoSuchAlgorithmException e1) {
               e1.printStackTrace();
             }
 
             JFrame success = new JFrame();
 
             JOptionPane.showMessageDialog(success,
                                           "The appliance model "
                                                   + appliance.getName()
                                                   + " was exported successfully",
                                           "Appliance Model Exported",
                                           JOptionPane.INFORMATION_MESSAGE);
 
           }
           // If it is activity
           else if (activity != null) {
 
             String[] applianceTemp =
               new String[activity.getAppliancesOf().length];
             String activityTemp = "";
             String durationTemp = "";
             String dailyTemp = "";
             String startTemp = "";
 
             // For each appliance that participates in the activity
             for (int i = 0; i < activity.getAppliancesOf().length; i++) {
 
               Appliance activityAppliance =
                 installation.findAppliance(activity.getAppliancesOf()[i]);
 
               try {
                 // In case the appliances contained in the Activity model are
                 // not
                 // in the database, we create the object there before sending
                 // the
                 // activity model
                 if (activityAppliance.getApplianceID().equalsIgnoreCase("")) {
 
                   activityAppliance.setApplianceID(APIUtilities
                           .sendEntity(activityAppliance
                                               .toJSON(APIUtilities.getUserID())
                                               .toString(), "/app"));
 
                   APIUtilities
                           .sendEntity(activityAppliance
                                               .powerConsumptionModelToJSON()
                                               .toString(),
                                       "/consmod");
                 }
                 applianceTemp[i] = activityAppliance.getApplianceID();
               }
               catch (IOException | AuthenticationException
                      | NoSuchAlgorithmException e1) {
                 e1.printStackTrace();
               }
 
             }
 
             try {
 
               String[] appliancesID = applianceTemp;
 
               // Creating the JSON of the activity model
               activity.setActivityModelID(APIUtilities.sendEntity(activity
                       .toJSON(appliancesID, APIUtilities.getUserID())
                       .toString(), "/actmod"));
 
               activityTemp = activity.getActivityModelID();
 
               // Creating the JSON of the distributions
               activity.getDailyTimes()
                       .setDistributionID(APIUtilities
                                                  .sendEntity(activity
                                                          .getDailyTimes()
                                                          .toJSON(activityTemp)
                                                          .toString(), "/distr"));
 
               activity.setDailyID(activity.getDailyTimes().getDistributionID());
               dailyTemp = activity.getDailyID();
 
               activity.getDuration()
                       .setDistributionID(APIUtilities
                                                  .sendEntity(activity.getDuration()
                                                                      .toJSON(activityTemp)
                                                                      .toString(),
                                                              "/distr"));
 
               activity.setDurationID(activity.getDuration().getDistributionID());
               durationTemp = activity.getDurationID();
 
               activity.getStartTime()
                       .setDistributionID(APIUtilities
                                                  .sendEntity(activity
                                                          .getStartTime()
                                                          .toJSON(activityTemp)
                                                          .toString(), "/distr"));
 
               activity.setStartID(activity.getStartTime().getDistributionID());
               startTemp = activity.getStartID();
 
               // Adding the JSON of the distributions to the activity model
               APIUtilities.updateEntity(activity.toJSON(appliancesID,
                                                         APIUtilities
                                                                 .getUserID())
                                                 .toString(), "/actmod",
                                         activityTemp);
 
             }
             catch (AuthenticationException | NoSuchAlgorithmException
                    | IOException e1) {
 
               e1.printStackTrace();
             }
 
             JFrame success = new JFrame();
 
             JOptionPane.showMessageDialog(success,
                                           "The activity model "
                                                   + activity.getName()
                                                   + " was exported successfully",
                                           "Activity Model Exported",
                                           JOptionPane.INFORMATION_MESSAGE);
 
           }
           // If it is response
           else if (response != null) {
             String[] applianceTemp =
               new String[response.getAppliancesOf().length];
 
             String responseTemp = "";
             String durationTemp = "";
             String dailyTemp = "";
             String startTemp = "";
 
             // For each appliance that participates in the activity
             for (int i = 0; i < response.getAppliancesOf().length; i++) {
 
               Appliance responseAppliance =
                 installation.findAppliance(response.getAppliancesOf()[i]);
 
               try {
                 // In case the appliances contained in the Activity model are
                 // not
                 // in the database, we create the object there before sending
                 // the
                 // activity model
                 if (responseAppliance.getApplianceID().equalsIgnoreCase("")) {
 
                   responseAppliance.setApplianceID(APIUtilities
                           .sendEntity(responseAppliance
                                               .toJSON(APIUtilities.getUserID())
                                               .toString(), "/app"));
 
                   APIUtilities
                           .sendEntity(responseAppliance
                                               .powerConsumptionModelToJSON()
                                               .toString(),
                                       "/consmod");
                 }
                 applianceTemp[i] = responseAppliance.getApplianceID();
               }
               catch (IOException | AuthenticationException
                      | NoSuchAlgorithmException e1) {
                 e1.printStackTrace();
               }
             }
 
             try {
 
               String[] appliancesID = applianceTemp;
 
               // Creating the JSON of the response
               response.setActivityModelID(APIUtilities.sendEntity(response
                       .toJSON(appliancesID, APIUtilities.getUserID())
                       .toString(), "/actmod"));
 
               responseTemp = response.getActivityModelID();
 
               // Creating the JSON of the distributions
               response.getDailyTimes()
                       .setDistributionID(APIUtilities
                                                  .sendEntity(response
                                                          .getDailyTimes()
                                                          .toJSON(responseTemp)
                                                          .toString(), "/distr"));
 
               response.setDailyID(response.getDailyTimes().getDistributionID());
               dailyTemp = response.getDailyID();
 
               response.getDuration()
                       .setDistributionID(APIUtilities
                                                  .sendEntity(response.getDuration()
                                                                      .toJSON(responseTemp)
                                                                      .toString(),
                                                              "/distr"));
 
               response.setDurationID(response.getDuration().getDistributionID());
               durationTemp = response.getDurationID();
 
               response.getStartTime()
                       .setDistributionID(APIUtilities
                                                  .sendEntity(response
                                                          .getStartTime()
                                                          .toJSON(responseTemp)
                                                          .toString(), "/distr"));
 
               response.setStartID(response.getStartTime().getDistributionID());
               startTemp = response.getStartID();
 
               // Adding the JSON of the distributions to the activity model
               APIUtilities.updateEntity(response.toJSON(appliancesID,
                                                         APIUtilities
                                                                 .getUserID())
                                                 .toString(), "/actmod",
                                         responseTemp);
 
             }
             catch (AuthenticationException | NoSuchAlgorithmException
                    | IOException e1) {
 
               e1.printStackTrace();
             }
 
             JFrame success = new JFrame();
 
             JOptionPane.showMessageDialog(success,
                                           "The response model "
                                                   + response.getName()
                                                   + " was exported successfully",
                                           "Response Model Exported",
                                           JOptionPane.INFORMATION_MESSAGE);
 
           }
         }
 
         finally {
           root.setCursor(Cursor.getDefaultCursor());
         }
       }
     });
 
     exportAllBaseButton.addActionListener(new ActionListener() {
       /**
        * This function is called when the user presses the Export All Base
        * button on the Connection Properties panel of the Export Models tab. The
        * export procedure above is iterated through all the entities available
        * on the list except for the response models.
        */
       public void actionPerformed (ActionEvent e)
       {
         Component root = SwingUtilities.getRoot((JButton) e.getSource());
 
         try {
 
           root.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 
           for (int i = 0; i < exportModelList.getModel().getSize(); i++) {
             exportModelList.setSelectedIndex(i);
 
             String selection = exportModelList.getSelectedValue();
 
             Appliance appliance = installation.findAppliance(selection);
 
             ActivityModel activity =
               installation.getPerson().findActivity(selection, false);
 
             ResponseModel response =
               installation.getPerson().findResponse(selection);
 
             if (selection.equalsIgnoreCase(installation.getName())) {
 
               String oldName = installation.getName();
 
               try {
 
                 installation.setName(householdNameTextField.getText() + " Base");
 
                 installation.setInstallationID(APIUtilities
                         .sendEntity(installation.toJSON(APIUtilities
                                                                 .getUserID())
                                             .toString(), "/inst"));
 
                 installation.setName(oldName);
               }
               catch (IOException | AuthenticationException
                      | NoSuchAlgorithmException e1) {
                 e1.printStackTrace();
               }
 
             }
             else if (selection.equalsIgnoreCase(installation.getPerson()
                     .getName())) {
 
               try {
                 installation
                         .getPerson()
                         .setPersonID(APIUtilities.sendEntity(installation
                                                                      .getPerson()
                                                                      .toJSON(installation
                                                                                      .getInstallationID())
                                                                      .toString(),
                                                              "/pers"));
               }
               catch (IOException | AuthenticationException
                      | NoSuchAlgorithmException e1) {
                 e1.printStackTrace();
               }
 
             }
             else if (appliance != null) {
 
               try {
                 appliance.setApplianceID(APIUtilities.sendEntity(appliance
                         .toJSON(installation.getInstallationID().toString())
                         .toString(), "/app"));
 
                 APIUtilities.sendEntity(appliance.powerConsumptionModelToJSON()
                         .toString(), "/consmod");
 
               }
               catch (IOException | AuthenticationException
                      | NoSuchAlgorithmException e1) {
                 e1.printStackTrace();
               }
 
             }
             else if (activity != null) {
 
               String[] applianceTemp =
                 new String[activity.getAppliancesOf().length];
 
               String personTemp = "";
               String activityTemp = "";
               String durationTemp = "";
               String dailyTemp = "";
               String startTemp = "";
 
               // For each appliance that participates in the activity
               for (int j = 0; j < activity.getAppliancesOf().length; j++) {
 
                 Appliance activityAppliance =
                   installation.findAppliance(activity.getAppliancesOf()[j]);
                 applianceTemp[j] = activityAppliance.getApplianceID();
               }
 
               personTemp = installation.getPerson().getPersonID();
 
               try {
 
                 activity.setActivityID(APIUtilities.sendEntity(activity
                         .activityToJSON(personTemp).toString(), "/act"));
 
                 String[] appliancesID = applianceTemp;
 
                 activity.setActivityModelID(APIUtilities.sendEntity(activity
                         .toJSON(appliancesID).toString(), "/actmod"));
                 activityTemp = activity.getActivityModelID();
 
                 activity.getDailyTimes()
                         .setDistributionID(APIUtilities
                                                    .sendEntity(activity.getDailyTimes()
                                                                        .toJSON(activityTemp)
                                                                        .toString(),
                                                                "/distr"));
                 activity.setDailyID(activity.getDailyTimes()
                         .getDistributionID());
                 dailyTemp = activity.getDailyID();
 
                 activity.getDuration()
                         .setDistributionID(APIUtilities
                                                    .sendEntity(activity.getDuration()
                                                                        .toJSON(activityTemp)
                                                                        .toString(),
                                                                "/distr"));
 
                 activity.setDurationID(activity.getDuration()
                         .getDistributionID());
                 durationTemp = activity.getDurationID();
 
                 activity.getStartTime()
                         .setDistributionID(APIUtilities
                                                    .sendEntity(activity.getStartTime()
                                                                        .toJSON(activityTemp)
                                                                        .toString(),
                                                                "/distr"));
 
                 activity.setStartID(activity.getStartTime().getDistributionID());
                 startTemp = activity.getStartID();
 
                 APIUtilities.updateEntity(activity.toJSON(appliancesID)
                         .toString(), "/actmod", activityTemp);
 
               }
               catch (IOException | AuthenticationException
                      | NoSuchAlgorithmException e1) {
                 e1.printStackTrace();
               }
 
             }
             else if (response != null) {
 
             }
           }
 
         }
 
         finally {
           root.setCursor(Cursor.getDefaultCursor());
         }
 
         JFrame success = new JFrame();
 
         JOptionPane.showMessageDialog(success,
                                       "The installation model "
                                               + installation.getName()
                                               + " for the base pricing scheme and all the entities contained within were exported successfully",
                                       "Installation Model Exported",
                                       JOptionPane.INFORMATION_MESSAGE);
 
       }
     });
 
     exportAllResponseButton.addActionListener(new ActionListener() {
       /**
        * This function is called when the user presses the Export All Base
        * button on the Connection Properties panel of the Export Models tab. The
        * export procedure above is iterated through all the entities available
        * on the list except for the activity models.
        */
       public void actionPerformed (ActionEvent e)
       {
         Component root = SwingUtilities.getRoot((JButton) e.getSource());
 
         try {
 
           root.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 
           for (int i = 0; i < exportModelList.getModel().getSize(); i++) {
             exportModelList.setSelectedIndex(i);
 
             String selection = exportModelList.getSelectedValue();
 
             Appliance appliance = installation.findAppliance(selection);
 
             ActivityModel activity =
               installation.getPerson().findActivity(selection, false);
 
             ResponseModel response =
               installation.getPerson().findResponse(selection);
 
             if (selection.equalsIgnoreCase(installation.getName())) {
 
               String oldName = installation.getName();
 
               try {
 
                installation.setName(householdNameTextField.getText()
                                     + " Response");
 
                 installation.setInstallationID(APIUtilities
                         .sendEntity(installation.toJSON(APIUtilities
                                                                 .getUserID())
                                             .toString(), "/inst"));
 
                 installation.setName(oldName);
 
               }
               catch (IOException | AuthenticationException
                      | NoSuchAlgorithmException e1) {
                 e1.printStackTrace();
               }
 
             }
             else if (selection.equalsIgnoreCase(installation.getPerson()
                     .getName())) {
 
               try {
                 installation
                         .getPerson()
                         .setPersonID(APIUtilities.sendEntity(installation
                                                                      .getPerson()
                                                                      .toJSON(installation
                                                                                      .getInstallationID())
                                                                      .toString(),
                                                              "/pers"));
               }
               catch (IOException | AuthenticationException
                      | NoSuchAlgorithmException e1) {
                 e1.printStackTrace();
               }
 
             }
             else if (appliance != null) {
 
               try {
                 appliance.setApplianceID(APIUtilities.sendEntity(appliance
                         .toJSON(installation.getInstallationID().toString())
                         .toString(), "/app"));
 
                 APIUtilities.sendEntity(appliance.powerConsumptionModelToJSON()
                         .toString(), "/consmod");
 
               }
               catch (IOException | AuthenticationException
                      | NoSuchAlgorithmException e1) {
                 e1.printStackTrace();
               }
 
             }
             else if (activity != null) {
 
             }
             else if (response != null) {
               String[] applianceTemp =
                 new String[response.getAppliancesOf().length];
 
               String personTemp = "";
               String responseTemp = "";
               String durationTemp = "";
               String dailyTemp = "";
               String startTemp = "";
 
               // For each appliance that participates in the activity
               for (int j = 0; j < response.getAppliancesOf().length; j++) {
 
                 Appliance responseAppliance =
                   installation.findAppliance(response.getAppliancesOf()[j]);
 
                 applianceTemp[j] = responseAppliance.getApplianceID();
               }
               personTemp = installation.getPerson().getPersonID();
 
               try {
 
                 response.setActivityID(APIUtilities.sendEntity(response
                         .activityToJSON(personTemp).toString(), "/act"));
 
                 String[] appliancesID = applianceTemp;
 
                 response.setActivityModelID(APIUtilities.sendEntity(response
                         .toJSON(appliancesID).toString(), "/actmod"));
                 responseTemp = response.getActivityModelID();
 
                 response.getDailyTimes()
                         .setDistributionID(APIUtilities
                                                    .sendEntity(response.getDailyTimes()
                                                                        .toJSON(responseTemp)
                                                                        .toString(),
                                                                "/distr"));
                 response.setDailyID(response.getDailyTimes()
                         .getDistributionID());
                 dailyTemp = response.getDailyID();
 
                 response.getDuration()
                         .setDistributionID(APIUtilities
                                                    .sendEntity(response.getDuration()
                                                                        .toJSON(responseTemp)
                                                                        .toString(),
                                                                "/distr"));
 
                 response.setDurationID(response.getDuration()
                         .getDistributionID());
                 durationTemp = response.getDurationID();
 
                 response.getStartTime()
                         .setDistributionID(APIUtilities
                                                    .sendEntity(response.getStartTime()
                                                                        .toJSON(responseTemp)
                                                                        .toString(),
                                                                "/distr"));
 
                 response.setStartID(response.getStartTime().getDistributionID());
                 startTemp = response.getStartID();
 
                 APIUtilities.updateEntity(response.toJSON(appliancesID)
                         .toString(), "/actmod", responseTemp);
 
               }
               catch (IOException | AuthenticationException
                      | NoSuchAlgorithmException e1) {
                 e1.printStackTrace();
               }
             }
           }
         }
 
         finally {
           root.setCursor(Cursor.getDefaultCursor());
         }
 
         JFrame success = new JFrame();
 
         JOptionPane.showMessageDialog(success,
                                       "The installation model "
                                               + installation.getName()
                                               + " for the new pricing scheme and all the entities contained within were exported successfully",
                                       "Installation Model Exported",
                                       JOptionPane.INFORMATION_MESSAGE);
       }
     });
   }
 
   /**
    * This function is used when the program needs to search through the list of
    * available activities to find the selected one.
    * 
    * @param name
    *          name of the activity as it can be found on the list of detected /
    *          selected activities
    * @return the index of the activity in the list of the tempActivities.
    */
   private static int findActivity (String name)
   {
 
     int result = -1;
 
     for (int i = 0; i < tempActivities.size(); i++) {
       if (tempActivities.get(i).getName().equals(name)) {
 
         result = i;
         break;
       }
 
     }
 
     return result;
   }
 
   /**
    * This function is used when the program needs to find the list of appliances
    * that participate in a certain activity.
    * 
    * @param activity
    *          the activity model for which we search the appliance that are
    *          participating.
    * @return list of the appliances.
    */
   private ArrayList<Appliance> findAppliances (ActivityTemp activity)
   {
 
     ArrayList<Appliance> appliances = new ArrayList<Appliance>();
     // System.out.println("Activity:" + activity.getName());
     for (Appliance appliance: installation.getAppliances()) {
 
       if (activity.getName().equalsIgnoreCase(appliance.getActivity())) {
         // System.out.println(appliance.getName());
         appliances.add(appliance);
       }
     }
 
     // System.out.println(appliances.size());
 
     return appliances;
   }
 }
