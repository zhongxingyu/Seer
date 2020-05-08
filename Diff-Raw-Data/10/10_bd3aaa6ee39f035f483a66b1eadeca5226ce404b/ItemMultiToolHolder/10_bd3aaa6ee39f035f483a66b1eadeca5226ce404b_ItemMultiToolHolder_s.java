 package ak.MultiToolHolders;
 
 import static net.minecraftforge.client.IItemRenderer.ItemRenderType.*;
 
 import java.io.DataOutputStream;
 import java.util.List;
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.gui.ScaledResolution;
 import net.minecraft.client.renderer.ItemRenderer;
 import net.minecraft.client.renderer.Tessellator;
 import net.minecraft.client.renderer.entity.RenderManager;
 import net.minecraft.enchantment.Enchantment;
 import net.minecraft.enchantment.EnchantmentHelper;
 import net.minecraft.enchantment.EnchantmentThorns;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.EnumAction;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemDye;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.potion.Potion;
 import net.minecraft.stats.AchievementList;
 import net.minecraft.stats.StatList;
 import net.minecraft.storagebox.ItemStorageBox;
 import net.minecraft.util.DamageSource;
 import net.minecraft.util.Icon;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.World;
 import net.minecraftforge.client.IItemRenderer;
 import net.minecraftforge.client.MinecraftForgeClient;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.event.entity.player.AttackEntityEvent;
 import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
 
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL12;
 
 import ak.MultiToolHolders.Client.MTHKeyHandler;
 
 import com.google.common.io.ByteArrayDataInput;
 
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class ItemMultiToolHolder extends Item implements IItemRenderer
 {
 	public int SlotNum;
 	public ToolHolderData tools;
 	private Random rand = new Random();
 	private int Slotsize;
 	private boolean OpenKeydown;
 	private boolean NextKeydown;
 	private boolean PrevKeydown;
 	private Minecraft mc;
 	public ItemMultiToolHolder(int par1, int slot)
 	{
 		super(par1);
 		this.hasSubtypes = true;
 		this.setMaxStackSize(1);
 		this.Slotsize = slot;
 		this.SlotNum = 0;
 	}
 	public void addInformation(ItemStack item, EntityPlayer player, List list, boolean advToolTio) {
 		String ToolName;
 		int SlotNum;
 		if(item != null && item.getItem() instanceof ItemMultiToolHolder)
 		{
 			ToolHolderData tooldata = ItemMultiToolHolder.getToolData(item, player.worldObj);
 			SlotNum = ((ItemMultiToolHolder)item.getItem()).SlotNum;
 			if(tooldata != null)
 			{
 				if(tooldata.getStackInSlot(SlotNum) != null)
 				{
 					if(MultiToolHolders.loadSB  && tooldata.getStackInSlot(SlotNum).getItem() instanceof ItemStorageBox)
 					{
 						tooldata.getStackInSlot(SlotNum).getItem().addInformation(tooldata.getStackInSlot(SlotNum), player, list, advToolTio);
 					}
 					else
 					{
 						ToolName =  tooldata.getStackInSlot(SlotNum).getDisplayName();
 						list.add(ToolName);
 					}
 
 				}
 			}
 		}
 	}
 	@SideOnly(Side.CLIENT)
 	public boolean isFull3D()
 	{
 		if(this.tools !=null && this.tools.getStackInSlot(SlotNum) != null)
 			return this.tools.getStackInSlot(SlotNum).getItem().isFull3D();
 		else
 			return false;
 	}
 	@SideOnly(Side.CLIENT)
 	@Override
 	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
 		return type == ItemRenderType.EQUIPPED;
 	}
 	@SideOnly(Side.CLIENT)
 	@Override
 	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
 		ToolHolderData tooldata = ((ItemMultiToolHolder)item.getItem()).tools;
 		if(tooldata != null && tooldata.getStackInSlot(SlotNum) != null)
 		{
 			ItemStack tool = tooldata.getStackInSlot(SlotNum);
 			IItemRenderer customRenderer = MinecraftForgeClient.getItemRenderer(tool, EQUIPPED);
 			if(customRenderer != null)
 				return customRenderer.shouldUseRenderHelper(type, tool, helper);
 			else
 				return helper == ItemRendererHelper.EQUIPPED_BLOCK;
 		}
 		else
 			return helper == ItemRendererHelper.EQUIPPED_BLOCK;
 	}
 	@SideOnly(Side.CLIENT)
 	@Override
 	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
 		ToolHolderData tooldata = ((ItemMultiToolHolder)item.getItem()).tools;//this.getData(item, ((EntityLiving) data[1]).worldObj);
 		if(tooldata != null && tooldata.getStackInSlot(SlotNum) != null)
 		{
 			ItemStack tool = tooldata.getStackInSlot(SlotNum);
 			IItemRenderer customRenderer = MinecraftForgeClient.getItemRenderer(tool, EQUIPPED);
 			if(customRenderer !=null)
 				customRenderer.renderItem(type, tool, data);
 			else
 				renderToolHolder((EntityLiving) data[1], tool);
 		}
 		else
 		{
 			renderToolHolder((EntityLiving) data[1], item);
 		}
 	}
 	@SideOnly(Side.CLIENT)
 	public void renderToolHolder(EntityLiving entity, ItemStack stack)
 	{
 		mc = Minecraft.getMinecraft();
 		Icon icon = entity.getItemIcon(stack, 0);
 		if (icon == null)
 		{
 //			GL11.glPopMatrix();
 			return;
 		}
 
 		if (stack.getItemSpriteNumber() == 0)
 		{
 			this.mc.renderEngine.bindTexture("/terrain.png");
 		}
 		else
 		{
 			this.mc.renderEngine.bindTexture("/gui/items.png");
 		}
 		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
 		Tessellator tessellator = Tessellator.instance;
 		float f = icon.getMinU();
 		float f1 = icon.getMaxU();
 		float f2 = icon.getMinV();
 		float f3 = icon.getMaxV();
 		float f4 = 0.0F;
 		float f5 = 0.3F;
 		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
 		GL11.glTranslatef(-f4, -f5, 0.0F);
 		float f6 = 1.5F;
 		GL11.glScalef(f6, f6, f6);
 		GL11.glRotatef(50.0F, 0.0F, 1.0F, 0.0F);
 		GL11.glRotatef(335.0F, 0.0F, 0.0F, 1.0F);
 		GL11.glTranslatef(-0.9375F, -0.0625F, 0.0F);
 		RenderManager.instance.itemRenderer.renderItemIn2D(tessellator, f1, f2, f, f3, icon.getSheetWidth(), icon.getSheetHeight(), 0.0625F);
 
 		if (stack != null && stack.hasEffect()/* && par3 == 0*/)
 		{
 			GL11.glDepthFunc(GL11.GL_EQUAL);
 			GL11.glDisable(GL11.GL_LIGHTING);
 			this.mc.renderEngine.bindTexture("%blur%/misc/glint.png");
 			GL11.glEnable(GL11.GL_BLEND);
 			GL11.glBlendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE);
 			float f7 = 0.76F;
 			GL11.glColor4f(0.5F * f7, 0.25F * f7, 0.8F * f7, 1.0F);
 			GL11.glMatrixMode(GL11.GL_TEXTURE);
 			GL11.glPushMatrix();
 			float f8 = 0.125F;
 			GL11.glScalef(f8, f8, f8);
 			float f9 = (float)(Minecraft.getSystemTime() % 3000L) / 3000.0F * 8.0F;
 			GL11.glTranslatef(f9, 0.0F, 0.0F);
 			GL11.glRotatef(-50.0F, 0.0F, 0.0F, 1.0F);
 			ItemRenderer.renderItemIn2D(tessellator, 0.0F, 0.0F, 1.0F, 1.0F, 256, 256, 0.0625F);
 			GL11.glPopMatrix();
 			GL11.glPushMatrix();
 			GL11.glScalef(f8, f8, f8);
 			f9 = (float)(Minecraft.getSystemTime() % 4873L) / 4873.0F * 8.0F;
 			GL11.glTranslatef(-f9, 0.0F, 0.0F);
 			GL11.glRotatef(10.0F, 0.0F, 0.0F, 1.0F);
 			ItemRenderer.renderItemIn2D(tessellator, 0.0F, 0.0F, 1.0F, 1.0F, 256, 256, 0.0625F);
 			GL11.glPopMatrix();
 			GL11.glMatrixMode(GL11.GL_MODELVIEW);
 			GL11.glDisable(GL11.GL_BLEND);
 			GL11.glEnable(GL11.GL_LIGHTING);
 			GL11.glDepthFunc(GL11.GL_LEQUAL);
 		}
 
 		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
 	}
 	@SideOnly(Side.CLIENT)
 	public static void renderToolsInfo(ItemStack item)
 	{
 		Minecraft mc = Minecraft.getMinecraft();
 		String name = item.getDisplayName();
 		int meta = item.getItemDamage();
 		int wide = mc.displayWidth;
 		int height = mc.displayHeight;
 		ScaledResolution sr = new ScaledResolution(mc.gameSettings, wide, height);
 		wide = sr.getScaledWidth();
 		height = sr.getScaledHeight();
 		mc.fontRenderer.drawString(name, MultiToolHolders.toolTipX, height - MultiToolHolders.toolTipY, ItemDye.dyeColors[15]);
 		mc.fontRenderer.drawString(String.valueOf(meta), MultiToolHolders.toolTipX, height - MultiToolHolders.toolTipY +10, ItemDye.dyeColors[15]);
 	}
 	public void onUpdate(ItemStack item, World world, Entity entity, int par4, boolean par5) {
 		if (entity instanceof EntityPlayer && par5)
 		{
 			EntityPlayer entityPlayer = (EntityPlayer) entity;
 //			ItemMultiToolHolder toolholder = (ItemMultiToolHolder) item.getItem();
 			ItemStack nowItem;
 			Side side = FMLCommonHandler.instance().getEffectiveSide();
 			if(side.isServer())
 			{
 				this.tools = this.getData(item, world);
 				this.tools.onUpdate(world, entityPlayer);
 				this.tools.onInventoryChanged();
 			}
 			if (item.hasTagCompound())
 			{
 				item.getTagCompound().removeTag("ench");
 			}
 			if(this.tools != null && this.tools.getStackInSlot(SlotNum) != null)
 			{
 				nowItem = this.tools.getStackInSlot(SlotNum);
 				nowItem.getItem().onUpdate(nowItem, world, entity, par4, par5);
 				this.setEnchantments(item, nowItem);
 			}
 			if(par5 && (entityPlayer.openContainer == null || !(entityPlayer.openContainer instanceof ContainerToolHolder)))
 			{
 				if(side.isClient())
 				{
 					this.NextKeydown = MTHKeyHandler.nextKeydown && MTHKeyHandler.nextKeyup;
 					this.PrevKeydown = MTHKeyHandler.prevKeydown && MTHKeyHandler.prevKeyup;
 					String name;
 					int meta;
 					String display;
 
 					if(this.NextKeydown)
 					{
 						MTHKeyHandler.nextKeyup = false;
 						this.SlotNum++;
 						if(this.SlotNum == this.Slotsize)
 							this.SlotNum = 0;
 					}
 					else if(this.PrevKeydown)
 					{
 						MTHKeyHandler.prevKeyup = false;
 						this.SlotNum--;
 						if(this.SlotNum == -1)
 							this.SlotNum = this.Slotsize - 1;
 					}
 					
 					if((this.NextKeydown || this.PrevKeydown) && this.tools != null  && this.tools.getStackInSlot(SlotNum) != null)
 					{
 						nowItem = this.tools.getStackInSlot(SlotNum);
 						name = nowItem.getDisplayName();
 						meta = nowItem.getMaxDamage() -  nowItem.getItemDamage();
 						display = String.format("%s duration:%d", name, meta);                                
 						if(MultiToolHolders.loadSB && nowItem.getItem() instanceof ItemStorageBox && ItemStorageBox.peekItemStackAll(nowItem) != null)
 						{
 							ItemStack storaged = ItemStorageBox.peekItemStackAll(nowItem);
 							name = storaged.getDisplayName();
 							meta = storaged.stackSize;
 							display = String.format("%s Stack:%d", name,meta);
 						}
 						entityPlayer.addChatMessage(display);
 					}
 					this.OpenKeydown = MTHKeyHandler.openKeydown && MTHKeyHandler.openKeyup;
 					PacketDispatcher.sendPacketToServer(PacketHandler.getPacketTool(this));
 				}
 				if(this.OpenKeydown)
 				{
 					MTHKeyHandler.openKeyup = false;
 					int GuiID = (this.Slotsize == 3)? MultiToolHolders.guiIdHolder3:(this.Slotsize == 5)? MultiToolHolders.guiIdHolder5:(this.Slotsize == 7)? MultiToolHolders.guiIdHolder7:MultiToolHolders.guiIdHolder9;
 					entityPlayer.openGui(MultiToolHolders.instance, GuiID, world, 0, 0, 0);
 				}
 			}
 		}
 	}
 	public static ToolHolderData getToolData(ItemStack item, World world)
 	{
 		ToolHolderData tooldata = null;
 		if(item != null && item.getItem() instanceof ItemMultiToolHolder)
 		{
 			tooldata = ((ItemMultiToolHolder)item.getItem()).getData(item, world);
 		}
 		return tooldata;
 	}
 	public ToolHolderData getData(ItemStack var1, World var2)
 	{
 		String itemName = "Holder" + this.Slotsize;
 		int itemDamage = var1.getItemDamage();
 		String var3 = String.format("%s_%s", itemName, itemDamage);;
 		ToolHolderData var4 = (ToolHolderData)var2.loadItemData(ToolHolderData.class, var3);
 
 		if (var4 == null)
 		{
 			var4 = new ToolHolderData(var3);
 			var4.markDirty();
 			var2.setItemData(var3, var4);
 		}
 
 		return var4;
 	}
 	private void makeData(ItemStack var1, World var2)
 	{
 		String itemName = "Holder" + this.Slotsize;
 		var1.setItemDamage(var2.getUniqueDataId(itemName));
 		int itemDamage = var1.getItemDamage();
 		String var3 = String.format("%s_%s", itemName, itemDamage);;
 		ToolHolderData var4 = new ToolHolderData(var3);
 		var4.markDirty();
 		var2.setItemData(var3, var4);
 	}
 	public void onCreated(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
 	{
 		if (par1ItemStack.getItem() instanceof ItemMultiToolHolder)
 		{
 			this.makeData(par1ItemStack, par2World);
 		}
 	}
 	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity)
 	{
 		if(this.tools.getStackInSlot(SlotNum) != null)
 		{
 			this.attackTargetEntityWithTheItem(entity, player, this.tools.getStackInSlot(SlotNum));
 			return true;
 		}
 		else
 			return false;
 	}
 	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
 	{
 		if(this.tools !=null && this.tools.getStackInSlot(SlotNum) != null)
 		{
 		boolean ret = this.tools.getStackInSlot(SlotNum).getItem().onItemUseFirst(this.tools.getStackInSlot(SlotNum), player, world, x, y, z, side, hitX, hitY, hitZ);
 			if (this.tools.getStackInSlot(SlotNum).stackSize <= 0)
 			{
 				this.destroyTheItem(player, this.tools.getStackInSlot(SlotNum));
 			}
 			return ret;
 		}
 		else
 			return false;
 	}
 	public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, int par4, int par5, int par6, int par7, float par8, float par9, float par10)
 	{
 		if(this.tools !=null && this.tools.getStackInSlot(SlotNum) != null)
 		{
 			boolean ret = this.tools.getStackInSlot(SlotNum).getItem().onItemUse(this.tools.getStackInSlot(SlotNum), par2EntityPlayer, par3World, par4, par5, par6, par7, par8, par9, par10);
 			if (this.tools.getStackInSlot(SlotNum).stackSize <= 0)
 			{
 				this.destroyTheItem(par2EntityPlayer, this.tools.getStackInSlot(SlotNum));
 			}
 			return ret;
 		}
 		else
 			return false;
 	}
 	public void onPlayerStoppedUsing(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer, int par4) {
 		if(this.tools !=null && this.tools.getStackInSlot(SlotNum) != null)
 		{
 			this.tools.getStackInSlot(SlotNum).getItem().onPlayerStoppedUsing(this.tools.getStackInSlot(SlotNum), par2World, par3EntityPlayer, par4);
 			if (this.tools.getStackInSlot(SlotNum).stackSize <= 0)
 			{
 				this.destroyTheItem( par3EntityPlayer, this.tools.getStackInSlot(SlotNum));
 			}
 		}
 	}
 	public ItemStack onEaten(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
 	{
 		if(this.tools !=null && this.tools.getStackInSlot(SlotNum) != null)
 		{
 			this.tools.getStackInSlot(SlotNum).getItem().onEaten(this.tools.getStackInSlot(SlotNum), par2World, par3EntityPlayer);
 		}
 		return par1ItemStack;
 	}
 	public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
 	{
 		if(this.tools !=null && this.tools.getStackInSlot(SlotNum) != null)
 		{
 			this.tools.setInventorySlotContents(SlotNum, this.tools.getStackInSlot(SlotNum).getItem().onItemRightClick(this.tools.getStackInSlot(SlotNum), par2World, par3EntityPlayer));
 		}
		par3EntityPlayer.setItemInUse(par1ItemStack, this.getMaxItemUseDuration(par1ItemStack));
 		return par1ItemStack;
 	}
     public boolean itemInteractionForEntity(ItemStack par1ItemStack, EntityLiving par2EntityLiving)
     {
 		if(this.tools != null && this.tools.getStackInSlot(SlotNum) != null)
 			return this.tools.getStackInSlot(SlotNum).getItem().itemInteractionForEntity(this.tools.getStackInSlot(SlotNum), par2EntityLiving);
 		else
 			return false;
     }
 	public EnumAction getItemUseAction(ItemStack par1ItemStack)
 	{
 		if(this.tools !=null && this.tools.getStackInSlot(SlotNum) != null)
 		{
 			return this.tools.getStackInSlot(SlotNum).getItemUseAction();
 		}
 		else
 			return super.getItemUseAction(par1ItemStack);
 	}
 	public int getMaxItemUseDuration(ItemStack par1ItemStack)
 	{
 		if(this.tools !=null && this.tools.getStackInSlot(SlotNum) != null)
 		{
 			return this.tools.getStackInSlot(SlotNum).getMaxItemUseDuration();
 		}
 		else
 			return super.getMaxItemUseDuration(par1ItemStack);
 	}
 	public float getStrVsBlock(ItemStack par1ItemStack, Block par2Block){
 		if(this.tools !=null && this.tools.getStackInSlot(SlotNum) != null)
 			return this.tools.getStackInSlot(SlotNum).getStrVsBlock(par2Block);
 		else
 			return super.getStrVsBlock(par1ItemStack, par2Block);
 	}
 	@Override
 	public float getStrVsBlock(ItemStack stack, Block block, int meta)
 	{
 		if(this.tools !=null && this.tools.getStackInSlot(SlotNum) != null)
 		{
 			return this.tools.getStackInSlot(SlotNum).getItem().getStrVsBlock(this.tools.getStackInSlot(SlotNum), block, meta);
 		}
 		else
 			return super.getStrVsBlock(stack, block, meta);
 	}
 	public int getDamageVsEntity(Entity par1Entity)
 	{
 		if(this.tools !=null && this.tools.getStackInSlot(SlotNum) != null)
 		{
 			return this.tools.getStackInSlot(SlotNum).getItem().getDamageVsEntity(par1Entity);
 		}
 		else
 			return super.getDamageVsEntity(par1Entity);
 	}
 	public int getDamageVsEntity(Entity par1Entity, ItemStack itemStack)
 	{
 		if(this.tools !=null && this.tools.getStackInSlot(SlotNum) != null)
 		{
 			return this.tools.getStackInSlot(SlotNum).getItem().getDamageVsEntity(par1Entity, this.tools.getStackInSlot(SlotNum));
 		}
 		else
 			return super.getDamageVsEntity(par1Entity, itemStack);
 	}
 	public boolean canHarvestBlock(Block par1Block)
 	{
 		if(this.tools !=null && this.tools.getStackInSlot(SlotNum) != null)
 		{
 			return this.tools.getStackInSlot(SlotNum).canHarvestBlock(par1Block);
 		}
 		else
 			return super.canHarvestBlock(par1Block);
 	}
     public boolean onBlockDestroyed(ItemStack par1ItemStack, World par2World, int par3, int par4, int par5, int par6, EntityLiving par7EntityLiving)
     {
 		if(this.tools !=null && this.tools.getStackInSlot(SlotNum) != null)
 		{
 			boolean ret = this.tools.getStackInSlot(SlotNum).getItem().onBlockDestroyed(this.tools.getStackInSlot(SlotNum), par2World, par3, par4, par5, par6, par7EntityLiving);
 			if (this.tools.getStackInSlot(SlotNum).stackSize <= 0)
 			{
 				this.destroyTheItem((EntityPlayer) par7EntityLiving, this.tools.getStackInSlot(SlotNum));
 			}
 			this.tools.onInventoryChanged();
 			return ret;
 		}
 		else
 			return super.onBlockDestroyed(par1ItemStack, par2World, par3, par4, par5, par6, par7EntityLiving);
     }
 	public void attackTargetEntityWithTheItem(Entity par1Entity, EntityPlayer player,ItemStack stack)
 	{
 		if (MinecraftForge.EVENT_BUS.post(new AttackEntityEvent(player, par1Entity)))
 		{
 			return;
 		}
 		if (stack != null && stack.getItem().onLeftClickEntity(stack, player, par1Entity))
 		{
 			return;
 		}
 		if (par1Entity.canAttackWithItem())
 		{
 			if (!par1Entity.func_85031_j(player))
 			{
 				int var2 = stack.getDamageVsEntity(par1Entity);
 
 				if (player.isPotionActive(Potion.damageBoost))
 				{
 					var2 += 3 << player.getActivePotionEffect(Potion.damageBoost).getAmplifier();
 				}
 
 				if (player.isPotionActive(Potion.weakness))
 				{
 					var2 -= 2 << player.getActivePotionEffect(Potion.weakness).getAmplifier();
 				}
 
 				int var3 = 0;
 				int var4 = 0;
 
 				if (par1Entity instanceof EntityLiving)
 				{
 					var4 = this.getEnchantmentModifierLiving(stack, player, (EntityLiving)par1Entity);
 					var3 += EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, stack);
 				}
 
 				if (player.isSprinting())
 				{
 					++var3;
 				}
 
 				if (var2 > 0 || var4 > 0)
 				{
 					boolean var5 = player.fallDistance > 0.0F && !player.onGround && !player.isOnLadder() && !player.isInWater() && !player.isPotionActive(Potion.blindness) && player.ridingEntity == null && par1Entity instanceof EntityLiving;
 
 					if (var5)
 					{
 						var2 += this.rand.nextInt(var2 / 2 + 2);
 					}
 
 					var2 += var4;
 					boolean var6 = false;
 					int var7 = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack);
 
 					if (par1Entity instanceof EntityLiving && var7 > 0 && !par1Entity.isBurning())
 					{
 						var6 = true;
 						par1Entity.setFire(1);
 					}
 
 					boolean var8 = par1Entity.attackEntityFrom(DamageSource.causePlayerDamage(player), var2);
 
 					if (var8)
 					{
 						if (var3 > 0)
 						{
 							par1Entity.addVelocity((double)(-MathHelper.sin(player.rotationYaw * (float)Math.PI / 180.0F) * (float)var3 * 0.5F), 0.1D, (double)(MathHelper.cos(player.rotationYaw * (float)Math.PI / 180.0F) * (float)var3 * 0.5F));
 							player.motionX *= 0.6D;
 							player.motionZ *= 0.6D;
 							player.setSprinting(false);
 						}
 
 						if (var5)
 						{
 							player.onCriticalHit(par1Entity);
 						}
 
 						if (var4 > 0)
 						{
 							player.onEnchantmentCritical(par1Entity);
 						}
 
 						if (var2 >= 18)
 						{
 							player.triggerAchievement(AchievementList.overkill);
 						}
 
 						player.setLastAttackingEntity(par1Entity);
 
 						if (par1Entity instanceof EntityLiving)
 						{
 							EnchantmentThorns.func_92096_a(player, (EntityLiving)par1Entity, this.rand);
 						}
 					}
 
 					ItemStack var9 = stack;
 
 					if (var9 != null && par1Entity instanceof EntityLiving)
 					{
 						var9.hitEntity((EntityLiving)par1Entity, player);
 
 						if (var9.stackSize <= 0)
 						{
 							this.destroyTheItem(player, stack);
 						}
 					}
 
 					if (par1Entity instanceof EntityLiving)
 					{
 
 
 						player.addStat(StatList.damageDealtStat, var2);
 
 						if (var7 > 0 && var8)
 						{
 							par1Entity.setFire(var7 * 4);
 						}
 						else if (var6)
 						{
 							par1Entity.extinguish();
 						}
 					}
 
 					player.addExhaustion(0.3F);
 				}
 			}
 		}
 	}
 	public void destroyTheItem(EntityPlayer player, ItemStack orig)
 	{
 		this.tools.setInventorySlotContents(this.SlotNum, (ItemStack)null);
 		MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(player, orig));
 	}
 	public int getEnchantmentModifierLiving(ItemStack stack, EntityLiving attacker, EntityLiving enemy)
 	{
 		int calc = 0;
 		if (stack != null)
 		{
 			NBTTagList nbttaglist = stack.getEnchantmentTagList();
 
 			if (nbttaglist != null)
 			{
 				for (int i = 0; i < nbttaglist.tagCount(); ++i)
 				{
 					short short1 = ((NBTTagCompound)nbttaglist.tagAt(i)).getShort("id");
 					short short2 = ((NBTTagCompound)nbttaglist.tagAt(i)).getShort("lvl");
 
 					if (Enchantment.enchantmentsList[short1] != null)
 					{
 						calc += Enchantment.enchantmentsList[short1].calcModifierLiving(short2, enemy);
 					}
 				}
 			}
 		}
 		return calc > 0 ? 1 + rand.nextInt(calc) : 0;
 	}
 	public void setEnchantments(ItemStack ToEnchant, ItemStack Enchanted)
 	{
 		int EnchNum;
 		int EnchLv;
 		NBTTagList list = Enchanted.getEnchantmentTagList();
 		if(list !=null)
 		{
 			for (int i = 0; i < list.tagCount(); ++i)
 			{
 				if(((NBTTagCompound)list.tagAt(i)).getShort("lvl") > 0)
 				{
 					EnchNum = ((NBTTagCompound)list.tagAt(i)).getShort("id");
 					EnchLv = ((NBTTagCompound)list.tagAt(i)).getShort("lvl");
 					ToEnchant.addEnchantment(Enchantment.enchantmentsList[EnchNum], EnchLv);
 				}
 			}
 		}
 	}
 	// パケットの読み込み(パケットの受け取りはPacketHandlerで行う)
 	public void readPacketData(ByteArrayDataInput data)
 	{
 		try
 		{
 			this.SlotNum = data.readInt();
 			this.OpenKeydown = data.readBoolean();
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	// パケットの書き込み(パケットの生成自体はPacketHandlerで行う)
 	public void writePacketData(DataOutputStream dos)
 	{
 		try
 		{
 			dos.writeInt(SlotNum);
 			dos.writeBoolean(OpenKeydown);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 }
