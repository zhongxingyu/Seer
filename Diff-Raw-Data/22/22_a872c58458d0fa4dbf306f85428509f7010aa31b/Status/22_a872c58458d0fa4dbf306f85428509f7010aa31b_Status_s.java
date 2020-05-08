 package ee.lutsu.alpha.mc.aperf.sys.cmd;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.bukkit.ChatColor;
 
 import com.google.common.base.Joiner;
 import com.google.common.collect.ImmutableSetMultimap;
 
 import net.minecraft.command.ICommandSender;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.ChunkCoordIntPair;
 import net.minecraft.world.WorldServer;
 import net.minecraftforge.common.ForgeChunkManager;
 import net.minecraftforge.common.ForgeChunkManager.Ticket;
 
 import ee.lutsu.alpha.mc.aperf.commands.BaseCommand;
 import ee.lutsu.alpha.mc.aperf.commands.Command;
 
 public class Status extends BaseCommand 
 {
 	@Command(
 		name = "aperf",
 		syntax = "(?:status|s)",
 		description = "Shows the general server status",
 		isPrimary = true,
 		permission = "aperf.cmd.status"
 	)
 	public void status(Object plugin, ICommandSender sender, Map<String, String> args) 
 	{
 		String format = "%s%3s | %s%5s%s | %7s | %8s | %5s | %6s | %6s";
 		
 		msg(sender, format, ChatColor.DARK_GREEN, " #", "", "Tick", "", "Players", "Entities", "Tiles", "Chunks", "Forced");
 		msg(sender, "%s-----------------------------------------------------", ChatColor.GRAY);
 		
 		double norm_tick_ms = 50; // 20 tps
 		for (WorldServer w : MinecraftServer.getServer().worldServers)
 		{
 			int dim = w.provider.dimensionId;
 			long[] ticks = MinecraftServer.getServer().worldTickTimes.get(dim);
 			double average_ms = MathHelper.average(ticks) * 1.0E-6;
 			int prc_tick = (int)(norm_tick_ms * 100 / average_ms);
 			
 			ImmutableSetMultimap<ChunkCoordIntPair, Ticket> forced = w.getPersistentChunks();
 
 			msg(sender, format, ChatColor.GREEN,
 					dim,
 					prc_tick >= 100 ? ChatColor.DARK_GREEN : prc_tick >= 50 ? ChatColor.GOLD : ChatColor.RED,
 					(prc_tick > 999 ? ">" : "") + String.valueOf(prc_tick > 999 ? 999 : prc_tick) + "%", ChatColor.GREEN,
 					w.playerEntities.size(),
 					w.loadedEntityList.size(),
 					w.loadedTileEntityList.size(),
 					w.getChunkProvider().getLoadedChunkCount(),
 					forced.keySet().size()
 					);
 		}
 		
 		msg(sender, "%s-----------------------------------------------------", ChatColor.GRAY);
 	}
 	
 	@Command(
 			name = "aperf",
 			syntax = "(?:status|s) (?:forcedchunks|fc)",
 			description = "Shows the forced chunks",
 			permission = "aperf.cmd.status.forced"
 	)
 	public void forced(Object plugin, ICommandSender sender, Map<String, String> args) throws Exception
 	{
 		Field compoundField = NBTTagCompound.class.getDeclaredFields()[0];
 		compoundField.setAccessible(true);
 		
 		msg(sender, "%s%s", ChatColor.DARK_GREEN, "Forge forced (chunkloaded) chunks");
 		msg(sender, "%s  Type, Mod id, Player, Entity, Mod data, Used of Total chunks", ChatColor.GREEN);
 		msg(sender, "%s----------------------------------", ChatColor.GRAY);
 		
 		for (WorldServer serv : MinecraftServer.getServer().worldServers)
 		{
 			ImmutableSetMultimap<ChunkCoordIntPair, Ticket> forced = serv.getPersistentChunks();
 			if (forced.size() < 1)
 				continue;
 			
 			List<Ticket> tickets = new ArrayList<Ticket>();
 			for (Ticket ticket : forced.values())
 				if (!tickets.contains(ticket))
 					tickets.add(ticket);
 			
 			msg(sender, "%s%s [%d], %s tickets", ChatColor.GREEN, serv.provider.getDimensionName(),
 					serv.provider.dimensionId, tickets.size());
 
 			for (Ticket ticket : tickets)
 			{
 				Map values = (Map)compoundField.get(ticket.getModData());
 				ArrayList<String> vals = new ArrayList<String>();
 				for (Object ov : values.entrySet())
 				{
 					Entry<Object, Object> v = (Entry<Object, Object>)ov;
 					vals.add(String.format("%s:%s", v.getKey(), v.getValue()));
 				}
 				
 				msg(sender, "%s  %s, %s, %s, %s, \"%s\" - %s of %s chunks", ChatColor.GOLD, 
 						ticket.getType(), ticket.getModId(), ticket.getPlayerName(), ticket.getEntity(), Joiner.on("|").join(vals), 
 						ticket.getChunkList().size(), ticket.getChunkListDepth());
 				
 				for (Object pos : ticket.getChunkList())
 				{
 					ChunkCoordIntPair p = (ChunkCoordIntPair)pos;
					
 					msg(sender, "%s    %s,%s [%s,%s]", ChatColor.YELLOW, 
							p.chunkXPos, p.chunkZPos, p.getCenterXPos(), p.getCenterZPosition());
 				}
 			}
 		}
 		
 		msg(sender, "%s----------------------------------", ChatColor.GRAY);
 	}
 }
