 package no.runsafe.nchat.database;
 
 import no.runsafe.framework.api.command.ICommandExecutor;
 import no.runsafe.framework.api.database.*;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 
 public class IgnoreDatabase extends Repository
 {
 	@Override
 	public String getTableName()
 	{
 		return "nchat_ignore";
 	}
 
 	@Override
 	public ISchemaUpdate getSchemaUpdateQueries()
 	{
 		ISchemaUpdate update = new SchemaUpdate();
 
 		update.addQueries(
 			"CREATE TABLE `nchat_ignore` (" +
 				"`player` VARCHAR(16) NULL," +
 				"`ignore` VARCHAR(16) NULL," +
 				"PRIMARY KEY (`player`, `ignore`)" +
 			')'
 		);
		update.addQueries("DELETE FROM `nchat_ignore` WHERE player like `ignore`");
 		return update;
 	}
 
 	public HashMap<String, List<String>> getIgnoreList()
 	{
 		HashMap<String, List<String>> ignoreList = new LinkedHashMap<String, List<String>>(1);
 		ISet result = database.query("SELECT `player`, `ignore` FROM nchat_ignore");
 		for (IRow row : result)
 		{
 			String ignoredPlayer = row.String("ignore");
 			if (!ignoreList.containsKey(ignoredPlayer))
 				ignoreList.put(ignoredPlayer, new ArrayList<String>(1));
 
 			ignoreList.get(ignoredPlayer).add(row.String("player"));
 		}
 
 		return ignoreList;
 	}
 
 	public void ignorePlayer(ICommandExecutor player, ICommandExecutor ignore)
 	{
 		database.update("INSERT IGNORE INTO nchat_ignore (`player`, `ignore`) VALUES(?, ?)", player.getName(), ignore.getName());
 	}
 
 	public void removeIgnorePlayer(ICommandExecutor player, ICommandExecutor ignore)
 	{
 		database.update("DELETE FROM nchat_ignore WHERE `player` = ? AND `ignore` = ?", player.getName(), ignore.getName());
 	}
 }
