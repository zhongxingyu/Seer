 package com.isocraft.client.gui;
 
 import java.util.List;
 
 import net.minecraft.client.gui.GuiButton;
 import net.minecraft.client.gui.inventory.GuiContainer;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.util.StatCollector;
 import net.minecraft.world.World;
 
 import org.lwjgl.opengl.GL11;
 
 import com.isocraft.ISOCraft;
 import com.isocraft.api.DiscoveryClient;
 import com.isocraft.inventory.ContainerPDA;
 import com.isocraft.lib.Reference;
 import com.isocraft.lib.Textures;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 /**
  * ISOCraft
  * 
  * Gui for PDA
  * 
  * @author Turnermator13
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  * 
  */
 
 @SideOnly(Side.CLIENT)
 public class GuiPDA extends GuiContainer {
 
     public ItemStack pda = null;
     public int currentSlot;
     public int skip = 0;
     private boolean change = false;
     private List<String> List;
     private boolean packet = false;
     private boolean topButtonEnabled = true;
     private boolean bottomButtonEnabled = true;
 
     public GuiPDA(EntityPlayer player, World world, int x, int y, int z) {
         super(new ContainerPDA(player.inventory, world, x, y, z));
         pda = player.inventory.getCurrentItem();
         currentSlot = player.inventory.currentItem;
         change = true;
         nbtSetup();
         xSize = 217;
         ySize = 238;
     }
 
     @Override
     protected void actionPerformed(GuiButton par1GuiButton) {
         if (par1GuiButton.enabled) {
             NBTTagCompound tag = pda.stackTagCompound.getCompoundTag(Reference.MOD_ID);
             if (par1GuiButton.id == 0) {
                 tag.setInteger(Reference.TAB, 1);
                 ISOCraft.proxy.sendItemNBTPacket(currentSlot, Reference.TAB, 1);
                 change = true;
             }
             if (par1GuiButton.id == 1) {
                 tag.setInteger(Reference.TAB, 2);
                 ISOCraft.proxy.sendItemNBTPacket(currentSlot, Reference.TAB, 2);
                 change = true;
             }
             if (par1GuiButton.id == 2) {
                 tag.setInteger(Reference.TAB, 3);
                 ISOCraft.proxy.sendItemNBTPacket(currentSlot, Reference.TAB, 3);
                 change = true;
             }
             if (par1GuiButton.id == 3) {
                 skip -= 1;
                 change = true;
             }
             if (par1GuiButton.id == 4) {
                 skip += 1;
                 change = true;
             }
         }
     }
 
     @SuppressWarnings("unchecked")
     private void ButtonManager(int xStart, int yStart, List<String> list) {
         int y = yStart;
         int j = 5;
 
         if (list.size() < 6) {
             for (int i = 0; i < list.size(); ++i) {
                 buttonList.add(new GuiPDADiscoveryButton(j, xStart, y, List.get(i)));
                 y = y + 37;
                 j++;
             }
         }
         else if (list.size() > 5) {
             for (int i = skip; i < skip + 5; ++i) {
                 buttonList.add(new GuiPDADiscoveryButton(j, xStart, y, List.get(i)));
                 y = y + 37;
                 ++j;
             }
         }
     }
 
     @Override
     protected void drawGuiContainerBackgroundLayer(float opacity, int x, int y) {
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         mc.renderEngine.bindTexture(Textures.GUI_PDA);
         int xStart = (width - xSize) / 2;
         int yStart = (height - ySize) / 2;
         this.drawTexturedModalRect(xStart, yStart, 0, 0, xSize, ySize);
 
         int tab = pda.getTagCompound().getCompoundTag(Reference.MOD_ID).getInteger(Reference.TAB);
         if (tab == 1) {
             tab1Back(xStart, yStart);
         }
         if (tab == 2) {
             tab2Back(xStart, yStart);
         }
         if (tab == 3) {
             tab3Back(xStart, yStart);
         }
     }
 
     @SuppressWarnings("unchecked")
     @Override
     protected void drawGuiContainerForegroundLayer(int x, int y) {
         int xStart = (width - xSize) / 2;
         int yStart = (height - ySize) / 2;
 
         if (change) {
             int tab = pda.getTagCompound().getCompoundTag(Reference.MOD_ID).getInteger(Reference.TAB);
 
             if (!packet) {
                 if (tab == 1) {
                     fontRenderer.drawString(StatCollector.translateToLocal("Updating..."), xStart + 28, yStart + 88 + 2, 4210752);
                 }
                 if (DiscoveryClient.Update) {
                     packet = true;
                     DiscoveryClient.Update = false;
                 }
             }
             else {
                 change = false;
                List = DiscoveryClient.playerDiscoveredList;
                topButtonEnabled = true;
                bottomButtonEnabled = true;
                if (List.size() < 6) {
                    topButtonEnabled = false;
                    bottomButtonEnabled = false;
                }
                else if (skip == 0) {
                    topButtonEnabled = false;
                }
                else if (skip == List.size() - 5) {
                    bottomButtonEnabled = false;
                }
                 initGui();
                 if (tab == 1) {
                     ButtonManager(xStart + 10, yStart + 42, List);
                    buttonList.add(new GuiPDAScrollButton(3, xStart + 160, yStart + 40, true, topButtonEnabled));
                    buttonList.add(new GuiPDAScrollButton(4, xStart + 160, yStart + 70, false, bottomButtonEnabled));
                 }
 
             }
         }
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public void initGui() {
         buttonList.clear();
         int xStart = (width - xSize) / 2;
         int yStart = (height - ySize) / 2;
         buttonList.add(new GuiPDATabButton(0, xStart + 39, yStart + 7));
         buttonList.add(new GuiPDATabButton(1, xStart + 86, yStart + 7));
         buttonList.add(new GuiPDATabButton(2, xStart + 133, yStart + 7));
     }
 
     private void nbtSetup() {
         if (pda.stackTagCompound == null) {
             pda.stackTagCompound = new NBTTagCompound();
         }
         if (!pda.stackTagCompound.hasKey(Reference.MOD_ID)) {
             pda.stackTagCompound.setCompoundTag(Reference.MOD_ID, new NBTTagCompound());
         }
         NBTTagCompound tag = pda.stackTagCompound.getCompoundTag(Reference.MOD_ID);
 
         if (!tag.hasKey(Reference.TAB)) {
             tag.setInteger(Reference.TAB, 1);
             ISOCraft.proxy.sendItemNBTPacket(currentSlot, Reference.TAB, 1);
         }
         
         if(tag.hasKey(Reference.SKIP)){
             skip = pda.getTagCompound().getCompoundTag(Reference.MOD_ID).getInteger(Reference.SKIP);
         }
         else{
             tag.setInteger(Reference.SKIP, 0);
         }
     }
 
     @Override
     public void onGuiClosed() {
         ISOCraft.proxy.sendItemNBTPacket(currentSlot, Reference.SKIP, skip);
     }
 
     private void tab1Back(int xStart, int yStart) {
         mc.renderEngine.bindTexture(Textures.GUI_PDA_EXTRAS);
         this.drawTexturedModalRect(xStart + 40, yStart + 8, 0, 0, 43, 18);
         this.drawTexturedModalRect(xStart + 8, yStart + 40, 0, 18, 151, 190);
     }
 
     private void tab2Back(int xStart, int yStart) {
         mc.renderEngine.bindTexture(Textures.GUI_PDA_EXTRAS);
         this.drawTexturedModalRect(xStart + 87, yStart + 8, 43, 0, 43, 18);
     }
 
     private void tab3Back(int xStart, int yStart) {
         mc.renderEngine.bindTexture(Textures.GUI_PDA_EXTRAS);
         this.drawTexturedModalRect(xStart + 134, yStart + 8, 86, 0, 43, 18);
     }
 }
