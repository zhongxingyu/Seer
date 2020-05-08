 import forge.Configuration;
 import ic2.api.Items;
 import ic2.platform.Platform;
 import java.io.File;
 
 public class mod_IC2_ChargingBench extends ChargingBenchMod {
 
    public static Configuration config;
    public static int idBlockChargingBench;
    public static pb blockChargingBench;
    public static int guiIdChargingBench;
 
 
    public void load() {
       blockChargingBench = new BlockChargingBench(idBlockChargingBench);
       ModLoader.registerBlock(blockChargingBench, ItemChargingBench.class);
       ModLoader.registerTileEntity(TileEntityChargingBench1.class, "Charging Bench Mk1");
       ModLoader.registerTileEntity(TileEntityChargingBench2.class, "Charging Bench Mk2");
       ModLoader.registerTileEntity(TileEntityChargingBench3.class, "Charging Bench Mk3");
       ModLoader.addRecipe(new aan(blockChargingBench, 1, 0), new Object[]{"UUU", "W W", "WWW", Character.valueOf('U'), Items.getItem("copperCableItem"), Character.valueOf('W'), pb.x});
       ModLoader.addRecipe(new aan(blockChargingBench, 1, 1), new Object[]{"UUU", "WCW", "WWW", Character.valueOf('U'), Items.getItem("goldCableItem"), Character.valueOf('W'), pb.x, Character.valueOf('C'), Items.getItem("electronicCircuit")});
       ModLoader.addRecipe(new aan(blockChargingBench, 1, 2), new Object[]{"UUU", "WCW", "WWW", Character.valueOf('U'), Items.getItem("ironCableItem"), Character.valueOf('W'), pb.x, Character.valueOf('C'), Items.getItem("advancedCircuit")});
    }
 
    public String getVersion() {
       return "1.95b";
    }
 
    static {
       try {
          config = new Configuration(new File(Platform.getMinecraftDir() + "/config/IC2ChargingBench.cfg"));
          config.load();
          idBlockChargingBench = Integer.valueOf(config.getOrCreateIntProperty("blockChargingBench", "block", 189).value).intValue();
          guiIdChargingBench = Integer.valueOf(config.getOrCreateIntProperty("guiIdChargingBench", "general", 110).value).intValue();
          config.save();
       } catch (Exception var1) {
          System.out.println("[ChargingBench] Error while trying to access configuration!");
          throw new RuntimeException(var1);
       }
    }
 }
