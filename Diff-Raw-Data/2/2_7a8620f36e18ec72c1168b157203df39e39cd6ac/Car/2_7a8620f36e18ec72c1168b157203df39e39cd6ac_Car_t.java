 package game;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import ia.IA;
 import physics.CarProperties;
 import physics.EnginePhysics;
 import physics.tools.MathTools;
 import audio.AudioRender;
 import audio.SoundStore;
 
 import com.jme3.asset.AssetManager;
 import com.jme3.bounding.BoundingBox;
 import com.jme3.bullet.collision.shapes.CollisionShape;
 import com.jme3.bullet.control.VehicleControl;
 import com.jme3.bullet.util.CollisionShapeFactory;
 import com.jme3.math.FastMath;
 import com.jme3.math.Vector2f;
 import com.jme3.math.Vector3f;
 import com.jme3.renderer.queue.RenderQueue.ShadowMode;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial;
 
 public class Car extends VehicleControl {
 
 	private AssetManager assetManager;
 
 	private CarProperties properties;
 	private EnginePhysics enginePhysics;
 	private IA ia;
 
 	private Node carNode;
 
 	private Geometry chasis;
 	private float wheelRadius;
 	private float steeringValue = 0;
 
 	private double life = 100;
 	private String driverName;
 	
 	private long timerNos = 0;
 	private int nosCharge = 0;
 
 	public enum CarType {
 		BOT, PLAYER
 	};
 
 	private CarType type;
 
 	protected ParticuleMotor particuleMotor;
 	protected AudioRender audioRender;
 
 	// Ensures that the stop thread is not launched more than needed
 	private boolean willStop = false;
 	
 	private boolean nosEnabled = false;
 
 	public Car(AssetManager assetManager, CarProperties properties, String scene) {
 		super();
 		this.assetManager = assetManager;
 		this.properties = properties;
 		this.enginePhysics = new EnginePhysics(properties);
 		this.ia = new IA(this, enginePhysics);
 
 		this.driverName = "Unknown";
 		this.type = CarType.BOT;
 
 		buildCar(scene);
 		buildParticuleMotor();
 		buildAudioRender();
 	}
 
 	private void buildParticuleMotor() {
 		// Init particule motor
 		particuleMotor = new ParticuleMotor(assetManager);
 	}
 
 	private void buildAudioRender() {
 		audioRender = new AudioRender(carNode, SoundStore.getInstance());
 	}
 
 	private void buildCar(String scene) {
 		float stiffness = properties.getStiffness();// 200=f1 car
 		float compValue = properties.getCompValue(); // (lower than damp!)
 		float dampValue = properties.getDampValue();
 		final float mass = properties.getMass();
 
 		this.setMass(mass);
 
 		// Load model and get chassis Geometry
 		carNode = (Node) assetManager.loadModel(scene);
 		carNode.setShadowMode(ShadowMode.Cast);
 
 		// Create a hull collision shape for the chassis
 		chasis = findGeom(carNode, "Car");
 		BoundingBox box = (BoundingBox) chasis.getModelBound();
 		CollisionShape carHull = CollisionShapeFactory
 				.createDynamicMeshShape(chasis);
 		this.setCollisionShape(carHull);
 
 		// Create a vehicle control
 		carNode.addControl(this);
 
 		// Setting default values for wheels
 		this.setSuspensionCompression(compValue * 2.0f
 				* FastMath.sqrt(stiffness));
 		this.setSuspensionDamping(dampValue * 2.0f * FastMath.sqrt(stiffness));
 		this.setSuspensionStiffness(stiffness);
 		this.setMaxSuspensionForce(10000);
 
 		// Create four wheels and add them at their locations
 		// note that our fancy car actually goes backwards..
 		Vector3f wheelDirection = new Vector3f(0, -1, 0);
 		Vector3f wheelAxle = new Vector3f(-1, 0, 0);
 
 		Geometry wheel_fr = findGeom(carNode, "WheelFrontRight");
 		wheel_fr.center();
 		box = (BoundingBox) wheel_fr.getModelBound();
 		wheelRadius = box.getYExtent();
 		float back_wheel_h = (wheelRadius * 1.7f) - 1f;
 		float front_wheel_h = (wheelRadius * 1.9f) - 1f;
 		this.addWheel(wheel_fr.getParent(),
 				box.getCenter().add(0, -front_wheel_h, 0), wheelDirection,
 				wheelAxle, 0.2f, wheelRadius, true);
 
 		Geometry wheel_fl = findGeom(carNode, "WheelFrontLeft");
 		wheel_fl.center();
 		box = (BoundingBox) wheel_fl.getModelBound();
 		this.addWheel(wheel_fl.getParent(),
 				box.getCenter().add(0, -front_wheel_h, 0), wheelDirection,
 				wheelAxle, 0.2f, wheelRadius, true);
 
 		Geometry wheel_br = findGeom(carNode, "WheelBackRight");
 		wheel_br.center();
 		box = (BoundingBox) wheel_br.getModelBound();
 		this.addWheel(wheel_br.getParent(),
 				box.getCenter().add(0, -back_wheel_h, 0), wheelDirection,
 				wheelAxle, 0.2f, wheelRadius, false);
 
 		Geometry wheel_bl = findGeom(carNode, "WheelBackLeft");
 		wheel_bl.center();
 		box = (BoundingBox) wheel_bl.getModelBound();
 		this.addWheel(wheel_bl.getParent(),
 				box.getCenter().add(0, -back_wheel_h, 0), wheelDirection,
 				wheelAxle, 0.2f, wheelRadius, false);
 
 		this.getWheel(0).setFrictionSlip(11f);
 		this.getWheel(1).setFrictionSlip(11f);
 		this.getWheel(2).setFrictionSlip(10f);
 		this.getWheel(3).setFrictionSlip(10f);
 	}
 
 	private Geometry findGeom(Spatial spatial, String name) {
 		if (spatial instanceof Node) {
 			Node node = (Node) spatial;
 			for (int i = 0; i < node.getQuantity(); i++) {
 				Spatial child = node.getChild(i);
 				Geometry result = findGeom(child, name);
 				if (result != null) {
 					return result;
 				}
 			}
 		} else if (spatial instanceof Geometry) {
 			if (spatial.getName().startsWith(name)) {
 				return (Geometry) spatial;
 			}
 		}
 		return null;
 	}
 
 	public Geometry getChassis() {
 		return findGeom(carNode, "Car");
 	}
 
 	public Node getNode() {
 		return carNode;
 	}
 
 	public CarProperties getProperties() {
 		return properties;
 	}
 
 	public EnginePhysics getEnginePhysics() {
 		return enginePhysics;
 	}
 
 	public IA getIA() {
 		return ia;
 	}
 
 	public float getSteeringValue() {
 		return steeringValue;
 	}
 
 	public void setSteeringValue(float steeringValue) {
 		this.steeringValue = (float) ((steeringValue <= 0.5) ? steeringValue
 				: 0.5);
 		this.steeringValue = (float) ((steeringValue >= -0.5) ? steeringValue
 				: -0.5);
 	}
 
 	public void increaseLife(double value) {
 		life += value;
 		if (life > 100)
 			life = 100;
 	}
 
 	public void decreaseLife(double value) {
 		life -= value;
 		if (life < 0)
 			life = 0;
 		if (life == 0) {
 			if (!particuleMotor.getBurstEnabled())
 				explode();
 		}
 	}
 
 	public int getLife() {
 		return (int) life;
 	}
 
 	public void setDriverName(String name) {
 		this.driverName = name;
 	}
 
 	public String getDriverName() {
 		return driverName;
 	}
 
 	public void setType(CarType type) {
 		this.type = type;
 	}
 
 	public CarType getType() {
 		return type;
 	}
 
 	public boolean inFront(Car c) {
 		Vector3f vect3 = getPhysicsLocation().subtract(c.getPhysicsLocation());
 		Vector2f vect = new Vector2f(vect3.x, vect3.z);
 
 		Vector3f ref = new Vector3f().subtract(this.getForwardVector(null));
 		Vector2f referenceOrientation = new Vector2f(ref.x, ref.z);
 
 		float angle = Math.abs(MathTools.orientedAngle(vect,
 				referenceOrientation));
 		if (angle <= Math.PI / 2)
 			return true;
 		else
 			return false;
 	}
 
 	public boolean getBurstEnabled() {
 		return particuleMotor.getBurstEnabled();
 	}
 	
 	public void addNos()	{
 		if (!nosEnabled)	{
 			// Vérifier qu'il reste une charge
 			if (nosCharge > 0)	{
 				nosEnabled = true;
 				nosCharge--;
 				enginePhysics.activeNos();
 				particuleMotor.addNos(carNode);
 
 				enginePhysics.activeNos();
 				
 				timerNos = System.currentTimeMillis();
 			}
 		}	
 	}
 
 	public void stopNos()	{
 		if (nosEnabled)	{
 			nosEnabled = false;
 			particuleMotor.removeNos(carNode);
 			timerNos = 0;
 			
 			enginePhysics.stopNos();
 		}
 	}
 	public void controlNos()	{
 		if (nosEnabled && System.currentTimeMillis() - timerNos > 2000)	{
 			particuleMotor.removeNos(carNode);
			enginePhysics.stopNos();
			
 			nosEnabled = false;			
 			timerNos = 0;
 		}
 	}
 	
 	public boolean getNosActivity()	 {
 		return nosEnabled;
 	}
 	
 	public void setNosCharge(int nombre)	{
 		nosCharge = nombre;
 	}
 
 	public void explode() {
 		particuleMotor.addExplosion(carNode);
 		enginePhysics.setBreaking(true);
 		audioRender.playBurst();
 		stop(1000);
 	}
 
 	public void removeExplosion() {
 		particuleMotor.removeExplosion(carNode);
 	}
 
 	public void updateSound(int rpm) {
 		audioRender.setRPM(rpm);
 	}
 
 	public void updateSound() {
 		audioRender.setRPM(enginePhysics.getRpm());
 	}
 
 	/**
 	 * Stops the car after a time delay Asynchronous method : will start a
 	 * thread not to block the rest of the application
 	 * 
 	 * @param delay
 	 *            the delay in ms
 	 */
 	public void stop(int delay) {
 		if (!willStop) {
 			willStop = true;
 			Timer timer = new Timer();
 			timer.schedule(new TimerTask() {
 				public void run() {
 					accelerate(0);
 					setLinearVelocity(Vector3f.ZERO);
 //					audioRender.mute();
 					willStop = false;
 				}
 			}, delay);
 		}
 	}
 }
