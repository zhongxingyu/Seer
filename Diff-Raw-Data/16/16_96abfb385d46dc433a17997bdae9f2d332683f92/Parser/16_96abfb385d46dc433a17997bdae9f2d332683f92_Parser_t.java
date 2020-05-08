 package uk.ac.gla.dcs.tp3.w.parser;
 
 import java.io.*;
 import java.util.HashMap;
 import java.util.Scanner;
 
 import uk.ac.gla.dcs.tp3.w.league.*;
 
 public class Parser {
 
 	private boolean verbose = false;
 	private Date current = new Date();
 	private HashMap<String, Division> divisions = new HashMap<String, Division>();
 
 	public Parser() {
 	}
 
 	public boolean parse(String fileName) {
		InputStream defaultFile = null;
 		Scanner fs;
 		String[] line;
 		if (fileName.equals("")) {
			defaultFile = this.getClass().getResourceAsStream(
 					"/uk/ac/gla/dcs/tp3/w/parser/baseballSource.txt");
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
 			if (verbose)
 				printLine(line);
 			if (line.length > 1)
 				if (line.length == 3)
 					newDate(line);
 				else
 					newMatch(line);
 		}
 		fs.close();
 		return true;
 	}
 
 	private void init() {
 		String[] aETeams = { "Baltimore Orioles", "Boston Red Sox",
 				"New York Yankees", "Tampa Bay Rays", "Toronto Blue Jays" };
 		Division americanEast = new Division("American East");
 		divisions.put(americanEast.getName(), americanEast);
 		for (String s : aETeams)
 			americanEast.addTeam(new Team(s, americanEast.getName()));
 
 		String[] aCTeams = { "Chicago White Sox", "Cleveland Indians",
 				"Detroit Tigers", "Kansas City Royals", "Minnesota Twins" };
 		Division americanCentral = new Division("American Central");
 		divisions.put(americanCentral.getName(), americanCentral);
 		for (String s : aCTeams)
 			americanCentral.addTeam(new Team(s, americanCentral.getName()));
 
 		String[] aWTeams = { "Seattle Mariners", "Texas Rangers",
 				"Houston Astros", "Los Angeles Angels", "Oakland Athletics" };
 		Division americanWest = new Division("American West");
 		divisions.put(americanWest.getName(), americanWest);
 		for (String s : aWTeams)
 			americanWest.addTeam(new Team(s, americanWest.getName()));
 
 		String[] nETeams = { "Atlanta Braves", "Miami Marlins",
 				"New York Mets", "Philadelphia Phillies",
 				"Washington Nationals" };
 		Division nationalEast = new Division("National East");
 		divisions.put(nationalEast.getName(), nationalEast);
 		for (String s : nETeams)
 			nationalEast.addTeam(new Team(s, nationalEast.getName()));
 
 		String[] nCTeams = { "Chicago Cubs", "Cincinnati Reds",
 				"Milwaukee Brewers", "Pittsburgh Pirates", "St.Louis Cardinals" };
 		Division nationalCentral = new Division("National Central");
 		divisions.put(nationalCentral.getName(), nationalCentral);
 		for (String s : nCTeams)
 			nationalCentral.addTeam(new Team(s, nationalCentral.getName()));
 
 		String[] nWTeams = { "Arizona Diamondbacks", "Colorado Rockies",
 				"San Francisco Giants", "Los Angeles Dodgers",
 				"San Diego Padres" };
 		Division nationalWest = new Division("National West");
 		divisions.put(nationalWest.getName(), nationalWest);
 		for (String s : nWTeams)
 			nationalWest.addTeam(new Team(s, nationalWest.getName()));
 	}
 
 	private void newDate(String[] line) {
 		int day = Integer.parseInt(line[0]);
 		int year = Integer.parseInt(line[2]);
 		current = new Date(day, line[1], year);
 		if (verbose)
 			System.out.println("--------------------\n" + "NEW DATE: "
 					+ current + "\n--------------------");
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
 		for (i = 1; !line[i].equals("-"); i++)
 			firstTeam += line[i] + " ";
 		firstTeam = firstTeam.trim();
 		for (i++; i < line.length - 1; i++)
 			secondTeam += line[i] + " ";
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
 		if (homeScore == -1 || awayScore == -1)
 			warning("match has no score", line);
 		if (homeTeam == null)
 			error("cannot find home team", line);
 		if (awayTeam == null)
 			error("cannot find away team", line);
 		Division d = divisions.get(homeTeam.getDivisionName());
 		if (d == null)
 			error("cannot find division", line);
 		if (d == null || homeTeam == null || awayTeam == null
 				|| homeScore == -1 || awayScore == -1)
 			return;
 		Match m = new Match(homeTeam, awayTeam, homeScore, awayScore,
 				matchDate, false);
 		homeTeam.addUpcomingMatch(m);
 		awayTeam.addUpcomingMatch(m);
 		d.addFixture(m);
 		if (played)
 			m.playMatch();
 	}
 
 	private void warning(String string, String[] line) {
 		System.out.println("Warning: " + string);
 		printLine(line);
 	}
 
 	private void error(String string, String[] line) {
 		System.out.println("Error: " + string);
 		printLine(line);
 	}
 
 	private static void printLine(String[] line) {
 		System.out.print("[");
 		for (String s : line)
 			System.out.print(s + ", ");
 		System.out.println("]: Length = " + line.length);
 	}
 
 	private Team getTeam(String s) {
 		for (Division d : divisions.values())
 			for (Team t : d.getTeams())
 				if (t.getName().equalsIgnoreCase(s))
 					return t;
 		return null;
 	}
 
 	public void setVerbose() {
 		verbose = true;
 	}
 
 	public HashMap<String, Division> getDivisions() {
 		return divisions;
 	}
 }
