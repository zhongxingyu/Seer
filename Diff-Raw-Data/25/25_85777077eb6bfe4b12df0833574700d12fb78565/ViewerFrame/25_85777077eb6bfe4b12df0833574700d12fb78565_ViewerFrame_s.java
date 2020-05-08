 package nl.ctmm.trait.proteomics.qcviewer.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.StringTokenizer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.imageio.ImageIO;
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JDesktopPane;
 import javax.swing.JFormattedTextField;
 import javax.swing.JFrame;
 import javax.swing.JInternalFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTextField;
 import javax.swing.WindowConstants;
 
 import nl.ctmm.trait.proteomics.qcviewer.input.MetricsParser;
 import nl.ctmm.trait.proteomics.qcviewer.input.ReportUnit;
 import nl.ctmm.trait.proteomics.qcviewer.utils.Constants;
 import nl.ctmm.trait.proteomics.qcviewer.utils.Utilities;
 
 import org.apache.commons.io.FilenameUtils;
 import org.jfree.chart.ChartMouseEvent;
 import org.jfree.chart.ChartMouseListener;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.ui.RefineryUtilities;
 
 /**
  * ViewerFrame with the GUI for the QC Report Viewer.
  *
  * Some useful links on Swing:
  * - InternalFrames: http://docs.oracle.com/javase/tutorial/uiswing/components/internalframe.html
  * - Radio buttons: http://www.leepoint.net/notes-java/GUI/components/50radio_buttons/25radiobuttons.html
  * - Swing layout: http://www.cs101.org/courses/fall05/resources/swinglayout/
  *
  * TODO: Clicking on right part of the main table (on a graph) does not select that row.
  *       (And therefore does not show that graph large at the bottom).
  *
  * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
  * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
  */
 public class ViewerFrame extends JFrame implements ActionListener, ItemListener, MouseListener, ChartMouseListener {
     /**
      * The logger for this class.
      */
     private static final Logger logger = Logger.getLogger(ViewerFrame.class.getName());
 
     /**
      * The version number for (de)serialization of this class (UID: universal identifier).
      */
     private static final long serialVersionUID = 1;
 
     /*private static final List<Color> LABEL_COLORS = Arrays.asList(
             Color.BLUE, Color.DARK_GRAY, Color.GRAY, Color.MAGENTA, Color.ORANGE, Color.RED, Color.BLACK);*/
 
     /**
      * Alternating colors used for the metrics.
      */
     private static final List<Color> LABEL_COLORS = Arrays.asList(
             Color.BLUE, Color.DARK_GRAY, Color.GREEN, Color.BLACK);
 
     /**
      * Height of a row with information from one report in the list.
      */
     private static final int CHART_HEIGHT = 150;
 
     /**
      * Width of the left panel for each report in the list, with the report number, the metrics details button and the
      * selection check box.
      */
     private static final int CHECK_PANEL_WIDTH = 90;
 
     /**
      * Width of the panel showing the selected metrics for each report in the list.
      */
     private static final int METRICS_PANEL_WIDTH = 350;
 
     /**
      * Width of the panel with a TIC chart for each report in the list.
      */
     private static final int CHART_PANEL_WIDTH = 800;
 
     /**
      * Initial width of the viewer frame.
      */
     private static final int DESKTOP_PANE_WIDTH = 1270;
 
     /**
      * Default divider location of the top split pane, which separates the top control panel from the rest of the GUI.
      */
     private static final int SPLIT_PANE_1_DIVIDER_LOCATION = 185;
 
     /**
      * Default divider location of the bottom split pane, which separates the central list with the QC results from the
      * bottom panel with the larger version of the TIC graph that is selected in the central list.
      */
     private static final int SPLIT_PANE_2_DIVIDER_LOCATION = 500;
 
     /**
      * Width of the panel with sorting controls.
      */
     private static final int SORT_PANEL_WIDTH = 700;
 
     /**
      * Height of the panel with sorting controls.
      */
     private static final int SORT_PANEL_HEIGHT = 140;
     
     /**
      * Width of the zoom panel controls
      */
     private static final int ZOOM_PANEL_WIDTH = 280;
     /**
      * Height of the zoom panel controls
      */
     private static final int ZOOM_PANEL_HEIGHT = 140;
     /**
      * Height of individual zoom panel components - Zoom All and Zoom Along X Axis
      */
     private static final int ZOOM_PANEL_FORM_HEIGHT = 60;
     /**
      * Width of an individual zoom button
      */
     private static final int ZOOM_BUTTON_WIDTH = 85;
     /**
      * Height of an individual zoom button
      */
     private static final int ZOOM_BUTTON_HEIGHT = 25;
     /**
      * Width of min - max text box
      */
     private static final int TEXT_BOX_WIDTH = 30;
     /**
      * Height of min - max text box
      */
     private static final int TEXT_BOX_HEIGHT = 20;
     /**
      * Text of the Zoom In button
      */
     private static final String ZOOM_IN_BUTTON_TEXT = "Zoom In";
     /**
      * Text of the Zoom Original button
      */
     private static final String ZOOM_ORIGINAL_BUTTON_TEXT = "Original";
     /**
      * Text of the Zoom Out button
      */
     private static final String ZOOM_OUT_BUTTON_TEXT = "Zoom Out";
     /**
      * Text of the Zoom Along X axis button
      */
     private static final String ZOOM_X_AXIS_BUTTON_TEXT = "Zoom X Axis";
     /**
      * Width of the OPL and CTMM TraIT logos on the top left and top right of the application.
      */
     private static final int LOGO_WIDTH = 179;
 
     /**
      * Height of the OPL and CTMM TraIT logos on the top left and top right of the application.
      */
     private static final int LOGO_HEIGHT = 100;
 
     private static final int STATUS_PANEL_WIDTH = 1333;
     private static final int STATUS_PANEL_HEIGHT = 20;
     private static final int CONTROL_PANEL_WIDTH = 1333;
     private static final int CONTROL_PANEL_HEIGHT = 140;
     private static final int CONTROL_FRAME_WIDTH = 1333;
     private static final int CONTROL_FRAME_HEIGHT = 165;
     private static final int METRIC_LABEL_WIDTH = 200;
     private static final int METRIC_LABEL_HEIGHT = 30;
 
     /**
      * Separator used in the metrics definition file.
      */
     private static final String METRICS_SEPARATOR = ":";
 
     /**
      * Prefix used for the action command of the metrics details buttons.
      */
     private static final String DETAILS_ACTION_PREFIX = "Details-";
 
     private JDesktopPane desktopPane = new ScrollDesktop();
     private JDesktopPane ticGraphPane = new ScrollDesktop();
     private List<ChartPanel> chartPanelList = new ArrayList<>(); //necessary for zooming
     private List<Boolean> reportIsSelected = new ArrayList<>();
     private JFormattedTextField minText, maxText;
     private List<ReportUnit> reportUnits = new ArrayList<>(); //preserve original report units
     private List<ReportUnit> orderedReportUnits = new ArrayList<>(); //use this list for display and other operations
     private final Map<ReportUnit, JPanel> reportUnitToMetricsPanel = new HashMap<>();
     private List<String> selectedMetricsKeys;
     private List<String> selectedMetricsNames;
     private List<JRadioButton> sortButtons;
     private String currentSortCriteria = "";
     private String newSortCriteria = "";
     private Properties appProperties;
     private MetricsParser metricsParser;
     private String pipelineStatus;
     private JSplitPane splitPane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
     private JSplitPane splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
     private JTextField statusField;
     private JPanel statusPanel;
     private JPanel sortPanel = new JPanel();
     private int yCoordinate;
 
     /**
      * Creates a new instance of the main frame of the proteomics QC viewer.
      *
      * @param metricsParser the metrics parser to use.
      * @param appProperties the application properties to use.
      * @param title the frame title.
      * @param reportUnits the initial report units to show.
      * @param selectedMetricsData the data of the selected metrics (keys and names).
      * @param pipelineStatus the initial pipeline status to show.
      */
     public ViewerFrame(final MetricsParser metricsParser, final Properties appProperties, final String title,
                        final List<ReportUnit> reportUnits, final List<String> selectedMetricsData,
                        final String pipelineStatus) {
         super(title);
         logger.fine("ViewerFrame constructor");
         setPreferredSize(new Dimension(DESKTOP_PANE_WIDTH + 25, CHART_HEIGHT * 10));
         setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
         this.metricsParser = metricsParser;
         this.appProperties = appProperties;
         this.pipelineStatus = pipelineStatus;
         parseSelectedMetricsData(selectedMetricsData);
         setReportUnits(reportUnits);
         setOrderedReportUnits(reportUnits);
         assembleComponents();
         setVisible(true);
         zoomMinMax();
         // Finally refresh the frame.
         revalidate();
     }
 
     /**
      * Parse the data of the selected metrics: create lists of metrics keys and metrics names.
      *
      * @param selectedMetricsData the data of the selected metrics (keys and names).
      */
     private void parseSelectedMetricsData(final List<String> selectedMetricsData) {
         this.selectedMetricsKeys = new ArrayList<>();
         this.selectedMetricsNames = new ArrayList<>();
         // Extract keys and names of the selected metrics.
         for (final String selectedMetricData : selectedMetricsData) {
             final StringTokenizer dataTokenizer = new StringTokenizer(selectedMetricData, METRICS_SEPARATOR);
             final String key = dataTokenizer.nextToken() + METRICS_SEPARATOR + dataTokenizer.nextToken();
             this.selectedMetricsKeys.add(key);
             final String value = dataTokenizer.nextToken();
             this.selectedMetricsNames.add(value);
         }
     }
 
  /**
  * New report units are available. Update report units in the viewer frame.
  *
  * @param newReportUnits New QC reports
  * @param newPipelineStatus Updated pipeline status
  * @param replaceFlag If true, existing report units will be replaced by new report units.
  *             Else if false, new reports are added to existing reports. GUI will be updated accordingly.
  */
     public void updateReportUnits(final List<ReportUnit> newReportUnits, final String newPipelineStatus,
                                   final Boolean replaceFlag) {
         logger.fine("In updateReportUnits yCoordinate = " + yCoordinate);
         if (replaceFlag) {
             //Replace all existing reports by newReportUnits.
             reportUnits.clear();
             orderedReportUnits.clear();
             reportIsSelected.clear();
             desktopPane.removeAll();
             ticGraphPane.removeAll();
             pack();
             revalidate();
             yCoordinate = 0;
         }
         final int numReportUnits = reportUnits.size();
         if (newReportUnits.size() > 0) {
             for (int reportIndex = 0; reportIndex < newReportUnits.size(); reportIndex++) {
                 final ReportUnit thisUnit = newReportUnits.get(reportIndex);
                 reportUnits.add(thisUnit);
                 orderedReportUnits.add(thisUnit);
                 reportIsSelected.add(false);
                 //update desktopFrame
                 final JInternalFrame chartFrame = createChartFrame(thisUnit, reportIndex + numReportUnits);
                 chartFrame.setBorder(BorderFactory.createRaisedBevelBorder());
                 chartFrame.pack();
                 chartFrame.setLocation(0, yCoordinate);
                 desktopPane.add(chartFrame);
                 chartFrame.setVisible(true);
                 logger.fine("yCoordinate = " + yCoordinate);
                 yCoordinate += CHART_HEIGHT + 15;
             }
             desktopPane.setPreferredSize(new Dimension(DESKTOP_PANE_WIDTH, reportUnits.size() * (CHART_HEIGHT + 15)));
             //Set first report graph in the Tic Pane. -1 adjusts to the index.
             setTicGraphPaneChart(orderedReportUnits.get(0).getReportNum() - 1);
         }
         updatePipelineStatus(newPipelineStatus);
         revalidate();
     }
 
     /**
      * Update pipelineStatus in the report viewer.
      *
      * @param newPipelineStatus as inferred from qc_status.log file
      */
     public void updatePipelineStatus(final String newPipelineStatus) {
         //logger.fine("ViewerFrame updatePipelineStatus");
         pipelineStatus = newPipelineStatus;
         statusPanel.removeAll();
         final String status = pipelineStatus + " | | | | | Number of report units = " + orderedReportUnits.size();
         statusField = new JTextField(status);
         statusField.setFont(Constants.DEFAULT_FONT);
 //        statusField.setBackground(Color.CYAN);
         statusField.setHorizontalAlignment(JTextField.CENTER);
         statusField.setEditable(false);
 //        statusPanel.setBackground(Color.CYAN);
         statusPanel.add(statusField);
         revalidate();
     }
 
     /**
      * Assemble following components of the ViewerFrame.
      * 1) ControlFrame 2) desktopPane 3) ticGraphPane 4) MenuBar
      */
     private void assembleComponents() {
         logger.fine("ViewerFrame assembleComponents");
         //We need two split panes to create 3 regions in the main frame
         
         //Add desktopPane for displaying graphs and other QC Control
         final int totalReports = orderedReportUnits.size();
         if (totalReports != 0) {
             desktopPane.setPreferredSize(new Dimension(DESKTOP_PANE_WIDTH, totalReports * (CHART_HEIGHT + 15)));
             prepareChartsInOrder(true);
           //Set initial tic Graph - specify complete chart in terms of orderedReportUnits
             setTicGraphPaneChart(orderedReportUnits.get(0).getReportNum() - 1);
         }
         //Display empty desktopPane and ticGraphPane
         splitPane2.add(new JScrollPane(desktopPane), 0);
         ticGraphPane.setPreferredSize(new Dimension(DESKTOP_PANE_WIDTH, 2 * CHART_HEIGHT));
         splitPane2.add(new JScrollPane(ticGraphPane), 1);
         //hide-show feature
         splitPane2.setOneTouchExpandable(true);
         splitPane2.setDividerLocation(SPLIT_PANE_2_DIVIDER_LOCATION);
         //Add static (immovable) Control frame
         final JInternalFrame controlFrame = getControlFrame();
         final JScrollPane controlFrameScrollPane = new JScrollPane(controlFrame);
         controlFrameScrollPane.setPreferredSize(new Dimension(CONTROL_FRAME_WIDTH, CONTROL_FRAME_HEIGHT));
         splitPane1.setLeftComponent(controlFrameScrollPane);
         splitPane1.setRightComponent(splitPane2);
         //hide-show feature
         splitPane1.setOneTouchExpandable(true);
         splitPane1.setDividerLocation(SPLIT_PANE_1_DIVIDER_LOCATION);
         splitPane1.setPreferredSize(new Dimension(DESKTOP_PANE_WIDTH + 15, (int)(6.5 * CHART_HEIGHT)));
         getContentPane().add(splitPane1, "Center");
         setJMenuBar(createMenuBar());
     }
 
     /**
      * Create Menu Bar for settings and about tab.
      *
      * @return the menu bar.
      */
     private JMenuBar createMenuBar() {
         final JMenuBar menuBar = new JMenuBar();
         final JMenu settingsMenu = new JMenu("Settings");
         menuBar.add(settingsMenu);
         final JMenuItem newDirAction = new JMenuItem("Set Root Directory...");
         settingsMenu.add(newDirAction);
         newDirAction.setActionCommand("ChangeRootDirectory");
         newDirAction.addActionListener(this);
         final JMenuItem filterAction = new JMenuItem("Set Filter...");
         settingsMenu.add(filterAction);
         filterAction.setActionCommand("SetFilter");
         filterAction.addActionListener(this);
         final JMenuItem metricsAction = new JMenuItem("Select Metrics...");
         settingsMenu.add(metricsAction);
         metricsAction.setActionCommand("SelectMetrics");
         metricsAction.addActionListener(this);
         final JMenuItem aboutAction = new JMenuItem("About...");
         settingsMenu.add(aboutAction);
         aboutAction.setActionCommand("About");
         aboutAction.addActionListener(this);
         return menuBar;
     }
 
     /**
      * Sets the report units to be displayed.
      * @param reportUnits the report units to be displayed.
      */
     private void setReportUnits(final List<ReportUnit> reportUnits) {
         logger.fine("ViewerFrame setReportUnits No. of reportUnits = " + reportUnits.size());
         if (this.reportUnits != null) {
             this.reportUnits.clear();
         }
         this.reportUnits = reportUnits;
         //Initialize reportIsSelected to false
         for (final ReportUnit ignored : reportUnits) {
             reportIsSelected.add(false);
         }
     }
 
     /**
      * Sets the report units to be displayed.
      * @param reportUnits the report units to be displayed.
      */
     private void setOrderedReportUnits(final List<ReportUnit> reportUnits) {
         logger.fine("ViewerFrame setOrderedReportUnits No. of reportUnits = " + reportUnits.size());
         if (orderedReportUnits != null) {
             orderedReportUnits.clear();
             orderedReportUnits.addAll(reportUnits);
             logger.fine("ViewerFrame setOrderedReportUnits No. of ordered reportUnits = " + orderedReportUnits.size());
         }
     }
 
     /**
      * Prepare and return controlFrame.
      *
      * @return JInternalFrame controlFrame.
      */
     private JInternalFrame getControlFrame() {
         logger.fine("ViewerFrame getControlFrame");
         logger.fine("CTMM TraIT logo file is " + Constants.CTMM_TRAIT_LOGO_FILE_NAME);
         final JInternalFrame controlFrame = new JInternalFrame("Control Panel", true);
         final javax.swing.plaf.InternalFrameUI ifu = controlFrame.getUI();
         ((javax.swing.plaf.basic.BasicInternalFrameUI)ifu).setNorthPane(null);
 
         //controlFrame now uses Box layout 
         controlFrame.setBorder(null);
         controlFrame.setBackground(Color.WHITE);
 
         // Zoom all - in, original, out
         final JButton inButton = new JButton(ZOOM_IN_BUTTON_TEXT);
         inButton.setActionCommand("Zoom In");
         inButton.addActionListener(this);
         final JButton originalButton = new JButton(ZOOM_ORIGINAL_BUTTON_TEXT);
         originalButton.setActionCommand("Zoom Original");
         originalButton.addActionListener(this);
         final JButton outButton = new JButton(ZOOM_OUT_BUTTON_TEXT);
         outButton.setActionCommand("Zoom Out");
         outButton.addActionListener(this);
         final ButtonGroup zoomGroup = new ButtonGroup();
         zoomGroup.add(inButton);
         zoomGroup.add(originalButton);
         zoomGroup.add(outButton);
 
         final JPanel zoomPanelButtons = new JPanel();
         zoomPanelButtons.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                                                                     "Zoom All TIC Charts"));
         zoomPanelButtons.setLayout(new BoxLayout(zoomPanelButtons, BoxLayout.X_AXIS));
         zoomPanelButtons.add(Box.createRigidArea(new Dimension(5, 0)));
         inButton.setMinimumSize(new Dimension(ZOOM_BUTTON_WIDTH, ZOOM_BUTTON_HEIGHT));
         zoomPanelButtons.add(inButton, Box.CENTER_ALIGNMENT);
         zoomPanelButtons.add(Box.createRigidArea(new Dimension(5, 0)));
         originalButton.setMinimumSize(new Dimension(ZOOM_BUTTON_WIDTH, ZOOM_BUTTON_HEIGHT));
         zoomPanelButtons.add(originalButton, Box.CENTER_ALIGNMENT);
         zoomPanelButtons.add(Box.createRigidArea(new Dimension(5, 0)));
         outButton.setMinimumSize(new Dimension(ZOOM_BUTTON_WIDTH, ZOOM_BUTTON_HEIGHT));
         zoomPanelButtons.add(outButton, Box.CENTER_ALIGNMENT);
         zoomPanelButtons.add(Box.createRigidArea(new Dimension(5, 0)));
         zoomPanelButtons.setBackground(Color.WHITE);
         
         // Zoom all - Min, Max and Submit
         final JLabel minLabel = new JLabel("Min: ");
         minText = new JFormattedTextField(NumberFormat.getInstance());
         minText.setValue(10);
         minText.addActionListener(new ActionListener() {
             public void actionPerformed(final ActionEvent e) {
                 zoomMinMax();
             }
         });
         final JLabel maxLabel = new JLabel("Max: ");
         maxText = new JFormattedTextField(NumberFormat.getInstance());
         maxText.setValue(80);
         maxText.addActionListener(new ActionListener() {
             public void actionPerformed(final ActionEvent e) {
                 zoomMinMax();
             }
         });
         final JButton zoomButton = new JButton(ZOOM_X_AXIS_BUTTON_TEXT);
         zoomButton.setActionCommand("zoomMinMax");
         zoomButton.addActionListener(this);
         final JPanel zoomPanelForm = new JPanel();
         zoomPanelForm.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Zoom Along X Axis"));
         zoomPanelForm.setLayout(new BoxLayout(zoomPanelForm, BoxLayout.X_AXIS));
         zoomPanelForm.add(Box.createRigidArea(new Dimension(10, 0)));
         zoomPanelForm.add(minLabel, Box.CENTER_ALIGNMENT);
         zoomPanelForm.add(Box.createRigidArea(new Dimension(5, 0)));
         minText.setMaximumSize(new Dimension(TEXT_BOX_WIDTH, TEXT_BOX_HEIGHT));
         zoomPanelForm.add(minText, Box.CENTER_ALIGNMENT);
         zoomPanelForm.add(Box.createRigidArea(new Dimension(5, 0)));
         zoomPanelForm.add(maxLabel, Box.CENTER_ALIGNMENT);
         zoomPanelForm.add(Box.createRigidArea(new Dimension(5, 0)));
         maxText.setMaximumSize(new Dimension(TEXT_BOX_WIDTH, TEXT_BOX_HEIGHT));
         zoomPanelForm.add(maxText, Box.CENTER_ALIGNMENT);
         zoomPanelForm.add(Box.createRigidArea(new Dimension(10, 0)));
         zoomButton.setMinimumSize(new Dimension(ZOOM_BUTTON_WIDTH, ZOOM_BUTTON_HEIGHT));
         zoomPanelForm.add(zoomButton, Box.CENTER_ALIGNMENT);
         zoomPanelForm.add(Box.createRigidArea(new Dimension(5, 0)));
         zoomPanelForm.setBackground(Color.WHITE);
 
         final JPanel zoomPanel = new JPanel();
         zoomPanel.setLayout(new GridLayout(2, 1));
         zoomPanel.setBackground(Color.WHITE);
         zoomPanelButtons.setMinimumSize(new Dimension(ZOOM_PANEL_WIDTH, ZOOM_PANEL_FORM_HEIGHT));
         zoomPanel.add(zoomPanelButtons, 0);
         zoomPanelForm.setMinimumSize(new Dimension(ZOOM_PANEL_WIDTH, ZOOM_PANEL_FORM_HEIGHT));
         zoomPanel.add(zoomPanelForm, 1);
 
         final JLabel oplLogoLabel = createLogoLabel(Constants.OPL_LOGO_FILE_NAME);
         final JLabel ctmmTraitLogoLabel = createLogoLabel(Constants.CTMM_TRAIT_LOGO_FILE_NAME);
 
         final JPanel controlPanel = new JPanel();
         controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
         controlPanel.setBackground(Color.WHITE);
         controlPanel.add(Box.createRigidArea(new Dimension(10, 0)));
         controlPanel.add(oplLogoLabel, Box.CENTER_ALIGNMENT);
         controlPanel.add(Box.createRigidArea(new Dimension(10, 0)));
         zoomPanel.setMinimumSize(new Dimension(ZOOM_PANEL_WIDTH, ZOOM_PANEL_HEIGHT));
         controlPanel.add(zoomPanel, Box.CENTER_ALIGNMENT);
         controlPanel.add(Box.createRigidArea(new Dimension(10, 0)));
         createOrUpdateSortPanel();
         controlPanel.add(sortPanel, Box.CENTER_ALIGNMENT);
         controlPanel.add(Box.createRigidArea(new Dimension(10, 0)));
         controlPanel.add(ctmmTraitLogoLabel, Box.CENTER_ALIGNMENT);
         controlPanel.add(Box.createRigidArea(new Dimension(10, 0)));
 
         final String status = pipelineStatus + " | | | | | Number of report units = " + orderedReportUnits.size();
         statusPanel = new JPanel(new GridLayout(1, 1));
         statusField = new JTextField(status);
         statusField.setFont(Constants.DEFAULT_FONT);
 //        statusField.setBackground(Color.CYAN);
         statusField.setHorizontalAlignment(JTextField.CENTER);
         statusField.setEditable(false);
         statusPanel.add(statusField);
 
         controlFrame.getContentPane().setLayout(new BoxLayout(controlFrame.getContentPane(), BoxLayout.Y_AXIS));
         controlFrame.getContentPane().add(Box.createRigidArea(new Dimension(5, 0)));
         controlPanel.setPreferredSize(new Dimension(CONTROL_PANEL_WIDTH, CONTROL_PANEL_HEIGHT));
         controlFrame.getContentPane().add(controlPanel, BorderLayout.CENTER);
         controlFrame.getContentPane().add(Box.createRigidArea(new Dimension(5, 0)));
         statusPanel.setPreferredSize(new Dimension(STATUS_PANEL_WIDTH, STATUS_PANEL_HEIGHT));
         controlFrame.getContentPane().add(statusPanel, BorderLayout.CENTER);
         controlFrame.getContentPane().add(Box.createRigidArea(new Dimension(5, 0)));
         controlFrame.pack();
         controlFrame.setLocation(0, 0);
         controlFrame.setResizable(false);
         controlFrame.setVisible(true);
         return controlFrame;
     }
     
     /**
     * Create sort panel displaying sort metrics and sort order
     * @return sortPanel 
      */
     private void createOrUpdateSortPanel() {
         if (sortPanel != null) {
             sortPanel.removeAll();
         }
         final ButtonGroup sortGroup = new ButtonGroup();
         final GridLayout layout = new GridLayout(selectedMetricsNames.size() / 2 + 1, 2);
         sortButtons = new ArrayList<>();
         sortPanel.setLayout(layout);
         sortPanel.setBackground(Color.WHITE);
         sortPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Sort Options"));
         for (int i = 0; i < selectedMetricsNames.size(); ++i) {
             final JLabel metricLabel = new JLabel(selectedMetricsNames.get(i) + ": ");
             metricLabel.setFont(Constants.DEFAULT_FONT);
             metricLabel.setBackground(Color.WHITE);
             //Sort ascending button
             final JRadioButton ascButton = new JRadioButton("Asc", false);
             ascButton.setBackground(Color.WHITE);
             ascButton.setActionCommand("Sort@" + selectedMetricsKeys.get(i) + "@Asc");
             ascButton.addActionListener(this);
             sortGroup.add(ascButton);
             sortButtons.add(ascButton);
             //Sort descending button
             final JRadioButton desButton = new JRadioButton("Des", false);
             desButton.setBackground(Color.WHITE);
             desButton.setActionCommand("Sort@" + selectedMetricsKeys.get(i) + "@Des");
             desButton.addActionListener(this);
             sortGroup.add(desButton);
             sortButtons.add(desButton);
             final JPanel sortItemPanel = new JPanel();
             sortItemPanel.setLayout(new BoxLayout(sortItemPanel, BoxLayout.X_AXIS));
             sortItemPanel.setBackground(Color.WHITE);
             sortItemPanel.add(Box.createRigidArea(new Dimension(5, 0)));
             metricLabel.setMinimumSize(new Dimension(METRIC_LABEL_WIDTH, METRIC_LABEL_HEIGHT));
             metricLabel.setMaximumSize(new Dimension(METRIC_LABEL_WIDTH, METRIC_LABEL_HEIGHT));
             sortItemPanel.add(metricLabel, Box.CENTER_ALIGNMENT);
             sortItemPanel.add(Box.createRigidArea(new Dimension(5, 0)));
             sortItemPanel.add(ascButton, Box.CENTER_ALIGNMENT);
             sortItemPanel.add(Box.createRigidArea(new Dimension(5, 0)));
             sortItemPanel.add(desButton, Box.CENTER_ALIGNMENT);
             sortItemPanel.add(Box.createRigidArea(new Dimension(5, 0)));
             sortPanel.add(sortItemPanel);
         }
         //Add sorting according to Compare 
         final JLabel compareLabel = new JLabel("Compare: ");
         compareLabel.setFont(Constants.DEFAULT_FONT);
         compareLabel.setBackground(Color.WHITE);
         //Sort ascending button
         final JRadioButton ascButton = new JRadioButton("Asc", false);
         ascButton.setBackground(Color.WHITE);
         ascButton.setActionCommand("Sort@" + "Compare" + "@Asc");
         ascButton.addActionListener(this);
         sortGroup.add(ascButton);
         sortButtons.add(ascButton);
         //Sort descending button
         final JRadioButton desButton = new JRadioButton("Des", false);
         desButton.setBackground(Color.WHITE);
         desButton.setActionCommand("Sort@" + "Compare" + "@Des");
         desButton.addActionListener(this);
         sortGroup.add(desButton);
         sortButtons.add(desButton);
         final JPanel sortItemPanel = new JPanel();
         sortItemPanel.setLayout(new BoxLayout(sortItemPanel, BoxLayout.X_AXIS));
         sortItemPanel.setBackground(Color.WHITE);
         sortItemPanel.add(Box.createRigidArea(new Dimension(5, 0)));
         compareLabel.setMinimumSize(new Dimension(METRIC_LABEL_WIDTH, METRIC_LABEL_HEIGHT));
         compareLabel.setMaximumSize(new Dimension(METRIC_LABEL_WIDTH, METRIC_LABEL_HEIGHT));
         sortItemPanel.add(compareLabel, Box.CENTER_ALIGNMENT);
         sortItemPanel.add(Box.createRigidArea(new Dimension(5, 0)));
         sortItemPanel.add(ascButton, Box.CENTER_ALIGNMENT);
         sortItemPanel.add(Box.createRigidArea(new Dimension(5, 0)));
         sortItemPanel.add(desButton, Box.CENTER_ALIGNMENT);
         sortItemPanel.add(Box.createRigidArea(new Dimension(5, 0)));
         sortPanel.add(sortItemPanel);
         //Set first button selected
         sortButtons.get(0).setSelected(true);
         this.currentSortCriteria = sortButtons.get(0).getActionCommand();
         sortPanel.setPreferredSize(new Dimension(SORT_PANEL_WIDTH, SORT_PANEL_HEIGHT));
     }
 
     /**
      * Create a label with the specified logo image.
      *
      * @param logoFilePath the file path of the logo image.
      * @return the label with the logo.
      */
     private JLabel createLogoLabel(final String logoFilePath) {
         BufferedImage logoImage = null;
         try {
             logoImage = ImageIO.read(new File(FilenameUtils.normalize(logoFilePath)));
         } catch (final IOException e) {
             logger.log(Level.SEVERE, "Something went wrong while reading logo file " + logoFilePath, e);
         }
         final Image scaledLogoImage = Utilities.scaleImage(logoImage, Utilities.SCALE_FIT, LOGO_WIDTH, LOGO_HEIGHT);
         return new JLabel(new ImageIcon(scaledLogoImage));
     }
 
     /**
      * Set ticChart in the ticGraphPane corresponding to given reportNum.
      *
      * @param reportNum Number of QC report for which chart is to be set.
      */
     private void setTicGraphPaneChart(final int reportNum) {
         logger.fine("ViewerFrame setTicGraphPaneChart " + reportNum);
         if (ticGraphPane != null) {
             ticGraphPane.removeAll();
         }
         // Create the visible chart panel.
         final ChartPanel chartPanel = new ChartPanel(reportUnits.get(reportNum).getChartUnit().getTicChart());
         chartPanel.setPreferredSize(new Dimension(DESKTOP_PANE_WIDTH, 2 * CHART_HEIGHT));
         final JInternalFrame chartFrame = new JInternalFrame("Chart " + reportNum, true);
         final javax.swing.plaf.InternalFrameUI ifu = chartFrame.getUI();
         ((javax.swing.plaf.basic.BasicInternalFrameUI)ifu).setNorthPane(null);
         chartFrame.getContentPane().add(chartPanel);
         chartFrame.setPreferredSize(new Dimension(DESKTOP_PANE_WIDTH, 2 * CHART_HEIGHT));
         chartFrame.setBorder(null);
         chartFrame.pack();
         chartFrame.setLocation(0, 0);
         chartFrame.setVisible(true);
         ticGraphPane.add(chartFrame);
         // Finally refresh the frame.
         revalidate();
     }
 
     /**
      * Zoom all the ticCharts according to min and max zoom values as obtained from controlFrame.
      */
     private void zoomMinMax() {
         //TODO: Make a new user interface for Min Max zoom box
         final String minValue = minText.getText();
         final String maxValue = maxText.getText();
         int min;
         int max;
         try {
             min = Integer.parseInt(minValue);
             max = Integer.parseInt(maxValue);
         } catch (final NumberFormatException e) {
             logger.log(Level.SEVERE, "Something went wrong while reading min and max zoom values", e);
             JOptionPane.showMessageDialog(this, "Incorrect min or max. Resetting to 10 and 80", "Error",
                                           JOptionPane.ERROR_MESSAGE);
             minText.setText("10");
             maxText.setText("80");
             min = 10;
             max = 80;
         }
         if (min < 0 || max > 99 || min > 99 || max < 1 || min > max) {
             JOptionPane.showMessageDialog(this, "Incorrect min or max. Resetting to 10 and 80", "Error",
                                           JOptionPane.ERROR_MESSAGE);
             minText.setText("10");
             maxText.setText("80");
             min = 10;
             max = 80;
         }
         logger.fine("minValue = " + minValue + " maxValue = " + maxValue + " min = " + min + " max = " + max);
         for (final ChartPanel chartPanel : chartPanelList) {
             final XYPlot plot = (XYPlot) chartPanel.getChart().getPlot();
             plot.getDomainAxis().setRange(min, max);
             chartPanel.setRefreshBuffer(true);
             chartPanel.repaint();
         }
     }
 
     /**
      * Process user input events.
      *
      * @param actionEvent the action event.
      */
     @Override
     public void actionPerformed(final ActionEvent actionEvent) {
         final String actionCommand = actionEvent.getActionCommand();
         logger.fine("Corresponding action command is " + actionCommand + ", event class: " + actionEvent.getClass());
         if (actionCommand.startsWith(DETAILS_ACTION_PREFIX)) {
             //Parse action command to get reportUnit number
             final StringTokenizer commandTokenizer = new StringTokenizer(actionCommand, "-");
             commandTokenizer.nextToken();
             final int reportNum = Integer.parseInt(commandTokenizer.nextToken());
             logger.fine("Details requested for reportNum " + reportNum);
             final ReportUnit reportUnit = reportUnits.get(reportNum - 1);
             final DetailsFrame detailsFrame = new DetailsFrame(metricsParser.getMetricsListing(), reportUnit);
             detailsFrame.setVisible(true);
             detailsFrame.revalidate();
         } else if (actionCommand.toLowerCase().startsWith("zoom")) {
             handleZoomActions(actionCommand);
         } else if (actionCommand.startsWith("Sort")) {
             // Sort chart frame list according to chosen Sort criteria.
             newSortCriteria = actionCommand;
             sortChartFrameList();
         } else {
             handleMenuActions(actionCommand);
         }
     }
 
     /**
      * Handle the zoom action events.
      *
      * @param actionCommand the action command.
      */
     private void handleZoomActions(final String actionCommand) {
         if ("zoomMinMax".equals(actionCommand)) {
             zoomMinMax();
         } else {
             logger.fine("Number of chart panels: " + chartPanelList.size());
             for (final ChartPanel chartPanel : chartPanelList) {
                 switch (actionCommand) {
                     case "Zoom In":
                         chartPanel.zoomInDomain(0, 0);
                         break;
                     case "Zoom Original":
                         chartPanel.restoreAutoBounds();
                         break;
                     case "Zoom Out":
                         chartPanel.zoomOutDomain(0, 0);
                         break;
                     default:
                         logger.warning("Unexpected zoom action: " + actionCommand);
                 }
                 chartPanel.setRefreshBuffer(true);
                 chartPanel.repaint();
             }
         }
     }
 
     /**
      * Handle the menu action events.
      *
      * @param actionCommand the action command.
      */
     private void handleMenuActions(final String actionCommand) {
         switch (actionCommand) {
             case "ChangeRootDirectory":
                 new DataEntryForm(this, appProperties).displayRootDirectoryChooser();
                 break;
             case "SetFilter":
                 new DataEntryForm(this, appProperties).displayDateFilterEntryForm();
                 break;
             case "SelectMetrics":
                 // Display ChooseMetricsForm to select metrics to display.
                 final ChooseMetricsForm metricsForm = new ChooseMetricsForm(this, metricsParser, selectedMetricsKeys);
                 metricsForm.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                 metricsForm.pack();
                 RefineryUtilities.centerFrameOnScreen(metricsForm);
                 metricsForm.setVisible(true);
                 break;
             case "About":
                 final AboutFrame aboutFrame = new AboutFrame();
                 aboutFrame.setVisible(true);
                 aboutFrame.revalidate();
                 break;
             default:
                 logger.warning("Unexpected action: " + actionCommand);
         }
     }
 
     /**
      * Sort displayed report units according to user assigned sort criteria.
      */
     private void sortChartFrameList() {
         logger.fine("sortChartFrameList From " + currentSortCriteria + " To " + newSortCriteria);
         final StringTokenizer sortCriteriaTokenizer = new StringTokenizer(newSortCriteria, "@");
         sortCriteriaTokenizer.nextToken();
         //e.g. generic:date
         final String sortKey = sortCriteriaTokenizer.nextToken();
         //e.g. Asc or Des
         final String sortOrder = sortCriteriaTokenizer.nextToken();
         logger.fine("Sort requested according to " + sortKey + " order " + sortOrder);
         //Remove currently ordered report units and recreate them according to sort criteria
         if (orderedReportUnits != null) {
             orderedReportUnits.clear();
         }
         orderedReportUnits = new ArrayList<>();
         if (!"Compare".equals(sortKey)) {
             // TODO: can we use Collections.sort with a custom comparator here? [Freek]
             //add initial element
             orderedReportUnits.add(reportUnits.get(0));
             //Sort in ascending order
             for (int i = 1; i < reportUnits.size(); ++i) {
                 //new element will be inserted at position j or at the end of list
                 int insertAtIndex = orderedReportUnits.size();
                 for (int j = 0; j < orderedReportUnits.size(); ++j) {
                     //comparing new and old lists
                     final int result = reportUnits.get(i).compareTo(orderedReportUnits.get(j), sortKey);
                     //reportUnit(i) is < orderedUnit(j)
                     if (result == -1) {
                         insertAtIndex = j;
                         break;
                     }
                 }
                 //Add to specified index
                 orderedReportUnits.add(insertAtIndex, reportUnits.get(i));
             }
         } else if ("Compare".equals(sortKey)) {
             //Check checkbox flag status and group those reports together at the beginning of orderedReportUnits
             //Add all selected reports first i refers to original report number
             for (int i = 0; i < reportIsSelected.size(); ++i) {
                 if (reportIsSelected.get(i)) {
                     logger.fine("Selected report index = " + i);
                     orderedReportUnits.add(reportUnits.get(i));
                 }
             }
             //Later add all deselected reports 
             for (int i = 0; i < reportIsSelected.size(); ++i) {
                 if (!reportIsSelected.get(i)) {
                     orderedReportUnits.add(reportUnits.get(i));
                 }
             }
         }
         if (desktopPane != null) {
             // A new chart frame will be given to every report.
             desktopPane.removeAll();
         }
         if (sortOrder.equals("Asc")) {
             prepareChartsInOrder(true);
             // Set first report graph in the Tic Pane. -1 adjusts to the index.
             setTicGraphPaneChart(orderedReportUnits.get(0).getReportNum() - 1);
         } else if (sortOrder.equals("Des")) {
             prepareChartsInOrder(false);
             // Set last report graph in the Tic Pane.
             setTicGraphPaneChart(orderedReportUnits.get(orderedReportUnits.size() - 1).getReportNum() - 1);
         }
         currentSortCriteria = newSortCriteria;
         newSortCriteria = "";
     }
 
     /**
      * Create chart frames and add them to the desktop pane.
      *
      * @param ascending if <code>true</code>, the charts will be prepared in ascending order; if <code>false</code>, the
      *                  charts will be prepared in descending order.
      */
     private void prepareChartsInOrder(final boolean ascending) {
         logger.fine("ViewerFrame prepareChartsInOrder");
         if (chartPanelList != null) {
             chartPanelList.clear();
         }
         yCoordinate = 0;
         logger.fine("No. of orderedReportUnits = " + orderedReportUnits.size());
         for (int reportIndex = 0; reportIndex < orderedReportUnits.size(); reportIndex++) {
             final int sortedIndex = ascending ? reportIndex : orderedReportUnits.size() - reportIndex - 1;
             final JInternalFrame chartFrame = createChartFrame(orderedReportUnits.get(sortedIndex), reportIndex);
             chartFrame.setBorder(BorderFactory.createRaisedBevelBorder());
             chartFrame.pack();
             chartFrame.setLocation(0, yCoordinate);
             chartFrame.setVisible(true);
             desktopPane.add(chartFrame);
             logger.fine("yCoordinate = " + yCoordinate);
             yCoordinate += CHART_HEIGHT + 15;
         }
     }
 
     /**
      * Create an internal frame for a report showing the report number, selection check box, details button, metrics
      * panel and the TIC chart. The controls are created in three panels: 1) reportIdPanel, 2) metricsPanel, and
      * 3) chartPanel.
      *
      * TODO: clarify when we use reportNumber, reportUnit.getReportNum() - 1 and reportUnit.getReportNum(). [Freek]
      *
      * @param reportUnit the report unit.
      * @param reportNumber the report number.
      * @return an internal frame with all information from a single report.
      */
     private JInternalFrame createChartFrame(final ReportUnit reportUnit, final int reportNumber) {
         logger.fine("ViewerFrame createChartFrame " + reportNumber + " ");
 
         final ChartPanel chartPanel = new ChartPanel(reportUnit.getChartUnit().getTicChart());
         chartPanel.addChartMouseListener(this);
         chartPanel.setPreferredSize(new Dimension(CHART_PANEL_WIDTH, CHART_HEIGHT - 10));
         chartPanelList.add(chartPanel);
 
         final JPanel reportIdPanel = createReportIdPanel(reportUnit);
         // TODO: Layout.
         final JPanel metricsPanel = createOrUpdateMetricsPanel(reportUnit, null);
         reportUnitToMetricsPanel.put(reportUnit, metricsPanel);
 
         // TODO: Layout.
         final JPanel displayPanel = new JPanel();
         displayPanel.add(reportIdPanel);
         displayPanel.add(metricsPanel);
         displayPanel.add(chartPanel);
         displayPanel.setBorder(null);
 
         final JInternalFrame frame = new JInternalFrame("Chart " + reportNumber, true);
         frame.setName(Integer.toString(reportUnit.getReportNum() - 1));
         ((javax.swing.plaf.basic.BasicInternalFrameUI) frame.getUI()).setNorthPane(null);
         frame.getContentPane().add(displayPanel);
         frame.setBorder(null);
         frame.addMouseListener(this);
         return frame;
     }
 
     /**
      * Create a panel for a report showing the report number, the selection check box and the metrics details button.
      *
      * @param reportUnit the report unit to show.
      * @return the panel with the relevant information.
      */
     private JPanel createReportIdPanel(final ReportUnit reportUnit) {
         final JLabel reportNumberLabel = new JLabel(Integer.toString(reportUnit.getReportNum()));
         reportNumberLabel.setFont(Constants.REPORT_NUMBER_FONT);
 
         final JButton detailsButton = new JButton("Details");
         detailsButton.setFont(Constants.DEFAULT_FONT);
        detailsButton.setPreferredSize(new Dimension(80, 20));
         detailsButton.setActionCommand(DETAILS_ACTION_PREFIX + reportUnit.getReportNum());
         detailsButton.addActionListener(this);
 
         final JCheckBox selectionCheckBox = new JCheckBox("Compare");
         selectionCheckBox.setFont(Constants.DEFAULT_FONT);
         selectionCheckBox.setBackground(Color.WHITE);
         // TODO: perhaps it's easier to use the report number here as well, since any unique id is ok? [Freek]
         // Since reportNum is > 0
         selectionCheckBox.setName(Integer.toString(reportUnit.getReportNum() - 1));
         // This call to setSelected preserves selection status.
         // TODO: this call to setSelected is needed after a refresh of the report list? [Freek]
         selectionCheckBox.setSelected(reportIsSelected.get(reportUnit.getReportNum() - 1));
         selectionCheckBox.addItemListener(this);
 
        // TODO: Layout.
         final JPanel reportIdPanel = new JPanel();
         reportIdPanel.setFont(Constants.DEFAULT_FONT);
         reportIdPanel.setBackground(Color.WHITE);
         reportIdPanel.setForeground(Color.WHITE);
        reportIdPanel.add(reportNumberLabel);
        reportIdPanel.add(selectionCheckBox);
        reportIdPanel.add(detailsButton);
         reportIdPanel.setPreferredSize(new Dimension(CHECK_PANEL_WIDTH, CHART_HEIGHT));
 
         return reportIdPanel;
     }
 
     /**
      * Update the selected metrics: parse the data and change the metrics labels.
      *
      * @param selectedMetricsData the data of the selected metrics (keys and names).
      */
     public void updateSelectedMetrics(final List<String> selectedMetricsData) {
       //Update control frame to display newly selected metrics
         logger.fine("In updateSelectedMetrics - refreshing metrics values..");
         parseSelectedMetricsData(selectedMetricsData);
         for (final ReportUnit reportUnit : reportUnits) {
             if (reportUnitToMetricsPanel.containsKey(reportUnit)) {
                 createOrUpdateMetricsPanel(reportUnit, reportUnitToMetricsPanel.get(reportUnit));
             }
         }
         createOrUpdateSortPanel();
         revalidate();
     }
 
     /**
      * Create or update a panel with the metrics for the specified report unit.
      *
      * @param reportUnit the report unit
      * @param existingMetricsPanel <code>null</code> to create a new panel or an existing panel to replace the labels.
      * @return the panel with the metrics.
      */
     private JPanel createOrUpdateMetricsPanel(final ReportUnit reportUnit, final JPanel existingMetricsPanel) {
         // Create a new metrics panel or remove all labels from the existing panel.
        // TODO: Layout.
         final JPanel metricsPanel = existingMetricsPanel == null ? new JPanel() : existingMetricsPanel;
         if (existingMetricsPanel == null) {
             metricsPanel.setBackground(Color.WHITE);
             final GridLayout layout = new GridLayout(selectedMetricsNames.size(), 1);
             metricsPanel.setLayout(layout);
             metricsPanel.setPreferredSize(new Dimension(METRICS_PANEL_WIDTH, CHART_HEIGHT));
         } else {
             metricsPanel.removeAll();
         }
         // Add labels for each of the selected metrics.
         for (int metricIndex = 0; metricIndex < selectedMetricsNames.size(); metricIndex++) {
             final String metricName = selectedMetricsNames.get(metricIndex);
             final String metricValue = reportUnit.getMetricsValueFromKey(selectedMetricsKeys.get(metricIndex));
             final Color foregroundColor = LABEL_COLORS.get(metricIndex % LABEL_COLORS.size());
             final JLabel label = new JLabel(metricName + ": " + metricValue);
             label.setFont(Constants.DEFAULT_FONT);
             label.setForeground(foregroundColor);
             metricsPanel.add(label);
         }
         if (existingMetricsPanel != null) {
             metricsPanel.validate();
             metricsPanel.repaint();
         }
         return metricsPanel;
     }
 
     @Override
     public void itemStateChanged(final ItemEvent itemEvent) {
         //Find out index of selection, checked-unchecked and update CheckBoxList
         if (itemEvent.getSource() instanceof JCheckBox) {
             final JCheckBox thisCheckBox = (JCheckBox) itemEvent.getSource();
             logger.fine("Check box name = " + thisCheckBox.getName());
             final int checkBoxFlagIndex = Integer.parseInt(thisCheckBox.getName());
             //reportIsSelected will be maintained all the time according to reportNum
             if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
                 logger.fine("Selected");
                 reportIsSelected.set(checkBoxFlagIndex, true);
             } else if (itemEvent.getStateChange() == ItemEvent.DESELECTED) {
                 logger.fine("DeSelected");
                 reportIsSelected.set(checkBoxFlagIndex, false);
             }
         }
     }
 
     @Override
     public void mouseClicked(final MouseEvent mouseEvent) {
         final Component clickedComponent = mouseEvent.getComponent();
         if (clickedComponent instanceof JInternalFrame) {
             final JInternalFrame clickedFrame = (JInternalFrame) clickedComponent;
             logger.fine("Frame title = " + clickedFrame.getTitle() + ", frame name = " + clickedFrame.getName());
             setTicGraphPaneChart(Integer.parseInt(clickedFrame.getName()));
         }
     }
 
     @Override
     public void mouseEntered(final MouseEvent mouseEvent) {
     }
 
     @Override
     public void mouseExited(final MouseEvent mouseEvent) {
     }
 
     @Override
     public void mousePressed(final MouseEvent mouseEvent) {
     }
 
     @Override
     public void mouseReleased(final MouseEvent mouseEvent) {
     }
 
     /**
      * Select the related report when the user clicks a TIC chart.
      *
      * @param chartMouseEvent the chart mouse event.
      */
     @Override
     public void chartMouseClicked(final ChartMouseEvent chartMouseEvent) {
         // Parse chart index from title.
         final StringTokenizer tokenizer = new StringTokenizer(chartMouseEvent.getChart().getTitle().getText(), "= ");
         // Skip first token. Second token is chart index.
         tokenizer.nextToken();
         final int index = Integer.parseInt(tokenizer.nextToken());
         logger.fine("Graph Index from SubTitle = " + index);
         setTicGraphPaneChart(index);
     }
 
     @Override
     public void chartMouseMoved(final ChartMouseEvent chartMouseEvent) {
     }
 }
