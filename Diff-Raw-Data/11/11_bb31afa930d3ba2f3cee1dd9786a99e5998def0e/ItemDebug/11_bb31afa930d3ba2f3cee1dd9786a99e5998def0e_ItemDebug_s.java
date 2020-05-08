 package mods.CountryGamer_Possession.Possession.Items;
 
 import java.util.List;
 
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.monster.EntityGhast;
 import net.minecraft.entity.monster.EntityIronGolem;
 import net.minecraft.entity.monster.EntityMob;
 import net.minecraft.entity.monster.EntitySnowman;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
 
 public class ItemDebug extends ItemBase {
 
 	public ItemDebug(int id) {
 		super(id);
		this.setCreativeTab(CreativeTabs.tabTools);
 	}
 	
 	public boolean onItemUse(ItemStack itemStack, EntityPlayer player,
 			World world, int x, int y, int z,
 			int side, float par8, float par9, float par10) {
 		//player.addChatMessage("Enter Possession Mode");
 		
 		return false;
 	}
 	
 	public ItemStack onItemRightClick(ItemStack itemStack, World world,
 			EntityPlayer player) {
 		if( !player.worldObj.isRemote )
 			player.addChatMessage("Enter Possession Mode");
 		
 		EntityLivingBase mob = null;
 		double radius = 20.0D; // default 5
 		List<Entity> entities = world.getEntitiesWithinAABBExcludingEntity(
 				player, player.boundingBox.expand(radius, radius, radius));
 		entLoop:
 		for(Entity ent : entities) {
 			if(ent instanceof EntityMob ||
 					ent instanceof EntityGhast ||
 					ent instanceof EntityIronGolem ||
 					ent instanceof EntitySnowman) {
 				mob = (EntityLivingBase) ent;
 				if( !(mob instanceof EntityIronGolem) ) {
 					mob.setDead();
 					EntityLivingBase newMob = new EntityIronGolem(player.worldObj);
 					newMob.setPosition(mob.posX, mob.posY, mob.posZ);
 					if( !player.worldObj.isRemote )
 						player.worldObj.spawnEntityInWorld(newMob);
 					break entLoop;
 				}
 			}
 		}
 		
 		return itemStack;
 	}
 
 
 }
