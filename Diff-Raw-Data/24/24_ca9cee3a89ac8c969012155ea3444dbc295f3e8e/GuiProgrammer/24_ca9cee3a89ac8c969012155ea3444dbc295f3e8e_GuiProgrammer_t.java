 package obscuron.mkin.gui;
 
 import net.minecraft.client.gui.GuiButton;
 import net.minecraft.client.gui.inventory.GuiContainer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.StatCollector;
 import obscuron.mkin.container.ContainerProgrammer;
 import obscuron.mkin.core.ItemsHandler;
 import obscuron.mkin.item.ItemCard;
 import obscuron.mkin.lib.ContainerInfo;
 import obscuron.mkin.lib.GuiInfo;
 import obscuron.mkin.network.packet.PacketProgrammer;
 import obscuron.mkin.tileentity.TileProgrammer;
 import obscuron.mkin.util.NBTWrapper;
 
 import org.lwjgl.opengl.GL11;
 
 public class GuiProgrammer extends GuiContainer {
 
     private TileProgrammer tileProgrammer;
 
     private int xStart;
     private int yStart;
 
     private final byte ENCODE_ID = 0;
     private GuiButton encodeButton;
 
     private final byte TYPE_ID = 1;
     private GuiButton typeButton;
     private byte typeState = 0;
     private final String[] typeStrings = {
             "Fuzzy",
             "Normal",
             "Exact"
     };
 
     public GuiProgrammer(InventoryPlayer inventoryPlayer, TileProgrammer tile) {
         super(new ContainerProgrammer(inventoryPlayer, tile));
         tileProgrammer = tile;
         xSize = 176;
         ySize = 166;
     }
 
     @Override
     public void initGui() {
         super.initGui();
         buttonList.clear();
         xStart = (width - xSize) / 2;
         yStart = (height - ySize) / 2;
         initButtons();
     }
 
     @SuppressWarnings("unchecked")
     private void initButtons() {
         encodeButton = new GuiButton(ENCODE_ID, xStart + 8, yStart + 16, 60, 20, "Encode");
         typeButton = new GuiButton(TYPE_ID, xStart + 8, yStart + 40, 60, 20, typeStrings[typeState]);
         this.buttonList.add(encodeButton);
         this.buttonList.add(typeButton);
     }
 
     @Override
     protected void drawGuiContainerForegroundLayer(int x, int y) {
         fontRenderer.drawString(StatCollector.translateToLocal(tileProgrammer.getInvName()), 8, 6, 0x404040);
         fontRenderer.drawString(StatCollector.translateToLocal(ContainerInfo.CONTAINER_INVENTORY), 8, ySize - 96 + 2, 0x404040);
     }
 
     @Override
     protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         mc.renderEngine.bindTexture(GuiInfo.PROGRAMMER_GUI_TEXTURE);
         drawTexturedModalRect(xStart, yStart, 0, 0, xSize, ySize);
     }
 
     @Override
     public void actionPerformed(GuiButton button) {
         switch (button.id) {
             case ENCODE_ID:
                 encodeAction();
                 break;
             case TYPE_ID:
                 typeAction();
                 break;
                 
         }
     }
 
     private void encodeAction() {
         ItemStack itemStack = tileProgrammer.getStackInSlot(0);
         ItemStack card = tileProgrammer.getStackInSlot(1);
         
        if (!card.getItem().equals(ItemsHandler.kineticCard)) {
             return;
         }
         
         if (card != null && itemStack != null) {
             NBTWrapper tags = new NBTWrapper(card, ItemCard.TAG_NAME);
            tags.setByte("id", (byte) (typeState + 1));
            tags.setItem("itemInfo", itemStack);
            tileProgrammer.onInventoryChanged();
         }
         
         PacketProgrammer packet = new PacketProgrammer();
         packet.readInfo(tileProgrammer, typeState);
         packet.sendPacket();
     }
 
     private void typeAction() {
         typeState++;
         if (typeState >= typeStrings.length) {
             typeState = 0;
         }
         typeButton.displayString = typeStrings[typeState];
     }
 
 }
