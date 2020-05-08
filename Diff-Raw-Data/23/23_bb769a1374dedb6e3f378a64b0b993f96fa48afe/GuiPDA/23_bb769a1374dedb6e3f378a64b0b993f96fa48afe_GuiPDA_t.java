 package com.isocraft.client.gui;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 
 import net.minecraft.client.gui.FontRenderer;
 import net.minecraft.client.gui.GuiButton;
 import net.minecraft.client.gui.inventory.GuiContainer;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.util.StatCollector;
 import net.minecraft.world.World;
 import net.minecraftforge.oredict.OreDictionary;
 
 import org.lwjgl.opengl.GL11;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import com.isocraft.ISOCraft;
 import com.isocraft.core.helpers.LogHelper;
 import com.isocraft.inventory.ContainerPDA;
 import com.isocraft.item.crafting.XmlRecipeReference;
 import com.isocraft.lib.Colours;
 import com.isocraft.lib.ItemInfo;
 import com.isocraft.lib.Strings;
 import com.isocraft.thesis.ThesisSystem;
 import com.isocraft.thesis.ThesisSystemClient;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 /**
  * ISOCraft
  * 
  * Gui for PDA
  * 
  * @author Turnermator13
  */
 
 @SideOnly(Side.CLIENT)
 public class GuiPDA extends GuiContainer {
 
 	private boolean topButtonEnabled = true;
 	private boolean bottomButtonEnabled = true;
 
 	public int currentSlot;
 	private boolean packet = false;
 	public ItemStack pda = null;
 	private int tab = 1;
 
 	private boolean change = true;
 	private List<String> objList;
 	private int[] scroll = new int[] { 0, 0, 0, 0 };
 	private int stage = 0;
 	private String thesis = "*";
 	private String[] selection = new String[] { null, null };
 
 	private int swapTick;
	private int thresh = 60;
 	private int[] disp = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
 	private int[] dispMax = new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1 };
 	private ItemStack[][] cyc = new ItemStack[][] { null, null, null, null, null, null, null, null, null };
 
 	/* NBT TAGS */
 	public String tagScroll = "scroll";
 	public String tagTab = "tab";
 	public String tagStage = "stage";
 	public String tagLastSelection1 = "lastSelection1";
 	public String tagLastSelection2 = "lastSelection2";
 	public String tagSelectedThesis = "thesisSelected";
 
 	public GuiPDA(EntityPlayer player, World world, int x, int y, int z) {
 		super(new ContainerPDA(player.inventory, world, x, y, z));
 		this.pda = player.inventory.getCurrentItem();
 		this.currentSlot = player.inventory.currentItem;
 		this.nbtSetup();
 		this.xSize = 217;
 		this.ySize = 250;
 	}
 
 	@Override
 	protected void actionPerformed(GuiButton par1GuiButton) {
 		if (par1GuiButton.enabled) {
 			if (par1GuiButton.id == 0) {
 				this.tab = 1;
 				this.change = true;
 			}
 			if (par1GuiButton.id == 1) {
 				this.tab = 2;
 				this.change = true;
 			}
 			if (par1GuiButton.id == 2) {
 				this.tab = 3;
 				this.change = true;
 			}
 			if (par1GuiButton.id == 3) {
 				this.change = true;
 				--this.scroll[this.tab];
 			}
 			if (par1GuiButton.id == 4) {
 				this.change = true;
 				++this.scroll[this.tab];
 			}
 
 			if (par1GuiButton.id == 5) {
 				this.change = true;
 				--this.stage;
 				this.thesis = "*";
 				this.objList = ThesisSystemClient.clientDisplayThesisList;
 			}
 
 			// Discovery Buttons
 			if (par1GuiButton.id == 6) {
 				if (this.stage == 0) {
 					this.change = true;
 					this.stage = 1;
 					this.thesis = this.objList.get(this.scroll[1] + 0);
 					this.objList = ThesisSystemClient.clientDisplayTheoremsList.get(this.thesis);
 				}
 				else {
 					this.change = true;
 					this.selection[0] = this.thesis;
 					this.selection[1] = this.objList.get(this.scroll[1] + 0);
 					this.scroll[2] = 0;
 					this.tab = 2;
 				}
 			}
 			if (par1GuiButton.id == 7) {
 				if (this.stage == 0) {
 					this.change = true;
 					this.stage = 1;
 					this.thesis = this.objList.get(this.scroll[1] + 1);
 					this.objList = ThesisSystemClient.clientDisplayTheoremsList.get(this.thesis);
 				}
 				else {
 					this.change = true;
 					this.selection[0] = this.thesis;
 					this.selection[1] = this.objList.get(this.scroll[1] + 1);
 					this.scroll[2] = 0;
 					this.tab = 2;
 				}
 			}
 			if (par1GuiButton.id == 8) {
 				if (this.stage == 0) {
 					this.change = true;
 					this.stage = 1;
 					this.thesis = this.objList.get(this.scroll[1] + 2);
 					this.objList = ThesisSystemClient.clientDisplayTheoremsList.get(this.thesis);
 				}
 				else {
 					this.change = true;
 					this.selection[0] = this.thesis;
 					this.selection[1] = this.objList.get(this.scroll[1] + 2);
 					this.scroll[2] = 0;
 					this.tab = 2;
 				}
 			}
 			if (par1GuiButton.id == 9) {
 				if (this.stage == 0) {
 					this.change = true;
 					this.stage = 1;
 					this.thesis = this.objList.get(this.scroll[1] + 3);
 					this.objList = ThesisSystemClient.clientDisplayTheoremsList.get(this.thesis);
 				}
 				else {
 					this.change = true;
 					this.selection[0] = this.thesis;
 					this.selection[1] = this.objList.get(this.scroll[1] + 3);
 					this.scroll[2] = 0;
 					this.tab = 2;
 				}
 			}
 			if (par1GuiButton.id == 10) {
 				if (this.stage == 0) {
 					this.change = true;
 					this.stage = 1;
 					this.thesis = this.objList.get(this.scroll[1] + 4);
 					this.objList = ThesisSystemClient.clientDisplayTheoremsList.get(this.thesis);
 				}
 				else {
 					this.change = true;
 					this.selection[0] = this.thesis;
 					this.selection[1] = this.objList.get(this.scroll[1] + 4);
 					this.scroll[2] = 0;
 					this.tab = 2;
 				}
 			}
 
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void ButtonManager(int xStart, int yStart) {
 		int y = yStart;
 		int j = 6;
 
 		if (this.stage == 0) {
 			if (this.objList.size() < 6) {
 				for (int i = 0; i < this.objList.size(); ++i) {
 					try {
 						this.buttonList.add(new GuiPDADiscoveryButton(j, xStart, y, ThesisSystem.getThesisName(this.objList.get(i)), false));
 					}
 					catch (IOException e) {
 						LogHelper.log(Level.SEVERE, " No XML tags were found for the thesis/theorem");
 						e.printStackTrace();
 					}
 					y = y + 37;
 					j++;
 				}
 			}
 			else if (this.objList.size() > 5) {
 				for (int i = this.scroll[1]; i < this.scroll[1] + 5; ++i) {
 					try {
 						this.buttonList.add(new GuiPDADiscoveryButton(j, xStart, y, ThesisSystem.getThesisName(this.objList.get(i)), false));
 					}
 					catch (IOException e) {
 						LogHelper.log(Level.SEVERE, " No XML tags were found for the thesis/theorem");
 						e.printStackTrace();
 					}
 					y = y + 37;
 					++j;
 				}
 			}
 		}
 		else {
 			y = y + 3;
 			if (this.objList.size() < 9) {
 				for (int i = 0; i < this.objList.size(); ++i) {
 					try {
 						this.buttonList.add(new GuiPDADiscoveryButton(j, xStart, y, ThesisSystem.getTheoremName(this.thesis, this.objList.get(i)), true));
 					}
 					catch (IOException e) {
 						LogHelper.log(Level.SEVERE, " No XML tags were found for the thesis/theorem");
 						e.printStackTrace();
 					}
 					y = y + 23;
 					j++;
 				}
 			}
 			else if (this.objList.size() > 8) {
 				for (int i = this.scroll[1]; i < this.scroll[1] + 8; ++i) {
 					try {
 						this.buttonList.add(new GuiPDADiscoveryButton(j, xStart, y, ThesisSystem.getTheoremName(this.thesis, this.objList.get(i)), true));
 					}
 					catch (IOException e) {
 						LogHelper.log(Level.SEVERE, " No XML tags were found for the thesis/theorem");
 						e.printStackTrace();
 					}
 					y = y + 23;
 					++j;
 				}
 			}
 		}
 	}
 
 	@Override
 	protected void drawGuiContainerBackgroundLayer(float opacity, int x, int y) {
 		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
 		this.mc.getTextureManager().bindTexture(ItemInfo.PDA_gui_path);
 		int xStart = (this.width - this.xSize) / 2;
 		int yStart = (this.height - this.ySize) / 2;
 		this.drawTexturedModalRect(xStart, yStart, 0, 0, this.xSize, this.ySize);
 
 		if (this.tab == 1) {
 			this.tab1Back(xStart, yStart);
 		}
 		if (this.tab == 2) {
 			this.tab2Back(xStart, yStart);
 		}
 		if (this.tab == 3) {
 			this.tab3Back(xStart, yStart);
 		}
 	}
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	@Override
 	protected void drawGuiContainerForegroundLayer(int x, int y) {
 		int xStart = (this.width - this.xSize) / 2;
 		int yStart = (this.height - this.ySize) / 2;
 
 		if (!this.packet) {
 			if (this.tab == 1 || this.tab == 2 || this.tab == 3) {
 				this.fontRenderer.drawString(StatCollector.translateToLocal("Updating..."), xStart + 61, yStart + 72, 4210752);
 			}
 			if (ThesisSystemClient.clientDisplayUpdate) {
 				this.packet = true;
 				ThesisSystemClient.clientDisplayUpdate = false;
 
 				if (this.thesis.equals("*")) {
 					this.objList = ThesisSystemClient.clientDisplayThesisList;
 				}
 				else {
 					this.objList = ThesisSystemClient.clientDisplayTheoremsList.get(this.thesis);
 				}
 			}
 		}
 		else {
 			if (this.change) {
 				this.initGui();
 			}
 			if (this.tab == 1) {
 				if (this.change) {
 
 					this.change = false;
 					this.topButtonEnabled = true;
 					this.bottomButtonEnabled = true;
 					if (this.stage == 0) {
 						if (this.objList.size() < 6) {
 							this.topButtonEnabled = false;
 							this.bottomButtonEnabled = false;
 						}
 						else if (this.scroll[1] == 0) {
 							this.topButtonEnabled = false;
 						}
 						else if (this.scroll[1] == this.objList.size() - 5) {
 							this.bottomButtonEnabled = false;
 						}
 					}
 					else if (this.objList.size() < 9) {
 						this.topButtonEnabled = false;
 						this.bottomButtonEnabled = false;
 					}
 					else if (this.scroll[1] == 0) {
 						this.topButtonEnabled = false;
 					}
 					else if (this.scroll[1] == this.objList.size() - 8) {
 						this.bottomButtonEnabled = false;
 					}
 
 					this.ButtonManager(xStart + 10, yStart + 54);
 					this.buttonList.add(new GuiPDAScrollButton(3, xStart + 174, yStart + 94, true, this.topButtonEnabled));
 					this.buttonList.add(new GuiPDAScrollButton(4, xStart + 174, yStart + 168, false, this.bottomButtonEnabled));
 
 					this.buttonList.add(new GuiPDABackButton(5, xStart + 11, yStart + 33, this.stage > 0));
 				}
 				String path = "../";
 				if (!this.thesis.equals("*")) {
 					try {
 						path = "../" + ThesisSystem.getThesisName(this.thesis) + "/";
 					}
 					catch (IOException e) {
 						LogHelper.log(Level.SEVERE, " No XML tags were found for the thesis/theorem");
 						e.printStackTrace();
 					}
 				}
 				this.fontRenderer.drawString(StatCollector.translateToLocal(path), xStart + 28, yStart + 36, Integer.parseInt(Colours.WHITE, 16));
 			}
 			else if (this.tab == 2) {
 				if (this.selection[0] != null && this.selection[1] != null) {
 					String path = "../";
 					if (!this.selection[0].equals(null) && !this.selection[1].equals(null)) {
 						try {
 							path = "../" + ThesisSystem.getThesisName(this.selection[0]) + "/" + ThesisSystem.getTheoremName(this.selection[0], this.selection[1]);
 						}
 						catch (IOException e) {
 							LogHelper.log(Level.SEVERE, " No XML tags were found for the thesis/theorem");
 							e.printStackTrace();
 						}
 					}
 					this.fontRenderer.drawString(StatCollector.translateToLocal(path), xStart + 11, yStart + 36, Integer.parseInt(Colours.WHITE, 16));
 
 					NodeList nNode = ThesisSystem.getTheoremPageNodeList(this.selection[0], this.selection[1]);
 					Element page = (Element) nNode.item(this.scroll[2]);
 					if (page.getAttribute(Strings.X_TYPE).toString().equals(Strings.P_TEXT)) {
 						if (page.hasAttribute(Strings.X_TITLE)) {
 							int gap = 140 - this.fontRenderer.getStringWidth(page.getAttribute(Strings.X_TITLE).toString());
 							this.fontRenderer.drawString(page.getAttribute(Strings.X_TITLE).toString(), xStart + 14 + gap / 2, yStart + 59, Integer.parseInt(Colours.WHITE, 16));
 							this.fontRenderer.drawSplitString(page.getTextContent(), xStart + 14, yStart + 74, 140, Integer.parseInt(Colours.WHITE, 16));
 						}
 						else {
 							this.fontRenderer.drawSplitString(page.getTextContent(), xStart + 14, yStart + 59, 140, Integer.parseInt(Colours.WHITE, 16));
 						}
 					}
 
 					else if (page.getAttribute(Strings.X_TYPE).toString().equals(Strings.P_CRAFTING)) {
 						XmlRecipeReference obj = ThesisSystem.xmlCraftingReference.get(page.getTextContent());
 						this.swapTick = this.swapTick + 1;
 						if (this.swapTick == this.thresh) {
 							this.swapTick = 0;
 							for (int p = 0; p < 9; ++p) {
 								++this.disp[p];
 								if (this.disp[p] == this.dispMax[p]) {
 									this.disp[p] = 0;
 								}
 							}
 						}
 
 						if (!obj.getTitle().isEmpty()) {
 							int gap = 140 - this.fontRenderer.getStringWidth(obj.getTitle());
 							this.fontRenderer.drawString(obj.getTitle(), xStart + 14 + gap / 2, yStart + 59, Integer.parseInt(Colours.WHITE, 16));
 						}
 
 						if (!obj.getText().isEmpty()) {
 							this.fontRenderer.drawSplitString(obj.getText(), xStart + 14, yStart + 150, 140, Integer.parseInt(Colours.WHITE, 16));
 						}
 
 						this.mc.getTextureManager().bindTexture(ItemInfo.PDA_gui_extras_path);
 						this.drawTexturedModalRect(xStart + 31, yStart + 78, 151, 62, 105, 66);
 
 						int i = 0;
						int iY = 79;
 						List<ItemStack> drawH = new ArrayList<ItemStack>();
 
 						for (int j = 0; j < obj.getWidth(); ++j) {
							int iX = 32;
 							for (int k = 0; k < obj.getHeight(); ++k) {
 
 								Object l = obj.getItems()[i];
 								ItemStack h = null;
 								if (l instanceof ItemStack) {
 									h = (ItemStack) l;
 								}
 								else if (l instanceof String) {
 									ArrayList<ItemStack> g = OreDictionary.getOres(l.toString());
 									if (this.change) {
 										
 										int eLength = 0;
 										for (int p = 0; p < g.size(); ++p) {
 											if (g.get(p).getItemDamage() == Short.MAX_VALUE) {
 												
 												List subTypeList = new ArrayList();
 												g.get(p).getItem().getSubItems(g.get(p).getItem().itemID, null, subTypeList);
 												
 												eLength = eLength + subTypeList.size();
 											}
 											else {
 												++eLength;	
 											}
 										}										
 										this.dispMax[i] = eLength;
 										
 										this.cyc[i] = new ItemStack[eLength];
 										int bump = 0;
 										for (int p = 0; p < g.size(); ++p) {
 											
 											if (g.get(p).getItemDamage() == Short.MAX_VALUE) {
 												List subTypeList = new ArrayList();
 												g.get(p).getItem().getSubItems(g.get(p).getItem().itemID, null, subTypeList);
 												
 												--bump;
 												ItemStack tmp = g.get(p);
 												for (int m = 0; m < subTypeList.size(); ++m) {
 													++bump;
 													int q = (p + bump);
 													this.cyc[i][q] = new ItemStack(tmp.getItem(), 1, m);
 												}
 											}
 											else {
 												int q = (p + bump);
 												this.cyc[i][q] = g.get(p);
 											}
 										}										
 									}
 									h = this.cyc[i][this.disp[i]];
 								}
 
 								this.drawItemStack(h, xStart + iX + 1, yStart + iY + 1);
 								if (this.isPointInRegion(xStart + iX + 1, yStart + iY + 1, 16, 16, x, y)) {
 									drawH.add(h);
 								}
 								++i;
								iX = iX + 23;
 							}
							iY = iY + 23;
 						}
 
 						this.drawItemStack(obj.getResult(), xStart + 114, yStart + 103);
 						if (this.isPointInRegion(xStart + 114, yStart + 105, 16, 16, x, y)) {
 							drawH.add(obj.getResult());
 						}
 						for (int p = 0; p < drawH.size(); ++p) {
 							this.drawItemStackTooltip(drawH.get(p), x, y);
 						}
 					}
 					if (this.change) {
 						this.change = false;
 						this.swapTick = 0;
 						for (int p = 0; p < 9; ++p) {
 							this.disp[p] = 0;
 						}
 
 						this.topButtonEnabled = true;
 						this.bottomButtonEnabled = true;
 						if (nNode.getLength() == 1) {
 							this.topButtonEnabled = false;
 							this.bottomButtonEnabled = false;
 						}
 						else if (this.scroll[2] == 0) {
 							this.topButtonEnabled = false;
 						}
 						else if (this.scroll[2] == nNode.getLength() - 1) {
 							this.bottomButtonEnabled = false;
 						}
 						this.buttonList.add(new GuiPDAScrollButton(3, xStart + 174, yStart + 94, true, this.topButtonEnabled));
 						this.buttonList.add(new GuiPDAScrollButton(4, xStart + 174, yStart + 168, false, this.bottomButtonEnabled));
 					}
 				}
 			}
 			else if (this.tab == 3) {
 
 			}
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void initGui() {
 		this.buttonList.clear();
 		int xStart = (this.width - this.xSize) / 2;
 		int yStart = (this.height - this.ySize) / 2;
 		this.buttonList.add(new GuiPDATabButton(0, xStart + 39, yStart + 7));
 		this.buttonList.add(new GuiPDATabButton(1, xStart + 86, yStart + 7));
 		this.buttonList.add(new GuiPDATabButton(2, xStart + 133, yStart + 7));
 	}
 
 	private void nbtSetup() {
 		NBTTagCompound tag = this.pda.stackTagCompound;
 
 		if (tag.hasKey(this.tagTab)) {
 			this.tab = this.pda.getTagCompound().getInteger(this.tagTab);
 		}
 
 		if (tag.hasKey(this.tagScroll + "1") && tag.hasKey(this.tagScroll + "2") && tag.hasKey(this.tagScroll + "3")) {
 			for (int j = 1; j < 4; j++) {
 				this.scroll[j] = this.pda.getTagCompound().getInteger(this.tagScroll + j);
 			}
 		}
 
 		if (tag.hasKey(this.tagLastSelection1)) {
 			this.selection[0] = this.pda.getTagCompound().getString(this.tagLastSelection1);
 			this.selection[1] = this.pda.getTagCompound().getString(this.tagLastSelection2);
 		}
 
 		if (tag.hasKey(this.tagSelectedThesis)) {
 			this.thesis = this.pda.getTagCompound().getString(this.tagSelectedThesis);
 		}
 
 		if (tag.hasKey(this.tagStage)) {
 			this.stage = this.pda.getTagCompound().getInteger(this.tagStage);
 		}
 	}
 
 	@Override
 	public void onGuiClosed() {
 		ISOCraft.proxy.sendItemNBTPacket(this.currentSlot, this.tagTab, this.tab);
 		for (int j = 1; j < 4; j++) {
 			ISOCraft.proxy.sendItemNBTPacket(this.currentSlot, this.tagScroll + j, this.scroll[j]);
 		}
 
 		ISOCraft.proxy.sendItemNBTPacket(this.currentSlot, this.tagStage, this.stage);
 		ISOCraft.proxy.sendItemNBTPacket(this.currentSlot, this.tagSelectedThesis, this.thesis);
 
 		if (this.selection[0] != null) {
 			ISOCraft.proxy.sendItemNBTPacket(this.currentSlot, this.tagLastSelection1, this.selection[0]);
 		}
 		if (this.selection[1] != null) {
 			ISOCraft.proxy.sendItemNBTPacket(this.currentSlot, this.tagLastSelection2, this.selection[1]);
 		}
 	}
 
 	private void tab1Back(int xStart, int yStart) {
 		this.mc.getTextureManager().bindTexture(ItemInfo.PDA_gui_extras_path);
 		this.drawTexturedModalRect(xStart + 40, yStart + 8, 0, 0, 43, 18);
 		this.drawTexturedModalRect(xStart + 8, yStart + 52, 0, 18, 151, 190);
 
 		this.drawTexturedModalRect(xStart + 25, yStart + 33, 0, 243, 134, 13);
 		this.drawTexturedModalRect(xStart + 158, yStart + 33, 150, 243, 1, 13);
 	}
 
 	private void tab2Back(int xStart, int yStart) {
 		this.mc.getTextureManager().bindTexture(ItemInfo.PDA_gui_extras_path);
 		this.drawTexturedModalRect(xStart + 87, yStart + 8, 43, 0, 43, 18);
 		this.drawTexturedModalRect(xStart + 8, yStart + 52, 0, 18, 151, 190);
 
 		this.drawTexturedModalRect(xStart + 8, yStart + 33, 0, 243, 151, 13);
 	}
 
 	private void tab3Back(int xStart, int yStart) {
 		this.mc.getTextureManager().bindTexture(ItemInfo.PDA_gui_extras_path);
 		this.drawTexturedModalRect(xStart + 134, yStart + 8, 86, 0, 43, 18);
 	}
 
 	public void drawItemStack(ItemStack par1ItemStack, int par2, int par3) {
 		GL11.glTranslatef(0.0F, 0.0F, 32.0F);
 		this.zLevel = 200.0F;
 		itemRenderer.zLevel = 200.0F;
 		FontRenderer font = null;
 		if (par1ItemStack != null) {
 			font = par1ItemStack.getItem().getFontRenderer(par1ItemStack);
 		}
 		if (font == null) {
 			font = this.fontRenderer;
 		}
 		itemRenderer.renderItemAndEffectIntoGUI(font, this.mc.getTextureManager(), par1ItemStack, par2, par3);
 		this.zLevel = 0.0F;
 		itemRenderer.zLevel = 0.0F;
 	}
 }
