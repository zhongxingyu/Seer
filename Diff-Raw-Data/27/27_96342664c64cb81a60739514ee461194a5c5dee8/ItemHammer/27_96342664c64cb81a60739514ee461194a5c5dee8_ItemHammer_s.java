 package mods.cc.rock.item;
 
 import mods.cc.rock.CookingCraft;
 import mods.cc.rock.block.ModBlocks;
 import mods.cc.rock.core.helpers.GeneralHelper;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.effect.EntityLightningBolt;
 import net.minecraft.entity.monster.EntityCreeper;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
 
 public class ItemHammer extends ItemCC
 {
     
 	World w;
 	
 	public ItemHammer(int id, String texture)
 	{
 		super(id, texture);
 		this.setUnlocalizedName("itemHammer")
 			.setCreativeTab(CookingCraft.tabCC)
 			.setMaxDamage(25);
 	}
 
 	@Override
     public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, int par4, int par5, int par6, int par7, float par8, float par9, float par10)
     {
     	if(!par3World.isRemote && (par3World.getBlockId(par4, par5, par6) == ModBlocks.machineCoreOn.blockID || par3World.getBlockId(par4, par5, par6) == ModBlocks.machineCoreOff.blockID))
     	{
 
     		par1ItemStack.damageItem(1, par2EntityPlayer);
             GeneralHelper.spawnItemOnGround(par3World, par4, par5, par6, 1, par2EntityPlayer, new ItemStack(ModBlocks.machineCoreOff));
             par3World.setBlockToAir(par4, par5, par6);
     		
     	}
     	if(!par3World.isRemote && (par3World.getBlockId(par4, par5, par6) == ModBlocks.fridge.blockID))
     	{
 
     		par1ItemStack.damageItem(1, par2EntityPlayer);
             GeneralHelper.spawnItemOnGround(par3World, par4, par5, par6, 1, par2EntityPlayer, new ItemStack(ModBlocks.fridge));
             par3World.setBlockToAir(par4, par5, par6);
     		
     	}
     	return true;
     }
 	
     @Override
     public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
     {
 		w = par2World;
         return par1ItemStack;
     }
     
     @Override
     public boolean itemInteractionForEntity(ItemStack par1ItemStack, EntityLiving par2EntityLiving){
     	
     	if(par2EntityLiving instanceof EntityCreeper && w != null){
     		int tempHp = ((EntityCreeper) par2EntityLiving).getHealth();
     		((EntityCreeper) par2EntityLiving).onStruckByLightning(new EntityLightningBolt(w, 0, 0, 0));
     		((EntityCreeper) par2EntityLiving).extinguish();
     		((EntityCreeper) par2EntityLiving).setEntityHealth(tempHp);
     		
     		return true;
     	}
     	
     	return false;
     }
     
 	
 	
 
 }
