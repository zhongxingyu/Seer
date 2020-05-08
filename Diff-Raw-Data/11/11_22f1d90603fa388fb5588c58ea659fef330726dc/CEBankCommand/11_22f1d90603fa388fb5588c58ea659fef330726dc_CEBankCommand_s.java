 package me.ccattell.plugins.completeeconomy.commands;
 
 import java.util.HashMap;
 import me.ccattell.plugins.completeeconomy.CompleteEconomy;
 import me.ccattell.plugins.completeeconomy.database.CEMainResultSet;
 import me.ccattell.plugins.completeeconomy.database.CEQueryFactory;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author Charlie
  */
 public class CEBankCommand implements CommandExecutor {
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
         String major;
         String minor;
         String majorSingle = CompleteEconomy.plugin.getConfig().getString("System.Currency.MajorSingle");
         String minorSingle = CompleteEconomy.plugin.getConfig().getString("System.Currency.MinorSingle");
         String majorPlural = CompleteEconomy.plugin.getConfig().getString("System.Currency.MajorPlural");
         String minorPlural = CompleteEconomy.plugin.getConfig().getString("System.Currency.MinorPlural");
         String format = CompleteEconomy.plugin.getConfig().getString("System.Formatting.Separate");
 
         if (cmd.getName().equalsIgnoreCase("bank")) {
             Player player = null;
             if (sender instanceof Player) {
                 player = (Player) sender;
             }
             String name = player.getName();
             String transaction_type;
             float transaction_amount;
             float balance;
             long major_amt;
             long minor_amt;
             double m;
             String s = "";
 
             if (args.length == 0) {
                 HashMap<String, Object> where_from = new HashMap<String, Object>();
                 where_from.put("player_name", name);
                 CEMainResultSet fq = new CEMainResultSet(where_from);
                 if (fq.resultSet()) {
                     balance = fq.getBank();
                     ////////////////////////////////////
                     //can we turn this into a class???//
                     if (format.equalsIgnoreCase("false")) {
                         if (balance == 1) {
                             major = majorSingle;
                         } else {
                             major = majorPlural;
                         }
                         s = balance + " " + major;
                     } else {
                         major_amt = (long) balance;
                         m = balance - major_amt;
                         m = m * 100;
                         minor_amt = (long) m;
                         if (major_amt > 0) {
                             if (major_amt == 1) {
                                 major = majorSingle;
                             } else {
                                 major = majorPlural;
                             }
                             s = s + major_amt + " " + major + " ";
                         }
                         if (minor_amt > 0) {
                             if (minor_amt == 1) {
                                 minor = minorSingle;
                             } else {
                                 minor = minorPlural;
                             }
                             s = s + minor_amt + " " + minor;
                         }
                     }
                     sender.sendMessage("Your current bank balance is " + s);
                     return true;
                 }
             } else if (args.length == 2) {
                 transaction_type = args[0];
                 try { // check it's a number, if not return false
                     transaction_amount = Float.parseFloat(args[1]);
                 } catch (NumberFormatException nfe) {
                    return false;
                 }
                 if(transaction_amount > 0){
                     HashMap<String, Object> where = new HashMap<String, Object>();
                     where.put("player_name", name);
                     CEMainResultSet fq = new CEMainResultSet(where);
                     if (fq.resultSet()) {
                         if(transaction_type.equalsIgnoreCase("withdrawal") || transaction_type.equalsIgnoreCase("w")){
                             balance = fq.getBank();
                             if(balance >= transaction_amount){
                                 CEQueryFactory qf = new CEQueryFactory();
                                 qf.alterBankBalance(name, -transaction_amount);
                                 qf.alterCashBalance(name, transaction_amount);
                                 sender.sendMessage("Transaction complete");
                                 return true;
                             }else{
                                 sender.sendMessage("Insufficient funds to complete withdrawal");
                                return false;
                             }
                         }
                         if(transaction_type.equalsIgnoreCase("deposit") || transaction_type.equalsIgnoreCase("d")){
                             balance = fq.getCash();
                             if(balance >= transaction_amount){
                                 CEQueryFactory qf = new CEQueryFactory();
                                 qf.alterBankBalance(name, transaction_amount);
                                 qf.alterCashBalance(name, -transaction_amount);
                                 sender.sendMessage("Transaction complete");
                                 return true;
                             }else{
                                 sender.sendMessage("Insufficient funds to complete deposit");
                                return false;
                             }
                         }
                     }
                 }
             } else {
                 sender.sendMessage("Incorrect number of arguments");
                return false;
             }
         }
         return false;
     }
 }
