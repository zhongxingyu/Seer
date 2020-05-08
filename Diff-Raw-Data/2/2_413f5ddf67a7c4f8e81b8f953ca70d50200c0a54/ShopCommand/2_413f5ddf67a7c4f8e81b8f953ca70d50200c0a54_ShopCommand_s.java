 package elxris.SpiceCraft.Commands;
 
 import java.util.List;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.TabCompleter;
 import org.bukkit.entity.Player;
 
 import elxris.SpiceCraft.SpiceCraft;
 import elxris.SpiceCraft.Objects.Factory;
 import elxris.SpiceCraft.Utils.Chat;
 import elxris.SpiceCraft.Utils.Strings;
 
 public class ShopCommand extends Comando implements TabCompleter{
     public Factory f;
     public ShopCommand() {
         f = new Factory();
         // Registra los eventos de plugin.
         SpiceCraft.plugin().getServer().getPluginManager().registerEvents(f, SpiceCraft.plugin());
     }
     @Override
     public boolean onCommand(CommandSender player, Command command, String label, String[] args) {
         Player p;
         if(player instanceof Player){
             p = (Player) player;
         }else{
             return true;
         }
         if(!p.hasPermission("spicecraft.shop")){
             mensaje(p, "alert.permission");
             return true;
         }
         if(( args.length == 1 || args.length == 2 ) && isCommand("comm.shop.userPrefix", args[0])){
             return onSlashShop(p, command, label, args);
        }else if(isCommand("comm.shop.messageSetter", args[0]) && args.length > 1){
             String message = new String();
             for(int i = 1; i < args.length; i++){
                 message += args[i];
                 message += " ";
             }
             Factory.getUserCache().set("userShop."+p.getName()+".message", message);
             Chat.mensaje(p, "shop.setMessage");
             return true;
         }else{
             return onShop(p, command, label, args);
         }
     }
     public boolean onShop(Player p, Command command, String label, String[] args){
         if(args.length == 0){ // Abre la tienda.
             if(!p.hasPermission("spicecraft.shop.server.open")){
                 mensaje(p, "alert.permission");
                 return true;
             }
             f.openInventory(p);
             mensaje(p, "shop.openInfo");
         }else
         if(args.length == 1){
             if(isCommand("comm.shop.help", args[0])){ // Imprime la ayuda.
                 mensaje(p, "shop.info", (f.SELLRATE*100d),
                         (f.USERMULTIPLIER/f.MULTIPLIER*100d),
                         ((f.USERMULTIPLIER/f.SELLRATE-1)*100d));
                 if(p.hasPermission("spicecraft.shop.master")){
                     mensaje(p, "shop.infoMaster");
                 }
             }else{ // Item info.
                 buscar(p, args[0], null, null, null);
             }
         }else if(args.length == 2){
             buscar(p, args[0], args[1], null, null);
         }else if(args.length == 3){
             buscar(p, args[0], args[1], args[2], null);
         }else if(args.length == 4){
             buscar(p, args[0], args[1], args[2], args[3]);
         }
         return true;
     }
     public boolean onSlashShop(Player p, Command command, String label, String[] args){
         if(!p.hasPermission("spicecraft.shop.private.open")){
             mensaje(p, "alert.permission");
             return true;
         }
         String arg = "";
         if(args.length == 1){
             arg = p.getName();
         }else if(args.length == 2){
             arg = args[1];
         }
         String shopName;
         List<String> players = SpiceCraft.getOfflinePlayerNamesMatch(arg);
         if(players.size() != 1){
             Chat.mensaje(p, "shop.userNotFound");
             return true;
         }else{
             shopName = players.get(0);
             // Si existe un mensaje establecido, se muestra en su lugar.
             if(Factory.getUserCache().isSet("userShop."+shopName+".message")){
                 Chat.mensaje(p, "shop.message", shopName, Factory.getUserCache().getString("userShop."+shopName+".message"));
             }else{
                 Chat.mensaje(p, "shop.openUserInfo", shopName);
             }
         }
         f.openInventory(p, shopName);
         return true;
     }
     @Override
     public List<String> onTabComplete(CommandSender sender, Command command,
             String label, String[] args) {
         if(sender instanceof Player){
             return tab((Player)sender, args);
         }
         return null;
     }
     private List<String> tab(Player p, String[] args){
         if(args.length == 1){
             return f.lookItems(args[0], true);
         }else if(args.length == 2){
             if(isCommand("comm.shop.userPrefix", args[0])){
                 if(SpiceCraft.getOfflinePlayerNamesMatch(args[1]).size() < 4){
                     return SpiceCraft.getOfflinePlayerNamesMatch(args[1]);
                 }
             }
         }
         return null;
     }
     private void buscar(Player p, String arg1, String arg2, String arg3, String arg4){
         List<String> items = f.lookItems(arg1);
         String item;
         if(items.size() == 0){
             mensaje(p, "shop.notExist");
             return;
         }else if(items.size() == 1){ // Si slo hay un resultado.
             item = items.get(0);
             arg4 = arg3;
             arg3 = arg2;
         }else{ // Si hay ms de un objeto en la bsqueda.
             if(arg2 == null){ // Slo si no especifica ni numero ni cantidad.
                 for(int i = 0; i < items.size(); i++){
                     items.set(i, String.format(Strings.getString("shop.searchItem"), i, items.get(i)));
                 }
                 mensaje(p, "shop.searchHead", arg1);
                 mensaje(p, items);
                 if(items.size() == 18){
                     mensaje(p, "shop.andMore", null);
                 }
                 return;
             }else{ // Si especifica el nmero.
                 // Si especifica nmero. Pero no es entero o se sale de rango.
                 if(!isInteger(arg2)){
                     mensaje(p, "alert.noInteger");
                     return;
                 }
                 if(Integer.parseInt(arg2) > items.size()-1 || Integer.parseInt(arg2) < 0){
                     mensaje(p, "shop.notExist");
                     return;
                 }
                 item = items.get(Integer.parseInt(arg2));
             }
         }
         if(arg3 == null && arg4 == null){ // Si no hay otro argumento, muestra informacin.
             f.showItemInfo(p, item);
         }else if(isCommand("comm.shop.reset", arg3) && arg4 == null){ // Si hay reset, resetea el precio.
             if(p.hasPermission("spicecraft.shop.master")){
                 f.reset(item);
                 Chat.mensaje(p, "shop.resetItem", item);
             }else{
                 Chat.mensaje(p, "alert.permission");
             }
         }else if(isCommand("comm.shop.set", arg3) && arg4 != null){// Si hay un set, setea el precio.
             if(isDouble(arg4)){
                 if(p.hasPermission("spicecraft.shop.master")){
                     f.setPrice(item, Double.parseDouble(arg4));
                     Chat.mensaje(p, "shop.setPrice", item);
                 }else{
                     Chat.mensaje(p, "alert.permission");
                 }
             }
         }
     }
 }
