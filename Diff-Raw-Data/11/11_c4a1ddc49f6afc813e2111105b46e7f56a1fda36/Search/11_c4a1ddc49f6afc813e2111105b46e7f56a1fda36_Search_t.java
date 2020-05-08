 package net.cubespace.RegionShop.Interface.CLI.Commands;
 
 import com.j256.ormlite.stmt.QueryBuilder;
 import com.j256.ormlite.stmt.Where;
 import net.cubespace.RegionShop.Config.ConfigManager;
 import net.cubespace.RegionShop.Data.Struct.ParsedItem;
 import net.cubespace.RegionShop.Database.Database;
 import net.cubespace.RegionShop.Database.ItemStorageHolder;
 import net.cubespace.RegionShop.Database.Repository.ItemRepository;
 import net.cubespace.RegionShop.Database.Table.Chest;
 import net.cubespace.RegionShop.Database.Table.ItemStorage;
 import net.cubespace.RegionShop.Database.Table.Items;
 import net.cubespace.RegionShop.Database.Table.Region;
 import net.cubespace.RegionShop.Interface.CLI.CLICommand;
 import net.cubespace.RegionShop.Interface.CLI.Command;
 import net.cubespace.RegionShop.Util.ItemName;
 import net.cubespace.RegionShop.Util.Logger;
 import net.cubespace.RegionShop.Util.Parser;
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Search implements CLICommand {
     @Command(command="shop search", arguments=1, permission="rs.command.search", helpKey="Command_Search_HelpText", helpPage="consumer")
     public static void search(CommandSender sender, String[] args) {
         //Check if sender is a player
         if(!(sender instanceof Player)) {
             sender.sendMessage(ConfigManager.main.Chat_prefix + ConfigManager.language.Command_OnlyForPlayers);
             return;
         }
 
         Player player = (Player) sender;
 
        String search = StringUtils.join(args, " ");
 
         ConcurrentHashMap<Items, ItemStack> result = new ConcurrentHashMap<Items, ItemStack>();
         Pattern r = Pattern.compile("(.*)" + search + "(.*)", Pattern.CASE_INSENSITIVE);
         ParsedItem parsedItem = Parser.parseItemID(search);
 
         List<ItemStorageHolder> itemStorages = new ArrayList<ItemStorageHolder>();
         try {
             QueryBuilder<Region, Integer> regionQb = Database.getDAO(Region.class).queryBuilder();
             itemStorages.addAll(regionQb.where().eq("world", player.getWorld().getName()).query());
 
             QueryBuilder<Chest, Integer> chestQb = Database.getDAO(Chest.class).queryBuilder();
             itemStorages.addAll(chestQb.where().eq("world", player.getWorld().getName()).query());
         } catch (SQLException e) {
             Logger.error("Could not get Items for this world", e);
             player.sendMessage(ConfigManager.main.Chat_prefix + ConfigManager.language.Command_Search_NoHit);
             return;
         }
 
         List<Items> items = new ArrayList<Items>();
 
         for(ItemStorageHolder itemStorageHolder : itemStorages) {
             items.addAll(itemStorageHolder.getItemStorage().getItems());
         }
 
         if(!items.isEmpty()) {
             for(Items item : items) {
                 if(item.getBuy() < 0.1 && item.getSell() < 0.1)
                     continue;
 
                 ItemStack iStack = ItemRepository.fromDBItem(item);
 
                 String dataName = ItemName.getDataName(iStack);
                 String niceItemName;
                 if(dataName.endsWith(" ")) {
                     niceItemName = dataName + ItemName.nicer(iStack.getType().toString());
                 } else if(!dataName.equals("")) {
                     niceItemName = dataName;
                 } else {
                     niceItemName = ItemName.nicer(iStack.getType().toString());
                 }
 
                 Matcher m = r.matcher(niceItemName);
 
                 if((parsedItem != null && item.getMeta().getItemID().equals(parsedItem.itemID) && item.getMeta().getDataValue().equals(parsedItem.dataValue))) {
                     result.put(item, iStack);
                     continue;
                 }
 
                 if (m.matches()) {
                     result.put(item, iStack);
                 }
             }
         }
 
         if(!result.isEmpty()) {
             if(net.cubespace.RegionShop.Data.Storage.Search.has(player)) {
                 net.cubespace.RegionShop.Data.Storage.Search.remove(player);
             }
 
             net.cubespace.RegionShop.Data.Storage.Search.put(player, search, result);
             Result.printResultPage(player, search, result, 1);
         } else {
             player.sendMessage(ConfigManager.main.Chat_prefix + ConfigManager.language.Command_Search_NoHit);
         }
     }
 }
