 package no.runsafe.UniqueInventories.Command;
 
 import no.runsafe.UniqueInventories.InventoryHandler;
 import no.runsafe.UniqueInventories.InventoryRepository;
 import no.runsafe.framework.command.RunsafeCommand;
 import no.runsafe.framework.command.RunsafePlayerCommand;
import no.runsafe.framework.server.RunsafeServer;
 import no.runsafe.framework.server.RunsafeWorld;
 import no.runsafe.framework.server.player.RunsafePlayer;
 
 public class WorldwipeCommand extends RunsafePlayerCommand
 {
 	public WorldwipeCommand(InventoryRepository inventories, InventoryHandler handler)
 	{
 		super("worldwipe", null, "world");
 		this.inventories = inventories;
 		this.handler = handler;
 	}
 
 	@Override
 	public boolean CanExecute(RunsafePlayer executor, String[] args)
 	{
 		if(args == null || args.length == 0)
 			return true;
 
 		worldName = args[0];
 
 		return executor.hasPermission("uniqueinventories.worldwipe." + worldName);
 	}
 
 	@Override
 	public String requiredPermission()
 	{
 		return "uniqueinventories.worldwipe." + worldName;
 	}
 
 	@Override
 	public String OnExecute(RunsafePlayer executor, String[] args)
 	{
 		String world = getArg("world");
 
 		inventories.Wipe(world);
 
		for (RunsafePlayer inWorld : RunsafeServer.Instance.getWorld(args[0]).getPlayers())
 			handler.loadTemplateInventory(inWorld);
 
 		return String.format("World %s inventories wiped.", world);
 	}
 
 	private InventoryRepository inventories;
 	private InventoryHandler handler;
 	private String worldName;
 }
