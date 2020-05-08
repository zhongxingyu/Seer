 package graphics;
 
 import game.Game;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import javax.media.opengl.GL2;
 
 import math.Vector4;
 
 public class Light implements java.io.Serializable{
     private static final long serialVersionUID = -7133937204421075824L;
     protected static Random gen = new Random();
     public static List<Light> lights = new ArrayList<Light>();
     float constantAttenuation, linearAttenuation, quadraticAttenuation;
     math.Vector4 position, ambient, diffuse, specular;
 
     public static Light newRandom(float rangeMax){
         Light light = new Light();
         light.setPosition(new Vector4(gen.nextFloat()* rangeMax,
                 gen.nextFloat() * rangeMax,
                 gen.nextFloat() * rangeMax,
                 gen.nextFloat() * rangeMax));
         light.setAmbient(new Vector4(gen.nextFloat(),
                 gen.nextFloat(),
                 gen.nextFloat(),
                 gen.nextFloat()));
         light.setDiffuse(new Vector4(gen.nextFloat(),
                 gen.nextFloat(),
                 gen.nextFloat(),
                 gen.nextFloat()));
         light.setSpecular(new Vector4(gen.nextFloat(),
                 gen.nextFloat(),
                 gen.nextFloat(),
                 gen.nextFloat()));
 
         return light;
     }
     public Light(){
         position = new Vector4();
         ambient = new Vector4();
         diffuse = new Vector4();
         specular = new Vector4();
         constantAttenuation = 100.0f;
         linearAttenuation = 500.0f;
         quadraticAttenuation = 10.0f;
     }
 
     public math.Vector4 getAmbient() {
         return ambient;
     }
 
     public float getConstantAttenuation() {
         return constantAttenuation;
     }
 
     public math.Vector4 getDiffuse() {
         return diffuse;
     }
 
     public float getLinearAttenuation() {
         return linearAttenuation;
     }
 
     public math.Vector4 getPosition() {
         return position;
     }
 
     public float getQuadraticAttenuation() {
         return quadraticAttenuation;
     }
 
     public math.Vector4 getSpecular() {
         return specular;
     }
 
     public Light setAmbient(math.Vector4 ambient) {
         this.ambient = ambient;
         return this;
     }
 
     public Light setConstantAttenuation(float constantAttenuation) {
         this.constantAttenuation = constantAttenuation;
         return this;
     }
 
     public Light setDiffuse(math.Vector4 diffuse) {
         this.diffuse = diffuse;
         return this;
     }
 
     public Light setLinearAttenuation(float linearAttenuation) {
         this.linearAttenuation = linearAttenuation;
         return this;
     }
 
     public Light setPosition(math.Vector4 position) {
         this.position = position;
         return this;
     }
 
     public Light setQuadraticAttenuation(float quadraticAttenuation) {
         this.quadraticAttenuation = quadraticAttenuation;
         return this;
     }
 
     public Light setSpecular(math.Vector4 specular) {
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
             Light.add(Light.newRandom(256));
             gl.glEnable(GL2.GL_LIGHT0 + i);
         }
         // Tell the shader how many lights we are using
        Game.getRenderer().shader.setUniform1i(gl, "numLights", numLights);
     }
 
 
     public static void update(GL2 gl){
         math.Quaternion inverseRot = Game.getPlayer().getCamera().rotation.inverse();
         for(int i = 0; i < lights.size(); i++) {
             Light light = lights.get(i);
             gl.glLightfv(GL2.GL_LIGHT0 + i, GL2.GL_AMBIENT, light.ambient.times(inverseRot).toFloatArray(), 0);
             gl.glLightfv(GL2.GL_LIGHT0 + i, GL2.GL_DIFFUSE, light.diffuse.times(inverseRot).toFloatArray(), 0);
             gl.glLightfv(GL2.GL_LIGHT0 + i, GL2.GL_SPECULAR, light.specular.times(inverseRot).toFloatArray(), 0);
             gl.glLightfv(GL2.GL_LIGHT0 + i, GL2.GL_POSITION, light.position.times(inverseRot).toFloatArray(), 0);
         }
     }
 }
