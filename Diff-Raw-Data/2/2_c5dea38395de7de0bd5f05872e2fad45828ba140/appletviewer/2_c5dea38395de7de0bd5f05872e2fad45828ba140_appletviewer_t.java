 package app;
 
 import java.applet.Applet;
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 import nativeadvert.browsercontrol;
 
 public class appletviewer
 	extends Frame
 	implements ComponentListener, WindowListener
 {
 	private static final appletviewer INSTANCE = new appletviewer();
 
 	public static boolean debug = false;
 
 	private boolean inWindows;
 	private boolean in64Bits;
 
 	private File _configFile;
 	private String _configUrl;
 	private final HashMap<String, String> _configClient = new HashMap<String, String>();
 	private final HashMap<String, String> _configApplet = new HashMap<String, String>();
 	private final HashMap<Integer, String> _languages = new HashMap<Integer, String>();
 
 	// interface elements
 	private Panel _panel;
 	private Component _footerPanel;
 	private Canvas _browserCanvas;
 	private Applet _gameApplet;
 
 	public static DialogProgress progressDialog;
 
 	private appletviewer()
 	{
 	}
 
 	public static appletviewer getInstance()
 	{
 		return INSTANCE;
 	}
 
 	private void buildMenu()
 	{
 		MenuBar menuBar = new MenuBar();
 		setMenuBar(menuBar);
 
 		// OPTIONS MENU
 		Menu menuOptions = new Menu(Language.getText("options"));
 		menuBar.add(menuOptions);
 
 		// always on top menu
 		String prefAlwaysOnTop = Preferences.get("AlwaysOnTop");
 		if (prefAlwaysOnTop == null) {
 			prefAlwaysOnTop = "no";
 		}
 		CheckboxMenuItem menuAlwaysOnTop = new CheckboxMenuItem("Always on top", prefAlwaysOnTop.equals("yes"));
 		menuAlwaysOnTop.addItemListener(new ItemListener() {
 			public void itemStateChanged(ItemEvent e) {
 				if (e.getStateChange() == ItemEvent.SELECTED) {
 					Preferences.set("AlwaysOnTop", "yes");
 					setAlwaysOnTop(true);
 				} else {
 					Preferences.set("AlwaysOnTop", "no");
 					setAlwaysOnTop(false);
 				}
 				Preferences.save();
 			}
 		});
 		menuOptions.add(menuAlwaysOnTop);
 
 		// create the language menu if we have languages
 		if (!_languages.isEmpty()) {
 			MenuItem menuLanguage = new MenuItem(Language.getText("language") + "...");
 			menuLanguage.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					new DialogLanguage(INSTANCE, _languages);
 				}
 			});
 			menuOptions.add(menuLanguage);
 		}
 
 		// VIEW MENU
 		Menu menuView = new Menu("View");
 		menuBar.add(menuView);
 
 		String prefCopyright = Preferences.get("ShowCopyright");
 		if (prefCopyright == null) {
 			prefCopyright = "yes";
 		}
 		CheckboxMenuItem menuCopyright = new CheckboxMenuItem("Copyright", prefCopyright.equals("yes"));
 		menuCopyright.addItemListener(new ItemListener() {
 			public void itemStateChanged(ItemEvent e) {
 				if (e.getStateChange() == ItemEvent.SELECTED) {
 					Preferences.set("ShowCopyright", "yes");
 					showFooter();
 				} else {
 					Preferences.set("ShowCopyright", "no");
 					hideFooter();
 				}
 				Preferences.save();
 			}
 		});
 		menuView.add(menuCopyright);
 	}
 
 	private void showFooter()
 	{
 		setSize(getSize().width, getSize().height + 40);
 		_footerPanel = new CopyrightBar(Language.getText("tandc"));
 		_panel.add(_footerPanel);
 		resize();
 	}
 
 	private void hideFooter()
 	{
 		_panel.remove(_footerPanel);
 		_footerPanel = null;
 		setSize(getSize().width, getSize().height - 40);
 	}
 
 	private void loadConfiguration()
 	{
 		_languages.clear();
 		_configApplet.clear();
 		_configClient.clear();
 
 		BufferedReader reader = null;
 		try {
 			reader = getConfigReader();
 			String configLine;
 			while ((configLine = reader.readLine()) != null) {
 				configLine = configLine.trim();
 				
 				if (configLine.startsWith("//") || configLine.startsWith("#")) {
 					// ignore commented lines
 				} else if (configLine.startsWith("msg=")) {
 					// language text strings
 					configLine = configLine.substring(4);
 					int k = configLine.indexOf('=');
 					if (k != -1) {
 						String alias = configLine.substring(0, k).trim().toLowerCase();
 						String text = configLine.substring(k + 1).trim();
 						if (alias.startsWith("lang")) {
 							try {
 								_languages.put(Integer.valueOf(alias.substring(4)), text);
 							} catch (NumberFormatException ex) {
 								// invalid language id
 							}
 						}
 						Language.setText(alias, text);
 						if (debug) {
 							System.out.println("Message: alias = " + alias + ", text = " + text);
 						}
 					}
 				} else if (configLine.startsWith("param=")) {
 					// game applet configuration
 					configLine = configLine.substring(6);
 					int k = configLine.indexOf('=');
 					if (k != -1) {
 						String variable = configLine.substring(0, k).trim().toLowerCase();
 						String value = configLine.substring(k + 1).trim();
 						_configApplet.put(variable, value);
 						if (debug) {
 							System.out.println("Applet config: variable = " + variable + ", value = " + value);
 						}
 					}
 				} else {
 					// client configuration
 					int k = configLine.indexOf('=');
 					if (k != -1) {
 						String variable = configLine.substring(0, k).trim().toLowerCase();
 						String value = configLine.substring(k + 1).trim();
 						_configClient.put(variable, value);
 						if (debug) {
 							System.out.println("Client config: variable = " + variable + ", value = " + value);
 						}
 					}
 				}
 			}
 		} catch (IOException ex) {
 			if (debug) {
 				ex.printStackTrace();
 			}
 			DialogMessage.showError(this, Language.getText("err_load_config"));
 		} catch (Exception ex) {
 			if (debug) {
 				ex.printStackTrace();
 			}
 			DialogMessage.showError(this, Language.getText("err_decode_config"));
 		} finally {
 			try {
 				reader.close();
 			} catch (Exception ex) {
 			}
 		}
 	}
 
 	private BufferedReader getConfigReader()
 			throws IOException
 	{
 		if (_configUrl == null) {
 			return new BufferedReader(new FileReader(_configFile));
 		}
 		return new BufferedReader(new InputStreamReader(new URL(_configUrl).openStream()));
 	}
 
 	private static File getPath(String fileName, String subDir, int mode, String workingPath)
 	{
 		String[] cachePaths1 = {
 				"c:/rscache/", "/rscache/", "c:/windows/", "c:/winnt/", "c:/", workingPath, "/tmp/", "" };
 		String[] cachePaths2 = {
 				".jagex_cache_" + mode, ".file_store_" + mode };
 
 		for (String cachePath1 : cachePaths1) {
 			for (String cachePath2 : cachePaths2) {
 				for (int i = 0; i < 2; i++) {
 					switch (i) {
 						case 0:
 							if (cachePath1.length() == 0 || new File(cachePath1).exists()) {
 								new File(cachePath1 + cachePath2 + (subDir == null ? "" : "/" + subDir)).mkdirs();
 							}
 							break;
 						case 1:
 							String filePath = cachePath1 + cachePath2 + "/" + (subDir == null ? "" : subDir + "/") + fileName;
 							File file = new File(filePath);
 
 							// check if the file is readable and writable
 							if (file.canRead() && file.canWrite()) {
 								return file;
 							} else {
 								if (debug) {
 									System.out.println("Unable to read/write: " + filePath);
 								}
 							}
 							break;
 					}
 				}
 			}
 		}
 
 		if (debug) {
 			throw new RuntimeException("Fatal - could not find ANY location for file: " + fileName);
 		}
 		throw new RuntimeException();
 	}
 
 	public void load(String gameName, String configUrl, String configFile, boolean debug)
 	{
 		// detect windows os
 		String osName = System.getProperty("os.name").toLowerCase();
 		inWindows = osName.startsWith("win");
 
 		// detect 64 bit architecture
 		String osArch = System.getProperty("os.arch").toLowerCase();
 		in64Bits = (osArch.startsWith("amd64") || osArch.startsWith("x86_64"));
 
 		// set debug status
 		this.debug = debug;
 		if (appletviewer.debug) {
 			System.setErr(DialogDebug.getPrintStream("Jagex host console"));
 			System.setOut(DialogDebug.getPrintStream("Jagex host console"));
 
 			System.out.println("release #7");
 			System.out.println("java.version = " + System.getProperty("java.version"));
 			System.out.println("os.name = " + osName);
 			System.out.println("os.arch = " + osArch);
 			System.out.println("========================================");
 		}
 
 		// load saved preferences
 		Preferences.load();
 
 		// set always on top
 		String alwaysOnTop = Preferences.get("AlwaysOnTop");
 		if (alwaysOnTop != null && alwaysOnTop.equals("yes")) {
 			setAlwaysOnTop(true);
 		}
 
 		// load or detect locale
 		int languageId = 0;
 		String savedLangId = Preferences.get("Language");
 		if (savedLangId != null) {
 			languageId = Integer.parseInt(savedLangId);
 		} else {
 			Locale locale = Locale.getDefault();
 			String localeLanguage = locale.getISO3Language();
 			String localeCountry = locale.getISO3Country();
 			if (appletviewer.debug) {
 				System.out.println("Current locale language: " + localeLanguage);
 				System.out.println("Current locale country: " + localeLanguage);
 			}
 
 			if (localeLanguage != null) {
 				if (localeLanguage.equals("ger") || localeLanguage.equals("deu") || localeLanguage.equals("nds") || localeLanguage.equals("gmh") || localeLanguage.equals("goh") || localeLanguage.equals("gem") || localeLanguage.equals("gsw")) {
 					languageId = 1;
 				} else if (localeLanguage.equals("fre") || localeLanguage.equals("fra") || localeLanguage.equals("cpf") || localeLanguage.equals("frm") || localeLanguage.equals("fro")) {
 					languageId = 2;
 				} else if (localeLanguage.equals("por") || localeLanguage.equals("cpp")) {
 					languageId = 3;
 				}
 			} else if (localeCountry != null) {
 				if (localeCountry.equals("deu") || localeCountry.equals("ddr")) {
 					languageId = 1;
 				} else if (localeCountry.equals("fra") || localeCountry.equals("fxx")) {
 					languageId = 2;
 				} else if (localeCountry.equals("prt") || localeCountry.equals("bra")) {
 					languageId = 3;
 				}
 			}
 			Preferences.set("Language", Integer.toString(languageId));
 			Preferences.save();
 		}
 		Language.load(languageId);
 		if (appletviewer.debug) {
 			System.out.println("Language ID loaded: " + languageId);
 			System.out.println("========================================");
 		}
 
 		// detect game and current working path
 		String currentPath = System.getProperty("user.home");
 		if (currentPath == null) {
 			currentPath = "~/";
 		} else if (!currentPath.endsWith("/")) {
 			currentPath += "/";
 		}
 		File gamePath = new File(new File(currentPath).getParentFile(), gameName);
 
 		// load window icon
 		File iconFile = new File(gamePath, "jagexappletviewer.png");
 		if (appletviewer.debug) {
 			System.out.println("Trying to load icon file: " + iconFile.getAbsolutePath());
 			System.out.println("========================================");
 		}
 		if (iconFile.exists()) {
 			Image icon = Toolkit.getDefaultToolkit().getImage(iconFile.getAbsolutePath());
 			if (icon != null) {
 				this.setIconImage(icon);
 			}
 		}
 
 		// open loading dialog
 		progressDialog = new DialogProgress(this, "Jagex Ltd.");
 		progressDialog.setText(Language.getText("loaderbox_initial"));
 
 		// load configuration (1st step)
 		progressDialog.setText(Language.getText("loading_config"));
 		if (configUrl == null) {
 			if (configFile == null) {
 				DialogMessage.showError(this, Language.getText("err_missing_config"));
 			} else {
 				_configFile = new File(gamePath, configFile);
 				if (appletviewer.debug) {
 					System.out.println("Config file: " + _configFile.getAbsolutePath());
 				}
 			}
 		} else {
 			if (configUrl != null) {
 				_configUrl = formatWithConfig(configUrl);
 				if (appletviewer.debug) {
 					System.out.println("Config URL: " + _configUrl);
 				}
 
 				// get config url domain
 				String domain = _configUrl.toLowerCase();
 				if (domain.startsWith("http://")) {
 					domain = domain.substring(7);
 				} else if (domain.startsWith("https://")) {
 					domain = domain.substring(8);
 				}
 
 				// trim port
 				int colonPos = domain.indexOf(":");
 				if (colonPos != -1) {
 					domain = domain.substring(0, colonPos);
 				}
 
 				// trim extra path
 				int slashPos = domain.indexOf("/");
 				if (slashPos != -1) {
 					domain = domain.substring(0, slashPos);
 				}
 
 				if (appletviewer.debug) {
 					System.out.println("Config URL domain: " + domain);
 				}
 				if (!(domain.endsWith(".runescape.com") || domain.endsWith(".funorb.com"))) {
 					DialogMessage.showError(this, Language.getText("err_invalid_config"));
 				}
 			}
 		}
 		progressDialog.setValue(1, 6); // half of first step done
 		loadConfiguration();
 		if (appletviewer.debug) {
 			System.out.println("========================================");
 		}
 		progressDialog.setValue(1, 3); // 1st step out of 3 finished
 
 		// build menu
 		buildMenu();
 
 		// check for newer version
 		String confVersion = _configClient.get("viewerversion");
 		if (confVersion != null) {
 			try {
 				int version = Integer.parseInt(confVersion);
 				if (version > 100) {
 					DialogMessage.showMessage(this, Language.getText("new_version"));
 				}
 			} catch (NumberFormatException ex) {
 			}
 		}
 
 		// get some config variables
 		int mode = Integer.parseInt(_configApplet.get("modewhat")) + 32;
 		String cacheSubdir = _configClient.get("cachesubdir");
 		String codeBase = _configClient.get("codebase");
 
 		// download browsercontrol (2nd step)
 		progressDialog.setText(Language.getText("loading_app_resources"));
 		progressDialog.setValue(28000, 84000);
 
 		File browserControlPath = null;
 		if (inWindows) {
 			try {
 				byte[] browserControlJar, browserControlDll;
 				if (in64Bits) {
 					browserControlJar = downloadFile(_configClient.get("browsercontrol_win_amd64_jar"), codeBase);
 					browserControlDll = new JavaArchive(browserControlJar).Extract("browsercontrol64.dll");
 					if (browserControlDll == null) {
 						DialogMessage.showError(this, Language.getText("err_verify_bc64"));
 					}
 
 					browserControlPath = getPath("browsercontrol64.dll", cacheSubdir, mode, currentPath);
 					saveFile(browserControlDll, browserControlPath);
 				} else {
 					browserControlJar = downloadFile(_configClient.get("browsercontrol_win_x86_jar"), codeBase);
 					browserControlDll = new JavaArchive(browserControlJar).Extract("browsercontrol.dll");
 					if (browserControlDll == null) {
 						DialogMessage.showError(this, Language.getText("err_verify_bc"));
 					}
 
 					browserControlPath = getPath("browsercontrol.dll", cacheSubdir, mode, currentPath);
 					saveFile(browserControlDll, browserControlPath);
 				}
 				if (appletviewer.debug) {
 					System.out.println("Browser control jar size: " + browserControlJar.length + " bytes");
 					System.out.println("Browser control dll size: " + browserControlDll.length + " bytes");
 					System.out.println("========================================");
 				}
 			} catch (Exception ex) {
 				if (appletviewer.debug) {
 					ex.printStackTrace();
 				}
 				DialogMessage.showError(this, Language.getText("err_load_bc"));
 			}
 		}
 		progressDialog.setValue(2, 3); // 2nd step out of 3 finished
 
 		// set up our own class loader, so we can trap
 		// 'netscape.javascript.jsobject'
 		if (inWindows) {
 			MasterClassLoader.init();
 		}
 
 		// download game loader // 3rd step
 		progressDialog.setText(Language.getText("loading_app"));
 		progressDialog.setValue(61000, 91500);
 
 		try {
 			byte[] loaderJar = downloadFile(_configClient.get("loader_jar"), codeBase);
 			JarClassLoader jcl = new JarClassLoader(loaderJar);
 			_gameApplet = (Applet) jcl.loadClass("loader").newInstance();
 			if (appletviewer.debug) {
 				System.out.println("Loader jar size: " + loaderJar.length + " bytes");
 				System.out.println("========================================");
 			}
 		} catch (Exception ex) {
 			if (appletviewer.debug) {
 				ex.printStackTrace();
 			}
 			DialogMessage.showError(this, Language.getText("err_target_applet"));
 		}
 
 		// hide loading dialog
 		progressDialog.dispose();
 
 		// set up client settings
 		this.setTitle(_configClient.get("title") + " - hacked by _aLfa_ (c) 2010");
 
 		int advertHeight = (inWindows ? Integer.parseInt(_configClient.get("advert_height")) : 0);
 		int preferredWidth = Integer.parseInt(_configClient.get("window_preferredwidth"));
 		int preferredHeight = Integer.parseInt(_configClient.get("window_preferredheight"));
 
 		Insets insets = this.getInsets();
 		this.setSize(preferredWidth + insets.left + insets.right, preferredHeight + advertHeight + insets.top + insets.bottom);
 		this.setLocationRelativeTo(null);
 		this.setVisible(true);
 
 		_panel = new Panel();
 		_panel.setBackground(Color.black);
 		_panel.setLayout(null);
 		add(_panel);
 
 		// display the browser canvas if the user isn't a member
 		String member = Preferences.get("Member");
 		if (inWindows && (member == null || !member.equals("yes"))) {
 			_browserCanvas = new Canvas();
 			_panel.add(_browserCanvas);
 		}
 
 		_panel.add(_gameApplet);
 
 		String showCopyright = Preferences.get("ShowCopyright");
 		if (showCopyright == null || showCopyright.equals("yes")) {
 			showFooter();
 		}
 
 		doLayout();
 		resize();
 		doLayout();
 
 		// display the browser control with advertising
 		if (inWindows && _browserCanvas != null) {
 			// wait until the browser canvas is visible
 			while (!(_browserCanvas.isDisplayable() && _browserCanvas.isShowing())) {
 				try {
 					Thread.sleep(100L);
 				} catch (InterruptedException ex) {
 				}
 			}
 
 			// create the browser control in the browser canvas
 			try {
				System.load(browserControlPath.getAbsolutePath());
 				browsercontrol.create(_browserCanvas, _configClient.get("adverturl"));
 				browsercontrol.resize(_browserCanvas.getSize().width, _browserCanvas.getSize().height);
 			} catch (Throwable ex) {
 				if (appletviewer.debug) {
 					ex.printStackTrace();
 				}
 				DialogMessage.showError(this, Language.getText("err_create_advertising"));
 			}
 		}
 
 		this.addWindowListener(this);
 		addComponentListener(this);
 		_gameApplet.setStub(new GameAppletStub(_configClient, _configApplet));
 		_gameApplet.init();
 		_gameApplet.start();
 	}
 
 	public void removeAdvert()
 	{
 		if (_browserCanvas == null) {
 			return;
 		}
 		if (browsercontrol.iscreated()) {
 			browsercontrol.destroy();
 		}
 		_panel.remove(_browserCanvas);
 		_browserCanvas = null;
 		resize();
 	}
 
 	private static String formatWithConfig(String format)
 	{
 		String text = format;
 		do {
 			int holderStart = text.indexOf("$(");
 			if (holderStart == -1) {
 				break;
 			}
 			int holderMiddle = text.indexOf(":", holderStart);
 			if (holderMiddle == -1) {
 				break;
 			}
 			int holderEnd = text.indexOf(")", holderMiddle);
 			if (holderEnd == -1) {
 				break;
 			}
 
 			String prefName = text.substring(holderStart + 2, holderMiddle);
 			String prefDefault = text.substring(holderMiddle + 1, holderEnd);
 			String prefValue = Preferences.get(prefName);
 			if (prefValue == null) {
 				prefValue = prefDefault;
 			}
 
 			if (holderEnd + 1 == text.length()) {
 				text = text.substring(0, holderStart) + prefValue;
 				break;
 			} else {
 				text = text.substring(0, holderStart) + prefValue + text.substring(holderEnd + 1);
 			}
 		} while (true);
 
 		return text;
 	}
 
 	public void reAddAdvert()
 	{
 		if (!inWindows || _browserCanvas != null) {
 			return;
 		}
 
 		_browserCanvas = new Canvas();
 		_panel.add(_browserCanvas);
 		resize();
 
 		// wait until the browser canvas is visible
 		while (!(_browserCanvas.isDisplayable() && _browserCanvas.isShowing())) {
 			try {
 				Thread.sleep(100L);
 			} catch (InterruptedException ex) {
 			}
 		}
 
 		// create the browser control in the browser canvas
 		try {
 			browsercontrol.create(_browserCanvas, _configClient.get("adverturl"));
 			browsercontrol.resize(_browserCanvas.getSize().width, _browserCanvas.getSize().height);
 		} catch (Throwable ex) {
 			if (debug) {
 				ex.printStackTrace();
 			}
 			DialogMessage.showError(this, Language.getText("err_create_advertising"));
 		}
 	}
 
 	public static void terminate()
 	{
 		if (browsercontrol.iscreated()) {
 			browsercontrol.destroy();
 		}
 		System.exit(0);
 	}
 
 	private void saveFile(byte[] fileData, File filePath)
 	{
 		FileOutputStream writer = null;
 		try {
 			writer = new FileOutputStream(filePath);
 			writer.write(fileData, 0, fileData.length);
 		} catch (Exception ex) {
 			if (debug) {
 				ex.printStackTrace();
 			}
 			DialogMessage.showError(this, Language.getText("err_save_file"));
 		} finally {
 			if (writer != null) {
 				try {
 					writer.close();
 				} catch (Exception ex) {
 					// ignore
 				}
 			}
 		}
 	}
 
 	private void resize()
 	{
 		int advertHeight = (_browserCanvas == null ? 0 : Integer.parseInt(_configClient.get("advert_height")));
 		int footerHeight = (_footerPanel == null ? 0 : 40);
 
 		Dimension frameSize = getSize();
 		Insets frameInsets = getInsets();
 
 		int appletWidth = frameSize.width - frameInsets.left - frameInsets.right;
 		int appletHeight = frameSize.height - frameInsets.top - frameInsets.bottom - advertHeight - footerHeight;
 
 		if (_browserCanvas != null) {
 			_browserCanvas.setBounds(0, 0, appletWidth, advertHeight);
 			if (browsercontrol.iscreated()) {
 				browsercontrol.resize(appletWidth, advertHeight);
 			}
 		}
 		_gameApplet.setBounds(0, advertHeight, appletWidth, appletHeight);
 		if (_footerPanel != null) {
 			_footerPanel.setBounds(0, advertHeight + appletHeight, appletWidth, footerHeight);
 		}
 	}
 
 	private byte[] downloadFile(String fileName, String baseUrl)
 	{
 		byte[] buffer = new byte[300000];
 		int bufferOffset = 0;
 
 		try {
 			InputStream reader = new URL(baseUrl + fileName).openStream();
 			int bytesRead;
 			while (bufferOffset < buffer.length && (bytesRead = reader.read(buffer, bufferOffset, buffer.length - bufferOffset)) != -1) {
 				bufferOffset += bytesRead;
 				progressDialog.setValue(progressDialog.getValue() + bytesRead);
 			}
 
 			reader.close();
 		} catch (Exception localException) {
 			if (debug) {
 				localException.printStackTrace();
 			}
 			DialogMessage.showError(this, Language.getText("err_downloading") + ": " + fileName);
 		}
 
 		byte[] fileData = new byte[bufferOffset];
 		System.arraycopy(buffer, 0, fileData, 0, bufferOffset);
 		return fileData;
 	}
 
 	public void showUrl(String url, String target)
 	{
 		// quit url
 		if (target != null && target.equals("_top") && (url.endsWith("MAGICQUIT") || url.indexOf("/quit.ws") != -1 || (url.indexOf(".ws") == -1 && url.endsWith("/")))) {
 			// exit application
 			terminate();
 		} else if (url.startsWith("http://") || url.startsWith("https://")) {
 			// we only open http or https urls
 			try {
 				// open url in user's default browser
 				Desktop.getDesktop().browse(new URI(url));
 			} catch (URISyntaxException ex) {
 				// ignore invalid url
 				if (debug) {
 					ex.printStackTrace();
 				}
 			} catch (Exception ex) {
 				// default browser could not be opened for some reason
 				if (debug) {
 					ex.printStackTrace();
 				}
 
 				// show a window for user to copy/paste the url
 				new DialogUrl(this, url);
 			}
 		}
 	}
 
 	@Override
 	public final void componentMoved(ComponentEvent e)
 	{
 	}
 
 	@Override
 	public final void componentResized(ComponentEvent e)
 	{
 		resize();
 	}
 
 	@Override
 	public final void componentHidden(ComponentEvent e)
 	{
 	}
 
 	@Override
 	public final void componentShown(ComponentEvent e)
 	{
 	}
 
 	@Override
 	public void windowOpened(WindowEvent e)
 	{
 	}
 
 	@Override
 	public void windowClosing(WindowEvent e)
 	{
 		terminate();
 	}
 
 	@Override
 	public void windowClosed(WindowEvent e)
 	{
 	}
 
 	@Override
 	public void windowIconified(WindowEvent e)
 	{
 	}
 
 	@Override
 	public void windowDeiconified(WindowEvent e)
 	{
 	}
 
 	@Override
 	public void windowActivated(WindowEvent e)
 	{
 	}
 
 	@Override
 	public void windowDeactivated(WindowEvent e)
 	{
 	}
 }
