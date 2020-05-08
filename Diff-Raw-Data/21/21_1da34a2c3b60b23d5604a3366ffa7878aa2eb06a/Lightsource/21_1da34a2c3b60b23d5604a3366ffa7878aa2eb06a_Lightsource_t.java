 import javax.media.opengl.GL2;
 import javax.media.opengl.fixedfunc.GLLightingFunc;
 
 
 /**
  * Class to hold info about a lightsource
  * @author Robert
  *
  */
 public class Lightsource {
 	private float[] pos;
     private float[] lightColor;
    private float[] ambientColor;
     private int lightnum;
     
	public Lightsource(int lightnum, float[] positions, float[] color, float[] ambcolor) {
 		this.lightnum = lightnum + 16384; //the enum for lights start at this value
 		pos = positions;
 		lightColor = color;
		ambientColor = ambcolor;
 	}
 	
 	public void draw(GL2 gl) {
 		gl.glLightfv(lightnum, GLLightingFunc.GL_POSITION, pos, 0);
 	}
 	
 	public void init(GL2 gl) {
 		gl.glLightfv(lightnum, GLLightingFunc.GL_SPECULAR, lightColor, 0);
		gl.glLightfv(lightnum, GLLightingFunc.GL_POSITION, pos, 0);
 		gl.glEnable(lightnum);
 	}
 }
