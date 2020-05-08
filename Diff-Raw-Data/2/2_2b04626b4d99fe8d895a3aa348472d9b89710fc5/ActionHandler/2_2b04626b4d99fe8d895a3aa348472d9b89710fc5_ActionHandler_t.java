 /*
  * This file is part of jHaushalt.
  * jHaushalt is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  * jHaushalt is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License
  * along with jHaushalt; if not, see <http://www.gnu.org/licenses/>.
  * (C)opyright 2002-2010 Dr. Lars H. Hahn
  */
 
 package haushalt.gui;
 
 import haushalt.gui.action.StandardAction;
 
 import java.awt.event.KeyEvent;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 import javax.swing.JToolBar;
 
 /**
  * @author Dr. Lars H. Hahn
  * @version 2.5/2006.07.04
  */
 
 /*
  * 2006.07.04 Internationalisierung
  * 2006.01.31 Erweiterung: Kontextmenü hinzugefügt
  */
 
 public class ActionHandler {
 
 	private static final TextResource RES = TextResource.get();
 
 	private final Haushalt haushalt;
 	private final JPopupMenu popupMenu = new JPopupMenu();
 	private final List<StandardAction> menuDatei;
 	private final List<StandardAction> menuBearbeiten;
 	private final List<StandardAction> menuAusgabe;
 	private final List<StandardAction> menuExtras;
 	private final List<StandardAction> menuHilfe;
 
 	// Sonderfall: Für MacOS X muss diese Action von Hand zur Toolbar hinzugefuegt werden
 	private StandardAction preferences;
 
 	public ActionHandler(final Haushalt haushalt) {
 		super();
 		this.haushalt = haushalt;
 
 		menuDatei = erzeugeMenuDatei();
 		menuBearbeiten = erzeugeMenuBearbeiten();
 		menuAusgabe = erzeugeMenuAusgabe();
 		menuExtras = erzeugeMenuExtras();
 		menuHilfe = erzeugeMenuHilfe();
 
 		// Das PopupMenü wird mit Bearbeiten-Menü belegt.
 		for (StandardAction action : menuBearbeiten) {
 			this.popupMenu.add(new JMenuItem(action));
 		}
 	}
 
 	// 0:
 	private List<StandardAction> erzeugeMenuDatei() {
 		final List<StandardAction> standardActions = new LinkedList<StandardAction>();
 
 		standardActions.add(new StandardAction(
 			haushalt,
 			"neu",
 			RES.getString("new"),
 			"New",
 			RES.getString("new_legend"),
 			KeyEvent.VK_N));
 		standardActions.add(new StandardAction(
 			haushalt,
 			"laden",
 			RES.getString("open") + "...",
 			"Open",
 			RES.getString("open_legend"),
 			KeyEvent.VK_L));
 		standardActions.add(new StandardAction(
 			haushalt,
 			"speichern",
 			RES.getString("save"),
 			"Save",
 			RES.getString("save_legend"),
 			KeyEvent.VK_S));
 		standardActions.add(new StandardAction(
 			haushalt,
 			"speichernUnter",
 			RES.getString("save_as") + "...",
 			"SaveAs",
 			RES.getString("save_as_legend"),
 			null));
 		standardActions.add(new StandardAction(
 			haushalt,
 			"beenden",
 			RES.getString("exit"),
 			null,
 			RES.getString("exit_legend"),
 			KeyEvent.VK_X));
 
 		return standardActions;
 	}
 
 	// 1:
 	private List<StandardAction> erzeugeMenuBearbeiten() {
 		final List<StandardAction> standardActions = new LinkedList<StandardAction>();
 
 		standardActions.add(new StandardAction(
 			haushalt,
 			"neueBuchungErstellen",
 			RES.getString("new_booking") + "...",
 			"AddBuchung",
 			RES.getString("new_booking_legend"),
 			KeyEvent.VK_C));
 		standardActions.add(new StandardAction(
 			haushalt,
 			"loeschen",
 			RES.getString("delete"),
 			"Delete",
 			RES.getString("delete_legend"),
 			KeyEvent.VK_D));
 		standardActions.add(new StandardAction(
 			haushalt,
 			"umbuchen",
 			RES.getString("rebook") + "...",
 			"Umbuchung",
 			RES.getString("rebook_legend"),
 			KeyEvent.VK_U));
 		standardActions.add(new StandardAction(
 			haushalt,
 			"splitten",
 			RES.getString("split") + "...",
 			"Splitten",
 			RES.getString("split_legend"),
			KeyEvent.VK_P));
 		standardActions.add(new StandardAction(
 			haushalt,
 			"umwandeln",
 			RES.getString("convert") + "...",
 			"Umwandeln",
 			RES.getString("convert_legend"),
 			KeyEvent.VK_W));
 		standardActions.add(new StandardAction(
 			haushalt,
 			"registerBearbeiten",
 			RES.getString("edit_registers") + "...",
 			"Register",
 			RES.getString("edit_registers_legend"),
 			KeyEvent.VK_R));
 		standardActions.add(new StandardAction(
 			haushalt,
 			"kategorienBearbeiten",
 			RES.getString("edit_category") + "...",
 			"Auto",
 			RES.getString("edit_category_legend"),
 			KeyEvent.VK_K));
 		standardActions.add(new StandardAction(
 			haushalt,
 			"suchen",
 			RES.getString("find") + "...",
 			"Find",
 			RES.getString("find_legend"),
 			null));
 		standardActions.add(new StandardAction(
 			haushalt,
 			"alteBuchungenLoeschen",
 			RES.getString("delete_old_bookings") + "...",
 			null,
 			RES.getString("delete_old_bookings_legend"),
 			KeyEvent.VK_E));
 		standardActions.add(new StandardAction(
 			haushalt,
 			"kategorieErsetzen",
 			RES.getString("replace_category") + "...",
 			null,
 			RES.getString("replace_category_legend"),
 			null));
 		standardActions.add(new StandardAction(
 			haushalt,
 			"kategorienBereinigen",
 			RES.getString("clean_categories") + "...",
 			null,
 			RES.getString("clean_categories_legend"),
 			KeyEvent.VK_B));
 		standardActions.add(new StandardAction(
 			haushalt,
 			"registerVereinigen",
 			RES.getString("join_register") + "...",
 			null,
 			RES.getString("join_register_legend"),
 			KeyEvent.VK_V));
 
 		return standardActions;
 	}
 
 	// 2:
 	private List<StandardAction> erzeugeMenuAusgabe() {
 		final List<StandardAction> standardActions = new LinkedList<StandardAction>();
 
 		standardActions.add(new StandardAction(
 			haushalt,
 			"zeigeAuswertung",
 			RES.getString("show_report") + "...",
 			"Auswertung",
 			RES.getString("show_report_legend"),
 			KeyEvent.VK_A));
 		standardActions.add(new StandardAction(
 			haushalt,
 			"exportCSV",
 			RES.getString("export_csv") + "...",
 			"Export",
 			RES.getString("export_csv_legend"),
 			null));
 		standardActions.add(new StandardAction(
 			haushalt,
 			"drucken",
 			RES.getString("print") + "...",
 			"Print",
 			RES.getString("print_legend"),
 			KeyEvent.VK_P));
 
 		return standardActions;
 	}
 
 	// 3: Extras
 	private List<StandardAction> erzeugeMenuExtras() {
 		final List<StandardAction> standardActions = new LinkedList<StandardAction>();
 
 		// Sonderfall: Für MacOS X muss diese Action von Hand zur Toolbar hinzugefuegt werden
 		preferences = new StandardAction(
 			haushalt,
 			"optionen",
 			RES.getString("preferences") + "...",
 			"Preferences",
 			RES.getString("preferences_legend"),
 			KeyEvent.VK_O);
 		final StandardAction autoBuchungen = new StandardAction(haushalt, "autoBuchung", RES.getString("automatic_booking")
 			+ "...", "Robot", RES.getString("automatic_booking_legend"), null);
 		final StandardAction importCSV = new StandardAction(
 			haushalt,
 			"importCSV",
 			RES.getString("import_csv") + "...",
 			"Import",
 			RES.getString("import_csv_legend"),
 			KeyEvent.VK_I);
 		final StandardAction importQuicken = new StandardAction(haushalt, "importQuicken", RES.getString("import_quicken")
 			+ "...", null, RES.getString("import_quicken_legend"), KeyEvent.VK_Q);
 
 		if (!Haushalt.isMacOSX()) {
 			standardActions.add(preferences);
 		}
 
 		standardActions.add(autoBuchungen);
 		standardActions.add(importCSV);
 		standardActions.add(importQuicken);
 
 		return standardActions;
 	}
 
 	// 4: Hilfe
 	private List<StandardAction> erzeugeMenuHilfe() {
 		final List<StandardAction> standardActions = new LinkedList<StandardAction>();
 		final StandardAction hilfe = new StandardAction(
 			haushalt,
 			"hilfeInhalt",
 			RES.getString("help_content") + "...",
 			"Help",
 			RES.getString("help_content_legend"),
 			KeyEvent.VK_F1);
 		final StandardAction programmInfo = new StandardAction(
 			haushalt,
 			"programmInfo",
 			RES.getString("program_info") + "...",
 			"Information",
 			RES.getString("program_info_legend"),
 			null);
 
 		standardActions.add(hilfe);
 
 		if (!Haushalt.isMacOSX()) {
 			standardActions.add(programmInfo);
 		}
 
 		return standardActions;
 	}
 
 	public JMenuBar erzeugeMenuBar() {
 		final JMenuBar menuBar = new JMenuBar();
 
 		String title = "";
 		JMenu menu = null;
 
 		// Datei:
 		title = RES.getString("file");
 		menu = createMenu(title, menuDatei);
 		menuBar.add(menu);
 
 		// Bearbeiten:
 		title = RES.getString("edit");
 		menu = createMenu(title, menuBearbeiten);
 		menuBar.add(menu);
 
 		// Ausgabe:
 		title = RES.getString("output");
 		menu = createMenu(title, menuAusgabe);
 		menuBar.add(menu);
 
 		// Extras:
 		title = RES.getString("extras");
 		menu = createMenu(title, menuExtras);
 		menuBar.add(menu);
 
 		// Hilfe:
 		title = RES.getString("help");
 		menu = createMenu(title, menuHilfe);
 		menuBar.add(menu);
 
 		return menuBar;
 	}
 
 	private JMenu createMenu(final String title, final List<StandardAction> actions) {
 		final JMenu menu = new JMenu(title);
 		for (StandardAction action : actions) {
 			final JMenuItem menuItem = new JMenuItem(action);
 			menu.add(menuItem);
 		}
 		return menu;
 	}
 
 	public JToolBar erzeugeToolBar() {
 		final JToolBar toolBar = new JToolBar();
 
 		addActions(toolBar, menuDatei);
 		toolBar.addSeparator();
 		addActions(toolBar, menuBearbeiten);
 		toolBar.addSeparator();
 		addActions(toolBar, menuAusgabe);
 		toolBar.addSeparator();
 		addActions(toolBar, menuExtras);
 
 		if (Haushalt.isMacOSX()) {
 			// Sonderfall: Für MacOS X muss die Preferences-Action von Hand zur Toolbar hinzugefuegt werden
 			final List<StandardAction> preferenceList = new LinkedList<StandardAction>();
 			preferenceList.add(preferences);
 			addActions(toolBar, preferenceList);
 		}
 
 		toolBar.addSeparator();
 		addActions(toolBar, menuHilfe);
 
 		return toolBar;
 	}
 
 	private void addActions(final JToolBar toolBar, final List<StandardAction> actions) {
 		JButton button;
 		for (StandardAction action : actions) {
 			if (action.getBigIcon() != null) {
 				button = createButton(action);
 				toolBar.add(button);
 			}
 		}
 	}
 
 	private JButton createButton(final StandardAction action) {
 		final JButton button = new JButton(action);
 		button.setText("");
 		button.setIcon(action.getBigIcon());
 		return button;
 	}
 
 	public JPopupMenu getPopupMenu() {
 		return this.popupMenu;
 	}
 
 }
