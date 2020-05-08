 package cz.vutbr.fit.gja.gjaddr.gui;
 
 import cz.vutbr.fit.gja.gjaddr.persistancelayer.Contact;
 import cz.vutbr.fit.gja.gjaddr.persistancelayer.Database;
 import cz.vutbr.fit.gja.gjaddr.persistancelayer.Group;
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.util.List;
 import javax.swing.*;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 /**
  * Main application window.
  *
  * @author Bc. Jan Kaláb <xkalab00@stud.fit,vutbr.cz>
  */
 public class MainWindow extends JFrame implements ActionListener, DocumentListener {
 	static final long serialVersionUID = 0;
 	private final Database db = Database.getInstance();
 	private JMenuItem menuItemClose;
 	private JTextField searchField;
 	private ContactsPanel contactsPanel;
 	private DetailPanel detailPanel;
     private StatusBar statusBar;
 
	static final String ROOT_GROUP = "My contacts";
 
 	public UserActions actions = new UserActions();
 
 	/**
 	 * Creates the main window.
 	 */
 	public MainWindow() {
 		super("GJAddr");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (Exception e) {
 			System.err.println(e);
 		}
 
 		setIconImage(new ImageIcon(getClass().getResource("/res/icon.png")).getImage());
 		setJMenuBar(this.createMenu());
 		final JToolBar toolbar = new JToolBar();
 		toolbar.setFloatable(false);
 
 		toolbar.add(actions.actionNewContact);
 		toolbar.add(actions.actionEditContact);
 		toolbar.add(actions.actionDeleteContact);
 		toolbar.addSeparator();
 		toolbar.add(actions.actionImport);
 		toolbar.add(actions.actionExport);
 		toolbar.addSeparator();
 		toolbar.add(actions.actionPreferences);
 		toolbar.addSeparator();
 
 		searchField = new JTextField();
 		searchField.getDocument().addDocumentListener(this);
 		toolbar.add(searchField);
 		add(toolbar, BorderLayout.NORTH);
 
 		add(new GroupsPanel(this, new GroupSelectionListener()), BorderLayout.WEST);
 		contactsPanel = new ContactsPanel(this, new ContactSelectionListener());
 		//contactsPanel.setMinimumSize(new Dimension(300, 300));
 		detailPanel = new DetailPanel();
 		detailPanel.setMinimumSize(new Dimension(300, 300));
 		final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, contactsPanel, detailPanel);
 		splitPane.setResizeWeight(1);
 		splitPane.setContinuousLayout(true);
 		add(splitPane);
 
         this.statusBar = new StatusBar();
         add(this.statusBar, BorderLayout.SOUTH);
 
 		pack();
 		setLocationRelativeTo(null);
 
 		// open window with notifications
 		List<Contact> contactsWithBirtday = this.db.getContactsWithBirtday();
 		if (contactsWithBirtday.size() > 0) {
 			new NotificationsWindow(contactsWithBirtday);
 		}
 	}
 
 	/**
 	 * Create and return application menu.
 	 *
 	 * @return MenuBar
 	 */
 	private JMenuBar createMenu() {
 		final JMenuBar menuBar = new JMenuBar();
 		final JMenu fileMenu = new JMenu("File");
 		fileMenu.setMnemonic(KeyEvent.VK_F);
 		menuBar.add(fileMenu);
 
 		JMenu helpMenu = new JMenu("Help");
 		helpMenu.setMnemonic(KeyEvent.VK_H);
 		menuBar.add(helpMenu);
 
 		fileMenu.add(this.actions.actionImport);
 		fileMenu.add(this.actions.actionExport);
 		fileMenu.addSeparator();
 		fileMenu.add(this.actions.actionPreferences);
 		fileMenu.addSeparator();
 
 		menuItemClose = new JMenuItem("Quit", KeyEvent.VK_Q);
 		menuItemClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
 		menuItemClose.addActionListener(this);
 		fileMenu.add(this.menuItemClose);
 
 		helpMenu.add(this.actions.actionHelp);
 		helpMenu.add(this.actions.actionAbout);
 
 		return menuBar;
 	}
 
 	@Override
 	public void changedUpdate(DocumentEvent e) {}
 
 	@Override
 	public void removeUpdate(DocumentEvent e) {
 		contactsPanel.filter(searchField.getText());
 	}
 
 	@Override
 	public void insertUpdate(DocumentEvent e) {
 		contactsPanel.filter(searchField.getText());
 	}
 
 	/**
 	 * Method binding functionality to close window.
 	 *
 	 * @param e
 	 */
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource() == menuItemClose) {
 			dispose();
 		}
  	}
 
 	void handleGroupActionsVisibility() {
 		if (isSelectRootGroup()) {
 			this.actions.actionDeleteGroup.setEnabled(false);
 			this.actions.actionRenameGroup.setEnabled(false);
 			this.actions.actionManageGroup.setEnabled(false);
 		}
 		else {
 			this.actions.actionDeleteGroup.setEnabled(true);
 			this.actions.actionRenameGroup.setEnabled(true);
 			this.actions.actionManageGroup.setEnabled(true);
 		}
 	}
 
 	void handleContactActionsVisibility() {
 		if (ContactsPanel.getSelectedContact() == null) {
 			this.actions.actionDeleteContact.setEnabled(false);
 			this.actions.actionEditContact.setEnabled(false);
 			this.actions.actionManageContactGroups.setEnabled(false);
 		}
 		else {
 			this.actions.actionDeleteContact.setEnabled(true);
 			this.actions.actionEditContact.setEnabled(true);
 			this.actions.actionManageContactGroups.setEnabled(true);
 		}
 	}
 
 	private boolean isSelectRootGroup() {
 		for (Group g : GroupsPanel.getSelectedGroups()) {
 			if (g.getName().equals(ROOT_GROUP)) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	/**
 	 * Listener class for groups list selection
 	 */
 	private class GroupSelectionListener implements ListSelectionListener {
 		@Override
 		public void valueChanged(ListSelectionEvent e) {
 			if (!e.getValueIsAdjusting()) {	//React only on final choice
 				ContactsPanel.fillTable(false);
 				handleGroupActionsVisibility();
 			}
 		}
 	}
 
 	/**
 	 * Listener class for contacts table selection
 	 */
 	private class ContactSelectionListener implements ListSelectionListener {
 		@Override
 		public void valueChanged(ListSelectionEvent e) {
 			//React only on final choice
 			if (!e.getValueIsAdjusting()) {
 				detailPanel.show(contactsPanel.getSelectedContact());
 				handleContactActionsVisibility();
 			}
 		}
 	}
 }
