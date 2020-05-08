 package hunternif.mc.dota2items.effect;
 
 import hunternif.mc.dota2items.client.particle.ParticleArcaneBoots;
 
 import java.util.Random;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.particle.EffectRenderer;
 import net.minecraft.client.particle.EntityFX;
 import net.minecraft.entity.Entity;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.World;
 
 public class EffectArcaneBoots extends EntityEffect {
 	public static final int MAX_PARTICLE_STREAMS = 32;
 	public static final double STREAM_SPACING = 0.15;
 	public static final double STREAM_LENGTH = 1.2;
 
 	public EffectArcaneBoots(int id) {
 		super(id);
 	}
 
 	@Override
 	public void perform(Entity entity, Object... data) {
 		World world = Minecraft.getMinecraft().theWorld;
 		EffectRenderer effectRenderer = Minecraft.getMinecraft().effectRenderer;
 		Random rand = new Random(entity.worldObj.getTotalWorldTime());
 		for (int i = 0; i < MAX_PARTICLE_STREAMS; i++) {
 			float size = 0.7f + 0.3f*rand.nextFloat();
 			float yaw = (rand.nextFloat()*2.0F - 1.0F) * (float) Math.PI;
 			float pitch = -0.3f;
 			double baseDistance = 0.2 + 0.3*rand.nextDouble();
 			double cosYaw = (double) MathHelper.cos(yaw);
 			double sinYaw = (double) MathHelper.sin(yaw);
 			double cosPitch = (double) MathHelper.cos(pitch);
 			double sinPitch = (double) MathHelper.sin(pitch);
 			for (double d = 0; d < STREAM_LENGTH; d += STREAM_SPACING) {
 				double distance = baseDistance + d;
 				double rX = -sinYaw*cosPitch * distance;
 				double rZ = cosYaw*cosPitch * distance;
 				double rY = -sinPitch * distance;
 				double velX = -sinYaw*cosPitch * 0.05D;
 				double velZ = cosYaw*cosPitch * 0.05D;
 				double velY = -sinPitch * 0.05D;
 				EntityFX particle = new ParticleArcaneBoots(world,
 						entity.posX + rX, entity.posY - entity.yOffset + 0.7 + rY, entity.posZ + rZ, 
 						velX, velY, velZ, (float)d*size);
 				effectRenderer.addEffect(particle);
 			}
 		}
 	}
 
 }
