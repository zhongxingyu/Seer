 package metalTools.item;
 
 import java.util.List;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import metalTools.MetalTools;
 import net.minecraft.block.Block;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.ItemTool;
 import net.minecraft.world.World;
 
 public class ItemHammer extends ItemTool {
 	
 	public static final Block[] blocksEffectiveAgainst = new Block[] {
 		Block.grass, Block.dirt, Block.sand, Block.gravel, Block.snow, Block.blockSnow, Block.blockClay, Block.tilledField, Block.slowSand, Block.mycelium,
 		Block.cobblestone, Block.stoneDoubleSlab, Block.stoneSingleSlab, Block.stone, Block.sandStone, Block.cobblestoneMossy, Block.oreIron, Block.blockIron, Block.oreCoal, Block.blockGold, Block.oreGold, Block.oreDiamond, Block.blockDiamond, Block.ice, Block.netherrack, Block.oreLapis, Block.blockLapis, Block.oreRedstone, Block.oreRedstoneGlowing, Block.rail, Block.railDetector, Block.railPowered, Block.railActivator,
 		Block.planks, Block.bookShelf, Block.wood, Block.chest, Block.stoneDoubleSlab, Block.stoneSingleSlab, Block.pumpkin, Block.pumpkinLantern};
 
 	private int radius;
 	
 	public ItemHammer(int par1, float par2, EnumToolMaterial par3EnumToolMaterial, int radius) {
 		super(par1, par2, par3EnumToolMaterial, blocksEffectiveAgainst);
 		
 		this.radius = radius;
 		
 		setUnlocalizedName("" + Items.lastNum++);
 		
 		setCreativeTab(MetalTools.tools);
 	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List list, boolean par4) {
		list.add("Destroys blocks in a " + radius + "x" + radius + " area");
 	}
 	
 	private void destroy(World world, int x, int y, int z, int side) {
 		int val = (radius - 1) / 2;
 		for(int i = 0; i < radius; i++) {
 			for(int j = 0; j < radius; j++) {
 				if(!world.isRemote) {
 					EntityItem ei = null;
 					ItemStack stack = null;
 					if(side == 0 || side == 1) {
 						stack = new ItemStack(world.getBlockId((x - val) + i, y, (z - val) + j), 1, world.getBlockMetadata((x - val) + i, y, (z - val) + j));
						if(stack.itemID != Block.bedrock.blockID) {
							world.setBlockToAir((x - val) + i, y, (z - val) + j);
						}
 						ei = new EntityItem(world, (x - val) + i, y, (z - val) + j, stack);
 					}
 					if(side == 2 || side == 3) {
 						stack = new ItemStack(world.getBlockId((x - val) + i, (y - val) + j, z), 1, world.getBlockMetadata((x - val) + i, (y - val) + j, z));
						if(stack.itemID != Block.bedrock.blockID) {
							world.setBlockToAir((x - val) + i, (y - val) + j, z);
						}
 						ei = new EntityItem(world, (x - val) + i, (y - val) + j, z, stack);
 					}
 					if(side == 4 || side == 5) {
 						stack = new ItemStack(world.getBlockId(x, (y - val) + i, (z - val) + j), 1, world.getBlockMetadata(x, (y - val) + i, (z - val) + j));
						if(stack.itemID != Block.bedrock.blockID) { 
							world.setBlockToAir(x, (y - val) + i, (z - val) + j);
						}
 						ei = new EntityItem(world, x, (y - val) + i, (z - val) + j, stack);
 					}
 					
					if(stack != null && stack.itemID != Block.tallGrass.blockID && stack.itemID != Block.bedrock.blockID) {
 						if(stack.itemID < Block.blocksList.length && stack.itemID != 0) {
 							stack.itemID = Block.blocksList[stack.itemID].idDropped(0, world.rand, 0);
 						}
 						
 						if(stack != null && stack.itemID != 0) {
 							world.spawnEntityInWorld(ei);
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	@Override
 	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
 		destroy(world, x, y, z, side);
 		stack.attemptDamageItem(1, world.rand);
 		return true;
 	}
 }
