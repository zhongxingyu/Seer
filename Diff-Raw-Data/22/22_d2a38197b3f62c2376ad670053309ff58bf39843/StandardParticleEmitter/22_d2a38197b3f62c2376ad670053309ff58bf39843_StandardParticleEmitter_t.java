 package spatial;
 
 import com.jme3.asset.AssetManager;
 import com.jme3.effect.ParticleEmitter;
 import com.jme3.effect.ParticleMesh;
 import com.jme3.material.Material;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Vector3f;
import com.jme3.texture.Texture;
 /**
  *
  * @author natwei
  */
public class StandardParticleEmitter {
     
    private static Texture textureForParticles;
     
     public StandardParticleEmitter () {}
     
     public static ParticleEmitter make (AssetManager assetManager) {
         ParticleEmitter fire = 
             new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 30);
             Material materialForParticles = new Material(assetManager, 
             "Common/MatDefs/Misc/Particle.j3md");
            if (textureForParticles == null) {
                textureForParticles = assetManager.loadTexture(
            "Textures/Explosion/flame.png");
            }
            materialForParticles.setTexture("Texture", textureForParticles);
         
 
             fire.setMaterial(materialForParticles.clone());
             fire.setImagesX(2); 
             fire.setImagesY(2); // 2x2 texture animation
         return fire;
     }
     
     public static ParticleEmitter forcefield (AssetManager assetManager) {
         ParticleEmitter glow = StandardParticleEmitter.make(assetManager);
          glow.getMaterial().setTexture("Texture", assetManager.loadTexture(
             "Textures/Explosion/shockwave.png"));
         glow.setImagesX(1);
         glow.setImagesY(1);
         glow.setNumParticles(1);
         glow.setStartColor(ColorRGBA.Yellow);
         glow.setEndColor(ColorRGBA.White);
         glow.getParticleInfluencer().setInitialVelocity(Vector3f.ZERO);
         glow.setStartSize(0.1f);
         glow.setEndSize(3.51f);
         glow.setGravity(0, 0, 0);
         glow.setLowLife(2f);
         glow.setHighLife(2f);
         glow.getParticleInfluencer().setVelocityVariation(0.3f);
         return glow;
     }
 }
