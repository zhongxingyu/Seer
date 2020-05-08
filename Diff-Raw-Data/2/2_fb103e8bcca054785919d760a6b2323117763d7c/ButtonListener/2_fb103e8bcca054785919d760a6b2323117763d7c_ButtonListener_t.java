 package info.bytecraft.listener;
 
 import java.util.List;
 
 import info.bytecraft.Bytecraft;
 import info.bytecraft.api.BytecraftPlayer;
 import info.bytecraft.database.DAOException;
 import info.bytecraft.database.IContext;
 import info.bytecraft.database.IPlayerDAO;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class ButtonListener implements Listener
 {
     private Bytecraft plugin;
     
     public ButtonListener(Bytecraft plugin)
     {
         this.plugin = plugin;
     }
 
     @EventHandler
     public void onClick(PlayerInteractEvent event)
     {
         BytecraftPlayer player = plugin.getPlayer(event.getPlayer());
         Action a = event.getAction();
         if(a != Action.RIGHT_CLICK_BLOCK)return;
         
         Block block = event.getClickedBlock();
         
         int x = block.getX();
         int y = block.getY();
         int z = block.getZ();
         
         if(x == 1129 && y == 67 && z == 1108){
             updateSign(player, block);
             event.setCancelled(true);
             return;
         }
         
         if(x == 1116 && y == 68 && z == 1092){
             givePaper(player, block);
             event.setCancelled(true);
             return;
         }
         
         if(x == 1117 && y == 68 && z == 1092){
             giveCompass(player, block);
             event.setCancelled(true);
             return;
         }
     }
     
     private void updateSign(BytecraftPlayer player, Block block)
     {
         if(!player.getRank().canVanish())return;
         if(block.getState() instanceof Sign){
             Sign sign = (Sign)block.getState();
             
             sign.setLine(0, "Richest players");
             try(IContext ctx = plugin.createContext()){
                 IPlayerDAO dao = ctx.getPlayerDAO();
                 
                 List<BytecraftPlayer> players = dao.getRichestPlayers();
                 
                 int i = 0;
                 for(BytecraftPlayer rich: players){
                     String name = rich.getName().substring(0, 5);
                    sign.setLine(i + 1, rich.getNameColor() + name);
                     i++;
                 }
                 sign.update(true, false);
                 
             }catch(DAOException e){
                 throw new RuntimeException(e);
             }
         }
     }
     
     private void givePaper(BytecraftPlayer player, Block block)
     {
         if(block.getType() != Material.STONE_BUTTON)return;
         
         try(IContext ctx = plugin.createContext()){
             IPlayerDAO dao = ctx.getPlayerDAO();
             
             if(dao.take(player, 500)){
                 player.sendMessage(ChatColor.AQUA + "500 bytes has been taken from your wallet");
                 player.getInventory().addItem(new ItemStack(Material.PAPER, 1));
                 player.updateInventory();
                 return;
             }else{
                 player.sendMessage(ChatColor.RED + "You dont have enough bytes to afford that!");
                 return;
             }
             
         }catch(DAOException e){
             throw new RuntimeException(e);
         }
     }
     
     private void giveCompass(BytecraftPlayer player, Block block)
     {
         if(block.getType() != Material.STONE_BUTTON)return;
         
         try(IContext ctx = plugin.createContext()){
             IPlayerDAO dao = ctx.getPlayerDAO();
             
             if(dao.take(player, 500)){
                 player.sendMessage(ChatColor.AQUA + "500 bytes has been taken from your wallet");
                 player.getInventory().addItem(new ItemStack(Material.COMPASS, 1));
                 player.updateInventory();
                 return;
             }else{
                 player.sendMessage(ChatColor.RED + "You dont have enough bytes to afford that!");
                 return;
             }
             
         }catch(DAOException e){
             throw new RuntimeException(e);
         }
     }
     
 }
