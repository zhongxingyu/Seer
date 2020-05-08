 package MysticWorld.Items;
 
 import MysticWorld.MysticWorld;
 import MysticWorld.Blocks.BlockHandler;
 import MysticWorld.Entity.EntityChargeAir;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.passive.EntityCow;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.EnumMovingObjectType;
 import net.minecraft.util.MovingObjectPosition;
 import net.minecraft.world.World;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.event.Event;
 import net.minecraftforge.event.entity.player.FillBucketEvent;
 
 public class ItemOrbWater extends Item {
 
 	public ItemOrbWater(int par1) 
 	{
 		super(par1);
         this.setMaxStackSize(1);
         this.setMaxDamage(200);
 		this.setCreativeTab(MysticWorld.MysticWorldTab);
 	}
 	
 	@Override
 	public void registerIcons(IconRegister iconRegister)
 	{
 	    itemIcon = iconRegister.registerIcon("MysticTextures:OrbWater");
 	}
 	
 	 public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer entityPlayer)
 	    {
 	        MovingObjectPosition movingobjectposition = this.getMovingObjectPositionFromPlayer(world, entityPlayer, true);
 
 	        if (movingobjectposition == null)
 	        {
 	            return itemStack;
 	        }
 	        else
 	        {
 	            if (movingobjectposition.typeOfHit == EnumMovingObjectType.TILE)
 	            {
 	                int i = movingobjectposition.blockX;
 	                int j = movingobjectposition.blockY;
 	                int k = movingobjectposition.blockZ;
 
 	                if (!world.canMineBlock(entityPlayer, i, j, k))
 	                {
 	                    return itemStack;
 	                }
 
 	                if (!entityPlayer.canPlayerEdit(i, j, k, movingobjectposition.sideHit, itemStack))
 	                {
 	                    return itemStack;
 	                }
 
 	                if (world.getBlockMaterial(i, j, k) == Material.water && world.getBlockMetadata(i, j, k) == 0)
 	                {
 	                    world.setBlockToAir(i, j, k);

 	                    itemStack.damageItem(1, entityPlayer);
 	                    
 	                    return itemStack;
 	                }
 	                else
 	                {
 	                	if (movingobjectposition.sideHit == 0)
 	                    {
 	                        --j;
 	                    }
 
 	                    if (movingobjectposition.sideHit == 1)
 	                    {
 	                        ++j;
 	                    }
 
 	                    if (movingobjectposition.sideHit == 2)
 	                    {
 	                        --k;
 	                    }
 
 	                    if (movingobjectposition.sideHit == 3)
 	                    {
 	                        ++k;
 	                    }
 
 	                    if (movingobjectposition.sideHit == 4)
 	                    {
 	                        --i;
 	                    }
 
 	                    if (movingobjectposition.sideHit == 5)
 	                    {
 	                        ++i;
 	                    }
 
 	                    if (!entityPlayer.canPlayerEdit(i, j, k, movingobjectposition.sideHit, itemStack))
 	                    {
 	                        return itemStack;
 	                    }
 	                    else
 	                    {
 	                        int i1 = world.getBlockId(i, j, k);
 
 	                        if (i1 == 0)
 	                        {
 	                            world.playSoundEffect((double)i + 0.5D, (double)j + 0.5D, (double)k + 0.5D, "fire.ignite", 1.0F, itemRand.nextFloat() * 0.4F + 0.8F);
 	                            world.setBlock(i, j, k, Block.waterMoving.blockID);
 	                        }

	                        itemStack.damageItem(1, entityPlayer);
 	                        
 	                        return itemStack;
 	                    }
 	                }
 	            }
 
 	            return itemStack;
 	        }
 	    }
 }
