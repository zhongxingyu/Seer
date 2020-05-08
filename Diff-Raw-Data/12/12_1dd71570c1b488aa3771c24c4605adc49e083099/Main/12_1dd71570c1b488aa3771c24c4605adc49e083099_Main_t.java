 package gsingh.learnkirtan;
 
 import static gsingh.learnkirtan.Constants.VERSION;
 import static gsingh.learnkirtan.Constants.WHITE_KEY_HEIGHT;
 import static gsingh.learnkirtan.Constants.WHITE_KEY_WIDTH;
 import gsingh.learnkirtan.FileManager.SaveResult;
 import gsingh.learnkirtan.installer.SoundBankInstaller;
 import gsingh.learnkirtan.keys.KeyMapper;
 import gsingh.learnkirtan.keys.LabelManager;
import gsingh.learnkirtan.listener.SettingsChangedListener;
 import gsingh.learnkirtan.note.NoteList;
 import gsingh.learnkirtan.player.ShabadPlayer;
 import gsingh.learnkirtan.settings.SettingsManager;
 import gsingh.learnkirtan.ui.ControlPanel;
 import gsingh.learnkirtan.ui.PianoPanel;
 import gsingh.learnkirtan.ui.WindowTitleManager;
 import gsingh.learnkirtan.ui.action.ActionFactory;
 import gsingh.learnkirtan.ui.menu.EditMenu;
 import gsingh.learnkirtan.ui.menu.FileMenu;
 import gsingh.learnkirtan.ui.menu.HelpMenu;
 import gsingh.learnkirtan.ui.menu.KeyboardMenu;
 import gsingh.learnkirtan.ui.menu.OptionsMenu;
 import gsingh.learnkirtan.ui.menu.controller.HelpMenuController;
 import gsingh.learnkirtan.ui.menu.controller.KeyboardMenuController;
 import gsingh.learnkirtan.ui.menu.controller.OptionsMenuController;
 import gsingh.learnkirtan.ui.shabadeditor.SwingShabadEditor;
 import gsingh.learnkirtan.ui.shabadeditor.tableeditor.TableShabadEditor;
 import gsingh.learnkirtan.utility.DialogUtility;
 import gsingh.learnkirtan.utility.NetworkUtility;
 
 import java.awt.Dimension;
 import java.awt.GridBagLayout;
 import java.awt.SplashScreen;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.IOException;
 
 import javax.swing.BoxLayout;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JMenuBar;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 
 /**
  * The main entry point for the program. Responsible for initializing the GUI,
  * installing the soundbank if necessary, and checking for updates
  * 
  * @author Gulshan
  * 
  */
 public class Main {
 
 	/** The base title of the software. Includes the name and version number */
 	public static final String BASETITLE = "Learn Kirtan v" + VERSION
 			+ " Beta - ";
 
 	/** The width of the screen */
 	private final int WIDTH = 20 * WHITE_KEY_WIDTH;
 
 	/**
 	 * The main shabad editor. When play is pressed, the text in here will be
 	 * played. It cannot be edited while playing.
 	 */
 	public SwingShabadEditor shabadEditor;
 
 	/** The main frame */
 	private JFrame frame;
 
 	/** Manages changes in the title of the window */
 	private WindowTitleManager titleManager;
 
 	/** Handles the opening and saving of files */
 	private FileManager fileManager = new FileManager();
 
 	/** Manages the setting and getting of settings */
 	private SettingsManager settingsManager = SettingsManager.getInstance();
 
 	/** A list of all of the notes */
 	private NoteList notes;
 
 	/** Manages the labelling of the keys */
 	private LabelManager labelManager;
 
 	// Only Main can instantiate itself
 	private Main() {
 	}
 
 	public static void main(String[] args) {
 		new Main().init();
 	}
 
 	/**
 	 * Constructs the GUI, installs the sound bank, and checks for updates
 	 */
 	public void init() {
 		SplashScreen.getSplashScreen();
 
 		// TODO
 		if (System.getProperty("os.name").startsWith("Windows")) {
 			try {
 				UIManager.setLookAndFeel(UIManager
 						.getSystemLookAndFeelClassName());
 			} catch (UnsupportedLookAndFeelException e) {
 			} catch (ClassNotFoundException e) {
 			} catch (InstantiationException e) {
 			} catch (IllegalAccessException e) {
 			}
 		}
 
 		// SettingsManager persists settings so it needs access to a FileManager
 		// However it is a singleton, so this must be set through a method
 		settingsManager.setFileManager(fileManager);
 
 		// The NoteList is initialized with the key representing middle Sa
 		notes = new NoteList(settingsManager.getSaKey());
 
 		labelManager = new LabelManager(notes);
 		KeyMapper.getInstance().setNotes(notes);
 
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				// Initialize GUI
 				createAndShowGui();
 
 				// Install soundbank if necessary
 				SoundBankInstaller installer = new SoundBankInstaller();
 				installer.installSoundBank();
 
 				// Check for update
 				new Thread(new Runnable() {
 					@Override
 					public void run() {
 						NetworkUtility.checkForUpdate();
 					}
 				}).start();
 			}
 		});
 	}
 
 	/**
 	 * Constructs the menu, control panel, piano panel, and shabad editor
 	 */
 	public void createAndShowGui() {
 		frame = new JFrame(BASETITLE + "Untitled Shabad");
 		titleManager = new WindowTitleManager(frame);
 		DialogUtility.setFrame(frame);
 
 		// TODO: Shouldn't need to pass these two managers
 		shabadEditor = new TableShabadEditor(titleManager, fileManager);
 
 		frame.setJMenuBar(createMenuBar());
 
 		JPanel controlPanel = new ControlPanel(new ShabadPlayer(),
 				shabadEditor);
 		JComponent pianoPanel = new PianoPanel(labelManager);
 
		settingsManager
				.addSettingsChangedListener((SettingsChangedListener) pianoPanel);
 		fileManager.addFileEventListener((ControlPanel) controlPanel);
 		fileManager.addFileEventListener(titleManager);
 
 		JPanel mainPanel = initMainPanel(controlPanel, pianoPanel, shabadEditor);
 		frame.add(mainPanel);
 
 		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 		frame.addWindowListener(new MyWindowAdapter());
 		frame.pack();
 		frame.setLocation(250, 60);
 		frame.setResizable(false);
 		frame.setVisible(true);
 
 		shabadEditor.reset();
 	}
 
 	/**
 	 * Constructs the mainPanel by taking the {@code controlPanel},
 	 * {@code pianoPanel}, and {@code editor} and arranging them using a
 	 * {@link GridBagLayout}.
 	 * 
 	 */
 	private JPanel initMainPanel(JPanel controlPanel, JComponent pianoPanel,
 			SwingShabadEditor editor) {
 		JPanel mainPanel = new JPanel();
 		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
 
 		mainPanel.add(controlPanel);
 
 		pianoPanel.setPreferredSize(new Dimension(WIDTH, WHITE_KEY_HEIGHT));
 		mainPanel.add(pianoPanel);
 
 		editor.setPreferredSize(new Dimension(editor.getWidth(), 250));
 		mainPanel.add(editor);
 
 		return mainPanel;
 	}
 
 	private JMenuBar createMenuBar() {
 		JMenuBar menuBar = new JMenuBar();
 		menuBar.add(new FileMenu(new ActionFactory(shabadEditor, fileManager)));
 		menuBar.add(new EditMenu(shabadEditor.getUndoAction(),
 				shabadEditor.getRedoAction()));
 		menuBar.add(new KeyboardMenu(new KeyboardMenuController(shabadEditor)));
 		menuBar.add(new OptionsMenu(new OptionsMenuController(notes,
 				labelManager)));
 		menuBar.add(new HelpMenu(new HelpMenuController()));
 		
 		return menuBar;
 	}
 
 	private class MyWindowAdapter extends WindowAdapter {
 		public void windowClosing(WindowEvent ev) {
 			try {
 				SaveResult result = null;
 				if (shabadEditor.isModified()) {
 					result = fileManager.safeSave(shabadEditor.getShabad());
 				}
 				if (result != SaveResult.NOTSAVEDCANCELLED) {
 					System.exit(0);
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
