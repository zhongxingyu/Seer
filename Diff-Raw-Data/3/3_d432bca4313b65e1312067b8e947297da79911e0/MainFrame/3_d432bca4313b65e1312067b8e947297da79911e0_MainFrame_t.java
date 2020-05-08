 package admin;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
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
 
 import admin.panel.bonus.BonusPanel;
 import admin.panel.general.GeneralPanel;
 import admin.panel.person.contestant.ContestantPanel;
 import admin.panel.person.player.PlayerPanel;
 import admin.panel.season.SeasonCreatePanel;
 import data.GameData;
 import data.Settings;
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
 	
 	private List<JRadioButtonMenuItem> radioMenuItems;
 	
 	private HashMap<String, JRadioButtonMenuItem> themeToItem;
 
 	public static final String GENERAL_PANEL = "General",
 			CONTESTANT_PANEL = "Contestants", PLAYER_PANEL = "Players",
 			BONUS_PANEL = "Bonus";
 
 	private StatusPanel statusBar;
 
 	private ContestantPanel conPanel;
 	private PlayerPanel playerPanel;
 	private BonusPanel bonusPanel;
 	
 	private Settings settings;
 
 	public MainFrame() {
 
 		GameData g = GameData.initGameData();
 
 		settings = Settings.initSettingsData();
 		
 		Bonus.initBonus();
 		
 		initMenuBar();
 
 		if (g != null)
 			initGUI();
 		else
 			initSeasonCreateGUI();
 		
 		setVisible(true);
 		setTitle("Survivor Pool Admin");
 		setMinimumSize(new Dimension(640, 480));
 		// can resize frame
 		setResizable(true);
 			
 		this.addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent we) {
 				windowClose();
 			}
 		});
 		
 		loadSettings();
		this.setIconImage(new ImageIcon("res/img/icon.png").getImage());
 	}
 	
 	private ActionListener al = new ActionListener() {
 		@Override
 		public void actionPerformed(ActionEvent ae) {
 			if (ae.getSource() == mnuItemExit) {
 				windowClose();
 			} else if (ae.getSource() == mnuItemReset) {
 				resetSeason();
 			} else if (ae.getSource() == mnuItemSave){
 				saveAllData();
 			} else if (ae.getSource() instanceof JRadioButtonMenuItem) {
 				changeTheme(ae.getActionCommand());
 			}
 		}
 	};
 
 	private ChangeListener cl = new ChangeListener() {
 		@Override
 		public void stateChanged(ChangeEvent ce) {
 			JTabbedPane tabSource = (JTabbedPane) ce.getSource();
 			
 			String tab = tabSource.getTitleAt(tabSource.getSelectedIndex());
 			statusBar.setTabLabel(tab);
 		}
 	};
 	
 	private void saveAllData() {
 		if (GameData.getCurrentGame() != null)
 			GameData.getCurrentGame().writeData();
 		
 		if (!Bonus.getAllQuestions().isEmpty()) 
 			Bonus.writeData();
 		
 		if (settings != null)
 			saveSettings();
 	}
 	
 	/**
 	 * Loads the settings value into the GUI from the file
 	 */
 	private void loadSettings() {
 		if (settings.containsKey(Settings.THEME)) {
 			changeTheme((String)settings.get(Settings.THEME));
 		} else {
 			changeTheme(Utils.GUITHEME.Snow.name());
 		}
 		
 		if (settings.containsKey(Settings.SCREEN_LOC_X)) {
 			int x = ((Number)settings.get(Settings.SCREEN_LOC_X)).intValue();
 			int y = ((Number)settings.get(Settings.SCREEN_LOC_Y)).intValue();
 			
 			setLocation(x, y);
 		} else {
 			// default to center:
 			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 		    int sHeight = screenSize.height;
 		    int sWidth = screenSize.width;
 		    
 		    Point loc = new Point(sWidth / 2 - (getWidth() / 2), sHeight / 2 - (getHeight() / 2));
 		    
 		    setLocation(loc);
 		}
 		
 		if (settings.containsKey(Settings.SCREEN_SIZE_X)) {
 			int x = ((Number)settings.get(Settings.SCREEN_SIZE_X)).intValue();
 			int y = ((Number)settings.get(Settings.SCREEN_SIZE_Y)).intValue();
 			
 			setSize(x, y);
 		} else {
 			setSize(640, 480);
 		}
 	}
 	
 	/**
 	 * Pulls the settings' values from the GUI into the file.
 	 */
 	private void saveSettings() {
 		settings.put(Settings.THEME, Utils.getTheme().name());
 		
 		Dimension d = getSize();
 		settings.put(Settings.SCREEN_SIZE_X, d.width);
 		settings.put(Settings.SCREEN_SIZE_Y, d.height);
 		
 		Point l = getLocation();
 		settings.put(Settings.SCREEN_LOC_X, l.x);
 		settings.put(Settings.SCREEN_LOC_Y, l.y);
 		
 		settings.writeData();
 	}
 
 	/**
 	 * Creates the seasonCreateGUI
 	 */
 	private void initSeasonCreateGUI() {
 		mnuItemSave.setEnabled(false);
 		this.setLayout(new BorderLayout());
 		statusBar = new StatusPanel();
 		this.add(new SeasonCreatePanel(), BorderLayout.CENTER);
 		this.add(statusBar, BorderLayout.SOUTH);
 		statusBar.setTabLabel("SEASON CREATE");
 	}
 
 	/**
 	 * Loads the initial GUI
 	 */
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
 
 	/**
 	 * Loads the menu bar
 	 */
 	private void initMenuBar() {
 		mnuItemReset = new JMenuItem("Reset Season");
 		mnuItemSave = new JMenuItem("Save");
 		mnuItemExit = new JMenuItem("Exit");
 		
 		// init the theme menu:
 		radioMenuItems = new LinkedList<JRadioButtonMenuItem>();
 		String[] themeName = Utils.getThemes();
 		themeToItem = 
 				new HashMap<String, JRadioButtonMenuItem>(themeName.length);
 		
 		ButtonGroup g = new ButtonGroup();
 		for (int i = 0; i < themeName.length; i++) {
 			JRadioButtonMenuItem item = new JRadioButtonMenuItem(themeName[i]);
 			radioMenuItems.add(i, item);
 			themeToItem.put(themeName[i], item);
 			
 			g.add(item);
 			item.addActionListener(al);
 			
 			mnuTheme.add(item);
 		}
 		// end init theme menu
 
 		statusBar = new StatusPanel();
 
 		mnuFile.add(mnuItemSave);
 		mnuFile.add(mnuItemReset);
 		mnuFile.add(mnuItemExit);
 
 		menuBar.add(mnuFile);
 		menuBar.add(mnuTheme);
 
 		mnuItemReset.addActionListener(al);
 		mnuItemSave.addActionListener(al);
 		mnuItemExit.addActionListener(al);
 		
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
 		
 		settings.put(Settings.THEME, name);
 		JRadioButtonMenuItem rb = themeToItem.get(name);
 		rb.setSelected(true);
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
 			}
 		}
 	}
 
 	/**
 	 * Saves all data associated with the application
 	 */
 	private void windowClose() {
 		saveAllData();
 		
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
