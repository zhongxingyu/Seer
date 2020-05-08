 package keyblades.common.Keyblades;
 
 import keyblades.common.magic.MPCounter;
 import keyblades.common.network.ClientMessager;
 import net.minecraft.src.*;
 
 public class LionKeyblade extends ItemSword{
 	public LionKeyblade(int i, EnumToolMaterial enumtoolmaterial)
 	{
 		super(i, enumtoolmaterial);
 	}
 	public String getTextureFile()
 	{
 		return "/keyblades/art/keyblades.png";
 	}
 	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer)
<<<<<<< HEAD
    {
 		if(MPCounter.MP >= 20)
 		{
 			itemstack.damageItem(1, entityplayer);
 			world.playSoundAtEntity(entityplayer, "random.bow", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
 			if (!world.isRemote)
 			{
 				Vec3 look = entityplayer.getLookVec();
 				EntitySmallFireball fireball = new EntitySmallFireball(world, entityplayer, 0, 0, 0);
 				fireball.setPosition(entityplayer.posX + look.xCoord * 2, entityplayer.posY + look.yCoord + 1, entityplayer.posZ + look.zCoord * 2);
 				fireball.accelerationX = look.xCoord * 0.1;
 				fireball.accelerationY = look.yCoord * 0.1;
 				fireball.accelerationZ = look.zCoord * 0.1;
 				world.spawnEntityInWorld(fireball);
 				MPCounter.loseMP(20);
            }
 		}
 		else{
 			ClientMessager.showInChat("Not Enough MP!");
 		}
=======
	{
 		itemstack.damageItem(1, entityplayer);
 		world.playSoundAtEntity(entityplayer, "random.bow", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
 		if (!world.isRemote)
 		{
 			Vec3 look = entityplayer.getLookVec();
 			EntitySmallFireball fireball = new EntitySmallFireball(world, entityplayer, 0, 0, 0);
 			fireball.setPosition(entityplayer.posX + look.xCoord * 2, entityplayer.posY + look.yCoord + 1, entityplayer.posZ + look.zCoord * 2);
             		fireball.accelerationX = look.xCoord * 0.1;
             		fireball.accelerationY = look.yCoord * 0.1;
             		fireball.accelerationZ = look.zCoord * 0.1;
             		world.spawnEntityInWorld(fireball);
             	}
>>>>>>> 6556fb8752300eb1a6889dd9f173ba35f9cb7b03
 		return itemstack;
 	}
 	public boolean hitEntity(ItemStack items, EntityLiving Living1, EntityLiving Living2)
 	{
 		Living1.setFire(5);
 		return true;
 	}
 }
