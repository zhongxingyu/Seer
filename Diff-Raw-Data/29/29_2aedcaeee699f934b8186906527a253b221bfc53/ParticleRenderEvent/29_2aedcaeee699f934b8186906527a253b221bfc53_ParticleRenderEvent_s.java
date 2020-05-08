 package spaceshooters.aurora.event.particle;
 
import pl.matterross.engine.gfx.IRenderable;
 import spaceshooters.aurora.particle.IParticle;
 
 public class ParticleRenderEvent extends ParticleEvent {
 	
 	private IRenderable tex;
 	
 	public ParticleRenderEvent(IParticle particle, IRenderable tex) {
 		super(particle);
 		this.tex = tex;
 	}
 	
 	public IRenderable getTexture() {
 		return tex;
 	}
 	
 	public void setTexture(IRenderable tex) {
 		this.tex = tex;
 	}
 }
