 package de.minestar.moneypit.modules;
 
 import org.bukkit.Material;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 import de.minestar.moneypit.data.protection.Protection;
 import de.minestar.moneypit.manager.ModuleManager;
 
 public class Module_Dispenser extends Module {
 
     private final String NAME = "dispenser";
 
     public Module_Dispenser(YamlConfiguration ymlFile) {
         this.writeDefaultConfig(NAME, ymlFile);
     }
 
     public Module_Dispenser(ModuleManager moduleManager, YamlConfiguration ymlFile) {
         super();
         this.init(moduleManager, ymlFile, Material.DISPENSER.getId(), NAME);
     }
 
     @Override
    public boolean addProtection(Protection protection, byte subData) {
         // register the protection
         return getProtectionManager().addProtection(protection);
     }
 }
