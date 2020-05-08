 package info.bytecraft.commands;
 
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Server;
 
 import info.bytecraft.Bytecraft;
 import info.bytecraft.api.BytecraftPlayer;
 import info.bytecraft.api.Rank;
 import info.bytecraft.database.DAOException;
 import info.bytecraft.database.IContext;
 import info.bytecraft.database.IPlayerDAO;
 
 public class UserCommand extends AbstractCommand
 {
 
     public UserCommand(Bytecraft instance)
     {
         super(instance, "user");
     }
 
     public boolean handlePlayer(BytecraftPlayer player, String[] args)
     {
         if(!player.isMentor())return true;
         if(args.length == 2){
             if(args[0].equalsIgnoreCase("refresh")){
                 List<BytecraftPlayer> cantidates = plugin.matchPlayer(args[1]);
                 if(cantidates.size() != 1){
                     return true;
                 }
                 BytecraftPlayer target = cantidates.get(0);
                 refreshPlayer(target);
                 player.sendMessage(ChatColor.AQUA + "You refreshed " + target.getDisplayName() + ChatColor.AQUA + "'s permissions");
             }
             return true;
         }
         if (args.length == 3) {// user make settler player
             if ("make".equalsIgnoreCase(args[0])) {
                 List<BytecraftPlayer> cantidates = plugin.matchPlayer(args[2]);
                 if (cantidates.size() != 1) {
                     return true;
                 }
 
                 BytecraftPlayer target = cantidates.get(0);
                 
                 if("settler".equalsIgnoreCase(args[1])){
                     this.makeSettler(target, player);
                    player.sendMessage(ChatColor.AQUA + "You have promoted " + target.getDisplayName() + " to settler");
                     return true;
                 }else if("member".equalsIgnoreCase(args[1])){
                     this.makeMember(target);
                    player.sendMessage(ChatColor.AQUA + "You have promoted " + target.getDisplayName() + " to member");
                     return true;
                 }
             }
         }
         return true;
     }
 
     public boolean handleOther(Server server, String[] args)
     {
         if(args.length == 2){
             if(args[0].equalsIgnoreCase("refresh")){
                 List<BytecraftPlayer> cantidates = plugin.matchPlayer(args[1]);
                 if(cantidates.size() != 1){
                     return true;
                 }
                 BytecraftPlayer target = cantidates.get(0);
                 refreshPlayer(target);
                 plugin.getLogger().info(ChatColor.AQUA + "You refreshed " + target.getDisplayName() + ChatColor.AQUA + "'s permissions");
             }
             return true;
         }
         if (args.length == 3) {// user make settler player
             if ("make".equalsIgnoreCase(args[0])) {
                 List<BytecraftPlayer> cantidates = plugin.matchPlayer(args[2]);
                 if (cantidates.size() != 1) {
                     return true;
                 }
 
                 BytecraftPlayer target = cantidates.get(0);
                 
                 if("settler".equalsIgnoreCase(args[1])){
                     this.makeSettler(target, null);
                     plugin.getLogger().info(ChatColor.AQUA + "You have promoted " + target.getDisplayName() + " to settler");
                     return true;
                 }else if("member".equalsIgnoreCase(args[1])){
                     this.makeMember(target);
                     plugin.getLogger().info(ChatColor.AQUA + "You have promoted " + target.getDisplayName() + " to member");
                     return true;
                 }
             }
         }
         return true;
     }
     
     private void refreshPlayer(BytecraftPlayer player)
     {
         plugin.refreshPlayer(player);
     }
     
     private void makeSettler(BytecraftPlayer player, BytecraftPlayer mentor)
     {
         try(IContext ctx = plugin.createContext()){
             IPlayerDAO dao = ctx.getPlayerDAO();
             dao.promoteToSettler(player);
             String name = player.getRank().getColor() + player.getName();
             player.setDisplayName(name + ChatColor.WHITE);
             if(name.length() > 16){
                 player.setPlayerListName(name.substring(0, 15));
             }else{
                 player.setPlayerListName(name);
             }
             String mentorName = mentor == null ? ChatColor.BLUE  + "God" : mentor.getDisplayName();
             player.sendMessage(ChatColor.AQUA + "You have been made a settler by " + mentorName);
         }catch(DAOException e){
             throw new RuntimeException(e);
         }
     }
     
     private void makeMember(BytecraftPlayer player)
     {
         try (IContext ctx = plugin.createContext()) {
             IPlayerDAO dao = ctx.getPlayerDAO();
             player.setRank(Rank.MEMBER);
             String name = player.getRank().getColor() + player.getName();
             player.setDisplayName(name + ChatColor.WHITE);
             if(name.length() > 16){
                 player.setPlayerListName(name.substring(0, 15));
             }else{
                 player.setPlayerListName(name);
             }
             player.sendMessage(ChatColor.AQUA + "You have been promoted to a member");
             dao.updatePermissions(player);
         } catch (DAOException e) {
             throw new RuntimeException(e);
         }
     }
 
 }
