 package mods.cc.rock.recipie;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import mods.cc.rock.item.ModItems;
 import mods.cc.rock.tileentity.TileEntityFoodAssembler;
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
 
 @SuppressWarnings({ "unchecked", "rawtypes" })
 public class CraftingManagerCC{
 	
     /** The static instance of this class */
     private static final CraftingManagerCC instance = new CraftingManagerCC();
 
     /** A list of all the recipes added */
     
 	private List recipes = new ArrayList();
 
     /**
      * Returns the static instance of this class
      */
     public static final CraftingManagerCC getInstance()
     {
         return instance;
     }
 
     private CraftingManagerCC()
     {
         
     	addShapelessRecipe(new ItemStack(Item.bread), new Object[]{ModItems.oilCookedLight,ModItems.oilCookedLight, ModItems.itemFlour, ModItems.itemFlour, ModItems.itemFlour, ModItems.itemFlour, Item.potion});
        addShapelessRecipe(new ItemStack(ModItems.oilLight), new Object[]{Block.plantYellow, Block.plantYellow, Item.flint, Item.potion});
        addShapelessRecipe(new ItemStack(ModItems.oilDark), new Object[]{Block.plantRed, Block.plantRed, Item.flint, Item.potion});
         
     }
     //add a shapeless recipe to the food preparation table. max 8 ingrediants
     public void addShapelessRecipe(ItemStack par1ItemStack, Object ... par2ArrayOfObj)
     {
         ArrayList arraylist = new ArrayList();
         Object[] aobject = par2ArrayOfObj;
         int i = par2ArrayOfObj.length;
         
         if(i>8){
         	throw new RuntimeException("Invalid Food Preparation Table Recipe!");
         }
         
         for (int j = 0; j < i; ++j)
         {
             Object object1 = aobject[j];
 
             if (object1 instanceof ItemStack)
             {
                 arraylist.add(((ItemStack)object1).copy());
             }
             else if (object1 instanceof Item)
             {
                 arraylist.add(new ItemStack((Item)object1));
             }
             else
             {
                 if (!(object1 instanceof Block))
                 {
                     throw new RuntimeException("Invalid Food Preparation Table Recipe!");
                 }
 
                 arraylist.add(new ItemStack((Block)object1));
             }
         }
 
         this.recipes.add(new ShapelessRecipeCC(par1ItemStack, arraylist));
     }
 
     public ItemStack findMatchingRecipe(TileEntityFoodAssembler tileEntity, World par2World)
     {
         int i = 0;
         ItemStack itemstack = null;
         ItemStack itemstack1 = null;
         int j;
 
         for (j = 0; j < 7; ++j)
         {
             ItemStack itemstack2 = tileEntity.getStackInSlot(j);
 
             if (itemstack2 != null)
             {
                 if (i == 0)
                 {
                     itemstack = itemstack2;
                 }
 
                 if (i == 1)
                 {
                     itemstack1 = itemstack2;
                 }
 
                 ++i;
             }
         }
 
         if (i == 2 && itemstack.itemID == itemstack1.itemID && itemstack.stackSize == 1 && itemstack1.stackSize == 1 && Item.itemsList[itemstack.itemID].isRepairable())
         {
             Item item = Item.itemsList[itemstack.itemID];
             int k = item.getMaxDamage() - itemstack.getItemDamageForDisplay();
             int l = item.getMaxDamage() - itemstack1.getItemDamageForDisplay();
             int i1 = k + l + item.getMaxDamage() * 5 / 100;
             int j1 = item.getMaxDamage() - i1;
 
             if (j1 < 0)
             {
                 j1 = 0;
             }
 
             return new ItemStack(itemstack.itemID, 1, j1);
         }
         
         else
         {
             for (j = 0; j < this.recipes.size(); ++j)
             {
                 IRecipeCC irecipe = (IRecipeCC)this.recipes.get(j);
 
                 if (irecipe.matches(tileEntity, par2World))
                 {
                     return irecipe.getCraftingResult(tileEntity);
                 }
             }
 
             return null;
         }
     }
 
     /**
      * returns the List<> of all recipes
      */
     public List getRecipeList()
     {
         return this.recipes;
     }
 
 }
