 package modJam;
 
 import org.lwjgl.opengl.GL11;
 
 import net.minecraft.client.gui.GuiButton;
 import net.minecraft.client.gui.inventory.GuiContainer;
 import net.minecraft.inventory.Container;
 
 public class GuiThemePreferences extends GuiContainer {
 
 	public int[] currentPrefs = ModJam.themeHandler.readConfiguration();
 	public int columnWidth = 176;
 	public int columnHeight = 66;
 	
 	public int numberOfFiles = 2;
 	public int themesPerFile = 3;
 	
 	public int previewXPos = 0, previewYPos = 0;
 	
 	public GuiLightButton subFile;
 	public GuiLightButton addFile;
 	public GuiLightButton subIndex;
 	public GuiLightButton addIndex;
 	public GuiLightButton savePrefs;
 	
 	public GuiThemePreferences() {
 		super(new ContainerDummy());
 	}
 
     /**
      * Adds the buttons (and other controls) to the screen in question.
      */
     public void initGui(){
         super.initGui();
         subFile = new GuiLightButton(0, 0, 0, 40, 20, "-");
         addFile = new GuiLightButton(1, 0, 0, 40, 20, "+");
         subIndex = new GuiLightButton(2, 0, 0, 40, 20, "-");
         addIndex = new GuiLightButton(3, 0, 0, 40, 20, "+");
         savePrefs = new GuiLightButton(4, 0, 0, 60, 20, "Select");
         this.buttonList.add(subFile);
         this.buttonList.add(addFile);
         this.buttonList.add(subIndex);
         this.buttonList.add(addIndex);
         this.buttonList.add(savePrefs);
         alignButtonsAndPreviewWindow();
     }
 	
 	@Override
 	protected void drawGuiContainerForegroundLayer(int par1, int par2){
        fontRenderer.drawString("Theme Preferences", 8, 5, 0xFFFFFF);
        fontRenderer.drawString("Preview:", 8, 73, 0xFFFFFF);
        fontRenderer.drawString("File: " + (currentPrefs[0] + 1), 8, 15, 0xFFFFFF);
        fontRenderer.drawString("Index: " + (currentPrefs[1] + 1), 8, 45, 0xFFFFFF);
 	}
     
 	private void alignButtonsAndPreviewWindow(){
 		previewXPos = (width - xSize) / 2;
 		previewYPos = this.height / 2;
 		subFile.xPosition = this.width / 2 - 40;
 		subFile.yPosition = this.height / 2 - 60;
 		addFile.xPosition = this.width / 2;
 		addFile.yPosition = this.height / 2 - 60;
 		subIndex.xPosition = this.width / 2 - 40;
 		subIndex.yPosition = this.height / 2 - 30;
 		addIndex.xPosition = this.width / 2;
 		addIndex.yPosition = this.height / 2 - 30;
 		savePrefs.xPosition = this.width / 2 - savePrefs.width - 20;
 		savePrefs.yPosition = this.height / 2 + 23;
 		
 	}
 	
 	@Override
 	protected void actionPerformed(GuiButton par1GuiButton){
 		switch (par1GuiButton.id) {
 		case 0:
 			if(currentPrefs[0] - 1 >= 0){
 				currentPrefs[0]--;
 			}
 			break;
 		case 1:
 			if(currentPrefs[0] + 1 < this.numberOfFiles){
 				currentPrefs[0]++;
 			}
 			break;
 		case 2:
 			if(currentPrefs[1] - 1 >= 0){
 				currentPrefs[1]--;
 			}
 			break;
 		case 3:
 			if(currentPrefs[1] + 1 < themesPerFile){
 				currentPrefs[1]++;
 			}
 			break;
 		case 4:
 			ModJam.themeHandler.writeConfiguration(currentPrefs[0], currentPrefs[1]);
 			break;
 		default:
 			break;
 		}
 	}
 	
 	@Override
 	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         this.mc.renderEngine.bindTexture("/gui/themePreferences.png");
         int x = (width - xSize) / 2;
         int y = (height - ySize) / 2;
         this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
         String textureFilePostfix = currentPrefs[0] == 0 ? "" : Integer.toString(currentPrefs[0] + 1);
         this.mc.renderEngine.bindTexture("/gui/lightSettings" + textureFilePostfix + ".png");
         this.drawTexturedModalRect(previewXPos, previewYPos, 0, this.currentPrefs[1] * columnHeight, columnWidth, columnHeight);
         alignButtonsAndPreviewWindow();
         
 	}
 
 }
