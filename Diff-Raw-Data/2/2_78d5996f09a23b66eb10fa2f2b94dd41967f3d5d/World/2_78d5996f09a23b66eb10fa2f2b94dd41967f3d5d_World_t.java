 package tesseract;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.media.j3d.BoundingBox;
 import javax.media.j3d.BoundingLeaf;
 import javax.media.j3d.BoundingSphere;
 import javax.media.j3d.BranchGroup;
 import javax.media.j3d.DirectionalLight;
 import javax.media.j3d.Group;
 import javax.media.j3d.IndexedLineArray;
 import javax.media.j3d.Light;
 import javax.media.j3d.Node;
 import javax.media.j3d.Shape3D;
 import javax.vecmath.Color3f;
 import javax.vecmath.Point3d;
 import javax.vecmath.Vector3f;
 
 import tesseract.forces.Force;
 import tesseract.objects.HalfSpace;
 import tesseract.objects.Particle;
 import tesseract.objects.PhysicalObject;
 
 import common.CollidableObject;
 import common.CollisionDetector;
 import common.CollisionInfo;
 import common.Peer;
 import common.PeerInformation;
 
 /**
  * Model of the 3D world.
  * 
  * @author Jesse Morgan
  */
 public class World implements Observer {
 	/**
 	 * Root element of the world.
 	 */
 	private BranchGroup myScene;
 
 	/**
 	 * Bounding box of the world.
 	 */
 	private BoundingBox myVirtualWorldBounds;
 	
 	/**
 	 * A list of the objects in the world. 
 	 */
 	private List<PhysicalObject> myObjects;
 	
 	/**
 	 * A list of the forces in the world.
 	 */
 	private List<Force> myForces;
 	
 	/**
 	 * The peer object for this world.
 	 */
 	private Peer myPeer;
 	
 	/**
 	 * Update rate for the world.
 	 */
 	private static final int UPDATE_RATE = 30;
 	
 	/**
 	 * Top HalfSpace.
 	 */
 	private HalfSpace my_top;
 	
 	/**
 	 * Bottom HalfSpace
 	 */
 	private HalfSpace my_bottom;
 	
 	/**
 	 * Side of HalfSpace for transmission decisions.
 	 */
 	private HalfSpace my_side1;
 	
 	/**
 	 * Side of HalfSpace for transmission decisions.
 	 */
 	private HalfSpace my_side2;
 	
 	/**
 	 * Side of HalfSpace for transmission decisions.
 	 */
 	private HalfSpace my_side3;
 	
 	/**
 	 * Side of HalfSpace for transmission decisions.
 	 */
 	private HalfSpace my_side4;
 	
 	/**
 	 * Create a new world.
 	 * 
 	 * @param bounds The bounding box of the world.
 	 */
 	public World(final BoundingBox bounds, final Peer peer) {
 		myVirtualWorldBounds = bounds;
 		myPeer = peer;
 		myPeer.addObserver(this);
 		
 		myForces = new LinkedList<Force>();
 		myObjects = new LinkedList<PhysicalObject>();
 		
 		// TODO: Should this go here?
 		myScene = new BranchGroup();
 		myScene.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
 		myScene.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
 		
 		BoundingLeaf originLeaf = new BoundingLeaf(new BoundingSphere());
 		myScene.addChild(originLeaf);
 		
 		myScene.addChild(createVirtualWorldBoundsShape());
 		
 		addLights();
 		addHalfspaces();
 		
 		myScene.compile();
 	}
 
 	/**
 	 * Create the visual bounding box around the world.
 	 * 
 	 * @return A shape of the bounding box.
 	 */
 	private Node createVirtualWorldBoundsShape() {
 		Point3d lower = new Point3d();
 		Point3d upper = new Point3d();
 		myVirtualWorldBounds.getLower(lower);
 		myVirtualWorldBounds.getUpper(upper);
 		
 		double[] coordinates = { lower.x, lower.y, lower.z, upper.x, lower.y,
 				lower.z, upper.x, lower.y, upper.z, lower.x, lower.y, upper.z,
 				lower.x, upper.y, lower.z, upper.x, upper.y, lower.z, upper.x,
 				upper.y, upper.z, lower.x, upper.y, upper.z };
 		
 		int[] coordinateIndices = { 0, 1, 1, 2, 2, 3, 3, 0, 4, 5, 5, 6, 6, 7,
 				7, 4, 0, 4, 1, 5, 2, 6, 3, 7 };
 		
 		IndexedLineArray geometry = new IndexedLineArray(
 				coordinates.length / 3, IndexedLineArray.COORDINATES,
 				coordinateIndices.length);
 		
 		geometry.setCoordinates(0, coordinates);
 		geometry.setCoordinateIndices(0, coordinateIndices);
 		
 		return new Shape3D(geometry);
 	}
 	
 	private void addHalfspaces() {
 		Point3d lower = new Point3d();
 		Point3d upper = new Point3d();
 		myVirtualWorldBounds.getLower(lower) ;
 		myVirtualWorldBounds.getUpper(upper);
 		
 		// Bottom
 		my_bottom = new HalfSpace(new Vector3f(lower), new Vector3f(0, 1, 0));
 		myObjects.add(my_bottom);
 		
 		// Top
 		my_top = new HalfSpace(new Vector3f(upper), new Vector3f(0, -1, 0));
 		myObjects.add(my_top);
 		
 		// Sides         
 		my_side1 = new HalfSpace(new Vector3f(upper), new Vector3f(0, 0, -1));
 		myObjects.add(my_side1);
 		my_side2 = new HalfSpace(new Vector3f(upper), new Vector3f(-1, 0, 0));
 		myObjects.add(my_side2);
 		
 		my_side3 = new HalfSpace(new Vector3f(lower), new Vector3f(0, 0, 1));
 		myObjects.add(my_side3);
 		my_side4 = new HalfSpace(new Vector3f(lower), new Vector3f(1, 0, 0));
 		myObjects.add(my_side4);
 		
 	}
 
 	/**
 	 * Add some standard lights to the world.
 	 */
 	private void addLights() {
 		Light light = new DirectionalLight(
 				new Color3f(1f, 1f, 1f), new Vector3f(-1f, -1f, -1f));
 		
 		light.setInfluencingBounds(
 				new BoundingSphere(new Point3d(0, 0, 0), 10));
 		
 		myScene.addChild(light);
 		
 		light = new DirectionalLight(
 				new Color3f(0.3f, 0.1f, 0.1f), new Vector3f(1f, 0f, 0f));
 		
 		light.setInfluencingBounds(
 				new BoundingSphere(new Point3d(0, 0, 0), 10));
 		
 		myScene.addChild(light);
 	}
 	
 	/**
 	 * Update the state of the world.
 	 */
 	public void tick() {
 		// Iterate over objects in the world.
 		Iterator<PhysicalObject> itr = myObjects.iterator();
 	
 		List<PhysicalObject> children = new LinkedList<PhysicalObject>();
 		
 		while (itr.hasNext()) {
 			CollidableObject obj = itr.next();
 
 			// Apply forces
 			for (Force force : myForces) {
 				force.applyForceTo((PhysicalObject) obj);
 			}
 			
 			// Update the object's state.
 			obj.updateState(1f / UPDATE_RATE);
 			
 			// Spawn new objects?
 			List<PhysicalObject> newChildren =
 				((PhysicalObject) obj).spawnChildren(1f / UPDATE_RATE);
 			
 			if (newChildren != null) {
 				children.addAll(newChildren);
 			}
 		}
 		
 		/*
 		  In the "tick" method of your application, rather than call the old form of 
 		  resolveCollisions to completely handle a collision, you can now:
 		 
  		 	1.Directly call CollisionDetector.calculateCollisions to receive an
 			  ArrayList<CollisionInfo> object.
 			2.If the list is empty, then there is no collision between the pair
 			  of objects and nothing further need be done.
 			3.If the list is not empty, then a collision has occurred and you can
 			  check whether the objects involved necessitate a transmission or a standard
 			  collision resolution (a.k.a. bounce).
 			4.If a standard collision resolution is called for, use the new form of
 			   resolveCollisions to pass in the ArrayList<CollisionInfo> object.
 			   
 		  The goal of this change is to prevent the computationally expensive 
 		  collision detection algorithm from being executed twice when objects collide.
 		 */
 
 		for (int i = 0; i < myObjects.size() - 1; i++) {
 			for (int j = i + 1; j < myObjects.size(); j++) {
 				ArrayList<CollisionInfo> collisions = 
 					CollisionDetector.calculateCollisions(myObjects.get(i), myObjects.get(j));
 				
 				if (collisions.size() > 0) {
 					HalfSpace hs = null;
 					PhysicalObject o = null;
 					
 					if (myObjects.get(i) instanceof HalfSpace) {
 						// If i is a halfspace, j must be an object
 						hs = (HalfSpace) myObjects.get(i);
 						o = myObjects.get(j);
 						
 						
 					} else if (myObjects.get(j) instanceof HalfSpace) {
 						// If j is a halfspace, i must be an object
 						hs = (HalfSpace) myObjects.get(j);
 						o = myObjects.get(i);
 					}
 					
 					// Was there a halfspace involved? If so, was it a side?
					if (hs != null && hs.normal.y != 1 && hs.normal.y != -1 && myPeer.getPeerSize() > 0)  {
 						// Side collision, is there a peer?
 						PeerInformation peer = myPeer.getPeerInDirection(o.getVelocity().x, -o.getVelocity().z);
 						
 						if (peer != null) {
 							o.switchX();
 							o.switchZ();
 							myPeer.sendPayloadToPeer(peer, o);
 							o.detach();
 							myObjects.remove(o);
 							
 							// Moving on
 							continue;
 						}
 					}
 					
 					// Collision as usual...
 					myObjects.get(i).resolveCollisions(myObjects.get(j), collisions);
 				}
 			}
 		}
 
 		// Add new children to the world.
 		for (PhysicalObject obj : children) {
 			myScene.addChild(obj.getGroup());
 		}
 		
 		myObjects.addAll(children);
 	}
 	
 	/**
 	 * @return the root BG of the scene.
 	 */
 	public BranchGroup getScene() {
 		return myScene;
 	}
 	
 	/**
 	 * Add a new object to the world.
 	 * 
 	 * @param obj The object to add
 	 */
 	public void addObject(final PhysicalObject obj) {
 		myScene.addChild(obj.getGroup());
 		myObjects.add(obj);
 	}
 	
 	/**
 	 * Add a new force to the world.
 	 * 
 	 * @param force the force to add.
 	 */
 	public void addForce(final Force force) {
 		myForces.add(force);
 	}
 	
 	/**
 	 * Remove a force from the world.
 	 * 
 	 * @param force The force to remove.
 	 */
 	public void removeForce(final Force force) {
 		myForces.remove(force);
 	}
 	
 	/**
 	 * Remove all forces and objects from the world.
 	 */
 	public void resetWorld() {
 		myForces.clear();
 		
 		for (CollidableObject obj : myObjects) {
 			obj.detach();
 		}
 		myObjects.clear();
 		
 		addHalfspaces();
 	}
 
 	/**
 	 * Observer Callback.
 	 * Called when a PAYLOAD or EXTRA peer message is recieved.
 	 * 
 	 * @param peer The network peer.
 	 * @param obj The object from the network.
 	 */
 	public void update(final Observable peer, final Object obj) {
 		if (obj != null) {
 			if (obj instanceof PhysicalObject) {
 				addObject((PhysicalObject) obj);
 				
 			} else if (obj instanceof CollidableObject) {
 				addObject(new PhysicalObject((CollidableObject) obj));
 			}
 		}
 	}
 }	
