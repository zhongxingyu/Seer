 package de.tu_darmstadt.gdi1.samegame.gameframes;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.GridLayout;
 
 import java.awt.event.KeyEvent;
 
 import static java.lang.Thread.sleep;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import java.util.Locale;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.KeyStroke;
 
 import de.tu_darmstadt.gdi1.samegame.GameController;
 import de.tu_darmstadt.gdi1.samegame.Level;
 
 import de.tu_darmstadt.gdi1.samegame.exceptions.InternalFailureException;
 import de.tu_darmstadt.gdi1.samegame.exceptions.ParameterOutOfRangeException;
 
 @SuppressWarnings("serial")
 public class MainFrame extends JFrame implements Runnable{
 
 	////////////////////////Class/Attributes//////////////////////////
 	private Level level;
 	private Locale locale;
 
 	private ResourceBundle messages;
 
 	private GameController controller;
 	
 	private MainPanel panel;
 
 	private JLabel MinStones;
 	private JLabel Points;
 	private JLabel ElapsedTime;
 	private JLabel TargetTime;
 	private JLabel sl_MinStones;
 	private JLabel sl_Points;
 	private JLabel sl_ElapsedTime;
 	private JLabel sl_TargetTime;
 	
 	private String skin;
 	private Color FColor;
 	private Color BColor;
 	
 			
 	////////////////////////Class/Constructors////////////////////////
 	public MainFrame(Level level, GameController controller, Locale locale, String skin, Color FColor, Color BColor){
 		super("Same Game");
 		this.level = level;
 		this.controller = controller;
 
 		this.locale = locale;
 
 		this.skin = skin;
 		this.FColor = FColor;
 		this.BColor = BColor; 
 		
 		this.panel = new MainPanel(this, level, controller, skin);
 		this.add(panel, BorderLayout.CENTER);
 
 		this.updateContents();
 
 		this.setFocusable(true);
 		this.addKeyListener(this.controller);
 
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.setResizable(false);
 
 		this.requestFocus();
 	}
 
 
 	////////////////////////Getters//&//Setters///////////////////////
 	public MainPanel getMainPanel() {
 		return panel;
 	}
 
 	public void setLanguage(Locale locale){
 		this.locale = locale;
 		updateContents();
 	}
 
 	public void setSkin(String skin, Color FColor, Color BColor){
 		this.skin = skin;
 		this.FColor = FColor;
 		this.BColor = BColor;
 
 		panel.setSkin(skin);
 		
 		updateContents();
 	}
 	////////////////////////Class/Operations//////////////////////////
 	public void updateContents(){
 		try{
 			messages = 
 				ResourceBundle.getBundle(
 						"de.tu_darmstadt.gdi1.samegame.gameframes.MainBundle", 
 						this.locale, 
 						this.getClass().getClassLoader()); 
 		}catch(MissingResourceException e){
 			this.locale = new Locale("de", "DE");
 
 			messages = 
 				ResourceBundle.getBundle(
 						"de.tu_darmstadt.gdi1.samegame.gameframes.MainBundle", 
 						this.locale,
 						this.getClass().getClassLoader()); 
 		}
 
 		// ========= menu =========
		JMenuBar menuBar = new JMenuBar();
 		menuBar.setBackground(BColor);
 		
 		JMenu fileMenu = new JMenu(messages.getString("FileMenu_Border"));
 		fileMenu.setForeground(FColor);
 		fileMenu.setBackground(BColor);
 		
 		JMenu new_lvl = new JMenu(messages.getString("FileMenu_NewLevel"));
 		new_lvl.setForeground(FColor);
 		new_lvl.setBackground(BColor);
 		fileMenu.add(new_lvl);
 		
 		JMenuItem gen_lvl = new JMenuItem(messages.getString("FileMenu_GenerateLevel"));
 		gen_lvl.addActionListener(controller);
 		gen_lvl.setName("FileMenu_GenerateLevel");
 		gen_lvl.setForeground(FColor);
 		gen_lvl.setBackground(BColor);
 		
 		
 		JMenuItem genc_lvl = new JMenuItem(messages.getString("FileMenu_GenerateCustomLevel"));
 		genc_lvl.addActionListener(controller);
 		genc_lvl.setName("FileMenu_GenerateCustomLevel");
 		genc_lvl.setForeground(FColor);
 		genc_lvl.setBackground(BColor);
 		
 		new_lvl.add(gen_lvl);
 		new_lvl.add(genc_lvl);
 		
 		
 		fileMenu.addSeparator();
 		
 		JMenuItem save_lvl = new JMenuItem(messages.getString("FileMenu_SaveLevel"));
 		save_lvl.setName("FileMenu_SaveLevel");
 		save_lvl.addActionListener(controller);
 		save_lvl.setForeground(FColor);
 		save_lvl.setBackground(BColor);
 		fileMenu.add(save_lvl);
 		
 		JMenuItem load_lvl = new JMenuItem(messages.getString("FileMenu_LoadLevel"));
 		load_lvl.setName("FileMenu_LoadLevel");
 		load_lvl.addActionListener(controller);
 		load_lvl.setForeground(FColor);
 		load_lvl.setBackground(BColor);
 		fileMenu.add(load_lvl);
 		
 		fileMenu.addSeparator();
 		
 		JMenuItem save_game = new JMenuItem(messages.getString("FileMenu_SaveGameState"));
 		save_game.setName("FileMenu_SaveGameState");
 		save_game.addActionListener(controller);
 		save_game.setForeground(FColor);
 		save_game.setBackground(BColor);
 		fileMenu.add(save_game);
 		
 		JMenuItem load_game = new JMenuItem(messages.getString("FileMenu_LoadGameState"));
 		load_game.setName("FileMenu_LoadGameState");
 		load_game.addActionListener(controller);
 		load_game.setForeground(FColor);
 		load_game.setBackground(BColor);
 		fileMenu.add(load_game);
 		
 		fileMenu.addSeparator();
 		
 		JMenuItem exit = new JMenuItem(messages.getString("FileMenu_Exit"));
 		exit.setName("FileMenu_Exit");
 		exit.addActionListener(controller);
 		exit.setForeground(FColor);
 		exit.setBackground(BColor);
 		fileMenu.add(exit);
 				
 			
 		// Game-Menu
 		JMenu GameMenu = new JMenu(messages.getString("GameMenu"));
 		GameMenu.setForeground(FColor);
 		GameMenu.setBackground(BColor);
 		
 		JMenuItem restart = new JMenuItem(messages.getString("GameMenu_RestartLvl"));
 		restart.setName("GameMenu_RestartLvl");
 		restart.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, 0));
 		restart.addActionListener(controller);
 		restart.setForeground(FColor);
 		restart.setBackground(BColor);
 		GameMenu.add(restart);
 		
 		GameMenu.addSeparator();
 		
 		JMenuItem undo = new JMenuItem(messages.getString("GameMenu_Undo"));
 		undo.setName("GameMenu_Undo");
 		undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0));
 		undo.addActionListener(controller);
 		undo.setForeground(FColor);
 		undo.setBackground(BColor);
 		GameMenu.add(undo);
 		
 		JMenuItem redo = new JMenuItem(messages.getString("GameMenu_Redo"));
 		redo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
 		redo.setName("GameMenu_Redo");
 		redo.addActionListener(controller);
 		redo.setForeground(FColor);
 		redo.setBackground(BColor);
 		GameMenu.add(redo);
 		
 
 		// Options-Menu
 		JMenu optionsMenu = new JMenu(messages.getString("OptionsMenu_Border"));
 		optionsMenu.setForeground(FColor);
 		optionsMenu.setBackground(BColor);
 		
 		
 		//Sub-Options-Menu Language
 		JMenu setLanguage = new JMenu(messages.getString("SetLanguage"));
 		setLanguage.setForeground(FColor);
 		setLanguage.setBackground(BColor);
 			
 		String iconsPath = this.getClass().getResource("../../resources/images/icons").toString();
 
 		// JMenuItem German 
 		JMenuItem ger_lang;
 		try{
 			ger_lang = new JMenuItem(messages.getString("German"), 
 									 new ImageIcon(new URL(iconsPath+"/ger.png")));
 		}catch(MalformedURLException e){
 			e.printStackTrace();
 			ger_lang = new JMenuItem(messages.getString("German"));
 		}
 		ger_lang.setName("German");
 		ger_lang.addActionListener(controller);
 		ger_lang.setForeground(FColor);
 		ger_lang.setBackground(BColor);
 		setLanguage.add(ger_lang);
 	
 
 		//JMenuItem English
 		JMenuItem eng_lang;
 		try{
 			eng_lang = new JMenuItem(messages.getString("English"),
 									 new ImageIcon(new URL(iconsPath+"/eng.png")));
 		}catch(MalformedURLException e){
 			e.printStackTrace();
 			eng_lang = new JMenuItem(messages.getString("German"));
 		}
 		eng_lang.setName("English");
 		eng_lang.addActionListener(controller);
 		eng_lang.setForeground(FColor);
 		eng_lang.setBackground(BColor);
 		setLanguage.add(eng_lang);		
 		
 		//Sub-Options-Menu Set Skin
 		JMenu setSkin = new JMenu(messages.getString("SetSkin"));
 		setSkin.setForeground(FColor);
 		setSkin.setBackground(BColor);
 		
 		JMenuItem skin_default = new JMenuItem(messages.getString("Skin_Default"));
 		skin_default.setName("Skin_Default");
 		skin_default.addActionListener(controller);
 		skin_default.setForeground(FColor);
 		skin_default.setBackground(BColor);
 		
 		JMenuItem skin_tuskin = new JMenuItem(messages.getString("Skin_Tuskin"));
 		skin_tuskin.setName("Skin_Tuskin");
 		skin_tuskin.addActionListener(controller);
 		skin_tuskin.setForeground(FColor);
 		skin_tuskin.setBackground(BColor);
 		
 		setSkin.add(skin_default);
 		setSkin.add(skin_tuskin);
 		//Place to for additional skins
 		
 		
 		optionsMenu.add(setLanguage);
 		optionsMenu.add(setSkin);
 		
 		JMenu Qm = new JMenu("?");
 		Qm.setForeground(FColor);
 		Qm.setBackground(BColor);
 		
 		JMenuItem about = new JMenuItem(messages.getString("About"));
 		about.setName("About");
 		about.addActionListener(controller);
 		about.setForeground(FColor);
 		about.setBackground(BColor);
 		
 		Qm.add(about);
 		
 		menuBar.add(fileMenu);
 		menuBar.add(GameMenu);
 		menuBar.add(optionsMenu);
 		menuBar.add(Qm);
 		this.add(menuBar, BorderLayout.NORTH);
 
 		// ===== status line =====
 		JPanel statusLine = new JPanel(new BorderLayout());
 		JPanel statusLineLabels = new JPanel(new GridLayout(4, 1));
 		JPanel statusLineValues = new JPanel(new GridLayout(4, 1, 10, 0));
 		statusLineLabels.setBackground(BColor);
 		statusLineValues.setBackground(BColor);
 
 		
 		//changed to be able to set Font-Colors --> last two lines per JLabel-Group
 		sl_MinStones = new JLabel(messages.getString("StatusLine_MinStones"));
 		sl_MinStones.setForeground(FColor);
 		MinStones = new JLabel(""+level.getMinStones());
 		MinStones.setForeground(FColor);
 		statusLineLabels.add(sl_MinStones);
 		statusLineValues.add(MinStones);
 			
 		sl_Points = new JLabel(messages.getString("StatusLine_Points"));
 		sl_Points.setForeground(FColor);
 		Points = new JLabel(""+(int)level.getPoints());
 		Points.setForeground(FColor);
 		statusLineLabels.add(sl_Points);
 		statusLineValues.add(Points);
 
 		sl_ElapsedTime = new JLabel(messages.getString("StatusLine_ElapsedTime"));
 		sl_ElapsedTime.setForeground(FColor);
 		ElapsedTime = new JLabel(""+(int)(level.getElapsedTime()/1000.0));
 		ElapsedTime.setForeground(FColor);
 		statusLineLabels.add(sl_ElapsedTime);	
 		statusLineValues.add(ElapsedTime);
 		
 		sl_TargetTime = new JLabel(messages.getString("StatusLine_TargetTime"));
 		sl_TargetTime.setForeground(FColor);
 		TargetTime = new JLabel(""+level.getTargetTime());
 		TargetTime.setForeground(FColor);
 		statusLineLabels.add(sl_TargetTime);
 		statusLineValues.add(TargetTime);
 
 		statusLine.add(statusLineLabels, BorderLayout.CENTER);
 		statusLine.add(statusLineValues, BorderLayout.EAST);
 
 		this.add(statusLine, BorderLayout.SOUTH);
 	}
 
 	/**
 	 * Notifies the game window that a new level has been loaded
 	 * 
 	 * @param width
 	 *            The width of the level just loaded
 	 * @param height
 	 *            The height of the level just loaded
 	 * @throws ParameterOutOfRangeException
 	 *             Thrown if one of the parameters falls out of the range of
 	 *             acceptable values
 	 * @throws InternalFailureException
 	 * 			   Thrown if an uncorrectable internal error occurs
 	 */
 	public void notifyLevelLoaded(int width, int height)
 			throws ParameterOutOfRangeException, InternalFailureException {
 		// Check the parameters
 		if (width <= 0)
 			throw new ParameterOutOfRangeException("Game Window width invalid");
 		if (height <= 0)
 			throw new ParameterOutOfRangeException("Game Window height invalid");
 
 		// Notify the panel
 		panel.notifyLevelLoaded(width, height);
 	}
 
 	public void redraw(){
 		if(level.isFinished()){
 			// TODO show finished message
 			ElapsedTime.setText(""+level.getElapsedTime()/1000.0);
 		}else
 			ElapsedTime.setText(""+(int)(level.getElapsedTime()/1000.0));
 
 
 		Points.setText(""+(int)level.getPoints());
 		MinStones.setText(""+level.getMinStones());
 		TargetTime.setText(""+level.getTargetTime());
 
 		try{
 			if(!panel.duringAnimation())
 				panel.redraw();
 		}catch(InternalFailureException e){
 			e.printStackTrace();
 		}
 
 	}
 
 	@Override
 	public void run(){
 		// Time update
 		while(true){
 			redraw();
 			try{
 				sleep(1000);
 			}catch(InterruptedException ignored){}
 		}
 	}
 }
