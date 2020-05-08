 package no.runsafe.essentialsimport;
 
import no.runsafe.framework.api.IConsole;
 import no.runsafe.framework.api.IScheduler;
 import no.runsafe.framework.api.IServer;
 import no.runsafe.framework.api.database.IDatabase;
 import no.runsafe.framework.api.player.IPlayer;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 import java.sql.Timestamp;
 
 public class UserControlImporter extends DataImporter
 {
 	public UserControlImporter(IScheduler scheduler, IConsole output, IDatabase database, IServer server)
 	{
 		super(scheduler, output);
 		this.database = database;
 		this.server = server;
 		essentials = new EssentialsFileReader(output);
 	}
 
 	@Override
 	String getName()
 	{
 		return "usercontrol";
 	}
 
 	@Override
 	void Import()
 	{
 		if (!essentials.hasNext())
 		{
 			console.logInformation("Essentials userdata not found, skipping!");
 			return;
 		}
 		console.logInformation("Importing user data from Essentials into Runsafe UserControl..");
 		int count = 0;
 		for (YamlConfiguration playerData : essentials)
 		{
 			if (playerData == null)
 				continue;
 
 			String playerName = essentials.getFile().getName().replace(".yml", "");
 
 			Timestamp login = new Timestamp(playerData.getLong("timestamps.login"));
 			Timestamp logout = null;
 			if (playerData.contains("timestamps.logout"))
 				logout = new Timestamp(playerData.getLong("timestamps.logout"));
 			String ip = playerData.getString("ipAddress");
 
 			String banReason = null;
 			if (playerData.contains("ban.reason"))
 			{
 				banReason = playerData.getString("ban.reason");
 				if (banReason != null)
 				{
 					IPlayer player = server.getPlayer(playerName);
 					if (player != null && player.isNotBanned())
 						banReason = null;
 				}
 			}
 			database.Update(
 				"INSERT INTO player_db (`name`,`joined`,`login`,`logout`,`ip`,`banned`,`ban_reason`) VALUES (?,?,?,?,INET_ATON(?),?,?)" +
 					"ON DUPLICATE KEY UPDATE " +
 					"`joined`=VALUES(`joined`)," +
 					"`login`=VALUES(`login`), " +
 					"`logout`=VALUES(`logout`)," +
 					"`ip`=VALUES(`ip`)," +
 					"`banned`=VALUES(`banned`)," +
 					"`ban_reason`=VALUES(`ban_reason`)",
 				playerName, login, login, logout, ip,
 				banReason == null ? null : logout,
 				banReason
 			);
 			count++;
 			try
 			{
 				Thread.sleep(1);
 			}
 			catch (InterruptedException e)
 			{
 			}
 		}
 		console.logInformation("Completed import of %d players into the UserControl database.", count);
 	}
 
 	private final IDatabase database;
 	private final EssentialsFileReader essentials;
 	private final IServer server;
 }
