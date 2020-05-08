 package presentation.view;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;
 
 import javax.swing.AbstractAction;
 import javax.swing.AbstractButton;
 import javax.swing.ButtonGroup;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JRadioButtonMenuItem;
 import javax.swing.JSeparator;
 import javax.swing.KeyStroke;
 
 import presentation.model.LibraryTabbedPaneModel;
 import presentation.model.ModelController;
 
 /**
  * Repräsentiert und verwaltet die Menübar für die Bibliotheksapplikation.
  * Überwacht den Status der aktiven Menü-Tabs, um die Menübar entsprechend
  * anzupassen.
  */
public class LibraryMenuBar extends JMenuBar implements Observer {
 
 	private static final long serialVersionUID = 1L;
 	private JMenuItem reservedMenuItem;
 	private JMenuItem ausgelieheneMenuItem;
 	private JMenuItem damagedBooksMenuItem;
 	private JMenuItem allBooksMenuItem;
 	private JRadioButtonMenuItem searchMenuItem;
 	private JRadioButtonMenuItem bookMenuItem;
 	private JRadioButtonMenuItem userMenuItem;
 	private AbstractAction exitAction;
 	private JMenuItem exitMenuItem;
 	private JMenu viewMenu;
 	private JMenu searchMenu;
 	private JMenu bookMenu;
 	private JMenu userMenu;
 	private JMenu helpMenu;
 	private JMenuItem lendBokMenuItem;
 	private JMenuItem editBookMenuItem;
 	private JMenuItem editUserMenuItem;
 	private JMenuItem resetMenuItem;
 	private JSeparator separator;
 	private JMenuItem createUserMenuItem;
 	private AbstractButton aboutMenuItem;
 	private ButtonGroup viewGroup;
 	private ModelController controller;
 
 	public LibraryMenuBar(ModelController controller) {
 		this.controller = controller;
 		initFileMenu();
 		initViewMenu();
 
 		initSearchMenu();
 		initBookMenu();
 		initUserMenu();
 
 		initHelpMenu();
		controller.tabbed_model.addObserver(this);
 	}
 
