 
 package me.heldplayer.plugins.nei.mystcraft.client;
 
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import me.heldplayer.plugins.nei.mystcraft.Objects;
 import me.heldplayer.plugins.nei.mystcraft.client.renderer.InkMixerOverlayRenderer;
 import me.heldplayer.util.HeldCore.client.GuiHelper;
 import net.minecraft.client.gui.inventory.GuiContainer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.nbt.NBTTagString;
 import net.minecraft.util.StatCollector;
 import net.minecraftforge.oredict.OreDictionary;
 
 import org.lwjgl.opengl.GL11;
 
 import codechicken.core.gui.GuiDraw;
 import codechicken.nei.NEIClientUtils;
 import codechicken.nei.PositionedStack;
 import codechicken.nei.api.IRecipeOverlayRenderer;
 import codechicken.nei.api.IStackPositioner;
 import codechicken.nei.recipe.GuiRecipe;
 import codechicken.nei.recipe.RecipeInfo;
 import codechicken.nei.recipe.TemplateRecipeHandler;
 
 import com.xcompwiz.mystcraft.api.MystAPI;
 import com.xcompwiz.mystcraft.api.MystObjects;
 import com.xcompwiz.mystcraft.api.internals.Color;
 import com.xcompwiz.mystcraft.api.internals.ColorGradient;
 
 @SuppressWarnings("rawtypes")
 public class InkMixerRecipeHandler extends TemplateRecipeHandler {
 
     public class CachedInkMixerRecipe extends CachedRecipe {
 
         public String[] modifiers;
         public ColorGradient gradient;
        public long frame;
         private PositionedStack stack;
         private PositionedStack leftOver;
         private PositionedStack ingredient;
         private ArrayList<PositionedStack> ingredients;
 
         public CachedInkMixerRecipe(Object ingredient) {
             this.ingredients = new ArrayList<PositionedStack>();
 
             if (ingredient instanceof ItemStack) {
                 this.ingredient = new PositionedStack(ingredient, 74, 29);
                 this.ingredients.add(this.ingredient);
             }
             else if (ingredient instanceof String) {
                 ArrayList<ItemStack> list = OreDictionary.getOres((String) ingredient);
 
                 if (list.size() > 0) {
                     this.ingredient = new PositionedStack(list, 74, 29);
                     this.ingredients.add(this.ingredient);
                 }
             }
             else if (ingredient instanceof Integer) {
                 this.ingredient = new PositionedStack(new ItemStack((Integer) ingredient, 1, OreDictionary.WILDCARD_VALUE), 74, 29);
                 this.ingredients.add(this.ingredient);
             }
             else {
                 this.ingredient = null;
             }
 
             if (this.ingredient != null) {
                 this.ingredient.setPermutationToRender(Objects.rnd.nextInt(this.ingredient.items.length));
             }
 
             InkMixerRecipe result = Integrator.getInkMixerRecipe(this.ingredient != null ? this.ingredient.item : null);
             if (result == null) {
                 this.modifiers = null;
                 this.gradient = null;
             }
             else {
                 this.modifiers = result.modifiers;
                 this.gradient = result.gradient;
             }
             this.frame = 0;
 
             if (this.modifiers != null) {
                 ItemStack stack = MystAPI.itemFact.buildLinkPage(this.modifiers);
 
                 this.stack = new PositionedStack(stack, 147, 37);
             }
             else {
                 ItemStack stack = MystAPI.itemFact.buildLinkPage();
 
                 this.stack = new PositionedStack(stack, 147, 37);
             }
 
             this.ingredients.add(new PositionedStack(new ItemStack(MystObjects.inkvial), 3, 16));
             this.ingredients.add(new PositionedStack(new ItemStack(Item.paper), 3, 37));
             this.leftOver = new PositionedStack(new ItemStack(Item.glassBottle), 147, 16);
         }
 
         @Override
         public PositionedStack getResult() {
             return this.stack;
         }
 
         @Override
         public ArrayList<PositionedStack> getIngredients() {
             if (!NEIClientUtils.shiftKey() && InkMixerRecipeHandler.this.cycleticks % 20 == 0) {
                 this.ingredient.setPermutationToRender(Objects.rnd.nextInt(this.ingredient.items.length));
             }
             return this.ingredients;
         }
 
         @Override
         public PositionedStack getIngredient() {
             return this.ingredients.get(0);
         }
 
         @Override
         public PositionedStack getOtherStack() {
             return this.leftOver;
         }
 
         @Override
         public boolean equals(Object obj) {
             if (this == obj) {
                 return true;
             }
             if (obj == null) {
                 return false;
             }
             if (!(obj instanceof CachedInkMixerRecipe)) {
                 return false;
             }
             CachedInkMixerRecipe other = (CachedInkMixerRecipe) obj;
             if (!this.getOuterType().equals(other.getOuterType())) {
                 return false;
             }
             if (this.ingredient == null) {
                 if (other.ingredient != null) {
                     return false;
                 }
             }
             else if (this.ingredient.item == null) {
                 if (other.ingredient.item != null) {
                     return false;
                 }
             }
             else if (!ItemStack.areItemStacksEqual(this.ingredient.item, other.ingredient.item)) {
                 return false;
             }
             if (!Arrays.equals(this.modifiers, other.modifiers)) {
                 return false;
             }
             if (this.stack == null) {
                 if (other.stack != null) {
                     return false;
                 }
             }
             else if (this.stack.item == null) {
                 if (other.stack.item != null) {
                     return false;
                 }
             }
             else if (!ItemStack.areItemStacksEqual(this.stack.item, other.stack.item)) {
                 return false;
             }
             return true;
         }
 
         private InkMixerRecipeHandler getOuterType() {
             return InkMixerRecipeHandler.this;
         }
 
     }
 
     @Override
     public String getRecipeName() {
         return StatCollector.translateToLocal("tile.myst.inkmixer.name");
     }
 
     @Override
     public String getGuiTexture() {
         return "mystcraft:gui/inkmixer.png";
     }
 
     @Override
     public void loadCraftingRecipes(String outputId, Object... results) {
         if (MystObjects.page == null) {
             return;
         }
 
         if (outputId.equals("item") || outputId.equals("inkmixer")) {
             if (results.length > 0) {
                 this.loadCraftingRecipes((ItemStack) results[0]);
             }
             else {
                 ArrayList recipes = Integrator.getALlInkMixerRecipes();
 
                 for (Object ingr : recipes) {
                     CachedInkMixerRecipe recipe = new CachedInkMixerRecipe(ingr);
                     if (recipe.modifiers != null) {
                         this.arecipes.add(recipe);
                     }
                 }
             }
             return;
         }
     }
 
     @Override
     public void loadCraftingRecipes(ItemStack result) {
         if (MystObjects.page == null) {
             return;
         }
 
         if (result.getItem() == MystObjects.page) {
             NBTTagCompound compound = result.getTagCompound();
             if (compound == null) {
                 return;
             }
 
             if (!compound.hasKey("linkpanel")) {
                 return;
             }
             NBTTagCompound linkPanelCompound = compound.getCompoundTag("linkpanel");
 
             if (!linkPanelCompound.hasKey("properties")) {
                 return;
             }
             NBTTagList list = linkPanelCompound.getTagList("properties");
 
             ArrayList recipes = Integrator.getALlInkMixerRecipes();
 
             for (Object recipeObj : recipes) {
                 CachedInkMixerRecipe recipe = new CachedInkMixerRecipe(recipeObj);
 
                 if (recipe.modifiers == null) {
                     continue;
                 }
                 if (list.tagCount() == 0) {
                     recipe:
                     {
                         for (String modifier : recipe.modifiers) {
                             if (!modifier.isEmpty()) {
                                 break recipe;
                             }
                         }
                         this.arecipes.add(recipe);
                     }
                     continue;
                 }
 
                 for (int i = 0; i < list.tagCount(); i++) {
                     for (String modifier : recipe.modifiers) {
                         if (!modifier.isEmpty() && ((NBTTagString) list.tagAt(i)).data.equals(modifier) && !this.arecipes.contains(recipe)) {
                             this.arecipes.add(recipe);
                             break;
                         }
                     }
                 }
             }
         }
         else if (result.getItem() == Item.glassBottle) {
             ArrayList recipes = Integrator.getALlInkMixerRecipes();
 
             for (Object ingr : recipes) {
                 CachedInkMixerRecipe recipe = new CachedInkMixerRecipe(ingr);
                 if (recipe.modifiers != null) {
                     this.arecipes.add(recipe);
                 }
             }
         }
     }
 
     @Override
     public void loadUsageRecipes(ItemStack ingredient) {
         if (MystObjects.page == null) {
             return;
         }
 
         if (ingredient.getItem() == Item.paper || ingredient.getItem() == MystObjects.inkvial) {
             ArrayList recipes = Integrator.getALlInkMixerRecipes();
 
             for (Object ingr : recipes) {
                 CachedInkMixerRecipe recipe = new CachedInkMixerRecipe(ingr);
                 if (recipe.modifiers != null) {
                     this.arecipes.add(recipe);
                 }
             }
         }
         else {
             ItemStack ingr = ingredient.copy();
             ingr.stackSize = 1;
             CachedInkMixerRecipe recipe = new CachedInkMixerRecipe(ingr);
             if (recipe.modifiers != null) {
                 this.arecipes.add(recipe);
             }
         }
     }
 
     @Override
     public int recipiesPerPage() {
         return 1;
     }
 
     @Override
     public void drawBackground(int recipe) {
         this.renderTank(49, 5, 66, 65, (CachedInkMixerRecipe) this.arecipes.get(recipe));
 
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         GuiDraw.changeTexture(this.getGuiTexture());
         GuiDraw.drawTexturedModalRect(0, 0, 5, 11, 166, 76);
     }
 
    @Override
    public void drawExtras(int recipeId) {
        CachedInkMixerRecipe recipe = (CachedInkMixerRecipe) this.arecipes.get(recipeId);

        if (recipe != null && recipe.gradient != null) {
            Color color = recipe.gradient.getColor(recipe.frame);
            MystAPI.render.drawColor(82.5F, 37.5F, 0.0F, 20.0F, color);
        }
    }

     private void renderTank(int left, int top, int width, int height, CachedInkMixerRecipe recipe) {
         GuiHelper.drawFluid(MystObjects.black_ink, left, top, width, height);
 
        if (recipe != null && recipe.gradient != null) {
             recipe.frame++;
            if (recipe.frame > recipe.gradient.getLength()) {
                recipe.frame = 0;
            }
            Color color = recipe.gradient.getColor(recipe.frame);
             int iColor = color.asInt();
             GuiDraw.drawGradientRect(left, top, left + width, top + height, 0x40000000 + iColor, 0xB0000000 + iColor);
         }
         else {
             GuiDraw.drawGradientRect(left, top, left + width, top + height, Objects.rnd.nextInt(0xFFFFFF) & 0xFFFFFF | 0xFF000000, Objects.rnd.nextInt(0xFFFFFF) & 0xFFFFFF | 0xFF000000);
         }
     }
 
     @Override
     public Class<? extends GuiContainer> getGuiClass() {
         return NEIConfig.guiInkMixerClass;
     }
 
     @Override
     public String getOverlayIdentifier() {
         return "inkmixer";
     }
 
     @Override
     public IRecipeOverlayRenderer getOverlayRenderer(GuiContainer gui, int recipe) {
         IStackPositioner positioner = RecipeInfo.getStackPositioner(gui, this.getOverlayIdentifier());
         if (positioner == null) {
             return null;
         }
         return new InkMixerOverlayRenderer(this.getIngredientStacks(recipe), positioner, this.arecipes.get(recipe).getIngredient());
     }
 
     @Override
     public List<String> handleItemTooltip(GuiRecipe gui, ItemStack stack, List<String> currenttip, int recipeId) {
         currenttip = super.handleItemTooltip(gui, stack, currenttip, recipeId);
 
         Point mousepos = GuiDraw.getMousePosition();
         Point relMouse = new Point(mousepos.x - gui.guiLeft, mousepos.y - gui.guiTop);
 
         Point center = new Point(87, 54);
 
         if (currenttip.isEmpty() && stack == null && center.distance(relMouse) < 34.0D) {
             currenttip.add(StatCollector.translateToLocal("nei.mystcraft.inkmixer.drop"));
         }
 
         return currenttip;
     }
 
 }
