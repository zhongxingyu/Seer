 
 package importing.pieces;
 
 import java.nio.FloatBuffer;
 import java.util.ArrayList;
 
 import javax.vecmath.Vector3f;
 import org.lwjgl.BufferUtils;
 
 public class Material {
 	//For all of these:  x = red, y = green, b = blue
 	private Vector3f ambientReflect; 	
 	private Vector3f diffuseReflect;	
 	private Vector3f specularReflect;	//default 0.0f, 0.0f, 0.0f
 	private Vector3f emission; //default 0.0f, 0.0f, 0.0f
 	static private FloatBuffer buffer = BufferUtils.createFloatBuffer(4);
 	private float alpha; //default 1.0f
 	private float shine; //default 0.0f
 	
 	public Material(){
 		matInit();
 	}
 
 	public Material(Vector3f ambient, Vector3f diffuse){
 		matInit();
 		ambientReflect = new Vector3f(ambient.x,ambient.y,ambient.z);
 		diffuseReflect = new Vector3f(diffuse.x,diffuse.y,diffuse.z);
 	}
 	
 	public Material( Vector3f ambient, Vector3f diffuse, Vector3f specular){
 		matInit();
 		ambientReflect = new Vector3f(ambient.x,ambient.y,ambient.z);
 		diffuseReflect = new Vector3f(diffuse.x,diffuse.y,diffuse.z);
 		specularReflect = new Vector3f(specular.x,specular.y,specular.z);
 	}
 	
 	public Material( Vector3f ambient, Vector3f diffuse, Vector3f specular, Vector3f emis){
 		matInit();
 		ambientReflect = new Vector3f(ambient.x,ambient.y,ambient.z);
 		diffuseReflect = new Vector3f(diffuse.x,diffuse.y,diffuse.z);
 		specularReflect = new Vector3f(specular.x,specular.y,specular.z);
 		emission = new Vector3f(emis.x,emis.y,emis.z);
 	}
 	
 	public Material( Vector3f ambient, Vector3f diffuse, Vector3f specular, Vector3f emis, float a){
 		matInit();
 		ambientReflect = new Vector3f(ambient.x,ambient.y,ambient.z);
 		diffuseReflect = new Vector3f(diffuse.x,diffuse.y,diffuse.z);
 		specularReflect = new Vector3f(specular.x,specular.y,specular.z);
 		emission = new Vector3f(emis.x,emis.y,emis.z);
 		alpha = a;
 	}
 	
 	public Material( Vector3f ambient, Vector3f diffuse, Vector3f specular, Vector3f emis, float a, float s){
 		matInit();
 		ambientReflect = new Vector3f(ambient.x,ambient.y,ambient.z);
 		diffuseReflect = new Vector3f(diffuse.x,diffuse.y,diffuse.z);
 		specularReflect = new Vector3f(specular.x,specular.y,specular.z);
 		emission = new Vector3f(emis.x,emis.y,emis.z);
 		alpha = a;
 		shine = s;
 	}
 	
 	//Deep Copy Constructor
 	public Material(Material mat) {
 		this.ambientReflect = new Vector3f(mat.ambientReflect.x,mat.ambientReflect.y,mat.ambientReflect.z); 	
 		this.diffuseReflect = new Vector3f(mat.diffuseReflect.x,mat.diffuseReflect.y,mat.diffuseReflect.z);	
 		this.specularReflect = new Vector3f(mat.specularReflect.x,mat.specularReflect.y,mat.specularReflect.z);
 		this.emission = new Vector3f(mat.emission.x,mat.emission.y,mat.emission.z); 
 		this.alpha = mat.alpha;
 		this.shine = mat.shine;
 	}
 	
 	private void matInit() {
 		ambientReflect = new Vector3f(0.216f,0.263f,0.333f);
 		diffuseReflect = new Vector3f(0.502f,0.502f,0.502f);
 		
 		specularReflect = new Vector3f(0.0f,0.0f,0.0f);
 		
 		emission = new Vector3f(0.0f,0.0f,0.0f);	
 		
 		alpha = 1.0f;
 		shine = 0.5f;
 	}
 
 	public Vector3f getAmbient() {
 		return ambientReflect;
 	}
 	
 	public FloatBuffer getAmbientAsBuffer(){
 		float[] temp = new float[3];
 		temp[0] = ambientReflect.x;
 		temp[1] = ambientReflect.y;
 		temp[2] = ambientReflect.z;
 		
 		buffer.clear();
 		buffer.put(temp);
 		buffer.put(1.0f);
 		buffer.flip();
 		return buffer;
 	}
 	
 	
 	public Vector3f getDiffuse() {
 		return diffuseReflect;
 	}
 	public FloatBuffer getDiffuseAsBuffer(){
 		float[] temp = new float[3];
 		temp[0] = diffuseReflect.x;
 		temp[1] = diffuseReflect.y;
 		temp[2] = diffuseReflect.z;
 		
 		buffer.clear();
 		buffer.put(temp);
 		buffer.put(1.0f);
 		buffer.flip();
 		return buffer;
 	}
 	
 	public Vector3f getSpecular() {
 		return specularReflect;
 	}
 	public FloatBuffer getSpecularAsBuffer(){
 		float[] temp = new float[3];
 		temp[0] = specularReflect.x;
 		temp[1] = specularReflect.y;
 		temp[2] = specularReflect.z;
 		
 		buffer.clear();
 		buffer.put(temp);
 		buffer.put(1.0f);
 		buffer.flip();
 		return buffer;
 	}
 	
 	
 	public Vector3f getEmission() {
 		return emission;
 	}
 	public FloatBuffer getEmissionAsBuffer(){
 		float[] temp = new float[3];
 		temp[0] = emission.x;
 		temp[1] = emission.y;
 		temp[2] = emission.z;
 		
 		buffer.clear();
 		buffer.put(temp);
 		buffer.put(1.0f);
 		buffer.flip();
 		return buffer;
 	}
 	
 	public float getAlpha() {
 		return alpha;
 	}
 	
 	public float getShine() {
 		return shine;
 	}
 	
 	public void setEmission(Vector3f emiss){
 		emission = emiss;
 	}
 	
 	public void setAlpha( float alph ){
 		alpha = alph;
 	}
 	
 	public void setShine( float _shine){
 		shine = _shine;
 	}
 
 	public void setAmbient(Vector3f floats) {
 		ambientReflect = floats;
 	}
 	
 	public void setDiffuse(Vector3f d){
 		diffuseReflect = d;
 	}
 	
 	public void setSpecular(Vector3f s){
 		specularReflect = s;
 	}
 	
 	public String toString(){
 		String ret = "";
 		ret += "			ambientReflect: (" + ambientReflect.x + "," + ambientReflect.y + "," + ambientReflect.z + ")\n";
 		ret += "			diffuseReflect: (" + diffuseReflect.x + "," + diffuseReflect.y + "," + diffuseReflect.z + ")\n";
 		ret += "			specularReflect: (" + specularReflect.x + "," + specularReflect.y + "," + specularReflect.z + ")\n";
 		ret += "			emission: (" + emission.x + "," + emission.y + "," + emission.z + ")\n";
 		ret += "			alpha: " + String.valueOf(alpha) + "\n";
 		ret += "			shine: " + String.valueOf(shine) + "\n";
 		return ret;
 	}
 	
 	public StringBuffer toXGLString(int ref){
 		StringBuffer data = new StringBuffer();
 		data.append("<MAT ID=\"" + ref + "\">\n");
 		data.append("<AMB>" + String.valueOf(ambientReflect.x) + ", " + String.valueOf(ambientReflect.y) + ", " + String.valueOf(ambientReflect.z) + "</AMB>\n");
 		data.append("<DIFF>" + String.valueOf(diffuseReflect.x) + ", " + String.valueOf(diffuseReflect.y) + ", " + String.valueOf(diffuseReflect.z) + "</DIFF>\n");
 		data.append("<SPEC>" + String.valueOf(specularReflect.x) + ", " + String.valueOf(specularReflect.y) + ", " + String.valueOf(specularReflect.z) + "</SPEC>\n");
 		data.append("<EMISS>" + String.valueOf(emission.x) + ", " + String.valueOf(emission.y) + ", " + String.valueOf(emission.z) + "</EMISS>\n");
 		data.append("<ALPHA>" + String.valueOf(alpha) + "</ALPHA>\n");
 		data.append("<SHINE>" + String.valueOf(shine) + "</SHINE>\n");
 		data.append("</MAT>\n");
 		return data;
 	}
 	
 	public ArrayList<Byte> getColor() {
 		ArrayList<Byte> color = new ArrayList<Byte>();
 		
		color.add(byteAverage(ambientReflect.x, diffuseReflect.x,specularReflect.x));
		color.add(byteAverage(ambientReflect.y, diffuseReflect.y,specularReflect.y));
		color.add(byteAverage(ambientReflect.z, diffuseReflect.z,specularReflect.z));
		
		return color;
	}
	
	public ArrayList<Float> getFloatColor() {
		ArrayList<Float> color = new ArrayList<Float>();
		
 		color.add(average(ambientReflect.x, diffuseReflect.x,specularReflect.x));
 		color.add(average(ambientReflect.y, diffuseReflect.y,specularReflect.y));
 		color.add(average(ambientReflect.z, diffuseReflect.z,specularReflect.z));
 		
 		return color;
 	}
 	
	private byte byteAverage(float a, float b, float c){
 		return (byte)(((a*255)+(b*255)+(c*255))/3);
 	}
	private float average(float a, float b, float c){
		return (a+b+c)/3;
	}
 }
