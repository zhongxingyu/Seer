 package no.runsafe.runsafejail.database;
 
 import no.runsafe.framework.database.IDatabase;
 import no.runsafe.framework.database.Repository;
 import no.runsafe.framework.output.IOutput;
 import no.runsafe.framework.server.RunsafeLocation;
 import no.runsafe.runsafejail.objects.JailSentence;
 import no.runsafe.runsafejail.objects.JailedPlayer;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 public class JailedPlayersDatabase extends Repository
 {
 	public JailedPlayersDatabase(IDatabase database, IOutput console)
 	{
 		this.console = console;
 		this.database = database;
 	}
 
 	@Override
 	public String getTableName()
 	{
 		return "jailed_players";
 	}
 
 	@Override
 	public HashMap<Integer, List<String>> getSchemaUpdateQueries()
 	{
 		HashMap<Integer, List<String>> queries = new HashMap<Integer, List<String>>();
 		ArrayList<String> sql = new ArrayList<String>();
 		sql.add(
 			"CREATE TABLE `jailed_players` (" +
 					"`playerName` VARCHAR(20) NOT NULL," +
 					"`jailName` VARCHAR(30) NOT NULL," +
 					"`sentenceEnd` DATETIME NOT NULL," +
 					"`returnX` DOUBLE NOT NULL," +
 					"`returnY` DOUBLE NOT NULL," +
 					"`returnZ` DOUBLE NOT NULL," +
 					"`returnWorld` VARCHAR(50) NULL," +
					"PRIMARY KEY (`playerName`)" +
 			")"
 		);
 		queries.put(1, sql);
 		return queries;
 	}
 
 	public List<JailedPlayerDatabaseObject> getJailedPlayers()
 	{
 		ArrayList<JailedPlayerDatabaseObject> jailedPlayers = new ArrayList<JailedPlayerDatabaseObject>();
 		PreparedStatement select = this.database.prepare(
 				"SELECT playerName, jailName, sentenceEnd, returnX, returnY, returnZ, returnWorld FROM jailed_players"
 		);
 
 		try
 		{
 			ResultSet player = select.executeQuery();
 			while (player.next())
 			{
 				JailedPlayerDatabaseObject jailedPlayer = new JailedPlayerDatabaseObject();
 				jailedPlayer.setPlayerName(player.getString("playerName"));
 				jailedPlayer.setJailName(player.getString("jailName"));
 				jailedPlayer.setSentenceEnd(convert(player.getTimestamp("sentenceEnd")));
 				jailedPlayer.setReturnX(player.getDouble("returnX"));
 				jailedPlayer.setReturnY(player.getDouble("returnY"));
 				jailedPlayer.setReturnZ(player.getDouble("returnZ"));
 				jailedPlayer.setReturnWorld(player.getString("returnWorld"));
 
 				jailedPlayers.add(jailedPlayer);
 			}
 		}
 		catch (SQLException e)
 		{
 			this.console.write(e.getMessage());
 		}
 
 		return jailedPlayers;
 	}
 
 	public void addJailedPlayer(JailedPlayer player, JailSentence jailSentence)
 	{
 		PreparedStatement insert = database.prepare(
 				"INSERT INTO jailed_players (" +
 						"playerName," +
 						"jailName," +
 						"sentenceEnd," +
 						"returnX," +
 						"returnY," +
 						"returnZ," +
 						"returnWorld" +
 				") VALUES(?, ?, ?, ?, ?, ?, ?)"
 		);
 
 		try
 		{
 			RunsafeLocation returnLocation = player.getReturnLocation();
 
 			insert.setString(1, player.getName());
 			insert.setString(2, jailSentence.getJailName());
 			insert.setTimestamp(3, convert(jailSentence.getEndSentence()));
 			insert.setDouble(4, returnLocation.getX());
 			insert.setDouble(5, returnLocation.getY());
 			insert.setDouble(6, returnLocation.getZ());
 			insert.setString(7, returnLocation.getWorld().getName());
 			insert.executeUpdate();
 		}
 		catch (SQLException e)
 		{
 			this.console.write(e.getMessage());
 		}
 	}
 
 	public void removeJailedPlayer(String playerName)
 	{
 		PreparedStatement delete = database.prepare("DELETE FROM jailed_players WHERE playerName = ?");
 
 		try
 		{
 			delete.setString(1, playerName);
 			delete.executeUpdate();
 		}
 		catch (SQLException e)
 		{
 			this.console.write(e.getMessage());
 		}
 	}
 
 	private IDatabase database;
 	private IOutput console;
 }
