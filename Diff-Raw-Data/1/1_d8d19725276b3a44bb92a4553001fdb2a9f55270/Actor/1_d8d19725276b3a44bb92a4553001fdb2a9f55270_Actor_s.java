 package actor;
 
 import graphics.Model;
 
 import java.io.Serializable;
 import java.util.Collections;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Random;
 
 import javax.media.opengl.GL2;
 
 import physics.GJKSimplex;
 
 import math.Quaternion;
 import math.Supportable;
 import math.Vector3;
 
 public abstract class Actor implements Serializable, Supportable, Rotatable, Velocitable, Positionable, Collidable {
     private static final long serialVersionUID = 744085604446096658L;
     /**
      * All the actors currently in play We use the fully qualified named space
      * for the Vector container so it doesn't clash with our name space. Vectors
      * work like ArrayLists, but are synchronized.
      */
     private static List<Actor> actors = Collections.synchronizedList(new java.util.ArrayList<Actor>());
     /**
      * Common random number generator object
      */
     protected static Random gen = new Random();
     private static int lastId = 0;
 
     /**
      * Thread safe way to remove actors
      * @param idToRemove
      */
     public static void removeActorId(int idToRemove) {
         synchronized(actors) {
             for (ListIterator<Actor> it = actors.listIterator(); it.hasNext(); ) {
                 Actor a = it.next();
                 if (a.getId() == idToRemove)
                     it.remove();
             }
         }
     }
 
     public static void addActor(Actor actor) {
         synchronized(actors) {
             actors.add(actor);
             System.out.println("There Are: " + actors.size() + " actors");
         }
     }
 
     public static void removeActor(Actor actor) {
         synchronized(actors) {
             actors.remove(actor);
         }
     }
 
     /**
      * 
      * @param frames the number of frames since the last update
      */
     public static void updateActors(int frames) {
         synchronized(actors) {
             // Update each actor
             for (Actor a : actors) {
                 // Track down actors without ids.
                 if (a.getId() == 0)
                     System.err.println("DEBUG: " + a + " actor without ID set");
                 a.update();
             }
         }
     }
 
     
     /**
      * Helper method to get rid of stupid syntax
      * @param other the other actor to test collision with
      * @return true if colliding, else false
      */
     public boolean isColliding(Actor other){
         // Don't collide with our children like lasers or bullets or missiles
         if(parentId == other.id || other.parentId == id)
             return false;
             
         if (isPossiblyColliding(other)) // do a cheap bounding sphere test before resorting to GJK
             return GJKSimplex.isColliding(this, other);
         return false;
     }
 
     /**
      * Simple bounding sphere test
      * @param other other actor to test collision with
      * @return if a collision is possible
      */
     private boolean isPossiblyColliding(Actor other) {
         Vector3 delta_p = other.position.minus(position);
         float collisionRadius = other.velocity.minus(velocity).magnitude();
         collisionRadius += getRadius();
         collisionRadius += other.getRadius();
                 
         return (delta_p.magnitude2() <= collisionRadius * collisionRadius);
     }
 
     /**
      * Update all the actors
      * @author Dustin Lundquist <dustin@null-ptr.net>
      * @param update a list of updates
      * @param ship the players ship or similar actor that should not be updated
      */
     public static void updateFromNetwork(List<Actor> update, Actor ship) {
         synchronized(actors) {
             // This is n^2 - but I don't have a better way to do it
             // Using ListEterators so we only need to make one pass adding and removing elements
             for (ListIterator<Actor> actors_iter = actors.listIterator(); actors_iter.hasNext(); ) {
                 Actor a = actors_iter.next();
                 boolean found = false;
 
                 for (ListIterator<Actor> update_iter = update.listIterator(); update_iter.hasNext(); ) {
                     Actor u = update_iter.next();
 
                     if (a.id != u.id)
                         continue;
 
                     // Do not update the players ship position from the network
                     if (ship != null && u.id == ship.id)
                         continue;
 
                     actors_iter.set(u);
                     update_iter.remove();
                     found = true;
                     break;
                 }
                 // Skip the last step if running on the server
                 if (ship == null)
                     continue;
 
                 // Remove actors that where not present in the update, except for the players ship
                 if (!found && a != ship)
                     actors_iter.remove();
             }
             // Add the remaining
             actors.addAll(update);
         }
 
     }
 
     public static int getActorCount() {
         synchronized(actors) {
             return actors.size();
         }
     }
 
     private int id; // unique ID for each Actor
     protected String modelName;
     private transient Model model; // CL - Used to store the model reference
     // after we look it up once
     protected Vector3 position, velocity, scale;
 
     // Rotation
     protected Quaternion rotation, angularVelocity;
 
     // protected int age; // Actor age in frames
      protected int parentId;
 
     public Actor() {
         id = generateId();
         rotation = new Quaternion();
         angularVelocity = new Quaternion();
         position = new Vector3();
         velocity = new Vector3();
         scale = new Vector3(1.0f,1.0f,1.0f);
         age = 0;
         //sets the time of the actor's birth 
         setTimeStamp();
     }
 
     public void changeYaw(float degrees) {
         angularVelocity = angularVelocity.times(new Quaternion(rotation
                 .yawAxis(), degrees));
     }
 
     public void changePitch(float degrees) {
         angularVelocity = angularVelocity.times(new Quaternion(rotation
                 .pitchAxis(), degrees));
     }
 
     public void changeRoll(float degrees) {
         angularVelocity = angularVelocity.times(new Quaternion(rotation
                 .rollAxis(), degrees));
     }
 
     protected void delete() {
         synchronized(actors) {
             actors.remove(this);
         }      
     }
 
     protected int generateId() {
         return (lastId += gen.nextInt(1000) + 1); // Pseudo random increments
     }
 
     public Vector3 getDirection() {
         return rotation.rollAxis();
     }
 
     public Model getModel() {
         // CL - If our reference is null, go look it up
         if (model == null)
             model = Model.findOrCreateByName(modelName);
 
         return model;
     }
 
     /**
      * @return the actors current position
      */
     public Vector3 getPosition() {
         return position;
     }
 
     public Actor setRotation(Quaternion rot){
         rotation = rot;
         return this;
     }
 
     public Quaternion getRotation() {
         return rotation;
     }
 
     /**
      * @return the actors size (for texture scaling and collision detection)
      */
     public Vector3 getSize() {
         return scale;
     }
 
     /**
      * 
      * @return the actors current velocity
      */
     public Vector3 getVelocity() {
         return velocity;
     }
 
     /**
      * Synchronize all access to the actors list
      * @return
      */
     public static List<Actor> getActors() {
         return actors;
     }
 
     /**
      * Call back upon collision detection for object to handle collision It
      * could... Bounce off Explode into many smaller objects Just explode
      * 
      * @param other
      *            the object this actor collided with
      */
     abstract public void handleCollision(Actor other);
 
     public void render(GL2 gl) {
         gl.glPushMatrix();
         // Translate the actor to it's position
         gl.glTranslatef(position.x, position.y, position.z);
 
         // Rotate the actor
         gl.glMultMatrixf(getRotation().toGlMatrix(), 0);
         // Scale the Actor
         gl.glScalef(scale.x, scale.y, scale.z);
         // CL - Render our model.
         getModel().render(gl);
         gl.glPopMatrix();
     }
 
     public Actor setPosition(Vector3 position) {
         this.position = position;
         return this;
     }
 
     // Lets you reference chain
     public Actor setSize(float size) {
         scale.x = size;
         scale.y = size;
         scale.z = size;
         return this;
     }
 
     public Actor setSize(Vector3 size){
         scale = size;
         return this;
     }
 
     public Actor setVelocity(Vector3 velocity) {
         this.velocity = velocity;
         return this;
     }
 
 
     public Vector3 getFarthestPointInDirection(Vector3 direction){
         // CL - put it into world space by translating it and rotating it
         // CL - NOTE we have to push the inverse of our transform of the direction
         //      so we can figure it out in model space
 
         // CL - We need to do the sweeping further point so we need to see if we want where we
         // are or where we will be is better
 
 
 
         Vector3 max = getModel().getFarthestPointInDirection(direction.times(getRotation().inverse()));
         // Scale the point by our actor's scale in world space
         max.x *= scale.x;
         max.y *= scale.y;
         max.z *= scale.z;
 
         // Rotate and translate our point to world space
         max = max.times(getRotation()).plus(getPosition());
 
         // If our velocity is in the same direction as the direction, then
         // we need to sweep the furthest point by our velocity
         if(velocity.sameDirection(direction))
             max.plusEquals(velocity);
         // Do the same thing for angular velocity
         //if(max.times(rotation.times(getAngularVelocity())).dotProduct(direction) > max.dotProduct(direction))
         //   max = max.times(getAngularVelocity());
 
         return max;
     }
 
     // CL - updates the state of the actor for the next frame
     public void update() {
         position.plusEquals(velocity);
         rotation.normalize();
 
         // This should also take into effect our maximum angular velocity --
         // this may be an overridden in subclasses to provide different handling
         rotation.timesEquals(angularVelocity);
         age++;
     }
 
     protected void dampenAngularVelocity() {
         angularVelocity = angularVelocity.dampen(0.01f);
     }
 
     // FIXME this is a linear time search
     public static Actor findById(int id) {
         synchronized(actors) {
             for (Actor a: actors)
                 if (a.getId() == id)
                     return a;
         }
         return null;
     }
 
     public int getId() {
         return id;
     }
     
     public float getRadius() {
         // TODO optimize: either cache, or change scale to a float
         return getModel().radius * Math.max(scale.x, Math.max(scale.y, scale.z));
     }
 
     public Quaternion getAngularVelocity() {
         return angularVelocity;
     }
 
     protected long age;
 
     /**
      * Sets the time when the Actor was born
      * Current uses System.currentTimeMillis, this might be problematic on different OS
      * Should be changed to deal with FPS from open gl
      * 
      */
     protected void setTimeStamp() {
         age = System.currentTimeMillis();       
     }
 
     protected long getAge() {
         return age;
     }
 
     public void setModel(Model model) {
         this.model = model;
     }
 
     public String getModelName(){
         return modelName;
     }
 }
