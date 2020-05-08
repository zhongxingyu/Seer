 // %1562433378:de.hattrickorganizer.gui.menu%
 package de.hattrickorganizer.gui.menu;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.swing.BorderFactory;
 import javax.swing.DefaultListModel;
 import javax.swing.InputMap;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JDialog;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.KeyStroke;
 import javax.swing.ListSelectionModel;
 import javax.swing.SpinnerDateModel;
 
 import de.hattrickorganizer.gui.HOMainFrame;
 import de.hattrickorganizer.gui.RefreshManager;
 import de.hattrickorganizer.gui.login.ProxyDialog;
 import de.hattrickorganizer.gui.model.CBItem;
 import de.hattrickorganizer.gui.templates.ImagePanel;
 import de.hattrickorganizer.gui.utils.HOTheme;
 import de.hattrickorganizer.model.HOVerwaltung;
 import de.hattrickorganizer.tools.HOLogger;
 import de.hattrickorganizer.tools.extension.StadiumCreator;
 import de.hattrickorganizer.tools.extension.StandingCreator;
 
 /**
  * Dialog, der den User den Download von verschiedenen Daten aus Hattrick heraus
  * ermöglicht
  */
 public class DownloadDialog extends JDialog implements ActionListener {
 
 	private static final long serialVersionUID = 7837303870465506844L;
 
 	// ~ Instance fields
 	// ----------------------------------------------------------------------------
 	private JButton m_jbAbbrechen = new JButton(HOVerwaltung.instance().getLanguageString("Abbrechen"));
 	final private JButton m_jbDownload = new JButton(HOVerwaltung.instance().getLanguageString("Download"));
	private JButton m_jbProxy = new JButton(HOVerwaltung.instance().getLanguageString("ConfigureProxy"));
 	private JCheckBox m_jchAlterSpielplan = new JCheckBox(HOVerwaltung.instance().getLanguageString("FixturesDownload"), false);
 	private JCheckBox m_jchEigenenSpiele = new JCheckBox(HOVerwaltung.instance().getLanguageString("AktuellerSpielplanDownload"),
 			gui.UserParameter.instance().currentMatchlist);
 	private JCheckBox m_jchHRF = new JCheckBox(HOVerwaltung.instance().getLanguageString("HRFDownload"),
 			gui.UserParameter.instance().xmlDownload);
 	private JCheckBox m_jchMatchArchiv = new JCheckBox(HOVerwaltung.instance().getLanguageString("Matcharchiv"), false);
 	private JCheckBox m_jchSpielplan = new JCheckBox(HOVerwaltung.instance().getLanguageString("FixturesDownload"), gui.UserParameter
 			.instance().fixtures);
 	private JList m_jlAlterSeasons = new JList();
 	private SpinnerDateModel m_clSpinnerModel = new SpinnerDateModel();
 	private JSpinner m_jsSpinner = new JSpinner(m_clSpinnerModel);
 
 	// ~ Constructors
 	// -------------------------------------------------------------------------------
 
 	/**
 	 * Creates a new DownloadDialog object.
 	 */
 	public DownloadDialog() {
 		super(HOMainFrame.instance(), de.hattrickorganizer.model.HOVerwaltung.instance().getLanguageString("Download"), true);
 		initComponents();
 	}
 
 	// ~ Methods
 	// ------------------------------------------------------------------------------------
 
 	// ------------------------------------------------------------------------
 	public final void actionPerformed(ActionEvent e) {
 		if (e.getSource().equals(m_jchAlterSpielplan)) {
 			m_jlAlterSeasons.setEnabled(m_jchAlterSpielplan.isSelected());
 		} else if (e.getSource().equals(m_jbDownload)) {
 			startDownload();
 			RefreshManager.instance().doReInit();
 			setVisible(false);
 		} else if (e.getSource().equals(m_jbAbbrechen)) {
 			setVisible(false);
 		} else if (e.getSource().equals(m_jbProxy)) {
 			new ProxyDialog(HOMainFrame.instance());
 		}
 	}
 
 	/**
 	 * Fill season list box.
 	 */
 	private void fillSpielplanListe() {
 		final int aktuelleSaison = HOVerwaltung.instance().getModel().getBasics().getSeason();
 		final DefaultListModel listModel = new DefaultListModel();
 
 		for (int i = aktuelleSaison; i > 0; i--) {
 			listModel.addElement(new CBItem(HOVerwaltung.instance().getLanguageString("Season") + " " + i, i));
 		}
 		m_jlAlterSeasons.setModel(listModel);
 	}
 
 	/**
 	 * Initialize the GUI components.
 	 */
 	private void initComponents() {
 		if (HOTheme.getDefaultFont() != null) {
 			setFont(HOTheme.getDefaultFont());
 		}
 		setResizable(false);
 		setContentPane(new ImagePanel(null));
 
 		final JPanel normalDownloadPanel = new ImagePanel(new GridLayout(3, 1, 4, 4));
 		normalDownloadPanel.setBorder(BorderFactory.createTitledBorder(HOVerwaltung.instance().getLanguageString("Download")));
 
 		m_jchHRF.setToolTipText(HOVerwaltung.instance().getLanguageString("tt_Download_XML"));
 		m_jchEigenenSpiele.setToolTipText(HOVerwaltung.instance().getLanguageString("tt_Download_AktuellerSpielplan"));
 		m_jchSpielplan.setToolTipText(HOVerwaltung.instance().getLanguageString("tt_Download_Ligatabelle"));
 		m_jchHRF.setOpaque(false);
 		m_jchEigenenSpiele.setOpaque(false);
 		m_jchSpielplan.setOpaque(false);
 		normalDownloadPanel.add(m_jchHRF);
 		normalDownloadPanel.add(m_jchEigenenSpiele);
 		normalDownloadPanel.add(m_jchSpielplan);
 
 		normalDownloadPanel.setSize(200, 200);
 		normalDownloadPanel.setLocation(10, 10);
 		getContentPane().add(normalDownloadPanel);
 
 		final JPanel speziellerDownload = new ImagePanel(new GridLayout(1, 1, 4, 4));
 		speziellerDownload.setBorder(BorderFactory.createTitledBorder(HOVerwaltung.instance().getLanguageString("Verschiedenes")));
 
 		// Alte Spielpläne
 		final JPanel alteSpielplaenePanel = new ImagePanel(new BorderLayout());
 
 		m_jchAlterSpielplan.setToolTipText(HOVerwaltung.instance().getLanguageString("tt_Download_AlteLigatabelle"));
 		m_jchAlterSpielplan.addActionListener(this);
 		m_jchAlterSpielplan.setOpaque(false);
 		alteSpielplaenePanel.add(m_jchAlterSpielplan, BorderLayout.NORTH);
 
 		m_jlAlterSeasons.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
 		m_jlAlterSeasons.setEnabled(false);
 		fillSpielplanListe();
 		alteSpielplaenePanel.add(new JScrollPane(m_jlAlterSeasons), BorderLayout.CENTER);
 
 		// MatchArchiv
 		final JPanel matchArchivPanel = new JPanel(new BorderLayout(1, 2));
 		matchArchivPanel.setOpaque(false);
 
 		m_jchMatchArchiv.setToolTipText(HOVerwaltung.instance().getLanguageString("tt_Download_Matcharchiv"));
 		m_jchMatchArchiv.addActionListener(this);
 		m_jchMatchArchiv.setOpaque(false);
 		matchArchivPanel.add(m_jchMatchArchiv, BorderLayout.WEST);
 
 		m_clSpinnerModel.setCalendarField(java.util.Calendar.MONTH);
 		((JSpinner.DateEditor) m_jsSpinner.getEditor()).getFormat().applyPattern("dd.MM.yyyy");
 
 		// JSpinner.DateEditor m_jspDateSpinner = new JSpinner.DateEditor(
 		// m_jsSpinner, "dd-MM-yyyy" );
 		matchArchivPanel.add(m_jsSpinner, BorderLayout.EAST);
 
 		alteSpielplaenePanel.add(matchArchivPanel, BorderLayout.SOUTH);
 
 		speziellerDownload.add(alteSpielplaenePanel);
 
 		speziellerDownload.setSize(300, 200);
 		speziellerDownload.setLocation(220, 10);
 		getContentPane().add(speziellerDownload);
 
 		m_jbDownload.setToolTipText(HOVerwaltung.instance().getLanguageString("tt_Download_Start"));
 		m_jbDownload.addActionListener(this);
 		m_jbDownload.setFont(m_jbDownload.getFont().deriveFont(Font.BOLD));
 		m_jbDownload.setSize(140, 30);
 		m_jbDownload.setLocation(10, 220);
 		InputMap buttonKeys = m_jbDownload.getInputMap(JButton.WHEN_FOCUSED);
 		buttonKeys.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0,false), "pressed");
 		buttonKeys.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0,true), "released");
 
 		getContentPane().add(m_jbDownload);
 		
 		m_jbProxy.setToolTipText("Proxy configuration"); // TODO
 		m_jbProxy.addActionListener(this);
 		m_jbProxy.setFont(m_jbProxy.getFont().deriveFont(Font.BOLD));
 		m_jbProxy.setSize(140, 30);
 		m_jbProxy.setLocation(195, 220);
 		
 		getContentPane().add(m_jbProxy);
 
 		m_jbAbbrechen.setToolTipText(HOVerwaltung.instance().getLanguageString("tt_Download_Abbrechen"));
 		m_jbAbbrechen.addActionListener(this);
 		m_jbAbbrechen.setSize(140, 30);
 		m_jbAbbrechen.setLocation(380, 220);
 		getContentPane().add(m_jbAbbrechen);
 
 		setSize(530, 280);
 
 		final Dimension size = getToolkit().getScreenSize();
 
 		if (size.width > this.getSize().width) {
 			// Mittig positionieren
 			this.setLocation((size.width / 2) - (this.getSize().width / 2), (size.height / 2) - (this.getSize().height / 2));
 		}
 		
 		addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowOpened(WindowEvent e) {
 				HOLogger.instance().log(getClass(), "Request focus 1");
 				boolean succ = m_jbDownload.requestFocusInWindow();
 				HOLogger.instance().log(getClass(), "Request success 1: " + succ);
 			}
 		});
 		
 		setVisible(true);
 	}
 
 	/**
 	 * The download action.
 	 */
 	private void startDownload() {
 		if (m_jchEigenenSpiele.isSelected()) {
 			// Nur, wenn der Spielplan gezogen wurde auch die Lineups holen
 			if (HOMainFrame.instance().getOnlineWorker().getMatches(HOVerwaltung.instance().getModel().getBasics().getTeamId(), false)) {
 				// Zu allen vorhandenen Matches die Lineups holen, wenn noch
 				// nicht vorhanden
 				HOMainFrame.instance().getOnlineWorker().getAllLineups();
 				StadiumCreator.extractHistoric();
 			}
 		}
 
 		if (m_jchMatchArchiv.isSelected()) {
 			final java.util.GregorianCalendar tempdate = new java.util.GregorianCalendar();
 			tempdate.setTimeInMillis(m_clSpinnerModel.getDate().getTime());
 			if (HOMainFrame.instance().getOnlineWorker().getMatchArchiv(HOVerwaltung.instance().getModel().getBasics().getTeamId(),
 					tempdate)) {
 				// Zu allen vorhandenen Matches die Lineups holen, wenn noch
 				// nicht vorhanden
 				HOMainFrame.instance().getOnlineWorker().getAllLineups();
 			}
 		}
 
 		if (m_jchSpielplan.isSelected()) {
 			// Immer aktuelle Saison und Liga
 			// ligaid );
 			HOMainFrame.instance().getOnlineWorker().getSpielplan(-1, -1);
 			StandingCreator.extractActual();
 		}
 
 		if (m_jchAlterSpielplan.isSelected()) {
 			if (m_jlAlterSeasons.getSelectedValues() != null) {
 				final Object[] saisons = m_jlAlterSeasons.getSelectedValues();
 
 				for (int i = 0; i < saisons.length; i++) {
 					if (saisons[i] instanceof CBItem) {
 						// Liga
 						final int saisonid = ((CBItem) saisons[i]).getId();
 
 						// Abfragen!
 						final LigaAuswahlDialog auswahlDialog = new LigaAuswahlDialog(this, saisonid);
 						final int ligaid = auswahlDialog.getLigaID();
 
 						if (ligaid > -2) {
 							HOMainFrame.instance().getOnlineWorker().getSpielplan(saisonid, ligaid);
 						}
 					}
 				}
 			}
 		}
 
 		// Als letztes, damit die Matches für die Trainingsberechnung schon
 		// vorhanden sind
 		if (m_jchHRF.isSelected()) {
 			HOMainFrame.instance().getOnlineWorker().getHrf();
 		}
 	}
 }
