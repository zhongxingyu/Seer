 package com.gravypod.alladmin;
 
 import com.gravypod.alladmin.commands.BroadcastCommand;
 import com.gravypod.alladmin.commands.BurnCommand;
 import com.gravypod.alladmin.commands.ChestCommand;
 import com.gravypod.alladmin.commands.ClearInventoryCommand;
 import com.gravypod.alladmin.commands.DelWarpCommand;
 import com.gravypod.alladmin.commands.EnchantTableCommand;
 import com.gravypod.alladmin.commands.FeedCommand;
 import com.gravypod.alladmin.commands.FlyCommand;
 import com.gravypod.alladmin.commands.GetPosCommand;
 import com.gravypod.alladmin.commands.GodCommand;
 import com.gravypod.alladmin.commands.HealCommand;
 import com.gravypod.alladmin.commands.HomeCommand;
 import com.gravypod.alladmin.commands.InvseeCommand;
 import com.gravypod.alladmin.commands.MuteCommand;
 import com.gravypod.alladmin.commands.PermissionCommand;
 import com.gravypod.alladmin.commands.RepairCommand;
 import com.gravypod.alladmin.commands.SetHomeCommand;
 import com.gravypod.alladmin.commands.SetSpawnCommand;
 import com.gravypod.alladmin.commands.SetWarpCommand;
 import com.gravypod.alladmin.commands.SpawnCommand;
 import com.gravypod.alladmin.commands.StopLagCommand;
 import com.gravypod.alladmin.commands.TpPosCommand;
 import com.gravypod.alladmin.commands.WarpCommand;
 import com.gravypod.alladmin.commands.WorkbenchCommand;
 
 import net.minecraft.command.CommandBase;
 import static com.gravypod.alladmin.permissions.PermissionManager.CommandPermissions.*;
 
 public enum AllAdminCommandRegistry {
 	
 	HEAL_COMMAND(new HealCommand(HEAL, "heal")),
 	HOME_COMMAND(new HomeCommand(HOME, "home")),
 	SET_HOME_COMMAND(new SetHomeCommand(SET_HOME, "sethome", "shome", "sho")),
 	PERMISSION_COMMAND(new PermissionCommand(PERMISSION, "permission")),
 	CHEST_COMMAND(new ChestCommand(CHEST, "chest", "c", "ec")),
 	WORKBENCH_COMMAND(new WorkbenchCommand(WORKBENCH, "workbench")),
 	FEED_COMMAND(new FeedCommand(FEED, "feed")),
 	ENCHANT_TABLE_COMMAND(new EnchantTableCommand(ENCHANT_TABLE, "enchanttable")),
 	MUTE_COMMAND(new MuteCommand(MUTE, "mute")),
 	STOP_LAG_COMMAND(new StopLagCommand(STOP_LAG, "stoplag")),
 	FLY_COMMAND(new FlyCommand(FLY, "fly")),
 	TP_POS_COMMAND(new TpPosCommand(TP, "tppos")), 
 	WARP_COMMAND(new WarpCommand(SET_WARP, "warp")),
	DEL_WARP_COMMAND(new DelWarpCommand(SET_WARP, "warp")),
 	SET_WARP_COMMAND(new SetWarpCommand(SET_WARP, "setwarp")),
 	SPAWN_COMMAND(new SpawnCommand(SPAWN, "spawn")),
 	SET_SPAWN_COMMAND(new SetSpawnCommand(SET_SPAWN, "setspawn")),
 	INVSEE_COMMAND(new InvseeCommand(INVSEE, "invsee")),
 	CLEAR_INVENTORY_COMMAND(new ClearInventoryCommand(CLEAR_INVENTORY, "clearinventory", "ci")),
 	GET_POS_COMMAND(new GetPosCommand(GET_POS, "getpos", "gs", "gps")),
 	BROADCAST_COMMAND(new BroadcastCommand(BROADCAST, "broadcast")),
 	REPAIR_COMMAND(new RepairCommand(REPAIR, "repair")),
 	GOD_COMMAND(new GodCommand(GAME_MODE, "god")),
 	BURN_COMMAND(new BurnCommand(BURN, "burn"));
 	
 	private final CommandBase command;
 
 	private AllAdminCommandRegistry(CommandBase command) {
 		this.command = command;
 	}
 
 	public CommandBase getCommand() {
 		return command;
 	}
 }
