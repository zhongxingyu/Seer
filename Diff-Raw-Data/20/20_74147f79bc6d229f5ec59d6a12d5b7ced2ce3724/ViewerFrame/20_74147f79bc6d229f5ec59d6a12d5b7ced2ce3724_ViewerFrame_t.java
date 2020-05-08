 package nl.ctmm.trait.proteomics.qcviewer.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
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
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.StringTokenizer;
 import java.util.logging.ConsoleHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.imageio.ImageIO;
 import javax.swing.BorderFactory;
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
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import nl.ctmm.trait.proteomics.qcviewer.input.MetricsParser;
 import nl.ctmm.trait.proteomics.qcviewer.input.ReportUnit;
 import nl.ctmm.trait.proteomics.qcviewer.utils.Constants;
 import nl.ctmm.trait.proteomics.qcviewer.utils.Utilities;
 
 import org.apache.commons.io.FilenameUtils;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
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
 public class ViewerFrame extends JFrame implements ActionListener, ItemListener, ChangeListener, MouseListener {
     /**
      * The logger for this class.
      */
     private static final Logger logger = Logger.getLogger(ViewerFrame.class.getName());
 
     /**
      * The version number for (de)serialization of this class (UID: universal identifier).
      */
     private static final long serialVersionUID = 1;
 
     private static final List<Color> LABEL_COLORS = Arrays.asList(
             Color.BLUE, Color.DARK_GRAY, Color.GRAY, Color.MAGENTA, Color.ORANGE, Color.RED, Color.BLACK);
 
     private static final int CHECK_PANEL_SIZE = 90;
     private static final int LABEL_PANEL_SIZE = 350;
     private static final int CHART_PANEL_SIZE = 800;
     private static final int CHART_HEIGHT = 150;
     private static final int DESKTOP_PANE_WIDTH = 1270;
     private static final int SPLITPANE_DIVIDER_LOCATION = 185; 
     private static final int CTMM_LOGO_WIDTH = 179; 
     private static final int CTMM_LOGO_HEIGHT = 100; 
 
     private JDesktopPane desktopPane = new ScrollDesktop();
     private JDesktopPane ticGraphPane = new ScrollDesktop();
     private List<ChartPanel> chartPanelList = new ArrayList<>(); //necessary for zooming
     private List<Boolean> chartCheckBoxFlags = new ArrayList<>();
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
     private JLabel statusLabel;
     private JPanel statusPanel;
     private int yCoordinate = 0;
 
     /**
      * Creates a new instance of the demo.
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
             final StringTokenizer dataTokenizer = new StringTokenizer(selectedMetricData, ":");
             final String key = dataTokenizer.nextToken() + ":" + dataTokenizer.nextToken();
             this.selectedMetricsKeys.add(key);
             final String value = dataTokenizer.nextToken();
             this.selectedMetricsNames.add(value);
         }
     }
 
  /**
  * New report units are available. Update report units in the viewer frame
  * @param newReportUnits New QC reports 
  * @param newPipelineStatus Updated pipeline status
  */
     public void updateReportUnits(final ArrayList<ReportUnit> newReportUnits, final String newPipelineStatus, final Boolean replaceFlag) {
         logger.fine("In updateReportUnits yCoordinate = " + yCoordinate);
         if (replaceFlag) { //Replace all existing reports by newReportUnits
         	reportUnits.clear();
         	orderedReportUnits.clear();
         	chartCheckBoxFlags.clear();
         	desktopPane.removeAll();
         	ticGraphPane.removeAll();
        	pack();
        	revalidate();
         	yCoordinate = 0; 
         }
         int numReportUnits = reportUnits.size();
         if (newReportUnits.size() > 0) {
             for (int i = 0; i < newReportUnits.size(); ++i) {
                 ReportUnit thisUnit = newReportUnits.get(i);
                 reportUnits.add(thisUnit);
                 orderedReportUnits.add(thisUnit);
                 chartCheckBoxFlags.add(false);
                 //update desktopFrame
                 JInternalFrame chartFrame = createChartFrame(i + numReportUnits, thisUnit.getChartUnit().getTicChart(), thisUnit);
                 chartFrame.setBorder(BorderFactory.createRaisedBevelBorder());
                 chartFrame.pack();
                 chartFrame.setLocation(0, yCoordinate);
                 desktopPane.add(chartFrame);
                 chartFrame.setVisible(true);
                 logger.fine("yCoordinate = " + yCoordinate);
                 yCoordinate +=  CHART_HEIGHT + 15;
             }
             desktopPane.setPreferredSize(new Dimension(DESKTOP_PANE_WIDTH, reportUnits.size() * (CHART_HEIGHT + 15)));
           //Set first report graph in the Tic Pane. -1 adjusts to the index. 
             setTicGraphPaneChart(orderedReportUnits.get(0).getReportNum() - 1); 
         }
         updatePipelineStatus(newPipelineStatus);
         revalidate();
     }
     
     /**
      * Update pipelineStatus in the report viewer
      * @param newPipelineStatus as inferred from qc_status.log file
      */
     public void updatePipelineStatus(final String newPipelineStatus) {
         //logger.fine("ViewerFrame updatePipelineStatus");
         pipelineStatus = newPipelineStatus;
         statusPanel.removeAll();
         final String status = pipelineStatus + " | | | | | Number of report units = " + orderedReportUnits.size();
         statusLabel = new JLabel (status);
         statusLabel.setFont(Constants.DEFAULT_FONT);
         statusLabel.setBackground(Color.CYAN);
         statusPanel.setBackground(Color.CYAN);
         statusPanel.add(statusLabel);
         revalidate();
     }
     
     /**
      * Assemble following components of the ViewerFrame. 
      * 1) ControlFrame 2) desktopPane 3) ticGraphPane 4) MenuBar
      */
     private void assembleComponents() { 
         logger.fine("ViewerFrame assembleComponents");
         //We need two split panes to create 3 regions in the main frame
         //Add static (immovable) Control frame
         JInternalFrame controlFrame = getControlFrame();
         //Add desktopPane for displaying graphs and other QC Control
         int totalReports = orderedReportUnits.size();
         
         if (totalReports != 0) {
             desktopPane.setPreferredSize(new Dimension(DESKTOP_PANE_WIDTH, totalReports * (CHART_HEIGHT + 15)));
             prepareChartsInAscendingOrder(true);
             splitPane2.add(new JScrollPane(desktopPane), 0);
             ticGraphPane.setPreferredSize(new Dimension(DESKTOP_PANE_WIDTH, 2 * CHART_HEIGHT));
             splitPane2.add(new JScrollPane(ticGraphPane), 1);
             //Set initial tic Graph - specify complete chart in terms of orderedReportUnits
             setTicGraphPaneChart(orderedReportUnits.get(0).getReportNum() - 1);
             splitPane2.setOneTouchExpandable(true); //hide-show feature
             splitPane2.setDividerLocation(500); //DesktopPane holding graphs will appear 500 pixels large
             splitPane1.add(new JScrollPane(controlFrame));
             splitPane1.add(splitPane2);
             splitPane1.setOneTouchExpandable(true); //hide-show feature
             splitPane1.setDividerLocation(SPLITPANE_DIVIDER_LOCATION); 
             splitPane1.setPreferredSize(new Dimension(DESKTOP_PANE_WIDTH + 15, (int)(6.5 * CHART_HEIGHT)));
             getContentPane().add(splitPane1, "Center");
             setJMenuBar(createMenuBar());
         } else {
             //Display empty desktopPane and ticGraphPane
             splitPane2.add(new JScrollPane(desktopPane), 0);
             ticGraphPane.setPreferredSize(new Dimension(DESKTOP_PANE_WIDTH, 2 * CHART_HEIGHT));
             splitPane2.add(new JScrollPane(ticGraphPane), 1);
             splitPane2.setOneTouchExpandable(true); //hide-show feature
             splitPane2.setDividerLocation(500); //DesktopPane holding graphs will appear 500 pixels large
             splitPane1.add(controlFrame);
             splitPane1.add(splitPane2);
             splitPane1.setOneTouchExpandable(true); //hide-show feature
             splitPane1.setDividerLocation(170); //control panel will appear 170 pixels large
             splitPane1.setPreferredSize(new Dimension(DESKTOP_PANE_WIDTH + 15, (int)(6.5 * CHART_HEIGHT)));
             getContentPane().add(splitPane1, "Center");
             setJMenuBar(createMenuBar());
         }
     }
     
     /**
      * Create Menu Bar for settings and about tab
      */
     private JMenuBar createMenuBar() {
         JMenuBar menuBar = new JMenuBar();
         JMenu settingsMenu = new JMenu("Settings");
         menuBar.add(settingsMenu);
         JMenuItem newDirAction = new JMenuItem("Set Root Directory...");
         settingsMenu.add(newDirAction);
         newDirAction.setActionCommand("ChangeRootDirectory");
         newDirAction.addActionListener(this);
         JMenuItem filterAction = new JMenuItem("Set Filter...");
         settingsMenu.add(filterAction);
         filterAction.setActionCommand("SetFilter");
         filterAction.addActionListener(this);
         JMenuItem metricsAction = new JMenuItem("Select Metrics...");
         settingsMenu.add(metricsAction);
         metricsAction.setActionCommand("SelectMetrics");
         metricsAction.addActionListener(this);
         JMenuItem aboutAction = new JMenuItem("About...");
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
         //Initialize chartCheckBoxFlags to false
         for (final ReportUnit ignored : reportUnits) {
             chartCheckBoxFlags.add(false);
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
      * Prepare and return controlFrame
      * @return JInternalFrame controlFrame
      */
     private JInternalFrame getControlFrame() {
     	logger.fine("ViewerFrame getControlFrame");
     	logger.fine("Project logo file is " + Constants.CTMM_TRAIT_LOGO_FILE_NAME);
         JInternalFrame controlFrame = new JInternalFrame("Control Panel", true);
         javax.swing.plaf.InternalFrameUI ifu= controlFrame.getUI();
         ((javax.swing.plaf.basic.BasicInternalFrameUI)ifu).setNorthPane(null);
         controlFrame.setBorder(null);
         controlFrame.setLayout(new BorderLayout(0, 0));
         controlFrame.setBackground(Color.WHITE);
         GridLayout layout = new GridLayout(2,1);
         // Zoom all - in, original, out
         JButton inButton = new JButton("In [+]");
         inButton.setActionCommand("Zoom In");
         inButton.addActionListener(this);
         JButton originalButton = new JButton("Original");
         originalButton.setActionCommand("Zoom Original");
         originalButton.addActionListener(this);
         JButton outButton = new JButton("Out [-]");
         outButton.setActionCommand("Zoom Out");
         outButton.addActionListener(this);
         ButtonGroup zoomGroup = new ButtonGroup();
         zoomGroup.add(inButton);
         zoomGroup.add(originalButton);
         zoomGroup.add(outButton);
         JPanel zoomPanelButtons = new JPanel();
         zoomPanelButtons.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Zoom All TIC Charts"));
         zoomPanelButtons.setPreferredSize(new Dimension(250, 20));
         zoomPanelButtons.setBackground(Color.WHITE); 
         //zoomPanelButtons.setLayout(layout);
         zoomPanelButtons.add(inButton);
         zoomPanelButtons.add(originalButton);
         zoomPanelButtons.add(outButton);
         // Zoom all - Min, Max and Submit
         JLabel minLabel = new JLabel("Min: ");
         minText = new JFormattedTextField(NumberFormat.getInstance());
         minText.setValue(10);
         minText.setPreferredSize(new Dimension(20, 20));
         minText.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                 zoomMinMax();
             }
         });
         JLabel maxLabel = new JLabel("Max: ");
         maxText = new JFormattedTextField(NumberFormat.getInstance());
         maxText.setValue(80);
         maxText.setPreferredSize(new Dimension(20, 20));
         maxText.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                 zoomMinMax();
             }
         });
         JButton zoomButton = new JButton("Zoom X Axis");
         zoomButton.setActionCommand(
         		"zoomMinMax");
         zoomButton.addActionListener(this);
         JPanel zoomPanelForm = new JPanel();
         zoomPanelForm.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Zoom Along X Axis"));
         zoomPanelForm.add(minLabel);
         zoomPanelForm.add(minText);
         zoomPanelForm.add(maxLabel);
         zoomPanelForm.add(maxText); 
         zoomPanelForm.add(zoomButton); 
         zoomPanelForm.setPreferredSize(new Dimension(250, 80));
         zoomPanelForm.setBackground(Color.WHITE); 
         JPanel zoomPanel = new JPanel();
         layout = new GridLayout(2,1);
         zoomPanel.setLayout(layout);
         zoomPanel.setPreferredSize(new Dimension(250, 130));
         zoomPanel.setBackground(Color.WHITE);
         zoomPanel.add(zoomPanelButtons, 0);
         zoomPanel.add(zoomPanelForm, 1);
         ButtonGroup sortGroup = new ButtonGroup();
         layout = new GridLayout(selectedMetricsNames.size() / 2 + 1,2);
         sortButtons = new ArrayList<>();
         JPanel sortPanel = new JPanel();
         sortPanel.setLayout(layout);
         sortPanel.setPreferredSize(new Dimension(700, 130));
         sortPanel.setBackground(Color.WHITE); 
         for (int i = 0; i < selectedMetricsNames.size(); ++i) {
             JLabel thisLabel = new JLabel(selectedMetricsNames.get(i) + ": ");
             thisLabel.setFont(Constants.DEFAULT_FONT);
             thisLabel.setBackground(Color.WHITE);
             JPanel namePanel = new JPanel(new GridLayout(1,1));
             namePanel.add(thisLabel);
             namePanel.setBackground(Color.WHITE);
             //Sort ascending button
             JRadioButton ascButton = new JRadioButton("Asc", false);
             ascButton.setBackground(Color.WHITE);
             ascButton.setActionCommand("Sort@" + selectedMetricsKeys.get(i) + "@Asc");
             ascButton.addActionListener(this);
             sortGroup.add(ascButton);
             sortButtons.add(ascButton);
             //Sort descending button
             JRadioButton desButton = new JRadioButton("Des", false);
             desButton.setBackground(Color.WHITE);
             desButton.setActionCommand("Sort@" + selectedMetricsKeys.get(i) + "@Des");
             desButton.addActionListener(this);
             sortGroup.add(desButton); 
             sortButtons.add(desButton);
             JPanel buttonPanel = new JPanel(new GridLayout(1,2));
             buttonPanel.add(ascButton);
             buttonPanel.add(desButton);
             JPanel metricPanel = new JPanel(new GridLayout(1,2));
             metricPanel.add(namePanel, 0);
             metricPanel.add(buttonPanel, 1);
             sortPanel.add(metricPanel);
         }
         //Add sorting according to Compare 
         JLabel thisLabel = new JLabel("Compare: ");
         thisLabel.setFont(Constants.DEFAULT_FONT);
         thisLabel.setBackground(Color.WHITE);
         JPanel namePanel = new JPanel(new GridLayout(1,1));
         namePanel.setBackground(Color.WHITE);
         namePanel.add(thisLabel);
         //Sort ascending button
         JRadioButton ascButton = new JRadioButton("Asc", false);
         ascButton.setBackground(Color.WHITE);
         ascButton.setActionCommand("Sort@" + "Compare" + "@Asc");
         ascButton.addActionListener(this);
         sortGroup.add(ascButton);
         sortButtons.add(ascButton);
         //Sort descending button
         JRadioButton desButton = new JRadioButton("Des", false);
         desButton.setBackground(Color.WHITE);
         desButton.setActionCommand("Sort@" + "Compare" + "@Des");
         desButton.addActionListener(this);
         sortGroup.add(desButton); 
         sortButtons.add(desButton); 
         JPanel buttonPanel = new JPanel(new GridLayout(1,2));
         buttonPanel.add(ascButton);
         buttonPanel.add(desButton);
         JPanel metricPanel = new JPanel(new GridLayout(1,2));
         metricPanel.add(namePanel, 0);
         metricPanel.add(buttonPanel, 1);
         sortPanel.add(metricPanel);
         
         //Set first button selected
         sortButtons.get(0).setSelected(true);
         this.currentSortCriteria = sortButtons.get(0).getActionCommand(); 
         sortPanel.setBorder(BorderFactory.createTitledBorder(
                 BorderFactory.createEtchedBorder(), "Sort Options"));
 
         //Add opl logo to control frame
         BufferedImage oplLogo = null;
         try {
             oplLogo = ImageIO.read(new File(FilenameUtils.normalize(Constants.OPL_LOGO_FILE_NAME)));
         } catch (IOException e) {
         	logger.log(Level.SEVERE, "Something went wrong while reading OPL logo file", e);
         }
         JLabel oplLabel = new JLabel(new ImageIcon(oplLogo));
         JPanel oplPanel = new JPanel();
         oplPanel.add(oplLabel);
         
         //Add trait logo to control frame
         BufferedImage traitCtmmLogo = null;
         try {
             traitCtmmLogo = ImageIO.read(new File(FilenameUtils.normalize(Constants.CTMM_TRAIT_LOGO_FILE_NAME)));
         } catch (IOException e) {
         	logger.log(Level.SEVERE, "Something went wrong while reading project logo file", e);
         }
         JLabel traitCtmmLabel = new JLabel(new ImageIcon(Utilities.scaleImage(traitCtmmLogo, Utilities.SCALE_FIT, CTMM_LOGO_WIDTH, CTMM_LOGO_HEIGHT)));
         JPanel traitCtmmPanel = new JPanel();
         traitCtmmPanel.add(traitCtmmLabel);
         JPanel controlPanel = new JPanel(new FlowLayout());
         controlPanel.add(oplPanel, 0);
         controlPanel.add(zoomPanel, 1);
         controlPanel.add(sortPanel, 2);
         controlPanel.add(traitCtmmPanel, 3);
         
         controlFrame.getContentPane().add(controlPanel, BorderLayout.NORTH);
         String status = pipelineStatus + " | | | | | Number of report units = " + orderedReportUnits.size(); 
         statusLabel = new JLabel(status);
         statusLabel.setFont(Constants.DEFAULT_FONT);
         statusLabel.setBackground(Color.CYAN);
         statusPanel = new JPanel();
         statusPanel.setBackground(Color.CYAN); 
         statusPanel.add(statusLabel);
         controlFrame.getContentPane().add(statusPanel, BorderLayout.SOUTH);
         controlFrame.setSize(new Dimension(DESKTOP_PANE_WIDTH + 30, 170));
         controlFrame.pack();
         controlFrame.setLocation(0, 0);
         controlFrame.setResizable(false); 
         controlFrame.setVisible(true);
         return controlFrame;
     }
     
     /**
      * Set ticChart in the ticGraphPane corresponding to given reportNum
      * @param reportNum Number of QC report for which chart is to be set
      */
     private void setTicGraphPaneChart(int reportNum) {
         logger.fine("ViewerFrame setTicGraphPaneChart " + reportNum);
         if (ticGraphPane != null) {
             ticGraphPane.removeAll();
         }
         int yCoordinate = 0;
         //Create the visible chart panel
         final ChartPanel chartPanel = new ChartPanel(reportUnits.get(reportNum).getChartUnit().getTicChart());
         chartPanel.setPreferredSize(new Dimension(DESKTOP_PANE_WIDTH, 2 * CHART_HEIGHT));
         final JInternalFrame chartFrame = new JInternalFrame("Chart " + reportNum, true);
         javax.swing.plaf.InternalFrameUI ifu= chartFrame.getUI();
         ((javax.swing.plaf.basic.BasicInternalFrameUI)ifu).setNorthPane(null);
         chartFrame.getContentPane().add(chartPanel);
         chartFrame.setPreferredSize(new Dimension(DESKTOP_PANE_WIDTH, 2 * CHART_HEIGHT));
         chartFrame.setBorder(null);
         chartFrame.pack();
         chartFrame.setLocation(0, yCoordinate);
         chartFrame.setVisible(true);
         ticGraphPane.add(chartFrame);
         // Finally refresh the frame.
         revalidate();
     }
 
     @Override
     public void stateChanged(ChangeEvent e) {
     }
     
     /**
      * Zoom all the ticCharts according to min and max zoom values as obtained from controlFrame  
      */
     private void zoomMinMax() {
     	//TODO: Make a new user interface for Min Max zoom box
         String minValue = minText.getText();
         String maxValue = maxText.getText();
         int min, max;
         try {
             min = Integer.parseInt(minValue); 
             max = Integer.parseInt(maxValue); 
         } catch (NumberFormatException e) {
         	logger.log(Level.SEVERE, "Something went wrong while reading min and max zoom values", e);
             JOptionPane.showMessageDialog(this, "Incorrect min or max. Resetting to 10 and 80", "Error",
                                           JOptionPane.ERROR_MESSAGE);
             minText.setText("10");
             maxText.setText("80");
             min = 10; 
             max = 80; 
         }
         if (min < 0 || max > 99 || min > 99 || max < 1 || min > max) {
             JOptionPane.showMessageDialog(this,"Incorrect min or max. Resetting to 10 and 80",
                       "Error",JOptionPane.ERROR_MESSAGE);
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
      */
     @Override
     public void actionPerformed(ActionEvent evt) {
         logger.fine("Corresponding action command is " + evt.getActionCommand() + " evt class = " + evt.getClass());
         //Check whether Details button is pressed - in order to open corresponding hyperlink 
 
         if (evt.getActionCommand().startsWith("Details")) {
             //Parse action command to get reportUnit number
             final StringTokenizer commandTokenizer = new StringTokenizer(evt.getActionCommand(), "-");
             commandTokenizer.nextToken();
             final int reportNum = Integer.parseInt(commandTokenizer.nextToken());
             logger.fine("Details requested for reportNum " + reportNum);
             final ReportUnit reportUnit = reportUnits.get(reportNum - 1); //-1 to adjust index
             DetailsFrame detailsFrame = new DetailsFrame(metricsParser.getMetricsListing(), reportUnit);
             detailsFrame.setVisible(true);
             detailsFrame.revalidate();
         }
         //Check whether zoom to particular range is pressed 
         else if (evt.getActionCommand().equals("zoomMinMax")) {
             zoomMinMax();
         } //Check whether zoom in - all is selected
         else if (evt.getActionCommand().equals("Zoom In")) {
             final Iterator<ChartPanel> it = chartPanelList.iterator();
             logger.fine("Number of chart panels = " + chartPanelList.size());
             while(it.hasNext()) {
                 final ChartPanel cPanel = it.next();
                 cPanel.zoomInDomain(0, 0);
                 cPanel.setRefreshBuffer(true);
                 cPanel.repaint();
             }
         } //Check whether zoom Original - all is selected 
         else if (evt.getActionCommand().equals("Zoom Original")) {
             final Iterator<ChartPanel> it = chartPanelList.iterator();
             logger.fine("Number of chart panels = " + chartPanelList.size());
             while(it.hasNext()) {
                 final ChartPanel cPanel = it.next();
                 cPanel.restoreAutoBounds();
                 cPanel.setRefreshBuffer(true);
                 cPanel.repaint();
             }
         } //Check whether zoom out - all is selected 
         else if (evt.getActionCommand().equals("Zoom Out")) {
             final Iterator<ChartPanel> it = chartPanelList.iterator();
             logger.fine("Number of chart panels = " + chartPanelList.size());
             while(it.hasNext()) {
                 final ChartPanel cPanel = it.next();
                 cPanel.zoomOutDomain(0, 0);
                 cPanel.setRefreshBuffer(true);
                 cPanel.repaint();
             }
         } else if (evt.getActionCommand().startsWith("Sort")) {
             //Sort chart frame list according to chosen Sort criteria
             newSortCriteria = evt.getActionCommand();
             sortChartFrameList();
         } else if (evt.getActionCommand().equals("ChangeRootDirectory")) {
             //Get new location to read reports from
             final DataEntryForm deForm = new DataEntryForm(this, appProperties);
             deForm.displayRootDirectoryChooser();
         } else if (evt.getActionCommand().equals("SetFilter")) {
             //Get new location to read reports from
             final DataEntryForm deForm = new DataEntryForm(this, appProperties);
             deForm.displayDateFilterEntryForm();
         } else if (evt.getActionCommand().equals("SelectMetrics")) {
             //Display ChooseMetricsForm to select metrics to display
             final ChooseMetricsForm cmForm = new ChooseMetricsForm(this, metricsParser, selectedMetricsKeys);
             cmForm.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
             cmForm.pack();
             RefineryUtilities.centerFrameOnScreen(cmForm);
             cmForm.setVisible(true);
         } else if (evt.getActionCommand().equals("About")) {
             //Display AboutFrame
             final AboutFrame aboutFrame = new AboutFrame();
             aboutFrame.setVisible(true);
             aboutFrame.revalidate();
         }
     }
     
     /**
      * Sort displayed report units according to user assigned sort criteria
      */
     private void sortChartFrameList() {
         logger.fine("sortChartFrameList From " + currentSortCriteria + " To " + newSortCriteria);
         StringTokenizer sortCriteriaTokenizer = new StringTokenizer(newSortCriteria, "@");
         sortCriteriaTokenizer.nextToken();
         String sortKey = sortCriteriaTokenizer.nextToken(); //e.g. generic:date
         String sortOrder = sortCriteriaTokenizer.nextToken(); //e.g. Asc or Des
         logger.fine("Sort requested according to " + sortKey + " order " + sortOrder);
         //Remove currently ordered report units and recreate them according to sort criteria
         if (orderedReportUnits != null) {
             orderedReportUnits.clear();
         }
         orderedReportUnits = new ArrayList<>();
         if (!sortKey.equals("Compare")) { //Except for Compare based sort
             orderedReportUnits.add(reportUnits.get(0)); //add initial element
             //Sort in ascending order
             for (int i = 1; i < reportUnits.size(); ++i) {
                 int insertAtIndex = orderedReportUnits.size(); //new element will be inserted at position j or at the end of list
                 for (int j = 0; j < orderedReportUnits.size(); ++j) {
                     int result = reportUnits.get(i).compareTo(orderedReportUnits.get(j), sortKey); //comparing new and old lists
                     if (result == -1) { //reportUnit(i) is < orderedUnit(j)
                         insertAtIndex = j;
                         break;
                     }
                 }
                 orderedReportUnits.add(insertAtIndex, reportUnits.get(i)); //Add to specified index
             }    
         } else if (sortKey.equals("Compare")) { 
             //Check checkbox flag status and group those reports together at the beginning of orderedReportUnits
             //Add all selected reports first i refers to original report number
             for (int i = 0; i < chartCheckBoxFlags.size(); ++i) {
                 if (chartCheckBoxFlags.get(i)) {
                     logger.fine("Selected report index = " + i);
                     orderedReportUnits.add(reportUnits.get(i));
                 }
             }
             //Later add all deselected reports 
             for (int i = 0; i < chartCheckBoxFlags.size(); ++i) {
                 if (!chartCheckBoxFlags.get(i)) {
                     orderedReportUnits.add(reportUnits.get(i));
                 }
             }
         }
         if (desktopPane != null) {
             desktopPane.removeAll(); //A new chart frame will be given to every report
         }
         if (sortOrder.equals("Asc")) {
             prepareChartsInAscendingOrder(true);
             //Set first report graph in the Tic Pane. -1 adjusts to the index. 
             setTicGraphPaneChart(orderedReportUnits.get(0).getReportNum() - 1); 
         } else if (sortOrder.equals("Des")) {
             prepareChartsInAscendingOrder(false);
             setTicGraphPaneChart(orderedReportUnits.get(orderedReportUnits.size()-1).getReportNum() - 1); ////Set last report graph in the Tic Pane
         }
         currentSortCriteria = newSortCriteria; 
         newSortCriteria = "";
     }
     
     /**
      * 
      * @param flag if true, charts will be prepared in ascending order. if false, the charts will be prepared in descending order
      */
     private void prepareChartsInAscendingOrder(boolean flag) {
         logger.fine("ViewerFrame prepareChartsInAscendingOrder");
         if (chartPanelList != null) {
             chartPanelList.clear();
         }
         yCoordinate = 0;
         logger.fine("No. of orderedReportUnits = " + orderedReportUnits.size());
         for (int i = 0; i < orderedReportUnits.size(); ++i) {
             JInternalFrame chartFrame;
             if (flag) {
                 chartFrame = createChartFrame(i, orderedReportUnits.get(i).getChartUnit().getTicChart(),
                                               orderedReportUnits.get(i));
             } else {
                 int index = orderedReportUnits.size() - i - 1;
                 chartFrame = createChartFrame(i, orderedReportUnits.get(index).getChartUnit().getTicChart(), orderedReportUnits.get(orderedReportUnits.size() - i - 1));
             }
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
      * Creates an internal frame.
      * setSelected is required to preserve check boxes status in the display
      * @return An internal frame.
      */
     private JInternalFrame createChartFrame(final int chartNum, final JFreeChart chart, final ReportUnit reportUnit) {
         logger.fine("ViewerFrame createChartFrame " + chartNum + " ");
         //Create the visible chart frame consisting of three panels: 1) checkPanel 2) labelPanel 3) chartPanel
         //ChartPanel is rightmost panel holding TIC chart
         ChartPanel chartPanel = new ChartPanel(chart);
         chartPanel.setPreferredSize(new Dimension(CHART_PANEL_SIZE, CHART_HEIGHT - 10));
         chartPanelList.add(chartPanel);
         final JInternalFrame frame = new JInternalFrame("Chart " + chartNum, true);
         frame.setName(Integer.toString(reportUnit.getReportNum() - 1)); //Set report index number as frame name
 
         ((javax.swing.plaf.basic.BasicInternalFrameUI) frame.getUI()).setNorthPane(null);
 
         //Create a checkbox for selection
         JCheckBox chartCheckBox = new JCheckBox("Compare");
         chartCheckBox.setFont(Constants.DEFAULT_FONT);
         chartCheckBox.setBackground(Color.WHITE);
         //ChartCheckBoxName is same as report number which is unique
         //chartCheckBoxFlags are organized according to original report num - which is same as ChartCheckBoxName
         chartCheckBox.setName(Integer.toString(reportUnit.getReportNum() - 1)); //Since reportNum is > 0
         chartCheckBox.setSelected(chartCheckBoxFlags.get(reportUnit.getReportNum() - 1));
         chartCheckBox.addItemListener(this);
 
         final JButton detailsButton = new JButton("Details");
         detailsButton.setFont(Constants.DEFAULT_FONT);
         detailsButton.setPreferredSize(new Dimension(80, 20));
         //Use Details button to display all QC metrics values 
         detailsButton.setActionCommand("Details-" + Integer.toString(reportUnit.getReportNum()));
         detailsButton.addActionListener(this);
 
         //The leftmost panel holding reportNum, detailsButton and chartCheckBox
         final JPanel checkPanel = new JPanel();
         checkPanel.setFont(Constants.DEFAULT_FONT);
         checkPanel.setBackground(Color.WHITE);
         checkPanel.setForeground(Color.WHITE); 
         checkPanel.add(detailsButton, 0);
         checkPanel.add(chartCheckBox, 1);
         final JLabel numLabel = new JLabel(Integer.toString(reportUnit.getReportNum()));
         numLabel.setFont(Constants.REPORT_NUMBER_FONT);
         checkPanel.add(numLabel);
         checkPanel.setPreferredSize(new Dimension(CHECK_PANEL_SIZE, CHART_HEIGHT));
 
         //The middle panel displaying values of selected metrics
         final JPanel displayPanel = new JPanel();
         displayPanel.add(checkPanel);
         final JPanel metricsPanel = createOrUpdateMetricsPanel(reportUnit, null);
         reportUnitToMetricsPanel.put(reportUnit, metricsPanel);
         displayPanel.add(metricsPanel);
         displayPanel.add(chartPanel);
         displayPanel.setBorder(null);
         frame.getContentPane().add(displayPanel);
 
         frame.addMouseListener(this);
         frame.setBorder(null);
         return frame;
     }
 
     /**
      * Update the selected metrics: parse the data and change the metrics labels.
      *
      * @param selectedMetricsData the data of the selected metrics (keys and names).
      */
     public void updateSelectedMetrics(final List<String> selectedMetricsData) {
         parseSelectedMetricsData(selectedMetricsData);
         for (final ReportUnit reportUnit : reportUnits) {
             if (reportUnitToMetricsPanel.containsKey(reportUnit)) {
                 createOrUpdateMetricsPanel(reportUnit, reportUnitToMetricsPanel.get(reportUnit));
             }
         }
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
         final JPanel metricsPanel = existingMetricsPanel == null ? new JPanel() : existingMetricsPanel;
         if (existingMetricsPanel == null) {
             metricsPanel.setBackground(Color.WHITE);
             final GridLayout layout = new GridLayout(selectedMetricsNames.size(), 1);
             metricsPanel.setLayout(layout);
             metricsPanel.setPreferredSize(new Dimension(LABEL_PANEL_SIZE, CHART_HEIGHT));
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
     public void itemStateChanged(ItemEvent evt) {
         //Find out index of selection, checked-unchecked and update CheckBoxList
         if (evt.getSource().getClass().getName().equals("javax.swing.JCheckBox")) {
             JCheckBox thisCheckBox = (JCheckBox) evt.getSource();
             logger.fine("Check box name = " + thisCheckBox.getName());
             int checkBoxFlagIndex = Integer.parseInt(thisCheckBox.getName());
             //chartCheckBoxFlags will be maintained all the time according to reportNum
             if (evt.getStateChange() == ItemEvent.SELECTED) {
                 logger.fine("Selected");
                 chartCheckBoxFlags.set(checkBoxFlagIndex, true);
             } else if (evt.getStateChange() == ItemEvent.DESELECTED) {
                 logger.fine("DeSelected");
                 chartCheckBoxFlags.set(checkBoxFlagIndex, false); 
             }
         }
     }
     
     @Override
     public void mouseClicked(MouseEvent arg0) {
         Component clickedComponent = arg0.getComponent(); 
         if (clickedComponent.getClass().getName().equals("javax.swing.JInternalFrame")) {
             JInternalFrame clickedFrame = (JInternalFrame) clickedComponent;
             logger.fine("Frame title = " + clickedFrame.getTitle() + " Frame name = " + clickedFrame.getName());
             setTicGraphPaneChart(Integer.parseInt(clickedFrame.getName()));
         }
     } 
 
     @Override
     public void mouseEntered(MouseEvent arg0) {
     }
 
     @Override
     public void mouseExited(MouseEvent arg0) {
     }
 
     @Override
     public void mousePressed(MouseEvent arg0) {
     }
 
     @Override
     public void mouseReleased(MouseEvent arg0) {
     }
 }
