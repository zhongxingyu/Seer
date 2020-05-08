 package euclidstand;
 
 import com.jmex.effects.particles.ParticleMesh;
 import euclidstand.engine.JMENode;
 import euclidstand.engine.JMESpatial;
 import euclidstand.engine.JMESphere;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 /**
  * Manages projectile collisions and creates explosions accordingly
  */
 public class ShellObserver extends EntityObserver implements Observer {
 
 	private final JMENode sceneNode;
 	private final JMENode explosionNode;
 	private final ShellCollision shellCollision;
 	private final Builder builder;
 	private final JMESphere explosionSphere;
 
 	public ShellObserver(List<Entity> entitiesToAdd, ShellCollision shellCollision,
 			JMENode sceneNode, JMENode explosionNode, Builder builder, JMESphere explosionSphere) {
 		super(entitiesToAdd);
 		this.shellCollision = shellCollision;
 		this.sceneNode = sceneNode;
 		this.explosionNode = explosionNode;
 		this.builder = builder;
 		this.explosionSphere = explosionSphere;
 	}
 
 	/**
 	 * Called when a shell collides.
 	 * Finds enemies in the blast radius and damages them accordingly
 	 * @param o shell entity
 	 * @param arg unused
 	 */
 	public void update(Observable o, Object arg) {
 		ShellEntity shell = (ShellEntity) o;
 		ParticleMesh explosion = builder.buildSmallExplosion(
 				"ShellExplosion", shell.getSelf());
 
 		// Find enemies and hurt them
		explosionSphere.setSphereToLocation(shell.getSelf());
		explosionSphere.updateModelBound();
 		explosionSphere.calculateCollisions(sceneNode, shellCollision);
		shellCollision.clear();
 		explosionNode.attachChild(explosion);
 		explosion.updateRenderState();
 		explosion.forceRespawn();
 	}
 }
