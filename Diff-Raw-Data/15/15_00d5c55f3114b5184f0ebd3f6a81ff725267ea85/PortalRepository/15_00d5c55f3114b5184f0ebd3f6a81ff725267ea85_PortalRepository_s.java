 package no.runsafe.warpdrive.portals;
 
 import no.runsafe.framework.database.IDatabase;
 import no.runsafe.framework.database.Repository;
 import no.runsafe.framework.database.Row;
 import no.runsafe.framework.database.Set;
 import no.runsafe.framework.server.RunsafeLocation;
 import no.runsafe.framework.server.RunsafeServer;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 public class PortalRepository extends Repository
 {
 	public PortalRepository(IDatabase database)
 	{
 		this.database = database;
 	}
 
 	public String getTableName()
 	{
 		return "warpdrive_portals";
 	}
 
 	public List<PortalWarp> getPortalWarps()
 	{
 		List<PortalWarp> warps = new ArrayList<PortalWarp>();
 
 		Set data = this.database.Query(
 			"SELECT `ID`,`permission`,`type`,`world`,`x`,`y`,`z`,`destWorld`,`destX`,`destY`,`destZ`,`destYaw`,`destPitch` " +
 				"FROM warpdrive_portals"
 		);
 
 		for (Row row : data)
 		{
 			warps.add(new PortalWarp(
 				row.String("ID"),
				new RunsafeLocation(
					RunsafeServer.Instance.getWorld(row.String("world")),
					row.Double("x"),
					row.Double("y"),
					row.Double("z")
				),
 				RunsafeServer.Instance.getWorld(row.String("destWorld")),
 				row.Double("destX"),
 				row.Double("destY"),
 				row.Double("destZ"),
 				row.Float("destYaw"),
 				row.Float("destPitch"),
 				PortalType.getPortalType(row.Integer("type"))
 			));
 		}
 		return warps;
 	}
 
 	@Override
 	public HashMap<Integer, List<String>> getSchemaUpdateQueries()
 	{
 		HashMap<Integer, List<String>> queries = new HashMap<Integer, List<String>>();
 		ArrayList<String> sql = new ArrayList<String>();
 		sql.add(
 			"CREATE TABLE `warpdrive_portals` (" +
 				"`ID` VARCHAR(50) NOT NULL," +
 				"`permission` VARCHAR(255) NOT NULL DEFAULT ''," +
 				"`type` TINYINT NOT NULL DEFAULT '0'," +
 				"`world` VARCHAR(255) NOT NULL," +
 				"`x` DOUBLE NOT NULL," +
 				"`y` DOUBLE NOT NULL," +
 				"`z` DOUBLE NOT NULL," +
 				"`destWorld` VARCHAR(255) NOT NULL," +
 				"`destX` DOUBLE NULL," +
 				"`destY` DOUBLE NULL," +
 				"`destZ` DOUBLE NULL," +
 				"`destYaw` DOUBLE NULL," +
 				"`destPitch` DOUBLE NULL," +
 				"PRIMARY KEY (`ID`)" +
 				")"
 		);
 		queries.put(1, sql);
 		return queries;
 	}
 
 	private IDatabase database;
 }
