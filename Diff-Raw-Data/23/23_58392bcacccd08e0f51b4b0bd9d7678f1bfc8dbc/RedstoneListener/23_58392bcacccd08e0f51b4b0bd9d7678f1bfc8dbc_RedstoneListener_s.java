 package no.HON95.ButtonCommands;
 
 import java.util.Set;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockRedstoneEvent;
 
 class RedstoneListener implements Listener
 {
 
 	private final BCMain PLUGIN;
 	Set<String> whiteList = null;
 	boolean enableRedstone = false;
 	boolean ignoreWhiteLists = false;
 	boolean outputInfo = false;
 
 	RedstoneListener(BCMain instance)
 	{
 		PLUGIN = instance;
 	}
 
 	@EventHandler
 	public void onBlockRedstoneEvent(BlockRedstoneEvent ev)
 	{
 
 		if (!enableRedstone)
 			return;
 
 		Block block = ev.getBlock();
 
 		if (!block.isBlockPowered() || block.getType() != Material.WALL_SIGN)
 			return;
 
 		Sign sign = (Sign) block.getState();
 		String[] lines = sign.getLines();
 
 		if (!lines[1].startsWith("/"))
 			return;
 
 		lines[1] = lines[1].replaceFirst("/", "");
 
 		String[] cmd = Misc.concatCmd(lines);
 		cmd[1] = Misc.insertAll(cmd[1], PLUGIN.getServer().getConsoleSender(), block);
 
 		if (cmd[0].equalsIgnoreCase("redstone"))
 			cmd[1] = cmd[1].replaceFirst("redstone ", "");
 		else if (cmd[0].equalsIgnoreCase("r"))
 			cmd[1] = cmd[1].replaceFirst("r ", "");
 		else
 			return;
 
 		if (cmd[1].length() == 0)
 			return;
 
 		if (ignoreWhiteLists || whiteList.contains(cmd[0]))
 		{
 			if (outputInfo)
 				PLUGIN.getLogger().info("Executing redstone console command: " + cmd[1]);
 			PLUGIN.getServer().dispatchCommand(PLUGIN.getServer().getConsoleSender(), cmd[1]);
 		}
 	}
 }
