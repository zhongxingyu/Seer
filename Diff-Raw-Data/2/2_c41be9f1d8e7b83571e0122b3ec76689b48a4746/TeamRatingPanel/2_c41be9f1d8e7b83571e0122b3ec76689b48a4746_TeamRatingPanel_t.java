 // %1301666028:de.hattrickorganizer.gui.matchprediction%
 package de.hattrickorganizer.gui.matchprediction;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import plugins.IMatchDetails;
 import de.hattrickorganizer.logik.matchengine.TeamData;
 import de.hattrickorganizer.logik.matchengine.TeamRatings;
 import de.hattrickorganizer.tools.PlayerHelper;
 
 
 /**
  * TODO Missing Class Documentation
  *
  * @author TODO Author Name
  */
 public class TeamRatingPanel extends JPanel implements ItemListener {
     //~ Instance fields ----------------------------------------------------------------------------
 
     private GridBagConstraints m_clConstraints;
     private GridBagLayout m_clLayout;
     private List levels;
     private List subLevels;
     private List tactics;
     private String teamName;
     private JComboBox[][] values = new JComboBox[8][2];
     private int row;
 
     //~ Constructors -------------------------------------------------------------------------------
 
     /**
      * Creates a new TeamRatingPanel object.
      *
      * @param team TODO Missing Constructuor Parameter Documentation
      */
     public TeamRatingPanel(plugins.IMPTeamData team) {
         super();
         teamName = team.getTeamName();
         initLevel();
         initSubLevel();
         initTactics();
         m_clLayout = new GridBagLayout();
         m_clConstraints = new GridBagConstraints();
         m_clConstraints.fill = GridBagConstraints.HORIZONTAL;
         m_clConstraints.weightx = 0.0;
         m_clConstraints.weighty = 0.0;
         m_clConstraints.insets = new Insets(1, 1, 1, 1);
         setLayout(m_clLayout);
 
         final TeamRatings tr = (TeamRatings) team.getRatings();
         final de.hattrickorganizer.model.HOVerwaltung verwaltung = de.hattrickorganizer.model.HOVerwaltung
                                                                    .instance();
         addLine(tr.getMidfield(), verwaltung.getResource().getProperty("MatchMittelfeld"));
         addLine(tr.getRightDef(), verwaltung.getResource().getProperty("rechteAbwehrseite"));
         addLine(tr.getMiddleDef(), verwaltung.getResource().getProperty("Abwehrzentrum"));
         addLine(tr.getLeftDef(), verwaltung.getResource().getProperty("linkeAbwehrseite"));
         addLine(tr.getRightAttack(), verwaltung.getResource().getProperty("rechteAngriffsseite"));
         addLine(tr.getMiddleAttack(), verwaltung.getResource().getProperty("Angriffszentrum"));
         addLine(tr.getLeftAttack(), verwaltung.getResource().getProperty("linkeAngriffsseite"));
 
         m_clConstraints.gridx = 0;
         m_clConstraints.gridy = row;
         m_clConstraints.gridwidth = 3;
 
         final JPanel taktikpanel = new JPanel();
         taktikpanel.setOpaque(false);
 
         values[row][0] = new JComboBox(tactics.toArray());
 
         int tactIndex = team.getTacticType();
 
         // Creative is 7 and not 5.. why????
         if (tactIndex == IMatchDetails.TAKTIK_CREATIVE) {
             tactIndex = 5;
         }
 
         values[row][0].setSelectedIndex(tactIndex);
         values[row][0].addItemListener(this);
         taktikpanel.add(values[row][0]);
 
         values[row][1] = new JComboBox(levels.toArray());
        values[row][1].setSelectedIndex(Math.min(team.getTacticLevel(), 19)); // limit tactic strength to divine
 
         if (team.getTacticType() == IMatchDetails.TAKTIK_NORMAL) {
             values[row][1].setEnabled(false);
         }
 
         taktikpanel.add(values[row][1]);
 
         add(taktikpanel, m_clConstraints);
 
         setOpaque(false);
     }
 
     //~ Methods ------------------------------------------------------------------------------------
 
     /**
      * TODO Missing Method Documentation
      *
      * @param teamdata TODO Missing Method Parameter Documentation
      */
     public final void setTeamData(plugins.IMPTeamData teamdata) {
         teamName = teamdata.getTeamName();
 
         final TeamRatings ratings = (TeamRatings) teamdata.getRatings();
         int lvl = 0;
         int subLvl = 0;
 
         lvl = ((int) ratings.getMidfield() - 1) / 4;
         subLvl = ((int) ratings.getMidfield() - 1) - (lvl * 4);
         values[0][0].setSelectedIndex(lvl);
         values[0][1].setSelectedIndex(subLvl);
 
         lvl = ((int) ratings.getRightDef() - 1) / 4;
         subLvl = ((int) ratings.getRightDef() - 1) - (lvl * 4);
         values[1][0].setSelectedIndex(lvl);
         values[1][1].setSelectedIndex(subLvl);
 
         lvl = ((int) ratings.getMiddleDef() - 1) / 4;
         subLvl = ((int) ratings.getMiddleDef() - 1) - (lvl * 4);
         values[2][0].setSelectedIndex(lvl);
         values[2][1].setSelectedIndex(subLvl);
 
         lvl = ((int) ratings.getLeftDef() - 1) / 4;
         subLvl = ((int) ratings.getLeftDef() - 1) - (lvl * 4);
         values[3][0].setSelectedIndex(lvl);
         values[3][1].setSelectedIndex(subLvl);
 
         lvl = ((int) ratings.getRightAttack() - 1) / 4;
         subLvl = ((int) ratings.getRightAttack() - 1) - (lvl * 4);
         values[4][0].setSelectedIndex(lvl);
         values[4][1].setSelectedIndex(subLvl);
 
         lvl = ((int) ratings.getMiddleAttack() - 1) / 4;
         subLvl = ((int) ratings.getMiddleAttack() - 1) - (lvl * 4);
         values[5][0].setSelectedIndex(lvl);
         values[5][1].setSelectedIndex(subLvl);
 
         lvl = ((int) ratings.getLeftAttack() - 1) / 4;
         subLvl = ((int) ratings.getLeftAttack() - 1) - (lvl * 4);
         values[6][0].setSelectedIndex(lvl);
         values[6][1].setSelectedIndex(subLvl);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final TeamData getTeamData() {
         final TeamRatings rat = new TeamRatings();
         rat.setMidfield(getValue(0));
         rat.setLeftDef(getValue(1));
         rat.setMiddleDef(getValue(2));
         rat.setRightDef(getValue(3));
         rat.setLeftAttack(getValue(4));
         rat.setMiddleAttack(getValue(5));
         rat.setRightAttack(getValue(6));
 
         final TeamData teamData = new TeamData(teamName, rat, values[7][0].getSelectedIndex(),
                                                values[7][1].getSelectedIndex());
         return teamData;
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param e TODO Missing Method Parameter Documentation
      */
     public final void itemStateChanged(ItemEvent e) {
         //Taktik
         if (values[7][0].getSelectedItem() instanceof RatingItem
             && (((RatingItem) values[7][0].getSelectedItem()).getValue() == IMatchDetails.TAKTIK_NORMAL)) {
             values[7][1].setEnabled(false);
         } else {
             values[7][1].setEnabled(true);
         }
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param row TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     private double getValue(int row) {
         final int lvl = values[row][0].getSelectedIndex();
         final int subLvl = values[row][1].getSelectedIndex();
         return (lvl * 4) + subLvl + 1;
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param d TODO Missing Method Parameter Documentation
      * @param zone TODO Missing Method Parameter Documentation
      */
     private void addLine(double d, String zone) {
         m_clConstraints.gridx = 0;
         m_clConstraints.gridy = row;
         add(new JLabel(zone), m_clConstraints);
 
         final int lvl = ((int) d - 1) / 4;
         values[row][0] = new JComboBox(levels.toArray());
         values[row][0].setSelectedIndex(lvl);
         m_clConstraints.gridx = 1;
         add(values[row][0], m_clConstraints);
 
         final int subLvl = ((int) d - 1) - (lvl * 4);
         values[row][1] = new JComboBox(subLevels.toArray());
         values[row][1].setSelectedIndex(subLvl);
         m_clConstraints.gridx = 2;
         add(values[row][1], m_clConstraints);
         row++;
     }
 
     /**
      * TODO Missing Method Documentation
      */
     private void initLevel() {
         subLevels = new ArrayList();
 
         final de.hattrickorganizer.model.HOVerwaltung verwaltung = de.hattrickorganizer.model.HOVerwaltung
                                                                    .instance();
         subLevels.add(new RatingItem(verwaltung.getResource().getProperty("verylow"), 0));
         subLevels.add(new RatingItem(verwaltung.getResource().getProperty("low"), 1));
         subLevels.add(new RatingItem(verwaltung.getResource().getProperty("high"), 2));
         subLevels.add(new RatingItem(verwaltung.getResource().getProperty("veryhigh"), 3));
     }
 
     /**
      * TODO Missing Method Documentation
      */
     private void initSubLevel() {
         levels = new ArrayList();
 
         for (int i = 1; i < 21; i++) {
             levels.add(new RatingItem(PlayerHelper.getNameForSkill(i, false), i));
         }
     }
 
     /**
      * TODO Missing Method Documentation
      */
     private void initTactics() {
         tactics = new ArrayList();
         tactics.add(new RatingItem(de.hattrickorganizer.model.matches.Matchdetails.getNameForTaktik(IMatchDetails.TAKTIK_NORMAL),
                                    IMatchDetails.TAKTIK_NORMAL));
         tactics.add(new RatingItem(de.hattrickorganizer.model.matches.Matchdetails.getNameForTaktik(IMatchDetails.TAKTIK_PRESSING),
                                    IMatchDetails.TAKTIK_PRESSING));
         tactics.add(new RatingItem(de.hattrickorganizer.model.matches.Matchdetails.getNameForTaktik(IMatchDetails.TAKTIK_KONTER),
                                    IMatchDetails.TAKTIK_KONTER));
         tactics.add(new RatingItem(de.hattrickorganizer.model.matches.Matchdetails.getNameForTaktik(IMatchDetails.TAKTIK_MIDDLE),
                                    IMatchDetails.TAKTIK_MIDDLE));
         tactics.add(new RatingItem(de.hattrickorganizer.model.matches.Matchdetails.getNameForTaktik(IMatchDetails.TAKTIK_WINGS),
                                    IMatchDetails.TAKTIK_WINGS));
         tactics.add(new RatingItem(de.hattrickorganizer.model.matches.Matchdetails.getNameForTaktik(IMatchDetails.TAKTIK_CREATIVE),
                                    IMatchDetails.TAKTIK_CREATIVE));
     }
 }
