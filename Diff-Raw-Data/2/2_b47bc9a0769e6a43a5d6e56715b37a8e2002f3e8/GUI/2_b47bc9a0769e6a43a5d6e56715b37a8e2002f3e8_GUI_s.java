 /*  $$$$$:  Comments by Liang
  *
  *  $$$$$$: Codes modified and/or added by Liang
  */
 
 
 package driver;
 
 import ga.GAChartOutput;
 import ga.GATracker;
 import ga.GeneticCode;
 import ga.GeneticCodeException;
 import ga.PhenotypeMaster;
 
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FileDialog;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.lang.reflect.Array;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.LookAndFeel;
 import javax.swing.SpringLayout;
 import javax.swing.SwingUtilities;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.TableColumnModel;
 
 import cobweb.TypeColorEnumeration;
 
 /**
  * Simulation configuration dialog
  * @author time itself
  *
  */
 public class GUI extends JPanel implements ActionListener {
 
 	final String TickScheduler = "cobweb.TickScheduler";
 	JTextField Width;
 	JTextField Height;
 	JCheckBox keepOldAgents;
 	JCheckBox spawnNewAgents;
 	JCheckBox keepOldArray;
 	JCheckBox dropNewFood;
 	JCheckBox ColorCodedAgents;
 	JCheckBox newColorizer;
 	JCheckBox keepOldWaste;
 	JCheckBox keepOldPackets;
 	JCheckBox wrap;
 	JCheckBox flexibility;
 	JCheckBox PrisDilemma;
 	JCheckBox FoodWeb;
 	JTextField numColor;
 	JTextField colorSelectSize;
 	JTextField reColorTimeStep;
 	JTextField colorizerMode;
 	JTextField RandomSeed;
 	JTextField randomStones;
 	JTextField maxFoodChance;
 	JTextField memory_size;
 
 	// //////////////////////////////////////////////////////////////////////////////////////////////
 
 	public static final int GA_AGENT_1_ROW = 0;
 	public static final int GA_AGENT_2_ROW = 1;
 	public static final int GA_AGENT_3_ROW = 2;
 	public static final int GA_AGENT_4_ROW = 3;
 	public static final int GA_LINKED_PHENOTYPE_ROW = 4;
 	public static final int GA_GENE_1_COL = 1;
 	public static final int GA_GENE_2_COL = 2;
 	public static final int GA_GENE_3_COL = 3;
 	public static final int GA_NUM_AGENT_TYPES = 4;
 	public static final int GA_NUM_GENES = 3;
 
 	/** The list of mutable phenotypes shown on Genetic Algorithm tab. */
 	JList mutable_phenotypes;
 
 	/** The TextFields and Buttons of the Genetic Algorithm tab. */
 	JButton link_gene_1 = new JButton("Link to Gene 1");
 
 	JButton link_gene_2 = new JButton("Link to Gene 2");
 
 	JButton link_gene_3 = new JButton("Link to Gene 3");
 
 	public static String[] meiosis_mode_list = {"Colour Averaging",
 	"Random Recombination", "Gene Swapping"};
 	public static JComboBox meiosis_mode = new JComboBox(meiosis_mode_list);
 
 	/** Default genetic bits. All genes are 00011110. */
 	public static String[][] default_genetics = {
 			{ "Agent Type 1", "00011110", "00011110", "00011110" },
 			{ "Agent Type 2", "00011110", "00011110", "00011110" },
 			{ "Agent Type 3", "00011110", "00011110", "00011110" },
 			{ "Agent Type 4", "00011110", "00011110", "00011110" },
 			{ "Linked Phenotype", "None", "None", "None" } };
 
 	public static String[] genetic_table_col_names = { "", "Gene 1", "Gene 2",
 			"Gene 3" };
 
 	/** The TextFields that store the genetic bits of the agents. */
 	public static JTable genetic_table = new JTable(default_genetics,
 			genetic_table_col_names);
 
 	/** Controls whether or not the distribution of gene status of an agent type is tracked and outputted. */
 	public static JCheckBox track_gene_status_distribution;
 
 	/** Controls whether or not the distribution of gene value of an agent type is tracked and outputted. */
 	public static JCheckBox track_gene_value_distribution;
 
 	/** The number of chart updates per time step. */
 	public static JTextField chart_update_frequency;
 
 	public void actionPerformed(java.awt.event.ActionEvent e) {
 		try {
 			if (e.getSource().equals(link_gene_1)) {
 				String linked_to_gene_1 = mutable_phenotypes.getSelectedValue()
 						.toString();
 				if (!linked_to_gene_1.equals("[No Phenotype]")) {
 					PhenotypeMaster.setLinkedAttributes(linked_to_gene_1,
 							PhenotypeMaster.RED_PHENOTYPE);
 					genetic_table.setValueAt(linked_to_gene_1,
 							GA_LINKED_PHENOTYPE_ROW, GA_GENE_1_COL);
 				} else if (!PhenotypeMaster.linked_phenotypes[PhenotypeMaster.RED_PHENOTYPE]
 						.equals("")) {
 					PhenotypeMaster
 							.removeLinkedAttributes(PhenotypeMaster.RED_PHENOTYPE);
 					genetic_table.setValueAt("None", GA_LINKED_PHENOTYPE_ROW,
 							GA_GENE_1_COL);
 				}
 			} else if (e.getSource().equals(link_gene_2)) {
 				String linked_to_gene_2 = mutable_phenotypes.getSelectedValue()
 						.toString();
 				if (!linked_to_gene_2.equals("[No Phenotype]")) {
 					PhenotypeMaster.setLinkedAttributes(linked_to_gene_2,
 							PhenotypeMaster.GREEN_PHENOTYPE);
 					genetic_table.setValueAt(linked_to_gene_2,
 							GA_LINKED_PHENOTYPE_ROW, GA_GENE_2_COL);
 				} else if (!PhenotypeMaster.linked_phenotypes[PhenotypeMaster.GREEN_PHENOTYPE]
 						.equals("")) {
 					PhenotypeMaster
 							.removeLinkedAttributes(PhenotypeMaster.GREEN_PHENOTYPE);
 					genetic_table.setValueAt("None", GA_LINKED_PHENOTYPE_ROW,
 							GA_GENE_2_COL);
 				}
 			} else if (e.getSource().equals(link_gene_3)) {
 				String linked_to_gene_3 = mutable_phenotypes.getSelectedValue()
 						.toString();
 				if (!linked_to_gene_3.equals("[No Phenotype]")) {
 					PhenotypeMaster.setLinkedAttributes(linked_to_gene_3,
 							PhenotypeMaster.BLUE_PHENOTYPE);
 					genetic_table.setValueAt(linked_to_gene_3,
 							GA_LINKED_PHENOTYPE_ROW, GA_GENE_3_COL);
 				} else if (!PhenotypeMaster.linked_phenotypes[PhenotypeMaster.BLUE_PHENOTYPE]
 						.equals("")) {
 					PhenotypeMaster
 							.removeLinkedAttributes(PhenotypeMaster.BLUE_PHENOTYPE);
 					genetic_table.setValueAt("None", GA_LINKED_PHENOTYPE_ROW,
 							GA_GENE_3_COL);
 				}
 			} else if (e.getSource().equals(meiosis_mode)) {
 
 				// Read which mode of meiosis is selected and
 				// add save it.
 				GeneticCode.meiosis_mode
 				= (String) ((JComboBox) e.getSource()).getSelectedItem();
 			} else if (e.getSource().equals(track_gene_status_distribution)) {
 				boolean state = GATracker.negateTrackGeneStatusDistribution();
 				track_gene_status_distribution.setSelected(state);
 			} else if (e.getSource().equals(track_gene_value_distribution)) {
 				boolean state = GATracker.negateTrackGeneValueDistribution();
 				track_gene_value_distribution.setSelected(state);
 			} else if (e.getSource().equals(chart_update_frequency)) {
 				try {
 					int freq = Integer.parseInt(chart_update_frequency.getText());
 
 					if (freq <= 0) {
 						chart_update_frequency.setText("Input must be > 0.");
 					} else {
 						GAChartOutput.update_frequency = freq;
 					}
 				} catch (NumberFormatException f) {
 					chart_update_frequency.setText("Input must be integer.");
 				}
 			}
 		} catch (GeneticCodeException f) {
 			// Handle Exception.
 		}
 	}
 
 	// //////////////////////////////////////////////////////////////////////////////////////////////
 
 	JTable resourceParamTable;
 
 	JTable agentParamTable;
 
 	JTable foodTable;
 
 	JTable tablePD;
 
 	JTabbedPane tabbedPane;
 
 	static JButton ok;
 
 	static JButton save;
 
 	public Object[][] inputArray;
 
 	public Object[][] foodData;
 
 	public Object[][] agentData;
 
 	public Object[][] foodwebData;
 
 	public Object[][] PDdata = { { null, null }, { null, null },
 			{ null, null }, { null, null } };
 
 	static JFrame frame;
 
 	Parser p;
 
 	private final CobwebApplication CA;
 
 	private static String datafile;
 
 	public static int numAgentTypes;
 
 	public static int numFoodTypes;
 
 	public GUI() {
 		super();
 		CA = null;
 	}
 
 	private static String[] agentParamNames = { "Initial Num. of Agents", "Mutation Rate",
 		"Initial Energy", "Favourite Food Energy", "Other Food Energy",
 		"Breed Energy", "Pregnancy Period - 1 parent",
 		"Step Energy Loss", "Step Rock Energy Loss",
 		"Turn Right Energy Loss", "Turn Left Energy Loss",
 		"Memory Bits", "Min. Communication Similarity",
 		"Step Agent Energy Loss", "Communication Bits",
 		"Pregnancy Period - 2 parents", "Min. Breed Similarity",
 		"2 parents Breed Chance", "1 parent Breed Chance",
 		"Aging Mode", "Aging Limit", "Aging Rate", "Waste Mode",
 		"Step Waste Energy Loss", "Energy gain Limit",
 		"Energy usage Limit", "Waste Half-life Rate",
 		"Initial Waste Quantity", "PD Tit for Tat",
 		"PD Cooperation Probability", "Broadcast Mode",
 		"Broadcast range energy-based", "Broadcast fixed range",
 		"Broadcast Minimum Energy", "Broadcast Energy Cost" };
 
 	private static String[] PDrownames = { "Temptation", "Reward", "Punishment",
 		"Sucker's Payoff" };
 
 	// GUI Special Constructor
 	public GUI(CobwebApplication ca, String filename) {
 		super();
 		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 
 		CA = ca;
 		datafile = filename;
 		tabbedPane = new JTabbedPane();
 
 		/* Environment panel - composed of 4 panels */
 
 		JComponent envPanel = setupEnvPanel();
 
 
 		/*
 		 * check filename, if file name exists and has a correct format set the
 		 * values from the file filename
 		 */
 
 		controllerPanel = new AIPanel();
 
 
 		File f = new File(datafile);
 
 		if (f.exists()) {
 			try {
 				p = new Parser(datafile);
 				loadfromParser(p);
 			} catch (Throwable e) {
 				e.printStackTrace();
 				setDefault();
 			}
 		} else {
 			setDefault();
 		}
 
 
 		tabbedPane = new JTabbedPane();
 
 		tabbedPane.addTab("Environment", envPanel);
 
 		/* Resources panel */
 		JComponent resourcePanel = setupResourcePanel();
 		tabbedPane.addTab("Resources", resourcePanel);
 
 		/* Agents' panel */
 		JComponent agentPanel = setupAgentPanel();
 		tabbedPane.addTab("Agents", agentPanel);
 
 		JComponent foodPanel = setupFoodPanel();
 		tabbedPane.addTab("Food Web", foodPanel);
 
 		JComponent panelPD = setupPDpannel();
 		tabbedPane.addTab("PD Options", panelPD);
 
 		JComponent panelGA = setupGApannel();
 		tabbedPane.addTab("Genetic Algorithm", panelGA);
 
 		tabbedPane.addTab("AI", controllerPanel);
 
 
 		ok = new JButton("OK");
 		ok.setMaximumSize(new Dimension(80, 20));
 		ok.addActionListener(new OkButtonListener());
 
 		save = new JButton("Save As...");
 		save.setMaximumSize(new Dimension(80, 20));
 		save.addActionListener(new SaveAsButtonListener());
 
 		JPanel buttons = new JPanel();
 		buttons.setLayout(new FlowLayout(FlowLayout.RIGHT));
 		buttons.add(save);
 		buttons.add(ok);
 
 		// Add the tabbed pane to this panel.
 		add(tabbedPane, BorderLayout.CENTER);
 		add(buttons, BorderLayout.SOUTH);
 		this.setPreferredSize(new Dimension(700, 570));
 	}
 
 	SettingsPanel controllerPanel;
 
 
 	private static final String[] AI_LIST = {
 		"cwcore.GeneticController",
 		"cwcore.LinearWeightsController"
 	};
 
 	private class AIPanel extends SettingsPanel {
 		/**
 		 *
 		 */
 		private static final long serialVersionUID = 6045306756522429063L;
 
 
 		CardLayout cl = new CardLayout();
 		JPanel inner = new JPanel();
 
 		JComboBox aiSwitch;
 
 
 
 		public AIPanel() {
 			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 			inner.setLayout(cl);
 
 			SettingsPanel GenPanel = new SettingsPanel() {
 				/**
 				 *
 				 */
 				private static final long serialVersionUID = 1139521733160862828L;
 
 				{
 					this.add(new JLabel("Nothing to configure"));
 				}
 
 				@Override
 				public void readFromParser(Parser p) {
 				}
 
 				@Override
 				public void writeXML(Writer out) throws IOException {
 				}
 
 			};
 			inner.add(GenPanel, AI_LIST[0]);
 
 			SettingsPanel LWpanel = new LinearAIGUI();
 			inner.add(LWpanel, AI_LIST[1]);
 
 			aiSwitch = new JComboBox(AI_LIST);
 			aiSwitch.setEditable(false);
 			aiSwitch.addItemListener(new ItemListener() {
 				public void itemStateChanged(ItemEvent e) {
 					cl.show(inner, (String)e.getItem());
 				}
 			});
 
 			add(aiSwitch);
 			add(inner);
 		}
 
 		@Override
 		public void readFromParser(Parser p) {
 			aiSwitch.setSelectedItem(p.ControllerName);
 			((SettingsPanel)inner.getComponent(aiSwitch.getSelectedIndex())).readFromParser(p);
 		}
 
 		@Override
 		public void writeXML(Writer out) throws IOException {
 			out.write("\t<ControllerName>" + aiSwitch.getSelectedItem() + "</ControllerName>\n");
 			out.write("\t<ControllerConfig>");
 			SettingsPanel contConf = (SettingsPanel)inner.getComponent(aiSwitch.getSelectedIndex());
 			contConf.writeXML(out);
 			out.write("</ControllerConfig>\n");
 		}
 
 
 	}
 
 
 	private JComponent setupEnvPanel() {
 		JComponent envPanel = new JPanel(new GridLayout(3, 2));
 
 		/* Environment Settings */
 		JPanel panel11 = new JPanel();
 		makeGroupPanel(panel11, "Grid Settings");
 		JPanel fieldPane = new JPanel();
 
 		fieldPane.add(new JLabel("Width"));
 		fieldPane.add(Width = new JTextField(3));
 
 		fieldPane.add(new JLabel("Height"));
 		fieldPane.add(Height = new JTextField(3));
 
 		fieldPane.add(new JLabel("Wrap"));
 		fieldPane.add(wrap = new JCheckBox(""));
 
 		panel11.add(fieldPane);
 
 		makeOptionsTable(fieldPane, 3);
 
 
 		envPanel.add(panel11);
 
 		JPanel panel15 = new JPanel();
 		makeGroupPanel(panel15, "Prisoner's Dilemma Options");
 
 		PrisDilemma = new JCheckBox("");
 		memory_size = new JTextField(3);
 		flexibility = new JCheckBox("");
 		fieldPane = new JPanel(new GridLayout(3, 1));
 
 		fieldPane.add(new JLabel("Prisoner's Game"));
 		fieldPane.add(PrisDilemma);
 
 		fieldPane.add(new JLabel("Memory Size"));
 		fieldPane.add(memory_size);
 
 		fieldPane.add(new JLabel("Energy Based"));
 		fieldPane.add(flexibility);
 
 		makeOptionsTable(fieldPane, 3);
 
 
 		panel15.add(fieldPane);
 		envPanel.add(panel15, "WEST");
 
 		/* Colour Settings */
 		JPanel panel12 = new JPanel();
 		//panel12.setLayout(new BoxLayout(panel12, BoxLayout.Y_AXIS));
 		makeGroupPanel(panel12, "Environment Transition Settings");
 
 		fieldPane = new JPanel();
 
 		fieldPane.add(new JLabel("Keep Old Agents"));
 		fieldPane.add(keepOldAgents = new JCheckBox(""));
 
 		fieldPane.add(new JLabel("Spawn New Agents"));
 		fieldPane.add(spawnNewAgents = new JCheckBox(""));
 
 		fieldPane.add(new JLabel("Keep Old Array"));
 		fieldPane.add(keepOldArray = new JCheckBox(""));
 
 		fieldPane.add(new JLabel("New Colorizer"));
 		fieldPane.add(newColorizer = new JCheckBox("", true));
 
 		fieldPane.add(new JLabel("Keep Old Waste"));
 		fieldPane.add(keepOldWaste = new JCheckBox("", true));
 
 		fieldPane.add(new JLabel("Keep Old Packets"));
 		fieldPane.add(keepOldPackets = new JCheckBox("", true));
 		makeOptionsTable(fieldPane, 6);
 
 		ColorCodedAgents = new JCheckBox("");
 
 		panel12.add(fieldPane);
 		envPanel.add(panel12);
 
 		/* Options */
 		JPanel panel13 = new JPanel();
 		String title = "Colour Settings";
 		makeGroupPanel(panel13, title);
 
 		fieldPane = new JPanel(new GridLayout(5, 1));
 
 		numColor = new JTextField(3);
 		colorSelectSize = new JTextField(3);
 		reColorTimeStep = new JTextField(3);
 		colorizerMode = new JTextField(3);
 
 		fieldPane.add(new JLabel("No. of Colors"));
 		fieldPane.add(numColor);
 		fieldPane.add(new JLabel("Color Select Size"));
 		fieldPane.add(colorSelectSize);
 		fieldPane.add(new JLabel("Recolor Time Step"));
 		fieldPane.add(reColorTimeStep);
 		fieldPane.add(new JLabel("Colorizer Mode"));
 		fieldPane.add(colorizerMode);
 		fieldPane.add(new JLabel("Color Coded Agents"));
 		fieldPane.add(ColorCodedAgents);
 
 		panel13.add(fieldPane);
 		makeOptionsTable(fieldPane, 5);
 
 		envPanel.add(panel13);
 
 		/* Random variables */
 		JPanel panel14 = new JPanel();
 		makeGroupPanel(panel14, "Random Variables");
 		fieldPane = new JPanel(new GridLayout(2, 1));
 
 		RandomSeed = new JTextField(3);
 		randomStones = new JTextField(3);
 
 		fieldPane.add(new JLabel("Random Seed"));
 		fieldPane.add(RandomSeed);
 		fieldPane.add(new JLabel("Random Stones no."));
 		fieldPane.add(randomStones);
 
 		panel14.add(fieldPane, BorderLayout.EAST);
 		makeOptionsTable(fieldPane, 2);
 
 		envPanel.add(panel14);
 
 		JPanel panel16 = new JPanel();
 		makeGroupPanel(panel16, "General Food Variables");
 
 		dropNewFood = new JCheckBox("");
 		maxFoodChance = new JTextField(3);
 
 		fieldPane = new JPanel(new GridLayout(2, 1));
 		fieldPane.add(new JLabel("Drop New Food"));
 		fieldPane.add(dropNewFood);
 
 		//UNUSED: See ComplexEnvironment.growFood()
 		//fieldPane.add(new JLabel("Max Food Chance"));
 		//fieldPane.add(maxFoodChance);
 
 		panel16.add(fieldPane, BorderLayout.EAST);
 		makeOptionsTable(fieldPane, 1);
 
 		envPanel.add(panel16);
 		return envPanel;
 	}
 
 
 	private void makeOptionsTable(JPanel fieldPane, int items) {
 		fieldPane.setLayout(new SpringLayout());
 		SpringUtilities.makeCompactGrid(fieldPane, items, 2, 0, 0, 8, 0);
 	}
 
 
 	private void makeGroupPanel(JComponent target, String title) {
 		target.setBorder(BorderFactory.createTitledBorder(BorderFactory
 				.createLineBorder(Color.blue), title));
 	}
 
 	private static String[] resParamNames = { "Initial Food Amount", "Food Rate",
 		"Growth Rate", "Depletion Rate", "Depletion Steps",
 		"Draught period", "Food Mode" };
 
 	private JComponent setupResourcePanel() {
 
 		JComponent resourcePanel = new JPanel();
 		resourceParamTable = new JTable(new MyTableModel(resParamNames, foodData.length, foodData));
 		TableColumnModel colModel = resourceParamTable.getColumnModel();
 		// Get the column at index pColumn, and set its preferred width.
 		colModel.getColumn(0).setPreferredWidth(120);
 		System.out.println(colModel.getColumn(0).getHeaderValue());
 
 		colorHeaders(resourceParamTable, 1);
 
 		JScrollPane resourceScroll = new JScrollPane(resourceParamTable);
 		resourcePanel.setLayout(new BoxLayout(resourcePanel, BoxLayout.X_AXIS));
 		makeGroupPanel(resourcePanel, "Resource Parameters");
 		resourcePanel.add(resourceScroll);
 		return resourcePanel;
 	}
 
 
 	private JComponent setupAgentPanel() {
 		JComponent agentPanel = new JPanel();
 		agentPanel.setLayout(new BoxLayout(agentPanel, BoxLayout.X_AXIS));
 		makeGroupPanel(agentPanel, "Agent Parameters");
 
 		agentParamTable = new JTable(new MyTableModel(agentParamNames, agentData.length, agentData));
 
 		TableColumnModel agParamColModel = agentParamTable.getColumnModel();
 		// Get the column at index pColumn, and set its preferred width.
 		agParamColModel.getColumn(0).setPreferredWidth(200);
 
 		colorHeaders(agentParamTable, 1);
 
 		JScrollPane agentScroll = new JScrollPane(agentParamTable);
 		// Add the scroll pane to this panel.
 		agentPanel.add(agentScroll);
 		return agentPanel;
 	}
 
 
 	private JComponent setupFoodPanel() {
 		JComponent foodPanel = new JPanel();
 		// tabbedPane.addTab("Agents", panel3);
 
 		String[] foodNames = new String[numAgentTypes + numFoodTypes];
 		for (int i = 0; i < numAgentTypes; i++) {
 			foodNames[i] = "Agent " + i;
 		}
 		for (int i = 0; i < numFoodTypes; i++) {
 			foodNames[i + numAgentTypes] = "Food " + i;
 		}
 
 		foodTable = new JTable(new MyTableModel2(foodNames, foodwebData.length, foodwebData));
 
 		colorHeaders(foodTable, 1);
 
 		// Create the scroll pane and add the table to it.
 		JScrollPane foodScroll = new JScrollPane(foodTable);
 
 		foodPanel.setLayout(new BoxLayout(foodPanel, BoxLayout.X_AXIS));
 		makeGroupPanel(foodPanel, "Food Parameters");
 		foodPanel.add(foodScroll);
 		return foodPanel;
 	}
 
 
 	private void colorHeaders(JTable ft, int shift) {
 		TypeColorEnumeration tc = TypeColorEnumeration.getInstance();
 		for (int t = 0; t < numAgentTypes; t++) {
 			DefaultTableCellRenderer r = new DefaultTableCellRenderer();
 			r.setBackground(tc.getColor(t, 0));
 			ft.getColumnModel().getColumn(t + shift).setHeaderRenderer(r);
 			LookAndFeel.installBorder(ft.getTableHeader(), "TableHeader.cellBorder");
 		}
 	}
 
 
 	private JComponent setupPDpannel() {
 		JComponent panelPD = new JPanel();
 
 		tablePD = new JTable(new PDTable(PDrownames, PDdata));
 		//tablePD.setPreferredScrollableViewportSize(new Dimension(800, 300));
 		JScrollPane scrollPanePD = new JScrollPane(tablePD);
 		// Create the scroll pane and add the table to it.
 		//scrollPanePD.setPreferredSize(new Dimension(400, 150));
 		//tablePD.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 		//(tablePD.getColumnModel()).getColumn(0).setPreferredWidth(200);
 
 		panelPD.add(scrollPanePD, BorderLayout.CENTER);
 
 		panelPD.setLayout(new BoxLayout(panelPD, BoxLayout.X_AXIS));
 		makeGroupPanel(panelPD, "Prisoner's Dilemma Parameters");
 		return panelPD;
 	}
 
 
 	private JComponent setupGApannel() {
 		JComponent panelGA = new JPanel();
 		panelGA.setLayout(new BoxLayout(panelGA, BoxLayout.Y_AXIS));
 		DefaultListModel mutable_list_model = new DefaultListModel();
 		for (String element : PhenotypeMaster.mutable) {
 			mutable_list_model.addElement(element);
 		}
 		mutable_list_model.addElement("[No Phenotype]");
 		mutable_phenotypes = new JList(mutable_list_model);
 		mutable_phenotypes
 				.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
 		mutable_phenotypes.setLayoutOrientation(JList.VERTICAL_WRAP);
 		mutable_phenotypes.setVisibleRowCount(-1);
 		JScrollPane phenotypeScroller = new JScrollPane(mutable_phenotypes);
 		phenotypeScroller.setPreferredSize(new Dimension(150, 200));
 
 		// Set the default selected item as the previously selected item.
 		meiosis_mode.setSelectedIndex(GeneticCode.meiosis_mode_index);
 
 		JPanel gene_1 = new JPanel();
 		gene_1.add(link_gene_1);
 
 		JPanel gene_2 = new JPanel();
 		gene_2.add(link_gene_2);
 
 		JPanel gene_3 = new JPanel();
 		gene_3.add(link_gene_3);
 
 		JPanel gene_info_display = new JPanel(new BorderLayout());
 
 		gene_info_display.add(gene_1, BorderLayout.WEST);
 		gene_info_display.add(gene_2, BorderLayout.CENTER);
 		gene_info_display.add(gene_3, BorderLayout.EAST);
 		JPanel meiosis_mode_panel = new JPanel(new BorderLayout());
 		meiosis_mode_panel.add(new JLabel("Mode of Meiosis"), BorderLayout.NORTH);
 		meiosis_mode_panel.add(meiosis_mode, BorderLayout.CENTER);
 
 		// Checkboxes and TextAreas
 		track_gene_status_distribution = new JCheckBox("Track Gene Status Distribution", GATracker.getTrackGeneStatusDistribution());
 		track_gene_value_distribution = new JCheckBox("Track Gene Value Distribution", GATracker.getTrackGeneValueDistribution());
 		chart_update_frequency = new JTextField(GAChartOutput.update_frequency + "", 12);
 		JPanel chart_update_frequency_panel = new JPanel();
 		chart_update_frequency_panel.add(new JLabel("Time Steps per Chart Update"));
 		chart_update_frequency_panel.add(chart_update_frequency);
 		JPanel gene_check_boxes = new JPanel(new BorderLayout());
 		gene_check_boxes.add(track_gene_status_distribution, BorderLayout.NORTH);
 		gene_check_boxes.add(track_gene_value_distribution, BorderLayout.CENTER);
 		gene_check_boxes.add(chart_update_frequency_panel, BorderLayout.SOUTH);
 
 		// Combine Checkboxes and Dropdown menu
 		JPanel ga_combined_panel = new JPanel(new BorderLayout());
 		ga_combined_panel.add(meiosis_mode_panel, BorderLayout.EAST);
 		ga_combined_panel.add(gene_check_boxes, BorderLayout.WEST);
 
 		gene_info_display.add(ga_combined_panel, BorderLayout.SOUTH);
 
 		genetic_table.setPreferredScrollableViewportSize(new Dimension(150, 160));
 		genetic_table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
 
 		makeGroupPanel(phenotypeScroller, "Agent Parameter Selection");
 
 		panelGA.add(phenotypeScroller);
 
 		JScrollPane geneScroll = new JScrollPane(genetic_table);
 
 		makeGroupPanel(geneScroll, "Gene Bindings");
 
 		panelGA.add(geneScroll);
 
 		panelGA.add(gene_info_display);
 
 		/** Listeners of JButtons, JComboBoxes, and JCheckBoxes */
 		link_gene_1.addActionListener(this);
 		link_gene_2.addActionListener(this);
 		link_gene_3.addActionListener(this);
     	meiosis_mode.addActionListener(this);
     	track_gene_status_distribution.addActionListener(this);
     	track_gene_value_distribution.addActionListener(this);
     	chart_update_frequency.addActionListener(this);
 
     	makeGroupPanel(panelGA, "Genetic Algorithm Parameters");
 		return panelGA;
 	}
 
 	// $$$$$$  Check the validity of genetic_table input, used by "OK" and "Save" buttons.  Feb 1
 	private boolean checkValidityOfGAInput() {
 		Pattern pattern = Pattern.compile("^[01]{8}$");
 		Matcher matcher;
 
 		boolean correct_input = true;
 
 		// $$$$$$  added on Jan 23  $$$$$  not perfect since the dialog won't popup until hit "OK"
 		updateTable(genetic_table);
 
 		for (int i = GA_AGENT_1_ROW; i < GA_AGENT_1_ROW + GA_NUM_AGENT_TYPES; i++) {
 			for (int j = GA_GENE_1_COL; j < GA_GENE_1_COL + GA_NUM_GENES; j++) {
 				matcher = pattern.matcher((String) genetic_table.getValueAt(i,j));
 				if (!matcher.find()) {
 					correct_input = false;
 					JOptionPane.showMessageDialog(GUI.this,
 						"GA: All genes must be binary and 8-bit long");
 					break;
 				}
 			}
 			if (!correct_input) {
 				break;
 			}
 		}
 
 		return correct_input;
 	}
 
 	/**************** Rendered defunct by Andy, because the pop up is annoying. */
 	// $$$$$$ To check whether RandomSeed == 0. If RandomSeed != 0, popup a message.  Apr 18 [Feb 18] Refer class ComplexEnvironment line 365-6 for the reason
 	private int checkRandomSeedStatus() {
 		/*
 		if ( Integer.parseInt(RandomSeed.getText()) != 0) {   // $$$$$$ change from "==".  Apr 18
 			//JOptionPane.showMessageDialog(GUI.frame,
 			//	"CAUTION:  \"Random Seed\" is setting to zero.\n" +
 			//	"\nFor retrievable experiments, please set \"Random Seed\" to non-zero.");
 			// $$$$$$ Change the above block as follows and return an integer.  Feb 25
 			Object[] options = {"Yes, please",
 								"No, thanks"};
 			int n = JOptionPane.showOptionDialog(frame,
 													"Note:  You can set \"Random Seed\" to zero for non-repeatable experiments.\n" +
 													"\nDo you want to be reminded next time?",
 													"Random Seed Setting",
 													JOptionPane.YES_NO_OPTION,
 													JOptionPane.QUESTION_MESSAGE,
 													null,     //do not use a custom Icon
 													options,  //the titles of buttons
 													options[0]); //default button title
 			return n;
 		} else {
 			return 0;
 		}*/
 		return 0;
 	}
 
 	// $$$$$$ check if Width >= Height, for if Height > Width, an exception will occur and cobweb2 will malfunction.  Feb 20
 	private boolean checkHeightLessThanWidth(){
 		if ( Integer.parseInt(Width.getText()) < Integer.parseInt(Height.getText()) ) {
 			JOptionPane.showMessageDialog(GUI.this,
 					"Please set Width >= Height for Grid Settings, or Cobweb2 would malfunction.",
 					"Warning", JOptionPane.WARNING_MESSAGE);
 			return false;
 		} else {
 			return true;
 		}
 	}
 
 
 	public void updateTable(JTable table) {
 		int row = table.getEditingRow();
 		int col = table.getEditingColumn();
 		if (table.isEditing()) {
 			table.getCellEditor(row, col).stopCellEditing();
 		}
 	}
 
 	// $$$$$$ This openFileDialog method is invoked by pressing the "Save" button
 	public void openFileDialog() {
 		FileDialog theDialog = new FileDialog(frame,
 				"Choose a file to save state to", java.awt.FileDialog.SAVE);
 		theDialog.setVisible(true);
 		if (theDialog.getFile() != null) {
 			// $$$$$$ Check if the saving filename is one of the names reserved by CobwebApplication.  Feb 8
 			// $$$$$$$$$$$$$$$$$$$$$$$$$$ Block silenced by Andy due to the annoyingness of this feature. May 7, 2008
 			//String savingFileName;
 			//savingFileName = theDialog.getFile();
 
 			// Block silenced, see above.
 
 			/*if ( (savingFileName.contains(CobwebApplication.INITIAL_OR_NEW_INPUT_FILE_NAME) != false)
 					  || (savingFileName.contains(CobwebApplication.CURRENT_DATA_FILE_NAME) != false) //$$$$$ added for "Modify Current Data"
 					  || (savingFileName.contains(CobwebApplication.DEFAULT_DATA_FILE_NAME) != false)) {
 
 
 				JOptionPane.showMessageDialog(GUI.this,
 				"Save State: The filename\"" + savingFileName + "\" is reserved by Cobweb Application.\n" +
 						"                       Please choose another file to save.",
 						"Warning", JOptionPane.WARNING_MESSAGE); // $$$$$$ modified on Feb 22
 				openFileDialog();
 			} else { */  // $$$$$ If filename not reserved.  Feb 8
 				try {
 					// $$$$$$ The following block added to handle a readonly file.  Feb 22
 					String savingFile = theDialog.getDirectory() + theDialog.getFile();
 					File sf = new File(savingFile);
 					if ( (sf.isHidden() != false) || ((sf.exists() != false) && (sf.canWrite() == false)) ) {
 						JOptionPane.showMessageDialog(GUI.frame,   // $$$$$$ change from "this" to "GUI.frame".  Feb 22
 								"Caution:  File \"" + savingFile + "\" is NOT allowed to be written to.",
 								"Warning", JOptionPane.WARNING_MESSAGE);
 					} else {
 						// $$$$$ The following block used to be the original code.  Feb 22
 						write(theDialog.getDirectory() + theDialog.getFile());
 						p = new Parser(theDialog.getDirectory() + theDialog.getFile());
 						CA.openFile(p);
 						if (!datafile.equals(CA.getCurrentFile())) {CA.setCurrentFile(datafile);}  // $$$$$$ added on Mar 14
 						frame.setVisible(false);
 						frame.dispose(); // $$$$$$ Feb 28
 						// $$$$$$ Added on Mar 14
 						if (CA.getUI() != null) {
 							if(CA.isInvokedByModify() == false) {
 								CA.getUI().reset();    // reset tick
 								//CA.refresh(CA.getUI());
 								//if (CA.tickField != null && !CA.tickField.getText().equals("")) {CA.tickField.setText("");}    // $$$$$$ Mar 17
 							}
 							CA.getUI().refresh(1);
 
 							/*** $$$$$$ Cancel textWindow  Apr 22*/
 							if (cobweb.globals.usingTextWindow == true) {
 								// $$$$$$ Reset the output window, specially for Linux.  Mar 29
 								if (CA.textArea.getText().endsWith(CobwebApplication.GREETINGS) == false) {
 									CA.textArea.setText(CobwebApplication.GREETINGS);
 								}
 							}
 
 						}
 					}
 				} 	catch (java.io.IOException evt) {
 					JOptionPane.showMessageDialog(CA,  // $$$$$$ added on Apr 22
 							"Save failed: " + evt.getMessage(),
 							"Warning", JOptionPane.WARNING_MESSAGE);
 					/*** $$$$$$ Cancel textWindow  Apr 22*/
 					if (cobweb.globals.usingTextWindow == true) {CA.textArea.append("Save failed:" + evt.getMessage());} // $$$$$$ Added to be consistent with
 																												// CobwebApplication's saveFile method.  Feb 8
 				}
 			// }
 		}
 	}
 
 	public Parser getParser() {
 		return p;
 	}
 
 	private final class SaveAsButtonListener implements
 			java.awt.event.ActionListener {
 		public void actionPerformed(java.awt.event.ActionEvent e) {
 			// $$$$$$ check validity of genetic table input.  Feb 1
 
 			// Save the chart update frequency
 			try {
 				int freq = Integer.parseInt(chart_update_frequency.getText());
 				if (freq <= 0) { // Non-positive integers
 					GAChartOutput.update_frequency = 1;
 				} else { // Valid input
 					GAChartOutput.update_frequency = freq;
 				}
 			} catch (NumberFormatException f) { // Invalid input
 				GAChartOutput.update_frequency = 1;
 			}
 
 			boolean correct_GA_input;
 			correct_GA_input = checkValidityOfGAInput();
 
 			// $$$$$$ Implement "Save" only if GA input is correct
 			if ( (checkHeightLessThanWidth() != false) && (correct_GA_input != false) ) { // modified on Feb 21
 
 				//checkRandomSeedValidity();	// $$$$$$ added on Feb 22
 				// $$$$$$ Change the above code as follows on Feb 25
 				if (CA.randomSeedReminder == 0) {
 					CA.randomSeedReminder = checkRandomSeedStatus();
 				}
 
 				updateTable(resourceParamTable);
 				updateTable(agentParamTable);
 				updateTable(foodTable);
 				updateTable(tablePD); // $$$$$$ Jan 25
 				//updateTable(genetic_table);  // $$$$$$ Jan 25 $$$$$$ genetic_table already updated by checkValidityOfGAInput(). Feb 22
 				openFileDialog();
 
 
 
 			}
 		}
 	}
 
 	private final class OkButtonListener implements ActionListener {
 		public void actionPerformed(java.awt.event.ActionEvent evt) {
 
 			// $$$$$$ check validity of genetic table input.  Feb 1
 			boolean correct_GA_input;
 			correct_GA_input = checkValidityOfGAInput();
 
 			// Save the chart update frequency
 			try {
 				int freq = Integer.parseInt(chart_update_frequency.getText());
 				if (freq <= 0) { // Non-positive integers
 					GAChartOutput.update_frequency = 1;
 				} else { // Valid input
 					GAChartOutput.update_frequency = freq;
 				}
 			} catch (NumberFormatException e) { // Invalid input
 				GAChartOutput.update_frequency = 1;
 			}
 
 			if ( (checkHeightLessThanWidth() != false) && (correct_GA_input != false) ) {
 
 				//checkRandomSeedValidity(); // $$$$$$ added on Feb 18
 				// $$$$$$ Change the above code as follows on Feb 25
 				if (CA.randomSeedReminder == 0) {
 					CA.randomSeedReminder = checkRandomSeedStatus();
 				}
 				/*
 				 * this fragment of code is necessary to update the last cell of
 				 * the table before saving it
 				 */
 				updateTable(resourceParamTable);
 				updateTable(agentParamTable);
 				updateTable(foodTable);
 				updateTable(tablePD); // $$$$$$ Jan 25
 
 				/* write UI info to xml file */
 				try {
 					write(datafile);  // $$$$$ write attributes showed in the "Test Data" window into the file "datafile".   Jan 24
 				} catch (java.io.IOException e) {
 					throw new RuntimeException(e);
 				}
 
 				/* create a new parser for the xml file */
 				try {
 					p = new Parser(datafile);
 				} catch (FileNotFoundException ex) {
 					// TODO Auto-generated catch block
 					ex.printStackTrace();
 				}
 				CA.openFile(p);
 				if (!datafile.equals(CA.getCurrentFile())) {CA.setCurrentFile(datafile);}  // $$$$$$ added on Mar 14
 				frame.setVisible(false);
 				frame.dispose(); // $$$$$$ added on Feb 28
 				// $$$$$$ Added on Mar 14
 				if (CA.getUI() != null) {
 					if(CA.isInvokedByModify() == false) {
 						CA.getUI().reset();   // reset tick
 						//CA.refresh(CA.getUI());
 						//if (CA.tickField != null && !CA.tickField.getText().equals("")) {CA.tickField.setText("");}    // $$$$$$ Mar 17
 					}
 					CA.getUI().refresh(1);
 
 					/*** $$$$$$ Cancel textWindow  Apr 22*/
 					if (cobweb.globals.usingTextWindow == true) {
 						// $$$$$$ Reset the output window, specially for Linux.  Mar 29
 						if (CA.textArea.getText().endsWith(CobwebApplication.GREETINGS) == false) {
 							CA.textArea.setText(CobwebApplication.GREETINGS);
 						}
 					}
 
 				}
 			}
 		}
 	}
 
 	class PDTable extends AbstractTableModel {
 		private final Object[][] values;
 
 		private final String[] rownames;
 
 		public PDTable(String rownames[], Object data[][]) {
 			this.rownames = rownames;
 			values = data;
 		}
 
 		public int getRowCount() {
 			return values.length;
 		}
 
 		public int getColumnCount() {
 			return 2;
 		}
 
 		@Override
 		public String getColumnName(int column) {
 			if (column == 0) {
 				return "";
 			}
 			return "value";
 		}
 
 		public String getRowName(int row) {
 			return rownames[row];
 		}
 
 		public Object getValueAt(int row, int column) {
 			if (column == 0) {
 				return rownames[row];
 			}
 			return values[row][0];
 		}
 
 		@Override
 		public boolean isCellEditable(int row, int col) {
 			// Note that the data/cell address is constant,
 			// no matter where the cell appears onscreen.
 			if (col == 0) {
 				return false;
 			} else {
 				return true;
 			}
 		}
 
 		@Override
 		public void setValueAt(Object value, int row, int col) {
 			if ((isCellEditable(row, col))) {
 				try {
 					values[row][0] = new Integer((String) value);
 				} catch (NumberFormatException e) {
 					if (SwingUtilities.isEventDispatchThread()) {
 						JOptionPane.showMessageDialog(GUI.this, "The \""
 								+ getRowName(row)
 								+ "\" row only accepts integer values.");
 					} else {
 						System.err
 								.println("User attempted to enter non-integer"
 										+ " value (" + value
 										+ ") into an integer-only column.");
 					}
 				}
 			}
 		}
 		// public Class getColumnClass(int c) { return values[0].getClass();}
 
 		public static final long serialVersionUID = 0x38FAF24EC6162F2CL;
 	}
 
 	/* table class */
 	class MyTableModel extends AbstractTableModel {
 
 		private final Object[][] data;
 
 		private final String[] rowNames;
 
 		private int numTypes = 0;
 
 		MyTableModel(String rownames[], int numcol, Object data[][]) {
 			this.data = data;
 			rowNames = rownames;
 			numTypes = numcol;
 		}
 
 		/* return the number of columns */
 		public int getColumnCount() {
 			return numTypes + 1;
 		}
 
 		/* return the number of rows */
 		public int getRowCount() {
 			return rowNames.length;
 		}
 
 		/* return column name given the number of the column */
 		@Override
 		public String getColumnName(int col) {
 			if (col == 0) {
 				return "";
 			} else {
 				return "Type" + (col);
 			}
 		}
 
 		/* return row name given the number of the row */
 		public String getRowName(int row) {
 			return rowNames[row];
 		}
 
 		public Object getValueAt(int row, int col) {
 			if (col == 0) {
 				return rowNames[row];
 			}
 			return data[col - 1][row];
 		}
 
 		/* add a column to this table */
 		public void addColumn() {
 			numTypes++;
 			for (int i = 0; i < getRowCount(); i++) {
 				data[numTypes][i] = "0";
 			}
 		}
 
 		/*
 		 * Don't need to implement this method unless your table's editable.
 		 */
 		@Override
 		public boolean isCellEditable(int row, int col) {
 			// Note that the data/cell address is constant,
 			// no matter where the cell appears onscreen.
 			if (col == 0) {
 				return false;
 			} else {
 				return true;
 			}
 		}
 
 		/*
 		 * set the value at (row,col)
 		 */
 		@Override
 		public void setValueAt(Object value, int row, int col) {
 
 			if ((isCellEditable(row, col))) {
 				// check if this cell is supposed to contain and integer
 				if (data[col - 1][row] instanceof Integer) {
 					// If we don't do something like this, the column
 					// switches to contain Strings.
 					try {
 						data[col - 1][row] = new Integer((String) value);
 
 					} catch (NumberFormatException e) {
 						if (SwingUtilities.isEventDispatchThread()) {
 							JOptionPane.showMessageDialog(GUI.this, "The \""
 									+ getRowName(row)
 									+ "\" row only accepts integer values.");
 						} else {
 							System.err
 									.println("User attempted to enter non-integer"
 											+ " value ("
 											+ value
 											+ ") into an integer-only column.");
 						}
 					}
 					// check if this cell is supposed to contain float or double
 				} else if ((data[col - 1][row] instanceof Double)
 						|| (data[col - 1][row] instanceof Float)) {
 					try {
 						data[col - 1][row] = new Float((String) value);
 
 					} catch (NumberFormatException e) {
 						if (SwingUtilities.isEventDispatchThread()) {
 							JOptionPane
 									.showMessageDialog(
 											GUI.this,
 											"The \""
 													+ getRowName(row)
 													+ "\" row only accepts float or double values.");
 						} else {
 							System.err
 									.println("User attempted to enter non-float"
 											+ " value ("
 											+ value
 											+ ") into an float-only column.");
 						}
 					}
 				} else {
 					data[col - 1][row] = value;
 				}
 				printDebugData();
 
 			}
 		}
 
 		// print the data from the table each time it gets updated (used for
 		// testing)
 
 		private void printDebugData() {
 			/*
 			 * int numRows = getRowCount(); int numCols = getColumnCount();
 			 *
 			 * for (int i=0; i < numRows; i++) { System.out.print(" row " + i +
 			 * ":"); for (int j=0; j < numCols-1; j++) { System.out.print(" " +
 			 * data[j][i]); } System.out.println(); }
 			 * System.out.println("--------------------------");
 			 */
 		}
 
 		public static final long serialVersionUID = 0x38DC79AEAD8B2091L;
 	}
 
 	/* extends MyTableModel, implements the checkboxes in the food web class */
 	class MyTableModel2 extends MyTableModel {
 		@SuppressWarnings("unused")
 		private Object[][] data;
 
 		@SuppressWarnings("unused")
 		private String[] rowNames;
 
 		MyTableModel2(String rownames[], int numcol, Object data[][]) {
 			super(rownames, numcol, data);
 		}
 
 		@Override
 		public Class<?> getColumnClass(int c) {
 			return getValueAt(0, c).getClass();
 		}
 
 		public static final long serialVersionUID = 0x6E1D565A6F6714AFL;
 	}
 
 	/**
 	 * Create the GUI and show it. For thread safety, this method should be
 	 * invoked from the event-dispatching thread.
 	 */
 	public static void createAndShowGUI(CobwebApplication ca, String filename) {
 		// Make sure we have nice window decorations.
 		//JFrame.setDefaultLookAndFeelDecorated(true);
 
 		// Create and set up the window.
 		frame = new JFrame("Test Data");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		// Create and set up the content pane.
 
 		JComponent newContentPane = new GUI(ca, filename);
 		frame.add(newContentPane);
 		// Display the window.
 		frame.pack();
 		frame.setVisible(true);
 		frame.getRootPane().setDefaultButton(ok);
 		//frame.validate();
 	}
 
 	public void setDefault() {
 		Width.setText("20");
 		wrap.setSelected(false);
 		Height.setText(Width.getText()); // $$$$$$ change to make Width == Height.  Feb 20
 		memory_size.setText("10");
 		flexibility.setSelected(false);
 		PrisDilemma.setSelected(false);
 		keepOldAgents.setSelected(false);
 		spawnNewAgents.setSelected(true);
 		keepOldArray.setSelected(false);
 		dropNewFood.setSelected(true);
 		ColorCodedAgents.setSelected(true);
 		newColorizer.setSelected(true);
 		keepOldWaste.setSelected(false); // $$$$$$ change from true to false for retrievable experiments.  Feb 20
 		keepOldPackets.setSelected(true);
 		numColor.setText("3");
 		colorSelectSize.setText("3");
 		reColorTimeStep.setText("300");
 		colorizerMode.setText("0");
 		RandomSeed.setText("0");
 		randomStones.setText("10");
 		maxFoodChance.setText("0.8");
 
 		/* Resources */
 		Object[][] temp1 = {
 				{ new Integer(20), new Double(0.5), new Integer(5),
 						new Double(0.9), new Integer(40), new Integer(0),
 						new Integer(0) },
 				{ new Integer(20), new Double(0.5), new Integer(5),
 						new Double(0.9), new Integer(40), new Integer(0),
 						new Integer(0) },
 				{ new Integer(20), new Double(0.5), new Integer(5),
 						new Double(0.9), new Integer(40), new Integer(0),
 						new Integer(0) },
 				{ new Integer(20), new Double(0.5), new Integer(5),
 						new Double(0.9), new Integer(40), new Integer(0),
 						new Integer(0) } };
 		foodData = temp1;
 
 		/* AGENTS INFO */
 		Object[][] temp2 = { { new Integer(20), /* Initial num of agents */
 		new Float(0.05), /* Mutation Rate */
 		new Integer(30), /* Initial Energy */
 		new Integer(30), /* Favourite Food Energy */
 		new Integer(20), /* Other Food Energy */
 		new Integer(100), /* Breed Energy */
 		new Integer(0), /* Pregnancy Period - 1 parent */
 		new Integer(1), /* Step ENergy Loss */
 		new Integer(20), /* Step Rock Energy Loss */
 		new Integer(1), /* TUrn Right Energy Loss */
 		new Integer(1), /* Turn Left Energy Loss */
 		new Integer(2), /* Memory Bits */
 		new Integer(0), /* Min. Communication Similarity */
 		new Integer(4), /* Step Agent Energy Loss */
 		new Integer(2), /* Communication Bits */
 		new Integer(1), /* Pregnancy Period - 2 parents */
 		new Float(0.0), /* Min. Breed Similarity */
 		new Float(1.0), /* 2-parents Breed Chance */
 		new Float(0.0), /* 1 parent Breed Chance */
 		new Boolean(false), /* Agent Aging */
 		new Integer(100), /* Age LImit */
 		new Float(1.0), /* Age Rate */
 		new Boolean(true), /* Waste Production */
 		new Integer(20), /* Step Waste Energy Loss */
 		new Integer(110), /* Energy gain to trigger waste production */
 		new Integer(110), /* Energy spend to trigger Waste production */
 		new Float(0.20), /* Half-life rate for the waste */
 		new Integer(110), /* Initial waste amount */
 		new Boolean(true), /* PD Tit for Tat on/off */
 		new Integer(50), /* PD cooperation bias */
 		new Boolean(true), /* Broadcast Mode */
 		new Boolean(true), /* Broadcast range energy-based */
 		new Integer(20), /* Broadcast fixed range */
 		new Integer(100), /* Broadcast Minimum Energy */
 		new Integer(40) }, /* Broadcast Energy Cost */
 		{ new Integer(20), /* Initial num of agents */
 		new Float(0.05), /* Mutation Rate */
 		new Integer(30), /* Initial Energy */
 		new Integer(30), /* Favourite Food Energy */
 		new Integer(20), /* Other Food Energy */
 		new Integer(100), /* Breed Energy */
 		new Integer(0), /* Pregnancy Period - 1 parent */
 		new Integer(1), /* Step ENergy Loss */
 		new Integer(20), /* Step Rock Energy Loss */
 		new Integer(1), /* TUrn Right Energy Loss */
 		new Integer(1), /* Turn Left Energy Loss */
 		new Integer(2), /* Memory Bits */
 		new Integer(0), /* Min. Communication Similarity */
 		new Integer(4), /* Step Agent Energy Loss */
 		new Integer(2), /* Communication Bits */
 		new Integer(1), /* Pregnancy Period - 2 parents */
 		new Float(0.0), /* Min. Breed Similarity */
 		new Float(1.0), /* 2-parents Breed Chance */
 		new Float(0.0), /* 1 parent Breed Chance */
 		new Boolean(false), /* Agent Aging */
 		new Integer(100), /* Age LImit */
 		new Float(1.0), /* Age Rate */
 		new Boolean(true), /* Waste Production */
 		new Integer(20), /* Step Waste Energy Loss */
 		new Integer(110), /* Energy gain to trigger waste production */
 		new Integer(110), /* Energy spend to trigger Waste production */
 		new Float(0.20), /* Half-life rate for the waste */
 		new Integer(110), /* Initial waste amount */
 		new Boolean(true), /* PD Tit for Tat on/off */
 		new Integer(50), /* PD cooperation bias */
 		new Boolean(true), /* Broadcast Mode */
 		new Boolean(true), /* Broadcast range energy-based */
 		new Integer(20), /* Broadcast fixed range */
 		new Integer(100), /* Broadcast Minimum Energy */
 		new Integer(40) }, /* Broadcast Energy Cost */
 		{ new Integer(20), /* Initial num of agents */
 		new Float(0.05), /* Mutation Rate */
 		new Integer(30), /* Initial Energy */
 		new Integer(30), /* Favourite Food Energy */
 		new Integer(20), /* Other Food Energy */
 		new Integer(100), /* Breed Energy */
 		new Integer(0), /* Pregnancy Period - 1 parent */
 		new Integer(1), /* Step ENergy Loss */
 		new Integer(20), /* Step Rock Energy Loss */
 		new Integer(1), /* TUrn Right Energy Loss */
 		new Integer(1), /* Turn Left Energy Loss */
 		new Integer(2), /* Memory Bits */
 		new Integer(0), /* Min. Communication Similarity */
 		new Integer(4), /* Step Agent Energy Loss */
 		new Integer(2), /* Communication Bits */
 		new Integer(1), /* Pregnancy Period - 2 parents */
 		new Float(0.0), /* Min. Breed Similarity */
 		new Float(1.0), /* 2-parents Breed Chance */
 		new Float(0.0), /* 1 parent Breed Chance */
 		new Boolean(false), /* Agent Aging */
 		new Integer(100), /* Age LImit */
 		new Float(1.0), /* Age Rate */
 		new Boolean(true), /* Waste Production */
 		new Integer(20), /* Step Waste Energy Loss */
 		new Integer(110), /* Energy gain to trigger waste production */
 		new Integer(110), /* Energy spend to trigger Waste production */
 		new Float(0.20), /* Half-life rate for the waste */
 		new Integer(110), /* Initial waste amount */
 		new Boolean(true), /* PD Tit for Tat on/off */
 		new Integer(50), /* PD cooperation bias */
 		new Boolean(true), /* Broadcast Mode */
 		new Boolean(true), /* Broadcast range energy-based */
 		new Integer(20), /* Broadcast fixed range */
 		new Integer(100), /* Broadcast Minimum Energy */
 		new Integer(40) }, /* Broadcast Energy Cost */
 		{ new Integer(20), /* Initial num of agents */
 		new Float(0.05), /* Mutation Rate */
 		new Integer(30), /* Initial Energy */
 		new Integer(30), /* Favourite Food Energy */
 		new Integer(20), /* Other Food Energy */
 		new Integer(100), /* Breed Energy */
 		new Integer(0), /* Pregnancy Period - 1 parent */
 		new Integer(1), /* Step ENergy Loss */
 		new Integer(20), /* Step Rock Energy Loss */
 		new Integer(1), /* TUrn Right Energy Loss */
 		new Integer(1), /* Turn Left Energy Loss */
 		new Integer(2), /* Memory Bits */
 		new Integer(0), /* Min. Communication Similarity */
 		new Integer(4), /* Step Agent Energy Loss */
 		new Integer(2), /* Communication Bits */
 		new Integer(1), /* Pregnancy Period - 2 parents */
 		new Float(0.0), /* Min. Breed Similarity */
 		new Float(1.0), /* 2-parents Breed Chance */
 		new Float(0.0), /* 1 parent Breed Chance */
 		new Boolean(false), /* Agent Aging */
 		new Integer(100), /* Age LImit */
 		new Float(1.0), /* Age Rate */
 		new Boolean(true), /* Waste Production */
 		new Integer(20), /* Step Waste Energy Loss */
 		new Integer(110), /* Energy gain to trigger waste production */
 		new Integer(110), /* Energy spend to trigger Waste production */
 		new Float(0.20), /* Half-life rate for the waste */
 		new Integer(110), /* Initial waste amount */
 		new Boolean(true), /* PD Tit for Tat on/off */
 		new Integer(50), /* PD cooperation bias */
 		new Boolean(true), /* Broadcast Mode */
 		new Boolean(true), /* Broadcast range energy-based */
 		new Integer(20), /* Broadcast fixed range */
 		new Integer(100), /* Broadcast Minimum Energy */
 		new Integer(40) } /* Broadcast Energy Cost */
 		};
 		agentData = temp2;
 
 		/* FOOD WEB */
 		Object[][] temp3 = {
 				{ new Boolean(false), new Boolean(false), new Boolean(false),
 						new Boolean(false), new Boolean(true),
 						new Boolean(true), new Boolean(true), new Boolean(true) },
 				{ new Boolean(false), new Boolean(false), new Boolean(false),
 						new Boolean(false), new Boolean(true),
 						new Boolean(true), new Boolean(true), new Boolean(true) },
 				{ new Boolean(false), new Boolean(false), new Boolean(false),
 						new Boolean(false), new Boolean(true),
 						new Boolean(true), new Boolean(true), new Boolean(true) },
 				{ new Boolean(false), new Boolean(false), new Boolean(false),
 						new Boolean(false), new Boolean(true),
 						new Boolean(true), new Boolean(true), new Boolean(true) } };
 		foodwebData = temp3;
 
 		Object[][] tempPD = { { new Integer(8), null },
 				{ new Integer(6), null }, { new Integer(3), null },
 				{ new Integer(2), null } };
 		PDdata = tempPD;
 	}
 
 	private void loadfromParser(Parser p) {
 		numAgentTypes = ((Integer) (Array.get(p.getfromHashTable("AgentCount"),
 				0))).intValue();
 		numFoodTypes = ((Integer) (Array.get(p.getfromHashTable("FoodCount"),
 				0))).intValue();
 		foodData = new Object[numFoodTypes][resParamNames.length];
 		agentData = new Object[numAgentTypes][agentParamNames.length];
 		foodwebData = new Object[numFoodTypes][numAgentTypes + numFoodTypes];
 		// PDdata = new Object[4][2];
 		setTextField(Width, Array.get(p.getfromHashTable("width"), 0));
 		setTextField(Height, Array.get(p.getfromHashTable("height"), 0));
 		setCheckBoxState(wrap, Array.get(p.getfromHashTable("wrap"), 0));
 		setCheckBoxState(PrisDilemma, Array.get(p
 				.getfromHashTable("PrisDilemma"), 0));
 		setTextField(memory_size, Array
 				.get(p.getfromHashTable("memorysize"), 0));
 		setCheckBoxState(flexibility, Array.get(p.getfromHashTable("foodBias"),
 				0));
 		setCheckBoxState(keepOldAgents, Array.get(p
 				.getfromHashTable("keepoldagents"), 0));
 		setCheckBoxState(keepOldArray, Array.get(p
 				.getfromHashTable("keepoldarray"), 0));
 		setCheckBoxState(spawnNewAgents, Array.get(p
 				.getfromHashTable("spawnnewagents"), 0));
 		setCheckBoxState(dropNewFood, Array.get(p
 				.getfromHashTable("dropnewfood"), 0));
 		setCheckBoxState(ColorCodedAgents, Array.get(p
 				.getfromHashTable("colorcodedagents"), 0));
 		setCheckBoxState(keepOldWaste, Array.get(p
 				.getfromHashTable("keepoldwaste"), 0));
 		setCheckBoxState(keepOldPackets, Array.get(p
 				.getfromHashTable("keepoldpackets"), 0));
 
 		setCheckBoxState(newColorizer, Array.get(p
 				.getfromHashTable("newcolorizer"), 0));
 		setTextField(numColor, Array.get(p.getfromHashTable("numcolor"), 0));
 		setTextField(colorSelectSize, Array.get(p
 				.getfromHashTable("colorselectsize"), 0));
 		setTextField(reColorTimeStep, Array.get(p
 				.getfromHashTable("recolortimestep"), 0));
 
 		setTextField(colorizerMode, Array.get(p
 				.getfromHashTable("colorizermode"), 0));
 		setTextField(RandomSeed, Array.get(p.getfromHashTable("randomseed"), 0));
 		setTextField(randomStones, Array.get(
 				p.getfromHashTable("randomstones"), 0));
 		setTextField(maxFoodChance, Array.get(p
 				.getfromHashTable("maxfoodchance"), 0));
 
 		setTableData(foodData, Array.get(p.getfromHashTable("food"), 0), 1);
 		setTableData(foodData, Array.get(p.getfromHashTable("foodrate"), 0), 2);
 		setTableData(foodData, Array.get(p.getfromHashTable("foodgrow"), 0), 3);
 		setTableData(foodData, Array.get(p.getfromHashTable("fooddeplete"), 0), 4);
 		setTableData(foodData, Array
 				.get(p.getfromHashTable("depletetimesteps"), 0), 5);
 		setTableData(foodData, Array.get(p.getfromHashTable("DraughtPeriod"), 0),
 				6);
 		setTableData(foodData, Array.get(p.getfromHashTable("foodmode"), 0), 7);
 
 		setTableData(agentData, Array.get(p.getfromHashTable("agents"), 0), 1);
 		setTableData(agentData, Array.get(p.getfromHashTable("mutationrate"), 0), 2);
 		setTableData(agentData, Array.get(p.getfromHashTable("initenergy"), 0), 3);
 		setTableData(agentData, Array.get(p.getfromHashTable("foodenergy"), 0), 4);
 		setTableData(agentData,
 				Array.get(p.getfromHashTable("otherfoodenergy"), 0), 5);
 		setTableData(agentData, Array.get(p.getfromHashTable("breedenergy"), 0), 6);
 		setTableData(agentData,
 				Array.get(p.getfromHashTable("pregnancyperiod"), 0), 7);
 
 		setTableData(agentData, Array.get(p.getfromHashTable("stepenergy"), 0), 8);
 		setTableData(agentData, Array.get(p.getfromHashTable("steprockenergy"), 0),
 				9);
 		setTableData(agentData,
 				Array.get(p.getfromHashTable("turnrightenergy"), 0), 10);
 		setTableData(agentData, Array.get(p.getfromHashTable("turnleftenergy"), 0),
 				11);
 		setTableData(agentData, Array.get(p.getfromHashTable("memorybits"), 0), 12);
 		setTableData(agentData, Array.get(p.getfromHashTable("commsimmin"), 0), 13);
 		setTableData(agentData,
 				Array.get(p.getfromHashTable("stepagentenergy"), 0), 14);
 		setTableData(agentData, Array.get(p.getfromHashTable("communicationbits"),
 				0), 15);
 		setTableData(agentData, Array.get(p
 				.getfromHashTable("sexualpregnancyperiod"), 0), 16);
 		setTableData(agentData, Array.get(p.getfromHashTable("breedsimmin"), 0), 17);
 		setTableData(agentData, Array.get(p.getfromHashTable("sexualbreedchance"),
 				0), 18);
 		setTableData(agentData, Array.get(p.getfromHashTable("asexualbreedchance"),
 				0), 19);
 
 		setTableData(agentData, Array.get(p.getfromHashTable("agingMode"), 0), 20);
 		setTableData(agentData, Array.get(p.getfromHashTable("agingLimit"), 0), 21);
 		setTableData(agentData, Array.get(p.getfromHashTable("agingRate"), 0), 22);
 
 		setTableData(agentData, Array.get(p.getfromHashTable("wasteMode"), 0), 23);
 		setTableData(agentData, Array.get(p.getfromHashTable("wastePen"), 0), 24);
 		setTableData(agentData, Array.get(p.getfromHashTable("wasteGain"), 0), 25);
 		setTableData(agentData, Array.get(p.getfromHashTable("wasteLoss"), 0), 26);
 		setTableData(agentData, Array.get(p.getfromHashTable("wasteRate"), 0), 27);
 		setTableData(agentData, Array.get(p.getfromHashTable("wasteInit"), 0), 28);
 		setTableData(agentData, Array.get(p.getfromHashTable("pdTitForTat"), 0), 29);
 		setTableData(agentData, Array.get(p.getfromHashTable("pdCoopProb"), 0), 30);
 		setTableData(agentData, Array.get(p.getfromHashTable("broadcastMode"), 0),
 				31);
 		setTableData(agentData, Array.get(p
 				.getfromHashTable("broadcastEnergyBased"), 0), 32);
 		setTableData(agentData, Array.get(
 				p.getfromHashTable("broadcastFixedRange"), 0), 33);
 		setTableData(agentData, Array.get(p.getfromHashTable("broadcastEnergyMin"),
 				0), 34);
 		setTableData(agentData, Array.get(
 				p.getfromHashTable("broadcastEnergyCost"), 0), 35);
 		setTableHelper(foodwebData);
 
 		for (int i = 0; i < foodwebData.length; i++) {
 			int j;
 			for (j = 0; j < foodwebData.length; j++) {
 				setTableData_agents2eat(foodwebData, Array.get(p
 						.getfromHashTable("agents2eat"), i), j, i);
 			}
 			for (int k = 0; k < foodwebData.length; k++) {
 				// setTableData2(data3,
 				// Array.get(p.getfromHashTable("plants2eat"), i),k+j, i);
 				setTableData_plants2eat(foodwebData, Array.get(p
 						.getfromHashTable("plants2eat"), i), k, i);
 			}
 		}
 		// TODO take off second dimension from array if not used
 		PDdata[0][0] = Array.get(p.getfromHashTable("temptation"), 0);
 		PDdata[1][0] = Array.get(p.getfromHashTable("reward"), 0);
 		PDdata[2][0] = Array.get(p.getfromHashTable("punishment"), 0);
 		PDdata[3][0] = Array.get(p.getfromHashTable("sucker"), 0);
 
 		controllerPanel.readFromParser(p);
 	}
 
 	private void setTextField(JTextField fieldName, Object value) {
 		fieldName.setText(value.toString());
 	}
 
 	private void setCheckBoxState(JCheckBox boxName, Object state) {
 		boxName.setSelected(((Boolean) state).booleanValue());
 	}
 
 	private void setTableData(Object data[][], Object rowdata, int row) {
 		for (int i = 0; i < data.length; i++) {
 			data[i][row - 1] = Array.get(rowdata, i);
 		}
 	}
 
 	/*
 	 * Helper method: load a list of "agents to eat" from the hashtable into
 	 * current data array
 	 */
 	private void setTableData_agents2eat(Object data[][], Object coldata,
 			int j, int i) {
 		int k = ((Integer) Array.get(coldata, j)).intValue();
 		if (k > -1) {
 			data[i][k] = new Boolean(true);
 
 		}
 
 	}
 
 	/*
 	 * Helper method: load a list of "plants to eat" from the hashtable into
 	 * current data array
 	 */
 	private void setTableData_plants2eat(Object data[][], Object coldata,
 			int j, int i) {
 
 		int k = ((Integer) Array.get(coldata, j)).intValue();
 		if (k > -1) {
 			data[i][k + foodwebData.length] = new Boolean(true);
 
 		}
 
 	}
 
 
 	private void setTableHelper(Object data[][]) {
 		for (int i = 0; i < data.length; i++) {
 			for (int j = 0; j < data[i].length; j++) {
 				data[i][j] = new Boolean(false);
 			}
 		}
 	}
 
 	/**
 	 * Writes the information stored in this tree to an XML file, conforming to
 	 * the rules of our spec.
 	 *
 	 * @param fileName
 	 *            the name of the file to which to save the file
 	 * @return true if the file was saved successfully, false otherwise
 	 */
 	public boolean write(String fileName) throws IOException {
 		try {
 			// open the file
 			BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
 			if (out != null) {
 				// write the initial project info
 				out.write("<?xml version='1.0' encoding='utf-8'?>");
 				out.write("\n\n");
 				out.write("<inputData>" + "\n");
 
 				out.write("\t" + "<scheduler>" + TickScheduler + "</scheduler>"
 						+ "\n");
 
 				controllerPanel.writeXML(out);
 				// out.write("\t" + "<ComplexEnvironment>" + new
 				// Integer(ComplexEnvironment.getText()) +
 				// "</ComplexEnvironment>" + "\n");
 				out.write("\t" + "<Width>" + Width.getText() + "</Width>"
 						+ "\n");
 				out.write("\t" + "<Height>" + Height.getText() + "</Height>"
 						+ "\n");
 				out.write("\t" + "<wrap>" + new Boolean(wrap.isSelected())
 						+ "</wrap>" + "\n");
 				out.write("\t" + "<PrisDilemma>"
 						+ new Boolean(PrisDilemma.isSelected())
 						+ "</PrisDilemma>" + "\n");
 				out.write("\t" + "<randomStones>"
 						+ new Integer(randomStones.getText())
 						+ "</randomStones>" + "\n");
 				out.write("\t" + "<maxFoodChance>"
 						+ new Float(maxFoodChance.getText())
 						+ "</maxFoodChance>" + "\n");
 				out.write("\t" + "<keepOldAgents>"
 						+ new Boolean(keepOldAgents.isSelected())
 						+ "</keepOldAgents>" + "\n");
 				out.write("\t" + "<spawnNewAgents>"
 						+ new Boolean(spawnNewAgents.isSelected())
 						+ "</spawnNewAgents>" + "\n");
 				out.write("\t" + "<keepOldArray>"
 						+ new Boolean(keepOldArray.isSelected())
 						+ "</keepOldArray>" + "\n");
 				out.write("\t" + "<dropNewFood>"
 						+ new Boolean(dropNewFood.isSelected())
 						+ "</dropNewFood>" + "\n");
 				out.write("\t" + "<randomSeed>"
 						+ new Integer(RandomSeed.getText()) + "</randomSeed>"
 						+ "\n");
 				out.write("\t" + "<newColorizer>"
 						+ new Boolean(newColorizer.isSelected())
 						+ "</newColorizer>" + "\n");
 				out.write("\t" + "<keepOldWaste>"
 						+ new Boolean(keepOldWaste.isSelected())
 						+ "</keepOldWaste>" + "\n");
 				out.write("\t" + "<keepOldPackets>"
 						+ new Boolean(keepOldPackets.isSelected())
 						+ "</keepOldPackets>" + "\n");
 				out.write("\t" + "<numColor>" + new Integer(numColor.getText())
 						+ "</numColor>" + "\n");
 				out.write("\t" + "<colorSelectSize>"
 						+ new Integer(colorSelectSize.getText())
 						+ "</colorSelectSize>" + "\n");
 				out.write("\t" + "<reColorTimeStep>"
 						+ new Integer(reColorTimeStep.getText())
 						+ "</reColorTimeStep>" + "\n");
 				out.write("\t" + "<colorizerMode>"
 						+ new Integer(colorizerMode.getText())
 						+ "</colorizerMode>" + "\n");
 				out.write("\t" + "<ColorCodedAgents>"
 						+ new Boolean(ColorCodedAgents.isSelected())
 						+ "</ColorCodedAgents>" + "\n");
 				out.write("\t" + "<memorySize>"
 						+ new Integer(memory_size.getText()) + "</memorySize>"
 						+ "\n");
 				out.write("\t" + "<food_bias>"
 						+ new Boolean(flexibility.isSelected()) + "</food_bias>"
 						+ "\n");
 
 				writeHelperFood(out, resourceParamTable.getColumnCount());
 				writeHelperAgents(out, agentParamTable.getColumnCount());
 				writeHelperPDOptions(out);
 				writeHelperGA(out);
 				out.write("</inputData>");
 
 				out.close();
 			}
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 		return true;
 	}
 
 	/**
 	 * internal recursive helper
 	 */
 	private boolean writeHelperFood(BufferedWriter out, int foodtypes)
 			throws IOException {
 		for (int type = 1; type < foodtypes; type++) {
 			try {
 				// write the node info for the current node
 				out.write("<food>" + "\n");
 				out.write("\t" + "<Index>" + (type - 1) + "</Index>" + "\n");
 				out.write("\t" + "<Food>" + resourceParamTable.getValueAt(0, type)
 						+ "</Food>" + "\n");
 				out.write("\t" + "<FoodRate>" + resourceParamTable.getValueAt(1, type)
 						+ "</FoodRate>" + "\n");
 				out.write("\t" + "<FoodGrow>" + resourceParamTable.getValueAt(2, type)
 						+ "</FoodGrow>" + "\n");
 				out.write("\t" + "<FoodDeplete>" + resourceParamTable.getValueAt(3, type)
 						+ "</FoodDeplete>" + "\n");
 				out.write("\t" + "<DepleteTimeSteps>"
 						+ resourceParamTable.getValueAt(4, type) + "</DepleteTimeSteps>"
 						+ "\n");
 				out.write("\t" + "<DraughtPeriod>" + resourceParamTable.getValueAt(5, type)
 						+ "</DraughtPeriod>" + "\n");
 				out.write("\t" + "<FoodMode>" + resourceParamTable.getValueAt(6, type)
 						+ "</FoodMode>" + "\n");
 				out.write("</food>" + "\n");
 			} catch (IOException e) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/* helper method for agents' parameters */
 	private boolean writeHelperAgents(BufferedWriter out, int agentypes) {
 
 		for (int type = 1; type < agentypes; type++) {
 			try {
 				// write the node info for the current node
 				out.write("<agent>" + "\n");
 				out.write("\t" + "<Index>" + (type - 1) + "</Index>" + "\n");
 				out.write("\t" + "<Agents>" + agentParamTable.getValueAt(0, type)
 						+ "</Agents>" + "\n");
 				out.write("\t" + "<MutationRate>" + agentParamTable.getValueAt(1, type)
 						+ "</MutationRate>" + "\n");
 				out.write("\t" + "<InitEnergy>" + agentParamTable.getValueAt(2, type)
 						+ "</InitEnergy>" + "\n");
 				out.write("\t" + "<FoodEnergy>" + agentParamTable.getValueAt(3, type)
 						+ "</FoodEnergy>" + "\n");
 				out.write("\t" + "<OtherFoodEnergy>"
 						+ agentParamTable.getValueAt(4, type) + "</OtherFoodEnergy>"
 						+ "\n");
 				out.write("\t" + "<BreedEnergy>" + agentParamTable.getValueAt(5, type)
 						+ "</BreedEnergy>" + "\n");
 				out.write("\t" + "<pregnancyPeriod>"
 						+ agentParamTable.getValueAt(6, type) + "</pregnancyPeriod>"
 						+ "\n");
 				out.write("\t" + "<StepEnergy>" + agentParamTable.getValueAt(7, type)
 						+ "</StepEnergy>" + "\n");
 				out.write("\t" + "<StepRockEnergy>"
 						+ agentParamTable.getValueAt(8, type) + "</StepRockEnergy>"
 						+ "\n");
 				out.write("\t" + "<TurnRightEnergy>"
 						+ agentParamTable.getValueAt(9, type) + "</TurnRightEnergy>"
 						+ "\n");
 				out.write("\t" + "<TurnLeftEnergy>"
 						+ agentParamTable.getValueAt(10, type) + "</TurnLeftEnergy>"
 						+ "\n");
 				out.write("\t" + "<MemoryBits>" + agentParamTable.getValueAt(11, type)
 						+ "</MemoryBits>" + "\n");
 				out.write("\t" + "<commSimMin>" + agentParamTable.getValueAt(12, type)
 						+ "</commSimMin>" + "\n");
 				out.write("\t" + "<StepAgentEnergy>"
 						+ agentParamTable.getValueAt(13, type) + "</StepAgentEnergy>"
 						+ "\n");
 				out.write("\t" + "<communicationBits>"
 						+ agentParamTable.getValueAt(14, type) + "</communicationBits>"
 						+ "\n");
 				out.write("\t" + "<sexualPregnancyPeriod>"
 						+ agentParamTable.getValueAt(15, type)
 						+ "</sexualPregnancyPeriod>" + "\n");
 				out.write("\t" + "<breedSimMin>" + agentParamTable.getValueAt(16, type)
 						+ "</breedSimMin>" + "\n");
 				out.write("\t" + "<sexualBreedChance>"
 						+ agentParamTable.getValueAt(17, type) + "</sexualBreedChance>"
 						+ "\n");
 				out.write("\t" + "<asexualBreedChance>"
 						+ agentParamTable.getValueAt(18, type) + "</asexualBreedChance>"
 						+ "\n");
 				out.write("\t" + "<agingMode>" + agentParamTable.getValueAt(19, type)
 						+ "</agingMode>" + "\n");
 				out.write("\t" + "<agingLimit>" + agentParamTable.getValueAt(20, type)
 						+ "</agingLimit>" + "\n");
 				out.write("\t" + "<agingRate>" + agentParamTable.getValueAt(21, type)
 						+ "</agingRate>" + "\n");
 				out.write("\t" + "<wasteMode>" + agentParamTable.getValueAt(22, type)
 						+ "</wasteMode>" + "\n");
 				out.write("\t" + "<wastePen>" + agentParamTable.getValueAt(23, type)
 						+ "</wastePen>" + "\n");
 				out.write("\t" + "<wasteGain>" + agentParamTable.getValueAt(24, type)
 						+ "</wasteGain>" + "\n");
 				out.write("\t" + "<wasteLoss>" + agentParamTable.getValueAt(25, type)
 						+ "</wasteLoss>" + "\n");
 				out.write("\t" + "<wasteRate>" + agentParamTable.getValueAt(26, type)
 						+ "</wasteRate>" + "\n");
 				out.write("\t" + "<wasteInit>" + agentParamTable.getValueAt(27, type)
 						+ "</wasteInit>" + "\n");
 				out.write("\t" + "<pdTitForTat>" + agentParamTable.getValueAt(28, type)
 						+ "</pdTitForTat>" + "\n");
 				out.write("\t" + "<pdCoopProb>" + agentParamTable.getValueAt(29, type)
 						+ "</pdCoopProb>" + "\n");
 				out.write("\t" + "<broadcastMode>"
 						+ agentParamTable.getValueAt(30, type) + "</broadcastMode>"
 						+ "\n");
 				out.write("\t" + "<broadcastEnergyBased>"
 						+ agentParamTable.getValueAt(31, type)
 						+ "</broadcastEnergyBased>" + "\n");
 				out.write("\t" + "<broadcastFixedRange>"
 						+ agentParamTable.getValueAt(32, type)
 						+ "</broadcastFixedRange>" + "\n");
 				out.write("\t" + "<broadcastEnergyMin>"
 						+ agentParamTable.getValueAt(33, type) + "</broadcastEnergyMin>"
 						+ "\n");
 				out.write("\t" + "<broadcastEnergyCost>"
 						+ agentParamTable.getValueAt(34, type)
 						+ "</broadcastEnergyCost>" + "\n");
 				writeHelperFoodWeb(out, type, resourceParamTable.getColumnCount(), agentParamTable
 						.getColumnCount());
 				out.write("</agent>" + "\n");
 			} catch (IOException e) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	private boolean writeHelperFoodWeb(BufferedWriter out, int type,
 			int num_foodtypes, int num_agentypes) throws IOException {
 		try {
 			// write the node info for the current node
 			out.write("<foodweb>" + "\n");
 			for (int i = 1; i < num_agentypes; i++) {
 				out.write("\t" + "<agent" + i + ">"
 						+ foodTable.getValueAt(i - 1, type) + "</agent" + i + ">"
 						+ "\n");
 			}
 			for (int i = 1; i < num_foodtypes; i++) {
 				out.write("\t" + "<food" + i + ">"
 						+ foodTable.getValueAt((num_agentypes + i) - 2, type)
 						+ "</food" + i + ">" + "\n");
 			}
 			out.write("</foodweb>" + "\n");
 		} catch (IOException e) {
 			return false;
 		}
 
 		return true;
 	}
 
 	private boolean writeHelperPDOptions(BufferedWriter out) throws IOException {
 		try {
 			out.write("<pd>");
 			out.write("<temptation>");
 			out.write("" + tablePD.getValueAt(0, 1));
 			out.write("</temptation>\n");
 
 			out.write("<reward>");
 			out.write("" + tablePD.getValueAt(1, 1));
 			out.write("</reward>\n");
 
 			out.write("<punishment>");
 			out.write("" + tablePD.getValueAt(2, 1));
 			out.write("</punishment>");
 
 			out.write("<sucker>");
 			out.write("" + tablePD.getValueAt(3, 1));
 			out.write("</sucker>\n");
 			out.write("</pd>\n");
 		} catch (IOException e) {
 			return false;
 		}
 		return true;
 	}
 
 	/** The GA portion of the write() method that writes parameters to xml file. */
 	private boolean writeHelperGA(BufferedWriter out) throws IOException {
 		try {
 			out.write("<ga>");
 			out.write("<agent1gene1>");
 			out.write(""
 					+ genetic_table.getValueAt(GA_AGENT_1_ROW, GA_GENE_1_COL));
 			out.write("</agent1gene1>\n");
 			out.write("<agent1gene2>");
 			out.write(""
 					+ genetic_table.getValueAt(GA_AGENT_1_ROW, GA_GENE_2_COL));
 			out.write("</agent1gene2>\n");
 			out.write("<agent1gene3>");
 			out.write(""
 					+ genetic_table.getValueAt(GA_AGENT_1_ROW, GA_GENE_3_COL));
 			out.write("</agent1gene3>\n");
 			out.write("<agent2gene1>");
 			out.write(""
 					+ genetic_table.getValueAt(GA_AGENT_2_ROW, GA_GENE_1_COL));
 			out.write("</agent2gene1>\n");
 			out.write("<agent2gene2>");
 			out.write(""
 					+ genetic_table.getValueAt(GA_AGENT_2_ROW, GA_GENE_2_COL));
 			out.write("</agent2gene2>\n");
 			out.write("<agent2gene3>");
 			out.write(""
 					+ genetic_table.getValueAt(GA_AGENT_2_ROW, GA_GENE_3_COL));
 			out.write("</agent2gene3>\n");
 			out.write("<agent3gene1>");
 			out.write(""
 					+ genetic_table.getValueAt(GA_AGENT_3_ROW, GA_GENE_1_COL));
 			out.write("</agent3gene1>\n");
 			out.write("<agent3gene2>");
 			out.write(""
 					+ genetic_table.getValueAt(GA_AGENT_3_ROW, GA_GENE_2_COL));
 			out.write("</agent3gene2>\n");
 			out.write("<agent3gene3>");
 			out.write(""
 					+ genetic_table.getValueAt(GA_AGENT_3_ROW, GA_GENE_3_COL));
 			out.write("</agent3gene3>\n");
 			out.write("<agent4gene1>");
 			out.write(""
 					+ genetic_table.getValueAt(GA_AGENT_4_ROW, GA_GENE_1_COL));
 			out.write("</agent4gene1>\n");
 			out.write("<agent4gene2>");
 			out.write(""
 					+ genetic_table.getValueAt(GA_AGENT_4_ROW, GA_GENE_2_COL));
 			out.write("</agent4gene2>\n");
 			out.write("<agent4gene3>");
 			out.write(""
 					+ genetic_table.getValueAt(GA_AGENT_4_ROW, GA_GENE_3_COL));
 			out.write("</agent4gene3>\n");
 			out.write("<linkedphenotype1>");
 			out.write(""
 					+ genetic_table.getValueAt(GA_LINKED_PHENOTYPE_ROW,
 							GA_GENE_1_COL));
 			out.write("</linkedphenotype1>\n");
 			out.write("<linkedphenotype2>");
 			out.write(""
 					+ genetic_table.getValueAt(GA_LINKED_PHENOTYPE_ROW,
 							GA_GENE_2_COL));
 			out.write("</linkedphenotype2>\n");
 			out.write("<linkedphenotype3>");
 			out.write(""
 					+ genetic_table.getValueAt(GA_LINKED_PHENOTYPE_ROW,
 							GA_GENE_3_COL));
 			out.write("</linkedphenotype3>\n");
 			out.write("<meiosismode>");
 			out.write(""+ GeneticCode.meiosis_mode);
 			out.write("</meiosismode>\n");
 			out.write("<trackgenestatusdistribution>");
 			out.write("" + GATracker.getTrackGeneStatusDistribution());
 			out.write("</trackgenestatusdistribution>\n");
 			out.write("<trackgenevaluedistribution>");
 			out.write("" + GATracker.getTrackGeneValueDistribution());
 			out.write("</trackgenevaluedistribution>\n");
 			out.write("<chartupdatefrequency>");
 			out.write("" + GAChartOutput.update_frequency);
 			out.write("</chartupdatefrequency>");
 			out.write("</ga>\n");
 		} catch (IOException e) {
 			return false;
 		}
 
 		return true;
 	}
 
 	public static final long serialVersionUID = 0xB9967684A8375BC0L;
 }
