 package feynstein.collision;
 
 import feynstein.*;
 import feynstein.geometry.*;
 import feynstein.utilities.Vector3d;
 import feynstein.properties.*;
 
 import java.util.*;
 
 public abstract class NarrowPhaseDetector<E extends NarrowPhaseDetector> extends Property<E> {
     
     protected Mesh mesh;
     protected Scene scene;
     protected String name;
 
     protected final BoundingVolumeHierarchy bvh;
     protected final boolean enableBvh;
 
     protected HashSet<Collision> actualCollisions;
 
     public NarrowPhaseDetector(Scene aScene) {
 	super(aScene);
 	actualCollisions = new HashSet<Collision>();
 
 	scene = aScene;
 	mesh = scene.getMesh();
 
 	bvh = scene.getProperty(BoundingVolumeHierarchy.class);
 	enableBvh = bvh != null;
     }
 
     public abstract HashSet<Collision> checkCollision(TrianglePair p, HashSet<Collision> cSet);
 
     public HashSet<Collision> getCollisions() {
 	return actualCollisions;
     }
 
     @SuppressWarnings("unchecked")
    public E set_name(String aName) {
 	name = aName;
 	return (E) this;
     }
 
     public String getName() {
 	return name;
     }
 
     protected double getVertex(Triangle t, int vertex, int axis) {
 	return mesh.getVert(t.getIdx(vertex)).get(axis);
     }
 
     public void update() {
 	actualCollisions.clear();
 
 	if (enableBvh) {
 	    List<TrianglePair> collisions = bvh.getCollisions();
 	    for (TrianglePair pair : collisions)
 		checkCollision(pair, actualCollisions);
 		//(checkCollision adds the collision to the given set as a side effect
 	} else {
 	    Triangle t1, t2;
 	    for (int i = 0; i < mesh.getTriangles().size(); i++) {
 		for (int j = i+1; j < mesh.getTriangles().size(); j++) {
 		    t1 = mesh.getTriangles().get(i);
 		    t2 = mesh.getTriangles().get(j);
 		    checkCollision(new TrianglePair(t1, t2), actualCollisions);
 		}
 	    }
 	}
 	
 	//TESTING PURPOSES ONLY:
 	for (Collision c : actualCollisions)
 	    System.out.println(c.toString());
     }
 
 }
