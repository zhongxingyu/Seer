 package nnwl.jduplicatefinder.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Cursor;
 import java.awt.Desktop;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.net.URI;
 import java.nio.file.Path;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.Box;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JSplitPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JToolBar;
 import javax.swing.SwingConstants;
 import javax.swing.UIManager;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.EtchedBorder;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.TreePath;
 
 import nnwl.jduplicatefinder.engine.FileResult;
 import nnwl.jduplicatefinder.engine.ResultsSet;
 import nnwl.jduplicatefinder.engine.Runner;
 import nnwl.jduplicatefinder.engine.SimilarityResult;
 import nnwl.jduplicatefinder.engine.comparators.AbstractDuplicateComparator;
 import nnwl.jduplicatefinder.engine.event.RunnerAdapter;
 import nnwl.jduplicatefinder.engine.event.RunnerEvent;
 import nnwl.jduplicatefinder.engine.event.RunnerExceptionEvent;
 import nnwl.jduplicatefinder.ui.comparators.config.ComparatorConfigPanel;
 import nnwl.jduplicatefinder.ui.tree.ResultsTree;
 import nnwl.jduplicatefinder.ui.tree.ResultsTree.ExpanderEvent;
 import nnwl.jduplicatefinder.ui.tree.renderer.ResultsTreeCellRenderer;
 import nnwl.jduplicatefinder.util.Paths;
 import nnwl.rewrite.javax.swing.EnhancedProgressMonitor;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 /**
  * JDuplicateFinder
  *  
  * @author Anael Ollier <nanawel NOSPAM [at] gmail [dot] com>
  * @license GPLv3 - See LICENSE
  */
 public class App
 {
 	public static String APP_NAME = "JDuplicateFinder";
 
 	public static String APP_VERSION = "1.0";
 
 	private static final Logger logger = Logger.getLogger(App.class);
 
 	private File defaultDirectory = null;
 
 	private DecimalFormat timeDecimalFormatter = new DecimalFormat("0.000");
 
 	private JFrame frmMain;
 
 	private FileStackPanel fileStackPane;
 
 	private JLabel lblStatus;
 
 	private final Action actionRun = new RunAction();
 
 	private final Action actionClear = new ClearAction();
 
 	private ResultsTree treeLeft;
 
 	private ResultsTree treeRight;
 
 	protected ResultsTreeModel treeModel;
 
 	private Thread runnerThread;
 
 	private JCheckBox chkboxRecurseSubdirectories;
 
 	private JTabbedPane configTabbedPane;
 
 	protected JScrollPane scrollPaneLeft;
 
 	protected JScrollPane scrollPaneRight;
 
 	protected FileInfoPanel fileInfoPanel;
 
 	protected int firstComparatorTabIndex;
 
 	private FileFilterStackPanel fileFiltersPanel;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(final String[] args) {
 
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					App app = new App();
 
 					File[] paths = null;
 					if (args.length > 0) {
 						logger.debug("App launched with " + args.length + " arg(s)");
 						app.setDefaultFolderPath(args[0]);
 
 						paths = new File[args.length];
 						for (int i = 0; i < args.length; i++) {
 							paths[i] = new File(args[i]);
 						}
 					}
 					app.initialize();
 
 					if (paths != null) {
 						app.setTargetPaths(paths);
 						app.fileStackPane.addEmptyLine();
 					}
 
 					logger.debug("Displaying main window");
 					app.frmMain.setVisible(true);
 				}
 				catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the application.
 	 */
 	public App() {
 		PropertyConfigurator.configure("config/log4j.conf");
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		// Turn off metal's use of bold fonts
 		UIManager.put("swing.boldMetal", Boolean.FALSE);
 
 		frmMain = new JFrame();
 		frmMain.setIconImage(Toolkit.getDefaultToolkit().getImage(App.class.getResource("/icons/app48.png")));
 		frmMain.setTitle(APP_NAME);
 		frmMain.setBounds(100, 100, 784, 505);
 		frmMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frmMain.getContentPane().setLayout(new BorderLayout(0, 0));
 
 		// //////////
 		// TOOLBAR
 		JToolBar toolBar = new JToolBar();
 		frmMain.getContentPane().add(toolBar, BorderLayout.NORTH);
 
 		JButton toolbarbtnRun = new JButton("Run!");
 		toolbarbtnRun.setAction(actionRun);
 		toolbarbtnRun.setIcon(new ImageIcon(App.class.getResource("/icons/go-next.png")));
 		toolBar.add(toolbarbtnRun);
 
 		JButton btnClearResults = new JButton("Clear results");
 		btnClearResults.setAction(actionClear);
 		btnClearResults.setIcon(new ImageIcon(App.class.getResource("/icons/edit-clear.png")));
 		toolBar.add(btnClearResults);
 		
 		toolBar.add(Box.createHorizontalGlue());
 
 		final JLabel lblAbout = new JLabel("<html>" + APP_NAME + " v" + APP_VERSION
 				+ "<br/>nanawel@gmail.com</html>");
 		lblAbout.setForeground(Color.GRAY);
 		lblAbout.setPreferredSize(lblAbout.getPreferredSize());
 		lblAbout.setMaximumSize(lblAbout.getPreferredSize());
 		lblAbout.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				try {
 					Desktop.getDesktop().browse(new URI("http://lanterne-rouge.over-blog.org/"));
 				}
 				catch (Exception e1) {}
 			}
 
 			@Override
 			public void mouseEntered(MouseEvent e) {
 				lblAbout.setForeground(Color.BLUE);
 				lblAbout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 			}
 			
 			@Override
 			public void mouseExited(MouseEvent e) {
 				lblAbout.setForeground(Color.GRAY);
 				lblAbout.setCursor(Cursor.getDefaultCursor());
 			}
 		});
 		lblAbout.setToolTipText("Go to author's blog");
 		toolBar.add(lblAbout);
 		
 		// //////////
 		// MAIN PANEL
 		JPanel panel = new JPanel();
 		frmMain.getContentPane().add(panel, BorderLayout.CENTER);
 		panel.setLayout(new BorderLayout(2, 2));
 
 		// //////////
 		// STATUS BAR (South)
 		JPanel statusBar = new JPanel();
 		statusBar.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
 		panel.add(statusBar, BorderLayout.SOUTH);
 		GridBagLayout gbl_statusBar = new GridBagLayout();
 		gbl_statusBar.columnWidths = new int[] { 325, 0, 0, 0 };
 		gbl_statusBar.rowHeights = new int[] { 14, 0 };
 		gbl_statusBar.columnWeights = new double[] { 1.0, 0.0, 0.0, Double.MIN_VALUE };
 		gbl_statusBar.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
 		statusBar.setLayout(gbl_statusBar);
 
 		lblStatus = new JLabel(APP_NAME + " started");
 		lblStatus.setBorder(new EmptyBorder(0, 4, 0, 4));
 		GridBagConstraints gbc_lblStatus = new GridBagConstraints();
 		gbc_lblStatus.fill = GridBagConstraints.HORIZONTAL;
 		gbc_lblStatus.insets = new Insets(0, 0, 0, 5);
 		gbc_lblStatus.anchor = GridBagConstraints.NORTH;
 		gbc_lblStatus.gridx = 0;
 		gbc_lblStatus.gridy = 0;
 		statusBar.add(lblStatus, gbc_lblStatus);
 
 		JSeparator sepStatusBar = new JSeparator();
 		sepStatusBar.setForeground(Color.GRAY);
 		sepStatusBar.setOrientation(SwingConstants.VERTICAL);
 		sepStatusBar.setPreferredSize(new Dimension(1, 10));
 		GridBagConstraints gbc_sepStatusBar = new GridBagConstraints();
 		gbc_sepStatusBar.insets = new Insets(0, 4, 0, 5);
 		gbc_sepStatusBar.gridx = 1;
 		gbc_sepStatusBar.gridy = 0;
 		statusBar.add(sepStatusBar, gbc_sepStatusBar);
 
 		JLabel lblMemoryStatus = new JLabel("Memory Status");
 		lblMemoryStatus.setBorder(new EmptyBorder(0, 4, 0, 4));
 		GridBagConstraints gbc_lblMemoryStatus = new GridBagConstraints();
 		gbc_lblMemoryStatus.gridx = 2;
 		gbc_lblMemoryStatus.gridy = 0;
 		statusBar.add(lblMemoryStatus, gbc_lblMemoryStatus);
 
 		this.startMemoryWatcher(lblMemoryStatus);
 
 		// //////////
 		// TREES & FILE INFO
 		treeModel = new ResultsTreeModel();
 
 		treeLeft = this.createResultsTree(true);
 		scrollPaneLeft = new JScrollPane(treeLeft);
 
 		treeRight = this.createResultsTree(false);
 		scrollPaneRight = new JScrollPane(treeRight);
 
 		JPanel rightPane = new JPanel();
 		BorderLayout bl_rightPane = new BorderLayout();
 		rightPane.setLayout(bl_rightPane);
 
 		rightPane.add(scrollPaneRight, BorderLayout.CENTER);
 
 		JSplitPane resultsSplitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPaneLeft, rightPane);
 
 		fileInfoPanel = new FileInfoPanel();
 		rightPane.add(fileInfoPanel, BorderLayout.SOUTH);
 		resultsSplitpane.setResizeWeight(0.5);
 
 		// //////////
 		// CONFIG TABS
 		configTabbedPane = new JTabbedPane(JTabbedPane.TOP);
 
 		JPanel tabFolders = new JPanel();
 		tabFolders.setBorder(new EmptyBorder(5, 5, 2, 5));
 		configTabbedPane
 				.addTab("Folders", new ImageIcon(App.class.getResource("/icons/i16x16/folder.png")), tabFolders);
 		configTabbedPane.setEnabledAt(0, true);
 		GridBagLayout gbl_tabFolders = new GridBagLayout();
 		gbl_tabFolders.columnWidths = new int[] { 0, 4, 0, 0 };
 		gbl_tabFolders.rowHeights = new int[] { 80, 0 };
 		gbl_tabFolders.columnWeights = new double[] { 0.0, 0.0, 1.0, Double.MIN_VALUE };
 		gbl_tabFolders.rowWeights = new double[] { 1.0, 0.0 };
 		tabFolders.setLayout(gbl_tabFolders);
 
 		JLabel lblAnalyzeFolder = new JLabel("Analyze folders:");
 		GridBagConstraints gbc_lblAnalyzeFolder = new GridBagConstraints();
 		gbc_lblAnalyzeFolder.anchor = GridBagConstraints.NORTHEAST;
 		gbc_lblAnalyzeFolder.gridx = 0;
 		gbc_lblAnalyzeFolder.gridy = 0;
 		tabFolders.add(lblAnalyzeFolder, gbc_lblAnalyzeFolder);
 
 		JFileChooser fc = new JFileChooser();
 		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 		fileStackPane = new FileStackPanel(this.defaultDirectory, fc);
 		GridBagConstraints gbc_fileStackPane = new GridBagConstraints();
 		gbc_fileStackPane.gridx = 2;
 		gbc_fileStackPane.gridy = 0;
 		gbc_fileStackPane.fill = GridBagConstraints.BOTH;
 		gbc_fileStackPane.anchor = GridBagConstraints.NORTH;
 		gbc_lblAnalyzeFolder.anchor = GridBagConstraints.EAST;
 		gbc_lblAnalyzeFolder.gridx = 1;
 		gbc_lblAnalyzeFolder.gridy = 0;
 		gbc_lblAnalyzeFolder.weighty = 1.0;
 		gbc_lblAnalyzeFolder.weightx = 1.0;
 		gbc_lblAnalyzeFolder.gridheight = GridBagConstraints.REMAINDER;
 		tabFolders.add(fileStackPane, gbc_fileStackPane);
 
 		chkboxRecurseSubdirectories = new JCheckBox("Recurse subdirectories");
 		chkboxRecurseSubdirectories.setSelected(true);
 		GridBagConstraints gbc_chkboxRecurseSubdirectories = new GridBagConstraints();
 		gbc_chkboxRecurseSubdirectories.fill = GridBagConstraints.HORIZONTAL;
 		gbc_chkboxRecurseSubdirectories.anchor = GridBagConstraints.SOUTH;
 		gbc_chkboxRecurseSubdirectories.insets = new Insets(0, 0, 0, 0);
 		gbc_chkboxRecurseSubdirectories.gridx = 2;
 		gbc_chkboxRecurseSubdirectories.gridy = 1;
 		tabFolders.add(chkboxRecurseSubdirectories, gbc_chkboxRecurseSubdirectories);
 
 		JPanel tabFilters = new JPanel();
 		tabFilters.setBorder(new EmptyBorder(2, 2, 2, 2));
 		configTabbedPane.addTab("Filters", new ImageIcon(App.class.getResource("/icons/i16x16/view-filter.png")),
 				tabFilters, null);
 		tabFilters.setLayout(new BorderLayout(0, 0));
 
 		fileFiltersPanel = new FileFilterStackPanel();
 		tabFilters.add(fileFiltersPanel, BorderLayout.CENTER);
 
 		JSplitPane topBottomSplitpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, configTabbedPane, resultsSplitpane);
 		topBottomSplitpane.setResizeWeight(0.2);
 		panel.add(topBottomSplitpane, BorderLayout.CENTER);
 
 		firstComparatorTabIndex = 2;
 
 		@SuppressWarnings("rawtypes")
 		Class[] comparatorConfigPanelClasses = getComparatorsUiConfig();
 		for (int i = 0; i < comparatorConfigPanelClasses.length; i++) {
 			try {
 				ComparatorConfigPanel comparatorConfigPanel = (ComparatorConfigPanel) getComparatorsUiConfig()[i]
 						.newInstance();
 				configTabbedPane.insertTab(comparatorConfigPanel.getTitle(), comparatorConfigPanel.getIcon(),
 						(Component) comparatorConfigPanel, null, i + firstComparatorTabIndex);
 				configTabbedPane.setTabComponentAt(i + firstComparatorTabIndex,
 						new ComparatorTab(comparatorConfigPanel.getTitle(), comparatorConfigPanel.getIcon()));
 			}
 			catch (Exception e) {
 				logger.error("Cannot add comparator config panel: " + comparatorConfigPanelClasses[i], e);
 				e.printStackTrace();
 			}
 		}
 	}
 
 	protected ResultsTree createResultsTree(boolean isLeft) {
 		ResultsTree tree = new ResultsTree();
 		tree.setBorder(new EmptyBorder(2, 2, 2, 2));
 		tree.setModel(treeModel);
 		tree.setCellRenderer(new ResultsTreeCellRenderer());
 		tree.addMouseListener(new ResultsTreeMouseListener());
 		// if (isLeft) {
 		tree.addTreeSelectionListener(new ResultsTreeSelectionListener());
 		// }
 		return tree;
 	}
 
 	@SuppressWarnings("rawtypes")
 	public static Class[] getComparatorsUiConfig() {
 		return new Class[] {
 				nnwl.jduplicatefinder.ui.comparators.config.Filesize.class,
 				nnwl.jduplicatefinder.ui.comparators.config.Digest.class,
 				nnwl.jduplicatefinder.ui.comparators.config.DateTime.class };
 	}
 
 	public void setDefaultFolderPath(String path) {
 		try {
 			this.defaultDirectory = new File(path);
 		}
 		catch (Exception e) {
 			/* just ignore */
 		}
 	}
 
 	public void setTargetPaths(File[] paths) {
 		this.fileStackPane.clearPaths();
 		for (int i = 0; i < paths.length; i++) {
 			if (paths[i].isDirectory()) {
 				this.fileStackPane.addPath(paths[i]);
 			}
 		}
 	}
 
 	public File[] getTargetPaths() {
 		return fileStackPane.getFiles();
 	}
 
 	public void run() throws Exception {
 		// Clean memory a little
 		this.clearResults();
 
 		ArrayList<File> folders = new ArrayList<File>();
 
 		for (File f : this.getTargetPaths()) {
 			if (!f.isDirectory() || !f.canRead()) {
 				JOptionPane.showMessageDialog(this.frmMain, "Invalid path:<br/>" + f.getAbsolutePath()
 						+ "<br/>is not a directory or is not readable", "Invalid path", JOptionPane.ERROR_MESSAGE,
 						new ImageIcon(App.class.getResource("/icons/i32x32/dialog-error.png")));
 				this.setStatusMessage("Invalid path: not a directory or not readable");
 				return;
 			}
 			folders.add(f);
 		}
 		if (folders.isEmpty()) {
 			JOptionPane.showMessageDialog(this.frmMain, "Please select a search folder.", "Missing folder",
 					JOptionPane.INFORMATION_MESSAGE,
 					new ImageIcon(App.class.getResource("/icons/i32x32/dialog-information.png")));
 			return;
 		}
 
 		ArrayList<AbstractDuplicateComparator> comparators = new ArrayList<AbstractDuplicateComparator>();
 		for (int t = this.firstComparatorTabIndex; t < this.configTabbedPane.getTabCount(); t++) {
 			ComparatorConfigPanel comparatorConfigPanel = (ComparatorConfigPanel) this.configTabbedPane
 					.getComponentAt(t);
 
 			if (((ComparatorTab) this.configTabbedPane.getTabComponentAt(t)).isChecked()) {
 				AbstractDuplicateComparator comparator = comparatorConfigPanel.getConfiguredComparatorInstance();
 				comparators.add(comparator);
 			}
 		}
 
 		if (comparators.isEmpty()) {
 			JOptionPane.showMessageDialog(this.frmMain, "Please select at least one comparator.", "Missing comparator",
 					JOptionPane.WARNING_MESSAGE,
 					new ImageIcon(App.class.getResource("/icons/i32x32/dialog-warning.png")));
 			return;
 		}
 
 		// Initialize and configure runner
 		Runner r = new Runner(folders, comparators);
 		r.setFileFilters(this.fileFiltersPanel.getFilters());
 		Map<String, Object> parameters = new HashMap<String, Object>();
 		parameters.put("runner.recurse_subdirectories", String.valueOf(this.chkboxRecurseSubdirectories.isSelected()));
 		r.configure(parameters);
 		r.addListener(new RunnerListener());
 
 		// Launch runner in a dedicated thread
 		runnerThread = new Thread(r, "Runner");
 		runnerThread.start();
 	}
 
 	public void clearResults() {
 		this.fileInfoPanel.clear();
 		this.treeModel.resetResults();
 		this.treeModel.reload();
 		System.gc();
 		
 		this.setStatusMessage("Press Run to search again");
 		logger.info("Results cleared");
 	}
 
 	public void setStatusMessage(String message) {
 		this.lblStatus.setText(message);
 	}
 
 	private void startMemoryWatcher(JLabel lblMemoryStatus) {
 		Thread t = new Thread(new MemoryWatcher(lblMemoryStatus), "MemoryWatcher");
 		t.start();
 	}
 
 	private class RunAction extends AbstractAction
 	{
 		private static final long serialVersionUID = -1013884528373811083L;
 
 		public RunAction() {
 			putValue(NAME, "Run");
 			putValue(SHORT_DESCRIPTION, "Run duplicate comparators on the selected directory");
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			try {
 				App.this.run();
 			}
 			catch (Exception ex) {
 				ex.printStackTrace();
 				JOptionPane.showMessageDialog(App.this.frmMain, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
 			}
 		}
 	}
 
 	private class ClearAction extends AbstractAction
 	{
 		private static final long serialVersionUID = 1L;
 
 		public ClearAction() {
 			putValue(NAME, "Clear results");
 			putValue(SHORT_DESCRIPTION, "Clear all results");
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			App.this.clearResults();
 		}
 	}
 
 	private class RunnerListener extends RunnerAdapter
 	{
 		private final Logger logger = Logger.getLogger(RunnerListener.class);
 
 		EnhancedProgressMonitor progressMonitor = null;
 
 		public RunnerListener() {
 			this.progressMonitor = new EnhancedProgressMonitor(App.this.frmMain, "", "Comparing files...", 0, 0);
 			this.progressMonitor.setAutoClose(false);
 		}
 
 		@Override
 		public void statusChanged(RunnerEvent ev) {
 			switch (ev.getStatus()) {
 				case Runner.STATUS_INTERRUPTED:
 					logger.info("Runner has been interrupted");
 					App.this.setStatusMessage("Scan canceled");
 					break;
 
 				case Runner.STATUS_COMPLETED:
 					String processTime = App.this.timeDecimalFormatter
 							.format((float) (ev.getRunner().getEndTimestamp() - ev.getRunner().getStartTimestamp()) / 1000);
 					logger.info("Runner completed (" + processTime + " seconds)");
 					App.this.setStatusMessage("Displaying results...");
 					this.progressMonitor.setNote("Analyze complete. Preparing results...");
 					this.progressMonitor.setIndeterminate(true);
 
 					ResultsSet results = ev.getRunner().getResults();
 					App.this.treeModel.resetResults();
 
 					Path[] targetPaths = Paths.commonPathsByRoot(App.this.getTargetPaths());
 					App.this.treeModel.setResults(targetPaths, results);
 
 					logger.info("Updating tree display...");
 					long drawTreeTimeStart = System.currentTimeMillis();
 
 					// Create an expanded tree in background before displaying it
 					// (avoids flickering + speeds up rendering X2 approx.)
 					ResultsTree newLeftTree = createResultsTree(true);
 
 					this.progressMonitor.setIndeterminate(false);
 					final ResultsTree.Expander expander = newLeftTree.new Expander();
 					this.progressMonitor.setMaximum(App.this.treeModel.getTotalNodesCount());
 					expander.addListener(new ResultsTree.ExpanderListener() {
 						@Override
 						public void expandProgress(ExpanderEvent ev) {
 							if (RunnerListener.this.progressMonitor.isCanceled()) {
 								expander.interrupt();
 								App.this.setStatusMessage("Results tree expansion canceled");
 								return;
 							}
 
 							RunnerListener.this.progressMonitor.setProgress(ev.getCurrentRow());
 							RunnerListener.this.progressMonitor.setNote("<html>Expanding results tree... ("
 									+ ev.getProgressPercent() + "%)<br/>Press Cancel to skip.</html>");
 						}
 					});
 					Thread expanderThread = new Thread(expander, "ResultsTree-Expander");
 					expanderThread.start();
 					try {
 						expanderThread.join();
 					}
 					catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 
 					App.this.scrollPaneLeft.setViewportView(newLeftTree);
 					App.this.treeLeft = newLeftTree;
 
 					App.this.treeRight.expand(1);
 					long drawTreeTimeEnd = System.currentTimeMillis();
 
 					String displayTime = App.this.timeDecimalFormatter
 							.format((float) (drawTreeTimeEnd - drawTreeTimeStart) / 1000);
 					logger.info("Tree display update complete (" + displayTime + " seconds)");
 
 					this.progressMonitor.close();
 
 					if (results.isEmpty()) {
 						logger.info("No similar files found (" + ev.getRunner().getFiles().size() + " total files)");
 						JOptionPane.showMessageDialog(App.this.frmMain, "No similar files found.", "Scan complete",
 								JOptionPane.INFORMATION_MESSAGE);
 						StringBuffer s = new StringBuffer()
 								.append("<html>Finished: <b>No similar files found.</b> in a total of <b>")
 								.append(ev.getRunner().getFiles().size()).append(" files</b> analyzed in ")
 								.append(processTime).append(" seconds</html>");
 						App.this.setStatusMessage(s.toString());
 					}
 					else {
 						StringBuffer s = new StringBuffer().append("<html>Finished: <b>").append(results.size())
 								.append(" positive results</b> found in a total of <b>")
 								.append(ev.getRunner().getFiles().size()).append(" files</b> in ").append(processTime)
 								.append(" seconds (+").append(displayTime)
 								.append(" seconds to display the results tree)</html>");
 						App.this.setStatusMessage(s.toString());
 					}
 					break;
 			}
 		}
 
 		@Override
 		public void fileScanStarted(RunnerEvent ev) {
 			App.this.setStatusMessage("Scanning files...");
 			this.progressMonitor.setNote("Please wait while analyzing folder...");
 			this.progressMonitor.setModal(true);
 			this.progressMonitor.setIndeterminate(true);
 		}
 
 		@Override
 		public void fileScanProgressed(RunnerEvent ev) {
 			if (this.progressMonitor.isCanceled()) {
 				ev.getRunner().interrupt();
 				App.this.setStatusMessage("Scan canceled");
 				return;
 			}
 			App.this.setStatusMessage("Scanning files : " + ev.getCurrentFilesCount() + " files found");
 			this.progressMonitor.setNote("Scannning files... (" + ((int) ev.getCurrentFilesCount()) + " files found)");
 			this.progressMonitor.setProgress(0);
 		}
 
 		@Override
 		public void fileScanCompleted(RunnerEvent ev) {
 			App.this.setStatusMessage("Files scan complete");
 		}
 
 		@Override
 		public void comparatorsFilesAnalyzeStarted(RunnerEvent ev) {
 			App.this.setStatusMessage("Running selected comparators...");
 			this.progressMonitor.setIndeterminate(false);
 			this.progressMonitor.setMaximum((int) ev.getTotalFilesCount());
 			this.progressMonitor.setProgress(0);
 		}
 
 		@Override
 		public void comparatorsFilesAnalyzeProgressed(RunnerEvent ev) {
 			if (this.progressMonitor.isCanceled()) {
 				ev.getRunner().interrupt();
 				App.this.setStatusMessage("Scan canceled");
 				return;
 			}
 
 			this.progressMonitor.setProgress((int) ev.getCurrentFilesCount());
 			this.progressMonitor.setNote("Analyzing files... "
 					+ (Math.round((float) ev.getCurrentFilesCount() / (float) ev.getTotalFilesCount() * 100)) + "%");
 		}
 
 		@Override
 		public void comparatorFilesComparisonStarted(RunnerEvent ev) {
 			App.this.setStatusMessage("Comparing files...");
 			this.progressMonitor.setIndeterminate(false);
 			this.progressMonitor.setMaximum((int) ev.getTotalFilesCount());
 			this.progressMonitor.setProgress(0);
 		}
 
 		@Override
 		public void comparatorFilesComparisonProgressed(RunnerEvent ev) {
 			if (this.progressMonitor.isCanceled()) {
 				ev.getRunner().interrupt();
 				App.this.setStatusMessage("Scan canceled");
 				return;
 			}
 
 			this.progressMonitor.setProgress((int) ev.getCurrentFilesCount());
 			String message = ev.getLabel() != null ? "Comparing files (" + ev.getLabel() + ")... "
 					: "Comparing files... ";
 			this.progressMonitor.setNote(message
 					+ (Math.round((float) ev.getCurrentFilesCount() / (float) ev.getTotalFilesCount() * 100)) + "%");
 		}
 
 		@Override
 		public void exceptionCaugth(RunnerExceptionEvent ev) {
 			this.progressMonitor.close();
 			App.this.setStatusMessage("An error occured.");
 			String message = ev.getCause().getMessage() != null ? ev.getCause().getMessage() : ev.getCause().getClass()
 					.getName();
 			JOptionPane.showMessageDialog(App.this.frmMain, message, "Error", JOptionPane.ERROR_MESSAGE);
 		}
 	}
 
 	public class ResultsTreeMouseListener extends MouseAdapter
 	{
 		private final Logger logger = Logger.getLogger(ResultsTreeMouseListener.class);
 
 		JPopupMenu similarFilesContextMenu;
 		JPopupMenu fileResultContextMenu;
 
 		DefaultMutableTreeNode selectedNode;
 
 		public ResultsTreeMouseListener() {
 			// SimilarFile Node
 			this.similarFilesContextMenu = new JPopupMenu();
 			JMenuItem browseToItem = new JMenuItem("Open in file browser", new ImageIcon(
 					ResultsTreeMouseListener.class.getResource("/icons/i16x16/folder-open.png")));
 			browseToItem.addActionListener(new OpenInFileBrowserAction());
 			this.similarFilesContextMenu.add(browseToItem);
 			this.similarFilesContextMenu.addSeparator();
 			JMenuItem deleteItem = new JMenuItem("Delete", new ImageIcon(
 					ResultsTreeMouseListener.class.getResource("/icons/i16x16/edit-delete-shred.png")));
 			deleteItem.addActionListener(new DeleteFileAction());
 			this.similarFilesContextMenu.add(deleteItem);
 
 			// FileResult Node
 			this.fileResultContextMenu = new JPopupMenu();
 			browseToItem = new JMenuItem("Open in file browser", new ImageIcon(
 					ResultsTreeMouseListener.class.getResource("/icons/i16x16/folder-open.png")));
 			browseToItem.addActionListener(new OpenInFileBrowserAction());
 			this.fileResultContextMenu.add(browseToItem);
 			this.fileResultContextMenu.addSeparator();
 			deleteItem = new JMenuItem("Delete", new ImageIcon(
 					ResultsTreeMouseListener.class.getResource("/icons/i16x16/edit-delete-shred.png")));
 			deleteItem.addActionListener(new DeleteFileAction());
 			this.fileResultContextMenu.add(deleteItem);
 			JMenuItem deleteAllSimilarItem = new JMenuItem("Delete all similar files", new ImageIcon(
 					ResultsTreeMouseListener.class.getResource("/icons/i16x16/edit-delete-shred.png")));
 			deleteAllSimilarItem.addActionListener(new DeleteAllSimilarFilesAction());
 			this.fileResultContextMenu.add(deleteAllSimilarItem);
 			this.fileResultContextMenu.addSeparator();
 			JMenuItem deleteFileAndAllSimilarItem = new JMenuItem("Delete this file and all similar", new ImageIcon(
 					ResultsTreeMouseListener.class.getResource("/icons/i16x16/edit-delete-shred-all.png")));
 			deleteFileAndAllSimilarItem.addActionListener(new DeleteFileAndAllSimilarFilesAction());
 			this.fileResultContextMenu.add(deleteFileAndAllSimilarItem);
 		}
 
 		protected int showConfirmDeleteDialog(File f) {
 			int choice = JOptionPane.showConfirmDialog(App.this.frmMain,
 					"<html>Are you sure you want to delete this file?<br/><br/>" + f.getAbsolutePath() + "</html>",
 					"File deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, new ImageIcon(
 							ResultsTreeMouseListener.class.getResource("/icons/i32x32/dialog-warning.png")));
 			return choice;
 		}
 
 		protected int showConfirmDeleteDialog(File f, String msg) {
 			int choice = JOptionPane.showConfirmDialog(App.this.frmMain, "<html>" + msg + "</html>", "File deletion",
 					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, new ImageIcon(
 							ResultsTreeMouseListener.class.getResource("/icons/i32x32/dialog-warning.png")));
 			return choice;
 		}
 
 		protected boolean deleteFileAndTreeNode(File f, DefaultMutableTreeNode node) {
 			logger.debug("Deleting file: " + f.getAbsolutePath());
 
 			if (f.delete()) {
 				App.this.treeModel.removeFileNodes(f);
 				logger.info("File deleted: " + f.getAbsolutePath());
 			}
 			else {
 				logger.error("Cannot delete file: " + f.getAbsolutePath());
 				JOptionPane.showMessageDialog(App.this.frmMain, "Cannot delete file.", "Error",
 						JOptionPane.ERROR_MESSAGE,
 						new ImageIcon(ResultsTreeMouseListener.class.getResource("/icons/i32x32/dialog-error.png")));
 				return false;
 			}
 			return true;
 		}
 
 		@Override
 		public void mousePressed(MouseEvent e) {
 			if (e.isPopupTrigger()) {
 				this.popupMenu(e);
 			}
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent e) {
 			if (e.isPopupTrigger()) {
 				this.popupMenu(e);
 			}
 		}
 
 		protected File getEventTargetFile() {
 			File targetFile = null;
 			if (selectedNode.getUserObject() instanceof SimilarityResult) {
 				SimilarityResult sr = ((SimilarityResult) selectedNode.getUserObject());
 				targetFile = sr.getSimilarFile();
 			}
 			else if (selectedNode.getUserObject() instanceof FileResult) {
 				FileResult fr = ((FileResult) selectedNode.getUserObject());
 				targetFile = fr.getReferenceFile();
 			}
 			else {
 				logger.error("Invalid user object: " + selectedNode.getUserObject());
 			}
 			return targetFile;
 		}
 
 		private void popupMenu(MouseEvent e) {
 			ResultsTree tree = (ResultsTree) e.getSource();
 			TreePath tp = tree.getPathForLocation(e.getX(), e.getY());
 			tree.setSelectionPath(tp);
 
 			if (tp != null) {
 				this.selectedNode = (DefaultMutableTreeNode) tp.getLastPathComponent();
 				if (this.selectedNode.getUserObject() instanceof FileResult) {
 					this.fileResultContextMenu.show(tree, e.getX(), e.getY());
 				}
 				else if (this.selectedNode.getUserObject() instanceof SimilarityResult) {
 					this.similarFilesContextMenu.show(tree, e.getX(), e.getY());
 				}
 				else {
 					this.selectedNode = null;
 				}
 			}
 		}
 
 		protected final class OpenInFileBrowserAction implements ActionListener
 		{
 			@Override
 			public void actionPerformed(ActionEvent ev) {
 				File targetFile = getEventTargetFile();
 
 				Desktop desktop = Desktop.getDesktop();
 				try {
 					desktop.browse(targetFile.getParentFile().toURI());
 				}
 				catch (Exception e) {
 					logger.error(e.getMessage(), e);
 					JOptionPane
 							.showMessageDialog(
 									App.this.frmMain,
 									"Cannot open file browser.",
 									"Error",
 									JOptionPane.ERROR_MESSAGE,
 									new ImageIcon(ResultsTreeMouseListener.class
 											.getResource("/icons/i16x16/dialog-error.png")));
 				}
 			}
 		}
 
 		protected final class DeleteFileAction implements ActionListener
 		{
 			@Override
 			public void actionPerformed(ActionEvent ev) {
 				File targetFile = getEventTargetFile();
 
 				int choice = ResultsTreeMouseListener.this.showConfirmDeleteDialog(targetFile);
 				if (choice == JOptionPane.YES_OPTION) {
 					ResultsTreeMouseListener.this.deleteFileAndTreeNode(targetFile, selectedNode);
 				}
 			}
 		}
 
 		protected final class DeleteAllSimilarFilesAction implements ActionListener
 		{
 			@Override
 			public void actionPerformed(ActionEvent ev) {
 				File targetFile = getEventTargetFile();
 
 				int choice = ResultsTreeMouseListener.this.showConfirmDeleteDialog(
 						targetFile,
 						"Are you sure you want to delete all files similar to this one?<br/>"
 								+ targetFile.getAbsolutePath() + "<br/>(" + selectedNode.getChildCount()
 								+ " file(s) will be deleted)");
 
 				int filesCount = selectedNode.getChildCount();
 				int filesDeleted = 0;
 				if (choice == JOptionPane.YES_OPTION) {
 					for (int i = filesCount - 1; i >= 0; i--) {
 						DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) selectedNode.getChildAt(i);
 						SimilarityResult sr = ((SimilarityResult) childNode.getUserObject());
 						if (ResultsTreeMouseListener.this.deleteFileAndTreeNode(sr.getSimilarFile(), childNode)) {
 							filesDeleted++;
 						}
 					}
 					if (filesCount == filesDeleted) {
 						JOptionPane.showMessageDialog(
 								App.this.frmMain,
 								"<html>" + filesCount + " file(s) deleted successfully</html>",
 								"File deletion",
 								JOptionPane.INFORMATION_MESSAGE,
 								new ImageIcon(ResultsTreeMouseListener.class
 										.getResource("/icons/i32x32/dialog-information.png")));
 					}
 					else {
 						JOptionPane.showMessageDialog(
 								App.this.frmMain,
 								"<html>" + filesDeleted + " file(s) deleted successfully.<br/>"
 										+ (filesCount - filesDeleted) + " could not be deleted.</html>",
 								"File deletion",
 								JOptionPane.WARNING_MESSAGE,
 								new ImageIcon(ResultsTreeMouseListener.class
 										.getResource("/icons/i32x32/dialog-warning")));
 					}
 				}
 			}
 		}
 
 		protected final class DeleteFileAndAllSimilarFilesAction implements ActionListener
 		{
 			@Override
 			public void actionPerformed(ActionEvent ev) {
 				File targetFile = getEventTargetFile();
 
 				int choice = ResultsTreeMouseListener.this.showConfirmDeleteDialog(
 						targetFile,
 						"Are you sure you want to delete all files similar to this one?<br/>"
 								+ targetFile.getAbsolutePath() + "<br/>(" + (selectedNode.getChildCount() + 1)
 								+ " file(s) will be deleted)");
 
 				int filesCount = selectedNode.getChildCount() + 1;
 				int filesDeleted = 0;
 				if (choice == JOptionPane.YES_OPTION) {
 					for (int i = filesCount - 2; i >= 0; i--) {
 						DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) selectedNode.getChildAt(i);
 						SimilarityResult sr = ((SimilarityResult) childNode.getUserObject());
 						if (ResultsTreeMouseListener.this.deleteFileAndTreeNode(sr.getSimilarFile(), childNode)) {
 							filesDeleted++;
 						}
 					}
 					if (ResultsTreeMouseListener.this.deleteFileAndTreeNode(targetFile, selectedNode)) {
 						filesDeleted++;
 					}
 					if (filesCount == filesDeleted) {
 						JOptionPane.showMessageDialog(
 								App.this.frmMain,
 								"<html>" + filesCount + " file(s) deleted successfully</html>",
 								"File deletion",
 								JOptionPane.INFORMATION_MESSAGE,
 								new ImageIcon(ResultsTreeMouseListener.class
 										.getResource("/icons/i32x32/dialog-information.png")));
 					}
 					else {
 						JOptionPane.showMessageDialog(
 								App.this.frmMain,
 								"<html>" + filesDeleted + " file(s) deleted successfully.<br/>"
 										+ (filesCount - filesDeleted) + " could not be deleted.</html>",
 								"File deletion",
 								JOptionPane.WARNING_MESSAGE,
 								new ImageIcon(ResultsTreeMouseListener.class
 										.getResource("/icons/i32x32/dialog-warning")));
 					}
 				}
 			}
 		}
 	}
 
 	private class ResultsTreeSelectionListener implements TreeSelectionListener
 	{
 		@Override
 		public void valueChanged(TreeSelectionEvent e) {
			final ResultsTree sourceTree = (ResultsTree) e.getSource();
			final ResultsTree otherTree = this.getOtherTree(sourceTree);
			//final JScrollPane otherScrollPane = this.getOtherScrollpane(sourceTree);
 
 			DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) sourceTree.getLastSelectedPathComponent();
 			if (selectedNode == null) {
 				return;
 			}
 
 			if (selectedNode.getUserObject() instanceof FileResult) {
 				File referenceFile = ((FileResult) selectedNode.getUserObject()).getReferenceFile();
 				App.this.fileInfoPanel.display(referenceFile);
 
 				// TODO more?
 			}
 			else if (selectedNode.getUserObject() instanceof SimilarityResult) {
 				File similarFile = ((SimilarityResult) selectedNode.getUserObject()).getSimilarFile();
 				App.this.fileInfoPanel.display(similarFile);
 
 				DefaultMutableTreeNode similarFileNode = App.this.treeModel.getNodeFromPath(similarFile);
 
				final TreePath path = new TreePath(similarFileNode.getPath());
 				otherTree.collapse();
 				otherTree.setSelectionPath(path);
 				otherTree.expandPath(path);
 				
				// Scroll to that node
				// TODO Improve that
				Rectangle bounds = otherTree.getPathBounds(path);
				bounds.height = otherTree.getVisibleRect().height;
				otherTree.scrollRectToVisible(bounds);
 			}
 			else {
 				App.this.fileInfoPanel.clear();
 			}
 		}
 
 		private ResultsTree getOtherTree(ResultsTree t) {
 			return App.this.treeLeft == t ? App.this.treeRight : App.this.treeLeft;
 		}
 
//		private JScrollPane getOtherScrollpane(ResultsTree t) {
//			return App.this.treeLeft == t ? App.this.scrollPaneRight : App.this.scrollPaneLeft;
//		}
 	}
 }
