 package zh.usefulthings.gui;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.lwjgl.opengl.GL11;
 
 import zh.usefulthings.UsefulThings;
 
 import net.minecraft.client.gui.GuiButton;
 import net.minecraft.client.gui.GuiScreen;
 import net.minecraft.client.gui.GuiTextField;
 import net.minecraft.client.multiplayer.NetClientHandler;
 import net.minecraft.client.renderer.RenderHelper;
 import net.minecraft.client.renderer.entity.RenderItem;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.network.packet.Packet250CustomPayload;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.EnumChatFormatting;
 import net.minecraft.village.MerchantRecipe;
 import net.minecraft.village.MerchantRecipeList;
 
 //TODO: Clicking the button will buy the items (if possible!) instead of taking the user to the villager's gui
 //TODO: Enable/Disable buttons if the player doesn't have the proper items or enough emeralds
 //TODO: Right clicking a button will take the user to the villager's gui. Config option can switch left/right click functionality...
 //TODO: Configurable option to display the maximum trades the player can make, rather than are available
 //TODO: Configurable option to drop items on ground if the player has enough items to buy it, but not enough space in inventory...
 //TODO: Potential: Add by villager type search mode (via @type or @id in search box)
 //TODO: Add support for custom currencies to be displayed...
 
 public class ZHTradePostGUI extends GuiScreen 
 {
 	private static final int guiLeft = 0;
 	private static final int guiTop = 0;
 	
 	private static RenderItem itemRenderer = new RenderItem();
 	
 	private static final int itemSize = 18, spaceBetweenItems = 10;
 	private static final int buttonWidth = itemSize * 3 + spaceBetweenItems * 4 + 20, buttonHeight = 20;
 	private static final int defaultGridWidth = 8, defaultGridHeight = 8; 
 	private static final int spaceToSaveForBottomUI = 130;
 	private static int gridWidth = 6, gridHeight = 6, gridSize = gridWidth * gridHeight;
 	private static final int guiWidth = buttonWidth + spaceBetweenItems, guiHeight = buttonHeight + 5;
 	private static int gridStartX, gridStartY;
 	
 	private int curPage = -1;
 	private int maxPages;
 	
 	private GuiButton[] tradeButtons;
 	private GuiButton previousButton, nextButton, modeButton;
 	
 	private String guiTitle = UsefulThings.tradeCenter.getLocalizedName();
 	
 	private TileEntity tradePost;
 	private EntityPlayer player;
 	
 	private ArrayList<ZHRecipe> buyRecipes = new ArrayList<ZHRecipe>();
 	private ArrayList<ZHRecipe> sellRecipes = new ArrayList<ZHRecipe>();
 	private ArrayList<ZHRecipe> enchantRecipes = new ArrayList<ZHRecipe>();
 	private ArrayList<ZHRecipe> foundRecipes = new ArrayList<ZHRecipe>();
 	
 	private enum DISPLAY_MODE  {BUY,SELL,ENCHANT}; 
 	private DISPLAY_MODE mode = DISPLAY_MODE.BUY;
 	
 	private GuiTextField searchField;
 	
 	private String searchTarget = "";
 	private boolean searching = false;
 	private boolean searchFinished = true;
 	
 	private class ZHRecipe implements Comparable
 	{
 		public ArrayList<MerchantRecipe> recipes = new ArrayList<MerchantRecipe>();
 		public ArrayList<Integer> villagerIDs = new ArrayList<Integer>();
 		public ItemStack itemToBuy1;
 		public ItemStack itemToBuy2;
 		public ItemStack itemToSell;
 
 		public ZHRecipe(MerchantRecipe recipe, int id)
 		{
 			itemToBuy1 = recipe.getItemToBuy();
 			itemToBuy2 = recipe.getSecondItemToBuy();
 			itemToSell = recipe.getItemToSell();
 			
 			recipes.add(recipe);
 			villagerIDs.add(id);
 		}
 		
 		public boolean equals(MerchantRecipe right)
 		{
 			if(this.itemToBuy1 == right.getItemToBuy() && this.itemToBuy2 == right.getSecondItemToBuy() && this.itemToSell == right.getItemToSell())
 				return true;
 			
 			return false;
 		}
 		
 		public boolean equals(Object right)
 		{
 			if(this.itemToBuy1.itemID == ((ZHRecipe)((ZHRecipe)right)).itemToBuy1.itemID && this.itemToSell.itemID == ((ZHRecipe)((ZHRecipe)right)).itemToSell.itemID &&	
 					this.itemToBuy1.stackSize == ((ZHRecipe)((ZHRecipe)right)).itemToBuy1.stackSize && this.itemToSell.stackSize == ((ZHRecipe)right).itemToSell.stackSize)
 			{
 				if(this.itemToBuy2 != null)
 				{
 					if(((ZHRecipe)right).itemToBuy2 != null)
 					{
 						if (this.itemToBuy2.itemID == ((ZHRecipe)right).itemToBuy2.itemID &&
 								this.itemToBuy2.stackSize == ((ZHRecipe)right).itemToBuy2.stackSize)
 							return true;
 						else
 							return false;
 					}
 					else
 						return false;
 				}
 				else if (((ZHRecipe)right).itemToBuy2 == null)
 					return true;
 				else
 					return false;
 					
 			}
 			else
 				return false;
 		}
 
 		@Override
 		public int compareTo(Object right) 
 		{
 			if (this.itemToBuy1.itemID < ((ZHRecipe)right).itemToBuy1.itemID)
 				return -1;
 			else if (this.itemToBuy1.itemID > ((ZHRecipe)right).itemToBuy1.itemID)
 				return 1;
 			else if (this.itemToBuy1.itemID == ((ZHRecipe)right).itemToBuy1.itemID)
 			{
 				if (this.itemToBuy2 != null)
 				{
 					if(((ZHRecipe)right).itemToBuy2 != null)
 					{
 						if(this.itemToBuy2.itemID < ((ZHRecipe)right).itemToBuy2.itemID)
 							return -1;
 						else if (this.itemToBuy1.itemID > ((ZHRecipe)right).itemToBuy1.itemID)
 							return 1;
 						else
 						{
 							//SO MANY THINGS TO CHECK
 							if(this.itemToSell.itemID < ((ZHRecipe)right).itemToSell.itemID)
 								return -1;
 							else if (this.itemToSell.itemID > ((ZHRecipe)right).itemToSell.itemID)
 								return 1;
 							else
 							{
 								//FINALLY CHECK THE BLOODY STACK SIZES
 								if(this.itemToBuy1.stackSize < ((ZHRecipe)right).itemToBuy1.stackSize)
 									return -1;
 								else if(this.itemToBuy1.stackSize > ((ZHRecipe)right).itemToBuy1.stackSize)
 									return 1;
 								else
 								{
 									//BET YOU THOUGHT WE WERE DONE
 									if(this.itemToBuy2.stackSize < ((ZHRecipe)right).itemToBuy2.stackSize)
 										return -1;
 									else if(this.itemToBuy2.stackSize > ((ZHRecipe)right).itemToBuy2.stackSize)
 										return 1;
 									else
 									{
 										//NOT EVEN CLOSE
 										if(this.itemToSell.stackSize > ((ZHRecipe)right).itemToSell.stackSize)
 											return -1;
 										else if(this.itemToSell.stackSize < ((ZHRecipe)right).itemToSell.stackSize)
 											return 1;
 										//Actually, that was it... for this line...
 									}
 								}
 							}
 						}
 					}
 					else
 						//the right has nothing, but this has something, so... the right is cheaper...
 						return 1;
 				}
 				else if (((ZHRecipe)right).itemToBuy2 != null)
 				{
 					//This needs something else, but right doesn't, thus right is 'cheaper'
 					return 1;
 				}
 				else
 				{
 					//At this point, neither has a second item to buy, so just check the item to sell...
 					if(this.itemToSell.itemID < ((ZHRecipe)right).itemToSell.itemID)
 						return -1;
 					else if (this.itemToSell.itemID > ((ZHRecipe)right).itemToSell.itemID)
 						return 1;
 					else
 					{
 						//FINALLY CHECK THE BLOODY STACK SIZES
 						if(this.itemToBuy1.stackSize < ((ZHRecipe)right).itemToBuy1.stackSize)
 							return -1;
 						else if(this.itemToBuy1.stackSize > ((ZHRecipe)right).itemToBuy1.stackSize)
 							return 1;
 						else
 						{
 							if(this.itemToSell.stackSize > ((ZHRecipe)right).itemToSell.stackSize)
 								//We are getting a better deal with this, so it is 'smaller'
 								return -1;
 							else if(this.itemToSell.stackSize < ((ZHRecipe)right).itemToSell.stackSize)
 								//We are getting a better deal with right, so it is 'smaller'
 								return 1;
 						}
 					}
 				}
 			}
 			
 			//If we ever reach this point, the two are equal, so return 0
 			return 0;
 		}
 	}
 
 	public ZHTradePostGUI(EntityPlayer player, int[] merchantIDs, MerchantRecipeList[] merchantRecipes, TileEntity tradePost)
 	{
 		this.tradePost = tradePost;
 		this.player = player;
 		
 		for(int i = 0; i < merchantRecipes.length; i++)
 		{
 			if (merchantRecipes[i].size() != 0)
 			{
 				for(int j = 0; j < merchantRecipes[i].size(); j++)
 				{
 					ZHRecipe recipe = new ZHRecipe(((MerchantRecipe)(merchantRecipes[i]).get(j)),merchantIDs[i]);
 					
 					if(recipe.itemToBuy1.itemID == recipe.itemToSell.itemID)
 					{
 						int index = recipeAlreadyExist(DISPLAY_MODE.ENCHANT,recipe);
 						
 						if(index != -1)
 						{
 							ZHRecipe temp = enchantRecipes.get(index);
 							temp.recipes.add((MerchantRecipe)merchantRecipes[i].get(j));
 							temp.villagerIDs.add(merchantIDs[i]);
 						}
 						else
 						{
 							enchantRecipes.add(recipe);
 						}
 					}
 					else if(recipe.itemToSell.itemID != Item.emerald.itemID)
 					{
 						int index = recipeAlreadyExist(DISPLAY_MODE.BUY, recipe);
 						
 						if(index != -1)
 						{
 							ZHRecipe temp = buyRecipes.get(index);
 							temp.recipes.add((MerchantRecipe)merchantRecipes[i].get(j));
 							temp.villagerIDs.add(merchantIDs[i]);
 						}
 						else
 						{
 							buyRecipes.add(recipe);
 						}
 					}
 					else
 					{
 						int index = recipeAlreadyExist(DISPLAY_MODE.SELL,recipe);
 						
 						if(index != -1)
 						{
 							ZHRecipe temp = sellRecipes.get(index);
 							temp.recipes.add((MerchantRecipe)merchantRecipes[i].get(j));
 							temp.villagerIDs.add(merchantIDs[i]);
 						}
 						else
 						{
 							sellRecipes.add(recipe);
 						}
 					}
 				}
 			}
 		}
 		
 		Collections.sort(buyRecipes);
 		Collections.sort(sellRecipes);
 		Collections.sort(enchantRecipes);
 	}
 	
 	public void initGui()
 	{
 		buttonList.clear();
 		
 		gridWidth = Math.min(width / guiWidth, defaultGridWidth);
 		
 		gridHeight = Math.min((height - spaceToSaveForBottomUI) / guiHeight, defaultGridHeight);
 		
 		if(gridHeight < 0)
 			gridHeight = 1;
 		
 		gridSize = gridWidth * gridHeight;
 		//UsefulThings.logger.warning("gridSize = " + gridSize);
 		
 		maxPages = (int)Math.ceil((double)getNumRecipes() / (double)gridSize);
 		
 		gridStartX = (width - gridWidth * guiWidth) / 2;
 		gridStartY = 80;
 		
 		searchField = new GuiTextField(fontRenderer, width/2 - 75, gridStartY + (gridHeight + 2) * buttonHeight + this.itemSize,150,buttonHeight);
 		searchField.setFocused(true);
 		searchField.setMaxStringLength(100);
 		searchField.setEnabled(true);
 		searchField.setText(searchTarget);
 		//id,x,y,width,height,text
 		previousButton = new GuiButton(0, width / 2 - 150, 20, 100, 20, "Previous");
 		nextButton = new GuiButton(1, width / 2 + 50, 20, 100, 20, "Next");
 		if(mode == DISPLAY_MODE.BUY)
 			modeButton = new GuiButton(2, width / 2 - 50, 50, 100, 20, "Buy Items");
 		else if(mode == DISPLAY_MODE.SELL)
 			modeButton = new GuiButton(2, width / 2 - 50, 50, 100, 20, "Sell Items");
 		else
 			modeButton = new GuiButton(2, width / 2 - 50, 50, 100, 20, "Enchant Items");
 		
 		buttonList.add(previousButton);
 		buttonList.add(nextButton);
 		buttonList.add(modeButton);
 		
 		tradeButtons = new GuiButton[gridSize];
 		for(int i = 0; i < gridSize; i++)
 		{
 			tradeButtons[i] = new GuiButton(3 + i, gridStartX + i / gridHeight * guiWidth, gridStartY + i % gridHeight * guiHeight, buttonWidth, buttonHeight, "");
 			
 			if(i >= getNumRecipes())
 				tradeButtons[i].enabled = false;
 			
 			buttonList.add(tradeButtons[i]);
 		}
 		
 		setPage(0);
 	}
 	
 	public boolean doesGuiPauseGame()
 	{
 		return false;
 	}
 	
 	public void updateScreen()
 	{
 		boolean flag = false;
 		
 		if (tradePost == null || tradePost.worldObj == null || tradePost.worldObj.getBlockTileEntity(tradePost.xCoord, tradePost.yCoord, tradePost.zCoord) != tradePost)
 			flag = true;
 		
 		if(flag)
 			mc.displayGuiScreen(null);
 	}
 
 	public void keyTyped(char c, int i)
 	{
 		super.keyTyped(c, i);
 		searchField.textboxKeyTyped(c, i);
 		
 		searchTarget = searchField.getText().trim();
 		
 		//UsefulThings.logger.warning("Current Search Target: " + searchTarget);
 		//UsefulThings.logger.warning("Current Search Target length: " + searchTarget.length());
 		
 		if(searchTarget.length() > 0)
 		{
 			this.searching = true;
 			this.searchFinished = false;
 			updateFoundList();
 		}
 		else
 		{
 			this.searching = false;
 			initGui();
 		}
 		
 		//UsefulThings.logger.warning("Is the GUI in search mode: " + searching);
 		
 	}
 	
 	protected void actionPerformed(GuiButton button)
 	{
 		if (button.enabled)
 		{
 			if(button.id == 0)
 				setPage(curPage - 1);
 			else if (button.id == 1)
 				setPage(curPage + 1);
 			else if (button.id == 2)
 			{
 				setMode();
 				updateFoundList();
 			}
 			else
 			{
 				int index = getRecipeForButton(button.id - 3);
 				
 				if (index >= getNumRecipes())
 					return;
 				
 				NetClientHandler net = mc.getNetHandler();
 				
 				if(net != null)
 				{
 					ByteArrayOutputStream output = new ByteArrayOutputStream();
 					DataOutputStream data = new DataOutputStream(output);
 					
 					try
 					{
 						data.writeInt(((Integer)getVillagerID(index)).intValue());
 						data.writeInt(tradePost.worldObj.getWorldInfo().getDimension());
 						data.writeInt(tradePost.xCoord);
 						data.writeInt(tradePost.yCoord);
 						data.writeInt(tradePost.zCoord);
 						net.addToSendQueue(new Packet250CustomPayload(UsefulThings.tradeChannel, output.toByteArray()));
 					}
 					catch (Exception ex)
 					{
 						ex.printStackTrace();
 					}
 				}
 			
 			}
 		}
 	}
 	
 	public void drawScreen(int par1, int par2, float par3)
 	{
 		int emeraldCount = 0;
 		
 		for(int i = 0; i < player.inventory.mainInventory.length; i++)
 			if(player.inventory.mainInventory[i] != null)
 				if(player.inventory.mainInventory[i].itemID == Item.emerald.itemID)
 					emeraldCount += player.inventory.mainInventory[i].stackSize;
 		
 		drawDefaultBackground();
 		drawCenteredString(fontRenderer,guiTitle,width / 2, 5, 16777215);
 		if(maxPages > 1)
 			drawCenteredString(fontRenderer,"Page " + (curPage + 1) + " of " + maxPages,width/2, 25, 16777215);
 		else if (maxPages == 0)
 			drawCenteredString(fontRenderer,"No Trades Found",width/2, 25, 16777215);
 		drawCenteredString(fontRenderer,((Integer)emeraldCount).toString(),width/2 - 10, gridStartY + (gridHeight + 2) * buttonHeight, 16777215);
 		
 		searchField.drawTextBox();
 		
 		super.drawScreen(par1, par2, par2);
 		GL11.glPushMatrix();
 		RenderHelper.enableGUIStandardItemLighting();
 		GL11.glDisable(2896); //GL_LIGHTING
 		GL11.glEnable(32826); //GL_RESCALE_NORMAL_EXT?
 		GL11.glEnable(2903);  //GL_COLOR_MATERIAL
 		GL11.glEnable(2896);  //GL_LIGHTING
 		itemRenderer.zLevel = 100.0F;
 		
 		itemRenderer.renderItemAndEffectIntoGUI(fontRenderer, this.mc.renderEngine,new ItemStack(Item.emerald), width / 2, gridStartY + (gridHeight + 2) * buttonHeight - 5);
 		//itemRenderer.renderItemOverlayIntoGUI(fontRenderer, this.mc.renderEngine, new ItemStack(Item.emerald,1), width / 2, gridStartY + (gridHeight + 2) * buttonHeight - 5);
 		
 		for (int i = 0; i < this.tradeButtons.length; i++)
 		{
 			int j = getRecipeForButton(i);
 		
 			if (j < getNumRecipes())
 			{
 				int x = this.tradeButtons[i].xPosition; int y = this.tradeButtons[i].yPosition;
 		
 				ZHRecipe temp = getRecipe(j);
 				
 				if (temp.itemToBuy2 != null)
 				{
 					itemRenderer.renderItemAndEffectIntoGUI(this.fontRenderer, this.mc.renderEngine, temp.itemToBuy1, x + this.spaceBetweenItems, y + 2);
 					itemRenderer.renderItemOverlayIntoGUI(this.fontRenderer, this.mc.renderEngine, temp.itemToBuy1, x + this.spaceBetweenItems, y + 2);
 					itemRenderer.renderItemAndEffectIntoGUI(this.fontRenderer, this.mc.renderEngine, temp.itemToBuy2, x + this.spaceBetweenItems * 2 + this.itemSize, y + 2);
 					itemRenderer.renderItemOverlayIntoGUI(this.fontRenderer, this.mc.renderEngine, temp.itemToBuy2, x + this.spaceBetweenItems * 2 + this.itemSize, y + 2);
 					itemRenderer.renderItemAndEffectIntoGUI(this.fontRenderer, this.mc.renderEngine, temp.itemToSell, x + 3 * this.spaceBetweenItems + 2 * this.itemSize, y + 2);
 					itemRenderer.renderItemOverlayIntoGUI(this.fontRenderer, this.mc.renderEngine, temp.itemToSell, x + 3 * this.spaceBetweenItems + 2 * this.itemSize, y + 2);
 				}
 				else
 				{
 					itemRenderer.renderItemAndEffectIntoGUI(this.fontRenderer, this.mc.renderEngine, temp.itemToBuy1, x + (this.itemSize + this.spaceBetweenItems) / 2 + this.spaceBetweenItems, y + 2);
 					itemRenderer.renderItemOverlayIntoGUI(this.fontRenderer, this.mc.renderEngine, temp.itemToBuy1, x + (this.itemSize + this.spaceBetweenItems) / 2 + this.spaceBetweenItems, y + 2);
 					itemRenderer.renderItemAndEffectIntoGUI(this.fontRenderer, this.mc.renderEngine, temp.itemToSell, x + (this.itemSize + this.spaceBetweenItems) / 2 + 2 * this.spaceBetweenItems + this.itemSize, y + 2);
 					itemRenderer.renderItemOverlayIntoGUI(this.fontRenderer, this.mc.renderEngine, temp.itemToSell, x + (this.itemSize + this.spaceBetweenItems) / 2 + 2 * this.spaceBetweenItems + this.itemSize, y + 2);
 				}
 			}
 		}
 		
 		itemRenderer.zLevel = 0.0F;
 		GL11.glDisable(2896); //GL_LIGHTING
 		
 		for (int i = 0; i < this.tradeButtons.length; i++)
 		{
 			int j = getRecipeForButton(i);
 		
 			if (j < getNumRecipes())
 			{
 				int x = this.tradeButtons[i].xPosition; int y = this.tradeButtons[i].yPosition;
 
 				ZHRecipe temp = getRecipe(j);
 				
 				if (temp.itemToBuy2 != null)
 				{
 					drawCenteredString(this.fontRenderer, "+", x + this.spaceBetweenItems * 3 / 2 + this.itemSize, y + 7, 16777215);
 					drawCenteredString(this.fontRenderer, "=", x + this.spaceBetweenItems * 5 / 2 + 2 * this.itemSize, y + 7, 16777215);
 					drawString(fontRenderer, ": " + getMaxUses(j), x + this.spaceBetweenItems * 5 / 2 + (int)(3.5 * this.itemSize), y + 7, 16777215);
 				}
 				else
 				{
 					drawCenteredString(this.fontRenderer, "=", x + (this.buttonWidth / 2) - 10, y + 7, 16777215);
 					if(getMaxUses(j) < 10)
 						drawString(fontRenderer, ":  " + getMaxUses(j), x + this.spaceBetweenItems * 5 / 2 + (int)(2.5 * this.itemSize), y + 7, 16777215);
 					else
 						drawString(fontRenderer, ": " + getMaxUses(j), x + this.spaceBetweenItems * 5 / 2 + (int)(2.5 * this.itemSize), y + 7, 16777215);
 		        }
 			}
 		}
 		
 		for (int i = 0; i < this.tradeButtons.length; i++)
 		{
 			int j = getRecipeForButton(i);
 		
 			if (j < getNumRecipes())
 			{
 				int x = this.tradeButtons[i].xPosition; int y = this.tradeButtons[i].yPosition;
 				
 				ZHRecipe temp = getRecipe(j);
 		
 				//TODO: Add tooltip on max trades to show how many villagers have that trade...
 				if (temp.itemToBuy2 != null)
 		        {
 					if (isPointInRegion(x + this.spaceBetweenItems, y, this.itemSize, this.itemSize, par1, par2))
 					{
 						drawItemStackTooltip(temp.itemToBuy1, par1, par2);
 					}
 		
 					if (isPointInRegion(x + this.spaceBetweenItems * 2 + this.itemSize, y, this.itemSize, this.itemSize, par1, par2))
 					{
 						drawItemStackTooltip(temp.itemToBuy2, par1, par2);
 					}
 		
 					if (isPointInRegion(x + 3 * this.spaceBetweenItems + 2 * this.itemSize, y, this.itemSize, this.itemSize, par1, par2))
 					{
 						drawItemStackTooltip(temp.itemToSell, par1, par2);
 					}
 		        }
 		        else
 		        {
 		        	if (isPointInRegion(x + (this.itemSize + this.spaceBetweenItems) / 2 + this.spaceBetweenItems, y, this.itemSize, this.itemSize, par1, par2))
 		        	{
 		        		drawItemStackTooltip(temp.itemToBuy1, par1, par2);
 		        	}
 		
 		        	if (isPointInRegion(x + (this.itemSize + this.spaceBetweenItems) / 2 + 2 * this.spaceBetweenItems + this.itemSize, y, this.itemSize, this.itemSize, par1, par2))
 		        	{
 		        		drawItemStackTooltip(temp.itemToSell, par1, par2);
 		        	}
 		        }
 			}
 		}
 		
 		if(isPointInRegion( width / 2, gridStartY + (gridHeight + 2) * buttonHeight - 5, this.itemSize, this.itemSize, par1, par2))
 			drawItemStackTooltip(new ItemStack(Item.emerald, 1),par1, par2);
 		
 		GL11.glPopMatrix();
 		GL11.glEnable(2896); //GL_LIGHTING
 		GL11.glEnable(2929); //GL_DEPTH_TEST
 		RenderHelper.enableStandardItemLighting();
 	}
 	
 	private void updateFoundList() 
 	{
 		ArrayList<ZHRecipe> temp;
 		
 		foundRecipes.clear();
 		
 		if(mode == DISPLAY_MODE.BUY)
 			temp = buyRecipes;
 		else if(mode == DISPLAY_MODE.SELL)
 			temp = sellRecipes;
 		else
 			temp = enchantRecipes;
 		
 		for(int i = 0; i < temp.size(); i++)
 		{
 			ZHRecipe recipe = temp.get(i);
 			
 			if(mode == DISPLAY_MODE.BUY || mode == DISPLAY_MODE.ENCHANT)
 			{
 				if (recipe.itemToSell.getDisplayName().toLowerCase().equals(searchTarget.toLowerCase()))
 					foundRecipes.add(recipe);
 				else if (recipe.itemToSell.getDisplayName().toLowerCase().indexOf(searchTarget.toLowerCase()) != -1)
 					foundRecipes.add(recipe);
 			}
 			else
 			{
 				if (recipe.itemToBuy1.getDisplayName().toLowerCase().equals(searchTarget.toLowerCase()) ||(recipe.itemToBuy2 != null && recipe.itemToBuy2.getDisplayName().toLowerCase().equals(searchTarget.toLowerCase())))
 					foundRecipes.add(recipe);
 				else if (recipe.itemToBuy1.getDisplayName().toLowerCase().indexOf(searchTarget.toLowerCase()) != -1 || (recipe.itemToBuy2 != null && recipe.itemToBuy2.getDisplayName().toLowerCase().indexOf(searchTarget.toLowerCase()) != -1))
 					foundRecipes.add(recipe);
 			}
 		}
 		
 		searchFinished = true;
 		initGui();
 		
 	}
 	
 	private void setMode() 
 	{
 		if(mode == DISPLAY_MODE.BUY)
 			mode = DISPLAY_MODE.SELL;
 		else if (mode == DISPLAY_MODE.SELL)
 			mode = DISPLAY_MODE.ENCHANT;
 		else
 			mode = DISPLAY_MODE.BUY;
 		
 		initGui();
 	}
 	
 	private int recipeAlreadyExist(DISPLAY_MODE type, ZHRecipe recipe)
 	{
 		int index = -1;
 		
 		ArrayList<ZHRecipe> temp;
 		
 		if(type == DISPLAY_MODE.BUY)
 			temp = buyRecipes;
 		else if(type == DISPLAY_MODE.SELL)
 			temp = sellRecipes;
 		else
 			temp = enchantRecipes;
 		
 		for(int i = 0; i < temp.size(); i++)
 		{
 			ZHRecipe curRecipe = temp.get(i);
 			
 			if (curRecipe.equals(recipe))
 			{
 				index = i;
 				break;
 			}
 		}
 		
 		return index;
 	}
 	
 	private int getNumRecipes()
 	{
 		if (searching && searchFinished)
 			return foundRecipes.size();
 		if (mode == DISPLAY_MODE.BUY)
 			return buyRecipes.size();
 		else if (mode == DISPLAY_MODE.SELL)
 			return sellRecipes.size();
 		else
 			return enchantRecipes.size();
 	}
 	
 	private int getRecipeForButton(int index) 
 	{
 		if(getNumRecipes() != 0)
 			return (this.curPage * this.gridSize) + index;
 		
 		return 0;
 	}
 	
 	public void setPage(int page)
 	{
 		if(page < 0)
 			page = 0;
 		
 		if (page >= maxPages)
 			page = maxPages - 1;
 		
 		this.curPage = page;
 		
 		//UsefulThings.logger.warning("curpage = " + page);
 		
 		for(int i = 0; i < this.tradeButtons.length; i++)
 		{
 			//UsefulThings.logger.warning("Index currently working with: " + i);
 			if (getRecipeForButton(i) < getNumRecipes())
 			{
 				//UsefulThings.logger.warning("Index gotten from getRecipeForButton(" + i + "): " + getRecipeForButton(i));
 				this.tradeButtons[i].enabled = areUsesAvailable(getRecipeForButton(i));
 				//UsefulThings.logger.warning("tradeButtons[" + i + "].enabled = " + tradeButtons[i].enabled);
 			}
 			else
 				this.tradeButtons[i].enabled = false;
 		}
 		
 		if(curPage == 0 || maxPages == 0)
 			previousButton.enabled = false;
 		else
 			previousButton.enabled = true;
 		
 		if(curPage == maxPages - 1)
 			nextButton.enabled = false;
 		else
 			nextButton.enabled = true;
 	}
 	
 	private boolean areUsesAvailable(int index)
 	{
 		boolean flag = false;
 		
 		ZHRecipe list;
 		
 		if(searching && searchFinished)
 		{
 			if (index < foundRecipes.size())
 				list = foundRecipes.get(index);
 			else
 				return false;
 		}
 		else if(mode == DISPLAY_MODE.BUY)
 		{
 			if (index < buyRecipes.size())
 				list = buyRecipes.get(index);
 			else
 				return false;
 		}
 		else if (mode == DISPLAY_MODE.SELL)
 		{
 			if (index < sellRecipes.size())
 				list = sellRecipes.get(index);
 			else
 				return false;
 		}
 		else
 		{
 			if (index < enchantRecipes.size())
 				list = enchantRecipes.get(index);
 			else
 				return false;
 		}
 		
 		for(int i = 0; i < list.recipes.size() && !flag; i++)
 			if(!list.recipes.get(i).func_82784_g())
 				flag = true;
 		
 		return flag;
 	}
 	
 	private int getVillagerID(int index)
 	{
 		int id = -1;
 		boolean flag = false;
 		
 		ZHRecipe list;
 		
 		if(searching && searchFinished)
 		{
 			if (index < foundRecipes.size())
 				list = foundRecipes.get(index);
 			else
 				return -1;
 		}
 		else if(mode == DISPLAY_MODE.BUY)
 		{
 			if(index < buyRecipes.size())
 				list = buyRecipes.get(index);
 			else
 				return -1;
 		}
 		else if (mode == DISPLAY_MODE.SELL)
 		{
 			if(index < sellRecipes.size())
 				list = sellRecipes.get(index);
 			else
 				return -1;
 		}
 		else
 		{
 			if(index < enchantRecipes.size())
 				list = enchantRecipes.get(index);
 			else
 				return -1;
 		}
 		
 		for(int i = 0; i < list.recipes.size() && !flag; i++)
 			if(!list.recipes.get(i).func_82784_g())
 			{
 				flag = true;
 				id = list.villagerIDs.get(i);
 			}
 		
 		return id;
 	}
 	
 	private ZHRecipe getRecipe(int index)
 	{
 		if(index < 0)
 		{
 			return null;
 		}
 		
 		if(searching && searchFinished)
 		{
 			if (index < foundRecipes.size())
 				return foundRecipes.get(index);
 			else
 				return null;
 		}
 		else if (mode == DISPLAY_MODE.BUY)
 			if(index < buyRecipes.size())
 				return buyRecipes.get(index);
 			else
 				return null;
 		else if (mode == DISPLAY_MODE.SELL)
 			if (index < sellRecipes.size())
 				return sellRecipes.get(index);
 			else
 				return null;
 		else
 			if (index < enchantRecipes.size())
 				return enchantRecipes.get(index);
 			else
 				return null;
 	}
 	
 	private int getMaxUses(int index)
 	{
 		int totalUses = 0;
 		
 		ZHRecipe list;
 		
 		if(searching && searchFinished)
 		{
 			if (index < foundRecipes.size())
 				list = foundRecipes.get(index);
 			else
 				return 0;
 		}
 		else if(mode == DISPLAY_MODE.BUY)
 		{
 			if (index < buyRecipes.size())
 				list = buyRecipes.get(index);
 			else
 				return 0;
 		}
 		else if (mode == DISPLAY_MODE.SELL)
 		{
 			if (index < sellRecipes.size())
 				list = sellRecipes.get(index);
 			else
 				return 0;
 		}
 		else
 		{
 			if(index < enchantRecipes.size())
 				list = enchantRecipes.get(index);
 			else
 				return 0;
 		}
 		
 		for(int i = 0; i < list.recipes.size(); i++)
 		{
 			NBTTagCompound temp = list.recipes.get(i).writeToTags(); 
 			
 			int uses = temp.getInteger("uses");
 			int maxUses = temp.getInteger("maxUses");
 			totalUses += maxUses - uses;
 		}
 		
 		return totalUses;
 	}
 	
 	//TODO: Decipher this a bit... from: net.minecraft.client.gui.GUIContainer
 	private void drawItemStackTooltip(ItemStack item, int par2, int par3)
 	{
 		GL11.glDisable(32826); //GL_RESCALE_NORMAL_EXT?
 		RenderHelper.disableStandardItemLighting();
 		GL11.glDisable(2896); //GL_LIGHTING
 		GL11.glDisable(2929); //GL_DEPTH_TEST
 		List list = item.getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips);
 			
 		if (!list.isEmpty())
 		{
 			int k = 0;
 		
 			for (int l = 0; l < list.size(); l++)
 			{
 				int i1 = this.fontRenderer.getStringWidth((String)list.get(l));
 			
 				if (i1 > k)
 				{
 					k = i1;
 				}
 			}
 			
 			int l = par2 + 12;
 			int i1 = par3 - 12;
 			int j1 = 8;
 			
 			if (list.size() > 1)
 			{
 				j1 += 2 + (list.size() - 1) * 10;
 			}
 			
 			this.zLevel = 300.0F;
 			itemRenderer.zLevel = 300.0F;
 			int k1 = -267386864;
 			drawGradientRect(l - 3, i1 - 4, l + k + 3, i1 - 3, k1, k1);
 			drawGradientRect(l - 3, i1 + j1 + 3, l + k + 3, i1 + j1 + 4, k1, k1);
 			drawGradientRect(l - 3, i1 - 3, l + k + 3, i1 + j1 + 3, k1, k1);
 			drawGradientRect(l - 4, i1 - 3, l - 3, i1 + j1 + 3, k1, k1);
 			drawGradientRect(l + k + 3, i1 - 3, l + k + 4, i1 + j1 + 3, k1, k1);
 			int l1 = 1347420415;
 			int i2 = (l1 & 0xFEFEFE) >> 1 | l1 & 0xFF000000;
 			drawGradientRect(l - 3, i1 - 3 + 1, l - 3 + 1, i1 + j1 + 3 - 1, l1, i2);
 			drawGradientRect(l + k + 2, i1 - 3 + 1, l + k + 3, i1 + j1 + 3 - 1, l1, i2);
 			drawGradientRect(l - 3, i1 - 3, l + k + 3, i1 - 3 + 1, l1, l1);
 			drawGradientRect(l - 3, i1 + j1 + 2, l + k + 3, i1 + j1 + 3, i2, i2);
 			
 			for (int j2 = 0; j2 < list.size(); j2++)
 			{
 				String s = (String)list.get(j2);
 			
 				if (j2 == 0)
 			    {
					s = "" + Integer.toHexString(item.getRarity().rarityColor) + s;
 			    }
 			    else
 			    {
 			    	s = EnumChatFormatting.GRAY + s;
 			    }
 			
 			    this.fontRenderer.drawStringWithShadow(s, l, i1, -1);
 			
 			    if (j2 == 0)
 			    {
 			    	i1 += 2;
 			    }
 			
 			    i1 += 10;
 			}
 			
 			this.zLevel = 0.0F;
 			itemRenderer.zLevel = 0.0F;
 		}
 	}
 
 	//TODO: Dechipher this too from GUI: from net.minecraft.client.gui.GUIContainer
 	private boolean isPointInRegion(int par1, int par2, int par3, int par4, int par5, int par6)
 	{
 		int k1 = this.guiTop;
 		int l1 = this.guiLeft;
 		par5 -= k1;
 		par6 -= l1;
 		return (par5 >= par1 - 1) && (par5 < par1 + par3 + 1) && (par6 >= par2 - 1) && (par6 < par2 + par4 + 1);
 	}
 }
