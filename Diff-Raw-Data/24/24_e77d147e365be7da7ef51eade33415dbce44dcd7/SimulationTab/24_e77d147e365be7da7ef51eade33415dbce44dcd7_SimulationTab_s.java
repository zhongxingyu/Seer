 package evaluation.simulator.gui.layout;
 
 import java.awt.BorderLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileFilter;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTabbedPane;
 import javax.swing.SwingUtilities;
 
 import org.jfree.chart.ChartPanel;
 
 import evaluation.simulator.core.binding.gMixBinding;
 import evaluation.simulator.gui.results.LineJFreeChartCreator;
 import evaluation.simulator.gui.results.ResultPanelFactory;
 import evaluation.simulator.gui.service.ConfigParser;
 
 @SuppressWarnings("serial")
 public class SimulationTab extends JPanel implements ActionListener {
 
 	private JButton addExperiment;
 	JList<File> availableExperiments;
 	final DefaultListModel<File> availableExperimentsModel;
 	gMixBinding callSimulation;
 	private JButton deleteExperiment;
 	private JPanel leftNorth;
 	JScrollPane leftScrollPane;
 
 	private final JPanel north;
 	private int resultCounter;
 	JScrollPane rightScrollPane;
 	JList<File> runExperiments;
 	final DefaultListModel<File> runExperimentsModel;
 	private final JTabbedPane south;
 	private JButton startButton;
 
 	private JButton stopButton;
 
 	public SimulationTab() {
 
 		this.availableExperimentsModel = new DefaultListModel<File>();
 		this.runExperimentsModel = new DefaultListModel<File>();
 
 		this.resultCounter = 1;
 
 		JSplitPane verticalSplitPlane = new JSplitPane();
 		verticalSplitPlane.setOrientation(JSplitPane.VERTICAL_SPLIT);
 		verticalSplitPlane.setOneTouchExpandable(true);
 		verticalSplitPlane.setDividerLocation(150);
 
 		this.setLayout(new BorderLayout());
 		this.add(verticalSplitPlane, BorderLayout.CENTER);
 
 		GridLayout simulationTabLayoutNorth = new GridLayout(1, 2);
 		this.leftNorth = new JPanel();
 
 		this.leftNorth = this.createSelectionPanel();
 
 		this.north = new JPanel();
 		this.north.setLayout(simulationTabLayoutNorth);
 		this.south = new JTabbedPane();
 
 		// GridLayout gridLayout = new GridLayout(3, 3);
 		// this.leftNorth.setLayout(gridLayout);
 		//
 		// JLabel experimentsToSelectLabel = new JLabel(
 		// "<html><b>Select</b> experiments:</html>");
 		// JLabel selectedExperimentsLabel = new
 		// JLabel("Experiments to perform:");
 		// this.leftNorth.add(experimentsToSelectLabel);
 		// this.leftNorth.add(new JLabel(""));
 		// this.leftNorth.add(selectedExperimentsLabel);
 		//
 		// this.availableExperiments = new JList<File>(
 		// this.availableExperimentsModel);
 		// this.leftScrollPane = new JScrollPane(this.availableExperiments);
 		// this.runExperiments = new JList<File>(this.runExperimentsModel);
 		// this.rightScrollPane = new JScrollPane(this.runExperiments);
 		// this.leftNorth.add(this.leftScrollPane);
 		//
 		// JPanel experimentSelectionPane = new JPanel();
 		// GridLayout buttonLayout = new GridLayout(2, 1);
 		// experimentSelectionPane.setLayout(buttonLayout);
 		// experimentSelectionPane.add(this.addExperiment);
 		// experimentSelectionPane.add(this.deleteExperiment);
 		// this.leftNorth.add(experimentSelectionPane);
 		//
 		// this.leftNorth.add(this.rightScrollPane);
 		//
 		// this.leftNorth.add(new JLabel(""));
 		// this.leftNorth.add(this.startButton, GridBagConstraints.NONE);
 		//
 		// this.leftNorth.add(this.stopButton, GridBagConstraints.NONE);
 
 		this.north.add(this.leftNorth);
 		this.north.add(new JLabel(""));
 
 		this.startButton.addActionListener(this);
 		this.stopButton.addActionListener(this);
 
 		this.south.addTab("Results_" + this.resultCounter, new ChartPanel(
 				LineJFreeChartCreator.createAChart()));
 		this.resultCounter++;
 
 		this.south.addTab("Results_" + this.resultCounter, new ChartPanel(
 				LineJFreeChartCreator.createAChart()));
 		this.resultCounter++;
 
 		verticalSplitPlane.setTopComponent(this.north);
 		verticalSplitPlane.setBottomComponent(this.south);
 
 		this.addExperiment.addActionListener(this);
 		this.deleteExperiment.addActionListener(this);
 
 		this.update();
 
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent event) {
 
 		if (event.getSource() == this.startButton) {
 			ConfigParser configParser = new ConfigParser();
 
 			String[][] params = new String[this.runExperiments.getModel()
 					.getSize()][1];
 
 			for (int i = 0; i < this.runExperiments.getModel().getSize(); i++) {
 				params[i][0] = configParser
 						.cleanupConfigurationForSimulator(this.runExperiments
 								.getModel().getElementAt(i));
 
 				this.callSimulation = new gMixBinding(params[i]);
 				this.callSimulation.start();
 
 				// TODO: sync with main thread (pass Statistics)
 			}
 
 			this.south.addTab("Results_" + this.resultCounter,
 					ResultPanelFactory.getResultPanel());
 
 			this.resultCounter++;
 		}
 
 		if (event.getSource() == this.stopButton) {
 			// callSimulation.interrupt();
 		}
 
 		if (event.getSource() == this.addExperiment) {
 			try {
 				int index = this.availableExperiments.getSelectedIndex();
 				if (index != -1) {
 					this.runExperimentsModel
 							.addElement(this.availableExperiments
 									.getSelectedValue());
 					this.availableExperimentsModel.remove(index);
 					this.updateUI();
 				}
 			} catch (Exception e) {
 
 			}
 		}
 
 		if (event.getSource() == this.deleteExperiment) {
 			try {
 				int index = this.runExperiments.getSelectedIndex();
 				if (index != -1) {
 					this.availableExperimentsModel
 							.addElement(this.runExperiments.getSelectedValue());
 					this.runExperimentsModel.remove(index);
 					this.updateUI();
 				}
 			} catch (Exception e) {
 
 			}
 		}
 	}
 
 	private JPanel createSelectionPanel() {
 		JPanel returnPanel = new JPanel();
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[] { 0, 0, 0, 0 };
 		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
 		gridBagLayout.columnWeights = new double[] { 1.0, 0.0, 1.0,
 				Double.MIN_VALUE };
 		gridBagLayout.rowWeights = new double[] { 1.0, 1.0, 0.0, 1.0, 0.0,
 				Double.MIN_VALUE };
 		returnPanel.setLayout(gridBagLayout);
 
 		JLabel lblAvailableConfigurations = new JLabel(
 				"Available Configurations");
 		GridBagConstraints gbc_lblAvailableConfigurations = new GridBagConstraints();
 		gbc_lblAvailableConfigurations.insets = new Insets(0, 0, 5, 5);
 		gbc_lblAvailableConfigurations.gridx = 0;
 		gbc_lblAvailableConfigurations.gridy = 0;
 		returnPanel.add(lblAvailableConfigurations,
 				gbc_lblAvailableConfigurations);
 
 		JLabel lblNewLabel = new JLabel("Experiments to perform");
 		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
 		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 0);
 		gbc_lblNewLabel.gridx = 2;
 		gbc_lblNewLabel.gridy = 0;
 		returnPanel.add(lblNewLabel, gbc_lblNewLabel);
 
 		JScrollPane scrollPane = new JScrollPane();
 		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
 		gbc_scrollPane.gridheight = 2;
 		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
 		gbc_scrollPane.fill = GridBagConstraints.BOTH;
 		gbc_scrollPane.gridx = 0;
 		gbc_scrollPane.gridy = 1;
 		returnPanel.add(scrollPane, gbc_scrollPane);
 
 		this.availableExperiments = new JList<File>(
 				this.availableExperimentsModel);
 		scrollPane.setViewportView(this.availableExperiments);
 
 		this.addExperiment = new JButton(">>");
 		GridBagConstraints gbc_addExperiment = new GridBagConstraints();
		gbc_addExperiment.weightx = 0.5;
		gbc_addExperiment.weighty = 0.5;
 		gbc_addExperiment.anchor = GridBagConstraints.SOUTH;
		gbc_addExperiment.insets = new Insets(0, 0, 5, 5);
 		gbc_addExperiment.gridx = 1;
 		gbc_addExperiment.gridy = 1;
 		returnPanel.add(this.addExperiment, gbc_addExperiment);
 
 		JScrollPane scrollPane_1 = new JScrollPane();
 		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
 		gbc_scrollPane_1.gridheight = 2;
 		gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
 		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
 		gbc_scrollPane_1.gridx = 2;
 		gbc_scrollPane_1.gridy = 1;
 		returnPanel.add(scrollPane_1, gbc_scrollPane_1);
 
 		this.runExperiments = new JList<File>(this.runExperimentsModel);
 		scrollPane_1.setViewportView(this.runExperiments);
 
 		this.deleteExperiment = new JButton("<<");
 		GridBagConstraints gbc_deleteExperiment = new GridBagConstraints();
		gbc_deleteExperiment.weightx = 0.5;
		gbc_deleteExperiment.weighty = 0.5;
 		gbc_deleteExperiment.anchor = GridBagConstraints.NORTH;
		gbc_deleteExperiment.insets = new Insets(0, 0, 5, 5);
 		gbc_deleteExperiment.gridx = 1;
 		gbc_deleteExperiment.gridy = 2;
 		returnPanel.add(this.deleteExperiment, gbc_deleteExperiment);
 
 		JPanel panel = new JPanel();
 		GridBagConstraints gbc_panel = new GridBagConstraints();
 		gbc_panel.weighty = 1.0;
 		gbc_panel.weightx = 1.0;
 		gbc_panel.ipady = 2;
 		gbc_panel.ipadx = 2;
 		gbc_panel.fill = GridBagConstraints.CENTER;
 		gbc_panel.insets = new Insets(0, 0, 5, 5);
 		gbc_panel.gridx = 1;
 		gbc_panel.gridy = 3;
 		returnPanel.add(panel, gbc_panel);
 
 		this.startButton = new JButton("Start Simulation");
 		panel.add(this.startButton);
 
 		this.stopButton = new JButton("Stop Simulation");
 		panel.add(this.stopButton);
 
 		return returnPanel;
 	}
 
 	void update() {
 
 		// Read names of existing experiment configurations
 		final File folder = new File("etc/experiments/");
 		final File[] listOfFiles = folder.listFiles(new FileFilter() {
 
 			@Override
 			public boolean accept(File f) {
 				return f.getName().toLowerCase().endsWith(".cfg");
 			}
 		});
 
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				for (File f : listOfFiles) {
 					boolean insertFlag = true;
 					for (int i = 0; i < SimulationTab.this.availableExperiments
 							.getModel().getSize(); i++) {
 						if (SimulationTab.this.availableExperiments.getModel()
 								.getElementAt(i).equals(f)) {
 							insertFlag = false;
 							break;
 						}
 					}
 
 					for (int i = 0; i < SimulationTab.this.runExperiments
 							.getModel().getSize(); i++) {
 						if (SimulationTab.this.runExperiments.getModel()
 								.getElementAt(i).equals(f)) {
 							insertFlag = false;
 							break;
 						}
 					}
 
 					if (insertFlag) {
 						SimulationTab.this.availableExperimentsModel
 								.addElement(f);
 					}
 				}
 			}
 		});
 	}
 }
