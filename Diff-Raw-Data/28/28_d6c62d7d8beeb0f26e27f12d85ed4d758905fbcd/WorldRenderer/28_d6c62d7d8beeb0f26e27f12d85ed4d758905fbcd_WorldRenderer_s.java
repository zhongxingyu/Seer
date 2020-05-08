 package com.gmail.quarzekk.worldstudio.ui.render;
 
 import static org.lwjgl.opengl.ARBVertexBufferObject.*;
 import static org.lwjgl.opengl.GL11.*;
 import static org.lwjgl.util.glu.GLU.*;
 
 import java.nio.FloatBuffer;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.opengl.Display;
 
import com.gmail.quarzekk.worldstudio.ui.render.component.ChunkRenderer;
 import com.gmail.quarzekk.worldstudio.world.Chunk;
 import com.gmail.quarzekk.worldstudio.world.World;
 
 public class WorldRenderer implements IRenderer {
 	
 	/**
	 * The ChunkRenderer from which this renderer gets chunk render data.
 	 */
	private ChunkRenderer chunkRenderer;
 	
 	/**
 	 * A reference to the world with which this renderer is concerned.
 	 */
 	private World world;
 	
 	/**
 	 * A reference to the camera from which the scene is viewed.
 	 */
 	private Camera camera;
 	
 	public WorldRenderer() {
		this.chunkRenderer = new ChunkRenderer();
 	}
 	
 	@Override
 	public void update() {
 		this.camera.update();
 		
 		glMatrixMode(GL_PROJECTION);
 		
 		gluPerspective(45.0F, (float) Display.getWidth()/(float) Display.getHeight(), 0.0F, 100.0F);
 		
 		glMatrixMode(GL_MODELVIEW);
 		
		glTranslated(-this.camera.getPositionX(), -this.camera.getPositionY(), -this.camera.getPositionZ());
		
 		glRotated(-this.camera.getRotationX(), 1.0D, 0.0D, 0.0D);
 		glRotated(-this.camera.getRotationY(), 0.0D, 1.0D, 0.0D);
 		glRotated(-this.camera.getRotationZ(), 0.0D, 0.0D, 1.0D);
 		
 		this.render();
 	}
 	
 	/**
 	 * Gets the world rendered by this renderer.
 	 * @return The World rendered by this renderer
 	 */
 	public World getWorld() {
 		return this.world;
 	}
 	
 	/**
 	 * Sets the world to render with this renderer.
 	 * @param world The World to render
 	 */
 	public void setWorld(World world) {
 		this.world = world;
 	}
 	
 	/**
 	 * Gets the camera from which the scene is viewed.
 	 * @return The camera viewing the scene
 	 */
 	public Camera getCamera() {
 		return this.camera;
 	}
 	
 	/**
 	 * Sets the camera from which to view the scene.
 	 * @param camera The camera to view the scene
 	 */
 	public void setCamera(Camera camera) {
 		this.camera = camera;
 	}
 	
 	/**
 	 * Renders the active world. It is not necessary to expose this method
 	 * publicly, as the modularization is merely for code readability.
 	 */
 	private void render() {
 		if (this.world != null) {
 			Iterable<Chunk> chunks = this.world.getLoadedChunks();
 			
 			for (Chunk chunk : chunks) {
 				for (int section = 0; section < 8; section++) {
 					if (chunk.getVboId(section) == 0) {
 						int vboId = glGenBuffersARB();
 						
 						chunk.setVboId(section, vboId);
 						chunk.setVboUpdateRequired(section, true);
 					}
 					
 					if (chunk.getVboUpdateRequired(section)) {
						FloatBuffer buffer = BufferUtils.createFloatBuffer(16*16*16*128); // TODO: Load from config
 						
						int numElements = this.chunkRenderer.renderSection(buffer, chunk, section);
 						
 						buffer.flip();
 						
 						glBindBufferARB(GL_ARRAY_BUFFER_ARB, chunk.getVboId(section));
 						glBufferDataARB(GL_ARRAY_BUFFER_ARB, buffer, GL_STATIC_DRAW_ARB);
 						glBindBufferARB(GL_ARRAY_BUFFER_ARB, 0);
 						
 						chunk.setVboUpdateRequired(section, false);
 						chunk.setNumVboElements(section, numElements);
 					}
 					
 					glPushMatrix();
 					
 					glEnableClientState(GL_VERTEX_ARRAY);
 					
 					glBindBufferARB(GL_ARRAY_BUFFER_ARB, chunk.getVboId(section));
 					glVertexPointer(3, GL_FLOAT, 0, 0L);
 					glDrawArrays(GL_TRIANGLES, 0, chunk.getNumVboElements(section));
 					glBindBufferARB(GL_ARRAY_BUFFER_ARB, 0);
 					
 					glDisableClientState(GL_VERTEX_ARRAY);
 					
 					glPopMatrix();
 				}
 			}
 		}
 	}
 	
 }
