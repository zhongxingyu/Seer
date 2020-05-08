 package com.nuclearw.rapsheet;
 
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.nuclearw.rapsheet.api.NotifyChanges;
 import com.nuclearw.rapsheet.api.RapsheetManager;
 
 public class SimpleRapsheetManager implements RapsheetManager {
 	private Rapsheet plugin;
 
 	public SimpleRapsheetManager(Rapsheet plugin) {
 		this.plugin = plugin;
 	}
 
 	@Override
 	public List<Record> getCharges(String playerName) {
 		return plugin.getDatabase().find(Record.class).where().ieq("offender", playerName).findList();
 	}
 
 	@Override
 	public Record getCharge(String playerName, int chargeId) {
 		return plugin.getDatabase().find(Record.class).where().ieq("offender", playerName).eq("charge_id", chargeId).findUnique();
 	}
 
 	@Override
	public int chargePlayer(String offenderName, String officialName, String shortDesciption, String longDescription, NotifyChanges notify) {
 		int newChargeId = 1;
 
 		// I do not like the way this is done
 		List<Record> found = getCharges(offenderName);
 
 		if(found != null && !found.isEmpty()) {
 			newChargeId = found.get(found.size() - 1).getChargeId() + 1;
 		}
 
 		Record record = new Record();
 		record.setChargeId(newChargeId);
 		record.setOffender(offenderName);
 		record.setOfficial(officialName);
		record.setChargeShort(shortDesciption);
 		record.setChargeLong(longDescription);
 		record.setTime(System.currentTimeMillis());
 		record.setSealed(false);
 		record.setState(RecordState.CHARGED);
 
 		plugin.getDatabase().save(record);
 
 		if(notify == NotifyChanges.NONE) {
 			return newChargeId;
 		}
 
 		CommandSender sender = null;
 		// Hard code console check?
 		if(officialName.equals("CONSOLE")) {
 			sender = plugin.getServer().getConsoleSender();
 		}
 
 		Player player = plugin.getServer().getPlayer(offenderName);
 
 		if(notify == NotifyChanges.BOTH || notify == NotifyChanges.OFFICIAL) {
 			if(player == null && sender == null) {
 				plugin.getLogger().warning("Could not notify official " + officialName + " of charge!");
 			} else {
 				if(player != null && sender == null) {
 					sender = (CommandSender) player;
 				}
 
 				sender.sendMessage(ChatColor.GOLD + "Charged" + ChatColor.RESET + ": " + ChatColor.AQUA + offenderName);
				sender.sendMessage(ChatColor.GOLD + "Charge " + ChatColor.RESET + "#" + newChargeId + ChatColor.GOLD + " - " + ChatColor.AQUA + shortDesciption);
 				sender.sendMessage(ChatColor.GOLD + "Report" + ChatColor.RESET + ": " + ChatColor.GRAY + longDescription);
 			}
 		}
 
 		if(notify == NotifyChanges.BOTH || notify == NotifyChanges.OFFENDER) {
 			if(player == null) {
 				plugin.getLogger().warning("Could not notify offender " + offenderName + " of charge!");
 			} else {
				player.sendMessage(ChatColor.GOLD + "Charged by " + ChatColor.AQUA + sender.getName() + ChatColor.GOLD + " of " + ChatColor.GRAY + shortDesciption);
 				player.sendMessage(ChatColor.GOLD + "Filed under Charge " + ChatColor.RESET + "#" + newChargeId);
 			}
 		}
 
 		return newChargeId;
 	}
 
 	@Override
 	public boolean convictPlayer(String offenderName, String officialName, int chargeId, NotifyChanges notify) {
 		Record found = getCharge(offenderName, chargeId);
 
 		if(found == null) return false;
 
 		found.setState(RecordState.CONVICTED);
 
 		plugin.getDatabase().update(found);
 
 		if(notify == NotifyChanges.NONE) {
 			return true;
 		}
 
 		CommandSender sender = null;
 		// Hard code console check?
 		if(officialName.equals("CONSOLE")) {
 			sender = plugin.getServer().getConsoleSender();
 		}
 
 		Player player = plugin.getServer().getPlayer(offenderName);
 
 		if(notify == NotifyChanges.BOTH || notify == NotifyChanges.OFFICIAL) {
 			if(player == null && sender == null) {
 				plugin.getLogger().warning("Could not notify official " + officialName + " of charge!");
 			} else {
 				if(player != null && sender == null) {
 					sender = (CommandSender) player;
 				}
 
 				sender.sendMessage(ChatColor.DARK_RED + "Convicted" + ChatColor.RESET + ": " + ChatColor.AQUA + found.getOffender());
 				sender.sendMessage(ChatColor.GOLD + "Charge " + ChatColor.RESET + "#" + found.getChargeId() + ChatColor.GOLD + " - " + ChatColor.AQUA + found.getChargeShort());
 				sender.sendMessage(ChatColor.GOLD + "Report" + ChatColor.RESET + ": " + ChatColor.GRAY + found.getChargeLong());
 			}
 		}
 
 		if(notify == NotifyChanges.BOTH || notify == NotifyChanges.OFFENDER) {
 			if(player == null) {
 				plugin.getLogger().warning("Could not notify offender " + offenderName + " of charge!");
 			} else {
 				player.sendMessage(ChatColor.DARK_RED + "Convicted" + ChatColor.GOLD + " by " + ChatColor.AQUA + sender.getName() + ChatColor.GOLD + " of " + ChatColor.GRAY + found.getChargeShort());
 				player.sendMessage(ChatColor.GOLD + "Filed under Charge " + ChatColor.RESET + "#" + found.getChargeId());
 			}
 		}
 
 		return true;
 	}
 
 	@Override
 	public boolean pardonPlayer(String offenderName, String officialName, int chargeId, NotifyChanges notify) {
 		Record found = getCharge(offenderName, chargeId);
 
 		if(found == null) return false;
 
 		found.setState(RecordState.PARDONED);
 
 		plugin.getDatabase().update(found);
 
 		if(notify == NotifyChanges.NONE) {
 			return true;
 		}
 
 		CommandSender sender = null;
 		// Hard code console check?
 		if(officialName.equals("CONSOLE")) {
 			sender = plugin.getServer().getConsoleSender();
 		}
 
 		Player player = plugin.getServer().getPlayer(offenderName);
 
 		if(notify == NotifyChanges.BOTH || notify == NotifyChanges.OFFICIAL) {
 			if(player == null && sender == null) {
 				plugin.getLogger().warning("Could not notify official " + officialName + " of charge!");
 			} else {
 				if(player != null && sender == null) {
 					sender = (CommandSender) player;
 				}
 
 				sender.sendMessage(ChatColor.LIGHT_PURPLE + "Pardoned" + ChatColor.RESET + ": " + ChatColor.AQUA + found.getOffender());
 				sender.sendMessage(ChatColor.GOLD + "Charge " + ChatColor.RESET + "#" + found.getChargeId() + ChatColor.GOLD + " - " + ChatColor.AQUA + found.getChargeShort());
 				sender.sendMessage(ChatColor.GOLD + "Report" + ChatColor.RESET + ": " + ChatColor.GRAY + found.getChargeLong());
 			}
 		}
 
 		if(notify == NotifyChanges.BOTH || notify == NotifyChanges.OFFENDER) {
 			if(player == null) {
 				plugin.getLogger().warning("Could not notify offender " + offenderName + " of charge!");
 			} else {
 				player.sendMessage(ChatColor.LIGHT_PURPLE + "Pardoned" + ChatColor.GOLD + " by " + ChatColor.AQUA + sender.getName() + ChatColor.GOLD + " of " + ChatColor.GRAY + found.getChargeShort());
 				player.sendMessage(ChatColor.GOLD + "Filed under Charge " + ChatColor.RESET + "#" + found.getChargeId());
 			}
 		}
 
 		return true;
 	}
 
 	@Override
 	public boolean sealPlayerCharge(String offenderName, String officialName, int chargeId, NotifyChanges notify) {
 		Record found = getCharge(offenderName, chargeId);
 
 		if(found == null) return false;
 
 		found.setSealed(true);
 
 		plugin.getDatabase().update(found);
 
 		if(notify == NotifyChanges.NONE) {
 			return true;
 		}
 
 		CommandSender sender = null;
 		// Hard code console check?
 		if(officialName.equals("CONSOLE")) {
 			sender = plugin.getServer().getConsoleSender();
 		}
 
 		Player player = plugin.getServer().getPlayer(offenderName);
 
 		if(notify == NotifyChanges.BOTH || notify == NotifyChanges.OFFICIAL) {
 			if(player == null && sender == null) {
 				plugin.getLogger().warning("Could not notify official " + officialName + " of charge!");
 			} else {
 				if(player != null && sender == null) {
 					sender = (CommandSender) player;
 				}
 
 				sender.sendMessage(ChatColor.GRAY + "Sealed " + ChatColor.GOLD + "charge " + ChatColor.RESET + "#" + found.getChargeId() + ChatColor.GOLD + " - " + ChatColor.AQUA + found.getChargeShort());
 			}
 		}
 
 		if(notify == NotifyChanges.BOTH || notify == NotifyChanges.OFFENDER) {
 			if(player == null) {
 				plugin.getLogger().warning("Could not notify offender " + offenderName + " of charge!");
 			} else {
 				player.sendMessage(ChatColor.GOLD + "Charge " + ChatColor.RESET + "#" + found.getChargeId() + ChatColor.GOLD + " has been " + ChatColor.GRAY + "sealed" + ChatColor.GOLD + " by " + ChatColor.AQUA + sender.getName());
 			}
 		}
 
 		return true;
 	}
 
 	@Override
 	public boolean unsealPlayerCharge(String offenderName, String officialName, int chargeId, NotifyChanges notify) {
 		Record found = getCharge(offenderName, chargeId);
 
 		if(found == null) return false;
 
 		found.setSealed(false);
 
 		plugin.getDatabase().update(found);
 
 		if(notify == NotifyChanges.NONE) {
 			return true;
 		}
 
 		CommandSender sender = null;
 		// Hard code console check?
 		if(officialName.equals("CONSOLE")) {
 			sender = plugin.getServer().getConsoleSender();
 		}
 
 		Player player = plugin.getServer().getPlayer(offenderName);
 
 		if(notify == NotifyChanges.BOTH || notify == NotifyChanges.OFFICIAL) {
 			if(player == null && sender == null) {
 				plugin.getLogger().warning("Could not notify official " + officialName + " of charge!");
 			} else {
 				if(player != null && sender == null) {
 					sender = (CommandSender) player;
 				}
 
 				sender.sendMessage(ChatColor.GRAY + "Unsealed " + ChatColor.GOLD + "charge " + ChatColor.RESET + "#" + found.getChargeId() + ChatColor.GOLD + " - " + ChatColor.AQUA + found.getChargeShort());
 			}
 		}
 
 		if(notify == NotifyChanges.BOTH || notify == NotifyChanges.OFFENDER) {
 			if(player == null) {
 				plugin.getLogger().warning("Could not notify offender " + offenderName + " of charge!");
 			} else {
 				player.sendMessage(ChatColor.GOLD + "Charge " + ChatColor.RESET + "#" + found.getChargeId() + ChatColor.GOLD + " has been " + ChatColor.GRAY + "unsealed" + ChatColor.GOLD + " by " + ChatColor.AQUA + sender.getName());
 			}
 		}
 
 		return true;
 	}
 
 	@Override
 	public boolean expungePlayerCharge(String offenderName, String officialName, int chargeId, NotifyChanges notify) {
 		Record found = getCharge(offenderName, chargeId);
 
 		if(found == null) return false;
 
 		plugin.getDatabase().delete(found);
 
 		if(notify == NotifyChanges.NONE) {
 			return true;
 		}
 
 		CommandSender sender = null;
 		// Hard code console check?
 		if(officialName.equals("CONSOLE")) {
 			sender = plugin.getServer().getConsoleSender();
 		}
 
 		Player player = plugin.getServer().getPlayer(offenderName);
 
 		if(notify == NotifyChanges.BOTH || notify == NotifyChanges.OFFICIAL) {
 			if(player == null && sender == null) {
 				plugin.getLogger().warning("Could not notify official " + officialName + " of charge!");
 			} else {
 				if(player != null && sender == null) {
 					sender = (CommandSender) player;
 				}
 
 				sender.sendMessage(ChatColor.GRAY + "Expunged " + ChatColor.GOLD + "charge " + ChatColor.RESET + "#" + found.getChargeId() + ChatColor.GOLD + " - " + ChatColor.AQUA + found.getChargeShort());
 			}
 		}
 
 		if(notify == NotifyChanges.BOTH || notify == NotifyChanges.OFFENDER) {
 			if(player == null) {
 				plugin.getLogger().warning("Could not notify offender " + offenderName + " of charge!");
 			} else {
 				player.sendMessage(ChatColor.GOLD + "Charge " + ChatColor.RESET + "#" + found.getChargeId() + ChatColor.GOLD + " has been " + ChatColor.GRAY + "expunged" + ChatColor.GOLD + " by " + ChatColor.AQUA + sender.getName());
 			}
 		}
 
 		return true;
 	}
 }
