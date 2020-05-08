 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package physics;
 
 import game.*;
 import java.util.ArrayList;
 import org.lwjgl.util.vector.Vector3f;
 import util.DebugMessages;
 import util.Utilities;
 
 /**
  *
  * @author Andy
  */
 public class ThreeDPhysicsManager extends StandardManager implements update.Updateable {
 
     ArrayList<PhysicalEntity> pe;
     CollisionMesh collisionMesh;
     float restitution = 0.2f;
     static ThreeDPhysicsManager instance;
 
     @Override
     public void create() {
         super.create();
         pe = new ArrayList<PhysicalEntity>();
     }
 
     public static ThreeDPhysicsManager getInstance() {
         if (instance == null) {
             instance = new ThreeDPhysicsManager();
         }
         return instance;
     }
 
     @Override
     public boolean update(int delta) {
 
         DebugMessages.getInstance().write("Physics starting");
 
         //Motion update
         float division = 3;
         float collision = 3;
         float time = delta / 1000f / division;
         for (int j = 0; j < division; j++) {
             for (PhysicalEntity e : pe) {
 
                 e.setAwake(!e.canSleep());
                 e.updateForces();
                 if (e.isAwake()) {
                     e.integrate(time);
                 }
 
             }
 
             ArrayList<PhysicalEntity> pcopy = new ArrayList<PhysicalEntity>(pe);
             for (PhysicalEntity e : pe) {
                 if (collisionMesh != null && !collisionMesh.isEmpty()) {
                     ArrayList<Plane> cols = collisionMesh.getPlanes(e);
                     if (!cols.isEmpty()) {
                         Vector3f[] vertices;
                         for (int i = 0; i < collision; i++) {
                             vertices = e.getBounds().getGlobalVertices();
                             for (Vector3f v : vertices) {
                                 float highestVelocity = Float.MAX_VALUE;
                                 Plane toResolve = null;
                                 for (Plane p : cols) {
                                     float distance = p.getDistance(v);
                                     if (distance > 0) {
                                         continue;
                                     }
 
                                     Vector3f pointVelocity = Vector3f.add(e.getVelocity(), Vector3f.cross(Utilities.transform(e.getAngularVelocity(), e.getOrientation()), Vector3f.sub(v, e.getPosition(), null), null), null);
                                     float seperatingVelocity = Vector3f.dot(pointVelocity, p.getNormal());
                                     if (seperatingVelocity < highestVelocity) {
                                         highestVelocity = seperatingVelocity;
                                         toResolve = p;
                                     }
                                 }
 
                                 if (highestVelocity >= 0) {
                                     continue;
                                 }
 
                                 float newVelocity = -highestVelocity;
 
                                 Vector3f accCausedVelocity = e.getAcceleration();
                                 float accCausedSepVelocity =
                                         Vector3f.dot(accCausedVelocity,
                                         toResolve.getNormal()) * time;
                                 if (accCausedSepVelocity < 0) {
                                     newVelocity +=
                                             accCausedSepVelocity;
                                     if (newVelocity < 0) {
                                         newVelocity = 0;
                                     }
                                 }
 
                                 newVelocity *= restitution;
 
                                 float deltaV = newVelocity - highestVelocity;
                                 Vector3f normal = new Vector3f(toResolve.getNormal());
                                 normal.scale(deltaV * e.getMass() / 10f);
                                 e.applyImpulseAtPoint(normal, v);
                             }
                         }
                         for (int i = 0; i < collision; i++) {
                             vertices = e.getBounds().getGlobalVertices();
                             for (Vector3f v : vertices) {
                                 float highestPenetration = Float.MAX_VALUE;
                                 Plane toResolve = null;
                                 for (Plane p : cols) {
                                     float distance = p.getDistance(v);
                                     if (distance < highestPenetration) {
                                         highestPenetration = distance;
                                         toResolve = p;
                                     }
                                 }
                                 if (highestPenetration > 0) {
                                     continue;
                                 }
                                 e.setPosition(Utilities.addScaledVector(e.getPosition(), toResolve.getNormal(), -1f * highestPenetration));
                             }
                         }
                         e.collide(cols);
                     }
                 }
                 pcopy.remove(e);
 
                 for (PhysicalEntity other : pcopy) {
                     if (e.getBounds().intersects(other.getBounds())) {
                         e.collide(other);
                         other.collide(e);
                     }
                 }
             }
         }
 
         DebugMessages.getInstance().write("Physics finished");
         
         return false;
     }
 
     @Override
     public boolean add(GameObject obj) {
         if (obj instanceof PhysicalEntity) {
             pe.add((PhysicalEntity) obj);
             return true;
         }
         return false;
     }
 
     @Override
     public void remove(GameObject obj) {
         if (pe.contains(obj)) {
             pe.remove(obj);
         }
     }
 
     public void setCollisionMesh(CollisionMesh cm) {
         this.collisionMesh = cm;
     }
    
    public CollisionMesh getCollisionMesh() {
        return collisionMesh;
    }
 }
