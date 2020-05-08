 package no.runsafe.mailbox.repositories;
 
 import no.runsafe.framework.database.IDatabase;
 import no.runsafe.framework.database.Repository;
 import no.runsafe.framework.server.RunsafeServer;
 import no.runsafe.framework.server.inventory.RunsafeInventory;
 import no.runsafe.framework.server.player.RunsafePlayer;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class MailboxRepository extends Repository
 {
 	public MailboxRepository(IDatabase database)
 	{
 		this.database = database;
 	}
 
 	@Override
 	public String getTableName()
 	{
 		return "player_mailboxes";
 	}
 
 	public RunsafeInventory getMailbox(RunsafePlayer player)
 	{
 		String playerName = player.getName();
 		Map<String, Object> data = this.database.QueryRow(
				"SELECT contents FROM player_mailboxes WHERE player = ?", playerName
 		);
 
 		RunsafeInventory inventory = RunsafeServer.Instance.createInventory(null, 27, String.format("%s's Mailbox", playerName));
 
 		if (data != null)
 			inventory.unserialize((String) data.get("contents"));
 
 		return inventory;
 	}
 
 	public void updateMailbox(RunsafePlayer player, RunsafeInventory inventory)
 	{
 		String contents = inventory.serialize();
 		this.database.Execute(
 			"INSERT INTO player_mailboxes (player, contents) VALUES(?, ?) ON DUPLICATE KEY UPDATE contents = ?",
 			player.getName(), contents, contents
 		);
 	}
 
 	@Override
 	public HashMap<Integer, List<String>> getSchemaUpdateQueries()
 	{
 		HashMap<Integer, List<String>> versions = new HashMap<Integer, List<String>>();
 		ArrayList<String> sql = new ArrayList<String>();
 		sql.add(
 				"CREATE TABLE `player_mailboxes` (" +
 						"`player` varchar(50) NOT NULL," +
 						"`contents` longtext," +
 						"PRIMARY KEY (`player`)" +
 				")"
 		);
 		versions.put(1, sql);
 		return versions;
 	}
 
 	private IDatabase database;
 }
