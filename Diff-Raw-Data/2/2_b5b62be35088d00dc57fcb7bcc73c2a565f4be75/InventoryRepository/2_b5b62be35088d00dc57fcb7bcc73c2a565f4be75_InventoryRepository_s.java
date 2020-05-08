 package no.runsafe.runsafeinventories.repositories;
 
 import no.runsafe.framework.api.database.IDatabase;
 import no.runsafe.framework.api.database.IRow;
 import no.runsafe.framework.api.database.Repository;
 import no.runsafe.framework.minecraft.player.RunsafePlayer;
 import no.runsafe.runsafeinventories.PlayerInventory;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 public class InventoryRepository extends Repository
 {
 	public InventoryRepository(IDatabase database)
 	{
 		this.database = database;
 	}
 
 	@Override
 	public String getTableName()
 	{
 		return "runsafeInventories";
 	}
 
 	public void saveInventory(PlayerInventory inventory)
 	{
 		database.Execute(
			"INSERT INTO runsafeInventories (owner, inventoryName, inventory, level, experience) VALUES(?, ?, ?, ?, ?, ?)" +
 				" ON DUPLICATE KEY UPDATE inventory = ?, level = ?, experience = ?, foodLevel = ?",
 			inventory.getPlayerName(), inventory.getInventoryName(),
 			inventory.getInventoryString(), inventory.getLevel(), inventory.getExperience(), inventory.getFoodLevel(),
 			inventory.getInventoryString(), inventory.getLevel(), inventory.getExperience(), inventory.getFoodLevel()
 		);
 	}
 
 	public PlayerInventory getInventory(RunsafePlayer player, String universeName)
 	{
 		String owner = player.getName();
 
 		IRow data = database.QueryRow(
 			"SELECT inventory, level, experience, foodLevel FROM runsafeInventories WHERE owner = ? AND inventoryName = ?",
 			owner, universeName
 		);
 
 		if (data == null)
 			return null; // We have no inventory, so no need to return a blank one.
 
 		long level = data.Long("level");
 
 		return new PlayerInventory(
 			owner,
 			universeName,
 			data.String("inventory"),
 			(int) level,
 			data.Float("experience"),
 			data.Integer("foodLevel")
 		);
 	}
 
 	public void wipeWorld(String worldName)
 	{
 		this.database.Execute("DELETE FROM runsafeInventories WHERE inventoryName = ?", worldName);
 	}
 
 	@Override
 	public HashMap<Integer, List<String>> getSchemaUpdateQueries()
 	{
 		HashMap<Integer, List<String>> versions = new HashMap<Integer, List<String>>();
 		ArrayList<String> sql = new ArrayList<String>();
 
 		sql.add(
 			"CREATE TABLE `runsafeInventories` (" +
 				"`owner` varchar(50) NOT NULL," +
 				"`inventoryName` varchar(255) NOT NULL," +
 				"`inventory` longtext," +
 				"`level` int(10) unsigned NOT NULL DEFAULT '0'," +
 				"`experience` float unsigned NOT NULL DEFAULT '0'," +
 				"PRIMARY KEY (`owner`,`inventoryName`)" +
 				")"
 		);
 		versions.put(1, sql);
 
 		sql.clear();
 		sql.add(
 				"ALTER TABLE `runsafeInventories`" +
 						"ADD COLUMN `foodLevel` TINYINT(2) UNSIGNED NOT NULL DEFAULT '20' AFTER `experience`"
 		);
 		versions.put(2, sql);
 		return versions;
 	}
 
 	private IDatabase database;
 }
