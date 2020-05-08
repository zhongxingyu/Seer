 package com.ecosistir.animals;
 
 import java.nio.FloatBuffer;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import edu.dhbw.andar.pub.SimpleBox;
 import edu.dhbw.andar.util.GraphicsUtil;
 
 public class Abrelatas extends Animal
 {
 	
 	private SimpleBox box = new SimpleBox();
 	private FloatBuffer mat_flash;
 	private FloatBuffer mat_ambient;
 	private FloatBuffer mat_flash_shiny;
 	private FloatBuffer mat_diffuse;
 	
 	
 	private Abrelatas(String name, String patternName, double markerWidth,
 			double[] markerCenter)
 	{
 		super(name, patternName, markerWidth, markerCenter);
 		
 		float   mat_ambientf[]     = {0f, 1.0f, 0f, 1.0f};
 		float   mat_flashf[]       = {0f, 1.0f, 0f, 1.0f};
 		float   mat_diffusef[]       = {0f, 1.0f, 0f, 1.0f};
 		float   mat_flash_shinyf[] = {50.0f};
 
 		mat_ambient = GraphicsUtil.makeFloatBuffer(mat_ambientf);
 		mat_flash = GraphicsUtil.makeFloatBuffer(mat_flashf);
 		mat_flash_shiny = GraphicsUtil.makeFloatBuffer(mat_flash_shinyf);
 		mat_diffuse = GraphicsUtil.makeFloatBuffer(mat_diffusef);
 	}
 	
 	/**
 	 * 
 	 */
 	@Override
 	public final void draw(GL10 gl)
 	{
 		super.draw(gl);
 		
 		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR,mat_flash);
 		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, mat_flash_shiny);	
 		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, mat_diffuse);	
 		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, mat_ambient);
 
 	    //draw cube
 	    gl.glColor4f(0, 1.0f, 0, 1.0f);
 	    gl.glTranslatef( 0.0f, 0.0f, 12.5f );
 	    
 	    //draw the box
 	    box.draw(gl);
 	}
 	
 	/**
 	 * Get a instance of Abrelatas.
 	 * @return Abrelatas
 	 */
 	public static Abrelatas getInstance()
 	{
 		//("test", "patt.hiro", 80.0, new double[]{0,0});
 		String name = "Abrelatas";
		String patternName = "barcode.patt"; 
 		double markerWidth = 80.0;
 		double[] markerCenter = new double[]{0.0};
 		return new Abrelatas(name, patternName, markerWidth, markerCenter);
 	}
 	
 }
