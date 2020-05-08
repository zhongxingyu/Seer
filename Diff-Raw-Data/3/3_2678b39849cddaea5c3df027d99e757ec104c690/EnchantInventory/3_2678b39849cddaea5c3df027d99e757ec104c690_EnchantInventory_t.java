 
 package tk.thundaklap.enchantism;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Instrument;
 import org.bukkit.Material;
 import org.bukkit.Sound;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.meta.EnchantmentStorageMeta;
 import org.bukkit.inventory.meta.ItemMeta;
 
 
 public final class EnchantInventory {
 
    public Player player;
    private EnchantPage[] pages;
    private int pageCount = 0;
    private int currentPage = 0;
    private Inventory inventory;
    private boolean showUnenchant = false;
    
    public EnchantInventory(Player player){
        
        this.player = player;
        this.inventory = Bukkit.createInventory(player, 54, "Enchant");
        slotChange(null);
        this.player.openInventory(inventory);
    }
    
    public Inventory getInventory(){
        return inventory;
    }
    
    public void updatePlayerInv(){
        
        boolean isMultiPage = pageCount != 0;
        inventory.setContents(concatArray(topRows(isMultiPage && pageCount != currentPage, isMultiPage && currentPage != 0, showUnenchant), pages[currentPage].getInventory()));
        new DelayUpdateInventory(player).runTaskLater(Enchantism.getInstance(), 1);
        
    }
    
    public void slotChange(ItemStack change){
        
        
        List<Enchantment> applicableEnchantments = new ArrayList<Enchantment>();
        
       //Amount check to prevent stacked books getting enchanted.
       if(change != null && !change.getType().equals(Material.AIR) && change.getAmount() > 1){
            
            boolean isBook = change.getType().equals(Material.BOOK);
            
            for(Enchantment enchant : Enchantment.values()){
                if(enchant.canEnchantItem(change) || isBook){
                     applicableEnchantments.add(enchant);
                }
            }
        }
        
        currentPage = 0;
        
        if(applicableEnchantments.isEmpty()){
            pageCount = 0;
            pages = new EnchantPage[1];
            pages[0] = new EnchantPage();
            pages[0].setEmpty();
            showUnenchant = false;
            
        }else{
        
            
            int numberOfEnchants = applicableEnchantments.size();
            
            pageCount = (numberOfEnchants - 1) / 8;
            
            
            pages = new EnchantPage[pageCount + 1];
            
            for(int i = 0; i < pages.length; i++){
                pages[i] = new EnchantPage();
            }
            
            int currentlyAddingPage = 0;
            
            for(Enchantment enchant : applicableEnchantments){
                
                //Method returns false if the page is full.
                if(!pages[currentlyAddingPage].addEnchantment(enchant)){
                    
                    pages[++currentlyAddingPage].addEnchantment(enchant);
                }
                
            }
            
            pages[currentlyAddingPage].fill();
            
            showUnenchant = true;
            
        }
        
        updatePlayerInv();
        
    }
    
    public void inventoryClicked(InventoryClickEvent event){
        
        int rawSlot = event.getRawSlot();
        
        //Prevent people from shift-clicking in enchantment tables, wool, etc and losing it.
        if((event.isShiftClick() && rawSlot >= 54) || (rawSlot < 54 && rawSlot != 4)){
            event.setCancelled(true);
        }
        
        
        
        if(rawSlot == 0 && currentPage != 0 && pageCount > 0){
            currentPage--;
            player.playSound(player.getLocation(), Sound.CLICK, 2F, 1F);
            updatePlayerInv();
            return;
        }
        
        if(rawSlot == 8 && currentPage != pageCount){
            currentPage++;
            player.playSound(player.getLocation(), Sound.CLICK, 2F, 1F);
            updatePlayerInv();
            return;
        }
        
        if(rawSlot == 4){
            
            slotChange(event.getCursor());
            return;
        }
        
        if(rawSlot == 6){
            ItemStack item = inventory.getItem(4);
            
            if(item != null && !item.getType().equals(Material.AIR)){
                
                List<Enchantment> enchantsToRemove = new ArrayList<Enchantment>();
                
                for(Map.Entry entry : item.getEnchantments().entrySet()){
                    enchantsToRemove.add((Enchantment)entry.getKey());
                }
                
                for(Enchantment enchant : enchantsToRemove){
                    item.removeEnchantment(enchant);
                }
            }
        }
        
        if(rawSlot >= 18 && rawSlot < 54){
            
            
            
            EnchantLevelCost enchant = pages[currentPage].enchantAtSlot(rawSlot - 18);
            
            if(enchant == null){
                
                return;
            }
            
            if(player.getLevel() < enchant.cost){
                player.sendMessage(ChatColor.RED + "You don\'t have enough XP to enchant the item with that enchantment!");
                return;
            }
            
            player.setLevel(player.getLevel() - enchant.cost);
            player.playSound(player.getLocation(), Sound.LEVEL_UP, 2F, 1F);
            
            ItemStack item = inventory.getItem(4);
            
            if(item.getType() == Material.BOOK){
                item.setType(Material.ENCHANTED_BOOK);
                
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta)item.getItemMeta();
                meta.addStoredEnchant(enchant.enchant, enchant.level, true);
                
                item.setItemMeta(meta);
                
                return;
            }
            
            try{
                item.addUnsafeEnchantment(enchant.enchant, enchant.level);
            }catch(Exception e){
                player.sendMessage(ChatColor.RED + "Unexpected error. See console for details.");
                Enchantism.getInstance().getLogger().severe(e.getMessage());
            }
            inventory.setItem(4, item);
            
        }
        
    }
    
    private ItemStack[] topRows(boolean showNextPage, boolean showPrevPage, boolean showUnenchantButton){
        
        ItemStack[] is = new ItemStack[18];
        
        for(int i = 0; i < 18; i++){
            
            switch(i){
                case 0:
                    if(showPrevPage){
                        is[i] = new ItemStack(Material.WOOL, 1, (byte)14);
                        ItemMeta woolMeta = is[i].getItemMeta();
                        woolMeta.setDisplayName(ChatColor.RED + "Previous Page");
                        is[i].setItemMeta(woolMeta);
                    }else{
                        is[i] = new ItemStack(Material.WOOL, 1);
                    }
                    break;
                    
                case 4:
                    is[i] = inventory.getItem(4);
                    break;
                    
                case 6:
                    if(showUnenchantButton){
                        is[i] = new ItemStack(Material.ENCHANTED_BOOK, 1);
                        ItemMeta meta = is[i].getItemMeta();
                        
                        meta.setDisplayName(ChatColor.RED + "Remove Enchantments");
                        
                        is[i].setItemMeta(meta);
                        
                    }else{
                        is[i] = new ItemStack(Material.WOOL, 1);
                    }
                    break;
                
                case 8:
                    if(showNextPage){
                        is[i] = new ItemStack(Material.WOOL, 1, (byte)5);
                        ItemMeta woolMeta = is[i].getItemMeta();
                        woolMeta.setDisplayName(ChatColor.GREEN + "Next Page");
                        is[i].setItemMeta(woolMeta);
                    }else{
                        is[i] = new ItemStack(Material.WOOL, 1);
                    }
                    break;
                    
                case 13:
                    is[i] = new ItemStack(Material.ENCHANTMENT_TABLE, 1);
                    break;
                default:
                    is[i] = new ItemStack (Material.WOOL, 1);
            }
            
        }
        
        return is;
        
    }
    
 
    
    private static <T> T[] concatArray(T[] a, T[] b) {
         final int alen = a.length;
         final int blen = b.length;
         final T[] result = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), alen + blen);
         System.arraycopy(a, 0, result, 0, alen);
         System.arraycopy(b, 0, result, alen, blen);
         return result;
     }
    
    
 }
