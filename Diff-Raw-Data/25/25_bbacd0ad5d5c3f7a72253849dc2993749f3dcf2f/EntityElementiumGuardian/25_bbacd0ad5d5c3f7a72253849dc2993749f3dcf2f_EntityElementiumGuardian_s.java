 /**
  * This Code is Open Source and distributed under a
  * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 License
  * (http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB)
  */
 // Created @ 24 Dec 2012
 package vazkii.tinkerer.entity;
 
 import java.awt.Color;
 
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.monster.EntitySilverfish;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.potion.PotionEffect;
 import net.minecraft.util.DamageSource;
 import net.minecraft.world.World;
 import vazkii.tinkerer.ElementalTinkerer;
import vazkii.tinkerer.client.handler.ClientTickHandler;
 import vazkii.tinkerer.reference.EntityReference;
 import vazkii.tinkerer.reference.ResourcesReference;
 
 /**
  * EntityElementiumGuardian
  *
  * The Elementium Guardian Entity. This class handles health, strenght and
  * texture values, as well as allowing it to be mapped to a different render
  * class than RenderSilverfish.
  *
  * @author Vazkii
  */
 public class EntityElementiumGuardian extends EntitySilverfish {
 
 	public EntityElementiumGuardian(World par1World) {
 		super(par1World);
 		texture = ResourcesReference.MOB_ELEMENTAL_GUARDIAN_TEXTURE;
 		moveSpeed = EntityReference.SPEED_ELEMENTIUM_GUARDIAN;
 	}
 
 	@Override
 	public int getMaxHealth() {
 		return EntityReference.MOB_HEALTH_ELEMENTIUM_GUARDIAN;
 	}
 
 	/** Always has a path, to prevent this entity (being an instance of
 	 * silverfish to enter blocks **/
 	@Override
 	public boolean hasPath() {
 		return true;
 	}
 
 	@Override
 	public int getAttackStrength(Entity par1Entity) {
 		return EntityReference.DMG_ELEMENTIUM_GUARDIAN;
 	}
 
 	@Override
 	public void onLivingUpdate() {
 		super.onLivingUpdate();
 
		Color rgbColor = new Color(Color.HSBtoRGB((float) Math.cos((double) ClientTickHandler.elapsedClientTicks / ResourcesReference.SPECTRUM_DIVISOR_ELEMENTIUM_GUARDIAN), 0.9F, 0.7F));
 		for(int i = 0; i < EntityReference.ELEMENTIUM_GUARDIAN_PARTICLE_COUNT; i++)
 			ElementalTinkerer.proxy.spawnColoredPortalParticle(rgbColor, worldObj, posX + (rand.nextDouble() - 0.5D) * width, posY + rand.nextDouble() * height - 0.25D, posZ + (rand.nextDouble() - 0.5D) * width, (rand.nextDouble() - 0.5D) * 2.0D, -rand.nextDouble(), (rand.nextDouble() - 0.5D) * 2.0D);
 	}
 
 	@Override
 	protected void attackEntity(Entity par1Entity, float par2) {
 		super.attackEntity(par1Entity, par2);
 
 		if(par1Entity instanceof EntityPlayer)
 			((EntityPlayer) par1Entity).addPotionEffect(new PotionEffect(EntityReference.ELEMENTIUM_GUARDIAN_DEBUFF_ID,
 																		 EntityReference.ELEMENTIUM_GUARDIAN_DEBUFF_TIME,
 																		 EntityReference.ELEMENTIUM_GUARDIAN_DEBUFF_LEVEL));
 		// Add a Potion Effect to the player, STOP MINING US!
 	}
 
 	@Override
 	protected void damageEntity(DamageSource par1DamageSource, int par2) {
 		if(par1DamageSource != DamageSource.drown &&
 		   par1DamageSource != DamageSource.fall &&
 		   par1DamageSource != DamageSource.onFire &&
 		   par1DamageSource != DamageSource.lava &&
 		   par1DamageSource != DamageSource.inWall) // Elemental Immunities
 		super.damageEntity(par1DamageSource, par2);
 	}
 
 }
