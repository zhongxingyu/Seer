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
 import obscuron.mkin.util.ColorUtil;
 import obscuron.mkin.util.KeyUtil;
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
     
     private final byte SIDE_ID = 2;
     private GuiButton sideButton;
     private byte sideState = 0;
     private final String[] sideStrings = {
             ColorUtil.BLUE + "Top",
             ColorUtil.GOLD + "Bottom",
            ColorUtil.RED + "North",
             ColorUtil.YELLOW + "South",
            ColorUtil.PURPLE + "East",
            ColorUtil.GREEN + "West"
     };
     
     private final byte COUNT_ID = 3;
     private GuiButton countButton;
     private byte countState;
     private final String[] countStrings = {
             "=",
             ">=",
             ">",
             "<",
             "<="
     };
     private final int COUNT_MAX = 4096;
     private int count;
     
     private GuiButton incButton;
     private GuiButton decButton;
     private GuiButton incFastButton;
     private GuiButton decFastButton;
 
     public GuiProgrammer(InventoryPlayer inventoryPlayer, TileProgrammer tile) {
         super(new ContainerProgrammer(inventoryPlayer, tile));
         tileProgrammer = tile;
         xSize = 176;
         ySize = 181;
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
         encodeButton = new GuiButton(ENCODE_ID, xStart + 8, yStart + 64, 60, 20, "Encode");
         typeButton = new GuiButton(TYPE_ID, xStart + 8, yStart + 16, 60, 20, typeStrings[typeState]);
         sideButton = new GuiButton(SIDE_ID, xStart + 8, yStart + 40, 60, 20, sideStrings[sideState]);
         countButton = new GuiButton(COUNT_ID, xStart + 124, yStart + 35, 20, 20, countStrings[countState]);
         incButton = new GuiButton(COUNT_ID + 1, xStart + 148, yStart + 27, 20, 20, "+");
         decButton = new GuiButton(COUNT_ID + 2, xStart + 100, yStart + 27, 20, 20, "-");
         incFastButton = new GuiButton(COUNT_ID + 3, xStart + 148, yStart + 49, 20, 20, "++");
         decFastButton = new GuiButton(COUNT_ID + 4, xStart + 100, yStart + 49, 20, 20, "--");
         
         this.buttonList.add(encodeButton);
         this.buttonList.add(typeButton);
         this.buttonList.add(sideButton);
         this.buttonList.add(countButton);
         this.buttonList.add(incButton);
         this.buttonList.add(decButton);
         this.buttonList.add(incFastButton);
         this.buttonList.add(decFastButton);
     }
 
     @Override
     protected void drawGuiContainerForegroundLayer(int x, int y) {
         fontRenderer.drawString(StatCollector.translateToLocal(tileProgrammer.getInvName()), 8, 6, 0x404040);
         fontRenderer.drawString(StatCollector.translateToLocal(ContainerInfo.CONTAINER_INVENTORY), 8, ySize - 96 + 2, 0x404040);
         int width = fontRenderer.getStringWidth(Integer.toString(count));
         fontRenderer.drawString(Integer.toString(count), 134 - width/2, 56, 0x000000);
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
             case SIDE_ID:
                 sideAction();
                 break;
             case COUNT_ID:
                 countAction();
                 break;
             default:
                 changeCount(button.id - 4, KeyUtil.isShiftHeld());
         }
     }
 
     private void encodeAction() {
         ItemStack itemStack = tileProgrammer.getStackInSlot(0);
         ItemStack card = tileProgrammer.getStackInSlot(1);
         
         if (card != null && itemStack != null) {
             if (!card.getItem().equals(ItemsHandler.kineticCard)) {
                 return;
             }
             NBTWrapper tags = new NBTWrapper(card, ItemCard.TAG_NAME);
             tags.setByte("id", (byte) (typeState + 1));
             tags.setByte("side", sideState);
             tags.setByte("countState", countState);
             tags.setInt("count", count);
             tags.setItem("itemInfo", itemStack);
             tileProgrammer.onInventoryChanged();
         }
         
         PacketProgrammer packet = new PacketProgrammer();
         packet.readInfo(tileProgrammer, typeState, sideState, countState, count);
         packet.sendPacket();
     }
 
     private void typeAction() {
         typeState++;
         if (typeState >= typeStrings.length) {
             typeState = 0;
         }
         typeButton.displayString = typeStrings[typeState];
     }
     
     private void sideAction() {
         sideState++;
         if (sideState >= sideStrings.length) {
             sideState = 0;
         }
         sideButton.displayString = sideStrings[sideState];
     }
     
     private void countAction() {
         countState++;
         if (countState >= countStrings.length) {
             countState = 0;
         }
         countButton.displayString = countStrings[countState];
     }
 
     private void changeCount(int id, boolean shiftHeld) {
         if (id < 0 || id > 3) {
             return;
         }
         int[] amount = {1, -1, 64, -64};
         if (shiftHeld) {
             count += amount[id] * 8;
         }
         else {
             count += amount[id];
         }
         if (count < 0) {
             count = 0;
         }
         if (count > COUNT_MAX) {
             count = COUNT_MAX;
         }
     }
     
 }
