 package net.battlenexus.bukkit.economy.commands;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 
 import yt.codebukkit.scoreboardapi.Scoreboard;
 
 import net.battlenexus.bukkit.economy.BattleConomy;
 import net.battlenexus.bukkit.economy.api.Api;
 
 public class Send extends BNCommand {
 
     @Override
     public void execute(CommandSender sender, String[] args) {
         if (sender instanceof ConsoleCommandSender) {
             sender.sendMessage("This command can only be used by players");
             return;
         }
         if (args.length < 2) {
             sender.sendMessage("/bc send <username> <amount> [world]");
             return;
         }
 
         String username = args[0];
 
         if (!Api.accountExists(args[0])) {
             sender.sendMessage("The account '" + args[0] + "' doesn't exist");
             return;
         }
 
         double amount = Double.parseDouble(args[1]);
 
         if (Api.hasEnough(sender.getName(), amount)) {
             if (Api.accountExists(username)) {                
                 Api.addMoney(username, amount, Api.getEconomyKeyByPlayerWorld(sender.getName()));
                 Api.takeMoney(sender.getName(), amount);
                 String money = Api.formatMoney(amount);
                 sender.sendMessage("You sent " + money
                         + " to " + username);
                final Player reciever = Bukkit.getServer().getPlayer(username);
                 if(reciever != null) {
                    final Scoreboard board = BattleConomy.scoreboard.createScoreboard("notify"+reciever.getName(), 2);
                     board.setType(Scoreboard.Type.SIDEBAR);
                    board.setScoreboardName("You've got money");
                    board.setItem(sender.getName()+"+", Integer.parseInt(args[1]));
                     board.showToPlayer(reciever, true);
                    //reciever.sendMessage(sender.getName()+ " has sent you " + money);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(BattleConomy.INSTANCE, new Runnable() {
                        public void run() {
                            board.stopShowingAllPlayers();
                        }
                    }, 100);
                 }
                 
             } else
                 sender.sendMessage("User '"
                         + username
                         + "' doesn't exist, money has not been taken from your balance");
         } else
             sender.sendMessage("You do not have enough to complete this transaction. No money has been taken.");
     }
 
 }
