 package com.github.omwah.SDFEconomy.commands;
 
 import com.github.omwah.SDFEconomy.BankAccount;
 import com.github.omwah.SDFEconomy.SDFEconomyAPI;
 import com.github.omwah.omcommands.CommandHandler;
 import com.github.omwah.omcommands.TranslatedCommand;
import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
import java.util.List;
 import java.util.ResourceBundle;
 import java.util.regex.PatternSyntaxException;
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.economy.EconomyResponse;
 import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.Server;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.RegisteredServiceProvider;
 
 public class ConvertCommand extends TranslatedCommand {
         private SDFEconomyAPI api;
         private Server server;
     
     public ConvertCommand(SDFEconomyAPI api, Server server, ResourceBundle translation) {
         super("convert", translation);
    
         this.api = api;
         this.server = server;
 
         setArgumentRange(2, 999); // Can specify many key val pairs
         setIdentifiers(this.getName());
         setPermission("sdfeconomy.admin");
     }
 
     @Override
     public boolean execute(CommandHandler handler, CommandSender sender, String label, String identifier, String[] args)
     {
         // Command handler, checks permission, but check again just in case
         if(!handler.hasAdminPermission(sender)) {
             sender.sendMessage(getTranslation("AccountCommon-not_admin"));
         }
 
         // Which economy to load from
         String economy_name = args[0];
 
         // Parse out location scaling key pairs
         HashMap<String, Double> location_scales = new HashMap<String, Double>();
         for(int arg_idx = 1; arg_idx < args.length; arg_idx++) {
             try {
                 String[] loc_scal_pair = args[arg_idx].split("=", 2);
                 
                 String location_name = loc_scal_pair[0].trim();
                 if(!api.validLocationName(location_name)) {
                     sender.sendMessage(getTranslation("AccountCommon-invalid_location"));
                     return false;
                 }
                 
                 Double scaling;
                 if (loc_scal_pair.length > 1) {
                     scaling = new Double(loc_scal_pair[1]);
                 } else {
                     sender.sendMessage(getClassTranslation("scaling_not_specified", location_name));
                     scaling = new Double(1.0);
                 }
                 location_scales.put(location_name, scaling);
             } catch(PatternSyntaxException e) {
                 sender.sendMessage(getClassTranslation("could_not_parse_argument", arg_idx, args[arg_idx]));
                 sender.sendMessage(getClassTranslation("specify_pairs"));
                 return false;
             } catch (NumberFormatException e) {
                 sender.sendMessage(getClassTranslation("could_not_parse_argument", arg_idx, args[arg_idx]));
                 sender.sendMessage(getClassTranslation("use_numbers_for_scalings"));
                 return false;
             }
         }
             
         Collection<RegisteredServiceProvider<Economy>> econs = this.server.getServicesManager().getRegistrations(Economy.class);
         if (econs == null || econs.size() < 2) {
             sender.sendMessage(getClassTranslation("need_other_economy"));
             return false;
         }
         
         Economy src_econ = null;
         for (RegisteredServiceProvider<Economy> econ : econs) {
             String econName = econ.getProvider().getName().replace(" ", "");
             sender.sendMessage(getClassTranslation("considering_economy", econName));
             if (econName.equalsIgnoreCase(economy_name)) {
                 src_econ = econ.getProvider();
             }
         }
 
         if (src_econ == null) {
             sender.sendMessage(getClassTranslation("could_not_find_economy", economy_name));
             return false;
         }
 
         sender.sendMessage(getClassTranslation("conversion_starting"));
         
         // Create all destination banks, with one created for each destination location
         // We have to loop over all players checking for ownership and membership
         // First onwer found becomes the defacto owner
         sender.sendMessage(getClassTranslation("converting_banks"));

        List<String> bank_names = new ArrayList<String>();
        try {
            bank_names.addAll(src_econ.getBanks());
        } catch (UnsupportedOperationException ex) {
            sender.sendMessage(getClassTranslation("bank_list_failed"));
        }

         for(String location_name : location_scales.keySet()) {
            for(String src_bank_name : bank_names) {
                 String dest_bank_name = src_bank_name + "-" + location_name;
                 
                 // Skip existing banks
                 if(api.getBankAccount(dest_bank_name) != null) {
                     if(!(sender instanceof Player)) {
                         sender.sendMessage(getClassTranslation("bank_exists", dest_bank_name));
                     }
                     continue;
                 }
                 
                 // Create a new name with the bogus owner being the same as the bank name
                 api.createBank(dest_bank_name, dest_bank_name, location_name);
                 double new_balance = src_econ.bankBalance(src_bank_name).balance * location_scales.get(location_name).doubleValue();
                 EconomyResponse res = api.bankDeposit(dest_bank_name, new_balance);
                 
                 if(res.type != ResponseType.SUCCESS) {
                     sender.sendMessage(getClassTranslation("bank_deposit_failure", dest_bank_name, res.errorMessage));
                     api.deleteBank(dest_bank_name);
                 }
                 
                 BankAccount new_bank = api.getBankAccount(dest_bank_name);
                 
                 // Check for membership, ownership of new bank
                 for (OfflinePlayer op : server.getOfflinePlayers()) {
                     String pName = op.getName();
                    
                     // Set new owner as first owner found of old bank
                     // all other owners become members
                     EconomyResponse owner_res = src_econ.isBankOwner(src_bank_name, pName);
                     if(new_bank.isOwner(dest_bank_name) && owner_res.type == ResponseType.SUCCESS) {
                         new_bank.setOwner(pName);
                     } else {
                         new_bank.addMember(pName);
                     }
 
                     
                     EconomyResponse member_res = src_econ.isBankMember(src_bank_name, pName);
                     if(member_res.type == ResponseType.SUCCESS && !new_bank.isMember(pName)) {
                         new_bank.addMember(pName);
                     }
                 }
                 
                 if(!(sender instanceof Player)) {
                     sender.sendMessage(getClassTranslation("bank_created", dest_bank_name));
                 }
             }
         }
         
         // Create accounts for every player who as ever played and has an account
         // in the old economy
         sender.sendMessage(getClassTranslation("converting_player_accounts"));
         for (OfflinePlayer op : server.getOfflinePlayers()) {
             String pName = op.getName();
             if (src_econ.hasAccount(pName)) {
                 // Scale incoming balance to each destination locations
                 double src_balance = src_econ.getBalance(pName);
                 for(String location_player : location_scales.keySet()) {
                     if (api.hasAccount(pName, location_player)) {
                         if(!(sender instanceof Player)) {
                             sender.sendMessage(getClassTranslation("player_account_exists", pName, location_player));
                         }
                         continue;
                     }
                     api.createPlayerAccount(pName, location_player);
                 
                     double new_balance = src_balance * location_scales.get(location_player).doubleValue();
                     if(!(sender instanceof Player)) {
                         sender.sendMessage(getClassTranslation("player_account_created", pName, location_player, new_balance));
                     }
                     api.setBalance(pName, location_player, new_balance);
                 }
             }
         }
         api.forceCommit();
         sender.sendMessage(getClassTranslation("conversion_complete"));
         
         return true;
     }
 }
