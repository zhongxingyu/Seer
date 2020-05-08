 //Copyright 2013 Tyler O'Meara
 //See License.txt for more details
 
 package com.TylerOMeara.EloCalculator;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 
 public class Main 
 {
 	/**
 	 * Number of participants specified on the first line of Participants.txt
 	 */
 	static int numParticipants;
 	
 	/**
 	 * Maps the Participant objects to their respective String identifications as specified in Participants.txt
 	 */
 	static HashMap<String,Participant> Participants = new HashMap<String,Participant>();
 	
 	/**
 	 * Whether or not to output data in a Reddit usable table format.
 	 */
 	static boolean reddit = true;
 	
 	/**
 	 * Whether or not to round the Win Percentages to the nearest who number when outputting it
 	 */
 	static boolean roundWinPercs = true;
 	
 	/**
 	 * Whether or not to round the Win Percentages to the nearest who number when outputting it
 	 */
 	static boolean useFullNames = false;
 	
 	/**
 	 * Whether or not to output the elo changes for every game
 	 */
 	static boolean perWinElo = false;
 	
 	/**
 	 * Holds the current week as specified in Games.txt
 	 */
 	static int currentWeek = 0;
 	
 	/**
 	 * It's the main method. Duh.
 	 * @param args Unused
 	 */
 	
 	final static String versionString = "0.0.5.026 7/1/2013";
 	
 	public static void main(String[] args)
 	{
 		System.out.println("EloCalculator Version " + versionString);
 		
 		//if an error occurred in loadSettings, exit
 		if(!loadSettings())
 		{
 			return;
 		}
 		
 		//if an error occurred in loadParticipants, exit
 		if(!loadParticipants())
 		{
 			return;
 		}
 		
 		//if an error occurred in loadGames, exit
 		if(!loadGames())
 		{
 			return;
 		}
 		
 		endOfWeekEloRatings();
 		winProbabilities();
 	}
 	
 	/**
 	 * Calculates the Elo change for the 2 participants and modifies the participants' Elo.
 	 * @param winner Participant that won the game.
 	 * @param loser Participant that lost the game.
 	 */
 	
 	public static void CalculateEloChange(Participant winner, Participant loser)
 	{
 		//Chance that the winner was going to win based on pre-game Elo values.
 		double winPerc = Math.pow(10, winner.getElo()/400)/(Math.pow(10, winner.getElo()/400) + Math.pow(10, loser.getElo()/400));
 		
 		//Calculate winner's Elo Change
 		double winnerEloChange = winner.getK()*(1-winPerc);	
 		winner.setElo(winner.getElo()+winnerEloChange);
 		winner.setWins(winner.getWins()+1);
 		winner.calculateWinRate();
 		
 		//Calculate loser's Elo Change
 		double loserEloChange = loser.getK()*(winPerc-1);
 		loser.setElo(loser.getElo()+loserEloChange);
 		loser.setLosses(loser.getLosses()+1);
 		loser.calculateWinRate();
 		
 		if(perWinElo)
 		{
 			System.out.println(winner.getName() + "(+" + winnerEloChange + ") won against " + loser.getName() + "(" + loserEloChange + ")" + " with an expected win chance of " + winPerc);
 		}
 	}
 	
 	/**
 	 * Ranks participants at the end of the week and outputs the ranking.
 	 */
 	
 	public static void endOfWeekEloRatings()
 	{
 		System.out.println();
 		System.out.println("End of week " + currentWeek + " Elo ratings");
 		
 		//Maps Elo Ratings to Participant ShortHandNames.
 		HashMap<Double, String> eloMap = new HashMap<Double,String>();
 		
 		//Loops through all participants and adds them to eloMap
 		for(Participant participant : Participants.values())
 		{
 			//In the event 2 (or more) participants share the same Elo, the value for that Elo is participant1Name:participant2Name...
 			if(eloMap.containsKey(participant.getElo()))
 			{
 				eloMap.put(participant.getElo(), eloMap.get(participant.getElo()) + ":" + participant.getShortName());
 			}
 			else
 			{
 				eloMap.put(participant.getElo(), participant.getShortName());
 			}
 		}
 	
 		//Array that holds all Elo values assigned to the participants
 		Double[] eloArray = new Double[eloMap.size()];
 		
 		//Puts the Elos into the Array
 		eloMap.keySet().toArray(eloArray);
 		
 		//Sorts the array from highest to lowest.
 		Arrays.sort(eloArray,Collections.reverseOrder());
 		int ranking = 1;
 		
 		//Formatting for the 2 output modes
 		if(!reddit)
 		{
 			System.out.println("---------------------------------");
 		}
 		else
 		{
 			System.out.println("| Rank | Team | Elo Rating | Elo Change | Win-Loss | Elo (Raw) | Elo Chg (Raw) |");
 			System.out.println("|:---:|:-----:|:---------:|:-------------:|:-----:|:---------:|:-------------:|");
 		}
 		
 		//Loops thrugh every elo in the array
 		for(double elo : eloArray)
 		{
 			//handles the case where this particular elo is held by only 1 participant
 			if(!eloMap.get(elo).contains(":"))
 			{
 				//Elo held by this participant at the end of the previous week.
 				double lastWeeksElo = Participants.get(eloMap.get(elo)).getEloByWeek().get(Participants.get(eloMap.get(elo)).getEloByWeek().size()-1);
 				String teamName = eloMap.get(elo);
 				
 				//Changes output to the full name if that setting is enabled
 				if(useFullNames)
 				{
 					teamName = Participants.get(eloMap.get(elo)).getName();
 				}
 				
 				//General Console output
 				if(!reddit)
 				{
 					System.out.println("#" + ranking + " " + teamName + " | " + elo + " (" + (elo - lastWeeksElo) + ")" + " | "
 							+ (int)Participants.get(eloMap.get(elo)).getWins() + "-" + (int)Participants.get(eloMap.get(elo)).getLosses()
 							+ " | " + Math.round(elo) + " (" + (Math.round(elo) - Math.round(lastWeeksElo)) + ")");
 				}
 				else //Output into a Reddit-friendly table
 				{
 					System.out.println("|" + ranking + "|" + teamName + "|" + Math.round(elo) + "|" + (Math.round(elo) - Math.round(lastWeeksElo))
 							+ "|" + (int)Participants.get(eloMap.get(elo)).getWins() + "-" + (int)Participants.get(eloMap.get(elo)).getLosses() + "|"
 							+ elo + "|" + (elo - lastWeeksElo) + "|");
 				}
 			}
 			else //Handle when 2 or more teams have the same Elo
 			{
 				String[] teams = eloMap.get(elo).split(":");
 				for(String team : teams)
 				{
 					//Elo held by this participant at the end of the previous week.
 					double lastWeeksElo = Participants.get(team).getEloByWeek().get(Participants.get(team).getEloByWeek().size()-1);
 					
 					//General Console output
 					if(!reddit)
 					{
 						System.out.println("#" + ranking + " " + team + " | " + elo + " (" + (elo - lastWeeksElo) + ")" + " | " 
 								+ (int)Participants.get(team).getWins() + "-" + (int)Participants.get(team).getLosses() + " | " + Math.round(elo) 
 								+ " (" + (Math.round(elo) - Math.round(lastWeeksElo)) + ")");
 					}
 					else //Output into a Reddit-friendly table
 					{
 						System.out.println("|" + ranking + "|" + team + "|" + Math.round(elo) + "|" + (Math.round(elo) - Math.round(lastWeeksElo))
 								+ "|" + (int)Participants.get(team).getWins() + "-" + (int)Participants.get(team).getLosses() + "|"
 								+ elo + "|" + (elo - lastWeeksElo) + "|");
 					}
 				}
 			}
 			ranking++;
 		}
 	}
 	
 	/**
 	 * Outputs the expected win probabilities for all future games based on teams' current Elo.
 	 */
 	public static void winProbabilities()
 	{
 		System.out.println();
 		System.out.println();
 		System.out.println("ESTIMATED WIN PERCENTAGES");
 		BufferedReader games;
 		
 		try 
 		{
 			games = new BufferedReader(new FileReader("FutureGames.txt"));
 			String line;
 			int lineNum = 0;
 			while((line = games.readLine()) != null)
 			{
 				lineNum++;
 				//Lines starting with // are special directives and should be handled separately.
 				if(!line.startsWith("//"))
 				{
 					String blue = line.split(":")[0];
 					String red = line.split(":")[1];
 					//Checks if the shorthand team names are valid
 					if(Participants.get(blue) == null || Participants.get(red) == null)
 					{
 						System.out.println("Invalid team on line " + lineNum);
 					}
 					
 					//Chance that the "blue" side team will win (whichever is on the left).
 					double blueWinPerc = Math.pow(10, Participants.get(blue).getElo()/400)/(Math.pow(10, Participants.get(blue).getElo()/400) + Math.pow(10, Participants.get(red).getElo()/400));
 					
					//Chance that the "red" side team will win (whichever is on the left).
 					double redWinPerc = 1-blueWinPerc;
 					//TODO: Add non-reddit formatting
 					if(reddit)
 					{
 						//Changes the strings to be the full names for the teams
 						if(useFullNames)
 						{
 							blue = Participants.get(blue).getName();
 							red = Participants.get(red).getName();
 						}
 						System.out.print("|" + blue);
 						if(roundWinPercs)
 						{
 							if(blueWinPerc >= .5)
 							{
 								System.out.println("|**" + Math.round(blueWinPerc*100) + "%**|vs|" + Math.round(redWinPerc*100) + "%|" + red + "|");
 							}
 							if(redWinPerc >= .5)
 							{
 								System.out.println("|" +  Math.round(blueWinPerc*100) + "%|vs|**" +  Math.round(redWinPerc*100) + "%**|" + red + "|");
 							}
 						}
 						else
 						{
 							if(blueWinPerc >= .5)
 							{
 								System.out.println("|**" + blueWinPerc*100 + "%**|vs|" + redWinPerc*100 + "%|" + red + "|");
 							}
 							if(redWinPerc >= .5)
 							{
 								System.out.println("|" + blueWinPerc*100 + "%|vs|**" + redWinPerc*100 + "%**|" + red + "|");
 							}
 						}
 					}
 					
 				}
 				else //Handle lines with //
 				{				
 					System.out.println();
 					System.out.println("Begin Week " +  Integer.valueOf(line.substring(line.lastIndexOf(" ") + 1)));
 					System.out.println();
 					if(reddit)
 					{
 						System.out.println("|Blue Team|Est. Win%||Est. Win%|Red Team|");
 						System.out.println("|:---:|:---:|:---:|:---:|:---:|");
 					}
 				}
 				
 			}
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Loads Participants.txt and adds all participants to the Participants HashMap
 	 * @return true if the method executed without error, false otherwise
 	 */
 	
 	public static boolean loadParticipants()
 	{
 		BufferedReader participants;
 		
 		//Open Participants.txt and read in it's data.
 		try {
 			participants = new BufferedReader(new FileReader("Participants.txt"));
 			
 			//Holds the line that is currently being processed.
 			String line;
 			
 			int lineNum = 0;
 			
 			//Loops until the end of the file is reached
 			while((line = participants.readLine()) != null)
 			{
 				//The first line holds how many participants are being ranked.
 				if(lineNum == 0)
 				{
 					numParticipants = Integer.valueOf(line);
 				}
 				//Every other line will have a single participant on it
 				//in the following format:
 				//ShortHandName:FullName
 				else
 				{
 					if(!StringCastUtils.isDouble(line.split(":")[2]))
 					{
 						System.out.println("Error on line " + lineNum + " of Participants.txt. Expected a number after the second semicolon.");
 						return false;
 					}
 					Participants.put(line.split(":")[0], new Participant(line.split(":")[1],line.split(":")[0],Double.valueOf(line.split(":")[2]),Double.valueOf(line.split(":")[3])));
 				}
 				lineNum++;
 			}
 		}
 		catch (IOException e) 
 		{
 			e.printStackTrace();
 		}
 		return true;
 	}
 	
 	/**
 	 * Loads Games.txt and calculates the participants Elos based on the results.
 	 * @return true if the method executed without error, false otherwise
 	 */
 	
 	public static boolean loadGames()
 	{
 		BufferedReader games;
 		try {
 			games = new BufferedReader(new FileReader("Games.txt"));
 			String line;
 			int lineNum = 0;
 			//Loops until the end of the file is reached
 			while((line = games.readLine()) != null)
 			{
 				lineNum++;
 				//Lines starting with // are special directives and should be handled separately.
 				if(!line.startsWith("//"))
 				{
 					String winner = line.split(":")[0];
 					String loser = line.split(":")[1];
 					if(Participants.get(winner) == null || Participants.get(loser) == null)
 					{
 						System.out.println("Unable to find participant specified on line " + lineNum + " of 'Games.txt'. Stopping execution.");
 						return false;
 					}
 					CalculateEloChange(Participants.get(winner),Participants.get(loser));
 				}
 				else if(line.startsWith("//Week")) //Handle lines that start with //Week
 				{
 					if(!line.equals("//Week 1"))
 					{
 						endOfWeekEloRatings();
 					}
 					
 					for(Participant team : Participants.values())
 					{
 						team.endOfWeek();
 					}
 					
 					System.out.println();
 					System.out.println("Begin " + line.substring(2));
 					
 					if(!StringCastUtils.isInteger(line.substring(line.lastIndexOf(" ") + 1)))
 					{
 						System.out.println("Error on line " + lineNum + " of Games.txt. Expected a number after the second semicolon.");
 						return false;
 					}
 					currentWeek = Integer.valueOf(line.substring(line.lastIndexOf(" ") + 1));
 					System.out.println();
 				}
 				else if(line.startsWith("//K-Change")) //Handle lines that start wtih //K-Change
 				{
 					if(Participants.get(line.split(":")[1]) == null)
 					{
 						System.out.println("Error on line " + lineNum + " of Games.txt. Expected a valid team shortHand name after the first semicolon.");
 						return false;
 					}
 					if(!StringCastUtils.isDouble(line.split(":")[2]))
 					{
 						System.out.println("Error on line " + lineNum + " of Games.txt. Expected a valid double after the second semicolon.");
 						return false;
 					}
 					Participants.get(line.split(":")[1]).setK(Double.valueOf(line.split(":")[2]));
 				}
 				
 			}
 		}
 		catch (IOException e) 
 		{
 			e.printStackTrace();
 		}
 		return true;
 	}
 	
 	/**
 	 * Loads Settings.txt
 	 * @return true if the method executed without error, false otherwise
 	 */
 	
 	public static boolean loadSettings()
 	{
 		BufferedReader settings;
 		try 
 		{
 			settings = new BufferedReader(new FileReader("Settings.txt"));
 			String line;
 			int lineNum = 0;
 			//Loops until the end of the file is reached
 			while((line = settings.readLine()) != null)
 			{
 				lineNum++;
 				if(line.startsWith("reddit:"))
 				{
 					if(!StringCastUtils.isBoolean(line.split(":")[1]))
 					{
 						System.out.println("Expected a boolean after the semicolon on line " + lineNum + " of Settings.txt.");
 						return false;
 					}
 					reddit = Boolean.valueOf(line.split(":")[1]);
 				}
 				
 				if(line.startsWith("roundWinPercentages:"))
 				{
 					if(!StringCastUtils.isBoolean(line.split(":")[1]))
 					{
 						System.out.println("Expected a boolean after the semicolon on line " + lineNum + " of Settings.txt.");
 						return false;
 					}
 					roundWinPercs = Boolean.valueOf(line.split(":")[1]);
 				}
 				
 				if(line.startsWith("useFullName:"))
 				{
 					if(!StringCastUtils.isBoolean(line.split(":")[1]))
 					{
 						System.out.println("Expected a boolean after the semicolon on line " + lineNum + " of Settings.txt.");
 						return false;
 					}
 					useFullNames = Boolean.valueOf(line.split(":")[1]);
 				}
 				
 				if(line.startsWith("displayPerWinEloChanges:"))
 				{
 					if(!StringCastUtils.isBoolean(line.split(":")[1]))
 					{
 						System.out.println("Expected a boolean after the semicolon on line " + lineNum + " of Settings.txt.");
 						return false;
 					}
 					perWinElo = Boolean.valueOf(line.split(":")[1]);
 				}
 			}
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 		return true;
 	}
 }
