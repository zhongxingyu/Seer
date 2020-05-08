 package makmods.levelstorage.item;
 
 import ic2.api.item.ElectricItem;
 import ic2.api.item.IElectricItem;
 import ic2.api.item.Items;
 import ic2.api.recipe.Recipes;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import makmods.levelstorage.LSBlockItemList;
 import makmods.levelstorage.LevelStorage;
 import makmods.levelstorage.lib.Reference;
 import makmods.levelstorage.logic.DrillEnhancementRecipe;
 import makmods.levelstorage.logic.IC2Access;
 import makmods.levelstorage.logic.util.AdvBlockFinder;
 import makmods.levelstorage.logic.util.BlockLocation;
 import makmods.levelstorage.logic.util.NBTHelper;
 import makmods.levelstorage.logic.util.NBTHelper.Cooldownable;
 import makmods.levelstorage.logic.util.OreDictHelper;
 import makmods.levelstorage.proxy.ClientProxy;
 import net.minecraft.block.Block;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.enchantment.Enchantment;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.EnumRarity;
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemPickaxe;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.CraftingManager;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.util.EnumMovingObjectType;
 import net.minecraft.util.Icon;
 import net.minecraft.util.MovingObjectPosition;
 import net.minecraft.world.World;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.ForgeDirection;
 import net.minecraftforge.common.ForgeHooks;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.common.Property;
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class ItemEnhancedDiamondDrill extends ItemPickaxe implements
         IElectricItem {
 
 	public static float SPEED = 32.0F;
 	public static final int TIER = 2;
 	public static final int STORAGE = 100000;
 	public static final int ENERGY_PER_USE = 200;
 	// Prank..
 	public static final String ENHANCEMENT_NBT = "enhancement";
 	public static final String ENHANCEMENT_ID_NBT = "id";
 	public static final String ENHANCEMENT_LVL_NBT = "level";
 	public static final String MODE_NBT = "mode";
 
 	public static final Integer COOLDOWN = 20;
 
 	public Icon iconPass1;
 	public Icon iconPass2;
 
 	public ItemEnhancedDiamondDrill(int id) {
 		super(id, EnumToolMaterial.EMERALD);
 
 		this.setMaxDamage(27);
 		this.setNoRepair();
 		if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
 			this.setCreativeTab(ClientProxy.getCreativeTab("IC2"));
 		}
 		this.setMaxStackSize(1);
 		MinecraftForge.setToolClass(this, "pickaxe", 3);
 		MinecraftForge.setToolClass(this, "shovel", 2);
 		this.damageVsEntity = 2.0f;
 		// MinecraftForge.setToolClass(this, "shovel", 3);
 		this.efficiencyOnProperMaterial = SPEED;
 	}
 
 	static {
 		Property p = LevelStorage.configuration
 		        .get(Configuration.CATEGORY_GENERAL,
 		                "enhancedDiamondDrillSpeed", 32);
 		p.comment = "Speed of enhanced diamond drill (diamond drill = 16, default = 32)";
 		SPEED = p.getInt(32);
 	}
 
 	public static enum Mode {
 		TUNNEL("3x3x1 (width x height x depth)"), NORMAL("normal (1x)");
 
 		public String name;
 
 		Mode(String par1Str) {
 			this.name = par1Str;
 		}
 	}
 
 	public static void invertMode(ItemStack stack, EntityPlayer player) {
 		try {
 			if (!NBTHelper.verifyKey(stack, MODE_NBT)) {
 				NBTHelper.setInteger(stack, MODE_NBT, Mode.TUNNEL.ordinal());
 				return;
 			}
 			Mode mode = Mode.values()[NBTHelper.getInteger(stack, MODE_NBT)];
 			switch (mode) {
 				case TUNNEL: {
 					NBTHelper
 					        .setInteger(stack, MODE_NBT, Mode.NORMAL.ordinal());
 					break;
 				}
 				case NORMAL: {
 					NBTHelper
 					        .setInteger(stack, MODE_NBT, Mode.TUNNEL.ordinal());
 					break;
 				}
 			}
 			LevelStorage.proxy.messagePlayer(
 			        player,
 			        "Drill mode: "
 			                + Mode.values()[NBTHelper.getInteger(stack,
 			                        MODE_NBT)].name, new Object[0]);
 		} catch (Exception e) {
 			System.out.println(Reference.MOD_NAME
 			        + ": something went wrong when tried to set drill's mode.");
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Drill's onUpdate method. Used only for cooldown
 	 */
 	public void onUpdate(ItemStack par1ItemStack, World par2World,
 	        Entity par3Entity, int par4, boolean par5) {
 		if (!par2World.isRemote) {
 			if (!NBTHelper.verifyKey(par1ItemStack, MODE_NBT))
 				NBTHelper.setInteger(par1ItemStack, MODE_NBT,
 				        Mode.NORMAL.ordinal());
 			Cooldownable.onUpdate(par1ItemStack, COOLDOWN);
 		}
 	}
 
 	/**
 	 * Drill's onItemRightClick method. Used only for mode change.
 	 */
 	public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World,
 	        EntityPlayer par3EntityPlayer) {
 		if (!par2World.isRemote) {
 			if (IC2Access.instance.isKeyDown("ModeSwitch", par3EntityPlayer)) {
 				if (!Cooldownable.use(par1ItemStack, COOLDOWN))
 					return par1ItemStack;
 				// Unneeded really, but i'll keep it here
 				NBTHelper.checkNBT(par1ItemStack);
 				invertMode(par1ItemStack, par3EntityPlayer);
 			}
 		}
 		return par1ItemStack;
 	}
 
 	public boolean hitEntity(ItemStack par1ItemStack,
 	        EntityLivingBase par2EntityLivingBase,
 	        EntityLivingBase par3EntityLivingBase) {
 		return false;
 	}
 
 	public static void addCraftingRecipe() {
 
 		Recipes.advRecipes.addRecipe(new ItemStack(
 		        LSBlockItemList.itemEnhDiamondDrill), "cdc", "did", "aea",
 		        Character.valueOf('c'), Items.getItem("carbonPlate"), Character
 		                .valueOf('e'), Items.getItem("energyCrystal"),
 		        Character.valueOf('i'), Items.getItem("diamondDrill"),
 		        Character.valueOf('a'), Items.getItem("advancedCircuit"),
 		        Character.valueOf('d'), new ItemStack(Item.diamond));
 		CraftingManager.getInstance().getRecipeList()
 		        .add(new DrillEnhancementRecipe());
 
 	}
 
 	public static ArrayList<Block> mineableBlocks = new ArrayList<Block>();
 
 	static {
 		mineableBlocks.add(Block.cobblestone);
 		mineableBlocks.add(Block.stoneSingleSlab);
 		mineableBlocks.add(Block.stoneDoubleSlab);
 		mineableBlocks.add(Block.stairsCobblestone);
 		mineableBlocks.add(Block.stone);
 		mineableBlocks.add(Block.sandStone);
 		mineableBlocks.add(Block.stairsSandStone);
 		mineableBlocks.add(Block.cobblestoneMossy);
 		mineableBlocks.add(Block.oreIron);
 		mineableBlocks.add(Block.blockIron);
 		mineableBlocks.add(Block.oreCoal);
 		mineableBlocks.add(Block.blockGold);
 		mineableBlocks.add(Block.oreGold);
 		mineableBlocks.add(Block.oreDiamond);
 		mineableBlocks.add(Block.blockDiamond);
 		mineableBlocks.add(Block.ice);
 		mineableBlocks.add(Block.netherrack);
 		mineableBlocks.add(Block.oreLapis);
 		mineableBlocks.add(Block.blockLapis);
 		mineableBlocks.add(Block.oreRedstone);
 		mineableBlocks.add(Block.oreRedstoneGlowing);
 		mineableBlocks.add(Block.brick);
 		mineableBlocks.add(Block.stairsBrick);
 		mineableBlocks.add(Block.glowStone);
 		mineableBlocks.add(Block.grass);
 		mineableBlocks.add(Block.dirt);
 		mineableBlocks.add(Block.mycelium);
 		mineableBlocks.add(Block.sand);
 		mineableBlocks.add(Block.gravel);
 		mineableBlocks.add(Block.snow);
 		mineableBlocks.add(Block.blockSnow);
 		mineableBlocks.add(Block.blockClay);
 		mineableBlocks.add(Block.tilledField);
 		mineableBlocks.add(Block.stoneBrick);
 		mineableBlocks.add(Block.stairsStoneBrick);
 		mineableBlocks.add(Block.netherBrick);
 		mineableBlocks.add(Block.stairsNetherBrick);
 		mineableBlocks.add(Block.slowSand);
 		mineableBlocks.add(Block.anvil);
 		mineableBlocks.add(Block.oreNetherQuartz);
 	}
 
 	@SideOnly(Side.CLIENT)
 	@Override
 	public EnumRarity getRarity(ItemStack is) {
 		return EnumRarity.rare;
 	}
 
 	public void addInformation(ItemStack par1ItemStack,
 	        EntityPlayer par2EntityPlayer, List par3List, boolean par4) {
 		par3List.add("\2472Mines a whole ore vein");
 		par3List.add("\2472if you break one block of it");
 		par3List.add("Enhancement: ");
 		if (par1ItemStack.stackTagCompound == null)
 			par1ItemStack.stackTagCompound = new NBTTagCompound();
 		NBTTagCompound enh = par1ItemStack.stackTagCompound
 		        .getCompoundTag(ENHANCEMENT_NBT);
 		if (!enh.hasKey(ENHANCEMENT_ID_NBT))
 			par3List.add("\247cNone.");
 		else {
 			int id = enh.getInteger(ENHANCEMENT_ID_NBT);
 			int lvl = enh.getInteger(ENHANCEMENT_LVL_NBT);
 			if (id > 0 && lvl > 0) {
 				Enchantment ench = Enchantment.enchantmentsList[id];
 				par3List.add(ench.getTranslatedName(lvl));
 			}
 		}
 	}
 
 	@SideOnly(Side.CLIENT)
 	public boolean requiresMultipleRenderPasses() {
 		return true;
 	}
 
 	public Icon getIconFromDamageForRenderPass(int par1, int par2) {
 		switch (par2) {
 			case 0:
 				return iconPass1;
 			case 1:
 				return iconPass2;
 		}
 		return null;
 	}
 
 	@Override
 	public boolean canProvideEnergy(ItemStack itemStack) {
 		return false;
 	}
 
 	@Override
 	public int getChargedItemId(ItemStack itemStack) {
 		return this.itemID;
 	}
 
 	@Override
 	public int getEmptyItemId(ItemStack itemStack) {
 		return this.itemID;
 	}
 
 	@Override
 	public int getMaxCharge(ItemStack itemStack) {
 		return STORAGE;
 	}
 
 	@Override
 	public int getTier(ItemStack itemStack) {
 		return TIER;
 	}
 
 	@Override
 	public int getTransferLimit(ItemStack itemStack) {
 		return 200;
 	}
 
 	@Override
 	public void getSubItems(int par1, CreativeTabs par2CreativeTabs,
 	        List par3List) {
 		ItemStack var4 = new ItemStack(this, 1);
 		ElectricItem.manager.charge(var4, Integer.MAX_VALUE, Integer.MAX_VALUE,
 		        true, false);
 		par3List.add(var4);
 		par3List.add(new ItemStack(this, 1, this.getMaxDamage()));
 
 		int[][] enhancements = { { Enchantment.fortune.effectId, 3 },
 		        { Enchantment.silkTouch.effectId, 1 } };
 		for (int[] enh : enhancements) {
 
 			NBTTagCompound nbtEnh = new NBTTagCompound();
 			nbtEnh.setInteger(ENHANCEMENT_ID_NBT, enh[0]);
 			nbtEnh.setInteger(ENHANCEMENT_LVL_NBT, enh[1]);
 
 			ItemStack charged = new ItemStack(this, 1);
 			ElectricItem.manager.charge(charged, Integer.MAX_VALUE,
 			        Integer.MAX_VALUE, true, false);
 			charged.stackTagCompound.setCompoundTag(ENHANCEMENT_NBT, nbtEnh);
 			par3List.add(charged);
 
 			ItemStack discharged = new ItemStack(this, 1, this.getMaxDamage());
 			discharged.stackTagCompound = new NBTTagCompound();
 			discharged.stackTagCompound.setInteger("charge", 0);
 			discharged.stackTagCompound.setCompoundTag(ENHANCEMENT_NBT, nbtEnh);
 			par3List.add(discharged);
 		}
 	}
 
 	public int getItemEnchantability() {
 		return 0;
 	}
 
 	public static ArrayList<Integer> blocksOtherThanOres = new ArrayList<Integer>();
 
 	static {
 		blocksOtherThanOres.add(Block.gravel.blockID);
 		blocksOtherThanOres.add(Block.oreRedstone.blockID);
 		blocksOtherThanOres.add(Block.oreRedstoneGlowing.blockID);
 		blocksOtherThanOres.add(Block.glowStone.blockID);
		blocksOtherThanOres.add(Block.blockClay.blockID);
 	}
 
 	@Override
 	public boolean onBlockDestroyed(ItemStack par1ItemStack, World par2World,
 	        int par3, int par4, int par5, int par6,
 	        EntityLivingBase par7EntityLivingBase) {
 		// if ()
 		if (!par2World.isRemote) {
 			int fortune = 0;
 			boolean silktouch = false;
 			NBTTagCompound enh = par1ItemStack.stackTagCompound
 			        .getCompoundTag(ENHANCEMENT_NBT);
 			if (enh.hasKey(ENHANCEMENT_ID_NBT)) {
 				int encid = enh.getInteger(ENHANCEMENT_ID_NBT);
 				int lvl = enh.getInteger(ENHANCEMENT_LVL_NBT);
 				if (encid > 0 && lvl > 0) {
 					if (Enchantment.enchantmentsList[encid] == Enchantment.fortune)
 						fortune = lvl;
 					if (Enchantment.enchantmentsList[encid] == Enchantment.silkTouch)
 						silktouch = true;
 				}
 			}
 			if (ElectricItem.manager.canUse(par1ItemStack, ENERGY_PER_USE)) {
 				ElectricItem.manager.use(par1ItemStack, ENERGY_PER_USE,
 				        par7EntityLivingBase);
 			}
 			Block bl = Block.blocksList[par3];
 			/*
 			 * if (OreDictHelper.getOreName(new ItemStack(bl)).startsWith("ore")
 			 * || blocksOtherThanOres.contains(bl.blockID)) { OreFinder finder =
 			 * new OreFinder(); finder.aimBlockId = bl.blockID;
 			 * finder.aimBlockMeta = par2World.getBlockMetadata(par4, par5,
 			 * par6); finder.world = par2World; finder.calibrate(par4, par5,
 			 * par6); for (BlockLocation oreCh : finder.foundOre) { int blockId
 			 * = par2World.getBlockId(oreCh.getX(), oreCh.getY(), oreCh.getZ());
 			 * 
 			 * int blockMeta = par2World.getBlockMetadata(oreCh.getX(),
 			 * oreCh.getY(), oreCh.getZ()); Block b = Block.blocksList[blockId];
 			 * if (b != null) { if (par7EntityLivingBase instanceof
 			 * EntityPlayer) { if (b.getBlockHardness(par2World, oreCh.getX(),
 			 * oreCh.getY(), oreCh.getZ()) != -1.0F) { if
 			 * (b.removeBlockByPlayer(par2World, (EntityPlayer)
 			 * par7EntityLivingBase, oreCh.getX(), oreCh.getY(), oreCh.getZ()))
 			 * {
 			 * 
 			 * if (!silktouch) { b.dropBlockAsItem(par2World, oreCh.getX(),
 			 * oreCh.getY(), oreCh.getZ(), finder.aimBlockMeta, fortune); } else
 			 * { if (b.canSilkHarvest( par2World, (EntityPlayer)
 			 * par7EntityLivingBase, oreCh.getX(), oreCh.getY(), oreCh.getZ(),
 			 * finder.aimBlockMeta)) { ItemStack itemstack = new ItemStack( b,
 			 * 1, finder.aimBlockMeta); if (itemstack != null) {
 			 * this.dropBlockAsItem_do( par2World, oreCh.getX(), oreCh.getY(),
 			 * oreCh.getZ(), itemstack); } } else { b.dropBlockAsItem(par2World,
 			 * oreCh.getX(), oreCh.getY(), oreCh.getZ(), finder.aimBlockMeta,
 			 * fortune); } } } if (ElectricItem.manager.canUse(par1ItemStack,
 			 * ENERGY_PER_USE)) { ElectricItem.manager.use(par1ItemStack,
 			 * ENERGY_PER_USE, par7EntityLivingBase); } else { break; } } } } }
 			 * }
 			 */
 			String name = OreDictHelper.getOreName(new ItemStack(bl));
 			int metadata = par2World.getBlockMetadata(par4, par5, par6);
 			if (name.startsWith("ore")) {
 				AdvBlockFinder finder = new AdvBlockFinder(par2World, par4,
 				        par5, par6, name);
 				for (BlockLocation oreCh : finder.getBlocksFound()) {
 					int blockId = par2World.getBlockId(oreCh.getX(),
 					        oreCh.getY(), oreCh.getZ());
 
 					int blockMeta = par2World.getBlockMetadata(oreCh.getX(),
 					        oreCh.getY(), oreCh.getZ());
 					Block b = Block.blocksList[blockId];
 					if (b != null) {
 						if (par7EntityLivingBase instanceof EntityPlayer) {
 							if (b.getBlockHardness(par2World, oreCh.getX(),
 							        oreCh.getY(), oreCh.getZ()) != -1.0F) {
 								if (b.removeBlockByPlayer(par2World,
 								        (EntityPlayer) par7EntityLivingBase,
 								        oreCh.getX(), oreCh.getY(),
 								        oreCh.getZ())) {
 
 									if (!silktouch) {
 										b.dropBlockAsItem(par2World,
 										        oreCh.getX(), oreCh.getY(),
 										        oreCh.getZ(), metadata, fortune);
 									} else {
 										if (b.canSilkHarvest(
 										        par2World,
 										        (EntityPlayer) par7EntityLivingBase,
 										        oreCh.getX(), oreCh.getY(),
 										        oreCh.getZ(), metadata)) {
 											ItemStack itemstack = new ItemStack(
 											        b, 1, metadata);
 											if (itemstack != null) {
 												this.dropBlockAsItem_do(
 												        par2World,
 												        oreCh.getX(),
 												        oreCh.getY(),
 												        oreCh.getZ(), itemstack);
 											}
 										} else {
 											b.dropBlockAsItem(par2World,
 											        oreCh.getX(), oreCh.getY(),
 											        oreCh.getZ(), metadata,
 											        fortune);
 										}
 									}
 								}
 								if (ElectricItem.manager.canUse(par1ItemStack,
 								        ENERGY_PER_USE)) {
 									ElectricItem.manager.use(par1ItemStack,
 									        ENERGY_PER_USE,
 									        par7EntityLivingBase);
 								} else {
 									break;
 								}
 							}
 						}
 					}
 				}
 			} else {
 				if (NBTHelper.getInteger(par1ItemStack, MODE_NBT) == Mode.TUNNEL
 				        .ordinal()) {
 					if (par7EntityLivingBase instanceof EntityPlayer) {
 						EntityPlayer player = (EntityPlayer) par7EntityLivingBase;
 						if (!player.isSneaking()) {
 							MovingObjectPosition pos = this
 							        .getMovingObjectPositionFromPlayer(
 							                par2World, player, true);
 							if (pos != null)
 								if (pos.typeOfHit == EnumMovingObjectType.TILE) {
 									ForgeDirection hitFrom = ForgeDirection.VALID_DIRECTIONS[pos.sideHit];
 									BlockLocation currBlock = new BlockLocation(
 									        par2World.provider.dimensionId,
 									        par4, par5, par6);
 									BlockLocation[] removeBlocks = new BlockLocation[12];
 									switch (hitFrom) {
 										case DOWN: {
 											removeBlocks[0] = currBlock.move(
 											        ForgeDirection.NORTH, 1);
 											removeBlocks[1] = currBlock.move(
 											        ForgeDirection.WEST, 1);
 											removeBlocks[2] = currBlock.move(
 											        ForgeDirection.SOUTH, 1);
 											removeBlocks[3] = currBlock.move(
 											        ForgeDirection.EAST, 1);
 
 											removeBlocks[4] = currBlock.move(
 											        ForgeDirection.NORTH, 1)
 											        .move(ForgeDirection.EAST,
 											                1);
 											removeBlocks[5] = currBlock.move(
 											        ForgeDirection.WEST, 1)
 											        .move(ForgeDirection.NORTH,
 											                1);
 											removeBlocks[6] = currBlock.move(
 											        ForgeDirection.NORTH, 1)
 											        .move(ForgeDirection.WEST,
 											                1);
 											removeBlocks[7] = currBlock.move(
 											        ForgeDirection.WEST, 1)
 											        .move(ForgeDirection.SOUTH,
 											                1);
 											removeBlocks[8] = currBlock.move(
 											        ForgeDirection.SOUTH, 1)
 											        .move(ForgeDirection.EAST,
 											                1);
 											break;
 										}
 										case UP: {
 											removeBlocks[0] = currBlock.move(
 											        ForgeDirection.NORTH, 1);
 											removeBlocks[1] = currBlock.move(
 											        ForgeDirection.WEST, 1);
 											removeBlocks[2] = currBlock.move(
 											        ForgeDirection.SOUTH, 1);
 											removeBlocks[3] = currBlock.move(
 											        ForgeDirection.EAST, 1);
 
 											removeBlocks[4] = currBlock.move(
 											        ForgeDirection.NORTH, 1)
 											        .move(ForgeDirection.EAST,
 											                1);
 											removeBlocks[5] = currBlock.move(
 											        ForgeDirection.WEST, 1)
 											        .move(ForgeDirection.NORTH,
 											                1);
 											removeBlocks[6] = currBlock.move(
 											        ForgeDirection.NORTH, 1)
 											        .move(ForgeDirection.WEST,
 											                1);
 											removeBlocks[7] = currBlock.move(
 											        ForgeDirection.WEST, 1)
 											        .move(ForgeDirection.SOUTH,
 											                1);
 											removeBlocks[8] = currBlock.move(
 											        ForgeDirection.SOUTH, 1)
 											        .move(ForgeDirection.EAST,
 											                1);
 											break;
 										}
 										case NORTH: {
 											// Up & down
 											removeBlocks[0] = currBlock.move(
 											        ForgeDirection.UP, 1);
 											removeBlocks[1] = currBlock.move(
 											        ForgeDirection.DOWN, 1);
 											// West & east
 											removeBlocks[2] = currBlock.move(
 											        ForgeDirection.WEST, 1);
 											removeBlocks[3] = currBlock.move(
 											        ForgeDirection.EAST, 1);
 											// Up-west & Down-west
 											removeBlocks[4] = currBlock.move(
 											        ForgeDirection.DOWN, 1)
 											        .move(ForgeDirection.WEST,
 											                1);
 											removeBlocks[5] = currBlock.move(
 											        ForgeDirection.UP, 1).move(
 											        ForgeDirection.EAST, 1);
 
 											removeBlocks[6] = currBlock.move(
 											        ForgeDirection.UP, 1).move(
 											        ForgeDirection.WEST, 1);
 											removeBlocks[7] = currBlock.move(
 											        ForgeDirection.DOWN, 1)
 											        .move(ForgeDirection.EAST,
 											                1);
 
 											removeBlocks[8] = currBlock.move(
 											        ForgeDirection.UP, 1).move(
 											        ForgeDirection.EAST, 1);
 											removeBlocks[9] = currBlock.move(
 											        ForgeDirection.DOWN, 1)
 											        .move(ForgeDirection.WEST,
 											                1);
 
 											break;
 											// South up & north down
 										}
 										case WEST: {
 											removeBlocks[0] = currBlock.move(
 											        ForgeDirection.UP, 1);
 											removeBlocks[1] = currBlock.move(
 											        ForgeDirection.DOWN, 1);
 											// West & east
 											removeBlocks[2] = currBlock.move(
 											        ForgeDirection.NORTH, 1);
 											removeBlocks[3] = currBlock.move(
 											        ForgeDirection.SOUTH, 1);
 
 											removeBlocks[4] = currBlock.move(
 											        ForgeDirection.UP, 1).move(
 											        ForgeDirection.NORTH, 1);
 											removeBlocks[5] = currBlock.move(
 											        ForgeDirection.DOWN, 1)
 											        .move(ForgeDirection.SOUTH,
 											                1);
 
 											removeBlocks[6] = currBlock.move(
 											        ForgeDirection.UP, 1).move(
 											        ForgeDirection.NORTH, 1);
 											removeBlocks[7] = currBlock.move(
 											        ForgeDirection.DOWN, 1)
 											        .move(ForgeDirection.SOUTH,
 											                1);
 											removeBlocks[10] = currBlock.move(
 											        ForgeDirection.SOUTH, 1)
 											        .move(ForgeDirection.UP, 1);
 											removeBlocks[11] = currBlock.move(
 											        ForgeDirection.NORTH, 1)
 											        .move(ForgeDirection.DOWN,
 											                1);
 											break;
 										}
 										case EAST: {
 											removeBlocks[0] = currBlock.move(
 											        ForgeDirection.UP, 1);
 											removeBlocks[1] = currBlock.move(
 											        ForgeDirection.DOWN, 1);
 											// West & east
 											removeBlocks[2] = currBlock.move(
 											        ForgeDirection.NORTH, 1);
 											removeBlocks[3] = currBlock.move(
 											        ForgeDirection.SOUTH, 1);
 
 											removeBlocks[4] = currBlock.move(
 											        ForgeDirection.UP, 1).move(
 											        ForgeDirection.NORTH, 1);
 											removeBlocks[5] = currBlock.move(
 											        ForgeDirection.DOWN, 1)
 											        .move(ForgeDirection.SOUTH,
 											                1);
 
 											removeBlocks[6] = currBlock.move(
 											        ForgeDirection.UP, 1).move(
 											        ForgeDirection.NORTH, 1);
 											removeBlocks[7] = currBlock.move(
 											        ForgeDirection.DOWN, 1)
 											        .move(ForgeDirection.SOUTH,
 											                1);
 											removeBlocks[10] = currBlock.move(
 											        ForgeDirection.SOUTH, 1)
 											        .move(ForgeDirection.UP, 1);
 											removeBlocks[11] = currBlock.move(
 											        ForgeDirection.NORTH, 1)
 											        .move(ForgeDirection.DOWN,
 											                1);
 											break;
 										}
 										case SOUTH: {
 											removeBlocks[0] = currBlock.move(
 											        ForgeDirection.UP, 1);
 											removeBlocks[1] = currBlock.move(
 											        ForgeDirection.DOWN, 1);
 											// West & east
 											removeBlocks[2] = currBlock.move(
 											        ForgeDirection.WEST, 1);
 											removeBlocks[3] = currBlock.move(
 											        ForgeDirection.EAST, 1);
 											// Up-west & Down-west
 											removeBlocks[4] = currBlock.move(
 											        ForgeDirection.DOWN, 1)
 											        .move(ForgeDirection.WEST,
 											                1);
 											removeBlocks[5] = currBlock.move(
 											        ForgeDirection.UP, 1).move(
 											        ForgeDirection.EAST, 1);
 
 											removeBlocks[6] = currBlock.move(
 											        ForgeDirection.UP, 1).move(
 											        ForgeDirection.WEST, 1);
 											removeBlocks[7] = currBlock.move(
 											        ForgeDirection.DOWN, 1)
 											        .move(ForgeDirection.EAST,
 											                1);
 
 											removeBlocks[8] = currBlock.move(
 											        ForgeDirection.UP, 1).move(
 											        ForgeDirection.EAST, 1);
 											removeBlocks[9] = currBlock.move(
 											        ForgeDirection.DOWN, 1)
 											        .move(ForgeDirection.WEST,
 											                1);
 											break;
 										}
 										case UNKNOWN:
 											break;
 									}
 									// Stuff destroying part.. messy code, but
 									// it
 									// works.
 									for (BlockLocation blockLoc : removeBlocks) {
 										if (blockLoc != null) {
 											Block b = Block.blocksList[par2World
 											        .getBlockId(
 											                blockLoc.getX(),
 											                blockLoc.getY(),
 											                blockLoc.getZ())];
 											int aimBlockMeta = par2World
 											        .getBlockMetadata(
 											                blockLoc.getX(),
 											                blockLoc.getY(),
 											                blockLoc.getZ());
 											if (b != null) {
 												if (b.getBlockHardness(
 												        par2World,
 												        blockLoc.getX(),
 												        blockLoc.getY(),
 												        blockLoc.getZ()) != -1.0F) {
 													if (b.removeBlockByPlayer(
 													        par2World,
 													        (EntityPlayer) par7EntityLivingBase,
 													        blockLoc.getX(),
 													        blockLoc.getY(),
 													        blockLoc.getZ())) {
 
 														if (!silktouch) {
 															b.dropBlockAsItem(
 															        par2World,
 															        blockLoc.getX(),
 															        blockLoc.getY(),
 															        blockLoc.getZ(),
 															        aimBlockMeta,
 															        fortune);
 														} else {
 															if (b.canSilkHarvest(
 															        par2World,
 															        (EntityPlayer) par7EntityLivingBase,
 															        blockLoc.getX(),
 															        blockLoc.getY(),
 															        blockLoc.getZ(),
 															        aimBlockMeta)) {
 																ItemStack itemstack = new ItemStack(
 																        b, 1,
 																        aimBlockMeta);
 																if (itemstack != null) {
 																	this.dropBlockAsItem_do(
 																	        par2World,
 																	        blockLoc.getX(),
 																	        blockLoc.getY(),
 																	        blockLoc.getZ(),
 																	        itemstack);
 																}
 															} else {
 																b.dropBlockAsItem(
 																        par2World,
 																        blockLoc.getX(),
 																        blockLoc.getY(),
 																        blockLoc.getZ(),
 																        aimBlockMeta,
 																        fortune);
 															}
 														}
 													}
 													if (ElectricItem.manager
 													        .canUse(par1ItemStack,
 													                ENERGY_PER_USE)) {
 														ElectricItem.manager
 														        .use(par1ItemStack,
 														                ENERGY_PER_USE,
 														                par7EntityLivingBase);
 													} else {
 														break;
 													}
 												}
 											}
 										}
 										// End of messy part
 									}
 								}
 						}
 					}
 				}
 			}
 		}
 		return true;
 	}
 
 	// From Block
 	public void dropBlockAsItem_do(World par1World, int par2, int par3,
 	        int par4, ItemStack par5ItemStack) {
 		if (!par1World.isRemote
 		        && par1World.getGameRules().getGameRuleBooleanValue(
 		                "doTileDrops")) {
 			float f = 0.7F;
 			double d0 = (double) (par1World.rand.nextFloat() * f)
 			        + (double) (1.0F - f) * 0.5D;
 			double d1 = (double) (par1World.rand.nextFloat() * f)
 			        + (double) (1.0F - f) * 0.5D;
 			double d2 = (double) (par1World.rand.nextFloat() * f)
 			        + (double) (1.0F - f) * 0.5D;
 			EntityItem entityitem = new EntityItem(par1World, (double) par2
 			        + d0, (double) par3 + d1, (double) par4 + d2, par5ItemStack);
 			entityitem.delayBeforeCanPickup = 10;
 			par1World.spawnEntityInWorld(entityitem);
 		}
 	}
 
 	public float getStrVsBlock(ItemStack itemstack, Block block, int md) {
 		if (!ElectricItem.manager.canUse(itemstack, ENERGY_PER_USE)) {
 			return 1.0F;
 		}
 		if (ForgeHooks.isToolEffective(itemstack, block, 0)) {
 			return this.efficiencyOnProperMaterial;
 		}
 		if (mineableBlocks.contains(block)) {
 			return this.efficiencyOnProperMaterial;
 		}
 		if (canHarvestBlock(block)) {
 			return this.efficiencyOnProperMaterial;
 		}
 
 		return 1.0F;
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister par1IconRegister) {
 		this.iconPass1 = par1IconRegister
 		        .registerIcon(ClientProxy.ITEM_ENHANCED_DIAMOND_DRILL_PASS_ONE);
 		this.iconPass2 = par1IconRegister
 		        .registerIcon(ClientProxy.ITEM_ENHANCED_DIAMOND_DRILL_PASS_TWO);
 	}
 
 }
