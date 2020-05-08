 package jmm.mods.Diamerald;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.enchantment.Enchantment;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.ItemPickaxe;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Direction;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.World;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class blackDiameraldpickaxe extends ItemPickaxe {
 
 	public static final Block[] blocksEffectiveAgainst = new Block[] {
 			Block.cobblestone, Block.stoneDoubleSlab, Block.stoneSingleSlab,
 			Block.stone, Block.sandStone, Block.cobblestoneMossy,
 			Block.oreIron, Block.blockIron, Block.oreCoal, Block.blockGold,
 			Block.oreGold, Block.oreDiamond, Block.blockDiamond, Block.ice,
 			Block.netherrack, Block.oreLapis, Block.blockLapis,
 			Block.oreRedstone, Block.oreRedstoneGlowing, Block.rail,
 			Block.railDetector, Block.railPowered };
 
 	public blackDiameraldpickaxe(int par1, EnumToolMaterial par2EnumToolMaterial) {
 		super(par1, par2EnumToolMaterial);
 
 	}
 
 	public void onCreated(ItemStack par1ItemStack, World par2World,
 			EntityPlayer par3EntityPlayer) {
 		par1ItemStack.addEnchantment(Enchantment.efficiency, 3);
 		par1ItemStack.addEnchantment(Enchantment.unbreaking, 5);
 		par1ItemStack.addEnchantment(Enchantment.fortune, 3);
 	}
 
 	public boolean canHarvestBlock(Block par1Block) {
 		return par1Block == Block.obsidian ? this.toolMaterial
 				.getHarvestLevel() == 3
 				: (par1Block != Block.blockDiamond
 						&& par1Block != Block.oreDiamond ? (par1Block != Block.oreEmerald
 						&& par1Block != Block.blockEmerald ? (par1Block != Block.blockGold
 						&& par1Block != Block.oreGold ? (par1Block != Block.blockIron
 						&& par1Block != Block.oreIron ? (par1Block != Block.blockLapis
 						&& par1Block != Block.oreLapis ? (par1Block != Block.oreRedstone
 						&& par1Block != Block.oreRedstoneGlowing ? (par1Block.blockMaterial == Material.rock ? true
 						: (par1Block.blockMaterial == Material.iron ? true
 								: par1Block.blockMaterial == Material.anvil))
 						: this.toolMaterial.getHarvestLevel() >= 2)
 						: this.toolMaterial.getHarvestLevel() >= 1)
 						: this.toolMaterial.getHarvestLevel() >= 1)
 						: this.toolMaterial.getHarvestLevel() >= 2)
 						: this.toolMaterial.getHarvestLevel() >= 2)
 						: this.toolMaterial.getHarvestLevel() >= 2);
 
 	}
 
 	public boolean onBlockDestroyed(ItemStack par1ItemStack, World par2World,
 			int par3, int par4, int par5, int par6,
 			EntityLivingBase par7EntityLivingBase) {
 		int direction = MathHelper
 				.floor_double((double) ((par7EntityLivingBase.rotationYaw * 4F) / 360F) + 0.5D) & 3;
 		int dir = MathHelper
 				.floor_double((double) ((par7EntityLivingBase.rotationPitch * 4F) / 360F) + 0.5D) & 3;
 		int[] offsetY = new int[] { 0, -1, 0, 1 };
 		for (int i = -1; i < 2; i++) {
 			for (int j = -1; j < 2; j++) {
 				for (int k = -1; k < 2; k++) {
 					if (offsetY[dir] == 0) {
						if(this.canHarvestBlockId(Block.blocksList[par2World.getBlockId(par4 + k + Direction.offsetX[direction],
 								par5 + i,
 								par6 + j + Direction.offsetZ[direction])]))
 						par2World.destroyBlock(par4 + k
 								+ Direction.offsetX[direction], par5 + i
 								+ offsetY[dir], par6 + j
 								+ Direction.offsetZ[direction], true);
 					} else {
 						if(this.canHarvestBlock(Block.blocksList[par2World.getBlockId(par4 + k, par5 + i
 								+ offsetY[dir], par6 + j)]))
 						par2World.destroyBlock(par4 + k, par5 + i
 								+ offsetY[dir], par6 + j, true);
 					}
 
 				}
 
 			}
 
 		}
 		return true;
 	}
 
 	public float getStrVsBlock(ItemStack par1ItemStack, Block par2Block) {
 		return par2Block != null
 				&& (par2Block.blockMaterial == Material.iron
 						|| par2Block.blockMaterial == Material.anvil || par2Block.blockMaterial == Material.rock) ? this.efficiencyOnProperMaterial
 				: super.getStrVsBlock(par1ItemStack, par2Block);
 	}
 
 	@Override
 	public boolean getIsRepairable(ItemStack par1ItemStack,
 			ItemStack par2ItemStack) {
 		return par2ItemStack.itemID == Diamerald.blackDiameraldgem.itemID;
 
 	}
 
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister par1IconRegister) {
 		this.itemIcon = par1IconRegister
 				.registerIcon("Diamerald:blackDiameraldpick");
 
 	}
 
 }
