 package frontend;
 
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.script.ScriptException;
 import javax.swing.AbstractAction;
 import javax.swing.BoxLayout;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSplitPane;
 import javax.swing.JTabbedPane;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 
 import foobar.ScriptFooable;
 import plugins.Plugin;
 import plugins.PluginManager;
 import plugins.SimplePluginManager;
 
 import com.apple.eawt.Application;
 
 import foobar.Fooable;
 import foobar.Foobar;
 import foobar.FoobarTest;
 
 /**
  * The main Bruno application.
  * 
  * @author samuelainsworth
  * 
  */
 public class Bruno extends JFrame {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1987233037023049749L;
 
 	private final JTabbedPane tabPane;
 	private final JSplitPane splitPane;
 	private final ComponentPlaceholder editingWindowPlaceholder;
 	private final ComponentPlaceholder undoViewPlaceholder;
 	private EditingWindow editingWindow;
 
 	private PluginManager pluginManager = new SimplePluginManager();
 
 	private Foobar foobar;
 
 	// private final Foobar foobar;
 	// private final PopupFactory factory = PopupFactory.getSharedInstance();
 	// private Popup foobarPopup;
 
 	public Bruno() {
 		setTitle("Bruno");
 		setBar();
 		setSize(1024, 768);
 
 		// Center on screen
 		setLocationRelativeTo(null);
 
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 
 		// Key bindings
 		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
 				KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit
 						.getDefaultToolkit().getMenuShortcutKeyMask()), "open");
 		getRootPane().getActionMap().put("open", new AbstractAction() {
 
 			/**
 			 * 
 			 */
 			private static final long serialVersionUID = 4189934329254672244L;
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				JFileChooser fc = new JFileChooser();
 				fc.showOpenDialog(getRootPane());
 				openFile(fc.getSelectedFile());
 			}
 
 		});
 
 		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
 				KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit
 						.getDefaultToolkit().getMenuShortcutKeyMask()),
 				"foobar");
 		getRootPane().getActionMap().put("foobar", new AbstractAction() {
 
 			/**
 			 * 
 			 */
 			private static final long serialVersionUID = 4189934329254672244L;
 			
 			private boolean hasFocus = false;
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (!hasFocus) {
 					foobar.requestFocusInWindow();
 					hasFocus = true;
 				}
 				else {
 					editingWindow.requestFocusInWindow();
 					hasFocus = false;
 				}
 			}
 
 		});
 
 		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
 				KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit
 						.getDefaultToolkit().getMenuShortcutKeyMask()), "save");
 		getRootPane().getActionMap().put("save", new AbstractAction() {
 
 			/**
 			 * 
 			 */
 			private static final long serialVersionUID = 4189934329254672244L;
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// Save current file
 				if (editingWindow != null) {
 					try {
 						editingWindow.save();
 					} catch (IOException e0) {
 						e0.printStackTrace();
 					}
 				}
 			}
 
 		});
 
 		editingWindowPlaceholder = new ComponentPlaceholder();
 		undoViewPlaceholder = new ComponentPlaceholder();
 
 		// Side pane
 		JPanel sidePane = new JPanel();
 		sidePane.setLayout(new BoxLayout(sidePane, BoxLayout.Y_AXIS));
 		tabPane = new JTabbedPane();
 		tabPane.addTab("Projects", new ProjectExplorer(this));
 		tabPane.addTab("Edit History", undoViewPlaceholder);
 		
 		foobar = new Foobar();
 		foobar.setMaximumSize(foobar.getPreferredSize());
 		sidePane.add(foobar);
 		sidePane.add(tabPane);
 
 		// Split Pane
 		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
 				editingWindowPlaceholder, sidePane);
 		splitPane.setOneTouchExpandable(true);
 
 		setContentPane(splitPane);
 
 		// foobar = new Foobar();
 
 		// Set<Plugin> plugins = setPlugins();
 
 		// Open blank initial document
 		openDocument(new DocumentModel());
 
 		pluginManager.exposeVariable("bruno", this);
 		pluginManager.exposeVariable("editingWindow", editingWindow);
 		// loadPlugins();
 		Set<ScriptFooable> workingDirScripts = pluginManager
 				.getAllScriptFooables(new File("plugins/"));
 		Set<ScriptFooable> libraryScripts = pluginManager
 				.getAllScriptFooables(new File(
 						"/Library/Application Support/bruno/plugins/"));
 
 		if (workingDirScripts != null)
 			foobar.addFooables(workingDirScripts);
 		if (libraryScripts != null)
 			foobar.addFooables(libraryScripts);
 	}
 
 	/*
 	 * private Set<Plugin> setPlugins() { // Plugins Set<Plugin> s1, s2; //
 	 * first look in current working directory s1 =
 	 * pluginManager.loadPlugins(new File("plugins/")); // then look in
 	 * specified folder s2 = pluginManager.loadPlugins(new File(
 	 * "/Library/Application Support/bruno/plugins/")); // s1 or s2 may be null
 	 * Set<Plugin> plugins = s1; if (plugins == null) { plugins = s2; } // at
 	 * least one of the plugin locations should be correct and readable assert
 	 * plugins != null; return plugins; }
 	 * 
 	 * private void loadPlugins() { Set<Plugin> plugins =
 	 * pluginManager.loadPlugins(new File("plugins/")); for (Plugin plugin :
 	 * plugins) {
 	 * foobarTest.getFoobar().addFooables(plugin.getScriptFooables()); } }
 	 */
 	/**
 	 * Sets up demo menu bar
 	 */
 	private void setBar() {
 		JMenuBar menuBar = new JMenuBar();
 		JMenu file = new JMenu("File");
 		JMenuItem item = new JMenuItem("Woah");
 		file.add(item);
 		menuBar.add(file);
 		setJMenuBar(menuBar);
 	}
	
	public void toggleFoobar() {
		getRootPane().getActionMap().get("foobar").actionPerformed(null);
	}
 
 	private static void setMacStuff() {
 		System.setProperty("apple.laf.useScreenMenuBar", "true");
 	}
 
 	private static void setNiceties() throws ClassNotFoundException,
 			UnsupportedLookAndFeelException, InstantiationException,
 			IllegalAccessException {
 		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		if (System.getProperty("os.name").equals("Mac OS X")) {
 			setMacStuff();
 		}
 		Application application = Application.getApplication();
 		Image image = Toolkit.getDefaultToolkit().getImage("resources/*.jpg");
 		application.setDockIconImage(image);
 	}
 
 	public void openDocument(DocumentModel doc) {
 		// Save current file
 		if (editingWindow != null) {
 			try {
 				editingWindow.save();
 			} catch (IOException e) {
 				JOptionPane.showMessageDialog(this, "Failed to save file "
 						+ doc.getFile(), "File saving error",
 						JOptionPane.ERROR_MESSAGE);
 				e.printStackTrace();
 			}
 		}
 
 		try {
 			editingWindow = new EditingWindow(doc);
 		} catch (FileNotFoundException e) {
 			JOptionPane.showMessageDialog(this,
 					"Failed to open file " + doc.getFile(),
 					"File opening error", JOptionPane.ERROR_MESSAGE);
 			e.printStackTrace();
 		}
 
 		editingWindowPlaceholder.setContents(editingWindow.getView());
 		undoViewPlaceholder.setContents(editingWindow.getUndoController()
 				.getView());
 
 		// Because fuck Swing
 		editingWindowPlaceholder.setVisible(false);
 		editingWindowPlaceholder.setVisible(true);
 	}
 
 	/**
 	 * Open the specified file in the editing area
 	 * 
 	 * @param file
 	 */
 	public void openFile(File file) {
 		openDocument(new DocumentModel(file));
 	}
 
 	/**
 	 * 
 	 */
 	public void close() {
 		setVisible(false);
 		dispose();
 		System.exit(0);
 	}
 
 	public Foobar getFoobar() {
 		return foobar;
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		try {
 			setNiceties();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (UnsupportedLookAndFeelException e) {
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		}
 
 		SwingUtilities.invokeLater(new Runnable() {
 
 			@Override
 			public void run() {
 				Bruno b = new Bruno();
 				b.setVisible(true);
 
 				// Can't set divider location until after frame has been packed
 				// or set visible.
 				b.splitPane.setDividerLocation(0.7);
 			}
 
 		});
 	}
 }
