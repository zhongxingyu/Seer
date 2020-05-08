 package admin.panel.general;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.util.Arrays;
 import java.util.EnumSet;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.border.BevelBorder;
 import javax.swing.border.Border;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.table.TableModel;
 
 import admin.MainFrame;
 import admin.Utils;
 import admin.panel.person.contestant.ContestantPanel;
 import data.GameData;
 import data.GameData.UpdateTag;
 import data.Person;
 import data.User;
 
 public class GeneralPanel extends JPanel implements Observer {
 
 	private static final long serialVersionUID = 1L;
 
 	Integer viewWeek = 0;
 
 	private JLabel lblWeek;
 	
 	private JTextField txtTribe1;
 	private JTextField txtTribe2;
 
 	private JButton btnStartSn;
 	private JButton btnAdvWk;
 	private JButton btnChangeTribeName;
 
 	private SpinnerNumberModel weekModel; // default,low,min,step
 	private JSpinner spnWeek;
 
 	private JPanel pnlRemCons;
 	private JPanel pnlCastOffs;
 
 	private JPanel pnlTribes;
 	private JPanel pnlWinners;
 	private TribeWinnerCont contTribeWin;
 	
 	private JPanel pnlWeekCtrl;
 	private JPanel pnlHistory;
 
 	private JPanel pnlCenter;
 
 	private JLabel lblTribe1;
 	private JLabel lblTribe2;
 	
 	private JTable activeTable;
 	private JTable castTable;
 
 	protected static final String TOOL_START = "Start the Season",
 				TOOL_ADV = "Advance the Season week", 
 				TOOL_FIN_ADV = "Advance the final week",
 				TOOL_TRIBE = "Change the tribe names",
 				TOOL_WKSPINNER = "Scroll through the weeks";
 	
 	
 	
 	public GeneralPanel() {
 		setLayout(new BorderLayout(10, 10));
 
 		pnlTribes = buildTribePanel();
 		pnlWinners = buildWinnerPanel();
 		contTribeWin = new TribeWinnerCont(pnlTribes, pnlWinners, true);
 		
 		pnlWeekCtrl = buildCtrlPanel();
 		pnlHistory = buildHistory();
 
 		pnlCenter = new JPanel();
 		pnlCenter.setLayout(new BoxLayout(pnlCenter, BoxLayout.PAGE_AXIS));
 
 		JPanel subFrame = new JPanel();
 		subFrame.setLayout(new GridLayout(1, 2, 10, 5));
 		
 		subFrame.add(pnlWeekCtrl);
 		subFrame.add(contTribeWin);
 
 		pnlCenter.add(subFrame);
 		pnlCenter.add(Box.createVerticalStrut(10));
 		pnlCenter.add(pnlHistory);
 
 		add(pnlCenter, BorderLayout.CENTER);
 		GameData.getCurrentGame().addObserver(this);
 		
 		setToolTips();
 		
 		initListeners();
 	}
 
 	/**
 	 * Sets the tool tips for all the components.
 	 */
 	protected void setToolTips() {
 
 		if (GameData.getCurrentGame().isFinalWeek())
 			btnAdvWk.setToolTipText(TOOL_ADV);
 		else
 			btnAdvWk.setToolTipText(TOOL_FIN_ADV);
 			
 		btnStartSn.setToolTipText(TOOL_START);
 		btnChangeTribeName.setToolTipText(TOOL_TRIBE);
 		spnWeek.setToolTipText(TOOL_WKSPINNER);
 		txtTribe1.setToolTipText(TOOL_TRIBE);
 		txtTribe2.setToolTipText(TOOL_TRIBE);
 		lblTribe1.setToolTipText(TOOL_TRIBE);
 		lblTribe2.setToolTipText(TOOL_TRIBE);
 	}
 	
 	private JPanel buildTribePanel() {
 		GameData g = GameData.getCurrentGame();
 
 		JPanel pane = new JPanel();
 		pane.setBorder(BorderFactory.createTitledBorder("Tribes"));
 		pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
 		lblTribe1 = new JLabel("Tribe 1:");
 		lblTribe2 = new JLabel("Tribe 2:");
 
 		txtTribe1 = new JTextField();
 		txtTribe2 = new JTextField();
 
 		btnChangeTribeName = new JButton("Save Tribes");
 
 		List<JLabel> lbls = Arrays.asList(lblTribe1, lblTribe2);
 		List<JTextField> tfs = Arrays.asList(txtTribe1, txtTribe2);
 		String[] tribes = g.getTribeNames();
 
 		for (int i = 0; i < 2; i++) {
 			tfs.get(i).setText(tribes[i]);
 
 			JPanel tPane = new JPanel();
 			tPane.setLayout(new BoxLayout(tPane, BoxLayout.LINE_AXIS));
 
 			tPane.add(lbls.get(i));
 			tPane.add(Box.createHorizontalGlue());
 			tPane.add(tfs.get(i));
 
 			pane.add(tPane);
 			pane.add(Box.createVerticalGlue());
 		}
 
 		btnChangeTribeName.setAlignmentX(JButton.RIGHT_ALIGNMENT);
 		pane.add(btnChangeTribeName);
 
 		return pane;
 	}
 	
 	protected JPanel buildWinnerPanel() {
 		JPanel pane = new JPanel();
 		pane.setBorder(BorderFactory.createTitledBorder("Winners"));
 		pane.setLayout(new BorderLayout(5, 5));
 		
 		JLabel stubLabel = new JLabel("STUBBB");
 		pane.add(stubLabel, BorderLayout.CENTER);
 		
 		return pane;
 	}
 	
 	private JPanel buildCtrlPanel() {
 		JPanel pane = new JPanel();
 		pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
 		pane.setBorder(BorderFactory.createTitledBorder("Time"));
 
 		btnStartSn = new JButton("Start Season");
 		btnStartSn.setAlignmentX(JButton.CENTER_ALIGNMENT);
 		btnAdvWk = new JButton("Advance Week");
 		btnAdvWk.setAlignmentX(JButton.CENTER_ALIGNMENT);
 
 		btnStartSn.setPreferredSize(btnAdvWk.getPreferredSize());
 
 		// put into the panel
 		pane.add(Box.createVerticalGlue());
 		pane.add(btnStartSn);
 		pane.add(Box.createVerticalStrut(10));
 		pane.add(btnAdvWk);
 		pane.add(Box.createVerticalGlue());
 
 		// / init:
 		GameData g = GameData.getCurrentGame();
 		btnStartSn.setEnabled(!g.isSeasonStarted());
 		btnAdvWk.setEnabled(g.isSeasonStarted() && !g.isSeasonEnded());
 
 		return pane;
 	}
 
 	private JPanel buildHistory() {
 		JPanel pane = new JPanel();
 		pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
 		pane.setBorder(BorderFactory.createTitledBorder("History"));
 
 		// / spinnner stuff
 		JPanel pnlSpin = new JPanel();
 		pnlSpin.setLayout(new BoxLayout(pnlSpin, BoxLayout.LINE_AXIS));
 		lblWeek = new JLabel("View Week:");
 		lblWeek.setAlignmentX(JLabel.LEFT_ALIGNMENT);
 		weekModel = new SpinnerNumberModel();
 		spnWeek = new JSpinner(weekModel);
 		spnWeek.setAlignmentX(JSpinner.LEFT_ALIGNMENT);
 		spnWeek.setEnabled(false);
 		
 		pnlSpin.add(lblWeek);
 		pnlSpin.add(Box.createHorizontalStrut(10));
 		pnlSpin.add(spnWeek);
 		pnlSpin.add(Box.createHorizontalGlue());
 
 		// / panels
 		JPanel pnlCont = new JPanel();
 		pnlCont.setLayout(new GridLayout(1, 2, 10, 5));
 		
 		buildHistoryTables();
 
 		pnlCont.add(pnlRemCons);
 		pnlCont.add(pnlCastOffs);
 
 		// put all together:
 		pane.add(pnlSpin);
 		pane.add(pnlCont);
 
 		/// init:
 		GameData g = GameData.getCurrentGame();
 		if (g.isSeasonStarted()) {
 			updateSpinnerModel(g.getCurrentWeek());
 			spnWeek.setValue(g.getCurrentWeek());
 		}
 
 		return pane;
 	}
 	
 	private void buildHistoryTables() {
 		// build the encapsulating panels:
 		final Border bevB = BorderFactory
 				.createSoftBevelBorder(BevelBorder.LOWERED);
 		final Dimension displaySize = new Dimension(150, 200);
 
 		pnlRemCons = new JPanel();
 		pnlRemCons.setBorder(BorderFactory.createTitledBorder(bevB,
 				"Contestants"));
 		pnlRemCons.setPreferredSize(displaySize);
 
 		pnlCastOffs = new JPanel();
 		pnlCastOffs.setBorder(BorderFactory.createTitledBorder(bevB,
 				"Cast Offs"));
 		pnlCastOffs.setPreferredSize(displaySize);
 
 		// Tables:
 		activeTable = new JTable();
 		TableModel model = new HistoryConModel(activeTable, false);
 		activeTable.setModel(model);
 
 		castTable = new JTable();
 		model = new HistoryConModel(castTable, true);
 		castTable.setModel(model);
 
 		List<JTable> tables = Arrays.asList(activeTable, castTable);
 		List<JPanel> panes = Arrays.asList(pnlRemCons, pnlCastOffs);
 		for (int i = 0; i < 2; i++) {
 			JTable table = tables.get(i);
 			JPanel pane = panes.get(i);
 
 			table.getTableHeader().setReorderingAllowed(false); // no moving.
 			table.setColumnSelectionAllowed(true);
 			table.setRowSelectionAllowed(true);
 			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 
 			table.setDefaultRenderer(Object.class, Utils.buildDefaultRenderer());
 
 			JScrollPane scroll = new JScrollPane(table);
 
 			pane.setLayout(new BorderLayout(5, 5));
 			pane.add(scroll, BorderLayout.CENTER);
 		}
 	}
 
 	private void initListeners() {
 		btnStartSn.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				GameData g = GameData.getCurrentGame();
 
 				if (g.getAllContestants().size() == 0) {
 					JOptionPane
 							.showMessageDialog(null,
 									"This will be a boring game with no contestants.  Add some!");
 					return;
 				}
 
 				if (g.getAllContestants().size() != g.getInitialContestants()) {
 					JOptionPane.showMessageDialog(null,
 							"You need to have " + g.getInitialContestants()
 									+ " contestants to start.");
 					return;
 					
 				} else if (!g.isSeasonStarted()) {
 					
 					String s = JOptionPane.showInputDialog("Enter weekly bet" +
 							" amount!");
 					
 					if (Utils.checkString(s, "^[0-9]+$")) {
 						int t = Integer.parseInt(s);
 						if (t >= 0) {
 							g.startSeason(t);
 						}
 						return;
 					}
 					
 					JOptionPane.showMessageDialog(null,
 							"Invalid amount entered.");
 				}
 			}
 		});
 
 		btnAdvWk.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				GameData g = GameData.getCurrentGame();
 
 				if (g.isSeasonEnded()) { // if end of game
 					return;
 				} 
 				
 				if (!g.doesElimExist()) {
 					if (g.isFinalWeek()) {
 						JOptionPane
 								.showMessageDialog(null,
 										"No Contestant has been selected to be the winner.");
 					} else {
 						JOptionPane
 								.showMessageDialog(null,
 										"No Contestant has been selected to be cast off.");
 					}
 				} else {
 					g.advanceWeek();
 				}
 			}
 		});
 
 		btnChangeTribeName.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				MainFrame mf = MainFrame.getRunningFrame();
 
 				if (!Utils.checkString(txtTribe1.getText(),
 						Person.TRIBE_PATTERN)) {
 					mf.setStatusErrorMsg("Tribe 1 name invalid.", txtTribe1);
 				} else if (!Utils.checkString(txtTribe2.getText(),
 						Person.TRIBE_PATTERN)) {
 					mf.setStatusErrorMsg("Tribe 2 name invalid.", txtTribe2);
 				} else if (txtTribe1.getText().equals(txtTribe2.getText())) {
 					mf.setStatusErrorMsg(
 							"Invalid tribe names, cannot be the same",
 							txtTribe1, txtTribe2);
 					return;
 				} else {
 					String[] txts = GameData.getCurrentGame().setTribeNames(
 							txtTribe1.getText(), txtTribe2.getText());
 
 					txtTribe1.setText(txts[0]);
 					txtTribe2.setText(txts[1]);
 				}
 			}
 
 		});
 
 		spnWeek.addChangeListener(new ChangeListener() {
 
 			@Override
 			public void stateChanged(ChangeEvent ce) {
 				int w = (Integer)spnWeek.getValue();
 				((HistoryConModel)activeTable.getModel()).loadContestantByWeek(w);
 				((HistoryConModel)castTable.getModel()).loadContestantByWeek(w);
 			}
 
 		});
 	}
 
 	private void updateSpinnerModel(int w) {
 		weekModel.setMinimum(w > 0 ? 0 : 0);
		weekModel.setMaximum(w-1 > 0 ? w-1 : 0);
 		weekModel.setStepSize(1);
 	}
 	
 
 	@Override
 	public void update(Observable obs, Object arg) {
 		@SuppressWarnings("unchecked")
 		EnumSet<UpdateTag> update = (EnumSet<UpdateTag>) arg;
 
 		GameData g = (GameData) obs;
 
 		if (update.contains(UpdateTag.START_SEASON)) {
 			btnStartSn.setEnabled(false);
 			btnAdvWk.setEnabled(true);
 			spnWeek.setEnabled(true);
 		}
 
 		if (update.contains(UpdateTag.ADVANCE_WEEK)) {
 			// TODO: What should happen here?
 			btnAdvWk.setEnabled(true);
 		}
 
 		if (update.contains(UpdateTag.FINAL_WEEK)) {
 			// TODO: now its the final week:
 			btnAdvWk.setEnabled(true);
 			btnAdvWk.setText("Advance Final Week");
 		}
 
 		if (update.contains(UpdateTag.ADVANCE_WEEK)
 				|| update.contains(UpdateTag.FINAL_WEEK)
 				|| update.contains(UpdateTag.START_SEASON)
 				|| update.contains(UpdateTag.END_GAME)) {
 
 			updateSpinnerModel(g.getCurrentWeek());
 		}
 
 		if (update.contains(UpdateTag.END_GAME)) {
 			// TODO: is this really necessary?
 			btnStartSn.setText("End of Season");
 			btnAdvWk.setText("See Player Winners");
 			btnAdvWk.setEnabled(true);
 		}
 		
 		if (update.contains(UpdateTag.ADVANCE_WEEK)) {
 			spnWeek.setValue(g.getCurrentWeek()-1);
 		}
 		
 		if (update.contains(UpdateTag.END_GAME)) {
 			// TODO: FIX, this is ugly. :P
 			
 			String tempString = "Top SurvivorPool Winners\n";
 			User tempUser;
 			List<User> winners = g.determineWinners();
 			List<Integer> pool = g.determinePrizePool();
 			
 			for (int i = 0; i<winners.size(); i++){
 				tempUser = (User) winners.get(i);
 				tempString = tempString + "\n" + (i+1) + ". " + 
 				tempUser.toString() + " $" + pool.get(i);
 			}
 			
 			pnlWinners.add(new JLabel(tempString));
 			
 			// show the winning panel 
 			contTribeWin.showWinners();
 		}
 
 	}
 }
