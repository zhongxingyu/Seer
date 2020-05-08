 package uk.ac.gla.dcs.tp3.w.parser;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 //import java.util.Date;
 import java.util.Scanner;
 import uk.ac.gla.dcs.tp3.w.league.*;
 
 /**
  * @author James & Kris
  * 
  */
 public class Parser {
 	// National League Divisions
 	private Division nationalEast = new Division();
 	private Division nationalCentral = new Division();
 	private Division nationalWest = new Division();
 	// American League Divisions
 	private Division americanEast = new Division();
 	private Division americanCentral = new Division();
 	private Division americanWest = new Division();
 	private boolean verbose = false;
 
 	public Parser(File f) {
 		// ArrayList<Division> leagueEachDay = new ArrayList<Division>();
 		// //array of
 		// days in season each with seperate state
 		boolean postponedCheck = false;
 		String thisLine = "", date = "";
		DateTime d = new DateTime();
 		Scanner fileScanner, lineScanner;
 		int score1 = 0, score2 = 0;
 		try {
 			fileScanner = new Scanner(f);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			return;
 		}
 		thisLine = fileScanner.nextLine();
 
 		int lCount = 0;
 
 		while (fileScanner.hasNextLine() || thisLine.isEmpty()) {
 
 			postponedCheck = false;
 
 			/** Setting the date */
 			if (thisLine.isEmpty()) {
 				date = fileScanner.nextLine();
 				lineScanner = new Scanner(date);
 				d.setDate(lineScanner.nextInt()); // setting day
 				String m = lineScanner.next();
 				d.setMonth(Month.getMonthNumber(m.toUpperCase())); // month
 																	// converted
 																	// to a
 																	// number
 				// and stored
 				d.setYear(lineScanner.nextInt()); // year
 			}
 			/** Checking to see if we're on the same day */
 			thisLine = fileScanner.nextLine(); // get each line one at a
 			// time(within a day)
 			if (thisLine.isEmpty()) {
 				continue;
 			}
 			lineScanner = new Scanner(thisLine); // start new scanner to
 			// scan particular line
 			/** parsing out information from a particular line */
 			String team1Name = "", team2Name = "", scoreOne = "", scoreTwo = "";
 			lineScanner.next(); // skip time
 			boolean hyphenCheck = false, atScore2 = false;
 			// loop through characters in the line starting at first team
 			// name
 			for (int i = 6; i < thisLine.length(); i++) {
 				char c = thisLine.charAt(i);
 				// time here
 				// check & add char to team 1's name
 				if (c != '-' && hyphenCheck == false) {
 					team1Name += c;
 				}
 				// check and add char to team 2's name if past hyphen in
 				// source
 				if (hyphenCheck
 						&& (c != '0' && c != '1' && c != '2' && c != '3'
 								&& c != '4' && c != '5' && c != '6' && c != '7'
 								&& c != '8' && c != '9') && c != '\t'
 						&& c != ':' && c != '$') {
 					team2Name += c;
 				}
 				// lets it know we're past the hyphen
 				if (c == '-') {
 					hyphenCheck = true;
 					i++;
 				}
 				// moved onto score of both teams
 				if (c == '0' || c == '1' || c == '2' || c == '3' || c == '4'
 						|| c == '5' || c == '6' || c == '7' || c == '8'
 						|| c == '9' || c == ':' || c == '$') {
 					/** if it hits a colon, working on team 2's score */
 					if (c == ':') {
 						atScore2 = true;
 						continue;
 					} else if (c == '$') {
 						postponedCheck = true;
 					}
 					/** setting score's for teams 1 and 2 */
 					if (atScore2) {
 						scoreTwo += c;
 					} else {
 						scoreOne += c;
 					}
 				}
 			} // - loop over line
 				// creating 2 temporary team objects to store parsed info
 			Team t1 = new Team(team1Name.trim());
 			Team t2 = new Team(team2Name.trim());
 			/** add first team to correct division if not already there */
 
 			// Set American Division : East
 			if ((t1.getName().equalsIgnoreCase("Baltimore Orioles")
 					|| t1.getName().equalsIgnoreCase("Boston Red Sox")
 					|| t1.getName().equalsIgnoreCase("New York Yankees")
 					|| t1.getName().equalsIgnoreCase("Tampa Bay Rays") || t1
 					.getName().equalsIgnoreCase("Toronto Blue Jays"))
 					&& !americanEast.isMember(t1)) {
 				americanEast.addTeam(t1);
 			}
 
 			// Set American Division : Central
 			if ((t1.getName().equalsIgnoreCase("Chicago White Sox")
 					|| t1.getName().equalsIgnoreCase("Cleveland Indians")
 					|| t1.getName().equalsIgnoreCase("Detroit Tigers")
 					|| t1.getName().equalsIgnoreCase("Kansas City Royals") || t1
 					.getName().equalsIgnoreCase("Minnesota Twins"))
 					&& !americanCentral.isMember(t1)) {
 				americanCentral.addTeam(t1);
 			}
 
 			// Set American Division : West
 			if ((t1.getName().equalsIgnoreCase("Seattle Mariners")
 					|| t1.getName().equalsIgnoreCase("Texas Rangers")
 					|| t1.getName().equalsIgnoreCase("Houston Astros")
 					|| t1.getName().equalsIgnoreCase("Los Angeles Angels") || t1
 					.getName().equalsIgnoreCase("Oakland Athletics"))
 					&& !americanWest.isMember(t1)) {
 				americanWest.addTeam(t1);
 			}
 
 			// Set National Division : East
 			if ((t1.getName().equalsIgnoreCase("Atlanta Braves")
 					|| t1.getName().equalsIgnoreCase("Miami Marlins")
 					|| t1.getName().equalsIgnoreCase("New York Mets")
 					|| t1.getName().equalsIgnoreCase("Philadelphia Phillies") || t1
 					.getName().equalsIgnoreCase("Washington Nationals"))
 					&& !nationalEast.isMember(t1)) {
 				nationalEast.addTeam(t1);
 			}
 
 			// Set National Division : Central
 			if ((t1.getName().equalsIgnoreCase("Chicago Cubs")
 					|| t1.getName().equalsIgnoreCase("Cincinnati Reds")
 					|| t1.getName().equalsIgnoreCase("Milwaukee Brewers")
 					|| t1.getName().equalsIgnoreCase("Pittsburgh Pirates") || t1
 					.getName().equalsIgnoreCase("St.Louis Cardinals"))
 					&& !nationalCentral.isMember(t1)) {
 				nationalCentral.addTeam(t1);
 			}
 
 			// Set National Division : West
 			if ((t1.getName().equalsIgnoreCase("Arizona Diamondbacks")
 					|| t1.getName().equalsIgnoreCase("Colorado Rockies")
 					|| t1.getName().equalsIgnoreCase("San Francisco Giants")
 					|| t1.getName().equalsIgnoreCase("Los Angeles Dodgers") || t1
 					.getName().equalsIgnoreCase("San Diego Padres"))
 					&& !nationalWest.isMember(t1)) {
 				nationalWest.addTeam(t1);
 			}
 
 			/** add second team to correct division if not already there */
 			// Set American Division : East
 			if ((t2.getName().equalsIgnoreCase("Baltimore Orioles")
 					|| t2.getName().equalsIgnoreCase("Boston Red Sox")
 					|| t2.getName().equalsIgnoreCase("New York Yankees")
 					|| t2.getName().equalsIgnoreCase("Tampa Bay Rays") || t2
 					.getName().equalsIgnoreCase("Toronto Blue Jays"))
 					&& !americanEast.isMember(t2)) {
 				americanEast.addTeam(t2);
 			}
 
 			// Set American Division : Central
 			if ((t2.getName().equalsIgnoreCase("Chicago White Sox")
 					|| t2.getName().equalsIgnoreCase("Cleveland Indians")
 					|| t2.getName().equalsIgnoreCase("Detroit Tigers")
 					|| t2.getName().equalsIgnoreCase("Kansas City Royals") || t2
 					.getName().equalsIgnoreCase("Minnesota Twins"))
 					&& !americanCentral.isMember(t2)) {
 				americanCentral.addTeam(t2);
 			}
 
 			// Set American Division : West
 			if ((t2.getName().equalsIgnoreCase("Seattle Mariners")
 					|| t2.getName().equalsIgnoreCase("Texas Rangers")
 					|| t2.getName().equalsIgnoreCase("Houston Astros")
 					|| t2.getName().equalsIgnoreCase("Los Angeles Angels") || t2
 					.getName().equalsIgnoreCase("Oakland Athletics"))
 					&& !americanWest.isMember(t2)) {
 				americanWest.addTeam(t2);
 			}
 
 			// Set National Division : East
 			if ((t2.getName().equalsIgnoreCase("Atlanta Braves")
 					|| t2.getName().equalsIgnoreCase("Miami Marlins")
 					|| t2.getName().equalsIgnoreCase("New York Mets")
 					|| t2.getName().equalsIgnoreCase("Philadelphia Phillies") || t2
 					.getName().equalsIgnoreCase("Washington Nationals"))
 					&& !nationalEast.isMember(t2)) {
 				nationalEast.addTeam(t2);
 			}
 
 			// Set National Division : Central
 			if ((t2.getName().equalsIgnoreCase("Chicago Cubs")
 					|| t2.getName().equalsIgnoreCase("Cincinnati Reds")
 					|| t2.getName().equalsIgnoreCase("Milwaukee Brewers")
 					|| t2.getName().equalsIgnoreCase("Pittsburgh Pirates") || t2
 					.getName().equalsIgnoreCase("St.Louis Cardinals"))
 					&& !nationalCentral.isMember(t2)) {
 				nationalCentral.addTeam(t2);
 			}
 
 			// Set National Division : West
 			if ((t2.getName().equalsIgnoreCase("Arizona Diamondbacks")
 					|| t2.getName().equalsIgnoreCase("Colorado Rockies")
 					|| t2.getName().equalsIgnoreCase("San Francisco Giants")
 					|| t2.getName().equalsIgnoreCase("Los Angeles Dodgers") || t2
 					.getName().equalsIgnoreCase("San Diego Padres"))
 					&& !nationalWest.isMember(t2)) {
 				nationalWest.addTeam(t2);
 			}
 
 			/** Creating the match object with them + scores */
 			Match match = new Match(t1, t2, score1, score2, d, false);
 
 			/** convert score to integers if the match was not postponed */
 			if (!postponedCheck) {
 				score1 = Integer.parseInt(scoreOne);
 				score2 = Integer.parseInt(scoreTwo);
 
 				if (score1 > score2) {
 
 					// if team 1 wins
 
 					/**
 					 * increment games played for team 1 if they won the game,
 					 * increase points and add fixture
 					 */
 
 					if (nationalCentral.isMember(t1)) {
 						for (int x = 0; x < nationalCentral.getTeams().size(); x++)
 							if (nationalCentral.getTeams().get(x).getName()
 									.equalsIgnoreCase(t1.getName())) {
 								nationalCentral
 										.getTeams()
 										.get(x)
 										.setGamesPlayed(
 												nationalCentral.getTeams()
 														.get(x)
 														.getGamesPlayed() + 1);
 								nationalCentral
 										.getTeams()
 										.get(x)
 										.setPoints(
 												nationalCentral.getTeams()
 														.get(x).getPoints() + 1);
 								nationalCentral.getTeams().get(x)
 										.addUpcomingMatch(match);
 								nationalCentral.addFixture(match);
 							}
 					}
 					if (nationalEast.isMember(t1)) {
 						for (int x = 0; x < nationalEast.getTeams().size(); x++)
 							if (nationalEast.getTeams().get(x).getName()
 									.equalsIgnoreCase(t1.getName())) {
 								nationalEast
 										.getTeams()
 										.get(x)
 										.setGamesPlayed(
 												nationalEast.getTeams().get(x)
 														.getGamesPlayed() + 1);
 								nationalEast
 										.getTeams()
 										.get(x)
 										.setPoints(
 												nationalEast.getTeams().get(x)
 														.getPoints() + 1);
 								nationalEast.getTeams().get(x)
 										.addUpcomingMatch(match);
 								nationalEast.addFixture(match);
 							}
 					}
 					if (nationalWest.isMember(t1)) {
 						for (int x = 0; x < nationalWest.getTeams().size(); x++)
 							if (nationalWest.getTeams().get(x).getName()
 									.equalsIgnoreCase(t1.getName())) {
 								nationalWest
 										.getTeams()
 										.get(x)
 										.setGamesPlayed(
 												nationalWest.getTeams().get(x)
 														.getGamesPlayed() + 1);
 								nationalWest
 										.getTeams()
 										.get(x)
 										.setPoints(
 												nationalWest.getTeams().get(x)
 														.getPoints() + 1);
 								nationalWest.getTeams().get(x)
 										.addUpcomingMatch(match);
 								nationalWest.addFixture(match);
 							}
 					}
 					if (americanCentral.isMember(t1)) {
 						for (int x = 0; x < americanCentral.getTeams().size(); x++)
 							if (americanCentral.getTeams().get(x).getName()
 									.equalsIgnoreCase(t1.getName())) {
 								americanCentral
 										.getTeams()
 										.get(x)
 										.setGamesPlayed(
 												americanCentral.getTeams()
 														.get(x)
 														.getGamesPlayed() + 1);
 								americanCentral
 										.getTeams()
 										.get(x)
 										.setPoints(
 												americanCentral.getTeams()
 														.get(x).getPoints() + 1);
 								americanCentral.getTeams().get(x)
 										.addUpcomingMatch(match);
 								americanCentral.addFixture(match);
 							}
 					}
 					if (americanEast.isMember(t1)) {
 						for (int x = 0; x < americanEast.getTeams().size(); x++)
 							if (americanEast.getTeams().get(x).getName()
 									.equalsIgnoreCase(t1.getName())) {
 								americanEast
 										.getTeams()
 										.get(x)
 										.setGamesPlayed(
 												americanEast.getTeams().get(x)
 														.getGamesPlayed() + 1);
 								americanEast
 										.getTeams()
 										.get(x)
 										.setPoints(
 												americanEast.getTeams().get(x)
 														.getPoints() + 1);
 								americanEast.getTeams().get(x)
 										.addUpcomingMatch(match);
 								americanEast.addFixture(match);
 							}
 					}
 					if (americanWest.isMember(t1)) {
 						for (int x = 0; x < americanWest.getTeams().size(); x++)
 							if (americanWest.getTeams().get(x).getName()
 									.equalsIgnoreCase(t1.getName())) {
 								americanWest
 										.getTeams()
 										.get(x)
 										.setGamesPlayed(
 												americanWest.getTeams().get(x)
 														.getGamesPlayed() + 1);
 								americanWest
 										.getTeams()
 										.get(x)
 										.setPoints(
 												americanWest.getTeams().get(x)
 														.getPoints() + 1);
 								americanWest.getTeams().get(x)
 										.addUpcomingMatch(match);
 								americanWest.addFixture(match);
 							}
 					}
 
 					/** increment games played for team 2 and add fixture */
 
 					if (nationalCentral.isMember(t2)) {
 						for (int x = 0; x < nationalCentral.getTeams().size(); x++)
 							if (nationalCentral.getTeams().get(x).getName()
 									.equalsIgnoreCase(t2.getName())) {
 								nationalCentral
 										.getTeams()
 										.get(x)
 										.setGamesPlayed(
 												nationalCentral.getTeams()
 														.get(x)
 														.getGamesPlayed() + 1);
 								nationalCentral.getTeams().get(x)
 										.addUpcomingMatch(match);
 
 							}
 					}
 					if (nationalEast.isMember(t2)) {
 						for (int x = 0; x < nationalEast.getTeams().size(); x++)
 							if (nationalEast.getTeams().get(x).getName()
 									.equalsIgnoreCase(t2.getName())) {
 								nationalEast
 										.getTeams()
 										.get(x)
 										.setGamesPlayed(
 												nationalEast.getTeams().get(x)
 														.getGamesPlayed() + 1);
 								nationalEast.getTeams().get(x)
 										.addUpcomingMatch(match);
 							}
 					}
 					if (nationalWest.isMember(t2)) {
 						for (int x = 0; x < nationalWest.getTeams().size(); x++)
 							if (nationalWest.getTeams().get(x).getName()
 									.equalsIgnoreCase(t2.getName())) {
 								nationalWest
 										.getTeams()
 										.get(x)
 										.setGamesPlayed(
 												nationalWest.getTeams().get(x)
 														.getGamesPlayed() + 1);
 								nationalWest.getTeams().get(x)
 										.addUpcomingMatch(match);
 							}
 					}
 					if (americanCentral.isMember(t2)) {
 						for (int x = 0; x < americanCentral.getTeams().size(); x++)
 							if (americanCentral.getTeams().get(x).getName()
 									.equalsIgnoreCase(t2.getName())) {
 								americanCentral
 										.getTeams()
 										.get(x)
 										.setGamesPlayed(
 												americanCentral.getTeams()
 														.get(x)
 														.getGamesPlayed() + 1);
 								americanCentral.getTeams().get(x)
 										.addUpcomingMatch(match);
 							}
 					}
 					if (americanEast.isMember(t2)) {
 						for (int x = 0; x < americanEast.getTeams().size(); x++)
 							if (americanEast.getTeams().get(x).getName()
 									.equalsIgnoreCase(t2.getName())) {
 								americanEast
 										.getTeams()
 										.get(x)
 										.setGamesPlayed(
 												americanEast.getTeams().get(x)
 														.getGamesPlayed() + 1);
 								americanEast.getTeams().get(x)
 										.addUpcomingMatch(match);
 							}
 					}
 					if (americanWest.isMember(t2)) {
 						for (int x = 0; x < americanWest.getTeams().size(); x++)
 							if (americanWest.getTeams().get(x).getName()
 									.equalsIgnoreCase(t2.getName())) {
 								americanWest
 										.getTeams()
 										.get(x)
 										.setGamesPlayed(
 												americanWest.getTeams().get(x)
 														.getGamesPlayed() + 1);
 								americanWest.getTeams().get(x)
 										.addUpcomingMatch(match);
 							}
 					}
 
 				}
 
 				else {
 
 					// if team 2 wins
 
 					/**
 					 * increment games played for team 2 if they won the game,
 					 * increase points and add fixture
 					 */
 
 					if (nationalCentral.isMember(t2)) {
 						for (int x = 0; x < nationalCentral.getTeams().size(); x++)
 							if (nationalCentral.getTeams().get(x).getName()
 									.equalsIgnoreCase(t2.getName())) {
 								nationalCentral
 										.getTeams()
 										.get(x)
 										.setGamesPlayed(
 												nationalCentral.getTeams()
 														.get(x)
 														.getGamesPlayed() + 1);
 								nationalCentral
 										.getTeams()
 										.get(x)
 										.setPoints(
 												nationalCentral.getTeams()
 														.get(x).getPoints() + 1);
 								nationalCentral.addFixture(match);
 								nationalCentral.getTeams().get(x)
 										.addUpcomingMatch(match);
 							}
 					}
 					if (nationalEast.isMember(t2)) {
 						for (int x = 0; x < nationalEast.getTeams().size(); x++)
 							if (nationalEast.getTeams().get(x).getName()
 									.equalsIgnoreCase(t2.getName())) {
 								nationalEast
 										.getTeams()
 										.get(x)
 										.setGamesPlayed(
 												nationalEast.getTeams().get(x)
 														.getGamesPlayed() + 1);
 								nationalEast
 										.getTeams()
 										.get(x)
 										.setPoints(
 												nationalEast.getTeams().get(x)
 														.getPoints() + 1);
 								nationalEast.addFixture(match);
 								nationalEast.getTeams().get(x)
 										.addUpcomingMatch(match);
 							}
 					}
 					if (nationalWest.isMember(t2)) {
 						for (int x = 0; x < nationalWest.getTeams().size(); x++)
 							if (nationalWest.getTeams().get(x).getName()
 									.equalsIgnoreCase(t2.getName())) {
 								nationalWest
 										.getTeams()
 										.get(x)
 										.setGamesPlayed(
 												nationalWest.getTeams().get(x)
 														.getGamesPlayed() + 1);
 								nationalWest
 										.getTeams()
 										.get(x)
 										.setPoints(
 												nationalWest.getTeams().get(x)
 														.getPoints() + 1);
 								nationalWest.addFixture(match);
 								nationalWest.getTeams().get(x)
 										.addUpcomingMatch(match);
 							}
 					}
 					if (americanCentral.isMember(t2)) {
 						for (int x = 0; x < americanCentral.getTeams().size(); x++)
 							if (americanCentral.getTeams().get(x).getName()
 									.equalsIgnoreCase(t2.getName())) {
 								americanCentral
 										.getTeams()
 										.get(x)
 										.setGamesPlayed(
 												americanCentral.getTeams()
 														.get(x)
 														.getGamesPlayed() + 1);
 								americanCentral
 										.getTeams()
 										.get(x)
 										.setPoints(
 												americanCentral.getTeams()
 														.get(x).getPoints() + 1);
 								americanCentral.addFixture(match);
 								americanCentral.getTeams().get(x)
 										.addUpcomingMatch(match);
 							}
 					}
 					if (americanEast.isMember(t2)) {
 						for (int x = 0; x < americanEast.getTeams().size(); x++)
 							if (americanEast.getTeams().get(x).getName()
 									.equalsIgnoreCase(t2.getName())) {
 								americanEast
 										.getTeams()
 										.get(x)
 										.setGamesPlayed(
 												americanEast.getTeams().get(x)
 														.getGamesPlayed() + 1);
 								americanEast
 										.getTeams()
 										.get(x)
 										.setPoints(
 												americanEast.getTeams().get(x)
 														.getPoints() + 1);
 								americanEast.addFixture(match);
 								americanEast.getTeams().get(x)
 										.addUpcomingMatch(match);
 							}
 					}
 					if (americanWest.isMember(t2)) {
 						for (int x = 0; x < americanWest.getTeams().size(); x++)
 							if (americanWest.getTeams().get(x).getName()
 									.equalsIgnoreCase(t2.getName())) {
 								americanWest
 										.getTeams()
 										.get(x)
 										.setGamesPlayed(
 												americanWest.getTeams().get(x)
 														.getGamesPlayed() + 1);
 								americanWest
 										.getTeams()
 										.get(x)
 										.setPoints(
 												americanWest.getTeams().get(x)
 														.getPoints() + 1);
 								americanWest.addFixture(match);
 								americanWest.getTeams().get(x)
 										.addUpcomingMatch(match);
 							}
 					}
 
 					/**
 					 * increment games played for team 2 if they lost the game,
 					 * and add fixture
 					 */
 
 					if (nationalCentral.isMember(t1)) {
 						for (int x = 0; x < nationalCentral.getTeams().size(); x++)
 							if (nationalCentral.getTeams().get(x).getName()
 									.equalsIgnoreCase(t1.getName())) {
 								nationalCentral
 										.getTeams()
 										.get(x)
 										.setGamesPlayed(
 												nationalCentral.getTeams()
 														.get(x)
 														.getGamesPlayed() + 1);
 								nationalCentral.getTeams().get(x)
 										.addUpcomingMatch(match);
 							}
 					}
 					if (nationalEast.isMember(t1)) {
 						for (int x = 0; x < nationalEast.getTeams().size(); x++)
 							if (nationalEast.getTeams().get(x).getName()
 									.equalsIgnoreCase(t1.getName())) {
 								nationalEast
 										.getTeams()
 										.get(x)
 										.setGamesPlayed(
 												nationalEast.getTeams().get(x)
 														.getGamesPlayed() + 1);
 								nationalEast.getTeams().get(x)
 										.addUpcomingMatch(match);
 							}
 					}
 					if (nationalWest.isMember(t1)) {
 						for (int x = 0; x < nationalWest.getTeams().size(); x++)
 							if (nationalWest.getTeams().get(x).getName()
 									.equalsIgnoreCase(t1.getName())) {
 								nationalWest
 										.getTeams()
 										.get(x)
 										.setGamesPlayed(
 												nationalWest.getTeams().get(x)
 														.getGamesPlayed() + 1);
 								nationalWest.getTeams().get(x)
 										.addUpcomingMatch(match);
 							}
 					}
 					if (americanCentral.isMember(t1)) {
 						for (int x = 0; x < americanCentral.getTeams().size(); x++)
 							if (americanCentral.getTeams().get(x).getName()
 									.equalsIgnoreCase(t1.getName())) {
 								americanCentral
 										.getTeams()
 										.get(x)
 										.setGamesPlayed(
 												americanCentral.getTeams()
 														.get(x)
 														.getGamesPlayed() + 1);
 								americanCentral.getTeams().get(x)
 										.addUpcomingMatch(match);
 							}
 					}
 					if (americanEast.isMember(t1)) {
 						for (int x = 0; x < americanEast.getTeams().size(); x++)
 							if (americanEast.getTeams().get(x).getName()
 									.equalsIgnoreCase(t1.getName())) {
 								americanEast
 										.getTeams()
 										.get(x)
 										.setGamesPlayed(
 												americanEast.getTeams().get(x)
 														.getGamesPlayed() + 1);
 								americanEast.getTeams().get(x)
 										.addUpcomingMatch(match);
 							}
 					}
 					if (americanWest.isMember(t1)) {
 						for (int x = 0; x < americanWest.getTeams().size(); x++)
 							if (americanWest.getTeams().get(x).getName()
 									.equalsIgnoreCase(t1.getName())) {
 								americanWest
 										.getTeams()
 										.get(x)
 										.setGamesPlayed(
 												americanWest.getTeams().get(x)
 														.getGamesPlayed() + 1);
 								americanWest.getTeams().get(x)
 										.addUpcomingMatch(match);
 							}
 					}
 
 				}
 
 			}
 
 			if (lCount == 2430)
 				System.out.println("break for inspection");
 
 			lCount++;
 			System.out.println(lCount);
 
 		}// end of while loop
 
 		System.out.println(americanEast);
 		System.out.println(americanCentral);
 		System.out.println(americanWest);
 		System.out.println(nationalEast);
 		System.out.println(nationalCentral);
 		System.out.println(nationalWest);
 
 	}// added for constructor
 
 	public Division getNationalEast() {
 		return nationalEast;
 	}
 
 	public void setNationalEast(Division nationalEast) {
 		this.nationalEast = nationalEast;
 	}
 
 	public Division getNationalCentral() {
 		return nationalCentral;
 	}
 
 	public void setNationalCentral(Division nationalCentral) {
 		this.nationalCentral = nationalCentral;
 	}
 
 	public Division getNationalWest() {
 		return nationalWest;
 	}
 
 	public void setNationalWest(Division nationalWest) {
 		this.nationalWest = nationalWest;
 	}
 
 	public Division getAmericanEast() {
 		return americanEast;
 	}
 
 	public void setAmericanEast(Division americanEast) {
 		this.americanEast = americanEast;
 	}
 
 	public Division getAmericanCentral() {
 		return americanCentral;
 	}
 
 	public void setAmericanCentral(Division americanCentral) {
 		this.americanCentral = americanCentral;
 	}
 
 	public Division getAmericanWest() {
 		return americanWest;
 	}
 
 	public void setAmericanWest(Division americanWest) {
 		this.americanWest = americanWest;
 	}
 
 	public String toString() {
 		return String.format("%s", nationalEast, nationalCentral, nationalWest);
 	}
 
 	public boolean isVerbose() {
 		return verbose;
 	}
 
 	public void setVerbose() {
 		verbose = true;
 	}
 }
