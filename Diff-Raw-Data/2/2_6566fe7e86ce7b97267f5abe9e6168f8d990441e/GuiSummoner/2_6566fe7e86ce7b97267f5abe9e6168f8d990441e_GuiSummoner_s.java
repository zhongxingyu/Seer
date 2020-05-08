 package demonmodders.crymod.client.gui;
 
 import static demonmodders.crymod.common.gui.ContainerSummoner.BUTTON_NEXT_PAGE;
 import static demonmodders.crymod.common.gui.ContainerSummoner.BUTTON_PREV_PAGE;
 import static demonmodders.crymod.common.gui.ContainerSummoner.BUTTON_SUMMON;
 import net.minecraft.client.gui.GuiButton;
 
 import org.lwjgl.opengl.GL11;
 
 import demonmodders.crymod.common.gui.ContainerSummoner;
 import demonmodders.crymod.common.inventory.InventorySummoner;
 import demonmodders.crymod.common.inventory.AbstractInventory.InventoryChangeListener;
 import demonmodders.crymod.common.recipes.SummoningRecipe;
 
 public class GuiSummoner extends AbstractGuiContainer<ContainerSummoner, InventorySummoner> implements InventoryChangeListener {
 
 	private static final int EFFECTIVE_HEIGHT = 256;
 	private static final int EFFECTIVE_WIDTH = 176;
 	private static final int BOOK_WIDTH = 159;
 	private static final int BOOK_HEIGHT = 163;
 	private static final int ARROW_HEIGHT = 10;
 	private static final int ARROW_WIDTH = 18;
 
 	private static final int HEADING_TEXT_FIELD_X_POSITION = 39;
 	private static final int HEADING_TEXT_FIELD_Y_POSITION = 17;
 	
 	private final String texture;
 	private String recipeName = "";
 	
 	public GuiSummoner(String texture, ContainerSummoner container) {
 		super(container);
 		this.texture = texture;
 		xSize = EFFECTIVE_WIDTH;
 		ySize = EFFECTIVE_HEIGHT;
 		container.getInventoryInstance().registerListener(this);
 	}
 
 	@Override
 	public void initGui() {
 		super.initGui();
 		int buttonNextX = (width - EFFECTIVE_WIDTH) / 2 + BOOK_WIDTH + 3;
 		int buttonPrevX = (width - EFFECTIVE_WIDTH) / 2 - 3;
 		
 		int buttonY = (height - EFFECTIVE_HEIGHT) / 2 + BOOK_HEIGHT - ARROW_HEIGHT - 3;
 		
 		controlList.add(new GuiButtonImage(BUTTON_NEXT_PAGE, buttonNextX, buttonY, ARROW_WIDTH, ARROW_HEIGHT, 256 - ARROW_WIDTH, 0, texture));
 		controlList.add(new GuiButtonImage(BUTTON_PREV_PAGE, buttonPrevX, buttonY, ARROW_WIDTH, ARROW_HEIGHT, 256 - ARROW_WIDTH - ARROW_WIDTH, 0, texture));
 		controlList.add(new GuiButton(BUTTON_SUMMON, width / 2 - EFFECTIVE_WIDTH / 2 + 49, height / 2 - EFFECTIVE_HEIGHT / 2 + 150, 80, 20, "Summon"));
 	}
 
 	@Override
 	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
 		int demonNameWidth = fontRenderer.getStringWidth(recipeName);
 		fontRenderer.drawString(recipeName, HEADING_TEXT_FIELD_X_POSITION, HEADING_TEXT_FIELD_Y_POSITION, 0xffffff);
		fontRenderer.drawString(String.valueOf(container.page()), width / 2 - BOOK_WIDTH / 2, height / 2 + 3, 0x000050);
 	}
 	
 	@Override
 	protected void actionPerformed(GuiButton button) {
 		super.actionPerformed(button);
 		if (button.id != BUTTON_SUMMON) {
 			updateRecipeName();
 		}
 	}
 
 	@Override
 	protected String getTextureFile() {
 		return texture;
 	}
 
 	@Override
 	public void onInventoryChange() {
 		updateRecipeName();
 	}
 	
 	private void updateRecipeName() {
 		SummoningRecipe recipe = container.currentMatchingRecipe();
 		if (recipe != null) {
 			recipeName = recipe.getRecipeName();
 		} else {
 			recipeName = "";
 		}
 	}
 }
