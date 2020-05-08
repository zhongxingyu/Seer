 package admin;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JRadioButtonMenuItem;
 import javax.swing.JTabbedPane;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import admin.Utils;
 import admin.StatusPanel;
 import admin.panel.bonus.BonusPanel;
 import admin.panel.person.contestant.ContestantPanel;
 import admin.panel.general.GeneralPanel;
 import admin.panel.person.player.PlayerPanel;
 import admin.panel.season.SeasonCreatePanel;
 
 import data.GameData;
 import data.Settings;
 import data.Settings.Field;
 import data.bonus.Bonus;
 
 public class MainFrame extends JFrame {
 
 	private static final long serialVersionUID = 1L;
 
 	static MainFrame m;
 	
 	private JTabbedPane tabPane = new JTabbedPane();
 
 	private JLabel lblGeneral = new JLabel(GENERAL_PANEL);
 	private JLabel lblContestants = new JLabel(CONTESTANT_PANEL);
 	private JLabel lblPlayers = new JLabel(PLAYER_PANEL);
 	private JLabel lblBonus = new JLabel(BONUS_PANEL);
 
 	private static JMenuBar menuBar = new JMenuBar();
 	private JMenu mnuFile = new JMenu("File");
 	private JMenu mnuTheme = new JMenu("Theme");
 
 	private JMenuItem mnuItemReset;
 	private JMenuItem mnuItemSave;
 	private JMenuItem mnuItemExit;
 	
 	private JRadioButtonMenuItem mnuItemTheme1;
 	private JRadioButtonMenuItem mnuItemTheme2;
 	private JRadioButtonMenuItem mnuItemTheme3;
 
 	public static final String GENERAL_PANEL = "General",
 			CONTESTANT_PANEL = "Contestants", PLAYER_PANEL = "Players",
 			BONUS_PANEL = "Bonus";
 
 	private StatusPanel statusBar;
 
 	private ContestantPanel conPanel;
 	private PlayerPanel playerPanel;
 	private BonusPanel bonusPanel;
 
 	ActionListener al = new ActionListener() {
 		@Override
 		public void actionPerformed(ActionEvent ae) {
 			if (ae.getSource() == mnuItemExit) {
 				windowClose();
 			} else if (ae.getSource() == mnuItemReset) {
 				resetSeason();
 			} else if (ae.getSource() == mnuItemSave){
 				GameData.getCurrentGame().writeData();
 			} else if (ae.getSource() == mnuItemTheme1) {
 				changeTheme(ae.getActionCommand());
 			} else if (ae.getSource() == mnuItemTheme3) {
 				changeTheme(ae.getActionCommand());
 			} else if (ae.getSource() == mnuItemTheme2) {
 				changeTheme(ae.getActionCommand());
 			}
 		}
 	};
 
 	ChangeListener cl = new ChangeListener() {
 		@Override
 		public void stateChanged(ChangeEvent ce) {
 			JTabbedPane tabSource = (JTabbedPane) ce.getSource();
 			String tab = tabSource.getTitleAt(tabSource.getSelectedIndex());
 			statusBar.setTabLabel(tab);
 		}
 	};
 
 	public MainFrame() {
 
 		GameData g = GameData.initGameData();
 
 		Settings s = Settings.initSettingsData();
 		
 		initMenuBar();
 
 		if (g != null)
 			initGUI();
 		else
 			initSeasonCreateGUI();
 
 		if (s == null) s = new Settings("Snow");
 		
 		applyTheme();
 		
 		this.setSize(640, 480);
 		this.setVisible(true);
 		this.setTitle("Survivor Pool Admin");
 		// can resize frame
 		//this.setResizable(false);
 		
 		// center the screen
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 	    int sHeight = screenSize.height;
 	    int sWidth = screenSize.width;
 	    setLocation(sWidth / 2 - getWidth(), sHeight / 2 - getHeight());
 		
 		this.addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent we) {
 				windowClose();
 			}
 		});
 	}
 
 	private void initSeasonCreateGUI() {
 		mnuItemSave.setEnabled(false);
 		this.setLayout(new BorderLayout());
 		statusBar = new StatusPanel();
 		this.add(new SeasonCreatePanel(), BorderLayout.CENTER);
 		this.add(statusBar, BorderLayout.SOUTH);
 		statusBar.setTabLabel("SEASON CREATE");
 	}
 
 	private void initGUI() {
 		mnuItemSave.setEnabled(true);
		Dimension d = new Dimension(132, 20);
 
 		lblGeneral.setPreferredSize(d);
 		lblContestants.setPreferredSize(d);
 		lblPlayers.setPreferredSize(d);
 		lblBonus.setPreferredSize(d);
 
 		conPanel = new ContestantPanel();
 		playerPanel = new PlayerPanel();
 		bonusPanel = new BonusPanel();
 
 		tabPane.addTab(lblGeneral.getText(), new GeneralPanel());
 		tabPane.addTab(lblContestants.getText(), conPanel);
 		tabPane.addTab(lblPlayers.getText(), playerPanel);
 		tabPane.addTab(lblBonus.getText(), bonusPanel);
 
 		tabPane.addChangeListener(playerPanel);
 
 		tabPane.setTabComponentAt(0, lblGeneral);
 		tabPane.setTabComponentAt(1, lblContestants);
 		tabPane.setTabComponentAt(2, lblPlayers);
 		tabPane.setTabComponentAt(3, lblBonus);
 		// tabPane.setBackground(Color.cyan);//tab background color,not the
 		// panel
 
 		tabPane.addChangeListener(cl);
 		statusBar.setTabLabel(GENERAL_PANEL);
 
 		this.setLayout(new BorderLayout());
 
 		this.add(tabPane);
 		this.add(statusBar, BorderLayout.SOUTH);
 	}
 
 	private void initMenuBar() {
 		mnuItemReset = new JMenuItem("Reset Season");
 		mnuItemSave = new JMenuItem("Save");
 		mnuItemExit = new JMenuItem("Exit");
 		String[] themeName = Utils.getThemes();
 		mnuItemTheme1 = new JRadioButtonMenuItem(themeName[0]);
 		mnuItemTheme3 = new JRadioButtonMenuItem(themeName[1]);
 		mnuItemTheme2 = new JRadioButtonMenuItem(themeName[2]);
 
 		statusBar = new StatusPanel();
 
 		ButtonGroup g = new ButtonGroup();
 		mnuTheme.add(mnuItemTheme1);
 		g.add(mnuItemTheme1);
 		mnuTheme.add(mnuItemTheme3);
 		g.add(mnuItemTheme3);
 		mnuTheme.add(mnuItemTheme2);
 		g.add(mnuItemTheme2);
 
 		mnuFile.add(mnuItemSave);
 		mnuFile.add(mnuItemReset);
 		mnuFile.add(mnuItemExit);
 
 		menuBar.add(mnuFile);
 		menuBar.add(mnuTheme);
 
 		mnuItemReset.addActionListener(al);
 		mnuItemSave.addActionListener(al);
 		mnuItemExit.addActionListener(al);
 		mnuItemTheme1.addActionListener(al);
 		mnuItemTheme3.addActionListener(al);
 		mnuItemTheme2.addActionListener(al);
 		
 		this.setJMenuBar(menuBar);
 	}
 
 	/**
 	 * Apply the theme to current components.
 	 */
 	private void applyTheme() {
 		Utils.style(this);
 	}
 
 	/**
 	 * Change the current theme.
 	 * 
 	 * @param name
 	 *            The theme name
 	 */
 	private void changeTheme(String name) {
 		Utils.changeTheme(name);
 		applyTheme();
 		Settings.getCurrentSettings().setSetting(Field.THEME, name);
 	}
 
 	/**
 	 * Called by SeasonCreatePanel when a new season has been created/
 	 */
 	public static void createSeason() {
 		// TODO: This is called twice for some reason. once at full init, once now
 		GameData.initGameData();
 		m.getContentPane().removeAll();
 		m.initGUI();
 		m.applyTheme();
 	}
 
 	/**
 	 * Set the statusbar message
 	 * 
 	 * @param msg
 	 */
 	public void setStatusMsg(String msg) {
 		statusBar.setMsgLabel(msg);
 	}
 
 	/**
 	 * Set an error message in the status bar.
 	 * 
 	 * @param msg
 	 *            The message to set
 	 */
 	public void setStatusErrorMsg(String msg) {
 		setStatusErrorMsg(msg, (Component)null);
 	}
 
 	/**
 	 * Set an error message in the statusbar. If comp is not null, it will set
 	 * the background of that component to red.
 	 * 
 	 * @param msg
 	 * @param comps
 	 *            Multiple components
 	 */
 	public void setStatusErrorMsg(String msg, Component... comps) {
 		statusBar.setErrorMsgLabel(msg, comps);
 	}
 	
 	/**
 	 * Delegate to {@link StatusPanel.clearPanel()}.
 	 */
 	public void clearStatusError() {
 		statusBar.clearPanel();
 	}
 
 	/**
 	 * Delete season file and show season create panel
 	 */
 	private void resetSeason() {
 		int response = JOptionPane.showConfirmDialog(null,
 				"Would you like to delete current season?", "Reset Season",
 				JOptionPane.YES_NO_OPTION);
 		if (response == JOptionPane.YES_OPTION) {
 			try {
 				GameData.getCurrentGame().endCurrentGame();
 				m.getJMenuBar().removeAll();
 				m.dispose();
 				m = new MainFrame();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Checks all components and force calls refreshGameFields if they implement
 	 * GameDataDependant
 	 */
 	public void forceGameDataRefresh() {
 		GameData.getCurrentGame().notifyObservers();
 	}
 
 	/**
 	 * Saves all data associated with the application
 	 */
 	// FIXME: Theme data etc needs to be saved.
 	private void windowClose() {
 		if (GameData.getCurrentGame() != null)
 			GameData.getCurrentGame().writeData();
 		
 		if (!Bonus.getAllQuestions().isEmpty()) 
 			Bonus.writeData();
 		
 		System.exit(0);
 	}
 
 	/**
 	 * Used to get reference to the running GUI.
 	 * 
 	 * @return Gets the running MainFrame.
 	 */
 	public static MainFrame getRunningFrame() {
 		return m;
 	}
 
 	public static void main(String[] args) {
 		m = new MainFrame();
 	}
 
 }
