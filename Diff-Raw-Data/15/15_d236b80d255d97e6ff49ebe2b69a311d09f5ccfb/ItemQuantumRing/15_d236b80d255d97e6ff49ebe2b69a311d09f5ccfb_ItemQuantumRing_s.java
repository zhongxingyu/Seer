 package makmods.levelstorage.item;
 
 import ic2.api.item.ElectricItem;
 import ic2.api.item.IElectricItem;
 import ic2.api.recipe.Recipes;
 
 import java.util.List;
 
 import makmods.levelstorage.LSBlockItemList;
 import makmods.levelstorage.LevelStorage;
 import makmods.levelstorage.armor.ArmorFunctions;
 import makmods.levelstorage.armor.ArmorFunctions.IForcefieldChestplate;
 import makmods.levelstorage.armor.ItemArmorLevitationBoots;
 import makmods.levelstorage.armor.ItemArmorTeslaHelmet;
 import makmods.levelstorage.lib.IC2Items;
 import makmods.levelstorage.proxy.ClientProxy;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.item.EnumRarity;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.living.LivingHurtEvent;
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class ItemQuantumRing extends Item implements IElectricItem,
         IForcefieldChestplate {
 
 	public static final String UNLOCALIZED_NAME = "itemQuantumRing";
 	public static final String NAME = "Quantum Ring";
 
 	public static final int TIER = 3;
 	public static final int STORAGE = 8 * 1000 * 1000;
 	// Energy per 1 damage
 	public static final int ENERGY_PER_DAMAGE = 100;
 
 	public ItemQuantumRing(int id) {
 		super(id);
 		this.setUnlocalizedName(UNLOCALIZED_NAME);
 		this.setMaxDamage(27);
 		this.setNoRepair();
 		if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
 			this.setCreativeTab(ClientProxy.getCreativeTab("IC2"));
 		}
 		this.setMaxStackSize(1);
 	}
 
 	@ForgeSubscribe
 	public void onDamage(LivingHurtEvent event) {
 		// if (event.source.isUnblockable() && event.source ==
 		// DamageSource.fall))
 		// return;
 		if (event.entityLiving instanceof EntityPlayer) {
 			EntityPlayer player = (EntityPlayer) event.entityLiving;
 			ItemStack ring = getRing(player);
 			if (ring != null) {
 				int energy = (int) (event.ammount * ENERGY_PER_DAMAGE);
 				if (ElectricItem.manager.canUse(ring, energy)) {
 					ElectricItem.manager.use(ring, energy, player);
 					event.setCanceled(true);
 				}
 			}
 		}
 	}
 
 	public static ItemStack getRing(EntityPlayer ep) {
 		InventoryPlayer inventory = ep.inventory;
 		for (ItemStack stack : inventory.mainInventory) {
 			if (stack != null)
 				if (stack.getItem() instanceof ItemQuantumRing)
 					return stack;
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
 		return 100000;
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister par1IconRegister) {
 		this.itemIcon = par1IconRegister
 		        .registerIcon(ClientProxy.QUANTUM_RING_TEXTURE);
 	}
 
 	@Override
 	public void addInformation(ItemStack par1ItemStack,
 	        EntityPlayer par2EntityPlayer, List par3List, boolean par4) {
 		par3List.add("\247bThe most endgame item. Probably.");
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public EnumRarity getRarity(ItemStack stack) {
 		return EnumRarity.epic;
 	}
 
 	public boolean chargeItem(ItemStack stack, ItemStack ring) {
 		if (stack == null)
 			return false;
 		if (!(stack.getItem() instanceof IElectricItem))
 			return false;
 		IElectricItem item = (IElectricItem) stack.getItem();
 		if (!ElectricItem.manager.canUse(ring, item.getTransferLimit(stack)))
 			return false;
 		int chargeSt = ElectricItem.manager.charge(stack,
 		        item.getTransferLimit(stack), 4, false, false);
 		int dischSt = ElectricItem.manager.discharge(ring, chargeSt, 4, true, false);
 		return chargeSt > 0 && dischSt > 0;
 	}
 
 	public void onUpdate(ItemStack itemStack, World world, Entity par3Entity,
 	        int par4, boolean par5) {
 		if (!(par3Entity instanceof EntityPlayer))
 			return;
 		EntityPlayer player = (EntityPlayer) par3Entity;
 		InventoryPlayer inv = player.inventory;
 		boolean didSomething = false;
 		for (ItemStack device : inv.armorInventory)
			if (chargeItem(device, itemStack))
 				didSomething = true;
 		if (!didSomething) {
 			for (ItemStack device : inv.mainInventory)
				if (chargeItem(device, itemStack))
 					didSomething = true;
 		}
 		ArmorFunctions.extinguish(player, world);
 		ArmorFunctions.fly(ItemArmorLevitationBoots.FLYING_ENERGY_PER_TICK,
 		        player, itemStack, world);
 		ArmorFunctions.helmetFunctions(world, player, itemStack,
 		        ItemArmorTeslaHelmet.RAY_COST,
 		        ItemArmorTeslaHelmet.ENTITY_HIT_COST,
 		        ItemArmorTeslaHelmet.FOOD_COST);
 		ArmorFunctions.jumpBooster(world, player, itemStack);
 		ArmorFunctions.speedUp(player, itemStack);
 	}
 
 	public static void addCraftingRecipe() {
 		if (LevelStorage.recipesHardmode) {
 			Recipes.advRecipes.addRecipe(new ItemStack(
 			        LSBlockItemList.itemQuantumRing), "hic", "iei", "bil",
 			        Character.valueOf('i'), SimpleItems.instance
 			                .getIngredient(3), Character.valueOf('e'),
 			        new ItemStack(LSBlockItemList.itemStorageFourMillion),
 			        Character.valueOf('h'), new ItemStack(
 			                LSBlockItemList.itemArmorTeslaHelmet), Character
 			                .valueOf('c'), new ItemStack(
 			                LSBlockItemList.itemArmorForcefieldChestplate),
 			        Character.valueOf('b'), new ItemStack(
 			                LSBlockItemList.itemLevitationBoots), Character
 			                .valueOf('l'), new ItemStack(
 			                LSBlockItemList.itemSupersonicLeggings));
 		} else {
 			Recipes.advRecipes.addRecipe(new ItemStack(
 			        LSBlockItemList.itemQuantumRing), "hic", "iei", "bil",
 			        Character.valueOf('i'), IC2Items.IRIDIUM_PLATE.copy(),
 			        Character.valueOf('e'), new ItemStack(
 			                LSBlockItemList.itemStorageFourMillion), Character
 			                .valueOf('h'), new ItemStack(
 			                LSBlockItemList.itemArmorTeslaHelmet), Character
 			                .valueOf('c'), new ItemStack(
 			                LSBlockItemList.itemArmorForcefieldChestplate),
 			        Character.valueOf('b'), new ItemStack(
 			                LSBlockItemList.itemLevitationBoots), Character
 			                .valueOf('l'), new ItemStack(
 			                LSBlockItemList.itemSupersonicLeggings));
 		}
 
 	}
 
 	@Override
 	public void getSubItems(int par1, CreativeTabs par2CreativeTabs,
 	        List par3List) {
 		ItemStack var4 = new ItemStack(this, 1);
 		ElectricItem.manager.charge(var4, Integer.MAX_VALUE, Integer.MAX_VALUE,
 		        true, false);
 		par3List.add(var4);
 		par3List.add(new ItemStack(this, 1, this.getMaxDamage()));
 	}
 
 	@Override
 	public int energyPerTick() {
 		return 512;
 	}
 
 	@Override
 	public WearType getWearType() {
 		return WearType.INVENTORY;
 	}
 }
