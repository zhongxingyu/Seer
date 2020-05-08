 package io.github.alshain01.flags.commands;
 
 import io.github.alshain01.flags.*;
 import io.github.alshain01.flags.System;
 
 import io.github.alshain01.flags.area.Area;
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionDefault;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 public class BundleCommand extends PluginCommand implements CommandExecutor {
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
         //if(!cmd.toString().equalsIgnoreCase("bundle")) { return false; }
 
         if (args.length < 1) {
             if(sender instanceof Player) {
                 sender.sendMessage(getUsage((Player) sender, io.github.alshain01.flags.System.getActive().getAreaAt(((Player) sender).getLocation())));
                 return true;
             }
             return false;
         }
 
         final BundleCommandType command = BundleCommandType.get(args[0]);
         if(command == null) {
             if(sender instanceof Player) {
                 sender.sendMessage(getUsage((Player) sender, System.getActive().getAreaAt(((Player) sender).getLocation())));
                 return true;
             }
             return false;
         }
 
         CommandLocation location = null;
         String bundle = null;
 
         // Check argument length (-1 means infinite optional args)
         if(args.length < command.requiredArgs
                 || (command.optionalArgs > 0 && args.length > command.requiredArgs + command.optionalArgs)) {
             sender.sendMessage(command.getHelp());
             return true;
         }
 
         // Check the command location for those that apply
         if(command.requiresLocation) {
             location = CommandLocation.get(args[1]);
             if(location == null) {
                 sender.sendMessage(command.getHelp());
                 return true;
             }
 
             // Location based commands require the player to be in the world
             if(!(sender instanceof Player)) {
                 sender.sendMessage(Message.NoConsoleError.get());
                 return true;
             }
 
             // Make sure we can set flags at that location
             if (System.getActive() == System.WORLD && (location == CommandLocation.AREA || location == CommandLocation.DEFAULT)) {
                 sender.sendMessage(Message.NoSystemError.get());
                 return true;
             }
         }
 
         if(command.requiresBundle != null) {
             if(command.requiresBundle || args.length >= 3) {
                 bundle = (command.requiresLocation) ? args[2] : args[1];
             }
         }
 
         // Process the command
         Set<String> players = new HashSet<String>();
         switch(command) {
             case HELP:
                 help(sender, getPage(args));
                 break;
             case GET:
                 get((Player)sender, location, bundle);
                 break;
             case SET:
                 Boolean value = getValue(args, 3);
                 if(value != null) { set((Player)sender, location, bundle, getValue(args, 3)); }
                 break;
             case REMOVE:
                 remove((Player)sender, location, bundle);
                 break;
             case TRUST:
                 players = getPlayers(args, command.requiredArgs - 1);
                 trust((Player)sender, location, bundle, players);
                 break;
             case DISTRUST:
                 if(args.length > command.requiredArgs) {
                     players = getPlayers(args, command.requiredArgs); } // Players can be omitted to distrust all
                 distrust((Player)sender, location, bundle, players);
                 break;
             case ADD:
                 add(sender, bundle, new HashSet<String>(Arrays.asList(args).subList(2, args.length)));
                 break;
             case CUT:
                 delete(sender, bundle, new HashSet<String>(Arrays.asList(args).subList(2, args.length)));
                 break;
             case ERASE:
                 erase(sender, bundle);
                 break;
         }
         return true;
     }
 
     private static String getUsage(Player player, Area area) {
         //usage: /bundle <get|set|remove|add|delete|erase|help>
         // We can assume if we get this far, the player has read access to the command.
         StringBuilder usage = new StringBuilder("/bundle <get");
         if(player.hasPermission("flags.command.bundle.set") && area.hasBundlePermission(player)) {
             usage.append("|set|remove|trust|distrust");
         }
 
         if(player.hasPermission("flags.command.bundle.edit")) {
             usage.append("|add|cut|erase");
         }
 
         usage.append("|help>");
 
         return usage.toString();
     }
 
     static void get(Player player, CommandLocation location, String bundleName) {
         Area area = getArea(player, location);
        Set<Flag> bundle = Flags.getDataStore().readBundle(bundleName.toLowerCase());
 
         if(!Validate.isArea(player, area)
                 || !Validate.isBundle(player, bundle, bundleName)
                 || !Validate.isBundlePermitted(player, area)
                 || !Validate.isBundlePermitted(player, bundleName))
         { return; }
 
         for(Flag flag : bundle) {
             player.sendMessage(Message.GetBundle.get()
                     .replaceAll("\\{Bundle\\}", flag.getName())
                     .replaceAll("\\{Value\\}", getFormattedValue(area.getValue(flag, false))));
         }
     }
 
     static void set(Player player, CommandLocation location, String bundleName, Boolean value) {
         boolean success = true;
         Area area = getArea(player, location);
         Set<Flag> bundle = Flags.getDataStore().readBundle(bundleName);
 
         if(!Validate.isArea(player, area)
                 || !Validate.isBundle(player, bundle, bundleName)
                 || !Validate.isBundlePermitted(player, area)
                 || !Validate.isBundlePermitted(player, bundleName))
         { return; }
 
         for(Flag flag : bundle) {
             if(!area.setValue(flag, value, player)) { success = false; }
         }
 
         player.sendMessage((success ? Message.SetBundle.get() : Message.SetMultipleFlagsError.get())
                 .replaceAll("\\{AreaType\\}", area.getSystemType().getAreaType().toLowerCase())
                 .replaceAll("\\{Bundle\\}", bundleName)
                 .replaceAll("\\{Value\\}", getFormattedValue(value).toLowerCase()));
     }
 
     static void remove(Player player, CommandLocation location, String bundleName) {
         boolean success = true;
         Area area = getArea(player, location);
         Set<Flag> bundle = Flags.getDataStore().readBundle(bundleName);
 
         if(!Validate.isArea(player, area)
                 || !Validate.isBundle(player, bundle, bundleName)
                 || !Validate.isBundlePermitted(player, area)
                 || !Validate.isBundlePermitted(player, bundleName))
         { return; }
 
         for (Flag flag : bundle) {
             if (!area.setValue(flag, null, player)) { success = false; }
         }
 
         player.sendMessage((success ? Message.RemoveBundle.get() : Message.RemoveAllFlags.get())
                 .replaceAll("\\{AreaType\\}", area.getSystemType().getAreaType().toLowerCase())
                 .replaceAll("\\{Bundle\\}", bundleName));
     }
 
     static void trust(Player player, CommandLocation location, String bundleName, Set<String> playerList) {
         if(playerList.size() == 0) { return; }
 
         Area area = getArea(player, location);
         if(!Bundle.isBundle(bundleName)
                 || !Validate.isArea(player, area)
                 || !Validate.isBundlePermitted(player, bundleName)
                 || !Validate.isPermitted(player, area))
         { return; }
 
         boolean success = true;
 
         for(Flag f : Bundle.getBundle(bundleName)) {
             if(!f.isPlayerFlag()) { continue; }
 
             for(String p : playerList) {
                 if(!area.setTrust(f, p, true, player)) { success = false; }
             }
         }
 
         player.sendMessage((success ? Message.SetTrust.get() : Message.SetTrustError.get())
                 .replaceAll("\\{AreaType\\}", area.getSystemType().getAreaType().toLowerCase())
                 .replaceAll("\\{Flag\\}", bundleName));
         return;
     }
 
     static void distrust(Player player, CommandLocation location, String bundleName, Set<String> playerList) {
         boolean success = true;
         Area area = getArea(player, location);
 
         if(!Bundle.isBundle(bundleName)
                 || !Validate.isArea(player, area)
                 || !Validate.isBundlePermitted(player, bundleName)
                 || !Validate.isPermitted(player, area))
         { return; }
 
         for(Flag f : Bundle.getBundle(bundleName)) {
             if(!f.isPlayerFlag()) { continue; }
 
             Set<String> trustList = area.getTrustList(f);
             if(trustList == null || !trustList.isEmpty()) { continue; }
 
             //If playerList is empty, remove everyone
             for(String p : playerList.isEmpty() ? trustList : playerList) {
                 if (!area.setTrust(f, p, false, player)) { success = false; }
             }
         }
 
         player.sendMessage((success ? Message.RemoveTrust.get() : Message.RemoveTrustError.get())
                 .replaceAll("\\{AreaType\\}", area.getSystemType().getAreaType().toLowerCase())
                 .replaceAll("\\{Flag\\}", bundleName));
     }
 
     static void add(CommandSender sender, String bundleName, Set<String> flags) {
         if(sender instanceof Player && !Validate.canEditBundle(sender)){ return; }
 
         Flag flag;
         Set<Flag> bundle = Flags.getDataStore().readBundle(bundleName);
 
         if(bundle == null) {
             Permission perm = new Permission("flags.bundle." + bundleName,
                     "Grants ability to use the bundle " + bundleName, PermissionDefault.FALSE);
             perm.addParent("flags.bundle", true);
             Bukkit.getServer().getPluginManager().addPermission(perm);
 
             bundle = new HashSet<Flag>();
         }
 
         for(String f : flags) {
             flag = Flags.getRegistrar().getFlagIgnoreCase(f);
             if (flag == null) {
                 sender.sendMessage(Message.AddBundleError.get());
                 return;
             }
             bundle.add(flag);
         }
 
         Flags.getDataStore().writeBundle(bundleName, bundle);
         sender.sendMessage(Message.UpdateBundle.get()
                 .replaceAll("\\{Bundle\\}", bundleName));
     }
 
     static void delete(CommandSender sender, String bundleName, Set<String> flags) {
         if(sender instanceof Player && !Validate.canEditBundle(sender)){
             return; }
 
         boolean success = true;
         Set<Flag> bundle = Flags.getDataStore().readBundle(bundleName.toLowerCase());
 
         if(!Validate.isBundle(sender, bundle, bundleName)) {
             return; }
 
         for(String s : flags) {
             Flag flag = Flags.getRegistrar().getFlag(s);
             if (flag == null || !bundle.remove(flag)) {
                 success = false; }
         }
         Flags.getDataStore().writeBundle(bundleName, bundle);
 
         sender.sendMessage((success ? Message.UpdateBundle.get() : Message.RemoveAllFlagsError.get())
                 .replaceAll("\\{Bundle\\}", bundleName));
     }
 
     static void erase(CommandSender sender, String bundleName) {
         if(sender instanceof Player && !Validate.canEditBundle(sender)){ return; }
 
         Set<String> bundles = Flags.getDataStore().readBundles();
         if (bundles == null || bundles.size() == 0 || !bundles.contains(bundleName)) {
             sender.sendMessage(Message.EraseBundleError.get());
             return;
         }
 
         Flags.getDataStore().writeBundle(bundleName, null);
         Bukkit.getServer().getPluginManager().removePermission("flags.bundle." + bundleName);
 
         sender.sendMessage(Message.EraseBundle.get()
                 .replaceAll("\\{Bundle\\}", bundleName));
     }
 
     static void help (CommandSender sender, int page) {
         Set<String> bundles = Flags.getDataStore().readBundles();
         if (bundles == null || bundles.size() == 0) {
             sender.sendMessage(Message.NoFlagFound.get()
                     .replaceAll("\\{Type\\}", Message.Bundle.get()));
             return;
         }
 
         //Get total pages: 1 header per page
         //9 flags per page, except on the first which has a usage line and 8 flags
         int total = ((bundles.size() + 1) / 9);
         if ((bundles.size() + 1) % 9 != 0) {
             total++; // Add the last page, if the last page is not full (less than 9 flags)
         }
 
         //Check the page number requested
         if (page < 1 || page > total) {
             page = 1;
         }
 
         sender.sendMessage(Message.HelpHeader.get()
                 .replaceAll("\\{Type\\}", Message.Bundle.get())
                 .replaceAll("\\{Group}", Message.Index.get())
                 .replaceAll("\\{Page\\}", String.valueOf(page))
                 .replaceAll("\\{TotalPages\\}", String.valueOf(total))
                 .replaceAll("\\{Type\\}", Message.Bundle.get()));
 
         // Setup for only displaying 10 lines at a time
         int lineCount = 1;
 
         // Usage line.  Displays only on the first page.
         if (page == 1) {
             sender.sendMessage(Message.HelpInfo.get()
                     .replaceAll("\\{Type\\}", Message.Bundle.get().toLowerCase()));
             lineCount++;
         }
 
         // Because the first page has 1 less flag count than the rest,
         // manually initialize the loop counter by subtracting one from the
         // start position of all pages other than the first.
         int loop = 0;
         if (page > 1) {
             loop = ((page-1)*9)-1;
         }
 
         String[] bundleArray = new String[bundles.size()];
         bundleArray = bundles.toArray(bundleArray);
 
         // Show the flags
         for (; loop < bundles.size(); loop++) {
             Set<Flag> flags = Flags.getDataStore().readBundle(bundleArray[loop]);
             if (flags == null) { continue; }
             StringBuilder description = new StringBuilder("");
             boolean first = true;
 
             for (Flag flag : flags) {
                 if(!first){
                     description.append(", ");
                 } else {
                     first = false;
                 }
 
                 description.append(flag.getName());
             }
             sender.sendMessage(Message.HelpTopic.get()
                     .replaceAll("\\{Topic\\}", bundleArray[loop])
                     .replaceAll("\\{Description\\}", description.toString()));
 
             lineCount++;
 
             if (lineCount > 9) {
                 return; // Page is full, we're done
             }
         }
     }
 }
