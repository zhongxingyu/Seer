 // %119582289:de.hattrickorganizer.gui.statistic%
 package de.hattrickorganizer.gui.statistic;
 
 import gui.HOColorName;
 import gui.HOIconName;
 import gui.UserParameter;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusListener;
 import java.awt.event.ItemListener;
 import java.util.Vector;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 
 import plugins.IMatchLineupPlayer;
 import plugins.IRefreshable;
 import plugins.ISpielePanel;
 import plugins.ISpielerPosition;
 import de.hattrickorganizer.database.DBZugriff;
 import de.hattrickorganizer.gui.RefreshManager;
 import de.hattrickorganizer.gui.model.CBItem;
 import de.hattrickorganizer.gui.model.StatistikModel;
 import de.hattrickorganizer.gui.templates.ImagePanel;
 import de.hattrickorganizer.gui.theme.ThemeManager;
 import de.hattrickorganizer.model.HOVerwaltung;
 import de.hattrickorganizer.model.matches.MatchKurzInfo;
 import de.hattrickorganizer.model.matches.MatchLineupPlayer;
 import de.hattrickorganizer.model.matches.Matchdetails;
 import de.hattrickorganizer.tools.HOLogger;
 import de.hattrickorganizer.tools.Helper;
 import de.hattrickorganizer.tools.PlayerHelper;
 
 /**
  * Das StatistikPanel
  */
 public class SpieleStatistikPanel extends ImagePanel
     implements ActionListener, FocusListener, ItemListener, IRefreshable
 {
 	private static final long serialVersionUID = 3954095099686666846L;
 
     //~ Static fields/initializers -----------------------------------------------------------------
 
 	private Color ratingColor 			= ThemeManager.getColor(HOColorName.STAT_RATING2);
     private Color midfieldColor 		= ThemeManager.getColor(HOColorName.SHIRT_MIDFIELD);
     private Color rightWingbackColor 	= ThemeManager.getColor(HOColorName.SHIRT_WINGBACK).darker();
     private Color centralDefenceColor 	= ThemeManager.getColor(HOColorName.SHIRT_CENTRALDEFENCE);
     private Color leftWingbackColor 	= ThemeManager.getColor(HOColorName.SHIRT_WINGBACK).brighter();
     private Color rightForwardColor 	= ThemeManager.getColor(HOColorName.SHIRT_WING).darker();
     private Color forwardColor 			= ThemeManager.getColor(HOColorName.SHIRT_FORWARD);
     private Color leftForwardColor 		= ThemeManager.getColor(HOColorName.SHIRT_WING).brighter();
     private Color totalColor 			= ThemeManager.getColor(HOColorName.STAT_TOTAL);
     private Color moodColor 			= ThemeManager.getColor(HOColorName.STAT_MOOD);
     private Color confidenceColor 		= ThemeManager.getColor(HOColorName.STAT_CONFIDENCE);
     private Color hatStatsColor 		= ThemeManager.getColor(HOColorName.STAT_HATSTATS);
     private Color loddarStatsColor 		= ThemeManager.getColor(HOColorName.STAT_LODDAR);
     //~ Instance fields ----------------------------------------------------------------------------
 
     private ImageCheckbox m_jchAbwehrzentrum = new ImageCheckbox(HOVerwaltung.instance().getLanguageString("Abwehrzentrum"),
     		centralDefenceColor,UserParameter.instance().statistikSpieleAbwehrzentrum);
 	private ImageCheckbox m_jchAngriffszentrum = new ImageCheckbox(HOVerwaltung
 			.instance().getLanguageString("Angriffszentrum"),forwardColor,
 			UserParameter.instance().statistikSpieleAngriffszentrum);
 	private ImageCheckbox m_jchBewertung = new ImageCheckbox(HOVerwaltung
 			.instance().getLanguageString("Bewertung"),ratingColor,
 			UserParameter.instance().statistikSpieleBewertung);
 	private ImageCheckbox m_jchGesamt = new ImageCheckbox(HOVerwaltung
 			.instance().getLanguageString("Gesamtstaerke"), totalColor,
 			UserParameter.instance().statistikSpieleGesamt);
 	private ImageCheckbox m_jchLinkeAbwehr = new ImageCheckbox(HOVerwaltung
 			.instance().getLanguageString("linkeAbwehrseite"),leftWingbackColor,
 			UserParameter.instance().statistikSpieleLinkeAbwehr);
 	private ImageCheckbox m_jchLinkerAngriff = new ImageCheckbox(HOVerwaltung
 			.instance().getLanguageString("linkeAngriffsseite"),leftForwardColor,
 			UserParameter.instance().statistikSpieleLinkerAngriff);
 	private ImageCheckbox m_jchMittelfeld = new ImageCheckbox(HOVerwaltung
 			.instance().getLanguageString("Mittelfeld"), midfieldColor,
 			UserParameter.instance().statistikSpieleMittelfeld);
 	private ImageCheckbox m_jchRechteAbwehr = new ImageCheckbox(HOVerwaltung
 			.instance().getLanguageString("rechteAbwehrseite"), rightWingbackColor,
 			UserParameter.instance().statistikSpieleRechteAbwehr);
 	private ImageCheckbox m_jchRechterAngriff = new ImageCheckbox(HOVerwaltung
 			.instance().getLanguageString("rechteAngriffsseite"),rightForwardColor,
 			UserParameter.instance().statistikSpieleRechterAngriff);
 	private ImageCheckbox m_jchSelbstvertrauen = new ImageCheckbox(HOVerwaltung
 			.instance().getLanguageString("Selbstvertrauen"), confidenceColor,
 			UserParameter.instance().statistikSpieleSelbstvertrauen);
 	private ImageCheckbox m_jchStimmung = new ImageCheckbox(HOVerwaltung
 			.instance().getLanguageString("Stimmung"),moodColor,
 			UserParameter.instance().statistikSpieleStimmung);
 	private ImageCheckbox m_jchHatStats = new ImageCheckbox(HOVerwaltung
 			.instance().getLanguageString("Hatstats"),hatStatsColor,
 			UserParameter.instance().statistikSpieleHatStats);
 	private ImageCheckbox m_jchLoddarStats = new ImageCheckbox(HOVerwaltung
 			.instance().getLanguageString("LoddarStats"),loddarStatsColor,
 			UserParameter.instance().statistikSpieleLoddarStats);
 	private JButton m_jbDrucken = new JButton(ThemeManager.getIcon(HOIconName.PRINTER));
 	private JButton m_jbUbernehmen = new JButton(HOVerwaltung.instance()
 			.getLanguageString("Uebernehmen"));
 	private JCheckBox m_jchBeschriftung = new JCheckBox(HOVerwaltung.instance()
 			.getLanguageString("Beschriftung"),
 			UserParameter.instance().statistikSpielerFinanzenBeschriftung);
 	private JCheckBox m_jchHilflinien = new JCheckBox(HOVerwaltung.instance()
 			.getLanguageString("Hilflinien"),
 			UserParameter.instance().statistikSpielerFinanzenHilfslinien);
 	private JComboBox m_jcbSpieleFilter;
 	private JTextField m_jtfAnzahlHRF = new JTextField(
 			UserParameter.instance().statistikSpielerFinanzenAnzahlHRF + "", 5);
 	private StatistikPanel m_clStatistikPanel;
 	private CBItem[] SPIELEFILTER = {
 			new CBItem(HOVerwaltung.instance().getLanguageString(
 					"NurEigeneSpiele"), ISpielePanel.NUR_EIGENE_SPIELE
 					+ ISpielePanel.NUR_GESPIELTEN_SPIELE),
 			new CBItem(HOVerwaltung.instance().getLanguageString(
 					"NurEigenePflichtspiele"),
 					ISpielePanel.NUR_EIGENE_PFLICHTSPIELE
 							+ ISpielePanel.NUR_GESPIELTEN_SPIELE),
 			new CBItem(HOVerwaltung.instance().getLanguageString(
 					"NurEigenePokalspiele"),
 					ISpielePanel.NUR_EIGENE_POKALSPIELE
 							+ ISpielePanel.NUR_GESPIELTEN_SPIELE),
 			new CBItem(HOVerwaltung.instance().getLanguageString(
 					"NurEigeneLigaspiele"), ISpielePanel.NUR_EIGENE_LIGASPIELE
 					+ ISpielePanel.NUR_GESPIELTEN_SPIELE),
 			new CBItem(HOVerwaltung.instance().getLanguageString(
 					"NurEigeneFreundschaftsspiele"),
 					ISpielePanel.NUR_EIGENE_FREUNDSCHAFTSSPIELE
 							+ ISpielePanel.NUR_GESPIELTEN_SPIELE) };
 	private boolean m_bInitialisiert;
 
     // ~ Constructors
 	// -------------------------------------------------------------------------------
 
     /**
 	 * Creates a new SpieleStatistikPanel object.
 	 */
     public SpieleStatistikPanel() {
         RefreshManager.instance().registerRefreshable(this);
         initComponents();
         // initStatistik();
     }
 
     // ~ Methods
 	// ------------------------------------------------------------------------------------
 
     public final void setInitialisiert(boolean init) {
         m_bInitialisiert = init;
     }
 
     public final boolean isInitialisiert() {
         return m_bInitialisiert;
     }
 
     // -- Helper
     public final double getMaxValue(double[] werte) {
         double max = 0;
 
         for (int i = 0; (werte != null) && (i < werte.length); i++) {
             if (werte[i] > max) {
                 max = werte[i];
             }
         }
 
         return (max);
     }
 
     // --------Listener-------------------------------
     public final void actionPerformed(java.awt.event.ActionEvent actionEvent) {
         if (actionEvent.getSource().equals(m_jbUbernehmen)) {
             initStatistik();
         } else if (actionEvent.getSource().equals(m_jbDrucken)) {
             m_clStatistikPanel.doPrint(HOVerwaltung.instance().getLanguageString("Spiele"));
         } else if (actionEvent.getSource().equals(m_jchHilflinien)) {
             m_clStatistikPanel.setHilfslinien(m_jchHilflinien.isSelected());
             UserParameter.instance().statistikSpielerFinanzenHilfslinien = m_jchHilflinien
                                                                                .isSelected();
         } else if (actionEvent.getSource().equals(m_jchBeschriftung)) {
             m_clStatistikPanel.setBeschriftung(m_jchBeschriftung.isSelected());
             UserParameter.instance().statistikSpielerFinanzenBeschriftung = m_jchBeschriftung
                                                                                 .isSelected();
         } else if (actionEvent.getSource().equals(m_jchBewertung.getCheckbox())) {
             m_clStatistikPanel.setShow("Bewertung", m_jchBewertung.isSelected());
             UserParameter.instance().statistikSpieleBewertung = m_jchBewertung.isSelected();
         } else if (actionEvent.getSource().equals(m_jchGesamt.getCheckbox())) {
             m_clStatistikPanel.setShow("Gesamtstaerke", m_jchGesamt.isSelected());
             UserParameter.instance().statistikSpieleGesamt = m_jchGesamt.isSelected();
         } else if (actionEvent.getSource().equals(m_jchMittelfeld.getCheckbox())) {
             m_clStatistikPanel.setShow("Mittelfeld", m_jchMittelfeld.isSelected());
             UserParameter.instance().statistikSpieleMittelfeld = m_jchMittelfeld.isSelected();
         } else if (actionEvent.getSource().equals(m_jchRechteAbwehr.getCheckbox())) {
             m_clStatistikPanel.setShow("RechteAbwehr", m_jchRechteAbwehr.isSelected());
             UserParameter.instance().statistikSpieleRechteAbwehr = m_jchRechteAbwehr.isSelected();
         } else if (actionEvent.getSource().equals(m_jchAbwehrzentrum.getCheckbox())) {
             m_clStatistikPanel.setShow("Abwehrzentrum", m_jchAbwehrzentrum.isSelected());
             UserParameter.instance().statistikSpieleAbwehrzentrum = m_jchAbwehrzentrum
                                                                         .isSelected();
         } else if (actionEvent.getSource().equals(m_jchLinkeAbwehr.getCheckbox())) {
             m_clStatistikPanel.setShow("LinkeAbwehr", m_jchLinkeAbwehr.isSelected());
             UserParameter.instance().statistikSpieleLinkeAbwehr = m_jchLinkeAbwehr.isSelected();
         } else if (actionEvent.getSource().equals(m_jchRechterAngriff.getCheckbox())) {
             m_clStatistikPanel.setShow("RechterAngriff", m_jchRechterAngriff.isSelected());
             UserParameter.instance().statistikSpieleRechterAngriff = m_jchRechterAngriff
                                                                          .isSelected();
         } else if (actionEvent.getSource().equals(m_jchAngriffszentrum.getCheckbox())) {
             m_clStatistikPanel.setShow("Angriffszentrum", m_jchAngriffszentrum.isSelected());
             UserParameter.instance().statistikSpieleAngriffszentrum = m_jchAngriffszentrum
                                                                           .isSelected();
         } else if (actionEvent.getSource().equals(m_jchLinkerAngriff.getCheckbox())) {
             m_clStatistikPanel.setShow("LinkerAngriff", m_jchLinkerAngriff.isSelected());
             UserParameter.instance().statistikSpieleLinkerAngriff = m_jchLinkerAngriff
                                                                         .isSelected();
         } else if (actionEvent.getSource().equals(m_jchStimmung.getCheckbox())) {
             m_clStatistikPanel.setShow("Stimmung", m_jchStimmung.isSelected());
             UserParameter.instance().statistikSpieleStimmung = m_jchStimmung.isSelected();
         } else if (actionEvent.getSource().equals(m_jchHatStats.getCheckbox())) {
             m_clStatistikPanel.setShow("Hatstats", m_jchHatStats.isSelected());
             UserParameter.instance().statistikSpieleHatStats = m_jchHatStats.isSelected();
         } else if (actionEvent.getSource().equals(m_jchLoddarStats.getCheckbox())) {
             m_clStatistikPanel.setShow("LoddarStats", m_jchLoddarStats.isSelected());
             UserParameter.instance().statistikSpieleLoddarStats = m_jchLoddarStats.isSelected();
         } else if (actionEvent.getSource().equals(m_jchSelbstvertrauen.getCheckbox())) {
             m_clStatistikPanel.setShow("Selbstvertrauen", m_jchSelbstvertrauen.isSelected());
             UserParameter.instance().statistikSpieleSelbstvertrauen = m_jchSelbstvertrauen
                                                                           .isSelected();
         }
     }
 
     public final void doInitialisieren() {
         initStatistik();
         m_bInitialisiert = true;
     }
 
     public void focusGained(java.awt.event.FocusEvent focusEvent) {
     }
 
     public final void focusLost(java.awt.event.FocusEvent focusEvent) {
         Helper.parseInt(de.hattrickorganizer.gui.HOMainFrame.instance(),
                                                    ((JTextField) focusEvent.getSource()), false);
     }
 
     public final void itemStateChanged(java.awt.event.ItemEvent itemEvent) {
         if (itemEvent.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
             initStatistik();
         }
     }
 
     //-------Refresh---------------------------------
     public final void reInit() {
         m_bInitialisiert = false;
 
         //initStatistik();
     }
 
     public void refresh() {
     }
 
     private void initComponents() {
         JLabel label;
 
         final GridBagLayout layout = new GridBagLayout();
         final GridBagConstraints constraints = new GridBagConstraints();
         constraints.fill = GridBagConstraints.HORIZONTAL;
         constraints.weightx = 0.0;
         constraints.weighty = 0.0;
         constraints.insets = new Insets(2, 0, 2, 0);
 
         setLayout(layout);
 
         final JPanel panel2 = new ImagePanel();
         final GridBagLayout layout2 = new GridBagLayout();
         final GridBagConstraints constraints2 = new GridBagConstraints();
         constraints2.fill = GridBagConstraints.HORIZONTAL;
         constraints2.weightx = 0.0;
         constraints2.weighty = 0.0;
         constraints2.insets = new Insets(2, 2, 2, 2);
 
         panel2.setLayout(layout2);
 
         constraints2.gridx = 0;
         constraints2.gridy = 0;
         constraints2.gridwidth = 2;
         constraints2.fill = GridBagConstraints.NONE;
         constraints2.anchor = GridBagConstraints.WEST;
         m_jbDrucken.setToolTipText(HOVerwaltung.instance().getLanguageString("tt_Statistik_drucken"));
         m_jbDrucken.setPreferredSize(new Dimension(25, 25));
         m_jbDrucken.addActionListener(this);
         layout2.setConstraints(m_jbDrucken, constraints2);
         panel2.add(m_jbDrucken);
 
         label = new JLabel(HOVerwaltung.instance().getLanguageString("Wochen"));
         constraints2.fill = GridBagConstraints.HORIZONTAL;
         constraints2.anchor = GridBagConstraints.WEST;
         constraints2.gridx = 0;
         constraints2.gridy = 1;
         constraints2.gridwidth = 1;
         layout2.setConstraints(label, constraints2);
         panel2.add(label);
         m_jtfAnzahlHRF.setHorizontalAlignment(SwingConstants.RIGHT);
         m_jtfAnzahlHRF.addFocusListener(this);
         constraints2.gridx = 1;
         constraints2.gridy = 1;
         layout2.setConstraints(m_jtfAnzahlHRF, constraints2);
         panel2.add(m_jtfAnzahlHRF);
 
         constraints2.gridx = 0;
         constraints2.gridy = 2;
         constraints2.gridwidth = 2;
         layout2.setConstraints(m_jbUbernehmen, constraints2);
         m_jbUbernehmen.setToolTipText(HOVerwaltung.instance().getLanguageString("tt_Statistik_HRFAnzahluebernehmen"));
         m_jbUbernehmen.addActionListener(this);
         panel2.add(m_jbUbernehmen);
 
         constraints2.gridx = 0;
         constraints2.gridy = 3;
         constraints2.gridwidth = 2;
         m_jcbSpieleFilter = new JComboBox(SPIELEFILTER);
         m_jcbSpieleFilter.setPreferredSize(new Dimension(150, 25));
         Helper.markierenComboBox(m_jcbSpieleFilter,
 				UserParameter.instance().statistikSpieleFilter);
         m_jcbSpieleFilter.addItemListener(this);
         m_jcbSpieleFilter.setFont(m_jcbSpieleFilter.getFont().deriveFont(Font.BOLD));
         layout2.setConstraints(m_jcbSpieleFilter, constraints2);
         panel2.add(m_jcbSpieleFilter);
 
         constraints2.gridwidth = 2;
         constraints2.gridx = 0;
         constraints2.gridy = 5;
         m_jchHilflinien.setOpaque(false);
         m_jchHilflinien.setBackground(Color.white);
         m_jchHilflinien.addActionListener(this);
         layout2.setConstraints(m_jchHilflinien, constraints2);
         panel2.add(m_jchHilflinien);
 
         constraints2.gridwidth = 2;
         constraints2.gridx = 0;
         constraints2.gridy = 6;
         m_jchBeschriftung.setOpaque(false);
         m_jchBeschriftung.setBackground(Color.white);
         m_jchBeschriftung.addActionListener(this);
         layout2.setConstraints(m_jchBeschriftung, constraints2);
         panel2.add(m_jchBeschriftung);
 
         constraints2.gridwidth = 2;
         constraints2.gridx = 0;
         constraints2.gridy = 7;
         m_jchBewertung.setOpaque(false);
         m_jchBewertung.addActionListener(this);
         layout2.setConstraints(m_jchBewertung, constraints2);
         panel2.add(m_jchBewertung);
 
         constraints2.gridwidth = 2;
         constraints2.gridx = 0;
         constraints2.gridy = 8;
         m_jchHatStats.setOpaque(false);
         m_jchHatStats.addActionListener(this);
         layout2.setConstraints(m_jchHatStats, constraints2);
         panel2.add(m_jchHatStats);
         
         constraints2.gridwidth = 2;
         constraints2.gridx = 0;
         constraints2.gridy = 9;
         m_jchLoddarStats.setOpaque(false);
         m_jchLoddarStats.addActionListener(this);
         layout2.setConstraints(m_jchLoddarStats, constraints2);
         panel2.add(m_jchLoddarStats);
         
         constraints2.gridwidth = 2;
         constraints2.gridx = 0;
         constraints2.gridy = 10;
         m_jchGesamt.setOpaque(false);
         m_jchGesamt.addActionListener(this);
         layout2.setConstraints(m_jchGesamt, constraints2);
         panel2.add(m_jchGesamt);
 
         constraints2.gridwidth = 2;
         constraints2.gridx = 0;
         constraints2.gridy = 11;
         m_jchMittelfeld.setOpaque(false);
         m_jchMittelfeld.addActionListener(this);
         layout2.setConstraints(m_jchMittelfeld, constraints2);
         panel2.add(m_jchMittelfeld);
 
         constraints2.gridwidth = 2;
         constraints2.gridx = 0;
         constraints2.gridy = 12;
         m_jchRechteAbwehr.setOpaque(false);
         m_jchRechteAbwehr.addActionListener(this);
         layout2.setConstraints(m_jchRechteAbwehr, constraints2);
         panel2.add(m_jchRechteAbwehr);
 
         constraints2.gridwidth = 2;
         constraints2.gridx = 0;
         constraints2.gridy = 13;
         m_jchAbwehrzentrum.setOpaque(false);
         m_jchAbwehrzentrum.addActionListener(this);
         layout2.setConstraints(m_jchAbwehrzentrum, constraints2);
         panel2.add(m_jchAbwehrzentrum);
 
         constraints2.gridwidth = 2;
         constraints2.gridx = 0;
         constraints2.gridy = 14;
         m_jchLinkeAbwehr.setOpaque(false);
         m_jchLinkeAbwehr.addActionListener(this);
         layout2.setConstraints(m_jchLinkeAbwehr, constraints2);
         panel2.add(m_jchLinkeAbwehr);
 
         constraints2.gridwidth = 2;
         constraints2.gridx = 0;
         constraints2.gridy = 15;
         m_jchRechterAngriff.setOpaque(false);
         m_jchRechterAngriff.addActionListener(this);
         layout2.setConstraints(m_jchRechterAngriff, constraints2);
         panel2.add(m_jchRechterAngriff);
 
         constraints2.gridwidth = 2;
         constraints2.gridx = 0;
         constraints2.gridy = 16;
         m_jchAngriffszentrum.setOpaque(false);
         m_jchAngriffszentrum.addActionListener(this);
         layout2.setConstraints(m_jchAngriffszentrum, constraints2);
         panel2.add(m_jchAngriffszentrum);
 
         constraints2.gridwidth = 2;
         constraints2.gridx = 0;
         constraints2.gridy = 17;
         m_jchLinkerAngriff.setOpaque(false);
         m_jchLinkerAngriff.addActionListener(this);
         layout2.setConstraints(m_jchLinkerAngriff, constraints2);
         panel2.add(m_jchLinkerAngriff);
 
         constraints2.gridwidth = 2;
         constraints2.gridx = 0;
         constraints2.gridy = 18;
         m_jchStimmung.setOpaque(false);
         m_jchStimmung.addActionListener(this);
         layout2.setConstraints(m_jchStimmung, constraints2);
         panel2.add(m_jchStimmung);
 
         constraints2.gridwidth = 2;
         constraints2.gridx = 0;
         constraints2.gridy = 19;
         m_jchSelbstvertrauen.setOpaque(false);
         m_jchSelbstvertrauen.addActionListener(this);
         layout2.setConstraints(m_jchSelbstvertrauen, constraints2);
         panel2.add(m_jchSelbstvertrauen);
 
         constraints.fill = GridBagConstraints.HORIZONTAL;
         constraints.gridx = 0;
         constraints.gridy = 1;
         constraints.gridwidth = 1;
         constraints.weightx = 0.01;
         constraints.weighty = 0.001;
         constraints.anchor = GridBagConstraints.NORTH;
         layout.setConstraints(panel2, constraints);
         add(panel2);
 
         //Table
         final JPanel panel = new ImagePanel();
         panel.setLayout(new BorderLayout());
 
         m_clStatistikPanel = new StatistikPanel(false);
         panel.add(m_clStatistikPanel);
 
         constraints.fill = GridBagConstraints.BOTH;
         constraints.gridx = 1;
         constraints.gridy = 1;
         constraints.gridwidth = 1;
         constraints.weighty = 1.0;
         constraints.weightx = 1.0;
         constraints.anchor = GridBagConstraints.NORTH;
         panel.setBorder(BorderFactory.createLineBorder(ThemeManager.getColor(HOColorName.PANEL_BORDER)));
         layout.setConstraints(panel, constraints);
         add(panel);
     }
 
     /**
      * TODO Missing Method Documentation
      */
     private void initStatistik() {
         try {
             int anzahlHRF = Integer.parseInt(m_jtfAnzahlHRF.getText());
 
             if (anzahlHRF <= 0) {
                 anzahlHRF = 1;
             }
 
             UserParameter.instance().statistikSpielerFinanzenAnzahlHRF = anzahlHRF;
             UserParameter.instance().statistikSpieleFilter = ((CBItem) m_jcbSpieleFilter.getSelectedItem()).getId();
 
             final java.text.NumberFormat format = Helper.DEFAULTDEZIMALFORMAT;
             final java.text.NumberFormat format2 = Helper.INTEGERFORMAT;
             final java.text.NumberFormat format3 = Helper.DEZIMALFORMAT_2STELLEN;
 
             final MatchKurzInfo[] matchkurzinfos = DBZugriff
 					.instance()
 					.getMatchesKurzInfo(
 							HOVerwaltung.instance().getModel().getBasics()
 									.getTeamId(),
 							((CBItem) m_jcbSpieleFilter.getSelectedItem()).getId(), true);
 
 			final int anzahl = Math.min(matchkurzinfos.length, anzahlHRF);
 			final int teamid = HOVerwaltung.instance().getModel().getBasics().getTeamId();
 
 			final double[][] statistikWerte = new double[14][anzahl];
 			
 			// Infos zusammenstellen
 			for (int i = 0; i < anzahl; i++) {
 				final Matchdetails details = DBZugriff.instance()
 						.getMatchDetails(matchkurzinfos[matchkurzinfos.length - i - 1]
 										.getMatchID());
 
                 int bewertungwert = 0;
                 double loddarStats = 0;
                 // Für match
                 int sublevel = 0;
 
                 // Für gesamtstärke
                 double temp = 0d;
 
                 if (details.getHeimId() == teamid) {
                     sublevel = (details.getHomeMidfield()) % 4;
 
                     // (int)Math.floor ( ( (float)bewertungwert)/4f ) +1;
                     bewertungwert = ((details.getHomeMidfield() - 1) / 4) + 1;
                     statistikWerte[1][i] = bewertungwert
                                            + PlayerHelper.getValue4Sublevel(sublevel);
                     sublevel = (details.getHomeRightDef()) % 4;
 
                     //(int)Math.floor ( ( (float)bewertungwert)/4f ) +1;
                     bewertungwert = ((details.getHomeRightDef() - 1) / 4) + 1;
                     statistikWerte[2][i] = bewertungwert
                                            + PlayerHelper.getValue4Sublevel(sublevel);
                     sublevel = (details.getHomeMidDef()) % 4;
 
                     //(int)Math.floor ( ( (float)bewertungwert)/4f ) +1;
                     bewertungwert = ((details.getHomeMidDef() - 1) / 4) + 1;
                     statistikWerte[3][i] = bewertungwert
                                            + PlayerHelper.getValue4Sublevel(sublevel);
                     sublevel = (details.getHomeLeftDef()) % 4;
 
                     //(int)Math.floor ( ( (float)bewertungwert)/4f ) +1;
                     bewertungwert = ((details.getHomeLeftDef() - 1) / 4) + 1;
                     statistikWerte[4][i] = bewertungwert
                                            + PlayerHelper.getValue4Sublevel(sublevel);
                     sublevel = (details.getHomeRightAtt()) % 4;
 
                     //(int)Math.floor ( ( (float)bewertungwert)/4f ) +1;
                     bewertungwert = ((details.getHomeRightAtt() - 1) / 4) + 1;
                     statistikWerte[5][i] = bewertungwert
                                            + PlayerHelper.getValue4Sublevel(sublevel);
                     sublevel = (details.getHomeMidAtt()) % 4;
 
                     //(int)Math.floor ( ( (float)bewertungwert)/4f ) +1;
                     bewertungwert = ((details.getHomeMidAtt() - 1) / 4) + 1;
                     statistikWerte[6][i] = bewertungwert
                                            + PlayerHelper.getValue4Sublevel(sublevel);
                     sublevel = (details.getHomeLeftAtt()) % 4;
 
                     //(int)Math.floor ( ( (float)bewertungwert)/4f ) +1;
                     bewertungwert = ((details.getHomeLeftAtt() - 1) / 4) + 1;
                     statistikWerte[7][i] = bewertungwert
                                            + PlayerHelper.getValue4Sublevel(sublevel);
                     temp = details.getHomeGesamtstaerke(false);
                     sublevel = ((int) temp) % 4;
                     statistikWerte[8][i] = (((int) temp - 1) / 4) + 1
                                            + PlayerHelper.getValue4Sublevel(sublevel);
                     statistikWerte[11][i] = details.getHomeHatStats();
                     // Calculate and return the LoddarStats rating
                     statistikWerte[12][i] = details.getHomeLoddarStats();
                 } else {
                     sublevel = (details.getGuestMidfield()) % 4;
 
                     //(int)Math.floor ( ( (float)bewertungwert)/4f ) +1;
                     bewertungwert = ((details.getGuestMidfield() - 1) / 4) + 1;
                     statistikWerte[1][i] = bewertungwert
                                            + PlayerHelper.getValue4Sublevel(sublevel);
                     sublevel = (details.getGuestRightDef()) % 4;
 
                     //(int)Math.floor ( ( (float)bewertungwert)/4f ) +1;
                     bewertungwert = ((details.getGuestRightDef() - 1) / 4) + 1;
                     statistikWerte[2][i] = bewertungwert
                                            + PlayerHelper.getValue4Sublevel(sublevel);
                     sublevel = (details.getGuestMidDef()) % 4;
 
                     //(int)Math.floor ( ( (float)bewertungwert)/4f ) +1;
                     bewertungwert = ((details.getGuestMidDef() - 1) / 4) + 1;
                     statistikWerte[3][i] = bewertungwert
                                            + PlayerHelper.getValue4Sublevel(sublevel);
                     sublevel = (details.getGuestLeftDef()) % 4;
 
                     //(int)Math.floor ( ( (float)bewertungwert)/4f ) +1;
                     bewertungwert = ((details.getGuestLeftDef() - 1) / 4) + 1;
                     statistikWerte[4][i] = bewertungwert
                                            + PlayerHelper.getValue4Sublevel(sublevel);
                     sublevel = (details.getGuestRightAtt()) % 4;
 
                     //(int)Math.floor ( ( (float)bewertungwert)/4f ) +1;
                     bewertungwert = ((details.getGuestRightAtt() - 1) / 4) + 1;
                     statistikWerte[5][i] = bewertungwert
                                            + PlayerHelper.getValue4Sublevel(sublevel);
                     sublevel = (details.getGuestMidAtt()) % 4;
 
                     //(int)Math.floor ( ( (float)bewertungwert)/4f ) +1;
                     bewertungwert = ((details.getGuestMidAtt() - 1) / 4) + 1;
                     statistikWerte[6][i] = bewertungwert
                                            + PlayerHelper.getValue4Sublevel(sublevel);
                     sublevel = (details.getGuestLeftAtt()) % 4;
 
                     //(int)Math.floor ( ( (float)bewertungwert)/4f ) +1;
                     bewertungwert = ((details.getGuestLeftAtt() - 1) / 4) + 1;
                     statistikWerte[7][i] = bewertungwert
                                            + PlayerHelper.getValue4Sublevel(sublevel);
                     temp = details.getGuestGesamtstaerke(false);
                     sublevel = ((int) temp) % 4;
                     statistikWerte[8][i] = (((int) temp - 1) / 4) + 1
                                            + PlayerHelper.getValue4Sublevel(sublevel);
                     statistikWerte[11][i] = details.getAwayHatStats();
                     // Calculate and return the LoddarStats rating
                     statistikWerte[12][i] = details.getAwayLoddarStats();
                 }
 
                 //Stimmung, Selbstvertrauen
                 final int hrfid = DBZugriff.instance().getHRFID4Date(
 						matchkurzinfos[matchkurzinfos.length - i - 1]
 								.getMatchDateAsTimestamp());
 				final int[] stimmungSelbstvertrauen = DBZugriff.instance()
 						.getStimmmungSelbstvertrauenValues(hrfid);
 
 				statistikWerte[9][i] = stimmungSelbstvertrauen[0];
 				statistikWerte[10][i] = stimmungSelbstvertrauen[1];
 
 				statistikWerte[13][i] = matchkurzinfos[matchkurzinfos.length
 						- i - 1].getMatchDateAsTimestamp().getTime();
 
 				final Vector<IMatchLineupPlayer> team = DBZugriff.instance()
 						.getMatchLineupPlayers(
 								matchkurzinfos[matchkurzinfos.length - i - 1]
 										.getMatchID(), teamid);
                 float sterne = 0;
 
                 // Sterne
                 for (int j = 0; j < team.size(); j++) {
                     final MatchLineupPlayer player = (MatchLineupPlayer) team.get(j);
 
                    if (player.getId() < ISpielerPosition.substKeeper) {
                         float rating = (float) player.getRating();
 
                         if (rating > 0) {
                             sterne += rating;
                         }
                     }
                 }
 
                 statistikWerte[0][i] = sterne;
             }
 
             final StatistikModel[] models = new StatistikModel[statistikWerte.length];
 
             //Es sind 13 Werte!
             if (statistikWerte.length > 0) {
                 double faktor = 20 / getMaxValue(statistikWerte[0]);
                 models[0] = new StatistikModel(statistikWerte[0], "Bewertung",
                                                m_jchBewertung.isSelected(), ratingColor, format,
                                                faktor);
                 models[1] = new StatistikModel(statistikWerte[1], "Mittelfeld",
                                                m_jchMittelfeld.isSelected(), midfieldColor, format);
                 models[2] = new StatistikModel(statistikWerte[2], "RechteAbwehr",
                                                m_jchRechteAbwehr.isSelected(), rightWingbackColor, format);
                 models[3] = new StatistikModel(statistikWerte[3], "Abwehrzentrum",
                                                m_jchAbwehrzentrum.isSelected(), centralDefenceColor,
                                                format);
                 models[4] = new StatistikModel(statistikWerte[4], "LinkeAbwehr",
                                                m_jchRechteAbwehr.isSelected(), leftWingbackColor, format);
                 models[5] = new StatistikModel(statistikWerte[5], "RechterAngriff",
                                                m_jchRechterAngriff.isSelected(), rightForwardColor,
                                                format);
                 models[6] = new StatistikModel(statistikWerte[6], "Angriffszentrum",
                                                m_jchAngriffszentrum.isSelected(), forwardColor,
                                                format);
                 models[7] = new StatistikModel(statistikWerte[7], "LinkerAngriff",
                                                m_jchLinkerAngriff.isSelected(), leftForwardColor,
                                                format);
                 models[8] = new StatistikModel(statistikWerte[8], "Gesamtstaerke",
                                                m_jchGesamt.isSelected(), totalColor, format3);
                 models[9] = new StatistikModel(statistikWerte[9], "Stimmung",
                                                m_jchStimmung.isSelected(), moodColor, format2);
                 models[10] = new StatistikModel(statistikWerte[10], "Selbstvertrauen",
                                                 m_jchSelbstvertrauen.isSelected(), confidenceColor,
                                                 format2);
                 faktor = 20 / getMaxValue(statistikWerte[11]);
                 models[11] = new StatistikModel(statistikWerte[11], "Hatstats",
                         m_jchHatStats.isSelected(), hatStatsColor,
                         format2, faktor);
                 faktor = 20 / getMaxValue(statistikWerte[12]);
                 models[12] = new StatistikModel(statistikWerte[12], "LoddarStats",
                         m_jchLoddarStats.isSelected(), loddarStatsColor,
                         format3, faktor);
             }
 
             final String[] yBezeichnungen = Helper.convertTimeMillisToFormatString(statistikWerte[13]);
 
             m_clStatistikPanel.setAllValues(models, yBezeichnungen, format,
                                             HOVerwaltung.instance().getLanguageString("Spiele"),
                                             "", m_jchBeschriftung.isSelected(),
                                             m_jchHilflinien.isSelected());
         } catch (Exception e) {
             HOLogger.instance().log(getClass(),"SpieleStatistikPanel.initStatistik : " + e);
             HOLogger.instance().log(getClass(),e);
         }
 
         //Test
         //double[] werte = { 1d, 2d, 1.5d, 3d, 2.5d };
         //StatistikModel[] model   = { new StatistikModel( werte, "Fuehrung", true, FUEHRUNG ) };
         //m_clStatistikPanel.setModel ( model );
     }
     private double hq(double value) {
         return (2 * value) / (value + 80);
     }
 }
