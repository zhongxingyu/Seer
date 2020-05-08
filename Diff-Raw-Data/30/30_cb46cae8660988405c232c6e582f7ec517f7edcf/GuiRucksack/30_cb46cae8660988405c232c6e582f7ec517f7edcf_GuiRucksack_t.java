 package burptech.client.gui;
 
 import org.lwjgl.opengl.GL11;
 
 import burptech.gui.ContainerRucksack;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.client.gui.inventory.GuiContainer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.StatCollector;
 import net.minecraft.util.ResourceLocation;
 
 public class GuiRucksack extends GuiContainer
 {
 	protected static final ResourceLocation resourceLocation = new ResourceLocation("textures/gui/container/generic_54.png");
 
 	/**
      * window height is calculated with these values, the more rows, the higher
      */
     private int inventoryRows = 0;
     private IInventory container;
     private ItemStack itemContainer;
     
     public GuiRucksack(IInventory playerInventory, IInventory containerInventory)
     {
         super(new ContainerRucksack(playerInventory, containerInventory));
         
         itemContainer = ((InventoryPlayer)playerInventory).mainInventory[((InventoryPlayer)playerInventory).currentItem];
         
         this.container = containerInventory;
         this.allowUserInput = false;
         short var3 = 222;
         int var4 = var3 - 108;
         this.inventoryRows = containerInventory.getSizeInventory() / 9;
         this.ySize = var4 + this.inventoryRows * 18;
     }
 
     /**
      * Draw the foreground layer for the GuiContainer (everything in front of the items)
      */
     @Override
     protected void drawGuiContainerForegroundLayer(int par1, int par2)
     {
     	String name = null;
     	
     	if (itemContainer != null)
     		name = itemContainer.getDisplayName();
     	
     	if (name == null || name == "")
     		name = LanguageRegistry.instance().getStringLocalization(container.getInvName()+".name");
     	
     	if (name == null || name == "")
     		name = StatCollector.translateToLocal(container.getInvName());
     	
         this.fontRenderer.drawString(name == null || name == "" ? "Container" : name, 8, 6, 4210752);
         this.fontRenderer.drawString(StatCollector.translateToLocal("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
     }
 
     /**
      * Draw the background layer for the GuiContainer (everything behind the items)
      */
     @Override
     protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
     {
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(resourceLocation);
         int var5 = (this.width - this.xSize) / 2;
         int var6 = (this.height - this.ySize) / 2;
         this.drawTexturedModalRect(var5, var6, 0, 0, this.xSize, this.inventoryRows * 18 + 17);
         this.drawTexturedModalRect(var5, var6 + this.inventoryRows * 18 + 17, 0, 126, this.xSize, 96);
     }
 }
