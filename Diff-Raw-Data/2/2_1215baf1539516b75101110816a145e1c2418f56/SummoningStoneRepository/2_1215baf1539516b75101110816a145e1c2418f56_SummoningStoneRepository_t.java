 package no.runsafe.warpdrive.summoningstone;
 
 import no.runsafe.framework.api.ILocation;
 import no.runsafe.framework.api.database.IDatabase;
 import no.runsafe.framework.api.database.IRow;
 import no.runsafe.framework.api.database.Repository;
 import no.runsafe.warpdrive.WarpDrive;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 public class SummoningStoneRepository extends Repository
 {
 	public SummoningStoneRepository(IDatabase database)
 	{
 		this.database = database;
 	}
 
 	public String getTableName()
 	{
 		return "summoningStones";
 	}
 
 	public List<ILocation> getStoneList()
 	{
 		List<ILocation> stones = new ArrayList<ILocation>();
 		for (IRow node : database.Query("SELECT world, x, y, z FROM summoningStones"))
 			stones.add(node.Location());
 		return stones;
 	}
 
 	public void wipeStoneList()
 	{
 		database.Execute("DELETE FROM summoningStones");
 	}
 
 	public void deleteSummoningStone(int ID)
 	{
 		WarpDrive.debug.debugFine("Delete stone %s from the database.", ID);
 		database.Execute("DELETE FROM summoningStones WHERE ID = ?", ID);
 	}
 
 	public int addSummoningStone(ILocation location)
 	{
 		database.Execute(
 			"INSERT INTO summoningStones (world, x, y, z) VALUES(?, ?, ?, ?)",
 			location.getWorld().getName(),
 			location.getX(),
 			location.getY(),
 			location.getZ()
 		);
		Integer id = database.QueryInteger("SELECT LAST_INSERT_ID() AS ID FROM summoningStones");
 		return id == null ? 0 : id;
 	}
 
 	@Override
 	public HashMap<Integer, List<String>> getSchemaUpdateQueries()
 	{
 		HashMap<Integer, List<String>> queries = new HashMap<Integer, List<String>>();
 		ArrayList<String> sql = new ArrayList<String>();
 		sql.add(
 			"CREATE TABLE `summoningStones` (" +
 				"`ID` int(10) NOT NULL AUTO_INCREMENT," +
 				"`world` VARCHAR(255) NOT NULL," +
 				"`x` DOUBLE NOT NULL," +
 				"`y` DOUBLE NOT NULL," +
 				"`z` DOUBLE NOT NULL," +
 				"PRIMARY KEY (`ID`)" +
 				")"
 		);
 		queries.put(1, sql);
 		return queries;
 	}
 
 	private final IDatabase database;
 }
