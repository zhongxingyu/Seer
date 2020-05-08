 /**
  *
  */
 package cytoscape.dialogs.plugins;
 
 import cytoscape.*;
 
 import cytoscape.bookmarks.Bookmarks;
 import cytoscape.bookmarks.Category;
 import cytoscape.bookmarks.DataSource;
 
 import cytoscape.dialogs.preferences.BookmarkDialog;
 
 import cytoscape.plugin.ManagerException;
 import cytoscape.plugin.ManagerUtil;
 import cytoscape.plugin.PluginInfo;
 import cytoscape.plugin.PluginManager;
 import cytoscape.plugin.PluginInquireAction;
 import cytoscape.plugin.PluginStatus;
 
 import cytoscape.task.ui.JTaskConfig;
 import cytoscape.task.util.TaskManager;
 import cytoscape.util.BookmarksUtil;
 
 import org.jdesktop.layout.GroupLayout;
 import org.jdesktop.layout.LayoutStyle;
 
 import java.awt.Component;
 import java.awt.Dimension;
 
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.ListCellRenderer;
 import javax.swing.WindowConstants;
 
 /**
  * @author skillcoy
  */
 public class PluginUrlDialog extends JDialog {
 	private String bookmarkCategory = "plugins";
 
 	private Bookmarks theBookmarks;
 
 	private PluginManageDialog parentDialog;
 
 	/**
 	 * Creates a new PluginUrlDialog object.
 	 */
 	public PluginUrlDialog(JDialog owner) {
 		super(owner, "Plugin Download Sites");
 		parentDialog = (PluginManageDialog) owner;
 		setLocationRelativeTo(owner);
 		bookmarksSetUp();
 		initComponents();
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 * 
 	 * @param Items
 	 *            DOCUMENT ME!
 	 */
 	public void addItems(String[] Items) {
 		urlComboBox.setModel(new javax.swing.DefaultComboBoxModel(Items));
 	}
 
 	/*
 	 * Sets up the bookmarks for plugin dowload sites
 	 */
 	private void bookmarksSetUp() {
 		try {
 			theBookmarks = Cytoscape.getBookmarks();
 		} catch (Exception E) {
 			JOptionPane.showMessageDialog(Cytoscape.getDesktop(),
 					"Failed to retrieve bookmarks for plugin download sites.",
 					"Error", JOptionPane.ERROR_MESSAGE);
 			E.printStackTrace();
 
 			return;
 		}
 
 		// if theBookmarks does not exist, create an empty one
 		if (theBookmarks == null) {
 			theBookmarks = new Bookmarks();
 			Cytoscape.setBookmarks(theBookmarks);
 		}
 
 	}
 
 	// ok - chooses a site. Needs to reopen the InstallDialog using new url for
 	// the manager inquiry
 	private void okHandler(java.awt.event.ActionEvent evt) {
 		DataSource SelectedSite = (DataSource) urlComboBox.getSelectedItem();
 		parentDialog.switchDownloadSites();
 		dispose();
 
 		PluginManager Mgr = PluginManager.getPluginManager();
 		
 		cytoscape.task.Task task = Mgr.getInquireTask
 			(SelectedSite.getHref(), new UrlAction(parentDialog, SelectedSite.getHref()));
 		// Configure JTask Dialog Pop-Up Box
 		JTaskConfig jTaskConfig = new JTaskConfig();
 		jTaskConfig.setOwner(Cytoscape.getDesktop());
 		jTaskConfig.displayCloseButton(false);
 		jTaskConfig.displayStatus(true);
 		jTaskConfig.setAutoDispose(true);
 		jTaskConfig.displayCancelButton(true);
 		// Execute Task in New Thread; pop open JTask Dialog Box.
 		TaskManager.executeTask(task, jTaskConfig);
 		
 		
 		parentDialog.setSiteName(SelectedSite.getName());
 	}
 
 	// add - opens the bookmarks dialog to add a new download site
 	private void addSiteHandler(java.awt.event.ActionEvent evt) {
 		try {
 			BookmarkDialog bDialog = new BookmarkDialog(Cytoscape.getDesktop());
 
 			// for some reason the windowStateListener wasn't getting the event
 			// so I have to use this one
 			// this allows me to update the combo box when the user is done
 			// adding
 			bDialog.addWindowListener(new java.awt.event.WindowListener() {
 				public void windowClosed(java.awt.event.WindowEvent evt) {
 					loadBookmarkCMBox();
 				}
 
 				public void windowOpened(java.awt.event.WindowEvent evt) {
 				}
 
 				public void windowDeiconified(java.awt.event.WindowEvent evt) {
 				}
 
 				public void windowIconified(java.awt.event.WindowEvent evt) {
 				}
 
 				public void windowClosing(java.awt.event.WindowEvent evt) {
 				}
 
 				public void windowDeactivated(java.awt.event.WindowEvent evt) {
 				}
 
 				public void windowActivated(java.awt.event.WindowEvent evt) {
 				}
 			});
 			bDialog.pack();
 			bDialog.setVisible(true);
 		} catch (Exception E) {
 			JOptionPane
 					.showMessageDialog(
 							Cytoscape.getDesktop(),
 							"Failed to get bookmarks.  Go to Edit->Preferences->Bookmarks to edit your plugin download sites.",
 							"Error", JOptionPane.ERROR_MESSAGE);
 			E.printStackTrace();
 		}
 	}
 
 	// loads the combo box for bookmars
 	private void loadBookmarkCMBox() {
 		DefaultComboBoxModel theModel = new DefaultComboBoxModel();
 
 		// Extract the URL entries
 		List<DataSource> theDataSourceList = BookmarksUtil.getDataSourceList(
 				bookmarkCategory, theBookmarks.getCategory());
 
 		if (theDataSourceList != null) {
 			for (DataSource Current : theDataSourceList) {
 				theModel.addElement(Current);
 			}
 		}
 
 		urlComboBox.setModel(theModel);
 	}
 
 	private void initComponents() {
 		labelPanel = new JPanel();
 		label = new JLabel();
 		urlComboBox = new JComboBox();
 		urlComboBox.setRenderer(new BookmarkCellRenderer());
 		urlComboBox.setEditable(false);
 
 		jPanel1 = new JPanel();
 		okButton = new JButton();
 		addSiteButton = new JButton();
 		cancelButton = new JButton();
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
 		label.setText("Choose a plugin download site");
 
 		GroupLayout labelPanelLayout = new GroupLayout(labelPanel);
 		labelPanel.setLayout(labelPanelLayout);
 		labelPanelLayout.setHorizontalGroup(labelPanelLayout
 				.createParallelGroup(GroupLayout.LEADING).add(
 						labelPanelLayout.createSequentialGroup()
 								.addContainerGap().add(label,
 										GroupLayout.DEFAULT_SIZE, 239,
 										Short.MAX_VALUE).addContainerGap()));
 		labelPanelLayout.setVerticalGroup(labelPanelLayout.createParallelGroup(
 				GroupLayout.LEADING).add(
 				labelPanelLayout.createSequentialGroup().addContainerGap().add(
 						label, GroupLayout.DEFAULT_SIZE, 16, Short.MAX_VALUE)
 						.addContainerGap()));
 		okButton.setText("Ok");
 		okButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				okHandler(evt);
 			}
 		});
 		addSiteButton.setText("Add Site");
 		addSiteButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				addSiteHandler(evt);
 			}
 		});
 		cancelButton.setText("Cancel");
 		cancelButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				PluginUrlDialog.this.dispose();
 			}
 		});
 
 		GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
 		jPanel1.setLayout(jPanel1Layout);
 		jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(
 				GroupLayout.LEADING).add(
 				jPanel1Layout.createSequentialGroup().add(8, 8, 8)
 						.add(okButton).addPreferredGap(LayoutStyle.RELATED)
 						.add(addSiteButton)
 						.addPreferredGap(LayoutStyle.RELATED).add(cancelButton)
 						.addContainerGap(46, Short.MAX_VALUE)));
 		jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(
 				GroupLayout.LEADING).add(
 				jPanel1Layout.createSequentialGroup().addContainerGap().add(
 						jPanel1Layout.createParallelGroup(GroupLayout.BASELINE)
 								.add(okButton).add(addSiteButton).add(
 										cancelButton)).addContainerGap(
 						GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
 
 		GroupLayout layout = new GroupLayout(getContentPane());
 		getContentPane().setLayout(layout);
 		layout
 				.setHorizontalGroup(layout
 						.createParallelGroup(GroupLayout.LEADING)
 						.add(
 								layout
 										.createSequentialGroup()
 										.add(
 												layout
 														.createParallelGroup(
 																GroupLayout.LEADING)
 														.add(
 																labelPanel,
 																GroupLayout.PREFERRED_SIZE,
 																GroupLayout.DEFAULT_SIZE,
 																GroupLayout.PREFERRED_SIZE)
 														.add(
 																layout
 																		.createSequentialGroup()
 																		.add(
 																				21,
 																				21,
 																				21)
 																		.add(
 																				layout
 																						.createParallelGroup(
 																								GroupLayout.LEADING)
 																						.add(
 																								urlComboBox,
 																								0,
 																								296,
 																								Short.MAX_VALUE)
 																						.add(
 																								jPanel1,
 																								GroupLayout.DEFAULT_SIZE,
 																								GroupLayout.DEFAULT_SIZE,
 																								Short.MAX_VALUE))))
 										.addContainerGap()));
 		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.LEADING)
 				.add(
 						layout.createSequentialGroup().add(labelPanel,
 								GroupLayout.PREFERRED_SIZE,
 								GroupLayout.DEFAULT_SIZE,
 								GroupLayout.PREFERRED_SIZE).addPreferredGap(
 								LayoutStyle.RELATED).add(urlComboBox,
 								GroupLayout.PREFERRED_SIZE,
 								GroupLayout.DEFAULT_SIZE,
 								GroupLayout.PREFERRED_SIZE).addPreferredGap(
 								LayoutStyle.RELATED).add(jPanel1,
 								GroupLayout.PREFERRED_SIZE,
 								GroupLayout.DEFAULT_SIZE,
 								GroupLayout.PREFERRED_SIZE)));
 		loadBookmarkCMBox();
 		pack();
 	}
 
 	// required to make the text of the data source show up correctly in the
 	// combo box
 	private class BookmarkCellRenderer extends JLabel implements
 			ListCellRenderer {
 		
 		public BookmarkCellRenderer() {
 			setOpaque(true);
 		}
 		
 		public Component getListCellRendererComponent(JList list, Object value,
 				int index, boolean isSelected, boolean cellHasFocus) {
 			DataSource dataSource = (DataSource) value;
 			setText(dataSource.getName());
 
 			if (isSelected) {
 				if (0 < index) {
 					list.setToolTipText(dataSource.getHref());
 				}
 			}
 
 			return this;
 		}
 	}
 
 	private class UrlAction extends PluginInquireAction {
 
 		private PluginManageDialog dialog;
 		private String url;
 
 		public UrlAction(PluginManageDialog Dialog, String Url) {
 			dialog = Dialog;
 			url = Url;
 
 		}
 
 		public boolean displayProgressBar() {
 			return true;
 		}
 
 		public String getProgressBarMessage() {
 			return "Attempting to connect...";
 		}
 
 		public void inquireAction(List<PluginInfo> Results) {
 
 			if (isExceptionThrown()) {
 				if (getIOException() != null) {
 					// failed to read the given url
 					getIOException().printStackTrace();
 					dialog.setMessage(PluginManageDialog.CommonError.NOXML + url);
 				} else if (getJDOMException() != null) {
 					// failed to parse the xml file at the url
 					getJDOMException().printStackTrace();
 					dialog.setMessage(PluginManageDialog.CommonError.BADXML + url);
 				}
 			} else {
 
 				PluginManager Mgr = PluginManager.getPluginManager();
 				List<PluginInfo> UniqueAvailable = ManagerUtil.getUnique(Mgr
 						.getPlugins(PluginStatus.CURRENT), Results);
 
 				Map<String, List<PluginInfo>> NewPlugins = ManagerUtil
 						.sortByCategory(UniqueAvailable);
 
 				if (NewPlugins.size() <= 0) {
 					dialog.setMessage("No plugins compatible with "
 							+ CytoscapeVersion.version
 							+ " available from this site.");
 				} else {
 					dialog.setMessage("");
 				}
 
 				for (String Category : NewPlugins.keySet()) {
 					dialog.addCategory(Category, NewPlugins.get(Category),
 							PluginManageDialog.PluginInstallStatus.AVAILABLE);
 				}
 			}
 
 		}
 
 	}
 
 	private JButton addSiteButton;
 
 	private JButton cancelButton;
 
 	private JPanel jPanel1;
 
 	private JLabel label;
 
 	private JPanel labelPanel;
 
 	private JButton okButton;
 
 	private JComboBox urlComboBox;
 }
