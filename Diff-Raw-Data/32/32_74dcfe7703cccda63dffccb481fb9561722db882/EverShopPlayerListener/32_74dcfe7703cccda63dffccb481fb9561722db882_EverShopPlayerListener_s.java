 package GyD.EverShop;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.ContainerBlock;
 import org.bukkit.block.Sign;
 import org.bukkit.craftbukkit.inventory.CraftInventory;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.inventory.ItemStack;
 
 import com.nijiko.coelho.iConomy.iConomy;
 import com.nijiko.coelho.iConomy.system.Account;
 import com.nijiko.coelho.iConomy.system.Bank;
 
 public class EverShopPlayerListener extends PlayerListener {
     private final EverShop plugin;
     //public Configuration EverShopConfig = new Configuration(new File("plugins/EverShop/config.yml"));
 
    public EverShopPlayerListener(final EverShop plugin) {
        this.plugin = plugin;
    }
    
    public void onPlayerInteract(PlayerInteractEvent e)
     {
         if ( !e.getAction().equals(Action.RIGHT_CLICK_BLOCK) )
         {
             return;
         }
         // get player
         Player player = e.getPlayer();
          
        if (EverShopBlockListener.isBank(e.getClickedBlock()))
         {
             // how many iConomy coin for a Slime?
             int ratio = 1;
              
             int number = player.getItemInHand().getAmount();
             Bank bank = iConomy.getBank();
             Account account = bank.getAccount(player.getName());
             double balance = account.getBalance();
              
             if( number == -1 ||  number == 0 )
             {
                 number = 1;
             }
  
             double price = number*ratio;
              
             ItemStack prix = new ItemStack(341);
             prix.setAmount(number);
              
             if( player.getItemInHand().getType().equals(Material.SLIME_BALL) )
             {
                 account.add(price);
                 InventoryWorkaround.removeItem((CraftInventory)player.getInventory(), true, new ItemStack[] { prix });
                 player.sendMessage("§eDépot de §a"+price+" §e"+bank.getCurrency());
                 player.sendMessage("§eSolde: §a"+account.getBalance());
             }
             else
             {
                 if( balance >= price )
                 {
                     if( !player.getInventory().addItem(new ItemStack[] { prix }).isEmpty() )
                     {
                         player.sendMessage("§cPas de place dans vos poches");
                     }
                     else
                     {
                         account.subtract(price);
                         player.sendMessage("§eRetrait de §a"+price+" §e"+bank.getCurrency());
                         player.sendMessage("§eSolde: §a"+account.getBalance());
                     }
                 }
                 else
                 {
                     player.sendMessage("§cPas assez d'argent (§a"+balance+")§c pour un retrait de §a"+price+" §c"+bank.getCurrency());
                 }
             }
  
             player.updateInventory();
         }
          
        if(EverShopBlockListener.isShop(e.getClickedBlock())){
              
              
             // get chest
             ContainerBlock chest = (ContainerBlock)e.getClickedBlock().getFace(BlockFace.DOWN, 1).getState();
              
             //get sign
             Sign sign = (Sign) e.getClickedBlock().getState();
              
             boolean restore = false;
              
             String l1 = sign.getLines()[1];
             String l2[] = sign.getLines()[2].split("[/]");
              
             //get price
             int price = Integer.parseInt(l2[1].substring(0, l2[1].length()-6));
              
             // get amount of items sold
             int sellamount = Integer.parseInt(l2[0]);
              
             // rename item
             String itemname = EverShopBlockListener.rename(l1);
             // separate items/colors
             String[] itemvals = itemname.split("[ :]+");
             // default color
             short color = new Short("0");
              
             if( itemvals.length == 2 ){
                 if( EverShopBlockListener.isInt(itemvals[1]) )
                 {
                     color = Short.valueOf(itemvals[1]);
                 }
                 else
                 {
                     player.sendMessage("§c[Shop] Erreur de format");
                     return;
                 }
             }
              
             ItemStack prix = new ItemStack(341);
             prix.setAmount(price);
              
             ItemStack recompense = new ItemStack(EverShopBlockListener.getItemId(itemvals[0]));
             recompense.setAmount(sellamount);
             recompense.setDurability(color);
              
             if (!InventoryWorkaround.containsItem((CraftInventory)chest.getInventory(), true, new ItemStack[] { recompense })) {
                 player.sendMessage("§cLe coffre est vide");
                 return ;
             }
              
             if (!InventoryWorkaround.containsItem((CraftInventory)player.getInventory(), true, new ItemStack[] { prix })) {
                 player.sendMessage("§cVous n'avez pas assez de Slime");
                 return ;
             }
              
             InventoryWorkaround.removeItem((CraftInventory)chest.getInventory(), true, new ItemStack[] { recompense });
             InventoryWorkaround.removeItem((CraftInventory)player.getInventory(), true, new ItemStack[] { prix });
             if( !chest.getInventory().addItem(new ItemStack[] { prix }).isEmpty() )
             {
                 player.sendMessage("§cPas de place dans le coffre");
                 restore = true;
             }
              
              
             if( !player.getInventory().addItem(new ItemStack[] { recompense }).isEmpty() )
             {
                 player.sendMessage("§cPas de place dans votre inventaire");
                 restore = true;
             }
              
             if( restore )
             {
                 chest.getInventory().addItem(new ItemStack[] { recompense });
                 player.getInventory().addItem(new ItemStack[] { prix });
             }
              
              
              
             player.updateInventory();
         }
     }
 }
