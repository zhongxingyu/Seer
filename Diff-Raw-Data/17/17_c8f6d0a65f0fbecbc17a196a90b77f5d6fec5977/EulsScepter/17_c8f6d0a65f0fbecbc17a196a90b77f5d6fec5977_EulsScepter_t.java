 package hunternif.mc.dota2items.item;
 
 import hunternif.mc.dota2items.Config;
 import hunternif.mc.dota2items.Config.Recipe;
 import hunternif.mc.dota2items.Dota2Items;
 import hunternif.mc.dota2items.Sound;
 import hunternif.mc.dota2items.core.EntityStats;
 import hunternif.mc.dota2items.core.buff.Buff;
 import hunternif.mc.dota2items.core.buff.BuffInstance;
 import hunternif.mc.dota2items.network.BuffPacket;
 import hunternif.mc.dota2items.network.EntityMovePacket;
 import hunternif.mc.dota2items.tileentity.TileEntityCyclone;
 
 import java.util.List;
 
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.World;
 import cpw.mods.fml.common.network.PacketDispatcher;
 
 @Recipe(ingredients={StaffOfWizardry.class, SagesMask.class, VoidStone.class})
 public class EulsScepter extends CooldownItem {
 
 	public EulsScepter(int id) {
 		super(id);
 		setCooldown(30);
 		passiveBuff = new Buff(this).setMovementSpeed(30).setIntelligence(10).setManaRegenPercent(150);
 		setPrice(600);
 		setManaCost(75);
 	}
 	
 	@Override
 	public boolean isFull3D() {
 		return true;
 	}
 	
 	@Override
 	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
 		return onUseEulsScepter(stack, player, entity);
 	}
 	
 	@Override
 	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world,
 			int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
 		AxisAlignedBB box = AxisAlignedBB.getBoundingBox((double)x, (double)y-0.5, (double)z, (double)x+1, (double)y+1.5, (double)z+1);
 		List<EntityLiving> list = world.getEntitiesWithinAABB(EntityLiving.class, box);
 		if (list != null && !list.isEmpty()) {
 			return onUseEulsScepter(itemStack, player, list.get(0));
 		} else {
 			playDenyGeneralSound(player);
 			return false;
 		}
 	}
 	
 	private boolean onUseEulsScepter(ItemStack stack, EntityPlayer player, Entity entity) {
 		if (!tryUse(stack, player, entity)) {
 			return false;
 		}
 		
 		int x = MathHelper.floor_double(entity.posX);
 		int y = MathHelper.floor_double(entity.posY);
 		int z = MathHelper.floor_double(entity.posZ);
 		while (entity.worldObj.getBlockMaterial(x, y, z).isLiquid() && y < entity.worldObj.getHeight()) {
 			y ++;
 		}
 		if (!entity.worldObj.isRemote) {
 			entity.worldObj.setBlock(x, y, z, Config.cycloneContainer.getID(), 0, 3);
 		}
 		
 		if (!entity.worldObj.isRemote) {
 			entity.motionX = 0;
 			entity.motionY = 0;
 			entity.motionZ = 0;
			entity.setPosition(((double) x) + 0.5, ((double) y) + 3, ((double) z) + 0.5);
			entity.lastTickPosX = entity.prevPosX = entity.posX;
			entity.lastTickPosY = entity.prevPosY = entity.posY;
			entity.lastTickPosZ = entity.prevPosZ = entity.posZ;
 			PacketDispatcher.sendPacketToAllPlayers(new EntityMovePacket(entity).makePacket());
 			
 			long cycloneEndTime = entity.worldObj.getTotalWorldTime() + (long) (TileEntityCyclone.duration * 20f);
 			BuffInstance buffInst = new BuffInstance(Buff.inCyclone, entity.entityId, cycloneEndTime, false);
 			EntityStats entityStats = Dota2Items.mechanics.getEntityStats((EntityLiving)entity);
 			entityStats.addBuff(buffInst);
 			PacketDispatcher.sendPacketToAllPlayers(new BuffPacket(buffInst).makePacket());
 			
 			// We're on the server, so it's ok:
 			startCooldown(stack, player);
 		}
 		if (!player.capabilities.isCreativeMode) {
 			Dota2Items.mechanics.getEntityStats(player).removeMana(getManaCost());
 		}
 		
 		player.worldObj.playSoundAtEntity(entity, Sound.CYCLONE_START.name, 0.7f, 1);
 		return true;
 	}
 }
