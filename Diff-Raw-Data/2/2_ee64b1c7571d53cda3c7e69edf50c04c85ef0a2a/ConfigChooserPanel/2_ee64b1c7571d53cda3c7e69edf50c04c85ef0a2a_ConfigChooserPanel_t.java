 package evaluation.simulator.gui.customElements;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.swing.ButtonGroup;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JProgressBar;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingUtilities;
 import javax.swing.SwingWorker;
 import javax.swing.border.TitledBorder;
 
 import net.miginfocom.swing.MigLayout;
 import evaluation.simulator.conf.service.SimulationConfigService;
 import evaluation.simulator.core.binding.gMixBinding;
 import evaluation.simulator.gui.actionListeners.ExportButtonAction;
 import evaluation.simulator.gui.helper.IOActions;
 import evaluation.simulator.gui.layout.SimulationTab;
 import evaluation.simulator.gui.pluginRegistry.SimPropRegistry;
 import evaluation.simulator.gui.results.GnuplotPanel;
 import evaluation.simulator.gui.service.ConfigParser;
 
 public class ConfigChooserPanel extends JPanel {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 8399323524494928469L;
 	public JList<File> configList;
 	public DefaultListModel<File> listModel;
 	JButton startButton;
 	JButton stopButton;
 	private static JProgressBar progressBar;
 
 	JRadioButton defaultPlot;
 	JRadioButton expertPlot;
 	ButtonGroup radioButtonGroup;
 	JTextField expertOptions;
 
 	// JButton rightButton = new JButton(">>");
 	// JButton leftButton = new JButton("<<");
 
 	public JButton exportPictureButton;
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
 		JPanel statusPanel = this.createStatusPanel();
 
 		MigLayout migLayout = new MigLayout("", "[grow]", "[grow][grow][grow][grow]");
 		this.setLayout(migLayout);
 		this.add(configurationSelectionPanel, "cell 0 0,grow");
 		// this.add(additionalPlotOptionsPanel, "cell 0 1,growx");
 		this.add(simulationControlPanel, "cell 0 1,growx");
 		this.add(exportResultsPanel, "cell 0 2,growx");
 		this.add(statusPanel, "cell 0 3,growx");
 
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
 					for (int i = 0; i < ConfigChooserPanel.getInstance().configList.getModel().getSize(); i++) {
 						if (ConfigChooserPanel.getInstance().configList.getModel().getElementAt(i).equals(f)) {
 							insertFlag = false;
 							break;
 						}
 					}
 
 					for (int i = 0; i < ConfigChooserPanel.getInstance().configList.getModel().getSize(); i++) {
 						if (ConfigChooserPanel.getInstance().configList.getModel().getElementAt(i).equals(f)) {
 							insertFlag = false;
 							break;
 						}
 					}
 
 					if (insertFlag) {
 						ConfigChooserPanel.getInstance().listModel.addElement(f);
 					}
 				}
 			}
 		});
 
 	}
 
 	private JPanel createStatusPanel() {
 		MigLayout migLayout = new MigLayout("", "[grow]", "[grow]");
 		JPanel panel = new JPanel(migLayout);
 
 		progressBar = new JProgressBar(0, 100);
 		progressBar.setIndeterminate(true);
 		progressBar.setVisible(false);
 		panel.add(progressBar, "growx,push");
 		panel.setBorder(new TitledBorder(null, "Simulation Status", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		return panel;
 	}
 
 	private JPanel createExportResultsPanel() {
 
 		MigLayout migLayout = new MigLayout("", "[grow][]", "[grow]");
 		JPanel panel = new JPanel(migLayout);
 
 		this.exportPictureButton = new JButton("Export Graph");
 		exportPictureButton.addActionListener(new ExportButtonAction());
 		exportPictureButton.setEnabled(false);
 
 		panel.add(this.exportPictureButton, "cell 0 1,growx");
 
 		panel.setBorder(new TitledBorder(null, "Export Results", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		return panel;
 
 	}
 
 	class ProgressWorker extends SwingWorker {
 		protected String doInBackground() {
 			progressBar.setVisible(true);
 			progressBar.setIndeterminate(true);
 
 			SimulationTab.getInstance().getResultsPanel().remove(SimulationTab.getInstance().homeTab);
 
 			ConfigParser configParser = new ConfigParser();
 
 			String[][] params = new String[ConfigChooserPanel.getInstance().configList.getSelectedValuesList().size()][1];
 
 			int i = 0;
 			for (File file : ConfigChooserPanel.getInstance().configList.getSelectedValuesList()) {
 				params[i][0] = configParser.cleanupConfigurationForSimulator(file);
 
 				ConfigChooserPanel.getInstance().callSimulation = gMixBinding.getInstance();
 				ConfigChooserPanel.getInstance().callSimulation.setParams(params[i]);
 				ConfigChooserPanel.getInstance().callSimulation.run();
 				final int j = i;
 
 				SwingUtilities.invokeLater(new Runnable() {
 					@Override
 					public void run() {
 						progressBar.setValue(j);
 					}
 				});
 				i++;
 			}
 			return null;
 		}
 
 		protected void done() {
 			progressBar.setVisible(false);
 		}
 	}
 
 	private JPanel createSimulationControlPanel() {
 
 		MigLayout migLayout = new MigLayout("", "[grow]", "[grow]");
 		JPanel panel = new JPanel(migLayout);
 
 		this.startButton = new JButton("Start Simulation");
 		this.startButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (configList.getSelectedIndices().length == 0) {
 					JOptionPane.showMessageDialog(null, "Please select at least one config!");
 				} else {
 					new ProgressWorker().execute();
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
 				gMixBinding.getInstance().resetExperiments();
 				SimulationTab.getInstance().getResultsPanel().add("Welcome", SimulationTab.getInstance().homeTab);
 				try {
 					IOActions.cleanOutputFolder();
 				} catch (IOException e1) {
 					JOptionPane.showMessageDialog(null, "Could not clean up Output directory "
 							+ GnuplotPanel.outputFolder, "Cleanup Error", JOptionPane.ERROR_MESSAGE);
 				}
 				exportPictureButton.setEnabled(false);
 			}
 		});
 
 		panel.add(this.startButton, "cell 0 0,growx");
 		panel.add(this.stopButton, "cell 0 1,growx");
 		panel.add(this.clearButton, "cell 0 2,growx");
 
 		panel.setBorder(new TitledBorder(null, "Simulation Control", TitledBorder.LEADING, TitledBorder.TOP, null, null));
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
 
 		panel.setBorder(new TitledBorder(null, "Additional Plot Options", TitledBorder.LEADING, TitledBorder.TOP, null,
 				null));
 
 		return panel;
 
 	}
 
 	private JPanel createConfigSelectionPanel() {
 
 		MigLayout migLayout = new MigLayout("", "[grow]", "[grow]");
 		JPanel panel = new JPanel(migLayout);
 		this.listModel = new DefaultListModel<File>();
 		this.configList = new JList<File>(this.listModel);
 		this.configList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
 
 		this.configList.addMouseListener(new MouseAdapter() {
 
 			public void mousePressed(MouseEvent e) {
 				final int[] selection = configList.getSelectedIndices();
 				final int hoverIndex = configList.locationToIndex(e.getPoint());
 				System.out.println(configList.getModel().getElementAt(hoverIndex) + " selected");
				if (SwingUtilities.isRightMouseButton(e)) {
 					JPopupMenu menu = new JPopupMenu();
 					JMenuItem item = new JMenuItem("Load into config tool");
 					item.addActionListener(new ActionListener() {
 						public void actionPerformed(ActionEvent e) {
 							File file = configList.getModel().getElementAt(hoverIndex);
 							SimPropRegistry simPropRegistry = SimPropRegistry.getInstance();
 							SimulationConfigService simulationConfigService = new SimulationConfigService(
 									simPropRegistry);
 							simulationConfigService.loadConfig(file);
 						}
 					});
 					JMenuItem addItem = new JMenuItem("Add to selection");
 					addItem.addActionListener(new ActionListener() {
 						public void actionPerformed(ActionEvent e) {
 							Set<Integer> newSelection = new HashSet<Integer>();
 							for (int i : selection) {
 								newSelection.add(i);
 							}
 							newSelection.add(hoverIndex);
 							int[] newIndexSelection = new int[newSelection.size()];
 							int i = 0;
 							for (int each : newSelection) {
 								newIndexSelection[i] = each;
 								i++;
 							}
 							configList.setSelectedIndices(newIndexSelection);
 						}
 					});
 					JMenuItem delItem = new JMenuItem("Delete from Selection");
 					delItem.addActionListener(new ActionListener() {
 						public void actionPerformed(ActionEvent e) {
 							Set<Integer> newSelection = new HashSet<Integer>();
 							for (int i : selection) {
 								newSelection.add(i);
 							}
 							newSelection.remove(hoverIndex);
 							int[] newIndexSelection = new int[newSelection.size()];
 							int i = 0;
 							for (int each : newSelection) {
 								newIndexSelection[i] = each;
 								i++;
 							}
 							configList.setSelectedIndices(newIndexSelection);
 						}
 					});
 					menu.add(item);
 
 					boolean selected = false;
 					for (int i = 0; i < selection.length; i++) {
 						if (selection[i] == hoverIndex)
 							selected = true;
 					}
 
 					if (!selected) {
 						menu.add(addItem);
 					} else {
 						menu.add(delItem);
 					}
 					menu.show(configList, e.getX(), e.getY());
 				}
 			}
 		});
 
 		JScrollPane scrollPane = new JScrollPane(this.configList);
 
 		panel.add(scrollPane, "cell 0 0,growx,growy");
 		panel.add(new JLabel("* Multiple selection is possible"), "cell 0 1,growx,growy");
 
 		panel.setBorder(new TitledBorder(null, "Configuration Selection", TitledBorder.LEADING, TitledBorder.TOP, null,
 				null));
 		return panel;
 
 	}
 
 }
