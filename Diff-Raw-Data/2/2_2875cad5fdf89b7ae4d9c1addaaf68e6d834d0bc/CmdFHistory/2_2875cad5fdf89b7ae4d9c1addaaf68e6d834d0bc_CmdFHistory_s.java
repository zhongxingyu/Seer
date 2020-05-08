 package net.t7seven7t.swornguard.commands;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.t7seven7t.swornguard.SwornGuard;
 import net.t7seven7t.swornguard.permissions.PermissionType;
 import net.t7seven7t.swornguard.types.PlayerData;
 import net.t7seven7t.util.FormatUtil;
 
 import org.bukkit.OfflinePlayer;
 
 /**
  * @author dmulloy2
  */
 
 public class CmdFHistory extends PaginatedCommand {
 	private OfflinePlayer target = null;
 	private List<String> factionHistory = null;
 	
 	public CmdFHistory(SwornGuard plugin) {
 		super(plugin);
 		this.name = "fhistory";
 		this.aliases.add("fh");
 		this.description = "show a player's faction history";
 		this.permission = PermissionType.CMD_SHOW.permission;
 		this.optionalArgs.add("player");
 		this.optionalArgs.add("page");
 		this.pageArgIndex = 1;
 		this.usesPrefix = true;
 	}
 	
 	@Override
 	public void perform() {
		OfflinePlayer target = getTarget(0);
 		if (target == null)
 			return;
 		
 		PlayerData data = getPlayerData(target);
 
 		if (data.getFactionLog() != null) {
 			factionHistory = new ArrayList<String>();
 			for (int i = data.getFactionLog().size() - 1; i >= 0; i--) {
 				factionHistory.add(data.getFactionLog().get(i));
 			}
 			
 			super.perform();
 		} else {
 			err(plugin.getMessage("fhistory_no_data"), target.getName());
 		}
 	}
 
 	@Override
 	public int getListSize() {
 		return factionHistory.size();
 	}
 
 	@Override
 	public String getHeader(int index) {
 		return FormatUtil.format(plugin.getMessage("fhistory_header"), target.getName(), index, getPageCount());
 	}
 
 	@Override
 	public String getLine(int index) {
 		return FormatUtil.format(factionHistory.get(index));
 	}
 	
 }
