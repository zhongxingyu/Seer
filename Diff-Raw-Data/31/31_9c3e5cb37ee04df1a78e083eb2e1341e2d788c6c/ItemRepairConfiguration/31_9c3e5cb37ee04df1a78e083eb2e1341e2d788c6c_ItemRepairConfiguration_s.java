 package de.codeinfection.quickwango.ItemRepair;
 
 import org.bukkit.Material;
 import org.bukkit.configuration.Configuration;
 
 /**
 *
 * @author VoLLi
 * @und Baeume Faithcaio
 */
 public class ItemRepairConfiguration
 {
     public final double price_perDamage;
     public final double price_enchantMultiplier_base;
     public final double price_enchantMultiplier_factor;
    public final double price_wood;
    public final double price_stone;
    public final double price_leather;
    public final double price_iron;
    public final double price_fire
    public final double price_gold;
    public final double price_diamond;
 
     public final Material repairBlocks_singleRepair_block;
     public final Material repairBlocks_completeRepair_block;
     
     public final Material repairBlocks_cheapRepair_block;
     public final int repairBlocks_cheapRepair_breakPercentage;
     public final int repairBlocks_cheapRepair_costPercentage;
     
     public ItemRepairConfiguration(Configuration config)
     {
         this.price_perDamage = config.getDouble("price.perDamage");
         this.price_enchantMultiplier_base = config.getDouble("price.enchantMultiplier.base");
         this.price_enchantMultiplier_factor = config.getDouble("price.enchantMultiplier.factor");
        this.price_wood = config.getDouble("price.price_wood");
        this.price_stone = config.getDouble("price.price_stone");
        this.price_leather = config.getDouble("price.price_leather");
        this.price_iron = config.getDouble("price.price_iron");
        this.price_fire = config.getDouble("price.price_fire");
        this.price_gold = config.getDouble("price.price_gold");
        this.price_diamond = config.getDouble("price.price_diamond");
 
         this.repairBlocks_singleRepair_block = Material.getMaterial(config.getInt("repairBlocks.singleRepair.block"));
         this.repairBlocks_completeRepair_block = Material.getMaterial(config.getInt("repairBlocks.completeRepair.block"));
 
 
         this.repairBlocks_cheapRepair_block = Material.getMaterial(config.getInt("repairBlocks.cheapRepair.block"));
         this.repairBlocks_cheapRepair_breakPercentage = config.getInt("repairBlocks.cheapRepair.breakPercentage");
         this.repairBlocks_cheapRepair_costPercentage = config.getInt("repairBlocks.cheapRepair.costPercentage");
     }
 }
