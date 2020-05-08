 package net.sprauer.sitzplaner.view.panels;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.JTabbedPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.border.LineBorder;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ListDataEvent;
 import javax.swing.event.ListDataListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import net.sprauer.sitzplaner.EA.ConfigManager;
 import net.sprauer.sitzplaner.EA.Configuration;
 import net.sprauer.sitzplaner.EA.EAFactory;
 import net.sprauer.sitzplaner.EA.Strategy;
 import net.sprauer.sitzplaner.model.DataBase;
 import net.sprauer.sitzplaner.view.Blackboard;
 import net.sprauer.sitzplaner.view.ClassRoom;
 import net.sprauer.sitzplaner.view.Commands.Factory;
 import net.sprauer.sitzplaner.view.helper.Parameter;
 
 public class ToolsPanel extends JPanel {
 
 	private static final long serialVersionUID = -5782625606599426122L;
 	private final JButton btnSaveClass;
 	private final JButton btnLoadClass;
 	private final JButton btnNewSeatingPlan;
 	private final JSpinner numCols;
 	private final JSpinner numRows;
 	private static ToolsPanel _instance;
 	private final JPanel pnlLoadAndSave;
 	private final JSpinner classSize;
 	private boolean blockUpdates = false;
 	private JTabbedPane tabClassDimensions;
 	private JLabel lblClassSize;
 	private JButton btnCreate;
 	private JPanel panel;
 	private JLabel lblDescendants_text;
 	private JLabel lblNumberOfGenerations;
 	private JSpinner spnNumberOfGenerations;
 	private JTabbedPane tabSettings;
 	private JPanel panel_1;
 	private JPanel panel_2;
 	private JRadioButton rdbCommaLambda;
 	private JLabel lblStrategy;
 	private JRadioButton rdbPlusLambda;
 	private JSpinner spnInversion;
 	private JSpinner spnSwap;
 	private GridBagConstraints gbc_spnSwap;
 	private GridBagConstraints gbc_spnInversion;
 	private JLabel lblStudent;
 	private JSpinner spnRight;
 	private JSpinner spnRight2;
 	private JSpinner spnBottom;
 	private JSpinner spnDiagonal;
 	private JLabel lblTop;
 	private JLabel lblDiagonal1;
 	private JLabel lblLeft;
 	private JLabel lblLeft2;
 	private JLabel lblDiagonal2;
 	private JLabel lblDiagonal3;
 	private GridBagConstraints gbc_spnRight2;
 	private JPanel panel_3;
 	private JList lstSettings;
 	private JButton btnSettingsSave;
 	private JButton btnSettingsLoad;
 	private JButton btnSettingsNew;
 	private JButton btnSettingsDelete;
 	private JLabel lblParents_text;
 	private JSpinner spnParents;
 	private JScrollPane scrollPane;
 	private JLabel lblParents_1;
 	private JLabel lblParents;
 	private JLabel lblDescendants;
 	private JLabel lblDescendents;
 	private JLabel lblDescendants1;
 	private JLabel lblTotal;
 	private JLabel lblSwap;
 	private JLabel lblInversion;
 	private JPanel pnlBlackboard;
 	private JSpinner spnPriority;
 	private JPanel panel_5;
 	private JLabel lblPriority;
 	private JProgressBar progressBar;
 	private JLabel lblCreate;
 	private JLabel lblDescendantsUsingSwap;
 	private JLabel lblSwapOperations;
 	private JSpinner spnNumberOfSwaps;
 	private JLabel lblCreate_1;
 	private JLabel lblDescendantsUsingInversion;
 	private JSpinner spnNumberOfInversions;
 	private JLabel lblInversionOperations;
 	private JLabel lblPerDescnendant;
 	private JLabel lblNewLabel;
 	private JLabel lblNewLabel_1;
 	private JLabel lblTheNewGeneration;
 	private JLabel lblDistanceToBlackboard;
 	private JRadioButton rdbRank;
 	private JRadioButton rdbTournament;
 
 	private final ChangeListener weightingChangedListener = new ChangeListener() {
 
 		@Override
 		public void stateChanged(ChangeEvent e) {
 			lblTop.setText("" + spnBottom.getValue());
 			lblLeft.setText("" + spnRight.getValue());
 			lblLeft2.setText("" + spnRight2.getValue());
 			lblDiagonal1.setText("" + spnDiagonal.getValue());
 			lblDiagonal2.setText("" + spnDiagonal.getValue());
 			lblDiagonal3.setText("" + spnDiagonal.getValue());
 			if (blockUpdates) {
 				return;
 			}
 			Configuration currentConfig = ConfigManager.instance().getCurrentConfig();
 			currentConfig.setWeightingRight((Integer) spnRight.getValue());
 			currentConfig.setWeightingRight2((Integer) spnRight2.getValue());
 			currentConfig.setWeightingBottom((Integer) spnBottom.getValue());
 			currentConfig.setWeightingDiagonal((Integer) spnDiagonal.getValue());
 			currentConfig.setWeightingPriority((Integer) spnPriority.getValue());
 			ConfigManager.instance().currentConfigUpdated();
 		}
 	};
 	private final ChangeListener classDimensionChangeListener = new ChangeListener() {
 		@Override
 		public void stateChanged(ChangeEvent e) {
 			if (blockUpdates) {
 				return;
 			}
 			Dimension dim = new Dimension((Short) numCols.getValue(), (Short) numRows.getValue());
 			ClassRoom.instance().setNewDimensions(dim);
 			Short maximum = (short) (dim.height * dim.width);
 			((SpinnerNumberModel) classSize.getModel()).setMaximum(maximum);
 			if ((Short) classSize.getValue() > maximum) {
 				classSize.setValue(maximum);
 			}
 		}
 	};
 	private final ListSelectionListener listSelectionListener = new ListSelectionListener() {
 
 		@Override
 		public void valueChanged(ListSelectionEvent arg) {
 			if (blockUpdates || arg.getValueIsAdjusting()) {
 				return;
 			}
 			final int newIndex = lstSettings.getSelectedIndex();
 			ConfigManager.instance().setCurrentConfigIndex(newIndex);
 			EAFactory.showChromosomeForCurrentConfig();
 		}
 	};
 	private JRadioButton rdbNormal;
 	private JSpinner spnTournamentSize;
 	private JPanel panel_4;
 
 	private void createRadioButtonGroup() {
 		ButtonGroup g = new ButtonGroup();
 		ButtonGroup g1 = new ButtonGroup();
 		g.add(rdbCommaLambda);
 		g.add(rdbPlusLambda);
 		g1.add(rdbNormal);
 		g1.add(rdbRank);
 		g1.add(rdbTournament);
 		ActionListener changeListener = new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				spnTournamentSize.setEnabled(rdbTournament.isSelected());
 				if (blockUpdates) {
 					return;
 				}
 				Strategy strategy = getStrategy();
 				ConfigManager.instance().getCurrentConfig().setStrategiePlus(rdbPlusLambda.isSelected());
 				ConfigManager.instance().getCurrentConfig().setTournamentSize((Integer) spnTournamentSize.getValue());
 				ConfigManager.instance().getCurrentConfig().setStrategy(strategy);
 				ConfigManager.instance().currentConfigUpdated();
 			}
 
 		};
 		rdbCommaLambda.getModel().addActionListener(changeListener);
 		rdbPlusLambda.getModel().addActionListener(changeListener);
 		rdbNormal.getModel().addActionListener(changeListener);
 		rdbRank.getModel().addActionListener(changeListener);
 		rdbTournament.getModel().addActionListener(changeListener);
 		spnTournamentSize.getModel().addChangeListener(new ChangeListener() {
 			@Override
 			public void stateChanged(ChangeEvent e) {
 				if (blockUpdates) {
 					return;
 				}
 				final Configuration currentConfig = ConfigManager.instance().getCurrentConfig();
 				currentConfig.setTournamentSize((Integer) spnTournamentSize.getValue());
 				ConfigManager.instance().currentConfigUpdated();
 			}
 		});
 	}
 
 	private Strategy getStrategy() {
 		Strategy strategy = Strategy.Normal;
 		if (rdbNormal.isSelected()) {
 			strategy = Strategy.Normal;
 		} else if (rdbRank.isSelected()) {
 			strategy = Strategy.Rank;
 		} else if (rdbTournament.isSelected()) {
 			strategy = Strategy.Tournament;
 		}
 		return strategy;
 	}
 
 	private void updateMutationDistribution() {
 		final int swap = (Integer) spnSwap.getValue();
 		final int inversion = (Integer) spnInversion.getValue();
 		final int numParents = (Integer) spnParents.getValue();
 		final int numDescendants = (inversion + swap) * numParents;
 		lblDescendants.setText("" + numDescendants);
 		lblDescendants1.setText("" + numDescendants);
 		lblSwap.setText("" + (swap * numParents));
 		lblInversion.setText("" + (inversion * numParents));
 		if (blockUpdates) {
 			return;
 		}
 		final Configuration currentConfig = ConfigManager.instance().getCurrentConfig();
 		currentConfig.setDescendantsUsingSwap(swap);
 		currentConfig.setDescendentsUsingInversion(inversion);
 		ConfigManager.instance().currentConfigUpdated();
 	}
 
 	private void addActionHandlers() {
 		spnNumberOfGenerations.getModel().addChangeListener(new ChangeListener() {
 			@Override
 			public void stateChanged(ChangeEvent e) {
 				EAFactory.setNumberOfGenerations((Integer) spnNumberOfGenerations.getValue());
 			}
 		});
 
 		spnParents.getModel().addChangeListener(new ChangeListener() {
 			@Override
 			public void stateChanged(ChangeEvent arg0) {
 				if (blockUpdates) {
 					return;
 				}
 				final Integer value = (Integer) spnParents.getValue();
 				lblParents.setText("" + value);
 				updateMutationDistribution();
 				ConfigManager.instance().getCurrentConfig().setParents(value);
 			}
 		});
 
 		ConfigManager.instance().addListDataListener(new ListDataListener() {
 			@Override
 			public void intervalRemoved(ListDataEvent arg0) {
 			}
 
 			@Override
 			public void intervalAdded(ListDataEvent arg0) {
 			}
 
 			@Override
 			public void contentsChanged(ListDataEvent arg0) {
 				if (blockUpdates) {
 					return;
 				}
 
 				final Configuration currentConfig = ConfigManager.instance().getCurrentConfig();
 				blockUpdates = true;
 				spnBottom.setValue(currentConfig.getWeightingBottom());
 				spnDiagonal.setValue(currentConfig.getWeightingDiagonal());
 				spnRight.setValue(currentConfig.getWeightingRight());
 				spnRight2.setValue(currentConfig.getWeightingRight2());
 				spnPriority.setValue(currentConfig.getWeightingPriority());
 				spnParents.setValue(currentConfig.getParents());
 				lblParents.setText("" + currentConfig.getParents());
 				spnInversion.setValue(currentConfig.getDescendantsUsingInversion());
 				spnSwap.setValue(currentConfig.getDescendantsUsingSwap());
 				rdbPlusLambda.setSelected(currentConfig.isStrategiePlus());
 				rdbCommaLambda.setSelected(!currentConfig.isStrategiePlus());
 				rdbNormal.setSelected(currentConfig.getStrategy() == Strategy.Normal);
 				rdbRank.setSelected(currentConfig.getStrategy() == Strategy.Rank);
 				rdbTournament.setSelected(currentConfig.getStrategy() == Strategy.Tournament);
 				spnTournamentSize.setValue(currentConfig.getTournamentSize());
 				lblDescendants.setText("" + currentConfig.getNumberOfDescendents());
 				lblDescendants1.setText("" + currentConfig.getNumberOfDescendents());
 				spnNumberOfInversions.setValue(currentConfig.getNumberOfInversions());
 				spnNumberOfSwaps.setValue(currentConfig.getNumberOfSwaps());
 				spnTournamentSize.setEnabled(rdbTournament.isSelected());
 
 				blockUpdates = false;
 			}
 		});
 	}
 
 	public ToolsPanel() {
 		_instance = this;
 
 		SpinnerNumberModel rowSpinnerModel = createRowSpinnerModel(classDimensionChangeListener);
 		SpinnerNumberModel colSpinnerModel = createColSpinnerModel(classDimensionChangeListener);
 
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[] { 130, 80, 0, 30, 0, 0 };
 		gridBagLayout.rowHeights = new int[] { 97, 0, 0, 0 };
 		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
 		gridBagLayout.rowWeights = new double[] { 1.0, 0.0, 0.0, Double.MIN_VALUE };
 		setLayout(gridBagLayout);
 
 		tabClassDimensions = new JTabbedPane(JTabbedPane.TOP);
 		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
 		gbc_tabbedPane.fill = GridBagConstraints.HORIZONTAL;
 		gbc_tabbedPane.gridwidth = 3;
 		gbc_tabbedPane.insets = new Insets(0, 0, 5, 5);
 		gbc_tabbedPane.gridx = 0;
 		gbc_tabbedPane.gridy = 0;
 		add(tabClassDimensions, gbc_tabbedPane);
 
 		JPanel pnlClassRoomSize = new JPanel();
 		tabClassDimensions.addTab("Create Class", null, pnlClassRoomSize, null);
 		GridBagLayout gbl_pnlClassRoomSize = new GridBagLayout();
 		gbl_pnlClassRoomSize.columnWidths = new int[] { 30, 50, 65, 50, 29 };
 		gbl_pnlClassRoomSize.rowHeights = new int[] { 20, 20, 0 };
 		gbl_pnlClassRoomSize.columnWeights = new double[] { 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE };
 		gbl_pnlClassRoomSize.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
 		pnlClassRoomSize.setLayout(gbl_pnlClassRoomSize);
 
 		JLabel label = new JLabel("Rows:");
 		GridBagConstraints gbc_label = new GridBagConstraints();
 		gbc_label.ipady = 3;
 		gbc_label.ipadx = 3;
 		gbc_label.anchor = GridBagConstraints.EAST;
 		gbc_label.insets = new Insets(10, 10, 10, 10);
 		gbc_label.gridx = 0;
 		gbc_label.gridy = 0;
 		pnlClassRoomSize.add(label, gbc_label);
 
 		numRows = new JSpinner();
 		numRows.setModel(rowSpinnerModel);
 		GridBagConstraints gbc_numRows = new GridBagConstraints();
 		gbc_numRows.insets = new Insets(0, 0, 5, 5);
 		gbc_numRows.weightx = 10.0;
 		gbc_numRows.fill = GridBagConstraints.HORIZONTAL;
 		gbc_numRows.gridx = 1;
 		gbc_numRows.gridy = 0;
 		pnlClassRoomSize.add(numRows, gbc_numRows);
 
 		lblClassSize = new JLabel("Class size:");
 		GridBagConstraints gbc_lblClassSize = new GridBagConstraints();
 		gbc_lblClassSize.anchor = GridBagConstraints.EAST;
 		gbc_lblClassSize.insets = new Insets(0, 0, 5, 5);
 		gbc_lblClassSize.gridx = 2;
 		gbc_lblClassSize.gridy = 0;
 		pnlClassRoomSize.add(lblClassSize, gbc_lblClassSize);
 
 		classSize = new JSpinner();
 		GridBagConstraints gbc_classSize = new GridBagConstraints();
 		gbc_classSize.fill = GridBagConstraints.HORIZONTAL;
 		gbc_classSize.insets = new Insets(0, 0, 5, 0);
 		gbc_classSize.gridx = 3;
 		gbc_classSize.gridy = 0;
 		pnlClassRoomSize.add(classSize, gbc_classSize);
 		classSize.setModel(new SpinnerNumberModel(new Short((short) 1), new Short((short) 1), new Short((short) 1), new Short(
 				(short) 1)));
 		classSize.setValue(new Short((short) (Parameter.numCols * Parameter.numRows)));
 		classSize.getModel().addChangeListener(new ChangeListener() {
 
 			@Override
 			public void stateChanged(ChangeEvent e) {
 				if (blockUpdates) {
 					return;
 				}
 				DataBase.instance().setSize((Short) classSize.getValue());
 			}
 		});
 
 		JLabel label_1 = new JLabel("Columns:");
 		GridBagConstraints gbc_label_1 = new GridBagConstraints();
 		gbc_label_1.anchor = GridBagConstraints.EAST;
 		gbc_label_1.insets = new Insets(10, 10, 10, 10);
 		gbc_label_1.ipady = 3;
 		gbc_label_1.ipadx = 3;
 		gbc_label_1.gridx = 0;
 		gbc_label_1.gridy = 1;
 		pnlClassRoomSize.add(label_1, gbc_label_1);
 
 		numCols = new JSpinner();
 		numCols.setModel(colSpinnerModel);
 		GridBagConstraints gbc_numCols = new GridBagConstraints();
 		gbc_numCols.insets = new Insets(0, 0, 0, 5);
 		gbc_numCols.fill = GridBagConstraints.HORIZONTAL;
 		gbc_numCols.gridx = 1;
 		gbc_numCols.gridy = 1;
 		pnlClassRoomSize.add(numCols, gbc_numCols);
 
 		btnCreate = new JButton(Factory.CommandNewClass);
 		GridBagConstraints gbc_btnCreate = new GridBagConstraints();
 		gbc_btnCreate.gridx = 3;
 		gbc_btnCreate.gridy = 1;
 		pnlClassRoomSize.add(btnCreate, gbc_btnCreate);
 
 		pnlLoadAndSave = new JPanel();
 		tabClassDimensions.addTab("Load & Save", null, pnlLoadAndSave, null);
 		pnlLoadAndSave.setBorder(new TitledBorder(null, "Persistence", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		GridBagLayout gbl_pnlLoadAndSave = new GridBagLayout();
 		gbl_pnlLoadAndSave.columnWidths = new int[] { 50, 0 };
 		gbl_pnlLoadAndSave.rowHeights = new int[] { 24, 20, 0 };
 		gbl_pnlLoadAndSave.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
 		gbl_pnlLoadAndSave.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
 		pnlLoadAndSave.setLayout(gbl_pnlLoadAndSave);
 
 		btnLoadClass = new JButton();
 		GridBagConstraints gbc_btnLoadClass = new GridBagConstraints();
 		gbc_btnLoadClass.insets = new Insets(0, 0, 5, 0);
 		gbc_btnLoadClass.gridx = 0;
 		gbc_btnLoadClass.gridy = 0;
 		pnlLoadAndSave.add(btnLoadClass, gbc_btnLoadClass);
 
 		btnSaveClass = new JButton();
 		GridBagConstraints gbc_btnSaveClass = new GridBagConstraints();
 		gbc_btnSaveClass.anchor = GridBagConstraints.NORTH;
 		gbc_btnSaveClass.gridx = 0;
 		gbc_btnSaveClass.gridy = 1;
 		pnlLoadAndSave.add(btnSaveClass, gbc_btnSaveClass);
 
 		tabSettings = new JTabbedPane(JTabbedPane.TOP);
 		GridBagConstraints gbc_tabbedPane_1 = new GridBagConstraints();
 		gbc_tabbedPane_1.gridheight = 3;
 		gbc_tabbedPane_1.gridwidth = 2;
 		gbc_tabbedPane_1.fill = GridBagConstraints.BOTH;
 		gbc_tabbedPane_1.gridx = 3;
 		gbc_tabbedPane_1.gridy = 0;
 		add(tabSettings, gbc_tabbedPane_1);
 
 		panel_3 = new JPanel();
 		tabSettings.addTab("Load & Save", null, panel_3, null);
 		GridBagLayout gbl_panel_3 = new GridBagLayout();
 		gbl_panel_3.columnWidths = new int[] { 143, 1, 0 };
 		gbl_panel_3.rowHeights = new int[] { 1, 0, 0, 0, 0, 0 };
 		gbl_panel_3.columnWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
 		gbl_panel_3.rowWeights = new double[] { 1.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
 		panel_3.setLayout(gbl_panel_3);
 
 		scrollPane = new JScrollPane();
 		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
 		gbc_scrollPane.gridheight = 4;
 		gbc_scrollPane.insets = new Insets(0, 0, 0, 5);
 		gbc_scrollPane.fill = GridBagConstraints.BOTH;
 		gbc_scrollPane.gridx = 0;
 		gbc_scrollPane.gridy = 1;
 		panel_3.add(scrollPane, gbc_scrollPane);
 
 		lstSettings = new JList();
 		scrollPane.setViewportView(lstSettings);
 		lstSettings.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		lstSettings.setCellRenderer(new SettingsCellRenderer());
 		lstSettings.setModel(ConfigManager.instance());
 		lstSettings.setSelectedIndex(0);
 		lstSettings.getSelectionModel().addListSelectionListener(listSelectionListener);
 		lstSettings.setBorder(new LineBorder(new Color(0, 0, 0)));
 
 		btnSettingsLoad = new JButton(Factory.CommandLoadSettings);
 		btnSettingsLoad.setText("Load");
 		GridBagConstraints gbc_btnLoad = new GridBagConstraints();
 		gbc_btnLoad.fill = GridBagConstraints.HORIZONTAL;
 		gbc_btnLoad.insets = new Insets(0, 0, 5, 0);
 		gbc_btnLoad.gridx = 1;
 		gbc_btnLoad.gridy = 1;
 		panel_3.add(btnSettingsLoad, gbc_btnLoad);
 
 		btnSettingsSave = new JButton(Factory.CommandSaveSettings);
 		btnSettingsSave.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 			}
 		});
 		GridBagConstraints gbc_btnSave = new GridBagConstraints();
 		gbc_btnSave.fill = GridBagConstraints.HORIZONTAL;
 		gbc_btnSave.insets = new Insets(0, 0, 5, 0);
 		gbc_btnSave.gridx = 1;
 		gbc_btnSave.gridy = 2;
 		panel_3.add(btnSettingsSave, gbc_btnSave);
 
 		btnSettingsNew = new JButton(Factory.CommandNewSettings);
 		GridBagConstraints gbc_btnSettingsNew = new GridBagConstraints();
 		gbc_btnSettingsNew.fill = GridBagConstraints.HORIZONTAL;
 		gbc_btnSettingsNew.insets = new Insets(0, 0, 5, 0);
 		gbc_btnSettingsNew.gridx = 1;
 		gbc_btnSettingsNew.gridy = 3;
 		panel_3.add(btnSettingsNew, gbc_btnSettingsNew);
 
 		btnSettingsDelete = new JButton(Factory.CommandDeleteSettings);
 		btnSettingsDelete.setEnabled(false);
 		GridBagConstraints gbc_btnSettingsDelete = new GridBagConstraints();
 		gbc_btnSettingsDelete.anchor = GridBagConstraints.NORTH;
 		gbc_btnSettingsDelete.fill = GridBagConstraints.HORIZONTAL;
 		gbc_btnSettingsDelete.gridx = 1;
 		gbc_btnSettingsDelete.gridy = 4;
 		panel_3.add(btnSettingsDelete, gbc_btnSettingsDelete);
 
 		panel = new JPanel();
 		tabSettings.addTab("Generation", null, panel, null);
 		GridBagLayout gbl_panel = new GridBagLayout();
 		gbl_panel.columnWidths = new int[] { 20, 0, 80, 0, 0, 0 };
 		gbl_panel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
 		gbl_panel.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
 		gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
 		panel.setLayout(gbl_panel);
 
 		lblStrategy = new JLabel("Strategy:");
 		GridBagConstraints gbc_lblStrategy = new GridBagConstraints();
 		gbc_lblStrategy.gridheight = 2;
 		gbc_lblStrategy.anchor = GridBagConstraints.EAST;
 		gbc_lblStrategy.insets = new Insets(0, 0, 5, 5);
 		gbc_lblStrategy.gridx = 1;
 		gbc_lblStrategy.gridy = 0;
 		panel.add(lblStrategy, gbc_lblStrategy);
 
 		rdbCommaLambda = new JRadioButton("μ , λ");
 		rdbCommaLambda.setMnemonic('b');
 		GridBagConstraints gbc_rdbCommaLambda = new GridBagConstraints();
 		gbc_rdbCommaLambda.anchor = GridBagConstraints.WEST;
 		gbc_rdbCommaLambda.insets = new Insets(0, 0, 5, 5);
 		gbc_rdbCommaLambda.gridx = 2;
 		gbc_rdbCommaLambda.gridy = 0;
 		panel.add(rdbCommaLambda, gbc_rdbCommaLambda);
 
 		panel_4 = new JPanel();
 		panel_4.setBorder(new TitledBorder(null, "Additional Selection Methods", TitledBorder.LEADING, TitledBorder.TOP, null,
 				null));
 		GridBagConstraints gbc_panel_4 = new GridBagConstraints();
 		gbc_panel_4.insets = new Insets(0, 0, 5, 0);
 		gbc_panel_4.gridheight = 4;
 		gbc_panel_4.fill = GridBagConstraints.BOTH;
 		gbc_panel_4.gridx = 4;
 		gbc_panel_4.gridy = 0;
 		panel.add(panel_4, gbc_panel_4);
 		GridBagLayout gbl_panel_4 = new GridBagLayout();
 		gbl_panel_4.columnWidths = new int[] { 59, 49, 0 };
 		gbl_panel_4.rowHeights = new int[] { 23, 14, 0, 0 };
 		gbl_panel_4.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
 		gbl_panel_4.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
 		panel_4.setLayout(gbl_panel_4);
 
 		rdbNormal = new JRadioButton("Normal");
 		rdbNormal.setToolTipText("Every parent may create the same amount of descendants");
 		rdbNormal.setSelected(true);
 		GridBagConstraints gbc_rdbNormal = new GridBagConstraints();
 		gbc_rdbNormal.anchor = GridBagConstraints.NORTHWEST;
 		gbc_rdbNormal.insets = new Insets(0, 0, 5, 5);
 		gbc_rdbNormal.gridx = 0;
 		gbc_rdbNormal.gridy = 0;
 		panel_4.add(rdbNormal, gbc_rdbNormal);
 
 		rdbRank = new JRadioButton("Rank");
 		rdbRank.setToolTipText("The best may create the most children (linear).");
 		GridBagConstraints gbc_rdbRank = new GridBagConstraints();
 		gbc_rdbRank.anchor = GridBagConstraints.NORTHWEST;
 		gbc_rdbRank.insets = new Insets(0, 0, 5, 5);
 		gbc_rdbRank.gridx = 0;
 		gbc_rdbRank.gridy = 1;
 		panel_4.add(rdbRank, gbc_rdbRank);
 
 		rdbTournament = new JRadioButton("Tournament");
 		GridBagConstraints gbc_rdbTournament = new GridBagConstraints();
 		gbc_rdbTournament.anchor = GridBagConstraints.NORTHWEST;
 		gbc_rdbTournament.insets = new Insets(0, 0, 0, 5);
 		gbc_rdbTournament.gridx = 0;
 		gbc_rdbTournament.gridy = 2;
 		panel_4.add(rdbTournament, gbc_rdbTournament);
 
 		spnTournamentSize = new JSpinner();
 		spnTournamentSize.setEnabled(false);
 		spnTournamentSize.setToolTipText("Pick the ONE best out of this many randomly selected parents.");
 		spnTournamentSize.setModel(new SpinnerNumberModel(5, 1, 10000, 1));
 		GridBagConstraints gbc_spnTournamentSize = new GridBagConstraints();
 		gbc_spnTournamentSize.fill = GridBagConstraints.HORIZONTAL;
 		gbc_spnTournamentSize.gridx = 1;
 		gbc_spnTournamentSize.gridy = 2;
 		panel_4.add(spnTournamentSize, gbc_spnTournamentSize);
 
 		rdbPlusLambda = new JRadioButton("μ + λ");
 		rdbPlusLambda.setMnemonic('a');
 		rdbPlusLambda.setSelected(true);
 		GridBagConstraints gbc_rdbPlusLambda = new GridBagConstraints();
 		gbc_rdbPlusLambda.anchor = GridBagConstraints.WEST;
 		gbc_rdbPlusLambda.insets = new Insets(0, 0, 5, 5);
 		gbc_rdbPlusLambda.gridx = 2;
 		gbc_rdbPlusLambda.gridy = 1;
 		panel.add(rdbPlusLambda, gbc_rdbPlusLambda);
 
 		lblParents_text = new JLabel("Parents (µ):");
 		GridBagConstraints gbc_lblParents_text = new GridBagConstraints();
 		gbc_lblParents_text.anchor = GridBagConstraints.EAST;
 		gbc_lblParents_text.insets = new Insets(0, 0, 5, 5);
 		gbc_lblParents_text.gridx = 1;
 		gbc_lblParents_text.gridy = 2;
 		panel.add(lblParents_text, gbc_lblParents_text);
 
 		spnParents = new JSpinner();
 		spnParents.setModel(new SpinnerNumberModel(1, 1, 100, 1));
 		GridBagConstraints gbc_spnParents = new GridBagConstraints();
 		gbc_spnParents.fill = GridBagConstraints.HORIZONTAL;
 		gbc_spnParents.insets = new Insets(0, 0, 5, 5);
 		gbc_spnParents.gridx = 2;
 		gbc_spnParents.gridy = 2;
 		panel.add(spnParents, gbc_spnParents);
 
 		lblDescendants_text = new JLabel("Descendants (λ):");
 		GridBagConstraints gbc_lblDescendants_text = new GridBagConstraints();
 		gbc_lblDescendants_text.anchor = GridBagConstraints.NORTHEAST;
 		gbc_lblDescendants_text.insets = new Insets(0, 0, 5, 5);
 		gbc_lblDescendants_text.gridx = 1;
 		gbc_lblDescendants_text.gridy = 3;
 		panel.add(lblDescendants_text, gbc_lblDescendants_text);
 
 		lblDescendants = new JLabel("0");
 		GridBagConstraints gbc_lblDescendants = new GridBagConstraints();
 		gbc_lblDescendants.anchor = GridBagConstraints.NORTH;
 		gbc_lblDescendants.insets = new Insets(0, 0, 5, 5);
 		gbc_lblDescendants.gridx = 2;
 		gbc_lblDescendants.gridy = 3;
 		panel.add(lblDescendants, gbc_lblDescendants);
 
 		panel_2 = new JPanel();
 		tabSettings.addTab("Mutation", null, panel_2, null);
 		GridBagLayout gbl_panel_2 = new GridBagLayout();
 		gbl_panel_2.columnWidths = new int[] { 30, 0, 50, 0, 60, 0, 30 };
 		gbl_panel_2.rowHeights = new int[] { 0, 0, 0, 0, 20, 0 };
 		gbl_panel_2.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0 };
 		gbl_panel_2.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
 		panel_2.setLayout(gbl_panel_2);
 
 		lblParents_1 = new JLabel("From");
 		GridBagConstraints gbc_lblParents_1 = new GridBagConstraints();
 		gbc_lblParents_1.anchor = GridBagConstraints.EAST;
 		gbc_lblParents_1.insets = new Insets(0, 0, 5, 5);
 		gbc_lblParents_1.gridx = 1;
 		gbc_lblParents_1.gridy = 0;
 		panel_2.add(lblParents_1, gbc_lblParents_1);
 
 		lblParents = new JLabel("0");
 		GridBagConstraints gbc_lblNumOfParents = new GridBagConstraints();
 		gbc_lblNumOfParents.insets = new Insets(0, 0, 5, 5);
 		gbc_lblNumOfParents.gridx = 2;
 		gbc_lblNumOfParents.gridy = 0;
 		panel_2.add(lblParents, gbc_lblNumOfParents);
 
 		lblDescendents = new JLabel("parents");
 		GridBagConstraints gbc_lblDescendents = new GridBagConstraints();
 		gbc_lblDescendents.insets = new Insets(0, 0, 5, 5);
 		gbc_lblDescendents.anchor = GridBagConstraints.WEST;
 		gbc_lblDescendents.gridx = 3;
 		gbc_lblDescendents.gridy = 0;
 		panel_2.add(lblDescendents, gbc_lblDescendents);
 
 		lblCreate = new JLabel("create");
 		GridBagConstraints gbc_lblCreate = new GridBagConstraints();
 		gbc_lblCreate.insets = new Insets(0, 0, 5, 5);
 		gbc_lblCreate.gridx = 1;
 		gbc_lblCreate.gridy = 1;
 		panel_2.add(lblCreate, gbc_lblCreate);
 
 		spnSwap = new JSpinner();
 		spnSwap.setModel(new SpinnerNumberModel(5, 0, 100, 1));
 		spnSwap.getModel().addChangeListener(new ChangeListener() {
 			@Override
 			public void stateChanged(ChangeEvent arg0) {
 				updateMutationDistribution();
 			}
 		});
 		gbc_spnSwap = new GridBagConstraints();
 		gbc_spnSwap.fill = GridBagConstraints.HORIZONTAL;
 		gbc_spnSwap.insets = new Insets(0, 0, 5, 5);
 		gbc_spnSwap.gridx = 2;
 		gbc_spnSwap.gridy = 1;
 		panel_2.add(spnSwap, gbc_spnSwap);
 
 		lblDescendantsUsingSwap = new JLabel("<html>descendants per parent using <b>swap</b> with</html>");
 		GridBagConstraints gbc_lblDescendantsUsingSwap = new GridBagConstraints();
 		gbc_lblDescendantsUsingSwap.anchor = GridBagConstraints.WEST;
 		gbc_lblDescendantsUsingSwap.insets = new Insets(0, 0, 5, 5);
 		gbc_lblDescendantsUsingSwap.gridx = 3;
 		gbc_lblDescendantsUsingSwap.gridy = 1;
 		panel_2.add(lblDescendantsUsingSwap, gbc_lblDescendantsUsingSwap);
 
 		spnNumberOfSwaps = new JSpinner();
 		spnNumberOfSwaps.getModel().addChangeListener(new ChangeListener() {
 			@Override
 			public void stateChanged(ChangeEvent e) {
 				ConfigManager.instance().getCurrentConfig().setNumberOfSwaps((Integer) spnNumberOfSwaps.getValue());
 				if (blockUpdates) {
 					return;
 				}
 				ConfigManager.instance().currentConfigUpdated();
 			}
 		});
 		GridBagConstraints gbc_spnNumberOfSwaps = new GridBagConstraints();
 		gbc_spnNumberOfSwaps.fill = GridBagConstraints.HORIZONTAL;
 		gbc_spnNumberOfSwaps.insets = new Insets(0, 0, 5, 5);
 		gbc_spnNumberOfSwaps.gridx = 4;
 		gbc_spnNumberOfSwaps.gridy = 1;
 		panel_2.add(spnNumberOfSwaps, gbc_spnNumberOfSwaps);
 
 		lblSwapOperations = new JLabel("swap operations,");
 		GridBagConstraints gbc_lblSwapOperations = new GridBagConstraints();
 		gbc_lblSwapOperations.anchor = GridBagConstraints.WEST;
 		gbc_lblSwapOperations.insets = new Insets(0, 0, 5, 5);
 		gbc_lblSwapOperations.gridx = 5;
 		gbc_lblSwapOperations.gridy = 1;
 		panel_2.add(lblSwapOperations, gbc_lblSwapOperations);
 
 		lblCreate_1 = new JLabel("create");
 		GridBagConstraints gbc_lblCreate_1 = new GridBagConstraints();
 		gbc_lblCreate_1.insets = new Insets(0, 0, 5, 5);
 		gbc_lblCreate_1.gridx = 1;
 		gbc_lblCreate_1.gridy = 2;
 		panel_2.add(lblCreate_1, gbc_lblCreate_1);
 
 		spnInversion = new JSpinner();
 		spnInversion.setModel(new SpinnerNumberModel(5, 0, 100, 1));
 		spnInversion.getModel().addChangeListener(new ChangeListener() {
 
 			@Override
 			public void stateChanged(ChangeEvent arg0) {
 				updateMutationDistribution();
 			}
 		});
 		gbc_spnInversion = new GridBagConstraints();
 		gbc_spnInversion.insets = new Insets(0, 0, 5, 5);
 		gbc_spnInversion.fill = GridBagConstraints.HORIZONTAL;
 		gbc_spnInversion.gridx = 2;
 		gbc_spnInversion.gridy = 2;
 		panel_2.add(spnInversion, gbc_spnInversion);
 
 		lblDescendantsUsingInversion = new JLabel("<html>descendants per parent using <b>inversion</b> with</html>");
 		GridBagConstraints gbc_lblDescendantsUsingInversion = new GridBagConstraints();
 		gbc_lblDescendantsUsingInversion.anchor = GridBagConstraints.WEST;
 		gbc_lblDescendantsUsingInversion.insets = new Insets(0, 0, 5, 5);
 		gbc_lblDescendantsUsingInversion.gridx = 3;
 		gbc_lblDescendantsUsingInversion.gridy = 2;
 		panel_2.add(lblDescendantsUsingInversion, gbc_lblDescendantsUsingInversion);
 
 		spnNumberOfInversions = new JSpinner();
 		spnNumberOfInversions.getModel().addChangeListener(new ChangeListener() {
 			@Override
 			public void stateChanged(ChangeEvent e) {
 				ConfigManager.instance().getCurrentConfig().setNumberOfInversions((Integer) spnNumberOfInversions.getValue());
 				if (blockUpdates) {
 					return;
 				}
 				ConfigManager.instance().currentConfigUpdated();
 			}
 		});
 		GridBagConstraints gbc_spnNumberOfInversions = new GridBagConstraints();
 		gbc_spnNumberOfInversions.fill = GridBagConstraints.HORIZONTAL;
 		gbc_spnNumberOfInversions.insets = new Insets(0, 0, 5, 5);
 		gbc_spnNumberOfInversions.gridx = 4;
 		gbc_spnNumberOfInversions.gridy = 2;
 		panel_2.add(spnNumberOfInversions, gbc_spnNumberOfInversions);
 
 		lblInversionOperations = new JLabel("inversion operations.");
 		GridBagConstraints gbc_lblInversionOperations = new GridBagConstraints();
 		gbc_lblInversionOperations.anchor = GridBagConstraints.WEST;
 		gbc_lblInversionOperations.insets = new Insets(0, 0, 5, 5);
 		gbc_lblInversionOperations.gridx = 5;
 		gbc_lblInversionOperations.gridy = 2;
 		panel_2.add(lblInversionOperations, gbc_lblInversionOperations);
 
 		lblTotal = new JLabel("Resulting in a total of");
 		GridBagConstraints gbc_lblTotal = new GridBagConstraints();
 		gbc_lblTotal.anchor = GridBagConstraints.EAST;
 		gbc_lblTotal.gridwidth = 3;
 		gbc_lblTotal.insets = new Insets(0, 0, 5, 5);
 		gbc_lblTotal.gridx = 1;
 		gbc_lblTotal.gridy = 3;
 		panel_2.add(lblTotal, gbc_lblTotal);
 
 		lblSwap = new JLabel("0");
 		GridBagConstraints gbc_lblInversion = new GridBagConstraints();
 		gbc_lblInversion.insets = new Insets(0, 0, 5, 5);
 		gbc_lblInversion.gridx = 4;
 		gbc_lblInversion.gridy = 3;
 		panel_2.add(lblSwap, gbc_lblInversion);
 
 		lblPerDescnendant = new JLabel("<html>children using <b>swap</b></html>");
 		GridBagConstraints gbc_lblPerDescnendant = new GridBagConstraints();
 		gbc_lblPerDescnendant.anchor = GridBagConstraints.WEST;
 		gbc_lblPerDescnendant.insets = new Insets(0, 0, 5, 5);
 		gbc_lblPerDescnendant.gridx = 5;
 		gbc_lblPerDescnendant.gridy = 3;
 		panel_2.add(lblPerDescnendant, gbc_lblPerDescnendant);
 
 		lblNewLabel_1 = new JLabel("and");
 		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
 		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
 		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
 		gbc_lblNewLabel_1.gridx = 3;
 		gbc_lblNewLabel_1.gridy = 4;
 		panel_2.add(lblNewLabel_1, gbc_lblNewLabel_1);
 
 		lblInversion = new JLabel("0");
 		GridBagConstraints gbc_lblSwap = new GridBagConstraints();
 		gbc_lblSwap.insets = new Insets(0, 0, 5, 5);
 		gbc_lblSwap.gridx = 4;
 		gbc_lblSwap.gridy = 4;
 		panel_2.add(lblInversion, gbc_lblSwap);
 
 		lblNewLabel = new JLabel("<html>children using <b>inversion</b></html>");
 		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
 		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
 		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
 		gbc_lblNewLabel.gridx = 5;
 		gbc_lblNewLabel.gridy = 4;
 		panel_2.add(lblNewLabel, gbc_lblNewLabel);
 
 		lblTheNewGeneration = new JLabel("The new generation has a population of");
 		GridBagConstraints gbc_lblTheNewGeneration = new GridBagConstraints();
 		gbc_lblTheNewGeneration.gridwidth = 2;
 		gbc_lblTheNewGeneration.anchor = GridBagConstraints.EAST;
 		gbc_lblTheNewGeneration.insets = new Insets(0, 0, 0, 5);
 		gbc_lblTheNewGeneration.gridx = 2;
 		gbc_lblTheNewGeneration.gridy = 5;
 		panel_2.add(lblTheNewGeneration, gbc_lblTheNewGeneration);
 
 		lblDescendants1 = new JLabel("10");
 		GridBagConstraints gbc_lblDescendants1 = new GridBagConstraints();
 		gbc_lblDescendants1.insets = new Insets(0, 0, 0, 5);
 		gbc_lblDescendants1.gridx = 4;
 		gbc_lblDescendants1.gridy = 5;
 		panel_2.add(lblDescendants1, gbc_lblDescendants1);
 
 		panel_1 = new JPanel();
 		tabSettings.addTab("Weighting", null, panel_1, null);
 		GridBagLayout gbl_panel_1 = new GridBagLayout();
 		gbl_panel_1.columnWidths = new int[] { 50, 50, 50, 50, 50, 0, 10 };
 		gbl_panel_1.rowHeights = new int[] { 25, 0, 0, 0, 0, 0, 0, 10 };
 		gbl_panel_1.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
 		gbl_panel_1.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE };
 		panel_1.setLayout(gbl_panel_1);
 
 		lblDiagonal1 = new JLabel("0");
 		GridBagConstraints gbc_lblDiagonal1 = new GridBagConstraints();
 		gbc_lblDiagonal1.insets = new Insets(0, 0, 5, 5);
 		gbc_lblDiagonal1.gridx = 1;
 		gbc_lblDiagonal1.gridy = 1;
 		panel_1.add(lblDiagonal1, gbc_lblDiagonal1);
 
 		lblTop = new JLabel("0");
 		GridBagConstraints gbc_lblTop = new GridBagConstraints();
 		gbc_lblTop.fill = GridBagConstraints.HORIZONTAL;
 		gbc_lblTop.insets = new Insets(0, 0, 5, 5);
 		gbc_lblTop.gridx = 2;
 		gbc_lblTop.gridy = 1;
 		panel_1.add(lblTop, gbc_lblTop);
 
 		lblDiagonal3 = new JLabel("0");
 		GridBagConstraints gbc_lblDiagonal3 = new GridBagConstraints();
 		gbc_lblDiagonal3.anchor = GridBagConstraints.SOUTH;
 		gbc_lblDiagonal3.insets = new Insets(0, 0, 5, 5);
 		gbc_lblDiagonal3.gridx = 3;
 		gbc_lblDiagonal3.gridy = 1;
 		panel_1.add(lblDiagonal3, gbc_lblDiagonal3);
 
 		lblLeft2 = new JLabel("0");
 		GridBagConstraints gbc_lblLeft2 = new GridBagConstraints();
 		gbc_lblLeft2.anchor = GridBagConstraints.EAST;
 		gbc_lblLeft2.insets = new Insets(0, 0, 5, 5);
 		gbc_lblLeft2.gridx = 0;
 		gbc_lblLeft2.gridy = 2;
 		panel_1.add(lblLeft2, gbc_lblLeft2);
 
 		lblLeft = new JLabel("0");
 		GridBagConstraints gbc_lblLeft = new GridBagConstraints();
 		gbc_lblLeft.insets = new Insets(0, 0, 5, 5);
 		gbc_lblLeft.gridx = 1;
 		gbc_lblLeft.gridy = 2;
 		panel_1.add(lblLeft, gbc_lblLeft);
 
 		lblStudent = new JLabel("Student");
 		GridBagConstraints gbc_lblStudent = new GridBagConstraints();
 		gbc_lblStudent.fill = GridBagConstraints.HORIZONTAL;
 		gbc_lblStudent.insets = new Insets(0, 0, 5, 5);
 		gbc_lblStudent.gridx = 2;
 		gbc_lblStudent.gridy = 2;
 		panel_1.add(lblStudent, gbc_lblStudent);
 
 		spnRight = new JSpinner();
 		spnRight.setModel(new SpinnerNumberModel(5, 0, 100, 1));
 		spnRight.getModel().addChangeListener(weightingChangedListener);
 		GridBagConstraints gbc_spnRight = new GridBagConstraints();
 		gbc_spnRight.fill = GridBagConstraints.HORIZONTAL;
 		gbc_spnRight.insets = new Insets(0, 0, 5, 5);
 		gbc_spnRight.gridx = 3;
 		gbc_spnRight.gridy = 2;
 		panel_1.add(spnRight, gbc_spnRight);
 
 		spnRight2 = new JSpinner();
 		spnRight2.setModel(new SpinnerNumberModel(1, 0, 100, 1));
 		spnRight2.getModel().addChangeListener(weightingChangedListener);
 		gbc_spnRight2 = new GridBagConstraints();
 		gbc_spnRight2.fill = GridBagConstraints.HORIZONTAL;
 		gbc_spnRight2.insets = new Insets(0, 0, 5, 5);
 		gbc_spnRight2.gridx = 4;
 		gbc_spnRight2.gridy = 2;
 		panel_1.add(spnRight2, gbc_spnRight2);
 
 		lblDiagonal2 = new JLabel("0");
 		GridBagConstraints gbc_lblDiagonal2 = new GridBagConstraints();
 		gbc_lblDiagonal2.anchor = GridBagConstraints.NORTH;
 		gbc_lblDiagonal2.insets = new Insets(0, 0, 5, 5);
 		gbc_lblDiagonal2.gridx = 1;
 		gbc_lblDiagonal2.gridy = 3;
 		panel_1.add(lblDiagonal2, gbc_lblDiagonal2);
 
 		spnBottom = new JSpinner();
 		spnBottom.setModel(new SpinnerNumberModel(2, 0, 100, 1));
 		spnBottom.getModel().addChangeListener(weightingChangedListener);
 		GridBagConstraints gbc_spnBottom = new GridBagConstraints();
 		gbc_spnBottom.fill = GridBagConstraints.HORIZONTAL;
 		gbc_spnBottom.anchor = GridBagConstraints.NORTH;
 		gbc_spnBottom.insets = new Insets(0, 0, 5, 5);
 		gbc_spnBottom.gridx = 2;
 		gbc_spnBottom.gridy = 3;
 		panel_1.add(spnBottom, gbc_spnBottom);
 
 		spnDiagonal = new JSpinner();
 		spnDiagonal.setModel(new SpinnerNumberModel(1, 0, 100, 1));
 		spnDiagonal.getModel().addChangeListener(weightingChangedListener);
 		GridBagConstraints gbc_spnDiagonal = new GridBagConstraints();
 		gbc_spnDiagonal.fill = GridBagConstraints.HORIZONTAL;
 		gbc_spnDiagonal.anchor = GridBagConstraints.NORTH;
 		gbc_spnDiagonal.insets = new Insets(0, 0, 5, 5);
 		gbc_spnDiagonal.gridx = 3;
 		gbc_spnDiagonal.gridy = 3;
 		panel_1.add(spnDiagonal, gbc_spnDiagonal);
 
 		panel_5 = new JPanel();
 		GridBagConstraints gbc_panel_5 = new GridBagConstraints();
 		gbc_panel_5.insets = new Insets(0, 0, 5, 5);
 		gbc_panel_5.fill = GridBagConstraints.BOTH;
 		gbc_panel_5.gridx = 2;
 		gbc_panel_5.gridy = 4;
 		panel_1.add(panel_5, gbc_panel_5);
 
 		lblPriority = new JLabel("Priority:");
 		GridBagConstraints gbc_lblPriority = new GridBagConstraints();
 		gbc_lblPriority.insets = new Insets(0, 0, 5, 5);
 		gbc_lblPriority.gridx = 1;
 		gbc_lblPriority.gridy = 5;
 		panel_1.add(lblPriority, gbc_lblPriority);
 
 		spnPriority = new JSpinner();
 		spnPriority.setModel(new SpinnerNumberModel(5, 0, 100, 1));
 		spnPriority.getModel().addChangeListener(weightingChangedListener);
 		GridBagConstraints gbc_spnPriority = new GridBagConstraints();
 		gbc_spnPriority.fill = GridBagConstraints.HORIZONTAL;
 		gbc_spnPriority.insets = new Insets(0, 0, 5, 5);
 		gbc_spnPriority.gridx = 2;
 		gbc_spnPriority.gridy = 5;
 		panel_1.add(spnPriority, gbc_spnPriority);
 
 		lblDistanceToBlackboard = new JLabel("distance to blackboard");
 		GridBagConstraints gbc_lblDistanceToBlackboard = new GridBagConstraints();
 		gbc_lblDistanceToBlackboard.anchor = GridBagConstraints.WEST;
 		gbc_lblDistanceToBlackboard.gridwidth = 3;
 		gbc_lblDistanceToBlackboard.insets = new Insets(0, 0, 5, 5);
 		gbc_lblDistanceToBlackboard.gridx = 3;
 		gbc_lblDistanceToBlackboard.gridy = 5;
 		panel_1.add(lblDistanceToBlackboard, gbc_lblDistanceToBlackboard);
 
 		pnlBlackboard = new JPanel();
 		pnlBlackboard.setBackground(Blackboard.DEFAULT_BLACKBOARD_COLOR);
 		GridBagConstraints gbc_pnlBlackboard = new GridBagConstraints();
 		gbc_pnlBlackboard.insets = new Insets(0, 0, 0, 5);
 		gbc_pnlBlackboard.anchor = GridBagConstraints.NORTH;
 		gbc_pnlBlackboard.gridwidth = 5;
 		gbc_pnlBlackboard.fill = GridBagConstraints.HORIZONTAL;
 		gbc_pnlBlackboard.gridx = 0;
 		gbc_pnlBlackboard.gridy = 6;
 		panel_1.add(pnlBlackboard, gbc_pnlBlackboard);
 
 		progressBar = new JProgressBar();
 		progressBar.setVisible(false);
 		GridBagConstraints gbc_progressBar = new GridBagConstraints();
 		gbc_progressBar.gridwidth = 3;
 		gbc_progressBar.insets = new Insets(0, 0, 5, 5);
 		gbc_progressBar.gridx = 0;
 		gbc_progressBar.gridy = 1;
 		add(progressBar, gbc_progressBar);
 
 		lblNumberOfGenerations = new JLabel("Number of Generations:");
 		GridBagConstraints gbc_lblNumberOfGenerations = new GridBagConstraints();
 		gbc_lblNumberOfGenerations.anchor = GridBagConstraints.EAST;
 		gbc_lblNumberOfGenerations.insets = new Insets(0, 0, 0, 5);
 		gbc_lblNumberOfGenerations.gridx = 0;
 		gbc_lblNumberOfGenerations.gridy = 2;
 		add(lblNumberOfGenerations, gbc_lblNumberOfGenerations);
 
 		spnNumberOfGenerations = new JSpinner();
 		GridBagConstraints gbc_spnNumberOfGenerations = new GridBagConstraints();
 		gbc_spnNumberOfGenerations.fill = GridBagConstraints.HORIZONTAL;
 		gbc_spnNumberOfGenerations.insets = new Insets(0, 0, 0, 5);
 		gbc_spnNumberOfGenerations.gridx = 1;
 		gbc_spnNumberOfGenerations.gridy = 2;
 		add(spnNumberOfGenerations, gbc_spnNumberOfGenerations);
		spnNumberOfGenerations.setModel(new SpinnerNumberModel(10, 1, 1000000, 1));
 
 		btnNewSeatingPlan = new JButton();
 		GridBagConstraints gbc_btnNewSeatingPlan = new GridBagConstraints();
 		gbc_btnNewSeatingPlan.insets = new Insets(0, 0, 0, 5);
 		gbc_btnNewSeatingPlan.anchor = GridBagConstraints.WEST;
 		gbc_btnNewSeatingPlan.gridx = 2;
 		gbc_btnNewSeatingPlan.gridy = 2;
 		add(btnNewSeatingPlan, gbc_btnNewSeatingPlan);
 		addActionHandlers();
 		EAFactory.setNumberOfGenerations((Integer) spnNumberOfGenerations.getValue());
 
 		createRadioButtonGroup();
 		storeAllConfigurationOptions();
 		ConfigManager.instance().setCurrentConfigIndex(0);
 	}
 
 	private void storeAllConfigurationOptions() {
 		Configuration conf = ConfigManager.instance().getCurrentConfig();
 		conf.setDescendentsUsingInversion((Integer) spnInversion.getValue());
 		conf.setDescendantsUsingSwap((Integer) spnSwap.getValue());
 		conf.setParents((Integer) spnParents.getValue());
 		conf.setWeightingRight((Integer) spnRight.getValue());
 		conf.setWeightingRight2((Integer) spnRight2.getValue());
 		conf.setWeightingBottom((Integer) spnBottom.getValue());
 		conf.setWeightingDiagonal((Integer) spnDiagonal.getValue());
 		conf.setWeightingPriority((Integer) spnPriority.getValue());
 		conf.setStrategy(getStrategy());
 	}
 
 	private SpinnerNumberModel createColSpinnerModel(ChangeListener classDimensionChangeListener) {
 		SpinnerNumberModel colSpinnerModel = new SpinnerNumberModel(new Short((short) Parameter.numCols), new Short((short) 1),
 				new Short((short) 20), new Short((short) 1));
 		colSpinnerModel.addChangeListener(classDimensionChangeListener);
 		return colSpinnerModel;
 	}
 
 	private SpinnerNumberModel createRowSpinnerModel(ChangeListener classDimensionChangeListener) {
 		SpinnerNumberModel rowSpinnerModel = new SpinnerNumberModel(new Short((short) Parameter.numRows), new Short((short) 1),
 				new Short((short) 20), new Short((short) 1));
 		rowSpinnerModel.addChangeListener(classDimensionChangeListener);
 		return rowSpinnerModel;
 	}
 
 	public void initView() {
 		btnNewSeatingPlan.setAction(Factory.CommandNewSeatingPlan);
 		btnLoadClass.setAction(Factory.CommandLoadClass);
 		btnSaveClass.setAction(Factory.CommandSaveClass);
 		btnCreate.setAction(Factory.CommandNewClass);
 		btnSettingsLoad.setAction(Factory.CommandLoadSettings);
 		btnSettingsSave.setAction(Factory.CommandSaveSettings);
 		btnSettingsNew.setAction(Factory.CommandNewSettings);
 		btnSettingsDelete.setAction(Factory.CommandDeleteSettings);
 	}
 
 	public static ToolsPanel instance() {
 		return _instance;
 	}
 
 	public void setClassRoomDimensions(Dimension dim, short size) {
 		blockUpdates = true;
 		numCols.setValue((short) dim.getWidth());
 		numRows.setValue((short) dim.getHeight());
 
 		Short maximum = (short) (((Short) numRows.getValue()) * (Short) numCols.getValue());
 		((SpinnerNumberModel) classSize.getModel()).setMaximum(maximum);
 		blockUpdates = false;
 		classSize.setValue(size);
 	}
 
 	public int getClassSize() {
 		return (Short) classSize.getValue();
 	}
 
 	public void setButtonNewConfigEnabled(boolean b) {
 		btnSettingsNew.setEnabled(b);
 	}
 
 	public void setButtonDeleteConfigEnabled(boolean b) {
 		btnSettingsDelete.setEnabled(b);
 	}
 
 	public void selectConfiguration(int index) {
 		lstSettings.setSelectedIndex(index);
 	}
 
 	public void setProgress(int i, int max) {
 		final boolean progressbarVisible = i <= max - 2;
 		btnNewSeatingPlan.setEnabled(!progressbarVisible);
 		progressBar.setVisible(progressbarVisible);
 		progressBar.setMaximum(max);
 		progressBar.setValue(i);
 	}
 
 }
