 package no.runsafe.UniqueInventories;
 
 import no.runsafe.framework.database.IDatabase;
 import no.runsafe.framework.database.IRepository;
 import no.runsafe.framework.output.IOutput;
 import no.runsafe.framework.server.player.RunsafePlayer;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.logging.Level;
 
 public class InventoryRepository implements IRepository<InventoryStorage, RunsafePlayer>
 {
 	public InventoryRepository(IDatabase database, IOutput output, IUniverses universes)
 	{
 		this.database = database;
 		this.output = output;
 		this.universes = universes;
 	}
 
 	@Override
 	public InventoryStorage get(RunsafePlayer key)
 	{
 		return get(key, key.getWorld().getName());
 	}
 
 	public InventoryStorage get(RunsafePlayer player, String world)
 	{
 		PreparedStatement select = null;
 		String playerName = player.getName();
 		String inventoryName = universes.getInventoryName(world);
 		try
 		{
 			select = this.database.prepare("SELECT * FROM uniqueInventories WHERE playerName=? AND inventoryName=? ORDER BY stack DESC");
 			select.setString(1, playerName);
 			select.setString(2, inventoryName);
 			ResultSet set = select.executeQuery();
 			InventoryStorage inv = new InventoryStorage();
 			if (set.first())
 			{
 				inv.setPlayerName(set.getString("playerName"));
 				inv.setWorldName(set.getString("inventoryName"));
 				inv.setArmor(set.getString("armor"));
 				inv.setInventory(set.getString("inventory"));
 				inv.setLevel(set.getInt("level"));
 				inv.setExperience(set.getFloat("experience"));
 				inv.setSaved(set.getBoolean("saved"));
 				inv.setStack(set.getInt("stack"));
 			}
 			else
 			{
 				inv = createInventory(playerName, inventoryName, 0);
 			}
 			select.close();
 			return inv;
 		}
 		catch (SQLException e)
 		{
 			this.output.outputToConsole(e.getMessage(), Level.SEVERE);
 		}
 		return null;
 	}
 
 	@Override
 	public void persist(InventoryStorage inventory)
 	{
 		try
 		{
 			this.output.outputDebugToConsole(
 				String.format(
 					"Saving player %s in world %s (%s)",
 					inventory.getPlayerName(),
 					inventory.getWorldName(),
 					inventory.getInventory()
 				),
 				Level.FINE
 			);
 			PreparedStatement update = this.database.prepare(
 				"UPDATE uniqueInventories SET armor=?, inventory=?, level=?, experience=?, saved=?, stack=? WHERE playerName=? AND inventoryName=? AND stack=?"
 			);
 			update.setString(1, inventory.getArmor());
 			update.setString(2, inventory.getInventory());
 			update.setLong(3, inventory.getLevel());
 			update.setFloat(4, inventory.getExperience());
 			update.setBoolean(5, inventory.getSaved());
 			update.setInt(6, inventory.getStack());
 			update.setString(7, inventory.getPlayerName());
 			update.setString(8, universes.getInventoryName(inventory.getWorldName()));
 			update.setInt(9, inventory.getStack());
 			update.execute();
 		}
 		catch (SQLException e)
 		{
 			this.output.outputToConsole(e.getMessage(), Level.SEVERE);
 		}
 	}
 
 	@Override
 	public void delete(InventoryStorage inventory)
 	{
 		try
 		{
 			PreparedStatement delete = this.database.prepare(
 				"DELETE FROM uniqueInventories WHERE playerName=? AND inventoryName=? AND stack=?"
 			);
 			delete.setString(1, inventory.getPlayerName());
 			delete.setString(2, universes.getInventoryName(inventory.getWorldName()));
 			delete.setInt(3, inventory.getStack());
 			delete.execute();
 		}
 		catch (SQLException e)
 		{
 			this.output.outputToConsole(e.getMessage(), Level.SEVERE);
 		}
 	}
 
 	public InventoryStorage addStack(RunsafePlayer key)
 	{
 		try
 		{
 			InventoryStorage inv = get(key);
			return createInventory(key.getName(), universes.getInventoryName(key.getWorld().getName()), inv.getStack() + 1);
 		}
 		catch (SQLException e)
 		{
 			this.output.outputToConsole(e.getMessage(), Level.SEVERE);
 			return null;
 		}
 	}
 
 	private InventoryStorage createInventory(String playerName, String inventoryName, int stack) throws SQLException
 	{
 		InventoryStorage inv = new InventoryStorage();
 		PreparedStatement insert = this.database.prepare("INSERT INTO uniqueInventories (playerName, inventoryName, stack) VALUES (?, ?, ?)");
 		inv.setPlayerName(playerName);
 		inv.setWorldName(inventoryName);
 		inv.setSaved(true);
 		insert.setString(1, playerName);
 		insert.setString(2, inventoryName);
 		insert.setInt(3, stack);
 		insert.executeUpdate();
 		insert.close();
 		return inv;
 	}
 
 	private IDatabase database;
 	private IOutput output;
 	private IUniverses universes;
 }
