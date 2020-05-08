 package tesseract.objects.emitters;
 
 import java.util.LinkedList;
 import java.util.List;
 
import javax.media.j3d.BranchGroup;
 import javax.media.j3d.Node;
 import javax.vecmath.Color3f;
 import javax.vecmath.Vector3f;
 
 import tesseract.objects.Particle;
 import tesseract.objects.PhysicalObject;
 
 /**
  * ParticleEmitter Class.
  *
  * @author Jesse Morgan
  */
 public class ParticleEmitter extends PhysicalObject {
 
 	/**
 	 * Frequency of new objects.
 	 */
 	private float myFrequency;
 	
 	/**
 	 * The object to create...
 	 */
 	private Color3f myColor;
 	
 	/**
 	 * Counter to keep track of how long has passed.
 	 */
 	private float myCount;
 	
 	/**
 	 * Construct a new Particle Emitter.
 	 * 
 	 * @param position Where to put the emitter
 	 * @param frequency How often to emit the object (seconds).
 	 * @param color Color the particle be (null for random).
 	 */
 	public ParticleEmitter(final Vector3f position, final float frequency,
 			final Color3f color) {
 		
 		super(position, Float.POSITIVE_INFINITY);
 		this.collidable = false;
		setShape(new BranchGroup());
 		
 		myCount = 0;
 		myFrequency = frequency;
 		myColor = color;
 	}
 
 	/**
 	 * Update State and maybe generate a new object.
 	 * 
 	 * @param duration The length of time that has passed.
 	 * @return A list of new objects to add to the world.
 	 */
 	public List<PhysicalObject> spawnChildren(final float duration) {
 		List<PhysicalObject> children = super.spawnChildren(duration);
 		
 		if (children == null) {
 			children = new LinkedList<PhysicalObject>();
 		}
 		
 		myCount += duration;
 		if (myCount >= myFrequency) {
 			children.add(new Particle(this.position, myColor));
 			myCount = 0;
 		}
 		
 		return children;
 	}
 }
