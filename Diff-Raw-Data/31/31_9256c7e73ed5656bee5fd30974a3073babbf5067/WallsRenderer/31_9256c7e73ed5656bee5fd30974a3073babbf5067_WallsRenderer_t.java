 package edu.agh.tunev.ui.opengl;
 
 import javax.media.opengl.GL2;
 
 import edu.agh.tunev.world.World;
 
 final class WallsRenderer implements Renderable {
 
 	private final World world;
 
 	public WallsRenderer(World world) {
 		this.world = world;
 	}
 
 	@Override
 	public void render(GL2 gl, double t) {
 		// TODO Auto-generated method stub
		world.getDimension();
 	}
 
 }
