 package mods.cc.rock.block;
 
 import java.util.Random;
 
 import mods.cc.rock.CookingCraft;
 import mods.cc.rock.item.ModItems;
 import mods.cc.rock.lib.Reference;
 import mods.cc.rock.tileentity.TileEntityFoodAssembler;
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
 
 public class BlockFoodAssembler extends BlockContainerCC{
     private Random rand = new Random();
 
     public BlockFoodAssembler(int id)
     {
         super(id, Material.rock);
         this.setResistance(30)
             .setHardness(2)
             .setStepSound(soundAnvilFootstep);
     }
     
     @Override
     public TileEntity createNewTileEntity(World world)
     {
         return new TileEntityFoodAssembler();
         
     }
 
     @Override
     public void breakBlock(World world, int x, int y, int z, int id, int meta)
     {
         dropInventory(world, x, y, z);
         super.breakBlock(world, x, y, z, id, meta);
     }
     private Icon sides;
     private Icon front;
     private Icon bottom;
     private Icon top;
     @Override
     public void registerIcons(IconRegister iconRegister)
     {
         sides = iconRegister.registerIcon(Reference.MOD_ID.toLowerCase() + ":foodprepsides");
         front = iconRegister.registerIcon(Reference.MOD_ID.toLowerCase() + ":foodprepfront");
         bottom = iconRegister.registerIcon(Reference.MOD_ID.toLowerCase() + ":foodprepbottom");
         top = iconRegister.registerIcon(Reference.MOD_ID.toLowerCase() + ":foodpreptop");
     }
     @Override
     public Icon getIcon(int par1, int par2){
     	return par2 == 0 && par1 == 3 ? front : (par1 == par2 ? front : ( par1 == 0  ? bottom : (par1 == 1 ? top : sides)));
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
             
                 TileEntityFoodAssembler tile = (TileEntityFoodAssembler) world.getBlockTileEntity(x, y, z);
 
                 if (tile != null){
                 	
                    player.openGui(CookingCraft.instance, 4, world, x, y, z);
                 }
             
 
             return true;
         }
     }
 
 
     private void dropInventory(World world, int x, int y, int z)
     {
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
     public int idDropped(int par1, Random par2Random, int par3)
     {
         return ModBlocks.machineCoreOff.blockID;
     }
 }
