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
 
 import haushalt.auswertung.CsvHandler;
 import haushalt.auswertung.DlgContainerAuswertung;
 import haushalt.auswertung.FarbPaletten;
 import haushalt.auswertung.domain.ColumnModelProperties;
 import haushalt.auswertung.domain.HaushaltProperties;
 import haushalt.auswertung.domain.HaushaltPropertiesException;
 import haushalt.auswertung.domain.HaushaltDefinitionLoader;
 import haushalt.daten.AbstractBuchung;
 import haushalt.daten.Datenbasis;
 import haushalt.daten.Datenbasis.QuickenImportException;
 import haushalt.daten.Datum;
 import haushalt.daten.EinzelKategorie;
 import haushalt.daten.Euro;
 import haushalt.daten.ExtendedDatabase;
 import haushalt.daten.SplitBuchung;
 import haushalt.daten.StandardBuchung;
 import haushalt.daten.Umbuchung;
 import haushalt.gui.dialoge.DlgAutoBuchung;
 import haushalt.gui.dialoge.DlgBereinigen;
 import haushalt.gui.dialoge.DlgEinrichtung;
 import haushalt.gui.dialoge.DlgHilfe;
 import haushalt.gui.dialoge.DlgImport;
 import haushalt.gui.dialoge.DlgInfo;
 import haushalt.gui.dialoge.DlgKategorienBearbeiten;
 import haushalt.gui.dialoge.DlgOptionen;
 import haushalt.gui.dialoge.DlgRegisterBearbeiten;
 import haushalt.gui.dialoge.DlgSplitBuchung;
 import haushalt.gui.dialoge.DlgSuchenErsetzen;
 import haushalt.gui.generischerdialog.DatumGDP;
 import haushalt.gui.generischerdialog.EineKategorieGDP;
 import haushalt.gui.generischerdialog.EuroGDP;
 import haushalt.gui.generischerdialog.GenerischerDialog;
 import haushalt.gui.generischerdialog.RegisterGDP;
 import haushalt.gui.generischerdialog.TextGDP;
 import haushalt.gui.mac.MacAdapter;
 import haushalt.service.data.DatabaseFileLoaderImpl;
 import haushalt.service.data.DatabaseService;
 import haushalt.service.data.DatabaseServiceImpl;
 import haushalt.service.data.DatabaseServiceException;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.print.PrinterException;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Locale;
 import java.util.logging.Logger;
 
 import javax.swing.DefaultCellEditor;
 import javax.swing.ImageIcon;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollBar;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 import javax.swing.JViewport;
 import javax.swing.ListSelectionModel;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.DefaultTableColumnModel;
 import javax.swing.table.TableColumnModel;
 
 
 public class Haushalt implements KeyListener, ListSelectionListener {
 	private static final Logger LOGGER = Logger.getLogger(Haushalt.class.getName());
 
 	private static final TextResource RES = TextResource.get();
 
 	private HaushaltProperties haushaltDefinition;
 	
 	private JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.BOTTOM);
 	private TableColumnModel columnModel = null;
 
 	private final GemerkteBuchungenGlassPane glassPane = new GemerkteBuchungenGlassPane();
 	private DlgOptionen dlgOptionen;
 	private DlgSuchenErsetzen dlgSuchenErsetzen;
 	private Datenbasis db;
 	private DlgContainerAuswertung containerAuswertung;
 	private ActionHandler actionHandler;
 
 	private MainWindow mainWindow;
 	
 	private final FileFilter fileFilter = new JHaushaltFileFilter();
 
 	// FIXME Dependency Injection
 	private DatabaseService databaseService;
 	
 	public Haushalt(final String dateiname) {
 		this.actionHandler = new ActionHandler(this);
 		
 		DatabaseServiceImpl internalDbService = new DatabaseServiceImpl();
 		internalDbService.setDatabaseFileLoader(new DatabaseFileLoaderImpl());
 		databaseService = internalDbService;
 		
 		// Properties laden		
 		try {
 			haushaltDefinition = HaushaltDefinitionLoader.getHaushaltDefinition();
 		} catch (HaushaltPropertiesException e) {
 			showDialogToCreateJHHFile();
 		}
 
 		mainWindow = new MainWindow(
 				haushaltDefinition.getMainWindowProperties(),
 				tabbedPane,
 				actionHandler,
 				glassPane);
 		mainWindow.defineMainWindow();
 		setLocaleAndFrameLocale();
 		setLookAndFeelDependingOnSystem();
 		
 		this.dlgSuchenErsetzen = new DlgSuchenErsetzen(this);
 		
 		defineMenuBar();
 		
 		loadOrCreateDatabaseFile(dateiname);
 		
 		defineOptionsDialog();
 		oberflaecheAnpassen(); // hier werden auch andere Optionen gesetzt
 
 		// some smaller improvements for apple-users
 		if (isMacOSX()) {
 			MacAdapter.macStyle(this);
 		}
 	}
 
 	public JFrame getFrame() {
 		return mainWindow.getFrame();
 	} 
 	
 	private void defineOptionsDialog() {
 		this.dlgOptionen = new DlgOptionen(this, haushaltDefinition.getDlgOptionProperties());
 	}
 
 	private void loadOrCreateDatabaseFile(final String dateiname) {
 		// Letzte Datei laden oder neu initialisieren
 		// In jedem Fall wird die Datenbasis erzeugt.
 		String fileToLoad = (dateiname != null)? 
 				dateiname:
 				(haushaltDefinition.getJhhFileName() != null)? 
 						haushaltDefinition.getJhhFileName() :
 						null;
 		if (fileToLoad != null) {
 			loadDatabase(new File(fileToLoad));
 		} else {
 			neu();
 		}
 	}
 
 	private void defineMenuBar() {
 		mainWindow.getFrame().setJMenuBar(this.actionHandler.erzeugeMenuBar());
 	}
 
 	private void setLookAndFeelDependingOnSystem() {
 		final String systemClassName = UIManager.getSystemLookAndFeelClassName();
 		try {
 			UIManager.setLookAndFeel(systemClassName);
 		} catch (final Exception e) {
 			LOGGER.warning(e.getMessage());
 		}
 	}
 
 	private void setLocaleAndFrameLocale() {
 		// Das Neusetzen der Locale geht leider nicht an einer zentralen Stelle
 		RES.setLocale(haushaltDefinition.getUserOrSystemLocale());
 		Locale.setDefault(RES.getLocale());
 		mainWindow.getFrame().setLocale(RES.getLocale());
 	}
 
 	private void showDialogToCreateJHHFile() {
 		final DlgEinrichtung einrichten = new DlgEinrichtung(
 				mainWindow.getFrame(),
 				haushaltDefinition.createJHHDialogProperties());
 		einrichten.pack();
 		einrichten.setVisible(true);
 	}
 
 	/**
 	 * Liefert das Arbeitsverzeichnis.
 	 * 
 	 * @return Arbeitsverzeichnis
 	 */
 	public String getOrdner() {
 		return haushaltDefinition.getJhhFolder();
 	}
 
 	// -- Einstellungen
 	// ---------------------------------------------------------
 
 	/**
 	 * Übernimmt die optionalen Einstellungen des Benutzters.
 	 * 
 	 */
 	private void oberflaecheAnpassen() {
 		setTabPlacement(haushaltDefinition.getTabPlacement());
 		for (int i = 0; i < tabbedPane.getTabCount(); i++) {
 			final JScrollPane scrollPane = (JScrollPane) tabbedPane.getComponent(i);
 			final JTable table = (JTable) scrollPane.getViewport().getView();
 			table.setSelectionBackground(getFarbeSelektion());
 			table.setGridColor(getFarbeGitter());
 		}
 		db.setStartDatum(haushaltDefinition.getTransactionStartDate());
 
 		DeleteableTextField.setDeltaste(haushaltDefinition.setDeleteKeyCode());
 		FarbPaletten.setCustomColor(haushaltDefinition.getCustomColorCodes());
 	}
 
 	public Color getFarbeSelektion() {
 		return haushaltDefinition.getSelectionColor();
 	}
 
 	public Color getFarbeGitter() {
 		return haushaltDefinition.getGridColor();
 	}
 
 	public Color getFarbeZukunft() {
 		return haushaltDefinition.getFarbeZukunft();
 	}
 
 	public String getFontname() {
 		return haushaltDefinition.getFontName();
 	}
 
 	public int getFontgroesse() {
 		return haushaltDefinition.getFontSize();
 	}
 
 	/**
 	 * Lädt Bilder aus dem Verzeichnis <code>res/</code>.
 	 * 
 	 * @param dateiname
 	 *            Dateiname des Bildes (ohne Pfad)
 	 */
 	public ImageIcon bildLaden(final String dateiname) {
 		final URLClassLoader urlLoader = (URLClassLoader) getClass().getClassLoader();
 		final URL fileLoc = urlLoader.findResource("res/" + dateiname);
 		return new ImageIcon(fileLoc);
 	}
 
 	/**
 	 * Setzt den Ort der Register-Reiter.
 	 * 
 	 * @param tabPlacement
 	 *            "TOP", "BOTTOM", "LEFT" oder "RIGHT"
 	 */
 	private void setTabPlacement(final String tabPlacement) {
 		int tp = SwingConstants.BOTTOM;
 		final Class<?> c = SwingConstants.class;
 		try {
 			final Field field = c.getField(tabPlacement);
 			tp = field.getInt(new Integer(0));
 		} catch (final NoSuchFieldException e) {
 			LOGGER.warning(e.getMessage());
 		} catch (final IllegalAccessException e) {
 			LOGGER.warning(e.getMessage());
 		}
 		tabbedPane.setTabPlacement(tp);
 	}
 
 	// -- Register-Tabellen
 	// ------------------------------------------------------
 
 	/**
 	 * Wandelt alle Register in Tabellen um und zeigt sie so an.
 	 */
 	private void zeigeAlleRegisterTabs() {
 		final String[] register = db.getRegisterNamen();
 		for (int i = 0; i < register.length; i++) {
 			zeigeRegisterTab(register[i]);
 		}
 	}
 
 	/**
 	 * Wandelt ein Register in eine Tabelle um und zeigt sie so an.
 	 * 
 	 * @param regname
 	 *            Name des Registers
 	 * @return Nummer des Registers
 	 */
 	public int zeigeRegisterTab(final String regname) {
 		final int tabNr = tabbedPane.indexOfTab(regname);
 		
 		ColumnModelProperties columnModelProperties = haushaltDefinition.getColumnModelProperties();
 		// int tabNr = getRegisterTabNr(""+tableModel);
 		if (tabNr > -1) {
 			// Register wird schon angezeigt, wahrscheinlich wurde es verändert:
 			registerVeraendert(tabNr);
 			return tabNr;
 		}
 		final RegisterTableModel tableModel = new RegisterTableModel(this, db, regname);
 		JTable table;
 		if (columnModel == null) {
 			// Das ColumnModel wird von der ersten erzeugten Tabelle genommen
 			// und initialisiert. Alle Tabellen erhalten so die gleichen Spalten-Breiten.
 			table = new JTable(tableModel);
 			columnModel = table.getColumnModel();
 			if (columnModelProperties.doesWidthExistForColumnNumber(0)) {
 				for (int j = 0; j < columnModel.getColumnCount(); j++) {
 					final int breite = columnModelProperties.getRegisterWidthForColumnNumber(j);
 					columnModel.getColumn(j).setPreferredWidth(breite);
 				}
 			}
 		} else {
 			table = new JTable(tableModel, columnModel);
 		}
 		table.setSurrendersFocusOnKeystroke(true);
 		table.getSelectionModel().addListSelectionListener(this);
 		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		table.setSelectionBackground(getFarbeSelektion());
 		table.setGridColor(getFarbeGitter());
 		table.setComponentPopupMenu(actionHandler.getPopupMenu());
 
 		// Cell-Editoren erzeugen
 		table.setDefaultEditor(Datum.class, new DefaultCellEditor(new DatumField()));
 		final DeleteableTextField textField = new DeleteableTextField();
 		textField.addKeyListener(this);
 		final DefaultCellEditor cellEditor = new DefaultCellEditor(textField);
 		table.setDefaultEditor(String.class, cellEditor);
 		table.setDefaultEditor(Object.class, new KategorieCellEditor(this, db));
 		table.setDefaultEditor(Euro.class, new DefaultCellEditor(new EuroField()));
 		// Cell-Renderer erzeugen
 		table.setDefaultRenderer(Datum.class, new DatumRenderer(haushaltDefinition.getDateRendererProperties()));
 		table.setDefaultRenderer(String.class, new DefaultTableCellRenderer());
 		table.setDefaultRenderer(Object.class, new KategorieRenderer());
 		table.setDefaultRenderer(Euro.class, new EuroRenderer());
 
 		final JScrollPane scrollPane = new JScrollPane(table);
 		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		tabbedPane.add(scrollPane, "" + tableModel);
 		table.setRowSelectionInterval(table.getRowCount() - 1, table.getRowCount() - 1);
 		final JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
 		// JavaBug: Hier ist der maxValue noch 100 ...
 		scrollBar.setValue(100);
 		// Hier hat MaxValue den tatsaechlichen Wert ...
 		scrollBar.setValue(scrollBar.getMaximum());
 
 		if (tabbedPane.getTabCount() == 1) {
 			mainWindow.getFrame().getContentPane().add(tabbedPane, BorderLayout.CENTER);
 			mainWindow.getFrame().validate();
 		}
 		mainWindow.getFrame().repaint();
 		return tabbedPane.getTabCount() - 1;
 	}
 
 	/**
 	 * Nennt den Tab eines Registers um.
 	 * 
 	 * @param alterName
 	 *            alter Name des Registers
 	 * @param neuerName
 	 *            neuer Name des Registers
 	 */
 	public void renameRegisterTab(final String alterName, final String neuerName) {
 		final int idx = tabbedPane.indexOfTab(alterName);
 		tabbedPane.setTitleAt(idx, neuerName);
 		final JScrollPane scrollPane = (JScrollPane) tabbedPane.getComponentAt(idx);
 		final JTable table = (JTable) scrollPane.getViewport().getView();
 		final RegisterTableModel tableModel = (RegisterTableModel) table.getModel();
 		tableModel.setRegisterName(neuerName);
 	}
 
 	/**
 	 * Erhöht den Index des Register-Tabs um eins.
 	 * 
 	 * @param regname
 	 *            Registername
 	 */
 	public void bewegeRegisterNachOben(final String regname) {
 		final int idx = tabbedPane.indexOfTab(regname);
 		final JScrollPane scrollPane = (JScrollPane) tabbedPane.getComponentAt(idx);
 		tabbedPane.removeTabAt(idx);
 		tabbedPane.insertTab(regname, null, scrollPane, null, idx + 1);
 	}
 
 	/**
 	 * Erniedrigt den Index des Register-Tabs um eins.
 	 * 
 	 * @param regname
 	 *            Registername
 	 */
 	public void bewegeRegisterNachUnten(final String regname) {
 		final int idx = tabbedPane.indexOfTab(regname);
 		final JScrollPane scrollPane = (JScrollPane) tabbedPane.getComponentAt(idx);
 		tabbedPane.removeTabAt(idx);
 		tabbedPane.insertTab(regname, null, scrollPane, null, idx - 1);
 	}
 
 	/**
 	 * Löscht die Tabelle zu einem Register. Falls keine Tabelle mehr vorhanden
 	 * ist,
 	 * wird wieder das Hintergrundbild angezeigt.
 	 * 
 	 * @param regname
 	 *            Name des Registers
 	 */
 	private void entferneRegisterTab(final String regname) {
 		for (int i = 0; i < tabbedPane.getTabCount(); i++) {
 			if (regname.equals(tabbedPane.getTitleAt(i))) {
 				final JScrollPane scrollPane = (JScrollPane) tabbedPane.getComponentAt(i);
 				final JTable table = (JTable) scrollPane.getViewport().getView();
 				// Die Register teilen sich ein ColumnModel, bevor ein Register
 				// gelöscht wird
 				// muss daher das ColumnModel neu gesetzt werden.
 				table.setColumnModel(new DefaultTableColumnModel());
 				tabbedPane.removeTabAt(i);
 			}
 		}
 		if (tabbedPane.getTabCount() == 0) {
 			// Wenn das letzte Register entfernt wurde, wird auch die TabbedPane
 			// entfernt.
 			// Dies ist ein Relikt aus der Zeit als noch ein Hintergrund
 			// angezeigt wurde
 			columnModel = null;
 			mainWindow.getFrame().getContentPane().remove(tabbedPane);
 			mainWindow.getFrame().validate();
 		}
 		mainWindow.getFrame().repaint();
 	}
 
 	private void entferneAlleRegisterTabs() {
 		final Container contentPane = mainWindow.getFrame().getContentPane();
 		tabbedPane.removeAll();
 		contentPane.remove(tabbedPane);
 		columnModel = null;
 		mainWindow.getFrame().validate();
 		mainWindow.getFrame().repaint();
 	}
 
 	/**
 	 * Teilt mit, dass die Daten des Registers verändert wurden
 	 * 
 	 * @param name
 	 *            Name des Registers
 	 */
 	public void registerVeraendert(final String name) {
 		registerVeraendert(tabbedPane.indexOfTab(name));
 	}
 
 	private void registerVeraendert(final int nr) {
 		final JScrollPane scrollPane = (JScrollPane) tabbedPane.getComponentAt(nr);
 		final JTable table = (JTable) scrollPane.getViewport().getView();
 		final RegisterTableModel tableModel = (RegisterTableModel) table.getModel();
 		tableModel.fireTableDataChanged();
 	}
 
 	public void alleRegisterVeraendert() {
 		for (int i = 0; i < tabbedPane.getTabCount(); i++) {
 			registerVeraendert(i);
 		}
 	}
 
 	/**
 	 * Wählt eine bestimmte Buchung in einer der Register-Tabellen aus.
 	 * 
 	 * @param regname
 	 *            Name des Registers
 	 * @param buchungIndex
 	 *            Zeile der Buchung
 	 */
 	public void selektiereBuchung(final String regname, int buchungIndex) {
 		final int tabIndex = tabbedPane.indexOfTab(regname);
 		if (tabIndex > -1) {
 			tabbedPane.setSelectedIndex(tabIndex);
 			final JScrollPane scrollPane = (JScrollPane) tabbedPane.getComponentAt(tabIndex);
 			final JViewport viewport = scrollPane.getViewport();
 			final JTable table = (JTable) viewport.getComponent(0);
 			if (buchungIndex >= table.getRowCount()) {
 				buchungIndex = table.getRowCount() - 1;
 			}
 			table.setRowSelectionInterval(buchungIndex, buchungIndex);
 			table.scrollRectToVisible(table.getCellRect(buchungIndex, 0, true));
 		} else {
 			mainWindow.setStatus("-E- "
 				+ RES.getString("status_register_not_found1")
 				+ " "
 				+ regname
 				+ " "
 				+ RES.getString("status_register_not_found2"));
 		}
 	}
 
 	// -- Gemerkte Buchungen
 	// -----------------------------------------------------
 
 	/**
 	 * Setzt die Koordinaten an denen die gemerkte Buchung erscheinen soll.
 	 * Die Koordinaten werden in Abhängigkeit von dem TextField gesetzt in dem
 	 * der Buchungstext eingegeben wird.
 	 * 
 	 * @param comp
 	 *            Component = JTextField
 	 */
 	public void setzteKoordinatenGemerkteBuchung(final Component comp) {
 		final Point xy = SwingUtilities.convertPoint(comp.getParent(), comp.getX(), comp.getY(), glassPane);
 		glassPane.setTextKoordinaten(xy);
 	}
 
 	public boolean gemerkteBuchungen() {
 		return haushaltDefinition.haveExistingTransactions();
 	}
 
 	/**
 	 * Zeigt eine zuvor gemerkte Buchung an.
 	 * 
 	 * @param prefix
 	 *            Anfang des Buchungstextes
 	 */
 	public void zeigeGemerkteBuchung(final String prefix) {
 		if (gemerkteBuchungen()) {
 			final AbstractBuchung buchung = db.findeGemerkteBuchung(prefix);
 			if (buchung != null) {
 				final String text = buchung.getText() + " / " + buchung.getKategorie() + " / " + buchung.getWert();
 				glassPane.setText(text);
 				glassPane.setVisible(true);
 			} else {
 				glassPane.setVisible(false);
 			}
 		}
 	}
 
 	/**
 	 * Überprüft, ob schon ein Register angelegt wurde
 	 * 
 	 * @return <code>false</code> mindestens ein Register vorhanden
 	 */
 	private boolean keinRegisterVorhanden() {
 		if (tabbedPane.getTabCount() == 0) {
 			final int n = JOptionPane.showConfirmDialog(
 					mainWindow.getFrame(),
 					RES.getString("message_no_register"),
 					RES.getString("warning"),
 					JOptionPane.YES_NO_OPTION);
 			if (n == JOptionPane.OK_OPTION) {
 				registerBearbeiten();
 			}
 			return true;
 		}
 		return false;
 	}
 
 	// == Menü: Datei
 	// ============================================================
 
 	/**
 	 * Überprüft, ob die Applikationsdaten geändert wurden. Wird von neu() und
 	 * beenden() aufgerufen.
 	 * 
 	 * @return <code>true</code> - Applikation wurde geändert, <code>false</code> - Daten sind gespeichert
 	 */
 	private boolean areAllFilesInASavedStatus() {
 		if (db == null) {
 			return true; // FIXME well, seems to be the most reasonable one
 		}
 		if (areThereUnsavedChanges()) {
 			final int n = JOptionPane.showConfirmDialog(mainWindow.getFrame(), RES.getString("message_data_changed"));
 			switch (n) {
 				case JOptionPane.CANCEL_OPTION:
 					return false;
 				case JOptionPane.NO_OPTION:
 					return true;
 				case JOptionPane.OK_OPTION:
 					speichern();
 					return !areThereUnsavedChanges();
 				default:
 					break;
 			}
 		}
 		return true;
 	}
 	
 	private boolean areThereUnsavedChanges() {
 		return (db.isGeaendert() || containerAuswertung.isGeaendert());
 	}
 
 	public void neu() {
 		if (areAllFilesInASavedStatus()) {
 			haushaltDefinition.setJhhFileName("");
 			entferneAlleRegisterTabs();
 			db = new Datenbasis();
 			showDialogEvaluation(db);
 			mainWindow.setCopyrightText();
 			final String name = db.erzeugeRegister(RES.getString("default_register_name"));
 			zeigeRegisterTab(name);
 			db.addUmbuchung(new Datum(), RES.getString("opening_balance"), name, name, Euro.NULL_EURO);
 		}
 	}
 
 	private void loadDatabase(final File datei) {	
 		entferneAlleRegisterTabs();		
 		reallyLoadDatabase(datei);
 		doAutomatedTransactions();
 		zeigeAlleRegisterTabs();
 		final String dateiname = datei.getPath() + ".jha";
 		showDialogEvaluation(db);
 		containerAuswertung.laden(dateiname);
 	}
 
 	private void showDialogEvaluation(Datenbasis db) {
 		containerAuswertung = new DlgContainerAuswertung(this, db);
 		containerAuswertung.setPreferredSize(getPreferredEvaluationDimension());
 	}
 
 	private void doAutomatedTransactions() {
 		final int anzahl = db.ausfuehrenAutoBuchungen(new Datum());
 		if (anzahl > 0) {
 			mainWindow.setStatus(RES.getString("executed_automatic_bookings1")
 				+ " "
 				+ anzahl
 				+ " "
 				+ RES.getString("executed_automatic_bookings2"));
 		}
 	}
 
 	private void reallyLoadDatabase(final File datei) {
 		ExtendedDatabase loadedDatabase = null;
 		
 		try {
 			loadedDatabase = databaseService.loadDatabase(datei);
 		} catch (FileNotFoundException e) {
 			handleJhhFileException("-E- " + datei.getPath() + " " + RES.getString("status_not_found"));
 		} catch (DatabaseServiceException e) {
 			handleJhhFileException("-E- " + RES.getString("status_load_error") + ": " + datei.getPath());
 		}
 
 		if (Datenbasis.givenVersionEqualsDatabaseVersion(loadedDatabase.getVersionId())
 				|| hasUserConfirmedWarningMessage(loadedDatabase.getVersionId())) {
 			this.db = loadedDatabase.getDataBase();			
 		}
 	}
 
 	private boolean hasUserConfirmedWarningMessage(String versionInfo) {
 		final String warningTitle = RES.getString("warning");
 		final String warningMessage = getWarningMessage(versionInfo);
 		int confirmationAnswer = JOptionPane.showConfirmDialog(null, warningMessage, warningTitle, JOptionPane.YES_NO_OPTION);
 		return confirmationAnswer == JOptionPane.YES_OPTION;
 	}
 	
 	private String getWarningMessage(final String versionInfo) {
 		final String warningMessage = RES.getString("message_old_version1")
 			+ " " + versionInfo + " " + RES.getString("message_old_version2")
 			+ Datenbasis.VERSION_DATENBASIS + RES.getString("message_old_version3");
 		return warningMessage;
 	}
 
 	private Dimension getPreferredEvaluationDimension() {
 		final int auswertungBreite = haushaltDefinition.getEvaluationWidth();
 		final int auswertungHoehe = haushaltDefinition.getEvaluationHeight();
 		return new Dimension(auswertungBreite, auswertungHoehe);
 	}
 
 	
 	public void laden() {
 		if (areAllFilesInASavedStatus()) {
 			final JFileChooser dateidialog = new JFileChooser();
 			dateidialog.setFileFilter(fileFilter);
 			dateidialog.setCurrentDirectory(new File(haushaltDefinition.getJhhFolder()));
 			if (dateidialog.showOpenDialog(mainWindow.getFrame()) == JFileChooser.APPROVE_OPTION) {
 				loadDatabase(dateidialog.getSelectedFile());
 			}
 		}
 	}
 
 	private void handleJhhFileException(String message) {
 		mainWindow.setStatus(message);
 		haushaltDefinition.setJhhFileName(null);
 	}
 	
 	public void speichern() {
 		final String dateiname = haushaltDefinition.getJhhFileName();
 		if (dateiname == null) {
 			speichernUnter();
 		} else {
 			speichern(new File(dateiname));
 		}
 	}
 
 	private void speichern(File datei) {
 		if (!datei.getName().toLowerCase().endsWith(".jhh")) {
 			final String name = datei.getAbsolutePath() + ".jhh";
 			datei = new File(name);
 		}
 		try {
 			databaseService.saveDbFile(db);
 			haushaltDefinition.setJhhFileName(datei.getPath());
 			mainWindow.setStatus(datei.getPath() + " " + RES.getString("status_saved") + ".");
 		} catch (final FileNotFoundException e1) {
 			mainWindow.setStatus("-E- " + datei.getPath() + " " + RES.getString("status_not_found"));
 		} catch (DatabaseServiceException e) {
 			mainWindow.setStatus("-E- " + RES.getString("status_write_error") + ": " + datei.getPath());
 		}
 
 		// Speichern der Auswertungen
 		containerAuswertung.speichern(datei.getPath() + ".jha");
 	}
 
 	public void speichernUnter() {
 		final JFileChooser dateidialog = new JFileChooser();
 		dateidialog.setFileFilter(fileFilter);
 		dateidialog.setCurrentDirectory(new File(haushaltDefinition.getJhhFolder()));
 		if (dateidialog.showSaveDialog(mainWindow.getFrame()) == JFileChooser.APPROVE_OPTION) {
			speichern(dateidialog.getSelectedFile());
 		}
 	}
 
 	public void beenden() {
 		if (areAllFilesInASavedStatus()) {
 			final Dimension dimension = tabbedPane.getSize();
 			haushaltDefinition.setProperty("jhh.register.breite", "" + dimension.width);
 			if (dimension.height > 100) {
 				haushaltDefinition.setProperty("jhh.register.hoehe", "" + dimension.height);
 			} else {
 				haushaltDefinition.setProperty("jhh.register.hoehe", "100");
 			}
 			if (columnModel != null) {
 				for (int i = 0; i < columnModel.getColumnCount(); i++) {
 					haushaltDefinition.setProperty("jhh.register.spalte" + i, "" + columnModel.getColumn(i).getWidth());
 				}
 			}
 
 			try {
 				haushaltDefinition.save();
 			} catch (HaushaltPropertiesException e) {
 				LOGGER.info("Could not save haushalt definition.");
 				e.printStackTrace();
 			}
 			
 			System.exit(0);
 		}
 	}
 
 	// === Menü: Bearbeiten
 	// ======================================================
 
 	public void suchen() {
 		if (keinRegisterVorhanden()) {
 			return;
 		}
 		dlgSuchenErsetzen.showDialog(db);
 	}
 
 	public void neueBuchungErstellen() {
 		final int registerIndex = tabbedPane.getSelectedIndex();
 		final String regname = tabbedPane.getTitleAt(registerIndex);
 		db.addStandardBuchung(regname, new StandardBuchung());
 
 		final JScrollPane scrollPane = (JScrollPane) tabbedPane.getSelectedComponent();
 		final JTable table = (JTable) scrollPane.getViewport().getView();
 		final RegisterTableModel tableModel = (RegisterTableModel) table.getModel();
 		final int buchungIndex = table.getRowCount();
 		tableModel.fireTableRowsInserted(buchungIndex, buchungIndex);
 	}
 
 	public void umbuchen() {
 		if (keinRegisterVorhanden()) {
 			return;
 		}
 		final int registerIndex = tabbedPane.getSelectedIndex();
 		final String regname = tabbedPane.getTitleAt(registerIndex);
 		final GenerischerDialog dlg = new GenerischerDialog(RES.getString("create_rebooking"), mainWindow.getFrame());
 		final DatumGDP pane1 = new DatumGDP(RES.getString("insert_date") + ":", new Datum());
 		dlg.addPane(pane1);
 		final TextGDP pane2 = new TextGDP(RES.getString("insert_posting_text") + ":", RES.getString("default_posting_text"));
 		dlg.addPane(pane2);
 		final RegisterGDP pane3 = new RegisterGDP(RES.getString("select_source_register") + ":", db, regname);
 		dlg.addPane(pane3);
 		final RegisterGDP pane4 = new RegisterGDP(RES.getString("select_destination_register") + ":", db, regname);
 		dlg.addPane(pane4);
 		final EuroGDP pane5 = new EuroGDP(RES.getString("insert_amount") + ":", new Euro());
 		dlg.addPane(pane5);
 		if (dlg.showDialog()) {
 			final Datum datum = (Datum) pane1.getRefreshedWert();
 			final String text = "" + pane2.getRefreshedWert();
 			final String quelle = "" + pane3.getRefreshedWert();
 			final String ziel = "" + pane4.getRefreshedWert();
 			final Euro betrag = (Euro) pane5.getRefreshedWert();
 			db.addUmbuchung(datum, text, quelle, ziel, betrag);
 			registerVeraendert(quelle);
 			registerVeraendert(ziel);
 		}
 	}
 
 	public void loeschen() {
 		if (keinRegisterVorhanden()) {
 			return;
 		}
 		final int registerIndex = tabbedPane.getSelectedIndex();
 		final JScrollPane scrollPane = (JScrollPane) tabbedPane.getSelectedComponent();
 		final String regname = tabbedPane.getTitleAt(registerIndex);
 		final JTable table = (JTable) scrollPane.getViewport().getView();
 		final int buchungIndex = table.getSelectedRow();
 		if (buchungIndex == -1) {
 			JOptionPane.showMessageDialog(mainWindow.getFrame(), RES.getString("message_no_row_selected"));
 			return;
 		}
 		if (buchungIndex == db.getAnzahlBuchungen(regname)) {
 			JOptionPane.showMessageDialog(mainWindow.getFrame(), RES.getString("message_row_can_not_be_deleted"));
 			return;
 		}
 		db.entferneBuchung(regname, buchungIndex);
 		final RegisterTableModel tableModel = (RegisterTableModel) table.getModel();
 		tableModel.fireTableRowsDeleted(buchungIndex, buchungIndex);
 		if (table.getCellEditor() != null) {
 			table.getCellEditor().cancelCellEditing();
 		}
 		table.requestFocus();
 		table.setRowSelectionInterval(buchungIndex, buchungIndex);
 		mainWindow.setStatus(RES.getString("status_posting_deleted1")
 			+ " "
 			+ (buchungIndex + 1)
 			+ " "
 			+ RES.getString("status_posting_deleted2")
 			+ " "
 			+ regname
 			+ " "
 			+ RES.getString("status_posting_deleted3"));
 	}
 
 	public void splitten() {
 		if (keinRegisterVorhanden()) {
 			return;
 		}
 		final int registerIndex = tabbedPane.getSelectedIndex();
 		final JScrollPane scrollPane = (JScrollPane) tabbedPane.getSelectedComponent();
 		final String regname = tabbedPane.getTitleAt(registerIndex);
 		final JTable table = (JTable) scrollPane.getViewport().getView();
 		final int buchungIndex = table.getSelectedRow();
 		if (buchungIndex == -1) {
 			JOptionPane.showMessageDialog(mainWindow.getFrame(), RES.getString("message_no_row_selected"));
 			return;
 		}
 		final AbstractBuchung buchung = db.getBuchung(regname, buchungIndex);
 		if (buchung.getClass() == Umbuchung.class) {
 			JOptionPane.showMessageDialog(mainWindow.getFrame(), RES.getString("message_rebookings_can_not_be_split"));
 		} else {
 			SplitBuchung splitBuchung;
 			if (buchung.getClass() == StandardBuchung.class) {
 				splitBuchung = new SplitBuchung((StandardBuchung) buchung);
 			} else {
 				splitBuchung = (SplitBuchung) buchung;
 			}
 			final JDialog dlg = new DlgSplitBuchung(this, db, splitBuchung);
 			dlg.pack();
 			dlg.setVisible(true);
 			final int pos = db.ersetzeBuchung(regname, buchungIndex, splitBuchung.reduziere());
 			db.buchungMerken(splitBuchung.reduziere());
 			registerVeraendert(regname);
 			selektiereBuchung(regname, pos);
 		}
 		if (table.getCellEditor() != null) {
 			table.getCellEditor().cancelCellEditing();
 		}
 		table.requestFocus();
 	}
 
 	public void umwandeln() {
 		if (keinRegisterVorhanden()) {
 			return;
 		}
 		final int registerIndex = tabbedPane.getSelectedIndex();
 		final JScrollPane scrollPane = (JScrollPane) tabbedPane.getSelectedComponent();
 		final String regname = tabbedPane.getTitleAt(registerIndex);
 		final JTable table = (JTable) scrollPane.getViewport().getView();
 		final int buchungIndex = table.getSelectedRow();
 		if (buchungIndex == -1) {
 			JOptionPane.showMessageDialog(mainWindow.getFrame(), RES.getString("message_no_row_selected"));
 			return;
 		}
 		if (buchungIndex == db.getAnzahlBuchungen(regname)) {
 			JOptionPane.showMessageDialog(mainWindow.getFrame(), RES.getString("message_row_can_not_be_deleted"));
 			return;
 		}
 		final AbstractBuchung buchung = db.getBuchung(regname, buchungIndex);
 		if (buchung.getClass() == Umbuchung.class) {
 			JOptionPane.showMessageDialog(mainWindow.getFrame(), RES.getString("message_posting_is_already_a_reposting"));
 			return;
 		}
 		final GenerischerDialog dlg = new GenerischerDialog(RES.getString("convert"), mainWindow.getFrame());
 		final RegisterGDP pane = new RegisterGDP(RES.getString("select_destination_register") + ":", db, regname);
 		dlg.addPane(pane);
 		if (dlg.showDialog()) {
 			final Datum datum = buchung.getDatum();
 			final String text = buchung.getText();
 			final String quelle = regname;
 			final String ziel = "" + pane.getRefreshedWert();
 			final Euro betrag = Euro.NULL_EURO.sub(buchung.getWert());
 			db.entferneBuchung(regname, buchungIndex);
 			db.addUmbuchung(datum, text, quelle, ziel, betrag);
 			registerVeraendert(quelle);
 			registerVeraendert(ziel);
 		}
 	}
 
 	public void registerBearbeiten() {
 		final DlgRegisterBearbeiten dlg = new DlgRegisterBearbeiten(this, db);
 		dlg.showDialog();
 	}
 
 	public void kategorienBearbeiten() {
 		final DlgKategorienBearbeiten dlg = new DlgKategorienBearbeiten(this, db);
 		dlg.showDialog();
 		alleRegisterVeraendert();
 	}
 
 	public void alteBuchungenLoeschen() {
 		final GenerischerDialog dlg = new GenerischerDialog(RES.getString("delete_old_bookings"), mainWindow.getFrame());
 		final DatumGDP pane = new DatumGDP(RES.getString("cutoff_date") + ":", new Datum());
 		pane.setPreferredSize(new Dimension(320, 60));
 		dlg.addPane(pane);
 
 		if (dlg.showDialog()) {
 			final Datum datum = (Datum) pane.getRefreshedWert();
 			db.entferneAlteBuchungen(datum);
 			alleRegisterVeraendert();
 			mainWindow.setStatus(RES.getString("status_posting_deleted4")
 				+ " "
 				+ datum
 				+ " "
 				+ RES.getString("status_posting_deleted5"));
 		}
 	}
 
 	public void kategorieErsetzen() {
 		final GenerischerDialog dlg = new GenerischerDialog(RES.getString("replace_category"), mainWindow.getFrame());
 		final EineKategorieGDP pane1 = new EineKategorieGDP(RES.getString("current_category") + ":", db, null);
 		dlg.addPane(pane1);
 		final EineKategorieGDP pane2 = new EineKategorieGDP(RES.getString("new_category") + ":", db, null);
 		dlg.addPane(pane2);
 
 		if (dlg.showDialog()) {
 			final EinzelKategorie alteKategorie = (EinzelKategorie) pane1.getRefreshedWert();
 			final EinzelKategorie neueKategorie = (EinzelKategorie) pane2.getRefreshedWert();
 			final int anzahl = db.ersetzeKategorie(alteKategorie, neueKategorie);
 			mainWindow.setStatus("" + anzahl + " " + RES.getString("status_replaced_categories"));
 			alleRegisterVeraendert();
 		}
 	}
 
 	public void kategorienBereinigen() {
 		final JDialog dlg = new DlgBereinigen(this, db);
 		dlg.pack();
 		dlg.setVisible(true);
 		alleRegisterVeraendert();
 	}
 
 	public void registerVereinigen() {
 		if (keinRegisterVorhanden()) {
 			return;
 		}
 		final int registerIndex = tabbedPane.getSelectedIndex();
 		final String regname = tabbedPane.getTitleAt(registerIndex);
 		final GenerischerDialog dlg = new GenerischerDialog(RES.getString("join_register"), mainWindow.getFrame());
 		final RegisterGDP pane1 = new RegisterGDP(RES.getString("select_source_register") + ":", db, regname);
 		pane1.setPreferredSize(new Dimension(330, 60));
 		dlg.addPane(pane1);
 		final RegisterGDP pane2 = new RegisterGDP(RES.getString("select_destination_register") + ":", db, regname);
 		pane2.setPreferredSize(new Dimension(330, 60));
 		dlg.addPane(pane2);
 
 		if (dlg.showDialog()) {
 			final String quelle = (String) pane1.getRefreshedWert();
 			final String ziel = (String) pane2.getRefreshedWert();
 			if (!quelle.equals(ziel)) {
 				db.registerVereinigen(quelle, ziel);
 				entferneRegisterTab(quelle);
 				mainWindow.setStatus(RES.getString("status_register_deleted1")
 					+ " "
 					+ quelle
 					+ " "
 					+ RES.getString("status_register_deleted2"));
 				registerVeraendert(ziel);
 			} else {
 				JOptionPane.showMessageDialog(mainWindow.getFrame(), RES.getString("message_registers_may_not_be_equal"));
 			}
 		}
 	}
 
 	// == Menü: Ausgabe
 	// ==========================================================
 
 	public void zeigeAuswertung() {
 		if (keinRegisterVorhanden()) {
 			return;
 		}
 		containerAuswertung.zeigeDialog();
 		final Dimension dimension = containerAuswertung.getSize();
 		haushaltDefinition.setProperty("jhh.auswertung.breite", "" + dimension.width);
 		haushaltDefinition.setProperty("jhh.auswertung.hoehe", "" + dimension.height);
 		containerAuswertung.setPreferredSize(dimension);
 	}
 
 	public void exportCSV() {
 		if (keinRegisterVorhanden()) {
 			return;
 		}
 		final ArrayList<String[]> buchungen = db.getBuchungen();
 		final CsvHandler handler = new CsvHandler(buchungen);
 		handler.exportDlg(mainWindow.getFrame(), haushaltDefinition.getJhhFolder());
 	}
 
 	public void drucken() {
 		final int tabIndex = tabbedPane.getSelectedIndex();
 		if (tabIndex != -1) {
 			final JScrollPane scrollPane = (JScrollPane) tabbedPane.getComponentAt(tabIndex);
 			final JViewport viewport = scrollPane.getViewport();
 			final JTable table = (JTable) viewport.getComponent(0);
 			final MessageFormat header = new MessageFormat(RES.getString("register") + ": " + tabbedPane.getTitleAt(tabIndex));
 			final MessageFormat footer = new MessageFormat(RES.getString("message_printed_with"));
 			try {
 				table.print(JTable.PrintMode.FIT_WIDTH, header, footer, true, null, true);
 			} catch (final PrinterException e) {
 				LOGGER.warning(e.getMessage());
 			}
 		}
 	}
 
 	// == Menü: Extras
 	// ===========================================================
 
 	public void optionen() {
 		dlgOptionen.showDialog();
 		oberflaecheAnpassen();
 	}
 
 	public void autoBuchung() {
 		if (keinRegisterVorhanden()) {
 			return;
 		}
 		final DlgAutoBuchung dlg = new DlgAutoBuchung(this, db);
 		dlg.zeigeDialog();
 		alleRegisterVeraendert();
 	}
 
 	public void importCSV() {
 		if (keinRegisterVorhanden()) {
 			return;
 		}
 		final int registerIndex = tabbedPane.getSelectedIndex();
 		final String regname = tabbedPane.getTitleAt(registerIndex);
 		final DlgImport spaltenZuordnung = new DlgImport(this);
 		spaltenZuordnung.pack();
 		spaltenZuordnung.setVisible(true);
 		final String[][] importTabelle = spaltenZuordnung.getImportTabelle();
 		if (importTabelle != null) {
 			db.importBuchungen(regname, importTabelle);
 			mainWindow.setStatus("" + importTabelle.length + " " + RES.getString("status_postings_imported"));
 			registerVeraendert(registerIndex);
 		}
 	}
 
 	public void importQuicken() {
 		if (keinRegisterVorhanden()) {
 			return;
 		}
 		final int registerIndex = tabbedPane.getSelectedIndex();
 		final String regname = tabbedPane.getTitleAt(registerIndex);
 		final GenerischerDialog dlg = new GenerischerDialog(RES.getString("import_quicken"), mainWindow.getFrame());
 		final RegisterGDP pane = new RegisterGDP(RES.getString("select_register") + ":", db, regname);
 		pane.setPreferredSize(new Dimension(250, 60));
 		dlg.addPane(pane);
 		if (dlg.showDialog()) {
 			final JFileChooser dateidialog = new JFileChooser();
 			dateidialog.setCurrentDirectory(new File(haushaltDefinition.getJhhFolder()));
 			if (dateidialog.showOpenDialog(mainWindow.getFrame()) == JFileChooser.APPROVE_OPTION) {
 				final File datei = dateidialog.getSelectedFile();
 				try {
 					final FileInputStream in = new FileInputStream(datei);
 					db.importQuickenRegister(
 							in, (String) pane.getRefreshedWert(), 
 							haushaltDefinition.isDataImportInEuroCurrency());
 					in.close();
 					zeigeAlleRegisterTabs();
 				} catch (final FileNotFoundException e1) {
 					LOGGER.warning("-E- " + datei.getPath() + " nicht gefunden!");
 					mainWindow.setStatus(e1.getMessage());
 				} catch (final IOException e2) {
 					LOGGER.warning("-E- Fehler beim Importieren: " + datei.getPath());
 					mainWindow.setStatus(e2.getMessage());
 				} catch (final QuickenImportException e) {
 					LOGGER.warning("-E- Fehler beim Importieren: " + datei.getPath());
 					mainWindow.setStatus(e.getMessage());
 				}
 			}
 		}
 	}
 
 	// == Menü: Hilfe
 	// ============================================================
 
 	/**
 	 * Wird von Menü-Handler bei Auswahl von '<b>Hilfe/Inhalt</b>' aufgerufen.
 	 * Es wird standardmäßig die Datei "html/help.html" angezeigt.
 	 */
 	public void hilfeInhalt() {
 		final DlgHilfe dlg = new DlgHilfe(mainWindow.getFrame());
 		dlg.pack();
 		dlg.setVisible(true);
 	}
 
 	/**
 	 * Wird von Menü-Handler bei Auswahl von '<b>Hilfe/Programm-Info</b>'
 	 * aufgerufen.
 	 */
 	public void programmInfo() {
 		final DlgInfo dlg = new DlgInfo(mainWindow.getFrame());
 		dlg.pack();
 		dlg.setVisible(true);
 	}
 
 	// -- Methoden des Interface 'KeyListener'
 	// -----------------------------------
 
 	public void keyReleased(final KeyEvent e) {
 		if (e.getSource().getClass() == DeleteableTextField.class) {
 			final DeleteableTextField textField = (DeleteableTextField) e.getSource();
 			setzteKoordinatenGemerkteBuchung(textField);
 			zeigeGemerkteBuchung(textField.getText());
 		}
 	}
 
 	public void keyPressed(final KeyEvent e) {
 		// nichts zu tun !
 	}
 
 	public void keyTyped(final KeyEvent e) {
 		// nichts zu tun !
 	}
 
 	// -- Methoden des Interface 'ListSelectionListener'
 	// ----------------------------
 
 	public void valueChanged(final ListSelectionEvent e) {
 		glassPane.setVisible(false);
 	}
 
 	public static boolean isMacOSX() {
 		final String osName = System.getProperty("os.name");
 		return osName.startsWith("Mac OS X");
 	}
 
 	// -- MAIN
 	// -------------------------------------------------------------------
 
 	public static void main(final String[] args) {
 		final String version = System.getProperty("java.specification.version");
 		if (version.compareTo("1.5") < 0) {
 			JOptionPane.showMessageDialog(
 					null,
 					RES.getString("message_wrong_java_version1")
 						+ " "
 						+ version
 						+ " "
 						+ RES.getString("message_wrong_java_version2"));
 			System.exit(0);
 		}
 		String dateiname = null;
 		if ((args.length > 0) && (!args[0].equals(""))) {
 			dateiname = args[0];
 		}
 
 		// small 'hack' for Apple-users
 		if (isMacOSX()) {
 			System.setProperty("apple.laf.useScreenMenuBar", "true");
 			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "jHaushalt");
 		}
 
 		final Haushalt haushalt = new Haushalt(dateiname);
 		SwingUtilities.invokeLater(new Runnable() {
 
 			public void run() {
 				final JFrame mainFrame = haushalt.getFrame();
 				mainFrame.pack();
 				// Frame mittig im Bildschirm platzieren:
 				mainFrame.setLocationRelativeTo(null);
 				mainFrame.setVisible(true);
 			}
 		});
 	}
 
 }
