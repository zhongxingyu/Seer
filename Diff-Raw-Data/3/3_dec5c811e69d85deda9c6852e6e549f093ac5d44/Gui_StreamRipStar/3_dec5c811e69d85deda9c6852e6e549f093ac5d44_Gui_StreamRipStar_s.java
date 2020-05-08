 package gui;
 /* This program is licensed under the terms of the GPL V3 or newer*/
 /* Written by Johannes Putzke*/
 /* eMail: die_eule@gmx.net*/ 
 
 import java.awt.AWTException;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Image;
 import java.awt.MenuItem;
 import java.awt.PopupMenu;
 import java.awt.SystemTray;
 import java.awt.Toolkit;
 import java.awt.TrayIcon;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.net.URL;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPopupMenu;
 import javax.swing.JToolBar;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.xml.stream.XMLEventFactory;
 import javax.xml.stream.XMLEventWriter;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamConstants;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.stream.events.XMLEvent;
 import misc.Stream;
 import thread.Thread_Control_Schedul;
 import thread.Thread_FillTableWithStreams;
 import thread.Thread_UpdateName;
 import control.Control_GetPath;
 import control.Control_RunExternProgram;
 import control.Control_Stream;
 import control.Control_Threads;
 import control.SRSOutput;
 import control.VolumeManager;
 
 public class Gui_StreamRipStar extends JFrame implements WindowListener 
 {
 	private static final long serialVersionUID = 1L;
 
 	private ResourceBundle trans = ResourceBundle.getBundle("translations.StreamRipStar");
 	private Control_Stream controlStreams = null;
 	private Gui_TablePanel table = null; 	//Table that shows all streams
 	private Thread_Control_Schedul controlJob = null;
 	private Gui_StreamBrowser2 streamBrowser = null;
 	private InternAudioControlPanel audioPanel = null;
 	private VolumeManager volumeManager = new VolumeManager(this);
 	
 	//for runtime
 	private Boolean tray = false;				// false = hide tray icon
 	private Boolean showText = false; 			//false = don't show text under Icons
 	private Boolean useInternalPlayer = true;	//true, if we use the internal player
 	private int winAction = 1;					//1= close window
 	private int action0 = -1;
 	private int action1 = -1;
 	private int action2 = -1;
 	
 	//Icons for the icon bar
 	private ImageIcon startRecordIcon = new ImageIcon((URL)getClass().getResource("/Icons/record.png"));
 	private ImageIcon stopRecordIcon = new ImageIcon((URL)getClass().getResource("/Icons/stop.png"));
 	private ImageIcon schudleIcon = new ImageIcon((URL)getClass().getResource("/Icons/schedule.png"));
 	private ImageIcon deleteIcon = new ImageIcon((URL)getClass().getResource("/Icons/del.png"));
 	private ImageIcon addIcon = new ImageIcon((URL)getClass().getResource("/Icons/add.png"));
 	private ImageIcon hearMusicIcon = new ImageIcon((URL)getClass().getResource("/Icons/player.png"));
 	private ImageIcon openMusicFolderIcon = new ImageIcon((URL)getClass().getResource("/Icons/m_open.png"));
 	private ImageIcon exitIcon = new ImageIcon((URL)getClass().getResource("/Icons/exit.png"));
 	private ImageIcon configIcon = new ImageIcon((URL)getClass().getResource("/Icons/config.png"));
 	private ImageIcon editIcon = new ImageIcon((URL)getClass().getResource("/Icons/edit.png"));
 	private ImageIcon infoIcon = new ImageIcon((URL)getClass().getResource("/Icons/info.png"));
 	private ImageIcon browserIcon = new ImageIcon((URL)getClass().getResource("/Icons/search.png"));
 	
 	//Icons for the menu bar
 	private ImageIcon addMenu = new ImageIcon((URL)getClass().getResource("/Icons/add_small.png"));
 	private ImageIcon editMenu = new ImageIcon((URL)getClass().getResource("/Icons/edit_small.png"));
 	private ImageIcon deleteMenu = new ImageIcon((URL)getClass().getResource("/Icons/delete_small.png"));
 	private ImageIcon importMenu = new ImageIcon((URL)getClass().getResource("/Icons/import_small.png"));
 	private ImageIcon exportMenu = new ImageIcon((URL)getClass().getResource("/Icons/export_small.png"));
 	private ImageIcon tuneIntoMenu = new ImageIcon((URL)getClass().getResource("/Icons/player_small.png"));
 	private ImageIcon musicfolderMenu = new ImageIcon((URL)getClass().getResource("/Icons/m_open_small.png"));
 	private ImageIcon recordMenu = new ImageIcon((URL)getClass().getResource("/Icons/record_small.png"));
 	private ImageIcon stopMenu = new ImageIcon((URL)getClass().getResource("/Icons/stop_small.png"));
 	private ImageIcon settingsMenu = new ImageIcon((URL)getClass().getResource("/Icons/settings_small.png"));
 	private ImageIcon aboutStreamRipStarMenu = new ImageIcon((URL)getClass().getResource("/Icons/streamRipStar_small.png"));
 	
 	//icon for the window
 	private ImageIcon windowIcon = new ImageIcon((URL)getClass().getResource("/Icons/streamRipStar.png"));
 	
 	private IconButton startRecordButton = new IconButton("Start",startRecordIcon);
 	private IconButton stopRecordButton = new IconButton("Stop",stopRecordIcon);
 	private IconButton scheduleButton = new IconButton("Schedule",schudleIcon);
 	private IconButton deleteButton = new IconButton("Delete",deleteIcon);
 	private IconButton editButton = new IconButton("Edit",editIcon);
 	private IconButton addButton = new IconButton("Add",addIcon);
 	private IconButton hearMusicButton = new IconButton("Hear",hearMusicIcon);
 	private IconButton openMusicFolderButton = new IconButton("Musicfolder",openMusicFolderIcon);
 	private IconButton exitButton = new IconButton("Exit",exitIcon);
 	private IconButton configButton = new IconButton("Preferences",configIcon);
 	private IconButton infoButton = new IconButton("Info",infoIcon);
 	private IconButton browseGenreButton = new IconButton("Browser",browserIcon);
 	
 //	here are listed all menuitems
 	private JMenuBar menu = new JMenuBar();
 	private JMenu streamMenu = new JMenu("Stream");
 	private JMenu programmMenu = new JMenu("Program");
 	private JMenu helpMenu = new JMenu("Help");
 
 	private JMenuItem startRecordStream = new JMenuItem("Start Recording",recordMenu);
 	private JMenuItem stopRecordStream = new JMenuItem("Stop Recording",stopMenu);
 	private JMenuItem addStream = new JMenuItem("Add",addMenu);
 	private JMenuItem editStream = new JMenuItem("Edit",editMenu);
 	private JMenuItem delStream = new JMenuItem("Delete",deleteMenu);
 	private JMenuItem importStream = new JMenuItem("Import",importMenu);
 	private JMenuItem exportStream = new JMenuItem("Export",exportMenu);
 	private JMenuItem streamBrowserItem = new JMenuItem("Streambrowser");
 	private JMenuItem schedulManagerItem = new JMenuItem("Schedulmanager");
 	private JMenuItem tuneInto = new JMenuItem("Listen To",tuneIntoMenu);
 	private JMenuItem openWebsite = new JMenuItem("Open Website");
 	private JMenuItem streamRipStarPref = new JMenuItem("Preferences",settingsMenu);
 	private JMenuItem openMusicFolder = new JMenuItem("Open Musicfolder",musicfolderMenu);
 	private JMenuItem streamOptions = new JMenuItem("Options");
 	private JMenuItem stream4AllOptions = new JMenuItem("Overwrite Options For Every Stream");
 	private JMenuItem streamDefaultOptions = new JMenuItem("Edit The Default Options For A New Stream");
 	private JMenuItem playNextStream = new JMenuItem("Play Next Stream");
 	private JMenuItem playLastStream = new JMenuItem("Play Last Stream");
 	private JMenuItem onlineHelp = new JMenuItem("Online Help");
 	private JMenuItem updateMenuItem = new JMenuItem("Look for an Update");
 	private JMenuItem about = new JMenuItem("About StreamRipStar",aboutStreamRipStarMenu);
 	private JMenuItem viewLog = new JMenuItem("Show log messages",aboutStreamRipStarMenu);
 	private JMenuItem streamRipStarSite = new JMenuItem("Go to StreamRipStars Website");
 	private JMenuItem exit = new JMenuItem("Exit");
 	
 	private JMenuItem startRecPopupIcon = new JMenuItem("Start Recording",recordMenu);
 	private JMenuItem stopRecPopupIcon = new JMenuItem("Stop Recording",stopMenu);
 	private JMenuItem tuneStreamPopupIcon = new JMenuItem("Listening",tuneIntoMenu);
 	private JMenuItem optionPopupIcon = new JMenuItem("Optionen");
 	
 	private JPopupMenu tablePopup = new JPopupMenu();
 	
 	private Font textUnderIconsFont = new Font("Dialog", Font.PLAIN, 10);
 
 	//sys tray icon
 	private TrayIcon trayIcon = null;
 	private SystemTray sysTray = null;
 	//all components of the iconbar
 	private JToolBar iconBar = new JToolBar();
 	
 	//if openPreferences == true -> it opens the preferences 
 	public Gui_StreamRipStar(Boolean openPreferences)
 	{
 		super("StreamRipStar");
 		setIconImage( windowIcon.getImage() );
 
 		controlStreams = new Control_Stream(this);
 		table = new Gui_TablePanel(controlStreams,this);
 		addWindowListener(this);
 		setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
         Container contPane = getContentPane();
 
         BorderLayout mainLayout = new BorderLayout();
         contPane.setLayout(mainLayout);
        
 		contPane.add(iconBar,BorderLayout.PAGE_START);
         contPane.add(table, BorderLayout.CENTER);	//Add that shows all streams
         
         buildMenuBar();
        setLanguage();
         loadProp();
         buildIconBar();
 		setSystemTray();
 		
 		//and pre-load the audio system; if it fails, the internal audio player is
 		//disabled automatically 
 		table.loadFirstAudioPlayer();
 		
 		if(useInternalPlayer) {
 			
 			//load the audio panel
 			audioPanel = new InternAudioControlPanel(this,volumeManager);
 			contPane.add(this.audioPanel, BorderLayout.SOUTH);
 			hearMusicButton.setEnabled(false);
 
 		}
 		
         //Center StreamRipStar
         //get size of window
         Dimension frameDim = getSize();
         //get resolution
         Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
         //calculates the app. values
         int x = (screenDim.width - frameDim.width)/2;
         int y = (screenDim.height - frameDim.height)/2;
         //set location
         setLocation(x, y);
         setVisible(true);
         
         //create object to control the next 2 Threads
         Control_Threads controlThreads = new Control_Threads();
         
         // start filling the table with streams
         Thread_FillTableWithStreams fill =  new Thread_FillTableWithStreams(controlStreams,table,controlThreads);
         fill.start();
         
         //the schedul control is an thread -> start it
 		controlJob = new Thread_Control_Schedul(this,controlThreads);
 		controlJob.start();
 		
         //if preferences should open, do it here
         if(openPreferences) 
         {
     		JOptionPane.showMessageDialog(this, trans.getString("firstTime"));
     		new Gui_Settings2(this);
         }
 	}
 //
 //	public ResourceBundle getTrans() {
 //		return trans;
 //	}
 	
 	/**
 	 * enables the system tray and set
 	 * the right properties
 	 */
 	private void setSystemTray() {
 		if(tray) {
 			if (SystemTray.isSupported()) {
 				if(sysTray != null)
 					sysTray.remove(trayIcon);
 				setDefaultCloseOperation( JFrame.HIDE_ON_CLOSE );
 			    sysTray = SystemTray.getSystemTray();
 			    Image image =  new ImageIcon((URL)getClass().getResource("/Icons/streamRipStar.png")).getImage();
 			    MouseListener mouseListener = new MouseListener() {
 			        public void mouseClicked(MouseEvent e) {
 			        	changeStatus();
 			        }
 			        public void mouseEntered(MouseEvent e) {}
 			        public void mouseExited(MouseEvent e) {}
 			        public void mousePressed(MouseEvent e) {}
 			        public void mouseReleased(MouseEvent e) {}
 			    };
 			    
 			    //build right-click menu
 			    PopupMenu popup = new PopupMenu();
 			    MenuItem exitItem = new MenuItem("Exit StreamRipStar");
 			    MenuItem showMainWindowItem = new MenuItem("Show StreamRipStar");
 			    exitItem.addActionListener(new ExitListener(Gui_StreamRipStar.this));
 			    showMainWindowItem.addActionListener(new ShowStreamRipStarListener());
 			    
 			    //visible
 			    popup.add(showMainWindowItem);
 			    popup.addSeparator();
 			    popup.add(exitItem);
 	
 			    trayIcon = new TrayIcon(image, "StreamRipStar", popup); 
 			    trayIcon.setImageAutoSize(true);
 			    trayIcon.addMouseListener(mouseListener);
 	
 			    try {
 			        sysTray.add(trayIcon);
 			    } catch (AWTException e) {
 			        SRSOutput.getInstance().logE("TrayIcon could not be added.");
 			    }
 			}
 		} else {
 			if(sysTray != null)
 				sysTray.remove(trayIcon);
 		}
 		
 	}
 	
 	/**
 	 * Displays a message in the systemtray
 	 * @param Message
 	 */
 	public void showMessageInTray(String Message) {
 		if(trayIcon != null) {
 			trayIcon.displayMessage("StreamRipStar",Message,
 					TrayIcon.MessageType.INFO );
 		}
 	}
 	
 
 	/**
 	 * add all JButton to the icon bar
 	 */
 	public void  buildIconBar() {	
 		//add Button to the bar
 		iconBar.add(startRecordButton);
 		iconBar.add(stopRecordButton);
 		if(!useInternalPlayer)
 		{
 			iconBar.addSeparator();
 			iconBar.add(hearMusicButton);
 		}
 		iconBar.addSeparator();
 		iconBar.add(scheduleButton);
 		iconBar.add(infoButton);
 		iconBar.addSeparator();
 		iconBar.add(deleteButton);
 		iconBar.add(editButton);
 		iconBar.add(addButton);
 		iconBar.add(browseGenreButton);
 		iconBar.addSeparator();
 		iconBar.add(openMusicFolderButton);
 		iconBar.add(configButton);
 		iconBar.addSeparator();
 		iconBar.add(exitButton);
 		
 		//set the color of all buttons to backgroundcolor
 		//from the iconbar and disable the Border
 		iconBar.setBackground(new Color(238,238,238,255));
 		menu.setBackground(new Color(238,238,238,255));
 		
 		//Fallback Tooltips
 		startRecordButton.setToolTipText("Start ripping");
 		stopRecordButton.setToolTipText("Stop ripping");
 		scheduleButton.setToolTipText("Schedule");
 		deleteButton.setToolTipText("Delete stream");
 		editButton.setToolTipText("Edit stream");
 		addButton.setToolTipText("Add stream");
 		hearMusicButton.setToolTipText("Hear stream");
 		exitButton.setToolTipText("Exit");
 		configButton.setToolTipText("Preferences");
 		openMusicFolderButton.setToolTipText("Open musicfolder");
 		infoButton.setToolTipText("Show Information about this Stream");
 		browseGenreButton.setToolTipText("Show stream browser");
 		
 		addButton.addActionListener(new AddStreamListener());
 		configButton.addActionListener(new PreferencesListener());
 		exitButton.addActionListener(new ExitListener(this));
 		startRecordButton.addActionListener(new StartRecordListener());
 		stopRecordButton.addActionListener(new StopRecordListener());
 		deleteButton.addActionListener(new DeleteListener());
 		editButton.addActionListener(new EditStreamListener());
 		hearMusicButton.addActionListener(new PlayMusikListener());
 		openMusicFolderButton.addActionListener(new OpenMusikFolder());
 		scheduleButton.addActionListener(new ScheduleListener());
 		infoButton.addActionListener(new ShowStatsListener());
 		browseGenreButton.addActionListener(new StreamBrowserListener());
 	}
 
 	
 //	Build menu structure
 	public void buildMenuBar() {
 		setJMenuBar(menu);
 		
 		menu.add(programmMenu);
 		menu.add(streamMenu);	
 		menu.add(helpMenu);
 		
 		streamMenu.add(startRecordStream);
 		streamMenu.add(stopRecordStream);
 		streamMenu.addSeparator();
 		streamMenu.add(addStream);
 		streamMenu.add(editStream);
 		streamMenu.add(delStream);
 		streamMenu.addSeparator();
 		streamMenu.add(tuneInto);
 		streamMenu.add(openWebsite);
 		streamMenu.add(streamOptions);
 		streamMenu.addSeparator();
 		streamMenu.add(stream4AllOptions);
 		streamMenu.addSeparator();
 		streamMenu.add(streamDefaultOptions);
 		streamMenu.addSeparator();
 		streamMenu.add(playLastStream);
 		streamMenu.add(playNextStream);
 		
 		programmMenu.add(streamRipStarPref);
 		programmMenu.add(importStream);
 		programmMenu.add(exportStream);
 		programmMenu.add(streamBrowserItem);
 		programmMenu.add(schedulManagerItem);
 		programmMenu.add(openMusicFolder);
 		programmMenu.addSeparator();
 		programmMenu.add(exit);
 		
 		helpMenu.add(onlineHelp);
 		helpMenu.add(streamRipStarSite);
 		helpMenu.add(updateMenuItem);
 		helpMenu.add(viewLog);
 		helpMenu.add(about);
 		
 //		set shortcuts
 		addStream.setAccelerator(KeyStroke.getKeyStroke(
 		        KeyEvent.VK_A, ActionEvent.CTRL_MASK));
 		editStream.setAccelerator(KeyStroke.getKeyStroke(
 		        KeyEvent.VK_E, ActionEvent.CTRL_MASK));
 		tuneInto.setAccelerator(KeyStroke.getKeyStroke(
 		        KeyEvent.VK_T, ActionEvent.CTRL_MASK));
 		streamOptions.setAccelerator(KeyStroke.getKeyStroke(
 		        KeyEvent.VK_S, ActionEvent.CTRL_MASK));
 		exit.setAccelerator(KeyStroke.getKeyStroke(
 		        KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
 		streamRipStarPref.setAccelerator(KeyStroke.getKeyStroke(
 		        KeyEvent.VK_C, ActionEvent.CTRL_MASK));
 		openMusicFolder.setAccelerator(KeyStroke.getKeyStroke(
 		        KeyEvent.VK_D, ActionEvent.CTRL_MASK));
 		startRecordStream.setAccelerator(KeyStroke.getKeyStroke(
 		        KeyEvent.VK_R, ActionEvent.CTRL_MASK));
 		stopRecordStream.setAccelerator(KeyStroke.getKeyStroke(
 		        KeyEvent.VK_W, ActionEvent.CTRL_MASK));
 		delStream.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0));
 		playLastStream.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP,0));
 		playNextStream.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN,0));
 
 //		add listener
 		streamRipStarPref.addActionListener(new PreferencesListener());
 		importStream.addActionListener(new ImportListener());
 		exportStream.addActionListener(new ExportListener());
 		schedulManagerItem.addActionListener(new ScheduleListener());
 		streamBrowserItem.addActionListener(new StreamBrowserListener());
 		addStream.addActionListener(new AddStreamListener());
 		editStream.addActionListener(new EditStreamListener());
 		delStream.addActionListener(new DeleteListener());
 		tuneInto.addActionListener(new PlayMusikListener());
 		exit.addActionListener(new ExitListener(Gui_StreamRipStar.this));
 		openMusicFolder.addActionListener(new OpenMusikFolder());
 		openWebsite.addActionListener(new BrowserListener());
 		about.addActionListener(new AboutDialogListener());
 		viewLog.addActionListener(new ViewLogDialogListener());
 		streamRipStarSite.addActionListener(new GoToWebSiteListener());
 		startRecordStream.addActionListener(new StartRecordListener());
 		stopRecordStream.addActionListener(new StopRecordListener());
 		onlineHelp.addActionListener(new GoToHelpSiteListener());
 		updateMenuItem.addActionListener(new SearchUpdateListener());
 		streamOptions.addActionListener(new EditStreamListener());
 		stream4AllOptions.addActionListener(new Edit4AllStreamListener());
 		streamDefaultOptions.addActionListener(new EditDefaultStreamListener());
 		playLastStream.addActionListener(new PlayPreviousStreamListener());
 		playNextStream.addActionListener(new PlayNextStreamListener());
 		
 		startRecPopupIcon.addActionListener(new StartRecordListener());
 		stopRecPopupIcon.addActionListener(new StopRecordListener());
 		tuneStreamPopupIcon.addActionListener(new PlayMusikListener());
 		optionPopupIcon.addActionListener(new EditStreamListener());
 		
 		//build pop up menu
 		table.setTTablePopup(tablePopup);
 		tablePopup.add(startRecPopupIcon);
 		tablePopup.add(stopRecPopupIcon);
 		tablePopup.add(tuneStreamPopupIcon);
 		tablePopup.add(optionPopupIcon);
 	}
 	
 	private void setTextUnderIcons() {
 		//if showTextunderButtons is true, the text will
 		//not be shown 
 		if(!showText) 
 		{
 			startRecordButton.setText(null);
 			stopRecordButton.setText(null);
 			hearMusicButton.setText(null);
 			scheduleButton.setText(null);
 			infoButton.setText(null);
 			deleteButton.setText(null);
 			editButton.setText(null);
 			addButton.setText(null);
 			openMusicFolderButton.setText(null);
 			configButton.setText(null);
 			exitButton.setText(null);
 			browseGenreButton.setText(null);
 		} 
 		
 		else
 		{
 			
 			//set the translated text under icons
 			try 
 			{
 				startRecordButton.setText(trans.getString("mainWin.startRecordButton"));
 				stopRecordButton.setText(trans.getString("mainWin.stopRecordButton"));
 				scheduleButton.setText(trans.getString("mainWin.scheduleButton"));
 				deleteButton.setText(trans.getString("mainWin.deleteButton"));
 				editButton.setText(trans.getString("mainWin.editButton"));
 				addButton.setText(trans.getString("mainWin.addButton"));
 				hearMusicButton.setText(trans.getString("mainWin.hearMusicButton"));
 				openMusicFolderButton.setText(trans.getString("mainWin.openMusicFolderButton"));
 				exitButton.setText(trans.getString("mainWin.exitButton"));
 				configButton.setText(trans.getString("mainWin.configButton"));
 				infoButton.setText(trans.getString("mainWin.infoButton"));
 				browseGenreButton.setText(trans.getString("mainWin.browseGenreButton"));
 			}
 			catch (MissingResourceException e)
 			{
 				SRSOutput.getInstance().logE("Could not find an translation (Text under Icons)");
 			}
 			
 			//set an smaller font
 			startRecordButton.setFont(textUnderIconsFont);
 			stopRecordButton.setFont(textUnderIconsFont);
 			scheduleButton.setFont(textUnderIconsFont);
 			deleteButton.setFont(textUnderIconsFont);
 			editButton.setFont(textUnderIconsFont);
 			addButton.setFont(textUnderIconsFont);
 			hearMusicButton.setFont(textUnderIconsFont);
 			openMusicFolderButton.setFont(textUnderIconsFont);
 			exitButton.setFont(textUnderIconsFont);
 			configButton.setFont(textUnderIconsFont);
 			infoButton.setFont(textUnderIconsFont);
 			browseGenreButton.setFont(textUnderIconsFont);
 		}
 		//if there is a use of the intern player, update the text, too
 		if(useInternalPlayer & audioPanel != null) 
 		{
 			audioPanel.setTextUnderIcons(showText);
 		}
 	
 	}
 	
 	private void setLanguage() {
 		try {
 			//Menubar
 			streamMenu.setText(trans.getString("stream"));
 			programmMenu.setText(trans.getString("program"));
 			helpMenu.setText(trans.getString("helpMenu"));
 			//Menubar programs
 			streamRipStarPref.setText(trans.getString("pref"));
 			importStream.setText(trans.getString("importStream"));
 			exportStream.setText(trans.getString("exportStream"));
 			streamBrowserItem.setText(trans.getString("mainWin.streamBrowserItem"));
 			schedulManagerItem.setText(trans.getString("mainWin.schedulManagerItem"));
 			openMusicFolder.setText(trans.getString("openMusic"));
 			exit.setText(trans.getString("exit"));
 			//Menubar stream
 			startRecordStream.setText(trans.getString("popup.startRec"));
 			stopRecordStream.setText(trans.getString("popup.stopRec"));
 			addStream.setText(trans.getString("add"));
 			editStream.setText(trans.getString("edit"));
 			delStream.setText(trans.getString("delete"));
 			tuneInto.setText(trans.getString("tune"));
 			openWebsite.setText(trans.getString("stream_website"));
 			streamOptions.setText(trans.getString("options"));
 			stream4AllOptions.setText(trans.getString("4AllOptions"));
 			streamDefaultOptions.setText(trans.getString("DefaultOptions"));
 			
 			//Menubar help
 			onlineHelp.setText(trans.getString("onlineHelp"));
 			streamRipStarSite.setText(trans.getString("streamRipStarWebsite"));
 			updateMenuItem.setText(trans.getString("checkUpdateItem"));
 			about.setText(trans.getString("about"));
 			viewLog.setText(trans.getString("mainWin.ViewLog"));
 			playNextStream.setText(trans.getString("mainWin.PlayNextStream"));
 			playLastStream.setText(trans.getString("mainWin.PlayerLastStream"));
 			
 			//Tooltip
 			editButton.setToolTipText(trans.getString("toolTip.editButton"));
 			startRecordButton.setToolTipText(trans.getString("toolTip.startRecordButton"));
 			stopRecordButton.setToolTipText(trans.getString("toolTip.stopRecordButton"));
 			scheduleButton.setToolTipText(trans.getString("toolTip.scheduleButton"));
 			deleteButton.setToolTipText(trans.getString("toolTip.deleteButton"));
 			addButton.setToolTipText(trans.getString("toolTip.addButton"));
 			hearMusicButton.setToolTipText(trans.getString("toolTip.hearMusicButton"));
 			exitButton.setToolTipText(trans.getString("toolTip.exitButton"));
 			configButton.setToolTipText(trans.getString("toolTip.configButton"));
 			openMusicFolderButton.setToolTipText(trans.getString("toolTip.openMusicFolderButton"));
 			infoButton.setToolTipText(trans.getString("toolTip.info"));
 			browseGenreButton.setToolTipText(trans.getString("toolTip.streambrowser"));
 			
 			//text under icons
 			startRecordButton.setText(trans.getString("mainWin.startRecordButton"));
 			stopRecordButton.setText(trans.getString("mainWin.stopRecordButton"));
 			scheduleButton.setText(trans.getString("mainWin.scheduleButton"));
 			deleteButton.setText(trans.getString("mainWin.deleteButton"));
 			editButton.setText(trans.getString("mainWin.editButton"));
 			addButton.setText(trans.getString("mainWin.addButton"));
 			hearMusicButton.setText(trans.getString("mainWin.hearMusicButton"));
 			openMusicFolderButton.setText(trans.getString("mainWin.openMusicFolderButton"));
 			exitButton.setText(trans.getString("mainWin.exitButton"));
 			configButton.setText(trans.getString("mainWin.configButton"));
 			infoButton.setText(trans.getString("mainWin.infoButton"));
 			browseGenreButton.setText(trans.getString("mainWin.browseGenreButton"));
 
 			//the popup
 			startRecPopupIcon.setText(trans.getString("popup.startRec"));
 			stopRecPopupIcon.setText(trans.getString("popup.stopRec"));
 			tuneStreamPopupIcon.setText(trans.getString("popup.hear"));
 			optionPopupIcon.setText(trans.getString("popup.options"));
 			
 		} catch ( MissingResourceException e ) { 
 			SRSOutput.getInstance().logE( e.getMessage() ); 
 		}
 	}
 	
 	/**
 	 * Shows an error message in a popup in the mainwindow
 	 */
 	public void showErrorMessageInPopUp(String errorMessage) {
 		JOptionPane.showMessageDialog(Gui_StreamRipStar.this,
 				errorMessage, trans.getString("error"),
 				JOptionPane.ERROR_MESSAGE, null);
 	}
 	
 	/**
 	 * Sets a new Volume to the audioSlider (the volume Control)
 	 * @param newVolume the volume in percent from 0 up to 100
 	 */
 	public void setVolume(int newVolume) 
 	{
 		getTabel().setAudioVolume(newVolume);
 	}
 	
 	/**
 	 * Get an font for all text with are shown under icons in icon bars
 	 * @return: the Font as defined in Gui_StreamRipStar
 	 */
 	public Font getFontForTextUnderIcons() {
 		return textUnderIconsFont;
 	}
 	
 	/**
 	 * Get the variable witch controls the text under icons. If the variable
 	 * is true, the text is visible with the special font (getIconTextFont())
 	 * @return true if the text is shown
 	 */
 	public Boolean showTextUnderIcons() {
 		return showText;
 	}
 	
 	
 	/**
 	 * Gives the variable back, which indicates, if you should use the internal
 	 * audio player or should we use an extern?
 	 * @return true, if we should use an internal audio player
 	 */
 	public Boolean useInternalAudioPlayer() {
 		return useInternalPlayer;
 	}
 	
 	/**
 	 * Set the variable to use the internal audio player
 	 * @param useItNow true if we should use, else false
 	 */
 	public void setUseInternalAudioPlayer(Boolean useItNow)
 	{ 
 		useInternalPlayer = useItNow;
 	}
 	
 	/**
 	*loads old window properties some components
 	*if they not exist, use pack() to set them automatic
 	*/
 	public void loadProp() {
 		
 		//first, try to load settings for the mainwindow
 		String loadPath =  new Control_GetPath().getStreamRipStarPath();
 		try {
 			XMLInputFactory factory = XMLInputFactory.newInstance(); 
 			XMLStreamReader parser;
 			parser = factory.createXMLStreamReader( new FileInputStream(loadPath+"/Prefs-MainWindow.xml" ) );
 			while ( parser.hasNext() ) { 
 	 
 				switch ( parser.getEventType() ) { 
 					case XMLStreamConstants.START_DOCUMENT: 
 						SRSOutput.getInstance().log( "Loading file Prefs-MainWindow.xml" ); 
 						break; 
 				 
 				    case XMLStreamConstants.END_DOCUMENT: 
 				    	SRSOutput.getInstance().log( "End of read settings " ); 
 				    	parser.close(); 
 				    	break; 
 				 
 				    case XMLStreamConstants.START_ELEMENT: 
 				    	int[] widths = new int[3];
 				    	int hight = 0;
 				    	int width = 0;
 				    	for ( int i = 0; i < parser.getAttributeCount(); i++ ) {
 				    		if (parser.getAttributeLocalName( i ).equals("winSizeWidth")) {
 				    			width = Integer.valueOf(parser.getAttributeValue(i));
 				    		} else if (parser.getAttributeLocalName( i ).equals("winSizeHeight")) {
 				    			hight = Integer.valueOf(parser.getAttributeValue(i));
 				    		} else if(parser.getAttributeLocalName( i ).equals("tableCol1")) {
 				    			widths[0] = Integer.valueOf(parser.getAttributeValue(i));
 				    		} else if (parser.getAttributeLocalName( i ).equals("tableCol2")) {
 				    			widths[1] = Integer.valueOf(parser.getAttributeValue(i));
 				    		} else if (parser.getAttributeLocalName( i ).equals("tableCol3")) {
 				    			widths[2] = Integer.valueOf(parser.getAttributeValue(i));
 				    		}
 				    	}
 				    	table.setColumWidths(widths);
 				    	setSize(new Dimension(width,hight));
 				    	break; 
 				 
 				    default: 
 				    	break; 
 				  }
 				parser.next(); 
 			}
 
 		} catch (FileNotFoundException e) {
 			SRSOutput.getInstance().logE("No configuartion file found: Prefs-MainWindow.xml");
 			pack();
 		} catch (XMLStreamException e) {
 			e.printStackTrace();
 			pack();
 		}
 		
 		//second, load and set path, settings etc from settingsfile
 		loadPath =  new Control_GetPath().getStreamRipStarPath();
 		try {
 			XMLInputFactory factory = XMLInputFactory.newInstance(); 
 			XMLStreamReader parser;
 			parser = factory.createXMLStreamReader( new FileInputStream(loadPath+"/Settings-StreamRipStar.xml" ) );
 			while ( parser.hasNext() ) { 
 	 
 				switch ( parser.getEventType() ) { 
 					case XMLStreamConstants.START_DOCUMENT: 
 						SRSOutput.getInstance().log( "Loading file Settings-StreamRipStar.xml" ); 
 						break; 
 				 
 				    case XMLStreamConstants.END_DOCUMENT: 
 				    	SRSOutput.getInstance().log( "End of read settings " ); 
 				    	parser.close(); 
 				    	break; 
 				 
 				    case XMLStreamConstants.START_ELEMENT: 
 				    	String[] path = new String[5];
 				    	int level = 0;		//loglevel
 				    	
 				    	for ( int i = 0; i < parser.getAttributeCount(); i++ ) {
 				    		if(parser.getAttributeLocalName( i ).equals("activeTrayIcon")) {
 				    			tray = Boolean.valueOf(parser.getAttributeValue(i));
 				    		}
 				    		else if (parser.getAttributeLocalName( i ).equals("showTextCB")) {
 				    			showText = Boolean.valueOf(parser.getAttributeValue(i));
 				    		}
 				    		else if (parser.getAttributeLocalName( i ).equals("useInternalAudioPlayerCB")) {
 				    			useInternalPlayer = Boolean.valueOf(parser.getAttributeValue(i));
 				    		}
 				    		else if (parser.getAttributeLocalName( i ).equals("ripperPathTF")) {
 				    			path[0] = parser.getAttributeValue(i);
 				    		}
 				    		else if (parser.getAttributeLocalName( i ).equals("shoutcastTF")) {
 				    			path[1] = parser.getAttributeValue(i);
 				    		}
 				    		else if (parser.getAttributeLocalName( i ).equals("generellPathTF")) {
 				    			path[2] = parser.getAttributeValue(i);
 				    		}
 				    		else if (parser.getAttributeLocalName( i ).equals("fileBrowserTF")) {
 				    			path[4] = parser.getAttributeValue(i);
 				    		}
 				    		else if (parser.getAttributeLocalName( i ).equals("webBrowserTF")) {
 				    			path[3] = parser.getAttributeValue(i);
 				    		}
 				    		else if (parser.getAttributeLocalName( i ).equals("statusBox_index")) {
 				    			action0 = Integer.valueOf(parser.getAttributeValue(i));
 				    		}
 				    		else if (parser.getAttributeLocalName( i ).equals("nameBox_index")) {
 				    			action1 = Integer.valueOf(parser.getAttributeValue(i));
 				    		}
 				    		else if (parser.getAttributeLocalName( i ).equals("currentTrackBox_index")) {
 				    			action2 = Integer.valueOf(parser.getAttributeValue(i));
 				    		}
 				    		else if (parser.getAttributeLocalName( i ).equals("windowActionBox_index")) {
 				    			winAction = Integer.valueOf(parser.getAttributeValue(i));
 				    		} else if (parser.getAttributeLocalName( i ).equals("logLevel_index")) {
 				    			level = Integer.valueOf(parser.getAttributeValue(i));
 				    		}
 				    	}
 				    	controlStreams.setPaths(path);
 						setTextUnderIcons();
 						switch(level) 
 						{
 							case 0:
 								SRSOutput.getInstance().setLoglevel(SRSOutput.LOGLEVEL.Nothing);
 								break;
 							case 1:
 								SRSOutput.getInstance().setLoglevel(SRSOutput.LOGLEVEL.Error);
 								break;
 							case 2:
 								SRSOutput.getInstance().setLoglevel(SRSOutput.LOGLEVEL.Normal);
 								break;
 							case 3:
 								SRSOutput.getInstance().setLoglevel(SRSOutput.LOGLEVEL.Debug);
 								break;
 							default:
 								SRSOutput.getInstance().setLoglevel(SRSOutput.LOGLEVEL.Normal);	
 						}
 				    	break; 
 				 
 				    default: 
 				    	break; 
 				  }
 				parser.next(); 
 			}
 
 		} catch (FileNotFoundException e) {
 			SRSOutput.getInstance().logE("No configuartion file found: Settings-StreamRipStar.xml");
 		} catch (XMLStreamException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * try to save the window properties some components
 	 * for the next start
 	 */
 	public void saveProp() {
 		String savePath =  new Control_GetPath().getStreamRipStarPath();
 		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance(); 
 		
 		try {
 			XMLEventWriter writer = outputFactory.createXMLEventWriter(
 					new FileOutputStream(savePath+"/Prefs-MainWindow.xml" ) );
 			XMLEventFactory eventFactory = XMLEventFactory.newInstance();
 			
 			//header for the file
 			XMLEvent header = eventFactory.createStartDocument();
 			XMLEvent startRootSettings = eventFactory.createStartElement( "", "", "Prefs" );
 
 			XMLEvent winSizeWidth = eventFactory.createAttribute( "winSizeWidth",  String.valueOf(getSize().width )); 
 			XMLEvent winSizeHeight = eventFactory.createAttribute( "winSizeHeight",  String.valueOf(getSize().height )); 
 			XMLEvent tableCol1 = eventFactory.createAttribute( "tableCol1",  String.valueOf( table.getColumnWidths()[0])); 
 			XMLEvent tableCol2 = eventFactory.createAttribute( "tableCol2",  String.valueOf( table.getColumnWidths()[1])); 
 			XMLEvent tableCol3 = eventFactory.createAttribute( "tableCol3",  String.valueOf( table.getColumnWidths()[2])); 
 			
 			XMLEvent endRoot = eventFactory.createEndElement( "", "", "Prefs" ); 
 			XMLEvent endDocument = eventFactory.createEndDocument();
 			
 			//finally write into file
 			writer.add( header ); 
 			writer.add( startRootSettings );
 			
 			writer.add( winSizeWidth ); 
 			writer.add( winSizeHeight ); 
 			writer.add( tableCol1 ); 
 			writer.add( tableCol2 ); 
 			writer.add( tableCol3 ); 
 			
 			writer.add( endRoot ); 
 			writer.add( endDocument ); 
 			writer.close();
 
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (XMLStreamException e) {
 			e.printStackTrace();
 		} 
 	}
 	
 	public void closeAll() {	
 		if(Stream.activeStreams > 0) {
 			int i = JOptionPane.showConfirmDialog(Gui_StreamRipStar.this,
 					trans.getString("countStream")+" "+Stream.activeStreams+
 					"\n"+trans.getString("realyExit"),
 			"streamRipStarExit",JOptionPane.YES_NO_OPTION);
 			if (i == 0) {
 				//save the settings for the mainwindow
 				saveProp();
 				
 				//stop all recording streams
 				controlStreams.stopAllStreams();
 				
 				//save the stream vector
 				controlStreams.saveStreamVector();
 				
 				//save the vector with all schedule jobs
 				controlJob.saveScheduleVector();
 				
 				System.exit(0);
 			}
 		}
 		else {
 			//save the settings for the mainwindow
 			saveProp();
 			
 			//stop all recording streams
 			controlStreams.stopAllStreams();
 			
 			//save the stream vector
 			controlStreams.saveStreamVector();
 			
 			//save the vector with all schedule jobs
 			controlJob.saveScheduleVector();
 			
 			System.exit(0);
 		}
 	}
 	
 	public Gui_TablePanel getTabel() {
 		return table;
 	}
 	
 	public Control_Stream getControlStream() {
 		return controlStreams;
 	}
 	
 	public VolumeManager getVolumeManager()
 	{
 		return volumeManager;
 	}
 	
 	/**
 	 * Set the title of the current song in the status field
 	 */
 	public void setTitleForAudioPlayer(String streamName, String title,boolean isErrorMessage) {
 		if(title != null && audioPanel != null) 
 		{
 			String gesTitle = streamName + " : " + title;
 			audioPanel.setTitle(gesTitle,isErrorMessage);
 			
 			//if the title is empty, change the title to 
 			//the program name
 			if(title == null || title.equals(""))
 			{
 				editWindowTitle("StreamRipStar");
 			}
 			else
 			{
 				editWindowTitle(title);
 			}
 			//show title messages in the streamrbowser, too
 			if(streamBrowser != null) 
 			{
 				streamBrowser.setStatusText(gesTitle,isErrorMessage);
 			}
 		}
 	}
 
 	
 	/**
 	 * 
 	 * @param actionNumber
 	 * @return the selected index from the box in settings
 	 */
 	public int getAction(int actionNumber) {
 		if (actionNumber == 0)
 			return action0;
 		else if (actionNumber == 1)
 			return action1;
 		else if (actionNumber == 2)
 			return action2;
 		else
 			return -1;
 	}
 	
 	/**
 	 * Set the variables for the activation on some actions
 	 * @param actions: What should happen, if you click on a cell in the table
 	 * @param newShowText: Should we show the text under icons?
 	 * @param newTray: Is the systemtray active?
 	 * @param useInternalPlayer: Shall we listen to an stream, with the internal audio player?
 	 */
 	public void setNewRuntimePrefs(int[] actions, Boolean newShowText, Boolean newTray,
 			String newlnfClassName, boolean useInternalPlayer) {
 		action0 = actions[0];
 		action1 = actions[1];
 		action2 = actions[2];
 		winAction = actions[3];
 		
 		showText = newShowText;
 		this.useInternalPlayer = useInternalPlayer;
 		tray = newTray;
 		
 		//enable / disable systray icon
 		setSystemTray();
 		
 		//show/hide text under icons
 		setTextUnderIcons();
 		
 		//update Look and Feel
 		if(newlnfClassName != null) {
 			updateLookAndFeel(newlnfClassName);
 		}
 	}
 	
 	/**
 	 * try to set an new look and feel
 	 * @param newlnfClassName: the class name of the new look and feel
 	 */
 	public void updateLookAndFeel(String newlnfClassName) {
 		if(newlnfClassName != null) {
 			try {
 				UIManager.setLookAndFeel(newlnfClassName);
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			} catch (InstantiationException e) {
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 			} catch (UnsupportedLookAndFeelException e) {
 				e.printStackTrace();
 			}
 			SwingUtilities.updateComponentTreeUI(Gui_StreamRipStar.this);
 		}
 	}
 	
 	/**
 	 * edit the first selected stream
 	 *
 	 */
 	public void editStream() {
 		if(getTabel().isTHSelected()) {
 			//If its currently recording -> show waring
 			if(table.getSelectedStream()[0].getStatus()) {
 				JOptionPane.showMessageDialog(Gui_StreamRipStar.this
 						,trans.getString("noEditRecord"));
 			}
 			
 			new Gui_StreamOptions(table.getSelectedStream()[0], Gui_StreamRipStar.this, false, true,false);
 		}
 		else {
 			JOptionPane.showMessageDialog(Gui_StreamRipStar.this
 					,trans.getString("select"));
 		}
 	}
 	
 	/**
 	 * Stop an stream, by given the stream object. This method will
 	 * stop fetching the names from the stream, stop recording and 
 	 * reset the optical status on the streamtable
 	 * @param stream
 	 */
 	public void stopRippingUnselected(Stream stream, boolean userStoppsRecording) {
 		stream.setStop(userStoppsRecording);
 	}
 	
 	/**
 	 * Stop all selected streams. If there are not ripping, they
 	 * will not stop. Nothing will happen in this case.
 	 */
 	public void stopRippingSelected() {
 		Stream[] streamsToStop = table.getSelectedStream();
 		
 		for( int i=0 ; i< streamsToStop.length; i++) {
 			
 			//if the stream doesn't exist anymore, show an errormessage
 			if (streamsToStop[i] == null) {
 				JOptionPane.showInputDialog(trans.getString("stopError"));
 			} else {
 				streamsToStop[i].setStop(true);
 			}
 		}
 	}
 	
 	public void startRippingUnselected(Stream recStream) {
 		//start only, if its not ripping
 		if(!recStream.getStatus()) {
 			Process p = getControlStream().startStreamripper(recStream);
 			if(p == null) {
 				JOptionPane.showMessageDialog(Gui_StreamRipStar.this,trans.getString("exeError"));
 				SRSOutput.getInstance().logE("Error while exec streamripper");
 			} else {
 				recStream.setProcess(p);
 				recStream.setStatus(true);
 				recStream.increaseRippingCount();
 				int row = getTabel().getNewRowForNameForUpdate(recStream.name);
 				Thread_UpdateName updateName = new Thread_UpdateName(recStream,row,getTabel());
 				updateName.start();
 				recStream.setUpdateName(updateName);
 			}
 		}
 	}
 	
 	/**
 	 * Start all selected streams, or show an error message
 	 */
 	public void startRippingSelected() {
 		if(getTabel().isTHSelected()) {
 			Stream[] streamsToRecord = table.getSelectedStream();
 			
 			for( int i=0 ; i <streamsToRecord.length; i++) {
 				if (streamsToRecord[i] == null) {
 					JOptionPane.showInputDialog(trans.getString("exeError"));
 				} else {
 					if(!streamsToRecord[i].getStatus()) {
 						Process p = getControlStream().startStreamripper(streamsToRecord[i]);
 						if(p == null) {
 							JOptionPane.showMessageDialog(Gui_StreamRipStar.this,trans.getString("exeError"));
 							SRSOutput.getInstance().logE("Error while exec streamripper");
 						} else {
 							streamsToRecord[i].increaseRippingCount();
 							streamsToRecord[i].setProcess(p);
 							streamsToRecord[i].setStatus(true);
 							Thread_UpdateName updateName = new Thread_UpdateName(streamsToRecord[i],getTabel().getSelectedRow(),getTabel());
 							updateName.start();
 							streamsToRecord[i].setUpdateName(updateName);
 						}
 					}
 				}
 			}
 			
 		}
 		else
 			JOptionPane.showMessageDialog(Gui_StreamRipStar.this
 					,trans.getString("select"));
 	}
 	
 	public void openExportGui() {
 		new Export_Streams(controlStreams,this);
 	}
 	
 	public void openImportGui() {
 		new Import_Streams(Gui_StreamRipStar.this,controlStreams);
 	}
 	
 	/**
 	 * Enter a new name for the frame. This is possible for every
 	 * String except null.
 	 * @param newTitle The new title
 	 */
 	public void editWindowTitle(String newTitle)
 	{
 		if(newTitle != null)
 		{
 			this.setTitle(newTitle);
 		}
 	}
 	
 	//switch the visibility of the mainwindow
 	public void changeStatus() {
 		if(this.isVisible()) {	
 			this.setVisible(false);
 		}
 		else {
 			setVisible(true);
 			setExtendedState(JFrame.NORMAL);
 			this.toFront();
 		}
 	}	
 	
 //
 //	From here are listed all listener from the menu
 //	
 	
 	class ImportListener  implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			openImportGui();
 		}
 	}
 	
 	class ExportListener  implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			openExportGui();
 		}
 	}
 	
 	/**
 	 * Open the website of the first selected streams
 	 * @author eule
 	 *
 	 */
 	class BrowserListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			if(getTabel().isTHSelected()) {
 				String website = table.getSelectedStream()[0].website;
 				
 				if(website != null  && !website.equals("")) {
 					controlStreams.startWebBrowser(website);
 				}	
 				else
 					JOptionPane.showMessageDialog(Gui_StreamRipStar.this,trans.getString("setBrowser"));
 			}
 			else
 				JOptionPane.showMessageDialog(Gui_StreamRipStar.this
 						,trans.getString("select"));
 		}
 	}
 	
 	/**
 	 * Create a schedul manager window witch connects the
 	 * the schedul libary witch controls all tasks
 	 */
 	class ScheduleListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			new Gui_SchedulManager(controlStreams, controlJob);
 		}
 	}
 	
 	class PreferencesListener implements ActionListener
 	{
 		public void actionPerformed(ActionEvent e)
 		{
 			new Gui_Settings2(Gui_StreamRipStar.this);
 		}
 	}
 	
 	class AddStreamListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			new Gui_StreamOptions(null,Gui_StreamRipStar.this,true,true,false);
 		}
 	}
 	
 	class ExitListener implements ActionListener {
 		Gui_StreamRipStar mainWindow;
 		
 		public ExitListener(Gui_StreamRipStar mainWindow) {
 			this.mainWindow = mainWindow;
 		}
 		
 		public void actionPerformed(ActionEvent e) {
 				mainWindow.closeAll();
 		}
 	}
 	
 	class OpenMusikFolder implements ActionListener {
 		public void actionPerformed(ActionEvent e) {	
 			String browser = getControlStream().getFileBrowserPath();
 			String musicpath = getControlStream().getGeneralPath();
 			if(browser == null || browser.trim().equals("")) {
 				JOptionPane.showMessageDialog(Gui_StreamRipStar.this,trans.getString("confiFileBrower"));
 			} else {
 				if(musicpath == null || musicpath.trim().equals("")) {
 					JOptionPane.showMessageDialog(Gui_StreamRipStar.this,trans.getString("configGenralPath"));
 				} else {
 					new Control_RunExternProgram(browser +" "+ musicpath).run();
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Start the intern or extern audioplayer to with the selected stream.
 	 * If no Stream is selected, an error message is shown to the user
 	 */
 	public synchronized void playSelectedStream() 
 	{
 		if(getTabel().isTHSelected()) 
 		{
 			getTabel().startMusicPlayerWithSelectedStream();
 		} 
 		else 
 		{
 			JOptionPane.showMessageDialog(Gui_StreamRipStar.this
 					,trans.getString("select"));
 		}
 	}
 	
 	/**
 	 * Is called, when you like to hear music 
 	 * @author Johannes Putzke	
 	 */
 	public class PlayMusikListener implements ActionListener 
 	{
 		public void actionPerformed(ActionEvent e) 
 		{
 			playSelectedStream();
 		}
 	}
 	
 	
 	/**
 	 * Show an field where you can find the runtime options 
 	 * for the first selected stream.
 	 * 
 	 */
 	class ShowStatsListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			//test if a cell is selected
 			if(getTabel().isTHSelected()) {
 				//get der Stream Object witch is selected
 				Stream stream = table.getSelectedStream()[0];
 				//open infodialog
 				new Gui_Infodialog(Gui_StreamRipStar.this,stream);
 
 			} else {
 				//say, that no cell is selected
 				JOptionPane.showMessageDialog(Gui_StreamRipStar.this
 						,trans.getString("select"));
 			}
 		}
 	}
 	
 	/**
 	 * deletes an stream from file, table and streamvector
 	 * @param id
 	 */
 	public void deleteStreamPerID(int id) {
 		//remove from table (the visible content)
 		table.removeStreamfromTable();
 		//from vector
 		controlStreams.removeStreamFromVector(id);
 		//from harddisk
 		controlStreams.saveStreamVector();
 		//delete all schedul jobs for this stream
 		controlJob.deleteAllJobsFromStream(id);
 	}
 	
 	class DeleteListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			boolean stopDeleting = false;
 			Stream[] toDeleteStreams = getTabel().getSelectedStream();
 			
 			//first look, if there are really streams to delete
 			if(toDeleteStreams != null && toDeleteStreams.length > 0) {
 				
 				//first run is to look, if an stream is recording
 				for(int i= toDeleteStreams.length-1; i >= 0; i--) {
 					if (toDeleteStreams[i].getStatus()) {
 						stopDeleting = true;
 						break;
 					}
 				}
 				
 				//test if we can move forward
 				if(!stopDeleting) {
 					
 					//now ask, if the user really wants to delete the stream
 					int selectedOption = JOptionPane.showConfirmDialog(Gui_StreamRipStar.this,
 							trans.getString("realyDelete").replace("%s", ""+toDeleteStreams.length),
 							trans.getString("deleteStream"),
 							JOptionPane.YES_NO_OPTION);
 					
 					//if yes -> delete all streams
 					if (selectedOption == 0) {
 						for(int i= toDeleteStreams.length-1; i >= 0; i--) {
 							deleteStreamPerID(toDeleteStreams[i].id);
 						}
 					}
 				} else {
 					JOptionPane.showMessageDialog(Gui_StreamRipStar.this
 							,trans.getString("noDeleteRecord"));
 				}
 			}
 		}
 	}
 	
 	class Edit4AllStreamListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			new Gui_Stream4AllOptions(Gui_StreamRipStar.this);
 		}
 	}
 	
 	class EditDefaultStreamListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			new Gui_StreamOptions(controlStreams.getDefaultStream(), Gui_StreamRipStar.this,false, true, true);
 		}
 	}
 	
 	class EditStreamListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			editStream();
 		}
 	}
 	
 	class StartRecordListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {	
 			startRippingSelected();
 		}
 	}
 	
 	class StopRecordListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			stopRippingSelected();
 		}
 	}
 	
 	class ShowStreamRipStarListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			changeStatus();
 		}
 	}
 	
 	class AboutDialogListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			new About_StreamRipStar(controlStreams,Gui_StreamRipStar.this);
 		}
 	}
 	
 	class ViewLogDialogListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			new ViewLog(Gui_StreamRipStar.this);
 		}
 	}
 	
 	class GoToWebSiteListener implements ActionListener{
 		public void actionPerformed(ActionEvent e){
 			controlStreams.startWebBrowser("http://streamripper.sourceforge.net");
 		}
 	}
 	
 	class GoToHelpSiteListener implements ActionListener{
 		public void actionPerformed(ActionEvent e){
 			controlStreams.startWebBrowser("https://github.com/Eule/StreamRipStar/wiki");
 		}
 	}
 	
 	class StreamBrowserListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			if(streamBrowser == null) {
 				streamBrowser = new Gui_StreamBrowser2(Gui_StreamRipStar.this);
 			} else {
 				streamBrowser.setVisible(true);
 			}
 		}
 	}
 	
 	class SearchUpdateListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			new Gui_searchUpdate(controlStreams,Gui_StreamRipStar.this);
 		}
 	}
 	/**
 	 * Play the previous stream. If no stream is selected, select and play
 	 * the last stream.  
 	 */
 	public synchronized void playPreviousStream()
 	{
 		//select the previous stream
 		getTabel().selectPreviousStream();
 		
 		//if a stream is selected, play it
 		if(getTabel().isStreamSelected())
 		{
 			getTabel().startMusicPlayerWithSelectedStream();
 		}
 	}
 	
 	
 	/**
 	 * Play the previous stream. If no stream is selected, select and play
 	 * the last stream.  
 	 *
 	 */
 	class PlayPreviousStreamListener implements ActionListener
 	{
 		public void actionPerformed(ActionEvent e)
 		{
 			playNextStream();
 		}
 	}
 	
 	/**
 	 * Play the previous stream. If no stream is selected, select the first stream in the list.
 	 * If the last Stream is selected, play the first one.
 	 */
 	public synchronized void playNextStream()
 	{
 		//select the next stream
 		getTabel().selectNextStream();
 
 		//if a stream is selected, play it
 		if(getTabel().isStreamSelected()) 
 		{
 			getTabel().startMusicPlayerWithSelectedStream();
 		}
 	}
 	
 	/**
 	 * Play the previous stream. If no stream is selected, select the first stream in the list.
 	 * If the last Stream is selected, play the first one.
 	 */
 	class PlayNextStreamListener implements ActionListener 
 	{
 		public void actionPerformed(ActionEvent e) 
 		{
 			playNextStream();
 		}
 	}
 	
 	public void windowClosing (WindowEvent e) {
 		//Do nothing
 		if(winAction == 0) {
 			setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
 		}
 		//close
 		else if(winAction == 1) {
 			setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
 			closeAll();
 		}
 		//hide
 		else if(winAction == 2) {
 			setDefaultCloseOperation( JFrame.HIDE_ON_CLOSE );
 		}
 	}
 
 	public void windowClosed (WindowEvent e) { }
 	public void windowOpened (WindowEvent e) { }
 	public void windowIconified (WindowEvent e) { }
 	public void windowDeiconified (WindowEvent e) { }
 	public void windowActivated (WindowEvent e) { }
 	public void windowDeactivated (WindowEvent e) { }
 	
 }
 
