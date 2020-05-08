 package admin.panel.general;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Arrays;
 import java.util.EnumSet;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSpinner;
 import javax.swing.JTextField;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.border.BevelBorder;
 import javax.swing.border.Border;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import admin.MainFrame;
 import admin.Utils;
 import data.GameData;
 import data.GameData.UpdateTag;
 import data.Person;
 
 public class GeneralPanel extends JPanel implements Observer {
 
 	private static final long serialVersionUID = 1L;
 
 	Integer viewWeek = 0;
 
 	private JLabel lblWeek;
 	private JLabel lblRemainingContestants;
 	private JLabel lblCastOffs;
 
 	private JTextField txtTribe1;
 	private JTextField txtTribe2;
 
 	private JButton btnStartSn;
 	private JButton btnAdvWk;
 	private JButton btnChangeTribeName;
 
 	private SpinnerNumberModel weekModel = new SpinnerNumberModel(0, 0, 0, 1); // default,low,min,step
 	private JSpinner spnWeek;
 
 	private JPanel pnlRemCons;
 	private JPanel pnlCastOffs;
 	
 	private JLabel lblWinners = new JLabel("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
 
 	private JPanel pnlTribes;
 	private JPanel pnlWeekCtrl;
 	private JPanel pnlHistory;
 	
 	private JPanel pnlCenter;
 
 	private JLabel lblTribe1;
 
 	private JLabel lblTribe2;
 	
 	public GeneralPanel() {
 		setLayout(new BorderLayout(10, 10));
 		
 		pnlTribes = buildTribePanel();
 		pnlWeekCtrl = buildCtrlPanel();
 		pnlHistory = buildHistory();
 		
 		pnlCenter = new JPanel();
 		pnlCenter.setLayout(new BoxLayout(pnlCenter, BoxLayout.PAGE_AXIS));
 		
 		JPanel subFrame = new JPanel();
 		subFrame.setLayout(new GridLayout(1, 2, 10, 5));
 		subFrame.add(pnlTribes);
 		//subFrame.add(Box.createHorizontalGlue());
 		subFrame.add(pnlWeekCtrl);
 		
 		pnlCenter.add(subFrame);
 		pnlCenter.add(Box.createVerticalStrut(10));
 		pnlCenter.add(pnlHistory);
 		
 		add(pnlCenter, BorderLayout.CENTER);
 		GameData.getCurrentGame().addObserver(this);
 		
 		initListeners();
 	}
 	
 	private JPanel buildTribePanel() {
 		GameData g = GameData.getCurrentGame();
 		
 		JPanel pane = new JPanel();
 		pane.setBorder(BorderFactory
 				.createTitledBorder("Tribes"));
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
 		
 		//pane.setPreferredSize(new Dimension(200, 200));
 		
 		return pane;
 	}
 	
 	private JPanel buildCtrlPanel() {
 		JPanel pane = new JPanel();
 		pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
 		pane.setBorder(BorderFactory.createTitledBorder("Time:"));
 		
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
 		
 		/// init:
 		GameData g = GameData.getCurrentGame();
 		btnStartSn.setEnabled(!g.isSeasonStarted());
 		btnAdvWk.setEnabled(g.isSeasonStarted() && !g.isSeasonEnded());
 		
 		return pane;	
 	}
 	
 	private JPanel buildHistory() {
 		JPanel pane = new JPanel();
 		pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
 		pane.setBorder(BorderFactory.createTitledBorder("History:"));
 		
 		/// spinnner stuff
 		JPanel pnlSpin = new JPanel();
 		pnlSpin.setLayout(new BoxLayout(pnlSpin, BoxLayout.LINE_AXIS));
 		lblWeek = new JLabel("View Week:");
 		lblWeek.setAlignmentX(JLabel.LEFT_ALIGNMENT);
 		spnWeek = new JSpinner(weekModel);
 		spnWeek.setAlignmentX(JSpinner.LEFT_ALIGNMENT);
 		
 		pnlSpin.add(lblWeek);
 		pnlSpin.add(Box.createHorizontalStrut(10));
 		pnlSpin.add(spnWeek);
 		pnlSpin.add(Box.createHorizontalGlue());
 		
 		/// panels
 		JPanel pnlCont = new JPanel();
 		pnlCont.setLayout(new GridLayout(1, 2, 10, 5));
 		Border bevB = BorderFactory.createSoftBevelBorder(BevelBorder.LOWERED);
 		final Dimension displaySize = new Dimension(150, 200);
 	
 		pnlRemCons = new JPanel();
 		pnlRemCons.setBorder(BorderFactory.createTitledBorder(bevB, "Contestants"));
 		pnlRemCons.setPreferredSize(displaySize);
 		
 		pnlCastOffs = new JPanel();
 		pnlCastOffs.setBorder(BorderFactory.createTitledBorder(bevB, "Cast Offs"));
 		pnlCastOffs.setPreferredSize(displaySize);
 
 		lblRemainingContestants = new JLabel("");
 		lblCastOffs = new JLabel("");
 		
 		pnlRemCons.add(lblRemainingContestants);
 		pnlCastOffs.add(lblCastOffs);
 		
 		pnlCont.add(pnlRemCons);
 		pnlCont.add(pnlCastOffs);
 		
 		// put all together:
 		
 		pane.add(pnlSpin);
 		pane.add(pnlCont);
 		
 		/// init:
 		setRemainingContestantsLabel();
 		setCastOffContestantsLabel();
 		
 		return pane;
 	}
 	
 	private void setRemainingContestantsLabel() {
 		String s = "<HTML>";
 		for (int i = 0; i < GameData.getCurrentGame().getAllContestants()
 				.size(); i++) {
 			if (GameData.getCurrentGame().getAllContestants().get(i)
 					.getCastDate() >= (Integer) spnWeek.getValue()
 					|| GameData.getCurrentGame().getAllContestants().get(i)
 							.getCastDate() == -1)
 				s += GameData.getCurrentGame().getAllContestants().get(i)
 						+ "<BR>";
 		}
 		s += "</HTML>";
 		lblRemainingContestants.setText(s);
 	}
 
 	private void setCastOffContestantsLabel() {
 		String s = "<HTML>";
 		for (int i = 0; i < GameData.getCurrentGame().getAllContestants()
 				.size(); i++) {
 			if (GameData.getCurrentGame().getAllContestants().get(i)
 					.getCastDate() < (Integer) spnWeek.getValue()
 					&& GameData.getCurrentGame().getAllContestants().get(i)
 							.getCastDate() != -1)
 				s += GameData.getCurrentGame().getAllContestants().get(i)
 						.toString()
 						+ ": Cast-Off Week "
 						+ GameData.getCurrentGame().getAllContestants().get(i)
 								.getCastDate() + "<BR>";
 		}
 		s += "</HTML>";
 		lblCastOffs.setText(s);
 	}
 
 	private void initListeners() {
 		btnStartSn.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				GameData g = GameData.getCurrentGame();
 				
 				if (g.getAllContestants().size() == 0) {
 					JOptionPane.showMessageDialog(null,
 							"This will be a boring game with no contestants.  Add some!");
 					return;
 				}
 				
 				if (g.getAllContestants().size() != g.getInitialContestants()) {
 					JOptionPane.showMessageDialog(null,
 							"You need to have " + g.getInitialContestants()
 									+ " contestants to start.");
 					return;
 				} else if (!g.isSeasonStarted()) {
 					String s = JOptionPane.showInputDialog("Enter weekly bet amount!");
 					if (Utils.checkString(s, "^[0-9]+$")) {
 						if (Integer.parseInt(s) >= 0) {
 							g.startSeason(Integer.parseInt(s));	
 							
 							// needs an update, can't figure out what the proper way of calling it.
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
 				
 				if (!g.doesElimExist()){
 					if (g.isFinalWeek()) {
 						JOptionPane.showMessageDialog(null,
 						"No Contestant has been selected to be the winner.");
 					} else {
 						JOptionPane.showMessageDialog(null,
 						"No Contestant has been selected to be cast off.");
 					}
 				} 
 				else{
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
 					mf.setStatusErrorMsg(
 							"Tribe 1 name invalid.", txtTribe1);
 				} else if (!Utils.checkString(txtTribe2.getText(),
 						Person.TRIBE_PATTERN)) {
 					mf.setStatusErrorMsg("Tribe 2 name invalid.", 
 							txtTribe2);
 				} else if (txtTribe1.getText().equals(txtTribe2.getText())) {
 					mf.setStatusErrorMsg("Invalid tribe names, cannot be the same",
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
 				setRemainingContestantsLabel();
 				setCastOffContestantsLabel();
 			}
 
 		});
 	}
 
 	@Override
 	public void update(Observable obs, Object arg) {
 		@SuppressWarnings("unchecked")
 		EnumSet<UpdateTag> update = (EnumSet<UpdateTag>)arg;
 		
 		GameData g = (GameData)obs;
 		
 		if (update.contains(UpdateTag.START_SEASON)) {
 			btnStartSn.setEnabled(false);
 			btnAdvWk.setEnabled(true);
 		}
 		
 		if (update.contains(UpdateTag.ADVANCE_WEEK)) {
 			// TODO: What should happen here?
 			btnAdvWk.setEnabled(true);
 		}
 		
 		if (update.contains(UpdateTag.FINAL_WEEK)) {
 			// TODO: now its the final week: 
			btnAdvWk.setEnabled(false);
 			btnAdvWk.setText("Advance Final Week");
 		}
 		
 		if (update.contains(UpdateTag.ADVANCE_WEEK) || 
 				update.contains(UpdateTag.FINAL_WEEK) ||
 				update.contains(UpdateTag.START_SEASON) ||
 				update.contains(UpdateTag.END_GAME)) {
 			setRemainingContestantsLabel();
 			setCastOffContestantsLabel();
 			
 			weekModel.setMaximum(g.getCurrentWeek());
 			weekModel.setValue(g.getCurrentWeek());
 		}
 		
 		if (update.contains(UpdateTag.END_GAME)) {
 			// TODO: is this really necessary?
 			btnAdvWk.setText("End Of Season");
 		}
 		
 	}
 }
