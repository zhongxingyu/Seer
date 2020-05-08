 
 package me.heldplayer.plugins.nei.mystcraft.client.renderer;
 
 import java.util.ArrayList;
import java.util.List;
 
 import net.minecraft.inventory.Slot;
 
 import org.lwjgl.opengl.GL11;
 
 import codechicken.nei.PositionedStack;
 import codechicken.nei.api.IRecipeOverlayRenderer;
 import codechicken.nei.api.IStackPositioner;
 import codechicken.nei.forge.GuiContainerManager;
 
 /**
  * Overlay renderer for ink mixer recipes, renders when the "?" button next to a
  * recipe is pressed
  * 
  * @author heldplayer
  * 
  */
 public class InkMixerOverlayRenderer implements IRecipeOverlayRenderer {
 
     private IStackPositioner positioner;
     private ArrayList<PositionedStack> ingreds;
     private PositionedStack ingredient;
 
    public InkMixerOverlayRenderer(List<PositionedStack> stacks, IStackPositioner positioner, PositionedStack ingredient) {
         this.positioner = positioner;
         this.ingreds = new ArrayList<PositionedStack>();
         for (PositionedStack stack : stacks) {
             this.ingreds.add(stack.copy());
         }
         this.ingreds = this.positioner.positionStacks(this.ingreds);
 
         ArrayList<PositionedStack> temp = new ArrayList<PositionedStack>();
         temp.add(ingredient);
         temp = this.positioner.positionStacks(temp);
         ingredient = temp.get(0);
         this.ingredient = ingredient;
     }
 
     @Override
     public void renderOverlay(GuiContainerManager gui, Slot slot) {
         GL11.glEnable(GL11.GL_BLEND);
         GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
 
         GuiContainerManager.setColouredItemRender(true);
 
         for (PositionedStack stack : this.ingreds) {
             if (stack.relx == slot.xDisplayPosition && stack.rely == slot.yDisplayPosition) {
                GL11.glColor4d(0.6F, 0.6F, 0.6F, 0.7F);
                 GuiContainerManager.drawItem(stack.relx, stack.rely, stack.item);
             }
         }

        if (slot.slotNumber == 0) {
            GL11.glColor4d(0.6F, 0.6F, 0.6F, 0.7F);
            GuiContainerManager.drawItem(this.ingredient.relx, this.ingredient.rely, this.ingredient.item);
        }

         GuiContainerManager.setColouredItemRender(false);
 
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         GL11.glDisable(GL11.GL_BLEND);
         GL11.glEnable(GL11.GL_LIGHTING);
     }
 
 }
