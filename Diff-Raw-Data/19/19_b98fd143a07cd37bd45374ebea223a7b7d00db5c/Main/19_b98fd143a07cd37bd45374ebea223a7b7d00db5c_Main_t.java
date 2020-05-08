 package uk.ac.gla.dcs.tp3.w;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import javax.swing.SwingUtilities;
 
 import uk.ac.gla.dcs.tp3.w.algorithm.Algorithm;
 import uk.ac.gla.dcs.tp3.w.league.Division;
 import uk.ac.gla.dcs.tp3.w.league.Match;
 import uk.ac.gla.dcs.tp3.w.league.Team;
 import uk.ac.gla.dcs.tp3.w.parser.Parser;
 import uk.ac.gla.dcs.tp3.w.ui.MainFrame;
 import uk.ac.gla.dcs.tp3.w.league.DateTime;
 
 /**
  * This is the main method for the entire project. The system can be either run
  * as a desktop application with a Swing interface or, if the command line
  * argument '--web' is specified, it can be run as a text output only program
  * for displaying on a website or some other application.
  * 
  * @author Team W
  * @version 1.0
  */
 public class Main {
 
 	private static void updateMatchesPlayed(DateTime displayDate,
 			HashMap<String, Division> map) {
 		for (Division d : map.values()) {
 			for (Match m : d.getFixtures()) {
 				if (m.getDateTime().before(displayDate)) {
 					m.playMatch();
 				} else {
 					m.unplayMatch();
 				}
 			}
 			for (Team t : d.getTeams()) {
 				t.setEliminated(false);
 				ArrayList<Team> teams = t.getEliminatedBy();
 				t.getEliminatedBy().removeAll(teams);
 			}
 			(new Algorithm(d)).updateDivisionElim();
 		}
 	}
 
 	public static void main(String[] args) {
         System.out.println(System.getProperty("os.name"));
 		// Set up variables for whole application.
 		Parser p = new Parser();
 		// Text only output request check
 		boolean web = false;
 		// Check supplied files were successfully parsed.
 		boolean parsed = false;
 		for (String s : args) {
 			if (s.equals("--web")) {
 				web = true;
 				break;
 			} else {
 				try {
 					parsed = p.parse(s);
 				} catch (Exception e) {
 					parsed = false;
 				}
 			}
 		}
 		// If none of the supplied files were successfully parsed, default to
 		// known file.
 		if (!parsed) {
 			p.parse("");
 		}
 		// Now there will be some valid divisions to obtain. Now work out
 		// elimination status of each team.
 		final HashMap<String, Division> map = p.getDivisions();
 		for (Division d : map.values()) {
 			(new Algorithm(d)).updateDivisionElim();
 		}
 		
 		//trivial elim test
		
 		for (Division d2 : map.values()) {
 			(new Algorithm(d2)).FirstNonTrivTeamElim();	
 			System.out.println(d2.getFirstNTTeamElim() + "   "+d2.getFirstNTTeamElimdate());
 			}	
		
 
 		
 		
 		// Swing interface requested.
 		if (!web) {
 			SwingUtilities.invokeLater(new Runnable() {
 				public void run() {
 					new MainFrame(map);
 				}
 			});
 		} else {
 			if (args.length != 2) {
 				for (Division d : map.values()) {
 					d.printWeb();
 				}
 				System.exit(0);
 			}
 			DateTime date = new DateTime();
 			String[] dmy = args[1].split("-");
 			date.setDate(Integer.parseInt(dmy[0]));
 			date.setMonth(Integer.parseInt(dmy[1]));
 			date.setYear(Integer.parseInt(dmy[2]));
 			// Text only output requested.
 			updateMatchesPlayed(date, map);
 			for (Division d : map.values()) {
 				d.printWeb();
 			}
 		}
 	}
 }
