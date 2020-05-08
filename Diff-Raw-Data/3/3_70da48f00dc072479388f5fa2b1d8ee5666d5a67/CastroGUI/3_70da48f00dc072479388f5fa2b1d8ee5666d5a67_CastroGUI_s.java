 package GUI;
 
 import java.awt.BorderLayout;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.Insets;
 import java.util.List;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.KeyEvent;
 import java.util.ArrayList;
 import java.util.Dictionary;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.LinkedList;
 import java.util.Set;
 
 import javax.media.j3d.Node;
 import javax.swing.AbstractButton;
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JEditorPane;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSlider;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingConstants;
 import javax.swing.border.Border;
 import javax.swing.border.LineBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.TableColumnModel;
 
 import Visualizer.CastroTableModel;
 import Visualizer.SpeechDetailPanel;
 import Visualizer.Visualize;
 
 import edu.uci.ics.jung.graph.Graph;
 import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
 import Functionality.DataModule;
 import Functionality.EdgeTypeEnum;
 import Functionality.IndexTypeEnum;
 import Functionality.SearchQuery;
 import Functionality.SearchQuerySimilarNodes;
 import Functionality.SearchQueryStandard;
 import Functionality.SimMatrixEnum;
 import Functionality.VertexDisplayPredicateMode;
 import Visualizer.CastroTableModel;
 import GUI.SettingsWindow;
 
 ;
 
 public class CastroGUI implements ActionListener, ChangeListener,
 		ComponentListener {
 
 	public static JFrame frame;
 	private Container content;
 
 	private JComboBox search_year_start;
 	private JComboBox search_year_end;
 	private JComboBox search_type;
 
 	private JButton search_button;
 	private JButton history_button;
 
 	private JTextField NE_textField;
 	private JTable table_search;
 
 	private JComponent graph_component;
 
 	private static Functionality.Graph bigGraph;
 
 	private JComboBox simMatCB;
 
 	private Visualize visu;
 
 	private JTextField maxDocsTB;
 	private JComboBox indexCB;
 	private JComboBox filterCB;
 	private Box vbFilterDistance;
 	private Box vbFilterNone;
 	private Box vbFilterActivation;
 	private Box vbFilter;
 	private JPanel graphPanel;
 	private JComboBox distanceFilterTypeCB;
 	private JComboBox edgeDisplayTypeCB;
 	private JSlider distanceSlider;
 	private JComboBox coreEdgeTypeCB;
 	private SpeechDetailPanel speechDetailPanel;
 
 	private Box vbEdgesRelative;
 	private Box vbEdgesAbsolute;
 
 	private JSlider edgeDensitySlider;
 	private JSlider edgeThresholdSlider;
 
 	private JCheckBox dottedEdgeChB;
 	private JCheckBox normalEdgeChB;
 	private JCheckBox thickEdgeChB;
 
 	private JMenuItem dataStructuresMenuItem;
 	private JMenuItem clusteringMenuItem;
 	private JMenuItem edgesMenuItem;
 	
 	
 	
 	private JButton layoutStartStopBtn;
 
 	private Integer maxNumNodes = 25;
 
 	private static int edgeThresholdSliderNumberOfValues = 100;
 	private static int edgeDensitySliderNumberOfValues = 100;
 
 	private static int frame_width = 1200;
 	private static int frame_height = 700;
 
 	public static Double edgeDensity = 2.5;
 	private static Double normalEdgeThreshold = 0.20;
 
 	private boolean lockLayout = true;
 
 	private static SelectionListener listener;
 	public static CastroGUI gui;
 	
 	private static SearchQuery currentQuery;
 
 	public CastroGUI() {
 
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		gui = new CastroGUI();
 		gui.init();
 	}
 
 	public static SearchQuery GetCurrentSearchQuery()
 	{
 		return currentQuery;
 	}
 	
 	private String getEdgeMode() {
 		return (String) (edgeDisplayTypeCB.getSelectedItem());
 	}
 
 	private void tableSearchSetColumnWidth() {
 		// String[] names = {"ID", "Author", "Title", "Type", "Location",
 		// "Report Date", "Source", "Speech Date"};
 		TableColumnModel tcm = table_search.getColumnModel();
 		tcm.getColumn(0).setPreferredWidth(20);
 		tcm.getColumn(1).setPreferredWidth(50);
 		tcm.getColumn(2).setPreferredWidth(150);
 		tcm.getColumn(3).setPreferredWidth(50);
 		tcm.getColumn(4).setPreferredWidth(70);
 		tcm.getColumn(5).setPreferredWidth(50);
 		tcm.getColumn(6).setPreferredWidth(120);
 		tcm.getColumn(7).setPreferredWidth(50);
 	}
 
 	public List<String> getQueryTerms() {
 		return processQueryString(NE_textField.getText());
 	}
 
 	public static void updateTableSelection(Set<Functionality.Node> sn) {
 		gui.table_search.getSelectionModel().removeListSelectionListener(
 				listener);
 
 		Functionality.Node node;
 		for (int i = 0; i < gui.table_search.getRowCount(); i++) {
 			node = DataModule.displayedGraph
 					.getNodeById((Integer) ((CastroTableModel) gui.table_search
 							.getModel()).getSpeechIDofSelectedRow(i));
 			if (sn.contains(node)) {
 				gui.table_search.addRowSelectionInterval(i, i);
 			} else {
 				gui.table_search.removeRowSelectionInterval(i, i);
 			}
 		}
 
 		gui.table_search.getSelectionModel().addListSelectionListener(listener);
 		listener.valueChanged(null);
 	}
 
 	public class SelectionListener implements ListSelectionListener {
 		JTable table;
 
 		SelectionListener(JTable table) {
 			this.table = table;
 		}
 
 		public void valueChanged(ListSelectionEvent e) {
 			int[] index = table.getSelectedRows();
 
 			if (index.length <= 0) {
 				return;
 			}
 
 			Set<Functionality.Node> sn = new HashSet<Functionality.Node>();
 
 			Integer id = new Integer(0);
 			for (int i = 0; i < index.length; i++) {
 				id = (Integer) ((CastroTableModel) table_search.getModel())
 						.getSpeechIDofSelectedRow(index[i]);
 
 				sn.add(DataModule.displayedGraph.getNodeById(id));
 
 			}
 
 			// bigGraph.setCenter(bigGraph.getNodeById(id));
 			visu.FocusNodes(sn, true);
 		}
 	}
 
 	private void init() {
 
 		frame = new JFrame("History Explorer - Unlock the secrets of the past!");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		Functionality.DataModule.InitConfiguration();
 		Functionality.DataModule.Init(SettingsWindow.currIndex,
 				SettingsWindow.smoothedIndex, SettingsWindow.smoothedSimMatrix,
 				SettingsWindow.personsCoef, SettingsWindow.locationsCoef,
 				SettingsWindow.organizationsCoef,
 				SettingsWindow.lexicalSimilarityCoef);
 
 		content = frame.getContentPane();
 		content.setLayout(new BorderLayout(5, 5));
 
 		JMenuBar menuBar = new JMenuBar();
 
 		JMenu fileMenu = new JMenu("File");
 		
 		JMenuItem exitMenuItem = new JMenuItem("Exit");
 		
 		exitMenuItem.addActionListener(new ActionListener() {
 			
 			public void actionPerformed(ActionEvent e) {
 				System.exit(0);
 			}
 		});
 		
 		fileMenu.add(exitMenuItem);
 		menuBar.add(fileMenu);
 		
 		JMenu setupMenu = new JMenu("Setup");
 
 		dataStructuresMenuItem = new JMenuItem("Index and Similarity matrix");
 
 		dataStructuresMenuItem.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent arg0) {
 				SettingsWindow.Show();
 			}
 
 		});
 		
 		edgesMenuItem = new JMenuItem("Links setting");
 		
 		edgesMenuItem.addActionListener(new ActionListener() {
 			
 			public void actionPerformed(ActionEvent e) {
 				EdgesSettings.Show();	
 			}
 		});
 
 		clusteringMenuItem = new JMenuItem("Graph clustering setting");
 		
 		clusteringMenuItem.addActionListener(new ActionListener() {
 			
 			public void actionPerformed(ActionEvent e) {
 				ClusteringSettings.Show();
 				
 			}
 		});
 		
 		setupMenu.add(dataStructuresMenuItem);
 		setupMenu.add(edgesMenuItem);
 		setupMenu.add(clusteringMenuItem);
 		
 		menuBar.add(setupMenu);
 		frame.setJMenuBar(menuBar);
 
 		String[][] data = {};
 
 		String[] names = { "ID", "Author", "Title", "Type", "Location",
 				"Report Date", "Source", "Speech Date" };
 
 		Box frameNorthBox = Box.createVerticalBox();
 
 		table_search = new JTable(data, names);
 		table_search.setFillsViewportHeight(true);
 		table_search.setToolTipText(GuiConst.table_search_tooltip);
 		table_search.setColumnSelectionAllowed(false);
 		table_search.setRowSelectionAllowed(true);
 		table_search.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
 		listener = new SelectionListener(table_search);
 		table_search.getSelectionModel().addListSelectionListener(listener);
 
 		JScrollPane scroll_panel = new JScrollPane(table_search);
 		scroll_panel.setPreferredSize(new Dimension(3000, 100));
 
 		frameNorthBox.add(scroll_panel);
 
 		tableSearchSetColumnWidth();
 
 		search_year_start = new JComboBox();
 		search_year_end = new JComboBox();
 		search_type = new JComboBox();
 
 		search_year_start.setToolTipText(GuiConst.search_year_start_tooltip);
 		search_year_end.setToolTipText(GuiConst.search_year_end_tooltip);
 		search_type.setToolTipText(GuiConst.search_type_tooltip);
 		
 		String[] years = new String[37];
 		for (int i = 0; i < years.length; ++i) {
 			years[i] = new Integer(1959 + i).toString();
 		}
 
 		for (String s : years) {
 			search_year_start.addItem(s);
 			search_year_end.addItem(s);
 		}
 
 		search_year_end.setSelectedIndex(search_year_end.getItemCount() - 1);
 
 		Box horizontal_box = Box.createHorizontalBox();
 
 		search_type.addItem("All");
 		search_type.addItem("SPEECH");
 		search_type.addItem("INTERVIEW");
 		search_type.addItem("MESSAGE");
 		search_type.addItem("APPEARANCE");
 		search_type.addItem("MEETING");
 		search_type.addItem("REPORT");
 		NE_textField = new JTextField();
 		NE_textField.setText("\"Conrado Benitez\" \"Santiago Chile\" \"PRENSA LATINA Havana\"");
 		NE_textField.setToolTipText(GuiConst.NE_textField_tooltip);
 		
 		Box smallVB1 = Box.createVerticalBox();
 		JLabel bleLabel = new JLabel("Search terms:");
 		bleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
 		smallVB1.add(bleLabel);
 		smallVB1.add(Box.createVerticalStrut(5));
 		smallVB1.add(NE_textField);
 
 		Box smallVB1p1 = Box.createVerticalBox();
 
 		bleLabel = new JLabel("Max number of results:");
 		bleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
 
 		smallVB1p1.add(bleLabel);
 		maxDocsTB = new JTextField();
 		maxDocsTB.setText(maxNumNodes.toString());
 		maxDocsTB.setAlignmentX(Component.LEFT_ALIGNMENT);
 		maxDocsTB.setHorizontalAlignment(JTextField.RIGHT);
 		smallVB1p1.add(Box.createVerticalStrut(5));
 		smallVB1p1.setMaximumSize(new Dimension(80, 1000));
 		smallVB1p1.add(maxDocsTB);
 		maxDocsTB.setToolTipText(GuiConst.maxDocsTB_tooltip);
 		
 		Box smallVB2 = Box.createVerticalBox();
 		bleLabel = new JLabel("From:");
 		bleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
 		smallVB2.add(bleLabel);
 		smallVB2.add(Box.createVerticalStrut(5));
 		search_year_start.setAlignmentX(Component.LEFT_ALIGNMENT);
 		smallVB2.add(search_year_start);
 
 		Box smallVB3 = Box.createVerticalBox();
 		bleLabel = new JLabel("To:");
 		bleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
 		smallVB3.add(bleLabel);
 		smallVB3.add(Box.createVerticalStrut(5));
 		search_year_end.setAlignmentX(Component.LEFT_ALIGNMENT);
 		smallVB3.add(search_year_end);
 
 		Box smallVB4 = Box.createVerticalBox();
 		bleLabel = new JLabel("Type:");
 		bleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
 		smallVB4.add(bleLabel);
 		smallVB4.add(Box.createVerticalStrut(5));
 		search_type.setAlignmentX(Component.LEFT_ALIGNMENT);
 		smallVB4.add(search_type);
 
 		search_button = new JButton("Search");
 		search_button.setToolTipText(GuiConst.search_button_tooltip);
 		search_button.setVerticalTextPosition(AbstractButton.BOTTOM);
 		search_button.setHorizontalTextPosition(AbstractButton.CENTER);
 		search_button.setMnemonic(KeyEvent.VK_S);
 		search_button.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				
 				String SinceDate = (String) search_year_start.getSelectedItem() + "-01-01";
 				String TillDate = (String) search_year_end.getSelectedItem() + "-12-31";
 				String Author = "NULL";
 				String DocType = (String) search_type.getSelectedItem();
 				String Place = "NULL";
 				List<String> queryTerms = processQueryString(NE_textField.getText());
 				Integer maxNumNodes = Integer.parseInt(maxDocsTB.getText());
 
 				SearchQueryStandard sq = new SearchQueryStandard(queryTerms, SinceDate, TillDate, DocType, maxNumNodes);
 				performSearch(sq, true);
 			}
 		});
 
 		Box smallVB5 = Box.createVerticalBox();
 		bleLabel = new JLabel("    ");
 		// bleLabel.setBackground(Color.GREEN);
 		// bleLabel.setOpaque(true);
 		bleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
 		smallVB5.add(bleLabel);
 		smallVB5.add(Box.createVerticalStrut(5));
 		search_button.setAlignmentX(Component.LEFT_ALIGNMENT);
 		smallVB5.add(search_button);
 
 		Box smallVB6 = Box.createVerticalBox();
 		bleLabel = new JLabel("    ");
 		bleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
 		smallVB6.add(bleLabel);
 		smallVB6.add(Box.createVerticalStrut(5));
 		history_button = new JButton("History");
 		history_button.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				QueryHistoryWindow.Show();
 			}
 		});
 		smallVB6.add(history_button);
 		
 		horizontal_box.add(smallVB1);
 		Component strut1 = Box.createHorizontalStrut(10);
 		horizontal_box.add(strut1);
 
 		horizontal_box.add(smallVB1p1);
 		horizontal_box.add(Box.createHorizontalStrut(10));
 
 		horizontal_box.add(smallVB2);
 		Component strut2 = Box.createHorizontalStrut(10);
 		horizontal_box.add(strut2);
 		horizontal_box.add(smallVB3);
 		Component strut3 = Box.createHorizontalStrut(10);
 		horizontal_box.add(strut3);
 		horizontal_box.add(smallVB4);
 		Component strut4 = Box.createHorizontalStrut(10);
 		horizontal_box.add(strut4);
 		horizontal_box.add(smallVB5);
 		horizontal_box.add(Box.createHorizontalStrut(10));
 		horizontal_box.add(smallVB6);
 		
 		frameNorthBox.add(horizontal_box);
 
 		content.add(frameNorthBox, BorderLayout.NORTH);
 
 		Box featuresVB = Box.createVerticalBox();
 
 		Box hbFilter = Box.createHorizontalBox();
 		hbFilter.add(Box.createHorizontalStrut(10));
 		hbFilter.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Document Filter:"));
 
 		vbFilter = Box.createVerticalBox();
 
 		filterCB = new JComboBox(new String[] { "no filter", "distance filter" });
 		filterCB.setToolTipText(GuiConst.filterCB_tooltip);
 		filterCB.addActionListener(this);
 
 		vbFilter.add(filterCB);
 		vbFilter.add(Box.createVerticalStrut(10));
 
 		vbFilterDistance = Box.createVerticalBox();
 
 		distanceSlider = new JSlider(0, 3);
 		distanceSlider.setToolTipText(GuiConst.distanceSlider_tooltip);
 		distanceSlider.setMajorTickSpacing(1);
 		distanceSlider.setPaintLabels(true);
 		distanceSlider.setPaintTicks(true);
 		distanceSlider.setSnapToTicks(true);
 		distanceSlider.addChangeListener(this);
 
 		vbFilterDistance.add(distanceSlider);
 		vbFilterDistance.add(Box.createVerticalStrut(10));
 
 		distanceFilterTypeCB = new JComboBox(new String[] { "union", "intersection" });
 		distanceFilterTypeCB.setToolTipText(GuiConst.distanceFilterTypeCB_tooltip);
 		distanceFilterTypeCB.addActionListener(this);
 		vbFilterDistance.add(distanceFilterTypeCB);
 
 		vbFilterDistance.add(Box.createVerticalStrut(10));
 
 		coreEdgeTypeCB = new JComboBox(new String[] { "all edges", "normal and thick", "thick" });
 		coreEdgeTypeCB.setToolTipText(GuiConst.coreEdgeTypeCB_tooltip);
 		coreEdgeTypeCB.addActionListener(this);
 		vbFilterDistance.add(coreEdgeTypeCB);
 
 		vbFilterDistance.setVisible(false);
 
 		vbFilterNone = Box.createVerticalBox();
 		vbFilterNone.add(Box.createVerticalStrut(vbFilterDistance.getPreferredSize().height));
 
 		vbFilter.add(vbFilterNone);
 		vbFilter.add(vbFilterDistance);
 		vbFilter.add(Box.createVerticalStrut(10));
 		hbFilter.add(vbFilter);
 		hbFilter.add(Box.createHorizontalStrut(10));
 
 		featuresVB.add(hbFilter);
 
 		Box hbEdges = Box.createHorizontalBox();
 		hbEdges.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Links:"));
 		hbEdges.add(Box.createHorizontalStrut(10));
 
 		Box vbEdges = Box.createVerticalBox();
 		// vbEdges.add(Box.createVerticalStrut(5));
 
 		Dictionary<Integer, JComponent> edgeSliderDictionary = new Hashtable<Integer, JComponent>();
 		edgeSliderDictionary.put(0, new JLabel("0"));
 		edgeSliderDictionary.put(10, new JLabel("0.5"));
 		edgeSliderDictionary.put(20, new JLabel("1"));
 
 		edgeDisplayTypeCB = new JComboBox(new String[] { "absolute", "relative" });
 		edgeDisplayTypeCB.setToolTipText(GuiConst.edgeDisplayTypeCB_tooltip);
 		edgeDisplayTypeCB.addActionListener(this);
 
 		edgeDisplayTypeCB.setAlignmentX(Component.LEFT_ALIGNMENT);
 		vbEdges.add(edgeDisplayTypeCB);
 
 		vbEdgesRelative = Box.createVerticalBox();
 
 		bleLabel = new JLabel("link density:");
 		bleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
 
 		vbEdgesRelative.add(bleLabel);
 		vbEdgesRelative.add(Box.createVerticalStrut(5));
 
 		edgeDensitySlider = new JSlider(0, edgeDensitySliderNumberOfValues);
 		edgeDensitySlider.setAlignmentX(Component.LEFT_ALIGNMENT);
 		edgeDensitySlider.setPaintTicks(true);
 		edgeDensitySlider.setToolTipText(GuiConst.edgeDensitySlider_tooltip);
 		// dottedEdgeSlider.setPaintLabels(true);
 		edgeDensitySlider.setMajorTickSpacing(edgeDensitySliderNumberOfValues / 2);
 		edgeDensitySlider.setMinorTickSpacing(edgeDensitySliderNumberOfValues / 20);
 		edgeDensitySlider.setSnapToTicks(true);
 		edgeDensitySlider.addChangeListener(this);
 		setEdgeDensitySliderValue(edgeDensitySlider, edgeDensity);
 		// dottedEdgeSlider.setLabelTable(edgeSliderDictionary);
 
 		vbEdgesRelative.add(edgeDensitySlider);
 		vbEdgesRelative.setVisible(false);
 
 		vbEdges.add(vbEdgesRelative);
 		vbEdges.setAlignmentX(Component.LEFT_ALIGNMENT);
 		vbEdgesAbsolute = Box.createVerticalBox();
 
 		bleLabel = new JLabel("Link Threshold:");
 		bleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
 		vbEdgesAbsolute.add(bleLabel);
 		vbEdgesAbsolute.add(Box.createVerticalStrut(5));
 
 		edgeThresholdSlider = new JSlider(0, edgeThresholdSliderNumberOfValues);
 		edgeThresholdSlider.setToolTipText(GuiConst.edgeThresholdSlider_tooltip);
 		edgeThresholdSlider.setPaintTicks(true);
 		// dottedEdgeSlider.setPaintLabels(true);
 		edgeThresholdSlider.setMajorTickSpacing(edgeThresholdSliderNumberOfValues / 2);
 		edgeThresholdSlider.setMinorTickSpacing(edgeThresholdSliderNumberOfValues / 20);
 		edgeThresholdSlider.setSnapToTicks(true);
 		edgeThresholdSlider.addChangeListener(this);
 		edgeThresholdSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
 		setEdgeThresholdSliderValue(edgeThresholdSlider, normalEdgeThreshold);
 		// dottedEdgeSlider.setLabelTable(edgeSliderDictionary);
 
 		vbEdgesAbsolute.add(edgeThresholdSlider);
 		vbEdgesAbsolute.setAlignmentX(Component.LEFT_ALIGNMENT);
 		vbEdgesAbsolute.setVisible(true);
 
 		vbEdges.add(vbEdgesAbsolute);
 
 		Box hbDottedEdges = Box.createHorizontalBox();
 		JLabel dottedEdgeLabel = new JLabel("Dotted:");
 		
 		hbDottedEdges.add(dottedEdgeLabel);
 		hbDottedEdges.add(Box.createHorizontalStrut(3));
 
 		dottedEdgeChB = new JCheckBox();
 		dottedEdgeChB.setToolTipText(GuiConst.dottedEdgeChB_tooltip);
 		dottedEdgeChB.setSelected(true);
 		dottedEdgeChB.addChangeListener(this);
 		hbDottedEdges.add(dottedEdgeChB);
 		hbDottedEdges.setAlignmentX(Component.LEFT_ALIGNMENT);
 		vbEdges.add(hbDottedEdges);
 		vbEdges.add(Box.createVerticalStrut(5));
 
 		Box hbNormalEdges = Box.createHorizontalBox();
 		JLabel normalEdgeLabel = new JLabel("Normal:");
 		hbNormalEdges.add(normalEdgeLabel);
 		hbNormalEdges.add(Box.createHorizontalStrut(3));
 
 		normalEdgeChB = new JCheckBox();
 		normalEdgeChB.setToolTipText(GuiConst.normalEdgeChB_tooltip);
 		normalEdgeChB.setSelected(true);
 		normalEdgeChB.addChangeListener(this);
 		hbNormalEdges.add(normalEdgeChB);
 		hbNormalEdges.setAlignmentX(Component.LEFT_ALIGNMENT);
 		vbEdges.add(hbNormalEdges);
 		vbEdges.add(Box.createVerticalStrut(5));
 
 		Box hbThickEdges = Box.createHorizontalBox();
 		bleLabel = new JLabel("Thick:    ");
 		bleLabel.setMinimumSize(normalEdgeLabel.getPreferredSize());
 		hbThickEdges.add(bleLabel);
 		hbThickEdges.add(Box.createHorizontalStrut(3));
 
 		thickEdgeChB = new JCheckBox();
 		thickEdgeChB.setToolTipText(GuiConst.thickEdgeChB_tooltip);
 		thickEdgeChB.setSelected(true);
 		thickEdgeChB.addChangeListener(this);
 		hbThickEdges.add(thickEdgeChB);
 		hbThickEdges.setAlignmentX(Component.LEFT_ALIGNMENT);
 		vbEdges.add(hbThickEdges);
 		vbEdges.add(Box.createVerticalStrut(5));
 
 		hbEdges.add(vbEdges);
 		hbEdges.createHorizontalStrut(10);
 
 		featuresVB.add(hbEdges);
 		featuresVB.add(Box.createVerticalStrut(10));
 
 
 		featuresVB.add(Box.createVerticalStrut(5));
 
 		layoutStartStopBtn = new JButton("Arrange Graph");
 		layoutStartStopBtn.setToolTipText(GuiConst.layoutStartStopBtn_tooltip);
 		layoutStartStopBtn.addActionListener(this);
 		layoutStartStopBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
 
 		featuresVB.add(layoutStartStopBtn);
 		featuresVB.setPreferredSize(new Dimension(170, 400));
 
 		Box blebleBox = Box.createVerticalBox();
 		blebleBox.add(featuresVB);
 		Box emptyBleBox = Box.createHorizontalBox();
 		emptyBleBox.setPreferredSize(new Dimension(170, 10000));
 		blebleBox.add(emptyBleBox);
 
 		content.add(blebleBox, BorderLayout.WEST);
 		// featuresVB.setBounds(10 + insets.left, search_top + 50, 170,
 		// frame_height - search_top - 85);
 
 		JPanel centralPanel = new JPanel(new BorderLayout());
 
 		Box centralBox = Box.createVerticalBox();
 		graphPanel = new JPanel();
 		graphPanel.setLayout(new BorderLayout());
 		bleLabel = new JLabel("Similarity graph:");
 		bleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
 		centralBox.add(bleLabel);
 		graphPanel.setBorder(new LineBorder(Color.BLACK, 1));
 		graphPanel.setBackground(Color.WHITE);
 		graphPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
 		centralBox.add(graphPanel);
 		centralPanel.add(centralBox, BorderLayout.CENTER);
 
 
 		content.add(centralPanel, BorderLayout.CENTER);
 
 		JEditorPane jep = new JEditorPane();
 		jep.setEditable(true);
 
 		jep.setToolTipText(GuiConst.jep_tooltip);
 		JScrollPane jepScroll = new JScrollPane(jep);
 		jepScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
 		
 		Box jepBox = Box.createVerticalBox();
 		//jepBox.add(Box.createVerticalStrut(5));
 		bleLabel = new JLabel("Named entities:");
 		bleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
 		jepBox.add(bleLabel);
 		jepBox.add(jepScroll);
 		content.add(jepBox, BorderLayout.EAST);
 		jepScroll.setPreferredSize(new Dimension(180, 3000));
 		// jepScroll.setBounds(graphLeft + graphWidth + 10, graphTop, 180,
 		// graphHeight);
 		speechDetailPanel = new SpeechDetailPanel(jep);
 
 		frame.setSize(new Dimension(frame_width, frame_height));
 		// frame.setResizable(false);
 		frame.setVisible(true);
 	}
 
 	private void setDistanceFilter() {
 		EdgeTypeEnum ete;
 
 		if (coreEdgeTypeCB.getSelectedItem().equals("all edges")) {
 			ete = EdgeTypeEnum.dotted;
 		} else if (coreEdgeTypeCB.getSelectedItem().equals("normal and thick")) {
 			ete = EdgeTypeEnum.normal;
 		} else if (coreEdgeTypeCB.getSelectedItem().equals("thick")) {
 			ete = EdgeTypeEnum.thick;
 		} else {
 			ete = EdgeTypeEnum.dotted;
 		}
 
 		if (distanceFilterTypeCB.getSelectedItem().equals("union")) {
 
 			visu.setDistanceFilter(distanceSlider.getValue(),
 					VertexDisplayPredicateMode.conjunction, ete);
 		} else {
 			visu.setDistanceFilter(distanceSlider.getValue(),
 					VertexDisplayPredicateMode.disjunction, ete);
 		}
 
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		
 		if (e.getSource().equals(filterCB)) {
 			if (filterCB.getSelectedItem().equals("distance filter")) {
 				vbFilterNone.setVisible(false);
 				vbFilterDistance.setVisible(true);
 				if (visu != null) {
 					setDistanceFilter();
 				}
 			} else if (filterCB.getSelectedItem().equals("no filter")) {
 				vbFilterNone.setVisible(true);
 				vbFilterDistance.setVisible(false);
 				if (visu != null)
 					visu.setNoneFilter();
 			}
 		} else if (e.getSource().equals(distanceFilterTypeCB)) {
 			setDistanceFilter();
 		} else if (e.getSource().equals(coreEdgeTypeCB)) {
 			setDistanceFilter();
 		} else if (e.getSource().equals(edgeDisplayTypeCB)) {
 			if (edgeDisplayTypeCB.getSelectedItem().equals("relative")) {
 				vbEdgesAbsolute.setVisible(false);
 				vbEdgesRelative.setVisible(true);
 				DataModule.displayedGraph.ChangeEdgeDensities(edgeDensity,
 						SettingsWindow.normalEdgeRelativeMultiplier,
 						SettingsWindow.thickEdgeRelativeMultiplier);
 				redrawGraph();
 				System.err.println("Edge mode: relative");
 			} else {
 				vbEdgesAbsolute.setVisible(true);
 				vbEdgesRelative.setVisible(false);
 				DataModule.displayedGraph.ChangeEdgeThresholds(
 						normalEdgeThreshold,
 						SettingsWindow.dottedEdgeAbsoluteMultiplier,
 						SettingsWindow.thickEdgeAbsoluteMultiplier);
 				redrawGraph();
 				System.err.println("Edge mode: absolute");
 			}
 		} else if (e.getSource().equals(layoutStartStopBtn)) {
 			if (layoutStartStopBtn.getText().equals("Arrange Graph")) {
 				lockLayout = false;
 				layoutStartStopBtn.setText("Stop");
 				visu.LayoutStart();
 			} else {
 				lockLayout = true;
 				layoutStartStopBtn.setText("Arrange Graph");
 				visu.LayoutStop();
 			}
 		}
 		
 		if (e.getSource() instanceof JSlider)
 		{
 			System.err.println("Slider command: " + e.getActionCommand());
 		}
 
 	}
 
 	private static void setEdgeDensitySliderValue(JSlider slider, double value) {
 		slider.setValue((int) Math.round(value / (SettingsWindow.maxEdgeDensity / (double) edgeDensitySliderNumberOfValues)));
 	}
 
 	private static double getEdgeDensitySliderValue(JSlider slider) {
 		// System.err.println("getEdgeSliderValue: " + slider.getValue() *
 		// (maxEdgeDensity / (double)edgeDensitySliderNumberOfValues));
 		return (slider.getValue() - 1)
 				* (SettingsWindow.maxEdgeDensity / (double) edgeDensitySliderNumberOfValues);
 	}
 
 	private static void setEdgeThresholdSliderValue(JSlider slider, double value) {
 		// System.err.println("sedEdgeSliderValue: " + value);
 		slider.setValue((int) Math.round(value / (SettingsWindow.maxEdgeThreshold / (double) edgeThresholdSliderNumberOfValues)));
 	}
 
 	private static double getEdgeThresholdSliderValue(JSlider slider) {
 		// System.err.println("getEdgeSliderValue: " + slider.getValue() *
 		// (maxEdgeThreshold / (double)edgeThresholdSliderNumberOfValues));
 		return (slider.getValue() - 1) * (SettingsWindow.maxEdgeThreshold / (double) edgeThresholdSliderNumberOfValues);
 	}
 
 	private void visualizeGraph() {
 		// -198, -270
 		//DataModule.EvaluateClustering(2, 5);
 		visu = new Visualize(bigGraph, 800, 800);
 
 		if (graph_component != null) {
 			graphPanel.remove(graph_component);
 		}
 
 		// visu.thick_edge_theshold =
 		// Double.parseDouble(edgeThresholdTB.getText()) * 3.0 / 2.0;
 		// visu.normal_edge_threshold =
 		// Double.parseDouble(edgeThresholdTB.getText());
 		graph_component = visu.drawGraph();
 		graph_component.addComponentListener(this);
 		graphPanel.add(graph_component, BorderLayout.CENTER);
 		graphPanel.validate();
 		System.err.println("added to content");
 
 	}
 
 	/**
 	 * Create and return a table
 	 * 
 	 * @return JTable
 	 */
 
 	private static List<String> processQueryString(String str) {
 		int b = 0;
 		int parStartIndex = -1;
 		int normStartIndex = 0;
 
 		List<String> terms = new ArrayList<String>();
 
 		for (int i = 0; i < str.length(); i++) {
 			if (str.charAt(i) == '"') {
 				if (b == 0) {
 					parStartIndex = i + 1;
 					b = 1;
 				} else {
 					terms.add(str.substring(parStartIndex, i));
 					parStartIndex = -1;
 					b = 0;
 					normStartIndex = i + 1;
 				}
 			} else {
 				if (str.charAt(i) == ' ' && b == 0) {
 					if (normStartIndex < i) {
 						terms.add(str.substring(normStartIndex, i));
 
 					}
 
 					normStartIndex = i + 1;
 				}
 			}
 		}
 
 		if (normStartIndex < str.length()) {
 			terms.add(str.substring(normStartIndex, str.length()));
 		}
 
 		return terms;
 	}
 
 	private int edgeDensitySliderPreviousValue = -1;
 	private int edgeThresholdSliderPreviousValue = -1;
 	
 	public void stateChanged(ChangeEvent arg0) {
 
 		
 		if (arg0.getSource() == distanceSlider) {
 			// System.err.println(distanceFilterTypeCB.getSelectedItem());
 			setDistanceFilter();
 		} else if (arg0.getSource() == edgeDensitySlider) {
 			// double newVal = getEdgeSliderValue(dottedEdgeSlider);
 			if (edgeDensitySlider.getValueIsAdjusting())
 				return;
 			
 			if (edgeDensitySlider.getValue() == edgeDensitySliderPreviousValue)
 				return;
 			
 			edgeDensitySliderPreviousValue = edgeDensitySlider.getValue();
 			edgeDensity = getEdgeDensitySliderValue(edgeDensitySlider);
 			
 			if (DataModule.displayedGraph != null) {
 				DataModule.displayedGraph.ChangeEdgeDensities(edgeDensity,
 						SettingsWindow.normalEdgeRelativeMultiplier,
 						SettingsWindow.thickEdgeRelativeMultiplier);
 				System.err.println("BLEBLEBLEBLEBLE!!!!");
 				redrawGraph();
 				// visualizeGraph();
 			}
 		} else if (arg0.getSource() == edgeThresholdSlider) {
 			normalEdgeThreshold = getEdgeThresholdSliderValue(edgeThresholdSlider);
 			
 			if (edgeThresholdSlider.getValueIsAdjusting())
 				return;
 			if (edgeThresholdSliderPreviousValue == edgeThresholdSlider.getValue())
 				return;
 			
 			edgeThresholdSliderPreviousValue = edgeThresholdSlider.getValue();
 			
 			if (DataModule.displayedGraph != null) {
 				// System.err.println("bleble!!!");
 				DataModule.displayedGraph.ChangeEdgeThresholds(
 						normalEdgeThreshold,
 						SettingsWindow.dottedEdgeAbsoluteMultiplier,
 						SettingsWindow.thickEdgeAbsoluteMultiplier);
 				System.err.println("BLEBLEBLEBLEBLE!!!!");
 				redrawGraph();
 			}
 		} else if (arg0.getSource() == dottedEdgeChB
 				|| arg0.getSource() == normalEdgeChB
 				|| arg0.getSource() == thickEdgeChB) {
 			if (visu != null)
 				visu.setEdgeFilter(dottedEdgeChB.isSelected(), normalEdgeChB
 						.isSelected(), thickEdgeChB.isSelected());
 		}
 
 	}
 
 	public static void setSelectedNodesDetail(List<Functionality.Node> nodes) {
 		gui.speechDetailPanel.setText(nodes);
 	}
 
 	private void redrawGraph() {
 		if (graph_component != null) {
 			graphPanel.remove(graph_component);
 		}
 
 		graph_component = visu.actualizeGraph(lockLayout);
 		visu.setEdgeWeightStrokeFunction();
 		graphPanel.add(graph_component, BorderLayout.CENTER);
 		graphPanel.validate();
 
 		if (filterCB.getSelectedItem().equals("distance filter")) {
 			System.err.println("!!!setDistanceFilter");
 			setDistanceFilter();
 		}
 
 	}
 
 	public void componentHidden(ComponentEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void componentMoved(ComponentEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void componentResized(ComponentEvent arg0) {
 		if (arg0.getSource().equals(graph_component)) {
 			visu.layoutResize(graph_component.getWidth(), graph_component
 					.getHeight());
 		}
 		// TODO Auto-generated method stub
 
 	}
 
 	public void componentShown(ComponentEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*public void performSearch() {
 		search_button.doClick();
 	}*/
 	
 	public void performSearch(SearchQuery sq, boolean addToHistory)
 	{		
 		if (addToHistory)
 		{
 			QueryHistoryWindow.AddQueryToList(sq);
 		}
 		
 		if (sq instanceof SearchQueryStandard)
 		{
 			SearchQueryStandard sqStd = (SearchQueryStandard)sq;
 			String SinceDate = sqStd.YearFrom;
 			String TillDate =  sqStd.YearUntil;
 			String Author = "NULL";
 			String DocType = sqStd.DocType;
 			String Place = "NULL";
 			List<String> queryTerms = sqStd.QueryTerms;
 	
 			List<Double> termWeights = new ArrayList<Double>();
 	
 			if (DocType == "All") {
 				DocType = "NULL";
 			}
 	
 			for (int i = 0; i < queryTerms.size(); i++) {
 				termWeights.add(1.0);
 			}
 			
 			if (getEdgeMode() == "relative") {
 	
 				bigGraph = DataModule.getGraphDensity(SinceDate, TillDate,
 						Place, Author, DocType, queryTerms, termWeights,
 						maxNumNodes, edgeDensity,
 						SettingsWindow.normalEdgeRelativeMultiplier,
 						SettingsWindow.thickEdgeRelativeMultiplier);
 			} else {
 	
 				bigGraph = DataModule.getGraphThreshold(SinceDate,
 						TillDate, Place, Author, DocType, queryTerms,
 						termWeights, maxNumNodes, normalEdgeThreshold,
 						SettingsWindow.dottedEdgeAbsoluteMultiplier,
 						SettingsWindow.thickEdgeAbsoluteMultiplier);
 	
 			}
 			table_search.setModel(new CastroTableModel(bigGraph));
 			tableSearchSetColumnWidth();
 			visualizeGraph();
 
 			String pomS = "";
 			String term;
 			for (int i = 0; i < queryTerms.size(); i++)
 			{
 				if (i > 0) pomS += " ";
 				
 				term = queryTerms.get(i);
 				if (term.contains(" "))
 				{
 					pomS += "\"" + term + "\"";
 				}
 				else
 				{
 					pomS += term;
 				}
 				
 			}
 			
 			NE_textField.setText(pomS);
 		}
 		else
 		{
 			if (getEdgeMode() == "relative")
 			{
 				bigGraph = DataModule.getGraphNewQueryDensity((SearchQuerySimilarNodes)sq, edgeDensity, SettingsWindow.normalEdgeRelativeMultiplier, SettingsWindow.thickEdgeRelativeMultiplier);
 			}
 			else
 			{
 				bigGraph = DataModule.getGraphNewQueryThreshold((SearchQuerySimilarNodes)sq, normalEdgeThreshold, SettingsWindow.dottedEdgeAbsoluteMultiplier, SettingsWindow.thickEdgeAbsoluteMultiplier);
 			}
 			
 			table_search.setModel(new CastroTableModel(bigGraph));
 			tableSearchSetColumnWidth();
 			visualizeGraph();
 			NE_textField.setText("");
 
 		}
 		
 		currentQuery = sq;
 
 	}
 }
