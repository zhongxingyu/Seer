 package cz.vutbr.fit.gja.gjaddr.gui;
 
 import cz.vutbr.fit.gja.gjaddr.persistancelayer.Contact;
 import cz.vutbr.fit.gja.gjaddr.persistancelayer.Database;
 import cz.vutbr.fit.gja.gjaddr.persistancelayer.Group;
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.Component;
 import java.awt.Desktop;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Arrays;
 import java.util.List;
 import javax.swing.Box;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JTextField;
 import javax.swing.JToolBar;
 import javax.swing.KeyStroke;
 import javax.swing.UIManager;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import org.jdesktop.swingx.MultiSplitLayout.Divider;
 import org.jdesktop.swingx.MultiSplitLayout.Leaf;
 import org.jdesktop.swingx.MultiSplitLayout.Split;
 import org.jdesktop.swingx.MultiSplitPane;
 
 /**
  * Main application window.
  *
  * @author xherrm01
  * @author Bc. Jan Kaláb <xkalab00@stud.fit,vutbr.cz>
  */
 public class MainWindow extends JFrame implements ActionListener, DocumentListener {
 	static final long serialVersionUID = 0;
 	private final Database db = new Database();
 	private JMenuItem menuItemClose, menuItemHelp, menuItemAbout, menuItemImport, menuItemExport;
 	private JTextField searchField;
 	private ContactsPanel contactsPanel;
 	private JButton addButton;
 
 	/**
 	 * Creates the main window.
 	 */
 	public MainWindow() {
 		super("GJAddr");
 		// cz.vutbr.fit.gja.gjaddr.persistancelayer.TestData.fillTestingData(db);	//DEBUG
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
 		addButton = new JButton(new ImageIcon(getClass().getResource("/res/plus.png"), "+"));
 		addButton.addActionListener(this);
 		toolbar.add(addButton);
 		searchField = new JTextField();
 		searchField.getDocument().addDocumentListener(this);
 		toolbar.add(searchField);
 		add(toolbar, BorderLayout.NORTH);
 		final Split model = new Split();
 		final Leaf groupsLeaf = new Leaf("groups");
 		final Leaf contactsLeaf = new Leaf("contacts");
 		final Leaf detailLeaf = new Leaf("detail");
 		groupsLeaf.setWeight(1.0 / 3);
 		contactsLeaf.setWeight(1.0 / 3);
 		detailLeaf.setWeight(1.0 / 3);
 		model.setChildren(Arrays.asList(groupsLeaf, new Divider(), contactsLeaf, new Divider(), detailLeaf));
 		final MultiSplitPane multiSplitPane = new MultiSplitPane();
 		multiSplitPane.getMultiSplitLayout().setModel(model);
 		multiSplitPane.add(new GroupsPanel(new GroupSelectionListener()), "groups");
 		contactsPanel = new ContactsPanel();
 		multiSplitPane.add(contactsPanel, "contacts");
 		multiSplitPane.add(new JButton("Detail"), "detail");
 		add(multiSplitPane, BorderLayout.CENTER);
 		pack();
 		setLocationRelativeTo(null);
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
 		menuBar.add(Box.createHorizontalGlue());
 		JMenu helpMenu = new JMenu("Help");
 		helpMenu.setMnemonic(KeyEvent.VK_H);
 		menuBar.add(helpMenu);
 
 		menuItemImport = new JMenuItem("Import", KeyEvent.VK_I);
 		menuItemImport.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
 		menuItemImport.addActionListener(this);
 		fileMenu.add(this.menuItemImport);
 		menuItemExport = new JMenuItem("Export", KeyEvent.VK_E);
 		menuItemExport.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
 		menuItemExport.addActionListener(this);
 		fileMenu.add(this.menuItemExport);
 		fileMenu.addSeparator();
 		menuItemClose = new JMenuItem("Quit", KeyEvent.VK_Q);
 		menuItemClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
 		menuItemClose.addActionListener(this);
 		fileMenu.add(this.menuItemClose);
 
 		menuItemHelp = new JMenuItem("Help", KeyEvent.VK_H);
 		menuItemHelp.setAccelerator(KeyStroke.getKeyStroke("F1"));
 		menuItemHelp.addActionListener(this);
 		helpMenu.add(this.menuItemHelp);
 
 		menuItemAbout = new JMenuItem("About", KeyEvent.VK_A);
 		menuItemAbout.addActionListener(this);
 		helpMenu.add(this.menuItemAbout);
 
 		return menuBar;
 	}
 
 	@Override
 	public void changedUpdate(DocumentEvent e) {
 		System.out.println(e.toString());
 	}
 
 	@Override
 	public void removeUpdate(DocumentEvent e) {
 		contactsPanel.filter(searchField.getText());
 	}
 
 	@Override
 	public void insertUpdate(DocumentEvent e) {
 		contactsPanel.filter(searchField.getText());
 	}
 
 	/**
 	 * Method binding functionality to difference actions performed (e.g. if button
 	 * was pressed etc.).
 	 *
 	 * @param e
 	 */
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		//System.out.println(e);
 		if (e.getSource() == menuItemClose) {
 			dispose();
 		} else if (e.getSource() == menuItemAbout) {
 			new AboutWindow();
 		} else if (e.getSource() == menuItemHelp) {
 			try{
 				Desktop.getDesktop().browse(new URI("http://pitel.github.com/GJAddr"));
 			} catch (URISyntaxException ex) {
 				System.err.println(ex);
 			} catch (IOException ex) {
 				System.err.println(ex);
 			}
 		} else if (e.getSource() == addButton) {
 			new EditWindow();
 		}
 	}
 
 	/**
 	 * Listener class for groups list selection
 	 */
 	private class GroupSelectionListener implements ListSelectionListener {
 		@Override
 		public void valueChanged(ListSelectionEvent e) {
 			if (!e.getValueIsAdjusting()) {	//React only on final choice
 				final JList list = (JList) e.getSource();
 				final Group[] groups = Arrays.copyOf(list.getSelectedValues(), list.getSelectedValues().length, Group[].class);
 				final List<Group> requiredGroupList = Arrays.asList(groups);
				//System.out.println("Groups: " + requiredGroupList);
 				final List<Contact> contacts = db.getAllContactsFromGroup(requiredGroupList);
 				contactsPanel.fillTable(contacts);
 			}
 		}
 	}
 }
