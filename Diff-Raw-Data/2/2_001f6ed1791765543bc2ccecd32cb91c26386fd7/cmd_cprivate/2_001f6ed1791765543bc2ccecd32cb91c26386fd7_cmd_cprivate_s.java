 package de.minestar.moneypit.commands;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 import de.minestar.minestarlibrary.commands.AbstractCommand;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 import de.minestar.moneypit.MoneyPitCore;
 import de.minestar.moneypit.data.PlayerState;
 
 public class cmd_cprivate extends AbstractCommand {
 
     public cmd_cprivate(String syntax, String arguments, String node) {
         super(MoneyPitCore.NAME, syntax, arguments, node);
         this.description = "Create a private protection.";
     }
 
     public void execute(String[] args, Player player) {
         MoneyPitCore.playerManager.setState(player.getName(), PlayerState.PROTECTION_ADD_PRIVATE);
        PlayerUtils.sendMessage(player, ChatColor.DARK_AQUA, MoneyPitCore.NAME, "Click on a block to protect it with a public protection!");
     }
 }
