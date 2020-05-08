 package de.tum.in.cindy3dplugin.jogl.primitives.renderers.fixedfunc;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GL2;
 
 import de.tum.in.cindy3dplugin.jogl.Util;
 import de.tum.in.cindy3dplugin.jogl.primitives.Sphere;
 import de.tum.in.cindy3dplugin.jogl.primitives.renderers.PrimitiveRenderer;
 import de.tum.in.cindy3dplugin.jogl.renderers.JOGLRenderState;
 import de.tum.in.cindy3dplugin.jogl.renderers.JOGLRenderState.CullMode;
 
 public class SphereRenderer extends PrimitiveRenderer<Sphere> {
 	private static final int LOD_COUNT = 8;
 	
 	private LODMesh[] meshes = new LODMesh[LOD_COUNT];
 
 	private LODMesh createMesh(GL gl, int stacks, int slices) {
 		GL2 gl2 = gl.getGL2();
 		
 		int vertexCount = 2 + slices * (stacks - 1);
 		int faceCount = 2 * slices + // stacks at poles, triangles
 				(stacks - 2) * slices * 2; // inner stacks loops, quads (2
 										   // triangles)
 		
 		LODMesh mesh = new LODMesh(3, vertexCount, faceCount, true);
 		
 		/*
 		 * Generate vertices
 		 */
 		double[] vertex = new double[3];
 		
 		vertex[0] = vertex[1] = 0;
 		vertex[2] = -1;
 		mesh.putVertex(vertex, vertex);
 		
 		for (int stack = 1; stack < stacks; ++stack) {
 			double latitude = (((double) stack) / stacks - 0.5) * Math.PI;
 			for (int slice = 0; slice < slices; ++slice) {
 				double longitude = ((double) slice) / slices * Math.PI * 2;
 				vertex[0] = Math.cos(longitude)*Math.cos(latitude);
 				vertex[1] = Math.sin(longitude)*Math.cos(latitude);
 				vertex[2] = Math.sin(latitude);
 				mesh.putVertex(vertex, vertex);
 				//mesh.putVertex(vertex);
 			}
 		}
 		
 		vertex[0] = vertex[1] = 0;
 		vertex[2] = 1;
 		mesh.putVertex(vertex, vertex);
 	
 		/*
 		 * Generate indices
 		 */
 		int stackOffset = 0;		// First vertex of current stack
 		int nextStackOffset = 1;	// First vertex of next stack
 		
 		// South pole
 		for (int slice = 0; slice < slices; ++slice) {
 			mesh.putFace(
 					stackOffset,
 					nextStackOffset + (slice + 1) % slices,
 					nextStackOffset + slice);
 		}
 		
 		for (int stack = 1; stack < stacks - 1; ++stack) {
 			stackOffset = nextStackOffset;
 			nextStackOffset += slices;
 			for (int slice = 0; slice < slices; ++slice) {
 				mesh.putFace(
 						stackOffset     + slice,
 						nextStackOffset + (slice + 1) % slices,
 						nextStackOffset + slice);
 				mesh.putFace(
 						stackOffset     + slice,
 						stackOffset     + (slice + 1) % slices,
 						nextStackOffset + (slice + 1) % slices);
 			}
 		}
 		
 		// North pole
 		for (int slice = 0; slice < slices; ++slice) {
 			mesh.putFace(
 					nextStackOffset + slices,
 					nextStackOffset + slice,
 					nextStackOffset + (slice + 1) % slices);
 		}
 	
 		mesh.finish(gl2);
 		return mesh;
 	}
 
 	@Override
 	public boolean init(GL gl) {
 		for (int lod = 0; lod < LOD_COUNT; ++lod) {
 			int stacks = lod + 2;
 			int slices = 2 * stacks;
 			
 			meshes[lod] = createMesh(gl, stacks, slices);
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
 		gl2.glEnable(GL2.GL_NORMALIZE);
 		gl2.glEnableClientState(GL2.GL_VERTEX_ARRAY);
 		gl2.glEnableClientState(GL2.GL_NORMAL_ARRAY);
 		if (jrs.cullMode == CullMode.CULL_FRONT) {
 			gl2.glEnable(GL2.GL_CULL_FACE);
 			gl2.glCullFace(GL2.GL_FRONT);
 		} else if (jrs.cullMode == CullMode.CULL_BACK) {
 			gl2.glEnable(GL2.GL_CULL_FACE);
 			gl2.glCullFace(GL2.GL_BACK);
 		}
 	}
 
 	@Override
 	public void postRender(JOGLRenderState jrs) {
 		GL2 gl2 = jrs.gl.getGL2();
 		gl2.glDisable(GL2.GL_NORMALIZE);
 		gl2.glDisableClientState(GL2.GL_VERTEX_ARRAY);
 		gl2.glDisableClientState(GL2.GL_NORMAL_ARRAY);
 	}
 
 	@Override
 	protected void render(JOGLRenderState jrs, Sphere sphere) {
 		GL2 gl2 = jrs.gl.getGL2();
 
		double distance = Util.transformPoint(jrs.camera.getTransform(),
				sphere.getCenter()).getNorm()
 				- sphere.getRadius();
 		double allowedWorldSpaceError = jrs.camera.getWorldSpaceError(
 				jrs.renderHints.getAllowedScreenSpaceError(), distance);
 		LODMesh mesh = meshes[LOD_COUNT - 1];
 		int lod;
 		for (lod = 0; lod < LOD_COUNT; ++lod) {
 			if (meshes[lod].isSufficient(sphere.getRadius(),
 					allowedWorldSpaceError)) {
 				mesh = meshes[lod];
 				break;
 			}
 		}
 
 		gl2.glMatrixMode(GL2.GL_MODELVIEW);
 		gl2.glPushMatrix();
 		gl2.glTranslated(sphere.getCenter().getX(), sphere.getCenter().getY(),
 				sphere.getCenter().getZ());
 		gl2.glScaled(sphere.getRadius(), sphere.getRadius(), sphere.getRadius());
 
 		mesh.render(gl2);
 
 		gl2.glPopMatrix();
 	}
 }
