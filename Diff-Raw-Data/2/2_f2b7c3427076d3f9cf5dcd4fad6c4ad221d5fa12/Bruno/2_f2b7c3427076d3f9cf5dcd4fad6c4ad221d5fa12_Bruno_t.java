 package org.bruno.frontend;
 
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.io.IOException;
 import java.util.Set;
 
 import javax.swing.AbstractAction;
 import javax.swing.BoxLayout;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSplitPane;
 import javax.swing.JTabbedPane;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 
 import org.bruno.foobar.Foobar;
 import org.bruno.foobar.ScriptFooable;
 import org.bruno.plugins.PluginManager;
 import org.bruno.plugins.SimplePluginManager;
 import org.fife.ui.autocomplete.AutoCompletion;
 import org.fife.ui.autocomplete.BasicCompletion;
 import org.fife.ui.autocomplete.CompletionProvider;
 import org.fife.ui.autocomplete.DefaultCompletionProvider;
 import org.fife.ui.autocomplete.ShorthandCompletion;
 
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
 
 	public static final String FILE_EXT = ".bruno~";
 
 	private final JTabbedPane tabPane;
 	private final JSplitPane splitPane;
 	private final ComponentPlaceholder editingWindowPlaceholder;
 	private final ComponentPlaceholder undoViewPlaceholder;
 	private EditingWindow editingWindow;
 
 	private PluginManager pluginManager = new SimplePluginManager();
 
 	private AutoCompletion ac = new AutoCompletion(
 			createJavaCompletionProvider());
 
 	private Foobar foobar;
 	private ProjectExplorer projectExplorer;
 
 	public Bruno() {
 		setTitle("Bruno");
 		setSize(1024, 768);
 
 		// Center on screen
 		setLocationRelativeTo(null);
 
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 
 		editingWindowPlaceholder = new ComponentPlaceholder();
 		undoViewPlaceholder = new ComponentPlaceholder();
 
 		// Side pane
 		JPanel sidePane = new JPanel();
 		sidePane.setLayout(new BoxLayout(sidePane, BoxLayout.PAGE_AXIS));
 		tabPane = new JTabbedPane();
 		projectExplorer = new ProjectExplorer(this);
 		tabPane.addTab("Projects", projectExplorer);
 		tabPane.addTab("Edit History", undoViewPlaceholder);
 
 		foobar = new Foobar(this);
 		foobar.setMaximumSize(new Dimension(999999, (int) foobar
 				.getPreferredSize().getHeight()));
 		sidePane.add(foobar);
 		sidePane.add(tabPane);
 
 		// Split Pane
 		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
 				editingWindowPlaceholder, sidePane);
 		splitPane.setOneTouchExpandable(true);
 
 		setContentPane(splitPane);
 
 		// Open blank initial document
 		openDocument(new DocumentModel());
 
 		// Set up Java autocompletion by default
 		ac.install(editingWindow.getTextArea());
 
 		setUpPlugins();
 		setUpKeybindings();
 	}
 
 	public void addJavaCompletion() {
 		ac.setCompletionProvider(createJavaCompletionProvider());
 		ac.install(editingWindow.getTextArea());
 	}
 
 	public void removeCompletion() {
 		ac.uninstall();
 	}
 
 	// From the RSyntaxTextArea website
 	private CompletionProvider createJavaCompletionProvider() {
 		DefaultCompletionProvider provider = new DefaultCompletionProvider();
 
 		provider.addCompletion(new BasicCompletion(provider, "abstract"));
 		provider.addCompletion(new BasicCompletion(provider, "assert"));
 		provider.addCompletion(new BasicCompletion(provider, "break"));
 		provider.addCompletion(new BasicCompletion(provider, "case"));
 		provider.addCompletion(new BasicCompletion(provider, "catch"));
 		provider.addCompletion(new BasicCompletion(provider, "class"));
 		provider.addCompletion(new BasicCompletion(provider, "const"));
 		provider.addCompletion(new BasicCompletion(provider, "continue"));
 		provider.addCompletion(new BasicCompletion(provider, "default"));
 		provider.addCompletion(new BasicCompletion(provider, "do"));
 		provider.addCompletion(new BasicCompletion(provider, "else"));
 		provider.addCompletion(new BasicCompletion(provider, "enum"));
 		provider.addCompletion(new BasicCompletion(provider, "extends"));
 		provider.addCompletion(new BasicCompletion(provider, "final"));
 		provider.addCompletion(new BasicCompletion(provider, "finally"));
 		provider.addCompletion(new BasicCompletion(provider, "for"));
 		provider.addCompletion(new BasicCompletion(provider, "goto"));
 		provider.addCompletion(new BasicCompletion(provider, "if"));
 		provider.addCompletion(new BasicCompletion(provider, "implements"));
 		provider.addCompletion(new BasicCompletion(provider, "import"));
 		provider.addCompletion(new BasicCompletion(provider, "instanceof"));
 		provider.addCompletion(new BasicCompletion(provider, "interface"));
 		provider.addCompletion(new BasicCompletion(provider, "native"));
 		provider.addCompletion(new BasicCompletion(provider, "new"));
 		provider.addCompletion(new BasicCompletion(provider, "package"));
 		provider.addCompletion(new BasicCompletion(provider, "private"));
 		provider.addCompletion(new BasicCompletion(provider, "protected"));
 		provider.addCompletion(new BasicCompletion(provider, "public"));
 		provider.addCompletion(new BasicCompletion(provider, "return"));
 		provider.addCompletion(new BasicCompletion(provider, "static"));
 		provider.addCompletion(new BasicCompletion(provider, "strictfp"));
 		provider.addCompletion(new BasicCompletion(provider, "super"));
 		provider.addCompletion(new BasicCompletion(provider, "switch"));
 		provider.addCompletion(new BasicCompletion(provider, "synchronized"));
 		provider.addCompletion(new BasicCompletion(provider, "this"));
 		provider.addCompletion(new BasicCompletion(provider, "throw"));
 		provider.addCompletion(new BasicCompletion(provider, "throws"));
 		provider.addCompletion(new BasicCompletion(provider, "transient"));
 		provider.addCompletion(new BasicCompletion(provider, "try"));
 		provider.addCompletion(new BasicCompletion(provider, "void"));
 		provider.addCompletion(new BasicCompletion(provider, "volatile"));
 		provider.addCompletion(new BasicCompletion(provider, "while"));
 
 		provider.addCompletion(new ShorthandCompletion(provider, "sysout",
 				"System.out.println(", "System.out.println("));
 		provider.addCompletion(new ShorthandCompletion(provider, "syserr",
 				"System.err.println(", "System.err.println("));
 
 		return provider;
 	}
 
 	public boolean requestFocusInWindow() {
 		return this.editingWindow.requestFocusInWindow();
 	}
 
 	private void setUpKeybindings() {
 		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
 				KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit
 						.getDefaultToolkit().getMenuShortcutKeyMask()), "new");
 		getRootPane().getActionMap().put("new", new AbstractAction() {
 
 			/**
 		 * 
 		 */
 			private static final long serialVersionUID = 4189934329254672244L;
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				openDocument(new DocumentModel());
 			}
 
 		});
 
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
 				JFileChooser fc;
 				if (projectExplorer.getCurrentFolder() == null) {
 					fc = new JFileChooser();
 				} else {
 					fc = new JFileChooser(projectExplorer.getCurrentFolder());
 				}
 				fc.setFileFilter(new BrunoFileFilter());
 				fc.showOpenDialog(getRootPane());
 				if (fc.getSelectedFile() != null)
 					openFile(fc.getSelectedFile());
 			}
 
 		});
 
 		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
 				KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit
 						.getDefaultToolkit().getMenuShortcutKeyMask()),
 				"foobar");
 		getRootPane().getActionMap().put("foobar", new AbstractAction() {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
				if (getFocusOwner() != null && getFocusOwner().equals(foobar.getField())) {
 					editingWindow.requestFocusInWindow();
 				} else {
 					foobar.requestFocusInWindow();
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
 						editingWindow.save(true);
 					} catch (IOException e0) {
 						e0.printStackTrace();
 					}
 				}
 			}
 
 		});
 
 		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
 				KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit
 						.getDefaultToolkit().getMenuShortcutKeyMask()),
 				"toggle tabPane");
 		getRootPane().getActionMap().put("toggle tabPane",
 				new AbstractAction() {
 					private static final long serialVersionUID = 1L;
 
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						tabPane.setSelectedIndex((tabPane.getSelectedIndex() + 1)
 								% tabPane.getTabCount());
 					}
 				});
 	}
 
 	public EditingWindow getEditingWindow() {
 		return editingWindow;
 	}
 
 	private void setUpPlugins() {
 		pluginManager.exposeVariable("bruno", this);
 		// loadPlugins();
 		Set<ScriptFooable> workingDirScripts = pluginManager
 				.getAllScriptFooables(new File("plugins/"));
 		Set<ScriptFooable> libraryScripts = pluginManager
 				.getAllScriptFooables(new File(
 						"/Library/Application Support/Bruno/plugins/"));
 
 		if (workingDirScripts != null)
 			foobar.addFooables(workingDirScripts);
 		if (libraryScripts != null)
 			foobar.addFooables(libraryScripts);
 	}
 
 	public void reloadPlugins() {
 		foobar.clearFooables();
 		pluginManager.clear();
 		setUpPlugins();
 	}
 
 	// /**
 	// * Sets up demo menu bar
 	// */
 	// private void setDemoMenuBar() {
 	// JMenuBar menuBar = new JMenuBar();
 	// JMenu file = new JMenu("File");
 	// JMenuItem item = new JMenuItem("Woah");
 	// file.add(item);
 	// menuBar.add(file);
 	// setJMenuBar(menuBar);
 	// }
 
 	/**
 	 * Sets up nice look and feel adjustments.
 	 * 
 	 * @throws ClassNotFoundException
 	 * @throws UnsupportedLookAndFeelException
 	 * @throws InstantiationException
 	 * @throws IllegalAccessException
 	 */
 	private static void setNiceties() throws ClassNotFoundException,
 			UnsupportedLookAndFeelException, InstantiationException,
 			IllegalAccessException {
 		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		if (System.getProperty("os.name").equals("Mac OS X")) {
 			System.setProperty("apple.laf.useScreenMenuBar", "true");
 		}
 		// Application application = Application.getApplication();
 		// Image image =
 		// Toolkit.getDefaultToolkit().getImage("resources/*.jpg");
 		// application.setDockIconImage(image);
 	}
 
 	/**
 	 * Toggle the Foobar.
 	 */
 	public void toggleFoobar() {
 		getRootPane().getActionMap().get("foobar").actionPerformed(null);
 	}
 
 	/**
 	 * Open a document in the editor, saving the current document and loading in
 	 * the new one.
 	 * 
 	 * @param doc
 	 */
 	public void openDocument(DocumentModel doc) {
 		// Don't allow opening the currently open file
 		if (doc != null && doc.getFile() != null && editingWindow != null
 				&& doc.getFile().equals(editingWindow.getDoc().getFile())) {
 			editingWindow.getTextArea().requestFocus();
 			return;
 		}
 
 		// Save current file
 		if (editingWindow != null) {
 			try {
 				editingWindow.save();
 			} catch (IOException e) {
 				JOptionPane.showMessageDialog(this, "Failed to save file",
 						"File saving error", JOptionPane.ERROR_MESSAGE);
 				e.printStackTrace();
 			}
 		}
 
 		try {
 			editingWindow = new EditingWindow(this, doc);
 
 			if (doc.getFile() != null
 					&& doc.getFile().getName().endsWith(".java"))
 				ac.install(editingWindow.getTextArea());
 		} catch (Exception e) {
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
 
 		// Now focus the text area
 		editingWindow.getTextArea().requestFocus();
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
 	 * Close the editor.
 	 */
 	public void close() {
 		setVisible(false);
 		dispose();
 		System.exit(0);
 	}
 
 	public Foobar getFoobar() {
 		return foobar;
 	}
 
 	public ProjectExplorer getProjectExplorer() {
 		return projectExplorer;
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
 
 	/* Scripts */
 	public void revertAll(String comment) throws IOException,
 			ClassNotFoundException {
 		File rootFolder = getProjectExplorer().getCurrentFolder();
 		process(rootFolder, comment);
 	}
 
 	private void process(File file, String comment) throws IOException,
 			ClassNotFoundException {
 		if (file.isFile()) {
 			DocumentModel doc1 = new DocumentModel(file);
 			if (doc1.getMetadataFile().exists()) {
 				System.out.println(file);
 				EditingWindow ew = null;
 				DocumentModel doc2 = getEditingWindow().getDoc();
 				if (doc2 != null && !doc2.getFile().equals(file)) {
 					ew = new EditingWindow(null, doc2);
 				} else {
 					ew = getEditingWindow();
 				}
 
 				// Do the revert
 				ew.getUndoController().revertByComment(comment);
 				// Save
 				ew.save();
 			}
 		} else {
 			File[] fileList = file.listFiles();
 			for (int i = 0; i < fileList.length; i++) {
 				process(fileList[i], comment);
 			}
 		}
 	}
 
 }
