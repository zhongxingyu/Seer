 package de.tum.in.cindy3dplugin.jogl.primitives.renderers.fixedfunc;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GL2;
 
 import de.tum.in.cindy3dplugin.jogl.Util;
 import de.tum.in.cindy3dplugin.jogl.primitives.Circle;
 import de.tum.in.cindy3dplugin.jogl.primitives.renderers.CircleRendererBase;
 import de.tum.in.cindy3dplugin.jogl.renderers.JOGLRenderState;
 
 public class CircleRenderer extends CircleRendererBase {
 	private static final int LOD_COUNT = 8;
 	
 	private LODMesh[] meshes = new LODMesh[LOD_COUNT];
 	
 	private LODMesh createMesh(GL gl, int loops, int slices) {
 		GL2 gl2 = gl.getGL2();
 		
 		int vertexCount = 1 + slices * loops;
 		int faceCount = slices + // inner loop, triangles
 				(loops - 1) * slices * 2; // outer loops, quads (2
 										  // triangles)
 		
 		LODMesh mesh = new LODMesh(2, vertexCount, faceCount, true);
 		
 		/*
 		 * Generate vertices
 		 */
 		double[] vertex = new double[2];
 		
 		double[] normal = new double[]{0,0,1};
 		
 		vertex[0] = vertex[1] = 0;
 		mesh.putVertex(vertex, normal);
 		
 		for (int loop = 0; loop < loops; ++loop) {
 			double radius = ((double) (loop + 1)) / loops;
 			for (int slice = 0; slice < slices; ++slice) {
 				double angle = ((double) slice) / slices * Math.PI * 2;
 				vertex[0] = Math.cos(angle) * radius;
 				vertex[1] = Math.sin(angle) * radius;
 				mesh.putVertex(vertex, normal);
 			}
 		}
 		
 		/*
 		 * Generate indices
 		 */
 		int loopOffset = 0; 	// First vertex of current loop
 		int nextLoopOffset = 1;	// First vertex of next loop
 		
 		// Inner loop
 		for (int slice = 0; slice < slices; ++slice) {
 			mesh.putFace(
 					loopOffset,
 					nextLoopOffset + slice,
 					nextLoopOffset + (slice + 1) % slices);
 		}
 
 		// Outer loops
 		for (int loop = 1; loop < loops; ++loop) {
 			loopOffset = nextLoopOffset;
 			nextLoopOffset += slices;
 			for (int slice = 0; slice < slices; ++slice) {
 				mesh.putFace(
 						loopOffset     + slice,
 						nextLoopOffset + slice,
 						nextLoopOffset + (slice + 1) % slices);
 				mesh.putFace(
 						loopOffset     + slice,
 						nextLoopOffset + (slice + 1) % slices,
 						loopOffset     + (slice + 1) % slices);
 			}
 		}
 		
 		mesh.finish(gl2);
 		
 		return mesh;
 	}
 
 	@Override
 	public boolean init(GL gl) {
 		for (int lod = 0; lod < LOD_COUNT; ++lod) {
 			int loops = lod + 1;
 			int slices = 4 * loops;
 			meshes[lod] = createMesh(gl, loops, slices);
 		}
 
 		return true;
 	}
 
 	@Override
 	public void dispose(GL gl) {
 		GL2 gl2 = gl.getGL2();
 		for (LODMesh mesh : meshes) {
 			mesh.dispose(gl2);
 		}
 	}
 
 	@Override
 	public void preRender(JOGLRenderState jrs) {
 		GL2 gl2 = jrs.gl.getGL2();
 		gl2.glDisable(GL2.GL_CULL_FACE);
 		gl2.glEnableClientState(GL2.GL_VERTEX_ARRAY);
 		//gl2.glEnableClientState(GL2.GL_NORMAL_ARRAY);
 	}
 
 	@Override
 	public void postRender(JOGLRenderState jrs) {
 		GL2 gl2 = jrs.gl.getGL2();
 		gl2.glDisableClientState(GL2.GL_VERTEX_ARRAY);
 		//gl2.glDisableClientState(GL2.GL_NORMAL_ARRAY);
 	}
 
 	@Override
 	protected void render(JOGLRenderState jrs, Circle circle) {
 		
 		GL2 gl2 = jrs.gl.getGL2();
 		
		double distance = -Util.transformPoint(jrs.camera.getTransform(),
				circle.getCenter()).getZ()
 				- circle.getRadius();
 		double allowedWorldSpaceError = jrs.camera.getWorldSpaceError(
 				jrs.renderHints.getAllowedScreenSpaceError(), distance);
 		LODMesh mesh = meshes[LOD_COUNT - 1];
 		int lod;
 		for (lod = 0; lod < LOD_COUNT; ++lod) {
 			if (meshes[lod].isSufficient(circle.getRadius(),
 					allowedWorldSpaceError)) {
 				mesh = meshes[lod];
 				break;
 			}
 		}
 
 		gl2.glMatrixMode(GL2.GL_MODELVIEW);
 		gl2.glPushMatrix();
 		
 		gl2.glMultTransposeMatrixf(buildTransform(circle), 0);
 
 		mesh.render(gl2);
 
 		gl2.glPopMatrix();
 	}
 }
