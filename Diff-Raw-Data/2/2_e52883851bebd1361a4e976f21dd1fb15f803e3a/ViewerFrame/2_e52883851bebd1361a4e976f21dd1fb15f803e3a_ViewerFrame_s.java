 package nl.ctmm.trait.proteomics.qcviewer.gui;
 
 /**
  * InternalFrames: http://docs.oracle.com/javase/tutorial/uiswing/components/internalframe.html
  * Radio buttons: http://www.leepoint.net/notes-java/GUI/components/50radio_buttons/25radiobuttons.html
  * 
  * Swing layout: http://www.cs101.org/courses/fall05/resources/swinglayout/
  */
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
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
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import java.util.StringTokenizer;
 
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
 
 import nl.ctmm.trait.proteomics.qcviewer.Main;
 import nl.ctmm.trait.proteomics.qcviewer.input.DataEntryForm;
 import nl.ctmm.trait.proteomics.qcviewer.input.ReportUnit;
 import nl.ctmm.trait.proteomics.qcviewer.utils.OpenBrowser;
 
 import org.jfree.chart.ChartMouseEvent;
 import org.jfree.chart.ChartMouseListener;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.data.Range;
 
 /**
  * ViewerFrame with the GUI for the QC Report Viewer V2.
  *
  * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
  * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
  */
 
 public class ViewerFrame extends JFrame implements ActionListener, ItemListener, ChangeListener, MouseListener {
 	private static final long serialVersionUID = 1L;
 	private JDesktopPane desktopPane = new ScrollDesktop();
 	private JDesktopPane ticGraphPane = new ScrollDesktop();
 	List<ChartPanel> chartPanelList = new ArrayList<ChartPanel>(); //necessary for zooming
 	private List<Boolean> chartCheckBoxFlags = new ArrayList<Boolean>();
 	private static int CHART_HEIGHT = 150; 
 	private static int DESKTOP_PANE_WIDTH = 1130; 
 	private JTextField minText, maxText;
 	private List<ReportUnit> reportUnits = new ArrayList<ReportUnit>(); //preserve original report units 
 	private List<ReportUnit> orderedReportUnits = new ArrayList<ReportUnit>(); //use this list for display and other operations
 	private List<String> qcParamNames; 
 	private List<JRadioButton> sortButtons;
     private static final List<Color> LABEL_COLORS = Arrays.asList(
             Color.BLUE, Color.DARK_GRAY, Color.GRAY, Color.MAGENTA, Color.ORANGE, Color.RED, Color.BLACK);
 	private String currentSortCriteria = "";
 	private String newSortCriteria = "";
 	private Properties appProperties = null;
     /**
      * Creates a new instance of the demo.
      * 
      * @param title  the title.
      */
     public ViewerFrame(final Properties appProperties, final String title, final List<ReportUnit> reportUnits, final List<String> qcParamNames) {
         super(title);
     	System.out.println("ViewerFrame constructor");
         setPreferredSize(new Dimension(DESKTOP_PANE_WIDTH + 25, CHART_HEIGHT * 10));
         setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
         this.qcParamNames = qcParamNames;
         this.appProperties = appProperties;
         setReportUnits(reportUnits);
         setOrderedReportUnits(reportUnits);
         assembleComponents();
         setVisible(true);
         // Finally refresh the frame.
         revalidate();
     }
     
     /**
      * Assemble components of the ViewerFrame
      */
     public void assembleComponents() { 
     	System.out.println("ViewerFrame assembleComponents");
         //We need two split panes to create 3 regions in the main frame
         final JSplitPane splitPane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
         //Add static (immovable) Control frame
 	    JInternalFrame controlFrame = getControlFrame();
 	    final JSplitPane splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
 	    //Add desktopPane for displaying graphs and other QC Control
 	    int totalReports = orderedReportUnits.size();
 	    desktopPane.setPreferredSize(new Dimension(DESKTOP_PANE_WIDTH, totalReports * CHART_HEIGHT));
 	    prepareChartsInAscendingOrder(true);
 	    splitPane2.add(new JScrollPane(desktopPane));
 	    ticGraphPane.setPreferredSize(new Dimension(DESKTOP_PANE_WIDTH, 2 * CHART_HEIGHT));
 	    splitPane2.add(new JScrollPane(ticGraphPane));
 	    //Set initial tic Graph
 	    setTicGraphPaneChart(0);
 	    splitPane2.setOneTouchExpandable(true); //hide-show feature
 	    splitPane2.setDividerLocation(500); //DesktopPane holding graphs will appear 500 pixels large
         splitPane1.add(controlFrame);
 	    splitPane1.add(splitPane2);
 	    splitPane1.setOneTouchExpandable(true); //hide-show feature
 	    splitPane1.setDividerLocation(140); //control panel will appear 140 pixels large
 	    splitPane1.setPreferredSize(new Dimension(DESKTOP_PANE_WIDTH + 15, (int)(6.5 * CHART_HEIGHT)));
 	    getContentPane().add(splitPane1, "Center");
 	    setJMenuBar(createMenuBar());
 	    setVisible(true);
 	    revalidate();
     }
     
     /**
      * Create Menu Bar for settings tab
      */
     private JMenuBar createMenuBar() {
     	JMenuBar menuBar = new JMenuBar();
     	JMenu settingsMenu = new JMenu("Settings");
     	menuBar.add(settingsMenu);
     	JMenuItem newDirAction = new JMenuItem("Set Root Directory...");
     	settingsMenu.add(newDirAction);
     	newDirAction.setActionCommand("ChangeRootDirectory");
     	newDirAction.addActionListener(this);
     	JMenuItem newWebserAction = new JMenuItem("Set Webserver...");
     	settingsMenu.add(newWebserAction);
     	newWebserAction.setActionCommand("ChangeServer");
     	newWebserAction.addActionListener(this);
     	JMenuItem filterAction = new JMenuItem("Set Filter...");
     	settingsMenu.add(filterAction);
     	filterAction.setActionCommand("SetFilter");
     	filterAction.addActionListener(this);
     	JMenuItem newRefAction = new JMenuItem("Refresh");
     	settingsMenu.add(newRefAction);
     	newRefAction.setActionCommand("Refresh");
     	newRefAction.addActionListener(this);
     	return menuBar;
     }
     
     /**
      * Sets the report units to be displayed.
      *
      * @param reportUnits the report units to be displayed.
      */
     public void setReportUnits(final List<ReportUnit> reportUnits) {
     	System.out.println("ViewerFrame setReportUnits No. of reportUnits = " + reportUnits.size());
         this.reportUnits = reportUnits;
         //Initialize chartCheckBoxFlags to false
         for (int i = 0; i < reportUnits.size(); ++i) {
         	chartCheckBoxFlags.add(false);
         }
     }
     
     /**
      * Sets the report units to be displayed.
      *
      * @param reportUnits the report units to be displayed.
      */
     public void setOrderedReportUnits(final List<ReportUnit> reportUnits) {
     	System.out.println("ViewerFrame setOrderedReportUnits No. of reportUnits = " + reportUnits.size());
     	if (orderedReportUnits != null) {
     		orderedReportUnits.clear();
     	}
     	for (int i = 0; i < reportUnits.size(); ++i) { 
     		orderedReportUnits.add(reportUnits.get(i));
     	}
     	System.out.println("ViewerFrame setOrderedReportUnits No. of ordered reportUnits = " + orderedReportUnits.size());
     }
     
     private JInternalFrame getControlFrame() {
     	System.out.println("ViewerFrame getControlFrame");
     	final JInternalFrame controlFrame = new JInternalFrame("Control Panel", true);
         javax.swing.plaf.InternalFrameUI ifu= controlFrame.getUI();
         ((javax.swing.plaf.basic.BasicInternalFrameUI)ifu).setNorthPane(null);
         GridLayout layout = new GridLayout(2,1);
         JPanel zoomPanel = new JPanel();
         zoomPanel.setLayout(layout);
         zoomPanel.setPreferredSize(new Dimension(230, 130));
         zoomPanel.setBackground(Color.WHITE);
         // Zoom all - in, original, out
         JRadioButton inButton = new JRadioButton("In", false);
         inButton.setActionCommand("Zoom In");
         inButton.setBackground(Color.WHITE);
         inButton.addActionListener(this);
         JRadioButton originalButton = new JRadioButton("Original", true);
         originalButton.setActionCommand("Zoom Original");
         originalButton.setBackground(Color.WHITE);
         originalButton.addActionListener(this);
         JRadioButton outButton = new JRadioButton("Out", false);
         outButton.setActionCommand("Zoom Out");
         outButton.setBackground(Color.WHITE);
         outButton.addActionListener(this);
         ButtonGroup zoomGroup = new ButtonGroup();
         zoomGroup.add(inButton);
         zoomGroup.add(originalButton);
         zoomGroup.add(outButton);
         layout = new GridLayout(1,3);
         JPanel zoomPanelRadio = new JPanel();
         zoomPanelRadio.setPreferredSize(new Dimension(230, 40));
         zoomPanelRadio.setBackground(Color.WHITE); 
         zoomPanelRadio.setLayout(layout);
         zoomPanelRadio.add(inButton);
         zoomPanelRadio.add(originalButton);
         zoomPanelRadio.add(outButton);
         // Zoom all - Min, Max and Submit
         layout = new GridLayout(1,5);
         JPanel zoomPanelForm = new JPanel(); 
         JLabel minLabel = new JLabel("Min: ");
         minText = new JFormattedTextField(NumberFormat.getInstance());
         minText.setPreferredSize(new Dimension(20, 20));
         JLabel maxLabel = new JLabel("Max: ");
         maxText = new JFormattedTextField(NumberFormat.getInstance());
         maxText.setPreferredSize(new Dimension(20, 20));
         JButton zoomButton = new JButton("Zoom");
         zoomButton.setActionCommand("ZoomMinMax");
         zoomButton.addActionListener(this);
         zoomPanelForm.add(minLabel);
         zoomPanelForm.add(minText);
         zoomPanelForm.add(maxLabel);
         zoomPanelForm.add(maxText); 
         zoomPanelForm.add(zoomButton); 
         zoomPanelForm.setPreferredSize(new Dimension(230, 80));
         zoomPanelForm.setBackground(Color.WHITE); 
         zoomPanel.setBorder(BorderFactory.createTitledBorder(
                 BorderFactory.createEtchedBorder(), "Zoom All"));
         zoomPanel.add(zoomPanelRadio, 0);
         zoomPanel.add(zoomPanelForm, 1);
         controlFrame.getContentPane().add(zoomPanel, 0);
         ButtonGroup sortGroup = new ButtonGroup();
         layout = new GridLayout(qcParamNames.size()/2+1,6);
         sortButtons = new ArrayList<JRadioButton>();
         JPanel sortPanel = new JPanel();
         sortPanel.setLayout(layout);
         sortPanel.setPreferredSize(new Dimension(600, 130));
         sortPanel.setBackground(Color.WHITE); 
         int style = Font.BOLD;
 	    Font font = new Font ("Garamond", style , 11);
         for (int i = 0; i < qcParamNames.size(); ++i) {
         	JLabel thisLabel = new JLabel(qcParamNames.get(i) + ": ");
         	thisLabel.setFont(font);
         	sortPanel.add(thisLabel);
         	//Sort ascending button
         	JRadioButton ascButton = new JRadioButton("Asc", false);
         	ascButton.setBackground(Color.WHITE);
         	ascButton.setActionCommand("Sort-" + qcParamNames.get(i) + "-Asc");
         	ascButton.addActionListener(this);
         	sortPanel.add(ascButton);
         	sortGroup.add(ascButton);
         	sortButtons.add(ascButton);
         	//Sort descending button
         	JRadioButton desButton = new JRadioButton("Des", false);
         	desButton.setBackground(Color.WHITE);
         	desButton.setActionCommand("Sort-" + qcParamNames.get(i) + "-Des");
         	desButton.addActionListener(this);
         	sortPanel.add(desButton); 
         	sortGroup.add(desButton); 
         	sortButtons.add(desButton);
         }
         //Add sorting according to Compare 
     	JLabel thisLabel = new JLabel("Compare: ");
     	thisLabel.setFont(font);
     	sortPanel.add(thisLabel);
     	//Sort ascending button
     	JRadioButton ascButton = new JRadioButton("Asc", false);
     	ascButton.setBackground(Color.WHITE);
     	ascButton.setActionCommand("Sort-" + "Compare" + "-Asc");
     	ascButton.addActionListener(this);
     	sortPanel.add(ascButton);
     	sortGroup.add(ascButton);
     	sortButtons.add(ascButton);
     	//Sort descending button
     	JRadioButton desButton = new JRadioButton("Des", false);
     	desButton.setBackground(Color.WHITE);
     	desButton.setActionCommand("Sort-" + "Compare" + "-Des");
     	desButton.addActionListener(this);
     	sortPanel.add(desButton); 
     	sortGroup.add(desButton); 
     	sortButtons.add(desButton);
         //Set first button selected
         sortButtons.get(0).setSelected(true);
         this.currentSortCriteria = sortButtons.get(0).getActionCommand(); 
         sortPanel.setBorder(BorderFactory.createTitledBorder(
                 BorderFactory.createEtchedBorder(), "Sort Options"));
         //controlFrame.getContentPane().add(sortPanel, 1);
         //Add logo to control frame
         BufferedImage oplLogo = null;
 		try {
 			oplLogo = ImageIO.read(new File("images\\opllogo.jpg"));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
         JLabel oplLabel = new JLabel(new ImageIcon(oplLogo));
         JPanel oplPanel = new JPanel();
         oplPanel.add(oplLabel);
         JPanel controlPanel = new JPanel();
         controlPanel.add(oplPanel, 0);
         controlPanel.add(zoomPanel, 1);
         controlPanel.add(sortPanel, 2);
         controlPanel.setBorder(null);
         controlFrame.getContentPane().add(controlPanel);
         controlFrame.setPreferredSize(new Dimension(DESKTOP_PANE_WIDTH, 150));
         controlFrame.pack();
         controlFrame.setLocation(0, 0);
         controlFrame.setBorder(null);
         controlFrame.setVisible(true);
         return controlFrame;
     }
     
     private void setTicGraphPaneChart(int reportNum) {
     	System.out.println("ViewerFrame setTicGraphPaneChart " + reportNum);
 	    if (ticGraphPane != null) {
 	    	ticGraphPane.removeAll();
 	    }
         int yCoordinate = 0;
         //Create the visible chart panel
        final ChartPanel chartPanel = new ChartPanel(orderedReportUnits.get(reportNum).getChartUnit().getTicChart());
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
 	
 	@Override
 	public void actionPerformed(ActionEvent evt) {
 		System.out.println("Corresponding action command is " + evt.getActionCommand() 
 				+ " evt class = " + evt.getClass());
 		//Check whether Details button is pressed - in order to open corresponding hyperlink 
 		if (evt.getActionCommand().startsWith("http://")) {
 			OpenBrowser.openURL(evt.getActionCommand().trim());
 		} //Check whether zoom to particular range is pressed 
 		else if (evt.getActionCommand().equals("ZoomMinMax")) { 
 			String minValue = minText.getText();
 			String maxValue = maxText.getText();
 			int min = 0, max = 99; 
 			try {
 				min = Integer.parseInt(minValue); 
 				max = Integer.parseInt(maxValue); 
 			} catch (NumberFormatException e) {
 				JOptionPane.showMessageDialog(this,"Incorrect min or max. Resetting to 10 and 80",
 						  "Error",JOptionPane.ERROR_MESSAGE);
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
 			System.out.println("minValue = " + minValue + " maxValue = " + maxValue + " min = " + min + " max = " + max);
 			Iterator<ChartPanel> it = chartPanelList.iterator();
 			System.out.println("Number of chart panels = " + chartPanelList.size());
 			while(it.hasNext()) {
 				ChartPanel cPanel = (ChartPanel) it.next();
 				JFreeChart chart = cPanel.getChart(); 
 				XYPlot plot = (XYPlot) chart.getPlot();
 				plot.getDomainAxis().setRange(min, max);
 				cPanel.setRefreshBuffer(true);
 				cPanel.repaint();
 			}
 		} //Check whether zoom in - all is selected
 		else if (evt.getActionCommand().equals("Zoom In")) {
 			Iterator<ChartPanel> it = chartPanelList.iterator();
 			System.out.println("Number of chart panels = " + chartPanelList.size());
 			while(it.hasNext()) {
 				ChartPanel cPanel = (ChartPanel) it.next();
 				cPanel.zoomInDomain(0, 0);
 				cPanel.setRefreshBuffer(true);
 				cPanel.repaint();
 			}
 		} //Check whether zoom Original - all is selected 
 		else if (evt.getActionCommand().equals("Zoom Original")) {
 			Iterator<ChartPanel> it = chartPanelList.iterator();
 			System.out.println("Number of chart panels = " + chartPanelList.size());
 			while(it.hasNext()) {
 				ChartPanel cPanel = (ChartPanel) it.next();
 				cPanel.restoreAutoBounds();
 				cPanel.setRefreshBuffer(true);
 				cPanel.repaint();
 			}
 		} //Check whether zoom out - all is selected 
 		else if (evt.getActionCommand().equals("Zoom Out")) {
 			Iterator<ChartPanel> it = chartPanelList.iterator();
 			System.out.println("Number of chart panels = " + chartPanelList.size());
 			while(it.hasNext()) {
 				ChartPanel cPanel = (ChartPanel) it.next();
 				cPanel.zoomOutDomain(0, 0);
 				cPanel.setRefreshBuffer(true);
 				cPanel.repaint();
 			}
 		} else if (evt.getActionCommand().startsWith("Sort")) {
 			newSortCriteria = evt.getActionCommand();
 			//if (! newSortCriteria.equals(currentSortCriteria)) {
 				sortChartFrameList();
 			//} else System.out.println("Already sorted according to " + newSortCriteria);
 		} else if (evt.getActionCommand().equals("ChangeRootDirectory")) {
 			//Get new location to read reports from
         	DataEntryForm deForm = new DataEntryForm(this, appProperties);
         	clean();
         	dispose();
         	deForm.displayRootDirectoryChooser();
 		} else if (evt.getActionCommand().equals("ChangeServer")) {
 			//Get new location to read reports from
 			clean();
         	dispose();
         	DataEntryForm deForm = new DataEntryForm(this, appProperties);
         	deForm.displayPreferredServerEntryForm();
 		} else if (evt.getActionCommand().equals("SetFilter")) {
 			//Get new location to read reports from
 			clean();
         	dispose();
         	DataEntryForm deForm = new DataEntryForm(this, appProperties);
         	deForm.displayDateFilterEntryForm();
 		} else if (evt.getActionCommand().equals("Refresh")) {
 			clean();
 			dispose();
 			new Main().runReportViewer();
 		}
 	}
 	
 	private void clean() {
 		if (desktopPane != null) {
 			desktopPane.removeAll();
 			desktopPane = null;
 		}
 		if (ticGraphPane != null) {
 			ticGraphPane.removeAll();
 			ticGraphPane = null;
 		}
 		if (chartPanelList != null) {
 			chartPanelList.clear();
 			chartPanelList = null;
 		}
 		if (chartCheckBoxFlags != null) {
 			chartCheckBoxFlags.clear();
 			chartCheckBoxFlags = null;
 		}
 		if (reportUnits != null) {
 			reportUnits.clear();
 			reportUnits = null;
 		}
 		if (orderedReportUnits != null) {
 			orderedReportUnits.clear();
 			orderedReportUnits = null;
 		}
 		if (sortButtons != null) {
 			sortButtons.clear();
 			sortButtons = null;
 		}
 	}
 	
     private void sortChartFrameList() {
 		System.out.println("sortChartFrameList From " + currentSortCriteria + " To " + newSortCriteria);
 		StringTokenizer stkz = new StringTokenizer(newSortCriteria, "-");
 		stkz.nextToken();
 		String sortParam = stkz.nextToken();
 		String sortOrder = stkz.nextToken(); 
 		System.out.println("Sort requested according to " + sortParam + " order " + sortOrder);
 		//Remove currently ordered report units and recreate them according to sort criteria
 		if (orderedReportUnits != null) {
 			orderedReportUnits.clear();
 		}
 		orderedReportUnits = new ArrayList<ReportUnit>();
 		if (!sortParam.equals("Compare")) { //Except for Compare based sort
 			orderedReportUnits.add(reportUnits.get(0)); //add initial element
 			//Sort in ascending order
 			for (int i = 1; i < reportUnits.size(); ++i) {
 				int insertAtIndex = orderedReportUnits.size(); //new element will be inserted at position j or at the end of list
 				for (int j = 0; j < orderedReportUnits.size(); ++j) {
 					int result = reportUnits.get(i).compareTo(orderedReportUnits.get(j), sortParam); //comparing new and old lists
 					if (result == -1) { //reportUnit(i) is < orderedUnit(j)
 						insertAtIndex = j;
 						break;
 					}
 				}
 				orderedReportUnits.add(insertAtIndex, reportUnits.get(i)); //Add to specified index
 				System.out.println("i = " + i + " insertAtIndex = " + insertAtIndex + " new size = " + orderedReportUnits.size());
 			}	
 		} else if (sortParam.equals("Compare")) { 
 			//Check checkboxflag status and group those reports together at the beginning of orderedReportUnits 
 			//Add all selected reports first i refers to original report number
 			for (int i = 0; i < chartCheckBoxFlags.size(); ++i) {
 				if (chartCheckBoxFlags.get(i)) {
 					System.out.println("Selected report index = " + i);
 					orderedReportUnits.add(reportUnits.get(i));
 				}
 			}
 			//Later add all deselected reports 
 			for (int i = 0; i < chartCheckBoxFlags.size(); ++i) {
 				if (!chartCheckBoxFlags.get(i)) {
 					System.out.println("Deselected report index = " + i);
 					orderedReportUnits.add(reportUnits.get(i));
 				}
 			}
 		}
 		if (desktopPane != null) {
 	    	desktopPane.removeAll(); //A new chart frame will be given to every report
 	    }
 		if (sortOrder.equals("Asc")) {
 			prepareChartsInAscendingOrder(true);
 			setTicGraphPaneChart(0); //Set first report graph in the Tic Pane
 		} else if (sortOrder.equals("Des")) {
 			prepareChartsInAscendingOrder(false);
 			setTicGraphPaneChart(orderedReportUnits.size()-1); ////Set last report graph in the Tic Pane
 		}
 		currentSortCriteria = newSortCriteria; 
 		newSortCriteria = "";
 	}
     
     /**
      * 
      * @param flag if true, charts will be prepared in ascending order. if false, the charts will be prepared in descending order
      */
     private void prepareChartsInAscendingOrder(boolean flag) {
 		System.out.println("ViewerFrame prepareChartsInAscendingOrder");
         int yCoordinate = 0;
         System.out.println("No. of orderedReportUnits = " + orderedReportUnits.size());
         for (int i = 0; i < orderedReportUnits.size(); ++i) {
         	JInternalFrame chartFrame;
         	if (flag) {
         		System.out.print("Report URI = " + orderedReportUnits.get(i).getDetailsUri() + " ");
         		//if (orderedReportUnits.get(i).getChartUnit() == null) {
         			chartFrame = createChartFrame(i, orderedReportUnits.get(i).getChartUnit().getTicChart(), orderedReportUnits.get(i));
         		//} else chartFrame = createChartFrame(i, orderedReportUnits.get(i).getChartUnit().getTicChart(), orderedReportUnits.get(i));
         	} else {
         		int index = orderedReportUnits.size() - i - 1;
         		System.out.print("Report URI = " + orderedReportUnits.get(index).getDetailsUri() + " ");
         		//if (orderedReportUnits.get(index).getChartUnit() == null) {
         			chartFrame = createChartFrame(i, orderedReportUnits.get(index).getChartUnit().getTicChart(), orderedReportUnits.get(orderedReportUnits.size() - i - 1));
         		//} else chartFrame = createChartFrame(i, orderedReportUnits.get(index).getChartUnit().getTicChart(), orderedReportUnits.get(orderedReportUnits.size() - i - 1));
         	}
         	chartFrame.pack();
         	chartFrame.setLocation(0, yCoordinate);
         	chartFrame.setVisible(true);
         	desktopPane.add(chartFrame);
         	yCoordinate += CHART_HEIGHT;
         }
 	}
     
 	/**
      * Creates an internal frame.
      * setSelected is required to preserve check boxes status in the display
      * @return An internal frame.
      */
     private JInternalFrame createChartFrame(int chartNum, JFreeChart chart, ReportUnit reportUnit) {
     	System.out.println("ViewerFrame createChartFrame " + chartNum);
         //Create the visible chart panel
         ChartPanel chartPanel = new ChartPanel(chart);
         chartPanel.setPreferredSize(new Dimension(800, CHART_HEIGHT - 10));
         chartPanelList.add(chartPanel);
         final JInternalFrame frame = new JInternalFrame("Chart " + chartNum, true);
         frame.setName(Integer.toString(reportUnit.getReportNum() - 1)); //Set report index number as frame name
         javax.swing.plaf.InternalFrameUI ifu= frame.getUI();
         ((javax.swing.plaf.basic.BasicInternalFrameUI)ifu).setNorthPane(null);
         int style = Font.BOLD;
 	    Font font = new Font ("Garamond", style , 11);
         //Create a checkbox for selection
         JCheckBox chartCheckBox = new JCheckBox("Compare");
         chartCheckBox.setFont(font);
         chartCheckBox.setBackground(Color.WHITE);
       //ChartCheckBoxName is same as report number which is unique
       //chartCheckBoxFlags are organized according to original report num - which is same as ChartCheckBoxName
         chartCheckBox.setName(Integer.toString(reportUnit.getReportNum() - 1)); //Since reportNum is > 0
         if (chartCheckBoxFlags.get(reportUnit.getReportNum() - 1)) {
         	chartCheckBox.setSelected(true); 
         } else chartCheckBox.setSelected(false);
         chartCheckBox.addItemListener(this);
         
         //Create a button for viewing URL based report
         JButton uriButton = new JButton("Details");
         uriButton.setFont(font);
         uriButton.setPreferredSize(new Dimension(80, 20));
         //Hyperlink to be replaced with path to browser report
         uriButton.setActionCommand(reportUnit.getDetailsUri().toString());
         uriButton.addActionListener(this);
         //uriButton.setBackground(Color.WHITE);
         JPanel checkPanel = new JPanel();
         checkPanel.setFont(font);
         checkPanel.setBackground(Color.WHITE);
         checkPanel.setForeground(Color.WHITE); 
         //GridLayout layout = new GridLayout(2, 1);
         //checkPanel.setLayout(layout);
         checkPanel.add(uriButton, 0);
         checkPanel.add(chartCheckBox, 1);
         checkPanel.setPreferredSize(new Dimension(90, CHART_HEIGHT));
         
         JPanel labelPanel = new JPanel();
         labelPanel.setFont(font);
         labelPanel.setBackground(Color.WHITE);
         GridLayout layout = new GridLayout(qcParamNames.size(),1);
         labelPanel.setLayout(layout);
         labelPanel.setPreferredSize(new Dimension(200, CHART_HEIGHT));
         // add qcparam labels, one in each cell
         //No., File Size(MB), MS1Spectra, MS2Spectra, Measured, Runtime(hh:mm:ss), maxIntensity
         for (int i = 0; i < qcParamNames.size(); ++i) { 
     		Color fgColor = LABEL_COLORS.get(i%LABEL_COLORS.size());
         	if (qcParamNames.get(i).trim().equalsIgnoreCase("No.")) {
         		JLabel thisLabel = new JLabel(qcParamNames.get(i) + ": " + (reportUnit.getReportNum()));
         		thisLabel.setFont(font);
         		thisLabel.setForeground(fgColor);
         		labelPanel.add(thisLabel);
         	} else if (qcParamNames.get(i).trim().equalsIgnoreCase("File Size(MB)")) {
         		JLabel thisLabel = new JLabel(qcParamNames.get(i) + ": " + reportUnit.getFileSizeString());
         		thisLabel.setFont(font);
         		thisLabel.setForeground(fgColor);
         		labelPanel.add(thisLabel);
         	} else if (qcParamNames.get(i).trim().equalsIgnoreCase("MS1Spectra")) {
         		JLabel thisLabel = new JLabel(qcParamNames.get(i) + ": " + reportUnit.getMs1Spectra());
         		thisLabel.setFont(font);
         		thisLabel.setForeground(fgColor);
         		labelPanel.add(thisLabel);
         	} else if (qcParamNames.get(i).trim().equalsIgnoreCase("MS2Spectra")) {
         		JLabel thisLabel = new JLabel(qcParamNames.get(i) + ": " + reportUnit.getMs2Spectra());
         		thisLabel.setFont(font);
         		thisLabel.setForeground(fgColor);
         		labelPanel.add(thisLabel);
         	} else if (qcParamNames.get(i).trim().equalsIgnoreCase("Measured")) {
         		JLabel thisLabel = new JLabel(qcParamNames.get(i) + ": " + reportUnit.getMeasured());
         		thisLabel.setFont(font);
         		thisLabel.setForeground(fgColor);
         		labelPanel.add(thisLabel);
         	} else if (qcParamNames.get(i).trim().equalsIgnoreCase("Runtime(hh:mm:ss)")) {
         		JLabel thisLabel = new JLabel(qcParamNames.get(i) + ": " + reportUnit.getRuntime());
         		thisLabel.setFont(font);
         		thisLabel.setForeground(fgColor);
         		labelPanel.add(thisLabel);
         	} else if (qcParamNames.get(i).trim().equalsIgnoreCase("maxIntensity")) {
         		double maxIntensity = reportUnit.getChartUnit().getMaxTicIntensity(); 
         		NumberFormat formatter = new DecimalFormat("0.0000E0");
         		JLabel thisLabel = new JLabel(qcParamNames.get(i) + ": " + formatter.format(maxIntensity));
         		thisLabel.setForeground(fgColor);
         		thisLabel.setFont(font);
         		labelPanel.add(thisLabel);
         	}
         }
         JPanel displayPanel = new JPanel();
         displayPanel.add(checkPanel, 0);
         displayPanel.add(labelPanel, 1);
         displayPanel.add(chartPanel, 2);
         displayPanel.setBorder(null);
         frame.getContentPane().add(displayPanel);
         frame.addMouseListener(this);
         frame.setBorder(null);
         return frame;
     }
  
 	@Override
 	public void itemStateChanged(ItemEvent evt) {
 		//Find out index of selection, checked-unchecked and update CheckBoxList
 		if (evt.getSource().getClass().getName().equals("javax.swing.JCheckBox")) {
 			JCheckBox thisCheckBox = (JCheckBox) evt.getSource();
 			System.out.println("Check box name = " + thisCheckBox.getName());
 			int checkBoxFlagIndex = Integer.parseInt(thisCheckBox.getName());
 			//chartCheckBoxFlags will be maintained all the time according to reportNum
 			if (evt.getStateChange() == ItemEvent.SELECTED) {
 				System.out.print("Selected");
 				chartCheckBoxFlags.set(checkBoxFlagIndex, true);
 			} else if (evt.getStateChange() == ItemEvent.DESELECTED) {
 				System.out.print("DeSelected");
 				chartCheckBoxFlags.set(checkBoxFlagIndex, false); 
 			}
 		}
 	}
 	
 	@Override
 	public void mouseClicked(MouseEvent arg0) {
 		System.out.println("mouseClicked X = " + arg0.getX() + " Y = " + arg0.getY() + 
 				" X on screen = " + arg0.getXOnScreen() + " Y on screen = " + arg0.getYOnScreen()
 				+ " Component class = " + arg0.getComponent().getClass().getName());
 		Component clickedComponent = arg0.getComponent(); 
 		if (clickedComponent.getClass().getName().equals("javax.swing.JInternalFrame")) {
 			JInternalFrame clickedFrame = (JInternalFrame) clickedComponent;
 			System.out.println("Frame title = " + clickedFrame.getTitle() + " Frame name = " + clickedFrame.getName());
 			setTicGraphPaneChart(Integer.parseInt(clickedFrame.getName()));
 		}
 	} 
 
 	@Override
 	public void mouseEntered(MouseEvent arg0) {
 		System.out.println("mouseEntered");
 	}
 
 	@Override
 	public void mouseExited(MouseEvent arg0) {
 		System.out.println("mouseExited");
 	}
 
 	@Override
 	public void mousePressed(MouseEvent arg0) {
 		System.out.println("MousePressed");
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent arg0) {
 		System.out.println("mouseReleased");
 	}
 
 
 }
 
