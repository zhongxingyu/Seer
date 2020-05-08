 package no.runsafe.eventengine.libraries;
 
 import no.runsafe.eventengine.events.CustomEvent;
 import no.runsafe.eventengine.handlers.SeatbeltHandler;
 import no.runsafe.framework.RunsafePlugin;
 import no.runsafe.framework.api.ILocation;
 import no.runsafe.framework.api.IScheduler;
 import no.runsafe.framework.api.lua.*;
 import no.runsafe.framework.api.player.IPlayer;
 import no.runsafe.framework.internal.LegacyMaterial;
 import no.runsafe.framework.minecraft.Buff;
 import no.runsafe.framework.minecraft.Item;
 import no.runsafe.framework.minecraft.inventory.RunsafeInventory;
 import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;
 import no.runsafe.framework.minecraft.player.GameMode;
 import no.runsafe.framework.text.ChatColour;
 import no.runsafe.worldguardbridge.IRegionControl;
 import org.bukkit.util.Vector;
 import org.luaj.vm2.LuaTable;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class PlayerLibrary extends Library
 {
 	public PlayerLibrary(RunsafePlugin plugin, IScheduler scheduler, IRegionControl regionControl)
 	{
 		super(plugin, "player");
 		this.scheduler = scheduler;
 		this.regionControl = regionControl;
 	}
 
 	@Override
 	protected LuaTable getAPI()
 	{
 		LuaTable lib = new LuaTable();
 		lib.set("kill", new VoidFunction()
 		{
 			@Override
 			protected void run(FunctionParameters parameters)
 			{
 				parameters.getPlayer(0).setHealth(0.0D);
 			}
 		});
 
 		lib.set("getLocation", new LocationFunction()
 		{
 			@Override
 			public ILocation run(FunctionParameters parameters)
 			{
 				return parameters.getPlayer(0).getLocation();
 			}
 		});
 		lib.set("isDead", new BooleanFunction()
 		{
 			@Override
 			protected boolean run(FunctionParameters parameters)
 			{
 				return parameters.getPlayer(0).isDead();
 			}
 		});
 
 		lib.set("sendMessage", new VoidFunction()
 		{
 			@Override
 			protected void run(FunctionParameters parameters)
 			{
 				parameters.getPlayer(0).sendColouredMessage(parameters.getString(1));
 			}
 		});
 		lib.set("setHealth", new VoidFunction()
 		{
 			@Override
 			protected void run(FunctionParameters parameters)
 			{
 				parameters.getPlayer(0).setHealth(parameters.getDouble(1));
 			}
 		});
 		lib.set("teleportToLocation", new VoidFunction()
 		{
 			@Override
 			protected void run(FunctionParameters parameters)
 			{
 				parameters.getPlayer(0).teleport(parameters.getLocation(1));
 			}
 		});
 		lib.set("teleportToLocationRotation", new VoidFunction()
 		{
 			@Override
 			protected void run(FunctionParameters parameters)
 			{
 				parameters.getPlayer(0).teleport(parameters.getLocation(1, true));
 			}
 		});
 		lib.set("teleportToPlayer", new VoidFunction()
 		{
 			@Override
 			protected void run(FunctionParameters parameters)
 			{
 				parameters.getPlayer(0).teleport(parameters.getPlayer(1));
 			}
 		});
 		lib.set("cloneInventory", new VoidFunction()
 		{
 			@Override
 			protected void run(FunctionParameters parameters)
 			{
 				CloneInventory(parameters.getPlayer(0), parameters.getPlayer(1));
 			}
 		});
 		lib.set("sendEvent", new VoidFunction()
 		{
 			@Override
 			protected void run(FunctionParameters parameters)
 			{
 				new CustomEvent(parameters.getPlayer(0), parameters.getString(1)).Fire();
 			}
 		});
 		lib.set("clearInventory", new VoidFunction()
 		{
 			@Override
 			protected void run(FunctionParameters parameters)
 			{
 				IPlayer player = parameters.getPlayer(0);
 				player.clearInventory();
 				player.getEquipment().clear();
 			}
 		});
 
 		lib.set("addItem", new VoidFunction()
 		{
 			@Override
 			protected void run(FunctionParameters parameters)
 			{
 				AddItem(parameters.getPlayer(0), parameters.getString(1), parameters.getInt(2));
 			}
 		});
 		lib.set("getPlayerAtLocation", new StringFunction()
 		{
 			@Override
 			public String run(FunctionParameters parameters)
 			{
 				return GetPlayerAtLocation(parameters.getLocation(0));
 			}
 		});
 		lib.set("isOnline", new BooleanFunction()
 		{
 			@Override
 			protected boolean run(FunctionParameters parameters)
 			{
 				return parameters.getPlayer(0).isOnline();
 			}
 		});
 
 		lib.set("hasPermission", new BooleanFunction()
 		{
 			@Override
 			protected boolean run(FunctionParameters parameters)
 			{
 				return parameters.getPlayer(0).hasPermission(parameters.getString(1));
 			}
 		});
 
 		lib.set("addPermission", new VoidFunction()
 		{
 			@Override
 			protected void run(FunctionParameters parameters)
 			{
 				parameters.getPlayer(0).addPermission(parameters.getString(1));
 			}
 		});
 
 		lib.set("addWorldPermission", new VoidFunction()
 		{
 			@Override
 			protected void run(FunctionParameters parameters)
 			{
 				parameters.getPlayer(0).addPermission(parameters.getString(1), parameters.getString(2));
 			}
 		});
 
 		lib.set("removePermission", new VoidFunction()
 		{
 			@Override
 			protected void run(FunctionParameters parameters)
 			{
 				parameters.getPlayer(0).removePermission(parameters.getString(1));
 			}
 		});
 
 		lib.set("removeWorldPermission", new VoidFunction()
 		{
 			@Override
 			protected void run(FunctionParameters parameters)
 			{
 				parameters.getPlayer(0).removePermission(parameters.getString(1), parameters.getString(2));
 			}
 		});
 
 		lib.set("removePotionEffects", new VoidFunction()
 		{
 			@Override
 			protected void run(FunctionParameters parameters)
 			{
 				parameters.getPlayer(0).removeBuffs();
 			}
 		});
 
 		lib.set("addPotionEffect", new VoidFunction()
 		{
 			@Override
 			protected void run(FunctionParameters parameters)
 			{
 				parameters.getPlayer(0).addBuff(Buff.getFromName(parameters.getString(1)).amplification(parameters.getInt(2)).duration(parameters.getInt(3)));
 			}
 		});
 
 		lib.set("closeInventory", new VoidFunction()
 		{
 			@Override
 			protected void run(final FunctionParameters parameters)
 			{
 				scheduler.startSyncTask(new Runnable()
 				{
 					@Override
 					public void run()
 					{
 						parameters.getPlayer(0).closeInventory();
 					}
 				}, 1L);
 			}
 		});
 
 		lib.set("setVelocity", new VoidFunction()
 		{
 			@Override
 			protected void run(final FunctionParameters parameters)
 			{
 				parameters.getPlayer(0).setVelocity(new Vector(
 						parameters.getDouble(1),
 						parameters.getDouble(2),
 						parameters.getDouble(3)
 				));
 			}
 		});
 
 		lib.set("lockMount", new VoidFunction()
 		{
 			@Override
 			protected void run(FunctionParameters parameters)
 			{
 				SeatbeltHandler.lockPlayer(parameters.getPlayer(0));
 			}
 		});
 
 		lib.set("unlockMount", new VoidFunction()
 		{
 			@Override
 			protected void run(FunctionParameters parameters)
 			{
 				SeatbeltHandler.unlockPlayer(parameters.getPlayer(0));
 			}
 		});
 
 		lib.set("dismount", new VoidFunction()
 		{
 			@Override
 			protected void run(FunctionParameters parameters)
 			{
 				parameters.getPlayer(0).leaveVehicle();
 			}
 		});
 
 		lib.set("isInRegion", new BooleanFunction()
 		{
 			@Override
 			protected boolean run(FunctionParameters parameters)
 			{
 				String checkRegion = parameters.getString(1) + '-' + parameters.getString(2);
 				return regionControl.getApplicableRegions(parameters.getPlayer(0)).contains(checkRegion);
 			}
 		});
 
 		lib.set("setMode", new VoidFunction()
 		{
 			@Override
 			protected void run(final FunctionParameters parameters)
 			{
 				GameMode mode = GameMode.search(parameters.getString(1));
 				if (mode != null)
 					mode.apply(parameters.getPlayer(0));
 			}
 		});
 
 		lib.set("removeItem", new VoidFunction()
 		{
 			@Override
 			protected void run(final FunctionParameters parameters)
 			{
 				IPlayer player = parameters.getPlayer(0);
 				player.removeItem(Item.get(parameters.getString(1)), parameters.getInt(2));
 			}
 		});
 
 		lib.set("removeItemByName", new VoidFunction()
 		{
 			@Override
 			protected void run(final FunctionParameters parameters)
 			{
 				IPlayer player = parameters.getPlayer(0);
 				RunsafeInventory inventory = player.getInventory();
 				String itemName = parameters.getString(1);
 				List<Integer> removeItems = new ArrayList<Integer>();
 
 				int curr = 0;
 				while (curr < inventory.getSize())
 				{
 					RunsafeMeta item = inventory.getItemInSlot(curr);
 					if (item != null)
 					{
 						String displayName = item.hasDisplayName() ? item.getDisplayName() : ChatColour.Strip(item.getNormalName());
 						if (itemName.equals(displayName))
 							removeItems.add(curr);
 					}
 					curr++;
 				}
 
 				for (int slot : removeItems)
 					inventory.removeItemInSlot(slot);
 			}
 		});
 
 		lib.set("hasItem", new BooleanFunction()
 		{
 			@Override
 			protected boolean run(FunctionParameters parameters)
 			{
				return parameters.getPlayer(0).hasItemStrict(Item.get(parameters.getString(1)), parameters.getInt(2));
 			}
 		});
 
 		lib.set("hasItemWithName", new BooleanFunction()
 		{
 			@Override
 			protected boolean run(FunctionParameters parameters)
 			{
 				String requiredName = parameters.getString(1);
 				for (RunsafeMeta item : parameters.getPlayer(0).getInventory().getContents())
 				{
 					String displayName = item.getDisplayName();
 					if (displayName != null && displayName.equals(requiredName))
 						return true;
 				}
 				return false;
 			}
 		});
 
 		return lib;
 	}
 
 	private static void CloneInventory(IPlayer source, IPlayer target)
 	{
 		target.getInventory().unserialize(source.getInventory().serialize());
 		target.updateInventory();
 	}
 
 	private static String GetPlayerAtLocation(ILocation location)
 	{
 		for (IPlayer player : location.getWorld().getPlayers())
 			if (player.getLocation().distance(location) < 2)
 				return player.getName();
 		return null;
 	}
 
 	private static void AddItem(IPlayer player,String item, int amount)
 	{
 		RunsafeMeta meta = Item.get(item).getItem();
 		meta.setAmount(amount);
 		player.give(meta);
 	}
 
 	private final IScheduler scheduler;
 	private final IRegionControl regionControl;
 }
