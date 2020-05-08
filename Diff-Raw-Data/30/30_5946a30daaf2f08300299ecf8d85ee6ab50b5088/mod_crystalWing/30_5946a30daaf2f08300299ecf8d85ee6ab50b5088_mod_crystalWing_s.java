 package net.minecraft.src;
 
 import bspkrs.util.ModVersionChecker;
 import net.minecraft.client.Minecraft;
 
 public class mod_crystalWing extends BaseMod
 {
     @MLProp(info="Set to true to allow checking for mod updates, false to disable")
     public static boolean allowUpdateCheck = true;
     @MLProp
     public static int idCrystalWing = 3100;
     @MLProp
     public static int idBurningWing = 3101;
     @MLProp
     public static int idBurnedWing = 3102;
     @MLProp(info="Number of Crystal Wing uses. Set to 0 for infinite.")
     public static int uses = 8;
     @MLProp(info="Maximum distance for the Burned Wing teleportation.\n\n**ONLY EDIT WHAT IS BELOW THIS**")
     public static int teleDistance = 500;
    
     static int indexCrystalWing = ModLoader.addOverride("/gui/items.png", "/daftpvf/crystalWing.png");
     static int indexBurningWing = ModLoader.addOverride("/gui/items.png", "/daftpvf/crystalWingBurning.png");
     static int indexBurnedWing = ModLoader.addOverride("/gui/items.png", "/daftpvf/crystalWingBurned.png");
     public static final Item crystalWing = (new ItemCrystalWing(idCrystalWing - 256)).setItemName("crystalWing").setIconIndex(indexCrystalWing);
     public static final Item crystalWingBurning = (new ItemCrystalWingBurning(idBurningWing - 256)).setItemName("burningWing").setIconIndex(indexBurningWing);
     public static final Item crystalWingBurned = (new ItemCrystalWingBurned(idBurnedWing - 256, teleDistance)).setItemName("burnedWing").setIconIndex(indexBurnedWing);
     public static final Achievement burnedWing = (new Achievement(1710, "burnedWing", 9, -5, crystalWingBurning, null)).registerAchievement();
    
     private boolean checkUpdate;
     private ModVersionChecker versionChecker;
     private String versionURL = "https://dl.dropbox.com/u/20748481/Minecraft/1.4.2/crystalWing.version";
     private String mcfTopic = "http://www.minecraftforum.net/topic/1009577-";
 
     public mod_crystalWing()
     {
         ModLoader.addName(crystalWing, "Crystal Wing");
         ModLoader.addName(crystalWingBurning, "BURNING WING");
         ModLoader.addName(crystalWingBurned, "Burned Wing");
         ModLoader.addAchievementDesc(burnedWing, "To Hell And Back", "Get a Burned Wing by entering in water with a Burning Wing.");
         ModLoader.addRecipe(new ItemStack(crystalWing, 1), new Object[] {
             "GGG", "EFF", Character.valueOf('G'), Item.ingotGold, Character.valueOf('E'), Item.enderPearl, Character.valueOf('F'), Item.feather
         });
        if(uses > 0)
        {
            crystalWing.setMaxDamage(uses - 1);
        }
        ModLoader.setInGameHook(this, true, true);
        
         checkUpdate = allowUpdateCheck;
         versionChecker = new ModVersionChecker(getName(), getVersion(), versionURL, mcfTopic, ModLoader.getLogger());
     }
 
     public String getVersion()
     {
        return "ML 1.4.2.r01";
     }
 
     public void load()
     {
         versionChecker.checkVersionWithLogging();
     }
    
     public boolean onTickInGame(float f, Minecraft mc)
    {        
         if(checkUpdate)
         {
             if(!versionChecker.isCurrentVersion())
                 for(String msg : versionChecker.getInGameMessage())
                     mc.thePlayer.addChatMessage(msg);
         }
         return false;
     }
 }
