 package no.runsafe.moosic.customjukebox;
 
 import no.runsafe.framework.database.IDatabase;
 import no.runsafe.framework.database.Repository;
 import no.runsafe.framework.server.RunsafeLocation;
 import no.runsafe.framework.server.RunsafeServer;
 import no.runsafe.framework.server.RunsafeWorld;
 import no.runsafe.framework.server.inventory.RunsafeInventory;
 import no.runsafe.framework.server.inventory.RunsafeInventoryType;
import no.runsafe.framework.server.item.RunsafeItemStack;
 import no.runsafe.framework.server.item.meta.RunsafeMeta;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class CustomJukeboxRepository extends Repository
 {
 	public CustomJukeboxRepository(IDatabase database)
 	{
 		this.database = database;
 	}
 
 	@Override
 	public String getTableName()
 	{
 		return "moosic_jukeboxes";
 	}
 
	public void storeJukebox(RunsafeLocation location, RunsafeItemStack item)
 	{
 		RunsafeInventory holder = RunsafeServer.Instance.createInventory(null, RunsafeInventoryType.CHEST);
 		holder.addItems(item);
 
 		this.database.Execute(
 			"INSERT INTO `moosic_jukeboxes` (world, x, y, z, item) VALUES(?, ?, ?, ?, ?)",
 			location.getWorld().getName(),
 			location.getBlockX(),
 			location.getBlockY(),
 			location.getBlockZ(),
 			holder.serialize()
 		);
 	}
 
 	public void deleteJukeboxes(RunsafeLocation location)
 	{
 		this.database.Execute(
 			"DELETE FROM `moosic_jukeboxes` WHERE world = ? AND x = ? AND y = ? AND z = ?",
 			location.getWorld().getName(),
 			location.getBlockX(),
 			location.getBlockY(),
 			location.getBlockZ()
 		);
 	}
 
 	public List<CustomJukebox> getJukeboxes()
 	{
 		List<CustomJukebox> jukeboxes = new ArrayList<CustomJukebox>();
 		List<Map<String, Object>> data = this.database.Query("SELECT world, x, y, z, item FROM moosic_jukeboxes");
 
 		if (data != null)
 		{
 			for (Map<String, Object> node : data)
 			{
 				RunsafeWorld world = RunsafeServer.Instance.getWorld((String) node.get("world"));
 				if (world != null)
 				{
 					RunsafeLocation location = new RunsafeLocation(world, (Double) node.get("x"), (Double) node.get("y"), (Double) node.get("z"));
 					RunsafeInventory holder = RunsafeServer.Instance.createInventory(null, RunsafeInventoryType.CHEST);
 					holder.unserialize((String) node.get("item"));
 
 					jukeboxes.add(new CustomJukebox(location, (RunsafeMeta) holder.getContents().get(0)));
 				}
 			}
 		}
 		return jukeboxes;
 	}
 
 	@Override
 	public HashMap<Integer, List<String>> getSchemaUpdateQueries()
 	{
 		HashMap<Integer, List<String>> versions = new HashMap<Integer, List<String>>();
 		ArrayList<String> sql = new ArrayList<String>();
 		sql.add(
 			"CREATE TABLE `moosic_jukeboxes` (" +
 				"`world` VARCHAR(50) NOT NULL," +
 				"`x` DOUBLE NOT NULL," +
 				"`y` DOUBLE NOT NULL," +
 				"`z` DOUBLE NOT NULL," +
 				"`item` longtext" +
 				")"
 		);
 		versions.put(1, sql);
 		return versions;
 	}
 
 	private final IDatabase database;
 }
