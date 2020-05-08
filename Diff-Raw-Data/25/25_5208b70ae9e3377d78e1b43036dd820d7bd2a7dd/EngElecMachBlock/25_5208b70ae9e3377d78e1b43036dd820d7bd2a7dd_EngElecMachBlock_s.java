 package gaarnik.bsa.common.block;
 
 import gaarnik.bsa.common.BSAGuiHandler;
 import gaarnik.bsa.common.BSAMod;
 import gaarnik.bsa.common.item.BSAItems;
 import gaarnik.bsa.common.tileentity.EngElecMachTileEntity;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class EngElecMachBlock extends BSAMachineBlock {
 	// *******************************************************************
 	private static final String GAME_NAME = 			"Eng. Electrical Machine";
 
 	private static final String TEXTURE_FRONT = 		"eng_elec_machine_front";
 	private static final String TEXTURE_FRONT_ACTIVE = 	"eng_elec_machine_front_active";
 	private static final String TEXTURE_SIDE = 			"eng_machine_side";
 	private static final String TEXTURE_OTHER = 		"eng_machine_other";
 
 	// *******************************************************************
 	@SideOnly(Side.CLIENT)
 	private Icon frontIcon;
 	@SideOnly(Side.CLIENT)
 	private Icon activeFrontIcon;
 	@SideOnly(Side.CLIENT)
 	private Icon sideIcon;
 	@SideOnly(Side.CLIENT)
 	private Icon otherIcon;
 
 	// *******************************************************************
 	public EngElecMachBlock(int id, boolean active) {
 		super(id, "EngElecMachBlock");
 
 		this.active = active;
 
 		if(active == false) {
 			GameRegistry.registerBlock(this, "engElecMachBlock");
 			LanguageRegistry.addName(this, GAME_NAME);
 
 			ItemStack screwStack = new ItemStack(BSAItems.screwItem, 1);
 			ItemStack ironStack = new ItemStack(Item.ingotIron, 1);
 			ItemStack redstoneStack = new ItemStack(Item.redstone, 1);
 			ItemStack circuitStack = new ItemStack(BSAItems.engCircuitItem, 1);
 
 			GameRegistry.addRecipe(new ItemStack(this, 1), new Object[] {
 				"AXA", "XYX", "XZX", 'X', ironStack, 'A', screwStack, 'Y', redstoneStack, 'Z', circuitStack
 			});
 		}
 	}
 
 	// *******************************************************************
 	@Override
 	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
 		if(player.isSneaking())
 			return false;
 		
 		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
 
 		if (tileEntity == null)
 			return false;
 
 		player.openGui(BSAMod.instance, BSAGuiHandler.GUI_ELECTRICAL_ENG_MACH, world, x, y, z);
 
 		return true;
 	}
 
 	// *******************************************************************
 	public static void updateBlockState(boolean isActive, World world, int x, int y, int z) {
 		int metadata = world.getBlockMetadata(x, y, z);
 		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
 		keepEngMachInventory = true;
 
		int id = isActive ? BSABlocks.engElecActiveMachBlock.blockID: BSABlocks.engElecMachBlock.blockID;
 		world.setBlock(x, y, z, id);
 
 		keepEngMachInventory = false;
 
 		world.setBlockMetadataWithNotify(x, y, z, metadata, 2);
 
 		if (tileEntity != null) {
 			tileEntity.validate();
 			world.setBlockTileEntity(x, y, z, tileEntity);
 		}
 	}
 
 	// *******************************************************************
 	public static void registry() {
 		int id = BSAMod.config.getBlock("EngElecMachBlock", BSABlocks.ELEC_ENG_MACH_ID).getInt();
 		BSABlocks.engElecMachBlock = new EngElecMachBlock(id, false);
 
 		id = BSAMod.config.getBlock("EngElecActiveMachBlock", BSABlocks.ELEC_ENG_MACH_ACTIVE_ID).getInt();
 		BSABlocks.engElecActiveMachBlock = new EngElecMachBlock(id, true);
 	}
 
 	// *******************************************************************
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister register) {
 		this.frontIcon = register.registerIcon("bsa:" + TEXTURE_FRONT);
 		this.activeFrontIcon = register.registerIcon("bsa:" + TEXTURE_FRONT_ACTIVE);
 		this.sideIcon = register.registerIcon("bsa:" + TEXTURE_SIDE);
 		this.otherIcon = register.registerIcon("bsa:" + TEXTURE_OTHER);
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public Icon getIcon(int side, int metadata) {
 		switch(side) {
 
 		//top & bottom
 		case 0:
 		case 1:
 			return this.otherIcon;
 
 		default:
 			//if metadata == 0 (inventory rendering) && side == front
 			if(metadata == 0 && side == 3)
 				return this.frontIcon;
 
 			return side != metadata ? this.sideIcon : (this.active ? this.activeFrontIcon: this.frontIcon);
 
 		}
 	}
 
 	@Override
 	public TileEntity createNewTileEntity(World world) { return new EngElecMachTileEntity(); }
 
 }
