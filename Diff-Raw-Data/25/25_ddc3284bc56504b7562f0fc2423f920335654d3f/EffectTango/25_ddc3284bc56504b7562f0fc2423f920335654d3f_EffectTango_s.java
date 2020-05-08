 package hunternif.mc.dota2items.effect;
 
 import hunternif.mc.dota2items.client.particle.ParticleTango;
 
 import java.util.Random;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.particle.EffectRenderer;
 import net.minecraft.client.particle.EntityFX;
 import net.minecraft.entity.Entity;
 import net.minecraft.world.World;
 
 public class EffectTango extends EntityEffect {
 
 	public EffectTango(int id) {
 		super(id);
 	}
 
 	@Override
 	public void perform(Entity entity, Object... data) {
 		Random rand = new Random();
 		if (entity.ticksExisted % (1 + rand.nextInt(3)) == 0) {
 			World world = Minecraft.getMinecraft().theWorld;
 			EffectRenderer effectRenderer = Minecraft.getMinecraft().effectRenderer;
 			double x = entity.posX + (rand.nextDouble()*2-1)*0.5;
 			double y = entity.posY - entity.yOffset + entity.height - rand.nextDouble()*0.5;
 			double z = entity.posZ + (rand.nextDouble()*2-1)*0.5;
 			EntityFX particle = new ParticleTango(entity.worldObj, x, y, z);
 			effectRenderer.addEffect(particle);
 		}
 	}
 
 }
