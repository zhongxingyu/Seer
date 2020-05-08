 package cz.vutbr.fit.gja.gjaddr.gui;
 
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
 	private final JTextField searchField = new JTextField();
 
 	/**
 	 * Creates the main window.
 	 */
 	public MainWindow() {
 		super("GJAddr");
 		//cz.vutbr.fit.gja.gjaddr.persistancelayer.TestData.fillTestingData(db);	//DEBUG
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (Exception e) {
 			System.err.println(e);
 		}
 		setIconImage(new ImageIcon(getClass().getResource("/res/icon.png")).getImage());
 		setJMenuBar(this.createMenu());
 		Container container = this.getContentPane();
 		JToolBar toolbar = new JToolBar();
 		toolbar.setFloatable(false);
 		toolbar.add(new JButton(new ImageIcon(getClass().getResource("/res/plus.png"), "+")));
 		searchField.getDocument().addDocumentListener(this);
 		toolbar.add(searchField);
 		container.add(toolbar, BorderLayout.NORTH);
 		Split model = new Split();
 		Leaf groupsLeaf = new Leaf("groups");
 		Leaf contactsLeaf = new Leaf("contacts");
 		Leaf detailLeaf = new Leaf("detail");
 		groupsLeaf.setWeight(1.0 / 3);
 		contactsLeaf.setWeight(1.0 / 3);
 		detailLeaf.setWeight(1.0 / 3);
 		model.setChildren(Arrays.asList(groupsLeaf, new Divider(), contactsLeaf, new Divider(), detailLeaf));
 		MultiSplitPane multiSplitPane = new MultiSplitPane();
 		multiSplitPane.getMultiSplitLayout().setModel(model);
 		multiSplitPane.add(new GroupsPanel(new GroupSelectionListener()), "groups");
		multiSplitPane.add(new ContactsPanel(), "contacts");
 		multiSplitPane.add(new JButton("Detail"), "detail");
 		container.add(multiSplitPane, BorderLayout.CENTER);
 		pack();
 		setLocationRelativeTo(null);
 	}
 
 	/**
 	 * Create and return application menu.
 	 *
 	 * @return MenuBar
 	 */
 	private JMenuBar createMenu() {
 		JMenuBar menuBar = new JMenuBar();
 		JMenu fileMenu = new JMenu("File");
 		fileMenu.setMnemonic(KeyEvent.VK_F);
 		menuBar.add(fileMenu);
 		menuBar.add(Box.createHorizontalGlue());
 		JMenu helpMenu = new JMenu("Help");
 		helpMenu.setMnemonic(KeyEvent.VK_H);
 		menuBar.add(helpMenu);
 
 		this.menuItemImport = new JMenuItem("Import", KeyEvent.VK_I);
 		this.menuItemImport.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
 		this.menuItemImport.addActionListener(this);
 		fileMenu.add(this.menuItemImport);
 		this.menuItemExport = new JMenuItem("Export", KeyEvent.VK_E);
 		this.menuItemExport.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
 		this.menuItemExport.addActionListener(this);
 		fileMenu.add(this.menuItemExport);
 		fileMenu.addSeparator();
 		this.menuItemClose = new JMenuItem("Quit", KeyEvent.VK_Q);
 		this.menuItemClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
 		this.menuItemClose.addActionListener(this);
 		fileMenu.add(this.menuItemClose);
 
 		this.menuItemHelp = new JMenuItem("Help", KeyEvent.VK_H);
 		this.menuItemHelp.setAccelerator(KeyStroke.getKeyStroke("F1"));
 		this.menuItemHelp.addActionListener(this);
 		helpMenu.add(this.menuItemHelp);
 
 		this.menuItemAbout = new JMenuItem("About", KeyEvent.VK_A);
 		this.menuItemAbout.addActionListener(this);
 		helpMenu.add(this.menuItemAbout);
 
 		return menuBar;
 	}
 
 	@Override
 	public void changedUpdate(DocumentEvent e) {
 		System.out.println(e.toString());
 	}
 
 	@Override
 	public void removeUpdate(DocumentEvent e) {
 		filterContacts(searchField.getText());
 	}
 
 	@Override
 	public void insertUpdate(DocumentEvent e) {
 		filterContacts(searchField.getText());
 	}
 
 	/**
 	 * Filter contacts
 	 *
 	 * @param filter Text to filer
 	 */
 	public void filterContacts(String filter) {
 		System.out.println("Filtering: " + filter);
 	}
 
 	/**
 	 * Method binding functionality to difference actions performed (e.g. if button
 	 * was pressed etc.).
 	 *
 	 * @param e
 	 */
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		System.out.println(e);
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
 		}
 	}
 
 	/**
 	 * Listener class for groups list selection
 	 */
 	private class GroupSelectionListener implements ListSelectionListener {
 		@Override
 		public void valueChanged(ListSelectionEvent e) {
 			if (!e.getValueIsAdjusting()) {	//React only on final choice
 				JList list = (JList) e.getSource();
 				Group[] groups = Arrays.copyOf(list.getSelectedValues(), list.getSelectedValues().length, Group[].class);
				System.out.println("Groups: " + Arrays.toString(groups));
 			}
 		}
 	}
 }
