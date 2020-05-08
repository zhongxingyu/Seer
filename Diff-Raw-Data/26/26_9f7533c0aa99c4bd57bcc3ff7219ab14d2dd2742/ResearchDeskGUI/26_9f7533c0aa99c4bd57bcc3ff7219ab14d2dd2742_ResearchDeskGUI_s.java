 package mod.industrialscience.modules.research;
 
 import org.lwjgl.opengl.GL11;
 
 import net.minecraft.client.gui.inventory.GuiContainer;
 import net.minecraft.entity.player.InventoryPlayer;
 
 public class ResearchDeskGUI extends GuiContainer {
     public ResearchDeskGUI(InventoryPlayer player_inventory, ResearchDeskTile tile_entity){
         super(new ResearchDeskContainer(tile_entity, player_inventory));
 }
 
     @Override
     protected void drawGuiContainerForegroundLayer(int i, int j){
    
     }
    
    
     @Override
     protected void drawGuiContainerBackgroundLayer(float f, int i, int j){
            
     	GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.mc.renderEngine.func_98187_b("/mods/industrialscience/textures/gui/GUIResearchDesk.png");
         int x = (width - xSize) / 2;
        
         int y = (height - ySize) / 2;
        
         this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
     }
 }
