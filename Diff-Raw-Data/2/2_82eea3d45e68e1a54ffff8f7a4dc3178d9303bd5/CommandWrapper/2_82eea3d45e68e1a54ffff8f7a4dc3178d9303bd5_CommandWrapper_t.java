 package com.undeadscythes.udsplugin;
 
 import java.util.*;
 import org.bukkit.*;
 import org.bukkit.command.*;
 import org.bukkit.enchantments.*;
 import org.bukkit.entity.*;
 import org.bukkit.inventory.*;
 
 /**
  * A command that is designed to be run by a player.
  * Methods allow various checks to be made and messages to be sent to the players on errors.
  * @author UndeadScythes
  */
 public abstract class CommandWrapper implements CommandExecutor {
     protected SaveablePlayer player;
     private String commandName;
     protected String[] args;
     protected String subCmd;
 
     /**
      * Checks player permission then passes arguments to executor.
      * @param sender Player who sent the command.
      * @param command The command sent.
      * @param label ?
      * @param args Arguments to the command.
      * @return <code>true</code> if the commands has been handled fully, <code>false</code> otherwise.
      */
     @Override
     public final boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
         if(sender instanceof Player) {
             commandName = command.getName();
             player = UDSPlugin.getOnlinePlayers().get(sender.getName());
             if(hasPerm(Perm.valueOf(commandName.toUpperCase()))) {
                 this.args = args.clone();
                 if(args.length > 0) {
                     subCmd = args[0].toLowerCase();
                 }
                 playerExecute();
             }
             return true;
         }
         return false;
     }
 
     /**
      * Checks if a string corresponds to a valid rank.
      * @param string String to check.
      * @return The rank if it is valid, <code>null</code> otherwise.
      */
     protected PlayerRank getRank(final String string) {
         final PlayerRank rank = PlayerRank.getByName(string);
         if(rank == null) {
             player.sendMessage(Color.ERROR + "You have not entered a valid rank.");
         }
         return rank;
     }
 
     /**
      * Checks the player has an item in their hand.
      * @return <code>true</code> if the player is holding an item, <code>false</code> otherwise.
      */
     protected boolean notAirHand() {
         if(player.getItemInHand().getType().equals(Material.AIR)) {
             player.sendMessage(Color.ERROR + "You need an item in your hand.");
             return false;
         } else {
             return true;
         }
     }
 
     /**
      * Check that the item is enchantable.
      * @param enchantment The enchantment.
      * @param item The item.
      * @return <code>true</code> if the item can be enchanted, <code>false</code> otherwise.
      */
     protected boolean canEnchant(final Enchantment enchantment, final ItemStack item) {
         if(enchantment.canEnchantItem(item)) {
             return true;
         } else {
             player.sendMessage(Color.ERROR + "You cannot use that enchantment on that item.");
             return false;
         }
     }
 
     /**
      * Gets the players selected pet.
      * @return The UUID of the pet or <code>null</code> if the player has no pet selected.
      */
     protected UUID getSelectedPet() {
         final UUID pet = player.getSelectedPet();
         if(pet == null) {
             player.sendMessage(Color.ERROR + "Right click a pet while sneaking to select it first.");
         }
         return pet;
     }
 
     /**
      * Check if the player is near monsters.
      * @return
      */
     protected boolean notNearMobs() {
         List<Entity> entities = player.getNearbyEntities(10, 3, 10);
         for(Entity entity : entities) {
             if(UDSPlugin.getHostileMobs().contains(entity.getType())) {
                 player.sendMessage(Color.ERROR + "You cannot do that now, there are monsters nearby.");
                 return false;
             }
         }
         return true;
     }
 
     /**
      * Get the players shop.
      * @return The shop region or <code>null</code> if the player does not own a shop.
      */
     protected Region getShop() {
         final Region shop = UDSPlugin.getRegions(RegionType.SHOP).get(player.getName() + "shop");
         if(shop == null) {
             player.sendMessage(Color.ERROR + "You do not own a shop.");
         }
         return shop;
     }
 
     /**
      * Check the number of arguments and send help if there are the wrong number.
      * @param num Number of arguments required.
      * @return <code>true</code> if there are the correct number of arguments, <code>false</code> otherwise.
      */
     protected boolean numArgsHelp(final int num) {
         if(args.length == num) {
             return true;
         } else {
             numArgsHelp();
             return false;
         }
     }
 
     /**
      * Check the number of arguments and send help if there are the wrong number.
      * @param num Number of arguments required.
      * @return <code>true</code> if there are the correct number of arguments, <code>false</code> otherwise.
      */
     protected boolean minArgsHelp(final int num) {
         if(args.length >= num) {
             return true;
         } else {
             numArgsHelp();
             return false;
         }
     }
 
     /**
      * Check the number of arguments and send help if there are the wrong number.
      * @param num Number of arguments required.
      * @return <code>true</code> if there are the correct number of arguments, <code>false</code> otherwise.
      */
     protected boolean maxArgsHelp(final int num) {
         if(args.length <= num) {
             return true;
         } else {
             numArgsHelp();
             return false;
         }
     }
 
     /**
      * Send the player help relating to the number of arguments used.
      */
     private void numArgsHelp() {
         player.sendMessage(Color.ERROR + "You have made an error using this command.");
         player.sendMessage(Color.MESSAGE + "Use /help " + commandName + " to check the correct usage.");
     }
 
     /**
      * If the arguments are asking for help, send help otherwise advise about bad arguments.
      * @param args Arguments to the command.
      */
     protected void subCmdHelp() {
         if(args[0].equalsIgnoreCase("help")) {
             if(args.length == 2 && args[1].matches(UDSPlugin.INT_REGEX)) {
                 sendHelp(Integer.parseInt(args[1]));
             } else {
                 sendHelp(1);
             }
         } else {
             player.sendMessage(Color.ERROR + "That is not a valid sub command.");
             player.sendMessage(Color.MESSAGE + "Use /" + commandName + " help to check the available sub commands.");
         }
     }
 
     /**
      * Send the player a help file for the command.
      * @param page The page to display.
      */
     protected void sendHelp(final int page) {
         player.performCommand("help " + commandName + " " + page);
     }
 
     /**
      * Check if the target player is marked as AFK.
      * @param target Target player.
      * @return Is target player marked as AFK.
      */
     protected boolean notAfk(final SaveablePlayer target) {
         if(target.isAfk()) {
             player.sendMessage(Color.MESSAGE + "That player is currently AFK.");
             return false;
         } else {
             return true;
         }
     }
 
     /**
      * Check that the player can send a request.
      * @param target The requestee.
      * @return <code>true</code> if the player can send a request, <code>false</code> otherwise.
      */
     protected boolean canRequest(final SaveablePlayer target) {
         return noRequests(target) && notIgnored(target) && notAfk(target);
     }
 
     /**
      * Check that the player can teleport.
      * @return <code>true</code> if the player is free to teleport, <code>false</code> otherwise.
      */
     protected boolean canTP() {
         return notPinned() && notJailed();
     }
 
     protected boolean hasUndo(Session session) {
         if(session.hasUndo()) {
             return true;
         } else {
             player.sendMessage(Color.ERROR + "You have nothing to undo.");
             return false;
         }
     }
 
     /**
      * Get the targets shop.
      * @param target The player.
      * @return The targets shop if they own one, <code>null</code> otherwise.
      */
     protected Region getShop(final SaveablePlayer target) {
         final Region shop = UDSPlugin.getRegions(RegionType.SHOP).get(target.getName() + "shop");
         if(shop == null) {
             player.sendMessage(Color.ERROR + "That player does not own a shop.");
         }
         return shop;
     }
 
     /**
      * Check that the enchantment can accept the level.
      * @param enchantment Enchantment.
      * @param level Level to check.
      * @return <code>true</code> if the enchantment can accept the level, <code>false</code> otherwise.
      */
     protected boolean goodEnchantLevel(final Enchantment enchantment, final int level) {
         if(level <= enchantment.getMaxLevel()) {
             return true;
         } else {
             player.sendMessage(Color.ERROR + "The level you have chosen is too high.");
             return false;
         }
     }
 
     /**
      * Check that a price is both a valid price and that the player can afford it.
      * @param amount The price.
      * @return The price if the player can afford it, <code>-1</code> otherwise.
      */
     protected int getAffordablePrice(final String amount) {
         final int cash = parseInt(amount);
         if(cash > -1 && !canAfford(cash)) {
             return -1;
         }
         return cash;
     }
 
     /**
      * Get the players whisperer.
      * @return The players whisperer, <code>null</code> if the player is not whispering.
      */
     protected SaveablePlayer getWhisperer() {
         final SaveablePlayer target = player.getWhisperer();
         if(target == null) {
             player.sendMessage(Color.ERROR + "There is no one to send this message to.");
         }
         return target;
     }
 
     /**
      * Check if the player is in a chat room.
      * @return <code>true</code> if the player is in a chat room, <code>false</code> otherwise.
      */
     protected boolean inChatRoom() {
         if(player.getChatRoom() == null) {
             player.sendMessage(Color.ERROR + "You are not in any private chat rooms.");
             return false;
         } else {
             return true;
         }
     }
 
     /**
      * Get the players WorldEdit session.
      * @return The players WE session, if the player does not currently have a session one is created.
      */
     protected Session getSession() {
         return player.forceSession();
     }
 
     /**
      * Get a region by name.
      * @param regionName Region name.
      * @return The region or <code>null</code> if no region exists by this name.
      */
     protected Region getRegion(final String regionName) {
         final Region region = UDSPlugin.getRegions(RegionType.GENERIC).get(regionName);
         if(region == null) {
             player.sendMessage(Color.ERROR + "No region exists by that name.");
         }
         return region;
     }
 
     /**
      * Get a region flag by name.
      * @param name Region flag name.
      * @return The region flag if it exists, <code>null</code> otherwise.
      */
     protected RegionFlag getFlag(final String name) {
         final RegionFlag flag = RegionFlag.getByName(name);
         if(flag == null) {
             player.sendMessage(Color.ERROR + "That is not a valid region type.");
         }
         return flag;
     }
 
     /**
      * Get a region type by name.
      * @param name The region type name.
      * @return The region type if it exists, <code>null</code> otherwise.
      */
     protected RegionType getRegionType(final String name) {
         final RegionType type = RegionType.getByName(name);
         if(type == null) {
             player.sendMessage(Color.ERROR + "That is not a valid region type.");
         }
         return type;
     }
 
     /**
      * Check that the player has two World Edit points selected.
      * @param session The players session.
      * @return <code>true</code> if the session has two points, <code>false</code> otherwise.
      */
     protected boolean hasTwoPoints(final Session session) {
         if(session.getV1() == null || session.getV2() == null) {
             player.sendMessage(Color.ERROR + "You need to select two points.");
             return false;
         } else {
             return true;
         }
     }
 
     /**
      * Check that the player does not own a shop.
      * @return <code>true</code> if the players does not own a shop, <code>false</code> otherwise.
      */
     protected boolean noShop() {
         if(UDSPlugin.getRegions(RegionType.SHOP).containsKey(player.getName() + "shop")) {
             player.sendMessage(Color.ERROR + "You already own a shop.");
             return false;
         } else {
             return true;
         }
     }
 
     /**
      * Check that the target player is banned.
      * @param target Target player.
      * @return <code>true</code> if the target is banned, <code>false</code> otherwise.
      */
     protected boolean isBanned(final SaveablePlayer target) {
         if(target.isBanned()) {
             return true;
         } else {
             player.sendMessage(Color.ERROR + "That player is not banned.");
             return false;
         }
     }
 
     /**
      * Get the players currently pending request.
      * @return The players current request if it exists, <code>null</code> otherwise.
      */
     protected Request getRequest() {
         final Request request = UDSPlugin.getRequests().get(player.getName());
         if(request == null) {
             player.sendMessage(Color.ERROR + "You have no pending requests.");
         }
         return request;
     }
 
     /**
      * Get the players home region.
      * @return The players home region if it exists, <code>null</code> otherwise.
      */
     protected Region getHome() {
         final Region home = UDSPlugin.getRegions(RegionType.HOME).get(player.getName() + "home");
         if(home == null) {
             player.sendMessage(Color.ERROR + "You do not have a home.");
         }
         return home;
     }
 
     /**
      * Get a direction by name.
      * @param dir The direction name.
      * @return The direction if it exists, <code>null</code> otherwise.
      */
     protected Direction getDirection(final String dir) {
         final Direction direction = Direction.getByName(dir);
         if(direction == null) {
             player.sendMessage(Color.ERROR + "That is not a valid direction.");
         }
         return direction;
     }
 
     /**
      * Get a cardinal direction by name.
      * @param dir The direction name.
      * @return The cardinal direction if it exists, <code>null</code> otherwise.
      */
     protected Direction getCardinalDirection(final String dir) {
         final Direction direction = getDirection(dir);
         if(direction == null) {
             return null;
         } else {
             if(direction.isCardinal()) {
                 return direction;
             } else {
                 player.sendMessage(Color.ERROR + "You must choose a cardinal direction.");
                 return null;
             }
         }
     }
 
     /**
      * Check that the player is homeless.
      * @return <code>true</code> if the player does not have a home, <code>false</code> otherwise.
      */
     protected boolean noHome() {
         if(UDSPlugin.getRegions(RegionType.HOME).containsKey(player.getName() + "home")) {
             player.sendMessage(Color.ERROR + "You already have a home.");
             return false;
         } else {
             return true;
         }
     }
 
     /**
      * Check that the clan has no base region.
      * @param clan The clan to check.
      * @return <code>true</code> if the clan does not have a base, <code>false</code> otherwise.
      */
     protected boolean noBase(final Clan clan) {
         if(UDSPlugin.getRegions(RegionType.BASE).containsKey(clan.getName() + "base")) {
             player.sendMessage(Color.ERROR + "Your clan already has a base.");
             return false;
         } else {
             return true;
         }
     }
 
     /**
      * Get a clan by name.
      * @param name Clan name.
      * @return The clan if it exists, <code>null</code> otherwise.
      */
     protected Clan getClan(final String name) {
         final Clan clan = UDSPlugin.getClans().get(name);
         if(clan == null) {
             player.sendMessage(Color.ERROR + "That clan does not exist.");
         }
         return clan;
     }
 
     /**
      * Check that a player is clanless.
      * @return <code>true</code> if the player is clanless, <code>false</code> otherwise.
      */
     protected boolean isClanless() {
         if(player.getClan() == null) {
             return true;
         } else {
             player.sendMessage(Color.ERROR + "You are already in a clan.");
             return false;
         }
     }
 
     /**
      * Check that the target player is in the clan.
      * @param player Target player.
      * @param clan Clan to check.
      * @return <code>true</code> if the player is a member of the clan, <code>false</code> otherwise.
      */
     protected boolean isInClan(final SaveablePlayer player, final Clan clan) {
         if(player.getClan().equals(clan)) {
             return true;
         } else {
             player.sendMessage(Color.ERROR + "That player is not in your clan.");
             return false;
         }
     }
 
     /**
      * Check that no clan exists by this name.
      * @param name Clan name.
      * @return <code>true</code> if no clan exists by this name, <code>false</code> otherwise.
      */
     protected boolean notClan(final String name) {
         if(UDSPlugin.getClans().containsKey(name)) {
             player.sendMessage(Color.ERROR + "A clan already exists with that name.");
             return false;
         } else {
             return true;
         }
     }
 
     /**
      * Check this player is leader of the clan.
      * @param clan Clan to check.
      * @return <code>true</code> if this player is clan leader, <code>false</code> otherwise.
      */
     protected boolean isLeader(final Clan clan) {
         if(clan.getLeader().equals(player)) {
             return true;
         } else {
             player.sendMessage(Color.ERROR + "You must be clan leader to do this.");
             return false;
         }
     }
 
     /**
      * Check that a player is in a clan.
      * @return The player's clan or <code>null</code> if the player is clanless.
      */
     protected Clan getClan() {
         final Clan clan = player.getClan();
         if(clan == null) {
             player.sendMessage(Color.ERROR + "You are not in a clan.");
         }
         return clan;
     }
 
     /**
      * Check if the players clan has a base.
      * @param clan The players clan.
      * @return The clan's base or <code>null</code> if the clan does not have a base.
      */
     protected Region getBase(final Clan clan) {
         final Region region = UDSPlugin.getRegions(RegionType.BASE).get(clan.getName() + "base");
         if(region == null) {
             player.sendMessage(Color.ERROR + "Your clan does not have a base.");
         }
         return region;
     }
 
     /**
      * Check if a string has bad words in it.
      * @param string String to check.
      * @return <code>true</code> if the word was clean, <code>false</code> otherwise.
      */
     protected boolean noCensor(final String string) {
         if(Censor.noCensor(string)) {
             return true;
         } else {
             player.sendMessage(Color.ERROR + "You can't use bad words here.");
             return false;
         }
     }
 
     /**
      * Match an online player by name.
      * @param name Player name.
      * @return The player if they are online, <code>null</code> otherwise.
      */
     protected SaveablePlayer getMatchingOtherPlayer(final String name) {
         final SaveablePlayer target = getMatchingPlayer(name);
         if(target != null && notSelf(target)) {
             return target;
         } else {
             return null;
         }
     }
 
     /**
      * Check that no warp by this name exists.
      * @param warpName Warp name to check.
      * @return <code>true</code> if no warp exists by this name, <code>false</code> otherwise.
      */
     protected boolean notWarp(final String warpName) {
         if(UDSPlugin.getWarps().get(warpName) == null) {
             return true;
         } else {
             player.sendMessage(Color.ERROR + "A warp already exists called " + warpName + ".");
             return false;
         }
     }
 
     /**
      * Check that the player is in jail.
      * @return <code>true</code> if the player is in jail, <code>false</code> otherwise.
      */
     protected boolean isJailed() {
         if(player.isJailed()) {
             return true;
         } else {
             player.sendMessage(Color.ERROR + "You are not in jail.");
             return false;
         }
     }
 
     /**
      * Check that the player is in jail.
      * @param target Player to check.
      * @return <code>true</code> if the player is in jail, <code>false</code> otherwise.
      */
     protected boolean isJailed(final SaveablePlayer target) {
         if(target.isJailed()) {
             return true;
         } else {
             player.sendMessage(Color.ERROR + target.getNick() + " is not in jail.");
             return false;
         }
     }
 
     /**
      * Check that a material exists.
      * @param string Material to check.
      * @return The material if it exists, <code>null</code> otherwise.
      */
     @SuppressWarnings("deprecation")
     protected ItemStack getItem(final String string) {
         Material material;
         String matString = string.toUpperCase();
         byte data = 0;
         if(string.contains(":")) {
             if(string.split(":")[1].matches("[0-9][0-9]*")) {
                 data = Byte.parseByte(string.split(":")[1]);
             } else {
                 player.sendMessage(Color.ERROR + "That is not a valid data value.");
                 return null;
             }
             matString = string.split(":")[0].toUpperCase();
         }
         if(matString.matches("[0-9][0-9]*")) {
             material = Material.getMaterial(Integer.parseInt(matString));
         } else {
             material = Material.getMaterial(matString);
         }
         if(material == null) {
             final ShortItem item = ShortItem.getByName(matString);
             if(item == null) {
                 player.sendMessage(Color.ERROR + "That is not a valid item.");
                 return null;
             } else {
                 return item.toItemStack();
             }
         } else {
             return new ItemStack(material, 1, (short)0, data);
         }
     }
 
     /**
      * Check if an enchantment exists.
      * @param enchant Enchantment name.
      * @return The enchantment if it exists, <code>null</code> otherwise.
      */
     protected Enchantment getEnchantment(final String enchant) {
         final Enchantment enchantment = Enchantment.getByName(enchant.toUpperCase());
         if(enchantment == null) {
             player.sendMessage(Color.ERROR + "That is not a valid enchantment.");
             return null;
         } else {
             return enchantment;
         }
     }
 
     /**
      * Check if the player has the required rank.
      * @param rank Rank required.
      * @return <code>true</code> if the player has the required rank, <code>false</code> otherwise.
      */
     protected boolean hasRank(final PlayerRank rank) {
         if(player.getRank().compareTo(rank) >= 0) {
             return true;
         } else {
             player.sendMessage(Color.ERROR + "You don't have the rank required to do that.");
             return false;
         }
     }
 
     /**
      * Get the first region the player is currently in.
      * @return The first region the player is in or <code>null</code> if none found.
      */
     protected Region getCurrentRegion() {
         final Region region = player.getCurrentRegion(RegionType.CITY);
         if(region == null) {
             player.sendMessage(Color.ERROR + "You are not in a city.");
         }
         return region;
     }
 
     /**
      * Check this player is a room mate in this home.
      * @param home Home to check.
      * @return <code>true</code> if this player is a room mate in this home, <code>false</code> otherwise.
      */
     protected boolean isRoomie(final Region home) {
         if(home.hasMember(player)) {
             return true;
         } else {
             player.sendMessage(Color.ERROR + "You are not that players room mate.");
             return false;
         }
     }
 
     /**
      * Check the target player works in the shop.
      * @param target Player to check.
      * @param shop Shop to check.
      * @return <code>true</code> if the target player works in the shop, <code>false</code> otherwise.
      */
     protected boolean isWorker(final SaveablePlayer target, final Region shop) {
         if(shop.hasMember(target)) {
             return true;
         } else {
             player.sendMessage(Color.ERROR + "That player is not your worker.");
             return false;
         }
     }
 
     /**
      * Check the shop is not owned by another player.
      * @param shop Shop to check.
      * @return <code>true</code> if the shop is empty, <code>false</code> otherwise.
      */
     protected boolean isEmptyShop(final Region shop) {
         if(shop.getOwner() == null) {
             return true;
         } else {
             player.sendMessage(Color.ERROR + "Somebody already owns this shop.");
             return false;
         }
     }
 
     /**
      * Get the shop the player is standing in.
      * @return The shop the player is standing in or <code>null</code>.
      */
     protected Region getContainingShop() {
         final Region shop = player.getCurrentRegion(RegionType.SHOP);
         if(shop == null) {
             player.sendMessage(Color.ERROR + "You must be stood inside a shop to buy it.");
         }
         return shop;
     }
 
     /**
      * Check the target player is a room mate in the home.
      * @param target Player to check.
      * @param home Home region to check.
      * @return <code>true</code> if the player is a room mate, <code>false</code> otherwise.
      */
     protected boolean isRoomie(final SaveablePlayer target, final Region home) {
         if(home.hasMember(target)) {
             return true;
         } else {
             player.sendMessage(Color.ERROR + "That player is not your room mate.");
             return false;
         }
     }
 
     /**
      * Get the target players home.
      * @param target Target player.
      * @return The target players home if it exists, <code>null</code> otherwise.
      */
     protected Region getHome(final SaveablePlayer target) {
         final Region home = UDSPlugin.getRegions(RegionType.HOME).get(target.getName() + "home");
         if(home == null) {
             player.sendMessage(Color.ERROR + "That player does not have a home.");
         }
         return home;
     }
 
     /**
      * Check the target player is in the home region.
      * @param target Target player.
      * @param home Home region.
      * @return <code>true</code> if the player is in the home region, <code>false</code> otherwise.
      */
     protected boolean isInHome(final SaveablePlayer target, final Region home) {
         if(target.getLocation().toVector().isInAABB(home.getV1(), home.getV2())) {
             return true;
         } else {
             player.sendMessage(Color.ERROR + "That player is not in your home.");
             return false;
         }
     }
 
     /**
      * Checks that no region already exists with a given name.
      * @param name Name to check.
      * @return <code>true</code> if no region already exists with the given name, <code>false</code> otherwise.
      */
     protected boolean notRegion(final String name) {
         if(UDSPlugin.getRegions(RegionType.GENERIC).containsKey(name)) {
             player.sendMessage(Color.ERROR + "A protected area already exists with that name.");
             return false;
         } else {
             return true;
         }
     }
 
     /**
      * Checks if a city exists by this name.
      * @param cityName City to check.
      * @return The city if it exists, <code>null</code> otherwise.
      */
     protected Region getMatchingCity(final String cityName) {
         Region city;
         if((city = UDSPlugin.getRegions(RegionType.CITY).get(cityName)) != null) {
             return city;
         } else {
             if((city = UDSPlugin.getRegions(RegionType.CITY).matchKey(cityName)) != null) {
                 return city;
             } else {
                 player.sendMessage(Color.ERROR + "No city exists by that name.");
                 return null;
             }
         }
     }
 
     /**
      * Check that the player is not the mayor of the city region.
      * @param city City region.
      * @return <code>true</code> if the player is the mayor of the city, <code>false</code> otherwise.
      */
     protected boolean notMayor(final Region city) {
         if(city.isOwner(player)) {
             player.sendMessage(Color.ERROR + "You cannot do that while you are the mayor.");
             return false;
         } else {
             return true;
         }
     }
 
     /**
      * Checks first if the city exists then that the player is the mayor of the city.
      * @param cityName City to check.
      * @return The city if both the city exists and the player is the mayor, <code>null</code> otherwise.
      */
     protected Region getMunicipality(final String cityName) {
         Region city;
         if((city = getMatchingCity(cityName)) != null) {
             if(city.getOwner().equals(player)) {
                 return city;
             } else {
                 player.sendMessage(Color.ERROR + "You are not the mayor of that city.");
                 return null;
             }
         } else {
             return null;
         }
     }
 
     /**
      * Checks that a region has no overlaps with any other regions.
      * @param region Region to check for overlaps.
      * @return <code>true</code> if there are no overlaps with other regions, <code>false</code> otherwise.
      */
     protected boolean noOverlaps(final Region region) {
         for(Region test : UDSPlugin.getRegions(RegionType.GENERIC).values()) {
            if(test != region && test.hasOverlap(region)) {
                 player.sendMessage(Color.ERROR + "You cannot do that here, you are too close to another protected area.");
                 return false;
             }
         }
         return true;
     }
 
     /**
      * Checks that a player is not already engaged in a duel with another player.
      * @param target Player to check.
      * @return <code>true</code> if the player is not duelling, <code>false</code> otherwise.
      */
     protected boolean notDueling(final SaveablePlayer target) {
         if(target.isDuelling()) {
             player.sendMessage(Color.MESSAGE + "That player is already dueling someone else.");
             return false;
         } else {
             return true;
         }
     }
 
     /**
      * Check that the player is not targeting themselves.
      * @param target Target to check.
      * @return <code>true</code> if target and this player are distinct, <code>false</code> otherwise.
      */
     protected boolean notSelf(final SaveablePlayer target) {
         if(target.equals(player)) {
             player.sendMessage(Color.ERROR + "You cannot use that command on yourself.");
             return false;
         } else {
             return true;
         }
     }
 
     /**
      * Check if a player is in clan.
      * @return <code>true</code> if player is in a clan, <code>false</code> otherwise.
      */
     protected boolean isInClan() {
         if(player.isInClan()) {
             return true;
         } else {
             player.sendMessage(Color.ERROR + "You must be in a clan to do that.");
             return false;
         }
     }
 
     /**
      * Check that the player is not pinned due to attacking another player recently.
      * @return <code>true</code> if the player is not pinned, <code>false</code> otherwise.
      */
     protected boolean notPinned() {
         if(player.getLastDamageCaused() + UDSPlugin.getConfigLong(ConfigRef.PVP_TIME) < System.currentTimeMillis()) {
             return true;
         } else {
             player.sendMessage(Color.ERROR + "You can't do that at this time.");
             return false;
         }
     }
 
     /**
      * Check that this player is not in jail.
      * @return <code>true</code> if the player is not in jail, <code>false</code> otherwise.
      */
     protected boolean notJailed() {
         if(player.isJailed()) {
             player.sendMessage(Color.ERROR + "You cannot do this while you are in jail.");
             return false;
         } else {
             return true;
         }
     }
 
     /**
      * Check that the target player is not in jail.
      * @param target
      * @return <code>true</code> if the target player is not in jail, <code>false</code> otherwise.
      */
     protected boolean notJailed(final SaveablePlayer target) {
         if(target.isJailed()) {
             player.sendMessage(Color.ERROR + "You can't do this while that player is in jail.");
             return false;
         } else {
             return true;
         }
     }
 
     /**
      * Check that the target player has no requests pending.
      * @param target The player to check.
      * @return <code>true</code> if the target player has no requests pending, <code>false</code> otherwise.
      */
     protected boolean noRequests(final SaveablePlayer target) {
         if(UDSPlugin.getRequests().containsKey(target.getName())) {
             player.sendMessage(Color.ERROR + "That player already has a request pending.");
             return false;
         } else {
             return true;
         }
     }
 
     /**
      * Match an online player that is not this player.
      * @param name Player name.
      * @return The player if it matched and was not this player, <code>null</code> otherwise.
      */
     protected SaveablePlayer getMatchingOtherOnlinePlayer(final String name) {
         SaveablePlayer target;
         if((target = getMatchingOnlinePlayer(name)) != null && notSelf(target)) {
             return target;
         } else {
             return null;
         }
     }
 
     /**
      * Check that the target player is not being ignored by the command sender.
      * @param target Player to check.
      * @return <code>true</code> if the target player is not ignoring the command sender, <code>false</code> otherwise.
      */
     protected boolean notIgnored(final SaveablePlayer target) {
         if(target.isIgnoringPlayer(player)) {
             player.sendMessage(Color.ERROR + "This player can't be reached at this time.");
             return false;
         } else {
             return true;
         }
     }
 
     /**
      * Checks to see if a string is a positive number.
      * @param number String to check.
      * @return The number if it was one, -1 otherwise.
      */
     protected int parseInt(final String number) {
         if(number.matches(UDSPlugin.INT_REGEX)) {
             return Integer.parseInt(number);
         } else {
             player.sendMessage(Color.ERROR + "The number you entered was invalid.");
             return -1;
         }
     }
 
     /**
      * Checks if a player is online.
      * @param target Player to check.
      * @return <code>true</code> if the player is online, <code>false</code> otherwise.
      */
     protected boolean isOnline(final SaveablePlayer target) {
         if(target.isOnline()) {
             return true;
         } else {
             player.sendMessage(Color.ERROR + "That player is not online.");
             return false;
         }
     }
 
     /**
      * Matches a partial player name to the first player it finds.
      * @param partial Partial player name.
      * @return The first player matched or <code>null</code> if no players matched.
      */
     protected SaveablePlayer getMatchingPlayer(final String partial) {
         final String lowPartial = partial.toLowerCase();
         SaveablePlayer target = UDSPlugin.getOnlinePlayers().get(lowPartial);
         if(target != null) {
             return target;
         } else {
             for(SaveablePlayer test : UDSPlugin.getOnlinePlayers().values()) {
                 if(test.getNick().equalsIgnoreCase(lowPartial)) {
                     return test;
                 }
             }
             target = UDSPlugin.getOnlinePlayers().matchKey(lowPartial);
             if(target != null) {
                 return target;
             } else {
                 for(SaveablePlayer test : UDSPlugin.getOnlinePlayers().values()) {
                     if(test.getNick().toLowerCase().contains(lowPartial)) {
                         return test;
                     }
                 }
                 target = UDSPlugin.getPlayers().get(lowPartial);
                 if(target != null) {
                     return target;
                 } else {
                     for(SaveablePlayer test : UDSPlugin.getPlayers().values()) {
                         if(test.getNick().equalsIgnoreCase(lowPartial)) {
                             return test;
                         }
                     }
                     target = UDSPlugin.getPlayers().matchKey(lowPartial);
                     if(target != null) {
                         return target;
                     } else {
                         for(SaveablePlayer test : UDSPlugin.getPlayers().values()) {
                             if(test.getNick().toLowerCase().contains(lowPartial)) {
                                 return test;
                             }
                         }
                     }
                     player.sendMessage(Color.ERROR + "Cannot find a player by that name.");
                     return null;
                 }
             }
         }
     }
 
     /**
      * Match an online player by name.
      * @param partial Partial name.
      * @return A player matching the partial name if it can be found, <code>null</code> otherwise.
      */
     protected SaveablePlayer getMatchingOnlinePlayer(final String partial) {
         final String lowPartial = partial.toLowerCase();
         SaveablePlayer target = UDSPlugin.getOnlinePlayers().get(lowPartial);
         if(target!= null) {
             return target;
         } else {
             for(SaveablePlayer test : UDSPlugin.getOnlinePlayers().values()) {
                 if(test.getNick().equalsIgnoreCase(lowPartial)) {
                     return test;
                 }
             }
             target = UDSPlugin.getOnlinePlayers().matchKey(lowPartial);
             if(target != null) {
                 return target;
             } else {
                 for(SaveablePlayer test : UDSPlugin.getOnlinePlayers().values()) {
                     if(test.getNick().contains(lowPartial)) {
                         return test;
                     }
                 }
                 player.sendMessage(Color.ERROR + "Cannot find that player.");
                 return null;
             }
         }
     }
 
     /**
      * Matches a warp name from a partial string.
      * @param partial Partial warp name.
      * @return The warp matched or <code>null</code> if there were no matches.
      */
     protected Warp getWarp(final String partial) {
         Warp warp = UDSPlugin.getWarps().get(partial);
         if(warp != null) {
             return warp;
         } else {
             warp = UDSPlugin.getWarps().matchKey(partial);
             if(warp != null) {
                 return warp;
             } else {
                 player.sendMessage(Color.ERROR + "That warp point does not exist.");
                 return null;
             }
         }
     }
 
     /**
      * Checks if the player can afford a certain cost.
      * @param cost The cost to check.
      * @return <code>true</code> if the player can afford the cost, <code>false</code> otherwise.
      */
     protected boolean canAfford(final int cost) {
         if(player.canAfford(cost)) {
             return true;
         } else {
             player.sendMessage(Color.ERROR + "You do not have enough money to do that.");
             return false;
         }
     }
 
     /**
      * Checks if the player has a permission or is opped. The permission is appended to the string "udsplugin.".
      * @param perm The permission suffix.
      * @return <code>true</code> if player has the permission or is opped, <code>false</code> otherwise.
      */
     protected boolean hasPerm(final Perm perm) {
         if(player.hasPermission(perm) || player.isOp()) {
             return true;
         } else {
             player.sendMessage(Color.ERROR + "You do not have permission to do that.");
             return false;
         }
     }
 
     /**
      * Used when a player on the server executes a command.
      */
     public abstract void playerExecute();
 }
