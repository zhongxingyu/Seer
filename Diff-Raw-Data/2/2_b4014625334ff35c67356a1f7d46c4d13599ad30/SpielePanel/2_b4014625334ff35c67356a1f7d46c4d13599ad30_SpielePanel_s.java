 // %393632151:de.hattrickorganizer.gui.matches%
 package de.hattrickorganizer.gui.matches;
 
 import gui.HOColorName;
 import gui.HOIconName;
 import gui.UserParameter;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseListener;
 import java.awt.image.BufferedImage;
 import java.util.Vector;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JViewport;
 
 import plugins.ILineUp;
 import plugins.IMPTeamData;
 import plugins.IMPTeamRatings;
 import plugins.IMatchDetails;
 import plugins.IMatchKurzInfo;
 import plugins.IMatchLineupPlayer;
 import plugins.IMatchPredictionManager;
 import plugins.ISpielerPosition;
 import de.hattrickorganizer.database.DBZugriff;
 import de.hattrickorganizer.gui.HOMainFrame;
 import de.hattrickorganizer.gui.RefreshManager;
 import de.hattrickorganizer.gui.Refreshable;
 import de.hattrickorganizer.gui.model.CBItem;
 import de.hattrickorganizer.gui.model.MatchesColumnModel;
 import de.hattrickorganizer.gui.templates.ColorLabelEntry;
 import de.hattrickorganizer.gui.templates.ImagePanel;
 import de.hattrickorganizer.gui.theme.ImageUtilities;
 import de.hattrickorganizer.gui.theme.ThemeManager;
 import de.hattrickorganizer.logik.MatchUpdater;
 import de.hattrickorganizer.model.HOMiniModel;
 import de.hattrickorganizer.model.HOVerwaltung;
 import de.hattrickorganizer.model.Lineup;
 import de.hattrickorganizer.model.matches.MatchKurzInfo;
 import de.hattrickorganizer.model.matches.MatchLineupPlayer;
 import de.hattrickorganizer.model.matches.Matchdetails;
 import de.hattrickorganizer.tools.HOLogger;
 import de.hattrickorganizer.tools.Helper;
 
 
 public final class SpielePanel extends ImagePanel implements MouseListener, KeyListener,
                                                              Refreshable, ItemListener,
                                                              ActionListener, plugins.ISpielePanel
 {
 	private static final long serialVersionUID = -6337569355347545083L;
 	
     //~ Instance fields ----------------------------------------------------------------------------
 
     private AufstellungsSternePanel m_jpAufstellungGastPanel;
     private AufstellungsSternePanel m_jpAufstellungHeimPanel;
     private JButton m_jbAufstellungUebernehmen = new JButton(ThemeManager.getIcon(HOIconName.GETLINEUP));
     private JButton m_jbDrucken = new JButton(ThemeManager.getIcon(HOIconName.PRINTER));
     private JButton m_jbLoeschen = new JButton(new ImageIcon(ImageUtilities
 			.getImageDurchgestrichen(new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB), Color.red, new Color(200, 0, 0))));
     private JButton m_jbReloadMatch = new JButton(ThemeManager.getIcon(HOIconName.RELOAD));
     private JButton m_jbSimMatch = new JButton(ThemeManager.getIcon(HOIconName.SIMULATEMATCH));
     private JButton m_jbStatistics = new JButton(ThemeManager.getIcon(HOIconName.STATISTICS));
     
     private JComboBox m_jcbSpieleFilter;
 
 
     private JPanel aufstellungsPanel;
     private JSplitPane horizontalLeftSplitPane;
     private JSplitPane verticalSplitPane;
     private JTabbedPane m_jtpSpieldetails;
     private ManschaftsBewertungsPanel m_jpManschaftsBewertungsPanel;
     private ManschaftsBewertungs2Panel m_jpManschaftsBewertungs2Panel;
     private MatchKurzInfo m_clMatchKurzInfo;
     private MatchberichtPanel m_jpMatchberichtPanel;
     private SpielHighlightPanel m_jpSpielHighlightPanel;
     private MatchesTable m_jtSpieleTable;
     private StaerkenvergleichPanel m_jpStaerkenvergleichsPanel;
 
     private CBItem[] SPIELEFILTER = {
 			new CBItem(HOVerwaltung.instance().getLanguageString("AlleSpiele"),ALLE_SPIELE),
 			new CBItem(HOVerwaltung.instance().getLanguageString("NurEigeneSpiele"), NUR_EIGENE_SPIELE),
 			new CBItem(HOVerwaltung.instance().getLanguageString("NurEigenePflichtspiele"), NUR_EIGENE_PFLICHTSPIELE),
 			new CBItem(HOVerwaltung.instance().getLanguageString("NurEigenePokalspiele"), NUR_EIGENE_POKALSPIELE),
 			new CBItem(HOVerwaltung.instance().getLanguageString("NurEigeneLigaspiele"), NUR_EIGENE_LIGASPIELE),
 			new CBItem(HOVerwaltung.instance().getLanguageString("NurEigeneFreundschaftsspiele"),NUR_EIGENE_FREUNDSCHAFTSSPIELE),
 			new CBItem(HOVerwaltung.instance().getLanguageString("NurFremdeSpiele"), NUR_FREMDE_SPIELE)
 	};
 
     public SpielePanel() {
         RefreshManager.instance().registerRefreshable(this);
 
         initComponents();
     }
 
 
     /**
 	 * Gibt die aktuellen DividerLocations zurück, damit sie gespeichert werden
 	 * können
 	 */
     public int[] getDividerLocations() {
         final int[] locations = new int[2];
         locations[0] = horizontalLeftSplitPane.getDividerLocation();
         locations[1] = verticalSplitPane.getDividerLocation();
         return locations;
     }
 
     /**
      * Gibt die Reihenfolge der Spalten in der Tabelle zurück.
      */
     public int[][] getSpaltenreihenfolge() {
         return m_jtSpieleTable.getSpaltenreihenfolge();
     }
 
     public void saveColumnOrder(){
     	m_jtSpieleTable.saveColumnOrder();
     }
     //----------------------Listener    
     public void actionPerformed(ActionEvent e) {
         if (e.getSource().equals(m_jbReloadMatch)) {
             final int matchid = m_clMatchKurzInfo.getMatchID();
 			HOMainFrame.instance().getOnlineWorker().getMatchlineup(m_clMatchKurzInfo.getMatchID(),
 					m_clMatchKurzInfo.getHeimID(), m_clMatchKurzInfo.getGastID());
 			HOMainFrame.instance().getOnlineWorker().getMatchDetails(m_clMatchKurzInfo.getMatchID());
 			MatchUpdater.updateMatch(HOMiniModel.instance(), // Dragettho werte setzen
 					m_clMatchKurzInfo.getMatchID());
 			RefreshManager.instance().doReInit();
 			HOMainFrame.instance().showMatch(matchid);
         } else if (e.getSource().equals(m_jbLoeschen)) {
             final int[] rows = m_jtSpieleTable.getSelectedRows();
             final MatchKurzInfo[] infos = new MatchKurzInfo[rows.length];
 
             for (int i = 0; i < rows.length; i++) {
                 infos[i] = ((MatchesColumnModel) m_jtSpieleTable.getSorter().getModel())
                            .getMatch((int) ((ColorLabelEntry) m_jtSpieleTable.getSorter().getValueAt(rows[i], 5)).getZahl());
             }
 
             String text = HOVerwaltung.instance().getLanguageString("loeschen");
 
             if (infos.length > 1) {
                 text += (" (" + infos.length + " "
                 + HOVerwaltung.instance().getLanguageString("Spiele")
                 + ") : ");
             } else {
                 text += " : ";
             }
 
             for (int i = 0; (i < infos.length) && (i < 11); i++) {
                 text += ("\n" + infos[i].getHeimName() + " - " + infos[i].getGastName());
 
                 if (i == 10) {
                     text += "\n ... ";
                 }
             }
 
             final int value = JOptionPane.showConfirmDialog(this, text, HOVerwaltung.instance().getLanguageString("loeschen"),
 					JOptionPane.YES_NO_OPTION);
 
             if (value == JOptionPane.YES_OPTION) {
                 for (int i = 0; i < infos.length; i++) {
                     DBZugriff.instance().deleteMatch(infos[i].getMatchID());
                 }
                 RefreshManager.instance().doReInit();
             }
         } else if (e.getSource().equals(m_jbDrucken)) {
             if (m_clMatchKurzInfo != null) {
                 final SpielePrintDialog printDialog = new SpielePrintDialog(m_clMatchKurzInfo);
                 printDialog.doPrint(m_clMatchKurzInfo.getHeimName() + " : "
                                     + m_clMatchKurzInfo.getGastName() + " - "
                                     + m_clMatchKurzInfo.getMatchDate());
                 printDialog.setVisible(false);
                 printDialog.dispose();
             }
         } else if (e.getSource().equals(m_jbAufstellungUebernehmen)) {
             if ((m_clMatchKurzInfo != null)
             		&& (m_clMatchKurzInfo.getMatchStatus() == IMatchKurzInfo.FINISHED)) {
                 final int teamid = HOVerwaltung.instance().getModel().getBasics().getTeamId();
                 final Vector<IMatchLineupPlayer> vteamspieler = DBZugriff.instance().getMatchLineupPlayers(m_clMatchKurzInfo.getMatchID(),
                                                                                        teamid);
                 final Lineup aufstellung = HOVerwaltung.instance().getModel().getAufstellung();
 
                 aufstellung.clearLineup(); // To make sure the old one is gone.
                 
                 for (int i = 0; (vteamspieler != null) && (i < vteamspieler.size()); i++) {
                     final MatchLineupPlayer player = (MatchLineupPlayer) vteamspieler.get(i);
 
                     if (player.getId() == ISpielerPosition.setPieces) {
                         aufstellung.setKicker(player.getSpielerId());
                     } else if (player.getId() == ISpielerPosition.captain) {
                         aufstellung.setKapitaen(player.getSpielerId());
                     } else {
                         aufstellung.setSpielerAtPosition(player.getId(), player.getSpielerId(),
                                                          player.getTaktik());
                     }
                 }
 
                 //Alles Updaten
                 HOMainFrame.instance().getAufstellungsPanel().update();
 
                 //Aufstellung zeigen
                 HOMainFrame.instance().showTab(HOMainFrame.AUFSTELLUNG);
             }
         } else if (e.getSource().equals(m_jbSimMatch)) {
         	if (m_clMatchKurzInfo != null) {
         		final Matchdetails details = DBZugriff.instance().getMatchDetails(m_clMatchKurzInfo.getMatchID());
         		final IMatchPredictionManager manager = HOMiniModel.instance().getMatchPredictionManager();
         		final int teamId = HOVerwaltung.instance().getModel().getBasics().getTeamId();
         		boolean homeMatch = false;
         		if (teamId == m_clMatchKurzInfo.getHeimID()) {
         			homeMatch = true;
         		}
         		
         		IMPTeamRatings homeTeamRatings = manager.generateTeamRatings(
         				details != null ? getRatingValue(details.getHomeMidfield()) : 1,
 						details != null ? getRatingValue(details.getHomeLeftDef()) : 1,
 						details != null ? getRatingValue(details.getHomeMidDef()) : 1,
 						details != null ? getRatingValue(details.getHomeRightDef()) : 1,
 						details != null ? getRatingValue(details.getHomeLeftAtt()) : 1,
 						details != null ? getRatingValue(details.getHomeMidAtt()) : 1,
 						details != null ? getRatingValue(details.getHomeRightAtt()) : 1);
         		
         		final IMPTeamData homeTeamValues;
         		if (homeMatch && !ratingsAreKnown(homeTeamRatings)) {
         			homeTeamValues = getOwnLineupRatings(manager);
         		} else {
         			homeTeamValues = manager.generateTeamData(m_clMatchKurzInfo.getHeimName(), homeTeamRatings,
     						details != null ? details.getHomeTacticType(): IMatchDetails.TAKTIK_NORMAL, 
     						details != null ? getRatingValue(details.getHomeTacticSkill() - 1) : 1);
         		}
         		
         		IMPTeamRatings awayTeamRatings = manager.generateTeamRatings(
 						details != null ? getRatingValue(details.getGuestMidfield()) : 1,
 						details != null ? getRatingValue(details.getGuestLeftDef()) : 1,
 						details != null ? getRatingValue(details.getGuestMidDef()) : 1,
 						details != null ? getRatingValue(details.getGuestRightDef()) : 1,
 						details != null ? getRatingValue(details.getGuestLeftAtt()) : 1,
 						details != null ? getRatingValue(details.getGuestMidAtt()) : 1,
 						details != null ? getRatingValue(details.getGuestRightAtt()) : 1);
         		
         		final IMPTeamData awayTeamValues;
         		if (!homeMatch && !ratingsAreKnown(awayTeamRatings)) {
         			awayTeamValues = getOwnLineupRatings(manager);
         		} else {
         			awayTeamValues = manager.generateTeamData(m_clMatchKurzInfo.getGastName(), awayTeamRatings,
         					details != null ? details.getGuestTacticType(): IMatchDetails.TAKTIK_NORMAL, 
         					details != null ? getRatingValue(details.getGuestTacticSkill() - 1) : 1);
         		}
         		
                 String match = m_clMatchKurzInfo.getHeimName() + " - " + m_clMatchKurzInfo.getGastName();
                 JPanel matchPredictionPanel = HOMiniModel.instance().getGUI().createMatchPredictionPanel(homeTeamValues, awayTeamValues);
 
                 JDialog d = new JDialog(HOMiniModel.instance().getGUI().getOwner4Dialog());
                 d.getContentPane().setLayout(new BorderLayout());
                 d.getContentPane().add(matchPredictionPanel, BorderLayout.CENTER);
                 d.setResizable(true);
                 d.setSize(900, 600);
                 d.setTitle(match);
                 d.setVisible(true);
         	}
         } else if (e.getSource().equals(m_jbStatistics)) {
         	//TODO Dialog with Values
         }
     }
 
     /**
      * Helper to get at least the minium rating value.
      */
     private int getRatingValue(int in) {
     	if (in > 0) {
     		return in;
     	}
     	return 1;
     }
     
     /**
      * Check, if the ratings are ok/known or if all are at the default.
      */
     private boolean ratingsAreKnown(IMPTeamRatings ratings) {
     	return (ratings != null && ratings.getMidfield() > 1d
     			&& ratings.getLeftDef() > 1d && ratings.getMiddleDef() > 1d && ratings.getRightDef() > 1d
     			&& ratings.getLeftAttack() > 1d && ratings.getMiddleAttack() > 1d && ratings.getRightAttack() > 1d);
     }
 
     /**
      * Get the team data for the own team (current linep).
      */
     private IMPTeamData getOwnLineupRatings(IMatchPredictionManager manager) {
     	ILineUp lineup = HOMiniModel.instance().getLineUP();
     	IMPTeamRatings teamRatings = manager.generateTeamRatings(
     			getRatingValue(lineup.getIntValue4Rating(lineup.getMidfieldRating())),
     			getRatingValue(lineup.getIntValue4Rating(lineup.getLeftDefenseRating())),
     			getRatingValue(lineup.getIntValue4Rating(lineup.getCentralDefenseRating())),
     			getRatingValue(lineup.getIntValue4Rating(lineup.getRightDefenseRating())),
     			getRatingValue(lineup.getIntValue4Rating(lineup.getLeftAttackRating())),
     			getRatingValue(lineup.getIntValue4Rating(lineup.getCentralAttackRating())),
 				getRatingValue(lineup.getIntValue4Rating(lineup.getRightAttackRating())));
     	
     	int tactic = lineup.getTacticType();
     	return manager.generateTeamData(HOMiniModel.instance().getBasics().getTeamName(),
     			teamRatings, tactic, getTacticStrength(lineup, tactic));
     }
 
     /**
      * Get the tactic strength of the given lineup.
      */
     private int getTacticStrength(ILineUp lineup, int tacticType) {
 		double tacticLevel = 1d;
 		switch (tacticType) {
 		case IMatchDetails.TAKTIK_KONTER:
 			tacticLevel = lineup.getTacticLevelCounter();
 			break;
 		case IMatchDetails.TAKTIK_MIDDLE:
 			tacticLevel = lineup.getTacticLevelAimAow();
 			break;
 		case IMatchDetails.TAKTIK_PRESSING:
 			tacticLevel = lineup.getTacticLevelPressing();
 			break;
 		case IMatchDetails.TAKTIK_WINGS:
 			tacticLevel = lineup.getTacticLevelAimAow();
 			break;
 		case IMatchDetails.TAKTIK_LONGSHOTS:
 			tacticLevel = lineup.getTacticLevelLongShots();
 			break;
 		}
 		tacticLevel -= 1;
 		return (int) Math.max(tacticLevel, 0);
 	}
     
     /**
 	 * React on item state changed events.
 	 */
     public void itemStateChanged(ItemEvent e) {
         if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
             // Änderung der Tabelle -> Anderer Filter!
             reInit();
         }
     }
 
     /**
      * React on key pressed events.
      */
     public void keyPressed(java.awt.event.KeyEvent keyEvent) {
         if (keyEvent.getSource().equals(m_jtSpieleTable)) {
             //manageSelectionRow (  );
             newSelectionInform();
         }
     }
 
     /**
      * React on key released events.
      */
     public void keyReleased(java.awt.event.KeyEvent keyEvent) {
         if (keyEvent.getSource().equals(m_jtSpieleTable)) {
             //manageSelectionRow (  );
             newSelectionInform();
         }
     }
 
     /**
      * React on key typed events.
      */
     public void keyTyped(java.awt.event.KeyEvent keyEvent) {
     }
 
     /**
      * React on mouse klicked events.
      */
     public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
         if (mouseEvent.getSource().equals(m_jtSpieleTable)) {
             //manageSelectionRow (  );
             newSelectionInform();
         }
     }
 
     /**
      * React on mouse entered events.
      */
     public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {
     }
 
     /**
      * React on mouse exited events.
      */
     public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
     }
 
     /**
      * React on mouse pressed events.
      */
     public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
     }
 
     /**
      * React on mouse released events.
      */
     public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
         if (mouseEvent.getSource().equals(m_jtSpieleTable)) {
             //manageSelectionRow (  );
             newSelectionInform();
         }
     }
 
     /**
      * ReInit
      */
     public void reInit() {
         if (m_jcbSpieleFilter.getSelectedIndex() > -1) {
             //Tabelle updaten
             m_jtSpieleTable.refresh(((CBItem) m_jcbSpieleFilter.getSelectedItem()).getId());
             UserParameter.instance().spieleFilter = ((CBItem) m_jcbSpieleFilter.getSelectedItem())
                                                     .getId();
 
             //Dann alle anderen Panels
             newSelectionInform();
         }
     }
 
     //----------------------Refresh--
 
     /**
      * Refresh
      */
     public void refresh() {
         //nix
     }
 
     /**
      * Zeigt das Match mit der ID an.
      */
     public void showMatch(int matchid) {
         m_jtSpieleTable.markiereMatch(matchid);
 
         //Wenn kein Match in Tabelle gefunden
         if (m_jtSpieleTable.getSelectedRow() < 0) {
             //Alle Spiele auswählen, damit die Markierung funktioniert  
             m_jcbSpieleFilter.setSelectedIndex(0);
             UserParameter.instance().spieleFilter = 0;
             m_jtSpieleTable.markiereMatch(matchid);
         }
 
         newSelectionInform();
     }
 
     /**
      * Disable all buttons.
      */
     private void clear() {
         m_jbReloadMatch.setEnabled(false);
         m_jbLoeschen.setEnabled(false);
         m_jbDrucken.setEnabled(false);
         m_jbAufstellungUebernehmen.setEnabled(false);
         m_jbSimMatch.setEnabled(false);
     }
 
     /**
      * Initialise and get the visitor lineup panel.
      */
     private Component initAufstellungGast() {
         m_jpAufstellungGastPanel = new AufstellungsSternePanel(false);
         return m_jpAufstellungGastPanel;
     }
 
     /**
      * Initialise and get the home teams lineup panel.
      */
     private Component initAufstellungHeim() {
         m_jpAufstellungHeimPanel = new AufstellungsSternePanel(true);
         return m_jpAufstellungHeimPanel;
     }
 
     //----------init-----------------------------------------------
     private void initComponents() {
         setLayout(new BorderLayout());
 
         horizontalLeftSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false,
                                                  initSpieleTabelle(), initSpieldetails());
 
         //horizontalRightSplitPane =   new JSplitPane( JSplitPane.VERTICAL_SPLIT, false, initAufstellungHeim(), initAufstellungGast() );
         aufstellungsPanel = new JPanel(new GridLayout(2, 1));
         aufstellungsPanel.add(initAufstellungHeim());
         aufstellungsPanel.add(initAufstellungGast());
         verticalSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false,
                                            horizontalLeftSplitPane,
                                            new JScrollPane(aufstellungsPanel));
 
         horizontalLeftSplitPane.setDividerLocation(gui.UserParameter.instance().spielePanel_horizontalLeftSplitPane);
 
         //horizontalRightSplitPane.setDividerLocation( gui.UserParameter.instance ().spielePanel_horizontalRightSplitPane );
         verticalSplitPane.setDividerLocation(gui.UserParameter.instance().spielePanel_verticalSplitPane);
 
         add(verticalSplitPane, BorderLayout.CENTER);
 
         m_jbLoeschen.setBackground(ThemeManager.getColor(HOColorName.BUTTON_BG));
     }
 
     /**
      * Initialise player details GUI components.
      */
     private Component initSpieldetails() {
         final JPanel mainpanel = new ImagePanel(new BorderLayout());
         m_jtpSpieldetails = new JTabbedPane();
 
         //Allgemein
         m_jpStaerkenvergleichsPanel = new StaerkenvergleichPanel();
         m_jtpSpieldetails.addTab(HOVerwaltung.instance().getLanguageString("Allgemein"),
                                  new JScrollPane(m_jpStaerkenvergleichsPanel));
 
         //Bewertung
         m_jpManschaftsBewertungsPanel = new ManschaftsBewertungsPanel();
         m_jtpSpieldetails.addTab(HOVerwaltung.instance().getLanguageString("Bewertung"),
                                  new JScrollPane(m_jpManschaftsBewertungsPanel));
 //        //Bewertung2
//        m_jpManschaftsBewertungs2Panel = new ManschaftsBewertungs2Panel();
 //        m_jtpSpieldetails.addTab(HOVerwaltung.instance().getLanguageString("Bewertung")+" 2",
 //                                 new JScrollPane(m_jpManschaftsBewertungs2Panel));
         
         //Highlights
         m_jpSpielHighlightPanel = new SpielHighlightPanel();
         m_jtpSpieldetails.addTab(HOVerwaltung.instance().getLanguageString("Highlights"),
                                  new JScrollPane(m_jpSpielHighlightPanel));
 
         //Matchbericht
         m_jpMatchberichtPanel = new MatchberichtPanel(true);
         m_jtpSpieldetails.addTab(HOVerwaltung.instance().getLanguageString("Matchbericht"),
                                  m_jpMatchberichtPanel);
 
         mainpanel.add(m_jtpSpieldetails,BorderLayout.CENTER);
 
         final JPanel buttonPanel = new ImagePanel(new FlowLayout(FlowLayout.LEFT));
 
         //Reloadbutton
         m_jbReloadMatch.setToolTipText(HOVerwaltung.instance().getLanguageString("tt_Spiel_reload"));
         m_jbReloadMatch.addActionListener(this);
         m_jbReloadMatch.setPreferredSize(new Dimension(24, 24));
         m_jbReloadMatch.setEnabled(false);
         buttonPanel.add(m_jbReloadMatch);
 
         m_jbLoeschen.setToolTipText(HOVerwaltung.instance().getLanguageString("tt_Spiel_loeschen"));
         m_jbLoeschen.addActionListener(this);
         m_jbLoeschen.setPreferredSize(new Dimension(24, 24));
         m_jbLoeschen.setEnabled(false);
         buttonPanel.add(m_jbLoeschen);
 
         m_jbDrucken.setToolTipText(HOVerwaltung.instance().getLanguageString("tt_Spiel_drucken"));
         m_jbDrucken.addActionListener(this);
         m_jbDrucken.setPreferredSize(new Dimension(24, 24));
         m_jbDrucken.setEnabled(false);
         buttonPanel.add(m_jbDrucken);
 
         m_jbAufstellungUebernehmen.setToolTipText(HOVerwaltung.instance().getLanguageString("tt_Spiel_aufstellunguebernehmen"));
         m_jbAufstellungUebernehmen.addActionListener(this);
         m_jbAufstellungUebernehmen.setPreferredSize(new Dimension(24, 24));
         m_jbAufstellungUebernehmen.setEnabled(false);
         buttonPanel.add(m_jbAufstellungUebernehmen);
         
         m_jbSimMatch.setToolTipText(HOVerwaltung.instance().getLanguageString("Simulate"));
         m_jbSimMatch.addActionListener(this);
         m_jbSimMatch.setPreferredSize(new Dimension(24, 24));
         m_jbSimMatch.setEnabled(false);
         buttonPanel.add(m_jbSimMatch);
 
         //FIXME
         m_jbStatistics.setVisible(false);
         m_jbStatistics.setToolTipText(HOVerwaltung.instance().getLanguageString("Statistik"));
         m_jbStatistics.addActionListener(this);
         m_jbStatistics.setPreferredSize(new Dimension(24, 24));
         buttonPanel.add(m_jbStatistics);
 
         mainpanel.add(buttonPanel,BorderLayout.SOUTH);
         return mainpanel;
     }
 
     /**
      * Initialise matches panel.
      */
     private Component initSpieleTabelle() {
         final ImagePanel panel = new ImagePanel(new BorderLayout());
 
         m_jcbSpieleFilter = new JComboBox(SPIELEFILTER);
         Helper.markierenComboBox(m_jcbSpieleFilter,UserParameter.instance().spieleFilter);
         m_jcbSpieleFilter.addItemListener(this);
         m_jcbSpieleFilter.setFont(m_jcbSpieleFilter.getFont().deriveFont(Font.BOLD));
         panel.add(m_jcbSpieleFilter, BorderLayout.NORTH);
 
         m_jtSpieleTable = new MatchesTable(UserParameter.instance().spieleFilter);
         m_jtSpieleTable.addMouseListener(this);
         m_jtSpieleTable.addKeyListener(this);
 
         final JScrollPane scrollpane = new JScrollPane(m_jtSpieleTable);
         scrollpane.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
 
         panel.add(scrollpane, BorderLayout.CENTER);
 
         return panel;
     }
 
     //----------------------------------------------------    
     private void newSelectionInform() {
         final int row = m_jtSpieleTable.getSelectedRow();
 
         if (row > -1) {
             //Selektiertes Spiel des Models holen und alle 3 Panel informieren 
             try {
                 final MatchKurzInfo info = ((MatchesColumnModel) m_jtSpieleTable.getSorter().getModel())
                                                                 .getMatch((int) ((ColorLabelEntry) m_jtSpieleTable.getSorter().getValueAt(row,5))
                                                                                         .getZahl());
                 refresh(info);
                 m_jpStaerkenvergleichsPanel.refresh(info);
                 m_jpManschaftsBewertungsPanel.refresh(info);
                 m_jpManschaftsBewertungs2Panel.refresh(info);
                 m_jpSpielHighlightPanel.refresh(info);
                 m_jpMatchberichtPanel.refresh(info);
 
                 if (info.getMatchStatus() == IMatchKurzInfo.FINISHED) {
                     m_jpAufstellungHeimPanel.refresh(info.getMatchID(), info.getHeimID());
                     m_jpAufstellungGastPanel.refresh(info.getMatchID(), info.getGastID());
                 } else {
                     m_jpAufstellungHeimPanel.clearAll();
                     m_jpAufstellungGastPanel.clearAll();
                 }
             } catch (Exception e) {
                 clear();
                 m_jpStaerkenvergleichsPanel.clear();
                 m_jpManschaftsBewertungsPanel.clear();
                 m_jpManschaftsBewertungs2Panel.clear();
                 m_jpSpielHighlightPanel.clear();
                 m_jpMatchberichtPanel.clear();
                 m_jpAufstellungHeimPanel.clearAll();
                 m_jpAufstellungGastPanel.clearAll();
                 HOLogger.instance().log(getClass(),"SpielePanel.newSelectionInform: No match for found for table entry! "
                                    + e);
             }
         } else {
             //Alle Panels zurücksetzen
             clear();
             m_jpStaerkenvergleichsPanel.clear();
             m_jpManschaftsBewertungsPanel.clear();
             m_jpManschaftsBewertungs2Panel.clear();
             m_jpSpielHighlightPanel.clear();
             m_jpMatchberichtPanel.clear();
             m_jpAufstellungHeimPanel.clearAll();
             m_jpAufstellungGastPanel.clearAll();
         }
     }
 
     /**
      * Refresh button states.
      */
     private void refresh(MatchKurzInfo info) {
     	m_clMatchKurzInfo = info;
     	m_jbLoeschen.setEnabled(true);
     	m_jbSimMatch.setEnabled(true);
     	if (info.getMatchStatus() == IMatchKurzInfo.FINISHED) {
     		m_jbReloadMatch.setEnabled(true);
     		final int teamid = HOVerwaltung.instance().getModel().getBasics().getTeamId();
 	    	if ((info.getHeimID() == teamid) || (info.getGastID() == teamid)) {
 	    		m_jbAufstellungUebernehmen.setEnabled(true);
 	    	} else {
 	    		m_jbAufstellungUebernehmen.setEnabled(false);
 	    	}
 	    	m_jbDrucken.setEnabled(true);
     	} else {
     		m_jbReloadMatch.setEnabled(false);
     		m_jbAufstellungUebernehmen.setEnabled(false);
     		m_jbDrucken.setEnabled(false);
     	}
     }
 }