 	private void initFileMenu() {
 		JMenu fileMenu = new JMenu();
 		this.add(fileMenu);
 		fileMenu.setText("Datei");
 		fileMenu.setMnemonic(java.awt.event.KeyEvent.VK_D);
 		{
 			resetMenuItem = new JMenuItem("Neu");
 			resetMenuItem.setAccelerator(KeyStroke.getKeyStroke("F4"));
 			resetMenuItem.addActionListener(new ChangeViewActionListener(
 					LibraryTabbedPaneModel.SEARCH_TAB) {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					super.actionPerformed(e);
 					controller.searchtab_model.resetSearchText();
 				}
 			});
 			fileMenu.add(resetMenuItem);
 
 			separator = new JSeparator();
 			fileMenu.add(separator);
 
 			exitMenuItem = new JMenuItem("Beenden");
 			fileMenu.add(exitMenuItem);
 			exitMenuItem.setAction(getExitAction());
 		}
 	}
 
 	private void initViewMenu() {
 		viewMenu = new JMenu();
 		this.add(viewMenu);
 		viewMenu.setText("Ansicht");
 		viewMenu.setMnemonic(java.awt.event.KeyEvent.VK_A);
 		{
 			viewGroup = new ButtonGroup();
 
 			searchMenuItem = new JRadioButtonMenuItem();
 			searchMenuItem.setText("Recherche");
 			searchMenuItem.setAccelerator(KeyStroke.getKeyStroke("F5"));
 			searchMenuItem.setSelected(true);
 			searchMenuItem
 					.addActionListener(createChangeViewAction(LibraryTabbedPaneModel.SEARCH_TAB));
 			viewGroup.add(searchMenuItem);
 			viewMenu.add(searchMenuItem);
 
 			bookMenuItem = new JRadioButtonMenuItem();
 			bookMenuItem.setText("Buchdetails");
 			bookMenuItem.setAccelerator(KeyStroke.getKeyStroke("F6"));
 			bookMenuItem
 					.addActionListener(createChangeViewAction(LibraryTabbedPaneModel.BOOK_TAB));
 			viewGroup.add(bookMenuItem);
 			viewMenu.add(bookMenuItem);
 
 			userMenuItem = new JRadioButtonMenuItem();
 			userMenuItem.setText("Benutzerdetails");
 			userMenuItem.setAccelerator(KeyStroke.getKeyStroke("F7"));
 			userMenuItem
 					.addActionListener(createChangeViewAction(LibraryTabbedPaneModel.USER_TAB));
 			viewGroup.add(userMenuItem);
 			viewMenu.add(userMenuItem);
 		}
 	}
 
 	private ActionListener createChangeViewAction(final int newTab) {
 		return new ChangeViewActionListener(newTab);
 	}
 
 	private class ChangeViewActionListener implements ActionListener {
 		private int newTab;
 
 		public ChangeViewActionListener(int newTab) {
 			this.newTab = newTab;
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			controller.tabbed_model.setActiveTab(newTab);
 		}
 	};
 
 	private void initSearchMenu() {
 		searchMenu = new JMenu();
 		this.add(searchMenu);
 		searchMenu.setText("Recherche");
 		searchMenu.setMnemonic(java.awt.event.KeyEvent.VK_R);
 
 		allBooksMenuItem = new JMenuItem();
 		allBooksMenuItem.setText("Alle Bücher");
 		searchMenu.add(allBooksMenuItem);
 
 		damagedBooksMenuItem = new JMenuItem();
 		damagedBooksMenuItem.setText("Beschädigte Bücher");
 		searchMenu.add(damagedBooksMenuItem);
 
 		searchMenu.add(getAusgelieheneMenuItem());
 		searchMenu.add(getReservedMenuItem());
 	}
 
 	private void initBookMenu() {
 		bookMenu = new JMenu();
 		this.add(bookMenu);
 		bookMenu.setText("Buch");
 		bookMenu.setMnemonic(java.awt.event.KeyEvent.VK_B);
 
 		lendBokMenuItem = new JMenuItem();
 		lendBokMenuItem.setText("Dieses Buch Ausleihen");
 		bookMenu.add(lendBokMenuItem);
 
 		editBookMenuItem = new JMenuItem();
 		editBookMenuItem.setText("Katalogdaten editieren");
 		bookMenu.add(editBookMenuItem);
 
 		bookMenu.setVisible(false);
 	}
 
 	private void initUserMenu() {
 		userMenu = new JMenu();
 		this.add(userMenu);
 		userMenu.setText("Benutzer");
 		userMenu.setMnemonic(java.awt.event.KeyEvent.VK_B);
 
 		editUserMenuItem = new JMenuItem();
 		editUserMenuItem.setText("Personalien editieren");
 		userMenu.add(editUserMenuItem);
 
 		createUserMenuItem = new JMenuItem();
 		createUserMenuItem.setText("Neuen Benutzer erstellen");
 		userMenu.add(createUserMenuItem);
 
 		userMenu.setVisible(false);
 	}
 
 	private void initHelpMenu() {
 		helpMenu = new JMenu();
 		this.add(helpMenu);
 		helpMenu.setText("Hilfe");
 		helpMenu.setMnemonic(java.awt.event.KeyEvent.VK_H);
 
 		aboutMenuItem = new JMenuItem();
 		aboutMenuItem.setText("Über");
 		aboutMenuItem.setMnemonic(java.awt.event.KeyEvent.VK_B);
 		helpMenu.add(aboutMenuItem);
 	}
 
 	private AbstractAction getExitAction() {
 		if (exitAction == null) {
 			exitAction = new AbstractAction("Beenden", null) {
 				private static final long serialVersionUID = 1L;
 
 				public void actionPerformed(ActionEvent evt) {
 					System.exit(0);
 				}
 			};
 		}
 		return exitAction;
 	}
 
 	private JMenuItem getAusgelieheneMenuItem() {
 		if (ausgelieheneMenuItem == null) {
 			ausgelieheneMenuItem = new JMenuItem();
 			ausgelieheneMenuItem.setText("Ausgeliehene Bücher");
 		}
 		return ausgelieheneMenuItem;
 	}
 
 	private JMenuItem getReservedMenuItem() {
 		if (reservedMenuItem == null) {
 			reservedMenuItem = new JMenuItem();
 			reservedMenuItem.setText("Reservierte Bücher");
 		}
 		return reservedMenuItem;
 	}
 
 	/**
 	 * When switching to another menu tab, this changes the menu bar to display
 	 * the corresponding actions.
 	 * 
 	 * @param activeTab
 	 *            The new menu which should be made visible
 	 */
 	public void setActiveViewIndex(int activeTab) {
 		searchMenu.setVisible(activeTab == 0);
 		bookMenu.setVisible(activeTab == 1);
 		userMenu.setVisible(activeTab == 2);
 
 		searchMenuItem.setSelected(activeTab == 0);
 		bookMenuItem.setSelected(activeTab == 1);
 		userMenuItem.setSelected(activeTab == 2);
 	}
 
	public void update(Observable o, Object arg) {
		setActiveViewIndex(controller.tabbed_model.getActiveTab());
	}

 }
