 package io.github.alshain01.flags;
 
 import io.github.alshain01.flags.area.Area;
 import org.bukkit.Bukkit;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionDefault;
 
 import java.util.*;
 
 final class CommandBundle extends Command implements CommandExecutor {
     private enum BundleCommandType {
         SET('S', 4, 0, true, true, "Set <area|world|default> <bundle> <true|false>"),
         GET('G', 3, 0, true, true, "Get <area|world|default> <bundle>"),
         REMOVE('R', 3, 0, true, true, "Remove <area|world|default> <bundle>"),
         TRUST('T', 4, -1, true, true, "Trust <area|world|default> <bundle> <player> [player]..."),
         DISTRUST('D', 3, -1, true, true, "Distrust <area|world|default> <bundle> [player] [player]..."),
         HELP('H', 1, 1, false, null, "Help [page]"),
         ADD('A', 3, -1, false, true, "Add <bundle> <flag> [flag]..."),
         CUT ('C', 3, -1, false, true, "Cut <bundle> <flag> [flag]..."),
         ERASE ('E', 2, 0, false, true, "Erase <bundle>");
 
         final char alias;
         final int requiredArgs;
         final int optionalArgs; // -1 for infinite
         final boolean requiresLocation;
         final Boolean requiresBundle; // null if bundle isn't even an optional arg.
         final String help;
 
         BundleCommandType(char alias, int requiredArgs, int optionalArgs, boolean hasLocation, Boolean requiresBundle, String help) {
             this.alias = alias;
             this.requiredArgs = requiredArgs;
             this.optionalArgs = optionalArgs;
             this.help = help;
             this.requiresLocation = hasLocation;
             this.requiresBundle = requiresBundle;
         }
 
         static BundleCommandType get(String name) {
             if(name.length() > 1)  { return valueOf(name.toUpperCase()); }
             return getByAlias(name.toUpperCase().toCharArray()[0]);
         }
 
         static BundleCommandType getByAlias(char alias) {
             for(BundleCommandType c : BundleCommandType.values()) {
                 if(alias == c.alias) { return c; }
             }
             return null;
         }
 
         String getHelp() {
             return "/bundle " + this.help;
         }
     }
 
     @Override
     public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
         if (args.length < 1) {
             if(sender instanceof Player) {
                 sender.sendMessage(getUsage((Player) sender, CuboidType.getActive().getAreaAt(((Player) sender).getLocation())));
                 return true;
             }
             return false;
         }
 
         final BundleCommandType command = BundleCommandType.get(args[0]);
         if(command == null) {
             if(sender instanceof Player) {
                 sender.sendMessage(getUsage((Player) sender, CuboidType.getActive().getAreaAt(((Player) sender).getLocation())));
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
             if (CuboidType.getActive() == CuboidType.WILDERNESS && (location == CommandLocation.AREA || location == CommandLocation.DEFAULT)) {
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
 
     private static void get(Player player, CommandLocation location, String bundleName) {
         final Area area = getArea(player, location);
 
         if(Validate.notPermittedBundle(player, area, bundleName)) { return; }
         final Set<Flag> bundle = Bundle.getBundle(bundleName);
 
         for(Flag flag : bundle) {
             player.sendMessage(Message.GetBundle.get()
                     .replace("{Bundle}", flag.getName())
                     .replace("{Value}", getFormattedValue(area.getValue(flag, false))));
         }
     }
 
     private static void set(Player player, CommandLocation location, String bundleName, Boolean value) {
         boolean success = true;
         final Area area = getArea(player, location);
 
         if(Validate.notPermittedBundle(player, area, bundleName)) { return; }
         final Set<Flag> bundle = Bundle.getBundle(bundleName);
 
         for(Flag flag : bundle) {
             if(!area.setValue(flag, value, player)) { success = false; }
         }
 
         player.sendMessage((success ? Message.SetBundle.get() : Message.SetMultipleFlagsError.get())
                 .replace("{AreaType}", area.getCuboidType().getCuboidName().toLowerCase())
                 .replace("{Bundle}", bundleName)
                 .replace("{Value}", getFormattedValue(value).toLowerCase()));
     }
 
     private static void remove(Player player, CommandLocation location, String bundleName) {
         boolean success = true;
         final Area area = getArea(player, location);
 
         if(Validate.notPermittedBundle(player, area, bundleName)) { return; }
         final Set<Flag> bundle = Bundle.getBundle(bundleName);
 
         for (Flag flag : bundle) {
             if (!area.setValue(flag, null, player)) { success = false; }
         }
 
         player.sendMessage((success ? Message.RemoveBundle.get() : Message.RemoveAllFlags.get())
                 .replace("{AreaType}", area.getCuboidType().getCuboidName().toLowerCase())
                 .replace("{Bundle}", bundleName));
     }
 
     private static void trust(Player player, CommandLocation location, String bundleName, Set<String> playerList) {
         boolean success = true;
         Area area = getArea(player, location);
 
         if(Validate.notPermittedBundle(player, area, bundleName)) { return; }
 
         for(Flag f : Bundle.getBundle(bundleName)) {
             if(!f.isPlayerFlag()) { continue; }
 
             for(String p : playerList) {
                 if(!area.setTrust(f, p, true, player)) { success = false; }
             }
         }
 
         player.sendMessage((success ? Message.SetTrust.get() : Message.SetTrustError.get())
                 .replace("{AreaType}", area.getCuboidType().getCuboidName().toLowerCase())
                 .replace("{Flag}", bundleName));
     }
 
     private static void distrust(Player player, CommandLocation location, String bundleName, Set<String> playerList) {
         boolean success = true;
         Area area = getArea(player, location);
 
         if(Validate.notPermittedBundle(player, area, bundleName)) { return; }
 
         for(Flag f : Bundle.getBundle(bundleName)) {
             if(!f.isPlayerFlag()) { continue; }
 
             Set<String> trustList = area.getTrustList(f);
             if(Validate.notTrustList(trustList)) { continue; }
 
             //If playerList is empty, remove everyone
             for(String p : playerList.isEmpty() ? trustList : playerList) {
                 if (!area.setTrust(f, p, false, player)) { success = false; }
             }
         }
 
         player.sendMessage((success ? Message.RemoveTrust.get() : Message.RemoveTrustError.get())
                 .replace("{AreaType}", area.getCuboidType().getCuboidName().toLowerCase())
                 .replace("{Flag}", bundleName));
     }
 
     private static void add(CommandSender sender, String bundleName, Set<String> flags) {
         if(Validate.notPermittedEditBundle(sender)){ return; }
 
         Flag flag;
         Set<Flag> bundle;
 
         if(Bundle.isBundle(bundleName)) {
             bundle = Bundle.getBundle(bundleName);
         } else {
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
 
         Bundle.setBundle(bundleName, bundle);
         sender.sendMessage(Message.UpdateBundle.get()
                 .replace("{Bundle}", bundleName));
     }
 
     private static void delete(CommandSender sender, String bundleName, Set<String> flags) {
         if(Validate.notPermittedEditBundle(sender)){ return; }
         if(Validate.notBundle(sender, bundleName)) { return; }
 
         boolean success = true;
         Set<Flag> bundle = Bundle.getBundle(bundleName.toLowerCase());
 
         for(String s : flags) {
             Flag flag = Flags.getRegistrar().getFlag(s);
             if (flag == null || !bundle.remove(flag)) {
                 success = false; }
         }
         Bundle.setBundle(bundleName, bundle);
 
         sender.sendMessage((success ? Message.UpdateBundle.get() : Message.RemoveAllFlagsError.get())
                 .replace("{Bundle}", bundleName));
     }
 
     private static void erase(CommandSender sender, String bundleName) {
         if(Validate.notPermittedEditBundle(sender)){ return; }
 
         Set<String> bundles = Bundle.getBundleNames();
         if (bundles == null || bundles.size() == 0 || !bundles.contains(bundleName)) {
             sender.sendMessage(Message.EraseBundleError.get());
             return;
         }
 
         Bundle.setBundle(bundleName, null);
         sender.sendMessage(Message.EraseBundle.get().replace("{Bundle}", bundleName));
     }
 
     private static void help (CommandSender sender, int page) {
         int startIndex, endIndex, totalPages;
         List<String> bundles = new ArrayList<String>(Bundle.getBundleNames());
 
         if (Validate.isNullOrEmpty(sender, bundles, Message.Bundle)) { return; }
         Collections.sort(bundles);
 
         // Get total pages: 1 header per page
         // 9 flags per page, except on the first which has a usage line and 8 flags
         // Add the last page, if the last page is not full (less than 9 flags)
         totalPages = ((bundles.size() + 1) / 9);
         if ((bundles.size() + 1) % 9 != 0) { totalPages++; }
 
         //Check the page number requested
         if (page < 1 || page > totalPages) { page = 1; }
 
         // Find the sub list for this page
         startIndex = ((page - 1) * 9) - 1;
         if(startIndex < 0) { startIndex = 0; }
 
         endIndex = startIndex + page > 1 ? 9 : 8;
         if(endIndex > bundles.size()) { endIndex = bundles.size(); }
 
         bundles = bundles.subList(startIndex, endIndex);
 
         // Send the help header
         sender.sendMessage(Message.HelpHeader.get()
                 .replace("{Type}", Message.Bundle.get())
                 .replace("{Group}", Message.Index.get())
                 .replace("{Page}", String.valueOf(page))
                 .replace("{TotalPages}", String.valueOf(totalPages))
                 .replace("{Type}", Message.Bundle.get()));
 
         // Send the usage line.  Displays only on the first page.
         if (page == 1) {
             sender.sendMessage(Message.HelpInfo.get()
                     .replace("{Type}", Message.Bundle.get().toLowerCase()));
         }
 
         // Send the help lines
         for(String b : bundles) {
             Set<Flag> flags = Bundle.getBundle(b);
             if (flags == null || flags.size() == 0) { continue; }
 
             // Build the help line
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
                     .replace("{Topic}", b)
                     .replace("{Description}", description.toString()));
         }
     }
 }
