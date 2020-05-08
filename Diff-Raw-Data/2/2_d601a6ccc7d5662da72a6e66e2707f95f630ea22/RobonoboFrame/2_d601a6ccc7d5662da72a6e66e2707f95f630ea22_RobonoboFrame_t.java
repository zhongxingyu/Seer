 package com.robonobo.gui.frames;
 
 import static com.robonobo.common.util.TextUtil.*;
 import static javax.swing.SwingUtilities.*;
 import info.clearthought.layout.TableLayout;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.File;
 import java.util.*;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.*;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.robonobo.Robonobo;
 import com.robonobo.common.concurrent.CatchingRunnable;
 import com.robonobo.common.util.FileUtil;
 import com.robonobo.core.Platform;
 import com.robonobo.core.RobonoboController;
 import com.robonobo.core.api.*;
 import com.robonobo.gui.GuiUtil;
 import com.robonobo.gui.GuiConfig;
 import com.robonobo.gui.panels.*;
 import com.robonobo.gui.preferences.PrefDialog;
 import com.robonobo.gui.sheets.*;
 import com.robonobo.gui.tasks.ImportFilesTask;
 import com.robonobo.gui.tasks.ImportITunesTask;
 import com.robonobo.mina.external.ConnectedNode;
 import com.robonobo.mina.external.HandoverHandler;
 
 @SuppressWarnings("serial")
 public class RobonoboFrame extends SheetableFrame implements TrackListener {
 	private RobonoboController control;
 	private String[] cmdLineArgs;
 	private JMenuBar menuBar;
 	private MainPanel mainPanel;
 	private LeftSidebar leftSidebar;
 	private Log log = LogFactory.getLog(RobonoboFrame.class);
 	private GuiConfig guiConfig;
 	private boolean tracksLoaded;
 	private boolean shownLogin;
 
 	public RobonoboFrame(RobonoboController control, String[] args) {
 		this.control = control;
 		this.cmdLineArgs = args;
 
 		setTitle("robonobo");
 		setIconImage(getRobonoboIconImage());
 		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 		addWindowListener(new CloseListener());
 
 		menuBar = Platform.getPlatform().getMenuBar(this);
 		setJMenuBar(menuBar);
 
 		JPanel contentPane = new JPanel();
 		double[][] cellSizen = { { 5, 200, 5, TableLayout.FILL, 5 }, { 3, TableLayout.FILL, 5 } };
 		contentPane.setLayout(new TableLayout(cellSizen));
 		setContentPane(contentPane);
 		leftSidebar = new LeftSidebar(this);
 		contentPane.add(leftSidebar, "1,1");
 		mainPanel = new MainPanel(this);
 		contentPane.add(mainPanel, "3,1");
 		setPreferredSize(new Dimension(1024, 723));
 		pack();
 		leftSidebar.selectMyMusic();
 		guiConfig = (GuiConfig) control.getConfig("gui");
 		addListeners();
 	}
 
 	private void addListeners() {
 		control.addTrackListener(this);
 		// There's a chance the control might have loaded all its tracks before we add ourselves as a tracklistener, so spawn a thread to check if this is so
 		control.getExecutor().execute(new CatchingRunnable() {
 			public void doRun() throws Exception {
 				checkTracksLoaded();
 			}
 		});
 		// Grab our events...
 		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventHandler());
 	}
 
 	@Override
 	public void setVisible(boolean visible) {
 		super.setVisible(visible);
 		if (visible) {
 			// Log us the hell in
 			Runnable onLogin = new CatchingRunnable() {
 				public void doRun() throws Exception {
 					// If the tracks haven't loaded yet, show the welcome when they have
 					shownLogin = true;
 					if (tracksLoaded)
 						showWelcome(false);
 				}
 			};
 			final LoginSheet lp = new LoginSheet(RobonoboFrame.this, onLogin);
 			showSheet(lp);
 			if (isNonEmpty(lp.getEmailField().getText())) {
 				invokeLater(new CatchingRunnable() {
 					public void doRun() throws Exception {
 						lp.tryLogin();
 					}
 				});
 			}
 		}
 	}
 
 	public LeftSidebar getLeftSidebar() {
 		return leftSidebar;
 	}
 
 	public GuiConfig getGuiConfig() {
 		return guiConfig;
 	}
 
 	public PlaybackPanel getPlaybackPanel() {
 		return mainPanel.getPlaybackPanel();
 	}
 
 	public MainPanel getMainPanel() {
 		return mainPanel;
 	}
 
 	/**
 	 * Once this is called, everything is up and running
 	 */
 	@Override
 	public void allTracksLoaded() {
 		tracksLoaded = true;
 		setupHandoverHandler();
 		handleArgs();
 		// If we haven't shown the login sheet yet, show the welcome later
 		if (shownLogin)
 			showWelcome(false);
 	}
 
 	private void checkTracksLoaded() {
 		if (tracksLoaded)
 			return;
 		if (control.haveAllSharesStarted())
 			allTracksLoaded();
 	}
 
 	private void handleArgs() {
 		// Handle everything that isn't the -console
 		for (String arg : cmdLineArgs) {
 			if (!"-console".equalsIgnoreCase(arg))
 				handleArg(arg);
 		}
 	}
 
 	private void setupHandoverHandler() {
 		control.setHandoverHandler(new HandoverHandler() {
 			@Override
 			public String gotHandover(String arg) {
 				handleArg(arg);
 				SwingUtilities.invokeLater(new CatchingRunnable() {
 					public void doRun() throws Exception {
 						// Note: this doesn't bring the app to the front on OSX, but we don't care that much as the app
 						// receives URL notifications directly anyway
 						// If we need it at a subsequent stage, just run an applescript:
 						// tell app "robonobo"
 						// activate
 						// end tell
 						RobonoboFrame.this.setState(Frame.NORMAL);
 						RobonoboFrame.this.toFront();
 					}
 				});
 				log.debug("Got handover msg: " + arg);
 				return "0:OK";
 			}
 
 		});
 	}
 
 	private void handleArg(String arg) {
 		if (isNonEmpty(arg)) {
 			if (arg.startsWith("rbnb"))
 				openRbnbUri(arg);
 			else
 				log.error("Received erroneous robonobo argument: " + arg);
 		}
 	}
 
 	public void openRbnbUri(String uri) {
 		Pattern uriPat = Pattern.compile("^rbnb:(\\w+):(.*)$");
 		Matcher m = uriPat.matcher(uri);
 		if (m.matches()) {
 			log.info("Opening URI "+uri);
 			String objType = m.group(1);
 			String objId = m.group(2);
 			if (objType.equalsIgnoreCase("focus")) {
 				// Do nothing, arg handler will bring us to front anyway
 				return;
 			}
 			if (objType.equalsIgnoreCase("playlist")) {
 				long pId = Long.parseLong(objId, 16);
 				leftSidebar.showPlaylist(pId);
 				return;
 			}
 		} else 
 			log.error("Received invalid rbnb uri: " + uri);
 	}
 
 	public void showWelcome(boolean forceShow) {
 		// If we have no shares (or we're forcing it), show the welcome dialog
 		final boolean gotShares = (control.getShares().size() > 0);
 		if (forceShow || (!gotShares && guiConfig.getShowWelcomePanel())) {
 			SwingUtilities.invokeLater(new CatchingRunnable() {
 				public void doRun() throws Exception {
 					showSheet(new WelcomeSheet(RobonoboFrame.this));
 				}
 			});
 		}
 	}
 
 	@Override
 	public void trackUpdated(String streamId) {
 		// Do nothing
 	}
 
 	@Override
 	public void tracksUpdated(Collection<String> streamIds) {
 		// Do nothing
 	}
 
 	public void importFilesOrDirectories(final List<File> files) {
 		List<File> allFiles = new ArrayList<File>();
 		for (File selFile : files)
 			if (selFile.isDirectory())
 				allFiles.addAll(FileUtil.getFilesWithinPath(selFile, "mp3"));
 			else
 				allFiles.add(selFile);
 		importFiles(allFiles);
 		return;
 	}
 
 	public void importFiles(final List<File> files) {
 		ImportFilesTask t = new ImportFilesTask(control, files);
 		control.runTask(t);
 	}
 
 	public void importITunes() {
 		ImportITunesTask t = new ImportITunesTask(control);
 		control.runTask(t);
 	}
 
 	public void showAddSharesDialog() {
 		// Define this as a runnable as we might need to login first
 		Runnable flarp = new CatchingRunnable() {
 			@Override
 			public void doRun() throws Exception {
 				JFileChooser fc = new JFileChooser();
 				fc.setFileFilter(new javax.swing.filechooser.FileFilter() {
 					public boolean accept(File f) {
 						if (f.isDirectory())
 							return true;
 						return "mp3".equalsIgnoreCase(FileUtil.getFileExtension(f));
 					}
 
 					public String getDescription() {
 						return "MP3 files";
 					}
 				});
 				fc.setMultiSelectionEnabled(true);
 				fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
 				int retVal = fc.showOpenDialog(RobonoboFrame.this);
 				if (retVal == JFileChooser.APPROVE_OPTION) {
 					final File[] selFiles = fc.getSelectedFiles();
 					control.getExecutor().execute(new CatchingRunnable() {
 						public void doRun() throws Exception {
 							importFilesOrDirectories(Arrays.asList(selFiles));
 						}
 					});
 				}
 			}
 		};
 		if (control.getMyUser() != null)
 			flarp.run();
 		else
 			showLogin(flarp);
 	}
 
 	/**
 	 * @param onLogin
 	 *            If the login is successful, this will be executed on the Swing GUI thread (so don't do too much in it)
 	 */
 	public void showLogin(final Runnable onLogin) {
 		SwingUtilities.invokeLater(new CatchingRunnable() {
 			public void doRun() throws Exception {
 				LoginSheet lp = new LoginSheet(RobonoboFrame.this, onLogin);
 				showSheet(lp);
 			}
 		});
 	}
 
 	public void showAbout() {
 		SwingUtilities.invokeLater(new CatchingRunnable() {
 			public void doRun() throws Exception {
 				AboutSheet ap = new AboutSheet(RobonoboFrame.this);
 				showSheet(ap);
 			}
 		});
 
 	}
 
 	public void showPreferences() {
 //		showSheet(new PreferencesSheet(this));
 		PrefDialog prefDialog = new PrefDialog(this);
 		prefDialog.setVisible(true);
 	}
 
 	public void showConsole() {
 		ConsoleFrame consoleFrame = new ConsoleFrame(this);
 		consoleFrame.setVisible(true);
 	}
 
 	public void showLogFrame() {
 		Log4jMonitorFrame logFrame = new Log4jMonitorFrame(this);
 		logFrame.setVisible(true);
 	}
 
 	public static Image getRobonoboIconImage() {
 		return GuiUtil.getImage("/rbnb-icon-128x128.png");
 	}
 
 	public void shutdown() {
 		setVisible(false);
 		Thread shutdownThread = new Thread(new CatchingRunnable() {
 			public void doRun() throws Exception {
 				control.shutdown();
 				System.exit(0);
 			}
 		});
 		shutdownThread.start();
 	}
 
 	public void restart() {
 		log.fatal("robonobo restarting");
 		Thread restartThread = new Thread(new CatchingRunnable() {
 			public void doRun() throws Exception {
 				// Show a message that we're restarting
 				SwingUtilities.invokeLater(new CatchingRunnable() {
 					public void doRun() throws Exception {
 						String[] butOpts = { "Quit" };
 						int result = JOptionPane.showOptionDialog(RobonoboFrame.this,
 								"robonobo is restarting, please wait...", "robonobo restarting",
 								JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, butOpts,
 								"Force Quit");
 						if (result >= 0) {
 							// They pressed the button... just kill everything
 							log.fatal("Emergency shutdown during restart... pressing Big Red Switch");
 							System.exit(1);
 						}
 					}
 				});
 				// Shut down the controller - this will block until the
 				// controller exits
 				control.shutdown();
 				// Hide this frame - don't dispose of it yet as this might make
 				// the jvm exit
 				SwingUtilities.invokeLater(new CatchingRunnable() {
 					public void doRun() throws Exception {
 						RobonoboFrame.this.setVisible(false);
 					}
 				});
 				// Startup a new frame and controller
 				Robonobo.startup(null, cmdLineArgs, false);
 				// Dispose of the old frame
 				RobonoboFrame.this.dispose();
 			}
 		});
 		restartThread.setName("Restart");
 		restartThread.start();
 	}
 
 	public RobonoboController getController() {
 		return control;
 	}
 
 	public void confirmThenShutdown() {
 		invokeLater(new CatchingRunnable() {
 			public void doRun() throws Exception {
 				// If we aren't sharing anything, just close
 				if (getController().getShares().size() == 0) {
 					shutdown();
 					return;
 				}
 				// Likewise, if they've asked us not to confirm
 				if (!getGuiConfig().getConfirmExit()) {
 					shutdown();
 					return;
 				}
 				showSheet(new ConfirmCloseSheet(RobonoboFrame.this));
 			}
 		});
 	}
 
 	class CloseListener extends WindowAdapter {
 		public void windowClosing(WindowEvent e) {
 			confirmThenShutdown();
 		}
 	}
 
 	class KeyEventHandler implements KeyEventDispatcher {
 		@Override
 		public boolean dispatchKeyEvent(KeyEvent e) {
 			int code = e.getKeyCode();
 			int modifiers = e.getModifiers();
 			if (code == KeyEvent.VK_ESCAPE) {
 				if (isShowingSheet()) {
 					discardTopSheet();
 					return true;
 				}
 			}
 			if (code == KeyEvent.VK_Q && modifiers == Platform.getPlatform().getCommandModifierMask()) {
				confirmThenShutdown();
 				return true;
 			}
 			return false;
 		}
 	}
 }
