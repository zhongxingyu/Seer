 /**
  *
  */
 package cytoscape.dialogs.plugins;
 
 import cytoscape.Cytoscape;
 
 import cytoscape.plugin.PluginInfo;
 import cytoscape.plugin.PluginManager;
 
 import cytoscape.task.TaskMonitor;
 import cytoscape.task.ui.JTaskConfig;
 import cytoscape.task.util.TaskManager;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import java.util.List;
 
 import javax.swing.JOptionPane;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 
 /**
  * @author skillcoy
  */
 public class PluginManageDialog extends javax.swing.JDialog implements
 		TreeSelectionListener, ActionListener {
 
 	public enum PluginInstallStatus {
 		INSTALLED("Currently Installed"), AVAILABLE("Available for Install");
 		private String typeText;
 
 		private PluginInstallStatus(String type) {
 			typeText = type;
 		}
 
 		public String toString() {
 			return typeText;
 		}
 	}
 
 	public enum CommonError {
 
 		NOXML("ERROR: Failed to read XML file "), BADXML(
 				"ERROR: XML file may be incorrectly formatted, unable to read ");
 
 		private String errorText;
 
 		private CommonError(String error) {
 			errorText = error;
 		}
 
 		public String toString() {
 			return errorText;
 		}
 	}
 
 	private String baseSiteLabel = "Plugins available for download from: ";
 
 	public PluginManageDialog(javax.swing.JDialog owner) {
 		super(owner, "Manage Plugins");
 		setLocationRelativeTo(owner);
 		initComponents();
 		initTree();
 	}
 
 	public PluginManageDialog(javax.swing.JFrame owner) {
 		super(owner, "Manage Plugins");
 		setLocationRelativeTo(owner);
 		initComponents();
 		initTree();
 	}
 
 	// trying to listen to events in the Url dialog
 	public void actionPerformed(ActionEvent evt) {
 		System.out.println("URL DIALOG: " + evt.getSource().toString());
 	}
 
 	/**
 	 * Enables the delete/install buttons when the correct leaf node is selected
 	 */
 	public void valueChanged(TreeSelectionEvent e) {
 		TreeNode Node = (TreeNode) pluginTree.getLastSelectedPathComponent();
 		if (Node == null) {
 			return;
 		}
 
 		if (Node.isLeaf()) {
 			// display any object selected
 			infoTextPane.setContentType("text/html");
 			infoTextPane.setText(((PluginInfo) Node.getObject()).htmlOutput());
 
 			if (Node.isNodeAncestor(installedNode)) {
 				deleteButton.setEnabled(true);
 				installButton.setEnabled(false);
 			} else if (Node.isNodeAncestor(availableNode)) {
 				deleteButton.setEnabled(false);
 				installButton.setEnabled(true);
 			}
 		} else {
 			deleteButton.setEnabled(false);
 			installButton.setEnabled(false);
 		}
 		if (PluginManager.usingWebstartManager()) {
 			deleteButton.setEnabled(false);
 		}
 
 	}
 
 	/**
 	 * Sets a message to be shown to the user regarding the plugin management
 	 * actions.
 	 * 
 	 * @param Msg
 	 */
 	public void setMessage(String Msg) {
 		msgLabel.setText(Msg);
 	}
 
 	/**
 	 * Set the name of the site the available plugins are from.
 	 * 
 	 * @param SiteName
 	 */
 	public void setSiteName(String SiteName) {
 		availablePluginsLabel.setText(baseSiteLabel + " " + SiteName);
 	}
 
 	/**
 	 * Call this when changing download sites to clear out the old available
 	 * list in order to create a new one.
 	 */
 	public void switchDownloadSites() {
 		java.util.Vector<TreeNode> AvailableNodes = new java.util.Vector<TreeNode>(
 				availableNode.getChildren());
 		for (TreeNode child : AvailableNodes) {
 			treeModel.removeNodeFromParent(child);
 		}
 
 		// availableNode = new
 		// TreeNode(PluginInstallStatus.AVAILABLE.toString(), true);
 		// rootTreeNode.addChild(availableNode);
 
 	}
 
 	/**
 	 * Adds a category and it's list of plugins to the appropriate tree (based
 	 * on Status) in the dialog.
 	 * 
 	 * @param CategoryName
 	 *            String category for this list of plugins
 	 * @param Plugins
 	 *            List of PluginInfo objects to be shown in the given category
 	 * @param Status
 	 *            PluginInstallStatus (currently installed or available for
 	 *            install)
 	 */
 	public void addCategory(String CategoryName, List<PluginInfo> Plugins,
 			PluginInstallStatus Status) {
 		switch (Status) {
 		case INSTALLED:
 			addCategory(CategoryName, Plugins, installedNode);
 			break;
 
 		case AVAILABLE:
 			addCategory(CategoryName, Plugins, availableNode);
 			if (treeModel.getIndexOfChild(rootTreeNode, availableNode) < 0) {
 				treeModel.addNodeToParent(rootTreeNode, availableNode);
 			}
 			break;
 		}
 	}
 
 	// add category to the set of plugins under given node
 	private void addCategory(String CategoryName, List<PluginInfo> Plugins,
 			TreeNode node) {
 		TreeNode Category = new TreeNode(CategoryName, true);
 		node.addChild(Category);
 
 		for (PluginInfo CurrentPlugin : Plugins) {
 			Category.addChild(new TreeNode(CurrentPlugin));
 		}
 	}
 
 	// change site url
 	private void changeSiteButtonActionPerformed(java.awt.event.ActionEvent evt) {
 		PluginUrlDialog dialog = new PluginUrlDialog(this);
 		dialog.setVisible(true);
 	}
 
 	// delete event
 	private void deleteButtonActionPerformed(ActionEvent evt) {
 		TreeNode Node = (TreeNode) pluginTree.getLastSelectedPathComponent();
 
 		if (Node == null) {
 			return;
 		}
 		PluginInfo NodeInfo = (PluginInfo) Node.getObject();
 		String ChangeMsg = "Changes will not take effect until you have restarted Cytoscape.";
 		String VerifyMsg = "";
 		if (NodeInfo.getCategory().equalsIgnoreCase("core")) {
 			VerifyMsg = "This is a 'core' plugin and other plugins may depend on it, "
 					+ "are you sure you want to delete it?\n" + ChangeMsg;
 		} else {
 			VerifyMsg = "Are you sure you want to delete the plugin '"
 					+ NodeInfo.getName() + "'?\n" + ChangeMsg;
 		}
 		if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this,
 				VerifyMsg, "Verify Delete Plugin", JOptionPane.YES_NO_OPTION,
 				JOptionPane.QUESTION_MESSAGE)) {
 			try {
 				PluginManager.getPluginManager().delete(NodeInfo);
 				treeModel.removeNodeFromParent(Node);
 				setMessage(NodeInfo.getName()
 						+ " will be removed when you restart Cytoscape.");
 			} catch (cytoscape.plugin.WebstartException we) {
 				we.printStackTrace();
 			}
 		}
 	}
 
 	// install new plugin
 	private void installButtonActionPerformed(java.awt.event.ActionEvent evt) {
 		final TreeNode node = (TreeNode) pluginTree
 				.getLastSelectedPathComponent();
 
 		if (node == null) { // error
 			return;
 		}
 		Object nodeInfo = node.getObject();
 		if (node.isLeaf()) {
 			final PluginInfo info = (PluginInfo) nodeInfo;
 
 			if (info.getLicenseText() != null) {
 				final LicenseDialog License = new LicenseDialog(this);
 				License.setPluginName(info.getName() + " v"
 						+ info.getPluginVersion());
 				License.addLicenseText(info.getLicenseText());
 				License.addListenerToFinish(new ActionListener() {
 					public void actionPerformed(java.awt.event.ActionEvent evt) {
 						License.dispose();
 						createInstallTask(info, node);
 
 					}
 				});
 				License.setVisible(true);
 			} else {
 				createInstallTask(info, node);
 			}
 		}
 	}
 
 	private void updateCurrent(PluginInfo info) {
 		boolean categoryMatched = false;
 
 		for (TreeNode Child : installedNode.getChildren()) {
 			if (Child.getTitle().equals(info.getCategory())) {
 				Child.addChild(new TreeNode(info));
 				categoryMatched = true;
 			}
 		}
 
 		if (!categoryMatched) {
 			List<PluginInfo> NewPlugin = new java.util.ArrayList<PluginInfo>();
 			NewPlugin.add(info);
 			addCategory(info.getCategory(), NewPlugin,
 					PluginInstallStatus.INSTALLED);
 		}
 	}
 
 	// close button
 	private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {
 		dispose();
 	}
 
 	// initialize the JTree and base nodes
 	private void initTree() {
 		pluginTree.setRootVisible(false);
 		pluginTree.addTreeSelectionListener(this);
 
 		pluginTree.getSelectionModel().setSelectionMode(
 				javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION);
 
 		rootTreeNode = new TreeNode("Plugins", true);
 		installedNode = new TreeNode(PluginInstallStatus.INSTALLED.toString(),
 				true);
 		availableNode = new TreeNode(PluginInstallStatus.AVAILABLE.toString(),
 				true);
 
 		treeModel = new ManagerModel(rootTreeNode);
 		treeModel.addNodeToParent(rootTreeNode, installedNode);
 
 		pluginTree.setModel(treeModel);
 	}
 
 	// initialize the dialog box & components
 	// <editor-fold defaultstate="collapsed" desc=" Generated Code ">
 	private void initComponents() {
 		jSplitPane1 = new javax.swing.JSplitPane();
 		infoScrollPane = new javax.swing.JScrollPane();
 		infoTextPane = new javax.swing.JEditorPane();
 		treeScrollPane = new javax.swing.JScrollPane();
 		pluginTree = new javax.swing.JTree();
 		availablePluginsLabel = new javax.swing.JLabel();
 		changeSiteButton = new javax.swing.JButton();
 		installButton = new javax.swing.JButton();
 		deleteButton = new javax.swing.JButton();
 		closeButton = new javax.swing.JButton();
 		msgLabel = new javax.swing.JLabel();
 
 		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
 		jSplitPane1.setDividerLocation(250);
 		infoScrollPane.setViewportView(infoTextPane);
 
 		jSplitPane1.setRightComponent(infoScrollPane);
 
 		treeScrollPane.setViewportView(pluginTree);
 
 		jSplitPane1.setLeftComponent(treeScrollPane);
 
 		availablePluginsLabel.setLabelFor(jSplitPane1);
 		availablePluginsLabel
 				.setVerticalAlignment(javax.swing.SwingConstants.TOP);
 
 		changeSiteButton.setText("Change Download Site");
 		changeSiteButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				changeSiteButtonActionPerformed(evt);
 			}
 		});
 
 		installButton.setText("Install");
 		installButton.setEnabled(false);
 		installButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				installButtonActionPerformed(evt);
 			}
 		});
 
 		deleteButton.setText("Delete");
 		deleteButton.setEnabled(false);
 		deleteButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				deleteButtonActionPerformed(evt);
 			}
 		});
 
 		closeButton.setText("Close");
 		closeButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				closeButtonActionPerformed(evt);
 			}
 		});
 
 		msgLabel.setForeground(new java.awt.Color(204, 0, 51));
 		msgLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
 
 		org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(
 				getContentPane());
 		getContentPane().setLayout(layout);
 		layout
 				.setHorizontalGroup(layout
 						.createParallelGroup(
 								org.jdesktop.layout.GroupLayout.LEADING)
 						.add(
 								layout
 										.createSequentialGroup()
 										.add(43, 43, 43)
 										.add(
 												layout
 														.createParallelGroup(
 																org.jdesktop.layout.GroupLayout.TRAILING,
 																false)
 														.add(
 																org.jdesktop.layout.GroupLayout.LEADING,
 																msgLabel)
 														.add(
 																org.jdesktop.layout.GroupLayout.LEADING,
 																availablePluginsLabel,
 																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 																Short.MAX_VALUE)
 														.add(
 																layout
 																		.createSequentialGroup()
 																		.add(
 																				changeSiteButton)
 																		.add(
 																				18,
 																				18,
 																				18)
 																		.add(
 																				installButton)
 																		.add(
 																				18,
 																				18,
 																				18)
 																		.add(
 																				deleteButton)
 																		.add(
 																				22,
 																				22,
 																				22)
 																		.add(
 																				closeButton))
 														.add(
 																org.jdesktop.layout.GroupLayout.LEADING,
 																jSplitPane1,
 																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 																574,
 																Short.MAX_VALUE))
 										.addContainerGap(41, Short.MAX_VALUE)));
 		layout
 				.setVerticalGroup(layout
 						.createParallelGroup(
 								org.jdesktop.layout.GroupLayout.LEADING)
 						.add(
 								layout
 										.createSequentialGroup()
 										.addContainerGap()
 										.add(
 												availablePluginsLabel,
 												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 												32,
 												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 										.addPreferredGap(
 												org.jdesktop.layout.LayoutStyle.RELATED)
 										.add(
 												jSplitPane1,
 												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 												324,
 												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 										.addPreferredGap(
 												org.jdesktop.layout.LayoutStyle.RELATED)
 										.add(
 												msgLabel,
 												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 												33, Short.MAX_VALUE)
 										.addPreferredGap(
 												org.jdesktop.layout.LayoutStyle.RELATED)
 										.add(
 												layout
 														.createParallelGroup(
 																org.jdesktop.layout.GroupLayout.BASELINE)
 														.add(changeSiteButton)
 														.add(installButton)
 														.add(deleteButton).add(
 																closeButton))
 										.addContainerGap()));
 		pack();
 	}// </editor-fold>
 
 	/*
 	 * --- create the tasks and task monitors to show the user what's going on
 	 * during download/install ---
 	 */
 
 	private void createInstallTask(PluginInfo obj, TreeNode node) {
 		// Create Task
 		PluginInstallTask task = new PluginInstallTask(obj, node);
 
 		// Configure JTask Dialog Pop-Up Box
 		JTaskConfig jTaskConfig = new JTaskConfig();
 		jTaskConfig.setOwner(Cytoscape.getDesktop());
 		jTaskConfig.displayCloseButton(false);
 		jTaskConfig.displayStatus(true);
 		jTaskConfig.setAutoDispose(true);
 		jTaskConfig.displayCancelButton(true);
 		// Execute Task in New Thread; pop open JTask Dialog Box.
 		TaskManager.executeTask(task, jTaskConfig);
 		PluginInfo info = task.getDownloadedPlugin();
 		if (info != null) {
 			loadPlugin(info);
 			cleanTree(node);
 		}
 	}
 
 	private void cleanTree(TreeNode node) {
 		PluginInfo info = (PluginInfo) node.getObject();
 		List<TreeNode> RemovableNodes = new java.util.ArrayList<TreeNode>();
 
 		for (int i = 0; i < node.getParent().getChildCount(); i++) {
 			TreeNode Child = (TreeNode) node.getParent().getChildAt(i);
 			PluginInfo childInfo = (PluginInfo) Child.getObject();
 
 			if (childInfo.getID().equals(info.getID())
 					&& childInfo.getName().equals(info.getName())) {
 				RemovableNodes.add(Child);
 			}
 		}
 
 		for (TreeNode treeNode : RemovableNodes) {
 			treeModel.removeNodeFromParent(treeNode);
 		}
 
 	}
 
 	// install and initialize the new plugin
 	private void loadPlugin(PluginInfo info) {
 		PluginManager Mgr = PluginManager.getPluginManager();
 		try {
 			Mgr.install(info);
 			Mgr.loadPlugin(info);
 			updateCurrent(info);
 		} catch (ClassNotFoundException cne) {
 			cne.printStackTrace();
 		} catch (java.io.IOException ioe) {
 			ioe.printStackTrace();
 		}
 	}
 
 	private class PluginInstallTask implements cytoscape.task.Task {
 		private cytoscape.task.TaskMonitor taskMonitor;
 
 		private PluginInfo pluginInfo;
 
 		private TreeNode node;
 
 		public PluginInstallTask(PluginInfo Info, TreeNode Node) {
 			pluginInfo = Info;
 			node = Node;
 		}
 
 		public void run() {
 			if (taskMonitor == null) {
 				throw new IllegalStateException("Task Monitor is not set.");
 			}
 			taskMonitor.setStatus("Installing " + pluginInfo.getName() + " v"
 					+ pluginInfo.getPluginVersion());
 			taskMonitor.setPercentCompleted(-1);
 
 			PluginManager Mgr = PluginManager.getPluginManager();
 			try {
 				pluginInfo = Mgr.download(pluginInfo, taskMonitor);
 				taskMonitor.setStatus(pluginInfo.getName() + " v"
 						+ pluginInfo.getPluginVersion() + " complete.");
 
 				PluginManageDialog.this.setMessage(pluginInfo.getName()
 						+ " install complete.");
 
 				taskMonitor.setStatus(pluginInfo.getName() + " v"
 						+ pluginInfo.getPluginVersion() + " loading...");
 			} catch (java.io.IOException ioe) {
				pluginInfo = null;
 				taskMonitor
 						.setException(ioe, "Failed to download "
 								+ pluginInfo.getName() + " from "
 								+ pluginInfo.getUrl());
 			} catch (cytoscape.plugin.ManagerException me) {
 				pluginInfo = null;
 				taskMonitor.setException(me, me.getMessage());
 			} finally {
 				taskMonitor.setPercentCompleted(100);
 			}
 		}
 
 		public PluginInfo getDownloadedPlugin() {
 			return pluginInfo;
 		}
 
 		public void halt() {
 			// not haltable
 		}
 
 		public void setTaskMonitor(TaskMonitor monitor)
 				throws IllegalThreadStateException {
 			this.taskMonitor = monitor;
 		}
 
 		public String getTitle() {
 			return "Installing Cytoscape Plugin '" + pluginInfo.getName() + "'";
 		}
 	}
 
 	// Variables declaration - do not modify
 	private javax.swing.JLabel availablePluginsLabel;
 
 	private javax.swing.JButton changeSiteButton;
 
 	private javax.swing.JButton closeButton;
 
 	private javax.swing.JButton deleteButton;
 
 	private javax.swing.JButton installButton;
 
 	private javax.swing.JScrollPane infoScrollPane;
 
 	private javax.swing.JEditorPane infoTextPane;
 
 	private javax.swing.JSplitPane jSplitPane1;
 
 	private javax.swing.JLabel msgLabel;
 
 	private javax.swing.JTree pluginTree;
 
 	private javax.swing.JScrollPane treeScrollPane;
 
 	// End of variables declaration
 	private TreeNode rootTreeNode;
 
 	private TreeNode installedNode;
 
 	private TreeNode availableNode;
 
 	private ManagerModel treeModel;
 }
