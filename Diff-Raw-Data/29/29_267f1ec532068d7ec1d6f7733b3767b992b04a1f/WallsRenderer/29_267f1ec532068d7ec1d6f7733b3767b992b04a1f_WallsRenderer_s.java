 package edu.agh.tunev.ui.opengl;
 
 import java.awt.geom.Point2D;
 
 import javax.media.opengl.GL2;
 
 import edu.agh.tunev.world.World;
 
 final class WallsRenderer implements Renderable {
 
 	private final World world;
 
 	public WallsRenderer(World world) {
 		this.world = world;
 	}
 
 	private static final double thickness = 0.3;
 	private static final double height = 2.0;
 	/** ile względem thickness ściana zachodzi na podłogę */
 	private static final double overlap = 0.1;
 
 	@Override
 	public void render(GL2 gl, double t) {
 		gl.glPushMatrix();
 		gl.glColor4d(1, 1, 1, 1);
 
 		Point2D.Double dim = world.getDimension();
		
		int numseg = (int)Math.round(Math.ceil(dim.y));
 
 		gl.glPushMatrix();
 		gl.glTranslated(-thickness * (1 - overlap), 0, 0);
		Common.drawCuboid(gl, thickness, 1, height, 1, dim.y, numseg);
 		gl.glPopMatrix();
 
 		gl.glPushMatrix();
 		gl.glTranslated(dim.x - thickness * overlap, 0, 0);
		Common.drawCuboid(gl, thickness, 1, height, 1, dim.y, numseg);
 		gl.glPopMatrix();
 
 		gl.glPopMatrix();
 	}
 
 }
