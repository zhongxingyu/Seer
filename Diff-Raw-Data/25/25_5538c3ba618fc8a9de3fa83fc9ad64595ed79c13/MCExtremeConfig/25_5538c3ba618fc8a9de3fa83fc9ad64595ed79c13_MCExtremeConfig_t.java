 package mcextreme.core.config;
 
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.Property;
 
 public class MCExtremeConfig
 {
     //--------------- MC-Extreme Aesthetics ------------------
     
     //--------------- MC-Extreme Core ------------------------
     
     //--------------- MC-Extreme Magic -----------------------
     public static int blockMagicPortalID, blockMagicOreID, blockMagicStorageID, blockSolidEvilID, blockWorldsEndID;   
     public static int itemCrystalID, itemDarkBucketID;   
     
     //--------------- MC-Extreme Tech ------------------------
    public static int blockTechOreID, blockTechStorageID, blockBasicMachineID, blockCompressorID, blockCompressorActiveID;
     public static int itemTechIngotID, itemScrewdriverID;
     
     //--------------- MC-Extreme VanillaTweaks ---------------
     public static int itemLinkID, itemPlateID;
     
     public static void loadConfig(Configuration config, String type)
     {
         config.load();
         
         if (type.toUpperCase().equals("AESTHETICS"))
         {
             
         }
         
         else if (type.toUpperCase().equals("CORE"))
         {
             
         }
         
         else if (type.toUpperCase().equals("MAGIC"))
         {
             blockMagicPortalID = config.get("block", "BlockMagicPortalID", 540).getInt();
             blockMagicOreID = config.get("block", "BlockOreID", 541).getInt();
             blockMagicStorageID = config.get("block", "BlockStorageID", 542).getInt();
             blockSolidEvilID = config.get("block", "BlockSolidEvilID", 543).getInt();
             blockWorldsEndID = config.get("block", "BlockWorldsEndID", 544).getInt();
             
             itemCrystalID = config.get("item", "ItemCrystalID", 6300).getInt();
             itemDarkBucketID = config.get("item", "ItemDarkBucketID", 6301).getInt();
         }
         
         else if (type.toUpperCase().equals("TECH"))
         {
             Property propBlockOreID, propBlockStorageID;
             
             propBlockOreID = config.get("block", "BlockOreID", 640);
             propBlockStorageID = config.get("block", "BlockStorageID", 641);
             
             propBlockOreID.comment = "Ore Block ID";
             propBlockStorageID.comment = "Storage Block ID";
             
             blockTechOreID = propBlockOreID.getInt();
             blockTechStorageID = propBlockStorageID.getInt();
             
             blockBasicMachineID = config.get("block", "BlockBasicMachineID", 670).getInt();
             
             itemTechIngotID = config.get("item", "ItemIngotID", 6400).getInt();
             itemScrewdriverID = config.get("item", "ItemScrewdriverID", 6350).getInt();
         }
         
         else if (type.toUpperCase().equals("VTWEAKS"))
         {
             itemLinkID = config.get("item", "ItemLinkID", 6500).getInt();
             itemPlateID = config.get("item", "ItemPlateID", 6501).getInt();
         }
         
         config.save();
     }
 }
