 package elxris.SpiceCraft.Objects;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.MemoryConfiguration;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryDragEvent;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.event.server.PluginDisableEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.InventoryView;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.EnchantmentStorageMeta;
 import org.bukkit.inventory.meta.SkullMeta;
 import elxris.SpiceCraft.SpiceCraft;
 import elxris.SpiceCraft.Utils.Archivo;
 import elxris.SpiceCraft.Utils.Chat;
 import elxris.SpiceCraft.Utils.Econ;
 import elxris.SpiceCraft.Utils.Strings;
 import elxris.SpiceCraft.Objects.FactoryGui;
 
 public class Factory implements Listener {
     private static Archivo file, fileUser;
     private static FileConfiguration fc, fcUser, volatil;
     private MemoryConfiguration paths;
     private int VEL, MIDDLE;
     private long FRECUENCY;
     public double MULTIPLIER, SELLRATE, USERMULTIPLIER, TAXPERREMOVEDSTACK;
     private boolean VARIABLE, DEFAULTUSERSELL, DEFAULTUSERBUY;
     private String shopName;
     private int EUCLIDE[];
     public Factory() {
         init();
         new FactoryGui(this);
         EUCLIDE = new int[(VEL*2)+1];
         for(int i = 1; i <= VEL*2; i++){
             int eu = (int)Math.exp(VEL*2-i+1);
             EUCLIDE[i] = eu;
         }
         MIDDLE = (getEuclide(VEL)+getEuclide(VEL+1))/2;
     }
     private void init() {
         FileConfiguration config = SpiceCraft.plugin().getConfig();
         VEL = config.getInt("shop.vel", 8);
         if(VEL < 1){
             VEL = 1;
         }
         FRECUENCY = config.getLong("shop.freq", 1);
         if(FRECUENCY < 1){
             FRECUENCY = 1;
         }
         FRECUENCY *= 60*1000;
         MULTIPLIER = config.getDouble("shop.multiplier", 1.0d);
         if(MULTIPLIER < 0){
             MULTIPLIER = 1;
         }
         USERMULTIPLIER = config.getDouble("shop.userMultiplier", 0.8d);
         SELLRATE = config.getDouble("shop.sellRate", 0.6d);
         VARIABLE = config.getBoolean("shop.variable");
         DEFAULTUSERSELL = config.getBoolean("shop.defaultUserSell");
         DEFAULTUSERBUY = config.getBoolean("shop.defaultUserBuy");
         TAXPERREMOVEDSTACK = config.getDouble("shop.taxPerRemovedStack", 100d);
     }
     private void update(String item){
         long now = getSystemTimeHour();
         long time = getTimeHour(item);
         if(time >= now){
             return;
         }
         double count = getCount(item);
         int vel = getVel(item);
         for(;time < now; time += FRECUENCY){
             vel = calculateEuclide(count);
             if(count < MIDDLE){
                 count += vel;
             }else{
                count -= (VEL*2+1)-vel;
             }
         }
         setTime(item, time);
         setCount(item, count);
         setVel(item, vel);
     }
     private void setTime(String item, long time) {
         getCache().set("item."+item+".time", time);
         save();
     }
     private long getTime(String item) {
         isSet("item."+item+".time", getSystemTimeHour());
         return getCache().getLong("item."+item+".time");
     }
     private long getTimeHour(String item) {
         long time = getTime(item);
         return time - (time % FRECUENCY);
     }
     private long getSystemTime(){ // Obtiene el tiempo del sistema.
         return System.currentTimeMillis();
     }
     private long getSystemTimeHour(){ // Obtiene el tiempo del sistema menos el resto de la frecuencia.
         long time = getSystemTime();
         return time - (time % FRECUENCY);
     }
     private void setCount(String item, double count){
         getCache().set("item."+item+".count", count);
         getTime(item);
         save();
     }
     private double getCount(String item){
         isSet("item."+item+".count", MIDDLE);
         return getCache().getDouble("item."+item+".count");
     }
     private void addCount(String item, double count){
         setCount(item, getCount(item)+count);
     }
     private void addCountRecursive(String item, double count){
         Map<String, Double> map = getDepends(item);
         for(String s: map.keySet()){
             addCount(s, count*map.get(s));
         }
     }
     private void setVel(String item, int vel){
         getCache().set("item."+item+".vel", vel);
         save();
     }
     private int getVel(String item){
         if(VARIABLE){
             isSet("item."+item+".vel", VEL);
             return getCache().getInt("item."+item+".vel");
         }else{
             return VEL;
         }
     }
     private double getPriceData(String item){
         return getCache().getDouble("item."+item+".price", 0.0d);
     }
     public double getPrice(String item, int acceleracion){
         Map<String, Double> map = getDepends(item);
         double price = 0;
         for(String s: map.keySet()){
             // Cantidad * Precio * Razon * Multiplicador
             price += map.get(s) * getPriceData(s) * getRazonPrecio(s, acceleracion);
         }
         return price*MULTIPLIER;
     }
     public double getPrice(String item){
         return getPrice(item, 0);
     }
     private double getRazonPrecio(String item, int acceleracion){
         double vel = (double)getVel(item)+acceleracion;
         if(vel < 1){
             vel = 1d;
         }
         return vel/VEL;
     }
     public double getPrecio(String item, int cantidad, int acceleracion){
         double r = getPrice(item, acceleracion)*(double)cantidad;
         if(r < 0){
             r = 0;
         }
         return r;
     }
     private double getPrecio(String item, int cantidad){
         return getPrecio(item, cantidad, 0);
     }
     private int getId(String item){
         return getCache().getInt("item."+item+".id");
     }
     private int getData(String item){
         return getCache().getInt("item."+item+".data");
     }
     public boolean getUserBuy(Player p, String item){
         // Si tiene master
         if(p.hasPermission("spicecraft.shop.master")){
             return true;
         }else{
             // Si tiene puesto un permiso especial de objeto.
             if(p.isPermissionSet("spicecraft.shop.itemBuy."+item)){
                 // Devuelve el permiso
                 return p.hasPermission("spicecraft.shop.itemBuy."+item);
             }else{
                 // Si no puede comprar.
                 if(p.hasPermission("spicecraft.shop.server.buy")){
                     // Devuelve el objeto
                     return getCache().getBoolean("item."+item+".userBuy", DEFAULTUSERBUY);
                 }else{
                     return false;
                 }
             }
         }
     }
     public boolean getUserSell(Player p, String item){
      // Si tiene master
         if(p.hasPermission("spicecraft.shop.master")){
             return true;
         }else{
             // Si tiene puesto un permiso especial de objeto.
             if(p.isPermissionSet("spicecraft.shop.itemSell."+item)){
                 // Devuelve el permiso
                 return p.hasPermission("spicecraft.shop.itemSell."+item);
             }else{
                 // Si no puede vender.
                 if(p.hasPermission("spicecraft.shop.server.sell")){
                     // Devuelve el objeto
                     return getCache().getBoolean("item."+item+".userSell", DEFAULTUSERSELL);
                 }else{
                     return false;
                 }
             }
         }
     }
     private int getRecipieMultiplie(String item){
         return getCache().getInt("item."+item+".recipieMultiplie", 1);
     }
     private Map<String, Double> getDepends(String item){
         Map<String, Double> mapa = new HashMap<String, Double>();
         update(item);
         mapa.put(item, 1d);
         if(!getCache().isSet("item."+item+".depend")){ // Si no hay dependencias
             return mapa;
         }
         ConfigurationSection memory = getCache().getConfigurationSection("item."+item+".depend");
         int multiplie = getRecipieMultiplie(item);
         for(String s: memory.getKeys(false)){
             Map<String, Double> dep = getDepends(s);
             for(String key: dep.keySet()){
                 double d = (dep.get(key)*memory.getDouble(s))/(double)multiplie;
                 if(!mapa.containsKey(key)){
                     mapa.put(key, d);
                 }else{
                     mapa.put(key, mapa.get(key)+d);
                 }
             }
         }
         return mapa;
     }
     public int getEuclide(int vel){
         return EUCLIDE[vel];
     }
     public int calculateEuclide(double count){
         int i = (VEL*2);
         for(; i > 0 ; i--){
             if(count <= getEuclide(i)){
                 return i;
             }
         }
         return i;
     }
     private String searchItem(String s){ // Busca el nomrbe real de un objeto.
         makePaths();
         if(getPaths().isSet(s)){
             String res = getPaths().getString(s);
             return res;
         }
         return null;
     }
     private Configuration getPaths(){
         if(paths == null){
             makePaths(); 
         }
         return paths;
     }
     private void makePaths(){
         paths = new MemoryConfiguration();
         Set<String> items = getCache().getConfigurationSection("item").getKeys(false);
         for(String s: items){// Items
             getPaths().set(s, s);
             if(getCache().isSet("item."+s+".alias")){// Alias
                 List<String> alias = getCache().getStringList("item."+s+".alias");
                 for(String a: alias){
                     getPaths().set(a, s);
                 }
             }
         }
         for(String s: items){// IDs
             if(haveData(s)){
                 if(getData(s) == 0){
                     getPaths().set(getId(s)+"", s);
                 }
                 getPaths().set(getId(s)+":"+getData(s), s);
             }else{
                 getPaths().set(getId(s)+"", s);
             }
         }
     }
     private void isSet(String path, Object value){
         if(!getCache().isSet(path)){
             getCache().set(path, value);
             save();
         }
     }
     public ItemStack createItem(String p, String item, int size){
         ItemStack stack = new ItemStack(getId(item));
         short data = (short)getData(item);
         if(haveData(item)){
             stack.setDurability(data);
         }
         stack.setAmount(size);
         // Da la cabeza del que la pide.
         if(stack.getType() == Material.SKULL_ITEM){
             if(data == 3){
                 SkullMeta skull = (SkullMeta) stack.getItemMeta();
                 skull.setOwner(p);
                 stack.setItemMeta(skull);
             }
         }
         // Da un libro con un encantamiento al azar.
         else if(stack.getType() == Material.ENCHANTED_BOOK){
             EnchantmentStorageMeta meta = ((EnchantmentStorageMeta)stack.getItemMeta());
             java.util.Random rndm = new java.util.Random();;
             Enchantment enchant;
             do{
                 enchant = Enchantment.values()[rndm.nextInt(Enchantment.values().length)];
             } while(enchant.canEnchantItem(stack));
             meta.addStoredEnchant(enchant, 1+rndm.nextInt(enchant.getMaxLevel()), true);
             stack.setItemMeta(meta);
         }
         return stack;
     }
     public List<ItemStack> createItems(String p, String item, int num){
         List<ItemStack> items = new ArrayList<ItemStack>();
         int maxStack = Material.getMaterial(getId(item)).getMaxStackSize();
         if(num%maxStack > 0){
             items.add(createItem(p, item, num%maxStack));
             num -= num%maxStack;
         }
         for(int i = 0; i < num/maxStack; i++){
             items.add(createItem(p, item, maxStack));
         }
         return items;
     }
     private boolean haveData(String item){
         return getCache().isSet("item."+item+".data");
     }
     // Compra un objeto
     public boolean shop(Player p, String item, int cantidad){
         String item_real = searchItem(item);
         if(item_real == null){
             Chat.mensaje(p, "shop.notExist");
             return false;
         }
         if(!getUserBuy(p, item)){
             Chat.mensaje(p, "shop.cantBuy");
             return false;
         }
         Econ econ = new Econ();
         double precio = getPrecio(item_real, cantidad);
         if(!econ.cobrar(p, precio)){
             Chat.mensaje(p, "shop.noMoney");
             return false;
         }
         econ.getLogg().logg("Shop", p, "buy", item_real, cantidad, precio);
         addItemsToInventory(p, createItems(p.getName(), item_real, cantidad));
         addCountRecursive(item_real, cantidad*(-1d));
         return true;
     }
     public boolean shopUser(Player p, String item, int cantidad, int acceleracion){
         String item_real = searchItem(item);
         if(item_real == null){
             Chat.mensaje(p, "shop.notExist");
             return false;
         }
         Econ econ = new Econ();
         double precio = getPrecio(item_real, cantidad, acceleracion)/MULTIPLIER*USERMULTIPLIER;
         if(!econ.cobrar(p, precio)){
             Chat.mensaje(p, "shop.noMoney");
             return false;
         }
         String user = new FactoryGui(p).getUserShopName();
         econ.getLogg().logg("Shop", p, "buy to "+user
             , item_real, cantidad, precio);
         addItemsToInventory(p, createItems(user, item_real, cantidad));
         return true;
     }
     public void addItemsToInventory(Player p, List<ItemStack> items){ // Aade el item al inventario del jugador.
         for(ItemStack item: items){
             addItemToInventory(p, item);
         }
     }
     public void addItemToInventory(Player p, ItemStack item){
         for(ItemStack i: p.getInventory().addItem(item).values()){
             p.getWorld().dropItem(p.getLocation(), i);
         }
     }
     public List<String> lookItems(String item, boolean all){ // Busca items.
         List<String> items = new ArrayList<String>();
         List<String> response = new ArrayList<String>();
         String n = "";
         for(int i = 0; i < item.length(); i++){
             n += "[";
             n += item.toLowerCase().toCharArray()[i];
             n += item.toUpperCase().toCharArray()[i];
             n += "]";
         }
         item = n;
         for(String s: getPaths().getKeys(false)){
             // Si encuentra un matche completo.
             if(s.matches("^"+item+"$")){
                 if(all){
                     items.add(s);
                 }else{
                     items = new ArrayList<String>();
                     items.add(s);
                     break;
                 }
             }else if(s.matches("(.*)("+item+")(.*)")){
                 items.add(s);
             }
         }
         for(String s: items){
             response.add(s);
             if(response.size() == 18){
                 break;
             }
         }
         return response;
     }
     public List<String> lookItems(String item){
         return lookItems(item, false);
     }
     public void reset(String item){
         Map<String, Double> map = getDepends(item);
         item = searchItem(item);
         update(item);
         for(String s: map.keySet()){
             setCount(s, MIDDLE);
             setVel(s, VEL);
         }
     }
     public void setPrice(String item, Double NewPrice){
         reset(item);
         item = searchItem(item);
         getCache().set("item."+item+".price", (NewPrice-getPrice(item))+getPriceData(item));
         save();
     }
     public void showItemInfo(Player p, String itemName){
         String item = searchItem(itemName);
         List<String> list = new ArrayList<String>();
         List<Double> precios = new ArrayList<Double>();
         ConfigurationSection cache = getUserCache().getConfigurationSection("userShop");
         String format = Strings.getString("shop.itemSearch");
         int vel, amount, i;
         double precio;
         Econ econ = new Econ();
         for(String user: cache.getKeys(false)){
             if(cache.isSet(user+".items."+item)){
                 vel = cache.getInt(user+".items."+item+".vel");
                 precio = (getPrice(item, vel)/MULTIPLIER)*USERMULTIPLIER;
                 for(i = 0; i < list.size(); i++){
                     if(precio < precios.get(i)){
                         break;
                     }
                 }
                 amount = cache.getInt(user+".items."+item+".amount");
                 precios.add(i, precio);
                 list.add(i, String.format(format, user, econ.getPrecio(precio), amount));
             }
         }
         Chat.mensaje(p, "shop.searchHead", item);
         Chat.mensaje(p, list);
     }
     public int getProduction(String item){
         Map<String, Double> map = getDepends(item);
         double sumPrice = 0;
         double sumProduct = 0;
         double currPrice;
         for(String s: map.keySet()){
             currPrice = getPriceData(s) * map.get(s);
             sumPrice += currPrice;
             sumProduct += currPrice * getVel(s);
         }
         return ((Double)(sumProduct/sumPrice)).intValue();
     }
     // Abre el inventario de la tienda.
     public void openInventory(Player p){
         openInventory(p, null);
     }
     public void openInventory(Player p, String userShop){
         if(userShop == null){
             if(!p.hasPermission("spicecraft.shop.server.open")){
                 Chat.mensaje(p, "alert.permission");
                 return;
             }
         }else{
             if(!p.hasPermission("spicecraft.shop.private.open")){
                 Chat.mensaje(p, "alert.permission");
                 return;
             }
         }
         if(p.getGameMode() == GameMode.CREATIVE){
             Chat.mensaje(p, "shop.creative");
             return;
         }
         Inventory inv = org.bukkit.Bukkit.createInventory(p, 27, getShopName());
         FactoryGui gui = new FactoryGui(p);
         if(userShop != null){
             gui.setPath("userShop."+userShop+".");
             if(!gui.isUserShop()){
                 getUserCache().set("userShop."+userShop+".money", 0.0d);
             }
         }
         gui.pay(p);
         gui.updateInventory(inv);
         p.openInventory(inv);
     }
     public boolean sellItem(Player p, ItemStack item){
         double money = 0;
         String name = getItemName(item);
         if(name == null){
             Chat.mensaje(p, "shop.notExist");
             addItemToInventory(p, item);
             return false;
         }
         if(item.getEnchantments().size() > 0){
             Chat.mensaje(p, "shop.notExist");
             addItemToInventory(p, item);
             return false;
         }
         if(!getUserSell(p, name)){
             Chat.mensaje(p, "shop.cantSell");
             addItemToInventory(p, item);
             return false;
         }
         addCountRecursive(name, (double)item.getAmount());
         double maxDurab = item.getType().getMaxDurability();
         double durab = maxDurab - item.getDurability();
         if(durab > 0){
             money += getPrecio(name, item.getAmount())*(durab/maxDurab);
         }else{
             money += getPrecio(name, item.getAmount());
         }
         Econ econ = new Econ();
         econ.pagar(p, money*SELLRATE/MULTIPLIER);
         econ.getLogg().logg("Shop", p, "sell", name, item.getAmount(), money);
         return true;
     }
     public String getItemName(ItemStack item){
         if(item == null){
             return null;
         }
         double maxDurab = item.getType().getMaxDurability();
         String id = ""+item.getTypeId();
         if(item.getDurability() > 0){
             if(maxDurab == 0){
                 id += ":"+item.getDurability();
             }
         }
         String name = searchItem(id);
         if(name == null){
             name = searchItem(id+":0");
         }
         return name;
     }
     public String getShopName(){
         if(shopName == null){
             shopName = getCache().getString("shop.name");
         }
         return shopName;
     }
     // Gestion de archivos.
     private static void setFile(String path){
         Factory.file = new Archivo(path);
     }
     private static Archivo getFile() {
         if(file == null){
             setFile("shop.yml");
         }
         return file;
     }
     private static void setCache(FileConfiguration fc){
         Factory.fc = fc;
     }
     public static FileConfiguration getCache(){
         if(fc == null){
             setCache(getFile().load());
         }
         return fc;
     }
     private static void setFileUser(String path){
         Factory.fileUser = new Archivo(path);
     }
     private static Archivo getFileUser(){
         if(fileUser == null){
             setFileUser("shopUser.yml");
         }
         return fileUser;
     }
     public static FileConfiguration getUserCache(){
         if(fcUser == null){
             fcUser = getFileUser().load();
         }
         return fcUser;
     }
     public static FileConfiguration getVolatilCache(){
         if(volatil == null){
             volatil = new YamlConfiguration();
         }
         return volatil;
     }
     public void save(){
         getFile().save(getCache());
         getFileUser().save(getUserCache());
     }
     public void saveNow(){
         getFileUser().saveNow(getUserCache());
     }
     @EventHandler
     private void onDragInventory(InventoryDragEvent event){
         Inventory inv = event.getInventory();
         if(inv.getType() != InventoryType.CHEST){
             return;
         }
         if(inv.getSize() != FactoryGui.SIZE){
             return;
         }
         if(inv.getName() == getShopName()){
             for(int idStack: event.getRawSlots()){
                 if(idStack < event.getInventory().getSize()){
                     event.setCancelled(true);
                 }
             }
         }
     }
     @EventHandler
     private void onClickInventory(InventoryClickEvent event){
         // TEST long timeI = System.nanoTime();
         if(event.getInventory().getType() != InventoryType.CHEST){
             return;
         }
         if(event.getInventory().getSize() != FactoryGui.SIZE){
             return;
         }
         if(!(event.getInventory().getName() == getShopName())){
             return;
         }
         Player p = (Player)event.getWhoClicked();
         FactoryGui gui = new FactoryGui(p);
         event.setCancelled(gui.click(event));
         // TEST Chat.mensaje("elxris", ""+((double)(System.nanoTime()-timeI))/1000000.0d);
     }
     @EventHandler
     private void onCloseInventory(org.bukkit.event.inventory.InventoryCloseEvent event){
         if(event.getInventory().getType() != InventoryType.CHEST){
             return;
         }
         if(event.getInventory().getName() == getShopName()){
             new FactoryGui((Player)event.getPlayer()).close();
         }
     }
     @EventHandler
     private void onDisable(PluginDisableEvent event){
         if(event.getPlugin() != SpiceCraft.plugin()){
             return;
         }
         for(Player p : event.getPlugin().getServer().getOnlinePlayers()){
             InventoryView inv = p.getOpenInventory();
             if(inv.getType() != InventoryType.CHEST){
                 return;
             }
             if(inv.getTitle() != getShopName()){
                 return;
             }
             new FactoryGui(p).close();
             inv.close();
         }
         saveNow();
     }
 }
