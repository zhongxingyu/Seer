 package ip.industrialProcessing.items.guide.gui.machines;
 
 import java.awt.Color;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.lwjgl.opengl.GL11;
 
 import codechicken.nei.recipe.FurnaceRecipeHandler.SmeltingPair;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import net.minecraft.block.Block;
 import net.minecraft.client.gui.FontRenderer;
 import net.minecraft.client.renderer.ItemRenderer;
 import net.minecraft.client.renderer.RenderHelper;
 import net.minecraft.client.renderer.entity.RenderItem;
 import net.minecraft.client.renderer.texture.TextureMap;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.CraftingManager;
 import net.minecraft.item.crafting.FurnaceRecipes;
 import net.minecraft.item.crafting.IRecipe;
 import net.minecraft.util.ResourceLocation;
 import ip.industrialProcessing.IndustrialProcessing;
 import ip.industrialProcessing.client.render.gui.GuiTools;
 import ip.industrialProcessing.client.render.gui.ToolTip;
 import ip.industrialProcessing.config.INamepace;
 import ip.industrialProcessing.gui.GuiLayout;
 import ip.industrialProcessing.gui.IGuiLayout;
 import ip.industrialProcessing.gui.IGuiLayoutMultiblock;
 import ip.industrialProcessing.items.guide.gui.GuiGuide;
 import ip.industrialProcessing.items.guide.gui.GuidePanoramaPage;
 import ip.industrialProcessing.items.guide.gui.machines.components.GuideMachineCraftingRecipeDetails;
 import ip.industrialProcessing.items.guide.gui.machines.components.GuideMachineFurnaceRecipeDetails;
 import ip.industrialProcessing.items.guide.gui.machines.components.GuideMachinePageMode;
 import ip.industrialProcessing.items.guide.gui.machines.old.GuideMachineDetailsFrame;
 import ip.industrialProcessing.machines.plants.blacksmith.anvil.ContainerAnvil;
 import ip.industrialProcessing.multiblock.recipes.RecipeMultiblock;
 import ip.industrialProcessing.recipes.Recipe;
 import ip.industrialProcessing.utils.IDescriptionBlock;
 import ip.industrialProcessing.utils.registry.BlockRegistry;
 import ip.industrialProcessing.utils.registry.BlockType;
 import ip.industrialProcessing.utils.registry.RecipeRegistry;
 
 public class GuideMachinePage extends GuidePanoramaPage {
 
 	private BlockType[][] type = new BlockType[][] { { BlockType.Ore_Processing, BlockType.Smelting, BlockType.Power,BlockType.fluid, BlockType.Refinary,BlockType.assemble }, { BlockType.Power, BlockType.Storage } };
 	private BlockType activeType;
 	private BlockType hoverType;
 	private GuideMachinePageMode mode;
 	private Block hoverBlock;
 	private Block activeBlock;
 	public ItemStack hoverToolTipStack;
 	public ItemStack activeToolTipStack;
 	private Block hoverTab;
 	private int hoverCraftingPane = -1;
 	private Block activeTab;
 	private int craftingPane = -1;
 
 	public ResourceLocation textureLocationLayout = new ResourceLocation(IndustrialProcessing.TEXTURE_DOMAIN, "textures/gui/layout.png");
 	public ResourceLocation textureLocationWorker;
 	protected static RenderItem itemRenderer = new RenderItem();
 
 	public void setTextureWorker(Block block) {
 		this.textureLocationWorker = new ResourceLocation(IndustrialProcessing.TEXTURE_DOMAIN, "textures/gui/"+block.getLocalizedName()+".png");
 		mc.renderEngine.func_110577_a(this.textureLocationWorker);
 	}
 
 	public void setTextureLayout() {
 		mc.renderEngine.func_110577_a(this.textureLocationLayout);
 	}
 
 	public void setTextureGuide() {
 		mc.renderEngine.func_110577_a(this.textureLocation);
 	}
 
 	public void setTextureBlocks() {
 		mc.renderEngine.func_110577_a(TextureMap.field_110575_b);
 	}
 
 	// private FontRenderer smallFont = new FontRenderer(mc.gameSettings, new
 	// ResourceLocation("textures/font/ascii.png"), mc.renderEngine, false);
 
 	public GuideMachinePage() {
 		super(new Rectangle(0, 0, 110, 178), new Point(5, 18));
 		this.allowHorizontalDrag = false;
 		this.allowVerticalDrag = true;
 		this.mode = GuideMachinePageMode.procces;
 		this.textureLocation = new ResourceLocation(INamepace.TEXTURE_DOMAIN, "textures/gui/GuideDropShadowOverlayHalf.png");
 	}
 
 	@Override
 	public void drawScreen(int mouseX, int mouseY, int x, int y) {
 		super.drawScreen(mouseX, mouseY, x, y);
 		hoverToolTipStack = null;
 		hoverTab = null;
 		hoverCraftingPane = -1;
 
 		if (this.activeBlock != null) {
 			RenderHelper.disableStandardItemLighting();
 			GL11.glPushMatrix();
 			x += 120;
 			y += 24;
 			// header
 			drawMachine(mouseX, mouseY, activeBlock, x, y, true, false);
 			fontRenderer.drawString(activeBlock.getLocalizedName(), x + 25, y + 5, 4210752);
 
 			// description
 			if (activeBlock instanceof IDescriptionBlock)
 				fontRenderer.drawSplitString(((IDescriptionBlock) activeBlock).getDescription(), x, y + 20, 136, 4210752);
 		}
 		if (this.activeToolTipStack != null) {
 			RenderHelper.disableStandardItemLighting();
 			GL11.glPushMatrix();
 			x += 120;
 			y += 24;
 			// header
 			drawItemstack(activeToolTipStack, x, y);
 			fontRenderer.drawString(activeToolTipStack.getDisplayName(), x + 25, y + 5, 4210752);
 		}
 		if (this.activeToolTipStack != null || this.activeBlock != null) {
 			fontRenderer.drawString("Crafting", x, y + 50, 4210752);
 			// get the recipes for the machine
 
 			ArrayList<Recipe> recipes;
 			int id;
 			if (this.activeBlock != null) {
 				recipes = RecipeRegistry.FindRecipeForOutput(new ItemStack(activeBlock));
 				id = activeBlock.blockID;
 			} else {
 				recipes = RecipeRegistry.FindRecipeForOutput(activeToolTipStack);
 				id = activeToolTipStack.itemID;
 			}
 			int i = 0;
 			for (i = 0; i < recipes.size(); i++) {
 				Recipe recipe = recipes.get(i);
 				Block craftingBlock = RecipeRegistry.getBlockForRecipe(recipe);
 
 				if (i == 0 && craftingPane == -1)
 					craftingPane = 0;
 				drawMachineTab(mouseX, mouseY, craftingBlock, x + i * 18, y + 60, i, craftingPane);
 				// draw the active recipe
 				if (craftingPane == i && craftingBlock instanceof IGuiLayout) {
 					GuiLayout layout = ((IGuiLayout) craftingBlock).getGuiLayout();
 					setTextureLayout();
 					layout.drawFilledPanels(new Rectangle(x, y + 80, 130, 90), this, mouseX, mouseY, recipe,craftingBlock);
 					setTextureGuide();
 				}
 				if (craftingPane == i && craftingBlock instanceof IGuiLayoutMultiblock && recipe instanceof RecipeMultiblock) {
 					GuiLayout layout = ((IGuiLayoutMultiblock) craftingBlock).getGuiLayout(((RecipeMultiblock)recipe).tier);
 					setTextureLayout();
 					layout.drawFilledPanels(new Rectangle(x, y + 80, 130, 90), this, mouseX, mouseY, recipe,craftingBlock);
 					setTextureGuide();
 				}
 			}
 			// crafting managers
 			List list = CraftingManager.getInstance().getRecipeList();
 			for (int j = 0; j < list.size(); j++) {
 				Object listItem = list.get(j);
 				if (listItem instanceof IRecipe) {
 					IRecipe recipe = (IRecipe) listItem;
 					ItemStack output = recipe.getRecipeOutput();
 					if (output != null && output.itemID == id) {
 						if (i == 0 && craftingPane == -1)
 							craftingPane = 0;
 						drawMachineTab(mouseX, mouseY, Block.workbench, x + i * 18, y + 60, i, craftingPane);
 						// draw the active recipe
 						if (craftingPane == i) {
 							GuideMachineCraftingRecipeDetails details = new GuideMachineCraftingRecipeDetails(recipe);
							details.renderTabContents(this, x, y + 80, mouseX, mouseY);
 						}
 						i++;
 					}
 				}
 			}
 			list = ContainerAnvil.recipes.getInstance().getRecipeList();
 			for (int j = 0; j < list.size(); j++) {
 				Object listItem = list.get(j);
 				if (listItem instanceof IRecipe) {
 					IRecipe recipe = (IRecipe) listItem;
 					ItemStack output = recipe.getRecipeOutput();
 					if (output != null && output.itemID == id) {
 						if (i == 0 && craftingPane == -1)
 							craftingPane = 0;
 						drawMachineTab(mouseX, mouseY, IndustrialProcessing.blockAnvil, x + i * 18, y + 60, i, craftingPane);
 						// draw the active recipe
 						if (craftingPane == i) {
 							GuideMachineCraftingRecipeDetails details = new GuideMachineCraftingRecipeDetails(recipe);
							details.renderTabContents(this, x, y + 80, mouseX, mouseY);
 						}
 						i++;
 					}
 				}
 			}
 			//smelting managers
 			Map map = FurnaceRecipes.smelting().getSmeltingList();
 			Iterator it = map.entrySet().iterator();
 			while(it.hasNext()){
 				Map.Entry<Integer, ItemStack> entry = (Entry<Integer, ItemStack>) it.next();
 				if(entry.getValue() != null && entry.getValue().itemID == id){
 					if (i == 0 && craftingPane == -1)
 						craftingPane = 0;
 					drawMachineTab(mouseX, mouseY, Block.furnaceBurning, x + i * 18, y + 60, i, craftingPane);
 					// draw the active recipe
 					if (craftingPane == i) {
 						GuideMachineFurnaceRecipeDetails details = new GuideMachineFurnaceRecipeDetails(entry.getKey(),entry.getValue());
						details.renderTabContents(this, x, y + 80, mouseX, mouseY);
 					}
 					i++;
 				}
 			}
 
 			setTextureGuide();
 
 			GL11.glPopMatrix();
 			RenderHelper.enableStandardItemLighting();
 		}
 
 		if (this.hoverBlock != null) {
 			ToolTip tip = new ToolTip(this.hoverBlock.getLocalizedName());
 			ToolTip.renderToolTip(tip, mouseX + 16, mouseY, 10, mc.fontRenderer);
 		}
 
 		if (this.hoverTab != null) {
 			ToolTip tip = new ToolTip(this.hoverTab.getLocalizedName());
 			ToolTip.renderToolTip(tip, mouseX + 16, mouseY, 10, mc.fontRenderer);
 		}
 
 		if (this.hoverToolTipStack != null) {
 			ToolTip toolTip = new ToolTip("\u00a77" + hoverToolTipStack.getDisplayName() + "  " + hoverToolTipStack.itemID);
 			ToolTip.renderToolTip(toolTip, mouseX + 16, mouseY, 10, mc.fontRenderer);
 		}
 	}
 
 	@Override
 	protected void drawPane(int mouseX, int mouseY, boolean mouseInside) {
 		int marginX = 8;
 		int marginY = 8;
 		int x = 0 + 4;
 		int y = 0 + 8;
 		this.hoverBlock = null;
 		this.hoverType = null;
 
 		// draw tabs and machines
 
 		for (int i = 0; i < type[mode.ordinal()].length; i++) {
 			RenderHelper.disableStandardItemLighting();
 			GL11.glPushMatrix();
 			GL11.glTranslatef(0, 0, 1);
 			drawRect(x, y, x + 90, y + 16, FILL_COLOR);
 			GL11.glTranslatef(1, 1, -1);
 			drawRect(x, y, x + 90, y + 16, SHADOW_COLOR);
 			GL11.glPopMatrix();
 
 			fontRenderer.drawString(LanguageRegistry.instance().getStringLocalization("IP.BlockType." + type[mode.ordinal()][i].toString(), "en_US"), x + 5, y + 5, 16777215);
 
 			if (x < mouseX && y < mouseY && x + 90 > mouseX && y + 16 > mouseY) {
 				hoverType = type[mode.ordinal()][i];
 			}
 
 			y = y + 16 + marginY;
 
 			if (activeType == type[mode.ordinal()][i]) {
 				y = y + marginY + drawMachines(activeType, x, y, mouseX, mouseY);
 			}
 		}
 	}
 
 	private int drawMachines(BlockType type, int x, int y, int mouseX, int mouseY) {
 		Block[] blocks = BlockRegistry.getBlocksByTagArray(type);
 		Arrays.sort(blocks, new Comparator<Block>() {
 			public int compare(Block o1, Block o2) {
 				return o1.getLocalizedName().compareTo(o2.getLocalizedName());
 			}
 		});
 		int dx = 0;
 		int dy = 0;
 		int marginX = 5;
 		int marginY = 5;
 		for (int i = 0; i < blocks.length; i++) {
 			Block block = blocks[i];
 			drawMachine(mouseX, mouseY, block, x + dx, y + dy, true, true);
 			if (dx < 58) {
 				dx = dx + 16 + marginX;
 			} else {
 				dx = 0;
 				dy = dy + 16 + marginY;
 			}
 		}
 		dy = dy + 16 + marginY;
 		return dy;
 	}
 
 	private void drawMachine(int mouseX, int mouseY, Block block, int x, int y, boolean allowHover, boolean drawBackground) {
 		ItemStack stack = new ItemStack(block);
 		GL11.glEnable(GL11.GL_LIGHTING);
 		GL11.glEnable(GL11.GL_DEPTH_TEST);
 
 		if (drawBackground) {
 			RenderHelper.disableStandardItemLighting();
 			GL11.glPushMatrix();
 			GL11.glTranslatef(0, 0, 1);
 			drawRect(x, y, x + 16, y + 16, FILL_COLOR);
 			GL11.glTranslatef(1, 1, -1);
 			drawRect(x, y, x + 16, y + 16, SHADOW_COLOR);
 			GL11.glPopMatrix();
 		}
 		RenderHelper.enableGUIStandardItemLighting();
 		GuiTools.drawItemStack(stack, x, y, null, GuiGuide.itemRenderer, mc.fontRenderer, mc.renderEngine);
 
 		Rectangle rect = new Rectangle(x, y, 16, 16);
 		if (rect.contains(mouseX, mouseY) && allowHover) {
 			this.hoverBlock = block;
 		}
 		GL11.glDisable(GL11.GL_DEPTH_TEST);
 		GL11.glDisable(GL11.GL_LIGHTING);
 		RenderHelper.disableStandardItemLighting();
 	}
 
 	private void drawMachineTab(int mouseX, int mouseY, Block block, int x, int y, int index, int craftingPane) {
 		ItemStack stack = new ItemStack(block);
 		GL11.glEnable(GL11.GL_LIGHTING);
 		GL11.glEnable(GL11.GL_DEPTH_TEST);
 
 		RenderHelper.disableStandardItemLighting();
 		GL11.glPushMatrix();
 		GL11.glTranslatef(0, 0, 1);
 		if (index == craftingPane)
 			drawRect(x, y, x + 16, y + 16, FILL_COLOR);
 		GL11.glTranslatef(-1, -1, -1);
 		drawRect(x, y, x + 18, y + 18, SHADOW_COLOR);
 		GL11.glPopMatrix();
 
 		RenderHelper.enableGUIStandardItemLighting();
 		GuiTools.drawItemStack(stack, x, y, null, GuiGuide.itemRenderer, mc.fontRenderer, mc.renderEngine);
 
 		Rectangle rect = new Rectangle(x, y, 16, 16);
 		if (rect.contains(mouseX, mouseY)) {
 			this.hoverTab = block;
 			this.hoverCraftingPane = index;
 		}
 		GL11.glDisable(GL11.GL_DEPTH_TEST);
 		GL11.glDisable(GL11.GL_LIGHTING);
 		RenderHelper.disableStandardItemLighting();
 	}
 
 	public void drawItemstack(ItemStack stack, int x, int y) {
 		RenderHelper.enableGUIStandardItemLighting();
 		GL11.glEnable(GL11.GL_LIGHTING);
 		GL11.glEnable(GL11.GL_DEPTH_TEST);
 		if(stack.stackSize == 1)
 			drawItemStack(stack, x, y, "");
 		else
 			drawItemStack(stack, x, y, stack.stackSize + "");
 		GL11.glDisable(GL11.GL_DEPTH_TEST);
 		GL11.glDisable(GL11.GL_LIGHTING);
 		RenderHelper.disableStandardItemLighting();
 	}
 	
 
     public void drawItemStack(ItemStack par1ItemStack, int par2, int par3, String par4Str)
     {
         GL11.glTranslatef(0.0F, 0.0F, 32.0F);
         this.zLevel = 200.0F;
 		itemRenderer .zLevel = 200.0F;
         FontRenderer font = null;
         if (par1ItemStack != null) font = par1ItemStack.getItem().getFontRenderer(par1ItemStack);
         if (font == null) font = fontRenderer;
         itemRenderer.renderItemAndEffectIntoGUI(font, this.mc.func_110434_K(), par1ItemStack, par2, par3);
         itemRenderer.renderItemOverlayIntoGUI(font, this.mc.func_110434_K(), par1ItemStack, par2, par3, par4Str);
         this.zLevel = 0.0F;
         itemRenderer.zLevel = 0.0F;
     }
 
 	@Override
 	public Point getIconLocation() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public String getTitle() {
 		return "Industrial machines";
 	}
 
 	@Override
 	public ItemStack getIconStack() {
 		return new ItemStack(IndustrialProcessing.blockCrusher);
 	}
 
 	@Override
 	public void mouseClicked(int d, int e, int par3) {
 		super.mouseClicked(d, e, par3);
 
 		if (this.hoverType != null) {
 			this.activeType = hoverType;
 		}
 
 		if (this.hoverBlock != null) {
 			this.craftingPane = -1;
 			this.activeToolTipStack = null;
 			this.activeBlock = hoverBlock;
 		}
 
 		if (this.hoverTab != null) {
 			this.craftingPane = hoverCraftingPane;
 		}
 
 		if (this.hoverToolTipStack != null) {
 			if (hoverToolTipStack.itemID < 4096 && Block.blocksList[hoverToolTipStack.itemID] != null) {
 				this.craftingPane = -1;
 				this.activeToolTipStack = null;
 				this.activeBlock = Block.blocksList[hoverToolTipStack.itemID];
 				if (activeBlock.blockID == 0) {
 					this.craftingPane = -1;
 					this.activeBlock = null;
 					this.activeToolTipStack = hoverToolTipStack;
 				}
 			} else {
 				this.craftingPane = -1;
 				this.activeBlock = null;
 				this.activeToolTipStack = hoverToolTipStack;
 			}
 		}
 	}
 
 }
