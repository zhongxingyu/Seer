 package com.github.epicvrvs.matchhistorian;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.sql.Array;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Types;
 import java.util.ArrayList;
 import java.util.Properties;
 
 import org.jsoup.HttpStatusException;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 
 
 public class MatchHistorian {
 	Connection database;
 	
 	public MatchHistorian(Connection database) {
 		this.database = database;
 	}
 	
 	/**
 	 * Update the database entries for the given summoner.
 	 * If the summoner is not stored in the database yet, a new entry will be added.
 	 * This also causes the automatic update flag to be enabled by default. 
 	 * @param region the lolking.net lower case region string of the region the summoner resides on 
 	 * @param summonerId the numeric ID of the summoner on the game servers
 	 */
 	public void updateSummoner(String region, int summonerId) throws MatchHistorianException, ParserException, SQLException, HTTPException {
 		SummonerWebData profile = getProfile(region, summonerId);
 		try(Transaction transaction = new Transaction(database)) {
 			// Check if the summoner needs to be added to the database
 			try(Statement select = getStatement("select id from summoner where region = ? and summoner_id = ?")) {
 				select.setString(region);
 				select.setInteger(summonerId);
 				ResultSet result = select.query();
 				int id;
 				if(result.first()) {
 					// The summoner was already stored in the database
 					// Still need to make sure that automatic updates are enabled
 					id = result.getInt("id");
 					try(Statement update = getAutomaticUpdateStatement(region, summonerId, true)) {
 						select.update();
 					}
 				}
 				else {
 					// The summoner is not in the database yet
 					try(Statement insert = getStatement("insert into summoner (region, summoner_id, name, update_automatically) values (?, ?, ?, true)")) {
 						insert.setString(region);
 						insert.setInteger(summonerId);
 						insert.setString(profile.name);
 						insert.update();
 						id = insert.getInsertId();
 					}
 				}
 				for(GameResult game : profile.games) {
 					processGameResult(region, id, game);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Sets the automatic update flag of a summoner in the database.
 	 * @param region the lolking.net lower case region string of the region the summoner resides on 
 	 * @param summonerId the numeric ID of the summoner on the game servers
 	 * @param enable new value of the flag that determines if a summoner 
 	 */
 	public void enableSummonerUpdates(String region, int summonerId, boolean enable) throws MatchHistorianException, SQLException {
 		try(Statement statement = getAutomaticUpdateStatement(region, summonerId, enable)) {
 			int rowsAffected = statement.update();
 			if(rowsAffected == 0)
 				throw new MatchHistorianException("Unable to find summoner");
 		}
 	}
 	
 	Statement getStatement(String query) throws SQLException {
 		return new Statement(this.database, query);
 	}
 
 	Statement getAutomaticUpdateStatement(String region, int summonerId, boolean enable) throws SQLException {
 		Statement statement = getStatement("update table summoner set automatic_updates = ? where region = ? and summoner_id = ?");
 		statement.setBoolean(enable);
 		statement.setString(region);
 		statement.setInteger(summonerId);
 		return statement;
 	}
 	
 	void setAggregatedStatsVariables(Statement statement, GameResult game) throws SQLException {
 		statement.setInteger(game.victory ? 1 : 0);
 		statement.setInteger(game.victory ? 0 : 1);
 		statement.setInteger(game.kills);
 		statement.setInteger(game.deaths);
 		statement.setInteger(game.assists);
 		statement.setInteger(game.gold);
 		statement.setInteger(game.minionsKilled);
 		statement.setInteger(game.duration);
 	}
 	
 	void processGameResult(String region, int id, GameResult game) throws SQLException {
 		// Check if the game is in the database yet
 		try(Statement selectGameId = getStatement("select id from game where region = ? and game_id = ?")) {
 			selectGameId.setString(region);
 			selectGameId.setInteger(game.gameId);
 			ResultSet gameIdResult = selectGameId.query();
 			int gameId;
 			boolean gameInDatabase = gameIdResult.first();
 			if(gameInDatabase) {
 				gameId = gameIdResult.getInt("id");
 				// The game was already in the database so we have to make sure that these entries for the player are set
 				try(Statement update = getStatement("update game_player set spells = ?, kills = ?, deaths = ?, items = ?, gold = ?, minions_killed = ? where game_id = ? and summoner_id = ?")) {
 					update.update();
 				}
 			}
 			else {
 				// The game wasn't in the database yet, add it
 				try(Statement insertGame = getStatement("insert into table game (region, game_id, map, game_mode, time, duration, losing_team, winning_team) values (?, ?, ?::map_type, ?::game_mode_type, ?, ?, ?, ?)")) {
 					insertGame.setString(region);
 					insertGame.setInteger(game.gameId);
					insertGame.setString(GameResult.getMapString(game.map));
 					insertGame.setString(GameResult.getGameModeString(game.mode));
 					insertGame.setDate(new java.sql.Date(game.date.getTime()));
 					insertGame.setInteger(game.duration);
 					insertGame.setArray(GameResult.getTeamIds(database, game.losingTeam));
 					insertGame.setArray(GameResult.getTeamIds(database, game.winningTeam));
 					insertGame.update();
 					gameId = insertGame.getInsertId();
 				}
 			}
 			try(Statement selectAggregatedId = getStatement("select id from aggregated_statistics where summoner_id = ? and map = ?::map_type and game_mode = ?::game_mode_type and champion_id = ?")) {
 				selectAggregatedId.setInteger(id);
 				selectAggregatedId.setString(GameResult.getMapString(game.map));
 				selectAggregatedId.setString(GameResult.getGameModeString(game.mode));
 				selectAggregatedId.setInteger(game.championId);
 				ResultSet aggregatedIdResult = selectAggregatedId.query();
 				if(aggregatedIdResult.first()) {
 					// There is already an entry for that combination of summoner, map, game mode and champion
 					// Update the existing entry based on the ID
 					int aggregatedStatisticsId = aggregatedIdResult.getInt("id");
 					try(Statement updateAggregatedStatistics = getStatement("update aggregated_statistics set wins = wins + ?, losses = losses + ?, kills = kills + ?, deaths = deaths + ?, assists = assists + ?, gold = gold + ?, minions_killed = minions_killed + ?, duration = duration + ? where id = ?")) {
 						setAggregatedStatsVariables(updateAggregatedStatistics, game);
 						updateAggregatedStatistics.update();
 					}
 				}
 				else {
 					// The entry didn't exist yet, create a new one first
 					try(Statement updateAggregatedStatistics = getStatement("insert into table aggregated_statistics (summoner_id, map, game_mode, champion_id, wins, losses, kills, deaths, assists, gold, minions_killed, duration) values (?, ?::map_type, ?::game_mode_type, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
 						updateAggregatedStatistics.setInteger(id);
 						updateAggregatedStatistics.setString(GameResult.getMapString(game.map));
 						updateAggregatedStatistics.setString(GameResult.getGameModeString(game.mode));
 						updateAggregatedStatistics.setInteger(game.championId);
 						setAggregatedStatsVariables(updateAggregatedStatistics, game);
 						updateAggregatedStatistics.update();
 					}
 				}
 			}
 		}
 	}
 	
 	SummonerWebData getProfile(String region, int summonerId) throws HTTPException, MatchHistorianException, ParserException {
 		try {
 			String url = "http://www.lolking.net/summoner/" + region + "/" + summonerId;
 			Document document = Jsoup.connect(url).get();
 			String summonerName = Parser.getName(document);
 			ArrayList<GameResult> games = Parser.parseGames(document, summonerId);
 			SummonerWebData profile = new SummonerWebData(region, summonerName, summonerId, games);
 			return profile;
 		}
 		catch(HttpStatusException exception) {
 			if(exception.getStatusCode() == 404)
 				throw new MatchHistorianException("Unable to find summoner");
 			else
 				throw new HTTPException("Server error", exception);
 		}
 		catch(IOException exception) {
 			throw new HTTPException("IO exception", exception);
 		}
 	}
 }
