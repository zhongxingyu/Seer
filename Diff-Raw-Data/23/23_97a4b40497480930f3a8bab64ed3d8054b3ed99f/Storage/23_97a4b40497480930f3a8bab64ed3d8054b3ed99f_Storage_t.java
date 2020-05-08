 package Ne0nx3r0.SignShop;
 import org.bukkit.util.config.Configuration;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.block.Block;
 import java.io.File;
 import java.util.Map;
 import java.util.HashMap;
 import org.bukkit.Location;
 import org.bukkit.Bukkit;
 import java.util.ArrayList;
 import org.bukkit.Material;
 
 public class Storage{
     public static Configuration yml;
 
     private static Map<Location,Seller> sellers;
 
     public Storage(File ymlFile){
        this.sellers = new HashMap <Location,Seller>();

         this.yml = new Configuration(ymlFile);
         this.yml.load();
 
         this.Load();
     }
 
     public void Load(){
         Map<String,Object> tempSellers = (Map<String,Object>) this.yml.getProperty("sellers");
 
         if(tempSellers == null){
             return;
         }
 
         Map<String,Object> tempSeller;
 
         String[] sSignLocation;
         Location lSign;
         Block bChest;
         ItemStack[] isItems;
         ArrayList<Integer> items;
         ArrayList<Integer> amounts;
         ArrayList<String> datas;
         ArrayList<Integer> durabilities;
         for(String sKey : tempSellers.keySet()){
             sSignLocation = sKey.split("/");
 
             while(sSignLocation.length > 4){
                 sSignLocation[0] = sSignLocation[0]+"/"+sSignLocation[1];
 
                 for(int i=0;i<sSignLocation.length-1;i++){
                     sSignLocation[i] = sSignLocation[i+1];
                 }
             }
 
             int iX = 0;
             int iY = 0;
             int iZ = 0;
 
             try{
                 iX = Integer.parseInt(sSignLocation[1]);
                 iY = Integer.parseInt(sSignLocation[2]);
                 iZ = Integer.parseInt(sSignLocation[3]);
             }catch(NumberFormatException nfe){
                 System.out.println("Invalid sign found at World:"+sSignLocation[0]
                     +" X:"+sSignLocation[1]
                     +" Y:"+sSignLocation[2]
                     +" Z:"+sSignLocation[3]);
             }
 
             Block bSign = Bukkit.getServer().getWorld(sSignLocation[0]).getBlockAt(
                 Integer.parseInt(sSignLocation[1]),
                 Integer.parseInt(sSignLocation[2]),
                 Integer.parseInt(sSignLocation[3]));
 
             if(bSign.getType() != Material.SIGN_POST && bSign.getType() != Material.WALL_SIGN){
                 //if no longer valid, remove this sign
                 System.out.println("Removed invalid signshop");
                 continue;
             }
 
             lSign = bSign.getLocation();
 
             tempSeller = (Map<String,Object>) tempSellers.get(sKey);
 
             bChest = Bukkit.getServer().getWorld((String) tempSeller.get("chestworld")).getBlockAt(
                 (Integer) tempSeller.get("chestx"),
                 (Integer) tempSeller.get("chesty"),
                 (Integer) tempSeller.get("chestz"));
 
             datas = (ArrayList<String>) tempSeller.get("datas");
             items = (ArrayList<Integer>) tempSeller.get("items");
             amounts = (ArrayList<Integer>) tempSeller.get("amounts");
             durabilities = (ArrayList<Integer>) tempSeller.get("durabilities");
             
             isItems = new ItemStack[items.size()];
 
             for(int i=0;i<items.size();i++){
                 isItems[i] = new ItemStack(items.get(i),amounts.get(i));
 
                 if(datas != null && datas.get(i) != null){
                     isItems[i].getData().setData(new Byte(datas.get(i)));
                 }
                 if(durabilities != null && durabilities.get(i) != null){
                     isItems[i].setDurability(durabilities.get(i).shortValue());
                 }
             }
 
             this.sellers.put(lSign, new Seller((String) tempSeller.get("owner"),bChest,isItems));
         }
     }
 
     public void Save(){
        System.out.println("[QuantumCircuits] Saving circuits...");

         Map<String,Object> tempSellers = new HashMap<String,Object>();
 
         Seller seller;
         Map<String,Object> temp;
         for(Location lKey : this.sellers.keySet()){
             temp = new HashMap<String,Object>();
 
             seller = sellers.get(lKey);
 
             temp.put("chestworld",seller.world);
             temp.put("chestx",seller.x);
             temp.put("chesty",seller.y);
             temp.put("chestz",seller.z);
 
             temp.put("items",seller.items);
             temp.put("amounts",seller.amounts);
             temp.put("durabilities",seller.durabilities);
 
             String[] sDatas = new String[seller.datas.length];
             for(int i=0;i<seller.datas.length;i++){
                 if(sDatas[i] != null){
                     sDatas[i] = Byte.toString(seller.datas[i]);
                 }
             }
             temp.put("datas", sDatas);
 
             temp.put("owner",seller.owner);
 
             tempSellers.put(lKey.getWorld().getName()+"/"+lKey.getBlockX()+"/"+lKey.getBlockY()+"/"+lKey.getBlockZ(),temp);
         }
         
         this.yml.setProperty("sellers",tempSellers);
         this.yml.save();

        System.out.println("[QuantumCircuits] Circuits saved!");
     }
 
     public void addSeller(String sPlayer,Block bSign,Block bChest,ItemStack[] isItems){
         this.sellers.put(bSign.getLocation(), new Seller(sPlayer,bChest,isItems));
         
         this.Save();
     }
 
     public Seller getSeller(Location lKey){
         if(this.sellers.containsKey(lKey)){
             return this.sellers.get(lKey);
         }
         return null;
     }
 
     public void removeSeller(Location lKey){
         if(this.sellers.containsKey(lKey)){
             this.sellers.remove(lKey);
         
             this.Save();
         }
     }
 }
