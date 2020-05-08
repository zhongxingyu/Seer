 package Ne0nx3r0.QuantumConnectors;
 
 import org.bukkit.util.config.Configuration;
 import java.io.File;
 import java.util.Map;
 import java.util.HashMap;
 import org.bukkit.block.Block;
 import org.bukkit.Material;
 import java.util.List;
 import org.bukkit.Bukkit;
 import org.bukkit.Server;
 import org.bukkit.Location;
 import java.util.ArrayList;
 
 public final class CircuitManager{
     private final QuantumConnectors plugin;
     public static Configuration yml;
 
     private static Map<Location,Circuit> circuits;
 
     public CircuitManager(File ymlFile,final QuantumConnectors plugin){
         this.plugin = plugin;
 
         circuits = new HashMap<Location,Circuit>();
         
         yml = new Configuration(ymlFile);
         yml.load();
 
         Load();
     }
 
     public void addCircuit(Location lSender,Location lReceiver,int iType){
         circuits.put(lSender,new Circuit(iType,lReceiver));
     }
 
     public boolean circuitExists(Location lSender){
         return circuits.containsKey(lSender);
     }
 
     public Circuit getCircuit(Location lSender){
         return circuits.get(lSender);
     }
 
     public void removeCircuit(Location lSender){
         if(circuits.containsKey(lSender)){
             circuits.remove(lSender);
         }
     }
 
     private Material[] validSenders = new Material[]{
         Material.LEVER,
         Material.REDSTONE_WIRE,
         Material.STONE_BUTTON,
         Material.STONE_PLATE,
         Material.WOOD_PLATE,
         Material.REDSTONE_TORCH_OFF,
         Material.REDSTONE_TORCH_ON,
         Material.IRON_DOOR_BLOCK,
         Material.WOODEN_DOOR,
         Material.TRAP_DOOR,
         Material.STONE_BUTTON,
         Material.POWERED_RAIL,
         Material.DIODE_BLOCK_OFF,
         Material.DIODE_BLOCK_ON
     };
     private Material[] validReceivers = new Material[]{
         Material.LEVER,
         Material.IRON_DOOR_BLOCK,
         Material.WOODEN_DOOR,
         Material.TRAP_DOOR,
         Material.POWERED_RAIL
     };
 
     public boolean isValidSender(Block block){
         Material mBlock = block.getType();
         for(int i=0;i<validSenders.length;i++){
             if(mBlock == validSenders[i]){
                 return true;
             }
         }
         return false;
     }
     
     public String getValidSendersString(){
         String sMessage = "";
         for(int i=0;i<validSenders.length;i++){
             sMessage += validSenders[i].name().toLowerCase().replace("_"," ")+", ";
         }
         return sMessage.substring(0,sMessage.length()-2);
     }
 
     public boolean isValidReceiver(Block block){
         Material mBlock = block.getType();
         for(int i=0;i<validReceivers.length;i++){
             if(mBlock == validReceivers[i]){
                 return true;
             }
         }
         return false;
     }
 
     public String getValidReceiversString(){
         String sMessage = "";
         for(int i=0;i<validReceivers.length;i++){
             sMessage += validReceivers[i].name().toLowerCase().replace("_"," ")+", ";
         }
         return sMessage.substring(0,sMessage.length()-2);
     }
 
     public void Load(){
         List tempCircuits = yml.getList("circuits");
 
         if(tempCircuits == null){
             System.out.println("[Quantum Connectors] No circuits.yml file found, will be created on the next save.");
             return;
         }
 
         Server server = Bukkit.getServer();
         Location lSender;
         Location lReceiver;
         int iType;
         Map<String,Object> temp;
         for(int i=0; i< tempCircuits.size(); i++){
             temp = (Map<String,Object>) tempCircuits.get(i);
 
             iType = (Integer) temp.get("type");
 
             lSender = new Location(
                 server.getWorld((String) temp.get("sw")),
                 (Integer) temp.get("sx"),
                 (Integer) temp.get("sy"),
                 (Integer) temp.get("sz")
             );
 
             lReceiver = new Location(
                 server.getWorld((String) temp.get("rw")),
                 (Integer) temp.get("rx"),
                 (Integer) temp.get("ry"),
                 (Integer) temp.get("rz")
             );
 
             if(isValidSender(lSender.getBlock()) 
             && isValidReceiver(lReceiver.getBlock())
            && plugin.circuitTypes.containsValue(iType)
			&& !lReceiver.toString().equals(lSender.toString())){
                 addCircuit(lSender,lReceiver,iType);
             }else{
                 System.out.println("[QuantumConnectors] Removing invalid circuit.");
             }
 	}
     }
 
     public void Save(){
         List<Object> tempCircuits = new ArrayList<Object>();
 
         Map<String,Object> temp;
         Circuit currentCircuit;
 
         for(Location lKey : circuits.keySet()){
             currentCircuit = circuits.get(lKey);
 
             temp = new HashMap<String,Object>();
             
             temp.put("sw",lKey.getWorld().getName());
             temp.put("sx",lKey.getBlockX());
             temp.put("sy",lKey.getBlockY());
             temp.put("sz",lKey.getBlockZ());
 
             temp.put("rw",currentCircuit.reciever.getWorld().getName());
             temp.put("rx",currentCircuit.reciever.getBlockX());
             temp.put("ry",currentCircuit.reciever.getBlockY());
             temp.put("rz",currentCircuit.reciever.getBlockZ());
 
             temp.put("type",currentCircuit.type);
 
             tempCircuits.add(temp);
         }
 
         yml.setProperty("circuits", tempCircuits);
 
         yml.save();
     }
 }
