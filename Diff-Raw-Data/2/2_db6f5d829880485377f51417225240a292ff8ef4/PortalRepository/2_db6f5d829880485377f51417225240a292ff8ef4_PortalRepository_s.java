 package no.runsafe.warpdrive.portals;
 
 import no.runsafe.framework.database.IDatabase;
 import no.runsafe.framework.database.Repository;
 import no.runsafe.framework.output.IOutput;
 import no.runsafe.framework.server.RunsafeLocation;
 import no.runsafe.framework.server.RunsafeServer;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 public class PortalRepository extends Repository
 {
 	public PortalRepository(IDatabase database, IOutput console)
 	{
 		this.database = database;
 		this.console = console;
 	}
 
 	public String getTableName()
 	{
 		return "warpdrive_portals";
 	}
 
 	public List<PortalWarp> getPortalWarps()
 	{
 		List<PortalWarp> warps = new ArrayList<PortalWarp>();
 
 		PreparedStatement query = this.database.prepare(
			"SELECT" +
 					"ID," +
 					"permission," +
 					"type," +
 					"world," +
 					"x," +
 					"y," +
 					"z," +
 					"destWorld," +
 					"destX," +
 					"destY," +
 					"destZ," +
 					"destYaw," +
 					"destPitch," +
 					"innerRadius," +
 					"outerRadius" +
 			"FROM warpdrive_portals"
 		);
 
 		try
 		{
 			ResultSet result = query.executeQuery();
 			while (result.next())
 			{
 				warps.add(new PortalWarp(
 						result.getString("ID"),
 						new RunsafeLocation(
 								RunsafeServer.Instance.getWorld(result.getString("world")),
 								result.getDouble("x"),
 								result.getDouble("y"),
 								result.getDouble("z")
 						),
 						new RunsafeLocation(
 								RunsafeServer.Instance.getWorld(result.getString("destWorld")),
 								result.getDouble("destX"),
 								result.getDouble("destY"),
 								result.getDouble("destZ"),
 								result.getFloat("destYaw"),
 								result.getFloat("destPitch")
 						),
 						PortalType.getPortalType(result.getInt("type"))
 				));
 			}
 		}
 		catch (SQLException e)
 		{
 			console.write(e.getMessage());
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
 						"`destX` DOUBLE NOT NULL," +
 						"`destY` DOUBLE NOT NULL," +
 						"`destZ` DOUBLE NOT NULL," +
 						"`destYaw` DOUBLE NOT NULL," +
 						"`destPitch` DOUBLE NOT NULL," +
 						"`innerRadius` INT NOT NULL," +
 						"`outerRadius` INT NOT NULL," +
 						"PRIMARY KEY (`ID`)" +
 					")"
 		);
 		queries.put(1, sql);
 		return queries;
 	}
 
 	private IDatabase database;
 	private IOutput console;
 }
