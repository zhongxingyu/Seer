 package de.tum.in.jrealityplugin.jogl;
 
 import java.util.Collection;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GL2;
 
 import com.jogamp.opengl.util.glsl.ShaderCode;
 import com.jogamp.opengl.util.glsl.ShaderProgram;
 
 class PointRenderer extends Renderer<Point> {
 	private ShaderProgram program = null;
 	private int centerLoc;
 	private int colorLoc;
 	private int radiusLoc;
 
 	public PointRenderer() {
 	}
 
 	@Override
 	public boolean init(GL gl) {
 		GL2 gl2 = gl.getGL2();
 
 		program = new ShaderProgram();
 		ShaderCode vertexShader = loadShader(
 				GL2.GL_VERTEX_SHADER,
 				getClass()
 						.getResource(
 								"/de/tum/in/jrealityplugin/resources/shader/sphere.vert"));
 		if (!vertexShader.compile(gl2))
 			return false;
 		ShaderCode fragmentShader = loadShader(
 				GL2.GL_FRAGMENT_SHADER,
 				getClass()
 						.getResource(
 								"/de/tum/in/jrealityplugin/resources/shader/sphere.frag"));
 		if (!fragmentShader.compile(gl2))
 			return false;
 
 		if (!program.add(vertexShader))
 			return false;
 		if (!program.add(fragmentShader))
 			return false;
 		if (!program.link(gl.getGL2(), null))
 			return false;
 
		centerLoc = gl2.glGetUniformLocation(program.program(), "sphereCenter");
		colorLoc = gl2.glGetUniformLocation(program.program(), "sphereColor");
		radiusLoc = gl2.glGetUniformLocation(program.program(), "sphereRadius");
 
 		return true;
 	}
 
 	@Override
 	public void render(GL gl, Collection<Point> points) {
 		if (points.isEmpty())
 			return;
 
 		GL2 gl2 = gl.getGL2();
 
 		gl2.glUseProgram(program.program());
 		for (Point p : points) {
			gl2.glUniform3f(centerLoc, (float) p.x, (float) p.y, (float) p.z);
			gl2.glUniform3fv(colorLoc, 1, p.color.getColorComponents(null), 0);
 			gl2.glUniform1f(radiusLoc, (float) p.size * 0.025f);
 			// gl2.glFlush();
 			gl2.glBegin(GL2.GL_QUADS);
 			gl2.glVertex2f(-1, -1);
 			gl2.glVertex2f(1, -1);
 			gl2.glVertex2f(1, 1);
 			gl2.glVertex2f(-1, 1);
 			gl2.glEnd();
 		}
 		gl2.glUseProgram(0);
 	}
 
 	@Override
 	public void dispose(GL gl) {
 		if (program != null)
 			program.destroy(gl.getGL2());
 	}
 }
