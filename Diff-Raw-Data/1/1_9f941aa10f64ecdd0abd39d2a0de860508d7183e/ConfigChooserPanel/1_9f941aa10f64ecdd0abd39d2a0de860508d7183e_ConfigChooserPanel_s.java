 package evaluation.simulator.gui.customElements;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileFilter;
 
 import javax.swing.ButtonGroup;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingUtilities;
 import javax.swing.border.TitledBorder;
 
 import net.miginfocom.swing.MigLayout;
 import evaluation.simulator.core.binding.gMixBinding;
 import evaluation.simulator.gui.layout.SimulationTab;
 import evaluation.simulator.gui.service.ConfigParser;
 
 public class ConfigChooserPanel extends JPanel{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 8399323524494928469L;
 	public JList<File> configList;
 	public DefaultListModel<File> listModel;
 	JButton startButton;
 	JButton stopButton;
 
 	JRadioButton defaultPlot;
 	JRadioButton expertPlot;
 	ButtonGroup radioButtonGroup;
 	JTextField expertOptions;
 
 	JButton rightButton = new JButton(">>");
 	JButton leftButton = new JButton("<<");
 
 	private JButton setSaveFolderButton;
 	private JButton exportPictureButton;
 	private gMixBinding callSimulation;
 	private JButton clearButton;
 
 	private static ConfigChooserPanel instance = null;
 
 	public ConfigChooserPanel() {
 
 		this.initialize();
 
 	}
 
 	public static ConfigChooserPanel getInstance() {
 		if (instance == null) {
 			instance = new ConfigChooserPanel();
 		}
 		return instance;
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 
 		JPanel configurationSelectionPanel = this.createConfigSelectionPanel();
 		JPanel additionalPlotOptionsPanel = this.createAdditionalPlotOptionsPanel();
 		JPanel simulationControlPanel = this.createSimulationControlPanel();
 		JPanel exportResultsPanel = this.createExportResultsPanel();
 
 		MigLayout migLayout = new MigLayout("","[grow]","[grow][grow][grow][grow]");
 		this.setLayout(migLayout);
 		this.add(configurationSelectionPanel,"cell 0 0,growx,growy");
 		this.add(additionalPlotOptionsPanel,"cell 0 1,growx");
 		this.add(simulationControlPanel,"cell 0 2,growx");
 		this.add(exportResultsPanel,"cell 0 3,growx");
 
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
 					for (int i = 0; i < ConfigChooserPanel.getInstance().configList
 							.getModel().getSize(); i++) {
 						if (ConfigChooserPanel.getInstance().configList.getModel()
 								.getElementAt(i).equals(f)) {
 							insertFlag = false;
 							break;
 						}
 					}
 
 					for (int i = 0; i < ConfigChooserPanel.getInstance().configList
 							.getModel().getSize(); i++) {
 						if (ConfigChooserPanel.getInstance().configList.getModel()
 								.getElementAt(i).equals(f)) {
 							insertFlag = false;
 							break;
 						}
 					}
 
 					if (insertFlag) {
 						ConfigChooserPanel.getInstance().listModel
 						.addElement(f);
 					}
 				}
 			}
 		});
 
 	}
 
 	private JPanel createExportResultsPanel() {
 
 		MigLayout migLayout = new MigLayout("", "[grow][]", "[grow]");
 		JPanel panel = new JPanel(migLayout);
 
 		this.setSaveFolderButton = new JButton("Set Save Folder");
 		this.exportPictureButton = new JButton("Export Different Format");
 
 		panel.add(this.setSaveFolderButton,"cell 0 0,growx");
 		panel.add(this.exportPictureButton,"cell 0 1,growx");
 
 		panel.setBorder(new TitledBorder(null, "Export Results",
 				TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		return panel;
 
 	}
 
 	private JPanel createSimulationControlPanel() {
 
 		MigLayout migLayout = new MigLayout("","[grow]","[grow]");
 		JPanel panel = new JPanel(migLayout);
 
 		this.startButton = new JButton("Start Simulation");
 		this.startButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 
 				SimulationTab.getInstance().getResultsPanel().remove(SimulationTab.getInstance().homeTab);
 
 				ConfigParser configParser = new ConfigParser();
 
 				String[][] params = new String[ConfigChooserPanel.getInstance().configList.getSelectedValuesList().size()][1];
 
 				int i=0;
 				for (File file : ConfigChooserPanel.getInstance().configList.getSelectedValuesList()) {
 					params[i][0] = configParser
 							.cleanupConfigurationForSimulator(file);
 
 					ConfigChooserPanel.getInstance().callSimulation = gMixBinding.getInstance();
 					ConfigChooserPanel.getInstance().callSimulation.setParams(params[i]);
 					ConfigChooserPanel.getInstance().callSimulation.run();
 					i++;
 					// TODO: sync with main thread (pass Statistics)
 
 				}
 			}
 		});
 
 		this.stopButton = new JButton("Stop Simulation");
 		this.stopButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				ConfigChooserPanel.getInstance().callSimulation.interrupt();
 			}
 		});
 
 		this.clearButton = new JButton("Clear Results");
 		this.clearButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				SimulationTab.getInstance().getResultsPanel().removeAll();
 				SimulationTab.getInstance().getResultsPanel().add("Welcome", SimulationTab.getInstance().homeTab);
 			}
 		});
 
 		panel.add(this.startButton, "cell 0 0,growx");
 		panel.add(this.stopButton, "cell 0 1,growx");
 		panel.add(this.clearButton, "cell 0 2,growx");
 
 		panel.setBorder(new TitledBorder(null, "Simulation Control",
 				TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		return panel;
 
 	}
 
 	private JPanel createAdditionalPlotOptionsPanel() {
 
 		MigLayout migLayout = new MigLayout("", "[grow][]", "[grow][]");
 		JPanel panel = new JPanel(migLayout);
 		this.radioButtonGroup = new ButtonGroup();
 		this.expertOptions = new JTextField();
 		this.expertOptions.setEnabled(false);
 		panel.add(this.expertOptions, "cell 0 1 2 1,growx");
 
 		this.defaultPlot = new JRadioButton("Default");
 		this.defaultPlot.setSelected(true);
 		this.defaultPlot.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				ConfigChooserPanel.getInstance().expertOptions.setEnabled(false);
 			}
 		});
 		this.radioButtonGroup.add(this.defaultPlot);
 
 		panel.add(this.defaultPlot, "flowx,cell 0 0");
 		this.expertPlot = new JRadioButton("Additional Options");
 		this.expertPlot.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				ConfigChooserPanel.getInstance().expertOptions.setEnabled(true);
 			}
 		});
 		this.radioButtonGroup.add(this.expertPlot);
 		panel.add(this.expertPlot, "cell 0 0,growx");
 
 		panel.setBorder(new TitledBorder(null, "Additional Plot Options",
 				TitledBorder.LEADING, TitledBorder.TOP, null, null));
 
 		return panel;
 
 	}
 
 	private JPanel createConfigSelectionPanel() {
 
 		MigLayout migLayout = new MigLayout("", "[grow]", "[grow]");
 		JPanel panel = new JPanel(migLayout);
 		this.listModel = new DefaultListModel<File>();
 		this.configList = new JList<File>(this.listModel);
 		this.configList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
 
 		JScrollPane scrollPane = new JScrollPane(this.configList);
 
 		panel.add(scrollPane,"cell 0 0,growx,growy");
 
 		panel.setBorder(new TitledBorder(null, "Configuration Selection",
 				TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		return panel;
 
 	}
 
 }
