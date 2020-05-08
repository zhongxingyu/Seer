 package de.codeinfection.quickwango.ItemRepair;
 
 import java.util.EnumMap;
 import java.util.Map;
 import org.bukkit.Material;
 
 /**
  * Maps items to thair base materials
  *
  * @author Phillip Schichtel
  */
 public enum Item
 {
    IRON_SPADE(Material.IRON_SPADE, BaseMaterial.IRON, 1),
     IRON_PICKAXE(Material.IRON_PICKAXE, BaseMaterial.IRON, 3),
     IRON_AXE(Material.IRON_AXE, BaseMaterial.IRON, 3),
     FLINT_AND_STEEL(Material.FLINT_AND_STEEL, BaseMaterial.IRON, 1),
     BOW(Material.BOW, BaseMaterial.WOOD, 2),
     IRON_SWORD(Material.IRON_SWORD, BaseMaterial.IRON, 2),
     WOOD_SWORD(Material.WOOD_SWORD, BaseMaterial.WOOD, 2),   
    WOOD_SPADE(Material.WOOD_SPADE, BaseMaterial.WOOD, 1),
     WOOD_PICKAXE(Material.WOOD_PICKAXE, BaseMaterial.WOOD, 3),    
     WOOD_AXE(Material.WOOD_AXE, BaseMaterial.WOOD, 3),
     STONE_SWORD(Material.STONE_SWORD, BaseMaterial.STONE, 2),
    STONE_SPADE(Material.STONE_SPADE, BaseMaterial.STONE, 1),    
     STONE_PICKAXE(Material.STONE_PICKAXE, BaseMaterial.STONE, 3),
     STONE_AXE(Material.STONE_AXE, BaseMaterial.STONE, 3),
     DIAMOND_SWORD(Material.DIAMOND_SWORD, BaseMaterial.DIAMOND, 2),
    DIAMOND_SPADE(Material.DIAMOND_SPADE, BaseMaterial.DIAMOND, 1),
     DIAMOND_PICKAXE(Material.DIAMOND_PICKAXE, BaseMaterial.DIAMOND, 3),
     DIAMOND_AXE(Material.DIAMOND_AXE, BaseMaterial.DIAMOND, 3),
     GOLD_SWORD(Material.GOLD_SWORD, BaseMaterial.GOLD, 2),
    GOLD_SPADE(Material.GOLD_SPADE, BaseMaterial.GOLD, 1),    
     GOLD_PICKAXE(Material.GOLD_PICKAXE, BaseMaterial.GOLD, 3),
     GOLD_AXE(Material.GOLD_AXE, BaseMaterial.GOLD, 3),
     WOOD_HOE(Material.WOOD_HOE, BaseMaterial.WOOD, 2),
     STONE_HOE(Material.STONE_HOE, BaseMaterial.STONE, 2),    
     IRON_HOE(Material.IRON_HOE, BaseMaterial.IRON, 2),
     DIAMOND_HOE(Material.DIAMOND_HOE, BaseMaterial.DIAMOND, 2),
     GOLD_HOE(Material.GOLD_HOE, BaseMaterial.GOLD, 2),
     LEATHER_HELMET(Material.LEATHER_HELMET, BaseMaterial.LEATHER, 5),
     LEATHER_CHESTPLATE(Material.LEATHER_CHESTPLATE, BaseMaterial.LEATHER, 8),
     LEATHER_LEGGINGS(Material.LEATHER_LEGGINGS, BaseMaterial.LEATHER, 7),
     LEATHER_BOOTS(Material.LEATHER_BOOTS, BaseMaterial.LEATHER, 4),
     CHAINMAIL_HELMET(Material.CHAINMAIL_HELMET, BaseMaterial.FIRE, 5),
     CHAINMAIL_CHESTPLATE(Material.CHAINMAIL_CHESTPLATE, BaseMaterial.FIRE, 8),
     CHAINMAIL_LEGGINGS(Material.CHAINMAIL_LEGGINGS, BaseMaterial.FIRE, 7),
     CHAINMAIL_BOOTS(Material.CHAINMAIL_BOOTS, BaseMaterial.FIRE, 4),
     IRON_HELMET(Material.IRON_HELMET, BaseMaterial.IRON, 5),
     IRON_CHESTPLATE(Material.IRON_CHESTPLATE, BaseMaterial.IRON, 8),
     IRON_LEGGINGS(Material.IRON_LEGGINGS, BaseMaterial.IRON, 7),
     IRON_BOOTS(Material.IRON_BOOTS, BaseMaterial.IRON, 4),
     DIAMOND_HELMET(Material.DIAMOND_HELMET, BaseMaterial.DIAMOND, 5),
     DIAMOND_CHESTPLATE(Material.DIAMOND_CHESTPLATE, BaseMaterial.DIAMOND, 8),
     DIAMOND_LEGGINGS(Material.DIAMOND_LEGGINGS, BaseMaterial.DIAMOND, 7),
     DIAMOND_BOOTS(Material.DIAMOND_BOOTS, BaseMaterial.DIAMOND, 4),
     GOLD_HELMET(Material.GOLD_HELMET, BaseMaterial.GOLD, 5),
     GOLD_CHESTPLATE(Material.GOLD_CHESTPLATE, BaseMaterial.GOLD, 8),
     GOLD_LEGGINGS(Material.GOLD_LEGGINGS, BaseMaterial.GOLD, 7),
     GOLD_BOOTS(Material.GOLD_BOOTS, BaseMaterial.GOLD, 4),
     FISHING_ROD(Material.FISHING_ROD, BaseMaterial.WOOD, 2),
     SHEARS(Material.SHEARS, BaseMaterial.IRON, 2);
 
     private static final Map<Material, Item> BY_MATERIAL = new EnumMap<Material, Item>(Material.class);
 
     private final Material material;
     private final BaseMaterial baseMaterial;
     private final int baseMaterialCount;
 
     private Item(final Material material, final BaseMaterial baseMaterial, final int baseMaterialCount)
     {
         this.material = material;
         this.baseMaterial = baseMaterial;
         this.baseMaterialCount = baseMaterialCount;
     }
 
     /**
      * Returns the material of this item
      *
      * @return the material
      */
     public Material getMaterial()
     {
         return this.material;
     }
 
     /**
      * Returns the base material of this item
      *
      * @return the base material
      */
     public BaseMaterial getBaseMaterial()
     {
         return this.baseMaterial;
     }
 
     /**
      * Returns the count of the required base materials
      *
      * @return the count the the required base materials
      */
     public int getBaseMaterialCount()
     {
         return this.baseMaterialCount;
     }
 
     /**
      * Returns an item by it's material
      *
      * @param material the material of the item
      * @return the item or null if not found
      */
     public static Item getByMaterial(Material material)
     {
         return BY_MATERIAL.get(material);
     }
 
     static
     {
         for (Item item : values())
         {
             BY_MATERIAL.put(item.getMaterial(), item);
         }
     }
 }
