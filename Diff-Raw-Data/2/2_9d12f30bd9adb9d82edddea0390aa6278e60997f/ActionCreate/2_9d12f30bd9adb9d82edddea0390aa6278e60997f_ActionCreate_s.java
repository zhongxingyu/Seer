 package com.simplyian.superplots.actions;
 
 import java.util.List;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 import com.google.common.base.Joiner;
 import com.simplyian.superplots.MsgColor;
 import com.simplyian.superplots.SuperPlots;
 import com.simplyian.superplots.plot.Plot;
 
 public class ActionCreate extends BaseAction {
 
     public ActionCreate(SuperPlots main) {
         super(main);
     }
 
     @Override
     public void perform(Player player, List<String> args) {
         Plot closest = main.getPlotManager().getClosestPlotAt(
                 player.getLocation());
         double dist = closest.influenceEdgeDistance(player.getLocation());
 
         double minDist = main.getSettings().getInitialPlotSize();
 
         if (dist < minDist) {
             player.sendMessage(MsgColor.ERROR
                     + "Cannot create a plot here; the plot '"
                     + MsgColor.ERROR_HILIGHT + closest.getName()
                     + MsgColor.ERROR + "' is too close.");
             return;
         }
 
         String name = Joiner.on(' ').join(args);
         Plot existing = main.getPlotManager().getPlotByName(name);
         if (existing != null) {
             player.sendMessage(MsgColor.ERROR
                     + "Sorry, that name is already taken.");
             return;
         }
 
         if (name.length() < 1) {
             player.sendMessage(MsgColor.ERROR + "You must enter a name.");
             return;
         }
 
         if (!Plot.isValidName(name)) {
             player.sendMessage(MsgColor.ERROR
                     + "The name you have given is invalid.");
             player.sendMessage(MsgColor.ERROR
                     + "Names may only contain letters, numbers, spaces, apostrophes, and exclamation points.");
             player.sendMessage(MsgColor.ERROR
                     + "Names can only be 40 letters long.");
             return;
         }
 
         int multiplier = 1;
         for (Plot plot : main.getPlotManager().getPlots()) {
             if (plot.isOwner(player.getName())) {
                 multiplier++;
             }
         }
        multiplier = (int) Math.pow(2.0, multiplier);
 
         int requiredMoney = multiplier * 1000;
         double money = main.getEconomy().getBalance(player.getName());
         if (money < requiredMoney) {
             player.sendMessage(MsgColor.ERROR
                     + "You don't have enough money to buy a plot! (Need "
                     + requiredMoney + ")");
             return;
         }
 
         PlayerInventory inventory = player.getInventory();
         ItemStack[] itemStacks = inventory.getContents();
         int diamondCount = 0;
         for (int i = 0; i < itemStacks.length; i++) {
             if (itemStacks[i] == null) {
                 continue;
             }
             if (itemStacks[i].getType() == Material.DIAMOND) {
                 diamondCount += itemStacks[i].getAmount();
             }
         }
 
         int requiredDiamonds = multiplier
                 * main.getSettings().getBaseDiamonds();
         if (diamondCount < requiredDiamonds) {
             player.sendMessage(MsgColor.ERROR
                     + "You don't have enough diamond to buy a plot! (Need "
                     + requiredDiamonds + " total)");
             return;
         }
 
         int amountGivee = requiredDiamonds;
         for (int i = 0; i < itemStacks.length; i++) {
             if (itemStacks[i] == null) {
                 continue;
             }
             if (itemStacks[i].getType() != Material.DIAMOND) {
                 continue;
             }
 
             if (amountGivee < itemStacks[i].getAmount()) {
                 itemStacks[i]
                         .setAmount(itemStacks[i].getAmount() - amountGivee);
                 diamondCount -= amountGivee;
                 amountGivee = 0;
                 break;
 
             } else if (amountGivee > itemStacks[i].getAmount()) {
                 diamondCount -= itemStacks[i].getAmount();
                 amountGivee -= itemStacks[i].getAmount();
                 inventory.setItem(i, null);
             } else if (amountGivee == itemStacks[i].getAmount()) {
                 inventory.setItem(i, null);
                 break;
             }
         }
 
         Plot plot = main.getPlotManager().createPlot(name, player.getName(),
                 main.getSettings().getInitialPlotSize(), player.getLocation());
         player.sendMessage(MsgColor.SUCCESS
                 + "Your plot has been created successfully. Enjoy!");
     }
 
 }
