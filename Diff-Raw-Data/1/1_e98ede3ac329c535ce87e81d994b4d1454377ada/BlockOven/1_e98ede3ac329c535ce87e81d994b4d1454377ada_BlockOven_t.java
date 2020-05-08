 package mods.cc.rock.block;
 
 import java.util.Random;
 
 import mods.cc.rock.CookingCraft;
 import mods.cc.rock.item.ModItems;
 import mods.cc.rock.lib.Reference;
 import mods.cc.rock.tileentity.TileEntityOven;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 
 public class BlockOven extends BlockContainerCC{
     private Random rand = new Random();
     private static boolean keepOvenInventory;
     private boolean isActive;
     private int type;
     public BlockOven(int id, boolean active, int type)
     {
     	
         super(id, Material.rock);
         this.setResistance(30)
             .setHardness(2)
             .setStepSound(soundAnvilFootstep);
             this.isActive = active;
         keepOvenInventory = false;
         this.type = type;
     }
     
     @Override
     public TileEntity createNewTileEntity(World world)
     {
     	TileEntityOven te = new TileEntityOven();
     	te.COOK_SPEED = type == 1 ? 150 : (type == 2 ? 100 : (type == 3 ? 50 : 200));
     	te.TYPE = type;
         return te;
         
     }
 
     @Override
     public void breakBlock(World world, int x, int y, int z, int id, int meta)
     {
         dropInventory(world, x, y, z);
         super.breakBlock(world, x, y, z, id, meta);
     }
     private Icon sides;
     private Icon front;
     @Override
     public void registerIcons(IconRegister iconRegister)
     {
         sides = iconRegister.registerIcon(Reference.MOD_ID.toLowerCase() + ":Oven"+type+"Sides");
         front = iconRegister.registerIcon(Reference.MOD_ID.toLowerCase() + (this.isActive ? ":OvenFrontOn" : ":OvenFront"));
     }
     @Override
     public Icon getIcon(int par1, int par2){
     	return par2 == 0 && par1 == 3 ? front : ( par1 == par2 ? front : sides );
     	
     }
     
     @Override
     public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9)
     {
     	if(player.getHeldItem()!=null){
 	    	if (player.getHeldItem().itemID == ModItems.itemHammer.itemID){
 	    		return false;
 	    	}
     	}
         if (player.isSneaking())
             return false;
         else
         {
             
                 TileEntityOven tile = (TileEntityOven) world.getBlockTileEntity(x, y, z);
 
                 if (tile != null){
                 	
                     player.openGui(CookingCraft.instance, type, world, x, y, z);
                 }
             
 
             return true;
         }
     }
 
     /**
      * Update which block ID the furnace is using depending on whether or not it is burning
      */
     public static void updateOvenBlockState(boolean par0, World par1World, int par2, int par3, int par4)
     {
         int l = par1World.getBlockMetadata(par2, par3, par4);
         TileEntity tileentity = par1World.getBlockTileEntity(par2, par3, par4);
         keepOvenInventory = true;
         if(tileentity != null){
 	        if (par0)
 	        {
 	            par1World.setBlock(par2, par3, par4, ((TileEntityOven)tileentity).TYPE == 1 ? ModBlocks.oven1On.blockID : (((TileEntityOven)tileentity).TYPE == 2 ? ModBlocks.oven2On.blockID : (((TileEntityOven)tileentity).TYPE == 3 ? ModBlocks.oven3On.blockID : 1)));
 	        }
 	        else
 	        {
 	            par1World.setBlock(par2, par3, par4, ((TileEntityOven)tileentity).TYPE == 1 ? ModBlocks.oven1.blockID : (((TileEntityOven)tileentity).TYPE == 2 ? ModBlocks.oven2.blockID : (((TileEntityOven)tileentity).TYPE == 3 ? ModBlocks.oven3.blockID : 1)));
 	        }
         }
 
         keepOvenInventory = false;
         par1World.setBlockMetadataWithNotify(par2, par3, par4, l, 2);
 
         if (tileentity != null)
         {
             tileentity.validate();
             par1World.setBlockTileEntity(par2, par3, par4, tileentity);
         }
     }
     
     private void dropInventory(World world, int x, int y, int z)
     {
     	if(!keepOvenInventory){
 	        TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
 	
 	        if (!(tileEntity instanceof IInventory))
 	            return;
 	
 	        IInventory inventory = (IInventory) tileEntity;
 	
 	        for (int i = 0; i < inventory.getSizeInventory(); i++)
 	        {
 	            ItemStack itemStack = inventory.getStackInSlot(i);
 	
 	            if (itemStack != null && itemStack.stackSize > 0)
 	            {
 	                float dX = rand.nextFloat() * 0.8F + 0.1F;
 	                float dY = rand.nextFloat() * 0.8F + 0.1F;
 	                float dZ = rand.nextFloat() * 0.8F + 0.1F;
 	
 	                EntityItem entityItem = new EntityItem(world, x + dX, y + dY, z + dZ, new ItemStack(itemStack.itemID, itemStack.stackSize, itemStack.getItemDamage()));
 	
 	                if (itemStack.hasTagCompound())
 	                    entityItem.getEntityItem().setTagCompound((NBTTagCompound) itemStack.getTagCompound().copy());
 	
 	                float factor = 0.05F;
 	                
 	                entityItem.motionX = rand.nextGaussian() * factor;
 	                entityItem.motionY = rand.nextGaussian() * factor + 0.2F;
 	                entityItem.motionZ = rand.nextGaussian() * factor;
 	                
 	                world.spawnEntityInWorld(entityItem);
 	                itemStack.stackSize = 0;
 	            }
 	        }
     	}
 
     }
     public int idDropped(int par1, Random par2Random, int par3)
     {
         return ModBlocks.machineCoreOff.blockID;
     }
     @Override
     public int idPicked(World par1World, int par2, int par3, int par4)
     {
         return type == 1 ?  ModBlocks.oven1On.blockID : (type == 2 ?  ModBlocks.oven2On.blockID : (type == 3 ?  ModBlocks.oven3On.blockID : 1));
     }
 }
