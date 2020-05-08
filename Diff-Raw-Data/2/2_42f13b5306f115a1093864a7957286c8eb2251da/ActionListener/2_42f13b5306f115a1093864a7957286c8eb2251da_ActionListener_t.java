 package de.minestar.buycraft.listener;
 
 import java.util.ArrayList;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPistonExtendEvent;
 import org.bukkit.event.block.BlockPistonRetractEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.entity.EntityChangeBlockEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 import com.bukkit.gemo.utils.BlockUtils;
 import com.bukkit.gemo.utils.SignUtils;
 import com.bukkit.gemo.utils.UtilPermissions;
 
 import de.minestar.buycraft.core.Core;
 import de.minestar.buycraft.core.Messages;
 import de.minestar.buycraft.core.Permission;
 import de.minestar.buycraft.manager.DatabaseManager;
 import de.minestar.buycraft.manager.ItemManager;
 import de.minestar.buycraft.manager.ShopManager;
 import de.minestar.buycraft.shops.InfiniteShop;
 import de.minestar.buycraft.shops.ShopType;
 import de.minestar.buycraft.shops.UserShop;
 import de.minestar.buycraft.units.Alias;
 import de.minestar.buycraft.units.BlockVector;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 
 public class ActionListener implements Listener {
 
     private ShopManager shopManager;
     private ItemManager itemManager;
     private DatabaseManager databaseManager;
 
     public ActionListener(ShopManager shopManager, ItemManager itemManager, DatabaseManager databaseManager) {
         this.shopManager = shopManager;
         this.itemManager = itemManager;
         this.databaseManager = databaseManager;
     }
 
     @EventHandler
     public void onPlayerInteract(PlayerInteractEvent event) {
         if (event.isCancelled()) {
             return;
         }
 
         // clicked on air => return
         if (event.getClickedBlock() == null) {
             return;
         }
 
         // Get the shoptype
         ShopType shopType = new ShopType(this.shopManager, event.getClickedBlock());
         // not a shop-sign => return;
         if (!shopType.isShop() || shopType.getSign() == null || shopType.getChest() == null) {
             return;
         }
 
         /** HANDLE SHOPS */
         if (shopType.isInfiniteShop()) {
             /** Handle Infiniteshop */
             this.handleInfiniteshopInteract(event, shopType);
         } else {
             /** Handle Usershop */
             this.handleUsershopInteract(event, shopType);
         }
     }
 
     private void handleInfiniteshopInteract(PlayerInteractEvent event, ShopType shopType) {
         // check permissions, if clicked on a sign
         if (shopType.isClickOnSign() && !UtilPermissions.playerCanUseCommand(event.getPlayer(), Permission.INFINITE_SHOP_CREATE)) {
             event.setUseInteractedBlock(Event.Result.DENY);
             event.setUseItemInHand(Event.Result.DENY);
             event.setCancelled(true);
         }
 
         if (shopType.isClickOnSign()) {
             /** SIGN INTERACT */
             if (shopType.getSign().getLine(1).length() < 1) {
                 event.setUseInteractedBlock(Event.Result.DENY);
                 event.setUseItemInHand(Event.Result.DENY);
                 event.setCancelled(true);
                 InfiniteShop.activateItem(event, shopType);
                 return;
             }
             if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                 event.setUseInteractedBlock(Event.Result.DENY);
                 event.setUseItemInHand(Event.Result.DENY);
                 event.setCancelled(true);
                 InfiniteShop.handleSignInteract(shopType, event.getPlayer());
             } else {
                 InfiniteShop.handleChestInteract(shopType, event.getPlayer());
             }
         } else {
             /** CHEST INTERACT */
             if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                 InfiniteShop.handleChestInteract(shopType, event.getPlayer());
             }
         }
     }
 
     private void handleUsershopInteract(PlayerInteractEvent event, ShopType shopType) {
         // check permissions, if clicked on a sign
         if (!UtilPermissions.playerCanUseCommand(event.getPlayer(), Permission.USER_SHOP_CREATE)) {
             event.setUseInteractedBlock(Event.Result.DENY);
             event.setUseItemInHand(Event.Result.DENY);
             event.setCancelled(true);
         }
 
         BlockVector position = new BlockVector(shopType.getSign().getLocation());
         UserShop shop = this.shopManager.getUserShop(position);
         if (shop == null) {
             PlayerUtils.sendError(event.getPlayer(), Core.NAME, Messages.USER_SHOP_INTERNAL_ERROR_0X00);
             PlayerUtils.sendInfo(event.getPlayer(), Core.NAME, Messages.GIVE_CODE_TO_ADMIN);
             event.setCancelled(true);
             return;
         }
 
         // GET THE ALIAS OF THE PLAYER
         Alias alias = this.shopManager.getAlias(event.getPlayer().getName());
 
         if (shopType.isClickOnSign()) {
             /** SIGN INTERACT */
             if (event.getAction() == Action.RIGHT_CLICK_BLOCK || (event.getAction() == Action.LEFT_CLICK_BLOCK && !this.verifyUsername(shopType.getSign().getLine(0), alias) && !UtilPermissions.playerCanUseCommand(event.getPlayer(), Permission.INFINITE_SHOP_CREATE))) {
                 event.setUseInteractedBlock(Event.Result.DENY);
                 event.setUseItemInHand(Event.Result.DENY);
                 event.setCancelled(true);
             }
 
             // SHOP FINISHED?
             if (!shop.isShopFinished()) {
                 if (this.verifyUsername(shopType.getSign().getLine(0), alias)) {
                     shop.activateItem(event, shopType);
                 } else {
                     PlayerUtils.sendError(event.getPlayer(), Core.NAME, Messages.USER_SHOP_CREATE_FOR_OTHERS_ERROR);
                 }
                 return;
             }
 
             // SHOP ACTIVATED?
             boolean isAdmin = UtilPermissions.playerCanUseCommand(event.getPlayer(), Permission.INFINITE_SHOP_CREATE);
             boolean isAdminAndSneakingAndOtherShop = isAdmin && !this.verifyUsername(shopType.getSign().getLine(0), alias) && event.getPlayer().isSneaking();
             if ((this.verifyUsername(shopType.getSign().getLine(0), alias) || isAdminAndSneakingAndOtherShop) && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                 if (isAdminAndSneakingAndOtherShop && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                     PlayerUtils.sendInfo(event.getPlayer(), Core.NAME, Messages.ADMINS_MUST_SNEAK);
                 }
                 if (!this.databaseManager.setUsershopActive(shop, !shop.isActive())) {
                     PlayerUtils.sendError(event.getPlayer(), Core.NAME, Messages.USER_SHOP_INTERNAL_ERROR_0X02);
                     PlayerUtils.sendInfo(event.getPlayer(), Core.NAME, Messages.TRY_AGAIN_OR_CONTACT_ADMIN);
                     return;
                 }
                 if (shop.setActive(!shop.isActive(), shopType.getChest())) {
                     PlayerUtils.sendMessage(event.getPlayer(), ChatColor.DARK_GREEN, Core.NAME, Messages.USER_SHOP_ACTIVATED);
                 } else {
                     PlayerUtils.sendMessage(event.getPlayer(), ChatColor.DARK_RED, Core.NAME, Messages.USER_SHOP_DEACTIVATED);
                 }
                 return;
             }
 
             // so its another user / click on the chest => handle interact
             if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                 shop.handleSignInteractByOtherPlayer(shopType, event.getPlayer());
             } else {
                 shop.handleChestInteract(shopType, event.getPlayer());
             }
         } else {
             /** CHEST INTERACT */
             if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                 shop.handleChestInteract(shopType, event.getPlayer());
             } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && !shop.isActive()) {
                 // CHECK SHOP-OWNER
                 if (!this.verifyUsername(shopType.getSign().getLine(0), alias)) {
                    PlayerUtils.sendError(event.getPlayer(), Core.NAME, Messages.SHOP_NOT_ACTIVATED);
                     event.setCancelled(true);
                     return;
                 }
             }
         }
     }
     // //////////////////////////////////////////////
     //
     // BLOCK-EVENTS
     //
     // //////////////////////////////////////////////
 
     private void handleShopBreak(BlockBreakEvent event, ShopType shopType) {
         Player player = event.getPlayer();
 
         // tred to break chest => return
         if (!shopType.isClickOnSign()) {
             // IF THE SHOPBLOCK IS A CHEST: DENY DESTROY (even for ops/admins!)
             PlayerUtils.sendError(player, Core.NAME, Messages.DESTROY_SIGN_FIRST);
             event.setCancelled(true);
             return;
         }
 
         if (shopType.isInfiniteShop()) {
             // handle infinite-shops
             if (!UtilPermissions.playerCanUseCommand(player, Permission.INFINITE_SHOP_CREATE)) {
                 PlayerUtils.sendError(player, Core.NAME, Messages.INFINITE_SHOP_DESTROY_ERROR);
                 event.setCancelled(true);
             } else {
                 PlayerUtils.sendSuccess(player, Core.NAME, Messages.INFINITE_SHOP_DESTROY_SUCCESS);
             }
             return;
         } else if (shopType.isUserShop()) {
             // handle user-shops
             UserShop shop = this.shopManager.getUserShop(shopType.getPosition());
 
             // UserShop expected, but not found?
             if (shop == null) {
                 PlayerUtils.sendError(player, Core.NAME, Messages.USER_SHOP_INTERNAL_ERROR_0X00);
                 event.setCancelled(true);
                 return;
             }
 
             // get alias
             Alias alias = this.shopManager.getAlias(event.getPlayer().getName());
 
             // UserShop must be deactivated first
             if (shop.isActive()) {
                 if (UtilPermissions.playerCanUseCommand(event.getPlayer(), Permission.INFINITE_SHOP_CREATE) || this.verifyUsername(shopType.getSign().getLine(0), alias)) {
                     PlayerUtils.sendError(player, Core.NAME, Messages.USER_SHOP_DEACTIVATE_FIRST);
                 } else {
                     PlayerUtils.sendError(player, Core.NAME, Messages.USER_SHOP_DESTROY_ERROR_THIS);
                 }
                 event.setCancelled(true);
                 return;
             }
 
             // check permissions
             if (UtilPermissions.playerCanUseCommand(event.getPlayer(), Permission.INFINITE_SHOP_CREATE) || this.verifyUsername(shopType.getSign().getLine(0), alias)) {
                 // remove
                 if (this.shopManager.removeUsershop(shop)) {
                     PlayerUtils.sendSuccess(player, Core.NAME, Messages.USER_SHOP_DESTROY_SUCCESS);
                 } else {
                     PlayerUtils.sendError(player, Core.NAME, Messages.USER_SHOP_INTERNAL_ERROR_0X04);
                     event.setCancelled(true);
                 }
                 return;
             } else {
                 // wrong user and not admin
                 PlayerUtils.sendError(player, Core.NAME, Messages.USER_SHOP_DESTROY_ERROR_THIS);
                 event.setCancelled(true);
                 return;
             }
         }
     }
 
     private void handleAnchorBreak(BlockBreakEvent event) {
         PlayerUtils.sendError(event.getPlayer(), Core.NAME, Messages.DESTROY_SIGN_FIRST);
         event.setCancelled(true);
     }
 
     @EventHandler
     public void onSignChange(SignChangeEvent event) {
         if (event.isCancelled())
             return;
 
         // CHECK IF THIS IS A SUPPORTED SIGN
         String line0 = event.getLine(0);
         if (!line0.startsWith("$") || !line0.startsWith("$"))
             return;
 
         // search for a chest
         if (event.getBlock().getRelative(BlockFace.DOWN).getTypeId() != Material.CHEST.getId()) {
             PlayerUtils.sendError(event.getPlayer(), Core.NAME, Messages.CREATE_CHEST_FIRST);
             SignUtils.cancelSignCreation(event);
             return;
         }
 
         // is it a doublechest?
         if (BlockUtils.isDoubleChest(event.getBlock().getRelative(BlockFace.DOWN)) != null) {
             PlayerUtils.sendError(event.getPlayer(), Core.NAME, Messages.DOUBLE_CHESTS_NOT_ALLOWED);
             SignUtils.cancelSignCreation(event);
             return;
         }
 
         if (this.shopManager.isInfiniteShop(event.getLines())) {
             this.createInfiniteShop(event);
         } else {
             this.createUserShop(event);
         }
     }
 
     private void createUserShop(SignChangeEvent event) {
         // check permissions
         if (!UtilPermissions.playerCanUseCommand(event.getPlayer(), Permission.USER_SHOP_CREATE)) {
             PlayerUtils.sendError(event.getPlayer(), Core.NAME, Messages.USER_SHOP_CREATE_ERROR);
             SignUtils.cancelSignCreation(event);
             return;
         }
 
         // get the alias of the current player
         Alias alias = this.shopManager.getAlias(event.getPlayer().getName());
 
         // UPDATE LINE 0: $USERSHOP$ --> $ALIAS$
         if (event.getLine(0).equalsIgnoreCase("$USERSHOP$")) {
             event.setLine(0, "$" + alias.getAliasName() + "$");
         }
 
         // Name too long => return
         if (event.getLine(0).length() > 15) {
             PlayerUtils.sendError(event.getPlayer(), Core.NAME, Messages.NAME_IS_TOO_LONG);
             SignUtils.cancelSignCreation(event);
             return;
         }
 
         // check if the user is the correct player
         if (!this.verifyUsername(event.getLine(0), alias)) {
             PlayerUtils.sendError(event.getPlayer(), Core.NAME, Messages.USER_SHOP_CREATE_FOR_OTHERS_ERROR);
             SignUtils.cancelSignCreation(event);
             return;
         }
 
         // update the lines and do all neccessary checks
         event.setLine(0, "$" + alias.getAliasName() + "$");
         int[] buyRatios = ItemManager.getRatio(event.getLine(2));
         int[] sellRatios = ItemManager.getRatio(event.getLine(3));
 
         // all ratios == 0? => error
         if (buyRatios[0] <= 0 && buyRatios[1] <= 0 && sellRatios[0] <= 0 && sellRatios[1] <= 0) {
             PlayerUtils.sendError(event.getPlayer(), Core.NAME, Messages.WRONG_SYNTAX);
             SignUtils.cancelSignCreation(event);
             return;
         }
 
         // check line 2
         if (event.getLine(1).length() > 0) {
             String[] split = ItemManager.extractItemLine(event.getLine(1)).split(":");
             int TypeID = this.itemManager.getItemId(split[0]);
             short data = 0;
             if (split.length == 2) {
                 try {
                     data = Short.valueOf(split[1]);
                 } catch (Exception e) {
                     data = 0;
                 }
             }
             if (!this.itemManager.isItemIDAllowed(TypeID)) {
                 PlayerUtils.sendError(event.getPlayer(), Core.NAME, Messages.ITEM_NOT_ALLOWED);
                 SignUtils.cancelSignCreation(event);
                 return;
             }
             String line = this.itemManager.getItemName(TypeID);
             if (data > 0) {
                 line += ":" + data;
             }
             event.setLine(1, "{" + line + "}");
         }
 
         // try to create the usershop in the database
         BlockVector position = new BlockVector(event.getBlock().getLocation());
         UserShop newShop = this.shopManager.addUsershop(position);
         if (newShop == null) {
             // ERROR: COULD NOT CREATE THE SHOP IN DB
             PlayerUtils.sendError(event.getPlayer(), Core.NAME, Messages.USER_SHOP_INTERNAL_ERROR_0X01);
             PlayerUtils.sendInfo(event.getPlayer(), Core.NAME, Messages.GIVE_CODE_TO_ADMIN);
             SignUtils.cancelSignCreation(event);
             return;
         }
         newShop.verifyCreationStatus(event.getLines());
         if (newShop.isShopFinished()) {
             this.databaseManager.setUsershopFinished(newShop, true);
         }
 
         // print success-info
         PlayerUtils.sendSuccess(event.getPlayer(), Core.NAME, Messages.USER_SHOP_CREATE_SUCCESS);
         if (!newShop.isShopFinished()) {
             PlayerUtils.sendMessage(event.getPlayer(), ChatColor.GRAY, Core.NAME, Messages.SHOP_CREATE_INFO);
         }
     }
 
     private boolean verifyUsername(String line0, Alias alias) {
         return line0.equalsIgnoreCase("$" + alias.getPlayerName() + "$") || line0.equalsIgnoreCase("$" + alias.getAliasName() + "$");
     }
 
     private void createInfiniteShop(SignChangeEvent event) {
         // check permissions
         if (!UtilPermissions.playerCanUseCommand(event.getPlayer(), Permission.INFINITE_SHOP_CREATE)) {
             PlayerUtils.sendError(event.getPlayer(), Core.NAME, Messages.INFINITE_SHOP_CREATE_ERROR);
             SignUtils.cancelSignCreation(event);
             return;
         }
 
         event.setLine(0, "$SHOP$");
         int[] buyRatios = ItemManager.getRatio(event.getLine(2));
         int[] sellRatios = ItemManager.getRatio(event.getLine(3));
 
         // all ratios == 0? => error
         if (buyRatios[0] <= 0 && buyRatios[1] <= 0 && sellRatios[0] <= 0 && sellRatios[1] <= 0) {
             PlayerUtils.sendError(event.getPlayer(), Core.NAME, Messages.WRONG_SYNTAX);
             SignUtils.cancelSignCreation(event);
             return;
         }
 
         // check line 2
         if (event.getLine(1).length() > 0) {
             String[] split = ItemManager.extractItemLine(event.getLine(1)).split(":");
             int TypeID = this.itemManager.getItemId(split[0]);
             short data = 0;
             if (split.length == 2) {
                 try {
                     data = Short.valueOf(split[1]);
                 } catch (Exception e) {
                     data = 0;
                 }
             }
             if (!this.itemManager.isItemIDAllowed(TypeID)) {
                 PlayerUtils.sendError(event.getPlayer(), Core.NAME, Messages.ITEM_NOT_ALLOWED);
                 SignUtils.cancelSignCreation(event);
                 return;
             }
             String line = this.itemManager.getItemName(TypeID);
             if (data > 0) {
                 line += ":" + data;
             }
             event.setLine(1, line);
         }
 
         PlayerUtils.sendSuccess(event.getPlayer(), Core.NAME, Messages.INFINITE_SHOP_CREATE_SUCCESS);
         if (event.getLine(1).length() < 1) {
             PlayerUtils.sendMessage(event.getPlayer(), ChatColor.GRAY, Core.NAME, Messages.SHOP_CREATE_INFO);
         }
     }
 
     @EventHandler
     public void onBlockPlace(BlockPlaceEvent event) {
         if (event.isCancelled())
             return;
 
         // only chests are affected
         if (event.getBlock().getTypeId() != Material.CHEST.getId())
             return;
 
         // look for doublechests and check them
         ArrayList<Block> blockList = BlockUtils.getDirectNeighbours(event.getBlock(), false);
         for (Block block : blockList) {
             if (block.getTypeId() != Material.CHEST.getId())
                 continue;
 
             if (this.shopManager.isShopBlock(block)) {
                 PlayerUtils.sendError(event.getPlayer(), Core.NAME, Messages.DOUBLE_CHESTS_NOT_ALLOWED);
                 event.setCancelled(true);
                 return;
             }
         }
     }
 
     @EventHandler
     public void onBlockBreak(BlockBreakEvent event) {
         // event is cancelled => return
         if (event.isCancelled())
             return;
 
         /** IS THE BLOCK A SIGN-ANCHOR? */
         if (this.shopManager.getSignAnchor(event.getBlock()) != null) {
             this.handleAnchorBreak(event);
             return;
         }
 
         // Get the shoptype
         ShopType shopType = new ShopType(this.shopManager, event.getBlock());
         // not a shop-sign => return;
         if (!shopType.isShop() || shopType.getSign() == null || shopType.getChest() == null) {
             return;
         }
 
         /** HANDLE SHOP-BREAK? */
         this.handleShopBreak(event, shopType);
     }
 
     @EventHandler
     public void onEntityChangeBlock(EntityChangeBlockEvent event) {
         if (event.isCancelled())
             return;
 
         // not a shop-sign => return;
         if (this.shopManager.isShopBlock(event.getBlock()) || (this.shopManager.getSignAnchor(event.getBlock()) != null)) {
             event.setCancelled(true);
         }
     }
 
     @EventHandler
     public void onPistonExtend(BlockPistonExtendEvent event) {
         if (event.isCancelled())
             return;
 
         // not a shop-sign => return;
         if (this.shopManager.isShopBlock(event.getBlocks()) || (this.shopManager.getSignAnchor(event.getBlocks()) != null)) {
             event.setCancelled(true);
         }
     }
 
     @EventHandler
     public void onPistonRetract(BlockPistonRetractEvent event) {
         if (event.isCancelled())
             return;
 
         // not a sticky piston => return;
         if (!event.isSticky())
             return;
 
         // not a shop-sign => return;
         if (this.shopManager.isShopBlock(event.getRetractLocation().getBlock()) || (this.shopManager.getSignAnchor(event.getRetractLocation().getBlock()) != null)) {
             event.setCancelled(true);
         }
     }
 
     @EventHandler
     public void onEntityExplode(EntityExplodeEvent event) {
         if (event.isCancelled())
             return;
 
         // not a shop-sign => return;
         if (this.shopManager.isShopBlock(event.blockList()) || (this.shopManager.getSignAnchor(event.blockList()) != null)) {
             event.setYield(0.0f);
             event.setCancelled(true);
         }
     }
 }
