 package shadow.mods.metallurgy.precious;
 
 import org.lwjgl.opengl.GL11;
 import net.minecraft.src.GuiContainer;
 import net.minecraft.src.IInventory;
 import net.minecraft.src.StatCollector;
 
 
 public class FC_GuiChest extends GuiContainer
 {
     private IInventory upperChestInventory;
     private IInventory lowerChestInventory;
 
     /**
      * window height is calculated with this values, the more rows, the heigher
      */
     private int inventoryRows = 0;
     private int inventoryCols = 0;
     private String image;
 
     public FC_GuiChest(IInventory playerInv, IInventory chestInv)
     {
         super(new FC_ContainerChest(playerInv, chestInv));
         this.upperChestInventory = playerInv;
         this.lowerChestInventory = chestInv;
         this.allowUserInput = false;
         
         short var3 = 222;
         int var4 = var3 - 108;
         //this.inventoryRows = chestInv.getSizeInventory() / 9;
         this.inventoryRows = ((FC_TileEntityChest)chestInv).getNumRows();
         this.inventoryCols = ((FC_TileEntityChest)chestInv).getNumCols();
         this.ySize = var4 + this.inventoryRows * 18;
         
         int type = ((FC_TileEntityChest)chestInv).getType();
         
         image = "/shadow/ironcontainer.png";
         switch(type)
         {
 	        case 0:
 	        {
 	        	image = "/shadow/ironcontainer.png";
 	        	break;
 	        }	
 	        case 1:
 	        {
 	        	image = "/shadow/silvercontainer.png";
 	        	break;
 	        }	
 	        case 2:
 	        {
 	        	image = "/shadow/goldcontainer.png";
 	        	break;
 	        }	
 	        case 3:
 	        {
 	        	image = "/shadow/electrumcontainer.png";
 	        	break;
 	        }	
 	        case 4:
 	        {
 	        	image = "/shadow/diamondcontainer.png";
 	        	break;
 	        }	
         }
         		
     }
 
     /**
      * Draw the foreground layer for the GuiContainer (everythin in front of the items)
      */
     protected void drawGuiContainerForegroundLayer()
     {
         //this.fontRenderer.drawString(StatCollector.translateToLocal(this.lowerChestInventory.getInvName()), 8, 6, 4210752);
         //this.fontRenderer.drawString(StatCollector.translateToLocal(this.upperChestInventory.getInvName()), 8, this.ySize - 96 + 2, 4210752);
     }
 
     /**
      * Draw the background layer for the GuiContainer (everything behind the items)
      */
     protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
     {
         int var4 = this.mc.renderEngine.getTexture(image);
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         this.mc.renderEngine.bindTexture(var4);
         int imageWidth = (11 + 18 *inventoryCols + 11);
         int imageHeight = (7 + 18 *inventoryRows + 4 + 18 * 3 + 4 + 18 + 7);
         
         int var5 = (this.width / 2) - (imageWidth/2);
         int var6 = (this.height / 2) - (imageHeight / 2);
         
         this.drawTexturedModalRect(var5, var6, 0, 0, imageWidth, imageHeight);
         //this.drawTexturedModalRect(var5, var6 + this.inventoryRows * 18 + 17, 0, 126, this.xSize, 96);
     }
 }
