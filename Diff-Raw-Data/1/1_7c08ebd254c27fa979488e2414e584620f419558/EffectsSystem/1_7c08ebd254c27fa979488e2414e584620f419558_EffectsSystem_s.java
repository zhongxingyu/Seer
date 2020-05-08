 package fx;
 
 import java.util.ArrayList;
 
 import engine.GlobalInfo;
 
import util.ParticleList;
 import util.SafeList;
 import util.Vector2D;
 
 public class EffectsSystem {
 
 	public ArrayList<Particle> particles = new ArrayList<Particle>();
 	public SafeList<FxEntity> fxEntities = new SafeList<FxEntity>();
 	
 	public EffectsSystem() {
 	}
 	
 	public void particleSpray(Vector2D pos, int n, double lifetime) {
 		for (int j = 0;j<n;j++)
 			particles.add(new Particle(pos, Vector2D.fromAngle(j*1.8, 1), (lifetime-0.2)*Math.random()+0.2, Math.random()*10, -10, this));
 	}
 	
 	
 	public void explosion(Vector2D pos) {
 		particleSpray(pos, 600, 1);
 		fxEntities.add(new Shockwave(this,1,pos,10));
 	}
 	
 	public void process(double dt) {
 		for(FxEntity p : fxEntities) {
 			p.process(dt);
 		}
 		for(int i=0; i<particles.size(); i++) {
 			Particle p = particles.get(i);
 			if(GlobalInfo.time > p.deathtime) {
 				particles.remove(i);
 				i--;
 			} else {
 				p.process(dt);
 			}
 		}
 
 		fxEntities.applyChanges();
 		
 		System.out.println(particles.size());
 	}
 	
 	public void removeEntity(FxEntity e) {
 		if(e instanceof Particle) return;
 		else fxEntities.remove(e);
 	}
 	
 	
 }
