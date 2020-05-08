 package com.github.epicvrvs.matchhistorian;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.TimeZone;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 import org.jsoup.Jsoup;
 import org.jsoup.helper.Validate;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 public class Parser {
 	static Pattern integerPattern = Pattern.compile("\\d+");
 	static Pattern championIdPattern = Pattern.compile("(\\d+)_\\d+\\.png");
 	
 	public static String getName(Document document) throws ParserException {
 		Element nameElement = document.getElementsByAttributeValue("style", "font-size: 36px; line-height: 44px; white-space: nowrap;").first();
 		if(nameElement == null)
 			throw new ParserException("Unable to extract summoner name");
 		return nameElement.text();
 	}
 	
 	public static ArrayList<GameResult> parseGames(Document document, int summonerId) throws ParserException {
 		final String dataGameId = "data-game-id";
 		Elements gameElements = document.getElementsByAttribute(dataGameId);
 		SimpleDateFormat inputDateFormat = new SimpleDateFormat("MM/dd/yy hh:mmaa zzz");
 		SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
 		outputDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
 		ArrayList<GameResult> output = new ArrayList<GameResult>();
 		for (Element gameElement : gameElements) {
 			GameResult game = new GameResult();
 			game.gameId = Integer.parseInt(gameElement.attr(dataGameId));
 			game.victory = gameElement.className().equals("match_win");
 			Element icon = gameElement.getElementsByClass("summoner_icon_64").first();
 			String style = icon.attr("style");
 			Matcher championIdMatcher = championIdPattern.matcher(style);
 			if(!championIdMatcher.find())
 				throw new ParserException("Unable to extract champion ID");
 			game.championId = Integer.parseInt(championIdMatcher.group(1));
 			Element gameModeElement = gameElement.getElementsByAttributeValueContaining("style", "font-weight: bold").first();
 			String gameMode = gameModeElement.text();
 			setMapAndMode(gameMode, game);
 			final String dataHoverSwitch = "data-hoverswitch";
 			Element dateElement = gameElement.getElementsByAttribute(dataHoverSwitch).first();
 			String dateString = dateElement.attr(dataHoverSwitch);
 			try {
 				game.date = inputDateFormat.parse(dateString);
 			}
 			catch(ParseException exception) {
 				throw new ParserException("Unable to parse date string", exception);
 			}
 			final String matchDetailCell = "match_details_cell";
 			Elements detailCells = gameElement.getElementsByClass(matchDetailCell);
 			Element durationCell = detailCells.get(2);
 			String durationString = durationCell.child(0).getElementsByTag("strong").first().text();
 			Matcher durationMatcher = integerPattern.matcher(durationString);
 			if(!durationMatcher.find())
 				throw new ParserException("Unable to extract duration");
 			game.duration = Integer.parseInt(durationMatcher.group()) * 60;
 			Elements kdaElements = detailCells.get(3).getElementsByTag("strong");
 			game.kills = Integer.parseInt(kdaElements.get(0).text());
 			game.deaths = Integer.parseInt(kdaElements.get(1).text());
 			game.assists = Integer.parseInt(kdaElements.get(2).text());
 			String goldString = detailCells.get(4).getElementsByTag("strong").first().text();
 			game.gold = (int)(Float.parseFloat(goldString.substring(0, goldString.length() - 1)) * 1000);
 			game.minionsKilled = Integer.parseInt(detailCells.get(5).getElementsByTag("strong").first().text());
 			Elements spellsElements = detailCells.get(6).getElementsByClass("icon_36");
 			game.spells = new int[2];
 			for(int i = 0; i < game.spells.length; i++) {
 				String spellStyle = spellsElements.get(i).attr("style");
 				Matcher spellMatcher = integerPattern.matcher(spellStyle);
 				if(!spellMatcher.find())
 					throw new ParserException("Unable to extract summoner spells");
 				game.spells[i] = Integer.parseInt(spellMatcher.group());
 			}
 			Elements itemElements = detailCells.get(7).getElementsByAttributeValue("style", "display: table-cell; padding: 2px; width: 34px; height: 34px;");
 			game.items = new int[6];
 			for(int i = 0; i < game.items.length; i++) {
 				Elements links = itemElements.get(i).getElementsByTag("a");
 				int item = 0;
 				if(links.size() != 0) {
 					Matcher itemMatcher = integerPattern.matcher(links.get(0).attr("href"));
 					if(!itemMatcher.find())
 						throw new ParserException("Unable to extract item ID");
 					item = Integer.parseInt(itemMatcher.group());
 				}
 				game.items[i] = item;
 			}
 			Element extendedDetails = gameElement.getElementsByClass("match_details_extended").first();
 			Elements teams = extendedDetails.getElementsByTag("tbody");
 			Element losingTeamBody = gameElement.getElementsContainingOwnText("Losing Team").first().parent().parent().parent();
 			Element winningTeamBody = gameElement.getElementsContainingOwnText("Winning Team").first().parent().parent().parent();
 			game.losingTeam = parseTeam(losingTeamBody, summonerId);
 			game.winningTeam = parseTeam(winningTeamBody, summonerId);
 			output.add(game);
 		}
 		return output;
 	}
 	
 	static ArrayList<GamePlayer> parseTeam(Element teamBody, int defaultSummonerId) throws ParserException {
 		ArrayList<GamePlayer> output = new ArrayList<GamePlayer>();
 		Elements summonerElements = teamBody.getElementsByAttributeValue("style", "color: #FFF;");
 		Elements iconElements = teamBody.getElementsByAttributeValueContaining("style", "png");
 		for(int i = 0; i < summonerElements.size(); i++) {
 			Element summonerElement = summonerElements.get(i);
 			Element iconElement = iconElements.get(i);
 			Elements summonerLinks = summonerElement.getElementsByTag("a");
 			String name;
 			int summonerId;
 			if(summonerLinks.size() == 0) {
 				name = summonerElement.text();
 				summonerId = defaultSummonerId;
 			}
 			else {
 				Element summonerLink = summonerLinks.first();
 				name = summonerLink.text();
 				Matcher idMatcher = integerPattern.matcher(summonerLink.attr("href"));
 				if(!idMatcher.find())
 					throw new ParserException("Unable to extract summoner ID");
 				summonerId = Integer.parseInt(idMatcher.group());
 			}
 			Matcher championIdMatcher = championIdPattern.matcher(iconElement.attr("style"));
 			if(!championIdMatcher.find())
 				throw new ParserException("Unable to extract champion ID");
 			int championId = Integer.parseInt(championIdMatcher.group(1));
 			GamePlayer player = new GamePlayer(name, summonerId, championId);
 			output.add(player);
 		}
 		return output;
 	}
 	
 	static void setMapAndMode(String description, GameResult result) throws ParserException {
 		if(description.equals("Custom")) {
			result.map = null;
 			result.mode = GameMode.CUSTOM;
 		}
 		else if(description.equals("Normal 3v3")) {
 			result.map = Map.TWISTED_TREELINE;
 			result.mode = GameMode.NORMAL;
 		}
 		else if(description.equals("Normal 5v5")) {
 			result.map = Map.SUMMONERS_RIFT;
 			result.mode = GameMode.NORMAL;
 		}
 		else if(description.equals("Dominion")) {
 			result.map = Map.THE_CRYSTAL_SCAR;
 			result.mode = GameMode.NORMAL;
 		}
 		else if(description.equals("Howling Abyss")) {
 			result.map = Map.HOWLING_ABYSS;
 			result.mode = GameMode.NORMAL;
 		}
 		else if(description.equals("Ranked Team 3v3")) {
 			result.map = Map.TWISTED_TREELINE;
 			result.mode = GameMode.RANKED_TEAM;
 		}
 		else if(description.equals("Ranked Solo 5v5")) {
 			result.map = Map.SUMMONERS_RIFT;
 			result.mode = GameMode.RANKED_SOLO;
 		}
 		else if(description.equals("Ranked Team 5v5")) {
 			result.map = Map.SUMMONERS_RIFT;
 			result.mode = GameMode.RANKED_TEAM;
 		}
 		else if(description.equals("Co-Op Vs AI 3v3")) {
 			result.map = Map.TWISTED_TREELINE;
 			result.mode = GameMode.COOP_VS_AI;
 		}
 		else if(description.equals("Co-Op Vs AI 5v5")) {
 			result.map = Map.SUMMONERS_RIFT;
 			result.mode = GameMode.COOP_VS_AI;
 		}
 		else if(description.equals("Co-Op Vs AI Dominion")) {
 			result.map = Map.THE_CRYSTAL_SCAR;
 			result.mode = GameMode.COOP_VS_AI;
 		}
 		else {
 			throw new ParserException("Unknown map/mode description: " + description);
 		}
 	}
 }
