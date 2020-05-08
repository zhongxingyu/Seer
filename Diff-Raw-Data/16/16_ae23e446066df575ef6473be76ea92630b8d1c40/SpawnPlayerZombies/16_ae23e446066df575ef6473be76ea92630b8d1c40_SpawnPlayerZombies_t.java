 /**
  * 
  */
 package com.nekokittygames.modjam.UnDeath;
 
 import net.minecraft.entity.Entity;
 
 import com.google.common.base.Function;
 
import cpw.mods.fml.client.FMLClientHandler;
 import cpw.mods.fml.common.network.EntitySpawnPacket;
 
 /**
  * @author Katrina
  *
  */
 public class SpawnPlayerZombies implements Function<EntitySpawnPacket, Entity> {
 
 	@Override
 	public
 	Entity apply(EntitySpawnPacket input) {
		return new EntityPlayerZombie(FMLClientHandler.instance().getClient().theWorld);
 	}
 
 }
