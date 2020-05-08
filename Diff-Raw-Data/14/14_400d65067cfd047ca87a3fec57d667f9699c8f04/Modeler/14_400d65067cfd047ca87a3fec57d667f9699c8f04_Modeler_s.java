 /*
  *   Copyright (C) 2006  The Concord Consortium, Inc.,
  *   25 Love Lane, Concord, MA 01742
  *
  *   This program is free software; you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation; either version 2 of the License, or
  *   (at your option) any later version.
  *
  *   This program is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with this program; if not, write to the Free Software
  *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * END LICENSE */
 
 package org.concord.modeler;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.FlowLayout;
 import java.awt.Insets;
 import java.awt.KeyboardFocusManager;
 import java.awt.Toolkit;
 import java.awt.datatransfer.StringSelection;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.Authenticator;
 import java.net.CookieHandler;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 import java.util.prefs.Preferences;
 
 import javax.net.ServerSocketFactory;
 import javax.net.SocketFactory;
 import javax.swing.AbstractButton;
 import javax.swing.Action;
 import javax.swing.BorderFactory;
 import javax.swing.ButtonGroup;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JRadioButtonMenuItem;
 import javax.swing.JToolBar;
 import javax.swing.KeyStroke;
 import javax.swing.SwingConstants;
 import javax.swing.UIManager;
 import javax.swing.event.PopupMenuEvent;
 import javax.swing.event.PopupMenuListener;
 import javax.swing.text.DefaultEditorKit;
 
 import com.apple.eawt.Application;
 import com.apple.eawt.ApplicationAdapter;
 import com.apple.eawt.ApplicationEvent;
 
 import org.concord.modeler.draw.FillMode;
 import org.concord.modeler.event.PageEvent;
 import org.concord.modeler.event.PageListener;
 import org.concord.modeler.text.ExternalClient;
 import org.concord.modeler.text.HyperlinkParameter;
 import org.concord.modeler.text.Page;
 import org.concord.modeler.text.SaveReminder;
 import org.concord.modeler.ui.ColorMenu;
 import org.concord.modeler.util.DataQueue;
 import org.concord.modeler.util.FileUtilities;
 import org.concord.modeler.util.SwingWorker;
 
 /**
  * This is the "browser" of <code>Page</code>.
  * 
  * @version 2.0 (JSE 5.0+)
  * @see org.concord.modeler.text.Page
  * @author Charles Xie
  */
 
 public class Modeler extends JFrame implements BookmarkListener, EditorListener, DownloadListener, PageListener {
 
 	final static boolean IS_MAC = System.getProperty("os.name").startsWith("Mac");
 	final static String MINIMUM_JAVA_VERSION = "1.5";
 
 	final static String NAME = "Molecular Workbench";
 	final static String VERSION = "2.0";
 	private final static String REMOTE_STATIC_ROOT = "http://mw2.concord.org/public/";
 	private final static String LOCAL_STATIC_ROOT = "http://localhost/public/";
 	private final static String REMOTE_CONTEXT_ROOT = "http://mw2.concord.org/";
 	private final static String LOCAL_CONTEXT_ROOT = "http://localhost/";
 
 	private final static short offset = 20;
 
 	public static Person user = new Person();
 
 	static Preferences preference;
 	static List<Modeler> windowList = new ArrayList<Modeler>();
 	static boolean showToolBarText = true;
 	static boolean launchedByJWS, directMW;
 	static boolean restart;
 	static boolean hostIsLocal, runOnCD;
 	static String startingURL;
 	static String userdir;
 	static Icon toolBarSeparatorIcon = new ImageIcon(Modeler.class.getResource("images/ToolBarSeparator.gif"));
 	private static Icon toolBarHeaderIcon = new ImageIcon(Modeler.class.getResource("images/ToolBarHeaderBar.gif"));
 	static ImageIcon frameIcon = new ImageIcon(Modeler.class.getResource("images/FrameIcon.gif"));
 
 	static byte windowCount;
 	static int tapeLength = DataQueue.DEFAULT_SIZE;
 	static boolean isUSLocale;
 	static String[] precacheFiles = new String[] { "zip/student.zip", "All Models in The Library", "zip/molecules.zip",
 			"All Molecules in The Library", "zip/part1.zip", "Activities: Part One", "zip/part2.zip",
 			"Activities: Part Two", "zip/tutorial.zip", "The User's Manual", "zip/teacher.zip", "Author Materials" };
 
 	boolean stopListening;
 	InputStream socketInputStream;
 	Socket clientSocket;
 
 	Editor editor;
 	Navigator navigator;
 	ServerGate serverGate;
 
 	private static ResourceBundle bundle;
 	private static short xOffset, yOffset;
 	private static final int PORT_LAUNCH = 9875;
 	private static boolean login;
 	private static String lookandfeel;
 	private static ServerSocket serverSocket;
 	private static Map<String, String> lnfMap;
 	private static UIManager.LookAndFeelInfo[] lf;
 	private StatusBar statusBar;
 	private JMenu preinstallMenu;
 	private JMenu bookmarkMenu;
 	private ColorMenu colorMenu;
 	private JMenuBar menuBar;
 	private JToolBar toolBar;
 	private PreferencesDialog preferencesDialog;
 	private JMenuItem reloadMenuItem, backMenuItem, forwardMenuItem, homeMenuItem, insertSymbolMenuItem;
 	private JButton reloadButton, backButton, forwardButton, homeButton;
 	private boolean listeningToSocket;
 	private static boolean disableJarUpdate, disableSetLAF;
 	private static String userLocale;
 	private static boolean initPageFlag;
 	private static boolean createUsingEDT;
 
 	private Action createReportAction, createReportForPageGroupAction;
 
 	public Modeler() {
 
 		Initializer.sharedInstance().setMessage("Initializing Modeler...");
 		setIconImage(frameIcon.getImage());
 
 		createReportAction = new CreateReportAction(this);
 		createReportForPageGroupAction = new CreateReportForPageGroupAction(this);
 
 		if (windowCount == 0) {
 
 			Page.setSoftwareName(NAME);
 			Page.setSoftwareVersion(VERSION);
 			PluginInfo pi = new PluginInfo("Jmol");
 			pi.addJar(REMOTE_STATIC_ROOT + "plugin/JmolApplet.jar");
 			pi.addJar(REMOTE_STATIC_ROOT + "plugin/netscape.jar");
 			pi.setMainClass("org.jmol.applet.MwPlugin");
 			PluginManager.addPlugInfo(pi);
 
 			// check look&feel installations
 			lf = UIManager.getInstalledLookAndFeels();
 			String lfName;
 			lnfMap = new HashMap<String, String>();
 			for (UIManager.LookAndFeelInfo lafInfo : lf) {
 				lfName = lafInfo.getName();
 				lnfMap.put(lfName, lafInfo.getClassName());
 			}
 
 			Initializer.sharedInstance().setMessage("Retrieving preferences...");
 			preference = Initializer.sharedInstance().getPreferences();
 
 			if (bundle == null) {
 				isUSLocale = Locale.getDefault().equals(Locale.US);
 				try {
 					bundle = ResourceBundle.getBundle("org.concord.modeler.properties.Modeler", Locale.getDefault());
 				}
 				catch (MissingResourceException e) {
 				}
 			}
 
 			int timeout = preference.getInt("Connect Timeout", 5);
 			if (timeout != 5)
 				ConnectionManager.setConnectTimeout(1000 * timeout);
 			timeout = preference.getInt("Read Timeout", 30);
 			if (timeout != 30)
 				ConnectionManager.setReadTimeout(1000 * timeout);
 
 			int historyDays = preference.getInt("History", 7);
 			if (historyDays != 7)
 				HistoryManager.sharedInstance().setDays(historyDays);
 
 			tapeLength = preference.getInt("Tape Length", 200);
 			float scale = preference.getFloat("Scale Characters", -1.0f);
 			if (scale > 0.0f)
 				Page.getPrintParameters().setCharacterScale(scale);
 			scale = preference.getFloat("Scale Images", -1.0f);
 			if (scale > 0.0f)
 				Page.getPrintParameters().setImageScale(scale);
 			scale = preference.getFloat("Scale Components", -1.0f);
 			if (scale > 0.0f)
 				Page.getPrintParameters().setComponentScale(scale);
 			scale = preference.getFloat("Scale Paragraph Indents", -1.0f);
 			if (scale > 0.0f)
 				Page.getPrintParameters().setIndentScale(scale);
 
 			if (!disableSetLAF) {
 				boolean foundLNF = false;
 				lookandfeel = preference.get("Look&Feel", null);
 				if (lookandfeel != null) {
 					String className = lnfMap.get(lookandfeel);
 					if (className != null)
 						foundLNF = setLookAndFeel(className);
 				}
 				if (!foundLNF) {
 					boolean isUnix = !System.getProperty("os.name").startsWith("Windows") && !IS_MAC;
 					if (!isUnix && setLookAndFeel(UIManager.getSystemLookAndFeelClassName())) {
 						lookandfeel = UIManager.getLookAndFeel().getName();
 					}
 					else { // if Unix/Linux, or in case Native L&F does not load properly
 						setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
 					}
 				}
 			}
 
 			if (lookandfeel != null) {
 				boolean isXP = System.getProperty("os.name").startsWith("Windows XP");
 				boolean isWindowsLook = lookandfeel.equalsIgnoreCase("Windows");
 				boolean isMacOSXLook = lookandfeel.startsWith("Mac OS X");
 				Page.setNativeLookAndFeelUsed((isXP && isWindowsLook) || (IS_MAC && isMacOSXLook));
 			}
 
 			if (ModelerUtilities.imageReader != null)
 				ModelerUtilities.imageReader.setParent(this);
 
 		}
 
 		setTitle(NAME + " V" + VERSION);
 
 		BookmarkManager.sharedInstance().addBookmarkListener(this);
 		createBookmarkMenu();
 
 		statusBar = new StatusBar();
 		ConnectionManager.sharedInstance().addProgressListener(statusBar);
 
 		Initializer.sharedInstance().setMessage("Creating editor...");
 		editor = new Editor(statusBar);
 		editor.addEditorListener(this);
 		Page page = editor.getPage();
 		navigator = new Navigator(page);
 		serverGate = new ServerGate(page);
 		editor.setServerGate(serverGate);
 		String s = Initializer.sharedInstance().getSystemProperty(PreferencesDialog.HOME_PAGE);
 		if (s != null)
 			navigator.setHomePage(s);
 		page.setNavigator(navigator);
 		page.addPageListener(this);
 		if (runOnCD)
 			page.getSaveReminder().setEnabled(false);
 		page.putActivityAction(createReportAction);
 		page.putActivityAction(createReportForPageGroupAction);
 
 		editor.createToolBars();
 
 		Initializer.sharedInstance().setMessage("Creating tool bar...");
 		createToolBar();
 
 		Initializer.sharedInstance().setMessage("Creating menu bar...");
 		createMenuBar();
 		setJMenuBar(menuBar);
 
 		getContentPane().add(editor, BorderLayout.CENTER);
 		getContentPane().add(toolBar, BorderLayout.NORTH);
 		getContentPane().add(statusBar, BorderLayout.SOUTH);
 
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 
 		int x = preference.getInt("Upper Left X", -1);
 		int y = preference.getInt("Upper Left Y", -1);
 		int w = preference.getInt("Width", -1);
 		int h = preference.getInt("Height", -1);
 
 		if (w < screenSize.width) {
 			if (x >= 0 && y >= 0 && x < screenSize.width && y < screenSize.height) {
 				setLocation(x, y);
 			}
 			else {
 				setLocation(75, 50);
 			}
 		}
 		else {
 			setLocation(0, 0);
 		}
 		if (w > 300 && h > 150 && w <= screenSize.width && h <= screenSize.height) {
 			editor.setPreferredSize(new Dimension(w, h));
 		}
 		else {
 			editor.setPreferredSize(new Dimension(screenSize.width - 150, screenSize.height - 250));
 		}
 
 		addWindowListener(new WindowAdapter() {
 			public void windowOpened(WindowEvent e) {
 				if (!initPageFlag) {
 					// defer until all other currently pending events to finish up
 					EventQueue.invokeLater(new Runnable() {
 						public void run() {
 							initPage();
 						}
 					});
 					initPageFlag = true;
 				}
 			}
 
 			public void windowClosing(WindowEvent e) {
 				savePageAndClose();
 			}
 		});
 
 		if (windowCount == 0) {
 			Initializer.sharedInstance().setMessage("Updating UI...");
 			EventQueue.invokeLater(new Runnable() {
 				public void run() {
 					ModelerUtilities.updateUI();
 				}
 			});
 		}
 
 		windowCount++;
 
 		if (IS_MAC) {
 			Application anApp = new Application();
 			anApp.setEnabledPreferencesMenu(true);
 			anApp.addApplicationListener(new ApplicationAdapter() {
 				public void handleQuit(ApplicationEvent e) {
 					savePageAndClose();
 					// e.setHandled(true); //DO NOT CALL THIS!!!
 				}
 
 				public void handlePreferences(ApplicationEvent e) {
 					e.setHandled(true);
 					// I am not sure this will be called by EventQueue, just make sure......
 					EventQueue.invokeLater(new Runnable() {
 						public void run() {
 							if (preferencesDialog == null)
 								preferencesDialog = new PreferencesDialog(Modeler.this);
 							preferencesDialog.setPreferences(preference);
 							preferencesDialog.setVisible(true);
 						}
 					});
 				}
 
 				public void handleAbout(ApplicationEvent e) {
 					e.setHandled(true);
 					navigator.visitLocation(navigator.getHomeDirectory() + "about.cml");
 				}
 			});
 		}
 
 	}
 
 	public static boolean isDirectMW() {
 		return directMW;
 	}
 
 	public static boolean isMac() {
 		return IS_MAC;
 	}
 
 	public static boolean isLaunchedByJws() {
 		return launchedByJWS;
 	}
 
 	public static String getContextRoot() {
 		return hostIsLocal ? LOCAL_CONTEXT_ROOT : REMOTE_CONTEXT_ROOT;
 	}
 
 	public static String getStaticRoot() {
 		return hostIsLocal ? LOCAL_STATIC_ROOT : REMOTE_STATIC_ROOT;
 	}
 
 	public static String getMyModelSpaceAddress() {
 		return (hostIsLocal ? LOCAL_CONTEXT_ROOT : REMOTE_CONTEXT_ROOT) + "mymodelspace.jsp?client=mw&author="
 				+ user.getUserID();
 	}
 
 	public static String getMyReportAddress() {
 		return (hostIsLocal ? LOCAL_CONTEXT_ROOT : REMOTE_CONTEXT_ROOT) + "myreportspace.jsp?client=mw&author="
 				+ user.getUserID();
 	}
 
 	private boolean setLookAndFeel(String className) {
 		if ("true".equals(System.getProperty("mw.nolookandfeel")))
 			return false;
 		boolean b = false;
 		try {
 			UIManager.setLookAndFeel(className);
 			b = true;
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 		return b;
 	}
 
 	/**
 	 * When the current instance is closed, destroy this component to prevent memory leak.
 	 */
 	public void destroy() {
 
 		if (editor != null) {
 			editor.getPage().removePageListener(this);
 			editor.removeEditorListener(this);
 			editor.destroy();
 		}
 
 		windowList.remove(this);
 		requestStopListening();
 		if (!windowList.isEmpty()) {
 			Modeler m = windowList.get(0);
 			if (!m.listeningToSocket) {
 				m.listenToSocket();
 				m.pingSocket("ping");
 			}
 		}
 
 		BookmarkManager.sharedInstance().removeBookmarkListener(this);
 		Zipper.sharedInstance().setProcessMonitor(null);
 		Zipper.sharedInstance().removeComponentToLock(preinstallMenu);
 		MultipageZipper.sharedInstance().removeProgressListener(editor.getPage());
 		ConnectionManager.sharedInstance().removeProgressListener(statusBar);
 		if (ModelerUtilities.imageReader != null)
 			ModelerUtilities.imageReader.setParent(null);
 
 		for (int i = 0, n = menuBar.getMenuCount(); i < n; i++)
 			destroyMenu(menuBar.getMenu(i));
 		menuBar.removeAll();
 		getContentPane().removeAll();
 
 		WindowListener[] wl = getWindowListeners();
 		for (WindowListener listener : wl)
 			removeWindowListener(listener);
 		ComponentListener[] cl = getComponentListeners();
 		for (ComponentListener listener : cl)
 			removeComponentListener(listener);
 
 		reloadMenuItem.setAction(null);
 		reloadButton.setAction(null);
 		backMenuItem.setAction(null);
 		backButton.setAction(null);
 		forwardMenuItem.setAction(null);
 		forwardButton.setAction(null);
 		homeMenuItem.setAction(null);
 		homeButton.setAction(null);
 		colorMenu.destroy();
 		preinstallMenu = null;
 		colorMenu = null;
 		bookmarkMenu = null;
 		createReportAction = null;
 		createReportForPageGroupAction = null;
 		statusBar = null;
 		editor = null;
 		navigator = null;
 		reloadMenuItem = null;
 		reloadButton = null;
 		backMenuItem = null;
 		backButton = null;
 		forwardMenuItem = null;
 		forwardButton = null;
 		homeMenuItem = null;
 		homeButton = null;
 
 		KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
 
 	}
 
 	private void destroyMenu(JMenu menu) {
 		if (menu == null)
 			return;
 		PopupMenuListener[] pml = menu.getPopupMenu().getPopupMenuListeners();
 		if (pml != null) {
 			for (PopupMenuListener listener : pml)
 				menu.getPopupMenu().removePopupMenuListener(listener);
 		}
 		Component c;
 		for (int i = 0, n = menu.getComponentCount(); i < n; i++) {
 			c = menu.getComponent(i);
 			if (c instanceof JMenu) {
 				destroyMenu((JMenu) c);
 			}
 			else if (c instanceof JMenuItem) {
 				JMenuItem menuItem = (JMenuItem) c;
 				menuItem.setAction(null);
 				ActionListener[] al = menuItem.getActionListeners();
 				if (al != null) {
 					for (ActionListener listener : al)
 						menuItem.removeActionListener(listener);
 				}
 				ItemListener[] il = menuItem.getItemListeners();
 				if (il != null) {
 					for (ItemListener listener : il)
 						menuItem.removeItemListener(listener);
 				}
 			}
 		}
 		menu.removeAll();
 	}
 
 	private void autoSelectEncoding() {
 		Locale d = Locale.getDefault();
 		String s = d.getLanguage();
 		Page page = editor.getPage();
 		if (d.equals(Locale.CHINA) || d.equals(Locale.PRC)) {
 			page.setCharacterEncoding("GB18030");
 		}
 		else if (d.equals(Locale.TAIWAN))
 			page.setCharacterEncoding("Big5");
 		else if (d.equals(Locale.JAPAN))
 			page.setCharacterEncoding("EUC-JP");
 		else if (d.equals(Locale.KOREA))
 			page.setCharacterEncoding("EUC-KR");
 		else if (s.equals("el"))
 			page.setCharacterEncoding("ISO-8859-7");
 		else if (s.equals("he"))
 			page.setCharacterEncoding("ISO-8859-8");
 		else if (s.equals("ru"))
 			page.setCharacterEncoding("UTF-8");
 		else if (s.equals("ar"))
 			page.setCharacterEncoding("ISO-8859-6");
 		else if (s.equals("th"))
 			page.setCharacterEncoding("ISO-8859-11");
 		else if (s.equals("tr"))
 			page.setCharacterEncoding("ISO-8859-9");
 		else if (s.equals("bs") || s.equals("pl") || s.equals("hr") || s.equals("sk") || s.equals("sl")
 				|| s.equals("hu") || s.equals("cs"))
 			page.setCharacterEncoding("ISO-8859-2");
 		else {
 			page.setCharacterEncoding("UTF-8");
 		}
 	}
 
 	public Editor getEditor() {
 		return editor;
 	}
 
 	private void initPage() {
 
 		autoSelectEncoding();
 
 		if (runOnCD) {
 			navigator.visitLocation(startingURL != null ? startingURL : getStaticRoot() + "index.cml");
 			return;
 		}
 
 		String proxyAddress = preference.get("Proxy Address", null);
 		if (proxyAddress != null) {
 			int proxyPort = preference.getInt("Proxy Port", -1);
 			if (proxyPort != -1) {
 				System.setProperty("http.proxyHost", proxyAddress);
 				System.setProperty("http.proxyPort", proxyPort + "");
 				System.setProperty("proxySet", "true");
 				String proxyUserName = preference.get("Proxy Username", null);
 				String proxyPassword = preference.get("Proxy Password", null);
 				if (proxyUserName != null && proxyPassword != null) {
 					Authenticator.setDefault(new MyAuthenticator(proxyUserName, proxyPassword));
 				}
 			}
 		}
 
 		String spt = preference.get("Start From", null);
 
 		if (startingURL == null && PreferencesDialog.DEFAULT_HOME_PAGE.equals(spt)) {
 			String s = getStaticRoot();
 			Locale l = Locale.getDefault();
 			if (Locale.PRC.equals(l))
 				navigator.visitLocation(s + "cn/index.cml");
 			else if (Locale.TAIWAN.equals(l))
 				navigator.visitLocation(s + "tw/index.cml");
 			else {
 				if ("ru".equals(l.getLanguage())) {
 					navigator.visitLocation(s + "ru/index.cml");
 				}
 				else {
 					navigator.visitLocation(s + "index.cml");
 				}
 			}
 		}
 		else {
 			if (startingURL == null) {
 				if (PreferencesDialog.HOME_PAGE.equals(spt)) {
 					String hp = Initializer.sharedInstance().getSystemProperty(PreferencesDialog.HOME_PAGE);
 					navigator.visitLocation(hp != null ? hp : navigator.getHomePage());
 				}
 				else if (PreferencesDialog.LAST_VISITED_PAGE.equals(spt)) {
 					String lpv = Initializer.sharedInstance().getSystemProperty(PreferencesDialog.LAST_VISITED_PAGE);
 					if (lpv != null)
 						navigator.visitLocation(lpv);
 				}
 				else {
 					navigator.visitLocation(navigator.getHomePage());
 				}
 			}
 			else {
 				navigator.visitLocation(startingURL);
 			}
 		}
 
 		Debugger.print("Load starting page");
 		LogDumper.sharedInstance().dump(new Date() + ": Starting page loaded.");
 
 	}
 
 	/*
 	 * If the page will be saved upon closing, window closing event must be re-dispatched after the writing thread
 	 * returns. For example, this is done in the Page.saveAndClose() method. When the WindowListener is signified by the
 	 * window dispatching event fired at the time the writing thread expires, it calls this method again, and the
 	 * options are checked once again. It is IMPORTANT that the save reminder be set to the proper status. For instance,
 	 * if nothing has been changed since last save, it should declare that fact in the <code>isChanged()</code>
 	 * method.
 	 * 
 	 * @see org.concord.modeler.text.Page#saveAndClose;
 	 */
 	private void savePageAndClose() {
 		SaveReminder reminder = editor.getPage().getSaveReminder();
 		int opt = JOptionPane.NO_OPTION;
 		if (!editor.getPage().isRemote() && reminder.isChanged())
 			opt = reminder.showConfirmDialog(this, FileUtilities.getFileName(editor.getPage().getAddress()));
 		if (opt == JOptionPane.NO_OPTION) {
 			if (windowCount == 1) {
 				if (!SnapshotGallery.sharedInstance().isEmpty()) {
 					String s = getInternationalText("Snapshot");
 					if (JOptionPane.showConfirmDialog(Modeler.this,
 							"You have taken some snapshots. Do you want to save them?", s != null ? s : "Snapshot",
 							JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
 						editor.getPage().importSnapshots();
 						SnapshotGallery.sharedInstance().clear();
 						opt = JOptionPane.CANCEL_OPTION;
 					}
 				}
 			}
 		}
 		switch (opt) {
 		case JOptionPane.YES_OPTION:
 			setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
 			editor.getPage().saveAndClose(this);
 			break;
 		case JOptionPane.NO_OPTION:
 			decideCloseOperation();
 			break;
 		case JOptionPane.CANCEL_OPTION:
 			setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
 			break;
 		default:
 			decideCloseOperation();
 		}
 	}
 
 	private static void reboot() {
 		String jarLocation = System.getProperty("java.class.path");
 		if (IS_MAC) {
 			jarLocation = ModelerUtilities.validateJarLocationOnMacOSX(jarLocation);
 		}
 		else if (System.getProperty("os.name").startsWith("Windows")) {
 			jarLocation = "\"" + jarLocation + "\"";
 		}
 		String s = "java -jar " + jarLocation;
 		s += hostIsLocal ? " local" : " remote";
 		if (startingURL != null)
 			s += " " + startingURL;
 		try {
 			// MUST close this ServerSocket, or it will remain blocked even after shutdown
 			if (serverSocket != null)
 				serverSocket.close();
 			Runtime.getRuntime().exec(s);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 		System.exit(0);
 	}
 
 	private void decideCloseOperation() {
 		if (editor.getPage().isReading() || editor.getPage().isWriting()) // reject closing request
 			return;
 		xOffset -= offset;
 		yOffset -= offset;
 		if (windowCount >= 1)
 			windowCount--;
 		if (windowCount < 1) {
 			savePreferences();
 			LogDumper.sharedInstance().output();
 			navigator.output();
 			if (restart) {
 				reboot();
 				restart = false;
 			}
 			else {
 				System.exit(0);
 			}
 		}
 		else {
 			editor.getPage().stopAllRunningModels();
 			EventQueue.invokeLater(new Runnable() {
 				public void run() {
 					destroy();
 					dispose();
 				}
 			});
 		}
 	}
 
 	private void savePreferences() {
 
 		/* save general information */
 		preference.put("Version", VERSION);
 
 		/* save size and location */
 		if (windowCount < 2) {
 			preference.putInt("Upper Left X", getBounds().x);
 			preference.putInt("Upper Left Y", getBounds().y);
 			preference.putInt("Width", editor.getWidth());
 			preference.putInt("Height", editor.getHeight());
 		}
 
 		Initializer.sharedInstance().putSystemProperty(PreferencesDialog.LAST_VISITED_PAGE,
 				editor.getPage().getAddress());
 		if (preferencesDialog != null) {
 
 			// save home page settings, if the preferences dialog has ever been invoked.
 			Initializer.sharedInstance().putSystemProperty(PreferencesDialog.HOME_PAGE, preferencesDialog.getHome());
 			preference.put("Start From", preferencesDialog.getStartPageType());
 
 			/* save proxy server settings */
 			if (preferencesDialog.getUseProxy()) {
 				String proxyAddress = preferencesDialog.getProxyAddress();
 				if (proxyAddress != null && !proxyAddress.trim().equals("")) {
 					preference.put("Proxy Address", proxyAddress);
 				}
 				else {
 					preference.remove("Proxy Address");
 				}
 				int proxyPortNumber = preferencesDialog.getProxyPortNumber();
 				if (proxyPortNumber >= 0) {
 					preference.putInt("Proxy Port", proxyPortNumber);
 				}
 				else {
 					preference.remove("Proxy Port");
 				}
 				String proxyUserName = preferencesDialog.getProxyUserName();
 				if (proxyUserName != null && !proxyUserName.trim().equals("")) {
 					preference.put("Proxy Username", proxyUserName);
 				}
 				else {
 					preference.remove("Proxy Username");
 				}
 				char[] proxyPassword = preferencesDialog.getProxyPassword();
 				if (proxyPassword != null && proxyPassword.length > 0) {
 					preference.put("Proxy Password", new String(proxyPassword));
 				}
 				else {
 					preference.remove("Proxy Password");
 				}
 			}
 			else {
 				preference.remove("Proxy Address");
 				preference.remove("Proxy Port");
 				preference.remove("Proxy Username");
 				preference.remove("Proxy Password");
 			}
 
 		}
 
 		/* save look&feel setting */
 		if (lookandfeel != null && !disableSetLAF)
 			preference.put("Look&Feel", lookandfeel);
 
 		/* save tape length */
 		preference.putInt("Tape Length", tapeLength);
 
 		/* save history days */
 		preference.putInt("History", HistoryManager.sharedInstance().getDays());
 
 		BookmarkManager.sharedInstance().writeBookmarks(
 				new File(Initializer.sharedInstance().getPropertyDirectory(), "bookmarks.xml"));
 		HistoryManager.sharedInstance().writeHistory(
 				new File(Initializer.sharedInstance().getPropertyDirectory(), "history.xml"));
 		ModelerUtilities.fileChooser.writeHistory(new File(Initializer.sharedInstance().getPropertyDirectory(),
 				"filechooser.xml"));
 		Initializer.sharedInstance().writeSystemProperties();
 
 	}
 
 	Modeler openPageInNewWindow(String address) {
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 		int w = Initializer.sharedInstance().getPreferences().getInt("Width", screenSize.width - 200);
 		int h = Initializer.sharedInstance().getPreferences().getInt("Height", screenSize.height - 300);
 		xOffset += offset;
 		yOffset += offset;
 		int x = getLocation().x + xOffset;
 		int y = getLocation().y + yOffset;
 		return openNewWindow(true, address, x, y, w, h, true, true, true, true, false);
 	}
 
 	Modeler openNewWindow(boolean currentPage, boolean offspring) {
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 		if (offspring)
 			return openNewWindow(currentPage, editor.getPage().getAddress(), 200, 100, screenSize.width - 250,
 					screenSize.height - 300, true, true, true, true, false);
 		int w = Initializer.sharedInstance().getPreferences().getInt("Width", screenSize.width - 200);
 		int h = Initializer.sharedInstance().getPreferences().getInt("Height", screenSize.height - 300);
 		xOffset += offset;
 		yOffset += offset;
 		int x = getLocation().x + xOffset;
 		int y = getLocation().y + yOffset;
 		return openNewWindow(currentPage, editor.getPage().getAddress(), x, y, w, h, true, true, true, true, false);
 	}
 
 	Modeler openNewWindowWithoutBars(boolean currentPage, boolean offspring) {
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 		if (offspring)
 			return openNewWindow(currentPage, editor.getPage().getAddress(), 200, 100, screenSize.width - 250,
 					screenSize.height - 300, true, false, false, false, false);
 		int w = Initializer.sharedInstance().getPreferences().getInt("Width", screenSize.width - 200);
 		int h = Initializer.sharedInstance().getPreferences().getInt("Height", screenSize.height - 300);
 		xOffset += offset;
 		yOffset += offset;
 		int x = getLocation().x + xOffset;
 		int y = getLocation().y + yOffset;
 		return openNewWindow(currentPage, editor.getPage().getAddress(), x, y, w, h, true, false, false, false, false);
 	}
 
 	static Modeler openNewWindow(boolean currentPage, String currentAddress, int x, int y, int w, int h,
 			boolean resizable, boolean hasToolbar, boolean hasMenubar, boolean hasStatusbar, boolean fullscreen) {
 		// unregisterComponentWithToolTip();
 		try {
 			final Modeler m = new Modeler();
 			windowList.add(m);
 			m.setResizable(resizable);
 			m.editor.setPreferredSize(new Dimension(w, h));
 			m.statusBar.setPreferredSize(new Dimension(w, 20));
 			m.setLocation(x, y);
 			if (!hasMenubar)
 				m.setJMenuBar(null);
 			if (!hasToolbar) {
 				m.getContentPane().remove(m.toolBar);
 				m.editor.removeAllToolBars();
 			}
 			if (!hasStatusbar)
 				m.getContentPane().remove(m.statusBar);
 			m.pack();
 			if (currentPage && !currentAddress.equals("Untitled.cml")) {
 				m.editor.getPage().openHyperlink(currentAddress);
 			}
 			else if (currentAddress.equals("Untitled.cml")) {
 				String s = Initializer.sharedInstance().getSystemProperty(PreferencesDialog.HOME_PAGE);
 				m.editor.getPage().openHyperlink(s != null ? s : m.navigator.getHomePage());
 			}
 			m.toFront();
 			m.editor.requestFocusInWindow();
 			m.setVisible(true);
 			return m;
 		}
 		catch (Throwable t) {
 			t.printStackTrace();
 			JOptionPane.showMessageDialog(null, "Failed in creating a new window. It may be good to start " + NAME
 					+ " now.\nCaused by: " + t, "Error", JOptionPane.ERROR_MESSAGE);
 			return null;
 		}
 	}
 
 	public static String getInternationalText(String name) {
 		if (bundle == null)
 			return null;
 		if (isUSLocale)
 			return null;
 		if (name == null)
 			return null;
 		String s = null;
 		try {
 			s = bundle.getString(name);
 		}
 		catch (MissingResourceException e) {
 			s = null;
 		}
 		return s;
 	}
 
 	void createMenuBar() {
 
 		final Page page = editor.getPage();
 
 		menuBar = new JMenuBar();
 
 		// create the File Menu
 
 		String s = getInternationalText("OpenRecentPages");
 		JMenu recentFileMenu = new JMenu(s != null ? s : "Recent Opened Files");
 		page.setRecentFilesMenu(recentFileMenu);
 		JMenuItem mi = null;
 		String[] recentFiles = ModelerUtilities.fileChooser.getRecentFiles();
 		for (int i = 0; i < 4; i++) {
 			mi = new JMenuItem(i < recentFiles.length ? recentFiles[i] : "");
 			mi.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					String s = ((JMenuItem) e.getSource()).getText();
 					if (s != null && !s.trim().equals(""))
 						navigator.visitLocation(s);
 				}
 			});
 			recentFileMenu.add(mi);
 		}
 
 		final JMenuItem reportMI = new JMenuItem(createReportAction);
 		s = getInternationalText("CreateJNLP");
 		final JMenuItem jnlpMI = new JMenuItem((s != null ? s : "Create a Customized JNLP Launching File") + "...");
 		s = getInternationalText("CreateLaunchingUrlInSystemClipboard");
 		final JMenuItem jnlpUrlMI = new JMenuItem(s != null ? s : "Create a Launching URL in System Clipboard");
 		s = getInternationalText("CompressPage");
 		final JMenuItem compressPageMI = new JMenuItem((s != null ? s : "Compress Current Page") + "...");
 		s = getInternationalText("CompressFolder");
 		final JMenuItem compressFolderMI = new JMenuItem((s != null ? s : "Compress Current Activity Folder") + "...");
 
 		s = getInternationalText("File");
 		JMenu menu = new JMenu(s != null ? s : "File");
 		menu.getPopupMenu().addPopupMenuListener(new PopupMenuListener() {
 			public void popupMenuCanceled(PopupMenuEvent e) {
 			}
 
 			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
 				if (!IS_MAC) {
 					boolean isRemote = page.isRemote();
 					jnlpMI.setEnabled(isRemote);
 					jnlpUrlMI.setEnabled(isRemote);
 					compressPageMI.setEnabled(!isRemote);
 					compressFolderMI.setEnabled(!isRemote);
 				}
 				if (page.getAddress().equals("Untitled.cml")) {
 					reportMI.setEnabled(false);
 				}
 				else {
 					reportMI.setEnabled(!page.isEditable());
 				}
 			}
 
 			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
 			}
 		});
 		menu.setMnemonic(KeyEvent.VK_F);
 		menuBar.add(menu);
 		if (windowCount == 0)
 			menu.setEnabled(false);
 		editor.addDisabledComponentWhileLoading(menu);
 
 		JMenuItem menuItem = new JMenuItem(page.getAction(Page.NEW_PAGE));
 		s = getInternationalText("NewBlankPage");
 		if (s != null)
 			menuItem.setText(s);
 		menuItem.setMnemonic(KeyEvent.VK_N);
 		menuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				editor.setEditable(true);
 				autoSelectEncoding();
 			}
 		});
 		menu.add(menuItem);
 
 		s = getInternationalText("NewWindow");
 		menuItem = new JMenuItem(s != null ? s : "New Window");
 		menuItem.setMnemonic(KeyEvent.VK_W);
 		menuItem.setAccelerator(IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_N,
 				KeyEvent.META_MASK | KeyEvent.SHIFT_MASK, true) : KeyStroke.getKeyStroke(KeyEvent.VK_N,
 				KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK, true));
 		menuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				openNewWindow(true, false);
 			}
 		});
 		menu.add(menuItem);
 		menu.addSeparator();
 
 		menuItem = new JMenuItem(page.getAction(Page.OPEN_PAGE));
 		s = getInternationalText("OpenPage");
 		menuItem.setText((s != null ? s : "Open") + "...");
 		menuItem.setMnemonic(KeyEvent.VK_O);
 		menu.add(menuItem);
 
 		menu.add(recentFileMenu);
 		recentFileMenu.setMnemonic(KeyEvent.VK_F);
 		menu.addSeparator();
 
 		menuItem = new JMenuItem(page.getAction(Page.SAVE_PAGE));
 		s = getInternationalText("SavePage");
 		if (s != null)
 			menuItem.setText(s);
 		menuItem.setMnemonic(KeyEvent.VK_S);
 		menu.add(menuItem);
 
 		menuItem = new JMenuItem(page.getAction(Page.SAVE_PAGE_AS));
 		s = getInternationalText("SavePageAs");
 		menuItem.setText((s != null ? s : "Save As") + "...");
 		menuItem.setMnemonic(KeyEvent.VK_A);
 		menu.add(menuItem);
 
 		menuItem = new JMenuItem(page.getAction(Page.HTML_CONVERSION));
 		menuItem.setMnemonic(KeyEvent.VK_H);
 		s = getInternationalText("SavePageAsHTML");
 		menuItem.setText((s != null ? s : "Save As HTML") + "...");
 		menuItem.setToolTipText("Convert this XML page to a HTML page");
 		menu.add(menuItem);
 
 		compressPageMI.setToolTipText("Compress all the files of the current page in a ZIP file");
 		compressPageMI.setMnemonic(KeyEvent.VK_G);
 		compressPageMI.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				page.writePageToZipFile();
 			}
 		});
 		if (IS_MAC)
 			navigator.addEnabledComponentWhenLocal(compressPageMI);
 		menu.add(compressPageMI);
 
 		compressFolderMI.setToolTipText("Compress all the files of the current activity folder in a ZIP file");
 		compressFolderMI.setMnemonic(KeyEvent.VK_M);
 		compressFolderMI.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (page.isRemote())
 					return;
 				JComboBox comboBox = new JComboBox();
 				File dir = new File(FileUtilities.getCodeBase(page.getAddress()));
 				if (!dir.isDirectory())
 					return;
 				String[] fn = dir.list(new FilenameFilter() {
 					public boolean accept(File directory, String name) {
 						return name.toLowerCase().endsWith(".cml");
 					}
 				});
 				if (fn == null)
 					return;
 				for (String s : fn)
 					comboBox.addItem(s);
 				if (JOptionPane.showConfirmDialog(Modeler.this, comboBox, "Please select the first page:",
 						JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION) {
 					new FolderCompressor(editor).compressCurrentFolder((String) comboBox.getSelectedItem());
 				}
 			}
 		});
 		menu.add(compressFolderMI);
 		if (IS_MAC)
 			navigator.addEnabledComponentWhenLocal(compressFolderMI);
 		menu.addSeparator();
 
 		jnlpUrlMI.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				StringSelection ss = new StringSelection(getContextRoot() + "tmp.jnlp?address="
 						+ FileUtilities.httpEncode(page.getAddress()));
 				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
 				JOptionPane.showMessageDialog(Modeler.this,
 						"An URL for opening the current MW page from outside MW\nis now available for pasting.");
 			}
 		});
 		menu.add(jnlpUrlMI);
 		jnlpMI.setMnemonic(KeyEvent.VK_J);
 		jnlpMI.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				JNLPSaver.save(Modeler.this, page.getAddress(), page.getTitle(), true);
 			}
 		});
 		menu.add(jnlpMI);
 		if (IS_MAC) {
 			navigator.addEnabledComponentWhenRemote(jnlpMI);
 			navigator.addEnabledComponentWhenRemote(jnlpUrlMI);
 		}
 
 		s = getInternationalText("CreateReport");
 		if (s != null)
 			reportMI.setText(s);
 		reportMI.setIcon(null);
 		menu.add(reportMI);
 		menu.addSeparator();
 
 		menuItem = new JMenuItem(page.getAction("Page Setup"));
 		s = getInternationalText("PageSetup");
 		menuItem.setText((s != null ? s : "Page Setup") + "...");
 		menuItem.setMnemonic(KeyEvent.VK_U);
 		menu.add(menuItem);
 
 		menuItem = new JMenuItem(page.getAction("Preview"));
 		menuItem.setMnemonic(KeyEvent.VK_V);
 		s = getInternationalText("PrintPreview");
 		menuItem.setText((s != null ? s : "Print Preview") + "...");
 		menu.add(menuItem);
 
 		menuItem = new JMenuItem(page.getAction("Print"));
 		menuItem.setMnemonic(KeyEvent.VK_P);
 		s = getInternationalText("Print");
 		menuItem.setText((s != null ? s : "Print") + "...");
 		menu.add(menuItem);
 		menu.addSeparator();
 
 		s = getInternationalText("ToggleEditor");
 		menuItem = new JMenuItem(s != null ? s : "Toggle Lock on Editor");
 		menuItem.setMnemonic(KeyEvent.VK_L);
 		menuItem.setAccelerator(IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_L,
 				KeyEvent.META_MASK | KeyEvent.SHIFT_MASK, true) : KeyStroke.getKeyStroke(KeyEvent.VK_L,
 				KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK, true));
 		menuItem.setRequestFocusEnabled(false);
 		menuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				editor.editCheckBox.doClick();
 			}
 		});
 		menu.add(menuItem);
 
 		menuItem = new JMenuItem(page.getAction(Page.SET_PROPERTIES));
 		menuItem.setIcon(null);
 		if (IS_MAC) {
 			menuItem.setMnemonic(KeyEvent.VK_I);
 			menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.META_MASK, true));
 		}
 		else {
 			menuItem.setMnemonic(KeyEvent.VK_R);
 		}
 		s = getInternationalText("Properties");
 		menuItem.setText((s != null ? s : "Properties") + "...");
 		menu.add(menuItem);
 
 		menuItem = new JMenuItem(editor.openSnapshotGallery);
 		s = getInternationalText("ViewSnapshot");
 		menuItem.setText((s != null ? s : "View Snapshots") + "...");
 		menu.add(menuItem);
 
 		s = getInternationalText("ViewSessionLog");
 		menuItem = new JMenuItem((s != null ? s : "View Session Log") + "...");
 		menuItem.setMnemonic(KeyEvent.VK_I);
 		menuItem.setToolTipText("The log maintains the records of visits and problems since this launch.");
 		menuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				LogDumper.sharedInstance().show(Modeler.this);
 			}
 		});
 		menu.add(menuItem);
 		menu.addSeparator();
 
 		s = getInternationalText("WorkOffline");
 		menuItem = new JCheckBoxMenuItem(s != null ? s : "Work Offline");
 		menuItem.setMnemonic(KeyEvent.VK_K);
 		menuItem.setSelected(ConnectionManager.sharedInstance().getWorkOffline());
 		menuItem.addItemListener(new ItemListener() {
 			public void itemStateChanged(ItemEvent e) {
 				JMenuItem mi = (JMenuItem) e.getSource();
 				ConnectionManager.sharedInstance().setWorkOffline(mi.isSelected());
 			}
 		});
 		menu.add(menuItem);
 
 		if (!IS_MAC) {
 			menuItem = menu.add(page.getActionMap().get(Page.CLOSE_PAGE));
 			s = getInternationalText("Exit");
 			if (s != null)
 				menuItem.setText(s);
 			menuItem.setMnemonic(KeyEvent.VK_X);
 			menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK));
 		}
 
 		// create the Edit Menu
 
 		final JMenuItem cutMI = new JMenuItem(page.getAction(DefaultEditorKit.cutAction));
 		final JMenuItem copyMI = new JMenuItem(page.getAction(DefaultEditorKit.copyAction));
 
 		s = getInternationalText("Edit");
 		menu = new JMenu(s != null ? s : "Edit");
 		menu.setMnemonic(KeyEvent.VK_E);
 		menu.getPopupMenu().addPopupMenuListener(new PopupMenuListener() {
 			public void popupMenuCanceled(PopupMenuEvent e) {
 			}
 
 			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
 			}
 
 			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
 				boolean b = page.getSelectedText() != null;
 				copyMI.setEnabled(b);
 				if (!page.isEditable())
 					return;
 				cutMI.setEnabled(b);
 			}
 		});
 		menuBar.add(menu);
 		if (windowCount == 0)
 			menu.setEnabled(false);
 		editor.addDisabledComponentWhileLoading(menu);
 
 		menuItem = new JMenuItem(page.getAction(Page.UNDO));
 		menuItem.setMnemonic(KeyEvent.VK_U);
 		s = getInternationalText("Undo");
 		if (s != null)
 			menuItem.setText(s);
 		menu.add(menuItem);
 
 		menuItem = new JMenuItem(page.getAction(Page.REDO));
 		menuItem.setMnemonic(KeyEvent.VK_R);
 		s = getInternationalText("Redo");
 		if (s != null)
 			menuItem.setText(s);
 		menu.add(menuItem);
 		menu.addSeparator();
 
 		s = getInternationalText("Cut");
 		cutMI.setText(s != null ? s : "Cut");
 		cutMI.setMnemonic(KeyEvent.VK_T);
 		cutMI.setAccelerator(IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.META_MASK) : KeyStroke
 				.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_MASK));
 		menu.add(cutMI);
 		editor.addEnabledComponentWhenEditable(cutMI);
 
 		s = getInternationalText("Copy");
 		copyMI.setText(s != null ? s : "Copy");
 		copyMI.setMnemonic(KeyEvent.VK_C);
 		copyMI.setAccelerator(IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_MASK) : KeyStroke
 				.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));
 		menu.add(copyMI);
 
 		menuItem = new JMenuItem(page.getAction(DefaultEditorKit.pasteAction));
 		s = getInternationalText("Paste");
 		menuItem.setText(s != null ? s : "Paste");
 		menuItem.setMnemonic(KeyEvent.VK_P);
 		menuItem.setAccelerator(IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_MASK) : KeyStroke
 				.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_MASK));
 		menu.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JMenuItem(page.getAction("Paste Plain Text"));
 		s = getInternationalText("PastePlainText");
 		menuItem.setText(s != null ? s : "Paste Plain Text");
 		menu.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 		menu.addSeparator();
 
 		s = getInternationalText("Title");
 		menuItem = new JMenuItem((s != null ? s : "Title") + "...");
 		menuItem.setMnemonic(KeyEvent.VK_T);
 		menuItem.setAccelerator(IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.META_MASK, true) : KeyStroke
 				.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_MASK, true));
 		menuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				page.inputTitle();
 			}
 		});
 		menu.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		s = getInternationalText("BackgroundSound");
 		menuItem = new JMenuItem((s != null ? s : "Background Sound") + "...");
 		menuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				page.inputBackgroundSound();
 			}
 		});
 		menu.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		final ButtonGroup encodingGroup = new ButtonGroup();
 		final ActionListener encodingListener = new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				AbstractButton ab = (AbstractButton) e.getSource();
 				if (ab.getName() != null)
 					page.setCharacterEncoding(ab.getName());
 			}
 		};
 
 		s = getInternationalText("CharacterEncoding");
 		JMenu subMenu = new JMenu(s != null ? s : "Character Encoding");
 		subMenu.setMnemonic(KeyEvent.VK_E);
 		subMenu.getPopupMenu().addPopupMenuListener(new PopupMenuListener() {
 			public void popupMenuCanceled(PopupMenuEvent e) {
 			}
 
 			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
 			}
 
 			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
 				JMenuItem mi;
 				Enumeration en = encodingGroup.getElements();
 				while (en.hasMoreElements()) {
 					mi = (JMenuItem) en.nextElement();
 					if (mi.getName().equals(page.getCharacterEncoding())) {
 						mi.removeActionListener(encodingListener);
 						mi.setSelected(true);
 						mi.addActionListener(encodingListener);
 						break;
 					}
 				}
 			}
 		});
 		menu.add(subMenu);
 		menu.addSeparator();
 
 		menuItem = new JRadioButtonMenuItem("Arabic (ISO-8859-6)");
 		menuItem.setName("ISO-8859-6");
 		menuItem.addActionListener(encodingListener);
 		subMenu.add(menuItem);
 		encodingGroup.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JRadioButtonMenuItem("Baltic (ISO-8859-4)");
 		menuItem.setName("ISO-8859-4");
 		menuItem.addActionListener(encodingListener);
 		subMenu.add(menuItem);
 		encodingGroup.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JRadioButtonMenuItem("Central European (ISO-8859-2)");
 		menuItem.setName("ISO-8859-2");
 		menuItem.addActionListener(encodingListener);
 		subMenu.add(menuItem);
 		encodingGroup.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		s = getInternationalText("SimplifiedChinese");
 		menuItem = new JRadioButtonMenuItem((s != null ? s : "Chinese Simplified") + " (GB18030)");
 		menuItem.setName("GB18030");
 		menuItem.addActionListener(encodingListener);
 		subMenu.add(menuItem);
 		encodingGroup.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		s = getInternationalText("TraditionalChinese");
 		menuItem = new JRadioButtonMenuItem((s != null ? s : "Chinese Traditional") + " (Big5)");
 		menuItem.setName("Big5");
 		menuItem.addActionListener(encodingListener);
 		subMenu.add(menuItem);
 		encodingGroup.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JRadioButtonMenuItem("Cyrillic (ISO-8859-5)");
 		menuItem.setName("ISO-8859-5");
 		menuItem.addActionListener(encodingListener);
 		subMenu.add(menuItem);
 		encodingGroup.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JRadioButtonMenuItem("Greek (ISO-8859-7)");
 		menuItem.setName("ISO-8859-7");
 		menuItem.addActionListener(encodingListener);
 		subMenu.add(menuItem);
 		encodingGroup.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JRadioButtonMenuItem("Hebrew (ISO-8859-8)");
 		menuItem.setName("ISO-8859-8");
 		menuItem.addActionListener(encodingListener);
 		subMenu.add(menuItem);
 		encodingGroup.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JRadioButtonMenuItem("Japanese (EUC-JP)");
 		menuItem.setName("EUC-JP");
 		menuItem.addActionListener(encodingListener);
 		subMenu.add(menuItem);
 		encodingGroup.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JRadioButtonMenuItem("Korean (EUC-KR)");
 		menuItem.setName("EUC-KR");
 		menuItem.addActionListener(encodingListener);
 		subMenu.add(menuItem);
 		encodingGroup.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JRadioButtonMenuItem("Thai (ISO-8859-11)");
 		menuItem.setName("ISO-8859-11");
 		menuItem.addActionListener(encodingListener);
 		subMenu.add(menuItem);
 		encodingGroup.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JRadioButtonMenuItem("Turkish (ISO-8859-9)");
 		menuItem.setName("ISO-8859-9");
 		menuItem.addActionListener(encodingListener);
 		subMenu.add(menuItem);
 		encodingGroup.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JRadioButtonMenuItem("Western (ISO-8859-1)");
 		menuItem.setName("ISO-8859-1");
 		menuItem.addActionListener(encodingListener);
 		subMenu.add(menuItem);
 		encodingGroup.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JRadioButtonMenuItem("Unicode (UTF-8)");
 		menuItem.setName("UTF-8");
 		menuItem.addActionListener(encodingListener);
 		subMenu.add(menuItem);
 		encodingGroup.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JMenuItem(page.getAction(DefaultEditorKit.selectAllAction));
 		menuItem.setMnemonic(KeyEvent.VK_A);
 		s = getInternationalText("SelectAll");
 		menuItem.setText(s != null ? s : "Select All");
 		menu.add(menuItem);
 
 		s = getInternationalText("Find");
 		menuItem = new JMenuItem((s != null ? s : "Find on This Page") + "...");
 		menuItem.setMnemonic(KeyEvent.VK_F);
 		menuItem.addActionListener(editor.findAction);
 		menuItem.setAccelerator(IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.META_MASK, true) : KeyStroke
 				.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_MASK, true));
 		menu.add(menuItem);
 
 		// create the Insert Menu
 
 		s = getInternationalText("Insert");
 		menu = new JMenu(s != null ? s : "Insert");
 		menu.setMnemonic(KeyEvent.VK_I);
 		menu.getPopupMenu().addPopupMenuListener(new PopupMenuListener() {
 			public void popupMenuCanceled(PopupMenuEvent e) {
 			}
 
 			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
 			}
 
 			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
 				if (!editor.isEditable())
 					return;
 				insertSymbolMenuItem.setEnabled(page.getCharacterEncoding().equals("UTF-8"));
 			}
 		});
 		menuBar.add(menu);
 		if (windowCount == 0)
 			menu.setEnabled(false);
 		editor.addDisabledComponentWhileLoading(menu);
 
 		// FIXME: insert file doesn't work right. editor.addEnabledComponentWhenEditable(menuItem);
 		menuItem = new JMenuItem(page.getAction("File"));
 		menuItem.setMnemonic(KeyEvent.VK_F);
 		s = getInternationalText("InsertFile");
 		if (s != null)
 			menuItem.setText(s);
 		menu.add(menuItem);
 		menuItem.setEnabled(false);
 
 		s = getInternationalText("InsertPicture");
 		subMenu = new JMenu(s != null ? s : "Picture");
 		subMenu.setMnemonic(KeyEvent.VK_P);
 		menu.add(subMenu);
 		menu.addSeparator();
 
 		menuItem = new JMenuItem(page.getAction("Input Image"));
 		s = getInternationalText("InputImage");
 		if (s != null)
 			menuItem.getAction().putValue("i18n", s);
 		menuItem.setMnemonic(KeyEvent.VK_F);
 		menuItem.setIcon(null);
 		s = getInternationalText("InsertPictureFromFile");
 		menuItem.setText((s != null ? s : "From File") + "...");
 		subMenu.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 		subMenu.addSeparator();
 
 		s = getInternationalText("InsertPictureFromSnapshotGallery");
 		menuItem = new JMenuItem((s != null ? s : "From Snapshot Gallery") + "...");
 		menuItem.setMnemonic(KeyEvent.VK_S);
 		menuItem.addActionListener(editor.openSnapshotGallery);
 		subMenu.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		s = getInternationalText("InsertModel");
 		subMenu = new JMenu(s != null ? s : "Model Container");
 		subMenu.setMnemonic(KeyEvent.VK_M);
 		menu.add(subMenu);
 
 		menuItem = new JMenuItem(page.getAction(Page.INSERT_ATOM_CONTAINER));
 		menuItem.setMnemonic(KeyEvent.VK_A);
 		menuItem.setIcon(null);
 		s = getInternationalText("InsertBasic2DContainer");
 		menuItem.setText(s != null ? s : "Basic 2D Molecular Simulator");
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.ALT_MASK | KeyEvent.SHIFT_MASK, true));
 		subMenu.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JMenuItem(page.getAction(Page.INSERT_CHEM_CONTAINER));
 		menuItem.setMnemonic(KeyEvent.VK_C);
 		menuItem.setIcon(null);
 		s = getInternationalText("InsertReaction2DContainer");
 		menuItem.setText(s != null ? s : "2D Chemical Reaction Simulator");
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, KeyEvent.ALT_MASK | KeyEvent.SHIFT_MASK, true));
 		subMenu.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JMenuItem(page.getAction(Page.INSERT_PROSYN_CONTAINER));
 		menuItem.setMnemonic(KeyEvent.VK_P);
 		menuItem.setIcon(null);
 		s = getInternationalText("InsertProteinSynthesisContainer");
 		menuItem.setText(s != null ? s : "2D Protein Synthesis Simulator");
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, KeyEvent.ALT_MASK | KeyEvent.SHIFT_MASK, true));
 		subMenu.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JMenuItem(page.getAction(Page.INSERT_GB_CONTAINER));
 		menuItem.setMnemonic(KeyEvent.VK_G);
 		menuItem.setIcon(null);
 		s = getInternationalText("InsertMesoscaleContainer");
 		menuItem.setText(s != null ? s : "2D Mesoscale Particle Simulator");
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, KeyEvent.ALT_MASK | KeyEvent.SHIFT_MASK, true));
 		subMenu.add(menuItem);
 		subMenu.addSeparator();
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JMenuItem(page.getAction(Page.INSERT_COMPONENT));
 		menuItem.setName(Page.INSERT_JMOL);
 		s = getInternationalText("InsertJmolContainer");
 		menuItem.setText((s != null ? s : Page.INSERT_JMOL) + "...");
 		menuItem.setMnemonic(KeyEvent.VK_V);
 		menuItem.setIcon(null);
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_5, KeyEvent.ALT_MASK | KeyEvent.SHIFT_MASK, true));
 		subMenu.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JMenuItem(page.getAction(Page.INSERT_COMPONENT));
 		menuItem.setName(Page.INSERT_MW3D);
 		s = getInternationalText("InsertBasic3DContainer");
 		menuItem.setText((s != null ? s : Page.INSERT_MW3D) + "...");
 		menuItem.setMnemonic(KeyEvent.VK_Y);
 		menuItem.setIcon(null);
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_6, KeyEvent.ALT_MASK | KeyEvent.SHIFT_MASK, true));
 		subMenu.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		s = getInternationalText("InsertModelOutput");
 		subMenu = new JMenu(s != null ? s : "Model Output");
 		subMenu.setMnemonic(KeyEvent.VK_G);
 		menu.add(subMenu);
 
 		menuItem = new JMenuItem(page.getAction(Page.INSERT_COMPONENT));
 		menuItem.setName("Numeric Box");
 		s = getInternationalText("InsertNumericBox");
 		menuItem.setText((s != null ? s : "Numeric Box") + "...");
 		menuItem.setMnemonic(KeyEvent.VK_N);
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.ALT_MASK | KeyEvent.SHIFT_MASK, true));
 		subMenu.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JMenuItem(page.getAction(Page.INSERT_COMPONENT));
 		menuItem.setName("Bar Graph");
 		s = getInternationalText("InsertBarGraph");
 		menuItem.setText((s != null ? s : "Bar Graph") + "...");
 		menuItem.setMnemonic(KeyEvent.VK_B);
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.ALT_MASK | KeyEvent.SHIFT_MASK, true));
 		subMenu.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JMenuItem(page.getAction(Page.INSERT_COMPONENT));
 		menuItem.setMnemonic(KeyEvent.VK_X);
 		menuItem.setName("X-Y Graph");
 		s = getInternationalText("InsertXYGraph");
 		menuItem.setText((s != null ? s : "X-Y Graph") + "...");
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.ALT_MASK | KeyEvent.SHIFT_MASK, true));
 		subMenu.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JMenuItem(page.getAction(Page.INSERT_COMPONENT));
 		menuItem.setMnemonic(KeyEvent.VK_H);
 		menuItem.setName("Histogram");
 		s = getInternationalText("InsertHistogram");
 		menuItem.setText((s != null ? s : "Histogram") + "...");
 		menuItem.setEnabled(false);
 		subMenu.add(menuItem);
 		// editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JMenuItem(page.getAction(Page.INSERT_COMPONENT));
 		menuItem.setMnemonic(KeyEvent.VK_P);
 		menuItem.setName("Pie Chart");
 		s = getInternationalText("InsertPieChart");
 		menuItem.setText((s != null ? s : "Pie Chart") + "...");
 		menuItem.setEnabled(false);
 		subMenu.add(menuItem);
 		// editor.addEnabledComponentWhenEditable(menuItem);
 
 		s = getInternationalText("InsertInstrument");
 		subMenu = new JMenu(s != null ? s : "Instrument");
 		menu.add(subMenu);
 
 		menuItem = new JMenuItem(page.getAction(Page.INSERT_COMPONENT));
 		menuItem.setMnemonic(KeyEvent.VK_B);
 		menuItem.setName("Diffraction Device");
 		s = getInternationalText("InsertDiffractionDevice");
 		menuItem.setText((s != null ? s : "Diffraction Device") + "...");
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.ALT_MASK | KeyEvent.SHIFT_MASK, true));
 		subMenu.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JMenuItem(page.getAction(Page.INSERT_COMPONENT));
 		menuItem.setMnemonic(KeyEvent.VK_B);
 		menuItem.setName("Emission and Absorption Spectrometer");
 		s = getInternationalText("InsertSpectrometer");
 		menuItem.setText((s != null ? s : "Emission and Absorption Spectrometer") + "...");
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.ALT_MASK | KeyEvent.SHIFT_MASK, true));
 		subMenu.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		s = getInternationalText("InsertStandardController");
 		subMenu = new JMenu(s != null ? s : "Standard Controller for Model");
 		subMenu.setMnemonic(KeyEvent.VK_C);
 		menu.add(subMenu);
 
 		menuItem = new JMenuItem(page.getAction(Page.INSERT_COMPONENT));
 		menuItem.setMnemonic(KeyEvent.VK_B);
 		menuItem.setName("Button");
 		s = getInternationalText("InsertButton");
 		menuItem.setText((s != null ? s : "Button") + "...");
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.ALT_MASK | KeyEvent.SHIFT_MASK, true));
 		subMenu.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JMenuItem(page.getAction(Page.INSERT_COMPONENT));
 		menuItem.setMnemonic(KeyEvent.VK_C);
 		menuItem.setName("Check Box");
 		s = getInternationalText("InsertCheckBox");
 		menuItem.setText((s != null ? s : "Check Box") + "...");
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, KeyEvent.ALT_MASK | KeyEvent.SHIFT_MASK, true));
 		subMenu.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JMenuItem(page.getAction(Page.INSERT_COMPONENT));
 		menuItem.setMnemonic(KeyEvent.VK_S);
 		menuItem.setName("Spinner");
 		s = getInternationalText("InsertSpinner");
 		menuItem.setText((s != null ? s : "Spinner") + "...");
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.ALT_MASK | KeyEvent.SHIFT_MASK, true));
 		subMenu.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JMenuItem(page.getAction(Page.INSERT_COMPONENT));
 		menuItem.setMnemonic(KeyEvent.VK_L);
 		menuItem.setName("Slider");
 		s = getInternationalText("InsertSlider");
 		menuItem.setText((s != null ? s : "Slider") + "...");
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.ALT_MASK | KeyEvent.SHIFT_MASK, true));
 		subMenu.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JMenuItem(page.getAction(Page.INSERT_COMPONENT));
 		menuItem.setMnemonic(KeyEvent.VK_M);
 		menuItem.setName("Combo Box");
 		s = getInternationalText("InsertComboBox");
 		menuItem.setText((s != null ? s : "Combo Box") + "...");
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.ALT_MASK | KeyEvent.SHIFT_MASK, true));
 		subMenu.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JMenuItem(page.getAction(Page.INSERT_COMPONENT));
 		menuItem.setName("A Group of Radio Buttons");
 		s = getInternationalText("InsertRadioButton");
 		menuItem.setText((s != null ? s : menuItem.getName()) + "...");
 		menuItem.setMnemonic(KeyEvent.VK_R);
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.ALT_MASK | KeyEvent.SHIFT_MASK, true));
 		subMenu.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JMenuItem(page.getAction(Page.INSERT_COMPONENT));
 		menuItem.setName("Script Console");
 		s = getInternationalText("InsertScriptConsole");
 		menuItem.setText((s != null ? s : menuItem.getName()) + "...");
 		menuItem.setMnemonic(KeyEvent.VK_S);
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.ALT_MASK | KeyEvent.SHIFT_MASK, true));
 		subMenu.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		s = getInternationalText("InsertSpecialController");
 		subMenu = new JMenu(s != null ? s : "Special Controller for Model");
 		subMenu.setMnemonic(KeyEvent.VK_E);
 		menu.add(subMenu);
 		menu.addSeparator();
 
 		s = getInternationalText("InsertChemicalReactionKinetics");
 		JMenu subMenu2 = new JMenu(s != null ? s : "Chemical Reaction Kinetics");
 		subMenu2.setMnemonic(KeyEvent.VK_C);
 		subMenu.add(subMenu2);
 
 		menuItem = new JMenuItem(page.getAction(Page.INSERT_COMPONENT));
 		menuItem.setMnemonic(KeyEvent.VK_B);
 		menuItem.setName("Bond-Breaking Barrier");
 		s = getInternationalText("InsertBondBreakingBarrier");
 		menuItem.setText((s != null ? s : menuItem.getName()) + "...");
 		subMenu2.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JMenuItem(page.getAction(Page.INSERT_COMPONENT));
 		menuItem.setMnemonic(KeyEvent.VK_A);
 		menuItem.setName("Activation Barrier");
 		s = getInternationalText("InsertActivationBarrier");
 		menuItem.setText((s != null ? s : menuItem.getName()) + "...");
 		subMenu2.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		s = getInternationalText("InsertProteinAndDNA");
 		subMenu2 = new JMenu(s != null ? s : "Proteins and DNA");
 		subMenu2.setMnemonic(KeyEvent.VK_P);
 		subMenu.add(subMenu2);
 
 		menuItem = new JMenuItem(page.getAction(Page.INSERT_COMPONENT));
 		menuItem.setMnemonic(KeyEvent.VK_S);
 		menuItem.setName("DNA Scroller");
 		s = getInternationalText("InsertDNAScroller");
 		menuItem.setText((s != null ? s : menuItem.getName()) + "...");
 		subMenu2.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		s = getInternationalText("InsertLightMatterInteraction");
 		subMenu2 = new JMenu(s != null ? s : "Light-Matter Interactions");
 		subMenu2.setMnemonic(KeyEvent.VK_L);
 		subMenu.add(subMenu2);
 
 		menuItem = new JMenuItem(page.getAction(Page.INSERT_COMPONENT));
 		menuItem.setMnemonic(KeyEvent.VK_E);
 		menuItem.setName("Electronic Structure");
 		s = getInternationalText("InsertElectronicStructure");
 		menuItem.setText((s != null ? s : menuItem.getName()) + "...");
 		subMenu2.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JMenuItem(page.getAction("Hyperlink"));
 		menuItem.setMnemonic(KeyEvent.VK_K);
 		menuItem.setIcon(null);
 		s = getInternationalText("InsertHyperlink");
 		menuItem.setText((s != null ? s : "Hyperlink") + "...");
 		menu.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JMenuItem(page.getAction(Page.INSERT_COMPONENT));
 		menuItem.setName("Activity Button");
 		s = getInternationalText("InsertActivityButton");
 		menuItem.setText((s != null ? s : menuItem.getName()) + "...");
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.ALT_MASK | KeyEvent.SHIFT_MASK, true));
 		menu.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JMenuItem(page.getAction(Page.INSERT_COMPONENT));
 		menuItem.setName("Multiple Choice");
 		s = getInternationalText("InsertMultipleChoice");
 		menuItem.setText((s != null ? s : menuItem.getName()) + "...");
 		menuItem.setMnemonic(KeyEvent.VK_U);
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.ALT_MASK | KeyEvent.SHIFT_MASK, true));
 		menu.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JMenuItem(page.getAction(Page.INSERT_COMPONENT));
 		menuItem.setName("Image Question");
 		s = getInternationalText("InsertImageQuestion");
 		menuItem.setText((s != null ? s : menuItem.getName()) + "...");
 		menuItem.setMnemonic(KeyEvent.VK_Q);
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.ALT_MASK | KeyEvent.SHIFT_MASK, true));
 		menu.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JMenuItem(page.getAction(Page.INSERT_COMPONENT));
 		menuItem.setName("User Input Text Field");
 		s = getInternationalText("InsertTextField");
 		menuItem.setText((s != null ? s : menuItem.getName()) + "...");
 		menuItem.setMnemonic(KeyEvent.VK_F);
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.ALT_MASK | KeyEvent.SHIFT_MASK, true));
 		menu.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JMenuItem(page.getAction(Page.INSERT_COMPONENT));
 		menuItem.setName("User Input Text Area");
 		s = getInternationalText("InsertTextArea");
 		menuItem.setText((s != null ? s : menuItem.getName()) + "...");
 		menuItem.setMnemonic(KeyEvent.VK_A);
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.ALT_MASK | KeyEvent.SHIFT_MASK, true));
 		menu.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JMenuItem(page.getAction(Page.INSERT_COMPONENT));
 		menuItem.setName("Text Box");
 		s = getInternationalText("InsertTextBox");
 		menuItem.setText((s != null ? s : menuItem.getName()) + "...");
 		menuItem.setMnemonic(KeyEvent.VK_B);
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.ALT_MASK | KeyEvent.SHIFT_MASK, true));
 		menu.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JMenuItem(page.getAction("Color Bar"));
 		s = getInternationalText("ColorBar");
 		menuItem.setText((s != null ? s : "Color Bar") + "...");
 		menuItem.setMnemonic(KeyEvent.VK_H);
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.ALT_MASK | KeyEvent.SHIFT_MASK, true));
 		menu.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JMenuItem(page.getAction(Page.INSERT_COMPONENT));
 		menuItem.setName("Table");
 		s = getInternationalText("InsertTable");
 		menuItem.setText((s != null ? s : menuItem.getName()) + "...");
 		menuItem.setMnemonic(KeyEvent.VK_L);
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.ALT_MASK | KeyEvent.SHIFT_MASK, true));
 		menu.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JMenuItem(page.getAction(Page.INSERT_COMPONENT));
 		menuItem.setName("Applet");
 		s = getInternationalText("Applet");
 		menuItem.setText((s != null ? s + " (Applet)" : "Applet") + "...");
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.ALT_MASK | KeyEvent.SHIFT_MASK, true));
 		menu.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JMenuItem(page.getAction(Page.INSERT_COMPONENT));
 		menuItem.setName("Plugin");
 		s = getInternationalText("Plugin");
 		menuItem.setText((s != null ? s : "Plugin") + "...");
 		menu.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JMenuItem(page.getAction(Page.INSERT_COMPONENT));
 		menuItem.setName("Audio Player");
 		s = getInternationalText("InsertAudioPlayer");
 		menuItem.setText((s != null ? s : "Audio Player") + "...");
 		menu.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 		menu.addSeparator();
 
 		insertSymbolMenuItem = new JMenuItem(page.getAction("Symbol (Unicode Only)"));
 		insertSymbolMenuItem.setIcon(null);
 		s = getInternationalText("InsertSymbol");
 		insertSymbolMenuItem.setText((s != null ? s : "Symbol (Unicode Only)") + "...");
 		menu.add(insertSymbolMenuItem);
 		editor.addEnabledComponentWhenEditable(insertSymbolMenuItem);
 
 		s = getInternationalText("InsertMiscComponent");
 		subMenu = new JMenu(s != null ? s : "Miscellaneous Components");
 		subMenu.setMnemonic(KeyEvent.VK_T);
 		menu.add(subMenu);
 
 		menuItem = new JMenuItem(page.getAction(Page.INSERT_COMPONENT));
 		menuItem.setName("Periodic Table");
 		s = getInternationalText("PeriodicTable");
 		menuItem.setText((s != null ? s : menuItem.getName()) + "...");
 		menuItem.setMnemonic(KeyEvent.VK_P);
 		menuItem.setIcon(null);
 		subMenu.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JMenuItem(page.getAction(Page.INSERT_COMPONENT));
 		menuItem.setName("Function Graph");
 		s = getInternationalText("FunctionGraph");
 		menuItem.setText((s != null ? s : menuItem.getName()) + "...");
 		menuItem.setMnemonic(KeyEvent.VK_F);
 		menuItem.setIcon(null);
 		subMenu.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 		subMenu.addSeparator();
 
 		menuItem = new JMenuItem(page.getAction(Page.INSERT_COMPONENT));
 		menuItem.setName("Feedback Area");
 		s = getInternationalText("InsertFeedbackArea");
 		menuItem.setText((s != null ? s : menuItem.getName()) + "...");
 		subMenu.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JMenuItem(page.getAction(Page.INSERT_COMPONENT));
 		menuItem.setName("Database Search Text Field");
 		s = getInternationalText("InsertDatabaseSearchField");
 		menuItem.setText((s != null ? s : menuItem.getName()) + "...");
 		subMenu.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		// create the View Menu
 
 		s = getInternationalText("View");
 		menu = new JMenu(s != null ? s : "View");
 		menu.setMnemonic(KeyEvent.VK_V);
 		menuBar.add(menu);
 		if (windowCount == 0)
 			menu.setEnabled(false);
 		editor.addDisabledComponentWhileLoading(menu);
 
 		menuItem = menu.add(page.getActionMap().get("Full Screen"));
 		s = getInternationalText("FullScreen");
 		if (s != null)
 			menuItem.setText(s);
 		editor.addEnabledComponentWhenNotEditable(menuItem);
 
 		s = getInternationalText("PageSource");
 		menuItem = new JMenuItem((s != null ? s : "Page Source") + "...");
 		menuItem.setMnemonic(KeyEvent.VK_O);
 		menuItem.setAccelerator(IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.ALT_MASK | KeyEvent.META_MASK,
 				true) : KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_MASK, true));
 		menuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				ExternalClient.open(ExternalClient.HTML_CLIENT, page.getAddress());
 			}
 		});
 		menu.add(menuItem);
 		menu.addSeparator();
 
 		s = getInternationalText("Goto");
 		subMenu = new JMenu(s != null ? s : "Go to");
 		subMenu.setMnemonic(KeyEvent.VK_G);
 		menu.add(subMenu);
 
 		backMenuItem = new JMenuItem(page.getActionMap().get(Navigator.BACK));
 		backMenuItem.setMnemonic(KeyEvent.VK_B);
 		backMenuItem.setIcon(null);
 		s = getInternationalText("Back");
 		if (s != null)
 			backMenuItem.setText(s);
 		subMenu.add(backMenuItem);
 		editor.addEnabledComponentWhenNotEditable(backMenuItem);
 
 		forwardMenuItem = new JMenuItem(page.getActionMap().get(Navigator.FORWARD));
 		forwardMenuItem.setMnemonic(KeyEvent.VK_F);
 		forwardMenuItem.setIcon(null);
 		s = getInternationalText("Forward");
 		if (s != null)
 			forwardMenuItem.setText(s);
 		subMenu.add(forwardMenuItem);
 		editor.addEnabledComponentWhenNotEditable(forwardMenuItem);
 		subMenu.addSeparator();
 
 		homeMenuItem = new JMenuItem(page.getActionMap().get(Navigator.HOME));
 		homeMenuItem.setMnemonic(KeyEvent.VK_H);
 		homeMenuItem.setIcon(null);
 		s = getInternationalText("Home");
 		if (s != null)
 			homeMenuItem.setText(s);
 		editor.addEnabledComponentWhenNotEditable(homeMenuItem);
 		subMenu.add(homeMenuItem);
 
 		reloadMenuItem = menu.add(page.getAction(Page.REFRESH));
 		s = getInternationalText("Reload");
 		if (s != null)
 			reloadMenuItem.setText(s);
 		reloadMenuItem.setIcon(null);
 		menu.add(reloadMenuItem);
 		editor.addEnabledComponentWhenNotEditable(reloadMenuItem);
 
 		menu.addSeparator();
 
 		s = getInternationalText("TextSize");
 		subMenu = new JMenu(s != null ? s : "Text Size");
 		subMenu.setMnemonic(KeyEvent.VK_T);
 		menu.add(subMenu);
 
 		s = getInternationalText("IncreaseFont");
 		menuItem = new JMenuItem(s != null ? s : "Increase");
 		menuItem.setMnemonic(KeyEvent.VK_I);
 		menuItem.setAccelerator(IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, KeyEvent.META_MASK
 				| KeyEvent.ALT_MASK, true) : KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, KeyEvent.CTRL_MASK
 				| KeyEvent.ALT_MASK, true));
 		menuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				FontSizeChanger.step(page, 1);
 			}
 		});
 		subMenu.add(menuItem);
 		editor.addEnabledComponentWhenNotEditable(menuItem);
 
 		s = getInternationalText("DecreaseFont");
 		menuItem = new JMenuItem(s != null ? s : "Decrease");
 		menuItem.setMnemonic(KeyEvent.VK_D);
 		menuItem.setAccelerator(IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, KeyEvent.META_MASK
 				| KeyEvent.ALT_MASK, true) : KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, KeyEvent.CTRL_MASK
 				| KeyEvent.ALT_MASK, true));
 		menuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				FontSizeChanger.step(page, -1);
 			}
 		});
 		subMenu.add(menuItem);
 		editor.addEnabledComponentWhenNotEditable(menuItem);
 		subMenu.addSeparator();
 
 		s = getInternationalText("ActualSize");
 		menuItem = new JMenuItem(s != null ? s : "Actual Size");
 		menuItem.setMnemonic(KeyEvent.VK_A);
 		menuItem.setAccelerator(IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, KeyEvent.META_MASK
 				| KeyEvent.ALT_MASK, true) : KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, KeyEvent.CTRL_MASK
 				| KeyEvent.ALT_MASK, true));
 		menuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				FontSizeChanger.step(page, -page.getFontIncrement());
 			}
 		});
 		subMenu.add(menuItem);
 		editor.addEnabledComponentWhenNotEditable(menuItem);
 
 		// create the Option Menu
 
 		s = getInternationalText("Option");
 		menu = new JMenu(s != null ? s : "Options");
 		menu.setMnemonic(KeyEvent.VK_O);
 		menuBar.add(menu);
 		if (windowCount == 0)
 			menu.setEnabled(false);
 		editor.addDisabledComponentWhileLoading(menu);
 
 		menuItem = new JMenuItem(page.getAction("Font"));
 		s = getInternationalText("Font");
 		menuItem.setText((s != null ? s : "Font") + "...");
 		menuItem.setIcon(null);
 		menuItem.setMnemonic(KeyEvent.VK_F);
 		menu.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JMenuItem(page.getAction("Paragraph"));
 		s = getInternationalText("Paragraph");
 		menuItem.setText((s != null ? s : "Paragraph") + "...");
 		menuItem.setIcon(null);
 		menuItem.setMnemonic(KeyEvent.VK_P);
 		menu.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 
 		menuItem = new JMenuItem(page.getAction("Bullet"));
 		s = getInternationalText("Bullet");
 		menuItem.setText((s != null ? s : "Bullet") + "...");
 		menuItem.setIcon(null);
 		menuItem.setMnemonic(KeyEvent.VK_B);
 		menu.add(menuItem);
 		editor.addEnabledComponentWhenEditable(menuItem);
 		menu.addSeparator();
 
 		s = getInternationalText("Background");
 		colorMenu = new ColorMenu(editor, s != null ? s : "Background", ModelerUtilities.colorChooser,
 				ModelerUtilities.fillEffectChooser);
 		colorMenu.getPopupMenu().addPopupMenuListener(new PopupMenuListener() {
 			public void popupMenuCanceled(PopupMenuEvent e) {
 			}
 
 			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
 			}
 
 			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
 				colorMenu.setColor(page.getBackground());
 			}
 		});
 		colorMenu.addNoFillListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				page.changeFillMode(FillMode.getNoFillMode());
 			}
 		});
 		colorMenu.addColorArrayListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				page.changeFillMode(new FillMode.ColorFill(colorMenu.getColor()));
 			}
 		});
 		colorMenu.addMoreColorListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				Color c = colorMenu.getColorChooser().getColor();
 				page.changeFillMode(new FillMode.ColorFill(c));
 			}
 		});
 		colorMenu.addHexColorListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				Color c = colorMenu
 						.getHexInputColor(page.getFillMode() instanceof FillMode.ColorFill ? ((FillMode.ColorFill) page
 								.getFillMode()).getColor() : null);
 				if (c == null)
 					return;
 				page.changeFillMode(new FillMode.ColorFill(c));
 			}
 		});
 		colorMenu.addFillEffectListeners(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				page.changeFillMode(colorMenu.getFillEffectChooser().getFillMode());
 			}
 		}, null);
 		colorMenu.setMnemonic(KeyEvent.VK_G);
 		menu.add(colorMenu);
 		editor.addEnabledComponentWhenEditable(colorMenu);
 
 		s = getInternationalText("Theme");
 		subMenu = new JMenu(s != null ? s : "Apply Theme");
 		subMenu.setMnemonic(KeyEvent.VK_T);
 		menu.add(subMenu);
 
 		ItemListener il = new ItemListener() {
 			public void itemStateChanged(ItemEvent e) {
 				if (e.getStateChange() == ItemEvent.SELECTED) {
 					JOptionPane.showMessageDialog(Modeler.this, NAME + " has to restart for the selected\n"
 							+ "theme to display properly.\n\n"
 							+ "(If it does not restart automatically, please click\n" + "to restart it.)",
 							"Theme change", JOptionPane.INFORMATION_MESSAGE);
 					lookandfeel = ((AbstractButton) e.getSource()).getText();
 					restart = true;
 					if (launchedByJWS) {
 						navigator.visitLocation(Initializer.sharedInstance().getResetJnlpAddress());
 					}
 					else {
 						Modeler.this.dispatchEvent(new WindowEvent(Modeler.this, WindowEvent.WINDOW_CLOSING));
 					}
 				}
 			}
 		};
 
 		ButtonGroup bg = new ButtonGroup();
 
 		String lfName;
 		for (int i = 0; i < lf.length; i++) {
 			lfName = lf[i].getName();
 			menuItem = new JRadioButtonMenuItem(lfName);
 			menuItem.setSelected(lfName.equals(lookandfeel));
 			menuItem.addItemListener(il);
 			subMenu.add(menuItem);
 			bg.add(menuItem);
 		}
 
 		s = getInternationalText("Precache");
 		preinstallMenu = new JMenu(s != null ? s : "Prefetch to Cache");
 		preinstallMenu.setMnemonic(KeyEvent.VK_P);
 		Zipper.sharedInstance().addComponentToLock(preinstallMenu);
 		menu.add(preinstallMenu);
 
 		for (int i = 0; i < precacheFiles.length; i += 2) {
 			menuItem = new JMenuItem(precacheFiles[i + 1]);
 			final int i1 = i;
 			menuItem.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					editor.precache(precacheFiles[i1], " Fetching " + precacheFiles[i1 + 1] + "......");
 				}
 			});
 			preinstallMenu.add(menuItem);
 		}
 		preinstallMenu.addSeparator();
 		menuItem = new JMenuItem("All Packages Above");
 		menuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				int n = precacheFiles.length / 2;
 				String[] address = new String[n];
 				String[] message = new String[n];
 				for (int i = 0; i < n; i++) {
 					address[i] = precacheFiles[i + i];
 					message[i] = precacheFiles[i + i + 1];
 				}
 				editor.precache(address, message);
 			}
 		});
 		preinstallMenu.add(menuItem);
 
 		if (!IS_MAC) {
 			menu.addSeparator();
 			s = getInternationalText("Preference");
 			menuItem = new JMenuItem((s != null ? s : "Preferences") + "...");
 			menuItem.setMnemonic(KeyEvent.VK_R);
 			menuItem.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					if (preferencesDialog == null)
 						preferencesDialog = new PreferencesDialog(Modeler.this);
 					preferencesDialog.setPreferences(preference);
 					preferencesDialog.setVisible(true);
 				}
 			});
 			menu.add(menuItem);
 		}
 
 		menuBar.add(bookmarkMenu);
 		editor.addEnabledComponentWhenNotEditable(bookmarkMenu);
 		if (windowCount == 0)
 			bookmarkMenu.setEnabled(false);
 		editor.addDisabledComponentWhileLoading(bookmarkMenu);
 
 		// create the remote menu
 
 		s = getInternationalText("SubmitCurrentPage");
 		final JMenuItem uploadMI = new JMenuItem((s != null ? s : "Submit Current Page") + "...");
 		s = getInternationalText("SubmitCurrentFolder");
 		final JMenuItem uploadCurrentFolderMI = new JMenuItem((s != null ? s : "Submit Current Activity Folder")
 				+ "...");
 		final JMenuItem commentMI = new JMenuItem();
 		final JMenuItem viewCommentMI = new JMenuItem();
 
 		s = getInternationalText("Webspace");
 		menu = new JMenu(s != null ? s : "Webspace");
 		menu.setMnemonic(KeyEvent.VK_C);
 		menu.getPopupMenu().addPopupMenuListener(new PopupMenuListener() {
 			public void popupMenuCanceled(PopupMenuEvent e) {
 			}
 
 			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
 			}
 
 			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
 				if (IS_MAC)
 					return;
 				boolean isRemote = page.isRemote();
 				uploadMI.setEnabled(!isRemote);
 				uploadCurrentFolderMI.setEnabled(!isRemote);
 				commentMI.setEnabled(isRemote);
 				viewCommentMI.setEnabled(isRemote);
 			}
 		});
 		menuBar.add(menu);
 		if (windowCount == 0)
 			menu.setEnabled(false);
 		editor.addDisabledComponentWhileLoading(menu);
 
 		uploadMI.setMnemonic(KeyEvent.VK_P);
 		uploadMI.addActionListener(serverGate.uploadAction);
 		menu.add(uploadMI);
 		if (IS_MAC)
 			navigator.addEnabledComponentWhenLocal(uploadMI);
 
 		uploadCurrentFolderMI.setMnemonic(KeyEvent.VK_F);
 		uploadCurrentFolderMI.addActionListener(serverGate.uploadCurrentFolderAction);
 		menu.add(uploadCurrentFolderMI);
 		if (IS_MAC)
 			navigator.addEnabledComponentWhenLocal(uploadCurrentFolderMI);
 		menu.addSeparator();
 
 		s = getInternationalText("MyMwSpace");
 		menuItem = new JMenuItem(s != null ? s : "My MW Space");
 		menuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				navigator.visitLocation(getContextRoot() + "myhome.jsp?client=mw");
 			}
 		});
 		menu.add(menuItem);
 		editor.addEnabledComponentWhenNotEditable(menuItem);
 
 		s = getInternationalText("MyModels");
 		menuItem = new JMenuItem(s != null ? s : "My Models");
 		menuItem.setMnemonic(KeyEvent.VK_V);
 		menuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (user.getUserID() != null && !user.getUserID().trim().equals("")) {
 					navigator.visitLocation(getMyModelSpaceAddress());
 				}
 				else {
 					navigator.visitLocation(getContextRoot() + "modelspace.jsp?client=mw");
 				}
 			}
 		});
 		menu.add(menuItem);
 		editor.addEnabledComponentWhenNotEditable(menuItem);
 
 		s = getInternationalText("MyReports");
 		menuItem = new JMenuItem(s != null ? s : "My Reports");
 		menuItem.setMnemonic(KeyEvent.VK_R);
 		menuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				navigator.visitLocation(getMyReportAddress());
 			}
 		});
 		menu.add(menuItem);
 		editor.addEnabledComponentWhenNotEditable(menuItem);
 		menu.addSeparator();
 
 		commentMI.setAction(serverGate.commentAction);
 		s = getInternationalText("MakeComments");
 		commentMI.setText((s != null ? s : "Make Comments") + "...");
 		menu.add(commentMI);
 		if (IS_MAC)
 			navigator.addEnabledComponentWhenRemote(commentMI);
 
 		viewCommentMI.setAction(serverGate.viewCommentAction);
 		s = getInternationalText("ViewComments");
 		viewCommentMI.setText((s != null ? s : "View Discussion about Current Page") + "...");
 		menu.add(viewCommentMI);
 		if (IS_MAC)
 			navigator.addEnabledComponentWhenRemote(viewCommentMI);
 
 		// create the Help Menu
 
 		s = getInternationalText("Help");
 		menu = new JMenu(s != null ? s : "Help");
 		menu.setMnemonic(KeyEvent.VK_H);
 		menuBar.add(menu);
 		if (windowCount == 0)
 			menu.setEnabled(false);
 		editor.addDisabledComponentWhileLoading(menu);
 
 		s = getInternationalText("UserManual");
 		menuItem = new JMenuItem(s != null ? s : "Online User's Manual");
 		menuItem.setMnemonic(KeyEvent.VK_O);
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0, true));
 		menuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				navigator.visitLocation(navigator.getHomeDirectory() + "tutorial/index.cml");
 			}
 		});
 		menu.add(menuItem);
 		editor.addEnabledComponentWhenNotEditable(menuItem);
 
 		s = getInternationalText("OpenUserManualInNewWindow");
 		menuItem = new JMenuItem(s != null ? s : "Open Online User's Manual in a New Window");
 		menuItem.setMnemonic(KeyEvent.VK_O);
 		menuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				openPageInNewWindow(navigator.getHomeDirectory() + "tutorial/index.cml");
 			}
 		});
 		menu.add(menuItem);
 
 		s = getInternationalText("KeyboardShortcuts");
 		menuItem = new JMenuItem(s != null ? s : "Keyboard Shortcuts");
 		menuItem.setMnemonic(KeyEvent.VK_K);
 		menuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				String s = navigator.getHomeDirectory();
 				if (Locale.getDefault().equals(Locale.PRC)) {
 					s += "cn/manual/keyboard.cml";
 				}
 				else if (Locale.getDefault().equals(Locale.TAIWAN)) {
 					s += "tw/manual/keyboard.cml";
 				}
 				else {
 					s += "tutorial/keyboard.cml";
 				}
 				navigator.visitLocation(s);
 			}
 		});
 		menu.add(menuItem);
 		editor.addEnabledComponentWhenNotEditable(menuItem);
 
 		s = getInternationalText("SupportedMathFunctions");
 		menuItem = new JMenuItem(s != null ? s : "Supported Math Functions");
 		menuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				navigator.visitLocation(navigator.getHomeDirectory() + "tutorial/functions.cml");
 			}
 		});
 		menu.add(menuItem);
 		editor.addEnabledComponentWhenNotEditable(menuItem);
 		menu.addSeparator();
 
 		s = getInternationalText("Feedback");
 		menuItem = new JMenuItem(s != null ? s : "Send Feedback");
 		menuItem.setMnemonic(KeyEvent.VK_F);
 		menuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				navigator.visitLocation(getContextRoot() + "contact.jsp?client=mw");
 			}
 		});
 		menu.add(menuItem);
 		editor.addEnabledComponentWhenNotEditable(menuItem);
 
 		s = getInternationalText("UninstallMWViaJavaCacheViewer");
 		menuItem = new JMenuItem((s != null ? s : "Uninstall Molecular Workbench via Java Cache Viewer") + "...");
 		menuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					Runtime.getRuntime().exec("javaws -viewer");
 				}
 				catch (IOException e1) {
 					e1.printStackTrace();
 				}
 				System.exit(0);
 			}
 		});
 		menu.add(menuItem);
 
 		if (launchedByJWS || "yes".equals(System.getProperty("mw.launcher"))) {
 			s = getInternationalText("ResetDesktopLauncher");
 			menuItem = new JMenuItem(s != null ? s : "Reset Desktop Launcher");
 			menuItem.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					navigator.visitLocation(Initializer.sharedInstance().getResetJnlpAddress());
 				}
 			});
 			menu.add(menuItem);
 		}
 		menu.addSeparator();
 
 		if (!IS_MAC) {
 			s = getInternationalText("About");
 			menuItem = new JMenuItem(s != null ? s : "About Molecular Workbench");
 			menuItem.setMnemonic(KeyEvent.VK_A);
 			menuItem.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					navigator.visitLocation(navigator.getHomeDirectory() + "about.cml");
 				}
 			});
 			menu.add(menuItem);
 			editor.addEnabledComponentWhenNotEditable(menuItem);
 		}
 
 		s = getInternationalText("SystemInfo");
 		menuItem = new JMenuItem((s != null ? s : "System Information") + "...");
 		menuItem.setMnemonic(KeyEvent.VK_S);
 		menuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				new SystemInfoDialog(Modeler.this).setVisible(true);
 			}
 		});
 		menu.add(menuItem);
 
 	}
 
 	void createBookmarkMenu() {
 
 		String s = getInternationalText("Bookmark");
 		bookmarkMenu = new JMenu(s != null ? s : "Bookmarks");
 		bookmarkMenu.setMnemonic(KeyEvent.VK_B);
 
 		s = getInternationalText("AddBookmark");
 		JMenuItem menuItem = new JMenuItem(s != null ? s : "Add Bookmark");
 		menuItem.setMnemonic(KeyEvent.VK_A);
 		menuItem.setAccelerator(IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.META_MASK, true) : KeyStroke
 				.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_MASK, true));
 		menuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				int opt = JOptionPane.YES_OPTION;
 				if (BookmarkManager.sharedInstance().getBookmarks().containsKey(editor.getTitle())) {
 					opt = JOptionPane.showConfirmDialog(Modeler.this, "There is already a bookmark with the name: "
 							+ editor.getTitle() + "\nWould you like to overwrite it?", "Bookmark name exists",
 							JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
 				}
 				if (opt == JOptionPane.YES_OPTION) {
 					BookmarkManager.sharedInstance().getBookmarks().put(
 							editor.getTitle() == null ? editor.getAddress() : editor.getTitle(), editor.getAddress());
 					updateBookmarks();
 				}
 			}
 		});
 		bookmarkMenu.add(menuItem);
 
 		s = getInternationalText("ManageBookmark");
 		menuItem = new JMenuItem((s != null ? s : "Manage Bookmarks") + "...");
 		menuItem.setMnemonic(KeyEvent.VK_M);
 		menuItem.setAccelerator(IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.META_MASK | KeyEvent.ALT_MASK,
 				true) : KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_MASK | KeyEvent.ALT_MASK, true));
 		menuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				BookmarkManager.sharedInstance().showDialog(Modeler.this);
 			}
 		});
 		bookmarkMenu.add(menuItem);
 		bookmarkMenu.addSeparator();
 
 		s = getInternationalText("MolecularWorkbenchHome");
 		menuItem = new JMenuItem(s != null ? s : NAME + " Home");
 		menuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				editor.getPage().rememberViewPosition(false);
 				String s = getStaticRoot();
 				Locale l = Locale.getDefault();
 				if (l.equals(Locale.PRC))
 					navigator.visitLocation(s + "cn/index.cml");
 				else if (l.equals(Locale.TAIWAN))
 					navigator.visitLocation(s + "tw/index.cml");
 				else {
 					if (l.getLanguage().equals("ru")) {
 						navigator.visitLocation(s + "ru/index.cml");
 					}
 					else {
 						navigator.visitLocation(s + "index.cml");
 					}
 				}
 			}
 		});
 		bookmarkMenu.add(menuItem);
 
 		updateBookmarks();
 
 	}
 
 	void updateBookmarks() {
 		while (bookmarkMenu.getItemCount() > 4)
 			bookmarkMenu.remove(4);
 		JMenuItem menuItem = null;
 		for (String key : BookmarkManager.sharedInstance().getBookmarks().keySet()) {
 			final String url = BookmarkManager.sharedInstance().getBookmarks().get(key);
 			menuItem = new JMenuItem(key);
 			menuItem.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					editor.getPage().rememberViewPosition(false);
 					navigator.visitLocation(url);
 				}
 			});
 			bookmarkMenu.add(menuItem);
 		}
 	}
 
 	void createToolBar() {
 
 		toolBar = new JToolBar();
 		toolBar.setMargin(new Insets(1, 1, 1, 1));
 		toolBar.setBorder(BorderFactory.createEtchedBorder());
 		toolBar.setFloatable(false);
 		toolBar.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 1));
 		toolBar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
 		if (!IS_MAC)
 			toolBar.add(new JLabel(toolBarHeaderIcon));
 
 		Dimension dim = new Dimension(24, 24);
 
 		String s;
 		backButton = new JButton(navigator.getAction(Navigator.BACK));
 		backButton.setHorizontalAlignment(SwingConstants.CENTER);
 		if (showToolBarText) {
 			backButton.setMargin(Editor.ZERO_INSETS);
 			int w = backButton.getFontMetrics(backButton.getFont()).stringWidth(backButton.getText());
 			w += backButton.getIconTextGap();
 			w += backButton.getIcon().getIconWidth();
 			backButton.setPreferredSize(new Dimension(w + 10, dim.height));
 			s = getInternationalText("BackButton");
 			if (s != null)
 				backButton.setText(s);
 		}
 		else {
 			backButton.setText(null);
 			backButton.setPreferredSize(dim);
 			if ("CDE/Motif".equals(lookandfeel))
 				backButton.setMargin(Editor.ZERO_INSETS);
 		}
 		if (!IS_MAC) {
 			backButton.setBorderPainted(false);
 			backButton.setFocusPainted(false);
 		}
 		toolBar.add(backButton);
 		editor.addDisabledComponentWhileLoading(backButton);
 
 		forwardButton = new JButton(navigator.getAction(Navigator.FORWARD));
 		forwardButton.setHorizontalAlignment(SwingConstants.CENTER);
 		forwardButton.setText(null);
 		forwardButton.setPreferredSize(dim);
 		if (!IS_MAC) {
 			forwardButton.setBorderPainted(false);
 			forwardButton.setFocusPainted(false);
 		}
 		if ("CDE/Motif".equals(lookandfeel))
 			forwardButton.setMargin(Editor.ZERO_INSETS);
 		toolBar.add(forwardButton);
 		editor.addDisabledComponentWhileLoading(forwardButton);
 
 		homeButton = new JButton(navigator.getAction(Navigator.HOME));
 		homeButton.setHorizontalAlignment(SwingConstants.CENTER);
 		homeButton.setText(null);
 		homeButton.setPreferredSize(dim);
 		if (!IS_MAC) {
 			homeButton.setBorderPainted(false);
 			homeButton.setFocusPainted(false);
 		}
 		if ("CDE/Motif".equals(lookandfeel))
 			homeButton.setMargin(Editor.ZERO_INSETS);
 		toolBar.add(homeButton);
 		editor.addEnabledComponentWhenNotEditable(homeButton);
 		if (windowCount == 0)
 			homeButton.setEnabled(false);
 		editor.addDisabledComponentWhileLoading(homeButton);
 
 		reloadButton = new JButton(editor.getPage().getAction(Page.REFRESH));
 		reloadButton.setHorizontalAlignment(SwingConstants.CENTER);
 		if (showToolBarText) {
 			reloadButton.setMargin(Editor.ZERO_INSETS);
 			int w = reloadButton.getFontMetrics(reloadButton.getFont()).stringWidth(reloadButton.getText());
 			w += reloadButton.getIconTextGap();
 			w += reloadButton.getIcon().getIconWidth();
 			reloadButton.setPreferredSize(new Dimension(w + 10, dim.height));
 			s = getInternationalText("ReloadButton");
 			if (s != null)
 				reloadButton.setText(s);
 		}
 		else {
 			reloadButton.setText(null);
 			reloadButton.setPreferredSize(dim);
 			if ("CDE/Motif".equals(lookandfeel))
 				reloadButton.setMargin(Editor.ZERO_INSETS);
 		}
 		if (!IS_MAC) {
 			reloadButton.setBorderPainted(false);
 			reloadButton.setFocusPainted(false);
 		}
 		toolBar.add(reloadButton);
 		editor.addEnabledComponentWhenNotEditable(reloadButton);
 		if (windowCount == 0)
 			reloadButton.setEnabled(false);
 		editor.addDisabledComponentWhileLoading(reloadButton);
 
 		navigator.getComboBox().setRequestFocusEnabled(false);
 		int fontSize = navigator.getComboBox().getFont().getSize();
 		navigator.getComboBox().setPreferredSize(new Dimension(400, fontSize * 2));
 		toolBar.add(navigator.getComboBox());
 		if (windowCount == 0)
 			navigator.getComboBox().setEnabled(false);
 		editor.addDisabledComponentWhileLoading(navigator.getComboBox());
 
 	}
 
 	/** side effect of implementing an interface */
 	public void bookmarkUpdated(BookmarkEvent e) {
 		if (e.getID() == BookmarkEvent.BOOKMARK_SORTED) {
 			EventQueue.invokeLater(new Runnable() {
 				public void run() {
 					updateBookmarks();
 				}
 			});
 		}
 	}
 
 	/** side effect of implementing an interface */
 	public void editorEnabled(EditorEvent e) {
 		if (!EventQueue.isDispatchThread())
 			throw new RuntimeException("This method must be called by the AWT event thread");
 		getContentPane().remove(toolBar);
 	}
 
 	/** side effect of implementing an interface */
 	public void editorDisabled(EditorEvent e) {
 		if (!EventQueue.isDispatchThread())
 			throw new RuntimeException("This method must be called by the AWT event thread");
 		getContentPane().add(toolBar, BorderLayout.NORTH);
 	}
 
 	/** side effect of implementing an interface */
 	public void downloadStarted(DownloadEvent e) {
 	}
 
 	/** side effect of implementing an interface */
 	public void downloadCompleted(DownloadEvent e) {
 		File src = UpdateManager.getPackFile();
 		if (src == null || !src.exists())
 			return;
 		if (UpdateManager.unpack()) {
 			src.deleteOnExit();
 			restart = true;
 			dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
 		}
 	}
 
 	/** side effect of implementing an interface */
 	public void downloadAborted(DownloadEvent e) {
 	}
 
 	/** side effect of implementing an interface */
 	public void pageUpdate(PageEvent e) {
 		switch (e.getType()) {
 		case PageEvent.PAGE_READ_END:
 		case PageEvent.LOAD_ERROR:
 			if (!login) {
 				login();
 				login = true;
 			}
 			break;
 		case PageEvent.OPEN_NEW_WINDOW:
 			final String str = e.getDescription();
 			final Modeler m = getModeler(str);
 			if (m != null) {
 				EventQueue.invokeLater(new Runnable() {
 					public void run() {
 						m.toFront();
 					}
 				});
 			}
 			else {
 				final Object o = e.getProperties();
 				EventQueue.invokeLater(new Runnable() {
 					public void run() {
 						boolean resizable = true;
 						boolean hasToolbar = true;
 						boolean hasMenubar = true;
 						boolean hasStatusbar = true;
 						boolean fullscreen = false;
 						Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 						int w = Initializer.sharedInstance().getPreferences().getInt("Width", screenSize.width - 200);
 						int h = Initializer.sharedInstance().getPreferences().getInt("Height", screenSize.height - 300);
 						xOffset += offset;
 						yOffset += offset;
 						int x = getLocation().x + xOffset;
 						int y = getLocation().y + yOffset;
 						if (o instanceof HyperlinkParameter) {
 							resizable = ((HyperlinkParameter) o).getResizable();
 							hasToolbar = ((HyperlinkParameter) o).getToolbar();
 							hasMenubar = ((HyperlinkParameter) o).getMenubar();
 							hasStatusbar = ((HyperlinkParameter) o).getStatusbar();
 							fullscreen = ((HyperlinkParameter) o).getFullscreen();
 							int x1 = ((HyperlinkParameter) o).getLeft();
 							int y1 = ((HyperlinkParameter) o).getTop();
 							int w1 = ((HyperlinkParameter) o).getWidth();
 							int h1 = ((HyperlinkParameter) o).getHeight();
 							if (w1 > 0)
 								w = w1;
 							if (h1 > 0)
 								h = h1;
 							if (x1 >= 0)
 								x = x1;
 							if (y1 >= 0)
 								y = y1;
 						}
 						Modeler m = openNewWindow(false, editor.getPage().getAddress(), x, y, w, h, resizable,
 								hasToolbar, hasMenubar, hasStatusbar, fullscreen);
 						m.editor.getPage().openHyperlink(str);
 					}
 				});
 			}
 			break;
 		case PageEvent.CLOSE_CURRENT_WINDOW:
 			EventQueue.invokeLater(new Runnable() {
 				public void run() {
 					Modeler.this.dispatchEvent(new WindowEvent(Modeler.this, WindowEvent.WINDOW_CLOSING));
 				}
 			});
 			break;
 		}
 	}
 
 	private Modeler getModeler(String address) {
 		if (windowList == null || windowList.isEmpty())
 			return null;
 		for (Modeler m : windowList) {
 			if (address.equals(m.editor.getPage().getAddress())) {
 				return m;
 			}
 		}
 		return null;
 	}
 
 	private void readFromSocket() throws Exception {
 		clientSocket = serverSocket.accept();
 		socketInputStream = clientSocket.getInputStream();
 		BufferedReader socketReader = new BufferedReader(new InputStreamReader(socketInputStream));
 		String inputLine = null;
 		while ((inputLine = socketReader.readLine()) != null) {
 			if (!stopListening) {
 				if (inputLine.equals("ping"))
 					break;
 				final String inputLine2 = inputLine;
 				EventQueue.invokeLater(new Runnable() {
 					public void run() {
 						navigator.visitLocation(inputLine2);
 						toFront();
 					}
 				});
 			}
 			break;
 		}
 	}
 
 	private void pingSocket(String s) {
 		Socket socket = null;
 		PrintWriter out = null;
 		try {
 			socket = SocketFactory.getDefault().createSocket("localhost", PORT_LAUNCH);
 			out = new PrintWriter(socket.getOutputStream(), true);
 			out.println(s);
 		}
 		catch (Exception e2) {
 			e2.printStackTrace();
 		}
 		finally {
 			if (out != null)
 				out.close();
 			if (socket != null) {
 				try {
 					socket.close();
 				}
 				catch (IOException ex) {
 				}
 			}
 		}
 	}
 
 	private void requestStopListening() {
 		stopListening = true;
 		if (socketInputStream != null) {
 			try {
 				socketInputStream.close();
 			}
 			catch (IOException e) {
 			}
 		}
 		if (clientSocket != null) {
 			try {
 				clientSocket.close();
 			}
 			catch (IOException e) {
 			}
 			pingSocket("ping");
 		}
 	}
 
 	private void listenToSocket() {
 		listeningToSocket = true;
 		if (serverSocket != null) {
 			Thread t = new Thread("Socket Listener") {
 				public void run() {
 					while (!stopListening) {
 						try {
 							readFromSocket();
 						}
 						catch (Exception e) {
 							e.printStackTrace();
 							break;
 						}
 					}
 				}
 			};
 			t.setPriority(Thread.MIN_PRIORITY);
 			t.start();
 		}
 	}
 
 	/*
 	 * at this stage, we can tell that MW has successfully launched. We can register the user information for further
 	 * references.
 	 */
 	private void login() {
 		if (ConnectionManager.sharedInstance().getWorkOffline())
 			return;
 		new SwingWorker("Check-in", Thread.MIN_PRIORITY) {
 			public Object construct() {
 				new Receptionist().checkin();
 				return null;
 			}
 
 			public void finished() {
 				if (!launchedByJWS && !runOnCD && !disableJarUpdate)
 					UpdateManager.showUpdateReminder(Modeler.this);
 				if (!runOnCD)
 					listenToSocket();
 			}
 		}.start();
 	}
 
 	private static void checkMinimumJavaVersion() {
 		final String version = System.getProperty("java.version");
 		if (version.compareTo(MINIMUM_JAVA_VERSION) < 0) {
 			try {
 				EventQueue.invokeAndWait(new Runnable() {
 					public void run() {
 						Initializer.sharedInstance().hideSplashScreen();
 						if (System.getProperty("os.name").startsWith("Windows")) {
 							JOptionPane.showMessageDialog(null, "Your current Java version is " + version
 									+ ". A newer version is needed.\nPlease go to http://java.com to get it.");
 							try {
 								Runtime.getRuntime().exec(new String[] { "explorer", "http://java.com" });
 							}
 							catch (Exception e) {
 							}
 						}
 						else if (System.getProperty("os.name").startsWith("Linux")) {
 							JOptionPane.showMessageDialog(null, "Your current Java version is " + version
 									+ ". A newer version is needed.\nPlease go to http://java.com to get it.");
 						}
 						else if (IS_MAC) {
 							JOptionPane.showMessageDialog(null, "Your current Java version is " + version
 									+ ". A newer version is needed.\nPlease update it.");
 						}
 						System.exit(0);
 					}
 				});
 			}
 			catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public static void setSystemProperty(String key, String value) {
 		System.setProperty(key, value);
 		if ("mw.cd.mode".equals(key))
 			runOnCD = "true".equals(value);
 		else if ("mw.jar.noupdate".equals(key))
 			disableJarUpdate = "true".equals(value);
 		else if ("mw.ui.nosetlaf".equals(key))
 			disableSetLAF = "true".equals(value);
 	}
 
 	private static void setLocale() {
 		userLocale = System.getProperty("mw.locale");
 		if (userLocale == null)
 			userLocale = Initializer.sharedInstance().getPreferences().get("Locale", null);
 		if (userLocale != null) {
 			if ("en_US".equals(userLocale))
 				Locale.setDefault(Locale.US);
 			else if ("zh_CN".equals(userLocale))
 				Locale.setDefault(Locale.CHINA);
 			else if ("zh_TW".equals(userLocale))
 				Locale.setDefault(Locale.TAIWAN);
 			else if (userLocale.startsWith("ru"))
 				Locale.setDefault(new Locale("ru"));
 			else if (userLocale.startsWith("es"))
 				Locale.setDefault(new Locale("es"));
 		}
 	}
 
 	private static void preventMultipleInstances(String[] args) {
 		try {
 			serverSocket = ServerSocketFactory.getDefault().createServerSocket();
 			serverSocket.bind(new InetSocketAddress(PORT_LAUNCH));
 		}
 		catch (Exception e) {
 			if (true) {
 				Socket socket = null;
 				PrintWriter out = null;
 				try {
 					socket = SocketFactory.getDefault().createSocket("localhost", PORT_LAUNCH);
 					out = new PrintWriter(socket.getOutputStream(), true);
 					String str = "";
 					if (args != null && args.length >= 2) {
 						str += args[1];
 					}
 					if (str.length() > 1) {
 						out.println(str);
 					}
 					else {
 						out.println(getStaticRoot() + "index.cml");
 					}
 				}
 				catch (Exception e2) {
 					e2.printStackTrace();
 				}
 				finally {
 					if (out != null)
 						out.close();
 					if (socket != null) {
 						try {
 							socket.close();
 						}
 						catch (IOException ex) {
 						}
 					}
 				}
 			}
 			else {
 				try {
 					EventQueue.invokeAndWait(new Runnable() {
 						public void run() {
 							JOptionPane.showMessageDialog(null, NAME + " is already running.");
 						}
 					});
 				}
 				catch (Exception ex) {
 					ex.printStackTrace();
 				}
 			}
 			System.exit(0);
 		}
 	}
 
 	// customize the main window
 	private static Modeler createModeler() {
 		Modeler m = new Modeler();
 		windowList.add(m);
 		if ("false".equalsIgnoreCase(System.getProperty("mw.window.resizable"))) {
 			m.setResizable(false);
 		}
 		if ("false".equalsIgnoreCase(System.getProperty("mw.window.menubar"))) {
 			m.setJMenuBar(null);
 		}
 		if ("false".equalsIgnoreCase(System.getProperty("mw.window.toolbar"))) {
 			m.getContentPane().remove(m.toolBar);
 			m.editor.removeAllToolBars();
 		}
 		if ("false".equalsIgnoreCase(System.getProperty("mw.window.statusbar"))) {
 			m.getContentPane().remove(m.statusBar);
 		}
 		String hp = System.getProperty("mw.homepage");
 		if (hp != null)
 			m.navigator.setHomePage(hp);
 		int x = -1, y = -1, w = -1, h = -1;
 		boolean parseOK = true;
 		try {
 			String s = System.getProperty("mw.window.width");
 			if (s != null)
 				w = Integer.parseInt(s);
 			s = System.getProperty("mw.window.height");
 			if (s != null)
 				h = Integer.parseInt(s);
 			s = System.getProperty("mw.window.left");
 			if (s != null)
 				x = Integer.parseInt(s);
 			s = System.getProperty("mw.window.top");
 			if (s != null)
 				y = Integer.parseInt(s);
 		}
 		catch (NumberFormatException e) {
 			e.printStackTrace();
 			parseOK = false;
 		}
 		if (parseOK) {
 			if (w > 0 || h > 0) {
 				if (w == -1)
 					w = m.editor.getPreferredSize().width;
 				if (h == -1)
 					h = m.editor.getPreferredSize().height;
 				m.editor.setPreferredSize(new Dimension(w, h));
 				m.statusBar.setPreferredSize(new Dimension(w, 20));
 			}
 			if (x >= 0 || y >= 0) {
 				if (x == -1)
 					x = m.getLocation().x;
 				if (y == -1)
 					y = m.getLocation().y;
 				m.setLocation(x, y);
 			}
 		}
 		return m;
 	}
 
 	private static void warnAboutJar() {
 		String userDir = System.getProperty("user.dir");
 		if (System.getProperty("os.name").startsWith("Windows"))
 			userDir = userDir.replace('\\', '/');
 		ClassLoader cl = Thread.currentThread().getContextClassLoader();
 		String resourceURL = cl.getResource("org").toString();
 		int i = resourceURL.indexOf(userDir);
 		if (i != -1) {
 			String s = resourceURL.substring(i + userDir.length() + 1);
 			if (s.indexOf(".jar") == -1) { // likely in developer mode, disable security manager
 				System.setProperty("mw.nosecurity", "true");
 			}
 			else {
 				if (!s.startsWith("mw.jar") && !s.startsWith("dist/mw.jar")) {
					JOptionPane.showMessageDialog(null,
 							"The file name must be exactly mw.jar in order to run in this mode.", "Security Error",
 							JOptionPane.ERROR_MESSAGE);
 					System.exit(-1);
 				}
 			}
 		}
 		else {
 			if (resourceURL.indexOf("mw.jar") == -1) {
 				JOptionPane
 						.showMessageDialog(
								null,
 								"You cannot run mw.jar directly from a web page. Save it\nto your computer and then double-click on it.",
 								"Security Error", JOptionPane.ERROR_MESSAGE);
 				System.exit(-1);
 			}
 		}
 	}
 
 	public static void main(String[] args) {
 
 		// ModelerUtilities.printSystemProperties();
 
 		checkMinimumJavaVersion();
 
 		// workaround to prevent the app from multiple instances
 		preventMultipleInstances(args);
 
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				Initializer.sharedInstance().showSplashScreen();
 			}
 		});
 
 		// feeding starting page and host flags
 		if (args != null && args.length > 0) {
 			hostIsLocal = "local".equalsIgnoreCase(args[0]);
 			if (args.length > 1) {
 				startingURL = args[1];
 				if (startingURL != null) {
 					if (System.getProperty("os.name").startsWith("Windows") && !FileUtilities.isRemote(startingURL)) {
 						startingURL = startingURL.replace('/', '\\');
 					}
 					if (FileUtilities.isRelative(startingURL)) {
 						startingURL = System.getProperty("user.dir") + System.getProperty("file.separator")
 								+ startingURL;
 					}
 				}
 			}
 		}
 
 		runOnCD = "true".equalsIgnoreCase(System.getProperty("mw.cd.mode"));
 
 		// detect if the app is launched via webstart from its classloader. We use only two classloaders:
 		// The SystemClassLoader and the JnlpClassLoader.
 		ClassLoader cl = Thread.currentThread().getContextClassLoader();
 		if (!cl.equals(ClassLoader.getSystemClassLoader()))
 			launchedByJWS = true;
 
 		// Workaround: Disable the default CookieHandler from Java Web Start, which prevents cookie from
 		// being sent to the server through URLConnection.setRequestProperty
 		if (launchedByJWS) {
 			CookieHandler.setDefault(null);
 		}
 
 		// set locale if detected in the command line
 		setLocale();
 
 		if (IS_MAC) {
 			System.setProperty("apple.laf.useScreenMenuBar", "false".equalsIgnoreCase(System
 					.getProperty("mw.window.menubar")) ? "false" : "true");
 			System.setProperty("apple.awt.brushMetalLook", "true");
 		}
 
 		if (hostIsLocal) {
 			javax.swing.RepaintManager.setCurrentManager(new ThreadCheckingRepaintManager());
 		}
 		else {
 			// warn user if the jar file is launched from untrusted path
 			if (!launchedByJWS)
 				warnAboutJar();
 		}
 
 		Debugger.print("Initialization");
 
 		if (createUsingEDT) {
 
 			EventQueue.invokeLater(new Runnable() {
 				public void run() {
 					Initializer.sharedInstance().setMessage("Testing connection to the main MW server...");
 					ConnectionManager.sharedInstance().setWorkOffline(!ModelerUtilities.pingMwServer());
 					Initializer.init();
 					final Modeler m = createModeler();
 					m.pack();
 					m.setVisible(true);
 					m.toFront();
 					Initializer.sharedInstance().hideSplashScreen();
 				}
 			});
 
 		}
 		else {
 
 			// loading worker thread
 			final SwingWorker worker = new SwingWorker("Main loader", Thread.MIN_PRIORITY) {
 				public Object construct() {
 					Debugger.print("Starting main loader");
 					Initializer.sharedInstance().setMessage("Testing connection to the main MW server...");
 					ConnectionManager.sharedInstance().setWorkOffline(!ModelerUtilities.pingMwServer());
 					Initializer.init();
 					return createModeler();
 				}
 
 				public void finished() {
 					Modeler m = (Modeler) get();
 					Debugger.print("Modeler created");
 					m.pack();
 					m.setVisible(true);
 					Debugger.print("Modeler becomes visible");
 					m.toFront();
 					Debugger.print("Modeler brought to front");
 					EventQueue.invokeLater(new Runnable() {
 						public void run() {
 							Initializer.sharedInstance().hideSplashScreen();
 						}
 					});
 				}
 			};
 			// Spawn the loading thread from the EDT, instead of the main thread?
 			EventQueue.invokeLater(new Runnable() {
 				public void run() {
 					worker.start();
 				}
 			});
 
 		}
 
 		directMW = true;
 
 		// signify the web launcher who is monitoring this process
 		if ("yes".equals(System.getProperty("mw.launcher")))
 			System.out.println("launched");
 		if (!hostIsLocal) {
 			LogDumper.sharedInstance().redirectSystemOutput();
 		}
 
 		ModelerUtilities.testQuicktime();
 
 	}
 }
