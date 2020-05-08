 package pixlepix.particlephysics.common.entity;
 
 import net.minecraft.block.Block;
 import net.minecraft.world.World;
 import pixlepix.particlephysics.common.api.BaseParticle;
 import pixlepix.particlephysics.common.helper.ClientProxy;
 import pixlepix.particlephysics.common.render.BlockRenderInfo;
 
 public class SeedParticle extends BaseParticle {
 
 	public SeedParticle(World par1World) {
 		super(par1World);
 	}
 
 	@Override
 	public float getStartingPotential() {
 		return 200;
 	}
 
 	@Override
 	public String getName(){
 		return "Seed";
 	}
 	@Override
 	public void onCollideWithParticle(BaseParticle particle) {
		particle.potential=(float) Math.max(particle.potential*1.1, particle.getStartingPotential()*2);
		
 	}
 
 }
