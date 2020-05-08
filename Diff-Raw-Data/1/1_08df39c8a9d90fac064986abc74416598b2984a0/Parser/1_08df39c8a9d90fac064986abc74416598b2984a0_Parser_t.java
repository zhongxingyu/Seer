 package uk.ac.gla.dcs.tp3.w.parser;
 
 import java.io.*;
 import java.util.HashMap;
 import java.util.Scanner;
 
 import uk.ac.gla.dcs.tp3.w.league.*;
 
 public class Parser {
 
 	private boolean verbose = false;
 	private Date current = new Date();
 	private HashMap<String, Division> divisions = new HashMap<String, Division>();
 	private final InputStream defaultFile = getClass().getResourceAsStream(
 			"/uk/ac/gla/dcs/tp3/w/parser/baseballSource.txt");
 
 	public Parser() {
 	}
 	
 	public void generateStandardDivisionInfo(){
 		this.init();
 	}
 
 	public boolean parse(String fileName) {
 		Scanner fs;
 		String[] line;
 		if (fileName.equals("")) {
 			fs = new Scanner(defaultFile);
 		} else {
 			try {
 				fs = new Scanner(new File(fileName));
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 				return false;
 			}
 		}
 		init();
 		while (fs.hasNextLine()) {
 			line = fs.nextLine().split(" ");
 			if (verbose) {
 				printLine(line);
 			}
 			if (line.length > 1) {
 				if (line.length == 3) {
 					newDate(line);
 				} else {
 					newMatch(line);
 				}
 			}
 		}
 		fs.close();
 		return true;
 	}
 
 	private void initDivision(String[] teams, String divisionName) {
 		Division d = new Division(divisionName);
 		divisions.put(divisionName, d);
 		for (String s : teams) {
 			d.addTeam(new Team(s, divisionName));
 		}
 	}
 
 	private void init() {
		divisions = new HashMap<String, Division>();
 		String[] aETeams = { "Baltimore Orioles", "Boston Red Sox",
 				"New York Yankees", "Tampa Bay Rays", "Toronto Blue Jays" };
 		initDivision(aETeams, "American East");
 
 		String[] aCTeams = { "Chicago White Sox", "Cleveland Indians",
 				"Detroit Tigers", "Kansas City Royals", "Minnesota Twins" };
 		initDivision(aCTeams, "American Central");
 
 		String[] aWTeams = { "Seattle Mariners", "Texas Rangers",
 				"Los Angeles Angels", "Oakland Athletics" };
 		initDivision(aWTeams, "American West");
 
 		String[] nETeams = { "Atlanta Braves", "Miami Marlins",
 				"New York Mets", "Philadelphia Phillies",
 				"Washington Nationals" };
 		initDivision(nETeams, "National East");
 
 		String[] nCTeams = { "Chicago Cubs", "Cincinnati Reds",
 				"Milwaukee Brewers", "Pittsburgh Pirates", "Houston Astros",
 				"St.Louis Cardinals" };
 		initDivision(nCTeams, "National Central");
 
 		String[] nWTeams = { "Arizona Diamondbacks", "Colorado Rockies",
 				"San Francisco Giants", "Los Angeles Dodgers",
 				"San Diego Padres" };
 		initDivision(nWTeams, "National West");
 	}
 
 	private void newDate(String[] line) {
 		int day = Integer.parseInt(line[0]);
 		int year = Integer.parseInt(line[2]);
 		current = new Date(day, line[1], year);
 		if (verbose) {
 			System.out.println("NEW DATE: " + current);
 		}
 	}
 
 	private void newMatch(String[] line) {
 		String[] time = line[0].split(":");
 		DateTime matchDate = new DateTime(current, Integer.parseInt(time[0]),
 				Integer.parseInt(time[1]));
 		int homeScore = -1;
 		int awayScore = -1;
 		String[] score = line[line.length - 1].split(":");
 		boolean played = false;
 		if (score.length == 2) {
 			homeScore = Integer.parseInt(score[0]);
 			awayScore = Integer.parseInt(score[1]);
 			played = true;
 		}
 		String firstTeam = "";
 		String secondTeam = "";
 		int i = 1;
 		for (i = 1; !line[i].equals("-"); i++) {
 			firstTeam += line[i] + " ";
 		}
 		firstTeam = firstTeam.trim();
 		for (i++; i < line.length - 1; i++) {
 			secondTeam += line[i] + " ";
 		}
 		secondTeam = secondTeam.trim();
 		if (verbose) {
 			System.out.println("MATCH:");
 			System.out.println("\t" + firstTeam + " plays " + secondTeam);
 			System.out.println("\t\ton: " + matchDate);
 			System.out.println("\t\tresult: " + homeScore + ":" + awayScore);
 			System.out.println("\t\tplayed: " + played);
 		}
 		Team homeTeam = getTeam(firstTeam);
 		Team awayTeam = getTeam(secondTeam);
 		if (homeScore == -1 || awayScore == -1) {
 			warning("match has no score", line);
 			return;
 		}
 		if (homeTeam == null) {
 			error("cannot find home team", line);
 			return;
 		}
 		if (awayTeam == null) {
 			error("cannot find away team", line);
 			return;
 		}
 		Division d = divisions.get(homeTeam.getDivisionName());
 		if (d == null) {
 			error("cannot find division", line);
 			return;
 		}
 		Match m = new Match(homeTeam, awayTeam, homeScore, awayScore,
 				matchDate, false);
 		homeTeam.addUpcomingMatch(m);
 		awayTeam.addUpcomingMatch(m);
 		d.addFixture(m);
 		if (played) {
 			m.playMatch();
 		}
 	}
 
 	private void warning(String string, String[] line) {
 		if (verbose) {
 			System.err.println("Warning: " + string);
 			printLine(line);
 		}
 	}
 
 	private void error(String string, String[] line) {
 		System.err.println("Error: " + string);
 		printLine(line);
 	}
 
 	private static void printLine(String[] line) {
 		System.err.print("[");
 		for (String s : line) {
 			System.err.print(s + ", ");
 		}
 		System.err.println("]: Length = " + line.length);
 	}
 
 	private Team getTeam(String s) {
 		for (Division d : divisions.values()) {
 			for (Team t : d.getTeams()) {
 				if (t.getName().equalsIgnoreCase(s)) {
 					return t;
 				}
 			}
 		}
 		return null;
 	}
 
 	public void setVerbose() {
 		verbose = true;
 	}
 
 	public HashMap<String, Division> getDivisions() {
 		return divisions;
 	}
 }
