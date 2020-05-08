 package hunternif.mc.dota2items.item;
 
 import hunternif.mc.dota2items.Sound;
 import hunternif.mc.dota2items.core.buff.Buff;
 import hunternif.mc.dota2items.util.DescriptionBuilder.Description;
 import hunternif.mc.dota2items.util.IntVec3;
 import hunternif.mc.dota2items.util.SideHit;
 import hunternif.mc.dota2items.util.TreeUtil;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemAxe;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 @Description("Active: Destroy Tree/Ward - Destroy a target tree, or deals 100 damage to a ward.\n" +
 		"Passive: Quell - Gives bonus attack damage against non-hero units, depending on the type of hero you are.\n" +
 		"[Melee bonus:] {32%}\n[Ranged bonus:] {12%}")
 public class QuellingBlade extends CooldownItem {
 
 	// Quelling Blade is an axe, so why not make it effective against wood
 	public static final Block[] blocksEffectiveAgainst = ItemAxe.blocksEffectiveAgainst;
 	// Like an iron axe it is
 	public float efficiencyOnProperMaterial = 6.0F;
 	
 	/** Look for the tree trunk minus this delta on the Y axis from the click point. */
 	public static final int trunkSearchDeltaY = 3;
 	/** Look for the tree trunk in a horizontal radius from the click point. */
 	public static final int trunkSearchRadius = 3;
 
 	public QuellingBlade(int id) {
 		super(id);
 		setCooldown(5);
 		passiveBuff = new Buff(this).setDamagePercentMelee(32).setDamagePercentRanged(16).setDoesNotStack();
 		setPrice(225);
 	}
 	
 	@Override
 	public float getStrVsBlock(ItemStack itemStack, Block block) {
 		return block != null && (block.blockMaterial == Material.wood || block.blockMaterial == Material.plants || block.blockMaterial == Material.vine) ? this.efficiencyOnProperMaterial : super.getStrVsBlock(itemStack, block);
 	}
 	
 	@Override
 	public boolean onBlockDestroyed(ItemStack itemStack, World world, int blockID, int x, int y, int z, EntityLiving player) {
 		if ((double)Block.blocksList[blockID].getBlockHardness(world, x, y, z) != 0.0D) {
 				itemStack.damageItem(1, player);
 		}
 		return true;
 	}
 	
 	@Override
 	public int getDamageVsEntity(Entity par1Entity) {
 		return 4;
 	}
 	
 	@SideOnly(Side.CLIENT)
 	@Override
 	public boolean isFull3D() {
 		return true;
 	}
 
 	@Override
 	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world,
 			int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		if (!tryUse(itemStack, player)) {
 			return false;
 		}
 		// Looking for a tree
 		Block block = Block.blocksList[world.getBlockId(x, y, z)];
 		if (block == Block.vine) {
 			// Hit vine while probably aiming at what was behind it:
 			switch(side) {
 			case SideHit.NORTH:
 				x++;
 				break;
 			case SideHit.SOUTH:
 				x--;
 				break;
 			case SideHit.EAST:
 				z++;
 				break;
 			case SideHit.WEST:
 				z--;
 				break;
 			}
 			// Now Try again
 			block = Block.blocksList[world.getBlockId(x, y, z)];
 		}
 		if (block == Block.snow) {
 			// Hit snow while probably aiming at what was below it:
 			y--;
 			// Now Try again
 			block = Block.blocksList[world.getBlockId(x, y, z)];
 		}
 		if (block != Block.wood && block != Block.leaves) {
 			return false;
 		} else if (block == Block.wood) {
 			// Supposedly hit the trunk
 			int trunkBaseY = TreeUtil.getTreeTrunkBaseY(world, x, y, z);
 			if (trunkBaseY > 0) {
 				// Yep, found a tree
				if (!world.isRemote) {
					startCooldown(itemStack, player);
				}
 				TreeUtil.removeTree(world, new IntVec3(x, trunkBaseY, z), true);
 				world.playSoundEffect(x, trunkBaseY, z, Sound.TREE_FALL.name, 1.0f, 1.0f);
 				return true;
 			} else {
 				return false;
 			}
 		} else if (block == Block.leaves) {
 			// Hit some leaves. Looking for the closest tree trunk around
 			IntVec3 trunkBase = TreeUtil.findTreeTrunkInBox(world, x, y, z, trunkSearchDeltaY, trunkSearchRadius);
 			if (trunkBase != null) {
 				// Yep, found a tree
				if (!world.isRemote) {
					startCooldown(itemStack, player);
				}
 				TreeUtil.removeTree(world, trunkBase, true);
 				world.playSoundEffect(trunkBase.x, trunkBase.y, trunkBase.z, Sound.TREE_FALL.name, 1.0f, 1.0f);
 				return true;
 			} else {
 				return false;
 			}
 		} else {
 			// Dead code
 			return false;
 		}
 	}
 	
 	@Override
 	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
 		playDenyGeneralSound(player);
 		return itemStack;
 	}
 }
