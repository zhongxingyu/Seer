 package no.runsafe.runsafeinventories.commands;
 
 import no.runsafe.framework.command.ExecutableCommand;
 import no.runsafe.framework.server.ICommandExecutor;
 import no.runsafe.runsafeinventories.repositories.InventoryRepository;
 
 import java.util.HashMap;
 
 public class WipeWorld extends ExecutableCommand
 {
 	public WipeWorld(InventoryRepository inventoryRepository)
 	{
		super("wipeworld", "Removes all inventories from the database for a world", "worldName");
 		this.inventoryRepository = inventoryRepository;
 	}
 
 	@Override
 	public String OnExecute(ICommandExecutor executor, HashMap<String, String> parameters)
 	{
 		String worldName = parameters.get("worldName");
 		this.inventoryRepository.wipeWorld(worldName);
 		return String.format("Deleted all database inventories for the %s world", worldName);
 	}
 
 	private InventoryRepository inventoryRepository;
 }
