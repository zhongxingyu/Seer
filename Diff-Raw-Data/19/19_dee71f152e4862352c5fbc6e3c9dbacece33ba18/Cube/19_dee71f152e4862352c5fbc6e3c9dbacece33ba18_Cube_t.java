 package cge.zeppelin;
 
 import javax.vecmath.Vector3f;
 
 import com.bulletphysics.collision.dispatch.CollisionObject;
 import com.bulletphysics.collision.shapes.CollisionShape;
 import com.bulletphysics.dynamics.RigidBody;
 import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
 
 import de.bht.jvr.core.GroupNode;
 import de.bht.jvr.core.SceneNode;
 
 public class Cube extends Entity{
 
 	Cube(GroupNode n, CollisionShape s, float m) {
         shape = s;
         node = n;
         mass = m;
         
         if (shape != null) {
             Vector3f inertia = new Vector3f(0, 0, 0);
            shape.calculateLocalInertia(mass, inertia);
 
            RigidBodyConstructionInfo info = new RigidBodyConstructionInfo(mass, this, shape,
                     inertia);
             info.restitution = 0.4f;
             info.linearDamping = 0.2f;
             info.angularDamping = 0.2f;
 
             body = new RigidBody(info);
             body.setUserPointer(this);
 
             if (mass == 0.0f)
                 body.setActivationState(CollisionObject.DISABLE_DEACTIVATION);
         } else {
             body = null;
         }
 	}
 
 }
