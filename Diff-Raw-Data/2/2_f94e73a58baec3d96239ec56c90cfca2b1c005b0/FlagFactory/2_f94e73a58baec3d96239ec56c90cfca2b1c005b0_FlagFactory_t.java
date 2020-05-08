 /**
  * 
  */
 package de.findus.cydonia.level;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.jme3.asset.AssetManager;
 import com.jme3.bullet.collision.shapes.BoxCollisionShape;
 import com.jme3.bullet.collision.shapes.CollisionShape;
 import com.jme3.bullet.control.GhostControl;
 import com.jme3.effect.ParticleEmitter;
 import com.jme3.effect.ParticleMesh.Type;
 import com.jme3.effect.shapes.EmitterBoxShape;
 import com.jme3.effect.shapes.EmitterMeshFaceShape;
 import com.jme3.material.Material;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Quaternion;
 import com.jme3.math.Vector3f;
 import com.jme3.renderer.queue.RenderQueue.ShadowMode;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Mesh;
 import com.jme3.scene.Node;
 import com.jme3.scene.shape.Box;
 import com.jme3.scene.shape.Quad;
 import com.jme3.util.TangentBinormalGenerator;
 
 /**
  * @author Findus
  *
  */
 public class FlagFactory {
 
 	private static FlagFactory instance;
 	
 	public static void init(AssetManager assetManager) {
 		instance = new FlagFactory(assetManager);
 	}
 	
 	public static FlagFactory getInstance() {
 		return instance;
 	}
 	
 	private AssetManager assetManager;
 	
 	/**
 	 * 
 	 */
 	private FlagFactory(AssetManager assetManager) {
 		this.assetManager = assetManager;
 	}
 	
 	public Flag createFlag(int id, Vector3f origin, int team) {
 		Flag f = new Flag();
 		f.setId(id);
 		f.setOrigin(origin);
 		f.setTeam(team);
 
 		ColorRGBA color = null;
 		ColorRGBA colorbase = null;
 		
 		if(team == 1) {
 			color = ColorRGBA.Blue;
 			colorbase = new ColorRGBA(0, 0, 1, 0.5f);
 		}else if (team == 2) {
 			color = ColorRGBA.Red;
 			colorbase = new ColorRGBA(1, 0, 0, 0.5f);
 		}
 		
 		// flag model
 		Material mat_lit = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
 	    mat_lit.setBoolean("UseMaterialColors",true);    
 	    mat_lit.setColor("Specular",ColorRGBA.White);
 	    mat_lit.setColor("Diffuse", color);
 	    mat_lit.setColor("Ambient", color);
 	    mat_lit.setFloat("Shininess", 2f);
 	    
 	    Mesh mesh = new Box(0.1f, 0.1f, 0.1f);
         Geometry model = new Geometry("Flag_" + id, mesh);
         model.setMaterial(mat_lit);
 		model.setUserData("id", id);
 		model.setShadowMode(ShadowMode.CastAndReceive);
 		TangentBinormalGenerator.generate(model);
 		model.setLocalTranslation(0, 0.5f, 0);
         
 		f.setModel(model);
 		
 		Node nodeBase = new Node("Flag_" + id);
 		nodeBase.setUserData("id", id);
 		nodeBase.setUserData("FlagBase", true);
 		nodeBase.setUserData("team", team);
 		
 		ParticleEmitter glitterBase = new ParticleEmitter("Glitter", Type.Triangle, 30);
         Material mat_red = new Material(assetManager, 
                 "Common/MatDefs/Misc/Particle.j3md");
         mat_red.setTexture("Texture", assetManager.loadTexture(
                 "Effects/Explosion/flame.png"));
         glitterBase.setMaterial(mat_red);
         glitterBase.setImagesX(2); 
         glitterBase.setImagesY(2); // 2x2 texture animation
         glitterBase.setEndColor(new ColorRGBA(1f, 1f, 1f, 0.5f));
         glitterBase.setStartColor(color);
         glitterBase.setStartSize(0.03f);
         glitterBase.setEndSize(0.001f);
         glitterBase.setGravity(0, 0, 0);
         glitterBase.setNumParticles(400);
 	    glitterBase.setShape(new EmitterBoxShape(new Vector3f(-0.5f, -1f, -0.5f), new Vector3f(0.5f, 1f, 0.5f)));
 	    glitterBase.getParticleInfluencer().setInitialVelocity(new Vector3f(0.01f, 0.01f, 0.01f));
 	    glitterBase.getParticleInfluencer().setVelocityVariation(1f);
 	    glitterBase.setRandomAngle(true);
 	    glitterBase.setParticlesPerSec(100f);
 	    glitterBase.setLowLife(1f);
 	    glitterBase.setHighLife(3f);
 	    nodeBase.attachChild(glitterBase);
 		glitterBase.setEnabled(true);
 		
 		CollisionShape collisionShape = new BoxCollisionShape(new Vector3f(0.5f, 1f, 0.5f));
 		GhostControl baseControl = new GhostControl(collisionShape);
 		baseControl.setCollisionGroup(GhostControl.COLLISION_GROUP_02);
 		baseControl.setCollideWithGroups(GhostControl.COLLISION_GROUP_02);
		nodeBase.addControl(baseControl);
 		
 		nodeBase.setLocalTranslation(origin);
 	    
 		f.setBaseControl(baseControl);
 		f.setBaseModel(nodeBase);
 		
 		return f;
 	}
 
 }
