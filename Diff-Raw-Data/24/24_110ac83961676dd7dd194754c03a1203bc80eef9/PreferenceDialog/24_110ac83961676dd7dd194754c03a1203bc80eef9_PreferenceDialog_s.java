 package usr.erichschroeter.jpreferences;
 
 import java.awt.BorderLayout;
 import java.awt.Dialog;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Window;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.prefs.BackingStoreException;
 import java.util.prefs.Preferences;
 
 import javax.imageio.ImageIO;
 import javax.swing.AbstractAction;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTextField;
 import javax.swing.JTree;
 import javax.swing.KeyStroke;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeCellRenderer;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.MutableTreeNode;
 
 import usr.erichschroeter.jpreferences.page.CustomPage;
 import usr.erichschroeter.jpreferences.page.Page;
 import usr.erichschroeter.jpreferences.page.PreferencePage;
 
 /**
  * A <code>PreferenceDialog</code> provides a graphical interface for users to
  * interact with preferences. The default behavior will display
  * {@link Preferences} nodes passed to the constructors.
  * 
  * @author Erich Schroeter, help from
  *         http://www.roseindia.net/javatutorials/javaapi.shtml
  */
 @SuppressWarnings("serial")
 public class PreferenceDialog extends JDialog {
 
 	/** The preference node tree the user interacts with to view preferences. */
 	private JTree tree;
 	/** The table displaying the preferences and their values. */
 	private CustomPage<?> page;
 	/**
 	 * A reference to the page that displays a table of {@link Preferences}.
 	 * This is kept as a single reference, as opposed to being added to
 	 * {@link #customNodeMap}, for performance.
 	 */
 	private PreferencePage preferencePage;
 	/** The root preference nodes to be displayed in the {@link #tree}. */
 	private Preferences[] preferences;
 	/** The list of custom preference pages. */
 	private Map<MutableTreeNode, CustomPage<?>> customNodeMap;
 	/** A mapping of the custom pages to their respective node. */
 	private Map<CustomPage<?>, MutableTreeNode> customPageMap;
 	/**
 	 * The left is the {@link #tree}, right is the {@link #page} or
 	 * {@link #preferencePage}.
 	 */
 	private JSplitPane splitPane;
 
 	/** Whether the search feature is enabled or disabled. */
 	private boolean searchEnabled;
 	/** Whether custom pages are allowed to be added. */
 	private boolean customPagesEnabled;
 	/** Whether the escape key is bound to close the dialog. */
 	private boolean escapeToCloseEnabled;
 	/** Whether the page will be wrapped in a <code>JScrollPane</code>. */
 	private boolean wrapPageInScrollPaneEnabled;
 
 	/** The action that handles closing the dialog. */
 	private AbstractAction closeAction = new AbstractAction() {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			setVisible(false);
 			dispose();
 		}
 	};
 
 	/** A blank page to fall back on. */
 	class BlankPage extends CustomPage<JPanel> {
 
 		public BlankPage() {
 			super("");
 		}
 
 		@Override
 		protected void initializePage(JPanel page) {
 			// keep blank
 		}
 
 	}
 
 	// TODO implement search for preferences and values
 
 	/**
 	 * Creates a <code>PreferenceDialog</code> calling
 	 * {@link JDialog#JDialog(Dialog, boolean)}.
 	 * 
 	 * @param parent
 	 *            the <code>Dialog</code> from which the dialog is displayed or
 	 *            <code>null</code> if this dialog has no owner
 	 * @param preferences
 	 *            the root preference nodes
 	 */
 	public PreferenceDialog(Dialog parent, Preferences... preferences) {
 		super(parent, true);
 		setPreferences(preferences);
 		initializeDialog();
 	}
 
 	/**
 	 * Creates a <code>PreferenceDialog</code> calling
	 * {@link JDialog#JDialog(Window, boolean)}.
 	 * 
 	 * @param parent
 	 *            the <code>Window</code> from which the dialog is displayed or
 	 *            <code>null</code> if this dialog has no owner
	 * @param conf
	 *            the configuration
 	 * @param preferences
 	 *            the root preference nodes
 	 */
 	public PreferenceDialog(Window parent, Preferences... preferences) {
 		super(parent, ModalityType.DOCUMENT_MODAL);
 		setPreferences(preferences);
 		initializeDialog();
 	}
 
	/**
	 * Initializes the User Interface (UI) for this dialog. A left panel
	 * contains a tree hierarchy; when the user selects a node the
	 * {@link #editTable} displays the preferences for that node.
	 */
 	protected void initializeDialog() {
 		try {
			setIconImage(ImageIO.read(PreferenceDialog.class.getClassLoader()
					.getResourceAsStream(
							"usr/erichschroeter/jpreferences/png/preferences.png")));
 		} catch (IOException e) {
 			// let system use default image
 		}
 		setLayout(new BorderLayout());
 		setTitle("Preferences Dialog");
 		setMinimumSize(new Dimension(400, 400));
 
 		customNodeMap = new HashMap<MutableTreeNode, CustomPage<?>>();
 		customPageMap = new HashMap<CustomPage<?>, MutableTreeNode>();
 		preferencePage = new PreferencePage(preferences[0]);
 
 		setEscapeToCloseEnabled(true);
 		setSearchEnabled(false);
 		setCustomPagesEnabled(false);
 		setWrapPageInScrollPaneEnabled(true);
 
 		// a default page
 		page = new BlankPage();
 
 		//
 		// TreePanel -- panel containing the tree hierarchy
 		//
 		JPanel treePanel = new JPanel(new BorderLayout());
 		if (isSearchEnabled()) {
 			JTextField searchField = new JTextField("Search...");
 			treePanel.add(searchField, BorderLayout.NORTH);
 		}
 		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
 		renderer.setOpenIcon(null);
 		renderer.setLeafIcon(null);
 		renderer.setClosedIcon(null);
 
 		tree = createTree(true, false, getPreferences());
 		tree.setCellRenderer(renderer);
 		treePanel.add(new JScrollPane(tree), BorderLayout.CENTER);
 
 		// Buttons
 		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
 		JButton closeButton = new JButton("Close");
 		closeButton.addActionListener(closeAction);
 		buttonPanel.add(closeButton);
 
 		splitPane = new JSplitPane();
 		splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
 		splitPane.setResizeWeight(0.3); // tree doesn't need more than 30%
 		splitPane.setLeftComponent(treePanel);
 		splitPane.setRightComponent(new JScrollPane(page.getPage()));
 		add(splitPane, BorderLayout.CENTER);
 		add(buttonPanel, BorderLayout.SOUTH);
 
 		setLocationRelativeTo(getParent());
 		pack();
 	}
 
 	/**
 	 * Returns whether the search feature is enabled or disabled. This feature
 	 * displays a search box and filters the preference nodes based on the
 	 * search result.
 	 * 
 	 * @see #setSearchEnabled(boolean)
 	 * @return <code>true</code> if the feature is enabled, else
 	 *         <code>false</code>
 	 */
 	public boolean isSearchEnabled() {
 		return searchEnabled;
 	}
 
 	/**
 	 * Enables or disables the search feature. This feature displays a search
 	 * box and filters the preference nodes based on the search result.
 	 * 
 	 * @see #isSearchEnabled()
 	 * @param enable
 	 *            <code>true</code> to enable the feature, <code>false</code> to
 	 *            disable
 	 */
 	public void setSearchEnabled(boolean enable) {
 		searchEnabled = enable;
 	}
 
 	/**
 	 * Returns whether the custom pages feature is enabled or disabled. This
 	 * feature enables/disables the ability to add custom preference pages.
 	 * 
 	 * @see #setCustomPagesEnabled(boolean)
 	 * @return <code>true</code> if the feature is enabled, else
 	 *         <code>false</code>
 	 */
 	public boolean isCustomPagesEnabled() {
 		return customPagesEnabled;
 	}
 
 	/**
 	 * Enables or disables the custom pages feature. This feature
 	 * enables/disables the ability to add custom preference pages.
 	 * 
 	 * @see #isCustomPagesEnabled()
 	 * @param enable
 	 *            <code>true</code> to enable the feature, <code>false</code> to
 	 *            disable
 	 */
 	public void setCustomPagesEnabled(boolean enable) {
 		this.customPagesEnabled = enable;
 	}
 
 	/**
 	 * Returns whether the escape key to close feature is enabled or disabled.
 	 * This feature enables/disables the ability to close the dialog with the
 	 * escape key.
 	 * 
 	 * @return <code>true</code> if the feature is enabled, else
 	 *         <code>false</code>
 	 */
 	public boolean isEscapeToCloseEnabled() {
 		return escapeToCloseEnabled;
 	}
 
 	/**
 	 * Enables or disables the escape key to close feature. This feature
 	 * enables/disables the ability to close the dialog with the escape key.
 	 * 
 	 * @param enable
 	 *            <code>true</code> to enable the feature, <code>false</code> to
 	 *            disable
 	 */
 	public void setEscapeToCloseEnabled(boolean enable) {
 		this.escapeToCloseEnabled = enable;
 		KeyStroke escapeStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
 		if (enable) {
 			// bind Escape key to close the dialog
 			getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
 					escapeStroke, escapeStroke.toString());
 			getRootPane().getActionMap().put(escapeStroke.toString(),
 					closeAction);
 		} else {
 			// unbind Escape key from closing the dialog
 			getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
 					.remove(escapeStroke);
 			getRootPane().getActionMap().remove(escapeStroke.toString());
 		}
 	}
 
 	/**
 	 * Returns whether the custom page will be wrapped in a
 	 * <code>JScrollPane</code> feature is enabled or disabled. This feature
 	 * will wrap the selected page in the right component of the
 	 * {@link #splitPane} in a <code>JScrollPane</code> if enabled, or add the
 	 * custom page component directly.
 	 * 
 	 * @return <code>true</code> if the feature is enabled, else
 	 *         <code>false</code>
 	 */
 	public boolean isWrapPageInScrollPaneEnabled() {
 		return wrapPageInScrollPaneEnabled;
 	}
 
 	/**
 	 * Enables or disables the custom page wrapped in a <code>JScrollPane</code>
 	 * feature. This feature will wrap the selected page in the right component
 	 * of the {@link #splitPane} in a <code>JScrollPane</code> if enabled, or
 	 * add the custom page component directly.
 	 * 
 	 * @param enable
 	 *            <code>true</code> to enable the feature, <code>false</code> to
 	 *            disable
 	 */
 	public void setWrapPageInScrollPaneEnabled(boolean enable) {
 		this.wrapPageInScrollPaneEnabled = enable;
 	}
 
 	protected MutableTreeNode addNodeFor(CustomPage<?> page) {
 		return addNode(new CustomPageTreeNode(page));
 	}
 
 	protected MutableTreeNode addNodeFor(Page page) {
 		return addNode(new PageTreeNode(page));
 	}
 
 	protected MutableTreeNode addNode(MutableTreeNode node) {
 		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
 		DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
 		model.insertNodeInto(node, root, root.getChildCount());
 		return node;
 	}
 
 	protected void removeNodeFor(CustomPage<?> page) {
 		removeNode(customPageMap.get(page));
 	}
 
 	protected void removeNode(MutableTreeNode node) {
 		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
 		model.removeNodeFromParent(node);
 	}
 
 	/**
 	 * Adds the specified preference page to the preference dialog. A new
 	 * preference node is created in the preference tree. When selected, the
 	 * <code>page</code>'s UI component is displayed.
 	 * <p>
 	 * If the custom pages feature is disabled, this method does nothing.
 	 * 
 	 * @param page
 	 *            the preference page
 	 * @return <code>true</code> if <code>page</code> was added, else
 	 *         <code>false</code>
 	 */
 	public boolean add(CustomPage<?> page) {
 		boolean added = false;
 		if (isCustomPagesEnabled()) {
 			MutableTreeNode node = addNodeFor(page);
 			customNodeMap.put(node, page);
 			customPageMap.put(page, node);
 			added = true;
 		}
 		return added;
 	}
 
 	/**
 	 * Removes the specified preference page from the preference dialog.
 	 * 
	 * @TODO handle falling back to last selected node
	 * 
 	 * @param page
 	 *            the preference page
 	 */
 	public void remove(CustomPage<?> page) {
 		// TODO handle falling back to last selected node
 		removeNodeFor(page);
 		customNodeMap.remove(page);
 		customPageMap.remove(page);
 	}
 
 	/**
 	 * Sets the page to display to the user. This sets the right component of
 	 * the {@link #splitPane} to <code>page</code>.
 	 * <p>
 	 * <em><b>Note:</b> this performs differently based on the result of 
 	 * {@link #isWrapPageInScrollPaneEnabled()}.</em>
 	 * 
 	 * @see #isWrapPageInScrollPaneEnabled()
 	 * @see #setWrapPageInScrollPaneEnabled(boolean)
 	 * @param page
 	 *            the page to display
 	 */
 	public void setPage(CustomPage<?> page) {
 		this.page = page;
 		if (isWrapPageInScrollPaneEnabled()) {
 			splitPane.setRightComponent(new JScrollPane(this.page.getPage()));
 		} else {
 			splitPane.setRightComponent(this.page.getPage());
 		}
 	}
 
 	/**
 	 * Returns the root preference nodes to be displayed in the tree.
 	 * 
 	 * @return the root preferences nodes
 	 */
 	public Preferences[] getPreferences() {
 		return preferences;
 	}
 
 	/**
 	 * Sets the root preference nodes to be displayed in the tree.
 	 * 
 	 * @param preferences
 	 *            the root preferences nodes
 	 */
 	public void setPreferences(Preferences... preferences) {
 		this.preferences = preferences;
 	}
 
 	/**
 	 * Creates and returns a <code>JTree</code> containing tree nodes for each
 	 * <code>Preferences</code> specified.
 	 * 
 	 * @param userPreferences
 	 *            <code>true</code> to add the nodes for user preferences, or
 	 *            <code>false</code> not to
 	 * @param systemPreferences
 	 *            <code>true</code> to add the nodes for system preferences, or
 	 *            <code>false</code> not to
 	 * @param preferences
 	 *            the objects containing preferences to add to the tree
 	 * @see #createTreeSelectionListener()
 	 * @return the preference tree created
 	 */
 	protected JTree createTree(boolean userPreferences,
 			boolean systemPreferences, Preferences... preferences) {
 		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Preferences");
 		for (Preferences node : preferences) {
 			if (userPreferences) {
 				root.add(createUserRootNode(node));
 			}
 			if (systemPreferences) {
 				root.add(createSystemRootNode(node));
 			}
 		}
 		DefaultTreeModel model = new DefaultTreeModel(root);
 		JTree tree = new JTree(model);
 		tree.addTreeSelectionListener(createTreeSelectionListener());
 		return tree;
 	}
 
 	/**
 	 * Returns the tree listener that listens to the node selected in
 	 * {@link #tree} and handles the page to be displayed.
 	 * 
 	 * @see #createTree(boolean, boolean, Preferences...)
 	 * @return the tree selection listener
 	 */
 	protected TreeSelectionListener createTreeSelectionListener() {
 		return new TreeSelectionListener() {
 
 			@Override
 			public void valueChanged(TreeSelectionEvent e) {
 				Object node = e.getPath().getLastPathComponent();
 				if (node instanceof PreferenceTreeNode) {
 					// PreferenceTreeNode node = (PreferenceTreeNode)
 					// e.getPath().getLastPathComponent();
 					Preferences pref = ((PreferenceTreeNode) node)
 							.getPrefObject();
 					// editTable.setModel(new PreferenceTableModel(pref));
 					preferencePage.setModel(new PreferenceTableModel(pref));
 					preferencePage.setPageTitle(pref.name());
 					setPage(preferencePage);
 				} else if (node instanceof CustomPageTreeNode) {
 					setPage(customNodeMap.get(node));
 				} else {
 					setPage(new BlankPage());
 				}
 			}
 		};
 	}
 
 	/**
 	 * Creates and returns a <code>MutableTreeNode</code> for the
 	 * <code>preference</code> in the system preferences space.
 	 * <p>
 	 * This is equivalent to <code>createRootNode(preference, false)</code>
 	 * 
 	 * @param preference
 	 *            the preferences object
 	 * @return a new tree node based on the specified <code>preference</code>
 	 */
 	protected MutableTreeNode createSystemRootNode(Preferences preference) {
 		return createRootNode(preference, false);
 	}
 
 	/**
 	 * Creates and returns a <code>MutableTreeNode</code> for the
 	 * <code>preference</code> in the user preferences space.
 	 * <p>
 	 * This is equivalent to <code>createRootNode(preference, true/code>
 	 * 
 	 * @param preference
 	 *            the preferences object
 	 * @return a new tree node based on the specified <code>preference</code>
 	 */
 	protected MutableTreeNode createUserRootNode(Preferences preference) {
 		return createRootNode(preference, true);
 	}
 
 	/**
 	 * Creates and returns a <code>MutableTreeNode</code> for the
 	 * <code>preference</code> in the user preferences space.
 	 * 
 	 * @param preference
 	 *            the preferences object
 	 * @param defaultUser
 	 *            <code>true</code> to fall back to
 	 *            <code>Preferences.userRoot()</code> or <code>false</code> to
 	 *            fall back to <code>Preferences.systemRoot()</code> if
 	 *            <code>pref</code> is <code>null</code>
 	 * @return a new tree node based on the specified <code>obj</code>
 	 */
 	protected MutableTreeNode createRootNode(Preferences preference,
 			boolean defaultUser) {
 		MutableTreeNode node = null;
 		try {
 			if (preference == null) {
 				node = new PreferenceTreeNode(
 						defaultUser ? Preferences.userRoot()
 								: Preferences.systemRoot());
 			} else {
 				node = new PreferenceTreeNode(preference);
 			}
 		} catch (BackingStoreException e) {
 			e.printStackTrace();
 			node = new DefaultMutableTreeNode(
 					defaultUser ? "No User Preferences!"
 							: "No System Preferences!");
 		}
 		return node;
 	}
 
 }
