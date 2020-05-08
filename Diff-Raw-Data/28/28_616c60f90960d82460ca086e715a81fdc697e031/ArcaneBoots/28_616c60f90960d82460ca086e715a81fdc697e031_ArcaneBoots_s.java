 package hunternif.mc.dota2items.item;
 
 import hunternif.mc.dota2items.Dota2Items;
 import hunternif.mc.dota2items.Sound;
 import hunternif.mc.dota2items.core.EntityStats;
 import hunternif.mc.dota2items.effect.Effect;
 import hunternif.mc.dota2items.effect.EffectInstance;
 
 import java.util.List;
 
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.AxisAlignedBB;
 
 public class ArcaneBoots extends ActiveItem {
 	public static final double radius = 6;
 	public static final float manaRestored = 135;
 	
 	public ArcaneBoots(int id) {
 		super(id);
 		setCooldown(55);
 		setManaCost(25);
 	}
 	
 	@Override
 	protected void onUse(ItemStack stack, EntityPlayer player) {
 		player.worldObj.playSoundAtEntity(player, Sound.ARCANE_BOOTS.getName(), 0.7f, 1);
 		// Restore mana in the area:
 		double x = Math.floor(player.posX);
 		double y = Math.floor(player.posY);
 		double z = Math.floor(player.posZ);
 		AxisAlignedBB area = AxisAlignedBB.getBoundingBox(x, y, z, x+1, y+1, z+1);
 		area = area.expand(radius, radius, radius);
 		List<EntityLivingBase> list = player.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, area);
 		if (list != null && !list.isEmpty()) {
 			for (EntityLivingBase entity : list) {
 				EntityStats stats = Dota2Items.stats.getEntityStats(entity);
				if (stats != null && stats.entity instanceof EntityPlayer &&
						player.getTeam().isSameTeam( ((EntityPlayer)stats.entity).getTeam() )) {
 					stats.addMana(manaRestored);
					stats.sendSyncPacketToClient((EntityPlayer)stats.entity);
 				}
 			}
 		}
 		EffectInstance effect = new EffectInstance(Effect.arcaneBoots, player);
 		if (!player.worldObj.isRemote) {
 			EffectInstance.notifyPlayersAround(effect, player);
 		}
 	}
 }
