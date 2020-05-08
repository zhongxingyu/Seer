 package net.qmat.qmhh.models;
 
 import java.nio.FloatBuffer;
 import javax.media.opengl.GL;
 import com.sun.opengl.util.BufferUtil;
 import net.qmat.qmhh.Main;
 import net.qmat.qmhh.utils.Settings;
 import processing.core.PVector;
 import processing.opengl.PGraphicsOpenGL;
 
 public class OrbModel extends ProcessingObject {
 
 	private float radius, r;
 	private float maxRadius, minRadius;
 
 	private int numSystems = 60;
 	private int numParticles = 20;
 	private OrbPSystem[] ps;
 	private float theta, theta2 = 0.0f;
 	private float amplitude; 
 	
 	private FloatBuffer vbuffer;
 	private int totalParticles;
 
 	public OrbModel() {
 		ps =  new OrbPSystem[numSystems];
 		maxRadius = Settings.getInteger(Settings.PR_ORB_MAX_RADIUS);
 		minRadius = Settings.getInteger(Settings.PR_ORB_MIN_RADIUS);
 		this.radius = 10.0f;
 		this.r = 10.0f;
 
 		float inx = p.width/2.0f;
 		float iny = p.height/2.0f;
 		float x, y;
 		
 		totalParticles = numParticles * numSystems;
 		vbuffer = BufferUtil.newFloatBuffer(totalParticles * 2);
 
 		for(int i=0; i<numSystems; i++){
 			// dispose PSystems in a circle
 			x = r * Main.cos(theta);
 			y = r * Main.sin(theta);
 			x += inx;
 			y += iny;     
 			ps[i] = new OrbPSystem(new PVector(x, y), 
 								   numParticles, 
 								   theta, 
 								   r,
 								   vbuffer);
 			theta += Main.TWO_PI / numSystems;
 		}
 		amplitude = r;
 	}
 
 	public void draw() {
 		p.noStroke();
 		p.fill(120, 120, 245, 30);
		p.ellipseMode(Main.CENTER);
 		p.ellipse(Main.centerX, Main.centerY, radius*3, radius*3);
 		p.ellipse(Main.centerX, Main.centerY, radius*2, radius*2);
 		p.ellipse(Main.centerX, Main.centerY, radius, radius);
 		
 		waveR();
 		for(int i=0; i<numSystems; i++) {
 			ps[i].draw();
 		}
 
 		vbuffer.rewind();
 		
 		PGraphicsOpenGL pgl = (PGraphicsOpenGL) p.g;  // g may change
 	    GL gl = pgl.beginGL();  // always use the GL object returned by beginGL
 	     
 	    gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
 	    gl.glVertexPointer(2, GL.GL_FLOAT, 0, vbuffer);
 
 	    gl.glPointSize(8.0f);
 	    gl.glColor4f(0.39f, 0.5f, 1.0f, 0.05f);
 	    gl.glDrawArrays(GL.GL_POINTS, 0, totalParticles);
 
 	    gl.glPointSize(1.0f);
 	    gl.glColor4f(0.47f, 0.47f, 1.0f, 0.59f);
 	    gl.glDrawArrays(GL.GL_POINTS, 0, totalParticles);
 	     
 	    pgl.endGL();
 	}
 
 	private void waveR()
 	{
 		theta += 0.05;
 		r = theta;
 		r = Main.sin(r) * amplitude;
 		r += radius;
 		for(int i=0; i<numSystems; i++) {
 			ps[i].setR(r);
 		}
 	}
 
 	public float getRadius() {
 		return radius;
 	}
 
 	public void setRadius(float radius) {
 		if(radius >= minRadius && radius <= maxRadius) {
 			this.radius = radius;
 			amplitude = radius;
 		}
 	}
 
 	public void increaseRadius() {
 		setRadius(radius+5.0f);
 	}
 
 	public void decreaseRadius() {
 		setRadius(radius-5.0f);
 	}
 
 }
