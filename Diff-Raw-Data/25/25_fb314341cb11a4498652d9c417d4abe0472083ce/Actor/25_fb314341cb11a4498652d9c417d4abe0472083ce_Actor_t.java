 package actor;
 import math.*;
 import graphics.Model;
 import java.io.Serializable;
 import java.util.Random;
 import java.util.Vector;
 import javax.media.opengl.GL2;
 
 public abstract class Actor implements Serializable {
     private static final long serialVersionUID = 744085604446096658L;
     /**
      *  All the actors currently in play
      *  We use the fully qualified named space for the Vector container so 
      *  it doesn't clash with our name space. Vectors work like ArrayLists,
      *  but are synchronized.
      */
     public static Vector<Actor> actors = new Vector<Actor>();
     /**
      * Common random number generator object
      */
     protected static Random gen = new Random();
     private static int lastId = 0;
 
     public static void removeActorId(int idToRemove) {
         for (Actor a: actors)
             if (a.id == idToRemove)
                 actors.remove(a);
     }
 
     public static void updateActors() {
         // Update each actor
         for(int i = 0; i < actors.size(); i++) {
             // We get the actor only once in case we the actor is removed
             // during the update phase. E.G. Bullets FramesToLive reaches 0
             Actor a = actors.get(i);
 
             // Track down actors without ids.
             if (a.id == 0)
                 System.err.println("DEBUG: " + a + " actor without ID set");
 
             a.update();
         }
     }
 
 
     protected int id; // unique ID for each Actor 
     protected int modelId;
     protected transient Model model; // CL - Used to store the model reference after we look it up once
     protected Vector3 position, velocity;
     protected float scale;
 
     // Rotation
     protected Quaternion rotation, angularVelocity;
 
     protected int age; // Actor age in frames
     protected int parentId;
 
 
     public Actor(){
         id = generateId();
         rotation = new Quaternion();
         angularVelocity = new Quaternion();
         position = new Vector3();
         velocity = new Vector3();
         modelId = Model.getModelIdFor(this);
     }
 
     public void changeYaw(float degrees) {
         angularVelocity = angularVelocity.times(new Quaternion(rotation.yawAxis(), degrees));
     }
 
     public void changePitch(float degrees)   {
         angularVelocity = angularVelocity.times(new Quaternion(rotation.pitchAxis(), degrees));
     }
 
     public void changeRoll(float degrees)   {
         angularVelocity = angularVelocity.times(new Quaternion(rotation.rollAxis(), degrees));
     }
 
     protected void delete() {
         // NOTE: This needs to be thread safe.
         Actor.actors.remove(this);
     }
 
     protected int generateId() {
         return (lastId += gen.nextInt(1000) + 1); // Pseudo random increments
     }
 
     public Vector3 getDirection(){
         // FIXME this often points in a negative direction
         return rotation.rollAxis();
     }
 
     public Model getModel() {
         // CL - If our reference is null, go look it up
         if(model == null)
             model = Model.findById(modelId);
 
         return model;
     }
 
     /**
      * @return the actors current position
      */
     public Vector3 getPosition() {
         return position;
     }
 
     public Quaternion getRotation() {
         return rotation;
     }
 
     /**
      * @return the actors size (for texture scaling and collision detection)
      */
     public float getSize(){
         return scale;
     }
 
     /**
      * 
      * @return the actors current velocity
      */
     public Vector3 getVelocity(){
         return velocity;
     }
 
     /**
      * Call back upon collision detection for object to handle collision
      * It could...
      *   Bounce off
      *   Explode into many smaller objects
      *   Just explode
      * @param other the object this actor collided with
      */
     abstract public void handleCollision(Actor other);
 
     public void render(GL2 gl) {
        gl.glPushMatrix();
         // Translate the actor to it's position
         gl.glTranslatef(position.x, position.y, position.z);
 
         // Rotate the actor
         gl.glMultMatrixf(getRotation().toGlMatrix(), 0);
         // Scale the Actor
 
         // CL - Render our model.
         getModel().render(gl);
        gl.glPopMatrix();
     }
 
     public void setPosition(Vector3 position) {
         this.position = position;
     }
 
     // Lets you reference chain
     public Actor setSize(float newSize){
         scale = newSize;
         return this;
     }
     public void setVelocity(Vector3 velocity) {
         this.velocity = velocity;
     }
 
     // CL - updates the state of the actor for the next frame
     public void update(){
         position.plusEquals(velocity);
         rotation.normalize();
         dampenAngularVelocity();
         // This should also take into effect our maximum angular velocity -- this may be an overridden in subclasses to provide different handling
         rotation = rotation.times(angularVelocity);
     }
 
     private void dampenAngularVelocity() {
         angularVelocity = angularVelocity.dampen(0.01f);
 
     }
 }
 
