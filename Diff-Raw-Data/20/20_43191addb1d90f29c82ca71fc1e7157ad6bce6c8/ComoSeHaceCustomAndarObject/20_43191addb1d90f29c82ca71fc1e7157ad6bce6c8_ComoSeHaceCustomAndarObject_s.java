 package org.utn.proyecto.helpful.integrart.integrar_t_android.activities.comosehace;
 
 import java.nio.FloatBuffer;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import org.utn.proyecto.helpful.integrart.integrar_t_android.R;
 
 import android.app.Activity;
 import android.app.Application;
 import android.content.Intent;
 
 import edu.dhbw.andar.ARObject;
 import edu.dhbw.andar.pub.SimpleBox;
 import edu.dhbw.andar.util.GraphicsUtil;
 
 public class ComoSeHaceCustomAndarObject extends ARObject {
 
     private static final String VIDEO = "video";
 	private ComoSeHaceAndarActivity activity;
 
     public ComoSeHaceCustomAndarObject(String name, String patternName,
 			double markerWidth, double[] markerCenter,ComoSeHaceAndarActivity comoSeHaceAndarActivity) {
 		super(name, patternName, markerWidth, markerCenter);
 		float   mat_ambientf[]     = {0f, 1.0f, 0f, 1.0f};
 		float   mat_flashf[]       = {0f, 1.0f, 0f, 1.0f};
 		float   mat_diffusef[]       = {0f, 1.0f, 0f, 1.0f};
 		float   mat_flash_shinyf[] = {50.0f};
 		
 		//Customized
 		activity = comoSeHaceAndarActivity;
 
 		mat_ambient = GraphicsUtil.makeFloatBuffer(mat_ambientf);
 		mat_flash = GraphicsUtil.makeFloatBuffer(mat_flashf);
 		mat_flash_shiny = GraphicsUtil.makeFloatBuffer(mat_flash_shinyf);
 		mat_diffuse = GraphicsUtil.makeFloatBuffer(mat_diffusef);
 		
 	}
 	public ComoSeHaceCustomAndarObject(String name, String patternName,
 			double markerWidth, double[] markerCenter, float[] customColor, ComoSeHaceAndarActivity act) {
 		super(name, patternName, markerWidth, markerCenter);
 		float   mat_flash_shinyf[] = {50.0f};
 
 		//Customized
 		activity = act;
 		
 		mat_ambient = GraphicsUtil.makeFloatBuffer(customColor);
 		mat_flash = GraphicsUtil.makeFloatBuffer(customColor);
 		mat_flash_shiny = GraphicsUtil.makeFloatBuffer(mat_flash_shinyf);
 		mat_diffuse = GraphicsUtil.makeFloatBuffer(customColor);
 		
 	}
 	
 	/**
 	 * Just a box, imported from the AndAR project.
 	 */
 	private SimpleBox box = new SimpleBox();
 	private FloatBuffer mat_flash;
 	private FloatBuffer mat_ambient;
 	private FloatBuffer mat_flash_shiny;
 	private FloatBuffer mat_diffuse;
 	
 	/**
 	 * Everything drawn here will be drawn directly onto the marker,
 	 * as the corresponding translation matrix will already be applied.
 	 */
 	
 	@Override
 	public final void draw(GL10 gl) {
 		
 	    //super.draw(gl);
 		
 		/*gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR,mat_flash);
 		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, mat_flash_shiny);	
 		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, mat_diffuse);	
 		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, mat_ambient);
 
 	    //draw cube
 	    gl.glColor4f(0, 1.0f, 0, 1.0f);
 	    gl.glTranslatef( 0.0f, 0.0f, 12.5f );
 	    
 	    //draw the box
 	    box.draw(gl);
 	    */
	    
 	    
 	    if (super.getPatternName().equals("patt.hiro")){
 	        //double width = super.getMarkerWidth();
 	        double[] matrix = super.getTransMatrix();
 	        
	        if (matrix.length > 1)
	        executeReproductor("android.resource://" + activity.getPackageName() +"/"+R.raw.assasin);
 	    }
 	}
 	
     protected void executeReproductor(String path) {
         activity.executeReproductor(path);
     }
     
 	@Override
 	public void init(GL10 gl) {
 		
 	}
 }
