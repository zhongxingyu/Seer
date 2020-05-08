 package graphics;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import javax.media.opengl.GL2;
 import math.Vector4f;
 
 public class Light implements java.io.Serializable{
     private static final long serialVersionUID = -7133937204421075824L;
     protected static Random gen = new Random(42);
     public static List<Light> lights = new ArrayList<Light>();
     float constantAttenuation, linearAttenuation, quadraticAttenuation;
     math.Vector4f position, ambient, diffuse, specular;
 
     public static Light newRandom(float rangeMax){
         Light light = new Light();
         light.setPosition(Vector4f.newRandom(rangeMax));
         // Not a directional light
         light.position.t = 0.0f;
         return light;
     }
     public Light(){
         position = new Vector4f(0,0,0,1);
         ambient = new Vector4f(0,0,0,1);
         diffuse = new Vector4f(gen.nextFloat(),gen.nextFloat(),gen.nextFloat(),1);
         specular = new Vector4f(1,1,1,1);
         constantAttenuation = 1.0f;
        linearAttenuation = 1E-3f; // Should be really small: light is reduced by  1 / (distance * factor)
        quadraticAttenuation = 1E-7f; // Should be really small: light is reduced by  1 / (distance * factor^2)
     }
 
     public math.Vector4f getAmbient() {
         return ambient;
     }
 
     public float getConstantAttenuation() {
         return constantAttenuation;
     }
 
     public math.Vector4f getDiffuse() {
         return diffuse;
     }
 
     public float getLinearAttenuation() {
         return linearAttenuation;
     }
 
     public math.Vector4f getPosition() {
         return position;
     }
 
     public float getQuadraticAttenuation() {
         return quadraticAttenuation;
     }
 
     public math.Vector4f getSpecular() {
         return specular;
     }
 
     public Light setAmbient(math.Vector4f ambient) {
         this.ambient = ambient;
         return this;
     }
 
     public Light setConstantAttenuation(float constantAttenuation) {
         this.constantAttenuation = constantAttenuation;
         return this;
     }
 
     public Light setDiffuse(math.Vector4f diffuse) {
         this.diffuse = diffuse;
         return this;
     }
 
     public Light setLinearAttenuation(float linearAttenuation) {
         this.linearAttenuation = linearAttenuation;
         return this;
     }
 
     public Light setPosition(math.Vector4f position) {
         this.position = position;
         return this;
     }
 
     public Light setQuadraticAttenuation(float quadraticAttenuation) {
         this.quadraticAttenuation = quadraticAttenuation;
         return this;
     }
 
     public Light setSpecular(math.Vector4f specular) {
         this.specular = specular;
         return this;
     }
 
     public static void add(Light light) {
         lights.add(light);
     }
 
     public static void initialize(GL2 gl, int numLights){
         int[] maxLights = new int[1];
 
         // Make Sure lighting is turned on
         gl.glEnable(GL2.GL_LIGHTING);
 
         // Check to make sure we aren't enable more lights than the graphics card can support
         gl.glGetIntegerv(GL2.GL_MAX_LIGHTS, maxLights, 0);
 
         // Make sure we don't enable more lights than the graphics card can handle.
         if(numLights > maxLights[0]){
             System.err.println("Unable to support " + numLights + " lights. Truncating to " + maxLights[0]);
             numLights = maxLights[0];
         }
 
         for(int i = 0; i < numLights; i++){
             Light.add(Light.newRandom(Skybox.SKYBOX_SIZE));
             gl.glEnable(GL2.GL_LIGHT0 + i);
         }
     }
 
     /* From the Orange book
      * OpenGL implementations often choose to do lighting
      * calculations in eye space; therefore, the incoming surface normals have to
      * be transformed into eye space as well. You accomplish this by transforming
      * surface normals by the inverse transpose of the upper leftmost 3x3 matrix
      * taken from the modelview matrix. At that point, you can apply the per vertex
      * lighting formulas defined by OpenGL to determine the lit color at
      * each vertex
      */
     public static void update(GL2 gl, Camera camera){
         math.Quaternion inverseRot = camera.rotation.inverse();
         for(int i = 0; i < lights.size(); i++) {
             Light light = lights.get(i);
             gl.glLightfv(GL2.GL_LIGHT0 + i, GL2.GL_AMBIENT, light.ambient.toFloatArray(), 0);
             gl.glLightfv(GL2.GL_LIGHT0 + i, GL2.GL_DIFFUSE, light.diffuse.toFloatArray(), 0);
             gl.glLightfv(GL2.GL_LIGHT0 + i, GL2.GL_SPECULAR, light.specular.toFloatArray(), 0);
             gl.glLightfv(GL2.GL_LIGHT0 + i, GL2.GL_POSITION, light.position.times(inverseRot).toFloatArray(), 0);
            gl.glLightf(GL2.GL_LIGHT0 + i, GL2.GL_CONSTANT_ATTENUATION, light.constantAttenuation);
            gl.glLightf(GL2.GL_LIGHT0 + i, GL2.GL_LINEAR_ATTENUATION, light.linearAttenuation);
            gl.glLightf(GL2.GL_LIGHT0 + i, GL2.GL_QUADRATIC_ATTENUATION, light.quadraticAttenuation);
 
         }
     }
 }
