 package org.publicmain.gui;
 
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.File;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Vector;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.ImageIcon;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JRadioButtonMenuItem;
 import javax.swing.JTabbedPane;
 import javax.swing.JToggleButton;
 import javax.swing.SwingUtilities;
 import javax.swing.UIDefaults;
 import javax.swing.UIManager;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import org.images.Help;
 import org.publicmain.chatengine.ChatEngine;
 import org.publicmain.chatengine.KnotenKanal;
 import org.publicmain.common.Config;
 import org.publicmain.common.FileTransferData;
 import org.publicmain.common.LogEngine;
 import org.publicmain.common.MSG;
 import org.publicmain.common.Node;
 import org.publicmain.sql.LocalDBConnection;
 
 
 /**
  * @author ATRM
  * 
  */
 
 public class GUI extends JFrame implements Observer, ChangeListener {
 
	private final int GRP_NAME_LENGTH = Config.getConfig().getMaxGroupLength();
	private final int PRIV_NAME_LENGTH = Config.getConfig().getMaxAliasLength();
 
 	// Deklarationen:
 	private ChatEngine ce;
 	LogEngine log;
 
 	private static GUI me;
 	private List<ChatWindow> chatList;
 	private JMenuBar menuBar;
 	private JMenu file;
 	private JMenu options;
 	private JMenu help;
 	private JMenu backupServer;
 	private JMenuItem pullHistory;
 	private JMenuItem pushHistory;
 	private JMenuItem settings;
 	private JMenuItem history;
 	private JMenuItem about;
 	private JMenuItem helpContent;
 	private JMenuItem exit;
 	private ButtonGroup btnGrp;
 	private JTabbedPane jTabbedPane;
 	private JToggleButton contactListBtn;
 	private boolean contactListActive;
 	private ContactList contactListWin;
 	private pMTrayIcon trayIcon;
 	private LocalDBConnection locDBCon;
 
 	/**
 	 * Konstruktor fr das GUI mit Initialisierungen
 	 */
 	private GUI() {
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (Exception ex) {
 			LogEngine.log(ex);
 		}
 
 		// Initialisierungen:
 		try {
 			this.ce = new ChatEngine();
 		} catch (Exception e) {
 			LogEngine.log(e);
 		}
 		
 		GUI.me 					= this;
 		this.log 				= new LogEngine();
 		// this.locDBCon 		= LocalDBConnection.getDBConnection();
 		this.about 				= new JMenuItem("About");
 		// TODO: evtl. noch anderes Icon whlen
 		this.helpContent		= new JMenuItem("Help Contents", Help.getIcon("helpContentsIcon.png"));
 		this.exit				= new JMenuItem("Exit");
 		this.btnGrp 			= new ButtonGroup();
 		this.chatList 			= Collections.synchronizedList(new ArrayList<ChatWindow>());
 		this.jTabbedPane 		= new JTabbedPane();
 		this.contactListBtn 	= new JToggleButton(Help.getIcon("UserListAusklappen.png"));
 		this.contactListActive 	= false;
 		this.menuBar 			= new JMenuBar();
 		this.file	 			= new JMenu("File");
 		this.options 			= new JMenu("Options");
 		this.help	 			= new JMenu("Help");
 		this.backupServer		= new JMenu("Backup-Server");
 		this.pushHistory		= new JMenuItem("Push History");
 		this.pullHistory		= new JMenuItem("Pull History");
 		this.settings 			= new JMenuItem("Settings");
 		this.history			= new JMenuItem("History");
 		this.trayIcon 			= new pMTrayIcon();
 		
 		/**
 		 * Erstellen erforderlicher Controller und Listener
 		 */
 		this.addWindowListener(new winController());				// WindowListener fr das GUI-Fenster
 		this.jTabbedPane.addChangeListener(this);					// ChangeListener fr den Focus auf dem Eingabefeld
 		this.exit.addActionListener(new menuContoller());			
 		this.about.addActionListener(new menuContoller());
 		this.helpContent.addActionListener(new menuContoller());
 		this.history.addActionListener(new menuContoller());
 		this.settings.addActionListener(new menuContoller());
 		
 		this.contactListBtn.setMargin(new Insets(2, 3, 2, 3));
 		this.contactListBtn.setToolTipText("show contacts");
 		this.contactListBtn.addActionListener(new ActionListener() {
 
 			/**
 			 * TODO: Kommentar
 			 */
 			public void actionPerformed(ActionEvent e) {
 				JToggleButton source = (JToggleButton) e.getSource();
 				if (source.isSelected()) {
 					contactListAufklappen();
 				} else {
 					contactListZuklappen();
 				}
 			}
 		});
 		
 		/**
 		 * Men-Komponenten hinzufgen
 		 */
 		this.options.add(history);
 		this.options.add(backupServer);
 		this.file.add(exit);
 		this.help.add(helpContent);
 		this.help.add(about);
 		this.backupServer.add(pushHistory);
 		this.backupServer.add(pullHistory);
 		this.backupServer.add(settings);
 		this.menuBar.setLayout(new BoxLayout(menuBar, BoxLayout.LINE_AXIS));
 		this.menuBar.add(contactListBtn);
 		this.menuBar.add(file);
 		this.menuBar.add(options);
 		this.menuBar.add(help);
 
 		// Einkommentieren wenn Logo gewnscht:
 		// this.menuBar.add(Box.createHorizontalGlue());
 		// this.menuBar.add(new JLabel(new
 		// ImageIcon(Help.class.getResource("miniSpin.gif"))));
 
 		// GUI Komponenten hinzufgen:
 		this.setJMenuBar(menuBar);
 		this.add(jTabbedPane);
 
 		// StandardGruppe erstellen:
 		this.addGrpCW("public", true);
 		// StandardGruppe joinen:
 
 		// Registriert Hauptfenster als Empfnger fr noch nicht gefangene
 		// Privatnachrichten.
 		this.ce.register_defaultMSGListener(this);
 
 		// GUI JFrame Einstellungen
 		this.setIconImage(Help.getIcon("pM_Logo2.png").getImage());
 		this.setMinimumSize(new Dimension(250, 250));
 		this.pack();
 		this.setLocationRelativeTo(null);
 		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		this.setTitle("publicMAIN");
 		this.contactListWin = new ContactList(me);
 		this.setVisible(true);
 		this.chatList.get(0).focusEingabefeld();
 		this.contactListAufklappen();
 
 	}
 	
 	/**
 	 * Diese Methode klappt die Benutzerliste auf.
 	 */
 	private void contactListAufklappen(){
 
 		if(!contactListActive){
 			
 			this.contactListBtn.setToolTipText("hide contacts");
 			this.contactListBtn.setIcon(Help.getIcon("UserListEinklappen.png"));
 			this.contactListBtn.setSelected(true);
 			this.contactListWin.repaint();
 			this.contactListWin.setVisible(true);
 			contactListActive = true;
 		}
 
 	}
 	
 	/**
 	 * Diese Methode klappt die Benutzerliste zu.
 	 */
 	private void contactListZuklappen() {
 
 		if (contactListActive) {
 			this.contactListBtn.setToolTipText("show contacts");
 			this.contactListBtn.setIcon(Help.getIcon("UserListAusklappen.png"));
 			this.contactListBtn.setSelected(false);
 			this.contactListWin.setVisible(false);
 			this.contactListActive = false;
 		}
 	}
 
 	/** 
 	 * Diese Methode erstellt ein neues ChatFenster. Ist das der Parameter ein {@link String} erstellt
 	 * sie ein Gruppenfenster ist er {@link Long} ein PrivatChatFenster.
 	 * 
 	 * @param referenz, fr was soll ein Fenster erstellt werden. 
 	 * @return
 	 */
 	public ChatWindow createChat(Object referenz) {
 
 		ChatWindow cw;
 		if (referenz instanceof String) {
 			cw = new ChatWindow((String) referenz);
 		} else if (referenz instanceof Long) {
 			cw = new ChatWindow((Long) referenz);
 		} else {
 			return null;
 		}
 
 		// Titel festlegen
 		String title = cw.getChatWindowName();
 
 		// Neues ChatWindow (cw) zur Chatliste (ArrayList<ChatWindow>)
 		// hinzufgen
 		this.chatList.add(cw);
 		// erzeugen von neuem Tab fr neues ChatWindow
 		this.jTabbedPane.addTab(title, cw);
 		// Index vom ChatWindow im JTabbedPane holen um am richtigen Ort
 		// einzufgen
 		int index = jTabbedPane.indexOfComponent(cw);
 		// den neuen Tab an die Stelle von index setzen
 		this.jTabbedPane.setTabComponentAt(index, cw.getWindowTab());
 		if (cw.isGroup()) {
 			ce.add_MSGListener(cw, (String) referenz);
 		} else {
 			ce.add_MSGListener(cw, (Long) referenz);
 		}
 
 		return cw;
 	}
 
 	/**
 	 * Diese Methode erstellt ein ChatWindow fr Gruppen, falls ChatWindow
 	 * bereits vorhanden, wird dieses fokusiert.
 	 * 
 	 * @param grpName
 	 * @param focus
 	 */
 	public void addGrpCW(String grpName, boolean focus) {
 		// Gruppenname auf Konvention prfen, ggf. nderungen vornehmen.
		String clean_group = sanatizeGroupname(grpName);
 		// Hol ref. auf Gruppenfenster wenn existent
 		ChatWindow tmp_cw = getCW(clean_group);
 		// wenn ref. leer dann erstelle neues Gruppenfesnter
 		if (tmp_cw == null) {
 			tmp_cw = createChat(clean_group);
 		}
 		// fokusiere das Gruppenfenster
 		if (focus) {
 			focus(tmp_cw);
 		}
 	}
 
 	/**
 	 * Bereinigt einen eingegebenen Gruppenstring von ungewollten Zeichen
 	 * 
 	 * @param grpName, der vom Benutzer eingegebene Gruppenname
 	 * @return, einen bereinigten String zur Verwendung als Gruppenname
 	 */
	private String sanatizeGroupname(String grpName) {
 		String clean_grpname = grpName;
		if (clean_grpname.length() > GRP_NAME_LENGTH) {
			clean_grpname = clean_grpname.substring(0, GRP_NAME_LENGTH);
		}
 		clean_grpname = clean_grpname.trim();
		clean_grpname = clean_grpname.replaceAll("[^a-zA-Z0-9\\-_]", "");
 		// grp_name = grp_name.replaceAll("[&#*?\\/@<>\\t\\n\\x0B\\f\\r]*", "");
 		clean_grpname = clean_grpname.toLowerCase();
 		return clean_grpname;
 	}
 	
 	/**
 	 * TODO: Kommentar
 	 * 
 	 * @param uid
 	 * @param focus
 	 */
 	public void addPrivCW(long uid,boolean focus){
 		ChatWindow tmp = getCW(uid);
 		if(tmp == null)tmp=createChat(uid);
 		if (focus) focus(tmp);
 	}
 
 	/**
 	 * Fokussiert das angegebene ChatWindow
 	 * 
 	 * @param cw, das zu fokussierende Chatwindow
 	 */
 	private void focus(ChatWindow cw) {
 		int index = jTabbedPane.indexOfComponent(cw);
 		if (index >= 0)
 			jTabbedPane.setSelectedIndex(index);
 	}
 	
 	/**
 	 * Diese Methode entfernt ein ChatWindow
 	 * 
 	 * Diese Methode sorgt dafr das ChatWindows aus der ArrayList "chatList"
 	 * entfernt werden und im GUI nicht mehr angezeigt werden.
 	 * 
 	 * @param ChatWindow
 	 */
 	public void delChat(ChatWindow cw) {
 		// TODO: Hier evtl. noch anderen Programmablauf implementier
 		// z.B. schlieen des Programms wenn letztes ChatWindow geschlossen
 		// wird
 		
 		// Falls nur noch ein ChatWindow brig kann dieses nicht entfernt werden
 		if(chatList.size() >= 2){
 			// ChatWindow (cw) aus jTabbedPane entfernen:
 			this.jTabbedPane.remove(cw);
 			// ChatWindow aus Chatliste entfernen:
 			this.chatList.remove(cw);
 			// ChatWindow aus Gruppe entfernen (MSGListener abschalten):
 			ce.remove_MSGListener(cw);
 		}
 	}
 	
 	/**
 	 * Diese Methode entfernt ein ChatWindow anhand des Namens, sie sorgt dafr
 	 * das ChatWindows aus der ArrayList "chatList" entfernt werden und im GUI
 	 * nicht mehr angezeigt werden.
 	 * 
 	 * @param chatname
 	 */
 	public void delChat(Object refObject) {
 		delChat(getCW(refObject));
 	}
 	
 	/**
 	 * Diese Methode nimmt die nderungsanforderung entgegen und prft ob der
 	 * Name bereits an einen anderen Benutzer oder an eine Gruppe vergeben
 	 * wurde. Existiert der Alias wird false zurckgeliefert in jedem anderen
 	 * Fall wird der Name ber die ChatEngine gendert.
 	 * 
 	 * @param alias
 	 * @return boolean
 	 */
 	public boolean changeAlias(String alias){
 		if(ce.getNodeforAlias(alias)!=null){
 			info("Username already in use", null, 1);
 			return false;
 		} else {
 			ce.setAlias(alias);
 			return true;
 		}
 	}
 	
 	/**
 	 * TODO: Kommentar
 	 * 
 	 * @return
 	 */
 	public ChatWindow getActiveCW() {
 		return (ChatWindow) jTabbedPane.getSelectedComponent();
 	}	
 	
 	
 	/**
 	 * Fhrt das Programm ordnungsgem runter
 	 */
 	void shutdown() {
 		new Thread(new Runnable() {
 			public void run() {
 				try {
 					Thread.sleep(2000);
 				} catch (InterruptedException e) {
 				}
 				System.exit(0);
 			}
 		}).start();
 		LogEngine.log(this, "Shutdown initiated!", LogEngine.INFO);
 		ce.shutdown();
 		if (locDBCon != null) {
 			locDBCon.shutdownLocDB();
 		}
 	}
 
 	/**
 	 * Diese Methode prft ob ein ChatWindow bereits vorhanden ist, sie prft ob
 	 * ein ChatWindow vorhanden ist falls ja wird dieses zurckgegeben. In jedem
 	 * anderen Fall wird null zurckgegeben.
 	 * 
 	 * TODO: BEREINIGEN!
 	 * 
 	 * @param empfGrp, Name des Chatwindow
 	 * @return, ChatWindow or null
 	 */
 	private ChatWindow existCW(ChatWindow referenz) {
 		int index = chatList.indexOf(referenz);
 		return (index >= 0) ? chatList.get(index) : null;
 	}
 	
 	/**
 	 * Die Methode liefert das zu einer Referenz gehrende {@link ChatWindow}.
 	 * 
 	 * <br>Sie ersetzt die alte <code>existCW</code> und liefert ein ChatWindow zu
 	 * einer Referenz. Die Referenz muss zwanslufig entweder ein {@link String}
 	 * oder ein {@link Long}. Ein <code>String</code> bezieht sich auf
 	 * GruppenChatFenster und liefert falls vorhanden das ChatFenster zu dieser
 	 * Gruppe. Ein <code>Long</code> liefert entsprechend das PrivatChatFenster
 	 * wenn der bergebene wert eine gltige UID war und einFenster zu diesem
 	 * Nutzer bereits existiert. Sollte eine ungltige Referenz bergeben werden
 	 * oder kein Fenster zu dieser Referenz existieren liefert die Methode
 	 * <code>null</code> zurck.
 	 * 
 	 * @param referenz, der Gruppenname (String) oder die UserID (Long) zu der ein
 	 *            		Fenster gesucht werden soll.
 	 * @return, 		die laufende Instanz des Chatwindows zur angegebenen Referenz
 	 *         			oder <code>null</code> falls keine Instanz gefunden.
 	 */
 	private ChatWindow getCW(Object referenz) {
 		if (referenz != null) {
 			for (ChatWindow cur : chatList) {
 				if (cur.equals(referenz)) {
 					return cur;
 				}
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Diese Methode wird in einem privaten ChatWindow zum versenden der
 	 * Nachricht verwendet
 	 * 
 	 * @param empfUID, long EmpfngerUID
 	 * @param msg, String die Nachricht
 	 */
 	void privSend(long empfUID, String msg) {
 		ce.send_private(empfUID, msg);
 	}
 
 	/**
 	 * Diese Methode wird fr das Senden von Gruppennachrichten verwendet Falls
 	 * noch kein ChatWindow fr diese Gruppe besteht wird eines erzeugt.
 	 * 
 	 * @param empfGrp, String Empfngergruppe
 	 * @param msg, String die Nachricht/Msg
 	 * @param cw, ChatWindow das aufrufende ChatWindow
 	 */
 	void groupSend(String empfGrp, String msg) {
 
 		ChatWindow tmpCW = getCW(empfGrp);
 		if (tmpCW == null) {
 			tmpCW = new ChatWindow(empfGrp);
 			addGrpCW(empfGrp, true);
 			focus(tmpCW);
 		} else {
 			focus(tmpCW);
 		}
 		ce.send_group(empfGrp, msg);
 	}
 	
 	/**
 	 * Diese Methode ist fr das Ignorien eines users
 	 * 
 	 * @param alias, String Alias des Users
 	 * @returns, true Wenn User gefunden
 	 */
 	boolean ignoreUser(long uid) {
 		return ce.ignore_user(uid);
 	}
 
 	/**
 	 * Diese Methode ist fr das nicht weitere Ignorieren eines Benutzers.
 	 * 
 	 * @param alias, String Alias des Users
 	 * @return, true Wenn User gefunden
 	 */
 	boolean unignoreUser(long uid) {
 		return ce.unignore_user(uid);
 	}	
 
 	/**
 	 * TODO: Kommentar
 	 * 
 	 * @param uid
 	 */
 	public void sendFile(long uid) {
 		JFileChooser fileChooser = new JFileChooser();
 		int returnVal=fileChooser.showOpenDialog(me);
 		File selectedFile = fileChooser.getSelectedFile();
 		if((returnVal== JFileChooser.APPROVE_OPTION)&&selectedFile!=null)sendFile(selectedFile, uid);
 	}
 
 	/**
 	 * TODO: Kommentar
 	 * 
 	 * @param datei
 	 * @param uid
 	 */
 	public void sendFile(File datei, long uid) {
 		if (datei.isFile()) {
 			if (datei.canRead()) {
 				if (datei.length() > 0) {
 					ce.send_file(datei, uid);
 				} else {
 					info("File has a size of 0 bytes!", uid, 3);
 				}
 			} else {
 				info("Cant read file \"" + datei.getName() + "\"!", uid, 3);
 			}
 		} else {
 			info("Only single Files are supported!", uid, 3);
 		}
 	}	
 	
 	/**
 	 * Displays a text message in the referenced ChatWindow (Group/UID) or the
 	 * active Window if reference is <code>null</code>
 	 * 
 	 * @param nachricht, Text of the message
 	 * @param reference, Groupname or UID of ChatWindow to put the message in or
 	 *            		 <code>null</code> to take the active one.
 	 * @param typ, <ul><li>0 - info<li>1 - warning<li>2 - error</ul>
 	 */
 	public void info(String nachricht, Object reference, int typ) {
 		ChatWindow tmp = getCW(reference);
 		;
 		if (tmp == null) {
 			tmp = getActiveCW();
 		}
 		if (tmp != null) {
 			if (typ == 0) {
 				tmp.info(nachricht);
 			} else if (typ == 1) {
 				tmp.warn(nachricht);
 			} else {
 				tmp.error(nachricht);
 			}
 		}
 	}
 	
 	/**
 	 * Diese Methode liefert ein Fileobjekt, sie benachrichtigt ber die GUI den
 	 * Nutzer und fordert einen Ablageort an.
 	 * 
 	 * @param filename
 	 * @return File
 	 */
 	public File request_File(FileTransferData fr) {
 		final long timeout = Config.getConfig().getFileTransferTimeout() - 1000;
 		String dateiname = fr.datei.getName();
 		JOptionPane yesno_pane= new  JOptionPane("Mchten sie die Datei \""+dateiname+ "\" von "+ fr.sender.getAlias() +" annehmen? ("+fr.getNiceSize() +")",JOptionPane.QUESTION_MESSAGE,JOptionPane.YES_NO_OPTION);
 		final JDialog yesno_dialog=yesno_pane.createDialog(me, "Dateitransfer");
 		final JFileChooser fileChooser = new JFileChooser();
 		
 		new Thread(new Runnable() {
 			public void run() {
 				try {
 					Thread.sleep(timeout);
 				} catch (InterruptedException e) {
 				}
 				yesno_dialog.dispose();
 				fileChooser.cancelSelection();
 			}
 		}).start();
 		
 		yesno_dialog.show();
 		Object  x = yesno_pane.getValue();
 		
 		
 		if(x!=null&&x instanceof Integer &&((Integer)x)==0) {
 			if (dateiname != null)
 				fileChooser.setSelectedFile(new File(dateiname));
 			int returnVal = fileChooser.showSaveDialog(me);
 			if (returnVal == JFileChooser.APPROVE_OPTION) {
 //				System.out.println("You chose to save this file: " + fileChooser.getSelectedFile().getName());
 				return fileChooser.getSelectedFile();
 			}
 			return null;
 		} else {
 			return null;
 		}
 
 	}
 
 	/**
 	 * Diese Methode  informiert die GUI ber nderungen.
 	 */
 	public void notifyGUI() {
 		for (ChatWindow cw : chatList) {
 			cw.updateName();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
 	 */
 	@Override
 	public void update(Observable o, Object arg) {
 		//FIXME : vielleicht nochmal berarbeiten... wenn Zeit ist
 		if(o instanceof KnotenKanal){
 			MSG tmp = (MSG) arg;
 			Node tmp_node = ce.getNodeForNID(tmp.getSender());
 			me.addPrivCW(tmp_node.getUserID(), false);
 			ce.put(tmp);
 		}
 	}
 
 	/**
 	 * Diese Methode stellt das Node bereit
 	 * 
 	 * Diese Methode holt das {@link NODE}-Objekt fr eine UserID
 	 * 
 	 * @param uid
 	 * @return Node
 	 */
 	public Node getNodeForUID(long uid) {
 		return ce.getNodeForUID(uid);
 	}
 	
 	/**
 	 * Diese Methode stellt das GUI bereit
 	 * 
 	 * Diese Methode stellt das GUI fr andere Klassen bereit um einen Zugriff
 	 * auf GUI Attribute zu ermglichen
 	 * 
 	 * @return GUI
 	 */
 	public static GUI getGUI() {
 		if (me == null) {
 			me = new GUI();
 		}
 		return me;
 	}
 	
 	/**
 	 * @return
 	 */
 	JTabbedPane getTabbedPane(){
 		return this.jTabbedPane;
 	}
 
 	/* (non-Javadoc)
 	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
 	 */
 	public void stateChanged(ChangeEvent e) {
 		((ChatWindow)jTabbedPane.getSelectedComponent()).focusEingabefeld();
 	}
 	
 	
 	/**
 	 * ActionListener fr Menu's
 	 * 
 	 * @author ABerthold
 	 *
 	 */
 	class menuContoller implements ActionListener{
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			
 			JMenuItem source = (JMenuItem)e.getSource();
 			
 			switch(source.getText()){
 			
 			case "Exit":
 				shutdown();
 				break;
 			case "About pMAIN":
 				new AboutPublicMAIN(me, "About publicMAIN", true);
 				break;
 			case "Help Contents":
 				//TODO: HelpContents HTML schreiben
 				new HelpContents();
 				break;
 			case "checkout History":
 				new checkoutHistoryWindow();
 				break;
 			case "Backup-Server Settings":
 				//TODO: hier noch eine vernnfige variante der Implementierung
 				registrationWindow.getRegistrationWindow();
 				break;
 			}
 			
 		}
 		
 	}
 	
 	/**
 	 * WindowListener fr GUI
 	 * 
 	 * 
 	 * 
 	 * @author ABerthold
 	 *
 	 */
 	class winController implements WindowListener{
 		public void windowOpened(WindowEvent arg0) {
 		}
 		@Override
 		// Wird das GUI minimiert wird die Userlist zugeklappt und der
 		// userListBtn zurckgesetzt:
 		public void windowIconified(WindowEvent arg0) {
 			if (contactListBtn.isSelected()) {
 				contactListZuklappen();
 			}
 		}
 		@Override
 		public void windowDeiconified(WindowEvent arg0) {
 		}
 		@Override
 		public void windowDeactivated(WindowEvent arg0) {
 		}
 		@Override
 		public void windowClosing(WindowEvent arg0) {
 		}
 		@Override
 		public void windowClosed(WindowEvent arg0) {
 			contactListZuklappen();
 			shutdown();
 			// Object[] eventCache =
 			// {"super"," liegt im Systemtray"};
 			// Object anchor = true;
 			// JOptionPane.showInputDialog(me,
 			// "pMAIN wird ins Systemtray gelegt!",
 			// "pMAIN -> Systemtray", JOptionPane.PLAIN_MESSAGE, new
 			// ImageIcon("media/pM16x16.png"), eventCache, anchor);
 		}
 		@Override
 		public void windowActivated(WindowEvent arg0) {
 			if (contactListBtn.isSelected()) {
 				contactListWin.toFront();
 			}
 		}
 	}
 	
 	/**
 	 * Diese Methode gibt die Default Settings des aktuellen L&F in der Console aus
 	 */
 /*	private void getLookAndFeelDefaultsToConsole(){
 		UIDefaults def = UIManager.getLookAndFeelDefaults();
 		Vector<?> vec = new Vector<Object>(def.keySet());
 		Collections.sort(vec, new Comparator<Object>() {
 			public int compare(Object arg0, Object arg1) {
 				return arg0.toString().compareTo(arg1.toString());
 			}
 		});
 		for (Object obj : vec) {
 //			System.out.println(obj + "\n\t" + def.get(obj));
 		}
 	}
 */
 
 
 }
