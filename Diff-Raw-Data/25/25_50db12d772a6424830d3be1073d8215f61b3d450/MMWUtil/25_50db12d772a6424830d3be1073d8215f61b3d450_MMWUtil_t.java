 package net.minecraft.src;
 
 import java.util.*;
 import java.lang.reflect.*;
 import net.minecraft.client.Minecraft;
 
 public class MMWUtil {
   private static Minecraft mc = Minecraft.getMinecraft();
   private static boolean isObfuscated;
   private static Method method_RegisterEntityID = null;
   private static Properties translateTable;
   private static BiomeGenBase[] standardBiomes;
   private static CraftingManager craftingManager;
   
   static {
     isObfuscated = false;
     try {
       Class.forName("net.minecraft.src.MathHelper", false, MMWUtil.class.getClassLoader());
     } catch (ClassNotFoundException e) {
       isObfuscated = true;
     }
     try {
       method_RegisterEntityID = EntityList.class.getDeclaredMethod((isObfuscated) ? "a" : "addMapping", Class.class, String.class, int.class);
     } catch (Exception ignored) { }
     standardBiomes = (BiomeGenBase[])((BiomeGenBase[])(new LinkedList()).toArray(new BiomeGenBase[0]));
     craftingManager = CraftingManager.getInstance();
   }
 
   public static boolean isObfuscated() {
     return isObfuscated;
   }
 
   public static Object getPrivateValue(Class cls, Object obj, String fieldName, String obfuscatedName) {
     try {
       Field field = cls.getDeclaredField((isObfuscated) ? obfuscatedName : fieldName);
       field.setAccessible(true);
       return field.get(obj);
     } catch (Exception exception) {
       return null;
     }
   }
 
   public static boolean setPrivateValue(Class cls, Object obj, String fieldName, String obfuscatedName, Object value) {
     try {
       Field field = cls.getDeclaredField((isObfuscated) ? obfuscatedName : fieldName);
       field.setAccessible(true);
       field.set(obj, value);
       return true;
     } catch (Exception exception) {
       return false;
     }
   }
 
   public static void removeSpawn(Class entityType, EnumCreatureType creatureType) {
     for (int idx=0; idx<standardBiomes.length; idx++) {
       List spawnList = standardBiomes[idx].getSpawnableList(creatureType);
       if (spawnList!=null) {
         Iterator itr = spawnList.iterator();
         while (itr.hasNext()) {
           SpawnListEntry entry = (SpawnListEntry)itr.next();
           if (entry.entityClass == entityType) {
             itr.remove();
           }
         }
       }
     }
   }
 
   public static void addSpawn(Class entity, int weight, int min, int max, EnumCreatureType creatureType, BiomeGenBase[] biomes) {
     for (int idx=0; idx<biomes.length; idx++) {
       List spawnList = biomes[idx].getSpawnableList(creatureType);
       if (spawnList!=null) {
         boolean found = false;
         Iterator itr = spawnList.iterator();
         while (itr.hasNext()) {
           SpawnListEntry entry = (SpawnListEntry)itr.next();
           if (entry.entityClass==entity) {
             entry.itemWeight = weight;
             entry.minGroupCount = min;
             entry.maxGroupCount = max;
             found = true;
             break;
           }
         }
         if (!found) {
           spawnList.add(new SpawnListEntry(entity, weight, min, max));
         }
       }
     }
   }
 
   public static void addSpawn(Class var0, int var1, int var2, int var3, EnumCreatureType var4) {
     addSpawn(var0, var1, var2, var3, var4, standardBiomes);
   }
 
   public static void killAll(Class entityType, EnumCreatureType creatureType) {
     for (int idx=0; idx<mc.theWorld.loadedEntityList.size(); idx++) {
       Entity ent = (Entity)mc.theWorld.loadedEntityList.get(idx);
       if ((ent!=null) && (entityType.isInstance(ent))) {
         ((EntityLiving)ent).setDead();
       }
     }
    IntegratedServer server = mc.getIntegratedServer();
    if (server!=null) {
      for (int idx=0;idx<server.worldServers.length;idx++) {
        for (int idx2=0; idx2<server.worldServers[idx].loadedEntityList.size(); idx2++) {
          Entity ent = (Entity)server.worldServers[idx].loadedEntityList.get(idx2);
          if ((ent!=null) && (entityType.isInstance(ent))) {
            ((EntityLiving)ent).setDead();
          }
        }
      }    
    }
   }
   
 	public static void removeRecipe(int item) {
 		Iterator<?> itr = craftingManager.getRecipeList().iterator();
 		
 		while (itr.hasNext()) {
 			Object o = itr.next();
 			if (o instanceof ShapedRecipes) {
 				if (((ShapedRecipes) o).recipeOutputItemID == item) {
 					itr.remove();
 				}
 			} else if (o instanceof ShapelessRecipes) {
 				if (((ShapelessRecipes) o).getRecipeOutput().itemID == item) {
 					itr.remove();
 				}
 			}
 		}
 	}
 
   public static void removeRecipe(ItemStack par1ItemStack, Object ... par2ArrayOfObj) {
     ShapedRecipes checkRecipe;
 		Iterator<?> itr = craftingManager.getRecipeList().iterator();
 
     // Set recipe to check against
     String recipeString = "";
     int itemIndex = 0;
     int width = 0;
     int height = 0;
     int numRows;
     HashMap recipeTokens;
 
     // Build recipe string
     if (par2ArrayOfObj[itemIndex] instanceof String[]) {
       String[] recipeRows = (String[])((String[])par2ArrayOfObj[itemIndex++]);
       numRows = recipeRows.length;
 
       for (int idx=0; idx<numRows; idx++) {
         String row = recipeRows[idx];
         height++;
         width = row.length();
         recipeString = recipeString + row;
       }
     } else {
       while (par2ArrayOfObj[itemIndex] instanceof String) {
         String row = (String)par2ArrayOfObj[itemIndex++];
         height++;
         width = row.length();
         recipeString = recipeString + row;
       }
     }
     // Build hash of token to replace in recipe string
     for (recipeTokens = new HashMap(); itemIndex < par2ArrayOfObj.length; itemIndex += 2) {
       Character token = (Character)par2ArrayOfObj[itemIndex];
       ItemStack tokenItem = null;
 
       if (par2ArrayOfObj[itemIndex + 1] instanceof Item) {
         tokenItem = new ItemStack((Item)par2ArrayOfObj[itemIndex + 1]);
       } else if (par2ArrayOfObj[itemIndex + 1] instanceof Block) {
         tokenItem = new ItemStack((Block)par2ArrayOfObj[itemIndex + 1], 1, -1);
       } else if (par2ArrayOfObj[itemIndex + 1] instanceof ItemStack) {
         tokenItem = (ItemStack)par2ArrayOfObj[itemIndex + 1];
       }
       recipeTokens.put(token, tokenItem);
     }
     // Build recipe array of ItemStacks
     ItemStack[] recipeStacks = new ItemStack[width * height];
 
     for (int idx=0; idx<width * height; idx++) {
       char token = recipeString.charAt(idx);
       if (recipeTokens.containsKey(Character.valueOf(token))) {
         recipeStacks[idx] = ((ItemStack)recipeTokens.get(Character.valueOf(token))).copy();
       } else {
         recipeStacks[idx] = null;
       }
     }
     checkRecipe = new ShapedRecipes(width, height, recipeStacks, par1ItemStack);
 		// Look through list for recipe and remove if found
 		while (itr.hasNext()) {
 			Object o = itr.next();
 			if (o instanceof ShapedRecipes) {
         if (recipesEqual((ShapedRecipes)o, checkRecipe)) {
 					itr.remove();
         }
 			}
 		}
   }
 
   public static void removeShapelessRecipe(ItemStack par1ItemStack, Object ... par2ArrayOfObj) {
     ShapelessRecipes checkRecipe;
 		Iterator<?> itr = craftingManager.getRecipeList().iterator();
 		
     // Set recipe to check against
     ArrayList recipeItems = new ArrayList();
     Object[] items = par2ArrayOfObj;
 
     for (int idx=0; idx<par2ArrayOfObj.length; idx++) {
       if (items[idx] instanceof ItemStack) {
         recipeItems.add(((ItemStack)items[idx]).copy());
       } else if (items[idx] instanceof Item) {
         recipeItems.add(new ItemStack((Item)items[idx]));
       } else if (items[idx] instanceof Block) {
         recipeItems.add(new ItemStack((Block)items[idx]));
       }
     }
     checkRecipe = new ShapelessRecipes(par1ItemStack, recipeItems);
 		// Look through list for recipe and remove if found
 		while (itr.hasNext()) {
 			Object o = itr.next();
 			if (o instanceof ShapelessRecipes) {
         if (recipesEqual((ShapelessRecipes)o, checkRecipe)) {
 					itr.remove();
         }
 			}
 		}
     
   }
 
   public static boolean recipesEqual(ShapedRecipes r1, ShapedRecipes r2) {
     // Different output, quick exit
     if (!ItemStack.areItemStacksEqual(r1.getRecipeOutput(), r2.getRecipeOutput())) {
       return false;
     }
     try {
       ItemStack[] recipe1Items = (ItemStack[])getPrivateValue(ShapedRecipes.class, r1, "recipeItems", "d");
       ItemStack[] recipe2Items = (ItemStack[])getPrivateValue(ShapedRecipes.class, r2, "recipeItems", "d");
       if (recipe1Items.length != recipe2Items.length) {
         return false;
       }
       for (int idx=0; idx<recipe1Items.length; idx++) {
         if (!ItemStack.areItemStacksEqual(recipe1Items[idx], recipe2Items[idx])) {
           return false;
         }
       }
     } catch (Exception error) {
       return false;
     }
     return true;
   }
   
   public static boolean recipesEqual(ShapelessRecipes r1, ShapelessRecipes r2) {
     // Different output, quick exit
     if (!ItemStack.areItemStacksEqual(r1.getRecipeOutput(), r2.getRecipeOutput())) {
       return false;
     }
     try {
       List recipe1Items = (List)getPrivateValue(ShapelessRecipes.class, r1, "recipeItems", "b");
       List recipe2Items = (List)getPrivateValue(ShapelessRecipes.class, r2, "recipeItems", "b");
       if (recipe1Items.size() != recipe2Items.size()) {
         return false;
       }
       for (int idx=0; idx<recipe1Items.size(); idx++) {
         if (!ItemStack.areItemStacksEqual((ItemStack)recipe1Items.get(idx), (ItemStack)recipe2Items.get(idx))) {
           return false;
         }
       }
     } catch (Exception error) {
       return false;
     }
     return true;
   }
 
   public static void addRecipe(ItemStack var0, Object ... var1) {
     craftingManager.addRecipe(var0, var1);
   }
 
   public static void addShapelessRecipe(ItemStack var0, Object ... var1) {
     craftingManager.addShapelessRecipe(var0, var1);
   }
 
   public static void registerEntityID(Class var0, String var1, int var2) {
     try {
       method_RegisterEntityID.invoke((Object)null, var0, var1, var2);
     } catch (Exception ignored) { }
   }
   
   public static void addLocalization(String key, String lang, String value) {
     Properties translateTable; 
 
     StringTranslate.getInstance().setLanguage(lang);
     try {
       translateTable = (Properties)getPrivateValue(StringTranslate.class, StringTranslate.getInstance(), "translateTable", "b");
     } catch (Exception aborts) {
       return;
     }
     translateTable.setProperty(key, value);
   }
 
   public static void addLocalization(String key, String value) {
     addLocalization(key, "en_US", value);
   }
   
   public static void addLocalization(Item item, String value) {
     addLocalization(item.getItemName()+".name", "en_US", value);
   }
 
   public static void addLocalization(Block block, String value) {
     addLocalization(block.getBlockName()+".name", "en_US", value);
   }
 }
 
 
 
 
