 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.PrintStream;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 //import repository.SteamStoreHtmlRepository;
 //import repository.SteamXmlRepository;
 import service.SteamDataService;
 import model.*;
 
 public class Main {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {		
 				
 		//TODO allow group to be specified on command line
 		String group = "flopsoc";
 		
 		SteamDataService svc = new SteamDataService();
 		
 		long[] memberIds = svc.getSteamGroupMembers(group);
 
 		//for each profile, get all games
 		
 		//map of game id to steamgame object
 		HashMap<Integer, SteamGame> gameIdObjectMap = new HashMap<Integer, SteamGame>();
 		//map of game id to players
 		HashMap<Integer, ArrayList<SteamProfile>> gameIdPlayersMap = new HashMap<Integer, ArrayList<SteamProfile>>();
 
 		//maintain hashtable of game id, games
 		//maintain hashtable of game id, list of profiles
 		int count=0;
 		for (long id :memberIds)
 		{	
 			count++;
 			System.err.print(String.format("Getting profile for id %s (%s/%s)", id, count, memberIds.length));
 			SteamProfile profile = svc.getSteamProfile(id);
 			if (profile == null)
 			{
 				//TODO: bug - some profiles cannot be parsed?
 				System.err.println(String.format("Could not retrieve profile %s",id));
 				continue;
 			}
 			
 			System.err.println(String.format(" %s", profile.getName()));
 			if (profile != null)
 			{
 				
 				for (SteamProfileGameData gameData : profile.getAllProfileGameData())
 				{
 					SteamGame game = gameData.get_app();
 					
					// TODO: should filtering be in service layer? probably
					
 					//skip non multiplayer games
 					if ( !svc.isMultiplayer(game))
 					{
 						//System.err.println(String.format("%s is not multi-player. Discarding...", game.getName()));
 						continue;
 					}
 
 					if (svc.isFree(game))
 					{
 						//if we haven't seen it before
 						if (!gameIdObjectMap.containsKey(game.getId()))
 						{
 							//add game to details map
 							gameIdObjectMap.put(game.getId(), game);
 						}
 						//else nothing - we don't need to record player details
 					}
 					else
 					{
 						// standard game - if we haven't seen it before...
 						if (!gameIdObjectMap.containsKey(game.getId()))
 						{
 							//add game to details map
 							gameIdObjectMap.put(game.getId(), game);
 							// add new list containing player, add player map 
 							ArrayList<SteamProfile> list = new ArrayList<SteamProfile>();
 							list.add(profile);
 							gameIdPlayersMap.put(game.getId(), list);
 						}					
 						else
 						{
 							// game details should be in map
 							//gameIdDetailsMap.get(game.getId());
 							ArrayList<SteamProfile> list = gameIdPlayersMap.get(game.getId());
 							//add to list
 							list.add(profile);
 						}
 					}
 				}				
 			}
 		}
 		
 		//Sorting for output
 		//TODO: should this be in View classes, or in service layer?
 		//create new sorted hashtable of size of profile and game
 		TreeMap<Integer, SortedMap<String, SteamGame>> playerCountGamesMap = new TreeMap<Integer, SortedMap<String, SteamGame>>();
 
 		System.err.println("Sorting by player count...");
 
 		for (Integer gameId : gameIdPlayersMap.keySet())
 		{
 			ArrayList<SteamProfile> profiles = gameIdPlayersMap.get(gameId);
 			SteamGame gameDetail = gameIdObjectMap.get(gameId);
 			
 			if (!playerCountGamesMap.containsKey(profiles.size()))
 			{
 				// add new list containing player, add player map 
 				SortedMap<String, SteamGame> map = new TreeMap<String, SteamGame>();
 				map.put(gameDetail.getName(), gameDetail);
 				playerCountGamesMap.put(profiles.size(), map);
 			}					
 			else
 			{
 				//playerCountGamesMap.get(profiles.size());
 				SortedMap<String, SteamGame> map = playerCountGamesMap.get(profiles.size());
 				//add to list
 				map.put(gameDetail.getName(), gameDetail);
 			}
 		}				
 
 		/*
 		 *TODO:	Below this point is output and formatting
 		 *		This is View! Should be in separate classes!
 		 */
 
 		// Create file
 		PrintStream out = null;
 		try {
 			out = new PrintStream(new FileOutputStream("output.html", false));
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			out = System.out;
 			out.println();
 			out.println("COULD NOT CREATE FILE - fallback to console output");
 			out.println("--------------------------------------------------");
 			out.println();
 		}
 		//doctype
 		out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
 		//html start, open headers
 		out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head>");
 		//title
 		out.println(String.format("<title>Common games for %s</title>", group));
 		//link to CSS
 		out.println("<link href=\"flopsoc.css\" rel=\"stylesheet\" type=\"text/css\" />");
 		//close headers, open body
 		out.println("</head><body><div class=\"main\">");
 		//open table
 		out.println("<div class=\"tab\">");
 		//headers
 		out.println("<div class=\"row\"> " +
 						"<div class=\"header game\">Game</div> " +
 						"<div class=\"header playerCount\">Player Count</div> " +
 						"<div class=\"header playerList\">Players</div> " +
 					"<hr /></div> ");
 		
 		//System.err.println("Enumerating games:");
 		//gamedatastring, playercount, playerdatastring
 		
 		//define format for row
 		String output = "<div class=\"row\"> "+
 							"<div class=\"game\">%s</div> " +
 							"<div class=\"playerCount\">%s</div> " +
 							"<div class=\"playerList\">%s</div> " +
 						"<hr /></div> ";
 		// output sorted values - games, size, profiles (from earlier hashset)
 		for (Integer playerCount : playerCountGamesMap.descendingKeySet())
 		{
 			//ignore games only owned by one person - useful?
 			if (playerCount < 2)
 				continue;
 					
 			SortedMap<String, SteamGame> map = playerCountGamesMap.get(playerCount);
 
 			//how far are we? 
 			System.err.println(String.format("Players: %s - %s games, ", playerCount, map.size()));
 	
 			for (SteamGame game : map.values())
 			{
 				System.err.println(String.format("Game '%s' (id:%s)", game.getName(), game.getId()));
 				
 				//System.err.println(" is multi-player! Sending output.");
 				
 				//get game data output
 				String gamedata = formatGameData(game);
 				
 				ArrayList<SteamProfile> players = gameIdPlayersMap.get(game.getId());
 				String playerString = "";
 				
 				//make player data output
 				for ( int i = 0; i < players.size(); i++)
 				{
 					SteamProfile player = players.get(i);
 					playerString += formatProfileData(player, game.getId());
 					//if (i < players.size() -1)
 					//{
 					//	playerString += "<br/>";
 					//}
 				}
 								
 				out.println(String.format(output, gamedata, playerCount, playerString ));
 			}							
 		}
 		//table footer
 		out.println("</div><br/>");
 
 		out.println("<div class=\"free\">");
 		out.println("<b>Free games:</b><br/>");
 		String gameList = "";
 		
 		SteamGame[] freeGames = svc.getFreeGames();
 		System.err.println(String.format("Free - %s games, ", freeGames.length));
 		for (SteamGame game : freeGames)
 		{
 			System.err.println(String.format("Game '%s' (id:%s)", game.getName(), game.getId()));
 			gameList += String.format("<a href=\"%s\">%s</a>, ", game.getStoreUrl(), game.getName());
 		}
 		//remove last comma
 		if (gameList.lastIndexOf(", ") >= 0)
 		{
 			gameList = gameList.substring(0, gameList.lastIndexOf(", "));
 		}
 		//add line break
 		gameList += "<br/>";
 		out.println(gameList);
 		out.println("</div><br/>");
 		
 		
 		out.println("<div class=\"other\">");
 		//games owned by one person
 		out.println("<b>Games owned by one person:</b><br/>");
 		gameList = "";
 		
 		SortedMap<String, SteamGame> map = playerCountGamesMap.get(1);
 		
 		// in case of no games (shouldn't happen)
 		if (map == null)
 		{
 			map = new TreeMap<String, SteamGame>();
 		}
 		
 		System.err.println(String.format("Players: 1 - %s games, ", map.size()));
 		for (SteamGame game : map.values())
 		{
 			System.err.println(String.format("Game '%s' (id:%s)", game.getName(), game.getId()));
 			gameList += String.format("<a href=\"%s\">%s</a>, ", game.getStoreUrl(), game.getName());
 		}
 		//remove last comma
 		if (gameList.lastIndexOf(", ") >= 0)
 		{
 			gameList = gameList.substring(0, gameList.lastIndexOf(", "));
 		}
 		//add line break
 		gameList += "<br/>";
 		out.println(gameList);
 		out.println("</div>");
 		out.println("<div class=\"date\">");
 		//date info
 		DateFormat formatter = DateFormat.getDateInstance(DateFormat.LONG);
 		String dateinfo = formatter.format(new Date());
 		out.println(String.format("<br/>Last generated: %s", dateinfo));
 		out.println("</div>");
 		out.println("</div></body></html>");
 		
 		out.close();
 
 	}
 
 	private static String formatGameData(SteamGame game) {
 		String format = "<a href=\"%s\"><img src=\"%s\" alt=\"%s\" /><br/>%s</a>";
 		String output = String.format(format, game.getStoreUrl(), game.getLogoUrl(), game.getName(), game.getName());
 		return output;
 	}
 
 	
 	private static String formatProfileData(SteamProfile profile, int appId) {
 		String hoursplayed = "";
 		
 		hoursplayed = String.format("<br /> (Played %s hours) ", profile.getProfileGameData(appId).get_hoursOnRecord());
 		
 		String format = "<div class=\"player\"> <a href=\"%s\"><img src=\"%s\" alt=\"%s\" />%s</a></div> ";
 		String output = String.format(format, profile.getCommunityProfileUrl(), profile.getSmallAvatarUrl(), profile.getName(), profile.getName(), hoursplayed);
 		return output;
 	}
 }
