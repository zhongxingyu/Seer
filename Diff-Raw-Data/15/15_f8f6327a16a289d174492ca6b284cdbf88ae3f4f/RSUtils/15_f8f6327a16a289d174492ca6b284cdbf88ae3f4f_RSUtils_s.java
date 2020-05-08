 package com.github.kuben.realshopping;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Chest;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.minecart.StorageMinecart;
 import org.bukkit.inventory.ItemStack;
 
 public class RSUtils {
 
     private RSUtils(){}//All static
 
     /*
      * 
      * Utils and formatting functions
      * 
      */
 
     private static String parseAliases(String str){
             String s = str.toLowerCase();
             for(String st:RealShopping.getSortedAliases()){
                     String replacement = "";
                     for(Integer i:RealShopping.getAliasesMap().get(st)){
                             if(!replacement.equals("")) replacement += ":";
                             replacement += i;
                     }
                     s = s.replaceAll(st, replacement);
             }
             return s;
     }
 
     /**
      * Accepts and parses item descriptors (for chests),
      * supports aliases, data values, stack sizes and multiple items.
      * 
      * Return an array the same size as the amount items (max. 27)
      */
     public static int[][] pullItems(String str) throws NumberFormatException {
         String[] strs = new String[27];
         int l = 0;
         for(String s:str.split(",")){//Repeat for every item
             int m = 1, i = 0;//m is multiplier, i is how many times the loop has multiplied
             if(s.contains("*")) m = Integer.parseInt(s.split("\\*")[1]);
             while(i < m && l < 27){//Add as many times as requested
                 strs[l] = s.split("\\*")[0];
                 l++;
                 i++;
             }
             if(i >= 27) break;
         }
         int[][] ids = new int[l][3];
         for(int i = 0;i < l;i++){
             int[] id = pullItem(strs[i]);
             ids[i][0] = id[0];
             if(id.length > 1) ids[i][1] = id[1];
             else ids[i][1] = 0;
             if(id.length > 2) ids[i][2] = id[2];
             else ids[i][2] = 0;//Full stack
         }
         return ids;
     }
     
     /**
      * Accepts and parses any single item descriptor
      * Supports aliases, data values and stack sizes.
      * Does not parse mulitple items ( , ) or ( * )
      * 
      * @return An array of the same size as the amount of ' : '
      */
     public static int[] pullItem(String str) throws NumberFormatException {
         String s = parseAliases(str);//Check for aliases
 
         //Parse resulting string
         int[] id = new int[s.split(":").length];
         int i = 0;
         for(String ss:s.split(":")){
                 id[i] = Integer.parseInt(ss);
                 i++;
         }
         return id;
     }
 
     public static Price pullPrice(String str, Player ply){//No commas on this one
         String[] s = new String[2];
         if(parseAliases(str).contains(":"))
             s = parseAliases(str).split(":");
         else {
             s[0] = parseAliases(str);
             s[1] = "0";
         }
         byte data = Byte.parseByte(s[1]);
         int id = Integer.parseInt(s[0]);
         if(id < 0) {
             return new Price(ply.getItemInHand());
         }
         if(s.length > 1){
                 return new Price(id,data);
         } else return new Price(id);
     }
 
     /**
      * Builds an object[] containing Price, cost and eventual minmax.
      * Its structure is o[0] = Price, o[1] = Integer[].
      * The Integer[] contains cost in [0] and, if present, min and max in [1] and [2].
      * @return Array object with Price and Integer[] of costs, min and max.
      */
     public static Object[] pullPriceCostMinMax(String str, Player ply){//No commas on this one
         String[] s = parseAliases(str).split(":");
         Price p = null;
         Integer[] i = null;
         byte data = 0;
 
         //ID:[DATA]:PRICE:[MIN]:[MAX]
         switch(s.length){
             case 5:
                 data = Byte.parseByte(s[1]);
             case 4:
                 i = new Integer[]{(int)(Float.parseFloat(s[s.length-3])*100), (int)(Float.parseFloat(s[s.length-2])*100), (int)(Float.parseFloat(s[s.length-1])*100)};
                 break;
             case 3:
                 data = Byte.parseByte(s[1]);
             case 2:
                 i = new Integer[]{(int)(Float.parseFloat(s[s.length - 1])*100)};
                 break;
             case 1:
                 return null;
             default:
                 i = new Integer[]{-1};
                 break;
         }
         int id = Integer.parseInt(s[0]);
 
         if( id < 0) 
             p = new Price(ply.getItemInHand());
         else 
             p = new Price(id, data);
         return new Object[]{p, i};
     }
 
     /**
      * Returns an object containing Price and minmax.
      * The structure is o[0] = Price, o[1] = Integer[] minmax
      */
     public static Object[] pullPriceMinMax(String str, Player ply){//No commas on this one
             String s[] = parseAliases(str).split(":");
             Price p = null;
             Integer i[] = new Integer[]{(int)(Float.parseFloat(s[s.length-2])*100), (int)(Float.parseFloat(s[s.length-1])*100)};
             int id = Integer.parseInt(s[0]); 
             if( id < 0) { //control that I want hand held item
                     p = new Price(ply.getItemInHand());
                     return new Object[]{p,i};
             }
             if(s.length > 3)//Has data
                     p = new Price(id,Byte.parseByte(s[1]));
             else p = new Price(id);
             return new Object[]{p, i};
     }
 
     public static String formatItemStackToMess(ItemStack[] IS){
             String str = "";
             int newLn = 0;
             for(ItemStack iS:IS){
                     if(iS != null){
                             String tempStr = "[" + ChatColor.RED + iS.getType() + (RealShopping.isTool(iS.getType())
                                                             ?ChatColor.RESET + LangPack.WITH + ChatColor.GREEN + (RealShopping.getMaxDur(iS.getType()) - iS.getDurability())
                                                             + ChatColor.RESET +  "/" + ChatColor.GREEN + RealShopping.getMaxDur(iS.getType()) + ChatColor.RESET + LangPack.USESLEFT + "] "
                                                             :ChatColor.RESET + " * " + ChatColor.GREEN + iS.getAmount() + ChatColor.RESET + "] " + ChatColor.BLACK + ChatColor.RESET);
                             if((str + tempStr).substring(newLn).length() > 96){//84+12
                                     str += "\n";
                                     newLn = str.length();
                                     str += tempStr;
                             } else str += tempStr;
                     }
             }
             return str;
     }
 
     public static String formatPlayerListToMess(String[] formStr){
             String str = "";
             int newLn = 0;
             for(String s:formStr){
                     boolean online = (Bukkit.getServer().getPlayerExact(s)==null)?false:true;
                     String tempStr = "[" + (online?ChatColor.GREEN:ChatColor.RESET) + s + ChatColor.RESET + "] ";
                     if((str + tempStr).substring(newLn).length() > 88){//84+4
                             if(str.split("\n").length < 7){
                                     str += "\n";
                                     newLn = str.length();
                                     str += tempStr;
                             } else {
                                     str += "...";
                             }
                     } else str += tempStr;
             }
             return str;
     }
 
     public static String locAsString(Location l){
             return l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ();
     }
 
     public static Location stringToLoc(String world, String s){
             return new Location(Bukkit.getServer().getWorld(world), Double.parseDouble(s.split(",")[0].trim()), Double.parseDouble(s.split(",")[1].trim()), Double.parseDouble(s.split(",")[2].trim()));
     }
 
     /**
      * Formats an integer representing any amount of seconds to a string in the format of "Xd Yh Wm Us"
      * where X is the number of days, Y number of hours, W number of minutes and U number of seconds.
      * If any of those is 0, it is not shown.
      * @param period How many seconds
      * @return a simple representation of the time.
      */
     public static String secsToDHMS(int period){
         int remainder = period;
         String STRING = "";
         if(remainder >= 86_400){//How many days
             int days = (int) Math.floor(remainder / 86_400);
             remainder = remainder % 86_400;
             STRING += days + "d ";
         }
         if(remainder >= 3600){//How many hours
             int hrs = (int) Math.floor(remainder / 3600);
             remainder = remainder % 3600;
             STRING += hrs + "h ";
         }
         if(remainder >= 60){//How many minutes
             int mins = (int) Math.floor(remainder / 60);
             remainder = remainder % 60;
             STRING += mins + "m ";
         }
         if(remainder > 0) STRING += remainder + "s";
         return STRING;
     }
     
     /*
      * 
      * Booleans and gets
      * 
      */
 
     public static boolean allowTpOutOfStore(Location l){
             Location[] keys = RealShopping.getTpLocsKeysArray();
             boolean allow = RealShopping.isTpLocBlacklist()?true:false;
             for(Location loc:keys){
                     if(l.getWorld() == loc.getWorld());
                     int xDif = l.getBlockX() - loc.getBlockX();
                     int yDif = l.getBlockY() - loc.getBlockY();
                     int zDif = l.getBlockZ() - loc.getBlockZ();
                     double temp = Math.sqrt(Math.pow(Math.max(xDif, xDif * -1), 2) + Math.pow(Math.max(yDif, yDif * -1), 2) + Math.pow(Math.max(zDif, zDif * -1), 2));
                     if(temp <= RealShopping.getTpLoc(loc)){//Is in zone
                             if(allow) allow = false;
                             else allow = true;
                             break;
                     }		
             }
             return allow;
     }
 
     public static boolean isChestProtected(Location l){
             for(Shop temp:RealShopping.getShops()){
                     if(temp.isProtectedChest(l)) return true;
             }
             return false;
     }
 
     public static Location[] getNearestTpLocs(Location loc, int maxAmount){
             Location[] keys = RealShopping.getTpLocsKeysArray();
             Location[] nearest = null;
             Map<Location, Double> locDist = new HashMap<>();
             for(Location l:keys)
                     if(loc.getWorld().equals(l.getWorld()))
                             locDist.put(l, loc.distance(l));
 
             if(!locDist.isEmpty()){
                     ValueComparator bvc =  new ValueComparator(locDist);
                     TreeMap<Location,Double> sorted_map = new TreeMap<>(bvc);
                     sorted_map.putAll(locDist);
 
 
                     if(sorted_map.size() < maxAmount) nearest = new Location[sorted_map.size()];
                     else nearest = new Location[maxAmount];
 
                     keys = sorted_map.keySet().toArray(new Location[0]);
                     for(int i = 0;i < nearest.length;i++){
                             nearest[i] = keys[i];
                     }
             }
 
             return nearest;
     }
 
     /*
      * 
      * Cart-related functions
      * 
      */
 
     public static Shop[] getOwnedShops(String player){
         List<Shop> shopList = new ArrayList<>();
         for(Shop shop:RealShopping.getShops())
             if(shop.getOwner().toLowerCase().equals(player.toLowerCase())) shopList.add(shop);
         return shopList.toArray(new Shop[0]);
     }
     /**
      * We check for minecarts near the pay block.
      * @param l Location where to check for shopping carts.
      * @return An array containing all storage carts found.
      */
     public static StorageMinecart[] checkForCarts(Location l){
             Location[] aL = new Location[]{	l.clone().subtract(0, 1, 0)		// [6][7][8]
                                                                             , l.clone().subtract(-1, 1, 0)	// [5][0][1]
                                                                             , l.clone().subtract(-1, 1, -1)	// [4][3][2]
                                                                             , l.clone().subtract(0, 1, -1)
                                                                             , l.clone().subtract(1, 1, -1)
                                                                             , l.clone().subtract(1, 1, 0)
                                                                             , l.clone().subtract(1, 1, 1)
                                                                             , l.clone().subtract(0, 1, 1)
                                                                             , l.clone().subtract(-1, 1, 1)};
             Block[] b = new Block[]{	aL[0].getBlock()
                                                                     , aL[1].getBlock()
                                                                     , aL[2].getBlock()
                                                                     , aL[3].getBlock()
                                                                     , aL[4].getBlock()
                                                                     , aL[5].getBlock()
                                                                     , aL[6].getBlock()
                                                                     , aL[7].getBlock()
                                                                     , aL[8].getBlock()};
             StorageMinecart firstCart = null;//First cart found on first block
             if(Config.isAllowFillChests() && Config.isCartEnabledW(l.getWorld().getName())){
             Object[] mcArr = l.getWorld().getEntitiesByClass(StorageMinecart.class).toArray();
             String rails = "";
             for(int i = 0;i < b.length;i++){//Get rails in area
                     if(b[i].getType() == Material.RAILS || b[i].getType() == Material.POWERED_RAIL || b[i].getType() == Material.DETECTOR_RAIL){
                             rails += "," + i;
                     }
             }
             if(!rails.equals("")){
                     for(int j = 1;j < rails.split(",").length;j++){//Repeat for every rail in area until a minecart is found
                             int areaInt = Integer.parseInt(rails.split(",")[j]);
                             int k = 0;
                             boolean hasCart = false;
                             for(;k < mcArr.length;k++){//Repeat until a minecart is found on the block, or no minecarts are found
                                     if(((StorageMinecart)mcArr[k]).getLocation().getBlock().getLocation().equals(aL[areaInt])){//If minecart is on the rail
                                             hasCart = true;
                                             break;
                                     }
                             }
                             if(hasCart){
                                     firstCart = (StorageMinecart)mcArr[k];
                                     break;
                             }
                     }
             }
             }
             if(firstCart == null) return new StorageMinecart[0];
             else return new StorageMinecart[]{firstCart};//TODO add more carts
     }
 
     public static boolean shipCartContents(StorageMinecart sM, Player p) {
             List<ItemStack> cartInv = new LinkedList<>();
             for(ItemStack i:sM.getInventory().getContents()){
                 if(i!= null) cartInv.add(i);
             }
 
             RSPlayerInventory pinv = RealShopping.getPInv(p);
             Shop tempshop = pinv.getShop();
             Map<Price, Integer> bought = pinv.getBought();
             //The shipment is invalid when cart is empty.
             if(cartInv.isEmpty() || bought.isEmpty()){
                 p.sendMessage(ChatColor.RED + LangPack.YOUCANTSHIPANEMPTYCART);
                 return true;
             }
             // I take note on every item bought from the player.
             if(!bought.isEmpty()){
                 List<ItemStack> rem = new ArrayList<>();
                 for(ItemStack i:cartInv){
                     if(i == null) continue;
                     Price x = new Price(i);
                     if(tempshop.hasPrice(x) && bought.containsKey(x)){ //Player owns this item (bought)
                         int amount = RealShopping.isTool(i.getType())?1:i.getAmount();
                         ItemStack r = i.clone();
                         if(amount > bought.get(x)){
                             r.setAmount(bought.get(x));
                         } else {
                             r.setAmount(amount);
                         }
                         rem.add(r);
                         pinv.delBought(x,amount);
                     }
                 }
                 cartInv = updateCartInv(cartInv,rem);
 
                 sM.getInventory().setContents(cartInv.toArray(new ItemStack[0]));
 
                 //Ship
                 RealShopping.addShippedToCollect(p.getName(), new ShippedPackage(rem.toArray(new ItemStack[0]), 0, sM.getLocation()));
                 p.sendMessage(ChatColor.GREEN + LangPack.PACKAGEWAITINGTOBEDELIVERED);
 
                 //Update bought inventory
                 for(Price is:bought.keySet()){
                     pinv.delBought(is, bought.get(is));
                 }
 
             } else p.sendMessage(ChatColor.RED + LangPack.YOUHAVENTBOUGHTANYTHING);
 
             return true;
     }
 
     private static List<ItemStack> updateCartInv(List<ItemStack> cart,List<ItemStack> rem){
         List<ItemStack> newInv = new LinkedList<>();
  
         for(ItemStack i : cart){
             for(ItemStack f : rem){
                 if(f.isSimilar(i))
                     if(f.getAmount() <= i.getAmount()){
                         i.setAmount(i.getAmount() - f.getAmount());
                         if(i.getAmount() == 0)
                             break;
                     }
             }
             if(i.getAmount() == 0) continue;
             newInv.add(i);
         }
         return newInv;
     }
     public static boolean collectShipped(Location l, Player p, int id) {
         if(l.getBlock().getState() instanceof Chest){
             if(!RealShopping.hasPInv(p) || RealShopping.getPInv(p).getShop().getOwner().equals(p.getName())){
                 if(RealShopping.hasShippedToCollect(p.getName())){
                     if(RealShopping.getShippedToCollectAmount(p.getName()) >= id){
                         boolean cont = false;
                         int cost = 0;
                         if(Config.getZoneArray().length > 0){
                             int i = 0;
                             if(p.getLocation().getWorld().equals(RealShopping.getShippedToCollect(p.getName(), id - 1).getLocationSent().getWorld())){//Same world
                                 double dist = p.getLocation().distance(RealShopping.getShippedToCollect(p.getName(), id - 1).getLocationSent());
                                while(i < Config.getZoneArray().length && dist > Config.getZoneArray()[i].getBounds() && dist != -1){
                                         i++;
                                 }
                             } else {
                                while(i < Config.getZoneArray().length && Config.getZoneArray()[i].getBounds() > -1){
                                     i++;
                                 }
                             }
 
                             if(Config.getZoneArray()[i].getPercent() == -1)
                                     cost = (int) (Config.getZoneArray()[i].getCost()*100);
                             else {
                                     cost = (int) (RealShopping.getShippedToCollect(p.getName(), id - 1).getCost() * Config.getZoneArray()[i].getPercent()/100f);
                             }
                             if(RSEconomy.getBalance(p.getName()) >= cost/100f) cont = true;
                         } else cont = true;
 
                         if(cont){
                             RSEconomy.withdraw(p.getName(), cost/100f);
                             p.sendMessage(ChatColor.GREEN + "" + cost/100f + LangPack.UNIT + LangPack.WITHDRAWNFROMYOURACCOUNT);
                             ItemStack[] contents = ((Chest)l.getBlock().getState()).getBlockInventory().getContents();
                             for(ItemStack tempIS:contents) if(tempIS != null) p.getWorld().dropItem(p.getLocation(), tempIS);
 
                             ((Chest)l.getBlock().getState()).getBlockInventory().setContents(RealShopping.getShippedToCollect(p.getName(), id - 1).getContents());
                             p.sendMessage(ChatColor.GREEN + LangPack.FILLEDCHESTWITH);
                             p.sendMessage(formatItemStackToMess(RealShopping.getShippedToCollect(p.getName(), id - 1).getContents()));
                             RealShopping.removeShippedToCollect(p.getName(), id - 1);
                             return true;
                         } else p.sendMessage(ChatColor.RED + LangPack.YOUCANTAFFORDTOPAYTHEDELIVERYFEEOF + cost);
                     } else p.sendMessage(ChatColor.RED + LangPack.THERESNOPACKAGEWITHTHEID + id);
                 } else p.sendMessage(ChatColor.RED + LangPack.YOUHAVENTGOTANYITEMSWAITINGTOBEDELIVERED);
             } else p.sendMessage(ChatColor.RED + LangPack.YOUCANTCOLLECT_YOUDONOTOWN);
         } else p.sendMessage(ChatColor.RED + LangPack.THEBLOCKYOUSELECTEDISNTACHEST);
         return false;
     }
 
     /*
      * 
      * Punishment functions
      * 
      */
 
 
     public static void punish(Player p){//TODO add more
         
         p.sendMessage(ChatColor.RED + LangPack.TRYINGTOCHEATYOURWAYOUT);
         
         switch(Config.getPunishment()){
             case "hell":
                 if(!Config.isKeepstolen()){
                     RSUtils.returnStolen(p);
                 }
                 RealShopping.removePInv(p);
                 RealShopping.loginfo(p.getName() + LangPack.TRIEDTOSTEALFROMTHESTORE);
                 Location dropLoc2 = Config.getDropLoc().clone().add(1, 0, 0);
                 if(p.teleport(Config.getHellLoc().clone().add(0.5, 0, 0.5))){
                         RealShopping.loginfo(p.getName() + LangPack.WASTELEPORTEDTOHELL);
                         p.sendMessage(ChatColor.RED + LangPack.HAVEFUNINHELL);
                         Block block = p.getWorld().getBlockAt(Config.getDropLoc());
                         if(block.getType() != Material.CHEST) 
                             block.setType(Material.CHEST);
                         BlockState blockState = block.getState();
 
                         block = p.getWorld().getBlockAt(dropLoc2);
                         if(block.getType() != Material.CHEST) 
                             block.setType(Material.CHEST);
 
                         BlockState blockState2 = block.getState();
                         ItemStack[] pS = p.getInventory().getContents();
                         if(blockState instanceof Chest) {
                             Chest chest = (Chest)blockState;
                             ItemStack[] iS = new ItemStack[27];
                             for (int i = 0;i<17;i++) {
                                 iS[i] = pS[i+9];
                             }
                             chest.getBlockInventory().clear();
                             chest.getBlockInventory().setContents(iS);
                         }
                         if(blockState2 instanceof Chest) {
                            Chest chest = (Chest)blockState;
                            ItemStack[] iS = new ItemStack[9];
                            System.arraycopy(pS, 0, iS, 0, 9);
                            chest.getBlockInventory().clear();
                            chest.getBlockInventory().setContents(iS);
                            chest.getBlockInventory().addItem(p.getInventory().getArmorContents());
                         }
                         p.getInventory().clear();
                         p.getInventory().setArmorContents(new ItemStack[]{null, null, null, null});
                 } else {
                         p.getInventory().clear();
                         p.getInventory().setArmorContents(new ItemStack[]{null, null, null, null});
                 }
                 break;
             case "jail":
                 if(!Config.isKeepstolen()){
                         RSUtils.returnStolen(p);
                 }
                 RealShopping.jailPlayer(p);
                 RealShopping.removePInv(p);
                 RealShopping.loginfo(p.getName() + LangPack.TRIEDTOSTEALFROMTHESTORE);
                 if(p.teleport(Config.getJailLoc().clone().add(0.5, 0, 0.5))) {
                         p.sendMessage(LangPack.YOUWEREJAILED);
                         RealShopping.loginfo(p.getName() + LangPack.WASJAILED);
                 }
                 break;
             default:
                 if(!Config.isKeepstolen()){
                     RSUtils.returnStolen(p);
                 }
                 break;
         }
     }
 
     public static void returnStolen(Player p){
         Map<Price, Integer> stolen = RealShopping.getPInv(p).getStolen();
         Map<Price, Integer> stolen2 = new HashMap<>(stolen);
 
         //Remove stolen items from players inventory
         ItemStack[][] playerInv = new ItemStack[][]{p.getInventory().getContents(), p.getInventory().getArmorContents()};
         ItemStack[][] newPlayerInv = new ItemStack[][]{new ItemStack[playerInv[0].length], new ItemStack[playerInv[1].length]};
         for(int j = 0;j < 2;j ++){
             for(int i = 0;i < playerInv[j].length;i++){
                 ItemStack x = playerInv[j][i];
                 if(x != null){
                     Price tempPI = new Price(x);
                     if(stolen.containsKey(tempPI)){//Has stolen item
                         int diff = stolen.get(tempPI) - (RealShopping.isTool(x.getType())?1:x.getAmount());
                         if(diff >= 0){//If + then even more stolen left
                             stolen.put(tempPI, diff);
                             x = null;
                         } else {//If negative then no more stolen thing in inventory
                             x.setAmount(x.getAmount() - stolen.get(tempPI));
                             stolen.remove(tempPI);
                         }
                     }
                 }
                 newPlayerInv[j][i] = x;
             }
         }
         p.getInventory().setContents(newPlayerInv[0]);
         p.getInventory().setArmorContents(newPlayerInv[1]);
 
         String own = RealShopping.getPInv(p).getShop().getOwner();
         if(!own.equals("@admin")){//Return items if player store.
             for(Price key:stolen2.keySet()){
                 ItemStack tempIS = key.toItemStack();
                 if(RealShopping.isTool(tempIS.getType())){
                     if(stolen2.get(key) > RealShopping.getMaxDur(tempIS.getType()))
                         while(RealShopping.getMaxDur(tempIS.getType()) < stolen2.get(key)){//If more than one stack/full tool
                             RealShopping.getPInv(p).getShop().addToClaim(tempIS.clone());
                             stolen2.put(key, stolen2.get(key) - RealShopping.getMaxDur(tempIS.getType()));
                         }
                     tempIS.setDurability((short) (RealShopping.getMaxDur(tempIS.getType()) - stolen2.get(key)));
                     RealShopping.getPInv(p).getShop().addToClaim(tempIS);
                 } else {
                     if(stolen2.get(key) > Material.getMaterial(key.getType()).getMaxStackSize())
                         while(Material.getMaterial(key.getType()).getMaxStackSize() < stolen2.get(key)){//If more than one stack/full tool
                             ItemStack tempIStemp = tempIS.clone();
                             tempIStemp.setAmount(Material.getMaterial(key.getType()).getMaxStackSize());
                             RealShopping.getPInv(p).getShop().addToClaim(tempIStemp);
                             stolen2.put(key, stolen2.get(key) - Material.getMaterial(key.getType()).getMaxStackSize());
                         }
                     tempIS.setAmount(stolen2.get(key));
                     RealShopping.getPInv(p).getShop().addToClaim(tempIS);
                 }
             }
         }
     }
 
     public static Map<Price, Integer> joinMaps(Map<Price,Integer> uno, Map<Price,Integer> dos){//Preserves old values
         Map<Price, Integer> retval = new HashMap<>(uno);
         for(Price p:dos.keySet()){
             if(retval.containsKey(p)) retval.put(p, retval.get(p) + dos.get(p));
             else retval.put(p, dos.get(p));
         }
         return retval;
     }
 	
     /**
      * Formats an integer to a string with the ordinal number and the correct suffix 
      * @param value The integer which is the ordinal number. Example: 2
      * @return The finished string. Example: "2nd" (when using the English LangPack)
      */
     public static String formatNum(int value) {
         int hunRem = value % 100;
         int tenRem = value % 10;
         if (hunRem - tenRem == 10) {
                 return value + LangPack.X_TH;
         }
         switch (tenRem) {
         	case 1:
                 return value + LangPack.X_ST;
         	case 2:
                 return value + LangPack.X_ND;
         	case 3:
                 return value + LangPack.X_RD;
         	default:
                 return value + LangPack.X_TH;
         }
     }
 
 
     public static int getTimeInt(String s){//In seconds
             if(s.equals("hour")) return 3600;
             else if(s.equals("day")) return 86400;
             else if(s.equals("week")) return 604800;
             else if(s.equals("month")) return 2592000;
             else return Integer.parseInt(s);
     }
 
     public static String getTimeString(int t){
 		if(t == 3600) return "hour";
 		else if(t == 86400) return "day";
 		else if(t == 604800) return "week";
 		else if(t == 2592000) return "month";
 		else return t + "";
 	}
 }
 
 class StringLengthComparator implements Comparator<String>{
     public int compare(String o1, String o2) {//Longest string first
       if (o1.length() > o2.length()) {
          return -1;
       } else {
          return 1;
       }
     }
 }
