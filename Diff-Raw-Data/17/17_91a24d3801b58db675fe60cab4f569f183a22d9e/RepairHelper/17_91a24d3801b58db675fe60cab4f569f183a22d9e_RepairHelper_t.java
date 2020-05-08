 package liquidrepair.util;
 
 import java.util.List;
 import java.util.HashMap;
 
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.util.MathHelper;
 
 import net.minecraftforge.liquids.LiquidStack;
 
 import mods.tinker.tconstruct.TConstruct;
 import mods.tinker.tconstruct.library.crafting.CastingRecipe;
 import mods.tinker.tconstruct.library.tools.AbilityHelper;
 import mods.tinker.tconstruct.library.tools.ToolCore;
import mods.tinker.tconstruct.library.util.IPattern;
 
 public class RepairHelper {
     public static boolean isTool(ItemStack possibleTool)
     {
         if(possibleTool == null) {
             return false;
         }
 
         if(possibleTool.getItem() instanceof ToolCore) {
             return true;
         }
         return false;
     }
 
     /**
      * Find the liquid corresponding to the possibleTool's head. Returns null
      * if there doesn't seem to be an appropriate liquid, for whatever reason.
      */
     public static LiquidStack findLiquidFor(ItemStack tool)
     {
         if(!isTool(tool)) { return null; }
 
         Item head = ((ToolCore) tool.getItem()).getHeadItem();
         if(head == null) {
             return null;
         }
 
         int headMaterial = tool.getTagCompound().getCompoundTag("InfiTool").getInteger("Head");
         return findLiquidFor(head, headMaterial);
     }
 
     public static HashMap<HashStack, LiquidStack> liquidMap = new HashMap<HashStack, LiquidStack>();
 
     /**
      * Find the liquid the given toolPart can be cast from. Returns null if
      * there doesn't seem to be a molten metal the toolPart can be cast from.
      */
     @SuppressWarnings("unchecked")
     public static LiquidStack findLiquidFor(Item toolPart, int materialID) {
         if(toolPart == null) {
             return null;
         }
 
         HashStack toolPartStack = new HashStack(new ItemStack(toolPart, 1, materialID));
 
         if(liquidMap.containsKey(toolPartStack)) {
             if(liquidMap.get(toolPartStack) == null) {
                 return null;
             }
             return liquidMap.get(toolPartStack).copy();
         }
 
         List<CastingRecipe> recipes = (List<CastingRecipe>)
                                       TConstruct.tableCasting.getCastingRecipes();
 
         for(CastingRecipe recipe: recipes) {
             if(recipe.output.isItemEqual(toolPartStack.itemStack)) {
                LiquidStack repairLiquid = recipe.castingMetal.copy();

                // Work around a bug in Tinker's for 1.5.2
                int cost = ((IPattern) recipe.cast.getItem()).getPatternCost(recipe.cast);
                repairLiquid.amount = (cost * TConstruct.ingotLiquidValue) / 2;
                // Ordinarily, the cost should just be the recipe's
                // castingMetal amount, but the value is incorrectly
                // initialized, therefore we have to redo it. Of note is that
                // tinker's casting table also do this! And probably for the
                // same reason.

                liquidMap.put(toolPartStack, repairLiquid);
                return repairLiquid;
             }
         }
 
         // Cache negatives, too.
         liquidMap.put(toolPartStack, null);
         return null;
     }
 
     /**
      * Tells you specifically if the given tool is repairable using molten
      * metal.
      */
     public static boolean isRepairable(ItemStack tool)
     {
         if(!isTool(tool)) { return false; }
 
         NBTTagCompound tag = tool.getTagCompound().getCompoundTag("InfiTool");
         if(tag.getInteger("Damage") > 0 && !tag.hasKey("charge") && findLiquidFor(tool) != null) {
             return true;
         }
 
         return false;
     }
 
     /**
      * "How damaged is this?"
      * Gives you the ratio (damage/max_damage) for the given tool.
      */
     public static float getDamageRatio(ItemStack tool)
     {
         if(!isTool(tool) || !isRepairable(tool)) { return 0.0F; }
 
         NBTTagCompound tag = tool.getTagCompound().getCompoundTag("InfiTool");
         if(tag.getInteger("BaseDurability") == 0) { return 0.0F; }
 
         return ((float) tag.getInteger("Damage")) /
                ((float) tag.getInteger("BaseDurability"));
     }
 
     /**
      * Repair a tool by a given fraction of its base durability.
      */
     public static boolean performRepair(ItemStack tool, float fraction) {
         if(!isTool(tool) || !isRepairable(tool)) { return false; }
 
         NBTTagCompound tag = tool.getTagCompound().getCompoundTag("InfiTool");
         int durability = tag.getInteger("BaseDurability");
         int repairAmount = MathHelper.ceiling_float_int(fraction * (float) durability);
 
         if(tag.getBoolean("Broken")) {
             int repairCount = tag.getInteger("RepairCount");
             repairCount += 1;
             tag.setInteger("RepairCount", repairCount);
             tag.setBoolean("Broken", false);
         }
 
         int damage = tag.getInteger("Damage");
         damage -= repairAmount;
         if(damage < 0) { damage = 0; }
         tag.setInteger("Damage", damage);
 
         AbilityHelper.damageTool(tool, 0, null, true);
 
         return true;
     }
 }
