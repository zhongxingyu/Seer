 package com.simplyian.superplots.actions;
 
import java.text.NumberFormat;
 import java.util.List;
 
 import org.bukkit.entity.Player;
 
 import com.google.common.base.Joiner;
 import com.simplyian.superplots.MsgColor;
 import com.simplyian.superplots.SuperPlotsPlugin;
 import com.simplyian.superplots.plot.Plot;
 
 public class ActionInfo extends BaseAction {
     public ActionInfo(SuperPlotsPlugin main) {
         super(main);
     }
 
     @Override
     public void perform(Player player, List<String> args) {
         Plot plot = main.getPlotManager().getPlotAt(player.getLocation());
         if (plot == null) {
             player.sendMessage(MsgColor.ERROR + "You are not in a plot.");
             return;
         }
 
         // Normal messages
         player.sendMessage(MsgColor.INFO + "======== Info for "
                 + MsgColor.INFO_HILIGHT + plot.getName() + MsgColor.INFO
                 + " ========");
 
         player.sendMessage(MsgColor.INFO_HILIGHT + "Size: " + MsgColor.INFO
                 + plot.getSize() + MsgColor.INFO_HILIGHT + "       Owner: "
                 + MsgColor.INFO + plot.getOwner());
 
         player.sendMessage(MsgColor.INFO_HILIGHT + "Protected: "
                 + MsgColor.INFO + (plot.isProtected() ? "yes" : "no")
                 + MsgColor.INFO_HILIGHT + "       Privacy: "
                 + (plot.isPrivate() ? "private" : "public"));
 
         if (!plot.isAdministrator(player.getName())) {
             return;
         }
 
         player.sendMessage(MsgColor.INFO + "=== Admin Info ===");
 
         player.sendMessage(MsgColor.INFO_HILIGHT + "Daily Tax: "
                 + MsgColor.INFO + plot.dailyTax() + "       "
                 + MsgColor.INFO_HILIGHT + "Funds: " + MsgColor.INFO + "D"
                 + plot.getFunds());
 
         player.sendMessage(MsgColor.INFO_HILIGHT + "Days of tax left: "
                 + MsgColor.INFO + plot.taxDaysLeft());
 
         player.sendMessage(MsgColor.INFO_HILIGHT + "Distance from center: "
                 + MsgColor.INFO + plot.distancef(player.getLocation()) + " blocks");
 
         player.sendMessage(MsgColor.INFO_HILIGHT + "Plot value: "
                 + MsgColor.INFO + "D" + plot.getValue());
 
         player.sendMessage(MsgColor.INFO_HILIGHT + "Coowners: "
                 + Joiner.on(", ").join(plot.getCoowners()));
 
         player.sendMessage(MsgColor.INFO_HILIGHT + "Friends: "
                 + Joiner.on(", ").join(plot.getFriends()));
     }
 }
