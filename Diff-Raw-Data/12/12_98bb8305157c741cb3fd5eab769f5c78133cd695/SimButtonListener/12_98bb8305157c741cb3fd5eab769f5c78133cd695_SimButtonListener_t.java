 // %3491987323:hoplugins.teamAnalyzer.ui.controller%
 package hoplugins.teamAnalyzer.ui.controller;
 
 import hoplugins.Commons;
 import hoplugins.commons.utils.PluginProperty;
 import hoplugins.teamAnalyzer.SystemManager;
 import hoplugins.teamAnalyzer.ui.RecapPanel;
 import hoplugins.teamAnalyzer.ui.TeamLineupData;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import plugins.IMPTeamData;
 import plugins.IMPTeamRatings;
 import plugins.IMatchDetails;
 import plugins.IMatchPredictionManager;
 
 
 /**
  * Action listener for the simulation Button
  *
  * @author <a href=mailto:draghetto@users.sourceforge.net>Massimiliano Amato</a>
  */
 public class SimButtonListener implements ActionListener {
     //~ Instance fields ----------------------------------------------------------------------------
 
     /** The match prediction Manager */
     IMatchPredictionManager manager = Commons.getModel().getMatchPredictionManager();
 
     /** The user team */
     TeamLineupData myTeam;
 
     /** The opponent team */
     TeamLineupData opponentTeam;
     private RecapPanel recapPanel = null;
 
     //~ Constructors -------------------------------------------------------------------------------
 
     /**
      * Creates a new SimButtonListener object.
      *
      * @param panel user team
      * @param panel2 opponent team
      */
     public SimButtonListener(TeamLineupData myTeam, TeamLineupData opponentTeam, RecapPanel recapPanel) {
         this.myTeam = myTeam;
         this.opponentTeam = opponentTeam;
         this.recapPanel = recapPanel;
     }
 
     //~ Methods ------------------------------------------------------------------------------------
 
     /**
      * Action performed event listener PRepares the date for the 2 teams and lauch the
      * MatchRatingPredictor
      *
      * @param e the event
      */
     public void actionPerformed(ActionEvent e) {
         if ((myTeam.getMidfield() < 1) || (opponentTeam.getMidfield() < 1)) {
             JPanel pan = new JPanel();
 
             pan.add(new JLabel(PluginProperty.getString("Error.SetFormation")));
             JOptionPane.showMessageDialog(SystemManager.getPlugin().getPluginPanel(), pan,
                                           PluginProperty.getString("Error"),
                                           JOptionPane.PLAIN_MESSAGE);
 
             return;
         }
 
         IMPTeamRatings myTeamRatings = manager.generateTeamRatings(myTeam.getMidfield(),
                                                                    myTeam.getLeftDefence(),
                                                                    myTeam.getMiddleDefence(),
                                                                    myTeam.getRightDefence(),
                                                                    myTeam.getLeftAttack(),
                                                                    myTeam.getMiddleAttack(),
                                                                    myTeam.getRightAttack());
 
         IMPTeamData myTeamValues = manager.generateTeamData(myTeam.getTeamPanel().getText(),
         		myTeamRatings, Commons.getModel().getLineUP().getTacticType(), getTacticLevel());
 
         IMPTeamRatings opponentTeamRatings = manager.generateTeamRatings(opponentTeam.getMidfield(),
                                                                          opponentTeam.getLeftDefence(),
                                                                          opponentTeam.getMiddleDefence(),
                                                                          opponentTeam.getRightDefence(),
                                                                          opponentTeam.getLeftAttack(),
                                                                          opponentTeam.getMiddleAttack(),
                                                                          opponentTeam.getRightAttack());
 
         IMPTeamData opponentTeamValues = manager.generateTeamData(opponentTeam.getTeamPanel().getText(),
         		opponentTeamRatings, getTacticType(recapPanel.getSelectedTacticType()),
         		getTacticSkill(recapPanel.getSelectedTacticSkill()));
 
         JPanel matchPredictionPanel;
         String match = "";
 
         if (Commons.getModel().getLineUP().getHeimspiel() == 1) {
             matchPredictionPanel =
             	Commons.getModel().getGUI().createMatchPredictionPanel(myTeamValues, opponentTeamValues);
             match = myTeamValues.getTeamName() + " - " + opponentTeamValues.getTeamName();
         } else {
             matchPredictionPanel =
             	Commons.getModel().getGUI().createMatchPredictionPanel(opponentTeamValues, myTeamValues);
             match = opponentTeamValues.getTeamName() + " - " + myTeamValues.getTeamName();
         }
 
         JDialog d = new JDialog(Commons.getModel().getGUI().getOwner4Dialog());
 
         d.getContentPane().setLayout(new BorderLayout());
         d.getContentPane().add(matchPredictionPanel, BorderLayout.CENTER);
         d.setResizable(true);
         d.setSize(900, 600);
         d.setTitle(match);
         d.setVisible(true);
     }
 
     /**
      * Returns the tactic level for the user team defined formation in HO Lineup
      *
      * @return the actual tactic level as shown in HO LIneup tab
      */
     private int getTacticLevel() {
         double tacticLevel = 0d;
 
         switch (Commons.getModel().getLineUP().getTacticType()) {
             case IMatchDetails.TAKTIK_KONTER:
                 tacticLevel = Commons.getModel().getLineUP().getTacticLevelCounter();
                 break;
 
             case IMatchDetails.TAKTIK_MIDDLE:
                 tacticLevel = Commons.getModel().getLineUP().getTacticLevelAimAow();
                 break;
 
             case IMatchDetails.TAKTIK_PRESSING:
                 tacticLevel = Commons.getModel().getLineUP().getTacticLevelPressing();
                 break;
 
             case IMatchDetails.TAKTIK_WINGS:
                 tacticLevel = Commons.getModel().getLineUP().getTacticLevelAimAow();
                 break;

            case IMatchDetails.TAKTIK_LONGSHOTS:
                tacticLevel = Commons.getModel().getLineUP().getTacticLevelLongShots();
                break;
         }
 
         return (int)tacticLevel;
     }
 
     /**
      * Get the tactic constant for a i18n'ed string.
      */
     private int getTacticType(String strTactic) {
     	try {
 	    	if (strTactic == null || RecapPanel.VALUE_NA.equals(strTactic)) {
 	    		return IMatchDetails.TAKTIK_NORMAL;
 	    	} else if (strTactic.equals(Commons.getModel().getHelper().getNameForTaktik(IMatchDetails.TAKTIK_KONTER))) {
 	    		return IMatchDetails.TAKTIK_KONTER;
 	    	} else if (strTactic.equals(Commons.getModel().getHelper().getNameForTaktik(IMatchDetails.TAKTIK_MIDDLE))) {
 	    		return IMatchDetails.TAKTIK_MIDDLE;
 	    	} else if (strTactic.equals(Commons.getModel().getHelper().getNameForTaktik(IMatchDetails.TAKTIK_WINGS))) {
 	    		return IMatchDetails.TAKTIK_WINGS;
 	    	} else if (strTactic.equals(Commons.getModel().getHelper().getNameForTaktik(IMatchDetails.TAKTIK_PRESSING))) {
 	    		return IMatchDetails.TAKTIK_PRESSING;
 	    	} else if (strTactic.equals(Commons.getModel().getHelper().getNameForTaktik(IMatchDetails.TAKTIK_CREATIVE))) {
 	    		return IMatchDetails.TAKTIK_CREATIVE;
 	    	} else if (strTactic.equals(Commons.getModel().getHelper().getNameForTaktik(IMatchDetails.TAKTIK_LONGSHOTS))) {
 	    		return IMatchDetails.TAKTIK_LONGSHOTS;
 	    	}
     	} catch (Exception e) {
     		e.printStackTrace();
     	}
     	return IMatchDetails.TAKTIK_NORMAL;
     }
 
     /**
      * Get the tactic skill for a i18n'ed string.
      */
     private int getTacticSkill(String strSkill) {
     	try {
     		if (strSkill == null || RecapPanel.VALUE_NA.equals(strSkill)) {
     			return 0;
     		}
     		for (int m=0; m<21; m++) {
     			if (strSkill.equals(Commons.getModel().getHelper().getNameForSkill(m, false))) {
     				return (m>0?m-1:m); // TeamRatingPanel doesnt handle 'non existant', index 0=disastrous
     			}
     		}
     	} catch (Exception e) {
     		e.printStackTrace();
     	}
     	return 0;
     }
 }
