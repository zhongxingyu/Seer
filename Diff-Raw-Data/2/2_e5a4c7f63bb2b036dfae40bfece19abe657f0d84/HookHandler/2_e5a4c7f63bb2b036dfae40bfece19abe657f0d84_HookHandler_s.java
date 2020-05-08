 package no.runsafe.eventengine.engine.hooks;
 
 import no.runsafe.framework.api.ILocation;
 import no.runsafe.framework.api.IScheduler;
 import no.runsafe.framework.api.IWorld;
 import no.runsafe.framework.api.block.IBlock;
 import no.runsafe.framework.api.event.block.IBlockBreak;
 import no.runsafe.framework.api.event.block.IBlockRedstone;
 import no.runsafe.framework.api.event.player.*;
 import no.runsafe.framework.api.log.IDebug;
 import no.runsafe.framework.api.player.IPlayer;
 import no.runsafe.framework.internal.extension.block.RunsafeBlock;
 import no.runsafe.framework.minecraft.Item;
 import no.runsafe.framework.minecraft.event.block.RunsafeBlockRedstoneEvent;
 import no.runsafe.framework.minecraft.event.player.*;
 import org.luaj.vm2.LuaTable;
 import org.luaj.vm2.LuaValue;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class HookHandler implements IPlayerChatEvent, IPlayerCustomEvent, IPlayerJoinEvent, IPlayerQuitEvent, IPlayerInteractEvent, IBlockRedstone, IBlockBreak, IPlayerLeftClickBlockEvent
 {
 	public HookHandler(IScheduler scheduler, IDebug debug)
 	{
 		this.scheduler = scheduler;
 		this.debug = debug;
 	}
 
 	public static void registerHook(Hook hook)
 	{
 		HookType type = hook.getType();
 		if (!HookHandler.hooks.containsKey(type))
 			HookHandler.hooks.put(type, new ArrayList<Hook>());
 
 		HookHandler.hooks.get(type).add(hook);
 	}
 
 	private static List<Hook> getHooks(HookType type)
 	{
 		if (HookHandler.hooks.containsKey(type))
 			return HookHandler.hooks.get(type);
 
 		return null;
 	}
 
 	public static void clearHooks()
 	{
 		HookHandler.hooks.clear();
 	}
 
 	@Override
 	public void OnPlayerChatEvent(RunsafePlayerChatEvent event)
 	{
 		List<Hook> hooks = HookHandler.getHooks(HookType.CHAT_MESSAGE);
 
 		if (hooks != null)
 		{
 			for (Hook hook : hooks)
 			{
 				if (event.getMessage().equalsIgnoreCase((String) hook.getData()))
 				{
 					LuaTable table = new LuaTable();
 					table.set("player", LuaValue.valueOf(event.getPlayer().getName()));
 					hook.execute(table);
 				}
 			}
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void OnPlayerCustomEvent(RunsafeCustomEvent event)
 	{
 		HookType type = null;
 		String eventType = event.getEvent();
 
 		if (eventType.equals("region.enter"))
 			type = HookType.REGION_ENTER;
 		else if (eventType.equals("region.leave"))
 			type = HookType.REGION_LEAVE;
 
 		if (type != null)
 		{
 			List<Hook> hooks = HookHandler.getHooks(type);
 
 			if (hooks != null)
 			{
 				for (final Hook hook : hooks)
 				{
 					Map<String, String> data = (Map<String, String>) event.getData();
 					if (((String) hook.getData()).equalsIgnoreCase(String.format("%s-%s", data.get("world"), data.get("region"))))
 					{
 						final LuaTable table = new LuaTable();
 						table.set("player", LuaValue.valueOf(event.getPlayer().getName()));
 
 						scheduler.runNow(new Runnable()
 						{
 							@Override
 							public void run()
 							{
 								hook.execute(table);
 							}
 						});
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public void OnPlayerJoinEvent(RunsafePlayerJoinEvent event)
 	{
 		this.playerLogEvent(event.getPlayer(), HookType.PLAYER_LOGIN);
 	}
 
 	@Override
 	public void OnPlayerQuit(RunsafePlayerQuitEvent event)
 	{
 		this.playerLogEvent(event.getPlayer(), HookType.PLAYER_LOGOUT);
 	}
 
 	private void playerLogEvent(IPlayer player, HookType type)
 	{
 		List<Hook> hooks = HookHandler.getHooks(type);
 
 		if (hooks != null)
 			for (Hook hook : hooks)
 				if (hook.getPlayerName().equalsIgnoreCase(player.getName()))
 					hook.execute();
 	}
 
 	@Override
 	public void OnPlayerInteractEvent(RunsafePlayerInteractEvent event)
 	{
 		debug.debugFine("Interact event detected");
 		List<Hook> hooks = HookHandler.getHooks(HookType.INTERACT);
 
 		if (hooks != null)
 		{
 			debug.debugFine("Hooks not null");
 			for (Hook hook : hooks)
 			{
 				debug.debugFine("Processing hook...");
 				IBlock block = event.getBlock();
 				if (hook.getData() != null)
 					if (block == null || block.getMaterial().getItemID() != (Integer) hook.getData())
						return;
 
 				debug.debugFine("Block is not null");
 
 				IWorld hookWorld = hook.getWorld();
 				ILocation location = block.getLocation();
 				if (hookWorld == null)
 				{
 					debug.debugFine("Hook world is null, using location");
 					if (location.getWorld().getName().equals(hook.getLocation().getWorld().getName()))
 					{
 						debug.debugFine("Correct world!");
 						if (location.distance(hook.getLocation()) < 1)
 						{
 							debug.debugFine("Distance is less than 1");
 							LuaTable table = new LuaTable();
 							if (event.getPlayer() != null)
 								table.set("player", LuaValue.valueOf(event.getPlayer().getName()));
 
 							table.set("x", LuaValue.valueOf(location.getBlockX()));
 							table.set("y", LuaValue.valueOf(location.getBlockY()));
 							table.set("z", LuaValue.valueOf(location.getBlockZ()));
 							table.set("blockID", LuaValue.valueOf(block.getMaterial().getItemID()));
 							table.set("blockData", LuaValue.valueOf((block).getData()));
 
 							hook.execute(table);
 						}
 					}
 				}
 				else if (hookWorld.getName().equals(block.getWorld().getName()))
 				{
 					debug.debugFine("Hook world is null, sending location data");
 					LuaTable table = new LuaTable();
 					if (event.getPlayer() != null)
 						table.set("player", LuaValue.valueOf(event.getPlayer().getName()));
 
 					table.set("x", LuaValue.valueOf(location.getBlockX()));
 					table.set("y", LuaValue.valueOf(location.getBlockY()));
 					table.set("z", LuaValue.valueOf(location.getBlockZ()));
 					table.set("blockID", LuaValue.valueOf(block.getMaterial().getItemID()));
 					table.set("blockData", LuaValue.valueOf((block).getData()));
 
 					hook.execute(table);
 				}
 			}
 		}
 	}
 
 	@Override
 	public void OnBlockRedstoneEvent(RunsafeBlockRedstoneEvent event)
 	{
 		if (event.getNewCurrent() > 0 && event.getOldCurrent() == 0)
 		{
 			List<Hook> hooks = HookHandler.getHooks(HookType.BLOCK_GAINS_CURRENT);
 
 			if (hooks != null)
 			{
 				for (Hook hook : hooks)
 				{
 					IBlock block = event.getBlock();
 					if (block != null)
 					{
 						ILocation location = block.getLocation();
 						if (location.getWorld().getName().equals(hook.getLocation().getWorld().getName()))
 							if (location.distance(hook.getLocation()) < 1)
 								hook.execute();
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public boolean OnBlockBreak(IPlayer player, IBlock block)
 	{
 		List<Hook> hooks = HookHandler.getHooks(HookType.BLOCK_BREAK);
 
 		if (hooks != null)
 		{
 			ILocation blockLocation = block.getLocation();
 			String blockWorld = blockLocation.getWorld().getName();
 			for (Hook hook : hooks)
 			{
 				IWorld world = hook.getWorld();
 				if (world != null && !blockWorld.equals(world.getName()))
 					return true;
 
 				LuaTable table = new LuaTable();
 				if (player != null)
 					table.set("player", LuaValue.valueOf(player.getName()));
 
 				table.set("world", LuaValue.valueOf(blockWorld));
 				table.set("x", LuaValue.valueOf(blockLocation.getBlockX()));
 				table.set("y", LuaValue.valueOf(blockLocation.getBlockY()));
 				table.set("z", LuaValue.valueOf(blockLocation.getBlockZ()));
 				table.set("blockID", LuaValue.valueOf(block.getMaterial().getItemID()));
 				table.set("blockData", LuaValue.valueOf(((RunsafeBlock) block).getData()));
 
 				hook.execute(table);
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public void OnPlayerLeftClick(RunsafePlayerClickEvent event)
 	{
 		List<Hook> hooks = HookHandler.getHooks(HookType.LEFT_CLICK_BLOCK);
 
 		if (hooks != null)
 		{
 			IBlock block = event.getBlock();
 			Item material = block.getMaterial();
 			ILocation blockLocation = block.getLocation();
 			String blockWorldName = blockLocation.getWorld().getName();
 			String playerName = event.getPlayer().getName();
 			for (Hook hook : hooks)
 			{
 				IWorld world = hook.getWorld();
 				if (world != null && !blockWorldName.equals(world.getName()))
 					return;
 
 				LuaTable table = new LuaTable();
 				table.set("player", LuaValue.valueOf(playerName));
 				table.set("world", LuaValue.valueOf(blockWorldName));
 				table.set("x", LuaValue.valueOf(blockLocation.getBlockX()));
 				table.set("y", LuaValue.valueOf(blockLocation.getBlockY()));
 				table.set("z", LuaValue.valueOf(blockLocation.getBlockZ()));
 				table.set("blockID", LuaValue.valueOf(material.getItemID()));
 				table.set("blockData", LuaValue.valueOf(material.getData()));
 
 				hook.execute(table);
 			}
 		}
 	}
 
 	private static final HashMap<HookType, List<Hook>> hooks = new HashMap<HookType, List<Hook>>();
 	private final IScheduler scheduler;
 	private final IDebug debug;
 }
