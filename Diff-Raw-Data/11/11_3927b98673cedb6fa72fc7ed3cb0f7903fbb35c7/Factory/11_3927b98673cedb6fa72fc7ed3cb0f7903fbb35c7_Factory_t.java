 package elxris.Useless.Objects;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.MaterialData;
 
 import elxris.Useless.Useless;
 import elxris.Useless.Utils.Archivo;
 import elxris.Useless.Utils.Chat;
 import elxris.Useless.Utils.Econ;
 import elxris.Useless.Utils.Strings;
 
 public class Factory {
     private static Archivo file;
     private static FileConfiguration fc;
     private int vel;
     private long FRECUENCY;
     private int STACKFULL;
     private static boolean save;
     public Factory() {
         init();
     }
     private void init() {
         vel = getCache().getInt("vel");
         FRECUENCY = getCache().getLong("freq")*60000;
         STACKFULL = getCache().getInt("full");
     }
     private void update(String item){
         for(;getTime(item) <= getSystemTimeHour(); addTime(item, FRECUENCY)){
             produce(item);
             save = true;
         }
     }
     private void produce(String item){
         // Producir deacuerdo a un item y su velocidad, y luego cambiar su velocidad.
         addCount(item, getVel(item));
         if(getCount(item) < 0){
             addVel(item, 1);
         }else if(getCount(item) > 0 && getCount(item) < STACKFULL){
             if(getVel(item) > vel){
                 addVel(item, -1);
             }else if(getVel(item) < vel){
                 addVel(item, +1);
             }
         }else if(getCount(item) > STACKFULL){
             if(getVel(item) > 1){
                 addVel(item, -1);
             }
         }
     }
     public void setTime(String item, long time) {
         getCache().set("item."+item+".time", time);
     }
     public long getTime(String item) {
         isSet("item."+item+".time", getSystemTime());
         return getCache().getLong("item."+item+".time");
     }
     public long getTimeHour(String item) {
         return getTime(item) - (getTime(item) % FRECUENCY);
     }
     public void addTime(String item, long time){
         setTime(item, getTime(item)+time);
     }
     public long getSystemTime(){
         return System.currentTimeMillis();
     }
     public long getSystemTimeHour(){
         return getSystemTime() - (getSystemTime() % FRECUENCY);
     }
     public void setCount(String item, int count){
         getCache().set("item."+item+".count", count);
     }
     public int getCount(String item){
         isSet("item."+item+".count", STACKFULL / 2);
         return getCache().getInt("item."+item+".count");
     }
     public void addCount(String item, int count){
     	setCount(item, getCount(item)+count);
 		for(String s: getDepends(item)){
 			setCount(s, getCount(s)+count);
 		}
     }
     public void setVel(String item, int vel){
         getCache().set("item."+item+".vel", vel);
     }
     public int getVel(String item){
         isSet("item."+item+".vel", vel);
         return getCache().getInt("item."+item+".vel");
     }
     public void addVel(String item, int vel){
         setVel(item, getVel(item)+vel);
     }
     public double getPrice(String item){
     	double price = getCache().getDouble("item."+item+".price");
     	if(getCache().isSet("item."+item+".depend")){
     		for(String s: getDepends(item)){
     			price += getPrecio(s);
     		}
     	}
         return price;
     }
     public double getRazonPrecio(String item){
         return (double)getVel(item)/vel;
     }
     public double getPrecio(String item){
         return getRazonPrecio(item)*getPrice(item);
     }
     public double getPrecio(String item, int cantidad){
         return getPrecio(item)*(double)cantidad;
     }
     public int getId(String item){
         return getCache().getInt("item."+item+".id");
     }
     public int getData(String item){
         return getCache().getInt("item."+item+".data");
     }
    private List<String> getDepends(String item){
     	List<String> dependency = new ArrayList<String>();
    	if(!getCache().isSet("item."+item+".depend")){
     		dependency.add(item);
     		return dependency;
     	}
    	ConfigurationSection memory = getCache().getConfigurationSection("item."+item+".depend");
     	for(String s: memory.getKeys(false)){
     		for(int i = memory.getInt(s); i > 0; i--){
     			dependency.addAll(getDepends(s));
     		}
     	}
 		return dependency;
     }
     public String searchItem(String s){
         makePaths();
         s = s.replace(':', '-');
         if(getCache().isSet("paths."+s.toLowerCase())){
             String res = getCache().getString("paths."+s.toLowerCase());
             update(res);
             return res;
         }
         return null;
     }
     private void makePaths(){
         if(getCache().isSet("paths")){
            return; 
         }
         Set<String> items = getCache().getConfigurationSection("item").getKeys(false);
         for(String s: items){// Items
             getCache().set("paths."+s.toLowerCase(), s);
             if(getCache().isSet("item."+s+".alias")){// Alias
             	List<String> alias = getCache().getStringList("item."+s+".alias");
             	for(String a: alias){
             		getCache().set("paths."+a.toLowerCase(), s);
             	}
             }
         }
         for(String s: items){// IDs
             if(!getCache().isSet("paths."+getCache().getInt("item."+s+".id"))){
             	if(haveData(s)){
             		getCache().set("paths."+getCache().getInt("item."+s+".id")+"-"+getCache().getInt("item."+s+".data"), s);
             	}else{
             		getCache().set("paths."+getCache().getInt("item."+s+".id"), s);
             	}
             }
         }
     }
     private void isSet(String path, Object value){
         if(!getCache().isSet(path)){
             getCache().set(path, value);
             save = true;
         }
     }
     private ItemStack createItem(String item, int size){
         ItemStack stack = new ItemStack(getId(item));
         byte data = (byte)getData(item);
         if(haveData(item)){
             MaterialData mData = stack.getData();
             mData.setData(data);
             stack = mData.toItemStack();
         }
         stack.setAmount(size);
         return stack;
     }
     private List<ItemStack> createItems(String item, int num){
         List<ItemStack> items = new ArrayList<ItemStack>();
         int maxStack = Material.getMaterial(getId(item)).getMaxStackSize();
         if(num%maxStack > 0){
             items.add(createItem(item, num%maxStack));
             num -= num%maxStack;
         }
         for(int i = 0; i < num/maxStack; i++){
             items.add(createItem(item, maxStack));
         }
         return items;
     }
     public boolean haveData(String item){
     	return getCache().isSet("item."+item+".data");
     }
     // Comandos de la tienda.
     public void shop(Player p, String item, int cantidad){ // Compra
         if(searchItem(item) == null){
             Chat.mensaje(p, "shop.notExist");
             return;
         }
         Econ econ = new Econ();
         if(!econ.cobrar(p, getPrecio(item, cantidad))){
             Chat.mensaje(p, "shop.noMoney");
             return;
         }
         addItemsToInventory(p, createItems(item, cantidad));
         addCount(item, -cantidad);
     }
     private void addItemsToInventory(Player p, List<ItemStack> items){ // Aade el item al inventario del jugador.
         ItemStack[] itemsArray = items.toArray(new ItemStack[0]);
         for(ItemStack item: p.getInventory().addItem(itemsArray).values()){
             p.getWorld().dropItemNaturally(p.getLocation(), item);
         }
     }
     public List<String> lookItems(String item){ // Busca items.
         List<String> items = new ArrayList<String>();
         makePaths();
         for(String s: getCache().getConfigurationSection("paths").getKeys(false)){
             if(s.matches("(.*)("+item.toLowerCase()+")(.*)")){
                 items.add(s);
             }
         }
         return items;
     }
     public void sell(Player p){
         String name = searchItem(String.valueOf(p.getItemInHand().getTypeId())); 
         if(name == null){
             Chat.mensaje(p, "shop.notExist");
             return;
         }
         if(p.getGameMode() == GameMode.CREATIVE){
             Chat.mensaje(p, "shop.creative");
             return;
         }
         addCount(name, p.getItemInHand().getAmount());
         new Econ().pagar(p, getPrecio(name, p.getItemInHand().getAmount())*Strings.getDouble("shop.sellRate"));
         p.setItemInHand(null);
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
     private static FileConfiguration getCache(){
         if(fc == null){
             setCache(getFile().load());
             if(!fc.isSet("vel")){
                 fc = Useless.getConfig("shop.yml");
             }
         }
         return fc;
     }
     public static void save(){
     	if(!save){
     		return;
     	}
         getCache().set("paths", null);
         getFile().save(getCache());
         save = false;
     }
 }
