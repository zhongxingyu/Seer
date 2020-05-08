 package vivadaylight3.myrmecology.client.gui;
 
 import java.util.ArrayList;
 
 import net.minecraft.client.gui.GuiButton;
 import net.minecraft.client.gui.ScaledResolution;
 import net.minecraft.client.gui.inventory.GuiContainer;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.EnumChatFormatting;
 import net.minecraft.util.Icon;
 
 import org.lwjgl.opengl.GL11;
 
 import vivadaylight3.myrmecology.api.Breeding;
 import vivadaylight3.myrmecology.api.BreedingRecipe;
 import vivadaylight3.myrmecology.api.ItemAnt;
 import vivadaylight3.myrmecology.api.Metadata;
 import vivadaylight3.myrmecology.api.MyrmopaediaProperties;
 import vivadaylight3.myrmecology.common.inventory.ContainerMyrmopaedia;
 import vivadaylight3.myrmecology.common.inventory.InventoryItem;
 import vivadaylight3.myrmecology.common.lib.Resources;
 import vivadaylight3.myrmecology.common.lib.Strings;
 import vivadaylight3.myrmecology.common.lib.Time;
 import cpw.mods.fml.common.network.Player;
 
 public class GuiMyrmopaedia extends GuiContainer {
 
     private InventoryItem inventory;
     private ContainerMyrmopaedia container;
     private ItemStack myrmopaedia;
 
     private Icon antIcon;
 
     private Player player;
 
     private static final EnumChatFormatting KEY_CHAT_FORMAT = EnumChatFormatting.BLUE;
     private static final EnumChatFormatting VALUE_CHAT_FORMAT = EnumChatFormatting.WHITE;
 
     private GuiButtonSizeable button1Names;
     private GuiButtonSizeable button2Info;
     private GuiButtonSizeable button3Breeding;
     private GuiButtonSizeable button4Ants;
 
     private String selectedScreen = "ants";
 
     // TODO
     private int buttonXOffset;
     private int buttonYOffset;
 
     private int infoButtonWidth = 45;
     private int infoButtonHeight = 15;
 
     private int buttonID;
 
     private ItemAnt selectedAnt;
 
     private ScaledResolution scaledRes;
 
     public GuiMyrmopaedia(ContainerMyrmopaedia parcontainer, EntityPlayer parPlayer) {
 
 	super(new ContainerMyrmopaedia(parcontainer.inventory, parPlayer));
 	this.player = (Player) parPlayer;
 	this.inventory = parcontainer.inventory;
 	this.container = parcontainer;
 	this.myrmopaedia = parcontainer.containerStack;
 	this.selectedScreen = "ants";
 	selectedAnt = null;
 
 	xSize = 248;
 	ySize = 255;
 
     }
 
     @Override
     public void initGui() {
 	super.initGui();
 
 	this.buttonList.clear();
 	this.buttonList.add(this.button1Names = new GuiButtonSizeable(0,
 		this.width / 2 - 85, this.height / 2 + 20, "Names",
 		infoButtonWidth - 4, infoButtonHeight));
 	this.buttonList.add(this.button2Info = new GuiButtonSizeable(1,
 		this.width / 2 - 40, this.height / 2 + 20, "Info",
 		infoButtonWidth - 10, infoButtonHeight));
 	this.buttonList.add(this.button3Breeding = new GuiButtonSizeable(2,
 		this.width / 2, this.height / 2 + 20, "Breeding",
 		infoButtonWidth + 8, infoButtonHeight));
 
 	this.buttonID = 2;
 
     }
 
     @Override
     protected void actionPerformed(GuiButton par1GuiButton) {
 
 	if (par1GuiButton == this.button1Names) {
 
 	    this.selectedScreen = "names";
 
 	} else if (par1GuiButton == this.button2Info) {
 
 	    this.selectedScreen = "info";
 
 	} else if (par1GuiButton == this.button3Breeding) {
 
 	    this.selectedScreen = "breeding";
 
 	} else if (par1GuiButton instanceof GuiButtonAnt) {
 
 	    this.selectedAnt = ((GuiButtonAnt) par1GuiButton).getAnt();
 
 	}
 
     }
 
     @Override
     public void drawScreen(int par1, int par2, float par3) {
 	super.drawScreen(par1, par2, par3);
     }
 
     @Override
     protected void drawGuiContainerForegroundLayer(int par1, int par2) {
 
 	printKeyAndValue(EnumChatFormatting.AQUA + "Selected Screen: ",
 		this.selectedScreen, 15);
 
 	if (this.selectedScreen == "names" && this.selectedAnt != null) {
 
 	    printKeyAndValue("Species: ", this.selectedAnt.getSpeciesName(), 30);
 
 	    printKeyAndValue("Binomial Name: ",
 		    this.selectedAnt.getSpeciesBinomialName(), 45);
 
 	} else if (this.selectedScreen == "info" && this.selectedAnt != null) {
 
 	    printKeyAndValue(
 		    "Lifetime: ",
 		    ""
 			    + Time.getMinutesFromTicks(this.selectedAnt
 				    .getLifetime()), 30);
 
 	    printKeyAndValue(
 		    "Maturing Time: ",
 		    ""
 			    + Time.getMinutesFromTicks(this.selectedAnt
 				    .getMaturingTime()), 45);
 
	    printKeyAndValue("Fetility: ",
 		    "" + this.selectedAnt.getFertility(), 60);
 
 	    this.printBiomes();
 
 	} else if (this.selectedScreen == "breeding"
 		&& this.selectedAnt != null) {
 
 	    ArrayList<BreedingRecipe> breedingList = Breeding
 		    .getAntBreedingOptions(selectedAnt);
 
 	    int offset = 0;
 
 	    int control = 0;
 
 	    for (int k = 0; k < breedingList.toArray().length; k++) {
 
 		if (control == 0) {
 
 		    ItemAnt partner;
 
 		    if (((BreedingRecipe) breedingList.toArray()[k]).getAnt1() == this.selectedAnt) {
 
 			partner = ((BreedingRecipe) breedingList.toArray()[k])
 				.getAnt2();
 
 		    } else {
 
 			partner = ((BreedingRecipe) breedingList.toArray()[k])
 				.getAnt1();
 
 		    }
 
 		    this.drawString(
 			    EnumChatFormatting.GREEN
 				    + this.selectedAnt.getSpeciesName()
 					    .replaceAll(" Ant", "")
 				    + EnumChatFormatting.WHITE
 				    + " + "
 				    + EnumChatFormatting.BLUE
 				    + partner.getSpeciesName().replaceAll(
 					    " Ant", "")
 				    + EnumChatFormatting.WHITE
 				    + " = "
 				    + EnumChatFormatting.YELLOW
 				    + ((BreedingRecipe) breedingList.toArray()[k])
 					    .getAntOutput().getSpeciesName()
 					    .replaceAll(" Ant", ""), 13,
 			    30 + offset);
 
 		    control = 1;
 
 		    offset += 13;
 
 		} else {
 
 		    control = 0;
 
 		}
 
 	    }
 
 	}
 
     }
 
     @Override
     public void updateScreen() {
 
 	super.updateScreen();
 
 	if (this.selectedAnt != null) {
 
 	    this.antIcon = this.selectedAnt.getIconFromDamage(Metadata
 		    .getMetaQueen());
 
 	}
 
 	if (this.antIcon != null) {
 
 	}
 
 	if (this.getAnt() != null) {
 
 	    this.selectedAnt = this.getAnt();
 
 	    MyrmopaediaProperties.addAntToMyrmopaedia(
 		    this.container.containerStack, this.selectedAnt,
 		    this.player);
 
 	    this.antIcon = this.getAnt().getIconFromDamage(
 		    this.inventory.getStackInSlot(0).getItemDamage());
 
 	    // this.container.inventory.decrStackSize(0, 1);
 
 	}// TODO
 
 	int offset = 23;
 
 	if (this.selectedScreen == "ants") {
 
 	    /*
 	     * 
 	     * for (int i = 0; i <
 	     * AntopediaProperties.getAntopediaAnts(this.container
 	     * .containerStack).toArray().length; i++) {
 	     * 
 	     * this.buttonList.add(new GuiButtonAnt(this.buttonID, this.width /
 	     * 3, 24 + offset, ((ItemAnt)
 	     * AntopediaProperties.getAntopediaAnts(this
 	     * .container.containerStack).toArray()[i]) .getSpeciesName(), 100,
 	     * 20, (ItemAnt)
 	     * AntopediaProperties.getAntopediaAnts(this.container.
 	     * containerStack).toArray()[i]));
 	     * 
 	     * this.buttonID++;
 	     * 
 	     * offset += 23;
 	     * 
 	     * }
 	     */
 
 	}
 
     }
 
     private void drawString(String string, int posX, int posY) {
 
 	this.fontRenderer.drawString(string, posX, posY, 4210752);
 
     }
 
     private void drawString(String string, int posX, int posY, int colour,
 	    boolean shadow) {
 
 	this.fontRenderer.drawString(string, posX, posY, colour, shadow);
 
     }
 
     private ItemAnt getAnt() {
 
 	if (this.inventory.getStackInSlot(0) != null) {
 
 	    if (this.inventory.getStackInSlot(0).getItem() instanceof ItemAnt) {
 
 		return (ItemAnt) this.inventory.getStackInSlot(0).getItem();
 
 	    }
 
 	}
 
 	return null;
 
     }
 
     // TODO
     private void printBiomes() {
 
 	if (this.selectedAnt.getAntBiomes() != null) {
 
 	    ArrayList<String> biomeStringList = new ArrayList<String>();
 
 	    for (int k = 0; k < this.selectedAnt.getAntBiomes().length; k++) {
 
 		biomeStringList.add(Strings.getBiomeString(this.selectedAnt
 			.getAntBiomes()[k]));
 
 	    }
 
 
 	    printKeyAndValue("Biomes: ", "All", 75);
 
 	}
 
     }
 
     private void printKeyAndValue(String key, String value, int posY) {
 
 	this.drawString(KEY_CHAT_FORMAT + key + VALUE_CHAT_FORMAT + value, 13,
 		posY);
 
     }
 
     private void drawIcon(Icon icon, int posX, int posY) {
 
 	drawTexturedModelRectFromIcon(posX, posY, icon, 16, 16);
 
     }
 
     @Override
     protected void drawGuiContainerBackgroundLayer(float par1, int par2,
 	    int par3) {
 	GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
 	this.mc.renderEngine.bindTexture(Resources.GUI_ANTOPEDIA);
 	int k = (this.width - this.xSize) / 2;
 	int l = (this.height - this.ySize) / 2;
 	this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);
 	int i1;
 
     }
 
 }
