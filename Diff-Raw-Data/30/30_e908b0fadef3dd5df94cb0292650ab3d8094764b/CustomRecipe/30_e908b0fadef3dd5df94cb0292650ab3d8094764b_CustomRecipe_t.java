 package com.undeadscythes.udsplugin;
 
 import org.bukkit.*;
 import org.bukkit.inventory.*;
 import org.bukkit.material.*;
 
 /**
  * Custom recipes to be loaded on server start up.
  * @author UndeadScythes
  */
 public enum CustomRecipe {
     GRASS_BLOCK(Material.GRASS, "A", "B", Material.LONG_GRASS, Material.DIRT),
     CHAIN_HEAD(Material.CHAINMAIL_HELMET, "AAA", "A A", Material.FLINT),
     CHAIN_BODY(Material.CHAINMAIL_CHESTPLATE, "A A", "AAA", "AAA", Material.FLINT),
     CHAIN_LEGS(Material.CHAINMAIL_LEGGINGS, "AAA", "A A", "A A", Material.FLINT),
     CHAIN_FEET(Material.CHAINMAIL_BOOTS, "A A", "A A", Material.FLINT),
     ICE_BLOCK(Material.ICE, "AAA", "ABA", "AAA", Material.SNOW_BALL, Material.WATER_BUCKET),
     MOSSY_COBBLE(Material.MOSSY_COBBLESTONE, "AAA", "ABA", "AAA", Material.VINE, Material.COBBLESTONE),
     MOSSY_STONE(Material.SMOOTH_BRICK, 1, "AAA", "ABA", "AAA", Material.VINE, Material.SMOOTH_BRICK),
     CRACKED_STONE(Material.SMOOTH_BRICK, 2, Material.WOOD_PICKAXE, Material.SMOOTH_BRICK),
     CIRCLE_STONE(Material.SMOOTH_BRICK, 3, "AAA", "A A", "AAA", Material.SMOOTH_BRICK, 8),
     SNOW_LAYER(Material.SNOW, "AAA", Material.SNOW_BALL),
     CREEPER_EGG(Material.MONSTER_EGG, 50, "AAA", "ABA", "AAA", Material.GOLD_BLOCK, Material.SULPHUR),
     SKELETON_EGG(Material.MONSTER_EGG, 50, "AAA", "ABA", "AAA", Material.GOLD_BLOCK, Material.BONE),
     SPIDER_EGG(Material.MONSTER_EGG, 50, "AAA", "ABA", "AAA", Material.GOLD_BLOCK, Material.SPIDER_EYE),
     ZOMBIE_EGG(Material.MONSTER_EGG, 50, "AAA", "ABA", "AAA", Material.GOLD_BLOCK, Material.ROTTEN_FLESH),
     SLIME_EGG(Material.MONSTER_EGG, 50, "AAA", "ABA", "AAA", Material.GOLD_BLOCK, Material.SLIME_BALL),
     GHAST_EGG(Material.MONSTER_EGG, 50, "AAA", "ABA", "AAA", Material.GOLD_BLOCK, Material.GHAST_TEAR),
     PIG_ZOMBIE_EGG(Material.MONSTER_EGG, 50, "AAA", "ABA", "AAA", Material.GOLD_BLOCK, Material.GOLD_NUGGET),
     ENDERMAN_EGG(Material.MONSTER_EGG, 50, "AAA", "ABA", "AAA", Material.GOLD_BLOCK, Material.ENDER_PEARL),
     CAVE_SPIDER_EGG(Material.MONSTER_EGG, 50, "AAA", "ABA", "AAA", Material.GOLD_BLOCK, Material.STRING),
     SILVERFISH_EGG(Material.MONSTER_EGG, 50, "AAA", "ABA", "AAA", Material.GOLD_BLOCK, Material.STONE),
     BLAZE_EGG(Material.MONSTER_EGG, 50, "AAA", "ABA", "AAA", Material.GOLD_BLOCK, Material.BLAZE_ROD),
     MAGMACUBE_EGG(Material.MONSTER_EGG, 50, "AAA", "ABA", "AAA", Material.GOLD_BLOCK, Material.MAGMA_CREAM),
     PIG_EGG(Material.MONSTER_EGG, 50, "AAA", "ABA", "AAA", Material.GOLD_BLOCK, Material.PORK),
     SHEEP_EGG(Material.MONSTER_EGG, 50, "AAA", "ABA", "AAA", Material.GOLD_BLOCK, Material.WOOL),
     COW_EGG(Material.MONSTER_EGG, 50, "AAA", "ABA", "AAA", Material.GOLD_BLOCK, Material.LEATHER),
     CHICKEN_EGG(Material.MONSTER_EGG, 50, "AAA", "ABA", "AAA", Material.GOLD_BLOCK, Material.FEATHER),
     SQUID_EGG(Material.MONSTER_EGG, 50, "AAA", "ABA", "AAA", Material.GOLD_BLOCK, Material.INK_SACK),
     WOLF_EGG(Material.MONSTER_EGG, 50, "AAA", "ABA", "AAA", Material.GOLD_BLOCK, Material.WHEAT),
     MOOSHROOM_EGG1(Material.MONSTER_EGG, 50, "AAA", "ABA", "AAA", Material.GOLD_BLOCK, Material.RED_MUSHROOM),
     MOOSHROOM_EGG2(Material.MONSTER_EGG, 50, "AAA", "ABA", "AAA", Material.GOLD_BLOCK, Material.BROWN_MUSHROOM),
     VILLAGER_EGG(Material.MONSTER_EGG, 50, "AAA", "ABA", "AAA", Material.GOLD_BLOCK, Material.RED_ROSE),
     WEB_BLOCK(Material.WEB, "AAA", "A A", "AAA", Material.STRING),
     SADDLE(Material.SADDLE, "AAA", " B ", "C C", Material.LEATHER, Material.IRON_INGOT, Material.DIAMOND);
     
     final private Recipe recipe;
     
     @SuppressWarnings("fallthrough")
     private CustomRecipe(final boolean shaped, final Material out, final byte data, final String row1, final String row2, final String row3, final Material ing1, final Material ing2, final Material ing3, final int num) {
         final MaterialData item = new MaterialData(out, data);
         if(shaped) {
             recipe = new ShapedRecipe(item.toItemStack(num));
             switch((row1 == null ? 0 : 1) + (row2 == null ? 0 : 1) + (row3 == null ? 0 : 1)) {
                 case 1:
                     ((ShapedRecipe)recipe).shape(row1);
                     break;
                 case 2:
                     ((ShapedRecipe)recipe).shape(row1, row2);
                     break;
                 case 3:
                     ((ShapedRecipe)recipe).shape(row1, row2, row3);
                     break;
             }
            switch((ing1 == null ? 0 : 1) + (ing2 == null ? 0 : 1) + (ing3 == null ? 0 : 1)) {
                case 3:
                    ((ShapedRecipe)recipe).setIngredient('C', ing3);
                case 2:
                    ((ShapedRecipe)recipe).setIngredient('B', ing2);
                case 1:
                    ((ShapedRecipe)recipe).setIngredient('A', ing1);
                    break;
            }
         } else {
             recipe = new ShapelessRecipe(item.toItemStack(num));
            switch((ing1 == null ? 0 : 1) + (ing2 == null ? 0 : 1) + (ing3 == null ? 0 : 1)) {
                case 3:
                    ((ShapelessRecipe)recipe).addIngredient(ing3);
                case 2:
                    ((ShapelessRecipe)recipe).addIngredient(ing2);
                case 1:
                    ((ShapelessRecipe)recipe).addIngredient(ing1);
                    break;
            }
         }
        
     }
     
     private CustomRecipe(final Material out, final int data, final String row1, final String row2, final String row3, final Material ing1, final Material ing2) {
         this(true, out, (byte)data, row1, row2, row3, ing1, ing2, null, 1);
     }
  
     private CustomRecipe(final Material out, final int data, final String row1, final String row2, final String row3, final Material ing1, final int num) {
         this(true, out, (byte)data, row1, row2, row3, ing1, null, null, num);
     }
  
     private CustomRecipe(final Material out, final int data, final Material ing1, final Material ing2) {
         this(false, out, (byte)data, null, null, null, ing1, ing2, null, 1);
     }
  
     private CustomRecipe(final Material out, final String row1, final String row2, final String row3, final Material ing1, final Material ing2, final Material ing3) {
         this(true, out, (byte)0, row1, row2, row3, ing1, ing2, ing3, 1);
     }
     
     private CustomRecipe(final Material out, final String row1, final String row2, final String row3, final Material ing1, final int num) {
         this(true, out, (byte)0, row1, row2, row3, ing1, null, null, num);
     }
     
     private CustomRecipe(final Material out, final String row1, final String row2, final String row3, final Material ing1, final Material ing2) {
         this(true, out, (byte)0, row1, row2, row3, ing1, ing2, null, 1);
     }
 
     private CustomRecipe(final Material out, final String row1, final String row2, final Material ing1, final Material ing2) {
         this(true, out, (byte)0, row1, row2, null, ing1, ing2, null, 1);
     }
     
     private CustomRecipe(final Material out, final String row1, final String row2, final String row3, final Material ing1) {
         this(true, out, (byte)0, row1, row2, row3, ing1, null, null, 1);
     }
     
     private CustomRecipe(final Material out, final String row1, final String row2, final Material ing1) {
         this(true, out, (byte)0, row1, row2, null, ing1, null, null, 1);
     }
     
     private CustomRecipe(final Material out, final String row1, final Material ing1) {
         this(true, out, (byte)0, row1, null, null, ing1, null, null, 1);
     }
     
     /**
      * Get the Recipe belonging to this enum.
      * @return Recipe
      */
     public final Recipe getRecipe() {
         return recipe;
     }
 }
