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
                                                              Refreshable, 
                                                              ActionListener, plugins.ISpielePanel {
 	private static final long serialVersionUID = -6337569355347545083L;
 	
     //~ Instance fields ----------------------------------------------------------------------------
 
     private AufstellungsSternePanel m_jpAufstellungGastPanel;
     private AufstellungsSternePanel m_jpAufstellungHeimPanel;
     private JButton m_jbAufstellungUebernehmen = new JButton(ThemeManager.getIcon(HOIconName.GETLINEUP));
     private JButton printButton = new JButton(ThemeManager.getIcon(HOIconName.PRINTER));
 //    private JButton deleteButton = new JButton(new ImageIcon(ImageUtilities
 //			.getImageDurchgestrichen(new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB), Color.red, new Color(200, 0, 0))));
     private JButton deleteButton = new JButton(ThemeManager.getIcon(HOIconName.REMOVE));
     private JButton m_jbReloadMatch = new JButton(ThemeManager.getIcon(HOIconName.RELOAD));
     private JButton m_jbSimMatch = new JButton(ThemeManager.getIcon(HOIconName.SIMULATEMATCH));
     
     private JComboBox m_jcbSpieleFilter;
 
 
     private JPanel linupPanel;
     private JSplitPane horizontalLeftSplitPane;
     private JSplitPane verticalSplitPane;
     private JTabbedPane matchDetailsTabbedPane;
     private ManschaftsBewertungsPanel m_jpManschaftsBewertungsPanel;
     private ManschaftsBewertungs2Panel m_jpManschaftsBewertungs2Panel;
     private MatchKurzInfo matchShortInfo;
     private MatchberichtPanel matchReportPanel;
     private SpielHighlightPanel matchHighlightPanel;
     private MatchesTable matchesTable;
     private MatchesOverviewTable matchesOverviewTable;
     private StaerkenvergleichPanel teamsComparePanel;
 
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
 
     public void saveColumnOrder(){
     	matchesTable.saveColumnOrder();
     	matchesOverviewTable.saveColumnOrder();
     }
     //----------------------Listener    
     public void actionPerformed(ActionEvent e) {
         if (e.getSource() == m_jbReloadMatch) {
             final int matchid = matchShortInfo.getMatchID();
 			HOMainFrame.instance().getOnlineWorker().getMatchlineup(matchShortInfo.getMatchID(),
 					matchShortInfo.getHeimID(), matchShortInfo.getGastID());
 			HOMainFrame.instance().getOnlineWorker().getMatchDetails(matchShortInfo.getMatchID());
 			MatchUpdater.updateMatch(HOMiniModel.instance(), // Dragettho werte setzen
 					matchShortInfo.getMatchID());
 			RefreshManager.instance().doReInit();
 			showMatch(matchid);
         } else if (e.getSource() == deleteButton ) {
             final int[] rows = matchesTable.getSelectedRows();
             final MatchKurzInfo[] infos = new MatchKurzInfo[rows.length];
 
             for (int i = 0; i < rows.length; i++) {
                 infos[i] = ((MatchesColumnModel) matchesTable.getSorter().getModel())
                            .getMatch((int) ((ColorLabelEntry) matchesTable.getSorter().getValueAt(rows[i], 5)).getZahl());
             }
 
             final StringBuilder text = new StringBuilder(100);
             text.append(HOVerwaltung.instance().getLanguageString("loeschen"));
             if (infos.length > 1) {
             	text.append(" (" + infos.length + " ");
             	text.append(HOVerwaltung.instance().getLanguageString("Spiele"));
                 text.append(")");
             } 
             text.append(" :");
             
 
             for (int i = 0; (i < infos.length) && (i < 11); i++) {
                 text.append("\n" + infos[i].getHeimName() + " - " + infos[i].getGastName());
                 if (i == 10) {
                 	text.append("\n ... ");
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
         } else if (e.getSource() == printButton) {
             if (matchShortInfo != null) {
                 final SpielePrintDialog printDialog = new SpielePrintDialog(matchShortInfo);
                 printDialog.doPrint(matchShortInfo.getHeimName() + " : "
                                     + matchShortInfo.getGastName() + " - "
                                     + matchShortInfo.getMatchDate());
                 printDialog.setVisible(false);
                 printDialog.dispose();
             }
         } else if (e.getSource() == m_jbAufstellungUebernehmen) {
             if ((matchShortInfo != null)
             		&& (matchShortInfo.getMatchStatus() == IMatchKurzInfo.FINISHED)) {
                 final int teamid = HOVerwaltung.instance().getModel().getBasics().getTeamId();
                 final Vector<IMatchLineupPlayer> vteamspieler = DBZugriff.instance().getMatchLineupPlayers(matchShortInfo.getMatchID(),
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
         } else if (e.getSource() == m_jbSimMatch) {
         	if (matchShortInfo != null) {
         		final Matchdetails details = DBZugriff.instance().getMatchDetails(matchShortInfo.getMatchID());
         		final IMatchPredictionManager manager = HOMiniModel.instance().getMatchPredictionManager();
         		final int teamId = HOVerwaltung.instance().getModel().getBasics().getTeamId();
         		boolean homeMatch = false;
         		if (teamId == matchShortInfo.getHeimID()) {
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
         			homeTeamValues = manager.generateTeamData(matchShortInfo.getHeimName(), homeTeamRatings,
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
         			awayTeamValues = manager.generateTeamData(matchShortInfo.getGastName(), awayTeamRatings,
         					details != null ? details.getGuestTacticType(): IMatchDetails.TAKTIK_NORMAL, 
         					details != null ? getRatingValue(details.getGuestTacticSkill() - 1) : 1);
         		}
         		
                 String match = matchShortInfo.getHeimName() + " - " + matchShortInfo.getGastName();
                 JPanel matchPredictionPanel = HOMiniModel.instance().getGUI().createMatchPredictionPanel(homeTeamValues, awayTeamValues);
 
                 JDialog d = new JDialog(HOMiniModel.instance().getGUI().getOwner4Dialog());
                 d.getContentPane().setLayout(new BorderLayout());
                 d.getContentPane().add(matchPredictionPanel, BorderLayout.CENTER);
                 d.setResizable(true);
                 d.setSize(900, 600);
                 d.setTitle(match);
                 d.setVisible(true);
         	}
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
      * React on key pressed events.
      */
     public void keyPressed(java.awt.event.KeyEvent keyEvent) {
         if (keyEvent.getSource().equals(matchesTable)) {
             //manageSelectionRow (  );
             newSelectionInform();
         }
     }
 
     /**
      * React on key released events.
      */
     public void keyReleased(java.awt.event.KeyEvent keyEvent) {
         if (keyEvent.getSource().equals(matchesTable)) {
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
         if (mouseEvent.getSource().equals(matchesTable)) {
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
         if (mouseEvent.getSource().equals(matchesTable)) {
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
         	int id = ((CBItem) m_jcbSpieleFilter.getSelectedItem()).getId();
             matchesTable.refresh(id);
             matchesOverviewTable.refresh(id);
             UserParameter.instance().spieleFilter = id;
 
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
         matchesTable.markiereMatch(matchid);
 
         //Wenn kein Match in Tabelle gefunden
         if (matchesTable.getSelectedRow() < 0) {
             //Alle Spiele auswählen, damit die Markierung funktioniert  
             m_jcbSpieleFilter.setSelectedIndex(0);
             UserParameter.instance().spieleFilter = 0;
             matchesTable.markiereMatch(matchid);
         }
 
         newSelectionInform();
     }
 
     /**
      * Disable all buttons.
      */
     private void clear() {
         m_jbReloadMatch.setEnabled(false);
         deleteButton.setEnabled(false);
         printButton.setEnabled(false);
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
 
         linupPanel = new JPanel(new GridLayout(2, 1));
         linupPanel.add(initAufstellungHeim());
         linupPanel.add(initAufstellungGast());
         verticalSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false,
                                            horizontalLeftSplitPane,
                                            new JScrollPane(linupPanel));
 
         horizontalLeftSplitPane.setDividerLocation(gui.UserParameter.instance().spielePanel_horizontalLeftSplitPane);
 
         verticalSplitPane.setDividerLocation(gui.UserParameter.instance().spielePanel_verticalSplitPane);
 
         add(verticalSplitPane, BorderLayout.CENTER);
 
         deleteButton.setBackground(ThemeManager.getColor(HOColorName.BUTTON_BG));
     }
 
     /**
      * Initialise player details GUI components.
      */
     private Component initSpieldetails() {
         final JPanel mainpanel = new ImagePanel(new BorderLayout());
         matchDetailsTabbedPane = new JTabbedPane();
 
         //Allgemein
         teamsComparePanel = new StaerkenvergleichPanel();
         matchDetailsTabbedPane.addTab(HOVerwaltung.instance().getLanguageString("Allgemein"),
                                  new JScrollPane(teamsComparePanel));
 
         //Bewertung
         m_jpManschaftsBewertungsPanel = new ManschaftsBewertungsPanel();
         matchDetailsTabbedPane.addTab(HOVerwaltung.instance().getLanguageString("Bewertung"),
                                  new JScrollPane(m_jpManschaftsBewertungsPanel));
 //        //Bewertung2
         m_jpManschaftsBewertungs2Panel = new ManschaftsBewertungs2Panel();
         matchDetailsTabbedPane.addTab(HOVerwaltung.instance().getLanguageString("Bewertung")+" 2",
                                  new JScrollPane(m_jpManschaftsBewertungs2Panel));
         
         //Highlights
         matchHighlightPanel = new SpielHighlightPanel();
         matchDetailsTabbedPane.addTab(HOVerwaltung.instance().getLanguageString("Highlights"),
                                  new JScrollPane(matchHighlightPanel));
 
         //Matchbericht
         matchReportPanel = new MatchberichtPanel(true);
         matchDetailsTabbedPane.addTab(HOVerwaltung.instance().getLanguageString("Matchbericht"),
                                  matchReportPanel);
 
         mainpanel.add(matchDetailsTabbedPane,BorderLayout.CENTER);
 
         final JPanel buttonPanel = new ImagePanel(new FlowLayout(FlowLayout.LEFT));
 
         //Reloadbutton
         m_jbReloadMatch.setToolTipText(HOVerwaltung.instance().getLanguageString("tt_Spiel_reload"));
         m_jbReloadMatch.addActionListener(this);
         m_jbReloadMatch.setPreferredSize(new Dimension(24, 24));
         m_jbReloadMatch.setEnabled(false);
         buttonPanel.add(m_jbReloadMatch);
 
         deleteButton.setToolTipText(HOVerwaltung.instance().getLanguageString("tt_Spiel_loeschen"));
         deleteButton.addActionListener(this);
         deleteButton.setPreferredSize(new Dimension(24, 24));
         deleteButton.setEnabled(false);
         buttonPanel.add(deleteButton);
 
         printButton.setToolTipText(HOVerwaltung.instance().getLanguageString("tt_Spiel_drucken"));
         printButton.addActionListener(this);
         printButton.setPreferredSize(new Dimension(24, 24));
         printButton.setEnabled(false);
         buttonPanel.add(printButton);
 
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
         m_jcbSpieleFilter.addItemListener(new ItemListener() {
 			
 			public void itemStateChanged(ItemEvent e) {
 				if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED){ 
 		            reInit();
 				}
 			}
 		});
         m_jcbSpieleFilter.setFont(m_jcbSpieleFilter.getFont().deriveFont(Font.BOLD));
         panel.add(m_jcbSpieleFilter, BorderLayout.NORTH);
 
         matchesTable = new MatchesTable(UserParameter.instance().spieleFilter);
         matchesTable.addMouseListener(this);
         matchesTable.addKeyListener(this);
         
         matchesOverviewTable = new MatchesOverviewTable(UserParameter.instance().spieleFilter);
         final JScrollPane scrollpane1 = new JScrollPane(matchesOverviewTable);
         scrollpane1.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
 
         final JScrollPane scrollpane = new JScrollPane(matchesTable);
         scrollpane.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
         
         JTabbedPane pane = new JTabbedPane();
         HOVerwaltung hov = HOVerwaltung.instance();
         pane.addTab(hov.getLanguageString("Spiele"), scrollpane);
         pane.addTab(hov.getLanguageString("Statistik"),scrollpane1);
         panel.add(pane, BorderLayout.CENTER);
 
         return panel;
     }
 
     //----------------------------------------------------    
     private void newSelectionInform() {
         final int row = matchesTable.getSelectedRow();
 
         if (row > -1) {
             //Selektiertes Spiel des Models holen und alle 3 Panel informieren 
             try {
                 final MatchKurzInfo info = ((MatchesColumnModel) matchesTable.getSorter().getModel())
                                                                 .getMatch((int) ((ColorLabelEntry) matchesTable.getSorter().getValueAt(row,5))
                                                                                         .getZahl());
                 refresh(info);
                 final Matchdetails details = DBZugriff.instance().getMatchDetails(info.getMatchID());
                teamsComparePanel.refresh(info,details);
                m_jpManschaftsBewertungsPanel.refresh(info,details);
                m_jpManschaftsBewertungs2Panel.refresh(info,details);
                matchHighlightPanel.refresh(info,details);
                matchReportPanel.refresh(info,details);

                 if (info.getMatchStatus() == IMatchKurzInfo.FINISHED) {
                     m_jpAufstellungHeimPanel.refresh(info.getMatchID(), info.getHeimID());
                     m_jpAufstellungGastPanel.refresh(info.getMatchID(), info.getGastID());
                 } else {
                     m_jpAufstellungHeimPanel.clearAll();
                     m_jpAufstellungGastPanel.clearAll();
                 }
             } catch (Exception e) {
                 clear();
                 teamsComparePanel.clear();
                 m_jpManschaftsBewertungsPanel.clear();
                 m_jpManschaftsBewertungs2Panel.clear();
                 matchHighlightPanel.clear();
                 matchReportPanel.clear();
                 m_jpAufstellungHeimPanel.clearAll();
                 m_jpAufstellungGastPanel.clearAll();
                 HOLogger.instance().log(getClass(),"SpielePanel.newSelectionInform: No match for found for table entry! "
                                    + e);
             }
         } else {
             //Alle Panels zurücksetzen
             clear();
             teamsComparePanel.clear();
             m_jpManschaftsBewertungsPanel.clear();
             m_jpManschaftsBewertungs2Panel.clear();
             matchHighlightPanel.clear();
             matchReportPanel.clear();
             m_jpAufstellungHeimPanel.clearAll();
             m_jpAufstellungGastPanel.clearAll();
         }
     }
 
     /**
      * Refresh button states.
      */
     private void refresh(MatchKurzInfo info) {
     	matchShortInfo = info;
     	deleteButton.setEnabled(true);
     	m_jbSimMatch.setEnabled(true);
     	if (info.getMatchStatus() == IMatchKurzInfo.FINISHED) {
     		m_jbReloadMatch.setEnabled(true);
     		final int teamid = HOVerwaltung.instance().getModel().getBasics().getTeamId();
 	    	if ((info.getHeimID() == teamid) || (info.getGastID() == teamid)) {
 	    		m_jbAufstellungUebernehmen.setEnabled(true);
 	    	} else {
 	    		m_jbAufstellungUebernehmen.setEnabled(false);
 	    	}
 	    	printButton.setEnabled(true);
     	} else {
     		m_jbReloadMatch.setEnabled(false);
     		m_jbAufstellungUebernehmen.setEnabled(false);
     		printButton.setEnabled(false);
     	}
     }
 }
